/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.config;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

import java.util.Collection;
import java.util.List;

/**
 * Schnittstelle zum Zugriff auf Objekte und Eigenschaften eines Datenmodells. Der Zugriff auf die Objekte des Datenmodells wird in der Schnittstelle über
 * sogenannte Stellvertreterobjekte realisiert. Stellvertreterobjekte sind Objekte im Sinne der eingesetzten Programmiersprache und damit Elemente von Klassen.
 * System-Objekte und die zugehörigen Stellvertreterobjekte werden im Folgenden synonym verwendet. Die Klassen, die den Zugriff auf die Objekte ermöglichen,
 * können je nach Typ des jeweiligen Objekts unterschiedlich sein. Alle Objekte des Datenmodells müssen die {@link SystemObject Schnittstellenklasse für
 * System-Objekte} implementieren. Der Zugriff auf folgende Typen des Metamodells und des System-Datenmodells wird durch die jeweils angegebenen
 * Schnittstellenklassen ermöglicht. <table cellpadding="2" cellspacing="2" border="1"> <tr> <th>        Objekt-Typ                                    </th>
 * <th> Schnittstellenklasse </th></tr> <tr><td>        typ.konfigurationsObjekt </td> <td>{@link ConfigurationObject }</td></tr> <tr><td> typ.dynamischesObjekt
 * </td> <td>{@link		DynamicObject }</td></tr> <tr><td> typ.typ </td> <td>{@link SystemObjectType					   }<br/> {@link ConfigurationObjectType }</td></tr>
 * <tr><td> typ.attributgruppe </td> <td>{@link AttributeGroup }</td></tr> <tr><td> typ.aspekt </td> <td>{@link		Aspect }</td></tr> <tr><td> typ.attribut </td>
 * <td>{@link Attribute }</td></tr> <tr><td> typ.attributTyp </td> <td>{@link AttributeType }</td></tr> <tr><td> typ.zeichenketteAttributTyp </td> <td>{@link
 * StringAttributeType }</td></tr> <tr><td> typ.ganzzahlAttributTyp </td> <td>{@link IntegerAttributeType }</td></tr> <tr><td>        typ.werteBereich </td>
 * <td>{@link IntegerValueRange }</td></tr> <tr><td> typ.werteZustand </td> <td>{@link IntegerValueState }</td></tr> <tr><td> typ.kommazahlAttributTyp </td>
 * <td>{@link DoubleAttributeType }</td></tr> <tr><td> typ.zeitstempelAttributTyp                    </td> <td>{@link TimeAttributeType }</td></tr> <tr><td>
 * typ.objektReferenzAttributTyp </td> <td>{@link ReferenceAttributeType }</td></tr> <tr><td> typ.attributListenDefinition </td> <td>{@link
 * AttributeListDefinition				}</td></tr> <tr><td> typ.mengenVerwendung </td> <td>{@link ObjectSetUse						   }</td></tr> <tr><td> typ.mengenTyp </td>
 * <td>{@link ObjectSetType }</td></tr> <tr><td> typ.konfigurationsVerantwortlicher </td> <td>{@link ConfigurationAuthority }</td></tr> <tr><td>
 * typ.konfigurationsMenge </td> <td>{@link NonMutableSet }</td></tr> <tr><td> typ.dynamischeMenge </td> <td>{@link MutableSet }</td></tr> <tr><td>
 * typ.applikation                               </td> <td>{@link ClientApplication }</td></tr> <tr><td>        typ.datenverteiler </td> <td>{@link
 * DavApplication }</td></tr> </table>
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */


public interface DataModel extends ObjectLookup {

