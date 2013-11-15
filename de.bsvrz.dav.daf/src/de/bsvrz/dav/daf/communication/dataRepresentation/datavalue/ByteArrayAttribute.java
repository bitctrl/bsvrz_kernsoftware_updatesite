/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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
 * Diese Klasse stellt die Attribute und Funktionalit�ten des Datentyps ByteArray zur Verf�gung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class ByteArrayAttribute extends DataValue {

	/** Der Byte Array Wert */
	private byte _byteArray[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public ByteArrayAttribute() {
		_type = BYTE_ARRAY_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param byteArray Feld bit byte-Werten
	 */
	public ByteArrayAttribute(byte byteArray[]) {
		_type = BYTE_ARRAY_TYPE;
		_byteArray = byteArray;
	}


	public final Object getValue() {
		return _byteArray;
	}


	public final DataValue cloneObject() {
		return new ByteArrayAttribute(_byteArray == null ? null : (byte[])_byteArray.clone());
	}


	public final String parseToString() {
		String str = "Byte Array  : [ ";
		if(_byteArray != null) {
			for(int i = 0; i < _byteArray.length; ++i) {
				if(i == 0) {
					str += _byteArray[i];
				}
				else {
					str += " , " + _byteArray[i];
				}
			}
		}
		str += " ]\n";
		return str;
	}


	public final void write(DataOutputStream out) throws IOException {
		if(_byteArray == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_byteArray.length);
			for(int i = 0; i < _byteArray.length; ++i) {
				out.writeByte(_byteArray[i]);
			}
		}
	}


	public final void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		if(length >= 0) {
			_byteArray = new byte[length];
			for(int i = 0; i < length; ++i) {
				_byteArray[i] = in.readByte();
			}
		}
	}

	/**
	 * Diese Methode pr�ft auf Gleichheit eines Objektes, dass dieser Klasse entstammt. Die Pr�fung erfolgt von "grob" nach "fein". Nach einer
	 * <code>null</code>-Referenzabfrage wird die Instanceof methode aufgerufen, abschlie�end wird der Inhalt des Objektes gepr�ft.
	 *
	 * @param obj Referenzobjekt
	 *
	 * @return true: objekt ist gleich, false: Objekt ist nicht gleich
	 */
	public final boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof ByteArrayAttribute)) {
			return false;
		}
		byte _byteArray[] = (byte[])((ByteArrayAttribute)obj).getValue();
		return java.util.Arrays.equals(this._byteArray, _byteArray);
	}
}
