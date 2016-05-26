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
package de.bsvrz.sys.funclib.crypt;

import de.bsvrz.sys.funclib.crypt.decrypt.Decrypt;
import de.bsvrz.sys.funclib.crypt.encrypt.Encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

/**
 * Klasse, die Methoden zur Ent/Verschlüssung nach PBE With MD5 And DES zur Verfügung
 *
 * @author Kappich Systemberatung
 * @version $Revision: 0 $
 */
public class PBEWithMD5AndDES implements Encrypt, Decrypt {
	private final byte[] _salt = {
			(byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
			(byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
	};
	private final int _count = 20;

	/**
	 * Zerlegt einen String in ein Array von Chars
	 *
	 * @param text String, der zerlegt werden soll
	 * @return Array von Chars
	 */
	final private char[] getChars(String text) {
		char[] passwordChars = new char[text.length()];
		text.getChars(0, text.length(), passwordChars, 0);
		return passwordChars;
	}

	/**
	 * Erzeugt aus einem String ein byte-Array. Der String wird nach ISO-8859-1 zerlegt.
	 * @param text String, der in ein byte-Array zerlegt werden soll
	 * @return byte-Array, das den übergebenen String enthält
	 * @throws UnsupportedEncodingException Falls der String nicht ISO-8859-1 konform ist
	 */
	final private byte[] getBytes(String text) throws UnsupportedEncodingException {
		return text.getBytes("ISO-8859-1");
	}

	public byte[] encrypt(String cleartext, String encryptionText) throws Exception {
		return encrypt(getBytes(cleartext),encryptionText);
	}

	public byte[] encrypt(byte[] cleartext, String encryptionText) throws Exception {
		final AlgorithmParameterSpec pbeParamSpec = new PBEParameterSpec(_salt, _count);
		final KeySpec pbeKeySpec = new PBEKeySpec(getChars(encryptionText));
		final SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		final Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		return pbeCipher.doFinal(cleartext);
	}

	public byte[] decrypt(byte[] encryptedText, String decryptionText) throws Exception {
		try {
			final AlgorithmParameterSpec pbeParamSpec = new PBEParameterSpec(_salt, _count);

			final KeySpec pbeKeySpec = new PBEKeySpec(getChars(decryptionText));

			final SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");

			final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

			final Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
			return pbeCipher.doFinal(encryptedText);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
