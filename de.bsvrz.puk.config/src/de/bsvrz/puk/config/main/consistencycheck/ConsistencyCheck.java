/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.consistencycheck;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.util.AttributeValues;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResult;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntry;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntryType;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.puk.config.configFile.datamodel.AreaDependencyCheck;
import de.bsvrz.puk.config.configFile.datamodel.AreaDependencyChecker;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.ConfigSystemObject;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaDependency;
import de.bsvrz.puk.config.configFile.datamodel.ConfigurationAreaDependencyKind;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaTime;
import de.bsvrz.puk.config.main.dataview.VersionedView;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Die Klasse übernimmt die Konsistenzprüfung, wie sie in TPuK1-138,139,140,141 gefordert wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConsistencyCheck {

	/** Flag zum Umschalten des Verhaltens beim Import für TestModelChanges **/
	public static boolean ALLOW_SPECIAL_CONFIG_CHANGES_FOR_TEST = false;

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	private final ConfigAreaAndVersion[] _consistencyCheckDefinition;

	/** Datenmodell für die übergebene Bereiche */
	private final ConfigDataModel _dataModel;

	/** Hilfsobjekt, das bei Zugriffen auf Konfigurationsdaten die Konfigurationsbereiche in vorgegebenen Versionen betrachtet */
	private final VersionedView _versionedView;

	/**
	 * Speichert zu einem Konfigurationsbereich die Version, mit der der Bereich aktiviert werden soll. Kann der Bereich in der Map nicht gefunden werden, wird der
	 * Bereich in der Version, in der er derzeit läuft, weiter laufen. Als Schlüssel dient der Konfigurationsbereich, als Ergebnis wird die Version, mit der der
	 * Konfigurationsbereich in der Zukunft laufen soll, zurückgegeben.
	 * <p>
	 * Anmerkung: Es werden nur die Bereiche eingetragen, die auch im Konstruktor übergeben wurden.
	 */
	private final Map<ConfigurationArea, Short> _areaVersionMap = new HashMap<ConfigurationArea, Short>();

	/**
	 * Speichert zu einem Konfigurationsbereich die Abhängigkeiten zu anderen Konfigurationsbereichen.
	 * <p>
	 * Key = Bereich, für den Abhängigkeiten gefunden wurden.
	 * <p>
	 * Value = Menge mit allen gefundenen Abhängigkeiten
	 */
	private final Map<ConfigurationArea, Set<ConfigurationAreaDependency>> _areasDependencies = Collections.synchronizedMap(new HashMap<ConfigurationArea, Set<ConfigurationAreaDependency>>());

	/** Objekt-ID des lokalen Konfigurationsverantwortlichen oder 0, falls der lokale Konfigurationsverantwortliche nicht bestimmt werden kann */
	private long _configurationAuthorityId = 0;

	/**
	 * Flag, das festlegt, ob die Abhängigkeiten zwischen Bereichen geschrieben und geprüft werden sollen. Es gesetzt wird, wenn das Metamodell in Version 9 oder
	 * höher vorliegt.
	 */
	private boolean _storeDependencies = false;

	/**
	 * Erstellt ein Objekt, das vorgegebene Konfigurationsbereiche einer Konsistenzprüfung unterzieht.
	 *
	 * @param consistencyCheckDefinition PidŽs aller Konfigurationsbereiche, die in einer neuen Version geprüft werden sollen. Zu jedem Konfigurationsbereich ist
	 *                                   ausserdem die Version gespeichert, die aktiviert werden soll. Ist die Version 0, so wird die größte zu verwendene Version
	 *                                   gesucht. Die ModifiableVersion darf nur dann berücksichtigt werden, wenn es auch Elemente gibt die in der
	 *                                   ModifiableVersion geändert werden würden. Die anderen Bereiche, die nicht übergeben wurden, werden in der aktuellen
	 *                                   Version geprüft.
	 * @param dataModel                  Datenmodell, mit dem die übergebenen Bereich geprüft werden sollen
	 */
	public ConsistencyCheck(ConfigAreaAndVersion[] consistencyCheckDefinition, ConfigDataModel dataModel) {
		_consistencyCheckDefinition = consistencyCheckDefinition;

		_dataModel = dataModel;
		final ConfigurationAuthority authority = _dataModel.getConfigurationAuthority();
		if(authority != null) {
			_configurationAuthorityId = authority.getId();
		}

		// Abhängigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		final ConfigurationArea metaModelArea = _dataModel.getConfigurationArea("kb.metaModellGlobal");
		if(metaModelArea != null && metaModelArea.getActiveVersion() >= 9) _storeDependencies = true;

		// Speichert zu den Bereichen die neue Version, in der der Bereich laufen soll.
		// Ist die Version des Bereichs 0, so wird die größte zu verwendene Version gesucht.
		// Die ModifiableVersion darf nur dann berücksichtigt werden, wenn es auch Elemente gibt die
		// in der ModifiableVersion gültig werden würden. bzw. gelöscht würden.

		for(ConfigAreaAndVersion checkDefinition : consistencyCheckDefinition) {
//			if(checkDefinition.getVersion() > 0) {
			_areaVersionMap.put(checkDefinition.getConfigArea(), checkDefinition.getVersion());
//			}
//			else {
//				// Version 0, also eine Version wählen
//				final ConfigConfigurationArea configArea = (ConfigConfigurationArea)checkDefinition.getConfigArea();
//				_areaVersionMap.put(configArea, configArea.getLastVersionObjectModified());
//			}
		}
		_versionedView = new VersionedView(dataModel, _areaVersionMap);
	}

	/**
	 * Diese Methode führt eine Konsistenzprüfung für alle(aktive, im Konstruktor übergebene, nur in den Verwaltungsinformationen) Konfigurationsbereiche durch.
	 * <p>
	 * Die Version mit der der Bereich geprüft wird, wurde entweder im Konstruktor übergeben oder ist die aktuelle Version des Bereichs.
	 * <p>
	 * Die Methode blockiert, bis ein Ergebnis vorliegt.
	 *
	 * @param kindOfConsistencyCheck Bestimmt wie mit Abhängigkeiten zwischen Konfigurationsbereichen umgegangen wird. Bei einer einfachen Konsistenzprüfung werden
	 *                               die Abhängigkeiten zwischen den Bereichen zwar erkannt, aber nicht mittels Dätensätzen am Bereich gespeichert. Bei einer
	 *                               Freigabe zur Übernahme (die auch Interferenzfehler verzeiht) werden wiedrum Abhängigkeiten gespeichert, die bei einer lokalen
	 *                               Aktivierung nicht gespeichert werden würden.
	 *
	 * @return Objekt, das das Ergebnis der Konsistenzprüfung enthält und im Fehlerfall die unterschiedlichen Fehlermeldungen zurückgibt.
	 */
	public ConsistencyCheckResultInterface startConsistencyCheck(final KindOfConsistencyCheck kindOfConsistencyCheck) {
		// Speichert Fehler und Warnungen, die aufgetreten sein können
		final ConsistencyCheckResult result = new ConsistencyCheckResult();
		try{
			if(_storeDependencies) {
				// Prüfung Abhängigkeiten der zu betrachtenden Bereiche
				final AreaDependencyChecker dependencyChecker = new AreaDependencyChecker();
				final List<ConfigAreaAndVersion> checkedAreaVersions = new ArrayList(Arrays.asList(_consistencyCheckDefinition));
				final Set<ConfigurationArea> exlicitSpecifiedAreas = new HashSet();
				for(ConfigAreaAndVersion configAreaAndVersion : _consistencyCheckDefinition) {
					exlicitSpecifiedAreas.add(configAreaAndVersion.getConfigArea());
				}
				for(ConfigurationArea implicitSpecifiedArea : ((ConfigDataModel)_dataModel).getAllConfigurationAreas().values()) {
					if(!exlicitSpecifiedAreas.contains(implicitSpecifiedArea)) {
						short version = implicitSpecifiedArea.getActiveVersion();
						checkedAreaVersions.add(new ConfigAreaAndVersion(implicitSpecifiedArea, version));
					}
				}
				final AreaDependencyCheck.AreaDependencyCheckResult dependencyCheckResult = dependencyChecker.checkAreas(checkedAreaVersions);
				try {
					dependencyChecker.printAndVerifyAreaDependencyCheckResult(dependencyCheckResult);
				}
				catch(IllegalStateException e) {
					final String message = "Fehler bei der erneuten Prüfung der Abhängigkeiten zwischen Konfigurationsbereichen in den durch die "
					                       + "Konsistenzprüfung zu betrachtenden Versionen";
					if(_dataModel.getIgnoreDependencyErrorsInConsistencyCheck()) {
						_debug.warning(message, e);
					}
					else {
						_debug.error(message, e);
					}
				}

				if(!_dataModel.getIgnoreDependencyErrorsInConsistencyCheck()) {
					final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredOptionalErrors = dependencyCheckResult.getOptionalDependencyErrors();
					if(occuredOptionalErrors.isEmpty() == false) {
						// Es sind einige Abhängigkeiten zwischen den Bereichen nicht erfüllt. Allerdings sind diese Abhängigkeiten optional und führen deshalb zu einer
						// Warnung.
						final Set<ConfigurationArea> areas = occuredOptionalErrors.keySet();
						// Alle Bereich, deren Abhängigkeiten nicht erfüllt sind (optionale Abhängigkeit)

						for(ConfigurationArea area : areas) {
							final List<ConfigurationAreaDependency> dependencies = occuredOptionalErrors.get(area);

							final StringBuffer text = new StringBuffer();
							text.append(
									"Der Bereich " + area.getPid()
									+ " besitzt folgende optionale Abhängigkeiten zu anderen Bereichen:"
									+ "\n"
							);

							for(ConfigurationAreaDependency dependency : dependencies) {
								text.append(dependency.toString());
								text.append("\n");
							}
							ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.WARNING,
									area,
									new SystemObject[]{},
									text.toString()
							);
							result.addEntry(entry);
						}// alle optionalen Abhängigkeiten
					}

					// Alle Abhängigkeiten, die benötigt werden, aber nicht vorhanden sind.
					final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredNeededDependencyErrors = dependencyCheckResult.getNeededDependencyErrors();
					if(occuredNeededDependencyErrors.isEmpty() == false) {
						// Es fehlen Bereiche, die gebraucht werden. Bereiche ausgeben und eine Exception werfen, da in diesem Fall die Konfiguration nicht gestartet werden
						// darf.

						final Set<ConfigurationArea> areas = occuredNeededDependencyErrors.keySet();

						for(ConfigurationArea area : areas) {
							final StringBuffer text = new StringBuffer();
							text.append(
									"Der Bereich " + area.getPid() + " benötigt folgende Bereiche in den angegebenen Versionen:" + "\n"
							);

							final List<ConfigurationAreaDependency> list = occuredNeededDependencyErrors.get(area);
							for(ConfigurationAreaDependency dependency : list) {
								text.append(dependency.toString());
								text.append("\n");
							}
							ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.LOCAL_ERROR,
									area,
									new SystemObject[]{},
									text.toString()
							);
							result.addEntry(entry);
						}// alle benötigen Bereiche
						return result;
					}
				}

			}

			final SystemObject hierarchyDefinitionTypeObject = _versionedView.getObject("typ.hierarchieDefinition");
			final Set<SystemObjectType> hierarchyDefinitionTypes = new HashSet<SystemObjectType>();
			if(hierarchyDefinitionTypeObject instanceof SystemObjectType) {
				SystemObjectType hierarchyDefinitionType = (SystemObjectType)hierarchyDefinitionTypeObject;
				hierarchyDefinitionTypes.add(hierarchyDefinitionType);
				
			}
			// Alle Bereiche anfordern. Es müssen alle Bereiche in der jeweiligen Version (entweder aktuell oder wie im
			// Konstruktor gefordert) geprüft werden

			// Zählt der wievielete Bereiche gerade geprüft wird. Dies dient nur für die Ausgabe
			int configAreaCounter = 1;
			final int numberOfAreas = _dataModel.getAllConfigurationAreas().values().size();

			// Diese Map speichert zu jeder Pid das dazugehörige Konfigurationsobjekt und zwar über alle Konfigurationsbereiche.
			// Als Key dient die Pid, als Value wird das Objekt mit der entsprechenden Pid zurückgegeben.
			// Mit dieser Map kann erkannt werden, ob es zu einem Objekt bereits ein zweites Objekt mit identischer Pid gibt.
			final Map<String, SystemObject> pidsFromAllAreas = new HashMap<String, SystemObject>();

			// Speicher alle Ids aller aktiven Objekten. (Aktiv: Das Objekt ist in der zu prüfenden Version des jeweilgen Bereichs aktiv)
			// Als Schlüssel dient die Id des Objekt. Als Value wird eine Liste zurückgegeben mit allen Objekten, die die gleiche Id haben.
			final Map<Long, List<SystemObject>> idsFromAllAreas = new HashMap<Long, List<SystemObject>>();

			final SystemObject defaultParameterAtg = _versionedView.getObject("atg.defaultParameterdatensätze");
			if(!(defaultParameterAtg instanceof AttributeGroup)) {
				throw new IllegalStateException(
						"atg.defaultParameterdatensätze wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
				);
			}
			final SystemObject defaultParameterAspect = _versionedView.getObject("asp.eigenschaften");
			if(!(defaultParameterAspect instanceof Aspect)) {
				throw new IllegalStateException(
						"atg.defaultParameterdatensätze wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
				);
			}
			final AttributeGroupUsage defaultParameterUsage = ((AttributeGroup)defaultParameterAtg).getAttributeGroupUsage((Aspect)defaultParameterAspect);

			final Collection<ConfigurationObject> hierarchyObjects = new ArrayList<ConfigurationObject>();

			for(ConfigurationArea verifyingConfigArea : ((ConfigDataModel)_dataModel).getAllConfigurationAreas().values()) {

				final short lastActiveVersion = verifyingConfigArea.getActiveVersion();
				// Speichert alle Objekte, die zu einen "zu prüfenden" Konfigurationsbreich gehören (aktuelle und zukünftig aktuelle)
				final Collection<SystemObject> configAreaObjects = new ArrayList<SystemObject>();
				// aktuelle Objekte des zu prüfenden Bereichs anfordern
				configAreaObjects.addAll(verifyingConfigArea.getCurrentObjects());
				// zukünftig aktuelle Objekte des Bereichs anfordern
				configAreaObjects.addAll(verifyingConfigArea.getNewObjects());

				_debug.info(
						"Prüfe Bereich " + configAreaCounter + " von insgesamt " + numberOfAreas + " Bereichen. Bereich, der geprüft wird: '"
						+ verifyingConfigArea.getPidOrNameOrId() + "' Anzahl Objekte des Bereichs: " + configAreaObjects.size() + " Anzahl bisheriger Fehler: "
						+ (result.getInterferenceErrors().size() + result.getLocalErrors().size()) + " Anzahl bisheriger Warnungen: " + result.getWarnings().size()
				);
				configAreaCounter++;

				// Version, mit der geprüft werden soll, ob alle Objekte des Bereichs konsistent sind
				final short verifyingVersion = getActiveVersion(verifyingConfigArea);

				// Diese Liste speichert alle Objekte, die in der geforderten Version des Konfigurationsbreichs gültig sind/sein werden und alle gültigen dynamischen Objekte.
				// Diese Liste ermöglicht es, bei den späteren Prüfungen auf lokale Fehler nur die Objekte zu betrachten,
				// die sich wirklich geändert haben können.
				// Es werden auch die aktuellen Objekte geprüft, die gültig bleiben. Dies ist nötig, weil sich Referenzen dieser Objekte geändert haben können.

				final Collection<SystemObject> checkObjects = new ArrayList<SystemObject>();

				//**********************************************************************************************************

				// Prüfung 1:
				// Die Pid eines Objekts muss eindeutig in einer Version sein.
				// Falls ein ConfigurationObject in der altuellen Version gültig ist und es gibt in der zu prüfenden Version
				// ebenfalls ein neues Objekt, das in der zu prüfenden Version gültig werden würde, so muss ein Fehler ausgegeben werden.
				// In diesem Fall würden 2 Objekte mit einer Pid gültig sein.

				// Speichert zu jeder Pid alle Objekte der Pid. Key = Pid(String) Value = Liste, die alle Objekte mit
				// der Pid enthält (es werden nur Objekte gespeichert, die derzeit aktuell sind oder in der zu prüfenden Version aktuell werden).
				// Die Liste ist nötig, weil die Pid als hashCode(integer 4 Byte) in der Map benutzt wird. Der String kann aber 64 Zeichen haben, es kann also
				// sehr leicht zu Kollisionen kommen.
				final Map<String, List<SystemObject>> pidMap = new HashMap<String, List<SystemObject>>();

				/**
				 * Diese Map speichert zu jeder Komponenten im Sinne von TPuK1-29 Komposition ab, wo diese Komponente bereits benutzt wurde. Jede Komponente darf nur
				 * einmal durch ein übergeordnetes Objekt verwendet werden verwendet werden.
				 *
				 * Als Key dient die Komponente. Als Value wird eine Liste zurückgegeben, die alle Objekte beinhaltet, in denen die Komponente bereits verwendet wurde.
				 * Wurde die Komponente richtig verwendet, so
				 */
				final Map<SystemObject, List<SystemObject>> usedComponent = new HashMap<SystemObject, List<SystemObject>>();

//			System.out.println("Anzahl zu prüfender Objekte: " + configAreaObjects.size());

				// Alle Objekte(jetzt gültig und alle die in der zukunft gültig werden) des Konfigurationsbereichs betrachten

				int counter2 = 1;

				for(SystemObject configAreaObject : configAreaObjects) {

					if(counter2 % 100 == 0) {
//					_debug.info("Prüfe Objekt " + counter2 + " von " + configAreaObjects.size());
					}
					counter2++;

					// Wird true, wenn die Pid des Objekts geprüft werden muss
					boolean checkPid = false;

					if(configAreaObject instanceof ConfigurationObject) {

						final ConfigurationObject configurationObject = (ConfigurationObject)configAreaObject;

						// Es werden nur Objekte betrachtetet, die gültig sind oder mit der zu prüfenden Version
						// gültig würden. Damit bleiben nur noch Objekte, die mit einer Version nach der zu prüfenden Version
						// gültig werden würden (also noch weiter in der Zukunft liegen). Diese werden nicht betrachtet, da
						// sie für die zu prüfende Version keine Rolle spielen (sie werden mit der zu prüfenden Version nicht
						// aktiviert, somit ist es egal, ob sie Konsistent sind. Sollen diese Objekte geprüft werden, so
						// müßte mit der Version der Objekte die Konsistenzprüfung gestartet werden).

						final short validSince = configurationObject.getValidSince();
						final short notValidSince = configurationObject.getNotValidSince();

						// Prüfungen, die für alle (in der zu betrachtenden Version) gültigen Konfigurationsobjekte durchgeführt werden:
						if(validSince <= verifyingVersion && (notValidSince == 0 || verifyingVersion < notValidSince)) {
							checkPid = true;
							checkObjects.add(configurationObject);

							checkDoublePidsInDifferntAreas(configurationObject, pidsFromAllAreas, result);
							checkDoubleIdsInDifferentAreas(configurationObject, idsFromAllAreas, result);

							if(configurationObject instanceof AttributeSet) {
								// Attributgruppen oder Attributlisten
								AttributeSet attributeSet = (AttributeSet)configurationObject;
								final List<Attribute> attributes = attributeSet.getAttributes();
								for(Attribute attribute : attributes) {
									final AttributeType attributeType = attribute.getAttributeType();
									if(attributeType instanceof AttributeListDefinition) continue;
									String defaultValue = attribute.getDefaultAttributeValue();
									if(defaultValue == null) {
										defaultValue = attributeType.getDefaultAttributeValue();
									}
									if(defaultValue == null) {
										// Kein Defaultwert vorhanden, also Undefined-Wert prüfen
										if(!AttributeValues.hasUndefinedValue(attributeType)) {
											ConsistencyCheckResultEntry localError = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													configurationObject.getConfigurationArea(),
													new SystemObject[]{configurationObject, attribute, attributeType},
													"Am Attribut oder Attributtyp sollte ein Defaultwert festgelegt werden, weil zum Attributtyp keinen Undefined-Wert bestimmt werden kann: "
											);
											result.addEntry(localError);
										}
									}
									else {
										try {
											AttributeValues.checkValue(attributeType, defaultValue, _versionedView);
										}
										catch(Exception e) {
											ConsistencyCheckResultEntry localError = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													configurationObject.getConfigurationArea(),
													new SystemObject[]{configurationObject, attribute, attributeType},
													"Der Defaultwert '" + defaultValue + " ist nicht interpretierbar: " + e.getMessage()
											);
											result.addEntry(localError);
										}
									}
								}
							}
							else if(configurationObject instanceof AttributeType) {
								AttributeType attributeType = (AttributeType)configurationObject;
								String defaultValue = attributeType.getDefaultAttributeValue();
								if(defaultValue != null) {
									try {
										AttributeValues.checkValue(attributeType, defaultValue, _versionedView);
									}
									catch(Exception e) {
										ConsistencyCheckResultEntry localError = new ConsistencyCheckResultEntry(
												ConsistencyCheckResultEntryType.LOCAL_ERROR,
												configurationObject.getConfigurationArea(),
												new SystemObject[]{configurationObject, attributeType},
												"Der Defaultwert '" + defaultValue + " ist nicht interpretierbar: " + e.getMessage()
										);
										result.addEntry(localError);
									}
								}
							}
						}
					}
					else {
//					final DynamicObject dynamicObject = (DynamicObject) configAreaObject;
						// dynamische Objekte sind sofort gültig und auch sofort ungültig, sie sind nicht an
						// Versionen gebunden.
						// Sie müssen also immer geprüft werden, wenn sie noch gültig sind.
						checkObjects.add(configAreaObject);
						checkPid = true;
						checkDoublePidsInDifferntAreas(configAreaObject, pidsFromAllAreas, result);
						checkDoubleIdsInDifferentAreas(configAreaObject, idsFromAllAreas, result);
					}

					if(checkPid && !configAreaObject.getPid().equals("")) {
						// In die Map der Pids eintragen, gibt es dort bereits einen Eintrag für diese Pid, dann wurde
						// ein lokaler Fehler gefunden.
						synchronized(pidMap) {
							List<SystemObject> elementsWithPid = pidMap.get(configAreaObject.getPid());
							if(elementsWithPid == null) {
								// Es gibt noch keine Liste, also Liste erzeugen, Element eintragen.
								// Wenn kein Fehler auftritt, sollte das Array immer die Größe 1 besitzen.
								// Es wird 2 vorgegeben, damit nicht sofort beim ersten Eintrag in das Array, ein
								// Arracopy angestossen wird.
								elementsWithPid = new ArrayList<SystemObject>(2);
								elementsWithPid.add(configAreaObject);
								// Liste in Map eintragen
								pidMap.put(configAreaObject.getPid(), elementsWithPid);
							}
							else {
								// Es gibt bereits Elemente in der Liste, es müssen die Pids verglichen werden.
								// Sind die Pids gleich, wurde ein Fehler gefunden.
								for(SystemObject systemObjectPidList : elementsWithPid) {
									if(systemObjectPidList.getPid().equals(configAreaObject.getPid())) {
										StringBuffer errorText = createLocalErrorMessagePartOne(
												verifyingConfigArea, "Objekte mit identischer Pid in Version " + verifyingVersion + " aktiv"
										);
										errorText.append(
												"Betroffene Pid: " + systemObjectPidList.getPid() + " .Betroffenes Objekt: " + systemObjectPidList
												+ " Betroffenes Objekt: " + configAreaObject
										);

										ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
												ConsistencyCheckResultEntryType.LOCAL_ERROR,
												verifyingConfigArea,
												new SystemObject[]{systemObjectPidList, configAreaObject},
												"Mehrere Objekte mit identischer Pid in der gleichen Version aktiv, Version " + verifyingVersion
										);
										result.addEntry(entry);
									}
								}
								// Das geprüfte Objekt wird in jedem Fall in die Liste aufgenommen
								// um Folgefehler anzuzeigen
								elementsWithPid.add(configAreaObject);
							}
						}
					}
				}

				//**********************************************************************************************************

				// Alle folgenden Prüfungen müssen auf jedes Element der Liste "checkObjects" durchgeführt werden
				for(SystemObject systemObject : checkObjects) {

					// Prüfung, ob der Typ des Objekts in der zu prüfenden Version gültig ist.
					final SystemObjectType objectTypeOfVerifiedObject = systemObject.getType();
					if(!_versionedView.isValid(objectTypeOfVerifiedObject)) {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
								verifyingConfigArea,
								new SystemObject[]{systemObject, objectTypeOfVerifiedObject},
								"Der Typ eines (in der zu prüfenden Version) gültigen Objekts ist (in der zu prüfenden Version) nicht gültig."
						);
						result.addEntry(entry);
					}

					// Wenn der Typ sich in einem anderen Bereich befindet als der zu prüfende Typ, wurde eine Abhängigkeit gefunden.
					checkDependency(systemObject, objectTypeOfVerifiedObject, ConfigurationAreaDependencyKind.REQUIRED);

					//**********************************************************************************************************
					// Prüfung 2:
					// a) Zu jeder Menge an einem Objekt gibt es beim Typ des Objekts eine entsprechende Mengenverwendung.
					// b) Jede Menge, die in der Mengenverwendung gefordert ist, muss auch am Objekt gefunden werden.
					// c) Paßt die Anzahl der Elemente in der Menge
					// d) Sind die Elemente in der Menge vom richtigen Typ
					// e) Ist das Objekt der Menge eine Objektreferenz(Komposition), so muss geprüft werden, ob sich das referenzierte Objekt im Konfigurattionsbereich befindet (Dies ist nur Teil 1 der Prüfung, da auch Objekte andere Objekt referenzieren können)
					// f) Wenn ein Element der Menge eine Komposition ist, so darf dieses Element nur von der einen Menge referenziert werden
					// g) Nachtrag: Da sich das Element der Menge in einem anderen Bereich befinden kann, entstehen dadurch Abhängigkeiten. Diese müssen erkannt
					// und gegebenenfalls gespeichert werden.

					if(systemObject instanceof ConfigurationObject) {
						// Mengen des Objekts
						final List<ObjectSet> objectSets = ((ConfigurationObject)systemObject).getObjectSets();

						// Mengenverwendungen des Types, die zu dem gerade betrachteten Objekt gehört

						// Die Mengenverwendung des Typs muss nicht in der aktuelle Version aktiv sein, sondern kann erst mit der Version aktiviert
						// werden, die gerade geprüft wird. Aus diesem Grund muss ein "getElementsInVersion" auf eine Menge benutzt werden.
						final List<ObjectSetUse> objectSetUses = getObjectSetUses(objectTypeOfVerifiedObject);

//					System.out.println("Objekt " + systemObject.getPid() + " Alle Mengenverwendungen des Typs: " + objectSetUses);

						// Diese Liste speichert alle Mengenverwendungen, die einer Menge am Objekt zugeordnet werden konnten.
						// Diese Objekte müssen nicht noch einmal betrachtet werden
						final Set<ObjectSetUse> objectSetUsesFound = Collections.synchronizedSet(new HashSet<ObjectSetUse>());

						// Jede Menge am Objekt muss in der Definition gefunden werden und umgekehrt. Das entspricht a) und b)
						for(final ObjectSet objectSet : objectSets) {
							// Menge sollte in der zu prüfenden Version auch gültig sein
							final boolean objectSetIsValid = _versionedView.isValid(objectSet);
							if(!objectSetIsValid) {
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject, objectSet},
										"An einem (in der zu prüfenden Version) gültigen Objekt gibt es eine (in der zu prüfenden Version) nicht gültige Menge."
								);
								result.addEntry(entry);
							}

							//getType
