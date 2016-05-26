/*
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Dieses Enum listet alle möglichen Gültigkeitszeitraum-Typen auf. Er gibt die Art und Weise an, wie der
 * Gültigkeitszeitraum zu betrachten ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public enum TimeSpecificationType {
	/**
	 * Für Objekte, die aktuell gültig sind.
	 */
	VALID((short)1),

	/**
	 * Für Objekte, die zu einem bestimmten Zeitpunkt gültig waren.
	 */
	VALID_AT_TIME((short)2),

	/**
	 * Für Objekte, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs gültig waren.
	 */
	VALID_IN_PERIOD((short)3),

	/**
	 * Für Objekte, die während des gesamten Zeitraumes gültig waren.
	 */
	VALID_DURING_PERIOD((short)4);

	public static final TimeSpecificationType getInstance(short code)
	{
		switch(code){
			case 1: return VALID;
			case 2: return VALID_AT_TIME;
			case 3: return VALID_IN_PERIOD;
			case 4: return VALID_DURING_PERIOD;
			default: throw new IllegalArgumentException("Die Zahl " + code + " kann nicht in ein Objekt umgewandlet werden.");
		}
	}

	/**
	 * Kode, der den Zustand eindeutig identifiziert.
	 */
	private final short _code;

	TimeSpecificationType(final short code) {
		_code = code;
	}

	/**
	 * Eindeutige Id des Zustands. Diese ID kann benutzt werden um das Objekt zu serialisieren und später wieder zu deserialisieren.
	 * @return Zahl, die den Zustand eindeutig identifiziert.
	 */
	public short getCode() {
		return _code;
	}
}
