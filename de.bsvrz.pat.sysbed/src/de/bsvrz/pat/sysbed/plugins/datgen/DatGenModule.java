/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.plugins.datgen;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.OutputOptionsPanel;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.sys.funclib.consoleProcessFrame.ConsoleProcessFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Diese Klasse implementiert das {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Modul} "Datengenerator" f�r eine Applikation. Es gibt zwei M�glichkeiten, den Datengenerator zu
 * starten. Entweder durch �bergabe einer Datenidentifikation. Dann wird ein Dialog angezeigt, damit weitere Einstellungen vorgenommen werden k�nnen. Oder,
 * falls schon alle Einstellungen vorhanden sind, dann kann der Datengenerator direkt, durch �bergabe der Parameter gestartet werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 * @see de.bsvrz.pat.sysbed.plugins.api.ExternalModule
 */
public class DatGenModule extends ExternalModuleAdapter {

	/** speichert den Dialog des Datengenerators */
	private static DatGenDialog _dialog;

	/** der Tooltip-Text */
	private String _tooltipText;


	/* ############# Methoden ############## */
	/**
	 * Gibt den Namen des Moduls zur�ck.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Datengenerator";
	}

	/**
	 * Gibt den Text des Buttons zur�ck.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Datengenerator";
	}

	/**
	 * Gibt den aktuellen Tooltip zur�ck.
	 *
	 * @return aktueller Tooltip
	 */
	public String getTooltipText() {
		return _tooltipText;
	}

	/**
	 * Diese Methode erh�lt eine Datenidentifikation und startet den {@link DatGenDialog Dialog} zur Auswahl der Einstellungen des Datengenerators.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new DatGenDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r den Datengenerator und startet diesen ohne den Dialog anzuzeigen.
	 *
	 * @param settingsData die Einstellungen f�r den Datengenerator
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new DatGenDialog();
		_dialog.startConsole(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r den Datengenerator und startet den {@link DatGenDialog Dialog} und f�llt ihn entsprechend der Einstellungen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new DatGenDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * �berpr�ft, ob die ausgew�hlte Datenidentifikation f�r dieses Modul zutrifft oder nicht.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 *
	 * @return gibt an, ob die ausgew�hlte Datenidentifikation f�r dieses Modul zutrifft
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		if(!super.isPreselectionValid(settingsData)) {
			_tooltipText = "Genau eine Attributgruppe, ein Aspekt und mindestens ein Objekt m�ssen ausgew�hlt sein.";
			return false;
		}

		// ATGV pr�fen
		final AttributeGroupUsage atgUsage = settingsData.getAttributeGroup().getAttributeGroupUsage(settingsData.getAspect());
		if(atgUsage == null || atgUsage.isConfigurating()) {
			_tooltipText = "Es muss eine Online-Attributgruppenverwendung ausgew�hlt werden.";
			return false;
		}
		_tooltipText = "Auswahl �bernehmen";
		return true;
	}


	/* ############# Klasse DatGenDialog ############ */
	/**
	 * Stellt einen Dialog dar, womit Parameter f�r den Datengenerator eingestellt werden k�nnen. Durch bet�tigen des "OK"-Buttons werden die Einstellungen
	 * �bernommen, der Datengenerator gestartet und der Dialog geschlossen. Durch bet�tigen des "Speichern unter ..."-Buttons werden nur die Einstellungen
	 * gespeichert. Und durch bet�tigen des "Abbrechen"-Buttons wird der Dialog wieder geschlossen.
	 */
	private class DatGenDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert die Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt die Einheit f�r den Zyklus an */
		private final String[] _cycleUnit = {"Tage", "Stunden", "Minuten", "Sekunden", "Millisekunden"};

		/** gibt an, welche Rollen zur Verf�gung stehen */
		private final String[] _roleUnit = {"Quelle", "Sender"};

		/** das Eingabefeld f�r den Zyklus */
		private JSpinner _cycleSpinner;

		/** die Auswahl f�r die Einheit des Zyklus */
		private JComboBox _cycleComboBox;

		/** gibt an, ob die Anzahl der zu erstellenden Datens�tze beschr�nkt ist */
		private boolean _areDatasetsLimited = false;

		/** enth�lt die Anzahl der zu erzeugenden Datens�tze */
		private JSpinner _limitedDatasetsSpinner;

		/** hiermit kann die Spreizung der Daten angegeben werden */
		private JSlider _spreadSlider;

		/** hier�ber kann die Rolle angegeben werden */
		private JComboBox _roleComboBox;

		/** hiermit kann ausgew�hlt werden, ob die Anzahl der Datens�tze beschr�nkt sein sollen, oder nicht */
		private JCheckBox _limitedDatasetsCheckBox;

		/** speichert ein Objekt der Ausgabeoptionen */
		private OutputOptionsPanel _outputOptions;

