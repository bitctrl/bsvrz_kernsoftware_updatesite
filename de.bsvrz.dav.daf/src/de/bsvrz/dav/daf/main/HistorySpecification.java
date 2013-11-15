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
 * Diese Klasse dient zur Angabe von Parametern bei der Abfrage von historischen Daten mit der Methode {@link ClientDavInterface#getCachedData}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class HistorySpecification {

	/** Typ der Anfrage */
	private byte _type;

	/** Anzahl zu lesender Datens�tze. */
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
	 * Bestimmt, ob diese Spezifikation �ber die Anzahl gew�nschter Datens�tze definiert wurde.
	 *
	 * @return <code>true</code> falls diese Spezifikation �ber die Anzahl definiert wurde, sonst <code>false</code>.
	 */
	public final boolean isCountSpecification() {
		return _type == 0;
	}

	/**
	 * Bestimmt, ob diese Spezifikation �ber einen Zeitbereich definiert wurde.
	 *
	 * @return <code>true</code> falls diese Spezifikation �ber einen Zeitbereich definiert wurde, sonst <code>false</code>.
	 */
	public final boolean isTimeSpecification() {
		return _type == 1;
	}

	/**
	 * Liefert die spezifizierte Anzahl gew�nschter Datens�tze zur�ck.
	 *
	 * @return Anzahl gew�nschter Datens�tze.
	 */
	public final int getCount() {
		return _count;
	}

	/**
	 * Liefert den Anfang des spezifierten Zeitbereichs zur�ck.
	 *
	 * @return Anfang des spezifierten Zeitbereichs in Millisekunden seit 1970.
	 */
	public final long getFromTime() {
		return _fromTime;
	}

	/**
	 * Liefert das Ende des spezifierten Zeitbereichs zur�ck.
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
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der letzten historischen Datens�tze mit Angabe der Anzahl zu lesender Datens�tze.
	 *
	 * @param count Anzahl zu lesender Datens�tze.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten.
	 */
	public static HistorySpecification last(int count) {
		return new HistorySpecification(count);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage des zu einem bestimmten Zeitpunkt g�ltigen Datensatzes.
	 *
	 * @param time Zeitpunkt, an dem der gew�nschte Datensatz g�ltig war, in Millisekunden seit 1970.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification atTime(long time) {
		return new HistorySpecification(time, time);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der in einem bestimmten Zeitbereich g�ltigen Datens�tze.
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
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der Datens�tze in einem Zeitbereich beginnend bei einem angegebenen Zeitpunkt bis zum aktuellen Zeitpunkt.
	 *
	 * @param time Anfangszeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Parameterobjekt zur Abfrage von historischen Daten
	 */
	public static HistorySpecification sinceTime(long time) {
		return new HistorySpecification(time, -1);
	}

	/**
	 * Erzeugt ein neues Parameterobjekt zur Abfrage der Datens�tze in einem Zeitbereich, der �ber eine spezifizierte Zeitdauer in der unmittelbaren Vergangenheit
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
