/*
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

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Dieses Interface stellt eine Verwaltung f�r alle Konfigurationsdateien dar und erm�glicht den Zugriff auf diese. Die beschriebenen get-Methoden beziehen sich
 * immer auf alle Konfigurationsdateien, die sich im Zugriff der Verwaltung befinden. Aus diesem Grund speichert die Verwaltung die aktiven Objekte zentral in
 * einer Datenstruktur und deligiert n�tige Zugriffe auf die Datei an das jeweilige ConfigurationAreaFile-Objekt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5079 $ / $Date: 2007-09-02 14:59:08 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
 */
public interface ConfigurationFileManager {

	/**
	 * Diese Methode erstellt zu einem neuen Konfigurationsbereich eine Konfigurationsdatei und f�gt den Bereich mit {@link #addAreaFile} den bestehenden Bereichen
	 * hinzu. Der neue Konfigurationsbereich erh�lt den Zustand inaktiv. Soll er von der Konfiguration genutzt werden k�nnen, so muss er aktiviert werden.
	 *
	 * @param configurationAreaPid die Pid des neuen Konfigurationsbereichs
	 * @param configurationAreaDir das Verzeichnis, in dem die Konfigurationsdatei angelegt werden soll
	 *
	 * @return der neue Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls die Argumente ung�ltig sind.
	 * @throws IOException              Falls Fehler im Zusammenhang mit der Datei des Konfigurationsbereichs auftreten.
	 * @throws NoSuchVersionException   TBD
	 */
	ConfigurationAreaFile createAreaFile(String configurationAreaPid, File configurationAreaDir)
			throws IllegalArgumentException, IOException, NoSuchVersionException;

	/**
	 * Der aktuellen Konfiguration wird der angegebene Konfigurationsbereich hinzugef�gt. Gibt es bereits einen Konfigurationsbereich mit der angegebenen Pid, wird
	 * eine Fehlermeldung erzeugt.
	 *
	 * @param configurationAreaPid die Pid des Konfigurationsbereichs
	 * @param configurationAreaDir Verzeichnis, in dem die Konfigurationsdatei gespeichert wurde
	 * @param activeVersion        die aktuelle Version des Konfigurationsbereichs
	 * @param localVersionTimes    Diese Liste speichert zu jeder Version, die jemals aktiviert wurde, den Zeitpunkt an dem die Version aktiviert wurde. Die
	 *                             Zeitpunkte beziehen sich auf die Zeit, an dem sie auf der Konfiguration, die diese Methode aufruft, aktiviert wurden.
	 *
	 * @return der hinzugef�gte Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls der Konfigurationsbereich mit der Pid bereits zur Konfiguration hinzugef�gt wurde.
	 * @throws IllegalStateException    Die Datei, die den Konfigurationsbereich enth�lt, existiert nicht.
	 * @throws IOException              TBD
	 * @throws NoSuchVersionException   TBD
	 */
	ConfigurationAreaFile addAreaFile(String configurationAreaPid, File configurationAreaDir, short activeVersion, List<VersionInfo> localVersionTimes)
			throws IllegalArgumentException, IOException, NoSuchVersionException;

	/**
	 * Diese Methode gibt ein Objekt zur�ck, das den Konfigurationsbereich darstellt.
	 *
	 * @param configurationAreaPid Pid des Konfigurationsbereichs
	 *
	 * @return Objekt, mit dem auf den Konfigurationsbereich zugegriffen werden kann. Ist kein Objekt vorhanden, wird <code>null</code> zur�ckgegeben.
	 */
	ConfigurationAreaFile getAreaFile(String configurationAreaPid);

	/**
	 * Gibt ein Objekt zur�ck, das �ber die Id identifiziert wird. Es werden alle Konfigurationsbereiche betrachtet, die mit {@link #addAreaFile} hinzugef�gt
	 * wurden.
	 *
	 * @param id Id des Objekts
	 *
	 * @return Objekt, das angefordert wurde oder <code>null</code> falls kein Objekt gefunden werden konnte
	 */
	SystemObjectInformationInterface getObject(long id);
	/**
	 * TBD Diese Methode wurde entfernt, da die �bergeordnete vielleicht selber gar nicht weiss, ob das Objekt
	 * aktiv, ung�ltig oder in Zukunft aktuell ist/wird.
	 *
	 * Diese Methode gibt ein Objekt zur�ck, das derzeit in einem Konfigurationsbereich aktiv ist. Es werden alle
	 * Konfigurationsbreiche gepr�ft, die mit {@link #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param id Id des Objekts, das gesucht werden soll
	 * @return Objekt, dessen Id �bergeben wurde
	 * @throws IllegalArgumentException Zur angegebenen Id konnte kein Objekt gefunden werden
	 */
	// SystemObjectInfo getActiveObject(long id) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt ein Objekt zur�ck, das derzeit in einem Konfigurationsbereich aktiv ist. Es werden alle Konfigurationsbreiche gepr�ft, die mit {@link
	 * #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param pid Pid des Objekts, das gesucht werden soll
	 *
	 * @return Objekt, dessen Pid �bergeben wurde oder <code>null</code> falls kein Objekt existiert
	 */
	SystemObjectInformationInterface getActiveObject(String pid);

