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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Telegramm zur Aktualisierung der Matrix der günstigsten Wege. Mit diesem Systemtelegramm werden die Matrizen der günstigsten Wege zwischen Datenverteilern
 * aktualisiert. Dazu schickt Datenverteiler A seinem Nachbar Datenverteiler B eine Liste von ihm aus erreichbarer Datenverteiler mit den entsprechenden
 * Verbindungsbewertungen durch die Angabe der Wichtung des Weges.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransmitterBestWayUpdate extends DataTelegram {

	/** Die Datenverteilergewichtungsinformationen */
	private RoutingUpdate _routingUpdates[];

	public TransmitterBestWayUpdate() {
		type = TRANSMITTER_BEST_WAY_UPDATE_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues TransmitterBestWayUpdate
	 *
	 * @param routingUpdates Datenverteilergewichtungsinformationen
	 */
	public TransmitterBestWayUpdate(RoutingUpdate routingUpdates[]) {
		type = TRANSMITTER_BEST_WAY_UPDATE_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_routingUpdates = routingUpdates;
		length = 2;
		if(_routingUpdates != null) {
			for(int i = 0; i < _routingUpdates.length; ++i) {
				length += _routingUpdates[i].getLength();
			}
		}
	}

	/**
	 * Gibt die Gewichtungsaktuallisierungen an.
	 *
	 * @return Gewichtungsaktuallisierungen
	 */
	public final RoutingUpdate[] getRoutingUpdates() {
		return _routingUpdates;
	}

	public final String parseToString() {
		String str = "Systemtelegramm beste Wege Aktuallisierung:\n";
		if(_routingUpdates != null) {
			for(int i = 0; i < _routingUpdates.length; ++i) {
				str += _routingUpdates[i].parseToString();
			}
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		if(_routingUpdates == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(_routingUpdates.length);
			for(int i = 0; i < _routingUpdates.length; ++i) {
				_routingUpdates[i].write(out);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		length = 2;
		int size = in.readShort();
		if(size > 0) {
			_routingUpdates = new RoutingUpdate[size];
			for(int i = 0; i < size; ++i) {
				_routingUpdates[i] = new RoutingUpdate();
				_routingUpdates[i].read(in);
				length += _routingUpdates[i].getLength();
			}
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
