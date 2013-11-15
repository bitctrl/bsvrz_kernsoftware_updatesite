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
 * Gibt alle Auswahlmöglichkeiten an, die in dem Element objektReferenz (siehe K2S.DTD) und dem Attribut "undefiniert"
 * zur Verfügung stehen.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 */
public enum UndefinedReferenceOptions {
	ALLOWED("erlaubt"),
	FORBIDDEN("verboten");

	private final String _value;

	private UndefinedReferenceOptions(String value) {
		_value = value;
	}

	public String getValue() {
		return _value;
	}
}
