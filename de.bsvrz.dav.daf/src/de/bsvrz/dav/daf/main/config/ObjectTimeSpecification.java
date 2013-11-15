/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
 * Diese Klasse stellt Methoden zur Verf�gung, um bei Konfigurationsanfragen, den G�ltigkeitsbereich der Objekte
 * einzuschr�nken.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public class ObjectTimeSpecification {
	/**
	 * Objekt, welches f�r alle Spezifikationen gilt, die aktuelle Objekte ermitteln wollen.
	 */
	private static ObjectTimeSpecification _default;

	/**
	 * Typ der Spezifikation wird hier im Objekt gespeichert.
	 */
	private TimeSpecificationType _type;

	/**
	 * Startzeitpunkt des G�ltigkeitszeitraumes.
	 */
	private long _startTime;

	/**
	 * Endzeitpunkt des G�ltigkeitszeitraumes.
	 */
	private long _endTime;

	/**
	 * Privater Konstruktor, damit keine undefinierten Objekte angelegt werden k�nnen.
	 */
	private ObjectTimeSpecification() {
	}

	/**
	 * Konstruktor f�r das Spezifikationsobjekt, welches nur einen Zeitpunkt enth�lt.
	 *
	 * @param type Typ der G�ltigkeit
	 */
	private ObjectTimeSpecification(TimeSpecificationType type) {
		_type = type;
	}

	/**
	 * Konstruktor f�r das Spezifikationsobjekt, welches einen Zeitbereich enth�lt.
	 *
	 * @param type	  Typ der G�ltigkeit
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 */
	private ObjectTimeSpecification(TimeSpecificationType type, long startTime, long endTime) {
		_type = type;
		_startTime = startTime;
		_endTime = endTime;
	}

	/**
	 * F�r Objekte, die aktuell g�ltig sind.
	 *
	 * @return Spezifikationsobjekt des G�ltigkeitszeitraumes
	 */
	public static ObjectTimeSpecification valid() {
		if (_default == null) _default = new ObjectTimeSpecification(TimeSpecificationType.VALID);
		return _default;
	}

	/**
	 * F�r Objekte, die zu einem bestimmten Zeitpunkt g�ltig sind.
	 *
	 * @param time der Zeitpunkt, zu dem die Objekte g�ltig waren
	 * @return Spezifikationsobjekt des G�ltigkeitszeitraumes
	 */
	public static ObjectTimeSpecification valid(long time) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_AT_TIME, time, time);
	}

	/**
	 * F�r Objekte, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs g�ltig waren.
	 *
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 * @return Spezifikationsobjekt des G�ltigkeitszeitraumes
	 */
	public static ObjectTimeSpecification validInPeriod(long startTime, long endTime) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_IN_PERIOD, startTime, endTime);
	}

	/**
	 * F�r Objekte, die w�hrend des gesamten Zeitraumes g�ltig waren.
	 *
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 * @return Spezifikationsobjekt des G�ltigkeitszeitraumes
	 */
	public static ObjectTimeSpecification validDuringPeriod(long startTime, long endTime) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_DURING_PERIOD, startTime, endTime);
	}

	/**
	 * Gibt den Typ der G�ltigkeits-Spezifikation zur�ck.
	 *
	 * @return Typ der G�ltigkeits-Spezifikation
	 */
	public TimeSpecificationType getType() {
		return _type;
	}

	/**
	 * Gibt den Beginn des Zeitbereichs der G�ltigkeit zur�ck. Gilt nur f�r die G�ltigkeits-Spezifikationen, die einen
	 * Zeitbereich angegeben haben.
	 *
	 * @return Beginn des angegebenen Zeitbereichs
	 */
	public long getStartTime() {
		if (_type != TimeSpecificationType.VALID && _type != TimeSpecificationType.VALID_AT_TIME) {
			return _startTime;
		} else {
			throw new IllegalStateException("Eine Startzeit gibt es nur bei einem Objekt mit Zeitbereich.");
		}
	}

	/**
	 * Gibt das Ende des Zeitbereichs der G�ltigkeit zur�ck. Gilt nur f�r die G�ltigkeits-Spezifikationen, die einen
	 * Zeitbereich angegeben haben.
	 *
	 * @return Ende des angegebenen Zeitbereichs
	 */
	public long getEndTime() {
		if (_type != TimeSpecificationType.VALID && _type != TimeSpecificationType.VALID_AT_TIME) {
			return _endTime;
		} else {
			throw new IllegalStateException("Eine Endzeit gibt es nur bei einem Objekt mit Zeitbereich.");
		}
	}

	/**
	 * Gibt den angegebenen Zeitpunkt der G�ltigkeit zur�ck. Gilt nur f�r die G�ltigkeits-Spezifikation bei der nur ein
	 * Zeitpunkt angegeben wurde.
	 *
	 * @return der angegebene Zeitpunkt
	 */
	public long getTime() {
		if (_type == TimeSpecificationType.VALID_AT_TIME) {
			return _startTime;
		} else {
			throw new IllegalStateException("Es gibt keinen Zeitpunkt oder einen Start- und Endzeitpunkt.");
		}
	}
}
