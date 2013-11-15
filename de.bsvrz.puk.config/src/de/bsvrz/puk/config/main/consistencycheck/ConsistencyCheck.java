/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
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
 * Die Klasse �bernimmt die Konsistenzpr�fung, wie sie in TPuK1-138,139,140,141 gefordert wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
public class ConsistencyCheck {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	private final ConfigAreaAndVersion[] _consistencyCheckDefinition;

	/** Datenmodell f�r die �bergebene Bereiche */
	private final ConfigDataModel _dataModel;

	/** Hilfsobjekt, das bei Zugriffen auf Konfigurationsdaten die Konfigurationsbereiche in vorgegebenen Versionen betrachtet */
	private final VersionedView _versionedView;

	/**
	 * Speichert zu einem Konfigurationsbereich die Version, mit der der Bereich aktiviert werden soll. Kann der Bereich in der Map nicht gefunden werden, wird der
	 * Bereich in der Version, in der er derzeit l�uft, weiter laufen. Als Schl�ssel dient der Konfigurationsbereich, als Ergebnis wird die Version, mit der der
	 * Konfigurationsbereich in der Zukunft laufen soll, zur�ckgegeben.
	 * <p/>
	 * Anmerkung: Es werden nur die Bereiche eingetragen, die auch im Konstruktor �bergeben wurden.
	 */
	private final Map<ConfigurationArea, Short> _areaVersionMap = new HashMap<ConfigurationArea, Short>();

	/**
	 * Speichert zu einem Konfigurationsbereich die Abh�ngigkeiten zu anderen Konfigurationsbereichen.
	 * <p/>
	 * Key = Bereich, f�r den Abh�ngigkeiten gefunden wurden.
	 * <p/>
	 * Value = Menge mit allen gefundenen Abh�ngigkeiten
	 */
	private final Map<ConfigurationArea, Set<ConfigurationAreaDependency>> _areasDependencies = Collections.synchronizedMap(new HashMap<ConfigurationArea, Set<ConfigurationAreaDependency>>());

	/** Objekt-ID des lokalen Konfigurationsverantwortlichen oder 0, falls der lokale Konfigurationsverantwortliche nicht bestimmt werden kann */
	private long _configurationAuthorityId = 0;

	/**
	 * Flag, das festlegt, ob die Abh�ngigkeiten zwischen Bereichen geschrieben und gepr�ft werden sollen. Es gesetzt wird, wenn das Metamodell in Version 9 oder
	 * h�her vorliegt.
	 */
	private boolean _storeDependencies = false;

	/**
	 * Erstellt ein Objekt, das vorgegebene Konfigurationsbereiche einer Konsistenzpr�fung unterzieht.
	 *
	 * @param consistencyCheckDefinition Pid�s aller Konfigurationsbereiche, die in einer neuen Version gepr�ft werden sollen. Zu jedem Konfigurationsbereich ist
	 *                                   ausserdem die Version gespeichert, die aktiviert werden soll. Ist die Version 0, so wird die gr��te zu verwendene Version
	 *                                   gesucht. Die ModifiableVersion darf nur dann ber�cksichtigt werden, wenn es auch Elemente gibt die in der
	 *                                   ModifiableVersion ge�ndert werden w�rden. Die anderen Bereiche, die nicht �bergeben wurden, werden in der aktuellen
	 *                                   Version gepr�ft.
	 * @param dataModel                  Datenmodell, mit dem die �bergebenen Bereich gepr�ft werden sollen
	 */
	public ConsistencyCheck(ConfigAreaAndVersion[] consistencyCheckDefinition, ConfigDataModel dataModel) {
		_consistencyCheckDefinition = consistencyCheckDefinition;

		_dataModel = dataModel;
		final ConfigurationAuthority authority = _dataModel.getConfigurationAuthority();
		if(authority != null) {
			_configurationAuthorityId = authority.getId();
		}

		// Abh�ngigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		final ConfigurationArea metaModelArea = _dataModel.getConfigurationArea("kb.metaModellGlobal");
		if(metaModelArea != null && metaModelArea.getActiveVersion() >= 9) _storeDependencies = true;

		// Speichert zu den Bereichen die neue Version, in der der Bereich laufen soll.
		// Ist die Version des Bereichs 0, so wird die gr��te zu verwendene Version gesucht.
		// Die ModifiableVersion darf nur dann ber�cksichtigt werden, wenn es auch Elemente gibt die
		// in der ModifiableVersion g�ltig werden w�rden. bzw. gel�scht w�rden.

		for(ConfigAreaAndVersion checkDefinition : consistencyCheckDefinition) {
//			if(checkDefinition.getVersion() > 0) {
			_areaVersionMap.put(checkDefinition.getConfigArea(), checkDefinition.getVersion());
//			}
//			else {
//				// Version 0, also eine Version w�hlen
//				final ConfigConfigurationArea configArea = (ConfigConfigurationArea)checkDefinition.getConfigArea();
//				_areaVersionMap.put(configArea, configArea.getLastVersionObjectModified());
//			}
		}
		_versionedView = new VersionedView(dataModel, _areaVersionMap);
	}

