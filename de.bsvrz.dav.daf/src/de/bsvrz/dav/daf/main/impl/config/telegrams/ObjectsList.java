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

import de.bsvrz.dav.daf.main.impl.config.DafSystemObject;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Telegramm dar, welches eine Liste von Objekten des gleichen Typs speichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class ObjectsList {

	/** Die Id des Typs */
	private long _baseObjectId;

	/** Die Objekte des Typs */
	private DafSystemObject _objects[];

	/** Das Datenmodel */
	private DafDataModel _dataModel;

	/**
	 * Erzeugt ein neues Objekt mit generalisiertem Parameter. Die spezifischen Parameter werden zu einem sp�teren Zeitpunkt �ber die read-Methode eingelesen.
	 *
	 * @param dataModel Datenmodel
	 */
	public ObjectsList(DafDataModel dataModel) {
		_dataModel = dataModel;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param baseObjectId Id des Typs
	 * @param objects      Objekte des Typs
	 * @param dataModel    Datenmodel
	 */
	public ObjectsList(long baseObjectId, DafSystemObject[] objects, DafDataModel dataModel) {
		_baseObjectId = baseObjectId;
		_objects = objects;
		_dataModel = dataModel;
	}

	/**
	 * Gibt die Id des Typs zur�ck.
	 *
	 * @return ID des Typs
	 */
	public final long getBaseObjectId() {
		return _baseObjectId;
	}

	/**
	 * Gibt die Objekte des Typs zur�ck.
	 *
	 * @return Objekte des typs
	 */
	public final DafSystemObject[] getObjects() {
		return _objects;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts f�r Debug-Zwecke zur�ck.
	 *
	 * @return Beschreibender Text dieses Objekts.
	 */
	public final String parseToString() {
		String str = "Typ Id: " + _baseObjectId + "\n";
		if(_objects != null) {
			for(int i = 0; i < _objects.length; ++i) {
				if(_objects[i] == null) {
					str += "Null Objekt";
				}
				else {
					str += _objects[i].parseToString();
				}
			}
		}
		return str;
	}

	/**
	 * Serialisiert dieses Objekt.
	 *
	 * @param out Stream auf den das Objekt geschrieben werden soll.
	 *
	 * @throws IOException, wenn beim Schreiben auf den Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_baseObjectId);
		if(_objects == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_objects.length);
			for(int i = 0; i < _objects.length; ++i) {
				if(_objects[i] == null) {
					out.writeByte(DafSystemObject.NULL_OBJECT);
				}
				else {
					out.writeByte(_objects[i].getInternType());
					_objects[i].write(out);
				}
			}
		}
	}

	/**
	 * Deserialisiert dieses Objekt.
	 *
	 * @param in Stream von dem das Objekt gelesen werden soll.
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public final void read(DataInputStream in) throws IOException {
		_baseObjectId = in.readLong();
		int size = in.readInt();
		if(size > 0) {
			_objects = new DafSystemObject[size];
			for(int i = 0; i < size; ++i) {
				byte internType = in.readByte();
				_objects[i] = DafSystemObject.getObject(internType, _dataModel);
				if(_objects[i] != null) {
					_objects[i].read(in);
				}
			}
		}
	}
}
