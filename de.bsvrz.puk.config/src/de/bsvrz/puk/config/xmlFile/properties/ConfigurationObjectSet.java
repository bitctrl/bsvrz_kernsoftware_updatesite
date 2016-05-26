/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Diese Klasse stellt eine "objektMenge" nach K2S.DTD dar.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationObjectSet implements ConfigurationObjectElements{
	private final String _name;
	/**
	 * Alle Elemente des Objects
	 */
	private final String _elements[];

	/** Pid der Konfiguration, die die Elementzugehörigkeit der Menge verwaltet*/
	private final String _managementPid;

	public ConfigurationObjectSet(String name, String[] elements, final String managementPid) {
		_managementPid = managementPid;
		if (name != null) {
			_name = name;
		} else {
			_name = "";
		}

		if (elements != null) {
			_elements = elements;
		} else {
			_elements = new String[0];
		}
	}

	/**
	 * Name
	 * @return Name oder "" falls im Konstruktor <code>null</code> übergeben wurde.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Elemente des Objekts
	 * @return Array mit Elementen oder ein leeres Array, falls keine Objekte vorhanden sind
	 */
	public String[] getElements() {
		return _elements;
	}

	/**
	 * Liefert die Pid der Konfiguration, die die Elementzugehörigkeit der Menge verwaltet.
	 * @return Pid der Konfiguration, die die Elementzugehörigkeit der Menge verwaltet
	 */
	public String getManagementPid() {
		return _managementPid;
	}
}
