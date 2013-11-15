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

import java.util.*;

/**
 * Minimalimplementierung einer Collection als verkettete Liste. Gut geeignet für Collections, die typischerweise nur
 * etwa 0 bis 5 Elemente haben. add(), peek(), pop() und clear() laufen in konstanter Zeit, die meisten anderen
 * Methoden in linearer. Erlaubt null-Werte und doppelte Einträge.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9888 $
 */
public class MiniCollection<E> extends AbstractCollection<E> {

	/**
	 * Nächstes Element in der Liste. null: dies ist das letzte Element. this: Die Liste ist leer.
	 */
	protected MiniCollection<E> next;

	protected E object;

	protected int modCount;

	/**
	 * Erstellt eine neue, leere Collection
	 */
	public MiniCollection() {
		next = this;
	}

	/**
	 * Erstellt eine Collection mit einem Element
	 *
	 * @param element Element
	 */
	public MiniCollection(final E element) {
		object = element;
	}

	protected MiniCollection(final E element, final MiniCollection<E> next) {
		object = element;
		this.next = next;
	}

	@Override
	public boolean isEmpty() {
		return next == this;
	}

	/**
	 * Gibt das oberste Element zurück
	 *
	 * @return Ein Element der Collection (das welches, der iterator zuerst ausgeben würde)
	 * @throws NoSuchElementException falls die Collection leer ist.
	 */
	public E peek() {
		if(next == this) throw new NoSuchElementException();
		return object;
	}

	/**
	 * Gibt das oberste Element zurück und entfernt es aus der Collection
	 *
	 * @return Ein Element der Collection (das welches, der iterator zuerst ausgeben würde)
	 * @throws NoSuchElementException falls die Collection leer ist.
	 */
	public E pop() {
		if(next == this) throw new NoSuchElementException();
		E tmp = object;
		if(next != null) {
			object = next.object;
			next = next.next;
		} else {
			// leere Liste
			this.next = this;
			object = null;
		}
		return tmp;
	}

	@Override
	public boolean add(final E e) {
		modCount++;
		if(isEmpty()) {
			addFirst(e);
			return true;
		}
		this.next = new MiniCollection<E>(this.object, this.next);
		this.object = e;
		return true;
	}

	protected void addFirst(final E element) {
		next = null;
		object = element;
	}

	@Override
	public void clear() {
		modCount++;
		next = this;
		object = null;
	}

	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}

	@Override
	public int size() {
		if(isEmpty()) return 0;
		int i = 1;
		MiniCollection<E> l = this;
		while(l.next != null) {
			l = l.next;
			i++;
		}
		return i;
	}

	protected final class Itr implements ListIterator<E> {

		MiniCollection<E> _next = MiniCollection.this;

		MiniCollection<E> _current = null;

		MiniCollection<E> _prev = null;

		MiniCollection<E> _lastRet = null;

		int _expectedModCount = modCount;
		
		int _cursor;

		public Itr() {}		
		
		public Itr(int index) {
			while (index > 0){
				next();
				index --;
			}
		}

		public boolean hasNext() {
			return _next != null && _next.next != _next;
		}

		public boolean hasPrevious() {
			return _prev != null;
		}

		public E previous() {
			checkForComodification();
			_next = MiniCollection.this;
			_current = null;
			_prev = null;
			_lastRet = null;
			int newCursor = _cursor - 1;
			_cursor = 0;
			while (_cursor < newCursor){
				next();
			}
			_lastRet = _next;
			if(_lastRet == null) throw new NoSuchElementException();
			return _lastRet.object;
		}

		public int nextIndex() {
			return _cursor;
		}

		public int previousIndex() {
			return _cursor-1;
		}

		public void set(final E e) {
			if(_lastRet == null) throw new IllegalStateException();

			_lastRet.object = e;
		}

		public void add(final E e) {
			checkForComodification();
			modCount++;

			if(_next != null){
				_next.next = new MiniCollection<E>(_next.object, _next.next);
				_next.object = e;
			}
			else{
				_next = _current.next = new MiniCollection<E>(e, null);
			}
			_expectedModCount = modCount;
			next();
			_lastRet = null;
		}

		public E next() {
			checkForComodification();
			if(_next == null) {
				throw new NoSuchElementException();
			}
			E o = _next.object;
			_prev = _current;
			_current = _lastRet = _next;
			_next = _next.next;
			_cursor++;
			return o;
		}

		public void remove() {
			if(_lastRet == null) throw new IllegalStateException();

			checkForComodification();

			modCount++;
			if(_lastRet.next != null) {
				_lastRet.object = _lastRet.next.object;
				_lastRet.next = _lastRet.next.next;
			} else if(_lastRet == _next) {
				// Letztes Objekt entfernen
				_current.next = null;	
				_next = null;
			} else if(_prev != null) {
				// Letztes Objekt entfernen
				_prev.next = null;
				_current = null;
				_next = null;
			} else {
				// leere Liste
				MiniCollection.this.next = MiniCollection.this;
				object = null;
			}
			_expectedModCount = modCount;
			if(_lastRet == _prev){
				previous();
			}
			_lastRet = null;
		}

		final void checkForComodification() {
			if(modCount != _expectedModCount)
				throw new ConcurrentModificationException();
		}
	}
}
