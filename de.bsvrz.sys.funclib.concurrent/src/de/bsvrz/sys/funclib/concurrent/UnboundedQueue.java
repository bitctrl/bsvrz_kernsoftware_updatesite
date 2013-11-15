/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.concurrent;

import java.util.LinkedList;

/**
 * Queue zum Austausch von Nachrichten zwischen Threads. Mit der Methode {@link #put} k�nnen beliebige Objekte
 * (Nachrichten) in die Queue eingetragen (gesendet) werden und i.a. von einem anderen Thread mit den Methoden {@link
 * #take} oder {@link #poll} aus der Queue entnommen (empfangen) werden. Die generische Queue kann durch Angabe eines
 * Typs auf diesen bestimmten Objekttypen arbeiten. Die Anzahl der Nachrichten in der Queue ist nicht beschr�nkt. Die
 * Nachrichten werden in der Reihenfolge empfangen in der sie versendet wurden (first in first out, FIFO).
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5006 $
 */
public class UnboundedQueue <E> {

	private final LinkedList<E> _list = new LinkedList<E>();

	/**
	 * Sendet eine Nachricht an die Queue.
	 *
	 * @param message Zu versendende Nachricht.
	 */
	public void put(E message) {
		synchronized (_list) {
			_list.addFirst(message);
			_list.notify();
		}
	}

	/**
	 * Empf�ngt und entfernt eine Nachricht aus der Queue. Wenn die Queue leer ist, dann wartet die Methode, bis eine
	 * Nachricht in die Queue gesendet wurde. Wenn mehrere Nachrichten in der Queue vorhanden sind, wird die Nachricht
	 * empfangen und entfernt, die als erstes in die Queue gesendet wurde.
	 *
	 * @return Empfangene Nachricht.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde w�hrend auf Nachrichten gewartet wurde.
	 */
	public E take() throws InterruptedException {
		synchronized (_list) {
			while (_list.isEmpty()) _list.wait();
			return _list.removeLast();
		}
	}

	/**
	 * Empf�ngt und entfernt eine Nachricht aus der Queue. Wenn die Queue leer ist, dann wartet die Methode, bis eine
	 * Nachricht in die Queue gesendet wurde oder bis die angegebene Zeit verstrichen ist. Wenn mehrere Nachrichten in der
	 * Queue vorhanden sind, wird diejenige Nachricht empfangen und entfernt, die als erstes in die Queue gesendet wurde.
	 *
	 * @param timeout Zeit in Millisekunden, f�r die bei leerer Queue auf neue Nachrichten gewartet wird.
	 * @return Empfangene Nachricht oder <code>null</code>, wenn nach Ablauf des Timeouts immer noch keine Nachricht in der
	 *         Queue vorhanden ist.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde w�hrend auf Nachrichten gewartet wurde.
	 */
	public E poll(long timeout) throws java.lang.InterruptedException {
		long maxWaitTime = System.currentTimeMillis() + timeout;
		synchronized (_list) {
			long maxSleep = maxWaitTime - System.currentTimeMillis();
			while (_list.isEmpty() && maxSleep > 0) {
				_list.wait(maxSleep);
				maxSleep = maxWaitTime - System.currentTimeMillis();
			}
			if (_list.isEmpty()) return null;
			return _list.removeLast();
		}
	}

	/**
	 * Bestimmt die Gr��e dieser Queue.
	 *
	 * @return Anzahl verbleibender Elemente in dieser Queue.
	 */
	public int size() {
		synchronized (_list) {
			return _list.size();
		}
	}
}
