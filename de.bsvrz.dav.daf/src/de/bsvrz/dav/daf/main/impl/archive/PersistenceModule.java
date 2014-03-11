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

package de.bsvrz.dav.daf.main.impl.archive;


import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.impl.archive.filesaver.BadVolumeException;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

import java.io.IOException;
import java.util.*;

/**
 * Ein Objekt, das dieses Interface implementiert, stellt einem Archivsystem das sogenannte "PersistenceModule" zur
 * Verf�gung. Die Aufgabe des "PersistenceModule" ist es, Datens�tze zu speichern und dieser einer Verwaltung zur
 * Verf�gung zu stellen.
 * <p/>
 * Eine weitere Aufgabe des "PersistenceModule" ist es, die gespeicherten Datens�tze persistent zu speichern. Diese
 * Aufgabe wird von einer Implementation des Interface {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} �bernommen, diese
 * wird auch als Sicherung bezeichnet.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5084 $ / $Date: 2007-09-03 10:42:50 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 */
public interface PersistenceModule {

	/**
	 * Diese Methode wird in der Initialisierungsphase aufgerufen um dem Persistenzmodul die M�glichkeit zu geben,
	 * Aufrufparameter der Applikation zu lesen und zu interpretieren.
	 * <p/>
	 *
	 * @param argumentList Aufrufparameter der Applikation.
	 * @throws ClassNotFoundException Die angegebene Klasse, die das PersistenceModule implementieren soll, konnte nicht
	 *                                gefunden werden
	 * @throws IllegalAccessException Die angegebene Klasse, die das PersistenceModule implementieren soll, ist nicht im
	 *                                Zugriff der aufrufenden Klasse
	 * @throws InstantiationException Die angegebene Klasse, die das PersistenceModule implementieren soll, kann nicht
	 *                                instanziiert werden
	 */
	void parseArguments(ArgumentList argumentList) throws ClassNotFoundException, IllegalAccessException, InstantiationException;

	/**
	 * Gibt ein Objekt zur�ck, �ber das auf einen Archivdatensatzcontainer zugegriffen werden kann. Das zur�ckgegebene
	 * Objekt erm�glicht den Zugriff auf die ID des Container und auf die identifizierenden Informationen des Containers
	 * (Systemobjekt, Attributgruppe, Aspekt, Simulationsvariante und Datensatzart). Wesentliche Funktionen des
	 * Persistenzmoduls (z.B. das Archivieren von Online empfangenen Datens�tzen) sind �ber entsprechende Methoden des
	 * zur�ckgegebenen Containerobjekts erreichbar.
	 *
	 * @param containerId Eindeutige Nummer des Containers.
	 * @return Archivdatensatzcontainer.
	 * @throws IOException           Der Container ist auf dem benutzten Speichermedium nicht mehr vorhanden (gel�scht,
	 *                               verschoben, umbenannt, ...)
	 * @throws IllegalStateException Der Container ist zwar physisch vorhanden, konnte nicht mehr rekonstruiert werden
	 *                               (Daten im Container sind fehlerhaft und k�nnen nicht mehr ausgelesen/dekodiert
	 *                               werden)
	 */
	PersistentDataContainer getContainer(long containerId) throws IOException, IllegalStateException;

	/**
	 * Diese Methode erzeugt ein Objekt, �ber das auf einen Archivdatensatzcontainer zugegriffen werden kann. Das
	 * zur�ckgegebene Objekt erm�glicht den Zugriff auf die ID des Container und auf die identifizierenden Informationen
	 * des Containers (Systemobjekt, Attributgruppe, Aspekt, Simulationsvariante und Datensatzart). Wesentliche Funktionen
	 * des Persistenzmoduls (z.B. das Archivieren von Online empfangenen Datens�tzen) sind �ber entsprechende Methoden des
	 * zur�ckgegebenen Containerobjekts erreichbar.
	 *
	 * @param containerId             Eindeutiger Index, der den Container identifiziert
	 * @param containerIdentification Eindeutige Identifizierung des Containers
	 * @param holdBackTime            Vorhaltezeitraum des Containers
	 * @param saveTypeB               Bestimmt ob der Container der Sicherung �bergeben werden soll (true = ja; false =
	 *                                nein)
	 * @param serializerVersion       Version mit der alle Datens�tze des Containers serialisiert wurden
	 * @return Archivdatensatzcontainer
	 * @throws IOException              Der Container konnte auf dem Speichermedium nicht erzeugt werden
	 * @throws IllegalArgumentException Der Container konnte nicht erzeugt werden, da er bereits existiert
	 */
	PersistentDataContainer createContainer(long containerId, DataContainerIdentification containerIdentification, long holdBackTime, boolean saveTypeB, int serializerVersion) throws IOException, IllegalArgumentException;

