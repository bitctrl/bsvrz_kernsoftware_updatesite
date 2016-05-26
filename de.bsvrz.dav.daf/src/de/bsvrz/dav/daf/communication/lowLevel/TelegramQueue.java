/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import java.util.LinkedList;

/**
 * Klasse, die zum gepufferten Austausch von Telegrammen zwischen verschiedenen Threads verwendet werden kann. Die Gesamtgröße der gepufferten Telegramme ist
 * beschränkt. Es werden verschiedene Telegrammprioritäten unterstützt.
 * <p>
 * Telegramme können mit der Methode {@link #put} gespeichert werden und mit der Methode {@link #take} wieder ausgelesen werden. Die Methoden blockieren, wenn
 * beim Speichern nicht genügend Platz vorhanden ist, bzw., wenn beim Auslesen kein Telegramm mehr zur Verfügung steht. Der Methode {@link #close} dient zum
 * Schließen der Queue. blockiert keine der beiden Methoden mehr.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TelegramQueue<Telegram extends QueueableTelegram> {

	/** Maximale Gesamtgröße für zwischengespeicherte Telegramme. */
	final private int _capacity;

	/** Gesamtgröße der aktuell zwischengespeicherten Telegramme. */
	private int _size;

	/**
	 * Array, das je mögliche Priorität eine verkettete Liste mit den zwischengespeicherten Telegrammen enthält. Es dient außerdem der Synchronisation von Threads
	 * beim lesenden und schreibenden Zugriff.
	 */
	final private LinkedList<Telegram>[] _priorityLists;

	private boolean _closed = false;

	/**
	 * Erzeugt eine neue Queue mit den angegebenen Eigenschaften.
	 *
	 * @param capacity        Maximale Gesamtgröße der gepufferten Telegramme.
	 * @param maximumPriority Maximale von Telegrammen verwendete Priorität.
	 */
	public TelegramQueue(int capacity, int maximumPriority) {
		if(capacity <= 0) throw new IllegalArgumentException("capacity muss positiv sein: " + capacity);
		if(maximumPriority < 0) throw new IllegalArgumentException("maximumPriority darf nicht negativ sein: " + maximumPriority);
		if(maximumPriority > 127) throw new IllegalArgumentException("maximumPriority darf nicht größer als 127 sein: " + maximumPriority);
		_capacity = capacity;
		_size = 0;
		_priorityLists = (LinkedList<Telegram>[])new LinkedList[maximumPriority + 1]; // Compiler-Warnung nicht vermeidbar
		for(int i = 0; i < _priorityLists.length; i++) {
			_priorityLists[i] = new LinkedList<Telegram>();
		}
	}

	/**
	 * Gibt das älteste in der Queue gespeicherte Telegramm mit der höchsten Priorität zurück. Wenn die Queue noch nicht geschlossen wurde, wartet diese Methode,
	 * bis ein Telegramm in der Queue zur Verfügung steht.
	 *
	 * @return Nächstes gespeicherte Telegramm mit der höchsten Priorität. Wenn die Queue geschlossen wurde und kein gespeichertes Telegramm mehr verfügbar ist
	 *         wird <code>null</code> zurückgegeben.
	 *
	 * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wurde.
	 */
	public Telegram take() throws InterruptedException {
		synchronized(this) {
			while(_size == 0) {
				// Wenn die Queue leer ist und geschlossen wurde, wird null zurückgegeben
				if(_closed) return null;
				// Wenn die Queue leer ist und nicht geschlossen wurde, wird gewartet
				wait();
			}
			for(int i = _priorityLists.length - 1; i >= 0; i--) {
				LinkedList<Telegram> priorityList = _priorityLists[i];
				if(!priorityList.isEmpty()) {
					final Telegram telegram = priorityList.removeFirst();
					_size -= telegram.getSize();
					notifyAll();
					return telegram;
				}
			}
		}
		throw new IllegalStateException("Interner Fehler: Es wurde kein Telegramm gefunden, obwohl die Gesamtgröße " + _size + " ist");
	}

	/**
	 * Speichert das angegebene Telegramm in der Queue. Bei Bedarf wartet diese Methode bis genügend Platz in der Queue für das zu speichernde Telegramm zur
	 * Verfügung steht.
	 *
	 * @param telegram Das zu speichernde Telegramm
	 *
	 * @throws InterruptedException Wenn der Thread während des Wartens unterbrochen wurde.
	 */
	public void put(Telegram telegram) throws InterruptedException {
		if(_closed) return;
		final int length = telegram.getSize();
		if(length <= 0) throw new IllegalArgumentException("Telegrammlänge muss größer 0 sein, ist aber " + length + ": " + telegram);
		final byte priority = telegram.getPriority();
		synchronized(this) {
			if(length > _capacity){
				// Telegramm passt nicht in Queue, solange warten bis _size == 0 und dann senden
				while(!_closed && _size > 0) {
					wait();
				}
			}
			else {
				while(!_closed && _size + length > _capacity) {
					wait();
				}
			}
			if(_closed) return;
			_priorityLists[priority].add(telegram);
			_size += length;
			notifyAll();
		}
	}

	/**
	 * Bestimmt die maximale Gesamtgröße für zwischengespeicherte Telegramme.
	 *
	 * @return Maximale Gesamtgröße für zwischengespeicherte Telegramme.
	 */
	public int getCapacity() {
		return _capacity;
	}

	/**
	 * Bestimmt die Gesamtgröße der aktuell zwischengespeicherten Telegramme.
	 *
	 * @return Gesamtgröße der aktuell zwischengespeicherten Telegramme.
	 */
	public int getSize() {
		synchronized(this) {
			return _size;
		}
	}


	/**
	 * Diese Methode schließt die Verbindung. Danach ignoriert die Methode {@link #put} sämtliche weitere zu speichernde Telegramme und die Methode {@link #take}
	 * liefert noch alle bisher gespeicherten Telegramme und danach <code>null</code> zurück. Eventuell blockierte Threads werden geweckt.
	 */
	public void close() {
		synchronized(this) {
			_closed = true;
			notifyAll();
		}
	}

	/**
	 * Diese Methode schließt die Verbindung und löscht alle noch gespeicherten Telegramme. Danach ignoriert die Methode {@link #put} sämtliche weitere zu
	 * speichernde Telegramme und die Methode {@link #take} liefert anschließend immer <code>null</code> zurück. Eventuell blockierte Threads werden geweckt.
	 */
	public void abort() {
		synchronized(this) {
			_closed = true;
			notifyAll();
			try {
				while(take() != null) ;
			}
			catch(InterruptedException ignored) {
			}
		}
	}
}
