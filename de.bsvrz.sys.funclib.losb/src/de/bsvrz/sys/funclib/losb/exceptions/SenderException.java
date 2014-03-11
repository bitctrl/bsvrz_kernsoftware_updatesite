/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.exceptions;

/**
 * Kommt es beim Senden von Daten zu einem Fehler, dann wird diese Exception geworfen.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class SenderException extends LoggerException {

	private static final long serialVersionUID = -5307407229319345647L;

	/**
	 * Erzeugen der Exception.
	 *
	 * @param message  Nachricht.
	 * @param logLevel Log-Level
	 */
	public SenderException(String message, int logLevel) {
		super(message, logLevel);
	}
}
