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
 * Mit diesem Telegramm legt der Datenverteiler die Protokollversion der Kommunikationsverbindung zu den Datenverteiler-Applikationsfunktionen fest. Die
 * Datenverteiler-Applikationsfunktionen senden dem Datenverteiler eine priorisierte Liste von unterst�tzten Versionsnummern aus denen der Datenverteiler im
 * Normalfall die Version der h�chsten Priorit�t ausw�hlt, die auch vom Datenverteiler unterst�tzt wird. Diese wird dann zu den
 * Datenverteiler-Applikationsfunktionen gesendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class ProtocolVersionAnswer extends DataTelegram {

	/** Die Protokoll-Version zur Kommunikation mit dem Datenverteiler. */
	private int _version;

	public ProtocolVersionAnswer() {
		type = PROTOCOL_VERSION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/** @param version die Versionsnummer */
	public ProtocolVersionAnswer(int version) {
		type = PROTOCOL_VERSION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_version = version;
		length = 4;
	}

	/**
	 * Gibt die unterst�tzte Protokoll-Version zur�ck.
	 *
	 * @return Die h�chste Protokoll-Version.
	 */
	public final int getPreferredVersion() {
		return _version;
	}

	// geerbt von abstract class DataTelegram
	public final String parseToString() {
		String str = "Systemtelegramm VersionsProtokoll Antwort: \n";
		str += "Unterst�tzte Version     : " + _version + "\n";
		return str;
	}

	// geerbt von abstrakt class DataTelegram
	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeInt(_version);
	}

	// geerbt von abstract class DataTelegram
	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_version = in.readInt();
		length = 4;
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}
