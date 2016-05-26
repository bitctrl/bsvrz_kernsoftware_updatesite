/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.main.managementfile;

import java.io.File;
import java.util.*;

/**
 * Ein Eintrag in den Verwaltungsinformationen der Konfiguration. Für jeden Konfigurationsbereich wird ein Eintrag erstellt. Zu jedem Eintrag wird die Pid des
 * Konfigurationsbereichs, der Speicherort der entsprechenden Konfigurationsbereichsdatei und die Versionsnummern mit ihren Aktivierungszeiten, gespeichert.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface ConfigurationAreaManagementInfo {

	/**
	 * Gibt die Pid des Konfigurationsbereichs eines Verwaltungseintrages zurück.
	 *
	 * @return die Pid des Konfigurationsbereichs
	 */
	String getPid();

	/**
	 * Gibt den Speicherort (Verzeichnis) der Datei des Konfigurationsbereichs zurück.
	 *
	 * @return Der Speicherort (Verzeichnis) der Konfigurationsbereichsdatei oder <code>null</code> wenn das Verzeichnis nicht ermittelt werden kann.
	 */
	File getDirectory();

	/**
	 * Setzt den Speicherort (das Verzeichnis) der Datei des Konfigurationsbereichs.
	 *
	 * @param directory der Speicherort (das Verzeichnis) der Konfigurationsbereichsdatei
	 */
	void setDirectory(File directory);

	/**
	 * Es wird die Versionsnummer angegeben, in welche dieser Konfigurationsbereich beim Neustart der Konfiguration überführt werden soll. Die erste zu
	 * aktivierende Version muss größer gleich 1 sein.
	 *
	 * @param nextActiveVersion Die nächste aktive Version. Sie muss größer sein, als die bisherigen aktiven Versionsnummern.
	 */
	void setNextActiveVersion(short nextActiveVersion);

	/**
	 * Gibt an, ob beim Laden der Verwaltungsinformationen, dieser Konfigurationsbereich in eine neue Version überführt wurde.
	 *
	 * @return <code>true</code>, wenn dieser Konfigurationsbereich in eine neue Version überführt wurde, sonst <code>false</code>.
	 */
	boolean isNewVersionActivated();

	/**
	 * Gibt die aktive Version des Konfigurationsbereichs und ihren Aktivierungszeitpunkt zurück.
	 *
	 * @return die aktive Version und ihren Aktivierungszeitpunkt des Konfigurationsbereichs
	 */
	VersionInfo getActiveVersion();

	/**
	 * Gibt alle Versionseinträge zu diesem Konfigurationsbereich in einer Liste zurück.
	 *
	 * @return eine Liste aller Versionseinträge zu diesem Konfigurationsbereich
	 */
	List<VersionInfo> getVersions();

	/**
	 * Gibt die Position innerhalb aller Konfigurationsbereiche in den Verwaltungsdaten zurück. (siehe auch TPuK1-99 Reihenfolge der Bereiche)
	 *
	 * @return Position innerhalb der Konfigurationsbereiche
	 */
	int getPosition();

	/**
	 * Mit dieser Methode soll die Reihenfolge der Konfigurationsbereiche verändert werden können. Der angegebene Wert gibt die Position zwischen 1 und der Anzahl
	 * aller eingetragenen Konfigurationsbereiche an. Andere Werte sind nicht erlaubt. (siehe auch TPuK1-99 Reihenfolge der Bereiche)
	 *
	 * @param position Position innerhalb der Konfigurationsbereiche
	 */
	void setPosition(int position);
}
