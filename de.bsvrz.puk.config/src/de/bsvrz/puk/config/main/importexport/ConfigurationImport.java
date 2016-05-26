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

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.puk.config.configFile.datamodel.*;
import de.bsvrz.puk.config.xmlFile.parser.ConfigAreaParser;
import de.bsvrz.puk.config.xmlFile.properties.*;
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
 * Diese Klasse importiert die Versorgungsdateien in das bestehende Datenmodell. Zu importierende Bereiche dürfen keine Pid mehrmals benutzen.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConfigurationImport implements ObjectLookup {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Das Datenmodell, in welches die Versorgungsdateien importiert werden sollen. Hierüber werden auch auf Modelldaten des MetaModells zugegriffen. */
	private DataModel _dataModel;

	/**
	 * Objekt, welches die Import-Eigenschaften mit möglichen konkreten Objekten vergleicht. Dabei wird auch ermittelt, ob das Objekt geändert werden kann, wenn
	 * die Eigenschaften nicht übereinstimmen.
	 */
	private final ComparePropertiesWithSystemObjects _objectDiffs;

	/**
	 * Enthält zu importierende Objekte. Diese Map speichert zu einer Pid ein sogenanntes ImportObject, welches Referenzen auf ein SystemObjectProperties und auf
	 * ein SystemObject enthält.
	 */
	private final Map<String, ImportObject> _importMap = new HashMap<String, ImportObject>();

	/**
	 * Diese Map speichert zu einem Konfigurationsbereich, die Objekte, die aktuell gültig sind. Diese Map wird benötigt, um am Ende des Imports herauszubekommen,
	 * welche Objekte auf {@link de.bsvrz.dav.daf.main.config.SystemObject#invalidate() ungültig} gesetzt werden müssen.
	 */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _currentObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/**
	 * Diese Map speichert alle Objekte, die zukünftig aktuell werden. Sie wurden bereits zur Übernahme oder zur Aktivierung freigegeben. Diese Objekte dürfen nur
	 * in begrenztem Maße modifiziert werden.
	 */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _newObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/** Diese Map speichert alle Objekte aus den zu importierenden Konfigurationsbereichen, die sich in Bearbeitung befinden. */
	private final Map<ConfigurationArea, Collection<CheckedObject>> _editingObjects = new HashMap<ConfigurationArea, Collection<CheckedObject>>();

	/** Enthält alle Konfigurationsbereiche, die importiert werden sollen */
	private final Set<ConfigurationArea> _allImportedConfigurationAreas = new HashSet<ConfigurationArea>();

	/**
	 * In dieses Set wird die Pid eines Objekt-Typen eingetragen, sobald er erstellt werden soll. Dabei wird überprüft, ob die Pid sich bereits in dem Set
	 * befindet. Falls ja, dann befindet sich eine Schleife in den Versorgungsdateien (der Objekt-Typ braucht einen anderen Typen und dieser braucht wiederum den
	 * ursprünglichen Objekt-Typ, der aber noch nicht erstellt wurde). Nach Erstellung des Objekt-Typen wird die Pid aus dem Set wieder entfernt.
	 */
	private final Set<String> _objectTypesToCreate = new HashSet<String>();

	/** Gibt an, ob noch Referenzen aufgelöst werden sollen, oder nicht. */
	private boolean _dissolveReference = true;

	/** Zu jedem Konfigurationsbereich wird die Version gespeichert, in der der Bereich betrachtet werden soll. */
	private final Map<ConfigurationArea, Short> _usingVersionOfConfigurationArea = new HashMap<ConfigurationArea, Short>();

	/** Speichert alle Objekte, die betrachtet werden sollen. (In Abhängigkeit von der zu betrachtenden Version.) */
	private Map<String, SystemObject> _viewingObjects;

	/** Enthält für jeden importierten Bereich ein Properties-Objekt. */
	private Map<ConfigurationAreaProperties, ConfigurationArea> _areaProperty2ConfigurationArea = new HashMap<ConfigurationAreaProperties, ConfigurationArea>();

	/**
	 * Der Konstruktor führt den Import der angegebenen Konfigurationsbereiche durch.
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
			// Parser für die XML-Dateien initialisieren
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
					// File-Objekt für den Konfigurationsbereich erstellen und XML-Datei einlesen.
					final File file = new File(importPath, configurationAreaPid + ".xml");
					final ConfigurationAreaProperties configurationAreaProperties = parser.parse(file);

					// System-Objekt des Konfigurationsbereichs einlesen
					final ConfigurationArea configurationArea = assignConfigurationArea(configurationAreaProperties, configurationControl);
					_areaProperty2ConfigurationArea.put(configurationAreaProperties, configurationArea);

					// alle zu importierenden Objekte werden in einer ImportMap gespeichert
					for(SystemObjectProperties objectProperty : configurationAreaProperties.getObjectProperties()) {
						ImportObject importObject = _importMap.put(objectProperty.getPid(), new ImportObject(configurationArea, objectProperty));
						// Falls bereits ein Eintrag mit der Pid als Schlüssel in der Map vorkommt,
						// muss eine Exception geworfen werden, damit auch alle Objekte verarbeitet werden.
						if(importObject != null) {
							throw new IllegalStateException(
									"Diese Pid " + objectProperty.getPid() + " wurde bereits in einem der anderen zu importierenden Bereiche "
									+ "verwendet. Für den Import ist es notwendig, dass alle Pids der zu importierenden Bereich nur einmal vorkommen."
							);
						}
					}

					// Alle (aktuelle, in Bearbeitung, zur Übernahme/Aktivierung freigegebene) Objekte, die in diesem Konfigurationsbereich sind werden eingelesen.
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
					_debug.error("Beim Importieren einer Versorgungsdatei kam es zu Fehlern beim Umsetzen der entsprechenden Änderungen in der Konfiguration" , ex);
					throw new ConfigurationChangeException(ex);
				}
			}// for, über alle zu importierenden Bereiche

			_debug.info("Anzahl der Definitionen und SystemObjekte, die zu importieren sind", _importMap.values().size());

			_dissolveReference = true;
			int counter = 1;
			while(_dissolveReference) {
				// erstmal ist davon auszugehen, dass keine Referenzen aufzulösen sind.
				_dissolveReference = false;
				/*
				 * 1.) Zu importierende Objekte, die noch nicht existieren oder nicht verändert werden dürfen, werden neu erstellt.
				 *     Objekte, die bereits existieren, werden also überprüft, ob sie geändert werden dürfen, falls sie geändert werden müssen.
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
					_debug.config("Bestehende Objekte wurden durch neue Objekte ersetzt - Referenzen müssen aufgelöst werden. Durchlauf", counter++);
					// ob ein Objekt beibehalten wird, muss hier zurückgesetzt werden!
					unsetSystemObjectKeeping();
					// alle Konfigurationsbereiche setzen, da diese in der Schleife nicht betrachtet werden.
					for(ConfigurationArea configurationArea : _allImportedConfigurationAreas) {
						setSystemObjectKeeping(configurationArea);
					}
				}
			}

			// prüfen und ggf. handeln, falls sich der KV des Bereichs geändert hat.
			checkChangeOfConfigurationAuthority();

			// 2.) Neu erstellte Objekte werden vervollständigt und bereits existierende Objekte so verändert, dass sie mit den Import-Daten übereinstimmen.
			_debug.config("Alle Modelldaten der zu importierenden Konfigurationsbereiche werden vervollständigt.");
			// erst alle Attributlisten (wird für die Default-Parameter-Datensätze benötigt)
			for(ImportObject importObject : _importMap.values()) {
				if(importObject.getProperties() instanceof AttributeListProperties) {
					completeImportObject(importObject);
				}
			}
			// dann alle Attributgruppen (wird für die Default-Parameter-Datensätze benötigt)
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

			_debug.config("Default-Werte werden vervollständigt.");
			for(ImportObject importObject : _importMap.values()) {
				if(!(importObject.getProperties() instanceof ConfigurationConfigurationObject)) {
					completeDefaults(importObject);
				}
			}

			_debug.config("Alle Objekte der zu importierenden Konfigurationsbereiche werden vervollständigt.");
			for(ImportObject importObject : _importMap.values()) {
				if(importObject.getProperties() instanceof ConfigurationConfigurationObject) {
					completeImportObject(importObject);
				}
			}

			// Nach dem Import muss noch aufgeräumt werden. Nicht mehr benötigte Objekte werden auf ungültig gesetzt oder werden gelöscht.
			// Bereits auf ungültig gesetzte Objekte, werden wieder gültig, wenn sie gebraucht werden.
			_debug.config("Aufräumen bestehender Objekte nach dem Import.");
			invalidateNoLongerRequiredObjects();
			deleteNoLongerRequiredObjects();
			validateRequiredObjects();

			// hier die Konfigurationsänderungen aktualisieren
			for(Map.Entry<ConfigurationAreaProperties, ConfigurationArea> entry : _areaProperty2ConfigurationArea.entrySet()) {
				setConfigurationAreaChangeInformation(entry.getKey(), entry.getValue());
			}

			long endTime = System.currentTimeMillis();
			_debug.info("Der Import wurde durchgeführt. Dauer in Sekunden", ((endTime - startTime) / 1000));
		}
		catch(Exception ex) {
			throw new ConfigurationChangeException(ex);
		}
	}

	/**
	 * Prüft, ob sich der KV des Bereichs geändert hat. Wenn ja, dann wird ein neuer Zuständiger für diesen Bereich eingetragen. Wenn nicht, dann wird ein evtl.
	 * vorhandener neuer Zuständiger gelöscht.
	 *
	 * @throws ConfigurationChangeException Falls der neue Zuständige nicht geschrieben werden kann.
	 */
	private void checkChangeOfConfigurationAuthority() throws ConfigurationChangeException {
		final ConfigurationAuthority configurationAuthority = _dataModel.getConfigurationAuthority();
		// KV der Bereiche prüfen
		if(configurationAuthority != null) {
			final AttributeGroup configurationAreaAtg = _dataModel.getAttributeGroup("atg.konfigurationsBereichEigenschaften");
			for(Map.Entry<ConfigurationAreaProperties, ConfigurationArea> entry : _areaProperty2ConfigurationArea.entrySet()) {
				final ConfigurationArea configurationArea = entry.getValue();
				final String areaPropertyAuthority = entry.getKey().getAuthority();
				if(!areaPropertyAuthority.equals(configurationArea.getConfigurationAuthority().getPid())) {
					// neuen Zuständigen eintragen
					final ConfigurationAuthority newAuthority = (ConfigurationAuthority)getObject(areaPropertyAuthority);
					if(newAuthority == null) {
						throwNoObjectException(areaPropertyAuthority);
					}

					for(ImportObject importObject : _importMap.values()) {
						if(importObject.getSystemObject() == newAuthority) {
							_debug.fine("Neuen KV vervollständigen", newAuthority);
							completeImportObject(importObject);
						}
					}

					// ist die Kodierung eindeutig?
					checkCodingOfConfigurationAuthority(newAuthority.getCoding(), newAuthority.getPid());

					final Data data = getConfigurationData(configurationArea, configurationAreaAtg);
					data.getReferenceValue("neuerZuständiger").setSystemObject(newAuthority);
					configurationArea.setConfigurationData(configurationAreaAtg, data);
					_debug.info(
							"Der Bereich " + configurationArea.getPid() + " ändert ab der nächsten Version seinen Zuständigen zu " + newAuthority.getPid() + "."
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
					final SystemObject oldConfigurationAuthority = data.getReferenceValue("zuständiger").getSystemObject();
					if(oldConfigurationAuthority == null || oldConfigurationAuthority.getId() != newAuthority.getId()) {

						for(ImportObject importObject : _importMap.values()) {
							if(importObject.getSystemObject() == newAuthority) {
								_debug.fine("Neuen KV vervollständigen", newAuthority);
								completeImportObject(importObject);
							}
						}

						// ist die Kodierung eindeutig?
						checkCodingOfConfigurationAuthority(newAuthority.getCoding(), newAuthority.getPid());
						// neuen Zuständigen eintragen
						data.getReferenceValue("neuerZuständiger").setSystemObject(newAuthority);
						configurationArea.setConfigurationData(configurationAreaAtg, data);
						_debug.info(
								"Der Bereich " + configurationArea.getPid() + " ändert ab der nächsten Version seinen Zuständigen von " +
								oldConfigurationAuthority.getPid() + "/" + oldConfigurationAuthority.getId() + " zu " +
								newAuthority.getPid() + "/" + newAuthority.getId() + "."
						);
					}
					else {
						// prüfen, ob beim neuen Zuständigen die null drinsteht!
						final Data.ReferenceValue referenceValue = data.getReferenceValue("neuerZuständiger");
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
			// Dieser Vergleich ist auch möglich, wenn der KV in den zu importierenden Bereichen neu angelegt wurde, da dann
			// _dataModel.getConfigurationAuthority() == null zurückgibt. Diese Gleichung sollte für keinen Bereich zutreffen, da alle einen anderen
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
	 * Diese Methode prüft, ob es bereits einen Konfigurationsbereich passend zur Versorgungsdatei (dargestellt durch das {@link ConfigurationAreaProperties
	 * Eigenschafts-Objekt}) gibt. Existiert er noch nicht, so wird eine neue Bereichs-Datei angelegt.
	 *
	 * @param property             Eigenschafts-Objekt, welches die Versorgungsdatei repräsentiert
	 * @param configurationControl Objekt, welches spezielle Zugriffsmethoden auf die Konfiguration enthält
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
		// Info überprüfen
		if(_objectDiffs.isInfoDifferent(configurationArea.getInfo(), property.getInfo()) && _objectDiffs.isInfoProcessable(
				configurationArea.getInfo(), property.getInfo()
		)) {
			setInfo(configurationArea, property.getInfo());
		}
		// KonfigurationsÄnderungen überprüfen, ob der Datensatz geändert werden kann.
		if(!_objectDiffs.isConfigurationAreaChangeInformationProcessable(property.getConfigurationAreaChangeInformation(), configurationArea)) {
			throw new ConfigurationChangeException(
					"Die Konfigurationsänderungen können nicht am Konfigurationsbereich " + configurationArea.getPidOrNameOrId() + " geändert werden."
			);
		}
		// Darf hier nicht mehr gemacht werden, weil die richtige Versionsnummer für die Aktualisierung der Konfigurationsänderungseinträge
		// noch nicht feststeht.
		// setConfigurationAreaChangeInformation(property, configurationArea);

		// die Zeiten, wann sich ein Datensatz geändert oder ein Objekt erzeugt wurde, müssen initialisiert werden
		((ConfigConfigurationArea)configurationArea).initialiseTimeOfLastChanges();
		return configurationArea;
	}

	/**
	 * Setzt die KonfigurationsÄnderungen an einem Konfigurationsbereich.
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
	 * Der Konfigurationsbereich wird überprüft, ob auch er geändert wurde.
	 *
	 * @param property          Eigenschaftsobjekt des Konfigurationsbereichs
	 * @param configurationArea Konfigurationsbereichs-Objekt
	 *
	 * @throws ConfigurationChangeException Falls es Unterschiede zwischen des Eigenschaftsobjekts und Konfigurationsbereichs-Objekts gibt.
	 */
	private void checkConfigurationArea(final ConfigurationAreaProperties property, final ConfigurationArea configurationArea)
			throws ConfigurationChangeException {
		// Der Konfigurationsbereich existiert bereits - prüfen, ob alle Werte übereinstimmen.
		// Namen des Konfigurationsbereichs überprüfen
		if(_objectDiffs.isNameDifferent(property.getName(), configurationArea.getName())) {
			if(_objectDiffs.isNameProcessable(property.getName(), configurationArea)) {
				configurationArea.setName(property.getName());
			}
			else {
				throw new ConfigurationChangeException(
						"Der Name des Konfigurationsbereichs darf nicht geändert werden. Alter Name: " + configurationArea.getName() + " neuer Name: "
						+ property.getName()
				);
			}
		}

		// Konfigurationsbereiche können nicht verändert werden - wenn einer verändert werden soll, muss ein neuer Bereich angelegt werden
		if(configurationArea.getNotValidSince() != 0) {
			
			((ConfigConfigurationObject)configurationArea).simpleRevalidate();
		}
	}

	/**
	 * Diese Methode erstellt einen neuen Konfigurationsbereich.
	 *
	 * @param property             Das Eigenschafts-Objekt zum Bereich.
	 * @param configurationControl Wird zum Erstellen eines neuen Bereichs benötigt.
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
					// der Typ des Konfigurationsverantwortlichen muss zu den aktiven Objekten gehören
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
			// Prüfen, ob Kodierung eindeutig ist. Alle KVs betrachten und prüfen.
			checkCodingOfConfigurationAuthority(authorityCoding, authorityPid);

			// Bereich und Konfigurationsverantwortlicher werden erstellt
			configurationArea = ((ConfigDataModel)_dataModel).createConfigurationArea(
					property.getName(), property.getPid(), authorityType, authorityPid, authorityName, authorityCoding
			);

			// Der Konfigurationsverantwortliche muss hinzugefügt werden, da er gerade erst erstellt wurde.
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
	 * Diese Methode prüft, ob die Kodierung des zu verwendenden Konfigurationsverantwortlichen eindeutig ist. Es darf also keinen aktuellen Verantwortlichen
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
	 * Erzeugt einen Datensatz mit den Konfigurationsänderungen aus der Versorgungsdatei und speichert diesen am Konfigurationsbereich ab.
	 *
	 * @param configurationAreaChangeInformation
	 *                          die Konfigurationsänderungen aus der Versorgungsdatei
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht am Konfigurationsbereich gespeichert werden konnte.
	 */
	private void setConfigurationAreaChangeInformation(
			ConfigurationAreaChangeInformation[] configurationAreaChangeInformation, ConfigurationArea configurationArea
	) throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.konfigurationsÄnderungen");

		// bestehendes Data holen
		final Data existData = getConfigurationData(configurationArea, atg);

		// neues Data wird erzeugt!
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		final Data.Array array = data.getArray("KonfigurationsÄnderung");
		// Länge festlegen
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
			if(version > lastModifiedVersion) version = lastModifiedVersion; // Version auf die größte verwendete Version beschränken
			item.getUnscaledValue("Version").set(version);
		}
		configurationArea.setConfigurationData(atg, data);
	}

	/**
	 * Ermittelt die Versionsnummer zu einem Änderungsvermerk, der zuvor schon mal gespeichert wurde.
	 *
	 * @param existData existierendes Data zu den Konfigurations-Änderungen
	 * @param info      ein neuer Info-Eintrag
	 *
	 * @return Versionsnummer dieses Info-Eintrages oder "-1", falls der Eintrag noch nicht existierte.
	 */
	private int existChangeInformation(Data existData, ConfigurationAreaChangeInformation info) {
		if(existData == null) return -1;
		Data.Array array = existData.getArray("KonfigurationsÄnderung");
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
	 * Liest alle aktuellen und zur Aktivierung/Übernahme freigegebenen Objekte aus dem Konfigurationsbereich aus und merkt sich diese lokal.
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

		// Alle zukünftigen, bereits vorhandene Objekte einladen - unterteilt nach freigegebenen und in Bearbeitung befindlichen Objekten.
		// Die größere Versionsnummer zwischen der "Übernehmbaren" und der "Aktivierbaren" Version ermitteln.
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
				// kann es nicht bei den zur Übernahme/Aktivierung freigegebenen und in Bearbeitung befindlichen Objekten sein.
				_debug.error("Ein dynamisches Objekt bei den neuen Objekten gefunden", systemObject.getPidOrNameOrId());
			}
		}
		// ermittelte Objekte hinzufügen
		_newObjects.put(configurationArea, newCheckedObjects);
		_editingObjects.put(configurationArea, editingCheckedObjects);
	}

	/**
	 * Diese Methode prüft, ob es bereits ein passendes Objekt zu einer zu importierenden Definition gibt und verwendet dieses. Wenn dieses Objekt allerdings
	 * verändert werden muss, wird überprüft, ob es auch verändert werden darf. Wenn es nicht verändert werden darf, wird ein neues Objekt angelegt.
	 * <p>
	 * Wird ein passendes Objekt gefunden, dann wird damit weitergearbeitet. Wenn nicht, dann wird ein neues Objekt erstellt.
	 *
	 * @param importObject Import-Objekt, welches ein Import-Objekt und ein System-Objekt enthält.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht angelegt werden.
	 */
	private void handleImportObject(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final String pid = importObject.getProperties().getPid();
		_debug.finer("*** Folgende Pid wird jetzt weiterverarbeitet", pid);
		CheckedObject checkedObject = getNewObject(configurationArea, pid);
		if(checkedObject != null) {
			// Es gibt ein zur Übernahme / Aktivierung freigegebenes Objekt.
			// wenn dieses Objekt geändert werden muss und geändert werden darf, dann soll es weiter verwendet werden
			boolean isObjectProcessable = _objectDiffs.isObjectProcessable(importObject.getProperties(), checkedObject.getSystemObject());
			if(_objectDiffs.isObjectDifferent(importObject.getProperties(), checkedObject.getSystemObject())) {
				_debug.fine(
						"Das zur Übernahme / Aktivierung freigegebene Objekt mit Pid '" + pid + "' und Id '" + checkedObject.getSystemObject().getId()
						+ "' muss geändert werden. Darf es geändert werden? " + isObjectProcessable
				);
			}
			else {
				_debug.finer(
						"Objekt mit Pid " + checkedObject.getSystemObject().getPid()
						+ " unterscheidet sich nicht von einem zur Übernahme / Aktivierung freigegebene Objekt!"
				);
			}
			if(isObjectProcessable) {
				// wenn es geändert werden kann, dann wird dieses weiterverwendet, wenn nicht, dann muss ein neues angelegt werden.
				final ConfigurationObject configurationObject = getModifiableObject(configurationArea, pid);
				if(configurationObject != null) {
					// ein Objekt mit gleicher Pid welches in Bearbeitung ist, muss gelöscht werden, da
					// das aktive Objekt verwendet wird
					
					((ConfigSystemObject)configurationObject).simpleInvalidation();
					_dissolveReference = true;
				}
				handleObject(checkedObject, importObject);
				return;
			}
			else {
				if(importObject.getSystemObject() == null || importObject.getSystemObject() == checkedObject.getSystemObject()) {
					// Wenn die beiden Objekte gleich sind, diese aber nicht weiterverarbeitet werden dürfen, dann müssen
					// die Referenzen mit einem neuen Objekt aufgelöst werden.
					_debug.finest("DissolveReference because Pid " + pid);
					_dissolveReference = true;
					importObject.setSystemObject(null);
				}
			}
		}
		else {
			// Es gibt kein zur Übernahme / Aktivierung freigegebenes Objekt - gibt es ein aktuelles Objekt?
			checkedObject = getCurrentObject(configurationArea, pid);
			if(checkedObject != null) {
				// es gibt ein aktuelles Objekt
				// wenn dieses Objekt geändert werden muss und geändert werden darf, dann soll es weiter verwendet werden
				boolean isObjectProcessable = _objectDiffs.isObjectProcessable(importObject.getProperties(), checkedObject.getSystemObject());
				if(_objectDiffs.isObjectDifferent(importObject.getProperties(), checkedObject.getSystemObject())) {
					_debug.fine(
							"Das aktuelle Objekt mit Pid '" + pid + "' und Id '" + checkedObject.getSystemObject().getId() + "' muss geändert werden (Typ: "
							+ checkedObject.getSystemObject().getType().getPidOrNameOrId() + "). Darf es geändert werden? " + isObjectProcessable
					);
				}
				else {
					_debug.finer("Objekt mit Pid " + checkedObject.getSystemObject().getPid() + " unterscheidet sich nicht von einem aktuellen Objekt!");
				}
				if(isObjectProcessable) {
					// wenn es geändert werden kann, dann wird dieses weiterverwendet, wenn nicht, dann muss ein neues angelegt werden.
					final ConfigurationObject configurationObject = getModifiableObject(configurationArea, pid);
					if(configurationObject != null) {
						// ein Objekt mit gleicher Pid welches in Bearbeitung ist, muss gelöscht werden, da
						// das aktive Objekt verwendet wird
						
						((ConfigSystemObject)configurationObject).simpleInvalidation();
						_dissolveReference = true;
					}
					handleObject(checkedObject, importObject);
					return;
				}
				else {
					if(importObject.getSystemObject() == null || importObject.getSystemObject() == checkedObject.getSystemObject()) {
						// Wenn die beiden Objekte gleich sind, diese aber nicht weiterverarbeitet werden dürfen, dann müssen
						// die Referenzen mit einem neuen Objekt aufgelöst werden.
						final SystemObject checkedSystemObject = checkedObject.getSystemObject();
						if(checkedSystemObject instanceof DynamicObject) {
							_debug.fine("dynamisches Objekt wird gelöscht", checkedSystemObject);
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
		// Es gibt weder ein zur Übernahme / Aktivierung freigegebenes Objekt, noch ein aktuelles Objekt.
		// Gibt es ein in Bearbeitung befindliches Objekt, welches vor dem Import bereits in Bearbeitung war?
		checkedObject = getEditingObject(configurationArea, pid);
		if(checkedObject != null) {
			// Es gibt ein in Bearbeitung befindliches Objekt -> dies kann nach belieben geändert werden.
			_debug.fine("Ein in Bearbeitung befindliches Objekt mit der Pid '" + pid + "' wird weiterverarbeitet.");
			// Objekt weiterverarbeiten - es kann nach belieben verändert werden
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
	 * Ermittelt zu einer Pid ein in Bearbeitung befindliches Objekt, löscht es aus der entsprechenden Liste, damit es nicht nochmal betrachtet wird und gibt es
	 * zurück.
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
	 * Diese Methode wird aufgerufen, wenn ein zu importierendes Objekt vervollständigt werden soll. Bei diesem Vorgang werden Mengen und Datensätze mit Referenzen
	 * erzeugt oder verändert. Je nach Typ des Eigenschafts-Objekts wird die entsprechende Methode zur Weiterverarbeitung aufgerufen.
	 *
	 * @param importObject Import-Objekt, welches ein Import-Objekt und ein System-Objekt enthält.
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollständigt werden konnte (Mengen und Datensätze konnten nicht hinzugefügt werden).
	 */
	private void completeImportObject(ImportObject importObject) throws ConfigurationChangeException {
		// Eigenschafts-Objekt des zu importierenden Objekts
		final SystemObjectProperties property = importObject.getProperties();
		final SystemObject systemObject = importObject.getSystemObject();
		// diese Objekte sollten alle bereits erstellt sein und benötigen noch die konfigurierenden Datensätze
		_debug.finer("### Folgendes Objekt wird jetzt vervollständigt", systemObject.getPidOrNameOrId());
		if(systemObject != null) {
			// je nachdem, um welches Eigenschafts-Objekt es sich handelt, müssen unterschiedliche Datensätze/Mengen vervollständigt werden
			if(property instanceof AspectProperties) {
				// ist bereits vollständig erstellt worden
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
				// ConfigurationObjectProperties und SystemObjectProperties werden gleichermaßen verarbeitet
				completeSystemObject(importObject);
			}
		}
		else {
			_debug.warning("Zu diesen Properties '" + property.getPid() + "' gibt es kein System-Objekt!");
		}
	}

	/**
	 * Default-Werte werden an den Objekten vervollständigt.
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
	 * Aktuelle Objekte und Objekte, die zur Übernahme / Aktivierung freigegeben wurden werden geprüft, ob sie bei einem vorhergehenden Import auf ungültig gesetzt
	 * wurden, nach diesem Import aber benötigt werden. Diese Objekte werden dann mittels {@link ConfigurationObject#revalidate() revalidate} wieder gültig.
	 *
	 * @throws ConfigurationChangeException Falls ein benötigtes Objekt nicht zurück auf gültig gesetzt werden kann.
	 */
	private void validateRequiredObjects() throws ConfigurationChangeException {
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				if(checkedObject.isObjectKeeping()) {
					// das Objekt wird noch benötigt, wenn es schon mal auf ungültig gesetzt wurde, wieder auf gültig setzen
					final SystemObject systemObject = checkedObject.getSystemObject();
					if(systemObject instanceof ConfigurationObject) {
						ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
						if(configurationObject.getNotValidSince() != 0) {
							_debug.finer(
									"Habe ein Objekt gefunden, welches in der nächsten Version ungültig wird, aber noch gebraucht wird. Es wird wieder gültig gemacht: "
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
					// das Objekt wird noch benötigt, wenn es schon mal auf ungültig gesetzt wurde, wieder auf gültig setzen
					final SystemObject systemObject = checkedObject.getSystemObject();
					if(systemObject instanceof ConfigurationObject) {
						ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
						if(configurationObject.getNotValidSince() != 0) {
							_debug.finer(
									"Habe ein Objekt gefunden, welches in der nächsten Version gebraucht wird, aber ungültig wird: "
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
	 * Aktuelle und freigegebene Objekte werden überprüft, ob sie nach dem Import weiterhin gebraucht werden. Werden sie nicht benötigt, dann werden sie auf
	 * ungültig gesetzt.
	 */
	private void invalidateNoLongerRequiredObjects() {
		_debug.finer("Folgende Objekte werden auf ungültig gesetzt: ");
		// alle aktuellen Objekte betrachten
		int counter1 = 0;
		int counter2 = 0;
		for(ConfigurationArea configurationArea : _currentObjects.keySet()) {
			for(CheckedObject checkedObject : _currentObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr benötigt wird, wird es auf ungültig gesetzt.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr benötigtes aktuelles Objekt = " + systemObject.toString());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das aktuelle Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht ungültig gemacht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der aktuellen Objekte, die auf ungültig gesetzt wurden", counter1);
		if(counter2 > 0) _debug.info("Anzahl der aktuellen Objekte, die gültig bleiben", counter2);

		counter1 = 0;
		counter2 = 0;

		// die freigegebenen Objekte
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr benötigt wird, wird es auf ungültig gesetzt.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr benötigtes freigegebenes Objekt = " + systemObject.getPidOrNameOrId());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das freigegebene Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht ungültig gemacht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der zur Übernahme/Aktivierung freigegebenen Objekte, die auf ungültig gesetzt wurden", counter1);
		if(counter2 > 0) _debug.info("Anzahl der zur Übernahme/Aktivierung freigegebenen Objekte, die gültig bleiben", counter2);
	}

	/** In Bearbeitung befindliche Objekte werden überprüft, ob sie nach dem Import nicht mehr benötigt werden. Wenn dem so ist, werden sie gelöscht. */
	private void deleteNoLongerRequiredObjects() {
		_debug.finer("Folgende in Bearbeitung befindliche Objekte werden gelöscht: ");
		int counter1 = 0;
		int counter2 = 0;
		for(ConfigurationArea configurationArea : _editingObjects.keySet()) {
			for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
				if(!checkedObject.isObjectKeeping()) {
					// Falls das Objekt nicht mehr benötigt wird, wird es gelöscht.
					try {
						final SystemObject systemObject = checkedObject.getSystemObject();
						_debug.finer("Nicht mehr benötigtes in Bearbeitung befindliches Objekt = " + systemObject.getPidOrNameOrId());
						
						((ConfigSystemObject)systemObject).simpleInvalidation();
						counter1++;
					}
					catch(ConfigurationChangeException ex) {
						// Sollte eigentlich nie vorkommen.
						final StringBuilder errorMessage = new StringBuilder();
						errorMessage.append("Das in Bearbeitung befindliche Objekt '").append(checkedObject.getSystemObject().getPidOrNameOrId()).append(
								"' konnte nicht gelöscht werden"
						);
						_debug.warning(errorMessage.toString(), ex.toString());
					}
				}
				else {
					counter2++;
				}
			}
		}
		if(counter1 > 0) _debug.info("Anzahl der in Bearbeitung befindlichen gelöschten Objekte", counter1);
		if(counter2 > 0) _debug.info("Anzahl der in Bearbeitung befindlichen nicht gelöschten Objekte", counter2);
	}

	/* ############################## Methoden zur Bearbeitung und Erstellung von Objekten ############################ */

	/**
	 * Ermittelt anhand des Eigenschafts-Objekts, welche Methode aufgerufen werden muss, damit das System-Objekt weiterverarbeitet bzw. erstellt wird.
	 *
	 * @param checkedObject Enthält das System-Objekt, welches weiterverarbeitet werden soll. <code>null</code>, wenn es neues Objekt erstellt werden soll und noch
	 *                      keines vorhanden war.
	 * @param importObject  Import-Objekt, welches ein Import-Objekt und ein System-Objekt enthält.
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
			// ConfigurationObjectProperties und SystemObjectProperties werden gleichermaßen verarbeitet
			handleSystemObject(importObject);
		}
	}

	/* ##################### Aspect-Methoden ############################ */

	/**
	 * Überarbeitet das übergebene System-Objekt. Falls keines vorhanden ist, wird aus einem Eintrag in der Versorgungsdatei - dargestellt durch ein
	 * Property-Objekt - ein Aspekt erstellt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Aspekts enthält
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
			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), aspect.getName())) {
				aspect.setName(property.getName());
			}
			// Info überprüfen
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
	 * Erstellt aus einem Eintrag in der Versorgungsdatei eine Attributliste, oder verändert ein bestehendes System-Objekt, so dass es mit der Definition
	 * übereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eine Attributliste enthält
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
			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), atl.getName())) {
				atl.setName(property.getName());
			}
			// Info überprüfen
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
	 * Diese Methode vervollständigt eine Attributliste mit konfigurierenden Datensätzen.
	 *
	 * @param importObject Objekt, welches die Daten für den Import einer Attributliste enthält
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollständigt werden konnte (Mengen und Datensätze).
	 */
	private void completeAttributeListDefinition(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final AttributeListDefinition atl = (AttributeListDefinition)importObject.getSystemObject();
		final AttributeListProperties property = (AttributeListProperties)importObject.getProperties();
		try {
			setAttributeObjectSet(configurationArea, atl, property.getAttributeAndAttributeList());
		}
		catch(ConfigurationChangeException ex) {
			final String errorMessage = "Die Attributliste " + property.toString() + " konnte nicht vollständig erstellt werden";
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

		// prüfen, ob zwei Attribute mit gleichem Namen versehen wurden
		final Set<String> attributeNames = new HashSet<String>();

		// erhält alle neu erzeugten Attribute dieser Attributgruppe
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
			// prüfen, ob das Attribut geändert werden darf, wenn es geändert werden muss, oder ob ein neues angelegt wird!
			if(att != null) {
				if(_objectDiffs.isAttributeProcessable(configurationAttribute, att, position)) {
					// Attribut kann weiterverarbeitet werden
					// Info überprüfen
					if(_objectDiffs.isInfoDifferent(info, att.getInfo())) {
						setInfo(att, info);
					}
					// Attribut-Eigenschaften überprüfen
					if((maxCount != att.getMaxCount() || isCountVariable != att.isCountVariable() || attributeType != att.getAttributeType())
					   || position != att.getPosition()) {
						setAttributeProperties(att, position, maxCount, isCountVariable, attributeType);
					}
					// Default-Wert überprüfen
					if(_objectDiffs.isDefaultDifferent(att, aDefault)) {
						setDefault(att, aDefault);
					}
				}
				else {
					// Das Attribut kann nicht weiterverarbeitet werden - muss je nach Referenzierungsart auf veraltet gesetzt oder gelöscht werden.
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
				objectSet.add(att);	// neues Attribut wird hinzugefügt
				_debug.finer("Neues Attribut '" + name + "' wird erstellt.");
			}
			else {
				setSystemObjectKeeping(att);
			}
			// dies ist ein importiertes Attribut
			newAttributes.add(att);
			position++;
		}

		// nur die importierten Attribute dürfen in der Menge vorhanden sein
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
	 * Erstellt aus einem Property-Objekt (welches einem Eintrag in der Versorgungsdatei entspricht) einen AttributTypen, oder verändert ein bestehendes
	 * System-Objekt, so dass es mit der Definition übereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Attribut-Typen enthält
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
				// Namen überprüfen
				if(_objectDiffs.isNameDifferent(property.getName(), attributeType.getName())) {
					attributeType.setName(property.getName());
				}
				// Info überprüfen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Attribut-Typ-Eigenschaften überprüfen
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
				// Namen überprüfen
				if(_objectDiffs.isNameDifferent(property.getName(), attributeType.getName())) {
					attributeType.setName(property.getName());
				}
				// Info überprüfen
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

				// Eigenschaften überprüfen
				if(attributeTypePropertiesData == null || _objectDiffs.isIntegerAttributeTypePropertiesDifferent(def, attributeType)) {
					setIntegerAttributeTypeProperties(attributeType, valueRange, def.getBits());
				}

				// Vorhandene Zustände merken
				final List<IntegerValueState> stateList = new LinkedList<IntegerValueState>();
				NonMutableSet stateSet = attributeType.getNonMutableSet("zustände");
				if(stateSet != null) {
					for(SystemObject systemObject : stateSet.getElementsInModifiableVersion()) {
						final IntegerValueState valueState = (IntegerValueState)systemObject;
						stateList.add(valueState);
					}
					setSystemObjectKeeping(stateSet);
				}
				else {
					// Menge der Zustände neu anlegen
					final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.INTEGER_VALUE_STATES);
					stateSet = (NonMutableSet)configurationArea.createConfigurationObject(type, "", "zustände", null);
					attributeType.addSet(stateSet);
				}
				// Menge der Zustände überprüfen
//				ObjectSet stateSet = attributeType.getObjectSet("zustände");
//				if (stateSet == null) {
//					// Menge muss erzeugt und dem Attribut-Typen hinzugefügt werden
//					final ConfigurationObjectType type = (ConfigurationObjectType) _dataModel.getType(Pid.SetType.INTEGER_VALUE_STATES);
//					stateSet = (ObjectSet) configurationArea.createConfigurationObject(type, "", "zustände", null);
//					attributeType.addSet(stateSet);
//				} else {
//					setSystemObjectKeeping(stateSet);
//				}

				// die einzelnen Zustände überprüfen und ggf. neu anlegen
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
						// Info überprüfen
						if(_objectDiffs.isInfoDifferent(configurationState.getInfo(), integerValueState.getInfo())) {
							setInfo(integerValueState, configurationState.getInfo());
						}
						// Eigenschaften überpüfen
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
					_debug.finer("Ein neuer Fließkommazahl-Attribut-Typ mit der Pid '" + property.getPid() + "' wurde angelegt.");
				}
				// Info überprüfen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Eigenschaften überprüfen
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
				// Info überprüfen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				// Eigenschaften überprüfen
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
				// Info überprüfen
				if(_objectDiffs.isInfoDifferent(property.getInfo(), attributeType.getInfo())) {
					setInfo(attributeType, property.getInfo());
				}
				importObject.setSystemObject(attributeType);
			}
			else {
				throw new IllegalStateException("Dieser AttributTyp " + configurationAttributeType.getClass().getName() + " wird noch nicht unterstützt.");
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
			// evtl. muss der Datensatz gelöscht werden
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
	 * Vervollständigt den Attribut-Typen um die fehlenden konfigurierenden Datensätze. Momentan ist nur der Referenz-Attribut-Typ davon betroffen.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Attribut-Typen enthält
	 *
	 * @throws ConfigurationChangeException Falls der Attribut-Typ nicht vervollständigt werden konnte (Mengen und Datensätze).
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
			final String errorMessage = "Der AttributTyp " + property.toString() + " konnte nicht vollständig erstellt werden";
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
		// Default-Wert überprüfen
		if(_objectDiffs.isDefaultDifferent(attributeType, property.getDefault())) {
			setDefault(attributeType, property.getDefault());
		}
	}

	/**
	 * Speichert die Eigenschaften des Zeichenketten-AttributTyps als konfigurierenden Datensatz.
	 *
	 * @param attributeType       Zeichenketten-AttributTyp, an dem die Eigenschaften gespeichert werden sollen
	 * @param configurationString Objekt, welches die Eigenschaften des AttributTyps enthält
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setStringAttributeTypeProperties(StringAttributeType attributeType, ConfigurationString configurationString)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.zeichenkettenAttributTypEigenschaften");
		Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		data.getUnscaledValue("länge").set(configurationString.getLength());
		String encoding = configurationString.getStringEncoding();
		if("ISO-8859-1".equals(encoding)) {
			data.getUnscaledValue("kodierung").set(StringAttributeType.ISO_8859_1);
		}
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Erstellt den Wertebereich einer Ganzzahl, oder überarbeitet ein bestehendes Bereichs-Objekt und gibt es zurück.
	 *
	 * @param configurationArea Konfigurationsbereich, dem der Wertebereich hinzugefügt werden soll
	 * @param valueRange        umzuwandelnder Wertebereich
	 * @param integerValueRange bestehender Wertebereich
	 *
	 * @return SystemObject des Wertebereichs
	 *
	 * @throws ConfigurationChangeException Falls der Wertebereich nicht vollständig erstellt werden konnte.
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
		// Info überprüfen
		if(_objectDiffs.isInfoDifferent(valueRange.getInfo(), integerValueRange.getInfo())) {
			setInfo(integerValueRange, valueRange.getInfo());
		}
		// Eigenschaften überprüfen
		if(_objectDiffs.isIntegerAttributeTypeValueRangePropertiesDifferent(valueRange, integerValueRange)) {
			setIntegerValueRangeProperties(integerValueRange, valueRange);
		}
		return integerValueRange;
	}

	/**
	 * Speichert die Eigenschaften des Ganzzahl-Wertebereichs als konfigurierenden Datensatz.
	 *
	 * @param range      der Ganzzahl-Wertebereich
	 * @param valueRange Objekt, welches die zu speichernden Eigenschaften enthält
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
	 * @param state      Objekt, welches die zu speichernden Eigenschaften enthält
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
	 * Speichert die Eigenschaften einer Fließkommazahl als konfigurierenden Datensatz.
	 *
	 * @param attributeType Fließkommazahl-AttributTyp
	 * @param def           Objekt, welches die zu speichernden Eigenschaften enthält
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
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + def.getAccuracy() + "' wird beim Import nicht unterstützt.");
		}
		data.getTextValue("einheit").setText(def.getUnit());
		data.getUnscaledValue("genauigkeit").set(accuracy);
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften eines Zeit-Attribut-Typs als konfigurierenden Datensatz.
	 *
	 * @param attributeType Zeit-Attribut-Typ
	 * @param stamp         Objekt, welches die zu speichernden Eigenschaften enthält
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
				throw new IllegalStateException("Dieser Genauigkeitstyp '" + stamp.getAccuracy() + "' wird beim Import nicht unterstützt");
		}
		data.getUnscaledValue("relativ").set((stamp.getRelative() ? 1 : 0));
		data.getUnscaledValue("genauigkeit").set(accuracy);
		attributeType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die Eigenschaften eines Referenz-Attribut-Typs als konfigurierenden Datensatz.
	 *
	 * @param attributeType Referenz-Attribut-Typ
	 * @param reference     Objekt, welches die zu speichernden Eigenschaften enthält
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
				throw new IllegalStateException("Diese Referenzierungsart '" + reference.getReferenceType() + "' wird nicht vom Import unterstützt.");
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
	 * Erstellt aus einem Property-Objekt (welches einem Eintrag in der Versorgungsdatei entspricht) eine Attributgruppe oder verändert eine bestehende
	 * Attributgruppe so, dass es der Import-Definition entspricht und gibt diese zurück.
	 *
	 * @param importObject Objekt, welches die Daten für den Import einer Attributgruppe enthält
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
						throw new IllegalStateException("Das verwendete Datenmodell unterstützt keine Transaktionen");
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
			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), attributeGroup.getName())) {
				attributeGroup.setName(property.getName());
			}
			// Info überprüfen
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
	 * Vervollständigt eine Attributgruppe um die fehlenden konfigurierenden Datensätze.
	 *
	 * @param importObject Objekt, welches die Daten für den Import einer Attributgruppe enthält
	 *
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollständigt werden konnte (Mengen und Datensätze).
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
			final String errorMessage = "Die Attributgruppe '" + property.getPid() + "' konnte nicht vollständig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Vervollständigt eine Transaktionsattributgruppe
	 * @param transaction Transaktionsattributgruppe
	 * @param transactionProperties Eigenschaften
	 * @throws ConfigurationChangeException Falls das Objekt nicht vervollständigt werden konnte (Mengen und Datensätze).
	 */
	private void setTransactionProperties(
			final AttributeGroup transaction, final TransactionProperties transactionProperties)
			throws ConfigurationChangeException {
		final AttributeGroup atg = _dataModel.getAttributeGroup("atg.transaktionsEigenschaften");
		final Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		setDids(data.getArray("akzeptiert"), transactionProperties.getPossibleDids());
		setDids(data.getArray("benötigt"), transactionProperties.getRequiredDids());
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
	 * berücksichtigt, ob die Attributgruppe parametrierend ist, oder nicht. Die neu erstellte Menge wird an der angegebenen Attributgruppe gespeichert.
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

		// enthält alle ATGVs, die in der nächsten Version aktiv sein sollen
		final Collection<AttributeGroupUsage> nextCurrentAtgUsageList = new ArrayList<AttributeGroupUsage>();

		// für jeden Aspekt wird eine Attributgruppenverwendung erstellt, wenn sie noch nicht vorhanden ist
		final ConfigurationObjectType type = (ConfigurationObjectType)_dataModel.getType(Pid.Type.ATTRIBUTE_GROUP_USAGE);
		final String atgvStr = "atgv." + atg.getPid() + ".";	// erster Teil der Pid einer Attributgruppenverwendung

		// handelt es sich um eine parametrierbare Attributgruppe, dann dürfen folgende drei Aspekte nicht fehlen:
		if(property.isParameter()) {
			// Aspekt "ParameterSoll" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterSoll");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// prüfen, ob auch die ATGV korrekt ist.
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
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzufügen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
			// Aspekt "ParameterVorgabe" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterVorgabe");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// prüfen, ob auch die ATGV korrekt ist.
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
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzufügen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
			// Aspekt "ParameterIst" wird verarbeitet
			{
				final Aspect asp = _dataModel.getAspect("asp.parameterIst");
				AttributeGroupUsage attributeGroupUsage = atgUsageMap.get(asp);
				if(attributeGroupUsage != null) {
					// prüfen, ob auch die ATGV korrekt ist.
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
					atgUsageSet.add(attributeGroupUsage);	// ATGV hinzufügen
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
					// prüfen, ob auch die ATGV korrekt ist.
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
					atgUsageSet.add(attributeGroupUsage); // ATGV hinzufügen
				}
				nextCurrentAtgUsageList.add(attributeGroupUsage);
			}
		}

		// alle explizit angegebene Aspekte werden abgearbeitet
		for(ConfigurationAspect configurationAspect : property.getConfigurationAspect()) {
			final String aspPid = configurationAspect.getPid();
			if(property.isParameter() && (aspPid.equals("asp.parameterSoll") || aspPid.equals("asp.parameterVorgabe") || aspPid.equals("asp.parameterIst")
			                              || aspPid.equals("asp.parameterDefault"))) {
				// diese Aspekte ignorieren, wenn es sich um eine Parameter-Atg handelt - die Aspekte werden durch die Methode isParameter() geprüft
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
					// prüfen, ob die ATGV korrekt ist.
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
					// konfigurierende Datensätze hinzufügen
					setInfo(atgUsage, configurationAspect.getInfo());
					setAttributeGroupUsageProperties(atgUsage, atg, asp, configurationAspect.getUsage(), true);
					nextCurrentAtgUsageList.add(atgUsage);
					atgUsageSet.add(atgUsage);
				}
			}
		}

		if(property.getConfiguring()) {
			// Attributgruppenverwendung asp.eigenschaften hinzufügen, falls sie noch nicht vorhanden ist und sie die einzige Verwendung ist und das Attribut
			// konfigurierend ist.
			if(atgUsageSet.getElementsInModifiableVersion().isEmpty()) {
				// ATGV asp.eigenschaften hinzufügen
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

		// alte nicht mehr verwendete ATGVs löschen
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
				throw new IllegalStateException("Diese AttributgruppenVerwendung '" + usage + "' wird beim Import noch nicht unterstützt.");
		}
		data.getUnscaledValue("DatensatzVerwendung").set(usageNumber);
		atgUsage.setConfigurationData(atg, data);
	}

	/* ##################### ObjectSetType-Methoden ############################ */

	/**
	 * Erstellt aus einem Eigenschafts-Objekt einen Mengen-Typ oder verändert ein bestehenden Mengen-Typen so, dass er mit der Import-Definition übereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Mengen-Typs enthält
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
			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), objectSetType.getName())) {
				objectSetType.setName(property.getName());
			}
			// Info überprüfen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), objectSetType.getInfo())) {
				setInfo(objectSetType, property.getInfo());
			}
			// Mengen-Typ-Eigenschaften überprüfen
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
	 * Vervollständigt den Mengen-Typ um noch fehlende konfigurierenden Datensätze.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Mengen-Typs enthält
	 *
	 * @throws ConfigurationChangeException Falls der Mengen-Typ nicht vervollständigt werden konnte (Mengen und Datensätze).
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
			final String errorMessage = "Der MengenTyp '" + property.getPid() + "' konnte nicht vollständig erstellt werden";
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Speichert die Eigenschaften eines Mengen-Typs als konfigurierenden Datensatz am Mengen-Typ ab.
	 *
	 * @param objectSetType der Mengen-Typ
	 * @param property      das Eigenschafts-Objekt, welches die Eigenschaften des Mengen-Typs enthält
	 *
	 * @throws ConfigurationChangeException Falls der konfigurierende Datensatz nicht am Objekt gespeichert werden konnte.
	 */
	private void setObjectSetTypeProperties(ObjectSetType objectSetType, ObjectSetTypeProperties property) throws ConfigurationChangeException {
		// Überprüfung, ob ein dynamischer MengenTyp als Referenzierungsart 'Assoziation' erhält.
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
		data.getUnscaledValue("änderbar").set((property.isMutable() ? 1 : 0));
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
			_debug.finer("Menge der ObjektTypen für den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' wurde erstellt.");
		}
		else {
			setSystemObjectKeeping(nonMutableSet);
		}

		// Elemente der Menge ermitteln
		final Set<SystemObjectType> objectTypes = new LinkedHashSet<SystemObjectType>();
		for(String element : elements) {
			// Element ist entweder im Datenmodell oder wurde just importiert
			final SystemObject object = getObject(element);
			if(object == null) throwNoObjectException(element);
			objectTypes.add((SystemObjectType)object);
		}

		// Menge überprüfen
		final List<SystemObject> elementsInVersion = nonMutableSet.getElementsInVersion(objectSetType.getConfigurationArea().getModifiableVersion());
		// Erst alle überflüssigen entfernen.
		for(SystemObject systemObject : elementsInVersion) {
			SystemObjectType systemObjectType = (SystemObjectType)systemObject;
			if(!objectTypes.contains(systemObjectType)) {
				nonMutableSet.remove(systemObjectType);
			}
		}
		// Jetzt neue hinzufügen
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
	 * @throws ConfigurationChangeException Falls die Menge der Mengendefinition nicht hinzugefügt werden konnten.
	 */
	private void setObjectSetTypeSuperTypeSet(ConfigurationArea configurationArea, ObjectSetType objectSetType) throws ConfigurationChangeException {
		NonMutableSet superTypeSet = objectSetType.getNonMutableSet("SuperTypen");
		if(superTypeSet == null) {
			// Mengen erstellen und dem MengenTyp hinzufügen
			final ConfigurationObjectType superTypeType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.OBJECT_TYPES);
			superTypeSet = (NonMutableSet)configurationArea.createConfigurationObject(superTypeType, "", "SuperTypen", null);
			objectSetType.addSet(superTypeSet);
			_debug.finer("Menge der SuperTypen wurde für den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
		}
		else {
			setSystemObjectKeeping(superTypeSet);
		}

		// je nachdem, ob die Menge änderbar ist oder nicht, muss der richtige SuperTyp ausgewählt werden
		// änderbar = typ.dynamischeMenge || nicht änderbar = typ.konfigurationsMenge
		final SystemObjectType superType = objectSetType.isMutable() ? _dataModel.getType(Pid.Type.MUTABLE_SET) : _dataModel.getType(Pid.Type.NON_MUTABLE_SET);
		final List<SystemObject> superTypeSetElements = superTypeSet.getElementsInVersion(objectSetType.getConfigurationArea().getModifiableVersion());
		if(!superTypeSetElements.contains(superType)) {
			// Supertyp eintragen, wenn noch nicht enthalten
			superTypeSet.add(superType);
		}

		// evtl. vorhandene andere Supertypen müssen entfernt werden.
		for(SystemObject object : superTypeSetElements) {
			if(!object.equals(superType)) superTypeSet.remove(object);
		}
	}

	/**
	 * Die Mengen 'Attributgruppen' und 'Mengen' müssen an einer Mengendefinition vorhanden sein, obwohl in der Versorgungsdatei keine Elemente vorhanden sind.
	 *
	 * @param configurationArea Konfigurationsbereich der Mengendefinition
	 * @param objectSetType     Mengendefinition
	 *
	 * @throws ConfigurationChangeException Falls die Mengen der Mengendefinition nicht hinzugefügt werden konnten.
	 */
	private void setObjectSetTypeEmptySets(ConfigurationArea configurationArea, ObjectSetType objectSetType) throws ConfigurationChangeException {
		NonMutableSet atgSet = objectSetType.getNonMutableSet("Attributgruppen");
		if(atgSet == null) {
			// Menge erstellen
			final ConfigurationObjectType atgType = (ConfigurationObjectType)_dataModel.getType(Pid.SetType.ATTRIBUTEGROUPS);
			atgSet = (NonMutableSet)configurationArea.createConfigurationObject(atgType, "", "Attributgruppen", null);
			objectSetType.addSet(atgSet);
			_debug.finer("Menge der Attributgruppen wurde für den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
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
			_debug.finer("Menge der Mengenverwendungen wurde für den Mengen-Typ mit der Pid '" + objectSetType.getPid() + "' angelegt.");
		}
		else {
			setSystemObjectKeeping(setUsesSet);
		}
	}

	/* ##################### SystemObjectType-Methoden ############################ */

	/**
	 * Erstellt aus einem Eigenschafts-Objekt einen Objekt-Typen oder verändert einen bestehenden Objekt-Typen so, dass er mit der Import-Definition
	 * übereinstimmt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Objekt-Typs enthält
	 *
	 * @throws ConfigurationChangeException Falls der Objekt-Typ nicht importiert werden konnte.
	 */
	private void handleSystemObjectType(ImportObject importObject) throws ConfigurationChangeException {
		final SystemObjectTypeProperties property = (SystemObjectTypeProperties)importObject.getProperties();
		try {
			// prüfen, ob die Pid des Objekt-Typen bereits eingetragen wurde, wenn ja, dann wurde eine Schleife erkannt, d.h. eine fehlerhafte Versorgung in
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
				// es ist kein BasisTyp -> herausbekommen, was für ein Typ es ist
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
				// falls der Typ des bestehenden Typs nicht mit dem neuen Typ der Properties übereinstimmt, muss ein neuer Objekt-Typ erstellt werden
				if(type != systemObjectType.getType()) {
					_debug.finer(
							"Der Typ des Typs " + systemObjectType.getPid() + " hat sich geänder von " + systemObjectType.getType().getPid() + " zu "
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

					// Typ muss jetzt schon auf ungültig gesetzt werden, damit ein neuer Typ mit gleicher Pid erstellt werden kann
					// in Bearbeitung erstellte Objekte werden direkt gelöscht.
					
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

			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), systemObjectType.getName())) {
				systemObjectType.setName(property.getName());
			}
			// Info überprüfen
			if(_objectDiffs.isInfoDifferent(property.getInfo(), systemObjectType.getInfo())) {
				setInfo(systemObjectType, property.getInfo());
			}
			// Eigenschaften überprüfen
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
	 * Vervollständigt den Objekt-Typ um fehlende konfigurierende Datensätze, die bei der Erstellung des Objekts noch nicht berücksichtigt wurden.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines Objekt-Typs enthält
	 *
	 * @throws ConfigurationChangeException Falls der Objekt-Typ nicht vervollständigt werden konnte (Mengen und Datensätze).
	 */
	private void completeSystemObjectType(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final SystemObjectType systemObjectType = (SystemObjectType)importObject.getSystemObject();
		final SystemObjectTypeProperties property = (SystemObjectTypeProperties)importObject.getProperties();
		try {
			setSystemObjectTypeAttributeGroups(configurationArea, systemObjectType, property.getAtgAndSet());
			// Default-Parameter-Datensätze hinzufügen
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
			final String errorMessage = "Der Objekt-Typ '" + property.getPid() + "' konnte nicht vollständig erstellt werden. Grund: " + ex.getMessage();
			_debug.error(errorMessage);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Speichert die Eigenschaften des Objekt-Typs als konfigurierenden Datensatz ab.
	 *
	 * @param objectType      der Objekt-Typ
	 * @param isConfigurating gibt an, ob der Objekt-Typ konfigurierend ist
	 * @param isNamePermanent gibt an, ob der Name des Objekt-Typs permanent, also nicht änderbar, ist
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
	 * Diese Methode prüft, ob die Super-Typen alle dynamisch oder konfigurierend sind. Sind die Super-Typen unterschiedlich, so liegt ein Versorgungsfehler vor
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
	 * Diese Methode ermittelt anhand der Super-Typen den gemeinsamen Persistenz-Modus. Dieser ist für alle Super-Typen gleich.
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
	 * Erstellt eine Menge mit Super-Typen für einen Objekt-Typen oder verändert eine bestehende Menge.
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
		// Erst alle überflüssigen Elemente entfernen
		for(SystemObject systemObject : elementsInVersion) {
			SystemObjectType objectType = (SystemObjectType)systemObject;
			if(!superTypes.contains(objectType)) {
				superTypeSet.remove(objectType);
			}
		}
		// Elemente neu hinzufügen
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
				throw new IllegalStateException("Persistenzmodus hat einen ungültigen Wert: " + persistenceMode);
		}
		data.getUnscaledValue("persistenzModus").set(mode);
		dynamicObjectType.setConfigurationData(atg, data);
	}

	/**
	 * Speichert die direkten Attributgruppen und Mengenverwendungen als Mengen am Objekt-Typ ab.
	 *
	 * @param configurationArea der Konfigurationsbereich des Objekt-Typs
	 * @param systemObjectType  der Objekt-Typ
	 * @param atgAndSet         Array, welches die Attributgruppen und Mengenverwendungen enthält
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
			_debug.finer("Menge der Attributgruppen für den Objekt-Typ mit der Pid '" + systemObjectType.getPid() + "' wurde erstellt.");
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
			_debug.finer("Menge der Mengenverwendungen für den Objekt-Typ mit der Pid '" + systemObjectType.getPid() + "' wurde erstellt.");
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
					// neues Objekt erhält keine Pid und keinen Namen (wird nicht benötigt)
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
					// Mengen-Typ der Elemente und ob die Menge erforderlich ist überprüfen
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
		// überflüssige Elemente entfernen
		for(SystemObject systemObject : elementsInAtgSet) {
			AttributeGroup attributeGroup = (AttributeGroup)systemObject;
			if(!directAttributeGroups.contains(attributeGroup)) {
				atgSet.remove(attributeGroup);
			}
		}
		// Elemente hinzufügen
		for(AttributeGroup attributeGroup : directAttributeGroups) {
			if(!elementsInAtgSet.contains(attributeGroup)) {
				atgSet.add(attributeGroup);
			}
		}

		if(systemObjectType.isConfigurating()) {
			// Menge der Mengenverwendungen
			// überflüssige Elemente entfernen
			for(SystemObject systemObject : elementsInObjectSetUsesSet) {
				ObjectSetUse use = (ObjectSetUse)systemObject;
				if(!directObjectSetUses.contains(use)) {
					objectSetUsesSet.remove(use);
				}
			}
			// Elemente hinzufügen
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
	 * @param configurationSet Objekt, welches die Eigenschaften der Mengenverwendung enthält
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
	 * Erstellt aus einem Eigenschafts-Objekt ein System-Objekt oder verändert ein bereits bestehendes System-Objekt.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines System-Objekts enthält
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
				// es gibt bereits ein Objekt - prüfen, ob der Typ übereinstimmt - wenn nicht, dann muss ein neues Objekt angelegt werden.
				if(systemObject.getType() != systemObjectType) {
					// handelt es sich um ein in Bearbeitung erstelltes Objekt, dann muss es aus der EditingObjects-Map entfernt werden.
					final Collection<CheckedObject> objects = _editingObjects.get(importObject.getConfigurationArea());
					for(CheckedObject object : objects) {
						if(object.getSystemObject() == systemObject) {
							objects.remove(object);
							break;
						}
					}

					// Objekt muss jetzt schon auf ungültig gesetzt werden, damit ein neues Objekt mit gleicher Pid erstellt werden kann
					// in Bearbeitung erstellte Objekte werden direkt gelöscht.
					
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
					// falls Datensätze am dynamischen Objekt gespeichert werden sollen, müssen diese schon hier berücksichtigt werden
					final ConfigurationConfigurationObject configurationObjectProperties = (ConfigurationConfigurationObject)property;
					final List<DataAndATGUsageInformation> dataAndATGUsageInformation = new LinkedList<DataAndATGUsageInformation>();
					for(ConfigurationObjectElements configurationObjectElements : configurationObjectProperties.getDatasetAndObjectSet()) {
						if(configurationObjectElements instanceof ConfigurationDataset) {
							ConfigurationDataset configurationDataset = (ConfigurationDataset)configurationObjectElements;
							// prüfen, ob es die Atg und den Asp gibt. Es müssen aktuelle Objekte sein.
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
							// alle notwendigen und optional nicht änderbaren Datensätze werden hier gespeichert.
							if(atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData) {
//							if(atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData
//							   || atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData) {
								final Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
								data.setToDefault();
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
					// das Objekt sofort gültig würde, ohne einen aktuellen Objekt-Typen.
					final DynamicObjectType dynamicObjectType = (DynamicObjectType)_dataModel.getObject(property.getType());
					if(dynamicObjectType != null) {
						_debug.finer("Dynamisches Objekt mit pid '" + property.getPid() + "' wird erstellt. Datensätze", dataAndATGUsageInformation);
						systemObject = importObject.getConfigurationArea().createDynamicObject(
								dynamicObjectType, property.getPid(), property.getName(), dataAndATGUsageInformation
						);
						_debug.finer("Dynamisches Objekt mit der Pid '" + property.getPid() + "' wurde erstellt.");
					}
					else {
						final String errorMessage = "Das dynamische Objekt mit der Pid '" + property.getPid() + "' konnte nicht erzeugt werden, "
						                            + "da der Typ des Objekts erst in der nächsten Version des Konfigurationsbereichs aktiviert wird.";
						_debug.error(errorMessage);
						throw new IllegalStateException(errorMessage);
					}
				}
			}
			// Namen überprüfen
			if(_objectDiffs.isNameDifferent(property.getName(), systemObject.getName())) {
				systemObject.setName(property.getName());
			}

			// Info überprüfen
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
	 * System-Objekte erhalten in dieser Methode ihre konfigurierenden Datensätze und Mengen.
	 *
	 * @param importObject Objekt, welches die Daten für den Import eines System-Objekts enthält
	 *
	 * @throws ConfigurationChangeException Falls das System-Objekt nicht vervollständigt werden konnte.
	 */
	private void completeSystemObject(ImportObject importObject) throws ConfigurationChangeException {
		final ConfigurationArea configurationArea = importObject.getConfigurationArea();
		final SystemObject systemObject = importObject.getSystemObject();
		final SystemObjectType objectType = systemObject.getType();
		final ConfigurationConfigurationObject property = (ConfigurationConfigurationObject)importObject.getProperties();
		try {
			// Set in der alle verwendeten ATGV gespeichert werden, damit alle Datensätze gelöscht werden können, die nicht vorhanden sein dürfen.
			final Set<AttributeGroupUsage> usedAtgUsages = new HashSet<AttributeGroupUsage>();

			// Datensätze und Mengen werden gespeichert
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

					// Merken, zu welchen ATGV Datensätze existieren sollen
					usedAtgUsages.add(atgUsage);
					// bei dynamischen Objekten und notwendig und nicht änderbaren DS nachfolgenden Part überspringen
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
							data.setToDefault();
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

					// dynamische Objekte können keine Mengen haben
					if(!objectType.isConfigurating()) {
						// es ist ein dynamisches Objekt!
						final String errorMessage = "Dynamische Objekte können keine Mengen haben. Es sind aber welche angegeben.";
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
							errorMessage.append(systemObject.getPidOrNameOrId()).append("' unterstützt die Menge '").append(configurationObjectSet.getName());
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


							// Typ überprüfen
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
							// Menge dem Objekt hinzufügen
							configObject.addSet(objectSet);
							_debug.finer(
									"Menge " + configurationObjectSet.getName() + " dem SystemObjekt mit der Pid '" + systemObject.getPid() + "' hinzugefügt."
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
											"Zugriff auf Verwaltungsinformationen von dynamischen Mengen nicht möglich, da die eingesetzte Version des Bereichs"
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

						// prüfen, ob die hinzuzufügenden Elemente in der Menge erlaubt sind (wird beim hinzufügen der Elemente überprüft)
						Set<SystemObject> setElements = new LinkedHashSet<SystemObject>();
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
						// überflüssige Elemente entfernen
						for(SystemObject object : elementsInSet) {
							if(!setElements.contains(object)) {
								objectSet.remove(object);
							}
						}
						// Elemente hinzufügen
						for(SystemObject object : setElements) {
							if(!elementsInSet.contains(object)) {
								objectSet.add(object);
							}
						}
					}
					else {
						// Menge muss nicht geändert werden -> aber es wird beibehalten
						setSystemObjectKeeping(objectSet);
					}
				}
			}

			// nicht mehr benötigte Datensätze entweder gelöscht oder auf null gesetzt, je nachdem, ob das Objekt in Bearbeitung ist
			// oder bereits freigegeben/aktiviert wurde
			if(objectType.isConfigurating()) {
				final Collection<AttributeGroupUsage> usedAttributeGroupUsages = systemObject.getUsedAttributeGroupUsages();
				for(AttributeGroupUsage atgUsage : usedAttributeGroupUsages) {
					if(!usedAtgUsages.contains(atgUsage) && !ComparePropertiesWithSystemObjects.isHiddenInExport(atgUsage)) {
						// die ATGV wurde nicht verwendet und entspricht nicht atg.info oder atg.konfigurationsVerantwortlicherLaufendeNummer -> Datensatz löschen
						// bei einem in Bearbeitung befindlichen Objekt werden die Datensätze richtig gelöscht.
						if(getEditingObject(systemObject.getConfigurationArea(), systemObject.getPid()) != null) {
							((ConfigSystemObject)systemObject).removeConfigurationData(atgUsage);
						}
						else {
							// bei einem zuvor freigegebenen Objekt werden die Datensätze nur auf null gesetzt.
							((ConfigSystemObject)systemObject).createConfigurationData(atgUsage, null);
						}
					}
				}
			}
			else {
				// es handelt sich um ein dynamisches Objekt - notwendige DS werden hier nicht betrachtet (sie dürfen nicht gelöscht werden)
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
						if(!usedAtgUsages.contains(atgUsage) && !ComparePropertiesWithSystemObjects.isHiddenInExport(atgUsage)) {
							// die ATGV wurde nicht verwendet -> muss also gelöscht werden
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
				// Default-Parameter-Datensätze hinzufügen
				if(_objectDiffs.isDefaultParameterDifferent(property.getDefaultParameters(), systemObject)) {
					setDefaultParameterDataset(property.getDefaultParameters(), systemObject);
				}
			}
		}
		catch(Exception ex) {
			final String errorMessage = "Das System-Objekt mit der Pid '" + property.getPid() + "' konnte nicht vollständig erstellt werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	private void setDefaultParameterDataset(final ConfigurationDefaultParameter[] defaultParameters, final SystemObject systemObject)
			throws IOException, ConfigurationChangeException {
		final AttributeGroup attributeGroup = (AttributeGroup)getObject("atg.defaultParameterdatensätze");
		final Data data;
		if(defaultParameters.length > 0) {
			// den Datensatz komplett neu schreiben
			data = AttributeBaseValueDataFactory.createAdapter(attributeGroup, AttributeHelper.getAttributesValues(attributeGroup));
			final Data.Array array = data.getArray("Default-Parameterdatensatz");
			array.setLength(defaultParameters.length);

			// Elemente füllen
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
							"Default-Parameter-Datensatz am '" + systemObject.getPidOrNameOrId() + "' konnte nicht importiert werden, weil ein ungültiger Typ '"
							+ pidType + "' angegeben wurde."
					);
				}
				final SystemObjectType referencedType = (SystemObjectType)typeObject;
				item.getReferenceValue("typ").setSystemObject(referencedType);

				final SystemObject atgObject = getObject(defaultParameter.getPidAtg());
				if(!(atgObject instanceof AttributeGroup)) {
					throw new ConfigurationChangeException(
							"Default-Parameter-Datensatz am '" + systemObject.getPidOrNameOrId()
							+ "' konnte nicht importiert werden, weil eine ungültige Attributgruppe '" + defaultParameter.getPidAtg() + "' angegeben wurde."
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
							+ "' ist nicht vollständig definiert."
					);
				}
				// aus dem Data ein byte-Array machen
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final Serializer serializer;
				try {
					serializer = SerializingFactory.createSerializer(3, out);
				}
				catch(NoSuchVersionException e) {
					throw new IOException("Serialisierer konnte nicht in der gewünschten Version erstellt werden");
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
			// Keine Default-Parameterdatensätze in der Versorgungsdatei vorhanden führt zum Löschen des Datensatzes
			data = null;
		}
		// Datensatz speichern
		systemObject.setConfigurationData(attributeGroup, data);
	}

	/**
	 * Diese Methode ermittelt anhand eines Objekt-Typen seine sämtlichen Mengenverwendungen.
	 *
	 * @param systemObjectType der Objekt-Typ
	 *
	 * @return Alle Mengenverwendungen dieses Objekt-Typs.
	 */
	Collection<ObjectSetUse> getObjectSetUses(final SystemObjectType systemObjectType) {
		final Collection<ObjectSetUse> objectSetUses = new HashSet<ObjectSetUse>();
		if(_allImportedConfigurationAreas.contains(systemObjectType.getConfigurationArea())) {
			// Typ wird gerade importiert -> die in Bearbeitung befindlichen Mengenverwendungen betrachten
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Mengen").getElementsInModifiableVersion();
			for(SystemObject systemObject : setUses) {
				final ObjectSetUse setUse = (ObjectSetUse)systemObject;
				objectSetUses.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
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
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElementsInVersion(version);
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				objectSetUses.addAll(getObjectSetUses(objectType));
			}
		}
		return Collections.unmodifiableCollection(objectSetUses);
	}

	/**
	 * Gibt alle Konfigurationsbereiche zurück, die gerade importiert werden.
	 *
	 * @return Alle Konfigurationsbereiche, die gerade importiert werden.
	 */
	Set<ConfigurationArea> getAllImportedConfigurationAreas() {
		return _allImportedConfigurationAreas;
	}

	/**
	 * Ein neues Data-Objekt wird mit den Elementen aus der Definition gefüllt.
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
				// Größe des Arrays beachten
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
	 * Gibt zu einem Konfigurationsbereich und einer Pid das zur Übernahme oder Aktivierung freigegebene Objekt zurück, wenn es existiert.
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
	 * Gibt zu einem Konfigurationsbereich und einer Pid das aktuelle Objekt zurück, wenn es existiert.
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
	 * Löscht aus der Liste der aktuellen Objekte eines Bereichs ein gegebenes Objekt, wenn es enthalten ist.
	 *
	 * @param configurationArea Der zu durchsuchende Konfigurationsbereich
	 * @param object Das zu löschende Objekt
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
	 * Gibt zu einem Konfigurationsbereich und einer Pid das in Bearbeitung befindliche Objekt zurück, wenn es existiert.
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
	 * Diese Methode gibt anhand der angegebenen Pid ein Objekt aus dem Datenmodell oder aus den Import-Versorgungsdateien zurück.
	 *
	 * @param pid Pid des gesuchten Objekts
	 *
	 * @return das gewünschte Objekt oder eine Exception, falls dies nicht existiert
	 */
	public SystemObject getObject(String pid) {
		// Erst die Import-Objekte durchforsten, damit auf jeden Fall auch neu erzeugte Objekte berücksichtigt werden
		SystemObject systemObject = null;

		// gibt es zu dieser Pid ein importiertes Objekt?
		final ImportObject importObject = _importMap.get(pid);
		if(importObject != null && importObject.getSystemObject() != null) {
			systemObject = importObject.getSystemObject();
		}
		else {
			// es gibt kein importiertes Objekt
			systemObject = getUsingObjects().get(pid);
			// wenn Objekt nicht gefunden wurde, wird geprüft, ob in der aktuellen Konfiguration ein Objekt mit der gewünschten Pid aktiviert ist
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
	 * Ermittelt zu allen Konfigurationsbereichen, die Objekte, die in der zu betrachtenden Version gültig sind.
	 *
	 * @return die Objekte, die in den zu betrachtenden Versionen gültig sind
	 */
	private Map<String, SystemObject> getUsingObjects() {
		if(_viewingObjects == null) {
			_viewingObjects = new HashMap<String, SystemObject>();
			// Version der Konfigurationsbereiche berücksichtigen, in der das Objekt sein soll!
			for(Map.Entry<ConfigurationArea, Short> entry : _usingVersionOfConfigurationArea.entrySet()) {
				final ConfigurationArea configurationArea = entry.getKey();
				// alle Bereiche erst in der aktuellen Version betrachten
				// erst die aktuellen Objekte holen ...
				final Collection<SystemObject> currentObjects = configurationArea.getCurrentObjects();

				// Konfigurationsbereich selber muss auch hinzugefügt werden
				_viewingObjects.put(configurationArea.getPid(), configurationArea);

				// anschließend alle aktuellen Objekte des Bereichs
				for(SystemObject object : currentObjects) {
					final String objPid = object.getPid();
					if(!objPid.equals("")) {
						if(object.getType() instanceof DynamicObjectType) {
							_viewingObjects.put(objPid, object);
						}
						else {
							// nur die aktuellen Objekte, die auch noch in der zu betrachtenden Version gültig sind
							final ConfigurationObject configObject = (ConfigurationObject)object;
							if(configObject.getNotValidSince() == 0 || configObject.getNotValidSince() > entry.getValue()) {
								_viewingObjects.put(objPid, object);
							}
						}
					}
				}

				// ... dann die neuesten Objekte holen und die aktuellen überschreiben lassen
				if(configurationArea.getActiveVersion() != entry.getValue()) {
					final Collection<SystemObject> newObjects = configurationArea.getNewObjects();
					for(SystemObject object : newObjects) {
						// Ist das Objekt in der richtigen Version gültig?
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
	 * Erstellt einen Objekt-Typen mit der angegebenen Pid, falls es nicht bereits existiert. Wird zur Ermittlung der Super-Typen benötigt.
	 *
	 * @param pid Pid des gesuchten Objekt-Typs
	 *
	 * @return der gewünschte Objekt-Typ
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
			// wenn der Info-Datensatz bereits existiert, kann dieser geändert werden
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
		// die Liste der zur Übernahme / Aktivierung freigegebenen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _newObjects.keySet()) {
			for(CheckedObject checkedObject : _newObjects.get(configurationArea)) {
				if(checkedObject.getSystemObject() == systemObject) {
					_debug.finer(
							"Ein zur Übernahme / Aktivierung freigegebenes Objekt '" + systemObject.getPidOrNameOrId() + "' wurde gefunden und wird markiert."
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

	/** Setzt alle Markierung, ob ein Objekt beibehalten werden soll, wieder zurück. */
	private void unsetSystemObjectKeeping() {
		// die Liste der in Bearbeitung befindlichen Objekte wird durchgearbeitet
		for(ConfigurationArea configurationArea : _editingObjects.keySet()) {
			for(CheckedObject checkedObject : _editingObjects.get(configurationArea)) {
				checkedObject.setObjectKeeping(false);
			}
		}
		// die Liste der zur Übernahme / Aktivierung freigegebenen Objekte wird durchgearbeitet
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
	 * Gibt die Attributgruppenverwendung zurück, die in der in Bearbeitung befindlichen Version gültig ist.
	 *
	 * @param atg die Attributgruppe
	 * @param asp der Aspekt
	 *
	 * @return Die Attributgruppenverwendung, die in der in Bearbeitung befindlichen Version gültig ist oder <code>null</code>, falls es keine gültige Verwendung
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
	 * Diese Klasse wird nur für den Import benutzt. Sie speichert eine Referenz auf ein {@link de.bsvrz.puk.config.xmlFile.properties.SystemObjectProperties} und auf ein {@link SystemObject}. Damit
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
		 * Gibt den Konfigurationsbereich dieses Import-Objekts zurück.
		 *
		 * @return der Konfigurationsbereich dieses Import-Objekts
		 */
		public ConfigurationArea getConfigurationArea() {
			return _configurationArea;
		}

		/**
		 * Gibt das Eigenschafts-Objekt dieses Import-Objekts zurück, welches die Definition eines Objekts aus der Versorgungsdatei enthält.
		 *
		 * @return das Eigenschafts-Objekt
		 */
		public SystemObjectProperties getProperties() {
			return _properties;
		}

		/**
		 * Gibt das zum Eigenschafts-Objekt gehörende System-Objekt zurück.
		 *
		 * @return das zum Eigenschafts-Objekt gehörende System-Objekt
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
	 * Diese Klasse speichert zu einem bereits bestehenden Objekt, ob es im Konfigurationsbereich beibehalten werden soll, oder nicht. Für diese Unterscheidung
	 * gibt es ein Flag. Wird es nicht gesetzt, dann wird das Objekt im letzten Schritt des Imports auf {@link de.bsvrz.dav.daf.main.config.SystemObject#invalidate()
	 * ungültig} gesetzt.
	 */
	private class CheckedObject {

		/** Ein bestehendes System-Objekt. */
		private SystemObject _systemObject;

		/** Ob nach Abschluss des Import-Vorgangs dieses System-Objekt noch benötigt wird oder es auf ungültig gesetzt wird. */
		private boolean _isObjectKeeping = false;

		/**
		 * Speicher ein System-Objekt ab, welches nach dem Import überprüft werden soll, ob es noch benötigt wird.
		 *
		 * @param systemObject ein System-Objekt
		 */
		public CheckedObject(SystemObject systemObject) {
			_systemObject = systemObject;
		}

		/**
		 * Gibt das System-Objekt zurück.
		 *
		 * @return das System-Objekt
		 */
		public SystemObject getSystemObject() {
			return _systemObject;
		}

		/**
		 * Gibt zurück, ob das System-Objekt auch nach dem Import beibehalten werden soll.
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
