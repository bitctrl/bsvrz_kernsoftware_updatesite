/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Konfigurationsausnahmen signalisieren Fehler bei Konfigurationsänderungen. Sie können von allen Methoden, die Änderungen an der Konfiguration durchführen,
 * erzeugt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @deprecated Exceptions dieser Klasse werden von der Konfiguration nicht mehr generiert. Statt dessen wird an bestimmten Stellen die
 *             {@link de.bsvrz.dav.daf.main.config.ConfigurationChangeException} verwendet, um Fehler bei Konfigurationsänderungen zu signalisieren.
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


