/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectCollection;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

/**
 * Implementierung des Interfaces {@link ObjectSet} und dem Interface {@link de.bsvrz.dav.daf.main.config.SystemObjectCollection} auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class ConfigObjectSet extends ConfigConfigurationObject implements ObjectSet, SystemObjectCollection {

	/**
	 * Konstruktor einer Menge.
	 *
	 * @param configurationArea Konfigurationsbereich dieser Menge
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigObjectSet(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public ObjectSetType getObjectSetType() {
		SystemObjectType type = getType();
		if(type instanceof ObjectSetType) {
			return (ObjectSetType)type;
		}
		else {
			throw new IllegalStateException("Fehlerhafter Mengentyp bei " + getNameOrPidOrId());
		}
	}

	public void add(SystemObject object) throws ConfigurationChangeException {
		add(new SystemObject[]{object});
	}

	public void remove(SystemObject object) throws ConfigurationChangeException {
		remove(new SystemObject[]{object});
	}
}
