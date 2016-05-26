/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.Data;

import java.util.*;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class AttributeGroupInfo implements AttributeInfo {

	private static final Map<AttributeGroup, AttributeInfo> _Atg2AttributeInfo = new IdentityHashMap<AttributeGroup, AttributeInfo>();

	private final AttributeGroup _atg;
	private final AttributeDefinitionInfo _definitionInfo;


	private AttributeGroupInfo(final AttributeGroup atg) {
		_atg = atg;
		_definitionInfo = AbstractAttributeDefinitionInfo.forAttributSet(atg);
	}

	public static AttributeInfo forAttributeGroup(final AttributeGroup atg) {
		synchronized(_Atg2AttributeInfo) {
			AttributeInfo info = _Atg2AttributeInfo.get(atg);
			if(info == null) {
				info = new AttributeGroupInfo(atg);
				_Atg2AttributeInfo.put(atg,info);
			}
			return info;
		}
	}

	public static void forgetDataModel(DataModel dataModel) {
		synchronized(_Atg2AttributeInfo) {
			List<AttributeGroup> atgs = new ArrayList<AttributeGroup>(_Atg2AttributeInfo.keySet());
			for(AttributeGroup attributeGroup : atgs) {
				if(attributeGroup.getDataModel() == dataModel) {
					_Atg2AttributeInfo.remove(attributeGroup);
				}
			}
		}
	}

	public String getName() {
		return _atg.getPid();
	}

	public AttributeDefinitionInfo getDefinitionInfo() {
		return _definitionInfo;
	}

	public boolean isArray() {
		return false;
	}

	public void dump(int indent) {
		for(int i=0; i <indent; ++i) System.out.print(" ");
		System.out.print("AttributeGroupInfo(" + getName() + (isArray() ? "[]" : "") + ") offset(" + getRelativeOffset() + ") " + (isSizeFixed() ? " fixedSize: " + getFixedSize() : "variableSize") + " --> ");
		getDefinitionInfo().dump(indent + 1);
	}


	public boolean isSizeFixed() {
		return getDefinitionInfo().isSizeFixed();
	}

	public int getFixedSize() {
		return getDefinitionInfo().getFixedSize();
	}

	public int getSize(byte[] bytes, int offset) {
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
		throw new UnsupportedOperationException("Index-Zugriff auf die Attribute einer Attributgruppe ist nicht erlaubt");
	}

	public int getAbsoluteElementOffset(byte[] bytes, int offset, int elementIndex) {
		throw new UnsupportedOperationException("Index-Zugriff auf die Attribute einer Attributgruppe ist nicht erlaubt");
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
		return ConcreteDataFactory.getInstance().createModifiableData(_atg, bytes);
	}
}
