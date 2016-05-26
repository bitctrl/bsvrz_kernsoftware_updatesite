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

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Klasse zur Verwaltung selektierter Zellen und Zeilen, die durch CellKeys und RowKeys
 * repräsentiert werden.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 * 
 */
public class SelectionManager {
	/** Ist der SelectionManager für Veränderungen komplett gesperrt. */
	boolean _locked = false;
	
	/** Die Listener, die auf Selektionsänderungen angemeldet sind. */
	private List<SelectionListener> _listeners = new CopyOnWriteArrayList<SelectionListener>();
	
	/** CellKey beim Drücken einer Maustaste */
	private CellKey _cellKeyPressed;
	
	/** CellKey beim Loslassen einer Maustaste */
	private CellKey _cellKeyReleased;
	
	/** RowKey beim Drücken einer Maustaste */
	private RowKey _rowKeyPressed;
	
	/** RowKey beim Loslassen einer Maustaste */
	private RowKey _rowKeyReleased;
	
	/** Das Objekt, das bei zellweiser Bereichsselektion die dazwischen liegenden CellKeys heranschafft. */
	private CellKeyServer _cellKeyServer;
	
	/** Liste mit den aktuell selektierten CellKeys */
	final private Set<CellKey> _selectedCellKeys = new HashSet<CellKey>();
	/** Der erste selektierte CellKey. */
	private CellKey _firstSelectedCellKey = null;
	/** Liste mit "ehemalig" selektierten CellKeys */
	private Set<CellKey> _oldCellKeys = new HashSet<CellKey>();
	
	/** Liste mit selektierten RowKeys */
	final private Set<RowKey> _selectedRowKeys = new HashSet<RowKey>();
	/** Der erste selektierte RowKey */
	private RowKey _firstSelectedRowKey = null;
	/** Liste mit "ehemalig" selektierten RowKeys */
	private Set<RowKey> _oldRowKeys = new HashSet<RowKey>();

	/** Ist das Betriebssystem Mac OS? */
	private final boolean _isMac;
	
	/**
	 * Liste der aktuell selektierten CellKeys.
	 * 
	 * @return selektierte CellKeys
	 */
	public Set<CellKey> getSelectedCellKeysAsSet() {
		return Collections.unmodifiableSet(_selectedCellKeys);
	}
	
	/**
	 * Liste der aktuell selektierten RowKeys.
	 * 
	 * @return selektierte RowKeys
	 */
	public Set<RowKey> getSelectedRowKeysAsSet() {
		return Collections.unmodifiableSet(_selectedRowKeys);
	}
	
	/**
	 * Gibt <code>true</code> zurück, wenn mindestestens ein CellKey selektiert ist, sonst <code>false</code>.
	 * 
	 * @return ist mindestestens ein CellKey selektiert?
	 */
	public boolean isSomethingSelected() {
		return !_selectedCellKeys.isEmpty();
	}
	
	/**
	 * Konstruktor zum Anlegen eines SelectionManagers.
	 * 
	 * @param keysBetween
	 *            Verwaltungsklasse der dazwischenliegenden Komponenten
	 */
	public SelectionManager(CellKeyServer keysBetween) {
		_cellKeyServer = keysBetween;
		_isMac = System.getProperty("os.name").toLowerCase().startsWith( "mac");
	}
	
