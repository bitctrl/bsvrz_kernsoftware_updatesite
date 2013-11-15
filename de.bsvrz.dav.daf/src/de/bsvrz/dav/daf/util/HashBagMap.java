/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Klasse, die zu einem Key mehrere Values zuordnen kann. Unter jedem Key wird ein Set gespeichert, sodass pro Key das gleiche Objekt maximal einmal gespeichert
 * wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9883 $
 */
@SuppressWarnings({"unchecked"})
public class HashBagMap<K, V> implements Serializable {

	private static final long serialVersionUID = 4495094277066988824L;
	public static final int HASHSET_THRESHOLD = 10;

	private final HashMap<K, Collection<V>> _backingHashMap;

	public HashBagMap() {
		this(8);
	}

	public HashBagMap(final int initialCapacity) {
		_backingHashMap = new HashMap<K, Collection<V>>(initialCapacity);
	}

	public Iterable<Map.Entry<K, V>> elements() {
		return new Iterable<Map.Entry<K, V>>() {
			public Iterator<Map.Entry<K, V>> iterator() {
				return new Itr();
			}
		};
	}

	public int size() {
		return values().size();
	}

	/**
	 * Gibt true zur�ck, wenn keine Werte gespeichert sind
	 *
	 * @return true, wenn keine Werte gespeichert sind
	 */
	public boolean isEmpty() {
		if(_backingHashMap.isEmpty()) return true;
		for(final Collection<V> v : _backingHashMap.values()) {
			if(!v.isEmpty()) return false;
		}
		return true;
	}

	public boolean contains(final Object o) {
		if(o instanceof Map.Entry) {
			final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			final Collection<V> v = get(entry.getKey());
			return v != null && v.contains(entry.getValue());
		}
		return false;
	}

	public boolean add(final Map.Entry<K, V> entry) {
		return add(entry.getKey(), entry.getValue());
	}

	/**
	 * Entfernt ein Map.Entry<K, V> aus der HashBagMap
	 * <p/>
	 * {@inheritDoc}
	 */
	private boolean remove(final Object o) {
		if(o instanceof Map.Entry) {
			final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			return remove(entry.getKey(), entry.getValue());
		}
		return false;
	}

	/**
	 * Gibt true zur�ck, wenn die Klasse zu diesem Key mindestens einen Wert enth�lt
	 *
	 * @param key Key
	 * @return true wenn die Klasse zu diesem key mindestens einen Wert enth�lt
	 */
	public boolean containsKey(final Object key) {
		final Collection<V> v = _backingHashMap.get(key);
		return v != null && v.size() > 0;
	}

	/**
	 * Gibt true zur�ck, wenn dieser Wert mindestens einmal einem Key zugeordnet wurde
	 *
	 * @param value Wert
	 * @return true, wenn dieser Wert mindestens einmal einem Key zugeordnet wurde
	 */
	public boolean containsValue(final Object value) {
		return values().contains(value);
	}

	/**
	 * Gibt die Collection mit Objekten zur�ck, die hinter diesem Key liegen
	 *
	 * @param key Key
	 * @return Liste mit Objekten
	 */
	public Collection<V> get(final K key) {
		return new Col(key);
	}

	/**
	 * F�gt einem Key einen Wert hinzu
	 *
	 * @param key   Key
	 * @param value Wert
	 * @return true wenn die Collection ver�ndert wurde
	 */
	public boolean add(final K key, final V value) {
		final Collection<V> v = _backingHashMap.get(key);
		if(v == null) {
			final Collection<V> collection = createNewInstance(1);
			collection.add(value);
			_backingHashMap.put(key, collection);
			return true;
		} else {
			return checkAdd(key, v, value);
		}
	}

	/**
	 * F�gt einem Key mehrere Werte hinzu
	 *
	 * @param key   Key
	 * @param value Werte
	 * @return true wenn die Collection ver�ndert wurde
	 */
	public boolean addAll(final K key, final Collection<? extends V> value) {
		if(value.size() == 0) return false;
		final Collection<V> v = _backingHashMap.get(key);
		if(v == null) {
			final Collection<V> collection = createNewInstance(value.size());
			collection.addAll(value);
			_backingHashMap.put(key, collection);
			return collection.size() > 0;
		} else {
			return checkAdd(key, v, value);
		}
	}

	private boolean checkAdd(final K key, final Collection<V> col, final V add) {
		Collection<V> set = col;
		if(col instanceof MiniSet && col.size() + 1 > HASHSET_THRESHOLD){
			set = new HashSet<V>(col.size() + 1);
			set.addAll(col);
			set.add(add);
			_backingHashMap.put(key, set);
		}
		return set.add(add);
	}

