/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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
 * Diese Schnittstelle definiert Methoden um auf Elemente von nicht online änderbaren Zusammenstellungen von
 * System-Objekten zugreifen zu können. Sie wird benutzt bei <ul> <li>{@link ObjectSet Mengen}, um auf die
 * Elemente einer Menge zugreifen zu können;</li> <li>{@link SystemObjectType Typen} um auf alle Objekte des
 * jeweiligen Typs zugreifen zu können;</li> </ul>
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface NonMutableCollection extends SystemObjectCollection {
	/**
	 * Bestimmt die Elemente, die in der in Bearbeitung befindlichen Version des Konfigurationsbereichs aktuell sind.
	 *
	 * @return Liste der System-Objekte, die in der Bearbeitung befindlichen Version des Konfigurationsbereichs aktuell sind.
	 */
	public List<SystemObject> getElementsInModifiableVersion();

	/**
	 * Bestimmt die in einer bestimmten Konfigurationsversion zur Zusammenstellung gehörenden Elemente.
	 *
	 * @param version Version der Konfiguration
	 * @return Liste mit den in der angegebenen Version zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsInVersion(short version);

	/**
	 * Bestimmt die Elemente, die in allen Konfigurationsversionen eines vorgegebenen Versionsbereichs zur
	 * Zusammenstellung gehört haben.
	 *
	 * @param fromVersion Erste Version des Bereichs von Konfigurationversionen
	 * @param toVersion   Letzte Version des Bereichs von Konfigurationversionen
	 * @return Liste mit den in allen Versionen des Bereichs zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsInAllVersions(short fromVersion, short toVersion);

	/**
	 * Bestimmt die Elemente, die in mindestens einer Konfigurationsversion eines vorgegebenen Versionsbereichs
	 * zur Zusammenstellung gehört haben.
	 *
	 * @param fromVersion Erste Version des Bereichs von Konfigurationversionen
	 * @param toVersion   Letzte Version des Bereichs von Konfigurationversionen
	 * @return Liste mit den in mindestens einer Version des Bereichs zur Zusammenstellung gehörenden
	 *         System-Objekten.
	 */
	public List<SystemObject> getElementsInAnyVersions(short fromVersion, short toVersion);
}

