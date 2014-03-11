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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.config.ConfigurationCommunicationListenerSupport;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.*;
import java.util.*;

import static de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications.CONFIGURATION_ELEMENTS_IN_MUTABLE_SET;

/**
 * Implementierung des Interfaces {@link MutableSet} für dynamische Mengen auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11495 $
 */
public class ConfigMutableSet extends ConfigObjectSet implements MutableSet {
	
	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();
	
	/** Objekt zur Verwaltung von Anmeldungen auf Änderungen der Elemente dieser Menge. */
	private ConfigMutableCollectionSupport _mutableCollectionSupport = new ConfigMutableCollectionSupport(this);

	/** Speichert alle angemeldeten Beobachter in Abhängigkeit zur Simulationsvariante. */
	private Map<Short, Set<MutableSetChangeListener>> _changeListeners;
	
	/** Objekt für den synchronisierten Zugriff auf die {@link #_changeListeners Listener}. */
	private final Object _lockListeners = new Object();
	
	/** Die aktuellen Elemente dieser dynamischen Menge. */
	private Set<SystemObject> _elements;
	
	/** Objekt für den synchronisierten Zugriff auf die {@link #_elements Elemente}. */
	private final Object _lockElements = new Object();
	
	/** Objekt für den synchronisierten Zugriff auf die Fields _elementAccessFieldsInitialized, _elementChangesAllowed und _elementsFile. */
	private final Object _lockElementAccessProperties = new Object();
	
	/** Wurden die Fields _elementChangesAllowed und _elementsFile schon initialisiert ? */
	private boolean _elementAccessFieldsInitialized;
	
	/** Dürfen Änderungen an der Elementzugehörigkeit durch diese Konfiguration durchgeführt werden? */
	private boolean _elementChangesAllowed;
	
	/**
	 * Datei in der die Elementzugehörigkeit dieser Menge gespeichert werden soll, oder <code>null</code>, falls die Elementzugehörigkeit als Datensatz der
	 * Menge gespeichert werden soll
	 */
	private File _elementsFile;
	
	/** Byte Array mit dem aus der Datei _elementsFile gelesenen oder noch zu schreibenden Datensatz mit der Elementzugehörigkeit der Menge */
	private byte[] _elementsDataBytes = null;
	
	/** Muss der in _elementsDataBytes enthaltene Datensatz noch in der Datei gespeichert werden? */
	private boolean _elementsDataBytesChanged = false;
	
	/** Delegations-Klasse für das Interface {@link ConfigurationCommunicationInterface} */
	ConfigurationCommunicationListenerSupport _configComHelper;

	private String _elementsManagementPid = "";

	/**
	 * Konstruktor einer dynamischen Menge.
	 * 
	 * @param configurationArea
	 *            Konfigurationsbereich dieser dynamischen Menge
	 * @param systemObjectInfo
	 *            das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigMutableSet(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
		_configComHelper = new ConfigurationCommunicationListenerSupport();
	}
	
	public void addChangeListener(MutableSetChangeListener listener) {
		addChangeListener(listener, (short)0);
	}
	
	public void addChangeListener(MutableSetChangeListener listener, short simulationVariant) {
		synchronized(_lockListeners) {
			// Map erzeugen, falls noch nicht vorhanden
			if(_changeListeners == null) {
				_changeListeners = new HashMap<Short, Set<MutableSetChangeListener>>();
			}
			// gibt es schon eine Liste für die angegebenen Simulationsvariante?
			Set<MutableSetChangeListener> listeners = _changeListeners.get(simulationVariant);
			if(listeners == null) {
				listeners = new HashSet<MutableSetChangeListener>();
				_changeListeners.put(simulationVariant, listeners); // in die Map eintragen
			}
			listeners.add(listener);
		}
	}
	
	public void removeChangeListener(MutableSetChangeListener listener) {
		removeChangeListener(listener, (short)0);
	}
	
	public void removeChangeListener(MutableSetChangeListener listener, short simulationsVariant) {
		synchronized(_lockListeners) {
			// es gibt nichts zu entfernen
			if(_changeListeners == null) {
				return;
			}
			final Set<MutableSetChangeListener> listeners = _changeListeners.get(simulationsVariant);
			if(listeners == null) {
				return; // es gibt immer noch nichts zu entfernen
			}
			listeners.remove(listener);
		}
	}
	
	/**
	 * Bei Änderungen an der dynamischen Menge werden alle angemeldeten Beobachter informiert. Die hinzugefügten und entfernten Elemente werden den Beobachtern
	 * ebenfalls mitgeteilt.
	 * 
	 * @param addedObjects
	 *            hinzugefügte Elemente
	 * @param removedObjects
	 *            entfernte Elemente
	 * @param simulationVariant
	 *            die Simulationsvariante
	 */
	void informListeners(SystemObject[] addedObjects, SystemObject[] removedObjects, short simulationVariant) {
		final List<SystemObject> addedElements = Arrays.asList(addedObjects);
		final List<SystemObject> removedElements = Arrays.asList(removedObjects);
		((ConfigDataModel)getDataModel()).sendCollectionChangedNotification(_mutableCollectionSupport, simulationVariant, addedElements, removedElements);
		synchronized(_lockListeners) {
			// falls kein Listener angemeldet wurde, muss auch niemandem Bescheid gegeben werden
			if(_changeListeners == null) {
				return;
			}
			// gibt es Listener zur Simulationsvariante?
			final Set<MutableSetChangeListener> listeners = _changeListeners.get(simulationVariant);
			if(listeners == null) {
				return; // zu dieser SV gibt es keine Listener
			}
			for(MutableSetChangeListener changeListener : listeners) {
				changeListener.update(this, addedObjects, removedObjects);
			}
		}
	}
	
