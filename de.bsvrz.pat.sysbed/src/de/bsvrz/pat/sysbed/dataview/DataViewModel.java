/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.CellKeyServer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Diese Klasse verwaltet Datensätze ({@link DataTableObject}) einer Attributgruppe. Änderungen werden allen Listenern - etwa {@link DataViewPanel
 * DataViewPanels} - mitgeteilt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8128 $
 */
public class DataViewModel implements CellKeyServer {

	/** speichert die darzustellende Attributgruppe */
	private final AttributeGroup _attributeGroup;

	/** speichert alle Datensätze, die von der Applikation übergeben werden */
	private final List<DataTableObject> _dataTableObjects = new ArrayList<DataTableObject>();

	/** speichert zu jedem Objekt den letzten zu betrachtenden Datensatz, wobei der Schlüssel die Id des Systemobjects innerhalb des DataTableObjects ist */
	private final Map<Long, DataTableObject> _currentDataTableObjects = new HashMap<Long, DataTableObject>();

	/** speichert alle angemeldeten Listener */
	private final List<DataViewListener> _listener = new LinkedList<DataViewListener>();

	/** der Debug-Logger */
	@SuppressWarnings("unused")
	private final Debug _debug = Debug.getLogger();

	/* ################ Konstruktor ############## */

	/**
	 * Konstruktor.
	 *
	 * @param attributeGroup Attributgruppe, die dargestellt werden soll
	 */
	public DataViewModel(final AttributeGroup attributeGroup) {
		_attributeGroup = attributeGroup;
	}


	/**
	 * Gibt die Attributgruppe zurück.
	 *
	 * @return Attributgruppe
	 */
	public AttributeGroup getAttributeGroup() {
		return _attributeGroup;
	}

	/**
	 * Fügt einen Datensatz an bestehende Daten hinten an.
	 *
	 * @param dataTableObject neuer Datensatz
	 */
	public void addDatasetBelow(final DataTableObject dataTableObject) {
		final List<DataTableObject> dataTableObjects = new ArrayList<DataTableObject>(1);
		dataTableObjects.add(dataTableObject);
		addDatasetsBelow(dataTableObjects);
	}

	/**
	 * Fügt mehrere Datensätze an bestehende Daten hinten an.
	 *
	 * @param dataTableObjects Liste mit anzuzeigenden Datensätzen
	 */
	public void addDatasetsBelow(final List<DataTableObject> dataTableObjects) {
		synchronized(_dataTableObjects) {
			_dataTableObjects.addAll(dataTableObjects);	// einmal werden die neuen Datensätze hier abgespeichert
		}
		fireAddDatasets(dataTableObjects); // zum anderen werden die neuen Datensätze weitergereicht zur Online-Tabelle
	}

	/**
	 * Fügt einen neuen Datensatz oben in der Tabelle ein.
	 *
	 * @param dataTableObject der neue Datensatz
	 */
	public void addDatasetAbove(final DataTableObject dataTableObject) {
		synchronized(_dataTableObjects) {
			_dataTableObjects.add(0, dataTableObject);
		}
		fireAddDataset(0, dataTableObject);
	}

	/**
	 * Festlegung aller Datensätze.
	 *
	 * @param dataTableObjects Liste mit den Datensätzen
	 *
	 * @see #addDatasetAbove(DataTableObject)
	 * @see #addDatasetBelow(DataTableObject)
	 */
	public void setDatasets(final List<DataTableObject> dataTableObjects) {
		synchronized(_dataTableObjects) {
			_dataTableObjects.clear();
			_dataTableObjects.addAll(dataTableObjects);
		}
		fireSetDatasets(dataTableObjects);
	}

	/**
	 * Führt ein Update hinsichtlich der Aktualität der Datensätze durch: die übergebenene Datensätze werden als aktuellste Versionen behandelt.
	 *
	 * @param dataTableObjects auszuwertende Daten
	 */
	public void updateDatasets(final List<DataTableObject> dataTableObjects) {
		// zuerst alles was kommt in die Map einspeisen
		for(DataTableObject dataTableObject : dataTableObjects) {
			_currentDataTableObjects.put(dataTableObject.getObject().getId(), dataTableObject);
		}
		// anschließend zu allen Systemobjekten ihren Wert (=letzter gültiger Wert) rausholen
		final List<DataTableObject> datasetList = new LinkedList<DataTableObject>();
		Set<Long> keySet = _currentDataTableObjects.keySet();
		for(Long objectID : keySet) {
			datasetList.add(_currentDataTableObjects.get(objectID));
		}
		fireSetDatasets(datasetList);
	}

	/**
	 * Es werden alle Datensätze gelöscht.
	 */
	public void removeDataSets() {
		List<DataTableObject> dataTableObjects = new ArrayList<DataTableObject>(_dataTableObjects);
		synchronized(_dataTableObjects) {
			_dataTableObjects.clear();
		}
		fireRemoveDatasets(dataTableObjects);
	}

