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

import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionManager;
import de.bsvrz.pat.sysbed.main.SelectionModel;
import de.bsvrz.pat.sysbed.main.TooltipAndContextUtil;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse liefert zu einem Datensatz ({@link DataTableObject}) alle für seine Darstellung notwendigen 
 * Komponenenten, d.h. Spalten- und Zeilen-Header und auch die Felder in Form der hierarchischen Struktur 
 * eines {@link RowData}.
 * <p>
 * Alle abrufbaren Informationen werden erst beim ersten Abruf gebildet.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 11925 $
 */
public class DataTableObjectRenderer {
	
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
	
	/** speichert den RowKey */
	private final RowKey _rowKey;
	
	/**
	 * Der Konstruktor.
	 * 
	 * @param head
	 *            Element, welches die hierarchische Struktur darstellt
	 * @param dataTableObject
	 *            neuer Datensatz
	 * @param selectionManager
	 * 			  Selektions-Manager           
	 */
	public DataTableObjectRenderer( final HeaderGrid header, 
			final DataTableObject dataTableObject, 
			final SelectionManager selectionManager) {
		_header = header;
		_dataTableObject = dataTableObject;
		_selectionManager = selectionManager;
		_rowKey = new RowKey(_dataTableObject.getObject().getPidOrId() + RowKey.getSeparator() + _dataTableObject.getDataIndex());
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
	 * Gibt den RowKey zurück.
	 * 
     * @return 
     */
    public RowKey getRowKey() {
    	return _rowKey;
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
	
	/**
	 * Legt einen Text für jeden DataState fest; wird für die "Keine-Daten-Fälle" benötigt.
	 *
	 * @param dataState der Zustand des Datensatzes
	 * @return der Text
	 */
	static public String getTextForState(final DataState dataState) {
		String text;
		if(dataState == DataState.NO_DATA) {
			text = "keine Daten";
		}
		else if(dataState == DataState.NO_SOURCE) {
			text = "keine Daten (keine Quelle)";
		}
		else if(dataState == DataState.NO_RIGHTS) {
			text = "keine Daten (keine Rechte)";
		}
		else if(dataState == DataState.POSSIBLE_GAP) {
			text = "keine Daten (potentielle Datenlücke)";
		}
		else if(dataState == DataState.END_OF_ARCHIVE) {
			text = "Ende des Archivanfragezeitraums";
		}
		else if(dataState == DataState.DELETED_BLOCK) {
			text = "Gelöschter Bereich";
		}
		else if(dataState == DataState.UNAVAILABLE_BLOCK) {
			text = "Ausgelagerter Bereich";
		}
		else if(dataState == DataState.INVALID_SUBSCRIPTION) {
			text = "keine Daten (fehlerhafte Anmeldung)";
		} 
		else if (dataState == DataState.DATA) {
			text = "Nutzdaten";
		} 
		else {
			text = "keine Daten (Undefinierte Objektkodierung)";
		}
		return text;
	}
	/**
	 * Legt die Hintergrundfarbe für die "Keine-Daten-Fälle" in Abhängigkeit von dem Status fest.
	 * 
	 * @param dataState
	 * @return
	 */
	static public Color getColorForState( final DataState dataState) {
		if(dataState == DataState.NO_DATA) {
			return Color.green;
		}
		else if(dataState == DataState.NO_SOURCE) {
			return Color.orange;
		}
		else if(dataState == DataState.NO_RIGHTS) {
			return Color.red;
		}
		else if(dataState == DataState.POSSIBLE_GAP) {
			return Color.magenta;
		}
		else if(dataState == DataState.END_OF_ARCHIVE) {
			return Color.cyan;
		}
		else if(dataState == DataState.DELETED_BLOCK) {
			return Color.red;
		}
		else if(dataState == DataState.UNAVAILABLE_BLOCK) {
			return Color.yellow;
		}
		else if(dataState == DataState.INVALID_SUBSCRIPTION) {
			return Color.red;
		}
		else if(dataState == DataState.DATA) {
			return null;
		}
		else {
			return Color.red;
		}
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
					int width1 = headerGrid.getHeaderElement().getSize().width;
					int width2 = headerGrid.getSplitter().getSize().width;
					rowData.setInitialWidth(width1+width2); // Breite mitteilen
					headerGrid.addColumnWidthChangeListener(rowData);
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
	 * Ermittelt den 2-Zeichentext für die allererste Spalte der Onlinetabelle, die mit 'Art' überschrieben ist.
	 * 
	 * @param dataKind die ArchiveDataKind
	 * @return der 2-Zeichentext der Spalte 'Art'
	 */
	static public String getDatakindText( final ArchiveDataKind dataKind) {
		if(dataKind == ArchiveDataKind.ONLINE) {
			return "OA";
		}
		else if(dataKind == ArchiveDataKind.ONLINE_DELAYED) {
			return "ON";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED) {
			return "NA";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED_DELAYED) {
			return "NN";
		}
		else {
			return "??";
		}
	}
	
	/**
	 * Ermittelt den Tooltipp für die allererste Spalte der Onlinetabelle, die mit 'Art' überschrieben ist.
	 * 
	 * @param dataKind die ArchiveDataKind
	 * @return der Tooltipp der Spalte 'Art'
	 */
	static public String getDatakindTooltipText( final ArchiveDataKind dataKind) {
		if(dataKind == ArchiveDataKind.ONLINE) {
			return  "online aktueller Datensatz";
		}
		else if(dataKind == ArchiveDataKind.ONLINE_DELAYED) {
			return "online nachgelieferter Datensatz";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED) {
			return "nachgefordert aktueller Datensatz";
		}
		else if(dataKind == ArchiveDataKind.REQUESTED_DELAYED) {
			return "nachgefordert nachgelieferter Datensatz";
		}
		else {
			return "Art des Datensatzes nicht bekannt";
		}
	}
	
	/**
	 * Ermittelt den Text für die zweite Spalte der Onlinetabelle, die mit 'Zeit' überschrieben ist.
	 * 
	 * @param timingType
	 * @param archiveTime
	 * @param dataIndexString
	 * @param dataTime
	 * @return
	 */
	static public String getTimeText( final TimingType timingType, 
			final String archiveTime, final String dataIndexString, final String dataTime) {
		if(timingType == TimingType.ARCHIVE_TIME) {
			return archiveTime;
		}
		else if(timingType == TimingType.DATA_INDEX) {
			return dataIndexString;
		}
		else {
			return dataTime;
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
		final String dataKindText = getDatakindText( dataKind);
		final String dataKindTooltipText = getDatakindTooltipText( dataKind);
		JLabel dataKindLabel = new JLabel(dataKindText);
		dataKindLabel.setToolTipText(dataKindTooltipText);
		dataKindLabel.setBorder(new EtchedBorder());
		dataKindLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dataKindLabel.setVerticalAlignment(SwingConstants.NORTH);
		
		JPanel dataKindPanel = new JPanel(new BorderLayout());
		dataKindPanel.add(dataKindLabel, BorderLayout.CENTER);
		
		// Mir ist absolut nicht klar, warum hier mal dataTableObject und mal _dataTableObject benutzt wird.
		// Da createRowHeaderRow nur an genau einer Stelle aufgerufen wird, und dort _dataTableObject
		// hineingesteckt wird, ist das Argument doch sowieso dasselbe. (TN)
		DateFormat timeFormat = new SimpleDateFormat(format);
		String archiveTime = timeFormat.format(new Date(dataTableObject.getArchiveTime()));
		String dataTime = timeFormat.format(new Date(dataTableObject.getDataTime()));
		long dataIndex = _dataTableObject.getDataIndex();
		String dataIndexString = (dataIndex >>> 32) + "#" + ((dataIndex >> 2) & 0x3fffffff) + "#" + (dataIndex & 0x3);
		
		final String timeText = getTimeText( _dataTableObject.getTimingType(), archiveTime, dataIndexString, dataTime);
		
		String toolTipText = "<html>";
		toolTipText += "Datenzeit: " + dataTime + "<br>";
		if(dataTableObject.getArchiveTime() > 0) {
			toolTipText += "Archivzeit: " + archiveTime + "<br>";
		}
		toolTipText += "Datenindex: " + dataIndexString;
		toolTipText += "</html>";
		
		JLabel timeLabel = new JLabel(timeText);
		timeLabel.setBorder(new EtchedBorder());
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timeLabel.setVerticalAlignment(SwingConstants.NORTH);
		
		timeLabel.setToolTipText(toolTipText);

		final SystemObject object = dataTableObject.getObject();
		final JLabel objectLabel = new JLabel(object.getNameOrPidOrId());
		objectLabel.setBorder(new EtchedBorder());
		objectLabel.setHorizontalAlignment(SwingConstants.CENTER);
		objectLabel.setVerticalAlignment(SwingConstants.NORTH);

		objectLabel.setToolTipText(TooltipAndContextUtil.getTooltip(object));

		JPanel gridPanel = new JPanel(new GridLayout(1, 2));
		gridPanel.add(timeLabel);
		gridPanel.add(objectLabel);
		
		JPanel rowHeaderPanel = new JPanel(new BorderLayout());
		rowHeaderPanel.add(dataKindPanel, BorderLayout.WEST);
		rowHeaderPanel.add(gridPanel, BorderLayout.CENTER);
		
		if ( _selectionManager.isRowSelected( getRowKey())) {
			dataKindPanel.setBackground( Color.orange);
			gridPanel.setBackground( Color.orange);
		}
		
		final MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int modifiersEx = e.getModifiersEx();
				_selectionManager.mousePressed( _rowKey, modifiersEx);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				int modifiersEx = e.getModifiersEx();
				
				if((modifiersEx & MouseEvent.CTRL_DOWN_MASK) > 0) {
					_selectionManager.mouseReleased( _rowKey, MouseEvent.CTRL_DOWN_MASK);
				}
				else if((modifiersEx & MouseEvent.SHIFT_DOWN_MASK) > 0) {
					_selectionManager.mouseReleased( _rowKey, MouseEvent.SHIFT_DOWN_MASK);
				}
				else {
					_selectionManager.mouseReleased( _rowKey, 0);
				}
			}
			
		};
		/*
		 * Da die beiden Label einen Tooltipp haben, muss man den MouseListener extra hinzufügen,
		 * da er sonst auf deren Flächen nicht arbeitet. Dieser Workaround funktioniert nicht für
		 * mouseEntered und mouseExited.
		 */
		rowHeaderPanel.addMouseListener( mouseListener);
		timeLabel.addMouseListener( mouseListener);
		dataKindLabel.addMouseListener( mouseListener);
		objectLabel.addMouseListener( mouseListener);

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
