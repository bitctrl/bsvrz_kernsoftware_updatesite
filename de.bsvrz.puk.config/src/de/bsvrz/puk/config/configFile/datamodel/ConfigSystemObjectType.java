/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaTime;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.dav.daf.main.config.Pid;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementierung der {@link SystemObjectType Typen von System-Objekten} auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10502 $
 */
public class ConfigSystemObjectType extends ConfigConfigurationObject implements SystemObjectType {

	/** Speichert die Super-Typen dieses Objekt-Typs. */
	private List<SystemObjectType> _superTypes = null;

	/** Speichert die Sub-Typen dieses Objekt-Typs. */
	private List<SystemObjectType> _subTypes = null;

	/** Speichert die Attributgruppen, die an diesem Objekt-Typ definiert wurden. Geerbte Attributgruppen werden hier nicht aufgeführt. */
	private List<AttributeGroup> _directAttributeGroups = null;

	/** Alle Attributgruppen dieses Objekt-Typs. */
	private List<AttributeGroup> _attributeGroups = null;

	/** Die Mengenverwendungen, die an diesem Objekt-Typ definiert wurden. Geerbte Mengenverwendungen werden hier nicht gespeichert. */
	private List<ObjectSetUse> _directObjectSetUses = null;

	/** Alle Mengenverwendungen dieses Objekt-Typs. */
	private List<ObjectSetUse> _objectSetUses = null;

	/** Objekt für den synchronisierten Zugriff auf die Elemente dieses Objekt-Typs. */
	private final Object _lockObject = new Object();

	/** Enthält alle aktuellen System-Objekte, die von diesem Objekt-Typ sind. */
	private List<SystemObject> _allElements;

