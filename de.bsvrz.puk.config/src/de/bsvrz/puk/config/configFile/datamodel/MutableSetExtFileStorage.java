/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.util.BufferedRandomAccessFile;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface um die Speicherung einer dynamischem Menge ({@linkplain ConfigMutableSet})
 * zu realisieren. Diese werden entweder als Konfigurationsdatensatz oder als eigene Datei gespeichert, je nach Implementierung
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class MutableSetExtFileStorage extends MutableSetStorage {
	
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Datei in der die Elementzugehörigkeit dieser Menge gespeichert werden soll, oder <code>null</code>, falls die Elementzugehörigkeit als Datensatz der
	 * Menge gespeichert werden soll
	 */
	private File _elementsFile;

	private ConfigMutableSet _mutableSet;

	private List<MutableElement> _saveElementLater;

	public MutableSetExtFileStorage(final File elementsFile, final ConfigMutableSet mutableSet) {
		_elementsFile = elementsFile;
		_mutableSet = mutableSet;
	}

	/**
	 * Speichert die Elemente dieser Menge (auch historische) in einem konfigurierenden Datensatz ab.
	 * 
	 * @param mutableElements
	 *            Elemente dieser Menge
	 *
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationChangeException
	 *             Falls die Elemente nicht in einem konfigurierenden Datensatz abgespeichert werden können.
	 */
	@Override
	protected void writeElements(final List<MutableElement> mutableElements) throws ConfigurationChangeException {
		_saveElementLater = mutableElements;
		_mutableSet.getDataModel().saveSetElementsFileLater(this);
	}

	@Override
	public String toString() {
		return _elementsFile.toString();
	}

	public synchronized void saveElementsData() {
		List<MutableElement> elementsToSave = _saveElementLater;
		if(elementsToSave == null) return;
		if(_elementsFile.isFile()) {
			final File backupFile = new File(_elementsFile.getParentFile(), _elementsFile.getName() + ".old");
			if(backupFile.exists()) {
				if(!backupFile.delete()){
					_debug.warning("Backup-Datei kann nicht gelöscht werden", backupFile);
				}
			}
			if(!_elementsFile.renameTo(backupFile)){
				_debug.warning("Backup-Datei kann nicht erstellt werden", backupFile);
			}
		}
		try {
			writeElementsToFile(elementsToSave, _elementsFile);
		}
		catch(IOException e){
			_debug.error("Fehler beim Erzeugen der Datei mit der Elementzugehörigkeit einer dynamischen Menge " + _elementsFile, e);
		}
		_saveElementLater = null;
	}

	public void createBackupFile(final File targetDirectory) throws IOException {
		File targetFile = new File(targetDirectory, _elementsFile.getName());
		writeElementsToFile(readElements(), targetFile);
	}

	public void writeElementsToFile(final List<MutableElement> elementsToSave, final File file) throws IOException {
		BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(file, "rw");
		try {
			// Version
			bufferedRandomAccessFile.writeByte(1);
			// Size
			int byteSize = MutableElement.BYTE_SIZE;
			int len = elementsToSave.size() * byteSize;
			bufferedRandomAccessFile.writeInt(len);
			bufferedRandomAccessFile.setLength(5 + len);
			// Bytes
			for(MutableElement mutableElement : elementsToSave) {
				bufferedRandomAccessFile.writeLong(mutableElement.getId());
				bufferedRandomAccessFile.writeLong(mutableElement.getStartTime());
				bufferedRandomAccessFile.writeLong(mutableElement.getEndTime());
				bufferedRandomAccessFile.writeShort(mutableElement.getSimulationVariant());
			}
		}
		finally {
			bufferedRandomAccessFile.close();
		}
	}

	/**
	 * Diese Methode liest den konfigurierenden Datensatz für die Elemente dieser Menge ein und gibt sie in einer Liste zurück.
	 * 
	 * @return eine Liste von Elementen mit Zeitstempeln, die die Zugehörigkeitszeiträume repräsentieren
	 */
	protected List<MutableElement> readElements() {
		saveElementsData();
		// die eingelesenen Elemente werden nicht alle vorgehalten, da dies auf Dauer zu viele werden können
		final List<MutableElement> mutableElements = new ArrayList<MutableElement>();
		try {
			
			byte[] bytes;
			if(_elementsFile.isFile() && _elementsFile.canRead()) {
				final FileInputStream in = new FileInputStream(_elementsFile);
				final DataInputStream din = new DataInputStream(in);
				try {
					final byte version = din.readByte();
					if(version == 1) {
						final int size = din.readInt();
						final byte[] readBytes = new byte[size];
						din.readFully(readBytes);
						bytes = readBytes;
					}
					else {
						final String errorMessage = "Elemente der dynamischen Menge " + _mutableSet.getNameOrPidOrId() + " konnten nicht ermittelt werden, unbekannte Version " + version;
						_debug.error(errorMessage);
						throw new RuntimeException(errorMessage);
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
				bytes = new byte[0];
			}
			final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			final Deserializer deserializer = SerializingFactory.createDeserializer(_mutableSet.getSerializerVersion(), in);
			assert bytes.length % MutableElement.BYTE_SIZE == 0 : "Format des Byte-Arrays für die Elemente einer Menge " + _mutableSet.getNameOrPidOrId()
			        + " hat sich geändert. Länge muss durch " + MutableElement.BYTE_SIZE + " teilbar sein.";
			int numberOfElements = bytes.length / MutableElement.BYTE_SIZE;
			for(int i = 0; i < numberOfElements; i++) {
				long id = deserializer.readLong();
				long startTime = deserializer.readLong(); // Zeit, ab der das Element zur Menge gehört
				long endTime = deserializer.readLong(); // Zeit, ab der das Element nicht mehr zur Menge gehört
				short simulationVariant = deserializer.readShort(); // Simulationsvariante dieses Objekt, in der es zur Menge hinzugefügt oder aus der Menge entfernt wurde
				final SystemObject object = _mutableSet.getDataModel().getObject(id);

				if(object == null) {
					_debug.warning(
							"Element mit Id '" + id + "' kann nicht der Menge '" + _mutableSet.getPidOrNameOrId()
									+ "' hinzugefügt werden, da es kein System-Objekt hierzu gibt."
					);
				}
				mutableElements.add(new MutableElement(object, startTime, endTime, simulationVariant));
			}
			in.close();
			return mutableElements;
		}
		catch(IllegalArgumentException ex) {
			final String errorMessage = "Elemente der dynamischen Menge '" + _mutableSet.getNameOrPidOrId()
			        + "' konnten nicht ermittelt werden (evtl. wurde die Menge neu angelegt)";
			_debug.finest(errorMessage, ex.getMessage());
		}
		catch(Exception ex) {
			final String errorMessage = "Elemente der dynamischen Menge " + _mutableSet.getNameOrPidOrId() + " konnten nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		return mutableElements;
	}
}
