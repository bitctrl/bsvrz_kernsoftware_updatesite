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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Signalisiert die Bereitschaft, sich gegenüber seinem Kommunikationspartner zu authentifizieren. Mit diesem Systemtelegramm leitet ein Datenverteiler seine
 * Authentifizierung gegenüber seinem Kommunikationspartner ein. Dabei überträgt er seine ID und seinen Namen. Dieses Systemtelegramm wird zunächst von dem
 * Datenverteiler gesendet, der die Verbindung zu einem anderen Datenverteiler aufbauen möchte. Wenn er sich erfolgreich authentifizieren konnte, wird die
 * Authentifizierung in der anderen Richtung durchgeführt. Dieses Telegramm ist die Aufforderung an den Kommunikationspartner eine
 * Authentifizierungsaufforderung mit einem neuen zu verschlüsselnden Zufallstext zu senden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterAuthentificationTextRequest extends DataTelegram {

	/** Die ID des Datenverteilers */
	private long _transmitterId;

	public TransmitterAuthentificationTextRequest() {
		type = TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/** @param transmitterId Identifikation des Datenverteilers, der sich authentifizieren möchte */
	public TransmitterAuthentificationTextRequest(long transmitterId) {
		type = TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_transmitterId = transmitterId;
		length = 8;
	}

	/**
	 * Gibt die ID des Datenverteilers an.
	 *
	 * @return die ID des Datenverteilers
	 */
	public final long getTransmitterId() {
		return _transmitterId;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikationsschlussel Anfrage: \n";
		str += "Datenverteiler Id: " + _transmitterId + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeLong(_transmitterId);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_transmitterId = in.readLong();
		length = 8;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
