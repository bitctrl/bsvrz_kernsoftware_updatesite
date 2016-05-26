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
 * Diese Klasse stellt ein Anfragetelegramm zu den Verbindungsinformationen eines DAV dar. Das Telegramm enthält die ID des DAV dessen Verbindungen, zu anderen
 * DAV, gesucht werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterConnectionInfoRequest extends ConfigTelegram {

	/**
	 * Gewünschte Version der Antwort. Der Wert 0 kennzeichnet, dass nur die ursprüngliche Version einer Antwort ohne Benutzernamen für die Authentifizierung
	 * erwartet wird; der Wert 1 kennzeichnet, dass falls möglich die neue Version der Antwort mit Benutzernamen für die Authentifizierung bevorzugt wird.
	 *
	 * @see TransmitterConnectionInfoAnswer
	 */
	private long _desiredReplyVersion;

	/** Die Id des Datenverteilers */
	private long _transmitterId;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem späteren Zeitpunkt über die read-Methode eingelesen. */
	public TransmitterConnectionInfoRequest() {
		_type = TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param desiredReplyVersion Gewünschte Version der Antwort
	 * @param transmitterId       TransmitterID
	 */
	public TransmitterConnectionInfoRequest(long desiredReplyVersion, long transmitterId) {
		_type = TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE;
		this._desiredReplyVersion = desiredReplyVersion;
		this._transmitterId = transmitterId;
	}

	/**
	 * Bestimmt die gewünschte Antwort-Version.
	 *
	 * @return Der Wert 0 kennzeichnet, dass nur die ursprüngliche Version einer Antwort ohne Benutzernamen für die Authentifizierung erwartet wird; der Wert 1
	 *         kennzeichnet, dass falls möglich die neue Version der Antwort mit Benutzernamen für die Authentifizierung bevorzugt wird.
	 *
	 * @see TransmitterConnectionInfoAnswer
	 */
	public final long getDesiredReplyVersion() {
		return _desiredReplyVersion;
	}

	/**
	 * Bestimmt die Id des Datenverteilers zu dem Verbindungsinformationen angefordert werden sollen.
	 *
	 * @return ID des Datenverteilers
	 */
	public final long getTransmitterId() {
		return _transmitterId;
	}

	/** @return Liefert eine Beschreibung des Telegramms für Debug-Zwecke */
	public final String parseToString() {
		String str = "Datenverteilernetztopologie Anfrage: \n";
		str += "gewünschte Antwort-Version: " + _desiredReplyVersion + "\n";
		str += "Datenverteiler Id: " + _transmitterId + "\n";
		return str;
	}


	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_desiredReplyVersion);
		out.writeLong(_transmitterId);
	}

	public final void read(DataInputStream in) throws IOException {
		_desiredReplyVersion = in.readLong();
		_transmitterId = in.readLong();
	}
}
