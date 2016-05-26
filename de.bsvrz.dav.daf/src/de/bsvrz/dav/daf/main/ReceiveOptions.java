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
 * Verwaltung von Anmeldeoptionen bei Empfangsanmeldungen. Die Klasse verwaltet, ob eine Anmeldung sich <ul> <li>auf geänderte und nicht geänderte Datensätze
 * (Normal)</li> <li>nur auf geänderte Datensätze (Delta)</li> <li>auch auf nachgelieferte Datensätze (Nachgeliefert)</li> </ul> bezieht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ClientDavInterface#subscribeReceiver
 */
public final class ReceiveOptions {

	/** Anmeldung bezieht sich auf geänderte und nicht geänderte Datensätze. */
	private static final ReceiveOptions NORMAL = new ReceiveOptions(false, false);

	/** Anmeldung bezieht sich nur auf geänderte Datensätze. */
	private static final ReceiveOptions DELTA = new ReceiveOptions(true, false);

	/** Anmeldung bezieht sich auf online aktuelle Datensätze und auf nachgelieferte Datensätze. */
	private static final ReceiveOptions DELAYED = new ReceiveOptions(false, true);

	/** Speichert, ob sich die Anmeldung nur auf geänderte Datensätze bezieht. */
	private final boolean onlyDelta;

	/** Speichert, ob sich die Anmeldung auch auf nachgelieferte Datensätze bezieht. */
	private final boolean withDelayedData;

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit den spezifizierten Werten für die Optionen "Delta", "Nachgeliefert".
	 *
	 * @param onlyDelta       <code>true</code>, wenn die Anmeldung sich nur auf geänderte Datensätze beziehen soll.
	 * @param withDelayedData <code>true</code>, wenn die Anmeldung sich auch auf nachgelieferte Datensätze beziehen soll.
	 */
	public ReceiveOptions(boolean onlyDelta, boolean withDelayedData) {
		if(onlyDelta && withDelayedData) {
			throw new IllegalArgumentException("Delta Anmeldung mit nachgelieferten Datensätzen nicht erlaubt");
		}
		this.onlyDelta = onlyDelta;
		this.withDelayedData = withDelayedData;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit dem spezifizierten Wert für die Option "Delta". Es werden keine "Nachgelieferten" Daten angemeldet.
	 *
	 * @param onlyDelta <code>true</code>, wenn die Anmeldung sich nur auf geänderte Datensätze beziehen soll.
	 */
	public ReceiveOptions(boolean onlyDelta) {
		this(onlyDelta, false);
	}

	/**
	 * Vergleicht dieses Objekt mit dem angegebenen Objekt auf logische Gleichheit.
	 *
	 * @param other Das Objekt mit dem dieses Objekt verglichen werden soll.
	 *
	 * @return <code>true</code>, wenn dieses Objekt gleich dem angegebenen Objekt ist, sonst <code>false</code>.
	 */
	public boolean equals(Object other) {
		if(other == this) return true;
		if(!(other instanceof ReceiveOptions)) return false;
		ReceiveOptions o = (ReceiveOptions)other;
		return (onlyDelta == o.onlyDelta) && (withDelayedData == o.withDelayedData);
	}

	/**
	 * Bestimmt den Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts.
	 */
	public int hashCode() {
		return (onlyDelta ? 1 : 0) + (withDelayedData ? 2 : 0);
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		if(this.equals(NORMAL)) return "Normal";
		if(this.equals(DELTA)) return "Delta";
		if(this.equals(DELAYED)) return "Nachgeliefert";
		throw new IllegalStateException("Delta Anmeldung mit nachgelieferten Datensätzen nicht erlaubt");
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten für die Optionen. Die Anmeldung erfolgt mit "Delta", d.h eine Anmeldung bezieht sich nur auf
	 * geänderte Daten. Es werden keine "Nachgelieferten" Daten angemeldet.
	 *
	 * @return Anmeldeoptionen mit aktivierter Delta-Option
	 */
	public static ReceiveOptions delta() {
		return DELTA;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten für die Optionen. Die Anmeldung erfolgt ohne "Delta", d.h eine Anmeldung bezieht sich nicht nur auf
	 * geänderte Daten, sondern auch auf unveränderte Datensätze. Die Anmeldung bezieht sich auch auf "Nachgelieferte" Daten.
	 *
	 * @return Anmeldeoptionen mit aktivierter Nachgeliefert-Option
	 */
	public static ReceiveOptions delayed() {
		return DELAYED;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten für die Optionen. Die Anmeldung erfolgt ohne "Delta", d.h eine Anmeldung bezieht sich nicht nur auf
	 * geänderte Daten, sondern auch auf unveränderte Datensätze. Es werden keine "Nachgelieferten" Daten angemeldet..
	 *
	 * @return Anmeldeoptionen mit deaktivierter Delta-Option und deaktivierter Nachgeliefert-Option
	 */
	public static ReceiveOptions normal() {
		return NORMAL;
	}

	/**
	 * Bestimmt, ob sich eine Anmeldung mit diesen Optionen "mit Delta" erfolgt, d.h. sich nur auf geänderte Datensätze bezieht.
	 *
	 * @return <code>true</code>, wenn die Anmeldung sich nur auf geänderte Datensätze bezieht; sonst <code>false</code>.
	 */
	public final boolean withDelta() {
		return onlyDelta;
	}

	/**
	 * Bestimmt, ob sich eine Anmeldung mit diesen Optionen auch auf nachgelieferte Datensätze bezieht.
	 *
	 * @return <code>true</code>, wenn die Anmeldung sich auch auf nachgelieferte Datensätze bezieht; sonst <code>false</code>.
	 */
	public final boolean withDelayed() {
		return withDelayedData;
	}
}
