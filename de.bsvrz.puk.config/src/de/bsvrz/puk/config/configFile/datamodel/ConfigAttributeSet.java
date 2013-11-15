/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kni� Systemberatung Aachen (K2S)
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
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Implementierung des Interfaces {@link AttributeSet} f�r Attributmengen auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (Sun, 02 Sep 2007) $ / ($Author: rs $)
 */
public class ConfigAttributeSet extends ConfigConfigurationObject implements AttributeSet {

	/** Die Attribute dieser Attributmenge. */
//	private Map<String, Attribute> _attributeMap = null;

	/**
	 * Konstruktor f�r eine {@link de.bsvrz.dav.daf.main.config.AttributeSet Attributmenge}.
	 *
	 * @param configurationArea der Konfigurationsbereich dieser Attributmenge
	 * @param systemObjectInfo  das korrespondierende Objekt f�r die Dateioperationen dieser Attributmenge
	 */
	public ConfigAttributeSet(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(new ArrayList<Attribute>(getAttributeMap().values()));
	}

	public Attribute getAttribute(String attributeName) {
		return getAttributeMap().get(attributeName);
	}

	/**
	 * Liest die Attribute aus der Konfiguration und merkt sie sich in einer Map. Als Schl�ssel wird der Name des Attributs verwendet.
	 *
	 * @return alle Attribute dieser Attributmenge
	 */
	private synchronized Map<String, Attribute> getAttributeMap() {
		// Attribute nicht cachen, da sonst bei neu erstellten Attributgruppen die Map nicht aktualisiert wird!
//		if (_attributeMap == null) {
		// Eine LinkedHashMap beh�lt die Sortierung bei (die Reihenfolge, wie
		// die Elemente hinzugef�gt werden ist entscheidend).
		Map<String, Attribute> attributeMap = new LinkedHashMap<String, Attribute>();
		List<Attribute> attributes = new ArrayList<Attribute>();
		ObjectSet set = getObjectSet("Attribute");
		if(set != null) {
			// Liste der Attribute ermitteln
			for(SystemObject systemObject : set.getElements()) {
				attributes.add((Attribute)systemObject);
			}
			// Liste sortieren
			// die Reihenfolge der Attribute ist wichtig f�r die getAttributes()-Methode - sie liefert die Attribute sortiert nach der Position zur�ck
			Collections.sort(
					attributes, new Comparator<Attribute>() {
				public int compare(Attribute o1, Attribute o2) {
					return (o1.getPosition() - o2.getPosition());
				}
			}
			);
			// Sortierte Liste in eine Map einf�gen.
			for(Attribute attribute : attributes) {
				attributeMap.put(attribute.getName(), attribute);
			}
		}
		return attributeMap;
//		_attributeMap = attributeMap;
//		}
//		return _attributeMap;
	}
}
