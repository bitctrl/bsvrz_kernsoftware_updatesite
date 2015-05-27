/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.archive;

/**
 * Persistenter Datensatz der über den Datensatzcontainer und den
 * Container-Datensatz innerhalb des Containers definiert wird.
 * Über den Datensatzcontainer werden identifizierende Information
 * (System-Objekt, Attributgruppe, Aspekt etc.) festgelegt. Weitere
 * Daten werden durch den Containerdatensatz definiert.
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
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
