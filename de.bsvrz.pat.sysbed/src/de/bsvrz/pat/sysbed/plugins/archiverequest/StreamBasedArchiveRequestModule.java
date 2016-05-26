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

package de.bsvrz.pat.sysbed.plugins.archiverequest;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.archive.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.dataview.ArchiveDataTableView;
import de.bsvrz.pat.sysbed.dataview.DataTableObject;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModule;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.sys.funclib.concurrent.Semaphore;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse implementiert das Modul für eine streambasierte Archivanfrage. Mit Hilfe eines Dialogs werden alle benötigten Parameter eingestellt, die die
 * streambasierte Archivanfrage braucht. Hierzu gehört die Priorität, der (Zeit-)Bereich und die Art der Archivanfrage. Falls erforderlich muß die
 * Sortierreihenfolge der als nachgeliefert gekennzeichneten Archivdatensätzen angegeben werden. Weiterhin muß angegeben werden, ob es sich um eine Zustands-
 * oder Deltaanfrage handelt. Zur Darstellung in einer Tabelle kann die Sortierreihenfolge der Archivdaten angegeben werden. Zwei Möglichkeiten stehen hierfür
 * zur Verfügung. Es ist möglich nach der Zeit oder nach den Datenidentifikationen zu sortieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StreamBasedArchiveRequestModule extends ExternalModuleAdapter implements ExternalModule {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** speichert den Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog des Archivmoduls */
	private static StreamBasedArchiveRequestDialog _dialog;

	/* ################### Methoden ########### */

	/**
	 * Gibt den Namen des Moduls zurück.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Archivanfrage Stream";
	}

	/**
	 * Gibt des Text des Buttons zurück.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Archivanfrage (Stream)";
	}

	/**
	 * Gibt den Text des Tooltips zurück.
	 *
	 * @return Text des Tooltips
	 */
	public String getTooltipText() {
		return _tooltipText;
	}

	/**
	 * Diese Methode erhält alle ausgewählten Parameter und startet den {@link StreamBasedArchiveRequestDialog Dialog} zur Auswahl weiterer Einstellungen der
	 * Archivanfrage.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new StreamBasedArchiveRequestDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für die Archivanfrage und startet diese ohne den Dialog anzuzeigen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new StreamBasedArchiveRequestDialog();
		_dialog.startRequest(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für die Archivanfrage und startet den {@link StreamBasedArchiveRequestDialog Dialog} und füllt ihn entsprechend mit
	 * den Einstellungen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new StreamBasedArchiveRequestDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Überprüft, ob die Voraussetzungen für das Modul gegeben sind.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt zurück, ob die Voraussetzungen für das Modul gegeben sind
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		if(!super.isPreselectionValid(settingsData)) {
			_tooltipText = "Genau eine Attributgruppe, ein Aspekt und mindestens ein Objekt müssen ausgewählt sein.";
			return false;
		}

		// ATGV prüfen
		final AttributeGroupUsage atgUsage = settingsData.getAttributeGroup().getAttributeGroupUsage(settingsData.getAspect());
		if(atgUsage == null || atgUsage.isConfigurating()) {
			_tooltipText = "Es muss eine Online-Attributgruppenverwendung ausgewählt werden.";
			return false;
		}
		_tooltipText = "Auswahl übernehmen";
		return true;
	}

	/* ################ Klasse ArchiveRequestDialog ############# */

	/**
	 * Stellt einen Dialog dar, womit Parameter für die Archivanfrage eingestellt werden können. Diese Einstellungen können gespeichert werden. Durch betätigen des
	 * "OK"-Buttons werden die Einstellungen übernommen, die Archivanfrage gestartet und der Dialog geschlossen. Durch betätigen des "Speichern unter ..."-Buttons
	 * werden die Einstellungen gespeichert.
	 */
	private class StreamBasedArchiveRequestDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** Speichert den Zugriff auf das Archivsystem. */
		private ArchiveRequestManager _archiveRequestManager;

		/** speichert die Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** Gibt die möglichen Prioritäten für eine Archivanfrage an. */
		private final String[] _priorityUnit = {"Hoch", "Mittel", "Niedrig"};

		/** Gibt die möglichen (Zeit-)Bereiche an. */
		private final String[] _timingUnit = {"Datenzeitstempel", "Archivzeitstempel", "Datenindex"};

		/** Gibt die möglichen Sortierreihenfolgen der als nachgeliefert gekennzeichneten Archivdatensätze an. */
		private final String[] _sortOfDataUnit = {"Datenindex", "Datenzeitstempel"};

		/** Betrifft die Sortierung der Datensätze bei der Darstellung in einer Tabelle. */
		private final String[] _sortViewUnit = {"Zeitstempel", "Datenidentifikation"};

		/** Das Panel für die (Zeit-)Bereichsanfrage. */
		private JPanel _domainPanel;

		/** Die Auswahlbox für die Prioritäten der Archivanfrage. */
		private JComboBox _priorityComboBox;

		/** Die Auswahlbox für die möglichen (Zeit-)Bereiche der Archivanfrage. */
		private JComboBox _timingComboBox;

		/** Gibt den Startwert des Zeitbereichs der Archivanfrage an. */
		private JSpinner _startTimeSpinner;

		/** Gibt den Endzeitpunkt des Zeitbereichs der Archivanfrage an. */
		private JSpinner _endTimeSpinner;

		/** Gibt den Startwert der Anfrage für des Datenindex an. */
		private JSpinner _startIndexSpinner;

		/** Gibt den Endwert der Anfrage für den Datenindex an. */
		private JSpinner _endIndexSpinner;

		/** Gibt an, wieviele Datensätze vor dem Endwert der Archivanfrage übertragen werden sollen. */
		private JSpinner _numberOfDatasetsSpinner;

		/** Das Ankreuzfeld für die Art der Archivanfrage: aktuelle Daten */
		private JCheckBox _oaDataCheckBox;

		/** Das Ankreuzfeld für die Art der Archivanfrage: nachgefordert-aktuelle Daten */
		private JCheckBox _naDataCheckBox;

		/** Das Ankreuzfeld für die Art der Archivanfrage: nachgelieferte Daten */
		private JCheckBox _onDataCheckBox;

		/** Das Ankreuzfeld für die Art der Archivanfrage: nachgefordert-nachgelieferte Daten */
		private JCheckBox _nnDataCheckBox;

		/** Die Auswahlbox für die Sortierreihenfolge der als nachgeliefert gekennzeichneten Archivdatensätze. */
		private JComboBox _sortSequenceComboBox;

		/** Die Auswahlbox für die Sortierung der Archivdatensätze zur Darstellung in einer Tabelle. */
		private JComboBox _viewSortComboBox;

		/** zur Auswahl, ob historische Objekte berücksichtigt werden sollen */
		private JCheckBox _useOldObjectsBox;

		/** Auswahlschaltfläche, ob alle Datensätze in dem spezifizierten Zeitraum übergeben werden sollen. */
		private JRadioButton _stateRadioButton;

		/** Auswahlschaltfläche, ob nur Archivdatensätze übermittelt werden, die sich auch vom vorhergehenden Datensatz unterscheiden. */
		private JRadioButton _deltaRadioButton;

		/** Stellt die untere Buttonleiste dar mit den Buttons "Speichern unter", "Abbrechen" und "OK" */
		private ButtonBar _buttonBar;

		/** Gibt an, ob der ausgewählte Bereich relativ oder absolut ist. */
		private JCheckBox _relativeBox;

		/* ################# Methoden ################### */

		/** Standardkonstruktor. Erstellt ein Objekt der Klasse. */
		public StreamBasedArchiveRequestDialog() {
			_archiveRequestManager = getConnection().getArchive();   // das aktuelle Archivsystem wird geholt
		}

		/**
		 * Mit dieser Methode können die Datenidentifikationsdaten übergeben werden.
		 *
		 * @param data enthält die ausgewählte Datenidentifikation
		 */
		public void setDataIdentification(final SettingsData data) {
			if(_dialog == null) {
				createDialog();
			}
			_dataIdentificationChoice.setDataIdentification(
					data.getObjectTypes(), data.getAttributeGroup(), data.getAspect(), data.getObjects(), data.getSimulationVariant()
			);
			_dataIdentificationChoice.showTree(getApplication().getTreeNodes(), getApplication().getConnection(), data.getTreePath());
			showDialog();
		}

		/**
		 * Diese Methode zeigt den Dialog an und trägt die Einstellungsdaten in die entsprechenden Felder ein.
		 *
		 * @param data Einstellungsdaten
		 */
		public void setSettings(final SettingsData data) {
			if(_dialog == null) {
				createDialog();
			}
			_dataIdentificationChoice.setDataIdentification(
					data.getObjectTypes(), data.getAttributeGroup(), data.getAspect(), data.getObjects(), data.getSimulationVariant()
			);
			_dataIdentificationChoice.showTree(getApplication().getTreeNodes(), getApplication().getConnection(), data.getTreePath());

			String timing = "";
			String relative = "";
			String from = "";
			String to = "";
			List keyValueList = data.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				String value = keyValueObject.getValue();
				if(key.equals("priority")) {
					setPriority(value);
				}
				else if(key.equals("timing")) {
					timing = value;
					setTimingType(timing);
				}
				else if(key.equals("relative")) {
					relative = value;
					setRelative(relative);
				}
				else if(key.equals("from")) {
					from = value;
				}
				else if(key.equals("to")) {
					to = value;
				}
				else if(key.equals("archivetype")) {
					setArchiveType(value);
				}
				else if(key.equals("sortsequence")) {
					setSortSequence(value);
				}
				else if(key.equals("requestview")) {
					setRequestView(value);
				}
				else if(key.equals("viewsort")) {
					setViewSort(value);
				}
				else if(key.equals("oldobj")) {
					setUseOldObjects(value);
				}
			}
			setFrom(timing, from, relative);
			setTo(timing, to);

			showDialog();
		}

		/**
		 * Startet die Archivanfrage anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startRequest(final SettingsData settingsData) {
			ArchiveQueryPriority archiveQueryPriority = ArchiveQueryPriority.LOW;
			TimingType timingType = TimingType.ARCHIVE_TIME;
			boolean startRelative = false;
			boolean useOldObjects = false;
			long intervalStart = 0;
			long intervalEnd = 0;
			ArchiveDataKindCombination archiveDataKindCombination = new ArchiveDataKindCombination(ArchiveDataKind.ONLINE);
			ArchiveOrder archiveOrder = ArchiveOrder.BY_INDEX;
			ArchiveRequestOption archiveRequestOption = ArchiveRequestOption.NORMAL;

			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				String value = keyValueObject.getValue();
				if(key.equals("priority")) {
					if(value.equals("Hoch")) {
						archiveQueryPriority = ArchiveQueryPriority.HIGH;
					}
					else if(value.equals("Mittel")) {
						archiveQueryPriority = ArchiveQueryPriority.MEDIUM;
					}
					else if(value.equals("Niedrig")) {
						archiveQueryPriority = ArchiveQueryPriority.LOW;
					}
				}
				else if(key.equals("timing")) {
					if(value.equals("Datenzeitstempel")) {
						timingType = TimingType.DATA_TIME;
					}
					else if(value.equals("Archivzeitstempel")) {
						timingType = TimingType.ARCHIVE_TIME;
					}
					else if(value.equals("Datenindex")) {
						timingType = TimingType.DATA_INDEX;
					}
				}
				else if(key.equals("relative")) {
					startRelative = (value.equals("true"));
				}
				else if(key.equals("from")) {
					intervalStart = Long.parseLong(value);
				}
				else if(key.equals("to")) {
					intervalEnd = Long.parseLong(value);
				}
				else if(key.equals("archivetype")) {
					String[] types = value.split(" ");
					ArchiveDataKind[] dataKinds = new ArchiveDataKind[types.length];
					for(int i = 0; i < types.length; i++) {
						String type = types[i];
						if(type.equals("oa")) {
							dataKinds[i] = ArchiveDataKind.ONLINE;
						}
						else if(type.equals("on")) {
							dataKinds[i] = ArchiveDataKind.ONLINE_DELAYED;
						}
						else if(type.equals("na")) {
							dataKinds[i] = ArchiveDataKind.REQUESTED;
						}
						else if(type.equals("nn")) {
							dataKinds[i] = ArchiveDataKind.REQUESTED_DELAYED;
						}
					}
					int length = dataKinds.length;
					switch(length) {
						case 1:
							archiveDataKindCombination = new ArchiveDataKindCombination(dataKinds[0]);
							break;
						case 2:
							archiveDataKindCombination = new ArchiveDataKindCombination(dataKinds[0], dataKinds[1]);
							break;
						case 3:
							archiveDataKindCombination = new ArchiveDataKindCombination(dataKinds[0], dataKinds[1], dataKinds[2]);
							break;
						case 4:
							archiveDataKindCombination = new ArchiveDataKindCombination(dataKinds[0], dataKinds[1], dataKinds[2], dataKinds[3]);
							break;
						default:
							archiveDataKindCombination = new ArchiveDataKindCombination(ArchiveDataKind.ONLINE);
					}
				}
				else if(key.equals("sortsequence")) {
					if(value.equals("Datenindex")) {
						archiveOrder = ArchiveOrder.BY_INDEX;
					}
					else {	 // nach Datenzeitstempel
						archiveOrder = ArchiveOrder.BY_DATA_TIME;
					}
				}
				else if(key.equals("requestview")) {
					if(value.equals("state")) {
						archiveRequestOption = ArchiveRequestOption.NORMAL;
					}
					else if(value.equals("delta")) {
						archiveRequestOption = ArchiveRequestOption.DELTA;
					}
				}
				else if(key.equals("viewsort")) {	 // Sortierung bei mehreren Datenidentifikationen
					_debug.fine("Sortierung bei mehreren Datenidentifikationen noch nicht implementiert");
				}
				else if(key.equals("oldobj")){
					useOldObjects = (value.equals("true"));
				}
			}
			DataDescription dataDescription = new DataDescription(
					settingsData.getAttributeGroup(), settingsData.getAspect(), (short)settingsData.getSimulationVariant()
			);

			_archiveRequestManager = getConnection().getArchive();
			ArchiveTimeSpecification archiveTimeSpecification = new ArchiveTimeSpecification(timingType, startRelative, intervalStart, intervalEnd);

			List<ArchiveDataSpecification> archiveDataSpecifications = new LinkedList<ArchiveDataSpecification>();
			List objects = settingsData.getObjects();
			boolean usePidQuery = useOldObjects;
			for(Iterator iterator = objects.iterator(); iterator.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator.next();
				ArchiveDataSpecification ads = new ArchiveDataSpecification(
						archiveTimeSpecification, archiveDataKindCombination, archiveOrder, archiveRequestOption, dataDescription,
						systemObject
				);
				if(usePidQuery){
					try{
						ads.setQueryWithPid();
					}
					catch(NoSuchMethodError e){
						usePidQuery = false;
					}
				}
				archiveDataSpecifications.add(ads);
			}
			if(useOldObjects && !usePidQuery){
				_debug.warning("Archivanfrage kann historische Objekte nicht berücksichtigen, bitte DAF-Bibliothek aktualisieren.");
			}
			// Erzeugen des Ausgabefensters mit der Online-Tabelle
			final ArchiveDataTableView dataTableView = new ArchiveDataTableView(settingsData, getConnection(), dataDescription);	// anzeigen der Tabelle

			final ArchiveDataQueryResult queryResult = _archiveRequestManager.request(archiveQueryPriority, archiveDataSpecifications);	 // Anfrage starten
			try {
				if(queryResult.isRequestSuccessful()) {
					_debug.info("Archivanfrage konnte erfolgreich bearbeitet werden.");
					ArchiveDataStream[] archiveDataStream = queryResult.getStreams();
					Thread archiveThread = new Thread(new ArchiveRequest(archiveDataStream, dataTableView, timingType), "GTM-Archivanfrage");
					archiveThread.start();
				}
				else {
					_debug.warning("Eine Archivanfrage konnte nicht bearbeitet werden, Fehler: " + queryResult.getErrorMessage());
					JOptionPane.showMessageDialog(
							_dialog, "Die Archivanfrage konnte nicht bearbeitet werden.", "Archivanfrage fehlerhaft", JOptionPane.ERROR_MESSAGE
					);
				}
			}
			catch(InterruptedException ex) {
				_debug.warning("Die Übertragung der Archivdaten wurde aufgrund eines Übertragungsfehlers unterbrochen (siehe exception)", ex);
				JOptionPane.showMessageDialog(_dialog, "Übertragung der Archivdaten wurde unterbrochen.", "Übertragungsfehler", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Diese Klasse verarbeitet die erhaltenen Streams einer Archivanfrage und stellt die Datensätze in einer Online-Tabelle dar. */
		private final class ArchiveRequest implements Runnable {

			private final ArchiveDataStream[] _archiveDataStreams;

			private final ArchiveDataTableView _dataTableView;

			private final TimingType _timingType;

			public ArchiveRequest(ArchiveDataStream[] archiveDataStreams, ArchiveDataTableView dataTableView, TimingType timingType) {
				_archiveDataStreams = archiveDataStreams;
				_dataTableView = dataTableView;
				_timingType = timingType;
			}

			public void run() {
				

				// Dieses Lock sorgt dafür, dass die beiden Threads abwechselnd zugreifen.
				// Swing nimmt sich einen Datensatz (solange ist take gesperrt), dann darf take wieder einen Datensatz ablegen
				final Semaphore lock = new Semaphore(1);
				for(int i = 0; i < _archiveDataStreams.length; i++) {
					try {
						ArchiveDataStream dataStream = _archiveDataStreams[i];
						// Es soll ein take ausgeführt werden, also muss ein Lock angefordert werden
						lock.acquire();
						ArchiveData archiveData = dataStream.take();
						while(archiveData != null) {
							if(_dataTableView.isDisposed()) {
								_debug.finer("Archivanfrage wurde abgebrochen", dataStream.getDataSpecification().toString());
								dataStream.abort();
								break;
							}
							else {
								// Datensatz für die Online-Tabelle erzeugen und weiterreichen

								// Es wird eine final Variable benötigt
								final ArchiveData helper = archiveData;
								Runnable runner = new Runnable() {
									public void run() {
										DataTableObject dataTableObject = new DataTableObject(
												helper.getObject(),
												helper.getDataDescription(),
												_timingType,
												helper.getArchiveTime(),
												helper.getDataTime(),
												helper.getDataIndex(),
												helper.getDataType(),
												helper.getDataKind(),
												helper.getData()
										);
										_dataTableView.addDataset(dataTableObject);
										// Die Daten wurden dargestellt, also darf nun der nächste Datensatz erzeugt werden
										lock.release();
									}
								};
								EventQueue.invokeLater(runner);

								// Es sollen Datensätze angefordert werden, dies darf nur geschehen, wenn der Swingthread
								// fertig ist
								lock.acquire();
								// nächsten Datensatz vom Archivsystem holen
								archiveData = dataStream.take();
							}
						}
					}
					catch(InterruptedException ex) {
						_debug.error("Die Übertragung der Archivdaten wurde aufgrund eines Übertragungsfehlers unterbrochen (siehe exception)", ex);
						JOptionPane.showMessageDialog(
								_dialog, "Übertragung der Archivdaten wurde unterbrochen.", "Übertragungsfehler", JOptionPane.ERROR_MESSAGE
						);
					}
					catch(IOException ex) {
						_debug.error("Übertragungsfehler zum Datenverteiler oder zum Archiv (siehe exception)", ex);
						JOptionPane.showMessageDialog(
								_dialog, "Problem mit Datenverteiler oder Archivsystem.", "Übertragungsfehler", JOptionPane.ERROR_MESSAGE
						);
					}
					finally {
						lock.release();
						_debug.info("Keine weiteren Archivdatensätze für diese Datenidentifikation.");
					}
				}
				_debug.info("Alle Datenidentifikationen abgearbeitet. Keine weiteren Daten vorhanden.");
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation, die Archivoptionen und die Darstellungsoptionen. */
		private void createDialog() {
			_dialog = new JDialog();
			_dialog.setTitle(getButtonText());
			_dialog.setResizable(false);

			Container pane = _dialog.getContentPane();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

			// Datenidentifikationsauswahl-Panel
			final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
			DataModel configuration = getConnection().getDataModel();
			types.add(configuration.getType("typ.konfigurationsObjekt"));
			types.add(configuration.getType("typ.dynamischesObjekt"));
			_dataIdentificationChoice = new DataIdentificationChoice(null, types);
			pane.add(_dataIdentificationChoice);

			// Archivoptionen
			JPanel archivePanel = new JPanel();
			archivePanel.setBorder(BorderFactory.createTitledBorder("Archivoptionen"));
			archivePanel.setLayout(new BoxLayout(archivePanel, BoxLayout.Y_AXIS));

			// Priorität
			JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel priorityLabel = new JLabel("Priorität der Anfrage: ");
			_priorityComboBox = new JComboBox(_priorityUnit);
			priorityPanel.add(priorityLabel);
			priorityPanel.add(Box.createHorizontalStrut(5));
			priorityPanel.add(_priorityComboBox);
			archivePanel.add(priorityPanel);

			_domainPanel = new JPanel();
			_domainPanel.setLayout(new BoxLayout(_domainPanel, BoxLayout.Y_AXIS));
			_domainPanel.setBorder(BorderFactory.createTitledBorder("(Zeit-)Bereich der Anfrage"));

			_relativeBox = new JCheckBox("relative Angabe");
			_relativeBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent event) {
							final JPanel domainPanel = createDomainPanel(_timingComboBox.getSelectedIndex(), (event.getStateChange() == 1));
							_domainPanel.remove(1);
							_domainPanel.add(domainPanel);
							_dialog.repaint();
							_dialog.pack();
						}
					}
			);

			JPanel domainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel domainLabel = new JLabel("Bereich: ");
			_timingComboBox = new JComboBox(_timingUnit);
			_timingComboBox.setSelectedIndex(0);
			_timingComboBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent event) {
							final JPanel domainPanel = createDomainPanel(_timingComboBox.getSelectedIndex(), _relativeBox.isSelected());
							_domainPanel.remove(1);
							_domainPanel.add(domainPanel);
							_dialog.repaint();
							_dialog.pack();
						}
					}
			);

			domainPanel.add(domainLabel);
			domainPanel.add(_timingComboBox);
			domainPanel.add(Box.createHorizontalStrut(10));
			domainPanel.add(_relativeBox);

			// Bereich hinzufügen bzw. wegnehmen
			_domainPanel.add(domainPanel);
			_domainPanel.add(createDomainPanel(_timingComboBox.getSelectedIndex(), _relativeBox.isSelected()));
			archivePanel.add(_domainPanel);

			// Art der Archivanfrage
			JPanel archiveTypePanel = new JPanel();
			archiveTypePanel.setBorder(BorderFactory.createTitledBorder("Art der Archivanfrage"));
			archiveTypePanel.setLayout(new BoxLayout(archiveTypePanel, BoxLayout.Y_AXIS));

			_oaDataCheckBox = new JCheckBox("aktuelle Daten", true);
			_naDataCheckBox = new JCheckBox("nachgefordert-aktuelle Daten", false);
			_onDataCheckBox = new JCheckBox("nachgelieferte Daten", false);
			_nnDataCheckBox = new JCheckBox("nachgefordert-nachgelieferte Daten", false);
			final JPanel sortSequencePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JLabel sortSequenceLabel = new JLabel("Sortierreihenfolge der nachgelieferten Daten: ");
			sortSequenceLabel.setEnabled(false);
			_sortSequenceComboBox = new JComboBox(_sortOfDataUnit);
			_sortSequenceComboBox.setEnabled(false);
			sortSequencePanel.add(sortSequenceLabel);
			sortSequencePanel.add(_sortSequenceComboBox);

			_useOldObjectsBox = new JCheckBox("Historische Objekte mit gleicher Pid berücksichtigen");

			_oaDataCheckBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if(_oaDataCheckBox.isSelected()) {
								// OK-Button enablen
								_buttonBar.getAcceptButton().setEnabled(true);
							}
							else {
								// ist jetzt keiner mehr ausgewählt? -> OK-Button disablen
								if(!_naDataCheckBox.isSelected() && !_onDataCheckBox.isSelected() && !_nnDataCheckBox.isSelected()) {
									_buttonBar.getAcceptButton().setEnabled(false);
								}
							}
						}
					}
			);
			_naDataCheckBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if(_naDataCheckBox.isSelected()) {
								_buttonBar.getAcceptButton().setEnabled(true);
							}
							else {
								if(!_oaDataCheckBox.isSelected() && !_onDataCheckBox.isSelected() && !_nnDataCheckBox.isSelected()) {
									_buttonBar.getAcceptButton().setEnabled(false);
								}
							}
						}
					}
			);

			_onDataCheckBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent event) {
							if(_onDataCheckBox.isSelected()) {	// -> Sortierreihenfolge aktivieren
								_buttonBar.getAcceptButton().setEnabled(true);
								sortSequenceLabel.setEnabled(true);
								_sortSequenceComboBox.setEnabled(true);
								sortSequencePanel.validate();
							}
							else {		// deaktivieren, wenn die andere Box (nachgefordert-nachgelieferte Daten) auch nicht selektiert ist
								if(!_oaDataCheckBox.isSelected() && !_naDataCheckBox.isSelected() && !_nnDataCheckBox.isSelected()) {
									_buttonBar.getAcceptButton().setEnabled(false);
								}
								if(!_nnDataCheckBox.isSelected()) {
									sortSequenceLabel.setEnabled(false);
									_sortSequenceComboBox.setEnabled(false);
									sortSequencePanel.validate();
								}
							}
						}
					}
			);

			_nnDataCheckBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							if(_nnDataCheckBox.isSelected()) {
								_buttonBar.getAcceptButton().setEnabled(true);
								sortSequenceLabel.setEnabled(true);
								_sortSequenceComboBox.setEnabled(true);
								sortSequencePanel.validate();
							}
							else {
								if(!_oaDataCheckBox.isSelected() && !_naDataCheckBox.isSelected() && !_onDataCheckBox.isSelected()) {
									_buttonBar.getAcceptButton().setEnabled(false);
								}
								if(!_onDataCheckBox.isSelected()) {
									sortSequenceLabel.setEnabled(false);
									_sortSequenceComboBox.setEnabled(false);
									sortSequencePanel.validate();
								}
							}
						}
					}
			);
			JPanel gridPanel = new JPanel(new GridLayout(4, 1));
			gridPanel.add(_oaDataCheckBox);
			gridPanel.add(_onDataCheckBox);
			gridPanel.add(_naDataCheckBox);
			gridPanel.add(_nnDataCheckBox);

			archiveTypePanel.add(gridPanel);
			archiveTypePanel.add(sortSequencePanel);
			archivePanel.add(archiveTypePanel);

			// Zustands- oder Deltaanfrage
			JPanel requestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ButtonGroup requestButtonGroup = new ButtonGroup();
			_stateRadioButton = new JRadioButton("Zustandsanfrage", true);
			_deltaRadioButton = new JRadioButton("Deltaanfrage", false);
			requestButtonGroup.add(_stateRadioButton);
			requestButtonGroup.add(_deltaRadioButton);
			requestPanel.add(_stateRadioButton);
			requestPanel.add(_deltaRadioButton);
			archivePanel.add(requestPanel);

			// Historische Objekte berücksichtigen
			JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT));
			box.add(_useOldObjectsBox);
			archivePanel.add(box);

			// Sortierung der Darstellung
			JPanel viewSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			viewSortPanel.setBorder(BorderFactory.createTitledBorder("Darstellungsoptionen"));
			JLabel viewSortLabel = new JLabel("Daten sortieren nach: ");
			_viewSortComboBox = new JComboBox(_sortViewUnit);
			_viewSortComboBox.setSelectedIndex(1);
			_viewSortComboBox.setEnabled(false);	
			viewSortPanel.add(viewSortLabel);
			viewSortPanel.add(_viewSortComboBox);


			pane.add(archivePanel);
			pane.add(viewSortPanel);

			// untere Buttonleiste
			_buttonBar = new ButtonBar(this);	 // brauche noch Übergabeparameter
			_dialog.getRootPane().setDefaultButton(_buttonBar.getAcceptButton());
			pane.add(_buttonBar);

			// Listener, ob das Archivsystem da ist
			if(!_archiveRequestManager.isArchiveAvailable()) {
				_buttonBar.getAcceptButton().setEnabled(false);
				_buttonBar.getAcceptButton().setToolTipText("Das Archivsystem ist nicht verfügbar.");
			}
			_archiveRequestManager.addArchiveAvailabilityListener(new ArchiveListener(_buttonBar.getAcceptButton()));
		}

		private class ArchiveListener implements ArchiveAvailabilityListener {

			private final JButton _okButton;

			public ArchiveListener(JButton okButton) {
				_okButton = okButton;
			}

			public void archiveAvailabilityChanged(ArchiveRequestManager archive) {
				if(archive.isArchiveAvailable()) {
					_okButton.setEnabled(true);
					_okButton.setToolTipText(null);
				}
				else {
					_okButton.setEnabled(false);
					_okButton.setToolTipText("Das Archivsystem ist nicht verfügbar.");
				}
			}
		}

		/**
		 * Das Panel für den Bereich, der angefragt wird, wird erstellt und zurückgegeben.
		 *
		 * @param domain     Datenzeitstempel, Archivzeitstempel oder Datenindex
		 * @param isRelative gibt an, ob es sich um eine relative Bereichsangabe handelt
		 *
		 * @return Panel für den anzufragenden Bereich
		 */
		private JPanel createDomainPanel(int domain, boolean isRelative) {
			if(domain == 0 || domain == 1) {	// Zeitbereich
				final JPanel domainPanel = new JPanel();
				domainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				domainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				final JLabel startLabel;
				long time = System.currentTimeMillis();	 // Werte für die Zeitbereiche minutengenau einstellen
				long temp = time % 60000;
				time = time - temp;
				if(isRelative) {				// relative Angabe
					startLabel = new JLabel("Anzahl Datensätze: ");
					if(_numberOfDatasetsSpinner == null) {
						final SpinnerModel numberModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
						_numberOfDatasetsSpinner = new JSpinner(numberModel);
						_numberOfDatasetsSpinner.setPreferredSize(new Dimension(100, _numberOfDatasetsSpinner.getPreferredSize().height));
					}
				}
				else {						// absolute Bereichsangabe
					startLabel = new JLabel("Von: ");
					if(_startTimeSpinner == null) {
						final SpinnerDateModel startTimeModel = new SpinnerDateModel();
						startTimeModel.setValue(new Date(time));
						_startTimeSpinner = new JSpinner(startTimeModel);
						_startTimeSpinner.addChangeListener(
								new ChangeListener() {
									public void stateChanged(ChangeEvent e) {
										if(((Date)_startTimeSpinner.getModel().getValue()).getTime()
										   > ((Date)_endTimeSpinner.getModel().getValue()).getTime()) {
											_endTimeSpinner.getModel().setValue(_startTimeSpinner.getModel().getValue());
										}
									}
								}
						);
					}
				}
				final JLabel endLabel = new JLabel("Bis: ");
				if(_endTimeSpinner == null) {
					final SpinnerDateModel endTimeModel = new SpinnerDateModel();
					endTimeModel.setValue(new Date(time));
					_endTimeSpinner = new JSpinner(endTimeModel);
					_endTimeSpinner.addChangeListener(
							new ChangeListener() {
								public void stateChanged(ChangeEvent e) {
									if(((Date)_endTimeSpinner.getModel().getValue()).getTime() < ((Date)_startTimeSpinner.getModel().getValue()).getTime()) {
										_startTimeSpinner.getModel().setValue(_endTimeSpinner.getModel().getValue());
									}
								}
							}
					);
				}
				domainPanel.add(startLabel);
				if(isRelative) {
					domainPanel.add(_numberOfDatasetsSpinner);
				}
				else {
					domainPanel.add(_startTimeSpinner);
				}
				domainPanel.add(Box.createHorizontalStrut(10));
				domainPanel.add(endLabel);
				domainPanel.add(_endTimeSpinner);
				return domainPanel;
			}
			else {							// Datenindex
				final JPanel domainPanel = new JPanel();
				domainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				domainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				final JLabel startLabel;
				if(isRelative) {			   // relative Angabe
					startLabel = new JLabel("Anzahl Datensätze: ");
					if(_numberOfDatasetsSpinner == null) {
						final SpinnerModel numberModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
						_numberOfDatasetsSpinner = new JSpinner(numberModel);
						_numberOfDatasetsSpinner.setPreferredSize(new Dimension(100, _numberOfDatasetsSpinner.getPreferredSize().height));
					}
				}
				else {						// absolute Bereichsangabe
					startLabel = new JLabel("Von: ");
					if(_startIndexSpinner == null) {
						SpinnerModel startIndexModel = new SpinnerLongModel(0, 0, Long.MAX_VALUE, 1);
						_startIndexSpinner = new JSpinner(startIndexModel);
						_startIndexSpinner.setPreferredSize(new Dimension(180, _startIndexSpinner.getPreferredSize().height));
						_startIndexSpinner.addChangeListener(
								new ChangeListener() {
									public void stateChanged(ChangeEvent e) {
										if(((Long)_startIndexSpinner.getModel().getValue()) > ((Long)_endIndexSpinner.getModel().getValue())) {
											_endIndexSpinner.getModel().setValue(_startIndexSpinner.getModel().getValue());
										}
									}
								}
						);
					}
				}
				final JLabel endLabel = new JLabel("Bis: ");
				if(_endIndexSpinner == null) {
					SpinnerModel endIndexModel = new SpinnerLongModel(0, 0, Long.MAX_VALUE, 1);
					_endIndexSpinner = new JSpinner(endIndexModel);
					_endIndexSpinner.setPreferredSize(new Dimension(180, _endIndexSpinner.getPreferredSize().height));
					_endIndexSpinner.addChangeListener(
							new ChangeListener() {
								public void stateChanged(ChangeEvent e) {
									if(((Long)_endIndexSpinner.getModel().getValue()) < ((Long)_startIndexSpinner.getModel().getValue())) {
										_startIndexSpinner.getModel().setValue(_endIndexSpinner.getModel().getValue());
									}
								}
							}
					);
				}
				domainPanel.add(startLabel);
				if(isRelative) {
					domainPanel.add(_numberOfDatasetsSpinner);
				}
				else {
					domainPanel.add(_startIndexSpinner);
				}
				domainPanel.add(Box.createHorizontalStrut(10));
				domainPanel.add(endLabel);
				domainPanel.add(_endIndexSpinner);
				return domainPanel;
			}
		}

		/** Durch diese Methode wird der Dialog angezeigt. */
		private void showDialog() {
			_dialog.setLocation(50, 50);
			_dialog.pack();
			_dialog.setVisible(true);
		}

		/**
		 * Gibt die Priorität der Archivanfrage zurück.
		 *
		 * @return die Priorität ("Hoch", "Mittel", "Niedrig")
		 */
		private String getPriority() {
			return (String)_priorityComboBox.getSelectedItem();
		}

		/**
		 * Setzt die Priorität der Archivanfrage im Dialog.
		 *
		 * @param priority die Priorität ("Hoch", "Mittel", "Niedrig")
		 */
		private void setPriority(String priority) {
			_priorityComboBox.setSelectedItem(priority);
		}

		/**
		 * Gibt den (Zeit-)Bereich der Archivanfrage zurück.
		 *
		 * @return der (Zeit-)Bereich ("Datenzeitstempel", "Archivzeitstempel", "Datenindex")
		 */
		private String getTimingType() {
			return (String)_timingComboBox.getSelectedItem();
		}

		/**
		 * Setzt den (Zeit-)Bereich der Archivanfrage im Dialog.
		 *
		 * @param timingType der (Zeit-)Bereich ("Datenzeitstempel", "Archivzeitstempel", "Datenindex")
		 */
		private void setTimingType(String timingType) {
			_timingComboBox.setSelectedItem(timingType);
		}

		/**
		 * Gibt zurück, ob die Zeit- / Index-Angabe relativ oder absolut gemacht wurde. Wobei relativ bedeutet, dass der eine Wert die Anzahl der Datensätze bestimmt,
		 * die vor dem zweiten Wert liegen.
		 *
		 * @return ob relativ oder absolut
		 */
		private String getRelative() {
			return String.valueOf(_relativeBox.isSelected());
		}

		/**
		 * Das Flag des Moduls wird gesetzt, wo unterschieden werden kann, ob die Zeit-/Index-Angabe relativ oder absolut ist.
		 *
		 * @param relative gibt an, ob die Zeit-/Index-Angabe relativ oder absolut ist
		 */
		private void setRelative(String relative) {
			_relativeBox.setSelected((relative.equals("true")));
		}

		/**
		 * Gibt den Startpunkt des (Zeit-)Bereichs zurück.
		 *
		 * @return Startpunkt des (Zeit-)Bereichs
		 */
		private String getFrom() {
			if(_relativeBox.isSelected()) {	// relative Angabe
				Integer i = (Integer)_numberOfDatasetsSpinner.getModel().getValue();
				return String.valueOf(i);
			}
			else {			// absolute Angabe
				if(_timingComboBox.getSelectedIndex() == 0 || _timingComboBox.getSelectedIndex() == 1) {	// Zeitbereich
					Date date = (Date)_startTimeSpinner.getModel().getValue();
					return String.valueOf(date.getTime());
				}
				else {	// Datenindex
					Long l = (Long)_startIndexSpinner.getModel().getValue();
					return String.valueOf(l.longValue());
				}
			}
		}

		/**
		 * Setzt den Startpunkt des (Zeit-)Bereichs im Dialog.
		 *
		 * @param timing   der (Zeit-)Bereich ("Datenzeitstempel", "Archivzeitstempel", "Datenindex")
		 * @param from     der Startpunkt
		 * @param relative ob die Parameter relativ sind
		 */
		private void setFrom(String timing, String from, String relative) {
			if(relative.equals("true")) {
				Integer i = Integer.valueOf(from);
				_numberOfDatasetsSpinner.getModel().setValue(i);
			}
			else {
				if(timing.equals(_timingUnit[0]) || timing.equals(_timingUnit[1])) {	// Zeitbereich
					Date date = new Date(Long.parseLong(from));
					_startTimeSpinner.getModel().setValue(date);
				}
				else {
					_startIndexSpinner.getModel().setValue(Long.parseLong(from));
				}
			}
		}

		/**
		 * Gibt den Endpunkt des (Zeit-)Bereichs zurück.
		 *
		 * @return Endpunkt des Zeitbereichs oder Anzahl der Datensätze, die ausgegeben werden sollen
		 */
		private String getTo() {
			if(_timingComboBox.getSelectedIndex() == 0 || _timingComboBox.getSelectedIndex() == 1) {	// Zeitbereich
				Date date = (Date)_endTimeSpinner.getModel().getValue();
				return String.valueOf(date.getTime());
			}
			else {
				Long l = (Long)_endIndexSpinner.getModel().getValue();
				return String.valueOf(l.longValue());
			}
		}

		/**
		 * Setzt den Wert für das Feld "Bis:" bzw. "Anzahl vor dem Index:", je nachdem welche Timingangabe angegeben wird.
		 *
		 * @param timing "Datenzeitstempel", "Archivzeitstempel" oder "Datenindex"
		 * @param to     entweder ein Zeitstempel oder die Anzahl Datensätze vor dem Index
		 */
		private void setTo(String timing, String to) {
			if(timing.equals(_timingUnit[0]) || timing.equals(_timingUnit[1])) {	// Zeitbereich
				Date date = new Date(Long.parseLong(to));
				_endTimeSpinner.getModel().setValue(date);
			}
			else {	// Datenindex
				_endIndexSpinner.getModel().setValue(Long.valueOf(to));
			}
		}

		/**
		 * Gibt zurück, welche Arten der Archivanfrage ausgewählt wurden.
		 *
		 * @return z.B. "oa on nn"
		 */
		private String getArchiveType() {
			String result = "";
			if(_oaDataCheckBox.isSelected()) result += "oa ";	// aktuelle Daten
			if(_onDataCheckBox.isSelected()) result += "on ";	// nachgelieferte Daten
			if(_naDataCheckBox.isSelected()) result += "na ";	// nachgefordert-aktuelle Daten
			if(_nnDataCheckBox.isSelected()) result += "nn ";	// nachgefordert-nachgelieferte Daten
			result = result.substring(0, result.length() - 1);
			return result;
		}

		/**
		 * Setzt die Checkboxen, welche Art der Archivanfrage gesetzt sein soll.
		 *
		 * @param archiveType z.B. "oa on nn";
		 */
		private void setArchiveType(String archiveType) {
			String[] types = archiveType.split(" ");
			_oaDataCheckBox.setSelected(false);		// alle deselektieren
			for(int i = 0; i < types.length; i++) {
				String type = types[i];
				if(type.equals("oa")) {
					_oaDataCheckBox.setSelected(true);
				}
				else if(type.equals("na")) {
					_naDataCheckBox.setSelected(true);
				}
				else if(type.equals("on")) {
					_onDataCheckBox.setSelected(true);
				}
				else if(type.equals("nn")) {
					_nnDataCheckBox.setSelected(true);
				}
			}
		}

		/**
		 * Gibt zurück, wie die nachgelieferten Archivdatensätze einsortiert werden sollen.
		 *
		 * @return "Datenindex" oder "Datenzeitstempel"
		 */
		private String getSortSequence() {
			return (String)_sortSequenceComboBox.getSelectedItem();
		}

		/**
		 * Setzt den Parameter für die Sortierreihenfolge der als nachgeliefert gekennzeichneten Archivdatensätze.
		 *
		 * @param sortSequence Sortierreihenfolge der nachgeliegerten Archivdatensätze
		 */
		private void setSortSequence(String sortSequence) {
			_sortSequenceComboBox.setSelectedItem(sortSequence);
		}

		/**
		 * Gibt zurück, ob es sich um eine Zustands- oder Deltaanfrage handelt.
		 *
		 * @return "state" oder "delta"
		 */
		private String getRequestView() {
			if(_stateRadioButton.isSelected()) {
				return "state";
			}
			else {
				return "delta";
			}
		}

		/**
		 * Setzt den Parameter, ob es sich um eine Zustands- oder Deltaanfrage handelt.
		 *
		 * @param requestView Parameter: "state" oder "delta"
		 */
		private void setRequestView(String requestView) {
			if(requestView.equals("state")) {
				_stateRadioButton.setSelected(true);
				_deltaRadioButton.setSelected(false);
			}
			else {
				_stateRadioButton.setSelected(false);
				_deltaRadioButton.setSelected(true);
			}
		}

		/**
		 * Parameter wird abgefragt, wie die Daten in der Tabelle angezeigt werden sollen, sortiert nach der Zeit oder nach der Datenidentifikation.
		 *
		 * @return "Zeitstempel" oder "Datenidentifikation"
		 */
		private String getViewSort() {
			return (String)_viewSortComboBox.getSelectedItem();
		}

		/**
		 * Parameter wird gesetzt, wie die Daten in der Tabelle angezeigt werden sollen, sortiert nach der Zeit oder nach der Datenidentifikation.
		 *
		 * @param viewSort Parameter: "Zeitstempel" oder "Datenidentifikation"
		 */
		private void setViewSort(String viewSort) {
			_viewSortComboBox.setSelectedItem(viewSort);
		}

		/**
		 * Gibt zurück, historische Objekte mit gleicher Pid berücksichtigt werden sollen
		 *
		 * @return
		 */
		private String getUseOldObjects() {
			return String.valueOf(_useOldObjectsBox.isSelected());
		}

		/**
		 * Das Flag des Moduls wird gesetzt, wenn historische Objekte mit gleicher Pid berücksichtigt werden sollen
		 *
		 * @param relative
		 */
		private void setUseOldObjects(String relative) {
			_useOldObjectsBox.setSelected((relative.equals("true")));
		}


		/**
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name für die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = StreamBasedArchiveRequestModule.class;
			List<SystemObjectType> objectTypes = _dataIdentificationChoice.getObjectTypes();
			AttributeGroup atg = _dataIdentificationChoice.getAttributeGroup();
			Aspect asp = _dataIdentificationChoice.getAspect();
			List<SystemObject> objects = _dataIdentificationChoice.getObjects();

			SettingsData settingsData = new SettingsData(getModuleName(), moduleClass, objectTypes, atg, asp, objects);
			settingsData.setTitle(title);
			settingsData.setSimulationVariant(_dataIdentificationChoice.getSimulationVariant());
			settingsData.setTreePath(_dataIdentificationChoice.getTreePath());
			settingsData.setKeyValueList(getKeyValueList());

			return settingsData;
		}

		/**
		 * Sammelt alle Parameter des Dialogs.
		 *
		 * @return Liste aller Parameter des Dialogs
		 */
		private List<KeyValueObject> getKeyValueList() {
			List<KeyValueObject> keyValueList = new LinkedList<KeyValueObject>();
			keyValueList.add(new KeyValueObject("priority", getPriority()));
			keyValueList.add(new KeyValueObject("timing", getTimingType()));
			keyValueList.add(new KeyValueObject("relative", getRelative()));
			keyValueList.add(new KeyValueObject("from", getFrom()));
			keyValueList.add(new KeyValueObject("to", getTo()));
			keyValueList.add(new KeyValueObject("archivetype", getArchiveType()));
			if(_onDataCheckBox.isSelected() || _nnDataCheckBox.isSelected()) {
				keyValueList.add(new KeyValueObject("sortsequence", getSortSequence()));
			}
			keyValueList.add(new KeyValueObject("requestview", getRequestView()));
			keyValueList.add(new KeyValueObject("viewsort", getViewSort()));
			keyValueList.add(new KeyValueObject("oldobj", getUseOldObjects()));

			return keyValueList;
		}

		/**
		 * Durch betätigen des "OK"-Buttons wird die Archivanfrage mit den eingestellten Parametern in einem neuen Fenster gestartet und dieser Dialog wird
		 * geschlossen. Die Parameter werden gespeichert.
		 */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			try {
				startRequest(settingsData);
				doCancel();
				saveSettings(settingsData);
			}
			catch(Exception ex) {
				String message = ex.getMessage();
				_debug.error("Aufgrund einer unerwarteten Ausnahme konnte die gewünschte Archivanfrage nicht gestellt werden (siehe exception)", ex);
				JOptionPane.showMessageDialog(_dialog, message, "Archivanfrage kann nicht gestellt werden.", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Durch betätigen des "Abbrechen"-Buttons wird der Dialog geschlossen. */
		public void doCancel() {
			_dialog.setVisible(false);
			_dialog.dispose();
		}

		/**
		 * Durch betätigen des "Speichern unter ..."-Buttons werden die Einstellungen gespeichert.
		 *
		 * @param title Titel der Einstellungen
		 */
		public void doSave(String title) {
			SettingsData settingsData = getSettings(title);
			saveSettings(settingsData);
		}
	}

	private static class SpinnerLongModel extends SpinnerNumberModel {

		public SpinnerLongModel(long value, long minimum, long maximum, long stepSize) {
			super(new Long(value), new Long(minimum), new Long(maximum), new Long(stepSize));
		}
	}
}
