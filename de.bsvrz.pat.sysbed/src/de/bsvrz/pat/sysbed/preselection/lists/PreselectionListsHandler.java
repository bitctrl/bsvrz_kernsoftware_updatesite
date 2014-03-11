/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.preselection.lists;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.preselection.util.SortUtil;

import java.util.*;

/**
 * Die Klasse <code>PreselectionListsHandler</code> verarbeitet die Daten des Panels {@link PreselectionLists}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10130 $
 */
class PreselectionListsHandler {

	/** speichert ein Objekt der Klasse <code>PreselectionLists</code> */
	private PreselectionLists _preselectionLists;

	/* diese Variablen speichern die für die Listen benötigten Objekte */
	private List<SystemObject> _objectsDependOnTree;

	private List<SystemObject> _objectsDependOnObjectType;

	private List<SystemObject> _objectsDependOnAtg;

	private List<SystemObject> _objectsDependOnAsp;

	private Set<SystemObjectType> _objectTypeFilter;

	private Set<AttributeGroup> _wantedAttributeGroupFilter;

	private Set<Aspect> _wantedAspectFilter;

	private Set<Aspect> _aspectFilter;

	private Set<AttributeGroup> _attributeGroupFilter;

	/**
	 * Konstruktor, damit {@link PreselectionLists} und <code>PreselectionListsHandler</code> miteinander arbeiten können. D.h.
	 * <code>PreselectionListsHandler</code> verarbeitet die Daten (Objekte) und stellt sie wieder auf dem Panel PreselectionLists dar.
	 *
	 * @param preselectionLists das Panel PreselectionLists
	 */
	PreselectionListsHandler(PreselectionLists preselectionLists) {
		_preselectionLists = preselectionLists;
	}

	/**
	 * Versetzt die Listen in ihren initialen Zustand. Die Listen werden anhand der übergebenen SystemObjekte erstellt und angezeigt.
	 *
	 * @param systemObjects die anzuzeigenden SystemObjekte
	 */
	void setObjects(Collection<SystemObject> systemObjects) {
		// doppelte SystemObjekte aussortieren, Menge sortieren und speichern
		final Set<SystemObject> set = new HashSet<SystemObject>();
		set.addAll(systemObjects);

		_objectsDependOnTree = SortUtil.sortCollection(set) ;

		final List<SystemObjectType> objectTypes = new LinkedList<SystemObjectType>();
		if(_objectTypeFilter != null) objectTypes.addAll(_objectTypeFilter);

		final List<AttributeGroup> attributeGroups = new LinkedList<AttributeGroup>();
		if(_attributeGroupFilter != null) attributeGroups.addAll(_attributeGroupFilter);

		final List<Aspect> aspects = new LinkedList<Aspect>();
		if(_aspectFilter != null) aspects.addAll(_aspectFilter);

		init(objectTypes, attributeGroups, aspects);
	}

	private void init(List<SystemObjectType> systemObjectTypes, List<AttributeGroup> attributeGroups, List<Aspect> aspects) {
		objectsDependOn(systemObjectTypes, attributeGroups, aspects);
		_preselectionLists.setObjectTypeList(getObjectTypeData(_objectsDependOnTree));
		_preselectionLists.setAtgList(getAtgData(_objectsDependOnObjectType));
		_preselectionLists.setAspList(getAspData(_objectsDependOnAtg, new LinkedList<AttributeGroup>()));
		_preselectionLists.setObjectList(_objectsDependOnAsp);
	}

	/**
	 * Liefert zu den übergebenden SystemObjekten ihre Objekttypen und die Objekttypen von denen sie abgeleitet sind.
	 *
	 * @param objects SystemObjekte, zu denen die Objekttypen gebraucht werden
	 *
	 * @return alle Objekttypen passend zu den SystemObjekten
	 */
	private List getObjectTypeData(List<SystemObject> objects) {
		final HashSet<SystemObjectType> allObjectTypes = new HashSet<SystemObjectType>();
		final HashSet<SystemObjectType> allObjectTypeSet = new HashSet<SystemObjectType>();
		// Erst die Typen der selektierten Objekte ermitteln
		for(SystemObject systemObject : objects) {
			allObjectTypes.add(systemObject.getType());
		}
		// Dann für jeden Typ die übergeordneten Typen abfragen
		for(SystemObjectType objectType : allObjectTypes) {
			allObjectTypeSet.addAll(getAllSuperTypes(objectType, allObjectTypeSet));
		}
		// Alle Objekt-Typen ermittelt - prüfen, ob eine Filterung vorliegt
		if(_objectTypeFilter == null || _objectTypeFilter.isEmpty()) {
			// Menge sortieren nach Name
			return SortUtil.sortCollection(allObjectTypeSet);
		}
		else {
			final HashSet<SystemObjectType> filteredObjectTypeSet = new HashSet<SystemObjectType>();
			for(SystemObjectType objectType : allObjectTypeSet) {
				if(_objectTypeFilter.contains(objectType)) filteredObjectTypeSet.add(objectType);
			}
			// Menge sortieren nach Name
			return SortUtil.sortCollection(filteredObjectTypeSet);
		}
	}

