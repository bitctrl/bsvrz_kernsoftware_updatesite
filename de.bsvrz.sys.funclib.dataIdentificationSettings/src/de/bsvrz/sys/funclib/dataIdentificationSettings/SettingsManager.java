/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.dataIdentificationSettings.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.dataIdentificationSettings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.dataIdentificationSettings; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.dataIdentificationSettings;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.MutableSetChangeListener;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectCollection;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.sys.funclib.asyncReceiver.AsyncReceiver;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Diese Klasse dient zur Verwaltung von Parameters�tzen mit Einstellungen die sich auf Datenidentifikationen beziehen. Derartige Parameterdatens�tze werden
 * z.B. zur Steuerung des Archivverhaltens (atg.archiv) und der Parametrierung (atg.parametrierung) eingesetzt. �ber die Parameterdatens�tze k�nnen in einzelnen
 * Eintr�gen mit Hilfe von Aufz�hlungen und Wildcards Einstellungen f�r viele Datenidentifikation auf einmal eingeben werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8324 $
 */
public class SettingsManager {

	private static final Debug _debug = Debug.getLogger();

	private final ClientDavInterface _connection;

	private final DataIdentification _parameterIdentification;

	private short _simulationVariantForTypeAndSetWildcardExpansion;

	private final List<UpdateListener> _updateListeners = new LinkedList<UpdateListener>();

	private final List<EndOfSettingsListener> _endOfSettingsListener = new LinkedList<EndOfSettingsListener>();

	private ClientReceiverInterface _receiver = null;

	private Map<DataIdentification, Data> _settingsTable;

	private final boolean _aspectUsed;

	private final boolean _simulationVariantUsed;

	/** Dieses Set speichert alle Attributgruppen, die �ber den SettingsManager nicht angemeldet werden d�rfen. Sonst gibt es ein Senke - Empf�nger - Konflikt. */
	private final Set<AttributeGroup> _excludedAttributeGroups = new HashSet<AttributeGroup>();

	/** Die dynamischen Objekt-Typen werden hier gespeichert, da zur Laufzeit Objekte dieser Typen erstellt und auf ung�ltig gesetzt werden k�nnen. */
	private final Set<DynamicObjectType> _dynamicObjectTypes = new HashSet<DynamicObjectType>();

	/**
	 * Die dynamischen Objekte werden hier gespeichert, da zur Laufzeit diese Objekte ung�ltig werden k�nnen und dann m�ssen die Einstellungen neu publiziert
	 * werden.
	 */
	private final Set<DynamicObject> _dynamicObjects = new HashSet<DynamicObject>();

	/**
	 * Die �nderbaren Mengen werden hier gespeichert, da sich zur Laufzeit der Inhalt der Elemente �ndern kann. Dann m�ssen die neuen Einstellungen publiziert
	 * werden.
	 */
	private final Set<MutableSet> _mutableSets = new HashSet<MutableSet>();

	/** Wurden dynamische Typen angegeben, so registriert sich dieses Objekt bei diesen, um �nderungen mitzubekommen. */
	private final RegisterDynamicListener _registerDynamicListener = new RegisterDynamicListener();

	/** Speichert den zuletzt empfangenen Datensatz mit den Einstellungen. */
	private Data _lastSettingsData;

	/**
	 * Runnable-Objekt des Threads, der asynchron die �nderungen der Einstellungen und dynamischen Objekten/Mengen verarbeitet und die angemeldeten Listener
	 * informiert.
	 */
	ChangeNotifier _changeNotifier = new ChangeNotifier();

	private ParameterSender _sender;

	/**
	 * Erzeugt ein neues Verwaltungsobjekt.
	 * Beim Zugriff auf dynamische Mengen und dynamischen Typen wird zur Aufl�sung
	 * von Wildcards f�r Elemente von dynamischen Mengen und dynamischen Typen die Simulationsvariante der angegebenen Datenidentifikation des
	 * Parameterdatensatzes oder die Simulationsvariante 0 verwendet, falls in der Datenidentifikation des Parameterdatensatzes keine explizite Angabe der
	 * Simulationsvariante gemacht wurde.
	 *
	 * @param connection              Verbindung zum Datenverteiler
	 * @param parameterIdentification der Parameterdatensatz
	 */
	public SettingsManager(ClientDavInterface connection, DataIdentification parameterIdentification) {
		this(connection, parameterIdentification, parameterIdentification.getDataDescription().getSimulationVariant());
	}

