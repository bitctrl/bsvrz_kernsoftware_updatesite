/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.filelock.FileLock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Diese Klasse stellt eine Konfigurationsbereichsdatei dar und speichert alle Objekte des Bereichs mit Historie.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision: 9664 $ / $Date: 2011-11-07 09:34:21 +0100 (Mon, 07 Nov 2011) $ / ($Author: jh $)
 */
public class ConfigAreaFile implements ConfigurationAreaFile {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/** Wo befindet sich die Datei */
	private final File _configAreaFile;

	/** aktive Version, diese wird durch einen Neustart der Konfiguration gesetzt (durch den Konstruktor) */
	private final short _activeVersion;

	/** aktive Version, die aus der Datei gelesen wurde. */
	private short _activeVersionFile = -1;  

	/** Speichert die Version, die als n�chstes g�ltig wird. Die Variable wird mit einer Setter gesetzt. */
	private short _nextActiveVersion = -1;

	/** n�chste aktive Version, die aus der Datei eingelesen wurde. */
	private short _nextActiveVersionFile = -1; 

	/** Mit dieser Version werden alle Daten in der Daten serialisiert. */
	private int _serializerVersion = -1;

	/**
	 * Speichert, wo der Header endet. Diese Information wird gebraucht, da alle Angaben zur Position in der Datei relativ zum Ende des Header sind. Addiert man
	 * die beiden Werte, so erh�lt man die abslute Position in der Datei.
	 * <p/>
	 * Die gespeicherte Position zeigt auf den ersten Wert, der nach dem Header gespeichert ist.
	 */
	private long _headerEnd;

	/**
	 * Speichert zu jedem Block, in dem ung�ltige Objekte gespeichert sind, ein Objekt ab, das die Position des Blocks (relativ zum Headerende) in der Datei
	 * enth�lt und einen Zeitstempel (wann wurde diese Version g�ltig), der sich auf den Block bezieht. Als Key dient die Versionsnummer. Sind keine Elemente
	 * vorhanden, so wurde noch kein Block angelegt. Wenn die Datei neu erzeugt wird, ist dies der Fall. Der erste Block entsteht nach der ersten Reorganisation,
	 * nach dem die aktuelle Version erh�ht wurde.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 * <p/>
	 * Diese Informationen werden f�r die Reorganisation gebraucht, bei einem Neustart wird aus diesen Informationen die Map
	 * _configurationAuthorityVersionActivationTime rekonstruiert.
	 * <p/>
	 * Es sind zwei Maps n�tig, da die _oldObjectBlocks-Map speichert wie weit die Reorganisation gekommen ist und die Map
	 * _configurationAuthorityVersionActivationTime wird im Konstruktor gesetzt und wird dann die n�chste g�ltige Vesion enthalten und ist dadurch um mindestens
	 * eine Version gr��er als jede Version die _oldObjectBlocks speichert.
	 * <p/>
	 * Findet kein Versionswechsel statt, sind die Version/Zeitstempel Paare in _oldObjectBlocks und _configurationAuthorityVersionActivationTime identisch.
	 */
	private final Map<Short, OldBlockInformations> _oldObjectBlocks;

	/**
	 * synchronisierte Map, die den Aktivierungszeitpunkt jeder Version speichert. Als Key dient die Version, als value wird der Zeitpunkt zur�ckgegbeen, an dem
	 * das Konfigurationsverantwortliche die Version aktiviert hat. Siehe auch Kommentar _oldObjectBlocks.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 */
	private final Map<Short, Long> _configurationAuthorityVersionActivationTime;

	/**
	 * Array mit den globalen Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 * Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der n�chsten Aktivierung eingetragen. Der gr��te verwendete Index entspricht
	 * der gr��ten aktivierten Version.
	 */
	private long[] _globalActivationTimes;

	/**
	 * Speichert die n�chste ung�ltige Version. Es ist die gr��te Version aus _oldObjectBlocks plus 1. Ist noch kein Block vorhanden, so ist die "n�chste"
	 * ung�ltige Version, Version 2 (Version 1 ist die erste m�gliche g�ltige Version)
	 */
	private short _nextInvalidBlockVersion = 2;

	/** relative Position des Blocks, der alle ung�ltigen dynamischen Objekte enth�lt, die nach einer Reorganisation in diesem Block geschrieben wurden. */
	private long _startOldDynamicObjects = 0;

	/** relative Position des Index, der die Id�s verwaltet. */
	private long _startIdIndex = 0;

	/**
	 * relative Position des Index, der die hashCodes der Pid�s verwaltet verwaltet. (-1 bedeutet, dass dieser Wert nicht aus dem Header ausgelesen werden konnte
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

	/** letzter Zeitpunkt, an dem ein dynamisches Objekt ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _dynamicObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein Konfigurationsobjekt ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein konfigurierender Datensatz ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationDataChanged = -1;

	/**
	 * In dieser Menge werden alle dynamischen Objekte und Konfigurationsobjekte gespeichert, die modifiziert wurden und die aus diesem Grund noch in der Datei zu
	 * speichern sind. Die Objekte tragen sich selbst�ndig mit {@link #objectModified} in die Liste ein.
	 */
	private final Set<SystemObjectInformationInterface> _modifiedObjects = new HashSet<SystemObjectInformationInterface>();

	/**
	 * Speichert alle (dynamisch und Konf.) aktuellen Objekte, als Schl�ssel dient die Id.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, SystemObjectInformationInterface> _actualObjects = new HashMap<Long, SystemObjectInformationInterface>();

	/**
	 * Speichert alle Objekte, die in Zukunft aktuell werden, als Schl�ssel dient die Id.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, SystemObjectInformationInterface> _newObjects = new HashMap<Long, SystemObjectInformationInterface>();

	/**
	 * Speichert alle ung�ltig markierte Objekte, die sich in der Mischmenge befinden. Schl�ssel = Id, Value = Objekte mit dem das ung�ltige Objekt angefordert
	 * werden kann.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf long gecastet werden (.get((long) XXX)) !!!
	 */
	private final Map<Long, OldObjectIdReference> _oldObjectsId = new HashMap<Long, OldObjectIdReference>();

	/**
	 * Speichert zu einer Pid (Key = HashCode Integer), alle Dateipositionen der alten Objekte, die sich in der Mischmenge befinden. Bei einer Reorganisation
	 * werden die Listen der Map um die Objekte bereinigt, die in einen nGa oder in den dyn nGa gespeichert werden.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf integer gecastet werden (.get((integer) XXX)) !!!
	 */
	private final Map<Integer, Set<Long>> _oldObjectsPid = new HashMap<Integer, Set<Long>>();

	/**
	 * Alle �nderungen an einem dynamischen Objekt oder an einem Konfigurationsobjekt, die den Zustand von "g�ltig" auf "ung�ltig" setzen oder ein Objekt
	 * hinzuf�gen m�ssen an dieses Objekt gemeldet werden
	 */
	private final ConfigFileManager _fileManager;

	/**
	 * Wird angefordert, wenn die Reorganisation der Datei statt findet. Die Sperre wird ebenfalls angefordert, wenn alte Objekte angefordert werden oder ein
	 * dynamisches Objekt als ung�ltig deklariert wird, die sich im Speicher befinden. Dieser Moment ist kritisch, da zu diesem Zeitpunkt eine Reorganisation
	 * stattfinden kann und sich damit die Dateipositionen �ndern und die Objekte sogar komplett aus dem Speicher gel�scht werden. Neue alte Objekte k�nnen aber
	 * ohne Probleme abgelegt werden, da sich die Reorgansation merkt, welche Objekte modifiziert wurden.
	 */
	private final Object _restructureLock = new Object();

	/**
	 * Stellt das Objekt dar, das den Konfigurationsbereich repr�sentiert. Das Objekt wird entweder beim laden der Mischmenge �ber die Pid des
	 * Konfigurationsbereichs gefunden oder es wird ein neuer Bereich angelegt. Dann wird das Objekt erst mit {@link #createConfigurationObject} erzeugt, auch in
	 * diesem Fall wird das Objekt �ber die Pid identifiziert.
	 */
	private ConfigurationObjectInfo _configAreaObject;

	/**
	 * Synchronisierte Map, die zu jeder Version den lokalen Aktivierungszeitpunkt speichert. Als Key dient die Versionsnummer, als value wird der
	 * Aktivierungszeitpunkt zur�ckgegeben.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 */
	private final Map<Short, Long> _localVersionActivationTime = Collections.synchronizedMap(new HashMap<Short, Long>());

	/**
	 * Array mit den lokalen Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 * Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der n�chsten Aktivierung eingetragen. Der gr��te verwendete Index entspricht
	 * der gr��ten aktivierten Version.
	 */
	private long[] _localActivationTimes;

	/**
	 * Speichert die gr��te Id, die im Konfigurationsbereich vergeben wurde. Der Wert wird beim laden des Konfigurationsbereichs erzeugt, in dem beim laden der
	 * Mischmenge die Id der Objekte gepr�ft wird, zus�tzlich muss der letzte Wert des Index (Id) betrachtet werden. Der Index enth�lt alle Id�s aller Objekte, die
	 * sich in den nGa Bereichen bzw. dem dynamischen nGa Bereich befinden.
	 * <p/>
	 * Der Wert 0 bedeutet, dass noch kein Konfigurationsobjekt oder dynamisches Objekt im Konfigurationsbereich abgelegt wurde.
	 */
	private long _greatestId = 0;

	/**
	 * Speichert zu allen TypeId�s die aktuellen Objekte. Als Schl�ssel dient die TypeId, als Value wird eine Liste mit allen Objekten zur�ckgegeben, die aktuell
	 * sind und deren TypeId mit dem Schl�ssel �bereinstimmt.
	 */
	private final Map<Long, List<SystemObjectInformationInterface>> _actualObjectsTypeId = new HashMap<Long, List<SystemObjectInformationInterface>>();

	/**
	 * Speichert zu allen TypeId�s die alten Objekte. Als Schl�ssel dient die TypeId, als Value wird eine Liste mit allen Objekten zur�ckgegeben. Die Objekte
	 * beinhalten ob das Objekt dynamisch oder konfigurierend ist und die Version bzw. Zeitpunkt an dem das Objekt g�ltig geworden ist. Als letztes ein Objekte,
	 * mit dem das Objekt wieder rekonstruiert werden kann.
	 */
	private final Map<Long, List<OldObjectTypeIdInfo>> _oldObjectsTypeId = new HashMap<Long, List<OldObjectTypeIdInfo>>();


	/**
	 * Mit diesem Objekt wird ein mehrfacher Zugriff auf diese Datei verhindert. Dieser Mechanismus funktioniert nur, wenn alle Klassen, die auf diese Datei
	 * zugreifen, ihn auch benutzen.
	 */
	private final FileLock _areaFileLock;

