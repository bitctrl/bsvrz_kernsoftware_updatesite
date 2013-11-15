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

package de.bsvrz.pat.sysbed.plugins.sysprot;

import de.bsvrz.pat.sysbed.plugins.api.ExternalModuleAdapter;
import de.bsvrz.pat.sysbed.plugins.api.ButtonBar;
import de.bsvrz.pat.sysbed.plugins.api.OutputOptionsPanel;
import de.bsvrz.pat.sysbed.plugins.api.DataIdentificationChoice;
import de.bsvrz.pat.sysbed.plugins.api.DialogInterface;
import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.pat.sysbed.plugins.api.settings.KeyValueObject;
import de.bsvrz.sys.funclib.consoleProcessFrame.ConsoleProcessFrame;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Implementiert das Modul für den {@link de.bsvrz.pat.sysprot.main.SystemProtocoller System-Protokollierer}. Ein Dialog wird zur Verfügung gestellt, damit die benötigten
 * Parameter für den System-Protokollierer eingestellt werden können. Die anzuzeigenden Daten werden in einer Konsole angezeigt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6201 $
 */
public class SystemProtocolModule extends ExternalModuleAdapter {

	/** speichert den Text des Tooltips */
	private String _tooltipText;

	/** speichert den Dialog des System-Protokollierers */
	private static SystemProtocolDialog _dialog;

	/* ################## Methoden ################ */

	/**
	 * Gibt den Namen des Moduls zurück.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName() {
		return "Systemprotokollierer";
	}

	/**
	 * Gibt den Text des Buttons zurück.
	 *
	 * @return Text des Buttons
	 */
	public String getButtonText() {
		return "Systemprotokollierer starten";
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
	 * Diese Methode erhält die ausgewählte Datenidentifikation und startet den {@link SystemProtocolDialog Dialog} zur Auswahl der Einstellungen des
	 * System-Protokollierers.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData) {
		_dialog = new SystemProtocolDialog();
		_dialog.setDataIdentification(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für den System-Protokollierer und startet diesen ohne den Dialog anzuzeigen.
	 *
	 * @param settingsData die Einstellungen für den System-Protokollierer
	 */
	public void startSettings(final SettingsData settingsData) {
		_dialog = new SystemProtocolDialog();
		_dialog.startConsole(settingsData);
	}

	/**
	 * Diese Methode erhält alle Einstellungen für den System-Protokollierer und startet den {@link SystemProtocolDialog Dialog} und füllt ihn entsprechend der
	 * Einstellungen
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void change(final SettingsData settingsData) {
		_dialog = new SystemProtocolDialog();
		_dialog.setSettings(settingsData);
	}

	/**
	 * Überprüft, ob die ausgewählte Datenidentifikation für dieses Modul zutrifft oder nicht.
	 *
	 * @param settingsData enthält die ausgewählte Datenidentifikation
	 *
	 * @return gibt an, ob die ausgewählte Datenidentifikation für dieses Modul zutrifft
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

	/* #################### Klasse SystemProtocolDialog ################ */

	/**
	 * Stellt einen Dialog dar, womit Parameter für den System-Protokollierer eingestellt werden können. Diese Parameter können gespeichert werden. Durch betätigen
	 * des "OK"-Buttons werden die Einstellungen übernommen, der System-Protokollierer gestartet und der Dialog geschlossen.
	 */
	private class SystemProtocolDialog implements DialogInterface {

		/** speichert den Dialog */
		private JDialog _dialog = null;

		/** speichert die Datenidentifikationsauswahl */
		private DataIdentificationChoice _dataIdentificationChoice;

		/** gibt den Beginn des Anfragezeitraums an */
		private JSpinner _periodOfSpinner;

		/** gibt das Ende des Anfragezeitraums an */
		private JSpinner _periodTillSpinner;

		/** speichert ein Objekt der Ausgabeoptionen */
		private OutputOptionsPanel _outputOptions;

		/* ################# Methoden ################ */

		/** Standardkonstruktor. Ein Objekt der Klasse wird angelegt. */
		public SystemProtocolDialog() {
		}

		/**
		 * Mit dieser Methode können die Datenidentifikationsdaten übergeben werden. Der Dialog wird mit Default-Werten dargestellt.
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
				if(key.equals("von")) {
					setPeriodOf(keyValueObject.getValue());
				}
				else if(key.equals("bis")) {
					setPeriodTill(keyValueObject.getValue());
				}
				else if(key.equals("ausgabe")) {
					setDetailLevel(keyValueObject.getValue());
				}
				else if(key.equals("datei")) {
					setFileName(keyValueObject.getValue());
				}
			}
			showDialog();
		}

		/**
		 * Diese Methode startet den System-Protokollierer mit den Einstellungsdaten. Die Ausgabe erfolgt in einer Konsole.
		 *
		 * @param settingsData die Einstellungsdaten
		 */
		public void startConsole(final SettingsData settingsData) {
			final List<String> parameterList = getParameterList(settingsData);
			String[] arguments = (String[])parameterList.toArray(new String[0]);
			try {
				ConsoleProcessFrame.createJavaProcessFrame(getModuleName(), "de.bsvrz.pat.sysprot.main.SystemProtocoller", arguments, null, null);
			}
			catch(IOException e) {
				JOptionPane.showMessageDialog(_dialog, e, "Fehler beim Start des System-Protokollierers", JOptionPane.ERROR_MESSAGE);
			}
		}

		/** Erstellt den Dialog. Bestandteil ist die Datenidentifikation, der Anfragezeitraum und die Ausgabeoption. */
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

			// Anfragezeitraum
			JPanel periodPanel = new JPanel();
			periodPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			periodPanel.setBorder(BorderFactory.createTitledBorder("Anfragezeitraum"));
			JLabel periodOfLabel = new JLabel("Von: ");
			SpinnerDateModel periodOfModel = new SpinnerDateModel();
			Date value = (Date)periodOfModel.getValue();
			long time = value.getTime();
			time = time - 3600000;
			value.setTime(time);
			periodOfModel.setValue(value);
			_periodOfSpinner = new JSpinner(periodOfModel);
			_periodOfSpinner.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							if(((Date)_periodOfSpinner.getModel().getValue()).getTime() > ((Date)_periodTillSpinner.getModel().getValue()).getTime()) {
								_periodTillSpinner.getModel().setValue(_periodOfSpinner.getModel().getValue());
							}
						}
					}
			);
			periodOfLabel.setLabelFor(_periodOfSpinner);

