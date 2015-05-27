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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 13136 $ / $Date: 2015-01-29 16:38:49 +0100 (Thu, 29 Jan 2015) $ / ($Author: jh $)
 */
public abstract class SystemObjectInformation implements SystemObjectInformationInterface {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final long _id;

	private final String _pid;

	private final long _typeId;

	private String _name = "";

	/** Sobald sich an dem Objekt etwas �ndert (Konfigurierender Datensatz hinzuf�gen oder l�schen) wird dieses Objekt benachrichtigt. */
	private final ConfigAreaFile _modifiedManger;

	/** Speichert die konfigurierenden Datens�tze des Objekts, als Schl�ssel dient die ID der ATGU (Attributgruppenverwendung) */
	private final Map<Long, byte[]> _dataSets = new HashMap<Long, byte[]>();

	/** Speicher ob Modifikationen gespeichert werden sollen. Beim laden darf das Objekt nicht automatisch gespeichert werden. */
	protected boolean _saveModifications;

	/** Eine Referenz auf ein beliebiges Objekt */
	private Object _reference = null;

	/**
	 * @param id                Id des Objekts
	 * @param pid               Pid des Objekts, diese muss ISO-8859-1 konform sein
	 * @param typeId            Tye des Objekts, der Type wird �ber die Id identifiziert
	 * @param name              Name des Objekts, <code>null</code> wird als "" interpretiert
	 * @param configAreaFile    Objekt, das die Datei verwaltet, in dem dieses Objekt gespeichert ist/wird
	 * @param saveModifications true = Das Objekt wird angelegt und in die Datei des Konfigurationsbereichs sp�ter gespeichert, werden �nderungen vorgenommen
	 *                          (Datens�tze ge�ndert, usw), so werden die �nderungen gespeichert; false = Das Objekt wird angelegt und nicht gespeichert (beim
	 *                          laden des Objekts aus der Datei w�re dies sinnvoll), auch Modifikationen am Objekt werden nicht gespeichert, damit gespeichert wird
	 *                          muss {@link #saveObjectModifications} aufgerufen werden
	 *
	 * @throws IllegalArgumentException Die Pid ist nicht ISO-8859-1 konform
	 */
	public SystemObjectInformation(long id, String pid, long typeId, String name, ConfigAreaFile configAreaFile, boolean saveModifications)
			throws IllegalArgumentException {
		_id = id;

		if(pid == null) pid = "";
		// Ist die Pid ISO-8859-1 konform. Daf�r wird jeder Char einzeln gepr�ft
		if(pid.length() > 0) {
			// Speichert jeden Char der Pid
			char pidChars[] = new char[pid.length()];
			pid.getChars(0, pid.length() - 1, pidChars, 0);

			for(char onePidChar : pidChars) {
				if(onePidChar > 255) {
					// Der Char kann nicht mit einem Byte dargestellt werden. Also ist der Char nicht ISO-8859-1 konform.
					throw new IllegalArgumentException(
							"Die Pid " + pid + " ist nicht ISO-8859-1 konform, fehlerhaftes Zeichen: " + onePidChar + " .Betroffenes Objekt: Id " + id
							+ " Name " + name + " Konfigurationsbereich Pid " + configAreaFile.getConfigAreaPid()
					);
				}
			}
		}
		_pid = pid;
		_typeId = typeId;

		_saveModifications = saveModifications;

		if(name != null) {
			_name = name;
		}
		_modifiedManger = configAreaFile;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn ein Objekt aus der Datei eingeladen wurde und im Konstruktor saveModifications == false �bergeben wurde. Nach Aufruf
	 * dieser Methode, werden alle �nderungen wieder gespeichert. Wurde also saveModifications == false gesetzt, so muss diese Methode aufgerufen werden, damit
	 * neue �nderungen gespeichert werden.
	 */
	public void saveObjectModifications() {
		_saveModifications = true;
	}

	public long getID() {
		return _id;
	}

	public String getPid() {
		return _pid;
	}

	int getPidHashCode() {
		return _pid.hashCode();
	}

	public long getTypeId() {
		return _typeId;
	}

	synchronized public String getName() {
		// Der Name kann auch gesetzt werden, aus diesem Grund synch um parallen Zugriff zu verhindern
		return _name;
	}

	synchronized public void setName(String newName) {
		// Der Name kann auch gelesen werden, aus diesem Grund synch um parallen Zugriff zu verhindern
		_name = newName;
		if(_saveModifications) {
			_modifiedManger.objectModified(this);
		}
	}

	public long[] getConfigurationsDataAttributeGroupUsageIds() {
		synchronized(_dataSets) {
			Collection<Long> keys = _dataSets.keySet();

			// return (long) keys.toArray(new Long[keys.size()]); funktioniert nicht !

			final long longKeys[] = new long[keys.size()];
			int nr = 0;
			for(Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
				Long aLong = iterator.next();
				longKeys[nr] = aLong.longValue();
				nr++;
			}
			return longKeys;
		}
	}

	public byte[] getConfigurationData(long attributeGroupUsageId) {
		synchronized(_dataSets) {
			byte[] bytes = _dataSets.get(attributeGroupUsageId);
			if(bytes != null) {
				// Es gibt einen Datensatz
				return bytes;
			}
			else {
				// Es gibt keinen Datensatz (das kann durchaus passieren, der Fall wird mit der Exception erkannt)
				_debug.finest(
						"Der Datensatz, der mit " + attributeGroupUsageId + " angefordert werden sollte, kann am  Objekt " + _id
						+ " nicht gefunden werden. Pid: " + getPid()
				);
				throw new IllegalArgumentException(
						"Der Datensatz, der mit " + attributeGroupUsageId + " angefordert werden sollte, kann am  Objekt " + _id
						+ " nicht gefunden werden. Pid: " + getPid()
				);
			}
		}
	}

	@Override
	public byte[] getConfigurationDataOptional(long attributeGroupUsageId) {
		synchronized(_dataSets) {
			return  _dataSets.get(attributeGroupUsageId);
		}
	}

	public void setConfigurationData(long attributeGroupUsageId, byte[] data) throws IllegalStateException {
		if(_saveModifications && isDeleted()){
			throw new IllegalStateException("Die konfigurierenden Datens�tze eines gel�schten Objekts k�nnen nicht ge�ndert werden");
		}
		synchronized(_dataSets) {
			if(data != null && data.length > 0) {
				_dataSets.put(attributeGroupUsageId, data);
			}
			else {
				if(_dataSets.remove(attributeGroupUsageId) == null) return;
			}
		}
		if(_saveModifications) {
			_modifiedManger.objectModified(this);
		}
	}

	public void removeConfigurationData(long attributeGroupUsageId) {
		synchronized(_dataSets) {
			final byte[] removedDataSet = _dataSets.remove(attributeGroupUsageId);
			if(_saveModifications && removedDataSet != null) {
				// �nderungen sollen gespeichert werden und es wurde ein Datensatz entfernt
				_modifiedManger.objectModified(this);
			}
		}
	}

	/**
	 * Gibt ein Objekt zur�ck, das mit {@link #setReference} gesetzt wurde. Wurde noch kein Objekt gesetzt, wird <code>null</code> zur�ckgegeben
	 *
	 * @return Objektreferenz oder <code>null</code>
	 */
	public Object getReference() {
		return _reference;
	}

	/**
	 * Setzt ein Objekt, das mit {@link #getReference} angefordert werden kann
	 *
	 * @param newReference s.o.
	 */
	public void setReference(Object newReference) {
		_reference = newReference;
	}

	/**
	 * Diese Methode gibt das Objekt zur�ck, das einen Konfigurationsbereich physisch auf einem Datentr�ger speichert.
	 *
	 * @return s.o.
	 */
	public ConfigurationAreaFile getConfigurationAreaFile() {
		return _modifiedManger;
	}

	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}