		/** Stellt die untere Schaltfl�chen-Leiste dar. */
		private ButtonBar _buttonBar;


		/* ############### Methoden ############## */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public DatGenDialog() {
		}

		/**
		 * Mit dieser Methode k�nnen die Datenidentifikationsdaten �bergeben werden. Der Dialog wird mit Default-Werten dargestellt.
		 *
		 * @param data enth�lt die ausgew�hlte Datenidentifikation
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
		 * Diese Methode zeigt den Dialog an und tr�gt die Einstellungsdaten in die entsprechenden Felder ein.
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
			List keyValueList = data.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				if(key.equals("ausgabe")) {
					setDetailLevel(keyValueObject.getValue());
				}
				else if(key.equals("rolle")) {
					setRole(keyValueObject.getValue());
				}
				else if(key.equals("spreizung")) {
					setSpreading(Integer.parseInt(keyValueObject.getValue()));
				}
				else if(key.equals("zyklus")) {
					setCycle(keyValueObject.getValue());
				}
				else if(key.equals("anzahl")) {
					setLimitedDatasets(Integer.parseInt(keyValueObject.getValue()));
				}
				else if(key.equals("datei")) {
					setFileName(keyValueObject.getValue());
				}
			}
			showDialog();
		}

		/**
		 * Startet den Datengenerator anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startConsole(final SettingsData settingsData) {
			final List<String> parameterList = getParameterList(settingsData);
//			System.out.println("parameterList = " + parameterList);
			String[] arguments = (String[])parameterList.toArray(new String[0]);
			try {
				ConsoleProcessFrame.createJavaProcessFrame("Datengenerator", "de.bsvrz.pat.datgen.generator.main.DataGenerator", arguments, null, null);
			}
			catch(IOException ex) {
				String message = ex.getMessage();
				JOptionPane.showMessageDialog(_dialog, message, "Fehler beim Starten des Datengenerators", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation, Generatoroptionen, Anmeldeoptionen und die Rolle f�r den Datengenerator. */
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

			// Generatoroptionen
			JPanel generatorOptionsPanel = new JPanel();
			generatorOptionsPanel.setLayout(new BoxLayout(generatorOptionsPanel, BoxLayout.Y_AXIS));
			generatorOptionsPanel.setBorder(BorderFactory.createTitledBorder("Generatoroptionen"));

			JPanel cyclePanel = new JPanel();
			cyclePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel cycleLabel = new JLabel("Zyklus: ");
			_cycleSpinner = new JSpinner(new SpinnerNumberModel(60, 1, Integer.MAX_VALUE, 1));
			_cycleComboBox = new JComboBox(_cycleUnit);
			_cycleComboBox.setEditable(false);
			_cycleComboBox.setSelectedItem("Sekunden");
			cyclePanel.add(cycleLabel);
			cyclePanel.add(_cycleSpinner);
			cyclePanel.add(_cycleComboBox);

			JPanel limitedDatasetsPanel = new JPanel();
			limitedDatasetsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			_limitedDatasetsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
			_limitedDatasetsSpinner.setEnabled(false);
			_limitedDatasetsCheckBox = new JCheckBox("Beschr�nkte Anzahl zu erzeugender Datens�tze: ");
			_limitedDatasetsCheckBox.addItemListener(
					new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							int state = e.getStateChange();
							if(state == 1) {
								_limitedDatasetsSpinner.setEnabled(true);
								_areDatasetsLimited = true;
							}
							else {
								_limitedDatasetsSpinner.setEnabled(false);
								_areDatasetsLimited = false;
							}
						}
					}
			);
			limitedDatasetsPanel.add(_limitedDatasetsCheckBox);
			limitedDatasetsPanel.add(_limitedDatasetsSpinner);

			JPanel spreadPanel = new JPanel();
			spreadPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel spreadLabel = new JLabel("Spreizung der Datens�tze je Intervall: ");
			_spreadSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
			_spreadSlider.setPaintLabels(true);
			_spreadSlider.setPaintTicks(true);
