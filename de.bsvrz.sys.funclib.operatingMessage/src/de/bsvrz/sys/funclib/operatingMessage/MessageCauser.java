/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.sys.funclib.operatingMessage;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Diese Klasse stellt die Urlasserinformationen dar. Die Urlasserinformationen bestehen aus einer Referenz auf den
 * Benutzer, der die Betriebsmeldung erzeugt hat, einer Angabe der Ursache für die Meldung und eines Veranlassers für
 * die Meldung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class MessageCauser {
	private final SystemObject _user;
	private final String _cause;
	private final String _causer;

	public MessageCauser(SystemObject user, String cause, String causer) {
		_user = user;
		_cause = cause;
		_causer = causer;
	}

	/**
	 * Gibt den Benutzer zurück, der die Betriebsmeldung erzeugt hat.
	 *
	 * @return der Benutzer
	 */
	public SystemObject getUser() {
		return _user;
	}

	/**
	 * Gibt die Ursache für die Betriebsmeldung zurück.
	 *
	 * @return die Ursache
	 */
	public String getCause() {
		return _cause;
	}

	/**
	 * Gibt den Veranlasser für die Betriebsmeldung zurück.
	 *
	 * @return der Veranlasser
	 */
	public String getCauser() {
		return _causer;
	}
}