	/**
	 * Hilfsmethode zu {@link #getObjectTypeData}. Gibt zurück, von welchen Objekttypen der übergebene Objekttyp erbt.
	 *
	 * @param systemObjectType Objekttyp, zu dem seine Objekttypen gesucht werden
	 * @param set              hier werden die Objekttypen gespeichert
	 *
	 * @return alle gefundenen Objekttypen
	 */
	private HashSet<SystemObjectType> getAllSuperTypes(SystemObjectType systemObjectType, HashSet<SystemObjectType> set) {
		List<SystemObjectType> superTypes = systemObjectType.getSuperTypes();
		if(superTypes.size() == 0) {	 // Rekursionsende -> es gibt keine SuperTypes mehr
			set.add(systemObjectType);
			return set;
		}
		else {
			set.add(systemObjectType);
			// es gibt wieder SuperTypen
			for(SystemObjectType superType : superTypes) {
				set.addAll(getAllSuperTypes(superType, set));
			}
			return set;
		}
	}

	/**
	 * Gibt die zu den Systemobjekten gehörenden Attributgruppen zurück.
	 *
	 * @param objects Liste der Systemobjekte
	 *
	 * @return Liste der gefundenen Attributgruppen
	 */
	private List getAtgData(List<SystemObject> objects) {
		// ermittelt die Atgs
		final HashSet<SystemObjectType> allObjectTypes = new HashSet<SystemObjectType>();
		final HashSet<AttributeGroup> allAttributeGroups = new HashSet<AttributeGroup>();
		// Erst die Typen der selektierten Objekte ermitteln
		for(SystemObject systemObject : objects) {
			allObjectTypes.add(systemObject.getType());
		}
		// Dann für jeden Typ die Attributgruppen abrufen
		for(SystemObjectType objectType : allObjectTypes) {
			final List<AttributeGroup> attributeGroups = objectType.getAttributeGroups();
			for(AttributeGroup attributeGroup : attributeGroups) {
				allAttributeGroups.add(attributeGroup);
			}
		}
		// Prüfen, ob Filterung vorliegt
		if(_attributeGroupFilter == null || _attributeGroupFilter.isEmpty()) {
			// Menge nach Namen sortieren
			return SortUtil.sortCollection(allAttributeGroups);
		}
		else {
			// erst filtern, danach sortieren
			final HashSet<AttributeGroup> filteredAttributeGroups = new HashSet<AttributeGroup>();
			for(AttributeGroup attributeGroup : allAttributeGroups) {
				if(_attributeGroupFilter.contains(attributeGroup)) filteredAttributeGroups.add(attributeGroup);
			}
			// Menge sortieren
			return SortUtil.sortCollection(filteredAttributeGroups);
		}
	}

