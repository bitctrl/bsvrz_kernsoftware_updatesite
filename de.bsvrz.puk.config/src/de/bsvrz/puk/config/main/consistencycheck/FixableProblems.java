/*
 * Copyright 2010 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
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
 * Enthält Klassen, die behebbare Probleme bei der Konsistenzprüfung darstellen
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
class FixableProblems {

	private FixableProblems(){
	}
}

/**
 * Repräsentiert einen Fehler, bei dem ein Objekt in einer Menge null ist. Das passiert, wenn man ein Objekt in einer Menge importiert, aber vor dem aktivieren
 * das Objekt löscht.
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
