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

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;

import java.io.IOException;
import java.util.*;


/**
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
 */
public interface ConfigurationAreaFile {

	/**
	 * Diese Methode gibt Informationen �ber das Konfigurationsobjekt zur�ck, das den Konfigurationsbereich darstellt.
	 *
	 * @return Informationen �ber das Objekt, das den Konfigurationsbereich darstellt
	 */
	ConfigurationObjectInfo getConfigurationAreaInfo();

	/**
	 * Diese Methode wird aufgerufen, wenn alle modifizierten Objekte des Konfigurationsbereichs persistent gespeichert werden sollen.
	 *
	 * @throws IOException Fehler beim Zugriff auf die Datei in der der Konfigurationsbereich gespeichert ist
	 */
	void flush() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren soll. Es m�ssen alle Daten gespeichert werden (siehe {@link #flush} und ein Zustand
	 * hergestellt werden, mit dem das System zu einem sp�teren Zeitpunkt wieder hochgefahren werden kann.
	 *
	  * @throws IOException Fehler beim Zugriff auf die Datei in der der Konfigurationsbereich gespeichert ist
	 */
	void close() throws IOException;


	/**
	 * Diese Methode erzeugt ein dynamisches Objekt, das sofort g�ltig ist.
	 *
	 * @param objectID          ID des Objekts
	 * @param typeID            Typ des Objekts, der Typ wird �ber die ID festgelegt
	 * @param pid               Pid des Objekts
	 * @param simulationVariant Simulationsvariante unter der das dynamische Objekt g�ltig ist
	 * @param name              Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param persistenceMode Soll das dynamische Objekte persistent gespeichert werden
	 *
	 * @return Objekt, �ber das auf das dynamische Objekt zugegriffen werden kann
	 *
	 * @throws IllegalArgumentException Die �bergebene Id des Objekts muss gr��er als die gr��te Id des Konfigurationsbereichs sein
	 * @see #getGreatestId
	 */
	DynamicObjectInfo createDynamicObject(long objectID, long typeID, String pid, short simulationVariant, String name, DynamicObjectType.PersistenceMode persistenceMode)
			throws IllegalArgumentException, IllegalStateException;

	/**
	 * Diese Methode erzeugt ein Konfigurationsobjekt.
	 *
	 * @param objectID ID des Objekts
	 * @param typeID   Typ des Objekts, der Typ wird �ber die ID festgelegt
	 * @param pid      Pid des Objekts
	 * @param name     Name des Objekts, <code>null</code> wird als "" interpretiert
	 *
	 * @return Objekt, �ber das auf das Konfigurationsobjekt zugegriffen werden kann
	 *
	 * @throws IllegalStateException    Die Version, mit der das Objekt g�ltig werden soll, wurde noch nicht mit {@link #setNextActiveVersion} festgelegt
	 * @throws IllegalArgumentException Die �bergebene Id des Objekts muss gr��er als die gr��te Id des Konfigurationsbereichs sein
	 * @see #getGreatestId
	 */
	ConfigurationObjectInfo createConfigurationObject(long objectID, long typeID, String pid, String name)
			throws IllegalStateException, IllegalArgumentException;

	/**
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zur�ck, die in der aktuellen Version aktuell sind.
	 *
	 * @return s.o.
	 */
	SystemObjectInformationInterface[] getCurrentObjects();

	/**
	 * Gibt alle aktuellen Objekte zur�ck, die als TypeId die �bergebene TypeId besitzen.
	 *
	 * @param typeId TypeId, die ein Objekt besitzen muss, damit es zur�ckgegeben wird
	 *
	 * @return Objekte, deren TypeId gleich der �bergebenen TypeId sind. Ist kein Objekt vorhanden, so wird eine leere Liste zur�ckgegeben.
	 */
	SystemObjectInformationInterface[] getActualObjects(long typeId);

	/**
	 * TBD vielleicht (raus, weil das mit dynamischen Objekten nicht funktioniert)
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zur�ck, die innerhalb des angegebenen
	 * Versionsbereichs g�ltig waren. Wurde ein Objekt in diesem Zeitraum als "ung�ltig" markiert, wird es ebenfalls
	 * zur�ckgegeben. Wird ein Objekt in diesem Bereich als aktuell markiert, wird es ebenfalls zur�ckgegeben.
	 *
	 * @param startVersion Version, ab der ein Objekt g�ltig sein muss, um zur�ckgegeben zu werden
	 * @param endVersion   Version, bis zu der Objekte zur�ckgegeben werden
	 * @return Objekte, die in dem angegebene Bereich zu einem Zeitpunkt g�ltig waren
	 */
//	SystemObjectInfo[] getObjects(short startVersion, short endVersion, List<Long> typeIds);

	/**
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zur�ck, die innerhalb des angegebenen Zeitbereichs g�ltig waren und deren TypeId
	 * gleich einer der �bergebenen TypeId�s ist. Der Parameter <code>timeSpecificationType</code> bestimmt, wann/wie lange ein Objekt g�ltig gewesen sein muss um
	 * in die L�sung aufgenommen zu werden.
	 * <p/>
	 *
	 * @param startTime             Zeitpunkt, ab der ein Objekt g�ltig sein muss, um zur�ckgegeben zu werden
	 * @param endTime               Zeitpunkt, bis zu der Objekte zur�ckgegeben werden
	 * @param kindOfTime            Ein Konfigurationsobjekt wird mit der Aktivierung einer Version g�ltig. Da der Konfigurationsverantwortliche die Version zu
	 *                              einem fr�heren Zeitpunkt aktiviert haben kann, als die lokale Konfiguration, legt dieser Parameter fest, welcher Zeitpunkt f�r
	 *                              ein Konfigurationsobjekt benutzt werden soll an dem es als "g�ltig" markiert wurde.
	 * @param timeSpecificationType Gibt die Art und Weise an, wie der Zeitraum zu betrachten ist.
	 * @param typeIds               Liste von TypeId�s. Damit ein Objekt zur�ckgegeben wird, muss die TypeId des Objekts mit einer TypeId in der Liste
	 *                              �bereinstimmen
	 *
	 * @return Objekte, die in dem angegebene Zeitbereich zu einem Zeitpunkt g�ltig waren und deren TypeId mit einer geforderten TypeId �bereinstimmt
	 */
	SystemObjectInformationInterface[] getObjects(
			long startTime, long endTime, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType, Collection<Long> typeIds
	);

	/**
	 * Diese Methode gibt alle Konfigurationsobjekte zur�ck, die in einer zuk�nftigen Version aktuell werden. 
	 *
	 * @return Konfigurationsobjekte, die zuk�nftig aktuell werden aber es in der aktuellen Version noch nicht sind.
	 */
	SystemObjectInformationInterface[] getNewObjects();

	/**
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zur�ck, die innerhalb des angegebenen
	 * Versionsbereichs als "ung�ltig" markiert wurden. Damit ein Objekt zur�ckgegeben wird, muss es in dem angegebenen
	 * Bereich aktuell gewesen/geworden sein und anschlie�en als "ung�ltig" markiert worden sein.
	 *
	 * @param startVersion Version, ab der ein Objekt als "ung�ltig" markiert werden muss, um zur�ckgegeben zu werden
	 * @param endVersion   Version, bis zu der Objekte zur�ckgegeben werden
	 * @return Objekte, die in dem angegebene Bereich zu einem Zeitpunkt als "ung�ltig" markiert wurden
	 */
	









	/**
	 * Stellt alle dynamischen Objekte und Konfigurationsobjekte zur Verf�gung.
	 *
	 * @return s.o.
	 */
	Iterator<SystemObjectInformationInterface> iterator();

	/**
	 * Diese Methode gibt ein Objekt zur�ck, das als ung�ltig markiert wurde. Ist in diesem Konfigurationsbereich kein Objekt mit der Id vorhanden, wird
	 * <code>null</code> zur�ckgegeben.
	 *
	 * @param id Id des geforderten Objekts
	 *
	 * @return Objekt, das als ung�ltig markiert wurde oder <code>null</code>
	 */
	SystemObjectInformationInterface getOldObject(long id);

	/**
	 * Diese Methode gibt Objekte zur�ck, die in dem angegebenen Zeibereich g�ltig waren.
	 *
	 * @param pid        Pid, �ber die die Objekte identifiziert werden
	 * @param startTime  Zeitpunkt, ab dem ein Objekt mit der angegebenen Pid g�ltig gewesen sein muss um zur�ckgegeben zu werden. War das Objekt vor dem
	 *                   angegebenen Zeitraum bereits g�ltig, so wird es ebenfalls zur�ckgegeben.
	 * @param endTime    Zeitpunkt, bis zu dem ein Objekt mit der angegebenen Pid g�ltig gewesen sein muss um zur�ckgegeben zu werden. Ist das Objekt �ber diesen
	 *                   Zeitraum hinweg g�ltig, so wird es ebenfalls zur�ckgegeben. Der Zeitpunkt darf nicht in der Zukunft liegen, der gr��t m�gliche Zeitpunkt
	 *                   ist die aktuelle Zeit.
	 * @param kindOfTime Legt fest, welcher Zeitpunkt bei einer aktivierten Version benutzt wird. Der Zeitpunkt der lokalen Aktivierung einer Version oder aber der
	 *                   Zeitpunkt der Aktivierung durch den Konfigurationsverantwortlichen.
	 *
	 * @return Alle Objekte, die �ber die Pid identifiziert werden und im gew�nschten Zeitbereich g�ltig waren. Sind keine Objekte vorhanden, wird ein leeres Array
	 *         zur�ckgegeben (Gr��e 0).
	 */
	SystemObjectInformationInterface[] getObjects(String pid, long startTime, long endTime, ConfigurationAreaTime kindOfTime);


	/**
	 * Alle Daten, die als Byte-Array gespeichert werden m�ssen, werden mit einem Serializer {@link de.bsvrz.sys.funclib.dataSerializer.Serializer} erstellt. Die benutzte Version ist in der
	 * gesamten Datei identisch. Ein Versionswechsel innerhalb der Datei ohne die Konvertierung aller Daten auf die neue Version ist nicht gestattet.
	 *
	 * @return Versionsnummer des Serialisierers mit dem alle Byte-Arrays erstellt wurden
	 */
	int getSerializerVersion();

	/**
	 * Legt die Version fest mit der Konfigurationsobjekte, die mit {@link #createConfigurationObject} erzeugt werden, g�ltig werden.
	 *
	 * @param nextActiveVersion Version, mit der Konfigurationsobjekte g�ltig werden. Die erste Version, mit der ein Objekt g�ltig sein kann, ist die 1. Die
	 *                          Versionen sind positive Ganzzahlen, die fortlaufend nummeriert sind.
	 */
	public void setNextActiveVersion(short nextActiveVersion);

	/**
	 * Liefert die Version, ab der neu erstellte Konfigurationsobjekte g�ltig werden.
	 *
	 * @return Liefert eine Version, ab der neu erstellte Konfigurationsobjekte g�ltig werden.
	 */
	public short getNextActiveVersion();

	/**
	 * Diese Methode reorganisiert eine Konfigurationsbereichsdatei. Die Mischmenge wird dabei, falls m�glich, verkleinert und die als ung�ltig markierten Objekte
	 * werden in die entsprechenden Bl�cke kopiert. Kommt es bei der Reorganisation zu einem Fehler, so wird der Zustand vor der Reorganisation wiederhergestellt.
	 * Alle Methoden, die einen Dateizugriff ben�tigen (flush, Objekt auf Invalid setzen, usw.) und w�hrend der Reorganisation aufgerufen werden, sind
	 * blockierend.
	 * <p/>
	 * Diese Methode darf nur durch den Konfigrationsverantwortlichen aufgerufen werden.
	 *
	 * @return true = Die Restrukturierung der Daten war erfolgreich; false = Die Restrukturierung der Daten hat nicht geklappt, es wird auf der alten Datei
	 *         weitergearbeitet, es sind keine Daten verloren gegangen
	 */
	public boolean restructure();

	/**
	 * Diese Methode wird aufgerufen, wenn der Konfigurationsverantwortliche eine neue Version aktiviert. Der Aufruf bewirkt, dass die Datei restrukturiert wird.
	 * Diese Methode muss druch den Konfigurationsverantwortlichen aufgerufen werden, wenn dieser die aktive Version wechselt.
	 *
	 * @return true = Die Reorganisation war erfolgreich; false = Die Reorganisation war nicht erfolgreich TBD am besten Konfiguration nicht starten
	 *
	 * @see #restructure
	 */
	public boolean initialVersionRestructure();

	/**
	 * Diese Methode gibt die gr��te vergebene laufende Nummer einer Id des Konfigurationsbereichs zur�ck. Die laufende Nummer ist in den ersten 40 Bits der Id
	 * kodiert.
	 *
	 * @return Die gr��te vergebene laufende Nummer einer Id des Konfigurationsbereich oder 0 falls die Id noch unbekannt ist. Der Wert 0 wird zum Beispiel dann
	 *         zur�ckgegeben, wenn der Konfigurationsbereich neu angelegt wurde und noch keine gr��te Id bekannt ist
	 */
	public long getGreatestId();
}
