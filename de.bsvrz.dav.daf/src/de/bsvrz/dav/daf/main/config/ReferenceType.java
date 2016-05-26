/*
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Von der Konfiguration werden folgende Referenzierungsarten unterschieden: <ul> <li>Gerichtete Assoziation</li>
 * <li>Aggregation</li> <li>Komposition</li> </ul>
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public enum ReferenceType {
	/**
	 * Gerichtete Assoziationen sind im Zusammenhang mit der Konfiguration die einfachste Form der Referenzierung.
	 */
	ASSOCIATION("assoziation"),

	/**
	 * Aggregationen sind eine spezielle Form von gerichteten Assoziationen, bei der die referenzierten Objekte Teile des
	 * referenzierenden Aggregats darstellen.
	 */
	AGGREGATION("aggregation"),

	/**
	 * Kompositionen sind eine spezielle Form von Aggregationen mit existentieller Abhängigkeit der referenzierten
	 * Objekte.
	 */
	COMPOSITION("komposition");

	private final String _value;

	private ReferenceType(String value) {
		_value = value;
	}

	public String getValue() {
		return _value;
	}
}
