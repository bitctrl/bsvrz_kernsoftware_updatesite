/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.config;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf Objekte und Eigenschaften eines Datenmodells. Der Zugriff auf die Objekte des Datenmodells wird in der Schnittstelle �ber
 * sogenannte Stellvertreterobjekte realisiert. Stellvertreterobjekte sind Objekte im Sinne der eingesetzten Programmiersprache und damit Elemente von Klassen.
 * System-Objekte und die zugeh�rigen Stellvertreterobjekte werden im Folgenden synonym verwendet. Die Klassen, die den Zugriff auf die Objekte erm�glichen,
 * k�nnen je nach Typ des jeweiligen Objekts unterschiedlich sein. Alle Objekte des Datenmodells m�ssen die {@link SystemObject Schnittstellenklasse f�r
 * System-Objekte} implementieren. Der Zugriff auf folgende Typen des Metamodells und des System-Datenmodells wird durch die jeweils angegebenen
 * Schnittstellenklassen erm�glicht. <table cellpadding="2" cellspacing="2" border="1"> <tr> <th>        Objekt-Typ                                    </th>
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
 * @version $Revision: 8278 $ / $Date: 2010-10-20 11:44:07 +0200 (Wed, 20 Oct 2010) $ / ($Author: jh $)
 */


public interface DataModel extends ObjectLookup {

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppe f�r mehrere Objekte zur�ck. Als Aspekt wird dabei <code>asp.eigenschaften</code> angenommen.
	 * Die zur�ckgelieferten Datens�tze werden auch lokal zwischengespeichert und k�nnen mit der Methode {@link SystemObject#getConfigurationData} ohne weitere
	 * Konfigurationsanfrage abgefragt werden. Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen
	 * Verz�gerungszeiten eingesetzt werden.
	 *
	 * @param objects {@link SystemObject Systemobjekte} der gew�nschten konfigurierenden Datens�tze.
	 * @param atg     Attributgruppe der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Das Array enth�lt f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg);

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppe und eines Aspekts f�r mehrere Objekte zur�ck. Die zur�ckgelieferten Datens�tze werden auch
	 * lokal zwischengespeichert und k�nnen mit der Methode {@link SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage abgefragt werden. Die
	 * Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verz�gerungszeiten eingesetzt werden.
	 *
	 * @param objects Liste der {@link SystemObject Systemobjekte} der gew�nschten konfigurierenden Datens�tze.
	 * @param atg     Attributgruppe der gew�nschten Datens�tze.
	 * @param asp     Aspekt der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Das Array enth�lt f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg, Aspect asp);

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppe f�r mehrere Objekte zur�ck. Als Aspekt wird dabei <code>asp.eigenschaften</code> angenommen.
	 * Die zur�ckgelieferten Datens�tze werden auch lokal zwischengespeichert und k�nnen mit der Methode {@link SystemObject#getConfigurationData} ohne weitere
	 * Konfigurationsanfrage abgefragt werden. Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen
	 * Verz�gerungszeiten eingesetzt werden.
	 *
	 * @param objects Array mit den {@link SystemObject Systemobjekten} der gew�nschten konfigurierenden Datens�tze.
	 * @param atg     Attributgruppe der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Das Array enth�lt f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg);

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppe und eines Aspekts f�r mehrere Objekte zur�ck. Die zur�ckgelieferten Datens�tze werden auch
	 * lokal zwischengespeichert und k�nnen mit der Methode {@link SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage abgefragt werden. Die
	 * Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verz�gerungszeiten eingesetzt werden.
	 *
	 * @param objects Array mit den {@link SystemObject Systemobjekten} der gew�nschten konfigurierenden Datens�tze.
	 * @param atg     Attributgruppe der gew�nschten Datens�tze.
	 * @param asp     Aspekt der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Das Array enth�lt f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe-Aspekt
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg, Aspect asp);

	/**
	 * Gibt die aktive Version des angegebenen Konfigurationsbereichs zur�ck.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @return Versionsnummer des Konfigurationsbereichs
	 */
	public short getActiveVersion(ConfigurationArea configurationArea);

	/**
	 * Liefert das System-Objekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 *
	 * @see DataModel
	 */
	public SystemObject getObject(String pid);

	/**
	 * Liefert das System-Objekt mit der angegebenen Objekt-ID zur�ck.
	 *
	 * @param id Die Objekt-ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen ID gibt.
	 *
	 * @see DataModel
	 */
	public SystemObject getObject(long id);