	/**
	 * Gibt die zu den Systemobjekten gehörenden Aspekte in Abhängigkeit der Attributgruppen zurück.
	 *
	 * @param objects      Liste der Systemobjekte
	 * @param selectedAtgs Liste der ausgewählten Attributgruppen
	 *
	 * @return Liste der gefundenen Aspekte
	 */
	private List getAspData(List<SystemObject> objects, List<AttributeGroup> selectedAtgs) {
		// ermittelt die Aspekte
		final HashSet<Aspect> allAspects = new HashSet<Aspect>();

		final HashSet<SystemObjectType> allObjectTypes = new HashSet<SystemObjectType>();
		final HashSet<AttributeGroup> allAttributeGroups = new HashSet<AttributeGroup>();
		// Erst die Typen der selektierten Objekte ermitteln
		for(SystemObject systemObject : objects) {
			allObjectTypes.add(systemObject.getType());
		}
		// Dann für jeden Typ die Attributgruppen abrufen
		for(SystemObjectType objectType : allObjectTypes) {
			final List<AttributeGroup> attributeGroups = objectType.getAttributeGroups();
			for(AttributeGroup attributeGroup : attributeGroups) {
				allAttributeGroups.add(attributeGroup);
			}
		}
		
		for(AttributeGroup attributeGroup : allAttributeGroups) {
			if(selectedAtgs != null && !selectedAtgs.isEmpty()) {
				if(selectedAtgs.contains(attributeGroup)) {
					for(Aspect aspect : attributeGroup.getAspects()) {
						allAspects.add(aspect);
					}
				}
			}
			else {
				for(Aspect aspect : attributeGroup.getAspects()) {
					allAspects.add(aspect);
				}
			}
		}

		// Prüfen, ob Filterung vorliegt
		if(_aspectFilter == null || _aspectFilter.isEmpty()) {
			// Menge sortieren
			return SortUtil.sortCollection(allAspects);
		}
		else {
			// erst filtern, danach sortieren
			final HashSet<Aspect> filteredAspects = new HashSet<Aspect>();
			for(Aspect aspect : allAspects) {
				if(_aspectFilter.contains(aspect)) filteredAspects.add(aspect);
			}
			// Menge sortieren
			return SortUtil.sortCollection(filteredAspects);
		}
	}

	/**
	 * Überprüft rekursiv, ob der übergebene Objekttyp eines Systemobjekts in der Liste der Objekttypen vorkommt.
	 *
	 * @param systemObjectType ein Objekttyp
	 * @param objects          Liste von Objekttypen
	 *
	 * @return true/false ob Objekttyp in der Liste der Objekttypen vorkommt
	 */
	private boolean hasObjectSuperType(SystemObjectType systemObjectType, List objects) {
		for(Iterator iterator = objects.iterator(); iterator.hasNext();) {
			if(systemObjectType.equals((SystemObjectType)iterator.next())) {
				return true;
			}
		}
		// es ist nicht drin -> Obertypen betrachten, falls es welche gibt
		List superTypes = systemObjectType.getSuperTypes();
		for(Iterator iterator = superTypes.iterator(); iterator.hasNext();) {
			SystemObjectType systemObjectSuperType = (SystemObjectType)iterator.next();
			if(hasObjectSuperType(systemObjectSuperType, objects)) return true;
		}
		return false;
	}

	/**
	 * Zu jeder der vier Listen der Klasse <code>PreselectionLists</code> werden in Abhängigkeit der selektierten Werte die Objekte gefiltert und gespeichert.
	 *
	 * @param selectedObjectTypes die selektierten Objekttypen
	 * @param selectedATGs        die selektierten Attributgruppen
	 * @param selectedASPs        die selektierten Aspekte
	 */
	private void objectsDependOn(List<SystemObjectType> selectedObjectTypes, List<AttributeGroup> selectedATGs, List<Aspect> selectedASPs) {
		// Filterung muss berücksichtigt werden
		if(selectedObjectTypes == null || selectedObjectTypes.isEmpty()) {
			// falls es eine Filterung gibt, dann diese nehmen
			if(_objectTypeFilter != null) {
				selectedObjectTypes = new ArrayList<SystemObjectType>(_objectTypeFilter);
			}
		}

		Set<SystemObject> set;
		if(selectedObjectTypes != null && !selectedObjectTypes.isEmpty()) {   // es gibt selektierte Objekttypen
			set = new HashSet<SystemObject>();
			for(Iterator iterator = _objectsDependOnTree.iterator(); iterator.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator.next();
				SystemObjectType systemObjectType = systemObject.getType();
				// überprüfe rekursiv, ob dieses Objekt eines dieser ObjektTypen als Obertypen besitzt
				if(hasObjectSuperType(systemObjectType, selectedObjectTypes)) {
					set.add(systemObject);
				}
			}
			_objectsDependOnObjectType = SortUtil.sortCollection(set);
		}
		else {								// es gibt keine
			_objectsDependOnObjectType = _objectsDependOnTree;
		}

		if(selectedATGs == null || selectedATGs.isEmpty()) {
			// falls es eine Filterung gibt, dann diese nehmen
			if(_attributeGroupFilter != null) {
				selectedATGs = new ArrayList<AttributeGroup>(_attributeGroupFilter);
			}
		}

		if(selectedATGs != null && !selectedATGs.isEmpty()) {		  // es gibt selektierte Attributgruppen
			set = new HashSet<SystemObject>();
			for(Iterator iterator1 = _objectsDependOnObjectType.iterator(); iterator1.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator1.next();
				List atgs = systemObject.getType().getAttributeGroups();
				for(Iterator iterator2 = selectedATGs.iterator(); iterator2.hasNext();) {
					if(atgs.contains((AttributeGroup)iterator2.next())) {
						set.add(systemObject);
					}
				}
			}
			_objectsDependOnAtg = SortUtil.sortCollection(set);
		}
		else {								// es gibt keine
			_objectsDependOnAtg = _objectsDependOnObjectType;
		}

		if(selectedASPs == null || selectedASPs.isEmpty()) {
			if(_aspectFilter != null) {
				selectedASPs = new ArrayList<Aspect>(_aspectFilter);
			}
		}
		if(selectedASPs != null && !selectedASPs.isEmpty()) {		  // es gibt selektierte Aspekte
			set = new HashSet<SystemObject>();
			for(Iterator iterator1 = _objectsDependOnAtg.iterator(); iterator1.hasNext();) {
				SystemObject systemObject = (SystemObject)iterator1.next();
				List atgs = systemObject.getType().getAttributeGroups();
				for(Iterator iterator2 = atgs.iterator(); iterator2.hasNext();) {
					AttributeGroup attributeGroup = (AttributeGroup)iterator2.next();
					Collection asps = attributeGroup.getAspects();
					for(Iterator iterator3 = selectedASPs.iterator(); iterator3.hasNext();) {
						if(asps.contains((Aspect)iterator3.next())) {
							set.add(systemObject);
						}
					}
				}
			}
			_objectsDependOnAsp = SortUtil.sortCollection(set);
		}
		else {								// es gibt keine
			_objectsDependOnAsp = _objectsDependOnAtg;
		}
	}

