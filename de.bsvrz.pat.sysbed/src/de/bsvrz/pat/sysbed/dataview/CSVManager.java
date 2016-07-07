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
import de.bsvrz.pat.sysbed.dataview.DataTableObject.DataTableObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Der CSVManager übernimmt innerhalb dieses Pakets die Erstellung der Header- und Datenzeilen
 * wie sie etwa in CSV-Dateien verwendet werden, das heißt, es handelt sich um Zeilen mit Datenfeldern,
 * die durch ein Trennzeichen voneinander separiert sind. Die Ausgangsdaten sind hier in  einer
 * Collection von DataTableObjects enthalten, und die Spalten und der Inhalt der Datenzeilen kann durch 
 * eine Collection von CellKeys gefiltert werden. Eine wesentliche Aufgabe bei der Erstellung der Zeilen
 * ist die Abbildung der Arrays: deren Inhalte werden in aufeinanderfolgenden Spalten wiedergegeben.
 * <p>
 * Jeder CSVManager kann nur für eine Attributgruppe verwendet werden, die schon im Konstruktor 
 * endgültig festgelegt wird.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 *
 */
public class CSVManager {
	
	private String _delimiter = ";";
	
	/**
	 * Jeder CSVManager kann nur für eine Attributgruppe verwendet werden, die schon
	 * im Konstruktor endgültig festgelegt wird.
	 */
	private final AttributeGroup _attributeGroup;
	
	/**
	 * Die im Konstruktor übergebene Menge selektierter CellkKeys bestimmt Spalten und
	 * diejenigen Einträge, die nicht leer sein können.
	 */
	private final Set<CellKey> _selectedCellKeys;
	
	/**
	 * Enthält die benötigten Datensätze in der richtigen Reihenfolge
	 */
	private final List<DataTableObject> _dataTableObjects = new ArrayList<DataTableObject>();
	
	/**
	 * Enthält die CSV-Spalten als Schlüssel und als Wert je eine HashMap, die wiederum
	 * als Schlüssel eine Datensatzkennung enthält und als Wert den Text der Zelle.
	 */
	private final Map<CSVColumn, Map<DataTableObjectId, String>> _csvColumnToText = 
		new TreeMap<CSVColumn, Map<DataTableObjectId, String>>();
	
	/** speichert das Zeitformat für die Spalte 'Zeit' */
	static private final String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss,SSS";
	
	/**
	 * Jeder CSVManager kann nur für eine Attributgruppe verwendet werden, die nicht veränderbar ist.
	 * Die übergebenen CellKeys werden als Filter benutzt, fall die Collection nicht <code>null</code>
	 * ist. Die Inhalte der Datenzeilen werden den DataTableObjects entnommen werden.
	 * 
	 * @param attributeGroup die Attributgruppe
	 * @param selectedCellKeys die CellKeys
	 * @param dataTableObjects die DataTableObjects
	 */
	public CSVManager( final AttributeGroup attributeGroup, final Set<CellKey> selectedCellKeys,
			final List<DataTableObject> dataTableObjects) {
		_attributeGroup = attributeGroup;
		final Map<String,DataTableObject> dataTableObjectMap = new HashMap<String, DataTableObject>();
		if ( selectedCellKeys != null) {	// eine Selektion
			_selectedCellKeys = new HashSet<CellKey>( selectedCellKeys);
			final Set<String> filterSet = new HashSet<String>();
			for ( CellKey cellKey : _selectedCellKeys) {
				filterSet.add( cellKey.getPidOfTheDataTableObject() + ":" + cellKey.getDataIndex());
			}
			for ( DataTableObject dataTableObject : dataTableObjects) {
				final String s = dataTableObject.getObject().getPid() + ":" + dataTableObject.getDataIndex();
				if ( filterSet.contains( s)) {
					_dataTableObjects.add( dataTableObject);
					dataTableObjectMap.put( s, dataTableObject);
				}
			}
		} else {	
			// keine Selektion, nehme alle CellKeys aller DataTableObjects; ACHTUNG: wenn dataTableObjects keine Datensätze
			// mit Daten enthält, entstehen hier keine CellKeys, die Spalteninformationen haben, was auf die Initialisierung
			// in initCSVColumns Auswirkungen hat.
			_selectedCellKeys = new HashSet<CellKey>();
			for ( DataTableObject dataTableObject : dataTableObjects) {
				_dataTableObjects.add( dataTableObject);
				final String s = dataTableObject.getObject().getPid() + ":" + dataTableObject.getDataIndex();
				dataTableObjectMap.put( s, dataTableObject);
				_selectedCellKeys.addAll( dataTableObject.getAllCellKeys());
			}
		}
		initCSVColumns( dataTableObjectMap);
	}
	
