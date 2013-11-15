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
 * Diese Klasse stellt ein Antworttelegramm zur Names�nderung eines Objektes dar. Es wird die �nderung des names best�tigt bzw das Fehlschlagen der �nderung
 * mitgeteilt
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class ObjectSetNameAnswer extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long configTime;

	/** Die Id des Objektes */
	private long objectId;

	/** Die Information ob die Aktion erfolgreich durchgef�hrt worden ist */
	private boolean success;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public ObjectSetNameAnswer() {
		_type = OBJECT_SET_NAME_ANSWER_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param _configTime Konfigurationszeit
	 * @param _objectId   Id des Objektes
	 * @param _success    true Name wurde ge�ndert, false Name wurde nicht ge�ndert
	 */
	public ObjectSetNameAnswer(long _configTime, long _objectId, boolean _success) {
		_type = OBJECT_SET_NAME_ANSWER_TYPE;
		configTime = _configTime;
		objectId = _objectId;
		success = _success;
	}

	/**
	 * Gibt die Konfigurationszeit zur�ck
	 *
	 * @return Die Konfigurationszeit
	 */
	public final long getConfigTime() {
		return configTime;
	}

	/**
	 * Gibt die Id des Objektes zur�ck
	 *
	 * @return Die Id des Objektes
	 */
	public final long getObjectId() {
		return objectId;
	}

	/**
	 * Gibt an, ob der Objektname erfolgreich ge�ndert worden ist oder nicht.
	 *
	 * @return true Name wurde ge�ndert, false Name wurde nicht ge�ndert
	 */
	public final boolean isNameSet() {
		return success;
	}

	public final String parseToString() {
		String str = "Setzen der Name eines Objektes Antwort: \n";
		str += "Objekt Id: " + objectId + "\n";
		if(success) {
			str += "Der Name des Objektes wurde erfolgreich gesetzt\n";
		}
		else {
			str += "Der Name des Objektes wurde nicht erfolgreich gesetzt\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(configTime);
		out.writeLong(objectId);
		out.writeBoolean(success);
	}

	public final void read(DataInputStream in) throws IOException {
		configTime = in.readLong();
		objectId = in.readLong();
		success = in.readBoolean();
	}
}
