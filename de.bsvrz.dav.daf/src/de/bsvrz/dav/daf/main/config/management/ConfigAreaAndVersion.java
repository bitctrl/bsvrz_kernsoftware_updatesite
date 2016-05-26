/*
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config.management;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;

/**
 * Diese Klasse speichert einen Konfigurationsbreich und die Version mit der der Bereich aktiviert/freigegeben/geprüft/usw. werden soll.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAreaAndVersion {

	private final ConfigurationArea _configArea;

	private final short _version;


	/**
	 * @param configArea Pid des Konfigurationsbereichs, auf den sich die Version bezieht
	 * @param version    Version, mit der der Konfigurationsbereich aktiviert/freigegeben/geprüft, usw. werden soll. Version 0 nimmt einer Sonderrolle ein und kann
	 *                   vom jeweiligen Nutzer anders interpretiert werden.
	 */
	public ConfigAreaAndVersion(ConfigurationArea configArea, short version) {
		if(configArea != null) {
			_configArea = configArea;
			_version = version;
		}
		else {
			throw new IllegalArgumentException("Der angegebene Konfigurationsbereich ist \"null\".");
		}
	}

	/**
	 * Erzeugt ein Objekt, die Version des Bereichs wird auf 0 gesetzt.
	 *
	 * @param configArea Pid des Konfigurationsbereichs, auf den sich die Version bezieht
	 */
	public ConfigAreaAndVersion(ConfigurationArea configArea) {
		if(configArea == null){
			throw new IllegalArgumentException("Es wurde kein gültiger Bereich sondern null übergeben.");
		}
		_configArea = configArea;
		_version = 0;
	}

	/**
	 * Konfigurationsbereich
	 *
	 * @return s.o.
	 */
	public ConfigurationArea getConfigArea() {
		return _configArea;
	}

	/**
	 * Version, mit der der Konfigurationsbereich aktiviert/freigegeben/geprüft werden soll
	 *
	 * @return Version des Bereichs. Der Wert 0 ist als "nimm die richtige" Version zu interpretieren. Der Benutzer hat explizit keine Version angegeben.
	 */
	public short getVersion() {
		return _version;
	}

	public String toString() {
		return "Konfigurationsbereich Pid " + _configArea.getPid() + " Version " + _version;
	}
}
