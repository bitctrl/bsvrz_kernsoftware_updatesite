/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

/**
 * Diese Klasse dient als Schnittstelle für ein Verfahren zur Authentifizierung mittels einer kryptographischen Funktion. Das Verfahren bildet aus einer
 * Nachricht und einem geheimen Schlüssel eine Signatur, die über ein unsicheres Medium übertragen werden kann und vom Empfänger der Nachricht auf Echtheit
 * überprüft werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class AuthentificationProcess {

	/** Der Name des Authentifizierungsverfahrens. */
	protected String name;

	/**
	 * Gibt den Namen des Authentifizierungsverfahrens zurück.
	 *
	 * @return der Name des Authentifizierungsverfahrens
	 */
	public final String getName() {
		return new String(name);
	}

	/**
	 * Verschlüsselt den Text mit Hilfe des Passworts.
	 *
	 * @param password das Passwort
	 * @param text     den zu verschlüsselnden Text
	 *
	 * @return Das Ergebnis der Verschlüsselung oder <code>null</code>, wenn der Text nicht verschlüsselt werden konnte.
	 */
	public abstract byte[] encrypt(String password, String text);
}
