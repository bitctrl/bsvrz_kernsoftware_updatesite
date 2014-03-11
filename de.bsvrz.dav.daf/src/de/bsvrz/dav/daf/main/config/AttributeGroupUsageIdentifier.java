/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstelle zum Zugriff auf die Identifizierung einer Attributgruppenverwendung bei der Kommunikation über den Datenverteiler.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision: 5052 $, $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $, $Author: rs $
 */
public interface AttributeGroupUsageIdentifier {
	/**
	 * Bestimmt die Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler.
	 *
	 * @return Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler
	 *
	 */
	public long getIdentificationForDav();


}
