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
 * Informationen, die nach der erfolgreichen Authentifizierung übergeben werden. Nach der erfolgreichen Authentifizierung sendet der Datenverteiler, der die
 * Authentifizierung akzeptiert hat, seinem Kommunikationspartner seine eigene Objekt-ID.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterAuthentificationAnswer extends DataTelegram {

	/** Erfolgreich authentifiziert <code>true / false</code> */
	private boolean successfullyAuthentified;

	/** Die ID des Partnerdatenverteilers */
	private long communicationTransmitterId;

	public TransmitterAuthentificationAnswer() {
		type = TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues TransmitterAuthentificationAnswer
	 *
	 * @param _successfullyAuthentified   Status der Authentiizierung
	 * @param _communicationTransmitterId ID des Partnerdatenverteilers
	 */
	public TransmitterAuthentificationAnswer(boolean _successfullyAuthentified, long _communicationTransmitterId) {
		type = TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		successfullyAuthentified = _successfullyAuthentified;
		communicationTransmitterId = _communicationTransmitterId;
		length = 9;
	}

	/**
	 * Gibt an, ob der Authentifizierungsvorgang erfolgreich abgeschlossen ist.
	 *
	 * @return <code>true / false</code>
	 */
	public final boolean isSuccessfullyAuthentified() {
		return successfullyAuthentified;
	}

	/**
	 * Gibt die ID des Partnerdatenverteilers an
	 *
	 * @return ID des Partnerdatenverteilers
	 */
	public final long getCommunicationTransmitterId() {
		return communicationTransmitterId;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikation Antwort:   \n";
		if(successfullyAuthentified) {
			str += "Status: Erfolgreich\n";
		}
		else {
			str += "Status: Erfolglos\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeBoolean(successfullyAuthentified);
		out.writeLong(communicationTransmitterId);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		successfullyAuthentified = in.readBoolean();
		communicationTransmitterId = in.readLong();
		length = 9;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
