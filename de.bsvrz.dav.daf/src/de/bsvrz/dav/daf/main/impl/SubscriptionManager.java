/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.RequestSenderDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionInfo;
import de.bsvrz.dav.daf.communication.protocol.ClientHighLevelCommunication;
import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.main.impl.subscription.CollectingReceiver;
import de.bsvrz.dav.daf.main.impl.subscription.CollectingReceiverManager;
import de.bsvrz.dav.daf.main.impl.subscription.ReceiverSubscription;
import de.bsvrz.dav.daf.main.impl.subscription.SenderSubscription;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Verwaltung der Sende- und Empfangsanmeldungen der Datenverteiler-Applikationsfunktionen
 * <p>
 * Diese interne Subkomponente SubscriptionManager ist für die An und Abmeldungen zuständig. Sie startet beim Aufruf die notwendigen Anmeldungen für die
 * gerichtete Kommunikation zwischen Applikation und Konfiguration oder Archiv. Sie bietet Methoden, um Anmeldungen und Abmeldungen, als Empfänger, Senke,
 * Sender oder Quelle, über die Protokollsteuerung DaVDAF, beim Datenverteiler durchzuführen. Die Anmeldungen werden hier verwaltet, so dass keine doppelten
 * Anmeldungen der gleichen Daten beim Datenverteiler existieren. Weiterhin bietet sie Methoden, um Daten, über die Protokollsteuerung DaV-DAF, zum
 * Datenverteiler zu senden. Die zu sendenden Datensätze werden mit dem passenden Datensatzindex2 versehen (alter Datensatzindex + 1). Falls eine
 * Empfangsanmeldung auch Archivdaten verlangt, werden die Anfrage aktueller Daten und die Archivanfrage so gesteuert, dass keine Lücken entstehen. Sie wird vom
 * Cache-Manager benachrichtigt, falls neue Daten angekommen sind. Diese werden dann in eine Tabelle eingefügt. Diese wird von einem Aktualisierungsthread
 * zyklisch entleert, und die Daten werden an den Interessenten weitergeleitet. Durch die zyklische en-bloc- Bearbeitung der in der Tabelle aufgelaufenen Daten
 * erreicht man eine bessere Durchsatzrate im Vergleich zur Einzelbearbeitung der Datensätze.
 * <p>
 * Alle Sende und Empfangsanmeldungen werden in entsprechenden Hashtables gespeichert. Ein Thread leitet empfangene Telegramme an die entsprechenden
 * angemeldeten Empfänger weiter.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SubscriptionManager {

	/** Debuglogger */
	private static final Debug _debug = Debug.getLogger();

	/** Eine Tabelle wo die Sendeanmeldungen und deren Informationen gehalten werden */
	private Hashtable<BaseSubscriptionInfo, SendSubscriptionObject> _senderObjectTable;

	/** Eine Tabelle wo die Empfangsanmeldungen und deren Informationen gehalten werden */
	private Hashtable<BaseSubscriptionInfo, ReceiveSubscriptionObject> _receiverObjectTable;

	/** Der Cachemanager */
	private CacheManager _cacheManager;

	/** Der Konfigurationsmanager */
	private ConfigurationManager _configurationManager;

	/** Liste mit weiteren Konfigurationsmanagern für entfernte/remote Konfigurationen */
	private final List<ConfigurationManager> _secondaryConfigurationManagers = new ArrayList<ConfigurationManager>();

	/** Die höhere Ebene der Kommunikation */
	private ClientHighLevelCommunication _highLevelCommunication;

	/** Die Parameter der Datenverteiler-Applikationsfunktionen */
	private ClientDavParameters _dafParameters;

	/** Die Id der Applikation */
	private long _applicationId;

	/** Der ID des lokalen Konfigurationsverantwortlichen */
	private long _localConfigurationId;

	/** Signalisiert, dass die Initialisierung fertig ist */
	private boolean _initialisationComplete;

	/** Signalisiert den Status der Datenanmeldungen auf die Konfigurationsanfgragen */
	private final Map<Long, ConfigurationRequestStatus> _configurationRequestStatus = Collections.synchronizedMap(
			new HashMap<Long, ConfigurationRequestStatus>()
	);

	/** Der Thread, der empfangene Datensätze an die angemeldeten Empfänger weiterleitet. */
	private DataDeliveryThread _dataDeliveryThread;

	/** Verwaltung der angemeldeten Receiver */
	private CollectingReceiverManager _receiverManager;

	/** @param dafParameters Startparameter der Verbindung */
	public SubscriptionManager(ClientDavParameters dafParameters) {
		_receiverManager = new CollectingReceiverManager(dafParameters.getDeliveryBufferSize());
		_dafParameters = dafParameters;

		_receiverObjectTable = new Hashtable<BaseSubscriptionInfo, ReceiveSubscriptionObject>();
		_senderObjectTable = new Hashtable<BaseSubscriptionInfo, SendSubscriptionObject>();
		_dataDeliveryThread = new DataDeliveryThread();

		_initialisationComplete = false;
		_dataDeliveryThread.start();
	}

	/**
	 * Setzt die ClientHighLevelCommunication-Subkomponente um Anmeldungen, Abmeldungen und Datensätze an den Datenverteiler weiterzuleiten. Diese interne Methode
	 * wird von der ClientHighLevelCommunication während der Initialisierungsphase aufgerufen, um die interne Kommunikation zwischen beiden Subkomponenten zu
	 * gewährleisten.
	 *
	 * @param highLevelCommunication Referenz auf die zu setzende Kommunikationsschicht
	 */
	public final void setHighLevelCommunication(ClientHighLevelCommunication highLevelCommunication) {
		_highLevelCommunication = highLevelCommunication;
		_applicationId = _highLevelCommunication.getApplicationId();
		_localConfigurationId = _highLevelCommunication.getConfigurationId();
	}

	/**
	 * Bestimmt die Kommunikationsschicht von der der SubscriptionManager abhängig ist
	 *
	 * @return Die Kommunikationsschicht.
	 */
	public final ClientHighLevelCommunication getHighLevelCommunication() {
		return _highLevelCommunication;
	}

	/**
	 * Initialisierung der Anmeldungsverwaltung. Diese Methode wird nach erfolgreicher Authentifizierung beim Datenverteiler aufgerufen und meldet sich auf
	 * Konfigurationsanfragen und -antworten an.
	 * <p>
	 * Für alle Applikationen außer der Konfiguration sorgt diese Methode für die Anmeldung als Sender von Lese- und Schreibkonfigurationsanfragen und als Senke
	 * für Lese- und Schreibkonfigurationsantworten, um eine gerichtete Kommunikation mit der Konfiguration zu gewährleisten. Diese Methode wird von
	 * ClientDavConnection aufgerufen.
	 * @param skipConfiguration Anmeldung von Konfigurationsanfragen unterdrücken
	 */
	public final void completeInitialisation(final boolean skipConfiguration) {
		if(_highLevelCommunication == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		final ReceiveOptions _receiveOptions = ReceiveOptions.normal();
		final String applicationTypePid = _dafParameters.getApplicationTypePid();

		if(skipConfiguration || applicationTypePid.equals(CommunicationConstant.CONFIGURATION_TYPE_PID)) {
			// Anmeldung von Konfigurationsdaten nicht nötig, weil entweder selbst Konfiguration
			// oder weil es eine 2. Verbindung gibt, die die Konfigurationsanfragen durchführt.
			_initialisationComplete = true;
		}
		else {
			_configurationRequestStatus.put(_localConfigurationId, new ConfigurationRequestStatus("Lokale Konfiguration"));

			// Sender Of Configuration Read Request
			BaseSubscriptionInfo _baseSubscriptionInfo = new BaseSubscriptionInfo(
					_localConfigurationId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
			);
			SendSubscriptionInfo _sendSubscriptionInfo = new SendSubscriptionInfo(_baseSubscriptionInfo, SenderRole.sender(), true);
			_highLevelCommunication.sendSendSubscription(_sendSubscriptionInfo);

			// Sender Of Configuration Write Request
			_baseSubscriptionInfo = new BaseSubscriptionInfo(
					_localConfigurationId, AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST, (short)0
			);
			_sendSubscriptionInfo = new SendSubscriptionInfo(_baseSubscriptionInfo, SenderRole.sender(), true);
			_highLevelCommunication.sendSendSubscription(_sendSubscriptionInfo);

			// Receiver Of Configuration Read Answer
			_baseSubscriptionInfo = new BaseSubscriptionInfo(
					_applicationId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REPLY, (short)0
			);
			ReceiveSubscriptionInfo receiveSubscriptionInfo = new ReceiveSubscriptionInfo(
					_baseSubscriptionInfo, _receiveOptions, ReceiverRole.drain()
			);
			_highLevelCommunication.sendReceiveSubscription(receiveSubscriptionInfo);

			// Receiver Of Configuration Write Answer
			_baseSubscriptionInfo = new BaseSubscriptionInfo(
					_applicationId, AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REPLY, (short)0
			);
			receiveSubscriptionInfo = new ReceiveSubscriptionInfo(_baseSubscriptionInfo, _receiveOptions, ReceiverRole.drain());
			_highLevelCommunication.sendReceiveSubscription(receiveSubscriptionInfo);
		}
	}

	/**
	 * Überprüft, ob die notwendigen Anmeldungen einer gerichtete Kommunikation durch die Konfiguration erfolgt sind oder nicht.
	 *
	 * @return true wenn die Initialisierungsphase abgeschlossen ist; sonst false
	 */
	public final boolean isInitialisationComplete() {
		return _initialisationComplete;
	}

	/**
	 * Setzt den CacheManager dieser Subkomponente. Diese interne Methode wird während der Initialisierungsphase vom CacheManager aufgerufen, um die interne
	 * Kommunikation zwischen beiden Subkomponenten zu gewährleisten.
	 *
	 * @param cacheManager Referenz auf den zu setzenden Cachemanager
	 */
	final void setCacheManager(CacheManager cacheManager) {
		_cacheManager = cacheManager;
	}

	/**
	 * Setzt den ConfigurationManager dieser Subkomponente und benachrichtigt die Protokollsteuerung DaV-DAF über die Bereitschaft, Konfigurationsdaten zu
	 * empfangen. Diese interne Methode wird während der Initialisierungsphase vom ConfigurationManager aufgerufen, um die interne Kommunikation zwischen beiden
	 * Subkomponenten zu gewährleisten.
	 *
	 * @param configurationManager Referenz auf den zu setzenden Konfigurationsmanager
	 */
	public final void setConfigurationManager(ConfigurationManager configurationManager) {
		_configurationManager = configurationManager;
		if(_highLevelCommunication != null) {
			_highLevelCommunication.setReadyForConfigDependantData();
		}
	}

	/**
	 * Realisiert eine Aspektumleitung, sofern dies über entsprechende Aufrufargumente der Applikation vorgegeben wurde. Gibt den Originalaspekt eines ersetzten
	 * Aspekts einer Attributgruppe zurück. Wenn keine entsprechende Aspektumleitung besteht, wird der übergebene Aspekt unverändert zurückgegeben.
	 *
	 * @param attributeGroup Umzuleitende Attributgruppe.
	 * @param aspect         Ersetzer Aspekt.
	 *
	 * @return Originalaspekt eines ersetzenden Aspekts. Wenn keine Aspektumleitung besteht, wird der übergebene Aspekt unverändert zurückgegeben.
	 */
	private final Aspect substituteToAspect(AttributeGroup attributeGroup, Aspect aspect) {
		if(_configurationManager == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		String oldAspectPid = aspect.getPid();
		String newAspectPid = _dafParameters.substituteToAspect(attributeGroup.getPid(), oldAspectPid);
		if(!oldAspectPid.equals(newAspectPid)) {
			DataModel model = _configurationManager.getDataModel();
			if(model != null) {
				Aspect _aspect = model.getAspect(newAspectPid);
				if(_aspect != null) {
					return _aspect;
				}
			}
		}
		return aspect;
	}

	
	/**
	 * Anmeldung zum Empfangen von Daten für eine Datenidentifikation.
	 *
	 * @param receiverSubscription Anmeldeinformationen
	 *
	 * @throws DataNotSubscribedException
	 */
	private final void subscribeReceiver(ReceiverSubscription receiverSubscription) throws DataNotSubscribedException {
		if((_highLevelCommunication == null) || (_cacheManager == null) /*|| (archiveManager == null)*/) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		BaseSubscriptionInfo baseSubscriptionInfo = receiverSubscription.getBaseSubscriptionInfo();
		ReceiveSubscriptionObject receiveSubscriptionObject = _receiverObjectTable.get(
				baseSubscriptionInfo
		);

		boolean shouldSend = false;

		final ClientReceiverInterface receiver = receiverSubscription.getClientReceiver();
		final CollectingReceiver collectingReceiver = _receiverManager.addReceiverReference(receiver);
		try {
			// Die Anmeldung und das evtl. Versenden eines bereits gespeichert Datensatzes darf sich nicht mit dem Versand eines neueren Datensatzes überschneiden
			synchronized(_receiverManager) {
				// Keine Anmeldung dieses Datums vorhanden
				if(receiveSubscriptionObject == null) {
					receiveSubscriptionObject = new ReceiveSubscriptionObject(receiverSubscription, _receiverManager);
					_receiverObjectTable.put(baseSubscriptionInfo, receiveSubscriptionObject);
					shouldSend = true;
				}
				else {
					if(receiveSubscriptionObject.addSubscription(receiverSubscription, _receiverManager)) {
						receiveSubscriptionObject.setActualDataAvaillable(false);
						if(_highLevelCommunication != null) {
							shouldSend = true;
						}
					}
				}

				ResultData lastResult = null;

				// Prüfen, ob ein passender Datensatz im Cache vorhanden ist. Dies kann der Fall sein, wenn es bereits eine Anmeldung gibt.
				if(receiveSubscriptionObject.isActualDataAvaillable()) {
					CachedObject cachedObject = _cacheManager.getLastValueOfCachedData(
							baseSubscriptionInfo, receiverSubscription.getDelayedDataFlag()
					);
					if(cachedObject != null) {
						DataDescription dataDescription = receiverSubscription.getDataDescription();
						DataDescription _dataDescription = dataDescription;
						if(_dataDescription != null) {
							AttributeGroup attributeGroup = _dataDescription.getAttributeGroup();
							Aspect aspect = _dataDescription.getAspect();
							if((attributeGroup != null) && (aspect != null)) {
								Aspect _aspect = substituteToAspect(attributeGroup, aspect);
								if(!_aspect.equals(aspect)) {
									_dataDescription = dataDescription.getRedirectedDescription(_aspect);
								}
							}
						}
						lastResult = new ResultData(
								receiverSubscription.getSystemObject(),
								_dataDescription,
								cachedObject.getDelayedDataFlag(),
								cachedObject.getDataNumber(),
								cachedObject.getDataTime(),
								cachedObject.getErrorFlag(),
								cachedObject.getData()
						);
						if(receiver instanceof NonQueueingReceiver) {
							receiver.update(new ResultData[]{lastResult});
						}
						else {
							_receiverManager.storeForDeliveryWithoutBlocking(collectingReceiver, lastResult);
						}
					}
				}
			}
		}
		finally {
			// Referenz, die zum Synchronisieren benutzt wurde, wieder entfernen
			_receiverManager.removeReceiverReference(receiver);
		}
		if(shouldSend) {
			_highLevelCommunication.sendReceiveSubscription(receiveSubscriptionObject.getReceiveSubscriptionInfo());
		}
	}

	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, daß bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 *
	 * @param receiver        Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erhält.
	 * @param objects         Liste mit System-Objekten für die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options         Für die Anmeldung zu verwendende Optionen.
	 * @param role            Für die Anmeldung zu verwendende Rolle (Empfänger oder Senke).
	 * @param cacheTime       Vorhaltezeitraum in Millisekunden. Der Vorhaltezeitraum spezifiziert, wie lange empfangene Daten zwischengespeichert werden sollen.
	 *
	 * @throws IllegalArgumentException Die angegebene ReceiverRole verstößt gegen die Attributgruppenverwendung.
	 */
	public final void subscribeReceiver(
			ClientReceiverInterface receiver,
			SystemObject[] objects,
			DataDescription dataDescription,
			ReceiveOptions options,
			ReceiverRole role,
			long cacheTime) {
		if((receiver == null) || (objects == null) || (dataDescription == null) || (options == null) || (role == null)) {
			throw new IllegalArgumentException("Argument ist null");
		}

		if(!checkATGUsage(dataDescription, role)) {
			// Es handelt sich nicht um eine Testverbindung und die Datenidentifikation darf so nicht angemeldet werden.
			final Aspect aspect = dataDescription.getAspect();
			throw new IllegalArgumentException(
					"Die anzumeldende ReceiverRole " + role + " verstößt gegen die Attributgruppenverwendung. Verwendung: "
					+ dataDescription.getAttributeGroup().getAttributeGroupUsage(aspect)
			);
		}

		// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
		// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
		short externalSimulationVariant = dataDescription.getSimulationVariant();
		if(externalSimulationVariant == (short)-1) {
			externalSimulationVariant = _dafParameters.getSimulationVariant();
		}

		for(int i = 0; i < objects.length; ++i) {
			ReceiverSubscription subscription = new ReceiverSubscription(
					receiver, objects[i], dataDescription, externalSimulationVariant, options, role, cacheTime
			);
			subscribeReceiver(subscription);
		}
	}

	/**
	 * Gibt den Zeitpunkt einer Sender-Anmeldung zurück
	 * @param info Anmeldung
	 * @return Sekunden seit 1970
	 */
	public int getTimeStampFromSenderSubscription(final BaseSubscriptionInfo info) {
		final SendSubscriptionObject o = _senderObjectTable.get(info);
		return o.getTimeStamp();
	}

	/**
	 * Prüft, ob eine Anmeldung als Senke/Empfänger laut der benutzten ATG-Verwendung erlaubt ist.
	 * <p>
	 * Eine Ausnahme bildet eine "Testverbindung", eine Testverbidnung darf Datenidentifikationen anmelden, die bei normalen Verbindungen nicht erlaubt sind.
	 *
	 * @param dataDescription Datenidentifikation, mit der angemeldet werden soll.
	 * @param receiverRole    Rolle, mit der angemeldet werden soll.
	 *
	 * @return true = Die übergebene Datenidentifikation und die übergebene Rolle sind korrekt oder es handelt sich um eine Testverbindung; false = Die Senderolle
	 *         verstößt gegen die ATG-Verwendung
	 *
	 * @throws IllegalArgumentException Es sollen Konfigurationsdaten angemeldet werden. Dies ist immer verboten.
	 */
	private boolean checkATGUsage(final DataDescription dataDescription, final ReceiverRole receiverRole) {
		final Aspect aspect = dataDescription.getAspect();
		final AttributeGroupUsage usedATGUsage = dataDescription.getAttributeGroup().getAttributeGroupUsage(aspect);

		if(isOnlineUsage(usedATGUsage.getUsage())) {
			if(_dafParameters.isConnectionForTests()) {
				// Es handelt sich um eine Verbindung für Tests und es sollen Onlinedaten angemeldet werden. Damit muss nicht weiter geprüft werden
				return true;
			}
			else {
				// Es müssen alle Bediengungen geprüft werden, da es sich nicht um eine Anmeldung für einen Test handelt

				if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain) {
					// Es darf nur als Senke angemeldet werden
					return receiverRole.isDrain();
				}
				else if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver) {
					// Es darf nur als Empfänger angemeldet werden
					return receiverRole.isReceiver();
				}
				else if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) {
					// Jede Art von Anmeldung ist erlaubt
					return true;
				}
				else {
					// Es ist eine unbekannte Anfrage
					throw new IllegalArgumentException("Die Attributgruppenverwendung ist unbekannt: Usage: " + usedATGUsage.getUsage());
				}
			}
		}
		else {
			// Es sollen Konfigurationsdaten angemeldet werden, dass ist verboten.
			throw new IllegalArgumentException(
					"Es darf nur der Online-Modus verwendet werden. Datenidentifikation: " + dataDescription + " Senke/Empfänger: " + receiverRole
			);
		}
	}

	private boolean checkATGUsage(final DataDescription dataDescription, final SenderRole senderRole) {
		final Aspect aspect = dataDescription.getAspect();
		final AttributeGroupUsage usedATGUsage = dataDescription.getAttributeGroup().getAttributeGroupUsage(aspect);

		if(isOnlineUsage(usedATGUsage.getUsage())) {
			if(_dafParameters.isConnectionForTests()) {
				// Es handelt sich um eine Verbindung für Tests und es sollen Onlinedaten angemeldet werden. Damit muss nicht weiter geprüft werden
				return true;
			}
			else {
				// Es müssen alle Bediengungen geprüft werden, da es sich nicht um eine Anmeldung für einen Test handelt
				if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain) {
					// Es darf nur als Sender angemeldet werden
					return senderRole.isSender();
				}
				else if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver) {
					// Es darf nur als Empfänger angemeldet werden
					return senderRole.isSource();
				}
				else if(usedATGUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) {
					// Jede Art von Anmeldung ist erlaubt
					return true;
				}
				else {
					// Es ist eine unbekannte Anfrage
					throw new IllegalArgumentException("Die Attributgruppenverwendung ist unbekannt: Usage: " + usedATGUsage.getUsage());
				}
			}
		}
		else {
			// Es sollen Konfigurationsdaten angemeldet werden, dass ist verboten.
			throw new IllegalArgumentException(
					"Es darf nur der Online-Modus verwendet werden. Datenidentifikation: " + dataDescription + " Sender/Quelle: " + senderRole
			);
		}
	}

	/**
	 * Prüft ob es sich um eine Anmeldung auf Onlinedaten handelt.
	 *
	 * @param usage Verwendete ATG-Usage
	 *
	 * @return true = Es handelt sich auf eine Anmeldung auf Onlinedaten; false = Es ist eine Anfrage auf Konfigurationsdaten
	 *
	 * @see AttributeGroupUsage.Usage#OnlineDataAsSenderDrain
	 * @see AttributeGroupUsage.Usage#OnlineDataAsSourceReceiver
	 * @see AttributeGroupUsage.Usage#OnlineDataAsSourceReceiverOrSenderDrain
	 */
	private boolean isOnlineUsage(final AttributeGroupUsage.Usage usage) {
		if(usage == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain || usage == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
		   || usage == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiverOrSenderDrain) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeReceiver} durchgeführte Empfangsanmeldung wieder rückgängig.
	 *
	 * @param receiver        Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects         Feld mit System-Objekten für die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public final void unsubscribeReceiver(
			ClientReceiverInterface receiver, SystemObject[] objects, DataDescription dataDescription) {
		if((_highLevelCommunication == null) || (_cacheManager == null)) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
		// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
		short externalSimulationVariant = dataDescription.getSimulationVariant();
		if(externalSimulationVariant == (short)-1) {
			externalSimulationVariant = _dafParameters.getSimulationVariant();
		}

		for(int i = 0; i < objects.length; ++i) {
			BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
					objects[i].getId(), dataDescription.getAttributeGroup().getAttributeGroupUsage(dataDescription.getAspect()), externalSimulationVariant
			);
			ReceiveSubscriptionObject receiveSubscriptionObject = _receiverObjectTable.get(
					baseSubscriptionInfo
			);
			if(receiveSubscriptionObject == null) {
				continue;
			}
			if(receiveSubscriptionObject.removeSubscription(receiver, _receiverManager)) {
				if(receiveSubscriptionObject.isValidSubscription()) {
					if(_highLevelCommunication != null) {
						_highLevelCommunication.sendReceiveSubscription(receiveSubscriptionObject.getReceiveSubscriptionInfo());
					}
				}
				else {
					_receiverObjectTable.remove(baseSubscriptionInfo);
					_cacheManager.cleanCache(baseSubscriptionInfo);
					if(_highLevelCommunication != null) {
						_highLevelCommunication.sendReceiveUnsubscription(baseSubscriptionInfo);
					}
				}
			}
		}
	}

	/**
	 * Anmeldung zum Senden von Daten.
	 *
	 * @param sender          Applikationsobjekt zur Verarbeitung von Sendesteuerungen
	 * @param objects         Feld mit System-Objekten für die spezifizierten Daten anzumelden sind.
	 * @param dataDescription Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param role            Für die Anmeldung zu verwendende Rolle (Quelle oder Sender).
	 *
	 * @throws IllegalArgumentException Die SenderRole verstößt gegen die Attributgruppenverwendung
	 * @throws de.bsvrz.dav.daf.main.OneSubscriptionPerSendData Wenn bereits eine lokale Sendeanmeldung für diese Datenidentifikation vorhanden ist
	 */
	public final void subscribeSender(
			ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription, SenderRole role)
			throws OneSubscriptionPerSendData {
		if(_highLevelCommunication == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		if((sender == null) || (objects == null) || (dataDescription == null) || (role == null)) {
			return;
		}

		if(!checkATGUsage(dataDescription, role)) {
			// Es handelt sich nicht um eine Testverbindung und die Datenidentifikation darf so nicht angemeldet werden.
			final Aspect aspect = dataDescription.getAspect();
			throw new IllegalArgumentException(
					"Die anzumeldende SenderRole " + role + " verstößt gegen die Attributgruppenverwendung. Verwendung: "
					+ dataDescription.getAttributeGroup().getAttributeGroupUsage(aspect)
			);
		}

		// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
		// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
		short externalSimulationVariant = dataDescription.getSimulationVariant();
		if(externalSimulationVariant == (short)-1) {
			externalSimulationVariant = _dafParameters.getSimulationVariant();
		}

		for(int i = 0; i < objects.length; ++i) {
			SenderSubscription _senderSubscription = new SenderSubscription(sender, objects[i], dataDescription, externalSimulationVariant, role);
			BaseSubscriptionInfo baseSubscriptionInfo = _senderSubscription.getBaseSubscriptionInfo();
			if(baseSubscriptionInfo == null) {
				continue;
			}
			SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(baseSubscriptionInfo);
			if(sendSubscriptionObject == null) {
				sendSubscriptionObject = new SendSubscriptionObject(_senderSubscription);
				_senderObjectTable.put(baseSubscriptionInfo, sendSubscriptionObject);
				SendSubscriptionInfo _sendSubscriptionInfo = _senderSubscription.getSendSubscriptionInfo();
				if((_sendSubscriptionInfo != null) && (_highLevelCommunication != null)) {
					_highLevelCommunication.sendSendSubscription(_sendSubscriptionInfo);
				}
			}
			else if(role.equals(SenderRole.sender()) && !sendSubscriptionObject.isSource()){
				// Mehrere Sender dürfen sich auf eine Identifikation anmelden
				sendSubscriptionObject.addSender(_senderSubscription);
			}
			else {
				// ... aber nicht mehrere Quellen
				throw new OneSubscriptionPerSendData("Ein Datum kann nur von einer Quelle angemeldet sein.");
			}
		}
	}

	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeSender} durchgeführte Sendeanmeldung wieder rückgängig.
	 *
	 * @param sender          Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects         Feld mit System-Objekten für die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public final void unsubscribeSender(
			ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription) {
		if(_highLevelCommunication == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		if((sender == null) || (objects == null) || (dataDescription == null)) {
			return;
		}

		// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
		// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
		short externalSimulationVariant = dataDescription.getSimulationVariant();
		if(externalSimulationVariant == (short)-1) {
			externalSimulationVariant = _dafParameters.getSimulationVariant();
		}

		for(int i = 0; i < objects.length; ++i) {
			BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
					objects[i].getId(), dataDescription.getAttributeGroup().getAttributeGroupUsage(dataDescription.getAspect()), externalSimulationVariant
			);
			SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(baseSubscriptionInfo);
			if(sendSubscriptionObject != null) {
				sendSubscriptionObject.removeSender(sender);
				if(sendSubscriptionObject.isEmpty()) {
					_senderObjectTable.remove(baseSubscriptionInfo);
					if(_highLevelCommunication != null) {
						_highLevelCommunication.sendSendUnsubscription(baseSubscriptionInfo);
					}
				}
			}
		}
	}

	/**
	 * Sendet eine Konfigurationsanfrage zum Datenverteiler. Die Daten müssen vorher mit einer Sendeanmeldung angemeldet worden sein.
	 *
	 * @param sendData Zu sendender Datensatz.
	 *
	 * @throws de.bsvrz.dav.daf.main.DataNotSubscribedException Wenn die Daten nicht zum Senden angemeldet waren.
	 */
	final void sendData(SendDataObject sendData) throws DataNotSubscribedException {
		if(_highLevelCommunication == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		final BaseSubscriptionInfo info = sendData.getBaseSubscriptionInfo();
		final ConfigurationRequestStatus configurationRequestStatus = _configurationRequestStatus.get(info.getObjectID());
		if(configurationRequestStatus != null) {
			if(info.getUsageIdentification() == AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST) {
				if(!configurationRequestStatus.isConfigurationReadRequestConfirmed()) {
					throw new DataNotSubscribedException("Keine Konfigurationsapplikation beim Datenverteiler angemeldet.");
				}
				else {
					if(_highLevelCommunication != null) {
						_highLevelCommunication.sendData(sendData);
					}
				}
				return;
			}
			else if(info.getUsageIdentification() == AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST) {
				if(!configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
					throw new DataNotSubscribedException("Keine Konfigurationsapplikation beim Datenverteiler angemeldet.");
				}
				else {
					if(_highLevelCommunication != null) {
						_highLevelCommunication.sendData(sendData);
					}
				}
				return;
			}
		}
		SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(info);
		if(sendSubscriptionObject == null) {
			throw new DataNotSubscribedException("Der Datensatz kann nicht versendet werden. Er muss vorher angemeldet sein (" + info.getObjectID() + ", "
			        + info.getUsageIdentification() + ")");
		}
		else {
			if(_highLevelCommunication != null) {
				_highLevelCommunication.sendData(sendData);
			}
		}
	}

	/**
	 * Sendet einen Ergebnisdatensatz zum Datenverteiler. Die Daten müssen vorher mit einer Sendeanmeldung angemeldet worden sein.
	 *
	 * @param result Ergebnis mit dem zu sendenden Datensatz.
	 *
	 * @throws DataNotSubscribedException Wenn die Daten nicht zum Senden angemeldet waren.
	 * @throws de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed Wenn die Sendesteuerung abgewartet werden muss, bevor gesendet werden kann.
	 */
	public final void sendData(ResultData result) throws DataNotSubscribedException, SendSubscriptionNotConfirmed {
		if(_highLevelCommunication == null) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		if(result == null) {
			return;
		}
		SystemObject systemObject = result.getObject();
		DataDescription description = result.getDataDescription();
		if((systemObject == null) || (description == null)) {
			return;
		}
		AttributeGroup attributeGroup = description.getAttributeGroup();
		Aspect aspect = description.getAspect();
		if((attributeGroup == null) || (aspect == null)) {
			return;
		}
		long id = systemObject.getId();

		// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
		// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
		short externalSimulationVariant = description.getSimulationVariant();
		if(externalSimulationVariant == (short)-1) {
			externalSimulationVariant = _dafParameters.getSimulationVariant();
		}

		BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
				id, attributeGroup.getAttributeGroupUsage(aspect), externalSimulationVariant
		);

		SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(baseSubscriptionInfo);
		if(sendSubscriptionObject == null) {
			throw new DataNotSubscribedException("Der Datensatz kann nicht versendet werden. Er muss vorher angemeldet sein ("
			        + systemObject.getPidOrNameOrId() + ", " + attributeGroup.getPidOrNameOrId() + ", " + aspect.getPidOrNameOrId() + ")");
		}
		if(!sendSubscriptionObject.canSendData()) {
			throw new SendSubscriptionNotConfirmed("Die Sendeanmeldung wurde noch nicht bestätigt ("
			        + systemObject.getPidOrNameOrId() + ", " + attributeGroup.getPidOrNameOrId() + ", " + aspect.getPidOrNameOrId() + ")");
		}
		final Data data = result.getData();
		final byte[] dataBytes;
		if(data != null) {
			if(data instanceof ByteArrayData) {
				ByteArrayData byteArrayData = (ByteArrayData)data;
				dataBytes = byteArrayData.getBytes();
				
			}
			else {
				Thread.dumpStack();
				throw new IllegalArgumentException("Daten können nicht serialisiert werden: " + data.getClass().getName());
			}
		}
		else {
			dataBytes = null;
		}
		synchronized(sendSubscriptionObject) {
			SendDataObject object = new SendDataObject(
					baseSubscriptionInfo,
					result.isDelayedData(),
					sendSubscriptionObject.getSendDataIndex(),
					result.getDataTime(),
					result.hasData() ? (byte)0 : (byte)1,
					null,
					dataBytes
			);
			if(_highLevelCommunication != null) {
				_highLevelCommunication.sendData(object);
			}
		}
	}

	/**
	 * Wenn eine Sendeanmeldung mit aktivierter Benachrichtigungsoption vorliegt, dann wird ihr über diese Methode mitgeteilt, welche Aktion von ihr durchzuführen
	 * ist. Es gibt folgende mögliche Aktionen: o Sendung starten. o Sendung anhalten. o Sendung anhalten (Grund: keine Rechte vorhanden). o Sendung anhalten
	 * (Grund: mehrere Quellen vorhanden). Wenn eine Sendung wegen mehrerer vorhandener Quellen angehalten werden muss, wird der zugehörige Vermerk der Anmeldung
	 * aus dem Anmelde-Manager entfernt. Die Nachricht wird durch den Aufruf der ClientSenderInterface-Methode dataRequest(SystemObject object, DataDescription
	 * dataDescription, byte state) zum Sender weitergeleitet: Auch die Sendeanmeldungen der Konfiguration wird hierdurch bestätigt. So wird ersichtlich, ob eine
	 * Konfiguration im System vorhanden ist.
	 *
	 * @param info Anmeldeinfo, auf die sich die Sendesteuerung bezieht
	 * @param state Zustand der Sendesteuerung
	 */
	public synchronized void notifySenderApplication(BaseSubscriptionInfo info, byte state) {
		if(info == null) {
			return;
		}
		ConfigurationRequestStatus configurationRequestStatus = _configurationRequestStatus.get(info.getObjectID());
		if(configurationRequestStatus != null) {
			final String configIdentifier;
			if(info.getObjectID() == _localConfigurationId){
				configIdentifier = "Die Konfiguration";
			}
			else{
				configIdentifier = "Die entfernte Konfiguration " + configurationRequestStatus.getName();
			}
			if(info.getUsageIdentification() == AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST) {
				if(state == 0) {
					_debug.finest(configIdentifier + " ist verfügbar (Lesen)");
					if(!configurationRequestStatus.isConfigurationReadRequestConfirmed() && configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
						_debug.info(configIdentifier + " ist für Anfragen bereit");
					}
					configurationRequestStatus.setConfigurationReadRequestConfirmed(true);
					if(configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
						_initialisationComplete = true;
					}
				}
				else {
					// stop Sending there is no configuration any more
					_debug.finest(configIdentifier + " ist nicht verfügbar (Lesen)");
					if(configurationRequestStatus.isConfigurationReadRequestConfirmed() && configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
						_debug.warning(configIdentifier + " ist nicht mehr für Anfragen bereit");
					}
					configurationRequestStatus.setConfigurationReadRequestConfirmed(false);
				}
				return;
			}
			else if(info.getUsageIdentification() == AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST) {
				if(state == 0) {
					_debug.finest(configIdentifier + " ist verfügbar (Schreiben)");
					if(configurationRequestStatus.isConfigurationReadRequestConfirmed() && !configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
						_debug.info(configIdentifier + " ist für Anfragen bereit");
					}
					configurationRequestStatus.setConfigurationWriteRequestConfirmed(true);
					if(configurationRequestStatus.isConfigurationReadRequestConfirmed()) {
						_initialisationComplete = true;
					}
				}
				else {
					// stop Sending there is no configuration any more
					_debug.finest(configIdentifier + " ist nicht verfügbar (Schreiben)");
					if(configurationRequestStatus.isConfigurationReadRequestConfirmed() && configurationRequestStatus.isConfigurationWriteRequestConfirmed()) {
						_debug.warning(configIdentifier + " ist nicht mehr für Anfragen bereit");
					}
					configurationRequestStatus.setConfigurationWriteRequestConfirmed(false);
				}
				return;
			}
		}
		SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(info);
		if(sendSubscriptionObject != null) {
			if(_configurationManager == null) {
				throw new InitialisationNotCompleteException(
						"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
				);
			}
			sendSubscriptionObject.confirmSendDataRequest(state);
			DataModel dataModel = _configurationManager.getDataModel();
			if(dataModel != null) {

				String warning = null;
				if(state == RequestSenderDataTelegram.STOP_SENDING_NOT_A_VALID_SUBSCRIPTION) {
					warning = "Ungültige Anmeldung";
				}
				if(state == RequestSenderDataTelegram.STOP_SENDING_NO_RIGHTS) {
					warning = "Ungültige Anmeldung (keine Rechte)";
				}

				Collection<SenderSubscription> senderSubscriptions = sendSubscriptionObject.getSenderSubscriptions();
				for(SenderSubscription senderSubscription : senderSubscriptions) {
					ClientSenderInterface client = senderSubscription.getClientSender();

					if(client != null) {
						SystemObject object = dataModel.getObject(info.getObjectID());
						DataDescription dataDescription = senderSubscription.getDataDescription();
						if(warning != null) {
							_debug.warning("Negative Sendesteuerung: " + warning + " (" + (object == null ? "null" : object.getPidOrNameOrId()) + " " + dataDescription);
							warning = null;
						}
						if(dataDescription != null) {
							AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
							Aspect aspect = dataDescription.getAspect();
							if((attributeGroup != null) && (aspect != null)) {
								Aspect _aspect = substituteToAspect(attributeGroup, aspect);
								if(!_aspect.equals(aspect)) {
									dataDescription = dataDescription.getRedirectedDescription(_aspect);
								}
							}
						}
						client.dataRequest(object, dataDescription, state);
					}
				}
			}
		}
	}

	/**
	 * Gibt die Verweilzeit eines Datums im Cache zurück
	 *
	 * @param info Anmeldeinfo, zu der die Verweilzeit bestimmt werden soll.
	 *
	 * @return Verweilzeit in Millisekunden
	 */
	long getTimeInCache(BaseSubscriptionInfo info) {
		ReceiveSubscriptionObject receiveSubscriptionObject = _receiverObjectTable.get(info);
		if(receiveSubscriptionObject == null) {
			return 0; // Wenn es keine Anmeldungen (mehr) gibt, dann muss auch nicht länger vorgehalten werden.
		}
		else {
			long time = receiveSubscriptionObject.getTimeInCache();
			if(time < 0) {
				time = 0; // Überflüssig, weil dies bereits im Konstruktor von ReceiverSubscription sichergestellt wird 
			}
			return time;
		}
	}

	/**
	 * Diese Methode wird von Cache-Manager aufgerufen, wenn aktuelle Daten empfangen wurden. Wenn eine entsprechende Empfangsanmeldung für diese Daten vorliegt,
	 * dann wird der zugehörige Repräsentant darüber benachrichtigt. Die Daten werden an angemeldete Empfänger weitergeleitet. Vorher wird der ursprüngliche bei
	 * der Anmeldung verwendete Aspekt wiederhergestellt, falls dieser umgeleitet wurde.
	 *
	 * @param cachedObject Empfangener Datensatz.
	 *
	 * @throws InterruptedException Wenn der Thread während eines blockierenden Aufrufs unterbrochen wurde
	 */
	void actualDataUpdate(CachedObject cachedObject) throws InterruptedException {
		if((_configurationManager == null) || (_cacheManager == null) /*|| (archiveManager == null)*/) {
			throw new InitialisationNotCompleteException(
					"Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert."
			);
		}
		if(cachedObject == null) {
			return;
		}
		BaseSubscriptionInfo baseSubscriptionInfo = cachedObject.getBaseSubscriptionInfo();
		if(baseSubscriptionInfo != null) {
			ReceiveSubscriptionObject receiveSubscriptionObject = _receiverObjectTable.get(
					baseSubscriptionInfo
			);
			if(receiveSubscriptionObject != null) {
				receiveSubscriptionObject.setActualDataAvaillable(true);
				List<ReceiverSubscription> list = receiveSubscriptionObject.getReceiverSubscriptionList();
				if(list != null) {
					final ReceiverSubscription[] receiverSubscriptions;
					synchronized(list) {
						receiverSubscriptions = list.toArray(new ReceiverSubscription[list.size()]);
					}
					for(int i = 0; i < receiverSubscriptions.length; ++i) {
						ReceiverSubscription receiverSubscription = receiverSubscriptions[i];
						if(receiverSubscription != null) {
							DataDescription dataDescription = receiverSubscription.getDataDescription();
							if(dataDescription != null) {
								AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
								Aspect aspect = dataDescription.getAspect();
								if((attributeGroup != null) && (aspect != null)) {
									Aspect _aspect = substituteToAspect(attributeGroup, aspect);
									if(!_aspect.equals(aspect)) {
										dataDescription = dataDescription.getRedirectedDescription(_aspect);
									}
								}
							}
							ResultData result = new ResultData(
									receiverSubscription.getSystemObject(),
									dataDescription,
									cachedObject.getDelayedDataFlag(),
									cachedObject.getDataNumber(),
									cachedObject.getDataTime(),
									cachedObject.getErrorFlag(),
									cachedObject.getData()
							);
							ClientReceiverInterface receiver = receiverSubscription.getClientReceiver();
							if(receiver instanceof NonQueueingReceiver) {
								receiver.update(new ResultData[]{result});
							}
							else {
								final CollectingReceiver collectingReceiver = receiverSubscription.getCollectingReceiver();
								if(collectingReceiver != null) {
									_receiverManager.storeForDelivery(collectingReceiver, result);
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Gibt der Index der Sendung der gegebene Anmeldungsinformation zurück. Der Index startet immer mit 1 und wird bei jeder Abfrage um 1 erhöht und wenn es
	 * 0x3FFFFFFF erreicht hat, dann fängt es wieder bei 1 an.
	 *
	 * @param info Anmeldeinformationen der Sendung
	 *
	 * @return der Index der Sendung
	 */
	final long getSendDataIndex(BaseSubscriptionInfo info) {
		SendSubscriptionObject sendSubscriptionObject = _senderObjectTable.get(info);
		if(sendSubscriptionObject != null) {
			return sendSubscriptionObject.getSendDataIndex();
		}
		return 0;
	}

	/** Schließt diese Subkomponente und terminiert den Aktualisierungsthread. */
	public final void close() {
		if(_dataDeliveryThread != null) {
			_dataDeliveryThread.interrupt();
		}
	}

	/**
	 * Fügt einen ConfigurationManager für eine entfernte Konfiguration hinzu
	 * @param configurationManager ConfigurationManager
	 * @param name Name der Konfiguration
	 */
	public void addConfiguration(final ConfigurationManager configurationManager, final String name) {
		_secondaryConfigurationManagers.add(configurationManager);
		_configurationRequestStatus.put(configurationManager.getConfigurationId(), new ConfigurationRequestStatus(name));
	}

	/**
	 * Gibt eine Liste mit ConfigurationManagern zurück, die entfernte Konfigurationen verwalten
	 * @return eine unveränderliche Liste mit Konfigurationsmanagern
	 */
	public List<ConfigurationManager> getSecondaryConfigurationManagers() {
		return Collections.unmodifiableList(_secondaryConfigurationManagers);
	}

	/**
	 * Wartet auf den Verbindungsaufbau zu einer Konfiguration
	 * @param configAuthorityId Konfigurationsverantwortlichen-Id
	 * @throws ConfigurationTaskException
	 */
	public void waitForInitialization(final long configAuthorityId) throws ConfigurationTaskException {
		final ConfigurationRequestStatus configurationRequestStatus = _configurationRequestStatus.get(configAuthorityId);
		synchronized(configurationRequestStatus) {
			final long startTime = System.currentTimeMillis();
			while(!configurationRequestStatus.isInitialized()){
				try {
					configurationRequestStatus.wait(10000);
				}
				catch(InterruptedException e) {
					throw new ConfigurationTaskException("Unterbrechung beim Warten auf die Konfiguration", e);
				}
				if(System.currentTimeMillis() - startTime > CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
					throw new ConfigurationTaskException("Timeout beim Warten auf die Konfiguration");
				}
			}
		}
	}

	class DataDeliveryThread extends Thread {

		DataDeliveryThread() {
			super("Updater");
			setDaemon(true);
		}

		public final void run() {
			while(!interrupted()) {
				try {
					_receiverManager.deliverOnce();
				}
				catch(InterruptedException ex) {
					return;
				}
				catch(Exception e){
					_debug.error("Fehler im Updater-Thread, Datenverteilerverbindung wird terminiert", e);
					_highLevelCommunication.terminate(true, "Fehler im Updater-Thread: " + e.toString());
					return;
				}
			}
		}
	}

	/**
	 * Status der Verbindung mit einer Konfiguration
	 */
	private class ConfigurationRequestStatus {
		/** Signalisiert, dass die Konfigurationsanfrage zum Lesen vom Datenverteiler bestätigt wurde */
		private boolean _configurationReadRequestConfirmed = false;

		/** Signalisiert, dass die Konfigurationsanfrage zum Schreiben vom Datenverteiler bestätigt wurde */
		private boolean _configurationWriteRequestConfirmed = false;

		private final String _name;

		public ConfigurationRequestStatus(final String name) {
			_name = name;
		}

		public synchronized boolean isConfigurationReadRequestConfirmed() {
			return _configurationReadRequestConfirmed;
		}

		public synchronized void setConfigurationReadRequestConfirmed(final boolean configurationReadRequestConfirmed) {
			_configurationReadRequestConfirmed = configurationReadRequestConfirmed;
			if(isInitialized()){
				notifyAll();
			}
		}

		public synchronized boolean isConfigurationWriteRequestConfirmed() {
			return _configurationWriteRequestConfirmed;
		}

		public synchronized void setConfigurationWriteRequestConfirmed(final boolean configurationWriteRequestConfirmed) {
			_configurationWriteRequestConfirmed = configurationWriteRequestConfirmed;
			if(isInitialized()){
				notifyAll();
			}
		}

		public String getName() {
			return _name;
		}

		public synchronized boolean isInitialized() {
			return _configurationReadRequestConfirmed && _configurationWriteRequestConfirmed;
		}
	}
}