	/**
	 * Diese Methode aktualisiert die Listen der Attributgruppen, Aspekte und Objekte in Abhängigkeit von den selektierten Werten der Listen Objekttyp,
	 * Attributgruppe und Aspekt.
	 *
	 * @param selectedObjectTypes die selektierten Objekttypen
	 * @param selectedATGs        die selektierten Attributgruppen
	 * @param selectedASPs        die selektierten Aspekte
	 */
	public void objectsDependOnObjectType(List<SystemObjectType> selectedObjectTypes, List<AttributeGroup> selectedATGs, List<Aspect> selectedASPs) {
		objectsDependOn(selectedObjectTypes, selectedATGs, selectedASPs);
		_preselectionLists.setAtgList(getAtgData(_objectsDependOnObjectType));
		_preselectionLists.setAspList(getAspData(_objectsDependOnAtg, selectedATGs));
		_preselectionLists.setObjectList(_objectsDependOnAsp);
	}

	/**
	 * Diese Methode aktualisiert die Listen der Aspekte und der Objekte in Abhängigkeit von den selektierten Werten der Listen Objekttyp, Attributgruppe und
	 * Aspekt.
	 *
	 * @param selectedObjectTypes die selektierten Objekttypen
	 * @param selectedATGs        die selektierten Attributgruppen
	 * @param selectedASPs        die selektierten Aspekte
	 */
	public void objectsDependOnAtg(List<SystemObjectType> selectedObjectTypes, List<AttributeGroup> selectedATGs, List<Aspect> selectedASPs) {
		objectsDependOn(selectedObjectTypes, selectedATGs, selectedASPs);
		_preselectionLists.setAspList(getAspData(_objectsDependOnAtg, selectedATGs));
		_preselectionLists.setObjectList(_objectsDependOnAsp);
	}

	/**
	 * Diese Methode aktualisiert die Listen der Objekte in Abhängigkeit von den selektierten Werten der Listen Objekttyp, Attributgruppe und Aspekt.
	 *
	 * @param selectedObjectType die selektierten Objekttypen
	 * @param selectedATGs       die selektierten Attributgruppen
	 * @param selectedASPs       die selektierten Aspekte
	 */
	public void objectsDependOnAsp(List<SystemObjectType> selectedObjectType, List<AttributeGroup> selectedATGs, List<Aspect> selectedASPs) {
		objectsDependOn(selectedObjectType, selectedATGs, selectedASPs);
		_preselectionLists.setObjectList(_objectsDependOnAsp);
	}

