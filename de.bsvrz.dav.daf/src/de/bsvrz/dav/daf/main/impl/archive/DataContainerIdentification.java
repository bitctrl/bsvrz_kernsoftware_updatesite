/*
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

package de.bsvrz.dav.daf.main.impl.archive;



import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;

/**
 * Identifikation der Archivdaten eines Archivdatencontainers.
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class DataContainerIdentification implements Comparable {
	private final SystemObject _object;
	private final DataDescription _dataDescription;
	private final ArchiveDataKind _archiveDataKind;

	/**
	 * Erzeugt eine neue Containeridentifikation.
	 * @param object  System-Objekt auf das sich alle Datensätze des entsprechenden Containers beziehen.
	 * @param dataDescription  Attributgruppe, Aspekt und Simulationsvariante auf die sich alle
	 *                         Datensätze des entsprechenden Containers beziehen.
	 * @param archiveDataKind  Datensatzart der Datensätze im entsprechenden Container
	 *                         (aktuell, nachgeliefert, nachgefordert).
	 */
	public DataContainerIdentification(SystemObject object, DataDescription dataDescription, ArchiveDataKind archiveDataKind) {
		_object = object;
		_dataDescription = dataDescription;
		_archiveDataKind = archiveDataKind;
	}

	/**
	 * Bestimmt das System-Objekt auf das sich alle Datensätze des entsprechenden Containers beziehen.
	 * @return Zugeordnetes System-Objekt.
	 */
	public SystemObject getObject() {
		return _object;
	}

	/**
	 * Bestimmt Attributgruppe, Aspekt und Simulationsvariante auf die sich alle
	 * Datensätze des entsprechenden Containers beziehen.
	 * @return Datenbeschreibung mit Attributgruppe, Aspekt und Simulationsvariante.
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Bestimmt die Datensatzart (aktuell, nachgeliefert, nachgefordert) der Datensätze
	 * im entsprechenden Container.
	 * @return Zugeordnete Datensatzart.
	 */
	public ArchiveDataKind getArchiveDataKind() {
		return _archiveDataKind;
	}

	/**
	 * Bestimmt einen hashCode, der nur von den im Konstruktor übergebenen Werten abhängig ist.
	 * Verschiedene Objekte dieser Klasse, die das gleiche Systemobjekt, die gleiche Datenbeschreibung
	 * und die gleiche Archivdatensatzart enthalten werden als gleich angesehen.
	 * @see #equals
	 * @see #compareTo
	 * @return Hashcode dieses Objekts.
	 */
	public int hashCode() {
		return (_object.hashCode() + _dataDescription.hashCode() * 37) * 4 + _archiveDataKind.hashCode();
	}

	/**
	 * Prüft, ob dieses Objekt dem übergebenen Objekt gleicht.
	 * Verschiedene Objekte dieser Klasse, die das gleiche Systemobjekt, die gleiche Datenbeschreibung
	 * und die gleiche Archivdatensatzart enthalten werden als gleich angesehen.
	 * @param   object   Das Objekt mit dem dieses Objekt verglichen werden soll.
	 * @return  <code>true</code> falls die Objekte gleich sind; sonst <code>false</code>.
	 * @see     #hashCode()
	 * @see     #compareTo
	 */
	public boolean equals(Object object) {
		if(!(object instanceof DataContainerIdentification)) {
			return false;
		}
		DataContainerIdentification o= (DataContainerIdentification)object;
		return _object.equals(o._object) && _dataDescription.equals(o._dataDescription) && _archiveDataKind.equals(o._archiveDataKind);
	}


	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.<p>
	 *
	 * In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
	 * is negative, zero or positive.
	 *
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)<p>
	 *
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.<p>
	 *
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.<p>
	 *
	 * It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 *
	 * @param   o the Object to be compared.
	 * @return  a negative integer, zero, or a positive integer as this object
	 *		is less than, equal to, or greater than the specified object.
	 *
	 * @throws ClassCastException if the specified object's type prevents it
	 *         from being compared to this Object.
	 */
	public int compareTo(Object o) {
		DataContainerIdentification other = (DataContainerIdentification)o;
		int result;
		result= _object.compareTo(other._object);
		if(result!=0) return result;
		result= _dataDescription.getAttributeGroup().compareTo(other._dataDescription.getAttributeGroup());
		if(result!=0) return result;
		result= _dataDescription.getAspect().compareTo(other._dataDescription.getAspect());
		if(result!=0) return result;
		result= _dataDescription.getSimulationVariant() - other._dataDescription.getSimulationVariant();
		if(result!=0) return result;
		result= _archiveDataKind.getCode() - other._archiveDataKind.getCode();
		return result;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke.
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "DataContainerIdentification{" +
		        "_object=" + _object +
		        ", _dataDescription=" + _dataDescription +
		        ", _archiveDataKind=" + _archiveDataKind +
		        "}";
	}
}
