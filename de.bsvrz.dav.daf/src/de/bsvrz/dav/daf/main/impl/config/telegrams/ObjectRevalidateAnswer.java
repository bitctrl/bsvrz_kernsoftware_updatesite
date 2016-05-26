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
 * Diese Klasse stellt ein Antworttelegramm zur Gültigkeitserklärung dar. Durch dieses Telegramm  die erneute Gültigkeit eines Objektes bestätigt bzw das
 * Fehlschlagen der Gültigkeitserklärung mitgeteilt
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ObjectRevalidateAnswer extends ConfigTelegram {

	/** Die Konfigurationszeit */
	private long configTime;

	/** Die Id des Objektes */
	private long objectId;

	/** Die Information ob die Aktion erfolgreich durchgeführt */
	private boolean success;

	/** Erzeugt ein neues Objekt ohne Parameter. Die Parameter werden zu einem Späteren Zeitpunkt über die read-Methode eingelesen. */
	public ObjectRevalidateAnswer() {
		_type = OBJECT_REVALIDATE_ANSWER_TYPE;
	}

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param _configTime Konfigurationszeit
	 * @param _objectId   Id des Objektes
	 * @param _success    true erfolgreich revalidiert, false nicht erfolgreich revalidiert
	 */
	public ObjectRevalidateAnswer(long _configTime, long _objectId, boolean _success) {
		_type = OBJECT_REVALIDATE_ANSWER_TYPE;
		configTime = _configTime;
		objectId = _objectId;
		success = _success;
	}

	/**
	 * Gibt die Konfigurationszeit zurück
	 *
	 * @return Die Konfigurationszeit
	 */
	public final long getConfigTime() {
		return configTime;
	}

	/**
	 * Gibt die Id des Objektes zurück
	 *
	 * @return Die Id des Objektes
	 */
	public final long getObjectId() {
		return objectId;
	}

	/**
	 * Gibt ob der Objekt erfolgreich revalidiert oder nicht
	 *
	 * @return true erfolgreich revalidiert, false nicht erfolgreich revalidiert
	 */

	public final boolean isRevalidated() {
		return success;
	}

	public final String parseToString() {
		String str = "Objektrevalidation Antwort: \n";
		str += "Objekt Id: " + objectId + "\n";
		if(success) {
			str += "Revalidation des Objektes erfolgreich\n";
		}
		else {
			str += "Revalidation des Objektes nicht erfolgreich\n";
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