//						System.out.println("");
//						System.out.println("	 Menge am Objekt: " + objectSet.getName() + " Typ: " + objectSet.getObjectSetType());
							// wird true, wenn zu der Menge am Objekt, eine Mengenverwendung gefunden wurde, die die Menge ebenfalls enthält
							boolean objectSetUseFound = false;
							for(ObjectSetUse objectSetUse : objectSetUses) {
//							System.out.println("	Mengenverwendung: " + objectSetUse.getObjectSetName() + " Typ: " + objectSetUse.getObjectSetType());

								if((objectSetUse.getObjectSetType().equals(objectSet.getObjectSetType()))
								   && (objectSetUse.getObjectSetName().equals(objectSet.getName()))) {
									// Gegenstück gefunden
									objectSetUsesFound.add(objectSetUse);

									// e) beachten. Wenn der Mengentyp Komposition ist, dann muss jedes Element der Menge
									// im Konfigurationsbereich enthalten sein.
									final boolean composition;
									if(objectSetUse.getObjectSetType().getReferenceType() == ReferenceType.COMPOSITION) {
										composition = true;
									}
									else {
										composition = false;
									}

									// Zur Menge kann eine entsprechende Mengeverwendung gefunden werden, stimmen die Elemente der Menge ? Die entspricht d)

									// Alle Elemente der Menge in Abhängigkeit vom Typ der Menge (Mutable, NonMutable)
									final List<SystemObject> elementsOfSet;

									if(objectSetUse.getObjectSetType().isMutable()) {
										// Eine dynamische Menge, diese kann sich jederzeit ändern. Also nur die aktuellen anfordern.
										elementsOfSet = objectSet.getElements();
									}
									else {
										// Bei NonMutableSets darf sich die Mengenzusammenstellung zur Laufzeit nicht ändern.
										// Es ist egal ob Assoziation oder Komposition/... ist.
										final NonMutableSet nonMutableSet = (NonMutableSet)objectSet;
										// Es muss die Version benutzt werden, in der die Menge aktiv ist bzw. in der sie aktiv gesetzt werden soll
										elementsOfSet = nonMutableSet.getElementsInVersion(getActiveVersion(nonMutableSet));

										// Die Elemente von gültige Mengen müssen gültige Objekte sein
										if(objectSetIsValid) {
											for(SystemObject object : elementsOfSet) {
												if(!_versionedView.isValid(object)) {
													final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
															ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
															verifyingConfigArea,
															new SystemObject[]{systemObject, objectSet, object},
															"An einer (in der zu prüfenden Version) gültigen Menge gibt es mindestens ein (in der zu prüfenden Version) nicht gültiges Objekt."
													);
													result.addEntry(entry);
													break;
												}
											}
										}
									}

//								System.out.println("Mengenverwendung Name: " + objectSetUse.getObjectSetName() + "Minimum: " + objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum: " + objectSetUse.getObjectSetType().getMaximumElementCount() + " Anzahl Elemente: " + elementsOfSet.size());

									// Paßt die Anzahl der Elemente? Dies entspricht c)
									if((objectSetUse.getObjectSetType().getMinimumElementCount() > elementsOfSet.size())) {
										// Es sind weniger Objekte in der Menge als vorgeschrieben -> Fehler

										// Unterscheiden, ob der maximale Wert gesetzt wurde. Dies wird für den Fehlertext benötigt
										if(objectSetUse.getObjectSetType().getMaximumElementCount() > 0) {
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet},
													"Eine Menge enthält nicht die geforderte Anzahl Elemente: minimum "
													+ objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum "
													+ objectSetUse.getObjectSetType().getMaximumElementCount() + " Anzahl Elemente in der Menge "
													+ elementsOfSet.size()
											);
											result.addEntry(entry);
										}
										else {
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet},
													"Eine Menge enthält nicht die geforderte Anzahl Elemente: minimum "
													+ objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum unbegrenzt"
													+ " Anzahl Elemente in der Menge " + elementsOfSet.size()
											);
											result.addEntry(entry);
										}
									}
									else {
										// Passt der maximale Wert
										if((objectSetUse.getObjectSetType().getMaximumElementCount() > 0) && (elementsOfSet.size()
										                                                                      > objectSetUse.getObjectSetType().getMaximumElementCount())) {
											// Es wurde ein maximaler Wert gesetzt (0 = unbegrenzt)
											// und
											// die Anzahl Elemente war größer als die maximal erlaubte Menge -> Fehler
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet},
													"Eine Menge enthält nicht die geforderte Anzahl Elemente: minimum "
													+ objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum "
													+ objectSetUse.getObjectSetType().getMaximumElementCount() + " Anzahl Elemente in der Menge "
													+ elementsOfSet.size()
											);
											result.addEntry(entry);
										}
									}

									// Passen die Elemente in der Menge mit ihrem Objekttyp zu den erlaubten Objekttypen der Menge.
									// Oder gibt es Elemente, die gar nicht in der Menge sein dürften? Dies entspricht d)

									// Alle Objekttypen, die als Elemente in der Menge vorhanden sein dürfen -> Diese Objekttypen müssen
									// in der Version aktiv sein, in der dieser Bereich aktiviert werden soll

									final NonMutableSet nonMutableSet = (NonMutableSet)objectSetUse.getObjectSetType().getObjectSet("ObjektTypen");

									// Müssen noch gecastet werden
									final List<SystemObject> objectTypes = nonMutableSet.getElementsInVersion(getActiveVersion(nonMutableSet));

									final List<SystemObjectType> requirededObjectTypes = new LinkedList<SystemObjectType>();

									for(SystemObject object : objectTypes) {
										if(object instanceof SystemObjectType) {
											final SystemObjectType systemObjectType = (SystemObjectType)object;
											requirededObjectTypes.add(systemObjectType);
										}
									}

									// Jedes Element der Menge besitzt einen Objekttyp, dieser muss in der Menge "requiredObjectTypes"
									// zu finden sein.

									for(SystemObject setElement : elementsOfSet) {

										// Fall g) (Auf Abhängigkeiten prüfen)
										if(setElement == null) {
											final ConsistencyCheckResultEntry entry = new ObjectSetEntryIsNull(verifyingConfigArea, systemObject, objectSet);
											result.addEntry(entry);
											continue;
										}
										else {
											checkDependency(objectSet, setElement, ConfigurationAreaDependencyKind.REQUIRED);
										}

										// wird true, wenn der Typ des Elements der Menge auch in der Menge verwendet werden darf
										boolean objectTypeFound = false;
										// Prüfen, ob das Element der Menge vom richtigen Typ ist
										for(SystemObjectType systemObjectType : requirededObjectTypes) {
											if(_versionedView.isOfType(setElement, systemObjectType)) {
												objectTypeFound = true;
												// Das Element hat die richtige Mengenverwendung. Ist die Mengenverwendung eine Komposition
												// so muss das Objekt auch im Konfigurationsbereich vorhanden sein. Dies entspricht e)
												if(composition) {
													if(!setElement.getConfigurationArea().equals(verifyingConfigArea)) {
														// Der Konfigurationsbereich des Objekts ist ein anderer als der
														// geprüfte Bereich. Also ist das Objekt dem geprüften Bereich
														// unbekannt -> Fehler, laut e)
														final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
																ConsistencyCheckResultEntryType.LOCAL_ERROR,
																verifyingConfigArea,
																new SystemObject[]{systemObject, setElement},
																"Das referenzierte Objekt einer Referenz(Komposition) befindet sich nicht im gleichen Konfigurationsbereich, wie der konfigurierende Datensatz, der die Referenz enthält"
														);
														result.addEntry(entry);
													}
													if(setElement instanceof DynamicObject) {
														// Der Konfigurationsbereich des Objekts ist ein anderer als der
														// geprüfte Bereich. Also ist das Objekt dem geprüften Bereich
														// unbekannt -> Fehler, laut e)
														final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
																ConsistencyCheckResultEntryType.LOCAL_ERROR,
																verifyingConfigArea,
																new SystemObject[]{systemObject, setElement},
																"Das referenzierte Objekt einer Referenz(Komposition) ist ein dynamisches Objekt"
														);
														result.addEntry(entry);
													}

													// f) prüfen: Das Element darf nur von dieser Menge referenziert werden
													checkDoubleComponentUsage(setElement, objectSet, usedComponent, result);
												}
												// Der Typ wurde gefunden
												break;
											}
										} // for über alle Typen, die in der Menge erlaubt sind

										if(!objectTypeFound) {
											// Der Typ des Elements der Menge ist nicht in der Mengeverwendung mit den erlaubten Objekttypen zu finden
											// (dieser Fall kann nicht vorkommen, da dies bereits beim hinzufügen in die Menge geprüft wird)
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet, setElement},
													"Ein Element der Menge besitzt einen Typ, der nicht in der Mengenverwendung aufgeführt ist"
											);
											result.addEntry(entry);
										}
									} // for über alle Elemente einer Menge

									// Es wurde eine Mengeverwendng gefunden (es kann aber trotzdem zu Fehlern gekomme sein,
									// die spielen aber für dieses true keine Rolle)
									objectSetUseFound = true;
									break;
								}
							} // for, alle Mengenverwendungen

							if(objectSetUseFound == false) {
								// Es konnte zu einer Menge keine Mengenverwendung gefunden werden
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.LOCAL_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject},
										"Zu einer Menge konnte keine Mengenverwendung gefunden werden"
								);
								result.addEntry(entry);
							}
						}

						// Es wurden alle Mengen eines Objekts geprüft, sind aber auch alle Mengen vorhanden, die in der Mengeverwendung
						// vorhanden sein müssen ? Dies entspricht b)
						// Zu jeder Mengenverwendung(falls gefordert) muss eine Menge am Objekt gefunden werden
						for(ObjectSetUse objectSetUse : objectSetUses) {
							// Es wurden bereits Objekte aus den Mengenverwendungen betrachtet. Diese wurden in einer Menge gespeichert
							// und müssen nicht noch einmal betrachtet werden.
							if(!objectSetUsesFound.contains(objectSetUse)) {
								// Diese Mengenverwendung wurde noch nicht bearbeitet
								if(objectSetUse.isRequired()) {

									if(systemObject instanceof DynamicObjectType && "menge.mengenVerwendungen".equals(objectSetUse.getObjectSetType().getPid())) {
										// Dieser Fall ist "ok", sobald das Datenmodell geändert wurde (menge nicht mehr erforderlich), kann
										// das If-Konstrukt raus
									}
									else {
										// Da jede Menge am Objekt bereits betrachtet wurde, darf es keine Mengenverwendung mehr geben
										// die nicht betrachtet wurde aber benötigt wird.
										final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
												ConsistencyCheckResultEntryType.LOCAL_ERROR,
												verifyingConfigArea,
												new SystemObject[]{systemObject, objectSetUse},
												"An einem Objekt fehlt eine Menge, die aber laut Mengenverwendung gefordert ist, Menge: "
												+ objectSetUse.getObjectSetName()
										);
										result.addEntry(entry);
									}
								}
							}
						}
					} //if(Konfigurationsobjekt)

					//**********************************************************************************************************

					//**********************************************************************************************************
					// Prüfung 3:
					// a) Alle Laut Attributgruppenverwendung notwendigen konfigurierenden Datensätze sind bei den Objekten vorhanden
					// b) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Komposition besitzt, müssen
					// alle konfigurierenden Datensätze geprüft werden, ob sie auf Objekte verweisen, die im Konfigurationsbereich vorhanden sind.
					// c) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Aggregation/Assoziation enthält, muss geprüft werden
					// ob das referenzierte Objekt dem Datenmodell bekannt ist, falls nicht, wurde ein Interferenzfehler gefunden.
					// d) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Aggregation/Assoziation enthält, muss geprüft werden
					// ob alle referenziereten Objekte, die nicht undefinierten Referenzen entsprechen vorhanden sind (siehe c).
					// e) Nachtrag: Alle benutzten ATG-Verwendungen am Objekt müssen auch laut Definition erlaubt sein
					// f) Wenn eine Referenz vom Typ "Komposition" ist, darf dieses Objekt nur von einem Datensatz referenziert werden (siehe b))
					// g) Wenn ein Objekt referenziert wird, dann muss das referenzierte Objekt den richtigen Typen besitzen (dieser wird an der Referenz festgelegt)
					// h) Die durch den Attributtyp festgelegten Attributwerte müssen eingehalten werden

					// Alle Attributgruppen des Objekts
