/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.operatingMessage;

/**
 * Diese Klasse repr�sentiert den Status einer Meldung. F�nf verschiedene Zust�nde erlaubt diese Klasse: <ul>
 * <li>Fehlermeldung</li>Meldung, die keinem der nachfolgenden Zust�nde entspricht oder wo der Status nicht ermittelt
 * werden kann. <li>Gutmeldung</li>Meldung, die zu einer zuvor gesandten Meldung geh�rt und deren Inhalt wieder aufhebt.
 * <li>Neue Meldung</li>Meldung, die zum ersten Mal erstellt wird. <li>Wiederholungsmeldung</li>Meldung, die zu einer
 * bereits zuvor gesendeten Meldung geh�rt und deren Inhalt wiederholt. <li>�nderungsmeldung</li>Meldung, die zu einer
 * zuvor gesendeten Meldung geh�rt und deren Inhalt modifiziert. </ul>
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5007 $
 */
public enum MessageState {
	MESSAGE ("Meldung"),
	GOOD_MESSAGE ("Gutmeldung"),
	NEW_MESSAGE ("Neue Meldung"),
	REPEAT_MESSAGE ("Wiederholungsmeldung"),
	CHANGE_MESSAGE ("�nderungsmeldung");

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
