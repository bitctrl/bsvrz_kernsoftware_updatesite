/*
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
 * Konfigurationsausnahmen signalisieren Fehler bei Konfigurationsänderungen. Sie können von allen Methoden,
 * die Änderungen an der Konfiguration durchführen, erzeugt werden.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public class ConfigurationChangeException extends ConfigurationTaskException {
	/**
	 * Erzeugt eine neue Konfigurationsausnahme ohne Detailmeldung.
	 */
	public ConfigurationChangeException() {
	}

	/**
	 * Erzeugt eine neue Konfigurationsausnahme mit der spezifizierten Detailmeldung.
	 *
	 * @param message Detailmeldung der neuen Konfigurationsausnahme.
	 */
	public ConfigurationChangeException(String message) {
		super(message);
	}

	public ConfigurationChangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationChangeException(Throwable cause) {
		super(cause);
	}
}
