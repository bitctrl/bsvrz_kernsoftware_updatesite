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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.Data;

import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ByteArrayListData extends ByteArrayStructuredData {
	public ByteArrayListData(byte[] bytes, int offset, AttributeInfo attributeInfo) {
		super(bytes, offset, attributeInfo);
	}

	public Data.Array asArray() {
		throw new UnsupportedOperationException("Attributliste " + getName() + " kann nicht in einem Array dargestellt werden");
	}

	public Data getItem(String itemName) {
		AttributeInfo subInfo = getInfo().getDefinitionInfo().getItem(itemName);
		int offset = subInfo.getAbsoluteOffset(_bytes, _offset);
		return create(_bytes, offset, subInfo);
	}

	public Iterator<Data> iterator() {
		return new ListDataIterator();
	}

	private class ListDataIterator implements Iterator<Data> {
		private int _nextElementIndex = 0;
		private int _nextElementOffset = _offset;

		public boolean hasNext() {
			if(_nextElementIndex < _info.getDefinitionInfo().getItemCount()) return true;
			return false;
		}

		public Data next() {
			if(_nextElementIndex >= _info.getDefinitionInfo().getItemCount()) throw new NoSuchElementException();
			final AttributeInfo elementInfo = _info.getDefinitionInfo().getItem(_nextElementIndex);
			final ByteArrayData data = create(_bytes, _nextElementOffset, elementInfo);
			_nextElementOffset += elementInfo.getSize(_bytes, _nextElementOffset);
			++_nextElementIndex;
			return data;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
	public boolean isList() {
		return true;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isPlain() {
		return false;
	}

	public Data.NumberArray asUnscaledArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.TimeArray asTimeArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.TextArray asTextArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.NumberArray asScaledArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

	public Data.ReferenceArray asReferenceArray() {
		throw new UnsupportedOperationException("Das Attribut " + getName() + " ist kein Array");
	}

}
