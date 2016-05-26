/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spezielle Map-implementierung, die dür die ID-Systemobjekt-Map im DafDataModel verwendet wird.
 *
 * Im Gegensatz zu einer normalen Map bietet sie die Möglichkeit, Bestimmte Werte bei Bedarf durch eine WeakReference zu ersetzen
 * (ggf. nach einem Timeout).
 *
 * Außerdem verkleinert sich die Map automatisch (rehash) wenn die Kapazität der Map mehr als 3 mal so groß ist, wie die aktuelle
 * Elementanzahl.
 *
 * Diese Klasse ist Threadsafe.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class AutoExpireMap<K, V> {

	private ConcurrentHashMap<K, Object> _delegate = new ConcurrentHashMap<K, Object>();
	private ReferenceQueue<V> _queue = new ReferenceQueue<V>();
	private static final Timer _timer = new Timer(true);
	private int _maxSize = 0;

	/**
	 * Leert die Map
	 */
	public synchronized void clear() {
		update();
		_delegate.clear();
	}

	/**
	 * Entfernt einen Eintrag sofort
	 * @param key Key
	 * @return Bisher gespeicherter Wert oder null
	 */
	public synchronized V remove(final Object key) {
		update();
		Object ref = _delegate.remove(key);
		return unpack(ref);
	}

	/**
	 * Löst bei Bedarf WeakReferences auf, die gemischt mit normalen Objekten in der Map gespeichert sind
	 * @param ref Objekt oder Weakreference
	 * @return Ausgepacktes Objekt
	 */
	private V unpack(final Object ref) {
		if(ref == null) return null;
		if(ref instanceof MyReference) {
			MyReference myReference = (MyReference) ref;
			return (V) myReference.get();
		}
		return (V) ref;
	}

	/**
	 * @see Map#put(Object, Object)
	 */
	public synchronized V put(final K key, final V value) {
		update();
		if(value == null){
			return remove(key);
		}
		Object ref = _delegate.put(key, value);
		return unpack(ref);
	}

	/**
	 * @see Map#get(Object)
	 */
	public synchronized V get(final Object key) {
		update();
		Object ref = _delegate.get(key);
		return unpack(ref);
	}

	/**
	 * @see Map#containsKey(Object)
	 */
	public synchronized boolean containsKey(final Object key) {
		return get(key) != null;
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public synchronized boolean isEmpty() {
		update();
		return _delegate.isEmpty();
	}

	/**
	 * @see java.util.Map#size() ()
	 */
	public synchronized int size() {
		return _delegate.size();
	}

	/**
	 * Arbeitet die ReferenceQueue ab um abgeräumte Einträge zu entfernen, Triggert falls nötig einen rehash()
	 */
	private void update() {
		while (true) {
			MyReference ref = (MyReference) _queue.poll();
			if(ref == null) break;
			K key = (K) ref.getKey();
			if(_delegate.get(key) == ref){
				_delegate.remove(key);
			}
		}
		int size = size();
		_maxSize = Math.max(_maxSize, size);
		if(_maxSize > size * 3){
			rehash();
		}
	}

	private void rehash() {
		// Muss synchronisiert ausgeführt werden
		assert Thread.holdsLock(this);

		_delegate = new ConcurrentHashMap<K, Object>(_delegate);
		_maxSize = _delegate.size();
	}

	/**
	 * Gibt alle Keys zurück. Da die values durch WeakReferences referneziert werden können, gibt es möglicherweise nicht zu jedem key einen Wert.
	 * @return Keys
	 */
	public synchronized Set<K> keySet() {
		update();
		return Collections.unmodifiableSet(_delegate.keySet());
	}

	/**
	 * Gibt eine Kopie der enthaltenen Werte zurück.
	 * @return
	 */
	public synchronized Collection<V> values() {
		final List<V> result = new ArrayList<V>(size());
		for(Object o : _delegate.values()) {
			V value = unpack(o);
			if(value != null){
				result.add(value);
			}
		}
		return result;
	}

	/**
	 * Sorgt dafür, dass ein Eintrag nach einer Zeit durch eine WeakReference ersetzt wird
	 * @param key Key des Eintrags
	 * @param val Wert des Eintrags (muss angegeben werden um sicherzustellen, dass nicht zwischenzeitlich der Wert geändert wurde und dann
	 *            irrtümlich entfernt wird, vgl. compareAndSwap-Technik)
	 * @param timeout Anzahl Millisekunden, nach der der Eintrag ersetzt wird (> 0)
	 */
	public synchronized void expire(final K key, final V val, final long timeout) {
		if(val == null) return;
		_timer.schedule(new TimerTask() {
			                @Override
			                public void run() {
				                expireNow(key, val);
			                }
		                }, timeout);
	}

	/**
	 * Sorgt dafür, dass ein Eintrag sofort durch eine WeakReference ersetzt wird
	 * @param key Key des Eintrags
	 * @param val Wert des Eintrags (muss angegeben werden um sicherzustellen, dass nicht zwischenzeitlich der Wert geändert wurde und dann
	 *            irrtümlich entfernt wird, vgl. compareAndSwap-Technik)
	 */
	public synchronized void expireNow(final K key, final V val) {
		Object ref = _delegate.get(key);
		if(val != null && val.equals(ref)){
			_delegate.replace(key, val, new MyReference(key, val, _queue));
		}
	}

	/**
	 * WeakReference-Implementierung, die sich den Key merkt
	 */
	private static class MyReference extends WeakReference<Object> {
		private final Object _key;

		public MyReference(final Object key, final Object value, final ReferenceQueue queue) {
			super(value, queue);
			_key = key;
		}

		public Object getKey() {
			return _key;
		}
	}
}
