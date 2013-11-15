/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.plugins.datasender;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.dataEditor.DatasetEditorFrame;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul, um den Dateneditor starten zu können. Der Dateneditor ermöglicht das Erstellen und Senden eines neuen Datensatzes. Nach Auswahl der
 * Anmeldeoption wird ein Fenster passend zu der ausgewählten Datenidentifikation mit einem leeren Datensatz angezeigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class SendCurrentDataModule extends ExternalModuleAdapter {

	/** speichert den aktuellen Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog um Daten erstellen und senden zu können */
	private static SendCurrentDataDialog _dialog;


	/* ################### Methoden ############# */
	/**
	 * Gibt den Namen des Moduls zurück.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Aktuelle Daten senden";
	}

	/**
	 * Gibt den Text des Buttons zurück.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Aktuelle Daten senden";
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
	 * Diese Methode erhält die ausgewählte Datenidentifikation und startet den {@link SendCurrentDataDialog Dialog}, damit die Anmeldeoption angegeben werden
	 * kann.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new SendCurrentDataDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen und startet direkt das Fenster, damit aktuelle Daten erstellt und gesendet werden können.
	 *
	 * @param settingsData Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new SendCurrentDataDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen des {@link SendCurrentDataDialog Dialogs} und startet diesen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new SendCurrentDataDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Gibt zurück, ob die Bedingungen erfüllt sind, um einen Datensatz erstellen zu können.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt zurück, ob die Bedingungen erfüllt sind, um einen Datensatz erstellen zu können
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


	/* ############# Klasse SendCurrentDataDialog ############## */
	/**
	 * Stellt einen Dialog zur Verfügung, mit dem die Anmeldeoption festgelegt wird. Diese Einstellungen können gespeichert werden. Durch betätigen des
	 * "OK"-Buttons wird der {@link de.bsvrz.pat.sysbed.dataEditor.DatasetEditorFrame Dateneditor} gestartet.
	 */
	private class SendCurrentDataDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert eine Instanz der Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt an, welche Rollen zur Verfügung stehen */
		private final String[] _roleUnit = {"Quelle", "Sender"};

		/** hier kann die Rolle ausgewählt werden */
		private JComboBox _roleComboBox;


		/* ############### Methoden #################### */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public SendCurrentDataDialog() {
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
			}
			showDialog();
		}

		/**
		 * Startet den Dateneditor anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			AttributeGroup atg = settingsData.getAttributeGroup();
			Aspect asp = settingsData.getAspect();
			SystemObject systemObject = (SystemObject)settingsData.getObjects().get(0);
			int simulationVariant = settingsData.getSimulationVariant();

			DatasetEditorFrame datasetEditorFrame = new DatasetEditorFrame(getConnection(), "Aktuelle Daten senden", atg, asp, systemObject, simulationVariant);
			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				if(key.equals("rolle")) {
					String value = keyValueObject.getValue();
					if(value.equals("quelle")) {
						datasetEditorFrame.setSenderRole(SenderRole.source());
					}
					else if(value.equals("sender")) {
						datasetEditorFrame.setSenderRole(SenderRole.sender());
					}
				}
			}
			try {
				datasetEditorFrame.startSendCurrentData();
			}
			catch(Exception ex) {
				JOptionPane.showMessageDialog(_dialog, ex.getMessage(), "Fehler bei der Anmeldung", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation und die Anmeldeoption, bestehend aus der Rolle. */
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

			pane.add(applyPanel);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);
			_dialog.getRootPane().setDefaultButton(buttonBar.getAcceptButton());
			pane.add(buttonBar);
		}

		/** Durch diese Methode wird der Dialog angezeigt. */
		private void showDialog() {
			_dialog.setLocation(70, 70);
			_dialog.pack();
			_dialog.setVisible(true);
		}

		/**
		 * Gibt die ausgewählte Rolle für den Dateneditor zurück.
		 *
		 * @return die ausgewählte Rolle
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
		 * Setzt die Rolle für den Dateneditor.
		 *
		 * @param role die Rolle für den Dateneditor
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
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name für die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = SendCurrentDataModule.class;
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

			return keyValueList;
		}

		/** Durch betätigen des "OK"-Buttons wird der Dateneditor gestartet und dieser Dialog wird geschlossen. Die Einstellungen werden gespeichert. */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startSettings(settingsData);
			doCancel();
			saveSettings(settingsData);
		}

		/** diese Methode schließt den Dialog */
		public void doCancel() {
			_dialog.setVisible(false);
			_dialog.dispose();
		}

		/**
		 * diese Methode speichert die Einstellungen
		 *
		 * @param title Titel der Einstellung
		 */
		public void doSave(String title) {
			SettingsData settingsData = getSettings(title);
			saveSettings(settingsData);
		}
	}
}
