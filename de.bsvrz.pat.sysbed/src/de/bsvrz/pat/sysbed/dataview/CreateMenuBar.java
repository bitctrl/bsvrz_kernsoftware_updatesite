/*
* Copyright 2010 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionManager;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.print.Book;
import java.awt.print.PrinterJob;
import java.util.List;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Klasse zum Anlegen der Menüleiste.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8369 $
 */
public class CreateMenuBar {

	private PrintFrame _printFrame = null;

	private final JCheckBoxMenuItem _highlightSelectionButton = new JCheckBoxMenuItem("Auswahl hervorgehoben drucken", true);

	private final JCheckBoxMenuItem _printPreviewInVisibleRegion = new JCheckBoxMenuItem("Druckvorschau anzeigen", false);

	private final JCheckBoxMenuItem _rowHeaderButtonForCSVExport = new JCheckBoxMenuItem("Datensatzinformationen speichern", true);

	private final JCheckBoxMenuItem _rowHeaderButtonForClipboard = new JCheckBoxMenuItem("Datensatzinformationen kopieren", true);

	private final JRadioButtonMenuItem _isoEncoding = new JRadioButtonMenuItem("ISO-8859-1");

	private final JRadioButtonMenuItem _macEncoding = new JRadioButtonMenuItem("MacRoman");

	private JFrame _frame;

	private DataViewModel _dataViewModel;

	private AttributeGroup _attributeGroup;

	private Aspect _aspect;

	private DataViewPanel _dataViewPanel;

	private ClientDavInterface _connection;

	private DataDescription _dataDescription;

	private boolean _archive;

	public CreateMenuBar(
			JFrame frame,
			DataViewModel dataViewModel,
			AttributeGroup attributeGroup,
			Aspect aspect,
			DataViewPanel dataViewPanel,
			ClientDavInterface connection,
			DataDescription dataDescription,
			boolean archive) {
		_frame = frame;
		_dataViewModel = dataViewModel;
		_attributeGroup = attributeGroup;
		_aspect = aspect;
		_dataViewPanel = dataViewPanel;
		_connection = connection;
		_dataDescription = dataDescription;
		_archive = archive;
	}

