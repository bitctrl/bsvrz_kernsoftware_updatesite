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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementierung der Schnittstelle zum Zugriff auf die Eigenschaften eines System-Objekts. Datensätze, die vom Datenverteiler transportiert werden, sind immer
 * genau einem System-Objekt zugeordnet. Zur internen Identifizierung eines System-Objekts wird die <code>id</code> des Objekts benutzt. Das ist ein 64-Bit-Wert
 * der systemweit eindeutig ist. Zur expliziten Referenzierung von Objekten (z.B. in Aufrufparametern von Applikationen, Versorgungsskripten etc.) kann die
 * permanente ID (PID) eines Objekts verwendet werden. Diese ist optional und muss nur bei den Objekten gesetzt werden, bei denen sie gebraucht wird. Die PID
 * ist eine leicht zu merkende Zeichenkette, die systemweit eindeutig ist und sich (wenn sie einmal vergeben wurde) nie mehr ändern kann. Außerdem kann jedem
 * System-Objekt ein Name zugeordnet werden, der zu Darstellungszwecken benutzt werden kann. Der Name eines Objekts kann i.a. jederzeit geändert werden und
 * sollte aus diesem Grund nicht zur Referenzierung von Objekten eingesetzt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class DafSystemObject implements SystemObject {

	/** Typfeld für die Serialisierung von <code>null</code>-Referenzen */
	public static final byte NULL_OBJECT = 0;

	/** Typfeld für die Serialisierung von Konfigurationsobjekten */
	public static final byte CONFIGURATION_OBJECT = 1;

	/** Typfeld für die Serialisierung von dynamischen Objekten */
	public static final byte DYNAMIC_OBJECT = 2;

	/** Typfeld für die Serialisierung von Typen von Systemobjekten */
	public static final byte SYSTEM_OBJECT_TYPE = 3;

	/** Typfeld für die Serialisierung von Typen von dynamischen Objekten */
	public static final byte DYNAMIC_OBJECT_TYPE = SYSTEM_OBJECT_TYPE;

	/** Typfeld für die Serialisierung von Typen von Konfigurationsobjekten */
	public static final byte CONFIGURATION_OBJECT_TYPE = 4;

	/** Typfeld für die Serialisierung von Mengentypen */
	public static final byte OBJECT_SET_TYPE = 5;

	/** Typfeld für die Serialisierung von Attributgruppen */
	public static final byte ATTRIBUTE_GROUP = 6;

	/** Typfeld für die Serialisierung von Aspekten */
	public static final byte ASPECT = 7;

	/** Typfeld für die Serialisierung von Mengenverwendungen */
	public static final byte OBJECT_SET_USE = 8;

	/** Typfeld für die Serialisierung von dynamischen Mengen */
	public static final byte MUTABLE_SET = 9;

	/** Typfeld für die Serialisierung von konfigurierenden Mengen */
	public static final byte NON_MUTABLE_SET = 10;

	/** Typfeld für die Serialisierung von Attributen */
	public static final byte ATTRIBUTE = 11;

	/** Typfeld für die Serialisierung von Ganzzahlattributtypen */
	public static final byte INTEGER_ATTRIBUTE_TYPE = 13;

	/** Typfeld für die Serialisierung von Kommazahlattributtypen */
	public static final byte FLOATING_POINT_NUMBER_ATTRIBUTE_TYPE = 14;

	/** Typfeld für die Serialisierung von Referenzattributtypen */
	public static final byte REFERENCE_ATTRIBUTE_TYPE = 15;

	/** Typfeld für die Serialisierung von Zeichenkettenattributtypen */
	public static final byte STRING_ATTRIBUTE_TYPE = 16;

	/** Typfeld für die Serialisierung von Zeitstempelattributtypen */
	public static final byte TIME_ATTRIBUTE_TYPE = 17;

	/** Typfeld für die Serialisierung von Attributlistendefinitionen */
	public static final byte ATTRIBUTE_LIST_TYPE = 18;

	/** Typfeld für die Serialisierung von Konfigurationsverantwortliche */
	public static final byte CONFIGURATION_AUTHORITY = 19;

	/** Typfeld für die Serialisierung von Datenverteilerobjekte */
	public static final byte DAV_APPLICATION = 20;

	/** Typfeld für die Serialisierung von Konfigurationsapplikationen */
	public static final byte CONFIGURATION_APPLICATION = 21;

	/** Typfeld für die Serialisierung von Applikationen */
	public static final byte CLIENT_APPLICATION = 22;

	/** Typfeld für die Serialisierung von Aufzählungswerten von Ganzzahlattributtypen */
	public static final byte INTEGER_VALUE_STATE = 23;

	/** Typfeld für die Serialisierung von Wertebereichen von Ganzzahlattributtypen */
	public static final byte INTEGER_VALUE_RANGE = 24;

	/** Typfeld für die Serialisierung von Konfigurationsbereichen */
	public static final byte CONFIGURATION_AREA = 25;

	/** Typfeld für die Serialisierung von Attributgruppenverwendungen */
	public static final byte ATTRIBUTE_GROUP_USAGE = 26;

	/** Statusfeld für ungültig gewordene (dynamische) Objekte */
	public static final byte OBJECT_DELETED = 0;

	/** Statusfeld für gültige Objekte */
	public static final byte OBJECT_EXISTS = 1;

	/** Statusfeld für nicht gültige (Konfigurations-) Objekte */
	public static final byte OBJECT_INVALID = 2;

	/** Die ID dieses Objekts */
	private long _id;

	/** Die PID dieses Objekts */
	private String _pid;

	/** Der Name dieses Objekts */
	private String _name;

	/** Die ID des Objekttyps dieses Objekts */
	private long _typeId;

	/** Der Objekttyp dieses Objekts */
	private DafSystemObjectType _systemObjectType;

	/**
	 * Statusfeld dieses Objektes.
	 *
	 * @see #OBJECT_DELETED
	 * @see #OBJECT_EXISTS
	 */
	private byte _state;

	/** Fehlerbeschreibung wenn vorhanden, sonst null. */
	private String _error;

	/** Typfeld dieses Objekts. Dieses Feld dient zur Unterscheidung der verschiedenartigen Systemobjekte bei der Serialisierung */
	protected byte _internType;

	/** Objekt zum Zugriff auf die Konfiguration */
	protected DafDataModel _dataModel;

	/** Id des Konfigurationsbereichs zu dem dieses Objekt gehört. */
	protected long _configurationAreaId;

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafSystemObject(DafDataModel dataModel) {
		_dataModel = dataModel;
	}

	/**
	 * Konstruktor, zur Erzeugung eines neuen Stellvertreterobjekts für ein Systemobjekt.
	 *
	 * @param id                  Id des Systemobjekts
	 * @param pid                 Pid des Systemobjekts
	 * @param name                Name des Systemobjekts
	 * @param typeId              Id des Typs des SystemObjects
	 * @param state               Status des SystemObjects
	 * @param error               Fehler des Systemobjekts
	 * @param dataModel           Objekt zum Zugriff auf die Konfiguration
	 * @param configurationAreaId Id des zugeordneten Konfigurationsbereichs
	 */
	public DafSystemObject(
			long id, String pid, String name, long typeId, byte state, String error, DafDataModel dataModel, long configurationAreaId
	) {
		_id = id;
		_pid = pid;
		_name = name;
		_typeId = typeId;
		_state = state;
		_error = error;
		_dataModel = dataModel;
		_configurationAreaId = configurationAreaId;
	}

	public ConfigurationArea getConfigurationArea() {
		return (DafConfigurationArea)_dataModel.getObject(_configurationAreaId);
	}

	/**
	 * Bestimmt die Objekt-Id des Konfigurationsbereichs des Objekts
	 *
	 * @return Objekt-Id des Konfigurationsbereichs des Objekts
	 */
	public long getConfigurationAreaId() {
		return _configurationAreaId;
	}

	/**
	 * Vergleicht das Objekt mit einem anderen Objekt. Zwei Objekte sind gleich, wenn sie die gleiche Objekt-Id haben.
	 *
	 * @return <code>true</code>, wenn die Objekte gleich sind, sonst <code>false</code>.
	 */
	public final boolean equals(Object other) {
		if(!(other instanceof DafSystemObject)) {
			return false;
		}
		return ((DafSystemObject)other)._id == _id;
	}

	/**
	 * Bestimmt den Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts.
	 */
	public final int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke zurück.
	 *
	 * @return Beschreibender Text dieses Objekts.
	 */
	public String parseToString() {
		String str = "ID: " + _id + "\n";
		str += "PID: " + _pid + "\n";
		str += "Name: " + _name + "\n";
		str += "Typ Id: " + _typeId + "\n";
		return str;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück. Es wird der Name des Objekts gefolgt von der Pid in runden Klammern oder der Id in eckigen
	 * Klammern, falls keine Pid vergeben ist.
	 */
	public String toString() {
		StringBuilder text = new StringBuilder();
		if(_name != null && !_name.equals("")) {
			text.append(_name).append(" ");
		}
		if(_pid != null && !_pid.equals("")) {
			text.append("(").append(_pid).append(")");
		}
		else {
			text.append("[").append(_id).append("]");
		}
		return text.toString();
	}

	/**
	 * Serialisiert dieses Objekt.
	 *
	 * @param out Stream auf den das Objekt geschrieben werden soll.
	 *
	 * @throws IOException, wenn beim Schreiben auf den Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public void write(DataOutputStream out) throws IOException {
		out.writeLong(_id);
		out.writeLong(_typeId);
		out.writeByte(_state);
		boolean b = (_pid != null);
		out.writeBoolean(b);
		if(b) {
			out.writeUTF(_pid);
		}
		b = (_name != null);
		out.writeBoolean(b);
		if(b) {
			out.writeUTF(_name);
		}
		b = (_error != null);
		out.writeBoolean(b);
		if(b) {
			out.writeUTF(_error);
		}
		out.writeLong(_configurationAreaId);
	}

	/**
	 * Deserialisiert dieses Objekt über die alte Methode.
	 *
	 * @param in Stream von dem das Objekt gelesen werden soll.
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public void read(DataInputStream in) throws IOException {
		_id = in.readLong();
		_typeId = in.readLong();
		_state = in.readByte();
		boolean b = in.readBoolean();
		if(b) {
			_pid = in.readUTF();
		}
		b = in.readBoolean();
		if(b) {
			_name = in.readUTF();
		}
		b = in.readBoolean();
		if(b) {
			_error = in.readUTF();
		}
		_configurationAreaId = in.readLong();
	}

	/**
	 * Deserialisiert dieses Objekt
	 * @param deserializer Deserialisierer als Datenquelle
	 */
	public void read(final Deserializer deserializer) throws IOException {
		_id = deserializer.readLong();
		_typeId = deserializer.readLong();
		byte flag = deserializer.readByte();
		if((flag & 1) != 0){
			// Objekt ist gültig
			_state = OBJECT_EXISTS;
		}
		else if(this instanceof DynamicObjectType){
			_state = OBJECT_DELETED;
		}
		else {
			_state = OBJECT_INVALID;
		}
		if((flag & 2) != 0){
			_pid = deserializer.readString();
		}
		if((flag & 4) != 0){
			_name = deserializer.readString();
		}
	}

	/**
	 * Bestimmt den Status dieses Objektes
	 *
	 * @return Liefert den Wert <code>OBJECT_INVALID</code>, <code>OBJECT_DELETED</code> für ungültige
	 * oder <code>OBJECT_EXISTS</code> für gültige Objekte zurück.
	 *
	 * @see #OBJECT_EXISTS Objekt ist gültig.
	 * @see #OBJECT_INVALID Ungültiges Konfigurationsobjekt
	 * @see #OBJECT_DELETED Dynamisches Objekt wurde auf ungültig gesetzt
	 */
	public final byte getState() {
		return _state;
	}


	/**
	 * Setzt den Status dieses Objektes.
	 *
	 * @param state <code>OBJECT_INVALID</code>, <code>OBJECT_DELETED</code> für ungültige
	 * oder <code>OBJECT_EXISTS</code> für gültige Objekte.
	 *
	 * @see #OBJECT_EXISTS Objekt ist gültig.
	 * @see #OBJECT_INVALID Ungültiges Konfigurationsobjekt
	 * @see #OBJECT_DELETED Dynamisches Objekt wurde auf ungültig gesetzt
	 */
	void setState(byte state) {
		_state = state;
	}

	/**
	 * Implementiert die Vergleichsfunktion des Comparable-Interface.
	 *
	 * @param o Systemobjekt mit dem dieses Objekt verglichen werden sollte.
	 *
	 * @return Negative Zahl, wenn dieses Objekt kleiner ist als das Vergleichsobjekt; positive Zahl, wenn dieses Objekt größer ist als das Vergleichsobjekt;
	 *         <code>0</code>, wenn dieses Objekt mit dem Vergleichsobjekt übereinstimmt.
	 */
	public final int compareTo(Object o) {
		DafSystemObject other = (DafSystemObject)o;
		if(_id < other._id) {
			return -1;
		}
		if(_id > other._id) {
			return 1;
		}
		return 0;
	}

	public final String getNameOrPidOrId() {
		if(_name != null && !_name.equals("")) return _name;
		if(_pid != null && !_pid.equals("")) return _pid;
		return Long.toString(_id);
	}

	public String getPidOrNameOrId() {
		if(_pid != null && !_pid.equals("")) return _pid;
		if(_name != null && !_name.equals("")) return _name;
		return Long.toString(_id);
	}

	public String getPidOrId() {
		if(_pid != null && !_pid.equals("")) return _pid;
		return Long.toString(_id);
	}

	public final long getId() {
		return _id;
	}

	public final String getPid() {
		return (_pid == null ? "" : _pid);
	}

	public final String getName() {
		return _name;
	}

	public final void setName(String _name) throws ConfigurationChangeException {
		final String name = _name != null ? _name : "";

		if(name.length() > 255) {
			throw new ConfigurationChangeException(
					"Der Name des Objekts ist zu lang, es sind nur 255 Zeichen erlaubt. Name, der gesetzt werden sollte: " + _name + " Länge des Strings: "
					+ name.length()
			);
		}

		_dataModel.setName(this, name); // Hier kann eine ConfigurationChangeException auftreten...
		this._name = name; // ...dann wird das hier nicht mehr ausgeführt
	}

	/**
	 * Diese Methode wird aufgerufen, wenn die Konfiguration den Namen des Objekts auf den aktuellen Stand bringen möchte. Der Aufruf der Methode führt nicht zu
	 * einer Anfrage an die Konfiguration wie bei {@link #setName(String)}.
	 * <p>
	 * Eventuell angemeldete Listener werden nicht informiert, soll dies geschehen muss die Methode {@link DafDynamicObjectType#updateName(long,String)} benutzt
	 * werden.
	 *
	 * @param newName Aktueller Name des Objekts
	 */
	protected void storeName(String newName) {
		this._name = newName;
	}

	public final SystemObjectType getType() {
		if(_systemObjectType == null) {
			_systemObjectType = (DafSystemObjectType)_dataModel.getObject(_typeId);
		}
		return _systemObjectType;
	}

	public final void invalidate() throws ConfigurationChangeException {
		_dataModel.invalidate(this);
	}

	public final boolean isOfType(String typePid) {
		if(typePid == null) {
			throw new IllegalArgumentException("Argument ist null");
		}
		if(_systemObjectType == null) {
			getType();
		}
		if(typePid.equals(_systemObjectType.getPid())) {
			return true;
		}
		List list = _systemObjectType.getSuperTypes();
		while((list != null) && (list.size() > 0)) {
			ArrayList tmpList = new ArrayList();
			for(int i = 0; i < list.size(); ++i) {
				DafSystemObjectType _systemObjectType = (DafSystemObjectType)list.get(i);
				if(typePid.equals(_systemObjectType.getPid())) {
					return true;
				}
				List tmp = _systemObjectType.getSuperTypes();
				if(tmp != null) {
					tmpList.addAll(tmp);
				}
			}
			list = tmpList;
		}
		return false;
	}

	public final boolean isOfType(SystemObjectType objectType) {
		String _pid = objectType.getPid();
		if(_pid != null && !_pid.equals("")) {
			return isOfType(_pid);
		}
		else {
			long typeId = objectType.getId();
			if(_systemObjectType == null) {
				getType();
			}
			if(typeId == _systemObjectType.getId()) {
				return true;
			}
			ArrayList list = (ArrayList)_systemObjectType.getSuperTypes();
			while((list != null) && (list.size() > 0)) {
				ArrayList tmpList = new ArrayList();
				for(int i = 0; i < list.size(); ++i) {
					DafSystemObjectType _systemObjectType = (DafSystemObjectType)list.get(i);
					if(typeId == _systemObjectType.getId()) {
						return true;
					}
					tmpList.addAll(_systemObjectType.getSuperTypes());
				}
				list = tmpList;
			}
		}
		return false;
	}

	/**
	 * Gibt den internen Typ zurück
	 *
	 * @return der interne Typ
	 */
	public final byte getInternType() {
		return _internType;
	}

	/**
	 * Gibt ein neues nicht initialisiertes Objekt der richtigen Klasse abhängig vom angegebenen Typfeld zurück
	 *
	 * @param internType Typfeld des gewünschten Systemobjekts
	 * @param dataModel  Objekt zum Zugriff auf die Konfiguration
	 *
	 * @return Neues nicht initialisiertes Systemobjekt
	 */
	public final static DafSystemObject getObject(byte internType, DafDataModel dataModel) {
		switch(internType) {
			case(CONFIGURATION_OBJECT): {
				return new DafConfigurationObject(dataModel);
			}
			case(DYNAMIC_OBJECT): {
				return new DafDynamicObject(dataModel);
			}
			case(DYNAMIC_OBJECT_TYPE): {
				return new DafDynamicObjectType(dataModel);
			}
			case(CONFIGURATION_OBJECT_TYPE): {
				return new DafConfigurationObjectType(dataModel);
			}
			case(OBJECT_SET_TYPE): {
				return new DafObjectSetType(dataModel);
			}
			case(ATTRIBUTE_GROUP): {
				return new DafAttributeGroup(dataModel);
			}
			case(ASPECT): {
				return new DafAspect(dataModel);
			}
			case(OBJECT_SET_USE): {
				return new DafObjectSetUse(dataModel);
			}
			case(MUTABLE_SET): {
				return new DafMutableSet(dataModel);
			}
			case(NON_MUTABLE_SET): {
				return new DafNonMutableSet(dataModel);
			}
			case(ATTRIBUTE): {
				return new DafAttribute(dataModel);
			}
			case(INTEGER_ATTRIBUTE_TYPE): {
				return new DafIntegerAttributeType(dataModel);
			}
			case(INTEGER_VALUE_RANGE): {
				return new DafIntegerValueRange(dataModel);
			}
			case(INTEGER_VALUE_STATE): {
				return new DafIntegerValueState(dataModel);
			}
			case(FLOATING_POINT_NUMBER_ATTRIBUTE_TYPE): {
				return new DafDoubleAttributeType(dataModel);
			}
			case(REFERENCE_ATTRIBUTE_TYPE): {
				return new DafReferenceAttributeType(dataModel);
			}
			case(STRING_ATTRIBUTE_TYPE): {
				return new DafStringAttributeType(dataModel);
			}
			case(TIME_ATTRIBUTE_TYPE): {
				return new DafTimeAttributeType(dataModel);
			}
			case(ATTRIBUTE_LIST_TYPE): {
				return new DafAttributeListDefinition(dataModel);
			}
			case(CONFIGURATION_AUTHORITY): {
				return new DafConfigurationAuthority(dataModel);
			}
			case(CONFIGURATION_AREA): {
				return new DafConfigurationArea(dataModel);
			}
			case(DAV_APPLICATION): {
				return new DafDavApplication(dataModel);
			}
			case(CLIENT_APPLICATION): {
				return new DafClientApplication(dataModel);
			}
			case(ATTRIBUTE_GROUP_USAGE): {
				return new DafAttributeGroupUsage(dataModel);
			}
			default: {
				return null;
			}
		}
	}

	public DataModel getDataModel() {
		return _dataModel;
	}

	public final Data getConfigurationData(AttributeGroup atg) {
		return _dataModel.getConfigurationData(this, atg);
	}

	public Data getConfigurationData(AttributeGroup atg, Aspect asp) {
		return _dataModel.getConfigurationData(this, atg.getAttributeGroupUsage(asp));
	}

	public Data getConfigurationData(AttributeGroupUsage atgUsage) {
		return _dataModel.getConfigurationData(this, atgUsage);
	}

	public void setConfigurationData(AttributeGroup atg, Data data)
			throws ConfigurationChangeException {
		setConfigurationData(atg.getAttributeGroupUsage(_dataModel.getAspect("asp.eigenschaften")), data);
	}

	public void setConfigurationData(
			AttributeGroup atg, Aspect aspect, Data data
	) throws ConfigurationChangeException {
		setConfigurationData(atg.getAttributeGroupUsage(aspect), data);
	}

	public void setConfigurationData(AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {
		_dataModel.setConfigurationData(this, atgUsage, data);
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		
		final Collection<AttributeGroupUsage> usages = new ArrayList<AttributeGroupUsage>();

		final List<AttributeGroup> attributeGroups = getType().getAttributeGroups();
		for(final AttributeGroup attributeGroup : attributeGroups) {
			for(final AttributeGroupUsage attributeGroupUsage : attributeGroup.getAttributeGroupUsages()) {
				if(attributeGroupUsage.isConfigurating()) {
					if(getConfigurationData(attributeGroupUsage) != null) {
						usages.add(attributeGroupUsage);
					}
				}
			}
		}

		return usages;
	}

	public abstract boolean isValid();

	public SystemObjectInfo getInfo() {
		Data infoData;
		try {
			infoData = getConfigurationData(_dataModel.getAttributeGroup("atg.info"));
		}
		catch(Exception e) {
			infoData = null;
		}
		if(infoData == null) {
			return SystemObjectInfo.UNDEFINED;
		}
		return new SystemObjectInfo(infoData.getTextValue("kurzinfo").getValueText(), infoData.getTextValue("beschreibung").getValueText());
	}

}
