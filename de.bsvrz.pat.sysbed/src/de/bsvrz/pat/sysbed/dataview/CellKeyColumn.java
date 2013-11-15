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

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eine CellKeyColumn steht für eine Spalte eines CellKeys. 
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8084 $
 * 
 */
public class CellKeyColumn {
	
	final static String SEPARATOR = CellKey.getSECOND_SEPARATOR();
	
	final String _columnString;
	final boolean _superColumn;
	
	/**
	 * Konstruiert die CellKeyColumn aus einem String, der keine Array-Informationen
	 * enthälten darf und einem Indikator, ob es sich um eine übergreifende Spalte
	 * handelt.
	 *  
	 * @param columnString die String-Darstellung
	 * @param superColumn ist die Spalte eine Super-Spalte?
	 */
	CellKeyColumn( final String columnString, final boolean superColumn) {
		_columnString = columnString;
		_superColumn = superColumn;
	}
	/**
	 * Konstruiert die CellKeyColumn direkt aus einem CellKey dieser Spalte.
	 * 
	 * @param key ein CellKey
	 */
	CellKeyColumn( final CellKey key) {
		_columnString = CellKey.removeArrays( key.getAttributeName());
		_superColumn = key.isSuperColumn();
	}
	
	/**
	 * Gibt an, ob es sich um eine übergreifende Spalte handelt.
	 * 
	 * @return ist die Spalte eine Super-Spalte?
	 */
	public boolean isSuperColumn() {
		return _superColumn;
	}
	
	/**
	 * Vergleicht die CellKeyColumn this mit dem String, ohne zunächst eine CellKeyColumn
	 * dafür anzulegen - und natürlich ohne den übergreifenden Spaltenstatus zu berücksichtigen.
	 * 
	 * @param columnString eine String-Darstellung einer CellKeyColumn
	 * @return sind die CellKeyColumns gmäß String-Darstellung gleich?
	 */
	public boolean isEqualTo( final String columnString) {
		return _columnString.equals( columnString);
	}
	
	/**
	 * Gibt die Spaltenliste der Attributegruppe zurück, und zwar mit oder ohne die
	 * übergeordneten Spalten, die die Listen definieren.
	 * 
	 * @param attributeGroup die Attributegruppe
	 * @param withColumnsForArrayListDefinitions mit oder ohne übergeordneten Spalten
	 * @return die Spaltenliste mit Namen
	 */
	public static List<String> getColumnList( final AttributeGroup attributeGroup,
			final boolean withColumnsForArrayListDefinitions) {
		final String nameOrPidOrId = attributeGroup.getNameOrPidOrId();
		List<String> columnList; 
		if ( withColumnsForArrayListDefinitions) {
			columnList = _columnListsWithCfLD.get(nameOrPidOrId);
		} else {
			columnList = _columnListsWithoutCfLD.get(nameOrPidOrId);
		}
		if ( columnList == null) {
			initForAttributeGroup( attributeGroup, withColumnsForArrayListDefinitions);
			if ( withColumnsForArrayListDefinitions) {
				columnList = _columnListsWithCfLD.get(nameOrPidOrId);
			} else {
				columnList = _columnListsWithoutCfLD.get(nameOrPidOrId);
			}
		}
		return columnList;
	}
	
	/**
	 * Gibt den Index der CellKeyColumn in der Attributgruppe zurück, wobei dies
	 * der Wert in der entsprechenden Liste mit oder ohne Spalten für Listen-Definitione
	 * ist. Wird kein Index gefunden, gibt die Methode <code>null</code> zurück.
	 * 
	 * @param attributeGroup die Attributegruppe
	 * @param withColumnsForArrayListDefinitions mit oder ohne übergeordneten Spalten
	 * @return ein Index oder <code>null</code>
	 */
	public Integer getIndexInColumnList( final AttributeGroup attributeGroup,
			final boolean withColumnsForArrayListDefinitions) {
		final String nameOrPidOrId = attributeGroup.getNameOrPidOrId();
		Map<String, Integer> columnIndexMap;
		if ( withColumnsForArrayListDefinitions) {
			columnIndexMap = _columnIndexMapsWithCfLD.get( nameOrPidOrId);
		} else {
			columnIndexMap = _columnIndexMapsWithoutCfLD.get( nameOrPidOrId);
		}
		if ( columnIndexMap == null) {
			initForAttributeGroup( attributeGroup, withColumnsForArrayListDefinitions);
			if ( withColumnsForArrayListDefinitions) {
				columnIndexMap = _columnIndexMapsWithCfLD.get( nameOrPidOrId);
			} else {
				columnIndexMap = _columnIndexMapsWithoutCfLD.get( nameOrPidOrId);
			}
		}
		final Integer index = columnIndexMap.get( _columnString);
		if ( index == null) {
			for ( String s : columnIndexMap.keySet()) {
				if ( s.startsWith( _columnString)) {
					return columnIndexMap.get( s);
				}
			}
		}
		return index;
	}
	
