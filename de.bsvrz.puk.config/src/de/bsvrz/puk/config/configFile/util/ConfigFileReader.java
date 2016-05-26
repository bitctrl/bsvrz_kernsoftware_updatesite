/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.util;

import de.bsvrz.dav.daf.util.HashBagMap;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileHeaderInfo;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.hexdump.HexDumper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.InflaterInputStream;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class ConfigFileReader {

	private static final boolean SHOW_GAP_INFO = false;
	private static final boolean SHOW_DATA_BYTES = false;
	final Map<Long, Long> _idMap = new HashMap<Long, Long>();
	final Map<Long, Integer> _pidMap = new HashMap<Long, Integer>();
	final HashBagMap<Long, Long> _positionsPerId = new HashBagMap<Long, Long>();
	final List<String> _errorList = new ArrayList<String>();
	private ConfigFileHeaderInfo _configFileHeaderInfo;
	private File _configAreaFile;
	private int _serializerVersion;

	public ConfigFileReader(final File configAreaFile) throws IOException, NoSuchVersionException {
		System.out.println("==== Datei-Informationen ====");
		_configAreaFile = configAreaFile;
		System.out.println("Dateiname:  " + _configAreaFile.getName());
		System.out.println("Dateigröße: " + _configAreaFile.length());
		System.out.println();
		System.out.println();
		System.out.println("==== Header ====");
		System.out.flush();
		System.err.flush();
		_configFileHeaderInfo = new ConfigFileHeaderInfo(_configAreaFile);
		System.out.flush();
		System.err.flush();
		_serializerVersion = _configFileHeaderInfo.getSerializerVersion();
		System.out.println("Länge des Headers:              " + _configFileHeaderInfo.getHeaderSize());
		System.out.println("Ende des Headers:               " + _configFileHeaderInfo.getHeaderEnd());
		System.out.println("Pid:                            " + _configFileHeaderInfo.getConfigurationAreaPid());
		System.out.println("SerializerVersion:              " + _serializerVersion);
		System.out.println("Aktive Version (Header):        " + _configFileHeaderInfo.getActiveVersionFile());
		System.out.println("Nächste Version (Header):       " + _configFileHeaderInfo.getNextActiveVersionFile());
		System.out.println("Nächste Version (real):         " + (_configFileHeaderInfo.getNextInvalidBlockVersion()));
		System.out.println("Objektversion:                  " + _configFileHeaderInfo.getObjectVersion());
		System.out.println("Konfigurationsdaten geändert:   " + _configFileHeaderInfo.getConfigurationDataChanged());
		System.out.println("Konfigurationsobjekte geändert: " + _configFileHeaderInfo.getConfigurationObjectChanged());
		System.out.println();
		System.out.println("Offset alte dynamische Objekte: " + _configFileHeaderInfo.getStartOldDynamicObjects());
		System.out.println("Offset Id-Index:                " + _configFileHeaderInfo.getStartIdIndex());
		System.out.println("Offset Pid-Hashcode-Index:      " + _configFileHeaderInfo.getStartPidHashCodeIndex());
		System.out.println("Offset Mixed Objects:           " + _configFileHeaderInfo.getStartMixedSet());
		System.out.println();
		System.out.println();

		assertEquals(_configFileHeaderInfo.getHeaderEnd(), _configFileHeaderInfo.getHeaderSize() + 4, "Falsche Header-Länge");

		readOldConfigBlocks();
		readOldDynamicBlock();
		readIndex();
		readMixedObjectSetObjects();

		if(_configFileHeaderInfo.getActiveVersionFile() != _configFileHeaderInfo.getNextInvalidBlockVersion() - 1){
			System.out.println("Warnung: Falsche aktive Version" + ": " + (long) _configFileHeaderInfo.getActiveVersionFile() + " != " + (long) (_configFileHeaderInfo.getNextInvalidBlockVersion() - 1));
			System.out.println();
		}

		for(Map.Entry<Long, Collection<Long>> entry : _positionsPerId.entrySet()) {
			if(entry.getValue().size() > 1){
				appendError("Objekt mit ID " + entry.getKey() + " befindet sich mehrfach in der Datei an folgenden Positionen: " + entry.getValue());
			}
		}

		System.out.println("==== Gefundene Fehler ====");
		System.out.println("Anzahl: " + _errorList.size());
		for(final String s : _errorList) {
			System.out.println(s);
		}
	}

	public List<String> getErrorList() {
		return _errorList;
	}

	public static void main(String[] args) throws Exception {
		Debug.init("ConfigFileReader", new ArgumentList(new String[]{"-debugLevelStdErrText=CONFIG"}));
		new ConfigFileReader(new File(args[0]));
	}

	private void readOldDynamicBlock() throws IOException, NoSuchVersionException {
		System.out.println("==== Block mit alten dynamischen Objekten ====");
		readOldObjectBlock(
				_configFileHeaderInfo.getStartOldDynamicObjects() + _configFileHeaderInfo.getHeaderEnd(),
				Long.MAX_VALUE,
				(_configFileHeaderInfo.getStartIdIndex() + _configFileHeaderInfo.getHeaderEnd())
		);
		System.out.println();
		System.out.println();
	}

	private void readOldConfigBlocks() throws IOException, NoSuchVersionException {
		System.out.println("==== Blöcke mit alten Konfigurationsobjekten ====");
		for(int i = 2; i < _configFileHeaderInfo.getNextInvalidBlockVersion(); i++) {
			ConfigAreaFile.OldBlockInformations block = _configFileHeaderInfo.getOldObjectBlocks().get((short) i);
			System.out.println("  == Objekte ungültig in Version " + i);
			if(block != null) {
				System.out.println(
						"  Aktivierungszeit:  " + new SimpleDateFormat().format(
								new Date(block.getTimeStamp())
						)
				);
				System.out.println("  Relative Position: " + block.getFilePosition());
				System.out.println();
				if(block.getFilePosition() >= 0) {
					// nächsten Block herausfinden, der Daten hat
					int n = 1;
					ConfigAreaFile.OldBlockInformations nextBlock;
					do {
						nextBlock = _configFileHeaderInfo.getOldObjectBlocks().get((short) (i + n));
						n++;
					}
					while(nextBlock != null && nextBlock.getFilePosition() < 0);
					long readEnd;
					if(nextBlock != null) {
						readEnd = nextBlock.getFilePosition() + _configFileHeaderInfo.getHeaderEnd();
					}
					else {
						readEnd = _configFileHeaderInfo.getStartOldDynamicObjects() + _configFileHeaderInfo.getHeaderEnd();
					}
					readOldObjectBlock(
							block.getFilePosition() + _configFileHeaderInfo.getHeaderEnd(),
							i,
							readEnd
					);
				}
				else {
					System.out.println("Keine Objekte");
				}
			}
			else {
				System.out.println("Keine Informationen");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	private void readOldObjectBlock(final long filePosition, final long version, final long readEnd) throws IOException, NoSuchVersionException {
		final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");
		try {
			file.seek(filePosition);
			// Es müssen solange Daten gelesen werden, bis der dynamische nGa-Bereich erreicht wird

			// Solange Daten aus den nGa-Blöcken lesen, bis alle nGa geprüft wurden
			while(file.getFilePointer() < readEnd) {

				long pos = file.getFilePointer();
				final int len = file.readInt();
				final long objectId = file.readLong();
				if(objectId > 0) {
					// Es ist ein Objekt und keine Lücke

					final int pidHashCode = file.readInt();

					final long typeId = file.readLong();

					// 0 = Konfobjekt, 1 = dyn Objekt
					final byte objectType = file.readByte();

					// Das kann entweder ein Zeitpunkt oder eine Version sein
					final long firstInvalid;
					final long firstValid;

					if(objectType == 0) {
						firstInvalid = file.readShort();
						firstValid = file.readShort();
					}
					else {
						firstInvalid = file.readLong();
						firstValid = file.readLong();
					}

					_idMap.put(pos, objectId);
					_pidMap.put(pos, pidHashCode);
					_positionsPerId.add(objectId, pos);

					if(firstInvalid > version) {
						System.out.println("Gültig:              Ja");
					}

					System.out.println("Position:            " + pos);
					System.out.println("Länge:               " + len);
					System.out.println("Id:                  " + objectId);
					System.out.println("PidHashCode:         " + pidHashCode);
					System.out.println("Objekttyp-Id:        " + typeId);
					System.out.println(
							"Objekttyp:           " + (objectType == 0
									? "Konfigurationsobjekt"
									: objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
					);
					System.out.println("Gültige Version:     " + firstValid);
					System.out.println("Ungültige Version:   " + firstInvalid);
					readObjectFromFile(
							len, objectType, file
					);
					System.out.println();
				}
				else {
					System.out.println("Gelöschtes Objekt:   " + len + " bytes");
					if(!SHOW_GAP_INFO) {
						// Eine Lücke, der filePointer muss verschoben werden.
						// Die Länge bezieht sich auf das gesamte Objekt, ohne die Länge selber.
						// Also ist die nächste Länge bei "aktuelle Position + Länge - 8.
						// - 8, weil die Id bereits gelesen wurde und das ist ein Long.
						file.seek(file.getFilePointer() + len - 8);
					}
					else {
						final int pidHashCode = file.readInt();

						final long typeId = file.readLong();

						// 0 = Konfobjekt, 1 = dyn Objekt
						final byte objectType = file.readByte();

						// Das kann entweder ein Zeitpunkt oder eine Version sein
						final long firstInvalid;
						final long firstValid;

						if(objectType == 0) {
							firstInvalid = file.readShort();
							firstValid = file.readShort();
						}
						else {
							firstInvalid = file.readLong();
							firstValid = file.readLong();
						}

						System.out.println("PidHashCode:         " + pidHashCode);
						System.out.println("Objekttyp-Id:        " + typeId);
						System.out.println(
								"Objekttyp:           " + (objectType == 0
										? "Konfigurationsobjekt"
										: objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
						);
						System.out.println("Gültige Version:     " + firstValid);
						System.out.println("Ungültige Version:   " + firstInvalid);
						readObjectFromFile(
								len, objectType, file
						);
					}
				}
				System.out.println();
			}
			assertEquals(file.getFilePointer(), readEnd, "Ende des Blocks stimmt nicht");
		}
		finally {
			file.close();
		}
	}

	private void readObjectFromFile(
			final int objectsize, final byte objecttype, RandomAccessFile file) throws IOException, NoSuchVersionException {

		if(objecttype == 0) {
			// Konfigurationsobjekt

			// Der vordere Teil ist konstant, also kann die Länge der gepackten Daten berechnet werden.
			// id, pidHash, typeId, type(Konf oder dynamische), Version, Version abziehen
			final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 2 - 2;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			file.read(packedBytes);

			// Byte-Array, das die ungepackten Daten enthält
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
			System.out.println("Name:                " + name);
			System.out.println("Pid:                 " + pid);


			// Menge der konfigurierenden Datensätze
			final int numberOfConfigurationData = deserializer.readInt();
			for(int nr = 0; nr < numberOfConfigurationData; nr++) {
				// ATG-Verwendung einlesen
				final long atgUseId = deserializer.readLong();
				// Länge der Daten
				final int sizeOfData = deserializer.readInt();
				final byte[] data = deserializer.readBytes(sizeOfData);
				System.out.println("Konfigurationsdaten: " + data.length + " bytes, ATGU: " + atgUseId);
				if(SHOW_DATA_BYTES) HexDumper.dumpTo(System.out, data);
			}

			// alle Daten einlesen, die spezifisch für ein Konfigurationsobjekt sind und
			// direkt am Objekt hinzufügen

			// Anzahl Mengen am Objekt
			final short numberOfSets = deserializer.readShort();

			for(int nr = 0; nr < numberOfSets; nr++) {
				final long setId = deserializer.readLong();
				System.out.println("SetId:               " + setId);
				final int numberOfObjects = deserializer.readInt();

				for(int i = 0; i < numberOfObjects; i++) {
					// Alle Objekte der Menge einlesen

					// Id des Objekts, das sich in Menge befinden
					final long setObjectId = deserializer.readLong();
					System.out.println("SetObjectId:         " + setObjectId);
				}
			}
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

			// Byte-Array, das die ungepackten Daten enthält
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

			System.out.println("Name:                " + name);
			System.out.println("Pid:                 " + pid);
			System.out.println("Simulationsvariante: " + simulationVariant);

			final int numberOfConfigurationData = deserializer.readInt();
			for(int nr = 0; nr < numberOfConfigurationData; nr++) {
				// ATG-Verwendung einlesen
				final long atgUseId = deserializer.readLong();
				// Länge der Daten
				final int sizeOfData = deserializer.readInt();
				final byte[] data = deserializer.readBytes(sizeOfData);
				System.out.println("Konfigurationsdaten: " + data.length + " bytes, ATGU: " + atgUseId);
			}
		}
		else {
			final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 8 - 8 - 2;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			file.readFully(packedBytes);
		}
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
			_errorList.add(e.getMessage());
		}
		return unzippedData.toByteArray();
	}

	private void readIndex() throws IOException {
		System.out.println("==== Id-Index ====");
		RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");
		try {
			file.seek(_configFileHeaderInfo.getStartIdIndex() + _configFileHeaderInfo.getHeaderEnd());
			while(file.getFilePointer() < (_configFileHeaderInfo.getStartPidHashCodeIndex() + _configFileHeaderInfo.getHeaderEnd())) {
				final long id = file.readLong();
				final long pos = getAbsoluteFilePositionForInvalidObjects(file.readLong());

				System.out.println("Id:       " + id);
				System.out.println("Position: " + pos);
				if(!Long.valueOf(id).equals(_idMap.get(pos))) {
					appendError("Id-Index verweist auf falsches Objekt. Erwartet: " + id + ". Ist: " + _idMap.get(pos));
				}

				System.out.println();
			}

			System.out.println();
			System.out.println();
			System.out.println("==== Pid-Index ====");

			// Pid Index einlesen
			file.seek(_configFileHeaderInfo.getStartPidHashCodeIndex() + _configFileHeaderInfo.getHeaderEnd());

			while(file.getFilePointer() < (_configFileHeaderInfo.getStartMixedSet() + _configFileHeaderInfo.getHeaderEnd())) {
				final int pid = file.readInt();
				final long pos = getAbsoluteFilePositionForInvalidObjects(file.readLong());

				System.out.println("Pid:      " + pid);
				System.out.println("Position: " + pos);
				if(!Integer.valueOf(pid).equals(_pidMap.get(pos))) {
					appendError("Pid-Index verweist auf falsches Objekt. Erwartet: " + pid + ". Ist: " + _pidMap.get(pos));
				}
				System.out.println();
			}
		}
		finally {
			file.close();
		}
		System.out.println();
		System.out.println();
	}

	private void appendError(final String error) {
		_errorList.add(error);
	}

	private void assertEquals(final long a, final long b, final String error) {
		if(a != b) _errorList.add(error + ": " + a + " != " + b);
	}

	private long getAbsoluteFilePositionForInvalidObjects(long relativeFilePosition) {
		if(relativeFilePosition > 0) {
			// Es handelt sich um dynamisches Objekt, das sich in der dyn. nGa Menge befindet.
			// Die relative Positionsangabe bezieht sich auf den Beginn des dyn. nGa Bereichs.
			// Die relative Position ist immer um +1 erhöht worden, damit wurde eine "doppelte 0" verhindert.
			// Die "0" gehört zu den Konfigurationsobjekten.
			return ((_configFileHeaderInfo.getStartOldDynamicObjects()) + relativeFilePosition + _configFileHeaderInfo.getHeaderEnd()) - 1;
		}
		else {
			// Es handelt sich um ein Konfigurationsobjekt. Die relative Position bezieht sich auf das
			// Headerende.
			return (relativeFilePosition * (-1) + _configFileHeaderInfo.getHeaderEnd());
		}
	}

	private void readMixedObjectSetObjects() throws IOException, NoSuchVersionException {

		System.out.println("==== Block mit aktuellen und zukünftigen Objekten ====");

		final long startingPosition;

		startingPosition = (_configFileHeaderInfo.getStartMixedSet() + _configFileHeaderInfo.getHeaderEnd());


		// Datei öffnen
		final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

		try {

			// Datei auf den Anfang der Mischmenge postieren
			file.seek(startingPosition);

			// Wie gross ist die Datei
			final long fileSize = file.length();

			// Wird true, wenn das Objekt, das den Konfigurationsbereich wiederspiegelt, gefunden wurde
			while(file.getFilePointer() < fileSize) {

				// speichert die Dateiposition des Objekts. Diese Position wird später
				// am Objekt gespeichert
				final long pos = file.getFilePointer();

				// Länge des Blocks einlesen
				final int sizeOfObject = file.readInt();

				// Id des Objekts einlesen
				final long objectId = file.readLong();

				if(objectId > 0) {
					// Es ist ein Objekt und keine Lücke

					_positionsPerId.add(objectId, pos);

					final int pidHashCode = file.readInt();

					final long typeId = file.readLong();

					// 0 = Konfobjekt, 1 = dyn Objekt
					final byte objectType = file.readByte();

					// Das kann entweder ein Zeitpunkt oder eine Version sein
					final long firstInvalid;
					final long firstValid;

					if(objectType == 0) {
						firstInvalid = file.readShort();
						firstValid = file.readShort();
					}
					else {
						firstInvalid = file.readLong();
						firstValid = file.readLong();
					}

					System.out.println("Position:            " + pos);
					System.out.println("Länge:               " + sizeOfObject);
					System.out.println("Id:                  " + objectId);
					System.out.println("PidHashCode:         " + pidHashCode);
					System.out.println("Objekttyp-Id:        " + typeId);
					System.out.println(
							"Objekttyp:           " + (objectType == 0
									? "Konfigurationsobjekt"
									: objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
					);
					System.out.println("Gültige Version:     " + firstValid);
					System.out.println("Ungültige Version:   " + firstInvalid);
					readObjectFromFile(
							sizeOfObject, objectType, file
					);
					System.out.println();
				}
				else {
					System.out.println("Gelöschtes Objekt:   " + sizeOfObject + " bytes");
					if(!SHOW_GAP_INFO) {
						// Eine Lücke, der filePointer muss verschoben werden.
						// Die Länge bezieht sich auf das gesamte Objekt, ohne die Länge selber.
						// Also ist die nächste Länge bei "aktuelle Position + Länge - 8.
						// - 8, weil die Id bereits gelesen wurde und das ist ein Long.
						file.seek(file.getFilePointer() + sizeOfObject - 8);
					}
					else {
						final int pidHashCode = file.readInt();

						final long typeId = file.readLong();

						// 0 = Konfobjekt, 1 = dyn Objekt
						final byte objectType = file.readByte();

						// Das kann entweder ein Zeitpunkt oder eine Version sein
						final long firstInvalid;
						final long firstValid;

						if(objectType == 0) {
							firstInvalid = file.readShort();
							firstValid = file.readShort();
						}
						else {
							firstInvalid = file.readLong();
							firstValid = file.readLong();
						}
						System.out.println("PidHashCode:         " + pidHashCode);
						System.out.println("Objekttyp-Id:        " + typeId);
						System.out.println(
								"Objekttyp:           " + (objectType == 0
										? "Konfigurationsobjekt"
										: objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
						);
						System.out.println("Gültige Version:     " + firstValid);
						System.out.println("Ungültige Version:   " + firstInvalid);
						readObjectFromFile(
								sizeOfObject, objectType, file
						);
					}
					System.out.println();
				}
			}// while
		}
		finally {
			file.close();
		}
	}

}
