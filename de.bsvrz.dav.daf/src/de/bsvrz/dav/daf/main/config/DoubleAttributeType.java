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
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Fließkomma-Attributtypen. Attribute von diesem
 * Attributtyp enthalten Fließkommazahl mit 32 oder 64 Bit Genauigkeit. Durch den Attributtyp wird die Einheit
 * und die Genauigkeit von Werten dieses Typs definiert.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface DoubleAttributeType extends AttributeType {
	/**
	 * Ergebnis der Methode {@link #getAccuracy()} für eine Genauigkeit entsprechend dem IEEE 754 floating-point
	 * "single format" mit 32 Bits.
	 */
	public final static byte FLOAT = 0;

	/**
	 * Ergebnis der Methode {@link #getAccuracy()} für eine Genauigkeit entsprechend dem IEEE 754 floating-point
	 * "double format" mit 64 Bits.
	 */
	public final static byte DOUBLE = 1;

	/**
	 * Bestimmt die Maßeinheit von Werten dieses Attributtyps.
	 *
	 * @return Maßeinheit dieses Attributtyps.
	 */
	public String getUnit();

	/**
	 * Bestimmt die Genauigkeit von Attributen dieses Typs.
	 *
	 * @return {@link #FLOAT} oder {@link #DOUBLE}.
	 */
	public byte getAccuracy();
}