		if(o == this) {
			return true;
		}

		if(!(o instanceof SystemObjectInformation)) {
			return false;
		}


		final SystemObjectInformation compareObject = (SystemObjectInformation)o;

		if(compareObject.getID() == getID()) {
			return true;
		}
		else {
			return false;
		}

//		// Alle Elemente vergleichen
//		if(((compareObject.getID() == getID()) && (compareObject.getPid().equals(getPid())) && (compareObject.getTypeId() == getTypeId())
//		    && (compareObject.getName().equals(getName()))) == false) {
//			// Es gab mindestens einen Unterschied
//			return false;
//		}
//
//		// Es wurden alle Felder verglichen, es trat kein Fehler auf, also sind die Objekte logisch identisch
//		return true;
	}

	public int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}

	abstract public FilePointer getLastFilePosition();

	abstract public void setLastFilePosition(FilePointer lastFilePosition);

	public String toString() {
		final StringBuffer out = new StringBuffer();
		out.append("Id: " + getID() + "\n");
		out.append("Pid: " + getPid() + "\n");
		out.append("Type, Id: " + getTypeId() + "\n");
		out.append("Name: " + getName() + "\n");

		synchronized(_dataSets) {
			out.append("Konfigurierende Datens�tze, Anzahl: " + _dataSets.size() + "\n");
			Collection<Long> keys = _dataSets.keySet();

			for(Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
				Long key = iterator.next();
				out.append("	Datensatz, Id: " + key + " bytes: Anzahl " + _dataSets.get(key).length + "\n");
				// out.append(HexDumper.toString(_dataSets.get(key)) + "\n");
			}
		}
		out.append("Modifikationen speichern: " + _saveModifications + "\n");
		return out.toString();
	}
}
