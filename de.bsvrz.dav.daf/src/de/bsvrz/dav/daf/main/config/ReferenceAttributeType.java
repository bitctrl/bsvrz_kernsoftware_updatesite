/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Referenz-Attributtypen. Attribute von diesem Attributtyp
 * referenzieren andere Objekte. Der Typ der referenzierten Objekte wird durch den Attributtyp festgelegt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface ReferenceAttributeType extends AttributeType, UndefinedAttributeValueAccess {
	/**
	 * Bestimmt den Typ von Objekten, die von Attributen dieses Attribut-Typs referenziert werden können.
	 *
	 * @return Typ der Objekte, die von Attributen dieses Typs referenziert werden können oder <code>null</code> falls
	 *         beliebige Objekte referenziert werden können.
	 */
	public SystemObjectType getReferencedObjectType();

	/**
	 * Bestimmt, ob undefinierte Referenzen in Attributwerten dieses Attributtyps zugelassen werden.
	 *
	 * @return ob undefinierte Referenzen in Attributwerten dieses Attributtyps zugelassen werden
	 */
	public boolean isUndefinedAllowed();

	/**
	 * Bestimmt, ob die in Attributwerten dieses Attributtyps enthaltenen Referenzen als gerichtete Assoziation, als
	 * Aggregation oder als Komposition realisiert werden sollen.
	 *
	 * @return die Referenzierungsart
	 */
	public ReferenceType getReferenceType();
}

