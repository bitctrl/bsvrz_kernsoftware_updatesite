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

import java.util.*;

/**
 * Diese Klasse beschreibt eine Ganzzahl nach der K2S.DTD.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationIntegerDef implements ConfigurationAttributeType {

	private int _bits = -1;

	/** Speichert die Objekte ConfigurationValueRange und ConfigurationState */
	private ConfigurationIntegerValueRange _regionAndState[] = new ConfigurationIntegerValueRange[0];

	/**
	 * Attribut "bits"
	 *
	 * @return bits oder -1, falls der Wert nicht gesetzt wurde
	 */
	public int getBits() {
		return _bits;
	}

	/**
	 * Attribut "bits"
	 *
	 * @param bits s.o.
	 *
	 * @throws IllegalArgumentException {@link #setBits(int)}
	 */
	public void setBits(String bits) throws IllegalArgumentException {
		if(!"".equals(bits)) {
			setBits(Integer.parseInt(bits));
		}
		else {
			_bits = -1;
		}
	}

	/**
	 * Attribut "bits"
	 *
	 * @param bits s.o.
	 *
	 * @throws IllegalArgumentException Die Anzahl Bits ist kleiner gleich 0 oder größer als 64
	 */
	public void setBits(int bits) throws IllegalArgumentException {
		if(bits > 0 && bits <= 64) {
			// Mehr als 0 und höchstens 64 Bits
			_bits = bits;
		}
		else {
			throw new IllegalArgumentException(
					"Für eine Ganzzahl soll eine ungültige Anzahl Bits benutzt werden (gültige Werte sind größer als 0 und kleiner gleich 64): " + bits
			);
		}
	}

	/**
	 * Array, das Objekte enthält, die vom Typ ConfigurationValueRange und vom Typ ConfigurationState sein können.
	 *
	 * @return Array mit Objekten (Typ siehe oben) oder ein leeres Array, falls keine Objekte vorhanden sind
	 */
	public ConfigurationIntegerValueRange[] getValueRangeAndState() {
		return _regionAndState;
	}

	/**
	 * @param regionAndState Array, das Objekte enthält, die vom Typ ConfigurationValueRange und vom Typ ConfigurationState sein können. Sind keine Objekte
	 *                       vorhanden, so ist das Array leer.
	 *
	 * @throws IllegalArgumentException Bei Objekten vom Typ "ConfigurationState" muss jeder Wert und jeder Name eindeutig (nur einmal vergeben) sein. Wird diese
	 *                                  Exception geworfen, wurde dagegen verstossen.
	 */
	public void setValueRangeAndState(ConfigurationIntegerValueRange[] regionAndState) {
		if(regionAndState != null) {
			_regionAndState = regionAndState;

			// Bei den Zuständen darf jeder Wert und jeder Name nur einmal vorkommen.

			final Set<String> stateNames = new HashSet<String>();
			final Set<Long> stateValues = new HashSet<Long>();

			for(ConfigurationIntegerValueRange configurationIntegerValueRange : _regionAndState) {
				if(configurationIntegerValueRange instanceof ConfigurationState) {
					final ConfigurationState configurationState = (ConfigurationState)configurationIntegerValueRange;
					if(stateNames.contains(configurationState.getName()) == true || stateValues.contains(configurationState.getValue()) == true){
						// Der Name oder/und der Wert wurde bereits vergeben -> Das ist verboten
						final StringBuffer errorText = new StringBuffer();

						// Die passende Fehlermeldung zusammenbauen
						if(stateNames.contains(configurationState.getName()) == true && stateValues.contains(configurationState.getValue()) == true){
							// Sowohl der Name als auch der Wert sind schon vergeben
							errorText.append("Sowohl der Name als auch der Wert eines Zustands wurden schon einmal vergeben: \"Name: " + configurationState.getName() + "\" \"Wert: " + configurationState.getValue() + "\"");
						}else if(stateNames.contains(configurationState.getName()) == true){
							errorText.append("Der Name des Zustands wurde schon einmal vergeben: \"Name: " + configurationState.getName() + "\" \"Wert: " + configurationState.getValue() + "\"");
						}else{
							errorText.append("Der Wert des Zustands wurde schon einmal vergeben: \"Name: " + configurationState.getName() + "\" \"Wert: " + configurationState.getValue() + "\"");
						}
						throw new IllegalArgumentException(errorText.toString());
					}
					stateNames.add(configurationState.getName());
					stateValues.add(configurationState.getValue());
				}
			}//for
		}
	}
}
