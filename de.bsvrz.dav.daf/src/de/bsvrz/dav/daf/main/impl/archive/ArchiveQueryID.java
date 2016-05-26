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

package de.bsvrz.dav.daf.main.impl.archive;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Die Aufgabe dieser Klasse ist es, einen Hashcode für eine Archivanfrage bereitzustellen. Dieser hashCode wird als Schlüssel für eine Hashtable benutzt um
 * eine Archivanfrage zu finden.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ArchiveQueryID {

	private final int _indexOfRequest;

	private final SystemObject _objectReference;

	/**
	 * Konstruktor, dieser erzeugt ein Objekt, das als Key für eine Hashtable benutzt werden kann.
	 *
	 * @param indexOfRequest  ein beliebiger Index
	 * @param objectReference Die Referenz auf ein Objekt
	 */
	public ArchiveQueryID(int indexOfRequest, SystemObject objectReference) {
		_indexOfRequest = indexOfRequest;
		_objectReference = objectReference;
	}

	/**
	 * Diese Methode berechnet den hashCode des Objekts. Ist <code>_objectReference == null</code> so wird der hashCode ebenfalls berechent. Die Referenz auf null
	 * fließt mit dem Integerwert "0" in die Berechnung ein.
	 *
	 * @return hashCode des Objekts
	 */
	public int hashCode() {

		int hashCode = _indexOfRequest;

		if(_objectReference != null) {
			hashCode = 11 * hashCode + _objectReference.hashCode();
		}
		else {
			hashCode = 11 * hashCode + 0;
		}

		return hashCode;
	}

	public boolean equals(Object o) {
		if(o instanceof ArchiveQueryID) {
			ArchiveQueryID other = (ArchiveQueryID)o;
			return (_indexOfRequest == other.getIndexOfRequest()) && (_objectReference == other.getObjectReference());
		}
		else {
			return false;
		}
	}

	public int getIndexOfRequest() {
		return _indexOfRequest;
	}

	public SystemObject getObjectReference() {
		return _objectReference;
	}

	public String toString() {
		return "ArchiveQueryID{" + "_indexOfRequest=" + _indexOfRequest + ", _objectReference=" + _objectReference.getNameOrPidOrId() + "}";
	}
}

