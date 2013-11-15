/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.preselection.lists;

import java.util.*;

/**
 * Interface, um ein Filter-Objekt bei der Klasse <code>PreselectionLists</code> anzumelden. Wenn die Listen des {@link PreselectionLists}-Panels zusätzlich
 * nach weiteren Kriterien gefiltert werden sollen, muss die Methode {@link #applyFilter} implementiert werden. Welche Liste gefiltert werden soll, kann mittels
 * Konstanten, die dieses Interface zur Verfügung stellt, bestimmt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5019 $
 */
public interface PreselectionListsFilter {

	/**
	 * Die zu filternde Liste der <code>applyFilter</code>-Methode: Objekt
	 *
	 * @see #applyFilter(int,java.util.List)
	 */
	int OBJECT_LIST = 0;

	/**
	 * Die zu filternde Liste der <code>applyFilter</code>-Methode: Objekttyp
	 *
	 * @see #applyFilter(int,java.util.List)
	 */
	int OBJECTTYPE_LIST = 1;

	/**
	 * Die zu filternde Liste der <code>applyFilter</code>-Methode: Attributgruppe
	 *
	 * @see #applyFilter(int,java.util.List)
	 */
	int ATTRIBUTEGROUP_LIST = 2;

	/**
	 * Die zu filternde Liste der <code>applyFilter</code>-Methode: Aspekt
	 *
	 * @see #applyFilter(int,java.util.List)
	 */
	int ASPECT_LIST = 3;

	/**
	 * Die Methode muss von der Anwendung implementiert werden, wenn eine der Listen des {@link PreselectionLists}-Panels zusätzlich nach weiteren Kriterien
	 * gefiltert werden soll. Welche Liste gefiltert werden soll, kann mittels der Konstanten des {@link PreselectionListsFilter Interfaces} bestimmt werden.
	 *
	 * @param whichList gibt an, welche Liste übergeben wird
	 * @param list      die zu filternde Liste
	 *
	 * @return die gefilterte Liste
	 */
	List applyFilter(int whichList, List list);
}
