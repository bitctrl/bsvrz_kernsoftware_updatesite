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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Aktualisierungselegramm der Weginformationen dar. Es werden ID des DAV, die Gewichtung der Verbindung und die Liste der involvierten
 * DAV gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5047 $
 */
public class RoutingUpdate {

	/** Id des Ausgangsdatenverteilers*/
	private long _transmitterId;

	/** Gewichtung der Verbindung */
	private short _transmitterWeight;

	/** Involvierte Datenverteiler dieser Verbindung */
	private long[] _involvedTransmitters;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public RoutingUpdate() {
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param transmitter          Id des Ausgangsdatenverteilers
	 * @param weight               Gewichtung der Verbindung
	 * @param transmittersInvolved Involvierte Datenverteiler dieser Verbindung
	 */
	public RoutingUpdate(long transmitter, short weight, long[] transmittersInvolved) {
		_transmitterId = transmitter;
		_transmitterWeight = weight;
		_involvedTransmitters = transmittersInvolved;
	}

	/**
	 * Die Id des Datenverteilers
	 *
	 * @return Id des Ausgangsdatenverteilers
	 */
	public final long getTransmitterId() {
		return _transmitterId;
	}

	/**
	 * Die Gewichtung der Verbindung.
	 *
	 * @return Gewichtung der Verbindung
	 */
	public final short getThroughputResistance() {
		return _transmitterWeight;
	}

	/**
	 * Die involvierten Datenverteiler dieser Verbindung.
	 *
	 * @return Involvierte Datenverteiler dieser Verbindung
	 */
	public final long[] getInvolvedTransmitterIds() {
		return _involvedTransmitters;
	}

	/**
	 * Gibt ein String zur�ck, der dieses Datensatzes beschreibt
	 *
	 * @return Der String, der dieses Datensatzes beschreibt
	 */
	public final String parseToString() {
		String str = "Datenverteiler: " + _transmitterId + "\n";
		str += "Gewichtung: " + _transmitterWeight + "\n";
		if(_involvedTransmitters != null) {
			str += "Beteiligte Datenverteiler: [ ";
			for(int i = 0; i < _involvedTransmitters.length; ++i) {
				str += _involvedTransmitters[i] + "  ";
			}
			str += " ]\n";
		}
		return str;
	}

	/**
	 * Schreibt ein Objekt in den gegebenen DataOutputStream.
	 *
	 * @param out DataOutputStream
	 *
	 * @throws IOException, wenn beim Schreiben in den Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_transmitterId);
		out.writeShort(_transmitterWeight);
		if(_involvedTransmitters == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(_involvedTransmitters.length);
			for(int i = 0; i < _involvedTransmitters.length; ++i) {
				out.writeLong(_involvedTransmitters[i]);
			}
		}
	}

	/**
	 * Liest ein Objekt aus dem gegebenen DataInputStream.
	 *
	 * @param in DataInputStrea
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public final void read(DataInputStream in) throws IOException {
		_transmitterId = in.readLong();
		_transmitterWeight = in.readShort();
		int size = in.readShort();
		if(size > 0) {
			_involvedTransmitters = new long[size];
			for(int i = 0; i < size; ++i) {
				_involvedTransmitters[i] = in.readLong();
			}
		}
	}

	/**
	 * Gibt die L�nge dieses Objekts in bytes zur�ck
	 *
	 * @return die L�nge dieses Telegrams
	 */
	public final int getLength() {
		return 12 + (_involvedTransmitters == null ? 0 : _involvedTransmitters.length * 8);
	}
}
