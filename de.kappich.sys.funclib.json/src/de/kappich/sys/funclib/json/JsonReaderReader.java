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

import java.io.IOException;
import java.io.Reader;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class JsonReaderReader extends JsonReader {
	private final Reader _reader;
	private final StringBuilder _cache = new StringBuilder();

	public JsonReaderReader(final Reader reader) {
		_reader = reader;
	}

	@Override
	char readChar() {
		while(_cache.length() <= _pos){
			int read = 0;
			try {
				read = _reader.read();
			}
			catch(IOException e) {
				
				throw new IllegalArgumentException(e);
			}
			if(read >= 0) {
				_cache.append((char)read);
			}
			else {
				_cache.append((char)0);
			}
		}
		return _cache.charAt(_pos++);
	}

	@Override
	public String toString() {
		int start = _pos - 16;
		int end = _pos + 16;
		int off = 16;
		if(start < 0) {
			off += start;
			start = 0;
		}
		if(end > _cache.length()){
			end = _cache.length();
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(_cache.subSequence(start, end));
		for(int i = 0; i < stringBuilder.length(); i++){
			if(stringBuilder.charAt(i) < 20){
				stringBuilder.setCharAt(i, ' ');
			}
		}
		stringBuilder.append("\n");
		for(int i = 0 ; i < off; i++){
			stringBuilder.append(" ");
		}
		stringBuilder.append("^");
		return stringBuilder.toString();
	}

}
