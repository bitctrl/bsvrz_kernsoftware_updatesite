/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.timeout.
 * 
 * de.bsvrz.sys.funclib.timeout is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.timeout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.timeout; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.sys.funclib.timeout;

import java.util.Date;

/**
 * Ein Objekt dieser Klasse kann mit einer Uhrzeit oder einer Zeitspanne initialisert werden. Das Objekt stellt eine
 * Methode {@link TimeoutTimer#isTimeExpired()} zur Verfügung, mit der Angefragt werden kann, ob die Uhrzeit erreicht
 * wurde oder ob die Zeitspanne abgelaufen ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TimeoutTimer {
	/**
	 * Zeitpunkt, an dem das Objekt erzeugt wurde
	 */
	private final long _startTime;

	/**
	 * Zeitpunkt, an dem das Objekt bei einem Methodenaufruf melden muss, das die vorgegebene Zeit verstrichen ist
	 */
	private final long _endTime;

	/**
	 * Ein Objekt, das mit einer Zeitspanne initialisiert wird. Ist die geforderte Zeitspanne abgelaufen, wird der
	 * Methodenaufruf {@link TimeoutTimer#isTimeExpired()} <code>false</code> zurückliefern. Die Zeit wird ab dem Zeitpunkt
	 * gemessen, ab dem der Construktor aufgerufen wird.
	 *
	 * @param time Zeitspanne, die ablaufen muss, bis {@link TimeoutTimer#isTimeExpired()} den Rückgabewert
	 *             <code>false</code> liefert
	 */
	public TimeoutTimer(long time) {

		_startTime = System.currentTimeMillis();
		// Es wurde eine Zeitspanne eingegeben, diese muss nun in eine Zeitpunkt umgerechnet werden
		_endTime = _startTime + time;
	}

	/**
	 * Ein Objekt, das mit einem Zeitpunkt initialisiert wird. Wird der geforderte Zeitpunkt überschritten, wird der
	 * Methodenaufruf {@link TimeoutTimer#isTimeExpired()} <code>false</code> zurückliefern. Die Zeit wird ab dem Zeitpunkt
	 * gemessen, ab dem der Construktor aufgerufen wird.
	 *
	 * @param date Zeitpunkt, der überschritten werden muss, damit die Methode {@link TimeoutTimer#isTimeExpired()}
	 *             <code>false</code> zurückliefert
	 */
	public TimeoutTimer(Date date) {
		_startTime = System.currentTimeMillis();
		_endTime = date.getTime();
	}

	/**
	 * Diese Methode wird aufgerufen, wenn geprüft werden soll ob die festgelegte Zeitspanne abgelaufen ist oder der
	 * festgelegte Zeitpunkt erreicht wurde.
	 *
	 * @return true = die Zeit ist abgelaufen; false = die/der Zeitspanne/Zeitpunkt wurde noch nicht erreicht
	 */
	public boolean isTimeExpired() {
		return System.currentTimeMillis() >= _endTime;
	}

	/**
	 * Diese Methode liefert die verbleibende Zeit, bis die vorgegebene Zeitspanne/Zeitpunkt abgelaufen ist.
	 *
	 * @return Zeit, die noch verbleibt, bis der vorgegebene Wert erreicht wird (in ms). Ist die Zeit abgelaufen, wird
	 *         immer <code>0</code> zurückgegeben.
	 */
	public long getRemainingTime() {
		final long remainingTime = _endTime - System.currentTimeMillis();
		if (remainingTime < 0) {
			// Die Zeit ist abgelaufen, somit wird 0 zurückgegeben
			return 0;
		} else {
			return remainingTime;
		}
	}

	public String toString() {
		return "Startzeitpunkt: " + new Date(_startTime).toString() + " Endzeitpunkt: " + new Date(_endTime).toString() + " Zeitspanne bis zum Endzeitpunkt(ms): " + (_endTime - System.currentTimeMillis()) + " Zeit abgelaufen: " + isTimeExpired();
	}
}
