/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation;

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.AttributeListArrayAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.AttributeListAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TBD Beschreibung
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AttributeListValue extends AttributeBaseValue {

	/** Die Attribute dieser Liste */
	private AttributeBaseValue _attributes[];

	/** Die Länge des Arrays, wenn <code>attribute</code> ein Array ist sonst 1 */
	private int _count = 0;

	/**
	 * Erzeugt eine neues Objekt mit gegebenen Parametern.
	 *
	 * @param datamodel     Datenmodell
	 * @param attributeList Feld mit Attributen
	 */
	public AttributeListValue(DataModel datamodel, Attribute attributeList) {
		super(datamodel, attributeList);
		try {
			if(_attribute.isArray()) {
				if(!_attribute.isCountVariable()) {
					_count = _attribute.getMaxCount();
				}
				else {
					_count = 0;
				}
			}
			else {
				_count = 1;
			}
		}
		catch(ConfigurationException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gibt die Länge des Feldes mit den Attributen zurück.
	 *
	 * @return Länge des Feldes
	 */
	public final synchronized int getElementsCount() {
		return _count;
	}

	/**
	 * Diese Methode setzt die Größe des Feldes mit den Attributen.
	 *
	 * @param count Länge des Feldes
	 *
	 * @throws ConfigurationException, wenn Fehler bei Konfigurationsänderungen auftreten
	 */
	public final synchronized void setElementsCount(int count) throws ConfigurationException {
		if(_attribute.isArray() && _attribute.isCountVariable() && (count > -1)) {
			int oldCount = _count;
			_count = count;
			if(_attributes == null) return;
			AttributeListDefinition attributeListDefinition = (AttributeListDefinition)_attribute.getAttributeType();
			AttributeBaseValue[] oldAttributes = _attributes;
			int saveItemCount = oldAttributes.length;
			int newItemCount = count * attributeListDefinition.getAttributes().size();
			if(newItemCount < saveItemCount) saveItemCount = newItemCount;
			AttributeBaseValue[] newAttributes = new AttributeBaseValue[newItemCount];
			int itemIndex;
			for(itemIndex = 0; itemIndex < saveItemCount; ++itemIndex) {
				newAttributes[itemIndex] = oldAttributes[itemIndex];
			}
			for(int i = oldCount; i < _count; ++i) {
				List list = AttributeHelper.getAttributesValues(attributeListDefinition);
//				List list = attributeListDefinition.getAttributesValues();
				if(list != null) {
					for(int j = 0; j < list.size(); ++j) {
						AttributeBaseValue attributeBaseValue = (AttributeBaseValue)list.get(j);
						if(attributeBaseValue instanceof AttributeListValue) {
							AttributeListValue attributeListValue = (AttributeListValue)attributeBaseValue;
							attributeListValue.getAttributeBaseValues();
						}
						newAttributes[itemIndex++] = attributeBaseValue;
					}
				}
			}
			_attributes = newAttributes;
		}
	}

	/**
	 * Diese Methode gibt ein Feld mit den Basiswerten der Atrribute zurück.
	 *
	 * @return Feld mit den Basiswerten der Atrribute
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException , wenn Fehler bei Konfigurationsänderungen auftreten
	 */
	public final synchronized AttributeBaseValue[] getAttributeBaseValues() throws ConfigurationException {
		if(_attributes == null) {
			AttributeListDefinition attributeListDefinition = (AttributeListDefinition)_attribute.getAttributeType();
			if(attributeListDefinition != null) {
				if(_attribute.isArray()) {
					ArrayList tmp = new ArrayList();
					for(int i = 0; i < _count; ++i) {
						List list = AttributeHelper.getAttributesValues(attributeListDefinition);
//						List list = attributeListDefinition.getAttributesValues();
						if(list != null) {
							for(int j = 0; j < list.size(); ++j) {
								AttributeBaseValue attributeBaseValue = (AttributeBaseValue)list.get(j);
								if(attributeBaseValue instanceof AttributeListValue) {
									AttributeListValue attributeListValue = (AttributeListValue)attributeBaseValue;
									attributeListValue.getAttributeBaseValues();
								}
								tmp.add(attributeBaseValue);
							}
						}
					}
					_attributes = new AttributeBaseValue[tmp.size()];
					tmp.toArray(_attributes);
				}
				else {
					List attributeBaseValueList = AttributeHelper.getAttributesValues(attributeListDefinition);
//					List attributeBaseValueList = attributeListDefinition.getAttributesValues();
					if(attributeBaseValueList != null) {
						_attributes = new AttributeBaseValue[attributeBaseValueList.size()];
						for(int i = 0; i < _attributes.length; ++i) {
							_attributes[i] = (AttributeBaseValue)attributeBaseValueList.get(i);
							if(_attributes[i] instanceof AttributeListValue) {
								AttributeListValue attributeListValue = (AttributeListValue)_attributes[i];
								attributeListValue.getAttributeBaseValues();
							}
						}
					}
				}
			}
		}
		return _attributes;
	}

	public final synchronized Object getValue() {
		return _attributes == null ? new AttributeBaseValue[0] : _attributes;
	}


	public final synchronized void setValue(DataValue _value) {
		if(_value == null) {
			throw new IllegalArgumentException("Argument ist null");
		}
		if(_value instanceof AttributeListAttribute) {
			if(_attribute.isArray()) {
				throw new IllegalArgumentException("Kein zulässiger Datensatz für ein Attributlistenfeld");
			}
			if(_attributes == null) {
				getAttributeBaseValues();
			}
			if(_attributes != null) {
				DataValue values[] = (DataValue[])_value.getValue();
				if(values != null) {
					for(int i = 0; i < _attributes.length; ++i) {
						_attributes[i].setValue(values[i]);
					}
				}
			}
		}
		else if(_value instanceof AttributeListArrayAttribute) {
			if(!_attribute.isArray()) {
				throw new IllegalArgumentException("Kein zulässiger Datensatz für eine Attributliste");
			}
			if(_attribute.isCountLimited()) {
				AttributeListAttribute attributeValues[] = (AttributeListAttribute[])_value.getValue();
				if(attributeValues != null) {
					if(_count != attributeValues.length) {
						if(_attribute.isCountVariable() && attributeValues.length <= _attribute.getMaxCount()) {
							setElementsCount(attributeValues.length);
						}
						else {
							throw new IllegalArgumentException(
									"Inkonsistenter Datensatz: Anzahl Elemente im Attribut " + _attribute.getNameOrPidOrId() + " ist " + attributeValues.length
									+ " sollte aber " + _attribute.getMaxCount() + " sein"
							);
						}
					}
					_attributes = null;
					getAttributeBaseValues();
					if(_attributes != null) {
						ArrayList dataValues = new ArrayList();
						for(int i = 0; i < _count; ++i) {
							DataValue values[] = (DataValue[])attributeValues[i].getValue();
							for(int j = 0; j < values.length; ++j) {
								dataValues.add(values[j]);
							}
						}
						for(int i = 0; i < _attributes.length; ++i) {
							_attributes[i].setValue((DataValue)dataValues.get(i));
						}
					}
				}
			}
			else {
				AttributeListAttribute attributeValues[] = (AttributeListAttribute[])_value.getValue();
				if(attributeValues != null) {
					_count = attributeValues.length;
					_attributes = null;
					getAttributeBaseValues();
					if(_attributes != null) {
						ArrayList dataValues = new ArrayList();
						for(int i = 0; i < _count; ++i) {
							DataValue values[] = (DataValue[])attributeValues[i].getValue();
							for(int j = 0; j < values.length; ++j) {
								dataValues.add(values[j]);
							}
						}
						for(int i = 0; i < _attributes.length; ++i) {
							_attributes[i].setValue((DataValue)dataValues.get(i));
						}
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("Kein zulässiger Datensatz für eine Attributliste");
		}
	}

	public final synchronized void writeValue(DataOutputStream out) throws IOException {
		try {
			if(_attribute.isArray()) {
				out.writeInt(_count);
			}
		}
		catch(ConfigurationException ex) {
			throw new IOException(ex.getMessage());
		}
		if(_attributes == null) {
			try {
				if(_attribute.isArray() && _count == 0) return;
			}
			catch(ConfigurationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			throw new IOException("Wert = null: Attributname: " + _attribute.getName() + " count: " + _count);
		}
		for(int i = 0; i < _attributes.length; ++i) {
			if(_attributes[i] != null) {
				_attributes[i].writeValue(out);
			}
		}
	}


	public final synchronized boolean equals(AttributeBaseValue attributeBaseValue) {
		if(attributeBaseValue == null) {
			return false;
		}
		if(_attribute.getId() != attributeBaseValue.getAttribute().getId()) {
			return false;
		}
		if(!(attributeBaseValue instanceof AttributeListValue)) {
			return false;
		}
		AttributeBaseValue _attributes[] = (AttributeBaseValue[])attributeBaseValue.getValue();
		if(this._attributes == null) {
			return (_attributes == null || _attributes.length == 0);
		}
		else {
			if(_attributes == null) {
				return false;
			}

			if(this._attributes.length != _attributes.length) return false;

			for(int i = 0; i < this._attributes.length; ++i) {
				if(this._attributes[i] != null) {
					if(!this._attributes[i].equals(_attributes[i])) {
						return false;
					}
				}
			}
			return true;
		}
	}

	/**
	 * Gibt den HashKode zurück
	 *
	 * @return Hashcode
	 */
	public final int hashCode() {
		if(_hashCode == 0) {
			long objectID = _attribute.getId();
			int result = 19;
			result = (41 * result) + (int)(objectID ^ (objectID >>> 32));
			_hashCode = result;
		}
		return _hashCode;
	}


	public final AttributeBaseValue clonePlain() {
		return new AttributeListValue(_dataModel, _attribute);
	}


	public final synchronized AttributeBaseValue cloneObject() {
		if(_attributes == null) {
			try {
				getAttributeBaseValues();
			}
			catch(ConfigurationException ex) {
				ex.printStackTrace();
			}
		}
		AttributeBaseValue _attributes[] = null;
		if(this._attributes != null) {
			_attributes = new AttributeBaseValue[this._attributes.length];
			for(int i = 0; i < this._attributes.length; ++i) {
				_attributes[i] = this._attributes[i].cloneObject();
			}
		}
		AttributeListValue clone = new AttributeListValue(_dataModel, _attribute);
		clone._attributes = _attributes;
		clone._count = _count;
		return clone;
	}


	public final synchronized boolean hasValue() {
		if(_attributes == null) {
			return false;
		}
		for(int i = 0; i < _attributes.length; ++i) {
			if(_attributes[i] instanceof AttributeValue) {
				if(!_attributes[i].hasValue()) {
					return false;
				}
			}
		}
		return true;
	}
}
