/*
 * Copyright 2010 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.config;
/**
 * Schnittstelle die für Statusmeldungen des Sicherungs-Vorgangs für Konfigurationsdateien benutzt wird.
 * @see de.bsvrz.puk.config.main.ConfigDataBackup
 * @see de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester#backupConfigurationFiles(String, ConfigurationAuthority, BackupProgressCallback)
 * @author Kappich Systemberatung
 * @version $Revision$
 *
 */
public interface BackupProgressCallback {

	/**
	 * Wird aufgerufen, nachdem der Backup-Vorgang gestartet wurde.
	 * @param path Absolutes Zielverzeichnis innerhalb der Konfiguration, in der die Sicherung angelegt wird. Das Verzeichnis befindet sich auf dem System,
	 * auf dem die Konfiguration läuft.
	 */
	public void backupStarted(final String path);

	/**
	 * Wird aufgerufen, um über den aktuellen Fortschritt des Backup-Vorgangs zu informieren. Diese Funktion wird mindestens etwa alle 10 Sekunden aufgerufen,
	 * außerdem nachdem das Backup beendet wurde, direkt vor backupFinished().
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
