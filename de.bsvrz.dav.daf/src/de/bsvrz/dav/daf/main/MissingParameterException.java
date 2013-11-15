/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main;

/**
 * Ausnahme, die generiert wird, wenn notwendige Verbindungsparameter nicht spezifiziert wurden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 7694 $
 * @see ClientDavParameters
 */
public class MissingParameterException extends Exception {

	/**
	 * Erzeugt eine neue Ausnahme dieser Klasse.
	 *
	 * @param message Fehlerbeschreibung.
	 */
	public MissingParameterException(String message) {
		super(message);
	}

	/**
	 * Erzeugt eine neue Ausnahme mit der angegebenen Beschreibung und urspr�nglichen Ausnahme
	 * @param message Beschreibung der Ausnahme
	 * @param cause Urspr�ngliche Ausnahme
	 */
	public MissingParameterException(String message, Throwable cause) {
		super(message, cause);
	}
}


