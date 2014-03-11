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

import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.sys.funclib.debug.Debug;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Diese Klasse stellt alle Informationen zur Verfügung, die für ein dynamisches Objekt in der Konfiguration benötigt werden. <br>
 * <p/>
 * Falls das Objekt in eine Datei geschrieben werden soll, sollte das Objekt zum synchronisieren benutzt werden. Dies verhindert, dass sich das Objekt während
 * des Schreibvorgangs verändert, lesende Zugriffe sind weiterhin möglich.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 6914 $ / $Date: 2009-11-05 15:41:41 +0100 (Do, 05 Nov 2009) $ / ($Author: rs $)
 */
public class DynamicObjectInformation extends SystemObjectInformation implements DynamicObjectInfo {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final long _firstValidTime;

	private long _firstInvalidTime = 0;


	private short _simulationVariant = -1;

	/** Speichert die letzte abselute Position ab, an der das Objekt gespeichert wurde */
	private long _lastFilePosition = -1;

	/** Dieses Objekt übernimmt die persistente Speicherung des Objekts und kann es gleichzeitig aus der Datei wieder entfernen. */
	private final ConfigAreaFile _modifiedManager;

	/** Speicher ob Modifikationen gespeichert werden sollen. Beim laden darf das Objekt nicht automatisch gespeichert werden. */
	private boolean _saveModifications;

	private final DynamicObjectType.PersistenceMode _persistenceMode;

	/**
	 * Für automatisierte Tests wird bei <code>true</code> innerhalb der setInvalid-Methode ein sleep von 10ms ausgeführt, das einen potentiellen Deadlock aufdecken würde.
	 */
	static private boolean _testWithSynchronizedSleep = false;

	/**
	 * @param testWithSynchronizedSleep  Wenn <code>true</code> übergeben wird, dann wird in nachfolgenden Aufrufen der Methode setInvalid() ein sleep von 10ms
	 * ausgeführt, das einen potentiellen Deadlock aufdecken würde.
	 */
	public static void setTestWithSynchronizedSleep(final boolean testWithSynchronizedSleep) {
		_testWithSynchronizedSleep = testWithSynchronizedSleep;
	}

	/**
	 * @param id                Id des Objekts
	 * @param pid               Pid des Objekts
	 * @param typeId            Tye des Objekts, der Type wird über die Id identifiziert
	 * @param name              Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param simulationVariant Simulationsvariante des dynamischen Objekts
	 * @param firstValidTime    Zeitpunkt, an dem das dynamische Objekt gültig werden soll
	 * @param saveModifications true = Das Objekt wird angelegt und in die Datei des Konfigurationsbereichs später gespeichert, werden Änderungen vorgenommen
	 *                          (Datensätze geändert, usw), so werden die Änderungen gespeichert; false = Das Objekt wird angelegt und nicht gespeichert (beim
	 *                          laden des Objekts aus der Datei wäre dies sinnvoll), auch Modifikationen am Objekt werden nicht gespeichert, damit gespeichert wird
	 *                          muss {@link #saveObjectModifications} aufgerufen werden
	 * @param persistenceMode   Persistenzmodus des dynamischen Objekts.
	 */
	public DynamicObjectInformation(
			long id,
			String pid,
			long typeId,
			String name,
			short simulationVariant,
			long firstValidTime,
			ConfigAreaFile configAreaFile,
			boolean saveModifications,
			DynamicObjectType.PersistenceMode persistenceMode
	) {
		super(id, pid, typeId, name, configAreaFile, saveModifications);
		_simulationVariant = simulationVariant;
		_firstValidTime = firstValidTime;
		_modifiedManager = configAreaFile;
		_saveModifications = saveModifications;
		_persistenceMode = persistenceMode;

		// Das Objekt soll gespeichert werden
		if(_saveModifications) {
			_modifiedManager.objectModified(this);
		}
	}

	/**
	 * Mit diesem Konstruktor können nur "nicht transiente" Objekte erzeugt werden.
	 *
	 * @param id
	 * @param pid
	 * @param typeId
	 * @param name
	 * @param simulationVariant
	 * @param firstValidTime
	 * @param firstInvalidTime
	 * @param configAreaFile
	 * @param saveModifications
	 */
	public DynamicObjectInformation(
			long id,
			String pid,
			long typeId,
			String name,
			short simulationVariant,
			long firstValidTime,
			long firstInvalidTime,
			ConfigAreaFile configAreaFile,
			boolean saveModifications
	) {
		super(id, pid, typeId, name, configAreaFile, saveModifications);
		_simulationVariant = simulationVariant;
		_firstValidTime = firstValidTime;
		_firstInvalidTime = firstInvalidTime;
		_modifiedManager = configAreaFile;
		_saveModifications = saveModifications;

		// Da der Wert nicht gespeichert wird, kann nicht zwischen "Persinstent" und "Persistent und ungültig nach Neustart" unterschieden werden.
		_persistenceMode = DynamicObjectType.PersistenceMode.PERSISTENT_OBJECTS;

		// Das Objekt soll gespeichert werden
		if(_saveModifications) {
			_modifiedManager.objectModified(this);
		}
	}