	/**
	 * Liefert das Systemobjekt, das den Typ von Typobjekten darstellt.
	 *
	 * @return Das Typ-Typ-Objekt.
	 */
	public SystemObjectType getTypeTypeObject();

	/**
	 * Liefert die Basistypen, also die Typ-Objekte, die keinen anderen Typ erweitern, zur�ck. Basistypen sind z.B. die Objekte mit den Permanenten IDs
	 * "typ.konfigurationsObjekt" und "typ.dynamischesObjekt".
	 *
	 * @return Liste mit Typ-Objekten.
	 */
	public List<SystemObjectType> getBaseTypes();

	/**
	 * Liefert das Typ-Objekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des Typ-Objekts
	 *
	 * @return Das gew�nschte Typ-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Typ-Objekt ist.
	 */
	public SystemObjectType getType(String pid);

	/**
	 * Liefert das Mengen-Typ-Objekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des Mengen-Typ-Objekts
	 *
	 * @return Das gew�nschte Typ-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Mengen-Typ-Objekt ist.
	 */
	public ObjectSetType getObjectSetType(String pid);

	/**
	 * Liefert die Attributgruppe mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID der Attributgruppe
	 *
	 * @return Die gew�nschte Attributgruppe oder <code>null</code>, wenn es kein Objekt mit der gew�nschten PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid keine Attributgruppe ist.
	 */
	public AttributeGroup getAttributeGroup(String pid);

	/**
	 * Liefert den Attribut-Typ mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des Attribut-Typs
	 *
	 * @return Der gew�nschte Attribut-Typ oder	<code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Attributtyp ist.
	 */
	public AttributeType getAttributeType(String pid);

	/**
	 * Liefert den Aspekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des Aspekts.
	 *
	 * @return Der gew�nschte Aspekt oder <code>null</code>, wenn es kein Objekt mit der gew�nschten PID gibt.
	 * @throws IllegalArgumentException Wenn das Objekt mit der angegebenen Pid kein Aspekt ist.
	 */
	public Aspect getAspect(String pid);

	/**
	 * Bestimmt die Attributgruppenverwendung mit der angegebenen Datenverteiler-Identifizierung.
	 *
	 * @param usageIdentification Identifizierung dieser Attributgruppenverwendung bei der Kommunikation �ber den Datenverteiler.
	 *
	 * @return Zur Identifizierung geh�rende Attributgruppenverwendung oder <code>null</code>, wenn es keine Attributgruppenverwendung mit der angegebenen
	 * Identifizierung gibt.
	 */
	public AttributeGroupUsage getAttributeGroupUsage(final long usageIdentification);

