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
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class AbstractAttributeInfo implements AttributeInfo {
	private final Attribute _attribute;
	private final int _offset;
	private final AttributeInfo _offsetReferral;
	private final AttributeDefinitionInfo _definitionInfo;

	protected AbstractAttributeInfo(Attribute attribute, int offset, AttributeInfo offsetReferral, AttributeDefinitionInfo definitionInfo) {
		_attribute = attribute;
		_offset = offset;
		_offsetReferral = offsetReferral;
		_definitionInfo = definitionInfo;
	}

	static AttributeInfo forAttribute(Attribute attribute, int offset, AttributeInfo offsetReferral) {
		try {
			AttributeType attributeType = attribute.getAttributeType();
			final AttributeDefinitionInfo definitionInfo;
			if(attributeType instanceof AttributeSet) {
				definitionInfo = AbstractAttributeDefinitionInfo.forAttributSet((AttributeSet)attributeType);
			}
			else {
				definitionInfo = AbstractAttributeDefinitionInfo.forAttributeType(attributeType);
			}
			if(attribute.isArray()) {
				return new ArrayAttributeInfo(attribute, offset, offsetReferral, definitionInfo);
			}
			else {
				return new SingleAttributeInfo(attribute, offset, offsetReferral, definitionInfo);
			}
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected Attribute getAttribute() {
		return _attribute;
	}

	public final String getName() {
		return _attribute.getName();
	}


	public void dump(int indent) {
		for(int i=0; i <indent; ++i) System.out.print(" ");
		System.out.print(getClass().getName() + "(" + getName() + (isArray() ? "[]" : "") + ") offset(" + getRelativeOffset() + (getOffsetReferral() == null ? "" : " hinter " + getOffsetReferral().getName()) +") " + (isSizeFixed() ? " fixedSize: " + getFixedSize() : "variableSize") + " --> ");
		getDefinitionInfo().dump(indent + 1);
	}

	public int getRelativeOffset() {
		return _offset;
	}

	public AttributeInfo getOffsetReferral() {
		return _offsetReferral;
	}

	public int getAbsoluteOffset(byte[] bytes, int parentOffset) {
		if(_offsetReferral == null) {
			return parentOffset + _offset;
		}
		int referralOffset = _offsetReferral.getAbsoluteOffset(bytes, parentOffset);
		int referralSize = _offsetReferral.getSize(bytes, referralOffset);
//		System.out.println("referralOffset = " + referralOffset);
//		System.out.println("referralSize = " + referralSize);
//		System.out.println("_offset = " + _offset);
//		System.out.println("(referralOffset + referralSize + _offset) = " + (referralOffset + referralSize + _offset));
		return referralOffset + referralSize + _offset;
	}

	public final AttributeDefinitionInfo getDefinitionInfo() {
		return _definitionInfo;
	}

	public boolean isCountVariable() {
		try {
			return getAttribute().isCountVariable();
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean isCountLimited() {
		try {
			return getAttribute().isCountLimited();
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public int getMaxCount() {
		try {
			return getAttribute().getMaxCount();
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getDefaultAttributeValue() {
		try {
			return getAttribute().getDefaultAttributeValue();
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Data createModifiableData(byte[] bytes) {
		throw new IllegalStateException("getModifiableCopy(): Kopie kann nur von ganzen Datensätzen erzeugt werden, this: " + getName());
	}
}
