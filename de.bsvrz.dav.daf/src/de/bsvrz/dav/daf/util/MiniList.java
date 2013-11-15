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
 * @author Kappich Systemberatung
 * @version $Revision: 9888 $
 */
public class MiniList<E> extends MiniCollection<E> implements List<E> {

	public MiniList() {
		super();
	}

	public MiniList(final E element) {
		super(element);
	}

	private MiniList(final E element, final MiniList<E> next) {
		super(element, next);
	}

	@Override
	public boolean isEmpty() {
		return next == this;
	}

	public E get(int index) {
		if(isEmpty()) throw new IndexOutOfBoundsException();
		MiniCollection<E> l = this;
		while(index != 0) {
			l = l.next;
			if(l == null) throw new IndexOutOfBoundsException();
			index--;
		}
		return l.object;
	}

	public E set(int index, E element) {
		if(isEmpty()) throw new IndexOutOfBoundsException();
		MiniCollection<E> l = this;
		while(index != 0) {
			l = l.next;
			if(l == null) throw new IndexOutOfBoundsException();
			index--;
		}
		E tmp = l.object;
		l.object = element;
		return tmp;
	}

	public boolean add(final E e) {
		modCount++;
		if(isEmpty()) {
			addFirstWhenEmpty(e);
		} else {
			MiniCollection<E> l = this;
			while(l.next != null) {
				l = l.next;
			}
			l.next = new MiniList<E>(e);
		}
		return true;
	}

	private void addFirstWhenEmpty(final E element) {
		next = null;
		object = element;
	}

	public void add(int index, E element) {
		modCount++;
		if(isEmpty()) {
			if(index != 0) throw new IndexOutOfBoundsException();
			addFirstWhenEmpty(element);
			return;
		}
		MiniCollection<E> l = this;
		while(index != 0) {
			l = l.next;
			if(l == null) throw new IndexOutOfBoundsException();
			index--;
		}
		l.next = new MiniCollection<E>(l.object, l.next);
		l.object = element;
	}


	public boolean addAll(int index, final Collection<? extends E> c) {
		if(c.isEmpty()) return false;
		modCount++;
		if(isEmpty()) {
			if(index != 0) throw new IndexOutOfBoundsException();
			Iterator<? extends E> iterator = c.iterator();
			addFirstWhenEmpty(iterator.next());
			while(iterator.hasNext()){
				add(0, iterator.next());
			}
			return true;
		}
		MiniCollection<E> l = this;
		while(index != 0) {
			l = l.next;
			if(l == null) throw new IndexOutOfBoundsException();
			index--;
		}
		Iterator<? extends E> iterator = c.iterator();
		while (iterator.hasNext()){
			l.next = new MiniCollection<E>(l.object, l.next);
			l.object = iterator.next();
		}
		return true;
	}

	public E remove(int index) {
		MiniCollection<E> l = this;
		MiniCollection<E> p = null;
		while(index != 0) {
			p = l;
			l = l.next;
			if(l == null) throw new IndexOutOfBoundsException();
			index--;
		}
		modCount++;
		E tmp = l.object;
		MiniCollection<E> n = l.next;
		if(n != null) {
			l.object = n.object;
			l.next = n.next;
		} else if(p != null) {
			// Letztes Objekt entfernen
			p.next = null;
		} else {
			// leere Liste
			next = this;
			object = null;
		}
		return tmp;
	}

	public int indexOf(final Object o) {
		int i = 0;
		if(o == null){
			for(E e : this) {
				if(e == null) return i;
				i++;
			}
		}
		else{
			for(E e : this) {
				if(e.equals(o)) return i;
				i++;
			}
		}
		return -1;
	}

	public int lastIndexOf(final Object o) {
		int i = 0, index = -1;
		if(o == null){
			for(E e : this) {
				if(e == null) index = i;
				i++;
			}
		}
		else{
			for(E e : this) {
				if(e.equals(o)) index = i;
				i++;
			}
		}
		return index;
	}

