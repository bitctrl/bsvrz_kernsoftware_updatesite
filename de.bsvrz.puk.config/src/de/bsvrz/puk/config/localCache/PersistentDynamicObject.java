/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.localCache;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1.AttributeGroupInfo;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class PersistentDynamicObject implements DynamicObject {

	private static final String NO_CHANGE_EXCEPTION = "Veränderungen dieses dynamischen Objektes sind nicht möglich, da es sich nur um eine lokale Kopie handelt "
	                                                 + "und das Original-Objekt nicht verfügbar ist.";

	private final long _id;

	private final String _pid;

	private final String _name;

	private final String _typePid;

	private final long _validSince;

	private final long _notValidSince;

	private final Map<AttributeGroupUsage, ByteArrayData> _configurationData = new HashMap<AttributeGroupUsage, ByteArrayData>();

	/**
	 * Der Konfigurationsbereich dieses SystemObjekts.
	 */
	private final ConfigurationArea _configurationArea;

	public PersistentDynamicObject(final DynamicObject object){
		_configurationArea = object.getConfigurationArea();
		_id = object.getId();
		_pid = object.getPid();
		_name = object.getName();
		_typePid = object.getType().getPid();
		_validSince = object.getValidSince();
		_notValidSince = object.getNotValidSince();
		for(final AttributeGroupUsage attributeGroupUsage : object.getUsedAttributeGroupUsages()) {
			final Data data = object.getConfigurationData(attributeGroupUsage);
			if(data != null) {
				_configurationData.put(attributeGroupUsage, (ByteArrayData)data.createUnmodifiableCopy());
			}
		}
	}

	private PersistentDynamicObject(
			final ConfigurationArea configurationArea,
			final long id,
			final String pid,
			final String name,
			final String typePid,
			final long validSince,
			final long notValidSince,
			final Map<AttributeGroupUsage, ByteArrayData> configurationData) {
		_configurationArea = configurationArea;
		_id = id;
		_pid = pid;
		_name = name;
		_typePid = typePid;
		_validSince = validSince;
		_notValidSince = notValidSince;
		_configurationData.putAll(configurationData);
	}

	public void write(final DataOutputStream outputStream) throws IOException {
		outputStream.writeLong(_id);
		writeString(outputStream, getConfigurationArea() == null ? "" : getConfigurationArea().getPid());
		writeString(outputStream, _pid);
		writeString(outputStream, _name);
		writeString(outputStream, _typePid);
		outputStream.writeLong(_validSince);
		outputStream.writeLong(_notValidSince);
		writeDataMap(outputStream, _configurationData.entrySet());
	}

	private static void writeString(final DataOutputStream outputStream, final String s) throws IOException {
		outputStream.writeInt(s.length());
		outputStream.writeBytes(s);
	}

	private static void writeDataMap(final DataOutputStream outputStream, final Collection<Map.Entry<AttributeGroupUsage, ByteArrayData>> configurationData)
			throws IOException {
		outputStream.writeInt(configurationData.size());
		for(final Map.Entry<AttributeGroupUsage, ByteArrayData> entry: configurationData){
			outputStream.writeLong(entry.getKey().getId());
			final byte[] bytes = entry.getValue().getBytes();
			outputStream.writeInt(bytes.length);
			outputStream.write(bytes);
		}
	}

	public static PersistentDynamicObject read(final DataInputStream inputStream, final DataModel dataModel) throws IOException {
		final long id = inputStream.readLong();
		final String configurationArea = readString(inputStream);
		final String pid = readString(inputStream);
		final String name = readString(inputStream);
		final String typePid = readString(inputStream);
		final long validSince = inputStream.readLong();
		final long notValidSince = inputStream.readLong();
		final Map<AttributeGroupUsage, ByteArrayData> configurationData = readDataMap(inputStream, dataModel);
		return new PersistentDynamicObject(dataModel.getConfigurationArea(configurationArea), id, pid, name, typePid, validSince, notValidSince, configurationData);
	}

	private static String readString(final DataInputStream inputStream) throws IOException {
		final int length = inputStream.readInt();
		final StringBuilder stringBuilder = new StringBuilder(length);
		for(int i = 0; i < length; i++){
			stringBuilder.append((char)inputStream.readByte());
		}
		return stringBuilder.toString();
	}

	private static Map<AttributeGroupUsage, ByteArrayData> readDataMap(final DataInputStream inputStream, final DataModel dataModel) throws IOException {
		final int length = inputStream.readInt();
		final Map<AttributeGroupUsage, ByteArrayData> map = new HashMap<AttributeGroupUsage, ByteArrayData>(length);
		for(int i = 0; i < length; i++){
			final long usageId = inputStream.readLong();
			final byte[] bytes = new byte[inputStream.readInt()];
			inputStream.readFully(bytes, 0, bytes.length);
			final AttributeGroupUsage usage = dataModel.getAttributeGroupUsage(usageId);
			if(usage != null){
				map.put(usage, ByteArrayData.create(bytes, AttributeGroupInfo.forAttributeGroup(usage.getAttributeGroup())));
			}
		}
		return map;
	}

	public long getValidSince() {
		return _validSince;
	}

	public long getNotValidSince() {
		return _notValidSince;
	}

	public boolean isConfigurationCommunicationActive() {
		return false;
	}

	public long getId() {
		return _id;
	}

	public SystemObjectType getType() {
		return getDataModel().getType(_typePid);
	}

	public String getPid() {
		return _pid;
	}

	public String getName() {
		return _name;
	}

	public boolean isValid() {
		return _notValidSince == 0;
	}

	public Data getConfigurationData(final AttributeGroup atg, final Aspect asp) {
		return getConfigurationData(atg.getAttributeGroupUsage(asp));
	}

	public Data getConfigurationData(final AttributeGroupUsage atgUsage) {
		return _configurationData.get(atgUsage);
	}

	public Collection<AttributeGroupUsage> getUsedAttributeGroupUsages() {
		return Collections.unmodifiableCollection(_configurationData.keySet());
	}

	public void addListenerForInvalidation(final InvalidationListener listener) {
		// Implementierung nicht sinnvoll, dieses Objekt ist unveränderlich
	}

	public void removeListenerForInvalidation(final InvalidationListener listener) {
		// Implementierung nicht sinnvoll, dieses Objekt ist unveränderlich
	}

	public void addConfigurationCommunicationChangeListener(final ConfigurationCommunicationChangeListener listener) {
		// Implementierung nicht sinnvoll, dieses Objekt ist unveränderlich
	}

	public void removeConfigurationCommunicationChangeListener(final ConfigurationCommunicationChangeListener listener) {
		// Implementierung nicht sinnvoll, dieses Objekt ist unveränderlich
	}

	public void setName(final String name) throws ConfigurationChangeException {
		throw new ConfigurationChangeException(NO_CHANGE_EXCEPTION);
	}

	public void invalidate() throws ConfigurationChangeException {
		throw new ConfigurationChangeException(NO_CHANGE_EXCEPTION);
	}

	public void setConfigurationData(final AttributeGroup atg, final Aspect asp, final Data data) throws ConfigurationChangeException {
		throw new ConfigurationChangeException(NO_CHANGE_EXCEPTION);
	}

	public void setConfigurationData(final AttributeGroupUsage atgUsage, final Data data) throws ConfigurationChangeException {
		throw new ConfigurationChangeException(NO_CHANGE_EXCEPTION);
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if(!"".equals(_name)) {
			builder.append(_name).append(" ");
		}
		if(!"".equals(_pid)) {
			builder.append("(").append(_pid).append(")");
		}
		else {
			builder.append("[").append(_id).append("]");
		}
		return builder.toString();
	}

	public String getNameOrPidOrId() {
		String result = getName();
		if (result.equals("")) result = getPid();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrNameOrId() {
		String result = getPid();
		if (result.equals("")) result = getName();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public String getPidOrId() {
		String result = getPid();
		if (result.equals("")) result = Long.toString(getId());
		return result;
	}

	public ConfigurationArea getConfigurationArea() {
		return _configurationArea;
	}

	public DataModel getDataModel() {
		return _configurationArea.getDataModel();
	}

	public SystemObjectInfo getInfo() {
		final Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.info"));
		if (data != null) {
			return new SystemObjectInfo(data.getTextValue("kurzinfo").getText(), data.getTextValue("beschreibung").getText());
		} else {
			return SystemObjectInfo.UNDEFINED;
		}
	}

	public Data getConfigurationData(final AttributeGroup atg) {
		return getConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"));
	}

	public void setConfigurationData(final AttributeGroup atg, final Data data) throws ConfigurationChangeException {
		setConfigurationData(atg, getDataModel().getAspect("asp.eigenschaften"), data);
	}

	public boolean isOfType(final String typePid) {
		return isOfType(getDataModel().getType(typePid));
	}

	public boolean isOfType(final SystemObjectType type) {
		if (type.equals(getType())) return true;
		return getType().inheritsFrom(type);
	}

	/**
	 * Der Vergleich zweier SystemObjekte (o1, o) erfolgt durch deren ID.
	 *
	 * @param o zu vergleichendes SystemObjekt
	 * @return <code>-1</code>, falls o1.getId() < o.getId()<br> <code> 1</code>, falls o1.getId() > o.getId()<br> <code> 0</code>, falls o1.getId() == o.getId()
	 */
	public int compareTo(final Object o) {
		final SystemObject otherObject = (SystemObject) o;
		if (getId() < otherObject.getId()) return -1;
		if (getId() > otherObject.getId()) return 1;
		return 0;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final PersistentDynamicObject other = (PersistentDynamicObject)o;

		if(_id != other.getId()) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}
}
