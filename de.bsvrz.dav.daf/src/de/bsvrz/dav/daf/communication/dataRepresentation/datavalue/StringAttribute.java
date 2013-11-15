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
 * Diese Klasse stellt die Attribute und Funktionalitäten des Datentyps String zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public class StringAttribute extends DataValue {

	/** Der Stringwert */
	private String _string;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public StringAttribute() {
		_type = STRING_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param str String
	 */
	public StringAttribute(String str) {
		_type = STRING_TYPE;
		_string = str;
	}

	public final Object getValue() {
		return _string;
	}


	public final DataValue cloneObject() {
		return new StringAttribute(_string);
	}


	public final String parseToString() {
		return "String: " + _string + "\n";
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeUTF(_string);
	}

	public final void read(DataInputStream in) throws IOException {
		_string = in.readUTF();
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
		if(!(obj instanceof StringAttribute)) {
			return false;
		}
		return _string.equals(((StringAttribute)obj).getValue());
	}
}
