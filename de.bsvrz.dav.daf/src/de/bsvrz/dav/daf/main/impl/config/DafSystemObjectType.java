/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Klasse, die den Zugriff auf Objekttypen seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafSystemObjectType extends DafConfigurationObject implements SystemObjectType {

	/** Kennung, die <code>true</code> ist, wenn die Namen von Objekten des Typs permanent sind? */
	private boolean _isNameOfObjectsPermanent;

	/** Liste der Attributgruppen dieses Typs. */
	private List<AttributeGroup> _attributeGroups;

	/** Liste der eigenen nicht geerbten Attributesgruppen dieses Typs */
	private List<AttributeGroup> _directAttributeGroups;

	/** Liste der Mengenverwendungen dieses Typs */
	private List<ObjectSetUse> _objectSetUses;

	/** Liste der eigenen nicht geerbten Mengenverwendungen dieses Typs */
	private List<ObjectSetUse> _directObjectSetUses;

	/** Liste der Supertypen */
	private List<SystemObjectType> _superTypes;

	/** Liste der Subtypen */
	private List<SystemObjectType> _subTypes;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	protected DafSystemObjectType(DafDataModel dataModel) {
		super(dataModel);
		_internType = SYSTEM_OBJECT_TYPE;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	protected DafSystemObjectType(
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
			long setIds[],
			boolean hasPermanentName
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = SYSTEM_OBJECT_TYPE;
		_isNameOfObjectsPermanent = hasPermanentName;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	@Deprecated
	protected DafSystemObjectType(
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
			long setIds[],
			boolean isConfigurator,
			boolean hasPermanentName
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = SYSTEM_OBJECT_TYPE;
		_isNameOfObjectsPermanent = hasPermanentName;
	}

	public String parseToString() {
		String str;
		str = "Objekt Typ: \n";
		str = str + super.parseToString();
		str += "Konfigurierend: " + isConfigurating() + "\n";
		str += "Name ist permanent: " + _isNameOfObjectsPermanent + "\n";
		final List attributeGroups = getAttributeGroups();
		for(Object attributeGroup : attributeGroups) {
			str += ((DafAttributeGroup)attributeGroup).parseToString();
		}
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeBoolean(isConfigurating());
		out.writeBoolean(_isNameOfObjectsPermanent);
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		in.readBoolean(); // Configurating, wird nicht benötigt, da bereits über Klasse definiert.
		_isNameOfObjectsPermanent = in.readBoolean();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_isNameOfObjectsPermanent = deserializer.readBoolean();
	}

	public boolean isConfigurating(){
		return true;
	}

	public boolean isNameOfObjectsPermanent() {
		return _isNameOfObjectsPermanent;
	}

	public final List<AttributeGroup> getAttributeGroups() {
		if(_attributeGroups == null) {
			List<AttributeGroup> directAttributeGroups = getDirectAttributeGroups();
			List<AttributeGroup> attributeGroups = new ArrayList<AttributeGroup>();
			attributeGroups.addAll(directAttributeGroups);

			List list = getSuperTypes();
			if((list != null) && (list.size() > 0)) {
				for(int i = 0; i < list.size(); ++i) {
					DafSystemObjectType _systemObjectType = (DafSystemObjectType)list.get(i);
					List tmpList = _systemObjectType.getAttributeGroups();
					if(tmpList != null) {
						for(int j = 0; j < tmpList.size(); ++j) {
							DafAttributeGroup attributeGroup = (DafAttributeGroup)tmpList.get(j);
							long attributeGroupId = attributeGroup.getId();
							int k = 0;
							for(; k < attributeGroups.size(); ++k) {
								DafAttributeGroup _attributeGroup = (DafAttributeGroup)attributeGroups.get(k);
								if(_attributeGroup.getId() == attributeGroupId) {
									break;
								}
							}
							if(k == attributeGroups.size()) {
								attributeGroups.add(attributeGroup);
							}
						}
					}
				}
			}
			_attributeGroups = Collections.unmodifiableList(attributeGroups);
		}
		return _attributeGroups;
	}

	public List<AttributeGroup> getDirectAttributeGroups() {
		if(_directAttributeGroups == null) {
			List<AttributeGroup> directAttributeGroups = new ArrayList<AttributeGroup>();
			ObjectSet _attributeGroupSet = getObjectSet("Attributgruppen");
			if(_attributeGroupSet != null) {
				List tmpList = _attributeGroupSet.getElements();
				if(tmpList != null) {
					for(int j = 0; j < tmpList.size(); ++j) {
						DafAttributeGroup attributeGroup = (DafAttributeGroup)tmpList.get(j);
						if(attributeGroup != null) {
							directAttributeGroups.add(attributeGroup);
						}
					}
				}
			}
			_directAttributeGroups = Collections.unmodifiableList(directAttributeGroups);
		}
		return _directAttributeGroups;
	}

	public final List<ObjectSetUse> getObjectSetUses() {
		if(_objectSetUses == null) {
			List<ObjectSetUse> directObjectSetUses = getDirectObjectSetUses();
			List<ObjectSetUse> objectSetUses = new ArrayList<ObjectSetUse>();
			objectSetUses.addAll(directObjectSetUses);

			List list = getSuperTypes();
			if((list != null) && (list.size() > 0)) {
				for(int i = 0; i < list.size(); ++i) {
					DafSystemObjectType _systemObjectType = (DafSystemObjectType)list.get(i);
					List tmpList = _systemObjectType.getObjectSetUses();
					if(tmpList != null) {
						for(int j = 0; j < tmpList.size(); ++j) {
							DafObjectSetUse objectSetUse = (DafObjectSetUse)tmpList.get(j);
							long objectSetUseId = objectSetUse.getId();
							int k = 0;
							for(; k < objectSetUses.size(); ++k) {
								DafObjectSetUse _objectSetUse = (DafObjectSetUse)objectSetUses.get(k);
								if(_objectSetUse.getId() == objectSetUseId) {
									break;
								}
							}
							if(k == objectSetUses.size()) {
								objectSetUses.add(objectSetUse);
							}
						}
					}
				}
			}
			_objectSetUses = Collections.unmodifiableList(objectSetUses);
		}
		return _objectSetUses;
	}

	public final List<ObjectSetUse> getDirectObjectSetUses() {
		if(_directObjectSetUses == null) {
			ArrayList<ObjectSetUse> directObjectSetUses = new ArrayList<ObjectSetUse>();
			ObjectSet _setUsesSet = getObjectSet("Mengen");
			if(_setUsesSet != null) {
				List tmpList = _setUsesSet.getElements();
				if(tmpList != null) {
					for(int j = 0; j < tmpList.size(); ++j) {
						DafObjectSetUse objectSetUse = (DafObjectSetUse)tmpList.get(j);
						if(objectSetUse != null) {
							directObjectSetUses.add(objectSetUse);
						}
					}
				}
			}
			_directObjectSetUses = Collections.unmodifiableList(directObjectSetUses);
		}
		return _directObjectSetUses;
	}

	public final boolean isBaseType() {
		return (getSuperTypes().size() == 0);
	}

	public final List<SystemObjectType> getSuperTypes() {
		if(_superTypes == null) {
			List<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
			ObjectSet superTypesSet = getObjectSet("SuperTypen");
			List<SystemObject> elements = superTypesSet.getElements();
			for(SystemObject element : elements) {
				superTypes.add((SystemObjectType)element);
			}
			_superTypes = Collections.unmodifiableList(superTypes);
		}
		return _superTypes;
	}

	public final List<SystemObjectType> getSubTypes() {
		if(_subTypes == null) {
			final List<SystemObjectType> subTypes = new ArrayList<SystemObjectType>();
			SystemObjectType typeType = getDataModel().getTypeTypeObject();
			if(typeType != null) {
				List list = typeType.getObjects();
				if(list != null) {
					for(int i = 0; i < list.size(); ++i) {
						DafSystemObjectType _type = (DafSystemObjectType)list.get(i);
						List tmp = _type.getSuperTypes();
						if((tmp != null) && (tmp.contains(this))) {
							subTypes.add(_type);
						}
					}
				}
			}
			_subTypes = Collections.unmodifiableList(subTypes);
		}
		return _subTypes;
	}

	public final boolean inheritsFrom(SystemObjectType other) {
		// direkte Vererbung
		final List<SystemObjectType> superTypes = getSuperTypes();
		if(superTypes.contains(other)) return true;
		// indirekte Vererbung
		for(SystemObjectType superType : superTypes) {
			if(superType.inheritsFrom(other)) return true;
		}
		return false;
	}

	public List<SystemObject> getObjects() {
		return Collections.unmodifiableList(_dataModel.getObjectsOfType(this));
	}

	public List<SystemObject> getElements() {
		return getObjects();
	}

	public List<SystemObject> getElements(long time) {
		return Collections.unmodifiableList(_dataModel.getElementsOfType(this, time));
	}

	public List<SystemObject> getElementsInPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(_dataModel.getElementsOfTypeInPeriod(this, startTime, endTime));
	}

	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime) {
		return Collections.unmodifiableList(_dataModel.getElementsOfTypeDuringPeriod(this, startTime, endTime));
	}
}
