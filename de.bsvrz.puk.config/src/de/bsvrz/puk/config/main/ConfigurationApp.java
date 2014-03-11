/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich+Kni� Systemberatung Aachen (K2S)
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.main;

import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.ConnectionException;
import de.bsvrz.dav.daf.main.InconsistentLoginException;
import de.bsvrz.dav.daf.main.MissingParameterException;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
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
 * von Versorgungsdateien</li> <li>Export von Versorgungsdateien</li> <li>Konsistenzpr�fung der Konfiguration</li> <li>Aktivierung von
 * Konfigurationsbereichen</li> <li>Freigabe zur �bernahme von Konfigurationsbereichen</li> <li>Freigabe zur Aktivierung von Konfigurationsbereichen</li>
 * <li>Konfigurationseditor zur interaktiven Versorgung</li> <li>Konfiguration als dienstleistender Hintergrundproze� f�r andere Applikationen des Systems</li>
 * </ul>
 * <p/>
 * Die Konfiguration kann mit folgenden spezifischen Aufrufparametern gestartet werden: <ul> <li>-verwaltung=datei</li>�ber diesen Aufrufparameter wird die
 * Verwaltungsdatei spezifiziert, aus der sich die Konfiguration initialisieren soll. Wenn keine weiteren Aufrufparameter angegeben werden, dann wird der
 * Arbeitsmodus 'Konfiguration' gestartet. <ul> Soll Die Konfiguration im Arbeitsmodus 'Konfiguration' betrieben werden, so k�nnen/m�ssen folgende Parameter
 * angegeben werden:<br> <li>Optionaler Parameter: -benutzerverwaltung=datei</li>�ber diesen Parameter wird die Datei spezifiziert, die die
 * Benutzerverwaltungsinformationen(Benutzernamen, Passw�rter, usw.) enth�lt. Wird keine Datei angegeben, wird eine Default-Datei benutzt. Neue Benutzer,
 * �nderungen der Passw�rter, usw. werden in der angegebenen Datei durchgef�hrt.<br> <li>-benutzer=Benutzername</li> Enth�lt den Benutzernamen, mit dem sich die
 * Konfiguration beim Datenverteiler anmeldet <li>-authentifizierung=datei</li> Enth�lt das Passwort, mit dem sich die Konfiguration beim Datenverteiler
 * anmeldet </ul> <li>-import=pid,pid,...</li>�ber diesen Aufrufparameter werden eine oder mehrere Pids der Konfigurationsbereiche angegeben, die importiert
 * werden sollen. Wenn dieser Aufrufparameter angegeben wurde, dann wird der Arbeitsmodus 'Import' gestartet. In diesem Arbeitsmodus m�ssen au�erdem die
 * Aufrufparameter '-verzeichnis' und '-verwaltung' angegeben werden. <li>-export=pid,pid,...</li>�ber diesen Aufrufparameter werden eine oder mehrere Pids der
 * Konfigurationsbereiche angegeben, die exportiert werden sollen. Wenn dieser Aufrufparameter angegeben wurde, dann wird der Arbeitsmodus 'Export' gestartet.
 * In diesem Arbeitsmodus m�ssen au�erdem die Aufrufparameter '-verzeichnis' und '-verwaltung' angegeben werden. <li>-verzeichnis=verzeichnis</li>Dieser
 * Aufrufparameter gilt im Zusammenhang mit dem Arbeitsmodus 'Import' oder 'Export'. Er gibt an, wo (beim Import) sich die Dateien befinden bzw. wohin (beim
 * Export) die Dateien geschrieben werden sollen. <li>-editor</li>�ber diesen Aufrufparameter wird spezifiziert, dass der Arbeitsmodus 'Konfigurationseditor'
 * gestartet werden soll. Wenn zus�tzlich der Aufrufparameter '-verwaltung' angegeben wird, dann arbeitet der Editor im Offline-Modus direkt auf den
 * Konfigurationsdateien. Ansonsten arbeitet der Editor im Online-Modus und versucht �ber die Datenverteilerschnittstelle eine Verbindung zu einer Instanz im
 * Arbeitsmodus 'Konfiguration' aufzubauen. <li>-konsistenzpr�fung</li>Beauftragt die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version auf
 * Konsistenz zu pr�fen. In diesem Arbeitsmodus muss au�erdem der Aufrufparameter '-verwaltung' angegeben werden. <li>-aktivierung</li>Beauftragt die
 * Konfiguration alle Konfigurationsbereiche in der aktuellsten Version zu aktivieren. In diesem Arbeitsmodus muss au�erdem der Aufrufparameter '-verwaltung'
 * angegeben werden. <li>-freigabeaktivierung</li>Beauftragt die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version f�r andere Konfigurationen
 * "zur Aktivierung freizugeben". In diesem Arbeitsmodus muss au�erdem der Aufrufparameter '-verwaltung' angegeben werden. <li>-freigabe�bernahme</li>Beauftragt
 * die Konfiguration alle Konfigurationsbereiche in der aktuellsten Version zur �bernahme freizugeben. In diesem Arbeitsmodus muss au�erdem der Aufrufparameter
 * '-verwaltung' angegeben werden. <li>-freigabeZurAktivierungOhneLokaleAktivierungDurchDenKV=pid,pid,...</li>�ber diesen Aufrufparameter werden eine oder
 * mehrere Pids von Konfigurationsbereichen angegeben. Diese Bereiche k�nnen durch eine andere Konfiguration aktiviert werden, ohne dass der
 * Konfigurationsverantwortliche der Bereiche die angegebene Bereiche aktiviert hat. In diesem Arbeitsmodus muss au�erdem der Aufrufparameter '-verwaltung'
 * angegeben werden.<li>keine Aufrufparameter</li>Ohne Aufrufparameter wird der Arbeitsmodus 'Konfigurationseditor' im Online-Modus gestartet. </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */

