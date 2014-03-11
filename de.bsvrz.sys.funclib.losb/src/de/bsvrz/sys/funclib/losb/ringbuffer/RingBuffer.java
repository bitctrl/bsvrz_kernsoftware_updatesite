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
 * Der FIFO-Ringpuffer ist über einem Feld von Objektreferenzen angelegt. Die Gesamtkapazität ist festgelegt oder unbegrenzt, wobei das zugrunde liegende Feld
 * stets durch Blöcke fester Größe erweitert oder reduziert wird. Wenn die Gesamtkapazität gleich der Blockgroesse ist, wird das zugrundeliegende Feld nicht
 * veraendert. Die Einfüge- und Ausleseoperationen sind im den Regelfall von konstantem Aufwand und von proportional zur Puffergröße ansteigendem Auf-wand,
 * falls ein neuer Block angefügt oder gelöscht werden muss. Die Einfüge-/Ausleseoperationen des Regelfalles benötigen nur wenige elementare Anweisungen. Es
 * sind keine Speicheroperationen notwendig und es entsteht auch keine Arbeit für den Garbage Collector. Falls die Feldgröße verändert werden muss, kommt der
 * Aufwand für das Kopieren des gesamten Feldes hinzu. Ein Block wird nur dann gelöscht, wenn eine bestimmte Anzahl Blöcke ungenutzt sind. Dadurch werden
 * oszillierende Felder vermieden, wenn der Füllgrad um eine Blockgrenze pendelt. Die Warteschlangen dienen darüber hinaus zur Synchronisation des
 * produzierenden Prozesses (ruft push() auf) und des verarbeitenden Prozesses (ruft pop() auf).
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 6438 $ / $Date: 2009-03-26 16:39:12 +0100 (Do, 26 Mrz 2009) $ / ($Author: rs $)
 * @param <E> Typ der Elemente im Ringpuffer.
 */
public class RingBuffer<E> {

	/** Minimale Blockgroesse */
	public static final int MIN_CHUNK_SIZE = 1;

	/** Minimale Blockgroesse */
	public static final int UNBOUNDED_SIZE = 0;

	/** Anzahl der Blöcke die initial angelegt und nicht unterschritten wird */
	private static final int MIN_CHUNK_NO = 1;

	/** Blöcke werden gelöscht, wenn mindestens diese Zahl an Blöcken ungenutzt ist */
	private static final int REM_CHUNK_THRESHOLD = 2;

	/** Zeiger auf Beginn und Ende des Ringpuffers */
	protected int firstElem = 0, lastElem = -1;

	/** Anzahl der Blöcke */
	protected int noOfChunks = MIN_CHUNK_NO;

	/** Größe der Blöcke */
	protected int chunkSize = -1;

	/** Maximale Größe der Warteschlange */
	protected int maxSize = UNBOUNDED_SIZE;

	protected Object[] buf;

	protected boolean isEmpty = true;

	// statistics
	private int chnkIncreases = 0, chnkDecreases = 0;

	/**
	 * @param chnkSize Größe der Blöcke, um die das Feld der Warteschlange wächst und schrumpft. Die empfohlene Mindestgröße für diesen Parameter ist
	 *                 <code>16</code>.
	 * @param mxSize   Maximale Groesse der Warteschlange. Wird diese ueberschritten, liefert {@link #push(Object)} false. Wenn dieser Parameter {@link
	 *                 #UNBOUNDED_SIZE} ist, ist die Groesse unbegrenzt.
	 */
	public RingBuffer(int chnkSize, int mxSize) {
		if(chnkSize < MIN_CHUNK_SIZE) throw new IllegalArgumentException("chunk size must be at least " + MIN_CHUNK_SIZE + ": " + chnkSize);
		if(mxSize < UNBOUNDED_SIZE) throw new IllegalArgumentException("illegal max size (0=unbounded): " + mxSize);
		if(mxSize != UNBOUNDED_SIZE && chnkSize > mxSize) {
			throw new IllegalArgumentException("chunk size (" + chnkSize + ") cannot be larger than max size (" + mxSize + ")");
		}

		chunkSize = chnkSize;
		maxSize = mxSize;
		buf = new Object[noOfChunks * chunkSize];
	}

