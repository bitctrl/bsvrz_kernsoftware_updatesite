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

import de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Aufforderung zur Authentifizierung. Dieses Systemtelegramm wird als Reaktion auf die Authentifizierungsbereitschaft gesendet. Mit diesem Systemtelegramm wird
 * der Kommunikationspartner, der seine Authentifizierungsbereitschaft signalisiert hat, aufgefordert sich zu authentifizieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterAuthentificationTextAnswer extends DataTelegram {

	/** Der Authentifikationszufallstext */
	private String _text;

	public TransmitterAuthentificationTextAnswer() {
		type = TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/** @param text Der Authentifikationszufallstext */
	public TransmitterAuthentificationTextAnswer(String text) {
		type = TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_text = text;
		length = 0;
		try {
			if(_text == null) {
				_text = "";
			}
			length += _text.getBytes("UTF-8").length + 2;
		}
		catch(UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage());
		}
	}

	/**
	 * Verschlüsselt den Text des Telegramms mit dem Passwort.
	 *
	 * @param authentificationProcess Authentfikations-Prozess
	 * @param password                Passwort
	 *
	 * @return entschlüsseltes Passwort
	 */
	public final byte[] getEncryptedPassword(AuthentificationProcess authentificationProcess, String password) {
		return authentificationProcess.encrypt(password, _text);
	}

	public final String parseToString() {
		return "Systemtelegramm Authentifikationsschlussel Antwort: \n";
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeUTF(_text);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_text = in.readUTF();
		length = _text.getBytes("UTF-8").length + 2;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