//				final List<AttributeGroup> objectTypeATGs = systemObject.getType().getAttributeGroups();
//				final List<AttributeGroup> objectTypeATGs = systemObject.getType().getDirectAttributeGroups();

					final NonMutableSet attributeSet = (NonMutableSet)objectTypeOfVerifiedObject.getObjectSet("Attributgruppen");

					// Die Elemente der Menge anfordern. Die Menge kann in einem anderen Konfigurationsbereich liegen, also
					// muss geprüft werden, in welcher Version der Bereich mit der Menge läuft.
					// Aus dieser Version müssen dann die Elemente angefordert werden.
					final List<SystemObject> objectTypeATGs = getAttributeGroups(objectTypeOfVerifiedObject);

//				if(_areaVersionMap.containsKey(attributeSet.getConfigurationArea())) {
//					// Der Bereich mit der Menge soll in einer neuen Version laufen
//					objectTypeATGs = attributeSet.getElementsInVersion(_areaVersionMap.get(attributeSet.getConfigurationArea()));
//				}
//				else {
//					// Der Bereich läuft in der aktuellen Version weiter
//					objectTypeATGs = attributeSet.getElements();
//				}

					// Speichert alle ATG-Verwendungen, die an diesem Objekt benutzt wurden. Alle ATG-Verwendungen, die benutzt wurden,
					// müssen auch erlaubte ATG-Verwendungen sein. (3 e))
					final Collection<AttributeGroupUsage> usedAttributeGroupUsages = systemObject.getUsedAttributeGroupUsages();

					// Hier werden alle erlaubten ATG-Verwendungen gespeichert, diese Menge wird später mit <code>usedAttributeGroupUsages</code>
					// verglichen.
					final Set<AttributeGroupUsage> allowedATGUsages = new HashSet<AttributeGroupUsage>();

					// Jede ATG einzeln betrachten
					for(SystemObject systemObjectAttributeGroup : objectTypeATGs) {
						final AttributeGroup attributeGroup = (AttributeGroup)systemObjectAttributeGroup;

						// Alle Attributgruppenverwendungen, die in der zu prüfenden Version aktiv sind
						final NonMutableSet nonMutableSetHelper = attributeGroup.getNonMutableSet("AttributgruppenVerwendungen");
						final List<SystemObject> attributeGroupUsages = nonMutableSetHelper.getElementsInVersion(
								getActiveVersion(nonMutableSetHelper)
						);

						// Jede ATG-Verwendung betrachten und prüfen, ob konfigurierende Datensätze vorhanden sind (falls gefordert).
						// Gleichzeitg wird geprüft, ob jeder Datensatz des Objektes ebenfalls mit einer gültigen ATG-Verwendung, die am Objekt erlaubt ist,
						// abgedeckt ist.
						for(SystemObject object : attributeGroupUsages) {
							final AttributeGroupUsage atgUsage = (AttributeGroupUsage)object;
							// Für den späteren Vergleich speichern
							allowedATGUsages.add(atgUsage);

							// So den Datensatz anfragen, da über ATG+Aspekt die Attributverwendung nicht angefordert werden kann, bei der Methode, die den
							// Datensatz sucht.
							final Data dataSet = ((ConfigSystemObject)systemObject).getConfigurationData(atgUsage, _versionedView);

							// + " Typ des Objekts: " + systemObject.getType().getPid()
//						System.out.println("SystemObjekt: " + systemObject.getPid() + " Usage " + atgUsage.getUsage() + " ATG " + atgUsage.getAttributeGroup() + " Aspekt " + atgUsage.getAspect());

							// Wenn die Verwendung benötig wird, dann muss es einen konfigurierenden Datensatz am Objekt
							// geben. 3 a)
							if((atgUsage.getUsage() == AttributeGroupUsage.Usage.RequiredConfigurationData
							    || atgUsage.getUsage() == AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData) && (dataSet == null)) {
								// Es gibt keinen konfigurierenden Datensatz am Objekt, aber er muss vorhanden sein, also Fehler.
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.LOCAL_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject, atgUsage},
										"Ein konfigurierender Datensatz konnte nicht gefunden werden, obwohl dieser in der Attributgruppenverwendung gefordert wurde"
								);
								result.addEntry(entry);
							}

							// Falls Komposition oder Assoziation/Aggrigation im Spiel ist, muss geprüft werden ob das Referenzierte Objekt im Konfigurationsbereich oder in der Konfiguration
							// vorhanden ist. 3 b),c),d)
							if((dataSet != null)) {
								// 3 b),c),d),f),g) prüfen
								// Falls ein Datensatz eine Referenz enthält, wird geprüft ob die Referenz innerhalb des
								// geprüften Konfiguratonsbereich enthalten ist. Mögliche Fehler werden direkt
								// eingetragen.
								

								checkDataSetReferences(dataSet, null, verifyingConfigArea, _dataModel, result, systemObject, usedComponent);

								// h) prüfen
								if(!dataSet.isDefined()) {
									if(ignoreAttributeValueError(systemObject, atgUsage.getAttributeGroup(), atgUsage.getAspect(), dataSet) == false) {

										if(ALLOW_SPECIAL_CONFIG_CHANGES_FOR_TEST && verifyingConfigArea.getConfigurationAuthority().getPid().startsWith( "kv.dav.")) {
											// Für TestModelChanges zulassen
										}
										else {
											// Der Datensatz kann so nicht über den Datenverteiler verschickt werden -> Die Werte der Attribute sind nicht korrekt.
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, atgUsage.getAttributeGroup(), atgUsage.getAspect()},
													"Es sind nicht alle Attribute der Attributgruppe mit Werten versehen. Der Datensatz kann nicht über den Datenverteiler verschickt werden: "
													+ dataSet

											);
											result.addEntry(entry);
										}
									}
								}
							}
						} // for, über alle ATG-Verwendungen des Objekts
					}

					// Prüfen, ob auch alle Datensätze, die am Objekt gespeichert sind, auch dort wirklich gespeichert sein dürfen. (Fall e)
					for(AttributeGroupUsage usedAttributeGroupUsage : usedAttributeGroupUsages) {
//						if(systemObject.getType().getPid().equals("typ.benutzer")) {
//							System.out.println("############ usedAttributeGroupUsage = " + usedAttributeGroupUsage);
//							System.out.println("usedAttributeGroupUsage.getId() = " + usedAttributeGroupUsage.getId());
//							System.out.println("usedAttributeGroupUsage.getValidSince() = " + usedAttributeGroupUsage.getValidSince());
//							System.out.println("usedAttributeGroupUsage.getNotValidSince() = " + usedAttributeGroupUsage.getNotValidSince());
//						}
						if(!allowedATGUsages.contains(usedAttributeGroupUsage)) {
//							if(systemObject.getType().getPid().equals("typ.benutzer")) {
//								System.out.println("-> nicht gut");
//							}
							// Es wurde eine ATG-Verwendung gefunden, zu der ein Datensatz an dem Objekt gespeichert wurde,
							// aber diese ATG-Verwendung ist an diesem Objekt gar nicht zugelassen.
							final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
									verifyingConfigArea,
									new SystemObject[]{systemObject, usedAttributeGroupUsage},
									"An einem Objekt wurde ein konfigurierender Datensatz mit einer Attributgruppenverwendung gefunden, die für dieses Objekt nicht in der Menge der erlaubten Attributgruppenverwendungen eingetragen ist."
							);
							result.addEntry(entry);
						}
