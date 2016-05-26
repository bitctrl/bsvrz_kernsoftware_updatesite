/*
 * Copyright 2012 by Kappich Systemberatung Aachen
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
package de.bsvrz.dav.daf.util;

import java.util.*;

/**
 * Minimalimplementierung eines Sets. Nur geeignet für wenige Elemente.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class MiniSet<E> extends AbstractSet<E> {

	private MiniList<E> _innerList = null;

	@Override
	public Iterator<E> iterator() {
		if(_innerList != null) return _innerList.iterator();
		return Collections.<E>emptyList().iterator();
	}

	@Override
	public int size() {
		if(_innerList == null) return 0;
		return _innerList.size();
	}

	@Override
	public boolean add(final E e) {
		if(_innerList == null){
			_innerList = new MiniList<E>(e);
			return true;
		}
		else {
			if(_innerList.contains(e)) return false;
			return _innerList.add(e);
		}
	}

	@Override
	public boolean remove(final Object o) {
		return _innerList.remove(o);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return _innerList.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return _innerList.retainAll(c);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return _innerList.containsAll(c);
	}

	@Override
	public void clear() {
		_innerList = null;
	}
}