	/**
	 * Dieser Konstruktor wird benutzt, wenn eine Datei f�r einen Konfigurationsbereich bereits existiert.
	 *
	 * @param configAreaFile             Datei, in der der Konfigurationsberich gespeichert ist
	 * @param activeVersion              aktive Version mit der der Konfigurationsbereich gestartet wird
	 * @param configFileManager          Objekt, das alle Konfigurationsbereiche verwaltet
	 * @param localActivatedVersionTimes Liste, die zu jeder Version, die lokal aktiviert wurde, den Zeitpunkt enth�lt, wann die Version aktiviert wurde
	 */
	public ConfigAreaFile(File configAreaFile, short activeVersion, ConfigFileManager configFileManager, List<VersionInfo> localActivatedVersionTimes)
			throws IOException {
		_configAreaFile = configAreaFile;
		_activeVersion = activeVersion;
		_fileManager = configFileManager;

		_areaFileLock = new FileLock(configAreaFile);
		// Zugriff durch andere sperren
		_areaFileLock.lock();

		_debug.info("Laden der Konfigurationsdatei", configAreaFile);

		for(Iterator<VersionInfo> iterator = localActivatedVersionTimes.iterator(); iterator.hasNext();) {
			final VersionInfo versionInfo = iterator.next();
			if(!_localVersionActivationTime.containsKey(versionInfo.getVersion())) {
				_localVersionActivationTime.put(versionInfo.getVersion(), versionInfo.getActivationTime());
			}
			else {
				throw new IllegalStateException(
						"Die �bergebene Liste enth�lt zu einer Version zwei Zeitpunkte, zu denen die Version g�ltig wurde. Version: " + versionInfo.getVersion()
				);
			}
		}
		_localActivationTimes = getActivationTimeArray(_localVersionActivationTime);

		// Header einlesen
		synchronized(_configAreaFile) {
			ConfigFileHeaderInfo configFileHeaderInfo = new ConfigFileHeaderInfo(_configAreaFile);

			_activeVersionFile = configFileHeaderInfo.getActiveVersionFile();

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

	/**
	 * Erzeugt ein Array mit den Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen.
	 *
	 * @param activationTimeMap Map mit der globalen oder lokalen Zuordnung von aktivierten Versionen zum jeweiligen Zeitpunkt
	 *
	 * @return Array mit den Aktivierungszeiten der Versionen dieses Bereichs. Am Index 0 ist die Zeit 0 eingetragen. Die Aktivierungszeit der Version n ist am
	 *         Index n eingetragen. Bei nicht aktivierten Zwischenversionen wird die Zeit der n�chsten Aktivierung eingetragen. Der gr��te verwendete Index
	 *         entspricht der gr��ten aktivierten Version.
	 */
	private long[] getActivationTimeArray(final Map<Short, Long> activationTimeMap) {
		// Schleife zur Bestimmung der gr��ten aktivierten Version
		int maximumVersion = 0;
		for(Short version : activationTimeMap.keySet()) {
			if(maximumVersion < version) maximumVersion = version;
		}
		final long[] activationTimes = new long[maximumVersion + 1];
		long time = 0;
		for(int i = maximumVersion; i > 0; --i) {
			final Long timeObject = activationTimeMap.get(new Short((short)i));
			if(timeObject != null) time = timeObject.longValue();
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
	 * @param serializerVersion Version, mit der alle Daten in der Datei serialisiert werden m�ssen. Alle Daten, die
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

		// Es wird keine Zuordnung von aktiver Version und Zeitstempel, da bei diesem Konstruktor Version "0" aktiv ist.

		synchronized(_configAreaFile) {
			final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "rw");

			// finally mit close
			try {
				writeHeader(file);
			}
			finally {
				file.close();
			}
		}
	}

	public void setNextActiveVersion(short nextActiveVersion) {
		_nextActiveVersion = nextActiveVersion;
	}

	public short getNextActiveVersion() {
		return _nextActiveVersion;
	}

	/**
	 * Methode, die das Objekt, das den Konfigurationsbereich darstellt, zur�ck gibt.
	 *
	 * @return Objekt oder <code>null</code>, wenn der Bereich gerade erzeugt wurde.
	 */
	public ConfigurationObjectInfo getConfigurationAreaInfo() {
		return _configAreaObject;
	}

	public void flush() throws IOException {
		// Alle Zugriffe auf die Datei sperren

		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {
				final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "rw");

				// finally f�r close der Datei
				try {
					while(_modifiedObjects.size() > 0) {

						// Liste sperren, in der alle Objekte stehen, die noch zu speichern sind
						final SystemObjectInformationInterface modifiedObject;
						// Das synch muss an dieser Stelle sein, da ein Objekt, das ge�ndert wird, sich auf sich selbst
						// synchronisiert und dann die Sperre f�r _modifiedObjects anfordert.

						// Ist das Objekt dann bereits in der _modifiedObjects Liste, blockiert es beim Versuch sich
						// erneut einzutragen. Wird nun hier ebenfalls die gesamte Zeit die Sperre f�r _modifiedObjects
						// gehalten, dann wird beim Speichern die Sperre f�r das Objekt angefordert.

						// Folgende Situation w�rde sich ergeben:
						// Diese Methode h�lt die Sperre f�r _modifiedObjects und will die Sperre f�r das Objekt(zum Speichern).
						// Das Objekt will die Sperre f�r _modifiedObjects(um sich zum speichern einzutragen) und h�lt die Sperre f�r das Objekt.
						// Es g�be einen Deadlock.
						synchronized(_modifiedObjects) {
							// Es gibt noch ein Objekt
							modifiedObject = _modifiedObjects.iterator().next();
							_modifiedObjects.remove(modifiedObject);
						} // synch auf Menge

						// Was f�r ein Objekt liegt vor ?
						// Auf das Objekt muss nicht synchronisiert werden, das �bernimmt die Methode,
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
	 * Speicher ein dynamisches Objekt in die Datei des Konfigurationsbereichs. Diese Methode wird ben�tigt, wenn ein dynamisches Objekt auf "Invalid" gesetzt
	 * werden. F�r Konfigurationsobjekte ist dies nicht n�tig, da diese bis zum beenden der Konfiguration im Speicher bleiben.
	 *
	 * @param object zu speicherndes Objekts
	 */
	void writeDynamicObject(DynamicObjectInformation object) {
		try {
			synchronized(_configAreaFile) {
				final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "rw");
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

	public DynamicObjectInfo createDynamicObject(
			long objectID, long typeID, String pid, short simulationVariant, String name, DynamicObjectType.PersistenceMode persistenceMode
	) {

//		if (objectID < _greatestId) {
//			throw new IllegalArgumentException("Die Id des Objekts ist kleiner als die gr��te Id des Konfigrationsbereichs, Id Konfigurationsbereich " + _greatestId + " Id des Objekts " + objectID + " Konfigurationsbereich " + _configAreaFile);
//		}

		// anlegen des Objekts im Speicher
		// Ob das Objekt auch in der Datei gespeichert werden muss, h�ngt vom PersistenceMode ab.
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
		// Dynamische Objekte sind sofort g�ltig
		putActualObject(newDynamicObjectInformation);
		putActualObjectTypeId(newDynamicObjectInformation);
		// Wenn es sich um ein Objekt f�r eine Simulation handelt, muss es in die entsprechende Datenstruktur eingef�gt werden
		if(simulationVariant > 0) {
			_fileManager.putSimulationObject(newDynamicObjectInformation);
		}

		_fileManager.newObjectCreated(newDynamicObjectInformation);

		// Das Objekt konnte erzeugt werden, also gibt es vielleicht eine neue gr��te Id.
		// (Wenn der Converter benutzt wird sind die Id�s nicht unbedingt streng monoton steigend, darum die Abfrage)
		if(getRunningNumber(objectID) > _greatestId) {
			_greatestId = getRunningNumber(objectID);
		}

		return newDynamicObjectInformation;
	}

	public ConfigurationObjectInfo createConfigurationObject(long objectID, long typeID, String pid, String name) {
//		if (objectID < _greatestId) {
//			throw new IllegalArgumentException("Die Id des Objekts ist kleiner als die gr��te Id des Konfigrationsbereichs, Id Konfigurationsbereich " + _greatestId + " Id des Objekts " + objectID + " Konfigurationsbereich " + _configAreaFile);
//		}

		if(_nextActiveVersion > 0) {
			final ConfigurationObjectInformation newConfigurationObjectInformation = new ConfigurationObjectInformation(
					this, objectID, pid, typeID, name, _nextActiveVersion, true
			);
			// Konf. Objekte sind erst in Zukunft mit der n�chsten Version g�ltig
			putNewObject(newConfigurationObjectInformation);
			_fileManager.newObjectCreated(newConfigurationObjectInformation);

			// Der Konfigurationsverantwortliche wurde ist noch unbekannt (bei der Erzeugung der Datei, als Beispiel)
			// und wird mit create erzeugt. Wurde der Konfigurationsverantwortliche schon gesetzt (geschieht beim einlesen der
			// Datei und stellt den Normalfall dar) kann er nicht mit einem "create" �berschrieben werden.
			if(_configAreaObject == null && newConfigurationObjectInformation.getPid().equals(_configurationAreaPid)) {
				_configAreaObject = newConfigurationObjectInformation;
			}

			// Das Objekt konnte erzeugt werden, also gibt es vielleicht eine neue gr��te Id.
			// (Wenn der Converter benutzt wird sind die Id�s nicht unbedingt streng monoton steigend, darum die Abfrage)
			if(getRunningNumber(objectID) > _greatestId) {
				_greatestId = getRunningNumber(objectID);
			}
			return newConfigurationObjectInformation;
		}
		else {
			throw new IllegalStateException("Es wurde keine Version festgelegt, mit der das Objekt g�ltig werden soll");
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

	public SystemObjectInformationInterface[] getCurrentObjects() {
		synchronized(_actualObjects) {
			Collection<SystemObjectInformationInterface> helper = _actualObjects.values();
			return (SystemObjectInformationInterface[])helper.toArray(new SystemObjectInformationInterface[helper.size()]);
		}
	}

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

	public SystemObjectInformationInterface[] getObjects(
			long startTime, long endTime, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType, Collection<Long> typeIds
	) {
		try {
			// Da f�r jedes Objekt, das aus der Datei geladen wird, gepr�ft werden muss ob es in der Collection typeIds
			// vorhanden ist, wird daf�r ein HashSet benutzt. Die HashSet erm�glicht den Zugriff in O(1) auf eine Id.
			// Die Collection wird mit der "contains" Methode lineare Zeit O(n) ben�tigen.

			// Speichert die geforderten TypeIds und erm�glicht den schnellen Zugriff mittels der TypeId
			final Set<Long> typeIdsSet = new HashSet<Long>(typeIds.size());
			// Set mit den Objekten der Collection laden
			for(Iterator<Long> iterator = typeIds.iterator(); iterator.hasNext();) {
				Long aLong = iterator.next();
				typeIdsSet.add(aLong);
			}

			// Speichert die Objekte, die zur L�sung geh�ren
			final List<SystemObjectInformationInterface> results = new LinkedList<SystemObjectInformationInterface>();

			// Wenn restrukturiert wird darf nicht gesucht werden, da auf Objekte zugegriffen wird, die durch
			// eine Restrukturierung verschoben werden w�rden
			synchronized(_restructureLock) {
				// Es finden lesende Dateizugriffe statt. Die Objekte d�rfen nicht verschoben werden
				synchronized(_configAreaFile) {
// *********************************************************************************************************************
					// Version ermitteln, die zum geforderten Startzeipunkt aktiv war

					// Version, die zum geforderten Startzeitpunkt g�ltig war
					final short firstOldVersion = getActiveVersion(startTime, kindOfTime);
					// Version, die zum geforderten Endzeitpunkt g�ltige Version
					final short lastOldVersion = getActiveVersion(endTime, kindOfTime);


// *********************************************************************************************************************
					// die aktuellen pr�fen, ob diese vielleicht auch schon g�ltig waren
					for(Iterator<Long> iterator = typeIdsSet.iterator(); iterator.hasNext();) {
						final Long typeId = iterator.next();

						// Objekte anfordern, die die geforderte TypeId besitzen
						final List<SystemObjectInformationInterface> objectsForTypeId = _actualObjectsTypeId.get(typeId);

						if(objectsForTypeId != null) {

							// Es gibt Objekte
							for(Iterator<SystemObjectInformationInterface> iterator1 = objectsForTypeId.iterator(); iterator1.hasNext();) {
								// Objekt, das die gew�nschte TypeId besitzt
								final SystemObjectInformationInterface systemObjectInfo = iterator1.next();
								//Pr�fen, ob das Objekt zur L�sung geh�rt

								if(objectValid(systemObjectInfo, startTime, endTime, kindOfTime, timeSpecificationType)) {
									// Das Objekt war g�ltig
									results.add(systemObjectInfo);
								}

							}
						}
					}


// *********************************************************************************************************************
					// Die alten Objekt der Mischmenge pr�fen

					// Map mit alten TypeId Objekten anfragen und alle Objekte nehmen, deren Version bzw. Zeit pa�t (g�ltig ab)
					for(Iterator<Long> iterator = typeIdsSet.iterator(); iterator.hasNext();) {
						// betrachtete TypeId
						final Long typeId = iterator.next();

						final List<OldObjectTypeIdInfo> oldObjectsForTypeId = _oldObjectsTypeId.get(typeId);

						if(oldObjectsForTypeId != null) {
							for(Iterator<OldObjectTypeIdInfo> iterator1 = oldObjectsForTypeId.iterator(); iterator1.hasNext();) {
								final OldObjectTypeIdInfo objectTypeIdInfo = iterator1.next();

								// if (wasObjectActive(objectTypeIdInfo, firstOldVersion, lastOldVersion, startTime, endTime)) {
								if(objectValid(objectTypeIdInfo, startTime, endTime, kindOfTime, timeSpecificationType)) {
									// Das Objekt befindet sich nur teilweise im Speicher und muss nun geladen werden. Transiente Objekte befinden sich
									// ganz im Speicher.
									results.add(objectTypeIdInfo.getOldObjectIdReference().getObject());
								}
							}
						}
					}

// *********************************************************************************************************************
					// nGa Bereiche durchlaufen (Version wurde schon ermittelt)
					// Die Startversion wurde ermittelt, jetzt m�ssen alle nGa Bereiche nach dieser Version geladen werden.
					// Es wird bei jedem Objekt nur die TypeId und Zeit/Version betrachtet. War das Objekt im
					// geforderten Zeitraum g�ltig, dann wird es geladen.

					// Die Datei wird f�r die nGa-Bereiche und den dyn. nGa Bereich ben�tigt
					RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

					// try/finally f�r close der Datei
					try {
						// Gestartet wird beim dem ersten NGA-Block, in dem ung�ltige Objekte der ersten gew�nschten Version enthalten sind (deshalb +1 )
						// Version, in der ein ngaBlock gesucht wird
						short ngaBlockVersion = (short)(firstOldVersion + 1);

							// An dieser Stelle kann muss eins beachtet werden: in dem nGa-Block, der zu der Version geh�rt
							// m�ssen sich nicht unbedingt Elemente befinden. Dies wird mit einer -1 gekennzeichnet.
							// Ist dies der Fall, dann muss der n�chste nGa-Block betrachtet werden (wenn die Version pa�t).
							// Der gerade beschriebene Fall tritt auf, wenn ein Bereich aktiviert wird, aber kein Objekt
							// in der alten Version ung�ltig geworden ist.

							// nGa Block, von dem Daten gelesen werden m�ssen (relative Position zum Header).
							// Dieser Wert wird nur benutzt, wenn auch ein sinnvoller Wert gefunden wurde.
							long relativeBlockOffset = Long.MIN_VALUE;

							// Wird true, wenn ein ngaBlock gefunden wurde, der adressiert werden konnte
							boolean offsetFound = false;

							while(_oldObjectBlocks.containsKey(ngaBlockVersion)) {
								// Pr�fen ob dieser nGaBlock benutzt werden kann.
								// Es muss gepr�ft werden, ob die Objekte des Blocks in der geforderten Zeit g�ltig waren
								final OldBlockInformations nGaBlockInformations = _oldObjectBlocks.get(ngaBlockVersion);
								if(nGaBlockInformations.getFilePosition() >= 0) {
									relativeBlockOffset = nGaBlockInformations.getFilePosition();
									offsetFound = true;
									break;
								}
								// Den n�chsten Bereich betrachten
								ngaBlockVersion++;
							}

							if(offsetFound) {
								// Auf den ersten nGa Block positionieren
								file.seek((relativeBlockOffset + _headerEnd));

								// Es m�ssen solange Daten gelesen werden, bis der dynamische nGa-Bereich erreicht wird
								final long oldConfigurationObjectsBlocks = (_startOldDynamicObjects + _headerEnd);

								// Solange Daten aus den nGa-Bl�cken lesen, bis alle nGa gepr�ft wurden
								while(file.getFilePointer() < oldConfigurationObjectsBlocks) {

									// speichert die Dateiposition des Objekts. Diese Position wird sp�ter
									// am Objekt gespeichert
									final long startObjectFileDescriptor = file.getFilePointer();

									// L�nge des Blocks einlesen
									final int sizeOfObject = file.readInt();

									// Id des Objekts einlesen
									final long objectId = file.readLong();

									if(objectId > 0) {
										// Es ist ein Objekt und keine L�cke

										final int pidHashCode = file.readInt();

										final long typeId = file.readLong();

										// 0 = Konfobjekt, 1 = dyn Objekt
										final byte objectType = file.readByte();

										// Es werden nur Konfigurationsobjekte eingelesen, darum ist es automatisch ein short
										final short firstInvalidVersion;
										final short firstValidVersion;

										firstInvalidVersion = file.readShort();
										firstValidVersion = file.readShort();
										// Wenn das Objekt im angegebenen Zeitraum g�ltig war und der Typ des Objekt pa�t, dann wurde ein Element gefunden
										final OldObjectTypeIdInfo oldObject = new OldObjectTypeIdInfo(
												firstValidVersion, firstInvalidVersion, true, new OldObjectIdReference(startObjectFileDescriptor)
										);
										if(objectValid(
												oldObject, startTime, endTime, kindOfTime, timeSpecificationType
										) && typeIdsSet.contains(new Long(typeId))) {
											// Das Objekt ist im angegebenen Bereich g�ltig, also kann es geladen werden
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
											// Das Objekt geh�rt nicht zu den gesuchten TypeId�s, also muss das
											// n�chste Objekt betrachtet werden

											// Die L�nge bezieht sich auf das gesamte Objekt, ohne die L�nge selber.
											// Also ist die n�chste L�nge bei
											// "aktuelle Position + L�nge - 8 (Id) - 4 (pidHashCode) - 8 (typeId) - 1 (objectType) - 2(firstInvalid) - 2 (firstValid)

											// Die anderen Werte m�ssen abgezogen werden, weil sie bereits eingelesen wurden
											file.seek(file.getFilePointer() + sizeOfObject - 8 - 4 - 8 - 1 - 2 - 2);
										}
									}
									else {
										// Dieser Fall darf nicht auftreten, da es in einem nGa Block keine L�cke gibt
										throw new IllegalStateException(
												"L�cke im nGa-Bereich gefunden, Bereich: " + _configurationAreaPid + " Position: " + file.getFilePointer()
												+ " falsche ObjektId: " + objectId
										);
									}
								}// while
							}
// *********************************************************************************************************************
						// Den dyn nGa Bereich linear durchlaufen

						

						
						// Auf den dyn nGa-Bereich positionieren
						file.seek(_startOldDynamicObjects + _headerEnd);

						// Es m�ssen bis zum Beginn des Id-Index Daten gelesen werden
						final long oldDynamicObjectsEnd = (_startIdIndex + _headerEnd);

						while(file.getFilePointer() < oldDynamicObjectsEnd) {

							// speichert die Dateiposition des Objekts. Diese Position wird sp�ter
							// am Objekt gespeichert
							final long startObjectFileDescriptor = file.getFilePointer();

							// L�nge des Blocks einlesen
							final int sizeOfObject = file.readInt();

							// Id des Objekts einlesen
							final long objectId = file.readLong();

							if(objectId > 0) {
								// Es ist ein Objekt und keine L�cke

								final int pidHashCode = file.readInt();

								final long typeId = file.readLong();

								// 0 = Konfobjekt, 1 = dyn Objekt
								final byte objectType = file.readByte();

								// Es werden nur dynamische Objekte eingelesen, darum ist es automatisch ein long
								final long firstInvalidTime;
								final long firstValidTime;

								firstInvalidTime = file.readLong();
								firstValidTime = file.readLong();

								// Objekt war g�ltig und der Typ des Objekts passt
								final OldObjectTypeIdInfo oldDynamicObject = new OldObjectTypeIdInfo(
										firstValidTime, firstInvalidTime, false, new OldObjectIdReference(startObjectFileDescriptor)
								);
								if(objectValid(
										oldDynamicObject, startTime, endTime, kindOfTime, timeSpecificationType
								) && typeIdsSet.contains(new Long(typeId))) {
									// Das Objekt ist im angegebenen Bereich g�ltig, also kann es geladen werden
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
									// Das Objekt geh�rt nicht zu den gesuchten TypeId�s, also muss das
									// n�chste Objekt betrachtet werden

									// Die L�nge bezieht sich auf das gesamte Objekt, ohne die L�nge selber.
									// Also ist die n�chste L�nge bei
									// "aktuelle Position + L�nge - 8 (Id) - 4 (pidHashCode) - 8 (typeId) - 1 (objectType) - 8(firstInvalid) - 8(firstValid)

									// Die anderen Werte m�ssen abgezogen werden, weil sie bereits eingelesen wurden
									file.seek(file.getFilePointer() + sizeOfObject - 8 - 4 - 8 - 1 - 8 - 8);
								}
							}
							else {
								// Dieser Fall darf nicht auftreten, da es in einem nGa Block keine L�cke gibt
								throw new IllegalStateException(
										"L�cke im dynamischen nGa-Bereich gefunden, Position: " + file.getFilePointer() + " L�cken: " + objectId
								);
							}
						}// while

					}
					finally {
						file.close();
					}
// *********************************************************************************************************************

					// gefundene Objekt zur�ckgeben
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
	 * Diese Methode pr�ft ob folgende Gleichung wahr ist:
	 * <p/>
	 * start <= middle <= end
	 *
	 * @return true = Die oben angegebene Gleichung ist wahr
	 */
	private boolean isValueAtTheMiddle(final long start, final long middle, final long end) {

		if((start <= middle) && (middle <= end)) {
			// Objekt war/ist im angegebenen Zeitraum g�ltig
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Gibt zu einem Zeitpunkt die Version zur�ck, die aktiv war.
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

		// Enth�lt alle Versionen in sortierter Reihenfolge
		final Short sortedVersions[];
		sortedVersions = unsortedVersions.toArray(new Short[unsortedVersions.size()]);

		Arrays.sort(sortedVersions);

		for(int nr = 0; nr < sortedVersions.length; nr++) {

			// untere Zeitgrenze
			final long start;
			// obere Zeitgrenze
			final long end;

			// Version, die gerade gepr�ft wird ob sie zu dem �bergebenen Zeitpunkt aktiv war.
			final short consideredVersion = sortedVersions[nr];

			if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
				start = _configurationAuthorityVersionActivationTime.get(consideredVersion);
			}
			else {
				start = _localVersionActivationTime.get(consideredVersion);
			}

			// Pr�fen, ob es noch eine n�chste Version gibt
			if((nr + 1) < sortedVersions.length) {
				if(kindOfTime == ConfigurationAreaTime.GLOBAL_ACTIVATION_TIME) {
					end = _configurationAuthorityVersionActivationTime.get(sortedVersions[nr + 1]);
				}
				else {
					end = _localVersionActivationTime.get(sortedVersions[nr + 1]);
				}
			}
			else {
				// Es gibt keinen �bern�chste Version mehr
				end = Long.MAX_VALUE;
			}

			// 1) Wenn sich der �bergebene Zeitpunkt zwischen start und end befindet, dann wurde die Version gefunden, die zu diesem Zeitpunkt
			// aktiv war.
			// 2) Ist der �bergebene Zeitpunkt kleiner als der start-Wert, wurde die aktive Version gefunden

			if(isValueAtTheMiddle(start, startTime, end) || (startTime <= start)) {
				return consideredVersion;
			}
		} // for

		// Es gibt keine Version, die zum geforderten Zeitpunkt g�ltig gewesen ist
		return -1;
	}

	public SystemObjectInformationInterface[] getNewObjects() {
		synchronized(_newObjects) {
			Collection<SystemObjectInformationInterface> helper = _newObjects.values();
			return (SystemObjectInformationInterface[])helper.toArray(new SystemObjectInformationInterface[helper.size()]);
		}
	}

	public Iterator<SystemObjectInformationInterface> iterator() {
		return new FileIterator();
	}

	/**
	 * Schreibt einen Header in die �bergebene Datei. Der FileDescriptor wird auf Position 0 gesetzt und steht am Ende des Schreibvorgangs am Ende des Headers.
	 *
	 * @param file
	 *
	 * @throws IOException
	 */
	private void writeHeader(RandomAccessFile file) throws IOException {
		synchronized(file) {
			// Der Header wird an den Beginn der Datei geschrieben
			file.seek(0);

			// Die Headerl�nge ist unbekannt, also wird als Platzhalter eine -1 geschrieben
			file.writeInt(-1);

			// Kennung 1 speichern
			file.writeShort(1);
			// L�nge der Kennung, 4 Bytes
			file.writeShort(4);
			// aktive Version
			file.writeShort(_activeVersion);
			// gr��te Version
			file.writeShort(_activeVersion + 1);

			// Kennung 2, alle nGa Bl�cke.
			if(_oldObjectBlocks.size() > 0) {
				file.writeShort(2);
				// Platzbedarf speichern
				file.writeShort(_oldObjectBlocks.size() * (8 * 2 + 2));

				// Schl�ssel sortieren
				final Short[] oldVersions = _oldObjectBlocks.keySet().toArray(new Short[_oldObjectBlocks.keySet().size()]);
				Arrays.sort(oldVersions);

				for(int nr = 0; nr < oldVersions.length; nr++) {
					// Die Objekte in aufsteigender Reihenfolge speichern
					final short version = oldVersions[nr];
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
				// Es gibt noch keine nGa-Bl�cke, die Kennung wird trotzdem geschrieben.
				// Also hat auch ein Header, der keine nGa-Bl�cke besitzt schon die richtige Gr��e.
				// Wird die Kennung nicht geschrieben, dann fehlen 4 Bytes (2 Bytes f�r die Kennung, 2 Bytes f�r die L�nge) und
				// dieser Fall muss extra erkannt werden. So hat der Header ohne nGa sofort die richtige L�nge.
				file.writeShort(2);
				// Platzbedarf speichern. Es gibt keine Bl�cke
				file.writeShort(0);
			}
			// Kennung 3, nGa dynamische Objekte
			file.writeShort(3);
			file.writeShort(8);
			// relative Position des dyn nGa Bereichs
			file.writeLong(_startOldDynamicObjects);

			// Kennung 4, Index Id
			file.writeShort(4);
			file.writeShort(8);

			file.writeLong(_startIdIndex);

			// Kennung 5, Index Pid
			file.writeShort(5);
			file.writeShort(8);

			file.writeLong(_startPidHashCodeIndex);

			// Kennung 6, Index Pid
			file.writeShort(6);
			file.writeShort(8);

			file.writeLong(_startMixedSet);

			// Kennung 7, Index Pid
			file.writeShort(7);
			// Die Pid des Konfigurationsbereichs ist Variable, also muss die L�nge berechnet werden
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

			// L�nge des Headers schreiben
			final long headerEnd = file.getFilePointer();
			file.seek(0);
			// Der Intergerwert, der die L�nge des Headers darstellt,
			// z�hlt nicht zu l�nge des Headers und wird darum abgezogen
			file.writeInt((int)(headerEnd - 4));
			file.seek(headerEnd);
		}
	}

	/**
	 * L�dt alle "aktuellen" und in "Zukunft aktuellen" Objekte. Objekte die ung�ltig sind, aber sich noch in der Mischobjektmenge befinden, werden nur Teilweise
	 * (ID, Pid-HashCode, Dateiposition) geladen.
	 * <p/>
	 * Soll der Bereich in einer anderen als der aktuellen Version geladen werden, dann m�ssen ebenfalls die nGa-Bereiche betrachtet werden.
	 *
	 * @return Collecetion, die alle Objekte der Mischmenge enth�lt (entweder ganz, oder nur als ID-Pid-Dateiposition Kombination) und Objekte aus den
	 *         entsprechende nGa-Bereichen
	 *
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	public Collection getMixedObjectSetObjects() throws IOException, NoSuchVersionException {

		try {
			// Es m�ssen alle Objekte der Mischobjektmenge zur�ckgegeben werden.

			final List mixedObjects = new ArrayList();

			synchronized(_configAreaFile) {

				// Beschreibt ab welcher Position Daten eingelesen werden.
				// Wenn die aktiver Version, die im Konstruktor �bergeben wurde, in der Map _oldObjectBlocks
				// zu finden ist, dann m�ssen zuerst diese Objekte geladen werden und zwar bis zum dyn nGa Block.
				// Erst danach wird die Mischmenge geladen.

				final long startingPosition;

				if(_oldObjectBlocks.containsKey(_activeVersion)) {
					// Es gibt mindestens einen nGa Block, der Objekte enth�lt, die geladen werden m�ssen.
					// Die relative Position des Blocks anfordern.
					// Die relative Position eines Blocks kann eine -1 enthalten. Dies bedeutet, dass es zu diesem Block
					// keine Objekte gibt, der Block kann also "�bersprungen" werden.
					// Es werden solange Bl�cke �bersprungen, bis eine relative Startposition gefunden wurde oder
					// es keine Bl�cke mehr gibt, in diesem Fall kann direkt die Mischmenge geladen werden.

					// Als Default auf die Mischmenge, wenn dieser Wert nicht neu gesetzt werden kann, dann wird direkt
					// die Mischmenge geladen
					long relativeStart = _startMixedSet;

					// Bestimmt die n�chste Version, die von der aktiven Version aus betrachtet wird und die altive
					// Version besitzt keinen nGa Block
					short versionOffset = 0;

					// Solange es nGa Blocks gibt
					while(_oldObjectBlocks.containsKey((short)(_activeVersion + versionOffset))) {
						final long blockOffset = _oldObjectBlocks.get((short)(_activeVersion + versionOffset)).getFilePosition();

//						System.out.println("Block Offset, relativ: " + blockOffset);

						if(blockOffset >= 0) {
							// Es gibt einen nGa Block f�r die Version
							relativeStart = blockOffset;
							// Der Start wurde gefunden, also kann die Schleife verlassen werden
//							System.out.println("Es muss ein nGa-Block geladen werden, absolute Position: " + (relativeStart + _headerEnd) + " relative Position: " + relativeStart + " Version des Blocks: " + (_activeVersion + versionOffset));
							break;
						}
						else {
							// Die Version enth�lt keine Daten, also wird die n�chste betrachtet
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

				// Datei �ffnen
				final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

				try {
					// Als erstes muss der Index-Id gepr�ft werden, um die gr��te Id der alten Objekte
					// zu finden.
					if(_startIdIndex > 0) {
						// Es gibt einen Id-Index
						file.seek((_startPidHashCodeIndex + _headerEnd) - 8 - 8);
						// gr��te Id der alten Objekte einlesen
						_greatestId = getRunningNumber(file.readLong());
					}

					// finally f�r close der Datei

					// Datei auf den Anfang der Mischmenge postieren
					file.seek(startingPosition);

					// Wie gross ist die Datei
					final long fileSize = file.length();

					// Wird true, wenn das Objekt, das den Konfigurationsbereich wiederspiegelt, gefunden wurde
					boolean configAreaObjectFound = false;
					while(file.getFilePointer() < fileSize) {

						// Wenn alle nGa Bl�cke geladen wurden, dann steht der FileDescriptor auf dem Anfang des
						// dyn nGa Blocks, dann m�ssen die Objekte aus der Mischmenge geladen werden.
						// Dazu wird der dyn nGa Block der Id Index und der Pid-HashCode Index �bersprungen.
						if(file.getFilePointer() == (_startOldDynamicObjects + _headerEnd)) {
//							System.out.println("Sprung zur Mischmenge, aktuelle Position, absolut: " + file.getFilePointer() + " neue Position: " + (_startMixedSet + _headerEnd));
							// Es wurde bis zum dyn nGa gelesen
							file.seek(_startMixedSet + _headerEnd);
						}

						// speichert die Dateiposition des Objekts. Diese Position wird sp�ter
						// am Objekt gespeichert
						final long startObjectFileDescriptor = file.getFilePointer();

						// L�nge des Blocks einlesen
						final int sizeOfObject = file.readInt();

						// Id des Objekts einlesen
						final long objectId = file.readLong();

						if(objectId > 0) {
							// Es ist ein Objekt und keine L�cke

							// Ist die Id des gelesenen Objekts gr��er als die bisher gespeicherte Id
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
									// Die Version, mit der das Objekt ung�ltig wird, ist kleiner gleich der aktuellen Version.
									// Also ist das Objekt jetzt ung�ltig, dies gilt nur unter der Vorrausetzung, dass
									// die Version gr��er 0 ist (0 bedeutet, der Wert wurde noch nicht gesetzt)

									OldObject oldObject = new OldObject(objectId, pidHashCode, startObjectFileDescriptor, this);
									mixedObjects.add(oldObject);
									putOldObject(oldObject);

									// Objekt in Map f�r typeId Suche aufnehmen, dies ist ein "ung�ltig" markiertes Objekts
									putOldObjectTypeId(typeId, firstValid, firstInvalid, true, new OldObjectIdReference(startObjectFileDescriptor));

									final long newDestination = startObjectFileDescriptor + 4 + sizeOfObject;
									file.seek(newDestination);
//								System.out.println("alter Datensatz: Id " + objectId);
								}
								else {
									// Das Objekt ist aktuell oder wird in der Zukunft aktuell, also alle Informationen laden.
									// Der pidHashCode muss nicht �bergeben werden, da das Objekt als ganzes geladen wird und somit zur Verf�gung steht
									final SystemObjectInformationInterface configurationObject = readObjectFromFile(
											startObjectFileDescriptor, sizeOfObject, objectId, typeId, firstInvalid, firstValid, objectType, file
									);

									// Es muss das Objekt, das den Konfigurationsbereich darstellt gefunden werden.
									// Es gibt 2 F�lle:
									// 1) Es gibt ein Objekt, das in der aktuellen Version g�ltig ist und den Konfigurationsbereich darstellt (Normalfall)
									// 2) Es gibt nur Objekte, die in der Zukunft g�ltig sind und den Konfigurationsbereich darstellen.

									// In Fall 1) wird das Objekt genommen, dessen Version in der aktuellen Version g�ltig ist

									// In Fall 2) wurde der Konfigurationsbereich gerade erzeugt und das Objekt, das den Konf.bereich darstellt,
									// ist erst in der n�chsten Version g�ltig (es ist ja ein Konfigurtionsobjekt).
									// In diesem Fall wird das Objekt genommen, das den gr��ten Wert bei "g�ltig ab" besitzt.

									// Das Objekt, das den Konfigurationsbereich darstellt, wird �ber die Pid identifiziert
									if((!configAreaObjectFound) && (configurationObject.getPid().equals(_configurationAreaPid))) {
										// Das Objekt muss noch g�ltig sein (egal wann es g�ltig wird)
										if((firstInvalid == 0) || (firstInvalid > _activeVersion)) {
											if(_configAreaObject == null) {
												// Das erste Objekt, dass passt wird genommen. Es muss nur g�ltig sein oder
												// in der Zukunft g�ltig werden (kein altes Objekt)
												_configAreaObject = (ConfigurationObjectInfo)configurationObject;
											}
											else {
												// Es gab bereits ein Objekt, das den Konfigurationsbereich darstellt.
												// Es wird wie in Fall 1),2) beschrieben vorgegangen

												// Fall 1)
												if(isValueAtTheMiddle(0, firstValid, _activeVersion)) {
													// Das Objekt ist jetzt g�ltig, also wurde das gesuchte Objekt gefunden
													// und es muss nicht weiter gesucht werden
													_configAreaObject = (ConfigurationObjectInfo)configurationObject;
													configAreaObjectFound = true;
												}
												else {
													// Das Objekt ist erst in Zukunft g�ltig. Fall 2)
													if(_configAreaObject.getFirstValidVersion() < firstValid) {
														// Das betrachtete Objekt wird sp�ter g�ltig, als das bisher gefundene.
														_configAreaObject = (ConfigurationObjectInfo)configurationObject;
													}
												}
											}
										}
									}

									mixedObjects.add(configurationObject);
									putUnknownObject(configurationObject);

									// F�r die TypeId-Suche werden nur aktuelle Objekte ben�tigt.
									// Objekte die erst in der Zukunft g�ltig werden, werden nicht betrachtet
									if(firstValid <= _activeVersion) {
										// Das Objekt ist derzeit g�ltig
										putActualObjectTypeId(configurationObject);
									}
								}
							}
							else {
								// dynamisches Objekt
								firstInvalid = file.readLong();
								firstValid = file.readLong();

								// Das vollst�ndige dynamische Objekt wird in den Speicher geladen.

								


								// Der pidHashCode muss nicht �bergeben werden, da das Objekt als ganzes geladen wird und somit zur Verf�gung steht
								final SystemObjectInformationInterface dynamicObject = readObjectFromFile(
										startObjectFileDescriptor, sizeOfObject, objectId, typeId, firstInvalid, firstValid, objectType, file
								);

								// Datenstruktur des FileManager mit dem dynamischen Objekt f�r Simulationen laden
								_fileManager.putSimulationObject((DynamicObjectInformation)dynamicObject);

								if((firstInvalid > 0)) {
									// Der Invalid Wert ist gesetzt, also ist das Objekt ung�ltig (ein dyn Objekt wird sofort ung�tlig, nicht in
									// der Zukunft)
									OldObject oldObject = new OldObject(objectId, pidHashCode, startObjectFileDescriptor, this);
									mixedObjects.add(oldObject);
									putOldObject(oldObject);

									// In die Liste f�r TypeId�s aufnehmen (false, da es sich um ein dynamisches Objekt handelt)
									putOldObjectTypeId(typeId, firstValid, firstInvalid, false, new OldObjectIdReference(startObjectFileDescriptor));

									// Den fileDescriptor auf den n�chsten Datensatz setzen.
									




								}
								else {
									// Das Objekt ist aktuell. es gibt kein dyn Objekt, das in der Zukunft g�ltig wird, dyn Objekt sind
									// immer sofort g�ltig.

									mixedObjects.add(dynamicObject);
									putUnknownObject(dynamicObject);
									putActualObjectTypeId(dynamicObject);
								}
							}
						}
						else {
//							System.out.println("L�cke: " + startObjectFileDescriptor + " negative Id: " + objectId);
							// Eine L�cke, der filePointer muss verschoben werden.
							// Die L�nge bezieht sich auf das gesamte Objekt, ohne die L�nge selber.
							// Also ist die n�chste L�nge bei "aktuelle Position + L�nge - 8.
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
			throw new IllegalStateException();
		}
		catch(NoSuchVersionException e) {
			e.printStackTrace();
			_debug.error("Versions-Fehler beim Laden aller aktuellen und in Zukunft aktuellen Objekten mit passenden Typ (siehe exception)", e);
			throw new IllegalStateException();
		}
	}

	/**
	 * Legt ein dynamisches Objekt oder ein Konfigurationsobjekt in die entsprechende Datenstruktur f�r aktuelle oder in Zukunft g�ltige Objekte ab.
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
				// Das Objekt ist erst in Zukunft g�ltig
				putNewObject(object);
			}
		}
		else if(unknownObject instanceof DynamicObjectInformation) {
			final DynamicObjectInformation object = (DynamicObjectInformation)unknownObject;
			if(object.getFirstInvalidTime() == 0) {
				// Wenn der Wert gesetzt w�re, dann w�re das Objekt bereits ung�ltig
				putActualObject(object);
			}
			// Den Fall, dass ein dynamisches Objekt in der Zukunft g�ltig wird, gibt es nicht
		}
		else {
			_debug.warning("Unbekanntes Objekt: " + unknownObject.getClass());
		}
	}

	/**
	 * F�gt ein als "ung�ltig" markiertes Objekt in alle interenen Datenstrukturen hinzu, die f�r einen schnellen Zugriff auf ung�ltige Objekte ben�tigt werden.
	 * <p/>
	 * Alle Objekte, die �bergeben werden, sind auch in der Datei enthalten, transiente Objekte k�nnen mit dieser Methode nicht abgebildet werden.
	 *
	 * @param oldObject Objekt, das Id, PidHashCode und die Dateipostion enth�lt. Das configAreaFileObjekt wird nicht ben�tigt.
	 */
	private void putOldObject(OldObject oldObject) {
		synchronized(_oldObjectsId) {
			final OldObjectIdReference oldObjectIdReference = new OldObjectIdReference(oldObject.getFilePosition());
			_oldObjectsId.put(oldObject.getId(), oldObjectIdReference);
		}

		synchronized(_oldObjectsPid) {
			Set<Long> filePositions = _oldObjectsPid.get(oldObject.getPidHashCode());

			if(filePositions != null) {
				// Es gibt eine Liste, also Dateipostion einf�gen
				filePositions.add(oldObject.getFilePosition());
			}
			else {
				// Es gibt noch keine Liste
				filePositions = new HashSet<Long>();
				filePositions.add(oldObject.getFilePosition());

				// Die neue Liste in die Map einf�gen
				_oldObjectsPid.put(oldObject.getPidHashCode(), filePositions);
			}
		}
	}

	/**
	 * F�gt das Objekt zu allen Datenstrukturen hinzu, die f�r den schnellen Zugriff auf "in Zukunft g�ltige" Objekte ben�tigt werden
	 *
	 * @param newObjekt Objekt, das in die Datenstruktur aufgenommen werden soll
	 */
	private void putNewObject(SystemObjectInformationInterface newObjekt) {
		synchronized(_newObjects) {
			_newObjects.put(newObjekt.getID(), newObjekt);
		}
	}

	/**
	 * Diese Methode entfernt ein Objekt, das in zuk�nftigen Versionen g�ltig werden soll aus allen Datenstrukturen (auch dem ConfigFileManager), die den schnellen
	 * Zugriff auf diese Art von Objekten erm�glichen.
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
	 * Entfernt ein dynamisches Objekt aus alle Datenstrukturen(auch ConfigFileManager). Es wird nicht aus der Datei gel�scht.
	 * <p/>
	 * Diese Methode darf nur auf Objekte mit einer Simulationsvariante gr��er 0 angewendet werden.
	 *
	 * @param dynamicObjectInfo Objekt, das entfernt werden soll
	 */
	void removeDynamicSimulationObject(DynamicObjectInformation dynamicObjectInfo) {

		// Dynamische Objekte k�nnen aktuell oder alt sein. Aus diesen Datenstrukturen werden die Elemente entfernt.
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
			final Set<Long> longs = _oldObjectsPid.get(dynamicObjectInfo.getPidHashCode());
			if(longs != null) {
				longs.remove(new Long(dynamicObjectInfo.getLastFilePosition()));
			}
		}

		// Beim Filemanager aufr�umen
		_fileManager.removeDynamicSimulationObject(dynamicObjectInfo);
	}

	/**
	 * F�gt das Objekt zu allen Datenstrukturen hinzu, die f�r den schnellen Zugriff(mit Id) auf aktuelle Objekte ben�tigt werden
	 *
	 * @param actualObjekt
	 */
	private void putActualObject(SystemObjectInformationInterface actualObjekt) {
		synchronized(_actualObjects) {
			_actualObjects.put(actualObjekt.getID(), actualObjekt);
		}
	}

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
	 * Speichert alle ben�tigten Ladeinforamtionen zu einem Objekt in der Map, die alle alten Objekte verwaltet.
	 *
	 * @param typeId               TypeId des Objekt, das nicht vollst�ndig geladen wurde
	 * @param firstValid           Zeitpunkt oder Version mit der das Objekt g�ltig wurde
	 * @param firstInvalid         Zeitpunkt oder Version mit der das Objekt ung�ltig wurde
	 * @param configurationObject  true = Es handelt sich um ein Konfigurationsobjekt (firstValid wird als Version interpretiert); false = Es handelt sich um ein
	 *                             dynamisches Objekt (firstValid wird als Zeitpunkt interpertiert)
	 * @param oldObjectIdReference Objekt zum anfordert des Objekts (Datei oder Speicher)
	 */
	private void putOldObjectTypeId(
			final long typeId,
			final long firstValid,
			final long firstInvalid,
			final boolean configurationObject,
			final OldObjectIdReference oldObjectIdReference
	) {
		synchronized(_oldObjectsTypeId) {
//			System.out.println("F�ge Ladeinformationen hinzu: typeId " + typeId + " confObjekt: " + configurationObject + " absoluteDateipoistion: " + absoluteFilePosition);
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
			typeIdInfoList.add(new OldObjectTypeIdInfo(firstValid, firstInvalid, configurationObject, oldObjectIdReference));
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

	public SystemObjectInformationInterface[] getObjects(String pid, long startTime, long endTime, ConfigurationAreaTime kindOfTime) {

		try {
			// Speichert alle Objekte, f�r die die Parameter �bereinstimmen
			final List<SystemObjectInformationInterface> results = new LinkedList<SystemObjectInformationInterface>();

			// K�nnen aktuelle Objekte betroffen sein ? Wenn nicht, wird null zur�ckgegeben.
			// Achtung, das zur�ckgegebene Objekt kann in einem beliebigen Bereich sein, es muss
			// nicht unbedingt dieser Bereich sein !! Wenn es nicht in diesem Bereich liegt, kann es ignoriert werden.
			final SystemObjectInformationInterface actualSystemObjectInfo = _fileManager.getActiveObject(pid);

			if(actualSystemObjectInfo != null) {
				// befindet sich das Objekt in diesem Bereich ?
				if(_actualObjects.containsKey(actualSystemObjectInfo.getID())) {
					// Das aktuelle Objekt ist Teil von diesem Bereich, also pr�fen ob es im gefordeten Zeitbereich g�ltig ist.
					if(objectValid(actualSystemObjectInfo, startTime, endTime, kindOfTime, TimeSpecificationType.VALID_IN_PERIOD)) {
						// Das Objekt ist im Zeibereich g�ltig
						results.add(actualSystemObjectInfo);
					}
				}
			}

			// als ung�ltig markierte Objekte pr�fen
			final List<SystemObjectInformationInterface> binarySearchResult;
			binarySearchResult = binarySearch(pid.hashCode(), false);
			


			if(binarySearchResult != null) {
				for(int nr = 0; nr < binarySearchResult.size(); nr++) {
					final SystemObjectInformationInterface systemObjectInfo = binarySearchResult.get(nr);
					// Pa�t die Zeit
					if(objectValid(systemObjectInfo, startTime, endTime, kindOfTime, TimeSpecificationType.VALID_IN_PERIOD)) {
						results.add(systemObjectInfo);
					}
				}
			}
			return results.toArray(new SystemObjectInformationInterface[results.size()]);
		}
		catch(Exception e) {
			e.printStackTrace();
			_debug.error("Objekt mit Pid " + pid + " konnte aufgrund eines Fehlers nicht gefunden werden", e);
			throw new IllegalStateException("Objekt mit Pid " + pid + " konnte aufgrund eines Fehlers nicht gefunden werden: " + e);
		}
	}

	/**
	 * Diese Methode pr�ft, ob ein Objekte im angegebenen Zeitraum g�ltig war/ist.
	 *
	 * @param objectInfo            Objekt, das gepr�ft werden soll
	 * @param queryIntervalStart             Startzeitpunkt ab dem ein Objekte g�ltig sein muss
	 * @param queryIntervalEnd               Endzeitpunkt, bis zu dem ein Objekt g�ltig geworden sein muss
	 * @param kindOfTime            Zeit des Konfigurationsverantwortlichen oder die lokale Zeit zu der eine Version g�ltig geworden ist
	 * @param timeSpecificationType Gibt an, ab wann ein Objekt als "g�ltig" deklariert werden darf. Es gibt 2 F�lle: Fall 1) Das Objekt muss im angegebnen Bereich
	 *                              irgendwann g�ltig gewesen sein (Darf aber auch in dem Bereich ung�ltig geworden sein). Fall 2) Das Objekt muss im gesamten
	 *                              Zeitbereich g�ltig gewesen sein, die entspricht TimeSpecificationType.VALID_DURING_PERIOD
	 *
	 * @return true = das Objekt war in dem angegebenen Zeitbereich g�ltig; false = das Objekt war im angegebenen Zeitbereich nicht g�ltig
	 */
	boolean objectValid(Object objectInfo, long queryIntervalStart, long queryIntervalEnd, ConfigurationAreaTime kindOfTime, TimeSpecificationType timeSpecificationType) {
		// Die Klasse ist im Package sichtbar, damit ein JUnit-Test die Methode pr�fen kann

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

		// Zeitpunkt, an dem das Objekt g�ltig wurde
		final long objectValidSince;
		// Zeitpunkt, an dem das Objekt ung�ltig wurde.
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
			// Das Objekt kann ein Konfigurationsobjekt sein oder ein dynamisches Objekt. In beiden F�llen ist das Objekt nur teilweise im Speicher vorhanden
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

		// (objectValidSince == 0) bedeutet, dass dieses Objekt noch immer g�ltig ist
		if(objectInvalidSince == 0) objectInvalidSince = Long.MAX_VALUE;

		if(TimeSpecificationType.VALID_DURING_PERIOD == timeSpecificationType) {
			// Das Objekt muss im gesamten Zeitraum g�ltig gewesen sein
			return objectValidSince <= queryIntervalStart && queryIntervalEnd <= objectInvalidSince;
		}
		else {
			return objectValidSince <= queryIntervalEnd && queryIntervalStart <= objectInvalidSince;
		}
	}


	public int getSerializerVersion() {
		return _serializerVersion;
	}

	/**
	 * L�dt ein Objekt aus einer Datei
	 *
	 * @param filePosition Position in der Datei, an der das Objekt beginnt
	 *
	 * @return Objekt, das aus der Datei erzeugt wurde
	 *
	 * @throws IOException
	 * @throws de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException
	 */
	SystemObjectInformation loadObjectFromFile(long filePosition) throws IOException, NoSuchVersionException {
		return loadObjectFromFile(filePosition, null);
	}

	/**
	 * L�dt ein Objekt aus einer Datei und setzt im Objekt fileIterator die Position, an der das n�chste Objekt gefunden werden kann (relative Position bezogen auf
	 * den Header).
	 *
	 * @param filePosition Position in der Datei, an der das Objekt beginnt
	 * @param fileIterator Objekt, an dem die Position des n�chsten zu ladenden Objekts gespeichert wird. Wird <code>null</code> �bergeben, so wird die Psoition
	 *                     des n�chsten Objekts nicht gesetzt. Wird eine -1 gesetzt,so gibt es kein n�chstes Objekt mehr
	 *
	 * @return Objekt, das aus der Datei erzeugt wurde
	 *
	 * @throws IOException
	 * @throws NoSuchVersionException
	 */
	private SystemObjectInformation loadObjectFromFile(long filePosition, FileIterator fileIterator) throws IOException, NoSuchVersionException {
		synchronized(_configAreaFile) {

			final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

			file.seek(filePosition);

			// try f�r finally und close
			try {

				// L�nge des Blocks einlesen
				final int sizeOfObject = file.readInt();

				// Id des Objekts einlesen
				final long objectId = file.readLong();

//				System.out.println("altes Objekt in den Speicher laden: " + filePosition + " Id " + objectId);

				if(objectId > 0) {
					// Es ist ein Objekt und keine L�cke
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
					}
					else {
						firstInvalid = file.readLong();
						firstValid = file.readLong();
					}

					final SystemObjectInformation loadedObject = readObjectFromFile(
							filePosition, sizeOfObject, objectId, typeId, firstInvalid, firstValid, objectType, file
					);
					// Position speichern, an der das n�chste Objekt geladen werden kann
					if(fileIterator != null) {
						// Sobald der Id-Index beginnt, wird eine -1 zur�ckgegeben
						if(file.getFilePointer() < (_startIdIndex + _headerEnd)) {
							// Es k�nnen noch Daten gelesen werden
							fileIterator.setRelativePosition((file.getFilePointer() - _headerEnd));
						}
						else {
							// Es gibt keinen n�chsten Datensatz mehr
							fileIterator.setRelativePosition(-1);
						}
					}

					return loadedObject;
				}
				else {
					// Es sollte eine L�cke eingelesen werden
					final String errorText = "L�cke an Position: " + filePosition + " Datei: " + _configAreaFile + " ObjectId: " + objectId;
					_debug.error(errorText);
					throw new IllegalStateException(errorText);
				}
			}
			finally {
				file.close();
			}
		}
	}

	/**
	 * L�dt ein dynamischen Objekt oder ein Konfigurationsobjekt aus der Datei des Konfigurationsbereichs. Die Datei, die �bergeben wird, steht mit ihrem
	 * fileDescriptor entweder auf der Simulationsvariante (bei dynamischen Objekten) oder auf den gepackten Daten (bei Konfigurationsobjekten).
	 *
	 * @param filePosition Position in der Datei, an der das Objekt gespeichert ist
	 * @param objectsize   Gesamtegr��e des Objekts (einschlie�lich der schon geladenen Daten)
	 * @param id           Id des Objekts
	 * @param typeId       Id des Types des Objekts
	 * @param firstInvalid Version oder Zeitpunkt, an dem das Objekt ung�ltig wurde/wird
	 * @param firstValid   Version oder Zeitpunkt, an dem das Objekt g�ltig wird/wurde
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
			RandomAccessFile file
	) throws IOException, NoSuchVersionException {

		// Das Objekt wird geladen, es wird daf�r gesorgt, dass es sich nicht sofort wieder speichern will
		synchronized(_configAreaFile) {
			if(objecttype == 0) {
				// Konfigurationsobjekt

				// Der vordere Teil ist konstant, also kann die L�nge der gepackten Daten berechnet werden.
				// id, pidHash, typeId, type(Konf oder dynamische), Version, Version abziehen
				final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 2 - 2;
				final byte packedBytes[] = new byte[sizeOfPackedData];
				file.readFully(packedBytes);

				// Byte-Array, das die ungepackten Daten enth�lt
				final byte[] unpackedBytes = unzip(packedBytes);

				final InputStream in = new ByteArrayInputStream(unpackedBytes);

				//deserialisieren
				final Deserializer deserializer = SerializingFactory.createDeserializer(_serializerVersion, in);

				// Das serialisierte SystemObjektInfo einlesen

				final int pidSize = deserializer.readUnsignedByte();
				final String pid;
				if(pidSize > 0) {
					pid = deserializer.readString(255);
				}
				else {
					
					pid = deserializer.readString(0);
				}

//				final String pid = readString(deserializer);

				// Name einlesen
				final int nameSize = deserializer.readUnsignedByte();
				final String name;
				if(nameSize > 0) {
					name = deserializer.readString(255);
				}
				else {
					
					name = deserializer.readString(0);
				}
//				final String name = readString(deserializer);

				// Es stehen nun alle Informationen zur Verf�gung, um ein Objekt zu erzeugen.
				// An dieses Objekt werden dann alle Mengen hinzugef�gt.
				final ConfigurationObjectInformation newConfObject = new ConfigurationObjectInformation(
						id, pid, typeId, name, (short)firstValid, (short)firstInvalid, this, false
				);
				// Am Objekt speichern, wo es in der Datei zu finden ist
				newConfObject.setLastFilePosition(filePosition);

				// konfigurierende Datens�tze einlesen und direkt an dem Objekt hinzuf�gen

				// Menge der konfigurierenden Datens�tze
				final int numberOfConfigurationData = deserializer.readInt();
				for(int nr = 0; nr < numberOfConfigurationData; nr++) {
					// ATG-Verwendung einlesen
					final long atgUseId = deserializer.readLong();
					// L�nge der Daten
					final int sizeOfData = deserializer.readInt();
					final byte[] data = deserializer.readBytes(sizeOfData);
					newConfObject.setConfigurationData(atgUseId, data);
				}

				// alle Daten einlesen, die spezifisch f�r ein Konfigurationsobjekt sind und
				// direkt am Objekt hinzuf�gen

				// Anzahl Mengen am Objekt
				final short numberOfSets = deserializer.readShort();

				for(int nr = 0; nr < numberOfSets; nr++) {
					final long setId = deserializer.readLong();
					newConfObject.addObjectSetId(setId);
					final int numberOfObjects = deserializer.readInt();

					for(int i = 0; i < numberOfObjects; i++) {
						// Alle Objekte der Menge einlesen

						// Id des Objekts, das sich in Menge befinden
						final long setObjectId = deserializer.readLong();
						newConfObject.addObjectSetObject(setId, setObjectId);
					}
				}

				// Das Objekt wurde geladen, also k�nnen ab jetzt alle �nderungen gespeichert werden
				newConfObject.saveObjectModifications();
				return newConfObject;
			}
			else if(objecttype == 1) {
				// Ein dynamisches Objekt einlesen, die Simulationsvariante wurde noch nicht eingelesen, aber der fileDesc.
				// steht sofort auf dem Wert
				final short simulationVariant = file.readShort();

				// Der vordere Teil ist konstant, also kann die L�nge der gepackten Daten berechnet werden.
				// id, pidHash, typeId, type(Konf oder dynamisch), Zeitstempel, Zeitstempel, Simulationsvariante abziehen
				final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 8 - 8 - 2;
				final byte packedBytes[] = new byte[sizeOfPackedData];
				file.readFully(packedBytes);

				// Byte-Array, das die ungepackten Daten enth�lt
				final byte[] unpackedBytes = unzip(packedBytes);

				final InputStream in = new ByteArrayInputStream(unpackedBytes);

				//deserialisieren
				final Deserializer deserializer = SerializingFactory.createDeserializer(_serializerVersion, in);

				// Das serialisierte SystemObjektInfo einlesen

				// Pid einlesen
				final int pidSize = deserializer.readUnsignedByte();
				final String pid;
				if(pidSize > 0) {
					pid = deserializer.readString(255);
				}
				else {
					
					pid = deserializer.readString(0);
				}
//				final String pid = readString(deserializer);

				// Name einlesen
				final int nameSize = deserializer.readUnsignedByte();
				final String name;
				if(nameSize > 0) {
					name = deserializer.readString(255);
				}
				else {
					
					name = deserializer.readString(0);
				}
//				final String name = readString(deserializer);

				// Es stehen nun alle Informationen zur Verf�gung, um ein Objekt zu erzeugen.
				// An dieses Objekt werden dann alle Mengen hinzugef�gt.
				final DynamicObjectInformation newDynObject = new DynamicObjectInformation(
						id, pid, typeId, name, simulationVariant, firstValid, firstInvalid, this, false
				);
				// Am Objekt speichern, wo es in der Datei zu finden ist
				newDynObject.setLastFilePosition(filePosition);

				// konfigurierende Datens�tze einlesen und direkt an dem Objekt hinzuf�gen

				// Menge der konfigurierenden Datens�tze
				final int numberOfConfigurationData = deserializer.readInt();
				for(int nr = 0; nr < numberOfConfigurationData; nr++) {
					// ATG-Verwendung einlesen
					final long atgUseId = deserializer.readLong();
					// L�nge der Daten
					final int sizeOfData = deserializer.readInt();
					final byte[] data = deserializer.readBytes(sizeOfData);
					newDynObject.setConfigurationData(atgUseId, data);
				}

				// Das Objekt wurde geladen, ab jetzt d�rfen alle �nderungen gespeichert werden
				newDynObject.saveObjectModifications();
				return newDynObject;
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
	 * Lie�t zuerst die L�nge(byte) des folgenden String aus und anschlie�end den String.
	 *
	 * Ist die L�nge des String negativ, so wird dieser negative Wert in einen positiven Wert umgerechnet. Allerdings darf dabei die untere Grenze von
	 * -128 nicht unterschritten werden.
	 *
	 * @param deserializer Objekt, aus dem die Daten ausgelesen werden
	 * @return String, der ausgelesen wurde
	 * @throws IOException Fehler beim Zugriff auf den Deserialisierer.
	 */
//	private String readString(Deserializer deserializer) throws IOException {
//		// L�nge des Strings einlesen
//		int sizeOfString = deserializer.readByte();
//		if(sizeOfString < 0){
//			sizeOfString = sizeOfString + 256;
//			if(sizeOfString < 0) throw new IllegalArgumentException("Ein String kann nicht ausgelesen werden, da die L�nge des Strings 255 �berschreitet.");
//		}
//		final String string = deserializer.readString(sizeOfString);
//		return string;
//	}

	/**
	 * Speichert ein Objekt ans Ende der �bergebenen Datei.
	 * <p/>
	 * Die Datei wird nach dem Schreibzugriff nicht geschlossen und die neue Dateiposition wird am Objekt vermerkt.
	 *
	 * @param dynamicObject      Objekt, das gesichert werden soll
	 * @param file               Datei, in der das Objekt gespeichert werden soll. Das Objekt wird ans Ende der Datei geschrieben
	 * @param declareGap         true = (Normalfall) Das Objekt wird gespeichert, es existiert aber noch eine �ltere Version des Objekts in der Datei und diese
	 *                           wird als L�cke deklariert (ID wird auf 0 gesetzt); false = (Anlegen einer neuen Konfigurationsbereichsdatei oder bei
	 *                           Reorganisation) Das Objekt wird in der Datei gespeichert (es wird keine L�cke erzeugt). Dies ist aber die erste Version des
	 *                           Objekts in der Datei und somit gibt es auch keinen Vorg�nger der als L�cke deklariert werden soll
	 * @param setNewFilePosition true = (Normalfall) Das Objekt wird in der Datei gespeichert und die neue Position in der Datei wird am Objekt, das sich im
	 *                           Speicher befindet, gespeichert; false = Das Objekt wird in der Datei gespeichert, aber die neue Dateiposition wird nicht am Objekt
	 *                           gespeichert sondern muss nachtr�glich am Objekt, das sich im Speicher befindet, gesetzt werden. Ein Beispiel w�re die
	 *                           Reorganisation. Das Objekt wird in der neue Datei gespeichert, aber die Dateiposition wird am Objekt im Speicher erst dann
	 *                           gesetzt, wenn die Reorganisation erfolgreich abgeschlossen werden konnte.
	 *
	 * @return Position in der Datei, an der das Objekt gespeichert wurde
	 *
	 * @throws IOException           Fehler beim schreiben in die Datei, Teile des Datensatzes wurden bereits geschrieben
	 * @throws IllegalStateException Fehler beim sammeln der Daten, die Datei wurde noch nicht ver�ndert
	 */
	private long writeDynamicObjectToFile(DynamicObjectInformation dynamicObject, final RandomAccessFile file, boolean declareGap, boolean setNewFilePosition)
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
				// Das Objekt ist derzeit f�r alle Zugriffe gesperrt, also werden alle Informationen angefordert.
				// So ist sichergestellt, das sie sich nach freigabe des Objekts nicht �ndern.
				// Daten die sich nicht mehr �ndern werden ebenfalls lokal gespeichert, damit sie nicht beim
				// schreiben angefordert werden m�ssen und dabei eventuell blockieren und somit die ganze Datei sperren.

				id = dynamicObject.getID();
				pidHashCode = dynamicObject.getPid().hashCode();
				typeId = dynamicObject.getTypeId();
				firstInvalidTime = dynamicObject.getFirstInvalidTime();
				firstValidTime = dynamicObject.getFirstValidTime();
				simulationVariant = dynamicObject.getSimulationVariant();

				// Als n�chstes werden die Daten des SystemObjects bearbeitet (alles, bis auf die Id wird eingetragen)
				writeSystemObjectWithoutIdAndTypeId(dynamicObject, serializer);
			} // synch

			// Im Serializer liegen nun alle Daten, die geschrieben werden sollen, diese als Byte-Array anfordern
			// und packen. Danach ist die L�nge des Objekts bekannt.
			packedData = zip(out.toByteArray());
		}
		catch(Exception e) {
			_debug.error("Id: " + dynamicObject.getID() + " Serialisiererversion: " + _serializerVersion, e);
			throw new IllegalStateException(
					"Die Daten eines dynamischen Objekts k�nnen nicht serialisiert werden, das Objekt wird nicht gespeichert: Id" + dynamicObject.getID()
			);
		}

		// Die L�nge kann berechnet werden
		// Id + HashCode + TypeId + (dyn oder konf) + Zeitstempel + Zeitstempel + Simulationsvariante + gepackte Daten
		final int dynamicObjectSize = 8 + 4 + 8 + 1 + 8 + 8 + 2 + packedData.length;

		// Datei sperren
		synchronized(_configAreaFile) {
			// Das dyn. Objekt ans Ende der Datei schreiben
			// Die neue Dateiposition speichern, nach erfolgreichen schreiben wird die Position am Objekt vermerkt
			final long newObjectFilePosition = file.length();

			// filedescriptor positionieren, n�tig, weil der Descriptor nicht am Ender der Datei stehen muss, auch wenn
			// vorher ein Objekt geschrieben wurde (das alte Objekt wurde als L�cke markiert, also steht der FD mitten in der Datei)
			file.seek(newObjectFilePosition);

			// L�nge der Daten, int
			// Id, long
			// hashCode der Pid, int
			// typeId des Objekts, long
			// Kennzeichnung dynamisches Objekt (1), byte
			// Zeitstempel nicht mehr g�ltig ab, long
			// Zeitstempel g�ltig ab, long
			// Simulationsvariante, short

			file.writeInt(dynamicObjectSize);
			file.writeLong(id);
			file.writeInt(pidHashCode);
			file.writeLong(typeId);
			file.writeByte(1);
			file.writeLong(firstInvalidTime);
			file.writeLong(firstValidTime);
			file.writeShort(simulationVariant);

			// Jetzt k�nnen die gepackten Daten gespeichert werden.
			// Die Anzahl Bytes, die zu diesem Array geh�ren, k�nnen beim einlesen des Daten aus der L�nge
			// des gesamten Objekts berechnet werden, da der ungepackte Teil konstant ist.
			file.write(packedData);

			// Soll eine L�cke erzeugt werden
			if(declareGap) {
				// Die Vorg�ngerversion in der Datei als L�cke deklarieren (ID auf 0)
				final long lastFilePosition = dynamicObject.getLastFilePosition();
				declareObjectAsAGap(lastFilePosition, file);
			}
			if(setNewFilePosition) {
				// Das Objekt wurde gespeichert, also muss die neue Dateiposition am Objekt, das sich im Speicher befindet,
				// neu gesetzt werden.
				dynamicObject.setLastFilePosition(newObjectFilePosition);
			}

			return newObjectFilePosition;
		}// synch
	}

	/**
	 * Speichert ein Objekt ans Ende der �bergebenen Datei. Die Datei wird nach dem Schreibzugriff nicht geschlossen. Die Dateiposition wird am Objekt
	 * aktualisiert.
	 *
	 * @param configurationObject Objekt, das gespeichert werden soll
	 * @param file                Datei, in der das Objekt gespeichert werden soll.
	 * @param declareGap          true = (Normalfall) Das Objekt wird gespeichert, es existiert aber noch eine �ltere Version des Objekts in der Datei und diese
	 *                            wird als L�cke deklariert (ID wird auf 0 gesetzt); false = (Anlegen einer neuen Konfigurationsbereichsdatei oder bei
	 *                            Reorganisation) Das Objekt wird in der Datei gespeichert (es wird keine L�cke erzeugt). Dies ist aber die erste Version des
	 *                            Objekts in der Datei und somit gibt es auch keinen Vorg�nger der als L�cke deklariert werden soll
	 * @param setNewFilePosition  true = (Normalfall) Das Objekt wird in der Datei gespeichert und die neue Position in der Datei wird am Objekt, das sich im
	 *                            Speicher befindet, gespeichert; false = Das Objekt wird in der Datei gespeichert, aber die neue Dateiposition wird nicht am
	 *                            Objekt gespeichert sondern muss nachtr�glich am Objekt, das sich im Speicher befindet, gesetzt werden. Ein Beispiel w�re die
	 *                            Reorganisation. Das Objekt wird in der neue Datei gespeichert, aber die Dateiposition wird am Objekt im Speicher erst dann
	 *                            gesetzt, wenn die Reorganisation erfolgreich abgeschlossen werden konnte.
	 *
	 * @return Position in der Datei, an der das Objekt gespeichert wurde
	 *
	 * @throws IOException           Fehler beim schreiben in die Datei, Teile des Datensatzes wurden bereits geschrieben
	 * @throws IllegalStateException Fehler beim sammeln der Daten, die Datei wurde noch nicht ver�ndert
	 */
	private long writeConfigurationObjectToFile(
			ConfigurationObjectInformation configurationObject, final RandomAccessFile file, boolean declareGap, boolean setNewFilePosition
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
				// Das Objekt ist derzeit f�r alle Zugriffe gesperrt, also werden alle Informationen angefordert.
				// So ist sichergestellt, das sie sich nach freigabe des Objekts nicht �ndern.
				// Daten die sich nicht mehr �ndern werden ebenfalls lokal gespeichert, damit sie nicht beim
				// schreiben angefordert werden m�ssen und dabei eventuell blockieren und somit die ganze Datei sperren.

				id = configurationObject.getID();
				pidHashCode = configurationObject.getPid().hashCode();
				typeId = configurationObject.getTypeId();
				firstInvalidVersion = configurationObject.getFirstInvalidVersion();
				firstValidVersion = configurationObject.getFirstValidVersion();

				// Die Version, in der ein Objekt g�ltig werden soll, muss Gr��er 0 sein.
				if(firstValidVersion <= 0) {
					throw new IllegalStateException(
							"Ein Konfigurationsobjekt " + configurationObject.getPid() + " ist in einer Version kleiner 1 g�ltig. Dies ist nicht erlaubt."
					);
				}

				// Als n�chstes werden die Daten des SystemObjects bearbeitet (alles, bis auf die Id wird eingetragen)
				writeSystemObjectWithoutIdAndTypeId(configurationObject, serializer);

				// Alle Daten, die speziell f�r ein Konfigurationsbjekt sind und noch fehlen

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

				for(int nr = 0; nr < objectSets.length; nr++) {
					final long objectSetId = objectSets[nr];
					serializer.writeLong(objectSetId);

					// Objekte der Menge anfordern
					final long objects[] = configurationObject.getObjectSetObjects(objectSetId);

					// Anzahl Elemente der Menge speichern
					serializer.writeInt(objects.length);

					for(int i = 0; i < objects.length; i++) {
						// Objekte der Menge speichern
						serializer.writeLong(objects[i]);
					}
				}
			} // synch

			// Im Serializer liegen nun alle Daten, die geschrieben werden sollen, diese als Byte-Array anfordern
			// und packen. Danach ist die L�nge des Objekts bekannt.
			packedData = zip(out.toByteArray());
		}
		catch(Exception e) {
			_debug.error("Id: " + configurationObject.getID() + " Serialisiererversion: " + _serializerVersion, e);
			throw new IllegalStateException(
					"Die Daten eines dynamischen Objekts k�nnen nicht serialisiert werden, das Objekt wird nicht gespeichert: Id" + configurationObject.getID()
			);
		}

		// Die L�nge kann berechnet werden
		// Id + HashCode + typeId + (dyn oder konf) + Version + Version + gepackte Daten
		final int dynamicObjectSize = 8 + 4 + 8 + 1 + 2 + 2 + packedData.length;

		// Datei sperren
		synchronized(_configAreaFile) {
			// Das dyn. Objekt ans Ende der Datei schreiben
			// Die neue Dateiposition speichern, nach erfolgreichen schreiben wird die Position am Objekt vermerkt
			final long newObjectFilePosition = file.length();

			// filedescriptor positionieren, n�tig, weil der Descriptor nicht am Ender der Datei stehen muss, auch wenn
			// vorher ein Objekt geschrieben wurde (das alte Objekt wurde als L�cke markiert, also steht der FD mitten in der Datei)
			file.seek(newObjectFilePosition);

			// L�nge der Daten, int
			// Id, long
			// hashCode der Pid, int
			// typeId des Objekts
			// Kennzeichnung Konfigurationsobjekt(0), byte
			// Version nicht mehr g�ltig ab, short
			// Version g�ltig ab, short

			file.writeInt(dynamicObjectSize);
			file.writeLong(id);
			file.writeInt(pidHashCode);
			file.writeLong(typeId);
			file.writeByte(0);
			file.writeShort(firstInvalidVersion);
			file.writeShort(firstValidVersion);

			// Jetzt k�nnen die gepackten Daten gespeichert werden.
			// Die anzahl Bytes, die zu diesem Array geh�ren, k�nnen beim einlesen des Daten aus der L�nge
			// des gesamten Objekts berechnet werden, da der ungepackte Teil konstant ist.
			file.write(packedData);

			// Soll eine L�cke erzeugt werden
			if(declareGap) {
				// Die Vorg�ngerversion in der Datei als L�cke deklarieren (ID auf 0)
				final long lastFilePosition = configurationObject.getLastFilePosition();
				declareObjectAsAGap(lastFilePosition, file);
			}
			// Soll die neue Position am Objekt im Speicher gespeichert werden
			if(setNewFilePosition) {
				// Das Objekt wurde gespeichert, also muss die neue Dateiposition am Objekt, das sich im Speicher befindet,
				// neu gesetzt werden.
				configurationObject.setLastFilePosition(newObjectFilePosition);
			}
			return newObjectFilePosition;
		} // synch
	}

	/**
	 * Setzt die Id eines Objekts auf 0 und erkl�rt es somit als L�cke. Es gibt keinen unterschied zwischen dynamischen Objekten und Konfigurationsobjekten
	 *
	 * @param filePosition Position des Objekts (L�nge des Objekts). Der Wert -1 bedeutet, dass dieses Objekt noch nie gespeichert wurde. Es muss also kein altes
	 *                     Objekt als L�cke definiert werden
	 * @param file         Dateiobjekt, mit dem auf die Platte zugegriffen werden kann
	 */
	private void declareObjectAsAGap(final long filePosition, RandomAccessFile file) throws IOException {
//		System.out.println("L�cke einf�gen, Dateiposition: " + filePosition);
		synchronized(_configAreaFile) {
			if(filePosition > 0) {
				// Die Id des Datensatzes steht nach der L�nge, die L�nge ist ein Integer, also kann dieser �bersprungen werden
				file.seek(filePosition + 4);
				// Der Zeiger steht auf der Id des Datensatzes, diese wird nun mit einer 0 �berschrieben und somit als
				// L�cke deklariert
				file.writeLong(0);
			}
		}
	}

	void declareObjectAsAGap(final long filePosition) {
		try {
			synchronized(_configAreaFile) {
				final RandomAccessFile configAreaFile = new RandomAccessFile(_configAreaFile, "rw");
				try {
					declareObjectAsAGap(filePosition, configAreaFile);
				}
				finally {
					configAreaFile.close();
				}
			}
		}
		catch(IOException e) {
			_debug.warning("Fehler beim L�schen eines Konfigurationsobjekts", e);
		}
	}

	/**
	 * Packt ein Byte-Array mit dem Packer "ZIP" und gibt die Daten gepackt zur�ck
	 *
	 * @param data ungepackte Daten
	 *
	 * @return gepackte Daten
	 */
	private byte[] zip(byte[] data) throws IOException {

		ByteArrayOutputStream packedData = new ByteArrayOutputStream();
		DeflaterOutputStream zipper = new DeflaterOutputStream(packedData);

		// Packen
		zipper.write(data);
		zipper.close();

		return packedData.toByteArray();
	}

	private byte[] unzip(byte[] zippedData) {

		ByteArrayInputStream inputStream = new ByteArrayInputStream(zippedData);
		InflaterInputStream unzip = new InflaterInputStream(inputStream);
		// In diesem Stream werden die entpackten Daten gespeichert
		ByteArrayOutputStream unzippedData = new ByteArrayOutputStream();

		try {
			// Die ungepackten Daten
			int unpackedData = unzip.read();

			while(unpackedData != -1) {
				unzippedData.write(unpackedData);
				unpackedData = unzip.read();
			}
			
			unzip.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return unzippedData.toByteArray();
	}

	/**
	 * Speichert die Pid, den Namen des SystemObjects und alle konfigurierenden Datens�tze .
	 *
	 * @param object     Objekt, von dem nur gewissen Teile gespeichert werden
	 * @param serializer Byte-Strom, in den die Daten eingef�gt werden
	 */
	private void writeSystemObjectWithoutIdAndTypeId(final SystemObjectInformationInterface object, Serializer serializer) throws IOException {

		// Pid, byte,String
		// Name, byte, String
		// konfigurierende Datens�tze (Erkl�rung weiter unten)

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

		// konfigurierende Datens�tze speichern

		// Anzahl Datens�tze, int
		// je Datensatz:
		//		Atg-Verwendung, long
		//		L�nge des serialisierten Datensatzes, int
		//		Datensatz als Byte-Array

		// Id�s der konfigurierenden Datens�tze anfordern
		final long configDataSetIds[] = object.getConfigurationsDataAttributeGroupUsageIds();

		// Anzahl Datens�tze ist nun bekannt
		serializer.writeInt(configDataSetIds.length);


		for(int nr = 0; nr < configDataSetIds.length; nr++) {
			final long atgUserId = configDataSetIds[nr];
			final byte[] dataSet = object.getConfigurationData(atgUserId);

			// Atg-Verwendung
			serializer.writeLong(atgUserId);
			// L�nge des serialisierten Datensatzes
			serializer.writeInt(dataSet.length);
			// Den serialisierten Datensatz schreiben
			serializer.writeBytes(dataSet);
		}
	}

	short getActiveVersion() {
		return _activeVersion;
	}


	/**
	 * Diese Methode wird von einem Objekt aufgerufen, wenn Informationen des Objekts ge�ndert wurden. Das gesamte Objekt wird persistent gespeichert, sobald die
	 * Methode {@link ConfigurationFileManager#saveConfigurationAreaFiles()} aufgerufen wird. Die Methode kann mehrfach vom selben Objekt aufgerufen werden, es
	 * wird nur einmal gespeichert.
	 * <p/>
	 * Es muss au�erdem gepr�ft werden, ob das Objekt Auswirkungen auf die drei Zeitstempel _dynamicObjectChanged,_configurationDataChanged,_configurationObjectChanged
	 * hat (siehe TPuK1-51).
	 *
	 * @param modifiedObject dynamisches Objekt oder Konfigurationsobjekt, dessen Informationen ge�ndert wurden und das somit persistent in der Datei des
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
	 * Diese Methode wird aufgerufen, wenn ein dynamisches Objekt auf "ung�ltig" gesetzt wird. Die Methode wird vom Objekt selber aufgerufen und aktualisiert alle
	 * Datenstrukturen, die das Objekt als "g�ltig" f�hren. Konfigurationsobjekte m�ssen nicht beachtet werden, da diese erst in der "n�chsten" Version
	 * ung�ltig/g�ltig werden. Dies wird beim einlesen der Datei erkannt und die Datenstrukturen werden sofort richtig angelegt. Die Methode ist blockierend, wenn
	 * eine Reorganisation stattfindet.
	 *
	 * @param invalidObject Objekt, das "ung�ltig" geworden ist
	 */
	void setDynamicObjectInvalid(DynamicObjectInformation invalidObject) {
		synchronized(_restructureLock) {
			// Das Objekt aus alle Datenstrukturen entfernen, in denen es als g�ltig gespeichert ist
			synchronized(_actualObjects) {
				_actualObjects.remove(invalidObject.getID());
			}

			synchronized(_oldObjectsId) {

				final OldObjectIdReference oldObjectIdReference;

				if(invalidObject.getPersPersistenceMode() == DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
					oldObjectIdReference = new OldObjectIdReference(invalidObject);
				}
				else {
					oldObjectIdReference = new OldObjectIdReference(invalidObject.getLastFilePosition());
				}

				_oldObjectsId.put(invalidObject.getID(), oldObjectIdReference);
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
				final OldObjectIdReference helper;
				// Wenn es ein transientes Objekt ist, dann bleibt es im Speicher und wird nicht in die Datei geschrieben -> Es kann auch nicht geladen werden
				if(invalidObject.getPersPersistenceMode() != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
					helper = new OldObjectIdReference(invalidObject.getLastFilePosition());
				}
				else {
					helper = new OldObjectIdReference(invalidObject);
				}

				// In die Map mit alten Objekten eintragen
				putOldObjectTypeId(
						invalidObject.getTypeId(), invalidObject.getFirstValidTime(), invalidObject.getFirstInvalidTime(), false, helper
				);
			}

			// Maps des FileManagers aufr�umen
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
	public boolean initialVersionRestructure() {
		// Die lokale Zuordnung von Version zu Zeitstempel ist bei diesem Aufruf auch gleichzeitig die Zuordnung
		// des Konfigurationsverantwortlichen.

		// Zu diesem Zeitpunkt wurde der Header bereits eingelesen die "alte" Zuordung von Version/Zeitpunkt ist
		// bekannt und muss nun um die neuen Werte erweitert werden.

		synchronized(_restructureLock) {
			boolean restructureNeeded = false;
			final Set<Short> localKeys = _localVersionActivationTime.keySet();
			for(Iterator<Short> iterator = localKeys.iterator(); iterator.hasNext();) {
				final Short localKey = iterator.next();
				// In der alten Liste pr�fen, ob der Wert vorhanden ist
				if(!_configurationAuthorityVersionActivationTime.containsKey(localKey)) {
					// Neues Version/Zeitpunkt Paar einf�gen
					_configurationAuthorityVersionActivationTime.put(localKey, _localVersionActivationTime.get(localKey));
					_globalActivationTimes = getActivationTimeArray(_configurationAuthorityVersionActivationTime);
					restructureNeeded = true;
				}
				else {
					// Diese �berpr�fung braucht nicht stattfinden, kann aber auf Fehler und manuelle �nderungen in der Verwaltungsdatei hinweisen, wenn
					// die �bergebenen Werte nicht mit den gespeicherten �bereinstimmen
					final long oldValue = _configurationAuthorityVersionActivationTime.get(localKey);
					final long newValue = _localVersionActivationTime.get(localKey);

					if(oldValue != newValue) {
						// Die Werte stimmen nicht �berein
						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						String oldDateString = dateFormat.format(oldValue);
						String newDateString = dateFormat.format(newValue);
						_debug.info(
								"Der gespeicherte Aktivierungszeitpunkt (" + oldDateString + ") des Bereichs " + _configAreaFile + " f�r die Version "
								+ localKey + " stimmt nicht mit dem Aktivierungszeitpunkt in der Verwaltungsdatei (" + newDateString + ") �berein."
						);
					}
				}
			}
			// Datei restrukturieren damit die neue Zuweisung von Version zu einem Zeitstempel im Header gespeichert wird.
			if(restructureNeeded) {
				return restructure();
			}
			_debug.info("Eine erneute Restrukturierung des Bereichs " + _configAreaFile + " ist nicht erforderlich.");
			return true;
		}
	}

	public long getGreatestId() {
		return _greatestId;
	}

	public boolean restructure() {

		// Alle Zugriffe auf alte Objekte, die sich im Speicher befinden, sperren
		synchronized(_restructureLock) {
			// Datei sperren, so werden m�gliche Dateizugriffe solange blockiert, bis die neue Datei zur Verf�gung steht.
			// Die Dateizugriffe werden dann sofort auf der neuen(reorganisierten) Datei durchgef�hrt.
			synchronized(_configAreaFile) {
				try {

					// Alle Dateioperationen ver�ndern nicht die Objekte, die sich im Speicher befinden.
					// Erst wenn die Reorganisation erfolgreich beendet wurde, werden die Objekte im Speicher
					// auf die neue Situation angepa�t und aus den HashMap�s entfernt und die aktuellen/zuk�nftig aktuellen
					// Objekte auf die neue Dateiposition gesetzt.
					// Kommt es zu einem Fehler bei der Reorganisation, wird die neue fehlerhaft reorganisierte Datei umbenannt
					// (damit sie analysiert werden kann) und es wird auf der alten Datei weitergearbeitet.
					// Damit dieses Verfahren durchgef�hrt werden kann, m�ssen alle Schritte der Reorganisation "protokolliert"
					// werden. Dazu wird eine HashMap und eine Liste(newOldObjectsIdList) angelegt, diese speichern welches Objekt aus dem Speicher gel�scht werden
					// kann und welche Objekte an einer anderen Dateiposition zu finden sind.

					// HashMap, die f�r aktuelle und zuk�nftig aktuelle Objekte die position in der neuen Datei speichert.
					// Dies ist n�tig, damit nach einer erfolgreichen Reorganisation die Elemente im Speicher auf den
					// neusten Stand gebracht werden k�nnen.
					// Als Schl�ssel dient die neue Dateiposition.
					// Als value dient das Objekt, das ge�ndert werden soll.
					// Es wird kein "richtiger" Schl�ssel gebraucht, da die Liste am Ende ganz durchlaufen wird und der Schluessel
					// dem value zugewiesen wird.
					final Map<Long, SystemObjectInformationInterface> memoryObjectsNewFilePositionMap = new HashMap<Long, SystemObjectInformationInterface>();

					// Es ist derzeit nicht m�glich ein dyn Objekt w�hrend der Reorganisation auf invalid zu setzen.

					// Speichert(Id der Objekte), welche alten Objekte in einen nGa/dyn nGa Bereich verschoben wurden. Diese Liste ist n�tig, da
					// w�hrend der Reorganisation neue Elemente in die Map _oldObjectsId geschoben werden k�nnen (dynamische Objekte).
					// Also darf die Map _oldObjectsId nicht generell gel�scht werden, sondern nur die Objekte, die sich in dieser Liste
					// befinden.

					// Ende des Header
					final long newAbsoluteEndHeader;
					// Bereich, in dem alle ung�ltigen Konfigurationsobjekte gespeichert sind
					final long newRelativeOldObjectArea;
					// relative Anfangsposition der Menge, die alle alten dynamischen Objekte enth�lt
					final long newRelativeDynObjectArea;
					// relative Anfangsposition des Id Indizes
					final long newRelativeIdIndex;
					// relative Anfangsposition des Pid Indizes
					final long newRelativePidIndex;
					// relative Anfangsposition der Mischobjektmenge
					final long newRelativeMixedSet;


					RandomAccessFile oldConfigAreaFile = new RandomAccessFile(_configAreaFile, "r");

					// Alle Daten, die sich ge�ndert haben, speichern
					flush();

					// Die neue Datei, in der der Konfigurationsbereich reorganisiert wird
					File configAreaNewName = new File(_configAreaFile.getAbsolutePath() + "New");

					RandomAccessFile newConfigAreaFile = new RandomAccessFile(configAreaNewName, "rw");

					// Try/finally f�r beide Dateien
					try {
						// Einen Pre-Header erzeugen, die relative Dateiposition der neuen nGa Bl�cke ist noch unbekannt, dadurch
						// sind auch die anderen relativen Positionen noch unbekannt. Es wird lediglich Platz reserviert.
						// Falls ein neuer nGa-Block keine Elemente besitzt, wird dieser als relative Dateipostion eine -1 bekommen.

						// Der neue Header setzt sich aus dem alten Header und den neuen nGa Bl�cken zusammen.
						// Der Header w�chst also wegen der neuen nGa Bl�cken.
						// Jeder neue nGa Block, wird mit zwei Longs und einem Short abgebildet.
						// Es gibt soviele neue Bl�cke, wie es Versionen zwischen dem letzten erzeugten nGa Block (Reorganisation)
						// und der jetzigen Version gibt.
						// Die Variable _nextInvalidBlockVersion speichert die erste Version des nGa Blocks, der bei der Reorganisation
						// geschrieben werden muss.

						// Anzahl nGa Bl�cke, die eingef�gt werden m�ssen
						final int numberOfNewOldBlocks;
						if(_nextInvalidBlockVersion > _activeVersion) {
							// Das ist immer dann der Fall, wenn es keinen "neuen" alten Block gibt.
							// zb. beim Neustart des Systems, oder wenn gerade eine Reorganisation stattgefunden hat, aber
							// es gibt keine Konfigurationsobjekte f�r einen neuen alten Block.
							numberOfNewOldBlocks = 0;
						}
						else {
							// Wieviele nGa Bl�cke m�ssen eingef�gt werden ? Soviele, wie Versionen fehlen, bis zur aktuellen Version
							// und zwar einschlie�lich der aktuellen Version (diese Objekte sind in der derzeit aktuellen Version
							// ung�ltig geworden), darum +1
							numberOfNewOldBlocks = (_activeVersion - _nextInvalidBlockVersion) + 1;
						}

//						System.out.println("Neue nGa Bl�cke: " + numberOfNewOldBlocks + " aktive Version: " + _activeVersion + " _nextInvalidBlockVersion " + _nextInvalidBlockVersion);

						// Speichert die Gr��e des neuen Headers
						// Die gr��e des alten Headers setzt sich aus seiner (Endeposition in der Datei) - (L�nge des Headers, Integer) zusammen
						// Das St�ck des neuen Headers ist um 2 Longs und einem Short pro nGa Bereich gr��er
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
						// Nach dem Pre-Header die alten nGa-Bl�cke speichern

						// Anfang der Daten suchen
						final long startOldObjectBlocks;

						if(_oldObjectBlocks.containsKey(2)) {

							startOldObjectBlocks = _oldObjectBlocks.get(2).getFilePosition() + _headerEnd;
						}
						else {
							startOldObjectBlocks = _headerEnd;
						}

						oldConfigAreaFile.seek(startOldObjectBlocks);

						// Es muss zum bis zum Beginn der dynamischen nGa kopiert werden
						while(oldConfigAreaFile.getFilePointer() < (_startOldDynamicObjects + _headerEnd)) {
							newConfigAreaFile.writeByte(oldConfigAreaFile.readByte());
						}

//*********************************************************************************************************
						// Hinter die alten nga-Bl�cke, die neuen nGa-Bl�cke speichern

						newRelativeOldObjectArea = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

						// Zu jeder abgelaufenen Version einzeln die alten Objekte laden und dann in den derzeit betrachteten
						// nGa einf�gen.

						// Es existiert ein Index nach Id. In diesem Index sind alle Objekte nach Id einsortiert, zus�tzlich
						// ist die relative Dateiposition zum Header gespeichert.
						// Damit dieser Index sp�ter aufgebaut werden
						// kann, wird jedes Objekt, das umkopiert wird, in einer Liste gespeichert.
						// Sp�ter werden auch die dynamischen Objekte in diese Liste eingetragen
						final List<SortObject> newOldObjectsIdList = new ArrayList<SortObject>();

						// Es existiert ein Index, in dem sich alle alten Objekte (aus ngA und dynamisch nGa) befinden.
						// Es wird der HashCode der Pid benutzt, als Ergebnis wird eine Liste mit relativen Dateipotionen
						// geliefert. Mit den Dateipositionen kann das "passende" Objekt zu der Pid geladen werden.
						// In dieser Map werden alle Objekte eingetragen, die in die Menge nGa oder dyn nGa eingetragen werden sollen.
						// Als Key dient der HashCode der Pid. R�ckgabe ist eine Liste, in der Liste sind alle Dateipositionen (relativ) aller Objekte enthalten,
						// die ebenfalls den HashCoder der Pid besitzen (wie der Schl�ssel)
						final Map<Integer, SortObjectPid> newOldObjectsPidMap = new HashMap<Integer, SortObjectPid>();

						short consideredOldVersion = _nextInvalidBlockVersion;
						// Es werden alle nGa Bl�cke erzeugt, die es bis zur jetzigen Version gibt.
						_nextInvalidBlockVersion = (short)(_activeVersion + 1);

						synchronized(_oldObjectsId) {
							// ung�ltige Objekte k�nnen auch in der aktiven Version sein, darum <=
							while(consideredOldVersion <= _activeVersion) {

								Long[] keysLong = _oldObjectsId.keySet().toArray(new Long[_oldObjectsId.keySet().size()]);
								// Es wird ein neuer nGa Bereich erzeugt

								// boolean ob ein Element zu dem Block hinzugef�gt wurde. wenn ja, dann dateipostion in map
								// speichern, wenn nein -1 als dateiposition
								// Wenn in einen nGa Bereich keine ung�ltigen Objekte angelegt werden k�nnen, dann bleibt diese
								// Variable false und als relative Startposition des Blocks wird eine -1 eingetragen
								boolean blockHasElements = false;

								// Speichert den relativen Beginn des potentiellen Blocks, der geschrieben werden soll
								final long relativeBlockPosition = (newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader);

								// Speichert den Zeitpunkt, zu dem die Version g�ltig wurde
								final long blockTimeStamp;
								if(_configurationAuthorityVersionActivationTime.containsKey(consideredOldVersion)) {
									blockTimeStamp = _configurationAuthorityVersionActivationTime.get(consideredOldVersion);
								}
								else {
									// Dieser Fall sollte niemals auftreten
									System.out.println(consideredOldVersion);
									_debug.error(
											"Es gibt zu einer alten Version keinen Zeitstempel: Version " + consideredOldVersion + " Konfigurationsbereich: "
											+ _configAreaFile + " . Die Reorganisation wird abgebrochen"
									);
									throw new IllegalStateException(
											"Es gibt zu einer alten Version keinen Zeitstempel: Version " + consideredOldVersion + " Konfigurationsbereich: "
											+ _configAreaFile + " . Die Reorganisation wird abgebrochen"
									);
								}

								for(int nr = 0; nr < keysLong.length; nr++) {
									

									final long idOldObject = keysLong[nr];

									// Das Objekt anfordern, es werden nur Konfigurationsobjekte betrachtet.
									final SystemObjectInformationInterface oldObject = _oldObjectsId.get(idOldObject).getObject();

									// Es werden nur Konfigurationsobjekte betrachtet
									if(oldObject instanceof ConfigurationObjectInfo) {
										final ConfigurationObjectInformation oldConfigObject = (ConfigurationObjectInformation)oldObject;

										if(oldConfigObject.getFirstInvalidVersion() == consideredOldVersion) {
											// Da in der neuen Datei muss ebenfalls ein Id Index angelegt werden muss, muss
											// die neue endg�ltige relative Position im nGa Bereich des Datensatzes gespeichert werden.
											// Die relative Adresse wird als negativer Wert gespeichert, dies erm�glicht bei Indexzugriffen
											// sofort zu erkennen, ob das Objekt ein Konfigurationsobjekt oder ein dynamisches Objekt
											// gefunden wurde.
											// Die negative relative Position bezieht sich auf das Headerende, da es sich
											// um ein Konfigurationsobjekt handelt.
											final long newRelativeObjectPosition = (-1) * ((newConfigAreaFile.getFilePointer() - 4) - headerSizeNewFileArea);
											newOldObjectsIdList.add(new SortObject(newRelativeObjectPosition, oldConfigObject.getID()));

											// Pid Index
											if(newOldObjectsPidMap.containsKey(oldConfigObject.getPid().hashCode())) {
												final SortObjectPid filePositions = newOldObjectsPidMap.get(oldConfigObject.getPid().hashCode());
												filePositions.putFilePosition(newRelativeObjectPosition);
											}
											else {
												// Es gibt noch kein Objekt, also einf�gen
												SortObjectPid filePositions = new SortObjectPid(oldConfigObject.getPid().hashCode());
												filePositions.putFilePosition(newRelativeObjectPosition);
												newOldObjectsPidMap.put(oldConfigObject.getPid().hashCode(), filePositions);
											}

											// Das Objekt muss in den Bereich eingef�gt werden. Es muss keine L�cke deklariert werden
											// auch die Dateiposition muss nicht gespeichert werden.
											writeConfigurationObjectToFile(oldConfigObject, newConfigAreaFile, false, false);
											// Da ein Objekt in den nGa Bereich geschrieben wurde, muss die relative Position
											// im Header unter Kennung 2 gesetzt werden
											blockHasElements = true;
										}
									}
								} // for
								if(blockHasElements) {
									// Es wurden ung�ltige Objekte in den nGa-Bereich eingetragen
									_oldObjectBlocks.put(consideredOldVersion, new OldBlockInformations(relativeBlockPosition, blockTimeStamp));
								}
								else {
									// Es wurden keine Elemente in den nGa-Bereich eingetragen, also gibt es auch
									// keine Startposition an der die Elemte zu finden sind.
									_oldObjectBlocks.put(consideredOldVersion, new OldBlockInformations((long)-1, blockTimeStamp));
								}

								// Es wurden alle Objekte betrachtet, also die n�chste veraltet Version pr�fen
								consideredOldVersion++;
							} // while
						} // synch

//*********************************************************************************************************

						// Den alten dyn Block schreiben und dann die neuen dyn Objekte nach Zeit an den alten Block "ansortieren"

						newRelativeDynObjectArea = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;
						// In der Map _oldObjectsId stehen jetzt nur noch dynamische Objekte

						// Die alten dynamischen Objekte aus der Datei umkopieren
						oldConfigAreaFile.seek(_startOldDynamicObjects + _headerEnd);

						// Diese Variable speichert die neue absolute Startposition des Bereichs, der aller ung�tligen dynamischen Objekte
						// in der neuen Konfigurationsbereichsdatei enth�lt
						final long absoluteStartNewDynamicArea = newConfigAreaFile.getFilePointer();

						
						while(oldConfigAreaFile.getFilePointer() < (_startIdIndex + _headerEnd)) {
							newConfigAreaFile.writeByte(oldConfigAreaFile.readByte());
						}

						// Die dynamischen Objekte der Mischmenge, die ung�ltig sind, speichern.
						// Diese m�ssen aufsteigend nach Invalid-Time sortiert werden

						// Alle dynamischen Objekte, die in Frage kommen, sind in der Map _oldObjectsId gespeichert

						// Array, das zu allen dynamischen Objekten den Zeitstempel enth�lt, wann das Objekt ung�ltig geworden ist
						// und die absolute Position in der Datei um das Objekt sp�ter zu laden.
						final SortObject oldDynamicObjectsLight[];

						synchronized(_oldObjectsId) {
							final Long[] keysLong = _oldObjectsId.keySet().toArray(new Long[_oldObjectsId.keySet().size()]);

							// Liste, in der die Objekte gespeichert werden, diese wird sp�ter in ein Array umgewandelt
							final List<SortObject> dynamicObjects = new LinkedList<SortObject>();

							for(int nr = 0; nr < keysLong.length; nr++) {
								
								long idOldObject = keysLong[nr];

								final SystemObjectInformationInterface oldObject = _oldObjectsId.get(idOldObject).getObject();

								if(oldObject instanceof DynamicObjectInformation) {
									final DynamicObjectInformation dynObject = (DynamicObjectInformation)oldObject;

									// Alle nicht transienten Objekte m�ssen gespeichert werden. Transiente Objekte sind nur im Speicher
									// vorhanden.
									if(dynObject.getPersPersistenceMode() != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
										dynamicObjects.add(new SortObject(dynObject.getLastFilePosition(), dynObject.getFirstInvalidTime()));
									}
								}
							}
							// Array zuweisen
							oldDynamicObjectsLight = dynamicObjects.toArray(new SortObject[dynamicObjects.size()]);
						}

						// In dem Array oldDynamicObjectsLight stehen nun alle dynamischen Objekte mit ihrem Zeitstempel und
						// ihrer Dateiposition zur Verf�gung.
						// Das Array wird jetzt nach dem Zeitstempel sortiert.

						Arrays.sort(
								oldDynamicObjectsLight, new Comparator() {
							public int compare(Object o1, Object o2) {
								SortObject object1 = (SortObject)o1;
								SortObject object2 = (SortObject)o2;
								return object1.compareTo(object2);
							}
						}
						);

						// Array liegt sortiert vor, also k�nnen die dynamischen Objekte in den Bereich geschrieben werden

						for(int nr = 0; nr < oldDynamicObjectsLight.length; nr++) {
							final SortObject dynamicSortObject = oldDynamicObjectsLight[nr];
							final SystemObjectInformation systemObject = loadObjectFromFile(dynamicSortObject.getFilePosition());
							final DynamicObjectInformation oldDynObject = (DynamicObjectInformation)systemObject;

							// Wie auch bei den alten Konfigurationsobjekten, m�ssen die dynamischen Objekte
							// in den Id Index eingetragen werden

							// Die relative Position der Objekte bezieht sich auf den Beginn des dynamischen Bereichs, nicht auf
							// das Headerende. Der Wert wird als positive Zahl gespeichert (Konfigurationsobjekte als negativ, s.o.)
							// Da die Konfigurationsobjekte mit den negativen Zahlen und die dynamischen Objekte
							// mit den positven Zahlen, muss festgelegt werden, zu welchem Bereich die "0" geh�rt.
							// Die "0" geh�rt zu den nGa Objekten geh�rt somit zu den negativen Werten.
							// Also wird jeder relative Position um eins erh�ht (aus 0, wird eine +1 und somit wird diese Zahl
							// im Zusammenhang mit dynamischen Objekten niemals im Index vergeben), wenn das Objekt
							// sp�ter geladen werden muss, kann an dem positiven Wert erkannt werden, dass es sich
							// um ein dynamisches Objekt handelt und die +1 kann wieder abgezogen werden.
							final long newRelativeObjectPosition = (newConfigAreaFile.getFilePointer() - absoluteStartNewDynamicArea) + 1;

							// Id
							newOldObjectsIdList.add(new SortObject(newRelativeObjectPosition, oldDynObject.getID()));

							// Pid Index
							if(newOldObjectsPidMap.containsKey(oldDynObject.getPid().hashCode())) {
								SortObjectPid filePositions = newOldObjectsPidMap.get(oldDynObject.getPid().hashCode());

								filePositions.putFilePosition(newRelativeObjectPosition);
							}
							else {
								// Es gibt noch kein Objekt, also einf�gen
								final SortObjectPid filePositions = new SortObjectPid(oldDynObject.getPid().hashCode());
								filePositions.putFilePosition(newRelativeObjectPosition);
								newOldObjectsPidMap.put(oldDynObject.getPid().hashCode(), filePositions);
							}

							// Objekt in der neuen Datei schreiben. Es muss keine L�cke deklariert werden und die
							// Dateiposition am Objekt ist egal
							writeDynamicObjectToFile(oldDynObject, newConfigAreaFile, false, false);
						}

						// Der neue "ung�ltige" dynamische Block wurde erzeugt
//*********************************************************************************************************

						// Den alten Index (Id) einlesen und die neues Objekte aus den nGa Bl�cken + neue dyn Objekte
						// nach Id einsortieren (Id + relative Dateipostion zum Header)

						newRelativeIdIndex = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

						// Der alte Index liegt bereits sortiert in der Datei vor.
						// Also wird die Liste, die die neuen Id�s enth�lt, sortiert (nun liegen beide sortiert vor).
						// Jetzt wird der erste Wert aus der Datei eingelesen (dies ist der kleinste Wert) und mit
						// dem ersten Wert der Liste verglichen. Der kleinere von beiden wird geschrieben. Dann wird
						// der n�chste Wert betrachtet, usw (wie Mergesort).

						final SortObject[] newOldObjectSortedArray = newOldObjectsIdList.toArray(new SortObject[newOldObjectsIdList.size()]);
						Arrays.sort(
								newOldObjectSortedArray, new Comparator<SortObject>() {
							public int compare(SortObject o1, SortObject o2) {
								return o1.compareTo(o2);
							}
						}
						);

						// Filedescriptor auf den Id-Index legen
						oldConfigAreaFile.seek(_startIdIndex + _headerEnd);

						// Bis zu dieser Position m�ssen Daten gelesen werden
						final long endIdIndexBlock = _startPidHashCodeIndex + _headerEnd;

						// Gibt an, ob alle Ids gemischt wurden (die neuen alten unter die alten Id�s)
						boolean allIdsMerged = false;

						// Speichert die Id aus der alten Datei. Die Variable wird mit Max initialisiert, f�r den Falls
						// das es keinen neuen Wert beim Start gibt
						long idOldConfigFile = Long.MAX_VALUE;
						// Speichert die relative Dateiposition aus der alten Datei.
						// -1, damit ein Fehler, der wie auch immer beim Start des Algorithmus entstanden ist,
						// nicht in die Datei geschrieben werden kann.
						long relativeFilePositionOldConfigFile = -1;
						// Wenn alle alten Objekte aus der alten Datei einsortiert sind
						boolean oldIdsMerged = false;
						// Speichert, ob die Werte idOldConfigFile, relativeFilePositionOldConfigFile benutzt wurden.
						// Wenn ja, dann m�ssen die n�chsten Werte geladen werden
						boolean oldFileObjectUsed = true;

						// Objekt, das gerade aus dem Array newOldObjectSortedArray betrachtet wird
						int actualIdMergeObject = 0;
						// Objekt aus dem Array newOldObjectSortedArray (Es ist ein altes Objekt, aber es befand sich noch nicht in der Datei).
						// Die Startwerte sind wo gew�hlt, das ein Fehler in der Initialisierung nicht in die Datei
						// geschrieben wird und das alle SortObjekte ignoriert werden w�rden.
						// Die gerade gesetzten Werte werden  beim Start des Algorithmus sofort neu gesetzt.
						SortObject newOldIdObject = new SortObject(-1, Long.MAX_VALUE);
						// Wenn alle neuen alten Id�s einsortiert sind
						boolean newOldIdsMerged = false;

						// Falls true, dann muss das n�chste Objekt geholt werden, da das letzte Objekt gespeichert wurde
						boolean newOldObjectUsed = true;

						while(allIdsMerged == false) {

							// Soll ein neuer Wert aus der alten Datei ausgelesen werden.
							if((oldConfigAreaFile.getFilePointer() < endIdIndexBlock) && oldFileObjectUsed) {
								// Es gibt noch Daten, die eingelesen werden m�ssen und der alte Wert wurde
								// einsortiert, also die n�chsten Daten laden
								idOldConfigFile = oldConfigAreaFile.readLong();
								relativeFilePositionOldConfigFile = oldConfigAreaFile.readLong();

								// Die Werte wurden geladen, also stehen neue Werte zur Verf�gung
								oldFileObjectUsed = false;
							}
							else if(oldFileObjectUsed) {
								// Es gibt keine Werte mehr, die aus der alten Datei kommen.

								// Durch diesen hohen Wert, werden immer die Werte aus dem Array benutzt und der Algorithmus
								// kann normal terminieren.
								idOldConfigFile = Long.MAX_VALUE;
								relativeFilePositionOldConfigFile = -1;
								oldFileObjectUsed = false;
								// Die Id-Menge ist fertig
								oldIdsMerged = true;
							}

							if(actualIdMergeObject < newOldObjectSortedArray.length && newOldObjectUsed) {
								// Es gibt noch Daten und der alte Wert wurde einsortiert
								newOldIdObject = newOldObjectSortedArray[actualIdMergeObject];
								actualIdMergeObject++;
								newOldObjectUsed = false;
							}
							else if(newOldObjectUsed) {
								// Alle Objekte aus dem Array wurden gespeichert (eingemischt), jetzt kann der Rest aus der alten Datei
								// kopiert werden.

								newOldIdObject = new SortObject(-1, Long.MAX_VALUE);
								newOldObjectUsed = false;
								// Das Array wurde bearbeitet
								newOldIdsMerged = true;
							}

							if((newOldIdsMerged == false || oldIdsMerged == false)) {
								// An dieser Stelle stehen zwei Werte zur Verf�gung, die eingemischt werden k�nnen
								if(idOldConfigFile < newOldIdObject.getValue()) {
									newConfigAreaFile.writeLong(idOldConfigFile);
									newConfigAreaFile.writeLong(relativeFilePositionOldConfigFile);
									// Dadurch wird ein neuer Wert angefordert
									oldFileObjectUsed = true;
								}
								else {
									// Id schreiben
									newConfigAreaFile.writeLong(newOldIdObject.getValue());
									// Relative Dateiposition
									newConfigAreaFile.writeLong(newOldIdObject.getFilePosition());
									// n�chsten Wert anfordern
									newOldObjectUsed = true;
								}
							}
							else {
								// Es gibt keine Werte mehr
								allIdsMerged = true;
							}
						} // while(oldIdsMerged)

						// Alle Id�s sind geschrieben
//*********************************************************************************************************
						// Pid Index, ebenfalls die nGa Bl�cke und neue dyn Objekte einf�gen
						// (hashCode Pid und relatve Dateipostion, Achtung pro PidHash kann es mehrer Dateipositionen geben)

						newRelativePidIndex = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

						// Es steht eine Map zur Verf�gung (newOldObjectsPidMap), die alle neuen Elemente enth�lt, die in die dynamische nGa Menge
						// einsortiert werden sollen. Daf�r wird ein Array erstellt, in dem alle Objekte nach ihrer
						// Pid (HashCode) sortiert vorlieren. Dieses Array wird dann in die alten Daten eingemischt.

						// Jeder Eintrag hat eine feste L�nge und besteht aus Pid HashCode und der relativen Position
						// in der Datei.
						// Nachteil, gibt es zu einer Pid 10 Objekte, so wird die Pid 10 mal gespeichert

						// sortiertes Array erstellen und sortieren
						SortObjectPid[] newOldObjectsPidSortedArray = (SortObjectPid[])newOldObjectsPidMap.values().toArray(new SortObjectPid[newOldObjectsPidMap.size()]);
						Arrays.sort(
								newOldObjectsPidSortedArray, new Comparator<SortObjectPid>() {
							public int compare(SortObjectPid o1, SortObjectPid o2) {
								return o1.compareTo(o2);
							}
						}
						);

						// Auf den Beginn des Pid HashCode Bereichs positionieren
						oldConfigAreaFile.seek(_startPidHashCodeIndex + _headerEnd);

						// Die alten schon sortiereten Objekte aus der alten Datei betrachten
						// und die gerade sortiereten neuen alten Objekte einmischen

						// Bis zu dieser Position m�ssen Daten gelesen werden
						final long endPidIndexBlock = _headerEnd + _startMixedSet;

						// gibt an, ob alle Pids gemischt wurden
						boolean allPidsMerged = false;

						// Speichert die Pid aus der alten Datei. Der Wert wird gross gew�hlt, da es sein kann, das es keinen
						// Wert aus der Datei gibt.
						int pidOldConfigFile = Integer.MIN_VALUE;
						// Speichert die relative Dateiposition aus der alten Datei
						relativeFilePositionOldConfigFile = -1;
						// Wenn alle alten Objekte aus der alten Datei einsortiert sind
						boolean oldPidsMerged = false;
						// Speichert, ob die Werte pidOldConfigFile, relativeFilePositionOldConfigFile benutzt wurden.
						// Wenn ja, dann m�ssen die n�chsten Werte geladen werden
						oldFileObjectUsed = true;

						// Objekt, das gerade aus dem Array newOldObjectsPidSortedArray betrachtet wird
						int actualPidMergeObject = 0;
						// Objekt aus dem Array newOldObjectsPidSortedArray (Es ist ein altes Objekt, aber es befand sich noch nicht in der Datei)
						SortObjectPid newOldPidObject = new SortObjectPid(Integer.MAX_VALUE);
						// Wenn alle neuen alten Id�s einsortiert sind
						boolean newOldPidsMerged = false;

						// Falls true, dann muss das n�chste Objekt geholt werden, da das letzte Objekt gespeichert wurde
						newOldObjectUsed = true;

						while(allPidsMerged == false) {
							// Soll ein neuer Wert aus der alten Datei ausgelesen werden.
							if((oldConfigAreaFile.getFilePointer() < endPidIndexBlock) && oldFileObjectUsed) {
								// Es gibt noch Daten, die eingelesen werden m�ssen und der alte Wert wurde
								// einsortiert, also die n�chsten Daten laden
								pidOldConfigFile = oldConfigAreaFile.readInt();
								relativeFilePositionOldConfigFile = oldConfigAreaFile.readLong();

								// Die Werte wurden geladen, also stehen neue Werte zur Verf�gung
								oldFileObjectUsed = false;
							}
							else if(oldFileObjectUsed) {
								// Es gibt keine Werte mehr, die aus der alten Datei kommen.

								// Durch diesen hohen Wert, werden immer die Werte aus dem Array benutzt und der Algorithmus
								// kann normal terminieren.
								pidOldConfigFile = Integer.MAX_VALUE;
								relativeFilePositionOldConfigFile = -1;
								oldFileObjectUsed = false;
								// Die Pid-Menge ist fertig
								oldPidsMerged = true;
							}

							// if (actualPidMergeObject < newOldObjectSortedArray.length && newOldObjectUsed) {
							if(actualPidMergeObject < newOldObjectsPidSortedArray.length && newOldObjectUsed) {
								// Es gibt noch Daten und der alte Wert wurde einsortiert
								newOldPidObject = newOldObjectsPidSortedArray[actualPidMergeObject];
								actualPidMergeObject++;
								newOldObjectUsed = false;
							}
							else if(newOldObjectUsed) {
								// Alle Objekte aus dem Array wurden gespeichert (eingemischt), jetzt kann der Rest aus der alten Datei
								// kopiert werden.

								newOldPidObject = new SortObjectPid(Integer.MAX_VALUE);
								newOldObjectUsed = false;
								// Das Array wurde bearbeitet
								newOldPidsMerged = true;
							}

							if((newOldPidsMerged == false || oldPidsMerged == false)) {
								// An dieser Stelle stehen zwei Werte zur Verf�gung, die eingemischt werden k�nnen
								if(pidOldConfigFile < newOldPidObject.getPidHashCode()) {
									newConfigAreaFile.writeInt(pidOldConfigFile);
									newConfigAreaFile.writeLong(relativeFilePositionOldConfigFile);
									// Dadurch wird ein neuer Wert angefordert
									oldFileObjectUsed = true;
								}
								else {
									// Alle Werte des Objekts schreiben
									final List<Long> filePositions = newOldPidObject.getFilePositions();
									for(int nr = 0; nr < filePositions.size(); nr++) {
										newConfigAreaFile.writeInt(newOldPidObject.getPidHashCode());
										// Relative Dateiposition, die Position bezieht sich auf die neue Datei
										newConfigAreaFile.writeLong(filePositions.get(nr));
									}
									// n�chstes Objekt anfordern
									newOldObjectUsed = true;
								}
							}
							else {
								// Es gibt keine Werte mehr
								allPidsMerged = true;
							}
						} // while(oldIdsMerged)

//*********************************************************************************************************

						// Die aktuellen und zuk�nftig aktuellen in die Mischmenge schreiben

						newRelativeMixedSet = newConfigAreaFile.getFilePointer() - newAbsoluteEndHeader;

						// Da die Objekte neu geschrieben werden, ver�ndert sich auch die Position in der Datei.
						// Das muss dem Objekt mitgeteilt werden (geschieht beim schreiben automatisch)

						synchronized(_actualObjects) {
							Collection<SystemObjectInformationInterface> allActualObjects = _actualObjects.values();
							for(Iterator<SystemObjectInformationInterface> iterator = allActualObjects.iterator(); iterator.hasNext();) {
								SystemObjectInformationInterface systemObjectInfo = iterator.next();

								// In die neue Datei speichern
								if(systemObjectInfo instanceof ConfigurationObjectInformation) {
									ConfigurationObjectInformation configurationObjectInformation = (ConfigurationObjectInformation)systemObjectInfo;
									// Da das Objekt neu in eine Datei geschrieben wird, muss keine L�cke eingef�gt werden. Die
									// neue Speicherposition darf nicht an dem Objekt gespeichert werden, sondern erst wenn
									// die Reorganisation abgeschlossen ist.
									// Den Speicherort merken, damit er nachtr�glich gesetzt werden kann.
									final long objectFilePosition = writeConfigurationObjectToFile(
											configurationObjectInformation, newConfigAreaFile, false, false
									);
									memoryObjectsNewFilePositionMap.put(objectFilePosition, configurationObjectInformation);
								}
								else if(systemObjectInfo instanceof DynamicObjectInformation) {
									DynamicObjectInformation dynamicObjectInformation = (DynamicObjectInformation)systemObjectInfo;
									// Da das Objekt neu in eine Datei geschrieben wird, muss keine L�cke eingef�gt werden.
									// Die neue Dateiposition darf nicht an dem Objekt gespeichert werden, sondern muss nachtr�glich gesetzt
									// werden.
									final long objectFilePosition = writeDynamicObjectToFile(dynamicObjectInformation, newConfigAreaFile, false, false);
									memoryObjectsNewFilePositionMap.put(objectFilePosition, dynamicObjectInformation);
								}
								else {
									_debug.error("Unbekanntes Objekt " + systemObjectInfo.getClass());
								}
							}
						}

						synchronized(_newObjects) {
							Collection<SystemObjectInformationInterface> allActualObjects = _newObjects.values();
							for(Iterator<SystemObjectInformationInterface> iterator = allActualObjects.iterator(); iterator.hasNext();) {
								SystemObjectInformationInterface systemObjectInfo = iterator.next();

								// In die neue Datei speichern
								if(systemObjectInfo instanceof ConfigurationObjectInformation) {
									ConfigurationObjectInformation configurationObjectInformation = (ConfigurationObjectInformation)systemObjectInfo;
									// Da das Objekt neu in eine Datei geschrieben wird, muss keine L�cke eingef�gt werden.
									// Die neue Dateiposition darf nicht an dem Objekt gespeichert werden, sondern muss nachtr�glich gesetzt
									// werden.
									final long objectFilePosition = writeConfigurationObjectToFile(
											configurationObjectInformation, newConfigAreaFile, false, false
									);
									memoryObjectsNewFilePositionMap.put(objectFilePosition, configurationObjectInformation);
								}
								else if(systemObjectInfo instanceof DynamicObjectInformation) {
									DynamicObjectInformation dynamicObjectInformation = (DynamicObjectInformation)systemObjectInfo;
									// Da das Objekt neu in eine Datei geschrieben wird, muss keine L�cke eingef�gt werden.
									// Die neue Dateiposition darf nicht an dem Objekt gespeichert werden, sondern muss nachtr�glich gesetzt
									// werden.
									final long objectFilePosition = writeDynamicObjectToFile(dynamicObjectInformation, newConfigAreaFile, false, false);
									memoryObjectsNewFilePositionMap.put(objectFilePosition, dynamicObjectInformation);
								}
								else {
									_debug.error("Unbekanntes Objekt " + systemObjectInfo.getClass());
								}
							}
						}

//*********************************************************************************************************
						// Header mit den richtigen relativen Dateipostionen f�llen

						// Es stehen alle relativen Positionen in dem neuen Konfigurationsbereich zur Verf�gung,
						// also kann das Objekt, das die Datei verwaltet, auf die neue Datei umgestellt werden.
						// Danach kann der Header in die neue Datei geschrieben werden.

						



						// Damit ist auch sofort bekannt, wo der Bereich beginnt, der alle ung�ltigen Konfigurationsobjekte
						// enth�lt
						_headerEnd = newAbsoluteEndHeader;

						_startOldDynamicObjects = newRelativeDynObjectArea;

						_startIdIndex = newRelativeIdIndex;

						_startPidHashCodeIndex = newRelativePidIndex;

						_startMixedSet = newRelativeMixedSet;

						// Mit den neuen relativen Positionen den Header schreiben
						writeHeader(newConfigAreaFile);
//*********************************************************************************************************
						// Datei umbennen und die alte Datei ersetzen

						// Die bisherige Konfigurationsdatei wird in "alter Name".configold umbenannt.
						// Die gerade erzeugte Konfigurationsbereichsdatei wird in den alten Namen umbenannt.

						oldConfigAreaFile.close();
						newConfigAreaFile.close();

						// Die original Datei in Name.configold umbennen
						final String originalFileName = _configAreaFile.getAbsolutePath();
						final File oldConfigFile = new File(originalFileName);

						// gibt es von einer vorherigen Reorganisation noch ein old-File, wenn ja, l�schen
						final File lastRestructureOldFile = new File(originalFileName + "Old");
						if(lastRestructureOldFile.exists()) {
							lastRestructureOldFile.delete();
						}

						if(oldConfigFile.renameTo(new File(originalFileName + "Old"))) {
							// Die original Datei konnte umbennant werden

							// Die neue Datei umbennen
							if(configAreaNewName.renameTo(new File(originalFileName))) {

								// Die Dateien konnten erfolgreich umbenannt werden, also k�nnen die Objekte, die sich
								// im Speicher befinden, auf die neue Situation angepa�t werden werden.

								// Alle Schl�ssel der Maps, dies entspricht den neuen Dateipositionen in der neuen Datei.
								// Diese Position muss den Objekten zugewiesen werden.
								final Collection<Long> newFilePositions = memoryObjectsNewFilePositionMap.keySet();

								for(Iterator<Long> iterator = newFilePositions.iterator(); iterator.hasNext();) {
									Long newObjectFilePosition = iterator.next();
									final SystemObjectInformationInterface object = memoryObjectsNewFilePositionMap.get(newObjectFilePosition);

									if(object instanceof ConfigurationObjectInformation) {
										final ConfigurationObjectInformation configObject = (ConfigurationObjectInformation)object;
										configObject.setLastFilePosition(newObjectFilePosition);
									}
									else if(object instanceof DynamicObjectInformation) {
										final DynamicObjectInformation dynObject = (DynamicObjectInformation)object;
										dynObject.setLastFilePosition(newObjectFilePosition);
									}
									else {
										_debug.error("Objekt unbekannten Typs: " + object.getClass());
									}
								}

								// In der Map _oldObjectsId sind alle Objekte gespeichert, die als ung�ltig markiert sind
								// und die sich in der Mischmenge befinden. Das gleiche gilt f�r die Map _oldObjectsPid
								// Diese Maps m�ssen nun um die Objekte bereinigt werden, die in die nGa Bereiche oder in den
								// dyn nGa Bereich gespeichert wurden.

								// In dem Objekt, das alle ConfigFiles verwaltet wird ebenfalls eine Map mit Id�s
								// f�r die alten Objekte gef�hrt, diese muss ebenfalls entfernt werden

								
								for(int nr = 0; nr < newOldObjectsIdList.size(); nr++) {
									// Objekt, das die Id eines Objekt speichert
									final SortObject oldObject = newOldObjectsIdList.get(nr);
									// entfernt das Objekt aus der Map
									synchronized(_oldObjectsId) {
										if(_oldObjectsId.remove(oldObject.getValue()) == null) {
											_debug.warning(
													"Ein altes Objekt, das sich in der Mischmenge befand und an die richtige Position gespeichert werden sollte, konnte nicht aus der Map der alten Objekte gel�scht werden, da es dort nicht mehr vorhanden war"
											);
										}
									}
									// Aus der �bergeorndeten Datenstruktur entfernen
									_fileManager.removeObject(oldObject.getValue());
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
							else {
								// Fehler, die neue Datei konnte nich umbenannt werden. Also wird die alte Datei weiter benutzt.
								// Die Reorganisation ist fehlgeschlagen.
								if(!oldConfigFile.renameTo(new File(originalFileName))) {
									
									_debug.error("Die alte Konfigurationsdatei kann nicht weiter benutzt werden");
								}

								throw new IllegalStateException(
										"Reorganisation: Fehler beim umbennen der neuen Konfigurationsdatei " + newConfigAreaFile + "in " + originalFileName
								);
							}
						}
						else {
							// Die Reorganisation ist fehlgeschlagen
							throw new IllegalStateException("Reorganisation: Fehler beim umbennen der aktuellen Konfigurationsdatei: " + originalFileName);
						}
					}
					finally {
						oldConfigAreaFile.close();
						newConfigAreaFile.close();
					}
				}
				catch(Exception e) {
					// F�ngt alle Fehler ab, kommt es zu einem Fehler, wird die Reorganisation unterbrochen und der
					// Ursprungszustand bleibt erhalten
					_debug.warning(
							"Fehler bei der Reorganisation " + _configAreaFile + " Pid " + _configurationAreaPid
							+ " . Die Reorganisation wurde abgebrochen, es wird ohne �nderung in der Ursprungsdate normal weitergearbeitet.", e
					);
					return false;
				}
			} // synch(datei)
		} // synch(_restruct)
		return true;
	}


	/**
	 * Diese Methode sucht zu einem Value alle Objekte, die als ung�ltig markiert sind. Der Parameter value wird dabei unterschiedlich interpretiert und zwar in
	 * Abh�ngigkeit vom Parameter searchId. Ist searchId = true, so wird der Parameter value als Id eines Objekts betrachtet. Die Methode sucht dann ein als
	 * ung�ltig markiertes Objekt, dessen Id gleich dem �bergebenen Parameter value entspricht. Ist der Parameter searchId = false, so wird der Wert als HashCode
	 * einer Pid interpretiert. Da eine Pid nicht bijektiv auf ein Integer abgebildet werden kann, muss die Liste sp�ter noch �berpr�ft werden, ob die Objekte auch
	 * wirklich mit der Pid �bereinstimmen.
	 *
	 * @param value    Id oder Pid, nach der die Objekte gesucht werden sollen
	 * @param searchId true = value wird als Id interpretiert; false = value wird als HashCode einer Pid interpretiert (siehe Methodenbeschreibung!)
	 *
	 * @return Liste, die die geforderten Objekte enth�lt. Bei einer Anfrage nach Id, ist nur ein Element in dieser Liste. Es wird <code>null</code> zur�ckgegeben,
	 *         falls kein Objekt gefunden werden konnte
	 *
	 * @throws IllegalStateException Zu einer Id wurden zwei Elemente gefunden
	 */
	private List<SystemObjectInformationInterface> binarySearch(long value, boolean searchId) throws NoSuchVersionException, IOException {
		// Wenn reorganisiert wird, dann muss gewartet werden, weil die Datei umgestellt wird
		synchronized(_restructureLock) {
			synchronized(_configAreaFile) {

				RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

				// Try/finally f�r close der Datei
				try {
					// Liste, die alle Objekte speichert, die dem geforderten value entsprechen
					final List<SystemObjectInformationInterface> searchResult = new ArrayList<SystemObjectInformationInterface>();

					// Die gesuchten Objekte k�nnen sich in der Mischmenge oder in den nGa Bereichen oder im dyn. nGa Bereich befinden.

					// mit Id suchen
					if(searchId) {
						final OldObjectIdReference oldObjectIdReference;
						final boolean positionFound;
						synchronized(_oldObjectsId) {
							if(_oldObjectsId.containsKey(value)) {
								oldObjectIdReference = _oldObjectsId.get(value);
								positionFound = true;
							}
							else {
								oldObjectIdReference = null;
								positionFound = false;
							}
						}

						if(positionFound) {
							// Objekt befand sich in der Mischmenge.
							// Im "else-Fall" muss das Objekt in den nGa-Bereichen, dem dyn nGa-Bereich gesucht werden
							searchResult.add(oldObjectIdReference.getObject());
							return searchResult;
						}
					}
					else {
						// Es soll nach einer Pid gesucht werden, diese kann sich im Mischbreich befinden
						// und in den nGa-Bereichen, dem dyn nGa-Bereich

						// Mischbreich pr�fen

						final Set<Long> filePositions;
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
								for(Long filePosition : filePositions) {
									// Objekt laden
									searchResult.add(loadObjectFromFile(filePosition));
								}
							}
						}
					}

					// An dieser Stelle wurde Mischobjektmenge abgearbeitet. Falls nach Id gesucht wurde, wurde diese nicht
					// gefunden, sonst w�re die Methode bereits mit "return" verlassen worden. Falls nach der Pid gefragt wurde
					// so kann in der Ergebnisliste bereits ein Wert vorhanden sein. Es kann aber sein, dass sich in den nGa-Bereichen
					// dem nGa-Bereich weitere Objekte befinden.

					// Nun muss entweder der Id oder der Pid Index benutzt werden

					// Als erstes muss gepr�ft werden, ob es den ben�tigten Index �berhaupt gibt. Dieser Fall kann
					// auftreten, wenn es noch zu keiner Reorganisation gekommen ist und es somit keine
					// nGa Bereiche bzw. den dyn. nGa Bereich noch nicht gibt.
					// Falls es keine Reorganisation gegeben hat, dann stehen die Elemente im Speicher und wurden bereits
					// gefunden, siehe oben.

					// Wird true, falls der ben�tigte Index(Id oder Pid) nach einer Reorganisation angelegt wurde
					boolean indexExist = false;

					if(searchId) {
						// Der Index nach Id muss vorhanden sein
						if(_startIdIndex < _startPidHashCodeIndex) {
							// Der start des Id-Index ist kleiner als der Start des Pid-Index, also m�ssen
							// in diesem Zwischenraum Daten stehen.
							indexExist = true;
						}
					}
					else {
						if(_startPidHashCodeIndex < _startMixedSet) {
							// Nach dem Pid Index kommt die Mischmenge, im Zwischenraum m�sen also
							// Daten stehen
							indexExist = true;
						}
					}

					// Die Suche macht nur Sinn, wenn der Index auch besteht.
					if(indexExist) {
						// Erste Eintrag im Index (Id oder Pid)
						long minimum;
						// In dieser Variablen steht sp�ter die Dateipostion an der das Paar "Id, relative Dateipostion" (Id Anfrage) steht
						long middle = -1;

						// In dieser Variablen steht sp�ter die Dateipostion an der das Paar "Pid-HashCode, relative Dateipostion" (Pid Anfrage) steht.
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
						// Alle Objekte, die nach dem Objekt gespeichert wurden, entsprechem dem value oder sind gr��er.
						// Dies ist f�r die Pid wichtig, da f�r jedes Objekt mit der gleichen Pid ein Eintrag gemacht wird.
						while(minimum < maximum) {
//							System.out.println("Start-while: min: " + minimum + " middle: " + middle + " max: " + maximum);

							// Speichert den Wert, des aus dem Index (Id oder Pid) geladen wurden
							final long valueFromIndex;
							// Neue Mitte ausrechnen. Dies entspricht einer Dateiposition.
							// Der n�chste Wert setzt sich im Id Index anderes zusammen, als bei einem
							// Pid Index. Nach dem die Mitte gefunden wurde, wird der Wert aus dem entsprechenden Index
							// eingelesen.
							if(searchId) {
								// Wieviele Paare (Long,Long (Id,Relative Position) sind zwischem Min und Max.
								final long numberOfPairs = (maximum - minimum) / 16;

								// Die h�lfte der Paare und dann pro paar 16 Byte (2 Longs) dazurechnen. Dieser Wert muss zum Min.
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

								// Die h�lfte der Paare und dann pro paar 12 Byte (ein Integer und ein Long) dazurechnen. Dieser Wert muss zum Min.
								// gerechnet werden um die Mitte zu erhalten.
								final long newMinOffset = (numberOfPairs / 2) * 12;
								middle = minimum + newMinOffset;
								// Wert aus dem Pid Index einlesen
								file.seek(middle);
								valueFromIndex = file.readInt();
							}

							// Die Id oder der HashCode der Pid steht nun zur Verf�gung
//							System.out.println("Bin�re Suche: middle: " + valueFromIndex + " gesucht: " + value);
							if(value <= valueFromIndex) {
								maximum = middle;
								if(value == valueFromIndex) {

									// Der Wert wurde gefunden. wird nur die Id gesucht, dann endet die Suche.
									// Bei der Suche nach der Pid muss weiter gesucht werden, da es mehrere
									// Objekte zu einer Pid geben kann.
									resultFound = true;
//									System.out.println("*****Bin�re suche findet das gesuchte Objekt*****");

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
								// das n�chste Element ist ein Paar, das entweder aus Long,Long oder aus Int,Long besteht.
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
							// der Pid k�nnen es mehrere sein)
							if(searchId) {

								// Den filePointer auf die Stelle setzen, wo das erste Objekt steht
								file.seek(middle);

								// Id, es gibt nur einen Wert

								// Id aus dem Index einlesen
								final long checkId = file.readLong();
								// relative Dateiposition des Objekts
								final long position = file.readLong();
								assert checkId == value : "geforderte Id: " + value + " gefundene Id: " + checkId;

								final SystemObjectInformationInterface idObject = loadObjectFromFile(getAbsoluteFilePositionForInvalidObjects(position));
								searchResult.add(idObject);
								assert idObject.getID() == checkId : "Dateiposition: " + position + " Datei: " + _configAreaFile + " gesuchte Id: " + value
								                                     + " gefundene Id: " + idObject.getID();
								// Liste mit dem Objekt zur�ckgeben
								return searchResult;
							}
							else {

								// Auf den richtigen Index springen, dieser steht im Gegensatz zu Id-Suche in dieser Variablen
								file.seek(helperPid);

								// Es werden Objekte f�r die Pid ben�tigt. F�r eine Pid kann es mehrer Objekte geben.
								// Es m�ssen alle Objekte aus dem Pid-Index eingelesen werden, deren Pid-HashCode mit
								// dem �bergebenen Code �bereinstimmen.

								// wird true, wenn das letzte Objekt aus dem Pid-Index geladen wurden, dessen hashCode stimmt
								boolean lastPidObjectRead = false;

								// Solange noch die richtigen Pids gelesen werden
								// und
								// der filePointer sich noch im Pid-Index befindet
								while((!lastPidObjectRead) && (file.getFilePointer() < (_startMixedSet + _headerEnd))) {
									final int pidHashCode = file.readInt();
									final long position = file.readLong();

									if(pidHashCode == value) {
										final SystemObjectInformationInterface pidObject = loadObjectFromFile(getAbsoluteFilePositionForInvalidObjects(position));
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
				finally {
					file.close();
				}
			} // synch file
		} // synch restructereLock
	}

	/**
	 * Diese Methode berechnet die absolute Position eines Objekts in der Konfigurationsbereichsdatei. Das Objekt befindet sich dabei in einem nGa Bereich oder in
	 * einem dyn. nGa Bereich.
	 *
	 * @param relativeFilePosition relative Position des Objekts. Ein negativer Wert (einschliesslich 0) wird als ung�ltiges Konfigurationsobjekt interpretiert.
	 *                             Ein positiver Wert als ung�ltiges dynamisches Objekt. Beide Objekte befinden sich in den nGa Bereichen oder im dyn. nGa
	 *                             Bereich.
	 *
	 * @return absolute Position, an der das Objekt zu finden ist
	 */
	private long getAbsoluteFilePositionForInvalidObjects(long relativeFilePosition) {
		if(relativeFilePosition > 0) {
			// Es handelt sich um dynamisches Objekt, das sich in der dyn. nGa Menge befindet.
			// Die relative Positionsangabe bezieht sich auf den Beginn des dyn. nGa Bereichs.
			// Die relative Position ist immer um +1 erh�ht worden, damit wurde eine "doppelte 0" verhindert.
			// Die "0" geh�rt zu den Konfigurationsobjekten.
			return ((_startOldDynamicObjects + _headerEnd) + relativeFilePosition) - 1;
		}
		else {
			// Es handelt sich um ein Konfigurationsobjekt. Die relative Position bezieht sich auf das
			// Headerende.
			return _headerEnd + (relativeFilePosition * (-1));
		}
	}

	/**
	 * Gibt die Pid des Konfigurationsbereichs zur�ck.
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

	/** Gibt die Indizes am Bildschirm aus, diese Methode ist nur f�r Debug-Zwecke geeignet. */
	void printIdIndex() {
		try {
			synchronized(_restructureLock) {
				synchronized(_configAreaFile) {
					StringBuffer out = new StringBuffer();
					RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");
					try {
						file.seek(_startIdIndex + _headerEnd);
						out.append("Id und Pid Index: \n");
						int numberOfIds = 0;
						while(file.getFilePointer() < (_startPidHashCodeIndex + _headerEnd)) {
							numberOfIds++;
							final long absoluteIndexPosition = file.getFilePointer();
							final long id = file.readLong();
							final long relative = file.readLong();
							final long absolute = getAbsoluteFilePositionForInvalidObjects(relative);

							out.append(
									"Indexposition: " + absoluteIndexPosition + " Id: " + id + " relative Dateiposition: " + relative + " absolut: " + absolute
									+ "\n"
							);
						}
						out.append("Anzahl Eintr�ge: " + numberOfIds + " relative Position: " + _startIdIndex + "\n");

						// Pid Index einlesen
						file.seek(_startPidHashCodeIndex + _headerEnd);

						int numberOfPids = 0;
						while(file.getFilePointer() < (_startMixedSet + _headerEnd)) {
							numberOfPids++;

							final long absoluteIndexPosition = file.getFilePointer();
							final long pid = file.readInt();
							final long relative = file.readLong();
							final long absolute = getAbsoluteFilePositionForInvalidObjects(relative);

							out.append(
									"Indexposition: " + absoluteIndexPosition + " Pid: " + pid + " relative Dateiposition: " + relative + " absolut: "
									+ absolute + "\n"
							);
						}
						out.append("Anzahl Eintr�ge: " + numberOfPids + " relative Position: " + _startPidHashCodeIndex + "\n");
						System.out.println(out);
					}
					finally {
						file.close();
					}
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			_debug.error("", e);
		}
	}

	/**
	 * Diese Klasse wird mehrfach mit unterschiedlichen Aufgaben benutzt. Es wird aber immer in einem Array benutzt um eine Sortierung des Array nach dem Value des
	 * Objekts zu erzeugen. Der Value kann dabei ein Zeitstempel oder eine Id sein. <br> Diese Klasse speichert den Zeitstempel, wann ein dynamisches Objekt
	 * ung�ltig geworden ist und die Position an dem das Objekt in der Datei gespeichert wurde. <br> Diese Klasse speichert die Id und die Dateiposition von
	 * beliebigen Objekten.
	 */
	private final static class SortObject implements Comparable {

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

		public int compareTo(Object o) {
			final SortObject sortObject = (SortObject)o;

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
	}

	private final static class SortObjectPid implements Comparable {

		/** relative Dateipositionen der Objekte, deren Pid auf _pidHashCode abgebildet werden konnten */
		private final List<Long> _filePositions = new ArrayList<Long>();

		/** HashCode, der die Pid abbildet */
		private final int _pidHashCode;

		/** @param pidHashCode HashCode einer Pid, dieser Wert wird bei einer Sortierung als Kriterium gew�hlt */
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

		public int compareTo(Object o) {
			final SortObjectPid sortObject = (SortObjectPid)o;

			if(_pidHashCode < sortObject.getPidHashCode()) {
				return -1;
			}
			else if(_pidHashCode > sortObject.getPidHashCode()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	/**
	 * Ein Objekt, das als "ung�ltig" markiert ist enth�lt den HashCode der Pid und die Id des Objekts. Damit das Objekt sp�ter, falls n�tig, geladen werden kann,
	 * befindet sich ebenfalls die Dateiposition und ein Objekt zum einladen der Daten am Objekt
	 */
	final static class OldObject {


		private final long _id;

		private final int _pidHashCode;

		/** Wo in der Datei befindet sich das Objekt */
		private final long _filePosition;

		/** Objekt zum laden des Objekts */
		private final ConfigAreaFile _configAreaFile;

		/**
		 * @param id
		 * @param pidHashCode
		 * @param filePosition   Position in der Datei, an der das Objekt gespeichert wurde
		 * @param configAreaFile Objekt, �ber das das als "ung�ltig" markierte Objekt geladen werden kann
		 */
		public OldObject(long id, int pidHashCode, long filePosition, ConfigAreaFile configAreaFile) {
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

		public long getFilePosition() {
			return _filePosition;
		}

		public ConfigAreaFile getConfigAreaFile() {
			return _configAreaFile;
		}

		public String toString() {
			final StringBuffer out = new StringBuffer();
			out.append("Ung�ltig markiertes Objekt:" + "\n");
			out.append("Id : " + getId() + "\n");
			out.append("Pid, hashCode: " + getPidHashCode() + "\n");
			// out.append("Datei: " + getConfigAreaFile() + "\n");
			out.append("Position in der Datei: " + getFilePosition() + "\n");
			if(_configAreaFile != null) {
				out.append("Objekt, ConfigAreaFile: " + _configAreaFile + "\n");
			}
			else {
				out.append("Objekt, ConfigAreaFile: null" + "\n");
			}
			return out.toString();
		}
	}

	/**
	 * Diese Klasse speichert die Dateiposition eines Blocks, der Konfigurationsobjekte enth�lt, die ung�ltig sind und durch eine Reorganisation verschoben wurden
	 * und sich nicht mehr in der Mischobjektmenge befinden. Als zus�tzliche Informatione speicher die Klasse einen Zeitstempel, der zu dem Block geh�rt.
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
	 * Diese Klasse speichert alle Inforamtionen, die ben�tigt werden um ein Objekt, das sich in der Mischmenge befindet aber nicht komplett in den Speicher
	 * geladen wurde, eventuell nachzuladen falls es gebraucht wird. Bei transienten Objekte wird das gesamte Objekt gespeichert.
	 * <p/>
	 * Bei der Suche nach der Type Id wird bei alten Objekten immer ein Zeitbereich angegeben, in dem das Objekt g�ltig gewesen sein muss. Also wird entweder der
	 * Zeitpunkt an dem das Objekt g�ltig wurde gespeichert (bei dynamischen Objekten) oder aber die Version mit der das Objekt g�ltig wurde.
	 * <p/>
	 * Es wird ebenfalls ein Boolean gespeichert, der angibt ob das Objekt ein Konfigurationsobjekt oder ein dynamisches Objekt ist.
	 * <p/>
	 * Als letztes wird die absolute Position des Objekts in der Datei gespeichert, damit es falls n�tig geladen werden kann.
	 */
	private final static class OldObjectTypeIdInfo {

		/**
		 * Bei einem Konfigurationsobjekt steht hier die Version, mit der das Objekt g�ltig wurde. Bei einem dynamischen Objekt ist es der Zeitpunkt, an dem das
		 * Objekt g�ltig wurde.
		 */
		private final long _firstValid;

		/** Zeitpunkt/Version, an dem das Objekt ung�ltig werden soll/geworden ist (0 bedeutet, dass dieser Zeitpunkt unbekannt ist) */
		private final long _firstInvalid;

		/** true = es handelt sich um ein Konfigurationsobjekt; false = das Objekt ist ein dynamisches Objekt */
		private final boolean _configurationObject;

		private final OldObjectIdReference _oldObjectIdReference;

		/**
		 * @param firstValid           Bei Konfigurationsobjekten die Version, mit der das Objekt g�ltig wurde. Bei einem dynamischen Objekt, der Zeitpunkt an dem das
		 *                             Objekt g�ltig wurde.
		 * @param firstInvalidVersion  Bei Konfigurationsobjekten die Version, mit der das Objekt ung�ltig wurde. Bei einem dynamischen Objekt, der Zeitpunkt an dem
		 *                             das Objekt ung�ltig wurde. Der Wert 0 bedeutet in beiden F�llen, dass die Version, in der das Objekt ung�ltig wird, noch
		 *                             unbekannt ist.
		 * @param configurationObject  Variable wird ben�tigt um den Paramter firstValid auszuwerten. true = Es ist ein Konfigurationsobjekt; false = es ist ein
		 *                             dynamisches Objekt
		 * @param oldObjectIdReference Objekt, mit dem das Objekt aus der Datei rekonstruiert werden kann, bzw direkt aus dem Speicher geholt wird.
		 */
		public OldObjectTypeIdInfo(long firstValid, long firstInvalidVersion, boolean configurationObject, OldObjectIdReference oldObjectIdReference) {
			if(firstValid > 0) {
				_firstValid = firstValid;
			}
			else {
				throw new IllegalArgumentException(
						"OldObjectTypeIdInfo, FirstValidVersion: " + firstValid + " FirstInvalidVersion " + firstInvalidVersion + " Konfiguriernend "
						+ configurationObject + " betroffenes Objekt: " + oldObjectIdReference
				);
			}

			_firstInvalid = firstInvalidVersion;
			_configurationObject = configurationObject;
			_oldObjectIdReference = oldObjectIdReference;
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

		public OldObjectIdReference getOldObjectIdReference() {
			return _oldObjectIdReference;
		}
	}

	/**
	 * Diese Klasse stellt ein Objekt dar, das ung�ltig ist und eine Methode um dieses Objekte anzufordern.
	 * <p/>
	 * Bei vielen Objekten muss das ung�ltige Objekt aus einer Datei geladen werden. Bei machen Objekten (transiente, als Beispiel) befindet sich das Objekt die
	 * ganze Zeit im Hauptspeicher.
	 */
	private final class OldObjectIdReference {

		final DynamicObjectInfo _dynamicObjectInfo;

		final long _absoluteFilePosition;


		public OldObjectIdReference(final long absoluteFilePosition) {
			_absoluteFilePosition = absoluteFilePosition;
			_dynamicObjectInfo = null;
		}

		public OldObjectIdReference(final DynamicObjectInfo dynamicObjectInfo) {
			_dynamicObjectInfo = dynamicObjectInfo;
			_absoluteFilePosition = -1;
		}

		/**
		 * L�dt das Objekt aus der Datei oder holt es aus dem Speicher (transiente Objekte).
		 *
		 * @return s.o.
		 *
		 * @throws NoSuchVersionException
		 * @throws IOException
		 */
		SystemObjectInformationInterface getObject() throws NoSuchVersionException, IOException {
			if(_dynamicObjectInfo != null) {
				return _dynamicObjectInfo;
			}
			else {
				return loadObjectFromFile(_absoluteFilePosition);
			}
		}
	}

	/** Diese Klasse stellt einen Iterator zur Verf�gung, der alle Objekte eines Konfigurationsbereichs zur Verf�gung stellt. */
	private final class FileIterator implements Iterator<SystemObjectInformationInterface> {

		/**
		 * Speichert die relative Position des Datensatzes, der als n�chstes aus der Datei gelesen werden muss. Die absolute Position setzt sich auf der relativen
		 * Position + _headerEnd zusammen. Der Wert -1 bedeutet, dass es keine weiteren Objekte mehr gibt, die in der Datei gespeichert sind.
		 */
		private long _relativePosition = -1;

		/** absolute Position eines als "ung�ltig" markierten Objekts, das sich in der Mischmenge befindet. */
		private final Iterator<OldObjectIdReference> _oldObjectsIterator = _oldObjectsId.values().iterator();

		/** Iterator �ber alle aktuellen Objekte, die sich im Speicher befinden */
		private final Iterator<SystemObjectInformationInterface> _actualObjectsIterator = _actualObjects.values().iterator();

		/** Iterator �ber alle zuk�nftig aktuellen Objekte, die sich im Speicher befinden */
		private final Iterator<SystemObjectInformationInterface> _newObjectsIterator = _newObjects.values().iterator();

		public FileIterator() {
			if(_startIdIndex > 0) {
				// Da der Id-Index nicht direkt nach dem Header beginnt, muss zwischem dem Header und dem Index
				// ein nGa und/oder dynamischer nGa Bereich zu finden sein
				_relativePosition = 0;
			}
		}

		synchronized public boolean hasNext() {
			// Gibt es noch Elemente, die aus der Datei geladen werden k�nnen
			if(_relativePosition > 0) return true;
			// Gibt es noch Elemente, die ung�ltig sind und in der Mischmenge vorhanden sind
			if(_oldObjectsIterator.hasNext()) return true;
			// Gibt es noch aktuelle Objekte, die zur�ckgegeben werden k�nnen
			if(_actualObjectsIterator.hasNext()) return true;
			// Gibt es noch zuk�nftig aktuelle Objekte, die zur�ckgegeben werden k�nnen
			if(_newObjectsIterator.hasNext()) return true;

			// Es wurde kein true zur�ckgegeben, also wurden alle Objekte zur�ckgegeben
			return false;
		}

		synchronized public SystemObjectInformationInterface next() {
			synchronized(_restructureLock) {
				synchronized(_configAreaFile) {

					if(_relativePosition >= 0) {
						// Es gibt noch Objekte, die aus der Datei geladen werden k�nnen.
						// Wenn das Objekt geladen wird, wird auch _relativePosition durch den setter gesetzt.

						try {
							return loadObjectFromFile((_relativePosition + _headerEnd), this);
						}
						catch(Exception e) {
							throw new RuntimeException(e);
						}
					}
					// Aus der Datei wurden alle Objekte geladen, jetzt die Objekte aus dem Speicher.
					if(_oldObjectsIterator.hasNext()) {
						try {
							return _oldObjectsIterator.next().getObject();
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

					// Es wurden alle Objekte zur�ckgegeben
					throw new NoSuchElementException();
				}
			}
		}

		synchronized public void remove() {
			throw new UnsupportedOperationException();
		}

		synchronized public void setRelativePosition(long relativePosition) {
			_relativePosition = relativePosition;
		}
	}
}
