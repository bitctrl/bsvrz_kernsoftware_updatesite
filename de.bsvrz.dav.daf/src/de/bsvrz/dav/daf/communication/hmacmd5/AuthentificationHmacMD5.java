/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.hmacmd5;

import de.bsvrz.sys.funclib.crypt.EncryptDecryptProcedure;
import de.bsvrz.dav.daf.communication.lowLevel.AuthentificationProcess;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Diese Klasse implementiert ein Verfahren zur Authentifizierung mittels der kryptographischen Hashfunktion MD5. Das Verfahren bildet aus einer Nachricht und
 * einem geheimen Schlüssel eine Signatur, die über ein unsicheres Medium übertragen werden kann und vom Empfänger der Nachricht auf Echtheit überprüft werden
 * kann. Das HMAC Verfahren kann mit verschiedenen Hashfunktionen benutzt werden. Hier wird es mit dem kryptographischen Verfahren MD5 verwendet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5047 $
 */
public class AuthentificationHmacMD5 extends AuthentificationProcess {

	/**
	 * Erzeugt ein neues Objekt.
	 */
	public AuthentificationHmacMD5() {
		name = EncryptDecryptProcedure.HmacMD5.getName();
	}

	public final synchronized byte[] encrypt(String password, String text) {
		if((password == null) || (text == null)) {
			return null;
		}
		try {
			final String charsetName = "ISO-8859-1";
			SecretKey sk = new SecretKeySpec(password.getBytes(charsetName), name);
			// Get instance of Mac object implementing HMAC-MD5, and
			// initialize it with the above secret key
			Mac mac = Mac.getInstance(name);
			mac.init(sk);
			return mac.doFinal(text.getBytes(charsetName));
		}
		catch(UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		catch(NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		catch(InvalidKeyException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
