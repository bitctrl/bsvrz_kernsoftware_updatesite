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

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Attributen. Neben der Position eines Attributs
 * in der zugehörigen Attributgruppe oder Attributliste, und Informationen, die angeben, ob ein Attribut als
 * Array verwendet wird, referenzieren Attribute einen {@link AttributeType Attribut-Typ}, der die
 * Eigenschaften eines konkreten Wertes des Attributs beschreibt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface Attribute extends ConfigurationObject {
	/**
	 * Bestimmt die Position eines Attributs oder einer Attributliste in der übergeordneten Attributmenge
	 * (Attributgruppe bzw. Attributliste).
	 *
	 * @return Position eines Attributs. Das erste Attribut hat die Position <code>1</code>.
	 */
	public int getPosition();

	/**
	 * Bestimmt, ob die Feldgröße dieses Attributs durch eine Obergrenze beschränkt ist.
	 *
	 * @return <code>true</code>, wenn die Anzahl der Werte beschränkt ist;<br/> <code>false</code>, wenn die
	 *         Anzahl der Werte nicht beschränkt ist.
	 */
	public boolean isCountLimited();

	/**
	 * Bestimmt, ob die Feldgröße dieses Attributs variieren kann.
	 *
	 * @return <code>true</code>, wenn die Anzahl der Werte dieses Attributs mit jedem Datensatz variieren
	 *         kann;<br/> <code>false</code>, wenn die Anzahl der Werte fix ist.
	 */
	public boolean isCountVariable();

	/**
	 * Bestimmt, ob die maximale Feldgröße dieses Attributs beschränkt ist. Wenn die Feldgröße nicht beschränkt
	 * ist, wird der Wert 0 zurückgegeben. Wenn die Feldgröße nicht variabel ist, wird die erforderliche
	 * Feldgröße zurückgegeben.
	 *
	 * @return Maximale Feldgröße.
	 */
	public int getMaxCount();

	/**
	 * Bestimmt, ob dieses Attribut ein Feld ist.
	 *
	 * @return <code>true</code>, wenn dieses Attribut ein Feld ist;<br/> <code>false</code>, wenn dieses
	 *         Attribut kein Feld ist.
	 */
	public boolean isArray();

	/**
	 * Bestimmt den Typ dieses Attributs über den ein Zugriff auf die Eigenschaften von konkreten Attributwerten
	 * ermöglicht wird.
	 *
	 * @return Typ des Attributs.
	 */
	public AttributeType getAttributeType();

	/**
	 * Ermittelt den Default-Attributwert dieses Attribut.
	 * @return Default-Attributwert dieses Attribut oder <code>null</code> falls kein Defaultwert festgelegt wurde.
	 */
	String getDefaultAttributeValue();

}

