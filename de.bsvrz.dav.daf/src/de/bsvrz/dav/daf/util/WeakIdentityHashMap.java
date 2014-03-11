/*
 * Copyright 2013 by Kappich Systemberatung Aachen
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * WeakHashMap, die anhand von Objekt-Identität vergleicht. Nicht Threadsafe.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11572 $
 */
public class WeakIdentityHashMap<K, V> implements Map<K, V> {

	private final ReferenceQueue<K> _refQueue = new ReferenceQueue<K>();
	private Map<IdentityWeakReference<K>, V> _innerMap = new HashMap<IdentityWeakReference<K>, V>();

	private void removeQueuedReferences() {
		while(true){
			Reference<? extends K> ref = _refQueue.poll();
			if(ref == null) return;
			_innerMap.remove(ref);
		}
	}

	@Override
	public int size() {
		removeQueuedReferences();
		return _innerMap.size();
	}

	@Override
	public boolean isEmpty() {
		removeQueuedReferences();
		return _innerMap.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		removeQueuedReferences();
		return _innerMap.containsKey(wrap(key));
	}

	private IdentityWeakReference<K> wrap(final Object key) {
		return new IdentityWeakReference<K>((K) key, _refQueue);
	}

	@Override
	public boolean containsValue(final Object value) {
		removeQueuedReferences();
		return _innerMap.containsValue(value);
	}

	@Override
	public V get(final Object key) {
		removeQueuedReferences();
		return _innerMap.get(wrap(key));
	}

	@Override
	public V put(final K key, final V value) {
		removeQueuedReferences();
		return _innerMap.put(wrap(key), value);
	}

	@Override
	public V remove(final Object key) {
		removeQueuedReferences();
		return _innerMap.remove(wrap(key));
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		_innerMap.clear();
	}

	@Override
	public Set<K> keySet() {
		removeQueuedReferences();
		HashSet<K> result = new HashSet<K>();
		for(IdentityWeakReference<K> weakReference : _innerMap.keySet()) {
			K k = weakReference.get();
			if(k != null) {
				result.add(k);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Collection<V> values() {
		removeQueuedReferences();
		return _innerMap.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		removeQueuedReferences();
		HashSet<Entry<K, V>> result = new HashSet<Entry<K, V>>();
		for(final Entry<IdentityWeakReference<K>, V> entry : _innerMap.entrySet()) {
			final K k = entry.getKey().get();
			if(k != null) {
				result.add(new Entry<K, V>(){
					@Override
					public K getKey() {
						return k;
					}

					@Override
					public V getValue() {
						return entry.getValue();
					}

					@Override
					public V setValue(final V value) {
						return entry.setValue(value);
					}
				});
			}
		}
		return Collections.unmodifiableSet(result);
	}

	private static class IdentityWeakReference<T> extends WeakReference<T>{
		private int _hashCode;

		private IdentityWeakReference(final T referent, final ReferenceQueue<? super T> q) {
			super(referent, q);
			_hashCode = System.identityHashCode(referent);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			IdentityWeakReference<?> ref = (IdentityWeakReference<?>)obj;
			if (this.get() == ref.get()) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return _hashCode;
		}
	}
}
