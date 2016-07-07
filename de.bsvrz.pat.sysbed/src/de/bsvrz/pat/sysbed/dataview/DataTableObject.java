/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ein Objekt dieser Klasse repräsentiert einen Datensatz in der {@link DataViewFrame Online-Tabelle}. 
 * Reichen die Informationen eines {@link ResultData} nicht aus, so steht ein weiterer Konstruktor 
 * zur Verfügung, dem neben den benötigten Werten für die Online-Tabelle die
 * {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind Art der Archivanfrage} übergeben werden kann.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DataTableObject {
	
	private final SystemObject _object;
	
	private final DataDescription _dataDescription;
	
	private final TimingType _timingType;
	
	private final long _archiveTime;
	
	private final long _dataTime;
	
	private final long _dataIndex;
	
	private final DataState _dataState;
	
	private final ArchiveDataKind _dataKind;
	
	private final Data _data;
	
	private List<CellKey> _allCellKeys = null;
	
	/**
	 * Erstellt einen Datensatz aus den Informationen eines {@link de.bsvrz.dav.daf.main.ResultData Ergebnisdatensatzes} 
	 * zur Anzeige in der {@link DataViewFrame Online-Tabelle}.
	 * 
	 * @param resultData
	 *            Ergebnisdatensatz
	 */
	public DataTableObject(final ResultData resultData) {
		this(resultData.getObject(), resultData.getDataDescription(), TimingType.DATA_TIME, 0, 
				resultData.getDataTime(), resultData.getDataIndex(), resultData.getDataState(), 
				(resultData.isDelayedData() ? ArchiveDataKind.ONLINE_DELAYED : ArchiveDataKind.ONLINE), 
				resultData.getData());
	}
	
	/**
	 * Erstellt einen Datensatz zur Anzeige in der {@link DataViewFrame Online-Tabelle}.
	 * 
	 * @param object
	 *            das Systemobjekt
	 * @param dataDescription
	 *            Datenbeschreibung besteht aus der Attributgruppe, Aspekt und der Simulationsvariante
	 * @param timingType
	 *            gibt an, welcher der drei Typen (Archivzeit, Datenzeit und Datenindex) angezeigt werden soll
	 * @param archiveTime
	 *            der Archivzeitstempel
	 * @param dataTime
	 *            der Datenzeitstempel
	 * @param dataIndex
	 *            der Datenindex
	 * @param dataState
	 *            Zustand der Daten
	 * @param dataKind
	 *            Art des Datensatzes
	 * @param data
	 *            die Daten
	 */
	public DataTableObject(
			SystemObject object,
			DataDescription dataDescription,
			TimingType timingType,
			long archiveTime,
			long dataTime,
			long dataIndex,
			DataState dataState,
			ArchiveDataKind dataKind,
			Data data) {
		_object = object;
		_dataDescription = dataDescription;
		_timingType = timingType;
		_archiveTime = archiveTime;
		_dataTime = dataTime;
		_dataIndex = dataIndex;
		_dataState = dataState;
		_dataKind = dataKind;
		_data = data;
	}
	
	/**
	 * Gibt das Systemobjekt zurück.
	 * 
	 * @return das Systemobjekt
	 */
	public SystemObject getObject() {
		return _object;
	}
	
	/**
	 * Gibt die Datenbeschreibung (Attributgruppe, Aspekt und Simulationsvariante) zurück.
	 * 
	 * @return die Datenbeschreibung
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}
	
	/**
	 * Gibt an, welcher Zeitstempel angezeigt werden soll.
	 * 
	 * @return die Art des Zeitstempels, der angezeigt werden soll
	 */
	public TimingType getTimingType() {
		return _timingType;
	}
	
	/**
	 * Gibt die Archivzeit zurück.
	 * 
	 * @return die Archivzeit
	 */
	public long getArchiveTime() {
		return _archiveTime;
	}
	
	/**
	 * Gibt die Datenzeit zurück.
	 * 
	 * @return die Datenzeit
	 */
	public long getDataTime() {
		return _dataTime;
	}
	
	/**
	 * Gibt den Datenindex zurück.
	 * 
	 * @return der Datenindex
	 */
	public long getDataIndex() {
		return _dataIndex;
	}
	
	/**
	 * Gibt den Zustand der Daten zurück.
	 * 
	 * @return der Zustand der Daten
	 */
	public DataState getDataState() {
		return _dataState;
	}
	
	/**
	 * Gibt die Art des Datensatzes zurück (ob OA = online aktuell, ON = online nachgeliefert, 
	 * NA = nachgefordert aktuell, NN = nachgefordert nachgeliefert -
	 * siehe auch Technische Anforderungen Archivsystem).
	 * 
	 * @return die Art des Datensatzes
	 */
	public ArchiveDataKind getDataKind() {
		return _dataKind;
	}
	
	/**
	 * Gibt die Daten dieses Datensatzes zurück.
	 * 
	 * @return die Daten dieses Datensatzes
	 */
	public Data getData() {
		return _data;
	}
	
	/**
	 * Gibt eine Liste aller CellKeys des Datensatzes zurück.
	 * 
	 * @return 
	 */
	public List<CellKey> getAllCellKeys() {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		}
		return _allCellKeys;
	}
	
	/**
	 * Gibt einen RowKey für den Datensatz zurück.
	 * 
	 * @return
	 */
	public RowKey getRowKey() {
		return new RowKey(getObject().getPidOrId() + RowKey.getSeparator() + getDataIndex());
	}

	/**
	 * Hängt an die übergebene Liste <code>theCellKeys</code> alle CellKeys, die zwischen CellKey
	 * <code>key1</code> und CellKey <code>key2</code> liegen.
	 * 
	 * @param key1 ein CellKey
	 * @param key2 ein CellKey
	 * @param theCellKeys die Liste, an die die CellKeys angehängt werden
	 */
	public void appendTheKeysBetween( 
			final CellKey key1, 
			final CellKey key2, 
			final List<CellKey> theCellKeys) {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		}
		if ( key1.isSuperColumn() || key2.isSuperColumn()) {
			theCellKeys.addAll( _allCellKeys);
		} else {
			final Iterator<CellKey> iterator = _allCellKeys.iterator();
			while( iterator.hasNext()) {
				final CellKey cellKey = iterator.next();
				final AttributeGroup attributeGroup = _dataDescription.getAttributeGroup();
				if ( key1.attributeNameIsLessOrEqual( attributeGroup, cellKey) && 
						cellKey.attributeNameIsLessOrEqual( attributeGroup, key2)) {
					theCellKeys.add( cellKey);
				}
			}
		}
	}
	
	/**
	 * Fügt am Ende der Liste alle die CellKeys an, die zwischen den übergebenen CellKeys
	 * und zwischen den übergebenen Spalten liegen.
	 *  
	 * @param key1 ein CellKey
	 * @param key2 ein CellKey
	 * @param minColumn die Anfangs-CellKeyColumn
	 * @param maxColumn die End-CellKeyColumn
	 * @param theCellKeys die Liste, an die die CellKeys angehängt werden
	 */
	public void appendTheKeysBetween( 
			final CellKey key1, 
			final CellKey key2,
			final CellKeyColumn minColumn, 
			final CellKeyColumn maxColumn, 
			final List<CellKey> theCellKeys) {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		}
		if ( key1.isSuperColumn() || key2.isSuperColumn()) {
			theCellKeys.addAll( _allCellKeys);
		} else {
			final Iterator<CellKey> iterator = _allCellKeys.iterator();
			while( iterator.hasNext()) {
				final CellKey cellKey = iterator.next();
				final AttributeGroup attributeGroup = _dataDescription.getAttributeGroup();
				if ( cellKey.isBetweenColumns( _dataDescription.getAttributeGroup(), minColumn, maxColumn)) {
					if ( key1.attributeNameIsLessOrEqual( attributeGroup, cellKey) && 
							cellKey.attributeNameIsLessOrEqual( attributeGroup, key2)) {
						theCellKeys.add( cellKey);
					}
				}
			}
		}
	}
	
	/**
	 * Fügt am Ende der Liste <code>theCellKeys</code> alle die CellKeys des Datensatzes ab dem übergebenen 
	 * CellKey <code>key</code>, die auch zwischen den übergebenen CellKeyColumns liegen, an.
	 * 
	 * @param key ab diesem CellKey wird angefügt
	 * @param minColumn die Anfangs-CellKeyColumn
	 * @param maxColumn die End-CellKeyColumn
	 * @param theCellKeys die Liste, an die die CellKeys angehängt werden
	 */
	public void appendTheKeysFrom( 
			final CellKey key, 
			final CellKeyColumn minColumn, 
			final CellKeyColumn maxColumn, 
			final List<CellKey> theCellKeys) {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		} 
		if ( key.isSuperColumn()) {
			theCellKeys.addAll( _allCellKeys);
		} else {
			final Iterator<CellKey> iterator = _allCellKeys.iterator();
			while( iterator.hasNext()) {
				final CellKey cellKey = iterator.next();
				if ( key.attributeNameIsLessOrEqual( _dataDescription.getAttributeGroup(), cellKey)) {
					if ( cellKey.isBetweenColumns( _dataDescription.getAttributeGroup(), minColumn, maxColumn)) {
						theCellKeys.add( cellKey);
					}
				}
			}
		}
	}
	
	/**
	 * Fügt am Ende der Liste alle die CellKeys des Datensatzes, die zwischen den 
	 * übergebenen Spalten liegen, an.
	 * 
	 * @param minColumn die Anfangs-CellKeyColumn
	 * @param maxColumn die End-CellKeyColumn
	 * @param theCellKeys theCellKeys die Liste, an die die CellKeys angehängt werden
	 */
	public void appendTheKeysBetween( 
			final CellKeyColumn minColumn, 
			final CellKeyColumn maxColumn, 
			final List<CellKey> theCellKeys) {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		}
		final Iterator<CellKey> iterator = _allCellKeys.iterator();
		while( iterator.hasNext()) {
			final CellKey cellKey = iterator.next();
			if ( cellKey.isBetweenColumns( _dataDescription.getAttributeGroup(), minColumn, maxColumn) ||
					cellKey.isSuperColumn()) {
				theCellKeys.add( cellKey);
			}
		}
	}
	
	/**
	 * Fügt am Ende der Liste <code>theCellKeys</code> alle die CellKeys des Datensatzes bis zu dem übergebenen 
	 * CellKey <code>key</code>, die auch zwischen den übergebenen CellKeyColumns liegen, an.
	 * 
	 * @param key bis zu diesem CellKey wird angefügt
	 * @param minColumn die Anfangs-CellKeyColumn
	 * @param maxColumn die End-CellKeyColumn
	 * @param theCellKeys die Liste, an die die CellKeys angehängt werden
	 */
	public void appendTheKeysTo( 
			final CellKey key, 
			final CellKeyColumn minColumn, CellKeyColumn maxColumn,
			final List<CellKey> theCellKeys) {
		if ( _allCellKeys == null) {
			initAllCellKeys();
		}
		final Iterator<CellKey> iterator = _allCellKeys.iterator();
		while( iterator.hasNext()) {
			final CellKey cellKey = iterator.next();
			if ( cellKey.attributeNameIsLessOrEqual( _dataDescription.getAttributeGroup(), key)) {
				if ( cellKey.isBetweenColumns( _dataDescription.getAttributeGroup(), minColumn, maxColumn)) {
					theCellKeys.add( cellKey);
				}
			}
		}
	}
	
	private void initAllCellKeys() {
		_allCellKeys = new ArrayList<CellKey>();
		String prefix = getObject().getPidOrId() + CellKey.getFIRST_SEPARATOR() + getDataIndex() + 
			CellKey.getFIRST_SEPARATOR() + _dataDescription.getAttributeGroup().getPidOrId();
		appendCellKeys( _data, prefix, _allCellKeys);
	}
	
	@SuppressWarnings("unchecked")
	private void appendCellKeys( final Data data, String prefix, final List<CellKey> cellKeys) {
		if ( data == null) {
			final CellKey newCellKey = new CellKey( prefix, true);
			newCellKey.setCellText( DataTableObjectRenderer.getTextForState(getDataState()));
			cellKeys.add( newCellKey);
			return;
		}
		if ( data.isPlain()) {
			final CellKey newCellKey = new CellKey( prefix, false);
			newCellKey.setCellText( data.valueToString());
			cellKeys.add( newCellKey);
		}
		else {
			if ( data.isList()) {
				Iterator it = data.iterator();
				while( it.hasNext()) {
					Data nextData = (Data)it.next();
					String nextPrefix = prefix + CellKey.getSECOND_SEPARATOR() + nextData.getName();
					appendCellKeys( nextData, nextPrefix, cellKeys);
				}
			}
			if ( data.isArray()) {
				Data.Array dataArray = data.asArray();
				final int length = dataArray.getLength();
				if ( length == 0) { // um auch leere Arrays selektierbar zu machen
					cellKeys.add( new CellKey( prefix, false));
				}
				for(int i = 0; i < length; i++) {
					Data nextData = dataArray.getItem(i);
					String nextPrefix = prefix + "[" + i + "]";
					appendCellKeys(nextData, nextPrefix, cellKeys);
				}
			}
		}
	}
	
	/**
	 * Diese Klasse kapselt die Id eines DataTableObjects, die aus der Systemobject-Id und dem
	 * Datensatz-Index besteht.
	 * 
	 * @author Kappich Systemberatung
	 * @version $Revision$
	 *
	 */
	public class DataTableObjectId {
		final long _systemObjectId;
		final long _dataTableObjectIndex;
		
		/**
		 * Konstruiert ein DataTableObjectId aus einem DataTableObject, indem es Systemobject-Id und 
		 * Datensatz-Index übernimmt. 
		 * 
		 * @param dataTableObject ein Datensatz
		 */
		public DataTableObjectId( final DataTableObject dataTableObject) {
			_systemObjectId = dataTableObject.getObject().getId();
			_dataTableObjectIndex = dataTableObject.getDataIndex();
		}
		
		@Override
		public boolean equals( final Object o) {
			if ( o instanceof DataTableObjectId) {
				final DataTableObjectId oo = (DataTableObjectId) o; 
				return ((_systemObjectId == oo._systemObjectId) && 
						(_dataTableObjectIndex == oo._dataTableObjectIndex)); 
			}
			return false;
		}
		
		@Override
        public int hashCode() {
			final Long h = _systemObjectId + _dataTableObjectIndex;
			return h.intValue();
		}
	}
	
	public DataTableObjectId getDataTableObjectId() {
		return new DataTableObjectId( this);
	}
	
	@Override
	public String toString() {
		return "DataTableObject{" + "_object=" + _object + ", _dataDescription=" + _dataDescription 
		+ ", _timingType=" + _timingType + ", _archiveTime=" + _archiveTime + 
		", _dataTime=" + _dataTime + ", _dataIndex=" + _dataIndex + ", _dataState=" + 
		_dataState + ", _dataKind=" + _dataKind + ", _data=" + _data + "}";
	}
}
