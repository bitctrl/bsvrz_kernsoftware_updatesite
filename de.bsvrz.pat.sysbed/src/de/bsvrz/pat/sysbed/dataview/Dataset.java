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

import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionManager;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse speichert die Verbindung zwischen einem {@link DataTableObject} 
 * dessen hierarchischer Struktur der Form {@link RowData}. Zusätzlich können die Komponenten 
 * eines Zeilenkopfes und einer Nutzdatenzeile angefordert werden.
 * 
 * Alle abrufbaren Informationen werden erst beim ersten Abruf konstruiert.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8082 $
 * 
 * @deprecated Der Klassenname ist komplett irreführend, denn es handelt sich nicht um etwas
 * datensatz-ähnliches. Die Klasse wurde in DataTableObjectRenderer umbenannt.
 */
@Deprecated
public class Dataset {
	
	/** speichert einen Datensatz vom Datenverteiler */
	private final DataTableObject _dataTableObject;
	
	/** speichert die Struktur des Spaltenheaders, damit die Nutzdaten damit verknüpft werden können */
	private final HeaderGrid _header;
	
	/** speichert die hierarchischen Struktur des Datensatzes */
	private RowData _rowData = null;
	
	/** speichert die zum Datensatz gehörende Zeilenkopf-Komponente */
	private JComponent _rowHeaderRow = null;
	
	/** speichert die zum Datensatz gehörende Datenzeilen-Komponente */
	private JComponent _viewportRow = null;
	
	/** speichert die Höhe der Komponente einer Zeile */
	private int _height = -1;
	
	/** speichert die Breite des Headers */
	private int _headerWidth = 0;
	
	/** speichert den SelectionManager */
	private final SelectionManager _selectionManager;
	
	/**
	 * Konstruktor, dem ein Datensatz zugewiesen wird.
	 * 
	 * @param head
	 *            Element, welches die hierarchische Struktur darstellt
	 * @param dataTableObject
	 *            neuer Datensatz
	 *            
	 * @deprecated Man benutze {@link DataTableObjectRenderer} stattdessen.
	 */
	@Deprecated
    public Dataset( final HeaderGrid header, final DataTableObject dataTableObject, 
			final SelectionManager selectionManager) {
		_header = header;
		_dataTableObject = dataTableObject;
		_selectionManager = selectionManager;
	}
	
	/**
	 * Gibt den Datensatz zurück.
	 * 
	 * @return Datensatz
	 */
	public DataTableObject getDataTableObject() {
		return _dataTableObject;
	}
	
	/**
	 * Gibt die hierarchische Struktur des Datensatzes zurück.
	 * 
	 * @return hierarchische Struktur des Datensatzes
	 */
	public RowData getRowData() {
		if(_rowData == null) {
			createRowData();
		}
		return _rowData;
	}
	
	/**
	 * Gibt die Komponente des Zeilenkopfes zurück.
	 * 
	 * @param timeFormat
	 *            das gewünschte Format
	 * 
	 * @return Komponente des Zeilenkopfes
	 */
	public JComponent getRowHeaderRow(String timeFormat) {
		if(_rowHeaderRow == null) {
			_rowHeaderRow = createRowHeaderRow(_dataTableObject, timeFormat);
		}
		return _rowHeaderRow;
	}
	
	/**
	 * Gibt die Komponente der Datenzeile zurück.
	 * 
	 * @return Komponente der Datenzeile
	 */
	public JComponent getViewportRow() {
		if(_viewportRow == null) {
			createViewportRow();
		}
		return _viewportRow;
	}
	
	/**
	 * Gibt die Höhe dieser Zeile in Pixel zurück.
	 * 
	 * @return Höhe dieser Zeile
	 */
	public int getHeight() {
		if(_height == -1) {
			setHeight();
		}
		return _height;
	}
	
