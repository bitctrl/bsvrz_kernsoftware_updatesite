/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.communication.ConfigurationCommunicator;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.util.async.AsyncRequestQueue;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Mit dieser Klasse wird die Konfiguration gestartet. Die Konfiguration kann in folgenden verschiedenen Arbeitsmodi parallel gestartet werden: <ul> <li>Import
 * von Versorgungsdateien</li> <li>Export von Versorgungsdateien</li> <li>Konsistenzprüfung der Konfiguration</li> <li>Aktivierung von
 * Konfigurationsbereichen</li> <li>Freigabe zur Übernahme von Konfigurationsbereichen</li> <li>Freigabe zur Aktivierung von Konfigurationsbereichen</li>
 * <li>Konfigurationseditor zur interaktiven Versorgung</li> <li>Konfiguration als dienstleistender Hintergrundprozeß für andere Applikationen des Systems</li>
 * </ul>
 * <p>
 * Die Konfiguration kann mit folgenden spezifischen Aufrufparametern gestartet werden: <ul> <li>-verwaltung=datei</li>Über diesen Aufrufparameter wird die
 * Verwaltungsdatei spezifiziert, aus der sich die Konfiguration initialisieren soll. Wenn keine weiteren Aufrufparameter angegeben werden, dann wird der
 * Arbeitsmodus 'Konfiguration' gestartet. <ul> Soll Die Konfiguration im Arbeitsmodus 'Konfiguration' betrieben werden, so können/müssen folgende Parameter
 * angegeben werden:<br> <li>Optionaler Parameter: -benutzerverwaltung=datei</li>Über diesen Parameter wird die Datei spezifiziert, die die
 * Benutzerverwaltungsinformationen(Benutzernamen, Passwörter, usw.) enthält. Wird keine Datei angegeben, wird eine Default-Datei benutzt. Neue Benutzer,
 * Änderungen der Passwörter, usw. werden in der angegebenen Datei durchgeführt.<br> <li>-benutzer=Benutzername</li> Enthält den Benutzernamen, mit dem sich die
 * Konfiguration beim Datenverteiler anmeldet <li>-authentifizierung=datei</li> Enthält das Passwort, mit dem sich die Konfiguration beim Datenverteiler
 * anmeldet </ul> <li>-import=pid,pid,...</li>Über diesen Aufrufparameter werden eine oder mehrere Pids der Konfigurationsbereiche angegeben, die importiert
 * werden sollen. Wenn dieser Aufrufparameter angegeben wurde, dann wird der Arbeitsmodus 'Import' gestartet. In diesem Arbeitsmodus müssen außerdem die
 * Aufrufparameter '-verzeichnis' und '-verwaltung' angegeben werden. <li>-export=pid,pid,...</li>Über diesen Aufrufparameter werden eine oder mehrere Pids der
 * Konfigurationsbereiche angegeben, die exportiert werden sollen. Wenn dieser Aufrufparameter angegeben wurde, dann wird der Arbeitsmodus 'Export' gestartet.
 * In diesem Arbeitsmodus müssen außerdem die Aufrufparameter '-verzeichnis' und '-verwaltung' angegeben werden. <li>-verzeichnis=verzeichnis</li>Dieser
 * Aufrufparameter gilt im Zusammenhang mit dem Arbeitsmodus 'Import' oder 'Export'. Er gibt an, wo (beim Import) sich die Dateien befinden bzw. wohin (beim
 * Export) die Dateien geschrieben werden sollen. <li>-editor</li>Über diesen Aufrufparameter wird spezifiziert, dass der Arbeitsmodus 'Konfigurationseditor'
 * gestartet werden soll. Wenn zusätzlich der Aufrufparameter '-verwaltung' angegeben wird, dann arbeitet der Editor im Offline-Modus direkt auf den
 * Konfigurationsdateien. Ansonsten arbeitet der Editor im Online-Modus und versucht über die Datenverteilerschnittstelle eine Verbindung zu einer Instanz im
 * Arbeitsmodus 'Konfiguration' aufzubauen. <li>-konsistenzprüfung</li>Beauftragt die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version auf
 * Konsistenz zu prüfen. In diesem Arbeitsmodus muss außerdem der Aufrufparameter '-verwaltung' angegeben werden. <li>-aktivierung</li>Beauftragt die
 * Konfiguration alle Konfigurationsbereiche in der aktuellsten Version zu aktivieren. In diesem Arbeitsmodus muss außerdem der Aufrufparameter '-verwaltung'
 * angegeben werden. <li>-freigabeaktivierung</li>Beauftragt die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version für andere Konfigurationen
 * "zur Aktivierung freizugeben". In diesem Arbeitsmodus muss außerdem der Aufrufparameter '-verwaltung' angegeben werden. <li>-freigabeübernahme</li>Beauftragt
 * die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version zur Übernahme freizugeben. In diesem Arbeitsmodus muss außerdem der Aufrufparameter
 * '-verwaltung' angegeben werden. <li>-freigabeZurAktivierungOhneLokaleAktivierungDurchDenKV=pid,pid,...</li>Über diesen Aufrufparameter werden eine oder
 * mehrere Pids von Konfigurationsbereichen angegeben. Diese Bereiche können durch eine andere Konfiguration aktiviert werden, ohne dass der
 * Konfigurationsverantwortliche der Bereiche die angegebene Bereiche aktiviert hat. In diesem Arbeitsmodus muss außerdem der Aufrufparameter '-verwaltung'
 * angegeben werden.<li>keine Aufrufparameter</li>Ohne Aufrufparameter wird der Arbeitsmodus 'Konfigurationseditor' im Online-Modus gestartet. </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */

