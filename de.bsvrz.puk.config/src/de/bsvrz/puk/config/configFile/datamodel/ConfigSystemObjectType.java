/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Implementierung der {@link SystemObjectType Typen von System-Objekten} auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
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

	/** Alle Attributgruppen dieses Objekt-Typs, die in aktueller oder in zukünftiger Version gültig sind. */
	private Set<AttributeGroup> _attributeGroupsRelaxed = null;

	/** Die Mengenverwendungen, die an diesem Objekt-Typ definiert wurden. Geerbte Mengenverwendungen werden hier nicht gespeichert. */
	private List<ObjectSetUse> _directObjectSetUses = null;

	/** Alle Mengenverwendungen dieses Objekt-Typs. */
	private List<ObjectSetUse> _objectSetUses = null;

	/** Objekt für den synchronisierten Zugriff auf die Elemente dieses Objekt-Typs. */
	private final Object _lockObject = new Object();

	/** Enthält alle aktuellen System-Objekte, die von diesem Objekt-Typ sind. */
	private Collection<SystemObject> _allElements;

	/** _allElements ist bei dynamischen Typen ein Set, da einige Funktionen eine Liste erwarten, wird hier eine Listenkopie erstellt und gecacht */
	private List<SystemObject> _dynamicElementCache;

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

	/**
	 * Gibt alle Supertypen in der aktuell gültigen oder in zukünftig gültigen Versionen zurücl
	 * @return Liste mit Supertypen
	 */
	private List<SystemObjectType> getSuperTypesRelaxed() {
		List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
		ConfigNonMutableSet set = (ConfigNonMutableSet) getObjectSet("SuperTypen");
		if(set != null) {
			for(SystemObject systemObject : set.getElementsInAnyVersions(
					getConfigurationArea().getActiveVersion(),
					getConfigurationArea().getModifiableVersion()
			)) {
				superTypes.add((SystemObjectType) systemObject);
			}
		}
		return Collections.unmodifiableList(superTypes);
	}

	public List<SystemObjectType> getSubTypes() {
		if(_subTypes == null) {
			final List<SystemObjectType> subTypes = new ArrayList<SystemObjectType>();

			// aktuellen Zeitpunkt ermitteln
			long currentTime = System.currentTimeMillis();

			// ID dieses Typs in einer Collection ablegen
			final Collection<Long> typeIds = new ArrayList<Long>();

			// Implementierung des Datenmodells holen
			final ConfigDataModel configDataModel = getDataModel();

			// die Dateien der Konfigurationsbereiche holen
			final ConfigurationAreaFile[] areaFiles = configDataModel.getConfigurationFileManager().getConfigurationAreas();

			// Sonderfall für Typ.Typ
			if(getPid().equals(Pid.Type.TYPE)) {
				typeIds.add(getId()); // ID von typ.typ nicht vergessen

				for(ConfigurationAreaFile areaFile : areaFiles) {
					// nur aktive Bereiche werden berücksichtigt
					if(getDataModel().getObject(areaFile.getConfigurationAreaInfo().getPid()) != null) {
						// alle aktuellen Objekte, die diese Type-ID als Typ haben, werden zurückgegeben
						SystemObjectInformationInterface[] systemObjectInfos = areaFile.getActualObjects(typeIds);
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
						SystemObjectInformationInterface[] systemObjectInfos = areaFile.getActualObjects(typeIds);

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

	/**
	 * Liefert eine Liste aller aktuell gültigen und aller zukünftig gültigen
	 * Attributgruppen, die von System-Objekten dieses Typs verwendet werden können und
	 * nicht von einem Supertyp geerbt wurden, zurück.
	 *
	 * @return Liste von {@link AttributeGroup Attributgruppen}
	 */
	private List<AttributeGroup> getDirectAttributeGroupsRelaxed() {
		List<AttributeGroup> directAttributeGroups = new ArrayList<AttributeGroup>();
		ConfigNonMutableSet set = (ConfigNonMutableSet) getObjectSet("Attributgruppen");
		if(set != null) {
			for(SystemObject systemObject : set.getElementsInAnyVersions(
					set.getConfigurationArea().getActiveVersion(),
					set.getConfigurationArea().getModifiableVersion()
			)) {
				directAttributeGroups.add((AttributeGroup) systemObject);
			}
		}
		return Collections.unmodifiableList(directAttributeGroups);
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

	/**
	 * Liefert eine Liste aller aktuell gültigen und aller zukünftig gültigen
	 * Attributgruppen, die von System-Objekten dieses Typs verwendet werden können, zurück.
	 *
	 * @return Liste von {@link AttributeGroup Attributgruppen}
	 */
	private Set<AttributeGroup> getAttributeGroupsRelaxed() {
		synchronized(_lockObject) {
			if(_attributeGroupsRelaxed == null) {
				Set<AttributeGroup> attributeGroups = new HashSet<AttributeGroup>();    // ein Set, damit keine Attributgruppen doppelt vorkommen.
				attributeGroups.addAll(getDirectAttributeGroupsRelaxed());
				for(SystemObjectType superType : getSuperTypesRelaxed()) {
					attributeGroups.addAll(((ConfigSystemObjectType) superType).getAttributeGroupsRelaxed());
				}
				_attributeGroupsRelaxed = attributeGroups;
			}
			return _attributeGroupsRelaxed;
		}
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

	public final List<SystemObject> getObjects() {
		return getElements();
	}

	public List<SystemObject> getElements() {
		Collection<SystemObject> allElements = _allElements;
		if(allElements instanceof List || (allElements == null && isConfigurating())) {
			// Konfigurierend und Unveränderlich
			return (List<SystemObject>) getAllElements();
		}
		else {
			List<SystemObject> list;
			synchronized(_lockObject) {
				list = _dynamicElementCache;
				if(list == null) {
					list = Collections.unmodifiableList(new ArrayList<SystemObject>(getAllElements()));
					_dynamicElementCache = list;
				}
			}
			return list;
		}
	}

	/**
	 * Liefert alle aktuellen Elemente des Typs zurück. Bei dynamischen Typen werden auch die in einer Simulation erzeugten Objekte zurückgeliefert.
	 * @return Alle aktuellen Elemente des Typs unabhängig von der Simulationsvariante.
	 */
	protected Collection<SystemObject> getAllElements() {
		synchronized(_lockObject) {
			if(_allElements == null) {
				final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
				objectTypes.add(this);

				// hier müssen alle Bereiche gefragt werden - die vollständigen SubTypen werden in der getObjects-Methode ermittelt
				final Collection<SystemObject> objects = getDataModel().getAllObjects(
						null, objectTypes, ObjectTimeSpecification.valid()
				);
				if(isConfigurating()) {
					// Konfigurierender Typ, Elemente sind fest
					_allElements = Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
				}
				else {
					// handelt es sich um einen dynamischen Typ, dann werden die Objekte auch gespeichert und bei Änderungen wird diese Collection modifiziert.
					_allElements = new ConcurrentSkipListSet<SystemObject>(objects);
					// Eine Alternative wäre eine java.util.concurrent.ConcurrentLinkedQueue, aber die hat O(n) Performance beim
					// Löschen während diese Klasse O(log(n))-Performance hat.
				}
			}
			return _allElements;
		}
	}

	public List<SystemObject> getElements(long time) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getAllObjects(null, objectTypes, ObjectTimeSpecification.valid(time));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}

	public List<SystemObject> getElementsInPeriod(long startTime, long endTime) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getAllObjects(null, objectTypes, ObjectTimeSpecification.validInPeriod(startTime, endTime));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}

	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime) {
		final List<SystemObjectType> objectTypes = new ArrayList<SystemObjectType>();
		objectTypes.add(this);
		// alle Bereich müssen betrachtet werden
		final Collection<SystemObject> objects = getDataModel().getAllObjects(null, objectTypes, ObjectTimeSpecification.validDuringPeriod(startTime, endTime));
		return Collections.unmodifiableList(new ArrayList<SystemObject>(objects));
	}

	@Override
	void invalidateCache() {
		super.invalidateCache();
		synchronized(_lockObject) {
			_allElements = null;
			_attributeGroupsRelaxed = null;
		}
	}

	protected void addElementToCache(final DynamicObject createdObject) {
		synchronized(_lockObject){
			getAllElements().add(createdObject);
			_dynamicElementCache = null;
		}
	}

	protected void removeElementFromCache(final DynamicObject invalidatedObject) {
		synchronized(_lockObject){
			getAllElements().remove(invalidatedObject);
			_dynamicElementCache = null;
		}
	}

	/**
	 * Hilfsmethode. Wirft eine Exception, wenn die angegebene Attributgruppe nicht an diesem Typ verwendet werden kann.
	 * @param attributeGroup Attributgruppe
	 * @throws ConfigurationChangeException
	 */
	protected void validateAttributeGroup(final AttributeGroup attributeGroup) throws ConfigurationChangeException {
		if(!getAttributeGroupsRelaxed().contains(attributeGroup)){
			// Cache leeren, es könnte in der Zwischenzeit eine neue Attributgruppe geben
			synchronized(_lockObject) {
				_attributeGroupsRelaxed = null;
			}
			if(!getAttributeGroupsRelaxed().contains(attributeGroup)) {
				throw new ConfigurationChangeException("Die Attributgruppe " + attributeGroup + " ist an dem Typ " + this + " nicht erlaubt.");
			}
		}
	}
}
