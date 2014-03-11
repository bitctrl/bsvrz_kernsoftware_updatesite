/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaTime;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationFileManager;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaChangeInformation;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementierung des Konfigurationsbereichs auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11583 $
 */
public class ConfigConfigurationArea extends ConfigConfigurationObject implements ConfigurationArea, ConfigConfigurationAreaInterface {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Eine Referenz auf das Datenmodell. */
	private final DataModel _dataModel;

	/** Konfigurationsverantwortlicher dieses Konfigurationsbereichs. */
	private ConfigurationAuthority _configurationAuthority = null;

	/**
	 * Diese Liste enth�lt alle Abh�ngigkeiten des Bereichs. Die Abh�ngigkeiten sind dabei in der Reihenfolge ihres Auftretens gespeichert. Der letzte Eintrag ist
	 * somit die letzte Abh�ngigkeite, die eingetragen wurde (und somit die aktuellste).
	 */
	private final List<ConfigurationAreaDependency> _areaDependencyList = new ArrayList<ConfigurationAreaDependency>();

	/**
	 * Mit dieser Verwendung kann ein Datensatz engefordert werden, der die Abh�ngigkeiten dieses Bereichs enth�lt. Ist der Wert <code>null</code>, so ist die
	 * ben�tigte ATG nicht bekannt und der Mechanismus wird nicht benutzt.
	 */
	private final AttributeGroupUsage _atguForDependencies;

	/** Mit diesem Enum kann angegeben werden, ob ein dynamisches Objekt, ein Konfigurationsobjekt oder ein konfigurierender Datensatz ge�ndert wurde. */
	enum KindOfLastChange {

		DynamicObject,
		ConfigurationObject,
		ConfigurationData;
	}

	/**
	 * Konstruktor eines System-Objekt f�r einen Konfigurationsbereich.
	 *
	 * @param dataModel        das Datenmodell, welches alle Konfigurationsbereiche enth�lt
	 * @param systemObjectInfo das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigConfigurationArea(DataModel dataModel, SystemObjectInformationInterface systemObjectInfo) {
		super(null, systemObjectInfo);
		_dataModel = dataModel;

		final AttributeGroup attributeGroup = _dataModel.getAttributeGroup("atg.konfigurationsBereichAbh�ngigkeiten");
		if(attributeGroup != null) {
			_atguForDependencies = attributeGroup.getAttributeGroupUsage(
					_dataModel.getAspect(
							"asp.eigenschaften"
					)
			);
			loadDependencyDataSets();
		}
		else {
			// Die Konfiguration unterst�tzt die ATG nicht
			_atguForDependencies = null;
		}
	}

	public DataModel getDataModel() {
		return _dataModel;
	}

	public ConfigurationArea getConfigurationArea() {
		return this;
	}

	public synchronized ConfigurationAuthority getConfigurationAuthority() {
		// Der KV wird hier gecached, damit sichergestellt ist, dass sich der KV zur Laufzeit niemals �ndert. Erst bei einem Neustart der Konfiguration
		// kann er sich �ndern.
		if(_configurationAuthority == null) {
			final AttributeGroup atg = getDataModel().getAttributeGroup("atg.konfigurationsBereichEigenschaften");
			if(atg == null) throw new IllegalStateException("Attributgruppe atg.konfigurationsBereichEigenschaften wurde nicht gefunden");
			final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
			if(asp == null) throw new IllegalStateException("Aspekt asp.eigenschaften wurde nicht gefunden");
			final Data data = getConfigurationData(atg, asp);
			if(data == null) {
				throw new IllegalStateException(
						"Der Datensatz mit dem Konfigurationsverantwortlichen des Konfigurationsbereichs " + getNameOrPidOrId()
						+ " konnte nicht gelesen werden."
				);
			}
			_configurationAuthority = (ConfigurationAuthority)data.getReferenceValue("zust�ndiger").getSystemObject();
			if(_configurationAuthority == null) {
				throw new IllegalStateException(
						"Der Konfigurationsverantwortliche des Konfigurationsbereichs " + getNameOrPidOrId() + " konnte nicht ermittelt werden."
				);
			}
		}
		return _configurationAuthority;
	}

	@Override
	public void setConfigurationData(final AttributeGroupUsage atgUsage, final Data data) throws ConfigurationChangeException {
		// Verhindern, dass der Parameter des Konfigurationsverantwortlichen ge�ndert wird.
		final AttributeGroupUsage usage = getDataModel().getAttributeGroup("atg.konfigurationsBereichEigenschaften").getAttributeGroupUsage(
				getDataModel().getAspect("asp.eigenschaften")
		);
		if(atgUsage.equals(usage)){
			if(!data.getReferenceValue("zust�ndiger").getSystemObject().equals(getConfigurationAuthority())){
				throw new ConfigurationChangeException("Der Konfigurationsverantwortliche eines Konfigurationsbereichs kann nicht ge�ndert werden.");
			}
		}

		// Normale Funktion aufrufen
		super.setConfigurationData(atgUsage, data);
	}

	/**
	 * Wird w�hrend der Aktivierung aufgerufen um den neuen KV zu setzen. Der Weg �ber setConfigurationData() ist nicht m�glich,
	 * da dort das Setzen des KV eine Exception produziert.
	 * @param newAuthority
	 * @throws ConfigurationChangeException
	 */
	public void activateNewAuthority(final SystemObject newAuthority) throws ConfigurationChangeException {
		final AttributeGroup configurationAreaAtg = getDataModel().getAttributeGroup("atg.konfigurationsBereichEigenschaften");
		final Aspect configurationAreaAsp = getDataModel().getAspect("asp.eigenschaften");

		final Data data = getConfigurationData(configurationAreaAtg, configurationAreaAsp);
		data.getReferenceValue("zust�ndiger").setSystemObject(newAuthority); // Zust�ndigen setzen
		data.getReferenceValue("neuerZust�ndiger").setSystemObject(null);    // neuen Zust�ndigen l�schen
		
		super.setConfigurationData(configurationAreaAtg.getAttributeGroupUsage(configurationAreaAsp), data);   // Datensatz speichern
	}

