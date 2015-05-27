/*
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

package de.bsvrz.dav.daf.main.impl.archive;




/**
 * Schnittstelle, die vom Persistenzmodul implementiert wird um die bereitgestellten Datensätze
 * streambasiert zu übertragen.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
 */
public interface PersistentDataStreamSupplier {

	/**
	 * Bestimmt den nächsten zu übertragenden Datensatz dieses Datensatzstroms.
	 * @return Nächster zu übertragender Datensatz oder <code>null</code> wenn kein Datensatz mehr zu übertragen ist.
	 */
	PersistentData fetchNextData();

	/**
	 * Wird aufgerufen, wenn die Übertragung abgebrochen werden soll, also keine weiteren Datensätze
	 * übertragen werden sollen.
	 */
	void cancel();


}
