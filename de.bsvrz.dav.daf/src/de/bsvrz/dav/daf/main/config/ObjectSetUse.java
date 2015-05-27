/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Beschreibt die Verwendung einer Menge im Kontext eines bestimmten Objekt-Typs. Zu jeder Mengen-Verwendung,
 * die bei einem Objekt-Typ konfiguriert ist, wird angegeben, welcher Name die Menge haben muss, von welchem
 * Mengen-Typ sie sein muss und ob sie an jedem Objekt des jeweiligen Objekt-Typs vorhanden sein muss bzw.
 * darf.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface ObjectSetUse extends ConfigurationObject {
	/**
	 * Liefert den persistenten Namen, den eine Menge dieser Verwendung haben muss.
	 *
	 * @return Name einer Menge dieser Verwendung.
	 */
	public String getObjectSetName();

	/**
	 * Bestimmt den Typ dieser Menge. Der Mengentyp enthält Beschränkungen über den
	 * Typ und die Anzahl der in einer Menge enthaltenen Objekte und eine
	 * Information darüber, ob Elemente online hinzugefügt oder entfernt werden
	 * dürfen. Die Methode entspricht mit Ausnahme des Rückgabetyps der Methode
	 * {@link SystemObject#getType}.
	 *
	 * @return Mengentyp einer Menge dieser Verwendung.
	 */
	public ObjectSetType getObjectSetType();

	/**
	 * Liefert eine Information darüber, ob die Verwendung der Menge bei einem Objekt des jeweiligen Objekt-Typs
	 * erforderlich ist.
	 *
	 * @return <code>true</code>, wenn die Menge vorhanden sein muss;<br> <code>false</code>, wenn die Menge
	 *         vorhanden sein darf.
	 */
	public boolean isRequired();
}

