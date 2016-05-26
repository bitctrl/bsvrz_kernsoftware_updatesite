/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main;

/**
 * Standardimplementierung zur Behandlung von Fehlern der Kommunikationsverbindung. Wenn ein Objekt dieser Klasse an die Methode {@link
 * ClientDavInterface#setCloseHandler} übergeben wird, dann führen Verbindungsfehler zur Terminierung der Applikation.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SystemTerminator implements ApplicationCloseActionHandler {

	/** Diese Variable verhindert, das <code>close</code> zweimal aufgerufen wird. */
	private boolean _terminated = false;

	/** Erzeugt ein neues Objekt dieser Klasse. */
	public SystemTerminator() {
	}

	/**
	 * Fehlerbehandlungsmethode, die zur Terminierung der Applikation führt.
	 *
	 * @param error Textliche Beschreibung des aufgetreten Fehlers.
	 */
	public final void close(String error) {
		if(System.out != null) {
			System.out.flush();
		}
		if(!_terminated) {
			// Es soll nicht noch einmal System.exit aufgerufen werden können.
			_terminated = true;
			System.exit(1);
		}
	}
}
