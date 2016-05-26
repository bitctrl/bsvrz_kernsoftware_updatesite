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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Anfragetelegramm dar. Es wird eine Liste gespeichert, welche die IDs der Objekte beinhalten soll.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class IdsToObjectsRequest extends SystemObjectRequestInfo {

	/** Die Ids der Objekte */
	private long _ids[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public IdsToObjectsRequest() {
		_requestType = IDS_TO_OBJECTS_TYPE;
	}


	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param ids   Liste der IDs
	 */
	public IdsToObjectsRequest(long ids[]) {
		_requestType = IDS_TO_OBJECTS_TYPE;
		_ids = ids;
	}

	/**
	 * Gibt die Ids der Objekte zurück
	 *
	 * @return Liste der IDs
	 */
	public final long[] getIds() {
		return _ids;
	}

	public final String parseToString() {
		String str = "Objeckt Ids: [ ";
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
		_ids = new long[size];
		for(int i = 0; i < size; ++i) {
			_ids[i] = in.readLong();
		}
	}
}
