/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.BackupProgressCallback;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Kommandozeilenprogramm zum Durchführen von Backups der Konfigurationsdateien. Das Programm ist mit den üblichen -benutzer= und -authentifizierung=-Parametern
 * zu starten, zusätzlich kann mit -dir= ein Zielverzeichnis angegeben werden, in das die Dateien gesichert werden sollen. Das Zielverzeichnis sollte relativ
 * angegeben werden und muss sich in dem Verzeichnis befinden, dass von der Konfiguration für Sicherungen festgelegt wurde, bzw. das man beim Starten der
 * Konfiguration mit dem Parameter -sicherungsVerzeichnis angeben kann. Wenn kein Zielverzeichnis angegeben wird, wird anhand der aktuellen Zeit ein neues
 * Verzeichnis angelegt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 0 $
 */
public class ConfigDataBackup implements StandardApplication {

	private String _targetDir;

	public void parseArguments(final ArgumentList argumentList) throws Exception {
		_targetDir = argumentList.fetchArgument("-dir=").asString();
		if(argumentList.hasUnusedArguments()){
			usage();
			System.exit(1);
		}
	}

	private void usage() {
		System.out.println();   // Leerzeile
		System.out.println("Folgende Parameter werden unterstützt:");
		System.out.println("\t-benutzer=                Benutzer zur Authentifizierung");
		System.out.println("\t-authentifizierung=       Authentifizierungsdatei");
		System.out.println("\t-dir=                     (optional) Zielverzeichnis, in das die Dateien gesichert werden sollen");
		System.out.println();   // Leerzeile
		System.out.println("Das Zielverzeichnis sollte relativ"
		                   + " angegeben werden und muss sich in dem Verzeichnis befinden, dass von der Konfiguration für Sicherungen festgelegt wurde, bzw. das man beim Starten der"
		                   + " Konfiguration mit dem Parameter -sicherungsVerzeichnis angeben kann. Wenn kein Zielverzeichnis angegeben wird, wird anhand der aktuellen Zeit ein neues"
		                   + " Verzeichnis angelegt.");
	}

	public void initialize(final ClientDavInterface connection) throws Exception {
		System.out.println("Sicherungsvorgang gestartet...");
		connection.getDataModel().backupConfigurationFiles(_targetDir, new Callback());
		System.out.println("Sicherungsvorgang beendet.");
	}

	public static void main(String[] args) {
		System.out.println();
		System.out.println("Kommandozeilenprogramm zum Durchführen von Backups der Konfigurationsdateien");
		System.out.print("Verwendung: ");
		System.out.println("java " + ConfigDataBackup.class.getName() + " -benutzer=<benutzer> -authentifizierung=<datei> [-dir=<Zielverzeichnis>]");
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
