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

import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.util.HashBagMap;

import java.util.*;

/**
 * Verwaltung der Super- und Sybtypen eines Datenmodells. Diese Klasse verarbeitet nur aktuell gültige Typen.
 * 
 * Diese Klasse ist threadsicher, da sie unveränderlich ist. Synchronisierung beim Zugriff auf diese Klasse ist nicht erforderlich.  
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TypeHierarchy {

	private final Map<Long, SystemObjectType> _types = new HashMap<Long, SystemObjectType>();
	
	private final HashBagMap<SystemObjectType, SystemObjectType> _superTypes = new HashBagMap<SystemObjectType, SystemObjectType>();
	
	private final HashBagMap<SystemObjectType, SystemObjectType> _subTypes = new HashBagMap<SystemObjectType, SystemObjectType>();
	
	public TypeHierarchy(DataModel dataModel) {
		List<SystemObject> types = dataModel.getTypeTypeObject().getElements();
		for(SystemObject type : types) {
			if(type instanceof SystemObjectType) {
				SystemObjectType systemObjectType = (SystemObjectType) type;
				_types.put(type.getId(), systemObjectType);
				List<SystemObjectType> superTypes = systemObjectType.getSuperTypes();
				_superTypes.addAll(systemObjectType, superTypes);
				for(SystemObjectType superType : superTypes) {
					_subTypes.add(superType, systemObjectType);
				}
			}
		}
	}

	public SystemObjectType getType(long id){
		return _types.get(id);
	}
	
	public Collection<SystemObjectType> getSuperTypes(SystemObjectType type){
		return Collections.unmodifiableCollection(_superTypes.get(type));
	}
	
	public Collection<SystemObjectType> getSubTypes(SystemObjectType type){
		return Collections.unmodifiableCollection(_subTypes.get(type));
	}
	
}