	/**
	 * Diese Methode wird beim Drücken einer Taste vom MouseListener einer Zelle in RowData ausgeführt.
	 * 
	 * @param key
	 *            übergebener Schlüssel
	 * @param modifiers
	 *            gedrückte Modifiertasten
	 */
	public void mousePressed(CellKey key, int modifiers) {
		if ( _locked ) {
			return;
		}
		_cellKeyPressed = key;
		
		if(_cellKeyPressed == null) {
			throw new IllegalArgumentException("Der CellKey darf nicht null sein!");
		}
		
		final int ctrlMetaOrShiftDown;
		final int ctrlMetaDown;
		if ( !isMac()) {
			ctrlMetaOrShiftDown = MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
			ctrlMetaDown = MouseEvent.CTRL_DOWN_MASK;
		} else {
			ctrlMetaOrShiftDown = MouseEvent.META_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
			ctrlMetaDown = MouseEvent.META_DOWN_MASK;
		}
		int filteredModifiers = modifiers & ctrlMetaOrShiftDown;
		
		if( (filteredModifiers == 0) || (filteredModifiers == ctrlMetaOrShiftDown)) { // Nur die letzte Selektion zählt
			_selectedCellKeys.clear();
			_selectedCellKeys.add( key);
			_firstSelectedCellKey = key;
			_selectedRowKeys.clear();
			_firstSelectedRowKey = null;
		}
		else if( filteredModifiers == ctrlMetaDown) {	// Toggle den Selektionsstatus des CellKey
			if( isCellKeySelected(key)) {
				_selectedCellKeys.remove( key);
				if ( key == _firstSelectedCellKey) {
					_firstSelectedCellKey = null;
				}
			}
			else {
				if ( _selectedCellKeys.isEmpty()) {
					_firstSelectedCellKey = key;
				}
				_selectedCellKeys.add(key);
			}
		}
		else if( filteredModifiers == MouseEvent.SHIFT_DOWN_MASK) {	// Selektiere den Bereich vom ersten bis zum aktuell selektierten Cellkey
			if ( _selectedCellKeys.size() > 0) {
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(_firstSelectedCellKey, _cellKeyPressed);
				if ( keysBetween.isEmpty()) {
					_selectedCellKeys.clear();
					_selectedCellKeys.add( _cellKeyPressed);
					_firstSelectedCellKey = _cellKeyPressed;
				} else {
					_selectedCellKeys.clear();
					_selectedCellKeys.addAll( keysBetween);
				}
				_selectedRowKeys.clear();
				_firstSelectedRowKey = null;
			} 
			else {
				_selectedCellKeys.add(_cellKeyPressed);
				_firstSelectedCellKey = _cellKeyPressed;
			}
		}
		fireCellSelectionChangeNotification();
		fireRowSelectionChangeNotification();
	}
	
