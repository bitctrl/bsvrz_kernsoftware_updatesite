/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config.request.telegramManager;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

/**
 * Dieses Interface emöglicht eine Kommunikation mit einem Sender und einem Empfänger. Der Sender verschickt Aufträge und empfängt dann die Antworten auf diese
 * Aufträge.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface SenderReceiverCommunication {

	/**
	 * Verschickt eine Anfrage vom angegeben Typ und mit dem in einem Byte Array angegebenen serialisierten Inhalt, die Methode gibt ohne zu blockieren eine
	 * neue Anfragenummer zurück. Die Antwort kann mit {@link #waitForReply} abgerufen werden.
	 *
	 * @param messageType Anfragetyp
	 * @param data Daten Serialiserte Anfragedaten.
	 *
	 * @return Index, der benötigt wird um die Antwort auf eine Anfrage zu abzufragen.
	 *
	 * @throws SendSubscriptionNotConfirmed Wenn noch keine positive Sendesteuerung vom Datenverteiler für die zu versendenden Daten vorliegt
	 * @throws IllegalStateException        Die Daten können versendet werden, aber die Antwort kann nicht empfangen werden, da für den empfang der Daten nicht die
	 *                                      benötigten Rechte vorhanden sind. Aus diesem Grund werden die Daten nicht verschickt.
	 */
	public int sendData(String messageType, byte[] data) throws SendSubscriptionNotConfirmed, IllegalStateException;

	/**
	 * Verschickt eine Antwort vom angegeben Typ zu einer Anfrage.
	 *
	 * @param messageType Antworttyp
	 * @param data Daten Serialiserte Anfragedaten.
	 * @param queryIndex Index der zugehörigen Anfrage
	 *
	 * @throws SendSubscriptionNotConfirmed Wenn noch keine positive Sendesteuerung vom Datenverteiler für die zu versendenden Daten vorliegt
	 * @throws IllegalStateException        Die Daten können versendet werden, aber die Antwort kann nicht empfangen werden, da für den empfang der Daten nicht die
	 *                                      benötigten Rechte vorhanden sind. Aus diesem Grund werden die Daten nicht verschickt.
	 */
	public void sendData(String messageType, byte[] data, int queryIndex) throws SendSubscriptionNotConfirmed, IllegalStateException;

	/**
	 * Stellt die Antwort auf eine Anfrage zur Verfügung
	 *
	 * @param requestIndex Index, der bei der Methode {@link #sendData} als Rückgabeparameter zurückgegeben wurde
	 *
	 * @return Antwort auf eine Anfrage
	 * @throws de.bsvrz.dav.daf.main.impl.config.request.RequestException Wenn die Kommunikation zum Datenverteiler unterbrochen wurde.
	 */
	public Data waitForReply(int requestIndex) throws RequestException;

	/** Schließt alle geöffneten Verbindungen und beendet mögliche Threads */
	public void close();

	/**
	 * Diese Methode gibt den derzeitigen Zustand einer Anmeldung als Sender/Quelle wieder.
	 *
	 * @return derzeitiger Zustand der Verbindung
	 */
	public ConnectionState getConnectionState();

	/**
	 * Setzt den Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten bzgl. Änderungen der Elemente von dynamischen Mengen bzw. dynamischen
	 * Typen.
	 * @param notifyingMutableCollectionChangeListener Listener zur Verarbeitung und Verteilung von Aktualisierungsnachrichten.
	 */
	void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener);

	/** Diese Klasse spiegelt die Verbindung einer Sende/Quelle-Anmeldung wieder. */
	public final static class ConnectionState {

		public final static ConnectionState NotYetConnected = new ConnectionState("Noch nicht verbunden");

		public final static ConnectionState Connected = new ConnectionState("Verbunden");

		public final static ConnectionState Disconnected = new ConnectionState("Nicht mehr verbunden");

		public final static ConnectionState Error = new ConnectionState("Fehler");
		
		public final static ConnectionState DavConnectionLost = new ConnectionState("Datenverteilerverbindung getrennt");

		public String toString() {
			return _name;
		}

		private String _name;

		private ConnectionState(String name) {
			_name = name;
		}
	}
}
