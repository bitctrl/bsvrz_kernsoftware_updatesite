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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt ein generelles Anfragetelegramm dar. In einer Fallunterscheidung des Anfragetyps werden die Antworttelegramme nach den entsprechenden
 * Anforderungen erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class SystemObjectsRequest extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long _configTime;

	/** Informationen zur Anfrage */
	private SystemObjectRequestInfo _systemObjectRequestInfo;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public SystemObjectsRequest() {
		_type = OBJECT_REQUEST_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configTime              Konfigurationszeit
	 * @param systemObjectRequestInfo Informationen zur Anfrage
	 */
	public SystemObjectsRequest(long configTime, SystemObjectRequestInfo systemObjectRequestInfo) {
		_type = OBJECT_REQUEST_TYPE;
		_configTime = configTime;
		_systemObjectRequestInfo = systemObjectRequestInfo;
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
	 * Gibt die Information zur Anfrage zurück
	 *
	 * @return Die Information zur Anfrage
	 */
	public final SystemObjectRequestInfo getSystemObjectRequestInfo() {
		return _systemObjectRequestInfo;
	}

	public final String parseToString() {
		String str = "Objektsanfrage: \n";
		str += "Konfigurationszeit: " + _configTime + "\n";
		if(_systemObjectRequestInfo != null) {
			str += _systemObjectRequestInfo.parseToString();
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_configTime);
		out.writeByte(_systemObjectRequestInfo.getRequestType());
		_systemObjectRequestInfo.write(out);
	}

	public final void read(DataInputStream in) throws IOException {
		_configTime = in.readLong();
		byte requestType = in.readByte();
		switch(requestType) {
			case(SystemObjectRequestInfo.IDS_TO_OBJECTS_TYPE): {
				_systemObjectRequestInfo = new IdsToObjectsRequest();
				break;
			}
			case(SystemObjectRequestInfo.PIDS_TO_OBJECTS_TYPE): {
				_systemObjectRequestInfo = new PidsToObjectsRequest();
				break;
			}
			case(SystemObjectRequestInfo.TYPE_IDS_TO_OBJECTS_TYPE): {
				_systemObjectRequestInfo = new TypeIdsToObjectsRequest();
				break;
			}
		}
		if(_systemObjectRequestInfo != null) {
			_systemObjectRequestInfo.read(in);
		}
	}
}
