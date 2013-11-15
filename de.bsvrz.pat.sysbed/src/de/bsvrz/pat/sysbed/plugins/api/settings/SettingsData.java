/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.pat.sysbed.plugins.api.settings;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

import java.util.*;

/**
 * Diese Klasse dient dazu, die Einstellungen eines Dialogs zu speichern. Ein Dialog ist Bestandteil eines {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Moduls}. Die zu jedem
 * Modul geh�rende Datenidentifikation (Attributgruppe, Aspekt und Objekte), die Klasse des benutzten Moduls und der Name des Moduls werden in einem Objekt
 * dieser Klasse gespeichert. Zus�tzlich k�nnen die Parameter eines Dialogs als {@link KeyValueObject Key/Value-Paare} �bergeben werden. Der Einstellung kann
 * ein Name zugewiesen werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5052 $
 */
public class SettingsData {

	/** Name der Einstellung */
	private String _title = "";

	/** Name des Moduls, welche den Dialog darstellt */
	private String _moduleName;

	/** die Klasse des Moduls */
	private Class _moduleClass;

	/** die Attributgruppe der Datenidentifikation */
	private AttributeGroup _attributeGroup;

	/** der Aspekt der Datenidentifikation */
	private Aspect _aspect;

	/** speichert die Simulationsvariante der Datenidentifikation */
	private int _simulationVariant = -1;

	/** die Objekte der Datenidentifikation */
	private List<SystemObject> _objects;

	/** die Objekttypen der Datenidentifikation */
	private List<SystemObjectType> _objectTypes;

	/** speichert die Key/Value-Objekte */
	private List<KeyValueObject> _keyValueList = new LinkedList<KeyValueObject>();

	/** speichert den Pfad im Baum, der zur Datenidentifikation geh�rt */
	private String _treePath = "";

	/** speichert, ob es sich um eine g�ltige Einstellung handelt, oder nicht */
	private boolean _isValid = true;


	/** Der Standardkonstruktor erstellt ein Objekt der Klasse SettingsData. */
	public SettingsData() {
	}

	/**
	 * Konstruktor. Ein Objekt wird angelegt, welches die �bergebenen Daten speichert.
	 *
	 * @param objectTypes    die Objekt-Typen
	 * @param attributeGroup die Attributgruppe
	 * @param aspect         der Aspekt
	 * @param objects        die Systemobjekte
	 */
	public SettingsData(final List<SystemObjectType> objectTypes, final AttributeGroup attributeGroup, final Aspect aspect, final List<SystemObject> objects) {
		this("", null, objectTypes, attributeGroup, aspect, objects);
	}

	/**
	 * Konstruktor. Ben�tigt wird der Modulname, die Klasse des Moduls und die Datenidentifikation, bestehend aus einer Attributgruppe, einem Aspekt und beliebig
	 * vielen Objekten. Ein Objekt wird angelegt, welches die �bergebenen Daten speichert.
	 *
	 * @param moduleName     der Name des Moduls
	 * @param moduleClass    die Klasse des Moduls
	 * @param objectTypes    die Objekt-Typen
	 * @param attributeGroup die Attributgruppe der Datenidentifikation
	 * @param aspect         der Aspekt der Datenidentifikation
	 * @param objects        die Objekte der Datenidentifikation
	 */
	public SettingsData(
			final String moduleName,
			final Class moduleClass,
			final List<SystemObjectType> objectTypes,
			final AttributeGroup attributeGroup,
			final Aspect aspect,
			final List<SystemObject> objects
	) {
		_moduleName = moduleName;
		_moduleClass = moduleClass;
		_objectTypes = objectTypes;
		_attributeGroup = attributeGroup;
		_aspect = aspect;
		_objects = objects;
	}

	/**
	 * F�gt ein Key/Value-Paar den Einstellungen hinzu.
	 *
	 * @param keyValueObject ein Key/Value-Paar
	 */
	public void addKeyValueObject(final KeyValueObject keyValueObject) {
		_keyValueList.add(keyValueObject);
	}

	/**
	 * Gibt den Aspekt zur�ck.
	 *
	 * @return der Aspekt der Datenidentifikation
	 */
	public Aspect getAspect() {
		return _aspect;
	}

	/**
	 * Gibt die Attributgruppe zur�ck.
	 *
	 * @return die Attributgruppe der Datenidentifikation
	 */
	public AttributeGroup getAttributeGroup() {
		return _attributeGroup;
	}

	/**
	 * Gibt die Key/Value-Paare zur�ck.
	 *
	 * @return die Key/Value-Paare
	 */
	public List<KeyValueObject> getKeyValueList() {
		return _keyValueList;
	}

	/**
	 * Gibt die Klasse des Moduls zur�ck.
	 *
	 * @return die Klasse des Moduls
	 */
	public Class getModuleClass() {
		return _moduleClass;
	}

