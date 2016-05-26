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

package de.bsvrz.pat.sysbed.main;

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModule;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionLists;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionPanel;
import de.bsvrz.pat.sysbed.preselection.tree.PreselectionTree;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

/**
 * Organisiert die Einstellungen der {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Module}. Es werden zwei Tabellen geführt. Eine zeigt die zuletzt verwendeten Einstellungen an, die
 * andere Tabelle die gespeicherten Einstellungen. Aus beiden Tabellen können die Einstellungen gestartet, gespeichert bzw. umbenannt, geändert oder gelöscht
 * werden. Die gespeicherten Einstellungen können zudem im XML-Format exportiert und importiert werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #SettingsHandler
 * @see #saveSettings
 * @see #loadAllSettings
 * @see #getSettingsPanel
 */
public class SettingsHandler {

	/** der Debug-Logger */
	private static final Debug _debug = Debug.getLogger();

	/** stellt die gespeicherten Einstellungen dar */
	private final JTable _savedSettingsTable;

	/** stellt die zuletzt verwendeten Einstellungen dar */
	private final JTable _lastUsedSettingsTable;

	/** das TableModel der gespeicherten Einstellungen */
	private final DefaultTableModel _savedSettingsTableModel;

	/** das TableModel der zuletzt verwendeten Einstellungen */
	private final DefaultTableModel _lastUsedSettingsTableModel;

	/** der Button startet das Modul mit den ausgewählten Einstellungen */
	private final JButton _startButton = new JButton();

	/** der Button speichert die Einstellungen unter einem neuen Namen */
	private final JButton _saveAsButton = new JButton();

	/** der Button ruft den Dialog des zu den Einstellungen passenden Moduls auf */
	private final JButton _changeButton = new JButton();

	/** der Button löscht die gewählte Einstellung aus der Liste */
	private final JButton _deleteButton = new JButton();

	/** der Button exportiert die ausgewählten Einstellungen */
	private final JButton _exportButton = new JButton();

	/** der Button importiert Einstellungen aus einer XML-Datei */
	private final JButton _importButton = new JButton();

	/** speichert das Datenmodell des Datenverteilers */
	private final DataModel _dataModel;

	/** speichert den Hauptknoten der Einstellungen */
	private final Preferences _preferences;
	/** speichert den Knoten mit den zuletzt verwendeten Einstellungen */
	private final Preferences _lastUsedPreferences;

	/** speichert den Knoten mit den gespeicherten Einstellungen */
	private final Preferences _savedPreferences;

	/** speichert das Panel mit den Tabellen für die Einstellungen */
	private JPanel _settingsPanel;

	/** speichert die PreselectionLists, damit Objekte anhand der Einstellung ausgewählt werden können */
	private final PreselectionLists _preselectionLists;

	/** speichert den PreselectionTree, damit ein Pfad vorausgewählt werden kann */
	private final PreselectionTree _preselectionTree;

	/** speichert ein Objekt der Applikation */
	private final GenericTestMonitorApplication _application;

	/** speichert alle gespeicherten Einstellungen als SettingsData-Objekte */
	private final List<SettingsData> _savedSettingsList = new ArrayList<SettingsData>();

	/** speichert alle zuletzt verwendeten Einstellungen als SettingsData-Objekte */
	private final List<SettingsData> _lastUsedSettingsList = new ArrayList<SettingsData>();

	private boolean _ignoreListChangeSelectionEvent;


	/* #################### Methoden ################### */
	/**
	 * Der Konstruktor erstellt ein SettingsHandler-Objekt. Es wird ein Objekt der Applikation übergeben, damit darüber auf die Module und die Verbindung zum
	 * Datenverteiler zugegriffen werden kann. Das {@link PreselectionPanel} wird übergeben, da bei Anwahl einer Einstellung in den Tabellen, die
	 * Datenidentifikation, die Simulationsvariante und der Pfad im {@link PreselectionTree Baum} vorausgewählt werden.
	 * <p>
	 * Zusätzlich wir das Panel erzeugt, welches die beiden Tabellen mit den gespeicherten und zuletzt verwendeten Einstellungen darstellt. Wird eine Einstellung
	 * in den Tabellen ausgewählt, dann kann sie direkt gestartet, umbenannt bzw. gespeichert, geändert und gelöscht werden. Dieses Panel kann mittels der Methode
	 * {@link #getSettingsPanel} abgefragt werden.
	 *
	 * @param application       die Applikation
	 * @param preselectionPanel das Panel mit der vollständigen Datenidentifikation
	 *
	 * @see #getSettingsPanel
	 */
	public SettingsHandler(final GenericTestMonitorApplication application, final PreselectionPanel preselectionPanel) {
		_application = application;

		final String kvPid = _application.getConnection().getLocalConfigurationAuthority().getPid();
		_preferences = Preferences.userRoot().node("/gtm").node(kvPid); // durch Angabe des "/" wird vom Wurzelverzeichnis ausgegangen
		_lastUsedPreferences = _preferences.node("lastusedsettings");
		_savedPreferences = _preferences.node("savedsettings");

		_preselectionLists = preselectionPanel.getPreselectionLists();
		_preselectionTree = preselectionPanel.getPreselectionTree();
		_dataModel = _application.getConnection().getDataModel();

		_savedSettingsTable = new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				SettingsData settingsData = (SettingsData)_savedSettingsList.get(row);
				if(!settingsData.isValid() && !isCellSelected(row, col)) {
					comp.setForeground(Color.LIGHT_GRAY);
				}
				else if(settingsData.isValid() && !isCellSelected(row, col)) {
					comp.setForeground(getForeground());
				}
				return comp;
			}
		};
		_lastUsedSettingsTable = new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				SettingsData settingsData = (SettingsData)_lastUsedSettingsList.get(row);
				if(!settingsData.isValid() && !isCellSelected(row, col)) {
					comp.setForeground(Color.LIGHT_GRAY);
				}
				else if(settingsData.isValid() && !isCellSelected(row, col)) {
					comp.setForeground(getForeground());
				}
				return comp;
			}
		};

		// die Tabellen sollen nicht editierbar sein
		_savedSettingsTableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		_lastUsedSettingsTableModel = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		// anordnen der Panel
		_settingsPanel = new JPanel(new BorderLayout());
		JPanel tablePanel = new JPanel(new GridLayout(2, 1));
		tablePanel.add(createSavedSettingsPanel());
		tablePanel.add(createLastUsedSettingsPanel());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(createModuleButtonPanel());
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(createExportImportPanel());

		setButtonWidth();

		_settingsPanel.add(tablePanel, BorderLayout.CENTER);
		_settingsPanel.add(buttonPanel, BorderLayout.EAST);

		actualizeTable();