	private static void initForAttributeGroup( final AttributeGroup attributeGroup,
			final boolean withColumnsForArrayListDefinitions) {
		final List<String> columnList = createColumnList( attributeGroup, withColumnsForArrayListDefinitions);
		final String nameOrPidOrId = attributeGroup.getNameOrPidOrId();
		if ( withColumnsForArrayListDefinitions) {
			_columnListsWithCfLD.put( nameOrPidOrId, columnList);
		} else {
			_columnListsWithoutCfLD.put( nameOrPidOrId, columnList);
		}
		final int size = columnList.size();
		Map<String,Integer> columnIndexMap = new HashMap<String,Integer> ();
		for ( int index = 0; index < size; index++) {
			columnIndexMap.put( columnList.get(index), index);
		}
		if ( withColumnsForArrayListDefinitions) {
			_columnIndexMapsWithCfLD.put( nameOrPidOrId, columnIndexMap);
		} else {
			_columnIndexMapsWithoutCfLD.put( nameOrPidOrId, columnIndexMap);
		}
//		dumpForAttributeGroup( attributeGroup, withColumnsForArrayListDefinitions);
	}
	
	@SuppressWarnings("unused")
    private static void dumpForAttributeGroup ( final AttributeGroup attributeGroup,
    		final boolean withColumnsForArrayListDefinitions) {
		final String nameOrPidOrId = attributeGroup.getNameOrPidOrId();
		final List<String> columnList;
		if ( withColumnsForArrayListDefinitions) {
			columnList = _columnListsWithCfLD.get( nameOrPidOrId);
		} else {
			columnList = _columnListsWithoutCfLD.get( nameOrPidOrId);
		}
		if ( columnList == null) {
			System.out.println("Keine Column-Liste vorhanden!");
		} else {
			System.out.println("Column-Liste:");
			for ( String column : columnList) {
				System.out.println( column);
			}
		}
		final Map<String, Integer> columnIndexMap;
		if ( withColumnsForArrayListDefinitions) {
			columnIndexMap = _columnIndexMapsWithCfLD.get( nameOrPidOrId);
		} else {
			columnIndexMap = _columnIndexMapsWithoutCfLD.get( nameOrPidOrId);
		}
		if ( columnIndexMap == null) {
			System.out.println("Column-Index-Map:");
		} else {
			System.out.println("Column-Index-Map:");
			for ( String column : columnIndexMap.keySet()) {
				System.out.println( column + ": " + columnIndexMap.get( column));
			}
		}
		
	}
	
	private static List<String> createColumnList( final AttributeGroup attributeGroup,
			final boolean withColumnsForArrayListDefinitions) {
		List<String> columnList = new ArrayList<String>();
		final List<Attribute> attributes = attributeGroup.getAttributes();
		for ( Attribute attribute : attributes) {
			final String prefix = attributeGroup.getPidOrId() + SEPARATOR + attribute.getName();
			appendToColumnList( prefix, attribute, columnList, withColumnsForArrayListDefinitions);
		}
		return columnList;
	}
	
	private static void appendToColumnList( final String prefix, 
			final Attribute attribute, final List<String> columnList,
			final boolean withColumnsForArrayListDefinitions) {
		final AttributeType attributeType = attribute.getAttributeType();
		if (  attributeType instanceof AttributeListDefinition) {
			if ( withColumnsForArrayListDefinitions) {
				if ( attribute.isArray()) {	// Arrays können leer sein, und dann ist es von Vorteil einen Eintrag zu haben.
					columnList.add( prefix);
				}
			}
			final AttributeListDefinition ald = (AttributeListDefinition) attributeType;
			for ( Attribute subAttribute : ald.getAttributes()) {
				final String newPrefix = prefix + SEPARATOR + subAttribute.getName();
				appendToColumnList( newPrefix, subAttribute, columnList, withColumnsForArrayListDefinitions);
			}
		} else {
			columnList.add( prefix);
		}
	}
	
	final static Map<String, List<String> > _columnListsWithCfLD = new HashMap<String, List<String> >();
	// Atg.namePidOrId -> Column-String-Liste
	final static Map<String, Map<String, Integer> > _columnIndexMapsWithCfLD = new HashMap<String, Map<String,Integer> >();
	// Atg.namePidOrId -> Map : Column-String -> Index in der Column-String-Liste
	final static Map<String, List<String> > _columnListsWithoutCfLD = new HashMap<String, List<String> >();
	// Atg.namePidOrId -> Column-String-Liste
	final static Map<String, Map<String, Integer> > _columnIndexMapsWithoutCfLD = new HashMap<String, Map<String,Integer> >();
	// Atg.namePidOrId -> Map : Column-String -> Index in der Column-String-Liste
}
