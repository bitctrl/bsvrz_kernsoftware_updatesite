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
 * Diese Klasse stellt ein Authentifikationsantwort Telegramm dar. Falls der Benutzer mit dem entsprechenden Passwort in der Konfiguration nicht vorhanden ist,
 * wird ein Antworttelegramm mit <code>-1</code> erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AuthentificationAnswer extends ConfigTelegram {

	/** Die Id des Benutzers */
	private long _userId;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public AuthentificationAnswer() {
		_type = AUTHENTIFICATION_ANSWER_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param userId Id des Benutzers
	 */
	public AuthentificationAnswer(long userId) {
		_type = AUTHENTIFICATION_ANSWER_TYPE;
		_userId = userId;
	}

	/**
	 * Gibt die ID des Benutzers zurück
	 *
	 * @return Id des Benutzers
	 */
	public final long getUserId() {
		return _userId;
	}

	public final String parseToString() {
		String str = "Authentifikationsantwort: \n";
		str += "Id des Benutzers: " + _userId;
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_userId);
	}

	public final void read(DataInputStream in) throws IOException {
		_userId = in.readLong();
	}
}
