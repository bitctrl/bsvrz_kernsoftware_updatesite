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
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ObjectLookup;

import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ByteArrayPrimitiveData extends ByteArrayData implements Data.TextValue, Data.TimeValue, Data.ReferenceValue, Data.NumberValue {
	public ByteArrayPrimitiveData(byte[] bytes, int offset, AttributeInfo attributeInfo) {
		super(bytes, offset, attributeInfo);
	}

	public Data.Array asArray() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einem Array dargestellt werden");
	}

	public Data getItem(String itemName) {
		throw new UnsupportedOperationException(
		        "getItem(" + itemName + "): Das Attribut " + getName() + " hat keine Unterattribute"
		);
	}

	public Iterator<Data> iterator() {
		throw new UnsupportedOperationException("Über das Attribut " + getName() + " kann nicht iteriert werden");
	}

	public boolean isList() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isPlain() {
		return true;
	}

	public String valueToString() {
		try {
			return asTextValue().getText();
		}
		catch(Exception e) {
			e.printStackTrace();
			return "<<Fehler:" + e.getMessage() + ">>";
		}
	}

	// TextValue-Sicht mit entsprechenden Zugriffsmethoden

	public Data.TextValue asTextValue() {
		return this;
	}

	public String getText() {
		String valueText = getValueText();
		String suffixText = getSuffixText();
		if(suffixText.equals("")) return valueText;
		if(valueText.equals("")) return suffixText;
		return valueText + " " + suffixText;
	}

	public String getValueText() {
		return _info.getDefinitionInfo().getValueText(_bytes, _offset);
	}

	public String getSuffixText() {
		return _info.getDefinitionInfo().getSuffixText(_bytes, _offset);
	}

	public void setText(String text) {
//		Thread.dumpStack();
		throw new UnsupportedOperationException("setText: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	// TimeValue-Sicht mit entsprechenden Zugriffsmethoden

	public Data.TimeValue asTimeValue() {
		if(_info.getDefinitionInfo().isTimeAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Zeitattribut");
	}

	public long getSeconds() {
		return _info.getDefinitionInfo().getSeconds(_bytes, _offset);
	}

	public long getMillis() {
		return _info.getDefinitionInfo().getMillis(_bytes, _offset);
	}

	public void setSeconds(long seconds) {
		throw new UnsupportedOperationException("setSeconds: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void setMillis(long milliSeconds) {
		throw new UnsupportedOperationException("setMillis: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	// ReferenceValue-Sicht mit entsprechenden Zugriffsmethoden

	public Data.ReferenceValue asReferenceValue() {
		if(_info.getDefinitionInfo().isReferenceAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Referenzattribut");
	}

	public long getId() {
		return _info.getDefinitionInfo().getId(_bytes, _offset);
	}

	public SystemObject getSystemObject() {
		return _info.getDefinitionInfo().getSystemObject(_bytes, _offset);
	}

	public void setSystemObject(SystemObject object) {
		throw new UnsupportedOperationException("setSystemObject: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void setSystemObjectPid(String objectPid, ObjectLookup datamodel) {
		throw new UnsupportedOperationException("setSystemObjectPid: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void setSystemObjectPid(final String objectPid) {
		throw new UnsupportedOperationException("setSystemObjectPid: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public String getSystemObjectPid() {
		final SystemObject systemObject = getSystemObject();
		if(systemObject == null) {
			return "";
		}
		else {
			return systemObject.getPid();
		}
	}

	// NumberValue-Sicht mit entsprechenden Zugriffsmethoden

	public Data.NumberValue asScaledValue() {
		if(_info.getDefinitionInfo().isNumberAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Zahl-Attribut");
	}

	public Data.NumberValue asUnscaledValue() {
		if(_info.getDefinitionInfo().isNumberAttribute()) {
			if(_info.getDefinitionInfo().isScalableNumberAttribute()) {
				return new UnscaledNumberValueView();
			}
			else {
				return this;
			}
		}
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Zahl-Attribut");
	}

	private class UnscaledNumberValueView implements Data.NumberValue {
		public boolean isNumber() {
			return _info.getDefinitionInfo().isNumber(_bytes, _offset);
		}

		public boolean isState() {
			return _info.getDefinitionInfo().isState(_bytes, _offset);
		}

		public byte byteValue() {
			return _info.getDefinitionInfo().unscaledByteValue(_bytes, _offset);
		}

		public short shortValue() {
			return _info.getDefinitionInfo().unscaledShortValue(_bytes, _offset);
		}

		public int intValue() {
			return _info.getDefinitionInfo().unscaledIntValue(_bytes, _offset);
		}

		public long longValue() {
			return _info.getDefinitionInfo().unscaledLongValue(_bytes, _offset);
		}

		public float floatValue() {
			return _info.getDefinitionInfo().unscaledFloatValue(_bytes, _offset);
		}

		public double doubleValue() {
			return _info.getDefinitionInfo().unscaledDoubleValue(_bytes, _offset);
		}

		public IntegerValueState getState() {
			return _info.getDefinitionInfo().getState(_bytes, _offset);
		}

		public void setState(IntegerValueState state) {
			throw new UnsupportedOperationException("(unscaled) setState: Das Attribut " + getName() + " darf nicht verändert werden");
		}

		public void set(int value) {
			throw new UnsupportedOperationException("(unscaled) set(int): Das Attribut " + getName() + " darf nicht verändert werden");
		}

		public void set(long value) {
			throw new UnsupportedOperationException("(unscaled) set(long): Das Attribut " + getName() + " darf nicht verändert werden");
		}

		public void set(float value) {
			throw new UnsupportedOperationException("(unscaled) set(float): Das Attribut " + getName() + " darf nicht verändert werden");
		}

		public void set(double value) {
			throw new UnsupportedOperationException("(unscaled) set(double): Das Attribut " + getName() + " darf nicht verändert werden");
		}

		public String getText() {
			String valueText = getValueText();
			String suffixText = getSuffixText();
			if(suffixText.equals("")) return valueText;
			if(valueText.equals("")) return suffixText;
			return valueText + " " + suffixText;
		}

		public String getValueText() {
			return _info.getDefinitionInfo().getUnscaledValueText(_bytes, _offset);
		}

		public String getSuffixText() {
			return _info.getDefinitionInfo().getUnscaledSuffixText(_bytes, _offset);
		}

		public void setText(String text) {
			throw new UnsupportedOperationException("(unscaled) setText: Das Attribut " + getName() + " darf nicht verändert werden");
		}
	}

	public boolean isNumber() {
		return _info.getDefinitionInfo().isNumber(_bytes, _offset);
	}

	public boolean isState() {
		return _info.getDefinitionInfo().isState(_bytes, _offset);
	}

	public byte byteValue() {
		return _info.getDefinitionInfo().byteValue(_bytes, _offset);
	}

	public short shortValue() {
		return _info.getDefinitionInfo().shortValue(_bytes, _offset);
	}

	public int intValue() {
		return _info.getDefinitionInfo().intValue(_bytes, _offset);
	}

	public long longValue() {
		return _info.getDefinitionInfo().longValue(_bytes, _offset);
	}

	public float floatValue() {
		return _info.getDefinitionInfo().floatValue(_bytes, _offset);
	}

	public double doubleValue() {
		return _info.getDefinitionInfo().doubleValue(_bytes, _offset);
	}

	public IntegerValueState getState() {
		return _info.getDefinitionInfo().getState(_bytes, _offset);
	}

	public void setState(IntegerValueState state) {
		throw new UnsupportedOperationException("setState: Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void set(int value) {
		throw new UnsupportedOperationException("set(int): Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void set(long value) {
		throw new UnsupportedOperationException("set(long): Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void set(float value) {
		throw new UnsupportedOperationException("set(float): Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public void set(double value) {
		throw new UnsupportedOperationException("set(double): Das Attribut " + getName() + " darf nicht verändert werden");
	}

	public Data.NumberArray asUnscaledArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.TimeArray asTimeArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.TextArray asTextArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.NumberArray asScaledArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.ReferenceArray asReferenceArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

}
