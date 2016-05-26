/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
 * Diese Klasse dient zur Angabe von Parametern bei der Abfrage von historischen Daten mit der Methode {@link ClientDavInterface#getCachedData}.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class HistorySpecification {

	/** Typ der Anfrage */
	private byte _type;

	/** Anzahl zu lesender Datensätze. */
	private int _count;

	/** Startszeit des Zeitintervalls */
	private long _fromTime;

	/** Endzeit des Zeitintervalls */
	private long _toTime;

	private HistorySpecification(int count) {
		_type = 0;
		_count = count;
	}

	private HistorySpecification(long fromTime, long toTime) {
		_type = 1;
		_fromTime = fromTime;
		_toTime = toTime;
	}

	/**
	 * Bestimmt, ob diese Spezifikation über die Anzahl gewünschter Datensätze definiert wurde.
	 *
	 * @return <code>true</code> falls diese Spezifikation über die Anzahl definiert wurde, sonst <code>false</code>.
	 */
	public final boolean isCountSpecification() {
		return _type == 0;
	}

	/**
	 * Bestimmt, ob diese Spezifikation über einen Zeitbereich definiert wurde.
	 *
	 * @return <code>true</code> falls diese Spezifikation über einen Zeitbereich definiert wurde, sonst <code>false</code>.
	 */
	public final boolean isTimeSpecification() {
		return _type == 1;
	}

	/**
	 * Liefert die spezifizierte Anzahl gewünschter Datensätze zurück.
	 *
	 * @return Anzahl gewünschter Datensätze.
	 */
	public final int getCount() {
		return _count;
	}

	/**
	 * Liefert den Anfang des spezifierten Zeitbereichs zurück.
	 *
	 * @return Anfang des spezifierten Zeitbereichs in Millisekunden seit 1970.
	 */
	public final long getFromTime() {
		return _fromTime;
	}

	/**
	 * Liefert das Ende des spezifierten Zeitbereichs zurück.
	 *
	 * @return Ende des spezifierten Zeitbereichs in Millisekunden seit 1970.
	 */
	public final long getToTime() {
		return _toTime;
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage des aktuellen Datensatzes.
	 *
	 * @return Parameterobjekt zur Abfrage des aktuellen Datensatzes.
	 */
	public static HistorySpecification actual() {
		return new HistorySpecification(1);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der letzten historischen Datensätze mit Angabe der Anzahl zu lesender Datensätze.
	 *
	 * @param count Anzahl zu lesender Datensätze.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten.
	 */
	public static HistorySpecification last(int count) {
		return new HistorySpecification(count);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage des zu einem bestimmten Zeitpunkt gültigen Datensatzes.
	 *
	 * @param time Zeitpunkt, an dem der gewünschte Datensatz gültig war, in Millisekunden seit 1970.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification atTime(long time) {
		return new HistorySpecification(time, time);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der in einem bestimmten Zeitbereich gültigen Datensätze.
	 *
	 * @param fromTime Anfangszeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param toTime   Endezeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification inPeriod(long fromTime, long toTime) {
		return new HistorySpecification(fromTime, toTime);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der Datensätze in einem Zeitbereich beginnend bei einem angegebenen Zeitpunkt bis zum aktuellen Zeitpunkt.
	 *
	 * @param time Anfangszeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification sinceTime(long time) {
		return new HistorySpecification(time, -1);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der Datensätze in einem Zeitbereich, der über eine spezifizierte Zeitdauer in der unmittelbaren Vergangenheit
	 * bis zum aktuellen Zeitpunkt definiert ist.
	 *
	 * @param time Dauer des Zeitbereichs in Millisekunden.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification forTime(long time) {
		return new HistorySpecification(System.currentTimeMillis() - time, -1);
	}
}
