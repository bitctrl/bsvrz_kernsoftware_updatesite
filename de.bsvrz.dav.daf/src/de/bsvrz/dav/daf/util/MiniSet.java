/*
 * Copyright 2012 by Kappich Systemberatung Aachen
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
package de.bsvrz.dav.daf.util;

import java.util.Set;

/**
 * Minimalimplementierung eines Sets als verkettete Liste mit einem Bloom-Filter. Gut geeignet für kleine Sets mit
 * typischerweise etwa 0-10 Einträgen. Das Set ist optimiert fürs iterieren und Hinzufügen von neuen Elementen. Das
 * häufige Entfernen (und wieder Einfügen) von Elementen (ohne das Set komplett zu leeren) verschlechtert den
 * Bloom-Filter und damit die Performance.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9883 $
 */
public class MiniSet<E> extends MiniCollection<E> implements Set<E> {

	protected int hash = 0xffffffff;

	public MiniSet() {
		super();
	}

	@Override
	public boolean contains(final Object o) {
		if((hash(o) & hash) != 0) return false;
		return super.contains(o);
	}

	@Override
	public boolean add(final E e) {
		if(contains(e)) return false;
		super.add(e);
		hash &= ~hash(e);
		return true;
	}

	@Override
	protected void addFirst(final E element) {
		hash = 0xffffffff;
		super.addFirst(element);
	}

	protected static int hash(Object o) {
		int i = o == null ? 13 : o.hashCode();
		i ^= i << 3;
		i ^= i << 5;
		i ^= i << 17;
		i &= ((i << 3) ^ (i >>> 7));
		return i;
	}
}
