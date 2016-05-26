/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Applikationsseitige Implementierung der Schnittstelle zum Zugriff auf die Eigenschaften eines Konfigurationsobjekts.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafConfigurationObject extends DafSystemObject implements ConfigurationObject {

	/** Version ab der dieses Objekt gültig ist. */
	private short _validSince;

	/** Version ab der dieses Objekt nicht mehr gültig ist. */
	private short _notValidSince;

	/** Die IDs der Mengen dieses Objekts */
	private long _setIds[];

	/** Liste der Mengen dieses Objekts */
	private List<ObjectSet> _sets;

	/** DebugLogger für Debug-Ausgaben */
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
		str += "Gültig ab Version: " + _validSince + "\n";
		str += "Gültig bis Version: " + _notValidSince + "\n";
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

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_validSince = deserializer.readShort();
		_notValidSince = deserializer.readShort();
		_configurationAreaId = deserializer.readLong();  // muss hier gelesen werden, da nach Gültigkeit serialisiert
		int size = deserializer.readInt();
		if(size > 0) {
			_setIds = new long[size];
			for(int i = 0; i < size; ++i) {
				_setIds[i] = deserializer.readLong();
			}
		}
	}


	public final boolean isValid() {
		short activeVersion = getConfigurationArea().getActiveVersion();
		if(activeVersion < getValidSince()) {
			return false;	// wurde noch nicht gültig
		}
		if(getNotValidSince() == 0) {
			return true;	// ist gültig und wurde noch nicht auf ungültig gesetzt
		}
		if(getNotValidSince() <= activeVersion) {
			return false; // wurde bereits auf ungültig gesetzt
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
		_dataModel.revalidate(this);
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
					"Von einem Objekt konnte kein Duplikat erstellt werden, da das nötige Telegramm nicht an den Datenverteiler verschickt werden konnte oder die Rückantwort konnte nicht entschlüsselt werden. Betroffenes Objekt: "
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
