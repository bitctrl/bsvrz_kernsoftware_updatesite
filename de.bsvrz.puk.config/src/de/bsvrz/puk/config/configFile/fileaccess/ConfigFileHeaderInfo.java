/*
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

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.hexdump.HexDumper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Diese Klasse repr�sentiert den Header einer Konfigurationsdatei ("blaue Datei"). Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8859 $
 */
public class ConfigFileHeaderInfo implements ConfigurationAreaFileInformationReader {

	/** Headerl�nge. dieser Wert steht vor dem eigentlichen Header*/
	int _headerSize;

	/** Headerende */
	long _headerEnd;

	/** aktive Version, die aus der Datei gelesen wurde. */
	private short _activeVersionFile;

	/** n�chste aktive Version, die aus der Datei eingelesen wurde. */
	private short _nextActiveVersionFile;

	/** Datei, aus der der Header gelesen wird */
	private File _configAreaFile;

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
	private Map _oldObjectBlocks = new HashMap<Short, ConfigAreaFile.OldBlockInformations>();

	/**
	 * Speichert die n�chste ung�ltige Version. Es ist die gr��te Version aus _oldObjectBlocks plus 1. Ist noch kein Block vorhanden, so ist die "n�chste"
	 * ung�ltige Version, Version 2 (Version 1 ist die erste m�gliche g�ltige Version)
	 */
	private short _nextInvalidBlockVersion = 2;

	/**
	 * synchronisierte Map, die den Aktivierungszeitpunkt jeder Version speichert. Als Key dient die Version, als value wird der Zeitpunkt zur�ckgegbeen, an dem
	 * das Konfigurationsverantwortliche die Version aktiviert hat. Siehe auch Kommentar _oldObjectBlocks.
	 * <p/>
	 * WARNUNG: Beim Zugriff mit .get() muss der Key auf short gecastet werden (.get((short) XXX)) !!!
	 */
	private Map<Short, Long> _configurationAuthorityVersionActivationTime = new HashMap<Short, Long>();

	/** relative Position des Blocks, der alle ung�ltigen dynamischen Objekte enth�lt, die nach einer Reorganisation in diesem Block geschrieben wurden. */
	private long _startOldDynamicObjects;

	/** relative Position des Index, der die Id�s verwaltet. */
	private long _startIdIndex;

	/**
	 * relative Position des Index, der die hashCodes der Pid�s verwaltet verwaltet. (-1 bedeutet, dass dieser Wert nicht aus dem Header ausgelesen werden konnte
	 * oder beim erzeugen des Headers nicht bekannt war (Datei erzeugen))
	 */
	private long _startPidHashCodeIndex;

	/**
	 * Gibt an, wo (relativ zum Header) die Mischobjektmenge beginnt. Dies steht entweder im Header, oder bei einener neuen Datei beginnt dieser Bereich genau
	 * hinter dem Header.
	 */
	private long _startMixedSet;

	/** Pid des Konfigurationsbereichs */
	private String _configurationAreaPid;

	/** letzter Zeitpunkt, an dem ein dynamisches Objekt ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _dynamicObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein Konfigurationsobjekt ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationObjectChanged = -1;

	/** letzter Zeitpunkt, an dem ein konfigurierender Datensatz ge�ndert wurde. Der Wert -1 zeigt an, dass dieser Wert noch unbekannt ist. */
	private long _configurationDataChanged = -1;

	private Debug _debug = Debug.getLogger();

	private int _serializerVersion;


	public long getHeaderEnd() {
		return _headerEnd;
	}


	public short getActiveVersionFile() {
		return _activeVersionFile;
	}

	public short getNextActiveVersionFile() {
		return _nextActiveVersionFile;
	}

	public File getConfigAreaFile() {
		return _configAreaFile;
	}

	public Map<Short, ConfigAreaFile.OldBlockInformations> getOldObjectBlocks() {
		return _oldObjectBlocks;
	}

	public short getNextInvalidBlockVersion() {
		return _nextInvalidBlockVersion;
	}

	public Map<Short, Long> getConfigurationAuthorityVersionActivationTime() {
		return _configurationAuthorityVersionActivationTime;
	}

	public long getStartOldDynamicObjects() {
		return _startOldDynamicObjects;
	}

	public long getStartIdIndex() {
		return _startIdIndex;
	}

	public long getStartPidHashCodeIndex() {
		return _startPidHashCodeIndex;
	}

	public long getStartMixedSet() {
		return _startMixedSet;
	}

	public String getConfigurationAreaPid() {
		return _configurationAreaPid;
	}

	public long getDynamicObjectChanged() {
		return _dynamicObjectChanged;
	}

	public long getConfigurationObjectChanged() {
		return _configurationObjectChanged;
	}

	public long getConfigurationDataChanged() {
		return _configurationDataChanged;
	}