	void setObjectTypeFilter(Collection<SystemObjectType> objectTypes) {
		_objectTypeFilter = new HashSet<SystemObjectType>(objectTypes);
		acquireFilterAttributeGroups();
		acquireFilterAspects();
		if(_objectsDependOnTree != null) {
			init(new ArrayList<SystemObjectType>(objectTypes), new ArrayList<AttributeGroup>(_attributeGroupFilter), new ArrayList<Aspect>(_aspectFilter));
		}
	}

	void setAttributeGroupFilter(Collection<AttributeGroup> attributeGroups) {
		_wantedAttributeGroupFilter = new HashSet<AttributeGroup>(attributeGroups);
		acquireFilterAttributeGroups();
		acquireFilterAspects();
		if(_objectsDependOnTree != null) {
			// Objekte wurden bereits übergeben
			objectsDependOnObjectType(null, new ArrayList<AttributeGroup>(attributeGroups), null);
		}
	}

	void setAspectFilter(Collection<Aspect> aspects) {
		_wantedAspectFilter = new HashSet<Aspect>(aspects);
		acquireFilterAspects();
		if(_objectsDependOnTree != null) {
			// Objekte wurden bereits übergeben
			objectsDependOnAtg(null, null, new ArrayList<Aspect>(aspects));
		}
	}

	private void acquireFilterAttributeGroups() {
		// relevante Attributgruppen ermitteln
		if(_objectTypeFilter != null && !_objectTypeFilter.isEmpty() && _wantedAttributeGroupFilter != null && !_wantedAttributeGroupFilter.isEmpty()) {
			// Schnittmenge ermitteln
			final Set<AttributeGroup> attributeGroupFilter = new HashSet<AttributeGroup>();

			// alle Attributgruppen der ObjektTypen durchgehen
			for(SystemObjectType objectType : _objectTypeFilter) {
				for(AttributeGroup attributeGroup : objectType.getAttributeGroups()) {
					if(_wantedAttributeGroupFilter.contains(attributeGroup)) {
						attributeGroupFilter.add(attributeGroup);
					}
				}
			}
			_attributeGroupFilter = attributeGroupFilter;
		}
		else if(_objectTypeFilter != null && !_objectTypeFilter.isEmpty()) {
			final Set<AttributeGroup> attributeGroupFilter = new HashSet<AttributeGroup>();
			for(SystemObjectType objectType : _objectTypeFilter) {
				attributeGroupFilter.addAll(objectType.getAttributeGroups());
			}
			_attributeGroupFilter = attributeGroupFilter;
			_wantedAttributeGroupFilter = attributeGroupFilter;
		}
		else if(_wantedAttributeGroupFilter != null && !_wantedAttributeGroupFilter.isEmpty()) {
			_attributeGroupFilter = new HashSet<AttributeGroup>(_wantedAttributeGroupFilter);
		}
	}

	private void acquireFilterAspects() {
		// relevante Aspekte ermitteln
		if(_wantedAttributeGroupFilter != null && !_wantedAttributeGroupFilter.isEmpty() && _wantedAspectFilter != null && !_wantedAspectFilter.isEmpty()) {
			// Schnittmenge ermitteln, wenn die Filter gesetzt und nicht leer sind
			final Set<Aspect> aspectFilter = new HashSet<Aspect>();

			// alle Aspekte der Attributgruppen ermitteln und prüfen, ob sie in den gewünschten Aspekten vorkommen.
			for(AttributeGroup attributeGroup : _wantedAttributeGroupFilter) {
				for(Aspect aspect : attributeGroup.getAspects()) {
					if(_wantedAspectFilter.contains(aspect)) {
						aspectFilter.add(aspect);
					}
				}
			}
			_aspectFilter = aspectFilter;
		}
		else if(_wantedAttributeGroupFilter != null && !_wantedAttributeGroupFilter.isEmpty()) {
			final Set<Aspect> aspectFilter = new HashSet<Aspect>();
			for(AttributeGroup attributeGroup : _wantedAttributeGroupFilter) {
				aspectFilter.addAll(attributeGroup.getAspects());
			}
			_aspectFilter = aspectFilter;
		}
		else if(_wantedAspectFilter != null && !_wantedAspectFilter.isEmpty()) {
			_aspectFilter = new HashSet<Aspect>(_wantedAspectFilter);
		}
	}
}
