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
 * Diese Klasse stellt ein Anfagetelegramm f�r Objekte gleichen Typs dar. Dem Typ der Objekte ist eine eindeutige ID zugeordnet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class TypeIdsToObjectsRequest extends SystemObjectRequestInfo {

	/** Die Ids der Typen */
	private long _ids[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public TypeIdsToObjectsRequest() {
		_requestType = TYPE_IDS_TO_OBJECTS_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param ids Liste der Typen-IDs
	 */
	public TypeIdsToObjectsRequest(long[] ids) {
		_requestType = TYPE_IDS_TO_OBJECTS_TYPE;
		_ids = ids;
	}

	/**
	 * Gibt die Ids der Typen zur�ck
	 *
	 * @return IDs der typen
	 */
	public final long[] getIds() {
		return _ids;
	}

	public final String parseToString() {
		String str = "Objecktstpen Ids: [ ";
		if(_ids != null) {
			for(int i = 0; i < _ids.length; ++i) {
				str += _ids[i] + "  ";
			}
		}
		str += "]\n";
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		if(_ids == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_ids.length);
			for(int i = 0; i < _ids.length; ++i) {
				out.writeLong(_ids[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int size = in.readInt();
		if(size > 0) {
			_ids = new long[size];
			for(int i = 0; i < size; ++i) {
				_ids[i] = in.readLong();
			}
		}
	}
}

