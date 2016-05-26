/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Stellt ein Objekt zur Verfügung, das ein "datum" abbildet, wie es in der K2S.DTD definiert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigurationData implements DatasetElement {

	private final String _name;

	private String _value;

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

	/**
	 * Attribut "wert"
	 *
	 * @param value neues Datum
	 */
	public void setValue(final String value) {
		_value = value;
	}
}
