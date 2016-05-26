/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.DataModel;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Diese Klasse deserialisiert einen empfangenen Datensatz in entsprechende DataValue-Objekte.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StreamFetcher {

	private static StreamFetcher _streamFetcher;

	private StreamFetcher() {
	}

	/**
	 * Lieferte das einzige Objekt dieser Klasse zurück.
	 *
	 * @return StreamFetcher-Objekt.
	 */
	public static StreamFetcher getInstance() {
		if(_streamFetcher == null) {
			_streamFetcher = new StreamFetcher();
		}
		return _streamFetcher;
	}

	/**
	 * Deserialisiert einen Datensatz.
	 *
	 * @param dataModel Zugriff auf das Datenmodell.
	 * @param atg       Attributgruppe des Datensatzes.
	 * @param in        Stream von dem der Datensatz gelesen werden soll.
	 *
	 * @return Array mit DataValue-Objekten, des deserialisierten Datensatzes.
	 *
	 * @throws IOException Wenn beim Lesen des Streams Fehler auftreten.
	 */
	public final DataValue[] getDataValuesFromStream(DataModel dataModel, AttributeGroup atg, DataInputStream in) throws IOException {
		if((dataModel == null) || (in == null)) {
			throw new IllegalArgumentException("Argument ist null");
		}
		if(atg != null) {
			List list = atg.getAttributes();
			if(list != null) {
				ArrayList array = new ArrayList();
				int size = list.size();
				for(int i = 0; i < size; ++i) {
					Attribute attribute = (Attribute)list.get(i);
					if(attribute != null) {
						AttributeType attributeType = attribute.getAttributeType();
						if(attributeType != null) {
							boolean isArray = attribute.isArray();
							byte type = AttributeHelper.getDataValueType(attributeType, isArray);
							DataValue value = DataValue.getObject(type);
							if(value == null) {
								return null;
							}
							if(type == DataValue.ATTRIBUTE_LIST_ARRAY_TYPE) {
								DataValue _values[] = getDataValues((AttributeListDefinition)attributeType);
								if(_values == null) {
									return null;
								}
								AttributeListArrayAttribute attributeList = (AttributeListArrayAttribute)value;
								attributeList.setValue(_values);
								value.read(in);
							}
							else if(type == DataValue.ATTRIBUTE_LIST_TYPE) {
								DataValue _values[] = getDataValues((AttributeListDefinition)attributeType);
								if(_values == null) {
									return null;
								}
								AttributeListAttribute attributeList = (AttributeListAttribute)value;
								attributeList.setValue(_values);
								value.read(in);
							}
							else {
								value.read(in);
							}
							array.add(value);
						}
					}
				}
				size = array.size();
				if(size > 0) {
					DataValue values[] = new DataValue[size];
					array.toArray(values);
					return values;
				}
			}
		}
		return null;
	}

	private final DataValue[] getDataValues(AttributeListDefinition attributeListDefinition) {
		List list = attributeListDefinition.getAttributes();
		if(list != null) {
			ArrayList array = new ArrayList();
			int size = list.size();
			for(int i = 0; i < size; ++i) {
				Attribute attribute = (Attribute)list.get(i);
				if(attribute != null) {
					AttributeType attributeType = attribute.getAttributeType();
					if(attributeType != null) {
						boolean isArray = attribute.isArray();
						byte type = AttributeHelper.getDataValueType(attributeType, isArray);
						DataValue value = DataValue.getObject(type);
						if(value == null) {
							return null;
						}
						if(type == DataValue.ATTRIBUTE_LIST_ARRAY_TYPE) {
							DataValue _values[] = getDataValues((AttributeListDefinition)attributeType);
							if(_values == null) {
								return null;
							}
							AttributeListArrayAttribute attributeList = (AttributeListArrayAttribute)value;
							attributeList.setValue(_values);
							array.add(value);
						}
						else if(type == DataValue.ATTRIBUTE_LIST_TYPE) {
							DataValue _values[] = getDataValues((AttributeListDefinition)attributeType);
							if(_values == null) {
								return null;
							}
							AttributeListAttribute attributeList = (AttributeListAttribute)value;
							attributeList.setValue(_values);
							array.add(value);
						}
						else {
							array.add(value);
						}
					}
				}
			}
			size = array.size();
			if(size > 0) {
				DataValue values[] = new DataValue[size];
				array.toArray(values);
				return values;
			}
		}
		return null;
	}
}
