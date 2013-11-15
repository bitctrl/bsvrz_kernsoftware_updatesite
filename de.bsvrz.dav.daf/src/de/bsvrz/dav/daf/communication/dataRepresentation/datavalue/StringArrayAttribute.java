/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps StringArray zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class StringArrayAttribute extends DataValue {

	/** Der String Array Wert */
	private String _stringArray[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public StringArrayAttribute() {
		_type = STRING_ARRAY_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param stringArray Feld mit Strings
	 */
	public StringArrayAttribute(String stringArray[]) {
		_type = STRING_ARRAY_TYPE;
		_stringArray = stringArray;
	}


	public final Object getValue() {
		return _stringArray;
	}

	public final DataValue cloneObject() {
		return new StringArrayAttribute(_stringArray == null ? null : (String[])_stringArray.clone());
	}


	public final String parseToString() {
		String str = "String Array  : [ ";
		if(_stringArray != null) {
			for(int i = 0; i < _stringArray.length; ++i) {
				if(i == 0) {
					str += _stringArray[i];
				}
				else {
					str += " , " + _stringArray[i];
				}
			}
		}
		str += " ]\n";
		return str;
	}


	public final void write(DataOutputStream out) throws IOException {
		if(_stringArray == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_stringArray.length);
			for(int i = 0; i < _stringArray.length; ++i) {
				out.writeUTF(_stringArray[i]);
			}
		}
	}


	public final void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		if(length >= 0) {
			_stringArray = new String[length];
			for(int i = 0; i < length; ++i) {
				_stringArray[i] = in.readUTF();
			}
		}
	}

	/**
	 * Diese Methode prüft auf Gleichheit eines Objektes, dass dieser Klasse entstammt. Die Prüfung erfolgt von "grob" nach "fein". Nach einer
	 * <code>null</code>-Referenzabfrage wird die Instanceof methode aufgerufen, abschließend wird der Inhalt des Objektes geprüft.
	 *
	 * @param obj Referenzobjekt
	 *
	 * @return true: objekt ist gleich, false: Objekt ist nicht gleich
	 */
	public final boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof StringArrayAttribute)) {
			return false;
		}
		String _stringArray[] = (String[])((StringArrayAttribute)obj).getValue();
		return java.util.Arrays.equals(this._stringArray, _stringArray);
	}
}
