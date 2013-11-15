/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

import java.util.*;

/**
 * Schnitstelle, die von der Applikation implementiert werden muss, um bei Änderungen der Elemente von dynamischen Mengen oder dynamischen Typen benachrichtigt
 * zu werden. Entsprechende Listener-Objekte können über das Interface {@link de.bsvrz.dav.daf.main.config.MutableCollection an- und abgemeldet werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5982 $
 */
public interface MutableCollectionChangeListener {

	/** Diese Methode wird nach einer entsprechenden Anmeldung aufgerufen, wenn bei jeweiligen dynamischen Menge oder dem dynamischen Typ Elmente hinzugefügt
	 * oder entfern wurden.
	 * @param mutableCollection Dynamische Menge oder dynamischer Typ dessen Elemente sich geändert haben.
	 * @param simulationVariant Simulationsvariante auf die sich die Änderung bezieht.
	 * @param addedElements Neue Elemente.
	 * @param removedElements Gelöschte Elemente.
	 */
	void collectionChanged(MutableCollection mutableCollection, short simulationVariant, List<SystemObject> addedElements, List<SystemObject> removedElements);
}
