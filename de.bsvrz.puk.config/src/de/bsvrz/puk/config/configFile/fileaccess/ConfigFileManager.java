/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.fileaccess;


import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Diese Methode stellt Konfigurationsbereiche zur Verfügung und verwaltet den Zugriff auf diese. Der Zugriff bezieht sich dabei auf die Dateien selber
 * (Anlegen, Daten speichern, usw.), aber auch auf die dynamischen Objekte/Konfigurationsobjekte der einzelnen Bereiche.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 6914 $ / $Date: 2009-11-05 15:41:41 +0100 (Thu, 05 Nov 2009) $ / ($Author: rs $)
 */
public class ConfigFileManager implements ConfigurationFileManager {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/**
	 * Speichert "alle" Objekte, aller Konfigurationsbereiche. Als Schlüssel dient die ID. Es werden die Objekte gespeichert, die sich in der Mischmenge des
	 * jeweiligen Bereichs befinden. Aktuelle und zukünftig aktuelle Objekte stehen direkt als Objekt zur Verfügung (ConfigurationObjectInfo und
	 * DynamicObjectInfo), Objekte die als "ungültig" markiert sind, aber sich trotzdem noch in der Mischmenge befinden, werden nur mit der Dateiposition und einem
	 * ConfigAreaFile-Objekt gespeichert. Dadurch können sie, falls nötig, nachgeladen werden.
	 */
	private final Map<Long, Object> _idMap = new HashMap<Long, Object>();

	/**
	 * Speichert alle aktiven Objekte, aller Konfigurationsbereiche. Als Schlüssel dient die Pid (String). Das Rückgabeobjekt ist das Objekt, das zu der Pid
	 * gehört. Dieser Mechanismus garantiert, dass es zu jeder Pid nur ein aktuelles Objekte gibt, werden mehrer Objekte mit der gleichen Pid eingetragen, so wird
	 * nur das letzte Objekt eingefügt.
	 */
	private final Map<String, SystemObjectInformationInterface> _pidMapActive = new HashMap<String, SystemObjectInformationInterface>();

	/**
	 * Speichert alle noch nicht aktiven Objekte, aller Konfigurationsbereiche. Als Schlüssel dient der HashCode der Pid. Es wird eine Menge zurückgegeben, die
	 * alle Objekte enthält deren Pid mit dem HashCode abgebildet wurden. Die Menge muss dann untersucht werden.
	 */
	private final Map<String, Set<SystemObjectInformationInterface>> _pidMapNew = new HashMap<String, Set<SystemObjectInformationInterface>>();

	/**
	 * Speichert zu dem HashCode einer Pid eine Menge von Objekten. Die Objekte enthalten alle Informationen , um das Objekt (ungültig markiert) aus der Datei zu
	 * laden. <br> Es muss die gesamte Menge durchsucht werden, da der HashCode nicht eindeutig ist, um die Pid genau zu identifizieren. Ausserdem kann es mehrer
	 * Pid´s zu einem Objekt geben.
	 */
	private final Map<Integer, Set<LoadInformations>> _pidMapOld = new HashMap<Integer, Set<LoadInformations>>();

	/**
	 * HashMap, die alle Dateien und somit Konfigurationsbereiche verwaltet. Als Schlüssel dient die Pid des Konfigurationsbereichs. Das zurückgegebene Objekt
	 * ermöglicht den Zugriff auf den Konfigurationsbereich.
	 */
	private final Map<String, ConfigurationAreaFile> _configurationFiles = new LinkedHashMap<String, ConfigurationAreaFile>();

	/**
	 * Speichert zu einer Simulationsvariante alle dynamischen Objekte. Als Schlüssel dient die Simulationsvariante, als Value wird eine Liste aller dynamischen
	 * Objekte der Simulationsvariante gespeichert.
	 * <p/>
	 * Wenn der Konfigurationsbereich geladen wird, wird die Liste mit Objekten bestückt. Wird ein neues dynamisches Objekt erzeugt, wird es ebenfalls in die Map
	 * aufgenommen.
	 */
	private final Map<Short, Set<DynamicObjectInformation>> _simulationObjects = new HashMap<Short, Set<DynamicObjectInformation>>();


