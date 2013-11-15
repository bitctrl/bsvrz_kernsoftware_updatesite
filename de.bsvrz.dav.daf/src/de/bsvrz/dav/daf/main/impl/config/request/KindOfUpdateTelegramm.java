/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.request;

/**
 * Enth�lt alle Typen von Telegrammen, die von der Konfiguration verschickt werden um anzuzeigen, dass sich Werte von Objekten ge�ndert haben.
 * <p/>
 * Anhand des Typs kann erkannt werden, wie der Byte-Strom zu interpretieren ist, der im Datensatz enthalten ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5060 $
 */
public enum KindOfUpdateTelegramm {

	/** Der Name eines Objekts hat sich ge�ndert. */
	UPDATE_NAME("Name ge�ndert", (byte)1),
	/** Der Zeitpunkt, an dem das Objekt ung�ltig wurde, hat sich ge�ndert */
	UPDATE_NOT_VALID_SINCE("Objekt ung�ltig ab", (byte)2),

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
