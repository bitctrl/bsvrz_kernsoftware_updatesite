/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.util.BufferedRandomAccessFile;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.filelock.FileLock;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Diese Klasse stellt eine Konfigurationsbereichsdatei dar und speichert alle Objekte des Bereichs mit Historie.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAreaFile implements ConfigurationAreaFile, HeaderInfo {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Wo befindet sich die Datei */
	private final File _configAreaFile;

	/** aktive Version, diese wird durch einen Neustart der Konfiguration gesetzt (durch den Konstruktor) */
	private short _activeVersion;

	/** aktive Version, die aus der Datei gelesen wurde. */
	private short _activeVersionFile = -1;  

	/** Speichert die Version, die als nächstes gültig wird. Die Variable wird mit einer Setter gesetzt. */
	private short _nextActiveVersion = -1;

	/** nächste aktive Version, die aus der Datei eingelesen wurde. */
	private short _nextActiveVersionFile = -1; 

	/** Mit dieser Version werden alle Daten in der Daten serialisiert. */
	private int _serializerVersion = -1;

	/**
	 * Speichert, wo der Header endet. Diese Information wird gebraucht, da alle Angaben zur Position in der Datei relativ zum Ende des Header sind. Addiert man
	 * die beiden Werte, so erhält man die abslute Position in der Datei.
	 * <p>
	 * Die gespeicherte Position zeigt auf den ersten Wert, der nach dem Header gespeichert ist.
	 */
	private long _headerEnd;

	/**
	 * Speichert zu jedem Block, in dem ungültige Objekte gespeichert sind, ein Objekt ab, das die Position des Blocks (relativ zum Headerende) in der Datei
	 * enthält und einen Zeitstempel (wann wurde diese Version gültig), der sich auf den Block bezieht. Als Key dient die Versionsnummer. Sind keine Elemente
	 * vorhanden, so wurde noch kein Block angelegt. Wenn die Datei neu erzeugt wird, ist dies der Fall. Der erste Block entsteht nach der ersten Reorganisation,
	 * nach dem die aktuelle Version erhöht wurde.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 * <p>
	 * Diese Informationen werden für die Reorganisation gebraucht, bei einem Neustart wird aus diesen Informationen die Map
	 * _configurationAuthorityVersionActivationTime rekonstruiert.
	 * <p>
	 * Es sind zwei Maps nötig, da die _oldObjectBlocks-Map speichert wie weit die Reorganisation gekommen ist und die Map
	 * _configurationAuthorityVersionActivationTime wird im Konstruktor gesetzt und wird dann die nächste gültige Vesion enthalten und ist dadurch um mindestens
	 * eine Version größer als jede Version die _oldObjectBlocks speichert.
	 * <p>
	 * Findet kein Versionswechsel statt, sind die Version/Zeitstempel Paare in _oldObjectBlocks und _configurationAuthorityVersionActivationTime identisch.
	 */
	private final Map<Short, OldBlockInformations> _oldObjectBlocks;

	/**
	 * synchronisierte Map, die den Aktivierungszeitpunkt jeder Version speichert. Als Key dient die Version, als value wird der Zeitpunkt zurückgegbeen, an dem
	 * das Konfigurationsverantwortliche die Version aktiviert hat. Siehe auch Kommentar _oldObjectBlocks.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 */
	private final Map<Short, Long> _configurationAuthorityVersionActivationTime;

	/**
	 * Array mit den globalen Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 * Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der nächsten Aktivierung eingetragen. Der größte verwendete Index entspricht
	 * der größten aktivierten Version.
	 */
	private long[] _globalActivationTimes;

	/**
	 * Speichert die nächste ungültige Version. Es ist die größte Version aus _oldObjectBlocks plus 1. Ist noch kein Block vorhanden, so ist die "nächste"
	 * ungültige Version, Version 2 (Version 1 ist die erste mögliche gültige Version)
	 */
	private short _nextInvalidBlockVersion = 2;

	/** relative Position des Blocks, der alle ungültigen dynamischen Objekte enthält, die nach einer Reorganisation in diesem Block geschrieben wurden. */
	private long _startOldDynamicObjects = 0;

	/** relative Position des Index, der die IdŽs verwaltet. */
	private long _startIdIndex = 0;

	/**
	 * relative Position des Index, der die hashCodes der PidŽs verwaltet verwaltet. (-1 bedeutet, dass dieser Wert nicht aus dem Header ausgelesen werden konnte
	 * oder beim erzeugen des Headers nicht bekannt war (Datei erzeugen))
	 */
	private long _startPidHashCodeIndex = 0;

	/**
	 * Gibt an, wo (relativ zum Header) die Mischobjektmenge beginnt. Dies steht entweder im Header, oder bei einener neuen Datei beginnt dieser Bereich genau
	 * hinter dem Header.
	 */
	private long _startMixedSet = 0;

	/** Mit welcher Version wurden die Objekte geschrieben. */
	private short _objectVersion;

	/** Legt die aktuelle Version fest, mit der Objekte codiert werden */
	private final short ACTUAL_OBJECT_VERSION = 1;

	/** Pid des Konfigurationsbereichs */
	private String _configurationAreaPid = null;

	/** letzter Zeitpunkt, an dem ein dynamisches Objekt geändert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _dynamicObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein Konfigurationsobjekt geändert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein konfigurierender Datensatz geändert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationDataChanged = -1;

	/**
	 * In dieser Menge werden alle dynamischen Objekte und Konfigurationsobjekte gespeichert, die modifiziert wurden und die aus diesem Grund noch in der Datei zu
	 * speichern sind. Die Objekte tragen sich selbständig mit {@link #objectModified} in die Liste ein.
	 */
	private final Set<SystemObjectInformationInterface> _modifiedObjects = new HashSet<SystemObjectInformationInterface>();

	/**
	 * Speichert alle (dynamisch und Konf.) aktuellen Objekte, als Schlüssel dient die Id.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, SystemObjectInformationInterface> _actualObjects = new HashMap<Long, SystemObjectInformationInterface>();

	/**
	 * Speichert alle Objekte, die in Zukunft aktuell werden, als Schlüssel dient die Id.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, SystemObjectInformationInterface> _newObjects = new HashMap<Long, SystemObjectInformationInterface>();

	/**
	 * Speichert alle ungültig markierte Objekte, die sich in der Mischmenge befinden. Schlüssel = Id, Value = Objekte mit dem das ungültige Objekt angefordert
	 * werden kann.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, ObjectReference> _oldObjectsId = new HashMap<Long, ObjectReference>();

	/**
	 * Speichert zu einer Pid (Key = HashCode Integer), alle Dateipositionen der alten Objekte, die sich in der Mischmenge befinden. Bei einer Reorganisation
	 * werden die Listen der Map um die Objekte bereinigt, die in einen nGa oder in den dyn nGa gespeichert werden.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf integer gecastet werden (.get((integer) XXX)) !!!
	 */
	private final Map<Integer, Set<FilePointer>> _oldObjectsPid = new HashMap<Integer, Set<FilePointer>>();

	/**
	 * Alle Änderungen an einem dynamischen Objekt oder an einem Konfigurationsobjekt, die den Zustand von "gültig" auf "ungültig" setzen oder ein Objekt
	 * hinzufügen müssen an dieses Objekt gemeldet werden
	 */
	private final ConfigFileManager _fileManager;

	/**
	 * Wird angefordert, wenn die Reorganisation der Datei statt findet. Die Sperre wird ebenfalls angefordert, wenn alte Objekte angefordert werden oder ein
	 * dynamisches Objekt als ungültig deklariert wird, die sich im Speicher befinden. Dieser Moment ist kritisch, da zu diesem Zeitpunkt eine Reorganisation
	 * stattfinden kann und sich damit die Dateipositionen ändern und die Objekte sogar komplett aus dem Speicher gelöscht werden. Neue alte Objekte können aber
	 * ohne Probleme abgelegt werden, da sich die Reorgansation merkt, welche Objekte modifiziert wurden.
	 */
	private final Object _restructureLock = new Object();

	/**
	 * Stellt das Objekt dar, das den Konfigurationsbereich repräsentiert. Das Objekt wird entweder beim laden der Mischmenge über die Pid des
	 * Konfigurationsbereichs gefunden oder es wird ein neuer Bereich angelegt. Dann wird das Objekt erst mit {@link #createConfigurationObject} erzeugt, auch in
	 * diesem Fall wird das Objekt über die Pid identifiziert.
	 */
	private ConfigurationObjectInfo _configAreaObject;

	/**
	 * Synchronisierte Map, die zu jeder Version den lokalen Aktivierungszeitpunkt speichert. Als Key dient die Versionsnummer, als value wird der
	 * Aktivierungszeitpunkt zurückgegeben.
	 * <p>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 */
	private final Map<Short, Long> _localVersionActivationTime = Collections.synchronizedMap(new HashMap<Short, Long>());

	/**
	 * Array mit den lokalen Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 * Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der nächsten Aktivierung eingetragen. Der größte verwendete Index entspricht
	 * der größten aktivierten Version.
	 */
	private long[] _localActivationTimes;

	/**
	 * Speichert die größte Id, die im Konfigurationsbereich vergeben wurde. Der Wert wird beim laden des Konfigurationsbereichs erzeugt, in dem beim laden der
	 * Mischmenge die Id der Objekte geprüft wird, zusätzlich muss der letzte Wert des Index (Id) betrachtet werden. Der Index enthält alle IdŽs aller Objekte, die
	 * sich in den nGa Bereichen bzw. dem dynamischen nGa Bereich befinden.
	 * <p>
	 * Der Wert 0 bedeutet, dass noch kein Konfigurationsobjekt oder dynamisches Objekt im Konfigurationsbereich abgelegt wurde.
	 */
	private long _greatestId = 0;

	/**
	 * Speichert zu allen TypeId's die aktuellen Objekte. Als Schlüssel dient die TypeId, als Value wird eine Liste mit allen Objekten zurückgegeben, die aktuell
	 * sind und deren TypeId mit dem Schlüssel übereinstimmt.
	 */
	private final Map<Long, List<SystemObjectInformationInterface>> _actualObjectsTypeId = new HashMap<Long, List<SystemObjectInformationInterface>>();

	/**
	 * Speichert zu allen TypeId's die alten Objekte. Als Schlüssel dient die TypeId, als Value wird eine Liste mit allen Objekten zurückgegeben. Die Objekte
	 * beinhalten ob das Objekt dynamisch oder konfigurierend ist und die Version bzw. Zeitpunkt an dem das Objekt gültig geworden ist. Als letztes ein Objekte,
	 * mit dem das Objekt wieder rekonstruiert werden kann.
	 */
	private final Map<Long, List<OldObjectTypeIdInfo>> _oldObjectsTypeId = new HashMap<Long, List<OldObjectTypeIdInfo>>();


	/**
	 * Mit diesem Objekt wird ein mehrfacher Zugriff auf diese Datei verhindert. Dieser Mechanismus funktioniert nur, wenn alle Klassen, die auf diese Datei
	 * zugreifen, ihn auch benutzen.
	 */
	private final FileLock _areaFileLock;

	/**
	 * Objekte, die im ersten Durchlauf als löschbar markiert wurden. Weitere Referenzen auf diese Objekte sind nicht erlaubt.
	 * Es ist allerdings möglich (wenn auch Unwahrscheinlich), dass Objekte zwischen ermittlung der nicht mehr
	 * Referenzierbaren Objekte und dem Eintragen in dieses Set plötzlich wieder referenziert wurden.
	 * Daher dürfen diese Objekte nicht endgültig gelöscht werden.
	 * Ist das Objekt auch im zweiten Durchlauf als löschbar markiert, wird es in _objectsPendingDeletion kopiert.
	 */
	private final Set<Long> _objectsLockedForDeletion = new HashSet<Long>();

	/**
	 * Objekte, die zwei Durchläufe hintereinander als löschbar erkannt wurden. Alle Objekte in diesem Set
	 * werden sicher nicht mehr referenziert und können endgültig gelöscht werden.
	 */
	private final Set<Long> _objectsPendingDeletion = new HashSet<Long>();

	/**
	 * Dieser Konstruktor wird benutzt, wenn eine Datei für einen Konfigurationsbereich bereits existiert.
	 *
	 * @param configAreaFile             Datei, in der der Konfigurationsberich gespeichert ist
	 * @param activeVersion              aktive Version mit der der Konfigurationsbereich gestartet wird
	 * @param configFileManager          Objekt, das alle Konfigurationsbereiche verwaltet
	 * @param localActivatedVersionTimes Liste, die zu jeder Version, die lokal aktiviert wurde, den Zeitpunkt enthält, wann die Version aktiviert wurde
	 */
	public ConfigAreaFile(File configAreaFile, short activeVersion, ConfigFileManager configFileManager, List<VersionInfo> localActivatedVersionTimes)
			throws IOException {
		_configAreaFile = configAreaFile;
		_activeVersion = activeVersion;
		_fileManager = configFileManager;

		_areaFileLock = new FileLock(configAreaFile);
		// Zugriff durch andere sperren
		_areaFileLock.lock();

		try {
			_debug.info("Laden der Konfigurationsdatei", configAreaFile);

			for(final VersionInfo versionInfo : localActivatedVersionTimes) {
				if(!_localVersionActivationTime.containsKey(versionInfo.getVersion())) {
					_localVersionActivationTime.put(versionInfo.getVersion(), versionInfo.getActivationTime());
				}
				else {
					throw new IllegalStateException(
							"Die übergebene Liste enthält zu einer Version zwei Zeitpunkte, zu denen die Version gültig wurde. Version: " + versionInfo
									.getVersion()
					);
				}
			}
			_localActivationTimes = getActivationTimeArray(_localVersionActivationTime);

			// Header einlesen
			synchronized(_configAreaFile) {
				ConfigFileHeaderInfo configFileHeaderInfo = new ConfigFileHeaderInfo(_configAreaFile);

				_activeVersionFile = configFileHeaderInfo.getActiveVersionFile();

				if(_activeVersion == (short)-1){
					_activeVersion = _activeVersionFile;
				}

				_oldObjectBlocks = configFileHeaderInfo.getOldObjectBlocks();
				_configurationAuthorityVersionActivationTime = configFileHeaderInfo.getConfigurationAuthorityVersionActivationTime();
				_globalActivationTimes = getActivationTimeArray(_configurationAuthorityVersionActivationTime);
				_nextInvalidBlockVersion = configFileHeaderInfo.getNextInvalidBlockVersion();

				_nextActiveVersionFile = configFileHeaderInfo.getNextActiveVersionFile();
				_startOldDynamicObjects = configFileHeaderInfo.getStartOldDynamicObjects();
				_startIdIndex = configFileHeaderInfo.getStartIdIndex();
				_startPidHashCodeIndex = configFileHeaderInfo.getStartPidHashCodeIndex();
				_startMixedSet = configFileHeaderInfo.getStartMixedSet();
				_configurationAreaPid = configFileHeaderInfo.getConfigurationAreaPid();
				_dynamicObjectChanged = configFileHeaderInfo.getDynamicObjectChanged();
				_configurationObjectChanged = configFileHeaderInfo.getConfigurationObjectChanged();
				_configurationDataChanged = configFileHeaderInfo.getConfigurationDataChanged();
				_objectVersion = configFileHeaderInfo.getObjectVersion();
				_serializerVersion = configFileHeaderInfo.getSerializerVersion();
				_headerEnd = configFileHeaderInfo.getHeaderEnd();
			}
		}
		catch(RuntimeException e) {
			// Dateisperre wieder freigeben
			_areaFileLock.unlock();
			throw e;
		}
		catch(IOException e) {
			// Dateisperre wieder freigeben
			_areaFileLock.unlock();
			throw e;
		}

		// Löschindex lesen und Objekte löschen, sofern vorhanden
		_objectsLockedForDeletion.addAll(readIdIndex("0"));
		_objectsPendingDeletion.addAll(readIdIndex("1"));
	}

	/**
	 * Schreibt eine ID-Index-Datei, die die zum Löschen vorgemerkten IDs enthält.
	 * Der Dateiaufbau ist wie folgt:
	 * Int: Anzahl Einträge
	 * Long[]: Einträge
	 * Long: HashCode der Einträge zur Prüfung der Dateiintegrität
	 * @param indexFileName Suffix oder Name des Index. Aktuell werden zwei Indizes verwendet, da Objekte im ersten Index
	 *                      noch in ausnahmefällen Referenziert werden können
	 * @return Liste mit gelesenen IDs
	 * @throws IOException falls Dateinhalt nicht stimmt oder ein fehler beim Lesen auftrat
	 */
	private Collection<Long> readIdIndex(final String indexFileName) throws IOException {
		File f = getIndexFileName(indexFileName);
		if(!f.exists()){
			// Wenn der Index fehlt ist das OK und kein Problem
			return Collections.emptyList();
		}
		BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(f, "r");
		try {
			long hash = 17;
			int count = bufferedRandomAccessFile.readInt();
			ArrayList<Long> result = new ArrayList<Long>(count);
			for(int i = 0; i < count; i++) {
				long l = bufferedRandomAccessFile.readLong();
				hash = hash * 31 + l;
				result.add(l);
			}
			long expectedHash = bufferedRandomAccessFile.readLong();
			if(expectedHash != hash) {
				_debug.warning("Hashwert der Datei " + f + " stimmt nicht, Dateiinhalt wird verworfen");
				return Collections.emptyList();
			}
			return result;
		}
		finally {
			bufferedRandomAccessFile.close();
		}
	}

	/**
	 * Schreibt einen ID-Index analog zu {@link #readIdIndex(String)}.
	 * @param indexFileName Indexname bzw. Suffix
	 * @param values Zu Schreibende Long-Werte (IDs)
	 * @throws IOException
	 */
	private void writeIdIndex(final String indexFileName, Collection<Long> values) throws IOException {
		File f = getIndexFileName(indexFileName);
		if(values.isEmpty()){
			// Wenn keine Einträge zu schreiben sind, Datei einfach nicht anlegen/löschen
			if(f.exists()){
				if(!f.delete()){
					_debug.error("Index kann nicht gelöscht werden", f);
				}
			}
			return;
		}
		BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(f, "rw");
		try {
			long hash = 17;
			bufferedRandomAccessFile.writeInt(values.size());
			for(Long value : values) {
				hash = hash * 31 + value;
				bufferedRandomAccessFile.writeLong(value);
			}
			bufferedRandomAccessFile.writeLong(hash);
		}
		finally {
			bufferedRandomAccessFile.close();
		}
	}

	private File getIndexFileName(final String indexFileName) {
		return new File(_configAreaFile.getParentFile(), "." + _configAreaFile.getName() + "." + indexFileName + ".index");
	}

	@Override
	public String toString() {
		return String.valueOf(_configAreaFile);
	}

	/**
	 * Erzeugt ein Array mit den Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen.
	 *
	 * @param activationTimeMap Map mit der globalen oder lokalen Zuordnung von aktivierten Versionen zum jeweiligen Zeitpunkt
	 *
	 * @return Array mit den Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 *         Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der nächsten Aktivierung eingetragen. Der größte verwendete Index
	 *         entspricht der größten aktivierten Version.
	 */
	private long[] getActivationTimeArray(final Map<Short, Long> activationTimeMap) {
		// Schleife zur Bestimmung der größten aktivierten Version
		int maximumVersion = 0;
		for(Short version : activationTimeMap.keySet()) {
			if(maximumVersion < version) maximumVersion = version;
		}
		final long[] activationTimes = new long[maximumVersion + 1];
		long time = 0;
		for(int i = maximumVersion; i > 0; --i) {
			final Long timeObject = activationTimeMap.get((short) i);
			if(timeObject != null) time = timeObject;
			activationTimes[i] = time;
		}
		activationTimes[0] = 0;
		return activationTimes;
	}

	/**
	 * Bestimmt den Aktivierungszeitpunkt einer vorgegebenen Version.
	 * @param version Version deren Aktivierungszeitpunkt bestimmt werden soll.
	 * @param activationTimes Array mit den globalen bzw. lokalen Aktivierungszeitpunkten
	 * @return Aktivierungszeitpunkt
	 */
	private long getActivationTime(int version, long[] activationTimes) {
		int maxVersion = activationTimes.length - 1;
		if(version > maxVersion) return Long.MAX_VALUE;
		return activationTimes[version];
	}

	/**
	 * Erzeugt eine Datei, die einen Konfigurationsbereich abbildet. Der Header wird erstellt, usw..
	 *
	 * @param configAreaFile    Datei, in der die Objekte gespeichert werden
	 * @param pid               Pid des Konfigurationsbereichs
	 * @param activeVersion     aktuelle Version
	 * @param serializerVersion Version, mit der alle Daten in der Datei serialisiert werden müssen. Alle Daten, die
	 * @param configFileManager
	 */
	public ConfigAreaFile(File configAreaFile, String pid, short activeVersion, int serializerVersion, ConfigFileManager configFileManager) throws IOException {
		//Falls keine Datei existiert

		_configAreaFile = configAreaFile;
		_activeVersion = activeVersion;
		_serializerVersion = serializerVersion;
		_fileManager = configFileManager;
		_configurationAreaPid = pid;
		_oldObjectBlocks = new HashMap<Short, OldBlockInformations>();
		_configurationAuthorityVersionActivationTime = Collections.synchronizedMap(new HashMap<Short, Long>());
		_globalActivationTimes = getActivationTimeArray(_configurationAuthorityVersionActivationTime);

		_areaFileLock = new FileLock(configAreaFile);
		// Zugriff durch andere sperren
		_areaFileLock.lock();

		try {
			// Es wird keine Zuordnung von aktiver Version und Zeitstempel, da bei diesem Konstruktor Version "0" aktiv ist.

			synchronized(_configAreaFile) {
				final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "rw");

				// finally mit close
				try {
					writeHeader(this, file);
				}
				finally {
					file.close();
				}
			}
		}
		catch(RuntimeException e) {
			// Dateisperre wieder freigeben
			_areaFileLock.unlock();
			throw e;
		}
		catch(IOException e) {
			// Dateisperre wieder freigeben
			_areaFileLock.unlock();
			throw e;
		}
	}

	@Override
	public void setNextActiveVersion(short nextActiveVersion) {
		_nextActiveVersion = nextActiveVersion;
	}

	@Override
	public short getNextActiveVersion() {
		return _nextActiveVersion;
	}

	/**
	 * Methode, die das Objekt, das den Konfigurationsbereich darstellt, zurück gibt.
	 *
	 * @return Objekt oder <code>null</code>, wenn der Bereich gerade erzeugt wurde.
	 */
	@Override
	public ConfigurationObjectInfo getConfigurationAreaInfo() {
		return _configAreaObject;
	}

	@Override
	public void flush() throws IOException {
		// Alle Zugriffe auf die Datei sperren

		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {
				final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "rw");

				// finally für close der Datei
				try {
					while(_modifiedObjects.size() > 0) {

						// Liste sperren, in der alle Objekte stehen, die noch zu speichern sind
						final SystemObjectInformationInterface modifiedObject;
						// Das synch muss an dieser Stelle sein, da ein Objekt, das geändert wird, sich auf sich selbst
						// synchronisiert und dann die Sperre für _modifiedObjects anfordert.

						// Ist das Objekt dann bereits in der _modifiedObjects Liste, blockiert es beim Versuch sich
						// erneut einzutragen. Wird nun hier ebenfalls die gesamte Zeit die Sperre für _modifiedObjects
						// gehalten, dann wird beim Speichern die Sperre für das Objekt angefordert.

						// Folgende Situation würde sich ergeben:
						// Diese Methode hält die Sperre für _modifiedObjects und will die Sperre für das Objekt(zum Speichern).
						// Das Objekt will die Sperre für _modifiedObjects(um sich zum speichern einzutragen) und hält die Sperre für das Objekt.
						// Es gäbe einen Deadlock.
						synchronized(_modifiedObjects) {
							// Es gibt noch ein Objekt
							modifiedObject = _modifiedObjects.iterator().next();
							_modifiedObjects.remove(modifiedObject);
						} // synch auf Menge

						// Was für ein Objekt liegt vor ?
						// Auf das Objekt muss nicht synchronisiert werden, das übernimmt die Methode,
						// die das Objekt schreibt.

						if(modifiedObject instanceof ConfigurationObjectInformation) {
							writeConfigurationObjectToFile((ConfigurationObjectInformation)modifiedObject, file, true, true);
						}
						else if(modifiedObject instanceof DynamicObjectInformation) {
							writeDynamicObjectToFile((DynamicObjectInformation)modifiedObject, file, true, true);
						}
						else {
							_debug.error(
									"Unbekanntes Objekt: Id " + modifiedObject.getID() + " Pid " + modifiedObject.getPid() + " Name " + modifiedObject.getName()
							);
						}
					}
				}
				finally {
					file.close();
				}
			} // synch Restrukturierung
		} // synch auf Datei

		// Indexdateien fürs endgültoge Löschen schreiben
		synchronized(_objectsPendingDeletion) {
			writeIdIndex("0", _objectsLockedForDeletion);
			writeIdIndex("1", _objectsPendingDeletion);
		}
	}

	private volatile long _backupProgress;
	private volatile long _fileLength;

	/**
	 * Sichert die Datei in ein angegebenes Zielverzeichnis
	 * @param targetDirectory Zielverzeichnis
	 * @throws IOException IO-Fehler
	 */
	public void createBackupFile(File targetDirectory) throws IOException {
		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {
				final String fileName = _configAreaFile.getName();

				// Alle Puffer sichern
				flush();

				_backupProgress = 0;
				_fileLength = _configAreaFile.length();
				// Datei kopieren
				final FileOutputStream fileOutputStream = new FileOutputStream(new File(targetDirectory, fileName));
				try {
					final FileInputStream inputStream = new FileInputStream(_configAreaFile);
					try {

						byte[] buf = new byte[1024*16];
						int len;
						while((len = inputStream.read(buf)) > 0) {
							fileOutputStream.write(buf, 0, len);
							_backupProgress += len;
						}
					}
					finally {
						inputStream.close();
					}
				}
				finally {
					fileOutputStream.close();
				}
			}
		}
	}


	public long getBackupProgress() {
		return _backupProgress;
	}

	public long getFileLength(){
		return _fileLength;
	}

	@Override
	public void close() throws IOException {
		try {
			// Alle Puffer sichern
			flush();
		}
		finally {
			// Datei wieder freigeben
			_areaFileLock.unlock();
		}
	}

	/**
	 * Speicher ein dynamisches Objekt in die Datei des Konfigurationsbereichs. Diese Methode wird benötigt, wenn ein dynamisches Objekt auf "Invalid" gesetzt
	 * werden. Für Konfigurationsobjekte ist dies nicht nötig, da diese bis zum beenden der Konfiguration im Speicher bleiben.
	 *
	 * @param object zu speicherndes Objekts
	 */
	void writeDynamicObject(DynamicObjectInformation object) {
		try {
			synchronized(_configAreaFile) {
				final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "rw");
				try {
					writeDynamicObjectToFile(object, file, true, true);
				}
				finally {
					file.close();
				}
			}
		}
		catch(IOException e) {
			_debug.error(
					"Das Objekt mit der Id " + object.getID() + " Pid " + object.getPid() + " konnte nicht in Datei " + _configAreaFile + " gespeichert werden"
			);
		}
	}

	@Override
	public DynamicObjectInfo createDynamicObject(
			long objectID, long typeID, String pid, short simulationVariant, String name, DynamicObjectType.PersistenceMode persistenceMode
	) {

//		if (objectID < _greatestId) {
//			throw new IllegalArgumentException("Die Id des Objekts ist kleiner als die größte Id des Konfigrationsbereichs, Id Konfigurationsbereich " + _greatestId + " Id des Objekts " + objectID + " Konfigurationsbereich " + _configAreaFile);
//		}

		// anlegen des Objekts im Speicher
		// Ob das Objekt auch in der Datei gespeichert werden muss, hängt vom PersistenceMode ab.
		boolean savePersistence;

		if(persistenceMode == DynamicObjectType.PersistenceMode.PERSISTENT_AND_INVALID_ON_RESTART
				|| persistenceMode == DynamicObjectType.PersistenceMode.PERSISTENT_OBJECTS) {
			// Das dynamische Objekt muss persistent in einer Datei gespeichert werden
			savePersistence = true;
		}
		else {
			// Das dynamische Objekt soll nicht persistent in einer Datei gespeichert werden, sondern soll nur
			// als Objekt im Speicher existieren
			savePersistence = false;
		}

		final DynamicObjectInformation newDynamicObjectInformation = new DynamicObjectInformation(
				objectID, pid, typeID, name, simulationVariant, System.currentTimeMillis(), this, savePersistence, persistenceMode
		);
		// Dynamische Objekte sind sofort gültig
		putActualObject(newDynamicObjectInformation);
		putActualObjectTypeId(newDynamicObjectInformation);
		// Wenn es sich um ein Objekt für eine Simulation handelt, muss es in die entsprechende Datenstruktur eingefügt werden
		if(simulationVariant > 0) {
			_fileManager.putSimulationObject(newDynamicObjectInformation);
		}
		else {
			_fileManager.newObjectCreated(newDynamicObjectInformation);
		}

		// Das Objekt konnte erzeugt werden, also gibt es vielleicht eine neue größte Id.
		// (Wenn der Converter benutzt wird sind die Id's nicht unbedingt streng monoton steigend, darum die Abfrage)
		if(getRunningNumber(objectID) > _greatestId) {
			_greatestId = getRunningNumber(objectID);
		}

		return newDynamicObjectInformation;
	}

	@Override
	public ConfigurationObjectInfo createConfigurationObject(long objectID, long typeID, String pid, String name) {
//		if (objectID < _greatestId) {
//			throw new IllegalArgumentException("Die Id des Objekts ist kleiner als die größte Id des Konfigrationsbereichs, Id Konfigurationsbereich " + _greatestId + " Id des Objekts " + objectID + " Konfigurationsbereich " + _configAreaFile);
//		}

		if(_nextActiveVersion > 0) {
			final ConfigurationObjectInformation newConfigurationObjectInformation = new ConfigurationObjectInformation(
					this, objectID, pid, typeID, name, _nextActiveVersion, true
			);
			// Konf. Objekte sind erst in Zukunft mit der nächsten Version gültig
			putNewObject(newConfigurationObjectInformation);
			_fileManager.newObjectCreated(newConfigurationObjectInformation);

			// Der Konfigurationsverantwortliche wurde ist noch unbekannt (bei der Erzeugung der Datei, als Beispiel)
			// und wird mit create erzeugt. Wurde der Konfigurationsverantwortliche schon gesetzt (geschieht beim einlesen der
			// Datei und stellt den Normalfall dar) kann er nicht mit einem "create" überschrieben werden.
			if(_configAreaObject == null && newConfigurationObjectInformation.getPid().equals(_configurationAreaPid)) {
				_configAreaObject = newConfigurationObjectInformation;
			}

			// Das Objekt konnte erzeugt werden, also gibt es vielleicht eine neue größte Id.
			// (Wenn der Converter benutzt wird sind die Id's nicht unbedingt streng monoton steigend, darum die Abfrage)
			if(getRunningNumber(objectID) > _greatestId) {
				_greatestId = getRunningNumber(objectID);
			}
			return newConfigurationObjectInformation;
		}
		else {
			throw new IllegalStateException("Es wurde keine Version festgelegt, mit der das Objekt gültig werden soll");
		}
	}

	/**
	 * Maskiert die ersten 40 Bits einer Id
	 *
	 * @param wholeNumber Id, die maksiert werden soll
	 *
	 * @return Id, mit den ersten 40 Bits
	 */
	private long getRunningNumber(long wholeNumber) {
		return (wholeNumber & 0xFFFFFFFFFFL);
	}

	@Override
	public SystemObjectInformationInterface[] getCurrentObjects() {
		synchronized(_actualObjects) {
			Collection<SystemObjectInformationInterface> helper = _actualObjects.values();
			return helper.toArray(new SystemObjectInformationInterface[helper.size()]);
		}
	}

	@Override
	public SystemObjectInformationInterface[] getActualObjects(long typeId) {
		synchronized(_actualObjectsTypeId) {
			final List<SystemObjectInformationInterface> objectList = _actualObjectsTypeId.get(typeId);
			if(objectList != null) {
				return objectList.toArray(new SystemObjectInformationInterface[objectList.size()]);
			}
			else {
				// Es gibt keine Objekte
				return new SystemObjectInformationInterface[0];
			}
		}
	}

	@Override
	public SystemObjectInformationInterface[] getActualObjects(Collection<Long> typeIds) {
		final Set<Long> typeIdsSet = new HashSet<Long>(typeIds); // Doppelte Typen entfernen
		synchronized(_actualObjectsTypeId) {
			final List<SystemObjectInformationInterface> result = new ArrayList<SystemObjectInformationInterface>();
			for(Long typeId : typeIdsSet) {
				final List<SystemObjectInformationInterface> objectList = _actualObjectsTypeId.get(typeId);
				if(objectList != null){
					result.addAll(objectList);
				}
			}
			return result.toArray(new SystemObjectInformationInterface[result.size()]);
		}
	}

	@Override
	public SystemObjectInformationInterface[] getObjects(
			long startTime, long endTime, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType, Collection<Long> typeIds
	) {
		try {
			// Da für jedes Objekt, das aus der Datei geladen wird, geprüft werden muss ob es in der Collection typeIds
			// vorhanden ist, wird dafür ein HashSet benutzt. Die HashSet ermöglicht den Zugriff in O(1) auf eine Id.
			// Die Collection wird mit der "contains" Methode lineare Zeit O(n) benötigen.

			// Speichert die geforderten TypeIds und ermöglicht den schnellen Zugriff mittels der TypeId
			final Set<Long> typeIdsSet = new HashSet<Long>(typeIds);

			// Speichert die Objekte, die zur Lösung gehören
			final List<SystemObjectInformationInterface> results = new LinkedList<SystemObjectInformationInterface>();

			// Wenn restrukturiert wird darf nicht gesucht werden, da auf Objekte zugegriffen wird, die durch
			// eine Restrukturierung verschoben werden würden
			synchronized(_restructureLock) {
				// Es finden lesende Dateizugriffe statt. Die Objekte dürfen nicht verschoben werden
				synchronized(_configAreaFile) {
// *********************************************************************************************************************
					// Version ermitteln, die zum geforderten Startzeipunkt aktiv war

					// Version, die zum geforderten Startzeitpunkt gültig war
					final short firstOldVersion = getActiveVersion(startTime, kindOfTime);
					// Version, die zum geforderten Endzeitpunkt gültige Version
					final short lastOldVersion = getActiveVersion(endTime, kindOfTime);


// *********************************************************************************************************************
					// die aktuellen prüfen, ob diese vielleicht auch schon gültig waren
					for(final Long typeId : typeIdsSet) {
						// Objekte anfordern, die die geforderte TypeId besitzen
						final List<SystemObjectInformationInterface> objectsForTypeId = _actualObjectsTypeId.get(typeId);

						if(objectsForTypeId != null) {

							// Es gibt Objekte
							for(final SystemObjectInformationInterface systemObjectInfo : objectsForTypeId) {
								// Objekt, das die gewünschte TypeId besitzt
								//Prüfen, ob das Objekt zur Lösung gehört

								if(objectValid(systemObjectInfo, startTime, endTime, kindOfTime, timeSpecificationType)) {
									// Das Objekt war gültig
									results.add(systemObjectInfo);
								}

							}
						}
					}


// *********************************************************************************************************************
					// Die alten Objekt der Mischmenge prüfen

					BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");

					// Map mit alten TypeId Objekten anfragen und alle Objekte nehmen, deren Version bzw. Zeit paßt (gültig ab)
					for(final Long typeId : typeIdsSet) {
						// betrachtete TypeId
						final List<OldObjectTypeIdInfo> oldObjectsForTypeId = _oldObjectsTypeId.get(typeId);

						if(oldObjectsForTypeId != null) {
							for(final OldObjectTypeIdInfo objectTypeIdInfo : oldObjectsForTypeId) {
								// if (wasObjectActive(objectTypeIdInfo, firstOldVersion, lastOldVersion, startTime, endTime)) {
								if(objectValid(objectTypeIdInfo, startTime, endTime, kindOfTime, timeSpecificationType)) {
									// Das Objekt befindet sich nur teilweise im Speicher und muss nun geladen werden. Transiente Objekte befinden sich
									// ganz im Speicher.
									results.add(getSystemObjectInfo(objectTypeIdInfo.getObjectReference(), file));
								}
							}
						}
					}

// *********************************************************************************************************************
					// nGa Bereiche durchlaufen (Version wurde schon ermittelt)
					// Die Startversion wurde ermittelt, jetzt müssen alle nGa Bereiche nach dieser Version geladen werden.
					// Es wird bei jedem Objekt nur die TypeId und Zeit/Version betrachtet. War das Objekt im
					// geforderten Zeitraum gültig, dann wird es geladen.

					// try/finally für close der Datei
					try {
						// Gestartet wird beim dem ersten NGA-Block, in dem ungültige Objekte der ersten gewünschten Version enthalten sind (deshalb +1 )
						// Version, in der ein ngaBlock gesucht wird
						short ngaBlockVersion = (short)(firstOldVersion + 1);

						// An dieser Stelle kann muss eins beachtet werden: in dem nGa-Block, der zu der Version gehört
						// müssen sich nicht unbedingt Elemente befinden. Dies wird mit einer -1 gekennzeichnet.
						// Ist dies der Fall, dann muss der nächste nGa-Block betrachtet werden (wenn die Version paßt).
						// Der gerade beschriebene Fall tritt auf, wenn ein Bereich aktiviert wird, aber kein Objekt
						// in der alten Version ungültig geworden ist.

						// nGa Block, von dem Daten gelesen werden müssen (relative Position zum Header).
						// Dieser Wert wird nur benutzt, wenn auch ein sinnvoller Wert gefunden wurde.
						long relativeBlockOffset = Long.MIN_VALUE;

						// Wird true, wenn ein ngaBlock gefunden wurde, der adressiert werden konnte
						boolean offsetFound = false;

						while(_oldObjectBlocks.containsKey(ngaBlockVersion)) {
							// Prüfen ob dieser nGaBlock benutzt werden kann.
							// Es muss geprüft werden, ob die Objekte des Blocks in der geforderten Zeit gültig waren
							final OldBlockInformations nGaBlockInformations = _oldObjectBlocks.get(ngaBlockVersion);
							if(nGaBlockInformations.getFilePosition() >= 0) {
								relativeBlockOffset = nGaBlockInformations.getFilePosition();
								offsetFound = true;
								break;
							}
							// Den nächsten Bereich betrachten
							ngaBlockVersion++;
						}

						if(offsetFound) {
							// Auf den ersten nGa Block positionieren
							file.seek((relativeBlockOffset + _headerEnd));

							// Es müssen solange Daten gelesen werden, bis der dynamische nGa-Bereich erreicht wird
							final long oldConfigurationObjectsBlocks = (_startOldDynamicObjects + _headerEnd);

							// Solange Daten aus den nGa-Blöcken lesen, bis alle nGa geprüft wurden
							while(file.getFilePointer() < oldConfigurationObjectsBlocks) {

								// speichert die Dateiposition des Objekts. Diese Position wird später
								// am Objekt gespeichert
								final long startObjectFileDescriptor = file.getFilePointer();

								// Länge des Blocks einlesen
								final int sizeOfObject = file.readInt();

								// Id des Objekts einlesen
								final long objectId = file.readLong();

								if(objectId > 0) {
									// Es ist ein Objekt und keine Lücke

									final int pidHashCode = file.readInt();

									final long typeId = file.readLong();

									// 0 = Konfobjekt, 1 = dyn Objekt
									final byte objectType = file.readByte();

									// Es werden nur Konfigurationsobjekte eingelesen, darum ist es automatisch ein short
									final short firstInvalidVersion;
									final short firstValidVersion;

									firstInvalidVersion = file.readShort();
									firstValidVersion = file.readShort();
									// Wenn das Objekt im angegebenen Zeitraum gültig war und der Typ des Objekt paßt, dann wurde ein Element gefunden
									final OldObjectTypeIdInfo oldObject = new OldObjectTypeIdInfo(
											firstValidVersion, firstInvalidVersion, true, FilePointer.fromAbsolutePosition(startObjectFileDescriptor, this)
									);
									if(objectValid(
											oldObject, startTime, endTime, kindOfTime, timeSpecificationType
									) && typeIdsSet.contains(new Long(typeId))) {
										// Das Objekt ist im angegebenen Bereich gültig, also kann es geladen werden
										results.add(
												readObjectFromFile(
														startObjectFileDescriptor,
														sizeOfObject,
														objectId,
														typeId,
														firstInvalidVersion,
														firstValidVersion,
														objectType,
														file
												)
										);
									}
									else {
										// Das Objekt gehört nicht zu den gesuchten TypeId's, also muss das
										// nächste Objekt betrachtet werden

										// Die Länge bezieht sich auf das gesamte Objekt, ohne die Länge selber.
										// Also ist die nächste Länge bei
										// "aktuelle Position + Länge - 8 (Id) - 4 (pidHashCode) - 8 (typeId) - 1 (objectType) - 2(firstInvalid) - 2 (firstValid)

										// Die anderen Werte müssen abgezogen werden, weil sie bereits eingelesen wurden
										file.seek(file.getFilePointer() + sizeOfObject - 8 - 4 - 8 - 1 - 2 - 2);
									}
								}
								else {
									// Dieser Fall darf nicht auftreten, da es in einem nGa Block keine Lücke gibt
									throw new IllegalStateException(
											"Lücke im nGa-Bereich gefunden, Bereich: " + _configurationAreaPid + " Position: " + file.getFilePointer()
													+ " falsche ObjektId: " + objectId
									);
								}
							}// while
						}
// *********************************************************************************************************************
						// Den dyn nGa Bereich linear durchlaufen

						


						// Auf den dyn nGa-Bereich positionieren
						file.seek(_startOldDynamicObjects + _headerEnd);

						// Es müssen bis zum Beginn des Id-Index Daten gelesen werden
						final long oldDynamicObjectsEnd = (_startIdIndex + _headerEnd);

						while(file.getFilePointer() < oldDynamicObjectsEnd) {

							// speichert die Dateiposition des Objekts. Diese Position wird später
							// am Objekt gespeichert
							final long startObjectFileDescriptor = file.getFilePointer();

							// Länge des Blocks einlesen
							final int sizeOfObject = file.readInt();

							// Id des Objekts einlesen
							final long objectId = file.readLong();

							if(objectId > 0) {
								// Es ist ein Objekt und keine Lücke

								final int pidHashCode = file.readInt();

								final long typeId = file.readLong();

								// 0 = Konfobjekt, 1 = dyn Objekt
								final byte objectType = file.readByte();

								// Es werden nur dynamische Objekte eingelesen, darum ist es automatisch ein long
								final long firstInvalidTime;
								final long firstValidTime;

								firstInvalidTime = file.readLong();
								firstValidTime = file.readLong();

								// Objekt war gültig und der Typ des Objekts passt
								final OldObjectTypeIdInfo oldDynamicObject = new OldObjectTypeIdInfo(
										firstValidTime, firstInvalidTime, false, FilePointer.fromAbsolutePosition(startObjectFileDescriptor, this)
								);
								if(objectValid(
										oldDynamicObject, startTime, endTime, kindOfTime, timeSpecificationType
								) && typeIdsSet.contains(new Long(typeId))) {
									// Das Objekt ist im angegebenen Bereich gültig, also kann es geladen werden
									results.add(
											readObjectFromFile(
													startObjectFileDescriptor,
													sizeOfObject,
													objectId,
													typeId,
													firstInvalidTime,
													firstValidTime,
													objectType,
													file
											)
									);
								}
								else {
									// Das Objekt gehört nicht zu den gesuchten TypeId's, also muss das
									// nächste Objekt betrachtet werden

									// Die Länge bezieht sich auf das gesamte Objekt, ohne die Länge selber.
									// Also ist die nächste Länge bei
									// "aktuelle Position + Länge - 8 (Id) - 4 (pidHashCode) - 8 (typeId) - 1 (objectType) - 8(firstInvalid) - 8(firstValid)

									// Die anderen Werte müssen abgezogen werden, weil sie bereits eingelesen wurden
									file.seek(file.getFilePointer() + sizeOfObject - 8 - 4 - 8 - 1 - 8 - 8);
								}
							}
							else {
								file.seek(file.getFilePointer() + sizeOfObject - 8);
							}
						}// while

					}
					finally {
						file.close();
					}
// *********************************************************************************************************************

					// gefundene Objekt zurückgeben
					return results.toArray(new SystemObjectInformationInterface[results.size()]);
				} // synch(file)
			} // synch(restructure)
		}
		catch(IOException e) {
			_debug.error("Fehler bei der Suche nach Objekten, die zu einer TypeId passen", e);
			throw new IllegalStateException(
					"Fehler bei der Suche nach Objekten, die zu einer TypeId passen: " + e + " in Konfigurationsdatei: " + _configAreaFile
			);
		}
		catch(NoSuchVersionException e) {
			_debug.error("Fehler bei der Suche nach Objekten, die zu einer TypeId passen", e);
			throw new IllegalStateException(
					"Fehler bei der Suche nach Objekten, die zu einer TypeId passen: " + e + " in Konfigurationsdatei: " + _configAreaFile
			);
		}
	}

	/**
	 * Diese Methode prüft ob folgende Gleichung wahr ist:
	 * <p>
	 * start <= middle <= end
	 *
	 * @return true = Die oben angegebene Gleichung ist wahr
	 */
	private boolean isValueAtTheMiddle(final long start, final long middle, final long end) {

		if((start <= middle) && (middle <= end)) {
			// Objekt war/ist im angegebenen Zeitraum gültig
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Gibt zu einem Zeitpunkt die Version zurück, die aktiv war.
	 *
	 * @param startTime  Zeitpunkt, zu dem die Version aktiv gewesen sein muss
	 * @param kindOfTime Soll die Aktivierungszeit des Konfigurationsverantwortlichen oder die lokale Aktivierung zur Berechnung der Version benutzt werden
	 *
	 * @return Version, die zu dem angegebenen Zeitpunkt aktiviert war. Der Wert -1 bedeutet, dass es keine aktive Version gibt, die zu dem geforderten Zeitpunkt
	 *         aktiviert war
	 */
	public short getActiveVersion(final long startTime, final ConfigurationAreaTime kindOfTime) {

		// Speichert alle aktiven Versionen (unsortiert)
		final Set<Short> unsortedVersions;

		if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
			unsortedVersions = _configurationAuthorityVersionActivationTime.keySet();
		}
		else {
			unsortedVersions = _localVersionActivationTime.keySet();
		}

		// Enthält alle Versionen in sortierter Reihenfolge
		final Short sortedVersions[];
		sortedVersions = unsortedVersions.toArray(new Short[unsortedVersions.size()]);

		Arrays.sort(sortedVersions);

		for(int nr = 0; nr < sortedVersions.length; nr++) {

			// untere Zeitgrenze
			final long start;
			// obere Zeitgrenze
			final long end;

			// Version, die gerade geprüft wird ob sie zu dem übergebenen Zeitpunkt aktiv war.
			final short consideredVersion = sortedVersions[nr];

			if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
				start = _configurationAuthorityVersionActivationTime.get(consideredVersion);
			}
			else {
				start = _localVersionActivationTime.get(consideredVersion);
			}

			// Prüfen, ob es noch eine nächste Version gibt
			if((nr + 1) < sortedVersions.length) {
				if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
					end = _configurationAuthorityVersionActivationTime.get(sortedVersions[nr + 1]);
				}
				else {
					end = _localVersionActivationTime.get(sortedVersions[nr + 1]);
				}
			}
			else {
				// Es gibt keinen übernächste Version mehr
				end = Long.MAX_VALUE;
			}

			// 1) Wenn sich der übergebene Zeitpunkt zwischen start und end befindet, dann wurde die Version gefunden, die zu diesem Zeitpunkt
			// aktiv war.
			// 2) Ist der übergebene Zeitpunkt kleiner als der start-Wert, wurde die aktive Version gefunden

			if(isValueAtTheMiddle(start, startTime, end) || (startTime <= start)) {
				return consideredVersion;
			}
		} // for

		// Es gibt keine Version, die zum geforderten Zeitpunkt gültig gewesen ist
		return -1;
	}

	@Override
	public SystemObjectInformationInterface[] getNewObjects() {
		synchronized(_newObjects) {
			Collection<SystemObjectInformationInterface> helper = _newObjects.values();
			return helper.toArray(new SystemObjectInformationInterface[helper.size()]);
		}
	}

	@Override
	@Deprecated
	public Iterator<SystemObjectInformationInterface> iterator() {
		return new FileIterator();
	}

	/**
	 * Iteriert über alle Objekte in diesem Bereich.
	 *
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	@Override
	public void forEach(Consumer<? super SystemObjectInformationInterface> consumer) {
		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {
				forEachOldConfigurationObject(consumer);
				forEachOldDynamicObject(consumer);
				forEachMixedObject(consumer);
			}
		}
	}

	/**
	 * Iteriert über alle Konfigurationsobjekte in den NGA-Blöcken in diesem Bereich.
	 *
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	@Override
	public void forEachOldConfigurationObject(final Consumer<? super ConfigurationObjectInfo> consumer) {
		Consumer<SystemObjectInformationInterface> converter = new Consumer<SystemObjectInformationInterface>() {
			@Override
			public void accept(final SystemObjectInformationInterface obj) {
				if(obj instanceof ConfigurationObjectInfo) {
					ConfigurationObjectInfo objectInfo = (ConfigurationObjectInfo) obj;
					consumer.accept(objectInfo);
				}
				else {
					throw new IllegalStateException("NGA-Block enthält dynamische Objekte");
				}
			}
		};
		forEachObjects(_headerEnd, _startOldDynamicObjects + _headerEnd, converter);
	}

	/**
	 * Iteriert über alle dynamischen Objekte im NGDyn-Block in diesem Bereich.
	 *
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	@Override
	public void forEachOldDynamicObject(final Consumer<? super DynamicObjectInfo> consumer) {
		Consumer<SystemObjectInformationInterface> converter = new Consumer<SystemObjectInformationInterface>() {
			@Override
			public void accept(final SystemObjectInformationInterface obj) {
				if(obj instanceof DynamicObjectInfo) {
					DynamicObjectInfo objectInfo = (DynamicObjectInfo) obj;
					consumer.accept(objectInfo);
				}
				else {
					throw new IllegalStateException("NGDyn-Block enthält Konfigurationsobjekte");
				}
			}
		};
		forEachObjects(_startOldDynamicObjects + _headerEnd, _startIdIndex + _headerEnd, converter);
	}

	/**
	 * Iteriert über alle Objekte in der Mischmenge in diesem Bereich.
	 *
	 * @param consumer Java-8-Style Consumer, an den jedes gefundene Objekt übergeben wird
	 */
	@Override
	public void forEachMixedObject(Consumer<? super SystemObjectInformationInterface> consumer) {
		forEachObjects(_startMixedSet + _headerEnd, _configAreaFile.length(), consumer);
	}

	private void forEachObjects(final long startOffset, final long endOffset, final Consumer<? super SystemObjectInformationInterface> consumer) {
		try {
			synchronized(_restructureLock) {
				synchronized(_configAreaFile) {
					BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");

					// try/finally für close der Datei
					try {
						file.seek(startOffset);
						while(file.getFilePointer() < endOffset) {

							BinaryObject binaryObject = BinaryObject.fromDataInput(file);
							SystemObjectInformationInterface systemObjectInfo = binaryObject.toSystemObjectInfo(this, file.getFilePointer());
							if(systemObjectInfo != null){
								// Falls keine Lücke
								consumer.accept(systemObjectInfo);
							}
						}// while
					}
					finally {
						file.close();
					}
				}
			}
		}
		catch(Exception e){
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Schreibt einen Header in die übergebene Datei. Der FileDescriptor wird auf Position 0 gesetzt und steht am Ende des Schreibvorgangs am Ende des Headers.
	 *
	 * @param file
	 *
	 * @throws IOException
	 */
	private void writeHeader(HeaderInfo positions, BufferedRandomAccessFile file) throws IOException {
		synchronized(file) {
			// Der Header wird an den Beginn der Datei geschrieben
			file.seek(0);

			// Die Headerlänge ist unbekannt, also wird als Platzhalter eine -1 geschrieben
			file.writeInt(-1);

			// Kennung 1 speichern
			file.writeShort(1);
			// Länge der Kennung, 4 Bytes
			file.writeShort(4);
			// aktive Version
			file.writeShort(_activeVersion);
			// größte Version
			file.writeShort(_activeVersion + 1);

			// Kennung 2, alle nGa Blöcke.
			if(_oldObjectBlocks.size() > 0) {
				file.writeShort(2);
				// Platzbedarf speichern
				file.writeShort(_oldObjectBlocks.size() * (8 * 2 + 2));

				// Schlüssel sortieren
				final Short[] oldVersions = _oldObjectBlocks.keySet().toArray(new Short[_oldObjectBlocks.keySet().size()]);
				Arrays.sort(oldVersions);

				for(final Short version : oldVersions) {
					// Die Objekte in aufsteigender Reihenfolge speichern
					final OldBlockInformations blockInformations = _oldObjectBlocks.get(version);

					// relative Dateiposition des Blocks schreiben
					file.writeLong(blockInformations.getFilePosition());
					// Version schreiben
					file.writeShort(version);
					// Zeitstempel schreiben
					file.writeLong(blockInformations.getTimeStamp());
				}
			}
			else {
				// Es gibt noch keine nGa-Blöcke, die Kennung wird trotzdem geschrieben.
				// Also hat auch ein Header, der keine nGa-Blöcke besitzt schon die richtige Größe.
				// Wird die Kennung nicht geschrieben, dann fehlen 4 Bytes (2 Bytes für die Kennung, 2 Bytes für die Länge) und
				// dieser Fall muss extra erkannt werden. So hat der Header ohne nGa sofort die richtige Länge.
				file.writeShort(2);
				// Platzbedarf speichern. Es gibt keine Blöcke
				file.writeShort(0);
			}
			// Kennung 3, nGa dynamische Objekte
			file.writeShort(3);
			file.writeShort(8);
			// relative Position des dyn nGa Bereichs
			file.writeLong(positions.getStartOldDynamicObjects());

			// Kennung 4, Index Id
			file.writeShort(4);
			file.writeShort(8);

			file.writeLong(positions.getStartIdIndex());

			// Kennung 5, Index Pid
			file.writeShort(5);
			file.writeShort(8);

			file.writeLong(positions.getStartPidHashCodeIndex());

			// Kennung 6, Index Pid
			file.writeShort(6);
			file.writeShort(8);

			file.writeLong(positions.getStartMixedSet());

			// Kennung 7, Index Pid
			file.writeShort(7);
			// Die Pid des Konfigurationsbereichs ist Variable, also muss die Länge berechnet werden
			byte pidAsBytes[] = _configurationAreaPid.getBytes("ISO-8859-1");
			file.writeShort(1 + pidAsBytes.length);

			// Anzahl Bytes
			file.writeByte(pidAsBytes.length);
			// String als Bytes
			file.write(pidAsBytes);

			// Kennung 8
			file.writeShort(8);
			file.writeShort(8 + 8 + 8);

			file.writeLong(_dynamicObjectChanged);
			file.writeLong(_configurationObjectChanged);
			file.writeLong(_configurationDataChanged);

			// Kennung 9
			file.writeShort(9);
			file.writeShort(2);

			file.writeShort(ACTUAL_OBJECT_VERSION);

			// Kennung 10
			file.writeShort(10);
			file.writeShort(4);

			file.writeInt(_serializerVersion);

			// Länge des Headers schreiben
			final long headerEnd = file.getFilePointer();
			file.seek(0);
			// Der Intergerwert, der die Länge des Headers darstellt,
			// zählt nicht zu länge des Headers und wird darum abgezogen
			file.writeInt((int)(headerEnd - 4));
			file.seek(headerEnd);
		}
	}

	/**
	 * Lädt alle "aktuellen" und in "Zukunft aktuellen" Objekte. Objekte die ungültig sind, aber sich noch in der Mischobjektmenge befinden, werden nur Teilweise
	 * (ID, Pid-HashCode, Dateiposition) geladen.
	 * <p>
	 * Soll der Bereich in einer anderen als der aktuellen Version geladen werden, dann müssen ebenfalls die nGa-Bereiche betrachtet werden.
	 *
	 * @return Collecetion, die alle Objekte der Mischmenge enthält (entweder ganz, oder nur als ID-Pid-Dateiposition Kombination) und Objekte aus den
	 *         entsprechende nGa-Bereichen (enthalten Sind Objekte der Typen
	 *         {@link de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface}
	 *         und
	 *         {@link de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile.OldObject}
	 *
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	public Collection<Object> getMixedObjectSetObjects() {

		try {
			// Es müssen alle Objekte der Mischobjektmenge zurückgegeben werden.

			final List<Object> mixedObjects = new ArrayList<Object>();

			synchronized(_configAreaFile) {

				// Beschreibt ab welcher Position Daten eingelesen werden.
				// Wenn die aktiver Version, die im Konstruktor übergeben wurde, in der Map _oldObjectBlocks
				// zu finden ist, dann müssen zuerst diese Objekte geladen werden und zwar bis zum dyn nGa Block.
				// Erst danach wird die Mischmenge geladen.

				final long startingPosition;

				if(_oldObjectBlocks.containsKey(_activeVersion)) {
					// Es gibt mindestens einen nGa Block, der Objekte enthält, die geladen werden müssen.
					// Die relative Position des Blocks anfordern.
					// Die relative Position eines Blocks kann eine -1 enthalten. Dies bedeutet, dass es zu diesem Block
					// keine Objekte gibt, der Block kann also "übersprungen" werden.
					// Es werden solange Blöcke übersprungen, bis eine relative Startposition gefunden wurde oder
					// es keine Blöcke mehr gibt, in diesem Fall kann direkt die Mischmenge geladen werden.

					// Als Default auf die Mischmenge, wenn dieser Wert nicht neu gesetzt werden kann, dann wird direkt
					// die Mischmenge geladen
					long relativeStart = _startMixedSet;

					// Bestimmt die nächste Version, die von der aktiven Version aus betrachtet wird und die altive
					// Version besitzt keinen nGa Block
					short versionOffset = 0;

					// Solange es nGa Blocks gibt
					while(_oldObjectBlocks.containsKey((short)(_activeVersion + versionOffset))) {
						final long blockOffset = _oldObjectBlocks.get((short)(_activeVersion + versionOffset)).getFilePosition();

//						System.out.println("Block Offset, relativ: " + blockOffset);

						if(blockOffset >= 0) {
							// Es gibt einen nGa Block für die Version
							relativeStart = blockOffset;
							// Der Start wurde gefunden, also kann die Schleife verlassen werden
//							System.out.println("Es muss ein nGa-Block geladen werden, absolute Position: " + (relativeStart + _headerEnd) + " relative Position: " + relativeStart + " Version des Blocks: " + (_activeVersion + versionOffset));
							break;
						}
						else {
							// Die Version enthält keine Daten, also wird die nächste betrachtet
							versionOffset++;
						}
					}
					startingPosition = relativeStart + _headerEnd;
//					System.out.println("Es muss ein nGa-Block geladen werden, absolute Position: " + startingPosition + " Offset: " + versionOffset + " activeVersion: " + _activeVersion);
				}
				else {
					startingPosition = (_startMixedSet + _headerEnd);
//					System.out.println("Es kann die Mischmenge geladen werden, absolute Position: " + startingPosition);
				}

				// Datei öffnen
				final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");

				try {
					// Als erstes muss der Index-Id geprüft werden, um die größte Id der alten Objekte
					// zu finden.
					if(_startIdIndex > 0) {
						// Es gibt einen Id-Index
						file.seek((_startPidHashCodeIndex + _headerEnd) - 8 - 8);
						// größte Id der alten Objekte einlesen
						_greatestId = getRunningNumber(file.readLong());
					}

					// finally für close der Datei

					// Datei auf den Anfang der Mischmenge postieren
					file.seek(startingPosition);

					// Wie gross ist die Datei
					final long fileSize = file.length();

					// Wird true, wenn das Objekt, das den Konfigurationsbereich wiederspiegelt, gefunden wurde
					boolean configAreaObjectFound = false;
					while(file.getFilePointer() < fileSize) {

						// Wenn alle nGa Blöcke geladen wurden, dann steht der FileDescriptor auf dem Anfang des
						// dyn nGa Blocks, dann müssen die Objekte aus der Mischmenge geladen werden.
						// Dazu wird der dyn nGa Block der Id Index und der Pid-HashCode Index übersprungen.
						if(file.getFilePointer() == (_startOldDynamicObjects + _headerEnd)) {
//							System.out.println("Sprung zur Mischmenge, aktuelle Position, absolut: " + file.getFilePointer() + " neue Position: " + (_startMixedSet + _headerEnd));
							// Es wurde bis zum dyn nGa gelesen
							file.seek(_startMixedSet + _headerEnd);
						}

						// speichert die Dateiposition des Objekts. Diese Position wird später
						// am Objekt gespeichert
						final long startObjectFileDescriptor = file.getFilePointer();

						// Länge des Blocks einlesen
						final int sizeOfObject = file.readInt();

						// Id des Objekts einlesen
						final long objectId = file.readLong();

						if(objectId > 0) {
							// Es ist ein Objekt und keine Lücke

							// Ist die Id des gelesenen Objekts größer als die bisher gespeicherte Id
							if(getRunningNumber(objectId) > _greatestId) {
								_greatestId = getRunningNumber(objectId);
							}

							final int pidHashCode = file.readInt();

							final long typeId = file.readLong();

							// 0 = Konfobjekt, 1 = dyn Objekt
							final byte objectType = file.readByte();

							// Das kann entweder ein Zeitpunkt oder eine Version sein
							final long firstInvalid;
							final long firstValid;

							if(objectType == 0) {
								// Konfigurationsobjekt, es wird die Version eingelesen
								firstInvalid = file.readShort();
								firstValid = file.readShort();

//								System.out.println("Id: " + objectId + " aktuelle Version: " + _activeVersion + " firstInvalid: " + firstInvalid + " firstValid " + firstValid + " absolute Position: " + startObjectFileDescriptor);

								if((firstInvalid <= _activeVersion) && (firstInvalid > 0)) {
									// Die Version, mit der das Objekt ungültig wird, ist kleiner gleich der aktuellen Version.
									// Also ist das Objekt jetzt ungültig, dies gilt nur unter der Vorrausetzung, dass
									// die Version größer 0 ist (0 bedeutet, der Wert wurde noch nicht gesetzt)

									OldObject oldObject = new OldObject(objectId, pidHashCode, FilePointer.fromAbsolutePosition(startObjectFileDescriptor, this), this);
									mixedObjects.add(oldObject);
									putOldObject(oldObject);

									// Objekt in Map für typeId Suche aufnehmen, dies ist ein "ungültig" markiertes Objekts
									putOldObjectTypeId(typeId, firstValid, firstInvalid, true, FilePointer.fromAbsolutePosition(startObjectFileDescriptor, this));

									final long newDestination = startObjectFileDescriptor + 4 + sizeOfObject;
									file.seek(newDestination);
//								System.out.println("alter Datensatz: Id " + objectId);
								}
								else {
									// Das Objekt ist aktuell oder wird in der Zukunft aktuell, also alle Informationen laden.
									// Der pidHashCode muss nicht übergeben werden, da das Objekt als ganzes geladen wird und somit zur Verfügung steht
									final SystemObjectInformationInterface configurationObject = readObjectFromFile(
											startObjectFileDescriptor, sizeOfObject, objectId, typeId, firstInvalid, firstValid, objectType, file
									);

									// Es muss das Objekt, das den Konfigurationsbereich darstellt gefunden werden.
									// Es gibt 2 Fälle:
									// 1) Es gibt ein Objekt, das in der aktuellen Version gültig ist und den Konfigurationsbereich darstellt (Normalfall)
									// 2) Es gibt nur Objekte, die in der Zukunft gültig sind und den Konfigurationsbereich darstellen.

									// In Fall 1) wird das Objekt genommen, dessen Version in der aktuellen Version gültig ist

									// In Fall 2) wurde der Konfigurationsbereich gerade erzeugt und das Objekt, das den Konf.bereich darstellt,
									// ist erst in der nächsten Version gültig (es ist ja ein Konfigurtionsobjekt).
									// In diesem Fall wird das Objekt genommen, das den größten Wert bei "gültig ab" besitzt.

									// Das Objekt, das den Konfigurationsbereich darstellt, wird über die Pid identifiziert
									if((!configAreaObjectFound) && (configurationObject.getPid().equals(_configurationAreaPid))) {
										// Das Objekt muss noch gültig sein (egal wann es gültig wird)
										if((firstInvalid == 0) || (firstInvalid > _activeVersion)) {
											if(_configAreaObject == null) {
												// Das erste Objekt, dass passt wird genommen. Es muss nur gültig sein oder
												// in der Zukunft gültig werden (kein altes Objekt)
												_configAreaObject = (ConfigurationObjectInfo)configurationObject;
											}
											else {
												// Es gab bereits ein Objekt, das den Konfigurationsbereich darstellt.
												// Es wird wie in Fall 1),2) beschrieben vorgegangen

												// Fall 1)
												if(isValueAtTheMiddle(0, firstValid, _activeVersion)) {
													// Das Objekt ist jetzt gültig, also wurde das gesuchte Objekt gefunden
													// und es muss nicht weiter gesucht werden
													_configAreaObject = (ConfigurationObjectInfo)configurationObject;
													configAreaObjectFound = true;
												}
												else {
													// Das Objekt ist erst in Zukunft gültig. Fall 2)
													if(_configAreaObject.getFirstValidVersion() < firstValid) {
														// Das betrachtete Objekt wird später gültig, als das bisher gefundene.
														_configAreaObject = (ConfigurationObjectInfo)configurationObject;
													}
												}
											}
										}
									}

									mixedObjects.add(configurationObject);
									putUnknownObject(configurationObject);

									// Für die TypeId-Suche werden nur aktuelle Objekte benötigt.
									// Objekte die erst in der Zukunft gültig werden, werden nicht betrachtet
									if(firstValid <= _activeVersion) {
										// Das Objekt ist derzeit gültig
										putActualObjectTypeId(configurationObject);
									}
								}
							}
							else {
								// dynamisches Objekt
								firstInvalid = file.readLong();
								firstValid = file.readLong();

								// Das vollständige dynamische Objekt wird in den Speicher geladen.

								


								// Der pidHashCode muss nicht übergeben werden, da das Objekt als ganzes geladen wird und somit zur Verfügung steht
								final SystemObjectInformationInterface dynamicObject = readObjectFromFile(
										startObjectFileDescriptor, sizeOfObject, objectId, typeId, firstInvalid, firstValid, objectType, file
								);

								// Datenstruktur des FileManager mit dem dynamischen Objekt für Simulationen laden
								_fileManager.putSimulationObject((DynamicObjectInformation)dynamicObject);

								if((firstInvalid > 0)) {
									// Der Invalid Wert ist gesetzt, also ist das Objekt ungültig (ein dyn Objekt wird sofort ungütlig, nicht in
									// der Zukunft)
									OldObject oldObject = new OldObject(objectId, pidHashCode, ((DynamicObjectInformation) dynamicObject).getLastFilePosition(), this);
									mixedObjects.add(oldObject);
									putOldObject(oldObject);

									// In die Liste für TypeId's aufnehmen (false, da es sich um ein dynamisches Objekt handelt)
									putOldObjectTypeId(typeId, firstValid, firstInvalid, false, FilePointer.fromAbsolutePosition(startObjectFileDescriptor, this));

									// Den fileDescriptor auf den nächsten Datensatz setzen.
									




								}
								else {
									// Das Objekt ist aktuell. es gibt kein dyn Objekt, das in der Zukunft gültig wird, dyn Objekt sind
									// immer sofort gültig.

									mixedObjects.add(dynamicObject);
									putUnknownObject(dynamicObject);
									putActualObjectTypeId(dynamicObject);
								}
							}
						}
						else {
//							System.out.println("Lücke: " + startObjectFileDescriptor + " negative Id: " + objectId);
							// Eine Lücke, der filePointer muss verschoben werden.
							// Die Länge bezieht sich auf das gesamte Objekt, ohne die Länge selber.
							// Also ist die nächste Länge bei "aktuelle Position + Länge - 8.
							// - 8, weil die Id bereits gelesen wurde und das ist ein Long.
							file.seek(file.getFilePointer() + sizeOfObject - 8);
						}
					}// while
				}
				finally {
					file.close();
				}
				return mixedObjects;
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			_debug.error("Ein-/Ausgabe-Fehler beim Laden aller aktuellen und in Zukunft aktuellen Objekten mit passenden Typ (siehe exception)", e);
			throw new IllegalStateException(e);
		}
		catch(NoSuchVersionException e) {
			e.printStackTrace();
			_debug.error("Versions-Fehler beim Laden aller aktuellen und in Zukunft aktuellen Objekten mit passenden Typ (siehe exception)", e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Legt ein dynamisches Objekt oder ein Konfigurationsobjekt in die entsprechende Datenstruktur für aktuelle oder in Zukunft gültige Objekte ab.
	 *
	 * @param unknownObject Objekt, das eingetragen werden soll
	 */
	private void putUnknownObject(SystemObjectInformationInterface unknownObject) {
		if(unknownObject instanceof ConfigurationObjectInformation) {
			final ConfigurationObjectInformation object = (ConfigurationObjectInformation)unknownObject;

			if(object.getFirstValidVersion() <= _activeVersion) {
				// Das Objekt ist jetzt aktuell
				putActualObject(object);
			}
			else {
				// Das Objekt ist erst in Zukunft gültig
				putNewObject(object);
			}
		}
		else if(unknownObject instanceof DynamicObjectInformation) {
			final DynamicObjectInformation object = (DynamicObjectInformation)unknownObject;
			if(object.getFirstInvalidTime() == 0) {
				// Wenn der Wert gesetzt wäre, dann wäre das Objekt bereits ungültig
				putActualObject(object);
			}
			// Den Fall, dass ein dynamisches Objekt in der Zukunft gültig wird, gibt es nicht
		}
		else {
			_debug.warning("Unbekanntes Objekt: " + unknownObject.getClass());
		}
	}

	/**
	 * Fügt ein als "ungültig" markiertes Objekt in alle interenen Datenstrukturen hinzu, die für einen schnellen Zugriff auf ungültige Objekte benötigt werden.
	 * <p>
	 * Alle Objekte, die übergeben werden, sind auch in der Datei enthalten, transiente Objekte können mit dieser Methode nicht abgebildet werden.
	 *
	 * @param oldObject Objekt, das Id, PidHashCode und die Dateipostion enthält. Das configAreaFileObjekt wird nicht benötigt.
	 */
	private void putOldObject(OldObject oldObject) {
		synchronized(_oldObjectsId) {
			final ObjectReference objectReference = oldObject.getFilePosition();
			_oldObjectsId.put(oldObject.getId(), objectReference);
		}

		synchronized(_oldObjectsPid) {
			Set<FilePointer> filePositions = _oldObjectsPid.get(oldObject.getPidHashCode());

			if(filePositions != null) {
				// Es gibt eine Liste, also Dateipostion einfügen
				filePositions.add(oldObject.getFilePosition());
			}
			else {
				// Es gibt noch keine Liste
				filePositions = new HashSet<FilePointer>();
				filePositions.add(oldObject.getFilePosition());

				// Die neue Liste in die Map einfügen
				_oldObjectsPid.put(oldObject.getPidHashCode(), filePositions);
			}
		}
	}

	/**
	 * Fügt das Objekt zu allen Datenstrukturen hinzu, die für den schnellen Zugriff auf "in Zukunft gültige" Objekte benötigt werden
	 *
	 * @param newObjekt Objekt, das in die Datenstruktur aufgenommen werden soll
	 */
	private void putNewObject(SystemObjectInformationInterface newObjekt) {
		synchronized(_newObjects) {
			_newObjects.put(newObjekt.getID(), newObjekt);
		}
	}

	/**
	 * Diese Methode entfernt ein Objekt, das in zukünftigen Versionen gültig werden soll aus allen Datenstrukturen (auch dem ConfigFileManager), die den schnellen
	 * Zugriff auf diese Art von Objekten ermöglichen.
	 *
	 * @param newObject Objekt, das aus allen Datenstrukturen entfernt werden soll.
	 */
	void removeNewObject(ConfigurationObjectInfo newObject) {
		synchronized(_newObjects) {
			_newObjects.remove(newObject.getID());
		}
		_fileManager.removeNewObject(newObject);
	}

	/**
	 * Entfernt ein dynamisches Objekt aus alle Datenstrukturen(auch ConfigFileManager). Es wird nicht aus der Datei gelöscht.
	 * Dient zum Löschen der dynamischen Objekte beim Beenden einer Simulation und zum permanenten Löschen von nicht mehr
	 * benötigten, historischen Objekten.
	 *
	 * @param dynamicObjectInfo Objekt, das entfernt werden soll
	 */
	void deleteDynamicObject(DynamicObjectInformation dynamicObjectInfo) {

		// Dynamische Objekte können aktuell oder alt sein. Aus diesen Datenstrukturen werden die Elemente entfernt.
		if(_actualObjects.containsKey(dynamicObjectInfo.getID())) {
			synchronized(_actualObjects) {
				_actualObjects.remove(dynamicObjectInfo.getID());
			}
		}
		else {
			synchronized(_oldObjectsId) {
				_oldObjectsId.remove(dynamicObjectInfo.getID());
			}
		}

		synchronized(_oldObjectsPid) {
			final Set<FilePointer> filePointers = _oldObjectsPid.get(dynamicObjectInfo.getPidHashCode());
			if(filePointers != null) {
				filePointers.remove(dynamicObjectInfo.getLastFilePosition());
			}
		}

		// TypeId Maps aktualisieren
		synchronized(_actualObjectsTypeId) {
			final List<SystemObjectInformationInterface> actualTypeList = _actualObjectsTypeId.get(dynamicObjectInfo.getTypeId());
			if((actualTypeList == null) || (!actualTypeList.remove(dynamicObjectInfo))) {
				// Das entfernen hat nicht geklappt
				_debug.error("Das Objekt " + dynamicObjectInfo + " konnte nicht aus der TypeId Map entfernt werden");
			}
		}

		// Beim Filemanager aufräumen, falls es sich um ein gültiges Simulationsabjekt handelt
		_fileManager.removeDynamicSimulationObject(dynamicObjectInfo);
	}

	/**
	 * Fügt das Objekt zu allen Datenstrukturen hinzu, die für den schnellen Zugriff(mit Id) auf aktuelle Objekte benötigt werden
	 *
	 * @param actualObjekt
	 */
	private void putActualObject(SystemObjectInformationInterface actualObjekt) {
		synchronized(_actualObjects) {
			_actualObjects.put(actualObjekt.getID(), actualObjekt);
		}
	}

	@Override
	public SystemObjectInformationInterface getOldObject(long id) {
		final List<SystemObjectInformationInterface> result;
		try {
			result = binarySearch(id, true);
			if(result != null) {
				return result.get(0);
			}
			else {
				return null;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			_debug.error("Objekt mit Id " + id + " konnte aufgrund eines Fehlers nicht gefunden werden", e);
			throw new IllegalStateException("Objekt mit Id " + id + " konnte aufgrund eines Fehlers nicht gefunden werden: " + e);
		}
	}

	/**
	 * Speichert alle benötigten Ladeinforamtionen zu einem Objekt in der Map, die alle alten Objekte verwaltet.
	 *
	 * @param typeId               TypeId des Objekt, das nicht vollständig geladen wurde
	 * @param firstValid           Zeitpunkt oder Version mit der das Objekt gültig wurde
	 * @param firstInvalid         Zeitpunkt oder Version mit der das Objekt ungültig wurde
	 * @param configurationObject  true = Es handelt sich um ein Konfigurationsobjekt (firstValid wird als Version interpretiert); false = Es handelt sich um ein
	 *                             dynamisches Objekt (firstValid wird als Zeitpunkt interpertiert)
	 * @param objectReference Objekt zum anfordert des Objekts (Datei oder Speicher)
	 */
	private void putOldObjectTypeId(
			final long typeId,
			final long firstValid,
			final long firstInvalid,
			final boolean configurationObject,
			final ObjectReference objectReference
	) {
		synchronized(_oldObjectsTypeId) {
//			System.out.println("Füge Ladeinformationen hinzu: typeId " + typeId + " confObjekt: " + configurationObject + " absoluteDateipoistion: " + absoluteFilePosition);
			final List<OldObjectTypeIdInfo> typeIdInfoList;

			if(_oldObjectsTypeId.containsKey(typeId)) {
				typeIdInfoList = _oldObjectsTypeId.get(typeId);
			}
			else {
				// Es gibt noch keine Liste
				typeIdInfoList = new ArrayList<OldObjectTypeIdInfo>();
				_oldObjectsTypeId.put(typeId, typeIdInfoList);
			}

			// Neues Objekt zum laden anlegen und speichern
			typeIdInfoList.add(new OldObjectTypeIdInfo(firstValid, firstInvalid, configurationObject, objectReference));
		}
	}

	/**
	 * Speichert ein Objekt in der Map, die alle aktuellen Objekte nach deren TypeId verwaltet
	 *
	 * @param actualObject Objekt, das in die Map aufgenommen werden soll
	 */
	private void putActualObjectTypeId(SystemObjectInformationInterface actualObject) {
		synchronized(_actualObjectsTypeId) {
			final List<SystemObjectInformationInterface> objects;

			if(_actualObjectsTypeId.containsKey(actualObject.getTypeId())) {
				objects = _actualObjectsTypeId.get(actualObject.getTypeId());
			}
			else {
				// Es wurde noch kein Objekt gespeichert, also Liste anlegen und in der Map speichern
				objects = new ArrayList<SystemObjectInformationInterface>();
				_actualObjectsTypeId.put(actualObject.getTypeId(), objects);
			}

			// Objekt in der Liste speichern
			objects.add(actualObject);
		}
	}

	@Override
	public List<SystemObjectInformationInterface> getObjects(String pid, long startTime, long endTime, ConfigurationAreaTime kindOfTime, final short simulationVariant) {

		try {
			// Speichert alle Objekte, für die die Parameter übereinstimmen
			final List<SystemObjectInformationInterface> results = new ArrayList<SystemObjectInformationInterface>();

			// Können aktuelle Objekte betroffen sein ? Wenn nicht, wird null zurückgegeben.
			// Achtung, das zurückgegebene Objekt kann in einem beliebigen Bereich sein, es muss
			// nicht unbedingt dieser Bereich sein !! Wenn es nicht in diesem Bereich liegt, kann es ignoriert werden.
			final SystemObjectInformationInterface actualSystemObjectInfo = _fileManager.getActiveObject(pid);

			if(actualSystemObjectInfo != null) {
				// befindet sich das Objekt in diesem Bereich ?
				if(_actualObjects.containsKey(actualSystemObjectInfo.getID())) {
					// Das aktuelle Objekt ist Teil von diesem Bereich, also prüfen ob es im gefordeten Zeitbereich gültig ist.
					if(objectValid(actualSystemObjectInfo, startTime, endTime, kindOfTime, TimeSpecificationType.VALID_IN_PERIOD)) {
						// Das Objekt ist im Zeibereich gültig
						results.add(actualSystemObjectInfo);
					}
				}
			}

			// Wenn Simulationen berücksichtigt werden sollen, diese durchsuchen

			final SystemObjectInformationInterface simulationObjectInfo = _fileManager.getSimulationObject(pid, simulationVariant);

			if(simulationObjectInfo != null) {
				// befindet sich das Objekt in diesem Bereich ?
				if(_actualObjects.containsKey(simulationObjectInfo.getID())) {
					// Das aktuelle Objekt ist Teil von diesem Bereich, also prüfen ob es im gefordeten Zeitbereich gültig ist.
					if(objectValid(simulationObjectInfo, startTime, endTime, kindOfTime, TimeSpecificationType.VALID_IN_PERIOD)) {
						// Das Objekt ist im Zeibereich gültig
						results.add(simulationObjectInfo);
					}
				}
			}

			// als ungültig markierte Objekte prüfen
			final List<SystemObjectInformationInterface> binarySearchResult;
			binarySearchResult = binarySearch(pid.hashCode(), false);

			if(binarySearchResult != null) {
				for(final SystemObjectInformationInterface systemObjectInfo : binarySearchResult) {
					// Pid muss noch einmal überprüft werden da bisher nur HashCode abgefragt wurde
					if(pid.equals(systemObjectInfo.getPid())) {
						// Paßt die Zeit
						if(objectValid(systemObjectInfo, startTime, endTime, kindOfTime, TimeSpecificationType.VALID_IN_PERIOD)) {
							results.add(systemObjectInfo);
						}
					}
				}
			}
			return results;
		}
		catch(Exception e) {
			e.printStackTrace();
			_debug.error("Objekt mit Pid " + pid + " konnte aufgrund eines Fehlers nicht gefunden werden", e);
			throw new IllegalStateException("Objekt mit Pid " + pid + " konnte aufgrund eines Fehlers nicht gefunden werden: " + e);
		}
	}

	/**
	 * Diese Methode prüft, ob ein Objekte im angegebenen Zeitraum gültig war/ist.
	 *
	 * @param objectInfo            Objekt, das geprüft werden soll
	 * @param queryIntervalStart             Startzeitpunkt ab dem ein Objekte gültig sein muss
	 * @param queryIntervalEnd               Endzeitpunkt, bis zu dem ein Objekt gültig geworden sein muss
	 * @param kindOfTime            Zeit des Konfigurationsverantwortlichen oder die lokale Zeit zu der eine Version gültig geworden ist
	 * @param timeSpecificationType Gibt an, ab wann ein Objekt als "gültig" deklariert werden darf. Es gibt 2 Fälle: Fall 1) Das Objekt muss im angegebnen Bereich
	 *                              irgendwann gültig gewesen sein (Darf aber auch in dem Bereich ungültig geworden sein). Fall 2) Das Objekt muss im gesamten
	 *                              Zeitbereich gültig gewesen sein, die entspricht TimeSpecificationType.VALID_DURING_PERIOD
	 *
	 * @return true = das Objekt war in dem angegebenen Zeitbereich gültig; false = das Objekt war im angegebenen Zeitbereich nicht gültig
	 */
	boolean objectValid(Object objectInfo, long queryIntervalStart, long queryIntervalEnd, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType) {
		// Die Klasse ist im Package sichtbar, damit ein JUnit-Test die Methode prüfen kann

		long[] activationTimes;
		if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
			activationTimes = _globalActivationTimes;
		}
		else if(kindOfTime == ConfigurationAreaTime.LOCAL_ACTIVATION_TIME) {
			activationTimes = _localActivationTimes;
		}
		else {
			throw new IllegalArgumentException("Unbekannte Zeit/Version Zuweisung " + kindOfTime);
		}

		// Zeitpunkt, an dem das Objekt gültig wurde
		final long objectValidSince;
		// Zeitpunkt, an dem das Objekt ungültig wurde.
		long objectInvalidSince;

		if((objectInfo instanceof ConfigurationObjectInfo)) {
			final ConfigurationObjectInfo confObject = (ConfigurationObjectInfo)objectInfo;
			objectValidSince = getActivationTime(confObject.getFirstValidVersion(), activationTimes);
			objectInvalidSince = getActivationTime(confObject.getFirstInvalidVersion(), activationTimes);
		}
		else if(objectInfo instanceof DynamicObjectInfo) {
			final DynamicObjectInfo dynObject = (DynamicObjectInfo)objectInfo;
			objectValidSince = dynObject.getFirstValidTime();
			objectInvalidSince = dynObject.getFirstInvalidTime();
		}
		else {
			// Das Objekt kann ein Konfigurationsobjekt sein oder ein dynamisches Objekt. In beiden Fällen ist das Objekt nur teilweise im Speicher vorhanden
			final OldObjectTypeIdInfo oldObjectTypeIdInfo = (OldObjectTypeIdInfo)objectInfo;
			if(oldObjectTypeIdInfo.isConfigurationObject()) {
				objectValidSince = getActivationTime((short)oldObjectTypeIdInfo.getFirstValid(), activationTimes);
				objectInvalidSince = getActivationTime((short)oldObjectTypeIdInfo.getFirstInvalid(), activationTimes);

			}
			else {
				// Es ist ein dynamisches Objekt
				objectValidSince = oldObjectTypeIdInfo.getFirstValid();
				objectInvalidSince = oldObjectTypeIdInfo.getFirstInvalid();
			}
		}

		// (objectValidSince == 0) bedeutet, dass dieses Objekt noch immer gültig ist
		if(objectInvalidSince == 0) objectInvalidSince = Long.MAX_VALUE;

		if(TimeSpecificationType.VALID_DURING_PERIOD == timeSpecificationType) {
			// Das Objekt muss im gesamten Zeitraum gültig gewesen sein
			return objectValidSince <= queryIntervalStart && queryIntervalEnd <= objectInvalidSince;
		}
		else {
			return objectValidSince <= queryIntervalEnd && queryIntervalStart <= objectInvalidSince;
		}
	}


	@Override
	public int getSerializerVersion() {
		return _serializerVersion;
	}

	/**
	 * Lädt ein Objekt aus einer Datei
	 *
	 * @param filePosition Position in der Datei, an der das Objekt beginnt
	 *
	 * @return Objekt, das aus der Datei erzeugt wurde
	 *
	 * @throws IOException
	 * @throws de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException
	 */
	SystemObjectInformation loadObjectFromFile(FilePointer filePosition) throws IOException, NoSuchVersionException {
		synchronized(_configAreaFile) {
			final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");
			// try für finally und close
			try {
				file.seek(filePosition.getAbsoluteFilePosition());

				BinaryObject binaryObject = BinaryObject.fromDataInput(file);
				SystemObjectInformationInterface systemObjectInfo = binaryObject.toSystemObjectInfo(this, file.getFilePointer());

				if(systemObjectInfo != null) {
					// Position speichern, an der das nächste Objekt geladen werden kann
					return (SystemObjectInformation) systemObjectInfo;
				}
				return null;
			}
			finally {
				file.close();
			}
		}
	}

	/**
	 * Lädt ein Objekt aus einer Datei und setzt im Objekt fileIterator die Position, an der das nächste Objekt gefunden werden kann (relative Position bezogen auf
	 * den Header).
	 *
	 * @param filePosition Position in der Datei, an der das Objekt beginnt
	 * @param fileIterator Objekt, an dem die Position des nächsten zu ladenden Objekts gespeichert wird. Wird <code>null</code> übergeben, so wird die Psoition
	 *                     des nächsten Objekts nicht gesetzt. Wird eine -1 gesetzt,so gibt es kein nächstes Objekt mehr
	 *
	 * @return Objekt, das aus der Datei erzeugt wurde oder null falls sich dort eine Lücke befindet
	 *
	 * @deprecated Diese Methode öffnet und schießt ein neues {@link BufferedRandomAccessFile}. Auf Performancegründen sollte für das Laden von mehreren Objekten
	 * immer dasselbe BufferedFile-Objekt verwendet werden. Einzige benutzung ist aktuell der {@link de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile.FileIterator},
	 * der ebenfalls nicht mehr benutzt wird und deprecated ist.
	 *
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	@Deprecated
	private SystemObjectInformation loadObjectFromFile(FilePointer filePosition, FileIterator fileIterator) throws IOException, NoSuchVersionException {
		synchronized(_configAreaFile) {

			final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");


			// try für finally und close
			try {
				return loadObjectFromFile(file, filePosition, fileIterator);
			}
			finally {
				file.close();
			}
		}
	}

	@Deprecated
	private SystemObjectInformation loadObjectFromFile(final BufferedRandomAccessFile file, final FilePointer filePosition, final FileIterator fileIterator) throws IOException, NoSuchVersionException {
		file.seek(filePosition.getAbsoluteFilePosition());

		BinaryObject binaryObject = BinaryObject.fromDataInput(file);
		SystemObjectInformationInterface systemObjectInfo = binaryObject.toSystemObjectInfo(this, file.getFilePointer());

		if(systemObjectInfo != null) {
			// Position speichern, an der das nächste Objekt geladen werden kann
			if(fileIterator != null) {
				// Sobald der Id-Index beginnt, wird eine -1 zurückgegeben
				if(file.getFilePointer() < (_startIdIndex + _headerEnd)) {
					// Es können noch Daten gelesen werden
					fileIterator.setRelativePosition((file.getFilePointer() - _headerEnd));
				}
				else {
					// Es gibt keinen nächsten Datensatz mehr
					fileIterator.setRelativePosition(-1);
				}
			}
			return (SystemObjectInformation) systemObjectInfo;
		}
		return null;
	}

	/**
	 * Lädt ein dynamischen Objekt oder ein Konfigurationsobjekt aus der Datei des Konfigurationsbereichs. Die Datei, die übergeben wird, steht mit ihrem
	 * fileDescriptor entweder auf der Simulationsvariante (bei dynamischen Objekten) oder auf den gepackten Daten (bei Konfigurationsobjekten).
	 *
	 * @param filePosition Position in der Datei, an der das Objekt gespeichert ist
	 * @param objectsize   Gesamtegröße des Objekts (einschließlich der schon geladenen Daten)
	 * @param id           Id des Objekts
	 * @param typeId       Id des Types des Objekts
	 * @param firstInvalid Version oder Zeitpunkt, an dem das Objekt ungültig wurde/wird
	 * @param firstValid   Version oder Zeitpunkt, an dem das Objekt gültig wird/wurde
	 * @param objecttype   0 = Konfigurationsobjekt, Valid und Invalid sind als Versionen zu interpretieren; 1 = dynamisches Objekt, Valid und Invalid sind als
	 *                     Zeitpunkte zu interpretieren
	 * @param file         Datei, aus der das Objekt gelesen werden soll, der fileDescriptor steht bei dynamsichen Objekten auf der Simulationsvariante, bei
	 *                     Konfigurationsobjekten auf den gepackten Daten
	 *
	 * @return Dynamisches Objekt oder Konfigurationsobjekt
	 *
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	private SystemObjectInformation readObjectFromFile(
			final long filePosition,
			final int objectsize,
			final long id,
			final long typeId,
			final long firstInvalid,
			final long firstValid,
			final byte objecttype,
			BufferedRandomAccessFile file
	) throws IOException, NoSuchVersionException {

		// Das Objekt wird geladen, es wird dafür gesorgt, dass es sich nicht sofort wieder speichern will
		synchronized(_configAreaFile) {
			if(objecttype == 0) {
				// Konfigurationsobjekt

				// Der vordere Teil ist konstant, also kann die Länge der gepackten Daten berechnet werden.
				// id, pidHash, typeId, type(Konf oder dynamische), Version, Version abziehen
				final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 2 - 2;
				final byte packedBytes[] = new byte[sizeOfPackedData];
				file.readFully(packedBytes);

				return ConfigurationObjectInformation.createSystemObjectInformation(this, filePosition, id, typeId, (short) firstInvalid, (short) firstValid, packedBytes);
			}
			else if(objecttype == 1) {
				// Ein dynamisches Objekt einlesen, die Simulationsvariante wurde noch nicht eingelesen, aber der fileDesc.
				// steht sofort auf dem Wert
				final short simulationVariant = file.readShort();

				// Der vordere Teil ist konstant, also kann die Länge der gepackten Daten berechnet werden.
				// id, pidHash, typeId, type(Konf oder dynamisch), Zeitstempel, Zeitstempel, Simulationsvariante abziehen
				final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 8 - 8 - 2;
				final byte packedBytes[] = new byte[sizeOfPackedData];
				file.readFully(packedBytes);

				return DynamicObjectInformation.getSystemObjectInformation(this, filePosition, id, typeId, firstInvalid, firstValid, simulationVariant, packedBytes);
			}
			else {
				// Unbekannt, das darf nicht passieren.
				throw new IllegalStateException(
						"Ein Objekt konnte weder als dynamisches Objekt noch als Konfigurationsobjekt identifiziert werden, Typ : " + objecttype
				);
			}
		}
	}

	/**
	 * Speichert ein Objekt ans Ende der übergebenen Datei.
	 * <p>
	 * Die Datei wird nach dem Schreibzugriff nicht geschlossen und die neue Dateiposition wird am Objekt vermerkt.
	 *
	 * @param dynamicObject      Objekt, das gesichert werden soll
	 * @param file               Datei, in der das Objekt gespeichert werden soll. Das Objekt wird ans Ende der Datei geschrieben
	 * @param declareGap         true = (Normalfall) Das Objekt wird gespeichert, es existiert aber noch eine ältere Version des Objekts in der Datei und diese
	 *                           wird als Lücke deklariert (ID wird auf 0 gesetzt); false = (Anlegen einer neuen Konfigurationsbereichsdatei oder bei
	 *                           Reorganisation) Das Objekt wird in der Datei gespeichert (es wird keine Lücke erzeugt). Dies ist aber die erste Version des
	 *                           Objekts in der Datei und somit gibt es auch keinen Vorgänger der als Lücke deklariert werden soll
	 * @param setNewFilePosition true = (Normalfall) Das Objekt wird in der Datei gespeichert und die neue Position in der Datei wird am Objekt, das sich im
	 *                           Speicher befindet, gespeichert; false = Das Objekt wird in der Datei gespeichert, aber die neue Dateiposition wird nicht am Objekt
	 *                           gespeichert sondern muss nachträglich am Objekt, das sich im Speicher befindet, gesetzt werden. Ein Beispiel wäre die
	 *                           Reorganisation. Das Objekt wird in der neue Datei gespeichert, aber die Dateiposition wird am Objekt im Speicher erst dann
	 *                           gesetzt, wenn die Reorganisation erfolgreich abgeschlossen werden konnte.
	 *
	 * @return Position in der Datei, an der das Objekt gespeichert wurde
	 *
	 * @throws IOException           Fehler beim schreiben in die Datei, Teile des Datensatzes wurden bereits geschrieben
	 * @throws IllegalStateException Fehler beim sammeln der Daten, die Datei wurde noch nicht verändert
	 */
	private long writeDynamicObjectToFile(DynamicObjectInformation dynamicObject, final BufferedRandomAccessFile file, boolean declareGap, boolean setNewFilePosition)
			throws IOException, IllegalStateException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer;
		final long id;
		final int pidHashCode;
		final long typeId;
		final long firstInvalidTime;
		final long firstValidTime;
		final short simulationVariant;
		final byte[] packedData;

		try {
			serializer = SerializingFactory.createSerializer(_serializerVersion, out);

			synchronized(dynamicObject) {
				// Das Objekt ist derzeit für alle Zugriffe gesperrt, also werden alle Informationen angefordert.
				// So ist sichergestellt, das sie sich nach freigabe des Objekts nicht ändern.
				// Daten die sich nicht mehr ändern werden ebenfalls lokal gespeichert, damit sie nicht beim
				// schreiben angefordert werden müssen und dabei eventuell blockieren und somit die ganze Datei sperren.

				id = dynamicObject.getID();
				pidHashCode = dynamicObject.getPid().hashCode();
				typeId = dynamicObject.getTypeId();
				firstInvalidTime = dynamicObject.getFirstInvalidTime();
				firstValidTime = dynamicObject.getFirstValidTime();
				simulationVariant = dynamicObject.getSimulationVariant();

				// Als nächstes werden die Daten des SystemObjects bearbeitet (alles, bis auf die Id wird eingetragen)
				writeSystemObjectWithoutIdAndTypeId(dynamicObject, serializer);
			} // synch

			// Im Serializer liegen nun alle Daten, die geschrieben werden sollen, diese als Byte-Array anfordern
			// und packen. Danach ist die Länge des Objekts bekannt.
			packedData = zip(out.toByteArray());
		}
		catch(Exception e) {
			_debug.error("Id: " + dynamicObject.getID() + " Serialisiererversion: " + _serializerVersion, e);
			throw new IllegalStateException(
					"Die Daten eines dynamischen Objekts können nicht serialisiert werden, das Objekt wird nicht gespeichert: Id" + dynamicObject.getID()
			);
		}

		// Die Länge kann berechnet werden
		// Id + HashCode + TypeId + (dyn oder konf) + Zeitstempel + Zeitstempel + Simulationsvariante + gepackte Daten
		final int dynamicObjectSize = 8 + 4 + 8 + 1 + 8 + 8 + 2 + packedData.length;

		// Datei sperren
		synchronized(_configAreaFile) {
			// Das dyn. Objekt ans Ende der Datei schreiben
			// Die neue Dateiposition speichern, nach erfolgreichen schreiben wird die Position am Objekt vermerkt
			final long newObjectFilePosition = file.length();

			// filedescriptor positionieren, nötig, weil der Descriptor nicht am Ender der Datei stehen muss, auch wenn
			// vorher ein Objekt geschrieben wurde (das alte Objekt wurde als Lücke markiert, also steht der FD mitten in der Datei)
			file.seek(newObjectFilePosition);

			// Länge der Daten, int
			// Id, long
			// hashCode der Pid, int
			// typeId des Objekts, long
			// Kennzeichnung dynamisches Objekt (1), byte
			// Zeitstempel nicht mehr gültig ab, long
			// Zeitstempel gültig ab, long
			// Simulationsvariante, short

			file.writeInt(dynamicObjectSize);
			file.writeLong(id);
			file.writeInt(pidHashCode);
			file.writeLong(typeId);
			file.writeByte(1);
			file.writeLong(firstInvalidTime);
			file.writeLong(firstValidTime);
			file.writeShort(simulationVariant);

			// Jetzt können die gepackten Daten gespeichert werden.
			// Die Anzahl Bytes, die zu diesem Array gehören, können beim einlesen des Daten aus der Länge
			// des gesamten Objekts berechnet werden, da der ungepackte Teil konstant ist.
			file.write(packedData);

			// Soll eine Lücke erzeugt werden
			FilePointer filePosition = dynamicObject.getLastFilePosition();
			if(declareGap && filePosition != null) {
				// Die Vorgängerversion in der Datei als Lücke deklarieren (ID auf 0)
				declareObjectAsAGap(filePosition, file);
			}
			if(setNewFilePosition) {
				// Das Objekt wurde gespeichert, also muss die neue Dateiposition am Objekt, das sich im Speicher befindet,
				// neu gesetzt werden.
				if(filePosition != null) {
					// Pointer weiterverwenden, damit bestehende Cache-Einträge (z.B. im ConfigFileManager) umgebogen werden
					filePosition.setAbsoluteFilePosition(newObjectFilePosition);
				}
				else {
					dynamicObject.setLastFilePosition(FilePointer.fromAbsolutePosition(newObjectFilePosition, this));
				}
			}

			return newObjectFilePosition;
		}// synch
	}

	/**
	 * Speichert ein Objekt ans Ende der übergebenen Datei. Die Datei wird nach dem Schreibzugriff nicht geschlossen. Die Dateiposition wird am Objekt
	 * aktualisiert.
	 *
	 * @param configurationObject Objekt, das gespeichert werden soll
	 * @param file                Datei, in der das Objekt gespeichert werden soll.
	 * @param declareGap          true = (Normalfall) Das Objekt wird gespeichert, es existiert aber noch eine ältere Version des Objekts in der Datei und diese
	 *                            wird als Lücke deklariert (ID wird auf 0 gesetzt); false = (Anlegen einer neuen Konfigurationsbereichsdatei oder bei
	 *                            Reorganisation) Das Objekt wird in der Datei gespeichert (es wird keine Lücke erzeugt). Dies ist aber die erste Version des
	 *                            Objekts in der Datei und somit gibt es auch keinen Vorgänger der als Lücke deklariert werden soll
	 * @param setNewFilePosition  true = (Normalfall) Das Objekt wird in der Datei gespeichert und die neue Position in der Datei wird am Objekt, das sich im
	 *                            Speicher befindet, gespeichert; false = Das Objekt wird in der Datei gespeichert, aber die neue Dateiposition wird nicht am
	 *                            Objekt gespeichert sondern muss nachträglich am Objekt, das sich im Speicher befindet, gesetzt werden. Ein Beispiel wäre die
	 *                            Reorganisation. Das Objekt wird in der neue Datei gespeichert, aber die Dateiposition wird am Objekt im Speicher erst dann
	 *                            gesetzt, wenn die Reorganisation erfolgreich abgeschlossen werden konnte.
	 *
	 * @return Position in der Datei, an der das Objekt gespeichert wurde
	 *
	 * @throws IOException           Fehler beim schreiben in die Datei, Teile des Datensatzes wurden bereits geschrieben
	 * @throws IllegalStateException Fehler beim sammeln der Daten, die Datei wurde noch nicht verändert
	 */
	private long writeConfigurationObjectToFile(
			ConfigurationObjectInformation configurationObject, final BufferedRandomAccessFile file, boolean declareGap, boolean setNewFilePosition
	) throws IOException, IllegalStateException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer;
		final long id;
		final int pidHashCode;
		final long typeId;
		final short firstInvalidVersion;
		final short firstValidVersion;
		final byte[] packedData;

		try {
			serializer = SerializingFactory.createSerializer(_serializerVersion, out);

			synchronized(configurationObject) {
				// Das Objekt ist derzeit für alle Zugriffe gesperrt, also werden alle Informationen angefordert.
				// So ist sichergestellt, das sie sich nach freigabe des Objekts nicht ändern.
				// Daten die sich nicht mehr ändern werden ebenfalls lokal gespeichert, damit sie nicht beim
				// schreiben angefordert werden müssen und dabei eventuell blockieren und somit die ganze Datei sperren.

				id = configurationObject.getID();
				pidHashCode = configurationObject.getPid().hashCode();
				typeId = configurationObject.getTypeId();
				firstInvalidVersion = configurationObject.getFirstInvalidVersion();
				firstValidVersion = configurationObject.getFirstValidVersion();

				// Die Version, in der ein Objekt gültig werden soll, muss Größer 0 sein.
				if(firstValidVersion <= 0) {
					throw new IllegalStateException(
							"Ein Konfigurationsobjekt " + configurationObject.getPid() + " ist in einer Version kleiner 1 gültig. Dies ist nicht erlaubt."
					);
				}

				// Als nächstes werden die Daten des SystemObjects bearbeitet (alles, bis auf die Id wird eingetragen)
				writeSystemObjectWithoutIdAndTypeId(configurationObject, serializer);

				// Alle Daten, die speziell für ein Konfigurationsbjekt sind und noch fehlen

				// Mengen des Konfigurationsobjekts
				// Anzahl Mengen, short
				// Je Eintrag
				//	 Id der Menge, long
				// 		Anzahl Elemente in der Menge, int
				// 		Je Objekt in der Menge
				// 			Id des Objekts in der Menge, long

				// Alle Mengen, mit ihrer ID anfordern
				final long objectSets[] = configurationObject.getObjectSetIds();

				// Anzahl der Menge speichern
				serializer.writeShort(objectSets.length);

				// Id der Menge speichern und Objekte der Menge anfordern und speichern

				for(final long objectSetId : objectSets) {
					serializer.writeLong(objectSetId);

					// Objekte der Menge anfordern
					final long objects[] = configurationObject.getObjectSetObjects(objectSetId);

					// Anzahl Elemente der Menge speichern
					serializer.writeInt(objects.length);

					for(final long object : objects) {
						// Objekte der Menge speichern
						serializer.writeLong(object);
					}
				}
			} // synch

			// Im Serializer liegen nun alle Daten, die geschrieben werden sollen, diese als Byte-Array anfordern
			// und packen. Danach ist die Länge des Objekts bekannt.
			packedData = zip(out.toByteArray());
		}
		catch(Exception e) {
			_debug.error("Id: " + configurationObject.getID() + " Serialisiererversion: " + _serializerVersion, e);
			throw new IllegalStateException(
					"Die Daten eines dynamischen Objekts können nicht serialisiert werden, das Objekt wird nicht gespeichert: Id" + configurationObject.getID()
			);
		}

		// Die Länge kann berechnet werden
		// Id + HashCode + typeId + (dyn oder konf) + Version + Version + gepackte Daten
		final int dynamicObjectSize = 8 + 4 + 8 + 1 + 2 + 2 + packedData.length;

		// Datei sperren
		synchronized(_configAreaFile) {
			// Das dyn. Objekt ans Ende der Datei schreiben
			// Die neue Dateiposition speichern, nach erfolgreichen schreiben wird die Position am Objekt vermerkt
			final long newObjectFilePosition = file.length();

			// filedescriptor positionieren, nötig, weil der Descriptor nicht am Ender der Datei stehen muss, auch wenn
			// vorher ein Objekt geschrieben wurde (das alte Objekt wurde als Lücke markiert, also steht der FD mitten in der Datei)
			file.seek(newObjectFilePosition);

			// Länge der Daten, int
			// Id, long
			// hashCode der Pid, int
			// typeId des Objekts
			// Kennzeichnung Konfigurationsobjekt(0), byte
			// Version nicht mehr gültig ab, short
			// Version gültig ab, short

			file.writeInt(dynamicObjectSize);
			file.writeLong(id);
			file.writeInt(pidHashCode);
			file.writeLong(typeId);
			file.writeByte(0);
			file.writeShort(firstInvalidVersion);
			file.writeShort(firstValidVersion);

			// Jetzt können die gepackten Daten gespeichert werden.
			// Die anzahl Bytes, die zu diesem Array gehören, können beim einlesen des Daten aus der Länge
			// des gesamten Objekts berechnet werden, da der ungepackte Teil konstant ist.
			file.write(packedData);

			// Soll eine Lücke erzeugt werden
			if(declareGap) {
				// Die Vorgängerversion in der Datei als Lücke deklarieren (ID auf 0)
				final FilePointer lastFilePosition = configurationObject.getLastFilePosition();
				declareObjectAsAGap(lastFilePosition, file);
			}
			// Soll die neue Position am Objekt im Speicher gespeichert werden
			if(setNewFilePosition) {
				// Das Objekt wurde gespeichert, also muss die neue Dateiposition am Objekt, das sich im Speicher befindet,
				// neu gesetzt werden.
				configurationObject.setLastFilePosition(FilePointer.fromAbsolutePosition(newObjectFilePosition, this));
			}
			return newObjectFilePosition;
		} // synch
	}

	/**
	 * Setzt die Id eines Objekts auf 0 und erklärt es somit als Lücke. Es gibt keinen unterschied zwischen dynamischen Objekten und Konfigurationsobjekten
	 *  @param filePosition Position des Objekts (Länge des Objekts).
	 * @param file         Dateiobjekt, mit dem auf die Platte zugegriffen werden kann
	 */
	private void declareObjectAsAGap(final FilePointer filePosition, BufferedRandomAccessFile file) throws IOException {
//		System.out.println("Lücke einfügen, Dateiposition: " + filePosition);
		if(filePosition == null){
			return;
		}
		synchronized(_configAreaFile) {
			// Die Id des Datensatzes steht nach der Länge, die Länge ist ein Integer, also kann dieser Übersprungen werden
			file.seek(filePosition.getAbsoluteFilePosition() + 4);
			// Der Zeiger steht auf der Id des Datensatzes, diese wird nun mit einer 0 überschrieben und somit als
			// Lücke deklariert
			file.writeLong(0);
		}
	}

	void declareObjectAsAGap(final FilePointer filePosition) {
		try {
			synchronized(_configAreaFile) {
				final BufferedRandomAccessFile configAreaFile = new BufferedRandomAccessFile(_configAreaFile, "rw");
				try {
					declareObjectAsAGap(filePosition, configAreaFile);
				}
				finally {
					configAreaFile.close();
				}
			}
		}
		catch(IOException e) {
			_debug.warning("Fehler beim Löschen eines Konfigurationsobjekts", e);
		}
	}

	/**
	 * Packt ein Byte-Array mit dem Packer "ZIP" und gibt die Daten gepackt zurück
	 *
	 * @param data ungepackte Daten
	 *
	 * @return gepackte Daten
	 */
	static byte[] zip(byte[] data) throws IOException {

		ByteArrayOutputStream packedData = new ByteArrayOutputStream();
		DeflaterOutputStream zipper = new DeflaterOutputStream(packedData);

		// Packen
		zipper.write(data);
		zipper.close();

		return packedData.toByteArray();
	}

	static byte[] unzip(byte[] zippedData) throws IOException {

		ByteArrayOutputStream unpackedData = new ByteArrayOutputStream();
		InflaterOutputStream unzipper = new InflaterOutputStream(unpackedData);

		// Entpacken
		unzipper.write(zippedData);
		unzipper.close();

		return unpackedData.toByteArray();
	}

	/**
	 * Speichert die Pid, den Namen des SystemObjects und alle konfigurierenden Datensätze .
	 *
	 * @param object     Objekt, von dem nur gewissen Teile gespeichert werden
	 * @param serializer Byte-Strom, in den die Daten eingefügt werden
	 */
	private void writeSystemObjectWithoutIdAndTypeId(final SystemObjectInformationInterface object, Serializer serializer) throws IOException {

		// Pid, byte,String
		// Name, byte, String
		// konfigurierende Datensätze (Erklärung weiter unten)

		// Pid anfordern
		final String pid = object.getPid();
		serializer.writeByte(pid.length());

		// String der Pid speichern
		if(pid.length() > 0) {
			serializer.writeString(pid, 255);
		}
		else {
			
			serializer.writeString(pid, 0);
		}
//		serializer.writeString(pid, pid.length());

		// Den Namen speichern, funktionsweise wie Pid
		final String name = object.getName();
		serializer.writeByte(name.length());
		if(name.length() > 0) {
			serializer.writeString(name, 255);
		}
		else {
			
			serializer.writeString(name, 0);
		}
//		serializer.writeString(name, name.length());

		// konfigurierende Datensätze speichern

		// Anzahl Datensätze, int
		// je Datensatz:
		//		Atg-Verwendung, long
		//		Länge des serialisierten Datensatzes, int
		//		Datensatz als Byte-Array

		// Id's der konfigurierenden Datensätze anfordern
		final long configDataSetIds[] = object.getConfigurationsDataAttributeGroupUsageIds();

		// Anzahl Datensätze ist nun bekannt
		serializer.writeInt(configDataSetIds.length);


		for(final long atgUserId : configDataSetIds) {
			final byte[] dataSet = object.getConfigurationData(atgUserId);

			// Atg-Verwendung
			serializer.writeLong(atgUserId);
			// Länge des serialisierten Datensatzes
			serializer.writeInt(dataSet.length);
			// Den serialisierten Datensatz schreiben
			serializer.writeBytes(dataSet);
		}
	}

	short getActiveVersion() {
		return _activeVersion;
	}


	/**
	 * Diese Methode wird von einem Objekt aufgerufen, wenn Informationen des Objekts geändert wurden. Das gesamte Objekt wird persistent gespeichert, sobald die
	 * Methode {@link ConfigurationFileManager#saveConfigurationAreaFiles()} aufgerufen wird. Die Methode kann mehrfach vom selben Objekt aufgerufen werden, es
	 * wird nur einmal gespeichert.
	 * <p>
	 * Es muss außerdem geprüft werden, ob das Objekt Auswirkungen auf die drei Zeitstempel _dynamicObjectChanged,_configurationDataChanged,_configurationObjectChanged
	 * hat (siehe TPuK1-51).
	 *
	 * @param modifiedObject dynamisches Objekt oder Konfigurationsobjekt, dessen Informationen geändert wurden und das somit persistent in der Datei des
	 *                       Konfigurationsbereichs gespeichert werden muss
	 */
	void objectModified(SystemObjectInformationInterface modifiedObject) {
		synchronized(_modifiedObjects) {
			if(!_modifiedObjects.contains(modifiedObject)) {
				_modifiedObjects.add(modifiedObject);
			}
		}
	}

	/**
	 * Diese Methode entfernt ein Objekt aus Menge zu speichernden Objekte
	 *
	 * @param object Objekt, das nicht gespeichert werden soll
	 */
	void dontSaveObject(SystemObjectInformationInterface object) {
		synchronized(_modifiedObjects) {
			_modifiedObjects.remove(object);
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein dynamisches Objekt auf "ungültig" gesetzt wird. Die Methode wird vom Objekt selber aufgerufen und aktualisiert alle
	 * Datenstrukturen, die das Objekt als "gültig" führen. Konfigurationsobjekte müssen nicht beachtet werden, da diese erst in der "nächsten" Version
	 * ungültig/gültig werden. Dies wird beim einlesen der Datei erkannt und die Datenstrukturen werden sofort richtig angelegt. Die Methode ist blockierend, wenn
	 * eine Reorganisation stattfindet.
	 *
	 * @param invalidObject Objekt, das "ungültig" geworden ist
	 */
	void setDynamicObjectInvalid(DynamicObjectInformation invalidObject) {
		synchronized(_restructureLock) {
			// Das Objekt aus alle Datenstrukturen entfernen, in denen es als gültig gespeichert ist
			synchronized(_actualObjects) {
				_actualObjects.remove(invalidObject.getID());
			}

			final ObjectReference oldObjRef;

			if(invalidObject.getPersPersistenceMode() != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
				oldObjRef = invalidObject.getLastFilePosition();
			}
			else {
				oldObjRef = new TransientObjectReference(invalidObject);
			}
			synchronized(_oldObjectsId) {
				_oldObjectsId.put(invalidObject.getID(), oldObjRef);
			}

			FilePointer lastFilePosition = invalidObject.getLastFilePosition();
			if(lastFilePosition != null) {
				// null bei transienten Objekten. In dem Fall kann hier nichts eingetragen werden.
				// Man könnte in Zukunft in _oldObjectsPid ObjectReference-s eintragen statt FilePointer. 
				
				synchronized(_oldObjectsPid) {
					Set<FilePointer> filePositions = _oldObjectsPid.get(invalidObject.getPidHashCode());

					if(filePositions != null) {
						// Es gibt eine Liste, also Dateipostion einfügen
						filePositions.add(lastFilePosition);
					}
					else {
						// Es gibt noch keine Liste
						filePositions = new HashSet<FilePointer>();
						filePositions.add(lastFilePosition);

						// Die neue Liste in die Map einfügen
						_oldObjectsPid.put(invalidObject.getPidHashCode(), filePositions);
					}
				}
			}

			// TypeId Maps aktualisieren
			synchronized(_actualObjectsTypeId) {
				final List<SystemObjectInformationInterface> actualTypeList = _actualObjectsTypeId.get(invalidObject.getTypeId());
				if((actualTypeList == null) || (!actualTypeList.remove(invalidObject))) {
					// Das entfernen hat nicht geklappt
					_debug.error("Das Objekt " + invalidObject + " konnte nicht aus der TypeId Map entfernt werden");
				}
			}

			synchronized(_oldObjectsTypeId) {

				// In die Map mit alten Objekten eintragen
				putOldObjectTypeId(
						invalidObject.getTypeId(), invalidObject.getFirstValidTime(), invalidObject.getFirstInvalidTime(), false, oldObjRef
				);
			}

			// Maps des FileManagers aufräumen
			_fileManager.setDynamicObjectInvalid(invalidObject);
		} // synch
	}

	/**
	 * Diese Methode wird aufgerufen, wenn der Konfigurationsverantwortliche eine neue Version aktiviert. Der Aufruf bewirkt, dass die Datei falls erforderlich
	 * restrukturiert wird. Diese Methode muss druch den Konfigurationsverantwortlichen aufgerufen werden, wenn dieser die aktive Version wechselt.
	 *
	 * @return true = Die Reorganisation war erfolgreich oder nicht erforderlich; false = Die Reorganisation war nicht erfolgreich
	 *
	 * @see #restructure
	 */
	@Override
	public boolean initialVersionRestructure() {
		// Die lokale Zuordnung von Version zu Zeitstempel ist bei diesem Aufruf auch gleichzeitig die Zuordnung
		// des Konfigurationsverantwortlichen.

		// Zu diesem Zeitpunkt wurde der Header bereits eingelesen die "alte" Zuordung von Version/Zeitpunkt ist
		// bekannt und muss nun um die neuen Werte erweitert werden.

		synchronized(_restructureLock) {
			boolean restructureNeeded = false;
			final Set<Short> localKeys = _localVersionActivationTime.keySet();
			for(final Short localKey : localKeys) {
				// In der alten Liste prüfen, ob der Wert vorhanden ist
				if(!_configurationAuthorityVersionActivationTime.containsKey(localKey)) {
					// Neues Version/Zeitpunkt Paar einfügen
					_configurationAuthorityVersionActivationTime.put(localKey, _localVersionActivationTime.get(localKey));
					_globalActivationTimes = getActivationTimeArray(_configurationAuthorityVersionActivationTime);
					restructureNeeded = true;
				}
				else {
					// Diese Überprüfung braucht nicht stattfinden, kann aber auf Fehler und manuelle Änderungen in der Verwaltungsdatei hinweisen, wenn
					// die übergebenen Werte nicht mit den gespeicherten übereinstimmen
					final long oldValue = _configurationAuthorityVersionActivationTime.get(localKey);
					final long newValue = _localVersionActivationTime.get(localKey);

					if(oldValue != newValue) {
						// Die Werte stimmen nicht überein
						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						String oldDateString = dateFormat.format(oldValue);
						String newDateString = dateFormat.format(newValue);
						_debug.info(
								"Der gespeicherte Aktivierungszeitpunkt (" + oldDateString + ") des Bereichs " + _configAreaFile + " für die Version "
										+ localKey + " stimmt nicht mit dem Aktivierungszeitpunkt in der Verwaltungsdatei (" + newDateString + ") überein."
						);
					}
				}
			}
			// Datei restrukturieren damit die neue Zuweisung von Version zu einem Zeitstempel im Header gespeichert wird.
			if(restructureNeeded) {
				try {
					restructure(RestructureMode.FullRestructure);
					return true;
				}
				catch(IOException e) {
					_debug.error("Fehler bei der Restrukturierung des Bereichs " + _configAreaFile, e);
					return false;
				}
			}
			_debug.info("Eine erneute Restrukturierung des Bereichs " + _configAreaFile + " ist nicht erforderlich.");
			return true;
		}
	}

	@Override
	public long getGreatestId() {
		return _greatestId;
	}

	public void restructure(final RestructureMode mode) throws IOException {
		try {
			restructureMain(mode);
		}
		catch(NoSuchVersionException e) {
			throw new IOException(e);
		}
		catch(RuntimeException e) {
			// Unschön, aber es werden z.B. IllegalArgumentExceoptions geworfen, die hier besser abgefangen werden
			throw new IOException(e);
		}
	}

	private void restructureMain(RestructureMode mode) throws IOException, NoSuchVersionException {
		// Alle Zugriffe auf alte Objekte, die sich im Speicher befinden, sperren
		synchronized(_restructureLock) {
			// Datei sperren, so werden mögliche Dateizugriffe solange blockiert, bis die neue Datei zur Verfügung steht.
			// Die Dateizugriffe werden dann sofort auf der neuen(reorganisierten) Datei durchgeführt.
			synchronized(_configAreaFile) {

				if(_actualObjects.size() == 0) {
					// falls Mischmenge noch nicht geladen, dies tun
					getMixedObjectSetObjects();
					assert _actualObjects.size() > 0;
				}

				// Alle Dateioperationen verändern nicht die Objekte, die sich im Speicher befinden.
				// Erst wenn die Reorganisation erfolgreich beendet wurde, werden die Objekte im Speicher
				// auf die neue Situation angepaßt und aus den HashMap's entfernt und die aktuellen/zukünftig aktuellen
				// Objekte auf die neue Dateiposition gesetzt.
				// Kommt es zu einem Fehler bei der Reorganisation, wird die neue fehlerhaft reorganisierte Datei umbenannt
				// (damit sie analysiert werden kann) und es wird auf der alten Datei weitergearbeitet.
				// Damit dieses Verfahren durchgeführt werden kann, müssen alle Schritte der Reorganisation "protokolliert"
				// werden. Im RestructureInfo werden deswegen die Dateipositionen aller gültigen bzw. neuen Objekte der Mischmenge
				// gemerkt, sodass diese bei Bedarf im laufenden Betrieb aktualisiert werden können.
				RestructureInfo restructureInfo;

				BufferedRandomAccessFile oldConfigAreaFile = new BufferedRandomAccessFile(_configAreaFile, "r");

				// Alle Daten, die sich geändert haben, speichern
				flush();

				// Die neue Datei, in der der Konfigurationsbereich reorganisiert wird
				File configAreaNewName = new File(_configAreaFile.getAbsolutePath() + "New");

				BufferedRandomAccessFile newConfigAreaFile = new BufferedRandomAccessFile(configAreaNewName, "rw");

				try {
					// Versuchen zu restrukturieren
					restructureInfo = createRestructuredCopy(oldConfigAreaFile, newConfigAreaFile, mode);
				}
				finally {
					oldConfigAreaFile.close();
					newConfigAreaFile.close();
				}

				// Wenn das Restrukturieren geklappt hat, die alte Datei durch die neue Datei ersetzen
				// Vorher auf jeden Fall beide Dateien schließen (finally-Block oben)!
				swapFiles(restructureInfo, configAreaNewName);

			} // synch(datei)
		} // synch(_restruct)
	}

	public void swapFiles(final RestructureInfo restructureInfo, final File configAreaNewName) throws IOException {
		// Die original Datei in Name.configold umbennen
		final String originalFileName = _configAreaFile.getAbsolutePath();
		final File oldConfigFile = new File(originalFileName);

		// gibt es von einer vorherigen Reorganisation noch ein old-File, wenn ja, löschen
		final File lastRestructureOldFile = new File(originalFileName + "Old");
		if(lastRestructureOldFile.exists()) {
			lastRestructureOldFile.delete();
		}

		if(!oldConfigFile.renameTo(new File(originalFileName + "Old"))) {
			// Die Reorganisation ist fehlgeschlagen
			throw new IOException("Reorganisation: Fehler beim umbennen der aktuellen Konfigurationsdatei: " + originalFileName);
		}
		if(!configAreaNewName.renameTo(new File(originalFileName))) {
			// Fehler, die neue Datei konnte nich umbenannt werden. Also wird die alte Datei weiter benutzt.
			// Die Reorganisation ist fehlgeschlagen.
			if(!oldConfigFile.renameTo(new File(originalFileName))) {
				
				_debug.error("Die alte Konfigurationsdatei kann nicht weiter benutzt werden");
			}

			throw new IOException(
					"Reorganisation: Fehler beim umbennen der neuen Konfigurationsdatei " + configAreaNewName + "in " + originalFileName
			);
		}

		// Dateiumbenennen hat geklappt, also neue Dateipositionen an den Objekten merken und neue
		// Dateioffsets setzen
		Map<Long, Long> newFilePositions = restructureInfo._newFilePositions;
		for(SystemObjectInformationInterface obj : _actualObjects.values()) {
			SystemObjectInformation objectInformation = (SystemObjectInformation) obj;
			FilePointer lastFilePosition = objectInformation.getLastFilePosition();
			if(lastFilePosition != null) {
				Long newPos = newFilePositions.get(lastFilePosition.getAbsoluteFilePosition());
				if(newPos != null) {
					lastFilePosition.setAbsoluteFilePosition(newPos);
				}
			}
		}
		for(SystemObjectInformationInterface obj : _newObjects.values()) {
			SystemObjectInformation objectInformation = (SystemObjectInformation) obj;
			FilePointer lastFilePosition = objectInformation.getLastFilePosition();
			if(lastFilePosition != null) {
				Long newPos = newFilePositions.get(lastFilePosition.getAbsoluteFilePosition());
				if(newPos != null) {
					lastFilePosition.setAbsoluteFilePosition(newPos);
				}
			}
		}
		for(ObjectReference reference : _oldObjectsId.values()) {
			if(reference instanceof FilePointer) {
				FilePointer lastFilePosition = (FilePointer) reference;
				Long newPos = newFilePositions.get(lastFilePosition.getAbsoluteFilePosition());
				if(newPos != null) {
					lastFilePosition.setAbsoluteFilePosition(newPos);
				}
			}
		}
		updateHeaderPositions(restructureInfo);

		// In der Map _oldObjectsId sind alle Objekte gespeichert, die als ungültig markiert sind
		// und die sich in der Mischmenge befinden. Das gleiche gilt für die Map _oldObjectsPid
		// Diese Maps müssen nun um die Objekte bereinigt werden, die in die nGa Bereiche oder in den
		// dyn nGa Bereich gespeichert wurden.

		// In dem Objekt, das alle ConfigFiles verwaltet wird ebenfalls eine Map mit Id's
		// für die alten Objekte geführt, diese muss ebenfalls entfernt werden
		synchronized(_oldObjectsId) {
			Iterator<Map.Entry<Long, ObjectReference>> iterator = _oldObjectsId.entrySet().iterator();
			while(iterator.hasNext()) {
				final Map.Entry<Long, ObjectReference> entry = iterator.next();
				// Transiente, gelöschte Objekte beibehalten
				if(entry.getValue() instanceof FilePointer) {
					_fileManager.removeObject(entry.getKey());
					iterator.remove();
				}
			}
		}

		synchronized(_oldObjectsPid) {
			_oldObjectsPid.clear();
		}

		synchronized(_oldObjectsTypeId) {
			_oldObjectsTypeId.clear();
		}

		_debug.info(
				"Die Restrukturierung des Konfigurationsbereichs " + _configurationAreaPid + " in der Datei " + _configAreaFile
						+ " wurde erfolgreich abgeschlossen"
		);
	}


	/**
	 * Führt eigentliche Restrukturierung aus
	 * @param oldConfigAreaFile
	 * @param newConfigAreaFile
	 * @param mode
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	private RestructureInfo createRestructuredCopy(final BufferedRandomAccessFile oldConfigAreaFile, final BufferedRandomAccessFile newConfigAreaFile, final RestructureMode mode) throws IOException, NoSuchVersionException {

		final RestructureInfo restructureInfo = new RestructureInfo();

		// Ende des Header
		final long newAbsoluteEndHeader;
		// relative Anfangsposition der Menge, die alle alten dynamischen Objekte enthält
		final long newRelativeDynObjectArea;
		// relative Anfangsposition des Id Indizes
		final long newRelativeIdIndex;
		// relative Anfangsposition des Pid Indizes
		final long newRelativePidIndex;
		// relative Anfangsposition der Mischobjektmenge
		final long newRelativeMixedSet;

		// Es existiert ein Index nach Id. In diesem Index sind alle Objekte nach Id einsortiert, zusätzlich
		// ist die relative Dateiposition zum Header gespeichert.
		// Damit dieser Index später aufgebaut werden
		// kann, wird jedes Objekt, das umkopiert wird, in einer Liste gespeichert.
		// Später werden auch die dynamischen Objekte in diese Liste eingetragen
		final List<SortObject> newIdIndex = new ArrayList<SortObject>();

		// Es existiert ein Index, in dem sich alle alten Objekte (aus ngA und dynamisch nGa) befinden.
		// Es wird der HashCode der Pid benutzt, als Ergebnis wird eine Liste mit relativen Dateipotionen
		// geliefert. Mit den Dateipositionen kann das "passende" Objekt zu der Pid geladen werden.
		// In dieser Map werden alle Objekte eingetragen, die in die Menge nGa oder dyn nGa eingetragen werden sollen.
		// Als Key dient der HashCode der Pid. Rückgabe ist eine Liste, in der Liste sind alle Dateipositionen (relativ) aller Objekte enthalten,
		// die ebenfalls den HashCoder der Pid besitzen (wie der Schlüssel)
		final Map<Integer, SortObjectPid> newPidIndex = new HashMap<Integer, SortObjectPid>();


		// Einen Pre-Header erzeugen, die relative Dateiposition der neuen nGa Blöcke ist noch unbekannt, dadurch
		// sind auch die anderen relativen Positionen noch unbekannt. Es wird lediglich Platz reserviert.
		// Falls ein neuer nGa-Block keine Elemente besitzt, wird dieser als relative Dateipostion eine -1 bekommen.

		// Der neue Header setzt sich aus dem alten Header und den neuen nGa Blöcken zusammen.
		// Der Header wächst also wegen der neuen nGa Blöcken.
		// Jeder neue nGa Block, wird mit zwei Longs und einem Short abgebildet.
		// Es gibt soviele neue Blöcke, wie es Versionen zwischen dem letzten erzeugten nGa Block (Reorganisation)
		// und der jetzigen Version gibt.
		// Die Variable _nextInvalidBlockVersion speichert die erste Version des nGa Blocks, der bei der Reorganisation
		// geschrieben werden muss.

		// Anzahl nGa Blöcke, die eingefügt werden müssen
		final int numberOfNewOldBlocks;
		if(_nextInvalidBlockVersion > _activeVersion) {
			// Das ist immer dann der Fall, wenn es keinen "neuen" alten Block gibt.
			// zb. beim Neustart des Systems, oder wenn gerade eine Reorganisation stattgefunden hat, aber
			// es gibt keine Konfigurationsobjekte für einen neuen alten Block.
			numberOfNewOldBlocks = 0;
		}
		else {
			// Wieviele nGa Blöcke müssen eingefügt werden ? Soviele, wie Versionen fehlen, bis zur aktuellen Version
			// und zwar einschließlich der aktuellen Version (diese Objekte sind in der derzeit aktuellen Version
			// ungültig geworden), darum +1
			numberOfNewOldBlocks = (_activeVersion - _nextInvalidBlockVersion) + 1;
		}

		if(numberOfNewOldBlocks != 0 && mode == RestructureMode.DynamicObjectRestructure){
			// Beim Restrukturieren der dynamischen Objekte im laufenden Betrieb dürfen keine neuen Blöcke angelegt werden
			throw new IllegalArgumentException("Nach der Aktivierung muss eine vollständige Restrukturierung erfolgen");
		}

		// Speichert die Größe des neuen Headers
		// Die größe des alten Headers setzt sich aus seiner (Endeposition in der Datei) - (Länge des Headers, Integer) zusammen
		// Das Stück des neuen Headers ist um 2 Longs und einem Short pro nGa Bereich größer
		final int headerSizeNewFileArea = (int)(_headerEnd - 4 + numberOfNewOldBlocks * (2 * 8 + 2));

		// In die neue Konfigurationsbereichsdatei einen Pre-Header schreiben
		newConfigAreaFile.writeInt(headerSizeNewFileArea * (-1));
		// Platzhalter schreiben
		final byte[] byteArray = new byte[headerSizeNewFileArea];
		for(int nr = 0; nr < byteArray.length; nr++) {
			byteArray[nr] = -100;
		}
		newConfigAreaFile.write(byteArray);

		newAbsoluteEndHeader = newConfigAreaFile.getFilePointer();

		//*********************************************************************************************************
		// Nach dem Pre-Header die alten nGa-Blöcke speichern

		// Anfang der Daten suchen
		final long startOldObjectBlocks;

		// es wird davon ausgegangen, dass die NGA-Blöcke (sofern vorhanden) direkt nach dem Header anfangen (vgl. writeHeader)
		startOldObjectBlocks = _headerEnd;

		oldConfigAreaFile.seek(startOldObjectBlocks);

		assert headerSizeNewFileArea + 4 == newAbsoluteEndHeader;

		// Objekte der bisherigen Nga-Blöcke (für Config-objekte) in neue Datei kopieren
		switch(mode){
			case DeleteObjectsPermanently:
			case FullRestructure: // fallthrough
				// Bei einer vollen Restrukturierung eventuelle Lücken füllen.
				// Lücken ggf. füllen (es sollte keine Lücken geben, außer bei Konfigurationsdateien wo gelöschte Objekte wieder modifiziert wurden)
				// (Fehler in frühen Konfigurationsversionen)
				copyObjectsRemoveGaps(oldConfigAreaFile, newConfigAreaFile, (_startOldDynamicObjects + _headerEnd), newIdIndex, newPidIndex, 0, newAbsoluteEndHeader);
				break;
			case DynamicObjectRestructure:
				// bei der Restrukturierung im laufenden Betrieb keine Lücken füllen, damit die Dateipositionen gleich bleibem
				copyObjectsPreserveGaps(oldConfigAreaFile, newConfigAreaFile, (_startOldDynamicObjects + _headerEnd), newIdIndex, newPidIndex, 0, newAbsoluteEndHeader);
				break;
		}

		//*********************************************************************************************************

		short consideredOldVersion = _nextInvalidBlockVersion;
		// Es werden alle nGa Blöcke erzeugt, die es bis zur jetzigen Version gibt.
		_nextInvalidBlockVersion = (short)(_activeVersion + 1);

		synchronized(_oldObjectsId) {
			// ungültige Objekte können auch in der aktiven Version sein, darum <=
			while(consideredOldVersion <= _activeVersion) {

				Long[] keysLong = _oldObjectsId.keySet().toArray(new Long[_oldObjectsId.keySet().size()]);
				// Es wird ein neuer nGa Bereich erzeugt

				// boolean ob ein Element zu dem Block hinzugefügt wurde. wenn ja, dann dateipostion in map
				// speichern, wenn nein -1 als dateiposition
				// Wenn in einen nGa Bereich keine ungültigen Objekte angelegt werden können, dann bleibt diese
				// Variable false und als relative Startposition des Blocks wird eine -1 eingetragen
				boolean blockHasElements = false;

				// Speichert den relativen Beginn des potentiellen Blocks, der geschrieben werden soll
				final long relativeBlockPosition = (newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader);

				// Speichert den Zeitpunkt, zu dem die Version gültig wurde
				final long blockTimeStamp;
				if(_configurationAuthorityVersionActivationTime.containsKey(consideredOldVersion)) {
					blockTimeStamp = _configurationAuthorityVersionActivationTime.get(consideredOldVersion);
				}
				else {
					// Dieser Fall sollte niemals auftreten
					_debug.error(
							"Es gibt zu einer alten Version keinen Zeitstempel: Version " + consideredOldVersion + " Konfigurationsbereich: "
									+ _configAreaFile + " . Die Reorganisation wird abgebrochen"
					);
					throw new IllegalStateException(
							"Es gibt zu einer alten Version keinen Zeitstempel: Version " + consideredOldVersion + " Konfigurationsbereich: "
									+ _configAreaFile + " . Die Reorganisation wird abgebrochen"
					);
				}

				for(final Long idOldObject : keysLong) {
					

					// Das Objekt anfordern, es werden nur Konfigurationsobjekte betrachtet.
					final SystemObjectInformationInterface oldObject = getSystemObjectInfo(_oldObjectsId.get(idOldObject), oldConfigAreaFile);

					// Es werden nur Konfigurationsobjekte betrachtet
					if(oldObject instanceof ConfigurationObjectInfo) {
						final ConfigurationObjectInformation oldConfigObject = (ConfigurationObjectInformation) oldObject;

						if(oldConfigObject.getFirstInvalidVersion() == consideredOldVersion) {
							// Da in der neuen Datei muss ebenfalls ein Id Index angelegt werden muss, muss
							// die neue endgültige relative Position im nGa Bereich des Datensatzes gespeichert werden.
							// Die relative Adresse wird als negativer Wert gespeichert, dies ermöglicht bei Indexzugriffen
							// sofort zu erkennen, ob das Objekt ein Konfigurationsobjekt oder ein dynamisches Objekt
							// gefunden wurde.
							// Die negative relative Position bezieht sich auf das Headerende, da es sich
							// um ein Konfigurationsobjekt handelt.
							// - 4 weil Header bei Byte 4 beginnt
							// (Es muss sowohl die header-Länge, als auch der Offset des HEaders abgezogen werden)
							final long newRelativeObjectPosition = getRelativeFilePositionForInvalidConfigObject(
									(4 + headerSizeNewFileArea), newConfigAreaFile.getFilePointer()
							);
							addToIndizes(newIdIndex, newPidIndex, oldConfigObject, newRelativeObjectPosition);

							// Das Objekt muss in den Bereich eingefügt werden. Es muss keine Lücke deklariert werden
							// auch die Dateiposition muss nicht gespeichert werden.
							writeConfigurationObjectToFile(oldConfigObject, newConfigAreaFile, false, false);
							// Da ein Objekt in den nGa Bereich geschrieben wurde, muss die relative Position
							// im Header unter Kennung 2 gesetzt werden
							blockHasElements = true;
						}
					}
				} // for
				if(blockHasElements) {
					// Es wurden ungültige Objekte in den nGa-Bereich eingetragen
					_oldObjectBlocks.put(consideredOldVersion, new OldBlockInformations(relativeBlockPosition, blockTimeStamp));
				}
				else {
					// Es wurden keine Elemente in den nGa-Bereich eingetragen, also gibt es auch
					// keine Startposition an der die Elemte zu finden sind.
					_oldObjectBlocks.put(consideredOldVersion, new OldBlockInformations((long)-1, blockTimeStamp));
				}

				// Es wurden alle Objekte betrachtet, also die nächste veraltet Version prüfen
				consideredOldVersion++;
			} // while
		} // synch

		//*********************************************************************************************************

		// Den alten dyn Block schreiben und dann die neuen dyn Objekte nach Zeit an den alten Block "ansortieren"

		newRelativeDynObjectArea = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;
		// In der Map _oldObjectsId stehen jetzt nur noch dynamische Objekte

		// Die alten dynamischen Objekte aus der Datei umkopieren
		oldConfigAreaFile.seek(_startOldDynamicObjects + _headerEnd);

		// Diese Variable speichert die neue absolute Startposition des Bereichs, der aller ungütligen dynamischen Objekte
		// in der neuen Konfigurationsbereichsdatei enthält
		final long absoluteStartNewDynamicArea = newConfigAreaFile.getFilePointer();

		// Durch das endgültige Löschen von historischen dynamischen Objekten können sich hier Lücken ergeben,
		// daher hier diese Lücken weglassen

		// Objekte des bisherigen DynNga-Blocks in neue Datei kopieren
		switch(mode) {
			case DynamicObjectRestructure:
				// Keine Lücken füllen, damit Dateipositionen gleich bleiben
				copyObjectsPreserveGaps(oldConfigAreaFile, newConfigAreaFile, (_startIdIndex + _headerEnd), newIdIndex, newPidIndex, absoluteStartNewDynamicArea, (4 + headerSizeNewFileArea));
				break;
			case FullRestructure:
				// Lücken ggf. füllen (es sollte keine Lücken geben, außer bei Konfigurationsdateien wo gelöschte Objekte wieder modifiziert wurden)
				// (Fehler in frühen Konfigurationsversionen)
				copyObjectsRemoveGaps(oldConfigAreaFile, newConfigAreaFile, (_startIdIndex + _headerEnd), newIdIndex, newPidIndex, absoluteStartNewDynamicArea, (4 + headerSizeNewFileArea));
				break;
			case DeleteObjectsPermanently:
				// hier werden die eigentlichen Löschungen vorgenommen
				copyObjectsRemoveGapsAndDeleteObjects(oldConfigAreaFile, newConfigAreaFile, (_startIdIndex + _headerEnd), newIdIndex, newPidIndex, absoluteStartNewDynamicArea, (4 + headerSizeNewFileArea));
				break;
		}


		// Die dynamischen Objekte der Mischmenge, die ungültig sind, speichern.
		// Diese müssen aufsteigend nach Invalid-Time sortiert werden

		// Alle dynamischen Objekte, die in Frage kommen, sind in der Map _oldObjectsId gespeichert

		// Array, das zu allen dynamischen Objekten den Zeitstempel enthält, wann das Objekt ungültig geworden ist
		// und die absolute Position in der Datei um das Objekt später zu laden.

		final List<SortObject> dynamicObjects = new ArrayList<SortObject>();

		synchronized(_oldObjectsId) {
			final Long[] keysLong = _oldObjectsId.keySet().toArray(new Long[_oldObjectsId.keySet().size()]);

			// Liste, in der die Objekte gespeichert werden, diese wird später in ein Array umgewandelt

			for(Long idOldObject : keysLong) {
				ObjectReference reference = _oldObjectsId.get(idOldObject);
				if(reference instanceof FilePointer) {  // falls es sich um kein transientes Objekt handelt
					long filePosition = ((FilePointer) reference).getAbsoluteFilePosition();
					oldConfigAreaFile.seek(filePosition);
					final BinaryObject oldObject = BinaryObject.fromDataInput(oldConfigAreaFile);

					if(oldObject instanceof BinaryDynamicObject) {
						final BinaryDynamicObject dynObject = (BinaryDynamicObject) oldObject;

						if(dynObject.getObjectId() == 0){
							// Das sollte nicht passieren!
							throw new IOException("Ungültiges dynamisches Objekt an Dateiposition " + filePosition);
						}

						if(mode == RestructureMode.DeleteObjectsPermanently
								&& dynObject.getFirstInvalid() != 0
								&& _objectsPendingDeletion.contains(dynObject.getObjectId())){
							// Dynamisches Objekt kann auch schon hier gelöscht werden
							// (einfach nicht im NgDyn-Block speichern)
							continue;
						}

						dynamicObjects.add(new SortObject(filePosition, dynObject.getFirstInvalid()));
					}
				}
			}
		}

		// Das Array wird jetzt nach dem Zeitstempel sortiert.

		Collections.sort(dynamicObjects);

		// Array liegt sortiert vor, also können die dynamischen Objekte in den Bereich geschrieben werden

		for(final SortObject dynamicSortObject : dynamicObjects) {
			oldConfigAreaFile.seek(dynamicSortObject.getFilePosition());

			// Hier werden die Objekte ein zweites mal gelesen, im Speicher halten (beim SortObject) wäre die Alternative.
			// Hier müsste man zwischen Performance und Speicherverbrauch abwägen.
			final BinaryDynamicObject oldDynObject = (BinaryDynamicObject) BinaryObject.fromDataInput(oldConfigAreaFile);

			// Wie auch bei den alten Konfigurationsobjekten, müssen die dynamischen Objekte
			// in den Id Index eingetragen werden

			// Die relative Position der Objekte bezieht sich auf den Beginn des dynamischen Bereichs, nicht auf
			// das Headerende. Der Wert wird als positive Zahl gespeichert (Konfigurationsobjekte als negativ, s.o.)
			// Da die Konfigurationsobjekte mit den negativen Zahlen und die dynamischen Objekte
			// mit den positven Zahlen, muss festgelegt werden, zu welchem Bereich die "0" gehört.
			// Die "0" gehört zu den nGa Objekten gehört somit zu den negativen Werten.
			// Also wird jeder relative Position um eins erhöht (aus 0, wird eine +1 und somit wird diese Zahl
			// im Zusammenhang mit dynamischen Objekten niemals im Index vergeben), wenn das Objekt
			// später geladen werden muss, kann an dem positiven Wert erkannt werden, dass es sich
			// um ein dynamisches Objekt handelt und die +1 kann wieder abgezogen werden.
			final long newRelativeObjectPosition = getRelativeFilePositionForInvalidDynamicObject(absoluteStartNewDynamicArea, newConfigAreaFile.getFilePointer());

			addToIndizes(newIdIndex, newPidIndex, oldDynObject, newRelativeObjectPosition);
			restructureInfo.rememberFilePosition(dynamicSortObject.getFilePosition(), newConfigAreaFile.getFilePointer());

			// Objekt in der neuen Datei schreiben. Es muss keine Lücke deklariert werden und die
			// Dateiposition am Objekt ist egal
			oldDynObject.write(newConfigAreaFile);
		}

		// Der aktualisierte "ungültige" dynamische Block wurde erzeugt
		//*********************************************************************************************************

		// Den alten Index (Id) einlesen und die neues Objekte aus den nGa Blöcken + neue dyn Objekte
		// nach Id einsortieren (Id + relative Dateipostion zum Header)

		newRelativeIdIndex = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

		// Der alte Index liegt bereits sortiert in der Datei vor.
		// Also wird die Liste, die die neuen Id's enthält, sortiert (nun liegen beide sortiert vor).
		// Jetzt wird der erste Wert aus der Datei eingelesen (dies ist der kleinste Wert) und mit
		// dem ersten Wert der Liste verglichen. Der kleinere von beiden wird geschrieben. Dann wird
		// der nächste Wert betrachtet, usw (wie Mergesort).

		final SortObject[] newOldObjectSortedArray = newIdIndex.toArray(new SortObject[newIdIndex.size()]);
		Arrays.sort(newOldObjectSortedArray);

		// Filedescriptor auf den Id-Index legen
		oldConfigAreaFile.seek(_startIdIndex + _headerEnd);

		// Objekt, das gerade aus dem Array newOldObjectSortedArray betrachtet wird
		int actualIdMergeObject = 0;

		// Falls true, dann muss das nächste Objekt geholt werden, da das letzte Objekt gespeichert wurde

		while(actualIdMergeObject < newOldObjectSortedArray.length) {
			// Es gibt noch Daten und der alte Wert wurde einsortiert
			SortObject newOldIdObject = newOldObjectSortedArray[actualIdMergeObject];
			actualIdMergeObject++;

			// Id schreiben
			newConfigAreaFile.writeLong(newOldIdObject.getValue());
			// Relative Dateiposition
			newConfigAreaFile.writeLong(newOldIdObject.getFilePosition());
			// nächsten Wert anfordern
		}

		// Alle Id's sind geschrieben
		//*********************************************************************************************************
		// Pid Index, ebenfalls die nGa Blöcke und neue dyn Objekte einfügen
		// (hashCode Pid und relatve Dateipostion, Achtung pro PidHash kann es mehrer Dateipositionen geben)

		newRelativePidIndex = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

		// Es steht eine Map zur Verfügung (newPidIndex), die alle neuen Elemente enthält, die in die dynamische nGa Menge
		// einsortiert werden sollen. Dafür wird ein Array erstellt, in dem alle Objekte nach ihrer
		// Pid (HashCode) sortiert vorlieren. Dieses Array wird dann in die alten Daten eingemischt.

		// Jeder Eintrag hat eine feste Länge und besteht aus Pid HashCode und der relativen Position
		// in der Datei.
		// Nachteil, gibt es zu einer Pid 10 Objekte, so wird die Pid 10 mal gespeichert

		// sortiertes Array erstellen und sortieren
		SortObjectPid[] newOldObjectsPidSortedArray = newPidIndex.values().toArray(new SortObjectPid[newPidIndex.size()]);
		Arrays.sort(newOldObjectsPidSortedArray);

		// Auf den Beginn des Pid HashCode Bereichs positionieren
		oldConfigAreaFile.seek(_startPidHashCodeIndex + _headerEnd);

		// Objekt, das gerade aus dem Array newOldObjectsPidSortedArray betrachtet wird
		int actualPidMergeObject = 0;

		// Falls true, dann muss das nächste Objekt geholt werden, da das letzte Objekt gespeichert wurde

		while(actualPidMergeObject < newOldObjectsPidSortedArray.length) {
			SortObjectPid newOldPidObject = newOldObjectsPidSortedArray[actualPidMergeObject];
			actualPidMergeObject++;

			// Alle Werte des Objekts schreiben
			final List<Long> filePositions = newOldPidObject.getFilePositions();
			for(Long filePosition : filePositions) {
				newConfigAreaFile.writeInt(newOldPidObject.getPidHashCode());
				// Relative Dateiposition, die Position bezieht sich auf die neue Datei
				newConfigAreaFile.writeLong(filePosition);
			}
		}

		//*********************************************************************************************************

		// Die aktuellen und zukünftig aktuellen in die Mischmenge schreiben

		newRelativeMixedSet = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

		writeCurrentObjects(newConfigAreaFile, restructureInfo, _actualObjects);
		writeCurrentObjects(newConfigAreaFile, restructureInfo, _newObjects);

		//*********************************************************************************************************
		// Neue Dateipositionen am RestructureInfo-Objekt merken. Die eigentlichen Fields am ConfigAreaFile werden aktualisiert,
		//sobald die Dateiumbenennung erfolgreich war

		restructureInfo._headerEnd = newAbsoluteEndHeader;

		restructureInfo._startOldDynamicObjects = newRelativeDynObjectArea;

		restructureInfo._startIdIndex = newRelativeIdIndex;

		restructureInfo._startPidHashCodeIndex = newRelativePidIndex;

		restructureInfo._startMixedSet = newRelativeMixedSet;

		// Header mit diesen neuen Positionen schreiben
		writeHeader(restructureInfo, newConfigAreaFile);
		return restructureInfo;
	}

	private void writeCurrentObjects(final BufferedRandomAccessFile newConfigAreaFile, final RestructureInfo restructureInfo, final Map<Long, SystemObjectInformationInterface> objectMap) throws IOException {
		// Diese Methode wird einmal für _allObjects und einmal für _newObjects aufgerufen. Die Synchronisation darauf ist OK.
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized(objectMap) {
			for(SystemObjectInformationInterface systemObjectInfo : objectMap.values()) {

				// Vorherige Dateiposition
				final FilePointer filePosition = ((SystemObjectInformation) systemObjectInfo).getLastFilePosition();

				// Nur Objekte speichern, die auch schon vorher in der Mischmenge gespeichert waren
				if(filePosition != null && filePosition.getAbsoluteFilePosition() >= _headerEnd + _startMixedSet) {

					if(systemObjectInfo instanceof ConfigurationObjectInformation) {
						ConfigurationObjectInformation configurationObjectInformation = (ConfigurationObjectInformation) systemObjectInfo;
						// Da das Objekt neu in eine Datei geschrieben wird, muss keine Lücke eingefügt werden. Die
						// neue Speicherposition darf nicht an dem Objekt gespeichert werden, sondern erst wenn
						// die Reorganisation abgeschlossen ist.
						// Den Speicherort merken, damit er nachträglich gesetzt werden kann.
						final long newPosition = writeConfigurationObjectToFile(configurationObjectInformation, newConfigAreaFile, false, false);
						restructureInfo.rememberFilePosition(filePosition, newPosition);
					}
					else if(systemObjectInfo instanceof DynamicObjectInformation) {
						DynamicObjectInformation dynamicObjectInformation = (DynamicObjectInformation) systemObjectInfo;
						// Da das Objekt neu in eine Datei geschrieben wird, muss keine Lücke eingefügt werden.
						// Die neue Dateiposition darf nicht an dem Objekt gespeichert werden, sondern muss nachträglich gesetzt
						// werden.
						final long newPosition = writeDynamicObjectToFile(dynamicObjectInformation, newConfigAreaFile, false, false);
						restructureInfo.rememberFilePosition(filePosition, newPosition);
					}
					else {
						_debug.error("Unbekanntes Objekt " + systemObjectInfo.getClass());
					}
				}
			}
		}
	}

	/**
	 * Kopiert Objekte zwecks Restrukturierung von einer Datei in eine andere. Diese Methode kopiert alle Lücken mit
	 * und schreibt daher die exakt gleiche Menge an Bytes, die sie auch liest.
	 * @param fromFile von-Datei
	 * @param toFile ziel-Datei
	 * @param endPos bis zu welcher Position (exklusiv) aus on-datei gelesen werden soll
	 * @param newIdIndex ID-index, der gebildet werden soll
	 * @param newPidIndex PID-Index, der gebildet werden soll
	 * @param absoluteDynamicAreaOffset Absoluter Offset des NgDyn-Bereichs (für Pointer der Indizes)
	 * @param headerEnd  Absoluter Offset des Headerendes (für Pointer der Indizes)
	 * @throws IOException
	 */
	private void copyObjectsPreserveGaps(final BufferedRandomAccessFile fromFile, final BufferedRandomAccessFile toFile, final long endPos, final List<SortObject> newIdIndex, final Map<Integer, SortObjectPid> newPidIndex, final long absoluteDynamicAreaOffset, final long headerEnd) throws IOException {
		while(fromFile.getFilePointer() < endPos) {
			BinaryObject binaryObject = BinaryObject.fromDataInput(fromFile);
			if(binaryObject != null) {
				if(binaryObject.getObjectId() != 0) {
					addToIndizes(toFile, newIdIndex, newPidIndex, absoluteDynamicAreaOffset, binaryObject, headerEnd);
				}
				binaryObject.write(toFile);
			}
		}
	}

	/**
	 * Kopiert Objekte zwecks Restrukturierung von einer Datei in eine andere. Diese Methode ignoriert alle Lücken und sorgt so dafür,
	 * dass möglicherweise weniger Bytes geschreiben werden, als gelesen werden.
	 * @param fromFile von-Datei
	 * @param toFile ziel-Datei
	 * @param endPos bis zu welcher Position (exklusiv) aus on-datei gelesen werden soll
	 * @param newIdIndex ID-index, der gebildet werden soll
	 * @param newPidIndex PID-Index, der gebildet werden soll
	 * @param absoluteDynamicAreaOffset Absoluter Offset des NgDyn-Bereichs (für Pointer der Indizes)
	 * @param headerEnd  Absoluter Offset des Headerendes (für Pointer der Indizes)
	 * @throws IOException
	 */
	private void copyObjectsRemoveGaps(final BufferedRandomAccessFile fromFile, final BufferedRandomAccessFile toFile, final long endPos, final List<SortObject> newIdIndex, final Map<Integer, SortObjectPid> newPidIndex, final long absoluteDynamicAreaOffset, final long headerEnd) throws IOException {
		while(fromFile.getFilePointer() < endPos) {
			BinaryObject binaryObject = BinaryObject.fromDataInput(fromFile);
			if(binaryObject != null && binaryObject.getObjectId() != 0) {
				addToIndizes(toFile, newIdIndex, newPidIndex, absoluteDynamicAreaOffset, binaryObject, headerEnd);
				binaryObject.write(toFile);
			}
		}
	}

	/**
	 * Kopiert Objekte zwecks Restrukturierung von einer Datei in eine andere. Diese Methode ignoriert alle Lücken und alle zu löschenden
	 * dynamischen Objekte
	 * und sorgt so dafür, dass möglicherweise weniger Bytes geschreiben werden, als gelesen werden.
	 * @param fromFile von-Datei
	 * @param toFile ziel-Datei
	 * @param endPos bis zu welcher Position (exklusiv) aus on-datei gelesen werden soll
	 * @param newIdIndex ID-index, der gebildet werden soll
	 * @param newPidIndex PID-Index, der gebildet werden soll
	 * @param absoluteDynamicAreaOffset Absoluter Offset des NgDyn-Bereichs (für Pointer der Indizes)
	 * @param headerEnd  Absoluter Offset des Headerendes (für Pointer der Indizes)
	 * @throws IOException
	 */
	private void copyObjectsRemoveGapsAndDeleteObjects(final BufferedRandomAccessFile fromFile, final BufferedRandomAccessFile toFile, final long endPos, final List<SortObject> newIdIndex, final Map<Integer, SortObjectPid> newPidIndex, final long absoluteDynamicAreaOffset, final long headerEnd) throws IOException {
		while(fromFile.getFilePointer() < endPos) {
			BinaryObject binaryObject = BinaryObject.fromDataInput(fromFile);
			if(binaryObject != null && binaryObject.getObjectId() != 0) {
				if(_objectsPendingDeletion.contains(binaryObject.getObjectId())) {
					// Dieses Objekt soll gelöscht werden
					// Sicherheitshalber noch einmal überprüfen, dass das Objekt wirklich gelöscht werden darf
					// Es könnte sein, dass jemand an den Indexdateien rumgespielt hat und jetzt Konfigurationobjekte löschen will...
					if(!(binaryObject instanceof BinaryDynamicObject)){
						throw new IOException("Es sollte ein Konfigurationsobjekt endgültig gelöscht werden");
					}
					if(((BinaryDynamicObject) binaryObject).getFirstInvalid() == 0){
						throw new IOException("Es sollte ein gültiges Objekt endgültig gelöscht werden");
					}
					continue;
				}
				addToIndizes(toFile, newIdIndex, newPidIndex, absoluteDynamicAreaOffset, binaryObject, headerEnd);
				binaryObject.write(toFile);
			}
		}
	}

	private void addToIndizes(final BufferedRandomAccessFile toFile, final List<SortObject> newIdIndex, final Map<Integer, SortObjectPid> newPidIndex, final long absoluteDynamicAreaOffset, final BinaryObject binaryObject, final long headerEnd) {
		if(binaryObject instanceof BinaryDynamicObject) {
			addToIndizes(newIdIndex, newPidIndex, binaryObject, getRelativeFilePositionForInvalidDynamicObject(
					             absoluteDynamicAreaOffset, toFile.getFilePointer()
			             ));
		}
		else {
			addToIndizes(newIdIndex, newPidIndex, binaryObject, getRelativeFilePositionForInvalidConfigObject(headerEnd, toFile.getFilePointer()));
		}
	}


	public static long getRelativeFilePositionForInvalidConfigObject(final long headerEnd, final long absoluteFilePosition) {
		return (-1) * (absoluteFilePosition - headerEnd);
	}

	public static long getRelativeFilePositionForInvalidDynamicObject(final long dynamicAreaOffset, final long absoluteFilePosition) {
		return absoluteFilePosition - dynamicAreaOffset + 1;
	}

	public void addToIndizes(
			final List<SortObject> idIndex,
			final Map<Integer, SortObjectPid> pidIndex,
			final BinaryObject objectInformation,
			final long filePosition) {
		idIndex.add(new SortObject(filePosition, objectInformation.getObjectId()));

		// Pid Index
		if(pidIndex.containsKey(objectInformation.getPidHashCode())) {
			final SortObjectPid filePositions = pidIndex.get(objectInformation.getPidHashCode());
			filePositions.putFilePosition(filePosition);
		}
		else {
			// Es gibt noch kein Objekt, also einfügen
			SortObjectPid filePositions = new SortObjectPid(objectInformation.getPidHashCode());
			filePositions.putFilePosition(filePosition);
			pidIndex.put(objectInformation.getPidHashCode(), filePositions);
		}
	}

	public void addToIndizes(
			final List<SortObject> idIndex,
			final Map<Integer, SortObjectPid> pidIndex,
			final SystemObjectInformationInterface objectInformation,
			final long filePosition) {
		idIndex.add(new SortObject(filePosition, objectInformation.getID()));

		// Pid Index
		int hashCode = objectInformation.getPid().hashCode();
		if(pidIndex.containsKey(hashCode)) {
			final SortObjectPid filePositions = pidIndex.get(hashCode);
			filePositions.putFilePosition(filePosition);
		}
		else {
			// Es gibt noch kein Objekt, also einfügen
			SortObjectPid filePositions = new SortObjectPid(hashCode);
			filePositions.putFilePosition(filePosition);
			pidIndex.put(hashCode, filePositions);
		}
	}

	/**
	 * Diese Methode sucht zu einem Value alle Objekte, die als ungültig markiert sind. Der Parameter value wird dabei unterschiedlich interpretiert und zwar in
	 * Abhängigkeit vom Parameter searchId. Ist searchId = true, so wird der Parameter value als Id eines Objekts betrachtet. Die Methode sucht dann ein als
	 * ungültig markiertes Objekt, dessen Id gleich dem übergebenen Parameter value entspricht. Ist der Parameter searchId = false, so wird der Wert als HashCode
	 * einer Pid interpretiert. Da eine Pid nicht bijektiv auf ein Integer abgebildet werden kann, muss die Liste später noch überprüft werden, ob die Objekte auch
	 * wirklich mit der Pid übereinstimmen.
	 *
	 * @param value    Id oder Pid, nach der die Objekte gesucht werden sollen
	 * @param searchId true = value wird als Id interpretiert; false = value wird als HashCode einer Pid interpretiert (siehe Methodenbeschreibung!)
	 *
	 * @return Liste, die die geforderten Objekte enthält. Bei einer Anfrage nach Id, ist nur ein Element in dieser Liste. Es wird <code>null</code> zurückgegeben,
	 *         falls kein Objekt gefunden werden konnte
	 *
	 * @throws IllegalStateException Zu einer Id wurden zwei Elemente gefunden
	 */
	private List<SystemObjectInformationInterface> binarySearch(long value, boolean searchId) throws NoSuchVersionException, IOException {
		// Wenn reorganisiert wird, dann muss gewartet werden, weil die Datei umgestellt wird
		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {

				BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "r");

				// Try/finally für close der Datei
				try {
					return binarySearch(file, value, searchId);
				}
				finally {
					file.close();
				}
			} // synch file
		} // synch restructereLock
	}

	private List<SystemObjectInformationInterface> binarySearch(final BufferedRandomAccessFile file, final long value, final boolean searchId) throws NoSuchVersionException, IOException {
		// Liste, die alle Objekte speichert, die dem geforderten value entsprechen
		final List<SystemObjectInformationInterface> searchResult = new ArrayList<SystemObjectInformationInterface>();

		// Die gesuchten Objekte können sich in der Mischmenge oder in den nGa Bereichen oder im dyn. nGa Bereich befinden.

		// mit Id suchen
		if(searchId) {
			final ObjectReference objectReference;
			final boolean positionFound;
			synchronized(_oldObjectsId) {
				if(_oldObjectsId.containsKey(value)) {
					objectReference = _oldObjectsId.get(value);
					positionFound = true;
				}
				else {
					objectReference = null;
					positionFound = false;
				}
			}

			if(positionFound) {
				// Objekt befand sich in der Mischmenge.
				// Im "else-Fall" muss das Objekt in den nGa-Bereichen, dem dyn nGa-Bereich gesucht werden
				searchResult.add(getSystemObjectInfo(objectReference, file));
				return searchResult;
			}
		}
		else {
			// Es soll nach einer Pid gesucht werden, diese kann sich im Mischbreich befinden
			// und in den nGa-Bereichen, dem dyn nGa-Bereich

			// Mischbreich prüfen

			final Set<FilePointer> filePositions;
			final boolean positionFound;
			synchronized(_oldObjectsPid) {
				if(_oldObjectsPid.containsKey((int)value)) {
					// Ein Wert ist vorhanden
					filePositions = _oldObjectsPid.get((int)value);
					positionFound = true;
				}
				else {
					// Es gibt keine Objekte mit der Pid
					positionFound = false;
					filePositions = null;
				}

				if(positionFound) {
					// Werte gefunden
					for(FilePointer filePosition : filePositions) {
						// Objekt laden
						searchResult.add(loadObjectFromFile(file, filePosition, null));
					}
				}
			}
		}

		// An dieser Stelle wurde Mischobjektmenge abgearbeitet. Falls nach Id gesucht wurde, wurde diese nicht
		// gefunden, sonst wäre die Methode bereits mit "return" verlassen worden. Falls nach der Pid gefragt wurde
		// so kann in der Ergebnisliste bereits ein Wert vorhanden sein. Es kann aber sein, dass sich in den nGa-Bereichen
		// dem nGa-Bereich weitere Objekte befinden.

		// Nun muss entweder der Id oder der Pid Index benutzt werden

		// Als erstes muss geprüft werden, ob es den benötigten Index überhaupt gibt. Dieser Fall kann
		// auftreten, wenn es noch zu keiner Reorganisation gekommen ist und es somit keine
		// nGa Bereiche bzw. den dyn. nGa Bereich noch nicht gibt.
		// Falls es keine Reorganisation gegeben hat, dann stehen die Elemente im Speicher und wurden bereits
		// gefunden, siehe oben.

		// Wird true, falls der benötigte Index(Id oder Pid) nach einer Reorganisation angelegt wurde
		boolean indexExist = false;

		if(searchId) {
			// Der Index nach Id muss vorhanden sein
			if(_startIdIndex < _startPidHashCodeIndex) {
				// Der start des Id-Index ist kleiner als der Start des Pid-Index, also müssen
				// in diesem Zwischenraum Daten stehen.
				indexExist = true;
			}
		}
		else {
			if(_startPidHashCodeIndex < _startMixedSet) {
				// Nach dem Pid Index kommt die Mischmenge, im Zwischenraum müsen also
				// Daten stehen
				indexExist = true;
			}
		}

		// Die Suche macht nur Sinn, wenn der Index auch besteht.
		if(indexExist) {
			// Erste Eintrag im Index (Id oder Pid)
			long minimum;
			// In dieser Variablen steht später die Dateipostion an der das Paar "Id, relative Dateipostion" (Id Anfrage) steht
			long middle = -1;

			// In dieser Variablen steht später die Dateipostion an der das Paar "Pid-HashCode, relative Dateipostion" (Pid Anfrage) steht.
			long helperPid = -1;

			// Letzter Eintrag im Index (Id oder Pid)
			long maximum;

			// wird true, wenn der Parameter value gefunden wurde
			boolean resultFound = false;

			if(searchId) {
				// Id
				minimum = _startIdIndex + _headerEnd;
				// Ende des Id-Index
				maximum = (_startPidHashCodeIndex + _headerEnd);
			}
			else {
				// Pid Index als Start
				minimum = _startPidHashCodeIndex + _headerEnd;
				// Ende des Pid-Index
				maximum = (_startMixedSet + _headerEnd);
			}

//						printIdIndex();

			// Der Algorithmus sucht nach dem Parameter value. Sind mehrere Objekte vorhanden, die dem value entsprechen
			// wird solange weitergesucht, bis das erste Objekt gefunden wurde, das dem value entspricht.
			// Alle Objekte, die nach dem Objekt gespeichert wurden, entsprechem dem value oder sind größer.
			// Dies ist für die Pid wichtig, da für jedes Objekt mit der gleichen Pid ein Eintrag gemacht wird.
			while(minimum < maximum) {
//							System.out.println("Start-while: min: " + minimum + " middle: " + middle + " max: " + maximum);

				// Speichert den Wert, des aus dem Index (Id oder Pid) geladen wurden
				final long valueFromIndex;
				// Neue Mitte ausrechnen. Dies entspricht einer Dateiposition.
				// Der nächste Wert setzt sich im Id Index anderes zusammen, als bei einem
				// Pid Index. Nach dem die Mitte gefunden wurde, wird der Wert aus dem entsprechenden Index
				// eingelesen.
				if(searchId) {
					// Wieviele Paare (Long,Long (Id,Relative Position) sind zwischem Min und Max.
					final long numberOfPairs = (maximum - minimum) / 16;

					// Die hälfte der Paare und dann pro paar 16 Byte (2 Longs) dazurechnen. Dieser Wert muss zum Min.
					// gerechnet werden um die Mitte zu erhalten.
					final long newMinOffset = (numberOfPairs / 2) * 16;
					middle = minimum + newMinOffset;
					// Wert aus dem Id Index einlesen
					file.seek(middle);
					valueFromIndex = file.readLong();
				}
				else {
					// Wieviele Paare (int,Long (HashCode der Pid,Relative Position) sind zwischem Min und Max.
					final long numberOfPairs = (maximum - minimum) / 12;

					// Die hälfte der Paare und dann pro paar 12 Byte (ein Integer und ein Long) dazurechnen. Dieser Wert muss zum Min.
					// gerechnet werden um die Mitte zu erhalten.
					final long newMinOffset = (numberOfPairs / 2) * 12;
					middle = minimum + newMinOffset;
					// Wert aus dem Pid Index einlesen
					file.seek(middle);
					valueFromIndex = file.readInt();
				}

				// Die Id oder der HashCode der Pid steht nun zur Verfügung
//							System.out.println("Binäre Suche: middle: " + valueFromIndex + " gesucht: " + value);
				if(value <= valueFromIndex) {
					maximum = middle;
					if(value == valueFromIndex) {

						// Der Wert wurde gefunden. wird nur die Id gesucht, dann endet die Suche.
						// Bei der Suche nach der Pid muss weiter gesucht werden, da es mehrere
						// Objekte zu einer Pid geben kann.
						resultFound = true;
//									System.out.println("*****Binäre suche findet das gesuchte Objekt*****");

						if(searchId) {
							// Id suche endet, die Position im Index steht in middle
							break;
						}
						else {
							// Bei Pid suchen kann es mehrer Pids geben, es wird die aktuelle
							// Position im Index gespeichert, danach wird weitergesucht.
							// Wird kein Objekt mehr gefunden, dann steht die Ergebnisposition
							// in dieser Variablen.
							helperPid = middle;
						}
					}
				}
				else {
					// das nächste Element ist ein Paar, das entweder aus Long,Long oder aus Int,Long besteht.
					if(searchId) {
						minimum = middle + 8 + 8;
					}
					else {
						minimum = middle + 4 + 8;
					}
				}
//							System.out.println("End-while min: " + minimum + " middle: " + middle + " max: " + maximum);
			} // while

			if(resultFound) {

				// Es wurde mindestens ein Objekt gefunden (bei der Id darf es nur ein Objekt sein, bei
				// der Pid können es mehrere sein)
				if(searchId) {

					// Den filePointer auf die Stelle setzen, wo das erste Objekt steht
					file.seek(middle);

					// Id, es gibt nur einen Wert

					// Id aus dem Index einlesen
					final long checkId = file.readLong();
					// relative Dateiposition des Objekts
					final long position = file.readLong();
					assert checkId == value : "geforderte Id: " + value + " gefundene Id: " + checkId;

					SystemObjectInformationInterface idObject = loadObjectFromFile(file, FilePointer.fromRelativePosition(position, this), null);
					searchResult.add(idObject);
					assert idObject.getID() == checkId : "Dateiposition: " + position + " Datei: " + _configAreaFile + " gesuchte Id: " + value
							+ " gefundene Id: " + idObject.getID();
					// Liste mit dem Objekt zurückgeben
					return searchResult;
				}
				else {

					// Auf den richtigen Index springen, dieser steht im Gegensatz zu Id-Suche in dieser Variablen
					file.seek(helperPid);

					// Es werden Objekte für die Pid benötigt. Für eine Pid kann es mehrer Objekte geben.
					// Es müssen alle Objekte aus dem Pid-Index eingelesen werden, deren Pid-HashCode mit
					// dem übergebenen Code übereinstimmen.

					// wird true, wenn das letzte Objekt aus dem Pid-Index geladen wurden, dessen hashCode stimmt
					boolean lastPidObjectRead = false;

					// Solange noch die richtigen Pids gelesen werden
					// und
					// der filePointer sich noch im Pid-Index befindet
					while((!lastPidObjectRead) && (file.getFilePointer() < (_startMixedSet + _headerEnd))) {
						final int pidHashCode = file.readInt();
						final long position = file.readLong();

						long oldPos = file.position();

						if(pidHashCode == value) {
							final SystemObjectInformationInterface pidObject = loadObjectFromFile(file, FilePointer.fromRelativePosition(position, this), null);

							file.position(oldPos);

							assert pidObject.getPid().hashCode() == value : "Dateiposition: " + position + " Datei: " + _configAreaFile
									+ " gesuchter Pid-HashCode: " + value + " gefundener Pdi-HashCode: "
									+ pidObject.getPid().hashCode() + " Pid des Objects: "
									+ pidObject.getPid();
							searchResult.add(pidObject);
						}
						else {
							// Der hashCode der Pids unterscheidet sich, also wurden alle Pids
							// gelesen.
							lastPidObjectRead = true;
						}
					}
				}
			} // if(resultFound)
		} // if(indexExist)

		if(searchResult.size() > 0) {
			return searchResult;
		}
		else {
			// Es konnte kein Objekt gefunden werden
			return null;
		}
	}


	/**
	 * Gibt die Pid des Konfigurationsbereichs zurück.
	 *
	 * @return Pid des Konfigurationsbereichs
	 */
	public String getConfigAreaPid() {
		if(_configurationAreaPid != null) {
			return _configurationAreaPid;
		}
		else {
			throw new IllegalStateException("Die Pid des Konifurationsbereichs wurde noch nicht eingelesen");
		}
	}

	@Override
	public void markObjectsForDeletion(final List<Long> objectsToDelete) {
		synchronized(_objectsPendingDeletion) {
			for(final Long dynamicObjectInfo : objectsToDelete) {
				if(_objectsLockedForDeletion.contains(dynamicObjectInfo)) {
					// Objekt kann endgültig gelöscht werden
					_objectsPendingDeletion.add(dynamicObjectInfo);
				}
			}
			_objectsLockedForDeletion.clear();
			for(Long dynamicObjectInfo : objectsToDelete) {
				_objectsLockedForDeletion.add(dynamicObjectInfo);
			}
		}
	}


	/**
	 * Löscht die für das Löschen vorgemerkten dynamischen Objekte vollständig und permanent. Die übergebenen Objekte sollten bereits einige Zeit
	 * gelöscht sein und sich im NgaDyn-Block befinden. Es sollte keine Referenzen auf diese Objekte geben.
	 *
	 * Da sich dadurch die Positionen von nicht-Gültigen Objekten in der Datei ändern können,
	 * können Objekte im Speicher durch diesen Vorgang unbrauchbar werden. Daher muss nach dem Aufruf dieser Methode
	 * das Datenmodell neu initialisiert werden oder alternativ die Methode zu einem Zeitpunkt aufgerufen werden, wenn noch keine
	 * alten Objekte im Speicher sind.
	 */
	public void deleteDynamicObjectsPermanently(){
		// _restructureLock jetzt schon belegen, damit Datenmodell konsistent bleibt
		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {
				synchronized(_objectsPendingDeletion) {
					if (_objectsPendingDeletion.size() > 0) {
						_debug.info("Lösche " + _objectsPendingDeletion.size() + " alte dynamische Objekte aus Konfigurationsbereich", getConfigAreaPid());
						try {
							restructure(RestructureMode.DeleteObjectsPermanently);
							_objectsPendingDeletion.clear();
						}
						catch(IOException e) {
							_debug.error("Fehler beim endgültigen Löschen im Bereich " + _configAreaFile, e);
						}
					}
				}
			}
		}
	}


	@Override
	public boolean referenceAllowed(final SystemObjectInformationInterface systemObjectInfo) {
		synchronized(_objectsPendingDeletion) {
			long id = systemObjectInfo.getID();
			if(_objectsPendingDeletion.contains(id) || _objectsLockedForDeletion.contains(id)) {
				return false;
			}
			return true;
		}
	}

	@Override
	public long getHeaderEnd() {
		return _headerEnd;
	}

	@Override
	public long getStartOldDynamicObjects() {
		return _startOldDynamicObjects;
	}

	@Override
	public long getStartIdIndex() {
		return _startIdIndex;
	}

	@Override
	public long getStartPidHashCodeIndex() {
		return _startPidHashCodeIndex;
	}

	@Override
	public long getStartMixedSet() {
		return _startMixedSet;
	}

	public void updateHeaderPositions(final HeaderInfo headerInfo) {
		_headerEnd = headerInfo.getHeaderEnd();
		_startOldDynamicObjects = headerInfo.getStartOldDynamicObjects();
		_startIdIndex = headerInfo.getStartIdIndex();
		_startPidHashCodeIndex = headerInfo.getStartPidHashCodeIndex();
		_startMixedSet = headerInfo.getStartMixedSet();
	}