	public short getActivatableVersion() {
		Data data = getConfigurationData(
				getDataModel().getAttributeGroup("atg.konfigurationsBereich�bernahmeInformationen"), getDataModel().getAspect("asp.eigenschaften")
		);
		if(data != null) {
			return data.getUnscaledValue("aktivierbareVersion").shortValue();
		}
		else {
			throw new IllegalStateException("Die aktivierbare Version des Konfigurationsbereichs " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
	}

	/**
	 * Setzt die Version, die zur �bernahme und Aktivierung dieses Konfigurationsbereichs in anderen Konfigurationen freigegeben werden soll.
	 *
	 * @param activatableVersion die Versionsnummer, die zur �bernahme und Aktivierung dieses Konfigurationsbereichs freigegeben werden soll
	 *
	 * @throws ConfigurationChangeException Falls die Versionsnummer nicht am Konfigurationsbereich gespeichert werden kann.
	 */
	void setActivatableVersion(short activatableVersion) throws ConfigurationChangeException {
		final AttributeGroup atg = getDataModel().getAttributeGroup("atg.konfigurationsBereich�bernahmeInformationen");
		Data data = getConfigurationData(atg);
		// Datensatz muss vorhanden sein, da ein neuer Bereich immer mit diesem Datensatz erzeugt wird.
		if(data != null) {
			data.getUnscaledValue("aktivierbareVersion").set((short)activatableVersion);
			setConfigurationData(atg, data);
		}
		else {
			throw new ConfigurationChangeException(
					"Die Version, die zur �bernahme und Aktivierung freigegeben werden soll, konnte nicht geschrieben werden, da der Datensatz nicht vorhanden ist."
			);
		}
	}

	public short getTransferableVersion() {
		Data data = getConfigurationData(
				getDataModel().getAttributeGroup("atg.konfigurationsBereich�bernahmeInformationen"), getDataModel().getAspect("asp.eigenschaften")
		);
		if(data != null) {
			return data.getUnscaledValue("�bernehmbareVersion").shortValue();
		}
		else {
			throw new IllegalStateException("Die �bernehmbare Version des Konfigurationsbereichs " + getNameOrPidOrId() + " konnte nicht ermittelt werden.");
		}
	}

	/**
	 * Setzt die Version, die zur �bernahme dieses Konfigurationsbereichs in anderen Konfigurationen freigegeben werden soll.
	 *
	 * @param transferableVersion die Versionsnummer, die zur �bernahme dieses Konfigurationsbereichs freigegeben werden soll
	 *
	 * @throws ConfigurationChangeException Falls die Versionsnummer nicht am Konfigurationsbereich gespeichert werden kann.
	 */
	void setTransferableVersion(short transferableVersion) throws ConfigurationChangeException {
		final AttributeGroup atg = getDataModel().getAttributeGroup("atg.konfigurationsBereich�bernahmeInformationen");
		Data data = getConfigurationData(atg);
		// Datensatz muss vorhanden sein, da ein neuer Bereich immer mit diesem Datensatz erzeugt wird.
		if(data != null) {
			data.getUnscaledValue("�bernehmbareVersion").set((short)transferableVersion);
			setConfigurationData(atg, data);
		}
		else {
			throw new ConfigurationChangeException(
					"Die Version, die zur �bernahme freigegeben werden soll, konnte nicht geschrieben werden, da der Datensatz nicht vorhanden ist."
			);
		}
	}

	public short getActiveVersion() {
		return _dataModel.getActiveVersion(getConfigurationArea());
	}

	public short getModifiableVersion() {
		// ver�nderbare Version
		final ConfigurationAreaFile areaFile = ((ConfigDataModel)getDataModel()).getConfigurationFileManager().getAreaFile(getPid());
		return areaFile.getNextActiveVersion();
	}

	/**
	 * Gibt die in Bearbeitung befindliche Version zur�ck, wenn �nderungen in dieser Version dieses Bereichs stattgefunden haben. �nderungen k�nnen sein: <ul>
	 * <li>Objekt wurde auf ung�ltig gesetzt</li> <li>Objekt wurde erstellt</li> <li>Elemente wurden einer Menge hinzugef�gt</li> <li>Elemente wurden aus einer
	 * Menge entfernt</li> </ul>
	 * <p/>
	 * Wurden keine �nderungen in der in Bearbeitung befindlichen Version vorgenommen, wird die Version davor zur�ckgegeben.
	 *
	 * @return Falls �nderungen in der in Bearbeitung befindlichen Version durchgef�hrt wurden, wird diese Version zur�ckgegeben, sonst die Version davor.
	 */
	public short getLastModifiedVersion() {
		final short modifiableVersion = getModifiableVersion();

		// Hier wird gepr�ft, ob ein aktuelles Objekt in der "modifiableVersion" auf ung�ltig gesetzt wurde.
		// Ist dies der Fall, wird die "modifiableVersion" zur�ckgegeben.
		final Collection<SystemObject> currentObjects = getCurrentObjects();
		for(SystemObject currentObject : currentObjects) {
			// Dynamische Objekte m�ssen nicht gepr�ft werden, da sie sofort g�ltig sind
			if(currentObject instanceof ConfigurationObject) {
				final ConfigurationObject configurationObject = (ConfigurationObject)currentObject;
				if(configurationObject.getNotValidSince() == modifiableVersion) {
					// dieses Objekt wurde auf ung�ltig gesetzt
					return modifiableVersion;
				}
			}
		}
		// Kein aktuelles Objekt wurde in der "modifiableVersion" auf ung�ltig gesetzt

		// Hier werden die zuk�nftig aktuellen Objekte gepr�ft, ob sie in der "modifiableVersion auf ung�ltig gesetzt oder erstellt wurde.
		// Ist dies der Fall, wird die "modifiableVersion" zur�ckgegeben.
		final Collection<SystemObject> newObjects = getNewObjects();
		for(SystemObject newObject : newObjects) {
			// da die Methode getNewObjects() nur KonfigurationsObjekte zur�ckgibt, muss hier nicht gepr�ft werden
			final ConfigurationObject configurationObject = (ConfigurationObject)newObject;
			if(configurationObject.getNotValidSince() == modifiableVersion || configurationObject.getValidSince() == modifiableVersion) {
				// es gibt zuk�nftige Objekte, die in der "modifiableVersion" ung�ltig werden
				// es gibt neu erstellte Objekte in der Version "modifiableVersion"
				return modifiableVersion;
			}
		}

		// Pr�ft die Mengen mit Referenzierungsart "Assoziation" dieses Bereichs, ob Elemente in
		// der Version "modifiableVersion" ung�ltig oder neu hinzugef�gt wurden/werden.
		final Collection<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
		objectTypes.add(getDataModel().getType("typ.konfigurationsMenge"));
		final Collection<SystemObject> systemObjects = getObjects(objectTypes, ObjectTimeSpecification.valid());
		for(SystemObject systemObject : systemObjects) {
			if(systemObject instanceof ConfigNonMutableSet) {
				final ConfigNonMutableSet nonMutableSet = (ConfigNonMutableSet)systemObject;
				// nur versionierte Mengen m�ssen betrachtet werden
				if(nonMutableSet.getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION) {
					// dieser Menge wurde in der "modifiableVersion" mindestens ein Element hinzugef�gt oder entfernt
					if(nonMutableSet.isSetChanged(modifiableVersion)) return modifiableVersion;
				}
			}
			else {
				_debug.error("Element vom Typ 'typ.konfigurationsMenge' ist kein konfigurierender MengenTyp: ", systemObject);
				throw new IllegalStateException("Element vom Typ 'typ.konfigurationsMenge' ist kein konfigurierender MengenTyp: " + systemObject.getPid());
			}
		}

		// Pr�fung auf unversionierte �nderungen
		AttributeGroup attributeGroup = _dataModel.getAttributeGroup("atg.konfigurationsBereichUnversionierte�nderungen");
		if(attributeGroup != null){
			Data configurationData = getConfigurationData(attributeGroup);
			if(configurationData != null){
				for(Data data : configurationData.getItem("versionen")) {
					if(data.getUnscaledValue("Version").shortValue() == modifiableVersion) return modifiableVersion;
				}
			}
		}

		// Weder ein aktuelles noch ein zuk�nftiges Objekt oder eine Menge wurde in der "modifiableVersion" ge�ndert.
		// Aus diese Grund, wird die "modifiableVersion" - 1 zur�ckgegeben.
		// MAX(ActiveVersion, ActivatableVersion, TransferableVersion) = ModifiableVersion - 1;
		return (short)(modifiableVersion - 1);
	}

	public long getTimeOfLastDynamicChange() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten"));
		if(data != null) {
			return data.getTimeValue("Letzte�nderungszeitDynamischesObjekt").getMillis();
		}
		else {
			return 0;
		}
	}

	public long getTimeOfLastNonActiveConfigurationChange() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten"));
		if(data != null) {
			return data.getTimeValue("Letzte�nderungszeitKonfigurationsObjekt").getMillis();
		}
		else {
			return 0;
		}
	}

