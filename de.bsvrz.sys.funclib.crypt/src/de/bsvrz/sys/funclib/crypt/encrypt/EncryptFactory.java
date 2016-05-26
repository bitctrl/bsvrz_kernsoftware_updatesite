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

import de.bsvrz.sys.funclib.crypt.PBEWithMD5AndDES;
import de.bsvrz.sys.funclib.crypt.EncryptDecryptProcedure;
import de.bsvrz.sys.funclib.crypt.hmacmd5.HmacMD5;

/**
 * Diese Klasse stellt Objekt zur Verfügung, die Texte verschlüsseln können.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class EncryptFactory {
	/**
	 * Erzeugt ein Objekt, mit dem Strings verschlüsselt werden können.
	 *
	 * @param encryptPractice Verfahren, mit dem die Daten verschlüsselt werden sollen
	 * @return Objekt zum verschlüsseln von Strings
	 * @throws IllegalArgumentException Das angegebene Verfahren ist unbekannt
	 */
	public static final Encrypt getEncryptInstance(final EncryptDecryptProcedure encryptPractice) {
		if (encryptPractice == EncryptDecryptProcedure.PBEWithMD5AndDES) {
			return new PBEWithMD5AndDES();
		} else if (encryptPractice == EncryptDecryptProcedure.HmacMD5) {
			return new HmacMD5();
		} else {
			throw new IllegalArgumentException("Unbekanntes Entschlüsslungsverfahren " + encryptPractice.getName());
		}
	}
}
