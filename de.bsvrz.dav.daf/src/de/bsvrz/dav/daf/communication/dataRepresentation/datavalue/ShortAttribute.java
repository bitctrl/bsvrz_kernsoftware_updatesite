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
 * Diese Klasse stellt die Attribute und Funktionalit�ten des Datentyps short zur Verf�gung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class ShortAttribute extends DataValue {

	/** Der Shortwert */
	private short _short;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public ShortAttribute() {
		_type = SHORT_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param s short Wert
	 */
	public ShortAttribute(short s) {
		_type = SHORT_TYPE;
		_short = s;
	}


	public final Object getValue() {
		return new Short(_short);
	}


	public final DataValue cloneObject() {
		return new ShortAttribute(_short);
	}

	public final String parseToString() {
		return "Short: " + _short + "\n";
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(_short);
	}

	public final void read(DataInputStream in) throws IOException {
		_short = in.readShort();
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
		if(!(obj instanceof ShortAttribute)) {
			return false;
		}
		short _s = ((Short)((ShortAttribute)obj).getValue()).shortValue();
		return _short == _s;
	}
}
