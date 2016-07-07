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

import java.util.List;

/**
 * Dieses Interface stellt Methoden zur Berechnung von RowKeys und CellKeys bereit, die typischerweise
 * bei den verschiedenen Selektionsformen benötigt werden.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 * 
 */
public interface CellKeyServer {
	/**
	 * Methode die eine Liste von CellKeys zurückliefert, die sich zwischen den übergebenen CellKeys befinden.
	 * 
	 * @param cellKey1 erster CellKey
	 * @param cellKey2 zweiter CellKey
	 * @return eine Liste von CellKeys zwischen den übergebenen CellKeys
	 */
	public List<CellKey> getCellKeysBetween( CellKey cellKey1, CellKey cellKey2);
	
	/**
	 * Methode, die eine Liste von CellKeys zurückliefert, die sich zwischen den übergebenen RowKeys befinden.
	 * 
	 * @param rowKey1 erster RowKey
	 * @param rowKey2 zweiter RowKey
	 * @return eine Liste von CellKeys zwischen den übergebenen RowKeys
	 */
	public List<CellKey> getCellKeysBetween( RowKey rowKey1, RowKey rowKey2);
	
	
	/**
	 * Methode die eine Liste von CellKeys zurückliefert, die zu dem übergebenen RowKey gehören.
	 * 
	 * @param rowKey RowKey
	 * @return eine Liste von CellKeys, die zu dem übergebenen RowKey gehören
	 */
	public List<CellKey> getCellKeys( RowKey rowKey);
	
	/**
	 * Liefert eine Liste mit allen CellKeys zurück.
	 * 
	 * @return eine Liste aller CellKeys
	 */
	public List<CellKey> getAllCellKeys(); 
	
	/**
	 * Methode, die eine Liste von RowKeys zurückliefert, die sich zwischen den übergebenen RowKeys befinden.
	 * 
	 * @param rowKey1 erster RowKey
	 * @param rowKey2 zweiter RowKey
	 * @return eine Liste von RowKeys zwischen den übergebenen RowKeys
	 */
	public List<RowKey> getRowKeysBetween( RowKey rowKey1, RowKey rowKey2);
}