	/**
	 * Startet eine Archivabfrage. F�r jede Teilanfrage im �bergebenen Array wird ein korrespondierendes
	 * Stream-Supplier-Objekt zur�ckgegeben. Je Teilanfrage k�nnen die Daten der Reihe nach vom entsprechenden
	 * Stream-Supplier-Objekt abgeholt werden. Die Reihenfolge der �ber einen Stream-Supplier zur�ckgegebenen Datens�tzen
	 * ist in den Techischen Anforderungen zum Archivsystem detailliert beschrieben und kann �ber ein Attribut ({@link
	 * PersistentDataRequest#isDelayedDataReorderedByDataTime}) in der entsprechenden Teilanfrage beeinflusst werden. Je
	 * Stream-Supplier werden alle Datens�tze im spezifizierten Intervall zur�ckgegeben. Dazu geh�rt auch der Datensatz der
	 * zu Beginn des Intervalls g�ltig ist. Dies ist i.a. nicht der erste Datensatz im Intervall, sondern der mit dem
	 * n�chstkleineren Datensatzindex. Dieser hat i.a. einen Zeitstempel der vor dem angefragten Intervall liegt. Nach dem
	 * letzten Datensatz im Intervall wird ein Datensatz mit dem Typ {@link de.bsvrz.dav.daf.main.DataState#END_OF_ARCHIVE}
	 * zur�ckgegeben. Es ist zu beachten, das die Teilanfragen unabh�ngig voneinander und parallel bearbeitet werden
	 * m�ssen, da vom Abnehmer dynamisch gesteuert werden kann, in welcher Reihenfolge die entsprechender
	 * Stream-Supplier-Objekte in der R�ckgabe ausgelesen werden. Weiterhin m�ssen mehrere Aufrufe parallel bearbeitet und
	 * die jeweiligen Ergebnis unabh�ngig voneinander abgefragt werden k�nnen.
	 *
	 * @param requests Array mit den parallel zu verarbeitenden Teilanfragen.
	 * @return Array, das je Teilanfrage ein Stream-Supplier-Objekt enth�lt, �ber das die Ergebnisse der Reihe nach
	 *         ausgelesen werden k�nnen.
	 */
	PersistentDataStreamSupplier[] getArchiveDataStreams(PersistentDataRequest[] requests);

	/**
	 * Die Implementation dieser Methode l�dt einen Container aus der Sicherung. Der Container wurde als Datei auf einem
	 * Speichermedium des Typs B abgelegt und wird dem PersistenceModule (Speichermedium Typ A) wieder zur Verf�gung
	 * gestellt.
	 *
	 * @param containerId Eindeutige Identifikation des Containers
	 * @param volumeId    Eindeutige Identifiktation des Speichermediums auf dem der Container gesichert wurde. Der
	 *                    Container wurde mit dem Aufruf {@link PersistentDataContainer#saveAllData()} auf dem
	 *                    Speichermedium Typ B gesichert.
	 * @return Archivdatensatzcontainer
	 * @throws IOException           Fehler beim Zugriff auf die Datei, die von der Sicherung zur�ckgegeben wurde/sollte
	 * @throws IllegalStateException Der Container ist zwar physisch vorhanden, konnte nicht mehr rekonstruiert werden
	 *                               (Daten im Container sind fehlerhaft und k�nnen nicht mehr ausgelesen/dekodiert
	 *                               werden)
	 * @throws BadVolumeException    Das angegebene Speichermedium vom Typ B befindet sich nicht im direkten Zugriff der
	 *                               Sicherung
	 */
	public PersistentDataContainer reloadContainer(long containerId, int volumeId) throws IOException, IllegalStateException, BadVolumeException;

	/**
	 * Die Implementation dieser Methode gibt die eindeutige Identifikation aller Speichermedien zur�ck, die sich im
	 * direkten Zugriff der Sicherung befinden.
	 *
	 * @return Eindeutige Identifikation aller Speichermedien, die sich im direkten Zugriff der Sicherung befinden
	 */
	public Collection<Integer> getVolumes();

	/**
	 * Die Implementation dieser Methode gibt alle Dateinamen zur�ck, die sich auf einem Speichermedium des Typs B der
	 * Sicherung befinden und mit der Methode {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver#saveFile} dort abgelegt
	 * wurden.
	 *
	 * @param volumeId Eindeutige Identifikation des Speichermediums Typ B der Sicherung
	 * @return Dateinamen aller Dateien, die auf dem gefordeten Speichermedium mit der Methode {@link
	 *         de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver#saveFile} gespeichert wurden
	 * @throws IOException              Der Zugriff auf die Datei �ber das Speichermedium der Sicherung ist fehlgeschlagen
	 * @throws IllegalArgumentException Die angegebene Datei war auf dem angegebenen Datentr�ger nicht zu finden
	 * @throws BadVolumeException       Der angegebene Datentr�ger ist nicht mehr vorhanden oder nicht mehr im direkten
	 *                                  Zugriff der Sicherung
	 */
	public Collection<String> getFiles(int volumeId) throws IOException, IllegalArgumentException, BadVolumeException;

	/**
	 * Die Implementation dieser Methode gibt ein Objekt zur�ck, �ber das alle Container, die sich im direkten Zugriff der
	 * Persistenz befinden, angefordert werden k�nnen.
	 *
	 * @return Objekt, �ber das alle Container, die sich im direkten Zugriff der Persistenz befinden, angefordert werden
	 *         k�nnen
	 */
	public PersistentContainerStreamSupplier getAllContainers();

	/**
	 * Die Implementation dieser Methode �bernimmt ein Objekt vom Typ ClientDavInterface und stellt dieses intern zur
	 * Verf�gung.
	 *
	 * @param connection Objekt der Klasse ClientDavInterface
	 */
	void initialize(ClientDavInterface connection);

	/**
	 * Die Implementation dieser Methode �berpr�ft, ob f�r die Persistenz gen�gend freier Speicherplatz zur Verf�gung
	 * steht, der zum Archivieren von Archivdaten genutzt werden kann.
	 *
	 * @param requiredCapacity Angabe des Speicherplatzes der Persistenz, der der Implementation des Interfaces zum
	 *                         Speichern von Archivdaten zur Verf�gung stehen muss. Die Angabe erfolgt in
	 *                         <code>Byte</code>.
	 * @return true = Der geforderte Speicherplatz steht der Persistenz zur Verf�gung; false = Der geforderte Speicherplatz
	 *         steht der Persistenz nicht zur Verf�gung.
	 */
	boolean checkPersistenceCapacity(long requiredCapacity);
}
