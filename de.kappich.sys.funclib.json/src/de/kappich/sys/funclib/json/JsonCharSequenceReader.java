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

/**
 * Angepasster StringReader, der die aktuelle Leseposition zurückgibt und immer rückwärts seek()en kann.
 * Dieser Reader liefert nach dem Sting-Ende beliebig viele weitere 0-Bytes (der Stream ist unendlich lang).
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class JsonCharSequenceReader extends JsonReader {
	private final CharSequence _s;

	JsonCharSequenceReader(CharSequence s) {
		_s = s;
	}

	@Override
	public char readChar() {
		char result;
		if(_pos >= _s.length()){
			result = 0;
		}
		else {
			result = _s.charAt(_pos);
		}
		_pos++;
		return result;
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
		if(end > _s.length()){
			end = _s.length();
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(_s.subSequence(start, end));
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
