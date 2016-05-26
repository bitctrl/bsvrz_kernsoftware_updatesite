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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Antworttelegramm zu den Verbindungsinformationen eines DAV dar. In diesem Telegramm werden die Verbindungen eines DAV, welcher über
 * eine ID identifiziert worden ist, gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterConnectionInfoAnswer extends ConfigTelegram {

	/**
	 * Version der Antwort. Der Wert 0 kennzeichnet, dass nur die ursprüngliche Version des Antworttelegramms ohne Benutzernamen für die Authentifizierung
	 * übertragen wird; der Wert 1 kennzeichnet, dass die neue Version der Antwort mit Benutzernamen für die Authentifizierung übertragen wird.
	 *
	 * @see TransmitterConnectionInfoRequest
	 */
	private long _telegramVersion;

	/** Die Id des Datenverteilers */
	private long _transmitterId;

	/** Array mit den Verbindungsinformationen des Datenverteilers */
	private TransmitterConnectionInfo _transmitterConnectionInfos[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem späteren Zeitpunkt über die read-Methode eingelesen. */
	public TransmitterConnectionInfoAnswer() {
		_type = TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den angegebenen Parametern.
	 *
	 * @param telegramVersion            Version des Antworttelegramms
	 * @param transmitterId              ID des Transmitters
	 * @param transmitterConnectionInfos Array mit den Verbindungsinformationen des Datenverteilers
	 */
	public TransmitterConnectionInfoAnswer(
			long telegramVersion, long transmitterId, TransmitterConnectionInfo transmitterConnectionInfos[]) {
		_type = TRANSMITTER_CONNECTION_INFO_ANSWER_TYPE;
		_telegramVersion = telegramVersion;
		_transmitterId = transmitterId;
		_transmitterConnectionInfos = transmitterConnectionInfos;
	}

	/**
	 * Bestimmt die Version des Antworttelegramms.
	 *
	 * @return Der Wert 0 kennzeichnet, dass nur die ursprüngliche Version des Antworttelegramms ohne Benutzernamen für die Authentifizierung übertragen wird; der
	 *         Wert 1 kennzeichnet, dass die neue Version der Antwort mit Benutzernamen für die Authentifizierung übertragen wird.
	 *
	 * @see TransmitterConnectionInfoRequest
	 */
	public final long getTelegramVersion() {
		return _telegramVersion;
	}

	/**
	 * Gibt die Id des Datenverteilers zurück
	 *
	 * @return ID des Datenverteilers
	 */
	public final long getTransmitterId() {
		return _transmitterId;
	}

	/**
	 * Bestimmt die Verbindungsinformationen des Datenverteilers.
	 *
	 * @return Array mit den Verbindungsinformationen des Datenverteilers.
	 */
	public final TransmitterConnectionInfo[] getTransmitterConnectionInfos() {
		return _transmitterConnectionInfos;
	}

	public final String parseToString() {
		String str = "Datenverteilernetztopologie Antwort: \n";
		str += "Telegrammversion: " + _telegramVersion + "\n";
		str += "Datenverteiler Id: " + _transmitterId + "\n";
		if(_transmitterConnectionInfos != null) {
			str += "[";
			for(int i = 0; i < _transmitterConnectionInfos.length; ++i) {
				str += "- " + _transmitterConnectionInfos[i].parseToString() + "\n";
			}
			str += "]";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_telegramVersion);
		out.writeLong(_transmitterId);
		out.writeInt(_transmitterConnectionInfos.length);
		for(int i = 0; i < _transmitterConnectionInfos.length; ++i) {
			_transmitterConnectionInfos[i].write(out, _telegramVersion);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		_telegramVersion = in.readLong();
		_transmitterId = in.readLong();
		int size = in.readInt();
		_transmitterConnectionInfos = new TransmitterConnectionInfo[size];
		for(int i = 0; i < size; ++i) {
			_transmitterConnectionInfos[i] = new TransmitterConnectionInfo();
			_transmitterConnectionInfos[i].read(in, _telegramVersion);
		}
	}
}
