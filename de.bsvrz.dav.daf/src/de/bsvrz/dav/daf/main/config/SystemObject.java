/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines System-Objekts. Datens�tze, die vom Datenverteiler transportiert werden, sind immer genau einem
 * System-Objekt zugeordnet. Zur internen Identifizierung eines System-Objekts wird die <code>id</code> des Objekts benutzt. Das ist ein 64-Bit-Wert der
 * systemweit eindeutig ist. Zur expliziten Referenzierung von Objekten (z.B. in Aufrufparametern von Applikationen, Versorgungsskripten etc.) kann die
 * permanente ID (PID) eines Objekts verwendet werden. Diese ist optional und muss nur bei den Objekten gesetzt werden, bei denen sie gebraucht wird. Die PID
 * ist eine leicht zu merkende Zeichenkette, die systemweit eindeutig ist und sich (wenn sie einmal vergeben wurde) nie mehr �ndern kann. Au�erdem kann jedem
 * System-Objekt ein Name zugeordnet werden, der zu Darstellungszwecken benutzt werden kann. Der Name eines Objekts kann i.a. jederzeit ge�ndert werden und
 * sollte aus diesem Grund nicht zur Referenzierung von Objekten eingesetzt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public interface SystemObject extends Comparable {

	/**
	 * Liefert die Objekt-ID des System-Objekts zur�ck.
	 *
	 * @return ID dieses System-Objekts oder 0, wenn das Objekt keine ID hat.
	 */
	public long getId();

	/**
	 * Liefert den Typ dieses System-Objekts zur�ck.
	 *
	 * @return Typ dieses System-Objekts.
	 */
	public SystemObjectType getType();

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zur�ckgeliefert wird. Au�erdem ist das Objekt ein Element von den direkten und indirekten {@link SystemObjectType#getSuperTypes Super-Typen} des
	 * Objekt-Typs.
	 *
	 * @param type Zu pr�fender Typ.
	 *
	 * @return <code>true</code>, wenn der �bergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorg�nger in der Vererbungshierarchie
	 *         �bereinstimmt; sonst <code>false</code>.
	 */
	public boolean isOfType(SystemObjectType type);

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zur�ckgeliefert wird. Au�erdem ist das Objekt ein Element von den direkten und indirekten {@link SystemObjectType#getSuperTypes Super-Typen} des
	 * Objekt-Typs.
	 *
	 * @param typePid PID des zu pr�fenden Typs.
	 *
	 * @return <code>true</code>, wenn der �bergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorg�nger in der Vererbungshierarchie
	 *         �bereinstimmt; sonst <code>false</code>.
	 */
	public boolean isOfType(String typePid);

	/**
	 * Liefert die permanente ID (PID) dieses Objekts zur�ck. Wenn das Objekt keine PID hat, wird ein leerer String zur�ckgegeben.
	 *
	 * @return PID des System-Objekts oder einen leeren String, wenn das Objekt keine PID hat.
	 */
	public String getPid();

	/**
	 * Liefert den Namen dieses Objekts zur�ck. Wenn das Objekt keinen Namen hat, wird ein leerer String zur�ckgegeben.
	 *
	 * @return Name des System-Objekts oder ein leerer String, wenn das Objekt keinen Namen hat.
	 */
	public String getName();

	/**
	 * Setzt den Namen dieses Objekts.
	 *
	 * @param name Neuer Name des Objekts. Der leere String ("") oder <code>null</code> wird als "kein Name" interpretiert.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Wenn der Name des Objektes nicht ge�ndert werden konnte.
	 * @see SystemObjectType#isNameOfObjectsPermanent
	 */
	public void setName(String name) throws ConfigurationChangeException;

	/**
	 * Liefert eine Bezeichnung f�r dieses Objekt zur�ck. Wenn dieses Objekt einen nicht leeren Namen hat, wird dieser zur�ckgegeben. Ansonsten wird (falls
	 * vorhanden) die PID des Objekts zur�ckgeben. Wenn die PID auch nicht vorhanden ist, wird die ID des Objekts zur�ckgegeben.
	 *
	 * @return Text, der den Namen, die PID oder die ID des System-Objekts enth�lt.
	 */
	public String getNameOrPidOrId();

	/**
	 * Liefert eine Bezeichnung f�r dieses Objekt zur�ck. Wenn dieses Objekt eine nicht leere PID hat, wird diese zur�ckgegeben. Ansonsten wird (falls vorhanden)
	 * der Name des Objekts zur�ckgegeben. Wenn der Name auch nicht vorhanden ist, wird die ID des Objekts zur�ckgegeben.
	 *
	 * @return Text, der die PID, den Namen oder die ID des System-Objekts enth�lt.
	 */
	public String getPidOrNameOrId();

	/**
	 * Liefert eine Bezeichnug f�r dieses Objekt zur�ck. Wenn dieses Objekt eine nicht leere PID hat, wird diese zur�ckgegeben. Ansonsten wird die ID des Objekts
	 * zur�ckgegeben.
	 *
	 * @return Text, der die PID oder die ID des System-Objekts enth�lt.
	 */
	public String getPidOrId();

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zur�ck. Das genaue Format ist nicht festgelegt und kann sich �ndern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString();

	/**
	 * Bestimmt, ob dieses Objekt zum aktuellen Zeitpunkt g�ltig ist. Konfigurierende System-Objekte sind nach deren {@link
	 * ConfigurationArea#createConfigurationObject Erzeugung} noch nicht g�ltig, sondern werden erst mit der Aktivierung der n�chsten Konfigurationsversion g�ltig.
	 * Nach dem {@link #invalidate L�schen} eines Konfigurationsobjekts bleibt es bis zur Aktivierung der n�chsten Konfigurationsversion g�ltig. Dynamische Objekte
	 * werden sofort mit deren {@link ConfigurationArea#createDynamicObject Erzeugung} g�ltig und mit dem {@link #invalidate L�schen} ung�ltig.
	 *
	 * @return <code>true</code>, falls das Objekt g�ltig ist;<br/> <code>false</code>, falls das Objekt nicht g�ltig ist.
	 */
	public boolean isValid();

	/**
	 * L�scht das Objekt, indem es ung�ltig gemacht wird. Dynamische System-Objekte werden sofort ung�ltig. Bereits g�ltige konfigurierende System-Objekte werden
	 * mit Aktivierung der n�chsten Konfigurationsversion ung�ltig. F�r historische Anfragen bleiben ung�ltige Objekte nach wie vor existent. Konfigurierende
	 * System-Objekte, die noch nie g�ltig waren, werden durch diese Methode gel�scht und sind nicht mehr zugreifbar.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht ung�ltig gemacht werden konnte.
	 */
	public void invalidate() throws ConfigurationChangeException;

	/**
	 * Liefert das zu diesem Objekt geh�rende Objekt zum Zugriff auf die Konfiguration zur�ck.
	 *
	 * @return Objekt zum Zugriff auf die Konfiguration.
	 */
	public DataModel getDataModel();

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zur�ck. Als Aspekt des gew�nschten Datensatzes wird "<code>asp.eigenschaften</code>" angenommen.
	 *
	 * @param atg Attributgruppe des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 *
	 * @see #getConfigurationData(AttributeGroup,Aspect)
	 */
	public Data getConfigurationData(AttributeGroup atg);

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zur�ck.
	 *
	 * @param atg Attributgruppe des gew�nschten Datensatzes.
	 * @param asp Aspekt des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	public Data getConfigurationData(AttributeGroup atg, Aspect asp);

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zur�ck.
	 *
	 * @param atgUsage Attributgruppenverwendung des gew�nschten Datensatzes
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppenverwendung oder <code>null</code>, wenn das Objekt keinen Datensatz zu der angegebenen
	 *         Attributgruppenverwendung hat.
	 */
	public Data getConfigurationData(AttributeGroupUsage atgUsage);

	/**
	 * �ndert einen konfigurierenden Datensatz dieses Objekts. Als Aspekt wird "<code>asp.eigenschaften</code>" verwendet.
	 *
	 * @param atg  Attributgruppe des zu �ndernden Datensatzes
	 * @param data Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gel�scht.
	 *
	 * @throws ConfigurationChangeException Wenn der Datensatz nicht ge�ndert werden konnte.
	 */
	public void setConfigurationData(AttributeGroup atg, Data data) throws ConfigurationChangeException;

	/**
	 * �ndert einen konfigurierenden Datensatz dieses Objekts.
	 *
	 * @param atg  Attributgruppe des zu �ndernden Datensatzes
	 * @param asp  Aspekt des zu �ndernden Datensatzes
	 * @param data Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gel�scht.
	 *
	 * @throws ConfigurationChangeException Wenn der Datensatz nicht ge�ndert werden konnte.
	 */
	public void setConfigurationData(AttributeGroup atg, Aspect asp, Data data) throws ConfigurationChangeException;

	/**
	 * �ndert einen konfigurierenden Datensatz dieses Objekts.
	 *
	 * @param atgUsage Attributgruppenverwendung des zu �ndernden Datensatzes
	 * @param data     Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gel�scht.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Wenn der Datensatz nicht ge�ndert werden konnte.
	 */
	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException;

	/**
	 * Liefert alle Attributgruppenverwendungen zur�ck, f�r die es einen konfigurierenden Datensatz an diesem Objekt gibt.
	 *
	 * @return Alle Attributgruppenverwendungen, f�r die es einen konfigurierenden Datensatz an diesem Objekt gibt.
	 *
	 * @throws UnsupportedOperationException Wenn diese Methode nicht von diesem System-Object unterst�tzt wird.
	 */
	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages();

	/**
	 * Liefert ein Objekt mit beschreibenden Informationen zu diesem Objekt zur�ck.
	 *
	 * @return Objekt mit beschreibenden Informationen.
	 */
	public SystemObjectInfo getInfo();

	/**
	 * Liefert den Konfigurationsbereich, zu dem dieses Objekt geh�rt, zur�ck.
	 *
	 * @return Konfigurationsbereich dieses Objekts.
	 */
	public ConfigurationArea getConfigurationArea();
}

