/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.impl.subscription.SenderSubscription;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.timeout.TimeoutTimer;

/**
 * TBD Beschreibung
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8625 $
 */
public class SendSubscriptionObject {

	/** Der Sendeindex dieser Anmeldung */
	private int _sendIndex;

	/** Die Sendeanmeldeinformationen */
	private SenderSubscription _senderSubscription;

	/** Die Zeit des Sendeindexes */
	private long _time;

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/**
	 * Falls auf die Sendesteuerung (positiv/negativ) gewartet werden muss, wird zum synchronisieren dieses Objekt benutzt. Falls sich die Sendesterung ändert,
	 * wird jeder Thread geweckt, der auf dieses Objekt wartet. Das selbe passiert, wenn eine fest vorgegebene Zeitspanne abläuft, auch in diesem Fall wird jeder
	 * benachtichtigt, der auf diesem Objekt wartet.
	 */
	private final Object _requestLock = new Object();

	/** true = Es wurde eine Sendesteuerung gesetzt (positiv oder negativ); false = Es wird noch auf eine Sendesteuerung gewartet. */
	private boolean _requestAnswered = false;

	/** Zeitraum (in ms), der gewartet wird um eine Sendesteuerung (positiv/negativ) zu erhalten. Es wird derzeit 1 Minute gewartet. */
	private final long _timeOut = 1 * 60 * 1000;

	/**
	 * Diese Variable wird true, falls das erste mal Daten verschickt werden können. Wenn danach die Sendesteuerung negativ wird, wird nicht noch einmal die
	 * <code>_timeOut</code> gewartet.
	 */
	private boolean _checkedConnectionFirstTime = false;

	public SendSubscriptionObject(SenderSubscription senderSubscription) {
		_senderSubscription = senderSubscription;
		_sendIndex = 0;
		_time = (((System.currentTimeMillis() / 1000L) << 32) & 0xFFFFFFFF00000000L);
	}

	/** Gibt den Anmelde-Zeitstempel zurück
	 * @return Sekunden seit 1970
	 */
	public int getTimeStamp() {
		return (int)(_time >> 32);
	}

	/**
	 * Gibt die Sendeanmeldeinformationen zurück
	 *
	 * @return Sendeanmeldeinformationen
	 */
	public final SenderSubscription getSenderSubscription() {
		return _senderSubscription;
	}

	/**
	 * Gibt den Index der Sendung der übergebenen Anmeldungsinformation zurück. Der Index startet immer mit 1 und wird bei jeder Abfrage um 1 erhöht, wenn es
	 * 0x3FFFFFFF (2 hoch 29 ) erreicht hat, fängt es wieder bei 1 an.
	 *
	 * @return der Index der Sendung
	 */
	public final long getSendDataIndex() {
		++_sendIndex;
		if(_sendIndex > 0x3FFFFFFF) {
			_sendIndex = 1;
		}
		long dataIndex = ((long)(_sendIndex << 2) & 0x00000000FFFFFFFCL);
		long retValue = ((_time | dataIndex) & 0xFFFFFFFFFFFFFFFCL);
		return retValue;
	}

	/**
	 * Gibt das Sendestellvetreterobject zurück.
	 *
	 * @return Sendestellenvertreterobjekt
	 */
	public final ClientSenderInterface getClientSender() {
		if(_senderSubscription == null) {
			return null;
		}
		return _senderSubscription.getClientSender();
	}

