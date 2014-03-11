/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Definiert eine Zeichenkette nach der K2S.DTD
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationString implements ConfigurationAttributeType {
	/**
	 * Länge der Zeichenkette
	 */
	final int _length;
	/**
	 * Wie wird der String kodiert
	 */
	String _stringEncoding = "ISO-8859-1";

	/**
	 *
	 * @param length Attribut "laenge", der String wird als Integer interpretiert und umgewandelt
	 */
	public ConfigurationString(String length) {
		_length = Integer.parseInt(length);
	}

	/**
	 *
	 * @param maxLength Attribut "laenge"
	 */
	public ConfigurationString(int maxLength) {
		_length = maxLength;
	}

	/**
	 * Attribut kodierung
	 * @param stringEncoding s.o
	 */
	public void setStringEncoding(String stringEncoding) {
		if (!"".equals(stringEncoding)) {
			_stringEncoding = stringEncoding;
		}
	}

	/**
	 *
	 * @return Attribut "laenge"
	 */
	public int getLength() {
		return _length;
	}

	/**
	 * Attribut "kodierung"
	 * @return Wert, der gesetzt wurde. Wurde kein Wert gesetzt, wird ISO-8859-1 benutzt und zurückgegeben
	 */
	public String getStringEncoding() {
		return _stringEncoding;
	}
}
