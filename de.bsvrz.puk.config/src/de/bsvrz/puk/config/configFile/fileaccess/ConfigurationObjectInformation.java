/*
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

import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Dieses Objekt stellt alle Informationen zur Verf�gung, die f�r ein Konfigurationsobjekt relevant sind. Die Methoden sind Thread-sicher. <br>
 * <p/>
 * Falls das Objekt in eine Datei geschrieben werden soll, sollte das Objekt zum synchronisieren benutzt werden. Dies verhindert, dass sich das Objekt w�hrend
 * des Schreibvorgangs ver�ndert, lesende Zugriffe sind weiterhin m�glich.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5501 $ / $Date: 2007-11-17 04:22:14 +0100 (Sat, 17 Nov 2007) $ / ($Author: rs $)
 */
public class ConfigurationObjectInformation extends SystemObjectInformation implements ConfigurationObjectInfo {

	private final short _firstValidVersion;

	private short _firstInvalidVersion = 0;

	/**
	 * Speichert alle Mengen, die zu diesem Objekt geh�ren. Als Schl�ssel dient die Id der Menge. Als value wird eine Menge zur�ckgegeben. In dieser Menge sind
	 * alle Objekte gespeichert, die sich in der Menge befinden.
	 */
	private final Map<Long, Set<Long>> _sets = new HashMap<Long, Set<Long>>();

	private final ConfigAreaFile _configAreaFile;

	/** Speichert die letzte abselute Position ab, an der das Objekt gespeichert wurde. */
	private long _lastFilePosition = -1;

	private boolean _saveModifications;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/**
	 * Der Zeitpunkt, wann das Objekt ung�ltig wird, wird automatisch auf 0 gesetzt
	 *
	 * @param configAreaFile    der Konfigurationsbereich
	 * @param id                Id des Objekts
	 * @param pid               Pid des Objekts
	 * @param typeId            Typ des Objekts, dieser wird �ber die Id des Typs identifiziert
	 * @param name              Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param firstValidVersion Version, mit der das Objekt g�tlig wird
	 * @param saveModifications true = Das Objekt wird angelegt und in die Datei des Konfigurationsbereichs sp�ter gespeichert, werden �nderungen vorgenommen
	 *                          (Datens�tze ge�ndert, usw), so werden die �nderungen gespeichert; false = Das Objekt wird angelegt und nicht gespeichert (beim
	 *                          laden des Objekts aus der Datei w�re dies sinnvoll), acuh Modifikationen am Objekt werden nicht gespeichert, damit gespeichert wird
	 *                          muss {@link #saveObjectModifications} aufgerufen werden
	 */
	public ConfigurationObjectInformation(
			ConfigAreaFile configAreaFile, long id, String pid, long typeId, String name, short firstValidVersion, boolean saveModifications
	) {
		this(id, pid, typeId, name, firstValidVersion, (short)0, configAreaFile, saveModifications);
	}

