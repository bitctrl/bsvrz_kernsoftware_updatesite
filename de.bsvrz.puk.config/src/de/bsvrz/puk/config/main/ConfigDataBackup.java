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

package de.bsvrz.puk.config.main;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.BackupProgressCallback;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Kommandozeilenprogramm zum Durchführen von Backups der Konfigurationsdateien. Das Programm ist mit den üblichen -benutzer= und -authentifizierung=-Parametern
 * zu starten, zusätzlich kann mit -dir= ein Zielverzeichnis angegeben werden, in das die Dateien gesichert werden sollen
 * und mit -kv ein Konfigurationsverantwortlicher angegeben werden, dessen Konfigurationsbereiche gesichert werden sollen (sonst wird alles gesichert). Das Zielverzeichnis sollte relativ
 * angegeben werden und muss sich in dem Verzeichnis befinden, dass von der Konfiguration für Sicherungen festgelegt wurde, bzw. das man beim Starten der
 * Konfiguration mit dem Parameter -sicherungsVerzeichnis angeben kann. Wenn kein Zielverzeichnis angegeben wird, wird anhand der aktuellen Zeit ein neues
 * Verzeichnis angelegt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 0 $
 */
public class ConfigDataBackup implements StandardApplication {

	private String _targetDir;
	private String _kv;

	public void parseArguments(final ArgumentList argumentList) throws Exception {
		_targetDir = argumentList.fetchArgument("-dir=").asString();
		_kv = argumentList.fetchArgument("-kv=").asString();
		if(argumentList.hasUnusedArguments()){
			usage();
			System.exit(1);
		}
	}

	private void usage() {
		System.err.println();   // Leerzeile
		System.err.println("Folgende Parameter werden unterstützt:");
		System.err.println("\t-benutzer=                Benutzer zur Authentifizierung");
		System.err.println("\t-authentifizierung=       Authentifizierungsdatei");
		System.err.println("\t-dir=                     (optional) Zielverzeichnis, in das die Dateien gesichert werden sollen");
		System.err.println("\t-kv=                      (optional) Konfigurationsverantwortlicher, dessen Konfigurationsdateien gesichert werden sollen");
		System.err.println();   // Leerzeile
		System.err.println("Das Zielverzeichnis sollte relativ"
		                   + " angegeben werden und muss sich in dem Verzeichnis befinden, dass von der Konfiguration für Sicherungen festgelegt wurde, bzw. das man beim Starten der"
		                   + " Konfiguration mit dem Parameter -sicherungsVerzeichnis angeben kann. Wenn kein Zielverzeichnis angegeben wird, wird anhand der aktuellen Zeit ein neues"
		                   + " Verzeichnis angelegt.");
	}

	public void initialize(final ClientDavInterface connection) throws Exception {
		DataModel dataModel = connection.getDataModel();
		ConfigurationAuthority configurationAuthority = null;
		if(_kv != null && _kv.length() != 0) {
			try {
				long id = Long.parseLong(_kv);
				SystemObject object = dataModel.getObject(id);
				if(object instanceof ConfigurationAuthority) {
					configurationAuthority = (ConfigurationAuthority) object;
				}
				else if(object == null) {
					System.err.println("Der angegebene Konfigurationsverantwortliche mit der id " + id + " wurde nicht gefunden.");
					System.exit(2);
				}
				else {
					System.err.println("Das angegebene Objekt " + object.getPid() + " ist kein Konfigurationsverantwortlicher.");
					System.exit(2);
				}
			} catch(NumberFormatException ignored){
				String pid = _kv;
				SystemObject object = dataModel.getObject(pid);
				if(object instanceof ConfigurationAuthority) {
					configurationAuthority = (ConfigurationAuthority) object;
				}
				else if(object == null) {
					System.err.println("Der angegebene Konfigurationsverantwortliche mit der pid " + pid + " wurde nicht gefunden.");
					System.exit(2);
				}
				else {
					System.err.println("Das angegebene Objekt " + object.getPid() + " ist kein Konfigurationsverantwortlicher.");
					System.exit(2);
				}
			}
		}
		System.out.println("Sicherungsvorgang gestartet...");
		if(_kv != null){
			System.out.println("Konfigurationsverantwortlicher: " + _kv);
		}
		dataModel.backupConfigurationFiles(_targetDir, configurationAuthority, new Callback());
		System.out.println("Sicherungsvorgang beendet.");
	}

	public static void main(String[] args) {
		System.out.println();
		System.out.println("Kommandozeilenprogramm zum Durchführen von Backups der Konfigurationsdateien");
		System.out.print("Verwendung: ");
		System.out.println("java " + ConfigDataBackup.class.getName() + " -benutzer=<benutzer> -authentifizierung=<datei> [-dir=<Zielverzeichnis>] [-kv=<Konfigurationsverantwortlicher>]");
		StandardApplicationRunner.run(new ConfigDataBackup(), args);
	}

	class Callback implements BackupProgressCallback {

		private boolean _init = true;

		private long _currentFile = 0;

		private long _currentChunks = 0;

		private long _currentChunksPainted = 0;

		private static final int PROGRESS_BAR_LENGTH = 100;

		public void backupStarted(final String path) {
			System.out.println("Zielverzeichnis: " + path);
		}

		public void backupProgress(final long completed, final long failed, final long total, final double fileProgress, final double overallProgress) {
			if(_init) {
				// In State.INITIALIZING ist noch nicht die Anzahl der Dateien bekannt.
				System.out.println(total + " Dateien werden gesichert.");
				_init = false;
			}

			while(completed + failed > _currentFile) {
				_currentChunks = PROGRESS_BAR_LENGTH;
				while(_currentChunksPainted < _currentChunks) {
					System.out.print("#");
					_currentChunksPainted++;
				}
				_currentFile++;
				System.out.println("");
				System.out.println("Gesichert: " + _currentFile + " von " + total + " Dateien. Fehler: " + failed);	
				_currentChunksPainted = 0;
			}

			_currentChunks = (long)(fileProgress * PROGRESS_BAR_LENGTH);

			while(_currentChunksPainted < _currentChunks) {
				System.out.print("#");
				_currentChunksPainted++;
			}
		}

		public void backupFinished(final long completed, final long failed, final long total) {
			System.out.println("");
			System.out.println("");
			System.out.println(completed + " von " + total + " Dateien wurden gesichert.");
			System.out.flush();
			System.exit(0);
		}
	}
}
