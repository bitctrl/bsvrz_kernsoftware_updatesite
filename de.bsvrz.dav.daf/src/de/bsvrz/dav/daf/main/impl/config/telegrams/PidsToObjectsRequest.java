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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Anfragetelegramm f�r permanente IDs(PIDs) dar. Die PIds werden in einem Feld gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class PidsToObjectsRequest extends SystemObjectRequestInfo {

	/** Die Pids der Objekte */
	private String pids[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public PidsToObjectsRequest() {
		_requestType = PIDS_TO_OBJECTS_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param _pids Pids der Objekte
	 */
	public PidsToObjectsRequest(String _pids[]) {
		_requestType = PIDS_TO_OBJECTS_TYPE;
		pids = _pids;
	}

	/**
	 * Gibt die Pids der Objekte zur�ck
	 *
	 * @return PIDs der Objekte
	 */
	public final String[] getPids() {
		return pids;
	}

	public final String parseToString() {
		String str = "Objeckt Pids: [ ";
		if(pids != null) {
			for(int i = 0; i < pids.length; ++i) {
				str += pids[i] + "  ";
			}
		}
		str += "]\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(pids == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(pids.length);
			for(int i = 0; i < pids.length; ++i) {
				out.writeUTF(pids[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int size = in.readInt();
		if(size > 0) {
			pids = new String[size];
			for(int i = 0; i < size; ++i) {
				pids[i] = in.readUTF();
			}
		}
	}
}

