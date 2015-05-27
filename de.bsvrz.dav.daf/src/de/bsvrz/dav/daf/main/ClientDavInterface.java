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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.archive.ArchiveRequestManager;
import de.bsvrz.dav.daf.main.config.*;

import java.util.Collection;

/**
 * Schnittstellenklasse, die die logische Verbindung zum Datenverteiler repr�sentiert. �ber ein Objekt dieser Klasse kann die Verbindung zum Datenverteiler
 * aufgenommen und wieder terminiert, sowie Daten an- und abgemeldet und gelesen und geschrieben werden. Au�erdem wird �ber die logische Verbindung der Zugriff
 * auf spezielle System-Objekte erm�glicht.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 13003 $
 */
public interface ClientDavInterface {
	
	/**
	 * Initialisiert einen Kommunikationskanal zum Datenverteiler mit den in den {@link ClientDavParameters Verbindungsparametern} angegebenen
	 * Addressinformationen. Nach dem Aufbau der physischen Verbindung wird die in der Kommunikation mit dem Datenverteiler zu verwendende Protokollversion
	 * verhandelt und die logische Verbindung in den Zustand <code>Initialisiert</code> �berf�hrt.
	 * 
	 * @throws ConnectionException
	 *             Wenn die physische Verbindung zum Datenverteiler nicht hergestellt werden konnte.
	 * @throws CommunicationError
	 *             Wenn bei der initialen Kommunikation mit dem Datenverteiler Fehler aufgetreten sind.
	 * @see ClientDavParameters
	 */
	public void connect() throws CommunicationError, ConnectionException;
	
	/**
	 * Terminiert die Verbindung mit dem Datenverteiler. Der physische Kommunikationskanal zum Datenverteiler wird geschlossen und die logische Verbindung in
	 * den Zustand <code>Au�erBetrieb</code> �berf�hrt. Bei einer normalen Terminierung wird sichergestellt, da� alle im Sendepuffer enthaltenen Telegramme
	 * vor dem Schlie�en des Kommunikationskanals gesendet werden. Bei einer abnormalen Terminierung wird der Kommunikationskanal sofort geschlossen.
	 * 
	 * @param error
	 *            Information, ob es sich um eine abnormale Terminierung der Verbindung handelt.
	 * @param message
	 *            Fehlertext, der im Falle einer abnormalen Terminierung die Ursache des Terminierung beschreibt, oder <code>null</code> wenn die Ursache
	 *            nicht bekannt ist.
	 */
	public void disconnect(boolean error, String message);
	
	///////////////////////////////////////////////////////////////////////
	////////////////////Authentifizierung Schnittstelle////////////////////
	///////////////////////////////////////////////////////////////////////
	
	/**
	 * Start der logischen Verbindung mit dem Datenverteiler. Dabei wird eine Authentifizierung des Benutzers beim Datenverteiler durchgef�hrt und die logische
	 * Verbindung in den Zustand <code>InBetrieb</code> �berf�hrt. Als Benutzername und Passwort werden die entsprechenden Werte aus den
	 * {@link ClientDavParameters Verbindungsparametern} benutzt.
	 * 
	 * @throws InconsistentLoginException
	 *             Wenn Benutzername oder Passwort nicht korrekt sind.
	 * @throws CommunicationError
	 *             Wenn bei der Kommunikation mit dem Datenverteiler Fehler aufgetreten sind.
	 */
	public void login() throws InconsistentLoginException, CommunicationError;
	
