/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.archive;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Schnittstelle, die von Applikationen benutzt wird, um Anfragen an ein Archivsystem zu stellen. Eine konkrete Implementierung wird von den
 * Datenverteiler-Applikationsfunktionen nach erfolgreichem Verbindungsaufbau zum Datenverteiler �ber die Methode {@link
 * de.bsvrz.dav.daf.main.ClientDavInterface#getArchive} zur Verf�gung gestellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6831 $
 */
public interface ArchiveRequestManager {

	/**
	 * Spezifiziert die Gr��e des Empfangspuffers je Archivanfrage, die mit nachfolgenden <code>request</code> Aufrufen erzeugt werden. Der angegebene Parameter
	 * ist eine grobe Vorgabe f�r alle Streams einer Archivanfrage zusammen und muss von einer Implementierung nicht exakt ber�cksichtigt werden, sondern sollte in
	 * der sendeseitigen Blockbildung und in der Flusskontrolle so ber�cksichtigt werden, dass die Summe der vom Archivsystem gesendeten aber noch nicht von der
	 * anfragenden Applikation abgerufenen Bytes in der angegebenen Gr��enordnung liegt. Der Defaultwert ist 0 mit der Bedeutung, das die Gr��e durch einen
	 * entsprechenden Parameter des Archivsystems festgelegt wird.
	 *
	 * @param numberOfBytes Grobe Vorgabe bez�glich der Anzahl zu puffernden Bytes pro Archivanfrage oder 0, wenn keine Vorgabe seitens der Empfangsapplikation
	 *                      gemacht werden soll und statt dessen ein entsprechender Parameter des Archivsystems verwendet werden soll.
	 */
	void setReceiveBufferSize(int numberOfBytes);

	/**
	 * Ruft Archivdaten von einem Archivsystem mit Hilfe eines Ergebnisdatenstroms ab. Diese Methode wird von einer Applikation aufgerufen um Archivdaten von einem
	 * Archivsystem abzufragen. Eine Implementierung dieser Methode sollte ein Objekt zur�ckliefern �ber das asynchron auf das Ergebnis der Archivanfrage
	 * zugegriffen werden kann. Im Ergebnis wird f�r die im Parameter <code>spec</code> spezifizierten Daten ein Stream von Ergebnisdatens�tzen erwartet.
	 *
	 * @param priority Priorit�t der Anfrage
	 * @param spec     Spezifikation der gew�nschten Archivdaten
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Ergebnisdatens�tze zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveDataQueryResult request(ArchiveQueryPriority priority, ArchiveDataSpecification spec) throws IllegalStateException;

	/**
	 * Ruft Archivdaten von einem Archivsystem mit Hilfe mehrerer Ergebnisdatenstr�me ab. Diese Methode wird von einer Applikation aufgerufen um Archivdaten von
	 * einem Archivsystem abzufragen. Eine Implementierung dieser Methode sollte ein Objekt zur�ckliefern �ber das asynchron auf die Ergebnisse der Archivanfrage
	 * zugegriffen werden kann. Im Ergebnis wird f�r jede Archivdatenspezifikation in der �bergebenen Liste <code>specs</code> jeweils ein Stream von
	 * Ergebnisdatens�tzen erwartet.
	 *
	 * @param priority Priorit�t der Anfrage
	 * @param specs    Liste mit Spezifikationen der gew�nschten Archivdaten
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die Ergebnisdatenstr�me mit den gew�nschten Ergebnisdatens�tzen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveDataQueryResult request(ArchiveQueryPriority priority, List<ArchiveDataSpecification> specs) throws IllegalStateException;

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode wird von einer Applikation aufgerufen, um eine Archivinformationsanfrage an das
	 * Archivsystem zu starten. Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zur�ckliefern �ber das asynchron auf das Ergebnis der
	 * Anfrage zugegriffen werden kann.
	 * <p/>
	 * Das Ergebnisobjekt beinhaltet eine Liste von Objekten. Diese Objekte geben unter anderem ein Objekt vom Typ <code>ArchiveDataSpecification</code> zur�ck.
	 * Dieses Objekt ist eine Referenz auf den Eingabeparameter <code>spec</code>.
	 *
	 * @param spec Spezifikation der Archivdaten zu denen Informationen gew�nscht werden.
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveInfoQueryResult requestInfo(ArchiveDataSpecification spec) throws IllegalStateException;

	/**
	 * Die Implementation dieser Methode meldet sich als Empf�nger von aktuellen Daten an. Die zur�ckgegebenen Datens�tze unterscheiden sich von einer "normalen"
	 * Anmeldung als Empf�nger, da Archivdaten zeitlich vor die aktuellen Daten gemischt werden. Der Benutzer legt dabei fest wie gross der Zeitraum sein soll, der
	 * vor die aktuellen Daten gemischt werden soll. Der Zeitraum wird als "Zeitspanne" oder als "Anzahl Datens�tze" angegeben. </br> Ist kein Archivsystem
	 * vorhanden oder bei der Archivanfrage tritt ein Fehler auf, werden nur die aktuellen Daten zur�ckgegeben.
	 *
	 * @param receiver        Objekt, das Methoden zur Verf�gung stellt um den Strom aus historischen und aktuellen Daten entgegen zu nehmen
	 * @param object          System-Objekt f�r die die spezifizierten Daten anzumelden sind
	 * @param dataDescription Beschreibende Informationen zu den anzumeldenden Daten
	 * @param options         F�r die Anmeldung zu verwendende Optionen
	 * @param historyType     <code>HistoryTypeParameter.TIME</code> = Der Parameter <code>history</code> bezieht sich auf einen Zeitraum, der vor den aktuellen
	 *                        Daten liegen soll (in ms); <code>HistoryTypeParameter.INDEX</code> = Der Parameter <code>history</code> bezieht sich auf die Anzahl
	 *                        Datens�tze, die mindestens vor den aktuellen Daten liegen sollen
	 * @param history         Zeitraum der Archivdaten in Millisekunden, die vor den ersten aktuellen Datens�tzen liegen
	 */
	void subscribeReceiver(
			DatasetReceiverInterface receiver,
			SystemObject object,
			DataDescription dataDescription,
			ReceiveOptions options,
			HistoryTypeParameter historyType,
			long history);

	/**
	 * Die Implementation dieser Methode meldet einen Empf�nger ab, der mit der Methode {@link ArchiveRequestManager#subscribeReceiver} angemeldet wurde.
	 *
	 * @param receiver
	 * @param object          System-Objekt f�r die die spezifizierten Daten angemeldet wurden
	 * @param dataDescription Beschreibende Informationen zu den angemeldeten Daten
	 */
	void unsubscribeReceiver(
			DatasetReceiverInterface receiver, SystemObject object, DataDescription dataDescription);

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode wird von einer Applikation aufgerufen, um eine Archivinformationsanfrage an das
	 * Archivsystem zu starten. Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zur�ckliefern �ber das asynchron auf das Ergebnis der
	 * Anfrage zugegriffen werden kann.
	 * <p/>
	 * Das Ergebnisobjekt beinhaltet eine Liste von Objekten. Diese Objekte geben unter anderem ein Objekt vom Typ <code>ArchiveDataSpecification</code> zur�ck.
	 * Diese Objekte sind Referenzen auf Eintr�ge des �bergabeparameters <code>specs</code>.
	 *
	 * @param specs Liste mit Spezifikationen der Archivdaten zu denen Informationen gew�nscht werden
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveInfoQueryResult requestInfo(List<ArchiveDataSpecification> specs) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem Archivdaten der Sicherung {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} zu �bergeben. Eine Applikation
	 * ruft diese Methode auf, um alle Archivdaten, die gesichert werden k�nnen, zu sichern. Eine Implementierung dieser Methode sollte ohne zu blockieren ein
	 * Objekt zur�ckliefern �ber das asynchron auf das Ergebnis der Anfrage zugegriffen werden kann.
	 *
	 * @return Ergebnisobjekt �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult savePersistentData() throws IllegalStateException;

	/**
	 * Start der Wiederherstellung von Datens�tzen, die der Sicherung �bergeben wurden (siehe {@link ArchiveRequestManager#savePersistentData()}). Diese Methode
	 * wird von einer Applikation aufgerufen, die Datens�tze ben�tgt, die sich nicht im direkten Zugriff des Archivsystems befinden, sondern bereits der Sicherung
	 * �bergeben wurden und sp�ter gel�scht wurden (siehe {@link ArchiveRequestManager#deleteDataSimulationVariant}, {@link ArchiveRequestManager#deleteData}).
	 * Eine Implementierung dieser Methode sollte ohne zu blockieren ein Objekt zur�ckliefern �ber das asynchron auf das Ergebnis des Auftrags zugegriffen werden
	 * kann.
	 *
	 * @param requiredData Eine Liste von Zeitbereichen/Indexbereichen, die Wiederhergestellt werden m�ssen.
	 *
	 * @return Ergebnisobjekt �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult restorePersistentData(List<ArchiveInformationResult> requiredData) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem Archivdaten, die zu einer Simulation geh�ren, aus dem Archivsystem zu l�schen. Der L�schauftrag bezieht sich dabei nicht auf
	 * Datens�tze, die bereits der Sicherung �bergeben wurden und von dieser verwaltet werden (siehe {@link ArchiveRequestManager#savePersistentData()}). Die
	 * Methode wird von einer Applikation aufgerufen, um nicht mehr ben�tigte Datens�tze, die zu einer Simulation geh�ren, aus dem Archivsystem zu l�schen.
	 *
	 * @param simulationVariant Simulationsvariante, ganzzahliger Wert zwischen 1,...,999
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 *
	 * @throws IllegalStateException Das Archivsystem ist nicht erreichbar.
	 */
	ArchiveQueryResult deleteDataSimulationVariant(short simulationVariant) throws IllegalStateException;

	/**
	 * Beauftragt das Archivsystem den L�schzeitpunkt der angegebenen Zeitbereiche zu verl�ngern. Die Methode wird von einer Applikation aufgerufen, um ben�tigte
	 * Zeitbereiche l�nger als vorgesehen im direkten Zugriff des Archivsystems zu halten.
	 *
	 * @param requiredData Zeitbereiche, die l�nger im direkten Zugriff des Archivsystems bleiben sollen
	 * @param timePeriod   Zeitspanne, die die ausgew�hlten Daten l�nger im direkten Zugriff des Archivsystem bleiben sollen (in ms)
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 */
	ArchiveQueryResult increaseDeleteTime(List<ArchiveInformationResult> requiredData, long timePeriod);

	/**
	 * Beauftragt das Archivsystem die angegebenen Zeitbereiche ohne R�cksicht auf den mit {@link ArchiveRequestManager#increaseDeleteTime} festgelegten Wert zu
	 * l�schen. Das L�schen der Zeitbereiche wird dabei in zwei Varianten unterteilt. In der ersten Variante werden die Zeitbereiche umgehend aus dem direkten
	 * Zugriff des Archivsystems entfernt. In der zweiten Variante werden die Zeitbereiche nur als "zu l�schend" markiert, sobald das "automatische L�schen" des
	 * Archivsystems angsto�en wird (dies geschieht zyklisch), werden die Zeitbereiche entfernt.
	 * <p/>
	 * Bei allen L�schoperationen, die durch diesen Methodenaufruf ausgel�st werden, muss darauf geachtete werden, dass der Vorhaltezeitraum der Zeitbereiche
	 * abgelaufen sein muss und das die Zeitbereiche gesichert wurden, falls diese gesichert werden sollten. Wird gegen eine diese Forderungen verstossen, wird der
	 * angegebene Zeitbereich nicht gel�scht.
	 *
	 * @param dataDisposedToDelete Zeitbereich(e), die gel�scht werden sollen
	 * @param deleteImmediately    true = Variante 1, die Zeitbereiche werden umgehend aus dem direkten Zugriff des Archivsystems entfernt; false = Variante 2, die
	 *                             Zeitbereiche werden nur als "zu l�schend" markiert und sp�ter aus dem direkten Zugriff des Archivsystems entfernt
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 */
	ArchiveQueryResult deleteData(List<ArchiveInformationResult> dataDisposedToDelete, boolean deleteImmediately);

	/**
	 * Beauftragt das Archivsystem seine Informationen zu gespeicherten Daten mit einem Speichermedium Typ B {@link
	 * de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} abzugleichen. Dies kann n�tig werden, wenn die eindeutigen Identifizierungen der
	 * Speichermedien von Typ B durch die Sicherung ge�ndert wurden (Beispiel: Die Daten wurden vorher auf CD gespeichert, nun werden die Daten auf DVD gespeichert
	 * und die alten Datenbest�nde auf DVD umkopiert, somit fallen mehrere CD�s auf eine DVD und die eindeutigen Identifizierungen der CD�s sind nutzlos. Die
	 * eindeutigen Identifizierungen der CD�s wurde aber vom Archivsystem gespeichert und m�ssen folglich abgeglichen werden).
	 *
	 * @param volumeIdTypB Eindeutige Identifikation eines Speichermediums Typ B
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 */
	ArchiveQueryResult archiveFileSaverAlignment(int volumeIdTypB);

	/**
	 * Stellt fest ob das Archivsystem �ber den Datenverteiler derzeit erreichbar ist. Eine positive/negative Antwort ist keine Garantie, dass dieser Zustand auch
	 * in Zukunft g�ltig ist/bleibt.
	 *
	 * @return true = Das Archivsystem ist derzeit erreichbar, es k�nnen alle im Interface spezifizierten Methoden benutzt werden; false = Das Archivsystem ist
	 *         derzeit nicht erreichbar
	 */
	boolean isArchiveAvailable();

	/**
	 * Das �bergebene Objekt <code>listener</code> wird benachrichtigt sobald sich die Erreichbarkeit des Archivsystems �ber den Datenverteiler �ndert.
	 *
	 * @param listener Objekt, das benutzt wird um �nderungen der Erreichbarkeit des Archivsystems �ber den Datenverteiler anzuzeigen
	 */
	void addArchiveAvailabilityListener(ArchiveAvailabilityListener listener);

	/**
	 * Das Objekt, das mit {@link #addArchiveAvailabilityListener} �bergeben wurde, wird nicht mehr benachrichtigt sobald sich die Erreichbarkeit des Archivsystems
	 * �ber den Datenverteiler �ndert.
	 *
	 * @param listener Objekt, das nicht mehr benachrichtigt werden soll, wenn sich die Erreichbarkeit des Archivsystems �ber den Datenverteiler �ndert
	 */
	void removeArchiveAvailabilityListener(ArchiveAvailabilityListener listener);

	/**
	 * Beauftragt das Archivsystem fehlende Daten von anderen Archivsystemen anzufordern und diese dann als "nachgefordert" zu speichern und bei Archivanfragen zur
	 * Verf�gung zu stellen.
	 *
	 * @param requiredData      Datenidentifikation(en), die nachgefordert werden sollen. Jede Datenidentifikation speichert zus�tlich den Zeitbereich, in dem
	 *                          Daten zu dieser Datenidentifikation angefordert werden soll. Es muss mindestens eine Datenidentifikation vorhanden sein.
	 * @param requestedArchives Archivsystem(e), bei denen Daten angefordert werden. Ist diese Liste leer werden alle Archivsystem angefragt, die beim
	 *                          automatischen Nachfordern angefragt werden.
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 */
	ArchiveQueryResult requestData(Collection<ArchiveInformationResult> requiredData, Collection<SystemObject> requestedArchives);

	/**
	 * Beauftragt das Archivsystem fehlende Daten von anderen Archivsystemen anzufordern und diese dann als "nachgefordert" zu speichern und bei Archivanfragen zur
	 * Verf�gung zu stellen. Es werden alle Datenidentifikationen angefragt, die auch beim automatischen Nachfordern angefragt werden.
	 *
	 * @param startTime         Startzeitpunkt, ab dem Daten nachgefordert werden
	 * @param endTime           Endzeitpunkt, bis zu dem Daten nachgefordert werden
	 * @param requestedArchives Archivsysteme die angefragt werden. Ist die Liste leer werden alle Archivsysteme angefragt, die beim automatischen Nachfordern
	 *                          angefragt werden
	 *
	 * @return Ergebnisobjekt, �ber das Informationen �ber den Zustand des Auftrags abgefragt werden k�nnen
	 */
	ArchiveQueryResult requestData(long startTime, long endTime, Collection<SystemObject> requestedArchives);
}