//		Dimension dim = new Dimension(100, 205);
		Dimension dim = _settingsPanel.getPreferredSize();
		dim.setSize(dim.getWidth(), buttonPanel.getPreferredSize().getHeight());
		_settingsPanel.setPreferredSize(dim);
		_settingsPanel.setMinimumSize(dim);
	}

	/**
	 * Das durch den Konstruktor erzeugte Panel kann hier geholt werden. Es stellt Tabellen zur Verfügung, die gespeicherte und zuletzt verwendete Einstellungen
	 * anzeigt. Diese können gestartet, gelöscht, geändert, gespeichert, exportiert und importiert werden.
	 *
	 * @return ein Panel für die Einstellungen der {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Module}
	 */
	public JPanel getSettingsPanel() {
		return _settingsPanel;
	}

	/** Setzt die maximale Breite der benutzten Buttons. */
	private void setButtonWidth() {
		// größte Breite ermitteln und anderen Buttons zuweisen
		// Import-Button hat die breiteste Breite
		Dimension dim = _importButton.getPreferredSize();
		_startButton.setMaximumSize(dim);
		_saveAsButton.setMaximumSize(dim);
		_changeButton.setMaximumSize(dim);
		_deleteButton.setMaximumSize(dim);
		_exportButton.setMaximumSize(dim);
	}

	/** Falls die Selektion in beiden Tabellen aufgehoben werden soll, kann diese Methode aufgerufen werden. */
	public void clearSelection() {
		_savedSettingsTable.clearSelection();
		_lastUsedSettingsTable.clearSelection();
	}

	/** Werden Einträge in den Preferences hinzugefügt, dann werden die Tabellen aktualisiert. */
	private void actualizeTable() {
		_lastUsedPreferences.addPreferenceChangeListener(
				new PreferenceChangeListener() {
					public void preferenceChange(PreferenceChangeEvent evt) {
						// Knoten hinzugefügt -> Zeile in der Tabelle hinzufügen!
						if(evt.getKey().equals("node") && !evt.getNewValue().equals("")) {
							Preferences prefs = evt.getNode().node(evt.getNewValue());
							try {
								SettingsData settingsData = createSettingsData(prefs);
								Vector v = loadLastUsedSettingsTableEntry(settingsData);
								if(v != null) {
									_ignoreListChangeSelectionEvent = true;
									_lastUsedSettingsTableModel.addRow(v);
									_lastUsedSettingsList.add(settingsData);
									//Letzten Eintrag selektieren
									int row = _lastUsedSettingsTable.getRowCount()-1;
									_lastUsedSettingsTable.setRowSelectionInterval(row, row);
									_lastUsedSettingsTable.scrollRectToVisible(_lastUsedSettingsTable.getCellRect(row,0,true));
									_ignoreListChangeSelectionEvent = false;
								}
							}
							catch(BackingStoreException ex) {
								_debug.error("Fehler beim Zugriff auf lokale Einstellungen", ex);
								JOptionPane.showMessageDialog(
										_settingsPanel,
										"Fehler beim Zugriff auf lokale Einstellungen " + ex,
										"Lokale Einstellungen",
										JOptionPane.ERROR_MESSAGE
								);
							}
							_lastUsedPreferences.put("node", "");
						}
					}
				}
		);
		_savedPreferences.addPreferenceChangeListener(
				new PreferenceChangeListener() {
					public void preferenceChange(PreferenceChangeEvent evt) {
						if(evt.getKey().equals("node") && !evt.getNewValue().equals("")) {
							Preferences prefs = evt.getNode().node(evt.getNewValue());
							try {
								// neue Zeile ermitteln
								int row = getRowNumber(_savedPreferences, evt.getNewValue());
								SettingsData settingsData = createSettingsData(prefs);
								Vector v = loadSavedSettingsTableEntry(settingsData);
								if(v != null) {
									_savedSettingsTable.clearSelection();
									_savedSettingsTableModel.insertRow(row, v);
									_savedSettingsList.add(row, settingsData);
								}
							}
							catch(BackingStoreException ex) {
								_debug.error("Fehler beim Zugriff auf lokale Einstellungen", ex);
								JOptionPane.showMessageDialog(
										_settingsPanel,
										"Fehler beim Zugriff auf lokale Einstellungen " + ex,
										"Lokale Einstellungen",
										JOptionPane.ERROR_MESSAGE
								);
							}
							_savedPreferences.put("node", "");
						}
					}
				}
		);
	}

	/**
	 * Ermittelt in den übergebenen Preferences die Position des Knotens node.
	 *
	 * @param prefs wo gesucht werden soll
	 * @param node  wonach gesucht werden soll
	 *
	 * @return die Zeilennummer des Knotens, -1 falls Knoten nicht vorhanden
	 *
	 * @throws BackingStoreException falls beim Zugriff auf das Speicherungssystem ein Fehler aufgetreten ist
	 */
	private int getRowNumber(Preferences prefs, String node) throws BackingStoreException {
		String[] children = prefs.childrenNames();
		int row = -1;
		for(int i = 0; i < children.length; i++) {
			String child = children[i];
			if(child.equals(node)) {
				row = i;
				break;
			}
		}
		return row;
	}

	/**
	 * Erstellt ein Panel mit einer Tabelle, die alle gespeicherten Einstellungen anzeigt.
	 *
	 * @return ein Panel mit Tabelle für die gespeicherten Einstellungen
	 */
	private JPanel createSavedSettingsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Gespeicherte Einstellungen"));
		panel.setLayout(new BorderLayout());

		_savedSettingsTable.setRowSelectionAllowed(true);
		_savedSettingsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_savedSettingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		_savedSettingsTable.addFocusListener(
				new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						if(_lastUsedSettingsTable.getSelectedRowCount() > 0) {
							_lastUsedSettingsTable.clearSelection();
						}
					}
				}
		);
		_savedSettingsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					// zur Ermittlung, ob eine neue Zeile ausgewählt wurde -> Abfrage spart doppelten Ausführung
					int row = -1;

					public void valueChanged(ListSelectionEvent e) {
						if(_savedSettingsTable.getSelectedRowCount() == 1 && _savedSettingsTable.getSelectedRow() != row) {
							row = _savedSettingsTable.getSelectedRow();
							final SettingsData settingsData = (SettingsData)_savedSettingsList.get(row);
							if(settingsData.isValid()) {
								_savedSettingsTable.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								preselectListsBySettings(settingsData);
								_savedSettingsTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							}
						}
						else if(_savedSettingsTable.getSelectedRowCount() == 0) {
							row = -1;
						}
						checkButtonStatus();
					}
				}
		);
		_savedSettingsTableModel.addColumn("Titel");
		_savedSettingsTableModel.addColumn("Modulname");
		_savedSettingsTableModel.addColumn("Attributgruppe");
		_savedSettingsTableModel.addColumn("Aspekt");
		_savedSettingsTableModel.addColumn("SV");
		_savedSettingsTableModel.addColumn("Objekte");
		_savedSettingsTable.setModel(_savedSettingsTableModel);

		ColumnHeaderToolTips headerToolTips = new ColumnHeaderToolTips();
		// Tooltip für die Spalte SV zuweisen und die Breite der Spalten initialisieren
		for(int i = 0, n = _savedSettingsTable.getColumnCount(); i < n; i++) {
			TableColumn column = _savedSettingsTable.getColumnModel().getColumn(i);
			String headerValue = (String)column.getHeaderValue();
			if(headerValue.equals("SV")) {
				headerToolTips.setToolTip(column, "Simulationsvariante");
				column.setPreferredWidth(30);
			}
			else {
				column.setPreferredWidth(200);
			}
		}
		_savedSettingsTable.getTableHeader().addMouseMotionListener(headerToolTips);

		JScrollPane scrollPane = new JScrollPane(_savedSettingsTable);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Erstellt ein Panel mit einer Tabelle, die alle zuletzt verwendeten Einstellungen anzeigt.
	 *
	 * @return ein Panel mit Tabelle für die zuletzt verwendeten Einstellungen
	 */
	private JPanel createLastUsedSettingsPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Zuletzt verwendete Einstellungen"));
		panel.setLayout(new BorderLayout());

		_lastUsedSettingsTable.setRowSelectionAllowed(true);
		_lastUsedSettingsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_lastUsedSettingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		_lastUsedSettingsTable.addFocusListener(
				new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						if(_savedSettingsTable.getSelectedRowCount() > 0) {
							_savedSettingsTable.clearSelection();
						}
					}
				}
		);
		_lastUsedSettingsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					int row = -1;

					public void valueChanged(ListSelectionEvent e) {
						if(_ignoreListChangeSelectionEvent) return;
						if(_lastUsedSettingsTable.getSelectedRowCount() == 1 && _lastUsedSettingsTable.getSelectedRow() != row) {
							row = _lastUsedSettingsTable.getSelectedRow();
							SettingsData settingsData = _lastUsedSettingsList.get(row);
							if(settingsData.isValid()) {
								preselectListsBySettings(settingsData);
							}
						}
						else if(_savedSettingsTable.getSelectedRowCount() == 0 && _lastUsedSettingsTable.getSelectedRowCount() == 0) {
//					_preselectionLists.setPreselectedAttributeGroups(new LinkedList());
//					_preselectionLists.setPreselectedAspects(new LinkedList());
//					_preselectionLists.setSimulationVariant(0);
//					_preselectionLists.setPreselectedObjects(new LinkedList());
//					_preselectionTree.setSelectedTreePath(null);
							row = -1;
						}
						checkButtonStatus();
					}
				}
		);
		_lastUsedSettingsTableModel.addColumn("Modulname");
		_lastUsedSettingsTableModel.addColumn("Attributgruppe");
		_lastUsedSettingsTableModel.addColumn("Aspekt");
		_lastUsedSettingsTableModel.addColumn("SV");
		_lastUsedSettingsTableModel.addColumn("Objekte");
		_lastUsedSettingsTable.setModel(_lastUsedSettingsTableModel);

		ColumnHeaderToolTips headerToolTips = new ColumnHeaderToolTips();
		// Tooltip für die Spalte SV zuweisen und die Breite der Spalten initialisieren
		for(int i = 0, n = _lastUsedSettingsTable.getColumnCount(); i < n; i++) {
			TableColumn column = _lastUsedSettingsTable.getColumnModel().getColumn(i);
			String headerValue = (String)column.getHeaderValue();
			if(headerValue.equals("SV")) {
				headerToolTips.setToolTip(column, "Simulationsvariante");
				column.setPreferredWidth(30);
			}
			else {
				column.setPreferredWidth(200);
			}
		}
		_lastUsedSettingsTable.getTableHeader().addMouseMotionListener(headerToolTips);

		JScrollPane scrollPane = new JScrollPane(_lastUsedSettingsTable);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Selektiert die Elemente in den Listen der <code>PreselectionLists</code> anhand der ausgewählten Einstellung. Die erste markierte Einstellung wird
	 * berücksichtigt.
	 *
	 * @param settingsData die Einstellung, die in den Listen vorausgewählt werden soll
	 */
	private void preselectListsBySettings(final SettingsData settingsData) {
		try {
			List<AttributeGroup> atgList = new LinkedList<AttributeGroup>();
			atgList.add(settingsData.getAttributeGroup());
			List<Aspect> aspList = new LinkedList<Aspect>();
			aspList.add(settingsData.getAspect());
			List<SystemObject> objects = settingsData.getObjects();

			int simulationVariant = settingsData.getSimulationVariant();
			if(simulationVariant == -1) {
				simulationVariant = 0;
			}
			String treePath = settingsData.getTreePath();
			// Frühere GTM-Versionen speicherten den Treepath nicht in jedem Fall.
			// Falls er nicht enthalten ist, die Selektion im Tree nicht verändern.
			if(!treePath.equals("")) _preselectionTree.setSelectedTreePath(treePath);
			_preselectionLists.setPreselectedAttributeGroups(atgList);
			_preselectionLists.setPreselectedAspects(aspList);
			_preselectionLists.setSimulationVariant(simulationVariant);
			_preselectionLists.setPreselectedObjects(objects);
		}
		catch(Exception ex) {
			_debug.warning("Einstellung kann nicht in den Listen vorausgewählt werden: " + ex.getMessage() + " " + ex.toString());
		}
	}

	/** Überprüft, ob die Buttons in den Einstellungen anwählbar oder nicht anwählbar sein sollen. */
	private void checkButtonStatus() {
		// Selektionen können nur in einer von beiden Tabellen vorgenommen werden.
		int selectedSavedSettingsRows = _savedSettingsTable.getSelectedRowCount();
		int selectedLastUsedSettingsRows = _lastUsedSettingsTable.getSelectedRowCount();

		if(selectedSavedSettingsRows > 0 || selectedLastUsedSettingsRows > 0) {
			if(selectedSavedSettingsRows > 1 || selectedLastUsedSettingsRows > 1) {
				_startButton.setEnabled(false);
				_saveAsButton.setEnabled(false);
				_changeButton.setEnabled(false);
				_deleteButton.setEnabled(true);
				_exportButton.setEnabled(selectedSavedSettingsRows > 1);
			}
			else if(selectedSavedSettingsRows == 1) {
				SettingsData settingsData = (SettingsData)_savedSettingsList.get(_savedSettingsTable.getSelectedRow());
				boolean valid = settingsData.isValid();
				_startButton.setEnabled(valid);
				_saveAsButton.setEnabled(valid);
				_changeButton.setEnabled(valid);
				_deleteButton.setEnabled(true);
				_exportButton.setEnabled(true);
			}
			else if(selectedLastUsedSettingsRows == 1) {
				SettingsData settingsData = (SettingsData)_lastUsedSettingsList.get(_lastUsedSettingsTable.getSelectedRow());
				boolean valid = settingsData.isValid();
				_startButton.setEnabled(valid);
				_saveAsButton.setEnabled(valid);
				_changeButton.setEnabled(valid);
				_deleteButton.setEnabled(true);
				_exportButton.setEnabled(false);
			}
		}
		else {	// keine Einstellung wurde selektiert
			_startButton.setEnabled(false);
			_saveAsButton.setEnabled(false);
			_changeButton.setEnabled(false);
			_deleteButton.setEnabled(false);
			_exportButton.setEnabled(false);
		}
	}

	/**
	 * Erstellt das Panel mit dem Button "Selektion exportieren", damit die ausgewählten Einstellungen im XML-Format exportiert werden können und dem Button
	 * "Einstellungen importieren", damit Einstellungen im XML-Format übernommen werden können.
	 *
	 * @return das Panel mit den Export- / Import- Buttons
	 */
	private JPanel createExportImportPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		_importButton.setText("Einstellungen importieren");
		_importButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fileChooser = new JFileChooser();
						String path = _preferences.get("importexportdirectory", "");
						if(!path.equals("")) {
							fileChooser.setCurrentDirectory(new File(path));
						}
						fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						fileChooser.setMultiSelectionEnabled(true);
						fileChooser.setFileFilter(new XMLFilter());
						fileChooser.setApproveButtonText("Importieren");
						if(fileChooser.showOpenDialog(_importButton) == JFileChooser.APPROVE_OPTION) {
							File[] files = fileChooser.getSelectedFiles();
							if(files.length > 0) {
								File file = files[0];
								String newPath = "";
								if(file.isFile()) {
									newPath = file.getParent();
								}
								else if(file.isDirectory()) {
									newPath = file.getPath();
								}
								if(!path.equals(newPath)) {
									_preferences.put("importexportdirectory", newPath);
								}
							}
							for(int i = 0; i < files.length; i++) {
								File file = files[i];
								if(file.isFile()) {
									String name = file.getName();
									name = name.substring(0, name.lastIndexOf(".xml"));
									try {
										if(_savedPreferences.nodeExists(name)) {
											String message = "Bestehende Einstellung \'" + name + "\' überschreiben?";
											String headline = "Einstellung existiert bereits.";
											if(JOptionPane.showConfirmDialog(
													_settingsPanel, message, headline, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
											) == JOptionPane.YES_OPTION) {
												// bestehende Einstellung überschreiben
												int row = getRowNumber(_savedPreferences, name);
												_savedSettingsTableModel.removeRow(row);
												_savedPreferences.node(name).removeNode();

												BufferedReader reader = new BufferedReader(new FileReader(file));
												readPreferences(reader);
												reader.close();
												_savedPreferences.put("node", name);
											}
										}
										else {
											BufferedReader reader = new BufferedReader(new FileReader(file));
											readPreferences(reader);
											reader.close();
											_savedPreferences.put("node", name);
										}
									}
									catch(IOException ex) {
										ex.printStackTrace();
									}
									catch(InvalidPreferencesFormatException ex) {
										String headline = "Einstellung wurde nicht übernommen.";
										String message = "Einstellungsdatei fehlerhaft.";
										JOptionPane.showMessageDialog(_settingsPanel, message, headline, JOptionPane.INFORMATION_MESSAGE);
										_debug.finest(ex.toString());
									}
									catch(BackingStoreException ex) {
										ex.printStackTrace();
									}
								}
							}
						}
					}
				}
		);
		_exportButton.setText("Selektion exportieren");
		_exportButton.setEnabled(false);
		_exportButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// FileDialog öffnen
						JFileChooser fileChooser = new JFileChooser();
						String path = _preferences.get("importexportdirectory", "");
						if(!path.equals("")) {
							fileChooser.setCurrentDirectory(new File(path));
						}
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(fileChooser.showDialog(_exportButton, "Verzeichnis auswählen") == JFileChooser.APPROVE_OPTION) {
							File directory = fileChooser.getSelectedFile();
							String newPath = directory.getPath();
							if(!path.equals(newPath)) {
								_preferences.put("importexportdirectory", newPath);
							}
							// alle ausgewählten Einstellungen mit dem entsprechenden Namen speichern.
							try {
								int[] selectedRows = _savedSettingsTable.getSelectedRows();
								String[] children = new String[0];
								children = _savedPreferences.childrenNames();
								for(int i = 0; i < selectedRows.length; i++) {
									int selectedRow = selectedRows[i];
									String child = children[selectedRow];	// Dateiname
									Preferences prefs = _savedPreferences.node(child);
									String separator = System.getProperty("file.separator");
									String fileName = directory.getPath() + separator + child + ".xml";
									File file = new File(fileName);
									FileOutputStream stream = new FileOutputStream(file);
									prefs.exportSubtree(stream);
									stream.close();
								}
							}
							catch(BackingStoreException ex) {
								ex.printStackTrace();
							}
							catch(FileNotFoundException ex) {
								ex.printStackTrace();
							}
							catch(IOException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
		);

		panel.add(_exportButton);
		panel.add(Box.createVerticalStrut(5));
		panel.add(_importButton);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Um den KV richtig zu importieren, muss die Eingabe bearbeitet werden. Hierzu wird die XML-Datei zeilenweise eingelesen, und die zweite Zeile, die mit
	 * <code>&lt;node name=</code> beginnt, ersetzt durch eine Zeile, die den korrekten KV beinhaltet. Anschließend werden die Einstellungen mit
	 * {@link Preferences#importPreferences(java.io.InputStream)} geladen.
	 * @param reader Reader für die XML-Datei
	 * @throws IOException IO-Fehler
	 * @throws InvalidPreferencesFormatException Fehler im Format der importierten Datei
	 */
	private void readPreferences(final BufferedReader reader) throws IOException, InvalidPreferencesFormatException {
		final String kvPid = _application.getConnection().getLocalConfigurationAuthority().getPid();

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));

		String line;
		boolean replaceNextLine = false;
		boolean replaced = false;
		while(true){
			line = reader.readLine();
			if (line == null)
				break;
			if(replaced){
				writer.write(line + "\n");
			}
			else if(!replaceNextLine && line.matches("\\s*<node name=\"gtm\">")) {
				replaceNextLine = true;
				writer.write(line + "\n");
			}
			else if(replaceNextLine && line.matches("\\s*<node name=\".+\">")) {
				writer.write("<node name=\"" + kvPid + "\">\n");
				replaceNextLine = false;
				replaced = true;
			}
			else{
				writer.write(line + "\n");
			}
		}

		writer.close();
		
		final byte[] buf = byteArrayOutputStream.toByteArray();

		final ByteArrayInputStream inputStream = new ByteArrayInputStream(buf);
		Preferences.importPreferences(inputStream);
	}

	/**
	 * Erstellt das Panel mit den Buttons zum Starten, Speichern, Ändern und Löschen der Einstellungen.
	 *
	 * @return das Panel mit den Buttons zum Starten, Speichern, Ändern und Löschen der Einstellungen
	 */
	private JPanel createModuleButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		_startButton.setText("Starten");
		_saveAsButton.setText("Speichern unter ...");
		_changeButton.setText("Ändern ...");
		_deleteButton.setText("Löschen");

		_startButton.setEnabled(false);
		_saveAsButton.setEnabled(false);
		_changeButton.setEnabled(false);
		_deleteButton.setEnabled(false);

		_startButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							SettingsData settingsData = null;
							if(_savedSettingsTable.getSelectedRowCount() == 1) {
								settingsData = (SettingsData)_savedSettingsList.get(_savedSettingsTable.getSelectedRow());
							}
							else if(_lastUsedSettingsTable.getSelectedRowCount() == 1) {
								settingsData = (SettingsData)_lastUsedSettingsList.get(_lastUsedSettingsTable.getSelectedRow());
							}
							if(settingsData != null) {
								Class moduleClass = settingsData.getModuleClass();
								ExternalModule externalModule = _application.getExternalModule(moduleClass.getName());
								if(externalModule == null) {
									_application.setExternalModule((ExternalModule)moduleClass.newInstance());
									externalModule = _application.getExternalModule(moduleClass.getName());
								}
								externalModule.startSettings(settingsData);
							}
						}
						catch(Exception ex) {
							String message = ex.getMessage();
							_debug.error("Beim Starten einer gespeicherten Einstellung kam es zu einem Fehler (siehe Exception)", ex);
							JOptionPane.showMessageDialog(_settingsPanel, message, "Einstellung kann nicht gestartet werden.", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
		);
		_saveAsButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							SettingsData settingsData = null;
							if(_lastUsedSettingsTable.getSelectedRowCount() == 1) {
								settingsData = (SettingsData)_lastUsedSettingsList.get(_lastUsedSettingsTable.getSelectedRow());
							}
							else {
								settingsData = (SettingsData)_savedSettingsList.get(_savedSettingsTable.getSelectedRow());
							}
							if(settingsData != null) {
								String oldTitle = settingsData.getTitle();
								String title = JOptionPane.showInputDialog("Bitte einen Namen vergeben: ", oldTitle);
								if(title != null && !title.equals("")) {	// falls doch -> passiert nichts!
									settingsData.setTitle(title);
									saveSettings(settingsData);
								}
							}
						}
						catch(Exception ex) {
							String message = ex.getMessage();
							_debug.error("Beim Speichern einer lokalen Einstellung kam es zu einem Fehler (siehe Exception)", ex);
							JOptionPane.showMessageDialog(
									_settingsPanel, message, "Einstellung kann nicht gespeichert werden.", JOptionPane.INFORMATION_MESSAGE
							);
						}
					}
				}
		);
		_changeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							SettingsData settingsData = null;
							if(_savedSettingsTable.getSelectedRowCount() == 1) {
								settingsData = (SettingsData)_savedSettingsList.get(_savedSettingsTable.getSelectedRow());
							}
							else if(_lastUsedSettingsTable.getSelectedRowCount() == 1) {
								settingsData = (SettingsData)_lastUsedSettingsList.get(_lastUsedSettingsTable.getSelectedRow());
							}
							if(settingsData != null) {
								Class moduleClass = settingsData.getModuleClass();
								ExternalModule externalModule = _application.getExternalModule(moduleClass.getName());
								if(externalModule == null) {
									_application.setExternalModule((ExternalModule)moduleClass.newInstance());
									externalModule = _application.getExternalModule(moduleClass.getName());
								}
								externalModule.change(settingsData);
							}
						}
						catch(Exception ex) {
							String message = ex.getMessage();
							_debug.error("Beim Bearbeiten einer gespeicherten Einstellung kam es zu einem Fehler (siehe Exception)", ex);
							JOptionPane.showMessageDialog(_settingsPanel, message, "Einstellung fehlerhaft.", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
		);
		_deleteButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							int[] rows = _savedSettingsTable.getSelectedRows();
							for(int i = rows.length - 1, n = 0; i >= n; i--) {
								int row = rows[i];
								_savedSettingsTableModel.removeRow(row);
								String[] children = _savedPreferences.childrenNames();
								_savedPreferences.node(children[row]).removeNode();
								_savedSettingsList.remove(row);
							}
							rows = _lastUsedSettingsTable.getSelectedRows();
							for(int i = rows.length - 1, n = 0; i >= n; i--) {
								int row = rows[i];
								_lastUsedSettingsTableModel.removeRow(row);
								String[] children = _lastUsedPreferences.childrenNames();
								_lastUsedPreferences.node(children[row]).removeNode();
								_lastUsedSettingsList.remove(row);
							}
						}
						catch(BackingStoreException ex) {
							ex.printStackTrace();
						}
					}
				}
		);

		panel.add(_startButton);
		panel.add(Box.createVerticalStrut(5));
		panel.add(_saveAsButton);
		panel.add(Box.createVerticalStrut(5));
		panel.add(_changeButton);
		panel.add(Box.createVerticalStrut(5));
		panel.add(_deleteButton);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	/**
	 * Diese Methode erstellt aus einem Knoten in den Preferences ein SettingsData-Objekt.
	 *
	 * @param preferences einen gespeicherten Eintrag der Einstellungen
	 *
	 * @return ein SettingsData-Objekt
	 *
	 * @throws BackingStoreException Falls beim Zugriff auf das Speicherungssystem Fehler aufgetreten sind.
	 */
	private SettingsData createSettingsData(Preferences preferences) throws BackingStoreException {
		SettingsData settingsData = new SettingsData();
		String moduleName = preferences.get("name", "");
		settingsData.setModuleName(moduleName);
		if(moduleName == null) settingsData.setValid(false);

		String title = preferences.get("title", "");
		settingsData.setTitle(title);

		Class moduleClass = null;
		try {
			moduleClass = Class.forName(preferences.get("class", ""));
		}
		catch(ClassNotFoundException ignore) {
		}
		settingsData.setModuleClass(moduleClass);
		if(moduleClass == null) settingsData.setValid(false);

		String atgPid = preferences.get("atg", "");
		
		AttributeGroup atg = _dataModel.getAttributeGroup(atgPid != null ? atgPid : "");
		if(atgPid.equals("") || atg != null) {
	        settingsData.setAttributeGroup(atg);
        }
		else if(atg == null) {
	        settingsData.setValid(false); // ohne Atg geht gar nichts
        }
		
		String aspPid = preferences.get("asp", "");
		Aspect asp = _dataModel.getAspect(aspPid != null ? aspPid : "");
		if(aspPid.equals("") || asp != null) {
			settingsData.setAspect(asp);
        }
		else if(asp == null) {
			settingsData.setValid(false);
        }
		
		int simulationVariant = preferences.getInt("simvariant", -1);
		settingsData.setSimulationVariant(simulationVariant);

		String treePath = preferences.get("treepath", "");
		settingsData.setTreePath(treePath != null ? treePath : "");

		Preferences objectPrefs = preferences.node("objects");
		String[] objectKeys = objectPrefs.keys();
		List<String> missingObjectList = new LinkedList<String>();
		List<SystemObject> objectList = new ArrayList<SystemObject>(objectKeys.length);

		for(int i = 0; i < objectKeys.length; i++) {
			String pidOrId = objectPrefs.get(objectKeys[i], "");
			SystemObject systemObject = null;
			try {		// prüfen ob es sich um eine id oder um eine pid handelt -> dann die entsprechenden getObject-Methoden aufrufen
				long objectId = Long.parseLong(pidOrId);
				systemObject = _dataModel.getObject(objectId);
			}
			catch(Exception ex) {
				systemObject = _dataModel.getObject(pidOrId);
			}
			if(systemObject == null) {
				settingsData.setValid(false);
				missingObjectList.add(pidOrId);
				break;
			}
			else {
				objectList.add(systemObject);
			}
		}
		settingsData.setObjects(objectList);

		Preferences objectTypePrefs = preferences.node("objecttypes");
		String[] objectTypeKeys = objectTypePrefs.keys();
		List<SystemObjectType> objectTypeList = new LinkedList<SystemObjectType>();
		for(int i = 0; i < objectTypeKeys.length; i++) {
			String objectTypePid = objectTypePrefs.get(objectTypeKeys[i], "");
			SystemObjectType objectType = _dataModel.getType(objectTypePid);
			if(objectType != null) {
				objectTypeList.add(objectType);
			}
		}
		settingsData.setObjectTypes(objectTypeList);

		Preferences keyValuePrefs = preferences.node("settings");
		String[] settingKeys = keyValuePrefs.keys();
		List<KeyValueObject> settingList = new LinkedList<KeyValueObject>();
		for(int i = 0; i < settingKeys.length; i++) {
			String settingKey = settingKeys[i];
			settingList.add(new KeyValueObject(settingKey, keyValuePrefs.get(settingKey, "")));
		}
		settingsData.setKeyValueList(settingList);
		if(!settingsData.isValid() && !missingObjectList.isEmpty()) {
			_debug.warning("Die Einstellung (" + (title.equals("") ? moduleName : title) + ") ist ungültig.");
			_debug.finer("Erstes nicht gefundene Systemobjekt: " + missingObjectList);
		}
		return settingsData;
	}

	/**
	 * Mit dieser Methode kann festgelegt werden, wieviele zuletzt verwendeten Einstellungen gespeichert werden sollen. Default-Wert ist 20.
	 *
	 * @param number Anzahl, wieviele der zuletzt verwendeten Einstellungen gespeichert werden
	 */
	public void setMaximumNumberOfLastUsedSettings(int number) {
		_lastUsedPreferences.putInt("numberOfLastUsedSettings", number);
	}

	/**
	 * Mit dieser Methode können Einstellungsdaten übergeben werden. Diese werden dann in den Preferences gespeichert und in den Tabellen angezeigt.
	 *
	 * @param settingsData Einstellungsdaten
	 */
	public void saveSettings(final SettingsData settingsData) {
		try {
			String title = settingsData.getTitle();
			String nextNode;
			Preferences modulePrefs;
			if(title == null || title.equals("")) {	// kein Title -> zuletzt verwendete Einstellungen
				String[] children = _lastUsedPreferences.childrenNames();
				// nächste Nummer ermitteln
				int nextNumber = 0;
				int numberOfSubNodes = children.length;
				if(numberOfSubNodes > 0) {
					String child = children[numberOfSubNodes - 1];
					nextNumber = Integer.parseInt(child) + 1;
					if(nextNumber > (Integer.MAX_VALUE - 5)) {
						_debug.error(
								"Der Integer Zahlenraum reicht nicht mehr aus! Bitte alle Einstellungen aus der Liste der zuletzt verwendeten Einstellungen löschen und das Programm neu starten"
						);
					}
				}
				// Falls die Anzahl der maximal darzustellenden Einstellungen größer als die obere Grenze ist, solange
				// die Einträge löschen, bis es passt.
				while(numberOfSubNodes >= _lastUsedPreferences.getInt("numberOfLastUsedSettings", 20)) {
					_ignoreListChangeSelectionEvent = true;
					_lastUsedPreferences.node(getOldestEntry(children)).removeNode();
					_lastUsedSettingsTableModel.removeRow(0);
					_lastUsedSettingsList.remove(0);
					_lastUsedSettingsTableModel.fireTableDataChanged();
					_ignoreListChangeSelectionEvent = false;
					children = _lastUsedPreferences.childrenNames();
					numberOfSubNodes = children.length;
				}
				nextNode = "0000000000";
				String number = String.valueOf(nextNumber);
				nextNode = nextNode.substring(0, nextNode.length() - number.length()) + number;
				_debug.finest("Neuer Knoten: " + nextNode);
				modulePrefs = _lastUsedPreferences.node(nextNode);
			}
			else {
				if(_savedPreferences.nodeExists(title)) {
					// Abfrage, ob bestehende Einstellung gelöscht werden soll, oder neuer Name vergeben soll
					String headline = "Einstellung " + title + " existiert bereits.";
					String message = "Bestehende Einstellung überschreiben?";

					if(JOptionPane.showConfirmDialog(_settingsPanel, message, headline, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane
							.YES_OPTION) {
						// überschreiben - Knoten auch aus der Tabelle löschen
						int row = 0;
						String[] children = _savedPreferences.childrenNames();
						for(int i = 0; i < children.length; i++) {
							String child = children[i];
							if(child.equals(title)) {
								row = i;
							}
						}
						_savedPreferences.node(title).removeNode();
						_savedSettingsTableModel.removeRow(row);
					}
					else {
						// nicht überschreiben - neuen Titel vergeben
						String newTitle = JOptionPane.showInputDialog("Bitte anderen Namen vergeben: ", title);
						if(newTitle != null && !newTitle.equals("")) {
							settingsData.setTitle(newTitle);
							saveSettings(settingsData);
							return;
						}
					}
				}
				nextNode = title;
				modulePrefs = _savedPreferences.node(nextNode);
				modulePrefs.put("title", nextNode);
			}
			modulePrefs.put("name", settingsData.getModuleName());
			if(settingsData.getAttributeGroup() != null) {
	            modulePrefs.put("atg", settingsData.getAttributeGroup().getPid());
            }
			else {
				modulePrefs.put("atg", "");
			}
			if(settingsData.getAspect() != null) {	// für den Parametereditor
				modulePrefs.put("asp", settingsData.getAspect().getPid());
			}
			else {
				modulePrefs.put("asp", "");
			}

			modulePrefs.putInt("simvariant", settingsData.getSimulationVariant());
			modulePrefs.put("class", settingsData.getModuleClass().getName());
			String treePath = settingsData.getTreePath();
			if(treePath == null) {
				treePath = "";
			}
			modulePrefs.put("treepath", treePath);
			Preferences objectPrefs = modulePrefs.node("objects");
			List objects = settingsData.getObjects();
			int i = 0;
			for(Iterator iterator = objects.iterator(); iterator.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator.next();
				String key = String.valueOf(i++);
				String value = systemObject.getPid();
				value = (value.equals("") ? String.valueOf(systemObject.getId()) : value);
				objectPrefs.put(key, value);
			}
			Preferences objectTypePrefs = modulePrefs.node("objecttypes");
			List objectTypes = settingsData.getObjectTypes();
			int j = 0;
			for(Iterator iterator = objectTypes.iterator(); iterator.hasNext();) {
				SystemObjectType systemObjectType = (SystemObjectType)iterator.next();
				String key = String.valueOf(j++);
				String value = String.valueOf(systemObjectType.getPid());
				objectTypePrefs.put(key, value);
			}
			Preferences keyValuePrefs = modulePrefs.node("settings");
			List keyValueList = settingsData.getKeyValueList();
			if(keyValueList != null) {
				for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
					KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
					keyValuePrefs.put(keyValueObject.getKey(), keyValueObject.getValue());
				}
			}

			// Bescheid geben, welcher Knoten hinzugekommen ist
			modulePrefs.parent().put("node", nextNode);
		}
		catch(BackingStoreException ex) {
			String message = "Daten konnten nicht gespeichert werden!";
			JOptionPane.showMessageDialog(_settingsPanel, message, "Fehler bei der Speicherung.", JOptionPane.ERROR_MESSAGE);
			_debug.warning(ex.toString());
		}
	}

	/**
	 * Gibt den ältesten Eintrag aus einem children-Array zurück. Hilfsfunktion von saveSettings.
	 *
	 * @param children Ein Array der Form <code>{"0000000000","0000000001","0000000002"}</code>
	 *
	 * @return den niedrigsten Wert im Array. Zum Beispiel <code>"0000000000"</code>
	 */
	private String getOldestEntry(final String[] children) {
		String newestChild = children[0];
		for(int i = 1; i < children.length; i++) {
			final String child = children[i];
			if(newestChild.compareTo(child) > 0) newestChild = child;
		}
		return newestChild;
	}

	/**
	 * Diese Methode lädt alle Einstellungen aus den Preferences und stellt sie in den Tabellen des SettingsHandlers dar. Diese Methode kann aufgerufen werden,
	 * nachdem das Panel dargestellt wurde. Damit hat der Anwender eine schnellere Rückmeldung der Anwendung.
	 * <p>
	 * Ist die Einstellung fehlerhaft, dann wird sie aus den Preferences gelöscht und in der Tabelle nicht angezeigt.
	 *
	 * @throws BackingStoreException falls beim Zugriff auf das Speicherungssystem ein Fehler aufgetreten ist
	 */
	public void loadAllSettings() throws BackingStoreException {
		String[] savedChildren = _savedPreferences.childrenNames();
		for(int i = 0; i < savedChildren.length; i++) {
			Preferences preferences = _savedPreferences.node(savedChildren[i]);
			SettingsData settingsData = createSettingsData(preferences);
			Vector v = loadSavedSettingsTableEntry(settingsData);
			if(v != null) {
				_savedSettingsTableModel.addRow(v);
				_savedSettingsList.add(settingsData);
			}
		}
		String[] lastUsedChildren = _lastUsedPreferences.childrenNames();
		for(int i = 0; i < lastUsedChildren.length; i++) {
			Preferences preferences = _lastUsedPreferences.node(lastUsedChildren[i]);
			// in Tabelle eintragen
			SettingsData settingsData = createSettingsData(preferences);	// hier können die Exceptions geworfen werden
			Vector v = loadLastUsedSettingsTableEntry(settingsData);
			if(v != null) {
				_lastUsedSettingsTableModel.addRow(v);
				_lastUsedSettingsList.add(settingsData);
			}
		}
	}

	/**
	 * Aus einem Einstellungsobjekt (SettingsData) wird ein Tabelleneintrag für die zuletzt verwendeten Einstellungen erstellt.
	 *
	 * @param settingsData die anzuzeigenden Einstellungen
	 *
	 * @return einen Tabelleneintrag
	 */
	private Vector loadLastUsedSettingsTableEntry(final SettingsData settingsData) {
		return loadTableEntry(settingsData);
	}

	/**
	 * Aus einem Einstellungsobjekt (SettingsData) wird ein Tabelleneintrag für die gespeicherten Einstellungen erstellt.
	 *
	 * @param settingsData die anzuzeigenden Einstellungen
	 *
	 * @return einen Tabelleneintrag
	 */
	private Vector loadSavedSettingsTableEntry(final SettingsData settingsData) {
		Vector<String> v = loadTableEntry(settingsData);
		v.add(0, settingsData.getTitle());
		return v;
	}

	/**
	 * Lädt die übergebene Einstellung und erzeugt einen Eintrag für eine der beiden Tabellen ("Gespeicherte Einstellungen" oder "Zuletzt verwendete
	 * Einstellungen").
	 *
	 * @param settingsData die anzuzeigende Einstellung
	 *
	 * @return einen Eintrag für eine Tabelle
	 */
	private Vector<String> loadTableEntry(final SettingsData settingsData) {
		Vector<String> v = new Vector<String>();
		v.add(settingsData.getModuleName());
		AttributeGroup atg = settingsData.getAttributeGroup();
		if(atg != null) {
			v.add(atg.getNameOrPidOrId());
		}
		else {
			v.add("");
		}
		Aspect asp = settingsData.getAspect();
		if(asp != null) {
			v.add(asp.getNameOrPidOrId());
		}
		else {
			v.add("");
		}
		int simulationVariant = settingsData.getSimulationVariant();
		if(simulationVariant == -1) {
			v.add("");
		}
		else {
			v.add(String.valueOf(simulationVariant));
		}
		List objects = settingsData.getObjects();
		StringBuilder builder = new StringBuilder(objects.size() * 10);
		for(int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
			final Object object = objects.get(i);
			SystemObject systemObject = (SystemObject)object;
			builder.append(systemObject.getNameOrPidOrId());
			builder.append("; ");

			if(builder.length() > 50 && objectsSize - i > 1) {
				builder.append("... (insgesamt ");
				builder.append(objectsSize);
				builder.append(" Objekte); ");
				break; //Der String wird nur zur Darstellung benutzt, es macht keinen Sinn, hier zigtausende Objekte einzufügen
			}
		}
		String result = "";
		if(builder.length() > 1) {
			result = builder.substring(0, builder.length() - 2);
		}
		v.add(result);
		return v;
	}


	/**
	 * Diese Klasse dient dem FileDialog zur Filterung nach XML-Dateien. Es werden nur Verzeichnisse und XML-Dateien angezeigt. Es wird die Klasse {@link
	 * FileFilter} erweitert.
	 */
	private static class XMLFilter extends FileFilter {

		public boolean accept(File file) {
			if(file.isDirectory()) {
				return true;
			}
			String extension = getExtension(file);
			if(extension != null) {
				if(extension.equals("xml")) {
					return true;
				}
			}
			return false;
		}

		public String getDescription() {
			return "Generischer Test Monitor";
		}

		/**
		 * Ermittelt die Endung einer Datei.
		 *
		 * @param file die Datei
		 *
		 * @return die Endung der Datei
		 */
		private String getExtension(final File file) {
			String ext = null;
			String s = file.getName();
			int i = s.lastIndexOf(".");
			if(i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}


	/**
	 * Diese Klasse weist einem Spaltenheader einer Tabelle einen Tooltip zu. Anhand der Mausposition wird ermittelt, über welcher Spalte sich die Maus befindet
	 * und welcher Tooltip dem Header zugewiesen wird.
	 */
	private static class ColumnHeaderToolTips extends MouseMotionAdapter {

		/** Speichert die aktuelle Spalte, die einen Tooltip anzeigt. Das reduziert die Aufrufe von <code>setToolTipText()</code>. */
		TableColumn _currentColumn;

		/** Speichert für jeden Spaltenkopf den Tooltip. */
		Map<TableColumn, String> _tooltips = new HashMap<TableColumn, String>();


		/**
		 * Wird als Tooltip <code>null</code> übergeben, dann wird der bestehende Eintrag gelöscht.
		 *
		 * @param column  die Spalte, die einen Tooltip bekommt
		 * @param tooltip der Tooltip für den Spaltenkopf
		 */
		public void setToolTip(final TableColumn column, final String tooltip) {
			if(tooltip == null) {
				_tooltips.remove(column);
			}
			else {
				_tooltips.put(column, tooltip);	// bestehender Tooltip wird überschrieben
			}
		}

		/**
		 * Ermittelt die Spalte anhand der Mausposition und weist dem Header den entsprechenden Tooltip zu.
		 *
		 * @param evt Mausereignis
		 */
		public void mouseMoved(MouseEvent evt) {
			JTableHeader tableHeader = (JTableHeader)evt.getSource();
			JTable table = tableHeader.getTable();	 // Tabelle wird ermittelt
			TableColumnModel columnModel = table.getColumnModel();		// um die Spalte zu ermitteln

			int columnIndex = columnModel.getColumnIndexAtX(evt.getX());
			TableColumn column = columnModel.getColumn(columnIndex);

			if(column != _currentColumn) {
				tableHeader.setToolTipText((String)_tooltips.get(column));
				_currentColumn = column;
			}
		}
	}
}
