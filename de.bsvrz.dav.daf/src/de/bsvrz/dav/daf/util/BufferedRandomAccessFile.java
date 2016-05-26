/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

/**
 * Implementierung eines gepufferten {@link java.io.RandomAccessFile}. Diese Klasse implementiert die Interfaces
 * {@link java.io.DataInput}, {@link java.io.DataOutput} und {@link java.nio.channels.ByteChannel} und unterstützt
 * alle wesentlichen Methoden eines {@link java.io.RandomAccessFile}s
 *
 * Diese Klasse ist (anders als {@link java.nio.channels.FileChannel}) nicht für die Verwendung durch mehrere unsynchronisierte
 * Threads geeignet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class BufferedRandomAccessFile implements DataInput, DataOutput, ByteChannel {
	

	/** ungepufferter EingabeStream */
	private final InputStream _rawInStream;

	/** ungepufferter AusgabeStream */
	private final OutputStream _rawOutStream;

	/** FileChannel zum lesen und Schreiben der Datei */
	private final FileChannel _channel;

	/** Gepufferter EingabeStream, wird bei Bedarf initialisiert und gelöscht */
	private DataInputStream _dataInStream = null;

	/** Gepufferter AusgabeStream, wird bei Bedarf initialisiert und gelöscht */
	private DataOutputStream _dataOutStream = null;

	/**
	 * Aktuelle Dateiposition, muss hier gemerkt und selbst berechnet werden, weil die Position des FileChannels durch die
	 * Pufferung beim Lesen und Schreiben nicht notwendigerweise der aktuellen logischen Position entspricht
	 */
	private long _position = 0L;

	/**
	 * Größe des Lese und Schreibpuffers
	 */
	private int _bufferSize;

	/** Standardpuffergröße */
	private static final int defaultBufferSize = 512;

	/**
	 * Erstellt ein neues gepuffertes BufferedFile als gepufferten Ersatz eines {@link java.io.RandomAccessFile}.
	 * @param file Datei
	 * @throws FileNotFoundException Falls Datei nicht gefunden
	 */
	public BufferedRandomAccessFile(final File file) throws FileNotFoundException {
		this(file, "rw");
	}

	/**
	 * Erstellt ein neues gepuffertes BufferedFile als gepufferten Ersatz eines {@link java.io.RandomAccessFile}.
	 * @param file Datei
	 * @param bufferSize Größe des Lese und Schreibpuffers in Byte
	 * @throws FileNotFoundException Falls Datei nicht gefunden
	 */
	public BufferedRandomAccessFile(final File file, final int bufferSize) throws FileNotFoundException {
		this(file, "rw", bufferSize);
	}

	/**
	 * Erstellt ein neues gepuffertes BufferedFile als gepufferten Ersatz eines {@link java.io.RandomAccessFile}.
	 * @param file Datei
	 * @param mode "r" wenn nur gelesen werden soll, "rw" zum Lesen und schreiben. Siehe {@link java.io.RandomAccessFile}

	 * @throws java.io.FileNotFoundException Falls Datei nicht gefunden
	 */
	public BufferedRandomAccessFile(final File file, final String mode) throws FileNotFoundException {
		this(file, mode, defaultBufferSize);
	}

	/**
	 * Erstellt ein neues gepuffertes BufferedFile als gepufferten Ersatz eines {@link java.io.RandomAccessFile}.
	 * @param file Datei
	 * @param mode "r" wenn nur gelesen werden soll, "rw" zum Lesen und schreiben. Siehe {@link java.io.RandomAccessFile}
	 * @param bufferSize Größe des Lese und Schreibpuffers in Byte
	 * @throws FileNotFoundException Falls Datei nicht gefunden
	 */
	public BufferedRandomAccessFile(final File file, final String mode, final int bufferSize) throws FileNotFoundException {
		this(new RandomAccessFile(file, mode), bufferSize);
	}

	private BufferedRandomAccessFile(final RandomAccessFile randomAccessFile, final int bufferSize) {
		if(bufferSize <= 0){
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		_bufferSize = bufferSize;
		_channel = randomAccessFile.getChannel();
		_rawInStream = Channels.newInputStream(_channel);
		_rawOutStream = Channels.newOutputStream(_channel);
	}

	private DataOutputStream getDataOutStream() {
		flushInStream(); // Vor dem Schreiben Leseoperationen abschließen und Lesepuffer löschen (um FilePointer abschließend zu setzen)
		if(_dataOutStream == null) {
			_dataOutStream = new DataOutputStream(new BufferedOutputStream(_rawOutStream, _bufferSize));
		}
		return _dataOutStream;
	}

	private DataInputStream getDataInStream() throws IOException {
		flushOutStream(); // Vor dem Lesen Schreiboperationen abschließen und Schreibpuffer löschen (um FilePointer abschließend zu setzen)
		if(_dataInStream == null) {
			_dataInStream = new DataInputStream(new BufferedInputStream(_rawInStream, _bufferSize));
		}
		return _dataInStream;
	}

	private void flushInStream() {
		// Eingabestream verwerfen, flushen nicht möglich und nicht nötig
		_dataInStream = null;
	}

	private void flushOutStream() throws IOException {
		// Ausgabestream verwerfen, nur flushen, nicht schließen, weil schließen das ganze RandomAccessFile schließen würde!
		if(_dataOutStream != null) {
			_dataOutStream.flush();
			_dataOutStream = null;
		}
	}

	@Override
	public boolean isOpen() {
		return _channel.isOpen();
	}

	@Override
	public void close() throws IOException {
		if(_dataInStream != null) _dataInStream.close();
		if(_dataOutStream != null) _dataOutStream.close();
		_channel.close();
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		getDataInStream().readFully(b);
		_position+=b.length;
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		getDataInStream().readFully(b,off,len);
		_position+=len;
	}

	/**
	 * Überspringt n genau Bytes. Anders als DataInput definiert wird immer genau die übergebene Zahl an bytes übersprungen,
	 * d.h. die Methode gibt immer den Parameter n zurück.
	 * Daher entspricht diese Methode <code>position(position() + n); return n;</code>
	 *
	 * Diese Methode kann über das Dateiende hinausspringen, vgl. {@link java.io.RandomAccessFile#seek(long)}.
	 *
	 * @param n Anzahl zu überspringender Bytes (kann negativ sein, dann wird rückwärts gesprungen)
	 * @return n
	 * @throws IOException
	 */
	@Override
	public int skipBytes(final int n) throws IOException {
		return (int) skip(n);
	}

	/**
	 * Überspringt n genau Bytes.
	 * Daher entspricht diese Methode <code>position(position() + n); return n;</code>
	 *
	 * Diese Methode kann über das Dateiende hinausspringen, vgl. {@link java.io.RandomAccessFile#seek(long)}.
	 *
	 * @param n Anzahl zu überspringender Bytes (kann negativ sein, dann wird rückwärts gesprungen)
	 * @return Der Parameter n (zur Kompatibilität mit FileChannel)
	 * @throws IOException
	 */
	public long skip(long n) throws IOException {
		if(n == 0) return 0;
		if(n > 0 && n < _bufferSize/2 && _dataInStream != null){
			// Es besteht eine gute Wahrscheinlichkeit, dass die Zielposition noch gepuffert ist
			int remaining = (int) n;
			while(remaining > 0) {
				int skipped = getDataInStream().skipBytes(remaining);
				if(skipped <= 0) break;
				remaining -= skipped;
			}
			if(remaining == 0){
				// Skip Erfolgreich
				_position += n;
				return n;
			}
			// Sonst als Fallback position() benutzen und den _dataInStream bei Bedarf neu initialisieren
		}
		position(position() + n);
		return n;
	}

	@Override
	public boolean readBoolean() throws IOException {
		boolean b = getDataInStream().readBoolean();
		_position ++;
		return b;
	}

	@Override
	public byte readByte() throws IOException {
		byte b = getDataInStream().readByte();
		_position ++;
		return b;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		int unsignedByte = getDataInStream().readUnsignedByte();
		_position ++;
		return unsignedByte;
	}

	@Override
	public short readShort() throws IOException {
		short readShort = getDataInStream().readShort();
		_position += 2;
		return readShort;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int unsignedShort = getDataInStream().readUnsignedShort();
		_position += 2;
		return unsignedShort;
	}

	@Override
	public char readChar() throws IOException {
		char readChar = getDataInStream().readChar();
		_position += 2;
		return readChar;
	}

	@Override
	public int readInt() throws IOException {
		int readInt = getDataInStream().readInt();
		_position += 4;
		return readInt;
	}

	@Override
	public long readLong() throws IOException {
		long readLong = getDataInStream().readLong();
		_position += 8;
		return readLong;
	}

	@Override
	public float readFloat() throws IOException {
		float readFloat = getDataInStream().readFloat();
		_position += 8;
		return readFloat;
	}

	@Override
	public double readDouble() throws IOException {
		double readDouble = getDataInStream().readDouble();
		_position += 16;
		return readDouble;
	}

	@Override
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		DataInputStream inStream = getDataInStream();
		inStream.mark(2);
		int len = inStream.readUnsignedShort();
		_position += len + 2;
		inStream.reset();
		return inStream.readUTF();
	}

	@Override
	public void write(final int b) throws IOException {
		getDataOutStream().write(b);
		_position += 1;
	}

	@Override
	public void write(final byte[] b) throws IOException {
		getDataOutStream().write(b);
		_position += b.length;
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		getDataOutStream().write(b,off,len);
		_position += len;
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		getDataOutStream().writeBoolean(v);
		_position += 1;
	}

	@Override
	public void writeByte(final int v) throws IOException {
		getDataOutStream().writeByte(v);
		_position += 1;
	}

	@Override
	public void writeShort(final int v) throws IOException {
		getDataOutStream().writeShort(v);
		_position += 2;
	}

	@Override
	public void writeChar(final int v) throws IOException {
		getDataOutStream().writeChar(v);
		_position += 2;
	}

	@Override
	public void writeInt(final int v) throws IOException {
		getDataOutStream().writeInt(v);
		_position += 4;
	}

	@Override
	public void writeLong(final long v) throws IOException {
		getDataOutStream().writeLong(v);
		_position += 8;
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		getDataOutStream().writeFloat(v);
		_position += 8;
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		getDataOutStream().writeDouble(v);
		_position += 16;
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		getDataOutStream().writeBytes(s);
		_position += s.length();
	}

	@Override
	public void writeChars(final String s) throws IOException {
		getDataOutStream().writeChars(s);
		_position += s.length() * 2;
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		DataOutputStream outStream = getDataOutStream();
		outStream.writeUTF(s);

		// Die Position ausnahmsweise nicht selbst berechnen, sondern die Ausgabe flushen und den FilePointer des Channels nehmen
		// denn die Anzahl Bytes, die bei writeUTF geschrieben werden sind nicht trivial und performant zu ermitteln
		outStream.flush();
		_position = _channel.position();
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		flushOutStream();
		flushInStream();
		int read = _channel.read(dst);
		_position += read;
		return read;
	}

	@Override
	public int write(final ByteBuffer src) throws IOException {
		flushOutStream();
		flushInStream();
		int write = _channel.write(src);
		_position += write;
		return write;
	}

	/**
	 * @see java.nio.channels.FileChannel#position()
	 */
	public long position() {
		// NICHT "return _channel.position();" wegen Pufferung, vgl. Javadoc zu _position
		return _position;
	}

	/**
	 * @see java.nio.channels.FileChannel#position(long)
	 */
	public BufferedRandomAccessFile position(final long newPosition) throws IOException {
		if(newPosition == _position) return this;
		flushOutStream();
		flushInStream();
		_channel.position(newPosition);
		_position = newPosition;
		return this;
	}

	/**
	 * @see java.nio.channels.FileChannel#size()
	 */
	public long size() throws IOException {
		flushOutStream();
		return _channel.size();
	}

	/**
	 * @see java.nio.channels.FileChannel#truncate(long)
	 */
	public BufferedRandomAccessFile truncate(final long size) throws IOException {
		flushOutStream();
		flushInStream();
		_channel.truncate(size);
		return this;
	}

	/**
	 * Für RandomAccessFile-Kompatibilität
	 * @see #position(long)
	 */
	public void seek(long position) throws IOException {
		position(position);
	}

	/**
	 * Für RandomAccessFile-Kompatibilität
	 * @see #position()
	 */
	public long getFilePointer() {
		return position();
	}

	/**
	 * Für RandomAccessFile-Kompatibilität
	 * @see #size()
	 */
	public long length() throws IOException {
		return size();
	}

	/**
	 * Für RandomAccessFile-Kompatibilität
	 */
	public void setLength(final long len) throws IOException {
		flushOutStream();
		flushInStream();
		_channel.truncate(len);
	}

	@Override
	public String toString() {
		return "[BufferedFile, pos=" + position() + "]";
	}
}
