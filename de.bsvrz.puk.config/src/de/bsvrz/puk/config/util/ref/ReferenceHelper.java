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

package de.bsvrz.puk.config.util.ref;

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.util.HashBagMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ReferenceHelper {

	private final HashBagMap<SystemObjectType, SystemObjectType> _typesByReferencingType;

	public ReferenceHelper(final DataModel dataModel) {
		// Speichert zu Typen die AttributGruppen, die sie referenzieren
		HashBagMap<SystemObjectType, AttributeGroup> atgByReferencingTypes = new HashBagMap<SystemObjectType, AttributeGroup>();

		// Speichert zu Attributgruppen die Typen, die sie benutzen
		HashBagMap<AttributeGroup, SystemObjectType> atgsToTypes = new HashBagMap<AttributeGroup, SystemObjectType>();

		_typesByReferencingType = new HashBagMap<SystemObjectType, SystemObjectType>();

		SystemObjectType attributeGroupType = dataModel.getType(Pid.Type.ATTRIBUTE_GROUP);
		for(SystemObject element : attributeGroupType.getElements()) {
			if(element instanceof AttributeGroup) {
				AttributeGroup attributeGroup = (AttributeGroup) element;
				for(AttributeGroupUsage attributeGroupUsage : attributeGroup.getAttributeGroupUsages()) {
					if(attributeGroupUsage.isConfigurating()) {
						List<Attribute> attributes = attributeGroup.getAttributes();
						for(Attribute attribute : attributes) {
							collectReferences(atgByReferencingTypes, attribute.getAttributeType(), attributeGroup);
						}
						break;
					}
				}
			}
		}

		SystemObjectType typeType = dataModel.getTypeTypeObject();
		for(SystemObject element : typeType.getElements()) {
			if(element instanceof SystemObjectType) {
				SystemObjectType type = (SystemObjectType) element;
				for(AttributeGroup attributeGroup : type.getAttributeGroups()) {
					for(AttributeGroupUsage attributeGroupUsage : attributeGroup.getAttributeGroupUsages()) {
						if(attributeGroupUsage.isConfigurating()) {
							atgsToTypes.add(attributeGroup, type);
							break;
						}
					}
				}
				for(ObjectSetUse objectSetUse : type.getObjectSetUses()) {
					for(SystemObjectType systemObjectType : objectSetUse.getObjectSetType().getObjectTypes()) {
						addRecursive(_typesByReferencingType, type, systemObjectType);
					}
				}
			}
		}

		for(SystemObject element : typeType.getElements()) {
			if(element instanceof SystemObjectType) {
				SystemObjectType type = (SystemObjectType) element;
				Collection<AttributeGroup> atgs = atgByReferencingTypes.get(type);
				for(AttributeGroup atg : atgs) {
					Collection<SystemObjectType> refTypes = atgsToTypes.get(atg);
					_typesByReferencingType.addAll(type, refTypes);
				}
			}
		}


	}

	private static void collectReferences(
			final HashBagMap<SystemObjectType, AttributeGroup> result,
			final AttributeType attributeType,
			final AttributeGroup attributeGroup) {
		if(attributeType instanceof ReferenceAttributeType) {
			ReferenceAttributeType refAtt = (ReferenceAttributeType) attributeType;
			SystemObjectType refType = refAtt.getReferencedObjectType();
			if(refType == null){
				refType=attributeType.getDataModel().getTypeTypeObject();
			}
			addRecursive(result, attributeGroup, refType);
		}
		else if(attributeType instanceof AttributeListDefinition) {
			AttributeListDefinition listDefinition = (AttributeListDefinition) attributeType;
			List<Attribute> attributes = listDefinition.getAttributes();
			for(Attribute attribute : attributes) {
				collectReferences(result, attribute.getAttributeType(), attributeGroup);
			}
		}
	}

	private static <T> void addRecursive(final HashBagMap<SystemObjectType, T> result, final T t, final SystemObjectType refType) {
		result.add(refType, t);
		// Wer A referenzieren kann, kann auch alle Kindtypen von A referenzieren
		for(SystemObjectType type : refType.getSubTypes()) {
			addRecursive(result, t, type);
		}
	}


	public Collection<? extends SystemObjectType> getReferencingTypes(final SystemObjectType type) {
		return Collections.unmodifiableCollection(_typesByReferencingType.get(type));
	}
}