	/**
	 * Löscht alle Elemente permanent aus dieser dynamischen Menge.
	 * 
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls die Elemente zur Simulationsvariante nicht gelöscht werden konnten oder nicht gelöscht werden dürfen (bei Simulationsvariante 0).
	 */
	public void deleteElements(short simulationVariant) throws ConfigurationChangeException {
		if(simulationVariant == 0) {
			throw new ConfigurationChangeException("Elemente mit Simulationsvariante '0' dürfen nicht aus einer dynamischen Menge gelöscht werden.");
		}
		
		synchronized(_lockElements) {
			// Datensatz auslesen
			final List<MutableElement> mutableElements = getMutableElements();
			final List<SystemObject> removedObjects = new ArrayList<SystemObject>();
			final long time = System.currentTimeMillis();
			
			// Elemente mit angegebener Simulationsvariante rausfiltern
			final List<MutableElement> filteredMutableElements = new ArrayList<MutableElement>();
			for(MutableElement mutableElement : mutableElements) {
				if(mutableElement.getSimulationVariant() != simulationVariant) {
					filteredMutableElements.add(mutableElement);
				}
				else if(mutableElement.getStartTime() <= time && (mutableElement.getEndTime() > time || mutableElement.getEndTime() == 0)) {
					removedObjects.add(mutableElement.getElement());
				}
			}
			
			// neue Liste abspeichern
			saveMutableSets(filteredMutableElements);
			informListeners(new SystemObject[0], removedObjects.toArray(new SystemObject[removedObjects.size()]), simulationVariant);
		}
	}
	
	public List<SystemObject> getElements() {
		synchronized(_lockElements) {
			if(_elements == null) {
				_elements = new HashSet<SystemObject>(getElementsWithSimulationVariant((short)0));
			}
			return Collections.unmodifiableList(new ArrayList<SystemObject>(_elements));
		}
	}
	
	public List<SystemObject> getElementsWithSimulationVariant(short simulationVariant) {
		return Collections.unmodifiableList(getElementsWithSimulationVariant(System.currentTimeMillis(), simulationVariant));
	}
	
	public List<SystemObject> getElements(long time) {
		return Collections.unmodifiableList(getElementsWithSimulationVariant(time, (short)0));
	}
	
	public List<SystemObject> getElementsWithSimulationVariant(long time, short simulationVariant) {
		synchronized(_lockElements) {
			final List<SystemObject> elements = new ArrayList<SystemObject>();
			for(MutableElement mutableElement : getMutableElements()) {
				if(mutableElement.getSimulationVariant() == simulationVariant && mutableElement.getStartTime() <= time
				        && (mutableElement.getEndTime() > time || mutableElement.getEndTime() == 0)) {
					elements.add(mutableElement.getElement());
				}
			}
			// gibt alle Elemente zurück, die zu dieser Zeit mit dieser Simulationsvariante gültig sind
			return Collections.unmodifiableList(elements);
		}
	}
	
