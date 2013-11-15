/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.xmlSupport.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.xmlSupport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.xmlSupport; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.xmlSupport.saxPullAdapter;

import org.xml.sax.Attributes;

/**
 * Klasse zum Zugriff auf die XML-Attribute eines XML-Elements.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5009 $
 */
public class AttributeMap {

	private final String[] _names;

	private final String[] _values;

	public AttributeMap(Attributes attributes) {
		final int numberOfAttributes = attributes.getLength();
		_names = new String[numberOfAttributes];
		_values = new String[numberOfAttributes];
		for(int i = 0; i < _names.length; i++) {
			_names[i] = attributes.getLocalName(i);
			_values[i] = attributes.getValue(i);
		}
	}

	/**
	 * Gibt zu einem Namen den dazugehˆrigen Wert zur¸ck.
	 *
	 * @param name Name, f¸r den ein Wert gesucht werden soll
	 *
	 * @return Wert oder ein Leerstring ("") falls kein Wert vorhanden ist
	 */
	public String getValue(String name) {
		for(int i = 0; i < _names.length; i++) {
			if(name.equals(_names[i])) return _values[i];
		}
		return "";
	}

	/**
	 * Gibt die Anzahl Namen zur¸ck, zu denen Werte gespeichert sind.
	 *
	 * @return s.o.
	 */
	public int size() {
		return _names.length;
	}

	/**
	 * Gibt alle Namen zur¸ck, zu denen es auch Werte gibt.
	 *
	 * @return s.o.
	 */
	public String[] getNames() {
		return _names;
	}


	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('[');
		for(int i = 0; i < _names.length; i++) {
			if(i > 0) result.append(", ");
			result.append(_names[i]).append("=\"").append(_values[i]).append('"');
		}
		result.append(']');
		return result.toString();
	}
}
