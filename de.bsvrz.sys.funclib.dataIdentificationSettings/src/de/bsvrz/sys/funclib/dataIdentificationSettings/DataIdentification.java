/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.dataIdentificationSettings.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.dataIdentificationSettings; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.dataIdentificationSettings;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.DataDescription;

/**
 * Speichert Identifizierende Informationen wie Systemobjekt, Attributgruppe, Aspekt und Simulationsvariante zu einem
 * Datensatz.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DataIdentification implements Comparable {
	private final SystemObject _object;
	private final DataDescription _dataDescription;


	/**
	 * Erzeugt eine neue Datenidentifikation aus dem übergebenen Systemobjekt und der übergebenen Datenbeschreibung.
	 *
	 * @param object          Systemobjekt auf das sich die neue Datenidentifikation beziehen soll.
	 * @param dataDescription Datenbeschreibung auf die sich die neue Datenidentifikation beziehen soll.
	 */
	public DataIdentification(SystemObject object, DataDescription dataDescription) {
		_object = object;
		_dataDescription = dataDescription;
	}

	/**
	 * Bestimmt das Systemobjekt dieser Datenidentifikation.
	 *
	 * @return Systemobjekt dieser Datenidentifikation
	 */
	public SystemObject getObject() {
		return _object;
	}

	/**
	 * Bestimmt die Datenbeschreibung dieser Datenidentifikation zurück. Die Datenbeschreibung enthält Attributgruppe,
	 * Aspekt und Simulationsvariante
	 *
	 * @return Datenbeschreibung dieser Datenidentifikation.
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Bestimmt einen hashCode, der nur von den im Konstruktor übergebenen Werten abhängig ist. Verschiedene Objekte dieser
	 * Klasse, die das gleiche Systemobjekt und die gleiche Datenbeschreibung enthalten werden als gleich angesehen und
	 * liefern den gleichen Hashcode.
	 *
	 * @return Hashcode dieses Objekts.
	 * @see #equals
	 * @see #compareTo
	 */
	public int hashCode() {
		return _object.hashCode() + _dataDescription.hashCode() * 37;
	}

	/**
	 * Prüft, ob dieses Objekt dem übergebenen Objekt gleicht. Verschiedene Objekte dieser Klasse, die das gleiche
	 * Systemobjekt und die gleiche Datenbeschreibung enthalten werden als gleich angesehen.
	 *
	 * @param object Das Objekt mit dem dieses Objekt verglichen werden soll.
	 * @return <code>true</code> falls die Objekte gleich sind; sonst <code>false</code>.
	 * @see #hashCode()
	 * @see #compareTo
	 */
	public boolean equals(Object object) {
		if(!(object instanceof DataIdentification)) {
			return false;
		}
		DataIdentification o = (DataIdentification)object;
		return _object.equals(o._object) && _dataDescription.equals(o._dataDescription);
	}


	/**
	 * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.<p>
	 * <p>
	 * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to
	 * whether the value of <i>expression</i> is negative, zero or positive.
	 * <p>
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.
	 * (This implies that <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt> throws an
	 * exception.)<p>
	 * <p>
	 * The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp;
	 * y.compareTo(z)&gt;0)</tt> implies <tt>x.compareTo(z)&gt;0</tt>.<p>
	 * <p>
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) ==
	 * sgn(y.compareTo(z))</tt>, for all <tt>z</tt>.<p>
	 * <p>
	 * It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.
	 * Generally speaking, any class that implements the <tt>Comparable</tt> interface and violates this condition should
	 * clearly indicate this fact.  The recommended language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 *         specified object.
	 * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
	 */
	public int compareTo(Object o) {
		DataIdentification other = (DataIdentification)o;
		int result;
		result = _object.compareTo(other._object);
		if(result != 0) return result;
		result = _dataDescription.getAttributeGroup().compareTo(other._dataDescription.getAttributeGroup());
		if(result != 0) return result;
		result = _dataDescription.getAspect().compareTo(other._dataDescription.getAspect());
		if(result != 0) return result;
		result = _dataDescription.getSimulationVariant() - other._dataDescription.getSimulationVariant();
		return result;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "DataIdentification{" +
		        "_object=" + _object +
		        ", _dataDescription=" + _dataDescription +
		        "}";
	}
}

