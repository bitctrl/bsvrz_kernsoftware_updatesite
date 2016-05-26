/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.DataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.*;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Diese abstarkte Klasse stellt eine Oberklasse zur Erstellung der Basisattributwerte dar. Hier werden weiter Subklassen definiert, die zur
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class AttributeBaseValueDataFactory {

	private static final NumberFormat _integerNumberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

	private static final NumberFormat _doubleNumberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

	private static final NumberFormat _precisionTestNumberFormat;

	static {
		_integerNumberFormat.setMinimumIntegerDigits(1);
		_integerNumberFormat.setMaximumIntegerDigits(999);
		_integerNumberFormat.setGroupingUsed(false);
		_doubleNumberFormat.setMinimumIntegerDigits(1);
		_doubleNumberFormat.setMaximumIntegerDigits(999);
		_doubleNumberFormat.setMinimumFractionDigits(0);
		_doubleNumberFormat.setMaximumFractionDigits(999);
		_doubleNumberFormat.setGroupingUsed(false);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		_precisionTestNumberFormat = new DecimalFormat("0.#", symbols);
		_precisionTestNumberFormat.setMaximumFractionDigits(999);
	}

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	public static Data createAdapter(AttributeBaseValue attributeBaseValue) {
		if(attributeBaseValue instanceof AttributeValue) {
			try {
				if(attributeBaseValue.getAttribute().isArray()) return new AttributeArrayAdapter(attributeBaseValue);
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
			return new AttributeValueAdapter((AttributeValue)attributeBaseValue);
		}
		else if(attributeBaseValue instanceof AttributeListValue) {
			try {
				if(attributeBaseValue.getAttribute().isArray()) return new AttributeArrayAdapter(attributeBaseValue);
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
			return new AttributeListValueAdapter((AttributeListValue)attributeBaseValue);
		}
		else {
			throw new IllegalArgumentException("Nicht unterstützte AttributeBaseValue Erweiterung: " + attributeBaseValue.getClass().getName());
		}
	}

	public static Data createAdapter(AttributeGroup attributeGroup, List attributeBaseValues) {
		return new AttributeGroupAdapter(attributeGroup, attributeBaseValues);
	}

	private static void setToDefault(Attribute attribute, Data data) {
		// Als erstes muss geprüft werden, ob an dem Attribut ein Default-Wert festgelegt wurde. Wurde
		// ein Default-Wert festgelegt, wird dieser benutzt.
		// Wurde am Attribut kein Default-Wert festgelegt, muss am Attributtyp geschaut werden.
		// Wurde am Attributtyp ein Default-Wert festgelegt, so wird dieser für das Attribut benutzt.
		// Wurde am Attributtype kein Default-Wert festgelegt, so wird der "undefiniert Wert" des
		// Attributtyps benutzt.

		// Defaultwert des Attributs bestimmen
		String defaultValueString = attribute.getDefaultAttributeValue();
		if(defaultValueString != null) {
			// Es gibt einen Wert, dieser muss gesetzt werden
			data.asTextValue().setText(defaultValueString);
		}
		else {
			// Am Attribut wurde kein Default-Wert gefunden. Der Typ des Attributes muss auf einen Default-Wert
			// geprüft werden.
			final AttributeType attributeType = attribute.getAttributeType();
			defaultValueString = attributeType.getDefaultAttributeValue();
			if(defaultValueString != null) {
				// Es ist ein Default-Wert vorhanden, dieser muss am Attribut gesetzt werden
				data.asTextValue().setText(defaultValueString);
			}
			else {
				// Es gibt keinen Default-Wert am Type des Attributes. Es muss der "undefiniert Wert"
				// am Attribut gesetzt werden
				if(attributeType instanceof UndefinedAttributeValueAccess) {
					final UndefinedAttributeValueAccess undefinedAttributeValueAccess = (UndefinedAttributeValueAccess)attributeType;
					undefinedAttributeValueAccess.setToUndefined(data);
				}
				else {
					// Für diesen Attributtyp ist kein Default definiert, aber es ist auch kein
					// "undefiniert Wert" definiert. Dies darf nicht passieren, es müßte ein Default-Wert
					// definiert sein.
					throw new IllegalStateException(
							"Es wurde kein Default-Wert definiert, auch ein undefiniert Wert ist nicht definiert. Attributtyp: "
							+ attributeType.getPidOrNameOrId()
					);
				}
			}
		}
	}

	private static boolean isDefined(Attribute attribute, Data data) {
		final AttributeType attributeType = attribute.getAttributeType();
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
				if(attribute.getDefaultAttributeValue() != null || attribute.getAttributeType().getDefaultAttributeValue() != null) {
					// wenn Defaultwert vorhanden, dann ist der Wert auf jeden Fall definiert, weil es keinen Undefinierten Zustand gibt.
					return true;
				}
			}
			return undefinedAttributeValueAccess.isDefined(data);
		}
		else {
			// Für diesen Attributtype wurde kein "undefiniert Wert" festgelegt (Beispielsweise DoubleAttributeType).
			// Da es keinen undefiniert Wert gibt, sind automatisch alle Werte gültig.
			return true;
		}
	}

	/** Subklasse von <code>AttributeBaseValueDataFactory</code>, abgeleitet von <code>AttributeListValueAdapter</code>. */
	private static class AttributeSetAdapter extends AttributeListValueAdapter {

		AttributeSetAdapter(String name, AttributeSet attributeSet, List attributeBaseValues) {
			super(name, attributeBaseValues);
		}
	}

	/** Subklasse von <code>AttributeBaseValueDataFactory</code>, abgeleitet von <code>AttributeSetAdapter</code>. */
	public static class AttributeGroupAdapter extends AttributeSetAdapter {

		public final AttributeGroup _attributeGroup;

		public final List _attributeBaseValueList;

		private AttributeGroupAdapter(AttributeGroup attributeGroup, List attributeBaseValues) {
			super(attributeGroup.getPid(), attributeGroup, attributeBaseValues);
			_attributeGroup = attributeGroup;
			_attributeBaseValueList = attributeBaseValues;
		}

		public Data createModifiableCopy() {
			List attributeValues = new ArrayList();
			final int listSize = _attributeBaseValueList.size();
			for(int i = 0; i < listSize; ++i) {
				AttributeBaseValue value = (AttributeBaseValue)_attributeBaseValueList.get(i);
				attributeValues.add(value.cloneObject());
			}
			return new AttributeGroupAdapter(_attributeGroup, attributeValues);
		}

		public Data createUnmodifiableCopy() {
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteBuffer);
			try {
				for(Iterator iterator = _attributeBaseValueList.iterator(); iterator.hasNext();) {
					AttributeBaseValue attributeBaseValue = (AttributeBaseValue)iterator.next();
					if(attributeBaseValue == null) throw new IllegalArgumentException("Unvollständiger Datensatz kann nicht gesendet werden");
					attributeBaseValue.writeValue(out);
				}
				out.close();
			}
			catch(IOException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			byte[] dataBytes = byteBuffer.toByteArray();
			return DataFactory.forVersion(1).createUnmodifiableData(_attributeGroup, dataBytes);
		}
	}

	/** Subklasse von <code>AttributeBaseValueDataFactory</code>, abgeleitet von <code>AbstractData.ListData</code>. */
	private static class AttributeListValueAdapter extends AbstractData.ListData {

		private String _name = null;

		private List<Data> _dataList = null;

		private final AttributeType _attributeType;

		private final int _minimumHashSize = 10;

		private final Map<String, Data> _dataMap;


		private AttributeListValueAdapter(String name, List attributeBaseValueList) {
			_name = name;
			_attributeType = null;
			Iterator i = attributeBaseValueList.iterator();
			int listSize = attributeBaseValueList.size();
			List<Data> dataList = new ArrayList<Data>(listSize);
			Map<String, Data> dataMap = null;
			if(listSize >= _minimumHashSize) dataMap = new HashMap<String, Data>(listSize);
			while(i.hasNext()) {
				AttributeBaseValue attributeBaseValue = (AttributeBaseValue)i.next();
				Data item = AttributeBaseValueDataFactory.createAdapter(attributeBaseValue);
				dataList.add(item);
				if(dataMap != null) dataMap.put(item.getName(), item);
			}
			_dataList = dataList;
			_dataMap = dataMap;
		}

		private AttributeListValueAdapter(AttributeListValue attributeListValue) {
			this(attributeListValue.getName(), attributeListValue);
		}

//		public AttributeListValueAdapter(String name, AttributeListValue attributeListValue) {
//			this(name, attributeListValue);
//			_attributeListValue = attributeListValue;
//		}

		public AttributeListValueAdapter(String name, AttributeListValue attributeListValue) {
			this(name, attributeListValue, 0);
		}

		public AttributeListValueAdapter(String name, AttributeListValue attributeListValue, int arrayIndex) {
			AttributeBaseValue[] listValues;
			try {
				listValues = attributeListValue.getAttributeBaseValues();
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
			//_attributeListValue = attributeListValue;
			try {
				_attributeType = attributeListValue.getAttribute().getAttributeType();
			}
			catch(ConfigurationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			int numberOfAttributes;
			try {
				numberOfAttributes = ((AttributeListDefinition)getAttributeType()).getAttributes().size();
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
			int indexOfFirstAttribute = arrayIndex * numberOfAttributes;
			int indexBehindLastAttribute = indexOfFirstAttribute + numberOfAttributes;
			_name = name;
//			List dataList= new LinkedList();
//			for(int i=indexOfFirstAttribute; i<indexBehindLastAttribute; ++i) {
//				dataList.add(AttributeBaseValueDataFactory.createAdapter(listValues[i]));
//			}
//			_dataList= dataList;
			List<Data> dataList = new ArrayList<Data>(numberOfAttributes);
			Map<String, Data> dataMap = null;
			if(numberOfAttributes >= _minimumHashSize) dataMap = new HashMap<String, Data>(numberOfAttributes);
			for(int i = indexOfFirstAttribute; i < indexBehindLastAttribute; ++i) {
				Data item = AttributeBaseValueDataFactory.createAdapter(listValues[i]);
				dataList.add(item);
				if(dataMap != null) dataMap.put(item.getName(), item);
			}
			_dataList = dataList;
			_dataMap = dataMap;
		}

//		public AttributeListValueAdapter(String name, AttributeListValue attributeListValue) {
//			AttributeBaseValue[] listValues;
//			try {
//				listValues= attributeListValue.getAttributeBaseValues();
//			}
//			catch(ConfigurationException e) {
//				throw new RuntimeException(e);
//			}
//			_attributeListValue = attributeListValue;
//			_name= name;
//			List dataList= new LinkedList();
//			for(int i=0; i< listValues.length; ++i) {
//				dataList.add(AttributeBaseValueDataFactory.createAdapter(listValues[i]));
//			}
//			_dataList= dataList;
//		}

		public AttributeType getAttributeType() {
			return _attributeType;
		}

		public String getName() {
			return _name;
		}

		public Data getItem(String itemName) {
			if(_dataMap == null) return super.getItem(itemName);
			Data item = _dataMap.get(itemName);
			if(item == null) throw new NoSuchElementException("Attribut " + itemName + " nicht im Datensatz enthalten: " + this);
			//System.out.println("hash bei " + itemName);
			return item;
		}

		public Iterator<Data> iterator() {
			return _dataList.iterator();
		}
	}

	/** Subklasse von <code>AttributeBaseValueDataFactory</code>, abgeleitet von <code>AbstractData.PlainData</code>. */
	private static class AttributeValueAdapter extends AbstractData.PlainData {

		private AttributeValue _attributeValue;
		//		private Data.Value _valueAdapter= null;
		//		private Data.Array _arrayAdapter= null;

		public AttributeValueAdapter(AttributeValue attributeValue) {
			_attributeValue = attributeValue;
		}

		public String getName() {
			return _attributeValue.getName();
		}

		public AttributeType getAttributeType() {
			try {
				return _attributeValue.getAttribute().getAttributeType();
			}
			catch(Exception e) {
				return null;
			}
		}

		public boolean isDefined() {
			return AttributeBaseValueDataFactory.isDefined(_attributeValue.getAttribute(), this);
		}

		public void setToDefault() {
			AttributeBaseValueDataFactory.setToDefault(_attributeValue.getAttribute(), this);
		}

//		public String toString() {
//			if(isDefined()) {
//				return _attributeValue.valueToString().toString();
//			}
//			else {
//				return "<Undefiniert>";
//			}
//		}

//		/**
//		 * Gibt zu einem Objekt den konfigurierenden Datensatz zzurück, der den Default-Wert des Objekts enthält
//		 *
//		 * @param configurationObject Objekt, zu dem ein Datensatz zurückgegeben werden soll
//		 * @return Datensatz oder <code>null</code> falls kein Datensatz gefunden werden konnte
//		 */
//		public Data getDefaultValueConfigurationData(ConfigurationObject configurationObject) {
//			return configurationObject.getConfigurationData(_attributeValue.getAttribute().getDataModel().getAttributeGroup("atg.defaultAttributwert"));
//		}

		public Data.NumberValue asUnscaledValue() {
			try {
				AttributeType type = getAttributeType();
				if(type instanceof IntegerAttributeType) {
					switch(((IntegerAttributeType)type).getByteCount()) {
						case IntegerAttributeType.BYTE:
							return new UnscaledByteValueAdapter();
						case IntegerAttributeType.SHORT:
							return new UnscaledShortValueAdapter();
						case IntegerAttributeType.INT:
							return new UnscaledIntegerValueAdapter();
						case IntegerAttributeType.LONG:
							return new UnscaledLongValueAdapter();
						default:
							throw new IllegalStateException("ungültige Anzahl Bytes im Attributtyp " + type);
					}
				}
				else if(type instanceof DoubleAttributeType) {
					switch(((DoubleAttributeType)type).getAccuracy()) {
						case DoubleAttributeType.FLOAT:
							return new FloatValueAdapter();
						case DoubleAttributeType.DOUBLE:
							return new DoubleValueAdapter();
						default:
							throw new IllegalStateException("ungültige Genauigkeit im Attributtyp " + type);
					}
				}
				else {
					throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht als unskalierte Zahl dargestellt werden");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public Data.NumberValue asScaledValue() {
			try {
				AttributeType type = getAttributeType();
				if(type instanceof IntegerAttributeType) {
					switch(((IntegerAttributeType)type).getByteCount()) {
						case IntegerAttributeType.BYTE:
							return new ScaledByteValueAdapter();
						case IntegerAttributeType.SHORT:
							return new ScaledShortValueAdapter();
						case IntegerAttributeType.INT:
							return new ScaledIntegerValueAdapter();
						case IntegerAttributeType.LONG:
							return new ScaledLongValueAdapter();
						default:
							throw new IllegalStateException("üngültige Anzahl Bytes im Attributtyp " + type);
					}
				}
				else if(type instanceof DoubleAttributeType) {
					switch(((DoubleAttributeType)type).getAccuracy()) {
						case DoubleAttributeType.FLOAT:
							return new FloatValueAdapter();
						case DoubleAttributeType.DOUBLE:
							return new DoubleValueAdapter();
						default:
							throw new IllegalStateException("üngültige Genauigkeit im Attributtyp " + type);
					}
				}
				else {
					throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht als skalierte Zahl dargestellt werden");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public Data.ReferenceValue asReferenceValue() {
			return new ReferenceValueAdapter();
		}

		public Data.TimeValue asTimeValue() {
			try {
				AttributeType type = getAttributeType();
				if(type instanceof TimeAttributeType) {
					TimeAttributeType timeType = ((TimeAttributeType)type);
					if(timeType.getAccuracy() == TimeAttributeType.MILLISECONDS) {
						if(timeType.isRelative()) {
							return new RelativeMillisTimeValueAdapter();
						}
						else {
							return new AbsoluteMillisTimeValueAdapter();
						}
					}
					else {
						if(timeType.isRelative()) {
							return new RelativeSecondsTimeValueAdapter();
						}
						else {
							return new AbsoluteSecondsTimeValueAdapter();
						}
					}
				}
				else {
					throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht als Zeit dargestellt werden");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public Data.TextValue asTextValue() {
			try {
				AttributeType type = getAttributeType();
				if(type instanceof IntegerAttributeType) {
					return asScaledValue();
				}
				else if(type instanceof StringAttributeType) {
					return new TextValueAdapter();
				}
				else if(type instanceof ReferenceAttributeType) {
					return new ReferenceValueAdapter();
				}
				else if(type instanceof TimeAttributeType) {
					return asTimeValue();
				}
				else if(type instanceof DoubleAttributeType) {
					if(((DoubleAttributeType)type).getAccuracy() == DoubleAttributeType.DOUBLE) {
						return new DoubleValueAdapter();
					}
					else {
						return new FloatValueAdapter();
					}
				}
				else {
					throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht als Text dargestellt werden");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		abstract private static class NumberValueAdapter extends AbstractData.NumberValue {
			//empty
		}

		abstract private static class IntegerValueAdapter extends NumberValueAdapter {

			private static Pattern _locationDistancePattern = Pattern.compile("[0-9]{1,5}\\s*-\\s*[0-9]{1,3}");

			abstract protected long getUnscaledLongValue();

			abstract protected void setUnscaledLongValue(long value);

			abstract protected AttributeType getAttributeType();

			abstract protected String getName();

			protected IntegerValueState getState(long value, IntegerAttributeType type) {
				try {
					List<IntegerValueState> states = type.getStates();
					Iterator<IntegerValueState> i = states.iterator();
					while(i.hasNext()) {
						IntegerValueState state = i.next();
						if(state.getValue() == value) return state;
					}
				}
				catch(Exception e) {
				}
				return null;
			}

			public IntegerValueState getState() {
				try {
					long value = getUnscaledLongValue();
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					return getState(value, type);
				}
				catch(Exception e) {
					return null;
				}
			}

			public void setText(String text) {
				text = text.trim();
				try {
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					List<IntegerValueState> states = type.getStates();
					Iterator<IntegerValueState> i = states.iterator();
					int matchLength = 0;
					IntegerValueState longestMatch = null;
					while(i.hasNext()) {
						IntegerValueState state = i.next();
						if(text.equals(state.getName())) {
							setUnscaledLongValue(state.getValue());
							return;
						}
						if(text.startsWith(state.getName())) {
							if(state.getName().length() > matchLength) {
								longestMatch = state;
								matchLength = state.getName().length();
							}
						}
					}
					if(longestMatch != null) {
						setUnscaledLongValue(longestMatch.getValue());
						return;
					}
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}

				// Ist der Text im location-distance Format?
				if(_locationDistancePattern.matcher(text).matches()) {
					String[] locationDistance = text.split("\\s*-\\s*");
					int location = Integer.parseInt(locationDistance[0]);
					int distance = Integer.parseInt(locationDistance[1]);
					if(location > 65535) {
						throw new IllegalArgumentException("Wert im Location-Distance Format aber Location zu groß: " + location);
					}
					if(distance > 255) {
						throw new IllegalArgumentException("Wert im Location-Distance Format aber Distance zu groß: " + distance);
					}
					setUnscaledLongValue((location * 256) + distance);
					return;
				}

				try {
					super.setText(text);
				}
				catch(RuntimeException e) {

					throw new IllegalArgumentException(
							"kein passender Werte-Zustand und " + e.getMessage() + " Attribut: " + getName() + ", Wert: " + text
							+ (this instanceof UnscaledValueAdapter ? "(unskaliert)" : "")
					);
				}
			}

			public void setState(IntegerValueState newState) {
				try {
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					List<IntegerValueState> states = type.getStates();
					Iterator<IntegerValueState> i = states.iterator();
					while(i.hasNext()) {
						IntegerValueState state = i.next();
						if(state.getId() == newState.getId()) {
							setUnscaledLongValue(state.getValue());
							return;
						}
					}
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
				throw new IllegalArgumentException("Zustand ungültig " + newState);
			}
		}

		abstract private static class UnscaledValueAdapter extends IntegerValueAdapter {

			protected long getUnscaledLongValue() {
				return longValue();
			}

			protected void setUnscaledLongValue(long value) {
				set(value);
			}

			public String getSuffixText() {
				StringBuffer text = new StringBuffer();
				try {
					long value = longValue();
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					IntegerValueState state = getState(value, type);
					if(state != null) return text.append("(").append(state.getName()).append(")").toString();
					IntegerValueRange range = type.getRange();
					if(range == null || value < range.getMinimum() || value > range.getMaximum()) {
						return text.append("<<ungültiger Wert>>").toString();
					}
					double conversionFactor = range.getConversionFactor();
					String separator = " ";
					if(conversionFactor == 1) {
						separator = "";
					}
					else {
						synchronized(_doubleNumberFormat) {
							text.append("*").append(_doubleNumberFormat.format(conversionFactor));
						}
					}
					String unit = range.getUnit();
					if(unit != null && !unit.equals("")) text.append(separator).append(unit);
					return text.toString();
				}
				catch(Exception e) {
					return text.append(formatError(e)).toString();
				}
			}

			public String getValueText() {
				try {
					return String.valueOf(longValue());
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

			abstract public void set(long value);

			public void set(double value) {
				if(value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
					throw new IllegalArgumentException("Fließkommawert " + value + " kann nicht in Ganzzahlwert konvertiert werden");
				}
				set(Math.round(value));
			}
		}

		private class UnscaledByteValueAdapter extends UnscaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public byte byteValue() {
				return ((Byte)((DataValue)_attributeValue.getValue()).getValue()).byteValue();
			}

			public void set(long value) {
				if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) throw new IllegalArgumentException("Wert " + value + " nicht in einem Byte darstellbar");
				_attributeValue.setValue(new ByteAttribute((byte)value));
			}
		}

		private class UnscaledShortValueAdapter extends UnscaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public short shortValue() {
				return ((Short)((DataValue)_attributeValue.getValue()).getValue()).shortValue();
			}

			public void set(long value) {
				if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
					throw new IllegalArgumentException("Wert " + value + " nicht in einem Short darstellbar");
				}
				_attributeValue.setValue(new ShortAttribute((short)value));
			}
		}

		private class UnscaledIntegerValueAdapter extends UnscaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public int intValue() {
				return ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
			}

			public void set(long value) {
				if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("Wert " + value + " nicht in einem Integer darstellbar");
				}
				_attributeValue.setValue(new IntegerAttribute((int)value));
			}
		}

		private class UnscaledLongValueAdapter extends UnscaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public long longValue() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue();
			}

			public void set(long value) {
				_attributeValue.setValue(new LongAttribute(value));
			}
		}

		abstract private static class ScaledValueAdapter extends IntegerValueAdapter {

			public String getSuffixText() {
				try {
					long value = getUnscaledLongValue();
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					IntegerValueState state = getState(value, type);
					if(state == null) {
						IntegerValueRange range = type.getRange();
						if(range != null && value >= range.getMinimum() && value <= range.getMaximum()) {
							String unit = range.getUnit();
							if(unit != null) return unit;
						}
					}
					return "";
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

			public String getValueText() {
				try {
					long value = getUnscaledLongValue();
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					IntegerValueState state = getState(value, type);
					if(state != null) return state.getName();
					IntegerValueRange range = type.getRange();
					if(range == null || value < range.getMinimum() || value > range.getMaximum()) {
						return " <<ungültiger Wert (" + value + ")>>";
					}
					double conversionFactor = range.getConversionFactor();
					if(conversionFactor == 1) {
						return String.valueOf(value);
					}
					else {
						int precision = 0;
						synchronized(_integerNumberFormat) {
							String formatted = _precisionTestNumberFormat.format(conversionFactor);
							int kommaPosition = formatted.lastIndexOf(',');
							if(kommaPosition >= 0) precision = formatted.length() - kommaPosition - 1;
							_integerNumberFormat.setMinimumFractionDigits(precision);
							_integerNumberFormat.setMaximumFractionDigits(precision);
							return _integerNumberFormat.format(doubleValue());
						}
					}
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

			/**
			 * Bestimmt, ob der aktuelle Wert des Datums durch einen Zahlwert dargestellt werden kann. In einer unskalierten Ansicht kann jeder gültige Wert des Datums
			 * als Zahlwert dargestellt werden. In einer skalierten Ansicht von Ganzzahlattributen werden nur Werte innerhalb des definierten Wertebereichs als Zahlwerte
			 * entsprechend des Skalierungsfaktors dargestellt.
			 *
			 * @return <code>true</code>, wenn der Wert aktuelle Wert des Datums durch einen Zahlwert dargestellt werden kann, sonst <code>false</code>.
			 */
			public boolean isNumber() {
				try {
					IntegerValueRange range = ((IntegerAttributeType)getAttributeType()).getRange();
					if(range == null) return false;
					long value = getUnscaledLongValue();
					if(value < range.getMinimum() || value > range.getMaximum()) return false;
					return true;
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}

			public long longValue() {
				try {
					IntegerValueRange range = ((IntegerAttributeType)getAttributeType()).getRange();
					if(range == null) throw new ArithmeticException("Attribut " + getName() + " hat keinen definierten Wertebereich");
					if(range.getConversionFactor() != 1) throw new ArithmeticException("Attribut " + getName() + " hat Skalierungsfaktor ungleich 1");
					long value = getUnscaledLongValue();
					if(value < range.getMinimum() || value > range.getMaximum()) {
						throw new IllegalStateException("Attribut " + getName() + ": Wert " + value + " nicht im Bereich");
					}
					return value;
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}

			public double doubleValue() {
				try {
					IntegerValueRange range = ((IntegerAttributeType)getAttributeType()).getRange();
					if(range == null) throw new ArithmeticException("Attribut " + getName() + " hat keinen definierten Wertebereich");
					long value = getUnscaledLongValue();
					if(value < range.getMinimum() || value > range.getMaximum()) {
						throw new IllegalStateException("Attribut " + getName() + ": Wert " + value + " nicht im Bereich");
					}
					double conversionFactor = range.getConversionFactor();
					
					// Folgende Fallunterscheidung vermeidet Genauigkeitsfehler bei conversionFactor < 1.0:
					// Beispiel: 
					// 95362170 * 0.000001 = 95.36216999999999
					// aber
					// 95362170 / (1 / 0.000001) = 95.36217 (wie erwartet)
					if(conversionFactor < 1.0) {
						return value / (1 / conversionFactor);
					}
					else if(conversionFactor > 1.0){
						return value * conversionFactor;
					}
					else {
						return value;
					}
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}

			public void set(long value) {
				try {
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					IntegerValueRange range = type.getRange();
					if(range == null) {
						throw new IllegalArgumentException("kein Zahlenbereich definiert");
					}
					long unscaledValue;
					double conversionFactor = range.getConversionFactor();
					//Folgende Fallunterscheidung ist erforderlich um Rundungsfehler zu vermeiden
					//Beispielsweise führt Math.round(0.95 / 0.1) zum falschen Ergebnis 9
					//aber Math.round(0.95 * (1 / 0.1)) führt zum richtigen Ergebnis 10

					if(conversionFactor < 1.0) {
						unscaledValue = Math.round((double)value * (1 / conversionFactor));
					}
					else if(conversionFactor > 1.0) {
						unscaledValue = Math.round((double)value / conversionFactor);
					}
					else {
						unscaledValue = value;
					}
					setUnscaledLongValue(unscaledValue);
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}

			public void set(double value) {
				try {
					IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
					IntegerValueRange range = type.getRange();
					if(range == null) {
						throw new IllegalArgumentException("kein Zahlenbereich definiert");
					}
					long unscaledValue;
					double conversionFactor = range.getConversionFactor();
					//Folgende Fallunterscheidung ist erforderlich um Rundungsfehler zu vermeiden
					//Beispielsweise führt Math.round(0.95 / 0.1) zum falschen Ergebnis 9
					//aber Math.round(0.95 * (1 / 0.1)) führt zum richtigen Ergebnis 10

					if(conversionFactor < 1.0) {
						unscaledValue = Math.round(value * (1 / conversionFactor));
					}
					else if(conversionFactor > 1.0) {
						unscaledValue = Math.round(value / conversionFactor);
					}
					else {
						unscaledValue = Math.round(value);
					}
					setUnscaledLongValue(unscaledValue);
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}
		}

		private class ScaledByteValueAdapter extends ScaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public long getUnscaledLongValue() {
				return ((Byte)((DataValue)_attributeValue.getValue()).getValue()).byteValue();
			}

			protected void setUnscaledLongValue(long value) {
				if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) throw new IllegalArgumentException("Wert " + value + " nicht in einem Byte darstellbar");
				_attributeValue.setValue(new ByteAttribute((byte)value));
			}

			public byte byteValue() {
				return (byte)longValue();
			}
		}

		private class ScaledShortValueAdapter extends ScaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public long getUnscaledLongValue() {
				return ((Short)((DataValue)_attributeValue.getValue()).getValue()).shortValue();
			}

			protected void setUnscaledLongValue(long value) {
				if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
					throw new IllegalArgumentException("Wert " + value + " nicht in einem Short darstellbar");
				}
				_attributeValue.setValue(new ShortAttribute((short)value));
			}

			public short shortValue() {
				return (short)longValue();
			}
		}

		private class ScaledIntegerValueAdapter extends ScaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public long getUnscaledLongValue() {
				return ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
			}

			protected void setUnscaledLongValue(long value) {
				if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("Wert " + value + " nicht in einem Integer darstellbar");
				}
				_attributeValue.setValue(new IntegerAttribute((int)value));
			}

			public int intValue() {
				return (int)longValue();
			}
		}

		private class ScaledLongValueAdapter extends ScaledValueAdapter {

			protected String getName() {
				return AttributeValueAdapter.this.getName();
			}

			protected AttributeType getAttributeType() {
				return AttributeValueAdapter.this.getAttributeType();
			}

			public long getUnscaledLongValue() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue();
			}

			protected void setUnscaledLongValue(long value) {
				_attributeValue.setValue(new LongAttribute(value));
			}
		}

		private class DoubleValueAdapter extends NumberValueAdapter {

			public double doubleValue() {
				return ((Double)((DataValue)_attributeValue.getValue()).getValue()).doubleValue();
			}

			public String getSuffixText() {
				try {
					DoubleAttributeType type = (DoubleAttributeType)getAttributeType();
					String unit = type.getUnit();
					if(unit != null && !unit.equals("")) return unit;
					return "";
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

			public String getValueText() {
				try {
					return _doubleNumberFormat.format(doubleValue());
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

//			public void setText(String text) {
//				Number number;
//				ParsePosition parsePosition = new ParsePosition(0);
//				text= text.replace('.', ',');
//				synchronized(_doubleNumberFormat) {
//					number= _doubleNumberFormat.parse(text, parsePosition);
//				}
//				if(number==null) throw new IllegalArgumentException("keine Zahl");
//				set(number.doubleValue());
//			}

			public void set(double value) {
				_attributeValue.setValue(new DoubleAttribute(value));
			}
		}

		private class FloatValueAdapter extends DoubleValueAdapter {

			public float floatValue() {
				return ((Float)((DataValue)_attributeValue.getValue()).getValue()).floatValue();
			}

			public double doubleValue() {
				return (double)floatValue();
			}

			public void set(float value) {
				_attributeValue.setValue(new FloatAttribute(value));
			}

			public void set(double value) {
				_attributeValue.setValue(new FloatAttribute((float)value));
			}
		}

		private class TextValueAdapter extends AbstractData.TextValue {

			public String getValueText() {
				try {
					return (String)((DataValue)_attributeValue.getValue()).getValue();
				}
				catch(Exception e) {
					return formatError(e);
				}
			}

			public void setText(String text) {
				_attributeValue.setValue(new StringAttribute(text));
			}
		}

		private class ReferenceValueAdapter extends AbstractData.ReferenceValue {

			protected DataModel getDataModel() {
				return _attributeValue.getAttribute().getDataModel();
			}

			boolean tryToStorePid(final String objectPid) {
				final ReferenceAttributeType att = ((ReferenceAttributeType)getAttributeType());
				if(att.getReferenceType() == ReferenceType.ASSOCIATION) {
					_attributeValue.setValue(new LongAndStringAttribute(0, objectPid));
					return true;
				}
				else {
					return false;
				}
			}

			String getStoredPid() {
				final Object dataValue = _attributeValue.getValue();
				if(dataValue instanceof LongAndStringAttribute) {
					LongAndStringAttribute longAndString = (LongAndStringAttribute)dataValue;
					return longAndString.getString();
				}
				return "";
			}

			public long getId() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue();
			}

			public void setSystemObject(SystemObject object) {
				long id;
				if(object == null) {
					id = 0;
				}
				else {
					checkObject(object, _attributeValue.getAttribute());
					id = object.getId();
				}
				_attributeValue.setValue(new LongAttribute(id));
			}
		}

		private class AbsoluteSecondsTimeValueAdapter extends AbstractData.AbsoluteSecondsTimeValue {

			public long getMillis() {
				//if(((TimeAttributeType)getAttributeType()).getAccuracy() == TimeAttributeType.SECONDS) {
				long seconds = ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
				//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
				//Das im Java-int-Typ enthaltene Vorzeichenbit darf dazu allerdings nicht als Vorzeichenbit
				//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden. Bei der obigen Zuweisung
				//auf den Java-long-Typ wird eine automatische Vorzeichenerweiterung gemacht, die mit
				//der folgenden Maskierung wieder rückgängig gemacht wird. Dadurch wird der übertragene
				//32-Bit Wert als Vorzeichenlose Zahl im Bereich 0 bis 4294967295 interpretiert.
				seconds &= 0xffffffffL;	//auch das L am Ende der Konstanten ist notwendig!
				return seconds * 1000;
			}

			public long getSeconds() {
				long seconds = ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
				//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
				//Das im Java-int-Typ enthaltene Vorzeichenbit darf dazu allerdings nicht als Vorzeichenbit
				//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden. Bei der obigen Zuweisung
				//auf den Java-long-Typ wird eine automatische Vorzeichenerweiterung gemacht, die mit
				//der folgenden Maskierung wieder rückgängig gemacht wird. Dadurch wird der übertragene
				//32-Bit Wert als Vorzeichenlose Zahl im Bereich 0 bis 4294967295 interpretiert.
				seconds &= 0xffffffffL;	//auch das L am Ende der Konstanten ist notwendig!
				return seconds;
			}

			public void setMillis(long milliSeconds) {
				if(milliSeconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
				long seconds = milliSeconds / 1000;
				if(seconds > 0xffffffffL) {
					throw new RuntimeException(
							"Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten sekundengenauen Zeitstempeln (07.02.2106 07:28:15)"
					);
				}
				int intSeconds = (int)seconds;
				//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
				//Das im Java-int-Typ enthaltene Vorzeichenbit muss dazu allerdings nicht als Vorzeichenbit
				//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden.
				_attributeValue.setValue(new IntegerAttribute(intSeconds));
			}

			public void setSeconds(long seconds) {
				if(seconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
				if(seconds > 0xffffffffL) {
					throw new RuntimeException(
							"Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten sekundengenauen Zeitstempeln (07.02.2106 07:28:15)"
					);
				}
				int intSeconds = (int)seconds;
				//_attributeValue.setValue(new IntegerAttribute(intSeconds));
				//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
				//Das im Java-int-Typ enthaltene Vorzeichenbit muss dazu allerdings nicht als Vorzeichenbit
				//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden.
				_attributeValue.setValue(new IntegerAttribute(intSeconds));
			}
		}

		private class AbsoluteMillisTimeValueAdapter extends AbstractData.AbsoluteMillisTimeValue {

			public long getMillis() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue();
			}

			public long getSeconds() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue() / 1000;
			}

			public void setMillis(long milliSeconds) {
				if(milliSeconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
				_attributeValue.setValue(new LongAttribute(milliSeconds));
			}

			public void setSeconds(long seconds) {
				if(seconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
				//Sekundenwert ist zu groß, wenn er größer als Long.MAX_VALUE/1000 (==9223372036854775L) ist
				if(seconds > 9223372036854775L) {
					throw new RuntimeException("Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten Zeitstempeln");
				}
				long milliSeconds = seconds * 1000;
				_attributeValue.setValue(new LongAttribute(milliSeconds));
			}
		}

		private class RelativeSecondsTimeValueAdapter extends AbstractData.RelativeTimeValue {

			public long getMillis() {
				long seconds = ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
				return seconds * 1000;
			}

			public long getSeconds() {
				long seconds = ((Integer)((DataValue)_attributeValue.getValue()).getValue()).intValue();
				return seconds;
			}

			public void setMillis(long milliSeconds) {
				long seconds = milliSeconds / 1000;
				if(seconds > Integer.MAX_VALUE) throw new RuntimeException("Zeitangabe größer als in einem relativen sekundengenauen Zeitstempel darstellbar");
				if(seconds < Integer.MIN_VALUE) throw new RuntimeException("Zeitangabe kleiner als in einem relativen sekundengenauen Zeitstempel darstellbar");
				int intSeconds = (int)seconds;
				_attributeValue.setValue(new IntegerAttribute(intSeconds));
			}

			public void setSeconds(long seconds) {
				if(seconds > Integer.MAX_VALUE) throw new RuntimeException("Zeitangabe größer als in einem relativen sekundengenauen Zeitstempel darstellbar");
				if(seconds < Integer.MIN_VALUE) throw new RuntimeException("Zeitangabe kleiner als in einem relativen sekundengenauen Zeitstempel darstellbar");
				int intSeconds = (int)seconds;
				_attributeValue.setValue(new IntegerAttribute(intSeconds));
			}
		}

		private class RelativeMillisTimeValueAdapter extends AbstractData.RelativeTimeValue {

			public long getMillis() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue();
			}

			public long getSeconds() {
				return ((Long)((DataValue)_attributeValue.getValue()).getValue()).longValue() / 1000;
			}

			public void setMillis(long milliSeconds) {
				_attributeValue.setValue(new LongAttribute(milliSeconds));
			}

			public void setSeconds(long seconds) {
				if(seconds > Long.MAX_VALUE / 1000) {
					throw new RuntimeException("Zeitangabe größer als in einem relativen millisekundengenauen Zeitstempel darstellbar");
				}
				if(seconds < Long.MIN_VALUE / 1000) {
					throw new RuntimeException("Zeitangabe kleiner als in einem relativen millisekundengenauen Zeitstempel darstellbar");
				}
				long milliSeconds = seconds * 1000;
				_attributeValue.setValue(new LongAttribute(milliSeconds));
			}
		}
	}

	public static String formatError(final Exception e) {
		String message = e.getMessage();
		if(message == null) {
			
			_debug.warning("Fehler beim Darstellen eines Datensatzes", e);
			message = e.getClass().getName();
		}
		return "<<" + message + ">>";
	}

	/** Subklasse von <code>AttributeBaseValueDataFactory</code>, abgeleitet von <code>AbstractData.ArrayData</code>. */
	private static class AttributeArrayAdapter extends AbstractData.ArrayData {

		private final AttributeBaseValue _attributeValue;

		private Data.Array _arrayAdapter;

		public AttributeArrayAdapter(AttributeBaseValue attributeValue) {
			_attributeValue = attributeValue;
			_arrayAdapter = null;
		}

		public String getName() {
			return _attributeValue.getName();
		}

		public boolean isCountVariable() {
			try {
				return _attributeValue.getAttribute().isCountVariable();
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public boolean isCountLimited() {
			try {
				return _attributeValue.getAttribute().isCountLimited();
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public int getMaxCount() {
			try {
				return _attributeValue.getAttribute().getMaxCount();
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public AttributeType getAttributeType() {
			try {
				return _attributeValue.getAttribute().getAttributeType();
			}
			catch(Exception e) {
				return null;
			}
		}

		private long getDefaultValue() throws ConfigurationException {

			IntegerAttributeType integerAttributeType = (IntegerAttributeType)getAttributeType();
			IntegerValueRange range = integerAttributeType.getRange();
			if(range != null) return range.getMinimum();
			List<IntegerValueState> states = integerAttributeType.getStates();
			if(states.size() > 0) return (states.get(0)).getValue();
			return 0;
		}

		public Iterator<Data> iterator() {
			return new AttributeArrayIterator();
		}

		public Data.Array asArray() {
			if(_arrayAdapter == null) _arrayAdapter = createArrayAdapter();
			return _arrayAdapter;
		}

		private Data.Array createArrayAdapter() {
			try {
				AttributeType type = getAttributeType();
				if(type instanceof IntegerAttributeType) {
					switch(((IntegerAttributeType)type).getByteCount()) {
						case IntegerAttributeType.BYTE:
							return new ByteArrayAdapter();
						case IntegerAttributeType.SHORT:
							return new ShortArrayAdapter();
						case IntegerAttributeType.INT:
							return new IntArrayAdapter();
						case IntegerAttributeType.LONG:
							return new LongArrayAdapter();
						default:
							throw new IllegalStateException("ungültige Anzahl Bytes im Attributtyp " + type);
					}
				}
				else if(type instanceof DoubleAttributeType) {
					switch(((DoubleAttributeType)type).getAccuracy()) {
						case DoubleAttributeType.FLOAT:
							return new FloatArrayAdapter();
						case DoubleAttributeType.DOUBLE:
							return new DoubleArrayAdapter();
						default:
							throw new IllegalStateException("ungültige Genauigkeit im Attributtyp " + type);
					}
				}
				else if(type instanceof ReferenceAttributeType) {
					return new ReferenceArrayAdapter();
				}
				else if(type instanceof StringAttributeType) {
					return new TextArrayAdapter();
				}
				else if(type instanceof TimeAttributeType) {
					if(((TimeAttributeType)type).getAccuracy() == TimeAttributeType.SECONDS) {
						return new SecondsTimeArrayAdapter();
					}
					else {
						return new MillisTimeArrayAdapter();
					}
				}
				else if(type instanceof AttributeListDefinition) {
					return new AttributeListArrayAdapter();
				}
				else {
					throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht als Array dargestellt werden");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		private class AttributeArrayIterator implements Iterator<Data> {

			int _position = 0;

			public boolean hasNext() {
				return _position < asArray().getLength();
			}

			public Data next() {
				return asArray().getItem(_position++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

		private abstract class AttributeArray extends AbstractData.Array implements ArrayRelaxedRangeCheckSupport {

			public String toString() {
				return AttributeArrayAdapter.this.toString();
			}

			public boolean isCountVariable() {
				return AttributeArrayAdapter.this.isCountVariable();
			}

			public boolean isCountLimited() {
				return AttributeArrayAdapter.this.isCountLimited();
			}

			public int getMaxCount() {
				return AttributeArrayAdapter.this.getMaxCount();
			}

			abstract protected void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck);

			public void setLength(int newLength) {
				setLength(newLength, true, false);
			}

			public void setLengthUninitialized(int newLength) {
				setLength(newLength, false, false);
			}

			@Override
			public void setLengthRelaxedRangeCheck(int newLength) {
				setLength(newLength, false, true);
			}

			protected void rangeCheck(final int newLength, final boolean relaxedRangeCheck) {
				if(newLength < 0 ||
						(!isCountVariable() && (newLength != getMaxCount())) ||
						(isCountLimited() && (newLength > getMaxCount()) && !relaxedRangeCheck)) {
					throw new IllegalArgumentException(
							"Arraygröße " + newLength + " ist beim Attribut " + getName() + " vom Typ " + getAttributeType().getPid() + " nicht erlaubt"
					);
				}
			}
		}

		private abstract class ArrayItemData extends AbstractData.PlainData {

			protected final int _itemIndex;

			ArrayItemData(int itemIndex) {
				_itemIndex = itemIndex;
			}

			public AttributeType getAttributeType() {
				return AttributeArrayAdapter.this.getAttributeType();
			}

			public String getName() {
				return String.valueOf(_itemIndex);
			}

			public boolean isDefined() {
				return AttributeBaseValueDataFactory.isDefined(_attributeValue.getAttribute(), this);
			}

			public void setToDefault() {
				AttributeBaseValueDataFactory.setToDefault(_attributeValue.getAttribute(), this);
			}
		}

		private class AttributeListArrayAdapter extends AttributeArray {

			AttributeListValue _listValue;
			//AttributeListAttribute[] _attributeLists;

			public AttributeListArrayAdapter() {
				_listValue = (AttributeListValue)_attributeValue;
				//AttributeListArrayAttribute attribute = (AttributeListArrayAttribute)_attributeValue.getValue();

//				if(attribute == null)
//					_attributeLists = new AttributeListAttribute[0];
//				else
//					_attributeLists = (AttributeListAttribute[])attribute.getValue();
			}

			public Data getItem(int itemIndex) {
				//return new AttributeListArrayItemData(itemIndex);
				return new AttributeListValueAdapter(String.valueOf(itemIndex), _listValue, itemIndex);

//				return new AttributeListValueAdapter(String.valueOf(itemIndex), _attributeLists[itemIndex]);
			}

			public int getLength() {
				return _listValue.getElementsCount();
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				try {
					// Speichert die Anzahl alter Werte
					final int numberOfOldValues = _listValue.getElementsCount();
					_listValue.setElementsCount(newLength);

					if(initializeElements) {
						// Bei den neuen Werten den Default setzen
						for(int nr = numberOfOldValues; nr < _listValue.getElementsCount(); ++nr) {
							getItem(nr).setToDefault();
						}
					}
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}


			private class AttributeListArrayItemData extends AbstractData.ListData {

				final int _itemIndex;

				private List _dataList = null;

				AttributeListArrayItemData(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				public String getName() {
					return String.valueOf(_itemIndex);
				}


				public Iterator iterator() {
					return _dataList.iterator();
				}
			}
		}

		private class ReferenceArrayAdapter extends AttributeArray implements Data.ReferenceArray {

			long _ids[];
			HashMap<Integer, String> _pids;

			public ReferenceArrayAdapter() {
				LongArrayAttribute attribute = (LongArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_ids = new long[0];
				}
				else {
					_ids = (long[])attribute.getValue();
				}
			}

			public Data.ReferenceArray asReferenceArray() {
				return this;
			}

			public Data getItem(int itemIndex) {
				return new ReferenceArrayItemData(itemIndex);
			}

			public int getLength() {
				return _ids.length;
			}

			public SystemObject getSystemObject(int itemIndex) {
				return getReferenceValue(itemIndex).getSystemObject();
			}

			public void set(final SystemObject[] systemObjects) {
				setLengthUninitialized(systemObjects.length);
				for(int i = 0; i < systemObjects.length; ++i) {
					getReferenceValue(i).setSystemObject(systemObjects[i]);
				}
			}

			public void set(final String... systemObjectPids) {
				setLengthUninitialized(systemObjectPids.length);
				for(int i = 0; i < systemObjectPids.length; ++i) {
					getReferenceValue(i).setSystemObjectPid(systemObjectPids[i]);
				}
			}

			public void set(final ObjectLookup dataModel, final String... systemObjectPids) {
				setLengthUninitialized(systemObjectPids.length);
				for(int i = 0; i < systemObjectPids.length; ++i) {
					getReferenceValue(i).setSystemObjectPid(systemObjectPids[i], dataModel);
				}
			}

			public SystemObject[] getSystemObjectArray() {
				int length = getLength();
				SystemObject[] results = new SystemObject[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getSystemObject(i);
				}
				return results;
			}

			public Data.ReferenceValue getReferenceValue(int itemIndex) {
				return getItem(itemIndex).asReferenceValue();
			}

			public Data.ReferenceValue[] getReferenceValues() {
				int length = getLength();
				Data.ReferenceValue[] results = new Data.ReferenceValue[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getReferenceValue(i);
				}
				return results;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _ids.length == newLength) return;
				long[] newIds = new long[newLength];
				_attributeValue.setValue(new LongArrayAttribute(newIds));
				int minLength = Math.min(newLength, _ids.length);
				for(int i = 0; i < minLength; ++i) {
					newIds[i] = _ids[i];
				}
				_ids = newIds;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _ids.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}


			private class ReferenceArrayItemData extends ArrayItemData {

				ReferenceArrayItemData(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new ReferenceArrayItemValue(_itemIndex);
				}

				public Data.ReferenceValue asReferenceValue() {
					return new ReferenceArrayItemValue(_itemIndex);
				}
			}

			private class ReferenceArrayItemValue extends AbstractData.ReferenceValue {

				private final int _itemIndex;

				ReferenceArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected DataModel getDataModel() {
					return _attributeValue.getAttribute().getDataModel();
				}

				public long getId() {
					return _ids[_itemIndex];
				}

				boolean tryToStorePid(final String objectPid) {
					final ReferenceAttributeType att = ((ReferenceAttributeType)getAttributeType());
					if(att.getReferenceType() == ReferenceType.ASSOCIATION) {
						if(_pids == null) _pids = new HashMap<Integer, String>();
						_pids.put(_itemIndex, objectPid);
						return true;
					}
					else {
						return false;
					}
				}

				String getStoredPid() {
					if(_pids != null) {
						String pid = _pids.get(_itemIndex);
						if(pid != null) return pid;
					}
					return "";
				}

				public void setSystemObject(SystemObject object) {
					long id;
					if(object == null) {
						id = 0;
					}
					else {
						checkObject(object, _attributeValue.getAttribute());
						id = object.getId();
					}
					_ids[_itemIndex] = id;
					if(_pids != null) _pids.remove(_itemIndex);
				}
			}
		}

		private abstract class TimeArrayAdapter extends AttributeArray implements Data.TimeArray {

			public Data.TimeArray asTimeArray() {
				return this;
			}

			public long getSeconds(int itemIndex) {
				return getTimeValue(itemIndex).getSeconds();
			}

			public long getMillis(int itemIndex) {
				return getTimeValue(itemIndex).getMillis();
			}

			public void setMillis(final long[] millis) {
				setLengthUninitialized(millis.length);
				for(int i = 0; i < millis.length; ++i) {
					getTimeValue(i).setMillis(millis[i]);
				}
			}

			public void setSeconds(final long[] seconds) {
				setLengthUninitialized(seconds.length);
				for(int i = 0; i < seconds.length; ++i) {
					getTimeValue(i).setSeconds(seconds[i]);
				}
			}

			public long[] getSecondsArray() {
				int length = getLength();
				long[] results = new long[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getSeconds(i);
				}
				return results;
			}

			public long[] getMillisArray() {
				int length = getLength();
				long[] results = new long[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getMillis(i);
				}
				return results;
			}


			public Data.TimeValue getTimeValue(int itemIndex) {
				return getItem(itemIndex).asTimeValue();
			}

			public Data.TimeValue[] getTimeValues() {
				int length = getLength();
				Data.TimeValue[] results = new Data.TimeValue[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getTimeValue(i);
				}
				return results;
			}
		}

		private class MillisTimeArrayAdapter extends TimeArrayAdapter {

			long _millisArray[];

			public MillisTimeArrayAdapter() {
				LongArrayAttribute attribute = (LongArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_millisArray = new long[0];
				}
				else {
					_millisArray = (long[])attribute.getValue();
				}
			}

			public Data getItem(int itemIndex) {
				return new TimeArrayItemData(itemIndex);
			}

			public int getLength() {
				return _millisArray.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _millisArray.length == newLength) return;
				long[] newTimes = new long[newLength];
				_attributeValue.setValue(new LongArrayAttribute(newTimes));
				int minLength = Math.min(newLength, _millisArray.length);
				for(int i = 0; i < minLength; ++i) {
					newTimes[i] = _millisArray[i];
				}
				_millisArray = newTimes;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _millisArray.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class TimeArrayItemData extends ArrayItemData {

				TimeArrayItemData(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return asTimeValue();
				}

				public Data.TimeValue asTimeValue() {
					try {
						if(((TimeAttributeType)getAttributeType()).isRelative()) {
							return new RelativeTimeArrayItemValue(_itemIndex);
						}
						else {
							return new AbsoluteTimeArrayItemValue(_itemIndex);
						}
					}
					catch(ConfigurationException e) {
						throw new RuntimeException(e);
					}
				}
			}

			private class RelativeTimeArrayItemValue extends AbstractData.RelativeTimeValue {

				private final int _itemIndex;

				RelativeTimeArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public long getMillis() {
					return _millisArray[_itemIndex];
				}

				public void setMillis(long milliSeconds) {
					_millisArray[_itemIndex] = milliSeconds;
				}

				public long getSeconds() {
					return _millisArray[_itemIndex] / 1000;
				}

				public void setSeconds(long seconds) {
					if(seconds > Long.MAX_VALUE / 1000) {
						throw new RuntimeException("Zeitangabe größer als in einem relativen millisekundengenauen Zeitstempel darstellbar");
					}
					if(seconds < Long.MIN_VALUE / 1000) {
						throw new RuntimeException("Zeitangabe kleiner als in einem relativen millisekundengenauen Zeitstempel darstellbar");
					}
					_millisArray[_itemIndex] = seconds * 1000;
				}
			}

			private class AbsoluteTimeArrayItemValue extends AbstractData.AbsoluteMillisTimeValue {

				private final int _itemIndex;

				AbsoluteTimeArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public long getMillis() {
					return _millisArray[_itemIndex];
				}

				public void setMillis(long milliSeconds) {
					_millisArray[_itemIndex] = milliSeconds;
				}

				public long getSeconds() {
					return _millisArray[_itemIndex] / 1000;
				}

				public void setSeconds(long seconds) {
					if(seconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
					//Sekundenwert ist zu groß, wenn er größer als Long.MAX_VALUE/1000 (==9223372036854775L) ist
					if(seconds > 9223372036854775L) {
						throw new RuntimeException("Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten Zeitstempeln");
					}
					_millisArray[_itemIndex] = seconds * 1000;
				}
			}
		}

		private class SecondsTimeArrayAdapter extends TimeArrayAdapter {

			int _secondsArray[];

			public SecondsTimeArrayAdapter() {
				IntegerArrayAttribute attribute = (IntegerArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_secondsArray = new int[0];
				}
				else {
					_secondsArray = (int[])attribute.getValue();
				}
			}

			public Data getItem(int itemIndex) {
				return new TimeArrayItemData(itemIndex);
			}

			public int getLength() {
				return _secondsArray.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _secondsArray.length == newLength) return;
				int[] newSecondsArray = new int[newLength];
				_attributeValue.setValue(new IntegerArrayAttribute(newSecondsArray));
				int minLength = Math.min(newLength, _secondsArray.length);
				for(int i = 0; i < minLength; ++i) {
					newSecondsArray[i] = _secondsArray[i];
				}
				_secondsArray = newSecondsArray;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _secondsArray.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}


			private class TimeArrayItemData extends ArrayItemData {

				TimeArrayItemData(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return asTimeValue();
				}

				public Data.TimeValue asTimeValue() {
					try {
						if(((TimeAttributeType)getAttributeType()).isRelative()) {
							return new RelativeTimeArrayItemValue(_itemIndex);
						}
						else {
							return new AbsoluteTimeArrayItemValue(_itemIndex);
						}
					}
					catch(ConfigurationException e) {
						throw new RuntimeException(e);
					}
				}
			}

			private class RelativeTimeArrayItemValue extends AbstractData.RelativeTimeValue {

				private final int _itemIndex;

				RelativeTimeArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public long getMillis() {
					return getSeconds() * 1000;
				}

				public long getSeconds() {
					long seconds = _secondsArray[_itemIndex];
					return seconds;
				}

				public void setMillis(long milliSeconds) {
					long seconds = milliSeconds / 1000;
					if(seconds > Integer.MAX_VALUE) {
						throw new RuntimeException("Zeitangabe größer als in einem relativen sekundengenauen Zeitstempel darstellbar");
					}
					if(seconds < Integer.MIN_VALUE) {
						throw new RuntimeException("Zeitangabe kleiner als in einem relativen sekundengenauen Zeitstempel darstellbar");
					}
					int intSeconds = (int)seconds;
					_secondsArray[_itemIndex] = intSeconds;
				}

				public void setSeconds(long seconds) {
					if(seconds > Integer.MAX_VALUE) {
						throw new RuntimeException("Zeitangabe größer als in einem relativen sekundengenauen Zeitstempel darstellbar");
					}
					if(seconds < Integer.MIN_VALUE) {
						throw new RuntimeException("Zeitangabe kleiner als in einem relativen sekundengenauen Zeitstempel darstellbar");
					}
					int intSeconds = (int)seconds;
					_secondsArray[_itemIndex] = intSeconds;
				}
			}

			private class AbsoluteTimeArrayItemValue extends AbstractData.AbsoluteSecondsTimeValue {

				private final int _itemIndex;

				AbsoluteTimeArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public long getMillis() {
					return getSeconds() * 1000;
				}

				public long getSeconds() {
					long seconds = _secondsArray[_itemIndex];
					//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
					//Das im Java-int-Typ enthaltene Vorzeichenbit darf dazu allerdings nicht als Vorzeichenbit
					//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden. Bei der obigen Zuweisung
					//auf den Java-long-Typ wird eine automatische Vorzeichenerweiterung gemacht, die mit
					//der folgenden Maskierung wieder rückgängig gemacht wird. Dadurch wird der übertragene
					//32-Bit Wert als Vorzeichenlose Zahl im Bereich 0 bis 4294967295 interpretiert.
					seconds &= 0xffffffffL;	//auch das L am Ende der Konstanten ist notwendig!
					return seconds;
				}

				public void setMillis(long milliSeconds) {
					if(milliSeconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
					long seconds = milliSeconds / 1000;
					if(seconds > 0xffffffffL) {
						throw new RuntimeException(
								"Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten sekundengenauen Zeitstempeln (07.02.2106 07:28:15)"
						);
					}
					int intSeconds = (int)seconds;
					//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
					//Das im Java-int-Typ enthaltene Vorzeichenbit muss dazu allerdings nicht als Vorzeichenbit
					//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden.
					_secondsArray[_itemIndex] = intSeconds;
				}

				public void setSeconds(long seconds) {
					if(seconds < 0) throw new RuntimeException("Negative Zeitangaben sind bei absoluten Zeitstempeln nicht erlaubt");
					if(seconds > 0xffffffffL) {
						throw new RuntimeException(
								"Zeitangabe liegt nach dem größten darstellbaren Wert von absoluten sekundengenauen Zeitstempeln (07.02.2106 07:28:15)"
						);
					}
					int intSeconds = (int)seconds;
					//_attributeValue.setValue(new IntegerAttribute(intSeconds));
					//Ein vorzeichenloser 32-Bit-Wert kann Sekunden bis zum 07.02.2106 07:28:15 darstellen.
					//Das im Java-int-Typ enthaltene Vorzeichenbit muss dazu allerdings nicht als Vorzeichenbit
					//sondern als Bit mit der Wertigkeit 2^31 interpretiert werden.
					_secondsArray[_itemIndex] = intSeconds;
				}
			}
		}

		private class TextArrayAdapter extends AttributeArray implements Data.TextArray {

			String[] _strings;

			public TextArrayAdapter() {
				StringArrayAttribute attribute = (StringArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_strings = new String[0];
				}
				else {
					_strings = (String[])attribute.getValue();
				}
			}

			public Data.TextArray asTextArray() {
				return this;
			}

			public Data getItem(int itemIndex) {
				return new TextArrayItemData(itemIndex);
			}

			public int getLength() {
				return _strings.length;
			}

			public String getText(int itemIndex) {
				return getTextValue(itemIndex).getText();
			}

			public String[] getTextArray() {
				int length = getLength();
				String[] results = new String[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getText(i);
				}
				return results;
			}

			public Data.TextValue getTextValue(int itemIndex) {
				return getItem(itemIndex).asTextValue();
			}

			public Data.TextValue[] getTextValues() {
				int length = getLength();
				Data.TextValue[] results = new Data.TextValue[length];
				for(int i = 0; i < length; ++i) {
					results[i] = getTextValue(i);
				}
				return results;
			}

			public void set(final String[] strings) {
				setLengthUninitialized(strings.length);
				for(int i = 0; i < strings.length; ++i) {
					getTextValue(i).setText(strings[i]);
				}
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _strings.length == newLength) return;
				String[] newStrings = new String[newLength];
				_attributeValue.setValue(new StringArrayAttribute(newStrings));
				int minLength = Math.min(newLength, _strings.length);
				for(int i = 0; i < minLength; ++i) {
					newStrings[i] = _strings[i];
				}
				_strings = newStrings;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _strings.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}


			private class TextArrayItemData extends ArrayItemData {

				TextArrayItemData(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new TextArrayItemValue(_itemIndex);
				}
			}

			private class TextArrayItemValue extends AbstractData.TextValue {

				private final int _itemIndex;

				TextArrayItemValue(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public String getValueText() {
					return _strings[_itemIndex];
				}

				public void setText(String text) {
					_strings[_itemIndex] = text;
				}
			}
		}

		private class DoubleArrayAdapter extends AttributeArray {

			double _values[];

			Data.NumberArray _unscaledView = null;

			public DoubleArrayAdapter() {
				DoubleArrayAttribute attribute = (DoubleArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new double[0];
				}
				else {
					_values = (double[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				return asUnscaledArray();
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return DoubleArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					DoubleArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					DoubleArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				double[] newValues = new double[newLength];
				_attributeValue.setValue(new DoubleArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new UnscaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AbstractData.NumberValue {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public double doubleValue() {
					return _values[_itemIndex];
				}

				public String getSuffixText() {
					try {
						DoubleAttributeType type = (DoubleAttributeType)getAttributeType();
						String unit = type.getUnit();
						if(unit != null && !unit.equals("")) return unit;
						return "";
					}
					catch(Exception e) {
						return formatError(e);
					}
				}

				public String getValueText() {
					try {
						return _doubleNumberFormat.format(doubleValue());
					}
					catch(Exception e) {
						return formatError(e);
					}
				}

				public void set(double value) {
					_values[_itemIndex] = value;
				}
			}
		}

		private class FloatArrayAdapter extends AttributeArray {

			float _values[];

			Data.NumberArray _unscaledView = null;

			public FloatArrayAdapter() {
				FloatArrayAttribute attribute = (FloatArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new float[0];
				}
				else {
					_values = (float[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				return asUnscaledArray();
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return FloatArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					FloatArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					FloatArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				float[] newValues = new float[newLength];
				_attributeValue.setValue(new FloatArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new UnscaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AbstractData.NumberValue {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				public float floatValue() {
					return _values[_itemIndex];
				}

				public double doubleValue() {
					return (double)floatValue();
				}

				public void set(float value) {
					_values[_itemIndex] = value;
				}

				public void set(double value) {
					_values[_itemIndex] = (float)value;
				}

				public String getSuffixText() {
					try {
						DoubleAttributeType type = (DoubleAttributeType)getAttributeType();
						String unit = type.getUnit();
						if(unit != null && !unit.equals("")) return unit;
						return "";
					}
					catch(Exception e) {
						return formatError(e);
					}
				}

				public String getValueText() {
					try {
						return _doubleNumberFormat.format(floatValue());
					}
					catch(Exception e) {
						return formatError(e);
					}
				}
			}
		}

		private class LongArrayAdapter extends AttributeArray {

			long _values[];

			Data.NumberArray _unscaledView = null;

			Data.NumberArray _scaledView = null;

			public LongArrayAdapter() {
				LongArrayAttribute attribute = (LongArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new long[0];
				}
				else {
					_values = (long[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				if(_scaledView == null) _scaledView = new ScaledArrayView();
				return _scaledView;
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return LongArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					LongArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					LongArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			private class ScaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return LongArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					LongArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					LongArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new ScaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				long[] newValues = new long[newLength];
				Arrays.fill(newValues, (long)getDefaultValue());
				_attributeValue.setValue(new LongArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AttributeValueAdapter.UnscaledValueAdapter {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public long longValue() {
					return _values[_itemIndex];
				}

				public void set(long value) {
					_values[_itemIndex] = value;
				}
			}

			private class ScaledItemValueView extends AttributeValueAdapter.ScaledValueAdapter {

				private final int _itemIndex;

				ScaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public long getUnscaledLongValue() {
					return _values[_itemIndex];
				}

				protected void setUnscaledLongValue(long value) {
					_values[_itemIndex] = value;
				}
			}
		}

		private class IntArrayAdapter extends AttributeArray {

			int _values[];

			Data.NumberArray _unscaledView = null;

			Data.NumberArray _scaledView = null;

			public IntArrayAdapter() {
				IntegerArrayAttribute attribute = (IntegerArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new int[0];
				}
				else {
					_values = (int[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				if(_scaledView == null) _scaledView = new ScaledArrayView();
				return _scaledView;
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return IntArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					IntArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					IntArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			private class ScaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return IntArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					IntArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					IntArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new ScaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				int[] newValues = new int[newLength];
				Arrays.fill(newValues, (int)getDefaultValue());
				_attributeValue.setValue(new IntegerArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AttributeValueAdapter.UnscaledValueAdapter {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public int intValue() {
					return _values[_itemIndex];
				}

				public void set(long value) {
					if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Integer darstellbar");
					}
					_values[_itemIndex] = (int)value;
				}
			}

			private class ScaledItemValueView extends AttributeValueAdapter.ScaledValueAdapter {

				private final int _itemIndex;

				ScaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public long getUnscaledLongValue() {
					return _values[_itemIndex];
				}

				protected void setUnscaledLongValue(long value) {
					if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Integer darstellbar");
					}
					_values[_itemIndex] = (int)value;
				}

				public int intValue() {
					return (int)longValue();
				}
			}
		}

		private class ShortArrayAdapter extends AttributeArray {

			short _values[];

			Data.NumberArray _unscaledView = null;

			Data.NumberArray _scaledView = null;

			public ShortArrayAdapter() {
				ShortArrayAttribute attribute = (ShortArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new short[0];
				}
				else {
					_values = (short[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				if(_scaledView == null) _scaledView = new ScaledArrayView();
				return _scaledView;
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return ShortArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					ShortArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					ShortArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			private class ScaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return ShortArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					ShortArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					ShortArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new ScaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				short[] newValues = new short[newLength];
				Arrays.fill(newValues, (short)getDefaultValue());
				_attributeValue.setValue(new ShortArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AttributeValueAdapter.UnscaledValueAdapter {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public short shortValue() {
					return _values[_itemIndex];
				}

				public void set(long value) {
					if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Short darstellbar");
					}
					_values[_itemIndex] = (short)value;
				}
			}

			private class ScaledItemValueView extends AttributeValueAdapter.ScaledValueAdapter {

				private final int _itemIndex;

				ScaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public long getUnscaledLongValue() {
					return _values[_itemIndex];
				}

				protected void setUnscaledLongValue(long value) {
					if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Short darstellbar");
					}
					_values[_itemIndex] = (short)value;
				}

				public short shortValue() {
					return (short)longValue();
				}
			}
		}

		private class ByteArrayAdapter extends AttributeArray {

			byte _values[];

			Data.NumberArray _unscaledView = null;

			Data.NumberArray _scaledView = null;

			public ByteArrayAdapter() {
				ByteArrayAttribute attribute = (ByteArrayAttribute)_attributeValue.getValue();
				if(attribute == null) {
					_values = new byte[0];
				}
				else {
					_values = (byte[])attribute.getValue();
				}
			}

			public Data.NumberArray asScaledArray() {
				if(_scaledView == null) _scaledView = new ScaledArrayView();
				return _scaledView;
			}

			public Data.NumberArray asUnscaledArray() {
				if(_unscaledView == null) _unscaledView = new UnscaledArrayView();
				return _unscaledView;
			}

			private class UnscaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return ByteArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					ByteArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					ByteArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new UnscaledItemValueView(itemIndex);
				}
			}

			private class ScaledArrayView extends AbstractData.NumberArray {

				public String toString() {
					return AttributeArrayAdapter.this.toString();
				}

				public int getLength() {
					return ByteArrayAdapter.this.getLength();
				}

				public void setLength(int newLength) {
					ByteArrayAdapter.this.setLength(newLength);
				}

				public void setLengthUninitialized(int newLength) {
					ByteArrayAdapter.this.setLengthUninitialized(newLength);
				}

				public Data.NumberValue getValue(int itemIndex) {
					return new ScaledItemValueView(itemIndex);
				}
			}

			public Data getItem(int itemIndex) {
				return new ItemDataView(itemIndex);
			}

			public int getLength() {
				return _values.length;
			}

			public void setLength(int newLength, boolean initializeElements, boolean relaxedRangeCheck) {
				rangeCheck(newLength, relaxedRangeCheck);
				if(newLength != 0 && _values.length == newLength) return;
				byte[] newValues = new byte[newLength];
				Arrays.fill(newValues, (byte)getDefaultValue());
				_attributeValue.setValue(new ByteArrayAttribute(newValues));
				int minLength = Math.min(newLength, _values.length);
				for(int i = 0; i < minLength; ++i) {
					newValues[i] = _values[i];
				}
				_values = newValues;

				if(initializeElements) {
					// Alle neuen Attribute müssen auf den Default-Wert gesetzt werden
					for(int i = minLength; i < _values.length; ++i) {
						getItem(i).setToDefault();
					}
				}
			}

			private class ItemDataView extends ArrayItemData {

				ItemDataView(int itemIndex) {
					super(itemIndex);
				}

				public Data.TextValue asTextValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asScaledValue() {
					return new ScaledItemValueView(_itemIndex);
				}

				public Data.NumberValue asUnscaledValue() {
					return new UnscaledItemValueView(_itemIndex);
				}
			}

			private class UnscaledItemValueView extends AttributeValueAdapter.UnscaledValueAdapter {

				private final int _itemIndex;

				UnscaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}

				public byte byteValue() {
					return _values[_itemIndex];
				}

				public void set(long value) {
					if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Byte darstellbar");
					}
					_values[_itemIndex] = (byte)value;
				}
			}

			private class ScaledItemValueView extends AttributeValueAdapter.ScaledValueAdapter {

				private final int _itemIndex;

				ScaledItemValueView(int itemIndex) {
					_itemIndex = itemIndex;
				}

				protected AttributeType getAttributeType() {
					return AttributeArrayAdapter.this.getAttributeType();
				}

				protected String getName() {
					return AttributeArrayAdapter.this.getName() + "[" + _itemIndex + "]";
				}


				public long getUnscaledLongValue() {
					return _values[_itemIndex];
				}

				protected void setUnscaledLongValue(long value) {
					if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
						throw new IllegalArgumentException("Wert " + value + " nicht in einem Byte darstellbar");
					}
					_values[_itemIndex] = (byte)value;
				}

				public byte byteValue() {
					return (byte)longValue();
				}
			}
		}

/*		private abstract class NumberArrayAdapter extends AttributeArray implements Data.NumberArray {
			public float floatValue(int itemIndex) {
				return (float)doubleValue(itemIndex);
			}

			public double doubleValue(int itemIndex) {
				return (double)longValue(itemIndex);
			}

			public long longValue(int itemIndex) {
				return intValue(itemIndex);
			}

			public int intValue(int itemIndex) {
				return shortValue(itemIndex);
			}

			public short shortValue(int itemIndex) {
				return byteValue(itemIndex);
			}

		}
*/

/*		private class UnscaledByteArrayAdapter extends NumberArrayAdapter {
			public int getLength() {
				DataValue dv= (DataValue)_attributeValue.getValue();
				if(dv == null) return 0;
				return ((byte[])dv.getValue()).length;
			}

			public void setLength(int newLength) {
				DataValue oldDataValue= (DataValue)_attributeValue.getValue();
				DataValue newDataValue= new ByteArrayAttribute(new byte[newLength]);
				
				try {
					_attributeValue.setValue(newDataValue, true);
				}
				catch(ConfigurationException e) {
					throw new RuntimeException(e);
				}
			}

			public Data.NumberArray asUnscaledArray() {
				return this;
			}

			public byte byteValue(int itemIndex) {
				DataValue dv= (DataValue)_attributeValue.getValue();
				if(dv == null) return 0;
				return ((byte[])dv.getValue())[itemIndex];
			}

			public byte[] getByteArray() {
			}

			public double[] getDoubleArray() {
			}

			public float[] getFloatArray() {
			}

			public int[] getIntArray() {
			}

			public long[] getLongArray() {
			}

			public short[] getShortArray() {
			}

			public de.bsvrz.dav.daf.main.Data.NumberValue getValue(int itemIndex) {
			}

			public de.bsvrz.dav.daf.main.Data.NumberValue[] getValues() {
			}

		}
*/
	}
}