	/**
	 * Erstellt die Verbindungen zwischen den Daten und dem Header. Jedes Blattelement meldet 
	 * sich beim entsprechenden Blatt im Header an, damit etwaige Größenänderungen vom Header 
	 * an die Datenstruktur übergeben werden kann.
	 */
	public void setLinks() {
		_rowHeaderRow = null; // wenn die Elemente verbunden werden sollen, müssen die Komponenten erst gelöscht
		_viewportRow = null; // werden. Nach dem nächsten Erstellen, benutzen sie auch die Informationen aus dem
		// Header
		if(_rowData == null) {
			createRowData();
		}
		linkData(_rowData, _header);
		createViewportRow();
	}
	
	/** Entfernt alle Einträge, außer den Datensatz und die Höhe einer Zeile. */
	public void unsetLinks() {
		// löscht alles außer ResultData und Height (Objekte einschließlich Verweise)
		if(_rowData != null) { // nur dann macht es Sinn, sonst gibt es nichts zum Löschen
			unlinkData(_rowData, _header);
		}
		removeComponents();
	}
	
	/* ################# Private Methoden ############# */

	/** Erzeugt aus einem Datensatz eine hierarchische Struktur. */
	private void createRowData() {
		_rowData = new RowData( _dataTableObject, _selectionManager);
	}
	

