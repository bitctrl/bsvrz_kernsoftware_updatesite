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
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps FloatArray zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class FloatArrayAttribute extends DataValue {

	/** Der Float Array Wert */
	private float _floatArray[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public FloatArrayAttribute() {
		_type = FLOAT_ARRAY_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param floatArray Feld mit Float Werten
	 */
	public FloatArrayAttribute(float floatArray[]) {
		_type = FLOAT_ARRAY_TYPE;
		_floatArray = floatArray;
	}

	public final Object getValue() {
		return _floatArray;
	}

	public final DataValue cloneObject() {
		return new FloatArrayAttribute(_floatArray == null ? null : (float[])_floatArray.clone());
	}

	public final String parseToString() {
		String str = "Float Array  : [ ";
		if(_floatArray != null) {
			for(int i = 0; i < _floatArray.length; ++i) {
				if(i == 0) {
					str += _floatArray[i];
				}
				else {
					str += " , " + _floatArray[i];
				}
			}
		}
		str += " ]\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(_floatArray == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_floatArray.length);
			for(int i = 0; i < _floatArray.length; ++i) {
				out.writeFloat(_floatArray[i]);
			}
		}
	}


	public final void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		if(length >= 0) {
			_floatArray = new float[length];
			for(int i = 0; i < length; ++i) {
				_floatArray[i] = in.readFloat();
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
		if(!(obj instanceof FloatArrayAttribute)) {
			return false;
		}
		float _floatArray[] = (float[])((FloatArrayAttribute)obj).getValue();
		return java.util.Arrays.equals(this._floatArray, _floatArray);
	}
}