	/**
	 * Erzeugt ein neues Verwaltungsobjekt.
	 *
	 * @param connection              Verbindung zum Datenverteiler
	 * @param parameterIdentification der Parameterdatensatz
	 * @param simulationVariantForTypeAndSetWildcardExpansion Simulationsvariante, die beim Zugriff auf dynamische Mengen und dynamischen Typen zur Aufl�sung
	 * von Wildcards verwendet werden soll.
	 */
	public SettingsManager(ClientDavInterface connection, DataIdentification parameterIdentification, short simulationVariantForTypeAndSetWildcardExpansion) {
		_connection = connection;
		_parameterIdentification = parameterIdentification;
		if(simulationVariantForTypeAndSetWildcardExpansion < 0) {
			_simulationVariantForTypeAndSetWildcardExpansion = 0;
		}
		else {
			_simulationVariantForTypeAndSetWildcardExpansion = simulationVariantForTypeAndSetWildcardExpansion;
		}
		AttributeListDefinition atl1;
		AttributeListDefinition atl2;
		atl1 = (AttributeListDefinition)_parameterIdentification.getDataDescription().getAttributeGroup().getAttribute("ParameterSatz").getAttributeType();
		atl2 = (AttributeListDefinition)atl1.getAttribute("DatenSpezifikation").getAttributeType();
		_aspectUsed = atl2.getAttribute("Aspekt") != null;
		_simulationVariantUsed = atl2.getAttribute("SimulationsVariante") != null;

		_debug.finest("_aspectUsed = " + _aspectUsed);

		// Hier werden die Attributgruppen bestimmt, die nicht f�r Anmeldungen publiziert werden d�rfen! Werden sie dennoch verwendet,
		// so gibt es ein Senke - Empf�nger - Konflikt.
		final DataModel configuration = connection.getDataModel();
		AttributeGroup atg = configuration.getAttributeGroup("atg.konfigurationsAnfrageSchnittstelle");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.konfigurationsSchreibAntwort");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.konfigurationsSchreibAnfrage");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.konfigurationsAntwort");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.konfigurationsAnfrage");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.archivAnfrageSchnittstelle");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.archivAntwort");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.archivAnfrage");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.angemeldeteApplikationen");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.telegrammLaufzeiten");
		if(atg != null) _excludedAttributeGroups.add(atg);
		atg = configuration.getAttributeGroup("atg.angemeldeteDatenidentifikationen");
		if(atg != null) _excludedAttributeGroups.add(atg);
	}

	/**
	 * Erg�nzt die Liste der Beobachter, die bei �nderung des Parameters zu informieren sind, um einen weiteren Eintrag.
	 *
	 * @param listener Neuer Beobachter.
	 */
	public void addUpdateListener(UpdateListener listener) {
		synchronized(_updateListeners) {
			_updateListeners.add(listener);
		}
	}

	/**
	 * L�scht einen Beobachter aus der Liste der Beobachter, die bei �nderung des Parameters zu informieren sind.
	 *
	 * @param listener Zu l�schender Beobachter.
	 */
	public void removeUpdateListener(UpdateListener listener) {
		synchronized(_updateListeners) {
			_updateListeners.remove(listener);
		}
	}

	/**
	 * Erg�nzt die Liste der Beobachter, die informiert werden wollen, sobald alle Einstellungen abgearbeitet wurden.
	 *
	 * @param listener neuer Beobachter
	 */
	public void addEndOfSettingsListener(EndOfSettingsListener listener) {
		synchronized(_endOfSettingsListener) {
			_endOfSettingsListener.add(listener);
		}
	}

	/**
	 * L�scht einen Beobachter aus der Liste der Beobachter, die informiert werden wollen, sobald alle Einstellungen abgearbeitet wurden.
	 *
	 * @param listener zu l�schender Beobachter
	 */
	public void removeEndOfSettingsListener(EndOfSettingsListener listener) {
		synchronized(_endOfSettingsListener) {
			_endOfSettingsListener.remove(listener);
		}
	}

	/**
	 * Meldet die im Konstruktor �bergebene Datenidentifikation an und startet damit auch die Verarbeitung und Weitergabe der alten und neuen Einstellungen pro
	 * Datenidentifikation aus erhaltenen Parameterdatens�tzen an die angemeldeten Beobachter.
	 *
	 * @throws IllegalStateException Wenn der Manager bereits gestartet wurde.
	 */
	public void start() {
		if(_receiver != null) throw new IllegalStateException("Ist bereits gestartet");
		_changeNotifier.reset();
		ClientReceiverInterface receiver = new AsyncReceiver(new Receiver());
		_settingsTable = new HashMap<DataIdentification, Data>();
		_connection.subscribeReceiver(
				receiver, _parameterIdentification.getObject(), _parameterIdentification.getDataDescription(), ReceiveOptions.normal(), ReceiverRole.receiver()
		);
		_receiver = receiver;
		final Thread notificationThread = new Thread(_changeNotifier, "SettingsManager.ChangeNotifier");
		notificationThread.setDaemon(true);
		notificationThread.start();
	}

	/**
	 * Meldet die im Konstruktor �bergebene Datenidentifikation wieder ab und beendet damit auch die Verarbeitung und Weitergabe der alten und neuen Einstellungen
	 * pro Datenidentifikation aus erhaltenen Parameterdatens�tzen an die angemeldeten Beobachter.
	 */
	public void stop() {
		if(_receiver == null) return;
		_changeNotifier.notifyTermination();
		_connection.unsubscribeReceiver(_receiver, _parameterIdentification.getObject(), _parameterIdentification.getDataDescription());
		_receiver = null;
		registerDynamicObjects(new HashSet<DynamicObject>());
		registerDynamicObjectTypes(new HashSet<DynamicObjectType>());
		registerMutableSets(new HashSet<MutableSet>());
	}

	/**
	 * Aktualisiert die Tabelle mit den Einstellungen je Datenidentifikation mit den jeweiligen Einstellungen aus dem �bergebenen Datensatz.
	 *
	 * @param settingsData
	 * @param newSettings  die neuen Einstellungen
	 */
	private void extractSettings(final Data settingsData, Map<DataIdentification, Data> newSettings) {
		// Objekt-Typen werden hier gespeichert. Diese werden am Ende der Methode weitergereicht, damit sich der SettingsManager sich auf �nderungen
		// an den Objekt-Typen anmeldet. Gleiches gilt f�r dynamische Objekte und �nderbare Mengen.
		final Set<DynamicObjectType> dynamicObjectTypes = new HashSet<DynamicObjectType>();
		final Set<DynamicObject> dynamicObjects = new HashSet<DynamicObject>();
		final Set<MutableSet> mutableSets = new HashSet<MutableSet>();

		DataModel configuration = _connection.getDataModel();
		Aspect parameterAspect = null;
		if(!_aspectUsed) parameterAspect = configuration.getAspect("asp.parameterVorgabe");
		_debug.finer("Parametrierte Daten = " + settingsData);
		if(settingsData != null) {
			boolean refreshParams = false;
			final Data modifiableDataCopy = settingsData.createModifiableCopy();
			Data parameterArray = modifiableDataCopy.getItem("ParameterSatz");
			//Schleife �ber die einzelnen Elemente des Arrays ParameterSatz
			for(Iterator iterator = parameterArray.iterator(); iterator.hasNext();) {
				final Data parameter = (Data)iterator.next();
				Data settings = parameter.getItem("Einstellungen");
				_debug.finer("parameter = " + parameter);
				_debug.finer("settings = " + settings);
				_debug.finer("parameter.getArray(\"Bereich\").getLength() = " + parameter.getArray("Bereich").getLength());
				final Data.ReferenceArray referenceArrayArea = parameter.getReferenceArray("Bereich");

				SystemObject[] specifiedConfigAreas = referenceArrayArea.getSystemObjectArray();
				refreshParams |= fixOldReferences(referenceArrayArea, specifiedConfigAreas);
				final boolean configAreasEmpty = specifiedConfigAreas.length == 0;
				specifiedConfigAreas = removeNullReferences(specifiedConfigAreas);

				for(Iterator dataSpecificationIterator = parameter.getItem("DatenSpezifikation").iterator(); dataSpecificationIterator.hasNext();) {
					final Data dataSpec = (Data)dataSpecificationIterator.next();
					final Data.ReferenceArray referenceArrayObject = dataSpec.getReferenceArray("Objekt");

					SystemObject[] specifiedObjects = referenceArrayObject.getSystemObjectArray();
					refreshParams |= fixOldReferences(referenceArrayObject, specifiedObjects);
					final boolean objectsEmpty = specifiedObjects.length == 0;
					specifiedObjects = removeNullReferences(specifiedObjects);

					final Data.ReferenceArray referenceArrayAttributeGroup = dataSpec.getReferenceArray("AttributGruppe");

					SystemObject[] specifiedAtgs = referenceArrayAttributeGroup.getSystemObjectArray();
					refreshParams |= fixOldReferences(referenceArrayAttributeGroup, specifiedAtgs);
					final boolean attributeGroupsEmpty = specifiedAtgs.length == 0;
					specifiedAtgs = removeNullReferences(specifiedAtgs);

					// Sortierung ist notwendig f�r das sp�tere bin�re Suchen in diesem Array s.u.
					Arrays.sort(specifiedAtgs);
					final HashSet<AttributeGroup> specifiedAtgsHash = new HashSet<AttributeGroup>();
					for(SystemObject specifiedAtg : specifiedAtgs) {
						if(specifiedAtg instanceof AttributeGroup) {
							AttributeGroup attributeGroup = (AttributeGroup)specifiedAtg;
							specifiedAtgsHash.add(attributeGroup);
						}
					}
					SystemObject[] specifiedAspects;
					final boolean aspectsEmpty;
					if(_aspectUsed) {

						final Data.ReferenceArray referenceArrayAspect = dataSpec.getReferenceArray("Aspekt");
						specifiedAspects = referenceArrayAspect.getSystemObjectArray();
						refreshParams |= fixOldReferences(referenceArrayAspect, specifiedAspects);
						aspectsEmpty = specifiedAspects.length == 0;
						specifiedAspects = removeNullReferences(specifiedAspects);

						// Sortierung ist notwendig f�r das sp�tere bin�re Suchen in diesem Array s.u.
						Arrays.sort(specifiedAspects);
					}
					else {
						specifiedAspects = new SystemObject[1];
						specifiedAspects[0] = parameterAspect;
						aspectsEmpty = false;
					}

					final HashSet<Aspect> specifiedAspectsHash = new HashSet<Aspect>();
					for(SystemObject specifiedAspect : specifiedAspects) {
						if(specifiedAspect instanceof Aspect) {
							Aspect aspect = (Aspect)specifiedAspect;
							specifiedAspectsHash.add(aspect);
						}
					}
					short simulationVariant = -1;
					if(_simulationVariantUsed) {
						simulationVariant = dataSpec.getScaledValue("SimulationsVariante").shortValue();
					}

					List<SystemObject> objectList;
					if(objectsEmpty) {
						// keine Objekte sind alle Objekte
						// Anmeldung auf relevante dynamische Typen
						registerDynamicObjectTypeOrSubTypes(
								dynamicObjectTypes, (DynamicObjectType)configuration.getType(Pid.Type.DYNAMIC_OBJECT), specifiedAtgsHash, specifiedAspectsHash
						);
//						dynamicObjectTypes.add((DynamicObjectType)configuration.getType(Pid.Type.DYNAMIC_OBJECT));
						// d.h. alle dynamischen Objekte und alle Konfigurationsobjekte
						List<SystemObject> dynObjects = ((MutableCollection)configuration.getType(Pid.Type.DYNAMIC_OBJECT)).getElements(
								_simulationVariantForTypeAndSetWildcardExpansion
						);
						List<SystemObject> configurationObjects = configuration.getType(Pid.Type.CONFIGURATION_OBJECT).getObjects();
						objectList = new ArrayList<SystemObject>(dynObjects.size() + configurationObjects.size());
						objectList.addAll(dynObjects);
						objectList.addAll(configurationObjects);
					}
					else {
						objectList = new ArrayList<SystemObject>(specifiedObjects.length);
						for(SystemObject object : specifiedObjects) {
							if(object instanceof DynamicObjectType) {
								final DynamicObjectType dynamicObjectType = (DynamicObjectType)object;
								// auf alle relevanten dynamischen Typen anmelden, falls neue Objekte hinzukommen oder welche ung�ltig werden
								registerDynamicObjectTypeOrSubTypes(dynamicObjectTypes, dynamicObjectType, specifiedAtgsHash, specifiedAspectsHash);
//								dynamicObjectTypes.add(dynamicObjectType);
								objectList.addAll(dynamicObjectType.getElements(_simulationVariantForTypeAndSetWildcardExpansion));
							}
							else if(object instanceof MutableSet) {
								final MutableSet mutableSet = (MutableSet)object;
								// �nderbare Mengen merken, falls sich dort etwas �ndert
								mutableSets.add(mutableSet);
								// alle Elemente der Menge ber�cksichtigen
								objectList.addAll(mutableSet.getElements(_simulationVariantForTypeAndSetWildcardExpansion));
							}
							else if(object instanceof SystemObjectCollection) {
								// ein SystemObjectType ist auch eine SystemObjectCollection
								SystemObjectCollection collection = (SystemObjectCollection)object;
								// Typen und Mengen werden durch die jeweils enthaltenen Elemente ersetzt
								objectList.addAll(collection.getElements());
							}
							else {
								objectList.add(object);
							}
						}
					}

					// Einschr�nkung auf ausgew�hlte Konfigurationsbereiche
					if(!configAreasEmpty) {
						_debug.finest("specifiedConfigAreas.length = " + specifiedConfigAreas.length);
						List<SystemObject> specifiedConfigAreaList = Arrays.asList(specifiedConfigAreas);
						List<SystemObject> newObjectList = new LinkedList<SystemObject>();
						for(final SystemObject systemObject : objectList) {
							ConfigurationArea configurationArea = systemObject.getConfigurationArea();
							if(specifiedConfigAreaList.contains(configurationArea)) {
								newObjectList.add(systemObject);
							} // alle anderen werden aus der Objektliste gel�scht
						}
						objectList.clear();
						objectList.addAll(newObjectList);
					}
					// Folgende Map Enth�lt als Schl�ssel die zu betrachtenden Typen und als Wert jeweils ein Set mit den zu
					// betrachtenden Objekten des Typs:
					Map<SystemObjectType, Collection<SystemObject>> type2objectSetMap = new TreeMap<SystemObjectType, Collection<SystemObject>>();

					// Erzeugen der Map, d.h. sortieren nach Typen und sicherstellen, dass jedes Objekt nur einmal enthalten ist.
					for(final SystemObject object : objectList) {
						SystemObjectType type = object.getType();
						// wenn das Objekt nicht g�ltig ist, nicht weiter betrachten
						


						if(!object.isValid()) continue;
						Collection<SystemObject> objectCollection = type2objectSetMap.get(type);
						if(objectCollection == null) {
							objectCollection = new LinkedList<SystemObject>();
							type2objectSetMap.put(type, objectCollection);
						}
						objectCollection.add(object);
					}

					objectList.clear();
					objectList = null;
					// FIXME: Riesige Funktion, in Methoden auslagern?

					// Schleife �ber Map mit Typ/ObjektSet Paaren
					for(Map.Entry<SystemObjectType, Collection<SystemObject>> entry : type2objectSetMap.entrySet()) {
						SystemObjectType type = entry.getKey();
						Collection<SystemObject> objectCollection = entry.getValue();
						// Im folgenden werden alle Attributgruppen des Typs betrachtet und diese eventuell weiter
						// eingeschr�nkt, dadurch werden unsinnige Spezifikation ignoriert.
						List<AttributeGroup> typeAtgs = type.getAttributeGroups();
						for(AttributeGroup typeAtg : typeAtgs) {
							// Wenn keine Attributgruppen spezifiziert wurden, dann werden alle des Typs weiter betrachtet,
							// ansonsten werden nur die Attributgruppen des Typs weiter betrachtet, die auch explizit
							// spezifiziert wurden.
							if(!attributeGroupsEmpty && Arrays.binarySearch(specifiedAtgs, typeAtg) < 0) continue;

							// Wenn die betrachtete Attributgruppe in der Liste der nicht erlaubten Attributgruppen ist, wird
							// diese nicht weiter betrachtet.
							if(_excludedAttributeGroups.contains(typeAtg)) continue;

							Collection<Aspect> atgAspects;

							atgAspects = typeAtg.getAspects();

							for(Aspect atgAspect : atgAspects) {
								// Wenn kein Aspekt spezifiziert wurde, dann werden alle Aspekte der Attributgruppe weiter betrachtet,
								// ansonsten werden nur die Aspekte weiter betrachtet, die auch explizit
								// spezifiziert wurden.
								if(!aspectsEmpty && Arrays.binarySearch(specifiedAspects, atgAspect) < 0) continue;

								// Konfigurierende Attributgruppenverwendungen werden ignoriert
								if(typeAtg.getAttributeGroupUsage(atgAspect).isConfigurating()) continue;

								//F�r alle spezifizierten Objekte des betrachteten Typs die spezifizierte Einstellung merken
								for(SystemObject object : objectCollection) {
									// alle dynamischen Objekte speichern, deren Typ nicht bereits f�r �nderungen gespeichert wird,
									// damit sich der SettingsManager darauf anmelden kann
									if(object instanceof DynamicObject && !dynamicObjectTypes.contains(object.getType())) {
										dynamicObjects.add((DynamicObject)object);
									}
									final DataDescription dataDescription;
									if(_simulationVariantUsed) {
										dataDescription = new DataDescription(typeAtg, atgAspect, simulationVariant);
									}
									else {
										dataDescription = new DataDescription(typeAtg, atgAspect);
									}
									DataIdentification key = new DataIdentification(object, dataDescription);
									newSettings.put(key, settings);
								}
							}
						}
					}
					

				}
			}
			if(refreshParams){
				sendNewParams(modifiableDataCopy);
			}
		}
		// Anmelden auf �nderungen der dynamischen Objekt-Typen (hier k�nnen zur Laufzeit Objekte angelegt werden)
		registerDynamicObjectTypes(dynamicObjectTypes);
		registerDynamicObjects(dynamicObjects);
		registerMutableSets(mutableSets);
	}

	/**
	 * Sucht aus einem Objekt-Array veraltete Eintr�ge, aktualisiert diese, und korrigiert das �bergebene Daten-Array gleich mit
	 * @param referenceArray Datenobjekt
	 * @param objects Systemobjekt-Array
	 * @return true wenn etwas ver�ndert wurde (d.h. wenn das Array behebbare ung�ltige Objekte enthielt)
	 */
	private boolean fixOldReferences(final Data.ReferenceArray referenceArray, final SystemObject[] objects) {
		boolean arrayChanged = false;
		for(int i = 0; i < objects.length; i++) {
			if(objects[i] != null && !objects[i].isValid()) {
				final SystemObject systemObject = getCurrentObject(objects[i].getPid());
				// Falls kein aktuelles Systemobjekt gefunden wurde, das veraltete Objekt im Array lassen,
				// da der Settingsmanager f�lschlicherweise annehmen k�nnte, dass das Array leer w�re und so alle Objekte ausw�hlen w�rde
				
				if(systemObject != null && !objects[i].equals(systemObject)) {
					objects[i] = systemObject;
					referenceArray.getReferenceValue(i).setSystemObject(systemObject);
					arrayChanged = true;
				}
			}
		}
		return arrayChanged;
	}

	private SystemObject getCurrentObject(final String pid) {
		return _connection.getDataModel().getObject(pid);
	}

	/**
	 * Sendet, falls die geladenen Parameterdaten veraltete Objektreferenzen enthielten, einen neuen Vorgabedatensatz mit aktuellen Objekten.
	 * @param data Neuer, modifizierter/korrigierter Datensatz
	 */
	private void sendNewParams(final Data data) {
		_debug.info(_parameterIdentification + " enth�lt veraltete Objektreferenzen. Diese werden durch aktuelle ersetzt.");

		try {
			if(_sender == null) {
				_sender = new ParameterSender();
				final DataIdentification dataIdentification = _sender.getDataIdentification();
				_connection.subscribeSender(
						_sender, dataIdentification.getObject(), dataIdentification.getDataDescription(), SenderRole.sender()
				);
			}
			_sender.setData(data);
		}
		catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
			_debug.warning("Konnte nicht als Sender anmelden", oneSubscriptionPerSendData);
		}
	}

	/**
	 * Anmeldungen bei den relevanten �nderbaren Mengen auf �nderung.
	 *
	 * @param mutableSets die relevanten �nderbaren Mengen
	 */
	private void registerMutableSets(final Set<MutableSet> mutableSets) {
		// Anmeldungen, die nicht mehr ben�tigt werden, wieder abmelden
		for(Iterator<MutableSet> iterator = _mutableSets.iterator(); iterator.hasNext();) {
			MutableSet mutableSet = iterator.next();
			if(!mutableSets.contains(mutableSet)) {
				// abmelden
//				mutableSet.removeChangeListener(_registerDynamicListener);
				mutableSet.removeChangeListener(_simulationVariantForTypeAndSetWildcardExpansion, _registerDynamicListener);
				// Objekt aus dem Set l�schen
				iterator.remove();
			}
		}

		// nur auf die �nderbaren Mengen anmelden, auf die noch nicht angemeldet wurde
		for(MutableSet mutableSet : mutableSets) {
			if(!_mutableSets.contains(mutableSet)) {
				// anmelden
//				mutableSet.addChangeListener(_registerDynamicListener);
				mutableSet.addChangeListener(_simulationVariantForTypeAndSetWildcardExpansion, _registerDynamicListener);
				// Objekt dem Set hinzuf�gen
				_mutableSets.add(mutableSet);
			}
		}
	}

	/**
	 * Anmeldungen bei den relevanten dynamischen Objekten.
	 *
	 * @param dynamicObjects die relevanten dynamischen Objekte
	 */
	private void registerDynamicObjects(final Set<DynamicObject> dynamicObjects) {
		// Anmeldungen, die nicht mehr ben�tigt werden, wieder abmelden
		for(Iterator<DynamicObject> iterator = _dynamicObjects.iterator(); iterator.hasNext();) {
			DynamicObject dynamicObject = iterator.next();
			if(!dynamicObjects.contains(dynamicObject)) {
				// abmelden
				dynamicObject.removeListenerForInvalidation(_registerDynamicListener);
				// Objekt aus dem Set l�schen
				iterator.remove();
			}
		}

		// nur auf die dynamischen Objekte anmelden, auf die noch nicht angemeldet wurde
		for(DynamicObject dynamicObject : dynamicObjects) {
			if(!_dynamicObjects.contains(dynamicObject)) {
				// anmelden
				dynamicObject.addListenerForInvalidation(_registerDynamicListener);
				// Objekt dem Set hinzuf�gen
				_dynamicObjects.add(dynamicObject);
			}
		}
	}

	/**
	 * Registriert den angegebenen dynamischen Typ oder Sub-Typen davon zur Anmeldung auf neue und gel�schte Objekte, falls am Typ bzw. Sub-Typ passende
	 * Attributgruppen und Aspekte Online verwendet werden k�nnen.
	 * @param registeredDynamicObjectTypes Bereits registrierte Typen. Die neu registrierten Typen werden in dieses Set hinzugef�gt.
	 * @param toBeRegisteredType Zu betrachtender dynamischer Typ.
	 * @param specifiedAtgs Relevante Attributgruppen. Ein leeres Set wird als Wildcard f�r alle Attributgruppen ber�cksichtigt.
	 * @param specifiedAspects Relevante Aspekte. Ein leeres Set wird als Wildcard f�r alle Aspekte ber�cksichtigt.
	 */
	private void registerDynamicObjectTypeOrSubTypes(Set<DynamicObjectType> registeredDynamicObjectTypes, DynamicObjectType toBeRegisteredType, Set<AttributeGroup> specifiedAtgs, Set<Aspect> specifiedAspects) {
		if(registeredDynamicObjectTypes.contains(toBeRegisteredType)) return;
		boolean match = hasMatchingAttributGroupUsage(toBeRegisteredType, specifiedAtgs, specifiedAspects);
		if(match) {
			registerDynamicObjectType(registeredDynamicObjectTypes, toBeRegisteredType);
		}
		else {
			final List<SystemObjectType> subTypes = toBeRegisteredType.getSubTypes();
			for(SystemObjectType subType : subTypes) {
				if(subType instanceof DynamicObjectType) {
					DynamicObjectType dynamicSubType = (DynamicObjectType)subType;
					registerDynamicObjectTypeOrSubTypes(registeredDynamicObjectTypes, dynamicSubType, specifiedAtgs, specifiedAspects);
				}
			}
		}
	}

	/**
	 * Pr�ft, ob an dem angegebenen dynamischen Typ passende Attributgruppen und Aspekte Online verwendet werden k�nnen.
	 * @param toBeRegisteredType Zu betrachtender dynamischer Typ.
	 * @param specifiedAtgs Relevante Attributgruppen. Ein leeres Set wird als Wildcard f�r alle Attributgruppen ber�cksichtigt.
	 * @param specifiedAspects Relevante Aspekte. Ein leeres Set wird als Wildcard f�r alle Aspekte ber�cksichtigt.
	 * @return <code>true</code> falls es mindestens eine passende Attributgruppen-Aspekt-Kombination am zu betrachtenden Typ gibt, sonst <code>false</code>.
	 */
	private boolean hasMatchingAttributGroupUsage(
			final DynamicObjectType toBeRegisteredType, final Set<AttributeGroup> specifiedAtgs, final Set<Aspect> specifiedAspects) {
		final List<AttributeGroup> attributeGroups = toBeRegisteredType.getDirectAttributeGroups();
		for(AttributeGroup attributeGroup : attributeGroups) {
			if(specifiedAtgs.isEmpty() || specifiedAtgs.contains(attributeGroup)) {
				final Collection<AttributeGroupUsage> attributeGroupUsages = attributeGroup.getAttributeGroupUsages();
				for(AttributeGroupUsage attributeGroupUsage : attributeGroupUsages) {
					if(!attributeGroupUsage.isConfigurating() && (specifiedAspects.isEmpty() || specifiedAspects.contains(attributeGroupUsage.getAspect()))) {
						// Es gibt also am Typ eine passende Attributgruppe mit einem passenden Aspekt
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Registriert den angegebenen dynamischen Typ zur Anmeldung auf neue und gel�schte Objekte.
	 * @param registeredDynamicObjectTypes Bereits registrierte Typen. Der neu registrierte Typ wird in dieses Set hinzugef�gt.
	 * @param toBeRegisteredType Zu registrierender dynamischer Typ.
	 */
	private void registerDynamicObjectType(Set<DynamicObjectType> registeredDynamicObjectTypes, DynamicObjectType toBeRegisteredType) {
		if(registeredDynamicObjectTypes.contains(toBeRegisteredType)) return;
		registeredDynamicObjectTypes.add(toBeRegisteredType);
//		toBeRegisteredType.addObjectCreationListener(_registerDynamicListener);
//		toBeRegisteredType.addInvalidationListener(_registerDynamicListener);
		toBeRegisteredType.addChangeListener(_simulationVariantForTypeAndSetWildcardExpansion, _registerDynamicListener);
		// Typ dem Set hinzuf�gen
		_dynamicObjectTypes.add(toBeRegisteredType);
	}

	/**
	 * Anmeldungen bei den relevanten dynamischen Objekt-Typen.
	 *
	 * @param dynamicObjectTypes die relevante Objekt-Typen
	 */
	private void registerDynamicObjectTypes(Set<DynamicObjectType> dynamicObjectTypes) {
		// Anmeldungen, die nicht mehr ben�tigt werden, wieder abmelden
		for(Iterator<DynamicObjectType> iterator = _dynamicObjectTypes.iterator(); iterator.hasNext();) {
			DynamicObjectType dynamicObjectType = iterator.next();
			if(!dynamicObjectTypes.contains(dynamicObjectType)) {
				// abmelden
//				dynamicObjectType.removeObjectCreationListener(_registerDynamicListener);
//				dynamicObjectType.removeInvalidationListener(_registerDynamicListener);
				dynamicObjectType.removeChangeListener(_simulationVariantForTypeAndSetWildcardExpansion, _registerDynamicListener);
				// Typ aus dem Set l�schen
				iterator.remove();
			}
		}

		// nur auf die Objekt-Typen anmelden, auf die noch nicht angemeldet
		for(DynamicObjectType dynamicObjectType : dynamicObjectTypes) {
			if(!_dynamicObjectTypes.contains(dynamicObjectType)) {
				// anmelden
//				dynamicObjectType.addObjectCreationListener(_registerDynamicListener);
//				dynamicObjectType.addInvalidationListener(_registerDynamicListener);
				dynamicObjectType.addChangeListener(_simulationVariantForTypeAndSetWildcardExpansion, _registerDynamicListener);
				// Typ dem Set hinzuf�gen
				_dynamicObjectTypes.add(dynamicObjectType);
			}
		}
	}

	/** Klasse, die sich auf �nderungen von dynamischen Objekten (erstellen und ung�ltig setzen), ObjektTypen und �nderbaren Mengen anmeldet. */
	private final class RegisterDynamicListener implements DynamicObjectType.DynamicObjectCreatedListener, InvalidationListener, MutableSetChangeListener, MutableCollectionChangeListener {

		public void objectCreated(DynamicObject createdObject) {
			_changeNotifier.notifyDynamicChange();
		}

		public void invalidObject(DynamicObject dynamicObject) {
			_changeNotifier.notifyDynamicChange();
		}

		public void update(MutableSet set, SystemObject[] addedObjects, SystemObject[] removedObjects) {
			_changeNotifier.notifyDynamicChange();
		}

		public void collectionChanged(
				MutableCollection mutableCollection, short simulationVariant, List<SystemObject> addedElements, List<SystemObject> removedElements) {
			_changeNotifier.notifyDynamicChange();
		}
	}

	private SystemObject[] removeNullReferences(SystemObject[] objects) {
		int numberOfNonNullObjects = 0;
		for(int i = 0; i < objects.length; i++) {
			if(objects[i] != null) numberOfNonNullObjects++;
		}
		if(numberOfNonNullObjects != objects.length) {
			SystemObject[] trimmedObjects = new SystemObject[numberOfNonNullObjects];
			int trimmedIndex = 0;
			for(int i = 0; i < objects.length; i++) {
				if(objects[i] != null) trimmedObjects[trimmedIndex++] = objects[i];
			}
			return trimmedObjects;
		}
		else {
			return objects;
		}
	}

	/**
	 * Iteriert �ber die Tabelle mit den Einstellungen je Datenidentifikation und informiert die angemeldeten Beobachter �ber �nderungen an den Einstellungen je
	 * Datenidentifikation. Die �bergebenen Einstellungen werden als aktuelle Einstellungen �bernommen.
	 *
	 * @param newSettingsTable Map mit Einstellungen je Datenidentifikation
	 */
	private void activateSettings(Map<DataIdentification, Data> newSettingsTable) {
		// Schleife �ber Zeilen der neuen Tabelle
		// entsprechender Eintrag in alter Tabelle wird gel�scht
		// Benachrichtigung an alle Beobachter
		long startTime = System.currentTimeMillis();
		_debug.fine("+++aktiviere " + newSettingsTable.size() + " neue Einstellungen");
		for(Iterator<Map.Entry<DataIdentification,Data>> iterator = newSettingsTable.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<DataIdentification, Data> entry = iterator.next();
			DataIdentification dataIdentification = entry.getKey();
			Data newSettings = entry.getValue();
			Data oldSettings = (Data)_settingsTable.remove(dataIdentification);
			notifySettings(dataIdentification, oldSettings, newSettings);
		}
		long duration = System.currentTimeMillis() - startTime;
		_debug.finer(
				"+++Ende Aktivierung " + newSettingsTable.size() + " neue Einstellungen (Zeit: " + duration + " ms [pro Parametersatz "
				+ ((double)duration / (double)((newSettingsTable.size() == 0) ? 1 : (newSettingsTable.size()))) + " ms]"
		);

		_debug.fine("---deaktiviere verbleibende " + _settingsTable.size() + " alte Einstellungen");
		startTime = System.currentTimeMillis();
		// Schleife �ber die verbleibenden Eintr�ge in der alten Tabelle, das sind die Eintr�ge, zu denen es keine
		// neuen Eintr�ge mehr gibt. Jeweils entsprechende Benachrichtigung der angemeldeten Beobachter.
		for(Iterator<Map.Entry<DataIdentification,Data>> iterator = _settingsTable.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<DataIdentification, Data> entry = iterator.next();
			DataIdentification dataIdentification = entry.getKey();
			Data oldSettings = (Data)_settingsTable.get(dataIdentification);
			notifySettings(dataIdentification, oldSettings, null);
		}
		duration = System.currentTimeMillis() - startTime;
		_debug.finer(
				"+++Ende Deaktivierung " + _settingsTable.size() + " alte Einstellungen (Zeit: " + duration + "ms [pro Parametersatz "
				+ ((double)duration / (double)((_settingsTable.size() == 0) ? 1 : (_settingsTable.size()))) + " ms]"
		);
		// Neue Einstellungstabelle wird als aktuelle �bernommen
		_settingsTable.clear();
		_settingsTable = newSettingsTable;
		notifyEndOfSettings();
	}

	/**
	 * Iteriert �ber alle Beobachter und gibt die Datenidentifikation mit alten und neuen Einstellungen an die {@link UpdateListener#update update(...)} Methode
	 * weiter.
	 *
	 * @param dataIdentification Betroffene Datenidentifikation.
	 * @param oldSettings        Zur Datenidentifikation geh�rende Einstellung vor der �nderung oder <code>null</code> wenn es vor der �nderung keinen spezifischen
	 *                           Eintrag gab.
	 * @param newSettings        Zur Datenidentifikation geh�rende Einstellung nach der �nderung oder <code>null</code>
	 */
	private void notifySettings(DataIdentification dataIdentification, Data oldSettings, Data newSettings) {
		//Schleife �ber alle Beobachter und jeweils Aufruf der update-Methode
		synchronized(_updateListeners) {
			for(Iterator<UpdateListener> iterator = _updateListeners.iterator(); iterator.hasNext();) {
				UpdateListener listener = iterator.next();
				listener.update(dataIdentification, oldSettings, newSettings);
			}
		}
	}

	/** Iteriert �ber alle Beobachter, die informiert werden wollen, sobald alle Einstellungen an den {@link UpdateListener} geschickt wurden. */
	private void notifyEndOfSettings() {
		synchronized(_endOfSettingsListener) {
			for(Iterator<EndOfSettingsListener> iterator = _endOfSettingsListener.iterator(); iterator.hasNext();) {
				EndOfSettingsListener endOfSettingsListener = iterator.next();
				endOfSettingsListener.inform();
			}
		}
	}

	/** Klasse, die zur Entgegennahme der Parameterdatens�tze vom Datenverteiler die entsprechende Update-Methode implementiert. */
	private class Receiver implements ClientReceiverInterface {

		/** Flag, dass daf�r sorgt, dass initiale leere Datens�tze ignoriert werden */
		private boolean _seenNonEmptyData = false;

		/**
		 * Der Datensatzindex des zuletzt verarbeiteten Parameterdatensatz wird als Workaround benutzt um doppelte Datens�tze zu erkennen und zu ignorieren.
		 * <p/>
		 * Doppelte Datens�tze k�nnen bei ver�nderten AnmeldeOptionen einer zweiten Anmeldung auf diese Datenidentifikation auftreten.
		 */
		private long _lastDataIndex = 0;

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird. Diese Methode muss
		 * von der Applikation zur Verarbeitung der empfangenen Datens�tze implementiert werden.
		 *
		 * @param results Feld mit den empfangenen Ergebnisdatens�tzen.
		 */
		public void update(ResultData results[]) {
			try {
				boolean gotNewData = false;
				for(int i = 0; i < results.length; i++) {
					ResultData result = results[i];
					if(result.hasData()) {
						_seenNonEmptyData = true;
					}
					long resultDataIndex = result.getDataIndex();
					if(resultDataIndex != _lastDataIndex) {
						_lastDataIndex = resultDataIndex;
						gotNewData = true;
					}
				}
				if(_seenNonEmptyData && gotNewData) _changeNotifier.notifyData(results[results.length - 1].getData());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	class ChangeNotifier implements Runnable {

		private LinkedList<NotificationObject> _notificationQueue = new LinkedList<NotificationObject>();

		public void reset() {
			synchronized(_notificationQueue) {
				_notificationQueue.clear();
			}
		}

		public void notifyDynamicChange() {
			notify(new NotificationObject(NotificationType.DYNAMIC_CHANGE, null));
		}

		public void notifyData(Data data) {
			notify(new NotificationObject(NotificationType.NEW_DATA, data));
		}

		public void notifyTermination() {
			notify(new NotificationObject(NotificationType.TERMINATION, null));
		}

		private void notify(final NotificationObject notificationObject) {
			synchronized(_notificationQueue) {
				_notificationQueue.addFirst(notificationObject);
				_notificationQueue.notifyAll();
			}
		}

		public void run() {
			try {
				_debug.fine("SettingsManager.ChangeNotifier wurde gestartet");
				while(!Thread.interrupted()) {
					Data lastDataGot = null;
					boolean gotNewData = false;
					boolean gotDynamicUpdate= false;
					synchronized(_notificationQueue) {
						// Warten auf neue Auftr�ge
						while(_notificationQueue.isEmpty()) _notificationQueue.wait();

						// Verz�gerung solange, bis mindestens 500 Millisekunden lang keine weiteren Aktualisierungen anliegen
						// Maximale Gesamtverz�gerung auf 10000 Millisekunden begrenzt um den Thread nicht verhungern zu lassen
						long time0 = System.currentTimeMillis();
						int lastSize = _notificationQueue.size();
						long timeMax = time0 + 10000;
						while(true) {
							_notificationQueue.wait(500);
							int newSize = _notificationQueue.size();
							if(lastSize == newSize) break;
							lastSize = newSize;
							if(System.currentTimeMillis() > timeMax) break;
						}

						// Aktualisierungen auslesen
						while(!_notificationQueue.isEmpty()) {
							final NotificationObject notificationObject = _notificationQueue.removeLast();
							switch(notificationObject.getType()) {
								case DYNAMIC_CHANGE:
									// Aktualisierung eines dynamischen Objekts oder einer dynamischen Menge
									gotDynamicUpdate = true;
									break;
								case NEW_DATA:
									// Neues Data wurde empfangen => nur das letzte verarbeiten
									gotNewData = true;
									lastDataGot = notificationObject.getData();
									break;
								case TERMINATION:
									// SettingsManager wurde gestoppt => Thread beenden
									return;
							}
						}
					}
					if(gotNewData) {
						_lastSettingsData = lastDataGot;
					}
					if(gotDynamicUpdate || gotNewData) {
						final HashMap<DataIdentification, Data> newSettings = new HashMap<DataIdentification, Data>();
						extractSettings(_lastSettingsData, newSettings);
						activateSettings(newSettings);
					}
				} 
				_debug.fine("SettingsManager.ChangeNotifier beendet sich");
			}
			catch(InterruptedException e) {
				_debug.fine("SettingsManager.ChangeNotifier wurde unterbrochen und beendet sich");
			}
		}

	}

	static enum NotificationType {
		TERMINATION, NEW_DATA, DYNAMIC_CHANGE
	}
	static class NotificationObject {

		private NotificationType _type;

		private Data _data;

		public NotificationObject(NotificationType type, Data data) {
			_type = type;
			_data = data;
		}

		public NotificationType getType() {
			return _type;
		}

		public Data getData() {
			return _data;
		}
	}

	private class ParameterSender implements ClientSenderInterface {

		private final DataIdentification _dataIdentification;

		private volatile Data _data = null;

		private volatile boolean _canSend = false;

		public void setData(final Data data) {
			_data = data;
			if(_canSend){
				sendData();
			}
		}

		public DataIdentification getDataIdentification() {
			return _dataIdentification;
		}

		public ParameterSender() {
			// Datenidentifikation erstellen
			// In _parameterIdentification steht der Aspekt ParameterSoll, es sollen aber neue Daten geschrieben werden, also brauchen wir den Aspekt ParameterVorgabe
			_dataIdentification = new DataIdentification(
					_parameterIdentification.getObject(), new DataDescription(
							_parameterIdentification.getDataDescription().getAttributeGroup(), _connection.getDataModel().getAspect("asp.parameterVorgabe")
					)
			);
		}

		public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
			_canSend = state == ClientSenderInterface.START_SENDING;
			if(_canSend && (_data != null)) {
				sendData();
			}
		}

		private void sendData() {
			try {
				_connection.sendData(
						new ResultData(
								_dataIdentification.getObject(), _dataIdentification.getDataDescription(), System.currentTimeMillis(), _data
						)
				);
				_data = null;
			}
			catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
				_debug.warning("Konnte keine neuen Daten versenden", sendSubscriptionNotConfirmed);
			}
		}

		public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
			return true;
		}

		@Override
		public String toString() {
			return "ParameterSender{" + "_dataIdentification=" + _dataIdentification + ", _data=" + _data + ", _canSend=" + _canSend + '}';
		}
	}
}
