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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import de.bsvrz.dav.daf.main.impl.config.DafSystemObject;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein NewObject-Antworttelegramm dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class NewObjectAnswer extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long _configTime;

	/** Die Meta-Objekte */
	private DafSystemObject _object;

	/** Das Datenmodel */
	private DafDataModel _dataModel;

	public NewObjectAnswer(DafDataModel dataModel) {
		_type = NEW_OBJECT_ANSWER_TYPE;
		_dataModel = dataModel;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configTime Konfigurationszeit
	 * @param object     das Objekt
	 * @param dataModel  Datenmodel
	 */
	public NewObjectAnswer(long configTime, DafSystemObject object, DafDataModel dataModel) {
		_type = NEW_OBJECT_ANSWER_TYPE;
		_configTime = configTime;
		_object = object;
		_dataModel = dataModel;
	}

	/**
	 * Gibt die Konfigurationszeit zurück
	 *
	 * @return Die Konfigurationszeit
	 */
	public final long getConfigTime() {
		return _configTime;
	}

	/**
	 * Gibt das Objekt zurück
	 *
	 * @return Das Objekt
	 */
	public final DafSystemObject getObject() {
		return _object;
	}

	public final String parseToString() {
		String str = "Neues Objekt Antwort: \n";
		str += "Konfigurationszeit: " + _configTime + "\n";
		if(_object == null) {
			str += "Null Objekt";
		}
		else {
			str += _object.parseToString();
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_configTime);
		if(_object == null) {
			out.writeByte(DafSystemObject.NULL_OBJECT);
		}
		else {
			out.writeByte(_object.getInternType());
			_object.write(out);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		_configTime = in.readLong();
		byte internType = in.readByte();
		_object = DafSystemObject.getObject(internType, _dataModel);
		if(_object != null) {
			_object.read(in);
		}
	}
}
