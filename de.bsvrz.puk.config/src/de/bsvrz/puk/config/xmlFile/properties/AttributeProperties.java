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
 * Dieses Interface wird von ListAttributeProperties und PlainAttributeProperties implementiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface AttributeProperties {

	/**
	 * Setzt die maximale Anzahl von Arrayelementen.
	 *
	 * @param maxCount Maximale Anzahl von Arrayelementen.
	 */
	void setMaxCount(String maxCount);

	/**
	 * Setzt die maximale Anzahl von Arrayelementen.
	 *
	 * @param maxCount Maximale Anzahl von Arrayelementen.
	 */
	void setMaxCount(int maxCount);

	/**
	 * Legt fest, ob die Anzahl Arrayelemente variabel oder fest ist.
	 *
	 * @param targetValue "variabel" oder "fest"
	 */
	void setTargetValue(String targetValue);

	/**
	 * Legt fest, ob die Anzahl Arrayelemente variabel oder fest ist.
	 *
	 * @param targetValue TargetValue.FIX oder TargetValue.VARIABLE
	 */
	void setTargetValue(TargetValue targetValue);

	/**
	 * Setzt den Namen des Attributs.
	 * @param name Name des Attributs.
	 */
	void setName(String name);

	/**
	 * Setzt die Info mit Kurzinfo und Beschreibung dieses Attributs.
	 * @param info Info mit Kurzinfo und Beschreibung dieses Attributs
	 */
	void setInfo(SystemObjectInfo info);

	/**
	 * Liefert den Attributtyps dieses Attributs.
	 * @return Attributtyps dieses Attributs
	 */
	String getAttributeTypePid();

	/**
	 * Bestimmt die maximale Anzahl von Arrayelementen.
	 * @return maximale Anzahl von Arrayelementen oder -1 falls der Wert nicht gesetzt wurde.
	 */
	int getMaxCount();

	/**
	 * Bestimmt, ob die Anzahl Arrayelemente variabel oder fest ist.
	 * @return TargetValue.FIX oder TargetValue.VARIABLE
	 */
	TargetValue getTargetValue();

	/**
	 * Bestimmt den Namen des Attributs
	 * @return Namen des Attributs oder "" falls der Wert nicht gesetzt wurde
	 */
	String getName();

	/**
	 * Bestimmt die Info mit Kurzinfo und Beschreibung dieses Attributs.
	 * @return Info mit Kurzinfo und Beschreibung dieses Attributs
	 */
	SystemObjectInfo getInfo();
}
