/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.main.ConnectionException;

/**
 * Dieses Interface legt die öffentlichen Methoden der unteren Kommunikationsebene von Datenverteilerverbindungen fest.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface LowLevelCommunicationInterface {

	/**
	 * Diese Methode baut eine Kommunikationsverbindung zu den angegebenen Adressen auf. Sie wird von der Protokollschicht DaV-DAF während der
	 * Verbindungsinitialisierung aufgerufen.
	 *
	 * @param mainAddress Adresse des Kommunikationspartners dieser Verbindung.
	 * @param subAddress  Subadresse der Adresse.
	 *
	 * @throws de.bsvrz.dav.daf.main.ConnectionException Falls die Verbindung nicht hergestellt werden kann.
	 */
	public void connect(String mainAddress, int subAddress) throws ConnectionException;

	/**
	 * Diese Methode wird von der Protokollschicht DaV-DAF aufgerufen, wenn die Kommunikationskanäle geschlossen werden sollen.
	 *
	 * @param error   Besagt, ob es sich um eine Terminierung mit Fehler handelt.
	 * @param message der Fehlertext
	 * @param terminationTelegram
	 */
	public void disconnect(boolean error, String message, final DataTelegram terminationTelegram);

	/**
	 * Gibt als Information zurück, ob die Kommunikationsverbindung unterbrochen ist.
	 *
	 * @return <code>true</code> = Es besteht keine Verbindung; <code>false</code> = sonst
	 */
	public boolean isNotConnected();

	/**
	 * Diese Methode wird von der Protokollschicht DaV-DAF aufgerufen, wenn ein Telegramm gesendet werden soll.
	 *
	 * @param telegram Das zu versendende Telegramm.
	 */
	public void send(DataTelegram telegram);

	/**
	 * Fügt mehrere Telegramme in die Sendetabelle ein.
	 *
	 * @param telegrams Die zu versendenden Telegramme.
	 *
	 * @see #send(de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram)
	 */
	public void send(DataTelegram telegrams[]);

	/**
	 * Setzt die Interpretationsschicht dieser Komponente für den internen Datenaustausch. Erst nach Aufruf dieser Methode von der Protokollschicht DaV-DAF werden
	 * die Kommunikations-Threads gestartet, weil sonst keine Telegramme interpretiert oder weitergeleitet werden können.
	 *
	 * @param highLevelComponent Komponente, die benachrichtigt werden muss, wenn neue Nachrichten empfangen werden.
	 */
	public void setHighLevelComponent(HighLevelCommunicationCallbackInterface highLevelComponent);

	/**
	 * Diese Methode wird von der Protokollschicht DaV-DAF aufgerufen, wenn die Keepalive-Parameter-Verhandlung erfolgreich abgeschlossen ist und setzt somit die
	 * Timeouts des Keepaliveprozesses.
	 *
	 * @param keepAliveSendTimeOut    Sendekeepalivetimeout
	 * @param keepAliveReceiveTimeOut Empfangekeepalivetimeout
	 */
	public void updateKeepAliveParameters(long keepAliveSendTimeOut, long keepAliveReceiveTimeOut);

	/**
	 * Diese Methode setzt die Parameter für die Durchsatzprüfung. Sie wird von der Protokollschicht DaV-DAF aufgerufen, wenn die Parameter für die
	 * Durchsatzprüfung erfolgreich verhandelt wurden.
	 *
	 * @param throughputControlSendBufferFactor
	 *                                 Füllungsgrad des Sendepuffers als Faktor zwischen 0 und 1, ab dem die Durchsatzprüfung anfängt zu arbeiten.
	 * @param throughputControlInterval Zeit zwischen zwei Durchsatzprüfungen in Millisekunden
	 * @param minimumThroughput        Minimal zulässiger Verbindungsdurchsatz in Bytes pro Sekunde
	 */
	public void updateThroughputParameters(float throughputControlSendBufferFactor, long throughputControlInterval, int minimumThroughput);

	/**
	 * Gibt den Repräsentant der Verbindung zurück
	 *
	 * @return Repräsentant der Verbindung
	 */
	public ConnectionInterface getConnectionInterface();

	/**
	 * Liefert einen beschreibenden Text mit dem Zustand des Sendepuffers
	 * @return Zustand des Sendepuffers
	 */
	public String getSendBufferState();

	/**
	 * Diese Methode setzt den Namen des Kommunikationspartners, der für Fehlermeldungen etc. verwendet wird.
	 * @param name Name oder Identifikation des Kommunikationspartners
	 */
	void setRemoteName(final String name);
}

