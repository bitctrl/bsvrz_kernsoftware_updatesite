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

/**
 * Diese Klasse repräsentiert den Status einer Meldung. Fünf verschiedene Zustände erlaubt diese Klasse: <ul>
 * <li>Fehlermeldung</li>Meldung, die keinem der nachfolgenden Zustände entspricht oder wo der Status nicht ermittelt
 * werden kann. <li>Gutmeldung</li>Meldung, die zu einer zuvor gesandten Meldung gehört und deren Inhalt wieder aufhebt.
 * <li>Neue Meldung</li>Meldung, die zum ersten Mal erstellt wird. <li>Wiederholungsmeldung</li>Meldung, die zu einer
 * bereits zuvor gesendeten Meldung gehört und deren Inhalt wiederholt. <li>Änderungsmeldung</li>Meldung, die zu einer
 * zuvor gesendeten Meldung gehört und deren Inhalt modifiziert. </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public enum MessageState {
	MESSAGE ("Meldung"),
	GOOD_MESSAGE ("Gutmeldung"),
	NEW_MESSAGE ("Neue Meldung"),
	REPEAT_MESSAGE ("Wiederholungsmeldung"),
	CHANGE_MESSAGE ("Änderungsmeldung");

	private final String _state;

	private MessageState(String state) {
		_state = state;
	}

	public String getState() {
		return _state;
	}

	public String toString() {
		return _state;
	}
}
