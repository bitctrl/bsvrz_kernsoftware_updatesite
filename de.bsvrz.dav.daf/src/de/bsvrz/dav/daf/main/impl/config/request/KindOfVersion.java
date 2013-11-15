/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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
 * Stellt alle Möglichkeiten dar, in der ein Objekt in einer Version gültig sein kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5060 $
 */
public enum KindOfVersion {

	/** In der nächsten Version */
	IN_NEXT_VERSION("Ab der nächsten Version", (byte)1),
	/** In der derzeit benutzten Version */
	IN_VERSION("In der jetzigen Version", (byte)2),
	/** In allen Versionen */
	IN_ALL_VERSIONS("In allen Versionen", (byte)3),
	/** In jeder Version */
	IN_ANY_VERSIONS("In jeder Version", (byte)4);


	public static KindOfVersion getInstance(byte code) {
		switch(code) {
			case 1:
				return IN_NEXT_VERSION;
			case 2:
				return IN_VERSION;
			case 3:
				return IN_ALL_VERSIONS;
			case 4:
				return IN_ANY_VERSIONS;
			default:
				throw new IllegalArgumentException("Die Zahl " + code + " kann in keinen Telegrammtypen umgewandelt werden.");
		}
	}

	private final String _name;

	private final byte _code;


	private KindOfVersion(final String name, final byte code) {
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
		return "KindOfVersion: " + getName() + " Code: " + getCode();
	}
}
