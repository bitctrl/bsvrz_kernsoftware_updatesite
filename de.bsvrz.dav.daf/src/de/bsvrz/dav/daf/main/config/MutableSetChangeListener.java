/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

/**
 * Schnittstelle für Änderungen von online änderbaren Mengen (dynamischen Mengen), auf die sich eine Applikation {@link
 * MutableSet#addChangeListener anmelden} und {@link MutableSet#removeChangeListener abmelden} kann.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface MutableSetChangeListener {
	/**
	 * Methode, die nach Änderung einer Menge aufgerufen wird. Die Methode ist seitens der Applikation zu implementieren.
	 *
	 * @param set			Dynamische Menge die verändert wurde.
	 * @param addedObjects   Objekte, die in die Menge aufgenommen wurden. Falls keine Objekte aufgenommen wurden, wird ein leeres Array übergeben.
	 * @param removedObjects Objekte, die aus der Menge entfernt wurden. Falls keine Objekte entfernt wurden, wird ein leeres Array übergeben.
	 */
	public void update(MutableSet set, SystemObject[] addedObjects, SystemObject[] removedObjects);
}

