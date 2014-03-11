/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.plugins.subscriptions;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 11326 $
 */
public class ShowSubscriptionInfo extends ExternalModuleAdapter {

	private String _tooltipText = "";

	private ShowSubscriptionInfoDialog _dialog;

	public String getModuleName() {
		return "Anmelde-Informationen";
	}

	public String getButtonText() {
		return "Anmeldungen";
	}

	public String getTooltipText() {
		return _tooltipText;
	}

	/**
	 * Gibt an, ob die Vorauswahl den Anforderungen der Onlinetabelle genügen.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt an, ob die Vorauswahl den Anforderungen der Onlinetabelle genügen
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		if(settingsData.getObjects().size() != 1){
			_tooltipText = "Genau ein Objekt muss ausgewählt sein.";
			return false;
		}

		if(settingsData.getObjects().get(0) instanceof ClientApplication
				&& (settingsData.getAttributeGroup() == null || settingsData.getAspect() == null)){
			// Anmeldungen für Applikation
			return true;
		}

		if(!super.isPreselectionValid(settingsData)) {
			_tooltipText = "Genau eine Applikation oder eine Attributgruppe und ein Aspekt muss ausgewählt sein.";
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

	/**
	 * Diese Methode erhält die ausgewählte Datenidentifikation und startet den {@link ShowSubscriptionInfoDialog Dialog}, damit die Anmeldeoptionen angegeben werden
	 * können.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new ShowSubscriptionInfoDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen und startet direkt den Dateneditor, um die aktuellen Daten anzuzeigen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new ShowSubscriptionInfoDialog();
		_dialog.startSettings(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen des {@link ShowSubscriptionInfoDialog Dialogs} und startet diesen.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new ShowSubscriptionInfoDialog();
		_dialog.setSettings(settingsData);
	}

private class ShowSubscriptionInfoDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert eine Instanz der Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/* ############### Methoden ############### */
		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public ShowSubscriptionInfoDialog() {
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
			showDialog();
		}

		/**
		 * Startet die Anzeige des aktuellen Datensatzes anhand der Einstellungsdaten.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startSettings(final SettingsData settingsData) {
			final AttributeGroup atg = settingsData.getAttributeGroup();
			final Aspect asp = settingsData.getAspect();
			final SystemObject systemObject = settingsData.getObjects().get(0);
			final int simulationVariant = settingsData.getSimulationVariant();

			if(systemObject instanceof ClientApplication) {
				ClientApplication object = (ClientApplication)systemObject;
				if(asp == null || atg == null){
					new AppSubscriptionInfoFrame(getConnection(), object).start();
					return;
				}
			}

			new SubscriptionInfoFrame(getConnection(), systemObject, atg, asp, simulationVariant).start();
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation und die Anmeldeoption, bestehend aus der Rolle und der Anmeldeart. */
		private void createDialog() {
			_dialog = new JDialog();
			_dialog.setTitle(getButtonText());
			_dialog.setResizable(false);

			final Container pane = _dialog.getContentPane();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

			// Datenidentifikationsauswahl-Panel
			final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
			final DataModel configuration = getConnection().getDataModel();
			types.add(configuration.getType("typ.konfigurationsObjekt"));
			types.add(configuration.getType("typ.dynamischesObjekt"));
			_dataIdentificationChoice = new DataIdentificationChoice(null, types);
			_dataIdentificationChoice.setNumberOfSelectedObjects(1, 1);
			pane.add(_dataIdentificationChoice);

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
		 * Erstellt die Einstellungsdaten.
		 *
		 * @param title der Name für die Einstellungen
		 *
		 * @return die Einstellungsdaten
		 */
		private SettingsData getSettings(final String title) {
			final Class moduleClass = ShowSubscriptionInfo.class;
			final List<SystemObjectType> objectTypes = _dataIdentificationChoice.getObjectTypes();
			final AttributeGroup atg = _dataIdentificationChoice.getAttributeGroup();
			final Aspect asp = _dataIdentificationChoice.getAspect();
			final List<SystemObject> objects = _dataIdentificationChoice.getObjects();

			final SettingsData settingsData = new SettingsData(getModuleName(), moduleClass, objectTypes, atg, asp, objects);
			settingsData.setTitle(title);
			settingsData.setSimulationVariant(_dataIdentificationChoice.getSimulationVariant());
			settingsData.setTreePath(_dataIdentificationChoice.getTreePath());

			return settingsData;
		}

		/**
		 * Durch betätigen des "OK"-Buttons werden die ausgewählten Objekte mit den eingestellten Parametern beim Datenverteiler angemeldet und angezeigt. Dieser
		 * Dialog wird geschlossen.
		 */
		public void doOK() {
			final SettingsData settingsData = getSettings("");
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
		public void doSave(final String title) {
			final SettingsData settingsData = getSettings(title);
			saveSettings(settingsData);
		}
	}
}
