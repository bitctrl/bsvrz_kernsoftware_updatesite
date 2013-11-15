/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

/**
 * Diese Klasse dient als Schnittstelle f�r ein Verfahren zur Authentifizierung mittels einer kryptographischen Funktion. Das Verfahren bildet aus einer
 * Nachricht und einem geheimen Schl�ssel eine Signatur, die �ber ein unsicheres Medium �bertragen werden kann und vom Empf�nger der Nachricht auf Echtheit
 * �berpr�ft werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5047 $
 */
public abstract class AuthentificationProcess {

	/** Der Name des Authentifizierungsverfahrens. */
	protected String name;

	/**
	 * Gibt den Namen des Authentifizierungsverfahrens zur�ck.
	 *
	 * @return der Name des Authentifizierungsverfahrens
	 */
	public final String getName() {
		return new String(name);
	}

	/**
	 * Verschl�sselt den Text mit Hilfe des Passworts.
	 *
	 * @param password das Passwort
	 * @param text     den zu verschl�sselnden Text
	 *
	 * @return Das Ergebnis der Verschl�sselung oder <code>null</code>, wenn der Text nicht verschl�sselt werden konnte.
	 */
	public abstract byte[] encrypt(String password, String text);
}
