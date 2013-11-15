/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation;

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * TBD
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public abstract class AttributeBaseValue {

	/** Der Hashcode */
	protected int _hashCode = 0;

	/** Das Attribut */
	protected Attribute _attribute;

	/** Das DataModel */
	protected DataModel _dataModel;

	public AttributeBaseValue(DataModel dataModel, Attribute attribute) {
		_attribute = attribute;
		_dataModel = dataModel;
	}

	/**
	 * Gibt den Namen des Attributs zur�ck.
	 *
	 * @return der Name des Attributs
	 */
	public final String getName() {
		return _attribute.getName();
	}

	/**
	 * Gibt das Attribut zur�ck.
	 *
	 * @return das Attribut
	 */
	public final Attribute getAttribute() {
		return _attribute;
	}

	/**
	 * Gibt den Wert des Attributs zur�ck.
	 *
	 * @return der Wert des Attributs
	 */
	public abstract Object getValue();

	/**
	 * Setzt den Wert dieses Attributs.
	 *
	 * @param value neuer Wert des Attributs
	 */
	public abstract void setValue(DataValue value);

	/**
	 * Schreibt den Wert dieses Attributs in einen Ausgabestrom
	 *
	 * @param out Ausgabestrom
	 *
	 * @throws IOException Falls der Schreibvorgang nicht durchgef�hrt werden konnte.
	 */
	public abstract void writeValue(DataOutputStream out) throws IOException;

	/** �berpr�fft auf gleichheit dieses Attribute und den gegebenen Attributeswert */
	/**
	 * �berpr�ft, ob das Attribut/Attributwert mit dem angegebenen Attribut �bereinstimmt.
	 *
	 * @param attributeBaseValue zu vergleichendes Attribut
	 *
	 * @return <code>true</code>, wenn die Attribute gleich sind, sonst <code>false</code>
	 */
	public abstract boolean equals(AttributeBaseValue attributeBaseValue);

	/**
	 * Erzeugt eine Kopie dieses Attributs, allerdings ohne Wert. (nur die Beschreibung)
	 *
	 * @return Kopie dieses Attributs
	 */
	public abstract AttributeBaseValue clonePlain();

	/**
	 * Erzeugt eine Kopie dieses Attributs.
	 *
	 * @return Kopie dieses Attributs
	 */
	public abstract AttributeBaseValue cloneObject();

	/**
	 * Ermittelt, ob dieses Attribut einen Wert besitzt.
	 *
	 * @return <code>true</code>, wenn das Attribut einen Wert besitzt, sonst <code>false</code>
	 */
	public abstract boolean hasValue();
}