	/**
	 * Gibt den Namen des Moduls zur�ck.
	 *
	 * @return Name des Moduls
	 */
	public String getModuleName() {
		return _moduleName;
	}

	/**
	 * Gibt die Objekte zur�ck.
	 *
	 * @return die Objekte der Datenidentifikation
	 */
	public List<SystemObject> getObjects() {
		return _objects;
	}

	/**
	 * Gibt die Objekttypen zur�ck.
	 *
	 * @return die Objekttypen
	 */
	public List<SystemObjectType> getObjectTypes() {
		return _objectTypes;
	}

	/**
	 * Gibt die Simulationsvariante der Datenidentifikation zur�ck.
	 *
	 * @return die Simulationvariante
	 */
	public int getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * Gibt den Namen / Titel der Einstellung zur�ck.
	 *
	 * @return Name der Einstellung
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Gibt den Pfad im Baum zur�ck, welcher angew�hlt war, als die Datenidentifikation ausgew�hlt worden ist.
	 *
	 * @return den Pfad im Baum
	 */
	public String getTreePath() {
		return _treePath;
	}

	/**
	 * Gibt zur�ck, ob es sich um eine g�ltige Einstellung handelt, oder nicht.
	 *
	 * @return ob die Einstellung g�ltig ist
	 */
	public boolean isValid() {
		return _isValid;
	}

	/**
	 * Setzt den Aspekt.
	 *
	 * @param aspect der Aspekt
	 */
	public void setAspect(Aspect aspect) {
		_aspect = aspect;
	}

	/**
	 * Setzt die Attributgruppe.
	 *
	 * @param attributeGroup die Attributgruppe
	 */
	public void setAttributeGroup(AttributeGroup attributeGroup) {
		_attributeGroup = attributeGroup;
	}

	/**
	 * Setzt die Key/Value-Paare.
	 *
	 * @param keyValueList eine Liste mit Key/Value-Paaren
	 */
	public void setKeyValueList(final List<KeyValueObject> keyValueList) {
		_keyValueList = keyValueList;
	}

	/**
	 * Setzt die Klasse des Moduls.
	 *
	 * @param moduleClass die Klasse des Moduls
	 */
	public void setModuleClass(Class moduleClass) {
		_moduleClass = moduleClass;
	}

	/**
	 * Setzt den Namen des Moduls.
	 *
	 * @param moduleName der Modulname
	 */
	public void setModuleName(String moduleName) {
		_moduleName = moduleName;
	}

	/**
	 * Setzt die Liste der Systemobjekte.
	 *
	 * @param objects Liste der Systemobjekte
	 */
	public void setObjects(List<SystemObject> objects) {
		_objects = objects;
	}

	/**
	 * Setzt die Liste der Objekttypen.
	 *
	 * @param objectTypes Liste der Objekttypen
	 */
	public void setObjectTypes(List<SystemObjectType> objectTypes) {
		_objectTypes = objectTypes;
	}

	/**
	 * Setzt die Simulationsvariante der Datenidentifikation.
	 *
	 * @param simulationVariant die Simulationsvariante der Datenidentifikation
	 */
	public void setSimulationVariant(int simulationVariant) {
		_simulationVariant = simulationVariant;
	}

	/**
	 * Setzt den Namen / Titel der Einstellung.
	 *
	 * @param title der Name / Titel der Einstellung
	 */
	public void setTitle(final String title) {
		_title = title;
	}

	/**
	 * �bergibt den Pfad im Baum, welcher angew�hlt war, als die Datenidentifikation ausgew�hlt wurde.
	 *
	 * @param treePath der Pfad im Baum
	 */
	public void setTreePath(final String treePath) {
		_treePath = treePath;
	}

	/**
	 * Setzt die G�ltigkeit der Einstellung. Ist die Einstellung ung�ltig, dann kann sie auch nicht gestartet werden.
	 *
	 * @param valid ob die Einstellung g�ltig ist
	 */
	public void setValid(boolean valid) {
		_isValid = valid;
	}

	/**
	 * Gibt eine String-Repr�sentation dieses Objekts zur�ck.
	 *
	 * @return String-Repr�sentation dieses Objekts
	 */
	public String toString() {
		return "SettingsData{" + "_title='" + _title + "'" + ", _moduleName='" + _moduleName + "'" + ", _moduleClass=" + _moduleClass + ", _attributeGroup="
		       + _attributeGroup + ", _aspect=" + _aspect + ", _simulationVariant=" + _simulationVariant + ", _objects=" + _objects + ", _objectTypes="
		       + _objectTypes + ", _keyValueList=" + _keyValueList + ", _treePath='" + _treePath + "'" + ", _isValid=" + _isValid + "}";
	}
}
