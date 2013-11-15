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
 * Diese Klasse stellt ein Antworttelegramm zur Ung�ltigkeitserkl�rung dar. Durch dieses Telegramm  Ung�ltigkeit eines Objektes best�tigt bzw das Fehlschlagen
 * der Ung�ltigkeitserkl�rung mitgeteilt
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class ObjectInvalidateAnswer extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long _configTime;

	/** Die Id des Objektes */
	private long _objectId;

	/** Die Information ob die Aktion erfolgreich durchgef�hrt worden ist */
	private boolean _success;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Sp�teren Zeitpunkt �ber die read-Methode eingelesen. */
	public ObjectInvalidateAnswer() {
		_type = OBJECT_INVALIDATE_ANSWER_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param configTime Zeitpunkt in Sekunden seit 1970 an dem das Objekt ung�ltig geworden ist.
	 * @param objectId   Id des Objektes
	 * @param success    true erfolgreich invalidiert, false nicht erfolgreich invalidiert
	 */
	public ObjectInvalidateAnswer(long configTime, long objectId, boolean success) {
		_type = OBJECT_INVALIDATE_ANSWER_TYPE;
		_configTime = configTime;
		_objectId = objectId;
		_success = success;
	}

	/**
	 * Zeitpunkt an dem das Objekt ung�ltig geworden ist
	 *
	 * @return Zeitpunkt in Sekunden seit 1970 an dem das Objekt ung�ltig geworden ist.
	 */
	public final long getConfigTime() {
		return _configTime;
	}

	/**
	 * Gibt die Id des Objektes zur�ck
	 *
	 * @return Die Id des Objektes
	 */
	public final long getObjectId() {
		return _objectId;
	}

	/**
	 * Gibt an, ob das Objekt erfolgreich invalidiert oder nicht
	 *
	 * @return true erfolgreich invalidiert, false nicht erfolgreich invalidiert
	 */
	public final boolean isInvalidated() {
		return _success;
	}

	public final String parseToString() {
		String str = "Objektinvalidation Antwort: \n";
		str += "Objekt Id: " + _objectId + "\n";
		if(_success) {
			str += "Invalidation des Objektes erfolgreich\n";
		}
		else {
			str += "Invalidation des Objektes nicht erfolgreich\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_configTime);
		out.writeLong(_objectId);
		out.writeBoolean(_success);
	}

	public final void read(DataInputStream in) throws IOException {
		_configTime = in.readLong();
		_objectId = in.readLong();
		_success = in.readBoolean();
	}
}
