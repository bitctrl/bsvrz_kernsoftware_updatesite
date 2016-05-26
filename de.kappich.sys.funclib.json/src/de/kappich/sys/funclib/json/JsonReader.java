/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.kappich.sys.funclib.json.
 * 
 * de.kappich.sys.funclib.json is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.kappich.sys.funclib.json is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.kappich.sys.funclib.json.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.kappich.sys.funclib.json;

import de.bsvrz.sys.funclib.kappich.annotations.NotNull;

import java.io.Reader;
import java.nio.CharBuffer;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class JsonReader extends Reader {
	protected int _pos = 0;
	private int _markPos = -1;

	public static JsonReader fromCharSequence(CharSequence s) {
		return new JsonCharSequenceReader(s);
	}

	public static JsonReader fromReader(Reader s) {
		return new JsonReaderReader(s);
	}

	@Override
	public int read(@NotNull final CharBuffer target) {
		int len = target.remaining();
		char[] cbuf = new char[len];
		int n = this.read(cbuf, 0, len);
		if (n > 0)
		    target.put(cbuf, 0, n);
		return n;
	}

	@Override
	public int read()  {
		return readChar();
	}

	abstract char readChar();

	@Override
	public int read(@NotNull final char[] cbuf) {
		return this.read(cbuf, 0, cbuf.length);
	}

	@Override
	public int read(@NotNull final char[] cbuf, final int off, final int len) {
		cbuf[off] = readChar();
		return 1;
	}

	@Override
	public void close(){
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(final int readAheadLimit){
		_markPos = _pos;
	}

	@Override
	public long skip(final long n) {
		_pos += n;
		return n;
	}

	@Override
	public void reset() {
		_pos = _markPos;
	}

	@Override
	public abstract String toString();

	public int getPos() {
		return _pos;
	}

	public void setPos(final int pos) {
		_pos = pos;
	}
}
