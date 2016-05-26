/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;

import java.util.*;

/**
 * Definition, wann historische dynamische Objekte bereinigt werden können, basierend auf einem Vorhaltezeitraum
 * pro Typ der dynamischen Objekte. Für dynamische Mengentypen kann angegeben werden, wie lange historische Referenzen
 * auf zu löschende dynamsiche Objekte vorgehalten werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TimeBasedMaintenanceSpec implements MaintenanceSpec {

	private final Map<DynamicObjectType, Long> _objectKeepTimes;
	private final Map<ObjectSetType, Long> _setKeepTimes;
	private Long _defaultSetKeepTime;
	private final TypeHierarchy _typeHierarchy;

	public TimeBasedMaintenanceSpec(TypeHierarchy typeHierarchy, Map<DynamicObjectType, Long> objectKeepTimes, Map<ObjectSetType, Long> setKeepTimes, final Long defaultSetKeepTime) {
		_typeHierarchy = typeHierarchy;
		_defaultSetKeepTime = defaultSetKeepTime;
		for(ObjectSetType objectSetType : setKeepTimes.keySet()) {
			if(!objectSetType.isMutable()){
				throw new IllegalArgumentException(objectSetType + " ist nicht änderbar");
			}
		}
		_objectKeepTimes = objectKeepTimes;
		_setKeepTimes = setKeepTimes;
	}

	@Override
	public boolean canDeleteObject(final DynamicObjectInfo object) {
		
		// In dieser Methode dürfen keinerlei synchroniserte Aufrufe erfolgen, insbesondere DateModel.getObject()
		// ist synchronisert und bewirkt daher schnell Deadlocks!
		
		SystemObjectType type = _typeHierarchy.getType(object.getTypeId());
		
		if(type == null) return false;
		Long keepTime = getKeepTime(type);
		if(keepTime == null) return false;
		long invalidTime = object.getFirstInvalidTime();
		if(invalidTime == 0){
			throw new IllegalArgumentException("Objekt ist noch gültig");
		}
		return System.currentTimeMillis() > keepTime + invalidTime;
	}

	/**
	 * Gibt die Zeit zurück, die historische Objekte eines Typs mindestens behalten werden.
	 * Befindet sich ein Eintrag in der {@link #_objectKeepTimes}-Map, wird dieser benutzt.
	 * Ansonsten wird der nächsthöhere übergeordnete Typ betrachtet. Gibt es mehrere Supertypen,
	 * dann müssen für alle Supertypen (direkt oder indirekt) Vorhaltezeiträume definiert sein
	 * und es wird der jeweils längste Vorhaltezeitraum benutzt.
	 * @param type Typ (sollte DynamicObjectType implementieren)
	 * @return vorhaltezeitraum oder null falls Objekt nie gelöscht werden darf.
	 */
	public Long getKeepTime(final SystemObjectType type) {
		if(type instanceof DynamicObjectType) {
			DynamicObjectType objectType = (DynamicObjectType) type;
			if(_objectKeepTimes.containsKey(objectType)) {
				return _objectKeepTimes.get(objectType);
			}
			Collection<SystemObjectType> superTypes = _typeHierarchy.getSuperTypes(type);
			final List<Long> times = new ArrayList<Long>();
			for(SystemObjectType superType : superTypes) {
				Long time = getKeepTime(superType);
				if(time != null){
					times.add(time);
				}
				else {
					return null;
				}
			}
			if(times.isEmpty()) return null; // kein Vorhaltezeitraum für irgendeinen übergeordneten Typ definiert
			return Collections.max(times);
		}
		return null;
	}


	/**
	 * Gibt die Zeit zurück, die Referenzen eines (dynamischen) Mengentyps mindestens behalten werden.
	 * Befindet sich ein Eintrag in der {@link #_setKeepTimes}-Map, wird dieser benutzt.
	 * Ansonsten wird der nächsthöhere übergeordnete Typ betrachtet. Gibt es mehrere Supertypen,
	 * dann müssen für alle Supertypen (direkt oder indirekt) Vorhaltezeiträume definiert sein
	 * und es wird der jeweils längste Vorhaltezeitraum benutzt.
	 * @param type Typ (sollte DynamicObjectType implementieren)
	 * @return vorhaltezeitraum oder null falls Objekt nie gelöscht werden darf.
	 */
	@Override
	public Long getSetKeepTime(final ObjectSetType type) {
		if(!type.isMutable()) throw new IllegalArgumentException();
		if(_setKeepTimes.containsKey(type)) {
			return _setKeepTimes.get(type);
		}
		return _defaultSetKeepTime;
	}
}
