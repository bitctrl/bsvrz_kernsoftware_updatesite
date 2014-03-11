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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5049 $ / $Date: 2007-08-31 17:49:39 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public class ByteArrayListArrayItemData extends ByteArrayListData {
	private final int _itemIndex;

	public ByteArrayListArrayItemData(byte[] bytes, int offset, AttributeInfo attributeInfo, int itemIndex) {
		super(bytes, offset, attributeInfo);
		_itemIndex = itemIndex;
	}

	public String getName() {
		return String.valueOf(_itemIndex);
	}
}
