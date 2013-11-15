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

package de.bsvrz.dav.daf.main.archive;

/**
 * Type einer Timingangabe. Timingangaben k�nnen sich auf den Datenzeitstempel,
 * den Archivzeitstempel oder den Datensatzindex beziehen.
 * Timingangabe werden in der Klasse {@link de.bsvrz.dav.daf.main.impl.archive.PersistentDataRequest} benutzt um
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public final class TimingType {

	/**
	 * Typ f�r Timingangaben die sich auf Datenzeitstempel beziehen.
	 */
	public static final TimingType DATA_TIME= new TimingType("Datenzeitstempel");

	/**
	 * Typ f�r Timingangaben die sich auf Archivzeitstempel beziehen.
	 */
	public static final TimingType ARCHIVE_TIME= new TimingType("Archivzeitstempel");

	/**
	 * Typ f�r Timingangaben die sich auf Datensatzindexe beziehen.
	 */
	public static final TimingType DATA_INDEX= new TimingType("Datensatzindex");


	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zur�ck. Das genaue Format ist nicht
	 * festgelegt und kann sich �ndern.
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return _name;
	}


	private final String _name;

	private TimingType(String name) {
		_name = name;
	}


}