	/**
	 * Diese Methode erstellt zu einem neuen Konfigurationsbereich eine Konfigurationsdatei. Der neue Konfigurationsbereich erhält den Zustand inaktiv. Soll er von
	 * der Konfiguration genutzt werden können, so muss er aktiviert werden.
	 *
	 * @param configurationAreaPid die Pid des neuen Konfigurationsbereichs
	 * @param configurationAreaDir das Verzeichnis, in dem die Konfigurationsdatei angelegt werden soll
	 *
	 * @return der neue Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls die Argumente ungültig sind.
	 * @throws IOException              Falls Fehler im Zusammenhang mit der Datei des Konfigurationsbereichs auftreten.
	 */
	public ConfigurationAreaFile createAreaFile(String configurationAreaPid, File configurationAreaDir)
			throws IllegalArgumentException, IOException, NoSuchVersionException {
		// Prüfen, ob es schon einen Konfigurationsbereich mit der angegebenen Pid gibt
		final String fileName = configurationAreaPid + ".config";
		final File areaFile = new File(configurationAreaDir, fileName);
		if(areaFile.exists()) {
			throw new IllegalArgumentException("Zu der Pid " + configurationAreaPid + " gibt es bereits einen Konfigurationsbereich: " + areaFile);
		}
		if(!areaFile.createNewFile()) throw new IOException("Datei " + areaFile.toString() + " konnte nicht angelegt werden.");
		// Datei erstellen
		_debug.info(
				"Konfigurationsdatei " + areaFile.toString() + " wurde für den neuen Konfigurationsbereich mit der Pid " + configurationAreaPid + " angelegt."
		);
		// Ein neuer Bereich wird angelegt, es wird ein initialer Header geschrieben. Der Konstruktor erledigt diese Arbeit
		// für mehr wird das Objekt nicht mehr benötigt. Der Konstruktor erzeugt auch ein lock für die Datei, dieses Lock muss wieder freigegeben werden.
		//
		
		final int serializerVersion = 3;
		final ConfigAreaFile newConfigAreaFile = new ConfigAreaFile(areaFile, configurationAreaPid, (short)0, serializerVersion, this);
		// Die neue Datei speichern und das lock wieder freigeben.
		newConfigAreaFile.close();

		// Konfigurationsbereich der Konfiguration mit der Version 0 (inaktive Version) hinzufügen
		return addAreaFile(configurationAreaPid, configurationAreaDir, (short)0, new LinkedList<VersionInfo>());
	}

