/*
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Definiert eine Kommazahl wie sie in der K2S.DTD definiert wird.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationDoubleDef implements ConfigurationAttributeType {
	private String _unit = "";
	private AccuracyDouble _accuracyDouble = AccuracyDouble.DOUBLE;

	public void setUnit(String unit) {
		_unit = unit;
	}

	public void setAccuracy(String accuracy) {
		if (AccuracyDouble.DOUBLE.getValue().equals(accuracy)) {
			_accuracyDouble = AccuracyDouble.DOUBLE;
		} else if (AccuracyDouble.FLOAT.getValue().equals(accuracy)) {
			_accuracyDouble = AccuracyDouble.FLOAT;
		}
	}

	/**
	 * Attribut "genauigkeit"
	 * @param accuracyDouble s.o.
	 */
	public void setAccuracy(AccuracyDouble accuracyDouble) {
		_accuracyDouble = accuracyDouble;
	}

	/**
	 * Attribut "einheit"
	 * @return einheit oder "" falls der Wert nicht gesetzt wurde
	 */
	public String getUnit() {
		return _unit;
	}

	/**
	 *
	 * @return Attribut "genauigkeit"
	 */
	public AccuracyDouble getAccuracy() {
		return _accuracyDouble;
	}
}