	/**
	 * Liefert die konfigurierenden Datensätze einer Attributgruppe für mehrere Objekte zurück. Als Aspekt wird dabei <code>asp.eigenschaften</code> angenommen.
	 * Die zurückgelieferten Datensätze werden auch lokal zwischengespeichert und können mit der Methode {@link SystemObject#getConfigurationData} ohne weitere
	 * Konfigurationsanfrage abgefragt werden. Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen
	 * Verzögerungszeiten eingesetzt werden.
	 *
	 * @param objects {@link SystemObject Systemobjekte} der gewünschten konfigurierenden Datensätze.
	 * @param atg     Attributgruppe der gewünschten Datensätze.
	 *
	 * @return Array mit den gewünschten konfigurierenden Datensätzen. Das Array enthält für jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg);

	/**
	 * Liefert die konfigurierenden Datensätze einer Attributgruppe und eines Aspekts für mehrere Objekte zurück. Die zurückgelieferten Datensätze werden auch
	 * lokal zwischengespeichert und können mit der Methode {@link SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage abgefragt werden. Die
	 * Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verzögerungszeiten eingesetzt werden.
	 *
	 * @param objects Liste der {@link SystemObject Systemobjekte} der gewünschten konfigurierenden Datensätze.
	 * @param atg     Attributgruppe der gewünschten Datensätze.
	 * @param asp     Aspekt der gewünschten Datensätze.
	 *
	 * @return Array mit den gewünschten konfigurierenden Datensätzen. Das Array enthält für jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg, Aspect asp);

	/**
	 * Liefert die konfigurierenden Datensätze einer Attributgruppe für mehrere Objekte zurück. Als Aspekt wird dabei <code>asp.eigenschaften</code> angenommen.
	 * Die zurückgelieferten Datensätze werden auch lokal zwischengespeichert und können mit der Methode {@link SystemObject#getConfigurationData} ohne weitere
	 * Konfigurationsanfrage abgefragt werden. Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen
	 * Verzögerungszeiten eingesetzt werden.
	 *
	 * @param objects Array mit den {@link SystemObject Systemobjekten} der gewünschten konfigurierenden Datensätze.
	 * @param atg     Attributgruppe der gewünschten Datensätze.
	 *
	 * @return Array mit den gewünschten konfigurierenden Datensätzen. Das Array enthält für jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg);

	/**
	 * Liefert die konfigurierenden Datensätze einer Attributgruppe und eines Aspekts für mehrere Objekte zurück. Die zurückgelieferten Datensätze werden auch
	 * lokal zwischengespeichert und können mit der Methode {@link SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage abgefragt werden. Die
	 * Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verzögerungszeiten eingesetzt werden.
	 *
	 * @param objects Array mit den {@link SystemObject Systemobjekten} der gewünschten konfigurierenden Datensätze.
	 * @param atg     Attributgruppe der gewünschten Datensätze.
	 * @param asp     Aspekt der gewünschten Datensätze.
	 *
	 * @return Array mit den gewünschten konfigurierenden Datensätzen. Das Array enthält für jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg, Aspect asp);

	/**
	 * Gibt die aktive Version des angegebenen Konfigurationsbereichs zurück.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @return Versionsnummer des Konfigurationsbereichs
	 */
	public short getActiveVersion(ConfigurationArea configurationArea);

	/**
	 * Liefert das System-Objekt mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des System-Objekts
	 *
	 * @return Das gewünschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 *
	 * @see DataModel
	 *
	 * @throws java.lang.IllegalArgumentException wenn der Parameter null ist
	 */
	public SystemObject getObject(String pid);

	/**
	 * Liefert das System-Objekt mit der angegebenen Objekt-ID zurück.
	 *
	 * @param id Die Objekt-ID des System-Objekts
	 *
	 * @return Das gewünschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen ID gibt.
	 *
	 * @see DataModel
	 */
	public SystemObject getObject(long id);

