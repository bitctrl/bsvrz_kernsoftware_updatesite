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
 * Verhandlung der Protokollversion (Server). Mit diesem Telegramm legt der Datenverteiler, zu dem eine Verbindung aufgebaut werden soll, die Protokollversion
 * der Kommunikationsverbindung fest.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterProtocolVersionAnswer extends DataTelegram {

	/** Die Liste der Versionen */
	private int _version;

	public TransmitterProtocolVersionAnswer() {
		type = TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	public TransmitterProtocolVersionAnswer(int version) {
		type = TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_version = version;
		length = 4;
	}

	/**
	 * Gibt die unterstützte Protokoll-Version zurück.
	 *
	 * @return die höchste Protokoll-Version
	 */
	public final int getPreferredVersion() {
		return _version;
	}

	public final String parseToString() {
		String str = "Systemtelegramm VersionsProtokoll Antwort: \n";
		str += "Unterstützte Version     : " + _version + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeInt(_version);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_version = in.readInt();
		length = 4;
		if(length != _length) {
			throw new IOException("Falsche Telegram Länge");
		}
	}
}
