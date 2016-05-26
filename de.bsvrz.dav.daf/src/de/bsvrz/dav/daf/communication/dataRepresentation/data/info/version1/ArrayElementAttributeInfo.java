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

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ArrayElementAttributeInfo implements AttributeInfo {
	private final boolean _sizeFixed;
	private final int _fixedSize;
	private final Attribute _attribute;
	private final AttributeDefinitionInfo _definitionInfo;

	public ArrayElementAttributeInfo(final Attribute attribute, AttributeDefinitionInfo definitionInfo) {
		_attribute = attribute;
		_definitionInfo = definitionInfo;
		_sizeFixed = definitionInfo.isSizeFixed();
		if(_sizeFixed) {
			_fixedSize = definitionInfo.getFixedSize();
		}
		else {
			_fixedSize = 0;
		}
	}

	public String getName() {
		return "?";
	}

	public AttributeDefinitionInfo getDefinitionInfo() {
		return _definitionInfo;
	}

	public boolean isArray() {
		return false;
	}

	public void dump(int indent) {
		for(int i=0; i <indent; ++i) System.out.print(" ");
		System.out.print(getClass().getName() + "(" + getName() + (isArray() ? "[]" : "") + ") " + (isSizeFixed() ? " fixedSize: " + getFixedSize() : "variableSize") + " --> ");
		getDefinitionInfo().dump(indent + 1);
	}

	public boolean isSizeFixed() {
		return _sizeFixed;
	}

	public int getFixedSize() {
		return _fixedSize;
	}

	public int getSize(byte[] bytes, int offset) {
		if(_sizeFixed) return _fixedSize;
		return getDefinitionInfo().getSize(bytes, offset);
	}

	public int getRelativeOffset() {
		return 0;
	}

	public AttributeInfo getOffsetReferral() {
		return null;
	}

	public int getAbsoluteOffset(byte[] bytes, int parentOffset) {
		return 0;
	}

	public int getElementCount(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Index-Zugriff auf die Sub-Attribute eines Array-Elements ist nicht erlaubt");
	}

	public int getAbsoluteElementOffset(byte[] bytes, int offset, int elementIndex) {
		throw new UnsupportedOperationException("Index-Zugriff auf die Sub-Attribute eines Array-Elements ist nicht erlaubt");
	}

	public AttributeInfo getElementInfo() {
		return null;
	}
	public boolean isCountVariable() {
		return false;
	}

	public boolean isCountLimited() {
		return true;
	}

	public int getMaxCount() {
		return 1;
	}

	public Data createModifiableData(byte[] bytes) {
		throw new IllegalStateException("getModifiableCopy(): Kopie kann nur von ganzen Datensätzen erzeugt werden, this: " + getName());
	}
}
