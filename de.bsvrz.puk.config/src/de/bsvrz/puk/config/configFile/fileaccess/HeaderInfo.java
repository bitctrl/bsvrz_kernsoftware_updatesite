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

package de.bsvrz.puk.config.configFile.fileaccess;

/**
 * Interface f�r die Dateipositionen in einem {@link de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile}
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13126 $
 */
interface HeaderInfo {
	/**
	 * Gibt das Headerende zur�ck ( = Position des Starts der NGA-Bl�cke)
	 * @return das Headerende
	 */
	long getHeaderEnd();

	/**
	 * Gibt den Start des NG-Dyn-Blocks zur�ck
	 * @return den Start des NG-Dyn-Blocks
	 */
	long getStartOldDynamicObjects();

	/**
	 * Gibt den Start des ID-Index zur�ck
	 * @return den Start des ID-Index
	 */
	long getStartIdIndex();

	/**
	 * Gibt den Start des Pid-Index zur�ck
	 * @return den Start des Pid-Index
	 */
	long getStartPidHashCodeIndex();

	/**
	 * Gibt den Start der Mischmenge zur�ck
	 * @return den Start der Mischmenge
	 */
	long getStartMixedSet();
}
