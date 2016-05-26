/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.crypt.
 * 
 * de.bsvrz.sys.funclib.crypt is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.crypt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.crypt; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.crypt.decrypt;

/**
 * Eine Implementierung stellt ein Objekt zur Verfügung, das einen verschlüsselten Text wieder
 * entschlüsselt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface Decrypt {
	/**
	 * Eine Implementierung dieser Methode entschlüsselt einen verschlüsselten Text mit einem bestimmten Verfahren.
	 * @param encryptedText Text, der verschlüsselt ist und entschlüsselt werden soll
	 * @param decryptionText Text, der zum entschlüsseln der Daten benutzt werden soll, dieser Text wurde auch zum verschlüsseln benutzt.
	 * @return Entschlüsselter Text
	 * @throws Exception Fehler, die beim entschlüsseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.encrypt.Encrypt
	 */
	byte[] decrypt(byte[] encryptedText, String decryptionText) throws Exception;
}
