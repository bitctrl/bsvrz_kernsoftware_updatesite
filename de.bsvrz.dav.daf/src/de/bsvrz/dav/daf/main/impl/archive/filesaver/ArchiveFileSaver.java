/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.impl.archive.filesaver;

import java.io.IOException;
import java.io.File;
import java.util.Collection;

/**
 * Dieses Interface stellt einem Archivsystem die Möglichkeit zur Verfügung Dateien dauerhaft zu speichern. Die
 * Implementierung, die diese Aufgabe übernimmt, wird auch als Sicherung bezeichnet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ArchiveFileSaver {

	/**
	 * Die Implementierung dieser Methode speichert eine Datei mit einem festgelegten Dateinamen auf einen Datenträger der
	 * Sicherung. Nachdem die Datei dauerhaft auf dem Datenträger gesichert wurde, wird die eindeutige
	 * Datenträgeridentifikation des Datenträgers zurückgegeben.
	 *
	 * @param filename  Dateiname, unter dem die Datei gesichert werden muss
	 * @param directory Verzeichnis, unter dem die Datei zu finden ist, die gesichert werden soll
	 * @return Eindeutige Datenträgerindentifikation, auf dem die zu sichernde Datei gesichert wurde
	 * @throws IOException Fehler beim speichern der Datei auf dem Datenträger der Sicherung oder beim Zugriff auf die zu
	 *                     sichernden Datei
	 */
	public int saveFile(String filename, File directory) throws IOException;

	/**
	 * Die Implementierung dieser Methode stellt eine Datei zur Verfügung, die mit der Methode <code>saveFile</code> auf
	 * einen Datenträger der Sicherung gesichert wurde. Damit die Datei eindeutig zu identifizieren ist, wird sowohl die
	 * Identifizierung des Speichermediums, als auch der Name der Datei angegeben. Die Datei wird dem angegebenen
	 * Verzeichnis zur Verfügung gestellt.
	 *
	 * @param filename         Name der Datei, die benötigt wird
	 * @param volumeID         Eindeutige Identifizierung des Datenträgers, auf dem die benötigte Datei gespeichert wurde
	 *                         (volumeID > 0)
	 * @param restoreDirectory Verzeichnis, unter dem die Datei zur Verfügung gestellt werden muss
	 * @throws IOException              Der Zugriff auf die Datei über das Speichermedium der Sicherung ist fehlgeschlagen
	 * @throws IllegalArgumentException Die angegebene Datei war auf dem angegebenen Datenträger nicht zu finden
	 * @throws BadVolumeException       Der angegebene Datenträger ist nicht mehr vorhanden oder nicht mehr im direkten
	 *                                  Zugriff der Sicherung
	 */
	public void restoreFile(String filename, int volumeID, File restoreDirectory) throws IOException, IllegalArgumentException, BadVolumeException;

	/**
	 * Die Implementierung dieser Methode stellt eine Liste mit den eindeutigen Identifizierungen aller Datenträger
	 * zusammen, die sich im direkten Zugriff der Sicherung befinden.
	 *
	 * @return Eindeutige Datenträgeridentifikation aller Datenträger, die sich im direkten Zugriff befinden
	 */
	public Collection<Integer> volumesDirectAccess();


	/**
	 * Die Implementierung dieser Methode gibt alle Dateinamen eines Speichermediums der Sicherung zurück. Die ausgwählten
	 * Dateien müssen mit der Methode <code>saveFile</code> der Sicherung übergeben worden sein.
	 *
	 * @param volumeId Speichermedium, von dem alle Dateinamen angefordert werden
	 * @return Alle Dateinamen, die auf dem angegebenen Speichermedium gespeichert sind. Die Dateien müssen mit der Methode
	 *         <code>saveFile</code> der Sicherung übergeben worden sein
	 * @throws IOException        Der physische Zugriff auf den Datenträger ist fehlgeschlagen
	 * @throws BadVolumeException Der angegebene Datenträger ist nicht mehr vorhanden oder nicht mehr im direkten Zugriff
	 *                            der Sicherung
	 */
	public Collection<String> getAllFilenames(int volumeId) throws IOException, BadVolumeException;
}
