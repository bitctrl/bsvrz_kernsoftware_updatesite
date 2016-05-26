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
 * Mit diesem Systemtelegramm authentifiziert sich eine Applikation beim Datenverteiler. Die Datenverteiler-Applikationsfunktionen senden dazu den Namen des
 * eingesetzten Authentifizierungsverfahren und die Benutzerkennung zusammen mit dem verschlüsselten Zufallstext an den Datenverteiler.
 *
 * @author Kappich Systemberatung
 */
public class AuthentificationRequest extends DataTelegram {

	/** Der Authentifikationsverfahren-Name */
	private String authentificationProcess;

	/** Der Benutzer Name */
	private String userName;

	/** Das verschlusselte Benutzer-Passwort */
	private byte userPassword[];

	public AuthentificationRequest() {
		type = AUTHENTIFICATION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * @param _authentificationProcess      Authentifikationsverfahren-Name
	 * @param _userName                     Benutzername
	 * @param _userPassword                 Benutzerpasswort
	 */
	public AuthentificationRequest(String _authentificationProcess, String _userName, byte _userPassword[]) {
		type = AUTHENTIFICATION_REQUEST_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		authentificationProcess = _authentificationProcess;
		userName = _userName;
		userPassword = _userPassword;
		length = 4;
		try {
			if(authentificationProcess == null) {
				authentificationProcess = "";
			}
			length += authentificationProcess.getBytes("UTF-8").length + 2;

			if(userName == null) {
				userName = "";
			}
			length += userName.getBytes("UTF-8").length + 2;

			if(userPassword == null) {
				userPassword = new byte[0];
			}
			length += userPassword.length;
		}
		catch(java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex.getLocalizedMessage());
		}
	}

	/**
	 * Gibt den Authentifikationsverfahren-Name zurück
	 *
	 * @return Authentifikationsverfahren-Name
	 */
	public final String getAuthentificationProcessName() {
		return authentificationProcess;
	}

	/** Gibt den Benutzername zurück
	 *  @return Benutzername
	 */
	public final String getUserName() {
		return userName;
	}

	/** Gibt das verschlusselte Benutzer-Passwort zurück
	 *
	 @return Benutzerpasswort
	 *
	 */
	public final byte[] getUserPassword() {
		return userPassword;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikation Anfrage:   \n";
		str += "Authentifikationsverfahren : " + authentificationProcess + "\n";
		str += "Benutzer Name              : " + userName + "\n";
		str += "Benutzer Passwort          : *******************************\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeUTF(authentificationProcess);
		out.writeUTF(userName);
		out.writeInt(userPassword.length);
		for(int i = 0; i < userPassword.length; ++i) {
			out.writeByte(userPassword[i]);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		authentificationProcess = in.readUTF();
		userName = in.readUTF();
		int size = in.readInt();
		userPassword = new byte[size];
		for(int i = 0; i < size; ++i) {
			userPassword[i] = in.readByte();
		}
		length = size + 4;
		length += authentificationProcess.getBytes("UTF-8").length + 2;
		length += userName.getBytes("UTF-8").length + 2;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
