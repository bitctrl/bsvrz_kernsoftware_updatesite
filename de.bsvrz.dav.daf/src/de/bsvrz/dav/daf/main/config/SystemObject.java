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

import de.bsvrz.dav.daf.main.Data;

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines System-Objekts. Datensätze, die vom Datenverteiler transportiert werden, sind immer genau einem
 * System-Objekt zugeordnet. Zur internen Identifizierung eines System-Objekts wird die <code>id</code> des Objekts benutzt. Das ist ein 64-Bit-Wert der
 * systemweit eindeutig ist. Zur expliziten Referenzierung von Objekten (z.B. in Aufrufparametern von Applikationen, Versorgungsskripten etc.) kann die
 * permanente ID (PID) eines Objekts verwendet werden. Diese ist optional und muss nur bei den Objekten gesetzt werden, bei denen sie gebraucht wird. Die PID
 * ist eine leicht zu merkende Zeichenkette, die systemweit eindeutig ist und sich (wenn sie einmal vergeben wurde) nie mehr ändern kann. Außerdem kann jedem
 * System-Objekt ein Name zugeordnet werden, der zu Darstellungszwecken benutzt werden kann. Der Name eines Objekts kann i.a. jederzeit geändert werden und
 * sollte aus diesem Grund nicht zur Referenzierung von Objekten eingesetzt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface SystemObject extends Comparable {

	/**
	 * Liefert die Objekt-ID des System-Objekts zurück.
	 *
	 * @return ID dieses System-Objekts oder 0, wenn das Objekt keine ID hat.
	 */
	public long getId();

	/**
	 * Liefert den Typ dieses System-Objekts zurück.
	 *
	 * @return Typ dieses System-Objekts.
	 */
	public SystemObjectType getType();

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zurückgeliefert wird. Außerdem ist das Objekt ein Element von den direkten und indirekten {@link SystemObjectType#getSuperTypes Super-Typen} des
	 * Objekt-Typs.
	 *
	 * @param type Zu prüfender Typ.
	 *
	 * @return <code>true</code>, wenn der übergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorgänger in der Vererbungshierarchie
	 *         übereinstimmt; sonst <code>false</code>.
	 */
	public boolean isOfType(SystemObjectType type);

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zurückgeliefert wird. Außerdem ist das Objekt ein Element von den direkten und indirekten {@link SystemObjectType#getSuperTypes Super-Typen} des
	 * Objekt-Typs.
	 *
	 * @param typePid PID des zu prüfenden Typs.
	 *
	 * @return <code>true</code>, wenn der übergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorgänger in der Vererbungshierarchie
	 *         übereinstimmt; sonst <code>false</code>.
	 */
	public boolean isOfType(String typePid);

	/**
	 * Liefert die permanente ID (PID) dieses Objekts zurück. Wenn das Objekt keine PID hat, wird ein leerer String zurückgegeben.
	 *
	 * @return PID des System-Objekts oder einen leeren String, wenn das Objekt keine PID hat.
	 */
	public String getPid();

	/**
	 * Liefert den Namen dieses Objekts zurück. Wenn das Objekt keinen Namen hat, wird ein leerer String zurückgegeben.
	 *
	 * @return Name des System-Objekts oder ein leerer String, wenn das Objekt keinen Namen hat.
	 */
	public String getName();

	/**
	 * Setzt den Namen dieses Objekts.
	 *
	 * @param name Neuer Name des Objekts. Der leere String ("") oder <code>null</code> wird als "kein Name" interpretiert.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Wenn der Name des Objektes nicht geändert werden konnte.
	 * @see SystemObjectType#isNameOfObjectsPermanent
	 */
	public void setName(String name) throws ConfigurationChangeException;

	/**
	 * Liefert eine Bezeichnung für dieses Objekt zurück. Wenn dieses Objekt einen nicht leeren Namen hat, wird dieser zurückgegeben. Ansonsten wird (falls
	 * vorhanden) die PID des Objekts zurückgeben. Wenn die PID auch nicht vorhanden ist, wird die ID des Objekts zurückgegeben.
	 *
	 * @return Text, der den Namen, die PID oder die ID des System-Objekts enthält.
	 */
	public String getNameOrPidOrId();

	/**
	 * Liefert eine Bezeichnung für dieses Objekt zurück. Wenn dieses Objekt eine nicht leere PID hat, wird diese zurückgegeben. Ansonsten wird (falls vorhanden)
	 * der Name des Objekts zurückgegeben. Wenn der Name auch nicht vorhanden ist, wird die ID des Objekts zurückgegeben.
	 *
	 * @return Text, der die PID, den Namen oder die ID des System-Objekts enthält.
	 */
	public String getPidOrNameOrId();

	/**
	 * Liefert eine Bezeichnug für dieses Objekt zurück. Wenn dieses Objekt eine nicht leere PID hat, wird diese zurückgegeben. Ansonsten wird die ID des Objekts
	 * zurückgegeben.
	 *
	 * @return Text, der die PID oder die ID des System-Objekts enthält.
	 */
	public String getPidOrId();

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück. Das genaue Format ist nicht festgelegt und kann sich ändern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString();

	/**
	 * Bestimmt, ob dieses Objekt zum aktuellen Zeitpunkt gültig ist. Konfigurierende System-Objekte sind nach deren {@link
	 * ConfigurationArea#createConfigurationObject Erzeugung} noch nicht gültig, sondern werden erst mit der Aktivierung der nächsten Konfigurationsversion gültig.
	 * Nach dem {@link #invalidate Löschen} eines Konfigurationsobjekts bleibt es bis zur Aktivierung der nächsten Konfigurationsversion gültig. Dynamische Objekte
	 * werden sofort mit deren {@link ConfigurationArea#createDynamicObject Erzeugung} gültig und mit dem {@link #invalidate Löschen} ungültig.
	 *
	 * @return <code>true</code>, falls das Objekt gültig ist;<br/> <code>false</code>, falls das Objekt nicht gültig ist.
	 */
	public boolean isValid();

	/**
	 * Löscht das Objekt, indem es ungültig gemacht wird. Dynamische System-Objekte werden sofort ungültig. Bereits gültige konfigurierende System-Objekte werden
	 * mit Aktivierung der nächsten Konfigurationsversion ungültig. Für historische Anfragen bleiben ungültige Objekte nach wie vor existent. Konfigurierende
	 * System-Objekte, die noch nie gültig waren, werden durch diese Methode gelöscht und sind nicht mehr zugreifbar.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht ungültig gemacht werden konnte.
	 */
	public void invalidate() throws ConfigurationChangeException;

	/**
	 * Liefert das zu diesem Objekt gehörende Objekt zum Zugriff auf die Konfiguration zurück.
	 *
	 * @return Objekt zum Zugriff auf die Konfiguration.
	 */
	public DataModel getDataModel();

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück. Als Aspekt des gewünschten Datensatzes wird "<code>asp.eigenschaften</code>" angenommen.
	 *
	 * @param atg Attributgruppe des gewünschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 *
	 * @see #getConfigurationData(AttributeGroup,Aspect)
	 */
	public Data getConfigurationData(AttributeGroup atg);

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück.
	 *
	 * @param atg Attributgruppe des gewünschten Datensatzes.
	 * @param asp Aspekt des gewünschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	public Data getConfigurationData(AttributeGroup atg, Aspect asp);

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zurück.
	 *
	 * @param atgUsage Attributgruppenverwendung des gewünschten Datensatzes
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppenverwendung oder <code>null</code>, wenn das Objekt keinen Datensatz zu der angegebenen
	 *         Attributgruppenverwendung hat.
	 */
	public Data getConfigurationData(AttributeGroupUsage atgUsage);

	/**
	 * Ändert einen konfigurierenden Datensatz dieses Objekts. Als Aspekt wird "<code>asp.eigenschaften</code>" verwendet.
	 *
	 * @param atg  Attributgruppe des zu ändernden Datensatzes
	 * @param data Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gelöscht.
	 *
	 * @throws ConfigurationChangeException Wenn der Datensatz nicht geändert werden konnte.
	 */
	public void setConfigurationData(AttributeGroup atg, Data data) throws ConfigurationChangeException;

	/**
	 * Ändert einen konfigurierenden Datensatz dieses Objekts.
	 *
	 * @param atg  Attributgruppe des zu ändernden Datensatzes
	 * @param asp  Aspekt des zu ändernden Datensatzes
	 * @param data Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gelöscht.
	 *
	 * @throws ConfigurationChangeException Wenn der Datensatz nicht geändert werden konnte.
	 */
	public void setConfigurationData(AttributeGroup atg, Aspect asp, Data data) throws ConfigurationChangeException;

	/**
	 * Ändert einen konfigurierenden Datensatz dieses Objekts.
	 *
	 * @param atgUsage Attributgruppenverwendung des zu ändernden Datensatzes
	 * @param data     Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gelöscht.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Wenn der Datensatz nicht geändert werden konnte.
	 */
	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException;

	/**
	 * Liefert alle Attributgruppenverwendungen zurück, für die es einen konfigurierenden Datensatz an diesem Objekt gibt.
	 *
	 * @return Alle Attributgruppenverwendungen, für die es einen konfigurierenden Datensatz an diesem Objekt gibt.
	 *
	 * @throws UnsupportedOperationException Wenn diese Methode nicht von diesem System-Object unterstützt wird.
	 */
	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages();

	/**
	 * Liefert ein Objekt mit beschreibenden Informationen zu diesem Objekt zurück.
	 *
	 * @return Objekt mit beschreibenden Informationen.
	 */
	public SystemObjectInfo getInfo();

	/**
	 * Liefert den Konfigurationsbereich, zu dem dieses Objekt gehört, zurück.
	 *
	 * @return Konfigurationsbereich dieses Objekts.
	 */
	public ConfigurationArea getConfigurationArea();
}

