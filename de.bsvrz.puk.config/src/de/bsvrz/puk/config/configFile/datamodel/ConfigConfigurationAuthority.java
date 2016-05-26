/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

/**
 * Implementierung des Interfaces {@link ConfigurationAuthority} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigConfigurationAuthority extends ConfigConfigurationObject implements ConfigurationAuthority {

	/**
	 * Konstruktor eines Konfigurationsverantwortlichen.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Konfigurationsverantwortlichen
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigConfigurationAuthority(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public short getCoding() {
		final Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsVerantwortlicherEigenschaften"));
		if(data != null) {
			return data.getUnscaledValue("kodierung").shortValue();
		}
		else {
			throw new IllegalStateException("Die Kodierung des Konfigurationsbereichs " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
	}

	@Override
	public ConfigurationArea getDefaultConfigurationArea(){
		final Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsVerantwortlicherEigenschaften"));
		Data.TextArray defaultConfigAreaArray = data.getTextArray(
				"defaultBereich"
		);
		if(defaultConfigAreaArray.getLength() != 1) {
			return null;
		}
		final String defaultConfigAreaPid = defaultConfigAreaArray.getTextValue(0).getValueText();
		SystemObject object = getDataModel().getObject(defaultConfigAreaPid);
		if(object instanceof ConfigurationArea) {
			return (ConfigurationArea) object;
		}
		return null;
	}
}