//            spreadSlider.setPaintTrack(true);
			_spreadSlider.setMajorTickSpacing(50);
			_spreadSlider.setMinorTickSpacing(10);
			spreadLabel.setLabelFor(_spreadSlider);
			JLabel unitLabel = new JLabel("%");
			spreadPanel.add(spreadLabel);
			spreadPanel.add(_spreadSlider);
			spreadPanel.add(unitLabel);

			generatorOptionsPanel.add(cyclePanel);
			generatorOptionsPanel.add(limitedDatasetsPanel);
			generatorOptionsPanel.add(spreadPanel);

			pane.add(generatorOptionsPanel);

			// Anmeldeoptionen
			JPanel applyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			applyPanel.setBorder(BorderFactory.createTitledBorder("Anmeldeoptionen"));
			JLabel roleLabel = new JLabel("Rolle: ");
			_roleComboBox = new JComboBox(_roleUnit);
			_roleComboBox.setSelectedIndex(0);
			roleLabel.setLabelFor(_roleComboBox);
			applyPanel.add(roleLabel);
			applyPanel.add(_roleComboBox);

			pane.add(applyPanel);

			// Ausgabeoptionen
			_outputOptions = new OutputOptionsPanel();
			_outputOptions.setDetailLevel(OutputOptionsPanel.UPDATING);
			pane.add(_outputOptions);

			// untere Buttonleiste
			_buttonBar = new ButtonBar(this);     // brauche noch �bergabeparameter
			_dialog.getRootPane().setDefaultButton(_buttonBar.getAcceptButton());
			pane.add(_buttonBar);
		}

		/** Durch diese Methode wird der Dialog angezeigt. */
		private void showDialog() {
			_dialog.setLocation(50, 50);
			_dialog.pack();
			_dialog.setVisible(true);
		}

		/**
		 * Gibt den Zyklus des Datengenerators zur�ck. Der Zyklus gibt an, in welchen Abst�nden Daten generiert werden.
		 *
		 * @return der Zyklus des Datengenerators
		 *
		 * @see #setCycle(String)
		 */
		private String getCycle() {
			Integer value = (Integer)_cycleSpinner.getModel().getValue();
			int index = _cycleComboBox.getSelectedIndex();
			String unit = "";
			switch(index) {
				case 0:
					unit = "t";     // Tage
					break;
				case 1:
					unit = "h";     // Stunden
					break;
				case 2:
					unit = "m";     // Minuten
					break;
				case 3:
					unit = "s";     // Sekunden
					break;
				case 4:
					unit = "ms";    // Millisekunden
					break;
			}
			return value.toString() + unit;
		}

		/**
		 * Mit dieser Methode kann der Zyklus des Datengenerators gesetzt werden.
		 *
		 * @param cycle der Zyklus des Datengenerators
		 *
		 * @see #getCycle()
		 */
		private void setCycle(final String cycle) {
			String prefix = "60";
			String suffix = "Sekunden";
			if(cycle.endsWith("ms")) {
				prefix = cycle.substring(0, cycle.length() - 2);
				suffix = "Millisekunden";
			}
			else {
				int length = cycle.length();
				prefix = cycle.substring(0, length - 1);
				suffix = cycle.substring(length - 1, length);
				if(suffix.equals("t")) {
					suffix = "Tage";
				}
				else if(suffix.equals("h")) {
					suffix = "Stunden";
				}
				else if(suffix.equals("m")) {
					suffix = "Minuten";
				}
				else if(suffix.equals("s")) {
					suffix = "Sekunden";
				}
			}
			_cycleSpinner.getModel().setValue(Integer.valueOf(prefix));
			_cycleComboBox.setSelectedItem(suffix);
		}

		/**
		 * Gibt zur�ck, wieviele Datens�tze erzeugt werden sollen.
		 *
		 * @return Anzahl zu erzeugender Datens�tze
		 */
		private String getLimitedDatasets() {
			if(_areDatasetsLimited) {
				return ((Integer)_limitedDatasetsSpinner.getModel().getValue()).toString();
			}
			else {
				return null;
			}
		}

		/**
		 * Mit dieser Methode kann die Anzahl zu erzeugender Datens�tze gesetzt werden.
		 *
		 * @param number Anzahl zu erzeugender Datens�tze
		 */
		private void setLimitedDatasets(final int number) {
			_limitedDatasetsCheckBox.setSelected(true);
			_limitedDatasetsSpinner.getModel().setValue(new Integer(number));
		}

		/**
		 * Gibt die Spreizung der Datens�tze zur�ck.
		 *
		 * @return Spreizung der Datens�tze
		 */
		private String getSpreading() {
			int value = _spreadSlider.getValue();
			return Integer.toString(value);
		}

		/**
		 * Mit dieser Methode kann die Spreizung zu erzeugender Datens�tze festgelegt werden.
		 *
		 * @param spreading Spreizung der Datens�tze
		 */
		private void setSpreading(final int spreading) {
			_spreadSlider.setValue(spreading);
		}

		/**
		 * Gibt die ausgew�hlte Rolle f�r den Datengenerator zur�ck.
		 *
		 * @return die Rolle
		 */
		private String getRole() {
			String item = (String)_roleComboBox.getSelectedItem();
			if(item.equals(_roleUnit[0])) {
				return "quelle";
			}
			else {
				return "sender";
			}
		}

		/**
		 * Setzt die Rolle f�r den Datengenerator.
		 *
		 * @param role die Rolle des Datengenerators
		 */
		private void setRole(final String role) {
			int index = 0;
			if(role.equals("quelle")) {
				index = 0;
			}
			else if(role.equals("sender")) {
				index = 1;
			}
			_roleComboBox.setSelectedIndex(index);
		}

		/**
		 * Gibt zur�ck, wie detailliert die Daten ausgegeben werden sollen.
		 *
		 * @return Detaillierungsgrad
		 */
		private String getDetailLevel() {
			return _outputOptions.getDetailLevel();
		}

		/**
		 * Mit dieser Methode kann der Detaillierungsgrad gesetzt werden.
		 *
		 * @param level der Detaillierungsgrad
		 */
		private void setDetailLevel(final String level) {
			_outputOptions.setDetailLevel(level);
		}

		/**
		 * Gibt den vollst�ndigen Pfad (incl. Dateinamen) zur�ck, welcher f�r die Ausgabe vorgesehen ist.
		 *
		 * @return Pfad incl. Dateiname f�r die Ausgabe
		 */
		private String getFileName() {
			// wenn hier nichts drin steht, darf der "OK"-Button nicht enabled sein
			return _outputOptions.getFileName();
		}

		/**
		 * �bergibt den Dateinamen an die {@link OutputOptionsPanel Ausgabeoptionen}.
		 *
		 * @param fileName Pfad incl. Dateiname f�r die Ausgabe
		 */
		private void setFileName(final String fileName) {
			_outputOptions.setFileName(fileName);
		}

		/**
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name der Einstellungsdaten
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = DatGenModule.class;
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
			List argumentList = getArgumentList();
			for(Iterator iterator = argumentList.iterator(); iterator.hasNext();) {
				String str = (String)iterator.next();
				int index = str.indexOf("=");
				String key = str.substring(1, index);
				String value = str.substring(index + 1, str.length());
				keyValueList.add(new KeyValueObject(key, value));
			}
			keyValueList.add(new KeyValueObject("ausgabe", getDetailLevel()));
			keyValueList.add(new KeyValueObject("rolle", getRole()));
			keyValueList.add(new KeyValueObject("spreizung", getSpreading()));
			keyValueList.add(new KeyValueObject("zyklus", getCycle()));
			String limitedDatasets = getLimitedDatasets();
			if(limitedDatasets != null) {
				keyValueList.add(new KeyValueObject("anzahl", limitedDatasets));
			}
			String fileName = getFileName();
			if(fileName != null) {
				keyValueList.add(new KeyValueObject("datei", fileName));
			}

			return keyValueList;
		}

		/**
		 * Sammelt alle Einstellungen in einer Liste und gibt sie zur�ck. Die Liste enth�lt die Parameter f�r den Datengenerator.
		 *
		 * @param settingsData Einstellungsdaten
		 *
		 * @return Liste aller Einstellungen dieses Dialogs
		 */
		private List<String> getParameterList(final SettingsData settingsData) {
			List<String> parameterList = new LinkedList<String>();
			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				parameterList.add("-" + keyValueObject.getKey() + "=" + keyValueObject.getValue());
//				System.out.println("-" + keyValueObject.getKey() + "=" + keyValueObject.getValue());
			}
			// Objekte zusammenstellen
			String result = "-objekte=";
			List objects = settingsData.getObjects();
			for(Iterator iterator = objects.iterator(); iterator.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator.next();
				result += systemObject.getId() + ",";
			}
			// befindet sich an letzter Stelle ein Komma -> wegnehmen
			result = result.substring(0, result.length() - 1);
			parameterList.add(result);

			// DataDescription zusammenstellen
			int simulationVariant = settingsData.getSimulationVariant();
			String dataString = "-daten=" + settingsData.getAttributeGroup().getPid() + ":" + settingsData.getAspect().getPid();
			if(simulationVariant != -1) {
				dataString += ":" + String.valueOf(simulationVariant);
			}
//			System.out.println("dataString = " + dataString);
			parameterList.add(dataString);

			return parameterList;
		}

		/**
		 * Durch bet�tigen des "OK"-Buttons wird der Datengenerator mit den eingestellten Parametern in einem neuen Fenster gestartet und dieser Dialog wird
		 * geschlossen.
		 */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startConsole(settingsData);
			doCancel();
			saveSettings(settingsData);
		}

		/** Durch bet�tigen des "Abbrechen"-Buttons wird der Dialog geschlossen. */
		public void doCancel() {
			_dialog.setVisible(false);
			_dialog.dispose();
		}

		/**
		 * Durch bet�tigen des "Speichern unter ..."-Buttons werden die Einstellungen gespeichert.
		 *
		 * @param title Titel der Einstellungen
		 */
		public void doSave(String title) {
			SettingsData settingsData = getSettings(title);
			saveSettings(settingsData);
		}
	}
}
