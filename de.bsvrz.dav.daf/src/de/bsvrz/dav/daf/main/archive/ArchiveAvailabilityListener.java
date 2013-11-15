/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
package de.bsvrz.dav.daf.main.archive;

/**
 * Die Implementation dieses Interface stellt einen Listener zur Verf�gung, der benachrichtigt wird sobald das
 * Archivsystem �ber den Datenverteiler "nicht mehr"/"wieder" zu erreichen ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public interface ArchiveAvailabilityListener {

	/**
	 * Diese Methode wird aufgerufen, wenn sich die Erreichbarkeit des Archivsystems �ndert.
	 *
	 * @param archive Archivsystem, dessen Zustand sich ge�ndert hat. Der aktuelle Zustand kann mit {@link
	 *                de.bsvrz.dav.daf.main.archive.ArchiveRequestManager#isArchiveAvailable} abgefragt werden.
	 */
	void archiveAvailabilityChanged(ArchiveRequestManager archive);
}
