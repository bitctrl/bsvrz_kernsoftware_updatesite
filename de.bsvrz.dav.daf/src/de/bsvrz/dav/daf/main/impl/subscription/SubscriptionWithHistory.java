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

package de.bsvrz.dav.daf.main.impl.subscription;


/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SubscriptionWithHistory {

	/** Die Empfangsanmeldung */
	private ReceiverSubscription _receiverSubscription;

	/** Signalisiert, dass mindenstens eine Aktuallisierung der Onlinedaten erfolgt ist */
	private boolean _actualDataAvailable;

	/** Anzahl der angestossenen Archive Anfragen. */
	private byte _historyRequestsNumber;

	/**
	 * Erzeugt ein neues objekt mit den gegebenen Parametern.
	 *
	 * @param receiverSubscription Empfangsanmeldung
	 */
	public SubscriptionWithHistory(ReceiverSubscription receiverSubscription) {
		_receiverSubscription = receiverSubscription;
		_actualDataAvailable = false;
		_historyRequestsNumber = 0;
	}

	/**
	 * Erzeugt ein neues objekt mit den gegebenen Parametern.
	 *
	 * @param receiverSubscription Empfangsanmeldung
	 * @param actualDataAvailable  mindestes ein datensatz ist online
	 */
	public SubscriptionWithHistory(ReceiverSubscription receiverSubscription, boolean actualDataAvailable) {
		_receiverSubscription = receiverSubscription;
		_actualDataAvailable = actualDataAvailable;
		_historyRequestsNumber = 0;
	}

	/**
	 * Gibt die Empfangsanmeldung zurück
	 *
	 * @return Empfangsanmeldung
	 */
	public final ReceiverSubscription getReceiverSubscription() {
		return _receiverSubscription;
	}

	/**
	 * Gibt an, ob eine Aktualisierung der Onlinedaten und Archivedaten erfolgt ist
	 *
	 * @return <code>true:</code> Aktualisierung der Onlinedaten und Archivedaten erfolgt, <code>false:</code> Aktualisierung nicht erfolgt
	 */
	public final boolean getUpdateState() {
		return (_actualDataAvailable && (_historyRequestsNumber > 0));
	}

	/** Aktualisiert die Zeitintervalle und markiert die Onlinedatenaktualisierung */
	public final void actualDataUpdate() {
		_actualDataAvailable = true;
	}

	/** Aktualisiert die Zeitintervalle und markiert die Archivdatenaktualisierung */
	public final void historyDataUpdate() {
		++_historyRequestsNumber;
	}

	/**
	 * Gibt an, ob schonmal eine Archivedatenaktualisierung erfolgt ist
	 *
	 * @return aktuelle Anzahl der angestossenen Archiv Anfragen.
	 */
	public final byte getHistoryUpdatesNumber() {
		return _historyRequestsNumber;
	}
}
