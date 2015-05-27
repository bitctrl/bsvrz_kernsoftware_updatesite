/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Definition, wann historische dynamische Objekte bereinigt werden können, basierend auf einem Vorhaltezeitraum
 * pro Typ der dynamischen Objekte. Für dynamische Mengentypen kann angegeben werden, wie lange historische Referenzen
 * auf zu löschende dynamsiche Objekte vorgehalten werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13103 $
 */
public class TimeBasedMaintenanceSpec implements MaintenanceSpec {

	private final Map<DynamicObjectType, Long> _objectKeepTimes;
	private final Map<ObjectSetType, Long> _setKeepTimes;
	private Long _defaultSetKeepTime;
	private final DataModel _dataModel;

	public TimeBasedMaintenanceSpec(DataModel dataModel, Map<DynamicObjectType, Long> objectKeepTimes, Map<ObjectSetType, Long> setKeepTimes, final Long defaultSetKeepTime) {
		_dataModel = dataModel;
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
		SystemObject type = _dataModel.getObject(object.getTypeId());
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
	public Long getKeepTime(final SystemObject type) {
		if(type instanceof DynamicObjectType) {
			DynamicObjectType objectType = (DynamicObjectType) type;
			if(_objectKeepTimes.containsKey(objectType)) {
				return _objectKeepTimes.get(objectType);
			}
			List<SystemObjectType> superTypes = objectType.getSuperTypes();
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
