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


import java.io.IOException;

import de.bsvrz.dav.daf.main.impl.archive.filesaver.BadVolumeException;
import de.bsvrz.dav.daf.main.DataState;

/**
 * Schnittstelle mit der das Persistenzmodul die Grundfunktionalität eines Archivdatensatzcontainers zur Verfügung
 * stellt (siehe auch Technische Anforderungen ArS). Konkrete Objekte dieses Typs werden von der Methode {@link
 * PersistenceModule#getContainer} des Persistenz-Moduls erzeugt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface PersistentDataContainer {
	/**
	 * Bestimmt die eindeutige laufende Nummer dieses Datensatzcontainers.
	 *
	 * @return Eindeutige laufende Container-Nummer.
	 */
	long getContainerId();

	/**
	 * Bestimmt die identifizierenden Informationen des Containers (Systemobjekt, Attributgruppe, Aspekt,
	 * Simulationsvariante und Datensatzart).
	 *
	 * @return Identifizierende Informationen des Containers.
	 */
	DataContainerIdentification getDataContainerIdentification();

	/**
	 * Bestimmt die Version des Serialisierers, der beim Serialisieren der Datensätze dieses Containers benutzt wurde.
	 *
	 * @return Version des Serialisierers
	 * @see de.bsvrz.sys.funclib.dataSerializer.Serializer#getVersion()
	 */
	int getSerializerVersion();

	/**
	 * Speichert einen Datensatz mit seinen Headerinformationen (Zeitstempel, laufende Nummer und Kompressionsart) im
	 * Container ab. Weitere Headerinformationen (Objekt-Id, Attributgruppe, Aspekt etc.) werden nicht mit jedem Datensatz
	 * gespeichert, weil sie sich innerhalb eines Containers nicht verändern. Bezüglich eines Containers kann davon
	 * ausgegangen werden, dass die Daten- und Archivzeitstempel sowie der Datensatzindex in aufeinanderfolgenden Aufrufen
	 * der Methode monoton steigend sind. Das übergebene Bytearray kann bei Bedarf von einer Implementierung komprimiert
	 * werden. Über ein Flag kann bei Bedarf spezifiziert werden, ob die Speicherung synchron erfolgen soll und
	 * abgeschlossen sein muss, bevor die Methode sich beendet. Dies wird vom ContainerManager gesetzt, wenn der Datensatz
	 * nach dem Speichern quittiert werden soll. Wenn das Flag nicht gesetzt ist, kann eine Implementierung die Speicherung
	 * bei Bedarf asynchron bzw. gepuffert durchführen.
	 *
	 * @param dataTiming Datenzeitstempel, Archivzeitstempel und Datensatzindex des Datensatzes.
	 * @param dataState  Typ des Datensatzes (Nutzdaten, keine Daten, keine Quelle, potentielle Datenlücke etc.).
	 * @param dataBytes  Byte-Array mit den unkomprimierten, serialisierten Nutzdaten oder <code>null</code> im Falle eines
	 *                   leeren Datensatzes ohne Nutzdaten.
	 * @param flush      <code>true</code>, wenn die Speicherung synchron durchgeführt werden soll, sonst
	 *                   <code>false</code>.
	 * @throws IOException Beim Zugriff auf die physische Abbildung des Containers auf einem Speichermedium kam es zu einem
	 *                     Fehler. Die Daten konnten nicht geschrieben werden.
	 */
	void storeData(DataTiming dataTiming, DataState dataState, byte[] dataBytes, boolean flush) throws IOException;

	/**
	 * Dieser Aufruf signalisiert dem Persistenzmodul, dass kein weiterer Datensatz mehr in diesen Container abgelegt wird.
	 * Nach dem Aufruf dieser Methode wird die Methode {@link #storeData} für diesen Container nicht mehr aufgerufen.
	 * <p>
	 *
	 * @throws IOException Beim Zugriff auf die physische Abbildung des Containers auf einem Speichermedium kam es zu einem
	 *                     Fehler. Der Container konnte nicht abgeschlossen werden.
	 */
	void finalizeAllData() throws IOException;

	/**
	 * Bestimmt die minimalen Werte von Datenzeitstempel, Archivzeitstempel und Datensatzindex aller in diesem Container
	 * mit {@link #storeData} gespeicherten Daten. Nach einem unkontrollierten Beenden des Archivsystems wird diese Methode
	 * in der Initialisierungsphase für jeden noch nicht abgschlossenen Container aufgerufen um die notwendigen
	 * Verwaltungsinformationen zu aktualisieren.
	 *
	 * @return Datenstruktur mit den minimalen Werten von Datenzeitstempel, Archivzeitstempel und Datensatzindex.
	 */
	DataTiming getMinimumDataTiming();

	/**
	 * Bestimmt die maximalen Werte von Datenzeitstempel, Archivzeitstempel und Datensatzindex aller in diesem Container
	 * mit {@link #storeData} gespeicherten Daten. Nach einem unkontrollierten Beenden des Archivsystems wird diese Methode
	 * in der Initialisierungsphase für jeden noch nicht abgschlossenen Container aufgerufen um die notwendigen
	 * Verwaltungsinformationen zu aktualisieren.
	 *
	 * @return Datenstruktur mit maximalen Werte von Datenzeitstempel, Archivzeitstempel und Datensatzindex.
	 */
	DataTiming getMaximumDataTiming();

	/**
	 * Bestimmt die Anzahl Datensätze in diesem Container Nach einem unkontrollierten Beenden des Archivsystems wird diese
	 * Methode in der Initialisierungsphase für jeden noch nicht abgschlossenen Container aufgerufen um die notwendigen
	 * Verwaltungsinformationen zu aktualisieren.
	 *
	 * @return Anzahl der Datensätze dieses Containers.
	 */
	int getDataCount();

	/**
	 * Bestimmt den Speicherbedarf des Containers. Diese Methode wird vom ContainerManager bei der Entscheidung ob ein
	 * Container abgeschlossen und ein neuer Container angelegt werden soll verwendet. Ein Implementierung sollte den vom
	 * Container beanspruchten Platz nach eventueller Komprimierung der Datensätze ermitteln und zurückgeben.
	 *
	 * @return Speicherbedarf dieses Containers in Anzahl Bytes.
	 */
	long getStorageSize();

	/**
	 * Der Rückgabewert dieser Methode gibt darüber Auskunft, ob der Container der Sicherung übergeben werden soll.
	 *
	 * @return true = Der Container soll der Sicherung übergeben werden. false = Der Container soll nicht der Sicherung
	 *         übergeben werden
	 */
	boolean getSaveStatus();

	/**
	 * Die Implementation dieser Methode zeigt an, ob der Container von der Sicherung auf einem Speichermedium vom Typ B
	 * gesichert wurde.
	 *
	 * @return true = Der Container wurde der Sicherung übergeben und diese hat den Container auf einem Speichermedium vom
	 *         Typ B gesichert; false = Der Container wurde noch nicht von der Sicherung auf einem Speichermedium vom Typ B
	 *         gesichert
	 */
	boolean containerSavedTypeB();

	/**
	 * Der Rückgabewert dieser Methode bestimmt, wann der Container zu löschen ist. Dieser Wert entspricht dem
	 * Vorhaltezeitraum in den technischen Anforderungen. Ist der Vorhaltezeitraum abgelaufen wird der Container vom
	 * Speichermedium Typ A entfernt.
	 *
	 * @return Vorhaltezeitraum
	 */
	long getDeleteDate();

	/**
	 * Die Implemetierung dieser Methode gibt die eindeutige Identifizierung des Speichermediums vom Typ B zurück, auf dem
	 * der Container gesichert wurde.
	 *
	 * @return Eindeutige Identifizierung des Speichermediums vom Typ B, auf dem der Container gesichert wurde
	 * @throws IllegalStateException Der Container wurde bisher noch nicht auf einem Speichermedium vom Typ B gesichert,
	 *                               somit ist die eindeutige Identifizierung unbekannt
	 */
	int getVolumeIdTypeB() throws IllegalStateException;

	/**
	 * Sichert alle Datensätze des Containers auf einem externen Medium.
	 *
	 * @return Name des Mediums auf das der Container gespeichert wurde. TODO: Notwendige Interaktionen mit Benutzer
	 *         klären. TODO: Exceptions mit entsprechenden Meldungen für Fehler (kein Medium, Medium voll etc.)
	 *         definieren.
	 */
	int saveAllData() throws IOException;

	/**
	 * Löscht den gesamten Container mit allen gespeicherten Datensätzen vom Speichermedium Typ A.
	 * @return true = Der Container konnte gelöscht werden; false = Der Container konnte nicht gelöscht werden
	 */
	boolean deleteAllData();

	/**
	 * Lädt alle Datensätze eines Containers von einem externen Medium.
	 *
	 * @param mediaName Name des Mediums auf dem die Datensätze vorher mit der Methode {@link #saveAllData} gesichert
	 *                  wurden.
	 * @throws IOException        Beim Zugriff auf die physische Abbildung des Containers auf einem Speichermedium kam es
	 *                            zu einem Fehler, der geforderte Container nicht wiederhergestellt werden
	 * @throws BadVolumeException Auf das Speichermedium der Sicherung konnte nicht Zugegriffen werden, obwohl sich dieses
	 *                            im Zugriff der Sicherung befindet
	 */
	void loadAllData(int mediaName) throws IOException, BadVolumeException;

	/**
	 * Sichert alle Datensätze, die vom Container gepuffert wurden.
	 * Nachdem diese Methode verlassen wird, sind alle Datensätze des Containers persistent auf einem Datenträger
	 * gespeichert und befinden sich im direkten Zugriff des Archivsystems.
	 *
	 * @throws IOException Beim Zugriff auf die physische Abbildung des Containers auf einem Speichermedium kam es zu einem
	 *                     Fehler, gepufferte Datensätze konnten nicht geschrieben werden.
	 */
	void flush() throws IOException;

	/**
	 * Die Implemetierung dieser Methode gibt den Zustand des Containers wieder,
	 * wurde der Container mit der Methode {@link PersistentDataContainer#finalizeAllData()} abgeschlossen, wird
	 * der Wert true zurückgegeben in allen anderen Fällen der Wert false.
	 *
	 * @return true = der Container wurde abgeschlossen; false = der Container wurde noch nicht abgeschlossen
	 */
	boolean isContainerFinalized();
}

