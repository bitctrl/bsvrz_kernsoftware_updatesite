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
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps List zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AttributeListAttribute extends DataValue {

	/** Werte der Daten */
	private DataValue _values[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public AttributeListAttribute() {
		_type = ATTRIBUTE_LIST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param _values Werte
	 */
	public AttributeListAttribute(DataValue _values[]) {
		_type = ATTRIBUTE_LIST_TYPE;
		this._values = _values;
	}

	/**
	 * Setzt <code>_values</code>
	 *
	 * @param values Werte der Daten
	 */
	public final void setValue(DataValue values[]) {
		_values = values;
	}

	public final Object getValue() {
		return _values;
	}


	public final DataValue cloneObject() {
		DataValue values[] = null;
		if(_values != null) {
			values = new DataValue[_values.length];
			for(int i = 0; i < _values.length; ++i) {
				values[i] = _values[i].cloneObject();
			}
		}
		return new AttributeListAttribute(values);
	}


	public final String parseToString() {
		String str = "Attributliste: [ \n";
		if(_values != null) {
			for(int i = 0; i < _values.length; ++i) {
				if(_values[i] != null) {
					str += _values[i].parseToString();
				}
			}
		}
		str += "\n]\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(_values != null) {
			for(int i = 0; i < _values.length; ++i) {
				if(_values[i] != null) {
					_values[i].write(out);
				}
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		if(_values != null) {
			for(int i = 0; i < _values.length; ++i) {
				if(_values[i] != null) {
					_values[i].read(in);
				}
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
		if(!(obj instanceof AttributeListAttribute)) {
			return false;
		}
		DataValue _values[] = (DataValue[])((AttributeListAttribute)obj).getValue();
		return java.util.Arrays.equals(this._values, _values);
	}
}
