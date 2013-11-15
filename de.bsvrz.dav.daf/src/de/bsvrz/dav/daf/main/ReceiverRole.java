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
 * Verwaltung der Rolle der Applikation bei Empfangsanmeldungen. Die Klasse verwaltet, ob eine Anmeldung als normaler Empf�nger oder als Senke erfolgen soll.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see ClientDavInterface#subscribeReceiver
 */
public final class ReceiverRole {

	/** Anmeldung als Senke. */
	private static final ReceiverRole DRAIN = new ReceiverRole(true);

	/** Anmeldung als Empf�nger. */
	private static final ReceiverRole RECEIVER = new ReceiverRole(false);

	/** Speichert, ob die Applikation in dieser Rolle eine Senke ist. */
	private final boolean _isDrain;

	/**
	 * Erzeugt die Rolle der Applikation als normalen Empf�nger oder als Senke.
	 *
	 * @param asDrain <code>true</code>, wenn die Anmeldung als Senke erfolgen soll; <code>false</code>, wenn die Anmeldung als Empf�nger erfolgen soll.
	 */
	public ReceiverRole(boolean asDrain) {
		_isDrain = asDrain;
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
		if(!(other instanceof ReceiverRole)) return false;
		return _isDrain == ((ReceiverRole)other)._isDrain;
	}

	/**
	 * Bestimmt den Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts.
	 */
	public int hashCode() {
		return _isDrain ? 1 : 0;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zur�ck.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		if(_isDrain) {
			return "Senke";
		}
		else {
			return "Empf�nger";
		}
	}

	/**
	 * Erzeugt die Rolle der Applikation als normalen Empf�nger.
	 *
	 * @return Rolle f�r Empfangsanmeldungen als normaler Empf�nger.
	 */
	public static ReceiverRole receiver() {
		return RECEIVER;
	}

	/**
	 * Erzeugt die Rolle der Applikation als Senke.
	 *
	 * @return Rolle f�r Empfangsanmeldungen als Senke.
	 */
	public static ReceiverRole drain() {
		return DRAIN;
	}

	/**
	 * Bestimmt, ob die Applikation in dieser Rolle eine Senke ist.
	 *
	 * @return <code>true</code>, falls die Applikation in dieser Rolle eine Senke ist; sonst <code>false</code>.
	 */
	public final boolean isDrain() {
		return _isDrain;
	}

	/**
	 * Bestimmt, ob die Applikation in dieser Rolle ein normaler Empf�nger ist.
	 *
	 * @return <code>true</code>, falls die Applikation in dieser Rolle ein Empf�nger ist; sonst <code>false</code>.
	 */
	public final boolean isReceiver() {
		return !_isDrain;
	}
}
