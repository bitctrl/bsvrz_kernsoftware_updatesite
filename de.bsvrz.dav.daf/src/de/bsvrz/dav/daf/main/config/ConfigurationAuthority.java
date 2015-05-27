/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsverantwortlichen.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 13141 $ / $Date: 2015-02-04 10:06:20 +0100 (Wed, 04 Feb 2015) $ / ($Author: jh $)
 * @see ConfigurationObject
 */
public interface ConfigurationAuthority extends ConfigurationObject {

	/**
	 * Liefert die eindeutige Kodierung des Konfigurationsverantwortlichen.
	 *
	 * @return Kodierung des Konfigurationsverantwortlichen
	 */
	public short getCoding();

	/**
	 * Gibt den Default-Bereich des Konfigurationsverantwortlichen zurück oder null falls kein Bereich doer kein gültiger Bereich definiert
	 * ist.
	 * @return Default-Bereich doer null
	 */
	ConfigurationArea getDefaultConfigurationArea();
}

