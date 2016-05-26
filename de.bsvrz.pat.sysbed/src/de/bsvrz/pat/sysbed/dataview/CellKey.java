/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.dataview;

import de.bsvrz.dav.daf.main.config.AttributeGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Ein CellKey ist eine Objektreferenz für ein Feld in den eigentlichen Daten
 * des DataViewFrames. Da der DataViewFrame bzw. das DataViewPanel nur die
 * gerade zu visualisierenden Swing-Komponenten bereithält, benötigt man
 * eine solche Objektreferenz, um sich etwa Dinge wie die Selektion merken
 * zu können.
 * Ein CellKey ist trotzdem kein schlankes Objekt.
 * Ein CellKey wird in der Regel mit einem String der Form "<Pid oder Id eines Systemobjekts>:<Datensatzindex>:<Attributbeschreibung>"
 * konstruiert. Die Attributbeschreibung ist genauer im Konstruktor beschrieben. Hier sei nur angemerkt,
 * dass sie Informationen zur Attributgruppe, aber nicht zum Aspekt enthält. Möglicherweise kann man
 * die ATG entfernen oder eines Tages noch den Aspekt hinzufügen.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 * 
 */
public class CellKey {
	/**
	 * Besitzt ein Datensatz keine Daten, so ist der CellKey nicht eine einzige Spalte,
	 * sondern umfasst alle Spalten und dieser Variable ist true.
	 */
	final private boolean _superColumn;
	
	/** Pid des Datensatzes */
	private Integer _internalIdForPid;
	
	/** Datensatzindex */
	private long _dataIndex;
	
	/** Attributgruppen-Id */
	private Integer _attributeGroupId;

	/** Statt Strings merkt sich jeder CellKey nur die Indizes in der entsprechenden Lookup-Map. */
	final private Integer[] _attributeNamePartIds;
	/** Statt Strings merkt sich jeder CellKey nur die Indizes in der entsprechenden Lookup-Map. */
	final private Integer[] _attributeNamePartArrayValues; 
	
	// In Pids sind Doopelpunkt und Komma verboten, so daß sie hier als Trennungszeichen benutzt werden können.
	private static String FIRST_SEPARATOR = ":";
	private static String SECOND_SEPARATOR = ",";
	
	private static Pattern FIRST_SEPARATOR_PATTERN = Pattern.compile( FIRST_SEPARATOR);
	private static Pattern SECOND_SEPARATOR_PATTERN = Pattern.compile( SECOND_SEPARATOR);
	
	/** Eine Lookup-Map für den Attributgruppennamen zur gegebenen, internen Id. */
	private static Map<Integer, String> _idToAttributGroupMap = new HashMap<Integer, String>();
	/** Eine Lookup-Map für den internen Index zum Attributgruppennamen. */
	private static Map<String, Integer> _attributGroupToIdMap = new HashMap<String, Integer>();
	/** Die nächste zu vergebende interne Attributgruppen-Id. */
	private static Integer _nextAttributeGroupId = 1;
	
	/** Eine Lookup-Map für die interne Id der PID. */
	private static Map<String, Integer> _pidToInternalIdMap = new HashMap<String, Integer>();
	/** Eine Lookup-Map für die PID zur internen Id. */
	private static Map<Integer, String> _internalIdToPidMap = new HashMap<Integer, String>();
	/** Die nächste zu vergebende interne PID-Id. */
	private static Integer _nextInternalIdForPid = 1;
	
	/** Eine Lookup-Map für die interne Id des Parts. */
	private static Map<String, Integer> _partToInternalIdMap = new HashMap<String, Integer>();
	/** Eine Lookup-Map für die Part zur internen Id. */
	private static Map<Integer, String> _internalIdToPartMap = new HashMap<Integer, String>();
	/** Die nächste zu vergebende interne Part-Id. */
	private static Integer _nextInternalIdForPart = 1;
	
	/** Der in der Zelle darzustellende Text. */
	private String _cellText;
	