	private boolean checkAdd(final K key, final Collection<V> col, final Collection<? extends V> add) {
		Collection<V> set = col;
		if(col instanceof MiniSet && col.size() + add.size() > HASHSET_THRESHOLD){
			set = new HashSet<V>(col.size() + 1);
			set.addAll(col);
			set.addAll(add);
			_backingHashMap.put(key, set);
		}
		return set.addAll(add);
	}

	/**
	 * Erstellt eine neue Collection-Instanz.
	 *
	 * @param size
	 * @return Leere Collection
	 */
	private Collection<V> createNewInstance(final int size) {
		if(size < HASHSET_THRESHOLD) {
			return new MiniSet<V>();
		} else {
			return new HashSet<V>(size);
		}
	}

	/**
	 * L�scht einen Eintrag
	 *
	 * @param key   Key
	 * @param value Wert
	 * @return true falls ein Wert entfernt wurde
	 */
	public boolean remove(final Object key, final Object value) {
		final Collection<V> v = _backingHashMap.get(key);
		if(v == null) return false;
		final boolean result = v.remove(value);
		if(v.size() == 0) {
			removeAll(key);
		}
		return result;
	}

	/**
	 * L�scht alle Eintr�ge eines Keys
	 *
	 * @param key Key
	 * @return true falls ein Wert entfernt wurde
	 */
	public Collection<V> removeAll(final Object key) {
		return _backingHashMap.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		_backingHashMap.clear();
	}

	/**
	 * Gibt ein Set �ber die Keys zur�ck
	 *
	 * @return ein Set �ber die Keys
	 */
	public Set<K> keySet() {
		return _backingHashMap.keySet();
	}

	/**
	 * Gibt eine Liste �ber alle values zur�ck. �nderungen an der zur�ckgegeben Collection haben keine Auswirkungen auf die HashBagMap. Eintr�ge, die mehreren
	 * Keys zugeordnet sind werden mehrfach zur�ckgegeben. Die Reihenfolge der Listeneintr�ge ist nicht definiert.
	 *
	 * @return eine Liste �ber alle values
	 */
	public List<V> values() {
		final List<V> c = new ArrayList<V>(_backingHashMap.size() * 8);
		for(final Collection<V> v : _backingHashMap.values()) {
			c.addAll(v);
		}
		return c;
	}

	/**
	 * Gibt ein Set �ber alle values zur�ck. �nderungen an der zur�ckgegeben Collection haben keine Auswirkungen auf die HashBagMap. Eintr�ge, die mehreren
	 * Keys zugeordnet sind werden mehrfach zur�ckgegeben
	 *
	 * @return eine Liste �ber alle values
	 */
	public Set<V> valueSet() {
		final Set<V> c = new HashSet<V>(_backingHashMap.size() * 8);
		for(final Collection<V> v : _backingHashMap.values()) {
			c.addAll(v);
		}
		return c;
	}

	/**
	 * Gibt ein Set �ber alle Eintr�ge zur�ck
	 *
	 * @return ein Set �ber alle Eintr�ge
	 */
	public Set<Map.Entry<K, Collection<V>>> entrySet() {
		return new EntrySet();
	}

	public String toString() {
		final Iterator<Map.Entry<K, Collection<V>>> i = entrySet().iterator();
		if(!i.hasNext()) return "{}";

		final StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(; ; ) {
			final Map.Entry<K, Collection<V>> e = i.next();
			final K key = e.getKey();
			final Collection<V> value = e.getValue();
			sb.append(key == this ? "(this)" : key);
			sb.append('=');
			sb.append(value == this ? "(this)" : value);
			if(!i.hasNext()) return sb.append('}').toString();
			sb.append(", ");
		}
	}

	/**
	 * Findet Keys, die den angegeben Wert als Value haben
	 *
	 * @param value Wert
	 * @return Collection mit Keys die den angegebenen Werten zugeordnet sind
	 */
	public Set<K> findKey(final V value) {
		return findKeys(Arrays.asList(value));
	}

	/**
	 * Findet Keys, die mindestens einen der angegebenen Werte als Value haben
	 *
	 * @param values Werte
	 * @return Collection mit Keys die den angegebenen Werten zugeordnet sind
	 */
	public Set<K> findKeys(final Collection<V> values) {
		final Set<K> keySet = new HashSet<K>(values.size());
		for(V v : values) {
			for(Map.Entry<K, Collection<V>> entry : _backingHashMap.entrySet()) {
				if(entry.getValue().contains(v)) {
					keySet.add(entry.getKey());
				}
			}
		}
		return keySet;
	}

