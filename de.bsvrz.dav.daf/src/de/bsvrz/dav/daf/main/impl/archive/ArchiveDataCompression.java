/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.dav.daf.main.impl.archive;

/**
 * Ein Objekt dieser Klasse zeigt an, welche Art vom Kompression zum packen der Daten benutzt wurde.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ArchiveDataCompression {
	/**
	 * Die Daten wurden nicht gepackt
	 */
	public static final ArchiveDataCompression NONE = new ArchiveDataCompression("nicht komprimiert", 1);
	/**
	 * Die Daten wurden mit dem Verfahren "ZIP" gepackt
	 */
	public static final ArchiveDataCompression ZIP = new ArchiveDataCompression("ZIP komprimiert", 2);

	/**
	 * Diese Methode wandelt den übergebenen Parameter in ein Objekt dieser Klasse um
	 * @param code Der Code bestimmt welches Objekt dieser Klasse erzeugt wird
	 * @return eindeutiges Objekt dieser Klasse
	 */
	public static ArchiveDataCompression getInstance(int code) {

		switch (code) {
			case 1:
				return NONE;
			case 2:
				return ZIP;
			default:
				throw new IllegalArgumentException("Undefinierte Kompression");
		}
	}

	/**
	 * Wandelt das Objekt in einen String um
	 * @return String, der ausgegeben werden kann
	 */
	public String toString() {
		return _name;
	}

	/**
	 * Code des Objekts, dieser Code kann zum erzeugen eines identischen Objekts benutzt werden.
	 * @return Code
	 */
	public byte getCode() {
		return (byte)_code;
	}

	private final String _name;
	private final int _code;

	private ArchiveDataCompression(String name, int code) {
		_name = name;
		_code = code;
	}
}
