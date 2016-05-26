/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.concurrent;

/**
 * Queue zum Austausch von Nachrichten zwischen Threads. Mit der Methode {@link #put} können beliebige Objekte
 * (Nachrichten) in die Queue eingetragen (gesendet) werden und i.a. von einem anderen Thread mit den Methode {@link
 * #take} aus der Queue entnommen (empfangen) werden. Die Anzahl der Nachrichten in der Queue ist
 * beschränkt. Die Nachrichten werden in der Reihenfolge empfangen in der sie versendet wurden (first in first
 * out, FIFO). Gesendete Nachrichten stehen erst dann empfangsseitig zur Verfügung, wenn die halbe Kapizität der Queue
 * erreicht ist oder die Methode {@link #flush} aufgerufen wurde.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class BufferedQueue<E> {

	private Object[] _putObjects;
	private Object[] _takeObjects;
	private int _putIndex = 0;
	private int _takeIndex = 0;
	private int _takeLimit = 0;
	private final int _capacity;
	private final Object _transferLock = new Object();
	private int _transferSize = 0;
	private Object[] _transferObjects = null;
	private Object[] _recycleArray;

	public BufferedQueue(int capacity) {
		_capacity = capacity / 2 + 1;
		_putObjects = new Object[_capacity];
		_takeObjects = new Object[_capacity];
		_recycleArray = new Object[_capacity];
	}

	public void flush() throws InterruptedException {
		if(_putIndex > 0) {
			synchronized(_transferLock) {
				while(_transferObjects != null) _transferLock.wait();
				_transferObjects = _putObjects;
				_transferSize = _putIndex;
				_putIndex = 0;
				_putObjects = _recycleArray;
				_recycleArray = null;
				_transferLock.notify();
			}
		}
	}

	/**
	 * Sendet eine Nachricht in die Queue.
	 *
	 * @param message Zu versendende Nachricht.
	 */
	public void put(E message) throws InterruptedException {
		if(_putIndex == _capacity) {
			flush();
		}
		_putObjects[_putIndex++] = message;
	}

	/**
	 * Empfängt und entfernt eine Nachricht aus der Queue. Wenn die Queue leer ist, dann wartet die Methode, bis eine
	 * Nachricht in die Queue gesendet wurde. Wenn mehrere Nachrichten in der Queue vorhanden sind, wird die Nachricht
	 * empfangen und entfernt, die als erstes in die Queue gesendet wurde.
	 *
	 * @return Empfangene Nachricht.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde während auf Nachrichten gewartet wurde.
	 */
	public E take() throws InterruptedException {
		if(_takeIndex == _takeLimit) {
			synchronized(_transferLock) {
				while(_transferObjects == null) _transferLock.wait();
				_takeIndex = 0;
				_takeLimit = _transferSize;
				_recycleArray = _takeObjects;
				_takeObjects = _transferObjects;
				_transferObjects = null;
				_transferLock.notify();
			}
		}
		final E taken = (E)_takeObjects[_takeIndex];
		_takeObjects[_takeIndex++] = null;
		return taken;
	}

}
