/*
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeDefinitionInfo;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public class SingleAttributeInfo extends AbstractAttributeInfo {
	private final boolean _sizeFixed;
	private final int _fixedSize;

	public SingleAttributeInfo(final Attribute attribute, int offset, AttributeInfo offsetReferral, AttributeDefinitionInfo definitionInfo) {
		super(attribute, offset, offsetReferral, definitionInfo);
		_sizeFixed = definitionInfo.isSizeFixed();
		if(_sizeFixed) {
			_fixedSize = definitionInfo.getFixedSize();
		}
		else {
			_fixedSize = 0;
		}
	}

	public boolean isArray() {
		return false;
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

	public int getElementCount(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Index-Zugriff auf die Sub-Attribute eines Attributs ist nicht erlaubt");
	}

	public int getAbsoluteElementOffset(byte[] bytes, int offset, int elementIndex) {
		throw new UnsupportedOperationException("Index-Zugriff auf die Sub-Attribute eines Attributs ist nicht erlaubt");
	}

	public AttributeInfo getElementInfo() {
		return null;
	}
}
