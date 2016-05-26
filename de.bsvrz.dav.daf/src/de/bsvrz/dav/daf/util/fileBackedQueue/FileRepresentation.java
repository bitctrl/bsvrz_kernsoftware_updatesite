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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Kapselt eine Datei für die FileSystemQueue
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
final class FileRepresentation {

	private final DataInputStream _inputStream;

	private final RandomAccessFile _randomAccessFile;

	final File _tempFile;

	/**
	 * Erstellt eine FileRepresentation-Klasse
	 *
	 * @throws IOException -
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public FileRepresentation() throws IOException {
		_tempFile = File.createTempFile("FileSystemQueue", null);
		_tempFile.deleteOnExit();
		_randomAccessFile = new RandomAccessFile(_tempFile, "rw");
		_inputStream = new DataInputStream(new FileInputStream(_randomAccessFile.getFD()));
	}

	public void seek(final long position){
		try {
			if(_randomAccessFile.getFilePointer() == position) return;
			_randomAccessFile.seek(position);
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public Object read(final QueueSerializer q) throws IOException {
		return q.deserialize(_inputStream);
	}

	public void delete() {
		try {
			_inputStream.close();
			_randomAccessFile.close();
		}
		catch(IOException ignored) {
		}
		_tempFile.delete();
	}

	public void clear() throws IOException {
		_randomAccessFile.setLength(0);
	}

	@Override
	public String toString() {
		return String.valueOf(_tempFile);
	}

	@Override
	protected void finalize() throws Throwable {
		delete();
		super.finalize();
	}

	public void setLength(final long newLength) throws IOException {
		_randomAccessFile.setLength(newLength);
	}

	public byte[] readBytes(final int len) throws IOException {
		final byte[] bytes = new byte[len];
		final int read = _randomAccessFile.read(bytes);
		return bytes;
	}

	public void writeBytes(final byte[] writeBuffer, final int len) throws IOException {
		_randomAccessFile.write(writeBuffer, 0, len);
	}

	public DataInputStream getInputStream() {
		return _inputStream;
	}
}
