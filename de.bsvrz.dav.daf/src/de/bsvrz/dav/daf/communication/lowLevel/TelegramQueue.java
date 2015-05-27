/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import java.util.LinkedList;

/**
 * Klasse, die zum gepufferten Austausch von Telegrammen zwischen verschiedenen Threads verwendet werden kann. Die Gesamtgr��e der gepufferten Telegramme ist
 * beschr�nkt. Es werden verschiedene Telegrammpriorit�ten unterst�tzt.
 * <p/>
 * Telegramme k�nnen mit der Methode {@link #put} gespeichert werden und mit der Methode {@link #take} wieder ausgelesen werden. Die Methoden blockieren, wenn
 * beim Speichern nicht gen�gend Platz vorhanden ist, bzw., wenn beim Auslesen kein Telegramm mehr zur Verf�gung steht. Der Methode {@link #close} dient zum
 * Schlie�en der Queue. blockiert keine der beiden Methoden mehr.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 12968 $
 */
public class TelegramQueue<Telegram extends QueueableTelegram> {

	/** Maximale Gesamtgr��e f�r zwischengespeicherte Telegramme. */
	final private int _capacity;

	/** Gesamtgr��e der aktuell zwischengespeicherten Telegramme. */
	private int _size;

	/**
	 * Array, das je m�gliche Priorit�t eine verkettete Liste mit den zwischengespeicherten Telegrammen enth�lt. Es dient au�erdem der Synchronisation von Threads
	 * beim lesenden und schreibenden Zugriff.
	 */
	final private LinkedList<Telegram>[] _priorityLists;

	private boolean _closed = false;

	/**
	 * Erzeugt eine neue Queue mit den angegebenen Eigenschaften.
	 *
	 * @param capacity        Maximale Gesamtgr��e der gepufferten Telegramme.
	 * @param maximumPriority Maximale von Telegrammen verwendete Priorit�t.
	 */
	public TelegramQueue(int capacity, int maximumPriority) {
		if(capacity <= 0) throw new IllegalArgumentException("capacity muss positiv sein: " + capacity);
		if(maximumPriority < 0) throw new IllegalArgumentException("maximumPriority darf nicht negativ sein: " + maximumPriority);
		if(maximumPriority > 127) throw new IllegalArgumentException("maximumPriority darf nicht gr��er als 127 sein: " + maximumPriority);
		_capacity = capacity;
		_size = 0;
		_priorityLists = (LinkedList<Telegram>[])new LinkedList[maximumPriority + 1]; // Compiler-Warnung nicht vermeidbar
		for(int i = 0; i < _priorityLists.length; i++) {
			_priorityLists[i] = new LinkedList<Telegram>();
		}
	}

	/**
	 * Gibt das �lteste in der Queue gespeicherte Telegramm mit der h�chsten Priorit�t zur�ck. Wenn die Queue noch nicht geschlossen wurde, wartet diese Methode,
	 * bis ein Telegramm in der Queue zur Verf�gung steht.
	 *
	 * @return N�chstes gespeicherte Telegramm mit der h�chsten Priorit�t. Wenn die Queue geschlossen wurde und kein gespeichertes Telegramm mehr verf�gbar ist
	 *         wird <code>null</code> zur�ckgegeben.
	 *
	 * @throws InterruptedException Wenn der Thread w�hrend des Wartens unterbrochen wurde.
	 */
	public Telegram take() throws InterruptedException {
		synchronized(this) {
			while(_size == 0) {
				// Wenn die Queue leer ist und geschlossen wurde, wird null zur�ckgegeben
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
		throw new IllegalStateException("Interner Fehler: Es wurde kein Telegramm gefunden, obwohl die Gesamtgr��e " + _size + " ist");
	}

	/**
	 * Speichert das angegebene Telegramm in der Queue. Bei Bedarf wartet diese Methode bis gen�gend Platz in der Queue f�r das zu speichernde Telegramm zur
	 * Verf�gung steht.
	 *
	 * @param telegram Das zu speichernde Telegramm
	 *
	 * @throws InterruptedException Wenn der Thread w�hrend des Wartens unterbrochen wurde.
	 */
	public void put(Telegram telegram) throws InterruptedException {
		if(_closed) return;
		final int length = telegram.getSize();
		if(length <= 0) throw new IllegalArgumentException("Telegramml�nge muss gr��er 0 sein, ist aber " + length + ": " + telegram);
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
	 * Bestimmt die maximale Gesamtgr��e f�r zwischengespeicherte Telegramme.
	 *
	 * @return Maximale Gesamtgr��e f�r zwischengespeicherte Telegramme.
	 */
	public int getCapacity() {
		return _capacity;
	}

	/**
	 * Bestimmt die Gesamtgr��e der aktuell zwischengespeicherten Telegramme.
	 *
	 * @return Gesamtgr��e der aktuell zwischengespeicherten Telegramme.
	 */
	public int getSize() {
		synchronized(this) {
			return _size;
		}
	}


	/**
	 * Diese Methode schlie�t die Verbindung. Danach ignoriert die Methode {@link #put} s�mtliche weitere zu speichernde Telegramme und die Methode {@link #take}
	 * liefert noch alle bisher gespeicherten Telegramme und danach <code>null</code> zur�ck. Eventuell blockierte Threads werden geweckt.
	 */
	public void close() {
		synchronized(this) {
			_closed = true;
			notifyAll();
		}
	}

	/**
	 * Diese Methode schlie�t die Verbindung und l�scht alle noch gespeicherten Telegramme. Danach ignoriert die Methode {@link #put} s�mtliche weitere zu
	 * speichernde Telegramme und die Methode {@link #take} liefert anschlie�end immer <code>null</code> zur�ck. Eventuell blockierte Threads werden geweckt.
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
