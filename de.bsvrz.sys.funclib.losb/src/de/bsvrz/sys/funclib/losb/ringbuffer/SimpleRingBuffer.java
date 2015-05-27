/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.ringbuffer;

/**
 * Einfacher RingBuffer (FIFO) <ul> <li>Nicht synchronisiert</li> <li>Bei erreichen der Kapazit�t wird der �lteste Eintrag �berschrieben, auch wenn dieser noch
 * nicht abgeholt wurde</li> <li>Gr��e nach Anlegen fix</li> </ul>
 *
 * @author beck et al. projects GmbH
 * @author Phil Schrettenbrunner
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Tue, 10 Mar 2009) $ / ($Author: rs $)
 */
public class SimpleRingBuffer<E> {

	private E buff[];

	private int first = 0, last = -1;


	/**
	 * SimpleRungBuffer fester Gr��e anlegen
	 *
	 * @param size Gr��e
	 *
	 * @throws IllegalArgumentException bei Gr��enabgaben kleiner 1
	 */
	@SuppressWarnings("unchecked")
	public SimpleRingBuffer(int size) throws IllegalArgumentException {
		if(size < 1) throw new IllegalArgumentException("Mindestgr��e f�r SimpleRingBuffer ist 1");
		this.buff = (E[])new Object[size];
	}


	/**
	 * Objekt anf�gen
	 *
	 * @param element Objekt
	 */
	public void push(E element) {
		shift();
		buff[last] = element;
	}


	/**
	 * Neuestes Objekt ansehen, aber nicht entfernen
	 *
	 * @return neuestes Objekt oder null, wenn keine Daten vorhanden
	 */
	public E peekTop() {
		if(entries() == 0) return null;
		return buff[last];
	}

	/**
	 * �ltestes Objekt ansehen, aber nicht entfernen
	 *
	 * @return �ltestes Objekt oder null, wenn keine Daten vorhanden
	 */
	public E peekBottom() {
		if(entries() == 0) return null;
		return buff[first];
	}


	/** RingPuffer leeren */
	@SuppressWarnings("unchecked")
	public void clear() {
		first = 0;
		last = -1;
	}

	/**
	 * Kapazit�t
	 *
	 * @return Kapazit�t
	 */
	public int capacity() {
		return buff.length;
	}


	/**
	 * Anzahl Eintr�g
	 *
	 * @return Anzahl eintr�ge
	 */
	public int entries() {
		if(last < 0) {
			return 0;
		}
		else {
			return first > last ? buff.length - first + last + 1 : last - first + 1;
		}
	}


	/**
	 * Ob alle Pl�tze belegt sind
	 *
	 * @return wahr, wenn size() == capacity()
	 */
	public boolean isFull() {
		return (entries() == capacity());
	}


	/** Zeiger um eins verschieben */
	private void shift() {
		if(entries() == capacity()) first = (first + 1) % buff.length;
		last = (last + 1) % buff.length;
	}
}
