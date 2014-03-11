/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;

/**
 * Implementierung des Interfaces {@link de.bsvrz.dav.daf.main.config.AttributeListDefinition} auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigAttributeListDefinition extends ConfigAttributeSet implements AttributeListDefinition {
	/**
	 * Konstruktor einer Attributliste.
	 *
	 * @param configurationArea der Konfigurationsbereich dieser Attributliste
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigAttributeListDefinition(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	/**
	 * Liefert als Default-Attributwert dieses Attributtyps den Wert <code>null</code> zurück, weil an Attributlisten
	 * keine Defaultwerte definiert werden können.
	 *
	 * @return <code>null</code>.
	 */
	public String getDefaultAttributeValue() {
		return null;
	}
}
