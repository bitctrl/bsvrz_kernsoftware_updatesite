/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
 * Stellt ein Objekt zur Verf�gung, das ein "datum" abbildet, wie es in der K2S.DTD definiert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5091 $
 */
public class ConfigurationData implements DatasetElement {

	private final String _name;

	private final String _value;

	public ConfigurationData(String name, String value) {
		_name = name;
		_value = value;
	}

	/**
	 * Attribut "name"
	 *
	 * @return s.o.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Attribut "wert"
	 *
	 * @return s.o.
	 */
	public String getValue() {
		return _value;
	}
}
