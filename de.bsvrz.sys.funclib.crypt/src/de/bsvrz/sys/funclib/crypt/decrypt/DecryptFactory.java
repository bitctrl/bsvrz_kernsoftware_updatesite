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

import de.bsvrz.sys.funclib.crypt.EncryptDecryptProcedure;
import de.bsvrz.sys.funclib.crypt.PBEWithMD5AndDES;

/**
 * Diese Klasse stellt Objekt zur Verf�gung, die verschl�sselte Texte entschl�sseln k�nnen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5010 $
 */
public class DecryptFactory {

	/**
	 * Erzeugt ein Objekt, mit dem Strings verschl�sselt werden k�nnen.
	 * @param decryptPractice Verfahren, mit dem die Daten verschl�sselt werden sollen
	 * @return Objekt zum verschl�sseln von Strings
	 *
	 * @throws IllegalArgumentException Das angegebene Verfahren ist unbekannt
	 */
	public static final Decrypt getDecryptInstance(EncryptDecryptProcedure decryptPractice) {
		if(decryptPractice == EncryptDecryptProcedure.PBEWithMD5AndDES)
		{
			return new PBEWithMD5AndDES();
		}else
		{
			throw new IllegalArgumentException("Unbekanntes Verschl�sslungsverfahren " + decryptPractice.getName());
		}
	}
}
