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

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Exception die den Logger unters�tzt.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class LoggerException extends Exception {

	private static final long serialVersionUID = 5728439329058687450L;

	/** Log-Level Error */
	public static final int ERROR = 0;

	/** Log-Level Warning */
	public static final int WARNING = 1;

	/** Weder Error noch Warning Log-Level */
	public static final int OTHER = 2;

	/** Logger */
	private static final Debug debug = Debug.getLogger();

	/** Eingestelltes Log-Level */
	private int logLevel;


	/**
	 * @param message  Fehlermeldung
	 * @param logLevel Log-Level, mit dem der Fehler geloggt wird.
	 */
	public LoggerException(String message, int logLevel) {
		super(message);
		this.logLevel = logLevel;
	}

	/**
	 * @param message  Fehlermeldung
	 * @param cause    Ursache
	 * @param logLevel Log-Level, mit dem der Fehler geloggt wird.
	 */
	public LoggerException(String message, Throwable cause, int logLevel) {
		super(message, cause);
		this.logLevel = logLevel;
	}

	/**
	 * @param cause    Fehlerursache
	 * @param logLevel Log-Level, mit dem der Fehler geloggt wird.
	 */
	public LoggerException(Throwable cause, int logLevel) {
		super(cause);
		this.logLevel = logLevel;
	}

	/** @param loggerException Fehlermeldung */
	public LoggerException(LoggerException loggerException) {
		super(loggerException);
		this.logLevel = loggerException.logLevel;
	}

	/** Loggt die Exception gem�� dem Log-Level. */
	public void log() {
		log(this.getMessage());
	}

	/**
	 * Gibt die Nachricht String mit dem gesetzten Log-Level aus.
	 *
	 * @param msg Nachricht
	 */
	public void log(String msg) {
		switch(logLevel) {
			case ERROR:
				debug.error(msg, this);
				break;
			case WARNING:
				debug.warning(msg);
				break;
			default:
				debug.fine(msg);
				break;
		}
	}

	/** @return Eingestelltes Log-Level */
	public int getLogLevel() {
		return logLevel;
	}

	/**
	 * Loggt eine Nachricht.
	 *
	 * @param msg      Nachricht
	 * @param logLevel Log-Level.
	 */
	public static void log(String msg, int logLevel) {
		switch(logLevel) {
			case ERROR:
				debug.error(msg);
				break;
			case WARNING:
				debug.warning(msg);
				break;
			default:
				debug.fine(msg);
				break;
		}
	}
}