	/**
	 * Der aktuellen Konfiguration wird der angegebene Konfigurationsbereich hinzugefügt. Gibt es bereits einen Konfigurationsbereich mit der angegebenen Pid, wird
	 * eine Fehlermeldung erzeugt.
	 *
	 * @param configurationAreaPid die Pid des Konfigurationsbereichs
	 * @param configurationAreaDir das Verzeichnis, in dem die Konfigurationsdatei gespeichert wurde. Diese Verzeichnis wird benutzt um den Bereich zu laden.
	 * @param activeVersion        die aktuelle Version des Konfigurationsbereichs
	 * @param localVersionTimes    Diese Liste speichert zu jeder Version, die jemals aktiviert wurde, den Zeitpunkt an dem die Version aktiviert wurde. Die
	 *                             Zeitpunkte beziehen sich auf den Zeit, an dem sie auf der Konfiguration, die diese Methode aufruft, aktiviert wurden.
	 *
	 * @return der hinzugefügte Konfigurationsbereich
	 *
	 * @throws IllegalArgumentException Falls der Konfigurationsbereich mit der Pid bereits zur Konfiguration hinzugefügt wurde.
	 * @throws IllegalStateException    Datei existiert nicht
	 */
	public ConfigurationAreaFile addAreaFile(String configurationAreaPid, File configurationAreaDir, short activeVersion, List<VersionInfo> localVersionTimes)
			throws IllegalArgumentException, IOException, NoSuchVersionException {
		// Datei laden
		final String fileName = configurationAreaPid + ".config";
		final File areaFile = new File(configurationAreaDir, fileName);
		if(!areaFile.exists()) throw new IllegalStateException("Konfigurationsdatei " + areaFile.toString() + " existiert nicht.");

		// Konfigurationsbereich wird geladen
		final ConfigAreaFile configurationAreaFile;
		// Diese Datei wurde mit createAreaFile erzeugt, also existiert ein Header
		configurationAreaFile = new ConfigAreaFile(areaFile, activeVersion, this, localVersionTimes);

		// überprüfen, ob der Konfigurationsbereich bereits der Konfiguration hinzugefügt wurde
		synchronized(_configurationFiles) {
			// Falls der Eintrag bereits existiert, wird eine Fehlermeldung geworfen, falls nicht, wird er gespeichert.
			if(_configurationFiles.containsKey(configurationAreaPid)) {
				throw new IllegalArgumentException("Konfigurationsbereich ist der Konfiguration bereits bekannt.");
			}
			else {
				_configurationFiles.put(configurationAreaPid, configurationAreaFile);
			}
		}

		// Alle Objekte, die sich in der Mischobjektmenge befinden anfragen
		final Collection allMixedObjects = configurationAreaFile.getMixedObjectSetObjects();

//		System.out.println("größe Mixed: " + allMixedObjects.size());
//		System.out.println("");

		for(Iterator iterator = allMixedObjects.iterator(); iterator.hasNext();) {
			final Object unknownObject = iterator.next();

			// Das Objekt kann ein dynamisches Objekt, ein Konfigurationsobjekt oder ein "OldObject" sein

			if((unknownObject instanceof ConfigurationObjectInfo)) {
				ConfigurationObjectInfo confObject = (ConfigurationObjectInfo)unknownObject;
				putObjectId(confObject);
//				synchronized (_idMap) {
//					// Die Id des Objekts einfügen
//					_idMap.put(confObject.getID(), confObject);
//				}
				// Pid eintragen

				// Ist das Objekt jetzt oder erst in der Zukunft gültig ?
				if(confObject.getFirstValidVersion() <= configurationAreaFile.getActiveVersion()) {
					// Das Objekt ist jetzt gültig
					putActiveObjectPidHashMap(confObject);
				}
				else {
					// Das Objekt ist erst in Zukunft gültig
					assert confObject.getFirstValidVersion() > configurationAreaFile.getActiveVersion() : "Gültig ab " + confObject.getFirstValidVersion()
					                                                                                      + " Derzeit gültige Version: "
					                                                                                      + configurationAreaFile.getActiveVersion();
					putNewObjectPidHashMap(confObject);
				}
			}
			else if(unknownObject instanceof DynamicObjectInfo) {
				DynamicObjectInfo dynObject = (DynamicObjectInfo)unknownObject;

				// In die Id Map eintragen
				putObjectId(dynObject);
//				synchronized (_idMap) {
//					_idMap.put(dynObject.getID(), dynObject);
//				}

				// Pid eintragen
				// dyn Objekte sind sofort bei ihrer Erschaffung gültig und werden sofort (nicht in der Zukunft ungültig)
				putActiveObjectPidHashMap(dynObject);
			}
			else if(unknownObject instanceof ConfigAreaFile.OldObject) {
				// Bei OldObjects wurde nur die Id und die Hash der Pid geladen. Weiterhin stellt die Klasse
				// die Position des Objekts in der Datei zur Verfügung, somit können die restlichen Teile nachgeladen werden.
				ConfigAreaFile.OldObject oldObject = (ConfigAreaFile.OldObject)unknownObject;

				// Mit diesem Objekt kann das als ungültig markierte Objekt aus der Datei in den Hauptspeicher geladen werden
				final LoadInformations loadInformations = new LoadInformations(oldObject.getFilePosition(), oldObject.getConfigAreaFile());

				putObjectId(oldObject.getId(), loadInformations);
//				synchronized (_idMap) {
//					_idMap.put(oldObject.getId(), loadInformations);
//				}
				// Pid eintragen
				putOldObjectPidHashMap(oldObject.getPidHashCode(), loadInformations);
			}
			else {
				// Das Objekt ist völlig unbekannt, das sollte nicht passieren
				_debug.error("Konfigurationsobjekt völlig unbekannt: " + unknownObject.getClass());
			}
		}

//		System.out.println("Größe der Id Map: " + _idMap.size());
//		System.out.println("");

		return configurationAreaFile;
	}

	/**
	 * Legt ein Objekt in der Map ab, die alle Pids, aller aktiven Objekte verwaltet. Es gibt zu einer Pid immer nur ein altives Objekt, wurde bereits ein aktives
	 * Objekt gespeichert, so wird dieses durch das neue Objekt überschrieben.
	 *
	 * @param object Objekt, das in die Map aufgenommen werden soll
	 */
	private void putActiveObjectPidHashMap(SystemObjectInformationInterface object) {
		synchronized(_pidMapActive) {
			_pidMapActive.put(object.getPid(), object);
		}
	}

