/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstelle, zum Aufsuchen von Objekten.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision: none $, $Date: 04.12.2006 $, $Author: rs $
 */
public interface ObjectLookup {
	/**
	 * Liefert das System-Objekt mit der angegebenen PID zurück.
	 *
	 * @param pid Die permanente ID des System-Objekts
	 * @return Das gewünschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 *
	 * @see DataModel
	 */
	public SystemObject getObject(String pid);

	/**
	 * Liefert das System-Objekt mit der angegebenen Objekt-ID zurück.
	 *
	 * @param id Die Objekt-ID des System-Objekts
	 * @return Das gewünschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen ID gibt.
	 *
	 * @see DataModel
	 */
	public SystemObject getObject(long id);
}
