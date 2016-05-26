/*
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

import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.ObjectSet;

import java.util.*;

/**
 * Klasse, die den Zugriff auf Datenverteilerobjekte seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafDavApplication extends DafConfigurationObject implements DavApplication {

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafDavApplication(DafDataModel dataModel) {
		super(dataModel);
		_internType = DAV_APPLICATION;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafDavApplication(
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
			long setIds[]
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = DAV_APPLICATION;
	}

	public String parseToString() {
		String str = "Datenverteiler: \n";
		str += super.parseToString();
		return str;
	}

	@Deprecated
	public final List getClientApplications() {
		ObjectSet applicationsSet = getObjectSet("Applikationen");
		List<SystemObject> applicationList;
		if(applicationsSet == null) {
			applicationList = new ArrayList();
		}
		else {
			applicationList = applicationsSet.getElements();
		}
		return Collections.unmodifiableList(applicationList);
	}

	public MutableSet getClientApplicationSet() {
		MutableSet applications = (MutableSet)getObjectSet("Applikationen");
		return applications;
	}
}
