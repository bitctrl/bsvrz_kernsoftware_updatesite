/*
 * Copyright 2009 by Kappich Systemberatung Aachen
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
package de.bsvrz.pat.sysbed.dataview.selectionManagement;

import de.bsvrz.pat.sysbed.dataview.CellKey;
import de.bsvrz.pat.sysbed.dataview.RowKey;

import java.util.List;

/**
 * Dieses Interface stellt Methoden zur Berechnung von RowKeys und CellKeys bereit, die typischerweise
 * bei den verschiedenen Selektionsformen ben�tigt werden.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8084 $
 * 
 */
public interface CellKeyServer {
	/**
	 * Methode die eine Liste von CellKeys zur�ckliefert, die sich zwischen den �bergebenen CellKeys befinden.
	 * 
	 * @param cellKey1 erster CellKey
	 * @param cellKey2 zweiter CellKey
	 * @return eine Liste von CellKeys zwischen den �bergebenen CellKeys
	 */
	public List<CellKey> getCellKeysBetween( CellKey cellKey1, CellKey cellKey2);
	
	/**
	 * Methode, die eine Liste von CellKeys zur�ckliefert, die sich zwischen den �bergebenen RowKeys befinden.
	 * 
	 * @param rowKey1 erster RowKey
	 * @param rowKey2 zweiter RowKey
	 * @return eine Liste von CellKeys zwischen den �bergebenen RowKeys
	 */
	public List<CellKey> getCellKeysBetween( RowKey rowKey1, RowKey rowKey2);
	
	
	/**
	 * Methode die eine Liste von CellKeys zur�ckliefert, die zu dem �bergebenen RowKey geh�ren.
	 * 
	 * @param rowKey RowKey
	 * @return eine Liste von CellKeys, die zu dem �bergebenen RowKey geh�ren
	 */
	public List<CellKey> getCellKeys( RowKey rowKey);
	
	/**
	 * Liefert eine Liste mit allen CellKeys zur�ck.
	 * 
	 * @return eine Liste aller CellKeys
	 */
	public List<CellKey> getAllCellKeys(); 
	
	/**
	 * Methode, die eine Liste von RowKeys zur�ckliefert, die sich zwischen den �bergebenen RowKeys befinden.
	 * 
	 * @param rowKey1 erster RowKey
	 * @param rowKey2 zweiter RowKey
	 * @return eine Liste von RowKeys zwischen den �bergebenen RowKeys
	 */
	public List<RowKey> getRowKeysBetween( RowKey rowKey1, RowKey rowKey2);
}
