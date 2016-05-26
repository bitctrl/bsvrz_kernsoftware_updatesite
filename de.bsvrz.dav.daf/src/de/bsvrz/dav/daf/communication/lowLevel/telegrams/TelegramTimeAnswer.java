/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Mit diesem Systemtelegramm wird eine Anfrage zur Ermittlung der Telegrammlaufzeit zwischen einer Applikation und dem zugehörigen Datenverteiler beantwortet.
 * Die Ermittlung der Telegrammlaufzeit ist in beiden Richtungen möglich (siehe  Telegrammlaufzeitanfrage ).
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TelegramTimeAnswer extends DataTelegram {

	/** Die Zeit bis hierher, die das Telegramm für seine Laufzeitsermittlung gebraucht hat. */
	private long _telegramTime;

	private long _roundTripTime = 0;

	public TelegramTimeAnswer() {
		type = TELEGRAM_TIME_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 *  Erzeugt neues TelegramTimeAnswer
	 * @param telegramRequestTime Zeitpunkt des Versandes der Telegrammlaufzeitanfrage 
	 */
	public TelegramTimeAnswer(long telegramRequestTime) {
		type = TELEGRAM_TIME_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_telegramTime = telegramRequestTime;
		length = 8;
	}

	/**
	 * Gibt die Startzeit der Durchsatzprüfung zurück.
	 *
	 * @return die Startzeit der Durchsatzprüfung
	 */
	public final long getTelegramStartTime() {
		return _telegramTime;
	}

	/**
	 * Gibt die Zeit der Durchsatzprüfung zurück.
	 *
	 * @return die benötigte Zeit für die Durchsatzprüfung
	 */
	public final long getRoundTripTime() {
		return _roundTripTime;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Laufzeitsermittlung Antwort: \n";
		str += "Gesamte Zeit der ermittlung : " + _telegramTime + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeLong(_telegramTime);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_telegramTime = in.readLong();
		_roundTripTime = System.currentTimeMillis() - _telegramTime;
		length = 8;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
