/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.plugins.api.settings;

/**
 * Diese Klasse dient dazu Schl�ssel-Wert-Paare zu speichern. Jedes Objekt repr�sentiert ein solches Paar. Schl�ssel als auch Wert werden als Zeichenkette
 * (String) abgelegt. Mittels Getter- und Setter-Methoden kann auf den Schl�ssel/Wert zugegriffen werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5001 $
 */
public class KeyValueObject {

	/** speichert den Schl�ssel */
	private String _key;

	/** speichert den Wert */
	private String _value;


	/** Standardkonstruktor. Ein Objekt dieser Klasse mit leerem Schl�ssel und Wert wird erzeugt. */
	public KeyValueObject() {
		this("", "");
	}

	/**
	 * Mit diesem Konstruktor wird ein Schl�ssel-Wert-Paar erzeugt.
	 *
	 * @param key   der Schl�ssel
	 * @param value der Wert
	 */
	public KeyValueObject(String key, String value) {
		_key = key;
		_value = value;
	}

	/**
	 * Gibt den Schl�ssel zur�ck.
	 *
	 * @return der Schl�ssel
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Setzt den Schl�ssel auf den �bergebenen Parameter.
	 *
	 * @param key der Schl�ssel
	 */
	public void setKey(String key) {
		_key = key;
	}

	/**
	 * Gibt den Wert zur�ck.
	 *
	 * @return der Wert
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * Setzt den Wert auf den �bergebenen Parameter.
	 *
	 * @param value der Wert
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * Gibt eine String-Repr�sentation dieses Objekts zur�ck.
	 *
	 * @return gibt die String-Repr�sentation dieses Objekts zur�ck
	 */
	public String toString() {
		return "KeyValueObject{" + "_key='" + _key + "'" + ", _value='" + _value + "'" + "}";
	}
}
