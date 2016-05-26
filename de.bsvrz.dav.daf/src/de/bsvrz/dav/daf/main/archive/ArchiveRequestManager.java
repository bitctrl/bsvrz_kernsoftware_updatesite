/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.archive;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Schnittstelle, die von Applikationen benutzt wird, um Anfragen an ein Archivsystem zu stellen. Eine konkrete Implementierung wird von den
 * Datenverteiler-Applikationsfunktionen nach erfolgreichem Verbindungsaufbau zum Datenverteiler über die Methode {@link
 * de.bsvrz.dav.daf.main.ClientDavInterface#getArchive} zur Verfügung gestellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ArchiveRequestManager {

	/**
	 * Spezifiziert die Größe des Empfangspuffers je Archivanfrage, die mit nachfolgenden <code>request</code> Aufrufen erzeugt werden. Der angegebene Parameter
	 * ist eine grobe Vorgabe für alle Streams einer Archivanfrage zusammen und muss von einer Implementierung nicht exakt berücksichtigt werden, sondern sollte in
	 * der sendeseitigen Blockbildung und in der Flusskontrolle so berücksichtigt werden, dass die Summe der vom Archivsystem gesendeten aber noch nicht von der
	 * anfragenden Applikation abgerufenen Bytes in der angegebenen Größenordnung liegt. Der Defaultwert ist 0 mit der Bedeutung, das die Größe durch einen
	 * entsprechenden Parameter des Archivsystems festgelegt wird.
	 *
	 * @param numberOfBytes Grobe Vorgabe bezüglich der Anzahl zu puffernden Bytes pro Archivanfrage oder 0, wenn keine Vorgabe seitens der Empfangsapplikation
	 *                      gemacht werden soll und statt dessen ein entsprechender Parameter des Archivsystems verwendet werden soll.
	 */
	void setReceiveBufferSize(int numberOfBytes);

	/**
	 * Ruft Archivdaten von einem Archivsystem mit Hilfe eines Ergebnisdatenstroms ab. Diese Methode wird von einer Applikation aufgerufen um Archivdaten von einem
	 * Archivsystem abzufragen. Eine Implementierung dieser Methode sollte ein Objekt zurückliefern über das asynchron auf das Ergebnis der Archivanfrage
	 * zugegriffen werden kann. Im Ergebnis wird für die im Parameter <code>spec</code> spezifizierten Daten ein Stream von Ergebnisdatensätzen erwartet.
	 *
	 * @param priority Priorität der Anfrage
	 * @param spec     Spezifikation der gewünschten Archivdaten. Ein Objekt der Klasse {@link de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification}
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Ergebnisdatensätze zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveDataQueryResult request(ArchiveQueryPriority priority, ArchiveDataSpecification spec) throws IllegalStateException;

	/**
	 * Ruft Archivdaten von einem Archivsystem mit Hilfe mehrerer Ergebnisdatenströme ab. Diese Methode wird von einer Applikation aufgerufen um Archivdaten von
	 * einem Archivsystem abzufragen. Eine Implementierung dieser Methode sollte ein Objekt zurückliefern über das asynchron auf die Ergebnisse der Archivanfrage
	 * zugegriffen werden kann. Im Ergebnis wird für jede Archivdatenspezifikation in der übergebenen Liste <code>specs</code> jeweils ein Stream von
	 * Ergebnisdatensätzen erwartet.
	 *
	 * @param priority Priorität der Anfrage
	 * @param specs    Liste mit Spezifikationen der gewünschten Archivdaten.
	 *
	 * @return Ergebnisobjekt über das asynchron auf die Ergebnisdatenströme mit den gewünschten Ergebnisdatensätzen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveDataQueryResult request(ArchiveQueryPriority priority, List<ArchiveDataSpecification> specs) throws IllegalStateException;

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode wird von einer Applikation aufgerufen, um eine Archivinformationsanfrage an das
	 * Archivsystem zu starten. Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zurückliefern über das asynchron auf das Ergebnis der
	 * Anfrage zugegriffen werden kann.
	 * <p>
	 * Das Ergebnisobjekt beinhaltet eine Liste von Objekten. Diese Objekte geben unter anderem ein Objekt vom Typ <code>ArchiveDataSpecification</code> zurück.
	 * Dieses Objekt ist eine Referenz auf den Eingabeparameter <code>spec</code>.
	 *
	 * @param spec Spezifikation der Archivdaten zu denen Informationen gewünscht werden.
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveInfoQueryResult requestInfo(ArchiveDataSpecification spec) throws IllegalStateException;

	/**
	 * Die Implementation dieser Methode meldet sich als Empfänger von aktuellen Daten an. Die zurückgegebenen Datensätze unterscheiden sich von einer "normalen"
	 * Anmeldung als Empfänger, da Archivdaten zeitlich vor die aktuellen Daten gemischt werden. Der Benutzer legt dabei fest wie gross der Zeitraum sein soll, der
	 * vor die aktuellen Daten gemischt werden soll. Der Zeitraum wird als "Zeitspanne" oder als "Anzahl Datensätze" angegeben. </br> Ist kein Archivsystem
	 * vorhanden oder bei der Archivanfrage tritt ein Fehler auf, werden nur die aktuellen Daten zurückgegeben.
	 *
	 * @param receiver        Objekt, das Methoden zur Verfügung stellt um den Strom aus historischen und aktuellen Daten entgegen zu nehmen
	 * @param object          System-Objekt für die die spezifizierten Daten anzumelden sind
	 * @param dataDescription Beschreibende Informationen zu den anzumeldenden Daten
	 * @param options         Für die Anmeldung zu verwendende Optionen
	 * @param historyType     <code>HistoryTypeParameter.TIME</code> = Der Parameter <code>history</code> bezieht sich auf einen Zeitraum, der vor den aktuellen
	 *                        Daten liegen soll (in ms); <code>HistoryTypeParameter.INDEX</code> = Der Parameter <code>history</code> bezieht sich auf die Anzahl
	 *                        Datensätze, die mindestens vor den aktuellen Daten liegen sollen
	 * @param history         Zeitraum der Archivdaten in Millisekunden, die vor den ersten aktuellen Datensätzen liegen
	 */
	void subscribeReceiver(
			DatasetReceiverInterface receiver,
			SystemObject object,
			DataDescription dataDescription,
			ReceiveOptions options,
			HistoryTypeParameter historyType,
			long history);

	/**
	 * Die Implementation dieser Methode meldet einen Empfänger ab, der mit der Methode {@link ArchiveRequestManager#subscribeReceiver} angemeldet wurde.
	 *
	 * @param receiver
	 * @param object          System-Objekt für die die spezifizierten Daten angemeldet wurden
	 * @param dataDescription Beschreibende Informationen zu den angemeldeten Daten
	 */
	void unsubscribeReceiver(
			DatasetReceiverInterface receiver, SystemObject object, DataDescription dataDescription);

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode wird von einer Applikation aufgerufen, um eine Archivinformationsanfrage an das
	 * Archivsystem zu starten. Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zurückliefern über das asynchron auf das Ergebnis der
	 * Anfrage zugegriffen werden kann.
	 * <p>
	 * Das Ergebnisobjekt beinhaltet eine Liste von Objekten. Diese Objekte geben unter anderem ein Objekt vom Typ <code>ArchiveDataSpecification</code> zurück.
	 * Diese Objekte sind Referenzen auf Einträge des Übergabeparameters <code>specs</code>.
	 *
	 * @param specs Liste mit Spezifikationen der Archivdaten zu denen Informationen gewünscht werden
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveInfoQueryResult requestInfo(List<ArchiveDataSpecification> specs) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem Archivdaten der Sicherung {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} zu übergeben. Eine Applikation
	 * ruft diese Methode auf, um alle Archivdaten, die gesichert werden können, zu sichern. Eine Implementierung dieser Methode sollte ohne zu blockieren ein
	 * Objekt zurückliefern über das asynchron auf das Ergebnis der Anfrage zugegriffen werden kann.
	 *
	 * @return Ergebnisobjekt über das Informationen über den Zustand des Auftrags abgefragt werden können
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult savePersistentData() throws IllegalStateException;

	/**
	 * Start der Wiederherstellung von Datensätzen, die der Sicherung übergeben wurden (siehe {@link ArchiveRequestManager#savePersistentData()}). Diese Methode
	 * wird von einer Applikation aufgerufen, die Datensätze benötgt, die sich nicht im direkten Zugriff des Archivsystems befinden, sondern bereits der Sicherung
	 * übergeben wurden und später gelöscht wurden (siehe {@link ArchiveRequestManager#deleteDataSimulationVariant}, {@link ArchiveRequestManager#deleteData}).
	 * Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zurückliefern über das asynchron auf das Ergebnis des Auftrags zugegriffen werden
	 * kann.
	 *
	 * @param requiredData Eine Liste von Zeitbereichen/Indexbereichen, die Wiederhergestellt werden müssen.
	 *
	 * @return Ergebnisobjekt über das Informationen über den Zustand des Auftrags abgefragt werden können
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult restorePersistentData(List<ArchiveInformationResult> requiredData) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem Archivdaten, die zu einer Simulation gehören, aus dem Archivsystem zu löschen. Der Löschauftrag bezieht sich dabei nicht auf
	 * Datensätze, die bereits der Sicherung übergeben wurden und von dieser verwaltet werden (siehe {@link ArchiveRequestManager#savePersistentData()}). Die
	 * Methode wird von einer Applikation aufgerufen, um nicht mehr benötigte Datensätze, die zu einer Simulation gehören, aus dem Archivsystem zu löschen.
	 *
	 * @param simulationVariant Simulationsvariante, ganzzahliger Wert zwischen 1,...,999
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult deleteDataSimulationVariant(short simulationVariant) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem den Löschzeitpunkt der angegebenen Zeitbereiche zu verlängern. Die Methode wird von einer Applikation aufgerufen, um benötigte
	 * Zeitbereiche länger als vorgesehen im direkten Zugriff des Archivsystems zu halten.
	 *
	 * @param requiredData Zeitbereiche, die länger im direkten Zugriff des Archivsystems bleiben sollen
	 * @param timePeriod   Zeitspanne, die die ausgewählten Daten länger im direkten Zugriff des Archivsystem bleiben sollen (in ms)
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 */
	ArchiveQueryResult increaseDeleteTime(List<ArchiveInformationResult> requiredData, long timePeriod);

	/**
	 * Beauftragt das Archivsystem die angegebenen Zeitbereiche ohne Rücksicht auf den mit {@link ArchiveRequestManager#increaseDeleteTime} festgelegten Wert zu
	 * löschen. Das Löschen der Zeitbereiche wird dabei in zwei Varianten unterteilt. In der ersten Variante werden die Zeitbereiche umgehend aus dem direkten
	 * Zugriff des Archivsystems entfernt. In der zweiten Variante werden die Zeitbereiche nur als "zu löschend" markiert, sobald das "automatische Löschen" des
	 * Archivsystems angstoßen wird (dies geschieht zyklisch), werden die Zeitbereiche entfernt.
	 * <p>
	 * Bei allen Löschoperationen, die durch diesen Methodenaufruf ausgelöst werden, muss darauf geachtete werden, dass der Vorhaltezeitraum der Zeitbereiche
	 * abgelaufen sein muss und das die Zeitbereiche gesichert wurden, falls diese gesichert werden sollten. Wird gegen eine diese Forderungen verstossen, wird der
	 * angegebene Zeitbereich nicht gelöscht.
	 *
	 * @param dataDisposedToDelete Zeitbereich(e), die gelöscht werden sollen
	 * @param deleteImmediately    true = Variante 1, die Zeitbereiche werden umgehend aus dem direkten Zugriff des Archivsystems entfernt; false = Variante 2, die
	 *                             Zeitbereiche werden nur als "zu löschend" markiert und später aus dem direkten Zugriff des Archivsystems entfernt
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 */
	ArchiveQueryResult deleteData(List<ArchiveInformationResult> dataDisposedToDelete, boolean deleteImmediately);

	/**
	 * Beauftragt das Archivsystem seine Informationen zu gespeicherten Daten mit einem Speichermedium Typ B {@link
	 * de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} abzugleichen. Dies kann nötig werden, wenn die eindeutigen Identifizierungen der
	 * Speichermedien von Typ B durch die Sicherung geändert wurden (Beispiel: Die Daten wurden vorher auf CD gespeichert, nun werden die Daten auf DVD gespeichert
	 * und die alten Datenbestände auf DVD umkopiert, somit fallen mehrere CDŽs auf eine DVD und die eindeutigen Identifizierungen der CDŽs sind nutzlos. Die
	 * eindeutigen Identifizierungen der CDŽs wurde aber vom Archivsystem gespeichert und müssen folglich abgeglichen werden).
	 *
	 * @param volumeIdTypB Eindeutige Identifikation eines Speichermediums Typ B
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 */
	ArchiveQueryResult archiveFileSaverAlignment(int volumeIdTypB);

	/**
	 * Stellt fest ob das Archivsystem über den Datenverteiler derzeit erreichbar ist. Eine positive/negative Antwort ist keine Garantie, dass dieser Zustand auch
	 * in Zukunft gültig ist/bleibt.
	 *
	 * @return true = Das Archivsystem ist derzeit erreichbar, es können alle im Interface spezifizierten Methoden benutzt werden; false = Das Archivsystem ist
	 *         derzeit nicht erreichbar
	 */
	boolean isArchiveAvailable();

	/**
	 * Das übergebene Objekt <code>listener</code> wird benachrichtigt sobald sich die Erreichbarkeit des Archivsystems über den Datenverteiler ändert.
	 *
	 * @param listener Objekt, das benutzt wird um Änderungen der Erreichbarkeit des Archivsystems über den Datenverteiler anzuzeigen
	 */
	void addArchiveAvailabilityListener(ArchiveAvailabilityListener listener);

	/**
	 * Das Objekt, das mit {@link #addArchiveAvailabilityListener} übergeben wurde, wird nicht mehr benachrichtigt sobald sich die Erreichbarkeit des Archivsystems
	 * über den Datenverteiler ändert.
	 *
	 * @param listener Objekt, das nicht mehr benachrichtigt werden soll, wenn sich die Erreichbarkeit des Archivsystems über den Datenverteiler ändert
	 */
	void removeArchiveAvailabilityListener(ArchiveAvailabilityListener listener);

	/**
	 * Beauftragt das Archivsystem fehlende Daten von anderen Archivsystemen anzufordern und diese dann als "nachgefordert" zu speichern und bei Archivanfragen zur
	 * Verfügung zu stellen.
	 *
	 * @param requiredData      Datenidentifikation(en), die nachgefordert werden sollen. Jede Datenidentifikation speichert zusätlich den Zeitbereich, in dem
	 *                          Daten zu dieser Datenidentifikation angefordert werden soll. Es muss mindestens eine Datenidentifikation vorhanden sein.
	 * @param requestedArchives Archivsystem(e), bei denen Daten angefordert werden. Ist diese Liste leer werden alle Archivsystem angefragt, die beim
	 *                          automatischen Nachfordern angefragt werden.
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 */
	ArchiveQueryResult requestData(Collection<ArchiveInformationResult> requiredData, Collection<SystemObject> requestedArchives);

	/**
	 * Beauftragt das Archivsystem fehlende Daten von anderen Archivsystemen anzufordern und diese dann als "nachgefordert" zu speichern und bei Archivanfragen zur
	 * Verfügung zu stellen. Es werden alle Datenidentifikationen angefragt, die auch beim automatischen Nachfordern angefragt werden.
	 *
	 * @param startTime         Startzeitpunkt, ab dem Daten nachgefordert werden
	 * @param endTime           Endzeitpunkt, bis zu dem Daten nachgefordert werden
	 * @param requestedArchives Archivsysteme die angefragt werden. Ist die Liste leer werden alle Archivsysteme angefragt, die beim automatischen Nachfordern
	 *                          angefragt werden
	 *
	 * @return Ergebnisobjekt, über das Informationen über den Zustand des Auftrags abgefragt werden können
	 */
	ArchiveQueryResult requestData(long startTime, long endTime, Collection<SystemObject> requestedArchives);

	/**
	 * Gibt Informationen über die Anzahl Anfragen zurück, die eine Applikation gleichzeitig stellen darf. Wenn die Anfrage fehlschlägt
	 * ({@link de.bsvrz.dav.daf.main.archive.ArchiveNumQueriesResult#isRequestSuccessful()} liefert false zurück und es gibt eine entsprechende Fehlermeldung) kann davon ausgegangen werden,
	 * dass eine ältere Archivsystem-Version eingesetzt wird und maximal 5 gleichzeitige Anfragen pro Applikation zulässig sind.
	 *
	 * @return Ergebnisobjekt, über dass Informationen zu der maximalen Anzahl Anfragen pro Applikation abgefragt werden können.
	 */
	ArchiveNumQueriesResult getNumArchiveQueries();
}
