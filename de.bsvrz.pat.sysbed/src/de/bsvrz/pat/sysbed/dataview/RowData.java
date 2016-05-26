/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionManager;
import de.bsvrz.sys.funclib.debug.Debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Diese Klasse bietet eine hierarchische Struktur für einen Datensatz aus dem Datenverteiler. 
 * Es werden Attribute, Listen, Arrays von Attributen und Arrays von Listen berücksichtigt.
 * 
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see Dataset
 */
public class RowData implements ColumnWidthChangeListener {
	
	/** Der Debug-Logger */
	private static final Debug _debug = Debug.getLogger();
	
	/** speichert den anzuzeigenden Text dieses Feldes */
	private String _value = "";
	
	/** Gibt an, ob sich dieses Objekt um ein Array von Listen/Attributen handelt, oder nicht. */
	private boolean _isArray = false;
	
	/** speichert die Komponente, die aus sich selbst und allen Nachfolgern besteht */
	private JComponent _component;
	
	/** speichert die initiale Breite dieser Komponente */
	private int _initialWidth = 0;
	
	/** speichert die optimale Spaltenbreite */
	private int _optimalColumnWidth = 0;
	
	/**
	 * Speichert alle Nachfolger in einer Liste. Die Nachfolger sind vom Typ {@link RowSuccessor} 
	 * oder {@link RowData}, je nachdem, ob _isArray <code>true</code> oder <code>false</code> ist.
	 */
	private final List<Object> _successors = new ArrayList<Object>();
	
	/** der empfangene Datensatz */
	private final DataTableObject _dataTableObject;
	
	/** Key zur eindeutigen Identifizierung */
	private CellKey _cellKey;
	
	/** der Selektion-Manager */
	private final SelectionManager _selectionManager;
	
	/**
	 * Setzt den CellKey des Objekts.
	 * 
	 * @param key
	 */
	public void setCellKey(CellKey key) {
		_cellKey = key;
	}
	
	/**
	 * Gibt den CellKey des Objekts zurück.
	 * 
	 * @return CellKey
	 */
	public CellKey getCellKey() {
		return _cellKey;
	}
	
	/**
	 * Stellt die Daten eines Ergebnisdatensatzes in der Onlinetabelle dar.
	 * 
	 * @param dataTableObject
	 *            ein Datensatz der Online-Tabelle
	 */
	public RowData( final DataTableObject dataTableObject, SelectionManager selectionManager) {
		_dataTableObject = dataTableObject;
		_selectionManager = selectionManager;
		if ( dataTableObject != null) {
			initHierarchy();
		}
	}
	
	/**
	 * Gibt die Komponente zurück, die sich selbst und alle ihre Nachfolger darstellt.
	 * 
	 * @return Komponente, die sich selbst und alle ihre Nachfolger darstellt
	 */
	public JComponent getComponent() {
		return _component;
	}
	
	/**
	 * Setzt die initiale Breite der Komponente, die sich selbst und alle Nachfolger darstellt.
	 * 
	 * @param width
	 *            die neue Breite dieser Komponente
	 */
	public void setInitialWidth(int width) {
		_initialWidth = width;
	}
	
	/**
	 * Gibt die initiale Spaltenbreite zurück.
	 * 
	 * @return initiale Spaltenbreite
	 */
	public int getInitialWidth() {
		return _initialWidth;
	}
	