	/**
	 * Erzeugt ein neues Konfigurationsobjekt eines vorgegebenen Typs. Optional k�nnen auch Name und PID des neuen Objekts vorgegeben werden. Die verantwortliche
	 * Instanz des neuen Objektes kann nicht spezifiziert werden, da sie von der jeweiligen Konfiguration vergeben wird. Das neue Objekt wird erst mit Aktivierung
	 * der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @param type Typ des neuen Objekts.
	 * @param pid  PID des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 * @param sets Liste der Mengen des neuen Objekts oder <code>null</code>, wenn kein Mengen vergeben werden sollen.
	 *
	 * @return Stellvertreterobjekt f�r das neu angelegte Konfigurationsobjekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see ConfigurationObject
	 * @see SystemObject#isValid
	 * @deprecated Objekte werden innerhalb eines Bereichs {@link ConfigurationArea#createConfigurationObject erstellt}.
	 */
	ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List sets) throws ConfigurationChangeException;

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs. Optional k�nnen auch Name und PID des neuen Objekts vorgegeben werden. Das neue Objekt
	 * wird sofort g�ltig.
	 *
	 * @param type Typ des neuen Objekts
	 * @param pid  PID des neuen Objekts.
	 * @param name Name des neuen Objekts.
	 *
	 * @return Stellvertreterobjekt f�r das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see SystemObject
	 * @see SystemObject#isValid
	 * @deprecated Objekte werden innerhalb eines Bereichs {@link ConfigurationArea#createDynamicObject erzeugt}.
	 */
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
	 * Gibt die Objekte zur�ck, die zu der angegebenen Pid in dem angegebenen Zeitraum g�ltig waren.
	 *
	 * @param pid       die Pid der gew�nschten Objekte
	 * @param startTime der zu betachtende Startzeitpunkt des Anfragezeitraums
	 * @param endTime   der zu betrachtende Endzeitpunkt des Anfragezeitraums
	 *
	 * @return Die Objekte, die zu der angegebenen Pid in dem angegebenen Zeitraum g�ltig waren.
	 */
	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime);

	/**
	 * Gibt die Objekte zur�ck, die in einem der angegebenen Konfigurationsbereiche sind und deren Objekttyp in der angegebenen Objekttyp-Menge ist.
	 *
	 * @param configurationAreas      Konfigurationsbereiche, die zu ber�cksichtigen sind. Wird <code>null</code> �bergeben, so gilt dies als Wildcard und alle
	 *                                Konfigurationsbereiche werden betrachtet.
	 * @param systemObjectTypes       Objekttypen, die zu ber�cksichtigen sind. Wird <code>null</code> �bergeben, so gilt dies als Wildcard und alle Objekttypen
	 *                                werden betrachtet.
	 * @param objectTimeSpecification Gibt den G�ltigkeitsbereich der geforderten Objekte an.
	 *
	 * @return Die gew�nschten System-Objekte oder eine leere Collection, falls es keine passenden Objekte gibt.
	 */
	public Collection<SystemObject> getObjects(
			Collection<ConfigurationArea> configurationAreas, Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification objectTimeSpecification
	);

	/**
	 * Gibt ein Objekt zur�ck, mit dem die Benutzer der Konfiguration verwaltet werden k�nnen.
	 *
	 * @return Objekt zur Benutzerverwaltung.  
	 *  */
	public UserAdministration getUserAdministration();

	/**
	 * Veranlasst die Konfiguration, die Konfigurationsdateien zu sichern. Diese Funktion wartet auf das Beenden des Vorgangs. Wird der Auftrag �ber den
	 * Datenverteiler ausgef�hrt (DafDataModel) kann die Konfiguration andere Anfragen parallel ausf�hren. Wird die Funktion lokal ausgef�hrt (ConfigDataModel),
	 * kann es m�glicherweise sinnvoll sein, die Funktion in einem eigenen Thread auszuf�hren.
	 *
	 * @param targetDirectory Relatives Zielverzeichnis innerhalb des in der Konfiguration (mit dem Parameter -sicherungsVerzeichnis) festgelegten
	 *                        Sicherungsordners. Wird null oder ein Leerstring angegeben, generiert die Konfiguration aus aktuellem Datum und Uhrzeit einen neuen
	 *                        Pfadnamen. Falls das {@link de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel} direkt benutzt wird und mit {@link
	 *                        de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel#setBackupBaseDirectory(java.io.File) } noch keine Zielverzeichnis angelegt
	 *                        wurde, kann auch ein absoluter Pfadname angegeben werden. Ein relativer Pfadname w�rde dann relativ zum Arbeitsverzeichnis
	 *                        interpretiert.
	 * @param callback        Objekt, an das Statusmeldungen gesendet werden oder null, falls keine R�ckmeldungen gew�nscht sind
	 *
	 * @return Objekt, das Informationen �ber das Ergebnis des Sicherungsvorgangs enth�lt
	 *
	 * @throws ConfigurationTaskException Der Backup-Vorgang konnte nicht durchgef�hrt werden, beispielsweise weil das Zielverzeichnis falsch war. Falls das
	 *                                    Sichern einzelner Dateien fehlschl�gt wird keine solche Exception geworfen, stattdessen findet man innerhalb vom callback
	 *                                    eventuelle Fehlschl�ge und BackupResult.getFailed ist gr��er 0.
	 * @throws RequestException           Fehler bei der �bertragung der Anfrage oder beim Empfang von Statusmeldungen der Konfiguration. Achtung: Man kann nicht
	 *                                    zwingend darauf schlie�en, dass der Backupvorgang nicht erfolgreich war, wenn eine Exception geworfen wurde. Wenn w�hrend
	 *                                    des Vorgangs beispielsweise die Verbindung zwischen Datenverteiler und Konfiguration abbricht, wird eine Exception
	 *                                    geworfen, aber die Konfiguration den Vorgang vermutlich dennoch korrekt beenden werden.
	 */
	public BackupResult backupConfigurationFiles(String targetDirectory, BackupProgressCallback callback) throws ConfigurationTaskException, RequestException;
}
