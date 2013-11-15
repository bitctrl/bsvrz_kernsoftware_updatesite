/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.main.managementfile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Die Implementierung dieses Interfaces ist f�r die Verwaltungsdaten der Konfiguration zust�ndig.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface ConfigurationManagementFile {

	/**
	 * Gibt die Pid des Konfigurationsverantwortlichen der Konfiguration zur�ck.
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
	 * Gibt alle Eintr�ge �ber Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge aus den Verwaltungsdaten zur�ck.
	 *
	 * @return alle Konfigurationsbereiche der Konfiguration in der zu verwendenden Reihenfolge
	 */
	List<ConfigurationAreaManagementInfo> getAllConfigurationAreaManagementInfos();

	/**
	 * Gibt einen Eintrag aus den Verwaltungsdaten zu der angegebenen Pid eines Konfigurationsbereichs zur�ck.
	 *
	 * @param configurationAreaPid Pid eines Konfigurationsbereichs
	 *
	 * @return Eintrag aus den Verwaltungsdaten zu einem Konfigurationsbereich, oder <code>null</code>, falls es keinen Eintrag gibt.
	 */
	ConfigurationAreaManagementInfo getConfigurationAreaManagementInfo(String configurationAreaPid);

	/**
	 * F�gt einen neuen Eintrag eines Konfigurationsbereichs ans Ende der Verwaltungsdatei ein.
	 *
	 * @param pid Pid des Konfigurationsbereichs, welcher zu den Verwaltungsdaten hinzugef�gt werden soll
	 *
	 * @return der Eintrag in den Verwaltungsdaten zu einem Konfigurationsbereich
	 */
	ConfigurationAreaManagementInfo addConfigurationAreaManagementInfo(String pid);

	/** Speichert die Verwaltungsdaten persistent. */
	void save() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren wird. Es m�ssen alle Daten persistent gespeichert werden und es muss
	 * ein Zustand hergestellt werden, dass das System sp�ter erneut gestartet werden kann (Tempor�re Dateien l�schen, usw.).
	 *
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Bestimmt das Verzeichnis in dem die Dateien mit den Elementzugeh�rigkeiten von dynamischen Mengen gespeichert werden sollen.
	 * Falls das Verzeichnis noch nich existiert, wird es erzeugt.
	 * @return Verzeichnis in dem die Dateien mit den Elementzugeh�rigkeiten von dynamischen Mengen gespeichert werden sollen.
	 */
	File getObjectSetDirectory();

	/**
	 * Bestimmt die Datei, in der dynamische Objekte, die von fremden Konfigurationen abgefragt wurden, persistent gespeichert werden.
	 * @return Datei, in der dynamische Objekte, die von fremden Konfigurationen abgefragt wurden, persistent gespeichert werden.
	 */
	File getForeignObjectCacheFile();
}
