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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info;

import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.IntegerValueState;

/**
 * Schnittstelle, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface AttributeDefinitionInfo {
	void dump(int indent);
	boolean isSizeFixed();
	int getFixedSize();
	AttributeType getAttributeType();
	boolean isList();
	AttributeInfo getItem(String name);
	AttributeInfo getItem(int itemIndex);
	int getItemCount();


	int getSize(byte[] bytes, int offset);

	String getValueText(byte[] bytes, int offset);
	String getSuffixText(byte[] bytes, int offset);

	boolean isTimeAttribute();

	long getSeconds(byte[] bytes, int offset);

	long getMillis(byte[] bytes, int offset);

	boolean isReferenceAttribute();

	long getId(byte[] bytes, int offset);

	SystemObject getSystemObject(byte[] bytes, int offset);

	boolean isNumberAttribute();

	boolean isScalableNumberAttribute();

	boolean isNumber(byte[] bytes, int offset);

	boolean isState(byte[] bytes, int offset);

	byte unscaledByteValue(byte[] bytes, int offset);

	short unscaledShortValue(byte[] bytes, int offset);

	int unscaledIntValue(byte[] bytes, int offset);

	long unscaledLongValue(byte[] bytes, int offset);

	float unscaledFloatValue(byte[] bytes, int offset);

	double unscaledDoubleValue(byte[] bytes, int offset);

	String getUnscaledValueText(byte[] bytes, int offset);

	String getUnscaledSuffixText(byte[] bytes, int offset);

	IntegerValueState getState(byte[] bytes, int offset);

	byte byteValue(byte[] bytes, int offset);

	short shortValue(byte[] bytes, int offset);

	int intValue(byte[] bytes, int offset);

	long longValue(byte[] bytes, int offset);

	float floatValue(byte[] bytes, int offset);

	double doubleValue(byte[] bytes, int offset);
}
