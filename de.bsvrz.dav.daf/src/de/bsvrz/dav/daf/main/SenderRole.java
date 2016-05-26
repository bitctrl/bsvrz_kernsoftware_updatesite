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
 * Verwaltung der Rolle der Applikation bei Sendeanmeldungen. Die Klasse verwaltet, ob eine Anmeldung als Quelle oder als einfacher Sender erfolgen soll. Eine
 * neue Rolle kann mit dem {@link #SenderRole Konstruktor} oder mit den Klassenmethoden {@link #source} und {@link #sender} instanziiert werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ClientDavInterface#subscribeSender
 */
public final class SenderRole {

	/** Anmeldung als Quelle. */
	private static SenderRole SOURCE = new SenderRole(true);

	/** Anmeldung als Sender. */
	private static SenderRole SENDER = new SenderRole(false);

	/** Speichert, ob die Applikation in dieser Rolle eine Quelle ist. */
	private boolean _isSource;

	/**
	 * Erzeugt die Rolle der Applikation als Quelle oder als einfacher Sender.
	 *
	 * @param asSource <code>true</code>, falls eine Anmeldung als Quelle erfolgen soll oder <code>false</code> falls eine Anmeldung als einfacher Sender erfolgen
	 *                 soll.
	 */
	public SenderRole(boolean asSource) {
		_isSource = asSource;
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
		if(!(other instanceof SenderRole)) return false;
		return _isSource == ((SenderRole)other)._isSource;
	}

	/**
	 * Bestimmt den Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts.
	 */
	public int hashCode() {
		return _isSource ? 1 : 0;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		if(_isSource) {
			return "Quelle";
		}
		else {
			return "Sender";
		}
	}

	/**
	 * Erzeugt die Rolle der Applikation als Quelle.
	 *
	 * @return Rolle für Empfangsanmeldungen als Quelle.
	 */
	public static SenderRole source() {
		return SOURCE;
	}

	/**
	 * Erzeugt die Rolle der Applikation als einfacher Sender.
	 *
	 * @return Rolle für Empfangsanmeldungen als einfacher Sender.
	 */
	public static SenderRole sender() {
		return SENDER;
	}

	/**
	 * Bestimmt, ob die Applikation in dieser Rolle eine Quelle ist.
	 *
	 * @return <code>true</code>, falls die Applikation in dieser Rolle eine Quelle ist; sonst <code>false</code>.
	 */
	public final boolean isSource() {
		return _isSource;
	}

	/**
	 * Bestimmt, ob die Applikation in dieser Rolle ein einfacher Sender ist.
	 *
	 * @return <code>true</code>, falls die Applikation in dieser Rolle ein Sender ist; sonst <code>false</code>.
	 */
	public final boolean isSender() {
		return !_isSource;
	}
}
