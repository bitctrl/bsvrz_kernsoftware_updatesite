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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ArrayAttributeInfo extends AbstractAttributeInfo {
	private final boolean _sizeFixed;
	private final int _fixedSize;
	private final AttributeInfo _elementInfo;

	public ArrayAttributeInfo(final Attribute attribute, int offset, AttributeInfo offsetReferral, AttributeDefinitionInfo definitionInfo) {
		super(attribute, offset, offsetReferral, definitionInfo);
		_elementInfo = new ArrayElementAttributeInfo(attribute, definitionInfo);
		try {
			_sizeFixed = definitionInfo.isSizeFixed() && !attribute.isCountVariable();
			if(_sizeFixed) {
				_fixedSize = 4 + getDefinitionInfo().getFixedSize() * attribute.getMaxCount();
			}
			else {
				_fixedSize = 0;
			}
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean isArray() {
		return true;
	}

	public boolean isSizeFixed() {
		return _sizeFixed;
	}

	public int getFixedSize() {
		return _fixedSize;
	}

	public int getSize(byte[] bytes, int offset) {
//		System.out.println(getName() + ".getSize(..," +offset + ")");
		final int elementCount = getElementCount(bytes, offset);
		final int relativeOffset = getRelativeOffset(bytes, offset, elementCount);
//		System.out.println("- returning " + getName() + ".getSize(..," +offset + "): " + relativeOffset);
		return relativeOffset;
	}

	public int getElementCount(final byte[] bytes, final int offset) {
		final int b0 = bytes[offset + 0] & 0xff;
		final int b1 = bytes[offset + 1] & 0xff;
		final int b2 = bytes[offset + 2] & 0xff;
		final int b3 = bytes[offset + 3] & 0xff;
//		System.out.println(getName() + ": bx = " + b0);
//		System.out.println(getName() + ": bx = " + b1);
//		System.out.println(getName() + ": bx = " + b2);
//		System.out.println(getName() + ": bx = " + b3);
		final int arraySize = (b0 << 24) | (b1 << 16) | (b2 << 8) | (b3 << 0);
//		System.out.println(getName() + ": arraySize = " + arraySize);
		return arraySize;
	}

	public int getAbsoluteElementOffset(byte[] bytes, int offset, int elementIndex) {
		final int elementCount = getElementCount(bytes, offset);
		if(elementIndex < 0 || elementIndex >= elementCount) {
			throw new ArrayIndexOutOfBoundsException("Ungültiger Index " + elementIndex + " beim Zugriff auf Array " + getName() + ", Arraygröße: " + elementCount);
		}
		return offset + getRelativeOffset(bytes, offset, elementIndex);


	}

	public AttributeInfo getElementInfo() {
		return _elementInfo;
	}

	private int getRelativeOffset(byte[] bytes, int offset, int elementIndex) {
		final AttributeDefinitionInfo definitionInfo = getDefinitionInfo();
		if(definitionInfo.isSizeFixed()) {
			return 4 + elementIndex * definitionInfo.getFixedSize();
		}
		int size = 4;
		for(int i = 0; i < elementIndex; ++i) {
			size += definitionInfo.getSize(bytes, offset + size);
		}
		return size;
	}

}
