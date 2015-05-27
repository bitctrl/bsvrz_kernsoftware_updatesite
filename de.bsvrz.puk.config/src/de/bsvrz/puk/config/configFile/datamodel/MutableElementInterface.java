/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Interface zum Abfragen von Mengenelementen (Elementen einer �nderbaren Menge)
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13074 $
 */
public interface MutableElementInterface {
	/**
	 * Gibt das System-Objekt zur�ck.
	 *
	 * @return das System-Objekt
	 */
	SystemObject getObject();

	/**
	 * Gibt den Zeitstempel zur�ck, der angibt, seit wann das Element zur Menge geh�rt.
	 *
	 * @return Zeitstempel, seit dem das Element zur Menge geh�rt
	 */
	long getStartTime();

	/**
	 * Gibt den Zeitstempel zur�ck, der angibt, seit wann das Element nicht mehr zur Menge geh�rt.
	 *
	 * @return Zeitstempel, seit dem das Element nicht mehr zur Menge geh�rt
	 */
	long getEndTime();

	/**
	 * Gibt die Simulationsvariante dieses Elements zur�ck, in der das Objekt dieser dynamischen Menge hinzugef�gt wurde.
	 *
	 * @return die Simulationsvariante, in welcher das Objekt der Menge hinzugef�gt wurde.
	 */
	short getSimulationVariant();
}
