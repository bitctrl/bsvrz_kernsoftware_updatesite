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

import de.bsvrz.dav.daf.main.config.ReferenceType;

/**
 * Diese Klasse stellt eine objektReferenz nach der K2S.DTD da.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5091 $ / $Date: 2007-09-03 15:31:49 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigurationObjectReference implements ConfigurationAttributeType {
	private String _referenceObjectType = "";

	/**
	 * Attribut "undefiniert".
	 */
	private UndefinedReferenceOptions _undefined = UndefinedReferenceOptions.FORBIDDEN;

	/**
	 * Referenzierungsart
	 */
	private ReferenceType _referenceType = ReferenceType.ASSOCIATION;

	/**
	 * Gibt den Wert des Attributes referenzierungsart zurück
	 *
	 * @return Referenzierungsart, falls nicht gesetzt, wird Assoziation zurückgegeben.
	 */
	public ReferenceType getReferenceType() {
		return _referenceType;
	}

	/**
	 * Attribut "referenzierungsart"
	 *
	 * @param referenceType referenzierungsart
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
		if (ReferenceType.ASSOCIATION.getValue().equals(referenceType)) {
			_referenceType = ReferenceType.ASSOCIATION;
		} else if (ReferenceType.AGGREGATION.getValue().equals(referenceType)) {
			_referenceType = ReferenceType.AGGREGATION;
		} else if (ReferenceType.COMPOSITION.getValue().equals(referenceType)) {
			_referenceType = ReferenceType.COMPOSITION;
		} else if ("".equals(referenceType)) {
			// Es muss nichts gemacht werden, die Variable ist mit "" initialisiert
		} else {
			// Fehler
			throw new IllegalArgumentException("Die Referenzierungsart kann nicht festgelegt werden, unbekannter Parameter: " + referenceType);
		}
	}

	/**
	 * Entspricht dem Attribut "undefiniert"
	 *
	 * @return Objekt, das den gesetzen Wert wiederspiegelt oder <code>null</code> falls der Wert nicht gesetzt wurde
	 */
	public UndefinedReferenceOptions getUndefined() {
		return _undefined;
	}

	/**
	 * Setzt das Attribut "undefiniert"
	 *
	 * @param undefinedReferenceOptions erlaubt/verboten oder "" falls der Wert nicht gesetzt werden soll.
	 */
	public void setUndefinedReferences(String undefinedReferenceOptions) {

		if (UndefinedReferenceOptions.ALLOWED.getValue().equals(undefinedReferenceOptions)) {
			_undefined = UndefinedReferenceOptions.ALLOWED;
		} else if (UndefinedReferenceOptions.FORBIDDEN.getValue().equals(undefinedReferenceOptions)) {
			_undefined = UndefinedReferenceOptions.FORBIDDEN;
		}
	}

	/**
	 * Setzt das Attribut "undefiniert"
	 *
	 * @param undefinedReferenceOptions erlaubt/verboten
	 */
	public void setUndefinedReferences(UndefinedReferenceOptions undefinedReferenceOptions) {
		_undefined = undefinedReferenceOptions;
	}

	/**
	 * Attribut "referenzierungsart"
	 * @return referenzierungsart oder "" falls der Wert nicht gesetzt wurde
	 */
	public String getReferenceObjectType() {
		return _referenceObjectType;
	}

	/**
	 * Typ der Objekte, die von Attributen dieses Typs referenziert werden können
	 * @param typePid type oder "", falls der Type nicht gesetzt wurde
	 */
	public void setReferenceObjectType(String typePid) {
		_referenceObjectType = typePid;
	}
}
