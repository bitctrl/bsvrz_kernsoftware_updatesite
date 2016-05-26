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

/**
 * Mögliche Verfahren, mit denen verschlüsselt und/oder entschlüsselt werden kann
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public enum EncryptDecryptProcedure {
	PBEWithMD5AndDES("PBEWithMD5AndDES"),
	HmacMD5("HmacMD5");

	final static EncryptDecryptProcedure getInstance()
	{
		return null;
	}

	private final String _name;

	/**
	 * @param name Name des Verfahrens
	 */
	private EncryptDecryptProcedure(String name) {
		_name = name;
	}

	/**
	 * @return Gibt den Namen eines Verfahrens, mit dem verschlüsselt oder entschlüsselt werden kann, zurück
	 */
	public String getName() {
		return _name;
	}
}