	/**
	 * Liefert eine Liste zurück, die zu den angegebenen IDs die zugehörigen System-Objekte enthält.
	 * Die Reihenfolge der Objekte der Liste entspricht der Reihenfolge der übergebenen IDs.
	 * Wurde ein Objekt nicht gefunden, enthält die Liste an der entsprechenden Position <code>null</code>.
	 * <p>
	 * Diese Methode kann bei der Anfrage nach mehreren IDs schneller sein, als mehrmals {@link #getObject(long)} aufzurufen.
	 * @param ids Array mit IDs
	 * @return zugehörige System-Objekte
	 * @see #getObject(long)
	 */
	List<SystemObject> getObjects(long... ids);

	/**
	 * Liefert eine Liste zurück, die zu den angegebenen PIDs die zugehörigen System-Objekte enthält.
	 * Die Reihenfolge der Objekte der Liste entspricht der Reihenfolge der übergebenen PIDs.
	 * Wurde ein Objekt nicht gefunden, enthält die Liste an der entsprechenden Position <code>null</code>.
	 * <p>
	 * Diese Methode kann bei der Anfrage nach mehreren PIDs schneller sein, als mehrmals {@link #getObject(java.lang.String)} aufzurufen.
	 * @param pids Array mit PIDs
	 * @return zugehörige System-Objekte
	 * @see #getObject(java.lang.String)
	 * @throws java.lang.IllegalArgumentException wenn ein Element des Parameters <code>null</code> ist
	 */
	List<SystemObject> getObjects(String... pids);

	/**
	 * Liefert eine Liste zurück, die zu den angegebenen IDs die zugehörigen System-Objekte enthält.
	 * Die Reihenfolge der Objekte der Liste entspricht der Reihenfolge der übergebenen IDs.
	 * Wurde ein Objekt nicht gefunden, enthält die Liste an der entsprechenden Position <code>null</code>.
	 * <p>
	 * Diese Methode kann bei der Anfrage nach mehreren IDs schneller sein, als mehrmals {@link #getObject(long)} aufzurufen.
	 * @param ids Liste mit IDs
	 * @return zugehörige System-Objekte
	 * @see #getObject(long)
	 * @throws java.lang.IllegalArgumentException wenn ein Element des Parameters <code>null</code> ist
	 */
	List<SystemObject> getObjectsById(Collection<Long> ids);

	/**
	 * Liefert eine Liste zurück, die zu den angegebenen PIDs die zugehörigen System-Objekte enthält.
	 * Die Reihenfolge der Objekte der Liste entspricht der Reihenfolge der übergebenen PIDs.
	 * Wurde ein Objekt nicht gefunden, enthält die Liste an der entsprechenden Position <code>null</code>.
	 * <p>
	 * Diese Methode kann bei der Anfrage nach mehreren PIDs schneller sein, als mehrmals {@link #getObject(java.lang.String)} aufzurufen.
	 * @param pids Liste mit PIDs
	 * @return zugehörige System-Objekte
	 * @see #getObject(java.lang.String)
	 * @throws java.lang.IllegalArgumentException wenn ein Element des Parameters <code>null</code> ist
	 */
	List<SystemObject> getObjectsByPid(Collection<String> pids);

	/**
	 * Liefert das Systemobjekt, das den Typ von Typobjekten darstellt.
	 *
	 * @return Das Typ-Typ-Objekt.
	 */
	public SystemObjectType getTypeTypeObject();

	/**
	 * Liefert die Basistypen, also die Typ-Objekte, die keinen anderen Typ erweitern, zurück. Basistypen sind z.B. die Objekte mit den Permanenten IDs
	 * "typ.konfigurationsObjekt" und "typ.dynamischesObjekt".
	 *
	 * @return Liste mit Typ-Objekten.
	 */
	public List<SystemObjectType> getBaseTypes();

	/**
	 * Liefert das Typ-Objekt mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des Typ-Objekts
	 *
	 * @return Das gewünschte Typ-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Typ-Objekt ist.
	 */
	public SystemObjectType getType(String pid);

