/*
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Diese Klasse stellt einen Zustand dar, der in der K2S.DTD definiert ist
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationState implements ConfigurationIntegerValueRange{
	SystemObjectInfo _info;
	final String _name;
	final long _value;

	/**
	 * @param name  Attribut "name", <code>null</code> ist nicht erlaubt
	 * @param value Attribut "wert"
	 */
	public ConfigurationState(String name, long value) {
		_name = name;
		_value = value;
	}

	public ConfigurationState(String name, String value) {
		_name = name;
		_value = Long.parseLong(value);
	}

	/**
	 * Attribut "info"
	 * @param info s.o.
	 */
	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	/**
	 * Attribut "info"
	 * @return info oder <code>null</code> falls der Wert nicht gesetzt ist
	 */
	public SystemObjectInfo getInfo() {
		return _info;
	}

	/**
	 * Attribut "name"
	 * @return s.o.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Attribut "wert"
	 * @return s.o.
	 */
	public long getValue() {
		return _value;
	}
}