	public void createMenuBar(final SelectionManager selectionManager) {

		final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		final int menuShortcutKeyWithAltMask = menuShortcutKeyMask + InputEvent.ALT_DOWN_MASK;

		JMenuBar menuBar = new JMenuBar();
		// Datei-Menue
		final JMenu fileMenu = new JMenu("Datei");
		fileMenu.setMnemonic(KeyEvent.VK_D);
		fileMenu.getAccessibleContext().setAccessibleDescription("Das Datei-Menue");

		final JMenuItem saveAllAsCSVItem = new JMenuItem("In CSV-Datei speichern ...");
		saveAllAsCSVItem.setToolTipText("Speichert alle Daten in einer CSV-Datei");
		saveAllAsCSVItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyMask));
		saveAllAsCSVItem.getAccessibleContext().setAccessibleDescription("Speichert den gesamten Inhalt in einer CSV-Datei.");
		fileMenu.add(saveAllAsCSVItem);
		saveAllAsCSVItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveEverythingAsCSV();
					}
				}
		);

		final JMenuItem saveSelectionAsCSVItem = new JMenuItem("Auswahl in CSV-Datei speichern ...");
		saveSelectionAsCSVItem.setToolTipText("Speichert nur die ausgewählten Inhalte");

		saveSelectionAsCSVItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyWithAltMask));
		saveSelectionAsCSVItem.getAccessibleContext().setAccessibleDescription("Speichert die Auswahl in einer CSV-Datei.");
		fileMenu.add(saveSelectionAsCSVItem);
		saveSelectionAsCSVItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveSelectionAsCSV(selectionManager);
					}
				}
		);

		_rowHeaderButtonForCSVExport.setToolTipText("Art, Zeit und Objekt werden gespeichert");
		_rowHeaderButtonForCSVExport.getAccessibleContext().setAccessibleDescription("Art, Zeit und Objekt werden gespeichert.");
		fileMenu.add(_rowHeaderButtonForCSVExport);

		final JMenu encodingMenu = new JMenu("Zeichenkodierung");

		ButtonGroup encodingGroup = new ButtonGroup();
		_isoEncoding.setToolTipText("Zur Benutzung in Windows-Excel");
		_macEncoding.setToolTipText("Zur Benutzung in Mac-Excel");
		if(System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			_macEncoding.setSelected(true);
		}
		else {
			_isoEncoding.setSelected(true);
		}
		encodingGroup.add(_isoEncoding);
		encodingGroup.add(_macEncoding);
		encodingMenu.add(_isoEncoding);
		encodingMenu.add(_macEncoding);
		fileMenu.add(encodingMenu);

		fileMenu.addSeparator();

		final JMenuItem printEverythingItem = new JMenuItem("Drucken ...");
		printEverythingItem.setToolTipText("Druckt alle Zeilen der Tabelle");
		printEverythingItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuShortcutKeyMask));
		if(_archive) {
			printEverythingItem.getAccessibleContext().setAccessibleDescription("Druckt die streambasierte Archivanfrage.");
		}
		else {
			printEverythingItem.getAccessibleContext().setAccessibleDescription("Druckt die Onlinetabelle.");
		}
		fileMenu.add(printEverythingItem);
		printEverythingItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						printEverything(false);
					}
				}
		);

		final JMenuItem printSelectionItem = new JMenuItem("Ausgewählte Zeilen drucken ...");
		printSelectionItem.setToolTipText("Druckt alle Zeilen, in denen mindestens ein Feld ausgewählt ist");
		printSelectionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuShortcutKeyWithAltMask));
		printSelectionItem.getAccessibleContext().setAccessibleDescription("Druckt die ausgewählten Zeilen.");
		fileMenu.add(printSelectionItem);
		printSelectionItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						printEverything(true);
					}
				}
		);

		_highlightSelectionButton.setToolTipText("Ausgewählte Zellen werden in den Auswahlfarben gedruckt");
		_highlightSelectionButton.getAccessibleContext().setAccessibleDescription("Ausgewählte Zellen werden in den Auswahlfarben gedruckt.");
		fileMenu.add(_highlightSelectionButton);

		if(System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			_printPreviewInVisibleRegion.setState(true);
			_printPreviewInVisibleRegion.setEnabled(false);
		}
		_printPreviewInVisibleRegion.setToolTipText("Während der Erstellung der Druckvorlage wird die Vorschau gezeigt");
		_printPreviewInVisibleRegion.getAccessibleContext().setAccessibleDescription("Während der Erstellung der Druckvorlage wird die Vorschau gezeigt.");
		fileMenu.add(_printPreviewInVisibleRegion);

		fileMenu.addSeparator();

		final JMenuItem closeItem = new JMenuItem("Schließen");
		if(_archive) {
			closeItem.setToolTipText("Schließt die streambasierte Archivanfrage");
		}
		else {
			closeItem.setToolTipText("Schließt die Onlinetabelle");
		}
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, menuShortcutKeyMask));

		if(_archive) {
			closeItem.getAccessibleContext().setAccessibleDescription("Schließt die streambasierte Archivanfrage.");
		}
		else {
			closeItem.getAccessibleContext().setAccessibleDescription("Schließt die Onlinetabelle.");
		}
		fileMenu.add(closeItem);
		closeItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_frame.dispose();
					}
				}
		);

		JMenu editMenu = new JMenu("Bearbeiten");
		fileMenu.setMnemonic(KeyEvent.VK_B);
		fileMenu.getAccessibleContext().setAccessibleDescription("Das Bearbeiten-Menue");

		final JMenuItem deleteAllItem = new JMenuItem("Alles löschen");
		deleteAllItem.setToolTipText("Löscht alle Inhalte der Tabelle");
		deleteAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, menuShortcutKeyMask));
		deleteAllItem.getAccessibleContext().setAccessibleDescription("Löscht alle Inhalte der Tabelle.");
		editMenu.add(deleteAllItem);
		deleteAllItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_dataViewModel.removeDataSets();
					}
				}
		);

		editMenu.addSeparator();

		final JMenuItem selectAllItem = new JMenuItem("Alles auswählen");
		selectAllItem.setToolTipText("Wählt alle Inhalte der Tabelle aus");
		selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcutKeyMask));
		selectAllItem.getAccessibleContext().setAccessibleDescription("Wählt alle Inhalte der Tabelle aus.");
		editMenu.add(selectAllItem);
		selectAllItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectionManager.selectAllCellKeys();
					}
				}
		);

		final JMenuItem deselectAllItem = new JMenuItem("Nichts auswählen");
		deselectAllItem.setToolTipText("Deselektiert alle Inhalte der Tabelle");
		deselectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcutKeyWithAltMask));
		deselectAllItem.getAccessibleContext().setAccessibleDescription("Deselektiert alle Inhalte der Tabelle.");
		editMenu.add(deselectAllItem);
		deselectAllItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectionManager.unselectAllCellKeys();
					}
				}
		);

		editMenu.addSeparator();

		final JMenuItem copyCSVToClipboardItem = new JMenuItem("Auswahl in die Zwischenablage kopieren");
		copyCSVToClipboardItem.setToolTipText("Kopiert die ausgewählten Inhalte in die Zwischenablage");
		copyCSVToClipboardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		copyCSVToClipboardItem.getAccessibleContext().setAccessibleDescription("Kopiert die Auswahl in die Zwischenablage.");
		editMenu.add(copyCSVToClipboardItem);
		copyCSVToClipboardItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						copySelectionToClipboard(selectionManager);
					}
				}
		);

		_rowHeaderButtonForClipboard.setToolTipText("Art, Zeit und Objekt werden kopiert");
		_rowHeaderButtonForClipboard.getAccessibleContext().setAccessibleDescription("Art, Zeit und Objekt werden kopiert.");
		editMenu.add(_rowHeaderButtonForClipboard);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		_frame.setJMenuBar(menuBar);
	}

	private void createDummyMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Attributgruppe: " + _attributeGroup.getNameOrPidOrId());
		JMenu editMenu = new JMenu("Aspekt: " + _aspect.getNameOrPidOrId());
		fileMenu.setEnabled(false);
		editMenu.setEnabled(false);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		_printFrame.getFrame().setJMenuBar(menuBar);
	}

	/**
	 * Eine eigene Klasse, die analog zu FileNameExtensionFilter funktioniert, welche aber erst ab Java 1.6 vorhanden ist.
	 *
	 * @author Kappich Systemberatung
	 * @version $Revision: 8369 $
	 */
	class MyFileNameExtensionFilter extends FileFilter {
		// Ähnlich zu FileNameExtensionFilter aus java.swing.JFileChooser.

		final String _description;

		final String _extension;

		/**
		 * Konstruiert ein Objekt aus einer Beschreibung des Filters und der definierenden Dateiendung.
		 *
		 * @param description die Beschreibung
		 * @param extension   die Dateiendung
		 */
		public MyFileNameExtensionFilter(final String description, final String extension) {
			_description = description;
			if(extension == null || (extension.length() == 0)) {
				throw new IllegalArgumentException(
						"The extension must be non-null and not empty"
				);
			}
			_extension = extension.toLowerCase(Locale.ENGLISH);
		}

		@Override
		public boolean accept(File f) {
			if(f != null) {
				if(f.isDirectory()) {
					return true;
				}
				String fileName = f.getName();
				int i = fileName.lastIndexOf('.');
				if(i > 0 && i < fileName.length() - 1) {
					String desiredExtension = fileName.substring(i + 1).toLowerCase(Locale.ENGLISH);
					if(desiredExtension.equals(_extension)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return _description;
		}
	}

	private File getCSVFileForExport() {
		final JFileChooser fileChooser = new JFileChooser();
		MyFileNameExtensionFilter filter = new MyFileNameExtensionFilter("CSV-Datei", "csv");
		fileChooser.setFileFilter(filter);
		if(_archive) {
			fileChooser.setDialogTitle("Streambasierte Archivanfrage: CSV-Export");
		}
		else {
			fileChooser.setDialogTitle("Onlinetabelle: CSV-Export");
		}

		fileChooser.setApproveButtonText("Exportieren");

		File csvFile;
		while(true) {
			int showSaveDialog = fileChooser.showSaveDialog(_frame);
			if(!(showSaveDialog == JFileChooser.CANCEL_OPTION)) {
				File selectedFile = fileChooser.getSelectedFile();
				String path = selectedFile.getPath();

				if(!path.toLowerCase().endsWith(".csv")) {
					path += ".csv";
				}
				csvFile = new File(path);

				if(csvFile.exists()) {
					int n = JOptionPane.showConfirmDialog(
							new JFrame(), "Die Datei '" + csvFile.getName() + "' existiert bereits.\nDatei überschreiben?", "Warning", JOptionPane.YES_NO_OPTION
					);
					if(n == JOptionPane.YES_OPTION) {
						break;
					}
				}
				else {
					break;
				}
			}
			else {
				return null;
			}
		}
		return csvFile;
	}

	private void saveAsCSV(final File csvFile, final CSVManager csvManager) {
		if(csvFile == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler in DataViewFrame.saveAsCSV: es wurde keine CSV-Datei übergeben.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		if(csvManager == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler in DataViewFrame.saveAsCSV: es wurde kein CSV-Manager übergeben.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		final OutputStreamWriter fileWriter;
		final SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
		Charset charset = null;
		if(_isoEncoding.isSelected()) {
			charset = availableCharsets.get("ISO-8859-1");
		}
		else if(_macEncoding.isSelected()) {
			charset = availableCharsets.get("MacRoman");
			if(charset == null) {
				charset = availableCharsets.get("x-MacRoman");
			}
		}
		if(charset == null) {
			charset = Charset.defaultCharset();
		}
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(csvFile), charset);
		}
		catch(IOException e) {
			System.err.println("Es wurde eine IOException beim Öffnen der Datei " + csvFile.getName() + " ausgelöst.");
			System.err.println("Möglicherweise wird die Datei von einem anderen Programm verwendet.");
			System.err.println("Die Nachricht der IOException ist: " + e.getMessage());
			JOptionPane.showMessageDialog(
					null, "Fehler beim Öffnen der Datei " + csvFile.getName(), "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}

		try {
			fileWriter.write(csvManager.getCSVHeaderLine(_rowHeaderButtonForCSVExport.isSelected()));
			fileWriter.write(csvManager.getCSVLines(_rowHeaderButtonForCSVExport.isSelected()));
		}
		catch(IOException e) {
			System.err.println("Es wurde eine IOException beim Schreiben der Datei " + csvFile.getName() + " ausgelöst.");
			System.err.println("Die Nachricht der IOException ist: " + e.getMessage());
			JOptionPane.showMessageDialog(
					null, "Fehler beim Schreiben der Datei " + csvFile.getName(), "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		try {
			fileWriter.close();
		}
		catch(IOException e) {
			System.err.println("Es wurde eine IOException beim Schließen der Datei " + csvFile.getName() + " ausgelöst.");
			System.err.println("Die Nachricht der IOException ist: " + e.getMessage());
			JOptionPane.showMessageDialog(
					null, "Fehler beim Schließen der Datei " + csvFile.getName(), "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
		}
	}

	private void saveEverythingAsCSV() {
		final File csvFile = getCSVFileForExport();
		if(csvFile == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler: es wurde keine CSV-Datei geöffnet.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}

		final CSVManager csvManager = new CSVManager(
				_attributeGroup, null, _dataViewModel.getDataTableObjects()
		);
		if(csvManager == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler: es wurde keine CSV-Manager geöffnet.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		csvManager.setDelimiter(";");
		saveAsCSV(csvFile, csvManager);
	}

	private void saveSelectionAsCSV(final SelectionManager selectionManager) {
		final File csvFile = getCSVFileForExport();
		if(csvFile == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler: es wurde keine CSV-Datei geöffnet.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}

		final CSVManager csvManager = new CSVManager(
				_attributeGroup, selectionManager.getSelectedCellKeysAsSet(), _dataViewModel.getDataTableObjects()
		);
		if(csvManager == null) {
			JOptionPane.showMessageDialog(
					null, "Fehler: es wurde keine CSV-Manager geöffnet.", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
			);
			return;
		}
		csvManager.setDelimiter(";");
		saveAsCSV(csvFile, csvManager);
	}

	private void copySelectionToClipboard(final SelectionManager selectionManager) {

		class MyTransferable implements Transferable {

			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if(flavor == DataFlavor.stringFlavor) {
					final CSVManager csvManager = new CSVManager(
							_attributeGroup, selectionManager.getSelectedCellKeysAsSet(), _dataViewModel.getDataTableObjects()
					);
					csvManager.setDelimiter("\t");
					final boolean rowHeader = _rowHeaderButtonForClipboard.isSelected();
					final String csvContent = csvManager.getCSVHeaderLine(rowHeader) + csvManager.getCSVLines(rowHeader);
					return csvContent;
				}
				return null;
			}

			public DataFlavor[] getTransferDataFlavors() {
				final DataFlavor[] flavors = new DataFlavor[1];
				flavors[0] = DataFlavor.stringFlavor;
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return (flavor == DataFlavor.stringFlavor);
			}
		}

		final MyTransferable transfer = new MyTransferable();
		final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		systemClipboard.setContents(transfer, null);
	}

	public void printEverything(final boolean selectedDataTableObjectsOnly) {

		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						if(_printFrame != null) {
							_printFrame.getFrame().dispose();
							_printFrame = null;
						}
						initPrintFrame(selectedDataTableObjectsOnly);
					}
				}
		);

		/** Diese komische Verschachtelung ist notwendig, da innerhalb von initPrintFrame()
		 * weitere invokeLater-Aufrufe stattfinden, deren Ergebnis für das unten zu konstruierende
		 * Book notwendig sind! Toll ist anders.
		 */
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {

						_printFrame.getFrame().setVisible(true);

						final PrinterJob printerJob = PrinterJob.getPrinterJob();
						final PageFormat defaultPage = printerJob.defaultPage();
						PageFormat pageFormat = printerJob.pageDialog(defaultPage);
						if(pageFormat == defaultPage) {
							if(_printFrame != null) {
								_printFrame.getFrame().dispose();
								_printFrame = null;
							}
							return;
						}


						final DataViewPanel printPanel = _printFrame.getDataViewPanel();
						final int maximumValue = printPanel.getVerticalScrollBarsMaximumValue();
						final int visibleAmount = printPanel.getVerticalScrollBarsVisibleAmount();
						int value = 0;

						final Book book = new Book();
						while(value < maximumValue - visibleAmount) {	// alle außer der letzten Seite
							book.append(new PageDescription(value), pageFormat);
							value += printPanel.getVisibleViewPortHeight();
							if(value == 0) {
								break;
							}
						}
						book.append(new PageDescription(value), pageFormat); // Darf man nicht vergessen!

						// Die nächsten drei Zeilen bedürfen einer Erklärung: die in initPrintPanel dem
						// printPanel hinzugefügten DataTableobjects werden ab dem zweiten erst durch
						// den Adjustmentlistener der vertikalen Scrollbar erstellt. Wenn alles auf eine
						// Seite passt, kommt aber kein AdjustmentChangedEvent; diesen lösen wir nun hier
						// aus, indem wir um 1 herunterscrollen (0 tut es nicht).
						if(book.getNumberOfPages() == 1) {
							printPanel.setVerticalScrollBarsMaximumValue(maximumValue + 1);
							printPanel.setVerticalScrollBarValue(1);
						}

						printerJob.setPageable(book);

						final PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
						printAttributes.add(new PrinterResolution(600, 600, PrinterResolution.DPI));
						if(printerJob.printDialog()) {
							print(printerJob, printAttributes);
						}
					}
				}
		);
	}

	private class PageDescription implements Printable {

		final int _scrollValue;

		/**
		 * Eine PageDescription wird aus dem Wert des Scrollbalkens gebildet. Dieser ist zur Beschreibung der auszudruckenden Seite ausreichend, da die
		 * Swing-Komponente anderweitig nicht geändert wird.
		 */
		public PageDescription(final int scrollValue) {
			_scrollValue = scrollValue;
		}

		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
			if(_scrollValue < 0) {
				return Printable.PAGE_EXISTS;	// macht eine leere Seite, NO_SUCH_PAGE führt dagegen zum Abbruch
			}
			if(_printFrame == null) {
				System.err.println("Fehler in PageDescription.print(): der Print-Frame wurde nicht initialisiert!");
				return 0;
			}
			final DataViewPanel printPanel = _printFrame.getDataViewPanel();
			final int maximumValue = printPanel.getVerticalScrollBarsMaximumValue();
			final int visibleAmount = printPanel.getVerticalScrollBarsVisibleAmount();
			if(_scrollValue > maximumValue - visibleAmount) { // Panel muss unten verlängert werden!
				final int missingSize = _scrollValue - (maximumValue - visibleAmount);
				printPanel.increaseLowerPanel(missingSize);
				_printFrame.getFrame().validate();	// total wichtig!
			}
			printPanel.setVerticalScrollBarValue(_scrollValue);
			_printFrame.getFrame().validate();	// hilft auch

			Graphics2D g2d = (Graphics2D)graphics;
			// Seitenformat anpassen
			final Rectangle bounds = _frame.getBounds();
			double scaleWidth = pageFormat.getImageableWidth() / bounds.width;
			double scaleHeight = pageFormat.getImageableHeight() / bounds.height;
			double scale = Math.min(scaleWidth, scaleHeight);

			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g2d.scale(scale, scale);
			UnsubscribingJFrame frame = _printFrame.getFrame();
			frame.disableDoubleBuffering();
			frame.paint(g2d);

			return PAGE_EXISTS;
		}
	}

	private void print(final PrinterJob printerJob, final PrintRequestAttributeSet printAttributes) {
		class PrintThread extends Thread {

			private final PrinterJob _printerJob;

			private final PrintRequestAttributeSet _printAttributes;

			PrintThread(
					final PrinterJob job, final PrintRequestAttributeSet attributes) {
				_printerJob = job;
				_printAttributes = attributes;
			}

			@Override
			public void run() {
				try {
					_printerJob.print(_printAttributes);
				}
				catch(Exception pe) {
					System.err.println(pe.toString());
					JOptionPane.showMessageDialog(
							null, "Fehler beim Drucken", "Fehlermeldung", JOptionPane.ERROR_MESSAGE
					);
				}
			}
		}
		PrintThread printThread = new PrintThread(printerJob, printAttributes);
		printThread.run();
		_printFrame.getFrame().dispose();
		_printFrame = null;
	}

	/**
	 * Das hier ist keine Software, auf die man stolz sein kann; dies ist schlicht ein funktionaler Anbau, damit das Drucken klappt, wobei möglichst wenig der
	 * bestehenden Software geändert werden sollte.
	 */

	private void initPrintFrame(final boolean selectedDataTableObjectsOnly) {
		// den Spezial-Konstruktor aufrufen:

		if(_archive) {
			_printFrame = new ArchiveDataTableView(_connection, _attributeGroup, _aspect, _dataDescription.getSimulationVariant());
		}
		else {
			_printFrame = new DataViewFrame(
					_connection, _attributeGroup, _aspect, _dataDescription.getSimulationVariant()
			);
		}

		// leere Menue-Leiste hinzufügen:
		createDummyMenuBar();
		// kopiere die Breiten aller Spalten und ihren offen/geschlossen-Status:
		final HeaderGrid printFrameHeaderGrid = _printFrame.getDataViewPanel().getHeaderGrid();
		final HeaderGrid originalHeaderGrid = _dataViewPanel.getHeaderGrid();
		printFrameHeaderGrid.makeSimilar(originalHeaderGrid);
		printFrameHeaderGrid.removeAllMouseListeners();
		// bestimme die relevanten DataTableObjects:
		if(selectedDataTableObjectsOnly) {
			final SelectionManager selectionManager = _dataViewPanel.getSelectionManager();
			final List<DataTableObject> thisDataTableObjects = Collections.synchronizedList(_dataViewModel.getDataTableObjects());
			synchronized(thisDataTableObjects) {
				final int size = thisDataTableObjects.size();
				for(int index = 0; index < size; index++) {
					final List<CellKey> cellKeyList = thisDataTableObjects.get(index).getAllCellKeys();
					for(CellKey cellKey : cellKeyList) {
						if(selectionManager.isCellKeySelected(cellKey)) {
							final int finalIndex = index;
							SwingUtilities.invokeLater(
									new Runnable() {
										public void run() {
											_printFrame.getDataViewPanel().addDataTableObject(finalIndex, thisDataTableObjects.get(finalIndex));
										}
									}
							);
							break;
						}
					}
				}
			}
		}
		else {
			final List<DataTableObject> thisDataTableObjects = _dataViewModel.getDataTableObjects();
			synchronized(thisDataTableObjects) {
				final int size = thisDataTableObjects.size();
				for(int index = 0; index < size; index++) {
					final int finalIndex = index;
					SwingUtilities.invokeLater(
							new Runnable() {
								public void run() {
									_printFrame.getDataViewPanel().addDataTableObject(finalIndex, thisDataTableObjects.get(finalIndex));
								}
							}
					);
				}
			}
		}
		// Präpariere den SelectionManager:
		final SelectionManager selectionManager = _printFrame.getDataViewPanel().getSelectionManager();
		if(_highlightSelectionButton.isSelected()) {
			selectionManager.setSelectedCellKeys(_dataViewPanel.getSelectionManager().getSelectedCellKeysAsSet());
			selectionManager.setSelectedRowKeys(_dataViewPanel.getSelectionManager().getSelectedRowKeysAsSet());
		}
		selectionManager.lock(true);

		_printFrame.getFrame().setTitle("Druckvorschau");
		_printFrame.getFrame().setBounds(_frame.getBounds());
		if(!_printPreviewInVisibleRegion.isSelected()) {
			_printFrame.getFrame().setLocation(10000, 10000);
		}
		// Das Layout wurde in dem privaten Konstruktor schon gesetzt, aber das Panel muss noch dazu:
		final Container pane = _printFrame.getFrame().getContentPane();
		pane.add(_printFrame.getDataViewPanel(), BorderLayout.CENTER);

		// Ein generelles JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED hätte wohl den Nachteil, dass bei
		// sich ändernden Spaltenbreiten der Zeilenköpfe, mal eine ScrollBar da sein könnte wäre und mal nicht,
		// was entweder zu verdecktem oder doppelten Darstellungen führen würde, je nachdem ob die Seiten des
		// Books mit oder ohne Scrollbar berechent wurden.
		if(_dataViewPanel.isHorizontalScrollBarVisible()) {
			_printFrame.getDataViewPanel().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		else {
			_printFrame.getDataViewPanel().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		_printFrame.getFrame().validate();
	}
}
