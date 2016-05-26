/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

import java.text.NumberFormat;

/**
 * Die Klasse spiegelt einen "bereich" nach K2S.DTD wieder
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigurationValueRange implements ConfigurationIntegerValueRange {
	/**
	 * Hiermit kann ein Double-Wert in Standard-Notation ausgegeben werden. Aus 1.0E-5 wird 0,00001.
	 */
	private final static NumberFormat _doubleNumberFormat = NumberFormat.getNumberInstance();

	static {
		_doubleNumberFormat.setMinimumIntegerDigits(1);
		_doubleNumberFormat.setMaximumIntegerDigits(999);
		_doubleNumberFormat.setMinimumFractionDigits(0);
		_doubleNumberFormat.setMaximumFractionDigits(999);
		_doubleNumberFormat.setGroupingUsed(false);
	}

	SystemObjectInfo _info = null;

	private double _scale = 1.0;
	private long _minimum;
	private long _maximum;
	private String _unit = "";

	/**
	 * Attribut "info"
	 *
	 * @param info s.o.
	 */
	public void setInfo(SystemObjectInfo info) {
		_info = info;
	}

	/**
	 * Attribut "skalierung"
	 *
	 * @param scale String, der die Skalierung enthält. Ist in dem String "," enthalten, wird dies durch einen "."
	 *              ersetzt.
	 */
	public void setScale(String scale) {
		if (!"".equals(scale)) {
			scale = scale.replace(',', '.');
			_scale = Double.parseDouble(scale);
		}
	}

	/**
	 * Attribut "skalierung"
	 * @param scale s.o.
	 */
	public void setScale(double scale) {
		_scale = scale;
	}

	/**
	 * Attribut "minimum"
	 * @param minimum String, der als Long interpretiert wird
	 */
	public void setMinimum(String minimum) {
		if (!"".equals(minimum)) {
			_minimum = Long.parseLong(minimum);
		}
	}

	/**
	 * Attribut "maximum"
	 * @param maximum String, der als Long interpretiert wird
	 */
	public void setMaximum(String maximum) {
		if (!"".equals(maximum)) {
			_maximum = Long.parseLong(maximum);
		}
	}

	/**
	 * Attribut "minimum"
	 * @param minimum s.o.
	 */
	public void setMinimum(long minimum) {
		_minimum = minimum;
	}

	/**
	 * Attribut "maximum"
	 * @param maximum s.o.
	 */
	public void setMaximum(long maximum) {
		_maximum = maximum;
	}

	/**
	 * Paramter "einheit"
	 * @param unit s.o.
	 */
	public void setUnit(String unit) {
		_unit = unit;
	}

	/**
	 * Parameter "info"
	 * @return info, die zu diesem Objekt gehört
	 */
	public SystemObjectInfo getInfo() {
		return _info;
	}

	/**
	 * skalierung
	 *
	 * @return Wert des Attributes skalierung, wurde kein Wert gesetzt wird 1.0 zurückgegeben
	 */
	public double getScale() {
		return _scale;
	}

	/**
	 * Gibt die Skalierung in Standard-Notation zurück.
	 *
	 * @return Skalierung in Standard-Notation.
	 */
	public String getScaleAsString() {
		return _doubleNumberFormat.format(_scale);
	}


	public long getMinimum() {
		return _minimum;
	}

	public long getMaximum() {
		return _maximum;
	}

	/**
	 * einheit
	 *
	 * @return Wert des Attributes einheit. Wurde kein Wert gesetzt, wird "" zurückgegeben
	 */
	public String getUnit() {
		return _unit;
	}
}
