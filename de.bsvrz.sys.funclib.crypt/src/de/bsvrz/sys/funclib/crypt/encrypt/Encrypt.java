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

package de.bsvrz.sys.funclib.crypt.encrypt;

/**
 * Eine Implementierung stellt ein Objekt zur Verfügung, das einen String verschlüsselt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface Encrypt {

	/**
	 * Verschlüsselt einen Text
	 *
	 * @param cleartext	  Text, der verschlüsselt werden soll. Der Text muss ISO-8859-1 konform sein.
	 * @param encryptionText Text, der benutzt werden soll um den Klartext zu verschlüsseln. Dieser Text wird ebenfalls zum
	 *                       entschlüsseln benötigt.
	 * @return Verschlüsselter Text
	 * @throws Exception Fehler, die beim verschlüsseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.decrypt.Decrypt
	 */
	byte[] encrypt(String cleartext, String encryptionText) throws Exception;

	/**
	 * Verschlüsselt einen Text
	 *
	 * @param cleartext	  Text, der verschlüsselt werden soll. Der Text muss ISO-8859-1 konform sein.
	 * @param encryptionText Text, der benutzt werden soll um den Klartext zu verschlüsseln. Dieser Text wird ebenfalls zum
	 *                       entschlüsseln benötigt.
	 * @return Verschlüsselter Text
	 * @throws Exception Fehler, die beim verschlüsseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.decrypt.Decrypt
	 */
	byte[] encrypt(byte[] cleartext, String encryptionText) throws Exception;
}
