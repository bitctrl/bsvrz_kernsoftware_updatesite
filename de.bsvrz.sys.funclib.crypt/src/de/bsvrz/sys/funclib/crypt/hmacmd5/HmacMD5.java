/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
package de.bsvrz.sys.funclib.crypt.hmacmd5;

import de.bsvrz.sys.funclib.crypt.encrypt.Encrypt;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Diese Klasse verschl�sselt einen Text nach HmacMD5.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5010 $
 */
public class HmacMD5 implements Encrypt {

	public byte[] encrypt(String cleartext, String encryptionText) throws Exception {
		return encrypt(cleartext.getBytes("ISO-8859-1"), encryptionText);
	}

	public byte[] encrypt(byte[] cleartext, String encryptionText) throws Exception {
		final SecretKey sk = new SecretKeySpec(cleartext, "HmacMD5");
		final Mac mac = Mac.getInstance("HmacMD5");
		mac.init(sk);
		return mac.doFinal(encryptionText.getBytes("ISO-8859-1"));
	}
}