	/**
	 * Konstruktor zum Anlegen eines CellKeys.
	 * Ein normaler CellKey beschreibt eine Zelle in einer Row; allerdings gibt
	 * es die übergreifenden Zeilen für die "Keine-Daten-Fälle", die mit superColumn = true
	 * konstruiert werden müssen.
	 * 
	 * @param cellKey Schlüssel
	 */
	public CellKey( final String cellKey, final boolean superColumn) {
		_superColumn = superColumn;
		String[] split = FIRST_SEPARATOR_PATTERN.split( cellKey);
		if(split.length > 1) {
			final String pidOfDataTableObject = split[0];
			_internalIdForPid = _pidToInternalIdMap.get( pidOfDataTableObject);
			if ( _internalIdForPid == null) {
				_internalIdForPid = _nextInternalIdForPid++;
				_pidToInternalIdMap.put( pidOfDataTableObject, _internalIdForPid);
				_internalIdToPidMap.put( _internalIdForPid, pidOfDataTableObject);
			}
			_dataIndex = Long.parseLong(split[1]);
		}
		if ( split.length > 2) {
			final String attributeDescription = split[2];
			// Ist die attributeDescription nicht leer, so beginnt sie mit der Pid der Attributgruppe.
			// Dann geht es gegebenenfalls mit der Attributhierarchie weiter, wobei die einzelnen Teile 
			// durch einen , getrennt werden.
			if ( (attributeDescription.length() != 0) && !attributeDescription.contains(SECOND_SEPARATOR)) {
				_attributeGroupId = _attributGroupToIdMap.get( attributeDescription);
				if ( _attributeGroupId == null) {
					_attributeGroupId = _nextAttributeGroupId++;
					_attributGroupToIdMap.put(attributeDescription, _attributeGroupId);
					_idToAttributGroupMap.put( _attributeGroupId, attributeDescription);
				}
				_attributeNamePartIds = null;
				_attributeNamePartArrayValues = null;
			} else {
				final String[] parts = SECOND_SEPARATOR_PATTERN.split( attributeDescription);
				final int arraySize = parts.length-1;
				if ( arraySize >= 0) {
					String s = parts[0];
					_attributeGroupId = _attributGroupToIdMap.get( s);
					if ( _attributeGroupId == null) {
						_attributeGroupId = _nextAttributeGroupId++;
						_attributGroupToIdMap.put(s, _attributeGroupId);
						_idToAttributGroupMap.put( _attributeGroupId, s);
					}
				}
				if ( arraySize > 0) {
					_attributeNamePartIds = new Integer[arraySize];
					_attributeNamePartArrayValues = new Integer[arraySize];
					for ( int index = 0; index < arraySize; index++) {
						final String part = parts[index+1];
						String partWithoutArray = CellKey.removeArrays(part);
						_attributeNamePartIds[index] = _partToInternalIdMap. get(partWithoutArray);
						if ( _attributeNamePartIds[index] == null) {
							_attributeNamePartIds[index] = _nextInternalIdForPart++;
							_partToInternalIdMap.put( partWithoutArray, _attributeNamePartIds[index]);
							_internalIdToPartMap.put( _attributeNamePartIds[index], partWithoutArray);
						}
						_attributeNamePartArrayValues[index] = CellKey.getArrayValue( part);
					}
				} else {
					_attributeNamePartIds = null;
					_attributeNamePartArrayValues = null;
				}
			}
		} else {
			_attributeNamePartIds = null;
			_attributeNamePartArrayValues = null;
		}
	}
	
	/**
	 * Gibt an, ob der CellKey eine Super-Spalte beschreibt oder eine normale Spalte.
	 * 
	 * @return steht der CellKey für eine Super-Spalte?
	 */
	public boolean isSuperColumn() {
		return _superColumn;
	}
	
	/**
	 * Gibt den String aus dem der CellKey konstruiert wurde zurück.
	 * 
	 * @return der String, aus dem der CellKey konstruiert wurde
	 */
	public String getCellKeyAsString() {
		String cellKeyAsString = _internalIdToPidMap.get( _internalIdForPid) + FIRST_SEPARATOR;
		cellKeyAsString += _dataIndex + FIRST_SEPARATOR + getAttributeName();
		return cellKeyAsString;
	}
	
	/**
	 * Gibt den Datensatzindex des CellKeys zurück.
	 * @return Datensatzindex des CellKeys
	 */
	public long getDataIndex() {
		return _dataIndex;
	}
	
