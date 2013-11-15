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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Diese Klasse stellt eine Anfrage zu Authentifizierung des Benutzers dar. Es wird ein Telegamm erzeugt, welches den Benutzernamen, das verschl�sselte
 * Passwort, einen Zufallstext sowie den Namen des Verschl�sselungsverfahrens enth�lt. Wenn der Zufallstext von der Applikation korrekt verschl�sselt wurde,
 * wird als Antwort ein Authentifizierungsannahme-Telegramm an die Applikation �bertragen. Wenn der Zufallstext nicht korrekt verschl�sselt wurde, wird ein
 * Authentifizierungsablehnungs-Telegramm gesendet und eine neues Authentifizierungsbereitschafts-Telegramm von der Applikation erwartet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class AuthentificationRequest extends ConfigTelegram {

	/** Der Benutzername */
	private String _userName;

	/** Das verschl�sselte Passwort */
	private byte _encriptedPassword[];

	/** Der Authentifizierungszufallstext */
	private String _text;

	/** Das Authentifizierungsverfahren */
	private String _processName;

	/** Erzeugt ein neues Objekt ohne Parameter. Die parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public AuthentificationRequest() {
		_type = AUTHENTIFICATION_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param userName          Benutzername
	 * @param encriptedPassword verschl�sseltesPasswort
	 * @param text              Zufallstext
	 * @param processName       Authentifizierungsprozessname
	 */
	public AuthentificationRequest(
			String userName, byte encriptedPassword[], String text, String processName
	) {
		_type = AUTHENTIFICATION_REQUEST_TYPE;
		_userName = userName;
		_encriptedPassword = encriptedPassword;
		_text = text;
		_processName = processName;
	}

	/**
	 * Gibt den Benutzernamen zur�ck.
	 *
	 * @return der Benutzername
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * Gibt das verschl�sselte passwort zur�ck.
	 *
	 * @return das verschl�sselte Passwort
	 */
	public final byte[] getEncriptedPasswort() {
		return _encriptedPassword;
	}

	/**
	 * Gibt den Authentifizierungstext zur�ck.
	 *
	 * @return der Authentifizierungstext
	 */
	public final String getAuthentificationText() {
		return _text;
	}

	/** @return das Authentifizierungsverfahren */
	public final String getAuthentificationProcessName() {
		return _processName;
	}

	public final String parseToString() {
		String str = "Authentifizierunganfrage: \n";
		str += "Benutzername: " + _userName + "\n";
		str += "Passwort: " + _encriptedPassword + "\n";
		str += "Authentifizierungstext: " + _text + "\n";
		str += "Authentifizierungsverfahren: " + _processName + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeUTF(_userName);
		out.writeInt(_encriptedPassword.length);
		for(int i = 0; i < _encriptedPassword.length; ++i) {
			out.writeByte(_encriptedPassword[i]);
		}
		out.writeUTF(_text);
		out.writeUTF(_processName);
	}

	public final void read(DataInputStream in) throws IOException {
		_userName = in.readUTF();
		int size = in.readInt();
		_encriptedPassword = new byte[size];
		for(int i = 0; i < size; ++i) {
			_encriptedPassword[i] = in.readByte();
		}
		_text = in.readUTF();
		_processName = in.readUTF();
	}
}
