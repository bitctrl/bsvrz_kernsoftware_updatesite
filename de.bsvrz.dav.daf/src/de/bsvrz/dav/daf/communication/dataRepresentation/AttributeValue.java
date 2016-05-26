/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * TBD Beschreibung
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AttributeValue extends AttributeBaseValue {

	/** Wieviel Stellen Genauigkeit bei Gleitkommazahlen */
	private static final int _precision = 2;

	/** Eine Hilfsvariable für die Zahlenformatierung */
	private static final NumberFormat _numberFormat;

	static {
		_numberFormat = NumberFormat.getNumberInstance();
		_numberFormat.setMinimumFractionDigits(_precision);
		_numberFormat.setMaximumFractionDigits(_precision);
	}

	/** Der Attributeswert */
	private DataValue _value;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param dataModel Datenmodell
	 * @param attribute Attribute
	 */
	public AttributeValue(DataModel dataModel, Attribute attribute) {
		super(dataModel, attribute);
	}

	public final Object getValue() {
		return _value;
	}


	public final void setValue(DataValue value) {
		if(value == null) {
			throw new IllegalArgumentException("Argument ist null");
		}
		_value = value;
	}

	public final void writeValue(DataOutputStream out) throws IOException {
		if(_value == null) {
			throw new IOException("Attribut '" + _attribute.getName() + "': Wert = null");
		}
		_value.write(out);
	}

	public final boolean equals(AttributeBaseValue attributeBaseValue) {
		if(attributeBaseValue == null) {
			return false;
		}
		if(_attribute.getId() != attributeBaseValue.getAttribute().getId()) {
			return false;
		}
		try {
			DataValue value1 = (DataValue)attributeBaseValue.getValue();
			if((_value == null) && (value1 == null)) {
				return true;
			}
			else if((_value == null) || (value1 == null)) {
				return false;
			}
			if((_value != null) && (value1 != null)) {
				return _value.equals(value1);
			}
		}
		catch(ClassCastException ex) {
		}
		return false;
	}

	/**
	 * Gibt den Hashcode zurück
	 *
	 * @return Hashcode
	 */
	public final int hashCode() {
		if(_hashCode == 0) {
			long objectID = _attribute.getId();
			int result = 19;
			result = (41 * result) + (int)(objectID ^ (objectID >>> 32));
			_hashCode = result;
		}
		return _hashCode;
	}

	public final AttributeBaseValue clonePlain() {
		return new AttributeValue(_dataModel, _attribute);
	}

	public final AttributeBaseValue cloneObject() {
		AttributeValue clone = new AttributeValue(_dataModel, _attribute);
		if(_value != null) {
			clone.setValue(_value.cloneObject());
		}
		return clone;
	}

	public final boolean hasValue() {
		if(_value == null) {
			return false;
		}
		return true;
	}

	private final StringBuffer getFormatedValue(IntegerAttributeType attributeType, long value) throws ConfigurationException {
		StringBuffer str = new StringBuffer();
		if(attributeType != null) {
			boolean noEnumValue = true;
			boolean interpreted = false;
			java.util.List list = attributeType.getStates();
			if(list != null) {
				for(int i = 0; i < list.size(); ++i) {
					IntegerValueState integerValueState = (IntegerValueState)list.get(i);
					if((integerValueState != null) && (integerValueState.getValue() == value)) {
						str.append(integerValueState.getName()).append(' ');
						noEnumValue = false;
						interpreted = true;
						break;
					}
				}
			}

			IntegerValueRange integerValueRange = (IntegerValueRange)attributeType.getRange();
			if(integerValueRange != null) {
				String unit = integerValueRange.getUnit();
				double scale = integerValueRange.getConversionFactor();
				if(noEnumValue) {
					if(scale != 0) {
						double number = value * scale;
						str.append(_numberFormat.format(number)).append(' ');
					}
					else {
						str.append(value).append(' ');
					}
					interpreted = true;
				}
				if(unit != null) {
					str.append('(').append(unit).append(") ");
				}
			}
			if(!interpreted) {
				str.append(value);
			}
		}
		return str;
	}
}
