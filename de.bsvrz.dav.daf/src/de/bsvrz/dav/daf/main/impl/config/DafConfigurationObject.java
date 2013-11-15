/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Applikationsseitige Implementierung der Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsobjekts.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 7092 $
 */
public class DafConfigurationObject extends DafSystemObject implements ConfigurationObject {

	/** Version ab der dieses Objekt g�ltig ist. */
	private short _validSince;

	/** Version ab der dieses Objekt nicht mehr g�ltig ist. */
	private short _notValidSince;

	/** Die IDs der Mengen dieses Objekts */
	private long _setIds[];

	/** Liste der Mengen dieses Objekts */
	private List<ObjectSet> _sets;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafConfigurationObject(DafDataModel dataModel) {
		super(dataModel);
		_internType = CONFIGURATION_OBJECT;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafConfigurationObject(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long setIds[]) {
		super(
				id, pid, name, typId, state, error, dataModel, responsibleObjectId
		);
		_internType = CONFIGURATION_OBJECT;
		_validSince = validFromVersionNumber;
		_notValidSince = validToVersionNumber;
		_setIds = setIds;
	}

	public String parseToString() {
		String str = super.parseToString();
		str += "G�ltig ab Version: " + _validSince + "\n";
		str += "G�ltig bis Version: " + _notValidSince + "\n";
		str += "Mengen: \n";
		if(_sets == null) {
			getObjectSets();
		}
		if(_sets != null) {
			for(int i = 0; i < _sets.size(); ++i) {
				str += ((DafObjectSet)_sets.get(i)).parseToString();
			}
		}
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeShort(_validSince);
		out.writeShort(_notValidSince);
		if(_setIds == null) {
			out.writeInt(0);
		}
		else {
			out.writeInt(_setIds.length);
			for(int i = 0; i < _setIds.length; ++i) {
				out.writeLong(_setIds[i]);
			}
		}
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		_validSince = in.readShort();
		_notValidSince = in.readShort();
		int size = in.readInt();
		if(size > 0) {
			_setIds = new long[size];
			for(int i = 0; i < size; ++i) {
				_setIds[i] = in.readLong();
			}
		}
	}

	public final boolean isValid() {
		short activeVersion = getConfigurationArea().getActiveVersion();
		if(activeVersion < getValidSince()) {
			return false;	// wurde noch nicht g�ltig
		}
		if(getNotValidSince() == 0) {
			return true;	// ist g�ltig und wurde noch nicht auf ung�ltig gesetzt
		}
		if(getNotValidSince() <= activeVersion) {
			return false; // wurde bereits auf ung�ltig gesetzt
		}
		return true;
	}

	public final short getValidSince() {
		return _validSince;
	}

	public final short getNotValidSince() {
		return _notValidSince;
	}

	public final void revalidate() throws ConfigurationChangeException {
		if(!_dataModel.revalidate(this)) {
			throw new ConfigurationChangeException("Das Objekt konnte nicht wieder g�ltig gemacht werden.");
		}
	}

	public SystemObject duplicate() throws ConfigurationChangeException {
		return duplicate(new HashMap<String, String>());
	}

	public SystemObject duplicate(final Map<String, String> substitutePids) throws ConfigurationChangeException {
		try {
			return _dataModel.getRequester().duplicate(this, substitutePids);
		}
		catch(RequestException e) {
			final String errorText =
					"Von einem Objekt konnte kein Duplikat erstellt werden, da das n�tige Telegramm nicht an den Datenverteiler verschickt werden konnte oder die R�ckantwort konnte nicht entschl�sselt werden. Betroffenes Objekt: "
					+ getPid();
			_debug.error(errorText, e);
			throw new IllegalStateException(e);
		}
	}


	public final MutableSet getMutableSet(String name) {
		ObjectSet set = getObjectSet(name);
		if(set != null && set.getObjectSetType().isMutable()) {
			return (MutableSet)set;
		}
		return null;
	}

	public final NonMutableSet getNonMutableSet(String name) {
		ObjectSet set = getObjectSet(name);
		if(set != null && !set.getObjectSetType().isMutable()) {
			return (NonMutableSet)set;
		}
		return null;
	}

	public final ObjectSet getObjectSet(String name) {
		final List<ObjectSet> sets = getObjectSets();
		for(ObjectSet set : sets) {
			if(name.equals(set.getName())) {
				return set;
			}
		}
		return null;
	}

	public final List<ObjectSet> getObjectSets() {
		if(_sets == null) {
			final ArrayList<ObjectSet> sets = new ArrayList<ObjectSet>();
			if(_setIds != null) {
				for(int i = 0; i < _setIds.length; ++i) {
					DafObjectSet set = (DafObjectSet)_dataModel.getObject(_setIds[i]);
					if(set != null) {
						sets.add(set);
					}
				}
			}
			_sets = Collections.unmodifiableList(sets);
		}
		return _sets;
	}

	public final void addSet(ObjectSet set) throws ConfigurationChangeException {
		_dataModel.addSet(this, set);
	}

	public final void removeSet(ObjectSet set) throws ConfigurationChangeException {
		_dataModel.removeSet(this, set);
	}
}
