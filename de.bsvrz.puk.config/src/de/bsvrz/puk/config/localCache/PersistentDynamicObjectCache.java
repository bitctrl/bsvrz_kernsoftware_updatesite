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

package de.bsvrz.puk.config.localCache;

import de.bsvrz.dav.daf.main.config.*;

import java.io.*;
import java.util.HashMap;

/**
 * Klasse zur persistenten Speicherung dynamischer Objekte
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class PersistentDynamicObjectCache implements ObjectLookup {

	private final HashMap<Long, PersistentDynamicObject> _objectsById = new HashMap<Long, PersistentDynamicObject>();

	private final HashMap<String, PersistentDynamicObject> _objectsByPid = new HashMap<String, PersistentDynamicObject>();

	public SystemObject getObject(final String pid) {
		if(pid.length() == 0) return null;
		return _objectsByPid.get(pid);
	}

	public SystemObject getObject(final long id) {
		return _objectsById.get(id);
	}

	/**
	 * Speichert ein Objekt in diesem Cache
	 * @param dynamicObject Dynamisches Objekt
	 */
	public void storeObject(final DynamicObject dynamicObject) {
		final PersistentDynamicObject persistentDynamicObject = new PersistentDynamicObject(dynamicObject);
		_objectsById.put(persistentDynamicObject.getId(), persistentDynamicObject);
		final String pid = persistentDynamicObject.getPid();
		if(pid.length() > 0){
			_objectsByPid.put(pid, persistentDynamicObject);
		}
	}

	/**
	 * Schreibt den ganzen Cache auf die Festplatte
	 * @param output Ausgabedatei
	 * @throws IOException Bei IO-Fehler
	 */
	public void writeToDisk(final File output) throws IOException {
		final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		try {
			// Version
			dataOutputStream.writeInt(0);
			// Anzahl Einträge
			dataOutputStream.writeInt(_objectsById.size());
			// Nacheinander alle Einträge
			for(final PersistentDynamicObject object : _objectsById.values()) {
				object.write(dataOutputStream);
			}
		}
		finally {
			dataOutputStream.close();
		}
	}

	/**
	 * Initialisiert einen neuen Cache mit einer Datei von der Festplatte
	 * @param dataModel Datenmodell
	 * @param input Datei
	 * @throws java.io.IOException Bei IO-Fehler
	 */
	public PersistentDynamicObjectCache(final DataModel dataModel, final File input) throws IOException {
		if(!input.exists()) return;
		final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(input)));
		try {
			final int version = dataInputStream.readInt();
			if(version != 0) return;
			final int size = dataInputStream.readInt();
			for(int i = 0; i < size; i++) {
				final PersistentDynamicObject persistentDynamicObject = PersistentDynamicObject.read(dataInputStream, dataModel);
				_objectsById.put(persistentDynamicObject.getId(), persistentDynamicObject);
				final String pid = persistentDynamicObject.getPid();
				if(pid.length() > 0){
					_objectsByPid.put(pid, persistentDynamicObject);
				}
			}
		}
		finally {
			dataInputStream.close();
		}
	}

	/**
	 * Initialisiert einen neuen leeren Cache
	 */
	public PersistentDynamicObjectCache() {
	}

	@Override
	public String toString() {
		return "PersistentDynamicObjectCache{}";
	}
}
