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
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class JsonParseException extends JsonException {
	private static final long serialVersionUID = -3808413152264062202L;
	private final int _pos;

	public JsonParseException(String expected, JsonReader reader) {
		super("Parse Error at " + reader.getPos() + " Expected: " + expected + "\n" + reader);
		_pos = reader.getPos();
	}

	public JsonParseException(final String expected, final JsonReader reader, final Throwable cause) {
		super("Parse Error at " + reader.getPos() + " Expected: " + expected + "\n" + reader, cause);
		_pos = reader.getPos();
	}

	public int getPos() {
		return _pos;
	}
}
