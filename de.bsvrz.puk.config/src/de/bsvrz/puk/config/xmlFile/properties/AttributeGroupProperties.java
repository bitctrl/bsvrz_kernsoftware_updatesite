/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

/**
 * Dieses Objekt spiegelt eine Attributgruppendefinition wieder, die in der K2S.DTD definiert ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AttributeGroupProperties extends ConfigurationObjectProperties {

	private boolean _configuring = false;

	private boolean _isParameter = false;

	/** Speichert alle Aspekte, Größe 0 bedeutet, das es keine Aspekte gibt */
	private ConfigurationAspect _configurationAspect[] = new ConfigurationAspect[0];

	/** Speichert alle Attribute und Attributlisten in der Reihe ihres auftretens. */
	private AttributeProperties _attributeAndAttributeList[] = new AttributeProperties[0];

	public AttributeGroupProperties(String name, String pid, long id, String typePid, SystemObjectInfo info) {
		super(name, pid, id, typePid, info);
	}

	/** @param configurationAspect Aspekte oder ein leeres Array, falls keine Aspekte vorhanden sind */
	public void setConfigurationAspect(ConfigurationAspect[] configurationAspect) {
		_configurationAspect = configurationAspect;
	}

	/**
	 * Alle Aspekte des Objekts
	 *
	 * @return Array mit Aspekten oder ein leeres Array, falls keine Aspekte vorhanden sind
	 */
	public ConfigurationAspect[] getConfigurationAspect() {
		return _configurationAspect;
	}

	/**
	 * Konfigurierend ja/nein
	 *
	 * @param configuring "ja" oder "nein", "" wird als "nein" interpretiert
	 */
	public void setConfiguring(String configuring) {
		if(!"".equals(configuring)) {
			if(configuring.equals("ja")) {
				_configuring = true;
			}
			else if(configuring.equals("nein")) {
				_configuring = false;
			}
			else {
				throw new IllegalArgumentException("Unbkannter Paramter: " + configuring);
			}
		}
	}

	/**
	 * Konfigurierend ja/nein
	 *
	 * @param configuring true = "ja" oder false = "nein"
	 */
	public void setConfiguring(boolean configuring) {
		_configuring = configuring;
	}

	/**
	 * parametrierend ja/nein
	 *
	 * @param isParameter
	 */
	public void setParameter(String isParameter) {
		if(!"".equals(isParameter)) {
			if(isParameter.equals("ja")) {
				_isParameter = true;
			}
			else if(isParameter.equals("nein")) {
				_isParameter = false;
			}
			else {
				throw new IllegalArgumentException("Ungültiger Wert für XML-Attribut 'parametrierend' einer Attributgruppendefinition: " + isParameter);
			}
		}
	}

	/**
	 * parametrierend ja/nein
	 *
	 * @param isParameter
	 */
	public void setParameter(boolean isParameter) {
		_isParameter = isParameter;
	}

	/**
	 * Konfigurierend ja/nein
	 *
	 * @return ja = true; nein = false
	 */
	public boolean getConfiguring() {
		return _configuring;
	}

	/**
	 * Parametrierend ja/nein
	 *
	 * @return ja = true; false = nein
	 */
	public boolean isParameter() {
		return _isParameter;
	}

	/**
	 * Array, das alle Attribute und Attributlisten enthält. In dem Array sind Objekte vom Typ PlainAttributeProperties und ListAttributeProperties gespeichert.
	 * Das erste Element, das aus der XML Datei eingelesen wurde, steht an Position [0]. Wurden keine Elemente eingelesen, ist das Array leer.
	 *
	 * @return s.o.
	 */
	public AttributeProperties[] getAttributeAndAttributeList() {
		return _attributeAndAttributeList;
	}

	/**
	 * @param attributeAndAttributeList Array, das alle Attribute und Attributlisten enthält. In dem Array sind Objekte vom Typ PlainAttributeProperties und
	 *                                  ListAttributeProperties gespeichert. Die Reihenfolge der Elemente bleibt beim speichern erhalten.
	 */
	public void setAttributeAndAttributeList(AttributeProperties[] attributeAndAttributeList) {
		if(attributeAndAttributeList != null) {
			_attributeAndAttributeList = attributeAndAttributeList;
		}
	}
}
