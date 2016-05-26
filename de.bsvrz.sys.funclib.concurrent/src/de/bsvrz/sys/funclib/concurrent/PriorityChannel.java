/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung, Aachen
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

import java.util.*;

/**
 * Diese Klasse implementiert einen größenbeschränkten Kommunikationskanal zum Datenaustausch zwischen Threads. Es
 * stehen Funktionen zur Verfügung mit den Objekte in den Kommunikationskanal übertragen werden bzw. aus dem
 * Kommunikationskanal ausgelesen werden. Wenn die gewünschte Funktion nicht durchgeführt werden kann, weil der Kanal
 * voll bzw. leer ist, dann blockiert die Funktion, bis die Funktion erfolgreich durchgeführt wurde oder bis eine
 * vorgegebene Zeit verstrichen ist. Der Kommunikationskanal ist priorisiert, d.h. es können Objekte verschiedener
 * Prioritätsklassen ausgetauscht werden, wobei die Objekte mit der kleinsten Prioritätsklasse bevorzugt behandelt
 * werden. Objekte der gleichen Prioritätsklasse werden in der Reihenfolge ausgelesen in der sie in den
 * Kommunikationskanal übertragen wurden (im Gegensatz zu Heap basierten Implementierungen). Der Aufwand des Auslesens
 * von Objekten ist proportional zur Anzahl verschiedener Prioritätsklassen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class PriorityChannel {
	private final int _numberOfPriorityClasses;
	private final int _capacityPerPriorityClass;
	private final LinkedList[] _queues;
	private int _count = 0;

	/**
	 * Legt einen neuen Kommunikationskanal an.
	 *
	 * @param numberOfPriorityClasses  Anzahl der zu verwendenden Prioritätsklassen. Diese sind von <code>0</code> bis
	 *                                 <code>numberOfPriorityClasses-1</code> durchnummeriert.
	 * @param capacityPerPriorityClass Maximale Anzahl der pro Prioritätsklasse im Kommunikationskanal gespeicherten
	 *                                 Objekte.
	 */
	public PriorityChannel(int numberOfPriorityClasses, int capacityPerPriorityClass) {
		_numberOfPriorityClasses = numberOfPriorityClasses;
		_capacityPerPriorityClass = capacityPerPriorityClass;
		_queues = new LinkedList[numberOfPriorityClasses];
		for(int i = 0; i < numberOfPriorityClasses; ++i) {
			_queues[i] = new LinkedList();
		}
	}

	/**
	 * Überträgt eine Nachricht in den Kommunikationskanal. Wenn die Kapazität des Kommunikationskanals in der jeweiligen
	 * Prioritätsklasse der Nachricht erschöpft ist, dann blockiert die Methode, bis wieder Platz vorhanden ist und das
	 * Objekt in den Kommunikationskanal übertragen wurde.
	 *
	 * @param item Objekt, das in den Kommunikationskanal übertragen werden soll.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während auf freien Platz im Kommunikationskanal
	 *                              gewartet wurde.
	 */
	public void put(PriorizedObject item) throws InterruptedException {
		int priorityClass = item.getPriorityClass();
		if(priorityClass < 0 || priorityClass >= _numberOfPriorityClasses) {
			throw new IllegalArgumentException("Prioritätsklasse muss im Bereich [0 und " + _numberOfPriorityClasses + ") liegen, ist: " + priorityClass);
		}
		LinkedList queue = _queues[priorityClass];
		synchronized(queue) {
			while(queue.size() >= _capacityPerPriorityClass) queue.wait();
			queue.addLast(item);
		}
		synchronized(_queues) {
			_queues.notify();
			++_count;
		}

	}

	/**
	 * Überträgt eine Nachricht in den Kommunikationskanal. Wenn die Kapazität des Kommunikationskanals in der jeweiligen
	 * Prioritätsklasse der Nachricht erschöpft ist, dann blockiert die Methode, bis wieder Platz vorhanden ist und das
	 * Objekt in den Kommunikationskanal übertragen wurde oder bis die angegebene Timeout-Zeit verstrichen ist.
	 *
	 * @param item    Objekt, das in den Kommunikationskanal übertragen werden soll.
	 * @param timeout Zeit in Millisekunden, für die bei vollem Kommunikationskanal maximal auf freien Platz gewartet
	 *                wird.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während auf freien Platz im Kommunikationskanal
	 *                              gewartet wurde.
	 */
	public boolean offer(PriorizedObject item, long timeout) throws InterruptedException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Empfängt und entfernt eine Nachricht aus dem Kommunikationskanal. Wenn der Kommunikationskanal leer ist, dann wartet
	 * die Methode, bis eine Nachricht in den Kommunikationskanal gesendet wurde. Der Kommunikationskanal ist priorisiert,
	 * d.h. es können Objekte verschiedener Prioritätsklassen ausgetauscht werden, wobei die Objekte mit der kleinsten
	 * Prioritätsklasse bevorzugt behandelt werden. Objekte der gleichen Prioritätsklasse werden in der Reihenfolge
	 * ausgelesen in der sie in den Kommunikationskanal übertragen wurden (im Gegensatz zu Heap basierten
	 * Implementierungen). Der Aufwand des Auslesens von Objekten ist proportional zur Anzahl verschiedener
	 * Prioritätsklassen.
	 *
	 * @return Empfangene Nachricht.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während auf Nachrichten gewartet wurde.
	 */
	public PriorizedObject take() throws InterruptedException {
		synchronized(_queues) {
			while(_count <= 0) _queues.wait();
			--_count;
		}
		for(int i = 0; i < _numberOfPriorityClasses; ++i) {
			LinkedList queue = _queues[i];
			synchronized(queue) {
				int queueSize = queue.size();
				if(queueSize != 0) {
					queue.notify();
					return (PriorizedObject)queue.removeFirst();
				}
			}
		}
		throw new IllegalStateException("kein Objekt gefunden");
	}

	/**
	 * Empfängt und entfernt eine Nachricht aus dem Kommunikationskanal. Wenn der Kommunikationskanal leer ist, dann wartet
	 * die Methode, bis eine Nachricht in den Kommunikationskanal gesendet wurde oder bis die angegebene Zeit verstrichen
	 * ist. Der Kommunikationskanal ist priorisiert, d.h. es können Objekte verschiedener Prioritätsklassen ausgetauscht
	 * werden, wobei die Objekte mit der kleinsten Prioritätsklasse bevorzugt behandelt werden. Objekte der gleichen
	 * Prioritätsklasse werden in der Reihenfolge ausgelesen in der sie in den Kommunikationskanal übertragen wurden (im
	 * Gegensatz zu Heap basierten Implementierungen). Der Aufwand des Auslesens von Objekten ist proportional zur Anzahl
	 * verschiedener Prioritätsklassen.
	 *
	 * @param timeout Zeit in Millisekunden, für die bei leerem Kommunikationskanal auf neue Nachrichten gewartet wird.
	 * @return Empfangene Nachricht oder <code>null</code>, wenn nach Ablauf des Timeouts immer noch keine Nachricht im
	 *         Kommunikationskanal vorhanden ist.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während auf Nachrichten gewartet wurde.
	 */
	public PriorizedObject poll(long timeout) throws InterruptedException {
		long maxWaitTime = System.currentTimeMillis() + timeout;
		synchronized(_queues) {
			long maxSleep = maxWaitTime - System.currentTimeMillis();
			while(_count <= 0 && maxSleep > 0) {
				_queues.wait(maxSleep);
				maxSleep = maxWaitTime - System.currentTimeMillis();
			}
			if(_count <= 0) return null;
			--_count;
		}
		for(int i = 0; i < _numberOfPriorityClasses; ++i) {
			LinkedList queue = _queues[i];
			synchronized(queue) {
				int queueSize = queue.size();
				if(queueSize != 0) {
					queue.notify();
					return (PriorizedObject)queue.removeFirst();
				}
			}
		}
		throw new IllegalStateException("kein Objekt gefunden");
	}

	/**
	 * Bestimmt, ob der Kommunikationskanal leer ist.
	 *
	 * @return <code>true</code> bei leerem Kommunikationskanal, sonst <code>false</code>.
	 */
	public boolean isEmpty() {
		synchronized(_queues) {
			return _count <= 0;
		}
	}

	/**
	 * Löscht alle im Kommunikationskanal vorhandenen Nachrichten.
	 */
	public void clear() {
		synchronized(_queues) {
			try {
				while(poll(0) != null) ;
			}
			catch(InterruptedException e) {
				throw new RuntimeException("poll mit timeout von 0 darf kein wait aufrufen", e);
			}
		}
	}


}
