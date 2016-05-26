/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.request;

/**
 * Enthält alle Typen von Telegrammen, die von der Konfiguration verschickt werden um anzuzeigen, dass sich Werte von Objekten geändert haben.
 * <p>
 * Anhand des Typs kann erkannt werden, wie der Byte-Strom zu interpretieren ist, der im Datensatz enthalten ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public enum KindOfUpdateTelegramm {

	/** Der Name eines Objekts hat sich geändert. */
	UPDATE_NAME("Name geändert", (byte)1),
	/** Der Zeitpunkt, an dem das Objekt ungültig wurde, hat sich geändert */
	UPDATE_NOT_VALID_SINCE("Objekt ungültig ab", (byte)2),

	/** Es wurde ein neues dynamisches Objekt erzeugt */
	CREATED("Neues Objekt", (byte)3);

	public static KindOfUpdateTelegramm getInstance(byte code) {
		switch(code) {
			case 1:
				return UPDATE_NAME;
			case 2:
				return UPDATE_NOT_VALID_SINCE;
			case 3:
				return CREATED;
			default:
				throw new IllegalArgumentException("Die Zahl " + code + " kann in keinen Telegrammtypen umgewandelt werden.");
		}
	}

	private final String _name;

	private final byte _code;

	private KindOfUpdateTelegramm(String name, byte code) {
		_name = name;
		_code = code;
	}


	public byte getCode() {
		return _code;
	}

	public String getName() {
		return _name;
	}

	public String toString() {
		return "KindOfUpdateTelegramm: " + getName() + " Code: " + getCode();
	}
}
