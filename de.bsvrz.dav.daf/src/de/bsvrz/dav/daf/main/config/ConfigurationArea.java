/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;

import java.util.Collection;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsbereichs. Jedes Konfigurationsobjekt ist genau einem Konfigurationsbereich zugeordnet.
 * Jedem Konfigurationsbereich ist ein Konfigurationsverantwortlicher zugeordnet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConfigurationArea extends ConfigurationObject {

	/**
	 * Liefert den Konfigurationsverantwortlichen dieses Konfigurationsbereichs. Die Konfigurationapplikation, die über Aufrufparameter dem hier zurückgelieferten
	 * Konfigurationsverantwortlichen zugeordnet ist, ist als einzige für Änderungen im Bereich zuständig und verantwortlich.
	 *
	 * @return Verantwortliche Instanz für den Konfigurationsbereich.
	 */
	public ConfigurationAuthority getConfigurationAuthority();

	/**
	 * Version eines Bereichs, die zur Übernahme und Aktivierung in anderen Konfigurationen freigegeben ist.
	 *
	 * @return Nummer der Version, die zur Übernahme und Aktivierung freigegeben ist.
	 *
	 * @see "TPuK1-103"
	 */
	public short getActivatableVersion();

	/**
	 * Version eines Bereichs, die zur Übernahme in anderen Konfigurationen freigegeben ist.
	 *
	 * @return Nummer der Version, die zur Übernahme freigegeben ist.
	 *
	 * @see "TPuK1-103"
	 */
	public short getTransferableVersion();

	/**
	 * Neue Version eines Bereichs, die weder zur Übernahme freigegeben noch lokal aktiviert ist. Dies ist die in Bearbeitung befindliche Version, auf die sich
	 * versionierte Konfigurationsänderungen beziehen.
	 *
	 * @return Nummer der Version, die sich in Bearbeitung befindet.
	 *
	 * @see "TPuK1-103"
	 */
	public short getModifiableVersion();

	/**
	 * Version eines Bereichs, die lokal aktiv ist.
	 *
	 * @return Nummer der Version, die lokal aktiv ist.
	 *
	 * @see "TPuK1-100"
	 */
	public short getActiveVersion();

	/**
	 * Liefert die Zeit vom letzten Erzeugen oder Löschen eines dynamischen Objekts in diesem Konfigurationsbereich.
	 *
	 * @return Zeit in Millisekunden seit 1970
	 */
	public long getTimeOfLastDynamicChange();

	/**
	 * Liefert die Zeit vom letzten Erzeugen oder Löschen eines Konfigurationsobjekts (in noch nicht aktivierten Versionen) dieses Konfigurationsbereichs.
	 *
	 * @return Zeit in Millisekunden seit 1970
	 */
	public long getTimeOfLastNonActiveConfigurationChange();

	/**
	 * Liefert die Zeit der letzten (nicht versionierten) Änderung eines änderbaren konfigurierenden Datensatzes.
	 *
	 * @return Zeit in Millisekunden seit 1970
	 */
	public long getTimeOfLastActiveConfigurationChange();

	/**
	 * Gibt alle System-Objekte zurück, die zu den angegebenen Objekttypen und deren erweiterten Typen und zur Zeitangabe passen. Wird beim Parameter für die
	 * Objekttypen <code>null</code> übergeben, so gilt dies als Wildcard. D. h. alle Objekttypen werden berücksichtigt.
	 *
	 * @param systemObjectTypes die Objekttypen oder <code>null</code>, falls alle betrachtet werden sollen
	 * @param timeSpecification gibt den Gültigkeitszeitraum für die gesuchten Objekte an
	 *
	 * @return Die System-Objekte, für die die Bedingungen (siehe Parameter) zutreffen.
	 */
	public Collection<SystemObject> getObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification);

	/**
	 * Gibt alle System-Objekte zurück, die zu den angegebenen Objekttypen und zur Zeitangabe passen. Es werden im Gegensatz zur {@link #getObjects}-Methode keine
	 * Typen betrachtet, die die angegebenen Typen erweitern.
	 *
	 * @param systemObjectTypes die zu betrachtenden Objekttypen
	 * @param timeSpecification gibt den Gültigkeitszeitraum für die gesuchten Objekte an
	 *
	 * @return Die System-Objekte, für die die Bedingungen (siehe Parameter) zutreffen.
	 */
	public Collection<SystemObject> getDirectObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification);

	/**
	 * Gibt alle System-Objekte dieses Konfigurationsbereichs zurück, die in der aktuellen Version des Bereichs gültig sind.
	 *
	 * @return alle gültigen Objekte dieses Konfigurationsbereichs
	 */
	public Collection<SystemObject> getCurrentObjects();

	/**
	 * Gibt alle System-Objekte dieses Konfigurationsbereichs zurück, die in einer zukünftigen Version gültig werden.
	 *
	 * @return alle zukünftig aktuellen Objekte dieses Konfigurationsbereichs
	 */
	public Collection<SystemObject> getNewObjects();

	/**
	 * Erzeugt ein neues Konfigurationsobjekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden. Die
	 * verantwortliche Instanz des neuen Objektes kann nicht spezifiziert werden, da sie von der jeweiligen Konfiguration vergeben wird. Das neue Objekt wird erst
	 * mit Aktivierung der nächsten Konfigurationsversion gültig.
	 *
	 * @param type Typ des neuen Objekts.
	 * @param pid  PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name Name des neuen Objekts (kann später verändert werden). Der leere String ("") oder <code>null</code> wird als "kein Name" interpretiert.
	 * @param sets Liste der Mengen des neuen Objekts oder <code>null</code>, wenn keine Mengen vergeben werden sollen.
	 *
	 * @return Stellvertreterobjekt für das neu angelegte Konfigurationsobjekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see ConfigurationObject
	 * @see SystemObject#isValid
	 */
	public ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, Collection<? extends ObjectSet> sets)
			throws ConfigurationChangeException;

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden.
	 * Das neue Objekt wird sofort gültig.
	 *
	 * @param type Typ des neuen Objekts
	 * @param pid  PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name Name des neuen Objekts (kann später verändert werden). Der leere String ("") oder <code>null</code> wird als "kein Name" interpretiert.
	 *
	 * @return Stellvertreterobjekt für das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see SystemObject
	 * @see SystemObject#isValid
	 */
	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name) throws ConfigurationChangeException;

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden.
	 * Diese Methode stellt sicher, dass zumindest alle für dieses Objekt notwendigen konfigurierenden Datensätze beim Erzeugen vorhanden sind.
	 *
	 * @param type Typ des neuen Objekts
	 * @param pid  PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name Name des neuen Objekts (kann später verändert werden). Der leere String ("") oder <code>null</code> wird als "kein Name" interpretiert.
	 * @param data Enthält alle konfigurierenden Datensätze mit den dazugehörigen Attributgruppenverwendungen, die am neuen Objekt gespeichert werden sollen. Wird
	 *             eine leere Liste oder <code>null</code> übergeben, so werden keine Datensätze am neu erzeugten Objekt angelegt.
	 *
	 * @return Stellvertreterobjekt für das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 * @see SystemObject
	 * @see SystemObject#isValid
	 */
	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name, Collection<DataAndATGUsageInformation> data)
			throws ConfigurationChangeException;



}

