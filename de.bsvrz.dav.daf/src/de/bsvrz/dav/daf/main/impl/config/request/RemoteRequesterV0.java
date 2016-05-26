/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.ConfigurationManager;
import de.bsvrz.dav.daf.main.impl.config.*;
import de.bsvrz.dav.daf.main.impl.config.telegrams.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Implementierung von Version 1 des Protokolls zur Kommunikation mit dem Datenverteiler. Die aktuelle Implementierugn befindet sich in
 * RemoteRequester
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class RemoteRequesterV0 extends RemoteRequester {


	private final BaseSubscriptionInfo _readBaseSubscriptionInfo;

	/** Liste mit den noch nicht bearbeiteten Antworten auf Konfigurationsanfragen */
	private LinkedList<ConfigTelegram> _pendingResponses;

	/** Attribugruppenverwendung Konfigurationsleseanfragen */
	private AttributeGroupUsage _configurationReadRequestUsage;

	/** Attribugruppenverwendung Konfigurationsleseantworten */
	private AttributeGroupUsage _configurationReadReplyUsage;

	/** Attribugruppenverwendung Konfigurationsschreibanfragen */
	private AttributeGroupUsage _configurationWriteRequestUsage;

	/** Attribugruppenverwendung Konfigurationsschreibantworten */
	private AttributeGroupUsage _configurationWriteReplyUsage;

	private boolean _connectionClosed = false;
	private ConfigurationManager _configurationManager;


	public RemoteRequesterV0(final ClientDavInterface connection, final DafDataModel localConfiguration, final ConfigurationAuthority configurationAuthority) {
		super(connection, localConfiguration, configurationAuthority);
		_pendingResponses = localConfiguration.getPendingResponses();
		_readBaseSubscriptionInfo = new BaseSubscriptionInfo(
				localConfiguration.getConfigurationAuthorityId(), AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
		);
		_configurationReadRequestUsage = (AttributeGroupUsage) localConfiguration.getObject("atgv.atg.konfigurationsAnfrage.asp.anfrage");
		_configurationReadReplyUsage = (AttributeGroupUsage) localConfiguration.getObject("atgv.atg.konfigurationsAntwort.asp.antwort");
		_configurationWriteRequestUsage = (AttributeGroupUsage) localConfiguration.getObject("atgv.atg.konfigurationsSchreibAnfrage.asp.anfrage");
		_configurationWriteReplyUsage = (AttributeGroupUsage) localConfiguration.getObject("atgv.atg.konfigurationsSchreibAntwort.asp.antwort");
		_configurationManager = localConfiguration.getConfigurationManager();
	}

	@Override
	public void close() {
		super.close();
		synchronized(_pendingResponses) {
			_connectionClosed = true;
			_pendingResponses.notifyAll();
		}
	}

	/**
	 * Lädt ein Systemobjekt durch entsprechende Konfigurationsanfragen aus der Konfiguration.
	 *
	 * @param id ID des gewünschen Objekts.
	 *
	 * @return Gewünschtes Systemobjekt
	 */
	public List<SystemObject> getObjects(long... ids) {
		DafSystemObject systemObject = null;
		SystemObjectRequestInfo systemObjectRequestInfo = new IdsToObjectsRequest(ids);
		ConfigTelegram telegram = new SystemObjectsRequest((long) 0, systemObjectRequestInfo);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}
					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							IdsToObjectsAnswer idsToObjectsAnswer = null;
							try {
								idsToObjectsAnswer = (IdsToObjectsAnswer)((SystemObjectAnswer)response).getSystemObjectAnswerInfo();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							if(idsToObjectsAnswer != null) {
								DafSystemObject objects[] = idsToObjectsAnswer.getObjects();
								if(objects != null) {
									return Arrays.<SystemObject>asList(objects);
								}
							}
							return null;
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public List<SystemObject> getObjects(final String... pids) {
		final DafSystemObject systemObject;
		SystemObjectRequestInfo systemObjectRequestInfo = new PidsToObjectsRequest(pids);
		ConfigTelegram telegram = new SystemObjectsRequest((long) 0, systemObjectRequestInfo);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							PidsToObjectsAnswer pidsToObjectsAnswer = null;
							try {
								pidsToObjectsAnswer = (PidsToObjectsAnswer)((SystemObjectAnswer)response).getSystemObjectAnswerInfo();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							if(pidsToObjectsAnswer != null) {
								DafSystemObject objects[] = pidsToObjectsAnswer.getObjects();
								if(objects != null) {
									return Arrays.<SystemObject>asList(objects);
								}
							}
							return null;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Liefert eine Liste mit allen Systemobjekten eines Typs zurück. Zu beachten ist, das auch Objekte eines Typs, der diesen Typ erweitert, zurückgegeben
	 * werden.
	 *
	 * @param type Typ der gewünschten Systemobjekte
	 *
	 * @return Liste von {@link DafSystemObject System-Objekten}
	 */
	@Override
	public final List<SystemObject> getObjectsOfType(SystemObjectType type) {
		long[] ids = {type.getId()};
		SystemObjectRequestInfo systemObjectRequestInfo = new TypeIdsToObjectsRequest(ids);
		ConfigTelegram telegram = new SystemObjectsRequest((long) 0, systemObjectRequestInfo);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							TypeIdsToObjectsAnswer typIdsToObjectsAnswer = null;
							try {
								typIdsToObjectsAnswer = (TypeIdsToObjectsAnswer)((SystemObjectAnswer)response).getSystemObjectAnswerInfo();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							ArrayList<SystemObject> list = new ArrayList<SystemObject>();
							if(typIdsToObjectsAnswer != null) {
								ObjectsList[] objectLists = typIdsToObjectsAnswer.getObjectsOfTypes();
								if(objectLists != null) {
									if(objectLists[0] != null) {
										if(objectLists[0].getBaseObjectId() == ids[0]) {
											DafSystemObject systemObjects[] = objectLists[0].getObjects();
											if(systemObjects != null) {
												for(int j = 0; j < systemObjects.length; ++j) {
													DafSystemObject systemObject = systemObjects[j];
													if(systemObject != null) {
														list.add(systemObject);
													}
												}
											}
										}
									}
								}
							}
							return list;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	@Override
	public final ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, Collection<? extends ObjectSet> sets)
			throws ConfigurationChangeException {
		if(!type.isConfigurating()) {
			throw new IllegalArgumentException("createConfigurationObject wurde mit einem Typ aufgerufen, der nicht konfigurierend ist: " + type);
		}
		long setIds[] = null;
		if(sets.size() > 0) {
			setIds = new long[sets.size()];
			int i = 0;
			for(ObjectSet set : sets) {
				setIds[i++] = set.getId();
			}
		}
		ConfigTelegram telegram = new NewObjectRequest((long) 0, -1, pid, name, type.getId(), setIds);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.NEW_OBJECT_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							DafSystemObject object = ((NewObjectAnswer)response).getObject();
							if(object == null) {
								throw new ConfigurationChangeException("Objekt konnte nicht in der Konfiguration angelegt werden.");
							}
							else {
								return (ConfigurationObject)object;
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	@Override
	public final DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name) throws ConfigurationChangeException {
		if(type.isConfigurating()) {
			throw new IllegalArgumentException("createDynamicObject wurde mit einem Typ aufgerufen, der nicht dynamisch ist: " + type);
		}
		ConfigTelegram telegram = new NewObjectRequest((long) 0, -1, pid, name, type.getId(), null);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.NEW_OBJECT_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							DafSystemObject object = ((NewObjectAnswer)response).getObject();
							if(object == null) {
								throw new ConfigurationChangeException("Objekt konnte nicht in der Konfiguration angelegt werden.");
							}
							else {
								return (DynamicObject)object;
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}


	/**
	 * Löscht das Objekt, indem es ungültig gemacht wird. Dynamische System-Objekte werden sofort ungültig. Bereits gültige konfigurierende System-Objekte werden
	 * mit Aktivierung der nächsten Konfigurationsversion ungültig. Für historische Anfragen bleiben ungültige Objekte nach wie vor existent. Konfigurierende
	 * System-Objekte, die noch nie gültig waren werden durch diese Methode gelöscht und sind nicht mehr zugreifbar.
	 *
	 * @param object Objekt, dass gelöscht bzw. ungültig gesetzt werden soll.
	 *
	 * @return <code>true</code>, falls das Objekt gelöscht bzw. ungültig gesetzt wurde; <code>false</code> wenn die Operation nicht durchgeführt werden konnte.
	 */
	@Override
	public final void invalidate(SystemObject object) throws ConfigurationChangeException {
		ObjectInvalidationWaiter objectInvalidationWaiter = new ObjectInvalidationWaiter(object);
		try {
			objectInvalidationWaiter.start();
			ObjectInvalidateRequest telegram = new ObjectInvalidateRequest((long) 0, object.getId());
			String info = Integer.toString(telegram.hashCode());
			telegram.setInfo(info);
			_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

			// Waiting for Answer
			ConfigTelegram response = null;
			long waitingTime = 0, startTime = System.currentTimeMillis();
			long sleepTime = 10;
			while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
				try {
					synchronized(_pendingResponses) {
						if(_connectionClosed) {
							throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
						}
						_pendingResponses.wait(sleepTime);
						if(sleepTime < 1000) {
							sleepTime *= 2;
						}

						ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
						while(_iterator.hasPrevious()) {
							response = _iterator.previous();
							if((response != null) && (response.getType() == ConfigTelegram.OBJECT_INVALIDATE_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
								_iterator.remove();
								try {
									ObjectInvalidateAnswer objectInvalidateAnswer = (ObjectInvalidateAnswer)response;
									if(telegram.getObjectId() == objectInvalidateAnswer.getObjectId()) {
										if(objectInvalidateAnswer.isInvalidated()) {
											if(object instanceof DafDynamicObject) {
												((DafDataModel)_localConfiguration).objectInvalidated((DafSystemObject) object, objectInvalidateAnswer.getConfigTime());
												objectInvalidationWaiter.await();
											}
											return;
										}
										else {
											throw new ConfigurationChangeException("Das Objekt konnte nicht gelöscht werden");
										}
									}
								}
								catch(ClassCastException ex) {
									throw new ConfigurationChangeException("Das Objekt konnte nicht gelöscht werden", ex);
								}
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
				catch(InterruptedException ex) {
					ex.printStackTrace();
					break;
				}
			}
			throw new RuntimeException("Die Konfiguration antwortet nicht");
		} finally {
			objectInvalidationWaiter.stop();
		}
	}

	static private class ObjectInvalidationWaiter {

		private final SystemObject _object;
		private final CountDownLatch _invalidationCountDown;
		private InvalidationListener _invalidationListener;

		public ObjectInvalidationWaiter(SystemObject object) {
			if(isDynamic(object)) {
				_object = object;
				_invalidationCountDown = new CountDownLatch(1);
				_invalidationListener = new InvalidationListener() {
					@Override
					public void invalidObject(final DynamicObject dynamicObject) {
						if(_object.equals(dynamicObject)) _invalidationCountDown.countDown();
					}
				};
			}
			else {
				_object = null;
				_invalidationCountDown = null;
				_invalidationListener = null;
			}
		}

		static boolean isDynamic(SystemObject object) {
			return object instanceof DynamicObject;
		}

		DynamicObjectType getType() {
			return (DynamicObjectType)_object.getType();
		}

		public void start() {
			if(isDynamic(_object)) getType().addInvalidationListener(_invalidationListener);
		}

		public void await() {
			try {
				if(isDynamic(_object)) _invalidationCountDown.await(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// InterruptedException führt zum sofortigen Ende der Methode
			}
		}

		public void stop() {
			if(isDynamic(_object)) getType().removeInvalidationListener(_invalidationListener);
		}
	}

	/**
	 * Macht ein bereits als ungültig markiertes Objekt wieder gültig. Wenn ein Konfigurationsobjekt mit der Methode {@link DafSystemObject#invalidate} für eine
	 * zukünftige Konfigurationsversion als ungültig markiert wurde und diese Konfigurationsversion noch nicht aktiviert wurde, dann kann das Objekt durch Aufruf
	 * dieser Methode wieder gültig gemacht werden.
	 *
	 * @param object Objekt, dass wieder gültig gemacht werden soll.
	 *
	 * @return <code>true</code>, falls das Objekt wieder gültig gemacht werden konnte; <code>false</code> wenn die Operation nicht durchgeführt werden konnte.
	 */
	@Override
	public final void revalidate(SystemObject object) throws ConfigurationChangeException {
		ObjectRevalidateRequest telegram = new ObjectRevalidateRequest((long) 0, object.getId());
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.OBJECT_REVALIDATE_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							try {
								ObjectRevalidateAnswer objectRevalidateAnswer = (ObjectRevalidateAnswer)response;
								if(telegram.getObjectId() == objectRevalidateAnswer.getObjectId()) {
									if(objectRevalidateAnswer.isRevalidated()) {
										return;
									}
									else {
										throw new ConfigurationChangeException("Das Objekt konnte nicth gültig gesetzt werden");
									}
								}
							}
							catch(ClassCastException ex) {
								throw new ConfigurationChangeException("Das Objekt konnte nicth gültig gesetzt werden", ex);
							}
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Setzt den Namen eines Systemobjekts.
	 *
	 * @param object Systemobjekt, das umbenannt werden soll.
	 * @param name   Neuer Name des Objekts. Der leere String ("") wird als "kein Name" interpretiert.
	 *
	 * @return <code>true</code>, falls der Name geändert werden konnte; <code>false</code> wenn die Operation nicht durchgeführt werden konnte.
	 *
	 * @see DafSystemObjectType#isNameOfObjectsPermanent
	 */
	@Override
	public final void setName(SystemObject object, String name) throws ConfigurationChangeException {
		ObjectSetNameRequest telegram = new ObjectSetNameRequest((long) 0, object.getId(), name);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram response = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponses) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponses.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator<ConfigTelegram> _iterator = _pendingResponses.listIterator(_pendingResponses.size());
					while(_iterator.hasPrevious()) {
						response = _iterator.previous();
						if((response != null) && (response.getType() == ConfigTelegram.OBJECT_SET_NAME_ANSWER_TYPE) && (info.equals(response.getInfo()))) {
							_iterator.remove();
							try {
								ObjectSetNameAnswer objectSetNameAnswer = (ObjectSetNameAnswer)response;
								if(telegram.getObjectId() == objectSetNameAnswer.getObjectId()) {
									if(objectSetNameAnswer.isNameSet()) {
										return;
									}
									else {
										throw new ConfigurationChangeException("Der Name konnte nicht gesetzt werden");
									}
								}
							}
							catch(ClassCastException ex) {
								throw new ConfigurationChangeException("Der Name konnte nicht gesetzt werden", ex);
							}
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}
}
