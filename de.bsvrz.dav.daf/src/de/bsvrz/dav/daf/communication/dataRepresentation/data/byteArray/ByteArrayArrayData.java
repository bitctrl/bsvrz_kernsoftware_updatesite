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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ByteArrayArrayData extends ByteArrayStructuredData implements Data.Array, Data.TextArray, Data.TimeArray, Data.ReferenceArray, Data.NumberArray {
	private final int _length;

	public ByteArrayArrayData(final byte[] bytes, final int offset, final AttributeInfo attributeInfo) {
		super(bytes, offset, attributeInfo);
		_length = attributeInfo.getElementCount(bytes, offset);
	}

	public String toParamString() {
		return super.toParamString() + ", Anzahl Elemente: " + _length;
	}

	public Data getItem(String itemName) {
		return getItem(Integer.parseInt(itemName));
	}

	public Iterator<Data> iterator() {
		return new ArrayDataIterator();
	}

	public Data getItem(int itemIndex) {
		int itemOffset = _info.getAbsoluteElementOffset(_bytes, _offset, itemIndex);
		return ByteArrayData.create(_bytes, itemOffset, _info.getElementInfo(), itemIndex);
	}

	public TextValue getTextValue(int itemIndex) {
		return getItem(itemIndex).asTextValue();
	}

	public TextValue[] getTextValues() {
		TextValue[] result = new TextValue[_length];
		Iterator iterator = iterator();
		for(int i = 0; i < _length; ++i) {
			Data data = (Data)iterator.next();
			result[i] = data.asTextValue();
		}
		return result;
	}

	public TimeValue getTimeValue(int itemIndex) {
		return getItem(itemIndex).asTimeValue();
	}

	public TimeValue[] getTimeValues() {
		TimeValue[] result = new TimeValue[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getTimeValue(i);
		}
		return result;
	}

	public NumberValue getScaledValue(int itemIndex) {
		return getItem(itemIndex).asScaledValue();
	}

	public NumberValue[] getScaledValues() {
		NumberValue[] result = new NumberValue[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i);
		}
		return result;
	}

	public NumberValue getUnscaledValue(int itemIndex) {
		return getItem(itemIndex).asUnscaledValue();
	}

	public NumberValue[] getUnscaledValues() {
		NumberValue[] result = new NumberValue[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getUnscaledValue(i);
		}
		return result;
	}

	public ReferenceValue getReferenceValue(int itemIndex) {
		return getItem(itemIndex).asReferenceValue();
	}

	public ReferenceValue[] getReferenceValues() {
		ReferenceValue[] result = new ReferenceValue[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getReferenceValue(i);
		}
		return result;
	}

	public int getLength() {
		return _length;
	}

	public void setLength(int newLength) {
		throw new UnsupportedOperationException("setLength(" + newLength + "): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(byte[] bytes) {
		throw new UnsupportedOperationException("set(byte[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(short[] shorts) {
		throw new UnsupportedOperationException("set(short[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(int[] ints) {
		throw new UnsupportedOperationException("set(int[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(long[] longs) {
		throw new UnsupportedOperationException("set(long[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(float[] floats) {
		throw new UnsupportedOperationException("set(float[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(double[] doubles) {
		throw new UnsupportedOperationException("set(double[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(final SystemObject[] systemObjects) {
		throw new UnsupportedOperationException("set(SystemObject[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(final String[] strings) {
		throw new UnsupportedOperationException("set(String[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void set(final ObjectLookup dataModel, final String... systemObjectPids) {
		throw new UnsupportedOperationException("set(ObjectLookup, String[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void setMillis(final long[] millis) {
		throw new UnsupportedOperationException("setMillis(long[]): Das Data-Objekt darf nicht verändert werden");
	}

	public void setSeconds(final long[] seconds) {
		throw new UnsupportedOperationException("setSeconds(long[]): Das Data-Objekt darf nicht verändert werden");
	}

	public int getMaxCount() {
		return _info.getMaxCount();
	}

	public boolean isCountLimited() {
		return _info.isCountLimited();
	}

	public boolean isCountVariable() {
		return _info.isCountVariable();
	}

	private class ArrayDataIterator implements Iterator<Data> {
		private int _nextElementIndex = 0;
		private int _nextElementOffset = _offset + 4;

		public boolean hasNext() {
			if(_nextElementIndex < _length) return true;
			return false;
		}

		public Data next() {
			if(_nextElementIndex >= _length) throw new NoSuchElementException();
			final AttributeInfo elementInfo = _info.getElementInfo();
			final ByteArrayData data = ByteArrayData.create(_bytes, _nextElementOffset, elementInfo, _nextElementIndex);
			_nextElementOffset += elementInfo.getSize(_bytes, _nextElementOffset);
			++_nextElementIndex;
			return data;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public boolean isList() {
		return false;
	}

	public boolean isArray() {
		return true;
	}

	public boolean isPlain() {
		return false;
	}

	public Data.Array asArray() {
		return this;
	}

	public Data.TimeArray asTimeArray() {
		if(_info.getDefinitionInfo().isTimeAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " enthält keine Zeitangaben");
	}

	public long getSeconds(int itemIndex) {
		return getTimeValue(itemIndex).getSeconds();
	}

	public long getMillis(int itemIndex) {
		return getTimeValue(itemIndex).getMillis();
	}

	public long[] getSecondsArray() {
		long[] result = new long[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getTimeValue(i).getSeconds();
		}
		return result;
	}

	public long[] getMillisArray() {
		long[] result = new long[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getTimeValue(i).getMillis();
		}
		return result;
	}

	public Data.TextArray asTextArray() {
		return this;
	}

	public String getText(int itemIndex) {
		return getTextValue(itemIndex).getText();
	}

	public String[] getTextArray() {
		String[] result = new String[_length];
		Iterator iterator = iterator();
		for(int i = 0; i < _length; ++i) {
			Data data = (Data)iterator.next();
			result[i] = data.asTextValue().getText();
		}
		return result;
	}

	public Data.ReferenceArray asReferenceArray() {
		if(_info.getDefinitionInfo().isReferenceAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " enthält keine Referenzen");
	}

	public SystemObject getSystemObject(int itemIndex) {
		return getReferenceValue(itemIndex).getSystemObject();
	}

	public SystemObject[] getSystemObjectArray() {
		SystemObject[] result = new SystemObject[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getReferenceValue(i).getSystemObject();
		}
		return result;
	}

	public Data.NumberArray asScaledArray() {
		if(_info.getDefinitionInfo().isNumberAttribute()) return this;
		throw new UnsupportedOperationException("Das Attribut " + getName() + " enthält keine Zahlen");
	}

	public NumberValue getValue(int itemIndex) {
		return getScaledValue(itemIndex);
	}

	public NumberValue[] getValues() {
		return getScaledValues();
	}

	public byte byteValue(int itemIndex) {
		return getScaledValue(itemIndex).byteValue();
	}

	public short shortValue(int itemIndex) {
		return getScaledValue(itemIndex).shortValue();
	}

	public int intValue(int itemIndex) {
		return getScaledValue(itemIndex).intValue();
	}

	public long longValue(int itemIndex) {
		return getScaledValue(itemIndex).longValue();
	}

	public float floatValue(int itemIndex) {
		return getScaledValue(itemIndex).floatValue();
	}

	public double doubleValue(int itemIndex) {
		return getScaledValue(itemIndex).doubleValue();
	}

	public byte[] getByteArray() {
		byte[] result = new byte[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).byteValue();
		}
		return result;
	}

	public short[] getShortArray() {
		short[] result = new short[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).shortValue();
		}
		return result;
	}

	public int[] getIntArray() {
		int[] result = new int[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).intValue();
		}
		return result;
	}

	public long[] getLongArray() {
		long[] result = new long[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).longValue();
		}
		return result;
	}

	public float[] getFloatArray() {
		float[] result = new float[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).floatValue();
		}
		return result;
	}

	public double[] getDoubleArray() {
		double[] result = new double[_length];
		for(int i = 0; i < _length; ++i) {
			result[i] = getScaledValue(i).doubleValue();
		}
		return result;
	}

	public Data.NumberArray asUnscaledArray() {
		final AttributeDefinitionInfo definitionInfo = _info.getDefinitionInfo();
		if(definitionInfo.isNumberAttribute()) {
			if(definitionInfo.isScalableNumberAttribute()) {
				return new UnscaledNumberArrayView();
			}
			else {
				// Bei DoubleAttributeType und FloatAttributeType:
				return this;
			}
		}
		throw new UnsupportedOperationException("Das Attribut " + getName() + " enthält keine Zahlen");
	}

	private class UnscaledNumberArrayView implements Data.NumberArray {

		public int getLength() {
			return _length;
		}

		public void setLength(int newLength) {
			throw new UnsupportedOperationException("setLength(" + newLength + "): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(byte[] bytes) {
			throw new UnsupportedOperationException("set(byte[]): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(short[] shorts) {
			throw new UnsupportedOperationException("set(short[]): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(int[] ints) {
			throw new UnsupportedOperationException("set(int[]): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(long[] longs) {
			throw new UnsupportedOperationException("set(long[]): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(float[] floats) {
			throw new UnsupportedOperationException("set(float[]): Das Data-Objekt darf nicht verändert werden");
		}

		public void set(double[] doubles) {
			throw new UnsupportedOperationException("set(double[]): Das Data-Objekt darf nicht verändert werden");
		}

		public NumberValue getValue(int itemIndex) {
			return getUnscaledValue(itemIndex);
		}

		public NumberValue[] getValues() {
			return getUnscaledValues();
		}

		public byte byteValue(int itemIndex) {
			return getUnscaledValue(itemIndex).byteValue();
		}

		public short shortValue(int itemIndex) {
			return getUnscaledValue(itemIndex).shortValue();
		}

		public int intValue(int itemIndex) {
			return getUnscaledValue(itemIndex).intValue();
		}

		public long longValue(int itemIndex) {
			return getUnscaledValue(itemIndex).longValue();
		}

		public float floatValue(int itemIndex) {
			return getUnscaledValue(itemIndex).floatValue();
		}

		public double doubleValue(int itemIndex) {
			return getUnscaledValue(itemIndex).doubleValue();
		}

		public byte[] getByteArray() {
			byte[] result = new byte[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).byteValue();
			}
			return result;
		}

		public short[] getShortArray() {
			short[] result = new short[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).shortValue();
			}
			return result;
		}

		public int[] getIntArray() {
			int[] result = new int[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).intValue();
			}
			return result;
		}

		public long[] getLongArray() {
			long[] result = new long[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).longValue();
			}
			return result;
		}

		public float[] getFloatArray() {
			float[] result = new float[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).floatValue();
			}
			return result;
		}

		public double[] getDoubleArray() {
			double[] result = new double[_length];
			for(int i = 0; i < _length; ++i) {
				result[i] = getUnscaledValue(i).doubleValue();
			}
			return result;
		}

	}
}
