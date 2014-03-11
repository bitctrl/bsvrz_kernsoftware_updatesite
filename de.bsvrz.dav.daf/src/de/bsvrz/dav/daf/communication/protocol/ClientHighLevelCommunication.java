/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.communication.protocol;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.HighLevelCommunicationCallbackInterface;
import de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunicationInterface;
import de.bsvrz.dav.daf.communication.lowLevel.SplittedApplicationTelegramsTable;
import de.bsvrz.dav.daf.communication.lowLevel.TelegramUtility;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationTextAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationTextRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ClosingTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ComParametersAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ComParametersRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveUnsubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.RequestSenderDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendUnsubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TerminateOrderTelegram;
import de.bsvrz.dav.daf.main.ApplicationCloseActionHandler;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.impl.CacheManager;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.ConfigurationManager;
import de.bsvrz.dav.daf.main.impl.SubscriptionManager;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Das Modul Protokollsteuerung ist das Bindeglied der Komponente Kommunikation zwischen den Modulen Telegrammverwaltung und Verwaltung. Es stellt f�r die
 * Interaktion mit dem Datenverteiler eine Funktionsschnittstelle zur Verf�gung, die die technischen Aspekte der Kommunikation gegen�ber der Verwaltung kapselt
 * und implementiert die Abbildung auf Telegramme und Kommunikationsabl�ufe. Die Protokollsteuerung grenzt sich zur Telegrammverwaltung ab, weil hier die aus
 * Sicht der Datenverteiler-Applikationsfunktionen speziellen und nicht wiederverwendbaren Funktionen der Kommunikation mit dem Datenverteiler enthalten sind.
 * Folgende Funktionen und Abl�ufe werden in der Protokollsteuerung implementiert: <ul> <li>Initialisierung und Verhandlung der Protokollversion. </li>Mit Hilfe
 * der Telegrammverwaltung wird die TCP-Verbindung zum Datenverteiler hergestellt. Anschlie�end wird dem Datenverteiler in einem
 * Protokollversionsanfrage-Telegramm mitgeteilt, welche Versionen des Kommunikationsprotokolls unterst�tzt werden. Als Reaktion wird vom Datenverteiler ein
 * Protokollversionsantwort-Telegramm erwartet in dem die zu verwendende Version des Protokolls enthalten ist. <li>Authentifizierung gegen�ber dem
 * Datenverteiler und Verhandlung der Verbindungsparameter. </li>Zur Authentifizierung wird ein Authentifizierungsbereitschafts-Telegramm �bertragen. Als
 * Antwort wird ein vom Datenverteiler generierter Zufallstext im Authentifizierungsaufforderungs-Telegramm erwartet. Dieser Text wird durch das Modul
 * HMAC-MD5-Verschl�sselung verschl�sselt. Als geheimer Schl�ssel dient dabei das Passwort des Benutzers. Der verschl�sselte Text wird mit weiteren
 * Informationen zum Datenverteiler �bertragen. Als Antwort wird vom Datenverteiler ein Authentifizierungsannahme-Telegramm oder ein
 * Authentifizierungsablehnungs-Telegramm erwartet. Im Falle einer Authentifizierungsablehnung wird der Authentifizierungsvorgang abgebrochen und kann bei
 * Bedarf von der Verwaltung mit anderen Authentifizierungsinformationen wiederholt initiiert werden. Im Falle einer Authentifizierungsannahme werden
 * anschlie�end die Verbindungsparameter verhandelt. Dazu sendet die Protokollsteuerung ein Verbindungsparameteranfrage-Telegramm an den Datenverteiler und
 * erwartet ein Verbindungsparameterantwort-Telegramm mit der Festlegung der Verbindungsparameter vom Datenverteiler. <li>Abbruch der Verbindung</li> Diese
 * Funktion f�hrt zum sofortigen Abbruch der Verbindung zum Datenverteiler mit Hilfe der Abbruch-Funktion der Telegrammverwaltung. <li>Terminierung der
 * Verbindung</li>Zur Terminierung der Verbindung zum Datenverteiler wird ein Applikationsabmeldungs-Telegramm �bertragen und die Kommunikationsverbindung
 * terminiert. Dabei wird durch die Telegrammverwaltung sichergestellt, dass alle noch im Sendepuffer befindliche Telegramme vor dem Schlie�en der
 * Kommunikationsverbindung zum Datenverteiler �bertragen werden. <li>Parametrierung des Datenverteilers</li>Zur An- bzw. Abmeldung von zu empfangenden bzw. zu
 * sendenden Daten wird ein entsprechendes Telegramm (Empfangsanmeldung, Sendeanmeldung, Empfangsabmeldung, Sendeabmeldung) an den Datenverteiler �bertragen.
 * <li>Entgegennahme von Sendesteuerungs-Telegrammen und Weiterleitung der enthaltenen Information (ob Daten, die von der Applikation zum Senden angemeldet
 * wurden, vom Datenverteiler ben�tigt werden oder nicht) an die Verwaltung. </li> <li>Senden von Anwendungsdaten</li>Zu sendende Datenbl�cke werden in
 * Telegramme zerlegt und an die Telegrammverwaltung weitergeleitet.<li>Empfang von Anwendungsdaten</li>Von der Telegrammverwaltung entgegengenommene Telegramme
 * werden zu Datenbl�cken rekombiniert und an das Modul Verwaltung weitergeleitet.<li>Zerlegung von gro�en Datenbl�cken in mehrere Datentelegramme beim Senden
 * und Rekombination von zusammengeh�rigen Datentelegrammen zu einem Datenblock beim Empfang.</li><li>Telegrammlaufzeitermittlung</li>Zur Messung von
 * Telegrammlaufzeiten wird ein Telegrammlaufzeitanfrage-Telegramm mit der aktuellen Systemzeit an den Datenverteiler �bertragen. Als Antwort wird ein
 * Telegrammlaufzeitantwort-Telegramm erwartet, in dem die urspr�nglich �bertragene Systemzeit enthalten ist. Durch Vergleich der Zeit zwischen dem Absenden des
 * Anfrage-Telegramms und dem Erhalt der Antwort wird die Laufzeit f�r Hin- und R�ckweg zusammen ermittelt. Andererseits kann auch vom Datenverteiler eine
 * solche Messung durchgef�hrt werden, d.h. bei Empfang eines Telegrammlaufzeitanfrage-Telegramms wird die enthaltene Systemzeit in einem
 * Telegrammlaufzeitantwort-Telegramm zur�ck �bertragen.<li>Fehlerbehandlung</li>Fehlerzust�nde, die von der Protokollsteuerung erkannt werden oder von der
 * Telegrammverwaltung gemeldet werden, f�hren zu einem Abbruch der Verbindung und zu einer Benachrichtigung des Moduls Verwaltung.</ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10161 $
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

	/** Tempor�re Liste der Systemtelegramme f�r interne Synchronisationszwecke. */
	private final List<DataTelegram> _syncSystemTelegramList;

	/** Tempor�re Liste der zerst�ckelten Telegramme. */
	private SplittedApplicationTelegramsTable splittedTelegramsTable;

	/** H�lt die Informationen �ber den Initialisierungszustand dieser Komponente. */
	private boolean _readyForConfigDependantData;

	/** Wird beim ersten Aufruf von terminate(...) gesetzt. */
	private volatile boolean _disconnecting = false;

	/** Wird beim ersten Aufruf von terminate(true, ...) gesetzt. */
	private volatile boolean _disconnectingOnError = false;

	/** Das Objekt, das f�r das Schliessen nach Auftritt eines Fehlers zust�ndig ist. */
	private ApplicationCloseActionHandler _closer;

	/** Die Kommunikationseingenschaften */
	private ClientConnectionProperties properties;

	/** Beobachter f�r Zustands�nderungen der Datenverteilerverbindung. */
	private DavConnectionListener _connectionListener;

	/** Asynchrone Verarbeitung von empfangenen Sendsteuerungstelegrammen */
	private ClientHighLevelCommunication.SendControlNotifier _sendControlNotifier;

	/**
	 * Dieser Konstruktor erzeugt eine Instanz dieser Klasse mit den �bergebenen Parametern. {@link de.bsvrz.dav.daf.main.ClientDavParameters} enth�lt die Adresse
	 * und Subadresse des Datenverteilers, spezifiziert das zu verwendende Protokoll durch dessen Namen. {@link de.bsvrz.dav.daf.main.ApplicationCloseActionHandler}
	 * bestimmt wie die Applikationsfunktion auf einen Kommunikationsfehler reagieren soll.
	 * <p/>
	 * {@link ClientConnectionProperties} werden erzeugt, die die Eigenschaften dieser Verbindung verk�rpern. Sie spezifizieren insbesondere
	 * den {@link de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess AuthentificationProcess}, das f�r die Passwortverschl�sselung zust�ndige Verfahren. Weiter enthalten
	 * sie die PID der Konfiguration.
	 * <p/>
	 * Ein Systemtelegramm {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionRequest} wird gebildet und zum Datenverteiler gesendet. Es enth�lt Angaben �ber die
	 * unterst�tzte Protokollversion. Auf die Antwort wird eine festgelegte Zeit gewartet (maximale Wartezeit auf synchrone Antworten). Wenn die Antwort nicht
	 * innerhalb diese Zeit an-gekommen ist oder die Protokollversion vom Datenverteiler nicht unterst�tzt wird, dann wird eine CommunicationError-Ausnahme
	 * erzeugt.
	 *
	 * @param clientDavParameters Parameterklasse f�r die Datenverteiler-Applikationsfunktionen
	 * @param closer              bestimmt, wie die Applikationsfunktionen auf einen Kommunikationsfehler reagieren soll
	 *
	 * @throws CommunicationError  Wenn die Verhandlung der Protokollversion nicht durchgef�hrt wurde oder die Protokollversion nicht unterst�tzt wird.
	 * @throws ConnectionException Wenn die Verbindung �ber die Telegrammverwaltung fehlschl�gt.
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
				errorMessage = "Die lokale Protokollversion (" + _dafVersion + ") wird vom Datenverteiler nicht unterst�tzt.";
			}
		}
		if(errorMessage != null) {
			lowLevelCommunication.disconnect(true, errorMessage, null);
			throw new CommunicationError(errorMessage);
		}
	}

	/**
	 * Asynchrone Verarbeitung von empfangenen Sendsteuerungstelegrammen. Speichert mit put �bergebene Sendesteuerungstelegramme in einer internen Queue, aus der
	 * die Telegramme asynchron von einem eigenen Thread ausgelesen und zur Weiterverarbeitung an den SubscriptionsManager weitergegeben werden.
	 */
	private class SendControlNotifier implements Runnable {

		private UnboundedQueue<RequestSenderDataTelegram> _telegrams;

		/** Der Konstruktor erzeugt die interne Queue und den Thread zur Verarbeitung der Telegramme. */
		public SendControlNotifier() {
			_telegrams = new UnboundedQueue<RequestSenderDataTelegram>();
			final Thread thread = new Thread(this, "SendControlNotifier");
			thread.setDaemon(true);
			thread.start();
		}

		/**
		 * Speichert das �bergebene Telegramm in der Queue zur asynchronen Verarbeitung.
		 *
		 * @param telegram Zu verarbeitendes Sendesteuerungstelegramm.
		 */
		public void put(RequestSenderDataTelegram telegram) {
			_telegrams.put(telegram);
		}

		/** Signalisiert dem Thread zur Verarbeitung der Telegramme, dass keine weiteren Telegramme verarbeitet werden m�ssen und der Thread sich beenden kann. */
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

	/** Dieser Konstruktor wird f�r JUnit-Tests gebraucht. */
	public ClientHighLevelCommunication() {
		// Variablen setzen, die final sind
		_syncSystemTelegramList = new LinkedList<DataTelegram>();
	}

	/**
	 * Schlie�t die Initialisierung ab. Diese Methode wird von ClientDavConnection aufgerufen. Es werden Referenzen auf den Konfigurationsmanager, den
	 * Cache-Manager und den Anmeldungsmanager festgehalten. Zus�tzlich wird eine Referenz auf ClientHighLevelCommunication an den Anmeldungsmanager �bergeben.
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
	 * Gibt die Id der Applikation zur�ck.
	 *
	 * @return die Id der Applikation
	 */
	public long getApplicationId() {
		return _applicationId;
	}

	/**
	 * Gibt die Id des Datenverteilers zur�ck.
	 *
	 * @return die Id des Datenverteilers
	 */
	public final long getDataTransmitterId() {
		return _localDVId;
	}

	/**
	 * Gibt die Id der Konfiguration zur�ck.
	 *
	 * @return die ID der Konfiguration
	 */
	public final long getConfigurationId() {
		return _localConfigurationId;
	}

	/**
	 * Gibt die Id des Benutzers zur�ck.
	 *
	 * @return die ID des Benutzers
	 */
	public final long getUserId() {
		return _userId;
	}

	/**
	 * Diese Methode wird vom Anmeldungsmanager aufgerufen, nachdem die Anmeldungen erfolgreich abgeschlossen sind, die eine gerichtete Kommunikation mit der
	 * Konfiguration gew�hrleisten. Dadurch signalisiert der Anmeldungsmanager die Bereitschaft, Datens�tze zu empfangen, die Konfigurationsdaten ben�tigen, um
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
	 * wird durchgef�hrt, damit der Austausch von Daten sicher durchgef�hrt werden kann. Ein {@link AuthentificationTextRequest}-Telegramm mit der
	 * Konfigurations-PID wird �ber die {@link de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunication Telegrammverwaltung} zum Datenverteiler gesendet, um einen Schl�ssel f�r die
	 * Authentifizierung anzufordern. Auf die Antwort {@link AuthentificationTextAnswer} wird eine gewisse Zeit gewartet (maximale Wartezeit auf synchrone
	 * Antworten). Mit dem vom Datenverteiler erhaltenen Schl�ssel wird das Benutzerpasswort durch den {@link de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess} verschl�sselt und als {@link
	 * AuthentificationRequest} zum Datenverteiler gesendet. Auch hier wird eine gewisse Zeit auf die Antwort {@link de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationAnswer} gewartet (maximale
	 * Wartezeit auf synchrone Antworten). Wenn die Authentifizierung erfolgreich ist, werden die Ids der Applikation, des Datenverteilers, der Konfiguration und
	 * des Benutzers �bertragen und von dieser Subkomponente festgehalten. Wird als Id der Konfiguration <code>-1</code> zur�ckgegeben, so war die spezifizierte
	 * PID dem System nicht bekannt. Sonst werden die Keep-alive-Parameter und die Durchsatzpr�fungsparameter mit dem Datenverteiler verhandelt. Ein {@link
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
			throw new CommunicationError("Der Datenverteiler antwortet nicht auf die Authentifikationsschl�sselanforderung.\n");
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
			throw new CommunicationError("Die gegebene 'Konfiguration-PID' ist ung�ltig.\n");
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
	 * Versendet ein Applikationsdatentelegramm an den Datenverteiler. Falls der zu sendende Datensatz gr��er ist, als die im System gesetzte maximale L�nge eines
	 * Telegramms, wird er in Teiltelegramme zerst�ckelt und zum Datenverteiler gesendet.
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
	 * @param message Fehlermeldung, die die Fehlersituation n�her beschreibt.
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
				myCloser.close(message);
			}
			catch(Exception ex) {
				return;
			}
		}
	}


	public void disconnected(boolean error, final String message) {
		terminate(error, message, null);
	}

	/**
	 * {@inheritDoc} Erh�lt ein Aktualisierungsdatum vom Datenverteiler. Diese Methode wird von der {@link de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunication} aufgerufen, wenn ein neues
	 * Telegramm angekommen ist. Sie reagiert nur auf die Telegramme, die f�r die Applikation von Interesse sind.
	 * <p/>
	 * Neu ankommende Telegramme werden je nach Typ unterschiedlich weiterverarbeitet:
	 * <p/>
	 * Wenn das Telegramm vom Typ {@link AuthentificationTextAnswer}, {@link AuthentificationAnswer}, {@link ComParametersAnswer}, {@link ProtocolVersionAnswer}
	 * oder {@link TelegramTimeAnswer} ist, wird es in eine Liste eingef�gt und eine Broadcast-Nachricht an alle wartenden Methoden gesendet. Diese �berpr�fen ob
	 * die Nachricht f�r sie relevant ist. In diesem Falle wird sie aus der Liste entfernt und bearbeitet.<br/> Wenn das Telegramm vom Typ {@link
	 * de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeRequest} ist, wird anhand der �bergebenen gemessene Zeit ein {@link TelegramTimeAnswer} gebildet und zum Datenverteiler gesendet. <br/> Wenn das
	 * Telegramm vom Typ {@link RequestSenderDataTelegram} ist, wird die Methode {@link SubscriptionManager#notifySenderApplication(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo,
	 *byte) notifySenderApplication} des Anmeldungsmanagers aufgerufen, um die Applikation zu benachrichtigen. <br/> Wenn das Telegramm vom Typ {@link
	 * ApplicationDataTelegram} ist, wird zun�chst �berpr�ft, ob ein zerst�ckeltes Telegramm vorliegt. Ist dies der Fall, dann wird es in eine Liste eingef�gt und
	 * �berpr�ft ob alle Teiltelegramme vorhanden sind. Sind alle vorhanden, wird aus den Telegrammen ein {@link SendDataObject} erzeugt, das weiterverarbeitet
	 * werden kann. Wenn das Telegramm nicht zerst�ckelt ist, wird es sofort in ein {@link SendDataObject} zur Weiterbearbeitung umgewandelt. Wenn dieses
	 * erfolgreich erzeugt wurde, wird anhand seiner Basisanmeldeinformation �berpr�ft, ob es eine Antwort einer Konfigurationsanfrage ist. Ist dies der Fall, so
	 * wird das Telegramm der {@link de.bsvrz.dav.daf.main.impl.ConfigurationManager#update(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject) update}-Methode des
	 * Konfigurations-Managers �bergeben, wenn nicht, handelt es sich um ein Online-Telegramm und es wird an die {@link
	 * CacheManager#update(de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject) update}-Methode des Cache-Managers �bergeben. <br/>Wenn das Telegramm vom Typ {@link
	 * de.bsvrz.dav.daf.communication.lowLevel.telegrams.ClosingTelegram} oder {@link TerminateOrderTelegram} ist, wird die Methode {@link #terminate} aufgerufen.
	 * <p/>
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
				// pr�fen, ob das Telegram in einem St�ck gesendet werden kann
				if(maxTelegramNumber == 1) {
					receivedData = TelegramUtility.getSendDataObject(applicationDataTelegram);
				}
				else {
					// das Telegramm wird in Teilst�cke zerlegt
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
						// Online-Datens�tze
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
	 * Diese Methode gibt die Eigenschaften dieser Verbindung zur�ck.
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
	 * Diese Methode dient dem reduzieren von doppeltem Code. Aus einer Telegrammliste wird das gew�nschte Telegramm herausgesucht und zur�ckgegeben.
	 *
	 * @param maximumWaitingTime die gew�nschte maximale Wartezeit
	 * @param telegramType          den gew�nschten Telegramm-Typ
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
						if(telegram != null) {     // in der LinkedList k�nnen theoretisch null-Objekte vorkommen
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
		// Name f�r das vom Datenverteiler zu erzeugende Objekt wird aus Applikationsname und Inkarnationsname gebildet.
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
		lowLevelCommunication.send(authentificationTextRequest);

		return (AuthentificationTextAnswer)getDataTelegram(
				CommunicationConstant.MAX_WAITING_TIME_FOR_CONNECTION, DataTelegram.AUTHENTIFICATION_TEXT_ANSWER_TYPE
		);
	}


	/**
	 * Schickt ein Authentifikations-Telegramm zum Datenverteiler und wartet auf seine Antwort.
	 *
	 * @param encryptedUserPassword das verschl�sselte Passwort des Benutzers
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
	 * Schickt ein Laufzeitermittlungs-Telegramm zum Datenverteiler und wartet auf seine Antwort, um herauszubekommen, wie viel Zeit die �bertragung in Anspruch
	 * nimmt.
	 *
	 * @param maxWaitingTime Zeit in Millisekunden, die maximal auf eine Antwort gewartet wird.
	 * @return Die Zeit, die ben�tigt wurde, um ein Telegramm von der Applikation zum Datenverteiler und zur�ck zu senden oder <code>-1</code> falls innerhalb
	 *         der angegebenen Timeout-Zeit keine Antwort empfangen wurde.
	 *
	 * @throws CommunicationError Falls die Verbindung zum Datenverteiler gest�rt ist.
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
	 * Meldet einen Beobachter f�r Zustands�nderungen der Datenverteilerverbindung an.
	 *
	 * @param connectionListener Beobachter f�r Zustands�nderungen der Datenverteilerverbindung
	 */
	public synchronized void setConnectionListener(DavConnectionListener connectionListener) {
		_connectionListener = connectionListener;
	}

	/** Wird die Verbindung zum Datenverteiler terminiert, so wird dem Beobachter, der sich daf�r interessiert, dieses mitgeteilt. */
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
	 * Gibt die tempor�re Liste der Systemtelegramme f�r interne Synchronisationszwecke zur�ck.
	 *
	 * @return die Liste der Systemtelegramme
	 */
	public List<DataTelegram> getSyncSystemTelegramList() {
		return _syncSystemTelegramList;
	}
}
