/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.puk.config.xmlFile.properties;

/**
 * Definiert eine Kommazahl wie sie in der K2S.DTD definiert wird.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
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