	/**
	 * Diese Methode f�hrt eine Konsistenzpr�fung f�r alle(aktive, im Konstruktor �bergebene, nur in den Verwaltungsinformationen) Konfigurationsbereiche durch.
	 * <p/>
	 * Die Version mit der der Bereich gepr�ft wird, wurde entweder im Konstruktor �bergeben oder ist die aktuelle Version des Bereichs.
	 * <p/>
	 * Die Methode blockiert, bis ein Ergebnis vorliegt.
	 *
	 * @param kindOfConsistencyCheck Bestimmt wie mit Abh�ngigkeiten zwischen Konfigurationsbereichen umgegangen wird. Bei einer einfachen Konsistenzpr�fung werden
	 *                               die Abh�ngigkeiten zwischen den Bereichen zwar erkannt, aber nicht mittels D�tens�tzen am Bereich gespeichert. Bei einer
	 *                               Freigabe zur �bernahme (die auch Interferenzfehler verzeiht) werden wiedrum Abh�ngigkeiten gespeichert, die bei einer lokalen
	 *                               Aktivierung nicht gespeichert werden w�rden.
	 *
	 * @return Objekt, das das Ergebnis der Konsistenzpr�fung enth�lt und im Fehlerfall die unterschiedlichen Fehlermeldungen zur�ckgibt.
	 */
	public ConsistencyCheckResultInterface startConsistencyCheck(final KindOfConsistencyCheck kindOfConsistencyCheck) {
		// Speichert Fehler und Warnungen, die aufgetreten sein k�nnen
		final ConsistencyCheckResult result = new ConsistencyCheckResult();
		try{
			if(_storeDependencies) {
				// Pr�fung Abh�ngigkeiten der zu betrachtenden Bereiche
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
					final String message = "Fehler bei der erneuten Pr�fung der Abh�ngigkeiten zwischen Konfigurationsbereichen in den durch die "
					                       + "Konsistenzpr�fung zu betrachtenden Versionen";
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
						// Es sind einige Abh�ngigkeiten zwischen den Bereichen nicht erf�llt. Allerdings sind diese Abh�ngigkeiten optional und f�hren deshalb zu einer
						// Warnung.
						final Set<ConfigurationArea> areas = occuredOptionalErrors.keySet();
						// Alle Bereich, deren Abh�ngigkeiten nicht erf�llt sind (optionale Abh�ngigkeit)

						for(ConfigurationArea area : areas) {
							final List<ConfigurationAreaDependency> dependencies = occuredOptionalErrors.get(area);

							final StringBuffer text = new StringBuffer();
							text.append(
									"Der Bereich " + area.getPid()
									+ " besitzt folgende optionale Abh�ngigkeiten zu anderen Bereichen:"
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
						}// alle optionalen Abh�ngigkeiten
					}

					// Alle Abh�ngigkeiten, die ben�tigt werden, aber nicht vorhanden sind.
					final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredNeededDependencyErrors = dependencyCheckResult.getNeededDependencyErrors();
					if(occuredNeededDependencyErrors.isEmpty() == false) {
						// Es fehlen Bereiche, die gebraucht werden. Bereiche ausgeben und eine Exception werfen, da in diesem Fall die Konfiguration nicht gestartet werden
						// darf.

						final Set<ConfigurationArea> areas = occuredNeededDependencyErrors.keySet();

						for(ConfigurationArea area : areas) {
							final StringBuffer text = new StringBuffer();
							text.append(
									"Der Bereich " + area.getPid() + " ben�tigt folgende Bereiche in den angegebenen Versionen:" + "\n"
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
						}// alle ben�tigen Bereiche
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
			// Alle Bereiche anfordern. Es m�ssen alle Bereiche in der jeweiligen Version (entweder aktuell oder wie im
			// Konstruktor gefordert) gepr�ft werden

			// Z�hlt der wievielete Bereiche gerade gepr�ft wird. Dies dient nur f�r die Ausgabe
			int configAreaCounter = 1;
			final int numberOfAreas = _dataModel.getAllConfigurationAreas().values().size();

			// Diese Map speichert zu jeder Pid das dazugeh�rige Konfigurationsobjekt und zwar �ber alle Konfigurationsbereiche.
			// Als Key dient die Pid, als Value wird das Objekt mit der entsprechenden Pid zur�ckgegeben.
			// Mit dieser Map kann erkannt werden, ob es zu einem Objekt bereits ein zweites Objekt mit identischer Pid gibt.
			final Map<String, SystemObject> pidsFromAllAreas = new HashMap<String, SystemObject>();

			// Speicher alle Ids aller aktiven Objekten. (Aktiv: Das Objekt ist in der zu pr�fenden Version des jeweilgen Bereichs aktiv)
			// Als Schl�ssel dient die Id des Objekt. Als Value wird eine Liste zur�ckgegeben mit allen Objekten, die die gleiche Id haben.
			final Map<Long, List<SystemObject>> idsFromAllAreas = new HashMap<Long, List<SystemObject>>();

			final SystemObject defaultParameterAtg = _versionedView.getObject("atg.defaultParameterdatens�tze");
			if(!(defaultParameterAtg instanceof AttributeGroup)) {
				throw new IllegalStateException(
						"atg.defaultParameterdatens�tze wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
				);
			}
			final SystemObject defaultParameterAspect = _versionedView.getObject("asp.eigenschaften");
			if(!(defaultParameterAspect instanceof Aspect)) {
				throw new IllegalStateException(
						"atg.defaultParameterdatens�tze wurde nicht gefunden, vermutlich ist der Bereich kb.systemModellGlobal nicht oder in der falschen Version vorhanden"
				);
			}
			final AttributeGroupUsage defaultParameterUsage = ((AttributeGroup)defaultParameterAtg).getAttributeGroupUsage((Aspect)defaultParameterAspect);

			final Collection<ConfigurationObject> hierarchyObjects = new ArrayList<ConfigurationObject>();

			for(ConfigurationArea verifyingConfigArea : ((ConfigDataModel)_dataModel).getAllConfigurationAreas().values()) {

				final short lastActiveVersion = verifyingConfigArea.getActiveVersion();
				// Speichert alle Objekte, die zu einen "zu pr�fenden" Konfigurationsbreich geh�ren (aktuelle und zuk�nftig aktuelle)
				final Collection<SystemObject> configAreaObjects = new ArrayList<SystemObject>();
				// aktuelle Objekte des zu pr�fenden Bereichs anfordern
				configAreaObjects.addAll(verifyingConfigArea.getCurrentObjects());
				// zuk�nftig aktuelle Objekte des Bereichs anfordern
				configAreaObjects.addAll(verifyingConfigArea.getNewObjects());

				_debug.info(
						"Pr�fe Bereich " + configAreaCounter + " von insgesamt " + numberOfAreas + " Bereichen. Bereich, der gepr�ft wird: '"
						+ verifyingConfigArea.getPidOrNameOrId() + "' Anzahl Objekte des Bereichs: " + configAreaObjects.size() + " Anzahl bisheriger Fehler: "
						+ (result.getInterferenceErrors().size() + result.getLocalErrors().size()) + " Anzahl bisheriger Warnungen: " + result.getWarnings().size()
				);
				configAreaCounter++;

				// Version, mit der gepr�ft werden soll, ob alle Objekte des Bereichs konsistent sind
				final short verifyingVersion = getActiveVersion(verifyingConfigArea);

				// Diese Liste speichert alle Objekte, die in der geforderten Version des Konfigurationsbreichs g�ltig sind/sein werden und alle g�ltigen dynamischen Objekte.
				// Diese Liste erm�glicht es, bei den sp�teren Pr�fungen auf lokale Fehler nur die Objekte zu betrachten,
				// die sich wirklich ge�ndert haben k�nnen.
				// Es werden auch die aktuellen Objekte gepr�ft, die g�ltig bleiben. Dies ist n�tig, weil sich Referenzen dieser Objekte ge�ndert haben k�nnen.

				final Collection<SystemObject> checkObjects = new ArrayList<SystemObject>();

				//**********************************************************************************************************

				// Pr�fung 1:
				// Die Pid eines Objekts muss eindeutig in einer Version sein.
				// Falls ein ConfigurationObject in der altuellen Version g�ltig ist und es gibt in der zu pr�fenden Version
				// ebenfalls ein neues Objekt, das in der zu pr�fenden Version g�ltig werden w�rde, so muss ein Fehler ausgegeben werden.
				// In diesem Fall w�rden 2 Objekte mit einer Pid g�ltig sein.

				// Speichert zu jeder Pid alle Objekte der Pid. Key = Pid(String) Value = Liste, die alle Objekte mit
				// der Pid enth�lt (es werden nur Objekte gespeichert, die derzeit aktuell sind oder in der zu pr�fenden Version aktuell werden).
				// Die Liste ist n�tig, weil die Pid als hashCode(integer 4 Byte) in der Map benutzt wird. Der String kann aber 64 Zeichen haben, es kann also
				// sehr leicht zu Kollisionen kommen.
				final Map<String, List<SystemObject>> pidMap = new HashMap<String, List<SystemObject>>();

				/**
				 * Diese Map speichert zu jeder Komponenten im Sinne von TPuK1-29 Komposition ab, wo diese Komponente bereits benutzt wurde. Jede Komponente darf nur
				 * einmal durch ein �bergeordnetes Objekt verwendet werden verwendet werden.
				 *
				 * Als Key dient die Komponente. Als Value wird eine Liste zur�ckgegeben, die alle Objekte beinhaltet, in denen die Komponente bereits verwendet wurde.
				 * Wurde die Komponente richtig verwendet, so
				 */
				final Map<SystemObject, List<SystemObject>> usedComponent = new HashMap<SystemObject, List<SystemObject>>();

//			System.out.println("Anzahl zu pr�fender Objekte: " + configAreaObjects.size());

				// Alle Objekte(jetzt g�ltig und alle die in der zukunft g�ltig werden) des Konfigurationsbereichs betrachten

				int counter2 = 1;

				for(SystemObject configAreaObject : configAreaObjects) {

					if(counter2 % 100 == 0) {
//					_debug.info("Pr�fe Objekt " + counter2 + " von " + configAreaObjects.size());
					}
					counter2++;

					// Wird true, wenn die Pid des Objekts gepr�ft werden muss
					boolean checkPid = false;

					if(configAreaObject instanceof ConfigurationObject) {

						final ConfigurationObject configurationObject = (ConfigurationObject)configAreaObject;

						// Es werden nur Objekte betrachtetet, die g�ltig sind oder mit der zu pr�fenden Version
						// g�ltig w�rden. Damit bleiben nur noch Objekte, die mit einer Version nach der zu pr�fenden Version
						// g�ltig werden w�rden (also noch weiter in der Zukunft liegen). Diese werden nicht betrachtet, da
						// sie f�r die zu pr�fende Version keine Rolle spielen (sie werden mit der zu pr�fenden Version nicht
						// aktiviert, somit ist es egal, ob sie Konsistent sind. Sollen diese Objekte gepr�ft werden, so
						// m��te mit der Version der Objekte die Konsistenzpr�fung gestartet werden).

						final short validSince = configurationObject.getValidSince();
						final short notValidSince = configurationObject.getNotValidSince();

						// Pr�fungen, die f�r alle (in der zu betrachtenden Version) g�ltigen Konfigurationsobjekte durchgef�hrt werden:
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
										// Kein Defaultwert vorhanden, also Undefined-Wert pr�fen
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
						// dynamische Objekte sind sofort g�ltig und auch sofort ung�ltig, sie sind nicht an
						// Versionen gebunden.
						// Sie m�ssen also immer gepr�ft werden, wenn sie noch g�ltig sind.
						checkObjects.add(configAreaObject);
						checkPid = true;
						checkDoublePidsInDifferntAreas(configAreaObject, pidsFromAllAreas, result);
						checkDoubleIdsInDifferentAreas(configAreaObject, idsFromAllAreas, result);
					}

					if(checkPid && !configAreaObject.getPid().equals("")) {
						// In die Map der Pids eintragen, gibt es dort bereits einen Eintrag f�r diese Pid, dann wurde
						// ein lokaler Fehler gefunden.
						synchronized(pidMap) {
							List<SystemObject> elementsWithPid = pidMap.get(configAreaObject.getPid());
							if(elementsWithPid == null) {
								// Es gibt noch keine Liste, also Liste erzeugen, Element eintragen.
								// Wenn kein Fehler auftritt, sollte das Array immer die Gr��e 1 besitzen.
								// Es wird 2 vorgegeben, damit nicht sofort beim ersten Eintrag in das Array, ein
								// Arracopy angestossen wird.
								elementsWithPid = new ArrayList<SystemObject>(2);
								elementsWithPid.add(configAreaObject);
								// Liste in Map eintragen
								pidMap.put(configAreaObject.getPid(), elementsWithPid);
							}
							else {
								// Es gibt bereits Elemente in der Liste, es m�ssen die Pids verglichen werden.
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
								// Das gepr�fte Objekt wird in jedem Fall in die Liste aufgenommen
								// um Folgefehler anzuzeigen
								elementsWithPid.add(configAreaObject);
							}
						}
					}
				}

				//**********************************************************************************************************

				// Alle folgenden Pr�fungen m�ssen auf jedes Element der Liste "checkObjects" durchgef�hrt werden
				for(SystemObject systemObject : checkObjects) {

					// Pr�fung, ob der Typ des Objekts in der zu pr�fenden Version g�ltig ist.
					final SystemObjectType objectTypeOfVerifiedObject = systemObject.getType();
					if(!_versionedView.isValid(objectTypeOfVerifiedObject)) {
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
								verifyingConfigArea,
								new SystemObject[]{systemObject, objectTypeOfVerifiedObject},
								"Der Typ eines (in der zu pr�fenden Version) g�ltigen Objekts ist (in der zu pr�fenden Version) nicht g�ltig."
						);
						result.addEntry(entry);
					}

					// Wenn der Typ sich in einem anderen Bereich befindet als der zu pr�fende Typ, wurde eine Abh�ngigkeit gefunden.
					checkDependency(systemObject, objectTypeOfVerifiedObject, ConfigurationAreaDependencyKind.REQUIRED);

					//**********************************************************************************************************
					// Pr�fung 2:
					// a) Zu jeder Menge an einem Objekt gibt es beim Typ des Objekts eine entsprechende Mengenverwendung.
					// b) Jede Menge, die in der Mengenverwendung gefordert ist, muss auch am Objekt gefunden werden.
					// c) Pa�t die Anzahl der Elemente in der Menge
					// d) Sind die Elemente in der Menge vom richtigen Typ
					// e) Ist das Objekt der Menge eine Objektreferenz(Komposition), so muss gepr�ft werden, ob sich das referenzierte Objekt im Konfigurattionsbereich befindet (Dies ist nur Teil 1 der Pr�fung, da auch Objekte andere Objekt referenzieren k�nnen)
					// f) Wenn ein Element der Menge eine Komposition ist, so darf dieses Element nur von der einen Menge referenziert werden
					// g) Nachtrag: Da sich das Element der Menge in einem anderen Bereich befinden kann, entstehen dadurch Abh�ngigkeiten. Diese m�ssen erkannt
					// und gegebenenfalls gespeichert werden.

					if(systemObject instanceof ConfigurationObject) {
						// Mengen des Objekts
						final List<ObjectSet> objectSets = ((ConfigurationObject)systemObject).getObjectSets();

						// Mengenverwendungen des Types, die zu dem gerade betrachteten Objekt geh�rt

						// Die Mengenverwendung des Typs muss nicht in der aktuelle Version aktiv sein, sondern kann erst mit der Version aktiviert
						// werden, die gerade gepr�ft wird. Aus diesem Grund muss ein "getElementsInVersion" auf eine Menge benutzt werden.
						final List<ObjectSetUse> objectSetUses = getObjectSetUses(objectTypeOfVerifiedObject);

//					System.out.println("Objekt " + systemObject.getPid() + " Alle Mengenverwendungen des Typs: " + objectSetUses);

						// Diese Liste speichert alle Mengenverwendungen, die einer Menge am Objekt zugeordnet werden konnten.
						// Diese Objekte m�ssen nicht noch einmal betrachtet werden
						final Set<ObjectSetUse> objectSetUsesFound = Collections.synchronizedSet(new HashSet<ObjectSetUse>());

						// Jede Menge am Objekt muss in der Definition gefunden werden und umgekehrt. Das entspricht a) und b)
						for(final ObjectSet objectSet : objectSets) {
							// Menge sollte in der zu pr�fenden Version auch g�ltig sein
							final boolean objectSetIsValid = _versionedView.isValid(objectSet);
							if(!objectSetIsValid) {
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
										verifyingConfigArea,
										new SystemObject[]{systemObject, objectSet},
										"An einem (in der zu pr�fenden Version) g�ltigen Objekt gibt es eine (in der zu pr�fenden Version) nicht g�ltige Menge."
								);
								result.addEntry(entry);
							}

							//getType
//						System.out.println("");
//						System.out.println("	 Menge am Objekt: " + objectSet.getName() + " Typ: " + objectSet.getObjectSetType());
							// wird true, wenn zu der Menge am Objekt, eine Mengenverwendung gefunden wurde, die die Menge ebenfalls enth�lt
							boolean objectSetUseFound = false;
							for(ObjectSetUse objectSetUse : objectSetUses) {
//							System.out.println("	Mengenverwendung: " + objectSetUse.getObjectSetName() + " Typ: " + objectSetUse.getObjectSetType());

								if((objectSetUse.getObjectSetType().equals(objectSet.getObjectSetType()))
								   && (objectSetUse.getObjectSetName().equals(objectSet.getName()))) {
									// Gegenst�ck gefunden
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

									// Alle Elemente der Menge in Abh�ngigkeit vom Typ der Menge (Mutable, NonMutable)
									final List<SystemObject> elementsOfSet;

									if(objectSetUse.getObjectSetType().isMutable()) {
										// Eine dynamische Menge, diese kann sich jederzeit �ndern. Also nur die aktuellen anfordern.
										elementsOfSet = objectSet.getElements();
									}
									else {
										// Bei NonMutableSets darf sich die Mengenzusammenstellung zur Laufzeit nicht �ndern.
										// Es ist egal ob Assoziation oder Komposition/... ist.
										final NonMutableSet nonMutableSet = (NonMutableSet)objectSet;
										// Es muss die Version benutzt werden, in der die Menge aktiv ist bzw. in der sie aktiv gesetzt werden soll
										elementsOfSet = nonMutableSet.getElementsInVersion(getActiveVersion(nonMutableSet));

										// Die Elemente von g�ltige Mengen m�ssen g�ltige Objekte sein
										if(objectSetIsValid) {
											for(SystemObject object : elementsOfSet) {
												if(!_versionedView.isValid(object)) {
													final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
															ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
															verifyingConfigArea,
															new SystemObject[]{systemObject, objectSet, object},
															"An einer (in der zu pr�fenden Version) g�ltigen Menge gibt es mindestens ein (in der zu pr�fenden Version) nicht g�ltiges Objekt."
													);
													result.addEntry(entry);
													break;
												}
											}
										}
									}

//								System.out.println("Mengenverwendung Name: " + objectSetUse.getObjectSetName() + "Minimum: " + objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum: " + objectSetUse.getObjectSetType().getMaximumElementCount() + " Anzahl Elemente: " + elementsOfSet.size());

									// Pa�t die Anzahl der Elemente? Dies entspricht c)
									if((objectSetUse.getObjectSetType().getMinimumElementCount() > elementsOfSet.size())) {
										// Es sind weniger Objekte in der Menge als vorgeschrieben -> Fehler

										// Unterscheiden, ob der maximale Wert gesetzt wurde. Dies wird f�r den Fehlertext ben�tigt
										if(objectSetUse.getObjectSetType().getMaximumElementCount() > 0) {
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet},
													"Eine Menge enth�lt nicht die geforderte Anzahl Elemente: minimum "
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
													"Eine Menge enth�lt nicht die geforderte Anzahl Elemente: minimum "
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
											// die Anzahl Elemente war gr��er als die maximal erlaubte Menge -> Fehler
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet},
													"Eine Menge enth�lt nicht die geforderte Anzahl Elemente: minimum "
													+ objectSetUse.getObjectSetType().getMinimumElementCount() + " maximum "
													+ objectSetUse.getObjectSetType().getMaximumElementCount() + " Anzahl Elemente in der Menge "
													+ elementsOfSet.size()
											);
											result.addEntry(entry);
										}
									}

									// Passen die Elemente in der Menge mit ihrem Objekttyp zu den erlaubten Objekttypen der Menge.
									// Oder gibt es Elemente, die gar nicht in der Menge sein d�rften? Dies entspricht d)

									// Alle Objekttypen, die als Elemente in der Menge vorhanden sein d�rfen -> Diese Objekttypen m�ssen
									// in der Version aktiv sein, in der dieser Bereich aktiviert werden soll

									final NonMutableSet nonMutableSet = (NonMutableSet)objectSetUse.getObjectSetType().getObjectSet("ObjektTypen");

									// M�ssen noch gecastet werden
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

										// Fall g) (Auf Abh�ngigkeiten pr�fen)
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
										// Pr�fen, ob das Element der Menge vom richtigen Typ ist
										for(SystemObjectType systemObjectType : requirededObjectTypes) {
											if(_versionedView.isOfType(setElement, systemObjectType)) {
												objectTypeFound = true;
												// Das Element hat die richtige Mengenverwendung. Ist die Mengenverwendung eine Komposition
												// so muss das Objekt auch im Konfigurationsbereich vorhanden sein. Dies entspricht e)
												if(composition) {
													if(!setElement.getConfigurationArea().equals(verifyingConfigArea)) {
														// Der Konfigurationsbereich des Objekts ist ein anderer als der
														// gepr�fte Bereich. Also ist das Objekt dem gepr�ften Bereich
														// unbekannt -> Fehler, laut e)
														final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
																ConsistencyCheckResultEntryType.LOCAL_ERROR,
																verifyingConfigArea,
																new SystemObject[]{systemObject, setElement},
																"Das referenzierte Objekt einer Referenz(Komposition) befindet sich nicht im gleichen Konfigurationsbereich, wie der konfigurierende Datensatz, der die Referenz enth�lt"
														);
														result.addEntry(entry);
													}
													if(setElement instanceof DynamicObject) {
														// Der Konfigurationsbereich des Objekts ist ein anderer als der
														// gepr�fte Bereich. Also ist das Objekt dem gepr�ften Bereich
														// unbekannt -> Fehler, laut e)
														final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
																ConsistencyCheckResultEntryType.LOCAL_ERROR,
																verifyingConfigArea,
																new SystemObject[]{systemObject, setElement},
																"Das referenzierte Objekt einer Referenz(Komposition) ist ein dynamisches Objekt"
														);
														result.addEntry(entry);
													}

													// f) pr�fen: Das Element darf nur von dieser Menge referenziert werden
													checkDoubleComponentUsage(setElement, objectSet, usedComponent, result);
												}
												// Der Typ wurde gefunden
												break;
											}
										} // for �ber alle Typen, die in der Menge erlaubt sind

										if(!objectTypeFound) {
											// Der Typ des Elements der Menge ist nicht in der Mengeverwendung mit den erlaubten Objekttypen zu finden
											// (dieser Fall kann nicht vorkommen, da dies bereits beim hinzuf�gen in die Menge gepr�ft wird)
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													ConsistencyCheckResultEntryType.LOCAL_ERROR,
													verifyingConfigArea,
													new SystemObject[]{systemObject, objectSetUse, objectSet, setElement},
													"Ein Element der Menge besitzt einen Typ, der nicht in der Mengenverwendung aufgef�hrt ist"
											);
											result.addEntry(entry);
										}
									} // for �ber alle Elemente einer Menge

									// Es wurde eine Mengeverwendng gefunden (es kann aber trotzdem zu Fehlern gekomme sein,
									// die spielen aber f�r dieses true keine Rolle)
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

						// Es wurden alle Mengen eines Objekts gepr�ft, sind aber auch alle Mengen vorhanden, die in der Mengeverwendung
						// vorhanden sein m�ssen ? Dies entspricht b)
						// Zu jeder Mengenverwendung(falls gefordert) muss eine Menge am Objekt gefunden werden
						for(ObjectSetUse objectSetUse : objectSetUses) {
							// Es wurden bereits Objekte aus den Mengenverwendungen betrachtet. Diese wurden in einer Menge gespeichert
							// und m�ssen nicht noch einmal betrachtet werden.
							if(!objectSetUsesFound.contains(objectSetUse)) {
								// Diese Mengenverwendung wurde noch nicht bearbeitet
								if(objectSetUse.isRequired()) {

									if(systemObject instanceof DynamicObjectType && "menge.mengenVerwendungen".equals(objectSetUse.getObjectSetType().getPid())) {
										// Dieser Fall ist "ok", sobald das Datenmodell ge�ndert wurde (menge nicht mehr erforderlich), kann
										// das If-Konstrukt raus
									}
									else {
										// Da jede Menge am Objekt bereits betrachtet wurde, darf es keine Mengenverwendung mehr geben
										// die nicht betrachtet wurde aber ben�tigt wird.
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
					// Pr�fung 3:
					// a) Alle Laut Attributgruppenverwendung notwendigen konfigurierenden Datens�tze sind bei den Objekten vorhanden
					// b) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Komposition besitzt, m�ssen
					// alle konfigurierenden Datens�tze gepr�ft werden, ob sie auf Objekte verweisen, die im Konfigurationsbereich vorhanden sind.
					// c) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Aggregation/Assoziation enth�lt, muss gepr�ft werden
					// ob das referenzierte Objekt dem Datenmodell bekannt ist, falls nicht, wurde ein Interferenzfehler gefunden.
					// d) Falls in einer ATG ein Attribut vorhanden ist, das als ReferenzeTyp Aggregation/Assoziation enth�lt, muss gepr�ft werden
					// ob alle referenziereten Objekte, die nicht undefinierten Referenzen entsprechen vorhanden sind (siehe c).
					// e) Nachtrag: Alle benutzten ATG-Verwendungen am Objekt m�ssen auch laut Definition erlaubt sein
					// f) Wenn eine Referenz vom Typ "Komposition" ist, darf dieses Objekt nur von einem Datensatz referenziert werden (siehe b))
					// g) Wenn ein Objekt referenziert wird, dann muss das referenzierte Objekt den richtigen Typen besitzen (dieser wird an der Referenz festgelegt)
					// h) Die durch den Attributtyp festgelegten Attributwerte m�ssen eingehalten werden

					// Alle Attributgruppen des Objekts
//				final List<AttributeGroup> objectTypeATGs = systemObject.getType().getAttributeGroups();
//				final List<AttributeGroup> objectTypeATGs = systemObject.getType().getDirectAttributeGroups();

					final NonMutableSet attributeSet = (NonMutableSet)objectTypeOfVerifiedObject.getObjectSet("Attributgruppen");

					// Die Elemente der Menge anfordern. Die Menge kann in einem anderen Konfigurationsbereich liegen, also
					// muss gepr�ft werden, in welcher Version der Bereich mit der Menge l�uft.
					// Aus dieser Version m�ssen dann die Elemente angefordert werden.
					final List<SystemObject> objectTypeATGs = getAttributeGroups(objectTypeOfVerifiedObject);

//				if(_areaVersionMap.containsKey(attributeSet.getConfigurationArea())) {
//					// Der Bereich mit der Menge soll in einer neuen Version laufen
//					objectTypeATGs = attributeSet.getElementsInVersion(_areaVersionMap.get(attributeSet.getConfigurationArea()));
//				}
//				else {
//					// Der Bereich l�uft in der aktuellen Version weiter
//					objectTypeATGs = attributeSet.getElements();
//				}

					// Speichert alle ATG-Verwendungen, die an diesem Objekt benutzt wurden. Alle ATG-Verwendungen, die benutzt wurden,
					// m�ssen auch erlaubte ATG-Verwendungen sein. (3 e))
					final Collection<AttributeGroupUsage> usedAttributeGroupUsages = systemObject.getUsedAttributeGroupUsages();

					// Hier werden alle erlaubten ATG-Verwendungen gespeichert, diese Menge wird sp�ter mit <code>usedAttributeGroupUsages</code>
					// verglichen.
					final Set<AttributeGroupUsage> allowedATGUsages = new HashSet<AttributeGroupUsage>();

					// Jede ATG einzeln betrachten
					for(SystemObject systemObjectAttributeGroup : objectTypeATGs) {
						final AttributeGroup attributeGroup = (AttributeGroup)systemObjectAttributeGroup;

						// Alle Attributgruppenverwendungen, die in der zu pr�fenden Version aktiv sind
						final NonMutableSet nonMutableSetHelper = attributeGroup.getNonMutableSet("AttributgruppenVerwendungen");
						final List<SystemObject> attributeGroupUsages = nonMutableSetHelper.getElementsInVersion(
								getActiveVersion(nonMutableSetHelper)
						);

						// Jede ATG-Verwendung betrachten und pr�fen, ob konfigurierende Datens�tze vorhanden sind (falls gefordert).
						// Gleichzeitg wird gepr�ft, ob jeder Datensatz des Objektes ebenfalls mit einer g�ltigen ATG-Verwendung, die am Objekt erlaubt ist,
						// abgedeckt ist.
						for(SystemObject object : attributeGroupUsages) {
							final AttributeGroupUsage atgUsage = (AttributeGroupUsage)object;
							// F�r den sp�teren Vergleich speichern
							allowedATGUsages.add(atgUsage);

							// So den Datensatz anfragen, da �ber ATG+Aspekt die Attributverwendung nicht angefordert werden kann, bei der Methode, die den
							// Datensatz sucht.
							final Data dataSet = ((ConfigSystemObject)systemObject).getConfigurationData(atgUsage, _versionedView);

							// + " Typ des Objekts: " + systemObject.getType().getPid()
//						System.out.println("SystemObjekt: " + systemObject.getPid() + " Usage " + atgUsage.getUsage() + " ATG " + atgUsage.getAttributeGroup() + " Aspekt " + atgUsage.getAspect());

							// Wenn die Verwendung ben�tig wird, dann muss es einen konfigurierenden Datensatz am Objekt
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

							// Falls Komposition oder Assoziation/Aggrigation im Spiel ist, muss gepr�ft werden ob das Referenzierte Objekt im Konfigurationsbereich oder in der Konfiguration
							// vorhanden ist. 3 b),c),d)
							if((dataSet != null)) {
								// 3 b),c),d),f),g) pr�fen
								// Falls ein Datensatz eine Referenz enth�lt, wird gepr�ft ob die Referenz innerhalb des
								// gepr�ften Konfiguratonsbereich enthalten ist. M�gliche Fehler werden direkt
								// eingetragen.
								

								checkDataSetReferences(dataSet, null, verifyingConfigArea, _dataModel, result, systemObject, usedComponent);

								// h) pr�fen
								if(!dataSet.isDefined()) {
									if(ignoreAttributeValueError(systemObject, atgUsage.getAttributeGroup(), atgUsage.getAspect(), dataSet) == false) {

										// Der Datensatz kann so nicht �ber den Datenverteiler verschickt werden -> Die Werte der Attribute sind nicht korrekt.
										final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
												ConsistencyCheckResultEntryType.LOCAL_ERROR,
												verifyingConfigArea,
												new SystemObject[]{systemObject, atgUsage.getAttributeGroup(), atgUsage.getAspect()},
												"Es sind nicht alle Attribute der Attributgruppe mit Werten versehen. Der Datensatz kann nicht �ber den Datenverteiler verschickt werden: "
												+ dataSet

										);
										result.addEntry(entry);
									}
								}
							}
						} // for, �ber alle ATG-Verwendungen des Objekts
					}

					// Pr�fen, ob auch alle Datens�tze, die am Objekt gespeichert sind, auch dort wirklich gespeichert sein d�rfen. (Fall e)
					for(AttributeGroupUsage usedAttributeGroupUsage : usedAttributeGroupUsages) {
						if(!allowedATGUsages.contains(usedAttributeGroupUsage)) {

							// Es wurde eine ATG-Verwendung gefunden, zu der ein Datensatz an dem Objekt gespeichert wurde,
							// aber diese ATG-Verwendung ist an diesem Objekt gar nicht zugelassen.
							final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
									verifyingConfigArea,
									new SystemObject[]{systemObject, usedAttributeGroupUsage},
									"An einem Objekt wurde ein konfigurierender Datensatz mit einer Attributgruppenverwendung gefunden, die f�r dieses Objekt nicht in der Menge der erlaubten Attributgruppenverwendungen eingetragen ist."
							);
							result.addEntry(entry);
						}
					}

					// Pr�fung 4:
					// a) Die Nummerierung von Attributen innerhalb von Attributgruppen/Listen �ber ihre Position muss bei eins
					// beginnen und fortlaufend sein.

					// Liste, die alle Attribute des Objekts enth�lt. Nur ATG�s und Attributlisten werden gepr�ft, in
					// allen anderen F�llen ist die Liste <code>null</code>.
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
						// Es m�ssen Attribute getestet werden. Das kleinste Attribut muss die Nummer 1 haben, dann m�ssen
						// die Werte fortlaufend sein.

						// Map, diese benutzt als Schl�ssel den Index, als Value das Attribut.
						// Bei einer Kollision kann so ermittelt werden mit welchen Attributen es eine Kollision gibt.
						// Die Map erm�glicht den Test in "O(2*n)"(Aufbau der Map und lineares durchlaufen der Map)
						// anstatt O(n^2) durchzuf�hren.
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

						// F�r jeden Wert requieredAttributeIndex muss ein Eintrag in der Map vorhanden sein.
						// Und die Werte m�ssen bei 1 beginnen und dann fortlaufend sein
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
										"F�r einen Index wurde kein Attribute gefunden, Index " + requieredAttributeIndex
								);
								result.addEntry(entry);
							}
							requieredAttributeIndex++;
						}
					}

					// Pr�fung der Zust�nde einer Ganzzahldefinition (nicht in TPuK gefordert)

					// Bei einer Attributdefinition, die eine Ganzzahl definiert, muss gepr�ft werden, ob die Zust�nde
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
								final StringBuffer text = new StringBuffer("Der Zustand einer Ganzzahldefinition enth�lt ");

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

					// Bei alle Typen muss ein Parameterdatensatz vorhanden sein und zwar f�r jedes Objekt, das von diesem Typen ist.
					if(systemObject instanceof SystemObjectType) {
						final SystemObjectType systemObjectType = (SystemObjectType)systemObject;

						// Alle ATG�s, die von diesem Typ sind
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
						// F�r alle Objekte, die nach Ablauf des Algorithmus noch in dieser Menge enthalten sind, wurde kein Datensatz gefunden -> ebenfalls ein Fehler
						final Set<AttributeGroup> directParameterATGs = new HashSet<AttributeGroup>();

						// F�r jede ATG, die parametrierend ist, muss ein default am Typ vorhanden sein
						for(AttributeGroup directATG : directATGs) {
							if(isParameter(directATG) == true) {
								directParameterATGs.add(directATG);
							}
						}

						final AttributeGroup atg = _dataModel.getAttributeGroup("atg.defaultParameterdatens�tze");
						final Aspect aspect = _dataModel.getAspect("asp.eigenschaften");

						// Datensatz, der die parametrierenden Datens�tze enth�lt
						final Data configurationData = ((ConfigSystemObject)systemObjectType).getConfigurationData(atg, aspect, _versionedView);

						if(configurationData != null && configurationData.isDefined()) {
							// Der Datensatz, wurde gefunden. Die Daten sind als Array gespeichert.
							final Data.Array arrayWithDefaultParameterDataSets = configurationData.getArray("Default-Parameterdatensatz");

							for(int nr = 0; nr < arrayWithDefaultParameterDataSets.getLength(); nr++) {
								final Data item = arrayWithDefaultParameterDataSets.getItem(nr);

								final AttributeGroup attributeGroup = (AttributeGroup)item.getReferenceValue("attributgruppe").getSystemObject();

								// In dem Set sind nur Parametriende ATG�s enhalten. Ist die zu pr�fende ATG ebenfalls parametrierend, muss der Rest ebenfalls
								// gepr�ft werden.
								if(directParameterATGs.contains(attributeGroup)) {

									final String pidType = item.getReferenceValue("typ").getSystemObject().getPid();

									final Data.Array datasetArray = item.getArray("datensatz");
									final byte[] bytes = new byte[datasetArray.getLength()];
									for(int j = 0; j < datasetArray.getLength(); j++) {
										bytes[j] = datasetArray.getScaledValue(j).byteValue();
									}

									final int serialiserVersion = item.getScaledValue("serialisierer").intValue();

									// Damit ein default Parameter richtig definiert ist, m�ssen folgende Bediengungen gepr�ft werden:
									// 1) Die Pid des Typen der Arrayeintrags muss gleich dem Typen sein, der gerade gepr�ft ist
									// 3) Die Gr��e des byte-Arrays muss gr��er 0 sein
									// 4) Der Datensatz muss deserialsiert werden k�nnen

									if(pidType.equals(systemObjectType.getPid())) {

										boolean errorFound = false;
										// Es wird ein Fehlertext eingetragen, wenn errorFound==true ist
										String errorText = "";

										// Es wird auf den zu pr�fenden Typen referenziert
										// Die ATG ist parametrierend
										if(bytes.length > 0) {
											// pr�fen, ob der DS deserialsiert werden kann

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
													// Kommt der Algorithmus bis an diese Stelle, wurde ein Defaultparameterdatensatz vollst�ndig definiert
													directParameterATGs.remove(attributeGroup);
												}
											}
											catch(NoSuchVersionException e) {
												// Die Version des Desrialisieres wird nicht unterst�zt
												errorFound = true;
												errorText =
														"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil die Version des Deserialisieres nicht unterst�tzt wird. Version, die benutzt werden sollte: "
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
											// Dieser Datensatz muss vorhanden sein. Da dieser den Defaultwert enth�lt
											errorFound = true;
											errorText = "Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil das byte-Array, das den Datens�tz enth�lt, die L�nge 0 besitzt.";
										}

										if(errorFound == true) {
											final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
													


													ConsistencyCheckResultEntryType.WARNING,
													verifyingConfigArea,
													new SystemObject[]{systemObject, attributeGroup},
													errorText
											);
											result.addEntry(entry);

											// Da bereits ein Fehler erzeugt wurde, kann das Objekt aus dem Set gel�scht werden, sonst w�rde
											// noch ein Fehler erzeugt werden.
											directParameterATGs.remove(attributeGroup);
										}
									}
								}
							}// for �ber alle Elemente des Arrays
						}

						// Es wurden alle Datens�tze gepr�ft. Zu jeder ATG des Tys sollte ein Datensatz gefunden worden sein.
						// Es wurden alle ATG�s entfernt die:
						// 1) Einen Datensatz hatten
						// 2) Einen Datensatz hatten, dieser aber nicht bearbeitet werden konnte (es wurde ein Fehler erzeugt)
						// Alle ATG�s die jetzt noch in der Menge sind, haben keinen Datensatz -> Fehler
						if(directParameterATGs.size() > 0) {
							for(AttributeGroup directParameterATG : directParameterATGs) {
								// Fehlertext, der eingetragen wird, wenn dataSetFound auf "false" bleibt.
								String text = "Es wurde kein Default-Parameter-Datensatz f�r die Attributgruppe eines Typen gefunden.";
								


								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.WARNING, verifyingConfigArea, new SystemObject[]{systemObject, directParameterATG}, text
								);
								result.addEntry(entry);
							}
						}
					}// Typ pr�fen
					else if(systemObject instanceof ConfigurationObject) {
						// Bei allen Konfigurationsobjekten kann ein Datensatz "atg.defaultParameterdatens�tze" vorhanden sein.
						// F�r Konfigurationsobjekte m�ssen dann folgende Bedienungen erf�llt sein (f�r jeden Array-Eintrag gilt):
						// 1) Die ATG muss parametrierend sein
						// 2) Das Byte-Array hat eine Gr��e gr��er 0 und kann mit der angegebenen Serialisiererversion deserialisiert werden
						// 3) Der unter 2) deserialisierte Datensatz muss definiert sein

						// Datensatz, der die parametrierenden Datens�tze enth�lt
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
											// Die Version des Desrialisieres wird nicht unterst�zt
											errorFound = true;
											errorText =
													"Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil die Version des Deserialisieres nicht unterst�tzt wird. Version, die benutzt werden sollte: "
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
									// Dieser Datensatz muss vorhanden sein. Da dieser den Defaultwert enth�lt
									errorFound = true;
									errorText = "Der Datensatz eines Default-Parameter-Datensatz konnte nicht ausgelesen werden, weil das byte-Array, das den Datens�tz enth�lt, die L�nge 0 besitzt.";
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

						// Wenn das Objekt eine Typ-Hierarchiedefinition f�r die Parametrierung ist, dann wird es sp�ter weiter gepr�ft
						if(hierarchyDefinitionTypes.contains(objectTypeOfVerifiedObject)) {
							hierarchyObjects.add((ConfigurationObject)systemObject);
						}
					}
				} // for(SystemObject systemObject : checkObjects)
			}// for, alle Konfigurationsbereiche

			_debug.info(
					"Pr�fe Typ-Hierarchie f�r Parametrierung. Anzahl Hierarchie-Objekte: " + hierarchyObjects.size() + " Anzahl bisheriger Fehler: "
					+ (result.getInterferenceErrors().size() + result.getLocalErrors().size()) + " Anzahl bisheriger Warnungen: " + result.getWarnings().size()
			);

			// Pr�ft, ob die Hierarchiedefinition der Typen f�r die Parametrierung in Ordnung sind.
			checkParameterTypeHierarchyDefinition(result, hierarchyObjects);

			// Die Konsistenzpr�fung hat alle Abh�ngigkeiten zwischen den Bereichen erkannt. Wurden keine entsprechenden Fehler gefunden, k�nnen
			// die Abh�ngigkeiten gespeichert werden.

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
					// In diesem Fall sind Interferenzfehler erlaubt, da diese durch einen anderen aufgel�st werden k�nnen.
					// Hat jemand anders die Interferenzfehler behoben, kann dieser Bereich auch lokal aktiviert werden.
					if(result.localError() == false) {
						saveDependencies();
					}
				}

				_areasDependencies.clear();
			}
			catch(ConfigurationChangeException e) {
				result.addEntry(
						new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								_dataModel.getConfigurationAuthority().getConfigurationArea(),
								Collections.<SystemObject>emptyList(),
								"Fehler in der Konsistenzpr�fung beim Schreiben der Datens�tze, die die Abh�ngigkeiten zwischen den Bereichen speichern:\n" +
								getStackTrace(e)
						)
				);
			}
		}
		catch(Exception e){
			String stacktrace = getStackTrace(e);

			result.addEntry(
					new ConsistencyCheckResultEntry(
							ConsistencyCheckResultEntryType.LOCAL_ERROR,
							_dataModel.getConfigurationAuthority().getConfigurationArea(),
							Collections.<SystemObject>emptyList(),
							"Bei der Konsistenzpr�fung ist ein unerwarteter Fehler aufgetreten:\n" + stacktrace
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
	 * Pr�ft, ob ein Objekt vom anderen Abh�ngig ist und tr�gt beim Konfigurationsbereich, falls n�tig, die Abh�ngigkeit ein.
	 * <p/>
	 * Der Konfigurationsverantwortliche des Parameters <code>systemObject</code> muss der Verantwortliche der Konfiguration sein, da nur dieser Abh�ngigkeiten in
	 * den Bereichen eintragen darf.
	 * <p/>
	 * Wenn die beiden Objekte im selben Bereich sind, kommt es zu keiner Abh�ngigkeit.
	 *
	 * @param systemObject     Objekt, das vielleicht von einem anderen Objekt abh�ngig ist und somit dazu f�hrt, dass der Bereich des Objekts eine Abh�ngigkeit zu
	 *                         einem anderen Bereich erh�lt.
	 * @param dependencyObject Objekt von dem der Parameter <code>systemObject</code> abh�ngig ist.
	 * @param dependencyKind   Art der Abh�ngigkeit, wenn eine Abh�ngigkeit gefunden wird. Als Versionen werden die eingetragen, in der die Aktion ausgef�hrt wird,
	 *                         dies muss nicht unbedingt die aktuelle sein.
	 */
	private void checkDependency(
			final SystemObject systemObject, final SystemObject dependencyObject, ConfigurationAreaDependencyKind dependencyKind) {
		// Abh�ngigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		if(!_storeDependencies) return;

		if(systemObject == null || dependencyObject == null) {
			_debug.warning("Abh�ngigkeit zwischen zwei Objekten kann nicht gepr�ft werden, systemObjekt: " + systemObject + ", dependencyObject: " + dependencyObject);
			return;
		}
		// Es darf in den Bereich nur dann eine Abh�ngigkeit eingetragen werden, wenn der KV der Konfiguration auch f�r den Bereich verantwortlich ist.
		if(systemObject.getConfigurationArea().getConfigurationAuthority().getId() == _configurationAuthorityId) {
			if(systemObject.getConfigurationArea().getId() != dependencyObject.getConfigurationArea().getId()) {

				// Beide Objekte befinden sich in unterschiedlichen Bereichen. Also wurde eine Abh�ngigkeit gefunden. Diese wird am Bereich eingetragen.

				// Abh�ngigkeiten k�nnen auch durch dynamische Objekte entstehen. Dynamische Objekte k�nnen aber auch transient (stehen nach dem Neustart der
				// Konfiguration nicht mehr zur Verf�gung) sein und das darf keine Abh�ngigkeit ausl�sen.
				if(isObjectTransient(systemObject) == false && isObjectTransient(dependencyObject) == false) {
					// Das Objekt, das die Abh�ngigkeite ausl�st ist ein Konfigurationsobjekt. Damit wird entweder die Version des Bereichs genommen
					// in der gerade eine Aktion stattfindet (denn in der w�rde die Abh�ngigkeit ja bestehen) oder aber die Version in der der Bereich
					// aktiv ist (diese Abh�ngigkeit w�re schon fr�her erfa�t worden und wird somit nicht erneut aufgenommen (siehe ConfigConfigurationArea)).
					// Falls das Objekt dependencyObject ein dynamisches Objekt ist, muss zuerst die Version ermittelt werden, in der das Objekt erzeugt wurde.
					createDependency(
							getActiveVersion(systemObject.getConfigurationArea()),
							systemObject.getConfigurationArea(),
							getDependenceVersion(dependencyObject),
							dependencyObject.getConfigurationArea(),
							dependencyKind
					);
				} // Pr�fung, ob die Objekte beide nicht transient sind, im else-Fall muss nichts gemacht werden. Da Transiente Objekte keine Abh�ngigkeiten ausl�sen d�rfen.
			}
		}
	}

	/**
	 * Pr�ft, ob ein Objekt transient ist. Konfigurationsobjekt sind niemals transient, dort wird immer <code>false</code> zur�ckgegeben. Bei dynamischen Objekten
	 * muss dies gepr�ft werden.
	 *
	 * @param systemObject Objekt, das gepr�ft werden soll, ob es transient ist.
	 *
	 * @return <code>false</code>, wenn ein dynamisches Objekt nicht transient ist und <code>false</code>, wenn ein Konfigurationsobjekt �bergeben wird;
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
	 * Speichert eine Abh�ngigkeit f�r den Bereich <code>verifyingArea</code> in Map <code>_areasDependencies</code>.
	 * <p/>
	 * Damit die Daten endg�ltig als Datensatz gespeichert werden, muss die Methode {@link #saveDependencies()} aufgerufen werden.
	 *
	 * @param verifyingVersion Version, in der die Abh�ngigkeit entstanden ist.
	 * @param verifyingArea    Bereich, der ab <code>verifyingVersion</code> vom Bereich <code>dependencyArea</code> abh�ngig ist.
	 * @param neededVersion    Version, in der der Bereich <code>dependencyArea</code> vorliegen muss, damit die Abh�ngigkeit aufgel�st werden kann.
	 * @param dependencyArea   Bereich, von dem <code>verifyingArea</code> abh�ngig ist.
	 * @param dependencyKind   Art der Abh�ngigkeit
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

			// Falls noch keine Abh�ngigkeit gefunden wurde, wird eine Menge angelegt, die diese neue Abh�ngigkeit speichern kann.
			if(allAreaDependencies == null) {
				allAreaDependencies = new HashSet<ConfigurationAreaDependency>();
				_areasDependencies.put(verifyingArea, allAreaDependencies);
			}
			allAreaDependencies.add(newDependency);
		}// synch
	}

	/**
	 * Speichert alle Abh�ngigkeiten zwischen Bereichen, die durch die Konsistenzpr�fung gefunden wurden, in entsprechenden Datens�tzen im Bereich.
	 * <p/>
	 * Die Methode sperrt die  <code>_areasDependencies</code> bis alle Operationen abgeschlossen sind.
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *          Wird geworfen, wenn der Datensatz, der die Abh�ngigkeiten enth�lt, nicht geschrieben werden kann.
	 */
	private void saveDependencies() throws ConfigurationChangeException {
		// Abh�ngigkeiten nur speichern, wenn mindestens Version 9 des Metamodells vorliegt
		if(!_storeDependencies) return;

		synchronized(_areasDependencies) {

			// Alle Aufrufe in diesem Block k�nnen zu Festplattenzugriffen f�hren. Das kann diese Methode sehr lange blockieren lassen.
			// In der Zeit ist es auch nicht m�glich weitere Abh�ngigkeiten hinzuzuf�gen. Das ist aber auch nicht n�tig, da diese
			// Methode am Ende der Konsistenzpr�fung aufgerufen wird.

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
				// Nur Speichern, wenn der Bereich ver�ndert wurde
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
					// Der Datensatz, der die Abh�ngigkeiten speichert, konnte nicht geschrieben werden.
					final StringBuffer errorText = new StringBuffer();
					errorText.append(
							"Fehler in der Konsistenzpr�fung: Der Datensatz, der alle Abh�ngigkeiten eines Bereich enth�lt, konnte nicht geschrieben werden. Betroffener Bereich: "
							+ area.getConfigurationArea().getPid() + " KV des Bereichs: " + area.getConfigurationArea().getConfigurationAuthority().getPid()
							+ " KV der Konfiguration: " + _dataModel.getConfigurationAuthorityPid() + " .Abh�ngigkeiten, die geschrieben werden sollten: "
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
									"Der Mengenname '" + setName + "' einer Hierarchiedefinition f�r den Typ '" + typePid
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
								"Die Typ-Hierarchiedefinition f�r die Parametrierung ist nicht Zykelfrei"
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
	 * Pr�ft, ob eine ATG in der aktiven/zu aktivierenden Version parametrierend ist.
	 *
	 * @param checkATG ATG, die gepr�ft werden soll
	 *
	 * @return <code>true</code>, wenn die �bergeben ATG in der �bergebenen Version g�ltig ist; <code>false</code>, wenn nicht.
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
	 * <p/>
	 * Bei der ersten Version wurden diese drei Zeitstempel mit 0 deklariert. Die Zahl 0 im Zusammenhang mit abseluten Zeitangaben wurden aber zur Erkennung des
	 * "undefiniert" Werts benutzt. Damit waren diese Datens�tze immer "nicht definiert", dies wurde in der Konsistenzpr�fung als Fehler erkannt (Der Datensatz war
	 * nicht definiert).
	 * <p/>
	 * Dieser Fehler wird im Rahmen des Imports behoben, neue Objekte bekommen als Zeitstempel die aktuelle Zeit.
	 * <p/>
	 * Damit trotzdem mit alten Daten weiter gearbeitet werden kann, wird dieser Fehler absichtlich von der Konsistenzpr�fung ignoriert.
	 * <p/>
	 * Es m�ssen folgende Bediengungen erf�llt sein, damit diese Methode <code>true</code> zur�ck gibt: 1) Das �bergebene Objekt muss ein Konfigurationsbereich
	 * sein 2) Es muss sich um die Attributgruppe "atg.konfigurationsBereich�nderungsZeiten" handeln 3) Es muss der Aspekt "asp.eigenschaften" Eigenschaften
	 * benutzt werden 4) Einer der folgenden Attribute muss 0 sein: Letzte�nderungszeitDynamischesObjekt, Letzte�nderungszeitKonfigurationsObjekt,
	 * Letzte�nderungszeitDatensatz 5) Falls ein Attribut nicht 0 ist, so muss der Wert des Attributs definiert sein.
	 *
	 * @param configArea     Es muss sich um einen Konfigurationsbereich handeln (das wird mit instanceOf gepr�ft)
	 * @param attributeGroup ATG, die zu einem Fehler f�hrte
	 * @param aspect         Aspekt, der zu einem Fehler f�hrte
	 * @param data           Datensatz, der Daten enth�lt, die nicht definiert sind.
	 *
	 * @return <code>true</code>, wenn die �bergenen Parameter zwar einen lokalen Fehler enthalten, dieser aber ignoriert werden kann. <code>false</code>, wenn es
	 *         sich um einen lokalen Fehler handelt, der gemeldet werden muss.
	 */
	private boolean ignoreAttributeValueError(final SystemObject configArea, final AttributeGroup attributeGroup, final Aspect aspect, final Data data) {
		if(configArea instanceof ConfigurationArea == false) {
			// Es handelt sich um keinen Bereich. Aber nur diese d�rfen den fehlerhaften Datensatz besitzen
			return false;
		}

		// Handelt es sich um die ATG, die den fehlerhaften Datensatz speichern darf
		if("atg.konfigurationsBereich�nderungsZeiten".equals(attributeGroup.getPid()) == false) {
			// Es handelt sich um eine andere ATG
			return false;
		}

		if("asp.eigenschaften".equals(aspect.getPid()) == false) {
			// Falscher Aspekt
			return false;
		}

		// Objekt, ATG und Aspekt stimmen. Nun muss gepr�ft werden, ob die(mindestens eins) gespeicherten Attribute 0 sind

		// wird true, wenn der Datensatz mindestens ein Attribut den Wert 0 besitzt.
		boolean zeroFound = false;

		// Wird true, wenn ein Attribut einen ung�ltigen Wert besitzt
		boolean errorFound = false;

		String attribute = "Letzte�nderungszeitDynamischesObjekt";

		if(data.getTimeValue(attribute).getMillis() == 0) {
			zeroFound = true;
		}
		else if(data.getItem(attribute).isDefined() == false) {
			// Der Wert ist nicht 0 aber trotzdem undefiniert -> Es handelt sich um einen fehlerhaften Wert
			errorFound = true;
		}

		attribute = "Letzte�nderungszeitKonfigurationsObjekt";

		if(data.getTimeValue(attribute).getMillis() == 0) {
			zeroFound = true;
		}
		else if(data.getItem(attribute).isDefined() == false) {
			// Der Wert ist nicht 0 aber trotzdem undefiniert -> Es handelt sich um einen fehlerhaften Wert
			errorFound = true;
		}

		attribute = "Letzte�nderungszeitDatensatz";

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
	 * Pr�ft, ob eine Komponente bereits von einem anderen �bergeordneten Objekt benutzt wird. Die �bergebene Komponente wird in der �bergebenen Map gespeichert.
	 * <p/>
	 * Wird die Komponente bereits benutzt, so wird ein lokaler Fehler erzeugt und am �bergebenen Objekt <code>result</code> gespeichert.
	 *
	 * @param component          Komponente, die gepr�ft werden soll. Diese wird als Key in der �bergebenen Map gespeichert.
	 * @param componentUser      �bergeordnetes Objekt, dass die Komponente nutzt. Dieser Wert wird als Value in der Map gespeichert.
	 * @param componentsOfAnArea Map, die alle bisher benutzten Komponenten und deren Benutzer enth�lt.
	 * @param result             Objekt, an dem lokale Fehler gespeichert werden.
	 *                           <p/>
	 *
	 * @return <code>true</code>, wenn es zu keinem Fehler gekommen ist. <code>false</code>, wenn eine Komponente von zwei �bergeordneten Objekten benutzt wurde.
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
	 * Diese Methode pr�ft, ob eine Id bereits vergeben wurde. Dazu werden alle Ids aller betrachteten Objekte gespeichert. Wurde eine Id bereits vergeben, so wird
	 * ein lokaler Fehler gemeldet.
	 *
	 * @param systemObject    Objekt, das �berpr�ft werden soll
	 * @param idsFromAllAreas Map, die alle Ids speichert. In dieser Map wir das �bergebene Objekt <code>systemObject</code> ebenfalls gespeichert.
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
	 * Diese Methode pr�ft, ob die Pid des �bergebenen Objekts bereits in einem anderen Konfigurationsbereich vergeben wurde. Ist dies der Fall, so wird eine
	 * Warnung erzeugt und an das �bergebene Objekt <code>result</code> �bergeben.
	 * <p/>
	 * Eine Ausnahme bilden Objekte mit der Pid "" (es wurde keine Pid angegeben), diese werden nicht gespeichert und nicht Ber�cksichtigt.
	 * <p/>
	 * Die Warnung darf nur eingetragen werden, wenn die beiden Objekte in unterschiedlichen Konfigurationsbereichen zu finden sind. Sind beide Objekte im gleichen
	 * Bereich, wurde ein lokaler Fehler gefunden. Dieser muss durch einen anderen Test gefunden und gemeldet werden.
	 * <p/>
	 * Wurde die Pid bisher nicht vergeben, so wird sie in der �bergebenen Map gespeichert.
	 *
	 * @param systemObject     Objekt, dessen Pid gepr�ft werden soll.
	 * @param pidsFromAllAreas Alle Pids, die bisher in die Map eingetragen wurden. Wurde eine doppelte Pid gefunden, so wird das neue Objekt nicht in die Map
	 *                         eingetragen.
	 * @param result           Objekt, in dem eine Warnung eingetragen wird, wenn die Pid des �bergebenen Objekts <code>systemObject</code> bereits in der Map
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
	 * Durchl�uft rekursiv einen Datensatz und pr�ft ob alle referenzierten Objekte im entsprechenden Konfigurationsbereich vorhanden und g�ltig sind.
	 * <p/>
	 * Ist ein referenziertes Objekt als Komposition gekennzeichnet, so muss das referenzierte Objekt im �bergebenen Konfigurationsbereich zu finden sein.
	 * <p/>
	 * Ist ein referenziertes Objekt als Aggregation bzw. Assoziation gekennzeichnet, muss das referenzierte Objekt im �bergebenen Datenmodell zu finden sein.
	 * <p/>
	 * In allen F�llen muss das referenzierte Objekt in der angegebenen Version g�ltig sein.
	 *
	 * @param data           Datensatz, der gepr�ft werden soll
	 * @param parentData     �bergeordnetes Data-Objekt oder <code>null</code>, falls <code>data</code> einen ganzen Datensatz darstellt.
	 * @param configArea     Konfigurationsbereich, in dem sich ein referenziertes Objekt befinden muss, wenn die Referenz als Komposition definiert ist
	 * @param dataModel      Datenmodell, in dem sich ein referenziertes Objekt befinden muss, wenn die Referenz nicht als Komposition definiert ist
	 * @param errorObject    Objekt, in dem ein Fehler eingetragen werden kann
	 * @param systemObject   Objekt, an dem der Datensatz gespeichert ist. Diese Information wird ben�tigt, um eine entsprechende Fehlermeldung zu generieren
	 * @param usedComponents Speichert alle Komponenten und deren �bergordnete Objekte.
	 *
	 * @return true = Alle Objekte, die referenziert werden, sind im Konfigurationsbereich vorhanden, falls nicht wird false zur�ckgegeben.
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

				// Objekt, das referenziert wurde. Das Objekt wird �ber die Id im Datenmodell angefordert.
				// Id (Zahl ungleich 0) oder "0"(Id ist 0), falls das Objekt mit der Id nicht gefunden wurde oder eine Referenz
				// undefiniert ist.
				final SystemObject referencedObject;

				if(data.asReferenceValue().getId() != 0) {
					// Objekt mit der Id anfordern
					referencedObject = dataModel.getObject(data.asReferenceValue().getId());

					// Es kann sein, dass das Objekt nicht gefunden wurde, weil der Benutzer den Bereich "vergessen" hat.
					// Es steht dann zwar eine Id im Datensatz, diese kann aber nicht aufgel�st werden -> es ist zu einer Abh�ngigkeit gekommen.
					if(referencedObject == null) {
						// Das referenzierte Objekt kann nicht gefunden werden. Also fehlt der Bereich.
						// Es besteht zwar eine Abh�ngigkeit, aber diese kann nicht aufgel�st werden.

						if(referenceAttributeType.isUndefinedAllowed() && referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION) {
							// Das Objekt darf fehlen. Es muss eine Warnung ausgegeben werden.
							final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
									ConsistencyCheckResultEntryType.WARNING,
									configArea,
									new SystemObject[]{systemObject, data.getAttributeType()},
									"Eine Objektreferenz (Assoziation und optional) (" + data.getName()
									+ ") kann nicht aufgel�st werden. Der Bereich, der das Objekt enth�lt, steht der Konfiguration nicht zur Verf�gung. "
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
					// Da bei einer Id mit 0 kein Objekt referenziert wird, kann es auch keine Abh�ngigkeit geben.
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

				// Kommt der Algorithmus an diese Stelle, k�nnte eine Abh�ngigkeit gefunden worden sein.
				// Der Typ der Abh�ngigkeit ist abh�ngig von der Art der Referenzierung.
				// Bei einer Komposition kann es keine Abh�ngigkeiten geben, da das referenzierte Objekt im Bereich zu finden sein muss.
				if(referenceAttributeType.getReferenceType() == ReferenceType.COMPOSITION) {
					// Es ist eine Komposition und das referenzierte Objekt ist vorhanden (das wurde vorher gepr�ft)
					// Vom data eine Referenz anfordern, der Name der Referenz ist der Name des data.
					// Der Konfigurationsbereich in dem sich die Referenz befindet, muss gleich der �bergebene Referenz sein
					if(!data.asReferenceValue().getSystemObject().getConfigurationArea().equals(configArea)) {
						// Das Objekt, das referenziert wird, befindet sich nicht im gepr�ften Konfigurationsbereich.
						// Dies ist ein Fehler.
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
								"Zu einer Referenz(Komposition) kann das referenzierte Objekt nicht im Konfigurationsbereich gefunden werden, wie der konfigurierende Datensatz, der die Referenz enth�lt. "
								+ getAttributeDescription(parentData, data)
						);
						errorObject.addEntry(entry);
						return false;
					}
					// Ist das Objekt in der gepr�ften Version g�ltig?
					if(referencedObject instanceof ConfigurationObject) {
						if(configurationObjectAvailability((ConfigurationObject)referencedObject)) {
							return true;
						}
						final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configArea,
								new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
								"Das referenzierte Objekt einer Referenz(Komposition) ist in der gepr�ften Version nicht mehr g�ltig. "
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

					// Kompositionen d�rfen nur einmal durch ein �bergeordnetes Objekt referenziert werden
					if(!checkDoubleComponentUsage(referencedObject, referenceAttributeType, usedComponents, errorObject)) {
						// Es wurde ein Fehler gefunden
						return false;
					}
					return true;
				}
				else {
					// Es ist keine Komposition, also eine Aggregation/Assoziation.
					// In diesem Fall muss das referenzierte Objekt im Datenmodell gesucht werden (das referenzierte Objekt ist vorhanden, das wurde vorher gepr�ft).
					// Es kann zu Abh�ngigkeiten zwischen Bereichen kommen.

					// Es werden nur Konfigurationsobjekte referenziert
					if(referencedObject instanceof ConfigurationObject) {
						if(configurationObjectAvailability((ConfigurationObject)referencedObject) == false) {
							// Das Objekt ist nicht mehr g�ltig, also tritt ein Interferenzefehler auf

							//Spezialbehandlung f�r wechselnden Konfigurationsverantwortlichen eines Bereichs
							if(configArea.getId() == systemObject.getId() && parentData != null && parentData.getName().equals("atg.konfigurationsBereichEigenschaften") && data.getName().equals("zust�ndiger")) {
								final SystemObject newAuthority = parentData.getReferenceValue("neuerZust�ndiger").getSystemObject();
								if(newAuthority == null) {
									// Das Objekt ist nicht mehr g�ltig und kein neues angegeben
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											configArea,
											new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
											"Der bisherige Verantwortliche des Bereichs ist in der gepr�ften Version nicht mehr g�ltig und es ist "
											+ "kein neuer Verantwortliche angegeben. BITTE ERNEUT IMPORTIEREN. "
											+ getAttributeDescription(parentData, data)
									);
									errorObject.addEntry(entry);
									return false;
								}
								if(configurationObjectAvailability((ConfigurationObject)newAuthority) == false) {
									// Der neue Konfigurationsverantwortliche ist auch nicht g�ltig
									final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
											ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
											configArea,
											new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject(), newAuthority},
											"Der bisherige Verantwortliche des Bereichs ist in der gepr�ften Version nicht mehr g�ltig und der neue Verantwortliche auch nicht. "
											+ getAttributeDescription(parentData, data)
									);
									errorObject.addEntry(entry);
									return false;
								}
							}
							else {
								// Das Objekt ist nicht mehr g�ltig, also tritt ein Interferenzfehler auf
								final ConsistencyCheckResultEntry entry = new ConsistencyCheckResultEntry(
										ConsistencyCheckResultEntryType.INTERFERENCE_ERROR,
										configArea,
										new SystemObject[]{systemObject, data.asReferenceValue().getSystemObject()},
										"Das referenzierte Objekt einer Referenz (Aggregation/Assoziation) ist in der gepr�ften Version nicht mehr g�ltig. "
										+ getAttributeDescription(parentData, data)
								);
								errorObject.addEntry(entry);
								return false;
							}
						}
					}
//					else {
//						// Es handelt sich um ein dynamisches Objekt. Diese sind sofort g�ltig, k�nnen aber
//						// auch jederzeit ung�ltig werden. An dieser Stelle w�re die Konsistenzpr�fung zu sp�t
//					}

					// Die Referenz ist g�ltig -> gibt es Abh�ngigkeiten
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
				// Der genau Typ des Objekts ist nicht von Intresse, nur Referenzen werden gepr�ft.
				return true;
			}
		}
		else {
			// Es ist entweder eine Liste oder ein Array
			Iterator listOrArray = data.iterator();
			// Wird false, wenn eine Referenz nicht im �bergebenen Konfigurationsbereich gefunden werden konnte.
			// Die Variable wird nur gesetzt, wenn ein Fehler aufgetreten ist
			boolean result = true;
			while(listOrArray.hasNext()) {
				if(!checkDataSetReferences(
						(Data)listOrArray.next(), data, configArea, dataModel, errorObject, systemObject, usedComponents
				)) {
					// Zu einer Referenz konnte kein Objekt im �bergebenen Konfigurationsbereich gefunden werden
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
	 * Pr�ft ob ein Objekt in der angegebenen Version g�ltig ist (das Objekt kann auch �ber die Version hinaus g�ltig sein).
	 *
	 * @param configurationObject Objekt, das gepr�ft werden soll
	 *
	 * @return true = Das Objekt ist in der angegebenen Version g�ltig, oder dar�ber hinaus; false = Das Objekt ist nicht g�ltig
	 */
	private boolean configurationObjectAvailability(
			ConfigurationObject configurationObject) {

		// Version rausfinden, in der der Konfigurationsbereich l�uft, bzw. Version in der der Konfigurationsbereich laufen soll.
		final short version;
		if(_areaVersionMap.containsKey(configurationObject.getConfigurationArea())) {
			// Es gibt f�r den Konfigurationsbereich eine neue Version, die aktiviert werden soll
			version = _areaVersionMap.get(configurationObject.getConfigurationArea());
		}
		else {
			// Es gibt keine neue Version, der Bereich l�uft weiter in seiner aktuellen Version.
			// Ist das Objekt derzeit g�ltig ?
			return configurationObject.isValid();
		}

		if((configurationObject.getNotValidSince() == 0) || (configurationObject.getNotValidSince() > version)) {
			// 0 bedeutet, dass der Zeitpunkt an dem das Objekt ung�ltig wird noch nicht gesetzt ist.
			// Ist die Version, in der das Objekt ung�ltig wird gr��er als die Version in der das Objekt g�ltig sein muss,
			// so ist es ebenfalls g�tig.
			return true;
		}
		else {
			// Das Objekt, das referenziert wird, ist ung�ltig
			return false;
		}
	}

	/**
	 * Erzeugt den ersten Teil einer Fehlermeldung, die zu einem lokalen Fehler geh�rt. Der erste Teil ist bei jedem lokalen Fehler identisch. Der Fehlertext hat
	 * folgende Form.
	 * <p/>
	 * "Text " "pid des Konfigurationsbereichs" " Text " "errorType" ". "
	 *
	 * @param configArea Konfigurationsbereich, in dem der lokale Fehler aufgetreten ist
	 * @param errorType  Art des Fehlers, der aufgetreten ist
	 *
	 * @return StringBuffer, in dem weitere Informationen eingetragen werden k�nnen
	 */
	private StringBuffer createLocalErrorMessagePartOne(
			ConfigurationArea configArea, String errorType) {
		final StringBuffer localErrorText = new StringBuffer(
				"Lokaler Fehler im Konfigurationsbereich " + configArea.getPid() + " gefunden, Art des Fehlers: " + errorType + ". "
		);
		return localErrorText;
	}

	/**
	 * Diese Methode ermittelt anhand eines Objekt-Typen seine s�mtlichen Mengenverwendungen.
	 *
	 * @param systemObjectType der Objekt-Typ
	 *
	 * @return Alle Mengenverwendungen dieses Objekt-Typs.
	 */
	List<ObjectSetUse> getObjectSetUses(final SystemObjectType systemObjectType) {
		final List<ObjectSetUse> objectSetUses = new LinkedList<ObjectSetUse>();

		if(_areaVersionMap.containsKey(systemObjectType.getConfigurationArea())) {
			// Version, in der die Objekte g�ltig sein m�ssen
			final Short version = _areaVersionMap.get(systemObjectType.getConfigurationArea());

			// Der Typ soll in einer neuen Version aktiviert werden -> Die zu aktivierende Version muss betrachtet werden
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
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
		else {
			// Es kann die aktuelle Version des Bereichs benutzt werden
			final List<SystemObject> setUses = systemObjectType.getNonMutableSet("Mengen").getElements();
			for(SystemObject systemObject : setUses) {
				final ObjectSetUse setUse = (ObjectSetUse)systemObject;
				objectSetUses.add(setUse);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
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
			// Version, in der die Objekte g�ltig sein m�ssen
			final Short version = _areaVersionMap.get(systemObjectType.getConfigurationArea());

			// Der Typ soll in einer neuen Version aktiviert werden -> Die zu aktivierende Version muss betrachtet werden
			// Die Menge der Mengenverwendungen ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
			final List<SystemObject> atgs = systemObjectType.getNonMutableSet("Attributgruppen").getElementsInVersion(version);
			for(SystemObject systemObject : atgs) {
				final AttributeGroup atg = (AttributeGroup)systemObject;
				allATGs.add(atg);
			}
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
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
			// Typen holen -> die Menge ist erforderlich, deshalb muss hier nicht auf != null gepr�ft werden
			final List<SystemObject> superTypes = systemObjectType.getNonMutableSet("SuperTypen").getElements();
			for(SystemObject systemObject : superTypes) {
				final SystemObjectType objectType = (SystemObjectType)systemObject;
				allATGs.addAll(getAttributeGroups(objectType));
			}
		}
		return allATGs;
	}

	/**
	 * Diese Methode sucht f�r ein Objekt die aktive Version. Befindet sich das Objekt in einem Bereich, der aktiviert werden soll, ist die aktive Version die
	 * Version in der der Bereich aktiviert werden soll. Wird der Bereich nicht mit einer neuen Version aktiviert, so wird die derzeit aktive Version
	 * zur�ckgegeben.
	 *
	 * @param systemObject Objekt, zu dem die aktive Version des Bereich ermittelt werden soll
	 *
	 * @return Version, in der der Bereich nach (angenommener) erfolgreicher Konsistenzpr�fung laufen wird
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
	 * Diese Methode gibt die Version zur�ck, in der eine Abh�ngigkeit zu dem �bergebenen Objekt besteht. Wird ein Konfigurationsobjekt �bergeben, so wird die
	 * Version zur�ckgegeben, in der das Objekt erzeugt wurde.
	 * <p/>
	 * Wird ein dynamisches Objekt �bergeben, so wird die Version gesucht, in der das dynamische Objekt erzeugt wurde und diese zur�ckgegeben. (Ein dynamisches
	 * Objekt entsteht zu einem Zeitpunkt, zu diesem Zeitpunkt muss eine Version aktiv gewesen sein, diese wird zur�ckgegeben.)
	 *
	 * @param systemObject Objekt, dessen Version gefunden werden soll
	 *
	 * @return Version, in der das Objekt ben�tigt wird um eine Abh�ngigkeit aufzul�sen.
	 */
	private short getDependenceVersion(final SystemObject systemObject) {
		if(systemObject instanceof ConfigurationObject) {
			ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
			return configurationObject.getValidSince();
		}
		else {
			final DynamicObject dynamicObject = (DynamicObject)systemObject;
			// Zeitpunkt, an dem das Objekt g�ltig geworden ist.
			final long validSinceTime = dynamicObject.getValidSince();

			final ConfigDataModel configDataModel = (ConfigDataModel)dynamicObject.getDataModel();
			final ConfigAreaFile areaFile = (ConfigAreaFile)(configDataModel.getConfigurationFileManager().getAreaFile(dynamicObject.getConfigurationArea().getPid()));

			short neededVersion = areaFile.getActiveVersion(validSinceTime, ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME);

			// Wenn der Bereich des referenzierten Objekts noch nicht aktiviert war, dann wird 1 als mindestens ben�tigte Version zur�ckgegeben
			if(neededVersion <= 0) neededVersion = 1;

			return neededVersion;
		}
	}
}
