/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Attribute seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafAttribute extends DafConfigurationObject implements Attribute {

	/** Die Position dieses Attributs in der Attributgruppe oder Attributliste */
	private short _attributePosition;

	/** Anzahl von Elementen in einem Feld, falls diese Attribut ein Feld ist */
	private int _arraySize;

	/** Flag, das gesetzt ist, wenn das Attribut ein Array mit variabler Länge ist. */
	private boolean _isDynamicArray;

	/** Die Id des Attributtyps dieses Attributs */
	private long _attributeTypeId;

	/** Der Attributtyp dieses Attributs */
	private DafAttributeType _attributeType;

	/** Default-Attributwert oder <code>null</code> falls nicht definiert. */
	private String _defaultAttributeValue;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafAttribute(DafDataModel dataModel) {
		super(dataModel);
		_internType = ATTRIBUTE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafAttribute(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long setIds[],
			short attributePosition,
			int arraySize,
			boolean isDynamicArray,
			long attributeTypeId,
			String defaultAttributeValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_attributePosition = attributePosition;
		_arraySize = arraySize;
		_isDynamicArray = isDynamicArray;
		_attributeTypeId = attributeTypeId;
		_internType = ATTRIBUTE;
		_defaultAttributeValue = defaultAttributeValue;
	}

	public final String parseToString() {
		String str = "Attribute: \n";
		str += super.parseToString();
		str += "Attributesposition: " + _attributePosition + "\n";
		str += "Anzahl von Elementen: " + _arraySize + "\n";
		str += "Dynamischesfeld: " + _isDynamicArray + "\n";
		if(_attributeType == null) {
			getAttributeType();
		}
		if(_attributeType != null) {
			str += _attributeType.parseToString();
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeShort(_attributePosition);
		out.writeInt(_arraySize);
		out.writeBoolean(_isDynamicArray);
		out.writeLong(_attributeTypeId);
		if(_defaultAttributeValue == null) {
			out.writeBoolean(false);
		}
		else {
			out.writeBoolean(true);
			out.writeUTF(_defaultAttributeValue);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_attributePosition = in.readShort();
		_arraySize = in.readInt();
		_isDynamicArray = in.readBoolean();
		_attributeTypeId = in.readLong();
		if(in.readBoolean()) {
			_defaultAttributeValue = in.readUTF();
		}
		else {
			_defaultAttributeValue = null;
		}
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_attributePosition = deserializer.readShort();
		_arraySize = deserializer.readInt();
		_isDynamicArray = deserializer.readBoolean();
		_attributeTypeId = deserializer.readLong();
		if(deserializer.readBoolean()) {
			_defaultAttributeValue = deserializer.readString();
		}
		else {
			_defaultAttributeValue = null;
		}
	}

	public final AttributeType getAttributeType() {
		if(_attributeType == null) {
			_attributeType = (DafAttributeType) _dataModel.getObject(_attributeTypeId);
		}
		return _attributeType;
	}

	public final int getMaxCount() {
		return _arraySize;
	}

	public final int getPosition() {
		return _attributePosition;
	}

	public final boolean isCountLimited() {
		return _arraySize != 0;
	}

	public final boolean isCountVariable() {
		return _isDynamicArray;
	}

	public final boolean isArray() {
		return _isDynamicArray || (_arraySize != 1);
	}

	public String getDefaultAttributeValue() {
		return _defaultAttributeValue;
	}
}
