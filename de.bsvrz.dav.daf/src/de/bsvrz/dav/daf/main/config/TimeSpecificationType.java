/*
 * Copyright 2006 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Dieses Enum listet alle m�glichen G�ltigkeitszeitraum-Typen auf. Er gibt die Art und Weise an, wie der
 * G�ltigkeitszeitraum zu betrachten ist.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public enum TimeSpecificationType {
	/**
	 * F�r Objekte, die aktuell g�ltig sind.
	 */
	VALID((short)1),

	/**
	 * F�r Objekte, die zu einem bestimmten Zeitpunkt g�ltig waren.
	 */
	VALID_AT_TIME((short)2),

	/**
	 * F�r Objekte, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs g�ltig waren.
	 */
	VALID_IN_PERIOD((short)3),

	/**
	 * F�r Objekte, die w�hrend des gesamten Zeitraumes g�ltig waren.
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
	 * Eindeutige Id des Zustands. Diese ID kann benutzt werden um das Objekt zu serialisieren und sp�ter wieder zu deserialisieren.
	 * @return Zahl, die den Zustand eindeutig identifiziert.
	 */
	public short getCode() {
		return _code;
	}
}
