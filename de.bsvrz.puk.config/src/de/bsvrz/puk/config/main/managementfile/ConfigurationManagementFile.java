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
import java.io.IOException;
import java.util.*;

/**
 * Die Implementierung dieses Interfaces ist für die Verwaltungsdaten der Konfiguration zuständig.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface ConfigurationManagementFile {

	/**
	 * Gibt die Pid des Konfigurationsverantwortlichen der Konfiguration zurück.
	 *
	 * @return Die Pid des Konfigurationsverantwortlichen der Konfiguration.
	 */
	String getConfigurationAuthority();

	/**
	 * Setzt die Pid des Konfigurationsverantwortlichen der Konfiguration.
	 *
	 * @param pid die Pid des Konfigurationsverantwortlichen
	 */
	void setConfigurationAuthority(String pid);

	/**
	 * Gibt alle Einträge über Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge aus den Verwaltungsdaten zurück.
	 *
	 * @return alle Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge
	 */
	List<ConfigurationAreaManagementInfo> getAllConfigurationAreaManagementInfos();

	/**
	 * Gibt einen Eintrag aus den Verwaltungsdaten zu der angegebenen Pid eines Konfigurationsbereichs zurück.
	 *
	 * @param configurationAreaPid Pid eines Konfigurationsbereichs
	 *
	 * @return Eintrag aus den Verwaltungsdaten zu einem Konfigurationsbereich, oder <code>null</code>, falls es keinen Eintrag gibt.
	 */
	ConfigurationAreaManagementInfo getConfigurationAreaManagementInfo(String configurationAreaPid);

	/**
	 * Fügt einen neuen Eintrag eines Konfigurationsbereichs ans Ende der Verwaltungsdatei ein.
	 *
	 * @param pid Pid des Konfigurationsbereichs, welcher zu den Verwaltungsdaten hinzugefügt werden soll
	 *
	 * @return der Eintrag in den Verwaltungsdaten zu einem Konfigurationsbereich
	 */
	ConfigurationAreaManagementInfo addConfigurationAreaManagementInfo(String pid);

	/** Speichert die Verwaltungsdaten persistent. */
	void save() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren wird. Es müssen alle Daten persistent gespeichert werden und es muss
	 * ein Zustand hergestellt werden, dass das System später erneut gestartet werden kann (Temporäre Dateien löschen, usw.).
	 *
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Bestimmt das Verzeichnis in dem die Dateien mit den Elementzugehörigkeiten von dynamischen Mengen gespeichert werden sollen.
	 * Falls das Verzeichnis noch nich existiert, wird es erzeugt.
	 * @return Verzeichnis in dem die Dateien mit den Elementzugehörigkeiten von dynamischen Mengen gespeichert werden sollen.
	 */
	File getObjectSetDirectory();

	/**
	 * Bestimmt die Datei, in der dynamische Objekte, die von fremden Konfigurationen abgefragt wurden, persistent gespeichert werden.
	 * @return Datei, in der dynamische Objekte, die von fremden Konfigurationen abgefragt wurden, persistent gespeichert werden.
	 */
	File getForeignObjectCacheFile();
}
