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

package de.bsvrz.pat.sysbed.plugins.onlprot;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.OutputOptionsPanel;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.sys.funclib.consoleProcessFrame.ConsoleProcessFrame;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul f�r den Online-Protokollierer. Ein Dialog wird zur Verf�gung gestellt, damit die ben�tigten Parameter f�r den Online-Protokollierer
 * eingestellt werden k�nnen. Die anzuzeigenden Daten werden in einer Konsole angezeigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5053 $
 * @see de.bsvrz.pat.onlprot.protocoller.main.Receiver
 */
public class OnlineProtocolModule extends ExternalModuleAdapter {

	/** speichert den Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog des Online-Protokollierers */
	private static OnlineProtocolDialog _dialog;


	/* ############## Methoden ############# */
	/**
	 * Gibt den Namen des Moduls zur�ck.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Onlineprotokollierer";
	}

	/**
	 * Gibt den Text des Buttons zur�ck.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Onlineprotokollierer starten";
	}

	/**
	 * Gibt den Text des Tooltips zur�ck.
	 *
	 * @return Text des Tooltips
	 */
	public String getTooltipText() {
		return _tooltipText;
	}

	/**
	 * Diese Methode erh�lt die ausgew�hlte Datenidentifikation und startet den {@link OnlineProtocolDialog Dialog} zur Auswahl der Einstellungen des
	 * Online-Protokollierers.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new OnlineProtocolDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r den Online-Protokollierer und startet diesen ohne den Dialog anzuzeigen.
	 *
	 * @param settingsData die Einstellungen f�r den Online-Protokollierer
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new OnlineProtocolDialog();
		_dialog.startConsole(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r den Online-Protokollierer und startet den {@link OnlineProtocolDialog Dialog} und f�llt ihn entsprechend der
	 * Einstellungen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new OnlineProtocolDialog();
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


	/* ################ Klasse OnlineProtocolDialog ############ */
	/**
	 * Stellt einen Dialog dar, womit Parameter f�r den Online-Protokollierer eingestellt werden k�nnen. Diese Parameter k�nnen gespeichert werden. Durch bet�tigen
	 * des "OK"-Buttons werden die Einstellungen �bernommen, der Online-Protokollierer gestartet und der Dialog geschlossen.
	 */
	private class OnlineProtocolDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert die Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt an, welche Rollen zur Verf�gung stehen */
		private final String[] _roleUnit = {"Empf�nger", "Senke"};

		/** gibt an, welche Anmeldearten zur Verf�gung stehen */
		private final String[] _applyModeEntries = {"Online", "Nur ge�nderte Datens�tze", "Auch nachgelieferte Datens�tze"};

		/** hiermit kann die Rolle ausgew�hlt werden */
		private JComboBox _roleComboBox;

		/** hiermit kann die Anmeldeart ausgew�hlt werden */
		private JComboBox _applyModeCombo;

		/** speichert ein Objekt der Ausgabeoptionen */
		private OutputOptionsPanel _outputOptions;


