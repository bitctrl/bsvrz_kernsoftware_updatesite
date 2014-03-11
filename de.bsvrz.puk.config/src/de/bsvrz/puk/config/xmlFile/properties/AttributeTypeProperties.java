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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Stellt eine attributDefinition nach der K2S.DTD dar.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 */
public class AttributeTypeProperties extends ConfigurationObjectProperties {

	/**
	 * Dieses Objekt speichert ein Objekt, das eine Zeichenkette, Ganzzahl, Zeitstempel, ObjekReferenz, Kommazahl oder ein
	 * default sein kann.
	 */
	private ConfigurationAttributeType _attributeType = null;

	private String _default = null;


	public AttributeTypeProperties(String name, String pid, long id, String typePid, SystemObjectInfo info) {
		super(name, pid, id, typePid, info);
	}

	/**
	 * Gibt ein Objekt zurück, das folgende Attributtypen darstellt: zeichenkette, ganzzahl, zeitstempel, objektReferenz,
	 * kommazahl
	 *
	 * @return Objekt, das folgende Typen besitzen kann: ConfigurationString, ConfigurationIntegerDef,
	 *         ConfigurationTimeStamp, ConfigurationObjectReference, ConfigurationDoubleDef, oder <code>null</code> falls
	 *         kein Attributtyp festgelegt wurde
	 */
	public ConfigurationAttributeType getAttributeType() {
		return _attributeType;
	}

	/**
	 *
	 * @param attributeType Objekt, das folgende Typen besitzen kann: ConfigurationString, ConfigurationIntegerDef,
	 *         ConfigurationTimeStamp, ConfigurationObjectReference, ConfigurationDoubleDef, oder <code>null</code> falls
	 *         kein Attributtyp festgelegt wurde
	 */
	public void setAttributeType(ConfigurationAttributeType attributeType) {
		_attributeType = attributeType;
	}

	/**
	 * Deafult-Wert als String oder <code>null</code> falls der Wert nicht gesetzt wurde
	 *
	 * @return Wert, der im Element default steht.
	 */
	public String getDefault() {
		return _default;
	}

	/**
	 * Stellt ein Element default nach K2S.DTD dar.
	 *
	 * @param aDefault Wert, der in default steht oder <code>null</code> falls der Wert nicht gesetzt wurde
	 */
	public void setDefault(String aDefault) {
		_default = aDefault;
	}
}