	/**
	 * Diese Methode wird beim Loslassen einer Taste vom MouseListener einer Zelle in RowData ausgeführt.
	 * 
	 * @param key
	 *            übergebener Schlüssel
	 * @param modifiers
	 *            gedrückte Modifiertasten
	 */
	public void mouseReleased(CellKey key, int modifiers) {
		if ( _locked) {
			return;
		}
		_cellKeyReleased = key;
		if(_cellKeyReleased == null) {
			throw new IllegalArgumentException("Der CellKey darf nicht null sein!");
		}
		if(_cellKeyPressed == null) {
			throw new IllegalArgumentException("Der CellKey darf nicht null sein!");
		}
		final int ctrlMetaDown;
		if ( !isMac()) {
			ctrlMetaDown = MouseEvent.CTRL_DOWN_MASK;
		} else {
			ctrlMetaDown = MouseEvent.META_DOWN_MASK;
		}
		if(!_cellKeyReleased.equals(_cellKeyPressed)) {
			if(modifiers == 0) {
				_selectedCellKeys.clear();
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(_cellKeyPressed, _cellKeyReleased);
				_selectedCellKeys.addAll( keysBetween);
				_firstSelectedCellKey = _cellKeyPressed;
			}
			else if(modifiers == ctrlMetaDown) {
				CellKey keyForKeysBetween = _cellKeyPressed;
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(keyForKeysBetween, _cellKeyReleased);
				_selectedCellKeys.addAll( keysBetween);
				_firstSelectedCellKey = keyForKeysBetween;
				
				if(isCellKeySelected(key)) {
					_selectedCellKeys.remove( key);
				}
				else {
					if ( _selectedCellKeys.isEmpty()) {
						_firstSelectedCellKey = key;
					}
					_selectedCellKeys.add(key);
				}
			}
			else if(modifiers == MouseEvent.SHIFT_DOWN_MASK) {
				CellKey newKey = _cellKeyPressed;
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(newKey, _cellKeyReleased);
				if ( _selectedCellKeys.isEmpty()) {
					_firstSelectedCellKey = newKey;
				}
				_selectedCellKeys.addAll( keysBetween);
			}
			fireCellSelectionChangeNotification();
			fireRowSelectionChangeNotification();
		}
	}
	/**
	 * Diese Methode wird von den  MouseListenern auf den Zeilen-Headern ausgeführt, wenn
	 * dort die Maus gedrückt wird.
	 * 
	 * @param key übergebener Schlüssel
	 * @param modifiers gedrückte Modifiertasten
	 */
	public void mousePressed( final RowKey key, int modifiers) {
		if ( _locked) {
			return;
		}
		_rowKeyPressed = key;
		
		if(_rowKeyPressed == null) {
			throw new IllegalArgumentException("Der RowKey darf nicht null sein!");
		}
		
		final int ctrlMetaOrShiftDown;
		final int ctrlMetaDown;
		if ( !isMac()) {
			ctrlMetaOrShiftDown = MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
			ctrlMetaDown = MouseEvent.CTRL_DOWN_MASK;
		} else {
			ctrlMetaOrShiftDown = MouseEvent.META_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
			ctrlMetaDown = MouseEvent.META_DOWN_MASK;
		}
		int filteredModifiers = modifiers & ctrlMetaOrShiftDown;
		
		if( (filteredModifiers == 0) || (filteredModifiers == ctrlMetaOrShiftDown)) { // Nur die letzte Selektion zählt
			_selectedCellKeys.clear();
			_selectedCellKeys.addAll( _cellKeyServer.getCellKeys(key));
			_firstSelectedCellKey = null;
			_selectedRowKeys.clear();
			_selectedRowKeys.add( key);
			_firstSelectedRowKey = key;
		}
		else if( filteredModifiers == ctrlMetaDown) {	// Toggle den Selektionsstatus der Zeile
			if( isRowSelected(key)) {
				_selectedRowKeys.remove( key);
				if ( key == _firstSelectedRowKey) {
					_firstSelectedRowKey = null;
				}
				final List<CellKey> unselectCellKeys = _cellKeyServer.getCellKeys( key);
				_selectedCellKeys.removeAll( unselectCellKeys);
				_firstSelectedCellKey = null;
			}
			else {
				if ( _selectedRowKeys.isEmpty()) {
					_firstSelectedRowKey = key;
				}
				_selectedRowKeys.add(key);
				final List<CellKey> selectCellKeys = _cellKeyServer.getCellKeys( key);
				_selectedCellKeys.addAll( selectCellKeys);
				_firstSelectedCellKey = null;
			}
		}
		else if( filteredModifiers == MouseEvent.SHIFT_DOWN_MASK) {	// Selektiere den Bereich vom ersten bis zum aktuell selektierten RowKey
			if ( _selectedRowKeys.size() > 0) {
				_selectedRowKeys.clear();
				_selectedRowKeys.addAll(_cellKeyServer.getRowKeysBetween( _firstSelectedRowKey, _rowKeyPressed));
				List<CellKey> cellKeysBetween = _cellKeyServer.getCellKeysBetween(_firstSelectedRowKey, _rowKeyPressed);
				_selectedCellKeys.clear();
				_selectedCellKeys.addAll( cellKeysBetween);
			} 
			else {
				_selectedRowKeys.add(_rowKeyPressed);
				_firstSelectedRowKey = _rowKeyPressed;
				List<CellKey> cellKeysBetween = _cellKeyServer.getCellKeys(_rowKeyPressed);
				_selectedCellKeys.clear();
				_selectedCellKeys.addAll( cellKeysBetween);
			}
		}
		fireCellSelectionChangeNotification();
		fireRowSelectionChangeNotification();
	}
	/**
	 * Diese Methode wird von den  MouseListenern auf den Zeilen-Headern ausgeführt, wenn
	 * dort die Maus losgelassen wird.
	 * 
	 * @param key übergebener Schlüssel
	 * @param modifiers gedrückte Modifiertasten
	 */
	public void mouseReleased( final RowKey key, int modifiers) {
		if ( _locked) {
			return;
		}
		_rowKeyReleased = key;
		if(_rowKeyReleased == null) {
			throw new IllegalArgumentException("Der RowKey darf nicht null sein!");
		}
		if(_rowKeyPressed == null) {
			throw new IllegalArgumentException("Der RowKey darf nicht null sein!");
		}
		final int ctrlMetaDown;
		if ( !isMac()) {
			ctrlMetaDown = MouseEvent.CTRL_DOWN_MASK;
		} else {
			ctrlMetaDown = MouseEvent.META_DOWN_MASK;
		}
		if(!_rowKeyReleased.equals(_rowKeyPressed)) {
			if( modifiers == 0 ) {
				_selectedCellKeys.clear();
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(_rowKeyPressed, _rowKeyReleased);
				_selectedCellKeys.addAll( keysBetween);
				_firstSelectedRowKey = _rowKeyPressed;
			}
			else if(modifiers == ctrlMetaDown) {
				RowKey rowKeyForKeysBetween = _rowKeyPressed;
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(rowKeyForKeysBetween, _rowKeyReleased);
				_selectedCellKeys.addAll( keysBetween);
				_firstSelectedRowKey = rowKeyForKeysBetween;
				
				if( isRowSelected(key)) {
					_selectedRowKeys.remove( key);
				}
				else {
					if ( _selectedRowKeys.isEmpty()) {
						_firstSelectedRowKey = key;
					}
					_selectedRowKeys.add(key);
				}
			}
			else if(modifiers == MouseEvent.SHIFT_DOWN_MASK) {
				RowKey newKey = _rowKeyPressed;
				List<CellKey> keysBetween = _cellKeyServer.getCellKeysBetween(newKey, _rowKeyReleased);
				if ( _selectedRowKeys.isEmpty()) {
					_firstSelectedRowKey = newKey;
				}
				_selectedCellKeys.addAll( keysBetween);
			}
			fireCellSelectionChangeNotification();
			fireRowSelectionChangeNotification();
		}
	}
	
