/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
 * Bildet einen Zeitstempel ab, der in in der K2S.DTD definiert ist
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationTimeStamp implements ConfigurationAttributeType {
	boolean _relative = false;
	AccuracyTimeStamp _accuracy = AccuracyTimeStamp.MILLISECONDS;

	/**
	 * Attribut "relative"
	 * @param relative ja/nein
	 */
	public void setRelative(String relative) {
		if ("ja".equals(relative)) {
			_relative = true;
		} else if ("nein".equals(relative)) {
			_relative = false;
		}
	}

	/**
	 * Attribut "relativ"
	 * @param relative true = ja
	 */
	public void setRelative(boolean relative) {
		_relative = relative;
	}

	/**
	 * Attribut "genauigkeit"
	 * @param accuracy sekunden/millisekunden
	 */
	public void setAccuracy(String accuracy) {
		if (AccuracyTimeStamp.SECONDS.getValue().equals(accuracy)) {
			_accuracy = AccuracyTimeStamp.SECONDS;
		} else if (AccuracyTimeStamp.MILLISECONDS.getValue().equals(accuracy)) {
			_accuracy = AccuracyTimeStamp.MILLISECONDS;
		}
	}

	/**
	 * Attribut "genauigkeit"
	 * @param accuracy s.o.
	 */
	public void setAccuracy(AccuracyTimeStamp accuracy) {
		_accuracy = accuracy;
	}

	/**
	 *
	 * @return Wert des Attributs "relativ"
	 */
	public boolean getRelative() {
		return _relative;
	}

	/**
	 *
	 * @return Wert des Attributs "genauigkeit", wurde kein Wert gesetzt wird "millisekunde" zurückgegeben
	 */
	public AccuracyTimeStamp getAccuracy() {
		return _accuracy;
	}
}
