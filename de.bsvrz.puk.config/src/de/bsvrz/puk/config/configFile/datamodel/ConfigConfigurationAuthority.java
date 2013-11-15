/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;

/**
 * Implementierung des Interfaces {@link ConfigurationAuthority} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
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
}
