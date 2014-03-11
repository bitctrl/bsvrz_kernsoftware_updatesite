/*
* Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.config.AttributeType;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Schnittstelle, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 9268 $ / $Date: 2011-05-16 11:12:28 +0200 (Mo, 16 Mai 2011) $ / ($Author: jh $)
 */
public abstract class AttributeTypeDefinitionInfo extends AbstractAttributeDefinitionInfo {
	protected static final NumberFormat _doubleNumberFormat;

	static {
		_doubleNumberFormat= NumberFormat.getNumberInstance(Locale.GERMANY);
		_doubleNumberFormat.setMinimumIntegerDigits(1);
		_doubleNumberFormat.setMaximumIntegerDigits(999);
		_doubleNumberFormat.setMinimumFractionDigits(0);
		_doubleNumberFormat.setMaximumFractionDigits(999);
		_doubleNumberFormat.setGroupingUsed(false);
	}

	private final AttributeType _attributeType;

	protected AttributeTypeDefinitionInfo(AttributeType attributeType) {
		_attributeType = attributeType;
	}

	public AttributeType getAttributeType() {
		return _attributeType;
	}

	public boolean isList() {
		return false;
	}

	public AttributeInfo getItem(String name) {
		throw new IllegalStateException(
		        "getItem(\"" + name + "\"): Ein Attribut vom Typ " + _attributeType.getPid() + " hat keine Sub-Attribute"
		);
	}

	public AttributeInfo getItem(int itemIndex) {
		throw new IllegalStateException(
		        "getItem(\"" + itemIndex + "\"): Ein Attribut vom Typ " + _attributeType.getPid() + " hat keine Sub-Attribute"
		);
	}

	public int getItemCount() {
		return 0;
	}

	public void dump(int indent) {
		//for(int i=0; i <indent; ++i) System.out.print(" ");
		System.out.println(
		        getClass().getName() + "(" + _attributeType.getPid() + ")" + (isSizeFixed() ?
		                                                                      " fixedSize: " + getFixedSize() :
		                                                                      "variableSize")
		);
	}

	public int getSize(byte[] bytes, int offset) {
		// Attributtypen mit variabler L�nge m�ssen diese Methode �berschreiben.
		return getFixedSize();
	}

	protected long readLong(byte[] bytes, int offset) {
		return (long)(bytes[offset + 0] & 0xff) << 56 |
		       (long)(bytes[offset + 1] & 0xff) << 48 |
		       (long)(bytes[offset + 2] & 0xff) << 40 |
		       (long)(bytes[offset + 3] & 0xff) << 32 |
		       (long)(bytes[offset + 4] & 0xff) << 24 |
		       (bytes[offset + 5] & 0xff) << 16 |
		       (bytes[offset + 6] & 0xff) << 8 |
		       (bytes[offset + 7] & 0xff) << 0;
	}

	protected long readUnsignedInt(byte[] bytes, int offset) {
		return (long)(bytes[offset + 0] & 0xff) << 24 |
		       (bytes[offset + 1] & 0xff) << 16 |
		       (bytes[offset + 2] & 0xff) << 8 |
		       (bytes[offset + 3] & 0xff) << 0;
	}

	protected int readInt(byte[] bytes, int offset) {
		return (bytes[offset + 0] & 0xff) << 24 |
		       (bytes[offset + 1] & 0xff) << 16 |
		       (bytes[offset + 2] & 0xff) << 8 |
		       (bytes[offset + 3] & 0xff) << 0;
	}

	protected short readShort(byte[] bytes, int offset) {
		return (short)((bytes[offset + 0] & 0xff) << 8 | (bytes[offset + 1] & 0xff) << 0);
	}
}
