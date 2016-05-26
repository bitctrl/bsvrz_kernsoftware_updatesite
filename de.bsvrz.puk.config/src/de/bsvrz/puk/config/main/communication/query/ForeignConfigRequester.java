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

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.impl.config.request.KindOfUpdateTelegramm;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ForeignConfigRequester {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private int _requestIndex = 0;

	private ForeignObjectManager _foreignObjectManager;

	private final Map<ProxyKey, ForeignMutableCollectionProxy> _mutableCollectionProxies = new HashMap<ProxyKey, ForeignMutableCollectionProxy>();

	private ForeignConfigCommunicator _foreignConfigCommunicator;

	private AttributeGroup _requestAtg;

	private boolean _communicatorStarted = false;

	private boolean _communicatorConnected = false;

	private boolean _communicationTimedOut = false;


	private Set<ForeignMutableCollectionProxy> _subscribedProxies = new HashSet<ForeignMutableCollectionProxy>();

	private SystemObject _localAuthority;

	private HashMap<Long, List<ForeignObjectTransferListener>> _waitingObjectQueries = new HashMap<Long, List<ForeignObjectTransferListener>>();

	private HashMap<Integer, DataQueryInfo> _waitingObjectDataQueries = new HashMap<Integer, DataQueryInfo>();

	private CopyOnWriteArrayList<ForeignCommunicationStateListener> _communicationStateListeners = new CopyOnWriteArrayList<ForeignCommunicationStateListener>();

	private ArrayList<RemoteObjectRequest> _activeObjectRequests = new ArrayList<RemoteObjectRequest>();

	public ForeignConfigRequester(
			final ForeignObjectManager foreignObjectManager,
			final ConfigurationAuthority foreignAuthority,
			final ForeignConfigReceiveCommunicator foreignConfigReceiveCommunicator) {
		_foreignObjectManager = foreignObjectManager;
		final DataModel configuration = _foreignObjectManager.getConfiguration();
		Aspect requestAspect = configuration.getAspect("asp.anfrage");
		_requestAtg = configuration.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelleLesend");

		final DataDescription requestDescription = new DataDescription(_requestAtg, requestAspect);
		_localAuthority = _foreignObjectManager.getLocalAuthority();
		_foreignConfigCommunicator = new ForeignConfigCommunicator(
				new CommunicationHandler(), _foreignObjectManager.getConnection(), foreignAuthority, requestDescription, foreignConfigReceiveCommunicator
		);
	}

	@Override
	public String toString() {
		return "ForeignConfigRequester{" + "_foreignConfigCommunicator=" + _foreignConfigCommunicator + ", _communicatorStarted=" + _communicatorStarted
		       + ", _communicatorConnected=" + _communicatorConnected + ", _communicationTimedOut=" + _communicationTimedOut + ", _localAuthority="
		       + _localAuthority + '}';
	}

	public ForeignMutableCollectionProxy getForeignMutableCollectionProxy(final short internalSimVariant, final MutableCollection mutableCollection) {
		ProxyKey key = new ProxyKey(internalSimVariant, mutableCollection);
		synchronized(_mutableCollectionProxies) {
			ForeignMutableCollectionProxy proxy = _mutableCollectionProxies.get(key);
			if(proxy == null) {
				_debug.fine("Erzeuge neuen ForeignMutableCollectionProxy");
			}
			else {
				_debug.fine("ForeignMutableCollectionProxy bereits vorhanden");
			}
			if(proxy == null) {
				proxy = new ForeignMutableCollectionProxy(this, internalSimVariant, mutableCollection);
				_mutableCollectionProxies.put(key, proxy);
			}
			return proxy;
		}
	}

	public ScheduledExecutorService getExecutor() {
		return _foreignObjectManager.getExecutor();
	}

	public void subscribe(final ForeignMutableCollectionProxy proxy) {
		subscribeProxyOrActivateObjectRequest(proxy, null);
	}

	public void activateObjectRequest(final RemoteObjectRequest remoteObjectRequest) {
		subscribeProxyOrActivateObjectRequest(null, remoteObjectRequest);
	}

	public boolean isStartedAndUnconnectedAndTimedOut() {
		synchronized(this) {
			return _communicatorStarted && !_communicatorConnected && _communicationTimedOut;
		}
	}

	public void subscribeProxyOrActivateObjectRequest(final ForeignMutableCollectionProxy proxy, final RemoteObjectRequest remoteObjectRequest) {
		synchronized(this) {
			_debug.fine("ForeignConfigRequester.subscribe, _communicatorConnected", _communicatorConnected);
			_debug.fine("ForeignConfigRequester.subscribe, _communicatorStarted", _communicatorStarted);

//			System.out.println("_communicatorStarted = " + _communicatorStarted);
//			System.out.println("_communicatorConnected = " + _communicatorConnected);
//			System.out.println("_communicationTimedOut = " + _communicationTimedOut);

			if(proxy != null) _subscribedProxies.add(proxy);
			if(remoteObjectRequest !=  null) _activeObjectRequests.add(remoteObjectRequest);
			if(_communicatorConnected) {
				getExecutor().execute(
						new Runnable() {
							public void run() {
								synchronized(ForeignConfigRequester.this) {
									if(proxy != null) proxy.processConnectedEvent();
									if(remoteObjectRequest != null) remoteObjectRequest.processConnectedEvent();
								}
							}
						}
				);
			}
			else if(_communicatorStarted) {
				getExecutor().execute(
						new Runnable() {
							public void run() {
								synchronized(ForeignConfigRequester.this) {
									if(!_communicatorConnected && _communicationTimedOut) {
										if(proxy != null) proxy.processConnectionTimeout();
										if(remoteObjectRequest != null) remoteObjectRequest.processConnectionTimeout();
									}
								}
							}
						}
				);
			}
			else {
				_communicatorStarted = true;
				getExecutor().schedule(
						new Runnable() {
							public void run() {
								synchronized(ForeignConfigRequester.this) {
									_communicationTimedOut = true;
									if(!_communicatorConnected) {
										for(final ForeignMutableCollectionProxy subscribedProxy : _subscribedProxies) {
											subscribedProxy.processConnectionTimeout();
										}
										for(final RemoteObjectRequest activeObjectRequest : _activeObjectRequests) {
											activeObjectRequest.processConnectionTimeout();
										}
									}
								}
							}
						}, 30, TimeUnit.SECONDS
				);
//				getExecutor().execute(
//						new Runnable() {
//							public void run() {
								_foreignConfigCommunicator.start();
//							}
//						}
//				);
			}
		}
	}

	public void unsubscribe(final ForeignMutableCollectionProxy proxy) {
		synchronized(this) {
			_subscribedProxies.remove(proxy);
		}
	}

	public void deactivateObjectRequest(final RemoteObjectRequest remoteObjectRequest) {
		synchronized(this) {
			_activeObjectRequests.remove(remoteObjectRequest);
		}
	}

	public ForeignObjectManager getForeignObjectManager() {
		return _foreignObjectManager;
	}

	public void queryObject(final Long id, final ForeignObjectTransferListener transferListener) {
		getExecutor().execute( new Runnable() { public void run() {
//			System.out.println("queryObject async exec started: id = " + id);
			try {
				boolean sendQuery = false;
				synchronized(_waitingObjectQueries) {
					List<ForeignObjectTransferListener> transferListeners = _waitingObjectQueries.get(id);
					if(transferListeners == null) {
						sendQuery = true;
						transferListeners = new LinkedList<ForeignObjectTransferListener>();
						_waitingObjectQueries.put(id, transferListeners);
//						System.out.println("queryObject transferlisteners angelegt: id = " + id + " transferlisteners: " + transferListeners);
					}
					else {
//						System.out.println("queryObject transferlisteners gefunden: id = " + id + " transferlisteners: " + transferListeners);
					}
					transferListeners.add(transferListener);
				}
				if(sendQuery) sendObjectQuery(id);
			}
			catch(RuntimeException e) {
				_debug.warning("Unerwarteter Fehler bei der asynchronen Objektanfrage bei einer fremden Konfiguration");
			}
			finally {
//				System.out.println("queryObject async exec done: id = " + id);
			}
		} } );
	}

	public void queryObjectData(final ForeignDynamicObject object) {
		getExecutor().execute(
				new Runnable() {
					public void run() {
						sendObjectDataQuery(object);
					}
				}
		);
	}

	public void addCommunicationStateListener(final ForeignCommunicationStateListener listener) {
		_communicationStateListeners.add(listener);
	}

	public void removeCommunicationStateListener(final ForeignCommunicationStateListener listener) {
		_communicationStateListeners.remove(listener);
	}

	static final class ProxyKey {

		private final short _internalSimVariant;

		private final MutableCollection _mutableCollection;

		public ProxyKey(final short internalSimVariant, final MutableCollection mutableCollection) {
			_internalSimVariant = internalSimVariant;
			_mutableCollection = mutableCollection;
		}

		public int hashCode() {
			return _mutableCollection.hashCode() + ((int)_internalSimVariant) << 16;
		}

		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj instanceof ProxyKey) {
				ProxyKey otherProxyKey = (ProxyKey)obj;
				return _internalSimVariant == otherProxyKey._internalSimVariant && _mutableCollection.equals(otherProxyKey._mutableCollection);
			}
			return false;
		}
	}

	public int sendRequest(String messageType, byte[] data) throws SendSubscriptionNotConfirmed, IllegalStateException {
		int requestIndex = getNewRequestIndex();
		sendRequest(messageType, data, requestIndex);
		return requestIndex;
	}

	public int sendRequest(String messageType, byte[] data, int requestIndex) throws SendSubscriptionNotConfirmed, IllegalStateException {
		Data requestData = AttributeBaseValueDataFactory.createAdapter(_requestAtg, AttributeHelper.getAttributesValues(_requestAtg));

		// Der Absender des Telegramms ist das Objekt, dass sich als Empfänger auf die Daten angemeldet hat
		requestData.getReferenceValue("absender").setSystemObject(_localAuthority);

		requestData.getScaledValue("anfrageIndex").set(requestIndex);
		requestData.getScaledValue("nachrichtenTyp").setText(messageType);
		requestData.getUnscaledArray("daten").set(data);
		_foreignConfigCommunicator.sendData(requestData);
		return requestIndex;
	}

	public int getNewRequestIndex() {
		int requestIndex;
		synchronized(this) {
			if(_requestIndex == 0) _requestIndex = 1;
			requestIndex = _requestIndex++;
		}
		return requestIndex;
	}

	class CommunicationHandler implements ForeignConfigCommunicator.CommunicationHandler {

		public void communicationStateChanged(final boolean connected) {
//			System.out.println("+++++++++++++++++++++++++ communicationStateChanged: connected = " + connected);
			synchronized(ForeignConfigRequester.this) {
				_communicatorConnected = connected;
				if(!connected) _communicationTimedOut = true;
				if(connected) {
					for(final ForeignMutableCollectionProxy subscribedProxy : _subscribedProxies) {
						getExecutor().execute(
								new Runnable() {
									public void run() {
										subscribedProxy.processConnectedEvent();
//										sendSubscription(subscribedProxy);
									}
								}
						);
					}
				}
				for(final RemoteObjectRequest activeObjectRequest : _activeObjectRequests) {
					getExecutor().execute(
							new Runnable() {
								public void run() {
									if(connected) {
										activeObjectRequest.processConnectedEvent();
									}
									else {
										activeObjectRequest.processConnectionTimeout();
									}
								}
							}
					);
				}
				for(final ForeignCommunicationStateListener communicationStateListener : _communicationStateListeners) {
					getExecutor().execute(
							new Runnable() {
								public void run() {
									communicationStateListener.communicationStateChanged(connected);
								}
							}
					);
				}
			}
		}

		public void answerReceived(Data data) {
			final String messageType = data.getTextValue("nachrichtenTyp").getValueText();
			final byte[] message = data.getScaledArray("daten").getByteArray();
			final DataModel configuration = _foreignObjectManager.getConfiguration();
			final Deserializer deserializer;
			try {
				deserializer = SerializingFactory.createDeserializer(2, new ByteArrayInputStream(message));
			}
			catch(NoSuchVersionException e) {
				_debug.error("Deserializer konnte nicht erzeugt werden", e);
				return;
			}
			try {
				if("DynamischeKollektionElemente".equals(messageType)) {
					final SystemObject receivedSystemObject = deserializer.readObjectReference(configuration);
					final short receivedSimVariant = deserializer.readShort();
					final int elementCount = deserializer.readInt();
					long[] elementIds = new long[elementCount];
					for(int i = 0; i < elementIds.length; i++) {
						elementIds[i] = deserializer.readLong();
					}
					final ForeignMutableCollectionProxy collectionProxy = getForeignMutableCollectionProxy(
							receivedSimVariant, (MutableCollection)receivedSystemObject
					);
					collectionProxy.processReceivedElementIds(elementIds);
				}
				else if("DynamischeKollektionAktualisierung".equals(messageType)) {
					final SystemObject receivedSystemObject = deserializer.readObjectReference(configuration);
					final MutableCollection mutableCollection = ((MutableCollection)receivedSystemObject);
					short receivedSimVariant = deserializer.readShort();
					int numberOfAddedElements = deserializer.readInt();

					long[] addedElementIds = new long[numberOfAddedElements];
					for(int i = 0; i < numberOfAddedElements; i++) {
						addedElementIds[i] = deserializer.readLong();
					}
					int numberOfRemovedElements = deserializer.readInt();
					long[] removedElementIds = new long[numberOfRemovedElements];
					for(int i = 0; i < numberOfRemovedElements; i++) {
						removedElementIds[i] = deserializer.readLong();
					}
					final ForeignMutableCollectionProxy collectionProxy = getForeignMutableCollectionProxy(
							receivedSimVariant, mutableCollection
					);
					collectionProxy.processElementsChanged(addedElementIds, removedElementIds);
				}
				else if("ObjektAntwort".equals(messageType)) {
					long id = 0;
					ForeignDynamicObject foreignDynamicObject = null;
					switch(deserializer.readByte()) {
						case 0:
							try {
								id = deserializer.readLong();
								String requestedPid = deserializer.readString();
							}
							catch(IOException e) {
								// Alte Konfiguration antwortet ohne id und pid, wenn Objekt nicht gefunden wurde, neue Konfiguration antwortet mit id und pid der Anfrage
							}
							break;
						case 1:
							// dynamisches Objekt
							id = deserializer.readLong();
							long typeId = deserializer.readLong();
							final byte flag = deserializer.readByte();
							final boolean valid = (flag & 1) != 0;
							String pid = null;
							if((flag & 2) != 0) pid = deserializer.readString();
							String name = null;
							if((flag & 4) != 0) name = deserializer.readString();
							final long validSince = deserializer.readLong();
							final long notValidSince = deserializer.readLong();
							final long configAreaId = deserializer.readLong();
							foreignDynamicObject = new ForeignDynamicObject(
									ForeignConfigRequester.this, configuration, id, typeId, pid, name, valid, validSince, notValidSince, configAreaId
							);
							break;
						case 2:
							// Konfigurationsobjekte werden hier nicht unterstützt
							id = deserializer.readLong();
							typeId = deserializer.readLong();
							break;
						default:
							_debug.error("Fehlerhafte ObjektAntwort empfangen", data);
					}
					if(id != 0) {
						if(foreignDynamicObject == null) {
							notifyObjectDataComplete(id, foreignDynamicObject);
						}
						else {
							queryObjectData(foreignDynamicObject);
						}
					}
				}
				else if("DatensatzAntwort".equals(messageType)) {
					try {
						final int requestIndex = data.getScaledValue("anfrageIndex").intValue();
						final ForeignConfigRequester.DataQueryInfo dataQueryInfo = _waitingObjectDataQueries.remove(requestIndex);
						int numberOfDatasets = deserializer.readInt();
						if(numberOfDatasets != 1) {
							throw new IOException("Empfangene Datensatz-Anzahl nicht wie erwartet: " + numberOfDatasets);
						}
						int numberOfBytes = deserializer.readInt();
						byte[] dataBytes = numberOfBytes == 0 ? null : deserializer.readBytes(numberOfBytes);

						Data configData = null;
						if(dataBytes != null) {
							try {
								final ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
								final Deserializer configDataDeserializer = SerializingFactory.createDeserializer(2, in);
								configData = configDataDeserializer.readData(dataQueryInfo._queryUsage.getAttributeGroup()).createUnmodifiableCopy();
							}
							catch(Exception ex) {
								final String errorMessage = "Der konfigurierende Datensatz für das fremde Objekt " + dataQueryInfo._object
								                            + " und der Attributgruppenverwendung " + dataQueryInfo._queryUsage
								                            + " konnte nicht deserialisiert werden";
								_debug.warning(errorMessage, ex);
							}
						}
						if(dataQueryInfo._object.saveConfigurationData(dataQueryInfo._queryUsage, configData)) {
							notifyObjectDataComplete(dataQueryInfo._object.getId(), dataQueryInfo._object);
						}
					}
					catch(IOException e) {
						_debug.error("Fehler bei der Verarbeitung von einer ObjektAntwort", e);
					}
//					if(id != 0) {
//						final Long key = new Long(id);
//						synchronized(_waitingObjectDataQueries) {
//							_waitingObjectDataQueries.put(key, foreignDynamicObject);
//							queryObjectData(foreignDynamicObject);
//						}
//						_foreignObjectManager.memorizeRemoteObject(key, foreignDynamicObject);
//						final List<ForeignObjectTransferListener> transferListeners;
//						synchronized(_waitingObjectQueries) {
//							transferListeners = _waitingObjectQueries.remove(key);
//						}
//						for(ForeignObjectTransferListener transferListener : transferListeners) {
//							try {
//								transferListener.objectComplete();
//							}
//							catch(Exception e) {
//								_debug.error("Fehler bei der Verarbeitung von einer ObjektAntwort", e);
//							}
//						}
//					}

				}
				else if("Objektaktualisierung".equals(messageType)) {
					//System.out.println("Objektaktualisierung data = " + data);
					final KindOfUpdateTelegramm telegramType = KindOfUpdateTelegramm.getInstance(deserializer.readByte());

					if(telegramType == KindOfUpdateTelegramm.UPDATE_NAME) {

						// Ein Telegramm, das den Namen eines Objekts aktualisiert, hat folgenden Aufbau:
						// 1) Id des Objekt, dessen Name geändert werden soll (long)
						// 2) Id des Typs von dem das Objekt ist (long)
						// 3) Der neue Name (String)
						final long objectId = deserializer.readLong();
						final long objectTypeId = deserializer.readLong();
						final String newName = deserializer.readString();

						_foreignObjectManager.updateName(objectId, objectTypeId, newName);
					}
					else if(telegramType == KindOfUpdateTelegramm.UPDATE_NOT_VALID_SINCE) {

						// Ein Telegramm, das den Zeitpunkt/Version eines Objekts setzt ab dem es nicht mehr gültig ist, besitzt folgenden Aufbau.
						// 1) Id des Objekt, dessen Version/Zeitpunkt geändert werden soll (long)
						// 2) Id des Typs von dem das Objekt ist (long)
						// 3) Konfiguration oder dynamisches Objekt (byte, 0 = Konfigurationsobjekt)
						// Der nächste Wert ist abhängig von 3), ist es ein Konfigurationsobjekt, so muss ein short gelesen werden
						// 4a) Version, ab der das Objekt ungültig werden wird, short
						// 4b) Zeitpunkt, ab dem das Objekt ungültig geworden ist, long

						final long objectId = deserializer.readLong();
						final long objectTypeId = deserializer.readLong();
						final byte kindOfObject = deserializer.readByte();

						if(kindOfObject == 1) {
							// Es handelt sich um ein dynamisches Objekt
							final long notValidSince = deserializer.readLong();
							_foreignObjectManager.updateNotValidSince(objectId, objectTypeId, notValidSince);
						}
						else {
							// Änderungen an Konfigurationsobjekten werden ignoriert
						}
					}
					else if(telegramType == KindOfUpdateTelegramm.CREATED) {
						// Neue Objekte einer anderen Konfiguration werden ignoriert, bis sie in irgendeiner Menge enthalten sind
						//	// Ein neues dynamisches Objekt wurde erzeugt. Das Telegramm besitzt folgenden Aufbau
						//	// 1) Id des Objekts, das erzeugt wurde (long)
						//	// 2) Id des Typs des Objekts (long)
						//
						//	final long objectId = deserializer.readLong();
						//	final long objectTypeId = deserializer.readLong();
						//
						//	_updateDynamicObjects.newDynamicObjectCreated(objectId, objectTypeId);
					}
				}
				else {
					_debug.warning("unerwartete Antwort vom messageType: " + messageType, data);
				}
			}
			catch(IOException ex) {
				_debug.error("Fehlerhafte Antwort vom Typ " + messageType + " erhalten", ex);
			}
		}
	}

	void sendSubscription(final ForeignMutableCollectionProxy proxy) {
		try {
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
			Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
			// Datensatz zusammensetzen
			serializer.writeObjectReference((SystemObject)proxy.getMutableCollection());
			serializer.writeShort(proxy.getInternalSimVariant());
			sendRequest("DynamischeKollektionAnmeldung", byteArrayStream.toByteArray());
		}
		catch(Exception e) {
			_debug.warning("ForeignConfigRequester: Fehler beim Versand einer Anmeldung auf die Elemente einer MutableCollection", e);
		}
	}

	private void sendObjectQuery(final long id) {
		try {
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(10);
			Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
			// Datensatz zusammensetzen
			serializer.writeLong(id);
			// Daten verschicken
			sendRequest("ObjektAnfrageMitId", byteArrayStream.toByteArray());
		}
		catch(Exception e) {
			_debug.warning("ForeignConfigRequester: Fehler beim Versand einer Objektanfrage mit ID für Elemente einer MutableCollection", e);
		}
	}

	private class DataQueryInfo {

		private final AttributeGroupUsage _queryUsage;

		private final ForeignDynamicObject _object;

		public DataQueryInfo(final AttributeGroupUsage queryUsage, final ForeignDynamicObject object) {
			_queryUsage = queryUsage;
			_object = object;
		}
	}

	private void sendObjectDataQuery(final ForeignDynamicObject object) {
		final SystemObjectType type = object.getType();
		final ArrayList<AttributeGroupUsage> queryUsages = new ArrayList<AttributeGroupUsage>();
		if(!type.isConfigurating()) {
			final List<AttributeGroup> atgs = type.getAttributeGroups();
			for(AttributeGroup atg : atgs) {
				final Collection<AttributeGroupUsage> attributeGroupUsages = atg.getAttributeGroupUsages();
				for(AttributeGroupUsage attributeGroupUsage : attributeGroupUsages) {
					if(attributeGroupUsage.isConfigurating()) {
						queryUsages.add(attributeGroupUsage);
					}
				}
			}
		}
		final int dataCount = queryUsages.size();
		object.setWaitingDataCount(dataCount);
		if(dataCount == 0) {
			notifyObjectDataComplete(object.getId(), object);
		}
		else {
			try {
				for(AttributeGroupUsage queryUsage : queryUsages) {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(8);
					Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
					serializer.writeLong(queryUsage.getId());
					serializer.writeInt(1);
					serializer.writeLong(object.getId());
					final int requestIndex = getNewRequestIndex();
					synchronized(_waitingObjectDataQueries) {
						final DataQueryInfo dataQueryInfo = new DataQueryInfo(queryUsage, object);
						_waitingObjectDataQueries.put(requestIndex, dataQueryInfo);
					}
					sendRequest("DatensatzAnfrage", byteArrayStream.toByteArray(), requestIndex);
				}
			}
			catch(Exception e) {
				_debug.warning("ForeignConfigRequester: Fehler beim Versand einer Datensatzanfrage für Elemente einer MutableCollection", e);
			}
		}
	}

	private void notifyObjectDataComplete(final long id, final ForeignDynamicObject foreignDynamicObject) {
		final Long key = new Long(id);
		_foreignObjectManager.memorizeRemoteObject(key, foreignDynamicObject);
		notifyObjectQueryDone(key);
	}

	public void notifyObjectDataTimeout(final long id) {
//		System.out.println("ForeignConfigRequester.notifyObjectDataTimeout");
		final Long key = new Long(id);
		notifyObjectQueryDone(key);
	}

	private void notifyObjectQueryDone(final Long key) {
		final List<ForeignObjectTransferListener> transferListeners;
		synchronized(_waitingObjectQueries) {
			transferListeners = _waitingObjectQueries.remove(key);
//			System.out.println("notifyObjectQueryDone transferlisteners gelöscht: id = " + key + " transferlisteners: " + transferListeners);
		}
		for(ForeignObjectTransferListener transferListener : transferListeners) {
			try {
				transferListener.objectComplete();
			}
			catch(Exception e) {
				_debug.error("Fehler bei der Benachrichtigung eines neuen Remote-Objekts", e);
			}
		}
	}

	public interface ForeignCommunicationStateListener {

		void communicationStateChanged(boolean communicationState);
	}


	public boolean isCommunicatorConnected() {
		return _communicatorConnected;
	}

	public interface RemoteObjectRequest {

		void processConnectedEvent();
		void processConnectionTimeout();
	}
}
