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

import de.bsvrz.dav.daf.main.impl.config.DafDataModel;
import de.bsvrz.dav.daf.main.impl.config.DafSystemObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein Metadaten-Antworttelegramm dar. Es werden die Metadaten gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * 
 */
public class MetaDataAnswer extends ConfigTelegram {

	/** Version des Kommunikationsprotokolls, das von der Konfiguration unterstützt wird */
	private long _protocolVersion;

	/** Die Meta-Objekte */
	private DafSystemObject _objects[];

	/** Das Datenmodel */
	private DafDataModel _dataModel;

	public MetaDataAnswer(DafDataModel dataModel) {
		_type = META_DATA_ANSWER_TYPE;
		_dataModel = dataModel;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param protocolVersion    Konfigurationszeit
	 * @param objects       Konfigurationsobjekte
	 * @param dataModel     Datenmodel
	 */
	public MetaDataAnswer(long protocolVersion, DafSystemObject objects[], DafDataModel dataModel
	) {
		_type = META_DATA_ANSWER_TYPE;
		_protocolVersion = protocolVersion;
		_objects = objects;
		_dataModel = dataModel;
	}

	/**
	 * Gibt die Konfigurationszeit zurück
	 *
	 * @return Die Konfigurationszeit
	 */
	public final long getProtocolVersion() {
		return _protocolVersion;
	}

	/**
	 * Gibt die Konfigurationsobjekte zurück
	 *
	 * @return Die Konfigurationsobjekte
	 */
	public final DafSystemObject[] getObjects() {
		return _objects;
	}

	public final String parseToString() {
		String str = "Metakonfigurationsantwort: \n";
		str += "Protokollversion: " + _protocolVersion + "\n";
		str += "Meta-Objekte: \n";
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

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_protocolVersion);
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

	public final void read(DataInputStream in) throws IOException {
		_protocolVersion = in.readLong();
		if(_protocolVersion > Integer.MAX_VALUE || _protocolVersion < 0){
			_protocolVersion = 0;
		}
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
