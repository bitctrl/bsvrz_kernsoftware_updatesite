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

package de.bsvrz.pat.sysbed.plugins.onlinetable;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.dataview.DataViewFrame;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.DataModel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul f�r die Onlinetabelle. Dieses Modul stellt f�r die ausgew�hlte Datenidentifikation alle aktuellen Daten vom Datenverteiler in
 * Tabellendarstellung dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class OnlineTableModule extends ExternalModuleAdapter {

	/** speichert den Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog der Onlinetabelle */
	private static OnlineTableDialog _dialog;

	/* ############ Methoden ############ */
	/**
	 * Gibt den Namen des Moduls zur�ck.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Onlinetabelle";
	}

	/**
	 * Gibt den Text des Buttons zur�ck.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Onlinetabelle anzeigen";
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
	 * Diese Methode erh�lt die ausgew�hlte Datenidentifikation und stellt die Onlinetabelle dar.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new OnlineTableDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r die Anzeige der OnlineTabelle.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new OnlineTableDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r die Onlinetabelle und startet den {@link OnlineTableDialog Dialog}. Dieser wird mit den Einstellungsdaten
	 * gef�llt.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new OnlineTableDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Gibt an, ob die Vorauswahl den Anforderungen der Onlinetabelle gen�gen.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 *
	 * @return gibt an, ob die Vorauswahl den Anforderungen der Onlinetabelle gen�gen
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


	/* ############### Klasse OnlineTableDialog ############## */
	/**
	 * Stellt einen Dialog zur Verf�gung, mit dem Einstellungen f�r die Onlinetabelle gemacht werden k�nnen. Diese Einstellungen k�nnen gespeichert werden. Durch
	 * bet�tigen des "OK"-Buttons wird die Onlinetabelle gestartet.
	 */
	private class OnlineTableDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert eine Instanz der Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt an, welche Rollen zur Verf�gung stehen */
		private final String[] _roleUnit = {"Empf�nger", "Senke"};

		/** gibt an, welche Anmeldearten zur Verf�gung stehen */
		private final String[] _applyModeEntries = {"Online", "Nur ge�nderte Datens�tze", "Auch nachgelieferte Datens�tze"};

		/** gibt an, welche Darstellungsoptionen zur Verf�gung stehen */
		private final String[] _displayOptions = {"Aktuelle Daten unten anh�ngen", "Aktuelle Daten oben einf�gen", "Nur aktuellste Daten anzeigen"};

		/** hier kann die Rolle ausgew�hlt werden */
		private JComboBox _roleComboBox;

		/** hier kann die Anmeldeart ausgew�hlt werden */
		private JComboBox _applyModeCombo;

		/** hier kann die Darstellungsoption ausgew�hlt werden */
		private JComboBox _displayOptionsComboBox;


		/* ############### Methoden ################### */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public OnlineTableDialog() {
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
				else if(key.equals("option")) {
					setApplyMode(keyValueObject.getValue());
				}
				else if(key.equals("display")) {
					setDisplayOptions(keyValueObject.getValue());
				}
			}
			showDialog();
		}

		/**
		 * Startet die Onlinetabelle anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			List<SystemObject> objects = settingsData.getObjects();
			AttributeGroup atg = settingsData.getAttributeGroup();
			Aspect asp = settingsData.getAspect();
			int simulationVariant = settingsData.getSimulationVariant();

			DataViewFrame dataViewFrame = new DataViewFrame(getConnection(), objects, atg, asp, simulationVariant);
			List keyValueList = settingsData.getKeyValueList();
			int displayOptions = 0;
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				if(key.equals("rolle")) {
					String value = keyValueObject.getValue();
					if(value.equals("empfaenger")) {
						dataViewFrame.setReceiverRole(ReceiverRole.receiver());
					}
					else if(value.equals("senke")) {
						dataViewFrame.setReceiverRole(ReceiverRole.drain());
					}
				}
				else if(key.equals("option")) {
					String value = keyValueObject.getValue();
					if(value.equals("online")) {
						dataViewFrame.setReceiveOptions(ReceiveOptions.normal());
					}
					else if(value.equals("delta")) {
						dataViewFrame.setReceiveOptions(ReceiveOptions.delta());
					}
					else if(value.equals("nachgeliefert")) {
						dataViewFrame.setReceiveOptions(ReceiveOptions.delayed());
					}
				}
				else if(key.equals("display")) {
					displayOptions = Integer.parseInt(keyValueObject.getValue());
				}
			}
			try {
				dataViewFrame.showOnlineData(displayOptions);
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

			// Darstellungsoptionen
			JPanel displayOptionsPanel = new JPanel();
			displayOptionsPanel.setLayout(new BoxLayout(displayOptionsPanel, BoxLayout.X_AXIS));
			displayOptionsPanel.setBorder(BorderFactory.createTitledBorder("Darstellungsoptionen"));
			JLabel displayOptionsLabel = new JLabel("Darstellung: ");
			_displayOptionsComboBox = new JComboBox(_displayOptions);
			_displayOptionsComboBox.setSelectedIndex(0);	// Default-Einstellung
			displayOptionsLabel.setLabelFor(_displayOptionsComboBox);
			displayOptionsPanel.add(displayOptionsLabel);
			displayOptionsPanel.add(Box.createHorizontalStrut(5));
			displayOptionsPanel.add(_displayOptionsComboBox);

			pane.add(applyPanel);
			pane.add(displayOptionsPanel);

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
		 * Gibt die ausgew�hlte Rolle f�r die Onlinetabelle zur�ck.
		 *
		 * @return die ausgew�hlte Rolle
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
		 * Setzt die Rolle f�r die Onlinetabelle.
		 *
		 * @param role die Rolle f�r die Onlinetabelle
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
		 * Gibt die ausgew�hlte Darstellungsoption zur�ck.
		 *
		 * @return der Index der ausgew�hlten Darstellungsoption
		 */
		private String getDisplayOptions() {
			return String.valueOf(_displayOptionsComboBox.getSelectedIndex());
		}

		/**
		 * Setzt die Darstellungsoption.
		 *
		 * @param index der Index der Darstellungsoption
		 */
		private void setDisplayOptions(final String index) {
			_displayOptionsComboBox.setSelectedIndex(Integer.parseInt(index));
		}

		/**
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name f�r die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = OnlineTableModule.class;
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
			keyValueList.add(new KeyValueObject("option", getApplyMode()));
			keyValueList.add(new KeyValueObject("display", getDisplayOptions()));

			return keyValueList;
		}

		/** Durch bet�tigen des "OK"-Buttons wird die Onlinetabelle gestartet und dieser Dialog wird geschlossen. Die Parameter werden gespeichert. */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startSettings(settingsData);
			doCancel();
			saveSettings(settingsData);
		}

		/** diese Methode schlie�t den Dialog */
		public void doCancel() {
			_dialog.setVisible(false);
			_dialog.dispose();
		}

		/**
		 * diese Methode speichert die Parameter
		 *
		 * @param title Titel dieser Konfiguration
		 */
		public void doSave(String title) {
			SettingsData settingsData = getSettings(title);
			saveSettings(settingsData);
		}
	}
}