//	public void rememberFilePointer(final FilePointer pointer) {
//		_filePointers.add(pointer);
//	}

	/**
	 * Wird aufgerufen, wenn ein dynamisches Objekt ungültig wird.
	 * Falls das Objekt bereits
	 * @param object
	 */
	public void writeInvalidTime(final DynamicObjectInformation object) {
		FilePointer filePosition = object.getLastFilePosition();
		try {
			synchronized(_configAreaFile) {
				if(filePosition == null){
					writeDynamicObject(object);
				}
				else {
					final BufferedRandomAccessFile file = new BufferedRandomAccessFile(_configAreaFile, "rw");
					try {
						// Größe(4) + ID(8) + Hashcode(4) + Typ(8) + Kennung(1)
						file.seek(filePosition.getAbsoluteFilePosition() + 4 + 8 + 4 + 8 + 1);
						file.writeLong(object.getFirstInvalidTime());
					}
					finally {
						file.close();
					}
				}
			}
		}
		catch(IOException e) {
			_debug.error(
					"Das Objekt mit der Id " + object.getID() + " Pid " + object.getPid() + " konnte nicht in Datei " + _configAreaFile + " gespeichert werden"
			);
		}
	}

	/**
	 * Führt eine volle Restrukturierugn aus
	 * @return true falls erfolgreich sonst false
	 * @deprecated Bitte Mode-Parameter übergeben um die Art der Restrukturierung anzugeben.
	 */
	@Deprecated
	public boolean restructure() {
		try {
			restructure(RestructureMode.FullRestructure);
			return true;
		}
		catch(IOException e) {
			// Fängt alle Fehler ab, kommt es zu einem Fehler, wird die Reorganisation unterbrochen und der
			// Ursprungszustand bleibt erhalten
			_debug.error(
					"Fehler bei der Reorganisation " + _configAreaFile + " Pid " + _configurationAreaPid
							+ " . Die Reorganisation wurde abgebrochen, es wird ohne Änderung in der Ursprungsdate normal weitergearbeitet.", e
			);
			return false;
		}
	}

	/**
	 * Diese Klasse wird mehrfach mit unterschiedlichen Aufgaben benutzt. Es wird aber immer in einem Array benutzt um eine Sortierung des Array nach dem Value des
	 * Objekts zu erzeugen. Der Value kann dabei ein Zeitstempel oder eine Id sein. <br> Diese Klasse speichert den Zeitstempel, wann ein dynamisches Objekt
	 * ungültig geworden ist und die Position an dem das Objekt in der Datei gespeichert wurde. <br> Diese Klasse speichert die Id und die Dateiposition von
	 * beliebigen Objekten.
	 */
	private final static class SortObject implements Comparable<SortObject> {

		// Positionsangabe (kann relativ aber auch absolut sein)
		private final long _filePosition;

		private final long _value;

		/**
		 * @param filePosition Dateiposition (relativ)
		 * @param value        Wert, nach dem auch sortiert wird (Id, Zeitstempel, usw)
		 */
		public SortObject(long filePosition, long value) {
			_filePosition = filePosition;
			_value = value;
		}

		public long getFilePosition() {
			return _filePosition;
		}

		public long getValue() {
			return _value;
		}

		@Override
		public int compareTo(SortObject sortObject) {
			if(_value < sortObject.getValue()) {
				return -1;
			}
			else if(_value > sortObject.getValue()) {
				return 1;
			}
			else {
				return 0;
			}
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			final SortObject that = (SortObject) o;

			if(_filePosition != that._filePosition) return false;
			if(_value != that._value) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = (int) (_filePosition ^ (_filePosition >>> 32));
			result = 31 * result + (int) (_value ^ (_value >>> 32));
			return result;
		}
	}

	private final static class SortObjectPid implements Comparable<SortObjectPid> {

		/** relative Dateipositionen der Objekte, deren Pid auf _pidHashCode abgebildet werden konnten */
		private final List<Long> _filePositions = new ArrayList<Long>();

		/** HashCode, der die Pid abbildet */
		private final int _pidHashCode;

		/** @param pidHashCode HashCode einer Pid, dieser Wert wird bei einer Sortierung als Kriterium gewählt */
		public SortObjectPid(int pidHashCode) {
			_pidHashCode = pidHashCode;
		}

		/**
		 * Speichert zu einer Pid die Dateipostion des Objekts. Die neue Dateiposition stellt den aktuellen Speicherort des Objekts dar.
		 *
		 * @param newFilePosition aktuelle Position in der Datei, an der das Objekt gespeichert ist
		 */
		public void putFilePosition(long newFilePosition) {
			synchronized(_filePositions) {
				_filePositions.add(newFilePosition);
			}
		}

		public List<Long> getFilePositions() {
			synchronized(_filePositions) {
				return _filePositions;
			}
		}

		public int getPidHashCode() {
			return _pidHashCode;
		}

		@Override
		public int compareTo(SortObjectPid o) {

			if(_pidHashCode < o.getPidHashCode()) {
				return -1;
			}
			else if(_pidHashCode > o.getPidHashCode()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	/**
	 * Ein Objekt, das als "ungültig" markiert ist enthält den HashCode der Pid und die Id des Objekts. Damit das Objekt später, falls nötig, geladen werden kann,
	 * befindet sich ebenfalls die Dateiposition und ein Objekt zum einladen der Daten am Objekt
	 */
	final static class OldObject {


		private final long _id;

		private final int _pidHashCode;

		/** Wo in der Datei befindet sich das Objekt */
		private final FilePointer _filePosition;

		/** Objekt zum laden des Objekts */
		private final ConfigAreaFile _configAreaFile;

		/**
		 * @param id
		 * @param pidHashCode
		 * @param filePosition   Position in der Datei, an der das Objekt gespeichert wurde
		 * @param configAreaFile Objekt, über das das als "ungültig" markierte Objekt geladen werden kann
		 */
		public OldObject(long id, int pidHashCode, FilePointer filePosition, ConfigAreaFile configAreaFile) {
			_id = id;
			_pidHashCode = pidHashCode;
			_filePosition = filePosition;
			_configAreaFile = configAreaFile;
		}

		public long getId() {
			return _id;
		}

		public int getPidHashCode() {
			return _pidHashCode;
		}

		public FilePointer getFilePosition() {
			return _filePosition;
		}

		public ConfigAreaFile getConfigAreaFile() {
			return _configAreaFile;
		}

		public String toString() {
			final StringBuilder out = new StringBuilder();
			out.append("Ungültig markiertes Objekt:" + "\n");
			out.append("Id : ").append(getId()).append("\n");
			out.append("Pid, hashCode: ").append(getPidHashCode()).append("\n");
			// out.append("Datei: " + getConfigAreaFile() + "\n");
			out.append("Position in der Datei: ").append(getFilePosition()).append("\n");
			if(_configAreaFile != null) {
				out.append("Objekt, ConfigAreaFile: ").append(_configAreaFile).append("\n");
			}
			else {
				out.append("Objekt, ConfigAreaFile: null" + "\n");
			}
			return out.toString();
		}
	}

	/**
	 * Diese Klasse speichert die Dateiposition eines Blocks, der Konfigurationsobjekte enthält, die ungültig sind und durch eine Reorganisation verschoben wurden
	 * und sich nicht mehr in der Mischobjektmenge befinden. Als zusätzliche Informatione speicher die Klasse einen Zeitstempel, der zu dem Block gehört.
	 */
	public final static class OldBlockInformations {


		private final long _filePosition;

		private final long _timeStamp;


		public OldBlockInformations(long filePosition, long timeStamp) {
			_filePosition = filePosition;
			_timeStamp = timeStamp;
		}


		public long getFilePosition() {
			return _filePosition;
		}

		public long getTimeStamp() {
			return _timeStamp;
		}

		public String toString() {
			return "OldBlockInfo: Position: " + _filePosition + " Zeitstempel: " + _timeStamp;
		}
	}

	/**
	 * Diese Klasse speichert alle Inforamtionen, die benötigt werden um ein Objekt, das sich in der Mischmenge befindet aber nicht komplett in den Speicher
	 * geladen wurde, eventuell nachzuladen falls es gebraucht wird. Bei transienten Objekte wird das gesamte Objekt gespeichert.
	 * <p>
	 * Bei der Suche nach der Type Id wird bei alten Objekten immer ein Zeitbereich angegeben, in dem das Objekt gültig gewesen sein muss. Also wird entweder der
	 * Zeitpunkt an dem das Objekt gültig wurde gespeichert (bei dynamischen Objekten) oder aber die Version mit der das Objekt gültig wurde.
	 * <p>
	 * Es wird ebenfalls ein Boolean gespeichert, der angibt ob das Objekt ein Konfigurationsobjekt oder ein dynamisches Objekt ist.
	 * <p>
	 * Als letztes wird die absolute Position des Objekts in der Datei gespeichert, damit es falls nötig geladen werden kann.
	 */
	private final static class OldObjectTypeIdInfo {

		/**
		 * Bei einem Konfigurationsobjekt steht hier die Version, mit der das Objekt gültig wurde. Bei einem dynamischen Objekt ist es der Zeitpunkt, an dem das
		 * Objekt gültig wurde.
		 */
		private final long _firstValid;

		/** Zeitpunkt/Version, an dem das Objekt ungültig werden soll/geworden ist (0 bedeutet, dass dieser Zeitpunkt unbekannt ist) */
		private final long _firstInvalid;

		/** true = es handelt sich um ein Konfigurationsobjekt; false = das Objekt ist ein dynamisches Objekt */
		private final boolean _configurationObject;

		private final ObjectReference _objectReference;

		/**
		 * @param firstValid           Bei Konfigurationsobjekten die Version, mit der das Objekt gültig wurde. Bei einem dynamischen Objekt, der Zeitpunkt an dem das
		 *                             Objekt gültig wurde.
		 * @param firstInvalidVersion  Bei Konfigurationsobjekten die Version, mit der das Objekt ungültig wurde. Bei einem dynamischen Objekt, der Zeitpunkt an dem
		 *                             das Objekt ungültig wurde. Der Wert 0 bedeutet in beiden Fällen, dass die Version, in der das Objekt ungültig wird, noch
		 *                             unbekannt ist.
		 * @param configurationObject  Variable wird benötigt um den Paramter firstValid auszuwerten. true = Es ist ein Konfigurationsobjekt; false = es ist ein
		 *                             dynamisches Objekt
		 * @param objectReference Objekt, mit dem das Objekt aus der Datei rekonstruiert werden kann, bzw direkt aus dem Speicher geholt wird.
		 */
		public OldObjectTypeIdInfo(long firstValid, long firstInvalidVersion, boolean configurationObject, ObjectReference objectReference) {
			if(firstValid > 0) {
				_firstValid = firstValid;
			}
			else {
				throw new IllegalArgumentException(
						"OldObjectTypeIdInfo, FirstValidVersion: " + firstValid + " FirstInvalidVersion " + firstInvalidVersion + " Konfiguriernend "
								+ configurationObject + " betroffenes Objekt: " + objectReference
				);
			}

			_firstInvalid = firstInvalidVersion;
			_configurationObject = configurationObject;
			_objectReference = objectReference;
		}

		public long getFirstValid() {
			return _firstValid;
		}

		public long getFirstInvalid() {
			return _firstInvalid;
		}

		public boolean isConfigurationObject() {
			return _configurationObject;
		}

		public ObjectReference getObjectReference() {
			return _objectReference;
		}
	}

	/**
	 * Lädt das Objekt aus der Datei oder holt es aus dem Speicher (transiente Objekte).
	 *
	 * @return s.o.
	 *
	 * @throws NoSuchVersionException
	 * @throws IOException
	 * @param file BufferedFile, das die Konfigurationsdatei enthält. Falls null wird eine neue Datei geöffnet.
	 *                   (Performanceproblem beim wiederholten Laden von vielen Objekten!)
	 *
	 */
	private SystemObjectInformationInterface getSystemObjectInfo(final ObjectReference ref, final BufferedRandomAccessFile file) throws NoSuchVersionException, IOException {
		if(ref instanceof TransientObjectReference) {
			TransientObjectReference transientObjectReference = (TransientObjectReference) ref;
			return transientObjectReference._dynamicObjectInfo;
		}
		if(ref instanceof FilePointer) {
			FilePointer filePointer = (FilePointer) ref;
			if(file == null){
				return loadObjectFromFile(filePointer);
			}
			return loadObjectFromFile(file, filePointer, null);
		}
		throw new IllegalArgumentException();
	}


	/** Diese Klasse stellt einen Iterator zur Verfügung, der alle Objekte eines Konfigurationsbereichs zur Verfügung stellt. */
	private final class FileIterator implements Iterator<SystemObjectInformationInterface> {

		/**
		 * Speichert die relative Position des Datensatzes, der als nächstes aus der Datei gelesen werden muss. Die absolute Position setzt sich auf der relativen
		 * Position + _headerEnd zusammen. Der Wert -1 bedeutet, dass es keine weiteren Objekte mehr gibt, die in der Datei gespeichert sind.
		 */
		private long _relativePosition = -1;

		/** absolute Position eines als "ungültig" markierten Objekts, das sich in der Mischmenge befindet. */
		private final Iterator<ObjectReference> _oldObjectsIterator = _oldObjectsId.values().iterator();

		/** Iterator über alle aktuellen Objekte, die sich im Speicher befinden */
		private final Iterator<SystemObjectInformationInterface> _actualObjectsIterator = _actualObjects.values().iterator();

		/** Iterator über alle zukünftig aktuellen Objekte, die sich im Speicher befinden */
		private final Iterator<SystemObjectInformationInterface> _newObjectsIterator = _newObjects.values().iterator();

		public FileIterator() {
			if(_startIdIndex > 0) {
				// Da der Id-Index nicht direkt nach dem Header beginnt, muss zwischem dem Header und dem Index
				// ein nGa und/oder dynamischer nGa Bereich zu finden sein
				_relativePosition = 0;
			}
		}

		@Override
		synchronized public boolean hasNext() {
			// Gibt es noch Elemente, die aus der Datei geladen werden können
			if(_relativePosition > 0) return true;
			// Gibt es noch Elemente, die ungültig sind und in der Mischmenge vorhanden sind
			if(_oldObjectsIterator.hasNext()) return true;
			// Gibt es noch aktuelle Objekte, die zurückgegeben werden können
			if(_actualObjectsIterator.hasNext()) return true;
			// Gibt es noch zukünftig aktuelle Objekte, die zurückgegeben werden können
			if(_newObjectsIterator.hasNext()) return true;

			// Es wurde kein true zurückgegeben, also wurden alle Objekte zurückgegeben
			return false;
		}

		@Override
		synchronized public SystemObjectInformationInterface next() {
			synchronized(_restructureLock) {
				synchronized(_configAreaFile) {

					if(_relativePosition >= 0) {
						// Es gibt noch Objekte, die aus der Datei geladen werden können.
						// Wenn das Objekt geladen wird, wird auch _relativePosition durch den setter gesetzt.

						try {
							return loadObjectFromFile(FilePointer.fromAbsolutePosition(_relativePosition + _headerEnd, ConfigAreaFile.this), this);
						}
						catch(Exception e) {
							throw new RuntimeException(e);
						}
					}
					// Aus der Datei wurden alle Objekte geladen, jetzt die Objekte aus dem Speicher.
					if(_oldObjectsIterator.hasNext()) {
						try {
							ObjectReference idReference = _oldObjectsIterator.next();
							return getSystemObjectInfo(idReference, null);
						}
						catch(Exception e) {
							throw new RuntimeException(e);
						}
					}

					if(_actualObjectsIterator.hasNext()) {
						return _actualObjectsIterator.next();
					}

					if(_newObjectsIterator.hasNext()) {
						return _newObjectsIterator.next();
					}

					// Es wurden alle Objekte zurückgegeben
					throw new NoSuchElementException();
				}
			}
		}

		@Override
		synchronized public void remove() {
			throw new UnsupportedOperationException();
		}

		synchronized public void setRelativePosition(long relativePosition) {
			_relativePosition = relativePosition;
		}
	}

}
