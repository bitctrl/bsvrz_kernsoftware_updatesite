/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

/**
 * Diese Klasse erweitert die Klasse LongAttribute um einen zusätzlichen String, der im Konstruktor angegeben werden kann und mit Hilfe eines Getters abgefragt
 * werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class LongAndStringAttribute extends LongAttribute {

	/** String, der im Konstruktor angegeben werden kann und mit Hilfe eines Getters abgefragt werden kann. */
	final String string;

	/**
	 * Legt ein neues Objekt mit den angegebenen Werten an.
	 * @param l Zahl die gespeichert werden soll.
	 * @param string String der gespeichert werden soll.
	 */
	public LongAndStringAttribute(long l, final String string) {
		super(l);
		this.string = string;
	}

	/**
	 * Liefert den String, der im Konstruktor übergeben wurde.
	 * @return String, der im Konstruktor übergeben wurde.
	 */
	public String getString() {
		return string;
	}

	@Override
	public DataValue cloneObject() {
		return new LongAndStringAttribute(getPlainValue(), string);
	}
}