	/** @return Maximale Groesse der Warteschlange oder {@link #UNBOUNDED_SIZE}. */
	public int maxSize() {
		return maxSize;
	}

	/**
	 * Aendert doe Maximale Groesse um den angegebenen Wert
	 *
	 * @param delta positiv oder negativ
	 */
	public void changeMaxSize(int delta) {
		maxSize = Math.max(0, maxSize + delta);
	}

	/**
	 * Fügt ein Objekt in die Warteschlange an letzter Stelle ein. Ein Thread, der in pop() wartet, wird fortgesetzt.
	 *
	 * @param elem Einzufügendes Objekt
	 *
	 * @return Wahr, wenn Platz in der Queue war und das Datum eingefuegt wurde, falsch sonst
	 */
	public synchronized boolean push(E elem) {
		if(maxSize != UNBOUNDED_SIZE && size() >= maxSize) {
			return false;
		}
		else {
			adjustSizePreIncr();
			lastElem = oneStepFurther(lastElem);
			buf[lastElem] = elem;
			isEmpty = false;
			notify();
			return true;
		}
	}

	/**
	 * Liefert das erste Element der Warteschlange. Wenn die Warteschlange leer ist, blockiert der aufrufende Thread bis zum nächsten pop()-Aufruf.
	 *
	 * @return Das erste Element der Warteschlange.
	 *
	 * @throws InterruptedException
	 */
	public synchronized E pop() throws InterruptedException {
		while(size() == 0) wait();
		return directPop();
	}

	/**
	 * Liefert das erste Element der Warteschlange. Wenn die Warteschlange leer ist, blockiert der aufrufende Thread oder bricht nach nach einer Wartezeit ab.
	 *
	 * @param timeout Wartezeit, nach der die Aktion abgebrochen wird.
	 *
	 * @return Das erste Element der Warteschlange oder <code>null</code>, falls kein Element in der vorgegebenen Zeit engetroffen ist.
	 *
	 * @throws InterruptedException
	 */
	public synchronized E pop(long timeout) throws InterruptedException {
		if(size() == 0) wait(timeout);
		return size() > 0 ? directPop() : null;
	}

	/**
	 * Liefert das erste Element der Warteschlange oder <code>null</code> wenn keins vorhanden ist.
	 *
	 * @return Das erste Element der Warteschlange oder <code>null</code>
	 */
	public synchronized E popNoWait() {
		return size() > 0 ? directPop() : null;
	}

	@SuppressWarnings({"unchecked"})
	private E directPop() {
		Object erg = buf[firstElem];
		buf[firstElem] = null; // let gc do its work
		firstElem = oneStepFurther(firstElem);
		isEmpty = isFirstElemOneAheadOfLastElem(); // only correct after advancing firstElem
		// pointer
		adjustSizePostDecr();
		return (E)erg;
	}

	/**
	 * Liefert das erste Element der Warteschlange ohne es zu entfernen.
	 *
	 * @return Liefert das erste Element aus der Warteschlange.<code>null</code> falls der Puffer leer ist.
	 */
	@SuppressWarnings({"unchecked"})
	public synchronized E peek() {
		if(size() > 0) {
			return (E)buf[firstElem];
		}
		else {
			return null;
		}
	}

	/**
	 * Liefert das zuletzt eingefügte Element.
	 *
	 * @return Zuletzt eingefügtes Element oder <code>null</code> falls RingBuffer leer.
	 */
	@SuppressWarnings({"unchecked"})
	public synchronized E getLast() {
		if(size() == 0) {
			return null;
		}
		else {
			return (E)buf[lastElem];
		}
	}

	/**
	 * Informmationsausgabe.
	 *
	 * @return Liefert die Anzahl der Blöcke und die Anzahl der Elemente im Puffer zurück.
	 */
	public synchronized String status() {
		return "chunks=" + noOfChunks + " size=" + size();
	}