public class ConfigurationApp {

	/** DebugLogger für Debug-Ausgaben */
	private static Debug _debug;

	/** Thread, der beim Herunterfahren des Systems die Daten sichert. */
	private AutoCloser _autoCloser = null;

	/** Zeitspanne, die gewartet wird bis die Konfiguration die gepufferten Daten persistent speichert. */
	public static final long _bufferTime = 10 * 60 * 1000;

	/** 7 Tage in Millisekunden, die Zeit, die zwischen periodischen Restrukturierungen gewartet wird */
	public static final long _restructureTime = 604800000;

	private ForeignObjectManager _foreignObjectManager;

	/**
	 * Main-Methode, welche die Aufrufparameter entgegennimmt und den Konstruktor aufruft.
	 *
	 * @param args die Aufrufparameter dieser Klasse
	 */
	public static void main(String[] args) {
		new ConfigurationApp(args);
	}

	/**
	 * Der Konstruktor wertet die Aufrufargumente aus.
	 *
	 * @param args die Aufrufargumente
	 */
	public ConfigurationApp(String[] args) {
		// Argumente einlesen
		final ArgumentList argumentList = new ArgumentList(args);

		// Debugger einrichten
		Debug.init("ConfigurationApp", argumentList);
		_debug = Debug.getLogger();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

		// die verschiedenen Arbeitsmodi bestimmen
		boolean correctUsage = false;

		try {
			if(argumentList.fetchArgument("-?=false").booleanValue() || argumentList.fetchArgument("-hilfe=false").booleanValue()) {
				System.out.println("Hilfe...");
			}
			else if(!argumentList.hasUnusedArguments()) {
				// wenn es keine Argumente gibt -> Konfigurationseditor im Online-Modus
				correctUsage = true;
				startConfigurationEditorOnline();
			}
			else if(argumentList.hasArgument("-import") && argumentList.hasArgument("-verzeichnis") && argumentList.hasArgument("-verwaltung")) {
				// wenn die Argumente import, verzeichnis und verwaltung angegeben wurden, dann wird importiert
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final File importPath = argumentList.fetchArgument("-verzeichnis").asDirectory();
				final String importPids = argumentList.fetchArgument("-import").asNonEmptyString();
				final String[] pids = importPids.split(",");
				if(!argumentList.hasUnusedArguments()) {
					correctUsage = true;
					startImport(managementFile, importPath, trimPids(pids));
				}
			}
			else if(argumentList.hasArgument("-export") && argumentList.hasArgument("-verzeichnis") && argumentList.hasArgument("-verwaltung")) {
				// wenn die Argumente export, verzeichnis und verwaltung angegeben wurden, dann wird exportiert
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final File exportPath = argumentList.fetchArgument("-verzeichnis").asDirectory();
				final String exportPids = argumentList.fetchArgument("-export=").asString();
				final String[] pids = exportPids.split(",");
				if(!argumentList.hasUnusedArguments()) {
					correctUsage = true;
					startExport(managementFile, exportPath, trimPids(pids));
				}
			}
			else if(argumentList.hasArgument("-editor")) {
				System.out.println("Editor");
				argumentList.fetchArgument("-editor");    // entfernt dieses Argument
				if(argumentList.hasArgument("-verwaltung")) {
					// wenn die Argumente editor und verwaltung angegeben wurden, dann wird der Konfigurationseditor im Offline-Modus gestartet
					final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
					if(!argumentList.hasUnusedArguments()) {
						correctUsage = true;
						startConfigurationEditorOffline(managementFile);
					}
				}
				else if(!argumentList.hasUnusedArguments()) {
					// wenn nur das Argument editor und sonst nichts angegeben wurde, dann wird der Konfigurationseditor im Online-Modus gestartet
					correctUsage = true;
					startConfigurationEditorOnline();
				}
			}
			else if(argumentList.hasArgument("-konsistenzprüfung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbhängigkeitenBeiKonsistenzprüfung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				argumentList.fetchArgument("-konsistenzprüfung");
				checkConsistency(managementFile, allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				correctUsage = true;
			}
			else if(argumentList.hasArgument("-aktivierung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbhängigkeitenBeiKonsistenzprüfung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				argumentList.fetchArgument("-aktivierung");
				startActivation(managementFile, allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				correctUsage = true;
			}
			else if(argumentList.hasArgument("-freigabeaktivierung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				argumentList.fetchArgument("-freigabeaktivierung");
				startReleaseAreasForActivation(managementFile);
				correctUsage = true;
			}
			else if((argumentList.hasArgument("-freigabeübernahme") || argumentList.hasArgument("-freigabeuebernahme")) && argumentList.hasArgument(
					"-verwaltung"
			)) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbhängigkeitenBeiKonsistenzprüfung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				if(argumentList.hasArgument("-freigabeübernahme")) {
					argumentList.fetchArgument("-freigabeübernahme");
				}
				else {
					argumentList.fetchArgument("-freigabeuebernahme");
				}
				startReleaseAreasForTransfer(managementFile, allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				correctUsage = true;
			}
			else if(argumentList.hasArgument("-freigabeZurAktivierungOhneLokaleAktivierungDurchDenKV") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbhängigkeitenBeiKonsistenzprüfung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				final String activatePids = argumentList.fetchArgument("-freigabeZurAktivierungOhneLokaleAktivierungDurchDenKV").asNonEmptyString();

				final String[] pids = activatePids.split(",");
				if(!argumentList.hasUnusedArguments()) {
					correctUsage = true;
					startReleaseForActivationWithoutCAActivation(managementFile, trimPids(pids), allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				}
			}
			else if(argumentList.hasArgument("-restrukturierung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				correctUsage = true;
				startRestructure(managementFile);
			}
			else if(argumentList.hasArgument("-verwaltung") && argumentList.hasArgument("-benutzer") && argumentList.hasArgument("-authentifizierung")) {

				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();

				// Information: Die Argumente -benutzer und -authentifizierung werden später automatisch rausgezogen

				final File userManagementFile;
				if(argumentList.hasArgument("-benutzerverwaltung")) {
					// Der Benutzer hat eine Datei angegeben
					userManagementFile = argumentList.fetchArgument("-benutzerverwaltung").asExistingFile();
				}
				else {
					// Es wird die Standarddatei benutzt
					userManagementFile = new File(ConfigAuthentication.class.getResource("User.xml").getFile());
				}

				final File backupDirectory;
				if(argumentList.hasArgument("-sicherungsVerzeichnis")) {
					backupDirectory = argumentList.fetchArgument("-sicherungsVerzeichnis").asFile();
					if(backupDirectory.exists() && !backupDirectory.isDirectory()) {
						throw new java.lang.IllegalArgumentException("Argument -sicherungsVerzeichnis: Ist kein Verzeichnis: " + backupDirectory);
					}
				}
				else {
					// Standard: Ordner innerhalb des Speicherortes der Verwaltungsdaten.xml
					backupDirectory = new File(managementFile.getParentFile(), "Sicherungen");
				}

				_debug.info("Konfiguration wird mit folgender Benutzerverwaltungsdatei gestartet: " + userManagementFile.getAbsolutePath());

				ClientDavParameters clientDavParameters = new ClientDavParameters(argumentList);

				final long garbageCollectionIntervallSeconds = argumentList.fetchArgument("-garbageCollection=0").longValue();
				argumentList.ensureAllArgumentsUsed();
				correctUsage = true;

				final ConfigDataModel dataModel = startConfiguration(managementFile, userManagementFile, clientDavParameters, backupDirectory);

				dealWithInvalidOnRestartObjects(dataModel);

				_debug.info("Konfiguration ist bereit für Anfragen");
				System.out.println("");
				System.out.println("******************************************************************");
				System.out.println("********** Die Konfiguration ist bereit für Anfragen *************");
				System.out.println("******************************************************************");
				System.out.println("");

				if(garbageCollectionIntervallSeconds > 0) {
					_debug.info("GarbageCollection alle " + garbageCollectionIntervallSeconds + " Sekunden");
					while(true) {
						Thread.sleep(1000L * garbageCollectionIntervallSeconds);
						_debug.fine("Garbage Collection");
						System.gc();
						_debug.finer("Garbage Collection fertig");
					}
				}
			}
		}
		catch(IllegalArgumentException ex) {
			_debug.error("Ein Fehler beim Auswerten der Aufrufparameter ist aufgetreten", ex);
		}
		catch(Exception ex) {
			_debug.error("Fehler beim Starten der Konfiguration ist aufgetreten", ex);
		}
		catch(Error e) {
			_debug.error("Error beim Starten der Konfiguration", e);
		}
		finally {
			// wenn keiner der if-Zweige gilt, dann waren die Aufrufparameter fehlerhaft
			if(!correctUsage) {
				usage();
				System.exit(1);
			}
		}
	}

	/**
	 * Diese Methode setzt alle dynamischen Objekte, die als "ungültigBeimNeustart" markiert sind, auf ungültig.
	 *
	 * @param dataModel Datenmodell
	 */
	private void dealWithInvalidOnRestartObjects(ConfigDataModel dataModel) throws ConfigurationChangeException {

		// Alle Objekte/Elemente des Typs typ.dynamischerTyp. Die entspricht allen Typen, die dynamisch sind.
		final List<SystemObject> allDynamicTypes = dataModel.getType("typ.dynamischerTyp").getElements();
		// Auf die richtige Klasse casten
		final List<SystemObjectType> systemObjectTypes = new ArrayList<SystemObjectType>(allDynamicTypes.size());
		for(SystemObject systemObject : allDynamicTypes) {
			systemObjectTypes.add((DynamicObjectType)systemObject);
		}

		// Alle Bereiche, die der Konfigurationsverantwortliche der Konfiguration ändern darf
		final Collection<ConfigurationArea> examineAreas = getAuthorityConfigAreas(dataModel.getConfigurationAuthority(), dataModel);

		// Alle aktuellen dynamischen Objekte, für die der Konfigurationsverantwortliche der Konfiguration
		// verantwortlich ist. Achtung, diese Methode benutzt die Vererbung, eine Einschränkung der Tyen macht also
		// keinen Sinn !!
		final Collection<SystemObject> allDynamicObjects = dataModel.getAllObjects(examineAreas, systemObjectTypes, ObjectTimeSpecification.valid());

		// Alle Objekte auf ungültig setzen, die transient sind.
		for(SystemObject systemObject : allDynamicObjects) {

			// Objekttyp des Objekts anfordern, dies kann nur ein dynamischer Typ sein
			final DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObject.getType();

			if(dynamicObjectType.getPersistenceMode() == DynamicObjectType.PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART) {
				systemObject.invalidate();
				_debug.finest("InvalidOnRestart Objekt: " + systemObject.getPidOrNameOrId());
			}
		}
//		_debug.info("Anzahl gefundener transienter dynamischer Typen: " + transientObjectTypes.size() + " Anzahl dynamischer Objekte, die transient waren und auf ungültig gesetzt wurden: " + allDynamicObjects.size());
	}

	/**
	 * Diese Methode sucht zu einem Konfigurationsverantwortlichen alle aktiven Konfigurationsbereiche heraus, für die er verantwortlich ist.
	 *
	 * @param authority Verantwortlicher, zu dem die Bereiche gesucht werden sollen
	 * @param dataModel Datenmodell, in dem die Bereiche vorhanden sind
	 */
	private Collection<ConfigurationArea> getAuthorityConfigAreas(ConfigurationAuthority authority, DataModel dataModel) {
		final Collection<ConfigurationArea> resultAreas = new ArrayList<ConfigurationArea>();
		// alle aktiven Konfigurationsbereiche ermitteln
		final SystemObjectType objectType = dataModel.getType("typ.konfigurationsBereich");
		final List<SystemObject> objects = objectType.getElements();
		for(SystemObject systemObject : objects) {
			final ConfigurationArea configurationArea = (ConfigurationArea)systemObject;
			if(authority.equals(configurationArea.getConfigurationAuthority())) {
				resultAreas.add(configurationArea);
			}
		}
		return resultAreas;
	}

	/**
	 * Die eingelesenen Strings werden von Leerzeichen befreit, die vor oder nach den Pids stehen.
	 *
	 * @param pids die zu überprüfenden Strings
	 *
	 * @return die Strings enthalten jetzt vor oder nach der Pid keine Leerzeichen mehr
	 */
	private List<String> trimPids(String[] pids) {
		final List<String> pidList = new ArrayList<String>();
		for(String pid : pids) {
			pidList.add(pid.trim());
		}
		return pidList;
	}

	private void startConfigurationEditorOnline() {
		_debug.info("Konfigurationseditor im Online-Modus wird gestartet");
	}

	private void startConfigurationEditorOffline(File managementFile) {
		_debug.info("Konfigurationseditor im Offline-Modus wird gestartet: " + managementFile.getName());
	}

	/** Startet die Konfiguration */
	private ConfigDataModel startConfiguration(File managementFile, File userManagementFile, ClientDavParameters dafParameters, final File backupDirectory)
			throws Exception, CommunicationError, ConnectionException, ParserConfigurationException, MissingParameterException, InterruptedException, InconsistentLoginException {
		_debug.info("Konfiguration wird gestartet: " + managementFile.getName());
		// Datenmodell starten
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile);

		// Queue und Thread für die asynchrone Abarbeitung von bestimmten Konfigurationsanfragen erzeugen
		AsyncRequestQueue asyncRequestQueue = new AsyncRequestQueue();
		asyncRequestQueue.start();

		// Startet den Mechanismus, der Konfigurationsanfragen entgegennimmt und verarbeitet
		final ConfigurationCommunicator configurationCommunicator;
		try {
			configurationCommunicator = new ConfigurationCommunicator(
					asyncRequestQueue, dataModel, userManagementFile, dafParameters, dataModel.getManagementFile().getForeignObjectCacheFile()
			);
		}
		catch(Exception e) {
			// Im Fehlerfall dafür sorgen, dass die bereits angelegten Lock-Dateien gelöscht werden
			dataModel.close();
			throw e;
		}

		dataModel.setBackupBaseDirectory(backupDirectory);
		dataModel.setUserManagement(configurationCommunicator.getAuthentication());

		_foreignObjectManager = configurationCommunicator.getForeignObjectManager();

		// Automatisches Speichern aktivieren
		startAutoSaver(dataModel);
		// Beim herunterfahren des Systems speichern
		startAutoCloser(dataModel, configurationCommunicator);

		_debug.info("Konfiguration", dataModel);
		return dataModel;
	}
	private void startImport(File managementFile, File importPath, List<String> pids) {
		_debug.fine("Import wird gestartet: " + managementFile.getAbsolutePath() + "\t" + importPath.getAbsolutePath() + "\t" + pids.size());
		// Datamodell
		final DataModel dataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.

		startAutoCloser(dataModel, null);

		// Import starten
		try {
			((ConfigurationControl)dataModel).importConfigurationAreas(importPath, pids);
		}
		catch(Exception ex) {
			_debug.error("Der Import konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Der Import konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	private void startExport(File managementFile, File exportPath, List<String> pids) {
		_debug.info("Export wird gestartet: " + managementFile.toString() + "\t" + exportPath.toString() + "\t" + pids.size());
		final ConfigDataModel configDataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(configDataModel, null);

		if(pids.size() == 1 && pids.get(0).equals("")) {
			pids = new ArrayList<String>(configDataModel.getAllConfigurationAreas().keySet());
		}

		// Export starten
		try {
			configDataModel.exportConfigurationAreas(exportPath, pids);
		}
		catch(Exception ex) {
			_debug.error("Der Export konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Der Export konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	/**
	 * Unterzieht alle Bereiche in der aktuellsten Version einer Konsistenzprüfung. Die Version muss nicht unbedingt die aktuelle Version sein, sondern die
	 * Version, die aktiviert werden könnte.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden sollen.
	 */
	private void checkConsistency(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Konsistenzprüfung wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
//		dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);
		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern.
		// Bereiche ,für die die Konfiguration verantwortlich ist, werden in der neusten Version geprüft.
		// Bereiche, für die die Konfiguration nicht verantwortlich ist, werden in der "zur Übernahme freigegeben" Version geprüft.

		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();
		// Alle Bereiche mit den jeweiligen Versionen
		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		for(ConfigurationArea configurationArea : configurationAreas) {
			final short version;
			if(dataModel.getConfigurationAuthority() == configurationArea.getConfigurationAuthority() || dataModel.getConfigurationAuthorityPid().equals(
					configurationArea.getConfigurationAuthority().getPid()
			)) {
				// In der neusten Version prüfen
				version = ((ConfigConfigurationArea)configurationArea).getLastModifiedVersion();
			}
			else {
				version = configurationArea.getTransferableVersion();
			}
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea, version));
		}

		try {
			final ConsistencyCheckResultInterface consistencyCheckResult = dataModel.checkConsistency(configAreasAndVersions);
			_debug.info(consistencyCheckResult.toString());
		}
		catch(Exception ex) {
			_debug.error("Die Konsistenzprüfung konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Konsistenzprüfung konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	/**
	 * Aktiviert alle Bereiche in der höchst möglichen Version.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden sollen.
	 */
	private void startActivation(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Aktivierung wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
		//dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Auch die aus der Verwaltungsdatei
		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		for(ConfigurationArea configurationArea : configurationAreas) {
			// Den Bereich mit der aktuellsten Version prüfen und aktivieren.
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
		}

		try {
			dataModel.activateConfigurationAreas(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Aktivierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Aktivierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	/**
	 * Gibt alle Bereiche zur Aktivierung für andere Konfigurationen in der höchst möglichen Version frei.
	 *
	 * @param managementFile Verwaltungsdatei, wird zum Erzeugen des DataModel gebraucht
	 */
	private void startReleaseAreasForActivation(File managementFile) {
		_debug.info("Freigabe zur Aktivierung wird gestartet: " + managementFile.toString());
		final DataModel dataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Die aus der Verwaltungsdatei müssen nicht angefordert werden, weil
		// die Bereiche noch nicht lokal aktiviert wurden und somit nicht für andere zur Aktivierung freigegeben
		// werden können.
		// Es können nur die Bereiche zur Aktivierung freigegen werden, für die die Konfiguration auch verantwortlich ist.

		// Alle Bereiche, die aktiv sind (müssen noch gecastet werden)
		final List<SystemObject> areas = dataModel.getType("typ.konfigurationsBereich").getObjects();
		// Alle Bereiche, die betrachtet werden müssen. Für dieses Bereiche ist die Konfiguration auch verantwortlich.
		final Collection<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>(areas.size());
		for(SystemObject area : areas) {
			final ConfigurationArea configurationArea = (ConfigurationArea)area;
			if(configurationArea.getConfigurationAuthority() == dataModel.getConfigurationAuthority()) {
				configurationAreas.add(configurationArea);
			}
		}

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>(configurationAreas.size());

		for(ConfigurationArea configurationArea : configurationAreas) {
			// Den Bereich mit der lokal aktivierten Version für andere freigeben
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
		}

		try {
			((ConfigurationControl)dataModel).releaseConfigurationAreasForActivation(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur Aktivierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Freigabe zur Aktivierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	/**
	 * Gibt alle Bereiche zur Übernahme für andere Konfigurationen in der höchst möglichen Version frei.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden sollen.
	 */
	private void startReleaseAreasForTransfer(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Freigabe zur Übernahme wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
		//dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Auch die aus der Verwaltungsdatei
		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();
		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		// Es dürfen nur Bereiche freigegeben werden, für die die Konfiguration auch der KV ist.

		for(ConfigurationArea configurationArea : configurationAreas) {
			if(dataModel.getConfigurationAuthority() == configurationArea.getConfigurationAuthority()) {
				// Der Bereich wird mit der größt möglichen Version für andere zur Übernahme freigegeben
				configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
			}
		}

		try {
			dataModel.releaseConfigurationAreasForTransfer(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur Übernahme konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Freigabe zur Übernahme konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}


	/**
	 * Gibt die angegebenen Bereiche zur Aktivierung für andere Konfigurationen in der höchst möglichen Version frei. Die entsprechenden Bereiche müssen vorher
	 * nicht lokal aktiviert worden sein.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param pids            Pids der Konfigurationsbereiche, die freigegeben werden sollen.
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden sollen.
	 */
	private void startReleaseForActivationWithoutCAActivation(
			final File managementFile, final List<String> pids, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {

		_debug.info("Freigabe zur Übernahme wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
//		dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		// Prüfen, ob die übergebenen Bereiche überhaupt für andere freigegeben werden dürfen.
		// Nur der KV kann die Bereiche freigeben.

		for(String pid : pids) {
			// Dieser Bereich soll für andere zur Aktivierung freigegeben werden ohne vorher durch den KV aktiviert worden zu sein
			final ConfigurationArea area = dataModel.getConfigurationArea(pid);

			configAreasAndVersions.add(new ConfigAreaAndVersion(area));
		}

		try {
			final ConsistencyCheckResultInterface consistencyCheckResult = dataModel.releaseConfigurationAreasForActivationWithoutCAActivation(
					configAreasAndVersions
			);

			if(consistencyCheckResult.interferenceErrors()) {
				// Es gab Interfernezfehler. Die Bereiche können nicht für anderen freigegeben werden.
				_debug.warning(
						"Bei der Aktivierung durch andere ohne vorherige Aktivierung durch den KV wurden folgende Inkonsistenzen erkannt, die nicht zu einem Abbruch der Aktion geführt haben: "
						+ consistencyCheckResult
				);
			}
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur Aktivierung ohne Aktivierung durch den KV konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Freigabe zur Aktivierung ohne Aktivierung durch den KV konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}

	/**
	 * Startet die manuelle Restrukturierung von allen Konfigurationsbereichen des aktuellen KV
	 * @param managementFile   Verwaltungsdaten
	 */
	private void startRestructure(final File managementFile) {

		_debug.info("Freigabe zur Übernahme wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind für andere gesperrt worden (lock-Dateien). Diese müssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		try {
			dataModel.restructure(ConfigurationAreaFile.RestructureMode.FullRestructure);
		}
		catch(Exception ex) {
			_debug.error("Die Restrukturierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
			throw new RuntimeException("Die Restrukturierung konnte nicht ordnungsgemäß durchgeführt werden", ex);
		}
	}


	/** Gibt die Verwendungsmöglichkeiten der Konfiguration aus. */
	private void usage() {
		System.out.println();
		System.out.print("Verwendung: ");
		System.out.println("java " + this.getClass().getName() + " [optionen]");
		System.out.println();   // Leerzeile
		System.out.println("Folgende Parameter werden unterstützt:");
		System.out.println("\t-?");
		System.out.println("\t-hilfe");
		System.out.println("\t-verwaltung=datei           die Verwaltungsdatei der Konfiguration");
		System.out.println("\t-import=pid,pid,...         die Pids der zu importierenden Konfigurationsbereiche");
		System.out.println("\t-export=pid,pid,...         die Pids der zu exportierenden Konfigurationsbereiche");
		System.out.println("\t-verzeichnis=versorgung     Verzeichnis der Versorgungsdateien");
		System.out.println("\t-editor                     für den Konfigurationseditor");
		System.out.println("\t-konsistenzprüfung          für die Konsistenzprüfung");
		System.out.println("\t-aktivierung                für die Aktivierung von Konfigurationsbereichen");
		System.out.println("\t-restrukturierung           für die manuelle Restrukturierung von Konfigurationsbereichen");
		System.out.println("\t-doppeltePidsZulassen       wenn doppelte PIDs zugelassen sein sollen");
		System.out.println("\t-ignoriereFehlerDerAbhängigkeitenBeiKonsistenzprüfung");
		System.out.println("\t                            wenn Fehler, die auf nicht erfüllte Abhängigkeiten zwischen Konfigurationsbereichen zurückzuführen");
		System.out.println("\t                            sind, während der Konsistenzprüfung ignoriert werden sollen");
		System.out.println("\t-freigabeaktivierung        für die Freigabe von Konfigurationsbereichen zur Aktivierung");
		System.out.println("\t-freigabeübernahme          für die Freigabe von Konfigurationsbereichen zur Übernahme");
		System.out.println("\t-sicherungsVerzeichnis=pfad Verzeichnis zur Sicherung von Konfigurationsdateien");

		System.out.println();    // Leerzeile
		System.out.println("Folgende Optionen werden unterstützt:");
		System.out.println("\t-Konfiguration benötigt die Parameter '-verwaltung', '-benutzer', '-authentifizierung' und optional '-benutzerverwaltung'");
		System.out.println("\t-Import benötigt die Parameter '-import', '-verzeichnis' und '-verwaltung'");
		System.out.println("\t-Export benötigt die Parameter '-export', '-verzeichnis' und '-verwaltung'");
		System.out.println("\t-Konfigurationseditor benötigt den Parameter '-editor' und für den Offline-Modus zusätzlich '-verwaltung'");
		System.out.println("\t-Konsistenzprüfung benötigt die Parameter '-konsistenzprüfung' und '-verwaltung'");
		System.out.println("\t-Aktivierung von Konfigurationsbereichen benötigt die Parameter '-aktivierung' und '-verwaltung'");
		System.out.println("\t-Restrukturierung benötigt die Parameter '-restrukturierung' und '-verwaltung'");
		System.out.println("\t-Freigabe aller Konfigurationsbereiche zur Aktivierung benötigt die Parameter '-freigabeaktivierung' und '-verwaltung'");
		System.out.println("\t-Freigabe aller Konfigurationsbereiche zur Übernahme benötigt die Parameter '-freigabeübernahme' und '-verwaltung'");
	}

	/**
	 * Startet den Timer, der zyklisch die Daten sichert
	 */
	public void startAutoSaver(ConfigDataModel dataModel) {

		final Timer configTimer = new Timer("ConfigTimer", true);
		// autoSaver Alle 10 Minuten speichern
		configTimer.schedule(new AutoSaver(dataModel), _bufferTime, _bufferTime);
	}

	/** Legt einen Thread an, der ausgeführt wird, wenn das System beendet wird. Dieser Thread wird alle Daten der Konfiguration sichern. */
	public void startAutoCloser(DataModel dataModel, ConfigurationCommunicator configurationCommunicator) {
		if(_autoCloser == null) {
			_autoCloser = new AutoCloser(dataModel, configurationCommunicator);
			Runtime.getRuntime().addShutdownHook(new Thread(_autoCloser));
		}
	}

	/** Runnable Implementierung, die beim Beenden und zyklisch dafür sorgt, dass ungesicherte Konfigurationsänderungen gespeichert werden. */
	private class AutoSaver extends TimerTask {

		private final ConfigDataModel _dataModel;

		public AutoSaver(ConfigDataModel dataModel) {
			_dataModel = dataModel;
		}

		/** Methode, die einerseits durch den Timer zyklisch aufgerufen wird und andererseits beim Beenden der Konfiguration. */
		public void run() {
			try {
				if(_foreignObjectManager != null) _foreignObjectManager.save();
			}
			catch(Exception ex) {
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim nächsten Mal
				_debug.warning("Zwischenspeicher für Fremdobjekte konnte nicht gespeichert werden", ex);
			}
			try {
				_dataModel.save();
			}
			catch(IOException ex) {
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim nächsten Mal
				_debug.warning("Die Verwaltungsdaten und die Konfigurationsdateien konnten nicht gespeichert werden", ex.getMessage());
			}
		}
	}

	/**
	 * Dieser Thread wird aufgerufen, wenn die Konfiguration runtergefahren werden soll. Es wird am ConfigDataModel und am ConfigurationCommunicator
	 * <code>close</code> aufgerufen.
	 */
	private class AutoCloser extends TimerTask implements Runnable {

		private final DataModel _dataModel;

		private final ConfigurationCommunicator _configurationCommunicator;

		/**
		 * Objekte, an denen die Close Methode aufgerufen wird.
		 *
		 * @param dataModel                 Datenmodell, an dem close aufgerufen wird. <code>null</code> darf nicht übergeben werden.
		 * @param configurationCommunicator Objekt, an dem close aufgerufen wird. Soll an diesem Objekte kein close aufgerufen werden, muss <code>null</code>
		 *                                  aufgerufen werden.
		 */
		public AutoCloser(DataModel dataModel, ConfigurationCommunicator configurationCommunicator) {
			_dataModel = dataModel;
			_configurationCommunicator = configurationCommunicator;
		}

		public void run() {
//			System.out.println("AutoCloser");
			try {
				if(_foreignObjectManager != null) _foreignObjectManager.close();
			}
			catch(Exception ex) {
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim nächsten Mal
				_debug.warning("Zwischenspeicher für Fremdobjekte konnte nicht geschlossen werden", ex.getMessage());
			}
			((ConfigDataModel)_dataModel).close();

			if(_configurationCommunicator != null) {
				_configurationCommunicator.close();
			}
		}
	}

	/**
	 * Implementierung eines UncaughtExceptionHandlers, der bei nicht abgefangenen Exceptions und Errors entsprechende Ausgaben macht und im Falle eines Errors den
	 * Prozess terminiert.
	 */
	private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		/** Speicherreserve, die freigegeben wird, wenn ein Error auftritt, damit die Ausgaben nach einem OutOfMemoryError funktionieren */
		private volatile byte[] _reserve = new byte[20000];

		public void uncaughtException(Thread t, Throwable e) {
			if(e instanceof Error) {
				// Speicherreserve freigeben, damit die Ausgaben nach einem OutOfMemoryError funktionieren
				_reserve = null;
				try {
					System.err.println("Schwerwiegender Laufzeitfehler: Ein Thread hat sich wegen eines Errors beendet, Prozess wird terminiert");
					System.err.println(t);
					e.printStackTrace(System.err);
					_debug.error("Schwerwiegender Laufzeitfehler: " + t + " hat sich wegen eines Errors beendet, Prozess wird terminiert", e);
				}
				catch(Throwable ignored) {
					// Weitere Fehler während der Ausgaben werden ignoriert, damit folgendes exit() auf jeden Fall ausgeführt wird.
				}
				System.exit(1);
			}
			else {
				System.err.println("Laufzeitfehler: Ein Thread hat sich wegen einer Exception beendet:");
				System.err.println(t);
				e.printStackTrace(System.err);
				_debug.error("Laufzeitfehler: " + t + " hat sich wegen einer Exception beendet", e);
			}
		}
	}
}
