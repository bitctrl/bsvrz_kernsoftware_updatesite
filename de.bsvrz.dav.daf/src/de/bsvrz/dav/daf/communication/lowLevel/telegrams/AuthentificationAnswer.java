/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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
 * Nach der erfolgreichen Authentifizierung sendet der Datenverteiler ein AuthenficationAnswer-Telegram zurück
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AuthentificationAnswer extends DataTelegram {

	/** Gibt an, ob die Authentifizierung erfolgreich war oder nicht.
	 */
	private boolean _successfullyAuthentified;

	/** Die ID des Benutzers. */
	private long _userId;

	/** Die ID der anfragenden Applikation. */
	private long _applicationId;

	/** Die ID der lokalen Konfiguration. */
	private long _localeConfigurationId;

	/** Die ID des lokalen Datenverteilers. */
	private long _localeDVId;

	public AuthentificationAnswer() {
		type = AUTHENTIFICATION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_userId = -1;
		_applicationId = -1;
		_localeConfigurationId = -1;
		_localeDVId = -1;
	}

	/**
	 * Dieser Konstruktor wird im Falle einer erfolgreichen Authentifizierung verwendet.
	 *
	 * @param userId                die ID des Benutzers
	 * @param applicationId         die ID der anfragenden Applikation
	 * @param localeConfigurationId die ID der lokalen Konfiguration
	 * @param localeDVId            die ID des lokalen Datenverteilers
	 */
	public AuthentificationAnswer(long userId, long applicationId, long localeConfigurationId, long localeDVId) {
		type = AUTHENTIFICATION_ANSWER_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		_successfullyAuthentified = true;
		_userId = userId;
		_applicationId = applicationId;
		_localeConfigurationId = localeConfigurationId;
		_localeDVId = localeDVId;
		length = 33;
	}

	/**
	 * Dieser Konstruktor wird im Falle einer nicht erfolgreichen Authentifizierung verwendet.
	 *
	 * @param successfullyAuthentified <code>false</code>, wenn die Authentifizierung nicht erfolgreich war. <code>true</code> wird nicht beachtet.
	 */
	public AuthentificationAnswer(boolean successfullyAuthentified) {
		this(); // ruft den Standardkonstruktor zum initialisieren der internen Variablen auf
		_successfullyAuthentified = false;
		length = 1;
	}

	/**
	 * Gibt die ID des Benutzers zurück.
	 *
	 * @return die ID des Benutzers
	 */
	public final long getUserId() {
		return _userId;
	}

	/**
	 * Gibt die ID der Applikation zurück.
	 *
	 * @return die ID der Applikation
	 */
	public final long getApplicationId() {
		return _applicationId;
	}

	/**
	 * Gibt die lokale Konfiguration-ID zurück.
	 *
	 * @return die lokale Konfiguration-ID
	 */
	public final long getLocaleConfigurationId() {
		return _localeConfigurationId;
	}

	/**
	 * Gibt die ID des lokalen Datenverteilers zurück.
	 *
	 * @return die ID des lokalen Datenverteilers
	 */
	public final long getLocaleDVId() {
		return _localeDVId;
	}

	/**
	 * Gibt an, ob der Authentifizierungsvorgang erfolgreich war.
	 *
	 * @return <code>true</code>, falls die Authentifizierung erfolgreich war, sonst <code>false</code>
	 */
	public final boolean isSuccessfullyAuthentified() {
		return _successfullyAuthentified;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Authentifikation Antwort:   \n";
		if(_successfullyAuthentified) {
			str += "Status            : Erfolgreich\n";
			str += "Applikation Id    : " + _applicationId + "\n";
			str += "Benutzer Id       : " + _userId + "\n";
			str += "Konfiguration Id  : " + _localeConfigurationId + "\n";
			str += "Datenverteiler Id : " + _localeDVId + "\n";
		}
		else {
			str += "Status             : Nicht erfolgreich\n";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		out.writeBoolean(_successfullyAuthentified);
		if(_successfullyAuthentified) {
			out.writeLong(_applicationId);
			out.writeLong(_userId);
			out.writeLong(_localeConfigurationId);
			out.writeLong(_localeDVId);
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_successfullyAuthentified = in.readBoolean();
		if(_successfullyAuthentified) {
			_applicationId = in.readLong();
			_userId = in.readLong();
			_localeConfigurationId = in.readLong();
			_localeDVId = in.readLong();
			length = 33;
		}
		else {
			length = 1;
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
