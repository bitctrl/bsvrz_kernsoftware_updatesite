/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;

/**
 * Diese Klasse speichert einen Datensatz und die dazugehörige Attributgruppenverwendung.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DataAndATGUsageInformation {
	private final AttributeGroupUsage _attributeGroupUsage;
	private final Data _data;


	/**
	 *
	 * @param attributeGroupUsage Attributgruppenverwendung
	 * @param data Datensatz, der die Attributgruppe der Attributgruppenverwendung benutzt.
	 *
	 * @throws IllegalArgumentException Wird unter folgenden Bediengungen geworfen: <ul>
	  <li>Die Attributgruppe ist null.</li>
	  <li>Der Datensatz ist null.</li>
	  <li>Die Attributgruppe der Attributgruppenverwendung stimmt nicht mit der Attributgruppe des Datensatzes überein.</li>
	 </ul>
	 */
	public DataAndATGUsageInformation(final AttributeGroupUsage attributeGroupUsage, final Data data) {

		// Prüfen ob die Parameter != null sind
		if(attributeGroupUsage == null){
			throw new IllegalStateException("Die übergebene Attributgruppenverwendung ist null.");
		}else if(data == null){
			throw new IllegalStateException("Der übergebene Datensatz ist null.");
		}

		// Prüfen ob der Datensatz die richtige ATG besitzt.
		if(attributeGroupUsage.getAttributeGroup().getPid().equals(data.getName()) == false){
			// Der übergebene Datensatz paßt nicht zur übergenen Verwendung
			throw new IllegalStateException("Die Attributgruppe des Datensatzes stimmt nicht mit der Attributgruppe der Attributgruppenverwendung überein. Attributgruppe des Datensatzes: " + data.getName() + " Attributgruppe der Attributguppenverwendung: " + attributeGroupUsage.getAttributeGroup().getPid());
		}

		_attributeGroupUsage = attributeGroupUsage;
		_data = data;
	}

	/**
	 *
	 * @return Attributgruppenverwendung, die im Konstruktor übergeben wurde.
	 */
	public AttributeGroupUsage getAttributeGroupUsage() {
		return _attributeGroupUsage;
	}

	/**
	 *
	 * @return Datensatz, der im Konstruktor übergeben wurde.
	 */
	public Data getData() {
		return _data;
	}


	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		final AttributeGroupUsage usage = getAttributeGroupUsage();
		builder.append(usage.getAttributeGroup().getPid());
		builder.append(":");
		builder.append(usage.getAspect().getPid());
		builder.append(":");
		builder.append(getData().toString());
		return builder.toString();
	}
}
