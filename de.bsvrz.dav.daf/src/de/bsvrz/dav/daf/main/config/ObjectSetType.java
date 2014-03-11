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

import java.util.List;

/**
 * Schnittstelle zum Zugriff auf die beschreibenden Informationen und Einschr�nkungen von Mengen-Typen. Zu
 * jedem Mengen-Typ wird konfiguriert welcher Name eine Menge dieses Typs haben muss, welche Typen von
 * Objekten enthalten sein d�rfen, wieviele Objekte mindestens und h�chstens enthalten sein m�ssen bzw.
 * d�rfen, ob eine Menge an den entsprechenden Objekten vorhanden sein muss oder darf und ob eine Menge dieses
 * Typs konfigurierend oder dynamisch ist.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface ObjectSetType extends ConfigurationObjectType {
	/**
	 * Liefert eine Liste von {@link SystemObjectType Objekt-Typen} zur�ck, die in Mengen dieses Typs verwendet
	 * werden k�nnen.
	 *
	 * @return Liste von Typ-Objekten.
	 */
	public List<SystemObjectType> getObjectTypes();

	/**
	 * Liefert die Anzahl von Objekten, die mindestens in der Menge vorhanden sein m�ssen.
	 *
	 * @return Anzahl der mindestens in der Menge geforderten Objekte
	 */
	public int getMinimumElementCount();

	/**
	 * Liefert die Anzahl von Objekten, die h�chstens in der Menge vorhanden sein d�rfen.
	 *
	 * @return Anzahl der h�chstens in der Menge erlaubten Objekte oder <code>0</code> falls die Anzahl
	 *         unbegrenzt ist.
	 */
	public int getMaximumElementCount();

	/**
	 * Liefert zur�ck, ob eine Menge dieses Typs online �nderbar ist. Mengen, deren Typ vom Typ DynamischeMenge
	 * abgeleitet ist, sind online �nderbar, d.h. es k�nnen Objekte online hinzugef�gt und entfernt werden.
	 * Mengen, deren Typ vom Typ KonfigurationsMenge abgeleitet ist, sind nicht online �nderbar, d.h. nur mit
	 * einer neuen Konfigurationsversion k�nnen Objekte hinzugef�gt und entfernt werden.
	 *
	 * @return <code>true</code>, wenn die Menge online �nderbar ist;<br> <code>false</code>, wenn die Menge
	 *         nicht online �nderbar ist.
	 */
	public boolean isMutable();

	/**
	 * Liefert die Referenzierungsart des Mengen-Typs zur�ck. Von der Konfiguration werden folgende
	 * Referenzierungsarten unterschieden: <ul> <li>Gerichtete Assoziation</li> <li>Aggregation</li>
	 * <li>Komposition</li> </ul>
	 *
	 * @return die Referenzierungsart des Mengen-Typs
	 */
	public ReferenceType getReferenceType();
}

