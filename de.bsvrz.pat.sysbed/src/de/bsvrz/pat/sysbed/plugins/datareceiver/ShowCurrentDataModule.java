/*
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

package de.bsvrz.pat.sysbed.plugins.datareceiver;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.dataEditor.DatasetEditorFrame;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul, um aktuelle Daten anzuzeigen. Anmeldeoptionen, wie die Rolle und die Anmeldeart können mit Hilfe eines Dialogs angegeben werden. Die
 * ausgewählte Datenidentifikation wird mit dem Dateneditor angezeigt. Jedoch ist eine Veränderung der Daten nicht möglich.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ShowCurrentDataModule extends ExternalModuleAdapter {

	/** speichert den aktuellen Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog, wo Parameter für die Datenanzeige eingestellt werden können */
	private static ShowCurrentDataDialog _dialog;


	/* ############# Methoden ############# */
	/**
	 * Gibt den Namen des Moduls zurück.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Aktuelle Daten anzeigen";
	}

	/**
	 * Gibt den Text des Buttons zurück.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Aktuelle Daten anzeigen";
	}

	/**
	 * Gibt den aktuellen Text des Tooltips zurück.
	 *
	 * @return Text des Tooltips
	 */
	public String getTooltipText() {
		return _tooltipText;
	}

	/**
	 * Diese Methode erhält die ausgewählte Datenidentifikation und startet den {@link ShowCurrentDataDialog Dialog}, damit die Anmeldeoptionen angegeben werden
	 * können.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new ShowCurrentDataDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen und startet direkt den Dateneditor, um die aktuellen Daten anzuzeigen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new ShowCurrentDataDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen des {@link ShowCurrentDataDialog Dialogs} und startet diesen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new ShowCurrentDataDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Gibt zurück, ob die Bedingungen für dieses Modul erfüllt sind.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt zurück, ob die Bedingungen für diese Modul erfüllt sind
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		if(!super.isPreselectionValid(settingsData) || settingsData.getObjects().size() != 1) {
			_tooltipText = "Genau eine Attributgruppe, ein Aspekt und ein Objekt müssen ausgewählt sein.";
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


	/* ############### Klasse ShowCurrentDataDialog ############## */
	/**
	 * Stellt einen Dialog zur Verfügung, mit dem Anmeldeoptionen, wie die Rolle und die Anmeldeart, festgelegt werden können. Diese Einstellungen können
	 * gespeichert werden. Durch betätigen des "OK"-Buttons wird der aktuelle Datensatz angezeigt.
	 */
	private class ShowCurrentDataDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert eine Instanz der Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt an, welche Rollen zur Verfügung stehen */
		private final String[] _roleUnit = {"Empfänger", "Senke"};

		/** gibt an, welche Anmeldearten zur Verfügung stehen */
		private final String[] _applyModeEntries = {"Online", "Nur geänderte Datensätze", "Auch nachgelieferte Datensätze"};

		/** hier kann die Rolle ausgewählt werden */
		private JComboBox _roleComboBox;

		/** hier kann die Anmeldeart ausgewählt werden */
		private JComboBox _applyModeCombo;


		/* ############### Methoden ############### */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public ShowCurrentDataDialog() {
		}

		/**
		 * Mit dieser Methode können die Datenidentifikationsdaten übergeben werden. Der Dialog wird mit der Default-Einstellung dargestellt.
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
		 * @param data die Einstellungsdaten
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
				if(key.equals("rolle")) {
					setRole(keyValueObject.getValue());
				}
				else if(key.equals("mode")) {
					setApplyMode(keyValueObject.getValue());
				}
			}
			showDialog();
		}

		/**
		 * Startet die Anzeige des aktuellen Datensatzes anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			AttributeGroup atg = settingsData.getAttributeGroup();
			Aspect asp = settingsData.getAspect();
			SystemObject systemObject = (SystemObject)settingsData.getObjects().get(0);
			int simulationVariant = settingsData.getSimulationVariant();

			DatasetEditorFrame datasetEditorFrame = new DatasetEditorFrame(
					getConnection(), "Aktuelle Daten anzeigen", atg, asp, systemObject, simulationVariant
			);
			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				if(key.equals("rolle")) {
					String value = keyValueObject.getValue();
					if(value.equals("empfaenger")) {
						datasetEditorFrame.setReceiverRole(ReceiverRole.receiver());
					}
					else if(value.equals("senke")) {
						datasetEditorFrame.setReceiverRole(ReceiverRole.drain());
					}
				}
				else if(key.equals("mode")) {
					String value = keyValueObject.getValue();
					if(value.equals("online")) {
						datasetEditorFrame.setReceiveOptions(ReceiveOptions.normal());
					}
					else if(value.equals("delta")) {
						datasetEditorFrame.setReceiveOptions(ReceiveOptions.delta());
					}
					else if(value.equals("nachgeliefert")) {
						datasetEditorFrame.setReceiveOptions(ReceiveOptions.delayed());
					}
				}
			}
			try {
				datasetEditorFrame.startShowCurrentData();
			}
			catch(Exception ex) {
				JOptionPane.showMessageDialog(_dialog, ex.getMessage(), "Fehler bei der Anmeldung", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation und die Anmeldeoption, bestehend aus der Rolle und der Anmeldeart. */
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
			_dataIdentificationChoice.setNumberOfSelectedObjects(1, 1);
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
			applyModeLabel.setLabelFor(_applyModeCombo);
			applyPanel.add(applyModeLabel);
			applyPanel.add(_applyModeCombo);

			pane.add(applyPanel);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);
			_dialog.getRootPane().setDefaultButton(buttonBar.getAcceptButton());
			pane.add(buttonBar);
		}

		/** Durch diese Methode wird der Dialog angezeigt. */
		private void showDialog() {
			_dialog.setLocation(90, 90);
			_dialog.pack();
			_dialog.setVisible(true);
		}

		/**
		 * Gibt die ausgewählte Rolle für die Datenanzeige zurück.
		 *
		 * @return die ausgewählte Rolle
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
		 * Setzt die Rolle für die Datenanzeige.
		 *
		 * @param role die Rolle
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
		 * Gibt die Anmeldeart zurück.
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
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name für die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = ShowCurrentDataModule.class;
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
			keyValueList.add(new KeyValueObject("rolle", getRole()));
			keyValueList.add(new KeyValueObject("mode", getApplyMode()));

			return keyValueList;
		}

		/**
		 * Durch betätigen des "OK"-Buttons werden die ausgewählten Objekte mit den eingestellten Parametern beim Datenverteiler angemeldet und angezeigt. Dieser
		 * Dialog wird geschlossen.
		 */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startSettings(settingsData);
			doCancel();
			saveSettings(settingsData);
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
}
