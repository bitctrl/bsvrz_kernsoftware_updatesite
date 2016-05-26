/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import java.util.*;

/**
 * Diese Schnittstelle definiert Methoden, um auf Elemente von Zusammenstellungen von System-Objekten zugreifen zu können. Sie wird benutzt bei <ul> <li>{@link
 * ObjectSet Mengen}, um auf die Elemente einer Menge zugreifen zu können;</li> <li>{@link SystemObjectType Typen}, um auf alle Objekte des jeweiligen Typs
 * zugreifen zu können;</li> </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface SystemObjectCollection {

	/**
	 * Bestimmt die zum aktuellen Zeitpunkt zur Zusammenstellung gehörenden Elemente.
	 *
	 * @return Liste mit den aktuell zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElements();

	/**
	 * Bestimmt die zu einem vorgegebenen Zeitpunkt zur Zusammenstellung gehörenden Elemente. Nicht dynamische Mengen mit Referenzierungsart Komposition oder
	 * Aggregation liefern immer alle Elemente der Menge zurück.
	 *
	 * @param time Zeitpunkt in Millisekunden seit 1970
	 *
	 * @return Liste mit den zum angegebenen Zeitpunkt zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElements(long time);

	/**
	 * Bestimmt die Elemente, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs zur Zusammenstellung gehört haben. Nicht dynamische Mengen mit
	 * Referenzierungsart Komposition oder Aggregation liefern immer alle Elemente der Menge zurück.
	 *
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den zu mindestens einem Zeitpunkt des Zeitbereichs zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsInPeriod(long startTime, long endTime);

	/**
	 * Bestimmt die Elemente, die während des gesamten angegebenen Zeitbereichs zur Zusammenstellung gehört haben. Nicht dynamische Mengen mit Referenzierungsart
	 * Komposition oder Aggregation liefern immer alle Elemente der Menge zurück.
	 *
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den während des gesamten Zeitbereichs zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime);
}

