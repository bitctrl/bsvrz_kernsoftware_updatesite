/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
 * Konfigurationsausnahmen signalisieren Fehler bei Konfigurations�nderungen. Sie k�nnen von allen Methoden, die �nderungen an der Konfiguration durchf�hren,
 * erzeugt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5052 $
 * @deprecated Exceptions dieser Klasse werden von der Konfiguration nicht mehr generiert. Statt dessen wird an bestimmten Stellen die
 *             {@link de.bsvrz.dav.daf.main.config.ConfigurationChangeException} verwendet, um Fehler bei Konfigurations�nderungen zu signalisieren.
 */
@Deprecated
public class ConfigurationException extends RuntimeException {

	/** Erzeugt eine neue Konfigurationsausnahme ohne Detailmeldung. */
	public ConfigurationException() {
	}

	/**
	 * Erzeugt eine neue Konfigurationsausnahme mit der spezifizierten Detailmeldung.
	 *
	 * @param message Detailmeldung der neuen Konfigurationsausnahme.
	 */
	public ConfigurationException(String message) {
		super(message);
	}
}