	/**
	 * Entfernt ein Objekt aus der Map, die alle Pids der aktiven Objekte verwaltet. Diese Methode macht nur bei dynamischen Objekten Sinn, da nur diese zur
	 * Laufzeit des Programms entfernt werden müssen (sie werden ungültig). Bei Konfigurationsobjekten findet dieser wechsel nur beim Start der Konfigurations
	 * statt.
	 *
	 * @param dynamicObjectInfo Objekt, das aus der Map entfernt werden soll
	 */
	private void removeActiveObjectPidHashMap(DynamicObjectInfo dynamicObjectInfo) {
		synchronized(_pidMapActive) {
			_pidMapActive.remove(dynamicObjectInfo.getPid());
		}
	}

	private void putNewObjectPidHashMap(SystemObjectInformationInterface object) {

		synchronized(_pidMapNew) {
			Set<SystemObjectInformationInterface> pids = _pidMapNew.get(object.getPid());
			// Es gibt die Menge noch nicht
			if(pids == null) {
				// Menge anlegen
				pids = new HashSet<SystemObjectInformationInterface>();
				_pidMapNew.put(object.getPid(), pids);
			}
			pids.add(object);
		}
	}

	private void putOldObjectPidHashMap(int pidHashCode, LoadInformations loadInformations) {

		synchronized(_pidMapOld) {
			Set<LoadInformations> pids = _pidMapOld.get(pidHashCode);
			// Es gibt die Menge noch nicht
			if(pids == null) {
				// Menge anlegen
				pids = new HashSet<LoadInformations>();
				_pidMapOld.put(pidHashCode, pids);
			}
			pids.add(loadInformations);
		}
	}

	public ConfigurationAreaFile getAreaFile(String configurationAreaPid) {
		synchronized(_configurationFiles) {
			if(_configurationFiles.containsKey(configurationAreaPid)) {
				return _configurationFiles.get(configurationAreaPid);
			}
			else {
				// Es gibt zu diesem Key kein Objekt
				return null;
			}
		}
	}

	public SystemObjectInformationInterface getObject(long id) {
		Object unknownObject;
		synchronized(_idMap) {
			unknownObject = _idMap.get(id);
		}
		if(unknownObject == null || (unknownObject instanceof LoadInformations)) {
			// Das Objekt wurde nur teilweise geladen oder befindet sich in einer Datei

			
			return getOldObject(id);
		}
		else {
			return (SystemObjectInformationInterface)unknownObject;
		}
	}

	/**
	 * Entfernt ein Objekt aus der Datenstruktur, die den schnellen Zugriff mittels Id auf Objekte zuläßt.
	 *
	 * @param id Id des Objekts, das entfernt werden soll
	 */
	void removeObject(long id) {
		synchronized(_idMap) {
			if(_idMap.remove(id) == null) {
				_debug.info(
						"Es sollte eine Id entfernt werden, die nicht in der entsprechenden Map gespeichert war: " + id + " Größe der Map: " + _idMap.size()
				);
			}
		}
	}

	/**
	 * Entfernt ein dynamisches Objekt mit einer Simulationvariante größer 0 aus allen Datenstrukturen, die zum schnellen Zugriff auf Objekte angelegt wurden.
	 *
	 * @param dynamicObjectInformation Objekt, das entfernt werden soll
	 */
	void removeDynamicSimulationObject(DynamicObjectInformation dynamicObjectInformation) {
		// Aus der Id-Map entfernen
		removeObject(dynamicObjectInformation.getID());
		// Aus der Map mit aktuellen Objekten (nach Pid) enfernen. Das Objekt muss aber gar nicht aktiv sein.
		if(_pidMapActive.containsKey(dynamicObjectInformation.getPid())) {
			removeActiveObjectPidHashMap(dynamicObjectInformation);
		}
		else {
			// Wenn das Objekt nicht aktiv ist, dann muss es alt sein.
			removeObjectPidMapOld(dynamicObjectInformation);
		}
		// Aus den Simulationen entfernen
		removeSimulationObjectFromMap(dynamicObjectInformation);
	}

	/**
	 * Entfernt die Ladeinformtionen eines dynamischen Objekts, aus der Map, die alle alten dynamsichen Objekte der Mischmenge enthält.
	 * <p/>
	 * Sind zu einer Pid keine Ladeinformationen mehr enthalten, wird die Menge aus der Map gelöscht.
	 *
	 * @param dynamicObjectInformation Objekt, das entfernt werden soll
	 */
	private void removeObjectPidMapOld(DynamicObjectInformation dynamicObjectInformation) {
		synchronized(_pidMapOld) {
			// Alle Objekte einer Simulationsvariante
			final Set<LoadInformations> loadInformations = _pidMapOld.get(dynamicObjectInformation.getPidHashCode());
			if(loadInformations != null) {
				// Mit diesem Objekte, kann im Set schnell(O(1)) das richtige Objekt gefunden werden.
				final LoadInformations neededInformation = new LoadInformations(
						dynamicObjectInformation.getLastFilePosition(), dynamicObjectInformation.getConfigAreaFile()
				);
				loadInformations.remove(neededInformation);

				if(loadInformations.size() == 0) {
					_pidMapOld.remove(dynamicObjectInformation.getPidHashCode());
				}
			}
		}
	}