	/**
	 * Start der logischen Verbindung mit dem Datenverteiler. Dabei wird eine Authentifizierung des Benutzers beim Datenverteiler durchgef�hrt und die logische
	 * Verbindung in den Zustand <code>InBetrieb</code> �berf�hrt.
	 * 
	 * @param userName
	 *            Name des Benutzers f�r die Authentifizierung.
	 * @param password
	 *            Passwort des Benutzers f�r die Authentifizierung.
	 * 
	 * @throws InconsistentLoginException
	 *             Wenn Benutzername oder Passwort nicht korrekt sind.
	 * @throws CommunicationError
	 *             Wenn bei der Kommunikation mit dem Datenverteiler Fehler aufgetreten sind.
	 */
	public void login(String userName, String password) throws InconsistentLoginException, CommunicationError;
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 * @param cacheTime
	 *            Vorhaltezeitraum in Millisekunden. Der Vorhaltezeitraum spezifiziert, wie lange empfangene Daten zwischengespeichert werden sollen. Der Wert
	 *            <code>-1</code> setzt den Vorhaltezeitraum auf den Default-Wert.
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        SystemObject[] objects,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role,
	        long cacheTime);
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        SystemObject[] objects,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role);
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param object
	 *            System-Objekt f�r das die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 * @param cacheTime
	 *            Vorhaltezeitraum in Millisekunden. Der Vorhaltezeitraum spezifiziert, wie lange empfangene Daten zwischengespeichert werden sollen. Der Wert
	 *            <code>-1</code> setzt den Vorhaltezeitraum auf den Default-Wert.
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        SystemObject object,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role,
	        long cacheTime);
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param object
	 *            System-Objekt f�r das die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        SystemObject object,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role);
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param objects
	 *            Liste mit System-Objekten f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 * @param cacheTime
	 *            Vorhaltezeitraum in Millisekunden. Der Vorhaltezeitraum spezifiziert, wie lange empfangene Daten zwischengespeichert werden sollen. Der Wert
	 *            <code>-1</code> setzt den Vorhaltezeitraum auf den Default-Wert.
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        Collection<SystemObject> objects,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role,
	        long cacheTime);
	
	/**
	 * Anmeldung zum Empfangen von Daten. Mit der Anmeldung wird von der Applikation ein Objekt bereitgestellt, da� bei nachfolgenden Aktualisierungen der Daten
	 * entsprechend benachrichtigt wird.
	 * 
	 * @param receiver
	 *            Ein von der Applikation bereitzustellendes Objekt, das bei Aktualisierungen entsprechende Methodenaufrufe erh�lt.
	 * @param objects
	 *            Liste mit System-Objekten f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param options
	 *            F�r die Anmeldung zu verwendende Optionen.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Empf�nger oder Senke).
	 */
	public void subscribeReceiver(
	        ClientReceiverInterface receiver,
	        Collection<SystemObject> objects,
	        DataDescription dataDescription,
	        ReceiveOptions options,
	        ReceiverRole role);
	
	/**
	 * Anmeldung zum Senden von Daten. F�r jedes mit <code>objects</code> angegebene Objekt wird ein Sendeanmeldung f�r die mit <code>dataDescription</code>
	 * spezifizierten Daten beim Datenverteiler durchgef�hrt. Bei Anmeldungen als Quelle (siehe Parameter <code>role</code>) wird nach der Anmeldung
	 * automatisch ein leerer Datensatz versendet.
	 * 
	 * @param sender
	 *            Anwendungs-Objekt, an das Sendesteuerungen gesendet werden.
	 * @param objects
	 *            Liste mit System-Objekten f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Quelle oder Sender).
	 * 
	 * @throws OneSubscriptionPerSendData
	 *             Wenn bereits eine lokale Sendeanmeldung f�r die gleichen Daten von einem anderen Anwendungsobjekt vorliegt.
	 */
	public void subscribeSender(ClientSenderInterface sender, Collection<SystemObject> objects, DataDescription dataDescription, SenderRole role)
	        throws OneSubscriptionPerSendData;
	
	/**
	 * Anmeldung zum Senden von Daten. F�r jedes mit <code>objects</code> angegebene Objekt wird ein Sendeanmeldung f�r die mit <code>dataDescription</code>
	 * spezifizierten Daten beim Datenverteiler durchgef�hrt. Bei Anmeldungen als Quelle (siehe Parameter <code>role</code>) wird nach der Anmeldung
	 * automatisch ein leerer Datensatz versendet.
	 * 
	 * @param sender
	 *            Anwendungs-Objekt, an das Sendesteuerungen gesendet werden.
	 * @param objects
	 *            Feld mit System-Objekten, f�r die die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Quelle oder Sender).
	 * 
	 * @throws OneSubscriptionPerSendData
	 *             Wenn bereits eine lokale Sendeanmeldung f�r die gleichen Daten von einem anderen Anwendungsobjekt vorliegt.
	 */
	public void subscribeSender(ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription, SenderRole role)
	        throws OneSubscriptionPerSendData;
	
	/**
	 * Anmeldung zum Senden von Daten. F�r das angegebene Objekt wird ein Sendeanmeldung f�r die mit <code>dataDescription</code> spezifizierten Daten beim
	 * Datenverteiler durchgef�hrt. Bei Anmeldungen als Quelle (siehe Parameter <code>role</code>) wird nach der Anmeldung automatisch ein leerer Datensatz
	 * versendet.
	 * 
	 * @param sender
	 *            Anwendungs-Objekt, an das Sendesteuerungen gesendet werden.
	 * @param object
	 *            System-Objekt, f�r das die spezifizierten Daten anzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den anzumeldenden Daten.
	 * @param role
	 *            F�r die Anmeldung zu verwendende Rolle (Quelle oder Sender).
	 * 
	 * @throws OneSubscriptionPerSendData
	 *             Wenn bereits eine lokale Sendeanmeldung f�r die gleichen Daten von einem anderen Anwendungsobjekt vorliegt.
	 */
	public void subscribeSender(ClientSenderInterface sender, SystemObject object, DataDescription dataDescription, SenderRole role)
	        throws OneSubscriptionPerSendData;
	
	/**
	 * Anmeldung als Quelle und versenden von initialen Daten. F�r die in <code>initialData</code> enthaltenen Daten wird ein entsprechende Sendeanmeldung
	 * beim Datenverteiler durchgef�hrt und anschlie�end wird der �bergebene Datensatz als initialer Datensatz versendet.
	 * 
	 * @param sender
	 *            Anwendungs-Objekt, an das Sendesteuerungen gesendet werden.
	 * @param initialData
	 *            Initialer Datensatz, der nach der entsprechenden Anmeldung zu versenden ist.
	 * 
	 * @throws OneSubscriptionPerSendData
	 *             Wenn bereits eine lokale Sendeanmeldung f�r die gleichen Daten von einem anderen Anwendungsobjekt vorliegt.
	 */
	public void subscribeSource(ClientSenderInterface sender, ResultData initialData) throws OneSubscriptionPerSendData;
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeReceiver} durchgef�hrte Empfangsanmeldung wieder r�ckg�ngig.
	 * 
	 * @param receiver
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeReceiver(ClientReceiverInterface receiver, SystemObject[] objects, DataDescription dataDescription);
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeReceiver} durchgef�hrte Empfangsanmeldung wieder r�ckg�ngig.
	 * 
	 * @param receiver
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects
	 *            Liste mit System-Objekten f�r die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeReceiver(ClientReceiverInterface receiver, Collection<SystemObject> objects, DataDescription dataDescription);
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeReceiver} durchgef�hrte Empfangsanmeldung wieder r�ckg�ngig.
	 * 
	 * @param receiver
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param object
	 *            System-Objekt f�r das die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeReceiver(ClientReceiverInterface receiver, SystemObject object, DataDescription dataDescription);
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeSender} durchgef�hrte Sendeanmeldung wieder r�ckg�ngig.
	 * 
	 * @param sender
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects
	 *            Liste mit System-Objekten f�r die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeSender(ClientSenderInterface sender, Collection<SystemObject> objects, DataDescription dataDescription);
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeSender} durchgef�hrte Sendeanmeldung wieder r�ckg�ngig.
	 * 
	 * @param sender
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeSender(ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription);
	
	/**
	 * Abmeldung von angemeldeten Daten. Die Methode macht eine mit der Methode {@link #subscribeSender} durchgef�hrte Sendeanmeldung wieder r�ckg�ngig.
	 * 
	 * @param sender
	 *            Das Anwendungsobjekt, das bei der Anmeldung benutzt wurde.
	 * @param object
	 *            System-Objekt f�r das die spezifizierten Daten abzumelden sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den abzumeldenden Daten.
	 */
	public void unsubscribeSender(ClientSenderInterface sender, SystemObject object, DataDescription dataDescription);
	
	//////////////////////////////////////////////////////////////////
	////////////////////Konfiguration Schnittstelle////////////////////
	//////////////////////////////////////////////////////////////////
	
	/**
	 * Gibt das Datenmodell zur�ck, �ber das auf die lokale Konfiguration zugegriffen werden kann.
	 * 
	 * @return Datenmodell zum Zugriff auf die Konfiguration
	 */
	public DataModel getDataModel();
	
	/**
	 * Gibt ein Datenmodell zur�ck, �ber das auf eine beliebige Konfiguration zugegriffen werden kann.
	 *
	 * @param configAuthority Systemobjekt zum Konfigurationsverantwortlichen der Konfiguration
	 *
	 * @return Datenmodell zum Zugriff auf die Konfiguration.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Falls die angegebene Konfiguration innerhalb von 10 Minuten nicht geantwortet hat oder ein Fehler in der Kommunikation auftrat
	 */
	DataModel getDataModel(SystemObject configAuthority) throws ConfigurationTaskException;

	/**
	 * Gibt ein Datenmodell zur�ck, �ber das auf eine beliebige Konfiguration zugegriffen werden kann.
	 *
	 * @param configAuthority Pid des Konfigurationsverantwortlichen der Konfiguration
	 *
	 * @return Datenmodell zum Zugriff auf die Konfiguration.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Falls die angegebene Konfiguration innerhalb von 10 Minuten nicht geantwortet hat oder ein Fehler in der Kommunikation auftrat
	 */
	DataModel getDataModel(String configAuthority) throws ConfigurationTaskException;

	/**
	 * Gibt ein Datenmodell zur�ck, �ber das auf eine beliebige Konfiguration zugegriffen werden kann. Diese Funktion kann verwendet werden, wenn das Systemobjekt
	 * des Konfigurationsverantwortlichen der lokalen Konfiguration nicht bekannt ist.
	 *
	 * @param configAuthority Id des Konfigurationsverantwortlichen der Konfiguration
	 *
	 * @return Datenmodell zum Zugriff auf die Konfiguration.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException
	 *          Falls die angegebene Konfiguration innerhalb von 10 Minuten nicht geantwortet hat oder ein Fehler in der Kommunikation auftrat
	 */
	DataModel getDataModel(long configAuthority) throws ConfigurationTaskException;

	//////////////////////////////////////////////////////////////////
	////////////////////Angemeldete Applikationen Schnittstelle////////////////////
	//////////////////////////////////////////////////////////////////
	
	/**
	 * Gibt das Stellvertreterobjekt f�r den Datenverteiler, mit dem die Applikation verbunden ist, zur�ck.
	 * 
	 * @return Stellvertreterobjekt f�r den verbundenen Datenverteiler.
	 */
	public DavApplication getLocalDav();
	
	/**
	 * Gibt das Stellvertreterobjekt f�r diese Applikation zur�ck. Die Erzeugung des entsprechenden Objekts in der Konfiguration wird vom Datenverteiler nach
	 * dem Verbindungsaufbau und der erfolgreichen Authentifizierung veranlasst.
	 * 
	 * @return Stellvertreterobjekt f�r die lokale Applikation.
	 */
	public ClientApplication getLocalApplicationObject();
	
	/**
	 * Bestimmt das Systemobjekt f�r den angemeldeten Benutzer.
	 * 
	 * @return Stellvertreterobjekt f�r den angemeldeten Benutzer.
	 */
	public DynamicObject getLocalUser();
	
	/**
	 * Gibt das Stellvertreterobjekt des zugeordneten Konfigurationsverantwortlichen zur�ck.
	 * 
	 * @return Konfigurationsverantwortlicher.
	 */
	public ConfigurationAuthority getLocalConfigurationAuthority();
	
	///////////////////////////////////////////////////////////////////////
	////////////////////Telegrammlaufzeit Schnittstelle////////////////////
	///////////////////////////////////////////////////////////////////////
	
	/**
	 * Bestimmt die Telegrammlaufzeit von dieser Applikation zum lokalen Datenverteiler und zur�ck.
	 * 
	 * @return Telegrammlaufzeit in Millisekunden oder <code>-1</code>, wenn innerhalb 60 Sekunden keine Antwort empfangen wurde.
	 */
	public long getDavRoundTripTime();
	
	/**
	 * Zugriff auf zwischengespeicherte Datens�tze. �ber die optionale Zeitangabe kann auch auf historische Datens�tze im Zwischenspeicher zugegriffen werden.
	 * 
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten zu lesen sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu den gew�nschten Daten.
	 * @param options
	 *            F�r den Zugriff zu verwendenden Optionen
	 * @param history
	 *            Spezifikation der gew�nschten Historie oder <code>null</code> wenn nur die aktuellen Daten gew�nscht sind.
	 * 
	 * @return Zeitlich sortiertes Feld mit den gew�nschten Daten.
	 */
	public ResultData[] getCachedData(SystemObject[] objects, DataDescription dataDescription, ReceiveOptions options, HistorySpecification history);
	
	/**
	 * Zugriff auf den aktuellen Datensatz eines System-Objekts. Wenn der spezifizierte Datensatz zwischengespeichert ist, dann wird er sofort zur Verf�gung
	 * gestellt. Wenn der Datensatz nicht angemeldet ist, dann wird er implizit angemeldet. Falls noch kein Ergebnis vorliegt, wartet die Methode darauf. Eine
	 * implizite Anmeldung wird wieder r�ckg�ngig gemacht, wenn �ber eine angegebene Zeitspanne kein Zugriff mehr auf das Datum erfolgte.
	 * 
	 * @param object
	 *            System-Objekt f�r das die spezifizierten Daten zu lesen sind.
	 * @param dataDescription
	 *            Beschreibende Informationen zu dem zu lesenden Datensatz.
	 * @param unsubscriptionTime
	 *            Relative Zeitangabe in Millisekunden nach der eine implizite Anmeldung wieder abgemeldet werden kann.
	 * 
	 * @return Der aktuelle Datensatz.
	 */
	public ResultData getData(SystemObject object, DataDescription dataDescription, long unsubscriptionTime);
	
	/**
	 * Zugriff auf die aktuellen Datens�tze von verschiedenen System-Objekten. Wenn einzelne der spezifizierten Datens�tze nicht angemeldet sind, dann werden
	 * sie implizit angemeldet. Falls zu einzelnen spezifizierten Datens�tzen noch kein Ergebnis vorliegt, wird darauf gewartet. Eine implizite Anmeldung wird
	 * wieder r�ckg�ngig gemacht, wenn �ber eine angegebene Zeitspanne kein Zugriff mehr auf das Datum erfolgte.
	 * 
	 * @param objects
	 *            Feld mit System-Objekten f�r die die spezifizierten Daten zu lesen sind.
	 * @param dataDescription
	 *            Beschreibende Informationen der zu lesenden Daten.
	 * @param unsubscriptionTime
	 *            Relative Zeitangabe in Millisekunden nach der eine implizite Anmeldung wieder abgemeldet werden kann.
	 * 
	 * @return Feld mit den aktuellen Datens�tzen.
	 */
	public ResultData[] getData(SystemObject[] objects, DataDescription dataDescription, long unsubscriptionTime);
	
	/**
	 * Liefert einen neuen initialisierten Datensatz zur�ck, der mit Attributwerten der in der angegebenen Attributgruppe definierten Attribute gef�llt ist.
	 * 
	 * @param attributeGroup
	 *            Attributgruppe des neuen Datensatzes.
	 * 
	 * @return Neuen initialisierten Datensatz.
	 */
	public Data createData(AttributeGroup attributeGroup);
	
	/**
	 * Sendet einen Ergebnisdatensatz zum Datenverteiler. Die Daten m�ssen vorher zum Senden angemeldet worden sein. Falls beim ersten Sendeversuch keine
	 * positive/negative Sendesteuerung vorhanden ist, wird ein fest vorgegebener Zeitraum abgewartet in dem eine positive/negative Sendesteuerung vorliegen
	 * muss. Wenn nach Ablauf des Zeitraums keine positive/negative Sendesteuerung vorliegt, wird eine <code>SendSubscriptionNotConfirmed</code> Exception
	 * geworfen, falls die positive Sendesteuerung vor dem Ablauf des Zeitraums vorliegt, werden die Daten verschickt, liegt eine negative Sendesteuerung vor,
	 * wird eine <code>SendSubscriptionNotConfirmed</code> Exception geworfen. <br>
	 * Falls die Sendesteuerung nach dem ersten erfolgreichen Sendeversuch wieder negativ wird, und es sollen erneut Daten verschickt werden, wird eine
	 * <code>SendSubscriptionNotConfirmed</code> Exception geworfen ohne die fest vorgegebene Zeitspanne abzuwarten.
	 * 
	 * @param result
	 *            Ergebnis mit dem zu sendenden Datensatz.
	 * 
	 * @throws DataNotSubscribedException
	 *             Wenn die Daten nicht zum Senden angemeldet waren.
	 * @throws SendSubscriptionNotConfirmed
	 *             Wenn beim Senden als einfacher Sender gesendet wird, ohne die Sendesteuerung abzuwarten.
	 */
	public void sendData(ResultData result) throws DataNotSubscribedException, SendSubscriptionNotConfirmed;
	
	/**
	 * Sendet mehrere Ergebnisdatens�tze zum Datenverteiler. Die Daten m�ssen vorher zum Senden angemeldet worden sein. Falls beim ersten Sendeversuch keine
	 * positive/negative Sendesteuerung vorhanden ist, wird ein fest vorgegebener Zeitraum abgewartet in dem eine positive/negative Sendesteuerung vorliegen
	 * muss. Wenn nach Ablauf des Zeitraums keine positive/negative Sendesteuerung vorliegt, wird eine <code>SendSubscriptionNotConfirmed</code> Exception
	 * geworfen, falls die positive Sendesteuerung vor dem Ablauf des Zeitraums vorliegt, werden die Daten verschickt, liegt eine negative Sendesteuerung vor,
	 * wird eine <code>SendSubscriptionNotConfirmed</code> Exception geworfen. <br>
	 * Falls die Sendesteuerung nach dem ersten erfolgreichen Sendeversuch wieder negativ wird, und es sollen erneut Daten verschickt werden, wird eine
	 * <code>SendSubscriptionNotConfirmed</code> Exception geworfen ohne die fest vorgegebene Zeitspanne abzuwarten.
	 * 
	 * @param results
	 *            Die zu sendenden Ergebnisdatens�tze.
	 * 
	 * @throws DataNotSubscribedException
	 *             Wenn nicht alle Datens�tze zum Senden angemeldet waren.
	 * @throws SendSubscriptionNotConfirmed
	 *             Wenn beim Senden als einfacher Sender gesendet wird, ohne die Sendesteuerung abzuwarten.
	 */
	public void sendData(ResultData[] results) throws DataNotSubscribedException, SendSubscriptionNotConfirmed;
	
	/**
	 * Setzt das Objekt, das f�r die Behandlung von Fehlern der Kommunikationsverbindung zust�ndig ist. Die Applikation kann mit dem �bergebenen Objekt selbst
	 * steuern, wie Verbindungsfehler dargestellt wird, und ob sich die Applikation beenden oder eine neue Verbindung aufgebauen soll. Wenn diese Methode nicht
	 * aufgerufen wird, wird als Default ein Objekt der Klasse {@link SystemTerminator} benutzt, das bei Kommunikationsfehlern eine Fehlermeldung ausgibt und
	 * die Applikation terminiert.
	 * 
	 * @param closer
	 *            Objekt f�r die Behandlung von Kommunikationsfehlern oder null, falls bei einem Verbindungsfehler nichts passieren soll.
	 */
	public void setCloseHandler(ApplicationCloseActionHandler closer);
	
	/**
	 * Bestimmt die aktuelle Zeit oder, wenn die Applikation mit der Simulationsvariante einer Offline-Simulation gestartet wurde, die simulierte Zeit.
	 * 
	 * @return Zeitpunkt in Millisekunden seit 1970.
	 * 
	 * @throws IllegalStateException
	 *             Wenn die simulierte Zeit im Fall einer Simulation nicht bestimmt werden kann.
	 */
	public long getTime();
	
	/**
	 * Blockiert den aufrufenden Thread f�r die spezifizierte Zeit. Die angegebene Dauer der Pause wird in Realzeit oder, wenn die Applikation mit der
	 * Simulationsvariante einer Offline-Simulation gestartet wurde, im Zeitfluss der Simulation ber�cksichtigt.
	 * 
	 * @param timeToSleep
	 *            Wartezeit in Millisekunden seit 1970.
	 * 
	 * @throws IllegalStateException
	 *             Wenn der simulierte Zeitfluss im Fall einer Simulation nicht bestimmt werden kann.
	 */
	public void sleep(long timeToSleep);
	
	/**
	 * Blockiert den aufrufenden Thread bis die spezifizierte Zeit erreicht ist. Der angegebene Zeitpunkt wird in Realzeit oder, wenn die Applikation mit der
	 * Simulationsvariante einer Offline-Simulation gestartet wurde, im Zeitfluss der Simulation ber�cksichtigt.
	 * 
	 * @param absoluteTime
	 *            Abzuwartender Zeitpunkt in Millisekunden seit 1970.
	 * 
	 * @throws IllegalStateException
	 *             Wenn der simulierte Zeitfluss im Fall einer Simulation nicht bestimmt werden kann.
	 */
	public void sleepUntil(long absoluteTime);
	
	/**
	 * Liefert ein Stellvertreterobjekt zur�ck, mit dem streambasierte Archivanfragen �ber das lokale Archivsystem abgewickelt werden k�nnen. Dieser Aufruf ist
	 * �quivalent zu dem Aufruf <code>getArchive(getLocalConfigurationAuthority)</code>.
	 * 
	 * @return Stellvertreterobjekt zur Abwicklung von Archivanfragen �ber das lokale Archivsystem.
	 * 
	 * @see #getLocalConfigurationAuthority()
	 * @see #getArchive(SystemObject)
	 */
	ArchiveRequestManager getArchive();
	
	/**
	 * Liefert ein Stellvertreterobjekt zur�ck, mit dem streambasierte Archivanfragen �ber ein bestimmtes Archivsystem abgewickelt werden k�nnen. Es wird eine
	 * Verbindung zu demjenigen Archivsystem hergestellt, dass �ber das angegebene System-Objekt identifiziert wird.
	 * 
	 * @param archiveSystem
	 *            Archivsystem �ber das Archivanfragen abgewickelt werden sollen.
	 * 
	 * @return Stellvertreterobjekt zur Abwicklung von Archivanfragen �ber das spezifizierte Archivsystem.
	 */
	ArchiveRequestManager getArchive(SystemObject archiveSystem);
	
	/**
	 * Bestimmt die Verbindungsparameter der Datenverteiler-Applikationsfunktionen.
	 * Es wird eine schreibgesch�tzte Kopie zur�ckgegeben.
	 * 
	 * @return Verbindungsparameter der Datenverteiler-Applikationsfunktionen
	 *
	 * @see de.bsvrz.dav.daf.main.ClientDavParameters#clone(boolean)
	 */
	public ClientDavParameters getClientDavParameters();
	
	/**
	 * Erg�nzt einen neuen Beobachter f�r bestimmte Zustands�nderungen bzgl. der Verbindung zum Datenverteiler.
	 * 
	 * @param davConnectionListener
	 *            Beobachterobjekt, das hinzugef�gt werden soll.
	 */
	void addConnectionListener(DavConnectionListener davConnectionListener);
	
	/**
	 * Entfernt einen Beobachter f�r bestimmte Zustands�nderungen bzgl. der Verbindung zum Datenverteiler.
	 * 
	 * @param davConnectionListener
	 *            Beobachterobjekt, das entfernt werden soll.
	 */
	void removeConnectionListener(DavConnectionListener davConnectionListener);
	
	/**
	 * Methode, die von der Applikation aufgerufen werden kann, um explizit eine Applikations-Fertigmeldung zu versenden. Die SW-Einheit Start/Stopp verz�gert
	 * den Start von Applikationen, die von einer anderen Applikation abh�ngig sind, bis diese Applikation die Fertigmeldung versendet.
	 * Die Fertigmeldung wird automatisch nach dem Verbindungsaufbau versendet, wenn die Applikation nicht vor der Authentifizierung der Verbindung die Methode
	 * {@link #enableExplicitApplicationReadyMessage()} aufgerufen hat.
	 */
	public void sendApplicationReadyMessage();
	
	/**
	 * Diese Methode sollte von Applikationen, die den Zeitpunkt zum Versand der Applikations-Fertigmeldung selbst vorgeben m�chten, vor der Authentifizierung
	 * einer Datenverteilerverbindung aufrufen. Der Aufruf dieser Methode bewirkt, dass nach dem Verbindungsaufbau automatisch eine Noch-Nicht-Fertigmeldung
	 * am Applikationsobjekt versendet wird. Die Applikation sollte dann nach erfolgreicher Initialisierung den Versand der Fertig-Meldung mit der Methode
	 * {@link #sendApplicationReadyMessage()} veranlassen.
	 */
	public void enableExplicitApplicationReadyMessage();

	/**
	 * Vergleicht den angegebenen Benutzernamen und das angegebene Benutzerpasswort mit den entsprechenden Werten, die beim Verbindungsaufbau zum Datenverteiler
	 * benutzt wurden.
	 *
	 * @param userName Zu pr�fender Benutzername
	 * @param password Zu pr�fendes Passwort
	 *
	 * @return <code>true</code>, wenn eine Verbindung zum Datenverteiler besteht und mit dem angegebenen Benutzernamen und Passwort aufgebaut wurde, sonst
	 *         <code>false</code>
	 */
	boolean checkLoggedUserNameAndPassword(String userName, String password);

	/**
	 * Gibt eine Klasse zur�ck, die f�r diese Verbindung Transaktions-Anmeldungen durchf�hrt
	 * @return Klasse die Funktionen zu Transaktionen bietet
	 */
	Transactions getTransactions();
}
