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
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Zeitstempel-Attributtypen. Attribute von diesem
 * Attributtyp enthalten Zeitangaben. Unterschieden werden relative und absolute Zeitangaben. Absolute
 * Zeitangaben beziehen sich immer auf den 1. Januar 1970 00:00 Uhr UTC. Die Auflösung ist entweder Sekunden
 * oder Millisekunden.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface TimeAttributeType extends AttributeType, UndefinedAttributeValueAccess {
	/**
	 * Ergebnis der Methode {@link #getAccuracy()} für sekundengenaue Auflösung.
	 */
	public static final byte SECONDS = 0;

	/**
	 * Ergebnis der Methode {@link #getAccuracy()} für millisekundengenaue Auflösung.
	 */
	public static final byte MILLISECONDS = 1;

	/**
	 * Bestimmt, ob Attribute dieses Attributtyps relative Zeitangaben enthalten.
	 *
	 * @return <code>true</code> bei relativen Zeitangaben oder <code>false</code> bei absoluten Zeitangaben.
	 */
	public boolean isRelative();

	/**
	 * Bestimmt die Genauigkeit der zeitlichen Auflösung von Attributen dieses Typs.
	 *
	 * @return {@link #SECONDS} oder {@link #MILLISECONDS}
	 */
	public byte getAccuracy();
}

