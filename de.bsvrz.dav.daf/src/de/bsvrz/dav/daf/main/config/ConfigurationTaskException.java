/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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
 * Diese Exception signalisiert das die Konfiguration eine ihr gestellte Aufgabe nicht bearbeiten konnte. Dies kann
 * geschehen, wenn aufgabenspezifische Randbediengungen nicht erfüllt sind.
 *
 * Wenn die Aufgabe modifiziert wird oder die benötigten Randbediengungen hergestellt sind, wird die Konfiguration den
 * Auftrag bearbeiten können, wenn dieser erneut an die Konfiguration geschickt wird.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public class ConfigurationTaskException extends Exception {
	/**
	 * Erzeugt eine neue Konfigurationsausnahme ohne Detailmeldung.
	 */
	public ConfigurationTaskException() {
	}

	/**
	 * Erzeugt eine neue Konfigurationsausnahme mit der spezifizierten Detailmeldung.
	 *
	 * @param message Detailmeldung der neuen Konfigurationsausnahme.
	 */
	public ConfigurationTaskException(String message) {
		super(message);
	}

	public ConfigurationTaskException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationTaskException(Throwable cause) {
		super(cause);
	}
}
