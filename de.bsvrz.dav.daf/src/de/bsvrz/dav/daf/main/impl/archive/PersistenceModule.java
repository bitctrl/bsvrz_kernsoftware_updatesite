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

package de.bsvrz.dav.daf.main.impl.archive;


import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.impl.archive.filesaver.BadVolumeException;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

import java.io.IOException;
import java.util.*;

/**
 * Ein Objekt, das dieses Interface implementiert, stellt einem Archivsystem das sogenannte "PersistenceModule" zur
 * Verfügung. Die Aufgabe des "PersistenceModule" ist es, Datensätze zu speichern und dieser einer Verwaltung zur
 * Verfügung zu stellen.
 * <p>
 * Eine weitere Aufgabe des "PersistenceModule" ist es, die gespeicherten Datensätze persistent zu speichern. Diese
 * Aufgabe wird von einer Implementation des Interface {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} übernommen, diese
 * wird auch als Sicherung bezeichnet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface PersistenceModule {

	/**
	 * Diese Methode wird in der Initialisierungsphase aufgerufen um dem Persistenzmodul die Möglichkeit zu geben,
	 * Aufrufparameter der Applikation zu lesen und zu interpretieren.
	 * <p>
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
	 * Gibt ein Objekt zurück, über das auf einen Archivdatensatzcontainer zugegriffen werden kann. Das zurückgegebene
	 * Objekt ermöglicht den Zugriff auf die ID des Container und auf die identifizierenden Informationen des Containers
	 * (Systemobjekt, Attributgruppe, Aspekt, Simulationsvariante und Datensatzart). Wesentliche Funktionen des
	 * Persistenzmoduls (z.B. das Archivieren von Online empfangenen Datensätzen) sind über entsprechende Methoden des
	 * zurückgegebenen Containerobjekts erreichbar.
	 *
	 * @param containerId Eindeutige Nummer des Containers.
	 * @return Archivdatensatzcontainer.
	 * @throws IOException           Der Container ist auf dem benutzten Speichermedium nicht mehr vorhanden (gelöscht,
	 *                               verschoben, umbenannt, ...)
	 * @throws IllegalStateException Der Container ist zwar physisch vorhanden, konnte nicht mehr rekonstruiert werden
	 *                               (Daten im Container sind fehlerhaft und können nicht mehr ausgelesen/dekodiert
	 *                               werden)
	 */
	PersistentDataContainer getContainer(long containerId) throws IOException, IllegalStateException;

	/**
	 * Diese Methode erzeugt ein Objekt, über das auf einen Archivdatensatzcontainer zugegriffen werden kann. Das
	 * zurückgegebene Objekt ermöglicht den Zugriff auf die ID des Container und auf die identifizierenden Informationen
	 * des Containers (Systemobjekt, Attributgruppe, Aspekt, Simulationsvariante und Datensatzart). Wesentliche Funktionen
	 * des Persistenzmoduls (z.B. das Archivieren von Online empfangenen Datensätzen) sind über entsprechende Methoden des
	 * zurückgegebenen Containerobjekts erreichbar.
	 *
	 * @param containerId             Eindeutiger Index, der den Container identifiziert
	 * @param containerIdentification Eindeutige Identifizierung des Containers
	 * @param holdBackTime            Vorhaltezeitraum des Containers
	 * @param saveTypeB               Bestimmt ob der Container der Sicherung übergeben werden soll (true = ja; false =
	 *                                nein)
	 * @param serializerVersion       Version mit der alle Datensätze des Containers serialisiert wurden
	 * @return Archivdatensatzcontainer
	 * @throws IOException              Der Container konnte auf dem Speichermedium nicht erzeugt werden
	 * @throws IllegalArgumentException Der Container konnte nicht erzeugt werden, da er bereits existiert
	 */
	PersistentDataContainer createContainer(long containerId, DataContainerIdentification containerIdentification, long holdBackTime, boolean saveTypeB, int serializerVersion) throws IOException, IllegalArgumentException;

	/**
	 * Startet eine Archivabfrage. Für jede Teilanfrage im übergebenen Array wird ein korrespondierendes
	 * Stream-Supplier-Objekt zurückgegeben. Je Teilanfrage können die Daten der Reihe nach vom entsprechenden
	 * Stream-Supplier-Objekt abgeholt werden. Die Reihenfolge der über einen Stream-Supplier zurückgegebenen Datensätzen
	 * ist in den Techischen Anforderungen zum Archivsystem detailliert beschrieben und kann über ein Attribut ({@link
	 * PersistentDataRequest#isDelayedDataReorderedByDataTime}) in der entsprechenden Teilanfrage beeinflusst werden. Je
	 * Stream-Supplier werden alle Datensätze im spezifizierten Intervall zurückgegeben. Dazu gehört auch der Datensatz der
	 * zu Beginn des Intervalls gültig ist. Dies ist i.a. nicht der erste Datensatz im Intervall, sondern der mit dem
	 * nächstkleineren Datensatzindex. Dieser hat i.a. einen Zeitstempel der vor dem angefragten Intervall liegt. Nach dem
	 * letzten Datensatz im Intervall wird ein Datensatz mit dem Typ {@link de.bsvrz.dav.daf.main.DataState#END_OF_ARCHIVE}
	 * zurückgegeben. Es ist zu beachten, das die Teilanfragen unabhängig voneinander und parallel bearbeitet werden
	 * müssen, da vom Abnehmer dynamisch gesteuert werden kann, in welcher Reihenfolge die entsprechender
	 * Stream-Supplier-Objekte in der Rückgabe ausgelesen werden. Weiterhin müssen mehrere Aufrufe parallel bearbeitet und
	 * die jeweiligen Ergebnis unabhängig voneinander abgefragt werden können.
	 *
	 * @param requests Array mit den parallel zu verarbeitenden Teilanfragen.
	 * @return Array, das je Teilanfrage ein Stream-Supplier-Objekt enthält, über das die Ergebnisse der Reihe nach
	 *         ausgelesen werden können.
	 */
	PersistentDataStreamSupplier[] getArchiveDataStreams(PersistentDataRequest[] requests);

	/**
	 * Die Implementation dieser Methode lädt einen Container aus der Sicherung. Der Container wurde als Datei auf einem
	 * Speichermedium des Typs B abgelegt und wird dem PersistenceModule (Speichermedium Typ A) wieder zur Verfügung
	 * gestellt.
	 *
	 * @param containerId Eindeutige Identifikation des Containers
	 * @param volumeId    Eindeutige Identifiktation des Speichermediums auf dem der Container gesichert wurde. Der
	 *                    Container wurde mit dem Aufruf {@link PersistentDataContainer#saveAllData()} auf dem
	 *                    Speichermedium Typ B gesichert.
	 * @return Archivdatensatzcontainer
	 * @throws IOException           Fehler beim Zugriff auf die Datei, die von der Sicherung zurückgegeben wurde/sollte
	 * @throws IllegalStateException Der Container ist zwar physisch vorhanden, konnte nicht mehr rekonstruiert werden
	 *                               (Daten im Container sind fehlerhaft und können nicht mehr ausgelesen/dekodiert
	 *                               werden)
	 * @throws BadVolumeException    Das angegebene Speichermedium vom Typ B befindet sich nicht im direkten Zugriff der
	 *                               Sicherung
	 */
	public PersistentDataContainer reloadContainer(long containerId, int volumeId) throws IOException, IllegalStateException, BadVolumeException;

	/**
	 * Die Implementation dieser Methode gibt die eindeutige Identifikation aller Speichermedien zurück, die sich im
	 * direkten Zugriff der Sicherung befinden.
	 *
	 * @return Eindeutige Identifikation aller Speichermedien, die sich im direkten Zugriff der Sicherung befinden
	 */
	public Collection<Integer> getVolumes();

	/**
	 * Die Implementation dieser Methode gibt alle Dateinamen zurück, die sich auf einem Speichermedium des Typs B der
	 * Sicherung befinden und mit der Methode {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver#saveFile} dort abgelegt
	 * wurden.
	 *
	 * @param volumeId Eindeutige Identifikation des Speichermediums Typ B der Sicherung
	 * @return Dateinamen aller Dateien, die auf dem gefordeten Speichermedium mit der Methode {@link
	 *         de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver#saveFile} gespeichert wurden
	 * @throws IOException              Der Zugriff auf die Datei über das Speichermedium der Sicherung ist fehlgeschlagen
	 * @throws IllegalArgumentException Die angegebene Datei war auf dem angegebenen Datenträger nicht zu finden
	 * @throws BadVolumeException       Der angegebene Datenträger ist nicht mehr vorhanden oder nicht mehr im direkten
	 *                                  Zugriff der Sicherung
	 */
	public Collection<String> getFiles(int volumeId) throws IOException, IllegalArgumentException, BadVolumeException;

	/**
	 * Die Implementation dieser Methode gibt ein Objekt zurück, über das alle Container, die sich im direkten Zugriff der
	 * Persistenz befinden, angefordert werden können.
	 *
	 * @return Objekt, über das alle Container, die sich im direkten Zugriff der Persistenz befinden, angefordert werden
	 *         können
	 */
	public PersistentContainerStreamSupplier getAllContainers();

	/**
	 * Die Implementation dieser Methode übernimmt ein Objekt vom Typ ClientDavInterface und stellt dieses intern zur
	 * Verfügung.
	 *
	 * @param connection Objekt der Klasse ClientDavInterface
	 */
	void initialize(ClientDavInterface connection);

	/**
	 * Die Implementation dieser Methode überprüft, ob für die Persistenz genügend freier Speicherplatz zur Verfügung
	 * steht, der zum Archivieren von Archivdaten genutzt werden kann.
	 *
	 * @param requiredCapacity Angabe des Speicherplatzes der Persistenz, der der Implementation des Interfaces zum
	 *                         Speichern von Archivdaten zur Verfügung stehen muss. Die Angabe erfolgt in
	 *                         <code>Byte</code>.
	 * @return true = Der geforderte Speicherplatz steht der Persistenz zur Verfügung; false = Der geforderte Speicherplatz
	 *         steht der Persistenz nicht zur Verfügung.
	 */
	boolean checkPersistenceCapacity(long requiredCapacity);
}
