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
 * Aufzählung für die verschiedenen Persistenzmodi der dynamischen Objekte eines Typs
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public enum PersistenceMode {
	/**
	 * Dynamische Objekte werden nicht persistent gespeichert.
	 */
	TRANSIENT_OBJECTS,

	/**
	 * Dynamische Objekte werden persistent gespeichert.
	 */
	PERSISTENT_OBJECTS,

	/**
	 * Dynamische Objekte werden persistent gespeichert und beim Neustart auf ungültig gesetzt.
	 */
	PERSISTENT_AND_INVALID_ON_RESTART,

	/**
	 * Es wurde nicht festgelegt, wie mit dynamischen Objekten verfahren werden soll
	 */
	UNDEFINED
}
