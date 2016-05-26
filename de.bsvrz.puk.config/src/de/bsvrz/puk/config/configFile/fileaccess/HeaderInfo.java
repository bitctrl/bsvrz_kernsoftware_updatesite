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

package de.bsvrz.puk.config.configFile.fileaccess;

/**
 * Interface für die Dateipositionen in einem {@link de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile}
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
interface HeaderInfo {
	/**
	 * Gibt das Headerende zurück ( = Position des Starts der NGA-Blöcke)
	 * @return das Headerende
	 */
	long getHeaderEnd();

	/**
	 * Gibt den Start des NG-Dyn-Blocks zurück
	 * @return den Start des NG-Dyn-Blocks
	 */
	long getStartOldDynamicObjects();

	/**
	 * Gibt den Start des ID-Index zurück
	 * @return den Start des ID-Index
	 */
	long getStartIdIndex();

	/**
	 * Gibt den Start des Pid-Index zurück
	 * @return den Start des Pid-Index
	 */
	long getStartPidHashCodeIndex();

	/**
	 * Gibt den Start der Mischmenge zurück
	 * @return den Start der Mischmenge
	 */
	long getStartMixedSet();
}
