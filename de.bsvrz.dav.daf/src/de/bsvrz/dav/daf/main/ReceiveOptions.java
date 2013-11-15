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
 * Verwaltung von Anmeldeoptionen bei Empfangsanmeldungen. Die Klasse verwaltet, ob eine Anmeldung sich <ul> <li>auf ge�nderte und nicht ge�nderte Datens�tze
 * (Normal)</li> <li>nur auf ge�nderte Datens�tze (Delta)</li> <li>auch auf nachgelieferte Datens�tze (Nachgeliefert)</li> </ul> bezieht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see ClientDavInterface#subscribeReceiver
 */
public final class ReceiveOptions {

	/** Anmeldung bezieht sich auf ge�nderte und nicht ge�nderte Datens�tze. */
	private static final ReceiveOptions NORMAL = new ReceiveOptions(false, false);

	/** Anmeldung bezieht sich nur auf ge�nderte Datens�tze. */
	private static final ReceiveOptions DELTA = new ReceiveOptions(true, false);

	/** Anmeldung bezieht sich auf online aktuelle Datens�tze und auf nachgelieferte Datens�tze. */
	private static final ReceiveOptions DELAYED = new ReceiveOptions(false, true);

	/** Speichert, ob sich die Anmeldung nur auf ge�nderte Datens�tze bezieht. */
	private final boolean onlyDelta;

	/** Speichert, ob sich die Anmeldung auch auf nachgelieferte Datens�tze bezieht. */
	private final boolean withDelayedData;

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit den spezifizierten Werten f�r die Optionen "Delta", "Nachgeliefert".
	 *
	 * @param onlyDelta       <code>true</code>, wenn die Anmeldung sich nur auf ge�nderte Datens�tze beziehen soll.
	 * @param withDelayedData <code>true</code>, wenn die Anmeldung sich auch auf nachgelieferte Datens�tze beziehen soll.
	 */
	public ReceiveOptions(boolean onlyDelta, boolean withDelayedData) {
		if(onlyDelta && withDelayedData) {
			throw new IllegalArgumentException("Delta Anmeldung mit nachgelieferten Datens�tzen nicht erlaubt");
		}
		this.onlyDelta = onlyDelta;
		this.withDelayedData = withDelayedData;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit dem spezifizierten Wert f�r die Option "Delta". Es werden keine "Nachgelieferten" Daten angemeldet.
	 *
	 * @param onlyDelta <code>true</code>, wenn die Anmeldung sich nur auf ge�nderte Datens�tze beziehen soll.
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
	 * Liefert eine textuelle Beschreibung dieses Objekts zur�ck.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		if(this.equals(NORMAL)) return "Normal";
		if(this.equals(DELTA)) return "Delta";
		if(this.equals(DELAYED)) return "Nachgeliefert";
		throw new IllegalStateException("Delta Anmeldung mit nachgelieferten Datens�tzen nicht erlaubt");
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten f�r die Optionen. Die Anmeldung erfolgt mit "Delta", d.h eine Anmeldung bezieht sich nur auf
	 * ge�nderte Daten. Es werden keine "Nachgelieferten" Daten angemeldet.
	 *
	 * @return Anmeldeoptionen mit aktivierter Delta-Option
	 */
	public static ReceiveOptions delta() {
		return DELTA;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten f�r die Optionen. Die Anmeldung erfolgt ohne "Delta", d.h eine Anmeldung bezieht sich nicht nur auf
	 * ge�nderte Daten, sondern auch auf unver�nderte Datens�tze. Die Anmeldung bezieht sich auch auf "Nachgelieferte" Daten.
	 *
	 * @return Anmeldeoptionen mit aktivierter Nachgeliefert-Option
	 */
	public static ReceiveOptions delayed() {
		return DELAYED;
	}

	/**
	 * Erzeugt Empfangsanmeldeoptionen mit festgelegten Werten f�r die Optionen. Die Anmeldung erfolgt ohne "Delta", d.h eine Anmeldung bezieht sich nicht nur auf
	 * ge�nderte Daten, sondern auch auf unver�nderte Datens�tze. Es werden keine "Nachgelieferten" Daten angemeldet..
	 *
	 * @return Anmeldeoptionen mit deaktivierter Delta-Option und deaktivierter Nachgeliefert-Option
	 */
	public static ReceiveOptions normal() {
		return NORMAL;
	}

	/**
	 * Bestimmt, ob sich eine Anmeldung mit diesen Optionen "mit Delta" erfolgt, d.h. sich nur auf ge�nderte Datens�tze bezieht.
	 *
	 * @return <code>true</code>, wenn die Anmeldung sich nur auf ge�nderte Datens�tze bezieht; sonst <code>false</code>.
	 */
	public final boolean withDelta() {
		return onlyDelta;
	}

	/**
	 * Bestimmt, ob sich eine Anmeldung mit diesen Optionen auch auf nachgelieferte Datens�tze bezieht.
	 *
	 * @return <code>true</code>, wenn die Anmeldung sich auch auf nachgelieferte Datens�tze bezieht; sonst <code>false</code>.
	 */
	public final boolean withDelayed() {
		return withDelayedData;
	}
}
