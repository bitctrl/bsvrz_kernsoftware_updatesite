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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.IntegerValueState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Klasse, die den Zugriff auf Werte-Zustände von Ganzzahl-Attributtypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class DafIntegerValueState extends DafConfigurationObject implements IntegerValueState {

	/** Diesem Wertezustand zugeordnete Wert eines Ganzzahlattributs in unskalierter Form. */
	private long _stateValue;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafIntegerValueState(DafDataModel dataModel) {
		super(dataModel);
		_internType = INTEGER_VALUE_STATE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafIntegerValueState(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long[] setIds,
			long stateValue
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = INTEGER_VALUE_STATE;
		_stateValue = stateValue;
	}

	public final long getValue() {
		return _stateValue;
	}

	public final String parseToString() {
		String str = "Wert Eigenschaften: \n";
		str += super.parseToString();
		str += "Wert: " + _stateValue + "\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeLong(_stateValue);
	}

	public final void read(DataInputStream in) throws IOException {
		super.read(in);
		_stateValue = in.readLong();
	}
}
