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

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class DafObjectSet extends DafConfigurationObject implements ObjectSet {

	/** Die Ids der Elemente dieser Menge */
	protected ArrayList<Long> _setElementIds;

	/** Die Elemente dieser Menge */
	protected List<SystemObject> _setElements;

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	protected DafObjectSet(DafDataModel dataModel) {
		super(dataModel);
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	protected DafObjectSet(
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
			long setIds[],
			ArrayList setElementIds
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_setElementIds = setElementIds;
	}

	public String parseToString() {
		String str = super.parseToString();
		if(_setElementIds == null) {
			str += "Leere Menge";
		}
		else {
			int size = _setElementIds.size();
			if(size == 0) {
				str += "Leere Menge";
			}
			else {
				str += "[ ";
				for(int i = 0; i < size; ++i) {
					str += ((Long)_setElementIds.get(i)).longValue() + " ";
				}
				str += "] ";
			}
		}
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		if(_setElementIds == null) {
			out.writeInt(0);
		}
		else {
			int size = _setElementIds.size();
			if(size == 0) {
				out.writeInt(0);
			}
			else {
				out.writeInt(size);
				for(int i = 0; i < size; ++i) {
					out.writeLong(((Long)_setElementIds.get(i)).longValue());
				}
			}
		}
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		_setElementIds = new ArrayList<Long>();
		int size = in.readInt();
		if(size > 0) {
			for(int i = 0; i < size; ++i) {
				_setElementIds.add(new Long(in.readLong()));
			}
		}
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_setElementIds = new ArrayList<Long>();
		int size = deserializer.readInt();
		for(int i = 0; i < size; ++i) {
			_setElementIds.add(deserializer.readLong());
		}
	}

	public final ObjectSetType getObjectSetType() {
		return (DafObjectSetType)getType();
	}

	public abstract List<SystemObject> getElements();

	public List getElements(long time) {
		return Collections.unmodifiableList(_dataModel.getSetElements(this, time));
	}

	public List getElementsInPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(_dataModel.getSetElementsInPeriod(this, startTime, endTime));
	}

	public List getElementsDuringPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(_dataModel.getSetElementsDuringPeriod(this, startTime, endTime));
	}

	public void add(SystemObject object) throws ConfigurationChangeException {
		add(new SystemObject[]{object});
	}

	public void add(SystemObject[] objects) throws ConfigurationChangeException {
		for(SystemObject object : objects) {
			if(object == null) throw new IllegalArgumentException("Ein zur Menge " + getNameOrPidOrId() + " hinzuzufügendes Objekt ist null");
		}
		try {
			final ConfigurationRequester requester = _dataModel.getRequester();
			requester.changeElements(this, objects, null);
			Thread.yield();
			Thread.yield();
		}
		catch(RequestException e) {
			_debug.error("Hinzufügen von Objekten zu einer Menge fehlgeschlagen", e);
			_dataModel.getConnection().disconnect(true, e.getMessage());
		}
	}

	public void remove(SystemObject object) throws ConfigurationChangeException {
		remove(new SystemObject[]{object});
	}

	public void remove(SystemObject[] objects) throws ConfigurationChangeException {
		for(SystemObject object : objects) {
			if(object == null) throw new IllegalArgumentException("Ein aus der Menge " + getNameOrPidOrId() + " zu entfernendes Objekt ist null");
		}
		try {
			final ConfigurationRequester requester = _dataModel.getRequester();
			requester.changeElements(this, null, objects);
			Thread.yield();
			Thread.yield();
		}
		catch(RequestException e) {
			_debug.error("Löschen von Objekten aus einer Menge fehlgeschlagen", e);
			_dataModel.getConnection().disconnect(true, e.getMessage());
		}
	}
}
