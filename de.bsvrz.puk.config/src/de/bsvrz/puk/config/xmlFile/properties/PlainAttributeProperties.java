/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Kapselt die Eigenschaften eines Attributs in Versorgungsdateien entsprechend K2S.DTD.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class PlainAttributeProperties extends AbstractAttributeProperties implements AttributeProperties {
	private String _default = null;

	/**
	 * Erzeugt ein neues Objekt für ein Attribut eines vorgegebenen Attributtyps.
	 * @param attributeTypePid Attributtyp des Attributs
	 */
	public PlainAttributeProperties(String attributeTypePid) {
		super(attributeTypePid);
	}

	/**
	 * Setzt den Defaultwert dieses Attributs.
	 * @param aDefault Defaultwert dieses Attributs
	 */
	public void setDefault(String aDefault) {
		_default = aDefault;
	}

	/**
	 * Bestimmt den Defaultwert dieses Attributs.
	 * @return Defaultwert dieses Attributs oder <code>null</code> falls der Wert nicht gesetzt wurde
	 */
	public String getDefault() {
		return _default;
	}
}