	public SystemObjectInformationInterface getActiveObject(long id) {
		final Object unknownObject;
		synchronized(_idMap) {
			unknownObject = _idMap.get(id);
		}
		if(unknownObject != null) {
			// Es muss geprüft werden, ob sich das Objekt vollständig im Speicher befinden, wenn nicht muss es geladen
			// werden

			if(unknownObject instanceof ConfigurationObjectInfo) {
				return (ConfigurationObjectInfo)unknownObject;
			}
			else if(unknownObject instanceof DynamicObjectInfo) {
				return (DynamicObjectInfo)unknownObject;
			}
			else {
				// Das Objekt muß sich vollständig im Speicher befinden, tut es aber nicht
				throw new IllegalArgumentException("Zu der Id " + id + " gibt es kein Objekt, das derzeit aktiv ist.");
			}
		}
		else {
			// Es gibt zu der Id kein Objekt
			throw new IllegalArgumentException("Zu der Id " + id + " gibt es kein Objekt, das derzeit aktiv ist.");
		}
	}

	public SystemObjectInformationInterface getActiveObject(String pid) {
		synchronized(_pidMapActive) {
			return _pidMapActive.get(pid);
		}
	}

//	public SystemObjectInformationInterface getNewObject(long id) {
//		final Object unknownObject;
//		synchronized(_idMap) {
//			unknownObject = _idMap.get(id);
//		}
//		if(unknownObject != null) {
//
//			if(unknownObject instanceof ConfigurationObjectInfo) {
//				return (ConfigurationObjectInfo)unknownObject;
//			}
//			else if(unknownObject instanceof DynamicObjectInfo) {
//				return (DynamicObjectInfo)unknownObject;
//			}
//			else {
//				// Das Objekt muß sich vollständig im Speicher befinden, tut es aber nicht
//				throw new IllegalArgumentException("Zu der Id " + id + " gibt es kein Objekt, das in der Zukunft aktiv wird.");
//			}
//		}
//		else {
//			// Es gibt zu der Id kein Objekt
//			throw new IllegalArgumentException("Zu der Id " + id + " gibt es kein Objekt, das in der Zukunft aktiv wird.");
//		}
//	}

	public SystemObjectInformationInterface[] getNewObjects(String pid) {
		synchronized(_pidMapNew) {
			List<SystemObjectInformationInterface> result = new LinkedList<SystemObjectInformationInterface>();

			if(_pidMapNew.containsKey(pid)) {
				// Es gibt zu der Pid ein aktives Objekt

				// Alle Objekte, die unter dem Hashcode der Pid zu finden sind
				final Set pidObjects = _pidMapNew.get(pid);
				for(Iterator iterator = pidObjects.iterator(); iterator.hasNext();) {
					SystemObjectInformationInterface systemObjectInfo = (SystemObjectInformationInterface)iterator.next();
					if(pid.equals(systemObjectInfo.getPid())) {
						// Die beiden Pids sind gleich, also zurückgeben
						result.add(systemObjectInfo);
					}
				}
			}
			return result.toArray(new SystemObjectInformationInterface[result.size()]);
		} // synch
	}

