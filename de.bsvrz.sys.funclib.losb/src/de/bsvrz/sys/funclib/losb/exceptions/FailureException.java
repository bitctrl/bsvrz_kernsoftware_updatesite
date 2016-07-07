/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.exceptions;


/**
 * Kommt es während der Abarbeitung einer Anfrage zu einem Fehler, der den Abbruch der Abarbeitung zur Folge hat, wird diese Exception geworfen.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class FailureException extends LoggerException {

	private static final long serialVersionUID = -7683253378348116061L;

	/**
	 * @param message  Fehlermeldung.
	 * @param logLevel Log-Level
	 */
	public FailureException(String message, int logLevel) {
		super(message, logLevel);
	}

	/**
	 * Übernimmt die Fehlermeldung der übergebenen Exception
	 *
	 * @param cause    Grund.
	 * @param logLevel Log-Level
	 */
	public FailureException(Throwable cause, int logLevel) {
		super(cause, logLevel);
	}

	/**
	 * @param message  Fehlermeldung.
	 * @param cause    Grund.
	 * @param logLevel Log-Level
	 */
	public FailureException(String message, Throwable cause, int logLevel) {
		super(message, cause, logLevel);
	}

	/** @param loggerException Fehlermeldung */
	public FailureException(LoggerException loggerException) {
		super(loggerException);
	}
}
