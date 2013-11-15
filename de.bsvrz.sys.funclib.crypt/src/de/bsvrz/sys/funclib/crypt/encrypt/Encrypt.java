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

package de.bsvrz.sys.funclib.crypt.encrypt;

/**
 * Eine Implementierung stellt ein Objekt zur Verf�gung, das einen String verschl�sselt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5010 $
 */
public interface Encrypt {

	/**
	 * Verschl�sselt einen Text
	 *
	 * @param cleartext	  Text, der verschl�sselt werden soll. Der Text muss ISO-8859-1 konform sein.
	 * @param encryptionText Text, der benutzt werden soll um den Klartext zu verschl�sseln. Dieser Text wird ebenfalls zum
	 *                       entschl�sseln ben�tigt.
	 * @return Verschl�sselter Text
	 * @throws Exception Fehler, die beim verschl�sseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.decrypt.Decrypt
	 */
	byte[] encrypt(String cleartext, String encryptionText) throws Exception;

	/**
	 * Verschl�sselt einen Text
	 *
	 * @param cleartext	  Text, der verschl�sselt werden soll. Der Text muss ISO-8859-1 konform sein.
	 * @param encryptionText Text, der benutzt werden soll um den Klartext zu verschl�sseln. Dieser Text wird ebenfalls zum
	 *                       entschl�sseln ben�tigt.
	 * @return Verschl�sselter Text
	 * @throws Exception Fehler, die beim verschl�sseln aufgetreten sind
	 * @see de.bsvrz.sys.funclib.crypt.decrypt.Decrypt
	 */
	byte[] encrypt(byte[] cleartext, String encryptionText) throws Exception;
}