	/**
	 * Erzeugt die Komponente, die sich selbst und alle Nachfolger darstellt.
	 * 
	 * @return die Komponente, die sich selbst und alle Nachfolger darstellt
	 */
	public JComponent createComponent() {
		if((_dataTableObject == null) || (_dataTableObject.getData() != null)) {
			if(_successors.isEmpty()) {
				_component = new RowElement(_value);
				_cellKey.setCellText( _value);
			}
			else {
				if(!_isArray) { // handelt sich um eine Liste - Einträge sind RowData
					JPanel panel = new JPanel(new BorderLayout());
					GridBagLayout gbl = new GridBagLayout();
					JPanel listPanel = new JPanel();
					listPanel.setLayout(gbl);
					int column = 0;
					
					for(Object object : _successors) {
						if(object instanceof RowData) {
							RowData rowData = (RowData)object;
							
							JComponent row = rowData.createComponent();
							
							GridBagConstraints gbc = makeGBC(column++, 0, 1, 1, 100., 100.);
							gbc.fill = GridBagConstraints.BOTH;
							gbl.setConstraints(row, gbc);
							listPanel.add(row);
						}
						else {
							_debug.error("Daten müssen vom Typ RowData sein!");
						}
					}
					panel.add(listPanel, BorderLayout.CENTER);
					_component = panel;
				}
				else { // es handelt sich um ein Array (von Lists, von Attributen)
					JPanel panel = new JPanel(new BorderLayout());
					GridBagLayout gbl = new GridBagLayout();
					JPanel arrayPanel = new JPanel();
					arrayPanel.setLayout(gbl);
					int row = 0;
					
					for(Object object : _successors) {
						if(object instanceof RowSuccessor) {
							RowSuccessor succ = (RowSuccessor)object;
							GridBagLayout gbl2 = new GridBagLayout();
							JPanel successorPanel = new JPanel();
							successorPanel.setLayout(gbl2);
							int column = 0;
							
							final List<RowData> succs = succ.getSuccessors();
							for(RowData rowData : succs) {
								JComponent rowComp = rowData.createComponent();
								GridBagConstraints gbc2 = makeGBC(column++, 0, 1, 1, 100., 100.);
								gbc2.fill = GridBagConstraints.BOTH;
								gbl2.setConstraints(rowComp, gbc2);
								successorPanel.add(rowComp);
							}
							JPanel succPanel = new JPanel();
							succPanel.setLayout(new BorderLayout());
							succPanel.add(successorPanel, BorderLayout.CENTER);
							
							GridBagConstraints gbc = makeGBC(0, row++, 1, 1, 100., 100.);
							gbc.fill = GridBagConstraints.BOTH;
							gbl.setConstraints(succPanel, gbc);
							arrayPanel.add(succPanel);
						}
						else {
							_debug.error("Daten müssen vom Typ RowSuccessor sein!");
						}
					}
					panel.add(arrayPanel, BorderLayout.CENTER);
					_component = panel;
				}
			}
		}
		else {
			DataState dataState = _dataTableObject.getDataState();
			String textForState = DataTableObjectRenderer.getTextForState(dataState);
			final Color color = DataTableObjectRenderer.getColorForState(dataState);
			_component = new RowPanel(textForState, color);
			_cellKey.setCellText( textForState);
		}
		
		_component.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				int modifiersEx = e.getModifiersEx();
				_selectionManager.mousePressed(_cellKey, modifiersEx);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				int modifiersEx = e.getModifiersEx();
				
				if((modifiersEx & MouseEvent.CTRL_DOWN_MASK) > 0) {
					_selectionManager.mouseReleased(_cellKey, MouseEvent.CTRL_DOWN_MASK);
				}
				else if((modifiersEx & MouseEvent.SHIFT_DOWN_MASK) > 0) {
					_selectionManager.mouseReleased(_cellKey, MouseEvent.SHIFT_DOWN_MASK);
				}
				else {
					_selectionManager.mouseReleased(_cellKey, 0);
				}
				
			}
		});
		
		_optimalColumnWidth = _component.getPreferredSize().width;
		
		if(_initialWidth > 0) {
			_component.setPreferredSize(new Dimension(_initialWidth, _component.getPreferredSize().height));
		}
		
		if(_cellKey != null) {
			if ( _component instanceof RowElement) {
				final RowElement rowElement = (RowElement) _component;
				rowElement.setSelectionColors( _selectionManager.isCellKeySelected( _cellKey));
			}
			else if ( _component instanceof RowPanel) {
				final RowPanel rowPanel = (RowPanel) _component;
				rowPanel.setSelectionBorder( _selectionManager.isCellKeySelected( _cellKey));
			}
		}
		return _component;
	}
	
	/**
	 * Hilfsfunktion zur Konstruktion des Panels. Hierüber werden die Bedingungen für die Anordnung der Elemente gesetzt.
	 * 
	 * @param gridx
	 *            Spaltennummer
	 * @param gridy
	 *            Zeilennummer
	 * @param gridwidth
	 *            Anzahl der Spalten über die das Element reicht
	 * @param gridheight
	 *            Anzahl der Zeilen über die das Element reicht
	 * @param weightx
	 *            Verteilung von zur Verfügung stehendem Platz (horizontal)
	 * @param weighty
	 *            Verteilung von zur Verfügung stehendem Platz (vertikal)
	 * 
	 * @return die Bedingungen für die Anordnung des Elements
	 */
	private GridBagConstraints makeGBC(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		gbc.gridheight = gridheight;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		return gbc;
	}
	
	/**
	 * Setzt das Flag, ob es sich hierbei um ein Array von Listen/Attributen handelt, oder nicht.
	 * 
	 * @param bool
	 *            <code>true</code>, falls es ein Array von Listen/Attributen ist, sonst <code>false</code>
	 */
	public void setIsArray(boolean bool) {
		_isArray = bool;
	}
	
	/**
	 * Gibt zurück, ob es sich um ein Array von Listen/Attributen handelt, oder nicht.
	 * 
	 * @return <code>true</code>, falls es sich um ein Array von Listen/Attributen handelt, sonst <code>false</code>
	 */
	public boolean isArray() {
		return _isArray;
	}
	
	/**
	 * Setzt den anzuzeigenden Text.
	 * 
	 * @param value
	 *            darzustellenden Text
	 */
	public void setValue(String value) {
		_value = value;
	}
	
	/**
	 * Gibt den anzuzeigenden Text zurück.
	 * 
	 * @return anzuzeigender Text
	 */
	public String getValue() {
		return _value;
	}
	
	/**
	 * Fügt einen Nachfolger vom Typ <code>RowSuccessor</code> oder <code>RowData</code> hinzu.
	 * 
	 * @param object
	 *            Nachfolger vom Typ <code>RowSuccessor</code> oder <code>RowData</code>
	 * 
	 * @see RowSuccessor
	 * @see RowData
	 */
	public void addArrayElement(Object object) {
		_successors.add(object);
	}
	
	/**
	 * Gibt alle Nachfolger zurück. Sie können vom Typ RowSuccessor oder vom Typ RowData sein, je nachdem, ob es sich um ein Array von Listen/Attributen
	 * handelt, oder nicht.
	 * 
	 * @return alle Nachfolger
	 */
	public List<Object> getSuccessors() {
		return _successors;
	}
	
	/* ############# implementiert die Methoden des RowListener-Interfaces ############## */
	/**
	 * Gibt die für diese Komponente optimale Spaltenbreite zurück.
	 * 
	 * @return die optimale Spaltenbreite
	 */
	public int getOptimalColumnWidth() {
		return _optimalColumnWidth;
	}
	
	/**
	 * Setzt die Breite der Komponente, die diese Daten repräsentiert.
	 * 
	 * @param width
	 *            neue Breite der Komponente
	 */
	public void setWidth(int width) {
		if(_dataTableObject != null) {
			if(_dataTableObject.getData() == null) {
				_component.setPreferredSize(new Dimension(width, _component.getPreferredSize().height));
				_component.setMinimumSize(new Dimension(width, _component.getPreferredSize().height));
				_component.validate();
			}
		}
		else {
			Dimension size = new Dimension(width, _component.getSize().height);
			_component.setPreferredSize(size);
			_component.setSize(size);
			_component.setMinimumSize(size);
			_component.setMaximumSize(size);
			_component.validate();
		}
	}
	
	@Override
	public String toString() {
		return "RowData{" + "_value='" + _value + "'" + ", _isArray=" + _isArray + ", _component=" + _component + ", _initialWidth=" + _initialWidth
		+ ", _optimalColumnWidth=" + _optimalColumnWidth + ", _successors=" + _successors + ", _dataTableObject=" + _dataTableObject + "}";
	}
	
	/** 
	 * Wandelt den Datensatz vom Datenverteiler in eine hierachische Struktur um.
	 * 
	 * @param data
	 *            Daten vom Datenverteiler
	 * @param rowData
	 *            neue hierarchische Struktur
	 * @param path
	 *            bildet den key
	 */
	private void initHierarchy() {
		final Data data = _dataTableObject.getData();
		final String firstDIVIDER = CellKey.getFIRST_SEPARATOR();
		final String cellKeyString = _dataTableObject.getObject().getPidOrId() + firstDIVIDER + 
		_dataTableObject.getDataIndex() + firstDIVIDER + 
		_dataTableObject.getDataDescription().getAttributeGroup().getPidOrId();
		
		setCellKey(new CellKey(cellKeyString, true));
		
		if(data == null) {
			return;
		}
		createNextLevel( this, cellKeyString, data, _selectionManager);
	}
	
	static private RowData getNextRowData( final Data data, final String path, final SelectionManager selectionManager) {
		final RowData nextRowData = new RowData( null, selectionManager);
		
		String localPath = getNextPath( data, path);
		final CellKey key = new CellKey(localPath, false);
		nextRowData.setCellKey( key);
		
		createNextLevel( nextRowData, localPath, data, selectionManager);
		
		return nextRowData;
	}
	
	static private String getNextPath( final Data data, final String path) {
		String nextPath = path;
		try {
			int parseInt = Integer.parseInt(data.getName());
			nextPath += "[" + parseInt + "]";
		}
		catch(NumberFormatException e) {
			nextPath += CellKey.getSECOND_SEPARATOR() + data.getName();
		}
		return nextPath;
	}
	
	@SuppressWarnings("unchecked")
	static private void createNextLevel( final RowData rowData, final String path,
			final Data data, final SelectionManager selectionManager) {
		if(data.isPlain()) {
			String value = data.valueToString();
			rowData.setValue(value);
		}
		else {
			if( data.isList()) { // kein Array
				Iterator it = data.iterator();
				while(it.hasNext()) {
					Data nextData = (Data)it.next();
					final RowData nextRowData = getNextRowData(nextData, path, selectionManager);
					rowData.addArrayElement(nextRowData);
				}
			}
			if(data.isArray()) { // ein Array
				rowData.setIsArray(true);
				Data.Array dataArray = data.asArray();
				for(int i = 0, n = dataArray.getLength(); i < n; i++) {
					Data nextData = dataArray.getItem(i);
					RowSuccessor succ = new RowSuccessor();
					String elementPath = path + "[" + i + "]";
					succ.setKey(new CellKey(elementPath, false));
					if(nextData.isList()) {
						Iterator it = nextData.iterator();
						while(it.hasNext()) {
							Data nextNextData = (Data)it.next();
							RowData nextRowData = getNextRowData(nextNextData, elementPath, selectionManager);
							succ.addSuccessor(nextRowData);
						}
					}
					else { // keine Liste
						final RowData nextRowData = getNextRowData(nextData, path, selectionManager);
						succ.addSuccessor(nextRowData);
					}
					rowData.addArrayElement(succ);
				}
			}
		}
	}
}
