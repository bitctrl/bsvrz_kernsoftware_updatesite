/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Attributen. Neben der Position eines Attributs
 * in der zugeh�rigen Attributgruppe oder Attributliste, und Informationen, die angeben, ob ein Attribut als
 * Array verwendet wird, referenzieren Attribute einen {@link AttributeType Attribut-Typ}, der die
 * Eigenschaften eines konkreten Wertes des Attributs beschreibt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface Attribute extends ConfigurationObject {
	/**
	 * Bestimmt die Position eines Attributs oder einer Attributliste in der �bergeordneten Attributmenge
	 * (Attributgruppe bzw. Attributliste).
	 *
	 * @return Position eines Attributs. Das erste Attribut hat die Position <code>1</code>.
	 */
	public int getPosition();

	/**
	 * Bestimmt, ob die Feldgr��e dieses Attributs durch eine Obergrenze beschr�nkt ist.
	 *
	 * @return <code>true</code>, wenn die Anzahl der Werte beschr�nkt ist;<br/> <code>false</code>, wenn die
	 *         Anzahl der Werte nicht beschr�nkt ist.
	 */
	public boolean isCountLimited();

	/**
	 * Bestimmt, ob die Feldgr��e dieses Attributs variieren kann.
	 *
	 * @return <code>true</code>, wenn die Anzahl der Werte dieses Attributs mit jedem Datensatz variieren
	 *         kann;<br/> <code>false</code>, wenn die Anzahl der Werte fix ist.
	 */
	public boolean isCountVariable();

	/**
	 * Bestimmt, ob die maximale Feldgr��e dieses Attributs beschr�nkt ist. Wenn die Feldgr��e nicht beschr�nkt
	 * ist, wird der Wert 0 zur�ckgegeben. Wenn die Feldgr��e nicht variabel ist, wird die erforderliche
	 * Feldgr��e zur�ckgegeben.
	 *
	 * @return Maximale Feldgr��e.
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
	 * Bestimmt den Typ dieses Attributs �ber den ein Zugriff auf die Eigenschaften von konkreten Attributwerten
	 * erm�glicht wird.
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

