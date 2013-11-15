/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Mit diesem Systemtelegramm wird eine Anfrage zur Ermittlung der Telegrammlaufzeit zwischen einer Applikation und dem zugeh�rigen Datenverteiler beantwortet.
 * Die Ermittlung der Telegrammlaufzeit ist in beiden Richtungen m�glich (siehe  Telegrammlaufzeitanfrage ).
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class TelegramTimeAnswer extends DataTelegram {

	/** Die Zeit bis hierher, die das Telegramm f�r seine Laufzeitsermittlung gebraucht hat. */
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
	 * Gibt die Startzeit der Durchsatzpr�fung zur�ck.
	 *
	 * @return die Startzeit der Durchsatzpr�fung
	 */
	public final long getTelegramStartTime() {
		return _telegramTime;
	}

	/**
	 * Gibt die Zeit der Durchsatzpr�fung zur�ck.
	 *
	 * @return die ben�tigte Zeit f�r die Durchsatzpr�fung
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
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}
