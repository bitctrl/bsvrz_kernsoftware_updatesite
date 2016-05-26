/*
 * Copyright 2009 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.dataview.selectionManagement;

import de.bsvrz.pat.sysbed.dataview.CellKey;
import de.bsvrz.pat.sysbed.dataview.RowKey;

import java.util.Set;

/**
 * Diese Klasse stellt einen Listener dar, welcher Änderungen an Selektionen registriert.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 * 
 */
public interface SelectionListener {
	/**
	 * Diese Methode teilt mit, welche CellKeys sich geändert haben.
	 * 
	 * @param selectionManager
	 *            Klasse, in der sich was geändert hat.
	 * @param keys
	 *            Schlüssel, die sich geändert haben.
	 */
	public void cellSelectionChanged( SelectionManager selectionManager, Set<CellKey> keys);
	
	/**
	 * Diese Methode teilt mit, welche RowKeys sich geändert haben.
	 * 
	 * @param selectionManager
	 *            Klasse, in der sich was geändert hat.
	 * @param keys
	 *            Schlüssel, die sich geändert haben.
	 */
	public void rowSelectionChanged( SelectionManager selectionManager, Set<RowKey> keys);
}
