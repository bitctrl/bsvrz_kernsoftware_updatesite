/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
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
 * Verbindungsausnahme, die generiert wird, wenn eine Verbindung nicht aufgebaut werden konnte.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class ConnectionException extends Exception {

	/**
	 * Erzeugt ein neue Verbindungsausnahme mit der angegebenen Fehlermeldung.
	 *
	 * @param message Fehlermeldung
	 */
	public ConnectionException(String message) {
		super(message);
	}
}
