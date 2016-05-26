/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;

import java.io.IOException;
import java.util.List;

/**
 * Klasse um Konfigurationsobjekte mit der neuen Methode ({@link de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager})
 * zu serialisieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafSerializerUtil {
	private DafSerializerUtil() {
	}

	/**
	 * Schreibt ein Systemobjekt in einen Serialisierer. Das Objekt kann später mit {@link #readObject} gelesen werden.
	 *
	 * @param serializer Serialisierer
	 * @param object     Zu schreibendes Objekt
	 * @throws IOException
	 */
	public static void writeObject(final Serializer serializer, final SystemObject object) throws IOException {
		if(object == null) {
			serializer.writeByte(DafSystemObject.NULL_OBJECT);
		}
		else if(object instanceof ConfigurationObject) {
			if(object instanceof Aspect) {
				serializer.writeByte(DafSystemObject.ASPECT);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}
			else if(object instanceof Attribute) {
				Attribute attribute = (Attribute) object;
				AttributeType attributeType = attribute.getAttributeType();
				if(attributeType == null) {
					throw new IllegalStateException("Attributtyp des Attributs " + attribute + " nicht definiert");
				}
				serializer.writeByte(DafSystemObject.ATTRIBUTE);
				writeConfigObjectData(serializer, attribute);
				serializer.writeShort(attribute.getPosition());
				serializer.writeInt(attribute.getMaxCount());
				serializer.writeBoolean(attribute.isCountVariable());
				serializer.writeLong(attributeType.getId());
				writeDefault(serializer, attribute.getDefaultAttributeValue());
			}
			else if(object instanceof AttributeGroup) {
				serializer.writeByte(DafSystemObject.ATTRIBUTE_GROUP);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}
			else if(object instanceof AttributeListDefinition) {
				AttributeListDefinition attributeListDefinition = (AttributeListDefinition) object;
				serializer.writeByte(DafSystemObject.ATTRIBUTE_LIST_TYPE);
				writeConfigObjectData(serializer, attributeListDefinition);
				writeDefault(serializer, attributeListDefinition.getDefaultAttributeValue());
			}
			else if(object instanceof DoubleAttributeType) {
				DoubleAttributeType doubleAttributeType = (DoubleAttributeType) object;
				serializer.writeByte(DafSystemObject.FLOATING_POINT_NUMBER_ATTRIBUTE_TYPE);
				writeConfigObjectData(serializer, doubleAttributeType);
				writeDefault(serializer, doubleAttributeType.getDefaultAttributeValue());
				serializer.writeByte(doubleAttributeType.getAccuracy());
				serializer.writeString(doubleAttributeType.getUnit());
			}
			else if(object instanceof IntegerAttributeType) {
				IntegerAttributeType integerAttributeType = (IntegerAttributeType) object;
				IntegerValueRange range = integerAttributeType.getRange();
				serializer.writeByte(DafSystemObject.INTEGER_ATTRIBUTE_TYPE);
				writeConfigObjectData(serializer, integerAttributeType);
				writeDefault(serializer, integerAttributeType.getDefaultAttributeValue());
				serializer.writeByte(integerAttributeType.getByteCount());
				serializer.writeLong(range == null ? 0 : range.getId());
			}
			else if(object instanceof ReferenceAttributeType) {
				ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) object;
				SystemObjectType referencedType = referenceAttributeType.getReferencedObjectType();
				serializer.writeByte(DafSystemObject.REFERENCE_ATTRIBUTE_TYPE);
				writeConfigObjectData(serializer, referenceAttributeType);
				writeDefault(serializer, referenceAttributeType.getDefaultAttributeValue());
				serializer.writeLong(referencedType == null ? 0 : referencedType.getId());
				serializer.writeBoolean(referenceAttributeType.isUndefinedAllowed());
				serializer.writeByte(DafReferenceAttributeType.getReferenceTypeCode(referenceAttributeType.getReferenceType()));
			}
			else if(object instanceof StringAttributeType) {
				StringAttributeType stringAttributeType = (StringAttributeType) object;
				serializer.writeByte(DafSystemObject.STRING_ATTRIBUTE_TYPE);
				writeConfigObjectData(serializer, stringAttributeType);
				writeDefault(serializer, stringAttributeType.getDefaultAttributeValue());
				serializer.writeInt(stringAttributeType.getMaxLength());
				serializer.writeString(stringAttributeType.getEncodingName());
				serializer.writeBoolean(stringAttributeType.isLengthLimited());
			}
			else if(object instanceof TimeAttributeType) {
				TimeAttributeType timeAttributeType = (TimeAttributeType) object;
				serializer.writeByte(DafSystemObject.TIME_ATTRIBUTE_TYPE);
				writeConfigObjectData(serializer, timeAttributeType);
				writeDefault(serializer, timeAttributeType.getDefaultAttributeValue());
				serializer.writeByte(timeAttributeType.getAccuracy());
				serializer.writeBoolean(timeAttributeType.isRelative());
			}
			else if(object instanceof ConfigurationAuthority) {
				serializer.writeByte(DafSystemObject.CONFIGURATION_AUTHORITY);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}
			else if(object instanceof ConfigurationArea) {
				serializer.writeByte(DafSystemObject.CONFIGURATION_AREA);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}
			else if(object instanceof IntegerValueRange) {
				IntegerValueRange integerValueRange = (IntegerValueRange) object;
				serializer.writeByte(DafSystemObject.INTEGER_VALUE_RANGE);
				writeConfigObjectData(serializer, integerValueRange);
				serializer.writeDouble(integerValueRange.getConversionFactor());
				serializer.writeLong(integerValueRange.getMaximum());
				serializer.writeLong(integerValueRange.getMinimum());
				serializer.writeString(integerValueRange.getUnit());
			}
			else if(object instanceof IntegerValueState) {
				IntegerValueState integerValueState = (IntegerValueState) object;
				serializer.writeByte(DafSystemObject.INTEGER_VALUE_STATE);
				writeConfigObjectData(serializer, integerValueState);
				serializer.writeLong(integerValueState.getValue());
			}
			else if(object instanceof MutableSet) {
				MutableSet mutableSet = (MutableSet) object;
				serializer.writeByte(DafSystemObject.MUTABLE_SET);
				writeConfigObjectData(serializer, mutableSet);
				List<SystemObject> elements = mutableSet.getElements();
				serializer.writeInt(elements.size());
				for(SystemObject systemObject : elements) {
					serializer.writeLong(systemObject == null ? 0 : systemObject.getId());
				}
			}
			else if(object instanceof NonMutableSet) {
				NonMutableSet nonMutableSet = (NonMutableSet) object;
				serializer.writeByte(DafSystemObject.NON_MUTABLE_SET);
				writeConfigObjectData(serializer, nonMutableSet);
				List<SystemObject> elements = nonMutableSet.getElements();
				serializer.writeInt(elements.size());
				for(SystemObject systemObject : elements) {
					serializer.writeLong(systemObject == null ? 0 : systemObject.getId());
				}
			}
			else if(object instanceof ObjectSetUse) {
				ObjectSetUse objectSetUse = (ObjectSetUse) object;
				serializer.writeByte(DafSystemObject.OBJECT_SET_USE);
				writeConfigObjectData(serializer, objectSetUse);
				serializer.writeString(objectSetUse.getObjectSetName());
				serializer.writeLong(objectSetUse.getObjectSetType().getId());
				serializer.writeBoolean(objectSetUse.isRequired());
			}
			else if(object instanceof ObjectSetType) {
				ObjectSetType objectSetType = (ObjectSetType) object;
				serializer.writeByte(DafSystemObject.OBJECT_SET_TYPE);
				writeConfigObjectData(serializer, objectSetType);
				serializer.writeBoolean(objectSetType.isNameOfObjectsPermanent());
				serializer.writeInt(objectSetType.getMinimumElementCount());
				serializer.writeInt(objectSetType.getMaximumElementCount());
				serializer.writeBoolean(objectSetType.isMutable());
			}
			else if(object instanceof ConfigurationObjectType) {
				ConfigurationObjectType configurationObjectType = (ConfigurationObjectType) object;
				serializer.writeByte(DafSystemObject.CONFIGURATION_OBJECT_TYPE);
				writeConfigObjectData(serializer, configurationObjectType);
				serializer.writeBoolean(configurationObjectType.isNameOfObjectsPermanent());
			}
			else if(object instanceof DynamicObjectType) {
				DynamicObjectType dynamicObjectType = (DynamicObjectType) object;
				serializer.writeByte(DafSystemObject.DYNAMIC_OBJECT_TYPE);
				writeConfigObjectData(serializer, dynamicObjectType);
				serializer.writeBoolean(dynamicObjectType.isNameOfObjectsPermanent());
			}
			else if(object instanceof AttributeGroupUsage) {
				AttributeGroupUsage attributeGroupUsage = (AttributeGroupUsage) object;
				serializer.writeByte(DafSystemObject.ATTRIBUTE_GROUP_USAGE);
				writeConfigObjectData(serializer, attributeGroupUsage);
				serializer.writeLong(attributeGroupUsage.getAttributeGroup().getId());
				serializer.writeLong(attributeGroupUsage.getAspect().getId());
				serializer.writeBoolean(attributeGroupUsage.isExplicitDefined());
				serializer.writeByte(attributeGroupUsage.getUsage().getId());
			}
			else if(object instanceof DavApplication) {
				serializer.writeByte(DafSystemObject.DAV_APPLICATION);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}
			else {
				serializer.writeByte(DafSystemObject.CONFIGURATION_OBJECT);
				writeConfigObjectData(serializer, (ConfigurationObject) object);
			}

		}
		else if(object instanceof DynamicObject) {
			if(object instanceof ClientApplication) {
				DynamicObject dynamicObject = (DynamicObject) object;
				serializer.writeByte(DafSystemObject.CLIENT_APPLICATION);
				writeDynamicObjectData(serializer, dynamicObject);
			}
			else {
				DynamicObject dynamicObject = (DynamicObject) object;
				serializer.writeByte(DafSystemObject.DYNAMIC_OBJECT);
				writeDynamicObjectData(serializer, dynamicObject);
			}
		}
		else {
			throw new AssertionError("Unbekannter Objekttyp");
		}
	}

	private static void writeDefault(final Serializer serializer, final String defaultAttributeValue) throws IOException {
		if(defaultAttributeValue == null) {
			serializer.writeBoolean(false);
		}
		else {
			serializer.writeBoolean(true);
			serializer.writeString(defaultAttributeValue);
		}
	}

	private static void writeDynamicObjectData(final Serializer serializer, final DynamicObject dynamicObject) throws IOException {
		serializer.writeLong(dynamicObject.getId());
		serializer.writeLong(dynamicObject.getType().getId());
		final String pid = dynamicObject.getPid();
		final String name = dynamicObject.getName();
		byte flag = 0;
		if(dynamicObject.isValid()) flag |= 1;
		if(pid != null) flag |= 2;
		if(name != null) flag |= 4;
		serializer.writeByte(flag);
		if(pid != null) serializer.writeString(pid);
		if(name != null) serializer.writeString(name);
		serializer.writeLong(dynamicObject.getValidSince());
		serializer.writeLong(dynamicObject.getNotValidSince());
		ConfigurationArea configurationArea = dynamicObject.getConfigurationArea();
		long configAreaId = (configurationArea == null ? 0l : configurationArea.getId());
		serializer.writeLong(configAreaId);
	}

	private static void writeConfigObjectData(final Serializer serializer, final ConfigurationObject configurationObject) throws IOException {
		serializer.writeLong(configurationObject.getId());
		serializer.writeLong(configurationObject.getType().getId());
		final String pid = configurationObject.getPid();
		final String name = configurationObject.getName();
		byte flag = 0;
		if(configurationObject.isValid()) flag |= 1;
		if(pid != null) flag |= 2;
		if(name != null) flag |= 4;
		serializer.writeByte(flag);
		if(pid != null) serializer.writeString(pid);
		if(name != null) serializer.writeString(name);
		serializer.writeShort(configurationObject.getValidSince());
		serializer.writeShort(configurationObject.getNotValidSince());
		ConfigurationArea configurationArea = configurationObject.getConfigurationArea();
		long configAreaId = (configurationArea == null ? 0l : configurationArea.getId());
		serializer.writeLong(configAreaId);
		List<ObjectSet> sets = configurationObject.getObjectSets();
		serializer.writeInt(sets.size());
		for(ObjectSet set : sets) {
			serializer.writeLong(set.getId());
		}
	}

	public static DafSystemObject readObject(final Deserializer deserializer, final DafDataModel dataModel) throws IOException {
		byte typeByte = deserializer.readByte();
		if(typeByte == DafSystemObject.NULL_OBJECT) return null;
		DafSystemObject object = DafSystemObject.getObject(typeByte, dataModel);
		object.read(deserializer);
		return object;
	}
}
