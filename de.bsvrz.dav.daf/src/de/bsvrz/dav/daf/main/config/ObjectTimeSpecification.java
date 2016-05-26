/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Diese Klasse stellt Methoden zur Verfügung, um bei Konfigurationsanfragen, den Gültigkeitsbereich der Objekte
 * einzuschränken.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ObjectTimeSpecification {
	/**
	 * Objekt, welches für alle Spezifikationen gilt, die aktuelle Objekte ermitteln wollen.
	 */
	private static ObjectTimeSpecification _default;

	/**
	 * Typ der Spezifikation wird hier im Objekt gespeichert.
	 */
	private TimeSpecificationType _type;

	/**
	 * Startzeitpunkt des Gültigkeitszeitraumes.
	 */
	private long _startTime;

	/**
	 * Endzeitpunkt des Gültigkeitszeitraumes.
	 */
	private long _endTime;

	/**
	 * Privater Konstruktor, damit keine undefinierten Objekte angelegt werden können.
	 */
	private ObjectTimeSpecification() {
	}

	/**
	 * Konstruktor für das Spezifikationsobjekt, welches nur einen Zeitpunkt enthält.
	 *
	 * @param type Typ der Gültigkeit
	 */
	private ObjectTimeSpecification(TimeSpecificationType type) {
		_type = type;
	}

	/**
	 * Konstruktor für das Spezifikationsobjekt, welches einen Zeitbereich enthält.
	 *
	 * @param type	  Typ der Gültigkeit
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 */
	private ObjectTimeSpecification(TimeSpecificationType type, long startTime, long endTime) {
		_type = type;
		_startTime = startTime;
		_endTime = endTime;
	}

	/**
	 * Für Objekte, die aktuell gültig sind.
	 *
	 * @return Spezifikationsobjekt des Gültigkeitszeitraumes
	 */
	public static ObjectTimeSpecification valid() {
		if (_default == null) _default = new ObjectTimeSpecification(TimeSpecificationType.VALID);
		return _default;
	}

	/**
	 * Für Objekte, die zu einem bestimmten Zeitpunkt gültig sind.
	 *
	 * @param time der Zeitpunkt, zu dem die Objekte gültig waren
	 * @return Spezifikationsobjekt des Gültigkeitszeitraumes
	 */
	public static ObjectTimeSpecification valid(long time) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_AT_TIME, time, time);
	}

	/**
	 * Für Objekte, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs gültig waren.
	 *
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 * @return Spezifikationsobjekt des Gültigkeitszeitraumes
	 */
	public static ObjectTimeSpecification validInPeriod(long startTime, long endTime) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_IN_PERIOD, startTime, endTime);
	}

	/**
	 * Für Objekte, die während des gesamten Zeitraumes gültig waren.
	 *
	 * @param startTime Beginn des Zeitraumes
	 * @param endTime   Ende des Zeitraumes
	 * @return Spezifikationsobjekt des Gültigkeitszeitraumes
	 */
	public static ObjectTimeSpecification validDuringPeriod(long startTime, long endTime) {
		return new ObjectTimeSpecification(TimeSpecificationType.VALID_DURING_PERIOD, startTime, endTime);
	}

	/**
	 * Gibt den Typ der Gültigkeits-Spezifikation zurück.
	 *
	 * @return Typ der Gültigkeits-Spezifikation
	 */
	public TimeSpecificationType getType() {
		return _type;
	}

	/**
	 * Gibt den Beginn des Zeitbereichs der Gültigkeit zurück. Gilt nur für die Gültigkeits-Spezifikationen, die einen
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
	 * Gibt das Ende des Zeitbereichs der Gültigkeit zurück. Gilt nur für die Gültigkeits-Spezifikationen, die einen
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
	 * Gibt den angegebenen Zeitpunkt der Gültigkeit zurück. Gilt nur für die Gültigkeits-Spezifikation bei der nur ein
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
