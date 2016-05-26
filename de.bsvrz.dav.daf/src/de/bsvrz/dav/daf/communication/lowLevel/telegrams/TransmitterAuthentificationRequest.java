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
import java.io.UnsupportedEncodingException;

/**
 * Übermittelung der Authentifizierungsdaten. Mit diesem Systemtelegramm authentifiziert sich der Datenverteiler bei seinem Kommunikationspartner. Er sendet
 * dazu den Namen des eingesetzten Authentifizierungsverfahrens und die Benutzerkennung, mit der er gestartet wurde, zusammen mit dem verschlüsselten
 * Zufallstext an seinen Kommunikationspartner.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterAuthentificationRequest extends DataTelegram {

	/** Der Authentifikationsverfahren-Name */
	private String _authentificationProcess;

	/** Der Benutzernname */
	private String _userName;

	/** Das verschlüsselte Benutzerpasswort */
	private byte _userPassword[];

	public TransmitterAuthentificationRequest() {
		type = TRANSMITTER_AUTHENTIFICATION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt ein neues TransmitterAuthentificationRequest-Telegramm.
	 *
	 * @param authentificationProcess Authentifikationsverfahren-Name
	 * @param userName                Benutzername
	 * @param userPassword            Passwort(veschlüsselt)
	 */
	public TransmitterAuthentificationRequest(String authentificationProcess, String userName, byte userPassword[]) {
		type = TRANSMITTER_AUTHENTIFICATION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_authentificationProcess = authentificationProcess;
		_userName = userName;
		_userPassword = userPassword;
		length = 4;
		try {
			if(_authentificationProcess == null) {
				_authentificationProcess = "";
			}
			length += _authentificationProcess.getBytes("UTF-8").length + 2;
			if(_userName == null) {
				_userName = "";
			}
			length += _userName.getBytes("UTF-8").length + 2;
			if(_userPassword == null) {
				_userPassword = new byte[0];
			}
			length += _userPassword.length;
		}
		catch(UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage());
		}
	}

	/**
	 * Gibt den Authentifikationsverfahren-Namen zurück.
	 *
	 * @return Authentifikationsverfahren-Namen
	 */
	public final String getAuthentificationProcessName() {
		return _authentificationProcess;
	}

	/**
	 * Gibt den Benutzername zurück.
	 *
	 * @return Benutzername
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * Gibt das verschlüsselte Benutzerpasswort zurück.
	 *
	 * @return Benutzerpasswort
	 */
	public final byte[] getUserPassword() {
		return _userPassword;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikation Anfrage:   \n";
		str += "Authentifikationsverfahren : " + _authentificationProcess + "\n";
		str += "Benutzer Name              : " + _userName + "\n";
		str += "Benutzer Passwort          : *******************************\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeUTF(_authentificationProcess);
		out.writeUTF(_userName);
		out.writeInt(_userPassword.length);
		for(int i = 0; i < _userPassword.length; ++i) {
			out.writeByte(_userPassword[i]);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_authentificationProcess = in.readUTF();
		_userName = in.readUTF();
		int size = in.readInt();
		_userPassword = new byte[size];
		for(int i = 0; i < size; ++i) {
			_userPassword[i] = in.readByte();
		}
		length = size + 4;
		length += _authentificationProcess.getBytes("UTF-8").length + 2;
		length += _userName.getBytes("UTF-8").length + 2;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
