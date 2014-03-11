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

package de.bsvrz.dav.daf.main.impl.config.request;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;

import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5961 $ / $Date: 2008-03-26 18:29:53 +0100 (Mi, 26 Mrz 2008) $ / ($Author: rs $)
 */
public class RemoteDynamicObject implements DynamicObject {

	private final DataModel _dataModel;

	private final long _id;

	private final long _typeId;

	private final String _pid;

	private final String _name;

	private final boolean _valid;

	private final long _validSince;

	private final long _notValidSince;

	private final long _configAreaId;

	public RemoteDynamicObject(
			DataModel dataModel, long id, long typeId, String pid, String name, boolean valid, long validSince, long notValidSince, long configAreaId
	) {
		_dataModel = dataModel;
		_id = id;
		_typeId = typeId;
		_pid = pid;
		_name = name;
		_valid = valid;
		_validSince = validSince;
		_notValidSince = notValidSince;
		_configAreaId = configAreaId;
	}

	/**
	 * Liefert den Zeitpunkt ab dem dieses dynamische Objekt g�ltig geworden ist.
	 *
	 * @return Zeit in Millisekunden seit 1970.
	 */
	public long getValidSince() {
		return _validSince;
	}

	/**
	 * Liefert den Zeitpunkt ab dem dieses dynamische Objekt nicht mehr g�ltig ist.
	 *
	 * @return Zeit in Millisekunden seit 1970.
	 */
	public long getNotValidSince() {
		return _notValidSince;
	}

	/**
	 * Methode zum Anmelden auf die Invalidierung des dynamischen Objekts. Sobald das dynamische Objekt auf invalid gesetzt wird, werden alle angemeldeten Listener
	 * informiert.
	 *
	 * @param listener Listener, der informiert wird, sobald das dynamische Objekt auf invalid gesetzt wird.
	 */
	public void addListenerForInvalidation(InvalidationListener listener) {
		throw new UnsupportedOperationException("Noch nicht implementiert.");
	}

	/**
	 * Methode zum Abmelden auf die Invalidierung des dynamischen Objekts.
	 *
	 * @param listener Listener, der nicht mehr informiert werden soll, sobald das dynamische Objekt auf invalid gesetzt wird.
	 */
	public void removeListenerForInvalidation(InvalidationListener listener) {
		throw new UnsupportedOperationException("Noch nicht implementiert.");
	}

	/**
	 * Liefert die Objekt-ID des System-Objekts zur�ck.
	 *
	 * @return ID dieses System-Objekts oder 0, wenn das Objekt keine ID hat.
	 */
	public long getId() {
		return _id;
	}

	/**
	 * Liefert den Typ dieses System-Objekts zur�ck.
	 *
	 * @return Typ dieses System-Objekts.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public SystemObjectType getType() throws ConfigurationException {
		return (SystemObjectType)getDataModel().getObject(_typeId);
	}

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zur�ckgeliefert wird. Au�erdem ist das Objekt ein Element von den direkten und indirekten {@link de.bsvrz.dav.daf.main.config.SystemObjectType#getSuperTypes
	 * Super-Typen} des Objekt-Typs.
	 *
	 * @param type Zu pr�fender Typ.
	 *
	 * @return <code>true</code>, wenn der �bergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorg�nger in der Vererbungshierarchie
	 *         �bereinstimmt; sonst <code>false</code>.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public boolean isOfType(SystemObjectType type) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Bestimmt, ob dieses System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Objekt-Typs, der von der Methode {@link #getType}
	 * zur�ckgeliefert wird. Au�erdem ist das Objekt ein Element von den direkten und indirekten {@link de.bsvrz.dav.daf.main.config.SystemObjectType#getSuperTypes
	 * Super-Typen} des Objekt-Typs.
	 *
	 * @param typePid PID des zu pr�fenden Typs.
	 *
	 * @return <code>true</code>, wenn der �bergebene Typ mit dem Objekt-Typ oder mit einem der direkten oder indirekten Vorg�nger in der Vererbungshierarchie
	 *         �bereinstimmt; sonst <code>false</code>.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public boolean isOfType(String typePid) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert die permanente ID (PID) dieses Objekts zur�ck. Wenn das Objekt keine PID hat, wird ein leerer String zur�ckgegeben.
	 *
	 * @return PID des System-Objekts oder einen leeren String, wenn das Objekt keine PID hat.
	 */
	public String getPid() {
		return (_pid == null ? "" : _pid);
	}

