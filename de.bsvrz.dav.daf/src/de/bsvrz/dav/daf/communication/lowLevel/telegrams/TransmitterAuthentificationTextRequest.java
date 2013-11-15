/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
 * Signalisiert die Bereitschaft, sich gegen�ber seinem Kommunikationspartner zu authentifizieren. Mit diesem Systemtelegramm leitet ein Datenverteiler seine
 * Authentifizierung gegen�ber seinem Kommunikationspartner ein. Dabei �bertr�gt er seine ID und seinen Namen. Dieses Systemtelegramm wird zun�chst von dem
 * Datenverteiler gesendet, der die Verbindung zu einem anderen Datenverteiler aufbauen m�chte. Wenn er sich erfolgreich authentifizieren konnte, wird die
 * Authentifizierung in der anderen Richtung durchgef�hrt. Dieses Telegramm ist die Aufforderung an den Kommunikationspartner eine
 * Authentifizierungsaufforderung mit einem neuen zu verschl�sselnden Zufallstext zu senden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5059 $
 */
public class TransmitterAuthentificationTextRequest extends DataTelegram {

	/** Die ID des Datenverteilers */
	private long _transmitterId;

	public TransmitterAuthentificationTextRequest() {
		type = TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/** @param transmitterId Identifikation des Datenverteilers, der sich authentifizieren m�chte */
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
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}