	/**
	 * Konstruktor eines System-Objekt-Typs.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses Objekts
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses Objekts
	 */
	public ConfigSystemObjectType(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public List<SystemObjectType> getSuperTypes() {
		if(_superTypes == null) {
			List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
			ObjectSet set = getObjectSet("SuperTypen");
			if(set != null) {
				for(SystemObject systemObject : set.getElements()) {
					superTypes.add((SystemObjectType)systemObject);
				}
			}
			_superTypes = Collections.unmodifiableList(superTypes);
		}
		return _superTypes;
	}

	public List<SystemObjectType> getSubTypes() {
		if(_subTypes == null) {
			final List<SystemObjectType> subTypes = new ArrayList<SystemObjectType>();

			// aktuellen Zeitpunkt ermitteln
			long currentTime = System.currentTimeMillis();

			// ID dieses Typs in einer Collection ablegen
			final Collection<Long> typeIds = new ArrayList<Long>();

			// Implementierung des Datenmodells holen
			final ConfigDataModel configDataModel = (ConfigDataModel)getDataModel();

			// die Dateien der Konfigurationsbereiche holen
			final ConfigurationAreaFile[] areaFiles = configDataModel.getConfigurationFileManager().getConfigurationAreas();

			// Sonderfall für Typ.Typ
			if(getPid().equals(Pid.Type.TYPE)) {
				typeIds.add(getId()); // ID von typ.typ nicht vergessen

				for(ConfigurationAreaFile areaFile : areaFiles) {
					// nur aktive Bereiche werden berücksichtigt
					if(getDataModel().getObject(areaFile.getConfigurationAreaInfo().getPid()) != null) {
						// alle aktuellen Objekte, die diese Type-ID als Typ haben, werden zurückgegeben
						SystemObjectInformationInterface[] systemObjectInfos = areaFile.getObjects(
								currentTime, currentTime, ConfigurationAreaTime.LOCAL_ACTIVATION_TIME, TimeSpecificationType.VALID, typeIds
						);
						for(SystemObjectInformationInterface systemObjectInfo : systemObjectInfos) {
							SystemObject systemObject = configDataModel.createSystemObject(systemObjectInfo);
							if(systemObject instanceof SystemObjectType) {
								SystemObjectType objectType = (SystemObjectType)systemObject;
								if(objectType.getSuperTypes().contains(this)) {
									subTypes.add(objectType);
								}
							}
						}
						// zurückgegeben werden müssten folgende Typen: typ.mengenTyp und typ.dynamischerTyp
					}
				}
			}
			else { // alle anderen Typen
				// IDs sammeln
				final List<SystemObjectType> typeTypeSubTypes = getDataModel().getTypeTypeObject().getSubTypes();
				typeIds.add(getDataModel().getTypeTypeObject().getId());	// typ.typ
				for(SystemObjectType objectType : typeTypeSubTypes) {	  // typ.mengenTyp und typ.dynamischerTyp
					typeIds.add(objectType.getId());
				}

				// alle Konfigurationsbereiche durchgehen
				for(ConfigurationAreaFile areaFile : areaFiles) {
					// nur aktive Bereiche werden berücksichtigt
					if(getDataModel().getObject(areaFile.getConfigurationAreaInfo().getPid()) != null) {
						SystemObjectInformationInterface[] systemObjectInfos = areaFile.getObjects(
								currentTime, currentTime, ConfigurationAreaTime.LOCAL_ACTIVATION_TIME, TimeSpecificationType.VALID, typeIds
						);

						for(SystemObjectInformationInterface systemObjectInfo : systemObjectInfos) {
							SystemObject systemObject = configDataModel.createSystemObject(systemObjectInfo);
							if(systemObject instanceof SystemObjectType) {
								SystemObjectType objectType = (SystemObjectType)systemObject;
								if(objectType.getSuperTypes().contains(this)) {
									subTypes.add(objectType);
								}
							}
						}
					}
				}
			}
			_subTypes = Collections.unmodifiableList(subTypes);
		}
		return _subTypes;
	}

	public List<AttributeGroup> getDirectAttributeGroups() {
		if(_directAttributeGroups == null) {
			List<AttributeGroup> directAttributeGroups = new ArrayList<AttributeGroup>();
			ObjectSet set = getObjectSet("Attributgruppen");
			if(set != null) {
				for(SystemObject systemObject : set.getElements()) {
					directAttributeGroups.add((AttributeGroup)systemObject);
				}
			}
			_directAttributeGroups = Collections.unmodifiableList(directAttributeGroups);
		}
		return _directAttributeGroups;
	}

	public List<AttributeGroup> getAttributeGroups() {
		if(_attributeGroups == null) {
			Set<AttributeGroup> attributeGroups = new HashSet<AttributeGroup>();	// ein Set, damit keine Attributgruppen doppelt vorkommen.
			attributeGroups.addAll(getDirectAttributeGroups());
			for(SystemObjectType superType : getSuperTypes()) {
				attributeGroups.addAll(superType.getAttributeGroups());
			}
			_attributeGroups = Collections.unmodifiableList(new ArrayList<AttributeGroup>(attributeGroups));
		}
		return _attributeGroups;
	}

	public boolean isBaseType() {
		return getSuperTypes().size() == 0;	// Gibt es keine Super-Typen, so muss dies wohl ein BasisTyp sein.
	}

	public boolean isConfigurating() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.typEigenschaften"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			return data.getTextValue("konfigurierend").getText().equals("ja");
		}
		throw new IllegalStateException("Es kann nicht ermittelt werden, ob dieser Typ konfigurierend ist.");
	}

	public List<ObjectSetUse> getDirectObjectSetUses() {
		if(_directObjectSetUses == null) {
			List<ObjectSetUse> directObjectSetUses = new ArrayList<ObjectSetUse>();
			ObjectSet set = getObjectSet("Mengen");
			if(set != null) {
				for(SystemObject systemObject : set.getElements()) {
					directObjectSetUses.add((ObjectSetUse)systemObject);
				}
			}
			_directObjectSetUses = Collections.unmodifiableList(directObjectSetUses);
		}
		return _directObjectSetUses;
	}

	public List<ObjectSetUse> getObjectSetUses() {
		if(_objectSetUses == null) {
			Set<ObjectSetUse> objectSetUses = new HashSet<ObjectSetUse>();
			objectSetUses.addAll(getDirectObjectSetUses());
			for(SystemObjectType superType : getSuperTypes()) {
				objectSetUses.addAll(superType.getObjectSetUses());
			}
			_objectSetUses = Collections.unmodifiableList(new ArrayList<ObjectSetUse>(objectSetUses));
		}
		return _objectSetUses;
	}

	public boolean inheritsFrom(SystemObjectType other) {
		// direkte Vererbung
		if(getSuperTypes().contains(other)) return true;
		// indirekte Vererbung
		for(SystemObjectType superType : getSuperTypes()) {
			if(superType.inheritsFrom(other)) return true;
		}
		return false;
	}

	public boolean isNameOfObjectsPermanent() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.typEigenschaften"), getDataModel().getAspect("asp.eigenschaften"));
		if(data != null) {
			return data.getTextValue("namePermanent").getText().equals("Ja");
		}
		throw new IllegalStateException(
				"Es kann nicht ermittelt werden, ob der Name eines Objekts vom Typ '" + getPidOrNameOrId() + "' geändert werden kann oder nicht."
		);
	}

	public List<SystemObject> getObjects() {
		return Collections.unmodifiableList(getAllElements());
	}

	public List<SystemObject> getElements() {
		List<SystemObject> allElements = getAllElements();
		List<SystemObject> systemObjects = Collections.unmodifiableList(allElements);
		return systemObjects;
	}

	/**
	 * Liefert alle aktuellen Elemente des Typs zurück. Bei dynamischen Typen werden auch die in einer Simulation erzeugten Objekte zurückgeliefert.
	 * @return Alle aktuellen Elemente des Typs unabhängig von der Simulationsvariante.
	 */
	protected List<SystemObject> getAllElements() {
		synchronized(_lockObject) {
			if(_allElements == null) {
				final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
				objectTypes.add(this);

				// hier müssen alle Bereiche gefragt werden - die vollständigen SubTypen werden in der getObjects-Methode ermittelt
				final Collection<SystemObject> objects = getDataModel().getObjects(null, objectTypes, ObjectTimeSpecification.valid());
				if(isConfigurating()) {
					_allElements = new ArrayList<SystemObject>(objects);
				}
				else {
					// handelt es sich um einen dynamischen Typ, dann werden die Objekte auch gespeichert und bei Änderungen wird die hier
					// zurückgelieferte Liste modifiziert.
					_allElements = new CopyOnWriteArrayList<SystemObject>(objects);
				}
			}
		}
		return _allElements;
	}

	public List<SystemObject> getElements(long time) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getObjects(null, objectTypes, ObjectTimeSpecification.valid(time));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}

	public List<SystemObject> getElementsInPeriod(long startTime, long endTime) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getObjects(null, objectTypes, ObjectTimeSpecification.validInPeriod(startTime, endTime));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}

	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getObjects(null, objectTypes, ObjectTimeSpecification.validDuringPeriod(startTime, endTime));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}
}
