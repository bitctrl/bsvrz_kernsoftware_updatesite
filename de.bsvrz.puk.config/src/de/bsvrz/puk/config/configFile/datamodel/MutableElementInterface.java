/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Interface zum Abfragen von Mengenelementen (Elementen einer änderbaren Menge)
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface MutableElementInterface {
	/**
	 * Gibt das System-Objekt zurück.
	 *
	 * @return das System-Objekt
	 */
	SystemObject getObject();

	/**
	 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element zur Menge gehört.
	 *
	 * @return Zeitstempel, seit dem das Element zur Menge gehört
	 */
	long getStartTime();

	/**
	 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element nicht mehr zur Menge gehört.
	 *
	 * @return Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
	 */
	long getEndTime();

	/**
	 * Gibt die Simulationsvariante dieses Elements zurück, in der das Objekt dieser dynamischen Menge hinzugefügt wurde.
	 *
	 * @return die Simulationsvariante, in welcher das Objekt der Menge hinzugefügt wurde.
	 */
	short getSimulationVariant();
}
