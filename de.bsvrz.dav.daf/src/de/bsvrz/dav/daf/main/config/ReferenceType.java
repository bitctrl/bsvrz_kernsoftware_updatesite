/*
 * Copyright 2006 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Von der Konfiguration werden folgende Referenzierungsarten unterschieden: <ul> <li>Gerichtete Assoziation</li>
 * <li>Aggregation</li> <li>Komposition</li> </ul>
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
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
	 * Kompositionen sind eine spezielle Form von Aggregationen mit existentieller Abh‰ngigkeit der referenzierten
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
