/*
 * Copyright 2010 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.preselection.util;

import de.bsvrz.dav.daf.main.config.SystemObject;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Hilfsklasse zur Sortierung von SystemObject-Collections nach den Regeln der deutschen Sprache.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SortUtil {

	/**
	 * Sortiert eine Collection lexikographisch unter Berücksichtigung von deutschen Umlauten.
	 *
	 * @param collection zu sortierende SystemObjekte
	 *
	 * @return eine neue lexikographisch sortierte Liste
	 */
	public static List<SystemObject> sortCollection(final Collection collection) {
		final Collator deCollator = Collator.getInstance(Locale.GERMANY);
		ArrayList<CollationKeySystemObject> collationKeySystemObjects = new ArrayList<CollationKeySystemObject>(collection.size());
		for(Object object : collection) {
			final SystemObject systemObject = (SystemObject)object;
			collationKeySystemObjects.add(new CollationKeySystemObject(systemObject, deCollator.getCollationKey(systemObject.getNameOrPidOrId())));
		}
		Collections.sort(
				collationKeySystemObjects, new Comparator<CollationKeySystemObject>() {
					public int compare(CollationKeySystemObject o1, CollationKeySystemObject o2) {
						return o1.getCollationKey().compareTo(o2.getCollationKey());
					}
				}
		);
		final ArrayList<SystemObject> result = new ArrayList<SystemObject>(collection.size());
		for(CollationKeySystemObject collationKeySystemObject : collationKeySystemObjects) {
			result.add(collationKeySystemObject.getSystemObject());
		}
		return result;
	}

	/** Speichert zur effizienten Sortierung ein CollationKey und ein SystemObject */
	private static final class CollationKeySystemObject {

		private final SystemObject _systemObject;

		private final CollationKey _collationKey;

		private CollationKeySystemObject(final SystemObject systemObject, final CollationKey collationKey) {
			_systemObject = systemObject;
			_collationKey = collationKey;
		}

		public final CollationKey getCollationKey() {
			return _collationKey;
		}

		public final SystemObject getSystemObject() {
			return _systemObject;
		}
	}
}
