/*
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1.AbstractAttributeInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1.ReferenceDefinitionInfo;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.UndefinedAttributeValueAccess;

import java.util.Iterator;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class ByteArrayData implements Data {
	protected final byte[] _bytes;
	protected final int _offset;
	protected final AttributeInfo _info;

	public static ByteArrayData create(byte[] bytes, AttributeInfo attributeGroupInfo) {
		return create(bytes, 0, attributeGroupInfo);
	}

	protected static ByteArrayData create(byte[] bytes, int offset, AttributeInfo attributeInfo) {
		if(attributeInfo.isArray()) {
			return new ByteArrayArrayData(bytes, offset, attributeInfo);
		}
		else if(attributeInfo.getDefinitionInfo().isList()) {
			return new ByteArrayListData(bytes, offset, attributeInfo);
		}
		else {
			return new ByteArrayPrimitiveData(bytes, offset, attributeInfo);
		}
	}

	protected static ByteArrayData create(byte[] bytes, int offset, AttributeInfo attributeInfo, int itemIndex) {
		if(attributeInfo.getDefinitionInfo().isList()) {
			return new ByteArrayListArrayItemData(bytes, offset, attributeInfo, itemIndex);
		}
		else {
			return new ByteArrayPrimitiveArrayItemData(bytes, offset, attributeInfo, itemIndex);
		}
	}

	protected ByteArrayData(byte[] bytes, int offset, AttributeInfo info) {
		_bytes = bytes;
		_offset = offset;
		_info = info;
	}

	public Data createModifiableCopy() {
		return _info.createModifiableData(_bytes);
	}

	public Data createUnmodifiableCopy() {
		return this;
	}

	public String getName() {
		return _info.getName();
	}

	public AttributeType getAttributeType() {
		return _info.getDefinitionInfo().getAttributeType();
	}

	public void setToDefault()
	{
		// Das byte-Array ist "read only", die Daten können also nicht geändert werden.
		throw new UnsupportedOperationException("read only Objekt, die Änderung von Daten wird nicht unterstützt");
	}

	public boolean isDefined()
	{
		if(!isPlain()){
			// Über Kindelemente iterieren
			for(final Data data : this) {
				if(!data.isDefined()) {
					return false;
				}
			}
			return true;
		}

		final AttributeType attributeType = getAttributeType();
		// Alle Attribute, die einen "undefiniert Wert" zu Verfügung stellen, implementieren
		// das Interface "UndefinedAttributeValueAccess"
		if(attributeType instanceof UndefinedAttributeValueAccess) {
			final UndefinedAttributeValueAccess undefinedAttributeValueAccess = (UndefinedAttributeValueAccess)attributeType;
			// Alle Typen, bis auf den <code>StringAttributeType</code> können entscheiden ob
			// die jeweiligen Attribute definiert sind (wenn der Wert des Attributes gleich dem "undefiniert Wert" ist, dann
			// ist das Attribut nicht definiert).

			// Am Attribut kann als Default-Wert der Wert "_Undefiniert" gesetzt werden. Dies entspricht aber dem
			// undefiniert Wert und könnte somit nicht erkannt werden, wenn nur der Attributwert mit dem undefiniert Wert
			// verglichen werden würde.
			// Darum wird an dieser Stelle geprüft, ob am Attribut ein Default-Wert gesetzt wird. Falls dies der Fall ist,
			// ist das Attribut definiert (es ist ja nicht möglich einen Undefiniert Wert anzugeben).
			if(attributeType instanceof StringAttributeType) {
				// Prüfen ob Default-Data am Attribut oder am Attributtyp vorhanden ist.
				if(_info instanceof AbstractAttributeInfo && ((AbstractAttributeInfo)_info).getDefaultAttributeValue() != null) {
					// wenn Defaultwert vorhanden, dann ist der Wert auf jeden Fall definiert, weil es keinen undefinierten Zustand gibt.
					return true;
				}
				else if(attributeType.getDefaultAttributeValue() != null) {
					// wenn Defaultwert vorhanden, dann ist der Wert auf jeden Fall definiert, weil es keinen undefinierten Zustand gibt.
					return true;
				}
			}
			return undefinedAttributeValueAccess.isDefined(this);
		}
		else {
			// Für diesen AttributeType wurde kein "undefiniert Wert" festgelegt (Beispielsweise DoubleAttributeType).
			// Da es keinen undefiniert Wert gibt, sind automatisch alle Werte gültig.
			return true;
		}
	}

	public  final byte[] getBytes() {
		return _bytes;
	}

	public final int getOffset() {
		return _offset;
	}

	protected final AttributeInfo getInfo() {
		return _info;
	}

	public String toDebugString() {
		return getClass().getName() + "(" + toParamString() + ")";
	}

	protected String toParamString() {
		final int size;
		String sizeText;
		try {
			size = _info.getSize(_bytes, _offset);
			sizeText = size + "/0x" + Integer.toHexString(size);
		}
		catch(Exception e) {
			sizeText = "<<" + e + ">>";
		}
		return "name: " + getName() + ", " +
		       "offset: " + _offset + "/0x" + Integer.toHexString(_offset) + ", " +
		       "size: " + sizeText;
	}

	public abstract Data getItem(String itemName);

	public abstract Iterator<Data> iterator();

	public Data getItem(int itemIndex) {
		throw new UnsupportedOperationException("getItem(" + itemIndex + ") ist nicht möglich, weil das Attribut " + getName() + " kein Array ist.");
	}

	public abstract boolean isList();
	public abstract boolean isArray();
	public abstract boolean isPlain();

	public void dump(final int indent, final int depth) {
		for(int i = 0; i < indent; ++i) System.out.print("  ");
		System.out.println(toDebugString());
		if(isPlain() || depth == 0) return;
		try {
			for(Iterator<Data> iterator = iterator(); iterator.hasNext(); ) {
				ByteArrayData subData = (ByteArrayData)iterator.next();
				subData.dump(indent + 1, depth - 1 );
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			for(int i = 0; i < indent; ++i) System.out.print("  ");
			System.out.println("-- " + e + ": " + toDebugString());
		}
	}

	public abstract String valueToString();

	public String toString() {
		return getName() + ":" + valueToString();
	}

	public Data.Array getArray(String itemName) {
		return getItem(itemName).asArray();
	}

	public Data.NumberValue getUnscaledValue(String itemName) {
		return getItem(itemName).asUnscaledValue();
	}

	public Data.NumberArray getUnscaledArray(String itemName) {
		return getItem(itemName).asUnscaledArray();
	}

	public Data.TimeValue getTimeValue(String itemName) {
		return getItem(itemName).asTimeValue();
	}

	public Data.TimeArray getTimeArray(String itemName) {
		return getItem(itemName).asTimeArray();
	}

	public Data.TextValue getTextValue(String itemName) {
		return getItem(itemName).asTextValue();
	}

	public Data.TextArray getTextArray(String itemName) {
		return getItem(itemName).asTextArray();
	}

	public Data.NumberValue getScaledValue(String itemName) {
		return getItem(itemName).asScaledValue();
	}

	public Data.NumberArray getScaledArray(String itemName) {
		return getItem(itemName).asScaledArray();
	}

	public Data.ReferenceValue getReferenceValue(String itemName) {
		return getItem(itemName).asReferenceValue();
	}

	public Data.ReferenceArray getReferenceArray(String itemName) {
		return getItem(itemName).asReferenceArray();
	}

	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj instanceof ByteArrayData) {
			ByteArrayData other = (ByteArrayData)obj;
			final int size = _info.getSize(_bytes, _offset);
			final int otherSize = other._info.getSize(other._bytes, other._offset);
			if(size != otherSize) return false;
			for(int i = 0; i < size; i++) {
				if(_bytes[_offset + i] != other._bytes[other._offset + i]) {
					return false;
				}
			}
			return true;
		}
		else if(obj instanceof Data) {
			String thisText = toString();
			String otherText = obj.toString();
			return thisText.equals(otherText);
		}
		else {
			return false;
		}
	}


	/**
	 * Diese Methode betrachtet rekursiv alle Attribute und sorgt mit entsprechenden Konfigurationsanfragen dafür, dass
	 * Referenzen auf lokal noch nicht bekannte Objekte aufgelöst werden.
	 */
	public void resolveReferences() {
		if(isPlain()) {
			if(getInfo().getDefinitionInfo() instanceof ReferenceDefinitionInfo) {
				asReferenceValue().getSystemObject();
			}
		}
		else {
			for(final Data subData : this) {
				try {
					ByteArrayData item = (ByteArrayData) subData;
					item.resolveReferences();
				}
				catch(Exception ignored) {
				}
			}
		}
	}

}
