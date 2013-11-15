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

package de.bsvrz.pat.sysbed.plugins.configdata;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.dataview.DataTableObject;
import de.bsvrz.pat.sysbed.dataview.DataViewFrame;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul f�r die Anzeige von Konfigurationsdaten. Die Daten werden als Tabelle dargestellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8209 $
 */
public class ConfigurationDataModule extends ExternalModuleAdapter {

	/** speichert den Dialog der Konfigurationsdaten */
	private static ConfigurationDataDialog _dialog;

	/** der Tooltip-Text */
	private String _tooltipText;


	/* ############## Methoden ################ */
	/**
	 * Gibt den Namen des Moduls zur�ck.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Konfigurationsdaten";
	}

	/**
	 * Gibt den Text des Buttons zur�ck.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Konfigurationsdaten anzeigen";
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
	 * Diese Methode erh�lt die ausgew�hlte Datenidentifikation und stellt die Konfigurationsdaten in einer Tabelle dar.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new ConfigurationDataDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r die Anzeige der Konfigurationsdaten.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new ConfigurationDataDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erh�lt alle Einstellungen f�r die Konfigurationsdaten und startet den {@link ConfigurationDataDialog Dialog}. Dieser wird mit den
	 * Einstellungsdaten gef�llt.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new ConfigurationDataDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * �berpr�ft, ob die Voraussetzungen f�r das Modul gegeben sind.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 *
	 * @return gibt zur�ck, ob die Voraussetzungen f�r das Modul gegeben sind
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		if(!super.isPreselectionValid(settingsData)) {
			_tooltipText = "Eine konfigurierende Attributgruppe, ein Aspekt und mindestens ein Objekt m�ssen ausgew�hlt sein.";
			return false;
		}

		// ATGV wird ber�cksichtigt
		final AttributeGroupUsage atgUsage = settingsData.getAttributeGroup().getAttributeGroupUsage(settingsData.getAspect());
		if(atgUsage == null || !atgUsage.isConfigurating()) {
			_tooltipText = "Keine konfigurierende Attributgruppe ausgew�hlt.";
			return false;
		}
		_tooltipText = "Auswahl �bernehmen";
		return true;
	}


	/* ################## Klasse ConfigurationDataDialog ############### */
	/**
	 * Stellt einen Dialog zur Verf�gung, mit dem Einstellungen f�r die Konfigurationsdaten gemacht werden k�nnen. Diese Einstellungen k�nnen gespeichert werden.
	 * Durch bet�tigen des "OK"-Button werden die Konfigurationsdaten in einer Tabelle dargestellt.
	 */
	private class ConfigurationDataDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert die Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/* ################## Methoden ############# */
		/** Standardkonstruktor. Erstellt ein Objekt der Klasse. */
		public ConfigurationDataDialog() {
		}

		/**
		 * Mit dieser Methode k�nnen die Datenidentifikationsdaten �bergeben werden.
		 *
		 * @param data enth�lt die ausgew�hlte Datenidentifikation
		 */
		public void setDataIdentification(final SettingsData data) {
			Class moduleClass = ConfigurationDataModule.class;

			SettingsData settingsData = new SettingsData(
					getModuleName(), moduleClass, data.getObjectTypes(), data.getAttributeGroup(), data.getAspect(), data.getObjects()
			);
			settingsData.setSimulationVariant(data.getSimulationVariant());
			settingsData.setTreePath(data.getTreePath());
			startSettings(settingsData);
			saveSettings(settingsData);
		}

		/**
		 * Diese Methode zeigt den Dialog an und tr�gt die Einstellungsdaten in die entsprechenden Felder.
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
			showDialog();
		}

		/**
		 * Startet die Darstellung f�r die Konfigurationsdaten anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			final AttributeGroup atg = settingsData.getAttributeGroup();
			final Aspect asp = settingsData.getAspect();
			final List<SystemObject> objects = settingsData.getObjects();
			int simulationVariant = settingsData.getSimulationVariant();
			final List<DataTableObject> dataTableObjects = new LinkedList<DataTableObject>();
			for(Iterator iterator = objects.iterator(); iterator.hasNext();) {
				final SystemObject systemObject = (SystemObject)iterator.next();
				Data data;
				if(asp == null) {
					data = systemObject.getConfigurationData(atg);
				}
				else {
					data = systemObject.getConfigurationData(atg, asp);
				}
				final ResultData resultData = new ResultData(systemObject, new DataDescription(atg, asp), System.currentTimeMillis(), data);
				dataTableObjects.add(new DataTableObject(resultData));
			}

			final DataViewFrame dataViewFrame = new DataViewFrame(getConnection(), objects, atg, asp, simulationVariant);
			dataViewFrame.showConfigurationData(dataTableObjects);
		}

		/** Erstellt den Dialog mit der Datenidentifikation. */
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
			PreselectionListsFilter listFilter = new PreselectionListsFilter() {
				public List applyFilter(int whichList, List list) {
					final List<AttributeGroup> filteredList = new ArrayList<AttributeGroup>(list.size());
					if(whichList == PreselectionListsFilter.ATTRIBUTEGROUP_LIST) {
						for(Iterator iterator = list.iterator(); iterator.hasNext();) {
							final AttributeGroup attributeGroup = (AttributeGroup)iterator.next();
							// pr�fen, ob es hier eine ATGV gibt, die konfigurierend ist
							for(AttributeGroupUsage attributeGroupUsage : attributeGroup.getAttributeGroupUsages()) {
								if(attributeGroupUsage.isConfigurating()) {
									filteredList.add(attributeGroup);
									break;
								}
							}
						}
						return filteredList;
					}
					return list;
				}
			};
			_dataIdentificationChoice = new DataIdentificationChoice(listFilter, types);
			pane.add(_dataIdentificationChoice);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);     // brauche noch �bergabeparameter
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
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name f�r die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = ConfigurationDataModule.class;
			List<SystemObjectType> objectTypes = _dataIdentificationChoice.getObjectTypes();
			AttributeGroup atg = _dataIdentificationChoice.getAttributeGroup();
			Aspect asp = _dataIdentificationChoice.getAspect();
			List<SystemObject> objects = _dataIdentificationChoice.getObjects();

			SettingsData settingsData = new SettingsData(getModuleName(), moduleClass, objectTypes, atg, asp, objects);
			settingsData.setSimulationVariant(_dataIdentificationChoice.getSimulationVariant());
			settingsData.setTreePath(_dataIdentificationChoice.getTreePath());
			settingsData.setTitle(title);

			return settingsData;
		}

		/** Durch bet�tigen des "OK"-Buttons werden die Konfigurationsdaten angezeigt. Die ausgew�hlten Einstellungen werden gespeichert. */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startSettings(settingsData);
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