	public void addAll(final Map<K, Collection<V>> map) {
		for(Map.Entry<K, Collection<V>> entry : map.entrySet()) {
			addAll(entry);
		}
	}

	public void addAll(final HashBagMap<K, V> hashBagMap) {
		for(Map.Entry<K, Collection<V>> entry : hashBagMap.entrySet()) {
			addAll(entry);
		}
	}

	private void addAll(final Map.Entry<K, Collection<V>> entry) {
		addAll(entry.getKey(), entry.getValue());
	}

	private class Itr implements Iterator<Map.Entry<K, V>> {

		private final Iterator<Map.Entry<K, Collection<V>>> _itr;

		private Iterator<V> _itr2 = null;

		private K _k = null;

		public Itr() {
			_itr = _backingHashMap.entrySet().iterator();
		}

		public boolean hasNext() {
			return _itr.hasNext() || (_itr2 != null && _itr2.hasNext());
		}

		public Map.Entry<K, V> next() {
			while(_itr2 == null || !_itr2.hasNext()) {
				final Map.Entry<K, Collection<V>> next = _itr.next();
				_k = next.getKey();
				_itr2 = next.getValue().iterator();
			}
			final V next = _itr2.next();
			return new Entr(next);
		}

		public void remove() {
			_itr2.remove();
		}

		private class Entr implements Map.Entry<K, V> {

			private V _v;

			public Entr(final V v) {
				_v = v;
			}

			public K getKey() {
				return _k;
			}

			public V getValue() {
				return _v;
			}

			public V setValue(final Object value) {
				final V newValue = (V) value;
				HashBagMap.this.remove(_k, _v);
				HashBagMap.this.add(_k, newValue);
				_v = newValue;
				return _v;
			}

			@Override
			public String toString() {
				return _k + "=" + _v;
			}
		}
	}

	private class Col extends AbstractSet<V> {

		private final K _key;

		public Col(final K key) {
			_key = key;
		}

		@Override
		public Iterator<V> iterator() {
			final Collection<V> collection = _backingHashMap.get(_key);
			if(collection == null) return Collections.<V>emptyList().iterator();
			return collection.iterator();
		}

		@Override
		public int size() {
			final Collection<V> c = _backingHashMap.get(_key);
			if(c == null) return 0;
			return c.size();
		}

		@Override
		public boolean add(final V v) {
			HashBagMap.this.add(_key, v);
			return true;
		}

		@Override
		public boolean remove(final Object o) {
			return HashBagMap.this.remove(_key, o);
		}

		@Override
		public boolean addAll(final Collection<? extends V> c) {
			return HashBagMap.this.addAll(_key, c);
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			final Collection<V> collection = _backingHashMap.get(_key);
			return collection != null && collection.removeAll(c);
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			final Collection<V> collection = _backingHashMap.get(_key);
			return collection != null && collection.retainAll(c);
		}
	}


	private class EntrySet extends AbstractSet<Map.Entry<K, Collection<V>>> {

		@Override
		public int size() {
			return _backingHashMap.size();
		}

		@Override
		public boolean add(final Map.Entry<K, Collection<V>> kCollectionEntry) {
			return HashBagMap.this.addAll(kCollectionEntry.getKey(), kCollectionEntry.getValue());
		}

		@Override
		public boolean remove(final Object o) {
			//noinspection deprecation
			return HashBagMap.this.remove(o);
		}

		@Override
		public Iterator<Map.Entry<K, Collection<V>>> iterator() {
			return new EntrySetItr();
		}

		private class EntrySetItr implements Iterator<Map.Entry<K, Collection<V>>> {

			private Iterator<K> _parentItr;

			public EntrySetItr() {
				_parentItr = _backingHashMap.keySet().iterator();
			}

			public boolean hasNext() {
				return _parentItr.hasNext();
			}

			public Map.Entry<K, Collection<V>> next() {
				final K next = _parentItr.next();
				return new Map.Entry<K, Collection<V>>() {
					public K getKey() {
						return next;
					}

					public Collection<V> getValue() {
						return new Col(next);
					}

					public Collection<V> setValue(final Collection<V> value) {
						throw new UnsupportedOperationException("Nicht implementiert");
					}
				};
			}

			public void remove() {
				_parentItr.remove();
			}
		}
	}
}
