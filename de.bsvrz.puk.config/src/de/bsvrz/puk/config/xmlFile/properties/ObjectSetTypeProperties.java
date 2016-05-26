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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.ReferenceType;

/**
 * Diese Klasse stellt eine mengenDefinition dar, die in der K2S.DTD definiert wird.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ObjectSetTypeProperties extends ConfigurationObjectProperties {

	private final String[] _elements;
	private boolean _mutable = false;
	private int _minimum = 0;
	private int _maximum = 0;

	/**
	 * Referenzierungsart
	 */
	private ReferenceType _referenceType = null;

	/**
	 * @param name
	 * @param pid
	 * @param id
	 * @param typePid
	 * @param info
	 * @param elements Werte des Attributs "elemente". Jeder Eintrag im Array wird dem Attribut "elemente" zugewiesen
	 */
	public ObjectSetTypeProperties(String name, String pid, long id, String typePid, SystemObjectInfo info, String[] elements) {
		super(name, pid, id, typePid, info);
		_elements = elements;
	}

	/**
	 * @param name
	 * @param pid
	 * @param id
	 * @param typePid
	 * @param info
	 * @param elements String der alle Werte des Attributs "elemente" enthält. Die einzelnen Einträge sind mit einem
	 *                 Leerzeichen getrennt.
	 */
	public ObjectSetTypeProperties(String name, String pid, long id, String typePid, SystemObjectInfo info, String elements) {
		super(name, pid, id, typePid, info);

		_elements = elements.split(" ");
	}

	/**
	 * Attribut "aenderbar"
	 *
	 * @return true = ja
	 */
	public boolean isMutable() {
		return _mutable;
	}

	/**
	 * Attribut "aenderbar"
	 *
	 * @param mutable true = ja
	 */
	public void setMutable(boolean mutable) {
		_mutable = mutable;
	}

	/**
	 * Attribut "referenzierungsart"
	 *
	 * @return Assoziation, Aggregation, Komposition oder <code>null</code>, falls der Wert nicht gesetzt wurde.
	 */
	public ReferenceType getReferenceType() {
		return _referenceType;
	}

	/**
	 * Attribut "referenzierungsart"
	 *
	 * @param referenceType s.o.
	 */
	public void setReferenceType(ReferenceType referenceType) {
		_referenceType = referenceType;
	}

	/**
	 * Setzt das Attribut referenzierungsart mit einem String.
	 *
	 * @param referenceType "assoziation", "aggregation", "komposition" und "" falls der Wert unbekannt ist
	 */
	public void setReferenceType(String referenceType) {
		if ("assoziation".equals(referenceType)) {
			_referenceType = ReferenceType.ASSOCIATION;
		} else if ("aggregation".equals(referenceType)) {
			_referenceType = ReferenceType.AGGREGATION;
		} else if ("komposition".equals(referenceType)) {
			_referenceType = ReferenceType.COMPOSITION;
		} else if ("".equals(referenceType)) {
			// Es muss nichts gemacht werden, die Variable ist mit null initialisiert
		} else {
			// Fehler
			throw new IllegalArgumentException("Die Referenzierungsart kann nicht festgelegt werden, unbekannter Parameter: " + referenceType);
		}
	}

	/**
	 * Änderbar ja/nein
	 *
	 * @param mutable "ja" oder "nein", null wird als "nein" interpretiert
	 */
	public void setMutable(String mutable) {
		if (mutable != null) {
			if (mutable.equals("ja")) {
				_mutable = true;
			} else if (mutable.equals("nein")) {
				_mutable = false;
			} else {
				throw new IllegalArgumentException("Unbkannter Paramter: " + mutable);
			}
		}
	}

	/**
	 * Attribut "mindestens"
	 *
	 * @param minimum String, der als Integer interpretiert wird
	 */
	public void setMinimum(String minimum) {
		if (!"".equals(minimum)) {
			_minimum = Integer.parseInt(minimum);
		}
	}

	/**
	 * Attribut "hoechstens"
	 *
	 * @param maximum String, der als Integer interpretiert wird
	 */
	public void setMaximum(String maximum) {
		if (!"".equals(maximum)) {
			_maximum = Integer.parseInt(maximum);
		}
	}

	/**
	 * Werte des Attributes "elemente"
	 *
	 * @return s.o.
	 */
	public String[] getElements() {
		return _elements;
	}

	/**
	 * Attribut "mindestens"
	 *
	 * @param minimum s.o
	 */
	public void setMinimum(int minimum) {
		_minimum = minimum;
	}

	/**
	 * Attribut "hoechstens"
	 * @param maximum s.o.
	 */
	public void setMaximum(int maximum) {
		_maximum = maximum;
	}

	/**
	 * Änderbar ja/nein
	 *
	 * @return ja = true; nein = false
	 */
	public boolean getMutable() {
		return _mutable;
	}

	/**
	 * Attribut "mindestens"
	 * @return Wert des Attributs oder 0, falls kein Wert gesetzt wurde
	 */
	public int getMinimum() {
		return _minimum;
	}

	/**
	 * Attribut "hoechstens"
	 * @return Wert des Attributs oder 0, falls kein Wert gesetzt wurde
	 */
	public int getMaximum() {
		return _maximum;
	}
}