	/**
	 * @param id
	 *
	 * @return Objekt oder <code>null</code> falls nicht vorhanden
	 */
	public SystemObjectInformationInterface getOldObject(long id) {
		// Gucken, ob das Objekt im Speicher ist, wenn nicht, alle Bereiche anfragen
		final Object oldObject;
		synchronized(_idMap) {
			oldObject = _idMap.get(id);
		}

		if(oldObject != null) {
			





			// Das Objekt ist zwar ungültig, befindet sich aber noch in der Mischmenge einer Datei.
			// Die Position in der Datei und ein Objekte, das den Zugriff auf die Datei ermöglicht, ist
			// allerdings im Speicher vorhanden. Also kann das Objekt in den Hauptspeicher geladen werden, ohne das
			// der Konfigurationsbereich gesucht werden muss

			if(oldObject instanceof LoadInformations) {
				LoadInformations informations = (LoadInformations)oldObject;
				return loadOldObject(informations);
			}
			else {
				// Fall, der nicht vorkommen kann
				_debug.error("Ein als ungültig markiertes Objekt, hat eine falsche Klasse: " + oldObject.getClass());
				throw new IllegalStateException("Ein als ungültig markiertes Objekt, hat eine falsche Klasse: " + oldObject.getClass());
			}

			// Das Objekt muss von einem bestimmten Typ sein
		}
		else {
			// Die Id muss in allen Konfigurationsbereichen gesucht werden.
			


			final ConfigurationAreaFile[] allConfigurationFiles = getConfigurationAreas();

			for(int nr = 0; nr < allConfigurationFiles.length; nr++) {
				final SystemObjectInformationInterface searchedObject = allConfigurationFiles[nr].getOldObject(id);
				if(searchedObject != null) {
					// Es wurde ein Objekt mit der geforderten Id gefunden
					return searchedObject;
				}
				// In diesem Konfigurationsbereich wurde kein Objekt mit der gewünschten Id gefunden, also wird
				// der nächste Bereich angefragt
			}// for

			// Es wurden alle Bereiche angefragt, kein Bereich hatte ein Objekt mit der Id
			return null;
		}
	}

	/**
	 * Lädt ein als ungültig markiertes Objekt in den Hauptspeicher
	 *
	 * @param oldObject Objekt, das alle Informationen zum laden des Objekts enthält
	 *
	 * @return Ein ungütlig markiertes Objekt
	 */
	private SystemObjectInformationInterface loadOldObject(final LoadInformations oldObject) {
		try {
			return oldObject.getOldObject();
		}
		catch(Exception e) {
			final String errorText = "Fehler beim Laden eines als 'ungültig' markierten Objekts. " + oldObject;
			_debug.error(errorText, e);
			throw new IllegalStateException(errorText + e);
		}
	}

	
	public SystemObjectInformationInterface[] getOldObjects(String pid, long startTime, long endTime) {
		// Alle Bereiche anfragen
		throw new UnsupportedOperationException("Implementieren falls nötig");
//		final ConfigurationAreaFile[] areaFiles = getConfigurationAreas();
//
//		// Jeden Bereich nach Objekten mit der Pid anfragen
//		for(int nr = 0; nr < areaFiles.length; nr++) {
//
//		}
//		return null;
	}

	public ConfigurationAreaFile[] getConfigurationAreas() {
		synchronized(_configurationFiles) {
			Collection<ConfigurationAreaFile> files = _configurationFiles.values();
			return files.toArray(new ConfigurationAreaFile[files.size()]);
		}
	}

	/**
	 * Fügt ein dynamisches Objekt mit einer Simulationsvariante größer 0 in eine Map ein. Ein Objekt mit Simulationsvariante kleiner gleich 0 wird nicht
	 * eingetragen.
	 *
	 * @param dynamicObjectInformation Objekt, das eingetragen werden soll
	 */
	void putSimulationObject(DynamicObjectInformation dynamicObjectInformation) {
		if(dynamicObjectInformation.getSimulationVariant() > 0) {
			synchronized(_simulationObjects) {
				Set<DynamicObjectInformation> dynamicObjectInformations = _simulationObjects.get(dynamicObjectInformation.getSimulationVariant());

				if(dynamicObjectInformations == null) {
					dynamicObjectInformations = new HashSet<DynamicObjectInformation>();
					_simulationObjects.put(dynamicObjectInformation.getSimulationVariant(), dynamicObjectInformations);
				}
				dynamicObjectInformations.add(dynamicObjectInformation);
			}
		}
	}

	/**
	 * Entfernt ein dynamisches Objekt aus der Datenstruktur, die alle dynamischen Objekte über Simulationsvariante verwaltet.
	 * <p/>
	 * Enthält die Menge, der dynamischen Objekte, die zu einer Simulationsvariante gehören, keine Objekte mehr, wird die Menge ebenfalls entfernt.
	 *
	 * @param dynamicObjectInformation Objekt, das entfernt werden soll
	 */
	private void removeSimulationObjectFromMap(DynamicObjectInformation dynamicObjectInformation) {
		synchronized(_simulationObjects) {
			final Set<DynamicObjectInformation> dynamicObjectInformations = _simulationObjects.get(dynamicObjectInformation.getSimulationVariant());
			if(dynamicObjectInformations != null) {
				dynamicObjectInformations.remove(dynamicObjectInformation);
				if(dynamicObjectInformations.size() == 0) {
					// Es gibt keine Objekte mehr, die zu einer Simulationsvariante gehören
					_simulationObjects.remove(dynamicObjectInformation.getSimulationVariant());
				}
			}
		}
	}

