/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import java.util.List;

/**
 * Schnittstellenklasse, die den Zugriff auf Attributmengen ermöglicht. Attributmengen werden in {@link
 * AttributeGroup Attributgruppen} und in {@link AttributeListDefinition Attributlisten} benutzt, um mehrere
 * Attribute zu einer Einheit zusammenzufassen.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Fouad
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface AttributeSet extends ConfigurationObject {
	/**
	 * Liefert eine Liste der Attribute dieser Attributmenge (Attributgruppe bzw. Attributliste) zurück. Die
	 * Reihenfolge der Attribute in der Liste entspricht der durch die {@link Attribute#getPosition Position} der
	 * Attribute definierte Reihenfolge innerhalb der Attributgruppe bzw. Attributliste
	 *
	 * @return Liste von {@link Attribute Attributen}
	 */
	public List<Attribute> getAttributes();

	/**
	 * Liefert das Attribut mit dem angegebenen Namen dieser Attributgruppe zurück.
	 *
	 * @param attributeName Name des gesuchten Attributs.
	 * @return Das gesuchte Attribut oder <code>null</code> wenn kein Attribut mit dem gegebenen Namen gefunden
	 *         wurde.
	 */
	public Attribute getAttribute(String attributeName);
}

