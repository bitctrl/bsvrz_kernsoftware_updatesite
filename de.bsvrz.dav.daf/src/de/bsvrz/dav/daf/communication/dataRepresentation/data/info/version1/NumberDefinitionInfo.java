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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class NumberDefinitionInfo extends AttributeTypeDefinitionInfo {

	private final String _unit;
	private final String _unscaledUnit;
	private final Map _value2StateMap;

	protected static final NumberFormat _integerNumberFormat;
	protected static final NumberFormat _precisionTestNumberFormat;

	static {
		_integerNumberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
		_integerNumberFormat.setMinimumIntegerDigits(1);
		_integerNumberFormat.setMaximumIntegerDigits(999);
		_integerNumberFormat.setGroupingUsed(false);
		DecimalFormatSymbols symbols= new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		_precisionTestNumberFormat= new DecimalFormat("0.#", symbols);
		_precisionTestNumberFormat.setMaximumFractionDigits(999);
	}

	public NumberDefinitionInfo(IntegerAttributeType att) {
		super(att);
		try {
			String unit = null;
			String unscaledUnit = "";
			IntegerValueRange range = att.getRange();
			if(range != null) {
				unit = range.getUnit();
				double conversionFactor= range.getConversionFactor();
				if(conversionFactor != 1) {
					synchronized(_doubleNumberFormat) {
						unscaledUnit = "*" + _doubleNumberFormat.format(conversionFactor);
					}
				}
			}
			if(unit != null) {
				_unit = unit;
			}
			else {
				_unit = "";
			}

			if(unscaledUnit.equals("")) {
				_unscaledUnit = _unit;
			}
			else if(_unit.equals("")) {
				_unscaledUnit = unscaledUnit;
			}
			else {
				_unscaledUnit = unscaledUnit + " " + _unit;
			}

			List states= att.getStates();
			_value2StateMap = new HashMap(states.size());
			for(Iterator iterator = states.iterator(); iterator.hasNext();) {
				IntegerValueState state = (IntegerValueState)iterator.next();
				_value2StateMap.put(new Long(state.getValue()), state);
			}
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e); 
		}

	}

	public boolean isSizeFixed() {
		return true;
	}

	public boolean isNumberAttribute() {
		return true;
	}

	public boolean isScalableNumberAttribute() {
		return true;
	}

	public boolean isNumber(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) return false;
			long value= unscaledLongValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum()) return false;
			return true;
		}
		catch(ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isState(byte[] bytes, int offset) {
		return getState(bytes, offset) != null;
	}

	public String getUnscaledValueText(byte[] bytes, int offset) {
		try {
			return String.valueOf(unscaledLongValue(bytes, offset));
		}
		catch(Exception e) {
			return "<<" + e.getMessage() + ">>";
		}
	}

	public String getUnscaledSuffixText(byte[] bytes, int offset) {
		StringBuffer text= new StringBuffer();
		try {
			long value= unscaledLongValue(bytes, offset);
			IntegerAttributeType type = (IntegerAttributeType)getAttributeType();
			IntegerValueState state= getState(value);
			if(state!=null) return text.append("(").append(state.getName()).append(")").toString();
			IntegerValueRange range= type.getRange();
			if(range==null || value<range.getMinimum() || value>range.getMaximum()) {
				return text.append("<<ungültiger Wert>>").toString();
			}
			return _unscaledUnit;
		}
		catch(Exception e) {
			return text.append("<<" + e.getMessage() + ">>").toString();
		}
	}

	public String getValueText(byte[] bytes, int offset) {
		try {
			long value= unscaledLongValue(bytes, offset);
			IntegerAttributeType type=(IntegerAttributeType)getAttributeType();
			IntegerValueState state= getState(value);
			if(state!=null) return state.getName();
			IntegerValueRange range= type.getRange();
			if(range==null || value<range.getMinimum() || value>range.getMaximum()) {
				return " <<ungültiger Wert (" + value + ")>>";
			}
			double conversionFactor= range.getConversionFactor();
			if(conversionFactor == 1) return String.valueOf(value);
			else {
				int precision= 0;
				synchronized(_integerNumberFormat) {
					String formatted= _precisionTestNumberFormat.format(conversionFactor);
					int kommaPosition= formatted.lastIndexOf(',');
					if(kommaPosition>=0) precision= formatted.length() - kommaPosition - 1;
					_integerNumberFormat.setMinimumFractionDigits(precision);
					_integerNumberFormat.setMaximumFractionDigits(precision);
					return _integerNumberFormat.format(value * range.getConversionFactor());
				}
			}
		}
		catch(Exception e) {
			return "<<" + e.getMessage() + ">>";
		}
	}

	public String getSuffixText(byte[] bytes, int offset) {
		if(isState(bytes, offset)) return "";
		return _unit;
	}

	public IntegerValueState getState(byte[] bytes, int offset) {
		return getState(unscaledLongValue(bytes, offset));
	}

	protected IntegerValueState getState(long value) {
		return (IntegerValueState)_value2StateMap.get(new Long(value));
	}

	public byte byteValue(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat keinen definierten Wertebereich");
			if(range.getConversionFactor() != 1) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat Skalierungsfaktor ungleich 1");
			byte value= unscaledByteValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum())  throw new IllegalStateException("Attributtyp " + getAttributeType().getPid() + ": Wert " + value + " nicht im Bereich");
			return value;
		}
		catch(ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public short shortValue(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat keinen definierten Wertebereich");
			if(range.getConversionFactor() != 1) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat Skalierungsfaktor ungleich 1");
			short value= unscaledShortValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum())  throw new IllegalStateException("Attributtyp " + getAttributeType().getPid() + ": Wert " + value + " nicht im Bereich");
			return value;
		}
		catch(ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public int intValue(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat keinen definierten Wertebereich");
			if(range.getConversionFactor() != 1) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat Skalierungsfaktor ungleich 1");
			int value= unscaledIntValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum())  throw new IllegalStateException("Attributtyp " + getAttributeType().getPid() + ": Wert " + value + " nicht im Bereich");
			return value;
		}
		catch(ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public long longValue(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat keinen definierten Wertebereich");
			if(range.getConversionFactor() != 1) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat Skalierungsfaktor ungleich 1");
			long value= unscaledLongValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum())  throw new IllegalStateException("Attributtyp " + getAttributeType().getPid() + ": Wert " + value + " nicht im Bereich");
			return value;
		}
		catch(ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public float floatValue(byte[] bytes, int offset) {
		return (float)doubleValue(bytes, offset);
	}

	public double doubleValue(byte[] bytes, int offset) {
		try {
			IntegerValueRange range= ((IntegerAttributeType)getAttributeType()).getRange();
			if(range==null) throw new ArithmeticException("Attributtyp " + getAttributeType().getPid() + " hat keinen definierten Wertebereich");
			long value= unscaledLongValue(bytes, offset);
			if(value < range.getMinimum() || value > range.getMaximum())  throw new IllegalStateException("Attributtyp " + getAttributeType().getPid() + ": Wert " + value + " nicht im Bereich");
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

}