	/**
	 * Gibt alle Datensätze zurück.
	 *
	 * @return alle Datensätze
	 */
	public List<DataTableObject> getDataTableObjects() {
		return _dataTableObjects;
	}

	/*
		 * Gehört zur Implementation des CellKeyServers.
		 */

	public List<CellKey> getCellKeysBetween(final CellKey key1, final CellKey key2) {
		final List<CellKey> theCellKeys = getKeysBetweenKernel(key1, key2);
		if(theCellKeys.isEmpty()) {
			List<CellKey> reverseCallCellKeys = getKeysBetweenKernel(key2, key1);
			return reverseCallCellKeys;
		}
		else {
			return theCellKeys;
		}
	}
	/*
     * Gehört zur Implementation des CellKeyServers. 
     */

	public List<CellKey> getCellKeys(final RowKey rowKey) {
		final int index = getIndex(rowKey);
		final DataTableObject dataTableObject = _dataTableObjects.get(index);
		return dataTableObject.getAllCellKeys();
	}
	/*
     * Gehört zur Implementation des CellKeyServers. 
     */

	public List<CellKey> getCellKeysBetween(final RowKey rowKey1, final RowKey rowKey2) {
		final List<CellKey> theCellKeys = new ArrayList<CellKey>();
		final int index1 = getIndex(rowKey1);
		final int index2 = getIndex(rowKey2);
		if(index1 == -1 || index2 == -1) {
			return theCellKeys;
		}
		final int minIndex = Math.min(index1, index2);
		final int maxIndex = Math.max(index1, index2);
		for(int index = minIndex; index <= maxIndex; index++) {
			final DataTableObject dataTableObject = _dataTableObjects.get(index);
			theCellKeys.addAll(dataTableObject.getAllCellKeys());
		}
		return theCellKeys;
	}
	/*
     * Gehört zur Implementation des CellKeyServers. 
     */

	public List<CellKey> getAllCellKeys() {
		final List<CellKey> theCellKeys = new ArrayList<CellKey>();
		for(int index = 0; index < _dataTableObjects.size(); index++) {
			final DataTableObject dataTableObject = _dataTableObjects.get(index);
			theCellKeys.addAll(dataTableObject.getAllCellKeys());
		}
		return theCellKeys;
	}
	/*
     * Gehört zur Implementation des CellKeyServers. 
     */

	public List<RowKey> getRowKeysBetween(final RowKey rowKey1, final RowKey rowKey2) {
		final List<RowKey> theRowKeys = new ArrayList<RowKey>();
		final int index1 = getIndex(rowKey1);
		final int index2 = getIndex(rowKey2);
		if(index1 == -1 || index2 == -1) {
			return theRowKeys;
		}
		final int minIndex = Math.min(index1, index2);
		final int maxIndex = Math.max(index1, index2);
		for(int index = minIndex; index <= maxIndex; index++) {
			theRowKeys.add(_dataTableObjects.get(index).getRowKey());
		}
		return theRowKeys;
	}

	/* ################ Methoden für die KeysBetween-Implementation ######## */

	/*
		 * Diese Methode ist der eigentliche Kern der Methode getCellKeysBetween, die für zwei beliebige CellKeys
		 * ein nicht-leeres Ergebnis liefert, während diese Methode dies nur dann macht, wenn der Index von
		 * <code>key1</code> kleiner gleich dem von <code>key2</code> ist.
		 */

	private List<CellKey> getKeysBetweenKernel(final CellKey key1, final CellKey key2) {
		final List<CellKey> theCellKeys = new ArrayList<CellKey>();
		final int index1 = getIndex(key1);
		if(index1 == -1) {
			return theCellKeys;
		}
		final int index2 = getIndex(key2);
		if(index2 == -1) {
			return theCellKeys;
		}
		if(index1 == index2) {
			final CellKeyColumn minColumn = CellKey.minColumn(_attributeGroup, key1, key2);
			final CellKeyColumn maxColumn = CellKey.maxColumn(_attributeGroup, key1, key2);
			final DataTableObject dataTableObject = _dataTableObjects.get(index1);
			dataTableObject.appendTheKeysBetween(key1, key2, minColumn, maxColumn, theCellKeys);
		}
		else {
			final CellKey beginKey, endKey;
			final int beginIndex, endIndex;
			if(index1 <= index2) {
				beginKey = key1;
				endKey = key2;
				beginIndex = index1;
				endIndex = index2;
			}
			else {
				beginKey = key2;
				endKey = key1;
				beginIndex = index2;
				endIndex = index1;
			}
			final CellKeyColumn minColumn = CellKey.minColumn(_attributeGroup, beginKey, endKey);
			final CellKeyColumn maxColumn = CellKey.maxColumn(_attributeGroup, beginKey, endKey);

			final DataTableObject dataTableObject1 = _dataTableObjects.get(beginIndex);
			dataTableObject1.appendTheKeysFrom(beginKey, minColumn, maxColumn, theCellKeys);

			for(int index = beginIndex + 1; index <= endIndex - 1; index++) {
				final DataTableObject dataTableObject = _dataTableObjects.get(index);
				dataTableObject.appendTheKeysBetween(minColumn, maxColumn, theCellKeys);
			}

			final DataTableObject dataTableObject2 = _dataTableObjects.get(endIndex);
			dataTableObject2.appendTheKeysTo(endKey, minColumn, maxColumn, theCellKeys);
		}
		return theCellKeys;
	}