public class ConfigurationApp {

	/** DebugLogger f�r Debug-Ausgaben */
	private static Debug _debug;

	/** Thread, der beim beenden oder nach einer festen Zeitspanne, die Konfiguration auffordert zu speichern. */
	private AutoSaver _autoSaver = null;

	/** Thread, der beim Herunterfahren des Systems die Daten sichert. */
	private AutoCloser _autoCloser = null;

	/** Zeitspanne, die gewartet wird bis die Konfiguration die gepufferten Daten persistent speichert. */
	public static final long _bufferTime = 10 * 60 * 1000;

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
			else if(argumentList.hasArgument("-konsistenzpr�fung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbh�ngigkeitenBeiKonsistenzpr�fung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				argumentList.fetchArgument("-konsistenzpr�fung");
				checkConsistency(managementFile, allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				correctUsage = true;
			}
			else if(argumentList.hasArgument("-aktivierung") && argumentList.hasArgument("-verwaltung")) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbh�ngigkeitenBeiKonsistenzpr�fung=nein").booleanValue() || argumentList.fetchArgument(
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
			else if((argumentList.hasArgument("-freigabe�bernahme") || argumentList.hasArgument("-freigabeuebernahme")) && argumentList.hasArgument(
					"-verwaltung"
			)) {
				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();
				final boolean allowDoublePids = argumentList.fetchArgument("-doppeltePidsZulassen=nein").booleanValue();
				final boolean ignoreDependencyErrorsInConsistencyCheck =
						argumentList.fetchArgument("-ignoriereFehlerDerAbh�ngigkeitenBeiKonsistenzpr�fung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				if(argumentList.hasArgument("-freigabe�bernahme")) {
					argumentList.fetchArgument("-freigabe�bernahme");
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
						argumentList.fetchArgument("-ignoriereFehlerDerAbh�ngigkeitenBeiKonsistenzpr�fung=nein").booleanValue() || argumentList.fetchArgument(
								"-ignoriereFehlerDerAbhaengigkeitenBeiKonsistenzpruefung=nein"
						).booleanValue();
				final String activatePids = argumentList.fetchArgument("-freigabeZurAktivierungOhneLokaleAktivierungDurchDenKV").asNonEmptyString();

				final String[] pids = activatePids.split(",");
				if(!argumentList.hasUnusedArguments()) {
					correctUsage = true;
					startReleaseForActivationWithoutCAActivation(managementFile, trimPids(pids), allowDoublePids, ignoreDependencyErrorsInConsistencyCheck);
				}
			}
			else if(argumentList.hasArgument("-verwaltung") && argumentList.hasArgument("-benutzer") && argumentList.hasArgument("-authentifizierung")) {

				final File managementFile = argumentList.fetchArgument("-verwaltung").asExistingFile();

				// Information: Die Argumente -benutzer und -authentifizierung werden sp�ter automatisch rausgezogen

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

				final DataModel dataModel = startConfiguration(managementFile, userManagementFile, clientDavParameters, backupDirectory);

				dealWithInvalidOnRestartObjects(dataModel);

				_debug.info("Konfiguration ist bereit f�r Anfragen");
				System.out.println("");
				System.out.println("******************************************************************");
				System.out.println("********** Die Konfiguration ist bereit f�r Anfragen *************");
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
	 * Diese Methode setzt alle dynamischen Objekte, die als "ung�ltigBeimNeustart" markiert sind, auf ung�ltig.
	 *
	 * @param dataModel Datenmodell
	 */
	private void dealWithInvalidOnRestartObjects(DataModel dataModel) throws ConfigurationChangeException {

		// Alle Objekte/Elemente des Typs typ.dynamischerTyp. Die entspricht allen Typen, die dynamisch sind.
		final List<SystemObject> allDynamicTypes = dataModel.getType("typ.dynamischerTyp").getElements();
		// Auf die richtige Klasse casten
		final List<SystemObjectType> systemObjectTypes = new ArrayList<SystemObjectType>(allDynamicTypes.size());
		for(SystemObject systemObject : allDynamicTypes) {
			systemObjectTypes.add((DynamicObjectType)systemObject);
		}

		// Alle Bereiche, die der Konfigurationsverantwortliche der Konfiguration �ndern darf
		final Collection<ConfigurationArea> examineAreas = getAuthorityConfigAreas(dataModel.getConfigurationAuthority(), dataModel);

		// Alle aktuellen dynamischen Objekte, f�r die der Konfigurationsverantwortliche der Konfiguration
		// verantwortlich ist. Achtung, diese Methode benutzt die Vererbung, eine Einschr�nkung der Tyen macht also
		// keinen Sinn !!
		final Collection<SystemObject> allDynamicObjects = dataModel.getObjects(examineAreas, systemObjectTypes, ObjectTimeSpecification.valid());

		// Alle Objekte auf ung�ltig setzen, die transient sind.
		for(SystemObject systemObject : allDynamicObjects) {

			// Objekttyp des Objekts anfordern, dies kann nur ein dynamischer Typ sein
			final DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObject.getType();

			if(dynamicObjectType.getPersistenceMode() == DynamicObjectType.PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART) {
				systemObject.invalidate();
				_debug.finest("InvalidOnRestart Objekt: " + systemObject.getPidOrNameOrId());
			}
		}
//		_debug.info("Anzahl gefundener transienter dynamischer Typen: " + transientObjectTypes.size() + " Anzahl dynamischer Objekte, die transient waren und auf ung�ltig gesetzt wurden: " + allDynamicObjects.size());
	}

	/**
	 * Diese Methode sucht zu einem Konfigurationsverantwortlichen alle aktiven Konfigurationsbereiche heraus, f�r die er verantwortlich ist.
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
	 * @param pids die zu �berpr�fenden Strings
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
	private DataModel startConfiguration(File managementFile, File userManagementFile, ClientDavParameters dafParameters, final File backupDirectory)
			throws Exception, CommunicationError, ConnectionException, ParserConfigurationException, MissingParameterException, InterruptedException, InconsistentLoginException {
		_debug.info("Konfiguration wird gestartet: " + managementFile.getName());
		// Datenmodell starten
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile);

		// Queue und Thread f�r die asynchrone Abarbeitung von bestimmten Konfigurationsanfragen erzeugen
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
			// Im Fehlerfall daf�r sorgen, dass die bereits angelegten Lock-Dateien gel�scht werden
			dataModel.close();
			throw e;
		}

		((ConfigDataModel)dataModel).setBackupBaseDirectory(backupDirectory);
		((ConfigDataModel)dataModel).setUserManagement(configurationCommunicator.getAuthentication());

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

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.

		startAutoCloser(dataModel, null);

		// Import starten
		try {
			((ConfigurationControl)dataModel).importConfigurationAreas(importPath, pids);
		}
		catch(Exception ex) {
			_debug.error("Der Import konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Der Import konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}

	private void startExport(File managementFile, File exportPath, List<String> pids) {
		_debug.info("Export wird gestartet: " + managementFile.toString() + "\t" + exportPath.toString() + "\t" + pids.size());
		final ConfigDataModel configDataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
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
			_debug.error("Der Export konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Der Export konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}

	/**
	 * Unterzieht alle Bereiche in der aktuellsten Version einer Konsistenzpr�fung. Die Version muss nicht unbedingt die aktuelle Version sein, sondern die
	 * Version, die aktiviert werden k�nnte.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzpr�fung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Pr�fung der Abh�ngigkeiten in der Konsistenzpr�fung ignoriert werden sollen.
	 */
	private void checkConsistency(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Konsistenzpr�fung wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
//		dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);
		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern.
		// Bereiche ,f�r die die Konfiguration verantwortlich ist, werden in der neusten Version gepr�ft.
		// Bereiche, f�r die die Konfiguration nicht verantwortlich ist, werden in der "zur �bernahme freigegeben" Version gepr�ft.

		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();
		// Alle Bereiche mit den jeweiligen Versionen
		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		for(ConfigurationArea configurationArea : configurationAreas) {
			final short version;
			if(dataModel.getConfigurationAuthority() == configurationArea.getConfigurationAuthority() || dataModel.getConfigurationAuthorityPid().equals(
					configurationArea.getConfigurationAuthority().getPid()
			)) {
				// In der neusten Version pr�fen
				version = ((ConfigConfigurationArea)configurationArea).getLastModifiedVersion();
			}
			else {
				version = configurationArea.getTransferableVersion();
			}
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea, version));
		}

		try {
			final ConsistencyCheckResultInterface consistencyCheckResult = ((ConfigurationControl)dataModel).checkConsistency(configAreasAndVersions);
			_debug.info(consistencyCheckResult.toString());
		}
		catch(Exception ex) {
			_debug.error("Die Konsistenzpr�fung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Die Konsistenzpr�fung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}

	/**
	 * Aktiviert alle Bereiche in der h�chst m�glichen Version.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzpr�fung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Pr�fung der Abh�ngigkeiten in der Konsistenzpr�fung ignoriert werden sollen.
	 */
	private void startActivation(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Aktivierung wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
		//dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Auch die aus der Verwaltungsdatei
		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		for(ConfigurationArea configurationArea : configurationAreas) {
			// Den Bereich mit der aktuellsten Version pr�fen und aktivieren.
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
		}

		try {
			((ConfigurationControl)dataModel).activateConfigurationAreas(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Aktivierung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Die Aktivierung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}

	/**
	 * Gibt alle Bereiche zur Aktivierung f�r andere Konfigurationen in der h�chst m�glichen Version frei.
	 *
	 * @param managementFile Verwaltungsdatei, wird zum Erzeugen des DataModel gebraucht
	 */
	private void startReleaseAreasForActivation(File managementFile) {
		_debug.info("Freigabe zur Aktivierung wird gestartet: " + managementFile.toString());
		final DataModel dataModel = new ConfigDataModel(managementFile);

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Die aus der Verwaltungsdatei m�ssen nicht angefordert werden, weil
		// die Bereiche noch nicht lokal aktiviert wurden und somit nicht f�r andere zur Aktivierung freigegeben
		// werden k�nnen.
		// Es k�nnen nur die Bereiche zur Aktivierung freigegen werden, f�r die die Konfiguration auch verantwortlich ist.

		// Alle Bereiche, die aktiv sind (m�ssen noch gecastet werden)
		final List<SystemObject> areas = dataModel.getType("typ.konfigurationsBereich").getObjects();
		// Alle Bereiche, die betrachtet werden m�ssen. F�r dieses Bereiche ist die Konfiguration auch verantwortlich.
		final Collection<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>(areas.size());
		for(SystemObject area : areas) {
			final ConfigurationArea configurationArea = (ConfigurationArea)area;
			if(configurationArea.getConfigurationAuthority() == dataModel.getConfigurationAuthority()) {
				configurationAreas.add(configurationArea);
			}
		}

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>(configurationAreas.size());

		for(ConfigurationArea configurationArea : configurationAreas) {
			// Den Bereich mit der lokal aktivierten Version f�r andere freigeben
			configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
		}

		try {
			((ConfigurationControl)dataModel).releaseConfigurationAreasForActivation(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur Aktivierung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Die Freigabe zur Aktivierung konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}

	/**
	 * Gibt alle Bereiche zur �bernahme f�r andere Konfigurationen in der h�chst m�glichen Version frei.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzpr�fung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Pr�fung der Abh�ngigkeiten in der Konsistenzpr�fung ignoriert werden sollen.
	 */
	private void startReleaseAreasForTransfer(File managementFile, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_debug.info("Freigabe zur �bernahme wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
		//dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		// Alle Bereiche anfordern. Auch die aus der Verwaltungsdatei
		final Collection<ConfigurationArea> configurationAreas = dataModel.getAllConfigurationAreas().values();
		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		// Es d�rfen nur Bereiche freigegeben werden, f�r die die Konfiguration auch der KV ist.

		for(ConfigurationArea configurationArea : configurationAreas) {
			if(dataModel.getConfigurationAuthority() == configurationArea.getConfigurationAuthority()) {
				// Der Bereich wird mit der gr��t m�glichen Version f�r andere zur �bernahme freigegeben
				configAreasAndVersions.add(new ConfigAreaAndVersion(configurationArea));
			}
		}

		try {
			((ConfigurationControl)dataModel).releaseConfigurationAreasForTransfer(configAreasAndVersions);
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur �bernahme konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Die Freigabe zur �bernahme konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}


	/**
	 * Gibt die angegebenen Bereiche zur Aktivierung f�r andere Konfigurationen in der h�chst m�glichen Version frei. Die entsprechenden Bereiche m�ssen vorher
	 * nicht lokal aktiviert worden sein.
	 *
	 * @param managementFile  Verwaltungsdatei, wird zum erzeugen des DataModel gebraucht
	 * @param pids            Pids der Konfigurationsbereiche, die freigegeben werden sollen.
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzpr�fung zugelassen werden sollen.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                        <code>true</code> falls Fehler bei der Pr�fung der Abh�ngigkeiten in der Konsistenzpr�fung ignoriert werden sollen.
	 */
	private void startReleaseForActivationWithoutCAActivation(
			final File managementFile, final List<String> pids, final boolean allowDoublePids, final boolean ignoreDependencyErrorsInConsistencyCheck) {

		_debug.info("Freigabe zur �bernahme wird gestartet: " + managementFile.toString());
		final ConfigDataModel dataModel = new ConfigDataModel(managementFile, ignoreDependencyErrorsInConsistencyCheck);
		dataModel.setAllowDoublePids(allowDoublePids);
//		dataModel.setIgnoreDependencyErrorsInConsistencyCheck(ignoreDependencyErrorsInConsistencyCheck);

		// Alle Dateien sind f�r andere gesperrt worden (lock-Dateien). Diese m�ssen wieder freigegeben werden, sobald
		// die Aktion beendet wurde.
		startAutoCloser(dataModel, null);

		final List<ConfigAreaAndVersion> configAreasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		// Pr�fen, ob die �bergebenen Bereiche �berhaupt f�r andere freigegeben werden d�rfen.
		// Nur der KV kann die Bereiche freigeben.

		for(String pid : pids) {
			// Dieser Bereich soll f�r andere zur Aktivierung freigegeben werden ohne vorher durch den KV aktiviert worden zu sein
			final ConfigurationArea area = dataModel.getConfigurationArea(pid);

			configAreasAndVersions.add(new ConfigAreaAndVersion(area));
		}

		try {
			final ConsistencyCheckResultInterface consistencyCheckResult = ((ConfigurationControl)dataModel).releaseConfigurationAreasForActivationWithoutCAActivation(
					configAreasAndVersions
			);

			if(consistencyCheckResult.interferenceErrors()) {
				// Es gab Interfernezfehler. Die Bereiche k�nnen nicht f�r anderen freigegeben werden.
				_debug.warning(
						"Bei der Aktivierung durch andere ohne vorherige Aktivierung durch den KV wurden folgende Inkonsistenzen erkannt, die nicht zu einem Abbruch der Aktion gef�hrt haben: "
						+ consistencyCheckResult
				);
			}
		}
		catch(Exception ex) {
			_debug.error("Die Freigabe zur Aktivierung ohne Aktivierung durch den KV konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
			throw new RuntimeException("Die Freigabe zur Aktivierung ohne Aktivierung durch den KV konnte nicht ordnungsgem�� durchgef�hrt werden", ex);
		}
	}


	/** Gibt die Verwendungsm�glichkeiten der Konfiguration aus. */
	private void usage() {
		System.out.println();
		System.out.print("Verwendung: ");
		System.out.println("java " + this.getClass().getName() + " [optionen]");
		System.out.println();   // Leerzeile
		System.out.println("Folgende Parameter werden unterst�tzt:");
		System.out.println("\t-?");
		System.out.println("\t-hilfe");
		System.out.println("\t-verwaltung=datei           die Verwaltungsdatei der Konfiguration");
		System.out.println("\t-import=pid,pid,...         die Pids der zu importierenden Konfigurationsbereiche");
		System.out.println("\t-export=pid,pid,...         die Pids der zu exportierenden Konfigurationsbereiche");
		System.out.println("\t-verzeichnis=versorgung     Verzeichnis der Versorgungsdateien");
		System.out.println("\t-editor                     f�r den Konfigurationseditor");
		System.out.println("\t-konsistenzpr�fung          f�r die Konsistenzpr�fung");
		System.out.println("\t-aktivierung                f�r die Aktivierung von Konfigurationsbereichen");
		System.out.println("\t-doppeltePidsZulassen       wenn doppelte PIDs zugelassen sein sollen");
		System.out.println("\t-ignoriereFehlerDerAbh�ngigkeitenBeiKonsistenzpr�fung");
		System.out.println("\t                            wenn Fehler, die auf nicht erf�llte Abh�ngigkeiten zwischen Konfigurationsbereichen zur�ckzuf�hren");
		System.out.println("\t                            sind, w�hrend der Konsistenzpr�fung ignoriert werden sollen");
		System.out.println("\t-freigabeaktivierung        f�r die Freigabe von Konfigurationsbereichen zur Aktivierung");
		System.out.println("\t-freigabe�bernahme          f�r die Freigabe von Konfigurationsbereichen zur �bernahme");
		System.out.println("\t-sicherungsVerzeichnis=pfad Verzeichnis zur Sicherung von Konfigurationsdateien");

		System.out.println();    // Leerzeile
		System.out.println("Folgende Optionen werden unterst�tzt:");
		System.out.println("\t-Konfiguration ben�tigt die Parameter '-verwaltung', '-benutzer', '-authentifizierung' und optional '-benutzerverwaltung'");
		System.out.println("\t-Import ben�tigt die Parameter '-import', '-verzeichnis' und '-verwaltung'");
		System.out.println("\t-Export ben�tigt die Parameter '-export', '-verzeichnis' und '-verwaltung'");
		System.out.println("\t-Konfigurationseditor ben�tigt den Parameter '-editor' und f�r den Offline-Modus zus�tzlich '-verwaltung'");
		System.out.println("\t-Konsistenzpr�fung ben�tigt die Parameter '-konsistenzpr�fung' und '-verwaltung'");
		System.out.println("\t-Aktivierung von Konfigurationsbereichen ben�tigt die Parameter '-aktivierung' und '-verwaltung'");
		System.out.println("\t-Freigabe aller Konfigurationsbereiche zur Aktivierung ben�tigt die Parameter '-freigabeaktivierung' und '-verwaltung'");
		System.out.println("\t-Freigabe aller Konfigurationsbereiche zur �bernahme ben�tigt die Parameter '-freigabe�bernahme' und '-verwaltung'");
	}

	/** Startet den Thread, der zyklisch die Daten sichert */
	public void startAutoSaver(DataModel dataModel) {

		if(_autoSaver == null) {
			_autoSaver = new AutoSaver(dataModel);
			final Timer configTimer = new Timer("ConfigTimer", true);
			// _autoSaver Alle 10 Minuten speichern
			configTimer.schedule(_autoSaver, _bufferTime, _bufferTime);
		}
	}

	/** Legt einen Thread an, der ausgef�hrt wird, wenn das System beendet wird. Dieser Thread wird alle Daten der Konfiguration sichern. */
	public void startAutoCloser(DataModel dataModel, ConfigurationCommunicator configurationCommunicator) {
		if(_autoCloser == null) {
			_autoCloser = new AutoCloser(dataModel, configurationCommunicator);
			Runtime.getRuntime().addShutdownHook(new Thread(_autoCloser));
		}
	}

	/** Runnable Implementierung, die beim Beenden und zyklisch daf�r sorgt, dass ungesicherte Konfigurations�nderungen gespeichert werden. */
	private class AutoSaver extends TimerTask implements Runnable {

		private final DataModel _dataModel;

		public AutoSaver(DataModel dataModel) {
			_dataModel = dataModel;
		}

		/** Methode, die einerseits durch den Timer zyklisch aufgerufen wird und andererseits beim Beenden der Konfiguration. */
		public void run() {
			try {
				if(_foreignObjectManager != null) _foreignObjectManager.save();
			}
			catch(Exception ex) {
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim n�chsten Mal
				_debug.warning("Zwischenspeicher f�r Fremdobjekte konnte nicht gespeichert werden", ex);
			}
			try {
				((ConfigDataModel)_dataModel).save();
			}
			catch(IOException ex) {
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim n�chsten Mal
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
		 * @param dataModel                 Datenmodell, an dem close aufgerufen wird. <code>null</code> darf nicht �bergeben werden.
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
				// falls das Speichern nicht gelingt wird nur eine Warnung ausgegeben, evtl. gelingt es beim n�chsten Mal
				_debug.warning("Zwischenspeicher f�r Fremdobjekte konnte nicht geschlossen werden", ex.getMessage());
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
					// Weitere Fehler w�hrend der Ausgaben werden ignoriert, damit folgendes exit() auf jeden Fall ausgef�hrt wird.
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
