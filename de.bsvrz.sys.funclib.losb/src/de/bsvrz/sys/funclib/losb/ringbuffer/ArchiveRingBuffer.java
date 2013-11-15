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
 * Diese Klasse führt einen zweiten Ringpuffer, um neben dem einzufügenden Objekt auch den Einfügezeitpunkt festzuhalten (für die Archivzeit). Sinn dieser
 * Klasse ist es, staendiges erzeugen und loeschen von Objekten zu vermeiden. Dies wäre der Fall, wenn man jedesmal ein Objekt erzeugt, das das Wertepaar
 * enthält.
 */
public class ArchiveRingBuffer extends RingBuffer<ArchiveRingBuffer.PopObject> {

	private long[] timeBuf;

	/**
	 * @param chunkSize: Größe der Blöcke, um die das Feld der Warteschlange wächst und schrumpft.
	 * @param mxSize:    Maximale Groesse der Warteschlange. Wird diese ueberschritten, liefert {@link #push(E)} false
	 */
	public ArchiveRingBuffer(int chunkSize, int mxsize) {
		super(chunkSize, mxsize);
		timeBuf = new long[noOfChunks * chunkSize];
	}

	/**
	 * Fügt ein Objekt in die Warteschlange an letzter Stelle ein. Ein Thread, der in pop() wartet, wird fortgesetzt.
	 *
	 * @param elem: Einzufügendes Objekt
	 * @param time: Einfügezeitpunkt
	 *
	 * @return Wahr, wenn Platz in der Queue war und das Datum eingefuegt wurde, falsch sonst
	 */
	public synchronized boolean push(Object elem, long time, long timeout) throws InterruptedException {
		if(size() >= maxSize) {
			wait(timeout);
		}
		final int sizeBeforeOperation = size();
		if(sizeBeforeOperation >= maxSize) {
			return false;
		}
		else {
			adjustSizePreIncr();
			lastElem = oneStepFurther(lastElem);
			buf[lastElem] = elem;
			timeBuf[lastElem] = time;
			isEmpty = false;
			if(sizeBeforeOperation == 0) {
				notify();
			}
			return true;
		}
	}

	/**
	 * Fügt die ersten Element der beiden Warteschlange in das uebergebene PopObject ein. Dieses Objekt muss vorher angelegt worden sein und wird nur zur Uebergabe
	 * der beiden Werte verwendet. Wenn die Warteschlange leer ist, blockiert der aufrufende Thread bis zum nächsten pop()-Aufruf.
	 *
	 * @param pobj: vorher zu erzeugendes Objekt zur Übergabe der Werte.
	 *
	 * @return Die jeweils ersten Elemente beider Warteschlangen.
	 *
	 * @throws InterruptedException
	 */
	public synchronized void pop(PopObject pobj) throws InterruptedException {
		int sizeBeforeOperation;
		while(0 == (sizeBeforeOperation = size())) wait();
		pobj.object = buf[firstElem];
		pobj.time = timeBuf[firstElem];
		buf[firstElem] = null;						// let gc do its work
		firstElem = oneStepFurther(firstElem);
		isEmpty = isFirstElemOneAheadOfLastElem();	// only correct after advancing firstElem pointer
		adjustSizePostDecr();
		if(sizeBeforeOperation >= maxSize) {
			notify();
		}
	}

	protected void copy2NewArray(int newSize) {
		Object[] newBuf = new Object[newSize];
		long[] newTimeBuf = new long[newSize];
		if(!isEmpty) {
			if(lastElem >= firstElem) {
				System.arraycopy(buf, firstElem, newBuf, 0, size());
				System.arraycopy(timeBuf, firstElem, newTimeBuf, 0, size());
			}
			else {
				System.arraycopy(buf, firstElem, newBuf, 0, buf.length - firstElem);
				System.arraycopy(buf, 0, newBuf, buf.length - firstElem, lastElem + 1);
				System.arraycopy(timeBuf, firstElem, newTimeBuf, 0, buf.length - firstElem);
				System.arraycopy(timeBuf, 0, newTimeBuf, buf.length - firstElem, lastElem + 1);
			}
		}
		lastElem = size() - 1;
		firstElem = 0;
		buf = newBuf;
		timeBuf = newTimeBuf;
	}

	/** Mit dieser Klasse kann man Paare in einem Aufruf aus dem Ringpuffer poppen. */
	public class PopObject {

		public Object object;

		public long time;
	}
}