	/**
	 * Gibt die Pid des Datensatzes des CellKeys zurück.
	 * 
	 * @return Pid des Datensatzes
	 */
	public String getPidOfTheDataTableObject() {
		return _internalIdToPidMap.get( _internalIdForPid);
	}
	
	/**
	 * Gibt den Attributname des CellKeys inklusive Attributgruppe zurück.
	 * @return Attributname des CellKeys inklusive Attributgruppe
	 */
	public String getAttributeName() {
		final String attributeGroupAsString = _idToAttributGroupMap.get( _attributeGroupId);
		final String attributePartsWithArrays = getAttributePartsWithArrays();
		if ( (attributePartsWithArrays == null) || (attributePartsWithArrays.length()==0) ) {
			return attributeGroupAsString;
		} else {
			return attributeGroupAsString + SECOND_SEPARATOR + attributePartsWithArrays;
		}
	}
	
	/**
	 * Gibt den Attributname ohne Attributgruppe, aber mit den Array-Informationen des CellKeys zurück.
	 * 
	 * @return Attributname ohne Attributgruppe, aber mit den Array-Informationen ds CellKeys
	 */
	public String getAttributePartsWithArrays() {
		final StringBuffer attributePartsWithArrays = new StringBuffer();
		final int numberOfParts = getNumberOfParts();
		for ( int i = 0; i < numberOfParts; i++) {
			final Integer arrayValue = _attributeNamePartArrayValues[i];
			if ( arrayValue != -1) {
				attributePartsWithArrays.append( _internalIdToPartMap.get( _attributeNamePartIds[i]));
				attributePartsWithArrays.append( "[").append( _attributeNamePartArrayValues[i]).append( "]").append(SECOND_SEPARATOR);
			} else {
				attributePartsWithArrays.append( _internalIdToPartMap.get( _attributeNamePartIds[i]));
				attributePartsWithArrays.append( SECOND_SEPARATOR);
			}
		}
		int length = attributePartsWithArrays.length();
		if ( length == 0) {
			return "";
		} 
		return attributePartsWithArrays.toString().substring(0, attributePartsWithArrays.length()-1);
	}
	
	private String getAttributePartName( final int index) {
		if ( (index > _attributeNamePartIds.length) || (index < 0)) {
			return null;
		}
		return _internalIdToPartMap.get( _attributeNamePartIds[index]);
	}
	
	/**
	 * Gibt die Array-Informationen des CellKeys zurück.
	 * 
	 * @return die Array-Informationen des CellKeys
	 */
	public Integer[] getAttributeNamePartArrayValues() {
		return _attributeNamePartArrayValues;
	}
	
	private Integer getAttributePartArrayValue( final int index) {
		if ( (_attributeNamePartArrayValues == null) || (index > _attributeNamePartArrayValues.length)) {
			return null;
		}
		return _attributeNamePartArrayValues[index];
	}
	
	
	
	private int getNumberOfParts() {
		if ( _attributeNamePartIds == null) {
			return 0;
		}
		return _attributeNamePartIds.length;
	}
	
	/**
	 * Entfernt alle Indizes aus den Arrays des übergebenen Strings und gibt das Ergebnis zurück.
	 * 
	 * @param s ein String, z.B. eine Attributbeschreibung mit Array-Informationen
	 * @return der String s ohne Indizes in den Array-Informationen 
	 */
	public static String removeIndices(String s) {
		return s.replaceAll("\\[\\d*\\]", "[]");
	}
	
	/**
	 * Entfernt alle Arrays aus dem übergebenen String und gibt das Ergebnis zurück.
	 * 
	 * @param s  ein String, z.B. eine Attributbeschreibung mit Array-Informationen
	 * @return der String s ohne seine Array-Informationen 
	 */
	public static String removeArrays(String s) {
		return s.replaceAll("\\[\\d*\\]", "");
	}
	