	/** @return Anzahl Elemente im Ringpuffer. */
	public synchronized int size() {
		return isEmpty ? 0 : (lastElem >= firstElem ? (lastElem - firstElem + 1) : (buf.length - firstElem + lastElem + 1));
	}

	/** @return <code>true</code> falls sich keine Elemente im Ringpuffer befinden. */
	public synchronized boolean isEmpty() {
		return isEmpty;
	}

	protected void adjustSizePreIncr() {
		if(size() + 1 > buf.length) {
			chnkIncreases++;
			copy2NewArray(++noOfChunks * chunkSize);
		}
	}

	protected void adjustSizePostDecr() {
		if(noOfChunks > MIN_CHUNK_NO && size() < (noOfChunks - REM_CHUNK_THRESHOLD) * chunkSize) { // avoid oscillating buffers
			chnkDecreases++;
			copy2NewArray(--noOfChunks * chunkSize);
		}
	}

	protected void copy2NewArray(int newSize) {
		Object[] newBuf = new Object[newSize];
		if(!isEmpty) {
			if(lastElem >= firstElem) {
				System.arraycopy(buf, firstElem, newBuf, 0, size());
			}
			else {
				System.arraycopy(buf, firstElem, newBuf, 0, buf.length - firstElem);
				System.arraycopy(buf, 0, newBuf, buf.length - firstElem, lastElem + 1);
			}
		}
		lastElem = size() - 1;
		firstElem = 0;
		buf = newBuf;
	}

	protected int oneStepFurther(int ptr) {
		return ptr + 1 >= buf.length ? 0 : ptr + 1;
	}

	protected boolean isFirstElemOneAheadOfLastElem() {
		return firstElem - lastElem == 1 || (lastElem == buf.length - 1 && firstElem == 0);
	}

	/**
	 * Zeigt ob der Buffer vollständig gefüllt ist.
	 *
	 * @return <code>true</code> falls der Buffer vollständig gefüllt.<code>false</code> sonst.
	 */
	public boolean isFull() {
		if(maxSize() == UNBOUNDED_SIZE) {
			return false;
		}
		else {
			return size() >= maxSize();
		}
	}

	/**
	 * Kopiert den Inhalt des Ringpuffers in eine Liste. Es werden nur die Referenzen kopiert - d.h. die Liste enthält die gleichen Objekte wie der Ringpuffer.
	 *
	 * @param newBuf Array in den das Ergebnis kopiert werden soll. Ist das Array zu klein, wird ein neues angelegt.
	 *
	 * @return Liste.
	 */
	@SuppressWarnings("unchecked")
	synchronized public E[] toArray(E[] newBuf) {
		int size = size();
		if(size != newBuf.length) newBuf = (E[])java.lang.reflect.Array.newInstance(newBuf.getClass().getComponentType(), size);
		if(size > 0) {
			if(lastElem >= firstElem) {
				System.arraycopy(buf, firstElem, newBuf, 0, size);
			}
			else {
				System.arraycopy(buf, firstElem, newBuf, 0, buf.length - firstElem);
				System.arraycopy(buf, 0, newBuf, buf.length - firstElem, lastElem + 1);
			}
		}
		return newBuf;
		/*
		 * Ist so umständlich, da (E[])new Object[0] NICHT funktioniert! Sonst könnte man einfach
		 * ein neues Array anlegen, und dort die Werte einkopieren.
		 */
	}

	/**
	 * Liefert den zweitobersten Datensatz zurück.
	 *
	 * @return Zeitobersten Datensatz. <code>null</code> falls weniger als zwei Datensätze im Ringpuffer sind.
	 */
	@SuppressWarnings("unchecked")
	public synchronized E next() {
		if(size() >= 2) {
			if(firstElem + 1 >= buf.length) {
				return (E)buf[0];
			}
			else {
				return (E)buf[firstElem + 1];
			}
		}
		else {
			return null;
		}
	}
}
