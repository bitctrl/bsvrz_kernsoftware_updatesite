/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.subscription;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;

import java.util.*;

/**
 * Speichert vom Datenverteiler empfangene Datensätze zur späteren Auslieferung an einen Receiver der Applikation.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CollectingReceiver {

	/** Liste mit den auszuliefernden Datensätzen */
	private final ArrayList<ResultData> _results = new ArrayList<ResultData>();

	/** Receiver der Applikation, an den die gespeicherten Datensätze ausgeliefert werden sollen. */
	private final ClientReceiverInterface _receiver;

	/** Gesamtgröße der auszuliefernden Datensätze. */
	private int _collectedSize = 0;

	private int _referenceCount = 0;


	/** Erzeugt ein neues Objekt für den angegebenen Receiver */
	public CollectingReceiver(final ClientReceiverInterface receiver) {
		_receiver = receiver;
	}


	/**
	 * Speichert einen Datensatz zur späteren Auslieferung an den zugeordneten Receiver der Applikation.
	 *
	 * @param result Zu speichernder Datensatz.
	 * @param size   Größe des zu speichernden Datensatzes.
	 *
	 * @return <code>true</code>, wenn kein noch nicht ausgelieferter Datensatz gespeichert war; <code>false</code> sonst.
	 */
	public boolean storeForDelivery(ResultData result, int size) {
		synchronized(this) {
			final boolean wasEmpty = _results.isEmpty();
			_results.add(result);
			_collectedSize += size;
			return wasEmpty;
		}
	}

	/**
	 * Liefert die gespeicherten Datensätze an den Receiver der Applikation aus.
	 *
	 * @return Gesamtgröße der ausgelieferten Datensätze.
	 */
	public int deliver() {
		final int deliveredSize;
		final ResultData[] results;
		synchronized(this) {
			deliveredSize = _collectedSize;
			results = _results.toArray(new ResultData[_results.size()]);
			final boolean trim = _results.size() > 100;
			_results.clear();
			if(trim) {
				_results.trimToSize();
				_results.ensureCapacity(10);
			}
			_collectedSize = 0;
		}
		if(results.length != 0) {
			for(ResultData result : results) {
				Data data = result.getData();
				if(data instanceof ByteArrayData) {
					ByteArrayData byteArrayData = (ByteArrayData) data;
					byteArrayData.resolveReferences();
				}
			}
			_receiver.update(results);
		}
		return (deliveredSize);
	}

	/** Erhöht den Referenzzähler um eins. */
	public void incrementReferenceCount() {
		++_referenceCount;
	}

	/**
	 * Verringert den Referenzzähler um eins.
	 *
	 * @return <code>true</code>, wenn der Referenzzähler den Wert 0 erreicht hat.
	 */
	public boolean decrementReferenceCount() {
		if(--_referenceCount == 0) return true;
		if(_referenceCount > 0) return false;
		throw new IllegalStateException("Referenzzähler wurde decrementiert, obwohl er nicht mehr positiv war");
	}
}
