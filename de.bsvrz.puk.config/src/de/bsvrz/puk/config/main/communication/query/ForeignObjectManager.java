/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDynamicObjectType;
import de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet;
import de.bsvrz.puk.config.localCache.PersistentDynamicObjectCache;
import de.bsvrz.puk.config.main.communication.async.AsyncIdsToObjectsRequest;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ForeignObjectManager {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final ClientDavInterface _connection;

	private final DataModel _configuration;

	private final SystemObject _localAuthority;

	private final Map<String, ForeignConfigRequester> _authorityPid2Requester = new HashMap<String, ForeignConfigRequester>();

	private ScheduledExecutorService _executor;

	private HashMap<Long, SystemObject> _foreignObjects = new HashMap<Long, SystemObject>();

	private ForeignConfigReceiveCommunicator _foreignConfigReceiveCommunicator;

	private HashMap<Integer, ConfigurationAuthority> _code2foreignConfigurationAuthority;

	private final File _foreignObjectCacheFile;

	private PersistentDynamicObjectCache _foreignObjectCache;

	private Object _foreignObjectCacheLock;

	private boolean _foreignObjectCacheConsistent;

	public ForeignObjectManager(
			final ClientDavInterface connection, final DataModel configuration, final SystemObject localAuthority, final File foreignObjectCacheFile) {
		_connection = connection;
		_configuration = configuration;
		_localAuthority = localAuthority;
		Aspect responseAspect = configuration.getAspect("asp.antwort");
		AttributeGroup responseAtg = configuration.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleLesend");
		final DataDescription responseDescription = new DataDescription(responseAtg, responseAspect);
		_foreignConfigReceiveCommunicator = new ForeignConfigReceiveCommunicator(connection, _localAuthority, responseDescription);
		_code2foreignConfigurationAuthority = new HashMap<Integer, ConfigurationAuthority>();
		final SystemObjectType kvType = _configuration.getType("typ.konfigurationsVerantwortlicher");
		final List<SystemObject> kvList = kvType.getObjects();
		final AttributeGroup kvAtg = _configuration.getAttributeGroup("atg.konfigurationsVerantwortlicherEigenschaften");
		final ConfigurationAuthority localConfigurationAuthority = _configuration.getConfigurationAuthority();
		for(SystemObject kv : kvList) {
			int kvCode = kv.getConfigurationData(kvAtg).getScaledValue("kodierung").intValue();
			_code2foreignConfigurationAuthority.put(kvCode, (ConfigurationAuthority)kv);
		}
		_foreignObjectCacheLock = new Object();
		synchronized(_foreignObjectCacheLock) {
			_foreignObjectCacheFile = foreignObjectCacheFile;
			if(foreignObjectCacheFile != null) {
				try {
					_foreignObjectCache = new PersistentDynamicObjectCache(configuration, foreignObjectCacheFile);
				}
				catch(Exception e1) {
					_debug.warning("Datei mit gespeicherten Fremdobjekten konnte nicht gelesen werden, Datei " + foreignObjectCacheFile + " wird gelöscht");
					try {
						foreignObjectCacheFile.delete();
						_foreignObjectCache = new PersistentDynamicObjectCache(configuration, foreignObjectCacheFile);
					}
					catch(IOException e2) {
						String message = "Fehler beim Erzeugen einer neuen Datei zur Speicherung von Fremdobjekten: " + foreignObjectCacheFile;
						_debug.error(message, e2);
						throw new RuntimeException(message, e2);
					}
				}
			}
			_foreignObjectCacheConsistent = true;
		}
	}

	public ForeignConfigRequester getForeignConfigRequester(final ConfigurationAuthority authority) {
		String managementPid = authority.getPid();
		return getForeignConfigRequester(managementPid);
	}

	private ForeignConfigRequester getForeignConfigRequester(final String managementPid) {
		if(managementPid.equals(_localAuthority.getPid())) {
			return null;
		}

		synchronized(_authorityPid2Requester) {
			// Da als Value für einen Key auch null eingetragen sein kann, muss dies hier mit containsKey() geprüft werden.
			if(_authorityPid2Requester.containsKey(managementPid)) {
				return _authorityPid2Requester.get(managementPid);
			}
		}
		final SystemObject managementObject = _configuration.getObject(managementPid);
		synchronized(_authorityPid2Requester) {
			// Da als Value für einen Key auch null eingetragen sein kann, muss dies hier mit containsKey() geprüft werden.
			if(_authorityPid2Requester.containsKey(managementPid)) {
				return _authorityPid2Requester.get(managementPid);
			}
			ForeignConfigRequester foreignConfigRequester = null;
			if(managementObject instanceof ConfigurationAuthority) {
				ConfigurationAuthority foreignAuthority = (ConfigurationAuthority)managementObject;
				foreignConfigRequester = new ForeignConfigRequester(this, foreignAuthority, _foreignConfigReceiveCommunicator);
			}
			else {
				_debug.warning(
						"Ein dynamisches Objekt oder eine dynamische Menge soll von " + managementPid + " verwaltet werden, aber es gibt keinen Konfigurationsverantwortlichen "
						+ "mit dieser Pid", managementObject
				);
			}
			_authorityPid2Requester.put(managementPid, foreignConfigRequester);
			return foreignConfigRequester;
		}
	}

	public ForeignConfigRequester getForeignConfigRequester(final MutableCollection mutableCollection) {
		if(mutableCollection instanceof MutableSet) {
			MutableSet mutableSet = (MutableSet)mutableCollection;

			String managementPid = getElementsManagementPid(mutableSet);
			return getForeignConfigRequester(managementPid);
		}
		// Es ist ein Typ-Objekt, dass hier nicht berücksichtigt wird
		return null;
	}

	public String getElementsManagementPid(final MutableSet mutableSet) {
		String managementPid = "";
		if(mutableSet instanceof ConfigMutableSet) {
			ConfigMutableSet configMutableSet = (ConfigMutableSet)mutableSet;
			managementPid = configMutableSet.getElementsManagementPid();
		}

		if(managementPid.length() == 0) {
			final ConfigurationAuthority setAuthority = mutableSet.getConfigurationArea().getConfigurationAuthority();
			managementPid = setAuthority.getPid();
		}
		return managementPid;
	}

	public ForeignMutableCollectionProxy getForeignMutableCollectionProxy(
			final short internalSimVariant, final MutableCollection mutableCollection) {
		final ForeignConfigRequester foreignConfigRequester = getForeignConfigRequester(mutableCollection);
		if(foreignConfigRequester == null) return null;
		return foreignConfigRequester.getForeignMutableCollectionProxy(internalSimVariant, mutableCollection);
	}

	public ClientDavInterface getConnection() {
		return _connection;
	}

	public DataModel getConfiguration() {
		return _configuration;
	}

	public SystemObject getLocalAuthority() {
		return _localAuthority;
	}

	void start() {
		_executor = Executors.newSingleThreadScheduledExecutor();
		_foreignConfigReceiveCommunicator.start();
	}

	ScheduledExecutorService getExecutor() {
		return _executor;
	}

	void memorizeRemoteObject(final Long key, final ForeignDynamicObject remoteObject) {
		synchronized(_foreignObjects) {
			_foreignObjects.put(key, remoteObject);
		}
	}

	public SystemObject getRemoteObject(final Long key) {
		synchronized(_foreignObjects) {
			return _foreignObjects.get(key);
		}
	}

	public boolean hasRemoteObject(final Long key) {
		synchronized(_foreignObjects) {
			return _foreignObjects.containsKey(key);
		}
	}

	public void updateNotValidSince(final long objectId, final long objectTypeId, final long notValidSince) {
		final SystemObject remoteObject = getRemoteObject(objectId);
		if(remoteObject instanceof ForeignDynamicObject) {
			final ForeignDynamicObject foreignDynamicObject = ((ForeignDynamicObject)remoteObject);
			final SystemObjectType remoteObjectType = foreignDynamicObject.getType();
			if(remoteObjectType instanceof ConfigDynamicObjectType && remoteObjectType.getId() == objectTypeId) {
				foreignDynamicObject.setNotValidSince(notValidSince);
				final ConfigDynamicObjectType configDynamicObjectType = ((ConfigDynamicObjectType)remoteObjectType);
				configDynamicObjectType.informInvalidationListener(foreignDynamicObject);
			}
		}
	}

	public void updateName(final long objectId, final long objectTypeId, final String newName) {
		final SystemObject remoteObject = getRemoteObject(objectId);
		if(remoteObject instanceof ForeignDynamicObject) {
			final ForeignDynamicObject foreignDynamicObject = ((ForeignDynamicObject)remoteObject);
			final SystemObjectType remoteObjectType = foreignDynamicObject.getType();
			if(remoteObjectType instanceof ConfigDynamicObjectType && remoteObjectType.getId() == objectTypeId) {
				foreignDynamicObject.setName(newName);
				final ConfigDynamicObjectType configDynamicObjectType = ((ConfigDynamicObjectType)remoteObjectType);
				configDynamicObjectType.informNameChangedListener(foreignDynamicObject);
			}
		}
	}

//	public boolean requestRemoteObjects(RequestRemoteObjectCallback callback, final long[] ids, final SystemObject[] objects) {
//		final ArrayList<Long> toBeRequestedObjectIds = new ArrayList<Long>();
//		// Schon bekannte Objekte in die Antwort eintragen
//		synchronized(_foreignObjects) {
//			for(int i = 0; i < objects.length; i++) {
//				SystemObject object = objects[i];
//				if(object == null) {
//					final long id = ids[i];
//					object = _foreignObjects.get(id);
//					if(object == null) {
//						toBeRequestedObjectIds.add(id);
//					}
//					else {
//						objects[i] = object;
//					}
//				}
//			}
//		}
//		// Überprüfen, ob der jeweilige KV von noch nicht bekannten Objekten bekannt ist und angefragt werden kann.
//		for(Iterator<Long> iterator = toBeRequestedObjectIds.iterator(); iterator.hasNext();) {
//			Long toBeRequestedObjectId = iterator.next();
//			final ConfigurationAuthority foreignConfigurationAuthority = getForeignConfigurationAuthority(toBeRequestedObjectId);
//			final ForeignConfigRequester foreignConfigRequester = getForeignConfigRequester(foreignConfigurationAuthority);
//			System.out.println("requestRemoteObjects1: foreignConfigRequester = " + foreignConfigRequester);
////			if(foreignConfigRequester != null) {
////				foreignConfigRequester.isCommunicatorConnected();
////				final RemoteObjectListener listener = new RemoteObjectListener(toBeRequestedObjectId);
////				foreignConfigRequester.queryObject(toBeRequestedObjectId, listener);
////
////			}
//			// Hier wird geprüft, ob eine asynchrone Anfrage momentan sinnvoll ist. Wenn nicht,
//			if(foreignConfigurationAuthority == null || foreignConfigRequester == null || foreignConfigRequester.isStartedAndUnconnectedAndTimedOut()) {
//				iterator.remove();
//			}
//		}
//		if(toBeRequestedObjectIds.size() == 0) {
//			return true;
//		}
//		else {
//			












	public void requestRemoteObjects(AsyncIdsToObjectsRequest asyncIdsToObjectsRequest) {
		final ArrayList<Long> toBeRequestedObjectIds = new ArrayList<Long>();
		// Schon bekannte Objekte in die Antwort eintragen
		SystemObject[] objects = asyncIdsToObjectsRequest.getObjects();
		long[] ids = asyncIdsToObjectsRequest.getIds();
		synchronized(_foreignObjects) {
			for(int i = 0; i < objects.length; i++) {
				SystemObject object = objects[i];
				if(object == null) {
					final long id = ids[i];
					object = _foreignObjects.get(id);
					if(object == null) {
						toBeRequestedObjectIds.add(id);
					}
					else {
						objects[i] = object;
					}
				}
			}
		}
		
		// Überprüfen, ob der jeweilige KV von noch nicht bekannten Objekten bekannt ist und angefragt werden kann.
		for(Iterator<Long> iterator = toBeRequestedObjectIds.iterator(); iterator.hasNext();) {
			Long toBeRequestedObjectId = iterator.next();
			final ConfigurationAuthority foreignConfigurationAuthority = getForeignConfigurationAuthority(toBeRequestedObjectId);
			final ForeignConfigRequester foreignConfigRequester;
			if(foreignConfigurationAuthority != null && foreignConfigurationAuthority.isOfType("typ.autarkeOrganisationsEinheit")) {
				foreignConfigRequester = getForeignConfigRequester(foreignConfigurationAuthority);
			}
			else {
				foreignConfigRequester = null;
			}
			// Hier wird geprüft, ob eine asynchrone Anfrage momentan sinnvoll ist.
			if(foreignConfigurationAuthority == null || foreignConfigRequester == null || foreignConfigRequester.isStartedAndUnconnectedAndTimedOut()) {
				iterator.remove();
			}
		}
		asyncIdsToObjectsRequest.setAsyncObjectRequestIds(toBeRequestedObjectIds);
		for(Long toBeRequestedObjectId : toBeRequestedObjectIds) {
			final ConfigurationAuthority foreignConfigurationAuthority = getForeignConfigurationAuthority(toBeRequestedObjectId);
			final ForeignConfigRequester foreignConfigRequester = getForeignConfigRequester(foreignConfigurationAuthority);
			if(foreignConfigRequester != null) {
				RemoteObjectRequest remoteObjectRequest = new RemoteObjectRequest(foreignConfigRequester, asyncIdsToObjectsRequest, toBeRequestedObjectId);
				foreignConfigRequester.activateObjectRequest(remoteObjectRequest);
			}
		}
	}

	private ConfigurationAuthority getForeignConfigurationAuthority(long objectId) {
		final int kvCode = (int)(objectId >>> 48);
		return _code2foreignConfigurationAuthority.get(kvCode);
	}

	public void close() throws IOException {
		save();
	}

	public void save() throws IOException {
		synchronized(_foreignObjectCacheLock) {
			if(_foreignObjectCacheFile != null && _foreignObjectCache != null && !_foreignObjectCacheConsistent ) {
				_foreignObjectCache.writeToDisk(_foreignObjectCacheFile);
				_foreignObjectCacheConsistent = true;
			}
		}
	}

	public SystemObject getCachedForeignObject(final long id) {
		synchronized(_foreignObjectCacheLock) {
			if(_foreignObjectCache != null) return _foreignObjectCache.getObject(id);
		}
		return null;
	}

	public void cacheForeignObject(final DynamicObject remoteDynamicObject) {
		synchronized(_foreignObjectCacheLock) {
			if(_foreignObjectCache != null) {
				_foreignObjectCache.storeObject(remoteDynamicObject);
				_foreignObjectCacheConsistent = false;
			}
		}

	}

	private class RemoteObjectRequest implements ForeignConfigRequester.RemoteObjectRequest, ForeignObjectTransferListener{

		private final ForeignConfigRequester _foreignConfigRequester;

		private final AsyncIdsToObjectsRequest _asyncIdsToObjectsRequest;

		private final Long _toBeRequestedObjectId;

		private boolean _queryHasBeenSend = false;

		public RemoteObjectRequest(
				final ForeignConfigRequester foreignConfigRequester, final AsyncIdsToObjectsRequest asyncIdsToObjectsRequest, final Long toBeRequestedObjectId) {
			_foreignConfigRequester = foreignConfigRequester;
			_asyncIdsToObjectsRequest = asyncIdsToObjectsRequest;
			_toBeRequestedObjectId = toBeRequestedObjectId;
		}

		public void processConnectedEvent() {
//			System.out.println("processConnectedEvent: foreignConfigRequester = " + _foreignConfigRequester);
//			System.out.println("_toBeRequestedObjectId = " + _toBeRequestedObjectId);
			_queryHasBeenSend = true;
			_foreignConfigRequester.queryObject(_toBeRequestedObjectId, this);
//			System.out.println("processConnectedEvent fertig");
		}

		public void processConnectionTimeout() {
//			System.out.println("processConnectionTimeout: foreignConfigRequester = " + _foreignConfigRequester);
//			System.out.println("_toBeRequestedObjectId = " + _toBeRequestedObjectId);
			if(_queryHasBeenSend) {
				_foreignConfigRequester.notifyObjectDataTimeout(_toBeRequestedObjectId);
			}
			else {
				objectComplete();
			}
//			System.out.println("processConnectionTimeout: fertig");
		}

		/**
		 * Wird aufgerufen, wenn das Objekt geladen wurde, oder das Laden fehlschlug
		 */
		public void objectComplete() {
			_foreignConfigRequester.deactivateObjectRequest(this);
//			System.out.println("objectComplete: foreignConfigRequester = " + _foreignConfigRequester);
//			System.out.println("_toBeRequestedObjectId = " + _toBeRequestedObjectId);
			_asyncIdsToObjectsRequest.objectComplete(_toBeRequestedObjectId);
		}
	}

}
