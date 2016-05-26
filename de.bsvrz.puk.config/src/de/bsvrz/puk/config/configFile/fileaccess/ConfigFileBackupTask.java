/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.fileaccess;


import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.SenderReceiverCommunication;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet;
import de.bsvrz.puk.config.configFile.datamodel.MutableSetExtFileStorage;
import de.bsvrz.puk.config.configFile.datamodel.MutableSetStorage;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.managementfile.ManagementFile;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Klasse, die Konfigurationsdateien einer Konfiguration sichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigFileBackupTask {

	public static final int BACKUP_STATE_INITIALIZING = 0;

	public static final int BACKUP_STATE_INPROGRESS = 1;

	public static final int BACKUP_STATE_FINISHED = 2;

	private final ConfigurationFileManager _configurationFileManager;

	private File _targetDirectory;

	private volatile long _failed = 0;

	private volatile long _total = 0;

	private volatile long _completed = 0;

	private static final Debug _debug = Debug.getLogger();

	private ConfigDataModel _dataModel;

	private BackupProgressCallback _callback;

	private ConfigAuthentication _configAuthentication;

	private ConfigurationAuthority _configurationAuthority;

	private volatile ConfigAreaFile _currentFile;

	private SenderReceiverCommunication _sender;

	private int _queryIndex;

	/**
	 * Erstellt einen neuen ConfigFileBackupTask, welches den Fortschritt an ein lokales BackupProgressCallback-Objekt übergibt
	 *
	 * @param authentication         Klasse von der die Benutzerverwaltung.xml gesichert werden soll
	 * @param dataModel              Lokale Konfiguration
	 * @param target                 Zielverzeichnis, welches innerhalb von {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#getBackupBaseDirectory()}
	 *                               angelegt werden soll.
	 * @param configurationAuthority Konfigurationsverantwortlicher, dessen Konfigurations-Dateien gesichert werden sollen. Falls null werden
	 *                               alle Dateien gesichert.
	 * @param callback               Objekt, das über den Fortschritt des Backup-Vorgangs informiert werden soll.
	 * @throws IOException Wenn das angegebene target-Verzeichnis ungültig ist
	 */
	public ConfigFileBackupTask(
			final ConfigAuthentication authentication, final ConfigDataModel dataModel, final String target, final ConfigurationAuthority configurationAuthority, final BackupProgressCallback callback)
			throws IOException {
		this(authentication, dataModel, target, configurationAuthority);
		_callback = callback;
	}

	/**
	 * Erstellt einen neuen ConfigFileBackupTask, welches den Fortschritt über den Datenverteiler an ein RemoteRequester übermittelt
	 *
	 * @param authentication         Klasse von der die Benutzerverwaltung.xml gesichert werden soll
	 * @param dataModel              Lokale Konfiguration
	 * @param target                 Zielverzeichnis, welches innerhalb von {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#getBackupBaseDirectory()}
	 *                               angelegt werden soll.
	 * @param configurationAuthority Konfigurationsverantwortlicher, dessen Konfigurations-Dateien gesichert werden sollen. Falls null werden
	 *                               alle Dateien gesichert.
	 * @param senderReplyAreaTasks   Verbindung mit dem RemoteRequestManager
	 * @param queryIndex             Anfrageindex   @throws IOException Wenn das angegebene target-Verzeichnis ungültig ist
	 */
	public ConfigFileBackupTask(
			final ConfigAuthentication authentication,
			final ConfigDataModel dataModel,
			final String target,
			final ConfigurationAuthority configurationAuthority, final SenderReceiverCommunication senderReplyAreaTasks,
			final int queryIndex) throws IOException {
		this(authentication, dataModel, target, configurationAuthority);
		_sender = senderReplyAreaTasks;
		_queryIndex = queryIndex;
	}

	/**
	 * Erstellt einen neuen ConfigFileBackupTask, welches keinerlei Fortschrittsmeldungen sendet.
	 *
	 * @param authentication         Klasse von der die Benutzerverwaltung.xml gesichert werden soll. Kann null sein, dann wird keine
	 *                               Benutzerverwaltung gesichert.
	 * @param dataModel              Lokale Konfiguration
	 * @param target                 Zielverzeichnis, welches innerhalb von {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#getBackupBaseDirectory()}
	 *                               angelegt werden soll. Falls null oder ein Leerstring angegeben wird, wird anhand des aktuellen Datums, der
	 *                               Uhrzeit und/oder anderen nicht näher spezifizierten Mechanismen ein eindeutiges neues Verzeichnis
	 *                               erstellt. Falls im ConfigDataModel kein Zielverzeichnis über {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#setBackupBaseDirectory(java.io.File)
	 *                               } festgelegt wurde kann jedes beliebige absolute oder relative Verzeichnis angegeben werden.
	 * @param configurationAuthority Konfigurationsverantwortlicher, dessen Konfigurations-Dateien gesichert werden sollen. Falls null werden
	 *                               alle Dateien gesichert.
	 * @throws IOException Wenn das angegebene target-Verzeichnis ungültig ist
	 */
	public ConfigFileBackupTask(final ConfigAuthentication authentication, final ConfigDataModel dataModel, final String target, final ConfigurationAuthority configurationAuthority) throws IOException {
		_dataModel = dataModel;
		_configurationAuthority = configurationAuthority;
		_configurationFileManager = dataModel.getConfigurationFileManager();
		_configAuthentication = authentication;

		String subDir;
		if(!isStringNullOrBlank(target)) {
			subDir = target;
		}
		else {
			subDir = generateDirectoryName();
		}

		// Zielverzeichnis ermitteln und prüfen
		final File backupBaseDirectory = _dataModel.getBackupBaseDirectory();
		_targetDirectory = new File(backupBaseDirectory, subDir);
		if(backupBaseDirectory != null && !_targetDirectory.getCanonicalPath().startsWith(backupBaseDirectory.getCanonicalPath())) {
			throw new SecurityException(_targetDirectory.getPath() + " befindet sich nicht innerhalb von " + backupBaseDirectory.getPath());
		}
		if(_targetDirectory.exists() && !_targetDirectory.isDirectory()) {
			throw new IOException("Ist kein gültiges Verzeichnis: " + _targetDirectory);
		}
		if(!_targetDirectory.exists() && !_targetDirectory.mkdirs()) {
			throw new IOException("Konnte das angegebene Verzeichnis für Sicherungen nicht erstellen: " + _targetDirectory);
		}
	}

	private ConfigurationAreaFile[] getConfigurationAreasToBackup() {
		if(_configurationAuthority == null) {
			return _configurationFileManager.getConfigurationAreas();
		}
		else {
			final List<ConfigurationAreaFile> list = new ArrayList<ConfigurationAreaFile>();
			for(ConfigurationAreaFile configurationAreaFile : _configurationFileManager.getConfigurationAreas()) {
				final ConfigurationArea configurationArea = (ConfigurationArea) _dataModel.getObject(
						configurationAreaFile.getConfigurationAreaInfo().getPid()
				);
				final boolean isSameConfigAuth = configurationArea.getConfigurationAuthority().equals(_configurationAuthority);
				if(isSameConfigAuth) {
					list.add(configurationAreaFile);
				}
			}
			return list.toArray(new ConfigurationAreaFile[list.size()]);
		}
	}

	/**
	 * Formatiert das aktuelle Datum und Uhrzeit in einen als Pfadnamen darstellbaren, einfach sortierbaren String
	 *
	 * @return Beispielsweise 201009172210 wenn das aktuelle Datum der 17.09.2010 um 22:10 ist.
	 */
	private static String generateDirectoryName() {
		final SimpleDateFormat instance = new SimpleDateFormat("yyyyMMddHHmm", Locale.GERMAN);
		return instance.format(new Date());
	}

	private static boolean isStringNullOrBlank(String param) {
		return param == null || param.trim().length() == 0;
	}

	/**
	 * Startet einen Backup-Vorgang asynchron, wartet also nicht auf das Beenden.
	 */
	public void startAsync() {
		new Thread(new ConfigFileBackupTaskImplementation()).start(); // Neuen Thread starten
	}

	/**
	 * Startet einen Backup-Vorgang und wartet auf das Beenden.
	 *
	 * @return Ergebnis des Backupvorgangs
	 */
	public BackupResult startSync() {
		new ConfigFileBackupTaskImplementation().run(); // Keinen neuen Thread starten
		return new SimpleBackupResult(_completed, _failed, _targetDirectory.getAbsolutePath());
	}

	/**
	 * Gibt den aktuellen Fortschritt der aktuellen Datei zurück
	 *
	 * @return Ein Wert von 0.0 bis 1.0
	 */
	public double getFileProgress() {
		if(_currentFile == null) return 0;
		return ((double) _currentFile.getBackupProgress()) / _currentFile.getFileLength();
	}

	/**
	 * Gibt den aktuellen Gesamt-Fortschritt zurück
	 *
	 * @return Ein Wert von 0.0 bis 1.0
	 */
	public double getOverallProgress() {
		return (_completed + _failed + getFileProgress()) / _total;
	}

	/**
	 * Gibt das Zielverzeichnis zurück
	 *
	 * @return Das absolute Verzeichnis, in dem das Backup angelegt wird
	 */
	public String getTargetPath() {
		try {
			return _targetDirectory.getCanonicalPath();
		}
		catch(IOException e) {
			_debug.warning("getCanonicalPath(" + _targetDirectory + ") ist fehlgeschlagen", e);
			// Sollte nicht passieren, aber falls doch kann immer der absolute Pfad zurückgegeben werden,
			// da getAbsolutePath() keine Exception auslöst.
			return _targetDirectory.getAbsolutePath();
		}
	}

	/**
	 * Klasse, die die eigentliche Arbeit macht
	 */
	private class ConfigFileBackupTaskImplementation implements Runnable {

		/**
		 * Startet den Backup-Vorgang
		 */
		public void run() {
			// Aktualisierung alle 10 Sekunden
			final Timer timer = new Timer();
			TimerTask publisher = null;
			if(_sender != null) {
				publisher = new RemoteProgressPublisher();
				timer.schedule(publisher, 10000, 10000);
			}
			else if(_callback != null) {
				publisher = new LocalProgressPublisher();
				timer.schedule(publisher, 500, 500);
			}

			try {
				
				// Zu sichernde Konfigurationsdateien ermitteln
				final ConfigurationAreaFile[] files = getConfigurationAreasToBackup();
				// Zu sichernde Mengendateien ermitteln
				final List<MutableSetExtFileStorage> externalSetsToBackup = new ArrayList<MutableSetExtFileStorage>();
				ManagementFile managementFile = (ManagementFile) _dataModel.getManagementFile();

				final List<URI> allFiles = new ArrayList<URI>();
				for(ConfigurationAreaFile file : files) {
					allFiles.add(new File(file.toString()).toURI());
				}
				allFiles.add(new File(managementFile.toString()).toURI());
				if(_configAuthentication != null) {
					allFiles.add(new File(_configAuthentication.toString()).toURI());
				}
				
				URI baseDir = ManagementFile.getCommonBaseDir(allFiles);

				_debug.fine("Basisverzeichnis für Backup", baseDir);
				
				// Danach sortieren, ob die Dateien zum lokalen KV gehören, also änderbar sind.
				Arrays.sort(
						files, new Comparator<ConfigurationAreaFile>() {
					public int compare(final ConfigurationAreaFile caf1, final ConfigurationAreaFile caf2) {
						return groupChangeableFile(caf1).compareTo(groupChangeableFile(caf2));
					}

					private Integer groupChangeableFile(final ConfigurationAreaFile caf) {
						final ConfigurationArea configurationArea = (ConfigurationArea) _dataModel.getObject(
								caf.getConfigurationAreaInfo().getPid()
						);
						final boolean isSameConfigAuth = configurationArea.getConfigurationAuthority()
								.getPid()
								.equals(_dataModel.getConfigurationAuthorityPid());
						return isSameConfigAuth ? 0 : 1;
					}
				}
				);

				// Die einzelnen Mengendateien suchen
				SystemObjectType mutableSetType = _dataModel.getType(Pid.Type.MUTABLE_SET);
				for(SystemObject set : mutableSetType.getElements()) {
					if(set instanceof ConfigMutableSet) {
						ConfigMutableSet mutableSet = (ConfigMutableSet) set;
						MutableSetStorage storage = mutableSet.getMutableSetStorage();
						if(storage instanceof MutableSetExtFileStorage) {
							externalSetsToBackup.add((MutableSetExtFileStorage) storage);
						}
					}
					else {
						_debug.warning("Unbekannter Mengentyp", set);
					}
				}

				_total = files.length + externalSetsToBackup.size() + 1 + ((_configAuthentication != null) ? 1 : 0);
				
				// Sicherungsvorgang starten
				for(ConfigurationAreaFile file : files) {
					_currentFile = (ConfigAreaFile) file;
					try {
						_currentFile.createBackupFile(relativizeTarget(_targetDirectory, baseDir, _currentFile.toString()));
						_completed++;
					}
					catch(IOException e) {
						_failed++;
						_debug.error("Fehler beim Sichern von " + _currentFile.getConfigAreaPid(), e);
					}
				}
				_currentFile = null;


				// Sicherungsvorgang starten
				for(MutableSetExtFileStorage storage : externalSetsToBackup) {
					try {
						storage.createBackupFile(relativizeTarget(_targetDirectory, baseDir, storage.toString()));
						_completed++;
					}
					catch(IOException e) {
						_failed++;
						_debug.error("Fehler beim Sichern von " + _currentFile.getConfigAreaPid(), e);
					}
				}

				// Die Verwaltungs-XML sichern
				try {
					managementFile.createBackupFile(relativizeTarget(_targetDirectory, baseDir, managementFile.toString()));
					_completed++;
				}
				catch(IOException e) {
					_failed++;
					_debug.error("Fehler beim Sichern der Verwaltungdaten.xml", e);
				}

				// Die Benutzerverwaltung sichern, falls vorhanden
				if(_configAuthentication != null) {
					try {
						_configAuthentication.createBackupFile(relativizeTarget(_targetDirectory, baseDir, _configAuthentication.toString()));
						_completed++;
					}
					catch(IOException e) {
						_failed++;
						_debug.error("Fehler beim Sichern der Benutzerverwaltung.xml", e);
					}
				}
			}
			finally {
				// Den Publisher signalisieren, dass das Backup fertig ist, damit dieser aufhört periodisch Daten zu senden
				// und eine eventuelle Fertig-Meldung abschicken kann.
				if(publisher != null) publisher.cancel();
			}
		}

		/**
		 * Berechnet ein korrektes Zielverzeichnis. Der Parameter file wird relativ zu basedir aufgefasst und in
		 * basedir eingefügt. Der Dateiname wird weggelassen, sodass nur das Zielverzeichnis übrig bleibt.
		 * Beispiel:
		 * targetdir = "/foo/"
		 * basedir = "/abc/def/"
		 * file = "/abc/def/ghi/j.dat"
		 * Das Ergebnis ist nun "/foo/ghi/"
		 * @param targetDirectory
		 * @param baseDir
		 * @param file
		 * @return
		 */
		private File relativizeTarget(final File targetDirectory, final URI baseDir, final String file) {
			URI relativeFileURI = ManagementFile.relativize(baseDir, new File(file).toURI());
			if(relativeFileURI.isAbsolute()) {
				_debug.warning("Problem beim Backup: Kann Datei " + file + " nicht relativ zum Verzeichnis " + baseDir + " auflösen.");
				return targetDirectory;
			}
			File result = new File(targetDirectory, relativeFileURI.toString()).getParentFile();
			if(!result.isDirectory() && !result.mkdirs()){
				_debug.warning("Problem beim Backup: Kann Verzeichnis " + result + " nicht erstellen");
				return targetDirectory;
			}
			return result;			
		}

		/**
		 * Veröffentlicht den aktuellen Fortschritt über den Datenverteiler an einen RemoteRequestManager, der den Sicherungsauftrag gestartet
		 * hat
		 */
		public class RemoteProgressPublisher extends TimerTask {

			/**
			 * Startet den periodischen Versand von Fortschrittsmeldungen
			 */
			@Override
			public void run() {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);
					serializer.writeInt(BACKUP_STATE_INPROGRESS);
					serializer.writeLong(_completed);
					serializer.writeLong(_failed);
					serializer.writeLong(_total);
					serializer.writeDouble(getFileProgress());
					serializer.writeDouble(getOverallProgress());
					_sender.sendData("AntwortBackupKonfigurationsdaten", byteArrayStream.toByteArray(), _queryIndex);
				}
				catch(Exception e) {
					_debug.info("Fehler beim Versenden einer Fortschrittsmeldung über das Backup", e);
				}
			}

			/**
			 * Signalisiert, dass der Backup-Vorgang fertig ist und stoppt den Timer. Sendet eine Antwort über das Netzwerk.
			 *
			 * @return Nicht von Bedeutung, normalerweise true
			 */
			@Override
			public boolean cancel() {
				boolean result = super.cancel();
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);
					serializer.writeInt(BACKUP_STATE_FINISHED);
					serializer.writeLong(_completed);
					serializer.writeLong(_failed);
					serializer.writeLong(_total);
					_sender.sendData("AntwortBackupKonfigurationsdaten", byteArrayStream.toByteArray(), _queryIndex);
				}
				catch(Exception e) {
					_debug.error("Fehler beim Versenden der Nachricht, dass das Backup fertig ist", e);
				}
				return result;
			}
		}

		/**
		 * Veröffentlicht den aktuellen Fortschritt an ein BackupProgressCallback-Objekt
		 */
		public class LocalProgressPublisher extends TimerTask {

			/**
			 * Startet den periodischen Versand von Fortschrittsmeldungen
			 */
			@Override
			public void run() {
				_callback.backupProgress(_completed, _failed, _total, getFileProgress(), getOverallProgress());
			}

			/**
			 * Signalisiert dem lokalen Callback, dass der Backup-Vorgang fertig ist und stoppt den Timer
			 *
			 * @return Nicht von Bedeutung, normalerweise true
			 */
			@Override
			public boolean cancel() {
				boolean result = super.cancel();
				_callback.backupProgress(_completed, _failed, _total, 1.0, 1.0);
				_callback.backupFinished(_completed, _failed, _total);
				return result;
			}
		}
	}
}