//						else {
//							if(systemObject.getType().getPid().equals("typ.benutzer")) {
//								System.out.println("-> ok");
//							}
//						}
					}

					// Prüfung 4:
					// a) Die Nummerierung von Attributen innerhalb von Attributgruppen/Listen über ihre Position muss bei eins
					// beginnen und fortlaufend sein.

					// Liste, die alle Attribute des Objekts enthält. Nur ATGŽs und Attributlisten werden geprüft, in
					// allen anderen Fällen ist die Liste <code>null</code>.
					final List<Attribute> attributes;
					if(systemObject instanceof AttributeGroup) {
						final AttributeGroup attributeGroup = (AttributeGroup)systemObject;
						attributes = attributeGroup.getAttributes();
					}
					else if(systemObject instanceof AttributeListDefinition) {
						final AttributeListDefinition attributeListDefinition = (AttributeListDefinition)systemObject;
						attributes = attributeListDefinition.getAttributes();
					}
					else {
						// Von diesem Objekt werden keine Attribute angefordert
						attributes = null;
					}

					if(attributes != null) {
						// Es müssen Attribute getestet werden. Das kleinste Attribut muss die Nummer 1 haben, dann müssen
						// die Werte fortlaufend sein.

						// Map, diese benutzt als Schlüssel den Index, als Value das Attribut.
						// Bei einer Kollision kann so ermittelt werden mit welchen Attributen es eine Kollision gibt.
						// Die Map ermöglicht den Test in "O(2*n)"(Aufbau der Map und lineares durchlaufen der Map)
						// anstatt O(n^2) durchzuführen.
						final Map<Integer, Attribute> attributeMap = new HashMap<Integer, Attribute>();
						for(Attribute attribute : attributes) {
							if(!attributeMap.containsKey(attribute.getPosition())) {
								attributeMap.put(attribute.getPosition(), attribute);
							}
							else {
								// Es war bereits ein Attribut mit dem Index in der Liste vorhanden -> Fehler
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.LOCAL_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject, attribute, attributeMap.get(attribute.getPosition())},
										"Es sind mindestens zwei Attribute mit einem Index vorhanden"
								);
								result.addEntry(entry);
							}
						}

						int requieredAttributeIndex = 1;

						// Für jeden Wert requieredAttributeIndex muss ein Eintrag in der Map vorhanden sein.
						// Und die Werte müssen bei 1 beginnen und dann fortlaufend sein
						while(requieredAttributeIndex <= attributes.size()) {
							if(!attributeMap.containsKey(requieredAttributeIndex)) {
//							final Set<Integer> integers = attributeMap.keySet();
//							System.out.println("Fehler, ein Index fehlt");
//							System.out.println(integers);
								// Zu einem Index wurde kein Attribut gefunden
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.LOCAL_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject},
										"Für einen Index wurde kein Attribute gefunden, Index " + requieredAttributeIndex
								);
								result.addEntry(entry);
							}
							requieredAttributeIndex++;
						}
					}

					// Prüfung der Zustände einer Ganzzahldefinition (nicht in TPuK gefordert)

					// Bei einer Attributdefinition, die eine Ganzzahl definiert, muss geprüft werden, ob die Zustände
					// definiert sind. Das bedeutet, dass:
					// 1) Jeder Name nur einmal verwendet wird
					// 2) Jeder Wert nur einmal vorkommt
					if(systemObject instanceof IntegerAttributeType) {
						final IntegerAttributeType integerAttributeType = (IntegerAttributeType)systemObject;

						final Set<String> names = new HashSet<String>();
						final Set<Long> values = new HashSet<Long>();

						final List<IntegerValueState> allStates = integerAttributeType.getStates();
						for(IntegerValueState state : allStates) {
							final String stateName = state.getName();
							final long stateValue = state.getValue();

							if(names.contains(stateName) == true || values.contains(stateValue) == true) {
								// Der Name oder der Wert wurde bereits vergeben -> Fehler gefunden
								final StringBuffer text = new StringBuffer("Der Zustand einer Ganzzahldefinition enthält ");

								if(names.contains(stateName) == true && values.contains(stateValue) == true) {
									text.append("einen Namen und einen Wert, der bereits benutzt wurde. Name: " + stateName + " Wert: " + stateValue);
								}
								else if(names.contains(stateName) == true) {
									text.append("einen Namen, der bereits benutzt wurde. Name: " + stateName + " Wert: " + stateValue);
								}
								else {
									text.append("einen Wert, der bereits benutzt wurde. Name: " + stateName + " Wert: " + stateValue);
								}

								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.WARNING, verifyingConfigArea, new SystemObject[]{systemObject}, text.toString()
								);
								result.addEntry(entry);
							}

							names.add(stateName);
							values.add(stateValue);
						}//for
					}

					// Bei alle Typen muss ein Parameterdatensatz vorhanden sein und zwar für jedes Objekt, das von diesem Typen ist.
					if(systemObject instanceof SystemObjectType) {
						final SystemObjectType systemObjectType = (SystemObjectType)systemObject;

						// Alle ATGŽs, die von diesem Typ sind
						final List<AttributeGroup> directATGs = new ArrayList<AttributeGroup>();
						// Die Javaobjekte sind vom falschen Typ
						final NonMutableSet atgSet = (NonMutableSet)systemObjectType.getObjectSet("Attributgruppen");
						if(atgSet != null) {
							final List<SystemObject> helperATGWithWrongObjecttyp = atgSet.getElementsInVersion(
									verifyingVersion
							);

							for(SystemObject object : helperATGWithWrongObjecttyp) {
								directATGs.add((AttributeGroup)object);
							}
						}
						// Hier werden alle ATGS des Typs eingetragen, die parametrierend sind. Aus dieser Menge werden alle Elemente entfernt,
						// zu denen ein parametrierender Daten gefunden wurde oder wenn ein Fehler im parametrierenden Datensatz gefunden wurde.
						// Für alle Objekte, die nach Ablauf des Algorithmus noch in dieser Menge enthalten sind, wurde kein Datensatz gefunden -> ebenfalls ein Fehler
						final Set<AttributeGroup> directParameterATGs = new HashSet<AttributeGroup>();

						// Für jede ATG, die parametrierend ist, muss ein default am Typ vorhanden sein
						for(AttributeGroup directATG : directATGs) {
							if(isParameter(directATG) == true) {
								directParameterATGs.add(directATG);
							}
						}

						final AttributeGroup atg = _dataModel.getAttributeGroup("atg.defaultParameterdatensätze");
						final Aspect aspect = _dataModel.getAspect("asp.eigenschaften");

						// Datensatz, der die parametrierenden Datensätze enthält
						final Data configurationData = ((ConfigSystemObject)systemObjectType).getConfigurationData(atg, aspect, _versionedView);

						if(configurationData != null && configurationData.isDefined()) {
							// Der Datensatz, wurde gefunden. Die Daten sind als Array gespeichert.
							final Data.Array arrayWithDefaultParameterDataSets = configurationData.getArray("Default-Parameterdatensatz");

							for(int nr = 0; nr < arrayWithDefaultParameterDataSets.getLength(); nr++) {
								final Data item = arrayWithDefaultParameterDataSets.getItem(nr);

								final AttributeGroup attributeGroup = (AttributeGroup)item.getReferenceValue("attributgruppe").getSystemObject();

								// In dem Set sind nur Parametriende ATGŽs enhalten. Ist die zu prüfende ATG ebenfalls parametrierend, muss der Rest ebenfalls
								// geprüft werden.
								if(directParameterATGs.contains(attributeGroup)) {

									final String pidType = item.getReferenceValue("typ").getSystemObject().getPid();

									final Data.Array datasetArray = item.getArray("datensatz");
									final byte[] bytes = new byte[datasetArray.getLength()];
									for(int j = 0; j < datasetArray.getLength(); j++) {
										bytes[j] = datasetArray.getScaledValue(j).byteValue();
									}

									final int serialiserVersion = item.getScaledValue("serialisierer").intValue();

									// Damit ein default Parameter richtig definiert ist, müssen folgende Bediengungen geprüft werden:
									// 1) Die Pid des Typen der Arrayeintrags muss gleich dem Typen sein, der gerade geprüft ist
									// 3) Die Größe des byte-Arrays muss größer 0 sein
									// 4) Der Datensatz muss deserialsiert werden können

									if(pidType.equals(systemObjectType.getPid())) {

										boolean errorFound = false;
										// Es wird ein Fehlertext eingetragen, wenn errorFound==true ist
										String errorText = "";

										// Es wird auf den zu prüfenden Typen referenziert
										// Die ATG ist parametrierend
										if(bytes.length > 0) {
											// prüfen, ob der DS deserialsiert werden kann

											try {
												final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
												final Deserializer deserializer = SerializingFactory.createDeserializer(
														serialiserVersion, in
												);
												final Data data = deserializer.readData(attributeGroup, _versionedView);

												// Der Datensatz muss definiert sein
												if(data.isDefined() == false) {
													// Fehler
													errorFound = true;
													errorText = "Der Datensatz eines Default-Parameter-Datensatz ist nicht definiert.";
												}
												else {
													// Kommt der Algorithmus bis an diese Stelle, wurde ein Defaultparameterdatensatz vollständig definiert
													directParameterATGs.remove(attributeGroup);
												}
											}
											catch(NoSuchVersionException e) {
												// Die Version des Desrialisieres wird nicht unterstüzt
												errorFound = true;
												errorText =
														"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil die Version des Deserialisieres nicht unterstützt wird. Version, die benutzt werden sollte: "
														+ serialiserVersion;
											}
											catch(IOException e) {
												// Der Datensatz kann nicht deserialisiert werden
												errorFound = true;
												errorText =
														"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil es beim deserialisieren zu folgendem Fehler gekommen ist: "
														+ e;
											}
										}
										else {
											// Dieser Datensatz muss vorhanden sein. Da dieser den Defaultwert enthält
											errorFound = true;
											errorText = "Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil das byte-Array, das den Datensätz enthält, die Länge 0 besitzt.";
										}

										if(errorFound == true) {
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													


													ConsistencyCheckResultEntryType.WARNING,
													verifyingConfigArea,
													new SystemObject[]{systemObject, attributeGroup},
													errorText
											);
											result.addEntry(entry);

											// Da bereits ein Fehler erzeugt wurde, kann das Objekt aus dem Set gelöscht werden, sonst würde
											// noch ein Fehler erzeugt werden.
											directParameterATGs.remove(attributeGroup);
										}
									}
								}
							}// for über alle Elemente des Arrays
						}

						// Es wurden alle Datensätze geprüft. Zu jeder ATG des Tys sollte ein Datensatz gefunden worden sein.
						// Es wurden alle ATGŽs entfernt die:
						// 1) Einen Datensatz hatten
						// 2) Einen Datensatz hatten, dieser aber nicht bearbeitet werden konnte (es wurde ein Fehler erzeugt)
						// Alle ATGŽs die jetzt noch in der Menge sind, haben keinen Datensatz -> Fehler
						if(directParameterATGs.size() > 0) {
							for(AttributeGroup directParameterATG : directParameterATGs) {
								// Fehlertext, der eingetragen wird, wenn dataSetFound auf "false" bleibt.
								String text = "Es wurde kein Default-Parameter-Datensatz für die Attributgruppe eines Typen gefunden.";
								


								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.WARNING, verifyingConfigArea, new SystemObject[]{systemObject, directParameterATG}, text
								);
								result.addEntry(entry);
							}
						}
					}// Typ prüfen
					else if(systemObject instanceof ConfigurationObject) {
						// Bei allen Konfigurationsobjekten kann ein Datensatz "atg.defaultParameterdatensätze" vorhanden sein.
						// Für Konfigurationsobjekte müssen dann folgende Bedienungen erfüllt sein (für jeden Array-Eintrag gilt):
						// 1) Die ATG muss parametrierend sein
						// 2) Das Byte-Array hat eine Größe größer 0 und kann mit der angegebenen Serialisiererversion deserialisiert werden
						// 3) Der unter 2) deserialisierte Datensatz muss definiert sein

						// Datensatz, der die parametrierenden Datensätze enthält
						final Data configurationData = ((ConfigSystemObject)systemObject).getConfigurationData(defaultParameterUsage, _versionedView);

						if(configurationData != null  && configurationData.isDefined()) {

							final Data.Array arrayWithDefaultParameterDataSets = configurationData.getArray("Default-Parameterdatensatz");

							for(int nr = 0; nr < arrayWithDefaultParameterDataSets.getLength(); nr++) {
								final Data item = arrayWithDefaultParameterDataSets.getItem(nr);

								final AttributeGroup attributeGroup = (AttributeGroup)item.getReferenceValue("attributgruppe").getSystemObject();
								final int serialiserVersion = item.getScaledValue("serialisierer").intValue();

								final Data.Array datasetArray = item.getArray("datensatz");
								final byte[] bytes = new byte[datasetArray.getLength()];
								for(int j = 0; j < datasetArray.getLength(); j++) {
									bytes[j] = datasetArray.getScaledValue(j).byteValue();
								}

								boolean errorFound = false;
								// Es wird ein Fehlertext eingetragen, wenn errorFound==true ist
								String errorText = "";

								// 1)
								if(isParameter(attributeGroup) == true) {
									//2
									if(bytes.length > 0) {
										final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
										try {
											final Deserializer deserializer = SerializingFactory.createDeserializer(
													serialiserVersion, in
											);
											final Data data = deserializer.readData(attributeGroup, _versionedView);

											if(data.isDefined() == false) {
												// Fehler
												errorFound = true;
												errorText = "Der Datensatz eines Default-Parameter-Datensatz ist nicht definiert.";
											}
										}
										catch(NoSuchVersionException e) {
											// Die Version des Desrialisieres wird nicht unterstüzt
											errorFound = true;
											errorText =
													"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil die Version des Deserialisieres nicht unterstützt wird. Version, die benutzt werden sollte: "
													+ serialiserVersion;
										}
										catch(IOException e) {
											// Der Datensatz kann nicht deserialisiert werden
											errorFound = true;
											errorText =
													"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil es beim deserialisieren zu folgendem Fehler gekommen ist: "
													+ e;
										}
									}
								}
								else {
									// Dieser Datensatz muss vorhanden sein. Da dieser den Defaultwert enthält
									errorFound = true;
									errorText = "Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil das byte-Array, das den Datensätz enthält, die Länge 0 besitzt.";
								}

								if(errorFound == true) {
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											


											ConsistencyCheckResultEntryType.WARNING,
											verifyingConfigArea,
											new SystemObject[]{systemObject, attributeGroup},
											errorText
									);
									result.addEntry(entry);
								}
							}
						}

						// Wenn das Objekt eine Typ-Hierarchiedefinition für die Parametrierung ist, dann wird es später weiter geprüft
						if(hierarchyDefinitionTypes.contains(objectTypeOfVerifiedObject)) {
							hierarchyObjects.add((ConfigurationObject)systemObject);
						}
					}
				} // for(SystemObject systemObject : checkObjects)
			}// for, alle Konfigurationsbereiche

			_debug.info(
					"Prüfe Typ-Hierarchie für Parametrierung. Anzahl Hierarchie-Objekte: " + hierarchyObjects.size() + " Anzahl bisheriger Fehler: "
					+ (result.getInterferenceErrors().size() + result.getLocalErrors().size()) + " Anzahl bisheriger Warnungen: " + result.getWarnings().size()
			);

			// Prüft, ob die Hierarchiedefinition der Typen für die Parametrierung in Ordnung sind.
			checkParameterTypeHierarchyDefinition(result, hierarchyObjects);

			// Die Konsistenzprüfung hat alle Abhängigkeiten zwischen den Bereichen erkannt. Wurden keine entsprechenden Fehler gefunden, können
			// die Abhängigkeiten gespeichert werden.

			try {
				if(kindOfConsistencyCheck == KindOfConsistencyCheck.LOCAL_ACTIVATION) {
					if(result.interferenceErrors() == false && result.localError() == false) {
						saveDependencies();
					}
				}
				else if(kindOfConsistencyCheck == KindOfConsistencyCheck.RELEASE_FOR_TRANSFER) {
					if(result.localError() == false) {
						saveDependencies();
					}
				}
				else if(kindOfConsistencyCheck == KindOfConsistencyCheck.RELEASE_FOR_ACTIVATION_WITHOUT_LOCAL_ACTIVATION) {
					// In diesem Fall sind Interferenzfehler erlaubt, da diese durch einen anderen aufgelöst werden können.
					// Hat jemand anders die Interferenzfehler behoben, kann dieser Bereich auch lokal aktiviert werden.
					if(result.localError() == false) {
						saveDependencies();
					}
				}

				_areasDependencies.clear();
			}
			catch(ConfigurationChangeException e) {
				final ConfigurationAuthority configurationAuthority = _dataModel.getConfigurationAuthority();
				final ConfigurationArea configurationArea = configurationAuthority == null ? null : configurationAuthority.getConfigurationArea();
				result.addEntry(
						new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configurationArea,
								Collections.<SystemObject>emptyList(),
								"Fehler in der Konsistenzprüfung beim Schreiben der Datensätze, die die Abhängigkeiten zwischen den Bereichen speichern:\n" +
								getStackTrace(e)
						)
				);
			}
		}
		catch(Exception e){
			String stacktrace = getStackTrace(e);

			final ConfigurationAuthority configurationAuthority = _dataModel.getConfigurationAuthority();
			final ConfigurationArea configurationArea = configurationAuthority == null ? null : configurationAuthority.getConfigurationArea();
			result.addEntry(
					new ConsistencyCheckResultEntry(
							ConsistencyCheckResultEntryType.LOCAL_ERROR,
							configurationArea,
							Collections.<SystemObject>emptyList(),
							"Bei der Konsistenzprüfung ist ein unerwarteter Fehler aufgetreten:\n" + stacktrace
					)
			);
		}
		return result;
	}

	private static String getStackTrace(final Exception e) {
		// Stacktrace in String umwandeln
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}

	/**
	 * Prüft, ob ein Objekt vom anderen Abhängig ist und trägt beim Konfigurationsbereich, falls nötig, die Abhängigkeit ein.
	 * <p>
	 * Der Konfigurationsverantwortliche des Parameters <code>systemObject</code> muss der Verantwortliche der Konfiguration sein, da nur dieser Abhängigkeiten in
	 * den Bereichen eintragen darf.
	 * <p>
	 * Wenn die beiden Objekte im selben Bereich sind, kommt es zu keiner Abhängigkeit.
	 *
	 * @param systemObject     Objekt, das vielleicht von einem anderen Objekt abhängig ist und somit dazu führt, dass der Bereich des Objekts eine Abhängigkeit zu
	 *                         einem anderen Bereich erhält.
	 * @param dependencyObject Objekt von dem der Parameter <code>systemObject</code> abhängig ist.
	 * @param dependencyKind   Art der Abhängigkeit, wenn eine Abhängigkeit gefunden wird. Als Versionen werden die eingetragen, in der die Aktion ausgeführt wird,
	 *                         dies muss nicht unbedingt die aktuelle sein.
	 */
	private void checkDependency(
			final SystemObject systemObject, final SystemObject dependencyObject, ConfigurationAreaDependencyKind dependencyKind) {
		// Abhängigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		if(!_storeDependencies) return;

		if(systemObject == null || dependencyObject == null) {
			_debug.warning("Abhängigkeit zwischen zwei Objekten kann nicht geprüft werden, systemObjekt: " + systemObject + ", dependencyObject: " + dependencyObject);
			return;
		}
		// Es darf in den Bereich nur dann eine Abhängigkeit eingetragen werden, wenn der KV der Konfiguration auch für den Bereich verantwortlich ist.
		if(systemObject.getConfigurationArea().getConfigurationAuthority().getId() == _configurationAuthorityId) {
			if(systemObject.getConfigurationArea().getId() != dependencyObject.getConfigurationArea().getId()) {

				// Beide Objekte befinden sich in unterschiedlichen Bereichen. Also wurde eine Abhängigkeit gefunden. Diese wird am Bereich eingetragen.

				// Abhängigkeiten können auch durch dynamische Objekte entstehen. Dynamische Objekte können aber auch transient (stehen nach dem Neustart der
				// Konfiguration nicht mehr zur Verfügung) sein und das darf keine Abhängigkeit auslösen.
				if(isObjectTransient(systemObject) == false && isObjectTransient(dependencyObject) == false) {
					// Das Objekt, das die Abhängigkeite auslöst ist ein Konfigurationsobjekt. Damit wird entweder die Version des Bereichs genommen
					// in der gerade eine Aktion stattfindet (denn in der würde die Abhängigkeit ja bestehen) oder aber die Version in der der Bereich
					// aktiv ist (diese Abhängigkeit wäre schon früher erfaßt worden und wird somit nicht erneut aufgenommen (siehe ConfigConfigurationArea)).
					// Falls das Objekt dependencyObject ein dynamisches Objekt ist, muss zuerst die Version ermittelt werden, in der das Objekt erzeugt wurde.
					createDependency(
							getActiveVersion(systemObject.getConfigurationArea()),
							systemObject.getConfigurationArea(),
							getDependenceVersion(dependencyObject),
							dependencyObject.getConfigurationArea(),
							dependencyKind
					);
				} // Prüfung, ob die Objekte beide nicht transient sind, im else-Fall muss nichts gemacht werden. Da Transiente Objekte keine Abhängigkeiten auslösen dürfen.
			}
		}
	}

	/**
	 * Prüft, ob ein Objekt transient ist. Konfigurationsobjekt sind niemals transient, dort wird immer <code>false</code> zurückgegeben. Bei dynamischen Objekten
	 * muss dies geprüft werden.
	 *
	 * @param systemObject Objekt, das geprüft werden soll, ob es transient ist.
	 *
	 * @return <code>false</code>, wenn ein dynamisches Objekt nicht transient ist und <code>false</code>, wenn ein Konfigurationsobjekt übergeben wird;
	 *         <code>true</code>, wenn ein dynamisches Objekt transient ist.
	 */
	private boolean isObjectTransient(final SystemObject systemObject) {
		if(systemObject instanceof ConfigurationObject) {
			return false;
		}
		else {
			final DynamicObjectType dynamicObjectType = (DynamicObjectType)systemObject.getType();

			if(dynamicObjectType.getPersistenceMode() == DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * Speichert eine Abhängigkeit für den Bereich <code>verifyingArea</code> in Map <code>_areasDependencies</code>.
	 * <p>
	 * Damit die Daten endgültig als Datensatz gespeichert werden, muss die Methode {@link #saveDependencies()} aufgerufen werden.
	 *
	 * @param verifyingVersion Version, in der die Abhängigkeit entstanden ist.
	 * @param verifyingArea    Bereich, der ab <code>verifyingVersion</code> vom Bereich <code>dependencyArea</code> abhängig ist.
	 * @param neededVersion    Version, in der der Bereich <code>dependencyArea</code> vorliegen muss, damit die Abhängigkeit aufgelöst werden kann.
	 * @param dependencyArea   Bereich, von dem <code>verifyingArea</code> abhängig ist.
	 * @param dependencyKind   Art der Abhängigkeit
	 */
	private void createDependency(
			short verifyingVersion,
			ConfigurationArea verifyingArea,
			short neededVersion,
			ConfigurationArea dependencyArea,
			ConfigurationAreaDependencyKind dependencyKind) {

		final ConfigurationAreaDependency newDependency = new ConfigurationAreaDependency(
				verifyingVersion, neededVersion, dependencyArea, dependencyKind
		);

		synchronized(_areasDependencies) {
			Set<ConfigurationAreaDependency> allAreaDependencies = _areasDependencies.get(verifyingArea);

			// Falls noch keine Abhängigkeit gefunden wurde, wird eine Menge angelegt, die diese neue Abhängigkeit speichern kann.
			if(allAreaDependencies == null) {
				allAreaDependencies = new HashSet<ConfigurationAreaDependency>();
				_areasDependencies.put(verifyingArea, allAreaDependencies);
			}
			allAreaDependencies.add(newDependency);
		}// synch
	}

	/**
	 * Speichert alle Abhängigkeiten zwischen Bereichen, die durch die Konsistenzprüfung gefunden wurden, in entsprechenden Datensätzen im Bereich.
	 * <p>
	 * Die Methode sperrt die  <code>_areasDependencies</code> bis alle Operationen abgeschlossen sind.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Wird geworfen, wenn der Datensatz, der die Abhängigkeiten enthält, nicht geschrieben werden kann.
	 */
	private void saveDependencies() throws ConfigurationChangeException {
		// Abhängigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		if(!_storeDependencies) return;

		synchronized(_areasDependencies) {

			// Alle Aufrufe in diesem Block können zu Festplattenzugriffen führen. Das kann diese Methode sehr lange blockieren lassen.
			// In der Zeit ist es auch nicht möglich weitere Abhängigkeiten hinzuzufügen. Das ist aber auch nicht nötig, da diese
			// Methode am Ende der Konsistenzprüfung aufgerufen wird.

			final Set<ConfigurationArea> areas = _areasDependencies.keySet();
			for(ConfigurationArea area : areas) {
				final Short desiredVersionObject = _areaVersionMap.get(area);
				if(desiredVersionObject == null) {
//					System.out.println("*****************************************************************************");
//					System.out.println("*****************************************************************************");
//					System.out.println("area = " + area.getPidOrNameOrId());
//					System.out.println("*****************************************************************************");
//					System.out.println("*****************************************************************************");
					continue;
				}
				final int desiredVersion = desiredVersionObject.intValue();
				int maxFreezedVersion = area.getActiveVersion();
				if(maxFreezedVersion < area.getTransferableVersion()) maxFreezedVersion = area.getTransferableVersion();
				if(maxFreezedVersion < area.getActivatableVersion()) maxFreezedVersion = area.getActivatableVersion();
				// Nur Speichern, wenn der Bereich verändert wurde
				if( maxFreezedVersion >= desiredVersion) {
//					System.out.println("*****************************************************************************");
//					System.out.println("*****************************************************************************");
//					System.out.println("area = " + area.getPidOrNameOrId());
//					System.out.println("maxFreezedVersion = " + maxFreezedVersion);
//					System.out.println("desiredVersion = " + desiredVersion);
//					System.out.println("*****************************************************************************");
//					System.out.println("*****************************************************************************");
					continue;
				}
				ConfigConfigurationArea configConfigurationArea = ((ConfigConfigurationArea)area);
				final Set<ConfigurationAreaDependency> dependencies = _areasDependencies.get(area);
				try {

					configConfigurationArea.addAreaDependency(dependencies);
				}
				catch(ConfigurationChangeException e) {
					// Der Datensatz, der die Abhängigkeiten speichert, konnte nicht geschrieben werden.
					final StringBuffer errorText = new StringBuffer();
					errorText.append(
							"Fehler in der Konsistenzprüfung: Der Datensatz, der alle Abhängigkeiten eines Bereich enthält, konnte nicht geschrieben werden. Betroffener Bereich: "
							+ area.getConfigurationArea().getPid() + " KV des Bereichs: " + area.getConfigurationArea().getConfigurationAuthority().getPid()
							+ " KV der Konfiguration: " + _dataModel.getConfigurationAuthorityPid() + " .Abhängigkeiten, die geschrieben werden sollten: "
							+ dependencies
					);

					throw new ConfigurationChangeException(errorText.toString(), e);
				}
			}
		}// synch
	}

	private void checkParameterTypeHierarchyDefinition(final ConsistencyCheckResult result, final Collection<ConfigurationObject> hierarchyObjects) {
		Map<SystemObjectType, Set<SystemObjectType>> typeHierarchy = new HashMap<SystemObjectType, Set<SystemObjectType>>();
		final SystemObject hierarchyDefinitionAtg = _versionedView.getObject("atg.hierarchieDefinition");
		if(!(hierarchyDefinitionAtg instanceof AttributeGroup)) {
			throw new IllegalStateException(
					"atg.hierarchieDefinition wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
			);
		}
		final SystemObject hierarchyDefinitionAspect = _versionedView.getObject("asp.eigenschaften");
		if(!(hierarchyDefinitionAspect instanceof Aspect)) {
			throw new IllegalStateException(
					"asp.eigenschaften wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
			);
		}
		final AttributeGroupUsage hierarchyDefinitionUsage = ((AttributeGroup)hierarchyDefinitionAtg).getAttributeGroupUsage((Aspect)hierarchyDefinitionAspect);
		for(ConfigurationObject hierarchyObject : hierarchyObjects) {
			Data data = hierarchyObject.getConfigurationData(hierarchyDefinitionUsage);
			if(data != null) {
				Iterator hierarchyDefinitions = data.getItem("HierarchieObjekte").iterator();
				while(hierarchyDefinitions.hasNext()) {
					Data hierarchyDefinition = (Data)hierarchyDefinitions.next();
					final String typePid = hierarchyDefinition.getTextValue("ObjektTyp").getText();
					final SystemObject typeObject = _versionedView.getObject(typePid);
					if(!(typeObject instanceof SystemObjectType)) {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
								hierarchyObject.getConfigurationArea(),
								new SystemObject[]{hierarchyObject},
								"Der ObjektTyp einer Hierarchiedefinition ist keine Pid eines Typs: " + typePid
						);
						result.addEntry(entry);
					}
					else {
						String setName = hierarchyDefinition.getTextValue("Menge").getText();
						final String successorPid = hierarchyDefinition.getTextValue("NachfolgerTyp").getText().trim();
						final SystemObject successorTypeObject;
						if(successorPid.equals("") || successorPid.equals("null")) {
							successorTypeObject = null;
						}
						else {
							successorTypeObject = _versionedView.getObject(successorPid);
						}
						if(setName.equals("") && successorTypeObject == null) {
							// Definition eines Blattknotens in der Hierarchie
							continue;
						}
						final SystemObjectType type = ((SystemObjectType)typeObject);
						final List<ObjectSetUse> setUses = _versionedView.getObjectSetUses(type);
						ObjectSetUse foundSetUse = null;
						for(ObjectSetUse setUse : setUses) {
							if(setUse.getObjectSetName().equals(setName)) {
								foundSetUse = setUse;
								break;
							}
						}
						if(foundSetUse == null) {
							final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
									hierarchyObject.getConfigurationArea(),
									new SystemObject[]{hierarchyObject},
									"Der Mengenname '" + setName + "' einer Hierarchiedefinition für den Typ '" + typePid
									+ "' ist an Objekten dieses Typs nicht erlaubt"
							);
							result.addEntry(entry);
						}
						else {
							if(!(successorTypeObject instanceof SystemObjectType)) {
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
										hierarchyObject.getConfigurationArea(),
										new SystemObject[]{hierarchyObject},
										"Der NachfolgerTyp einer Hierarchiedefinition ist keine Pid eines Typs: " + successorPid
								);
								result.addEntry(entry);
							}
							else {
								final SystemObjectType successorType = ((SystemObjectType)successorTypeObject);
								final ObjectSetType objectSetType = foundSetUse.getObjectSetType();

								final ObjectSet allowedTypesSet = objectSetType.getObjectSet("ObjektTypen");
								final List<SystemObjectType> allowedTypes = new ArrayList<SystemObjectType>();
								if(allowedTypesSet != null) {
									final Collection<SystemObject> allowedTypeObjects = _versionedView.getElements(allowedTypesSet);
									for (SystemObject systemObject : allowedTypeObjects) {
										if(systemObject instanceof SystemObjectType) {
											SystemObjectType systemObjectType = (SystemObjectType)systemObject;
											allowedTypes.add(systemObjectType);
										}
									}
								}
								boolean typeIsAllowed = false;
								for(SystemObjectType allowedType : allowedTypes) {
									if(successorType.getId() == allowedType.getId() || _versionedView.inheritsFrom(successorType, allowedType)) {
										typeIsAllowed = true;
										break;
									}
								}
								if(!typeIsAllowed) {
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											hierarchyObject.getConfigurationArea(),
											new SystemObject[]{hierarchyObject, foundSetUse},
											"Der NachfolgerTyp '" + successorPid + "' einer Hierarchiedefinition ist in der Menge '" + setName
											+ "' nicht erlaubt"
									);
									result.addEntry(entry);
								}
								else {
									Set<SystemObjectType> successorTypes = typeHierarchy.get(type);
									if(successorTypes == null) {
										successorTypes = new HashSet<SystemObjectType>();
										typeHierarchy.put(type, successorTypes);
									}
									successorTypes.add(successorType);
								}
							}
						}
					}
				}
			}
		}
		for(Map.Entry<SystemObjectType, Set<SystemObjectType>> typeEntry : typeHierarchy.entrySet()) {
			final SystemObjectType type = typeEntry.getKey();
			final LinkedList<SystemObjectType> typesToBeVisited = new LinkedList<SystemObjectType>();
			typesToBeVisited.add(type);
			final Set<SystemObjectType> visitedTypes = new HashSet<SystemObjectType>();
			while(!typesToBeVisited.isEmpty()) {
				SystemObjectType visitType = typesToBeVisited.remove();
				for(SystemObjectType visitedType : visitedTypes) {
					if(visitType.getId() == visitedType.getId() || _versionedView.inheritsFrom(visitType, visitedType) || _versionedView.inheritsFrom(
							visitedType, visitType
					)) {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
								visitType.getConfigurationArea(),
								new SystemObject[]{visitType, visitedType},
								"Die Typ-Hierarchiedefinition für die Parametrierung ist nicht Zykelfrei"
						);
						result.addEntry(entry);
						return;
					}
				}
				final Set<SystemObjectType> successorTypes = typeHierarchy.get(visitType);
				if(successorTypes != null) {
					typesToBeVisited.addAll(successorTypes);
				}
				visitedTypes.add(visitType);
			}
		}
	}

	/**
	 * Prüft, ob eine ATG in der aktiven/zu aktivierenden Version parametrierend ist.
	 *
	 * @param checkATG ATG, die geprüft werden soll
	 *
	 * @return <code>true</code>, wenn die übergeben ATG in der übergebenen Version gültig ist; <code>false</code>, wenn nicht.
	 */
	private boolean isParameter(final AttributeGroup checkATG) {

		final short version = getActiveVersion(checkATG);

		// 1) Damit ein ATG parametrierend ist, muss der Aspekt asp.soll und asp.vorgabe vorhanden sein
		// 2) Die Aspekte aus 1) sind an der ATGV gespeichert -> Die ATGV muss in der richtigen Version angefordert werden

		// Alle ATGV in der richtigen Version
		final NonMutableSet mutableSet = checkATG.getNonMutableSet("AttributgruppenVerwendungen");

		final List<SystemObject> atgu = mutableSet.getElementsInVersion(version);

		boolean vorgabeFound = false;
		boolean sollFound = false;

		for(SystemObject wrongObjectType : atgu) {
			final AttributeGroupUsage usage = (AttributeGroupUsage)wrongObjectType;
			final Aspect aspect = usage.getAspect();

			if("asp.parameterVorgabe".equals(aspect.getPid())) {
				vorgabeFound = true;
			}
			else if("asp.parameterSoll".equals(aspect.getPid())) {
				sollFound = true;
			}

			// Da beide Parameter gefunden wurden, ist das Objekt parametrierend
			if(vorgabeFound == true && sollFound == true) return true;
		}
		// Es wurden alle Objekte betrachtet, aber die gesuchten Aspekte wurden nicht gefunden
		return false;
	}

	/**
	 * Beim anlegen eines Konfigurationsobjekts, das einen Konfigurationsbereich darstellt, werden in einem Datensatz drei Zeitstempel gespeichert.
	 * <p>
	 * Bei der ersten Version wurden diese drei Zeitstempel mit 0 deklariert. Die Zahl 0 im Zusammenhang mit abseluten Zeitangaben wurden aber zur Erkennung des
	 * "undefiniert" Werts benutzt. Damit waren diese Datensätze immer "nicht definiert", dies wurde in der Konsistenzprüfung als Fehler erkannt (Der Datensatz war
	 * nicht definiert).
	 * <p>
	 * Dieser Fehler wird im Rahmen des Imports behoben, neue Objekte bekommen als Zeitstempel die aktuelle Zeit.
	 * <p>
	 * Damit trotzdem mit alten Daten weiter gearbeitet werden kann, wird dieser Fehler absichtlich von der Konsistenzprüfung ignoriert.
	 * <p>
	 * Es müssen folgende Bediengungen erfüllt sein, damit diese Methode <code>true</code> zurück gibt: 1) Das übergebene Objekt muss ein Konfigurationsbereich
	 * sein 2) Es muss sich um die Attributgruppe "atg.konfigurationsBereichÄnderungsZeiten" handeln 3) Es muss der Aspekt "asp.eigenschaften" Eigenschaften
	 * benutzt werden 4) Einer der folgenden Attribute muss 0 sein: LetzteÄnderungszeitDynamischesObjekt, LetzteÄnderungszeitKonfigurationsObjekt,
	 * LetzteÄnderungszeitDatensatz 5) Falls ein Attribut nicht 0 ist, so muss der Wert des Attributs definiert sein.
	 *
	 * @param configArea     Es muss sich um einen Konfigurationsbereich handeln (das wird mit instanceOf geprüft)
	 * @param attributeGroup ATG, die zu einem Fehler führte
	 * @param aspect         Aspekt, der zu einem Fehler führte
	 * @param data           Datensatz, der Daten enthält, die nicht definiert sind.
	 *
	 * @return <code>true</code>, wenn die übergenen Parameter zwar einen lokalen Fehler enthalten, dieser aber ignoriert werden kann. <code>false</code>, wenn es
	 *         sich um einen lokalen Fehler handelt, der gemeldet werden muss.
	 */
	private boolean ignoreAttributeValueError(final SystemObject configArea, final AttributeGroup attributeGroup, final Aspect aspect, final Data data) {
		if(configArea instanceof ConfigurationArea == false) {
			// Es handelt sich um keinen Bereich. Aber nur diese dürfen den fehlerhaften Datensatz besitzen
			return false;
		}

		// Handelt es sich um die ATG, die den fehlerhaften Datensatz speichern darf
		if("atg.konfigurationsBereichÄnderungsZeiten".equals(attributeGroup.getPid()) == false) {
			// Es handelt sich um eine andere ATG
			return false;
		}

		if("asp.eigenschaften".equals(aspect.getPid()) == false) {
			// Falscher Aspekt
			return false;
		}

		// Objekt, ATG und Aspekt stimmen. Nun muss geprüft werden, ob die(mindestens eins) gespeicherten Attribute 0 sind

		// wird true, wenn der Datensatz mindestens ein Attribut den Wert 0 besitzt.
		boolean zeroFound = false;

		// Wird true, wenn ein Attribut einen ungültigen Wert besitzt
		boolean errorFound = false;

		String attribute = "LetzteÄnderungszeitDynamischesObjekt";

		if(data.getTimeValue(attribute).getMillis() == 0) {
			zeroFound = true;
		}
		else if(data.getItem(attribute).isDefined() == false) {
			// Der Wert ist nicht 0 aber trotzdem undefiniert -> Es handelt sich um einen fehlerhaften Wert
			errorFound = true;
		}

		attribute = "LetzteÄnderungszeitKonfigurationsObjekt";

		if(data.getTimeValue(attribute).getMillis() == 0) {
			zeroFound = true;
		}
		else if(data.getItem(attribute).isDefined() == false) {
			// Der Wert ist nicht 0 aber trotzdem undefiniert -> Es handelt sich um einen fehlerhaften Wert
			errorFound = true;
		}

		attribute = "LetzteÄnderungszeitDatensatz";

		if(data.getTimeValue(attribute).getMillis() == 0) {
			zeroFound = true;
		}
		else if(data.getItem(attribute).isDefined() == false) {
			// Der Wert ist nicht 0 aber trotzdem undefiniert -> Es handelt sich um einen fehlerhaften Wert
			errorFound = true;
		}

		// Nur eine 0 finden reicht nicht aus. Es kann eine 0 gefunden werden und trotzdem ist ein anderes Attribut falsch. Darum muss beides gelten.
		if(zeroFound == true && errorFound == false) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Prüft, ob eine Komponente bereits von einem anderen übergeordneten Objekt benutzt wird. Die übergebene Komponente wird in der übergebenen Map gespeichert.
	 * <p>
	 * Wird die Komponente bereits benutzt, so wird ein lokaler Fehler erzeugt und am übergebenen Objekt <code>result</code> gespeichert.
	 *
	 * @param component          Komponente, die geprüft werden soll. Diese wird als Key in der übergebenen Map gespeichert.
	 * @param componentUser      Übergeordnetes Objekt, dass die Komponente nutzt. Dieser Wert wird als Value in der Map gespeichert.
	 * @param componentsOfAnArea Map, die alle bisher benutzten Komponenten und deren Benutzer enthält.
	 * @param result             Objekt, an dem lokale Fehler gespeichert werden.
	 *                           <p>
	 *
	 * @return <code>true</code>, wenn es zu keinem Fehler gekommen ist. <code>false</code>, wenn eine Komponente von zwei übergeordneten Objekten benutzt wurde.
	 */
	private boolean checkDoubleComponentUsage(
			final SystemObject component,
			final SystemObject componentUser,
			final Map<SystemObject, List<SystemObject>> componentsOfAnArea,
			final ConsistencyCheckResult result) {
		synchronized(componentsOfAnArea) {
			if(!componentsOfAnArea.containsKey(component)) {
				// Es gibt bisher niemanden, der diese Komponente nutzt. Das ist der Normalfall.

				final List<SystemObject> helper = new ArrayList<SystemObject>();
				helper.add(componentUser);

				componentsOfAnArea.put(component, helper);

				return true;
			}
			else {
				// Die Komponente wird schon benutzt.

				// Alle Objekte, die an dem Fehler beteiligt waren
				final List<SystemObject> involvedObjects = new ArrayList<SystemObject>();
				involvedObjects.add(component);

				// Alle Objekte, die diese Komponente benutzen
				final List<SystemObject> componentUsers = componentsOfAnArea.get(component);
				componentUsers.add(componentUser);

				involvedObjects.addAll(componentUsers);

				final String localErrorText = "Eine Komponente wird mehrfach verwendet. Verwendete Komponente: " + component.getPid();

				final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
						ConsistencyCheckResultEntryType.LOCAL_ERROR, component.getConfigurationArea(), involvedObjects, localErrorText
				);
				result.addEntry(entry);

				return false;
			}
		}
	}

	/**
	 * Diese Methode prüft, ob eine Id bereits vergeben wurde. Dazu werden alle Ids aller betrachteten Objekte gespeichert. Wurde eine Id bereits vergeben, so wird
	 * ein lokaler Fehler gemeldet.
	 *
	 * @param systemObject    Objekt, das überprüft werden soll
	 * @param idsFromAllAreas Map, die alle Ids speichert. In dieser Map wir das übergebene Objekt <code>systemObject</code> ebenfalls gespeichert.
	 * @param result          Wird eine doppelte Id gefunden, so wird ein lokaler Fehler erzeugt und an diesem Objekt gespeichert.
	 */
	private void checkDoubleIdsInDifferentAreas(
			final SystemObject systemObject, final Map<Long, List<SystemObject>> idsFromAllAreas, final ConsistencyCheckResult result) {
		synchronized(idsFromAllAreas) {
			final Long id = new Long(systemObject.getId());

			if(!idsFromAllAreas.containsKey(id)) {
				// bisher gab es noch kein Objekt mit der Id (dies ist der Regelfall)
				final List<SystemObject> newList = new ArrayList<SystemObject>();
				newList.add(systemObject);
				idsFromAllAreas.put(id, newList);
			}
			else {
				// Es wurde eine doppelte Id gefunden
				final List<SystemObject> objectsWithSameId = idsFromAllAreas.get(id);
				objectsWithSameId.add(systemObject);

				final String localErrorText = "Eine Id wurde doppelt vergeben: Id " + systemObject.getId();

				final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
						ConsistencyCheckResultEntryType.LOCAL_ERROR,
						systemObject.getConfigurationArea(),
						objectsWithSameId.toArray(new SystemObject[objectsWithSameId.size()]),
						localErrorText
				);
				result.addEntry(entry);
			}
		}
	}

	/**
	 * Diese Methode prüft, ob die Pid des übergebenen Objekts bereits in einem anderen Konfigurationsbereich vergeben wurde. Ist dies der Fall, so wird eine
	 * Warnung erzeugt und an das übergebene Objekt <code>result</code> übergeben.
	 * <p>
	 * Eine Ausnahme bilden Objekte mit der Pid "" (es wurde keine Pid angegeben), diese werden nicht gespeichert und nicht Berücksichtigt.
	 * <p>
	 * Die Warnung darf nur eingetragen werden, wenn die beiden Objekte in unterschiedlichen Konfigurationsbereichen zu finden sind. Sind beide Objekte im gleichen
	 * Bereich, wurde ein lokaler Fehler gefunden. Dieser muss durch einen anderen Test gefunden und gemeldet werden.
	 * <p>
	 * Wurde die Pid bisher nicht vergeben, so wird sie in der übergebenen Map gespeichert.
	 *
	 * @param systemObject     Objekt, dessen Pid geprüft werden soll.
	 * @param pidsFromAllAreas Alle Pids, die bisher in die Map eingetragen wurden. Wurde eine doppelte Pid gefunden, so wird das neue Objekt nicht in die Map
	 *                         eingetragen.
	 * @param result           Objekt, in dem eine Warnung eingetragen wird, wenn die Pid des übergebenen Objekts <code>systemObject</code> bereits in der Map
	 *                         vorhanden war.
	 */
	private void checkDoublePidsInDifferntAreas(
			final SystemObject systemObject, final Map<String, SystemObject> pidsFromAllAreas, final ConsistencyCheckResult result) {
		synchronized(pidsFromAllAreas) {
			if(!systemObject.getPid().equals("")) {
				if(!pidsFromAllAreas.containsKey(systemObject.getPid())) {
					// Es gibt bisher kein Objekt mit der Pid (das ist der Regelfall)
					pidsFromAllAreas.put(systemObject.getPid(), systemObject);
				}
				else {
					// Die Pid wurde bereits vergeben.

					final SystemObject objectWithSamePid = pidsFromAllAreas.get(systemObject.getPid());

					// Befinden sich beide Objekte in unterschiedlichen Bereichen, so muss eine Warnung erzeugt werden.
					if(!objectWithSamePid.getConfigurationArea().equals(systemObject.getConfigurationArea())) {
						// Die beiden Bereiche sind nicht gleich -> Warnung erzeugen
						final String warningText = "Eine Pid ist in mindestens zwei Bereichen zur gleichen Zeit aktiv: Pid " + systemObject.getPid()
						                           + " betroffene Bereiche: " + systemObject.getConfigurationArea().getPid() + " , "
						                           + objectWithSamePid.getConfigurationArea().getPid();
						final boolean allowDoublePids = _dataModel.getAllowDoublePids();
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								allowDoublePids ? ConsistencyCheckResultEntryType.WARNING : ConsistencyCheckResultEntryType.INTERFERENCE_ERROR, 
								systemObject.getConfigurationArea(),
								new SystemObject[]{systemObject, objectWithSamePid},
								warningText
						);
						result.addEntry(entry);
					}
					else {
						// Es wurde ein lokaler Fehler gefunden. Dieser wird an einer anderen Stelle bearbeitet.
					}
				}
			}
		}
	}

	/**
	 * Durchläuft rekursiv einen Datensatz und prüft ob alle referenzierten Objekte im entsprechenden Konfigurationsbereich vorhanden und gültig sind.
	 * <p>
	 * Ist ein referenziertes Objekt als Komposition gekennzeichnet, so muss das referenzierte Objekt im übergebenen Konfigurationsbereich zu finden sein.
	 * <p>
	 * Ist ein referenziertes Objekt als Aggregation bzw. Assoziation gekennzeichnet, muss das referenzierte Objekt im übergebenen Datenmodell zu finden sein.
	 * <p>
	 * In allen Fällen muss das referenzierte Objekt in der angegebenen Version gültig sein.
	 *
	 * @param data           Datensatz, der geprüft werden soll
	 * @param parentData     Übergeordnetes Data-Objekt oder <code>null</code>, falls <code>data</code> einen ganzen Datensatz darstellt.
	 * @param configArea     Konfigurationsbereich, in dem sich ein referenziertes Objekt befinden muss, wenn die Referenz als Komposition definiert ist
	 * @param dataModel      Datenmodell, in dem sich ein referenziertes Objekt befinden muss, wenn die Referenz nicht als Komposition definiert ist
	 * @param errorObject    Objekt, in dem ein Fehler eingetragen werden kann
	 * @param systemObject   Objekt, an dem der Datensatz gespeichert ist. Diese Information wird benötigt, um eine entsprechende Fehlermeldung zu generieren
	 * @param usedComponents Speichert alle Komponenten und deren übergeordnete Objekte.
	 *
	 * @return true = Alle Objekte, die referenziert werden, sind im Konfigurationsbereich vorhanden, falls nicht wird false zurückgegeben.
	 */
	private boolean checkDataSetReferences(
			final Data data,
			final Data parentData,
			final ConfigurationArea configArea,
			DataModel dataModel,
			ConsistencyCheckResult errorObject,
			SystemObject systemObject,
			final Map<SystemObject, List<SystemObject>> usedComponents) {
		if(data.isPlain()) {
			// Rekursionsende
			if(data.getAttributeType() instanceof ReferenceAttributeType) {
				final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)data.getAttributeType();

				



				// Es ist eine Referenz

				// Objekt, das referenziert wurde. Das Objekt wird über die Id im Datenmodell angefordert.
				// Id (Zahl ungleich 0) oder "0"(Id ist 0), falls das Objekt mit der Id nicht gefunden wurde oder eine Referenz
				// undefiniert ist.
				final SystemObject referencedObject;

				if(data.asReferenceValue().getId() != 0) {
					// Objekt mit der Id anfordern
					referencedObject = dataModel.getObject(data.asReferenceValue().getId());

					// Es kann sein, dass das Objekt nicht gefunden wurde, weil der Benutzer den Bereich "vergessen" hat.
					// Es steht dann zwar eine Id im Datensatz, diese kann aber nicht aufgelöst werden -> es ist zu einer Abhängigkeit gekommen.
					if(referencedObject == null) {
						// Das referenzierte Objekt kann nicht gefunden werden. Also fehlt der Bereich.
						// Es besteht zwar eine Abhängigkeit, aber diese kann nicht aufgelöst werden.

						if(referenceAttributeType.isUndefinedAllowed() && referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION) {
							// Das Objekt darf fehlen. Es muss eine Warnung ausgegeben werden.
							final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.WARNING,
									configArea,
									new SystemObject[]{systemObject, data.getAttributeType()},
									"Eine Objektreferenz (Assoziation und optional) (" + data.getName()
									+ ") kann nicht aufgelöst werden. Der Bereich, der das Objekt enthält, steht der Konfiguration nicht zur Verfügung. "
									+ getAttributeDescription(parentData, data)
							);
							errorObject.addEntry(entry);
							return true;
						}
						else {
							// Das Objekt muss da sein, wenn:
							// 1) Es sich um eine Komposition handelt

							// Das Objekt darf fehlen, wenn es sich um eine:
							// 2) Assoziation handelt, deren Datensatz optional ist (das wird im If-Zweig behandelt
							// 3) Aggregation handelt, deren Datensatz optional ist

							if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.LOCAL_ERROR,
										configArea,
										new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
										"Das referenzierte Objekt einer Referenz(Komposition) ist dem Konfigurationsbereich unbekannt. "
										+ getAttributeDescription(parentData, data)
								);
								errorObject.addEntry(entry);
								return false;
							}
							else {
								// Es kann nur noch 2) und 3) sein. Also muss an dieser Stelle der Datensatz optional sein.
								if(referenceAttributeType.isUndefinedAllowed() == false) {
									// Es muss eine Referenz vorhanden sein -> Interferenzfehler
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											configArea,
											new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
											"Das referenzierte Objekt einer Referenz(Aggregation/Assoziation) ist der Konfiguration nicht bekannt. "
											+ getAttributeDescription(parentData, data)
									);
									errorObject.addEntry(entry);
									return false;
								}
								else {
									// Es muss keine Referenz angegeben werden
									return true;
								}
							}
						}
					}
				}
				else {
					// Ist die undefinierte Referenz 0 erlaubt ? Wenn ja dann Ende.
					// Da bei einer Id mit 0 kein Objekt referenziert wird, kann es auch keine Abhängigkeit geben.
					if(referenceAttributeType.isUndefinedAllowed()) {
						// Es ist erlaubt
						return true;
					}
					else {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.getAttributeType()},
								"Eine Objektreferenz (" + data.getName() + ") ist undefiniert, obwohl dies laut Datenmodell hier nicht erlaubt ist. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}
				}

				final SystemObjectType referencedObjectType = referenceAttributeType.getReferencedObjectType();
				if(referencedObjectType != null) {
					if(_versionedView.isOfType(referencedObject, referencedObjectType) == false) {
						// Der Typ des referenzierten Objekts passt nicht zu dem geforderten Typen in der Referenz
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, referencedObjectType, referencedObject},
								"Der Typ eines referenzierten Objekts entspricht nicht dem Typ, der durch die Referenz gefordert wurde. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}
				}

				// Kommt der Algorithmus an diese Stelle, könnte eine Abhängigkeit gefunden worden sein.
				// Der Typ der Abhängigkeit ist abhängig von der Art der Referenzierung.
				// Bei einer Komposition kann es keine Abhängigkeiten geben, da das referenzierte Objekt im Bereich zu finden sein muss.
				if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
					// Es ist eine Komposition und das referenzierte Objekt ist vorhanden (das wurde vorher geprüft)
					// Vom data eine Referenz anfordern, der Name der Referenz ist der Name des data.
					// Der Konfigurationsbereich in dem sich die Referenz befindet, muss gleich der übergebene Referenz sein
					if(!data.asReferenceValue().getSystemObject().getConfigurationArea().equals(configArea)) {
						// Das Objekt, das referenziert wird, befindet sich nicht im geprüften Konfigurationsbereich.
						// Dies ist ein Fehler.
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
								"Zu einer Referenz(Komposition) kann das referenzierte Objekt nicht im Konfigurationsbereich gefunden werden, wie der konfigurierende Datensatz, der die Referenz enthält. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}
					// Ist das Objekt in der geprüften Version gültig?
					if(referencedObject instanceof ConfigurationObject) {
						if(configurationObjectAvailability((ConfigurationObject)referencedObject)) {
							return true;
						}
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
								"Das referenzierte Objekt einer Referenz(Komposition) ist in der geprüften Version nicht mehr gültig. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}
					// dynamische Objekte die via Komposition referenziert werden, sind nicht erlaubt
					if(referencedObject instanceof DynamicObject) {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
								"Das referenzierte Objekt einer Referenz(Komposition) ist ein dynamisches Objekt. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}

					// Kompositionen dürfen nur einmal durch ein Übergeordnetes Objekt referenziert werden
					if(!checkDoubleComponentUsage(referencedObject, referenceAttributeType, usedComponents, errorObject)) {
						// Es wurde ein Fehler gefunden
						return false;
					}
					return true;
				}
				else {
					// Es ist keine Komposition, also eine Aggregation/Assoziation.
					// In diesem Fall muss das referenzierte Objekt im Datenmodell gesucht werden (das referenzierte Objekt ist vorhanden, das wurde vorher geprüft).
					// Es kann zu Abhängigkeiten zwischen Bereichen kommen.

					// Es werden nur Konfigurationsobjekte referenziert
					if(referencedObject instanceof ConfigurationObject) {
						if(configurationObjectAvailability((ConfigurationObject)referencedObject) == false) {
							// Das Objekt ist nicht mehr gültig, also tritt ein Interferenzfehler auf

							//Spezialbehandlung für wechselnden Konfigurationsverantwortlichen eines Bereichs
							if(configArea.getId() == systemObject.getId() && parentData != null && parentData.getName().equals("atg.konfigurationsBereichEigenschaften") && data.getName().equals("zuständiger")) {
								final SystemObject newAuthority = parentData.getReferenceValue("neuerZuständiger").getSystemObject();
								if(newAuthority == null) {
									// Das Objekt ist nicht mehr gültig und kein neues angegeben
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											configArea,
											new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
											"Der bisherige Verantwortliche des Bereichs ist in der geprüften Version nicht mehr gültig und es ist "
											+ "kein neuer Verantwortliche angegeben. BITTE ERNEUT IMPORTIEREN. "
											+ getAttributeDescription(parentData, data)
									);
									errorObject.addEntry(entry);
									return false;
								}
								if(configurationObjectAvailability((ConfigurationObject)newAuthority) == false) {
									// Der neue Konfigurationsverantwortliche ist auch nicht gültig
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											configArea,
											new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject(), newAuthority},
											"Der bisherige Verantwortliche des Bereichs ist in der geprüften Version nicht mehr gültig und der neue Verantwortliche auch nicht. "
											+ getAttributeDescription(parentData, data)
									);
									errorObject.addEntry(entry);
									return false;
								}
							}
							else if(!referenceAttributeType.isUndefinedAllowed()) {
								// Das Objekt ist nicht mehr gültig, also tritt ein Interferenzfehler auf
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
										configArea,
										new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
										"Das referenzierte Objekt einer Referenz (Aggregation/Assoziation) ist in der geprüften Version nicht mehr gültig. "
										+ getAttributeDescription(parentData, data)
								);
								errorObject.addEntry(entry);
								return false;
							}
							else {
								// Das Objekt darf fehlen. Es muss eine Warnung ausgegeben werden.
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.WARNING,
										configArea,
										new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
										"Das referenzierte Objekt einer optionalen Referenz (Aggregation/Assoziation) ist in der geprüften Version nicht gültig. "
												+ getAttributeDescription(parentData, data)
								);
								errorObject.addEntry(entry);
								return true;
							}
						}
					}