	/**
	 * Setzt die permanente ID (PID) dieses Objekts. Zu beachten ist, da� die PID eines Objektes, wenn sie einmal vergeben wurde, nicht mehr ge�ndert werden kann.
	 *
	 * @param pid Neue PID des Objektes.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn die PID des Objektes nicht ge�ndert werden konnte.
	 */
	public void setPid(String pid) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert den Namen dieses Objekts zur�ck. Wenn das Objekt keinen Namen hat, wird <code>null</code> zur�ckgegeben.
	 *
	 * @return Name des System-Objekts oder <code>null</code>, wenn das Objekt keinen Namen hat.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Setzt den Namen dieses Objekts.
	 *
	 * @param name Neuer Name des Objekts.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn der Name des Objektes nicht ge�ndert werden konnte.
	 * @see de.bsvrz.dav.daf.main.config.SystemObjectType#isNameOfObjectsPermanent
	 */
	public void setName(String name) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert eine Bezeichnung f�r dieses Objekt zur�ck. Wenn dieses Objekt einen nicht leeren Namen hat, wird dieser zur�ckgegeben. Ansonsten wird (falls
	 * vorhanden) die PID des Objekts zur�ckgeben. Wenn die PID auch nicht vorhanden ist, wird die ID des Objekts zur�ckgegeben.
	 *
	 * @return Text, der den Namen die PID oder die ID des System-Objekts enth�lt.
	 */
	public String getNameOrPidOrId() {
		String result = getName();
		if(result == null || result.equals("")) result = getPid();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	/**
	 * Liefert eine Bezeichnung f�r dieses Objekt zur�ck. Wenn dieses Objekt eine nicht leere PID hat, wird diese zur�ckgegeben. Ansonsten wird (falls vorhanden)
	 * der Name des Objekts zur�ckgegeben. Wenn der Name auch nicht vorhanden ist, wird die ID des Objekts zur�ckgegeben.
	 *
	 * @return Text, der die PID, den Namen oder die ID des System-Objekts enth�lt.
	 */
	public String getPidOrNameOrId() {
		String result = getPid();
		if(result == null || result.equals("")) result = getName();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	/**
	 * Liefert eine Bezeichnug f�r dieses Objekt zur�ck. Wenn dieses Objekt eine nicht leere PID hat, wird diese zur�ckgegeben. Ansonsten wird die ID des Objekts
	 * zur�ckgegeben.
	 *
	 * @return Text, der die PID oder die ID des System-Objekts enth�lt.
	 */
	public String getPidOrId() {
		String result = getPid();
		if(result == null || result.equals("")) result = Long.toString(getId());
		return result;
	}

	/**
	 * Bestimmt, ob dieses Objekt zum aktuellen Zeitpunkt g�ltig ist. Konfigurierende System-Objekte sind nach deren {@link
	 * de.bsvrz.dav.daf.main.config.DataModel#createConfigurationObject Erzeugung} noch nicht g�ltig, sondern werden erst mit der Aktivierung der n�chsten
	 * Konfigurationsversion g�ltig. Nach dem {@link #invalidate L�schen} eines Konfigurationsobjekts bleibt es bis zur Aktivierung der n�chsten
	 * Konfigurationsversion g�ltig. Dynamische Objekte werden sofort mit deren {@link de.bsvrz.dav.daf.main.config.ConfigurationArea#createDynamicObject
	 * Erzeugung} g�ltig und mit dem {@link #invalidate L�schen} ung�ltig.
	 *
	 * @return <code>true</code>, falls das Objekt g�ltig ist; <code>false</code>, falls das Objekt nicht g�ltig ist.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public boolean isValid() throws ConfigurationException {
		return _valid;
	}

	/**
	 * L�scht das Objekt, indem es ung�ltig gemacht wird. Dynamische System-Objekte werden sofort ung�ltig. Bereits g�ltige konfigurierende System-Objekte werden
	 * mit Aktivierung der n�chsten Konfigurationsversion ung�ltig. F�r historische Anfragen bleiben ung�ltige Objekte nach wie vor existent. Konfigurierende
	 * System-Objekte, die noch nie g�ltig waren, werden durch diese Methode gel�scht und sind nicht mehr zugreifbar.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn das Objekt nicht ung�ltig gemacht werden konnte.
	 */
	public void invalidate() throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert das zu diesem Objekt geh�rende Datenmodell zur�ck.
	 *
	 * @return Datenmodell des Objekts.
	 */
	public DataModel getDataModel() {
		return _dataModel;
	}

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zur�ck.
	 *
	 * @param atg Attributgruppe des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn bei der Kommunikation mit der Konfiguration Fehler aufgetreten sind.
	 */
	public Data getConfigurationData(AttributeGroup atg) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert einen konfigurierenden Datensatz dieses Objekts zur�ck.
	 *
	 * @param atg Attributgruppe des gew�nschten Datensatzes.
	 * @param asp Aspekt des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	public Data getConfigurationData(AttributeGroup atg, Aspect asp) {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public Data getConfigurationData(AttributeGroupUsage atgUsage) {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * �ndert einen konfigurierenden Datensatz dieses Objekts.
	 *
	 * @param atg  Attributgruppe des zu �ndernden Datensatzes
	 * @param data Neuer Datensatz
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 *          Wenn der nicht ge�ndert werden konnte.
	 */
	public void setConfigurationData(AttributeGroup atg, Data data) throws ConfigurationException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public void setConfigurationData(AttributeGroup atg, Aspect asp, Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		throw new UnsupportedOperationException("Diese Methode 'getUsedAttributeGroupUsage()' wird hier nicht unterst�tzt.");
	}

	/**
	 * Liefert ein Objekt mit beschreibenden Informationen zu diesem Objekt zur�ck.
	 *
	 * @return Objekt mit beschreibenden Informationen.
	 */
	public SystemObjectInfo getInfo() {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Liefert den Konfigurationsbereich, zu dem dieses Objekt geh�rt, zur�ck.
	 *
	 * @return Konfigurationsbereich dieses Objekts.
	 */
	public ConfigurationArea getConfigurationArea() {
		try {
			return (ConfigurationArea)getDataModel().getObject(_configAreaId);
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or
	 * greater than the specified object.<p>
	 * <p/>
	 * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical <i>signum</i> function, which is defined to
	 * return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i> is negative, zero or positive.
	 * <p/>
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>. (This implies that
	 * <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt> throws an exception.)<p>
	 * <p/>
	 * The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.<p>
	 * <p/>
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all
	 * <tt>z</tt>.<p>
	 * <p/>
	 * It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any class that
	 * implements the <tt>Comparable</tt> interface and violates this condition should clearly indicate this fact.  The recommended language is "Note: this class
	 * has a natural ordering that is inconsistent with equals."
	 *
	 * @param o the Object to be compared.
	 *
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 *
	 * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
	 */
	public int compareTo(Object o) {
		SystemObject other = (SystemObject)o;
		if(getId() < other.getId()) return -1;
		if(getId() > other.getId()) return 1;
		return 0;
	}

	public String toString() {
		return getTypeString() + "{" + getParamString() + "}";
	}

	protected String getParamString() {
		String typeName;
		try {
			typeName = getType().getNameOrPidOrId();
		}
		catch(Exception e) {
			typeName = "<error " + e + ">";
		}
		return "name: '" + getName() + "'" + ", pid: '" + getPid() + "'" + ", id: '" + getId() + "'" + ", typ: '" + typeName + "'";
	}

	protected String getTypeString() {
		return getClass().getName();
	}

	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		throw new UnsupportedOperationException("addConfigurationCommunicationChangeListener nicht implementiert");
	}

	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		throw new UnsupportedOperationException("removeConfigurationCommunicationChangeListener nicht implementiert");
	}

	public boolean isConfigurationCommunicationActive() {
		throw new UnsupportedOperationException("isConfigurationCommunicationActive nicht implementiert");
	}
}
