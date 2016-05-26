/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
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
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.dataview.VersionedView;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.Collator;
import java.util.*;

import static de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications.CONFIGURATION_SETS;

/**
 * Implementierung des Interfaces {@link ConfigurationObject} auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigConfigurationObject extends ConfigSystemObject implements ConfigurationObject {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Wird genutzt um den Zugriff auf das Objekt _sets zu synchronisieren. */
	private final Object _lockObject = new Object();

	/** Speichert die Mengen dieses Objekts. */
	private Map<String, ObjectSet> _sets;

	/** Ein Enum zur Unterscheidung von Löschen und Wiederbeleben von Konfigurationsobjekten. */
	private enum Modification {

		INVALIDATE,
		REVALIDATE;
	}

	/**
	 * Konstruktor für ein KonfigurationsObjekt.
	 *
	 * @param configurationArea der Konfigurationsbereich dieses KonfigurationsObjekts
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen dieses KonfigurationsObjekts
	 */
	public ConfigConfigurationObject(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public short getValidSince() {
		return ((ConfigurationObjectInfo)_systemObjectInfo).getFirstValidVersion();
	}

	public short getNotValidSince() {
		return ((ConfigurationObjectInfo)_systemObjectInfo).getFirstInvalidVersion();
	}

	public boolean isValid() {
		short activeVersion = getConfigurationArea().getActiveVersion();
		if(getValidSince() > activeVersion) {
			// Objekt wurde noch nicht gültig
			return false;
		}
		final short notValidSince = getNotValidSince();
		if(notValidSince != 0 && notValidSince <= activeVersion) {
			// Objekt wurde bereits auf ungültig gesetzt
			return false;
		}
		// Objekt ist gültig und wurde noch nicht auf ungültig gesetzt oder wird erst in einer späteren Version ungültig
		return true;
	}

	public void invalidate() throws ConfigurationChangeException {
		super.invalidate();
		// prüfen, ob es sich bei diesem Objekt um ein freies Objekt handelt.
		if(!isFreeObject()) {
			final String message = "Das Objekt '" + getPidOrNameOrId() + "' ist kein freies Objekt und kann deshalb nicht gelöscht werden.";
			_debug.warning(message);
			throw new ConfigurationChangeException(message);
		}
		directModification(Modification.INVALIDATE);
	}

	/**
	 * Löscht oder wiederbelebt das Objekt ohne zu prüfen, ob der Konfigurationsverantwortliche das Objekt ändern darf und unabhängig davon, ob es sich um ein
	 * freies Objekt handelt.
	 *
	 * @param mod gibt an, ob die Objekt-Einheit gelöscht oder wiederbelebt werden soll
	 */
	void directModification(final Modification mod) {
		// die Mengen und bei Komposition auch die Elemente werden betrachtet
		final List<ObjectSet> sets = getObjectSets();
		for(ObjectSet set : sets) {
			if(set.getObjectSetType().getReferenceType() == ReferenceType.COMPOSITION) {
				// Elemente
				final List<SystemObject> elements = set.getElements();
				for(SystemObject element : elements) {
					final ConfigConfigurationObject configConfigurationObject = (ConfigConfigurationObject)element;
					configConfigurationObject.directModification(mod);   // rekursiv die Elemente weiterverarbeiten
				}
			}
			// die Menge wird auch modifiziert
			final ConfigConfigurationObject configConfigurationObject = (ConfigConfigurationObject)set;
			configConfigurationObject.directModification(mod);
		}

		// Sans, STS, KonfigAss: lookup erstellen und benutzen
		ConfigDataModel dataModel = (ConfigDataModel)getDataModel();
		Map<ConfigurationArea, Short> configurationVersions = new HashMap<ConfigurationArea, Short>();
		Collection<ConfigurationArea> areas = dataModel.getAllConfigurationAreas().values();

		for (ConfigurationArea configurationArea: areas) {
			configurationVersions.put(configurationArea, configurationArea.getModifiableVersion());
		}

		ObjectLookup lookup = new VersionedView(dataModel, configurationVersions);

		// alle Datensätze prüfen
		final Collection<AttributeGroupUsage> atgUsages = getUsedAttributeGroupUsages();
		for(AttributeGroupUsage atgUsage : atgUsages) {
			final Data data = getConfigurationData(atgUsage, lookup);
			if(data != null) {
				// vorhandene Referenzen auf Komposition prüfen und Objekte bearbeiten
				modifyDependentObjects(data, mod);
			}
		}
		if(mod == Modification.INVALIDATE) {
			((ConfigurationObjectInfo)_systemObjectInfo).invalidate();
		}
		else if(mod == Modification.REVALIDATE) {
			((ConfigurationObjectInfo)_systemObjectInfo).revalidate();
		}
	}

	/**
	 * Objekte, die via Komposition referenziert werden, werden auf {@link #invalidate() ungültig} gesetzt.
	 *
	 * @param data der zu prüfende Datensatz
	 * @param mod  gibt an, ob die Objekt-Einheit gelöscht, wiederbelebt oder dupliziert werden soll
	 */
	private void modifyDependentObjects(final Data data, final Modification mod) {
		if(data.isPlain()) {
			final AttributeType att = data.getAttributeType();
			if(att instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)att;
				if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
					final ConfigConfigurationObject referencedObject = (ConfigConfigurationObject)data.asReferenceValue().getSystemObject();
					if(referencedObject != null && referencedObject != this) {
						referencedObject.directModification(mod);
					}
				}
			}
		}
		else {
			for(Iterator iterator = data.iterator(); iterator.hasNext();) {
				Data subData = (Data)iterator.next();
				modifyDependentObjects(subData, mod);
			}
		}
	}

	/**
	 * Ermittelt, ob dieses Objekt ein freies Objekt ist. Ein freies Objekt ist ein Objekt, welches nicht Komponente eines anderen übergeordneten Objekts ist.
	 *
	 * @return <code>true</code>, wenn dieses Objekt ein freies Objekt ist, sonst <code>false</code>
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Nicht mehr gültige Objekte, können nicht geprüft werden, ob sie freie Objekte sind.
	 */
	private boolean isFreeObject() throws ConfigurationChangeException {
		// gibt es einen Datensatz, der mittels Komposition auf dieses Objekt zeigt?
		// alle Objekte (in der Version des Objekts) dieses Bereichs müssen betrachtet werden!
		final Collection<SystemObject> systemObjects = new LinkedList<SystemObject>();
		if(isValid()) {
			// Sans, STS, KonfigAss: Zukünftig ungültig werdende Objekte werde nicht betrachtet
			



			ConfigurationArea configurationArea = getConfigurationArea();
			for (SystemObject object: configurationArea.getObjects(null, ObjectTimeSpecification.valid()))
			{
				if (object instanceof ConfigurationObject)
				{
					if (!(((ConfigurationObject)object).getNotValidSince() >= configurationArea.getModifiableVersion()))
					// Objekt wird in zukünftiger Version nicht ungültig
					{
						systemObjects.add(object);
					}
				}
				else if (object instanceof DynamicObject)
				{
					if (!(((DynamicObject)object).getNotValidSince() >= configurationArea.getModifiableVersion()))
					// Objekt wird in zukünftiger Version nicht ungültig
					{
						systemObjects.add(object);
					}
				}
			}
		}
		else if(getValidSince() > getConfigurationArea().getActiveVersion()) {
			systemObjects.addAll(getConfigurationArea().getNewObjects());
		}
		else {
			throw new ConfigurationChangeException("Nicht mehr gültige Objekte, können nicht geprüft werden, ob sie freie Objekte sind.");
		}

		// Sans, STS, KonfigAss: lookup erstellen und benutzen
		ConfigDataModel dataModel = (ConfigDataModel)getDataModel();

		Map<ConfigurationArea, Short> configurationVersions = new HashMap<ConfigurationArea, Short>();
		Collection<ConfigurationArea> areas = dataModel.getAllConfigurationAreas().values();

		for (ConfigurationArea configurationArea: areas) {
			configurationVersions.put(configurationArea, configurationArea.getModifiableVersion());
		}

		ObjectLookup lookup = new VersionedView(dataModel, configurationVersions);

		for(SystemObject systemObject : systemObjects) {
			// dieses Objekt selbst muss nicht überprüft werden
			if(systemObject == this) continue;

			// gibt es eine Menge mit Komposition, die auf dieses Objekt zeigt?
			if(systemObject instanceof ObjectSet) {
				final ObjectSet objectSet = (ObjectSet)systemObject;
				if(objectSet.getObjectSetType().getReferenceType() == ReferenceType.COMPOSITION) {
					final List<SystemObject> elements = objectSet.getElements();
					for(SystemObject element : elements) {
						if(element == this) return false;
					}
				}
			}

			// Referenzen in Datensätzen der Objekte überprüfen
			final Collection<AttributeGroupUsage> atgUsages = systemObject.getUsedAttributeGroupUsages();
			for(AttributeGroupUsage atgUsage : atgUsages) {
				final Data data = ((ConfigSystemObject)systemObject).getConfigurationData(atgUsage, lookup);
				// rekursiv auf Abhängigkeit prüfen
				if(data != null && isObjectDependsOnDataset(data)) {
					// es gibt eine Abhängigkeit, damit ist das Objekt kein freies Objekt
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Prüft, ob in dem angegebenen Datensatz eine Referenz mittels Komposition auf dieses Objekt verweist.
	 *
	 * @param data zu prüfenden Datensatz
	 *
	 * @return <code>true</code>, wenn es eine Referenz mittels Komposition auf dieses Objekt gibt, sonst <code>false</code>
	 */
	private boolean isObjectDependsOnDataset(final Data data) {
		if(data.isPlain()) {
			final AttributeType att = data.getAttributeType();
			if(att instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)att;
				if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
					final SystemObject referencedObject = data.asReferenceValue().getSystemObject();
					if(referencedObject != null && referencedObject == this) return true;
				}
			}
			return false;
		}
		else {
			boolean isObjectDependsOnDataset = false;
			for(Iterator iterator = data.iterator(); iterator.hasNext();) {
				Data subData = (Data)iterator.next();
				if(isObjectDependsOnDataset(subData)) isObjectDependsOnDataset = true;
			}
			return isObjectDependsOnDataset;
		}
	}

	public void revalidate() throws ConfigurationChangeException {
		if(!checkChangePermit()) {
			final String errorMessage = "Das Objekt '" + getNameOrPidOrId() + "' darf nicht wiederbelebt werden, da keine Berechtigung hierfür vorliegt.";
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
		// prüfen, ob es sich bei diesem Objekt um ein freies Objekt handelt.
		if(!isFreeObject()) {
			throw new ConfigurationChangeException("Dieses Objekt '" + getPidOrNameOrId() + "' ist kein freies Objekt und darf nicht wiederbelebt werden.");
		}

		// wenn das Objekt noch gültig ist, mache nichts
		if(getNotValidSince() == 0) return;

		// das Objekt kann nur in einer Version wiederbelebt werden, in der es auch ungültig wurde (die modifiableVersion also)
		if(getNotValidSince() != getConfigurationArea().getModifiableVersion()) {
			throw new ConfigurationChangeException(
					"Das Objekt " + getPidOrNameOrId() + " wurde in einer früheren Version ungültig. Es kann nicht wiederbelebt werden."
			);
		}
		directModification(Modification.REVALIDATE);
	}

	public void simpleRevalidate() {
		((ConfigurationObjectInfo)_systemObjectInfo).revalidate();
	}

	public SystemObject duplicate() throws ConfigurationChangeException {
		return duplicate(new HashMap<String, String>());
	}

	public SystemObject duplicate(Map<String, String> substitudePids) throws ConfigurationChangeException {
		if(!checkChangePermit()) {
			final String errorMessage = "Das Objekt '" + getNameOrPidOrId() + "' darf nicht dupliziert werden, da keine Berechtigung hierfür vorliegt.";
			_debug.warning(errorMessage);
			throw new ConfigurationChangeException(errorMessage);
		}
		// prüfen, ob es sich bei diesem Objekt um ein freies Objekt handelt.
		if(!isFreeObject()) {
			throw new ConfigurationChangeException("Dieses Objekt '" + getPidOrNameOrId() + "' ist kein freies Objekt und darf nicht dupliziert werden.");
		}

		// das Objekt wird bereits hier erstellt, denn falls ein Fehler in der Methode directDuplicate vorkommt, kann dieses Objekt mit der
		// gesamten Objekt-Einheit gelöscht werden
		final String pid = substitudePids.get(this.getPid());   // gibt es einen Ersatz für diese Pid?
		final ConfigurationObject duplicatedObject = getConfigurationArea().createConfigurationObject(
				(ConfigurationObjectType)this.getType(), pid == null ? this.getPid() : pid, this.getName(), null
		);
		try {
			directDuplicate(this, duplicatedObject, substitudePids);
		}
		catch(ConfigurationChangeException e) {
			// dupliziertes Objekt wieder löschen, da es nicht vollständig dupliziert werden konnte
			duplicatedObject.invalidate();
			throw e;
		}
		return duplicatedObject;
	}

	/**
	 * Diese Methode erhält ein KonfigurationsObjekt und gibt ein Duplikat zurück.
	 *
	 * @param object           zu duplizierendes KonfigurationsObjekt
	 * @param duplicatedObject dupliziertes Objekt oder <code>null</code>, falls es noch dupliziert werden soll
	 * @param substitudePids   Map, die die Wert-Paare (altePid, neuePid) enthält.
	 *
	 * @return Duplikat
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht dupliziert werden konnte.
	 */
	SystemObject directDuplicate(final ConfigurationObject object, ConfigurationObject duplicatedObject, final Map<String, String> substitudePids)
			throws ConfigurationChangeException {

		// das Objekt wird dupliziert (nur KonfigurationObjekte)
		if(duplicatedObject == null) {
			final String pid = substitudePids.get(object.getPid());
			duplicatedObject = getConfigurationArea().createConfigurationObject(
					(ConfigurationObjectType)object.getType(), pid == null ? object.getPid() : pid, object.getName(), null
			);
		}

		// Mengen werden dupliziert und dem Objekt hinzugefügt
		final List<ObjectSet> sets = object.getObjectSets();
		for(ObjectSet set : sets) {
			// neue Menge erstellen
			final String pid = substitudePids.get(set.getPid());
			final ObjectSet duplicatedSet = (ObjectSet)getConfigurationArea().createConfigurationObject(
					set.getObjectSetType(), pid == null ? set.getPid() : pid, set.getName(), null
			);
			if(set.getObjectSetType().getReferenceType() == ReferenceType.COMPOSITION) {
				// bei Komposition müssen die Elemente selber dupliziert werden
				final List<SystemObject> elements = set.getElements();
				for(SystemObject element : elements) {
					// jedes Element wird dupliziert
					duplicatedSet.add(directDuplicate((ConfigurationObject)element, null, substitudePids));
				}
			}
			else {
				// alle aktuellen Elemente aus der alten Menge nehmen und der neuen Menge hinzufügen
				final List<SystemObject> elements = set.getElements();
				duplicatedSet.add(elements.toArray(new SystemObject[elements.size()]));
			}
			// Menge dem Objekt hinzufügen
			duplicatedObject.addSet(duplicatedSet);
		}

		// Sans, STS, KonfigAss: lookup erstellen und benutzen
		ConfigDataModel dataModel = (ConfigDataModel)getDataModel();
		Map<ConfigurationArea, Short> configurationVersions = new HashMap<ConfigurationArea, Short>();
		Collection<ConfigurationArea> areas = dataModel.getAllConfigurationAreas().values();

		for (ConfigurationArea configurationArea: areas) {
			configurationVersions.put(configurationArea, configurationArea.getModifiableVersion());
		}

		ObjectLookup lookup = new VersionedView(dataModel, configurationVersions);

		// alle Datensätze prüfen
		final Collection<AttributeGroupUsage> atgUsages = object.getUsedAttributeGroupUsages();
		for(AttributeGroupUsage atgUsage : atgUsages) {
			final Data data = ((ConfigSystemObject)object).getConfigurationData(atgUsage, lookup);
			if(data != null) {
				final Data modifiableData = data.createModifiableCopy();
				// Kompositionen ersetzen
				duplicateDependentObjects(modifiableData, substitudePids);
				// geänderten Datensatz speichern
				duplicatedObject.setConfigurationData(atgUsage, modifiableData);
			}
		}
		return duplicatedObject;
	}

	/**
	 * Objekte, die via Komposition referenziert werden, werden dupliziert.
	 *
	 * @param data           der zu duplizierende Datensatz
	 * @param substitudePids Map, die die Wert-Paare (altePid, neuePid) enthält.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Falls ein via Komposition referenziertes Objekt nicht dupliziert werden konnte.
	 */
	private void duplicateDependentObjects(final Data data, final Map<String, String> substitudePids) throws ConfigurationChangeException {
		if(data.isPlain()) {
			final AttributeType att = data.getAttributeType();
			if(att instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)att;
				if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
					final Data.ReferenceValue referenceValue = data.asReferenceValue();
					final SystemObject systemObject = referenceValue.getSystemObject();
					if(systemObject != null && systemObject != this) {
						referenceValue.setSystemObject(directDuplicate((ConfigurationObject)systemObject, null, substitudePids));
					}
				}
			}
		}
		else {
			for(Iterator iterator = data.iterator(); iterator.hasNext();) {
				Data subData = (Data)iterator.next();
				duplicateDependentObjects(subData, substitudePids);
			}
		}
	}

	public MutableSet getMutableSet(String name) {
		final ObjectSet set = getObjectSet(name);
		if(set instanceof MutableSet) return (MutableSet)set;
		return null;
	}

	public NonMutableSet getNonMutableSet(String name) {
		final ObjectSet set = getObjectSet(name);
		if(set instanceof NonMutableSet) return (NonMutableSet)set;
		return null;
	}

	public ObjectSet getObjectSet(String name) {
		return getObjectSetMap().get(name);
	}

	/**
	 * Ermittelt die Mengen dieses Konfigurationsobjekts und speichert sie in einer Map.
	 *
	 * @return die Mengen dieses Konfigurationsobjekts
	 */
	private Map<String, ObjectSet> getObjectSetMap() {
		synchronized(_lockObject) {
			if(_sets == null) {
				Map<String, ObjectSet> sets = new TreeMap<String, ObjectSet>(Collator.getInstance(Locale.GERMAN)::compare);
				try {
					// feste ID für die Attributgruppenverwendung um alle Mengen zu erhalten
					byte[] bytes = _systemObjectInfo.getConfigurationData(CONFIGURATION_SETS);
					final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
					final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);

					int numberOfIds = bytes.length / 8;
					for(int i = 0; i < numberOfIds; i++) {
						long id = deserializer.readLong();
						SystemObject systemObject = getDataModel().getObject(id);
						if(systemObject instanceof ObjectSet) {
							ObjectSet objectSet = (ObjectSet)systemObject;
							sets.put(objectSet.getName(), objectSet);
						}
					}
				}
				catch(IllegalArgumentException ex) {
					final String errorMessage = "Die Mengen des Objekts " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
					_debug.finest(errorMessage);
					// es wird eine leere Map zurückgegeben.
				}
				catch(Exception ex) {
					final String errorMessage = "Die Mengen des Objekts " + getPidOrNameOrId() + " konnten nicht ermittelt werden";
					_debug.error(errorMessage, ex);
					throw new RuntimeException(errorMessage, ex);
				}
				_sets = sets;
			}
			return _sets;
		}
	}

	public List<ObjectSet> getObjectSets() {
		return Collections.unmodifiableList(new ArrayList<ObjectSet>(getObjectSetMap().values()));
	}

	public void addSet(ObjectSet set) throws ConfigurationChangeException {
		// darf hinzugefügt werden ?
		if(checkChangePermit()) {
			// wurde das Objekt bereits aktiviert?
			if(getValidSince() < getConfigurationArea().getModifiableVersion()) {
				// Objekt darf nicht mehr verändert werden
				throw new ConfigurationChangeException(
						"Das Konfigurationsobjekt " + getNameOrPidOrId() + " darf nicht mehr verändert werden, "
						+ " da es bereits aktiviert oder zur Übernahme / Aktivierung freigegeben wurde."
				);
			}
			// das Objekt darf verändert werden
			synchronized(_lockObject) {
				getObjectSetMap();  // Mengen wurden eingeladen, falls sie noch nicht da waren
				if(_sets.containsKey(set.getName())) {
					throw new ConfigurationChangeException(
							"Die Menge " + set.getNameOrPidOrId() + " gibt es bereits am Konfigurationsobjekt. " + "Sie wurde nicht hinzugefügt."
					);
				}
				else {
					// Menge hinzufügen
					_sets.put(set.getName(), set);

					// den konfigurierenden Datensatz schreiben
					try {
						setConfigurationData(_sets.values());
					}
					catch(ConfigurationChangeException ex) {
						_sets = null; // die eingeladenen Mengen passen evtl. nicht mehr zu den gespeicherten
						throw new ConfigurationChangeException(ex);
					}
				}
			}
		}
		else {
			throw new ConfigurationChangeException(
					"Es liegt keine Berechtigung zum Verändern dieses Objekts '" + getNameOrPidOrId() + "' vor."
					+ " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
					+ "' zuständig."
			);
		}
	}

	public void removeSet(ObjectSet set) throws ConfigurationChangeException {
		// darf entfernt werden ?
		if(checkChangePermit()) {
			// wurde das Objekt bereits aktiviert?
			if(getValidSince() < getConfigurationArea().getModifiableVersion()) {
				// Objekt darf nicht mehr verändert werden
				throw new ConfigurationChangeException(
						"Das Konfigurationsobjekt " + getNameOrPidOrId() + " darf nicht mehr verändert werden, "
						+ " da es bereits aktiviert oder zur Übernahme / Aktivierung freigegeben wurde."
				);
			}
			// das Objekt darf also verändert werden
			synchronized(_lockObject) {
				getObjectSetMap();   // Mengen wurden eingeladen, falls sie noch nicht da waren
				ObjectSet returnedSet = _sets.remove(set.getName());
				if(returnedSet != null) {
					// es wurde tatsächlich eine Menge gelöscht
					// -> den konfigurierenden Datensatz schreiben
					try {
						setConfigurationData(_sets.values());
					}
					catch(ConfigurationChangeException ex) {
						_sets = null; // die eingeladenen Mengen passen ggf. nicht mehr
						throw new ConfigurationChangeException(ex);
					}
				}
			}
		}
		else {
			throw new ConfigurationChangeException(
					"Es liegt keine Berechtigung zum Verändern dieses Objekts '" + getNameOrPidOrId() + "' vor."
					+ " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
					+ "' zuständig."
			);
		}
	}

	/**
	 * Speichert den konfigurierenden Datensatz, der die Mengen enthält, am Objekt und gibt dem Konfigurationsbereich Bescheid, dass sich ein Datensatz geändert
	 * hat.
	 *
	 * @param sets Die Mengen, die in einem Datensatz gespeichert werden sollen.
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht gespeichert werden konnte.
	 */
	private void setConfigurationData(final Collection<ObjectSet> sets) throws ConfigurationChangeException {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
			for(ObjectSet objectSet : sets) {
				serializer.writeLong(objectSet.getId());
			}
			_systemObjectInfo.setConfigurationData(CONFIGURATION_SETS, out.toByteArray());
			((ConfigConfigurationArea)getConfigurationArea()).setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der konfigurierende Datensatz mit den Mengen des Objekts " + getNameOrPidOrId() + " konnte nicht geschrieben werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. Erbende Klassen müssen
	 * diese Funktion überschreiben, wenn sie Daten cachen.
	 */
	void invalidateCache() {
		super.invalidateCache();
		synchronized(_lockObject){
			_sets = null;
		}
	}
}
