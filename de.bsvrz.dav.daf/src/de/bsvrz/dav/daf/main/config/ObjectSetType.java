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
 * Schnittstelle zum Zugriff auf die beschreibenden Informationen und Einschränkungen von Mengen-Typen. Zu
 * jedem Mengen-Typ wird konfiguriert welcher Name eine Menge dieses Typs haben muss, welche Typen von
 * Objekten enthalten sein dürfen, wieviele Objekte mindestens und höchstens enthalten sein müssen bzw.
 * dürfen, ob eine Menge an den entsprechenden Objekten vorhanden sein muss oder darf und ob eine Menge dieses
 * Typs konfigurierend oder dynamisch ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ObjectSetType extends ConfigurationObjectType {
	/**
	 * Liefert eine Liste von {@link SystemObjectType Objekt-Typen} zurück, die in Mengen dieses Typs verwendet
	 * werden können.
	 *
	 * @return Liste von Typ-Objekten.
	 */
	public List<SystemObjectType> getObjectTypes();

	/**
	 * Liefert die Anzahl von Objekten, die mindestens in der Menge vorhanden sein müssen.
	 *
	 * @return Anzahl der mindestens in der Menge geforderten Objekte
	 */
	public int getMinimumElementCount();

	/**
	 * Liefert die Anzahl von Objekten, die höchstens in der Menge vorhanden sein dürfen.
	 *
	 * @return Anzahl der höchstens in der Menge erlaubten Objekte oder <code>0</code> falls die Anzahl
	 *         unbegrenzt ist.
	 */
	public int getMaximumElementCount();

	/**
	 * Liefert zurück, ob eine Menge dieses Typs online änderbar ist. Mengen, deren Typ vom Typ DynamischeMenge
	 * abgeleitet ist, sind online änderbar, d.h. es können Objekte online hinzugefügt und entfernt werden.
	 * Mengen, deren Typ vom Typ KonfigurationsMenge abgeleitet ist, sind nicht online änderbar, d.h. nur mit
	 * einer neuen Konfigurationsversion können Objekte hinzugefügt und entfernt werden.
	 *
	 * @return <code>true</code>, wenn die Menge online änderbar ist;<br> <code>false</code>, wenn die Menge
	 *         nicht online änderbar ist.
	 */
	public boolean isMutable();

	/**
	 * Liefert die Referenzierungsart des Mengen-Typs zurück. Von der Konfiguration werden folgende
	 * Referenzierungsarten unterschieden: <ul> <li>Gerichtete Assoziation</li> <li>Aggregation</li>
	 * <li>Komposition</li> </ul>
	 *
	 * @return die Referenzierungsart des Mengen-Typs
	 */
	public ReferenceType getReferenceType();
}

