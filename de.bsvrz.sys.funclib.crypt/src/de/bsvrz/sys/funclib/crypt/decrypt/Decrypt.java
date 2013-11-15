/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.crypt.
 * 
 * de.bsvrz.sys.funclib.crypt is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.crypt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.crypt; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.crypt.decrypt;

/**
 * Eine Implementierung stellt ein Objekt zur Verf�gung, das einen verschl�sselten Text wieder
 * entschl�sselt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5010 $
 */
public interface Decrypt {
	/**
	 * Eine Implementierung dieser Methode entschl�sselt einen verschl�sselten Text mit einem bestimmten Verfahren.
	 * @param encryptedText Text, der verschl�sselt ist und entschl�sselt werden soll
	 * @param decryptionText Text, der zum entschl�sseln der Daten benutzt werden soll, dieser Text wurde auch zum verschl�sseln benutzt.
	 * @return Entschl�sselter Text
	 * @throws Exception Fehler, die beim entschl�sseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.encrypt.Encrypt
	 */
	byte[] decrypt(byte[] encryptedText, String decryptionText) throws Exception;
}