	/**
	 * Gibt die Information zurük, ob die Applikation Daten senden kann oder nicht.
	 *
	 * @return true: Applikation kann Daten senden, false: Applikation kann keine Daten senden.
	 */
	public final boolean canSendData() {
		if(_senderSubscription == null) {
			return false;
		}
		synchronized(_requestLock) {
			// Falls beim ersten mal eine gar keine Sendesteuerung vorliegt (positiv/negativ), wird eine bestimmte Zeitdauer gewartet.
			// In dieser Zeit muss die Sendesteuerung positiv oder negativ werden.
			// Wird die Sendesteuerung nicht positiv, wird <code>false</code> zurückgegeben, dies führt
			// dann zu einer Exception. Wird die Sendesteuerung vor Ablauf der Zeit positiv, wird
			// <code>true</code> zurückgegeben.
			// Liegt gar keine Sendesteuerung vor, wird <code>false</code> zurückgegeben.

			// Bei Anmeldung als Quelle wird direkt <code>true</code> zurückgegeben
			if(_senderSubscription.isSource()) {
				return true;
			}
			else if(_checkedConnectionFirstTime == true && _requestAnswered == true) {
				// Es wurde ein bestimmter Zeitraum abgewartet und es liegt eine Antwort vor, dann
				// wird dieses Ergebnis benutzt (wenn also auch nach Ablauf der Zeit eine Sendersteuerung vorliegt,
				// die nach Ablauf der Zeit noch nicht vorhanden war, wird dieser Zustand zurückgegeben)
				return _senderSubscription.isRequestConfirmed();
			}
			else if(_checkedConnectionFirstTime == true && _requestAnswered == false) {
				// Es wurde ein bestimmter Zeitraum abgewartet und es liegt !keine! Antwort vor, dann
				// wird immer false zurückgegeben, da der Zustand unbekannt ist.
				return false;
			}

			// Es besteht keine Sendesteuerung (positiv oder negativ) und das "erste warten" hat noch nicht statt gefunden, also warten.
			// Dieser Teil wird nur einmal am Anfang durchlaufen, danach verhindert _checkedConnectionFirstTime dies.
			final TimeoutTimer timer = new TimeoutTimer(_timeOut);
			// Solange die Sendesteuerung nicht auf positiv/negativ geändert wurde oder aber der Timer ist
			// abgelaufen, wird nichts gemacht

			while((_requestAnswered == false) && (timer.isTimeExpired() == false)) {
				_debug.fine("Applikation wartet auf eine positive/negative Sendesteuerung: sendIndex = " + _sendIndex);

				// Die Restzeit speichern, ist diese 0 darf der Thread nicht mit Restzeit 0 schlafen gelegt werden.
				// Dies ist nötig, denn ein Thread, der mit 0 schlafen gelegt wird, wartet auf ein notify und
				// wacht nicht nach 0 Sekunden auf !
				// (bei Restzeit 0 wird die while-Schleife von alleine verlassen, da isTimeExpired == true ist)
				final long remainingTime = timer.getRemainingTime();
				if(remainingTime > 0) {
					try {
						_requestLock.wait(remainingTime);
					}
					catch(InterruptedException e) {
						_debug.info("Ein Thread, der auf eine positive/negative Sendersteuerung wartet, wurde abgebrochen: ", e);
					}
				}
			}

			// Die while-Schleife wurde verlassen, also ist die Sendesteuerung(positiv oder negativ) entweder vorhanden oder die Zeit ist abgelaufen.
			_checkedConnectionFirstTime = true;
			if(_requestAnswered == true) {
				// Es wurde eine positive/negative Sendesteuerung gesetzt
				return _senderSubscription.isRequestConfirmed();
			}
			else {
				// Es wurde noch keine Sendesteuerung gesetzt, also eine Excpetion auslösen
				return false;
			}
		}
	}

	/**
	 * Setzt die Flagge, ob Daten gesendet werden können oder nicht
	 *
	 * @param state Status
	 */
	public final void confirmSendDataRequest(byte state) {
		if(state == 0) {
			// Es liegt eine positive Sendesteuerung vor, falls jemand auf diese Nachricht wartet, wird er geweckt
			_senderSubscription.confirmRequest(true);
			synchronized(_requestLock) {
				_requestAnswered = true;
				_requestLock.notifyAll();
			}
		}
		else {
			// Es liegt keine positive Sendesteuerung vor, falls jemand auf die Antwort wartet, wird er geweckt
			_senderSubscription.confirmRequest(false);
			synchronized(_requestLock) {
				_requestAnswered = true;
				_requestLock.notifyAll();
			}
		}
	}
}
