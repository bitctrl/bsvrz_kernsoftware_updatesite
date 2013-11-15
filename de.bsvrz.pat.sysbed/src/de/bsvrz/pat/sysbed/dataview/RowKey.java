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
package de.bsvrz.pat.sysbed.dataview;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Die Klasse kapselt ein Objektreferenz für einen Datensatz bzw eine Zeile in der
 * visuellen Darstellung. Da das DataViewFrame bzw das DataViewPanel immer nur die
 * Swing-Komponenten vorhält, die sich im sichtbaren Bereich befinden, braucht man
 * diese Objektreferenzen um sich etwa selektierte Zeilen außerhalb des sichtbaren
 * Bereichs zu merken. Die Klasse ist ähnlich zu CellKey aufgebaut, nur viel einfacher.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8084 $
 *
 */
public class RowKey {
	/** Pid des Datensatzes */
	private Integer _internalIdForPid;
	
	/** Datensatzindex */
	private long _dataIndex;
	
	private final static String SEPARATOR = CellKey.getFIRST_SEPARATOR();
	private final static Pattern SEPARATOR_PATTERN = Pattern.compile( SEPARATOR);
	
	private final static Map<String, Integer> _pidToInternalIdMap = new HashMap<String, Integer>();
	private final static Map<Integer, String> _internalIdToPidMap = new HashMap<Integer, String>();
	private static Integer _nextInternalIdForPid = 1;
	
	/**
	 * Konstruktor zum Anlegen eines Schlüssels
	 * 
	 * @param rowKey
	 *            Schlüssel
	 */
	public RowKey( String rowKey) {
		String[] split = SEPARATOR_PATTERN.split( rowKey);
		if(split.length == 2) {
			final String pidOfDataTableObject = split[0];
			_internalIdForPid = _pidToInternalIdMap.get( pidOfDataTableObject);
			if ( _internalIdForPid == null) {
				_internalIdForPid = _nextInternalIdForPid++;
				_pidToInternalIdMap.put( pidOfDataTableObject, _internalIdForPid);
				_internalIdToPidMap.put( _internalIdForPid, pidOfDataTableObject);
			}
			_dataIndex = Long.parseLong(split[1]);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Gibt den Datensatzindex zurück.
	 * 
     * @return den Datensatzindex
     */
    public long getDataIndex() {
    	return _dataIndex;
    }

    /**
     * Gibt die PID des Datensatzes zurück.
     * 
     * @return die PID des Datensatzes
     */
    public String getPidOfDataTableObject() {
    	return _internalIdToPidMap.get( _internalIdForPid);
    }

    @Override
    public boolean equals( final Object o) {
    	if ( !(o instanceof RowKey)) {
    		return false;
    	}
    	final RowKey oAsRowKey = (RowKey) o;
    	if ( (_internalIdForPid == oAsRowKey._internalIdForPid) && (_dataIndex == oAsRowKey._dataIndex)) {
    		return true;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
    	final Long dataIndex = _dataIndex;
    	return _internalIdForPid + dataIndex.intValue();
    }

	/**
	 * Gibt das Trennzeichen aus der String-Darstellung zurück.
	 * 
     * @return das Trennzeichen aus der String-Darstellung
     */
    public static String getSeparator() {
    	return SEPARATOR;
    }
}
