/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Klasse, die den Zugriff auf Konfigurationsmengen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafNonMutableSet extends DafObjectSet implements NonMutableSet {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafNonMutableSet(DafDataModel dataModel) {
		super(dataModel);
		_internType = NON_MUTABLE_SET;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafNonMutableSet(
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
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, setElementIds
		);
		_internType = NON_MUTABLE_SET;
	}

	public final String parseToString() {
		String str = "Statische Menge: \n";
		str += super.parseToString();
		return str;
	}

	public final List<SystemObject> getElements() {
		if((_setElementIds == null) || (_setElementIds.size() == 0)) {
			return new ArrayList();
		}
		if(_setElements == null) {
			final ArrayList<SystemObject> setElements = new ArrayList<SystemObject>();
			for(Object setElementId : _setElementIds) {
				final long objectId = ((Long)(setElementId)).longValue();
				final SystemObject systemObject = _dataModel.getObject(objectId);
				if(systemObject != null) {
					setElements.add(systemObject);
				}
				else {
					_debug.warning("Element der Menge " + getName() + " mit ID " + objectId + " nicht gefunden (wird ignoriert)");
				}
			}
			_setElements = Collections.unmodifiableList(setElements);
		}
		return _setElements;
	}

	public final List getElementsInModifiableVersion() {
		return Collections.unmodifiableList(_dataModel.getSetElementsInNextVersion(this));
	}

	public final List getElementsInVersion(short version) {
		return Collections.unmodifiableList(_dataModel.getSetElementsInVersion(this, version));
	}

	public final List getElementsInAllVersions(short fromVersion, short toVersion) {
		return Collections.unmodifiableList(_dataModel.getSetElementsInAllVersions(this, fromVersion, toVersion));
	}

	public final List getElementsInAnyVersions(short fromVersion, short toVersion) {
		return Collections.unmodifiableList(_dataModel.getSetElementsInAnyVersions(this, fromVersion, toVersion));
	}
}
