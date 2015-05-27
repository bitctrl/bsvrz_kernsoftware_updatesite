/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.impl.archive.filesaver;

import java.io.IOException;
import java.io.File;
import java.util.Collection;

/**
 * Dieses Interface stellt einem Archivsystem die M�glichkeit zur Verf�gung Dateien dauerhaft zu speichern. Die
 * Implementierung, die diese Aufgabe �bernimmt, wird auch als Sicherung bezeichnet.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
 */
public interface ArchiveFileSaver {

	/**
	 * Die Implementierung dieser Methode speichert eine Datei mit einem festgelegten Dateinamen auf einen Datentr�ger der
	 * Sicherung. Nachdem die Datei dauerhaft auf dem Datentr�ger gesichert wurde, wird die eindeutige
	 * Datentr�geridentifikation des Datentr�gers zur�ckgegeben.
	 *
	 * @param filename  Dateiname, unter dem die Datei gesichert werden muss
	 * @param directory Verzeichnis, unter dem die Datei zu finden ist, die gesichert werden soll
	 * @return Eindeutige Datentr�gerindentifikation, auf dem die zu sichernde Datei gesichert wurde
	 * @throws IOException Fehler beim speichern der Datei auf dem Datentr�ger der Sicherung oder beim Zugriff auf die zu
	 *                     sichernden Datei
	 */
	public int saveFile(String filename, File directory) throws IOException;

	/**
	 * Die Implementierung dieser Methode stellt eine Datei zur Verf�gung, die mit der Methode <code>saveFile</code> auf
	 * einen Datentr�ger der Sicherung gesichert wurde. Damit die Datei eindeutig zu identifizieren ist, wird sowohl die
	 * Identifizierung des Speichermediums, als auch der Name der Datei angegeben. Die Datei wird dem angegebenen
	 * Verzeichnis zur Verf�gung gestellt.
	 *
	 * @param filename         Name der Datei, die ben�tigt wird
	 * @param volumeID         Eindeutige Identifizierung des Datentr�gers, auf dem die ben�tigte Datei gespeichert wurde
	 *                         (volumeID > 0)
	 * @param restoreDirectory Verzeichnis, unter dem die Datei zur Verf�gung gestellt werden muss
	 * @throws IOException              Der Zugriff auf die Datei �ber das Speichermedium der Sicherung ist fehlgeschlagen
	 * @throws IllegalArgumentException Die angegebene Datei war auf dem angegebenen Datentr�ger nicht zu finden
	 * @throws BadVolumeException       Der angegebene Datentr�ger ist nicht mehr vorhanden oder nicht mehr im direkten
	 *                                  Zugriff der Sicherung
	 */
	public void restoreFile(String filename, int volumeID, File restoreDirectory) throws IOException, IllegalArgumentException, BadVolumeException;

	/**
	 * Die Implementierung dieser Methode stellt eine Liste mit den eindeutigen Identifizierungen aller Datentr�ger
	 * zusammen, die sich im direkten Zugriff der Sicherung befinden.
	 *
	 * @return Eindeutige Datentr�geridentifikation aller Datentr�ger, die sich im direkten Zugriff befinden
	 */
	public Collection<Integer> volumesDirectAccess();


	/**
	 * Die Implementierung dieser Methode gibt alle Dateinamen eines Speichermediums der Sicherung zur�ck. Die ausgw�hlten
	 * Dateien m�ssen mit der Methode <code>saveFile</code> der Sicherung �bergeben worden sein.
	 *
	 * @param volumeId Speichermedium, von dem alle Dateinamen angefordert werden
	 * @return Alle Dateinamen, die auf dem angegebenen Speichermedium gespeichert sind. Die Dateien m�ssen mit der Methode
	 *         <code>saveFile</code> der Sicherung �bergeben worden sein
	 * @throws IOException        Der physische Zugriff auf den Datentr�ger ist fehlgeschlagen
	 * @throws BadVolumeException Der angegebene Datentr�ger ist nicht mehr vorhanden oder nicht mehr im direkten Zugriff
	 *                            der Sicherung
	 */
	public Collection<String> getAllFilenames(int volumeId) throws IOException, BadVolumeException;
}