	public void saveObjectModifications() {
		_saveModifications = true;
		super.saveObjectModificationsSystemObject();
	}

	public long getFirstValidTime() {
		return _firstValidTime;
	}

	synchronized public long getFirstInvalidTime() {
		// Der Zugriff ist synchronisiert, da zur selben Zeit der Wert gesetzt werden kann und ein
		// Long nicht "atomar" gesetzt werden kann
		return _firstInvalidTime;
	}

	/** Objekt auf Invalid setzen und sofort speichern */
	public void setInvalid() {
		boolean doWrite = false;
		boolean doUpdateCache = false;
		synchronized(this) {
			if(_testWithSynchronizedSleep) {
				try {
					Thread.sleep(10);
				}
				catch(InterruptedException e) {
					throw new UnsupportedOperationException("nicht implementiert");
				}
			}
			if(_firstInvalidTime == 0) {

				_debug.fine("Objekt wird invalid, Id", getID() + " Pid: " + getPid());
				// Der Zugriff ist synchronisiert, da zur selben Zeit der Wert angefordert werden kann und ein
				// Long nicht "atomar" gesetzt werden kann
				_firstInvalidTime = System.currentTimeMillis();

				if(_persistenceMode != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
					// Das Objekt wird jetzt gespeichert, es kann aus dem Puffer entfernt werden.
					_modifiedManager.dontSaveObject(this);
					// Außerhalb des synchronized-Blocks speichern, damit ein Zugriff auf das Objekt, auch wenn es aus der Datei geladen wird, immer zu den selben Informationen führt
					doWrite = true;
				}

				// Außerhalb des Synchronized Blocks dafür sorgen, dass das Objekt bei einer Anfrage auf "aktuelle" Objekte nicht mehr auftaucht
				doUpdateCache = true;
			}
			else {
				// Der Wert wurde schon gesetzt. Das kann vorkommen, wenn dies von 2 Clients gemacht wird.
				_debug.warning("Das dynamische Objekt mit der ID " + getID() + " sollte zum zweiten mal auf invalid gesetzt werden. Pid: " + getPid());
			}
		}
		// Speichern, damit ein Zugriff auf das Objekt, auch wenn es aus der Datei geladen wird, immer zu den selben Informationen führt
		if(doWrite) _modifiedManager.writeDynamicObject(this);
		// Das Objekt ist jetzt ungültig, also darf es bei einer Anfrage auf "aktuelle" Objekte nicht mehr auftauchen
		if(doUpdateCache) _modifiedManager.setDynamicObjectInvalid(this);
	}

	public short getSimulationVariant() {
		return _simulationVariant;
	}

	synchronized public void remove() {
		if(_simulationVariant > 0) {

			if(_persistenceMode != DynamicObjectType.PersistenceMode.TRANSIENT_OBJECTS) {
				// Falls sich das Objekt noch im Puffer befindet, wird es entfernt. Sonst wird es beim nächsten flush() aus dem Puffer erneut in die Datei
				// geschrieben.
				_modifiedManager.dontSaveObject(this);
				// Das Objekt als gelöscht markieren
				_modifiedManager.declareObjectAsAGap(_lastFilePosition);
			}
			_modifiedManager.removeDynamicSimulationObject(this);
		}
		else {
			throw new IllegalStateException(
					"Ein dynamisches Objekte mit Simulationsvariante 0 sollte gelöscht werden. pid: " + getPid() + " Konfigurationsbereich "
					+ _modifiedManager.getConfigAreaPid()
			);
		}
	}

	public DynamicObjectType.PersistenceMode getPersPersistenceMode() {
		return _persistenceMode;
	}

	synchronized public long getLastFilePosition() {
		return _lastFilePosition;
	}

	synchronized public void setLastFilePosition(long lastFilePosition) {
		_lastFilePosition = lastFilePosition;
	}

	public ConfigAreaFile getConfigAreaFile() {
		return _modifiedManager;
	}


	public String toString() {
		final StringBuffer out = new StringBuffer();
		out.append("Dynamisches Objekt: " + "\n");
		out.append(super.toString());
		final String format = "dd.MM.yyyy HH:mm:ss,SSS";
		final DateFormat timeFormat = new SimpleDateFormat(format);
		out.append("nicht mehr gültig ab dem Zeitpunkt: " + timeFormat.format(new Date(getFirstInvalidTime())) + "\n");
		out.append("gültig ab dem Zeitpunkt: " + timeFormat.format(new Date(getFirstValidTime())) + "\n");
		out.append("Simulationsvariante: " + getSimulationVariant() + "\n");
		out.append("Dateiposition: " + _lastFilePosition + "\n");
		return out.toString();
	}
}