			JLabel periodTillLabel = new JLabel("Bis: ");
			SpinnerDateModel periodTillModel = new SpinnerDateModel();
			_periodTillSpinner = new JSpinner(periodTillModel);
			_periodTillSpinner.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							if(((Date)_periodTillSpinner.getModel().getValue()).getTime() < ((Date)_periodOfSpinner.getModel().getValue()).getTime()) {
								_periodOfSpinner.getModel().setValue(_periodTillSpinner.getModel().getValue());
							}
						}
					}
			);
			periodTillLabel.setLabelFor(_periodTillSpinner);

			periodPanel.add(periodOfLabel);
			periodPanel.add(_periodOfSpinner);
			periodPanel.add(Box.createHorizontalStrut(10));
			periodPanel.add(periodTillLabel);
			periodPanel.add(_periodTillSpinner);

			pane.add(periodPanel);

			// Ausgabeoptionen
			_outputOptions = new OutputOptionsPanel();
			_outputOptions.setDetailLevel(OutputOptionsPanel.DATA);
			pane.add(_outputOptions);

			// untere Buttonleiste
			final ButtonBar buttonBar = new ButtonBar(this);     // brauche noch Übergabeparameter
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
		 * Gibt den ausgewählten Beginn des Zeitraumes zurück.
		 *
		 * @return der Anfang des Anfragezeitraumes
		 */
		private String getPeriodOf() {
			Date date = (Date)_periodOfSpinner.getModel().getValue();
			long time = date.getTime();
			return String.valueOf(time);
		}

		/**
		 * Setzt den Anfang des Anfragezeitraumes.
		 *
		 * @param time Anfangszeit des Anfragezeitraumes
		 */
		private void setPeriodOf(String time) {
			Date date = new Date(Long.parseLong(time));
			_periodOfSpinner.getModel().setValue(date);
		}

		/**
		 * Gibt das ausgewählte Ende des Zeitraumes zurück.
		 *
		 * @return das Ende des Anfragezeitraumes
		 */
		private String getPeriodTill() {
			Date date = (Date)_periodTillSpinner.getModel().getValue();
			long time = date.getTime();
			return String.valueOf(time);
		}

		/**
		 * Setzt das Ende des Anfragezeitraumes.
		 *
		 * @param time das Ende des Anfragezeitraumes
		 */
		private void setPeriodTill(String time) {
			Date date = new Date(Long.parseLong(time));
			_periodTillSpinner.getModel().setValue(date);
		}

		/**
		 * Gibt zurück, wie detailliert die Daten ausgegeben werden sollen.
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
		 * Gibt den vollständigen Pfad (incl. Dateinamen) zurück, welcher für die Ausgabe vorgesehen ist.
		 *
		 * @return Pfad incl. Dateiname für die Ausgabe
		 */
		private String getFileName() {
			return _outputOptions.getFileName();
		}

		/**
		 * Übergibt den Dateinamen an die {@link de.bsvrz.pat.sysbed.plugins.api.OutputOptionsPanel Ausgabeoptionen}.
		 *
		 * @param fileName Pfad incl. Dateiname für die Ausgabe
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
			Class moduleClass = SystemProtocolModule.class;
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
				if(key.equals("objekt")) continue;
				String value = str.substring(index + 1, str.length());
				keyValueList.add(new KeyValueObject(key, value));
			}
			keyValueList.add(new KeyValueObject("von", getPeriodOf()));
			keyValueList.add(new KeyValueObject("bis", getPeriodTill()));
			keyValueList.add(new KeyValueObject("ausgabe", getDetailLevel()));
			String fileName = getFileName();
			if(fileName != null) {
				keyValueList.add(new KeyValueObject("datei", fileName));
			}

			return keyValueList;
		}

		/**
		 * Sammelt alle Einstellungen in einer Liste und gibt sie zurück. Die Liste enthält die Parameter für den System-Protokollierer.
		 *
		 * @param settingsData die Einstellungsdaten des Dialogs
		 *
		 * @return Liste aller Einstellungen für die Konsole
		 */
		private List<String> getParameterList(final SettingsData settingsData) {
			List<String> parameterList = new LinkedList<String>();
			List keyValueList = settingsData.getKeyValueList();
			for(Iterator iterator = keyValueList.iterator(); iterator.hasNext();) {
				KeyValueObject keyValueObject = (KeyValueObject)iterator.next();
				String key = keyValueObject.getKey();
				if(key.equals("von") || key.equals("bis")) {
					SimpleDateFormat dateFormat = new SimpleDateFormat();
					Date date = new Date(Long.parseLong(keyValueObject.getValue()));
					parameterList.add("-" + keyValueObject.getKey() + "=" + dateFormat.format(date));
				}
				else {
					parameterList.add("-" + keyValueObject.getKey() + "=" + keyValueObject.getValue());
				}
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
			
			parameterList.add("-daten=" + settingsData.getAttributeGroup().getPid() + ":" + settingsData.getAspect().getPid() + ":" + settingsData.getSimulationVariant());

			return parameterList;
		}

		/**
		 * Durch betätigen des "OK"-Buttons wird der Systemprotokollierer mit den eingestellten Parametern in einem neuen Fenster gestartet und dieser Dialog wird
		 * geschlossen.
		 */
		public void doOK() {
			SettingsData settingsData = getSettings("");
			startConsole(settingsData);
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