	/**
	 * Liefert das Mengen-Typ-Objekt mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des Mengen-Typ-Objekts
	 *
	 * @return Das gewünschte Typ-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Mengen-Typ-Objekt ist.
	 */
	public ObjectSetType getObjectSetType(String pid);

	/**
	 * Liefert die Attributgruppe mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID der Attributgruppe
	 *
	 * @return Die gewünschte Attributgruppe oder <code>null</code>, wenn es kein Objekt mit der gewünschten PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid keine Attributgruppe ist.
	 */
	public AttributeGroup getAttributeGroup(String pid);

	/**
	 * Liefert den Attribut-Typ mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des Attribut-Typs
	 *
	 * @return Der gewünschte Attribut-Typ oder	<code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Attributtyp ist.
	 */
	public AttributeType getAttributeType(String pid);

	/**
	 * Liefert den Aspekt mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des Aspekts.
	 *
	 * @return Der gewünschte Aspekt oder <code>null</code>, wenn es kein Objekt mit der gewünschten PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Aspekt ist.
	 */
	public Aspect getAspect(String pid);

	/**
	 * Bestimmt die Attributgruppenverwendung mit der angegebenen Datenverteiler-Identifizierung.
	 *
	 * @param usageIdentification Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler.
	 *
	 * @return Zur Identifizierung gehörende Attributgruppenverwendung oder <code>null</code>, wenn es keine Attributgruppenverwendung mit der angegebenen
	 * Identifizierung gibt.
	 */
	public AttributeGroupUsage getAttributeGroupUsage(final long usageIdentification);

