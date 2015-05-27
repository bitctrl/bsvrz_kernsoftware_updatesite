/*
 * Copyright 2014 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

/**
 * Definiert den Typ der Kennzeichnungsart von Keine-Änderungen-Markierungen bei Zustandsprotokollen
 *
 * @author Kappich Systemberatung
 * @version $Revision: 12816 $
 */
public enum NoChangeMarker {
	/**
	 * Gibt an, dass die Information, dass sich die Daten nicht geändert haben, zeilenweise übertragen wird.
	 */
	Row,
	/**
	 * Gibt an, dass die Information, dass sich die Daten nicht geändert haben, für jede Zelle einzeln übertragen wird.
	 */
	Cell,
	/**
	 * Gibt an, dass der Default-Wert des Skriptes beibehalten werden soll, Wenn das Skript keine explizite Vorgabe macht, wird
	 * die Information zeilenweise übertragen ({@link #Row}).
	 */
	Undefined

}
