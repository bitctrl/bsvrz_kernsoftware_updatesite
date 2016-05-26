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
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueState;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class DoubleDefinitionInfo extends AttributeTypeDefinitionInfo {
	private final String _unit;

	public DoubleDefinitionInfo(DoubleAttributeType att) {
		super(att);
		String unit;
		try {
			unit = att.getUnit();
			if(unit == null) unit = "";
		}
		catch(ConfigurationException e) {
			unit = "<<" + e.getMessage() + ">>";
		}
		_unit = unit;
	}

	public boolean isSizeFixed() {
		return true;
	}

	public int getFixedSize() {
		return 8;
	}

	public String getValueText(byte[] bytes, int offset) {
		synchronized(_doubleNumberFormat) {
			return _doubleNumberFormat.format(doubleValue(bytes, offset));
		}
	}

	public String getSuffixText(byte[] bytes, int offset) {
		return _unit;
	}

	public boolean isNumberAttribute() {
		return true;
	}

	public boolean isScalableNumberAttribute() {
		return false;
	}

	public boolean isNumber(byte[] bytes, int offset) {
		return true;
	}

	public boolean isState(byte[] bytes, int offset) {
		return false;
	}

	public IntegerValueState getState(byte[] bytes, int offset) {
		return null;
	}

	public byte byteValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attribut  kann nicht im gewüschten Zahlentyp dargestellt werden");
	}

	public short shortValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attribut  kann nicht im gewüschten Zahlentyp dargestellt werden");
	}

	public int intValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attribut  kann nicht im gewüschten Zahlentyp dargestellt werden");
	}

	public long longValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attribut  kann nicht im gewüschten Zahlentyp dargestellt werden");
	}

	public float floatValue(byte[] bytes, int offset) {
		return (float)doubleValue(bytes, offset);
	}

	public double doubleValue(byte[] bytes, int offset) {
		return Double.longBitsToDouble(readLong(bytes, offset));
	}


}
