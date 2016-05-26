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

import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;

/**
 * Implementierung des Interfaces {@link ObjectSetUse} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigObjectSetUse extends ConfigConfigurationObject implements ObjectSetUse {
	/**
	 * Konstruktor einer Mengenverwendung.
	 *
	 * @param configurationArea Konfigurationsbereich dieser Mengenverwendung
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigObjectSetUse(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public String getObjectSetName() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.mengenVerwendungsEigenschaften"));
		if (data != null) {
			return data.getTextValue("mengenName").getText();
		} else {
			throw new IllegalStateException("Der Name einer Menge dieser Verwendung '" + getPid() + "' konnte nicht ermittelt werden.");
		}
	}

	public ObjectSetType getObjectSetType() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.mengenVerwendungsEigenschaften"));
		if (data != null) {
			return (ObjectSetType) data.getReferenceValue("mengenTyp").getSystemObject();
		} else {
			throw new IllegalStateException("Der Mengen-Typ, einer Menge dieser Verwendung '" + getPid() + "' konnte nicht ermittelt werden.");
		}
	}

	public boolean isRequired() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.mengenVerwendungsEigenschaften"));
		if (data != null) {
			return data.getUnscaledValue("erforderlich").intValue() == 1;
		} else {
			throw new IllegalStateException("Es konnte nicht ermittelt werden, ob die Verwendung '" + getPid() + "' der Menge erforderlich ist.");
		}
	}
}
