/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
package de.bsvrz.dav.daf.main.archive;

/**
 * Diese Klasse stellt Objekte zur Verfügung über die definiert werden kann ob die Archivanfrage eine Deltaanfrage oder
 * eine Zustandsanfrage ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ArchiveRequestOption {
	/**
	 * Zustandsanfrage
	 */
	public static final ArchiveRequestOption NORMAL = new ArchiveRequestOption("Zustandsanfrage", 1);
	/**
	 * Deltaanfrage
	 */
	public static final ArchiveRequestOption DELTA = new ArchiveRequestOption("Deltaanfrage", 2);

	/**
	 * Diese Methode wandelt den übergebenen Wert in ein identisches Objekt dieser Klasse um.
	 *
	 * @param code Code, der das zurückgegebene Objekt bestimmt
	 * @return Referenz auf ein Objekt dieser Klasse
	 */
	public static ArchiveRequestOption getInstance(int code) {
		switch (code) {
			case 1:
				return NORMAL;
			case 2:
				return DELTA;
			default:
				throw new IllegalArgumentException("Undefinierte Anfrage");
		}
	}

	/**
	 * @return Gibt das Objekt als String zurück
	 */
	public String toString() {
		return _name;
	}

	/**
	 * Gibt den Code des Objekts zurück, dieser kann dazu verwendet werden um wieder das identische Objekt
	 * zu erzeugen.
	 * @return Code des Objekts
	 */
	public int getCode() {
		return _code;
	}

	private final String _name;
	private final int _code;

	private ArchiveRequestOption(String name, int code) {
		_name = name;
		_code = code;
	}
}