	@Override
	public void clear() {
		modCount++;
		next = this;
		object = null;
	}

	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	public ListIterator<E> listIterator(final int index) {
		return new Itr(index);
	}

	public List<E> subList(final int fromIndex, final int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex +
					") > toIndex(" + toIndex + ")");
		return new SubList(this, fromIndex, toIndex);
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof List))
			return false;

		ListIterator<E> e1 = listIterator();
		ListIterator e2 = ((List) o).listIterator();
		while(e1.hasNext() && e2.hasNext()) {
			E o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1==null ? o2==null : o1.equals(o2)))
				return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	public int hashCode() {
		int hashCode = 1;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	
	class SubList extends AbstractList<E> {
		private MiniList<E> l;
		private int offset;
		private int size;
		private int expectedModCount;

		SubList(MiniList<E> list, int fromIndex, int toIndex) {
			if (fromIndex < 0)
				throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
			if (toIndex > list.size())
				throw new IndexOutOfBoundsException("toIndex = " + toIndex);
			if (fromIndex > toIndex)
				throw new IllegalArgumentException("fromIndex(" + fromIndex +
						") > toIndex(" + toIndex + ")");
			l = list;
			offset = fromIndex;
			size = toIndex - fromIndex;
			expectedModCount = l.modCount;
		}

		public E set(int index, E element) {
			rangeCheck(index);
			checkForComodification();
			return l.set(index+offset, element);
		}

		public E get(int index) {
			rangeCheck(index);
			checkForComodification();
			return l.get(index+offset);
		}

		public int size() {
			checkForComodification();
			return size;
		}

		public void add(int index, E element) {
			if (index<0 || index>size)
				throw new IndexOutOfBoundsException();
			checkForComodification();
			l.add(index+offset, element);
			expectedModCount = l.modCount;
			size++;
			modCount++;
		}

		public E remove(int index) {
			rangeCheck(index);
			checkForComodification();
			E result = l.remove(index+offset);
			expectedModCount = l.modCount;
			size--;
			modCount++;
			return result;
		}

		public boolean addAll(Collection<? extends E> c) {
			return addAll(size, c);
		}

		public boolean addAll(int index, Collection<? extends E> c) {
			if (index<0 || index>size)
				throw new IndexOutOfBoundsException(
						"Index: "+index+", Size: "+size);
			int cSize = c.size();
			if (cSize==0)
				return false;

			checkForComodification();
			l.addAll(offset+index, c);
			expectedModCount = l.modCount;
			size += cSize;
			modCount++;
			return true;
		}

		public Iterator<E> iterator() {
			return listIterator();
		}

		public ListIterator<E> listIterator(final int index) {
			checkForComodification();
			if (index<0 || index>size)
				throw new IndexOutOfBoundsException(
						"Index: "+index+", Size: "+size);

			return new ListIterator<E>() {
				private ListIterator<E> i = l.listIterator(index+offset);

				public boolean hasNext() {
					return nextIndex() < size;
				}

				public E next() {
					if (hasNext())
						return i.next();
					else
						throw new NoSuchElementException();
				}

				public boolean hasPrevious() {
					return previousIndex() >= 0;
				}

				public E previous() {
					if (hasPrevious())
						return i.previous();
					else
						throw new NoSuchElementException();
				}

				public int nextIndex() {
					return i.nextIndex() - offset;
				}

				public int previousIndex() {
					return i.previousIndex() - offset;
				}

				public void remove() {
					i.remove();
					expectedModCount = l.modCount;
					size--;
					modCount++;
				}

				public void set(E e) {
					i.set(e);
				}

				public void add(E e) {
					i.add(e);
					expectedModCount = l.modCount;
					size++;
					modCount++;
				}
			};
			
		}

		private void rangeCheck(int index) {
			if (index<0 || index>=size)
				throw new IndexOutOfBoundsException("Index: "+index+
						",Size: "+size);
		}

		private void checkForComodification() {
			if (l.modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}
}
