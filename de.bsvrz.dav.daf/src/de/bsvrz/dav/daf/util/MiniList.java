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
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class MiniList<E> implements List<E> {

	private Object[] _elements = null;

	public MiniList() {
		_elements = null;
	}

	public MiniList(final Collection<? extends E> c) {
		_elements = c.toArray();
	}

	public MiniList(final E[] e) {
		_elements = Arrays.copyOf(e, e.length, Object[].class);
	}

	public MiniList(final E element) {
		this(Collections.singletonList(element));
	}

	@Override
	public int size() {
		return _elements == null ? 0 : _elements.length;
	}

	@Override
	public boolean isEmpty() {
		return _elements == null;
	}

	@Override
	public boolean contains(final Object o) {
		if(_elements == null) return false;
		for(final Object element : _elements) {
			if(element != null) {
				if(element.equals(o)) return true;
			}
			else {
				if(o == null) return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		if(_elements == null) return Collections.<E>emptyList().iterator();
		return (Iterator<E>) Arrays.asList(_elements).iterator();
	}

	@Override
	public Object[] toArray() {
		if(_elements == null) return new Object[0];
		return _elements.clone();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		if(_elements == null){
			if(a.length > 0) a[0] = null;
			return a;
		}
		if(a.length < _elements.length) {
			return (T[]) Arrays.copyOf(_elements, _elements.length, a.getClass());
		}
		System.arraycopy(_elements, 0, a, 0, _elements.length);
		if(a.length > _elements.length) a[_elements.length] = null;
		return a;
	}

	@Override
	public boolean add(final E e) {
		if(_elements == null){
			_elements = new Object[]{e};
			return true;
		}
		Object[] tmp = Arrays.copyOf(_elements, _elements.length + 1);
		tmp[_elements.length] = e;
		_elements = tmp;
		return true;
	}

	@Override
	public boolean remove(final Object o) {
		int i = indexOf(o);
		if(i == -1) return false;
		remove(i);
		return true;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for(Object o : c) {
			if(!contains(o)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		if(_elements == null){
			_elements = c.toArray();
			return true;
		}
		Object[] tmp = Arrays.copyOf(_elements, _elements.length + c.size());
		int i = _elements.length;
		for(E e : c) {
			tmp[i++] = e;
		}
		_elements = tmp;
		return true;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		if(_elements == null){
			if(index != 0) throw new IndexOutOfBoundsException("index : " + index);
			_elements = c.toArray();
			return true;
		}
		Object[] tmp = new Object[_elements.length + c.size()];
		System.arraycopy(_elements, 0, tmp, 0, index);
		int i = index;
		for(E e : c) {
			tmp[i++] = e;
		}
		System.arraycopy(_elements, index, tmp, i, _elements.length-index);
		_elements = tmp;
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for(Object o : c) {
			changed |= remove(o);
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		ArrayList<E> tmp = new ArrayList<E>(this);
		boolean b = tmp.retainAll(c);
		_elements = tmp.toArray();
		return b;
	}

	@Override
	public void clear() {
		_elements = null;
	}

	@Override
	public E get(final int index) {
		if(_elements == null) throw new IndexOutOfBoundsException("List is empty");
		return (E) _elements[index];
	}

	@Override
	public E set(final int index, final E element) {
		if(_elements == null) throw new IndexOutOfBoundsException("List is empty");
		Object old = _elements[index];
		_elements[index] = element;
		return (E) old;
	}

	@Override
	public void add(final int index, final E element) {
		addAll(index, Collections.singletonList(element));
	}

	@Override
	public E remove(final int index) {
		if(_elements == null) throw new IndexOutOfBoundsException("List is empty");
		E old = (E) _elements[index];
		if(_elements.length == 1){
			_elements = null;
			return old;
		}
		Object[] tmp = new Object[_elements.length - 1];
		System.arraycopy(_elements, 0, tmp, 0, index);
		System.arraycopy(_elements, index + 1, tmp, index, _elements.length - index - 1);
		_elements = tmp;
		return old;
	}

	@Override
	public int indexOf(final Object o) {
		if(_elements == null) return -1;
		if(o != null){
			for(int i = 0; i < _elements.length; i++) {
				final Object element = _elements[i];
				if(o.equals(element)) return i;
			}
		}
		else {
			for(int i = 0; i < _elements.length; i++) {
				final Object element = _elements[i];
				if(element == null) return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object o) {
		if(_elements == null) return -1;
		if(o != null){
			for(int i = _elements.length - 1; i >= 0; i--) {
				final Object element = _elements[i];
				if(o.equals(element)) return i;
			}
		}
		else {
			for(int i = _elements.length - 1; i >= 0; i--) {
				final Object element = _elements[i];
				if(element == null) return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		if(_elements == null) return Collections.<E>emptyList().listIterator();
		return (ListIterator<E>) Arrays.asList(_elements).listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		if(_elements == null) return Collections.<E>emptyList().listIterator(index);
		return (ListIterator<E>) Arrays.asList(_elements).listIterator(index);
	}

	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		return new SubList(fromIndex, toIndex);
	}

	@Override
	public boolean equals(final Object obj) {
		if(_elements == null) return Collections.emptyList().equals(obj);
		return Arrays.asList(_elements).equals(obj);
	}

	@Override
	public int hashCode() {
		if(_elements == null) return Collections.emptyList().hashCode();
		return Arrays.asList(_elements).hashCode();
	}

	@Override
	public String toString() {
		if(_elements == null) return Collections.emptyList().toString();
		return Arrays.asList(_elements).toString();
	}

	private class SubList extends AbstractList<E> {
		private final int _fromIndex;
		private final int _toIndex;

		public SubList(final int fromIndex, final int toIndex) {
			_fromIndex = fromIndex;
			_toIndex = toIndex;
		}

		@Override
		public E get(final int index) {
			if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("index: " + index);
			return MiniList.this.get(index + _fromIndex);
		}

		@Override
		public int size() {
			return _toIndex - _fromIndex;
		}

		@Override
		public E set(final int index, final E element) {
			if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("index: " + index);
			return MiniList.this.set(index + _fromIndex, element);
		}

		@Override
		public void add(final int index, final E element) {
			if(index < 0 || index > size()) throw new IndexOutOfBoundsException("index: " + index);
			MiniList.this.add(index + _fromIndex, element);
		}

		@Override
		public E remove(final int index) {
			if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("index: " + index);
			return MiniList.this.remove(index + _fromIndex) ;
		}
	}
}
