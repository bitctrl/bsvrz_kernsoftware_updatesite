/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

import java.util.*;

/**
 * Schnittstelle zum Zugriff auf Attribute und Eigenschaften einer Attributgruppe.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface AttributeGroup extends AttributeSet {

	/**
	 * Ermittelt, ob diese Attributgruppe mit dem Aspekt "asp.eigenschaften" für konfigurierende Datensätze benutzt werden kann.
	 *
	 * @return <code>true</code>, wenn die Attributgruppe mit dem Aspekt "asp.eigenschaften" für konfigurierende Datensätze benutzt werden kann; sonst
	 *         <code>false</code>.
	 *
	 * @deprecated Wird durch die Methode {@link AttributeGroupUsage#isConfigurating} ersetzt.
	 */
	public boolean isConfigurating();

	/**
	 * Gibt an, ob diese Attributgruppe als Parameter verwendet werden kann oder nicht.
	 *
	 * @return <code>true</code>, wenn die Attributgruppe als Parameter verwendet werden kann;<br/> <code>false</code>, wenn die Attributgruppe nicht als Parameter
	 *         verwendet werden kann.
	 */
	public boolean isParameter();

	/**
	 * Bestimmt die möglichen Aspekte, unter denen die Attributgruppe verwendet werden kann.
	 *
	 * @return Liste von {@link Aspect Aspekten}
	 */
	public Collection<Aspect> getAspects();

	/**
	 * Liefert alle Attributgruppenverwendungen dieser Attributgruppe zurück.
	 *
	 * @return alle Attributgruppenverwendungen dieser Attributgruppe
	 */
	public Collection<AttributeGroupUsage> getAttributeGroupUsages();

	/**
	 * Gibt die Attributgruppenverwendung des angegebenen Aspekts zurück.
	 *
	 * @param aspect Aspekt der gewünschten Attributgruppenverwendung
	 *
	 * @return Attributgruppenverwendung für den angegebenen Aspekt oder <code>null</code>, falls zum angegebenen Aspekt keine Attributgruppenverwendung definiert
	 *         ist.
	 */
	public AttributeGroupUsage getAttributeGroupUsage(Aspect aspect);
}