	/**
	 * Diese Methode selektiert alle CellKeys.
	 */
	public void selectAllCellKeys() {
		if ( _locked) {
			return;
		}
		_cellKeyPressed = null;
		_cellKeyReleased = null;
		_rowKeyPressed = null;
		_rowKeyReleased = null;
		_firstSelectedCellKey = null;
		_selectedRowKeys.clear();
		_firstSelectedRowKey = null;
		_selectedCellKeys.addAll( _cellKeyServer.getAllCellKeys());
		fireCellSelectionChangeNotification();
		fireRowSelectionChangeNotification();
	}
	/**
	 * Diese Methode deselektiert alle CellKeys.
	 */
	public void unselectAllCellKeys() {
		if ( _locked) {
			return;
		}
		_cellKeyPressed = null;
		_cellKeyReleased = null;
		_rowKeyPressed = null;
		_rowKeyReleased = null;
		_selectedCellKeys.clear();
		_firstSelectedCellKey = null;
		_selectedRowKeys.clear();
		_firstSelectedRowKey = null;
		fireCellSelectionChangeNotification();
		fireRowSelectionChangeNotification();
	}
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene CellKey selektiert ist.
	 * 
	 * @param key ein CellKey
	 * @return <code>true</code>, wenn selektiert, sonst <code>false</code>
	 */
	public boolean isCellKeySelected(CellKey key) {
		return _selectedCellKeys.contains(key);
	}
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene RowKey selektiert ist.
	 * 
	 * @param key  RowKey
	 * @return <code>true</code>, wenn selektiert, sonst <code>false</code>
	 */
	public boolean isRowSelected( RowKey key) {
		return _selectedRowKeys.contains(key);
	}
	
	/**
	 * Diese Methode fügt einen Selektions-Listener hinzu.
	 * 
	 * @param listener ein Listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		if ( _locked) {
			return;
		}
		_listeners.add(listener);
	}
	
	/**
	 * Diese Methode entfernt den übergebenen Selektions-Listener.
	 * 
	 * @param listener ein Listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		if ( _locked) {
			return;
		}
		_listeners.remove(listener);
	}
	
	/**
	 * Diese Methode entfernt alle Selektions-Listener.
	 */
	public void removeSelectionListeners() {
		if ( _locked) {
			return;
		}
		_listeners.clear();
	}
	
