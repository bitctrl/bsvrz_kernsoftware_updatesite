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
 * Schnittstelle zum Zugriff auf die Eigenschaften von dynamischen Zusammenstellungen. Diese Schnittstelle wird von dynamischen Mengen und von dynamischen
 * Typen implementiert. Enthalten sind Methoden zum Zugriff auf die Elemente sowie zur An- und Abmeldung auf Änderungen der Elemente einer dynamischen Menge
 * oder eines dynamischen Typs unter Berücksichtigung der Simulationsvariante.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5982 $
 */
public interface MutableCollection extends ConfigurationObject {

	/**
	 * Meldet einen Listener auf Änderungen der Elemente einer dynamischen Menge oder eines dynamischen Typs unter Berücksichtigung der Simulationsvariante an.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @param changeListener Listener, der bei Änderungen der Elemente informiert werden soll.
	 */
	void addChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener);

	/**
	 * Meldet einen zuvor angemeldeten Listener wieder ab.
	 * @param simulationVariant Simulationsvariante der entsprechenden Anmeldung.
	 * @param changeListener Listener der entsprechenden Anmeldung.
	 */
	void removeChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener);

	/**
	 * Bestimmt die aktuellen Elemente einer dynamischen Menge oder eines dynamischen Typs unter Berücksichtigung der Simulationsvariante.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @return Aktuelle Elemente der dynamischen Menge oder des dynamischen Typs unter Berücksichtigung der Simulationsvariante.
	 */
	List<SystemObject> getElements(short simulationVariant);
}
