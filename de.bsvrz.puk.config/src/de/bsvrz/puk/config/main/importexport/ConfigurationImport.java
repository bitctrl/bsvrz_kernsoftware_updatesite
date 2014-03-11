/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
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

package de.bsvrz.puk.config.main.importexport;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectInfo;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.puk.config.configFile.datamodel.ConfigAttribute;
import de.bsvrz.puk.config.configFile.datamodel.ConfigAttributeType;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationObject;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.ConfigSystemObject;
import de.bsvrz.puk.config.xmlFile.parser.ConfigAreaParser;
import de.bsvrz.puk.config.xmlFile.properties.AspectProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeGroupProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeListProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.AttributeTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaChangeInformation;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaProperties;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAspect;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAttributeType;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationConfigurationObject;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationData;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataField;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataList;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDataset;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDefaultParameter;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationDoubleDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerDef;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationIntegerValueRange;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectElements;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectReference;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationObjectSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationSet;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationState;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationString;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationTimeStamp;
import de.bsvrz.puk.config.xmlFile.properties.ConfigurationValueRange;
import de.bsvrz.puk.config.xmlFile.properties.DatasetElement;
import de.bsvrz.puk.config.xmlFile.properties.ListAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.ObjectSetTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.PlainAttributeProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectProperties;
import de.bsvrz.puk.config.xmlFile.properties.SystemObjectTypeProperties;
import de.bsvrz.puk.config.xmlFile.properties.TargetValue;
import de.bsvrz.puk.config.xmlFile.properties.TransactionProperties;
import de.bsvrz.puk.config.xmlFile.properties.UndefinedReferenceOptions;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import org.xml.sax.SAXException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Diese Klasse importiert die Versorgungsdateien in das bestehende Datenmodell. Zu importierende Bereiche d�rfen keine Pid mehrmals benutzen.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConfigurationImport implements ObjectLookup {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Das Datenmodell, in welches die Versorgungsdateien importiert werden sollen. Hier�ber werden auch auf Modelldaten des MetaModells zugegriffen. */
	private DataModel _dataModel;

	/**
	 * Objekt, welches die Import-Eigenschaften mit m�glichen konkreten Objekten vergleicht. Dabei wird auch ermittelt, ob das Objekt ge�ndert werden kann, wenn
	 * die Eigenschaften nicht �bereinstimmen.
	 */
	private final ComparePropertiesWithSystemObjects _objectDiffs;

	/**
	 * Enth�lt zu importierende Objekte. Diese Map speichert zu einer Pid ein sogenanntes ImportObject, welches Referenzen auf ein SystemObjectProperties und auf
	 * ein SystemObject enth�lt.
	 */
	private final Map<String, ImportObject> _importMap = new HashMap<String, ImportObject>();

	/**
	 * Diese Map speichert zu einem Konfigurationsbereich, die Objekte, die aktuell g�ltig sind. Diese Map wird ben�tigt, um am Ende des Imports herauszubekommen,
	 * welche Objekte auf {@link de.bsvrz.dav.daf.main.config.SystemObject#invalidate() ung�ltig} gesetzt werden m�ssen.
	 */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _currentObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/**
	 * Diese Map speichert alle Objekte, die zuk�nftig aktuell werden. Sie wurden bereits zur �bernahme oder zur Aktivierung freigegeben. Diese Objekte d�rfen nur
	 * in begrenztem Ma�e modifiziert werden.
	 */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _newObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/** Diese Map speichert alle Objekte aus den zu importierenden Konfigurationsbereichen, die sich in Bearbeitung befinden. */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _editingObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/** Enth�lt alle Konfigurationsbereiche, die importiert werden sollen */
	private final Set<ConfigurationArea> _allImportedConfigurationAreas = new HashSet<ConfigurationArea>();

	/**
	 * In dieses Set wird die Pid eines Objekt-Typen eingetragen, sobald er erstellt werden soll. Dabei wird �berpr�ft, ob die Pid sich bereits in dem Set
	 * befindet. Falls ja, dann befindet sich eine Schleife in den Versorgungsdateien (der Objekt-Typ braucht einen anderen Typen und dieser braucht wiederum den
	 * urspr�nglichen Objekt-Typ, der aber noch nicht erstellt wurde). Nach Erstellung des Objekt-Typen wird die Pid aus dem Set wieder entfernt.
	 */
	private final Set<String> _objectTypesToCreate = new HashSet<String>();

	/** Gibt an, ob noch Referenzen aufgel�st werden sollen, oder nicht. */
	private boolean _dissolveReference = true;

	/** Zu jedem Konfigurationsbereich wird die Version gespeichert, in der der Bereich betrachtet werden soll. */
	private final Map<ConfigurationArea, Short> _usingVersionOfConfigurationArea = new HashMap<ConfigurationArea, Short>();

	/** Speichert alle Objekte, die betrachtet werden sollen. (In Abh�ngigkeit von der zu betrachtenden Version.) */
	private Map<String, SystemObject> _viewingObjects;

	/** Enth�lt f�r jeden importierten Bereich ein Properties-Objekt. */
	private Map<ConfigurationAreaProperties, ConfigurationArea> _areaProperty2ConfigurationArea = new HashMap<ConfigurationAreaProperties, ConfigurationArea>();

	/**
	 * Der Konstruktor f�hrt den Import der angegebenen Konfigurationsbereiche durch.
	 *
	 * @param dataModel  das Datenmodell der Konfiguration
	 * @param importPath das Verzeichnis der Versorgungsdateien
	 * @param pids       die Pids der zu importierenden Konfigurationsbereiche
	 *
	 * @throws ConfigurationChangeException Falls beim Import ein Fehler auftritt, wird der Import abgebrochen und der Fehler weitergereicht.
	 */
	public ConfigurationImport(final DataModel dataModel, final File importPath, final Collection<String> pids) throws ConfigurationChangeException {
		try {
			long startTime = System.currentTimeMillis();
			// Ausgabe, welche Konfigurationsbereiche importiert werden sollen
			StringBuilder builder = new StringBuilder();
			builder.append("Import folgender Konfigurationsbereiche wird gestartet:\n");
			for(String pid : pids) {
				builder.append(pid).append(" ");
			}
			_debug.info(builder.toString());

			// Datenmodell zuweisen
			_dataModel = dataModel;

			// Konfigurations-Steuerungs-Modul wird geladen
			final ConfigurationControl configurationControl = (ConfigurationControl)dataModel;

			// Versionsnummern der Konfigurationsbereiche ermitteln, in der sie betrachtet werden sollen
			determineUsingVersionOfConfigurationAreas(configurationControl);

			// Alle Bereiche und deren Versionen in der der Import stattfinden soll
			final List<ConfigAreaAndVersion> areasAndVersions = new ArrayList<ConfigAreaAndVersion>();
			for(ConfigurationArea area : _usingVersionOfConfigurationArea.keySet()) {
				areasAndVersions.add(new ConfigAreaAndVersion(area, _usingVersionOfConfigurationArea.get(area)));
			}

			// Modul zum Objektvergleich laden
			_objectDiffs = new ComparePropertiesWithSystemObjects(this, dataModel);

			// Die zu importierenden Daten werden geladen!
			// Parser f�r die XML-Dateien initialisieren
			ConfigAreaParser parser = null;
			try {
				parser = new ConfigAreaParser();
			}
			catch(Exception ex) {
				// ParserConfigurationException und SAXException werden hier abgefangen
				final String errorMessage = "Der Import wird abgebrochen, da bei der Initialisierung des Parsers zum Lesen der Import-Dateien ein Fehler auftrat";
				_debug.error(errorMessage);
				throw new RuntimeException(errorMessage, ex);
			}

			// Alle zu importierenden Konfigurationsbereiche werden eingelesen
			for(String configurationAreaPid : pids) {
				try {
					// File-Objekt f�r den Konfigurationsbereich erstellen und XML-Datei einlesen.
					final File file = new File(importPath, configurationAreaPid + ".xml");
					final ConfigurationAreaProperties configurationAreaProperties = parser.parse(file);

					// System-Objekt des Konfigurationsbereichs einlesen
					final ConfigurationArea configurationArea = assignConfigurationArea(configurationAreaProperties, configurationControl);
					_areaProperty2ConfigurationArea.put(configurationAreaProperties, configurationArea);

					// alle zu importierenden Objekte werden in einer ImportMap gespeichert
					for(SystemObjectProperties objectProperty : configurationAreaProperties.getObjectProperties()) {
						ImportObject importObject = _importMap.put(objectProperty.getPid(), new ImportObject(configurationArea, objectProperty));
						// Falls bereits ein Eintrag mit der Pid als Schl�ssel in der Map vorkommt,
						// muss eine Exception geworfen werden, damit auch alle Objekte verarbeitet werden.
						if(importObject != null) {
							throw new IllegalStateException(
									"Diese Pid " + objectProperty.getPid() + " wurde bereits in einem der anderen zu importierenden Bereiche "
									+ "verwendet. F�r den Import ist es notwendig, dass alle Pids der zu importierenden Bereich nur einmal vorkommen."
							);
						}
					}

					// Alle (aktuelle, in Bearbeitung, zur �bernahme/Aktivierung freigegebene) Objekte, die in diesem Konfigurationsbereich sind werden eingelesen.
					readExistingObjects(configurationArea);
					_allImportedConfigurationAreas.add(configurationArea);
					setSystemObjectKeeping(configurationArea);	// dieses Objekt soll beibehalten werden

					// Debug-Ausgabe:
					final StringBuilder debugStr = new StringBuilder();
					debugStr.append("Im Konfigurationsbereich ").append(configurationArea.getPid()).append(" gibt es ");
					debugStr.append((_currentObjects.get(configurationArea) == null ? 0 : _currentObjects.get(configurationArea).size())).append(
							" aktuelle Objekte, "
					);
					debugStr.append((_newObjects.get(configurationArea) == null ? 0 : _newObjects.get(configurationArea).size())).append(
							" freigegebene Objekte und "
					);
					debugStr.append((_editingObjects.get(configurationArea) == null ? 0 : _editingObjects.get(configurationArea).size())).append(
							" in Bearbeitung befindliche Objekte."
					);
					_debug.config(debugStr.toString());
				}
				catch(SAXException ex) {
					final StringBuilder errorMessage = new StringBuilder();
					errorMessage.append("Die Versorgungsdatei des Konfigurationsbereichs mit der Pid '").append(configurationAreaPid).append(
							"' konnte nicht eingelesen werden"
					);
					_debug.error(errorMessage.toString(), ex);
					throw new RuntimeException(errorMessage.toString(), ex);
				}
				catch(ConfigurationChangeException ex) {
					_debug.error("Beim Importieren einer Versorgungsdatei kam es zu Fehlern beim Umsetzen der entsprechenden �nderungen in der Konfiguration" , ex);
					throw new ConfigurationChangeException(ex);
				}
			}// for, �ber alle zu importierenden Bereiche

			_debug.info("Anzahl der Definitionen und SystemObjekte, die zu importieren sind", _importMap.values().size());

			_dissolveReference = true;
			int counter = 1;
			while(_dissolveReference) {
				// erstmal ist davon auszugehen, dass keine Referenzen aufzul�sen sind.
				_dissolveReference = false;
				/*
				 * 1.) Zu importierende Objekte, die noch nicht existieren oder nicht ver�ndert werden d�rfen, werden neu erstellt.
				 *     Objekte, die bereits existieren, werden also �berpr�ft, ob sie ge�ndert werden d�rfen, falls sie ge�ndert werden m�ssen.
				 */
				_debug.config("Alle Modelldaten der zu importierenden Konfigurationsbereiche werden erstellt.");
				for(ImportObject importObject : _importMap.values()) {
					if(!(importObject.getProperties() instanceof ConfigurationConfigurationObject)) {
						handleImportObject(importObject);
					}
				}

				_debug.config("Alle Objekte der zu importierenden Konfigurationsbereiche werden erstellt.");
				for(ImportObject importObject : _importMap.values()) {
					if(importObject.getProperties() instanceof ConfigurationConfigurationObject) {
						handleImportObject(importObject);
					}
				}
				if(_dissolveReference) {
					_debug.config("Bestehende Objekte wurden durch neue Objekte ersetzt - Referenzen m�ssen aufgel�st werden. Durchlauf", counter++);
					// ob ein Objekt beibehalten wird, muss hier zur�ckgesetzt werden!
					unsetSystemObjectKeeping();
					// alle Konfigurationsbereiche setzen, da diese in der Schleife nicht betrachtet werden.
					for(ConfigurationArea configurationArea : _allImportedConfigurationAreas) {
						setSystemObjectKeeping(configurationArea);
					}
				}
			}

			// pr�fen und ggf. handeln, falls sich der KV des Bereichs ge�ndert hat.
			checkChangeOfConfigurationAuthority();

			// 2.) Neu erstellte Objekte werden vervollst�ndigt und bereits existierende Objekte so ver�ndert, dass sie mit den Import-Daten �bereinstimmen.
			_debug.config("Alle Modelldaten der zu importierenden Konfigurationsbereiche werden vervollst�ndigt.");
			// erst alle Attributlisten (wird f�r die Default-Parameter-Datens�tze ben�tigt)
			for(ImportObject importObject : _importMap.values()) {
				if(importObject.getProperties() instanceof AttributeListProperties) {
					completeImportObject(importObject);
				}
			}
			// dann alle Attributgruppen (wird f�r die Default-Parameter-Datens�tze ben�tigt)
			for(ImportObject importObject : _importMap.values()) {
				if(importObject.getProperties() instanceof AttributeGroupProperties) {
					completeImportObject(importObject);
				}
			}

			for(ImportObject importObject : _importMap.values()) {
				if(!(importObject.getProperties() instanceof ConfigurationConfigurationObject)
				   && !(importObject.getProperties() instanceof AttributeGroupProperties)&& !(importObject.getProperties() instanceof AttributeListProperties)) {
					completeImportObject(importObject);
				}
			}

			_debug.config("Default-Werte werden vervollst�ndigt.");
			for(ImportObject importObject : _importMap.values()) {
				if(!(importObject.getProperties() instanceof ConfigurationConfigurationObject)) {
					completeDefaults(importObject);
				}
			}

			_debug.config("Alle Objekte der zu importierenden Konfigurationsbereiche werden vervollst�ndigt.");
			for(ImportObject importObject : _importMap.values()) {
				if(importObject.getProperties() instanceof ConfigurationConfigurationObject) {
					completeImportObject(importObject);
				}
			}

			// Nach dem Import muss noch aufger�umt werden. Nicht mehr ben�tigte Objekte werden auf ung�ltig gesetzt oder werden gel�scht.
			// Bereits auf ung�ltig gesetzte Objekte, werden wieder g�ltig, wenn sie gebraucht werden.
			_debug.config("Aufr�umen bestehender Objekte nach dem Import.");
			invalidateNoLongerRequiredObjects();
			deleteNoLongerRequiredObjects();
			validateRequiredObjects();

			// hier die Konfigurations�nderungen aktualisieren
			for(Map.Entry<ConfigurationAreaProperties, ConfigurationArea> entry : _areaProperty2ConfigurationArea.entrySet()) {
				setConfigurationAreaChangeInformation(entry.getKey(), entry.getValue());
			}

			long endTime = System.currentTimeMillis();
			_debug.info("Der Import wurde durchgef�hrt. Dauer in Sekunden", ((endTime - startTime) / 1000));
		}
		catch(Exception ex) {
			throw new ConfigurationChangeException(ex);
		}
	}

	/**
	 * Pr�ft, ob sich der KV des Bereichs ge�ndert hat. Wenn ja, dann wird ein neuer Zust�ndiger f�r diesen Bereich eingetragen. Wenn nicht, dann wird ein evtl.
	 * vorhandener neuer Zust�ndiger gel�scht.
	 *
	 * @throws ConfigurationChangeException Falls der neue Zust�ndige nicht geschrieben werden kann.
	 */
	private void checkChangeOfConfigurationAuthority() throws ConfigurationChangeException {
		final ConfigurationAuthority configurationAuthority = _dataModel.getConfigurationAuthority();
		// KV der Bereiche pr�fen
		if(configurationAuthority != null) {
			final AttributeGroup configurationAreaAtg = _dataModel.getAttributeGroup("atg.konfigurationsBereichEigenschaften");
			for(Map.Entry<ConfigurationAreaProperties, ConfigurationArea> entry : _areaProperty2ConfigurationArea.entrySet()) {
				final ConfigurationArea configurationArea = entry.getValue();
				final String areaPropertyAuthority = entry.getKey().getAuthority();
				if(!areaPropertyAuthority.equals(configurationArea.getConfigurationAuthority().getPid())) {
					// neuen Zust�ndigen eintragen
					final ConfigurationAuthority newAuthority = (ConfigurationAuthority)getObject(areaPropertyAuthority);
					if(newAuthority == null) {
						throwNoObjectException(areaPropertyAuthority);
					}

					for(ImportObject importObject : _importMap.values()) {
						if(importObject.getSystemObject() == newAuthority) {
							_debug.fine("Neuen KV vervollst�ndigen", newAuthority);
							completeImportObject(importObject);
						}
					}

					// ist die Kodierung eindeutig?
					checkCodingOfConfigurationAuthority(newAuthority.getCoding(), newAuthority.getPid());

					final Data data = getConfigurationData(configurationArea, configurationAreaAtg);
					data.getReferenceValue("neuerZust�ndiger").setSystemObject(newAuthority);
					configurationArea.setConfigurationData(configurationAreaAtg, data);
					_debug.info(
							"Der Bereich " + configurationArea.getPid() + " �ndert ab der n�chsten Version seinen Zust�ndigen zu " + newAuthority.getPid() + "."
					);
				}
				else {
					final ConfigurationAuthority newAuthority = (ConfigurationAuthority)getObject(areaPropertyAuthority);
					if(newAuthority == null) {
						throwNoObjectException(areaPropertyAuthority);
					}
					final Data data = getConfigurationData(configurationArea, configurationAreaAtg);
					if(data==null) {
						throw new IllegalStateException("Am Bereich " + configurationArea + " fehlt der Datensatz der Attributgruppe " + configurationAreaAtg);
					}
					final SystemObject oldConfigurationAuthority = data.getReferenceValue("zust�ndiger").getSystemObject();
					if(oldConfigurationAuthority == null || oldConfigurationAuthority.getId() != newAuthority.getId()) {

						for(ImportObject importObject : _importMap.values()) {
							if(importObject.getSystemObject() == newAuthority) {
								_debug.fine("Neuen KV vervollst�ndigen", newAuthority);
								completeImportObject(importObject);
							}
						}

						// ist die Kodierung eindeutig?
						checkCodingOfConfigurationAuthority(newAuthority.getCoding(), newAuthority.getPid());
						// neuen Zust�ndigen eintragen
						data.getReferenceValue("neuerZust�ndiger").setSystemObject(newAuthority);
						configurationArea.setConfigurationData(configurationAreaAtg, data);
						_debug.info(
								"Der Bereich " + configurationArea.getPid() + " �ndert ab der n�chsten Version seinen Zust�ndigen von " +
								oldConfigurationAuthority.getPid() + "/" + oldConfigurationAuthority.getId() + " zu " +
								newAuthority.getPid() + "/" + newAuthority.getId() + "."
						);
					}
					else {
						// pr�fen, ob beim neuen Zust�ndigen die null drinsteht!
						final Data.ReferenceValue referenceValue = data.getReferenceValue("neuerZust�ndiger");
						final SystemObject object = referenceValue.getSystemObject();
						if(object != null) {
							referenceValue.setSystemObject(null);
							configurationArea.setConfigurationData(configurationAreaAtg, data);
						}
					}
				}
			}
		}
	}

	/**
	 * Ermittelt die Version, jedes Konfigurationsbereichs, in der dieser betrachtet werden soll.
	 *
	 * @param configurationControl das Datenmodell
	 */
	private void determineUsingVersionOfConfigurationAreas(final ConfigurationControl configurationControl) {
		final Collection<ConfigurationArea> configurationAreas = configurationControl.getAllConfigurationAreas().values();
		for(ConfigurationArea configurationArea : configurationAreas) {
			// Dieser Vergleich ist auch m�glich, wenn der KV in den zu importierenden Bereichen neu angelegt wurde, da dann
			// _dataModel.getConfigurationAuthority() == null zur�ckgibt. Diese Gleichung sollte f�r keinen Bereich zutreffen, da alle einen anderen
			// Verantwortlichen besitzen.
			ConfigurationAuthority areaAuthority = null;
			try {
				areaAuthority = configurationArea.getConfigurationAuthority();
			}
			catch(IllegalStateException ignore) {
			}

			if(areaAuthority == _dataModel.getConfigurationAuthority() || areaAuthority.getPid().equals(_dataModel.getConfigurationAuthorityPid())) {
				_usingVersionOfConfigurationArea.put(configurationArea, configurationArea.getModifiableVersion());
			}
			else {
				_usingVersionOfConfigurationArea.put(configurationArea, configurationArea.getTransferableVersion());
			}
		}
	}

	/**
	 * Diese Methode pr�ft, ob es bereits einen Konfigurationsbereich passend zur Versorgungsdatei (dargestellt durch das {@link ConfigurationAreaProperties
	 * Eigenschafts-Objekt}) gibt. Existiert er noch nicht, so wird eine neue Bereichs-Datei angelegt.
	 *
	 * @param property             Eigenschafts-Objekt, welches die Versorgungsdatei repr�sentiert
	 * @param configurationControl Objekt, welches spezielle Zugriffsmethoden auf die Konfiguration enth�lt
	 *
	 * @return Konfigurationsbereich, passend zur Versorgungsdatei.
	 *
	 * @throws ConfigurationChangeException Falls kein neuer Konfigurationsbereich angelegt werden konnte.
	 */
	private ConfigurationArea assignConfigurationArea(ConfigurationAreaProperties property, ConfigurationControl configurationControl)
			throws ConfigurationChangeException {
		// Konfigurationsbereich aus der aktuellen Konfiguration auslesen
		ConfigurationArea configurationArea = configurationControl.getAllConfigurationAreas().get(property.getPid());

		if(configurationArea == null) {
			// es gibt noch keinen Bereich -> Pid des KV der Konfiguration mit der Pid des KV des Bereichs-Properties vergleichen
			if(!property.getAuthority().equals(_dataModel.getConfigurationAuthorityPid())) {
				final String errorMessage = "Der Konfigurationsverantwortliche '" + property.getAuthority() + "' des Konfigurationsbereichs '"
				                            + property.getPid() + "' entspricht nicht dem Konfigurationsverantwortlichen der Konfiguration '"
				                            + _dataModel.getConfigurationAuthorityPid() + "'";
				throw new IllegalArgumentException(errorMessage);
			}
			configurationArea = createConfigurationArea(property, configurationControl);
		}
		else {
			// Konfigurationsbereich gibt es bereits -> KV der Konfiguration mit dem KV des Bereichs vergleichen
			final ConfigurationAuthority areaAuthority = configurationArea.getConfigurationAuthority();
			final ConfigurationAuthority configurationAuthority = _dataModel.getConfigurationAuthority();
			if(configurationAuthority == null) {
				if(areaAuthority.getPid().equals(_dataModel.getConfigurationAuthorityPid())) {
					((ConfigDataModel)_dataModel).setConfigurationAuthority(areaAuthority);
				}
				else {
					final String errorMessage = "Der Konfigurationsverantwortliche '" + areaAuthority.getPid() + "' des Konfigurationsbereichs '"
					                            + configurationArea.getPid() + "' entspricht nicht dem Konfigurationsverantwortlichen der Konfiguration '"
					                            + _dataModel.getConfigurationAuthorityPid() + "'";
					throw new IllegalStateException(errorMessage);
				}
			}
			else {
				if(areaAuthority != configurationAuthority) {
					final String errorMessage = "Der Konfigurationsverantwortliche '" + areaAuthority.getPid() + "' des Konfigurationsbereichs '"
					                            + configurationArea.getPid() + "' entspricht nicht dem Konfigurationsverantwortlichen der Konfiguration '"
					                            + _dataModel.getConfigurationAuthorityPid() + "'";
					throw new IllegalStateException(errorMessage);
				}
			}
			checkConfigurationArea(property, configurationArea);
		}
		// Info �berpr�fen
		if(_objectDiffs.isInfoDifferent(configurationArea.getInfo(), property.getInfo()) && _objectDiffs.isInfoProcessable(
				configurationArea.getInfo(), property.getInfo()
		)) {
			setInfo(configurationArea, property.getInfo());
		}
		// Konfigurations�nderungen �berpr�fen, ob der Datensatz ge�ndert werden kann.
		if(!_objectDiffs.isConfigurationAreaChangeInformationProcessable(property.getConfigurationAreaChangeInformation(), configurationArea)) {
			throw new ConfigurationChangeException(
					"Die Konfigurations�nderungen k�nnen nicht am Konfigurationsbereich " + configurationArea.getPidOrNameOrId() + " ge�ndert werden."
			);
		}
		// Darf hier nicht mehr gemacht werden, weil die richtige Versionsnummer f�r die Aktualisierung der Konfigurations�nderungseintr�ge
		// noch nicht feststeht.
		// setConfigurationAreaChangeInformation(property, configurationArea);

		// die Zeiten, wann sich ein Datensatz ge�ndert oder ein Objekt erzeugt wurde, m�ssen initialisiert werden
		((ConfigConfigurationArea)configurationArea).initialiseTimeOfLastChanges();
		return configurationArea;
	}

	/**
	 * Setzt die Konfigurations�nderungen an einem Konfigurationsbereich.
	 *
	 * @param property          eingelesene Werte der Versorgungsdatei
	 * @param configurationArea Konfigurationsbereich
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht geschrieben werden kann.
	 */
	private void setConfigurationAreaChangeInformation(final ConfigurationAreaProperties property, final ConfigurationArea configurationArea)
			throws ConfigurationChangeException {
		if(_objectDiffs.isConfigurationAreaChangeInformationDifferent(property.getConfigurationAreaChangeInformation(), configurationArea)) {
			setConfigurationAreaChangeInformation(property.getConfigurationAreaChangeInformation(), configurationArea);
		}
	}

	/**
	 * Der Konfigurationsbereich wird �berpr�ft, ob auch er ge�ndert wurde.
	 *
	 * @param property          Eigenschaftsobjekt des Konfigurationsbereichs
	 * @param configurationArea Konfigurationsbereichs-Objekt
	 *
	 * @throws ConfigurationChangeException Falls es Unterschiede zwischen des Eigenschaftsobjekts und Konfigurationsbereichs-Objekts gibt.
	 */
	private void checkConfigurationArea(final ConfigurationAreaProperties property, final ConfigurationArea configurationArea)
			throws ConfigurationChangeException {
		// Der Konfigurationsbereich existiert bereits - pr�fen, ob alle Werte �bereinstimmen.
		// Namen des Konfigurationsbereichs �berpr�fen
		if(_objectDiffs.isNameDifferent(property.getName(), configurationArea.getName())) {
			if(_objectDiffs.isNameProcessable(property.getName(), configurationArea)) {
				configurationArea.setName(property.getName());
			}
			else {
				throw new ConfigurationChangeException(
						"Der Name des Konfigurationsbereichs darf nicht ge�ndert werden. Alter Name: " + configurationArea.getName() + " neuer Name: "
						+ property.getName()
				);
			}
		}

		// Konfigurationsbereiche k�nnen nicht ver�ndert werden - wenn einer ver�ndert werden soll, muss ein neuer Bereich angelegt werden
		if(configurationArea.getNotValidSince() != 0) {
			
			((ConfigConfigurationObject)configurationArea).simpleRevalidate();
		}
	}

	/**
	 * Diese Methode erstellt einen neuen Konfigurationsbereich.
	 *
	 * @param property             Das Eigenschafts-Objekt zum Bereich.
	 * @param configurationControl Wird zum Erstellen eines neuen Bereichs ben�tigt.
	 *
	 * @return der neue Konfigurationsbereich
	 *
	 * @throws ConfigurationChangeException Falls der neue Bereich nicht erstellt werden konnte.
	 */
	private ConfigurationArea createConfigurationArea(final ConfigurationAreaProperties property, final ConfigurationControl configurationControl)
			throws ConfigurationChangeException {
		final ConfigurationArea configurationArea;
		// Gibt es diesen Konfigurationsbereich nicht, muss ein neuer angelegt werden.
		_debug.info("Konfigurationsbereich " + property.getPid() + " wird neu erstellt.");
		// gibt es den Konfigurationsverantwortlichen bereits? Oder muss der auch neu angelegt werden? Er muss nicht aktiv sein.
		ConfigurationAuthority authority = null;
		try {
			authority = (ConfigurationAuthority)_dataModel.getObject(property.getAuthority());
		}
		catch(IllegalStateException ignore) {
		}

		if(authority != null) {
			configurationArea = configurationControl.createConfigurationArea(property.getName(), property.getPid(), property.getAuthority());
		}
		else {
			// KV existiert noch nicht - Daten des Konfigurationsverantwortlichen ermitteln
			final List<SystemObjectProperties> objectProperties = property.getObjectProperties();
			SystemObjectType authorityType = null;
			String authorityPid = "";
			String authorityName = "";
			short authorityCoding = -1;
			for(SystemObjectProperties systemObjectProperties : objectProperties) {
				if(systemObjectProperties.getPid().equals(property.getAuthority())) {
					// Objekt des Konfigurationsverantwortlichen gefunden
					// es handelt sich um ein Konfigurationsobjekt
					ConfigurationConfigurationObject configurationObjectProperties = (ConfigurationConfigurationObject)systemObjectProperties;
					// der Typ des Konfigurationsverantwortlichen muss zu den aktiven Objekten geh�ren
					authorityType = _dataModel.getType(configurationObjectProperties.getType());
					authorityPid = configurationObjectProperties.getPid();
					authorityName = configurationObjectProperties.getName();
					authorityCoding = getAuthorityCoding(configurationObjectProperties);
					break;
				}
			}
			if(authorityCoding == -1 || authorityType == null) {
				throw new IllegalStateException(
						"Es gibt kein aktives Objekt des Konfigurationsverantwortlichen '" + property.getAuthority()
						+ "' und es wird kein neuer Konfigurationsverantwortlicher in diesem Bereich '" + property.getPid() + "' angelegt."
				);
			}
			// Pr�fen, ob Kodierung eindeutig ist. Alle KVs betrachten und pr�fen.
			checkCodingOfConfigurationAuthority(authorityCoding, authorityPid);

			// Bereich und Konfigurationsverantwortlicher werden erstellt
			configurationArea = ((ConfigDataModel)_dataModel).createConfigurationArea(
					property.getName(), property.getPid(), authorityType, authorityPid, authorityName, authorityCoding
			);

			// Der Konfigurationsverantwortliche muss hinzugef�gt werden, da er gerade erst erstellt wurde.
			if(_viewingObjects != null) {
				final ConfigurationAuthority configurationAuthority = configurationArea.getConfigurationAuthority();
				_viewingObjects.put(configurationAuthority.getPid(), configurationAuthority);
			}
		}
		setInfo(configurationArea, property.getInfo());

		// ein neu angelegter Bereich muss in der Map mit aufgenommen werden
		_usingVersionOfConfigurationArea.put(configurationArea, configurationArea.getModifiableVersion());
		if(_viewingObjects != null) {
			_viewingObjects.put(configurationArea.getPid(), configurationArea);
		}
		return configurationArea;
	}

	/**
	 * Diese Methode pr�ft, ob die Kodierung des zu verwendenden Konfigurationsverantwortlichen eindeutig ist. Es darf also keinen aktuellen Verantwortlichen
	 * geben, der die gleiche Kodierung verwendet.
	 *
	 * @param authorityCoding zu vergleichende Kodierung
	 * @param authorityPid    Pid des Konfigurationsverantwortlichen
	 *
	 * @throws ConfigurationChangeException Falls die Kodierung nicht eindeutig ist.
	 */
	private void checkCodingOfConfigurationAuthority(final short authorityCoding, final String authorityPid) throws ConfigurationChangeException {
		final List<SystemObject> allAuthorities = _dataModel.getType("typ.konfigurationsVerantwortlicher").getObjects();
		for(SystemObject systemObject : allAuthorities) {
			final ConfigurationAuthority foreignAuthority = (ConfigurationAuthority)systemObject;
			if(!authorityPid.equals(foreignAuthority.getPid()) && authorityCoding == foreignAuthority.getCoding()) {
				throw new ConfigurationChangeException(
						"Die Kodierung " + authorityCoding + " des Konfigurationsverantwortlichen " + authorityPid
						+ " kollidiert mit dem Konfigurationsverantwortlichen " + foreignAuthority.getPid() + "."
				);
			}
		}
	}

	/**
	 * Ermittelt zu einem Konfigurationsobjekt eines Konfigurationsverantwortlichen die Kodierung.
	 *
	 * @param configurationObjectProperties Konfigurationsobjekt
	 *
	 * @return Kodierung des Konfigurationsverantwortlichen
	 */
	private short getAuthorityCoding(final ConfigurationConfigurationObject configurationObjectProperties) {
		final ConfigurationObjectElements[] datasetAndObjectSet = configurationObjectProperties.getDatasetAndObjectSet();
		for(ConfigurationObjectElements element : datasetAndObjectSet) {
			if(element instanceof ConfigurationDataset) {
				// Datensatz ermitteln
				ConfigurationDataset dataset = (ConfigurationDataset)element;
				if(dataset.getPidATG().equals("atg.konfigurationsVerantwortlicherEigenschaften") && dataset.getPidAspect().equals(
						"asp.eigenschaften"
				)) {
					final DatasetElement[] data = dataset.getDataAnddataListAndDataField();
					for(DatasetElement datasetElement : data) {
						if(datasetElement instanceof ConfigurationData) {
							final ConfigurationData configurationData = (ConfigurationData)datasetElement;
							if(configurationData.getName().equals("kodierung")) {
								return Short.valueOf(configurationData.getValue());
							}
						}
					}
				}
			}
		}
		// Es konnte keine Kodierung ermittelt werden
		throw new IllegalStateException("Zum Konfigurationsverantwortlichen '" + configurationObjectProperties.getPid() + "' gibt es keine Kodierung.");
	}

	/**
	 * Erzeugt einen Datensatz mit den Konfigurations�nderungen aus der Versorgungsdatei und speichert diesen am Konfigurationsbereich ab.
	 *
	 * @param configurationAreaChangeInformation
	 *                          die Konfigurations�nderungen aus der Versorgungsdatei
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht am Konfigurationsbereich gespeichert werden konnte.
	 */
	private void setConfigurationAreaChangeInformation(
			ConfigurationAreaChangeInformation[] configurationAreaChangeInformation, ConfigurationArea configurationArea
	) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.konfigurations�nderungen");

		// bestehendes Data holen
		final Data existData = getConfigurationData(configurationArea, atg);

		// neues Data wird erzeugt!
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		final Data.Array array = data.getArray("Konfigurations�nderung");
		// L�nge festlegen
		array.setLength(configurationAreaChangeInformation.length);
		int i = 0;
		final short lastModifiedVersion = ((ConfigConfigurationArea)configurationArea).getLastModifiedVersion();
		for(ConfigurationAreaChangeInformation information : configurationAreaChangeInformation) {
			Data item = array.getItem(i++);
			item.getTimeValue("Stand").setMillis(information.getCondition());
			item.getTextValue("Autor").setText(information.getAuthor());
			item.getTextValue("Grund").setText(information.getReason());
			item.getTextValue("Text").setText(information.getText());
			int version = information.getVersion();
			if(version == -1) version = existChangeInformation(existData, information);    // Version aus dem bestehenden Datensatz ermitteln
			if(version == -1) version = lastModifiedVersion;   // Falls keine Version ermittelt werden konnte
			if(version > lastModifiedVersion) version = lastModifiedVersion; // Version auf die gr��te verwendete Version beschr�nken
			item.getUnscaledValue("Version").set(version);
		}
		configurationArea.setConfigurationData(atg, data);
	}

	/**
	 * Ermittelt die Versionsnummer zu einem �nderungsvermerk, der zuvor schon mal gespeichert wurde.
	 *
	 * @param existData existierendes Data zu den Konfigurations-�nderungen
	 * @param info      ein neuer Info-Eintrag
	 *
	 * @return Versionsnummer dieses Info-Eintrages oder "-1", falls der Eintrag noch nicht existierte.
	 */
	private int existChangeInformation(Data existData, ConfigurationAreaChangeInformation info) {
		if(existData == null) return -1;
		Data.Array array = existData.getArray("Konfigurations�nderung");
		for(int i = 0; i < array.getLength(); i++) {
			Data item = array.getItem(i);
			if(item.getTextValue("Text").getText().equals(info.getText()) && item.getTextValue("Grund").getText().equals(info.getReason())
			   && item.getTextValue("Autor").getText().equals(info.getAuthor()) && item.getTimeValue("Stand").getMillis() == info.getCondition()) {
				return item.getUnscaledValue("Version").intValue();
			}
		}
		return -1;
	}

	/**
	 * Liest alle aktuellen und zur Aktivierung/�bernahme freigegebenen Objekte aus dem Konfigurationsbereich aus und merkt sich diese lokal.
	 *
	 * @param configurationArea ein zu importierender Konfigurationsbereich
	 */
	private void readExistingObjects(ConfigurationArea configurationArea) {
		// Alle aktuellen Objekte dieses Bereichs werden eingelesen und in einer Map gespeichert.
		final Collection<CheckedObject> currentCheckedObjects = new LinkedList<CheckedObject>();
		for(SystemObject systemObject : configurationArea.getCurrentObjects()) {
			final CheckedObject checkedObject = new CheckedObject(systemObject);
			currentCheckedObjects.add(checkedObject);
		}
		_currentObjects.put(configurationArea, currentCheckedObjects);

		// Alle zuk�nftigen, bereits vorhandene Objekte einladen - unterteilt nach freigegebenen und in Bearbeitung befindlichen Objekten.
		// Die gr��ere Versionsnummer zwischen der "�bernehmbaren" und der "Aktivierbaren" Version ermitteln.
		short releasedVersion = configurationArea.getActivatableVersion();
		if(configurationArea.getTransferableVersion() > releasedVersion) {
			releasedVersion = configurationArea.getTransferableVersion();
		}

		final Collection<CheckedObject> newCheckedObjects = new ArrayList<CheckedObject>();
		final Collection<CheckedObject> editingCheckedObjects = new ArrayList<CheckedObject>();
		for(SystemObject systemObject : configurationArea.getNewObjects()) {
			// nach konfigurierenden und dynamischen Objekten unterscheiden
			if(systemObject instanceof ConfigurationObject) {
				ConfigurationObject configObject = (ConfigurationObject)systemObject;
				final CheckedObject checkedObject = new CheckedObject(systemObject);
				if(configObject.getValidSince() <= releasedVersion) {
					// Objekt wurde bereits freigegeben
					newCheckedObjects.add(checkedObject);
				}
				else {
					// Objekt befindet sich in Bearbeitung
					editingCheckedObjects.add(checkedObject);
				}
			}
			else {
				// Ein dynamisches Objekt ist entweder aktuell oder veraltet. Sobald es erstellt wurde, gilt ein dynamisches Objekt als aktuell, somit
				// kann es nicht bei den zur �bernahme/Aktivierung freigegebenen und in Bearbeitung befindlichen Objekten sein.
				_debug.error("Ein dynamisches Objekt bei den neuen Objekten gefunden", systemObject.getPidOrNameOrId());
			}
		}
		// ermittelte Objekte hinzuf�gen
		_newObjects.put(configurationArea, newCheckedObjects);
		_editingObjects.put(configurationArea, editingCheckedObjects);
	}

	/**
	 * Diese Methode pr�ft, ob es bereits ein passendes Objekt zu einer zu importierenden Definition gibt und verwendet dieses. Wenn dieses Objekt allerdings
	 * ver�ndert werden muss, wird �berpr�ft, ob es auch ver�ndert werden darf. Wenn es nicht ver�ndert werden darf, wird ein neues Objekt angelegt.
	 * <p/>
	 * Wird ein passendes Objekt gefunden, dann wird damit weitergearbeitet. Wenn nicht, dann wird ein neues Objekt erstellt.
	 *
	 * @param importObject Import-Objekt, welches ein Import-Objekt und ein System-Objekt enth�lt.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht angelegt werden.
	 */
	private void handleImportObject(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final String pid = importObject.getProperties().getPid();
		_debug.finer("*** Folgende Pid wird jetzt weiterverarbeitet", pid);
		CheckedObject checkedObject = getNewObject(configurationArea, pid);
		if(checkedObject != null) {
			// Es gibt ein zur �bernahme / Aktivierung freigegebenes Objekt.
			// wenn dieses Objekt ge�ndert werden muss und ge�ndert werden darf, dann soll es weiter verwendet werden
			boolean isObjectProcessable = _objectDiffs.isObjectProcessable(importObject.getProperties(), checkedObject.getSystemObject());
			if(_objectDiffs.isObjectDifferent(importObject.getProperties(), checkedObject.getSystemObject())) {
				_debug.fine(
						"Das zur �bernahme / Aktivierung freigegebene Objekt mit Pid '" + pid + "' und Id '" + checkedObject.getSystemObject().getId()
						+ "' muss ge�ndert werden. Darf es ge�ndert werden? " + isObjectProcessable
				);
			}
			else {
				_debug.finer(
						"Objekt mit Pid " + checkedObject.getSystemObject().getPid()
						+ " unterscheidet sich nicht von einem zur �bernahme / Aktivierung freigegebene Objekt!"
				);
			}
			if(isObjectProcessable) {
				// wenn es ge�ndert werden kann, dann wird dieses weiterverwendet, wenn nicht, dann muss ein neues angelegt werden.
				final ConfigurationObject configurationObject = getModifiableObject(configurationArea, pid);
				if(configurationObject != null) {
					// ein Objekt mit gleicher Pid welches in Bearbeitung ist, muss gel�scht werden, da
					// das aktive Objekt verwendet wird
					
					((ConfigSystemObject)configurationObject).simpleInvalidation();
					_dissolveReference = true;
				}
				handleObject(checkedObject, importObject);
				return;
			}
			else {
				if(importObject.getSystemObject() == null || importObject.getSystemObject() == checkedObject.getSystemObject()) {
					// Wenn die beiden Objekte gleich sind, diese aber nicht weiterverarbeitet werden d�rfen, dann m�ssen
					// die Referenzen mit einem neuen Objekt aufgel�st werden.
					_debug.finest("DissolveReference because Pid " + pid);
					_dissolveReference = true;
					importObject.setSystemObject(null);
				}
			}
		}
		else {
			// Es gibt kein zur �bernahme / Aktivierung freigegebenes Objekt - gibt es ein aktuelles Objekt?
			checkedObject = getCurrentObject(configurationArea, pid);
			if(checkedObject != null) {
				// es gibt ein aktuelles Objekt
				// wenn dieses Objekt ge�ndert werden muss und ge�ndert werden darf, dann soll es weiter verwendet werden
				boolean isObjectProcessable = _objectDiffs.isObjectProcessable(importObject.getProperties(), checkedObject.getSystemObject());
				if(_objectDiffs.isObjectDifferent(importObject.getProperties(), checkedObject.getSystemObject())) {
					_debug.fine(
							"Das aktuelle Objekt mit Pid '" + pid + "' und Id '" + checkedObject.getSystemObject().getId() + "' muss ge�ndert werden (Typ: "
							+ checkedObject.getSystemObject().getType().getPidOrNameOrId() + "). Darf es ge�ndert werden? " + isObjectProcessable
					);
				}
				else {
					_debug.finer("Objekt mit Pid " + checkedObject.getSystemObject().getPid() + " unterscheidet sich nicht von einem aktuellen Objekt!");
				}
				if(isObjectProcessable) {
					// wenn es ge�ndert werden kann, dann wird dieses weiterverwendet, wenn nicht, dann muss ein neues angelegt werden.
					final ConfigurationObject configurationObject = getModifiableObject(configurationArea, pid);
					if(configurationObject != null) {
						// ein Objekt mit gleicher Pid welches in Bearbeitung ist, muss gel�scht werden, da
						// das aktive Objekt verwendet wird
						
						((ConfigSystemObject)configurationObject).simpleInvalidation();
						_dissolveReference = true;
					}
					handleObject(checkedObject, importObject);
					return;
				}
				else {
					if(importObject.getSystemObject() == null || importObject.getSystemObject() == checkedObject.getSystemObject()) {
						// Wenn die beiden Objekte gleich sind, diese aber nicht weiterverarbeitet werden d�rfen, dann m�ssen
						// die Referenzen mit einem neuen Objekt aufgel�st werden.
						final SystemObject checkedSystemObject = checkedObject.getSystemObject();
						if(checkedSystemObject instanceof DynamicObject) {
							_debug.fine("dynamisches Objekt wird gel�scht", checkedSystemObject);
							forgetCurrentObject(configurationArea, checkedSystemObject);
							((ConfigSystemObject)checkedSystemObject).simpleInvalidation();
						}
						_debug.finest("DissolveReference because Pid " + pid);
						_dissolveReference = true;
						importObject.setSystemObject(null);
					}
				}
			}
		}
		// Es gibt weder ein zur �bernahme / Aktivierung freigegebenes Objekt, noch ein aktuelles Objekt.
		// Gibt es ein in Bearbeitung befindliches Objekt, welches vor dem Import bereits in Bearbeitung war?
		checkedObject = getEditingObject(configurationArea, pid);
		if(checkedObject != null) {
			// Es gibt ein in Bearbeitung befindliches Objekt -> dies kann nach belieben ge�ndert werden.
			_debug.fine("Ein in Bearbeitung befindliches Objekt mit der Pid '" + pid + "' wird weiterverarbeitet.");
			// Objekt weiterverarbeiten - es kann nach belieben ver�ndert werden
			handleObject(checkedObject, importObject);
			return;
		}
		if(importObject.getSystemObject() == null) {
			_debug.fine("Ein neues Objekt mit der Pid '" + pid + "' wird erstellt.");
			_dissolveReference = true;
		}
		else {
			_debug.fine("Ein importiertes Objekt mit der Pid '" + pid + "' wird weiterverarbeitet.");
		}
		handleObject(null, importObject);
	}

	/**
	 * Ermittelt zu einer Pid ein in Bearbeitung befindliches Objekt, l�scht es aus der entsprechenden Liste, damit es nicht nochmal betrachtet wird und gibt es
	 * zur�ck.
	 *
	 * @param area Konfigurationsbereich, in dem das Objekt sein soll
	 * @param pid  Pid des Objekts
	 *
	 * @return SystemObjekt mit der angegebenen Pid oder <code>null</code>, falls es kein Objekt zu der Pid gibt.
	 */
	private ConfigurationObject getModifiableObject(final ConfigurationArea area, final String pid) {
		final Collection<CheckedObject> checkedObjects = _editingObjects.get(area);
		for(CheckedObject checkedObject : checkedObjects) {
			final SystemObject systemObject = checkedObject.getSystemObject();
			if(systemObject.getPid().equals(pid)) {
				if(systemObject instanceof ConfigurationObject) {
					checkedObjects.remove(checkedObject);
					return (ConfigurationObject)systemObject;
				}
			}
		}
		return null;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein zu importierendes Objekt vervollst�ndigt werden soll. Bei diesem Vorgang werden Mengen und Datens�tze mit Referenzen
	 * erzeugt oder ver�ndert. Je nach Typ des Eigenschafts-Objekts wird die entsprechende Methode zur Weiterverarbeitung aufgerufen.
	 *
	 * @param importObject Import-Objekt, welches ein Import-Objekt und ein System-Objekt enth�lt.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollst�ndigt werden konnte (Mengen und Datens�tze konnten nicht hinzugef�gt werden).
	 */
	private void completeImportObject(ImportObject importObject) throws ConfigurationChangeException {
		// Eigenschafts-Objekt des zu importierenden Objekts
		final SystemObjectProperties property = importObject.getProperties();
		final SystemObject systemObject = importObject.getSystemObject();
		// diese Objekte sollten alle bereits erstellt sein und ben�tigen noch die konfigurierenden Datens�tze
		_debug.finer("### Folgendes Objekt wird jetzt vervollst�ndigt", systemObject.getPidOrNameOrId());
		if(systemObject != null) {
			// je nachdem, um welches Eigenschafts-Objekt es sich handelt, m�ssen unterschiedliche Datens�tze/Mengen vervollst�ndigt werden
			if(property instanceof AspectProperties) {
				// ist bereits vollst�ndig erstellt worden
			}
			else if(property instanceof AttributeListProperties) {
				completeAttributeListDefinition(importObject);
			}
			else if(property instanceof AttributeTypeProperties) {
				completeAttributeType(importObject);
			}
			else if(property instanceof AttributeGroupProperties) {
				completeAttributeGroup(importObject);
			}
			else if(property instanceof ConfigurationAreaProperties) {
				// wird ignoriert, da bereits ausgewertet
			}
			else if(property instanceof ObjectSetTypeProperties) {
				completeObjectSetType(importObject);
			}
			else if(property instanceof SystemObjectTypeProperties) {
				completeSystemObjectType(importObject);
			}
			else {
				// ConfigurationObjectProperties und SystemObjectProperties werden gleicherma�en verarbeitet
				completeSystemObject(importObject);
			}
		}
		else {
			_debug.warning("Zu diesen Properties '" + property.getPid() + "' gibt es kein System-Objekt!");
		}
	}

	/**
	 * Default-Werte werden an den Objekten vervollst�ndigt.
	 *
	 * @param importObject Import-Objekte
	 *
	 * @throws ConfigurationChangeException Falls die Default-Werte nicht gesetzt werden konnten.
	 */
	private void completeDefaults(ImportObject importObject) throws ConfigurationChangeException {
		// Eigenschafts-Objekt des zu importierenden Objekts
		final SystemObjectProperties property = importObject.getProperties();
		final SystemObject systemObject = importObject.getSystemObject();
		if(systemObject != null) {
			if(property instanceof AttributeTypeProperties) {
				completeDefaults((AttributeType)systemObject, (AttributeTypeProperties)property);
			}
		}
	}

	/**
	 * Aktuelle Objekte und Objekte, die zur �bernahme / Aktivierung freigegeben wurden werden gepr�ft, ob sie bei einem vorhergehenden Import auf ung�ltig gesetzt
	 * wurden, nach diesem Import aber ben�tigt werden. Diese Objekte werden dann mittels {@link ConfigurationObject#revalidate() revalidate} wieder g�ltig.
	 *
	 * @throws ConfigurationChangeException Falls ein ben�tigtes Objekt nicht zur�ck auf g�ltig gesetzt werden kann.
	 */
	private void validateRequiredObjects() throws ConfigurationChangeException {
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				if(checkedObject.isObjectKeeping()) {
					// das Objekt wird noch ben�tigt, wenn es schon mal auf ung�ltig gesetzt wurde, wieder auf g�ltig setzen
					final SystemObject systemObject = checkedObject.getSystemObject();
					if(systemObject instanceof ConfigurationObject) {
						ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
						if(configurationObject.getNotValidSince() != 0) {
							_debug.finer(
									"Habe ein Objekt gefunden, welches in der n�chsten Version ung�ltig wird, aber noch gebraucht wird. Es wird wieder g�ltig gemacht: "
									+ configurationObject.getPidOrNameOrId()
							);
							
							((ConfigConfigurationObject)configurationObject).simpleRevalidate();
						}
					}
				}
			}
		}
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				if(checkedObject.isObjectKeeping()) {
					// das Objekt wird noch ben�tigt, wenn es schon mal auf ung�ltig gesetzt wurde, wieder auf g�ltig setzen
					final SystemObject systemObject = checkedObject.getSystemObject();
					if(systemObject instanceof ConfigurationObject) {
						ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
						if(configurationObject.getNotValidSince() != 0) {
							_debug.finer(
									"Habe ein Objekt gefunden, welches in der n�chsten Version gebraucht wird, aber ung�ltig wird: "
									+ configurationObject.getPidOrNameOrId()
							);
							
							((ConfigConfigurationObject)configurationObject).simpleRevalidate();
						}
					}
				}
			}
		}
	}

	/**
	 * Aktuelle und freigegebene Objekte werden �berpr�ft, ob sie nach dem Import weiterhin gebraucht werden. Werden sie nicht ben�tigt, dann werden sie auf
	 * ung�ltig gesetzt.
	 */
	private void invalidateNoLongerRequiredObjects() {
		_debug.finer("Folgende Objekte werden auf ung�ltig gesetzt: ");
		// alle aktuellen Objekte betrachten
		int counter1 = 0;
		int counter2 = 0;
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr ben�tigt wird, wird es auf ung�ltig gesetzt.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr ben�tigtes aktuelles Objekt = " + systemObject.toString());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das aktuelle Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht ung�ltig gemacht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der aktuellen Objekte, die auf ung�ltig gesetzt wurden", counter1);
		if(counter2 > 0) _debug.info("Anzahl der aktuellen Objekte, die g�ltig bleiben", counter2);

		counter1 = 0;
		counter2 = 0;

		// die freigegebenen Objekte
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr ben�tigt wird, wird es auf ung�ltig gesetzt.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr ben�tigtes freigegebenes Objekt = " + systemObject.getPidOrNameOrId());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das freigegebene Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht ung�ltig gemacht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der zur �bernahme/Aktivierung freigegebenen Objekte, die auf ung�ltig gesetzt wurden", counter1);
		if(counter2 > 0) _debug.info("Anzahl der zur �bernahme/Aktivierung freigegebenen Objekte, die g�ltig bleiben", counter2);
	}

	/** In Bearbeitung befindliche Objekte werden �berpr�ft, ob sie nach dem Import nicht mehr ben�tigt werden. Wenn dem so ist, werden sie gel�scht. */
	private void deleteNoLongerRequiredObjects() {
		_debug.finer("Folgende in Bearbeitung befindliche Objekte werden gel�scht: ");
		int counter1 = 0;
		int counter2 = 0;
		for(ConfigurationArea configurationArea : _editingObjects.keySet()) {
			for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr ben�tigt wird, wird es gel�scht.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr ben�tigtes in Bearbeitung befindliches Objekt = " + systemObject.getPidOrNameOrId());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen.
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das in Bearbeitung befindliche Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht gel�scht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der in Bearbeitung befindlichen gel�schten Objekte", counter1);
		if(counter2 > 0) _debug.info("Anzahl der in Bearbeitung befindlichen nicht gel�schten Objekte", counter2);
	}

	/* ############################## Methoden zur Bearbeitung und Erstellung von Objekten ############################ */

	/**
	 * Ermittelt anhand des Eigenschafts-Objekts, welche Methode aufgerufen werden muss, damit das System-Objekt weiterverarbeitet bzw. erstellt wird.
	 *
	 * @param checkedObject Enth�lt das System-Objekt, welches weiterverarbeitet werden soll. <code>null</code>, wenn es neues Objekt erstellt werden soll und noch
	 *                      keines vorhanden war.
	 * @param importObject  Import-Objekt, welches ein Import-Objekt und ein System-Objekt enth�lt.
	 *
	 * @throws ConfigurationChangeException Falls ein Objekt nicht importiert werden konnte.
	 */
	private void handleObject(CheckedObject checkedObject, ImportObject importObject) throws ConfigurationChangeException {
		final SystemObjectProperties property = importObject.getProperties();
		if(checkedObject != null) {
			importObject.setSystemObject(checkedObject.getSystemObject());
		}

		if(property instanceof AspectProperties) {
			handleAspect(importObject);
		}
		else if(property instanceof AttributeListProperties) {
			handleAttributeListDefinition(importObject);
		}
		else if(property instanceof AttributeTypeProperties) {
			handleAttributeType(importObject);
		}
		else if(property instanceof AttributeGroupProperties) {
			handleAttributeGroup(importObject);
		}
		else if(property instanceof ConfigurationAreaProperties) {
			_debug.error("Ein weiterer Konfigurationsbereich soll importiert werden", property.getPid());
		}
		else if(property instanceof ObjectSetTypeProperties) {
			handleObjectSetType(importObject);
		}
		else if(property instanceof SystemObjectTypeProperties) {
			handleSystemObjectType(importObject);
		}
		else {
			// ConfigurationObjectProperties und SystemObjectProperties werden gleicherma�en verarbeitet
			handleSystemObject(importObject);
		}
	}

	/* ##################### Aspect-Methoden ############################ */

	/**
	 * �berarbeitet das �bergebene System-Objekt. Falls keines vorhanden ist, wird aus einem Eintrag in der Versorgungsdatei - dargestellt durch ein
	 * Property-Objekt - ein Aspekt erstellt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Aspekts enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Aspekt nicht erstellt werden konnte.
	 */
	private void handleAspect(ImportObject importObject) throws ConfigurationChangeException {
		final AspectProperties property = (AspectProperties)importObject.getProperties();
		try {
			Aspect aspect = (Aspect)importObject.getSystemObject();
			if(aspect == null) {
				// Objekt gibt es noch nicht -> muss also neu erstellt werden
				final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ASPECT);
				aspect = (Aspect)importObject.getConfigurationArea().createConfigurationObject(type, property.getPid(), property.getName(), null);
				_debug.finer("Neuer Aspekt mit der Pid '" + property.getPid() + "' wurde angelegt.");
			}
			else {
				setSystemObjectKeeping(aspect);
			}
			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), aspect.getName())) {
				aspect.setName(property.getName());
			}
			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), aspect.getInfo())) {
				setInfo(aspect, property.getInfo());
			}
			importObject.setSystemObject(aspect);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der Aspekt " + property.toString() + " konnte nicht erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/* ##################### AttributeListDefinition-Methoden ############################ */

	/**
	 * Erstellt aus einem Eintrag in der Versorgungsdatei eine Attributliste, oder ver�ndert ein bestehendes System-Objekt, so dass es mit der Definition
	 * �bereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eine Attributliste enth�lt
	 *
	 * @throws ConfigurationChangeException Falls die Attributliste nicht importiert werden konnte.
	 */
	private void handleAttributeListDefinition(ImportObject importObject) throws ConfigurationChangeException {
		final AttributeListProperties property = (AttributeListProperties)importObject.getProperties();
		try {
			AttributeListDefinition atl = (AttributeListDefinition)importObject.getSystemObject();
			if(atl == null) {
				// Objekt gibt es noch nicht -> muss also neu erstellt werden
				final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ATTRIBUTE_LIST_DEFINITION);
				atl = (AttributeListDefinition)importObject.getConfigurationArea().createConfigurationObject(type, property.getPid(), property.getName(), null);
				_debug.finer("Neue AttributListDefinition mit der Pid '" + property.getPid() + "' wurde angelegt.");
			}
			else {
				setSystemObjectKeeping(atl);
			}
			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), atl.getName())) {
				atl.setName(property.getName());
			}
			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), atl.getInfo())) {
				setInfo(atl, property.getInfo());
			}
			importObject.setSystemObject(atl);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Die Attributliste " + property.toString() + " konnte nicht erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Diese Methode vervollst�ndigt eine Attributliste mit konfigurierenden Datens�tzen.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import einer Attributliste enth�lt
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void completeAttributeListDefinition(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final AttributeListDefinition atl = (AttributeListDefinition)importObject.getSystemObject();
		final AttributeListProperties property = (AttributeListProperties)importObject.getProperties();
		try {
			setAttributeObjectSet(configurationArea, atl, property.getAttributeAndAttributeList());
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Die Attributliste " + property.toString() + " konnte nicht vollst�ndig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Speichert die Attribute eines Konfigurationsobjekts in einer Menge names "Attribute" an diesem Konfigurationsobjekt ab.
	 *
	 * @param configurationArea       Konfigurationsbereich des Konfigurationsobjekts
	 * @param object                  das Konfigurationsobjekt
	 * @param configurationAttributes die zu speichernden Attribute
	 *
	 * @throws ConfigurationChangeException Falls die Menge der Attribute nicht am Konfigurationsobjekt gespeichert werden konnte.
	 */
	private void setAttributeObjectSet(ConfigurationArea configurationArea, ConfigurationObject object, AttributeProperties[] configurationAttributes)
			throws ConfigurationChangeException {
		// Menge "Attribute" erzeugen, falls es sie noch nicht gibt
		NonMutableSet objectSet = (NonMutableSet)object.getObjectSet("Attribute");
		if(objectSet == null) {
			// Menge erzeugen
			final ConfigurationObjectType setType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.ATTRIBUTES);
			objectSet = (NonMutableSet)configurationArea.createConfigurationObject(setType, "", "Attribute", null);
			// Menge "Attribute" am KonfigurationsObjekt speichern
			object.addSet(objectSet);
			_debug.finer("Die Menge der Attribute wurde erzeugt.");
		}
		else {
			setSystemObjectKeeping(objectSet);
		}

		// pr�fen, ob zwei Attribute mit gleichem Namen versehen wurden
		final Set<String> attributeNames = new HashSet<String>();

		// erh�lt alle neu erzeugten Attribute dieser Attributgruppe
		final List<Attribute> newAttributes = new ArrayList<Attribute>();

		ConfigurationObjectType attType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ATTRIBUTE);
		short position = 1;
		for(AttributeProperties configurationAttribute : configurationAttributes) {
			SystemObjectInfo info;
			String name;
			int maxCount;
			boolean isCountVariable;
			AttributeType attributeType;
			String attributeTypePid = "";
			String aDefault = null;

			if(configurationAttribute instanceof ListAttributeProperties) {
				ListAttributeProperties attributeList = (ListAttributeProperties)configurationAttribute;
				info = attributeList.getInfo();
				name = attributeList.getName();
				maxCount = attributeList.getMaxCount();
				isCountVariable = (attributeList.getTargetValue() == TargetValue.VARIABLE);
				attributeTypePid = attributeList.getAttributeTypePid();
				attributeType = (AttributeType)getObject(attributeTypePid);
			}
			else {
				PlainAttributeProperties attribute = (PlainAttributeProperties)configurationAttribute;
				info = attribute.getInfo();
				name = attribute.getName();
				maxCount = attribute.getMaxCount();
				isCountVariable = (attribute.getTargetValue() == TargetValue.VARIABLE);
				attributeTypePid = attribute.getAttributeTypePid();
				attributeType = (AttributeType)getObject(attributeTypePid);
				aDefault = attribute.getDefault();
			}

			// Name darf nicht doppelt vergeben werden
			final boolean isNewElement = attributeNames.add(name);
			if(!isNewElement) {
				throw new IllegalStateException("Doppelten Attributnamen '" + name + "' vergeben.");
			}

			if(attributeType == null) {
				throwNoObjectException(attributeTypePid);
			}

			// gibt es das Attribut bereits?
			Attribute att = null;
			for(SystemObject systemObject : objectSet.getElementsInModifiableVersion()) {
				if(systemObject.getName().equals(name)) {
					att = (Attribute)systemObject;
					break;
				}
			}
			// pr�fen, ob das Attribut ge�ndert werden darf, wenn es ge�ndert werden muss, oder ob ein neues angelegt wird!
			if(att != null) {
				if(_objectDiffs.isAttributeProcessable(configurationAttribute, att, position)) {
					// Attribut kann weiterverarbeitet werden
					// Info �berpr�fen
					if(_objectDiffs.isInfoDifferent(info, att.getInfo())) {
						setInfo(att, info);
					}
					// Attribut-Eigenschaften �berpr�fen
					if((maxCount != att.getMaxCount() || isCountVariable != att.isCountVariable() || attributeType != att.getAttributeType())
					   || position != att.getPosition()) {
						setAttributeProperties(att, position, maxCount, isCountVariable, attributeType);
					}
					// Default-Wert �berpr�fen
					if(_objectDiffs.isDefaultDifferent(att, aDefault)) {
						setDefault(att, aDefault);
					}
				}
				else {
					// Das Attribut kann nicht weiterverarbeitet werden - muss je nach Referenzierungsart auf veraltet gesetzt oder gel�scht werden.
					if(objectSet.getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION && !objectSet.getObjectSetType().isMutable()) {
						// ein bestehendes Objekt wird auf veraltet gesetzt
						objectSet.remove(att);
//						att.invalidate();
					}
					else {
						objectSet.remove(att);
					}
					att = null;	// Neues Attribut muss angelegt werden
				}
			}
			if(att == null) {
				// neues Attribut erzeugen und speichern
				att = (Attribute)configurationArea.createConfigurationObject(attType, "", name, null);
				setInfo(att, info);
				setAttributeProperties(att, position, maxCount, isCountVariable, attributeType);
				setDefault(att, aDefault);
				objectSet.add(att);	// neues Attribut wird hinzugef�gt
				_debug.finer("Neues Attribut '" + name + "' wird erstellt.");
			}
			else {
				setSystemObjectKeeping(att);
			}
			// dies ist ein importiertes Attribut
			newAttributes.add(att);
			position++;
		}

		// nur die importierten Attribute d�rfen in der Menge vorhanden sein
		final List<SystemObject> elementsInVersion = objectSet.getElementsInModifiableVersion();
		for(SystemObject systemObject : elementsInVersion) {
			Attribute att = (Attribute)systemObject;
			if(!newAttributes.contains(att)) {
				objectSet.remove(att);
			}
		}
	}

	/**
	 * Speichert Informationen (z.B. Position) zu Attributen/-listen innerhalb einer Menge ab.
	 *
	 * @param attribute       ein Attribut der Menge "Attribute"
	 * @param position        Position des Attributs in der Menge
	 * @param maxCount        maximale Anzahl von Elemente des Attributs
	 * @param isCountVariable gibt an, ob die Anzahl fest oder variabel ist
	 * @param attributeType   der Attribut-Typ des Attributs
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setAttributeProperties(Attribute attribute, short position, int maxCount, boolean isCountVariable, AttributeType attributeType)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.attributEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("position").set(position);
		data.getUnscaledValue("anzahl").set(maxCount);
		data.getUnscaledValue("anzahlVariabel").set((isCountVariable ? 1 : 0));
		data.getReferenceValue("attributTyp").setSystemObject(attributeType);
		attribute.setConfigurationData(atg, data);
	}

	/* ##################### AttributeType-Methoden ############################ */

	/**
	 * Erstellt aus einem Property-Objekt (welches einem Eintrag in der Versorgungsdatei entspricht) einen AttributTypen, oder ver�ndert ein bestehendes
	 * System-Objekt, so dass es mit der Definition �bereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Attribut-Typen enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Attribut-Typ nicht importiert werden konnte.
	 */
	private void handleAttributeType(ImportObject importObject) throws ConfigurationChangeException {
		final AttributeTypeProperties property = (AttributeTypeProperties)importObject.getProperties();
		try {
			final ConfigurationArea configurationArea = importObject.getConfigurationArea();
			final ConfigurationAttributeType configurationAttributeType = property.getAttributeType();
			final AttributeType existingAttributeType = (AttributeType)importObject.getSystemObject();
			if(configurationAttributeType instanceof ConfigurationString) {
				final ConfigurationString type = (ConfigurationString)configurationAttributeType;
				StringAttributeType attributeType = null;
				if(existingAttributeType instanceof StringAttributeType) {
					attributeType = (StringAttributeType)existingAttributeType;
					setSystemObjectKeeping(attributeType);
				}
				else {
					// ein neuer Attribut-Typ muss angelegt werden
					final ConfigurationObjectType objectType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.STRING_ATTRIBUTE_TYPE);
					attributeType = (StringAttributeType)configurationArea.createConfigurationObject(objectType, property.getPid(), property.getName(), null);
					_debug.finer("Ein neuer String-Attribut-Typ mit der Pid '" + property.getPid() + "'wurde angelegt.");
				}
				// Namen �berpr�fen
				if(_objectDiffs.isNameDifferent(property.getName(), attributeType.getName())) {
					attributeType.setName(property.getName());
				}
				// Info �berpr�fen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Attribut-Typ-Eigenschaften �berpr�fen
//				if(attributeType.getConfigurationData(_dataModel.getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften")) == null
				if(getConfigurationData(attributeType, _dataModel.getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften")) == null
				   || type.getLength() != attributeType.getMaxLength() || !type.getStringEncoding().equals(attributeType.getEncodingName())) {
					setStringAttributeTypeProperties(attributeType, type);
				}
				importObject.setSystemObject(attributeType);
			}
			else if(configurationAttributeType instanceof ConfigurationIntegerDef) {
				final ConfigurationIntegerDef def = (ConfigurationIntegerDef)configurationAttributeType;
				IntegerAttributeType attributeType = null;
				if(existingAttributeType instanceof IntegerAttributeType) {
					attributeType = (IntegerAttributeType)existingAttributeType;
					setSystemObjectKeeping(attributeType);
				}
				else {
					// ein neuer Attribut-Typ wird angelegt
					final ConfigurationObjectType objectType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.INTEGER_ATTRIBUTE_TYPE);
					attributeType = (IntegerAttributeType)configurationArea.createConfigurationObject(objectType, property.getPid(), property.getName(), null);
					_debug.finer("Ein neuer Ganzzahl-Attribut-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
				}
				// Namen �berpr�fen
				if(_objectDiffs.isNameDifferent(property.getName(), attributeType.getName())) {
					attributeType.setName(property.getName());
				}
				// Info �berpr�fen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}

				// ValueRange ermittlen
				IntegerValueRange valueRange = null;
//				final Data attributeTypePropertiesData = attributeType.getConfigurationData(_dataModel.getAttributeGroup("atg.ganzzahlAttributTypEigenschaften"));
				final Data attributeTypePropertiesData = getConfigurationData(
						attributeType, _dataModel.getAttributeGroup("atg.ganzzahlAttributTypEigenschaften")
				);
				if(attributeTypePropertiesData != null) {
					valueRange = attributeType.getRange();
				}
				for(ConfigurationIntegerValueRange rangeAndStates : def.getValueRangeAndState()) {
					if(rangeAndStates instanceof ConfigurationValueRange) {
						final ConfigurationValueRange configurationValueRange = (ConfigurationValueRange)rangeAndStates;
						valueRange = handleIntegerValueRange(configurationArea, configurationValueRange, valueRange);
					}
				}

				// Eigenschaften �berpr�fen
				if(attributeTypePropertiesData == null || _objectDiffs.isIntegerAttributeTypePropertiesDifferent(def, attributeType)) {
					setIntegerAttributeTypeProperties(attributeType, valueRange, def.getBits());
				}

				// Vorhandene Zust�nde merken
				final List<IntegerValueState> stateList = new LinkedList<IntegerValueState>();
				NonMutableSet stateSet = attributeType.getNonMutableSet("zust�nde");
				if(stateSet != null) {
					for(SystemObject systemObject : stateSet.getElementsInModifiableVersion()) {
						final IntegerValueState valueState = (IntegerValueState)systemObject;
						stateList.add(valueState);
					}
					setSystemObjectKeeping(stateSet);
				}
				else {
					// Menge der Zust�nde neu anlegen
					final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.INTEGER_VALUE_STATES);
					stateSet = (NonMutableSet)configurationArea.createConfigurationObject(type, "", "zust�nde", null);
					attributeType.addSet(stateSet);
				}
				// Menge der Zust�nde �berpr�fen
//				ObjectSet stateSet = attributeType.getObjectSet("zust�nde");
//				if (stateSet == null) {
//					// Menge muss erzeugt und dem Attribut-Typen hinzugef�gt werden
//					final ConfigurationObjectType type = (ConfigurationObjectType) _dataModel.getType(Pid.SetType.INTEGER_VALUE_STATES);
//					stateSet = (ObjectSet) configurationArea.createConfigurationObject(type, "", "zust�nde", null);
//					attributeType.addSet(stateSet);
//				} else {
//					setSystemObjectKeeping(stateSet);
//				}

				// die einzelnen Zust�nde �berpr�fen und ggf. neu anlegen
				for(ConfigurationIntegerValueRange rangeAndStates : def.getValueRangeAndState()) {
					if(rangeAndStates instanceof ConfigurationState) {
						final ConfigurationState configurationState = (ConfigurationState)rangeAndStates;
						IntegerValueState integerValueState = null;
						for(IntegerValueState state : stateList) {
							if(state.getValue() == configurationState.getValue()) {
								integerValueState = state;
							}
						}
						if(integerValueState == null) {
							// Neuen State anlegen
							final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.INTEGER_VALUE_STATE);
							integerValueState = (IntegerValueState)configurationArea.createConfigurationObject(type, "", configurationState.getName(), null);
							stateSet.add(integerValueState);
						}
						else {
							setSystemObjectKeeping(integerValueState);
						}
						// Info �berpr�fen
						if(_objectDiffs.isInfoDifferent(configurationState.getInfo(), integerValueState.getInfo())) {
							setInfo(integerValueState, configurationState.getInfo());
						}
						// Eigenschaften �berp�fen
						if(getConfigurationData(integerValueState, _dataModel.getAttributeGroup("atg.werteZustandsEigenschaften")) == null) {
							setIntegerValueStateProperties(integerValueState, configurationState);
						}
						if(!configurationState.getName().equals(integerValueState.getName())) {
							// Statuswert umbenennen
							integerValueState.setName(configurationState.getName());
						}
					}
				}
				importObject.setSystemObject(attributeType);
			}
			else if(configurationAttributeType instanceof ConfigurationDoubleDef) {
				final ConfigurationDoubleDef def = (ConfigurationDoubleDef)configurationAttributeType;
				DoubleAttributeType attributeType = null;
				if(existingAttributeType instanceof DoubleAttributeType) {
					attributeType = (DoubleAttributeType)existingAttributeType;
					setSystemObjectKeeping(attributeType);
				}
				else {
					// Attribut-Typ wird neu angelegt
					final ConfigurationObjectType objectType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.DOUBLE_ATTRIBUTE_TYPE);
					attributeType = (DoubleAttributeType)configurationArea.createConfigurationObject(objectType, property.getPid(), property.getName(), null);
					_debug.finer("Ein neuer Flie�kommazahl-Attribut-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
				}
				// Info �berpr�fen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Eigenschaften �berpr�fen
				if(_objectDiffs.isDoubleAttributeTypeDifferent(def, attributeType)) {
					setDoubleAttributeTypeProperties(attributeType, def);
				}
				importObject.setSystemObject(attributeType);
			}
			else if(configurationAttributeType instanceof ConfigurationTimeStamp) {
				final ConfigurationTimeStamp stamp = (ConfigurationTimeStamp)configurationAttributeType;
				TimeAttributeType attributeType = null;
				if(existingAttributeType instanceof TimeAttributeType) {
					attributeType = (TimeAttributeType)existingAttributeType;
					setSystemObjectKeeping(attributeType);
				}
				else {
					// ein neuer Attribut-Typ muss angelegt werden
					final ConfigurationObjectType objectType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.TIME_ATTRIBUTE_TYPE);
					attributeType = (TimeAttributeType)configurationArea.createConfigurationObject(objectType, property.getPid(), property.getName(), null);
					_debug.finer("Ein neuer Zeitstempel-Attribut-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
				}
				// Info �berpr�fen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Eigenschaften �berpr�fen
				if(_objectDiffs.isTimeAttributeTypeDifferent(stamp, attributeType)) {
					setTimeAttributeTypeProperties(attributeType, stamp);
				}
				importObject.setSystemObject(attributeType);
			}
			else if(configurationAttributeType instanceof ConfigurationObjectReference) {
				ReferenceAttributeType attributeType = null;
				if(existingAttributeType instanceof ReferenceAttributeType) {
					attributeType = (ReferenceAttributeType)existingAttributeType;
					setSystemObjectKeeping(attributeType);
				}
				else {
					// Attribut-Typ wird neu angelegt
					final ConfigurationObjectType objectType = (ConfigurationObjectType)_dataModel.getType(Pid.Type.REFERENCE_ATTRIBUTE_TYPE);
					attributeType = (ReferenceAttributeType)configurationArea.createConfigurationObject(
							objectType, property.getPid(), property.getName(), null
					);
					_debug.finer("Ein neuer Referenz-Attribut-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
				}
				// Info �berpr�fen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				importObject.setSystemObject(attributeType);
			}
			else {
				throw new IllegalStateException("Dieser AttributTyp " + configurationAttributeType.getClass().getName() + " wird noch nicht unterst�tzt.");
			}
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der AttributTyp " + property.toString() + " konnte nicht erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Der Default-Wert wird am angegebenen System-Objekt gesetzt.
	 *
	 * @param systemObject System-Objekt
	 * @param aDefault     neuer Default-Wert des System-Objekts
	 *
	 * @throws ConfigurationChangeException Falls der Default nicht gesetzt werden konnte.
	 */
	private void setDefault(SystemObject systemObject, String aDefault) throws ConfigurationChangeException {
		if("_Undefiniert_".equals(aDefault)) throw new IllegalArgumentException("Der Default-Wert '_Undefiniert_' ist nicht erlaubt.");
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.defaultAttributwert");
		final Aspect asp = _dataModel.getAspect("asp.eigenschaften");
		// Attributgruppenverwendung ermitteln
		final AttributeGroupUsage atgUsage = getAttributeGroupUsage(atg, asp);
		if(aDefault == null) {
			// evtl. muss der Datensatz gel�scht werden
//			if(systemObject.getConfigurationData(atgUsage) != null) {
			if(getConfigurationData(systemObject, atgUsage) != null) {
				systemObject.setConfigurationData(atgUsage, null);
			}
		}
		else {
			// wenn der Wert nicht der gleiche ist, dann muss er geschrieben werden
			Data data = getConfigurationData(systemObject, atgUsage);
//			Data data = systemObject.getConfigurationData(atgUsage);
			if(data != null && data.getTextValue("wert").getText().equals(aDefault)) {
				// tue nichts - Werte sind gleich
			}
			else {
				// Default-Wert anlegen
				data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
				data.getTextValue("wert").setText(aDefault);
				systemObject.setConfigurationData(atgUsage, data);
			}
		}
		if(systemObject instanceof AttributeType) {
			((ConfigAttributeType)systemObject).loadDefaultAttributeValue();
		}
		else if(systemObject instanceof Attribute) {
			((ConfigAttribute)systemObject).loadDefaultAttributeValue();
		}
	}

	/**
	 * Vervollst�ndigt den Attribut-Typen um die fehlenden konfigurierenden Datens�tze. Momentan ist nur der Referenz-Attribut-Typ davon betroffen.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Attribut-Typen enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Attribut-Typ nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void completeAttributeType(ImportObject importObject) throws ConfigurationChangeException {
		final AttributeType attributeType = (AttributeType)importObject.getSystemObject();
		final AttributeTypeProperties property = (AttributeTypeProperties)importObject.getProperties();
		try {
			if(attributeType instanceof ReferenceAttributeType) {
				final ConfigurationObjectReference configurationObjectReference = (ConfigurationObjectReference)property.getAttributeType();
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)attributeType;
				if(_objectDiffs.isReferenceAttributeTypeDifferent(configurationObjectReference, referenceAttributeType)) {
					setReferenceAttributeTypeProperties(referenceAttributeType, configurationObjectReference);
				}
			}
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der AttributTyp " + property.toString() + " konnte nicht vollst�ndig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Setzt die Default-Werte an einem Attribut-Typ.
	 *
	 * @param attributeType ein Attribut-Typ
	 * @param property      Eigenschafts-Objekt
	 *
	 * @throws ConfigurationChangeException Falls die Default-Werte nicht am Attribut-Typ gespeichert werden konnten.
	 */
	private void completeDefaults(AttributeType attributeType, AttributeTypeProperties property) throws ConfigurationChangeException {
		// Default-Wert �berpr�fen
		if(_objectDiffs.isDefaultDifferent(attributeType, property.getDefault())) {
			setDefault(attributeType, property.getDefault());
		}
	}

	/**
	 * Speichert die Eigenschaften des Zeichenketten-AttributTyps als konfigurierenden Datensatz.
	 *
	 * @param attributeType       Zeichenketten-AttributTyp, an dem die Eigenschaften gespeichert werden sollen
	 * @param configurationString Objekt, welches die Eigenschaften des AttributTyps enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setStringAttributeTypeProperties(StringAttributeType attributeType, ConfigurationString configurationString)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("l�nge").set(configurationString.getLength());
		String encoding = configurationString.getStringEncoding();
		if("ISO-8859-1".equals(encoding)) {
			data.getUnscaledValue("kodierung").set(StringAttributeType.ISO_8859_1);
		}
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Erstellt den Wertebereich einer Ganzzahl, oder �berarbeitet ein bestehendes Bereichs-Objekt und gibt es zur�ck.
	 *
	 * @param configurationArea Konfigurationsbereich, dem der Wertebereich hinzugef�gt werden soll
	 * @param valueRange        umzuwandelnder Wertebereich
	 * @param integerValueRange bestehender Wertebereich
	 *
	 * @return SystemObject des Wertebereichs
	 *
	 * @throws ConfigurationChangeException Falls der Wertebereich nicht vollst�ndig erstellt werden konnte.
	 */
	private IntegerValueRange handleIntegerValueRange(
			ConfigurationArea configurationArea, ConfigurationValueRange valueRange, IntegerValueRange integerValueRange
	) throws ConfigurationChangeException {
		if(integerValueRange == null) {
			// Neues Objekt muss angelegt werden
			final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.INTEGER_VALUE_RANGE);
			integerValueRange = (IntegerValueRange)configurationArea.createConfigurationObject(type, "", "", null);
		}
		else {
			setSystemObjectKeeping(integerValueRange);
		}
		// Info �berpr�fen
		if(_objectDiffs.isInfoDifferent(valueRange.getInfo(), integerValueRange.getInfo())) {
			setInfo(integerValueRange, valueRange.getInfo());
		}
		// Eigenschaften �berpr�fen
		if(_objectDiffs.isIntegerAttributeTypeValueRangePropertiesDifferent(valueRange, integerValueRange)) {
			setIntegerValueRangeProperties(integerValueRange, valueRange);
		}
		return integerValueRange;
	}

	/**
	 * Speichert die Eigenschaften des Ganzzahl-Wertebereichs als konfigurierenden Datensatz.
	 *
	 * @param range      der Ganzzahl-Wertebereich
	 * @param valueRange Objekt, welches die zu speichernden Eigenschaften enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setIntegerValueRangeProperties(IntegerValueRange range, ConfigurationValueRange valueRange) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.werteBereichsEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getScaledValue("minimum").set(valueRange.getMinimum());
		data.getScaledValue("maximum").set(valueRange.getMaximum());
		data.getScaledValue("skalierung").set(valueRange.getScale());
		data.getTextValue("einheit").setText(valueRange.getUnit());
		range.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften des Ganzzahl-Werte-Zustands als konfigurierenden Datensatz.
	 *
	 * @param valueState der Ganzzahl-Werte-Zustand
	 * @param state      Objekt, welches die zu speichernden Eigenschaften enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setIntegerValueStateProperties(IntegerValueState valueState, ConfigurationState state) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.werteZustandsEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("wert").set(state.getValue());
		valueState.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften des Ganzzahl-AttributTyps als konfigurierenden Datensatz.
	 *
	 * @param attributeType Ganzzahl-AttributTyp, an dem die Eigenschaften gespeichert werden sollen
	 * @param valueRange    zu speichernder Werte-Bereich
	 * @param bitCount      Anzahl Bits des Bereichs
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setIntegerAttributeTypeProperties(IntegerAttributeType attributeType, IntegerValueRange valueRange, int bitCount)
			throws ConfigurationChangeException {
		// Anzahl Bytes ermitteln
		int byteCount;
		if(bitCount <= 8) {
			byteCount = 1;
		}
		else if(bitCount <= 16) {
			byteCount = 2;
		}
		else if(bitCount <= 32) {
			byteCount = 4;
		}
		else {
			byteCount = 8;
		}
		// Wurde der Ganzzahl-AttributTyp neu angelegt, dann muss auch der ValueRange neu angelegt werden.
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.ganzzahlAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getReferenceValue("bereich").setSystemObject(valueRange);
		data.getUnscaledValue("anzahlBytes").set(byteCount);
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften einer Flie�kommazahl als konfigurierenden Datensatz.
	 *
	 * @param attributeType Flie�kommazahl-AttributTyp
	 * @param def           Objekt, welches die zu speichernden Eigenschaften enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setDoubleAttributeTypeProperties(DoubleAttributeType attributeType, ConfigurationDoubleDef def) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.kommazahlAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		byte accuracy;
		switch(def.getAccuracy()) {
			case DOUBLE:
				accuracy = DoubleAttributeType.DOUBLE;
				break;
			case FLOAT:
				accuracy = DoubleAttributeType.FLOAT;
				break;
			default:
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + def.getAccuracy() + "' wird beim Import nicht unterst�tzt.");
		}
		data.getTextValue("einheit").setText(def.getUnit());
		data.getUnscaledValue("genauigkeit").set(accuracy);
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften eines Zeit-Attribut-Typs als konfigurierenden Datensatz.
	 *
	 * @param attributeType Zeit-Attribut-Typ
	 * @param stamp         Objekt, welches die zu speichernden Eigenschaften enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setTimeAttributeTypeProperties(TimeAttributeType attributeType, ConfigurationTimeStamp stamp) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.zeitstempelAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		byte accuracy;
		switch(stamp.getAccuracy()) {
			case MILLISECONDS:
				accuracy = TimeAttributeType.MILLISECONDS;
				break;
			case SECONDS:
				accuracy = TimeAttributeType.SECONDS;
				break;
			default:
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + stamp.getAccuracy() + "' wird beim Import nicht unterst�tzt");
		}
		data.getUnscaledValue("relativ").set((stamp.getRelative() ? 1 : 0));
		data.getUnscaledValue("genauigkeit").set(accuracy);
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften eines Referenz-Attribut-Typs als konfigurierenden Datensatz.
	 *
	 * @param attributeType Referenz-Attribut-Typ
	 * @param reference     Objekt, welches die zu speichernden Eigenschaften enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setReferenceAttributeTypeProperties(ReferenceAttributeType attributeType, ConfigurationObjectReference reference)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.objektReferenzAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		byte referenceType;
		switch(reference.getReferenceType()) {
			case ASSOCIATION:
				referenceType = 0;
				break;
			case AGGREGATION:
				referenceType = 1;
				break;
			case COMPOSITION:
				referenceType = 2;
				break;
			default:
				throw new IllegalStateException("Diese Referenzierungsart '" + reference.getReferenceType() + "' wird nicht vom Import unterst�tzt.");
		}
		final String referenceObjectTypePid = reference.getReferenceObjectType();
		if(referenceObjectTypePid.equals("")) {
			data.getReferenceValue("typ").setSystemObject(null);		// undefiniert
		}
		else {
			data.getReferenceValue("typ").setSystemObject(getObject(referenceObjectTypePid));
		}
		data.getUnscaledValue("undefiniertErlaubt").set((reference.getUndefined() == UndefinedReferenceOptions.ALLOWED) ? 1 : 0);
		data.getUnscaledValue("referenzierungsart").set(referenceType);
		attributeType.setConfigurationData(atg, data);
	}

	/* ##################### AttributeGroup-Methoden ############################ */

	/**
	 * Erstellt aus einem Property-Objekt (welches einem Eintrag in der Versorgungsdatei entspricht) eine Attributgruppe oder ver�ndert eine bestehende
	 * Attributgruppe so, dass es der Import-Definition entspricht und gibt diese zur�ck.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import einer Attributgruppe enth�lt
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht importiert werden konnte.
	 */
	private void handleAttributeGroup(ImportObject importObject) throws ConfigurationChangeException {
		final AttributeGroupProperties property = (AttributeGroupProperties)importObject.getProperties();
		try {
			AttributeGroup attributeGroup = (AttributeGroup)importObject.getSystemObject();
			if(attributeGroup == null) {
				// Attributgruppe gibt es noch nicht -> muss also erstellt werden
				final ConfigurationObjectType type;
				if(property instanceof TransactionProperties) {
					// Eine Transaktion erstellen
					type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.TRANSACTION);
					if(type == null) {
						throw new IllegalStateException("Das verwendete Datenmodell unterst�tzt keine Transaktionen");
					}
				}
				else{
					//  Eine normale Attributgruppe erstellen
					type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ATTRIBUTE_GROUP);
				}

				attributeGroup = (AttributeGroup)importObject.getConfigurationArea().createConfigurationObject(
						type, property.getPid(), property.getName(), null
				);
				_debug.finer("Neue Attributgruppe mit Pid '" + attributeGroup.getPid() + "' und Id '" + attributeGroup.getId() + "' wurde angelegt.");
			}
			else {
				setSystemObjectKeeping(attributeGroup);
			}
			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), attributeGroup.getName())) {
				attributeGroup.setName(property.getName());
			}
			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeGroup.getInfo())) {
				setInfo(attributeGroup, property.getInfo());
			}
			importObject.setSystemObject(attributeGroup);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Die Attributgruppe '" + property.getPid() + "' konnte nicht erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Vervollst�ndigt eine Attributgruppe um die fehlenden konfigurierenden Datens�tze.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import einer Attributgruppe enth�lt
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void completeAttributeGroup(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final AttributeGroup attributeGroup = (AttributeGroup)importObject.getSystemObject();
		final AttributeGroupProperties property = (AttributeGroupProperties)importObject.getProperties();
		try {
			// neue Menge mit den AttributgruppenVerwendungen
			setAttributeGroupUsagesObjectSet(configurationArea, attributeGroup, property);
			setAttributeObjectSet(configurationArea, attributeGroup, property.getAttributeAndAttributeList());
			if(property instanceof TransactionProperties) {
				// Es handelt sich um eine Transaktionsattributgruppe
				final TransactionProperties transactionProperties = (TransactionProperties)property;
				setTransactionProperties(attributeGroup, transactionProperties);
			}
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Die Attributgruppe '" + property.getPid() + "' konnte nicht vollst�ndig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Vervollst�ndigt eine Transaktionsattributgruppe
	 * @param transaction Transaktionsattributgruppe
	 * @param transactionProperties Eigenschaften
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void setTransactionProperties(
			final AttributeGroup transaction, final TransactionProperties transactionProperties)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.transaktionsEigenschaften");
		final Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		setDids(data.getArray("akzeptiert"), transactionProperties.getPossibleDids());
		setDids(data.getArray("ben�tigt"), transactionProperties.getRequiredDids());
		transaction.setConfigurationData(atg,data);
	}

	/**
	 * Setzt die Datenidentifikationen im Datenmodell
	 * @param array Daten-Array
	 * @param dids Liste mit Datenidentifikationen
	 */
	private void setDids(final Data.Array array, final List<TransactionProperties.DataIdentification> dids) {
		if(dids == null) return;
		array.setLength(dids.size());
		for(int i = 0; i < dids.size(); i++) {
			final TransactionProperties.DataIdentification did = dids.get(i);
			final Data item = array.getItem(i);
			item.getReferenceValue("ObjektTyp").setSystemObjectPid(did.getObjectType());
			item.getReferenceValue("Attributgruppe").setSystemObjectPid(did.getAttributeGroup());
			item.getReferenceValue("Aspekt").setSystemObjectPid(did.getAspect());
			item.getTextValue("NurTransaktionsObjekt").setText(did.isOnlyTransactionObject() ? "Ja" : "Nein");
		}
	}

	/**
	 * Erstellt anhand der Attributgruppe und der Eigenschaften eine Menge mit den Attributgruppenverwendungen, wenn es sie noch nicht gibt. Dabei wird
	 * ber�cksichtigt, ob die Attributgruppe parametrierend ist, oder nicht. Die neu erstellte Menge wird an der angegebenen Attributgruppe gespeichert.
	 *
	 * @param configurationArea der Konfigurationsbereich, an dem die Attributgruppe steht
	 * @param atg               die Attributgruppe, die um die Attributgruppenverwendungen erweitert werden soll
	 * @param property          die Eigenschaften der Attributgruppe
	 *
	 * @throws ConfigurationChangeException Falls die Attributgruppenverwendungen nicht an der Attributgruppe gespeichert werden konnten.
	 */
	private void setAttributeGroupUsagesObjectSet(ConfigurationArea configurationArea, AttributeGroup atg, AttributeGroupProperties property)
			throws ConfigurationChangeException {
		// Merken, welche Attributgruppenverwendungen es an der Attributgruppe gibt
		final Map<Aspect, AttributeGroupUsage> atgUsageMap = new HashMap<Aspect, AttributeGroupUsage>();

		NonMutableSet atgUsageSet = atg.getNonMutableSet("AttributgruppenVerwendungen");
		if(atgUsageSet != null) {
			for(SystemObject systemObject : atgUsageSet.getElementsInModifiableVersion()) {
				final AttributeGroupUsage usage = (AttributeGroupUsage)systemObject;
				atgUsageMap.put(usage.getAspect(), usage);
			}
			setSystemObjectKeeping(atgUsageSet);
		}
		else {
			// Die Menge der Attributgruppenverwendungen gibt es noch nicht, muss also neu angelegt werden
			final ConfigurationObjectType setType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.ATTRIBUTE_GROUP_USAGES);
			atgUsageSet = (NonMutableSet)configurationArea.createConfigurationObject(setType, "", "AttributgruppenVerwendungen", null);
			atg.addSet(atgUsageSet);
			_debug.finer("Eine Menge der Attributgruppenverwendungen an der Attributgruppe '" + atg.getPid() + "' wurde erstellt.");
		}

		// enth�lt alle ATGVs, die in der n�chsten Version aktiv sein sollen
		final Collection<AttributeGroupUsage> nextCurrentAtgUsageList = new ArrayList<AttributeGroupUsage>();

		// f�r jeden Aspekt wird eine Attributgruppenverwendung erstellt, wenn sie noch nicht vorhanden ist
		final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ATTRIBUTE_GROUP_USAGE);
		final String atgvStr = "atgv." + atg.getPid() + ".";	// erster Teil der Pid einer Attributgruppenverwendung

		// handelt es sich um eine parametrierbare Attributgruppe, dann d�rfen folgende drei Aspekte nicht fehlen:
		if(property.isParameter()) {
			// Aspekt "ParameterSoll" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterSoll");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// pr�fen, ob auch die ATGV korrekt ist.
					if(!attributeGroupUsage.isExplicitDefined() || !(attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
					                                                 || attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage
							.OnlineDataAsSourceReceiverOrSenderDrain)) {
						setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver, true);
					}
					setSystemObjectKeeping(attributeGroupUsage);
				}
				else {
					// Die Attributgruppenverwendung ist nicht vorhanden -> erstellen
					attributeGroupUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(type, atgvStr + asp.getPid(), "", null);
					setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver, true);
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzuf�gen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
			// Aspekt "ParameterVorgabe" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterVorgabe");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// pr�fen, ob auch die ATGV korrekt ist.
					if(!attributeGroupUsage.isExplicitDefined() || !(attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSenderDrain
					                                                 || attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage
							.OnlineDataAsSourceReceiverOrSenderDrain)) {
						setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSenderDrain, true);
					}
					setSystemObjectKeeping(attributeGroupUsage);
				}
				else {
					// Neue ATGV wird erstellt.
					attributeGroupUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(type, atgvStr + asp.getPid(), "", null);
					setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSenderDrain, true);
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzuf�gen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
			// Aspekt "ParameterIst" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterIst");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// pr�fen, ob auch die ATGV korrekt ist.
					if(!attributeGroupUsage.isExplicitDefined() || !(attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver
					                                                 || attributeGroupUsage.getUsage() == AttributeGroupUsage.Usage
							.OnlineDataAsSourceReceiverOrSenderDrain)) {
						setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver, true);
					}
					setSystemObjectKeeping(attributeGroupUsage);
				}
				else {
					// Neue ATGV wird erstellt.
					attributeGroupUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(type, atgvStr + asp.getPid(), "", null);
					setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.OnlineDataAsSourceReceiver, true);
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzuf�gen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
			// Aspekt "ParameterDefault" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterDefault");
				if(asp == null) {
					throw new IllegalStateException(
							"Der Aspekt 'asp.parameterDefault' ist nicht vorhanden. Die Konfigurationsdateien passen nicht zum Stand der Kernsoftware."
					);
				}
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// pr�fen, ob auch die ATGV korrekt ist.
					if(!attributeGroupUsage.isExplicitDefined()
					   || attributeGroupUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData) {
						setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData, true);
					}
					setSystemObjectKeeping(attributeGroupUsage);
				}
				else {
					// Neue ATGV wird erstellt.
					attributeGroupUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(type, atgvStr + asp.getPid(), "", null);
					setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData, true);
					atgUsageSet.add(attributeGroupUsage); // ATGV hinzuf�gen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
		}

		// alle explizit angegebene Aspekte werden abgearbeitet
		for(ConfigurationAspect configurationAspect : property.getConfigurationAspect()) {
			final String aspPid = configurationAspect.getPid();
			if(property.isParameter() && (aspPid.equals("asp.parameterSoll") || aspPid.equals("asp.parameterVorgabe") || aspPid.equals("asp.parameterIst")
			                              || aspPid.equals("asp.parameterDefault"))) {
				// diese Aspekte ignorieren, wenn es sich um eine Parameter-Atg handelt - die Aspekte werden durch die Methode isParameter() gepr�ft
				_debug.warning(
						"Aspekt " + aspPid
						+ " wurde explizit angegeben und wird ignoriert. Da es sich um eine Parameter-Attributgruppe handelt, wird er implizit gesetzt."
				);
			}
			else {
				final Aspect asp = (Aspect)getObject(aspPid);
				if(asp == null) throwNoObjectException(aspPid);
				// Gibt es die ATGV bereits?
//				AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(asp);
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// pr�fen, ob die ATGV korrekt ist.
					if(_objectDiffs.isInfoDifferent(configurationAspect.getInfo(), attributeGroupUsage.getInfo())) {
						setInfo(attributeGroupUsage, configurationAspect.getInfo());
					}
					if(!attributeGroupUsage.isExplicitDefined() || attributeGroupUsage.getUsage() != configurationAspect.getUsage()) {
						setAttributeGroupUsageProperties(attributeGroupUsage, atg, asp, configurationAspect.getUsage(), true);
					}
					nextCurrentAtgUsageList.add(attributeGroupUsage);
					setSystemObjectKeeping(attributeGroupUsage);
				}
				else {
					// Neue ATGV wird erstellt.
					final AttributeGroupUsage atgUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(
							type, atgvStr + configurationAspect.getPid(), "", null
					);
					// konfigurierende Datens�tze hinzuf�gen
					setInfo(atgUsage, configurationAspect.getInfo());
					setAttributeGroupUsageProperties(atgUsage, atg, asp, configurationAspect.getUsage(), true);
					nextCurrentAtgUsageList.add(atgUsage);
					atgUsageSet.add(atgUsage);
				}
			}
		}

		if(property.getConfiguring()) {
			// Attributgruppenverwendung asp.eigenschaften hinzuf�gen, falls sie noch nicht vorhanden ist und sie die einzige Verwendung ist und das Attribut
			// konfigurierend ist.
			if(atgUsageSet.getElementsInModifiableVersion().isEmpty()) {
				// ATGV asp.eigenschaften hinzuf�gen
				final Aspect asp = _dataModel.getAspect("asp.eigenschaften");
				// neue Attributgruppenverwendung erstellen
				final AttributeGroupUsage atgUsage = (AttributeGroupUsage)configurationArea.createConfigurationObject(
						type, atgvStr + "asp.eigenschaften", "", null
				);
				setAttributeGroupUsageProperties(atgUsage, atg, asp, AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData, true);
				nextCurrentAtgUsageList.add(atgUsage);
				atgUsageSet.add(atgUsage);
			}
			else if(atgUsageSet.getElementsInModifiableVersion().size() == 1) {
				final AttributeGroupUsage atgUsage = (AttributeGroupUsage)atgUsageSet.getElementsInModifiableVersion().get(0);
				if(atgUsage.getAspect().getPid().equals("asp.eigenschaften")) {
					nextCurrentAtgUsageList.add(atgUsage);
					setSystemObjectKeeping(atgUsage);
				}
			}
		}

		// alte nicht mehr verwendete ATGVs l�schen
		for(AttributeGroupUsage atgUsage : atgUsageMap.values()) {  // bisheriger Inhalt der Menge der ATGVs
			if(!nextCurrentAtgUsageList.contains(atgUsage)) {
				atgUsageSet.remove(atgUsage);
			}
		}
	}

	/**
	 * Speichert die Eigenschaften einer Attributgruppenverwendung an der angegebenen Verwendung ab.
	 *
	 * @param atgUsage          die Attributgruppenverwendung
	 * @param attributeGroup    die Attributgruppe der Verwendung
	 * @param aspect            der Aspekt
	 * @param usage             die Verwendung der Attributgruppenverwendung
	 * @param isExplicitDefined gibt an, ob die Verwendung explizit vorgegeben sein muss
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setAttributeGroupUsageProperties(
			AttributeGroupUsage atgUsage, AttributeGroup attributeGroup, Aspect aspect, AttributeGroupUsage.Usage usage, boolean isExplicitDefined
	) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.attributgruppenVerwendung");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getReferenceValue("Attributgruppe").setSystemObject(attributeGroup);
		data.getReferenceValue("Aspekt").setSystemObject(aspect);
		// beim Import werden nur solche Attributgruppenverwendungen erstellt, die explizit in der Versorgungsdatei angegeben wurden
		data.getUnscaledValue("VerwendungExplizitVorgegeben").set((isExplicitDefined ? 1 : 0));
		int usageNumber;
		switch(usage) {
			case RequiredConfigurationData:
				usageNumber = 1;
				break;
			case ChangeableRequiredConfigurationData:
				usageNumber = 2;
				break;
			case OptionalConfigurationData:
				usageNumber = 3;
				break;
			case ChangeableOptionalConfigurationData:
				usageNumber = 4;
				break;
			case OnlineDataAsSourceReceiver:
				usageNumber = 5;
				break;
			case OnlineDataAsSenderDrain:
				usageNumber = 6;
				break;
			case OnlineDataAsSourceReceiverOrSenderDrain:
				usageNumber = 7;
				break;
			default:
				throw new IllegalStateException("Diese AttributgruppenVerwendung '" + usage + "' wird beim Import noch nicht unterst�tzt.");
		}
		data.getUnscaledValue("DatensatzVerwendung").set(usageNumber);
		atgUsage.setConfigurationData(atg, data);
	}

	/* ##################### ObjectSetType-Methoden ############################ */

	/**
	 * Erstellt aus einem Eigenschafts-Objekt einen Mengen-Typ oder ver�ndert ein bestehenden Mengen-Typen so, dass er mit der Import-Definition �bereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Mengen-Typs enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Mengen-Typ nicht importiert werden konnte.
	 */
	private void handleObjectSetType(ImportObject importObject) throws ConfigurationChangeException {
		final ObjectSetTypeProperties property = (ObjectSetTypeProperties)importObject.getProperties();
		try {
			ObjectSetType objectSetType = (ObjectSetType)importObject.getSystemObject();
			if(objectSetType == null) {
				// Objekt gibt es noch nicht -> muss also neu erstellt werden
				final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.OBJECT_SET_TYPE);
				objectSetType = (ObjectSetType)importObject.getConfigurationArea().createConfigurationObject(type, property.getPid(), property.getName(), null);
				_debug.finer("Neuer Mengen-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
			}
			else {
				setSystemObjectKeeping(objectSetType);
			}
			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), objectSetType.getName())) {
				objectSetType.setName(property.getName());
			}
			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), objectSetType.getInfo())) {
				setInfo(objectSetType, property.getInfo());
			}
			// Mengen-Typ-Eigenschaften �berpr�fen
			if(_objectDiffs.isObjectSetTypePropertiesDifferent(property, objectSetType)) {
				setObjectSetTypeProperties(objectSetType, property);
			}
			importObject.setSystemObject(objectSetType);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der MengenTyp '" + property.getPid() + "' konnte nicht korrekt erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Vervollst�ndigt den Mengen-Typ um noch fehlende konfigurierenden Datens�tze.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Mengen-Typs enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Mengen-Typ nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void completeObjectSetType(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final ObjectSetType objectSetType = (ObjectSetType)importObject.getSystemObject();
		final ObjectSetTypeProperties property = (ObjectSetTypeProperties)importObject.getProperties();
		try {
			setObjectSetTypeObjectTypes(configurationArea, objectSetType, property.getElements());
			setObjectSetTypeSuperTypeSet(configurationArea, objectSetType);
			setObjectSetTypeEmptySets(configurationArea, objectSetType);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der MengenTyp '" + property.getPid() + "' konnte nicht vollst�ndig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Speichert die Eigenschaften eines Mengen-Typs als konfigurierenden Datensatz am Mengen-Typ ab.
	 *
	 * @param objectSetType der Mengen-Typ
	 * @param property      das Eigenschafts-Objekt, welches die Eigenschaften des Mengen-Typs enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setObjectSetTypeProperties(ObjectSetType objectSetType, ObjectSetTypeProperties property) throws ConfigurationChangeException {
		// �berpr�fung, ob ein dynamischer MengenTyp als Referenzierungsart 'Assoziation' erh�lt.
		if(property.isMutable() && (property.getReferenceType() != ReferenceType.ASSOCIATION)) {
			throw new IllegalArgumentException(
					"Eine dynamische Menge muss immer mit der Referenzierungsart 'Assoziation' versorgt werden. " + "\nDer MengenTyp "
					+ objectSetType.getNameOrPidOrId() + " wurde aber mit " + property.getReferenceType() + " versorgt."
			);
		}

		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.mengenTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("minimaleAnzahl").set(property.getMinimum());
		data.getUnscaledValue("maximaleAnzahl").set(property.getMaximum());
		data.getUnscaledValue("�nderbar").set((property.isMutable() ? 1 : 0));
		byte referenceType;
		switch(property.getReferenceType()) {
			case ASSOCIATION:
				referenceType = 0;
				break;
			case AGGREGATION:
				referenceType = 1;
				break;
			case COMPOSITION:
				referenceType = 2;
				break;
			default:
				// Assoziation
				referenceType = 0;
		}
		data.getUnscaledValue("referenzierungsart").set(referenceType);
		objectSetType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Menge der Objekt-Typen am Mengen-Typ ab.
	 *
	 * @param configurationArea Konfigurationsbereich des Mengen-Typs
	 * @param objectSetType     der Mengen-Typ
	 * @param elements          Elemente der Menge der Objekt-Typen
	 *
	 * @throws ConfigurationChangeException Falls die Menge nicht am Objekt gespeichert werden konnte.
	 */
	private void setObjectSetTypeObjectTypes(ConfigurationArea configurationArea, ObjectSetType objectSetType, String[] elements)
			throws ConfigurationChangeException {
		NonMutableSet nonMutableSet = objectSetType.getNonMutableSet("ObjektTypen");
		if(nonMutableSet == null) {
			// Menge neu erstellen
			final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_TYPES);
			nonMutableSet = (NonMutableSet)configurationArea.createConfigurationObject(type, "", "ObjektTypen", null);
			objectSetType.addSet(nonMutableSet);
			_debug.finer("Menge der ObjektTypen f�r den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' wurde erstellt.");
		}
		else {
			setSystemObjectKeeping(nonMutableSet);
		}

		// Elemente der Menge ermitteln
		final Set<SystemObjectType> objectTypes = new HashSet<SystemObjectType>();
		for(String element : elements) {
			// Element ist entweder im Datenmodell oder wurde just importiert
			final SystemObject object = getObject(element);
			if(object == null) throwNoObjectException(element);
			objectTypes.add((SystemObjectType)object);
		}

		// Menge �berpr�fen
		final List<SystemObject> elementsInVersion = nonMutableSet.getElementsInVersion(objectSetType.getConfigurationArea().getModifiableVersion());
		// Erst alle �berfl�ssigen entfernen.
		for(SystemObject systemObject : elementsInVersion) {
			SystemObjectType systemObjectType = (SystemObjectType)systemObject;
			if(!objectTypes.contains(systemObjectType)) {
				nonMutableSet.remove(systemObjectType);
			}
		}
		// Jetzt neue hinzuf�gen
		for(SystemObjectType objectType : objectTypes) {
			if(!elementsInVersion.contains(objectType)) {
				nonMutableSet.add(objectType);
			}
		}
	}

	/**
	 * Die Menge SuperTypen muss an einer Mengendefinition vorhanden sein. In der Menge wird ein einziger Typ gespeichert.
	 *
	 * @param configurationArea Konfigurationsbereich der Mengendefinition
	 * @param objectSetType     Mengendefinition
	 *
	 * @throws ConfigurationChangeException Falls die Menge der Mengendefinition nicht hinzugef�gt werden konnten.
	 */
	private void setObjectSetTypeSuperTypeSet(ConfigurationArea configurationArea, ObjectSetType objectSetType) throws ConfigurationChangeException {
		NonMutableSet superTypeSet = objectSetType.getNonMutableSet("SuperTypen");
		if(superTypeSet == null) {
			// Mengen erstellen und dem MengenTyp hinzuf�gen
			final ConfigurationObjectType superTypeType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_TYPES);
			superTypeSet = (NonMutableSet)configurationArea.createConfigurationObject(superTypeType, "", "SuperTypen", null);
			objectSetType.addSet(superTypeSet);
			_debug.finer("Menge der SuperTypen wurde f�r den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
		}
		else {
			setSystemObjectKeeping(superTypeSet);
		}

		// je nachdem, ob die Menge �nderbar ist oder nicht, muss der richtige SuperTyp ausgew�hlt werden
		// �nderbar = typ.dynamischeMenge || nicht �nderbar = typ.konfigurationsMenge
		final SystemObjectType superType = objectSetType.isMutable() ? _dataModel.getType(Pid.Type.MUTABLE_SET) : _dataModel.getType(Pid.Type.NON_MUTABLE_SET);
		final List<SystemObject> superTypeSetElements = superTypeSet.getElementsInVersion(objectSetType.getConfigurationArea().getModifiableVersion());
		if(!superTypeSetElements.contains(superType)) {
			// Supertyp eintragen, wenn noch nicht enthalten
			superTypeSet.add(superType);
		}

		// evtl. vorhandene andere Supertypen m�ssen entfernt werden.
		for(SystemObject object : superTypeSetElements) {
			if(!object.equals(superType)) superTypeSet.remove(object);
		}
	}

	/**
	 * Die Mengen 'Attributgruppen' und 'Mengen' m�ssen an einer Mengendefinition vorhanden sein, obwohl in der Versorgungsdatei keine Elemente vorhanden sind.
	 *
	 * @param configurationArea Konfigurationsbereich der Mengendefinition
	 * @param objectSetType     Mengendefinition
	 *
	 * @throws ConfigurationChangeException Falls die Mengen der Mengendefinition nicht hinzugef�gt werden konnten.
	 */
	private void setObjectSetTypeEmptySets(ConfigurationArea configurationArea, ObjectSetType objectSetType) throws ConfigurationChangeException {
		NonMutableSet atgSet = objectSetType.getNonMutableSet("Attributgruppen");
		if(atgSet == null) {
			// Menge erstellen
			final ConfigurationObjectType atgType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.ATTRIBUTEGROUPS);
			atgSet = (NonMutableSet)configurationArea.createConfigurationObject(atgType, "", "Attributgruppen", null);
			objectSetType.addSet(atgSet);
			_debug.finer("Menge der Attributgruppen wurde f�r den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
		}
		else {
			setSystemObjectKeeping(atgSet);
		}

		NonMutableSet setUsesSet = objectSetType.getNonMutableSet("Mengen");
		if(setUsesSet == null) {
			// Menge erstellen
			final ConfigurationObjectType setType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_SET_USES);
			setUsesSet = (NonMutableSet)configurationArea.createConfigurationObject(setType, "", "Mengen", null);
			objectSetType.addSet(setUsesSet);
			_debug.finer("Menge der Mengenverwendungen wurde f�r den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
		}
		else {
			setSystemObjectKeeping(setUsesSet);
		}
	}

	/* ##################### SystemObjectType-Methoden ############################ */

	/**
	 * Erstellt aus einem Eigenschafts-Objekt einen Objekt-Typen oder ver�ndert einen bestehenden Objekt-Typen so, dass er mit der Import-Definition
	 * �bereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Objekt-Typs enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Objekt-Typ nicht importiert werden konnte.
	 */
	private void handleSystemObjectType(ImportObject importObject) throws ConfigurationChangeException {
		final SystemObjectTypeProperties property = (SystemObjectTypeProperties)importObject.getProperties();
		try {
			// pr�fen, ob die Pid des Objekt-Typen bereits eingetragen wurde, wenn ja, dann wurde eine Schleife erkannt, d.h. eine fehlerhafte Versorgung in
			// der Versorgungsdatei
			if(_objectTypesToCreate.contains(property.getPid())) {
				final String errorMessage = "Es gibt einen Fehler in der Versorgungsdatei. Zwei Objekt-Typen referenzieren sich gegenseitig.";
				_debug.error(errorMessage);
				throw new IllegalStateException(errorMessage);
			}
			// Pid des Objekt-Typen wird eingetragen
			_objectTypesToCreate.add(property.getPid());

			// Liste der erweiterten Typen (SuperTypen)
			final List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();

			// speichert, ob es sich um einen dynamischen oder einen konfigurierenden Typen handelt
			boolean isDynamicType = false;

			// ist dies ein Basis-Typ?
			if(property.getExtendedPids().length == 0) {
				if(property.getConfiguring()) {
					// ein konfigurierender Basis-Typ (typ.typ)
					isDynamicType = false;
				}
				else {
					// ein dynamischer Basis-Typ (typ.dynamischerTyp)
					isDynamicType = true;
				}
			}
			else {
				// es ist kein BasisTyp -> herausbekommen, was f�r ein Typ es ist
				// Liste der erweiterten Typen wird erstellt
				superTypes.addAll(createSuperTypes(property.getExtendedPids()));
				isDynamicType = isDynamicType(superTypes);
			}

			// Typ des Typs bestimmen (typ.typ oder typ.dynamischerTyp)
			final SystemObjectType type;
			if(isDynamicType) {
				type = _dataModel.getType(Pid.Type.DYNAMIC_TYPE);
			}
			else {
				type = _dataModel.getType(Pid.Type.TYPE);
			}

			SystemObjectType systemObjectType = (SystemObjectType)importObject.getSystemObject();
			if(systemObjectType != null) {
				// falls der Typ des bestehenden Typs nicht mit dem neuen Typ der Properties �bereinstimmt, muss ein neuer Objekt-Typ erstellt werden
				if(type != systemObjectType.getType()) {
					_debug.finer(
							"Der Typ des Typs " + systemObjectType.getPid() + " hat sich ge�nder von " + systemObjectType.getType().getPid() + " zu "
							+ type.getPid()
					);

					// handelt es sich um ein in Bearbeitung erstellter Typ, dann muss es aus der EditingObjects-Map entfernt werden.
					final Collection<CheckedObject> objects = _editingObjects.get(importObject.getConfigurationArea());
					for(CheckedObject object : objects) {
						if(object.getSystemObject() == (SystemObject)systemObjectType) {
							objects.remove(object);
							break;
						}
					}

					// Typ muss jetzt schon auf ung�ltig gesetzt werden, damit ein neuer Typ mit gleicher Pid erstellt werden kann
					// in Bearbeitung erstellte Objekte werden direkt gel�scht.
					
					((ConfigSystemObject)systemObjectType).simpleInvalidation();
					systemObjectType = null;
					_dissolveReference = true;
				}
				else {
					setSystemObjectKeeping(systemObjectType);
				}
			}
			if(systemObjectType == null) {
				// Neuer Objekt-Typ wird angelegt.
				systemObjectType = (SystemObjectType)importObject.getConfigurationArea().createConfigurationObject(
						(ConfigurationObjectType)type, property.getPid(), property.getName(), null
				);
				_debug.finer("Ein Objekt-Typ mit der Pid '" + property.getPid() + "' wurde erstellt.");
			}

			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), systemObjectType.getName())) {
				systemObjectType.setName(property.getName());
			}
			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), systemObjectType.getInfo())) {
				setInfo(systemObjectType, property.getInfo());
			}
			// Eigenschaften �berpr�fen
