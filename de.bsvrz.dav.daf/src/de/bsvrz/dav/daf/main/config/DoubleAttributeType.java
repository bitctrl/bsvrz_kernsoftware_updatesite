/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Flie�komma-Attributtypen. Attribute von diesem
 * Attributtyp enthalten Flie�kommazahl mit 32 oder 64 Bit Genauigkeit. Durch den Attributtyp wird die Einheit
 * und die Genauigkeit von Werten dieses Typs definiert.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface DoubleAttributeType extends AttributeType {
	/**
	 * Ergebnis der Methode {@link #getAccuracy()} f�r eine Genauigkeit entsprechend dem IEEE 754 floating-point
	 * "single format" mit 32 Bits.
	 */
	public final static byte FLOAT = 0;

	/**
	 * Ergebnis der Methode {@link #getAccuracy()} f�r eine Genauigkeit entsprechend dem IEEE 754 floating-point
	 * "double format" mit 64 Bits.
	 */
	public final static byte DOUBLE = 1;

	/**
	 * Bestimmt die Ma�einheit von Werten dieses Attributtyps.
	 *
	 * @return Ma�einheit dieses Attributtyps.
	 */
	public String getUnit();

	/**
	 * Bestimmt die Genauigkeit von Attributen dieses Typs.
	 *
	 * @return {@link #FLOAT} oder {@link #DOUBLE}.
	 */
	public byte getAccuracy();
}
