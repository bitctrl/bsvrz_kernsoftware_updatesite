/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ConfigurationAreaFile {

	/**
	 * Diese Methode gibt Informationen über das Konfigurationsobjekt zurück, das den Konfigurationsbereich darstellt.
	 *
	 * @return Informationen über das Objekt, das den Konfigurationsbereich darstellt
	 */
	ConfigurationObjectInfo getConfigurationAreaInfo();

	/**
	 * Diese Methode wird aufgerufen, wenn alle modifizierten Objekte des Konfigurationsbereichs persistent gespeichert werden sollen.
	 *
	 * @throws IOException Fehler beim Zugriff auf die Datei in der der Konfigurationsbereich gespeichert ist
	 */
	void flush() throws IOException;

	/**
	 * Diese Methode wird aufgerufen, wenn das System heruntergefahren soll. Es müssen alle Daten gespeichert werden (siehe {@link #flush} und ein Zustand
	 * hergestellt werden, mit dem das System zu einem späteren Zeitpunkt wieder hochgefahren werden kann.
	 *
	  * @throws IOException Fehler beim Zugriff auf die Datei in der der Konfigurationsbereich gespeichert ist
	 */
	void close() throws IOException;


	/**
	 * Diese Methode erzeugt ein dynamisches Objekt, das sofort gültig ist.
	 *
	 * @param objectID          ID des Objekts
	 * @param typeID            Typ des Objekts, der Typ wird über die ID festgelegt
	 * @param pid               Pid des Objekts
	 * @param simulationVariant Simulationsvariante unter der das dynamische Objekt gültig ist
	 * @param name              Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param persistenceMode Soll das dynamische Objekte persistent gespeichert werden
	 *
	 * @return Objekt, über das auf das dynamische Objekt zugegriffen werden kann
	 *
	 * @throws IllegalArgumentException Die übergebene Id des Objekts muss größer als die größte Id des Konfigurationsbereichs sein
	 * @see #getGreatestId
	 */
	DynamicObjectInfo createDynamicObject(long objectID, long typeID, String pid, short simulationVariant, String name, DynamicObjectType.PersistenceMode persistenceMode)
			throws IllegalArgumentException, IllegalStateException;

	/**
	 * Diese Methode erzeugt ein Konfigurationsobjekt.
	 *
	 * @param objectID ID des Objekts
	 * @param typeID   Typ des Objekts, der Typ wird über die ID festgelegt
	 * @param pid      Pid des Objekts
	 * @param name     Name des Objekts, <code>null</code> wird als "" interpretiert
	 *
	 * @return Objekt, über das auf das Konfigurationsobjekt zugegriffen werden kann
	 *
	 * @throws IllegalStateException    Die Version, mit der das Objekt gültig werden soll, wurde noch nicht mit {@link #setNextActiveVersion} festgelegt
	 * @throws IllegalArgumentException Die übergebene Id des Objekts muss größer als die größte Id des Konfigurationsbereichs sein
	 * @see #getGreatestId
	 */
	ConfigurationObjectInfo createConfigurationObject(long objectID, long typeID, String pid, String name)
			throws IllegalStateException, IllegalArgumentException;

	/**
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zurück, die in der aktuellen Version aktuell sind.
	 *
	 * @return s.o.
	 */
	SystemObjectInformationInterface[] getCurrentObjects();

	/**
	 * Gibt alle aktuellen Objekte zurück, die als TypeId die übergebene TypeId besitzen.
	 *
	 * @param typeId TypeId, die ein Objekt besitzen muss, damit es zurückgegeben wird
	 *
	 * @return Objekte, deren TypeId gleich der übergebenen TypeId sind. Ist kein Objekt vorhanden, so wird ein leeres Array zurückgegeben.
	 */
	SystemObjectInformationInterface[] getActualObjects(long typeId);

	/**
	 * Gibt alle aktuellen Objekte zurück, die als TypeId einen der übergebenen TypeIds besitzen.
	 *
	 * @param typeIds TypeIds, die ein Objekt besitzen muss, damit es zurückgegeben wird
	 *
	 * @return Objekte, deren TypeId gleich einem der übergebenen TypeIds sind. Ist kein Objekt vorhanden, so wird ein leeres Array zurückgegeben.
	 */
	SystemObjectInformationInterface[] getActualObjects(Collection<Long> typeIds);

	/**
	 * Diese Methode gibt alle dynamischen Objekte und Konfigurationsobjekte zurück, die innerhalb des angegebenen Zeitbereichs gültig waren und deren TypeId
	 * gleich einer der übergebenen TypeIdŽs ist. Der Parameter <code>timeSpecificationType</code> bestimmt, wann/wie lange ein Objekt gültig gewesen sein muss um
	 * in die Lösung aufgenommen zu werden.
	 * <p>
	 *
	 * @param startTime             Zeitpunkt, ab der ein Objekt gültig sein muss, um zurückgegeben zu werden
	 * @param endTime               Zeitpunkt, bis zu der Objekte zurückgegeben werden
	 * @param kindOfTime            Ein Konfigurationsobjekt wird mit der Aktivierung einer Version gültig. Da der Konfigurationsverantwortliche die Version zu
	 *                              einem früheren Zeitpunkt aktiviert haben kann, als die lokale Konfiguration, legt dieser Parameter fest, welcher Zeitpunkt für
	 *                              ein Konfigurationsobjekt benutzt werden soll an dem es als "gültig" markiert wurde.
	 * @param timeSpecificationType Gibt die Art und Weise an, wie der Zeitraum zu betrachten ist.
	 * @param typeIds               Liste von TypeIdŽs. Damit ein Objekt zurückgegeben wird, muss die TypeId des Objekts mit einer TypeId in der Liste
	 *                              übereinstimmen
	 *
	 * @return Objekte, die in dem angegebene Zeitbereich zu einem Zeitpunkt gültig waren und deren TypeId mit einer geforderten TypeId übereinstimmt
	 */
	SystemObjectInformationInterface[] getObjects(
			long startTime, long endTime, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType, Collection<Long> typeIds
	);

	/**
	 * Diese Methode gibt alle Konfigurationsobjekte zurück, die in einer zukünftigen Version aktuell werden. 
	 *
	 * @return Konfigurationsobjekte, die zukünftig aktuell werden aber es in der aktuellen Version noch nicht sind.
	 */
	SystemObjectInformationInterface[] getNewObjects();

	/**
	 * Stellt alle dynamischen Objekte und Konfigurationsobjekte zur Verfügung.
	 *
	 * @return s.o.
	 *
	 * @deprecated Wird aktuell nicht mehr benutzt, aktuelle Implementierung ist sehr ineffizient. Stattdessen forEach() benutzen.
	 */
	@Deprecated
	Iterator<SystemObjectInformationInterface> iterator();

	/**
	 * Iteriert über alle Objekte in diesem Bereich.
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	void forEach(Consumer<? super SystemObjectInformationInterface> consumer);

	/**
	 * Iteriert über alle Konfigurationsobjekte in den NGA-Blöcken in diesem Bereich.
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	void forEachOldConfigurationObject(Consumer<? super ConfigurationObjectInfo> consumer);

	/**
	 * Iteriert über alle dynamischen Objekte im NGDyn-Block in diesem Bereich.
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	void forEachOldDynamicObject(Consumer<? super DynamicObjectInfo> consumer);

	/**
	 * Iteriert über alle Objekte in der Mischmenge in diesem Bereich.
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	void forEachMixedObject(Consumer<? super SystemObjectInformationInterface> consumer);

	/**
	 * Diese Methode gibt ein Objekt zurück, das als ungültig markiert wurde. Ist in diesem Konfigurationsbereich kein Objekt mit der Id vorhanden, wird
	 * <code>null</code> zurückgegeben.
	 *
	 * @param id Id des geforderten Objekts
	 *
	 * @return Objekt, das als ungültig markiert wurde oder <code>null</code>
	 */
	SystemObjectInformationInterface getOldObject(long id);

	/**
	 * Diese Methode gibt Objekte zurück, die in dem angegebenen Zeibereich gültig waren.
	 *
	 * @param pid        Pid, über die die Objekte identifiziert werden
	 * @param startTime  Zeitpunkt, ab dem ein Objekt mit der angegebenen Pid gültig gewesen sein muss um zurückgegeben zu werden. War das Objekt vor dem
	 *                   angegebenen Zeitraum bereits gültig, so wird es ebenfalls zurückgegeben.
	 * @param endTime    Zeitpunkt, bis zu dem ein Objekt mit der angegebenen Pid gültig gewesen sein muss um zurückgegeben zu werden. Ist das Objekt über diesen
	 *                   Zeitraum hinweg gültig, so wird es ebenfalls zurückgegeben. Der Zeitpunkt darf nicht in der Zukunft liegen, der größt mögliche Zeitpunkt
	 *                   ist die aktuelle Zeit.
	 * @param kindOfTime Legt fest, welcher Zeitpunkt bei einer aktivierten Version benutzt wird. Der Zeitpunkt der lokalen Aktivierung einer Version oder aber der
	 *                   Zeitpunkt der Aktivierung durch den Konfigurationsverantwortlichen.
	 * @param simulationVariant Zusätzlich zu berücksichtigende Simulationsvariante (außer 0). Es wird nicht nach dieser Simulationsvariante gefilert,
	 *                          d. h. wenn der Aufrufer nur Objekte benötigt, die innerhalb einer speziellen Simulation gültig sind,
	 *                          dann muss er selbst die zurückgegebenen Objekte noch einmal selbst filtern und dabei auch die in der
	 *                          Simulationsstrecke definierten Typen beachten.
	 *
	 * @return Alle Objekte, die über die Pid identifiziert werden und im gewünschten Zeitbereich gültig waren. Sind keine Objekte vorhanden, wird ein leeres Array
	 *         zurückgegeben (Größe 0). Wenn eine Simulationsvariante außer 0 angegeben wurde, werden sowohl Objekte dieser Variante als auch
	 *         nicht simulierte Objekte (SimVar = 0) zurückgegeben.
	 */
	List<SystemObjectInformationInterface> getObjects(String pid, long startTime, long endTime, ConfigurationAreaTime kindOfTime, final short simulationVariant);

	/**
	 * Alle Daten, die als Byte-Array gespeichert werden müssen, werden mit einem Serializer {@link de.bsvrz.sys.funclib.dataSerializer.Serializer} erstellt. Die benutzte Version ist in der
	 * gesamten Datei identisch. Ein Versionswechsel innerhalb der Datei ohne die Konvertierung aller Daten auf die neue Version ist nicht gestattet.
	 *
	 * @return Versionsnummer des Serialisierers mit dem alle Byte-Arrays erstellt wurden
	 */
	int getSerializerVersion();

	/**
	 * Legt die Version fest mit der Konfigurationsobjekte, die mit {@link #createConfigurationObject} erzeugt werden, gültig werden.
	 *
	 * @param nextActiveVersion Version, mit der Konfigurationsobjekte gültig werden. Die erste Version, mit der ein Objekt gültig sein kann, ist die 1. Die
	 *                          Versionen sind positive Ganzzahlen, die fortlaufend nummeriert sind.
	 */
	public void setNextActiveVersion(short nextActiveVersion);

	/**
	 * Liefert die Version, ab der neu erstellte Konfigurationsobjekte gültig werden.
	 *
	 * @return Liefert eine Version, ab der neu erstellte Konfigurationsobjekte gültig werden.
	 */
	public short getNextActiveVersion();

	/**
	 * Diese Methode reorganisiert eine Konfigurationsbereichsdatei. Die Mischmenge wird dabei, falls möglich, verkleinert und die als ungültig markierten Objekte
	 * werden in die entsprechenden Blöcke kopiert. Kommt es bei der Reorganisation zu einem Fehler, so wird der Zustand vor der Reorganisation wiederhergestellt.
	 * Alle Methoden, die einen Dateizugriff benötigen (flush, Objekt auf Invalid setzen, usw.) und während der Reorganisation aufgerufen werden, sind
	 * blockierend.
	 * <p>
	 * Diese Methode darf nur durch den Konfigrationsverantwortlichen aufgerufen werden.
	 *
	 * @return true = Die Restrukturierung der Daten war erfolgreich; false = Die Restrukturierung der Daten hat nicht geklappt, es wird auf der alten Datei
	 *         weitergearbeitet, es sind keine Daten verloren gegangen
	 *
	 * @deprecated Bitte Restrukturierungsart angeben
	 */
	@Deprecated
	public boolean restructure();

	/**
	 * Diese Methode reorganisiert eine Konfigurationsbereichsdatei. Die Mischmenge wird dabei, falls möglich, verkleinert und die als ungültig markierten Objekte
	 * werden in die entsprechenden Blöcke kopiert. Kommt es bei der Reorganisation zu einem Fehler, so wird der Zustand vor der Reorganisation wiederhergestellt.
	 * Alle Methoden, die einen Dateizugriff benötigen (flush, Objekt auf Invalid setzen, usw.) und während der Reorganisation aufgerufen werden, sind
	 * blockierend.
	 * <p>
	 * Diese Methode darf nur durch den Konfigrationsverantwortlichen aufgerufen werden.
	 *
	 * @param mode Restrukturierungsart (siehe {@link de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile.RestructureMode RestructureMode}
	 * @throws java.io.IOException Falls ein Fehler bei der Restrukturierung auftrat. In diesem Fall bleibt der vorherige Zustand erhalten.
	 */
	public void restructure(RestructureMode mode) throws IOException;

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
	 * Diese Methode gibt die größte vergebene laufende Nummer einer Id des Konfigurationsbereichs zurück. Die laufende Nummer ist in den ersten 40 Bits der Id
	 * kodiert.
	 *
	 * @return Die größte vergebene laufende Nummer einer Id des Konfigurationsbereich oder 0 falls die Id noch unbekannt ist. Der Wert 0 wird zum Beispiel dann
	 *         zurückgegeben, wenn der Konfigurationsbereich neu angelegt wurde und noch keine größte Id bekannt ist
	 */
	public long getGreatestId();

	/**
	 * Markiert eine Menge von dynamischen Objekten als ausreichend als und nicht mehr Referenziert, sodass diese ggf. beim nächsten Neustart
	 * endgültig gelöscht werden können. Jedes dynamische Objekt muss mindestens zweimal durch diese Methode markiert werden (auch über einen
	 * Neustart hinweg) bevor es gelöscht wird, da das Objekt mit dem ersten Aufruf zuerst als nicht mehr referenzierbar markiert wird
	 * und dann beim zweiten Aufruf sichergestellt ist, dass das Objekt in Zukunft nicht mehr referenziert werden kann.
	 *
	 * @param objectsToDelete Menge mit zu löschenden dynamischen Objekten dieses Bereichs (Objekt-IDs)
	 */
	void markObjectsForDeletion(final List<Long> objectsToDelete);

	/**
	 * Prüft, ob ein angegebenenes Objekt von anderen Objekten referenzert werden darf
	 * @param systemObjectInfo Objekt-Info (Objekt sollte zum aktuellen Bereich gehören)
	 * @return true, falls es referenziert werden darf (es bisher nicht als {@link #markObjectsForDeletion(java.util.List) zu Löschen markiert} wurde,
	 * sonst false
	 */
	boolean referenceAllowed(SystemObjectInformationInterface systemObjectInfo);

	/**
	 * Definiert die Art einer Restrukturierung
	 */
	public enum RestructureMode {
		/**
		 * Volle Restrukturierung der Konfigurationsobjekte und dynamischen Dbjekte. Lücken (Gaps) werden gefüllt.
		 * Alle Dateipositionen können sich ändern, daher darf diese Restrukturierung nur Offline ausgeführt werden,
		 * oder bevor Dateipositionen gecacht werden (also direkt beim Start).
		 */
		FullRestructure,
		/**
		 * Restrukturierung der dynamischen Objekte im laufenden Betrieb. Die Objekte, die sich vorher in der Mischmenge befanden,
		 * erhalten (sehr wahrscheinlich) eine neue Dateipostion. Ungültige dynamische Objekte werden in den NgDyn-Block verschoben,
		 * Gültige und zukünftige Objekte bleiben in der Mischmenge, wandern dort aber ggf. an eine andere Position.
		 */
		DynamicObjectRestructure,
		/**
		 * Volle Restrukturierung, bei der zusätzlich Objekte in
		 * {@link ConfigAreaFile#_objectsPendingDeletion} gelöscht (d. h. nicht mitkopiert) werden.
		 */
		DeleteObjectsPermanently
	}
}
