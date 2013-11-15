/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.archive;

/**
 * Diese Klasse stellt eine feste Anzahl von Prioritätsobjekten zur Verfügung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public final class ArchiveQueryPriority {

	/**
	 * höchste Priorität
	 */
	public static final ArchiveQueryPriority HIGH = new ArchiveQueryPriority("High", 1);
	/**
	 * mittlere Priorität
	 */
	public static final ArchiveQueryPriority MEDIUM = new ArchiveQueryPriority("Medium", 2);
	/**
	 * niedrigste Priorität
	 */
	public static final ArchiveQueryPriority LOW = new ArchiveQueryPriority("Low", 3);

	/**
	 * Diese Methode erlaubt es, eine Referenz auf ein Objekt der Klasse anzufordern.
	 *
	 * @param priority Code, der die Priorität identifiziert (1,2,3)
	 * @return Referenz auf ein Objekt dieser Klasse
	 */
	public static ArchiveQueryPriority getInstance(int priority) {
		switch (priority) {
			case 1:
				return HIGH;
			case 2:
				return MEDIUM;
			case 3:
				return LOW;
			default:
				throw new IllegalArgumentException("Undefinierte Priorität");
		}
	}

	/**
	 * Gibt die Priorität eines Objektes als <code>int</code> zurück
	 *
	 * @return Priorität des Objekts
	 */
	public int getCode() {
		return _priorityInt;
	}

	/**
	 * Gibt die Priorität eines Objektes als <code>String</code> zurück
	 *
	 * @return Priorität des Objekts
	 */
	public String getStringPriority() {
		return _priorityString;
	}

	/**
	 * Wandelt das Objekt in einen String um
	 * @return Priorität als String
	 */
	public String toString() {
		return _priorityString + " " + _priorityInt;
	}

	private final String _priorityString;
	private final int _priorityInt;

	private ArchiveQueryPriority(String priorityString, int priorityInt) {
		_priorityString = priorityString;
		_priorityInt = priorityInt;
	}
}
