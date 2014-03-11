/*
 * Copyright 2012 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.commandLineArgs.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.commandLineArgs; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.commandLineArgs;

import java.lang.IllegalArgumentException;
import java.lang.String;

/**
 * TBD kommentieren
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11174 $
 */
public class ScaleUnit {

	/**
	 * Wandelt die Masseinheit in einem Faktor um.
	 * Dabei wird typische Masseinheiten in der IT-Technik verwendet,
	 * mit der 2er Potenz
	 *
	 * @param scaleUnit Messeinheit z.B. kiB, GiB, TiB usw.
	 *
	 * @return
	 */
	public static long getScaleFactorBinary(final String scaleUnit) {
		if(scaleUnit == null) throw new IllegalArgumentException("Ihre Präfix-Angabe ist ungültig. " + scaleUnit);
		if(scaleUnit.length() == 0) return 1;
		switch(scaleUnit.toLowerCase().charAt(0)) {
			case 'k':
				return 1024;
			case 'm':
				return 1024 * 1024;
			case 'g':
				return 1024 * 1024 * 1024;
			case 't':
				return (1024l * 1024l * 1024l * 1024l);
			case 'p':
				return (1024l * 1024l * 1024l * 1024l * 1024l);
			case 'e':
				return (1024l * 1024l * 1024l * 1024l * 1024l * 1024l);
			default:
				throw new IllegalScaleUnitException("Ihre Angabe ist keine gültige Einheit! " + scaleUnit);
		}
	}

	/**
	 * Wandelt die Masseinheit in einem Faktor um.
	 *
	 * @param scaleUnit Messeinheit z.B. kilo, Giga, Terra usw.
	 *
	 * @return
	 */
	public static long getScaleFactorDecimal(final String scaleUnit) {
		if(scaleUnit == null) throw new IllegalArgumentException("Ihre Präfix-Angabe ist ungültig. " + scaleUnit);
		if(scaleUnit.length() == 0) return 1;
		switch(scaleUnit.toLowerCase().charAt(0)) {
			case 'h':
				return 100;
			case 'k':
				return 1000;
			case 'm':
				return 1000 * 1000;
			case 'g':
				return 1000 * 1000 * 1000;
			case 't':
				return (1000l * 1000l * 1000l * 1000l);
			case 'p':
				return (1000l * 1000l * 1000l * 1000l * 1000l);
			case 'e':
				return (1000l * 1000l * 1000l * 1000l * 1000l * 1000l);
			default:
				throw new IllegalScaleUnitException("Ihre Angabe ist keine gültige Einheit! " + scaleUnit);
		}
	}

	/**
	 * Wandelt die Masseinheit in einem Faktor um.
	 *
	 * @param scaleUnit Messeinheit z.B. kilo, Giga, Terra usw.
	 *
	 * @return
	 */
	public static double getScaleFactorDecimalFloatingPoint(final String scaleUnit){
		if(scaleUnit == null) throw new IllegalArgumentException("Ihre Präfix-Angabe ist ungültig. " + scaleUnit);
		if(scaleUnit.length() == 0) return 1;
		switch(scaleUnit.toLowerCase().charAt(0)) {
			case 'd':
				return 0.1;
			case 'c':
				return 0.01;
			case 'm':
				return 0.001;
			case 'y':
				return 0.000001;
			case 'n':
				return 0.000000001;
			case 'p':
				return 0.000000000001;
			case 'f':
				return 0.000000000000001;
			case 'a':
				return 0.000000000000000001;
			case 'z':
				return 0.000000000000000000001;
			default:
				throw new IllegalScaleUnitException("Ihre Angabe ist keine gültige Einheit! " + scaleUnit);
		}
	}

	private static class IllegalScaleUnitException extends IllegalArgumentException{
		public IllegalScaleUnitException(final String message){
			super(message);
		}
	}
}
