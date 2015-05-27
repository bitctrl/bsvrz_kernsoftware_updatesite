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

import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Implementierung der Attributgruppe auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision: 8583 $ / $Date: 2011-01-11 12:33:20 +0100 (Tue, 11 Jan 2011) $ / ($Author: rr $)
 */
public class ConfigAttributeGroup extends ConfigAttributeSet implements AttributeGroup {

	/** Speichert die Attributgruppenverwendungen zu ihren Aspekten. */
	private Map<Aspect, AttributeGroupUsage> _attributeGroupUsageMap = null;

	/**
	 * Konstruktor f�r eine Attributgruppe.
	 *
	 * @param configurationArea der Konfigurationsbereich der Attributgruppe
	 * @param systemObjectInfo  das korrespondierende Objekt f�r die Dateioperationen der Attributgruppe
	 */
	public ConfigAttributeGroup(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public boolean isParameter() {
		final Collection<Aspect> aspects = getAspects();
		final Aspect asp01 = getDataModel().getAspect("asp.parameterSoll");
		final Aspect asp02 = getDataModel().getAspect("asp.parameterVorgabe");
		if(aspects.contains(asp01) && aspects.contains(asp02)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isConfigurating() {
		final AttributeGroupUsage attributeGroupUsage = getAttributeGroupUsage(getDataModel().getAspect("asp.eigenschaften"));
		return attributeGroupUsage != null && attributeGroupUsage.isConfigurating();
	}

	public Collection<AttributeGroupUsage> getAttributeGroupUsages() {
		return Collections.unmodifiableCollection(getAttributeGroupUsageMap().values());
	}

	public AttributeGroupUsage getAttributeGroupUsage(Aspect aspect) {
		return getAttributeGroupUsageMap().get(aspect);
	}

	public Collection<Aspect> getAspects() {
		return Collections.unmodifiableCollection(getAttributeGroupUsageMap().keySet());
	}

	/**
	 * Liest die Attributgruppenverwendungen ein und speichert sie in Abh�ngigkeit zu ihren Aspekten.
	 *
	 * @return die Attributgruppenverwendungen in Abh�ngigkeit zu ihren Aspekten
	 */
	private synchronized Map<Aspect, AttributeGroupUsage> getAttributeGroupUsageMap() {
		if(_attributeGroupUsageMap == null) {
			Map<Aspect, AttributeGroupUsage> attributeGroupUsageMap = new HashMap<Aspect, AttributeGroupUsage>();
			ObjectSet objectSet = getObjectSet("AttributgruppenVerwendungen");
			if(objectSet != null) {
				List<SystemObject> systemObjectList;
				if(getValidSince()>getConfigurationArea().getActiveVersion()) {
					systemObjectList = ((NonMutableSet)objectSet).getElementsInModifiableVersion();
				}
				else {
					systemObjectList = objectSet.getElements();
				}
				for(SystemObject systemObject : systemObjectList) {
					AttributeGroupUsage atgUsage = (AttributeGroupUsage)systemObject;
					if(atgUsage.getPid().equals("atgv.atg.attributgruppenVerwendung.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else if(atgUsage.getPid().equals("atgv.atg.attributEigenschaften.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else if(atgUsage.getPid().equals("atgv.atg.ganzzahlAttributTypEigenschaften.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else if(atgUsage.getPid().equals("atgv.atg.objektReferenzAttributTypEigenschaften.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else if(atgUsage.getPid().equals("atgv.atg.mengenTypEigenschaften.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else if(atgUsage.getPid().equals("atgv.atg.defaultAttributwert.asp.eigenschaften")) {
						attributeGroupUsageMap.put(getDataModel().getAspect("asp.eigenschaften"), atgUsage);
					}
					else {
						attributeGroupUsageMap.put(atgUsage.getAspect(), atgUsage);
					}
				}
			}
			_attributeGroupUsageMap = attributeGroupUsageMap;
		}
		return _attributeGroupUsageMap;
	}

	/**
	 * Wird aufgerufen, wenn das Objekt ver�ndert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zur�cksetzen. Erbende Klassen m�ssen diese
	 * Funktion �berschreiben, wenn sie Daten cachen.
	 */
	@Override
	synchronized void invalidateCache() {
		super.invalidateCache();
		_attributeGroupUsageMap = null;
	}
}
