/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb;

import java.util.*;

/**
 * Map fuer das Mapping von Objekt-IDs auf Objekte. Wird im DataIdentTree benutzt. Erlaubt die Implementierung eigener Hashing-Strategien in Abhaengigkeit von
 * der Map-Groesse.
 *
 * @author beck et al. projects GmbH
 * @author Thomas Schaefer
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class IDMap<K, V> implements Map<K, V> {

	private Map<K, V> hashMap;

	/** Erzeugt eine neue IDMap. */
	public IDMap() {
		// Das Backup der IDMap erfolgt in der aktuellen Implementierung durch eine HashMap.
		// Die HashMap ist synchronisiert, damit die Erstellung einer Map-Kopie sicher ist,
		// sofern die Iteration ueber diese Map in einem Block erfolgt, der auf die Map
		// synchronisiert. Siehe getCopy() und Collections.synchronizedMap().
		hashMap = Collections.synchronizedMap(new HashMap<K, V>());
	}

	/* (non-Javadoc)
		 * @see java.util.Map#size()
		 */
	public int size() {
		return hashMap.size();
	}

	/* (non-Javadoc)
		 * @see java.util.Map#isEmpty()
		 */
	public boolean isEmpty() {
		return hashMap.isEmpty();
	}

	/* (non-Javadoc)
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
	public boolean containsKey(Object key) {
		return hashMap.containsKey(key);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#containsValue(java.lang.Object)
		 */
	public boolean containsValue(Object value) {
		return hashMap.containsValue(value);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#get(java.lang.Object)
		 */
	public V get(Object key) {
		return hashMap.get(key);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#put(K, V)
		 */
	public V put(K key, V value) {
		return hashMap.put(key, value);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#remove(java.lang.Object)
		 */
	public V remove(Object key) {
		return hashMap.remove(key);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#putAll(java.util.Map)
		 */
	public void putAll(Map<? extends K, ? extends V> t) {
		hashMap.putAll(t);
	}

	/* (non-Javadoc)
		 * @see java.util.Map#clear()
		 */
	public void clear() {
		hashMap.clear();
	}

	/* (non-Javadoc)
		 * @see java.util.Map#keySet()
		 */
	public Set<K> keySet() {
		return hashMap.keySet();
	}

	/* (non-Javadoc)
		 * @see java.util.Map#values()
		 */
	public Collection<V> values() {
		return hashMap.values();
	}

	/* (non-Javadoc)
		 * @see java.util.Map#entrySet()
		 */
	public Set<Entry<K, V>> entrySet() {
		return hashMap.entrySet();
	}

	/**
	 * Liefert eine Kopie der Map als {@link HashMap}. Erstellung der Kopie ist threadsafe.
	 *
	 * @return Map-Kopie
	 */
	public HashMap<K, V> getCopy() {
		synchronized(hashMap) {
			return new HashMap<K, V>(hashMap);
//			HashMap<K, V> m = new HashMap<K, V>(hashMap.size());
//			for(K key : hashMap.keySet()) {
//				m.put(key, hashMap.get(key));
//			}
//			return m;
		}
	}
}
