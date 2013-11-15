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
 * Diese Klasse stellt die Attribute und Funktionalit�ten des Datentyps ListArray zur Verf�gung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class AttributeListArrayAttribute extends DataValue {

	private AttributeListAttribute _values[];

	private DataValue _attributeListValues[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public AttributeListArrayAttribute() {
		_type = ATTRIBUTE_LIST_ARRAY_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param values feld mit Werten
	 */
	public AttributeListArrayAttribute(AttributeListAttribute[] values) {
		_type = ATTRIBUTE_LIST_ARRAY_TYPE;
		_values = values;
	}

	public final void setValue(DataValue values[]) {
		_attributeListValues = values;
	}

	/**
	 * Gibt den Wert zurr�ck.
	 *
	 * @return der Wert
	 */
	public final Object getValue() {
		return _values;
	}

	/**
	 * Gibt die Anzahl der Attributlisten in diesem Array zur�ck.
	 *
	 * @return Anzahl der Attributlisten
	 */
	public final int getAttributeListsLength() {
		return _values == null ? 0 : _values.length;
	}


	public final DataValue cloneObject() {
		AttributeListAttribute _values[] = null;
		if(this._values != null) {
			_values = new AttributeListAttribute[this._values.length];
			for(int i = 0; i < this._values.length; ++i) {
				if(this._values[i] != null) {
					_values[i] = (AttributeListAttribute)this._values[i].cloneObject();
				}
			}
		}
		AttributeListArrayAttribute clone = new AttributeListArrayAttribute(_values);
		clone.setValue(cloneAttributeListValues());
		return clone;
	}


	public final String parseToString() {
		String str = "Attributeliste Array: [\n";
		if(_values != null) {
			for(int i = 0; i < _values.length; ++i) {
				if(_values[i] != null) {
					str += _values[i].parseToString();
				}
			}
		}
		str += "]\n";
		return str;
	}


	public final void write(DataOutputStream out) throws IOException {
		if(_values == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_values.length);
			for(int i = 0; i < _values.length; ++i) {
				if(_values[i] != null) {
					_values[i].write(out);
				}
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		if(length >= 0) {
			_values = new AttributeListAttribute[length];
			for(int i = 0; i < length; ++i) {
				AttributeListAttribute v = (AttributeListAttribute)DataValue.getObject(ATTRIBUTE_LIST_TYPE);
				if(v != null) {
					v.setValue(cloneAttributeListValues());
					v.read(in);
					_values[i] = v;
				}
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
		if(!(obj instanceof AttributeListArrayAttribute)) {
			return false;
		}
		AttributeListAttribute values[] = (AttributeListAttribute[])((AttributeListArrayAttribute)obj).getValue();
		return java.util.Arrays.equals(_values, values);
	}

	/**
	 * Kopiert die Werte in der Attributsliste und gibt die Kopie zur�ck.
	 *
	 * @return Kopie der Werte in der Attributsliste
	 */
	private DataValue[] cloneAttributeListValues() {
		if(_attributeListValues == null) {
			return null;
		}
		DataValue clone[] = new DataValue[_attributeListValues.length];
		for(int i = 0; i < _attributeListValues.length; ++i) {
			if(_attributeListValues[i] != null) {
				clone[i] = _attributeListValues[i].cloneObject();
			}
		}
		return clone;
	}
}
