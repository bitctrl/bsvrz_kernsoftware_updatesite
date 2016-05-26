/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.configObjectAcquisition.
 * 
 * de.bsvrz.sys.funclib.configObjectAcquisition is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.configObjectAcquisition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.configObjectAcquisition; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.configObjectAcquisition;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

/**
 * Hilfsfunktion(en) für den Zugriff auf die Konfiguration
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigurationHelper {

	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/** Erzeugt ein neues Objekt der Klasse <code>ConfigurationHelper</code> */
	public ConfigurationHelper() {
	}

	/**
	 * Holt die durch den <code>objectSpecString</code> spezifizierten Objekte.
	 *
	 * @param objectSpecString String der die Objekte beschreibt.
	 * @param dataModel        Objekt zum Zugriff auf die Konfiguration.
	 *
	 * @return Spezifizierte Objekte.
	 *
	 * @throws IllegalArgumentException Wenn die Objektspezifikation nicht interpretiert werden kann.
	 */
	public static List<SystemObject> getObjects(String objectSpecString, DataModel dataModel) throws IllegalArgumentException {
		List<SystemObject> allObjects = new LinkedList<SystemObject>();
		String[] objectSpecs = objectSpecString.split(",");
		for(int specIndex = 0; specIndex < objectSpecs.length; ++specIndex) {
			List objects = new LinkedList();

			String[] objectSpecParts = objectSpecs[specIndex].split(":");
			try {
				long objectId = 0;
				objectId = Long.parseLong(objectSpecParts[0]);
				objects.add(dataModel.getObject(objectId));
			}
			catch(Exception e) {
				SystemObject theNewObject = dataModel.getObject(objectSpecParts[0]);
				if(theNewObject != null) {
					objects.add(theNewObject);
				}
				else {
					debug.error("Objekt \"" + objectSpecParts[0] + "\" existiert nicht.");
					throw new IllegalArgumentException(
							"Objekt \"" + objectSpecParts[0] + "\" existiert nicht"
					);
				}
			}
			for(int partIndex = 1; partIndex < objectSpecParts.length; ++partIndex) {
				String objectSetName = objectSpecParts[partIndex];
				List newObjects = new LinkedList();
				Iterator objectsIterator = objects.iterator();
				while(objectsIterator.hasNext()) {
					Object object = objectsIterator.next();
					if(object instanceof ConfigurationObject) {
						if(objectSetName.equals("*")) {
							if(object instanceof SystemObjectType) {
								SystemObjectType typeObject = (SystemObjectType)object;
								List elements = typeObject.getElements();
								newObjects.addAll(elements);
							}
						}
						else {
							ObjectSet set = ((ConfigurationObject)object).getObjectSet(objectSetName);
							if(set != null) newObjects.addAll(set.getElements());
						}
					}
				}
				objects = newObjects;
			}
			allObjects.addAll(objects);
		}
		return allObjects;
	}
}