	/**
	 * @param id                  Id des Objekts
	 * @param pid                 Pid des Objekts
	 * @param typeId              Typ des Objekts, dieser wird �ber die Id des Typs identifiziert
	 * @param name                Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param firstValidVersion   Version, mit der das Objekt g�tlig wird
	 * @param firstInvalidVersion Version, mit der das Objekt ung�tlig wird
	 * @param configAreaFile      der Konfigurationsbereich
	 * @param saveModifications   true = Das Objekt wird angelegt und in die Datei des Konfigurationsbereichs sp�ter gespeichert, werden �nderungen vorgenommen
	 *                            (Datens�tze ge�ndert, usw), so werden die �nderungen gespeichert; false = Das Objekt wird angelegt und nicht gespeichert (beim
	 *                            laden des Objekts aus der Datei w�re dies sinnvoll), acuh Modifikationen am Objekt werden nicht gespeichert, damit gespeichert
	 *                            wird muss {@link #saveObjectModifications} aufgerufen werden
	 */
	public ConfigurationObjectInformation(
			long id,
			String pid,
			long typeId,
			String name,
			short firstValidVersion,
			short firstInvalidVersion,
			ConfigAreaFile configAreaFile,
			boolean saveModifications
	) {
		super(id, pid, typeId, name, configAreaFile, saveModifications);

		if(firstValidVersion <= 0) {
			throw new IllegalArgumentException(
					"Ein Konfigurationsobjekt, Pid: " + pid + " firstValidVersion: " + firstValidVersion + " firstInvalidVersion: " + firstInvalidVersion
					+ " KB: " + configAreaFile.getConfigAreaPid()
			);
		}

		_configAreaFile = configAreaFile;
		_firstValidVersion = firstValidVersion;
		_firstInvalidVersion = firstInvalidVersion;
		_saveModifications = saveModifications;

		if(_saveModifications) {
			_configAreaFile.objectModified(this);
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein Objekt aus der Datei eingeladen wurde und im Konstruktor saveModifications == true �bergeben wurde. Nach Aufruf
	 * dieser Methode, werden alle �nderungen wieder gespeichert. Wurde also saveModifications == true gesetzt, so muss diese MEthode aufgerufen werden, damit neue
	 * �nderungen gespeichert werden.
	 */
	public void saveObjectModifications() {
		_saveModifications = true;
		super.saveObjectModificationsSystemObject();
	}

	public short getFirstValidVersion() {
		return _firstValidVersion;
	}

	/**
	 * Gibt die Version zur�ck, mit der das Objekt ung�ltig wird. Der Zugriff ist synchonisiert, weil zum Zeitpunkt des Zugriffs, das Objekt gerade gesetzt werden
	 * k�nnte.
	 *
	 * @return Version, mit der das Objekt ung�ltig wird
	 */
	synchronized public short getFirstInvalidVersion() {
		return _firstInvalidVersion;
	}

	/**
	 * Das Konfigurationsobjekt wird mit der n�chsten Version des Konfigurationsbereichs ung�ltig. Diese Objekte m�ssen nicht sofort gespeichert werden, da sie bis
	 * zum beenden der Konfiguration im Speicher bleiben.
	 * <p/>
	 * War das Objekt noch nicht g�ltig, so wird es direkt gel�scht.
	 *
	 * @see #revalidate
	 */
	synchronized public void invalidate() {

		// Nur wenn das Objekt noch g�ltig ist, kann es auf ung�ltig gesetzt werden. Ohne diese Abfrage werden auch Objekte im nGa-Bereich
		// f�lschlicherweise als L�cke deklariert.
		if(_firstInvalidVersion == 0) {
			// Das Objekt wird in der Version ung�ltig in der es auch g�ltig werden w�rde -> Es kann gel�scht werden.
			// Es kann also gar nicht aktiviert werden.
			if(_firstValidVersion == _configAreaFile.getNextActiveVersion()) {
				_debug.fine(
						"Das Objekt Id" + super.getID() + " wird gel�scht, Version mit der das Objekt g�ltig werden sollte " + _firstValidVersion
						+ " aktive Version des Konfigurationsbereichs " + _configAreaFile.getNextActiveVersion()
				);

				// Das Objekt kann sich noch im Puffer befinden, dort muss es vorher entfernt werden. Sonst wird es beim n�chsten flush wieder geschrieben
				_configAreaFile.dontSaveObject(this);

				// Das Objekt wird als L�cke erkl�rt und somit gel�scht
				_configAreaFile.declareObjectAsAGap(_lastFilePosition);
				_configAreaFile.removeNewObject(this);
			}
			else {
				_debug.fine("Objekt wird invalid: Id " + super.getID() + " InvalidVersion: " + (_configAreaFile.getNextActiveVersion()));
				_firstInvalidVersion = _configAreaFile.getNextActiveVersion();
				if(_saveModifications) {
					_configAreaFile.objectModified(this);
				}
			}
		}
	}

	/**
	 * Solange der Konfigurationsbereich noch nicht in eine neue Version �berf�hrt wurde, kann ein Konfigurationsobjekt, welches auf ung�ltig gesetzt wurde, mit
	 * dieser Methode wieder auf g�ltig gesetzt werden.
	 *
	 * @see #invalidate
	 */
	synchronized public void revalidate() {
		// Diese Methode darf nur dann aufgerufen werden, wenn sich das Objekt nicht in einem nGa-Bereich befindet. Dies kann aber hier nicht gepr�ft werden
		// und muss von der Klasse erledigt werden, die diese Methode aufruft.
		// if (_firstInvalidVersion > 0 && _configAreaFile.getActiveVersion() >= _firstInvalidVersion) throw new IllegalStateException("Objekt kann nicht wieder auf g�ltig gesetzt werden, da der Konfigurationsbereich bereits in einer neuen Version l�uft.");
		_firstInvalidVersion = 0;
		if(_saveModifications) {
			_configAreaFile.objectModified(this);
		}
	}

	public long[] getObjectSetIds() {
		synchronized(_sets) {
			// Von Hand ein long[] erzeugen, da Long[] nicht auf long[] gecastet werden kann, wenn man
			// die .toArray Methode benutzt
			Set keySet = _sets.keySet();
			long keys[] = new long[_sets.size()];
			int nr = 0;
			for(Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				Long keyLong = (Long)iterator.next();
				keys[nr] = keyLong.longValue();
				nr++;
			}
			return keys;
		}
	}

	synchronized public void addObjectSetId(long setId) throws IllegalStateException {
		// Die Methode wird zum einlesen der Sets aus der Datei ben�tigt. Aus diesem Grund muss nicht gepr�ft werden, ob das Objekt noch g�ltig ist.
		synchronized(_sets) {
			if(!_sets.containsKey(setId)) {
				// Menge erzeugen, die Objekte der Menge aufnehmen kann
				final Set<Long> setObjects = new HashSet<Long>();
				_sets.put(setId, setObjects);
			}
			else {
				// Die Menge wurde bereits angelegt
				throw new IllegalStateException(
						"Die Menge mit der Id " + setId + " kann nicht am Konfigurationsobjekt mit der Id " + getID()
						+ " hinzugef�gt werden, da diese Menge bereits am Objekt vorhanden ist."
				);
			}
		}
		if(_saveModifications) {
			_configAreaFile.objectModified(this);
		}
	}

	public long[] getObjectSetObjects(long setId) {
		final Set setObjects;
		synchronized(_sets) {
			if(_sets.containsKey(setId)) {
				setObjects = _sets.get(setId);
			}
			else {
				throw new IllegalArgumentException("Die Menge mit der Id " + setId + " ist am Konfigurationsobjekt mit der Id " + getID() + " nicht vorhanden");
			}
		}

		synchronized(setObjects) {
			final Long objectsLong[] = (Long[])setObjects.toArray(new Long[setObjects.size()]);
			// long Array, dies ist die R�ckgabe
			final long objectslong[] = new long[objectsLong.length];
			for(int nr = 0; nr < objectsLong.length; nr++) {
				objectslong[nr] = objectsLong[nr].longValue();
			}
			return objectslong;
		}
	}

	synchronized public void addObjectSetObject(long setId, long objectId) throws IllegalArgumentException, IllegalStateException {
		// Die Methode wird zum einlesen der Sets aus der Datei ben�tigt. Aus diesem Grund muss nicht gepr�ft werden, ob das Objekt noch g�ltig ist.
		final Set setObjects;
		synchronized(_sets) {
			if(_sets.containsKey(setId)) {
				setObjects = _sets.get(setId);
			}
			else {
				throw new IllegalArgumentException(
						"Die Menge mit der Id " + setId + " ist am Konfigurationsobjekt mit der Id " + getID() + " nicht vorhanden"
				);
			}
		}

		// Das Objekt hinzuf�gen
		synchronized(setObjects) {
			if(!setObjects.contains(objectId)) {
				// Das Objekt ist noch nicht in der Menge
				setObjects.add(objectId);
			}
			else {
				throw new IllegalStateException(
						"Die Menge mit der Id " + setId + " enth�lt das Objekt mit der Id " + objectId + " bereits, Id des betroffenen Konfigurationsobjekts"
						+ getID()
				);
			}
		}
		if(_saveModifications) {
			_configAreaFile.objectModified(this);
		}
	}

	synchronized public long getLastFilePosition() {
		return _lastFilePosition;
	}

	synchronized public void setLastFilePosition(long lastFilePosition) {
		_lastFilePosition = lastFilePosition;
	}

	public String toString() {
		final StringBuffer out = new StringBuffer();
		out.append("Konfigurationsobjekt: " + "\n");
		out.append(super.toString());
		out.append("nicht mehr g�ltig ab Version: " + getFirstInvalidVersion() + "\n");
		out.append("g�ltig ab Version: " + getFirstValidVersion() + "\n");

		// Alle Mengen und deren Objekte einlesen
		final long[] objectSetIds = getObjectSetIds();
		for(int nr = 0; nr < objectSetIds.length; nr++) {
			out.append("	Objekte der Menge mit Id: " + objectSetIds[nr] + "\n");
			final long[] setObjects = getObjectSetObjects(objectSetIds[nr]);
			for(int i = 0; i < setObjects.length; i++) {
				out.append("		Objekte Id: " + setObjects[i] + "\n");
			}
		}
		out.append("Dateiposition: " + _lastFilePosition + "\n");
		out.append("Modifikationen speichern: " + _saveModifications + "\n");
		return out.toString();
	}
}
