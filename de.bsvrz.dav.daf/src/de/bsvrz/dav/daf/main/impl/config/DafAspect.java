/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.Aspect;

/**
 * Klasse, die den Zugriff auf Aspekte seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class DafAspect extends DafConfigurationObject implements Aspect {

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafAspect(DafDataModel dataModel) {
		super(dataModel);
		_internType = ASPECT;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafAspect(
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
		_internType = ASPECT;
	}

	public final String parseToString() {
		String str = "Aspekt: \n";
		str += super.parseToString();
		return str;
	}
}
