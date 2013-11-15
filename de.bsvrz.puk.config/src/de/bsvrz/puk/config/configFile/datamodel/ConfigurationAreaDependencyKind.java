/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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

package de.bsvrz.puk.config.configFile.datamodel;

/**
 * Gibt an, ob die Abh�ngigkeiten zwischen zwei Konfigurationsbereichen "Optional" oder "Zwingend" sind.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5074 $
 */
public enum ConfigurationAreaDependencyKind {

	OPTIONAL("optional", (byte)0),
	REQUIRED("notwendig", (byte)1);

	private final String _value;

	private final byte _code;

	public static final ConfigurationAreaDependencyKind getInstance(byte code) {
		switch(code) {
			case 0:
				return OPTIONAL;
			case 1:
				return REQUIRED;
			default: {
				throw new IllegalArgumentException("Unbekannte Kodierung: " + code);
			}
		}
	}


	public static final ConfigurationAreaDependencyKind getInstance(final String name) {
		final String trimmedString = name.toLowerCase().trim();

		if(trimmedString.equals(OPTIONAL.getValue())) {
			return OPTIONAL;
		}
		else if(trimmedString.equals(REQUIRED.getValue())) {
			return REQUIRED;
		}
		else {
			throw new IllegalArgumentException("Unbekannter Name: " + name);
		}
	}

	private ConfigurationAreaDependencyKind(String value, byte code) {
		_value = value;
		_code = code;
	}

	public String getValue() {
		return _value;
	}

	public byte getCode() {
		return _code;
	}


	public String toString() {
		return _value;
	}
}