	public List<DynamicObjectInfo> getObjects(short simulationVariant) throws IllegalArgumentException {
		// Die Elemente des Sets müssen noch gecastet werden
		final Set<DynamicObjectInformation> dynamicObjectInformations;
		synchronized(_simulationObjects) {
			dynamicObjectInformations = _simulationObjects.get(simulationVariant);
		}

		final List<DynamicObjectInfo> resultlist = new ArrayList<DynamicObjectInfo>();
		if(dynamicObjectInformations != null) {
			for(DynamicObjectInformation dynamicObjectInformation : dynamicObjectInformations) {
				resultlist.add((DynamicObjectInfo)dynamicObjectInformation);
			}
		}
		return resultlist;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein neues dynamisches Objekt oder Konfigurationsobjekt zu einem Konfigurationsbreich hinzugefügt wurde.
	 *
	 * @param newObjectInfo
	 */
	void newObjectCreated(SystemObjectInformationInterface newObjectInfo) {

		putObjectId(newObjectInfo);

		if(newObjectInfo instanceof ConfigurationObjectInfo) {
			// Konfigurationsobjekte sind erst in der Zukunft gültig
			putNewObjectPidHashMap(newObjectInfo);
		}
		else if(newObjectInfo instanceof DynamicObjectInfo) {
			// Dynamische Objekte sind sofort gültig
			putActiveObjectPidHashMap(newObjectInfo);
		}
		else {
			throw new IllegalArgumentException("Unbekanntes Objekt: " + newObjectInfo.getClass());
		}
	}

	/**
	 * Ein dynamisches Objekt wird ungültig (Konfigurationsobjekte müssen nicht beachtet werden, da sie erst beim nächsten Neustart ungültig werden)
	 *
	 * @param oldDynamicObject Objekt, das ungültig wurde
	 */
	void setDynamicObjectInvalid(DynamicObjectInformation oldDynamicObject) {
		final LoadInformations loadInfo;

		if(oldDynamicObject.getPersPersistenceMode() == DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
			// Das Objekt befindet sich nur im Speicher und kann deshalb nicht aus dem Speicher gelöscht werden, da es sonst endgültig
			// verloren gehen würde.
			loadInfo = new LoadInformations(oldDynamicObject);
		}
		else {
			// Das Objekt wurde in einer Datei gespeichert und kann somit aus einer Datei rekonstruiert werden
			loadInfo = new LoadInformations(oldDynamicObject.getLastFilePosition(), oldDynamicObject.getConfigAreaFile());
		}
		 
		// Id muss auch geändert werden, das Value-Objekt muss erneuert werden
		putObjectId(oldDynamicObject.getID(), loadInfo);

		// Das Objekt aus der Map mit den Pids aller aktuellen Objekte entfernen.
		removeActiveObjectPidHashMap(oldDynamicObject);

		// Pid in die Map der "alten" Objekte einfügen
		putOldObjectPidHashMap(oldDynamicObject.getPid().hashCode(), loadInfo);
	}

	/**
	 * Entfernt ein Objekt, das in zukünftig gültig sein wird, aus den Datenstrukturen des Objekts dieser Klasse.
	 *
	 * @param object Objekt, das aus allen Datenstrukturen entfernt werden soll
	 */
	void removeNewObject(ConfigurationObjectInfo object) {
		// Aus der Id-Map entfernen
		removeObject(object.getID());
		// Aus der Map der zukünftig aktuellen Objekte entfernen, die die Pid als Schlüssel benutzen
		synchronized(_pidMapNew) {
			if(_pidMapNew.containsKey(object.getPid())) {
				// Es gibt Objekte zu der Pid
				final Set<SystemObjectInformationInterface> allNewObjects = _pidMapNew.get(object.getPid());
				if(!allNewObjects.remove(object)) {
					_debug.warning(
							"Das zukünftig aktuelle Objekt " + object
							+ " sollte entfernt werden, konnte aber in keiner Map gefunden werden. Größe newPid-Map: " + _pidMapNew.size()
					);
				}
			}
		} // synch _pidMapNew
	}


	private void putObjectId(SystemObjectInformationInterface object) {
		synchronized(_idMap) {
			_idMap.put(object.getID(), object);
		}
	}

	private void putObjectId(Long id, LoadInformations object) {
		synchronized(_idMap) {
			_idMap.put(id, object);
		}
	}

	/**
	 * Methode, die alle Konfigurationsbreiche speichert. Der Aufruf der Methode ist blockierend und kehrt erst dann zurück, wenn alle Konfigurationsbereiche
	 * gespeichert sind.
	 */
	public void saveConfigurationAreaFiles() throws IOException {
		final ConfigurationAreaFile[] files = getConfigurationAreas();

		// Speichert die zuletzt geworfene Exception. Dies können entweder RuntimeEceptions sein oder aber eine IOException, die von flush() geworfen wird.
		Exception lastException = null;

		for(int nr = 0; nr < files.length; nr++) {
			final ConfigurationAreaFile file = files[nr];
			try {
				file.flush();
			}
			catch(Exception e) {
				_debug.error("Fehler beim Speichern eines Konfigurationsbereichs, es wird versucht die restlichen Dateien zu sichern", e);
				lastException = e;
			}
		}

		if(lastException != null) {
			if(lastException instanceof RuntimeException) {
				throw ((RuntimeException)lastException);
			}
			else {
				// Es muss eine IO sein, aber falls nicht, dann wird die "falsche Exception" in eine IO umgewandelt. Es ist aber auf alle Fälle
				// keine RuntimeException.
				throw new IOException(lastException.getMessage());
			}
		}
	}

	public void close() {
		final ConfigurationAreaFile[] files = getConfigurationAreas();

		for(int nr = 0; nr < files.length; nr++) {
			final ConfigurationAreaFile file = files[nr];
			try {
				file.close();
			}
			catch(Exception e) {
				e.printStackTrace();
				_debug.error("Fehler beim Speichern eines Konfigurationsbereichs, es wird versucht die restlichen Dateien zu sichern", e);
			}
			catch(Error error) {
				_debug.error("Fehler beim Herunterfahren der Konfiguration ", error);
				System.out.println("Error: " + error);
			}
		}
	}

	/**
	 * Diese Klasse speichert alle Informationen, die nötig sind um ein als "ungültig" markiertes Objekt nachträglich komplett aus einer Datei in den Hauptspeicher
	 * zu laden.
	 */
	private final static class LoadInformations {

		/** Position in der Datei, an der das Objekt gespeichert ist */
		private final long _filePosition;

		/** Dateiobjekt, mit dem das Objekt geladen werden kann */
		private final ConfigAreaFile _configAreaFile;

		/** Das Objekt ist transient und kann nicht aus der Datei geladen werden, da es nicht gespeichert werden darf. */
		private final DynamicObjectInformation _transientDynamicObject;

		/**
		 * Dieser Konstruktor wird benutzt, wenn das Objekt in einer Konfigurationsbereichsadtei gespeichert wurde und auch aus dieser geladen werden kann.
		 *
		 * @param filePosition   abselute Position an der das Objekt gespeichert wurde.
		 * @param configAreaFile Bereich, in dem das Objekt gespeichert wurde.
		 */
		public LoadInformations(long filePosition, ConfigAreaFile configAreaFile) {
			_filePosition = filePosition;
			_configAreaFile = configAreaFile;
			_transientDynamicObject = null;
		}

		/**
		 * Dieser Konstruktor wird benutzt, wenn ein Objekt auf ungültig gesetzt wurde, aber in keiner Datei gespeichert wurde. Es kann somit nicht rekonstruiert
		 * werden und muss im Speicher gehalten werden. Ein Beispiel hierfür sind transiente Objekte.
		 *
		 * @param transientDynamicObject Objekt, das zwar ungültig ist, aber nicht in einer Konfigurationsdatei gespeichert wurde.
		 */
		public LoadInformations(final DynamicObjectInformation transientDynamicObject) {
			_transientDynamicObject = transientDynamicObject;
			_filePosition = -1;
			_configAreaFile = null;
		}

		public SystemObjectInformationInterface getOldObject() throws NoSuchVersionException, IOException {
			if(_transientDynamicObject != null) {
				return _transientDynamicObject;
			}
			else {
				// Das Objekt aus der Datei laden
				return _configAreaFile.loadObjectFromFile(_filePosition);
			}
		}

		public String toString() {
			if(_transientDynamicObject == null) {
				return "LoadInformations{" + "_filePosition=" + _filePosition + ", _configAreaFile=" + _configAreaFile + "}";
			}
			else {
				return "LoadInformations{" + "TransientesObjekt: " + _transientDynamicObject;
			}
		}
	}
}
