/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Speichert die Identifizierung einer Attributgruppenverwendung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AttributeGroupAspectCombination {

	/** Die Identifizierung der Attributgruppenverwendung. */
	private long _atgUsageIdentification;

	/** Erzeugt ein neues Objekt zur Deserialisierung mittels der read-Methode */
	public AttributeGroupAspectCombination() {
	}

	/**
	 * Erzeugt ein neues Objekt mit der angegebenen Identifizierung.
	 *
	 * @param attributeUsageIdentification Identifizierung der Attributgruppenverwendung
	 */
	public AttributeGroupAspectCombination(long attributeUsageIdentification) {
		_atgUsageIdentification = attributeUsageIdentification;
	}

	/**
	 * Liefert die Identifizierung der Attributgruppenverwendung zurück.
	 *
	 * @return Identifizierung der Attributgruppenverwendung.
	 */
	public long getAtgUsageIdentification() {
		return _atgUsageIdentification;
	}

	/**
	 * Vergleicht dieses Objekt mit einem anderen.
	 *
	 * @param otherObject anderes Objekt
	 *
	 * @return <code>true</code>, wenn das übergebene Objekt die gleiche Identifizierung hat wie dieses Objekt; sonst <code>false</code>.
	 */
	public boolean equals(Object otherObject) {
		if(!(otherObject instanceof AttributeGroupAspectCombination)) {
			return false;
		}
		AttributeGroupAspectCombination other = (AttributeGroupAspectCombination)otherObject;
		return _atgUsageIdentification == other._atgUsageIdentification;
	}

	/**
	 * Bestimmt einen Hash-Code dieses Objekts.
	 *
	 * @return Hash-Code dieses Objekts
	 */
	public int hashCode() {
		return (int)(_atgUsageIdentification ^ (_atgUsageIdentification >>> 32));
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zurück. Das genaue Format ist nicht festgelegt und kann sich ändern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public final String toString() {
		return "Attributgruppenverwendung{id:" + _atgUsageIdentification + "}";
	}

	/**
	 * Serialisiert dieses Objekt.
	 *
	 * @param out Stream auf den das Objekt geschrieben werden soll.
	 *
	 * @throws java.io.IOException Falls nicht auf dem Stream geschrieben werden kann.
	 */
	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(_atgUsageIdentification);
	}

	/**
	 * Deserialisiert dieses Objekt.
	 *
	 * @param in Stream von dem das Objekt gelesen werden soll.
	 *
	 * @throws java.io.IOException Falls nicht vom Stream gelesen werden kann.
	 */
	public final void read(DataInputStream in) throws IOException {
		_atgUsageIdentification = in.readLong();
	}
}
