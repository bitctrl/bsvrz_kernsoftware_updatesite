/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Diese Klasse dient dazu Schlüssel-Wert-Paare zu speichern. Jedes Objekt repräsentiert ein solches Paar. Schlüssel als auch Wert werden als Zeichenkette
 * (String) abgelegt. Mittels Getter- und Setter-Methoden kann auf den Schlüssel/Wert zugegriffen werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5001 $
 */
public class KeyValueObject {

	/** speichert den Schlüssel */
	private String _key;

	/** speichert den Wert */
	private String _value;


	/** Standardkonstruktor. Ein Objekt dieser Klasse mit leerem Schlüssel und Wert wird erzeugt. */
	public KeyValueObject() {
		this("", "");
	}

	/**
	 * Mit diesem Konstruktor wird ein Schlüssel-Wert-Paar erzeugt.
	 *
	 * @param key   der Schlüssel
	 * @param value der Wert
	 */
	public KeyValueObject(String key, String value) {
		_key = key;
		_value = value;
	}

	/**
	 * Gibt den Schlüssel zurück.
	 *
	 * @return der Schlüssel
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Setzt den Schlüssel auf den übergebenen Parameter.
	 *
	 * @param key der Schlüssel
	 */
	public void setKey(String key) {
		_key = key;
	}

	/**
	 * Gibt den Wert zurück.
	 *
	 * @return der Wert
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * Setzt den Wert auf den übergebenen Parameter.
	 *
	 * @param value der Wert
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * Gibt eine String-Repräsentation dieses Objekts zurück.
	 *
	 * @return gibt die String-Repräsentation dieses Objekts zurück
	 */
	public String toString() {
		return "KeyValueObject{" + "_key='" + _key + "'" + ", _value='" + _value + "'" + "}";
	}
}