		/* #################### Methoden ################# */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public OnlineProtocolDialog() {
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
				else if(key.equals("option")) {
					setApplyMode(keyValueObject.getValue());
				}
				else if(key.equals("datei")) {
					setFileName(keyValueObject.getValue());
				}
			}
			showDialog();
		}

		/**
		 * Diese Methode startet den Online-Protokollierer mit den Einstellungsdaten. Die Ausgabe erfolgt in einer Konsole.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startConsole(final SettingsData settingsData) {
			List<String> parameterList = getParameterList(settingsData);
			String[] arguments = (String[])parameterList.toArray(new String[0]);
			try {
				ConsoleProcessFrame.createJavaProcessFrame(getModuleName(), "de.bsvrz.pat.onlprot.protocoller.main.Receiver", arguments, null, null);
			}
			catch(IOException e) {
				JOptionPane.showMessageDialog(_dialog, e, "Fehler beim Start des Online-Protokollierers", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation, die Anmeldeoptionen, bestehend aus der Rolle und der Anmeldeart und die Ausgabeoptionen. */
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

			// Anmeldeoptionen
			JPanel applyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			applyPanel.setBorder(BorderFactory.createTitledBorder("Anmeldeoptionen"));
			JLabel roleLabel = new JLabel("Rolle: ");
			_roleComboBox = new JComboBox(_roleUnit);
			_roleComboBox.setSelectedIndex(0);
			roleLabel.setLabelFor(_roleComboBox);
			applyPanel.add(roleLabel);
			applyPanel.add(_roleComboBox);
			applyPanel.add(Box.createHorizontalStrut(10));

			JLabel applyModeLabel = new JLabel("Anmeldeart: ");
			_applyModeCombo = new JComboBox(_applyModeEntries);
			_applyModeCombo.setEditable(false);
			_applyModeCombo.setSelectedIndex(0);
			applyModeLabel.setLabelFor(_applyModeCombo);
			applyPanel.add(applyModeLabel);
			applyPanel.add(_applyModeCombo);

			pane.add(applyPanel);

			// Ausgabeoptionen
			_outputOptions = new OutputOptionsPanel();
			_outputOptions.setDetailLevel(OutputOptionsPanel.DATA);
			pane.add(_outputOptions);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);
			_dialog.getRootPane().setDefaultButton(buttonBar.getAcceptButton());
			pane.add(buttonBar);
		}

		/** Durch diese Methode wird der Dialog angezeigt. */
		private void showDialog() {
			_dialog.setLocation(50, 50);
			_dialog.pack();
			_dialog.setVisible(true);
		}

		/**
		 * Gibt die ausgew�hlte Rolle f�r den Datengenerator zur�ck.
		 *
		 * @return die Rolle
		 */
		private String getRole() {
			String item = (String)_roleComboBox.getSelectedItem();
			if(item.equals(_roleUnit[0])) {
				return "empfaenger";
			}
			else {
				return "senke";
			}
		}

		/**
		 * Setzt die Rolle f�r den Online-Protokollierer.
		 *
		 * @param role die Rolle des Online-Protokollierers
		 */
		private void setRole(final String role) {
			int index = 0;
			if(role.equals("empfaenger")) {
				index = 0;
			}
			else if(role.equals("senke")) {
				index = 1;
			}
			_roleComboBox.setSelectedIndex(index);
		}

		/**
		 * Gibt die Anmeldeart zur�ck.
		 *
		 * @return die Anmeldeart
		 */
		private String getApplyMode() {
			int index = _applyModeCombo.getSelectedIndex();
			String mode = "";
			switch(index) {
				case 0:
					mode = "online";
					break;
				case 1:
					mode = "delta";
					break;
				case 2:
					mode = "nachgeliefert";
					break;
			}
			return mode;
		}

		/**
		 * Mit dieser Methode kann die Anmeldeart gesetzt werden.
		 *
		 * @param mode Anmeldeart
		 */
		private void setApplyMode(final String mode) {
			int index = 0;
			if(mode.equals("online")) {
				index = 0;
			}
			else if(mode.equals("delta")) {
				index = 1;
			}
			else if(mode.equals("nachgeliefert")) {
				index = 2;
			}
			_applyModeCombo.setSelectedIndex(index);
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
			return _outputOptions.getFileName();
		}

		/**
		 * �bergibt den Dateinamen an die {@link de.bsvrz.pat.sysbed.plugins.api.OutputOptionsPanel Ausgabeoptionen}.
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
			Class moduleClass = OnlineProtocolModule.class;
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
			keyValueList.add(new KeyValueObject("option", getApplyMode()));
			String fileName = getFileName();
			if(fileName != null) {
				keyValueList.add(new KeyValueObject("datei", fileName));
			}
			return keyValueList;
		}

		/**
		 * Sammelt alle Einstellungen in einer Liste und gibt sie zur�ck. Die Liste enth�lt die Parameter f�r den Online-Protokollierer.
		 *
		 * @param settingsData die Einstellungsdaten des Dialogs
		 *
		 * @return Liste aller Einstellungen f�r die Konsole
		 */
		private List<String> getParameterList(final SettingsData settingsData) {
			List<String> parameterList = new LinkedList<String>();
			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				parameterList.add("-" + keyValueObject.getKey() + "=" + keyValueObject.getValue());
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
			parameterList.add(dataString);

			return parameterList;
		}

		/**
		 * Durch bet�tigen des "OK"-Buttons wird der Onlineprotokollierer mit den eingestellten Parametern in einem neuen Fenster gestartet und dieser Dialog wird
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
