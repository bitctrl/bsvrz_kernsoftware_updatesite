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

/**
 * Persistenter Datensatz der über den Datensatzcontainer und den
 * Container-Datensatz innerhalb des Containers definiert wird.
 * Über den Datensatzcontainer werden identifizierende Information
 * (System-Objekt, Attributgruppe, Aspekt etc.) festgelegt. Weitere
 * Daten werden durch den Containerdatensatz definiert.
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class PersistentData {
	private final PersistentDataContainer _container;
	private final PersistentContainerData _data;

	/**
	 * Erzeugt einen neuen Datensatz.
	 * @param container  Zugeordneter Datensatzcontainer über den identifizierende Information
     *                   (System-Objekt, Attributgruppe, Aspekt etc.) festgelegt werden.
	 * @param data  Containerdatensatz über den weitere Daten (Zeitstempel, Nutzdaten etc.)
	 *              des Datensatzes festgelegt werden.
	 */
	public PersistentData(PersistentDataContainer container, PersistentContainerData data) {
		_container = container;
		_data = data;
	}

	/**
	 * Bestimmt den diesem Datensatz zugeordneten Datensatzcontainer.
	 * @return Datensatzcontainer.
	 */
	public PersistentDataContainer getContainer() {
		return _container;
	}

	/**
	 * Bestimmt den Containerdatensatz dieses Datensatzes.
	 * @return Containerdatensatz.
	 */
	public PersistentContainerData getData() {
		return _data;
	}
}