	public List<SystemObject> getElementsInPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(getElementsInPeriod(startTime, endTime, (short)0));
	}
	
	/**
	 * Bestimmt die Elemente, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs zur Zusammenstellung gehört haben in Abhängigkeit der
	 * Simulationsvariante.
	 * 
	 * @param startTime
	 *            Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime
	 *            Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @return Liste mit den zu mindestens einem Zeitpunkt des Zeitbereichs zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsInPeriod(long startTime, long endTime, short simulationVariant) {
		synchronized(_lockElements) {
			final List<SystemObject> elements = new ArrayList<SystemObject>();
			for(MutableElement mutableElement : getMutableElements()) {
				if(mutableElement.getSimulationVariant() == simulationVariant && mutableElement.getStartTime() <= endTime
				        && (mutableElement.getEndTime() > startTime || mutableElement.getEndTime() == 0)) {
					elements.add(mutableElement.getElement());
				}
			}
			return Collections.unmodifiableList(elements);
		}
	}
	
	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(getElementsDuringPeriod(startTime, endTime, (short)0));
	}
	
	/**
	 * Bestimmt die Elemente, die während des gesamten angegebenen Zeitbereichs zur Zusammenstellung gehört haben in Abhängigkeit der Simulationsvariante.
	 * 
	 * @param startTime
	 *            Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime
	 *            Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @return Liste mit den während des gesamten Zeitbereichs zur Zusammenstellung gehörenden System-Objekten.
	 */
	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime, short simulationVariant) {
		synchronized(_lockElements) {
			final List<SystemObject> elements = new ArrayList<SystemObject>();
			for(MutableElement mutableElement : getMutableElements()) {
				if(mutableElement.getSimulationVariant() == simulationVariant && mutableElement.getStartTime() <= startTime
				        && (mutableElement.getEndTime() > endTime || mutableElement.getEndTime() == 0)) {
					elements.add(mutableElement.getElement());
				}
			}
			return Collections.unmodifiableList(elements);
		}
	}
	
	/**
	 * Fügt ein Element zur dynamischen Menge in Abhängigkeit der Simulationsvariante.
	 * 
	 * @param object
	 *            Element, welches hinzugefügt werden soll
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls das Objekt nicht hinzugefügt werden kann/darf.
	 */
	public void add(SystemObject object, short simulationVariant) throws ConfigurationChangeException {
		add(new SystemObject[]{object}, simulationVariant);
	}
	
	public void add(SystemObject[] objects) throws ConfigurationChangeException {
		add(objects, (short)0);
	}
	
	/**
	 * Erweitert die add-Methode um eine Simulationsvariante, die angibt, in welcher Simulation diese Objekte der Menge hinzugefügt werden.
	 * 
	 * @param objects
	 *            die hinzuzufügenden Elemente
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Wenn mindestens eines der übergebenen Objekte nicht in die Menge aufgenommen werden konnte und noch nicht in der Menge enthalten war.
	 * @see #add(de.bsvrz.dav.daf.main.config.SystemObject[])
	 */
	public void add(SystemObject[] objects, short simulationVariant) throws ConfigurationChangeException {
		if(checkChangeElementsPermit()) {
			// Typ der Elemente prüfen
			checkObjectTypeOfElements(objects);
			
			synchronized(_lockElements) {
				// aktuell in der Menge vorhandene Elemente mit der angegebenen Simulationsvariante
				Set<SystemObject> currentElements = new HashSet<SystemObject>();
				if(simulationVariant == 0) {
					currentElements.addAll(getElements()); // aktuelle Elemente - sie werden gecached
				}
				else {
					currentElements.addAll(getElementsWithSimulationVariant(simulationVariant));
				}
				
				// nur die Elemente hinzufügen, die noch nicht enthalten sind
				final Set<SystemObject> elementsToAdd = new HashSet<SystemObject>();
				for(SystemObject systemObject : objects) {
					if(!currentElements.contains(systemObject)) {
						elementsToAdd.add(systemObject);
					}
				}
				
				// zukünftige Anzahl der Elemente in der Menge prüfen
				final int maximumElements = getObjectSetType().getMaximumElementCount();
				if(maximumElements > 0 && currentElements.size() + elementsToAdd.size() > maximumElements) {
					throw new ConfigurationChangeException(
					        "Elemente können nicht der dynamischen Menge hinzugefügt werden, da sonst die maximale Elementanzahl (" + maximumElements
					                + ") überschritten wird.");
				}
				
				// Elemente der Menge hinzufügen, wenn welche hinzuzufügen sind
				if(!elementsToAdd.isEmpty()) {
					try {
						final SystemObject[] systemObjectsToAdd = elementsToAdd.toArray(new SystemObject[elementsToAdd.size()]);
						
						// den konfigurierenden Datensatz speichern
						setConfigurationData(systemObjectsToAdd, null, simulationVariant);
						
						// aktuelle Menge auf den neuesten Stand bringen
						if(_elements != null && simulationVariant == 0) {
							_elements.addAll(elementsToAdd); // repräsentiert die aktuelle Menge
						}
						
						// die angemeldeten Listener informieren
						informListeners(systemObjectsToAdd, new SystemObject[0], simulationVariant);
					}
					catch(ConfigurationChangeException ex) {
						_elements = null; // die eingeladene Menge passt evtl. nicht mehr, deshalb löschen, damit sie neu eingeladen wird
						throw new ConfigurationChangeException(ex);
					}
				}
			}
		}
		else {
			throw new ConfigurationChangeException("Es liegt keine Berechtigung zum Verändern dieser Menge '" + getNameOrPidOrId() + "' vor."
			        + " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
			        + "' zuständig.");
		}
	}
	
	/**
	 * Prüft, ob die Konfiguration berechtigt ist an der Elementzugehörigkeit dieser Menge eine Änderung durchzuführen.
	 * 
	 * @return <code>true</code>, falls die Konfiguration die Berechtigung hat, Änderungen an der Elementzugehörigkeit dieser Menge durchzuführen, <br/>
	 *         <code>false</code>, falls die Konfiguration diese Berechtigung nicht hat.
	 */
	private boolean checkChangeElementsPermit() {
		loadElementAccessProperties();
		return _elementChangesAllowed;
	}
	
	private void loadElementAccessProperties() {
		synchronized(_lockElementAccessProperties) {
			if(!_elementAccessFieldsInitialized) {
				_elementChangesAllowed = getDataModel().getConfigurationAuthorityPid().equals(getConfigurationArea().getConfigurationAuthority().getPid());
				_elementsFile = null;
				
				final DataModel configuration = getDataModel();
				final AttributeGroup atg = configuration.getAttributeGroup("atg.dynamischeMenge");
				if(atg != null) {
					final Data data = this.getConfigurationData(atg);
					if(data != null) {
						_elementsManagementPid = data.getTextValue("verwaltung").getValueText();
						if(!_elementsManagementPid.equals("")) {
							_elementChangesAllowed = getDataModel().getConfigurationAuthorityPid().equals(_elementsManagementPid);
							if(_elementChangesAllowed) {
								_elementsFile = new File(((ConfigDataModel)getDataModel()).getManagementFile().getObjectSetDirectory(), String.valueOf(getId())
								                                                                                                        + ".menge");
							}
						}
					}
				}
				
				_elementAccessFieldsInitialized = true;
			}
		}
	}


	public String getElementsManagementPid() {
		loadElementAccessProperties();
		return _elementsManagementPid;
	}

	/**
	 * Prüft, ob alle Objekte vom richtigen Typ sind.
	 * 
	 * @param objects
	 *            zu prüfende Elemente
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls ein Objekt nicht vom erlaubten Typ der Menge ist.
	 */
	private void checkObjectTypeOfElements(final SystemObject[] objects) throws ConfigurationChangeException {
		// erlaubte Objekt-Typen dieser dynamischen Menge
		final Set<SystemObjectType> objectTypes = new HashSet<SystemObjectType>(getObjectSetType().getObjectTypes());
		for(SystemObject systemObject : objects) {
			boolean superTypesOk = false;
			// prüfen, ob das hinzuzufügende Element vom richtigen Typ ist
			// da die dynamische Menge ein konfigurierendes Objekt ist, kann die Typ-Überprüfung wegfallen, wenn sie noch nicht gültig (valid) ist
			// die Überprüfung findet dann in der Konsistenzprüfung statt
			if(!isValid() || objectTypes.contains(systemObject.getType())) {
				superTypesOk = true;
			}
			else {
				// ist es nicht in der Menge der erlaubten Typen, ist vielleicht einer der Super-Typen ein erlaubter Typ
				for(SystemObjectType objectType : objectTypes) {
					// evtl. handelt es sich um einen abgeleiteten Typen
					if(systemObject.getType().inheritsFrom(objectType)) {
						superTypesOk = true;
						// da dieser Typ von einem der anderen abgeleitet ist, kann dieser der Menge der erlaubten Typen hinzugefügt werden,
						// dies hat den Vorteil, dass bei einem weiteren Element die contains-Abfrage direkt zuschlägt
						objectTypes.add(systemObject.getType());
						break; // länger braucht nicht gesucht werden
					}
				}
			}
			
			// ist der Typ nicht ok, dann wird eine Fehlermeldung generiert.
			if(!superTypesOk) {
				throw new ConfigurationChangeException("Der Typ " + systemObject.getType().getNameOrPidOrId() + " des Objekts "
				        + systemObject.getNameOrPidOrId() + " ist in dieser dynamischen Menge " + getNameOrPidOrId() + " nicht erlaubt.");
			}
		}
	}
	
	/**
	 * Entfernt ein Element aus der dynamischen Menge in Abhängigkeit der Simulationsvariante.
	 * 
	 * @param object
	 *            Element, welches aus der dynamischen Menge entfernt werden soll
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls das Element nicht entfernt werden kann.
	 */
	public void remove(SystemObject object, short simulationVariant) throws ConfigurationChangeException {
		remove(new SystemObject[]{object}, simulationVariant);
	}
	
	public void remove(SystemObject[] objects) throws ConfigurationChangeException {
		remove(objects, (short)0);
	}
	
	/**
	 * Erweitert die remove-Methode um eine Simulationsvariante, die angibt, in welcher Simulation diese Objekte aus der Menge entfernt werden sollen.
	 * 
	 * @param objects
	 *            Elemente, welche aus der dynamischen Menge entfernt werden sollen
	 * @param simulationVariant
	 *            die Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls die Elemente nicht entfernt werden können.
	 */
	public void remove(SystemObject[] objects, short simulationVariant) throws ConfigurationChangeException {
		if(checkChangeElementsPermit()) {
			synchronized(_lockElements) {
				// aktuelle Elemente dieser Menge ermitteln
				final Set<SystemObject> currentElements = new HashSet<SystemObject>();
				if(simulationVariant == 0) {
					currentElements.addAll(getElements());
				}
				else {
					currentElements.addAll(getElementsWithSimulationVariant(simulationVariant));
				}
				
				// prüfen, ob auch alle Elemente in der Menge vorhanden sind
				final Set<SystemObject> elementsToRemove = new HashSet<SystemObject>();
				for(SystemObject systemObject : objects) {
					if(currentElements.contains(systemObject)) {
						elementsToRemove.add(systemObject);
					}
				}
				
				// Anzahl der minimalen Elemente prüfen
				final int minimumElements = getObjectSetType().getMinimumElementCount();
				if((currentElements.size() - elementsToRemove.size()) < minimumElements) {
					throw new ConfigurationChangeException(
					        "Elemente können nicht aus der dynamischen Menge entfernt werden, da sonst die minimale Elementanzahl (" + minimumElements
					                + ") unterschritten wird.");
				}
				
				// zu entfernende Elemente löschen
				if(!elementsToRemove.isEmpty()) {
					try {
						final SystemObject[] systemObjectsToRemove = elementsToRemove.toArray(new SystemObject[elementsToRemove.size()]);
						
						// den konfigurierenden Datensatz speichern
						setConfigurationData(null, systemObjectsToRemove, simulationVariant);
						
						// aktuelle Menge auf den neuesten Stand bringen
						if(_elements != null && simulationVariant == 0) {
							_elements.removeAll(elementsToRemove); // repräsentiert die aktuelle Menge
						}
						
						// die angemeldeten Listener informieren
						informListeners(new SystemObject[0], systemObjectsToRemove, simulationVariant);
					}
					catch(ConfigurationChangeException ex) {
						_elements = null; // die eingeladene Menge passt evtl. nicht mehr, deshalb löschen, damit sie neu eingeladen wird
						throw new ConfigurationChangeException(ex);
					}
				}
			}
		}
		else {
			throw new ConfigurationChangeException("Es liegt keine Berechtigung zum Verändern dieser Menge '" + getNameOrPidOrId() + "' vor."
			        + " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
			        + "' zuständig.");
		}
	}
	
	/**
	 * Speichert einen konfigurierenden Datensatz mit den übergebenen Änderungen der Elemente an der Menge.
	 * 
	 * @param addedElements
	 *            hinzugefügte Elemente
	 * @param removedElements
	 *            entfernte Elemente
	 * @param simulationVariant
	 *            Simulationsvariante
	 * 
	 * @throws ConfigurationChangeException
	 *             Wenn der konfigurierende Datensatz nicht geschrieben werden konnte.
	 */
	private void setConfigurationData(SystemObject[] addedElements, SystemObject[] removedElements, short simulationVariant)
	        throws ConfigurationChangeException {
		// alle Elemente aus der Menge holen einschließlich der nicht aktuellen (versionierten)
		final List<MutableElement> mutableElements = getMutableElements();
		// Liste anpassen
		long time = System.currentTimeMillis(); // benötigter Zeitstempel
		if(addedElements != null) {
			// neue Elemente hinzufügen
			for(SystemObject systemObject : addedElements) {
				mutableElements.add(new MutableElement(systemObject, time, 0, simulationVariant));
			}
		}
		if(removedElements != null) {
			// Elemente löschen
			for(SystemObject systemObject : removedElements) {
				for(MutableElement mutableElement : mutableElements) {
					// da Elemente mehrfach in die Menge eingefügt und entfernt werden können, muss auf EndTime == 0 abgefragt werden
					if(mutableElement.getElement().equals(systemObject) && mutableElement.getSimulationVariant() == simulationVariant
					        && mutableElement.getEndTime() == 0) {
						mutableElement.setEndTime(time);
					}
				}
			}
		}
		saveMutableSets(mutableElements);
	}
	
	/**
	 * Speichert die Elemente dieser Menge (auch historische) in einem konfigurierenden Datensatz ab.
	 * 
	 * @param mutableElements
	 *            Elemente dieser Menge
	 * 
	 * @throws ConfigurationChangeException
	 *             Falls die Elemente nicht in einem konfigurierenden Datensatz abgespeichert werden können.
	 */
	private void saveMutableSets(final List<MutableElement> mutableElements) throws ConfigurationChangeException {
		// Liste in ein Byte-Array packen und abspeichern
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
			for(MutableElement mutableElement : mutableElements) {
				serializer.writeLong(mutableElement.getElement().getId());
				serializer.writeLong(mutableElement.getStartTime());
				serializer.writeLong(mutableElement.getEndTime());
				serializer.writeShort(mutableElement.getSimulationVariant());
			}
			final byte[] bytes = out.toByteArray();
			if(_elementsFile != null) {
				_elementsDataBytes = bytes;
				if(!_elementsDataBytesChanged) {
					_elementsDataBytesChanged = true;
					((ConfigDataModel)getDataModel()).saveSetElementsFileLater(this);
				}
			}
			else {
				_systemObjectInfo.setConfigurationData(CONFIGURATION_ELEMENTS_IN_MUTABLE_SET, bytes);
				// ein Datensatz hat sich geändert -> dem Konfigurationsbereich Bescheid sagen
				((ConfigConfigurationArea)getConfigurationArea()).setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			}
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der konfigurierende Datensatz mit den Elementen der Menge " + getNameOrPidOrId() + " konnte nicht geschrieben werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}
	
	/**
	 * Diese Methode liest den konfigurierenden Datensatz für die Elemente dieser Menge ein und gibt sie in einer Liste zurück.
	 * 
	 * @return eine Liste von Elementen mit Zeitstempeln, die die Zugehörigkeitszeiträume repräsentieren
	 */
	private List<MutableElement> getMutableElements() {
		// die eingelesenen Elemente werden nicht alle vorgehalten, da dies auf Dauer zu viele werden können
		final List<MutableElement> mutableElements = new ArrayList<MutableElement>();
		try {
			byte[] bytes;
			loadElementAccessProperties();
			if(_elementsFile == null) {
				// feste ID für die ATG-Verwendung um die Elemente einer Menge zu erhalten
				bytes = _systemObjectInfo.getConfigurationData(CONFIGURATION_ELEMENTS_IN_MUTABLE_SET);
			}
			else {
				if(_elementsDataBytes == null) {
					if(_elementsFile.isFile() && _elementsFile.canRead()) {
						final FileInputStream in = new FileInputStream(_elementsFile);
						final DataInputStream din = new DataInputStream(in);
						try {
							final byte version = din.readByte();
							if(version == 1) {
								final int size = din.readInt();
								final byte[] readBytes = new byte[size];
								din.readFully(readBytes);
								_elementsDataBytes = readBytes;
							}
						}
						finally {
							din.close();
							in.close();
						}
					}
					else {
						if(_elementsFile.exists()) {
							_debug.warning("Datei mit der Elementzugehörigkeit einer dynamischen Menge kann nicht gelesen werden", _elementsFile.getPath());
						}
						_elementsDataBytes = new byte[0];
					}
				}
				bytes = _elementsDataBytes;
			}
			final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);
			assert bytes.length % 26 == 0 : "Format des Byte-Arrays für die Elemente einer Menge " + getNameOrPidOrId()
			        + " hat sich geändert. Länge muss durch 26 teilbar sein.";
			int numberOfElements = bytes.length / 26;
			for(int i = 0; i < numberOfElements; i++) {
				long id = deserializer.readLong();
				long startTime = deserializer.readLong(); // Zeit, ab der das Element zur Menge gehört
				long endTime = deserializer.readLong(); // Zeit, ab der das Element nicht mehr zur Menge gehört
				short simulationVariant = deserializer.readShort(); // Simulationsvariante dieses Objekt, in der es zur Menge hinzugefügt oder aus der Menge entfernt wurde
				final SystemObject object = getDataModel().getObject(id);
				
				if(object != null) {
					mutableElements.add(new MutableElement(object, startTime, endTime, simulationVariant));
				}
				else {
					_debug.warning("Element mit Id '" + id + "' kann nicht der Menge '" + getPidOrNameOrId()
					        + "' hinzugefügt werden, da es kein System-Objekt hierzu gibt.");
				}
			}
			in.close();
			return mutableElements;
		}
		catch(IllegalArgumentException ex) {
			final String errorMessage = "Elemente der dynamischen Menge '" + getNameOrPidOrId()
			        + "' konnten nicht ermittelt werden (evtl. wurde die Menge neu angelegt)";
			_debug.finest(errorMessage, ex.getMessage());
		}
		catch(Exception ex) {
			final String errorMessage = "Elemente der dynamischen Menge " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		return mutableElements;
	}
	
	void saveElementsData() {
		synchronized(_lockElements) {
			if(!_elementsDataBytesChanged) {
				return;
			}
			_elementsDataBytesChanged = false;
			if(_elementsFile.isFile()) {
				final File backupFile = new File(_elementsFile.getParentFile(), _elementsFile.getName() + ".old");
				if(backupFile.exists()) {
					backupFile.delete();
				}
				_elementsFile.renameTo(backupFile);
			}
			final FileOutputStream out;
			try {
				out = new FileOutputStream(_elementsFile);
			}
			catch(FileNotFoundException e) {
				_debug.error("Fehler beim Erzeugen der Datei mit der Elementzugehörigkeit einer dynamischen Menge", _elementsFile);
				return;
			}
			final DataOutputStream dout = new DataOutputStream(out);
			try {
				// Version
				dout.writeByte(1);
				// Size
				dout.writeInt(_elementsDataBytes.length);
				// Bytes
				dout.write(_elementsDataBytes);
			}
			catch(Exception e) {
				_debug.error("Fehler beim Schreiben der Datei mit der Elementzugehörigkeit einer dynamischen Menge", _elementsFile);
			}
			finally {
				try {
					dout.close();
				}
				catch(IOException e) {
					_debug.error("Fehler beim Schließen der Datei mit der Elementzugehörigkeit einer dynamischen Menge", _elementsFile);
				}
				try {
					out.close();
				}
				catch(IOException e) {
					_debug.error("Fehler beim Schließen der Datei mit der Elementzugehörigkeit einer dynamischen Menge", _elementsFile);
				}
			}
		}
	}

	public void addChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.addChangeListener(simulationVariant, changeListener);
	}

	public void removeChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.removeChangeListener(simulationVariant, changeListener);
	}

	public List<SystemObject> getElements(short simulationVariant) {
		if(simulationVariant == 0) {
			return getElements();
		}
		else {
			return getElementsWithSimulationVariant(simulationVariant);
		}
	}

	/**
	 * Wird aufgerufen, wenn das Objekt verändert wird. Soll alle zwischengespeicherten Daten neu anfordern bzw. zurücksetzen. Erbende Klassen müssen diese
	 * Funktion überschreiben, wenn sie Daten cachen.
	 */
	@Override
	void invalidateCache() {
		super.invalidateCache();
		synchronized(_lockElements) {
			_elements = null;
		}
	}

	/**
	 * Repräsentiert ein Element der Menge mit dem Zeitstempel, ab dem das Element zur Menge gehört und dem Zeitstempel, ab dem das Element nicht mehr zur Menge
	 * gehört.
	 */
	private class MutableElement {
		
		/** ein Element der Menge */
		private SystemObject _element;
		
		/** Zeitstempel, seit dem das Element zur Menge gehört */
		private long _startTime;
		
		/** Zeitstempel, seit dem das Element nicht mehr zur Menge gehört */
		private long _endTime;
		
		/** Simulationsvariante, in welcher das Objekt zur Menge hinzugefügt wurde */
		private short _simulationVariant;
		
		/**
		 * Erzeugt ein Objekt für die dynamische Menge.
		 * 
		 * @param element
		 *            das System-Objekt
		 * @param startTime
		 *            Zeitstempel, seit dem das Element zur Menge gehört
		 * @param endTime
		 *            Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 * @param simulationVariant
		 *            Simulationsvariante, in welcher das Objekt zur Menge hinzugefügt wurde
		 */
		public MutableElement(SystemObject element, long startTime, long endTime, short simulationVariant) {
			_element = element;
			_startTime = startTime;
			_endTime = endTime;
			_simulationVariant = simulationVariant;
		}
		
		/**
		 * Gibt das System-Objekt zurück.
		 * 
		 * @return das System-Objekt
		 */
		public SystemObject getElement() {
			return _element;
		}
		
		/**
		 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element zur Menge gehört.
		 * 
		 * @return Zeitstempel, seit dem das Element zur Menge gehört
		 */
		public long getStartTime() {
			return _startTime;
		}
		
		/**
		 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element nicht mehr zur Menge gehört.
		 * 
		 * @return Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 */
		public long getEndTime() {
			return _endTime;
		}
		
		/**
		 * Gibt die Simulationsvariante dieses Elements zurück, in der das Objekt dieser dynamischen Menge hinzugefügt wurde.
		 * 
		 * @return die Simulationsvariante, in welcher das Objekt der Menge hinzugefügt wurde.
		 */
		public short getSimulationVariant() {
			return _simulationVariant;
		}
		
		/**
		 * Setzt den Zeitstempel, der angibt, seit wann das Element nicht mehr zur Menge gehört.
		 * 
		 * @param endTime
		 *            Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 */
		public void setEndTime(long endTime) {
			_endTime = endTime;
		}
		
	}
	
	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		
		_configComHelper.addConfigurationCommunicationChangeListener(listener);
	}
	
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		
		_configComHelper.removeConfigurationCommunicationChangeListener(listener);
	}
	
	void configurationCommunicationChange(boolean configComStatus) {
		_configComHelper.configurationCommunicationChange(this, configComStatus);
	}
	
	public boolean isConfigurationCommunicationActive() {
		throw new UnsupportedOperationException("Nicht implementiert!");
	}
	
}