	public int getSerializerVersion() {
		return _serializerVersion;
	}

	/**
	 * Versionsnummer des Dateiformates.
	 * @return Versionsnummer des Dateiformates
	 */
	public short getObjectVersion() {
		return _objectVersion;
	}

	private short _objectVersion;

	/** Konstruktor zu Testzwecken */
	public ConfigFileHeaderInfo(
			final int headerSize,
			final short activeVersionFile,
			final short nextActiveVersionFile,
			final File configAreaFile,
			final Map oldObjectBlocks,
			final short nextInvalidBlockVersion,
			final Map<Short, Long> configurationAuthorityVersionActivationTime,
			final long startOldDynamicObjects,
			final long startIdIndex,
			final long startPidHashCodeIndex,
			final long startMixedSet,
			final String configurationAreaPid,
			final long dynamicObjectChanged,
			final long configurationObjectChanged,
			final long configurationDataChanged,
			final Debug debug,
			final int serializerVersion,
			final short objectVersion
	) {
		_headerSize = headerSize;
		//	_headerEnd = headerEnd;
		_activeVersionFile = activeVersionFile;
		_nextActiveVersionFile = nextActiveVersionFile;
		_configAreaFile = configAreaFile;
		_oldObjectBlocks = oldObjectBlocks;
		_nextInvalidBlockVersion = nextInvalidBlockVersion;
		_configurationAuthorityVersionActivationTime = configurationAuthorityVersionActivationTime;
		_startOldDynamicObjects = startOldDynamicObjects;
		_startIdIndex = startIdIndex;
		_startPidHashCodeIndex = startPidHashCodeIndex;
		_startMixedSet = startMixedSet;
		_configurationAreaPid = configurationAreaPid;
		_dynamicObjectChanged = dynamicObjectChanged;
		_configurationObjectChanged = configurationObjectChanged;
		_configurationDataChanged = configurationDataChanged;
		_debug = debug;
		_serializerVersion = serializerVersion;
		_objectVersion = objectVersion;
	}

