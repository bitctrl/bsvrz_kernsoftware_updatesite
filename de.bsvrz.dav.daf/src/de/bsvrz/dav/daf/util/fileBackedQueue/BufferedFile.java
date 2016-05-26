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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class BufferedFile {

	private FileRepresentation _fileRepresentation = null;

	private long _inputPosition = 0L;

	private long _outputPosition = 0L;

	private long _readBufferStart = 0L;

	private long _writeBufferStart = 0L;

	private int _writeBufferInUse = 0;

	private final int _maxBufferSize;

	private final InputStream _inputStream;

	private final OutputStream _outputStream;

	private byte[] _readBuffer = new byte[0];

	private byte[] _writeBuffer = new byte[0];

	public BufferedFile(final int maxBufferSize) {
		_maxBufferSize = maxBufferSize;
		_inputStream = new InStream();
		_outputStream = new OutStream();
	}

	private byte readByte() throws IOException {
		try {
			if(_writeBufferStart <= _inputPosition && _writeBufferStart + _writeBufferInUse > _inputPosition) {
				return _writeBuffer[((int) (_inputPosition - _writeBufferStart))];
			}
			if(_readBufferStart > _inputPosition || _readBufferStart + _readBuffer.length <= _inputPosition) {
				repositionReadBuffer();
			}
			return _readBuffer[((int) (_inputPosition - _readBufferStart))];
		} finally {
			_inputPosition++;
		}
	}

	private void repositionReadBuffer() {
		int readBufferLength = _maxBufferSize;
		if(((_inputPosition + readBufferLength) > _writeBufferStart) && (_inputPosition < _writeBufferStart)) {
			readBufferLength = (int) (_writeBufferStart - _inputPosition);
		}

		final FileRepresentation fileRepresentation = getFileRepresentation();
		fileRepresentation.seek(_inputPosition);
		try {
			_readBuffer = fileRepresentation.readBytes(readBufferLength);
			_readBufferStart = _inputPosition;
		} catch(IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void writeByte(final byte b) throws IOException {
		try {
			if(_writeBufferStart > _outputPosition || _writeBufferStart + _writeBuffer.length <= _outputPosition) {
				repositionWriteBuffer();
			}
			final int pos = (int) (_outputPosition - _writeBufferStart);
			_writeBufferInUse = pos + 1;
			_writeBuffer[pos] = b;
		} finally {
			_outputPosition++;
		}
	}

	private void repositionWriteBuffer() {
		try {
			final FileRepresentation fileRepresentation = getFileRepresentation();
			fileRepresentation.seek(_writeBufferStart);
			fileRepresentation.writeBytes(_writeBuffer, _writeBufferInUse);
		} catch(IOException e) {
			throw new IllegalStateException(e);
		}
		int writeBufferLength = _maxBufferSize;
		if(((_outputPosition + writeBufferLength) > _readBufferStart) && (_outputPosition < _readBufferStart)) {
			writeBufferLength = (int) (_readBufferStart - _outputPosition);
		}
		_writeBufferStart = _outputPosition;
		_writeBufferInUse = 0;
		_writeBuffer = new byte[writeBufferLength];

	}


	private FileRepresentation getFileRepresentation() {
		if(_fileRepresentation == null) {
			try {
				_fileRepresentation = new FileRepresentation();
			} catch(IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return _fileRepresentation;
	}

	public void clear() {
		if(_fileRepresentation != null) {
			_fileRepresentation.delete();
			_fileRepresentation = null;
		}
		_inputPosition = 0;
		_outputPosition = 0;
	}

	public void seekInput(final long filePosition) {
		_inputPosition = filePosition;
	}

	public void seekOutput(final long filePosition) {
		_outputPosition = filePosition;
	}

	@Override
	public String toString() {
		return "BufferedFile{" + _fileRepresentation + '}';
	}

	public InputStream getInputStream() {
		return _inputStream;
	}

	public OutputStream getOutputStream() {
		return _outputStream;
	}

	private class InStream extends InputStream {

		@Override
		public int read() throws IOException {
			return readByte() & 0xff;
		}
	}

	private class OutStream extends OutputStream {

		@Override
		public void write(final int b) throws IOException {
			writeByte((byte) b);
		}
	}
}
