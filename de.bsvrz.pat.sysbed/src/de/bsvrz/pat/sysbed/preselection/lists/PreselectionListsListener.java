/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.preselection.lists;


/**
 * Listener zum Anmelden bei einem Objekt der Klasse {@link PreselectionLists}.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface PreselectionListsListener {

	/**
	 * Methode, die ein Objekt der Klasse <code>PreselectionLists</code> übergibt, falls bei einer Liste eine Änderung bei der Selektion aufgetreten ist.
	 *
	 * @param preselectionLists das Objekt <code>PreselectionLists</code>, bei der sich die Selektion geändert hat
	 */
	void listSelectionChanged(PreselectionLists preselectionLists);
}
