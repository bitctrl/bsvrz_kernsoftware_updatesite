/*
 * Copyright 2010 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.consistencycheck;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.FixableConsistencyCheckResultEntry;
import de.bsvrz.puk.config.configFile.datamodel.ConfigNonMutableSet;

import java.util.Arrays;

/**
 * Enth�lt Klassen, die behebbare Probleme bei der Konsistenzpr�fung darstellen
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8953 $
 */
class FixableProblems {

	private FixableProblems(){
	}
}

/**
 * Repr�sentiert einen Fehler, bei dem ein Objekt in einer Menge null ist. Das passiert, wenn man ein Objekt in einer Menge importiert, aber vor dem aktivieren
 * das Objekt l�scht.
 */
class ObjectSetEntryIsNull extends FixableConsistencyCheckResultEntry {

	private final ObjectSet _objectSet;

	public ObjectSetEntryIsNull(final ConfigurationArea verifyingConfigArea, final SystemObject systemObject, final ObjectSet objectSet) {
		super(
				verifyingConfigArea,
				Arrays.asList(systemObject, objectSet),
				"Element der Menge " + objectSet.getName() + " unterhalb des Objekts " + systemObject + " ist null."
		);
		_objectSet = objectSet;
	}

	@Override
	public void fix() throws ConfigurationChangeException {
		((ConfigNonMutableSet)_objectSet).removeNullElements();
		update(null, false);
	}
}
