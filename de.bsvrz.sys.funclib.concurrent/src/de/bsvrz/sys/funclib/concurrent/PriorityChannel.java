/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kni� Systemberatung, Aachen
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
 * Diese Klasse implementiert einen gr��enbeschr�nkten Kommunikationskanal zum Datenaustausch zwischen Threads. Es
 * stehen Funktionen zur Verf�gung mit den Objekte in den Kommunikationskanal �bertragen werden bzw. aus dem
 * Kommunikationskanal ausgelesen werden. Wenn die gew�nschte Funktion nicht durchgef�hrt werden kann, weil der Kanal
 * voll bzw. leer ist, dann blockiert die Funktion, bis die Funktion erfolgreich durchgef�hrt wurde oder bis eine
 * vorgegebene Zeit verstrichen ist. Der Kommunikationskanal ist priorisiert, d.h. es k�nnen Objekte verschiedener
 * Priorit�tsklassen ausgetauscht werden, wobei die Objekte mit der kleinsten Priorit�tsklasse bevorzugt behandelt
 * werden. Objekte der gleichen Priorit�tsklasse werden in der Reihenfolge ausgelesen in der sie in den
 * Kommunikationskanal �bertragen wurden (im Gegensatz zu Heap basierten Implementierungen). Der Aufwand des Auslesens
 * von Objekten ist proportional zur Anzahl verschiedener Priorit�tsklassen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5006 $
 */
public class PriorityChannel {
	private final int _numberOfPriorityClasses;
	private final int _capacityPerPriorityClass;
	private final LinkedList[] _queues;
	private int _count = 0;

	/**
	 * Legt einen neuen Kommunikationskanal an.
	 *
	 * @param numberOfPriorityClasses  Anzahl der zu verwendenden Priorit�tsklassen. Diese sind von <code>0</code> bis
	 *                                 <code>numberOfPriorityClasses-1</code> durchnummeriert.
	 * @param capacityPerPriorityClass Maximale Anzahl der pro Priorit�tsklasse im Kommunikationskanal gespeicherten
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
	 * �bertr�gt eine Nachricht in den Kommunikationskanal. Wenn die Kapazit�t des Kommunikationskanals in der jeweiligen
	 * Priorit�tsklasse der Nachricht ersch�pft ist, dann blockiert die Methode, bis wieder Platz vorhanden ist und das
	 * Objekt in den Kommunikationskanal �bertragen wurde.
	 *
	 * @param item Objekt, das in den Kommunikationskanal �bertragen werden soll.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, w�hrend auf freien Platz im Kommunikationskanal
	 *                              gewartet wurde.
	 */
	public void put(PriorizedObject item) throws InterruptedException {
		int priorityClass = item.getPriorityClass();
		if(priorityClass < 0 || priorityClass >= _numberOfPriorityClasses) {
			throw new IllegalArgumentException("Priorit�tsklasse muss im Bereich [0 und " + _numberOfPriorityClasses + ") liegen, ist: " + priorityClass);
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
	 * �bertr�gt eine Nachricht in den Kommunikationskanal. Wenn die Kapazit�t des Kommunikationskanals in der jeweiligen
	 * Priorit�tsklasse der Nachricht ersch�pft ist, dann blockiert die Methode, bis wieder Platz vorhanden ist und das
	 * Objekt in den Kommunikationskanal �bertragen wurde oder bis die angegebene Timeout-Zeit verstrichen ist.
	 *
	 * @param item    Objekt, das in den Kommunikationskanal �bertragen werden soll.
	 * @param timeout Zeit in Millisekunden, f�r die bei vollem Kommunikationskanal maximal auf freien Platz gewartet
	 *                wird.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, w�hrend auf freien Platz im Kommunikationskanal
	 *                              gewartet wurde.
	 */
	public boolean offer(PriorizedObject item, long timeout) throws InterruptedException {
		throw new UnsupportedOperationException("nicht implementiert");
	}

	/**
	 * Empf�ngt und entfernt eine Nachricht aus dem Kommunikationskanal. Wenn der Kommunikationskanal leer ist, dann wartet
	 * die Methode, bis eine Nachricht in den Kommunikationskanal gesendet wurde. Der Kommunikationskanal ist priorisiert,
	 * d.h. es k�nnen Objekte verschiedener Priorit�tsklassen ausgetauscht werden, wobei die Objekte mit der kleinsten
	 * Priorit�tsklasse bevorzugt behandelt werden. Objekte der gleichen Priorit�tsklasse werden in der Reihenfolge
	 * ausgelesen in der sie in den Kommunikationskanal �bertragen wurden (im Gegensatz zu Heap basierten
	 * Implementierungen). Der Aufwand des Auslesens von Objekten ist proportional zur Anzahl verschiedener
	 * Priorit�tsklassen.
	 *
	 * @return Empfangene Nachricht.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, w�hrend auf Nachrichten gewartet wurde.
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
	 * Empf�ngt und entfernt eine Nachricht aus dem Kommunikationskanal. Wenn der Kommunikationskanal leer ist, dann wartet
	 * die Methode, bis eine Nachricht in den Kommunikationskanal gesendet wurde oder bis die angegebene Zeit verstrichen
	 * ist. Der Kommunikationskanal ist priorisiert, d.h. es k�nnen Objekte verschiedener Priorit�tsklassen ausgetauscht
	 * werden, wobei die Objekte mit der kleinsten Priorit�tsklasse bevorzugt behandelt werden. Objekte der gleichen
	 * Priorit�tsklasse werden in der Reihenfolge ausgelesen in der sie in den Kommunikationskanal �bertragen wurden (im
	 * Gegensatz zu Heap basierten Implementierungen). Der Aufwand des Auslesens von Objekten ist proportional zur Anzahl
	 * verschiedener Priorit�tsklassen.
	 *
	 * @param timeout Zeit in Millisekunden, f�r die bei leerem Kommunikationskanal auf neue Nachrichten gewartet wird.
	 * @return Empfangene Nachricht oder <code>null</code>, wenn nach Ablauf des Timeouts immer noch keine Nachricht im
	 *         Kommunikationskanal vorhanden ist.
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, w�hrend auf Nachrichten gewartet wurde.
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
	 * L�scht alle im Kommunikationskanal vorhandenen Nachrichten.
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
