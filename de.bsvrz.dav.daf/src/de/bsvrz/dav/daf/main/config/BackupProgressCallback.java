/*
 * Copyright 2010 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.config;
/**
 * Schnittstelle die f�r Statusmeldungen des Sicherungs-Vorgangs f�r Konfigurationsdateien benutzt wird.
 * @see de.bsvrz.puk.config.main.ConfigDataBackup
 * @see de.bsvrz.dav.daf.main.impl.config.request.RemoteRequestManager.RemoteRequester#backupConfigurationFiles(java.lang.String, BackupProgressCallback)
 * @author Kappich Systemberatung
 * @version $Revision: 0 $
 *
 */
public interface BackupProgressCallback {

	/**
	 * Wird aufgerufen, nachdem der Backup-Vorgang gestartet wurde.
	 * @param path Absolutes Zielverzeichnis innerhalb der Konfiguration, in der die Sicherung angelegt wird. Das Verzeichnis befindet sich auf dem System,
	 * auf dem die Konfiguration l�uft.
	 */
	public void backupStarted(final String path);

	/**
	 * Wird aufgerufen, um �ber den aktuellen Fortschritt des Backup-Vorgangs zu informieren. Diese Funktion wird mindestens etwa alle 10 Sekunden aufgerufen,
	 * au�erdem nachdem das Backup beendet wurde, direkt vor backupFinished().
	 * @param completed Anzahl der gesicherten Dateien
	 * @param failed Anzahl der nicht erfolgreich gesicherten Dateien
	 * @param total Anzahl der zu sichernden Dateien
	 * @param fileProgress Fortschritt der aktuellen Datei (0.0-1.0)
	 * @param overallProgress Fortschritt insgesamt (0.0-1.0)
	 */
	public void backupProgress(final long completed, final long failed, final long total, final double fileProgress, final double overallProgress);

	/**
	 * Wird aufgerufen, nachdem der Backup-Vorgang beendet wurde.
	 * @param completed Anzahl der gesicherten Dateien
	 * @param failed Anzahl der nicht erfolgreich gesicherten Dateien
	 * @param total Anzahl der zu sichernden Dateien
	 */
	public void backupFinished(final long completed, final long failed, final long total);
}
