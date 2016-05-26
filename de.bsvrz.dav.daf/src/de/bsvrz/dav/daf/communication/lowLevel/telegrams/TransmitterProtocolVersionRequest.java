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
 * Verhandlung der Protokollversion (Client). Der den Verbindungsaufbau einleitende Datenverteiler sendet dem Datenverteiler, zu dem eine Verbindung hergestellt
 * werden soll, eine priorisierte Liste von unterstützten Versionsnummern aus denen dieser Datenverteiler im Normalfall die Version der höchsten Priorität
 * auswählt, die auch von ihm unterstützt wird. Diese wird dann zu dem Datenverteiler, der die Verbindung aufbauen will, gesendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterProtocolVersionRequest extends DataTelegram {

	/** Die Liste der Versionen */
	private int _versions[];

	public TransmitterProtocolVersionRequest() {
		type = TRANSMITTER_PROTOCOL_VERSION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erstellt ein neues Telegramm des Typs <code>ProtocolVersionRequest</code>.
	 *
	 * @param versions eine Liste mit Protokollversionen
	 */
	public TransmitterProtocolVersionRequest(int[] versions) {
		type = TRANSMITTER_PROTOCOL_VERSION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_versions = versions;
		length = 4;
		if(this._versions != null) {
			length += this._versions.length * 4;
		}
	}

	/**
	 * Gibt die unterstützten Protokoll-Versionen zurück.
	 *
	 * @return Eine Liste der unterstützten Protokoll-Versionen.
	 */
	public final int[] getVersions() {
		if(_versions == null) {
			return null;
		}
		int versions[] = new int[_versions.length];
		System.arraycopy(_versions, 0, versions, 0, _versions.length);
		return versions;
	}

	/**
	 * Gibt die höchste Protokoll-Version zurück.
	 *
	 * @return die höchste Protokoll-Version oder <code>-1</code>, wenn keine Protokollversionen spezifiziert wurden.
	 */
	public final int getPreferredVersion() {
		if(_versions == null) {
			return -1;
		}
		return _versions[0];
	}

	public final String parseToString() {
		String str = "Systemtelegramm VersionsProtokoll Anfrage: \n";
		str += "Unterstützte Versionen     : ";
		if(_versions != null) {
			for(int i = 0; i < _versions.length; ++i) {
				str += _versions[i];
				if(i < _versions.length - 1) {
					str += " , ";
				}
			}
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		if(_versions == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_versions.length);
			for(int i = 0; i < _versions.length; ++i) {
				out.writeInt(_versions[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		length = 4;
		int size = in.readInt();
		if(size > 0) {
			_versions = new int[size];
			for(int i = 0; i < size; ++i) {
				_versions[i] = in.readInt();
			}
			length += _versions.length * 4;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegram Länge");
		}
	}
}
