/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tuple bestehend aus zwei Objekten. Implementiert {@link java.io.Serializable}. Das Serialisieren kann aber trotzdem fehlschlagen, wenn eines der Elemente
 * nicht serialisierbar ist.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 * @param <X> Erstes Element
 * @param <Y> Zweites Element
 */
public class Tuple<X, Y> implements Comparable, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4053465783773266899L;

	/** Element #1 */
	public X first;

	/** Element #2 */
	public Y last;

	/**
	 * Setzt die Werte des Tupels.
	 *
	 * @param first
	 * @param last
	 */
	public Tuple(X first, Y last) {
		this.first = first;
		this.last = last;
	}

	/**
	 * Vergleicht ob die Elemente der Objekte übereinstimmen. Dazu müssen {@link #first} und {@link #last} ungleich <code>null</code> sein.
	 *
	 * @param tupel Vergleichsobjekt
	 *
	 * @return Ergebnis des Vergleichs
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object tupel) {
		boolean result = false;
		if(tupel instanceof Tuple) {
			Tuple obj = (Tuple)tupel;
			if(obj != null) result = Objects.equals(first, obj.first) && Objects.equals(last, obj.last);
		}
		return result;
	}

	/** @see java.lang.Object#hashCode() */
	public int hashCode() {
		int hashCode = 1;
		if(first != null) hashCode = first.hashCode();
		if(last != null) hashCode = 37 * hashCode + last.hashCode();
		return hashCode;
	}

	/** @see java.lang.Object#toString() */
	public String toString() {
		return "< " + first + " , " + last + " > ";
	}

	/**
	 * Vergleicht die Werte von {@link #first} falls sie {@link Comparable} implementieren. Wirft keine ClassCastException.
	 *
	 * @param o Zu vergleichendes Objekt.
	 *
	 * @return Positiver Wert falls this.first > other.first, negativer Wert falls this.first < other.first, null sonst.
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object o) {
		if(o instanceof Tuple) {
			Tuple other = (Tuple)o;

			if(other.first instanceof Comparable) {
				if(this.first instanceof Comparable) {
					Comparable thisF = (Comparable)this.first;
					Comparable otherF = (Comparable)other.first;

					try {
						return thisF.compareTo(otherF);
					}
					catch(Exception e) {
						return 0;
					}
				}
				else {
					return 0;
				}
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}
}
