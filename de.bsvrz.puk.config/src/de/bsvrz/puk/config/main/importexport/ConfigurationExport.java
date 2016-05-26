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

package de.bsvrz.puk.config.main.importexport;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.puk.config.configFile.datamodel.*;
import de.bsvrz.puk.config.main.dataview.VersionedView;
import de.bsvrz.puk.config.xmlFile.properties.*;
import de.bsvrz.puk.config.xmlFile.writer.ConfigAreaWriter;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

/**
 * Diese Klasse exportiert Konfigurationsbereiche aus dem Datenmodell. Die Bereiche werden in sogenannten Versorgungsdateien abgespeichert.
 * <p>
 * Welche Konfigurationsbereiche exportiert werden sollen und wo sie gespeichert werden, wird im Konstruktor dieser Klasse angegeben.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConfigurationExport {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Betrachtet die Konfigurationsbereiche in unterschiedlichen Versionen. */
	private final ObjectLookup _objectLookup;

	/** Attributgruppe atg.dynamischeMenge oder <code>null</code> falls diese nicht verfügbar ist. */
	final AttributeGroup _dynamicSetPropertiesAtg;

	/**
	 * Konstruktor führt den Export der angegebenen Konfigurationsbereiche durch.
	 *
	 * @param control    liefert die nötigen Zugriffsmethoden auf die Konfiguration
	 * @param exportPath das Verzeichnis, in das die Versorgungsdateien exportiert werden
	 * @param pids       die Pids der zu exportierenden Konfigurationsbereiche
	 *
	 * @throws IOException              Falls die Versorgungsdatei nicht geschrieben werden kann.
	 * @throws IllegalArgumentException Falls zu einer angegebenen Pid kein Konfigurationsbereich existiert.
	 */
	public ConfigurationExport(final ConfigurationControl control, final File exportPath, final Collection<String> pids)
			throws IOException, IllegalArgumentException {
		_debug.config("ConfigurationExport");

		// zu jeder Pid wird der entsprechende Konfigurationsbereich ermittelt
		// gleichzeitig wird geprüft, ob zu jeder Pid auch ein Konfigurationsbereich existiert
		final List<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>();
		final Map<String, ConfigurationArea> allAreas = control.getAllConfigurationAreas();
		for(String pid : pids) {
			ConfigurationArea configurationArea = allAreas.get(pid);
			if(configurationArea == null) {
				// den Konfigurationsbereich gibt es nicht
				final String errorMessage = "Zu dieser Pid '" + pid + "' gibt es keinen Konfigurationsbereich im Datenmodell.";
				_debug.error(errorMessage);
				throw new IllegalArgumentException(errorMessage);
			}
			else {
				configurationAreas.add(configurationArea);
			}
		}

		// Datenmodell ermitteln
		final DataModel dataModel = (DataModel)control;

		_dynamicSetPropertiesAtg = dataModel.getAttributeGroup("atg.dynamischeMenge");
		if(_dynamicSetPropertiesAtg == null) {
			_debug.warning("Warnung Zugriff auf Verwaltungsinformationen von dynamischen Mengen nicht möglich, da die eingesetzte Version des Bereichs"
					               + " kb.metaModellGlobal zu alt ist (mindestens Version 10 notwendig).");
		}

		// Konfigurationsverantwortlicher ermitteln
//		final ConfigurationAuthority configurationAuthority = dataModel.getConfigurationAuthority();

		// Versionen der zu exportierenden Bereiche festlegen
		final Map<ConfigurationArea, Short> versionedView = new HashMap<ConfigurationArea, Short>();

		// Speichert zu jedem Bereich die Version mit der etwas gemacht werden soll. Dies wird benötigt,
		// damit eventuelle Abhängigkeiten zwischen Bereichen erkannt werden können.
		final List<ConfigAreaAndVersion> areasAndVersions = new ArrayList<ConfigAreaAndVersion>();

		for(ConfigurationArea configurationArea : allAreas.values()) {
			// Alle Bereiche werden in der neuesten Version betrachtet. Egal, ob verantwortlich, oder nicht. 
			versionedView.put(configurationArea, configurationArea.getModifiableVersion());
			areasAndVersions.add(new ConfigAreaAndVersion(configurationArea, configurationArea.getModifiableVersion()));
//			if(configurationArea.getConfigurationAuthority() == configurationAuthority) {
//				 bei gleichem KV wird die in Bearbeitung befindliche Version genommen
//				versionedView.put(configurationArea, configurationArea.getModifiableVersion());
//			}
//			else {
//				// bei unterschiedlichem KV wird die zur Übernahme freigegebene Version verwendet
//				versionedView.put(configurationArea, configurationArea.getTransferableVersion());
//			}
		}

		// Die Abhängigkeiten zwischen den Bereichen müssen nicht geprüft werden, da dies bereits im Konstruktor
		// des Datenmodells geschieht.

		_objectLookup = new VersionedView(dataModel, versionedView);

		// zu jedem Konfigurationsbereich nach den aktuellen und zukünftig aktuellen Objekten fragen
		// alle Objekte aus dem Datenmodell holen
		for(ConfigurationArea configurationArea : configurationAreas) {
			// pro Konfigurationsbereich
			exportArea(exportPath, dataModel, configurationArea);
		}
	}

	private void exportArea(final File exportPath, final DataModel dataModel, ConfigurationArea configurationArea) throws IOException {
		// nur die neuesten Objekte sollen genommen werden
		short version = configurationArea.getModifiableVersion();
		final Set<SystemObject> objects = new HashSet<SystemObject>();

		// alle aktuellen
		for(SystemObject systemObject : configurationArea.getCurrentObjects()) {
			if(systemObject instanceof DynamicObject) {
				final ConfigDynamicObject dynamicObject = (ConfigDynamicObject)systemObject;
				// wenn eine Simulationsvariante vergeben wurde (> 0), dann soll das dynamische Objekt nicht beachtet werden
				if(dynamicObject.getSimulationVariant() <= 0) {
					configurationArea = handleObject(configurationArea, objects, systemObject, dynamicObject);
				}
			}
			else {
				ConfigurationObject configObject = (ConfigurationObject)systemObject;
				if(configObject.getValidSince() <= version && (configObject.getNotValidSince() > version || configObject.getNotValidSince() == 0)) {
					configurationArea = handleObject(configurationArea, objects, systemObject, configObject);
				}
			}
		}
		// alle neuen Objekte
		for(SystemObject systemObject : configurationArea.getNewObjects()) {
			// Dynamische Objekte gibt es hier nicht -> die sind immer aktuell
			if(systemObject instanceof ConfigurationObject) {
				ConfigurationObject configObject = (ConfigurationObject)systemObject;
				if(configObject.getValidSince() <= version && (configObject.getNotValidSince() > version || configObject.getNotValidSince() == 0)) {
					configurationArea = handleObject(configurationArea, objects, systemObject, configObject);
				}
			}
		}

		_debug.info("Konfigurationsbereich: " + configurationArea.getPid() + " in Version: " + version + " Anzahl der Objekte: " + objects.size());

		if(configurationArea != null) {
			// Konfigurationsverantwortlichen ermitteln
			final Data data = configurationArea.getConfigurationData(
					dataModel.getAttributeGroup("atg.konfigurationsBereichEigenschaften"), dataModel.getAspect("asp.eigenschaften")
			);
			final SystemObject newAuthority = data.getReferenceValue("neuerZuständiger").getSystemObject();
			SystemObject authority = null;
			if(newAuthority != null) {
				authority = newAuthority;
			}
			else {
				authority = configurationArea.getConfigurationAuthority();
			}

			// Property-Objekt für den Konfigurationsbereich erstellen.
			// Da das Objekt exportiert werden soll, müssen auch die Abhängigkeiten geschrieben werden.
			final Collection<ConfigurationAreaDependency> dependenciesFromOtherConfigurationAreas;

			ConfigConfigurationArea configConfigurationArea = (ConfigConfigurationArea)configurationArea;
			if(configConfigurationArea.dependenciesChecked() == true) {
				dependenciesFromOtherConfigurationAreas = configConfigurationArea.getDependencyFromOtherConfigurationAreas();
			}
			else {
				dependenciesFromOtherConfigurationAreas = null;
			}

			final ConfigurationAreaProperties configurationAreaProperties = new ConfigurationAreaProperties(
					configurationArea.getName(),
					configurationArea.getPid(),
					configurationArea.getId(),
					authority.getPid(),
					configurationArea.getInfo(),
					new ObjectPropertiesList(objects),
					dependenciesFromOtherConfigurationAreas
			);
			configurationAreaProperties.setConfigurationAreaChangeInformation(createChangeInformation(configurationArea));
			configurationAreaProperties.setUnversionedChanges(getUnversionedChanges(dataModel, configurationArea));
			// Writer für die XML-Ausgabe initialisieren
			final ConfigAreaWriter writer = new ConfigAreaWriter(configurationAreaProperties);
			// File-Objekt erzeugen
			try {
				final File file = new File(exportPath, configurationArea.getPid() + ".xml");

				// Falls die Datei existiert - erst umbenennen!
				if(file.exists()) {
					final File oldFile = new File(exportPath, configurationArea.getPid() + "_old.xml");
					_debug.fine("Versorgungsdatei " + file + " existiert bereits. Sie wurde in " + oldFile + " umbenannt.");
					// prüfen, ob es bereits eine "alte" Datei gibt - dann wird diese zuerst gelöscht (siehe auch renameTo-Methode)
					if(oldFile.exists()) {
						oldFile.delete();
					}
					file.renameTo(oldFile);
				}

				// Datei schreiben
				writer.writeConfigAreaAsXML(file);
				_debug.info("Datei " + file.getName() + " wurde geschrieben.");
			}
			catch(IOException ex) {
				final String errorMessage =
						"Die Versorgungsdatei für den Konfigurationsbereich " + configurationArea.getPid() + " konnte nicht geschrieben werden";
				_debug.error(errorMessage, ex);
				throw new IOException(errorMessage + ex.toString());
			}
		}
		else {
			throw new IllegalArgumentException("Dieser Konfigurationsbereich hat kein ConfigurationArea-Objekt.");
		}
	}

	private SystemObjectProperties createObjectProperties(final SystemObject systemObject) {
		if(systemObject instanceof Aspect) {
			return (createAspectProperties((Aspect)systemObject));
		}
		else if(systemObject instanceof AttributeListDefinition) { // enthält AttributeSet und AttributeType
			// muss vor dem AttributTyp stehen, da dieser ebenfalls eine AttributeListDefinition sein kann
			return (createAttributeListProperties((AttributeListDefinition)systemObject));
		}
		else if(systemObject instanceof AttributeType) {
			return (createAttributeTypeProperties((AttributeType)systemObject));
		}
		else if(systemObject instanceof AttributeGroup) {
			if(systemObject.getType().getPid().equals(Pid.Type.TRANSACTION)) {
				return (createTransactionProperties((AttributeGroup)systemObject));
			}
			else
			{
				return (createAttributeGroupProperties((AttributeGroup)systemObject));
			}
		}
		else if(systemObject instanceof ObjectSetType) {	// von ConfigurationObjectType
			return (createObjectSetTypeProperties((ObjectSetType)systemObject));
		}
		else if(systemObject instanceof SystemObjectType) {
			return (createSystemObjectTypeProperties((SystemObjectType)systemObject));
		}
		else {
			return (createConfigurationObjectProperties(systemObject));
		}
	}

	private ConfigurationArea handleObject(ConfigurationArea configurationArea, final Set<SystemObject> objects, final SystemObject systemObject, final SystemObject dynamicObject) {
		// alle SystemObjekte werden hier betrachtet (Konfigurations-Objekte und dynamische Objekte)
		// Attribute, ObjectSet, AttributeGroupUsage etc. müssen herausgefiltert werden
		if(systemObject instanceof Attribute) return configurationArea;
		if(systemObject instanceof AttributeGroupUsage) return configurationArea;
		if(systemObject instanceof ObjectSet) return configurationArea;
		if(systemObject instanceof ObjectSetUse) return configurationArea;
		if(systemObject instanceof IntegerValueRange) return configurationArea;
		if(systemObject instanceof IntegerValueState) return configurationArea;

		// Konfigurationsbereich-Objekt bei Bedarf neu setzen
		if(systemObject instanceof ConfigurationArea) {
			configurationArea = (ConfigurationArea)systemObject;
		}
		else {
			// Im Normalfall einfach Objekt merken (ggf. überschreiben)
			objects.add(dynamicObject);
		}
		return configurationArea;
	}

	private Collection<ConfigurationAreaUnversionedChange> getUnversionedChanges(final DataModel dataModel, final ConfigurationArea configurationArea) {
		final List<ConfigurationAreaUnversionedChange> result = new ArrayList<ConfigurationAreaUnversionedChange>();
		AttributeGroup attributeGroup = dataModel.getAttributeGroup("atg.konfigurationsBereichUnversionierteÄnderungen");
		if(attributeGroup != null){
			Data configurationData = configurationArea.getConfigurationData(attributeGroup);
			if(configurationData != null){
				for(Data data : configurationData.getItem("versionen")) {
					result.add(new ConfigurationAreaUnversionedChange(
							           data.getUnscaledValue("Version").shortValue(),
							           data.getTextArray("AttributTypen").getTextArray()));
				}
			}
		}
		return result;
	}

	/**
	 * Wandelt die KonfigurationsÄnderungen, die am Konfigurationsbereich gespeichert werden, so um, dass sie in die Versorgungsdatei geschrieben werden kann.
	 *
	 * @param configurationArea ein Konfigurationsbereich
	 *
	 * @return die KonfigurationsÄnderungen für ein Eigenschafts-Objekt eines Konfigurationsbereichs
	 */
	private ConfigurationAreaChangeInformation[] createChangeInformation(ConfigurationArea configurationArea) {
		final AttributeGroup atg = configurationArea.getDataModel().getAttributeGroup("atg.konfigurationsÄnderungen");
		Data data = configurationArea.getConfigurationData(atg);
		if(data != null) {
			// Data auslesen und Array erzeugen
			final Data.Array array = data.getArray("KonfigurationsÄnderung");
			final ConfigurationAreaChangeInformation[] configurationAreaChangeInformation = new ConfigurationAreaChangeInformation[array.getLength()];
			for(int i = 0; i < array.getLength(); i++) {
				Data item = array.getItem(i);
				configurationAreaChangeInformation[i] = new ConfigurationAreaChangeInformation(
						item.getTimeValue("Stand").getMillis(),
						item.getUnscaledValue("Version").intValue(),
						item.getTextValue("Autor").getText(),
						item.getTextValue("Grund").getText(),
						item.getTextValue("Text").getText()
				);
			}
			return configurationAreaChangeInformation;
		}
		else {
			return new ConfigurationAreaChangeInformation[0];
		}
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften eines Aspekts enthält.
	 *
	 * @param asp ein Aspekt
	 *
	 * @return Objekt mit den Eigenschaften eines Aspekts
	 */
	private AspectProperties createAspectProperties(Aspect asp) {
		// Der Code wird nicht mehr benötigt.
//		aspectProperties.setCode(asp.getCode());
		return new AspectProperties(asp.getName(), asp.getPid(), asp.getId(), asp.getType().getPid(), asp.getInfo());
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften einer AttributListenDefinition enthält.
	 *
	 * @param atl eine AttributListenDefinition
	 *
	 * @return Objekt mit den Eigenschaften einer AttributListenDefinition
	 */
	private AttributeListProperties createAttributeListProperties(AttributeListDefinition atl) {
		final AttributeListProperties attributeListProperties = new AttributeListProperties(
				atl.getName(), atl.getPid(), atl.getId(), atl.getType().getPid(), atl.getInfo()
		);

		// Attribute und Attributlisten werden hier eingetragen - Reihenfolge beachten (Position)
		// hier erhalte ich eine unsortierte AttributListe
		NonMutableSet attributeSet = (NonMutableSet)atl.getObjectSet("Attribute");
		if(attributeSet != null) {
			final List<SystemObject> attributes = attributeSet.getElementsInModifiableVersion();
			AttributeProperties[] attributesAndAttributeLists = new AttributeProperties[attributes.size()];
			for(SystemObject systemObject : attributes) {
				Attribute attribute = (Attribute)systemObject;
				if(attribute.getAttributeType() instanceof AttributeListDefinition) {
					final ListAttributeProperties listAttributeProperties = new ListAttributeProperties(attribute.getAttributeType().getPid());
					listAttributeProperties.setInfo(attribute.getInfo());
					listAttributeProperties.setMaxCount(attribute.getMaxCount());
					listAttributeProperties.setName(attribute.getName());
					listAttributeProperties.setTargetValue((attribute.isCountVariable() ? TargetValue.VARIABLE : TargetValue.FIX));
					attributesAndAttributeLists[attribute.getPosition() - 1] = listAttributeProperties;
				}
				else {
					final PlainAttributeProperties configurationAttribute = new PlainAttributeProperties(attribute.getAttributeType().getPid());
					configurationAttribute.setDefault(getDefault(attribute));
					configurationAttribute.setInfo(attribute.getInfo());
					configurationAttribute.setMaxCount(attribute.getMaxCount());
					configurationAttribute.setName(attribute.getName());
					configurationAttribute.setTargetValue((attribute.isCountVariable() ? TargetValue.VARIABLE : TargetValue.FIX));
					attributesAndAttributeLists[attribute.getPosition() - 1] = configurationAttribute;
				}
			}
			attributeListProperties.setAttributeAndAttributeList(attributesAndAttributeLists);
		}

		return attributeListProperties;
	}

	/**
	 * Gibt den Default-Wert eines System-Objekts zurück.
	 *
	 * @param systemObject das System-Objekt
	 *
	 * @return Der Default-Wert eines System-Objekts oder <code>null</code>, falls es keinen Default-Wert gibt.
	 */
	private String getDefault(SystemObject systemObject) {
		final DataModel dataModel = systemObject.getConfigurationArea().getDataModel();
		final AttributeGroup atg = dataModel.getAttributeGroup("atg.defaultAttributwert");
		final Aspect asp = dataModel.getAspect("asp.eigenschaften");
		// Attributgruppenverwendung raussuchen
		AttributeGroupUsage atgUsage = null;
		final NonMutableSet atgUsageSet = atg.getNonMutableSet("AttributgruppenVerwendungen");
		if(atgUsageSet != null) {
			final List<SystemObject> atgUsages = atgUsageSet.getElementsInModifiableVersion();
			for(SystemObject object : atgUsages) {
				final AttributeGroupUsage usage = (AttributeGroupUsage)object;
				if(usage.getAspect() == asp) {
					atgUsage = usage;
					break;
				}
			}
		}
		Data data = systemObject.getConfigurationData(atgUsage);
		if(data == null) {
			return null;
		}
		else {
			return data.getTextValue("wert").getText();
		}
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften eines AttributTyps enthält.
	 *
	 * @param attributeType ein AttributTyp
	 *
	 * @return Objekt mit den Eigenschaften eines AttributTyps
	 */
	private AttributeTypeProperties createAttributeTypeProperties(AttributeType attributeType) {
		// neues Properties-Objekt anlegen
		final AttributeTypeProperties attributeTypeProperties = new AttributeTypeProperties(
				attributeType.getName(), attributeType.getPid(), attributeType.getId(), attributeType.getType().getPid(), attributeType.getInfo()
		);
		attributeTypeProperties.setDefault(getDefault(attributeType));
		// AttributTyp ermitteln und umwandeln
		if(attributeType instanceof StringAttributeType) {
			StringAttributeType stringAttributeType = (StringAttributeType)attributeType;
			ConfigurationString configurationString = new ConfigurationString(stringAttributeType.getMaxLength());
			configurationString.setStringEncoding(stringAttributeType.getEncodingName());
			attributeTypeProperties.setAttributeType(configurationString);
		}
		else if(attributeType instanceof IntegerAttributeType) {
			IntegerAttributeType integerAttributeType = (IntegerAttributeType)attributeType;
			ConfigurationIntegerDef integerDef = new ConfigurationIntegerDef();
			integerDef.setBits(integerAttributeType.getByteCount() * 8);

			final List<ConfigurationIntegerValueRange> valueRangeAndStates = new ArrayList<ConfigurationIntegerValueRange>();
			// den Wertebereich
			final IntegerValueRange integerValueRange = integerAttributeType.getRange();
			if(integerValueRange != null) {
				final ConfigurationValueRange configurationValueRange = new ConfigurationValueRange();
				configurationValueRange.setInfo(integerValueRange.getInfo());
				configurationValueRange.setMaximum(integerValueRange.getMaximum());
				configurationValueRange.setMinimum(integerValueRange.getMinimum());
				configurationValueRange.setScale(integerValueRange.getConversionFactor());
				configurationValueRange.setUnit(integerValueRange.getUnit());
				valueRangeAndStates.add(configurationValueRange);
			}
			// die Wert-Zustände
			NonMutableSet stateSet = (NonMutableSet)integerAttributeType.getObjectSet("zustände");
			if(stateSet != null) {
				for(SystemObject systemObject : stateSet.getElementsInModifiableVersion()) {
					IntegerValueState integerValueState = (IntegerValueState)systemObject;
					final ConfigurationState configurationState = new ConfigurationState(integerValueState.getName(), integerValueState.getValue());
					configurationState.setInfo(integerValueState.getInfo());
					valueRangeAndStates.add(configurationState);
				}
			}
			integerDef.setValueRangeAndState(valueRangeAndStates.toArray(new ConfigurationIntegerValueRange[valueRangeAndStates.size()]));
			attributeTypeProperties.setAttributeType(integerDef);
		}
		else if(attributeType instanceof DoubleAttributeType) {
			DoubleAttributeType doubleAttributeType = (DoubleAttributeType)attributeType;
			ConfigurationDoubleDef doubleDef = new ConfigurationDoubleDef();
			doubleDef.setAccuracy((doubleAttributeType.getAccuracy() == DoubleAttributeType.FLOAT ? AccuracyDouble.FLOAT : AccuracyDouble.DOUBLE));
			doubleDef.setUnit(doubleAttributeType.getUnit());
			attributeTypeProperties.setAttributeType(doubleDef);
		}
		else if(attributeType instanceof TimeAttributeType) {
			TimeAttributeType timeAttributeType = (TimeAttributeType)attributeType;
			ConfigurationTimeStamp timeStamp = new ConfigurationTimeStamp();
			timeStamp.setAccuracy(
					(timeAttributeType.getAccuracy() == TimeAttributeType.MILLISECONDS ? AccuracyTimeStamp.MILLISECONDS : AccuracyTimeStamp.SECONDS)
			);
			timeStamp.setRelative(timeAttributeType.isRelative());
			attributeTypeProperties.setAttributeType(timeStamp);
		}
		else if(attributeType instanceof ReferenceAttributeType) {
			ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)attributeType;
			ConfigurationObjectReference objectReference = new ConfigurationObjectReference();
			objectReference.setReferenceType(referenceAttributeType.getReferenceType());

			SystemObjectType referencedObjectType = referenceAttributeType.getReferencedObjectType();
			objectReference.setReferenceObjectType((referencedObjectType != null ? referencedObjectType.getPid() : ""));
			objectReference.setUndefinedReferences(
					(referenceAttributeType.isUndefinedAllowed() ? UndefinedReferenceOptions.ALLOWED : UndefinedReferenceOptions.FORBIDDEN)
			);
			attributeTypeProperties.setAttributeType(objectReference);
		}
		else {
			throw new IllegalStateException(
					"Dieser AttributTyp " + attributeType.getNameOrPidOrId() + " (" + attributeType.getClass().getName() + ") wird noch nicht unterstützt."
			);
		}

		return attributeTypeProperties;
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften einer Attributgruppe enthält.
	 *
	 * @param atg eine Attributgruppe
	 *
	 * @return Objekt mit den Eigenschaften einer Attributgruppe
	 */
	private AttributeGroupProperties createAttributeGroupProperties(AttributeGroup atg) {
		final AttributeGroupProperties attributeGroupProperties = new AttributeGroupProperties(
				atg.getName(), atg.getPid(), atg.getId(), atg.getType().getPid(), atg.getInfo()
		);

		// Attribute und Attributlisten werden hier eingetragen - Reihenfolge beachten (Position)
		NonMutableSet attributeSet = (NonMutableSet)atg.getObjectSet("Attribute");
		if(attributeSet != null) {
			final List<SystemObject> attributes = attributeSet.getElementsInModifiableVersion();
			AttributeProperties[] attributesAndAttributeLists = new AttributeProperties[attributes.size()];
			for(SystemObject systemObject : attributes) {
				Attribute attribute = (Attribute)systemObject;
				if(attribute.getAttributeType() instanceof AttributeListDefinition) {
					ListAttributeProperties attributeList = new ListAttributeProperties(attribute.getAttributeType().getPid());
					attributeList.setInfo(attribute.getInfo());
					attributeList.setMaxCount(attribute.getMaxCount());
					attributeList.setName(attribute.getName());
					attributeList.setTargetValue((attribute.isCountVariable() ? TargetValue.VARIABLE : TargetValue.FIX));
					attributesAndAttributeLists[attribute.getPosition() - 1] = attributeList;
				}
				else {
					PlainAttributeProperties configurationAttribute = new PlainAttributeProperties(attribute.getAttributeType().getPid());
					configurationAttribute.setDefault(getDefault(attribute));
					configurationAttribute.setInfo(attribute.getInfo());
					configurationAttribute.setMaxCount(attribute.getMaxCount());
					configurationAttribute.setName(attribute.getName());
					configurationAttribute.setTargetValue((attribute.isCountVariable() ? TargetValue.VARIABLE : TargetValue.FIX));
					attributesAndAttributeLists[attribute.getPosition() - 1] = configurationAttribute;
				}
			}
			attributeGroupProperties.setAttributeAndAttributeList(attributesAndAttributeLists);
		}

		// Parameter-Aspekte ermitteln
		final Set<String> parameterAspects = new HashSet<String>();
		// Aspekte abspeichern
		final List<ConfigurationAspect> configurationAspects = new ArrayList<ConfigurationAspect>();
		NonMutableSet atgUsageSet = (NonMutableSet)atg.getObjectSet("AttributgruppenVerwendungen");
		boolean isParameter = false;
		if(atgUsageSet != null) {
			// ermitteln, ob Attributgruppe parametrierend ist.
			final List<SystemObject> elements = atgUsageSet.getElementsInModifiableVersion();
			for(SystemObject element : elements) {
				final AttributeGroupUsage atgUsage = (AttributeGroupUsage)element;
				final String aspPid = atgUsage.getAspect().getPid();
				if(aspPid.equals("asp.parameterSoll") || aspPid.equals("asp.parameterIst") || aspPid.equals("asp.parameterVorgabe") || aspPid.equals(
						"asp.parameterDefault"
				)) {
					parameterAspects.add(aspPid);
				}
			}
			if(parameterAspects.contains("asp.parameterSoll") && parameterAspects.contains("asp.parameterVorgabe")) {
				isParameter = true;
			}

			for(SystemObject systemObject : elements) {
				AttributeGroupUsage atgUsage = (AttributeGroupUsage)systemObject;
				if(atgUsage.isExplicitDefined()) {
					// die Verwendung wurde explizit vorgegeben, deshalb wird der Aspekt auch bei der Attributgruppe mit ausgegeben, sonst nicht
					Aspect aspect = atgUsage.getAspect();
					String aspPid = aspect.getPid();
					if(isParameter && (aspPid.equals("asp.parameterSoll") || aspPid.equals("asp.parameterIst") || aspPid.equals("asp.parameterVorgabe")
							|| aspPid.equals("asp.parameterDefault"))) {
						// diese Aspekte nicht rausschreiben
					}
					else {
						ConfigurationAspect configurationAspect = new ConfigurationAspect(aspect.getPid());
						configurationAspect.setInfo(atgUsage.getInfo());
						configurationAspect.setUsage(atgUsage.getUsage());
						configurationAspects.add(configurationAspect);
					}
				}
			}
			attributeGroupProperties.setConfigurationAspect(configurationAspects.toArray(new ConfigurationAspect[configurationAspects.size()]));
		}

		// Der Code und die Eigenschaft konfigurierend wird nicht mehr benötigt.
//		attributeGroupProperties.setCode(atg.getCode());
//		attributeGroupProperties.setConfiguring(atg.isConfigurating());
		attributeGroupProperties.setParameter(isParameter);

		return attributeGroupProperties;
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften einer Transaktion enthält.
	 *
	 * @param transaction eine Transaktion
	 *
	 * @return Objekt mit den Eigenschaften einer Transaktion
	 */
	private TransactionProperties createTransactionProperties(AttributeGroup transaction) {
		final TransactionProperties transactionProperties = new TransactionProperties(
				transaction.getName(), transaction.getPid(), transaction.getId(), transaction.getType().getPid(), transaction.getInfo()
		);

		// Aspekte abspeichern
		NonMutableSet atgUsageSet = (NonMutableSet)transaction.getObjectSet("AttributgruppenVerwendungen");
		if(atgUsageSet != null) {

			final List<SystemObject> elements = atgUsageSet.getElementsInModifiableVersion();
			final List<ConfigurationAspect> configurationAspects = new ArrayList<ConfigurationAspect>();
			for(SystemObject systemObject : elements) {
				AttributeGroupUsage atgUsage = (AttributeGroupUsage)systemObject;
				if(atgUsage.isExplicitDefined()) {
					// die Verwendung wurde explizit vorgegeben, deshalb wird der Aspekt auch bei der Attributgruppe mit ausgegeben, sonst nicht
					Aspect aspect = atgUsage.getAspect();
					ConfigurationAspect configurationAspect = new ConfigurationAspect(aspect.getPid());
					configurationAspect.setInfo(atgUsage.getInfo());
					configurationAspect.setUsage(atgUsage.getUsage());
					configurationAspects.add(configurationAspect);
				}
			}
			transactionProperties.setConfigurationAspect(configurationAspects.toArray(new ConfigurationAspect[configurationAspects.size()]));
		}

		final AttributeGroup transactionAttributeGroup = (AttributeGroup)_objectLookup.getObject("atg.transaktionsEigenschaften");

		final Data data = transaction.getConfigurationData(transactionAttributeGroup);

		// Akzeptierte Datenidentifikationen speichern
		transactionProperties.setPossibleDids(getDids(data.getItem("akzeptiert")));

		// Benötigte Datenidentifikationen speichern
		transactionProperties.setRequiredDids(getDids(data.getItem("benötigt")));

		return transactionProperties;
	}

	private List<TransactionProperties.DataIdentification> getDids(final Data data) {
		final List<TransactionProperties.DataIdentification> result = new ArrayList<TransactionProperties.DataIdentification>();
		for(final Data entry:data){
			result.add(new TransactionProperties.DataIdentification(
					           entry.getReferenceValue("ObjektTyp").getSystemObjectPid(),
					           entry.getReferenceValue("Attributgruppe").getSystemObjectPid(),
					           entry.getReferenceValue("Aspekt").getSystemObjectPid(),
					           entry.getTextValue("NurTransaktionsObjekt").getText().toLowerCase())
			);
		}
		return result;
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften eines Mengen-Typs enthält.
	 *
	 * @param objectSetType ein Mengen-Typ
	 *
	 * @return Objekt mit den Eigenschaften eines Mengen-Typs
	 */
	private ObjectSetTypeProperties createObjectSetTypeProperties(ObjectSetType objectSetType) {
		// die Pids der Elementtypen der Menge ermitteln
		final Set<String> elementPids = new TreeSet<String>(Collator.getInstance(Locale.GERMAN));
		NonMutableSet objectTypeSet = (NonMutableSet)objectSetType.getObjectSet("ObjektTypen");
		if(objectTypeSet != null) {
			for(SystemObject systemObject : objectTypeSet.getElementsInModifiableVersion()) {
				elementPids.add(((SystemObjectType)systemObject).getPid());
			}
		}
		final ObjectSetTypeProperties objectSetTypeProperties = new ObjectSetTypeProperties(
				objectSetType.getName(),
				objectSetType.getPid(),
				objectSetType.getId(),
				objectSetType.getType().getPid(),
				objectSetType.getInfo(),
				elementPids.toArray(new String[elementPids.size()])
		);
		objectSetTypeProperties.setMutable(objectSetType.isMutable());
		objectSetTypeProperties.setMaximum(objectSetType.getMaximumElementCount());
		objectSetTypeProperties.setMinimum(objectSetType.getMinimumElementCount());
		objectSetTypeProperties.setReferenceType(objectSetType.getReferenceType());
		return objectSetTypeProperties;
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften eines Typ-Objekts enthält.
	 *
	 * @param systemObjectType ein Typ-Objekt
	 *
	 * @return Objekt mit den Eigenschaften eines Typ-Objekts
	 */
	private SystemObjectTypeProperties createSystemObjectTypeProperties(SystemObjectType systemObjectType) {
		final SystemObjectTypeProperties systemObjectTypeProperties = new SystemObjectTypeProperties(
				systemObjectType.getName(), systemObjectType.getPid(), systemObjectType.getId(), systemObjectType.getType().getPid(), systemObjectType.getInfo()
		);
		// alle direkten Attributgruppen und Mengen
		final List<Object> atgAndSets = new ArrayList<Object>();
		final List<String> transactions = new ArrayList<String>();
		NonMutableSet attributeGroups = (NonMutableSet)systemObjectType.getObjectSet("Attributgruppen");
		if(attributeGroups != null) {
			for(SystemObject systemObject : attributeGroups.getElementsInModifiableVersion()) {
				if(systemObject.getType().getPid().equals(Pid.Type.TRANSACTION)) {
					transactions.add(systemObject.getPid());
				}
				else {
					atgAndSets.add(systemObject.getPid());
				}
			}
		}
		NonMutableSet sets = (NonMutableSet)systemObjectType.getObjectSet("Mengen");
		if(sets != null) {
			for(SystemObject systemObject : sets.getElementsInModifiableVersion()) {
				ObjectSetUse objectSetUse = (ObjectSetUse)systemObject;
				ConfigurationSet configurationSet = new ConfigurationSet(objectSetUse.getObjectSetType().getPid(), objectSetUse.getObjectSetName());
				configurationSet.setInfo(objectSetUse.getInfo());
				configurationSet.setRequired(objectSetUse.isRequired());
				atgAndSets.add(configurationSet);
			}
		}
		systemObjectTypeProperties.setTransactions(transactions);
		systemObjectTypeProperties.setAtgAndSet(atgAndSets.toArray(new Object[atgAndSets.size()]));
		systemObjectTypeProperties.setConfiguring(systemObjectType.isConfigurating());
		systemObjectTypeProperties.setObjectNamesPermanent(systemObjectType.isNameOfObjectsPermanent());

		// SuperTypen ermitteln
		final List<String> extendedPids = new ArrayList<String>();

		NonMutableSet superTypeSet = (NonMutableSet)systemObjectType.getObjectSet("SuperTypen");
		if(superTypeSet != null) {
			for(SystemObject systemObject : superTypeSet.getElementsInModifiableVersion()) {
				extendedPids.add(((SystemObjectType)systemObject).getPid());
			}
		}
		systemObjectTypeProperties.setExtendedPids(extendedPids.toArray(new String[extendedPids.size()]));

		// PersistenzModus ermitteln
		if(systemObjectType instanceof DynamicObjectType) {
			final DynamicObjectType.PersistenceMode mode = ((DynamicObjectType)systemObjectType).getPersistenceMode();
			switch(mode) {
				case TRANSIENT_OBJECTS:
					systemObjectTypeProperties.setPersistenceMode(PersistenceMode.TRANSIENT_OBJECTS);
					break;
				case PERSISTENT_OBJECTS:
					systemObjectTypeProperties.setPersistenceMode(PersistenceMode.PERSISTENT_OBJECTS);
					break;
				case PERSISTENT_AND_INVALID_ON_RESTART:
					systemObjectTypeProperties.setPersistenceMode(PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART);
					break;
				default:
					systemObjectTypeProperties.setPersistenceMode(PersistenceMode.UNDEFINED);
			}
		}

		// Default-Parameter-Datensätze erstellen
		systemObjectTypeProperties.setDefaultParameters(createConfigurationDefaultParameters(systemObjectType));

		return systemObjectTypeProperties;
	}

	/**
	 * Diese Methode erzeugt ein korrespondierendes Objekt, welches die Eigenschaften eines konfigurierenden oder dynamischen Objekts enthält.
	 *
	 * @param systemObject ein konfigurierendes Objekt
	 *
	 * @return Objekt mit den Eigenschaften eines konfigurierenden Objekts
	 */
	private ConfigurationObjectProperties createConfigurationObjectProperties(SystemObject systemObject) {
		final ConfigurationConfigurationObject configObject = new ConfigurationConfigurationObject(
				systemObject.getName(), systemObject.getPid(), systemObject.getId(), systemObject.getType().getPid(), systemObject.getInfo()
		);
		final List<ConfigurationObjectElements> datasetAndObjectSet = new ArrayList<ConfigurationObjectElements>();

		// alle ATGVs durchgehen und sämtliche Datensätze ausgeben
		final Collection<AttributeGroupUsage> usedAttributeGroupUsages = systemObject.getUsedAttributeGroupUsages();
		for(AttributeGroupUsage atgUsage : usedAttributeGroupUsages) {
			final AttributeGroup attributeGroup = atgUsage.getAttributeGroup();
			if(!ComparePropertiesWithSystemObjects.isHiddenInExport(atgUsage)) {
				final Data data = ((ConfigSystemObject)systemObject).getConfigurationData(atgUsage, _objectLookup);
				if(data != null) {
					final List<DatasetElement> elementList = new LinkedList<DatasetElement>();
					final ConfigurationDataset dataset = new ConfigurationDataset(attributeGroup.getPid(), atgUsage.getAspect().getPid());
					// Iterator auf unterster Ebene
					Iterator iterator = data.iterator();
					while(iterator.hasNext()) {
						Data nextData = (Data)iterator.next();
						elementList.add(extractData(nextData, false));
					}
					dataset.setDataAndDataListAndDataField(elementList.toArray(new DatasetElement[elementList.size()]));
					datasetAndObjectSet.add(dataset);
				}
			}
		}

		// speichern der Objektmengen eines Konfigurationsobjekts
		// nur Konfigurations-Objekte haben Mengen
		if(systemObject instanceof ConfigurationObject) {
			final ConfigurationObject configurationObject = (ConfigurationObject)systemObject;

			for(ObjectSet objectSet : configurationObject.getObjectSets()) {
				List<SystemObject> elements;
				if(objectSet instanceof NonMutableSet) {
					NonMutableSet nonMutableSet = (NonMutableSet)objectSet;
					elements = nonMutableSet.getElementsInModifiableVersion();
				}
				else {
					// veränderbare Mengen - aktueller Stand
					elements = objectSet.getElements();
				}
				String[] sortedElementPids = elements.stream().map(SystemObject::getPid).sorted(Collator.getInstance(Locale.GERMAN)).toArray(String[]::new);
				String managementPid = "";
				if(objectSet instanceof MutableSet && _dynamicSetPropertiesAtg != null) {
					Data data = objectSet.getConfigurationData(_dynamicSetPropertiesAtg);
					if(data != null) {
						managementPid = data.getTextValue("verwaltung").getValueText();
					}
				}

				final ConfigurationObjectSet configurationObjectSet = new ConfigurationObjectSet(
						objectSet.getName(), sortedElementPids, managementPid
				);
				datasetAndObjectSet.add(configurationObjectSet);
			}
		}

		configObject.setDatasetAndObjectSet(datasetAndObjectSet.toArray(new ConfigurationObjectElements[datasetAndObjectSet.size()]));

		// Default-Parameter-Datensätze
		configObject.setDefaultParameters(createConfigurationDefaultParameters(systemObject));
		return configObject;
	}

	/**
	 * Liest einen Default-Parameter-Datensatz aus und gibt den Inhalt zurück.
	 *
	 * @param systemObject besitzt den Default-Parameter-Datensatz
	 *
	 * @return Inhalt des Default-Parameter-Datensatzes
	 */
	private ConfigurationDefaultParameter[] createConfigurationDefaultParameters(final SystemObject systemObject) {
		final Data configurationData = ((ConfigSystemObject)systemObject).getConfigurationData(
				(AttributeGroup)_objectLookup.getObject("atg.defaultParameterdatensätze"), _objectLookup
		);
		if(configurationData != null) {
			final Data.Array array = configurationData.getArray("Default-Parameterdatensatz");
			final ConfigurationDefaultParameter[] defaultParameters = new ConfigurationDefaultParameter[array.getLength()];

			for(int i = 0; i < array.getLength(); i++) {
				final Data item = array.getItem(i);
				// Werte auslesen
				final AttributeGroup attributeGroup = (AttributeGroup)item.getReferenceValue("attributgruppe").getSystemObject();
				final String pidAtg = attributeGroup.getPid();

				final String pidType = item.getReferenceValue("typ").getSystemObject().getPid();
				final ConfigurationDefaultParameter defaultParameter = new ConfigurationDefaultParameter(pidType, pidAtg);

				try {
					// Datensatz auslesen und extrahieren
					final Data.Array datasetArray = item.getArray("datensatz");
					final byte[] bytes = new byte[datasetArray.getLength()];
					for(int j = 0; j < datasetArray.getLength(); j++) {
						bytes[j] = datasetArray.getScaledValue(j).byteValue();
					}
					// byte-Array deserialisieren
					final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
					final Deserializer deserializer = SerializingFactory.createDeserializer(item.getScaledValue("serialisierer").intValue(), in);
					final Data data = deserializer.readData(attributeGroup, _objectLookup);

					// Iterator auf unterster Ebene
					final Iterator iterator = data.iterator();
					final List<DatasetElement> elementList = new LinkedList<DatasetElement>();
					while(iterator.hasNext()) {
						Data nextData = (Data)iterator.next();
						elementList.add(extractData(nextData, false));
					}
					defaultParameter.setDataAndDataListAndDataField(elementList.toArray(new DatasetElement[elementList.size()]));
				}
				catch(Exception ex) {
					throw new IllegalStateException(
							"Die Default-Parameter-Datensätze vom Objekt " + systemObject.getPidOrNameOrId() + " können nicht deserialisiert werden."
					);
				}
				defaultParameters[i] = defaultParameter;
			}
			return defaultParameters;
		}
		else {
			return new ConfigurationDefaultParameter[0];
		}
	}

	/**
	 * Dies ist eine Hilfsklasse für ein konfigurierendes Objekt, damit die Datensätze vollständig ermittelt werden können. Hierzu ist es notwendig bei einer Liste
	 * oder einem Array diese Methode rekursiv aufzurufen.
	 *
	 * @param data                das zu betrachtende Data-Objekt
	 * @param calledFromDataArray Gibt an, ob diese Methode von einem Daten-Array aufgerufen wurde.
	 *
	 * @return die extrahierte Struktur des Datensatzes
	 */
	private DatasetElement extractData(Data data, boolean calledFromDataArray) {
		if(data.isPlain()) {
			if(data.getAttributeType() instanceof ReferenceAttributeType) {
				String value = data.asReferenceValue().getSystemObjectPid();
				if(value.length() == 0) value = "undefiniert";
				return new ConfigurationData(data.getName(), value);
			}
			else {
				return new ConfigurationData(data.getName(), data.asTextValue().getText());
			}
		}
		else if(data.isList()) {
			final List<DatasetElement> elements = new ArrayList<DatasetElement>();
			Iterator iter = data.iterator();
			while(iter.hasNext()) {
				elements.add(extractData((Data)iter.next(), false));
			}
			String name;
			if(calledFromDataArray) {
				name = "-";
			}
			else {
				name = data.getName();
			}
			return new ConfigurationDataList(elements.toArray(new DatasetElement[elements.size()]), name);
		}
		else if(data.isArray()) {
			final List<DatasetElement> elements = new ArrayList<DatasetElement>();
			Data.Array array = data.asArray();
			for(int i = 0; i < array.getLength(); i++) {
				// dies ist ein Array, nachfolgende Liste soll den Namen "-" annehmen
				elements.add(extractData(array.getItem(i), true));
			}
			return new ConfigurationDataField(data.getName(), elements.toArray(new DatasetElement[elements.size()]));
		}
		else {
			throw new IllegalStateException("Unbekanntes Data. Es ist kein einfaches Datum, noch eine Liste oder ein Array. Data = " + data);
		}
	}

	/**
	 * Anstatt die Objekte alle nacheinander zu konvertieren und dann abzuspeichern, konvertiert/exportiert diese Liste die Objekte nur dann,
	 * wenn sie gebraucht werden (sonst wird viel zuviel Speicherplatz verbraucht)
	 */
	private class ObjectPropertiesList extends AbstractList<SystemObjectProperties> implements RandomAccess {
		private List<SystemObject> _objects;

		public ObjectPropertiesList(final Set<SystemObject> objects) {
			_objects = new ArrayList<SystemObject>(objects);
			Collections.sort(
					_objects, new Comparator<SystemObject>() {
						public int compare(final SystemObject o1, final SystemObject o2) {
							Collator deCollator = Collator.getInstance(Locale.GERMAN);
							return deCollator.compare(o1.getPid(), o2.getPid());
						}
					}
			);
		}

		@Override
		public SystemObjectProperties get(final int index) {
			return createObjectProperties(_objects.get(index));
		}

		@Override
		public int size() {
			return _objects.size();
		}
	}
}