	/**
	 * Gibt den ersten Integer zwischen einer [ und einer ] innerhalb von s zurück.
	 * 
	 * @param s ein String
	 * @return der erste Integer zwischen einer [ und einer ] innerhalb von s
	 */
	public static Integer getArrayValue(String s) {
		final int beginIndex = s.indexOf('[')+1;
		final int endIndex = s.indexOf(']');
		if ( beginIndex < endIndex) {
			return Integer.parseInt( s.substring( beginIndex, endIndex));
		} else {
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof CellKey) {
			CellKey cellKey = (CellKey)object;
			if ( getCellKeyAsString().equals(cellKey.getCellKeyAsString())) {
				return _superColumn == cellKey._superColumn;
			} else {
				return false;
			}
		}
		else if(object instanceof String) {
			throw new IllegalArgumentException();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getCellKeyAsString().hashCode();
	}
	
	/**
	 * Gestattet einen Vergleich des CellKeys this mit dem übergebenen CellKey. Definiert die
	 * Ordnung auf den Spalten bei der Interval-Selektion.
	 * 
	 * @param attributeGroup die Attributgruppe
	 * @param key der CellKey
	 * @return das Vergleichsergebnis
	 */
	public boolean attributeNameIsLessOrEqual( final AttributeGroup attributeGroup, final CellKey key) {
		int lastIndex1 = -1;
		int lastIndex2 = -1;
		int index = 0;
		int size = Math.min( getNumberOfParts(), key.getNumberOfParts());
		while ( index < size) {	// also getAttributePartArrayValue != null bzw. key.isSuperColumn() = false !!!
			if ( getAttributePartName(index).equals( key.getAttributePartName(index))) {
				lastIndex1 = getAttributePartArrayValue(index);
				lastIndex2 = key.getAttributePartArrayValue(index);
				if ( lastIndex1 >= 0) { // d.h. es handelt sich um Indizes
					if ( lastIndex1 != lastIndex2) { // es ist schon eine Entscheidung möglich!
						return (lastIndex1 <= lastIndex2);
					}
				}
			} else {
				if ( lastIndex1 != -1) {
					if ( lastIndex1 < lastIndex2) {
						return true;
					} else if ( lastIndex1 > lastIndex2) {
						return false;
					} else { // lastIndex1 = lastIndex2
						break;
					}
				} else {
					break;
				}
			}
			index++;
		}
		if ( index < size) {
			final CellKeyColumn cellKeyColumn1 = new CellKeyColumn( CellKey.removeArrays( getAttributeName()), false);
			final CellKeyColumn cellKeyColumn2 = new CellKeyColumn( CellKey.removeArrays(key.getAttributeName()), false);
			final Integer indexInColumnList1 = cellKeyColumn1.getIndexInColumnList(attributeGroup, true);
			final Integer indexInColumnList2 = cellKeyColumn2.getIndexInColumnList(attributeGroup, true);
			return (indexInColumnList1 <= indexInColumnList2);
		} else {
			if ( size > 0) {
				lastIndex1 = getAttributePartArrayValue(size-1);
				lastIndex2 = key.getAttributePartArrayValue(size-1);
				return (lastIndex1 <= lastIndex2);
			} else {
				return getNumberOfParts() <= key.getNumberOfParts();
			}
		}
	}
	
	/**
	 * Gibt genau dann <code>true</code> zurück, wenn der CellKey <code>this</code> sich zwischen den Spaltenschranken
	 * befindet.
	 * 
	 * @param attributeGroup die Attributgruppe
	 * @param lowerColumn die untere Spaltengrenze
	 * @param upperColumn die obere Spaltengrenze
	 * @return <code>true</code> genau dann, wenn this zwischen den Grenzen liegt
	 */
	public boolean isBetweenColumns( final AttributeGroup attributeGroup,
			final CellKeyColumn lowerColumn, final CellKeyColumn upperColumn) {
		if ( lowerColumn.isSuperColumn() || upperColumn.isSuperColumn()) {
			return true;
		}
		final Integer lowerIndex = lowerColumn.getIndexInColumnList(attributeGroup, true);
		if ( lowerIndex == null) {
			return false;
		}
		final Integer upperIndex = upperColumn.getIndexInColumnList(attributeGroup, true);
		if ( upperIndex == null) {
			return false;
		}
		if ( lowerIndex > upperIndex) {
			return false;
		}
		
		CellKeyColumn thisColumn = new CellKeyColumn( CellKey.removeArrays( getAttributeName()), false);
		final Integer thisIndex = thisColumn.getIndexInColumnList(attributeGroup, true);
		if ( thisIndex == null) {
			return false;
		}
		return (lowerIndex <= thisIndex) && (thisIndex <= upperIndex);
	}
	
	/**
	 * Gibt die kleinere, d.h. weiter links stehende CellKeyColumn der beiden CellKeys zurück.
	 *  
	 * @param attributeGroup die Attributgruppe
	 * @param key1 ein CellKey
	 * @param key2 noch ein CellKey
	 * @return der kleinere der beiden CellKeys
	 */
	public static CellKeyColumn minColumn( final AttributeGroup attributeGroup, final CellKey key1, final CellKey key2) {
		final CellKeyColumn column1 = new CellKeyColumn( key1);
		if ( key1.isSuperColumn()) {
			return column1;
		}
		final CellKeyColumn column2 = new CellKeyColumn( key2);
		List<String> columnList = CellKeyColumn.getColumnList( attributeGroup, true);
		for ( String columnString : columnList) {
			if ( column1.isEqualTo(columnString)) {
				return column1;
			}
			if ( column2.isEqualTo(columnString)) {
				return column2;
			}
		}
		return null;
	}
	/**
	 * Gibt die größere, d.h. weiter rechts stehende CellKeyColumn der beiden CellKeys zurück.
	 *  
	 * @param attributeGroup die Attributgruppe
	 * @param key1 ein CellKey
	 * @param key2 noch ein CellKey
	 * @return der größere der beiden CellKeys
	 */
	public static CellKeyColumn maxColumn( final AttributeGroup attributeGroup, final CellKey key1, final CellKey key2) {
		final CellKeyColumn column1 = new CellKeyColumn( key1);
		final CellKeyColumn column2 = new CellKeyColumn( key2);
		if ( key2.isSuperColumn()) {
			return column2;
		}
		List<String> columnList = CellKeyColumn.getColumnList( attributeGroup, true);
		final int size = columnList.size();
		for ( int i = size - 1; i >= 0; i--) {
			final String columnString = columnList.get( i);
			if ( column1.isEqualTo(columnString)) {
				return column1;
			}
			if ( column2.isEqualTo(columnString)) {
				return column2;
			}
		}
		return null;
	}
	
	/**
	 * Gibt den Text, der in der Zelle angezeigt werden soll, zurück.
	 * 
	 * @return den Text, der in der Zelle angezeigt werden soll
	 */
	public String getCellText() {
		return _cellText;
	}
	
	/**
	 * Setzt den Text, der in der Zelle angezeigt werden soll.
	 *
	 * @param der neue Text
	 */
	public void setCellText(String cellText) {
		_cellText = cellText;
	}
	
	/**
	 * Gibt den CellKey auf System.out aus.
	 */
	public void dumpMe() {
		System.out.println("CellKey: " + getCellKeyAsString());
		System.out.println("PIDOfDataTableObject: " + _internalIdToPidMap.get( _internalIdForPid));
		System.out.println("DataIndex: " + _dataIndex);
		System.out.println("AttributeName: " + getAttributeName());
		for (int i = 0; i < getNumberOfParts(); i++) {
			System.out.println("Part " + i + ": " + _internalIdToPartMap.get( _attributeNamePartIds[i]) 
					+ " with array: " + _attributeNamePartArrayValues[i]);
		}
		System.out.println("CellText: " + getCellText());
	}

	/**
	 * Gibt das erste Trennzeichen in der String-Darstellung des CellKeys zurück.
	 * 
     * @return das erste Trennzeichen in der String-Darstellung des CellKeys
     */
    public static String getFIRST_SEPARATOR() {
    	return FIRST_SEPARATOR;
    }

	/**
	 * Gibt das zweite Trennzeichen in der String-Darstellung des CellKeys zurück.
	 * 
     * @return das zweite Trennzeichen in der String-Darstellung des CellKeys
     */
    public static String getSECOND_SEPARATOR() {
    	return SECOND_SEPARATOR;
    }

	@Override
	public String toString() {
		return getCellKeyAsString();
	}
}