//			if(systemObjectType.getConfigurationData(_dataModel.getAttributeGroup("atg.typEigenschaften")) == null
			if(getConfigurationData(systemObjectType, _dataModel.getAttributeGroup("atg.typEigenschaften")) == null
			   || _objectDiffs.isSystemObjectTypePropertiesDifferent(property, systemObjectType)) {
				setSystemObjectTypeProperties(systemObjectType, !isDynamicType, property.getObjectNamesPermanent());
			}

			// Die Menge der SuperTypen erstellen
			handleSystemObjectTypeSuperTypes(importObject.getConfigurationArea(), systemObjectType, superTypes);

			// bei dynamischen Objekt-Typen wird der Persistenz-Modus gespeichert
			if(isDynamicType) {
				DynamicObjectType.PersistenceMode persistenceMode;
				switch(property.getPersistenceMode()) {
					case TRANSIENT_OBJECTS:
						persistenceMode = DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS;
						break;
					case PERSISTENT_OBJECTS:
						persistenceMode = DynamicObjectType.PersistenceMode.PERSISTENT_OBJECTS;
						break;
					case PERSISTENT_AND_INVALID_ON_RESTART:
						persistenceMode = DynamicObjectType.PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART;
						break;
					default:
						persistenceMode = getSuperTypePersistenceMode(superTypes);
				}
				final DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObjectType;
				if(getConfigurationData(dynamicObjectType, _dataModel.getAttributeGroup("atg.dynamischerTypEigenschaften")) == null
				   || persistenceMode != dynamicObjectType.getPersistenceMode()) {
					setDynamicObjectTypePersistenceMode(dynamicObjectType, persistenceMode);
				}
			}

			// nach der Erstellung des Objekt-Typs wird die Pid wieder aus der Liste entfernt
			_objectTypesToCreate.remove(property.getPid());
			importObject.setSystemObject(systemObjectType);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der Objekt-Typ '" + property.getPid() + "' konnte nicht korrekt erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Vervollst�ndigt den Objekt-Typ um fehlende konfigurierende Datens�tze, die bei der Erstellung des Objekts noch nicht ber�cksichtigt wurden.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines Objekt-Typs enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der Objekt-Typ nicht vervollst�ndigt werden konnte (Mengen und Datens�tze).
	 */
	private void completeSystemObjectType(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final SystemObjectType systemObjectType = (SystemObjectType)importObject.getSystemObject();
		final SystemObjectTypeProperties property = (SystemObjectTypeProperties)importObject.getProperties();
		try {
			setSystemObjectTypeAttributeGroups(configurationArea, systemObjectType, property.getAtgAndSet());
			// Default-Parameter-Datens�tze hinzuf�gen
			if(_objectDiffs.isDefaultParameterDifferent(property.getDefaultParameters(), systemObjectType)) {
				try {
					setDefaultParameterDataset(property.getDefaultParameters(), systemObjectType);
				}
				catch(IOException ex) {
					throw new ConfigurationChangeException(ex);
				}
			}
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Der Objekt-Typ '" + property.getPid() + "' konnte nicht vollst�ndig erstellt werden. Grund: " + ex.getMessage();
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Speichert die Eigenschaften des Objekt-Typs als konfigurierenden Datensatz ab.
	 *
	 * @param objectType      der Objekt-Typ
	 * @param isConfigurating gibt an, ob der Objekt-Typ konfigurierend ist
	 * @param isNamePermanent gibt an, ob der Name des Objekt-Typs permanent, also nicht �nderbar, ist
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt-Typ gespeichert werden konnte.
	 */
	private void setSystemObjectTypeProperties(SystemObjectType objectType, boolean isConfigurating, boolean isNamePermanent)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.typEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("konfigurierend").set((isConfigurating ? 1 : 0));
		data.getUnscaledValue("namePermanent").set((isNamePermanent ? 1 : 0));
		objectType.setConfigurationData(atg, data);
	}

	/**
	 * Aus einer Liste von Pids von Super-Typen werden System-Objekte von Super-Typen erstellt.
	 *
	 * @param superTypePids Pids von Super-Typen
	 *
	 * @return Liste von System-Objekten von Super-Typen
	 */
	private List<SystemObjectType> createSuperTypes(String[] superTypePids) {
		final List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
		for(String pid : superTypePids) {
			superTypes.add(createSystemObjectType(pid));
		}
		return superTypes;
	}

	/**
	 * Diese Methode pr�ft, ob die Super-Typen alle dynamisch oder konfigurierend sind. Sind die Super-Typen unterschiedlich, so liegt ein Versorgungsfehler vor
	 * und eine Ausnahme wird geworfen.
	 *
	 * @param superTypes Liste der Super-Typen eines neuen Typs
	 *
	 * @return <code>true</code>, falls alle Super-Typen dynamisch sind, <br> <code>false</code>, falls alle Super-Typen konfigurierend sind
	 *
	 * @throws IllegalStateException Falls ein Typ sowohl einen dynamischen als auch einen konfigurierenden Typen erweitert.
	 */
	private boolean isDynamicType(List<SystemObjectType> superTypes) {
		boolean isConfigurating = false;
		if(superTypes.size() > 0) isConfigurating = superTypes.get(0).isConfigurating();
		for(SystemObjectType objectType : superTypes) {
			if(objectType.isConfigurating() != isConfigurating) {
				// Typ erweitert sowohl einen dynamischen Typ als auch einen konfigurierenden Typ
				throw new IllegalStateException("Typ erweitert sowohl einen dynamischen Typ, als auch einen konfigurierenden Typ.");
			}
		}
		return !isConfigurating;
	}

	/**
	 * Diese Methode ermittelt anhand der Super-Typen den gemeinsamen Persistenz-Modus. Dieser ist f�r alle Super-Typen gleich.
	 *
	 * @param superTypes die zu betrachtenden Super-Typen
	 *
	 * @return den Persistenz-Modus
	 *
	 * @throws IllegalStateException Falls die Persistenz-Modi der Super-Typen unterschiedlich sind.
	 */
	DynamicObjectType.PersistenceMode getSuperTypePersistenceMode(List<SystemObjectType> superTypes) {
		DynamicObjectType.PersistenceMode mode;
		if(superTypes.size() > 0) {
			mode = ((DynamicObjectType)superTypes.get(0)).getPersistenceMode();
			for(SystemObjectType objectType : superTypes) {
				DynamicObjectType type = (DynamicObjectType)objectType;
				if(type.getPersistenceMode() != mode) {
					throw new IllegalStateException("Der Persistenz-Modus der Super-Typen ist unterschiedlich.");
				}
			}
		}
		else {
			throw new IllegalStateException("Der Persistenz-Modus der Super-Typen ist unterschiedlich.");
		}
		return mode;
	}

	/**
	 * Erstellt eine Menge mit Super-Typen f�r einen Objekt-Typen oder ver�ndert eine bestehende Menge.
	 *
	 * @param configurationArea Konfigurationsbereich des Objekt-Typs
	 * @param systemObjectType  Objekt-Typ an dem die Menge der SuperTypen gespeichert werden soll.
	 * @param superTypes        die Super-Typen des Objekt-Typs
	 *
	 * @throws ConfigurationChangeException Falls die Menge nicht am Objekt-Typ gespeichert werden konnte.
	 */
	private void handleSystemObjectTypeSuperTypes(ConfigurationArea configurationArea, SystemObjectType systemObjectType, List<SystemObjectType> superTypes)
			throws ConfigurationChangeException {
		NonMutableSet superTypeSet = systemObjectType.getNonMutableSet("SuperTypen");
		if(superTypeSet == null) {
			// Menge neu erstellen
			final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_TYPES);
			superTypeSet = (NonMutableSet)configurationArea.createConfigurationObject(type, "", "SuperTypen", null);
			systemObjectType.addSet(superTypeSet);
			_debug.finer("Menge der SuperTypen am Objekt-Typ mit der Pid '" + systemObjectType.getPid() + "' wurde angelegt.");
		}
		else {
			setSystemObjectKeeping(superTypeSet);
		}

		final List<SystemObject> elementsInVersion = superTypeSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());
		// Erst alle �berfl�ssigen Elemente entfernen
		for(SystemObject systemObject : elementsInVersion) {
			SystemObjectType objectType = (SystemObjectType)systemObject;
			if(!superTypes.contains(objectType)) {
				superTypeSet.remove(objectType);
			}
		}
		// Elemente neu hinzuf�gen
		for(SystemObjectType objectType : superTypes) {
			if(!elementsInVersion.contains(objectType)) {
				superTypeSet.add(objectType);
			}
		}
	}

	/**
	 * Speichert den Persistenz-Modus als konfigurierenden Datensatz am dynamischen Objekt-Typ.
	 *
	 * @param dynamicObjectType dynamischer Objekt-Typ
	 * @param persistenceMode   der zu speichernde Persistenz-Modus
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht am Objekt-Typ gespeichert werden konnte.
	 */
	private void setDynamicObjectTypePersistenceMode(DynamicObjectType dynamicObjectType, DynamicObjectType.PersistenceMode persistenceMode)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.dynamischerTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		int mode;
		switch(persistenceMode) {
			case TRANSIENT_OBJECTS:
				mode = 1;
				break;
			case PERSISTENT_OBJECTS:
				mode = 2;
				break;
			case PERSISTENT_AND_INVALID_ON_RESTART:
				mode = 3;
				break;
			default:
				throw new IllegalStateException("Persistenzmodus hat einen ung�ltigen Wert: " + persistenceMode);
		}
		data.getUnscaledValue("persistenzModus").set(mode);
		dynamicObjectType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die direkten Attributgruppen und Mengenverwendungen als Mengen am Objekt-Typ ab.
	 *
	 * @param configurationArea der Konfigurationsbereich des Objekt-Typs
	 * @param systemObjectType  der Objekt-Typ
	 * @param atgAndSet         Array, welches die Attributgruppen und Mengenverwendungen enth�lt
	 *
	 * @throws ConfigurationChangeException Falls die Mengen nicht am Objekt-Typ gespeichert werden konnten.
	 */
	private void setSystemObjectTypeAttributeGroups(ConfigurationArea configurationArea, SystemObjectType systemObjectType, Object[] atgAndSet)
			throws ConfigurationChangeException {
		NonMutableSet atgSet = systemObjectType.getNonMutableSet("Attributgruppen");
		if(atgSet == null) {
			// Menge der Attributgruppen wird neu angelegt
			final ConfigurationObjectType atgType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.ATTRIBUTEGROUPS);
			atgSet = (NonMutableSet)configurationArea.createConfigurationObject(atgType, "", "Attributgruppen", null);
			systemObjectType.addSet(atgSet);
			_debug.finer("Menge der Attributgruppen f�r den Objekt-Typ mit der Pid '" + systemObjectType.getPid() + "' wurde erstellt.");
		}
		else {
			setSystemObjectKeeping(atgSet);
		}
		NonMutableSet objectSetUsesSet = systemObjectType.getNonMutableSet("Mengen");
		if(objectSetUsesSet == null && systemObjectType.isConfigurating()) {
			// Menge der Mengenverwendungen erzeugen, wenn es kein dynamischer Typ ist.
			final ConfigurationObjectType objectSetUseType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_SET_USES);
			objectSetUsesSet = (NonMutableSet)configurationArea.createConfigurationObject(objectSetUseType, "", "Mengen", null);
			systemObjectType.addSet(objectSetUsesSet);
			_debug.finer("Menge der Mengenverwendungen f�r den Objekt-Typ mit der Pid '" + systemObjectType.getPid() + "' wurde erstellt.");
		}
		else {
			setSystemObjectKeeping(objectSetUsesSet);
		}

		// Elemente der Mengenverwendungen
		List<SystemObject> elementsInObjectSetUsesSet = new LinkedList<SystemObject>();
		if(systemObjectType.isConfigurating()) {
			elementsInObjectSetUsesSet = objectSetUsesSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());
		}

		final List<AttributeGroup> directAttributeGroups = new ArrayList<AttributeGroup>();
		final List<ObjectSetUse> directObjectSetUses = new ArrayList<ObjectSetUse>();
		for(Object o : atgAndSet) {
			if(o instanceof String) {
				// hier handelt es sich um Attributgruppen
				final String pid = (String)o;
				final SystemObject object = getObject(pid);
				if(object == null) throwNoObjectException(pid);
				directAttributeGroups.add((AttributeGroup)object);
			}
			else if(o instanceof ConfigurationSet) {
				if(systemObjectType.isConfigurating()) {
					// hier handelt es sich um Mengen
					ConfigurationSet configurationSet = (ConfigurationSet)o;
					// neues Objekt erh�lt keine Pid und keinen Namen (wird nicht ben�tigt)
					ObjectSetUse objectSetUse = null;
					for(SystemObject systemObject : elementsInObjectSetUsesSet) {
						ObjectSetUse use = (ObjectSetUse)systemObject;
						if(use.getObjectSetName().equals(configurationSet.getObjectSetName())) {
							objectSetUse = use;
						}
					}
					if(objectSetUse == null) {
						// Neue Mengenverwendung wird angelegt
						final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.OBJECT_SET_USE);
						objectSetUse = (ObjectSetUse)configurationArea.createConfigurationObject(type, "", "", null);
					}
					else {
						setSystemObjectKeeping(objectSetUse);
					}
					if(_objectDiffs.isInfoDifferent(configurationSet.getInfo(), objectSetUse.getInfo())) {
						setInfo(objectSetUse, configurationSet.getInfo());
					}
					// Mengen-Typ der Elemente und ob die Menge erforderlich ist �berpr�fen
					final String setTypePid = configurationSet.getSetTypePid();
					final SystemObject objectSetType = getObject(setTypePid);
					if(objectSetType == null) throwNoObjectException(setTypePid);
//					if(objectSetUse.getConfigurationData(_dataModel.getAttributeGroup("atg.mengenVerwendungsEigenschaften")) == null
					if(getConfigurationData(objectSetUse, _dataModel.getAttributeGroup("atg.mengenVerwendungsEigenschaften")) == null
					   || configurationSet.getRequired() != objectSetUse.isRequired() || objectSetType != objectSetUse.getObjectSetType()) {
						setObjectSetUseProperties(objectSetUse, configurationSet);
					}
					directObjectSetUses.add(objectSetUse);
				}
				else {
					throw new ConfigurationChangeException("Ein dynamischer Typ darf keine Mengen besitzen.");
				}
			}
		}

		// Menge der Attributgruppen
		final List<SystemObject> elementsInAtgSet = atgSet.getElementsInVersion(systemObjectType.getConfigurationArea().getModifiableVersion());
		// �berfl�ssige Elemente entfernen
		for(SystemObject systemObject : elementsInAtgSet) {
			AttributeGroup attributeGroup = (AttributeGroup)systemObject;
			if(!directAttributeGroups.contains(attributeGroup)) {
				atgSet.remove(attributeGroup);
			}
		}
		// Elemente hinzuf�gen
		for(AttributeGroup attributeGroup : directAttributeGroups) {
			if(!elementsInAtgSet.contains(attributeGroup)) {
				atgSet.add(attributeGroup);
			}
		}

		if(systemObjectType.isConfigurating()) {
			// Menge der Mengenverwendungen
			// �berfl�ssige Elemente entfernen
			for(SystemObject systemObject : elementsInObjectSetUsesSet) {
				ObjectSetUse use = (ObjectSetUse)systemObject;
				if(!directObjectSetUses.contains(use)) {
					objectSetUsesSet.remove(use);
				}
			}
			// Elemente hinzuf�gen
			for(ObjectSetUse objectSetUse : directObjectSetUses) {
				if(!elementsInObjectSetUsesSet.contains(objectSetUse)) {
					objectSetUsesSet.add(objectSetUse);
				}
			}
		}
	}

	/**
	 * Speichert die Eigenschaften einer Mengenverwendung an einer Mengenverwendung als konfigurierenden Datensatz ab.
	 *
	 * @param objectSetUse     die Mengenverwendung
	 * @param configurationSet Objekt, welches die Eigenschaften der Mengenverwendung enth�lt
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht an der Mengenverwendung gespeichert werden konnte.
	 */
	private void setObjectSetUseProperties(ObjectSetUse objectSetUse, ConfigurationSet configurationSet) throws ConfigurationChangeException {
		final String setTypePid = configurationSet.getSetTypePid();
		final SystemObject objectSetType = getObject(setTypePid);
		if(objectSetType == null) throwNoObjectException(setTypePid);

		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.mengenVerwendungsEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getTextValue("mengenName").setText(configurationSet.getObjectSetName());
		data.getReferenceValue("mengenTyp").setSystemObject(objectSetType);
		data.getUnscaledValue("erforderlich").set((configurationSet.getRequired() ? 1 : 0));
		objectSetUse.setConfigurationData(atg, data);
	}

	/* ##################### SystemObject-Methoden ############################ */

	/**
	 * Erstellt aus einem Eigenschafts-Objekt ein System-Objekt oder ver�ndert ein bereits bestehendes System-Objekt.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines System-Objekts enth�lt
	 *
	 * @throws ConfigurationChangeException Falls das System-Objekt nicht erstellt werden konnte.
	 */
	private void handleSystemObject(ImportObject importObject) throws ConfigurationChangeException {
		final SystemObjectProperties property = importObject.getProperties();
		try {
			final SystemObjectType systemObjectType = (SystemObjectType)getObject(property.getType());
			if(systemObjectType == null) throwNoObjectException(property.getType());

			SystemObject systemObject = importObject.getSystemObject();
			if(systemObject != null) {
				// es gibt bereits ein Objekt - pr�fen, ob der Typ �bereinstimmt - wenn nicht, dann muss ein neues Objekt angelegt werden.
				if(systemObject.getType() != systemObjectType) {
					// handelt es sich um ein in Bearbeitung erstelltes Objekt, dann muss es aus der EditingObjects-Map entfernt werden.
					final Collection<CheckedObject> objects = _editingObjects.get(importObject.getConfigurationArea());
					for(CheckedObject object : objects) {
						if(object.getSystemObject() == systemObject) {
							objects.remove(object);
							break;
						}
					}

					// Objekt muss jetzt schon auf ung�ltig gesetzt werden, damit ein neues Objekt mit gleicher Pid erstellt werden kann
					// in Bearbeitung erstellte Objekte werden direkt gel�scht.
					
					((ConfigSystemObject)systemObject).simpleInvalidation();
					systemObject = null;
					_dissolveReference = true;
				}
				else {
					setSystemObjectKeeping(systemObject);
				}
			}
			if(systemObject == null) {
				// Objekt muss neu erstellt werden
				if(systemObjectType.isConfigurating()) {
					final ConfigurationObjectType configurationObjectType = (ConfigurationObjectType)systemObjectType;
					systemObject = importObject.getConfigurationArea().createConfigurationObject(
							configurationObjectType, property.getPid(), property.getName(), null
					);
					_debug.finer("Konfigurierendes Objekt mit Pid '" + property.getPid() + "' wurde erstellt.");
				}
				else {
					// falls Datens�tze am dynamischen Objekt gespeichert werden sollen, m�ssen diese schon hier ber�cksichtigt werden
					final ConfigurationConfigurationObject configurationObjectProperties = (ConfigurationConfigurationObject)property;
					final List<DataAndATGUsageInformation> dataAndATGUsageInformation = new LinkedList<DataAndATGUsageInformation>();
					for(ConfigurationObjectElements configurationObjectElements : configurationObjectProperties.getDatasetAndObjectSet()) {
						if(configurationObjectElements instanceof ConfigurationDataset) {
							ConfigurationDataset configurationDataset = (ConfigurationDataset)configurationObjectElements;
							// pr�fen, ob es die Atg und den Asp gibt. Es m�ssen aktuelle Objekte sein.
							final AttributeGroup atg = _dataModel.getAttributeGroup(configurationDataset.getPidATG());
							if(atg == null) throwNoObjectException(configurationDataset.getPidATG());
							final Aspect asp = _dataModel.getAspect(configurationDataset.getPidAspect());
							if(asp == null) throwNoObjectException(configurationDataset.getPidAspect());

							final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(asp);
							// Exception, falls es keine ATGV gibt
							if(atgUsage == null) {
								final StringBuilder errorMessage = new StringBuilder();
								errorMessage.append("Datensatz kann nicht am Objekt '").append(property.getPid());
								errorMessage.append("' gespeichert werden. Zu der Attributgruppe '").append(atg.getPid()).append("' und dem Aspekt '");
								errorMessage.append(asp.getPid()).append("' gibt es  keine Attributgruppenverwendung zur Speicherung des Datensatzes.");
								_debug.error(errorMessage.toString());
								throw new ConfigurationChangeException(errorMessage.toString());
							}
							// alle notwendigen und optional nicht �nderbaren Datens�tze werden hier gespeichert.
							if(atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData) {
//							if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
//							   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
								final Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
								fillData(data, configurationDataset.getDataAnddataListAndDataField());
								dataAndATGUsageInformation.add(new DataAndATGUsageInformation(atgUsage, data));
							}
						}
						else {
							throw new IllegalStateException(
									"Beim dynamischen Objekt '" + property.getPid() + "' wurde eine Menge angegeben. Dies ist nicht erlaubt."
							);
						}
					}

					// Der zu nutzende dynamische Typ muss ein aktueller Typ sein. Falls er gerade erst importiert wurde, kann er nicht verwendet werden, da
					// das Objekt sofort g�ltig w�rde, ohne einen aktuellen Objekt-Typen.
					final DynamicObjectType dynamicObjectType = (DynamicObjectType)_dataModel.getObject(property.getType());
					if(dynamicObjectType != null) {
						_debug.finer("Dynamisches Objekt mit pid '" + property.getPid() + "' wird erstellt. Datens�tze", dataAndATGUsageInformation);
						systemObject = importObject.getConfigurationArea().createDynamicObject(
								dynamicObjectType, property.getPid(), property.getName(), dataAndATGUsageInformation
						);
						_debug.finer("Dynamisches Objekt mit der Pid '" + property.getPid() + "' wurde erstellt.");
					}
					else {
						final String errorMessage = "Das dynamische Objekt mit der Pid '" + property.getPid() + "' konnte nicht erzeugt werden, "
						                            + "da der Typ des Objekts erst in der n�chsten Version des Konfigurationsbereichs aktiviert wird.";
						_debug.error(errorMessage);
						throw new IllegalStateException(errorMessage);
					}
				}
			}
			// Namen �berpr�fen
			if(_objectDiffs.isNameDifferent(property.getName(), systemObject.getName())) {
				systemObject.setName(property.getName());
			}

			// Info �berpr�fen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), systemObject.getInfo())) {
				setInfo(systemObject, property.getInfo());
			}
			importObject.setSystemObject(systemObject);
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Das System-Objekt '" + property.getPid() + "' konnte nicht korrekt erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * System-Objekte erhalten in dieser Methode ihre konfigurierenden Datens�tze und Mengen.
	 *
	 * @param importObject Objekt, welches die Daten f�r den Import eines System-Objekts enth�lt
	 *
	 * @throws ConfigurationChangeException Falls das System-Objekt nicht vervollst�ndigt werden konnte.
	 */
	private void completeSystemObject(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final SystemObject systemObject = importObject.getSystemObject();
		final SystemObjectType objectType = systemObject.getType();
		final ConfigurationConfigurationObject property = (ConfigurationConfigurationObject)importObject.getProperties();
		try {
			// Set in der alle verwendeten ATGV gespeichert werden, damit alle Datens�tze gel�scht werden k�nnen, die nicht vorhanden sein d�rfen.
			final Set<AttributeGroupUsage> usedAtgUsages = new HashSet<AttributeGroupUsage>();

			// Datens�tze und Mengen werden gespeichert
			for(ConfigurationObjectElements elements : property.getDatasetAndObjectSet()) {
				if(elements instanceof ConfigurationDataset) {
					ConfigurationDataset dataset = (ConfigurationDataset)elements;
					if(dataset.getPidATG().equals("atg.konfigurationsVerantwortlicherLaufendeNummer")) {
						throw new IllegalStateException(
								"Dieser Datensatz '" + dataset.getPidATG() + "' darf nicht importiert werden. Er wird nur intern verwendet."
						);
					}
					// Attributgruppe, Aspekt und AttributgruppenVerwendung ermitteln
					final AttributeGroup atg = (AttributeGroup)getObject(dataset.getPidATG());
					if(atg == null) throwNoObjectException(dataset.getPidATG());
					final Aspect asp = (Aspect)getObject(dataset.getPidAspect());
					if(asp == null) throwNoObjectException(dataset.getPidAspect());

					final AttributeGroupUsage atgUsage = getAttributeGroupUsage(atg, asp);
					// Exception, falls es keine ATGV gibt
					if(atgUsage == null) {
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Datensatz kann nicht am Objekt '").append(systemObject.getPidOrNameOrId());
						errorMessage.append("' gespeichert werden. Zu der Attributgruppe '").append(atg.getPid()).append("' und dem Aspekt '");
						errorMessage.append(asp.getPid()).append("' gibt es  keine Attributgruppenverwendung zur Speicherung des Datensatzes.");
						_debug.error(errorMessage.toString());
						throw new ConfigurationChangeException(errorMessage.toString());
					}

					// Merken, zu welchen ATGV Datens�tze existieren sollen
					usedAtgUsages.add(atgUsage);
					// bei dynamischen Objekten und notwendig und nicht �nderbaren DS nachfolgenden Part �berspringen
					if(!objectType.isConfigurating() && (atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData
					                                     || atgUsage.getUsage() == AttributeGroupUsage.Usage.OptionalConfigurationData)) {
						// tue nix
					}
					else {

						// Falls der Datensatz unterschiedlich ist, wird er neu abgespeichert
						boolean datasetDifferent = _objectDiffs.isDatasetDifferent(dataset, systemObject);
//						if(atg.getPid().equals("atg.benutzerParameter")) {
//							System.out.println("atgUsage.getValidSince() = " + atgUsage.getValidSince());
//							System.out.println("atgUsage.getNotValidSince() = " + atgUsage.getNotValidSince());
//							System.out.println("atg = " + atg);
//							System.out.println("asp = " + asp);
//							System.out.println("systemObject.getPid() = " + systemObject.getPid());
//							System.out.println("############### datasetDifferent = " + datasetDifferent);
//						}
						if(datasetDifferent) {
							Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
//						printData(data);
							fillData(data, dataset.getDataAnddataListAndDataField());
//						printData(data);
							// das Data wird am System-Objekt gespeichert
							systemObject.setConfigurationData(atgUsage, data);
						}
					}
				}
				else if(elements instanceof ConfigurationObjectSet) {
					ConfigurationObjectSet configurationObjectSet = (ConfigurationObjectSet)elements;

					// dynamische Objekte k�nnen keine Mengen haben
					if(!objectType.isConfigurating()) {
						// es ist ein dynamisches Objekt!
						final String errorMessage = "Dynamische Objekte k�nnen keine Mengen haben. Es sind aber welche angegeben.";
						_debug.error(errorMessage);
						throw new IllegalArgumentException(errorMessage);
					}

					final ConfigurationObject configObject = (ConfigurationObject)systemObject;
					ObjectSet objectSet = configObject.getObjectSet(configurationObjectSet.getName());
					if(_objectDiffs.isSystemObjectSetDifferent(configurationObjectSet, objectSet)) {
						ObjectSetType objectSetType = null;
						// Mengenverwendungen ermitteln
						final Collection<ObjectSetUse> objectSetUses = getObjectSetUses(objectType);
						for(ObjectSetUse objectSetUse : objectSetUses) {
							if(objectSetUse.getObjectSetName().equals(configurationObjectSet.getName())) {
								objectSetType = objectSetUse.getObjectSetType();
								break; // da gefunden
							}
						}
						if(objectSetType == null) {
							final StringBuilder errorMessage = new StringBuilder();
							errorMessage.append("Der Typ '").append(objectType.getPidOrNameOrId()).append("' dieses Objektes '");
							errorMessage.append(systemObject.getPidOrNameOrId()).append("' unterst�tzt die Menge '").append(configurationObjectSet.getName());
							errorMessage.append("' nicht.");
							_debug.error(errorMessage.toString());
							throw new IllegalArgumentException(errorMessage.toString());
						}

						final String importManagementPid = configurationObjectSet.getManagementPid();
						// Menge ist verschieden oder nicht vorhanden
						if(objectSet != null) {
							boolean versionedChange = false;
							if(objectSet instanceof MutableSet) {
								final AttributeGroup dynamicSetPropertiesAtg = _dataModel.getAttributeGroup("atg.dynamischeMenge");
								if(dynamicSetPropertiesAtg != null) {
									final Aspect asp = _dataModel.getAspect("asp.eigenschaften");
									final AttributeGroupUsage atgUsage = getAttributeGroupUsage(dynamicSetPropertiesAtg, asp);
									String managementPid = "";
									final Data data = objectSet.getConfigurationData(atgUsage);
									if(data != null) managementPid = data.getTextValue("verwaltung").getValueText();
									if(!managementPid.equals(importManagementPid)) {
										versionedChange = true;
									}
								}
							}


							// Typ �berpr�fen
							if(objectSet.getObjectSetType() != objectSetType) {
								versionedChange = true;
							}
							if(versionedChange) {
								configObject.removeSet(objectSet);
								objectSet = null;
							}
						}
						if(objectSet == null) {
							// neue Menge anlegen
							objectSet = (ObjectSet)configurationArea.createConfigurationObject(objectSetType, "", configurationObjectSet.getName(), null);
							// Menge dem Objekt hinzuf�gen
							configObject.addSet(objectSet);
							_debug.finer(
									"Menge " + configurationObjectSet.getName() + " dem SystemObjekt mit der Pid '" + systemObject.getPid() + "' hinzugef�gt."
							);
						}
						else {
							setSystemObjectKeeping(objectSet);
						}

						boolean ignoreElements = false;
						if(objectSet instanceof MutableSet) {
							final AttributeGroup dynamicSetPropertiesAtg = _dataModel.getAttributeGroup("atg.dynamischeMenge");
							if(dynamicSetPropertiesAtg == null) {
								if(!importManagementPid.equals("")) {
									_debug.warning(
											"Zugriff auf Verwaltungsinformationen von dynamischen Mengen nicht m�glich, da die eingesetzte Version des Bereichs"
											+ " kb.metaModellGlobal zu alt ist (mindestens Version 10 notwendig)."
									);
								}
							}
							else {
								final Aspect asp = _dataModel.getAspect("asp.eigenschaften");
								final AttributeGroupUsage atgUsage = getAttributeGroupUsage(dynamicSetPropertiesAtg, asp);
								String managementPid = "";
								final Data data = objectSet.getConfigurationData(atgUsage);
								if(data != null) managementPid = data.getTextValue("verwaltung").getValueText();
								if(!managementPid.equals(importManagementPid)) {
									Data newData = AttributeBaseValueDataFactory.createAdapter(
											dynamicSetPropertiesAtg, AttributeHelper.getAttributesValues(dynamicSetPropertiesAtg)
									);
									newData.getTextValue("verwaltung").setText(importManagementPid);
									objectSet.setConfigurationData(atgUsage, newData);
									if(!importManagementPid.equals("")) ignoreElements = true;
								}
							}
						}
						else {
							if(!importManagementPid.equals("")) {
								_debug.warning(
										"Angabe der verwaltenden Konfiguration wird bei der nicht dynamischen Menge " + objectSet.getName() + " am Objekt "
										+ systemObject.getPidOrNameOrId() + " im Bereich " + configurationArea.getPidOrNameOrId() + " ignoriert"
								);
							}
						}

						// pr�fen, ob die hinzuzuf�genden Elemente in der Menge erlaubt sind (wird beim hinzuf�gen der Elemente �berpr�ft)
						Set<SystemObject> setElements = new HashSet<SystemObject>();
						final String[] elementPids = configurationObjectSet.getElements();
						if(elementPids.length != 0) {
							if(ignoreElements) {
								_debug.warning(
										"Wegen der Angabe der verwaltenden Konfiguration werden die bei der dynamischen Menge " + objectSet.getName()
										+ " am Objekt " + systemObject.getPidOrNameOrId() + " im Bereich " + configurationArea.getPidOrNameOrId()
										+ " angegebenen Elemente ignoriert"
								);
							}
							else {
								for(String pid : elementPids) {
									final SystemObject object = getObject(pid);
									if(object == null) throwNoObjectException(pid);
									setElements.add(object);
								}
							}
						}
						final List<SystemObject> elementsInSet = objectSet.getElements();
						// �berfl�ssige Elemente entfernen
						for(SystemObject object : elementsInSet) {
							if(!setElements.contains(object)) {
								objectSet.remove(object);
							}
						}
						// Elemente hinzuf�gen
						for(SystemObject object : setElements) {
							if(!elementsInSet.contains(object)) {
								objectSet.add(object);
							}
						}
					}
					else {
						// Menge muss nicht ge�ndert werden -> aber es wird beibehalten
						setSystemObjectKeeping(objectSet);
					}
				}
			}

			// nicht mehr ben�tigte Datens�tze entweder gel�scht oder auf null gesetzt, je nachdem, ob das Objekt in Bearbeitung ist
			// oder bereits freigegeben/aktiviert wurde
			if(objectType.isConfigurating()) {
				final Collection<AttributeGroupUsage> usedAttributeGroupUsages = systemObject.getUsedAttributeGroupUsages();
				for(AttributeGroupUsage atgUsage : usedAttributeGroupUsages) {
					if(!usedAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
					                                          || atgUsage.getAttributeGroup().getPid().equals("atg.konfigurationsVerantwortlicherLaufendeNummer"))) {
						// die ATGV wurde nicht verwendet und entspricht nicht atg.info oder atg.konfigurationsVerantwortlicherLaufendeNummer -> Datensatz l�schen
						// bei einem in Bearbeitung befindlichen Objekt werden die Datens�tze richtig gel�scht.
						if(getEditingObject(systemObject.getConfigurationArea(), systemObject.getPid()) != null) {
							((ConfigSystemObject)systemObject).removeConfigurationData(atgUsage);
						}
						else {
							// bei einem zuvor freigegebenen Objekt werden die Datens�tze nur auf null gesetzt.
							((ConfigSystemObject)systemObject).createConfigurationData(atgUsage, null);
						}
					}
				}
			}
			else {
				// es handelt sich um ein dynamisches Objekt - notwendige DS werden hier nicht betrachtet (sie d�rfen nicht gel�scht werden)
				for(AttributeGroupUsage atgUsage : systemObject.getUsedAttributeGroupUsages()) {
//					System.out.println("############### Betrachte atgUsage = " + atgUsage);
//					System.out.println("atgUsage.getValidSince() = " + atgUsage.getValidSince());
//					System.out.println("atgUsage.getNotValidSince() = " + atgUsage.getNotValidSince());
					if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
					   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
						// nicht weiter beachten
//						System.out.println("-> bleibt, weil notwendig");
					}
					else {
						if(!usedAtgUsages.contains(atgUsage) && !(atgUsage.getAttributeGroup().getPid().equals("atg.info")
						                                          || atgUsage.getAttributeGroup().getPid().equals(
								"atg.konfigurationsVerantwortlicherLaufendeNummer"
						))) {
							// die ATGV wurde nicht verwendet -> muss also gel�scht werden
							((ConfigSystemObject)systemObject).createConfigurationData(atgUsage, null);
//							System.out.println("-> wird auf null gesetzt");
						}
//						else {
//							System.out.println("-> bleibt");
//						}
					}
				}
			}

			if(objectType.isConfigurating()) {
				// Default-Parameter-Datens�tze hinzuf�gen
				if(_objectDiffs.isDefaultParameterDifferent(property.getDefaultParameters(), systemObject)) {
					setDefaultParameterDataset(property.getDefaultParameters(), systemObject);
				}
			}
		}
		catch(Exception ex) {
			final String errorMessage = "Das System-Objekt mit der Pid '" + property.getPid() + "' konnte nicht vollst�ndig erstellt werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	private void setDefaultParameterDataset(final ConfigurationDefaultParameter[] defaultParameters, final SystemObject systemObject)
			throws IOException, ConfigurationChangeException {
		final AttributeGroup attributeGroup = (AttributeGroup)getObject("atg.defaultParameterdatens�tze");
		final Data data;
		if(defaultParameters.length > 0) {
			// den Datensatz komplett neu schreiben
			data = AttributeBaseValueDataFactory.createAdapter(attributeGroup, AttributeHelper.getAttributesValues(attributeGroup));
			final Data.Array array = data.getArray("Default-Parameterdatensatz");
			array.setLength(defaultParameters.length);

			// Elemente f�llen
			for(int i = 0; i < array.getLength(); i++) {
				final Data item = array.getItem(i);
				final ConfigurationDefaultParameter defaultParameter = defaultParameters[i];
				String pidType = defaultParameter.getPidType();
				if("".equals(pidType)) {
					if(systemObject instanceof SystemObjectType) {
						SystemObjectType systemObjectType = (SystemObjectType)systemObject;
						pidType = systemObjectType.getPid();
					}
					else {
						pidType = systemObject.getType().getPid();
					}
				}
				final SystemObject typeObject = getObject(pidType);
				if(!(typeObject instanceof SystemObjectType)) {
					throw new ConfigurationChangeException(
							"Default-Parameter-Datensatz am '" + systemObject.getPidOrNameOrId() + "' konnte nicht importiert werden, weil ein ung�ltiger Typ '"
							+ pidType + "' angegeben wurde."
					);
				}
				final SystemObjectType referencedType = (SystemObjectType)typeObject;
				item.getReferenceValue("typ").setSystemObject(referencedType);

				final SystemObject atgObject = getObject(defaultParameter.getPidAtg());
				if(!(atgObject instanceof AttributeGroup)) {
					throw new ConfigurationChangeException(
							"Default-Parameter-Datensatz am '" + systemObject.getPidOrNameOrId()
							+ "' konnte nicht importiert werden, weil eine ung�ltige Attributgruppe '" + defaultParameter.getPidAtg() + "' angegeben wurde."
					);
				}
				final AttributeGroup atg = (AttributeGroup)atgObject;
				item.getReferenceValue("attributgruppe").setSystemObject(atg);

				// Datensatz mit der angegebenen ATG erstellen
				final Data defaultData = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
				defaultData.setToDefault();
//System.out.println("atg.getPid() = " + atg.getPid());
//System.out.println("atg.isValid() = " + atg.isValid());
//System.out.println("defaultData = " + defaultData);
				fillData(defaultData, defaultParameter.getDataAnddataListAndDataField());
				if(!defaultData.isDefined()) {
					throw new IllegalStateException(
							"Default-Parameter-Datensatz mit der ATG '" + atg.getPid() + "' am Konfigurationsobjekt '" + systemObject.getPidOrNameOrId()
							+ "' ist nicht vollst�ndig definiert."
					);
				}
				// aus dem Data ein byte-Array machen
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final Serializer serializer;
				try {
					serializer = SerializingFactory.createSerializer(3, out);
				}
				catch(NoSuchVersionException e) {
					throw new IOException("Serialisierer konnte nicht in der gew�nschten Version erstellt werden");
				}
				serializer.writeData(defaultData);
				final byte[] bytes = out.toByteArray();

				item.getUnscaledValue("serialisierer").set(serializer.getVersion());
				final Data.Array datasetArray = item.getArray("datensatz");
				datasetArray.setLength(bytes.length);
				for(int j = 0; j < bytes.length; j++) {
					datasetArray.getScaledValue(j).set(bytes[j]);
				}
			}
		}
		else {
			// Keine Default-Parameterdatens�tze in der Versorgungsdatei vorhanden f�hrt zum L�schen des Datensatzes
			data = null;
		}
		// Datensatz speichern
		systemObject.setConfigurationData(attributeGroup, data);
	}

	/**
	 * Diese Methode ermittelt anhand eines Objekt-Typen seine s�mtlichen Mengenverwendungen.
	 *
	 * @param systemObjectType der Objekt-Typ
	 *
	 * @return Alle Mengenverwendungen dieses Objekt-Typs.
	 */
	Collection<ObjectSetUse> getObjectSetUses(final SystemObjectType systemObjectType) {
		final Collection<ObjectSetUse> objectSetUses = new HashSet<ObjectSetUse>();
		if(_allImportedConfigurationAreas.contains(systemObjectType.getConfigurationArea())) {
			// Typ wird gerade importiert -> die in Bearbeitung befindlichen Mengenverwendungen betrachten
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Mengen").getElementsInModifiableVersion();
			for(SystemObject systemObject : setUses) {
				final ObjectSetUse setUse = (ObjectSetUse)systemObject;
				objectSetUses.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElementsInModifiableVersion();
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				objectSetUses.addAll(getObjectSetUses(objectType));
			}
		}
		else {
			// Typ ist in einem nicht importierten Bereich -> die aktiven bzw. freigegebenen Mengenverwendungen betrachten
//			objectSetUses.addAll(systemObjectType.getObjectSetUses());
			// In welcher Version soll der Konfigurationsbereich des Objekt-Typs betrachtet werden?
			short version = _usingVersionOfConfigurationArea.get(systemObjectType.getConfigurationArea());
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Mengen").getElementsInVersion(version);
			for(SystemObject systemObject : setUses) {
				final ObjectSetUse setUse = (ObjectSetUse)systemObject;
				objectSetUses.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElementsInVersion(version);
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				objectSetUses.addAll(getObjectSetUses(objectType));
			}
		}
		return Collections.unmodifiableCollection(objectSetUses);
	}

	/**
	 * Gibt alle Konfigurationsbereiche zur�ck, die gerade importiert werden.
	 *
	 * @return Alle Konfigurationsbereiche, die gerade importiert werden.
	 */
	Set<ConfigurationArea> getAllImportedConfigurationAreas() {
		return _allImportedConfigurationAreas;
	}

	/**
	 * Ein neues Data-Objekt wird mit den Elementen aus der Definition gef�llt.
	 *
	 * @param data     ein neues Data-Objekt
	 * @param elements Elemente der Definition eines System-Objekts
	 */
	void fillData(Data data, DatasetElement[] elements) {
		for(int i = 0; i < elements.length; i++) {
			DatasetElement datasetElement = elements[i];

			if(datasetElement instanceof ConfigurationData) {
				ConfigurationData configurationData = (ConfigurationData)datasetElement;
				try {
					Data item;
					if(configurationData.getName().equals("-")) {
						item = data.getItem(String.valueOf(i));
					}
					else {
						item = data.getItem(configurationData.getName());
					}
					String value = configurationData.getValue();
					if(item.getAttributeType() instanceof ReferenceAttributeType) {
						value = value.trim();
						final String pid;
						if(value.equals("0") || value.equals("null") || value.equals("undefiniert")) {
							pid = "";
						}
						else {
							pid = value;
						}
						try {
							item.asReferenceValue().setSystemObjectPid(pid, this);
						}
						catch(RuntimeException e) {
							throw new IllegalStateException("Das im Attribut " + item.getName() + " referenzierte Objekt mit der Pid '" + pid + "' ist weder ein aktuelles noch ein gerade importiertes Objekt.", e);
						}
					}
					else {
						item.asTextValue().setText(value);
					}
				}
				catch(IllegalArgumentException ex) {
					String errorMessage = "Ein Fehler ist beim Datensatz " + configurationData.getName() + " aufgetreten";
//					_debug.error(errorMessage, ex.getMessage());
					throw new IllegalArgumentException(errorMessage, ex);
				}
			}
			else if(datasetElement instanceof ConfigurationDataList) {
				ConfigurationDataList dataList = (ConfigurationDataList)datasetElement;
				Data item;
				if(dataList.getName().equals("-")) {
					item = data.getItem(String.valueOf(i));
				}
				else {
					item = data.getItem(dataList.getName());
				}
				fillData(item, dataList.getDataAndDataListAndDataField());
			}
			else if(datasetElement instanceof ConfigurationDataField) {
				ConfigurationDataField dataField = (ConfigurationDataField)datasetElement;
				// Gr��e des Arrays beachten
				Data item = data.getItem(dataField.getName());
				item.asArray().setLength(dataField.getDataAndDataList().length);
				fillData(item, dataField.getDataAndDataList());
			}
		}
	}

	/**
	 * Hilfsmethode, die einen Datensatz ausgibt.
	 *
	 * @param data ein Datensatz
	 */
	private void printData(Data data) {
		if(data.isPlain()) {
			_debug.info("Data is Plain: " + data.getName() + " Inhalt: " + data.asTextValue().getText());
		}
		else if(data.isList()) {
			_debug.info("Data is List: " + data.getName());
			final Iterator iterator = data.iterator();
			while(iterator.hasNext()) {
				Data internData = (Data)iterator.next();
				printData(internData);
			}
		}
		else if(data.isArray()) {
			_debug.info("Data is Array: " + data.getName());
			final Data.Array array = data.asArray();
			_debug.info("array = " + array);
			for(int i = 0; i < array.getLength(); i++) {
				printData(array.getItem(i));
			}
		}
		else {
			_debug.info("Was ist denn das?");
		}
	}

	/* ############################# verschiedene Getter-Methoden ############################# */

	/**
	 * Gibt zu einem Konfigurationsbereich und einer Pid das zur �bernahme oder Aktivierung freigegebene Objekt zur�ck, wenn es existiert.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 * @param pid               die Pid des gesuchten Objekts
	 *
	 * @return Das gesuchte Objekt oder <code>null</code>, falls es nicht existiert.
	 */
	private CheckedObject getNewObject(ConfigurationArea configurationArea, String pid) {
		for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
			if(checkedObject.getSystemObject().getPid().equals(pid)) {
				return checkedObject;
			}
		}
		return null;
	}

	/**
	 * Gibt zu einem Konfigurationsbereich und einer Pid das aktuelle Objekt zur�ck, wenn es existiert.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 * @param pid               die Pid des gesuchten Objekts
	 *
	 * @return Das gesuchte Objekt oder <code>null</code>, falls es nicht existiert.
	 */
	private CheckedObject getCurrentObject(ConfigurationArea configurationArea, String pid) {
		for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
			if(checkedObject.getSystemObject().getPid().equals(pid)) {
				return checkedObject;
			}
		}
		return null;
	}

	/**
	 * L�scht aus der Liste der aktuellen Objekte eines Bereichs ein gegebenes Objekt, wenn es enthalten ist.
	 *
	 * @param configurationArea Der zu durchsuchende Konfigurationsbereich
	 * @param object Das zu l�schende Objekt
	 *
	 * @return Das gesuchte Objekt oder <code>null</code>, falls es nicht existiert.
	 */
	private CheckedObject forgetCurrentObject(ConfigurationArea configurationArea, SystemObject object) {
		final Collection<CheckedObject> objects = _currentObjects.get(configurationArea);
		for(Iterator<CheckedObject> iterator = objects.iterator(); iterator.hasNext();) {
			CheckedObject checkedObject = iterator.next();
			if(checkedObject.getSystemObject() == object) {
				iterator.remove();
				return checkedObject;
			}
		}
		return null;
	}

	/**
	 * Gibt zu einem Konfigurationsbereich und einer Pid das in Bearbeitung befindliche Objekt zur�ck, wenn es existiert.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 * @param pid               die Pid des gesuchten Objekts
	 *
	 * @return Das gesuchte Objekt oder <code>null</code>, falls es nicht existiert.
	 */
	private CheckedObject getEditingObject(ConfigurationArea configurationArea, String pid) {
		for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
			if(checkedObject.getSystemObject().getPid().equals(pid)) {
				return checkedObject;
			}
		}
		return null;
	}

	/* ##################### Allgemeine-Methoden ############################ */

	/**
	 * Diese Methode gibt anhand der angegebenen Pid ein Objekt aus dem Datenmodell oder aus den Import-Versorgungsdateien zur�ck.
	 *
	 * @param pid Pid des gesuchten Objekts
	 *
	 * @return das gew�nschte Objekt oder eine Exception, falls dies nicht existiert
	 */
	public SystemObject getObject(String pid) {
		// Erst die Import-Objekte durchforsten, damit auf jeden Fall auch neu erzeugte Objekte ber�cksichtigt werden
		SystemObject systemObject = null;

		// gibt es zu dieser Pid ein importiertes Objekt?
		final ImportObject importObject = _importMap.get(pid);
		if(importObject != null && importObject.getSystemObject() != null) {
			systemObject = importObject.getSystemObject();
		}
		else {
			// es gibt kein importiertes Objekt
			systemObject = getUsingObjects().get(pid);
			// wenn Objekt nicht gefunden wurde, wird gepr�ft, ob in der aktuellen Konfiguration ein Objekt mit der gew�nschten Pid aktiviert ist
			if(systemObject == null) {
				systemObject = _dataModel.getObject(pid);
				
				_debug.warning("Objekt mit Pid " + pid + " wurde nicht gefunden");
			}
		}
		return systemObject;
	}

	public SystemObject getObject(long id) {
		return _dataModel.getObject(id);
	}

	/**
	 * Ermittelt zu allen Konfigurationsbereichen, die Objekte, die in der zu betrachtenden Version g�ltig sind.
	 *
	 * @return die Objekte, die in den zu betrachtenden Versionen g�ltig sind
	 */
	private Map<String, SystemObject> getUsingObjects() {
		if(_viewingObjects == null) {
			_viewingObjects = new HashMap<String, SystemObject>();
			// Version der Konfigurationsbereiche ber�cksichtigen, in der das Objekt sein soll!
			for(Map.Entry<ConfigurationArea, Short> entry : _usingVersionOfConfigurationArea.entrySet()) {
				final ConfigurationArea configurationArea = entry.getKey();
				// alle Bereiche erst in der aktuellen Version betrachten
				// erst die aktuellen Objekte holen ...
				final Collection<SystemObject> currentObjects = configurationArea.getCurrentObjects();

				// Konfigurationsbereich selber muss auch hinzugef�gt werden
				_viewingObjects.put(configurationArea.getPid(), configurationArea);

				// anschlie�end alle aktuellen Objekte des Bereichs
				for(SystemObject object : currentObjects) {
					final String objPid = object.getPid();
					if(!objPid.equals("")) {
						if(object.getType() instanceof DynamicObjectType) {
							_viewingObjects.put(objPid, object);
						}
						else {
							// nur die aktuellen Objekte, die auch noch in der zu betrachtenden Version g�ltig sind
							final ConfigurationObject configObject = (ConfigurationObject)object;
							if(configObject.getNotValidSince() == 0 || configObject.getNotValidSince() > entry.getValue()) {
								_viewingObjects.put(objPid, object);
							}
						}
					}
				}

				// ... dann die neuesten Objekte holen und die aktuellen �berschreiben lassen
				if(configurationArea.getActiveVersion() != entry.getValue()) {
					final Collection<SystemObject> newObjects = configurationArea.getNewObjects();
					for(SystemObject object : newObjects) {
						// Ist das Objekt in der richtigen Version g�ltig?
						if(object.getType() instanceof DynamicObjectType) {
							_viewingObjects.put(object.getPid(), object);
						}
						else {
							final ConfigurationObject configObject = (ConfigurationObject)object;
							if(configObject.getValidSince() <= entry.getValue() && (configObject.getNotValidSince() == 0
							                                                        || configObject.getNotValidSince() > entry.getValue())) {
								_viewingObjects.put(configObject.getPid(), configObject);
							}
						}
					}
				}
			}
		}
		return _viewingObjects;
	}

	Data getConfigurationData(final SystemObject object, final AttributeGroupUsage atgUsage) {
		return ((ConfigSystemObject)object).getConfigurationData(atgUsage, this);
	}

	Data getConfigurationData(final SystemObject object, final AttributeGroup atg) {
		return ((ConfigSystemObject)object).getConfigurationData(atg, this);
	}

	Data getConfigurationData(final SystemObject object, final AttributeGroup atg, final Aspect asp) {
		return ((ConfigSystemObject)object).getConfigurationData(atg, asp, this);
	}

	/**
	 * Erstellt einen Objekt-Typen mit der angegebenen Pid, falls es nicht bereits existiert. Wird zur Ermittlung der Super-Typen ben�tigt.
	 *
	 * @param pid Pid des gesuchten Objekt-Typs
	 *
	 * @return der gew�nschte Objekt-Typ
	 *
	 * @throws IllegalArgumentException Falls zur angegebenen Pid kein Objekt-Typ existiert.
	 */
	private SystemObjectType createSystemObjectType(String pid) throws IllegalArgumentException {
		SystemObjectType systemObjectType = null;
		final ImportObject importObject = _importMap.get(pid);
		if(importObject == null) {
			// Objekt soll auch gar nicht importiert werden -> im Datenmodell nachschauen
			systemObjectType = (SystemObjectType)getUsingObjects().get(pid);
		}
		else {
			// Wurde das System-Objekt bereits erstellt?
			if(importObject.getSystemObject() == null) {
				// wurde das ImportObjekt noch nicht erstellt, dann wird es jetzt erstellt
				try {
					handleImportObject(importObject);
				}
				catch(ConfigurationChangeException ex) {
					throw new RuntimeException(ex);
				}
			}
			systemObjectType = (SystemObjectType)importObject.getSystemObject();
		}
		if(systemObjectType == null) {
			throw new IllegalArgumentException(
					"Zur angegebenen Pid '" + pid + " gibt es weder im Datenmodell noch in den Import-Versorgungsdateien ein Objekt"
			);
		}
		return systemObjectType;
	}

	/**
	 * Speichert die Info als konfigurierenden Datensatz an einem Objekt.
	 *
	 * @param systemObject Objekt, an dem die Info gespeichert werden soll
	 * @param info         die zu speichernde Info
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setInfo(SystemObject systemObject, SystemObjectInfo info) throws ConfigurationChangeException {
		// wenn Info nicht definiert ist, braucht es auch nicht abgespeichert zu werden
		if(info != SystemObjectInfo.UNDEFINED) {
			// wenn der Info-Datensatz bereits existiert, kann dieser ge�ndert werden
			final AttributeGroup atg = _dataModel.getAttributeGroup("atg.info");
			Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
			data.getTextValue("kurzinfo").setText(info.getShortInfoAsXML());
			data.getTextValue("beschreibung").setText(info.getDescriptionAsXML());
			systemObject.setConfigurationData(atg, data);
		}
	}

	/**
	 * Setzt die Markierung, ob ein System-Objekt beibehalten werden soll.
	 *
	 * @param systemObject das zu markierende SystemObjekt.
	 */
	private void setSystemObjectKeeping(SystemObject systemObject) {
		// die Liste der in Bearbeitung befindlichen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _editingObjects.keySet()) {
			for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
				if(checkedObject.getSystemObject() == systemObject) {
					_debug.finer("Ein in Bearbeitung befindliches Objekt '" + systemObject.getPidOrNameOrId() + "' wurde gefunden und wird markiert.");
					checkedObject.setObjectKeeping(true);
					return; // gesucht - gefunden
				}
			}
		}
		// die Liste der zur �bernahme / Aktivierung freigegebenen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				if(checkedObject.getSystemObject() == systemObject) {
					_debug.finer(
							"Ein zur �bernahme / Aktivierung freigegebenes Objekt '" + systemObject.getPidOrNameOrId() + "' wurde gefunden und wird markiert."
					);
					checkedObject.setObjectKeeping(true);
					return; // gesucht - gefunden
				}
			}
		}
		// die Liste der aktuellen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				if(checkedObject.getSystemObject() == systemObject) {
					_debug.finer("Ein aktuelles Objekt '" + systemObject.getPidOrNameOrId() + "' wurde gefunden und wird markiert.");
					checkedObject.setObjectKeeping(true);
					return; // gesucht - gefunden
				}
			}
		}
	}

	/** Setzt alle Markierung, ob ein Objekt beibehalten werden soll, wieder zur�ck. */
	private void unsetSystemObjectKeeping() {
		// die Liste der in Bearbeitung befindlichen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _editingObjects.keySet()) {
			for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
				checkedObject.setObjectKeeping(false);
			}
		}
		// die Liste der zur �bernahme / Aktivierung freigegebenen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				checkedObject.setObjectKeeping(false);
			}
		}
		// die Liste der aktuellen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				checkedObject.setObjectKeeping(false);
			}
		}
	}

	/**
	 * Gibt die Attributgruppenverwendung zur�ck, die in der in Bearbeitung befindlichen Version g�ltig ist.
	 *
	 * @param atg die Attributgruppe
	 * @param asp der Aspekt
	 *
	 * @return Die Attributgruppenverwendung, die in der in Bearbeitung befindlichen Version g�ltig ist oder <code>null</code>, falls es keine g�ltige Verwendung
	 *         gibt.
	 */
	AttributeGroupUsage getAttributeGroupUsage(AttributeGroup atg, Aspect asp) {
		final NonMutableSet atgUsageSet = atg.getNonMutableSet("AttributgruppenVerwendungen");
		if(atgUsageSet != null) {
			final Short version = _usingVersionOfConfigurationArea.get(atg.getConfigurationArea());
			final List<SystemObject> atgUsages = atgUsageSet.getElementsInVersion(version);

			for(SystemObject systemObject : atgUsages) {
				final AttributeGroupUsage atgUsage = (AttributeGroupUsage)systemObject;
				if(atgUsage.getAspect() == asp) return atgUsage;
			}
		}
		return null;
	}

	/**
	 * Wirft eine IllegalStateException. Wird verwendet, falls es zu einer Pid kein Objekt gibt.
	 *
	 * @param pid Pid des Objekts
	 */
	private void throwNoObjectException(final String pid) {
		throw new IllegalStateException("Zu dieser Pid '" + pid + "' gibt es weder ein aktuelles noch ein gerade importiertes Objekt.");
	}

	/* ##################### Import-Klasse ############################ */

	/**
	 * Diese Klasse wird nur f�r den Import benutzt. Sie speichert eine Referenz auf ein {@link de.bsvrz.puk.config.xmlFile.properties.SystemObjectProperties} und auf ein {@link SystemObject}. Damit
	 * auch bekannt ist, wo das Eigenschafts-Objekt importiert werden soll, wird auch der Konfigurationsbereich gespeichert.
	 */
	private class ImportObject {

		/** Der Bereich, in den dieses Objekt importiert werden soll. */
		private ConfigurationArea _configurationArea;

		/** Das zu importierende Objekt. */
		private SystemObjectProperties _properties;

		/** Die in ein SystemObjekt umgewandelten Eigenschaften aus der Versorgungsdatei. */
		private SystemObject _systemObject;

		/**
		 * Speichert den Konfigurationsbereich und das Eigenschafts-Objekt aus einer Versorgungsdatei ab.
		 *
		 * @param configurationArea ein Konfigurationsbereich
		 * @param properties        ein Eigenschafts-Objekt einer Versorgungsdatei
		 */
		public ImportObject(ConfigurationArea configurationArea, SystemObjectProperties properties) {
			_configurationArea = configurationArea;
			_properties = properties;
		}

		/**
		 * Gibt den Konfigurationsbereich dieses Import-Objekts zur�ck.
		 *
		 * @return der Konfigurationsbereich dieses Import-Objekts
		 */
		public ConfigurationArea getConfigurationArea() {
			return _configurationArea;
		}

		/**
		 * Gibt das Eigenschafts-Objekt dieses Import-Objekts zur�ck, welches die Definition eines Objekts aus der Versorgungsdatei enth�lt.
		 *
		 * @return das Eigenschafts-Objekt
		 */
		public SystemObjectProperties getProperties() {
			return _properties;
		}

		/**
		 * Gibt das zum Eigenschafts-Objekt geh�rende System-Objekt zur�ck.
		 *
		 * @return das zum Eigenschafts-Objekt geh�rende System-Objekt
		 */
		public SystemObject getSystemObject() {
			return _systemObject;
		}

		/**
		 * Speichert das zum Eigenschafts-Objekt passende System-Objekt am Import-Objekt.
		 *
		 * @param systemObject das zum Eigenschafts-Objekt passende System-Objekt
		 */
		public void setSystemObject(SystemObject systemObject) {
			_systemObject = systemObject;
		}
	}

	/**
	 * Diese Klasse speichert zu einem bereits bestehenden Objekt, ob es im Konfigurationsbereich beibehalten werden soll, oder nicht. F�r diese Unterscheidung
	 * gibt es ein Flag. Wird es nicht gesetzt, dann wird das Objekt im letzten Schritt des Imports auf {@link de.bsvrz.dav.daf.main.config.SystemObject#invalidate()
	 * ung�ltig} gesetzt.
	 */
	private class CheckedObject {

		/** Ein bestehendes System-Objekt. */
		private SystemObject _systemObject;

		/** Ob nach Abschluss des Import-Vorgangs dieses System-Objekt noch ben�tigt wird oder es auf ung�ltig gesetzt wird. */
		private boolean _isObjectKeeping = false;

		/**
		 * Speicher ein System-Objekt ab, welches nach dem Import �berpr�ft werden soll, ob es noch ben�tigt wird.
		 *
		 * @param systemObject ein System-Objekt
		 */
		public CheckedObject(SystemObject systemObject) {
			_systemObject = systemObject;
		}

		/**
		 * Gibt das System-Objekt zur�ck.
		 *
		 * @return das System-Objekt
		 */
		public SystemObject getSystemObject() {
			return _systemObject;
		}

		/**
		 * Gibt zur�ck, ob das System-Objekt auch nach dem Import beibehalten werden soll.
		 *
		 * @return <code>true</code>, falls das System-Objekt auch nach dem Import beibehalten werden soll, sonst <code>false</code>
		 */
		public boolean isObjectKeeping() {
			return _isObjectKeeping;
		}

		/**
		 * Setzt die Markierung, ob das System-Objekt auch nach dem Import beibehalten werden soll.
		 *
		 * @param objectKeeping gibt an, ob das System-Objekt auch nach dem Import beibehalten werden soll
		 */
		public void setObjectKeeping(boolean objectKeeping) {
			_isObjectKeeping = objectKeeping;
		}
	}
}
