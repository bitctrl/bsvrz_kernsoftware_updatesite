/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.SystemObject;

/** @author fouad */

/**
 * Klasse, die den Zugriff auf Konfigurationsverantwortliche seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafConfigurationAuthority extends DafConfigurationObject implements ConfigurationAuthority {

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafConfigurationAuthority(DafDataModel dataModel) {
		super(dataModel);
		_internType = CONFIGURATION_AUTHORITY;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafConfigurationAuthority(
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
		super(id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds);
		_internType = CONFIGURATION_AUTHORITY;
	}

	public final String parseToString() {
		String str = "Konfigurationsobjekt Zuständiger: \n";
		str += super.parseToString();
		return str;
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
	public ConfigurationArea getDefaultConfigurationArea() {
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


