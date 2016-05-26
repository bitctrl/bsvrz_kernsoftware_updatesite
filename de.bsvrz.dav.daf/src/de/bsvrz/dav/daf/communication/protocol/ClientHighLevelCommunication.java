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

package de.bsvrz.dav.daf.communication.protocol;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.HighLevelCommunicationCallbackInterface;
import de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunicationInterface;
import de.bsvrz.dav.daf.communication.lowLevel.SplittedApplicationTelegramsTable;
import de.bsvrz.dav.daf.communication.lowLevel.TelegramUtility;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.*;
import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.impl.CacheManager;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.ConfigurationManager;
import de.bsvrz.dav.daf.main.impl.SubscriptionManager;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Das Modul Protokollsteuerung ist das Bindeglied der Komponente Kommunikation zwischen den Modulen Telegrammverwaltung und Verwaltung. Es stellt für die
 * Interaktion mit dem Datenverteiler eine Funktionsschnittstelle zur Verfügung, die die technischen Aspekte der Kommunikation gegenüber der Verwaltung kapselt
 * und implementiert die Abbildung auf Telegramme und Kommunikationsabläufe. Die Protokollsteuerung grenzt sich zur Telegrammverwaltung ab, weil hier die aus
 * Sicht der Datenverteiler-Applikationsfunktionen speziellen und nicht wiederverwendbaren Funktionen der Kommunikation mit dem Datenverteiler enthalten sind.
 * Folgende Funktionen und Abläufe werden in der Protokollsteuerung implementiert: <ul> <li>Initialisierung und Verhandlung der Protokollversion. </li>Mit Hilfe
 * der Telegrammverwaltung wird die TCP-Verbindung zum Datenverteiler hergestellt. Anschließend wird dem Datenverteiler in einem
 * Protokollversionsanfrage-Telegramm mitgeteilt, welche Versionen des Kommunikationsprotokolls unterstützt werden. Als Reaktion wird vom Datenverteiler ein
 * Protokollversionsantwort-Telegramm erwartet in dem die zu verwendende Version des Protokolls enthalten ist. <li>Authentifizierung gegenüber dem
 * Datenverteiler und Verhandlung der Verbindungsparameter. </li>Zur Authentifizierung wird ein Authentifizierungsbereitschafts-Telegramm übertragen. Als
 * Antwort wird ein vom Datenverteiler generierter Zufallstext im Authentifizierungsaufforderungs-Telegramm erwartet. Dieser Text wird durch das Modul
 * HMAC-MD5-Verschlüsselung verschlüsselt. Als geheimer Schlüssel dient dabei das Passwort des Benutzers. Der verschlüsselte Text wird mit weiteren
 * Informationen zum Datenverteiler übertragen. Als Antwort wird vom Datenverteiler ein Authentifizierungsannahme-Telegramm oder ein
 * Authentifizierungsablehnungs-Telegramm erwartet. Im Falle einer Authentifizierungsablehnung wird der Authentifizierungsvorgang abgebrochen und kann bei
 * Bedarf von der Verwaltung mit anderen Authentifizierungsinformationen wiederholt initiiert werden. Im Falle einer Authentifizierungsannahme werden
 * anschließend die Verbindungsparameter verhandelt. Dazu sendet die Protokollsteuerung ein Verbindungsparameteranfrage-Telegramm an den Datenverteiler und
 * erwartet ein Verbindungsparameterantwort-Telegramm mit der Festlegung der Verbindungsparameter vom Datenverteiler. <li>Abbruch der Verbindung</li> Diese
 * Funktion führt zum sofortigen Abbruch der Verbindung zum Datenverteiler mit Hilfe der Abbruch-Funktion der Telegrammverwaltung. <li>Terminierung der
 * Verbindung</li>Zur Terminierung der Verbindung zum Datenverteiler wird ein Applikationsabmeldungs-Telegramm übertragen und die Kommunikationsverbindung
 * terminiert. Dabei wird durch die Telegrammverwaltung sichergestellt, dass alle noch im Sendepuffer befindliche Telegramme vor dem Schließen der
 * Kommunikationsverbindung zum Datenverteiler übertragen werden. <li>Parametrierung des Datenverteilers</li>Zur An- bzw. Abmeldung von zu empfangenden bzw. zu
 * sendenden Daten wird ein entsprechendes Telegramm (Empfangsanmeldung, Sendeanmeldung, Empfangsabmeldung, Sendeabmeldung) an den Datenverteiler übertragen.
 * <li>Entgegennahme von Sendesteuerungs-Telegrammen und Weiterleitung der enthaltenen Information (ob Daten, die von der Applikation zum Senden angemeldet
 * wurden, vom Datenverteiler benötigt werden oder nicht) an die Verwaltung. </li> <li>Senden von Anwendungsdaten</li>Zu sendende Datenblöcke werden in
 * Telegramme zerlegt und an die Telegrammverwaltung weitergeleitet.<li>Empfang von Anwendungsdaten</li>Von der Telegrammverwaltung entgegengenommene Telegramme
 * werden zu Datenblöcken rekombiniert und an das Modul Verwaltung weitergeleitet.<li>Zerlegung von großen Datenblöcken in mehrere Datentelegramme beim Senden
 * und Rekombination von zusammengehörigen Datentelegrammen zu einem Datenblock beim Empfang.</li><li>Telegrammlaufzeitermittlung</li>Zur Messung von
 * Telegrammlaufzeiten wird ein Telegrammlaufzeitanfrage-Telegramm mit der aktuellen Systemzeit an den Datenverteiler übertragen. Als Antwort wird ein
 * Telegrammlaufzeitantwort-Telegramm erwartet, in dem die ursprünglich übertragene Systemzeit enthalten ist. Durch Vergleich der Zeit zwischen dem Absenden des
 * Anfrage-Telegramms und dem Erhalt der Antwort wird die Laufzeit für Hin- und Rückweg zusammen ermittelt. Andererseits kann auch vom Datenverteiler eine
 * solche Messung durchgeführt werden, d.h. bei Empfang eines Telegrammlaufzeitanfrage-Telegramms wird die enthaltene Systemzeit in einem
 * Telegrammlaufzeitantwort-Telegramm zurück übertragen.<li>Fehlerbehandlung</li>Fehlerzustände, die von der Protokollsteuerung erkannt werden oder von der
 * Telegrammverwaltung gemeldet werden, führen zu einem Abbruch der Verbindung und zu einer Benachrichtigung des Moduls Verwaltung.</ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ClientHighLevelCommunication implements HighLevelCommunicationCallbackInterface {

	/** Der Debug-Logger. */
	private static final Debug _debug = Debug.getLogger();

	/** Die eingestellte Protokollversion */
	private int _dafVersion = 3;

	/** Die Id des Benutzers */
	private long _userId;

	/** Die Applikation Id */
	private long _applicationId;

	/** Die lokale Konfiguration Id */
	private long _localConfigurationId;

	/** Die lokale Datenverteiler-Id */
	private long _localDVId;

	/** Der Konfigurationsmanager (Teil der Verwaltung). */
	private ConfigurationManager _configurationManager;

	/** Der Cache-Manager (Teil der Verwaltung). */
	private CacheManager _cacheManager;

	/** Der Anmeldemanager (Teil der Verwaltung). */
	private SubscriptionManager _subscriptionManager;

	/** Temporäre Liste der Systemtelegramme für interne Synchronisationszwecke. */
	private final List<DataTelegram> _syncSystemTelegramList;

	/** Temporäre Liste der zerstückelten Telegramme. */
	private SplittedApplicationTelegramsTable splittedTelegramsTable;

	/** Hält die Informationen über den Initialisierungszustand dieser Komponente. */
	private boolean _readyForConfigDependantData;

	/** Wird beim ersten Aufruf von terminate(...) gesetzt. */
	private volatile boolean _disconnecting = false;

	/** Wird beim ersten Aufruf von terminate(true, ...) gesetzt. */
	private volatile boolean _disconnectingOnError = false;

	/** Das Objekt, das für das Schliessen nach Auftritt eines Fehlers zuständig ist. */
	private ApplicationCloseActionHandler _closer;

	/** Die Kommunikationseingenschaften */
	private ClientConnectionProperties properties;

	/** Beobachter für Zustandsänderungen der Datenverteilerverbindung. */
	private DavConnectionListener _connectionListener;

	/** Asynchrone Verarbeitung von empfangenen Sendsteuerungstelegrammen */
	private ClientHighLevelCommunication.SendControlNotifier _sendControlNotifier;
	
	// Für Tests
	private static boolean DO_NOT_SEND_AUTH_REQUEST_FOR_TESTS = false;

	/**
	 * Dieser Konstruktor erzeugt eine Instanz dieser Klasse mit den übergebenen Parametern. {@link de.bsvrz.dav.daf.main.ClientDavParameters} enthält die Adresse
	 * und Subadresse des Datenverteilers, spezifiziert das zu verwendende Protokoll durch dessen Namen. {@link de.bsvrz.dav.daf.main.ApplicationCloseActionHandler}
	 * bestimmt wie die Applikationsfunktion auf einen Kommunikationsfehler reagieren soll.
	 * <p>
	 * {@link ClientConnectionProperties} werden erzeugt, die die Eigenschaften dieser Verbindung verkörpern. Sie spezifizieren insbesondere
	 * den {@link de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess AuthentificationProcess}, das für die Passwortverschlüsselung zuständige Verfahren. Weiter enthalten
	 * sie die PID der Konfiguration.
	 * <p>
	 * Ein Systemtelegramm {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionRequest} wird gebildet und zum Datenverteiler gesendet. Es enthält Angaben über die
	 * unterstützte Protokollversion. Auf die Antwort wird eine festgelegte Zeit gewartet (maximale Wartezeit auf synchrone Antworten). Wenn die Antwort nicht
	 * innerhalb diese Zeit an-gekommen ist oder die Protokollversion vom Datenverteiler nicht unterstützt wird, dann wird eine CommunicationError-Ausnahme
	 * erzeugt.
	 *
	 * @param clientDavParameters Parameterklasse für die Datenverteiler-Applikationsfunktionen
	 * @param closer              bestimmt, wie die Applikationsfunktionen auf einen Kommunikationsfehler reagieren soll
	 *
	 * @throws CommunicationError  Wenn die Verhandlung der Protokollversion nicht durchgeführt wurde oder die Protokollversion nicht unterstützt wird.
	 * @throws ConnectionException Wenn die Verbindung über die Telegrammverwaltung fehlschlägt.
	 */
	public ClientHighLevelCommunication(ClientDavParameters clientDavParameters, ApplicationCloseActionHandler closer) throws CommunicationError, ConnectionException {
		LowLevelCommunicationInterface lowLevelCommunication = null;
		properties = new ClientConnectionProperties(clientDavParameters);
		_closer = closer;
		_syncSystemTelegramList = new LinkedList<DataTelegram>();
		splittedTelegramsTable = new SplittedApplicationTelegramsTable();
		_readyForConfigDependantData = false;
		_sendControlNotifier = new SendControlNotifier();

		lowLevelCommunication = properties.getLowLevelCommunication();
		String ip = properties.getCommunicationAddress();
		int port = properties.getCommunicationSubAddress();
		lowLevelCommunication.connect(ip, port);
		lowLevelCommunication.setHighLevelComponent(this);

		// Protokollversion verhandeln
		ProtocolVersionAnswer protocolVersionAnswer = getProtocolVersions();
		String errorMessage = null;
		if(protocolVersionAnswer == null) {
			errorMessage = "Der Datenverteiler antwortet nicht auf die Verhandlung der Protokollversionen";
		}
		else {
			int davVersion = protocolVersionAnswer.getPreferredVersion();
			if(davVersion != _dafVersion) {
				errorMessage = "Die lokale Protokollversion (" + _dafVersion + ") wird vom Datenverteiler nicht unterstützt.";
			}
		}
		if(errorMessage != null) {
			lowLevelCommunication.disconnect(true, errorMessage, null);
			throw new CommunicationError(errorMessage);
		}

		_sendControlNotifier.start();
	}

	/** 
	 * Gibt <tt>true</tt> zurück, wenn die Verbindung getrennt wurde
	 * @return <tt>true</tt>, wenn die Verbindung getrennt wurde, sonst <tt>false</tt>
	 */
	public boolean isDisconnecting() {
		return _disconnecting;
	}

	/**
	 * Asynchrone Verarbeitung von empfangenen Sendsteuerungstelegrammen. Speichert mit put übergebene Sendesteuerungstelegramme in einer internen Queue, aus der
	 * die Telegramme asynchron von einem eigenen Thread ausgelesen und zur Weiterverarbeitung an den SubscriptionsManager weitergegeben werden.
	 */
	private class SendControlNotifier implements Runnable {

		private UnboundedQueue<RequestSenderDataTelegram> _telegrams;

		/** Der Konstruktor erzeugt die interne Queue. Der Thread zur Verarbeitung der Telegramme wird erst später bei Aufruf der start()-Methode angelegt und gestartet. */
		public SendControlNotifier() {
			_telegrams = new UnboundedQueue<RequestSenderDataTelegram>();
		}

		/** Erzeugt und startet einen separaten Thread zur Verarbeitung der Telegramme. */
		private void start() {
			final Thread thread = new Thread(this, "SendControlNotifier");
			thread.setDaemon(true);
			thread.start();
		}

		/**
		 * Speichert das übergebene Telegramm in der Queue zur asynchronen Verarbeitung.
		 *
		 * @param telegram Zu verarbeitendes Sendesteuerungstelegramm.
		 */
		public void put(RequestSenderDataTelegram telegram) {
			_telegrams.put(telegram);
		}

		/** Signalisiert dem Thread zur Verarbeitung der Telegramme, dass keine weiteren Telegramme verarbeitet werden müssen und der Thread sich beenden kann. */
		public void close() {
			_telegrams.put(null);
		}

		/** Methode zur asynchronen Verarbeitung von gespeicherten Telegrammen durch einen eigenen Thread. */
		public void run() {
			try {
				RequestSenderDataTelegram telegram;
				while((telegram = _telegrams.take()) != null) {
					if(_disconnecting) return;
					try {
						_subscriptionManager.notifySenderApplication(telegram.getDataToSendInfo(), telegram.getState());
					}
					catch(RuntimeException e) {
						_debug.error("Fehler bei der Verarbeitung der Sendesteuerung", e);
					}
				}
			}
			catch(InterruptedException e) {
				_debug.fine("SendControlNotifier-Thread wurde unterbrochen");
			}
			_debug.fine("SendControlNotifier-Thread beendet sich");
		}
	}

	/** Dieser Konstruktor wird für JUnit-Tests gebraucht. */
	public ClientHighLevelCommunication() {
		// Variablen setzen, die final sind
		_syncSystemTelegramList = new LinkedList<DataTelegram>();
	}

	/**
	 * Schließt die Initialisierung ab. Diese Methode wird von ClientDavConnection aufgerufen. Es werden Referenzen auf den Konfigurationsmanager, den
	 * Cache-Manager und den Anmeldungsmanager festgehalten. Zusätzlich wird eine Referenz auf ClientHighLevelCommunication an den Anmeldungsmanager übergeben.
	 *
	 * @param configurationManager der Konfigurationsmanager
	 * @param cacheManager         der Cache-Manager
	 * @param subscriptionManager  der Anmeldungsmanager
	 */
	public final void completeInitialisation(ConfigurationManager configurationManager, CacheManager cacheManager, SubscriptionManager subscriptionManager) {
		_configurationManager = configurationManager;
		_cacheManager = cacheManager;
		_subscriptionManager = subscriptionManager;
		_subscriptionManager.setHighLevelCommunication(this);
	}

	/**
	 * Gibt die Id der Applikation zurück.
	 *
	 * @return die Id der Applikation
	 */
	public long getApplicationId() {
		return _applicationId;
	}

	/**
	 * Gibt die Id des Datenverteilers zurück.
	 *
	 * @return die Id des Datenverteilers
	 */
	public final long getDataTransmitterId() {
		return _localDVId;
	}

	/**
	 * Gibt die Id der Konfiguration zurück.
	 *
	 * @return die ID der Konfiguration
	 */
	public final long getConfigurationId() {
		return _localConfigurationId;
	}

	/**
	 * Gibt die Id des Benutzers zurück.
	 *
	 * @return die ID des Benutzers
	 */
	public final long getUserId() {
		return _userId;
	}

	/**
	 * Diese Methode wird vom Anmeldungsmanager aufgerufen, nachdem die Anmeldungen erfolgreich abgeschlossen sind, die eine gerichtete Kommunikation mit der
	 * Konfiguration gewährleisten. Dadurch signalisiert der Anmeldungsmanager die Bereitschaft, Datensätze zu empfangen, die Konfigurationsdaten benötigen, um
	 * interpretiert zu werden.
	 */
	public final synchronized void setReadyForConfigDependantData() {
		_readyForConfigDependantData = true;
	}

	/**
	 * Setzt den {@link ApplicationCloseActionHandler} dieser Subkomponente. Dieser bestimmt wie die Applikationsfunktion auf einen Kommunikationsfehler reagieren
	 * soll.
	 *
	 * @param closer {@link ApplicationCloseActionHandler}
	 */
	public final void setCloseHandler(ApplicationCloseActionHandler closer) {
		_closer = closer;
	}

	/**
	 * Die Methode erstellt eine logische Verbindung zum Datenverteiler, d. h. die Authentifizierung der Applikation und das Einstellen der Kommunikationsparameter
	 * wird durchgeführt, damit der Austausch von Daten sicher durchgeführt werden kann. Ein {@link AuthentificationTextRequest}-Telegramm mit der
	 * Konfigurations-PID wird über die {@link de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunication Telegrammverwaltung} zum Datenverteiler gesendet, um einen Schlüssel für die
	 * Authentifizierung anzufordern. Auf die Antwort {@link AuthentificationTextAnswer} wird eine gewisse Zeit gewartet (maximale Wartezeit auf synchrone
	 * Antworten). Mit dem vom Datenverteiler erhaltenen Schlüssel wird das Benutzerpasswort durch den {@link de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess} verschlüsselt und als {@link
	 * AuthentificationRequest} zum Datenverteiler gesendet. Auch hier wird eine gewisse Zeit auf die Antwort {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationAnswer} gewartet (maximale
	 * Wartezeit auf synchrone Antworten). Wenn die Authentifizierung erfolgreich ist, werden die Ids der Applikation, des Datenverteilers, der Konfiguration und
	 * des Benutzers übertragen und von dieser Subkomponente festgehalten. Wird als Id der Konfiguration <code>-1</code> zurückgegeben, so war die spezifizierte
	 * PID dem System nicht bekannt. Sonst werden die Keep-alive-Parameter und die Durchsatzprüfungsparameter mit dem Datenverteiler verhandelt. Ein {@link
	 * ComParametersRequest} wird zum Datenverteiler gesendet. Auch hier wird auf die Antwort {@link ComParametersAnswer} eine gewisse Zeit gewartet (maximale
	 * Wartezeit auf synchrone Antworten).
	 *
	 * @throws de.bsvrz.dav.daf.main.InconsistentLoginException Wenn die Authentifizierung nicht erfolgreich abgeschlossen werden konnte.
	 * @throws CommunicationError         Wenn eine Antwort nicht innerhalb einer parametrierten Zeit vom Datenverteiler beantwortet wird oder wenn als Id der
	 *                                    Konfiguration eine <code>-1</code> ermittelt wird.
	 */
	public final void connect() throws InconsistentLoginException, CommunicationError {
		_syncSystemTelegramList.clear();
		// Authentifikationstext holen
		AuthentificationTextAnswer text = getAuthentificationText();
		if(text == null) {
			throw new CommunicationError("Der Datenverteiler antwortet nicht auf die Authentifikationsschlüsselanforderung.\n");
		}
		byte password[] = text.getEncryptedPassword(properties.getAuthentificationProcess(), properties.getUserPassword());

		// User Authentifizierung
		AuthentificationAnswer authentification = authentify(password);
		if(authentification == null) {
			throw new CommunicationError("Der Datenverteiler antwortet nicht auf die Authentifikationsanforderung.\n");
		}
		if(!authentification.isSuccessfullyAuthentified()) {
			throw new InconsistentLoginException("Die Authentifikationsdaten sind fehlerhaft.");
		}
		_userId = authentification.getUserId();
		_applicationId = authentification.getApplicationId();
		_localDVId = authentification.getLocaleDVId();
		_localConfigurationId = authentification.getLocaleConfigurationId();
		if(_localConfigurationId == -1) {
			throw new CommunicationError("Die gegebene 'Konfiguration-PID' ist ungültig.\n");
		}

		// Timeouts Parameter verhandeln
		ComParametersAnswer comParametersAnswer = getComParameters();
		if(comParametersAnswer == null) {
			throw new CommunicationError("Der Datenverteiler antwortet nicht auf die Verhandlung der Kommunikationsparameter. \n");
		}
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.updateKeepAliveParameters(
				comParametersAnswer.getKeepAliveSendTimeOut(), comParametersAnswer.getKeepAliveReceiveTimeOut()
		);
		lowLevelCommunication.updateThroughputParameters(
				(float)comParametersAnswer.getCacheThresholdPercentage() * 0.01f,
				(long)(comParametersAnswer.getFlowControlThresholdTime() * 1000),
				comParametersAnswer.getMinConnectionSpeed()
		);
	}

	/**
	 * Es wird ein {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionTelegram Empfangsanmeldungstelegramm} erstellt und zum Datenverteiler gesendet.
	 *
	 * @param subscription die Empfangsanmeldeinformationen
	 */
	public void sendReceiveSubscription(ReceiveSubscriptionInfo subscription) {
//		System.out.println("sendReceiveSubscription: " + subscription.getBaseSubscriptionInfo().toString());
		ReceiveSubscriptionTelegram receiveSubscriptionTelegram = new ReceiveSubscriptionTelegram(subscription);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(receiveSubscriptionTelegram);
	}

	/**
	 * Es wird ein {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveUnsubscriptionTelegram Empfangsabmeldungstelegramm} erstellt und zum Datenverteiler gesendet.
	 *
	 * @param unsubscription die Empfangsabmeldeinformationen
	 */
	public final void sendReceiveUnsubscription(BaseSubscriptionInfo unsubscription) {
		ReceiveUnsubscriptionTelegram receiveUnsubscriptionTelegram = new ReceiveUnsubscriptionTelegram(unsubscription);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(receiveUnsubscriptionTelegram);
	}

	/**
	 * Es wird ein {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionTelegram Sendeanmeldetelegramm} erstellt und zum Datenverteiler gesendet.
	 *
	 * @param subscription die Sendeanmeldeinformationen
	 */
	public void sendSendSubscription(SendSubscriptionInfo subscription) {
//		System.out.println("sendSendSubscription: " + subscription.getBaseSubscriptionInfo().toString());
		SendSubscriptionTelegram sendSubscriptionTelegram = new SendSubscriptionTelegram(subscription);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(sendSubscriptionTelegram);
	}

	/**
	 * Es wird ein {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendUnsubscriptionTelegram Sendeabmeldetelegramm} erstellt und zum Datenverteiler gesendet.
	 *
	 * @param unsubscription die Sendeabmeldeinformationen
	 */
	public final void sendSendUnsubscription(BaseSubscriptionInfo unsubscription) {
		SendUnsubscriptionTelegram sendUnsubscriptionTelegram = new SendUnsubscriptionTelegram(unsubscription);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(sendUnsubscriptionTelegram);
	}

	/**
	 * Versendet ein Applikationsdatentelegramm an den Datenverteiler. Falls der zu sendende Datensatz größer ist, als die im System gesetzte maximale Länge eines
	 * Telegramms, wird er in Teiltelegramme zerstückelt und zum Datenverteiler gesendet.
	 *
	 * @param dataToSend die zu sendenden Daten als Bytefeld vorbereitet
	 */
	public final void sendData(SendDataObject dataToSend) {
//		System.out.println("dataToSend = " + dataToSend.getDataNumber() );
		ApplicationDataTelegram telegrams[] = TelegramUtility.splitToApplicationTelegrams(dataToSend);
		if(telegrams != null) {
			LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
			lowLevelCommunication.send(telegrams);
		}
	}

	/**
	 * Die bestehende Verbindung zum Datenverteiler wird terminiert, und der Kommunikationskanal wird geschlossen. Wenn der Parameter <code>error</code> gesetzt
	 * ist, wird die close-Methode vom {@link ApplicationCloseActionHandler} aufgerufen.
	 *
	 * @param error   Ist <code>true</code>, wenn die Verbindung im Fehlerfall abgebrochen werden soll, ohne die noch gepufferten Telegramme zu versenden; <code>false</code>, wenn versucht werden soll alle gepufferten Telegramme zu versenden.
	 * @param message Fehlermeldung, die die Fehlersituation näher beschreibt.
	 */
	public synchronized final void terminate(boolean error, String message) {
		final DataTelegram terminationTelegram;
		if(error) {
			terminationTelegram = new TerminateOrderTelegram(message);
		}
		else {
			terminationTelegram = new ClosingTelegram();
		}
		terminate(error, message, terminationTelegram);
	}

	public final void terminate(boolean error, String message, DataTelegram terminationTelegram) {
		try {
			synchronized(this) {
				if(_disconnecting && _disconnectingOnError) {
					return;
				}
				_disconnectingOnError = error;
				_disconnecting = true;
				_sendControlNotifier.close();
				String debugMessage = "Verbindung zum Datenverteiler wird terminiert. Ursache: " + message;
				if(error) {
					_debug.error(debugMessage);
				}
				else {
					_debug.info(debugMessage);
				}
				LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
				if(lowLevelCommunication != null) {
					lowLevelCommunication.disconnect(error, message, terminationTelegram);
				}
			}
		}
		finally {
			notifyConnectionClosed();
		}
		if(error) {
			try {
				ApplicationCloseActionHandler myCloser = null;
				synchronized(this) {
					if(_closer != null) {
						myCloser = _closer;
						_closer = null;
					}
				}
				if(myCloser != null) {
					myCloser.close(message);
				}
			}
			catch(Exception ex) {
				_debug.fine("Fehler beim Aufruf des CloseHandlers", ex);
			}
		}
	}


	public void disconnected(boolean error, final String message) {
		terminate(error, message, null);
	}

	/**
	 * {@inheritDoc} Erhält ein Aktualisierungsdatum vom Datenverteiler. Diese Methode wird von der {@link de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunication} aufgerufen, wenn ein neues
	 * Telegramm angekommen ist. Sie reagiert nur auf die Telegramme, die für die Applikation von Interesse sind.
	 * <p>
	 * Neu ankommende Telegramme werden je nach Typ unterschiedlich weiterverarbeitet:
	 * <p>
	 * Wenn das Telegramm vom Typ {@link AuthentificationTextAnswer}, {@link AuthentificationAnswer}, {@link ComParametersAnswer}, {@link ProtocolVersionAnswer}
	 * oder {@link TelegramTimeAnswer} ist, wird es in eine Liste eingefügt und eine Broadcast-Nachricht an alle wartenden Methoden gesendet. Diese überprüfen ob
	 * die Nachricht für sie relevant ist. In diesem Falle wird sie aus der Liste entfernt und bearbeitet.<br/> Wenn das Telegramm vom Typ {@link
	 * de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeRequest} ist, wird anhand der übergebenen gemessene Zeit ein {@link TelegramTimeAnswer} gebildet und zum Datenverteiler gesendet. <br/> Wenn das
	 * Telegramm vom Typ {@link RequestSenderDataTelegram} ist, wird die Methode {@link SubscriptionManager#notifySenderApplication(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo,
	 *byte) notifySenderApplication} des Anmeldungsmanagers aufgerufen, um die Applikation zu benachrichtigen. <br/> Wenn das Telegramm vom Typ {@link
	 * ApplicationDataTelegram} ist, wird zunächst überprüft, ob ein zerstückeltes Telegramm vorliegt. Ist dies der Fall, dann wird es in eine Liste eingefügt und
	 * überprüft ob alle Teiltelegramme vorhanden sind. Sind alle vorhanden, wird aus den Telegrammen ein {@link SendDataObject} erzeugt, das weiterverarbeitet
	 * werden kann. Wenn das Telegramm nicht zerstückelt ist, wird es sofort in ein {@link SendDataObject} zur Weiterbearbeitung umgewandelt. Wenn dieses
	 * erfolgreich erzeugt wurde, wird anhand seiner Basisanmeldeinformation überprüft, ob es eine Antwort einer Konfigurationsanfrage ist. Ist dies der Fall, so
	 * wird das Telegramm der {@link de.bsvrz.dav.daf.main.impl.ConfigurationManager#update(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject) update}-Methode des
	 * Konfigurations-Managers übergeben, wenn nicht, handelt es sich um ein Online-Telegramm und es wird an die {@link
	 * CacheManager#update(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject) update}-Methode des Cache-Managers übergeben. <br/>Wenn das Telegramm vom Typ {@link
	 * de.bsvrz.dav.daf.communication.lowLevel.telegrams.ClosingTelegram} oder {@link TerminateOrderTelegram} ist, wird die Methode {@link #terminate} aufgerufen.
	 * <p>
	 * Jeder andere Telegrammtyp wird ignoriert.
	 */
	public final void update(DataTelegram telegram) throws InterruptedException {
		if(telegram == null) {
			return;
		}
		switch(telegram.getType()) {
			case DataTelegram.PROTOCOL_VERSION_ANSWER_TYPE:
			case DataTelegram.AUTHENTIFICATION_TEXT_ANSWER_TYPE:
			case DataTelegram.AUTHENTIFICATION_ANSWER_TYPE:
			case DataTelegram.COM_PARAMETER_ANSWER_TYPE:
			case DataTelegram.TELEGRAM_TIME_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TELEGRAM_TIME_REQUEST_TYPE: {
				TelegramTimeRequest telegramTimeRequest = (TelegramTimeRequest)telegram;
				LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
				lowLevelCommunication.send(new TelegramTimeAnswer(telegramTimeRequest.getTelegramRequestTime()));
				break;
			}
			case DataTelegram.REQUEST_SENDER_DATA_TYPE: {
				RequestSenderDataTelegram requestSenderDataTelegram = (RequestSenderDataTelegram)telegram;
				_sendControlNotifier.put(requestSenderDataTelegram);
				break;
			}
			case DataTelegram.APPLICATION_DATA_TELEGRAM_TYPE: {
				ApplicationDataTelegram applicationDataTelegram = (ApplicationDataTelegram)telegram;
				SendDataObject receivedData = null;
				int maxTelegramNumber = applicationDataTelegram.getTotalTelegramsCount();
				// prüfen, ob das Telegram in einem Stück gesendet werden kann
				if(maxTelegramNumber == 1) {
					receivedData = TelegramUtility.getSendDataObject(applicationDataTelegram);
				}
				else {
					// das Telegramm wird in Teilstücke zerlegt
					final ApplicationDataTelegram telegramArray[] = splittedTelegramsTable.put(applicationDataTelegram);
					if(telegramArray != null) {
						receivedData = TelegramUtility.getSendDataObject(telegramArray);
					}
				}
				if(receivedData != null) {
					BaseSubscriptionInfo baseSubscriptionInfo = receivedData.getBaseSubscriptionInfo();
					if(AttributeGroupUsageIdentifications.isConfigurationReply(baseSubscriptionInfo.getUsageIdentification())) {
						// Antworten der Konfiguration werden direkt an den Konfigurationsmanager weitergeleitet.
						_configurationManager.update(receivedData);
						for(final ConfigurationManager configurationManager : _subscriptionManager.getSecondaryConfigurationManagers()) {
							configurationManager.update(receivedData);
						}
					}
					else {
						// Online-Datensätze
						if(_readyForConfigDependantData) {
							_cacheManager.update(receivedData);
						}
						else {
							_debug.error(
									"Empfangener Datensatz konnte nicht verarbeitet werden, weil die Initialisierung noch nicht abgeschlossen wurde. Anmeldeinfo",
									baseSubscriptionInfo
							);
						}
					}
				}
				break;
			}
			case DataTelegram.TERMINATE_ORDER_TYPE: {
				TerminateOrderTelegram terminateOrderTelegram = (TerminateOrderTelegram)telegram;
				terminate(true, "Verbindung wurde vom Datenverteiler terminiert. Ursache: " + terminateOrderTelegram.getCause(), null);
				break;
			}
			case DataTelegram.CLOSING_TYPE: {
				terminate(true, "Verbindung wurde vom Datenverteiler geschlossen", null);
				break;
			}
			case DataTelegram.KEEP_ALIVE_TYPE: {
				break;
			}
			default: {
				break;
			}
		}
	}

	public final void updateConfigData(SendDataObject receivedData) {
		if(_configurationManager != null) {
			_configurationManager.update(receivedData);
		}
		for(final ConfigurationManager configurationManager : _subscriptionManager.getSecondaryConfigurationManagers()) {
			configurationManager.update(receivedData);
		}
	}

	/**
	 * Diese Methode gibt die Eigenschaften dieser Verbindung zurück.
	 *
	 * @return die Eigenschaften dieser Verbindung
	 */
	public final ClientConnectionProperties getConnectionProperties() {
		return properties;
	}

	/**
	 * Schickt ein Protokollversions-Telegramm zum Datenverteiler und wartet auf seine Antwort.
	 *
	 * @return Die Protokollversion-Telegramm-Antwort des Datenverteilers oder <code>null</code>, falls die Antwort nicht ermittelt werden konnte.
	 */
	private ProtocolVersionAnswer getProtocolVersions() {
		int versions[] = {_dafVersion};
		ProtocolVersionRequest protocolVersionRequest = new ProtocolVersionRequest(versions);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(protocolVersionRequest);

		return (ProtocolVersionAnswer)getDataTelegram(CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE, DataTelegram.PROTOCOL_VERSION_ANSWER_TYPE);
	}

	/**
	 * Diese Methode dient dem reduzieren von doppeltem Code. Aus einer Telegrammliste wird das gewünschte Telegramm herausgesucht und zurückgegeben.
	 *
	 * @param maximumWaitingTime die gewünschte maximale Wartezeit
	 * @param telegramType          den gewünschten Telegramm-Typ
	 *
	 * @return das gesuchte Telegramm
	 */
	private DataTelegram getDataTelegram(final long maximumWaitingTime, byte telegramType) {
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < maximumWaitingTime) {
			try {
				synchronized(_syncSystemTelegramList) {
					if(_disconnecting) throw new RuntimeException("Verbindung zum Datenverteiler wurde unterbrochen");
					_syncSystemTelegramList.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					DataTelegram telegram = null;
					ListIterator iterator = _syncSystemTelegramList.listIterator(0);
					while(iterator.hasNext()) {
						telegram = (DataTelegram)iterator.next();
						if(telegram != null) {     // in der LinkedList können theoretisch null-Objekte vorkommen
							if(telegram.getType() == telegramType) {
								iterator.remove();
								return telegram;
							}
							else {
								System.out.println(telegram.parseToString());
							}
						}
					} // while
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				_debug.warning("Thread wurde unterbrochen", ex);
				break;
			}
		} // while
		return null;
	}

	/**
	 * Schickt ein AuthentifikationsText-Telegramm zum Datenverteiler und wartet auf seine Antwort.
	 *
	 * @return Die AuthentifikationsText-Telegramm-Antwort des Datenverteilers oder <code>null</code>, falls sie nicht ermittelt werden konnte.
	 */
	private AuthentificationTextAnswer getAuthentificationText() {
		String applicationName = properties.getApplicationName();
		if(applicationName == null) {
			throw new IllegalArgumentException("Applikationsname ist null");
//			return null;
		}
		String incarnationName = properties.getIncarnationName();
		final String applicationIncarnationName;
		// Name für das vom Datenverteiler zu erzeugende Objekt wird aus Applikationsname und Inkarnationsname gebildet.
		// Dabei wird ": " zur Trennung benutzt, wenn der Inkarnationsname nicht leer ist
		if(incarnationName.length() > 0) {
			applicationIncarnationName = applicationName + ": " + incarnationName;
		}
		else {
			applicationIncarnationName = applicationName;
		}
		String applicationTypePid = properties.getApplicationTypePid();
		if(applicationTypePid == null) {
			throw new IllegalArgumentException("Applikationstyp ist null");
//			return null;
		}
		String configurationPid = properties.getConfigurationPid();
		if(configurationPid == null) {
			throw new IllegalArgumentException("Konfigurationspid ist null");
//			return null;
		}
		if(configurationPid.equals(CommunicationConstant.LOCALE_CONFIGURATION_PID_ALIASE)) {
			configurationPid = "";
		}
		AuthentificationTextRequest authentificationTextRequest = new AuthentificationTextRequest(
				applicationIncarnationName, applicationTypePid, configurationPid
		);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		if(!DO_NOT_SEND_AUTH_REQUEST_FOR_TESTS) {
			lowLevelCommunication.send(authentificationTextRequest);
		}

		return (AuthentificationTextAnswer)getDataTelegram(
				CommunicationConstant.MAX_WAITING_TIME_FOR_CONNECTION, DataTelegram.AUTHENTIFICATION_TEXT_ANSWER_TYPE
		);
	}


	/**
	 * Schickt ein Authentifikations-Telegramm zum Datenverteiler und wartet auf seine Antwort.
	 *
	 * @param encryptedUserPassword das verschlüsselte Passwort des Benutzers
	 *
	 * @return Die Authentifikations-Telegramm-Antwort des Datenverteilers oder <code>null</code>, falls die Antwort nicht ermittelt werden konnte.
	 */
	private AuthentificationAnswer authentify(byte encryptedUserPassword[]) {
		String _authentificationProcessName = properties.getAuthentificationProcess().getName();
		if(_authentificationProcessName == null) {
			return null;
		}
		String _userName = properties.getUserName();
		if(_userName == null) {
			return null;
		}
		AuthentificationRequest authentificationRequest = new AuthentificationRequest(
				_authentificationProcessName, _userName, encryptedUserPassword
		);

		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(authentificationRequest);

		return (AuthentificationAnswer)getDataTelegram(CommunicationConstant.MAX_WAITING_TIME_FOR_CONNECTION, DataTelegram.AUTHENTIFICATION_ANSWER_TYPE);
	}

	/**
	 * Schickt ein ComParameter-Telegramm zum Datenverteiler und wartet auf seine Antwort.
	 *
	 * @return Die ComParameter-Telegramm-Antwort des Datenverteilers oder <code>null</code>, falls die Antwort nicht ermittelt werden konnte.
	 */
	private ComParametersAnswer getComParameters() {
		ComParametersRequest comParametersRequest = new ComParametersRequest(
				properties.getKeepAliveSendTimeOut(),
				properties.getKeepAliveReceiveTimeOut(),
				(byte)(properties.getCommunicationParameters().getThroughputControlSendBufferFactor() * 100),
				(short)(properties.getCommunicationParameters().getThroughputControlInterval() * 0.001),
				// in Sekunden
				properties.getCommunicationParameters().getMinimumThroughput()
		);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(comParametersRequest);

		return (ComParametersAnswer)getDataTelegram(CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE, DataTelegram.COM_PARAMETER_ANSWER_TYPE);
	}

	/**
	 * Schickt ein Laufzeitermittlungs-Telegramm zum Datenverteiler und wartet auf seine Antwort, um herauszubekommen, wie viel Zeit die Übertragung in Anspruch
	 * nimmt.
	 *
	 * @param maxWaitingTime Zeit in Millisekunden, die maximal auf eine Antwort gewartet wird.
	 * @return Die Zeit, die benötigt wurde, um ein Telegramm von der Applikation zum Datenverteiler und zurück zu senden oder <code>-1</code> falls innerhalb
	 *         der angegebenen Timeout-Zeit keine Antwort empfangen wurde.
	 *
	 * @throws CommunicationError Falls die Verbindung zum Datenverteiler gestört ist.
	 */
	public final long getTelegramTime(long maxWaitingTime) throws CommunicationError {
		if(_disconnecting) {
			throw new CommunicationError("Verbindung zum Datenverteiler wurde geschlossen.");
		}
		long time = System.currentTimeMillis();
		TelegramTimeRequest telegramTimeRequest = new TelegramTimeRequest(time);
		LowLevelCommunicationInterface lowLevelCommunication = properties.getLowLevelCommunication();
		lowLevelCommunication.send(telegramTimeRequest);

		TelegramTimeAnswer telegramTimeAnswer = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < maxWaitingTime) {
			try {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					DataTelegram telegram = null;
					ListIterator _iterator = _syncSystemTelegramList.listIterator(0);
					while(_iterator.hasNext()) {
						telegram = (DataTelegram)_iterator.next();
						if(telegram != null) {
							if(telegram.getType() == DataTelegram.TELEGRAM_TIME_ANSWER_TYPE) {
								if(((TelegramTimeAnswer)telegram).getTelegramStartTime() == time) {
									telegramTimeAnswer = (TelegramTimeAnswer)telegram;
									_iterator.remove();
									break;
								}
							}
							else {
								System.out.println(telegram.parseToString());
							}
						}
					}
					if(telegramTimeAnswer != null) {
						break;
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				throw new CommunicationError("Thread wurde unterbrochen.", ex);
			}
		}
		if(telegramTimeAnswer == null) {
			return -1;
		}
		return telegramTimeAnswer.getRoundTripTime();
	}

	/**
	 * Meldet einen Beobachter für Zustandsänderungen der Datenverteilerverbindung an.
	 *
	 * @param connectionListener Beobachter für Zustandsänderungen der Datenverteilerverbindung
	 */
	public synchronized void setConnectionListener(DavConnectionListener connectionListener) {
		_connectionListener = connectionListener;
	}

	/** Wird die Verbindung zum Datenverteiler terminiert, so wird dem Beobachter, der sich dafür interessiert, dieses mitgeteilt. */
	private void notifyConnectionClosed() {
		final DavConnectionListener connectionListener;
		synchronized(this) {
			connectionListener = _connectionListener;
			_connectionListener = null;
		}
		if(connectionListener != null) {
			try {
				connectionListener.connectionClosed(null);
			}
			catch(Exception e) {
				_debug.warning("Fehler beim Verarbeiten der connectionClosed-Meldung", e);
			}
		}
	}

	/**
	 * Gibt die temporäre Liste der Systemtelegramme für interne Synchronisationszwecke zurück.
	 *
	 * @return die Liste der Systemtelegramme
	 */
	public List<DataTelegram> getSyncSystemTelegramList() {
		return _syncSystemTelegramList;
	}
}
