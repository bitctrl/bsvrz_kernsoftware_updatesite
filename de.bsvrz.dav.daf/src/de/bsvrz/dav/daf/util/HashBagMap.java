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

import java.io.Serializable;
import java.util.*;

/**
 * Klasse, die zu einem Key mehrere Values zuordnen kann. Unter jedem Key wird ein Set gespeichert, sodass pro Key das
 * gleiche Objekt maximal einmal gespeichert wird. Dies kann durch den Optionalen useSet-parameter geändert werden,
 * wodurch dann eine Liste verwendet wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
@SuppressWarnings({"unchecked"})
public class HashBagMap<K, V> implements Serializable {

	private static final long serialVersionUID = 4495094277066988824L;
	public static final int HASHSET_THRESHOLD = 10;

	private final HashMap<K, Collection<V>> _backingHashMap;

	private final boolean _useSet;

	/**
	 * Erstellt eine HashBagMap, die zu einem Key mehrere Values speichert, und für die Speicherung der Values ein
	 * {@link Set} verwendet, sodass pro Key jedes Objekt nur einmal gespeichert wird.
	 */
	public HashBagMap() {
		this(8, true);
	}
	/**
	 * Erstellt eine HashBagMap, die zu einem Key mehrere Values speichert. Über den useSet-parameter kann festgelegt werden,
	 * ob für die Speicherung der Values intern ein Set (keine doppelten Values pro Key) oder eine Liste (doppelte Values pro Key möglich,
	 * ggf. etwas speichersparender) verwendet wird.
	 *
	 * @param useSet ob ein Set verwendet werden soll
	 */
	public HashBagMap(boolean useSet) {
		this(8, useSet);
	}

	/**
	 * Erstellt eine HashBagMap, die zu einem Key mehrere Values speichert, und für die Speicherung der Values ein
	 * {@link Set} verwendet, sodass pro Key jedes Objekt nur einmal gespeichert wird.
	 * @param initialCapacity Initiale Key-Kapazität
	 */
	public HashBagMap(final int initialCapacity) {
		this(initialCapacity, true);
	}
	/**
	 * Erstellt eine HashBagMap, die zu einem Key mehrere Values speichert. Über den useSet-parameter kann festgelegt werden,
	 * ob für die Speicherung der Values intern ein Set (keine doppelten Values pro Key) oder eine Liste (doppelte Values pro Key möglich,
	 * ggf. etwas speichersparender) verwendet wird.
	 *
	 * @param useSet ob ein Set verwendet werden soll
	 * @param initialCapacity Initiale Key-Kapazität
	 */
	public HashBagMap(final int initialCapacity, boolean useSet) {
		_useSet = useSet;
		_backingHashMap = new LinkedHashMap<K, Collection<V>>(initialCapacity);
	}

	/**
	 * Gibt die Anzahl der gespeicherten Werte (Values) zurück
	 * @return die Anzahl der gespeicherten Werte (Values)
	 */
	public int size() {
		return values().size();
	}

	/**
	 * Gibt true zurück, wenn keine Werte gespeichert sind
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

	/**
	 * Gibt <tt>true</tt> zurück, wenn ein Objekt vom Typ Map.Entry enthalten ist, also hier dem Entry-Key mindestens das Entry-Value zugeordnet ist.
	 * @return <tt>true</tt>, wenn ein Objekt vom Typ Map.Entry enthalten ist, sonst <tt>false</tt>
	 */
	public boolean contains(final Object o) {
		if(o instanceof Map.Entry) {
			final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			final Collection<V> v = get(entry.getKey());
			return v != null && v.contains(entry.getValue());
		}
		return false;
	}

	/**
	 * Fügt einen Entry hinzu. Shortcut für add(entry.getKey(), entry.getValue()).
	 * @param entry Entry
	 * @return siehe {@link #add(Object, Object)}
	 */
	public boolean add(final Map.Entry<K, V> entry) {
		return add(entry.getKey(), entry.getValue());
	}

	/**
	 * Entfernt ein Map.Entry<K, V> aus der HashBagMap. Entspricht remove(entry.getKey(), entry.getValue). Tut nichts und gibt false
	 * zurück, falls o kein Map.Entry ist.
	 */
	private boolean remove(final Object o) {
		if(o instanceof Map.Entry) {
			final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			return remove(entry.getKey(), entry.getValue());
		}
		return false;
	}

	/**
	 * Gibt true zurück, wenn die Klasse zu diesem Key mindestens einen Wert enthält
	 *
	 * @param key Key
	 * @return true wenn die Klasse zu diesem key mindestens einen Wert enthält
	 */
	public boolean containsKey(final Object key) {
		final Collection<V> v = _backingHashMap.get(key);
		return v != null && v.size() > 0;
	}

	/**
	 * Gibt true zurück, wenn dieser Wert mindestens einmal einem Key zugeordnet wurde
	 *
	 * @param value Wert
	 * @return true, wenn dieser Wert mindestens einmal einem Key zugeordnet wurde
	 */
	public boolean containsValue(final Object value) {
		return values().contains(value);
	}

	/**
	 * Gibt die Collection mit Objekten zurück, die hinter diesem Key liegen
	 *
	 * @param key Key
	 * @return Liste mit Objekten
	 */
	public Collection<V> get(final K key) {
		return new Col(key);
	}

	/**
	 * Fügt einem Key einen Wert hinzu
	 *
	 * @param key   Key
	 * @param value Wert
	 * @return true wenn die Collection verändert wurde
	 */
	public boolean add(final K key, final V value) {
		final Collection<V> v = _backingHashMap.get(key);
		if(v == null) {
			final Collection<V> collection = createNewInstance(1);
			collection.add(value);
			_backingHashMap.put(key, collection);
			return true;
		}
		else {
			return checkAdd(key, v, value);
		}
	}

	/**
	 * Fügt einem Key mehrere Werte hinzu
	 *
	 * @param key   Key
	 * @param value Werte
	 * @return true wenn die Collection verändert wurde
	 */
	public boolean addAll(final K key, final Collection<? extends V> value) {
		if(value.size() == 0) return false;
		final Collection<V> v = _backingHashMap.get(key);
		if(v == null) {
			final Collection<V> collection = createNewInstance(value.size());
			collection.addAll(value);
			_backingHashMap.put(key, collection);
			return collection.size() > 0;
		}
		else {
			return checkAdd(key, v, value);
		}
	}

	private boolean checkAdd(final K key, final Collection<V> col, final V add) {
		Collection<V> set = col;
		if(isMini(col) && col.size() + 1 > HASHSET_THRESHOLD) {
			set = createNewInstance(col.size() + 1);
			set.addAll(col);
			set.add(add);
			_backingHashMap.put(key, set);
		}
		return set.add(add);
	}

	private boolean checkAdd(final K key, final Collection<V> col, final Collection<? extends V> add) {
		Collection<V> set = col;
		if(isMini(col) && col.size() + add.size() > HASHSET_THRESHOLD) {
			set = createNewInstance(col.size() + 1);
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
		if(_useSet) {
			if(size < HASHSET_THRESHOLD) {
				return new MiniSet<V>();
			}
			else {
				return new LinkedHashSet<V>(size);
			}
		}
		return new ArrayList<V>(size);
	}

	private boolean isMini(final Collection<V> col) {
		if(_useSet) return col instanceof MiniSet;
		return false;
	}

	/**
	 * Löscht einen Eintrag
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
	 * Löscht alle Einträge eines Keys
	 *
	 * @param key Key
	 * @return true falls ein Wert entfernt wurde
	 */
	public Collection<V> removeAll(final Object key) {
		Collection<V> remove = _backingHashMap.remove(key);
		return remove == null ? Collections.<V>emptyList() : remove;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		_backingHashMap.clear();
	}

	/**
	 * Gibt ein Set über die Keys zurück
	 *
	 * @return ein Set über die Keys
	 */
	public Set<K> keySet() {
		return _backingHashMap.keySet();
	}

	/**
	 * Gibt eine Liste über alle values zurück. Änderungen an der zurückgegeben Collection haben keine Auswirkungen auf die
	 * HashBagMap. Einträge, die mehreren Keys zugeordnet sind werden mehrfach zurückgegeben. Die Reihenfolge der
	 * Listeneinträge ist nicht definiert.
	 *
	 * @return eine Liste über alle values
	 */
	public List<V> values() {
		final List<V> c = new ArrayList<V>(_backingHashMap.size() * 8);
		for(final Collection<V> v : _backingHashMap.values()) {
			c.addAll(v);
		}
		return c;
	}

	/**
	 * Gibt ein Set über alle values zurück. Änderungen an der zurückgegeben Collection haben keine Auswirkungen auf die
	 * HashBagMap.
	 *
	 * @return eine Liste über alle values
	 */
	public Set<V> valueSet() {
		final Set<V> c = new HashSet<V>(_backingHashMap.size() * 8);
		for(final Collection<V> v : _backingHashMap.values()) {
			c.addAll(v);
		}
		return c;
	}

	/**
	 * Gibt ein Set über alle Einträge zurück
	 *
	 * @return ein Set über alle Einträge
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
	 * Findet Keys, die mindestens den angegeben Wert als Value haben
	 *
	 * @param value Wert
	 * @return Collection mit Keys die den angegebenen Werten zugeordnet sind
	 */
	public Set<K> findKey(final V value) {
		return findKeys(Arrays.asList(value));
	}

	/**
	 * Findet Keys, die mindestens einen der angegebenen Werte als (nicht unbedingt einzigen) Value haben
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

					@Override
					public String toString() {
						return "{" + getKey() + ", " + getValue() + "}";
					}
				};
			}

			public void remove() {
				_parentItr.remove();
			}
		}
	}
}
