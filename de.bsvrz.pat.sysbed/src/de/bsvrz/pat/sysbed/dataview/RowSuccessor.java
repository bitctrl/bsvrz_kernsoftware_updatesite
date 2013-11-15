/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.dataview;

import java.util.*;

/**
 * Diese Klasse wird von der Klasse {@link RowData} ben�tigt, um Arrays von Listen/Attributen speichern 
 * zu k�nnen. Ein Objekt dieses Typs entspricht einer Liste oder einem Attribut. Viele Objekte dieses 
 * Typs als Nachfolger einer RowData entsprechen einem Array von Listen oder einem Array von Attributen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8084 $
 */
public class RowSuccessor {

	/** speichert alle Nachfolger */
	private final List<RowData> _successors;
	/** speichert den Schl�ssel */
	private CellKey _key;

	/* ################ Konstruktor ############ */
	/** Standardkonstruktot. */
	public RowSuccessor() {
		_successors = new ArrayList<RowData>();
	}

	/**
	 * F�gt einen Nachfolger hinzu.
	 *
	 * @param rowData ein Nachfolger
	 */
	public void addSuccessor(RowData rowData) {
		_successors.add(rowData);
	}

	/**
	 * Gibt alle Nachfolger zur�ck.
	 *
	 * @return alle Nachfolger
	 */
	public List<RowData> getSuccessors() {
		return _successors;
	}

	/**
	 * Gibt den CellKey zur�ck.
	 * 
     * @return gibt den key zur�ck
     */
    public CellKey getKey() {
	    return _key;
    }

	/**
	 * Setzt den CellKey.
     * @param key der neue CellKey
     */
    public void setKey(CellKey key) {
	    _key = key;
    }
}
