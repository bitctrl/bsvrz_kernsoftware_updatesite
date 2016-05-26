/*
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

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Dieses Interface stellt eine Verwaltung für alle Konfigurationsdateien dar und ermöglicht den Zugriff auf diese. Die beschriebenen get-Methoden beziehen sich
 * immer auf alle Konfigurationsdateien, die sich im Zugriff der Verwaltung befinden. Aus diesem Grund speichert die Verwaltung die aktiven Objekte zentral in
 * einer Datenstruktur und deligiert nötige Zugriffe auf die Datei an das jeweilige ConfigurationAreaFile-Objekt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ConfigurationFileManager {

	/**
	 * Diese Methode erstellt zu einem neuen Konfigurationsbereich eine Konfigurationsdatei und fügt den Bereich mit {@link #addAreaFile} den bestehenden Bereichen
	 * hinzu. Der neue Konfigurationsbereich erhält den Zustand inaktiv. Soll er von der Konfiguration genutzt werden können, so muss er aktiviert werden.
	 *
	 * @param configurationAreaPid die Pid des neuen Konfigurationsbereichs
	 * @param configurationAreaDir das Verzeichnis, in dem die Konfigurationsdatei angelegt werden soll
	 *
	 * @return der neue Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls die Argumente ungültig sind.
	 * @throws IOException              Falls Fehler im Zusammenhang mit der Datei des Konfigurationsbereichs auftreten.
	 * @throws NoSuchVersionException   TBD
	 */
	ConfigurationAreaFile createAreaFile(String configurationAreaPid, File configurationAreaDir)
			throws IllegalArgumentException, IOException, NoSuchVersionException;

	/**
	 * Der aktuellen Konfiguration wird der angegebene Konfigurationsbereich hinzugefügt. Gibt es bereits einen Konfigurationsbereich mit der angegebenen Pid, wird
	 * eine Fehlermeldung erzeugt.
	 *
	 * @param configurationAreaPid die Pid des Konfigurationsbereichs
	 * @param configurationAreaDir Verzeichnis, in dem die Konfigurationsdatei gespeichert wurde
	 * @param activeVersion        die aktuelle Version des Konfigurationsbereichs
	 * @param localVersionTimes    Diese Liste speichert zu jeder Version, die jemals aktiviert wurde, den Zeitpunkt an dem die Version aktiviert wurde. Die
	 *                             Zeitpunkte beziehen sich auf die Zeit, an dem sie auf der Konfiguration, die diese Methode aufruft, aktiviert wurden.
	 *
	 * @return der hinzugefügte Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls der Konfigurationsbereich mit der Pid bereits zur Konfiguration hinzugefügt wurde.
	 * @throws IllegalStateException    Die Datei, die den Konfigurationsbereich enthält, existiert nicht.
	 * @throws IOException              TBD
	 * @throws NoSuchVersionException   TBD
	 */
	ConfigurationAreaFile addAreaFile(String configurationAreaPid, File configurationAreaDir, short activeVersion, List<VersionInfo> localVersionTimes)
			throws IllegalArgumentException, IOException, NoSuchVersionException;

	/**
	 * Diese Methode gibt ein Objekt zurück, das den Konfigurationsbereich darstellt.
	 *
	 * @param configurationAreaPid Pid des Konfigurationsbereichs
	 *
	 * @return Objekt, mit dem auf den Konfigurationsbereich zugegriffen werden kann. Ist kein Objekt vorhanden, wird <code>null</code> zurückgegeben.
	 */
	ConfigurationAreaFile getAreaFile(String configurationAreaPid);

	/**
	 * Gibt ein Objekt zurück, das über die Id identifiziert wird. Es werden alle Konfigurationsbereiche betrachtet, die mit {@link #addAreaFile} hinzugefügt
	 * wurden.
	 *
	 * @param id Id des Objekts
	 *
	 * @return Objekt, das angefordert wurde oder <code>null</code> falls kein Objekt gefunden werden konnte
	 */
	SystemObjectInformationInterface getObject(long id);

	/**
	 * Diese Methode gibt ein Objekt zurück, das derzeit in einem Konfigurationsbereich aktiv ist. Es werden alle Konfigurationsbreiche geprüft, die mit {@link
	 * #addAreaFile} hinzugefügt wurden. Objekte, die nur in Simulationen gültig sind werden hier nicht zurückgegeben,
	 * stattdessen ist {@link #getSimulationObject(String, short)} zu benutzen.
	 *
	 * @param pid Pid des Objekts, das gesucht werden soll
	 *
	 * @return Objekt, dessen Pid übergeben wurde oder <code>null</code> falls kein Objekt existiert
	 */
	SystemObjectInformationInterface getActiveObject(String pid);

	/**
	 * Gibt ein simulationsspezifisches Objekt anhand der Pid zurück. Es werden nur Objekte zurückgegeben, die in einer Simulation erzeugt
	 * wurden und damit auch nur in dieser Simulation gültig sind.
	 * @param pid Pid
	 * @param simulationVariant Simulationsvariante
	 * @return Objekt, dessen Pid übergeben wurde oder <code>null</code> falls kein Objekt existiert
	 */
	SystemObjectInformationInterface getSimulationObject(String pid, short simulationVariant);

	/**
	 * Diese Methode gibt alle Objekte zurück, die derzeit in einem Konfigurationsbereich weder aktiv noch als ungültig markiert sind. Es werden alle
	 * Konfigurationsbereiche geprüft, die mit {@link #addAreaFile} hinzugefügt wurden.
	 *
	 * @param pid pid der gesuchten Objekte
	 *
	 * @return Alle Objekte, deren Pid mit der übergebenen Pid übereinstimmt. Konnte kein Objekt gefunden werden, wird eine leeres Array zurückgegeben
	 */
	SystemObjectInformationInterface[] getNewObjects(String pid) throws IllegalArgumentException;

	/**
	 * Diese Methode gibt alle Konfigurationsbereiche zurück, die mit {@link #addAreaFile} eingefügt wurden.
	 *
	 * @return s.o.
	 */
	ConfigurationAreaFile[] getConfigurationAreas();

	/**
	 * Diese Methode gibt alle dynamischen Objekte zurück, die zu einer Simulationsvariante gehören. Der Zustand der dynamischen Objekte (gültig/ungültig) wird
	 * dabei nicht berücksichtigt. Die Simulationsvariante muss dabei größer 0 sein.
	 *
	 * @param simulationVariant Simulationsvariante, für die alle dynamischen Objekte zurückgegeben werden sollen. (Wertebereich 1...999)
	 *
	 * @return Liste mit dynamischen Objekten. Sind keine Objekte vorhande, wird eine leere Liste zurückgegeben.
	 *
	 * @throws IllegalStateException Der Wert der Simulationsvariante ist nicht 1...999
	 */
	List<DynamicObjectInfo> getObjects(short simulationVariant) throws IllegalArgumentException;

	/**
	 * Diese Methode sichert alle Konfigurationsbereiche, die mit {@link #addAreaFile} übergeben wurden. In den einzelnen Konfigurationsbereichen werden alle
	 * Änderungen, die an Objekten eines Konfigurationsbereichs vorgenommen wurden, persistent in die dafür vorgesehene Datei gespeichert.
	 *
	 * @throws IOException Es ist beim speichern der Versorgungsdateien zu einem Fehler gekommen. Es wurde trotz des Fehlers bei jeder Datei {@link
	 *                     ConfigurationAreaFile#flush()} aufgerufen. Die zuletzt aufgetretene IOException
	 *                     wird zurückgegeben.
	 */
	void saveConfigurationAreaFiles() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn ein System heruntegefahren werden soll. Sie speichert alle Konfigurationsbereiche (siehe {@link
	 * #saveConfigurationAreaFiles} und stellt einen Zustand her in dem das System wieder gestartet werden kann.
	 */
	void close();
}
