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
 * Schnittstellenklasse zum Zugriff auf die gemeinsamen Eigenschaften von Attributtypen. Über Attributtypen
 * ist Zugriff auf die beschreibenden Information von konkreten Attributwerten möglich. Je nach Art wird von
 * einem Attributtyp eine der folgenden Schnittstellenklassen unterstützt:
 * <ul>
 * 	<li>{@link StringAttributeType} für Zeichenketten,</li>
 * 	<li>{@link IntegerAttributeType} für Ganze Zahlen,</li>
 * 	<li>{@link DoubleAttributeType} für Fließkommazahlen,</li>
 * 	<li>{@link TimeAttributeType} für Zeitstempel und</li>
 * 	<li>{@link ReferenceAttributeType} für Objekt-Referenzen und</li>
 * 	<li>{@link AttributeListDefinition} für Attributlisten in strukturierten Attributgruppen.</li>
 * </ul>
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface AttributeType extends ConfigurationObject {
	/**
	 * Ermittelt den Default-Attributwert dieses Attributtyps.
	 * @return Default-Attributwert dieses Attributtyps oder <code>null</code> falls kein Defaultwert festgelegt wurde.
	 */
	String getDefaultAttributeValue();
}