	/**
	 * Diese Methode teilt geänderte CellKey-Selektionen allen Selektions-Listenern mit.
	 */
	private void fireCellSelectionChangeNotification() {
		if ( _locked) {
			return;
		}
		Set<CellKey> changedKeys = new HashSet<CellKey>();
		for(CellKey oldKey : _oldCellKeys) {
			if(!isCellKeySelected(oldKey)) {
				changedKeys.add(oldKey);
			}
		}
		for(CellKey selectedKey : _selectedCellKeys) {
			if ( !_oldCellKeys.contains( selectedKey)) {
				changedKeys.add( selectedKey);
			}
		}
		if(changedKeys.size() != 0) {
			for(SelectionListener listener : _listeners) {
				listener.cellSelectionChanged(this, changedKeys);
			}
		}

		_oldCellKeys = new HashSet<CellKey>(_selectedCellKeys);
	}
	
	/**
	 * Diese Methode teilt geänderte RowKey-Selektionen allen Selektions-Listenern mit.
	 */
	private void fireRowSelectionChangeNotification() {
		if ( _locked) {
			return;
		}
		Set<RowKey> changedKeys = new HashSet<RowKey>();
		for(RowKey oldKey : _oldRowKeys) {
			if(!isRowSelected(oldKey)) {
				changedKeys.add(oldKey);
			}
		}
		for(RowKey selectedKey : _selectedRowKeys) {
			if ( !_oldRowKeys.contains( selectedKey)) {
				changedKeys.add( selectedKey);
			}
		}
		if(changedKeys.size() != 0) {
			for(SelectionListener listener : _listeners) {
				listener.rowSelectionChanged(this, changedKeys);
			}
		}

		_oldRowKeys = new HashSet<RowKey>(_selectedRowKeys);
	}
	
	/* Methoden zur Modifikation des SelectionManagers, eingführt im Zusammenhang mit dem Drucken */
	
	/**
	 * Dies ist eine der besonderen Methoden, die im Rahmen der Einführung des Druckens
	 * hinzugefügt wurde. Diese Methode setzt, falls möglich, die selektierten CellKeys, 
	 * aber alles andere, etwa Benachrichtigung von Listenern, geschieht nicht.
	 * 
	 * @param cellKeys eine Collection von CellKeys
	 */
	public void setSelectedCellKeys(Collection<CellKey> cellKeys) {
		if ( _locked) {
			return;
		}
		_selectedCellKeys.clear();
		for ( CellKey cellKey : cellKeys) {
			_selectedCellKeys.add( cellKey);
		}
	}
	/**
	 * Dies ist eine der besonderen Methoden, die im Rahmen der Einführung des Druckens
	 * hinzugefügt wurde. Diese Methode setzt, falls möglich, die selektierten RowKeys, 
	 * aber alles andere, etwa Benachrichtigung von Listenern, geschieht nicht.
	 * 
	 *  @param cellKeys eine Collection von RowKeys
	 */
	public void setSelectedRowKeys(Collection<RowKey> rowKeys) {
		if ( _locked) {
			return;
		}
		_selectedRowKeys.clear();
		for ( RowKey rowKey : rowKeys) {
			_selectedRowKeys.add( rowKey);
		}
	}
	/**
	 * Dies ist eine der besonderen Methoden, die im Rahmen der Einführung des Druckens
	 * hinzugefügt wurde. Wird ein true übergeben, so wird der Selektions-Manager gesperrt,
	 * d.h. es sind keine Änderungen an den Selektionen möglich, bis die Sperre wieder
	 * durch einen Aufruf mit false aufgehoben wird.
	 * 
	 * @param der neue Wert
	 */
	public void lock( boolean lock) {
		_locked = lock;
	}
	
	/**
	 * Gibt true zurück, wenn das Betriebssystem ein Mac OS ist, und false sonst.
	 * 
	 * @return läuft das Programm auf einem Mac?
	 */
	private boolean isMac() {
		return _isMac;
	}
	
}