	/*
	 * Der Index eines CellKeys ist der Index seines Datensatzes in der Liste aller Datensätze.
	 */

	private int getIndex(final CellKey key) {
		if(key == null) {
			return -1;
		}
		final String pidOfTheDataTableObject = key.getPidOfTheDataTableObject();
		if(pidOfTheDataTableObject == null) {
			return -1;
		}
		final long dataIndex = key.getDataIndex();
		int index = 0;
		for(DataTableObject dataTableObject : _dataTableObjects) {
			if(pidOfTheDataTableObject.equals(dataTableObject.getObject().getPidOrId())) {
				if(dataIndex == dataTableObject.getDataIndex()) {
					return index;
				}
			}
			index++;
		}
		return -1;
	}

	/*
		 * Der Index eines RowKeys ist der Index seines Datensatzes in der Liste aller Datensätze.
		 */

	private int getIndex(final RowKey key) {
		if(key == null) {
			return -1;
		}
		final String pidOfDataTableObject = key.getPidOfDataTableObject();
		if(pidOfDataTableObject == null) {
			return -1;
		}
		final long dataIndex = key.getDataIndex();
		int index = 0;
		for(DataTableObject dataTableObject : _dataTableObjects) {
			if(pidOfDataTableObject.equals(dataTableObject.getObject().getPidOrId())) {
				if(dataIndex == dataTableObject.getDataIndex()) {
					return index;
				}
			}
			index++;
		}
		return -1;
	}

	/* ################ Listener - Funktionalität ############### */

	/**
	 * Meldet einen Listener beim Model an.
	 *
	 * @param listener der anzumeldende Listener
	 */
	public void addDataViewListener(final DataViewListener listener) {
		_listener.add(listener);
	}

	/**
	 * Meldet einen Listener vom Model ab.
	 *
	 * @param listener der abzumeldende Listener
	 */
	public void removeDataViewListener(final DataViewListener listener) {
		_listener.remove(listener);
	}

	/**
	 * Alle Listener werden benachrichtigt, dass einer oder mehrere Datensätze an die bestehenden Datensätze angefügt wurden. Diese werden auch übergeben.
	 *
	 * @param datasets neue Datensätze
	 */
	private void fireAddDatasets(final List<DataTableObject> datasets) {
		List<DataTableObject> unmodifiableList = Collections.unmodifiableList(datasets);
		for(DataViewListener dataViewListener : _listener) {
			dataViewListener.addDataTableObjects(unmodifiableList);
		}
	}

	/**
	 * Alle Listener werden benachrichtigt, dass ein Datensatz an eine bestimmte Position der bestehenden Datensätze eingefügt wurde.
	 *
	 * @param index           Position des neuen Datensatzes
	 * @param dataTableObject der neue Datensatz
	 */
	private void fireAddDataset(int index, final DataTableObject dataTableObject) {
		for(DataViewListener dataViewListener : _listener) {
			dataViewListener.addDataTableObject(index, dataTableObject);
		}
	}

	/**
	 * Alle Listener werden benachrichtigt, dass die bisherigen Datensätze gelöscht und # durch die neuen (übergebenen) Datensätze ersetzt werden. Diese werden
	 * auch übergeben.
	 *
	 * @param datasets neue Datensätze
	 */
	private void fireSetDatasets(final List<DataTableObject> datasets) {
		final List<DataTableObject> unmodifiableList = Collections.unmodifiableList(datasets);
		for(DataViewListener dataViewListener : _listener) {
			dataViewListener.setDataTableObjects(unmodifiableList);
		}
	}

	/**
	 * Alle Listener werden benachrichtigt, dass die bisherigen Datensätze gelöscht werden. Diese werden
	 * auch übergeben.
	 *
	 * @param datasets der alte Datensatz
	 */
	private void fireRemoveDatasets(final List<DataTableObject> datasets) {
		for(DataViewListener dataViewListener : _listener) {
			for(int i = datasets.size() - 1; i >= 0; i--) {
				dataViewListener.removeDataTableObject(i);
			}
		}
	}
}
