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
 * Diese Klasse stellt eine Anfrage zu Authentifizierung des Benutzers dar. Es wird ein Telegamm erzeugt, welches den Benutzernamen, das verschlüsselte
 * Passwort, einen Zufallstext sowie den Namen des Verschlüsselungsverfahrens enthält. Wenn der Zufallstext von der Applikation korrekt verschlüsselt wurde,
 * wird als Antwort ein Authentifizierungsannahme-Telegramm an die Applikation übertragen. Wenn der Zufallstext nicht korrekt verschlüsselt wurde, wird ein
 * Authentifizierungsablehnungs-Telegramm gesendet und eine neues Authentifizierungsbereitschafts-Telegramm von der Applikation erwartet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AuthentificationRequest extends ConfigTelegram {

	/** Der Benutzername */
	private String _userName;

	/** Das verschlüsselte Passwort */
	private byte _encriptedPassword[];

	/** Der Authentifizierungszufallstext */
	private String _text;

	/** Das Authentifizierungsverfahren */
	private String _processName;

	/** Erzeugt ein neues Objekt ohne Parameter. Die parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public AuthentificationRequest() {
		_type = AUTHENTIFICATION_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param userName          Benutzername
	 * @param encriptedPassword verschlüsseltesPasswort
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
	 * Gibt den Benutzernamen zurück.
	 *
	 * @return der Benutzername
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * Gibt das verschlüsselte passwort zurück.
	 *
	 * @return das verschlüsselte Passwort
	 */
	public final byte[] getEncriptedPasswort() {
		return _encriptedPassword;
	}

	/**
	 * Gibt den Authentifizierungstext zurück.
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
