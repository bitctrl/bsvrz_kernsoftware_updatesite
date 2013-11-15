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

package de.bsvrz.pat.sysbed.plugins.parameditor;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.preselection.lists.PreselectionListsFilter;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul für den {@link ParameterEditor Parametereditor}. Mit der ausgewählten Datenidentifikation wird der Parametereditor
 * gestartet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public class ParameterEditorModule extends ExternalModuleAdapter {

	/** speichert den Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog des Parametereditors */
	private static EditParameterDialog _dialog;


	/* ############# Methoden ############ */
	/**
	 * Gibt den Namen des Moduls zurück.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Parametereditor";
	}

	/**
	 * Gibt den Text des Buttons zurück.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Parameter editieren";
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
	 * Diese Methode erhält die ausgewählte Datenidentifikation und startet den Parametereditor.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new EditParameterDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für die Anzeige des Parametereditors.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new EditParameterDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für den Parametereditor und startet den {@link EditParameterDialog Dialog}. Dieser wird mit den Einstellungen
	 * initialisiert.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new EditParameterDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Gibt an, ob die Vorauswahl den Anforderungen des Parametereditors genügen.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt an, ob die Vorauswahl den Anforderungen des Parametereditors genügen
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		final AttributeGroup atg = settingsData.getAttributeGroup();
		final List<SystemObject> objects = settingsData.getObjects();

		if(atg != null && objects != null && settingsData.getObjects().size() == 1) {
			// prüfen, ob die Attributgruppe die richtigen Aspekte enthält
			Collection aspects = atg.getAspects();
			if(checkAspects(aspects)) {
				_tooltipText = "Auswahl übernehmen";
				return true;
			}
		}
		_tooltipText = "Genau eine Parameterattributgruppe und ein Objekt müssen ausgewählt sein.";
		return false;
	}

	/**
	 * Prüft, ob die für den Parametereditor benötigten Aspekte auch in der Liste der Aspekte vorkommen.
	 *
	 * @param aspects zu durchsuchende Liste von Aspekten
	 *
	 * @return gibt an, ob die benötigten Aspekte in der Liste vorhanden sind
	 */
	private boolean checkAspects(Collection aspects) {
		DataModel configuration = getConnection().getDataModel();
		Aspect asp01 = configuration.getAspect("asp.parameterSoll");
		Aspect asp02 = configuration.getAspect("asp.parameterVorgabe");
		if(aspects.contains(asp01) && aspects.contains(asp02)) {
			return true;
		}
		else {
			return false;
		}
	}


	/* ################# Klasse EditParameterDialog #################### */
	/**
	 * Stellt einen Dialog zur Verfügung, mit dem Einstellungen für den Parametereditor gemacht werden können. Diese Einstellungen können gespeichert werden. Durch
	 * betätigen des "OK"-Buttons wird der Parametereditor gestartet.
	 */
	private class EditParameterDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert eine Instanz der Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;


		/* ########################## Methoden ################# */
		/** Standardkonstruktor. Erstellt ein Objekt der Klasse. */
		public EditParameterDialog() {
		}

		/**
		 * Mit dieser Methode können die Datenidentifikationsdaten übergeben werden.
		 *
		 * @param data enthält die ausgewählte Datenidentifikation
		 */
		public void setDataIdentification(final SettingsData data) {
			Class moduleClass = ParameterEditorModule.class;

			SettingsData settingsData = new SettingsData(
					getModuleName(), moduleClass, data.getObjectTypes(), data.getAttributeGroup(), data.getAspect(), data.getObjects()
			);
			settingsData.setSimulationVariant(data.getSimulationVariant());
			settingsData.setTreePath(data.getTreePath());
			startSettings(settingsData);
			saveSettings(settingsData);
		}

		/**
		 * Diese Methode zeigt den Dialog an und trägt die Einstellungsdaten in die entsprechenden Felder.
		 *
		 * @param data die Einstellungsdaten
		 */
		public void setSettings(final SettingsData data) {
			if(_dialog == null) {
				createDialog();
			}
			_dataIdentificationChoice.setDataIdentification(
					data.getObjectTypes(), data.getAttributeGroup(), null, data.getObjects(), data.getSimulationVariant()
			);
			_dataIdentificationChoice.showTree(getApplication().getTreeNodes(), getApplication().getConnection(), data.getTreePath());
			showDialog();
		}

		/**
		 * Startet den Parametereditor mit den Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			AttributeGroup atg = settingsData.getAttributeGroup();
			List objects = settingsData.getObjects();
			SystemObject systemObject = (SystemObject)objects.get(0);
//			int simulationVariant = settingsData.getSimulationVariant();
//			DatasetEditorFrame datasetEditorFrame = new DatasetEditorFrame(getConnection(), "ParameterEditor", atg, null, systemObject, simulationVariant);
//			datasetEditorFrame.startParameterEditor();
			new ParameterEditor(getConnection(), systemObject, atg, (short)settingsData.getSimulationVariant());
		}

		/** Erstellt den Dialog mit der Datenidentifikation. */
		private void createDialog() {
			_dialog = new JDialog();
			_dialog.setTitle(getButtonText());
			_dialog.setResizable(false);

			Container pane = _dialog.getContentPane();
			pane.setLayout(new BorderLayout());

			// Datenidentifikationsauswahl-Panel
			DataModel configuration = getConnection().getDataModel();
			final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
			final List<Aspect> neededAspects = new LinkedList<Aspect>();
			types.add(configuration.getType("typ.konfigurationsObjekt"));
			neededAspects.add(configuration.getAspect("asp.parameterSoll"));
			neededAspects.add(configuration.getAspect("asp.parameterVorgabe"));
			PreselectionListsFilter listFilter = new PreselectionListsFilter() {
				public List applyFilter(int whichList, List list) {
					List<SystemObject> filteredList = new ArrayList<SystemObject>(list.size());
					for(Iterator iterator = list.iterator(); iterator.hasNext();) {
						SystemObject systemObject = (SystemObject)iterator.next();
						if(systemObject.getName() == null || systemObject.getName().equals("")) continue;
						if(whichList == PreselectionListsFilter.ATTRIBUTEGROUP_LIST) {
							if(!((AttributeGroup)systemObject).getAspects().containsAll(neededAspects)) continue;
						}
						filteredList.add(systemObject);
					}
					return filteredList;
				}
			};
			_dataIdentificationChoice = new DataIdentificationChoice(listFilter, types);
			pane.add(_dataIdentificationChoice, BorderLayout.NORTH);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);
			_dialog.getRootPane().setDefaultButton(buttonBar.getAcceptButton());
			pane.add(buttonBar, BorderLayout.SOUTH);
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
		 * @param title der Name der Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(String title) {
			Class moduleClass = ParameterEditorModule.class;
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

		/** Durch betätigen des "OK"-Buttons wird der Parametereditor gestartet. Die ausgewählten Einstellungen werden gespeichert. */
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