	/**
	 * Erzeugt ein neues Konfigurationsobjekt eines vorgegebenen Typs. Optional können auch Name und PID des neuen Objekts vorgegeben werden. Die verantwortliche
	 * Instanz des neuen Objektes kann nicht spezifiziert werden, da sie von der jeweiligen Konfiguration vergeben wird. Das neue Objekt wird erst mit Aktivierung
	 * der nächsten Konfigurationsversion gültig und im {@link ConfigurationAuthority#getDefaultConfigurationArea() Standard-Konfigurationsbereich} des
	 * {@link #getConfigurationAuthority() aktuellen KV} erstellt.
	 *
	 * @param type Typ des neuen Objekts.
	 * @param pid  PID des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @param sets Liste der Mengen des neuen Objekts oder <code>null</code>, wenn kein Mengen vergeben werden sollen.
	 *
	 * @return Stellvertreterobjekt für das neu angelegte Konfigurationsobjekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see ConfigurationObject
	 * @see SystemObject#isValid
	 * @see ConfigurationAuthority#getDefaultConfigurationArea()
	 * @deprecated Objekte werden innerhalb eines Bereichs {@link ConfigurationArea#createConfigurationObject erstellt}.
	 */
	@Deprecated
	ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List<? extends ObjectSet> sets) throws ConfigurationChangeException;

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs. Optional können auch Name und PID des neuen Objekts vorgegeben werden. Das neue Objekt
	 * wird sofort gültig und im {@link ConfigurationAuthority#getDefaultConfigurationArea() Standard-Konfigurationsbereich} des {@link #getConfigurationAuthority() aktuellen KV} erstellt.
	 *
	 * @param type Typ des neuen Objekts
	 * @param pid  PID des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 *
	 * @return Stellvertreterobjekt für das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see SystemObject
	 * @see SystemObject#isValid
	 * @deprecated Objekte werden innerhalb eines Bereichs {@link ConfigurationArea#createDynamicObject erzeugt}.
	 */
	@Deprecated
	DynamicObject createDynamicObject(SystemObjectType type, String pid, String name) throws ConfigurationChangeException;

	/**
	 * Liefert zu der angegebenen Pid den passenden Konfigurationsbereich.
	 *
	 * @param pid die Pid des Konfigurationsbereichs
	 *
	 * @return der Konfigurationsbereich zur angegebenen Pid
	 */
	public ConfigurationArea getConfigurationArea(String pid);

	/**
	 * Liefert den lokalen Verantwortlichen der gesamten Konfiguration.
	 *
	 * @return der Konfigurationsverantwortliche der Konfiguration
	 */
	public ConfigurationAuthority getConfigurationAuthority();

	/**
	 * Liefert die Pid des lokalen Verantwortlichen der gesamten Konfiguration.
	 *
	 * @return die Pid des Konfigurationsverantwortlichen
	 */
	public String getConfigurationAuthorityPid();

	/**
	 * Gibt die Objekte zurück, die zu der angegebenen Pid in dem angegebenen Zeitraum gültig waren.
	 *
	 * @param pid       die Pid der gewünschten Objekte
	 * @param startTime der zu betachtende Startzeitpunkt des Anfragezeitraums
	 * @param endTime   der zu betrachtende Endzeitpunkt des Anfragezeitraums
	 *
	 * @return Die Objekte, die zu der angegebenen Pid in dem angegebenen Zeitraum gültig waren.
	 */
	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime);

	/**
	 * Gibt die Objekte zurück, die in einem der angegebenen Konfigurationsbereiche sind und deren Objekttyp in der angegebenen Objekttyp-Menge ist.
	 *
	 * @param configurationAreas      Konfigurationsbereiche, die zu berücksichtigen sind. Wird <code>null</code> übergeben, so gilt dies als Wildcard und alle
	 *                                Konfigurationsbereiche werden betrachtet.
	 * @param systemObjectTypes       Objekttypen, die zu berücksichtigen sind. Wird <code>null</code> übergeben, so gilt dies als Wildcard und alle Objekttypen
	 *                                werden betrachtet.
	 * @param objectTimeSpecification Gibt den Gültigkeitsbereich der geforderten Objekte an.
	 *
	 * @return Die gewünschten System-Objekte oder eine leere Collection, falls es keine passenden Objekte gibt.
	 */
	public Collection<SystemObject> getObjects(
			Collection<ConfigurationArea> configurationAreas, Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification objectTimeSpecification
	);

	/**
	 * Gibt ein Objekt zurück, mit dem die Benutzer der Konfiguration verwaltet werden können.
	 *
	 * @return Objekt zur Benutzerverwaltung.  
	 *  */
	public UserAdministration getUserAdministration();

	/**
	 * Veranlasst die Konfiguration, alle Konfigurationsdateien zu sichern. Diese Funktion wartet auf das Beenden des Vorgangs. Wird der Auftrag über den
	 * Datenverteiler ausgeführt (DafDataModel) kann die Konfiguration andere Anfragen parallel ausführen. Wird die Funktion lokal ausgeführt (ConfigDataModel),
	 * kann es möglicherweise sinnvoll sein, die Funktion in einem eigenen Thread auszuführen.
	 *
	 * @param targetDirectory Relatives Zielverzeichnis innerhalb des in der Konfiguration (mit dem Parameter -sicherungsVerzeichnis) festgelegten
	 *                        Sicherungsordners. Wird null oder ein Leerstring angegeben, generiert die Konfiguration aus aktuellem Datum und Uhrzeit einen neuen
	 *                        Pfadnamen. Falls das {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel} direkt benutzt wird und mit {@link
	 *                        de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#setBackupBaseDirectory(java.io.File) } noch keine Zielverzeichnis angelegt
	 *                        wurde, kann auch ein absoluter Pfadname angegeben werden. Ein relativer Pfadname würde dann relativ zum Arbeitsverzeichnis
	 *                        interpretiert.
	 * @param callback        Objekt, an das Statusmeldungen gesendet werden oder null, falls keine Rückmeldungen gewünscht sind
	 *
	 * @return Objekt, das Informationen über das Ergebnis des Sicherungsvorgangs enthält
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationTaskException Der Backup-Vorgang konnte nicht durchgeführt werden, beispielsweise weil das Zielverzeichnis falsch war. Falls das
	 *                                    Sichern einzelner Dateien fehlschlägt wird keine solche Exception geworfen, stattdessen findet man innerhalb vom callback
	 *                                    eventuelle Fehlschläge und BackupResult.getFailed ist größer 0.
	 * @throws de.bsvrz.dav.daf.main.impl.config.request.RequestException           Fehler bei der Übertragung der Anfrage oder beim Empfang von Statusmeldungen der Konfiguration. Achtung: Man kann nicht
	 *                                    zwingend darauf schließen, dass der Backupvorgang nicht erfolgreich war, wenn eine Exception geworfen wurde. Wenn während
	 *                                    des Vorgangs beispielsweise die Verbindung zwischen Datenverteiler und Konfiguration abbricht, wird eine Exception
	 *                                    geworfen, aber die Konfiguration wird den Vorgang vermutlich dennoch korrekt beenden.
	 */
	BackupResult backupConfigurationFiles(String targetDirectory, BackupProgressCallback callback) throws ConfigurationTaskException, RequestException;

	/**
	 * Veranlasst die Konfiguration, ausgewählte Konfigurationsdateien zu sichern. Diese Funktion wartet auf das Beenden des Vorgangs. Wird der Auftrag über den
	 * Datenverteiler ausgeführt (DafDataModel) kann die Konfiguration andere Anfragen parallel ausführen. Wird die Funktion lokal ausgeführt (ConfigDataModel),
	 * kann es möglicherweise sinnvoll sein, die Funktion in einem eigenen Thread auszuführen.
	 *
	 * @param targetDirectory Relatives Zielverzeichnis innerhalb des in der Konfiguration (mit dem Parameter -sicherungsVerzeichnis) festgelegten
	 *                        Sicherungsordners. Wird null oder ein Leerstring angegeben, generiert die Konfiguration aus aktuellem Datum und Uhrzeit einen neuen
	 *                        Pfadnamen. Falls das {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel} direkt benutzt wird und mit {@link
	 *                        de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#setBackupBaseDirectory(java.io.File) } noch keine Zielverzeichnis angelegt
	 *                        wurde, kann auch ein absoluter Pfadname angegeben werden. Ein relativer Pfadname würde dann relativ zum Arbeitsverzeichnis
	 *                        interpretiert.
	 * @param configurationAuthority  Konfigurationsverantwortlicher, dessen Konfigurations-Dateien gesichert werden sollen. Falls null werden
	 *                                alle Dateien gesichert.
	 * @param callback        Objekt, an das Statusmeldungen gesendet werden oder null, falls keine Rückmeldungen gewünscht sind
	 *
	 * @return Objekt, das Informationen über das Ergebnis des Sicherungsvorgangs enthält
	 *
	 * @throws ConfigurationTaskException Der Backup-Vorgang konnte nicht durchgeführt werden, beispielsweise weil das Zielverzeichnis falsch war. Falls das
	 *                                    Sichern einzelner Dateien fehlschlägt wird keine solche Exception geworfen, stattdessen findet man innerhalb vom callback
	 *                                    eventuelle Fehlschläge und BackupResult.getFailed ist größer 0.
	 * @throws RequestException           Fehler bei der Übertragung der Anfrage oder beim Empfang von Statusmeldungen der Konfiguration. Achtung: Man kann nicht
	 *                                    zwingend darauf schließen, dass der Backupvorgang nicht erfolgreich war, wenn eine Exception geworfen wurde. Wenn während
	 *                                    des Vorgangs beispielsweise die Verbindung zwischen Datenverteiler und Konfiguration abbricht, wird eine Exception
	 *                                    geworfen, aber die Konfiguration wird den Vorgang vermutlich dennoch korrekt beenden.
	 */
	public BackupResult backupConfigurationFiles(String targetDirectory, final ConfigurationAuthority configurationAuthority, BackupProgressCallback callback) throws ConfigurationTaskException, RequestException;
}