	/**
	 * Legt ein neues Objekt vom Typ ConfigFileHeaderInfo an. Aus der Datei, die als Parameter �bergeben wird, wird der Header seriell ausgelesen und die
	 * entsprechenden Variabelen gespeichert.
	 *
	 * @param configAreaFile "blaue Datei" aus der der Header gelesen wird.
	 *
	 * @throws IOException falls beim Einlesen ein Fehler auftritt.
	 */
	public ConfigFileHeaderInfo(File configAreaFile) throws IOException {
		_configAreaFile = configAreaFile;
		final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

		// finally f�r close der Datei
		try {

			_headerSize = file.readInt();
			final StringBuffer debugMessage = new StringBuffer();
//          headerSize ist die die L�nge des Headers ab dem vierten Byte()
			while(file.getFilePointer() < _headerSize + 4) {
				// Kennung einlesen
				final short identifier = file.readShort();
				// Anzahl Bytes, die zu der Kennung geh�ren
				final short size = file.readShort();

				switch(identifier) {
					case 1: {
						// aktuelle und zuk�nftige Version
						_activeVersionFile = file.readShort();
						_nextActiveVersionFile = file.readShort();
//						System.out.println("Kennung 1: aktive Version: " + _activeVersionFile + " n�chste aktive Version: " + _nextActiveVersionFile);
						debugMessage.append(
								"Kennung 1 (nicht ma�geblich): aktive Version: " + _activeVersionFile + " n�chste aktive Version: " + _nextActiveVersionFile + " \n"
						);
						break;
					}
					case 2: {
						// relative Startpositionen der ung�ltigen Bl�cke

						// Z�hlt die Bytes, die gelesen wurden.
						int dataRead = 0;
						final StringBuffer blockVersionsAndPositions = new StringBuffer();
						blockVersionsAndPositions.append("Kennung 2: " + "\n");
						while(dataRead < size) {
							final long relativeBlockPosition = file.readLong();
							final short version = file.readShort();
							final long timeStamp = file.readLong();
							blockVersionsAndPositions.append(
									"	Version: " + version + " Zeitstempel: " + timeStamp + " relative Position: " + relativeBlockPosition + "\n"
							);
							synchronized(_oldObjectBlocks) {
								final ConfigAreaFile.OldBlockInformations informations = new ConfigAreaFile.OldBlockInformations(
										relativeBlockPosition, timeStamp
								);
								_oldObjectBlocks.put(version, informations);
								// Was ist die derzeit gr��te Version f�r Konfigurationsobjekte, die nGa ist.
								if(version >= _nextInvalidBlockVersion) {
									// Die n�chste ung�ltige Version ist eins gr��er, als die derzeit aktuelle
									// �lteste Version
									_nextInvalidBlockVersion = (short)(version + 1);
								}
							}

							// Da der Konfigurationsbereich geladen wird, wird auch diese Map rekonstruiert.
							// Es kann passieren, das noch mindestens eine Version zu den bestehenden
							// hinzukommt, n�mlich wenn der Konfigurationsverantwortliche die Version
							// wechselt. Die �bergebene Liste localActivatedVersionTimes enth�lt dann
							// die neuen Version/Zeitpunkt Paare. Das ist zu diesem Zeitpunkt aber
							// noch unbekannt und wird durch den Aufruf von "initialVersionRestructure"
							// angezeigt.
							_configurationAuthorityVersionActivationTime.put(version, timeStamp);
							// short und 2 * long wurden gelesen
							dataRead = dataRead + 8 + 8 + 2;
						}
						debugMessage.append(blockVersionsAndPositions.toString());
//								System.out.println(blockVersionsAndPositions.toString());
						break;
					}
					case 3: {
						// relative Position ung�ltige dynamische Objekte
						_startOldDynamicObjects = file.readLong();
						debugMessage.append("Kennung 3: relative Position ung�ltig dynamisch Block " + _startOldDynamicObjects + "\n");
//								System.out.println("_startOldDynamicObjects = " + _startOldDynamicObjects);
						break;
					}
					case 4: {
						// relative Position Indexstruktur Id�s
						_startIdIndex = file.readLong();
						debugMessage.append("Kennung 4: relative Position Id-Index: " + _startIdIndex + "\n");
//								System.out.println("_startIdIndex = " + _startIdIndex);
						break;
					}
					case 5: {
						// relative Position Indexstruktur hashCode-Pid�s
						_startPidHashCodeIndex = file.readLong();
						debugMessage.append("Kennung 5: relative Position HashCodePid-Index: " + _startPidHashCodeIndex + "\n");
//								System.out.println("_startPidHashCodeIndex = " + _startPidHashCodeIndex);
						break;
					}
					case 6: {
						// relative Position der Mischobjektmenge
						_startMixedSet = file.readLong();
						debugMessage.append("Kennung 6: relative Position Mischobjektmenge: " + _startMixedSet + "\n");
//								System.out.println("_startMixedSet = " + _startMixedSet);
						break;
					}
					case 7: {
						// Pid des Konfigurationsbereichs

						// L�nge des Strings
						final byte pidStringSize = file.readByte();
						final byte pidAsBytes[] = new byte[pidStringSize];
						file.readFully(pidAsBytes);
						_configurationAreaPid = new String(pidAsBytes, "ISO-8859-1");
//						System.out.println("Kennung 7: Pid des Konfigurationsbereichs: " + _configurationAreaPid);
						debugMessage.append("Kennung 7: Pid des Konfigurationsbereichs: " + _configurationAreaPid + "\n");

						break;
					}
					case 8: {
						// Zeitstempel einlesen
						_dynamicObjectChanged = file.readLong();
						_configurationObjectChanged = file.readLong();
						_configurationDataChanged = file.readLong();
						final String format = "dd.MM.yyyy HH:mm:ss,SSS";
						final DateFormat timeFormat = new SimpleDateFormat(format);
						break;
					}
					case 9: {
						// Versionsnummer unter der die Objekte gespeichert wurden
						_objectVersion = file.readShort();
//								System.out.println("Kennung 9: Version unter der die Objekte erstellt wurden " + _objectVersion);
						debugMessage.append("Kennung 9: Version unter der die Objekte erstellt wurden " + _objectVersion + "\n");
						break;
					}
					case 10: {
						// Serialisiererversion einlesen
						_serializerVersion = file.readInt();
//								System.out.println("Kennung 10: Serialisiererversion " + _serializerVersion);
						debugMessage.append("Kennung 10: Serialisiererversion " + _serializerVersion + "\n");
						break;
					}
					default: {
						// Die Kennung ist unbekannt, sie wird �bersprungen

						byte unknownIdentifier[] = new byte[size];
						file.readFully(unknownIdentifier);
						_debug.warning(
								"Im Header eines Konfigurationsbereichs " + _configAreaFile + " konnte eine Kennung nicht erkannt werden. Kennung " + identifier
								+ " Anzahl Bytes der Kennung " + size + " Daten der Kennung " + HexDumper.toString(unknownIdentifier)
						);
						break;
					}
				}
			} // while

			_debug.config(debugMessage.toString());

			// Der Header wurde eingelesen

//				System.out.println("L�nge: " + headerSize + " Fileposition " + file.getFilePointer());
			_headerEnd = file.getFilePointer();
		}
		finally {
			file.close();
		}
	}

	public SystemObjectInformationInterface getObject(long filePosition) {
		return null;  
	}

	public ConfigFileHeaderInfo getHeader() {
		return this;
	}

	public int getHeaderSize() {
		return _headerSize;
	}
}
