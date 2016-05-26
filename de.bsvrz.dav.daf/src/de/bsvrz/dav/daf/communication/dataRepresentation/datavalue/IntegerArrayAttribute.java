/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps IntegerArray zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class IntegerArrayAttribute extends DataValue {

	/** Der Integer Array Wert */
	private int _intArray[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public IntegerArrayAttribute() {
		_type = INTEGER_ARRAY_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param intArray Feld mit Integer Werten
	 */
	public IntegerArrayAttribute(int intArray[]) {
		_type = INTEGER_ARRAY_TYPE;
		_intArray = intArray;
	}

	public final Object getValue() {
		return _intArray;
	}

	public final DataValue cloneObject() {
		return new IntegerArrayAttribute(_intArray == null ? null : (int[])_intArray.clone());
	}

	public final String parseToString() {
		String str = "Integer Array  : [ ";
		if(_intArray != null) {
			for(int i = 0; i < _intArray.length; ++i) {
				if(i == 0) {
					str += _intArray[i];
				}
				else {
					str += " , " + _intArray[i];
				}
			}
		}
		str += " ]\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(_intArray == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_intArray.length);
			for(int i = 0; i < _intArray.length; ++i) {
				out.writeInt(_intArray[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		if(length >= 0) {
			_intArray = new int[length];
			for(int i = 0; i < length; ++i) {
				_intArray[i] = in.readInt();
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
		if(!(obj instanceof IntegerArrayAttribute)) {
			return false;
		}
		int _intArray[] = (int[])((IntegerArrayAttribute)obj).getValue();
		return java.util.Arrays.equals(this._intArray, _intArray);
	}
}