	/**
	 * Diese Methode gibt ein Objekt zur�ck, das derzeit in einem Konfigurationsbereich weder aktiv noch als ung�ltig
	 * markiert ist. Es werden alle Konfigurationsbreiche gepr�ft, die mit {@link #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param id Id des gesuchten Objekts
	 * @return das gesuchte Objekt
	 * @throws IllegalArgumentException Zur angegebenen Id konnte kein Objekt gefunden werden
	 */
	// SystemObjectInfo getNewObjects(long id) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt alle Objekte zur�ck, die derzeit in einem Konfigurationsbereich weder aktiv noch als ung�ltig markiert sind. Es werden alle
	 * Konfigurationsbereiche gepr�ft, die mit {@link #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param pid pid der gesuchten Objekte
	 *
	 * @return Alle Objekte, deren Pid mit der �bergebenen Pid �bereinstimmt. Konnte kein Objekt gefunden werden, wird eine leeres Array zur�ckgegeben
	 */
	SystemObjectInformationInterface[] getNewObjects(String pid) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt ein Objekt zur�ck, das in einem Konfigurationsbereich als ung�ltig markiert ist. Es werden alle
	 * Konfigurationsbreiche gepr�ft, die mit {@link #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param id id des gesuchten Objekts
	 * @return das gesuchte Objekt
	 * @throws IllegalArgumentException Zur angegebenen Id konnte kein Objekt gefunden werden
	 * @throws IllegalStateException Das Objekt zu der angegebenen Id kann nicht wiederhergestellt werden
	 */
	// SystemObjectInfo getObjects(long id) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt alle Objekte zur�ck, die in einem Konfigurationsbereich als ung�ltig markiert sind und die im angegebnen Zeitbereich ung�ltig geworden
	 * sind. Es werden alle Konfigurationsbreiche gepr�ft, die mit {@link #addAreaFile} hinzugef�gt wurden.
	 *
	 * @param pid       pid des gesuchten Objekts
	 * @param startTime Startzeitpunkt des Bereichs
	 * @param endTime   Endzeitpunkt des Bereichs
	 *
	 * @return die gesuchten Objekte
	 *
	 * @throws IllegalArgumentException Zur angegebenen Pid konnte kein Objekt gefunden werden
	 */
	SystemObjectInformationInterface[] getOldObjects(String pid, long startTime, long endTime) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt alle Konfigurationsbereiche zur�ck, die mit {@link #addAreaFile} eingef�gt wurden.
	 *
	 * @return s.o.
	 */
	ConfigurationAreaFile[] getConfigurationAreas();

	/**
	 * Diese Methode gibt alle dynamischen Objekte zur�ck, die zu einer Simulationsvariante geh�ren. Der Zustand der dynamischen Objekte (g�ltig/ung�ltig) wird
	 * dabei nicht ber�cksichtigt. Die Simulationsvariante muss dabei gr��er 0 sein.
	 *
	 * @param simulationVariant Simulationsvariante, f�r die alle dynamischen Objekte zur�ckgegeben werden sollen. (Wertebereich 1...999)
	 *
	 * @return Liste mit dynamischen Objekten. Sind keine Objekte vorhande, wird eine leere Liste zur�ckgegeben.
	 *
	 * @throws IllegalStateException Der Wert der Simulationsvariante ist nicht 1...999
	 */
	List<DynamicObjectInfo> getObjects(short simulationVariant) throws IllegalArgumentException;

	/**
	 * Diese Methode sichert alle Konfigurationsbereiche, die mit {@link #addAreaFile} �bergeben wurden. In den einzelnen Konfigurationsbereichen werden alle
	 * �nderungen, die an Objekten eines Konfigurationsbereichs vorgenommen wurden, persistent in die daf�r vorgesehene Datei gespeichert.
	 *
	 * @throws IOException Es ist beim speichern der Versorgungsdateien zu einem Fehler gekommen. Es wurde trotz des Fehlers bei jeder Datei {@link
	 *                     ConfigurationAreaFile#flush()} aufgerufen. Die zuletzt aufgetretene IOException
	 *                     wird zur�ckgegeben.
	 */
	void saveConfigurationAreaFiles() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn ein System heruntegefahren werden soll. Sie speichert alle Konfigurationsbereiche (siehe {@link
	 * #saveConfigurationAreaFiles} und stellt einen Zustand her in dem das System wieder gestartet werden kann.
	 */
	void close();
}
