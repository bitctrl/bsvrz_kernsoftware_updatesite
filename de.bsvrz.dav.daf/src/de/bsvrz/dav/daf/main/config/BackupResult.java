/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

/** Interface, das die Rückgabe eines Konfigurationsbackups speichert */
public interface BackupResult {

	/**
	 * Korrekt gesicherte Dateien
	 *
	 * @return Anzahl der gesicherten Dateien
	 */
	long getCompleted();

	/**
	 * Nicht gesicherte Dateien
	 *
	 * @return Anzahl der nicht gesicherten dateien (z.B. wegen Lesefehlern, zu wenig Speicherplatz etc.)
	 */
	long getFailed();

	/**
	 * Anzahl der Dateien, die gesichert werden sollten.
	 *
	 * @return getCompleted() + getFailed()
	 */
	long getTotal();

	/**
	 * Pfad in dem die Sicherung angelegt wurde. Befindet sich auf dem System, auf dem die Konfiguration läuft.
	 *
	 * @return Absoluter Pfad als String
	 */
	String getPath();
}
