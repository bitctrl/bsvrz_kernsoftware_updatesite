/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Klasse, die den Zugriff auf Attributlistendefinitionen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafAttributeListDefinition extends DafAttributeType implements AttributeListDefinition {

	/** Liste der Attribute dieses Objektes */
	private List<Attribute> _attributes;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafAttributeListDefinition(DafDataModel dataModel) {
		super(dataModel);
		_internType = ATTRIBUTE_LIST_TYPE;
		_dataValueType = DataValue.ATTRIBUTE_LIST_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafAttributeListDefinition(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long setIds[]
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, null
		);
		_internType = ATTRIBUTE_LIST_TYPE;
		_dataValueType = DataValue.ATTRIBUTE_LIST_TYPE;
	}

	public String parseToString() {
		String str = super.parseToString();
		str += "Attribute: \n";
		if(_attributes == null) {
			getAttributes();
		}
		if(_attributes != null) {
			for(int i = 0; i < _attributes.size(); ++i) {
				str += ((DafAttribute)_attributes.get(i)).parseToString();
			}
		}
		return str;
	}

	public final List<Attribute> getAttributes() {
		if((_attributes == null) || (_attributes.size() == 0)) {
			final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			ObjectSet attributesSet = getObjectSet("Attribute");

			// Die Attribute müssen nach ihrem Index sortiert zurückgegeben werden
			final List tmp = attributesSet.getElements();

			// Liefert die Sortierung
			final TreeMap<Integer, Attribute> attributeMap = new TreeMap<Integer, Attribute>();

			// Alle Attribute durchlaufen und in der TreeMap speichern
			for(Object o : tmp) {
				final Attribute attribute = (Attribute)o;
				attributeMap.put(attribute.getPosition(), attribute);
			}
			attributes.addAll(attributeMap.values());
			_attributes = Collections.unmodifiableList(attributes);
		}
		return _attributes;
	}

	public final Attribute getAttribute(String attributeName) {
		final List<Attribute> attributes = getAttributes();
		for(Attribute attribute : attributes) {
			if(attributeName.equals(attribute.getName())) {
				return attribute;
			}
		}
		return null;
	}
}
