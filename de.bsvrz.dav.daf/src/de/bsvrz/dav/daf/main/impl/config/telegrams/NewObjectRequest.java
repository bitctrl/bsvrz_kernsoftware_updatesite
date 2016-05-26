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
 * Diese Klasse stellt ein NewObject-Anfragetelegramm dar.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class NewObjectRequest extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long _configTime;

	/** Die ID des objektes */
	private long _id;

	/** Die PID des objektes */
	private String _pid;

	/** Der Name des objektes */
	private String _name;

	/** Die ID des typs */
	private long _typeId;

	/** Die Ids der Mengen */
	private long _setIds[];

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public NewObjectRequest() {
		_type = NEW_OBJECT_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configTime Konfigurationszeit
	 * @param id         Objekt-ID
	 * @param pid        permanente Id des Objektes
	 * @param name       Name des Objektes
	 * @param typeId     ID des Typs
	 * @param setIds     IDs der Mengen
	 */
	public NewObjectRequest(long configTime, long id, String pid, String name, long typeId, long setIds[]) {
		_type = NEW_OBJECT_REQUEST_TYPE;
		_configTime = configTime;
		_id = id;
		_pid = pid;
		_name = name;
		_typeId = typeId;
		_setIds = setIds;
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
	 * Liefert die Objekt-ID zurück.
	 *
	 * @return Objekt ID
	 */
	public final long getId() {
		return _id;
	}

	/**
	 * Liefert die permanente ID (PID) des Objekts zurück.
	 *
	 * @return permanente Id des Objektes
	 */
	public final String getPid() {
		return _pid;
	}

	/**
	 * Gibt den Namen des Objektes zurück
	 *
	 * @return der Name
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * Gibt die ID des Typs zurück
	 *
	 * @return Die ID des Typs
	 */
	public final long getTypeId() {
		return _typeId;
	}

	/**
	 * Gibt die IDs der Mengen zurück
	 *
	 * @return Die IDs der Mengen
	 */
	public final long[] getSetIds() {
		return _setIds;
	}

	public final String parseToString() {
		String str = "Neues Objekt Anfrage: \n";
		str += "Konfigurationszeit: " + _configTime + "\n";
		str += "ID: " + _id + "\n";
		str += "PID: " + _pid + "\n";
		str += "Name: " + _name + "\n";
		str += "Typ Id: " + _typeId + "\n";
		str += "Mengen: [ \n";
		if(_setIds != null) {
			for(int i = 0; i < _setIds.length; ++i) {
				str += _setIds[i] + " ";
			}
		}
		str += "]";
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeLong(_configTime);
		out.writeLong(_id);
		out.writeLong(_typeId);
		boolean b = (_pid != null);
		out.writeBoolean(b);
		if(b) {
			out.writeUTF(_pid);
		}
		b = (_name != null);
		out.writeBoolean(b);
		if(b) {
			out.writeUTF(_name);
		}
		if(_setIds == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_setIds.length);
			for(int i = 0; i < _setIds.length; ++i) {
				out.writeLong(_setIds[i]);
			}
		}
	}

	public void read(DataInputStream in) throws IOException {
		_configTime = in.readLong();
		_id = in.readLong();
		_typeId = in.readLong();
		boolean b = in.readBoolean();
		if(b) {
			_pid = in.readUTF();
		}
		b = in.readBoolean();
		if(b) {
			_name = in.readUTF();
		}
		int size = in.readInt();
		if(size > 0) {
			_setIds = new long[size];
			for(int i = 0; i < size; ++i) {
				_setIds[i] = in.readLong();
			}
		}
	}
}