	/**
	 * Gibt die Titelzeile der CSV-Datei zurück. Ist der übergebene Wert <code>true</code>, so werden
	 * auch die Spalten der Zeilenköpfe (Art, Zeit und Objekt) aufgeführt.
	 * <p>
	 * Wurde im Konstruktor eine von <code>null</code> verschiedene Collection von CellKeys angegeben,
	 * so wirkt diese als Filter auf Spalten, Zeilen und Zellen. Für die Header-Zeile heißt dies: es
	 * treten nur Spalten auf, für die mindestens ein selektierter CellKey existiert.
	 * 
	 * @return die Header-Zeile
	 */
	
	public String getCSVHeaderLine( final boolean rowHeader) {
		final StringBuffer buffer = new StringBuffer();
		if ( rowHeader) {
			buffer.append("Art" + _delimiter + "Zeit" + _delimiter + "Objekt" + _delimiter);
		}
		for ( CSVColumn column : _csvColumnToText.keySet()) {
			final String name = column.getName();
			if ( name.length()>0) {	// Der leere String entsteht nur bei den Keine-Daten-Datensätzen
				buffer.append( name + _delimiter);
			}
		}
		buffer.append("\n");
		return buffer.toString();
	}
	
	/**
	 * Gibt die Zeilen der CSV-Datei, die die Inhalte enthalten, zurück. Ist der übergebene Wert <code>true</code>, 
	 * so werden auch die Inhalte der Zeilenköpfe (Art, Zeit und Objekt) aufgeführt.
	 * <p>
	 * Wurde im Konstruktor eine von <code>null</code> verschiedene Collection von CellKeys angegeben,
	 * so wirkt diese als Filter auf Spalten, Zeilen und Zellen. Für die Datenzeilen heißt dies, dass nur
	 * die Inhalte von Zellen mit selektierten CellKeys ausgegeben werden, und dass leere Spalten und
	 * leere Zeilen gar nicht ausgeben werden.
	 * 
	 * @return ein String mit allen Datenzeilen
	 */
	public String getCSVLines( final boolean rowHeader) {
		final StringBuffer buffer = new StringBuffer();
		for ( DataTableObject dataTableObject : _dataTableObjects) {
			final StringBuffer csvLineForDataTableObject = getCSVLineForDataTableObject(dataTableObject, rowHeader);
			if ( csvLineForDataTableObject != null) {
				buffer.append( csvLineForDataTableObject);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Setzt das Trennzeichen, das in der Header- und den Datenzeilen verwendet wird.
	 * 
	 * @param delimiter
	 */
	public void setDelimiter(final String delimiter) {
		_delimiter = delimiter;
	}
	
	////////////////////////////////////////////////////////////////////////
	// private Methoden 
	////////////////////////////////////////////////////////////////////////
	
	private void initCSVColumns(final Map<String,DataTableObject> dataTableObjectMap) {
		// Enthält _dataTableObjects keine Datensätze mit richtigen Daten, so enthält _selectedCellKeys
		// nur Super-Spalten, weshalb _csvColumnToText leer bleibt. Im Moment fällt das nur bei der
		// Ausgabe, die auf getCSVHeaderLine zurückgreift auf, und ist wohl erträglich.
		// Richtiger wäre es, die folgende Art der Initialisierung nur für einen CSVManager mit
		// Selektion durchzuführen, und für einen CSV-Manager ohne Selektion die CSVColumns aus
		// der Attributgruppe zu initialisieren.
		for ( CellKey cellKey : _selectedCellKeys) {
			if ( !cellKey.isSuperColumn()) {
				final CSVColumn column = new CSVColumn(cellKey);
				final String key = cellKey.getPidOfTheDataTableObject() + ":" + cellKey.getDataIndex();
				final DataTableObject dataTableObject = dataTableObjectMap.get( key);
				final DataTableObjectId dataTableObjectId = dataTableObject.getDataTableObjectId();
				if ( _csvColumnToText.containsKey( column)) {
					_csvColumnToText.get( column).put( dataTableObjectId, cellKey.getCellText());
				} else {
					final Map<DataTableObjectId, String> newMap = new HashMap<DataTableObjectId, String>();
					newMap.put( dataTableObjectId, cellKey.getCellText());
					_csvColumnToText.put( column, newMap);
				}
			}
		}
	}
	
	private String encodeForCSV( final String s) {
		String r = s;
		if ( r.contains("\"")) {
			r = r.replaceAll("\"", "\"\"");
		}
		if ( r.contains(";") || r.contains( _delimiter) || r.contains("\n") || r.contains(" ") || r.contains("\"")) {
			r = "\"" + r + "\"";
		}
		return r;
	}
	
	private StringBuffer getCSVLineForDataTableObject(
			final DataTableObject dataTableObject,
			final boolean rowHeader) {
		final StringBuffer buffer = new StringBuffer();
		if ( rowHeader) {
			buffer.append( encodeForCSV( DataTableObjectRenderer.getDatakindText(dataTableObject.getDataKind())));
			buffer.append(_delimiter);
			
			DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
			String archiveTime = timeFormat.format(new Date(dataTableObject.getArchiveTime()));
			String dataTime = timeFormat.format(new Date(dataTableObject.getDataTime()));
			long dataIndex = dataTableObject.getDataIndex();
			String dataIndexString = (dataIndex >>> 32) + "#" + ((dataIndex >> 2) & 0x3fffffff) + "#" + (dataIndex & 0x3);
			buffer.append( encodeForCSV( DataTableObjectRenderer.getTimeText(dataTableObject.getTimingType(), 
					archiveTime, dataIndexString, dataTime)));
			buffer.append(_delimiter);
			
			buffer.append( encodeForCSV( dataTableObject.getObject().getNameOrPidOrId()));
			buffer.append(_delimiter);
			
		}
		// Die Vorgehensweise ist nun wie folgt: wir haben ein DataTableObject, die _csvColumns,
		// sowie die _selectedCellKeys. Wir iterieren über _csvColumns und gucken, ob der entsprechende
		// CellKey selektiert ist; wenn nicht, so gibt es einen leeren Spalteneintrag. Wenn doch, so
		// holen wir uns von dem DataTableObject den Wert und benutzen ihn.
		boolean lineIsRelevant = false;	// relevant = mind. ein CellKey wurde selektiert (false dürfte nicht mehr auftreten)
		if ( (dataTableObject == null) || (dataTableObject.getData() != null)) {	// normaler Datensatz
			for ( CSVColumn column : _csvColumnToText.keySet()) {
				final Map<DataTableObjectId,String> theDataIndexSet = _csvColumnToText.get( column);
				final String cellText = theDataIndexSet.get( dataTableObject.getDataTableObjectId());
				if ( cellText != null) {
					buffer.append( encodeForCSV( cellText));
					lineIsRelevant = true;
				}
				buffer.append(_delimiter);
			}
			if ( lineIsRelevant) {
				buffer.append("\n");
				return buffer;
			} else {
				return null;
			}
		}
		else {
			buffer.append( encodeForCSV( DataTableObjectRenderer.getTextForState( dataTableObject.getDataState())));
			buffer.append( _delimiter + "\n");
			return buffer;
		}
	}
	
	/**
	 * Eine CSVColumn ist entweder ein ansonsten leeres Objekt für eine Spalte, die noch 
	 * Subspalten hat, oder sie kapselt die Informationen, die eine Spalte in einer
	 * CSV-Datei benötigt, also Name und die Informationen des CellKeys, aus dem sie
	 * konstruiert wurde, die benötigt werden, um die Inhalte von Datensätzen ermitteln
	 * zu können. Von einer CellKeyColumn unterscheidet sie sich dadurch, dass ein Array
	 * dort hier durch mehrere Spalten repräsentiert wird.
	 * 
	 * @author Kappich Systemberatung
	 * @version $Revision$
	 *
	 */
	private class CSVColumn implements Comparable<CSVColumn> {
		
		/** Falls dies eine Spalte mit Subspalten ist, true; false sonst */
		final boolean _superColumn;
		
		/** Falls es eine Spalte ohne Subspalten ist, so der Index in der Spaltenliste */
		final Integer _cellKeyColumnIndex;
		
		/** Falls es eine Spalte ohne Subspalten ist, so AttributeNamePartArrayValues */
		final Integer[] _arrayIndexes;
		
		/** Falls es eine Spalte ohne Subspalten ist, so AttributePartsWithArrays */
		final String _name;
		
		/**
		 * Eine CSVColumn wird aus einem CellKey konstruiert.
		 * 
		 * @param cellKey der CellKey
		 */
		public CSVColumn ( final CellKey cellKey) {
			final CellKeyColumn cellKeyColumn = new CellKeyColumn( cellKey);
			_superColumn = cellKey.isSuperColumn();
			if ( _superColumn) {
				_cellKeyColumnIndex = null;
				_arrayIndexes = null;
				_name = "";
			} else {
				_cellKeyColumnIndex = cellKeyColumn.getIndexInColumnList(_attributeGroup, true);
				_arrayIndexes = cellKey.getAttributeNamePartArrayValues();
				_name = cellKey.getAttributePartsWithArrays();
			}
		}
		
		public int compareTo(CSVColumn o) {
			if ( _superColumn && o._superColumn) {
				return 0;
			}
			if ( _superColumn && !o._superColumn) {
				return -1;
			}
			if ( !_superColumn && o._superColumn) {
				return 1;
			}
			if ( _cellKeyColumnIndex != o._cellKeyColumnIndex)  {
				return _cellKeyColumnIndex - o._cellKeyColumnIndex;
			}
			if ( (_arrayIndexes != null) && (o._arrayIndexes != null)) {
				final int length = _arrayIndexes.length;
				if ( length!= o._arrayIndexes.length) {
					// Möglicherweise kann das eintreten, aber nicht, wenn die _cellKeyColumnIndex'e gleich sind!
					throw new RuntimeException("Fehler in CSVColumn.compareTo: Arrays unterschiedlicher Größe deuten auf ein Logikwölkchen hin!");
				}
				for ( int index = 0; index < length; index++) {
					if ( _arrayIndexes[index] != o._arrayIndexes[index]) {
						return _arrayIndexes[index] - o._arrayIndexes[index];
					}
				}
				return 0;
			} // Die letzten Fälle dienen zur Sicherheit.
			else if ( _arrayIndexes != null) {
				return -1;
			} 
			else if ( o._arrayIndexes != null) {
				return 1;
			}
			else {
				return 0;
			}
		}
		
		/**
		 * Gibt den Namen zurück.
		 * 
		 * @return den Namen
 		 */
		public String getName() {
			return _name;
		}
		
		@Override
		public boolean equals( Object o) {
			if ( !(o instanceof CSVColumn)) {
				return false;
			}
			final CSVColumn otherColumn = (CSVColumn) o;
			return (compareTo(otherColumn) == 0);
		}
		
		@Override
		public int hashCode() {
			if ( !_superColumn) {
				int h = _cellKeyColumnIndex * 10;
				final int length = _arrayIndexes.length;
				for ( int index = 0; index < length; index++) {
					h += _arrayIndexes[index];
				}
				return h;
			} else {
				return 0;
			}
		}
	}
}