	public long getTimeOfLastActiveConfigurationChange() {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten"));
		if(data != null) {
			return data.getTimeValue("Letzte�nderungszeitDatensatz").getMillis();
		}
		else {
			return 0;
		}
	}

	public Collection<SystemObject> getObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification) {
		// alle Objekt-Typen, die zu betrachten sind, ermitteln - also auch die Typen, die die angegebenen Typen erweitern
		final Set<SystemObjectType> relevantObjectTypes = new HashSet<SystemObjectType>();
		if(systemObjectTypes == null) {
			// es wurde keine Einschr�nkung der Typen gegeben -> alle Typen ermitteln
			// die Typen eines TypTyp-Objekts ermittelt man mit getElements()!
			relevantObjectTypes.add(getDataModel().getTypeTypeObject());
			final List<SystemObject> typeTypeElements = getDataModel().getTypeTypeObject().getElements();
			for(SystemObject typeTypeElement : typeTypeElements) {
				relevantObjectTypes.add((SystemObjectType)typeTypeElement);
			}
		}
		else {
			// die Typen eines Typ-Objekts werden �ber die Sub-Types ermittelt.
			for(SystemObjectType objectType : systemObjectTypes) {
				relevantObjectTypes.add(objectType);
				relevantObjectTypes.addAll(((ConfigDataModel)getDataModel()).getAllSubTypes(objectType));
			}
		}
		return getDirectObjects(relevantObjectTypes, timeSpecification);
	}

	public Collection<SystemObject> getDirectObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification) {
		// Parameter null ist hier nicht erlaubt
		if(systemObjectTypes == null || timeSpecification == null) {
			throw new IllegalArgumentException("Parameter 'null' ist hier nicht erlaubt.");
		}

		// speichert alle IDs der zu betrachtenden Typen ab
		final Collection<Long> typeIds = new ArrayList<Long>();
		// Typ-IDs ermitteln
		for(SystemObjectType objectType : systemObjectTypes) {
			typeIds.add(objectType.getId());
		}

		// Anfangs- und Endzeit ermitteln
		long startTime;
		long endTime;
		if(timeSpecification.getType() == TimeSpecificationType.VALID) {
			startTime = System.currentTimeMillis();
			endTime = startTime;
		}
		else if(timeSpecification.getType() == TimeSpecificationType.VALID_AT_TIME) {
			startTime = timeSpecification.getTime();
			endTime = timeSpecification.getTime();
		}
		else {
			startTime = timeSpecification.getStartTime();
			endTime = timeSpecification.getEndTime();
		}

		// Konfigurationsbereichsdatei ermitteln
		ConfigurationAreaFile areaFile = ((ConfigDataModel)getDataModel()).getConfigurationFileManager().getAreaFile(getConfigurationArea().getPid());

		// lokale Zeit f�r aktivierte Versionen (LOCAL_ACTIVATION_TIME)
		SystemObjectInformationInterface[] systemObjectInfos = areaFile.getObjects(
				startTime, endTime, ConfigurationAreaTime.LOCAL_ACTIVATION_TIME, timeSpecification.getType(), typeIds
		);

		// SystemObjekte holen
		final Collection<SystemObject> objects = new ArrayList<SystemObject>();
		for(SystemObjectInformationInterface systemObjectInfo : systemObjectInfos) {
			SystemObject systemObject = ((ConfigDataModel)getDataModel()).createSystemObject(systemObjectInfo);
			objects.add(systemObject);
		}
		return Collections.unmodifiableCollection(objects);
	}

	public Collection<SystemObject> getCurrentObjects() {
		return ((ConfigDataModel)getDataModel()).getCurrentObjects(getConfigurationArea());
	}

	public Collection<SystemObject> getNewObjects() {
		return ((ConfigDataModel)getDataModel()).getNewObjects(getConfigurationArea());
	}

	public ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List<ObjectSet> sets)
			throws ConfigurationChangeException {
		// wenn bei der pid oder dem namen "null" �bergeben wird, wird der Leerstring ("") verwendet.
		if(pid == null) pid = "";
		if(name == null) name = "";
		// 1. pr�fen, ob �berhaupt ge�ndert werden darf
		{
			if(checkChangePermit()) {

				// Namen und Pids d�rfen nur eine L�nge von 255 Zeichen besitzen
				checkNameLength(name);
				checkPidLength(pid);

				// gibt es bereits ein Objekt mit der angegebenen Pid in diesem Bereich, so wird kein neues Objekt angelegt, sondern der Vorgang mit einer
				// ConfigurationChangeException abgebrochen
				final ConfigDataModel configDataModel = (ConfigDataModel)getDataModel();
				final ConfigurationFileManager fileManager = configDataModel.getConfigurationFileManager();

				try {
					short modifiableVersion = getModifiableVersion();
					short transferableVersion = getTransferableVersion();

					// wurde keine Pid angegeben, so muss auch nicht �berpr�ft werden (z.B. beim Erstellen von Mengen)
					if(!pid.equals("")) {
						SystemObjectInformationInterface[] systemObjectInfos = fileManager.getNewObjects(pid);
						if(systemObjectInfos.length > 0) {
							// zuk�nftige Elemente �berpr�fen
							for(SystemObjectInformationInterface systemObjectInfo : systemObjectInfos) {
								ConfigurationObjectInfo configurationObjectInfo = (ConfigurationObjectInfo)systemObjectInfo;
								if(configurationObjectInfo.getFirstValidVersion() > transferableVersion && (
										configurationObjectInfo.getFirstInvalidVersion() == 0
										|| configurationObjectInfo.getFirstInvalidVersion() > modifiableVersion)) {
									throw new IllegalStateException(
											"Es existiert bereits ein zuk�nftiges Objekt mit gleicher Pid '" + pid + "' in diesem Bereich "
											+ getConfigurationArea() + ": " + systemObjectInfo
									);
								}
							}
						}
					}
					// ein zuk�nftiges kollidierendes Objekt existiert nicht
					// aktuell g�ltiges Objekt �berp�fen
					SystemObject existAnotherObject = getDataModel().getObject(pid);
					if(existAnotherObject != null && existAnotherObject.getConfigurationArea() == getConfigurationArea()
					   && !existAnotherObject.getType().isConfigurating()) {
						throw new IllegalStateException(
								"Es existiert bereits ein aktuelles dynamisches Objekt mit gleicher Pid '" + pid + "' in diesem Bereich "
								+ getConfigurationArea()
						);
					}
				}
				catch(IllegalStateException ex) {
					// neue ConfigurationChangeException werfen
					final String errorMessage = "Es konnte kein neues konfigurierendes Objekt angelegt werden";
					_debug.error(errorMessage, ex.getMessage());
					throw new ConfigurationChangeException(errorMessage, ex);
				}

				// 2. n�chste freie ID ermitteln
				long nextObjectId = configDataModel.getNextObjectId();
				final ConfigurationAreaFile areaFile = fileManager.getAreaFile(getPid());
				final ConfigurationObjectInfo objectInfo = areaFile.createConfigurationObject(nextObjectId, type.getId(), pid, name);
				setTimeOfLastChanges(KindOfLastChange.ConfigurationObject);
				// die Mengen werden in einem konfigurierenden Datensatz mit der Attributgruppenverwendungs-ID "-1" abgespeichert
				if(sets != null) createConfigurationDataForSets(objectInfo, sets, getSerializerVersion());

				final ConfigurationObject configObject = (ConfigurationObject)configDataModel.createSystemObject(objectInfo);
				_debug.fine("Neues konfigurierendes Objekt angelegt: " + configObject.getPidOrNameOrId() + " Id: " + configObject.getId());
				return configObject;
			}
			else {
				throw new ConfigurationChangeException(
						"F�r diesen Konfigurationsbereich " + getNameOrPidOrId()
						+ " ist die Konfiguration nicht berechtigt ein neues KonfigurationsObjekt anzulegen."
				);
			}
		}
	}

	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name) throws ConfigurationChangeException {
		return createDynamicObject(type, pid, name, null, (short)0, false);
	}

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden.
	 * Das neue Objekt wird sofort g�ltig.
	 *
	 * @param type              Typ des neuen Objekts
	 * @param pid               PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name              Name des neuen Objekts (kann sp�ter ver�ndert werden)
	 * @param simulationVariant Simulationsvariante des neuen Objekts
	 *
	 * @return Stellvertreterobjekt f�r das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 */
	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name, short simulationVariant) throws ConfigurationChangeException {
		return createDynamicObject(type, pid, name, null, simulationVariant, false);
	}

	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name, Collection<DataAndATGUsageInformation> data)
			throws ConfigurationChangeException {
		return createDynamicObject(type, pid, name, data, (short)0, true);
	}

	public Collection<ConfigurationAreaDependency> getDependencyFromOtherConfigurationAreas() {
		final List<ConfigurationAreaDependency> copy = new ArrayList<ConfigurationAreaDependency>();
		copy.addAll(_areaDependencyList);
		return copy;
	}

	/**
	 * Diese Methode pr�ft, ob eine Abh�ngigkeit gegen�ber einer anderen Abh�ngigkeite neuere Informationen enth�lt, bei diesem Vergleich wird die Art der
	 * Abh�ngigkeit nicht ber�cksichtigt. Ein Vergleich ist nur dann sinnvoll, wenn beide Abh�ngigkeiten den selben Bereich referenzieren.
	 * <p/>
	 * <p/>
	 * Definition: Eine Abh�ngigkeit(A_neu) enth�lt gegen�ber einer anderen Abh�ngigkeit(A_alt) neuere Informationen, wenn:<br> <ul> <li>1) Die Version in der die
	 * Abh�ngigkeite entdeckt wurde in A_Neu gr��er ist, als die der Abh�ngigkeit A_alt. Zus�tzlich muss noch die Version, in der der abh�ngige Bereich ben�tigt
	 * wird, gr��er sein als die aktuell gespeicherte Version. Oder aber A_alt war optional und A_neu ist notwendig. </li> <li>2) Sind die in 1) genannten
	 * Versionen gleich, so wird dann eine neuere Information gefunden, wenn die Version, in der der Abh�ngige Bereich ben�tigt wird, von A_neu gr��er ist als die
	 * Version, die in A_alt gespeichert ist.</li> <li></li> <li></li> </ul>
	 *
	 * @param oldAreaDependency   Abh�ngigkeit mit vermeindlich �lteren Informationen
	 * @param newerAreaDependency Abh�ngigkeit mit vermeindlich neueren Informationen als <code>oldAreaDependency</code>
	 *
	 * @return <code>true</code>, wenn newerAreaDependency neuere Informationen enth�lt als <code>oldAreaDependency</code>; <code>false</code>, sonst.
	 */
	private boolean hasNewVersionInformation(ConfigurationAreaDependency oldAreaDependency, ConfigurationAreaDependency newerAreaDependency) {
		if(newerAreaDependency.getDependencyOccurredAtVersion() > oldAreaDependency.getDependencyOccurredAtVersion()) {
			// 1)
			if(newerAreaDependency.getNeededVersion() > oldAreaDependency.getNeededVersion()) {
				return true;
			}
			else if(hasNewStateInformation(oldAreaDependency, newerAreaDependency) == true) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			// 2
			if(newerAreaDependency.getDependencyOccurredAtVersion() == oldAreaDependency.getDependencyOccurredAtVersion()) {
				if(newerAreaDependency.getNeededVersion() > oldAreaDependency.getNeededVersion()) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				// Die Version, in der die Abh�ngigkeit entdeckt wurde ist kleiner. Also kann sie keine neueren Infos besitzen.
				// 2)
				return false;
			}
		}
	}

	/**
	 * Pr�ft, ob die Art der Abh�ngigkeite eine neue Information enth�lt. Definition: Eine neue Abh�ngigkeite A_neu enth�lt dann eine neuere Information als eine
	 * alte Abh�ngigkeite A_alt wenn: <ul> <li>1) Die Abh�ngigkeite A_neu notwendig ist und die alte Abh�ngigkeit A_alt optional</li> </ul>
	 *
	 * @param oldAreaDependency   Abh�ngigkeit, die bisher gespeichert wurde.
	 * @param newerAreaDependency Abh�ngigkeit, die vielleicht gespeichert werden soll
	 *
	 * @return <code>true</code>, wenn die neue Abh�ngigkeite neue Informationen enth�lt; <code>false</code>, sonst.
	 */
	private boolean hasNewStateInformation(ConfigurationAreaDependency oldAreaDependency, ConfigurationAreaDependency newerAreaDependency) {
		if(newerAreaDependency.getKind() == ConfigurationAreaDependencyKind.REQUIRED
		   && oldAreaDependency.getKind() == ConfigurationAreaDependencyKind.OPTIONAL) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Erzeugt eine neue Abh�ngigkeit die "notwendig" ist.
	 *
	 * @param areaDependence Abh�ngigkeit, die kopiert wird.
	 *
	 * @return Kopie von <code>areaDependence</code> mit der Eigenschaft notwendig.
	 */
	private ConfigurationAreaDependency createRequiredDependence(ConfigurationAreaDependency areaDependence) {
		return new ConfigurationAreaDependency(
				areaDependence.getDependencyOccurredAtVersion(),
				areaDependence.getNeededVersion(),
				areaDependence.getDependantArea(),
				ConfigurationAreaDependencyKind.REQUIRED
		);
	}

	/**
	 * Speichert eine Abh�ngigkeit dieses Bereichs. Besteht bereits eine Abh�ngigkeit (gleiche Versionen, gleiche Breiche), so wird die Abh�ngigkeit nicht erneut
	 * gespeichert.
	 * <p/>
	 * Wurde eine Abh�ngigkeit gespeichert, die den Typ OPTIONAL {@link ConfigurationAreaDependencyKind} und es soll eine Abh�ngigkeit REQUIRED gespeichert werden,
	 * so wird die optionale Abh�ngigkeit ersetzt. Umgekehrt ist dies nicht m�glich.
	 * <p/>
	 * <p/>
	 * Ist der Bereich bereits von einem anderen Bereich abh�ngig (notwendig) und es soll eine Abh�ngigkeit optional hinzugef�gt werden (dessen needed Version >
	 * als die notwendige Abh�ngigkeit), so wird diese Abh�ngigkeit automatisch auf "notwendig" gesetzt. Damit wird verhindert, das eine Abh�ngigkeit, die
	 * notwendig war, durch Versionswechsel wieder auf optional gesetzt werden kann.
	 * <p/>
	 * Ist der Konfiguration die Attributgruppe zum speichern der Datens�tze nicht bekannt, so die Abh�ngigkeiten ignoriert und nichts gemacht.
	 *
	 * @param areaDependencies Abh�ngigkeiten, die gespeichert werden soll.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Wird geworfen, wenn der Datensatz, der die Abh�ngigkeiten enth�lt, nicht gespeichert werden kann.
	 */
	synchronized public void addAreaDependency(Collection<ConfigurationAreaDependency> areaDependencies) throws ConfigurationChangeException {

		if(_atguForDependencies != null) {
			/**
			 * Speichert die neuste Abh�ngigkeit(value) zu einem Bereich (Key). Der neuste Eintrag wird aus den �bergebenen Abh�ngigkeiten rausgesucht.
			 */
			final Map<String, ConfigurationAreaDependency> newestDependence = new HashMap<String, ConfigurationAreaDependency>();

			for(ConfigurationAreaDependency areaDependency : areaDependencies) {
				// Abh�ngigkeite, die als letztes betrachtet wurde
				final ConfigurationAreaDependency lastFoundDependence = newestDependence.get(areaDependency.getDependantArea());

				if(lastFoundDependence != null) {
					// Enth�lt die neue Abh�ngigkeit neuere Informationen?

					if(hasNewVersionInformation(lastFoundDependence, areaDependency) == true) {
						// Die neue Abh�ngigkeit enth�lt neuere Informationen (h�here Versionsnummern).
						// Also muss die alte Abh�ngigkeite gel�scht werden und durch die neue ersetzt werden. Dabei ist zu beachten, ob die
						// Abh�ngigkeiten notwendig sind.
						if(lastFoundDependence.getKind() == ConfigurationAreaDependencyKind.REQUIRED) {
							// Die alte Abh�ngigkeite war notwendig, also sind alle anderen auch notwendig.
							newestDependence.put(areaDependency.getDependantArea(), createRequiredDependence(areaDependency));
						}
						else {
							// Die alte Abh�ngigkeit war optional, also kann die neue Abh�ngigkeite die alte einfach ersetzen.
							newestDependence.put(areaDependency.getDependantArea(), areaDependency);
						}
					}
					else if(hasNewStateInformation(lastFoundDependence, areaDependency) == true) {
						// Die Versionen bringen keine neuen Informationen, aber die neue Abh�ngigkeite war notwendig und die bisher gespeicherte
						// war nur optional. Also muss die bisher gespeicherte Abh�ngigkeit auf "notwendig" ge�ndert werden
						newestDependence.put(lastFoundDependence.getDependantArea(), createRequiredDependence(lastFoundDependence));
					}
				}
				else {
					// bisher gab es zu dem Bereich noch keine Abh�ngigkeit
					newestDependence.put(areaDependency.getDependantArea(), areaDependency);
				}
			} // Alle �bergebenen Abh�ngigkeiten nach der Abh�ngigkeit durchsuchen, die die neuste darstellt

			// An dieser Stelle steht in der Map <code>newestDependence</code> zu jedem abh�ngigen Bereich die jeweils neuste
			// Abh�ngigkeite, die �bergeben wurde.
			// Nun kann mit der zuletzt gespeicherten Abh�ngigkeit verglichen werden.

			// Enth�lt die Abh�ngigkeit aus der Map neuere Informationen als die bisher gespeicherte Abh�ngigkeite, wird
			// die neue Abh�ngigkeite zus�tzlich gespeichert.

			final Collection<ConfigurationAreaDependency> possibleNewDependencies = newestDependence.values();

			// Alle bisher gespeicherte Abh�ngigkeiten werden in _areaDependencyList in der Reihenfolge ihres eintreffens gespeichert.
			// Die Liste muss also vom Ende zum Anfang untersucht werden.

			// Die G��e der Liste muss gespeichert werden, da die neuen Elemente direkt in die Liste eingef�gt werden.
			final int oldSize = _areaDependencyList.size() - 1;

			// Diese Liste speichert alle Abh�ngigkeiten, die an dem bestehenden Datensatz zus�tzlich gespeichert werden m�ssen. Alle Versionen und
			// vorallem die Art der Abh�ngigkeit sind bereits richtig gesetzt.
			final Collection<ConfigurationAreaDependency> saveNewAreaDependencies = new ArrayList<ConfigurationAreaDependency>();

			for(ConfigurationAreaDependency possibleNewDependence : possibleNewDependencies) {
				// Wird ben�tigt um zu erkennen, dass eine neue Abh�ngigkeite eingef�gt werden muss (bisher gab es zu dem Bereich noch keine Abh�ngigkeit)
				boolean areaFound = false;
				for(int nr = oldSize; nr >= 0; nr--) {
					// Eine Abh�ngigkeite, die schon gespeichert wurde
					final ConfigurationAreaDependency savedDependence = _areaDependencyList.get(nr);

					// Wenn die abh�ngigen Bereiche gleich sind kann verglichen werden.
					if(possibleNewDependence.getDependantArea().equals(savedDependence.getDependantArea())) {
						// Der Bereich stimmt. Da die neusten Abh�ngigkeiten immer an das Ende der Liste geh�ngt werden, kann die Suche hier enden.
						areaFound = true;

						// Nun muss gepr�ft werden, ob die neue Abh�ngigkeit �berhaupt neue Informationen beinhaltet.
						if(hasNewVersionInformation(savedDependence, possibleNewDependence) == true) {
							// Wenn die gespeicherte Abh�ngigkeite notwendig gewesen ist, so muss es auch die neue sein.
							if(savedDependence.getKind() == ConfigurationAreaDependencyKind.REQUIRED) {
								saveNewAreaDependencies.add(createRequiredDependence(possibleNewDependence));
							}
							else {
								// Die Abh�ngigkeit kann so gespeichert werden wie sie ist.
								saveNewAreaDependencies.add(possibleNewDependence);
							}
						}
						else if(hasNewStateInformation(savedDependence, possibleNewDependence) == true) {
							// Die Versionen unterscheiden sich nicht, aber die alte Abh�ngigkeite war optional und die neue ist
							// notwendig. Die alte Abh�ngigkeite wird nicht ge�ndert.
							saveNewAreaDependencies.add(possibleNewDependence);
						}// Im else-Fall muss nichts gemacht werden, die neue Abh�ngigkeite bringt keine neuen Informationen und muss somit nicht gespeichert werden.

						break;
					}
				}// alle gespeicherten Bereiche betrachten

				if(areaFound == false) {
					// Es gab zu dem Bereich noch keine Abh�ngigkeit. Dies ist die erste Abh�ngigkeit zu dem Bereich.
					saveNewAreaDependencies.add(possibleNewDependence);
				}
			}// Pr�fung, ob die gefundenen Abh�ngigkeiten �berhaupt gespeichert werden m�ssen.

			// Die gefundenen Abh�gigkeiten in die Datenstruktur eintragen und dann an den bestehnden Datensatz anh�ngen.
			_areaDependencyList.addAll(saveNewAreaDependencies);
			writeDependencyDataSet(saveNewAreaDependencies);
		}// Wird eine alte Konfiguration ohne die ATG benutzt, wird nichts gemacht. Da der Datensatz nicht geschrieben werden kann.
	}

	public boolean dependenciesChecked() {
		if(_atguForDependencies != null) {
			return getConfigurationData(_atguForDependencies) != null;
		}
		else {
			// Dieser Fallunterschied ist eigentlich nicht n�tig, da getConfigurationData() null zur�ck gibt, wenn null �bergeben wird.
			// Aber diese Implementierung garantiert ein gleichbleibendes Verhalten.
			return false;
		}
	}

	/**
	 * Schreibt einen Datensatz, der alle Abh�ngigkeiten dieses Bereich zu anderen Bereichen speichert. Gibt es bereits einen Datensatz, wird dieser um die neuen
	 * Abh�ngigkeiten erweitert. Die neuen Abh�ngigkeiten werden ans Ende des Arrays geschrieben.
	 *
	 * @param areaDependencies Neue Abh�ngigkeiten, die am Ende des Datensatzes (Array) eingef�gt werden sollen.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Der Datensatz darf nicht ge�ndert werden, weil der Konfigurationsverantwortliche der Konfiguration keinen Zugriff auf diesen Bereich hat.
	 */
	synchronized private void writeDependencyDataSet(final Collection<ConfigurationAreaDependency> areaDependencies) throws ConfigurationChangeException {

		if(_atguForDependencies != null) {

			// Dies verhindert, dass der Datensatz erneut geschrieben wird, obwohl es keine neuen Informationen gibt.
			if(areaDependencies.isEmpty() == true) {
				return;
			}

			// Datensatz, der bisher die Abh�ngigkeiten enth�lt
			Data dataSetWithDependencies = getConfigurationData(_atguForDependencies);

			if(dataSetWithDependencies == null) {
				// Der Datensatz wurde bisher noch nicht gesetzt. Die erste Abh�ngigkeit soll angelegt werden. Damit muss ein Datensatz erzeugt werden.
				final Data newDependancyDataSet = AttributeBaseValueDataFactory.createAdapter(
						_atguForDependencies.getAttributeGroup(), AttributeHelper.getAttributesValues(_atguForDependencies.getAttributeGroup())
				);

				setConfigurationData(_atguForDependencies, newDependancyDataSet);
				// Den Datensatz vom Objekt anfordern
				dataSetWithDependencies = getConfigurationData(_atguForDependencies);
			} // Neuen Datensatz anlegen ende

			// Der bestehende Datensatz muss um die neuen Eintr�ge erweitert werden.
			final Data.Array dependencyArray = dataSetWithDependencies.getItem("KonfigurationsAbh�ngigkeiten").asArray();
			final int oldArrayLength = dependencyArray.getLength();
			dependencyArray.setLength(oldArrayLength + areaDependencies.size());

			// Speichert die Stelle, an der der n�chste Datensatz eingef�gt werden soll.
			int indexCounter = oldArrayLength;

			for(ConfigurationAreaDependency dependence : areaDependencies) {
				// Eine Abh�ngigkeit, die im Datensatz gespeichert wird
				final Data oneDependencyInDataSet = dependencyArray.getItem(indexCounter);

				oneDependencyInDataSet.getItem("Abh�ngigkeitEntstandenInVersion").asUnscaledValue().set(dependence.getDependencyOccurredAtVersion());
				oneDependencyInDataSet.getItem("BereichNotwendig").asTextValue().setText(dependence.getDependantArea());
				oneDependencyInDataSet.getItem("VersionNotwendig").asUnscaledValue().set(dependence.getNeededVersion());
				String value = dependence.getKind().getValue();
				value = value.toLowerCase();
				value = value.trim();
				oneDependencyInDataSet.getItem("Kennung").asTextValue().setText(value);

				// Der Wert wurde eingetragen
				indexCounter++;
			}
			setConfigurationData(_atguForDependencies, dataSetWithDependencies);
		} // Wenn eine alte Konfiguration benutzt wird, mache nix
	}

	/**
	 * Diese Methode l�dt alle Abh�ngigkeiten aus dem Datensatz <code>_atguForDependancies</code> in die<code>_areaDependencyList</code>.
	 * <p/>
	 * Der Datensatz speichert die Abh�ngigkeiten in der Reihenfolge LIFO. Der letzte und damit neuste Eintrag befindet sich am Ende.
	 */
	private void loadDependencyDataSets() {

		if(_atguForDependencies != null) {
			final Data dataWithDependencies = getConfigurationData(_atguForDependencies);
			// Gibt es keinen Datensatz, wird nichts gemacht. Sobald es Abh�ngigkeiten gibt, wird der Datensatz automatisch angelegt.
			if(dataWithDependencies != null) {
				final Data.Array allDependenciesInDataSets = dataWithDependencies.getItem("KonfigurationsAbh�ngigkeiten").asArray();

				int index = 0;

				for(int nr = 0; nr < allDependenciesInDataSets.getLength(); nr++) {
					final Data oneDependencyDataSet = allDependenciesInDataSets.getItem(index);
					index++;
					final short dependencyOccuredAt = oneDependencyDataSet.getItem("Abh�ngigkeitEntstandenInVersion").asUnscaledValue().shortValue();

					final String pidDependencyArea = oneDependencyDataSet.getItem("BereichNotwendig").asTextValue().getValueText();

					final short neededVersion = oneDependencyDataSet.getItem("VersionNotwendig").asUnscaledValue().shortValue();
					final ConfigurationAreaDependencyKind configurationAreaDependencyKind = ConfigurationAreaDependencyKind.getInstance(
							oneDependencyDataSet.getItem(
									"Kennung"
							).asTextValue().getText()
					);

					final ConfigurationAreaDependency areaDependency = new ConfigurationAreaDependency(
							dependencyOccuredAt, neededVersion, pidDependencyArea, configurationAreaDependencyKind
					);
					_areaDependencyList.add(areaDependency);
				}
			}
		} // Wenn eine alte Konfiguration benutzt wird, mache nix
	}

	/**
	 * Pr�ft, ob zwei Abh�ngigkeiten identisch sind. Welche Werte genau gepr�ft werden, ist von den �bergabeparametern abh�ngig.
	 *
	 * @param first  Erster Bereich
	 * @param second Zweiter Bereich
	 *
	 * @return <code>true</code>, wenn die beiden Bereiche unter Ber�cksichtigung der Parameter logisch identisch sind.
	 */
	private boolean equalsConfigurationAreaDependancy(final ConfigurationAreaDependency first, ConfigurationAreaDependency second) {
		if(first == second) return true;

		if(first.getDependencyOccurredAtVersion() != second.getDependencyOccurredAtVersion()) {
			return false;
		}

		if(first.getDependantArea().equals(second.getDependantArea())) {
			return false;
		}

		if(first.getNeededVersion() != second.getNeededVersion()) {
			return false;
		}

		if(first.getKind().getCode() != second.getKind().getCode()) {
			return false;
		}

		return true;
	}

	/**
	 * Diese Methode gibt alle eingetragenen Konfigurations�nderungen zur�ck, die in der Versorgungsdatei eingetragen wurden.
	 *
	 * @return Alle eingetragenen �nderungen oder eine leere Collection, falls keine Eintr�ge gemacht wurden.
	 */
	public Collection<ConfigurationAreaChangeInformation> getChangeLogs() {

		final List<ConfigurationAreaChangeInformation> logs = new ArrayList<ConfigurationAreaChangeInformation>();

		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.konfigurations�nderungen");
		Data data = _dataModel.getConfigurationArea(getPid()).getConfigurationData(atg);
		if(data != null) {
			// Data auslesen und Array erzeugen
			final Data.Array array = data.getArray("Konfigurations�nderung");
			for(int i = 0; i < array.getLength(); i++) {
				Data item = array.getItem(i);
				logs.add(
						new ConfigurationAreaChangeInformation(
								item.getTimeValue("Stand").getMillis(),
								item.getUnscaledValue("Version").intValue(),
								item.getTextValue("Autor").getText(),
								item.getTextValue("Grund").getText(),
								item.getTextValue("Text").getText()
						)
				);
			}
		}

		return logs;
	}

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden.
	 * Das neue Objekt wird sofort g�ltig. Zus�tzlich k�nnen alle Datens�tze �bergeben werden.
	 *
	 * @param type              Typ des neuen Objekts
	 * @param pid               PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name              Name des neuen Objekts (kann sp�ter ver�ndert werden)
	 * @param data              Datens�tze f�r das dynamische Objekt
	 * @param simulationVariant Simulationsvariante des neuen Objekts
	 *
	 * @return Stellvertreterobjekt f�r das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 */
	public DynamicObject createDynamicObject(
			DynamicObjectType type, String pid, String name, Collection<DataAndATGUsageInformation> data, short simulationVariant
	) throws ConfigurationChangeException {
		return createDynamicObject(type, pid, name, data, simulationVariant, true);
	}

	/**
	 * Pr�ft, ob ein Name l�nger als 255 Zeichen ist.
	 *
	 * @param name Name oder <code>null</code>
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException Der Name ist l�nger als 255 Zeichen
	 */
	private void checkNameLength(final String name) throws ConfigurationChangeException {
		if(name != null) {
			if(name.length() <= 255) {
				return;
			}
			else {
				throw new ConfigurationChangeException("Der Name ist l�nger als 255 Zeichen " + name + " L�nge " + name.length());
			}
		}
	}

	/**
	 * Pr�ft, ob die Pid l�nger als 255 Zeichen ist.
	 *
	 * @param pid Pid oder <code>null</code>
	 *
	 * @throws ConfigurationChangeException Die Pid ist l�nger als 255 Zeichen
	 */
	private void checkPidLength(final String pid) throws ConfigurationChangeException {
		if(pid != null) {
			if(pid.length() <= 255) {
				return;
			}
			else {
				throw new ConfigurationChangeException("Die Pid ist l�nger als 255 Zeichen " + pid + " L�nge " + pid.length());
			}
		}
	}

	/**
	 * Erzeugt ein neues dynamisches System-Objekt eines vorgegebenen Typs mit einer angegebenen PID. Optional kann der Name des neuen Objekts vorgegeben werden.
	 * Das neue Objekt wird sofort g�ltig. Zus�tzlich k�nnen alle Datens�tze �bergeben werden.
	 *
	 * @param type              Typ des neuen Objekts
	 * @param pid               PID des neuen Objekts. Der leere String ("") oder <code>null</code> wird als "keine PID" interpretiert.
	 * @param name              Name des neuen Objekts (kann sp�ter ver�ndert werden)
	 * @param data              Datens�tze f�r das dynamische Objekt
	 * @param simulationVariant Simulationsvariante des neuen Objekts
	 * @param checkDatasets     gibt an, ob gepr�ft werden soll, ob die notwendigen Datens�tze vorhanden sind
	 *
	 * @return Stellvertreterobjekt f�r das neu angelegte dynamische Objekt.
	 *
	 * @throws ConfigurationChangeException Wenn das Objekt nicht erzeugt werden konnte.
	 */
	private DynamicObject createDynamicObject(
			final DynamicObjectType type,
			String pid,
			String name,
			final Collection<DataAndATGUsageInformation> data,
			final short simulationVariant,
			final boolean checkDatasets
	) throws ConfigurationChangeException {
		// wenn bei der pid oder dem namen "null" �bergeben wird, wird der Leerstring ("") verwendet.
		if(pid == null) pid = "";
		if(name == null) name = "";
		if(checkChangePermit()) {

			// Namen und Pids d�rfen nur eine L�nge von 255 Zeichen besitzen
			checkNameLength(name);
			checkPidLength(pid);

			// gibt es bereits ein aktives Objekt mit der angegebenen Pid, so wird kein neues Objekt angelegt, sondern der Vorgang mit einer
			// ConfigurationChangeException abgebrochen
			SystemObject anotherObject = getDataModel().getObject(pid);
			if(anotherObject != null && anotherObject.isValid()) {
				throw new ConfigurationChangeException(
						"Es konnte kein neues dynamisches Objekt angelegt werden, da zu der angegebenen Pid '" + pid
						+ "' bereits ein aktives Objekt im Bereich " + getNameOrPidOrId() + " existiert."
				);
			}

			final Set<AttributeGroupUsage> requiredAtgUsages = new HashSet<AttributeGroupUsage>();
			if(data != null && !data.isEmpty()) {
				for(DataAndATGUsageInformation dataAndATGUsageInformation : data) {
					final AttributeGroupUsage atgUsage = dataAndATGUsageInformation.getAttributeGroupUsage();
					if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
					   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
						requiredAtgUsages.add(atgUsage);
					}
				}
			}
			// pr�fen, ob auch alle notwendigen Datens�tze am dynamischen Objekt vorhanden sind
			for(AttributeGroup attributeGroup : type.getAttributeGroups()) {
				for(AttributeGroupUsage atgUsage : attributeGroup.getAttributeGroupUsages()) {
					if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
					   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
						if(!requiredAtgUsages.contains(atgUsage)) {
							final String message = "Es wurden nicht alle notwendigen Datens�tze f�r das dynamische Objekt " + pid + " angegeben.";
							if(checkDatasets) {
								_debug.error(message);
								throw new ConfigurationChangeException(message);
							}
							else {
								_debug.warning(message);
							}
						}
					}
				}
			}

			// neues Objekt wird angelegt
			final ConfigDataModel configDataModel = (ConfigDataModel)getDataModel();
			long nextObjectId = configDataModel.getNextObjectId();
			final ConfigurationAreaFile areaFile = configDataModel.getConfigurationFileManager().getAreaFile(getPid());

			final DynamicObjectInfo objectInfo = areaFile.createDynamicObject(
					nextObjectId, type.getId(), pid, simulationVariant, name, type.getPersistenceMode()
			);
			setTimeOfLastChanges(KindOfLastChange.DynamicObject);


			if(data != null && !data.isEmpty()) {
				// Datens�tze am dynamischen Objekt speichern
				for(DataAndATGUsageInformation dataAndATGUsageInformation : data) {
					final AttributeGroupUsage atgUsage = dataAndATGUsageInformation.getAttributeGroupUsage();
					try {
						final Data configurationData = dataAndATGUsageInformation.getData();
						byte[] bytes = new byte[0];
						if(configurationData != null) {
							final ByteArrayOutputStream out = new ByteArrayOutputStream();
							Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
							serializer.writeData(configurationData);
							bytes = out.toByteArray();
						}
						objectInfo.setConfigurationData(atgUsage.getId(), bytes);
						setTimeOfLastChanges(KindOfLastChange.ConfigurationData);
					}
					catch(Exception ex) {
						final String errorMessage = "Der Datensatz '" + data + "' am Objekt " + getNameOrPidOrId() + " mit der Attributgruppe "
						                            + atgUsage.getAttributeGroup().getNameOrPidOrId() + " und dem Aspekt "
						                            + atgUsage.getAspect().getNameOrPidOrId() + " konnte nicht erstellt werden";
						_debug.error(errorMessage, ex);
						throw new ConfigurationChangeException(errorMessage, ex);
					}
				}
			}

			// beim createSystemObject f�r dynamische Objekte wird (im Konstruktor von ConfigDynamicObject) der Typ benachrichtigt,
			// dass ein neues Objekt angelegt wurde
			final DynamicObject dynamicObject = (DynamicObject)configDataModel.createSystemObject(objectInfo);
			_debug.fine("Neues dynamisches Objekt angelegt: " + dynamicObject.getPidOrNameOrId() + " Id: " + dynamicObject.getId());
			((ConfigDynamicObjectType)type).informCreateListener((DynamicObject)dynamicObject, simulationVariant);
			return dynamicObject;
		}
		else {
			throw new ConfigurationChangeException(
					"Es konnte kein neues dynamisches Objekt f�r diesen Bereich " + getNameOrPidOrId()
					+ " angelegt werden, da die Konfiguration keine Berechtigung hierf�r hat."
			);
		}
	}


	/**
	 * Speichert die angegebenen Mengen an einem Konfigurationsobjekt.
	 *
	 * @param objectInfo        wo der konfigurierende Datensatz mit den Mengen abgespeichert werden soll
	 * @param sets              die Mengen
	 * @param serializerVersion die Version des Serialisierers
	 */
	private void createConfigurationDataForSets(ConfigurationObjectInfo objectInfo, List<ObjectSet> sets, int serializerVersion) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(serializerVersion, out);
			for(ObjectSet objectSet : sets) {
				serializer.writeLong(objectSet.getId());
			}
			objectInfo.setConfigurationData(-1, out.toByteArray()); // die ATGV "-1" ist f�r Mengen fest implementiert
			setTimeOfLastChanges(KindOfLastChange.ConfigurationData);
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Konfigurierender Datensatz konnte nicht geschrieben werden, da es Probleme mit dem Serializer gibt";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
	}

	/**
	 * Gibt die Versionsnummer dieses Konfigurationsbereichs zur�ck, die zur angegebenen Zeit aktiv war.
	 *
	 * @param time die Zeit, zu der die Versionsnummer des Konfigurationsbereichs gesucht wird
	 *
	 * @return die Versionsnummer dieses Konfigurationsbereichs zur angegebenen Zeit oder <code>0</code>, falls der Konfigurationsbereich noch nie aktiviert wurde
	 */
	short getVersionAtAssignedTime(long time) {
		final List<VersionInfo> versionInfos = ((ConfigDataModel)getDataModel()).getVersionInfoOfConfigurationArea(getConfigurationArea());
		for(int i = versionInfos.size() - 1; i >= 0; i--) {	// Schleife durchl�uft die Liste r�ckw�rts
			VersionInfo versionInfo = versionInfos.get(i);
			if(versionInfo.getActivationTime() <= time) return versionInfo.getVersion();
		}
		return 0;
	}

	/**
	 * Gibt die Aktivierungszeit dieses Konfigurationsbereichs zur angegebenen Versionsnummer zur�ck.
	 *
	 * @param version die Versionsnummer, dessen Aktivierungszeit gew�nscht ist
	 *
	 * @return die Aktivierungszeit der angegebenen Version dieses Konfigurationsbereichs oder <code>0</code>, falls zur Version kein Eintrag vorliegt
	 */
	long getTimeAtAssignedVersion(short version) {
		final List<VersionInfo> versionInfos = ((ConfigDataModel)getDataModel()).getVersionInfoOfConfigurationArea(getConfigurationArea());
		for(VersionInfo versionInfo : versionInfos) {
			if(versionInfo.getVersion() == version) return versionInfo.getActivationTime();
		}
		return 0;
	}

	/**
	 * Wurde ein dynamisches Objekt, ein Konfigurationsobjekt (welches noch nicht aktiviert ist) oder ein �nderbarer konfigurierender Datensatz ge�ndert, so wird
	 * die �nderungszeit in dem Attribut des Datensatzes eingetragen und abgespeichert.
	 *
	 * @param kind gibt an, welche der drei Zeiten angepasst werden muss
	 */
	void setTimeOfLastChanges(KindOfLastChange kind) {
		try {
			long time = System.currentTimeMillis();
			// Datensatz holen und wegschreiben
			long timeOfDynamicObject = 0;
			long timeOfConfigurationObject = 0;
			long timeOfConfigurationData = 0;

			// bisherige Daten werden aus dem Byte-Array eingelesen, da ich kein Data habe, wenn noch kein Datensatz existiert.
			final AttributeGroup atg = getDataModel().getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten");
			final Aspect asp = getDataModel().getAspect("asp.eigenschaften");
			final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(asp);
			byte[] bytes = getConfigurationDataBytes(atgUsage);
			if(bytes != null) {
				final ByteArrayInputStream in = new ByteArrayInputStream(bytes);	// falls Daten vorhanden sind werden sie eingelesen
				final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);
				timeOfDynamicObject = deserializer.readLong();
				timeOfConfigurationObject = deserializer.readLong();
				timeOfConfigurationData = deserializer.readLong();
				in.close();
			}

			// welche Zeit hat sich ge�ndert?
			if(kind == KindOfLastChange.DynamicObject) {
				timeOfDynamicObject = time;
			}
			else if(kind == KindOfLastChange.ConfigurationObject) {
				timeOfConfigurationObject = time;
			}
			else if(kind == KindOfLastChange.ConfigurationData) {
				timeOfConfigurationData = time;
			}
			else {
				throw new RuntimeException(
						"Die Zeiten der letzten �nderung eines Bereichs k�nnen nicht ver�ndert werden, da die angegebene Art '" + kind + "'unbekannt ist."
				);
			}

			// neue Daten werden ins Byte-Array geschrieben
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
			serializer.writeLong(timeOfDynamicObject);
			serializer.writeLong(timeOfConfigurationObject);
			serializer.writeLong(timeOfConfigurationData);
			_systemObjectInfo.setConfigurationData(atgUsage.getId(), out.toByteArray());
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der Datensatz mit der letzten �nderung eines Objekts oder Datensatzes konnte am Bereich " + getNameOrPidOrId()
			                            + " nicht geschrieben werden.";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
	}

	/**
	 * Pr�ft, ob es einen Wert im Datensatz <code>atg.konfigurationsBereich�nderungsZeiten</code> gibt, der <code>Undefiniert (0)</code> ist, und ersetzt diesen
	 * mit der aktuellen Zeit.
	 */
	public void initialiseTimeOfLastChanges() {
		if(getTimeOfLastDynamicChange() == 0) setTimeOfLastChanges(KindOfLastChange.DynamicObject);
		if(getTimeOfLastActiveConfigurationChange() == 0) setTimeOfLastChanges(KindOfLastChange.ConfigurationData);
		if(getTimeOfLastNonActiveConfigurationChange() == 0) setTimeOfLastChanges(KindOfLastChange.ConfigurationObject);
	}
}