//					else {
//						// Es handelt sich um ein dynamisches Objekt. Diese sind sofort gültig, können aber
//						// auch jederzeit ungültig werden. An dieser Stelle wäre die Konsistenzprüfung zu spät
//					}

					// Die Referenz ist gültig -> gibt es Abhängigkeiten
					if(referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION && referenceAttributeType.isUndefinedAllowed()) {
						checkDependency(systemObject, referencedObject, ConfigurationAreaDependencyKind.OPTIONAL);
					}
					else {
						checkDependency(systemObject, referencedObject, ConfigurationAreaDependencyKind.REQUIRED);
					}
					return true;
				}
			} // Es handelte sich um eine Referenz
			else {
				// Der genau Typ des Objekts ist nicht von Intresse, nur Referenzen werden geprüft.
				return true;
			}
		}
		else {
			// Es ist entweder eine Liste oder ein Array
			Iterator listOrArray = data.iterator();
			// Wird false, wenn eine Referenz nicht im übergebenen Konfigurationsbereich gefunden werden konnte.
			// Die Variable wird nur gesetzt, wenn ein Fehler aufgetreten ist
			boolean result = true;
			while(listOrArray.hasNext()) {
				if(!checkDataSetReferences(
						(Data)listOrArray.next(), data, configArea, dataModel, errorObject, systemObject, usedComponents
				)) {
					// Zu einer Referenz konnte kein Objekt im übergebenen Konfigurationsbereich gefunden werden
					result = false;
				}
			}
			return result;
		}
	}

	private String getAttributeDescription(final Data parentData, final Data data) {
		return ("Attribut: " + (parentData == null ? "" : parentData.getName() + "." ) + data.getName());
	}

	/**
	 * Prüft ob ein Objekt in der angegebenen Version gültig ist (das Objekt kann auch über die Version hinaus gültig sein).
	 *
	 * @param configurationObject Objekt, das geprüft werden soll
	 *
	 * @return true = Das Objekt ist in der angegebenen Version gültig, oder darüber hinaus; false = Das Objekt ist nicht gültig
	 */
	private boolean configurationObjectAvailability(
			ConfigurationObject configurationObject) {

		// Version rausfinden, in der der Konfigurationsbereich läuft, bzw. Version in der der Konfigurationsbereich laufen soll.
		final short version;
		if(_areaVersionMap.containsKey(configurationObject.getConfigurationArea())) {
			// Es gibt für den Konfigurationsbereich eine neue Version, die aktiviert werden soll
			version = _areaVersionMap.get(configurationObject.getConfigurationArea());
		}
		else {
			// Es gibt keine neue Version, der Bereich läuft weiter in seiner aktuellen Version.
			// Ist das Objekt derzeit gültig ?
			return configurationObject.isValid();
		}

		if((configurationObject.getNotValidSince() == 0) || (configurationObject.getNotValidSince() > version)) {
			// 0 bedeutet, dass der Zeitpunkt an dem das Objekt ungültig wird noch nicht gesetzt ist.
			// Ist die Version, in der das Objekt ungültig wird größer als die Version in der das Objekt gültig sein muss,
			// so ist es ebenfalls gütig.
			return true;
		}
		else {
			// Das Objekt, das referenziert wird, ist ungültig
			return false;
		}
	}

	/**
	 * Erzeugt den ersten Teil einer Fehlermeldung, die zu einem lokalen Fehler gehört. Der erste Teil ist bei jedem lokalen Fehler identisch. Der Fehlertext hat
	 * folgende Form.
	 * <p>
	 * "Text " "pid des Konfigurationsbereichs" " Text " "errorType" ". "
	 *
	 * @param configArea Konfigurationsbereich, in dem der lokale Fehler aufgetreten ist
	 * @param errorType  Art des Fehlers, der aufgetreten ist
	 *
	 * @return StringBuffer, in dem weitere Informationen eingetragen werden können
	 */
	private StringBuffer createLocalErrorMessagePartOne(
			ConfigurationArea configArea, String errorType) {
		final StringBuffer localErrorText = new StringBuffer(
				"Lokaler Fehler im Konfigurationsbereich " + configArea.getPid() + " gefunden, Art des Fehlers: " + errorType + ". "
		);
		return localErrorText;
	}

	/**
	 * Diese Methode ermittelt anhand eines Objekt-Typen seine sämtlichen Mengenverwendungen.
	 *
	 * @param systemObjectType der Objekt-Typ
	 *
	 * @return Alle Mengenverwendungen dieses Objekt-Typs.
	 */
	List<ObjectSetUse> getObjectSetUses(final SystemObjectType systemObjectType) {
		final List<ObjectSetUse> objectSetUses = new LinkedList<ObjectSetUse>();

		if(_areaVersionMap.containsKey(systemObjectType.getConfigurationArea())) {
			// Version, in der die Objekte gültig sein müssen
			final Short version = _areaVersionMap.get(systemObjectType.getConfigurationArea());

			// Der Typ soll in einer neuen Version aktiviert werden -> Die zu aktivierende Version muss betrachtet werden
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
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
		else {
			// Es kann die aktuelle Version des Bereichs benutzt werden
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Mengen").getElements();
			for(SystemObject systemObject : setUses) {
				final ObjectSetUse setUse = (ObjectSetUse)systemObject;
				objectSetUses.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElements();
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				objectSetUses.addAll(getObjectSetUses(objectType));
			}
		}
		return objectSetUses;
	}

	/**
	 * Alle Attributgruppen suchen (auch supertypen)
	 *
	 * @param systemObjectType
	 *
	 * @return ATG und Supertypen
	 */
	List<SystemObject> getAttributeGroups(final SystemObjectType systemObjectType) {
		final List<SystemObject> allATGs = new LinkedList<SystemObject>();

		if(_areaVersionMap.containsKey(systemObjectType.getConfigurationArea())) {
			// Version, in der die Objekte gültig sein müssen
			final Short version = _areaVersionMap.get(systemObjectType.getConfigurationArea());

			// Der Typ soll in einer neuen Version aktiviert werden -> Die zu aktivierende Version muss betrachtet werden
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> atgs = systemObjectType.getNonMutableSet("Attributgruppen").getElementsInVersion(version);
			for(SystemObject systemObject : atgs) {
				final AttributeGroup atg = (AttributeGroup)systemObject;
				allATGs.add(atg);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElementsInVersion(version);
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				allATGs.addAll(getAttributeGroups(objectType));
			}
		}
		else {
			// Es kann die aktuelle Version des Bereichs benutzt werden
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Attributgruppen").getElements();
			for(SystemObject systemObject : setUses) {
				final AttributeGroup setUse = (AttributeGroup)systemObject;
				allATGs.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null geprüft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElements();
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				allATGs.addAll(getAttributeGroups(objectType));
			}
		}
		return allATGs;
	}

	/**
	 * Diese Methode sucht für ein Objekt die aktive Version. Befindet sich das Objekt in einem Bereich, der aktiviert werden soll, ist die aktive Version die
	 * Version in der der Bereich aktiviert werden soll. Wird der Bereich nicht mit einer neuen Version aktiviert, so wird die derzeit aktive Version
	 * zurückgegeben.
	 *
	 * @param systemObject Objekt, zu dem die aktive Version des Bereich ermittelt werden soll
	 *
	 * @return Version, in der der Bereich nach (angenommener) erfolgreicher Konsistenzprüfung laufen wird
	 */
	private short getActiveVersion(SystemObject systemObject) {
		final ConfigurationArea configurationArea = systemObject.getConfigurationArea();
		if(_areaVersionMap.containsKey(configurationArea)) {
			return _areaVersionMap.get(configurationArea);
		}
		else {
			return configurationArea.getActiveVersion();
		}
	}

	/**
	 * Diese Methode gibt die Version zurück, in der eine Abhängigkeit zu dem übergebenen Objekt besteht. Wird ein Konfigurationsobjekt übergeben, so wird die
	 * Version zurückgegeben, in der das Objekt erzeugt wurde.
	 * <p>
	 * Wird ein dynamisches Objekt übergeben, so wird die Version gesucht, in der das dynamische Objekt erzeugt wurde und diese zurückgegeben. (Ein dynamisches
	 * Objekt entsteht zu einem Zeitpunkt, zu diesem Zeitpunkt muss eine Version aktiv gewesen sein, diese wird zurückgegeben.)
	 *
	 * @param systemObject Objekt, dessen Version gefunden werden soll
	 *
	 * @return Version, in der das Objekt benötigt wird um eine Abhängigkeit aufzulösen.
	 */
	private short getDependenceVersion(final SystemObject systemObject) {
		if(systemObject instanceof ConfigurationObject) {
			ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
			return configurationObject.getValidSince();
		}
		else {
			final DynamicObject dynamicObject = (DynamicObject)systemObject;
			// Zeitpunkt, an dem das Objekt gültig geworden ist.
			final long validSinceTime = dynamicObject.getValidSince();

			final ConfigDataModel configDataModel = (ConfigDataModel)dynamicObject.getDataModel();
			final ConfigAreaFile areaFile = (ConfigAreaFile)(configDataModel.getConfigurationFileManager().getAreaFile(dynamicObject.getConfigurationArea().getPid()));

			short neededVersion = areaFile.getActiveVersion(validSinceTime, ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME);

			// Wenn der Bereich des referenzierten Objekts noch nicht aktiviert war, dann wird 1 als mindestens benötigte Version zurückgegeben
			if(neededVersion <= 0) neededVersion = 1;

			return neededVersion;
		}
	}
}