	/**
	 * Rekursive Hilfsmethode. Sie wird von {@link #setLinks()} aufgerufen. Die Verbindungen zum 
	 * Spaltenheader werden hergestellt und die Breite der Komponenten, welche die Daten anzeigen 
	 * wird initial festgelegt. Diese Methode wird nur ausgeführt, wenn im Datensatz auch Daten 
	 * vorhanden sind.
	 * 
	 * @param rowData
	 *            darzustellende Daten
	 * @param headerGrid
	 *            Spaltenheader
	 */
	@SuppressWarnings("unchecked")
    private void linkData(RowData rowData, HeaderGrid headerGrid) {
		if(_dataTableObject != null) { // _resultData.getData() != null
			if(_dataTableObject.getData() == null) {
				// Idee: bis in die Blätter gehen, dort anmelden und die Summe ergibt dann die Breite 
				// des leeren Datensatzes
				_headerWidth = 0;
				getHeaderWidth(headerGrid, rowData);
				rowData.setInitialWidth(_headerWidth);
			}
			else {
				if(rowData.getSuccessors().isEmpty()) { // RowData ist Blatt
					int width = headerGrid.getHeaderElement().getSize().width + headerGrid.getSplitter().getSize().width;
					rowData.setInitialWidth(width); // Breite mitteilen
					headerGrid.addColumnWidthChangeListener(rowData); // beim Listener anmelden
				}
				else { // RowData ist kein Blatt
					// entweder sind alle vom Typ RowData oder vom Typ RowSuccessor je nachdem, ob Array oder nicht
					List<Object> array = rowData.getSuccessors();
					if(!rowData.isArray()) { // kein Array -> alle Nachfolger vom Typ RowData
						Iterator gridIt = headerGrid.getHeaderSuccessors().iterator();
						Iterator rowIt = array.iterator();
						while(rowIt.hasNext() && gridIt.hasNext()) {
							RowData nextRowData = (RowData)rowIt.next();
							HeaderGrid nextHeaderGrid = (HeaderGrid)gridIt.next();
							linkData(nextRowData, nextHeaderGrid);
						}
					}
					else {
						for(Iterator iterator = array.iterator(); iterator.hasNext();) {
							RowSuccessor rowSuccessor = (RowSuccessor)iterator.next();
							if(headerGrid.getHeaderSuccessors().isEmpty()) {
								for(final RowData rowData1 : rowSuccessor.getSuccessors()) {
									RowData nextRowData = (RowData)rowData1;
									linkData(nextRowData, headerGrid);
								}
							}
							else {
								Iterator gridIt = headerGrid.getHeaderSuccessors().iterator();
								Iterator succIt = rowSuccessor.getSuccessors().iterator();
								while(succIt.hasNext() && gridIt.hasNext()) {
									RowData nextRowData = (RowData)succIt.next();
									HeaderGrid nextHeaderGrid = (HeaderGrid)gridIt.next();
									linkData(nextRowData, nextHeaderGrid);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void getHeaderWidth(HeaderGrid headerGrid, RowData rowData) {
		List<HeaderGrid> succs = headerGrid.getHeaderSuccessors();
		if(succs.isEmpty()) { // Blattknoten erreicht
			_headerWidth += (headerGrid.getHeaderElement().getSize().width + headerGrid.getSplitter().getSize().width);
			headerGrid.addColumnWidthChangeListener(rowData);
		}
		else {
			for(HeaderGrid grid : succs) {
				getHeaderWidth(grid, rowData);
			}
		}
	}
	
	/**
	 * Rekursive Hilfsmethode. Sie entfernt die Verbindungen zwischen den Daten und dem Spaltenheader. Diese Methode 
	 * wird nur ausgeführt, wenn auch Daten vorhanden sind.
	 * 
	 * @param rowData
	 *            Daten, die mit dem Spaltenheader verbunden sind
	 * @param headerGrid
	 *            Spaltenheader
	 */
	@SuppressWarnings("unchecked")
    private void unlinkData(RowData rowData, HeaderGrid headerGrid) {
		if(_dataTableObject != null) {
			if(rowData.getSuccessors().isEmpty()) {
				headerGrid.removeColumnWidthChangeListener(rowData);
			}
			else {
				// entweder sind alle vom Typ RowData oder vom Typ RowSuccessor
				List<Object> array = rowData.getSuccessors();
				Object object = array.get(0);
				if(object instanceof RowData) {
					Iterator gridIt = headerGrid.getHeaderSuccessors().iterator();
					Iterator rowIt = array.iterator();
					while(rowIt.hasNext() && gridIt.hasNext()) {
						RowData nextRowData = (RowData)rowIt.next();
						HeaderGrid nextHeaderGrid = (HeaderGrid)gridIt.next();
						unlinkData(nextRowData, nextHeaderGrid);
					}
				}
				if(object instanceof RowSuccessor) {
					for(Iterator iterator = array.iterator(); iterator.hasNext();) {
						RowSuccessor rowSuccessor = (RowSuccessor)iterator.next();
						if(headerGrid.getHeaderSuccessors().isEmpty()) {
							for(final RowData rowData1 : rowSuccessor.getSuccessors()) {
								RowData nextRowData = (RowData)rowData1;
								unlinkData(nextRowData, headerGrid);
							}
						}
						else {
							Iterator gridIt = headerGrid.getHeaderSuccessors().iterator();
							Iterator succIt = rowSuccessor.getSuccessors().iterator();
							while(succIt.hasNext() && gridIt.hasNext()) {
								RowData nextRowData = (RowData)succIt.next();
								HeaderGrid nextHeaderGrid = (HeaderGrid)gridIt.next();
								unlinkData(nextRowData, nextHeaderGrid);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Erzeugt anhand der Daten eine neue Zeile im Zeilenheader, bestehend aus einem Zeitstempel und 
	 * dem dazugehörenden Objekt.
	 * 
	 * @param dataTableObject
	 *            das Objekt der Online-Tabelle
	 * @param format
	 *            das Zeitformat, um Datum und Zeit darzustellen
	 * 
	 * @return der erstellte Zeilen-Header
	 */
	private JComponent createRowHeaderRow(final DataTableObject dataTableObject, String format) {
		final ArchiveDataKind dataKind = dataTableObject.getDataKind();
		final SystemObject systemObject = dataTableObject.getObject();
		String dataKindText;
		String dataKindTooltipText;
		if(dataKind == ArchiveDataKind.ONLINE) {
			dataKindText = "OA";
			dataKindTooltipText = "online aktueller Datensatz";
		}
		else if(dataKind == ArchiveDataKind.ONLINE_DELAYED) {
			dataKindText = "ON";
			dataKindTooltipText = "online nachgelieferter Datensatz";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED) {
			dataKindText = "NA";
			dataKindTooltipText = "nachgefordert aktueller Datensatz";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED_DELAYED) {
			dataKindText = "NN";
			dataKindTooltipText = "nachgefordert nachgelieferter Datensatz";
		}
		else {
			dataKindText = "??";
			dataKindTooltipText = "Art des Datensatzes nicht bekannt";
		}
		JLabel dataKindLabel = new JLabel(dataKindText);
		dataKindLabel.setToolTipText(dataKindTooltipText);
		dataKindLabel.setBorder(new EtchedBorder());
		dataKindLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dataKindLabel.setVerticalAlignment(SwingConstants.NORTH);
		
		JPanel dataKindPanel = new JPanel(new BorderLayout());
		dataKindPanel.add(dataKindLabel, BorderLayout.CENTER);
		
		DateFormat timeFormat = new SimpleDateFormat(format);
		String archiveTime = timeFormat.format(new Date(dataTableObject.getArchiveTime()));
		String dataTime = timeFormat.format(new Date(dataTableObject.getDataTime()));
		long dataIndex = _dataTableObject.getDataIndex();
		String dataIndexString = (dataIndex >>> 32) + "#" + ((dataIndex >> 2) & 0x3fffffff) + "#" + (dataIndex & 0x3);
		
		String labelText = "";
		if(_dataTableObject.getTimingType() == TimingType.ARCHIVE_TIME) {
			labelText = archiveTime;
		}
		else if(_dataTableObject.getTimingType() == TimingType.DATA_INDEX) {
			labelText = dataIndexString;
		}
		else {
			labelText = dataTime;
		}
		
		String toolTipText = "<html>";
		toolTipText += "Datenzeit: " + dataTime + "<br>";
		if(dataTableObject.getArchiveTime() > 0) {
			toolTipText += "Archivzeit: " + archiveTime + "<br>";
		}
		toolTipText += "Datenindex: " + dataIndexString;
		toolTipText += "</html>";
		
		//		JLabel timeLabel = new JLabel(timeFormat.format(new Date(dataTableObject.getDataTime())));
		JLabel timeLabel = new JLabel(labelText);
		timeLabel.setBorder(new EtchedBorder());
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timeLabel.setVerticalAlignment(SwingConstants.NORTH);
		
		timeLabel.setToolTipText(toolTipText);
		
		String object = systemObject.getNameOrPidOrId();
		JLabel objectLabel = new JLabel(object);
		objectLabel.setBorder(new EtchedBorder());
		objectLabel.setHorizontalAlignment(SwingConstants.CENTER);
		objectLabel.setVerticalAlignment(SwingConstants.NORTH);
		
		JPanel gridPanel = new JPanel(new GridLayout(1, 2));
		gridPanel.add(timeLabel);
		gridPanel.add(objectLabel);
		
		JPanel rowHeaderPanel = new JPanel(new BorderLayout());
		rowHeaderPanel.add(dataKindPanel, BorderLayout.WEST);
		rowHeaderPanel.add(gridPanel, BorderLayout.CENTER);
		
		return rowHeaderPanel;
	}
	
	/** Erzeugt aus einem Datensatz eine Swing-Komponente, damit die Daten angezeigt werden können. */
	private void createViewportRow() {
		if(_rowData == null) {
			createRowData();
		}
		_viewportRow = _rowData.createComponent();
		_height = _viewportRow.getPreferredSize().height;
	}
	
	/**
	 * Ermittelt die Höhe der diesen Datensatz repräsentierenden Swing-Komponente. Falls die Komponenten 
	 * extra für die Ermittlung der Höhe erzeugt werden, dann werden sie anschließend auch wieder gelöscht.
	 */
	private void setHeight() {
		if(_viewportRow == null) {
			createViewportRow();
			removeComponents();
		}
		else {
			_height = _viewportRow.getPreferredSize().height;
		}
	}
	
	/** Löscht alle nicht mehr benötigten Komponenten, außer des Datensatzes, des Spaltenheaders und der Höhe. */
	private void removeComponents() {
		_rowData = null;
		_rowHeaderRow = null;
		_viewportRow = null;
	}
}
