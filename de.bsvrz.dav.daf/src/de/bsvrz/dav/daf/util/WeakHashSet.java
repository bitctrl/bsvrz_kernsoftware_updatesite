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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * Ein WeakHashSet referenziert seine Elemente nur über {@link WeakReference}s. Damit können Elemente im WeakHashSet
 * jederzeit durch den Garbage-Collector entsorgt werden, sofern sie nirgendwo anders direkt referenziert werden.
 * <p>
 * Das Hinzufügen von null-Elementen wird still ignoriert, da null-Elemente hier bedeuten, dass ein entsprechendes
 * Objekt vom Garbage-Collector entfernt wurde. Das Hinzufügen von null kann also so interpretiert werden, dass ein
 * bereits nicht mehr verfügbares Element hinzugefügt werden soll, was in keiner Änderung des Sets resultiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class WeakHashSet<E> extends AbstractSet<E> {
	private final WeakHashMap<E, Reference<E>> _backingHashMap;

	/**
	 * Erstellt ein neues WeakHashSet
	 */
	public WeakHashSet() {
		_backingHashMap = new WeakHashMap<E, Reference<E>>();
	}

	/**
	 * Erstellt ein neues WeakHashSet
	 *
	 * @param initialCapacity ursprüngliche Kapazität
	 */
	public WeakHashSet(final int initialCapacity) {
		_backingHashMap = new WeakHashMap<E, Reference<E>>(initialCapacity);
	}

	/**
	 * Erstellt ein neues WeakHashSet
	 *
	 * @param initialCapacity ursprüngliche Kapazität
	 * @param loadFactor      load Factor
	 */
	public WeakHashSet(final int initialCapacity, final float loadFactor) {
		_backingHashMap = new WeakHashMap<E, Reference<E>>(initialCapacity, loadFactor);
	}

	/**
	 * Gibt einen iterator über alle verbleibenden Elemente zurück
	 *
	 * @return einen iterator über alle verbleibenden Elemente
	 */
	@Override
	public Iterator<E> iterator() {
		return _backingHashMap.keySet().iterator();
	}

	/**
	 * Gibt die Anzahl der enthaltenen Elemente zurück. Da praktisch zu jeder Zeit Elemente vom Garbage Collector entfernt
	 * werden können, kann die zurückgegebene Anzahl sich von folgenden Funktionsaufrufen unterscheiden.
	 *
	 * @return die Anzahl der enthaltenen Elemente
	 */
	@Override
	public int size() {
		return _backingHashMap.size();
	}

	@Override
	public boolean isEmpty() {
		return _backingHashMap.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		if(o == null) return false;
		return _backingHashMap.containsKey(o);
	}

	/**
	 * Fügt ein neues Element hinzu, welches über eine {@link WeakReference} referenziert wird.
	 *
	 * @param e Element
	 * @see #addWeakReference(Object)
	 * @see #addSoftReference(Object)
	 */
	@Override
	public boolean add(final E e) {
		return addWeakReference(e);
	}

	/**
	 * Fügt ein neues Element hinzu, welches über eine {@link WeakReference} referenziert wird. Identisch zu {@link
	 * #add(Object)}
	 *
	 * @param e Element
	 */
	public boolean addWeakReference(final E e) {
		if(e == null) return false;
		return _backingHashMap.put(e, new WeakReference<E>(e)) == null;
	}

	/**
	 * Fügt ein neues Element hinzu, welches über eine {@link SoftReference} referenziert wird.
	 *
	 * @param e Element
	 */
	public boolean addSoftReference(final E e) {
		if(e == null) return false;
		return _backingHashMap.put(e, new SoftReference<E>(e)) == null;
	}

	@Override
	public boolean remove(final Object o) {
		if(o == null) return false;
		return _backingHashMap.remove(o) == null;
	}

	@Override
	public void clear() {
		_backingHashMap.clear();
	}

	/**
	 * Gibt einen bereits gespeicherten Eintrag zurück, bei dem equals() mit dem übergebenen Objekt true liefern würde.
	 * Durch diese Methode kann das WeakHashSet als ein Cache benutzt werden, da hier mehrere Identische Objekte durch ein
	 * einzelnes Objekt mit den gleichen Eigenschaften ersetzt werden kann.
	 *
	 * @return Eintrag oder null falls kein solches Element (mehr) exstiert
	 */
	public E get(final E entry) {
		Reference<E> reference = _backingHashMap.get(entry);
		if(reference == null) return null;
		return reference.get();
	}
}
