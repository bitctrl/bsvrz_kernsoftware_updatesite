/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.SystemObjectInfo;

/**
 * Klasse, die ein konfigurationsObjekt aus der K2S.DTD abbildet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5091 $
 */
public class ConfigurationConfigurationObject extends ConfigurationObjectProperties {

	/** "datensatz" und "objektMenge" Objekte */
	private ConfigurationObjectElements[] _datasetAndObjectSet = new ConfigurationObjectElements[0];

	/** "defaultParameter"-Objekte */
	private ConfigurationDefaultParameter[] _defaultParameters = new ConfigurationDefaultParameter[0];

	public ConfigurationConfigurationObject(String name, String pid, long id, String typePid, SystemObjectInfo info) {
		super(name, pid, id, typePid, info);
	}

	/** @param datasetAndObjectSet Elemente vom Typ datensatz und objektMenge (siehe K2S.DTD) */
	public void setDatasetAndObjectSet(ConfigurationObjectElements[] datasetAndObjectSet) {
		if(datasetAndObjectSet != null) {
			_datasetAndObjectSet = datasetAndObjectSet;
		}
	}

	/**
	 * Diese Methode gibt Elemente vom Typ datensatz und objektMenge (siehe K2S.DTD) zurück.
	 *
	 * @return Objekte vom Typ ConfigurationObjectSet und ConfigurationDataset
	 */
	public ConfigurationObjectElements[] getDatasetAndObjectSet() {
		return _datasetAndObjectSet;
	}

	/**
	 * Diese Methode gibt die Default-Parameter dieses Objekts zurück.
	 *
	 * @return die Default-Parameter dieses Objekts
	 */
	public ConfigurationDefaultParameter[] getDefaultParameters() {
		return _defaultParameters;
	}

	/**
	 * Setzt die Default-Parameter dieses Objekts.
	 *
	 * @param defaultParameters die Default-Parameter dieses Objekts
	 */
	public void setDefaultParameters(final ConfigurationDefaultParameter[] defaultParameters) {
		_defaultParameters = defaultParameters;
	}
}
