/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util.fileBackedQueue;

import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Eine Queue, die zur Speicherung von Daten ausschließlich das Dateisystem verwendet. Diese Klasse ist nicht Threadsafe.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class FileSystemQueue<E> extends AbstractQueue<E> {

	private static final Debug _debug = Debug.getLogger();

	private final int _maxBufferSize;

	private long _fileHeadPosition = 0L;

	private long _fileSizeInUse = 0L;

	private final long _maximumFileSize;

	private final QueueSerializer<E> _queueSerializer;

	private int _size = 0;

	private long _fileTailPosition = 0L;

	private final BufferedFile _bufferedFile;

	private final DataOutputStream _dataOutputStream;

	private final DataInputStream _dataInputStream;

	/**
	 * Eine Queue, die zur Speicherung von Daten ausschließlich das Dateisystem verwendet
	 *
	 * @param maximumFileSize Maximalgröße der Datei in Bytes. Diese kann um maximal eine Elementgröße überschritten werden, sodass immer mindestens ein Eintrag in
	 *                        die Datei passt.
	 */
	public FileSystemQueue(final long maximumFileSize, final QueueSerializer<E> queueSerializer) {
		this(maximumFileSize, queueSerializer, Math.max((int)Math.min(128 * 1024 * 1024, maximumFileSize / 16), 4096));
	}

	/**
	 * Eine Queue, die zur Speicherung von Daten ausschließlich das Dateisystem verwendet
	 *
	 * @param maximumFileSize Maximalgröße der Datei in Bytes. Diese kann um maximal eine Elementgröße überschritten werden, sodass immer mindestens ein Eintrag in
	 *                        die Datei passt.
	 * @param maxBufferSize Maximale Größe für den Schreib- und Lesebuffer für Datei-Ein- und Ausgaben. Kleine Werte verringern den Speicherverbrauch,
	 * senken aber auch die Performance.
	 */
	public FileSystemQueue(final long maximumFileSize, final QueueSerializer<E> queueSerializer, final int maxBufferSize) {
		if(queueSerializer == null) throw new IllegalArgumentException("queueSerializer ist null");
		if(maxBufferSize <= 0) throw new IllegalArgumentException("maxBufferSize muss > 0 sein");
		_maximumFileSize = maximumFileSize;
		_queueSerializer = queueSerializer;
		_maxBufferSize = maxBufferSize;
		_bufferedFile = new BufferedFile(_maxBufferSize);
		final InputStream inputStream = _bufferedFile.getInputStream();
		final OutputStream outputStream = _bufferedFile.getOutputStream();
		_dataOutputStream = new DataOutputStream(outputStream);
		_dataInputStream = new DataInputStream(inputStream);
	}

	/**
	 * Returns an iterator over the elements contained in this collection.
	 *
	 * @return an iterator over the elements contained in this collection
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}

	@Override
	public int size() {
		return _size;
	}

	/**
	 * Inserts the specified element into this queue if it is possible to do so immediately without violating capacity restrictions. When using a
	 * capacity-restricted queue, this method is generally preferable to {@link #add}, which can fail to insert an element only by throwing an exception.
	 *
	 * @param e the element to add
	 *
	 * @return <tt>true</tt> if the element was added to this queue, else <tt>false</tt>
	 *
	 * @throws ClassCastException       if the class of the specified element prevents it from being added to this queue
	 * @throws NullPointerException     if the specified element is null and this queue does not permit null elements
	 * @throws IllegalArgumentException if some property of this element prevents it from being added to this queue
	 */
	public boolean offer(final E e) {
		int size = _queueSerializer.getSize(e);
		final long newSize = _fileSizeInUse + size;
		if(newSize > _maximumFileSize) {
			return false;
		}

		try {
			write(e);
			_size++;
		}
		catch(IOException ioException) {
			_debug.warning("Konnte FileSystemQueue nicht schreiben. Betroffene Datei: " + _bufferedFile, ioException);
			return false;
		}

		_fileSizeInUse = newSize;
		_fileTailPosition = (size + _fileTailPosition) % _maximumFileSize;
		return true;
	}

	@SuppressWarnings({"unchecked"})
	private E read() throws IOException {
		_bufferedFile.seekInput(_fileHeadPosition);
		return (E)_queueSerializer.deserialize(_dataInputStream);
	}

	private void write(final E e) throws IOException {
		_bufferedFile.seekOutput(_fileTailPosition);
		_queueSerializer.serialize(_dataOutputStream, e);
	}

	/**
	 * Retrieves and removes the head of this queue, or returns <tt>null</tt> if this queue is empty.
	 *
	 * @return the head of this queue, or <tt>null</tt> if this queue is empty
	 */
	public E poll() {
		try {
			if(_fileSizeInUse == 0) return null;
			final E result = read();
			final int size = _queueSerializer.getSize(result);
			_fileHeadPosition = (_fileHeadPosition + size) % _maximumFileSize;
			_fileSizeInUse -= size;
			_size--;
			if(_size == 0) {
				_bufferedFile.clear();
				_fileHeadPosition = 0;
				_fileTailPosition = 0;
			}
			return result;
		}
		catch(IOException e) {
			throw new IllegalStateException("Fehler beim Lesen eines Objekts aus dem Dateisystem", e);
		}
	}
	/**
	 * Retrieves, but does not remove, the head of this queue, or returns <tt>null</tt> if this queue is empty.
	 *
	 * @return the head of this queue, or <tt>null</tt> if this queue is empty
	 */
	public E peek() {
		try {
			if(_fileSizeInUse == 0) return null;

			final E result = read();
			return result;
		}
		catch(IOException e) {
			throw new IllegalStateException("Fehler beim Lesen eines Objekts aus dem Dateisystem", e);
		}
	}

	@Override
	public String toString() {
		return _fileSizeInUse + " bytes (" + _size + " Entries) auf der Platte";
	}

	public long getDiskUsed() {
		return _fileSizeInUse;
	}

	public long getCapacity() {
		return _maximumFileSize;
	}

	@Override
	public void clear() {
		_bufferedFile.clear();
		_fileHeadPosition = 0;
		_fileSizeInUse = 0;
		_size = 0;
	}

	private class Itr implements Iterator<E> {

		long _filePosition;

		private Itr(){
//			throw new UnsupportedOperationException();
			_filePosition = _fileHeadPosition;
		}

		public boolean hasNext() {
			return _filePosition != _fileTailPosition;
		}

		public E next() {
			if(!hasNext()) throw new NoSuchElementException();
			final E result;
			try {
				_bufferedFile.seekInput(_filePosition);
				result = (E)_queueSerializer.deserialize(_dataInputStream);
			}
			catch(IOException e) {
				throw new IllegalStateException("Fehler beim Lesen eines Objekts aus dem Dateisystem", e);
			}
			final int size = _queueSerializer.getSize(result);
			_filePosition = (_filePosition + size) % _maximumFileSize;
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException("Nicht unterstützt");
		}
	}


}
