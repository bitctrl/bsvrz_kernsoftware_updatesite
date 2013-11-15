/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.preselection.treeFilter.standard;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.configObjectAcquisition.ConfigurationHelper;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.pat.sysbed.preselection.treeFilter.plugins.api.ExtendedFilter;

import java.util.*;

/**
 * Die Klasse <code>Filter</code> speichert ein Kriterium und die dazugehörigen Werte und bietet Methoden an, um Systemobjekte anhand des Kriteriums zu
 * filtern.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public class Filter {

	/** Der Debug-Logger der Klasse */
	private static Debug _debug = Debug.getLogger();

	/** speichert das Kriterium des Filters */
	private String _criteria;

	/** speichert die Einträge zum Kriterium */
	private String[] _values;

	/** speichert die Verbindung zum Datenverteiler */
	private ClientDavInterface _connection;

	/** filtert die aufgelisteten Objekte nach dem Konfigurationsbereich */
	public final static String CONFIGURATIONAREA = "Konfigurationsbereich";

	/** filtert die aufgelisteten Objekte nach dem Objekttyp */
	public final static String OBJECTTYPE = "Objekttyp";

	/** filtert die aufgelisteten Objekte nach der Attributgruppe */
	public final static String ATTRIBUTEGROUP = "Attributgruppe";

	/** filtert die aufgelisteten Objekte nach dem Aspekt */
	public final static String ASPECT = "Aspekt";

	/** filtert die aufgelisteten Objekte nach dem Objekt */
	public final static String OBJECT = "Objekt";

	/** filtert die aufgelisteten Objekte nach einem erweiterten Filter */
	public final static String EXTENDED = "Erweitert";


	/**
	 * Erzeugt ein neues Objekt der Klasse <code>Filter</code>.
	 *
	 * @param criteria   Filterkriterium
	 * @param values     Filterattribute
	 * @param connection Verbindung zum Datenverteiler
	 */
	public Filter(String criteria, String[] values, ClientDavInterface connection) {
		_criteria = criteria;
		_values = values;
		_connection = connection;
	}

	/**
	 * Die übergebenen Systemobjekte werden entsprechend des Kriteriums gefiltert und zurückgegeben.
	 *
	 * @param systemObjects die zu filternden Systemobjekte
	 *
	 * @return die gefilterten Systemobjekte
	 */
	public Collection<SystemObject> filterObjects(Collection<SystemObject> systemObjects) {
		Collection<SystemObject> tempObjects;
		if(systemObjects == null) {
			tempObjects = new LinkedList<SystemObject>();
		}
		else {
			tempObjects = applyFilter(systemObjects);
		}
		return Collections.unmodifiableCollection(tempObjects);
	}

	/**
	 * Die Systemobjekte werden in Abhängigkeit vom Kriterium gefiltert.
	 *
	 * @param systemObjects die zu filternden System-Objekte
	 *
	 * @return die gefilterten System-Objekte
	 */
	private Collection<SystemObject> applyFilter(Collection<SystemObject> systemObjects) {
		DataModel configuration = _connection.getDataModel();
		Collection<SystemObject> tempObjects = new HashSet<SystemObject>();
		if(_criteria.equals("Konfigurationsbereich")) {
			for(int i = 0; i < _values.length; i++) {
				String value = _values[i];
				List resultList;
				try {
					resultList = ConfigurationHelper.getObjects(value, configuration);
				}
				catch(IllegalArgumentException e) {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
					resultList = new ArrayList();
				}
				if(resultList.size() > 0) {
					if(resultList.size() > 1) _debug.finest("Anzahl der erstellten ObjekttypenObjekte: " + resultList.size());
					for(Iterator iterator = resultList.iterator(); iterator.hasNext();) {
						ConfigurationArea configurationArea = (ConfigurationArea)iterator.next();
						for(Iterator iterator1 = systemObjects.iterator(); iterator1.hasNext();) {
							SystemObject systemObject = (SystemObject)iterator1.next();
							ConfigurationArea confArea = systemObject.getConfigurationArea();
							if(confArea == configurationArea) {
								tempObjects.add(systemObject);
							}
						}
					}
				}
				else {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
				}
			}
		}
		else if(_criteria.equals("Objekttyp")) {
			for(int i = 0; i < _values.length; i++) {
				String value = _values[i];
				List resultList;
				try {   
					resultList = ConfigurationHelper.getObjects(value, configuration);
				}
				catch(IllegalArgumentException e) {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
					resultList = new ArrayList();
				}
				if(resultList.size() > 0) {
					if(resultList.size() > 1) _debug.finest("Anzahl der erstellten ObjekttypenObjekte: " + resultList.size());
					for(Iterator iterator2 = resultList.iterator(); iterator2.hasNext();) {
						SystemObjectType systemObjectType = (SystemObjectType)iterator2.next();
						for(Iterator iterator = systemObjects.iterator(); iterator.hasNext();) {
							SystemObject systemObject = (SystemObject)iterator.next();
							if(systemObject.isOfType(systemObjectType)) {
								tempObjects.add(systemObject);
							}
						}
					}
				}
				else {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
				}
			}
		}
		else if(_criteria.equals("Attributgruppe")) {
			// AttributGruppen für alle Values holen
			for(int i = 0; i < _values.length; i++) {
				String value = _values[i];
				List resultList;
				try {
					resultList = ConfigurationHelper.getObjects(value, configuration);
				}
				catch(IllegalArgumentException e) {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
					resultList = new ArrayList();
				}
				if(resultList.size() > 0) {
					if(resultList.size() > 1) _debug.finest("Anzahl der erstellten AttributgruppenObjekte: " + resultList.size());
					for(Iterator iterator2 = resultList.iterator(); iterator2.hasNext();) {
						AttributeGroup attributeGroup = (AttributeGroup)iterator2.next();
						for(Iterator iterator = systemObjects.iterator(); iterator.hasNext();) {
							SystemObject systemObject = (SystemObject)iterator.next();
							List atgs = systemObject.getType().getAttributeGroups();
							if(atgs.contains(attributeGroup)) {
								tempObjects.add(systemObject);
							}
						}
					}
				}
				else {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
				}
			}
		}
		else if(_criteria.equals("Aspekt")) {
			for(int i = 0; i < _values.length; i++) {
				String value = _values[i];
				List resultList;
				try {
					resultList = ConfigurationHelper.getObjects(value, configuration);
				}
				catch(IllegalArgumentException e) {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
					resultList = new ArrayList();
				}
				if(resultList.size() > 0) {
					if(resultList.size() > 1) _debug.finest("Anzahl der erstellten AspektObjekte: " + resultList.size());
					for(Iterator iterator = resultList.iterator(); iterator.hasNext();) {
						Aspect aspect = (Aspect)iterator.next();
						for(Iterator iterator2 = systemObjects.iterator(); iterator2.hasNext();) {
							SystemObject systemObject = (SystemObject)iterator2.next();
							List atgs = systemObject.getType().getAttributeGroups();
							for(Iterator iterator1 = atgs.iterator(); iterator1.hasNext();) {
								AttributeGroup attributeGroup = (AttributeGroup)iterator1.next();
								Collection aspects = attributeGroup.getAspects();
								if(aspects.contains(aspect)) {
									tempObjects.add(systemObject);
								}
							}
						}
					}
				}
				else {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
				}
			}
		}
		else if(_criteria.equals("Objekt")) {
			// Objekt holen zur Pid und dann alle durchlaufen und vergleichen
			for(int i = 0; i < _values.length; i++) {
				String value = _values[i];
				List resultList;
				try {
					resultList = ConfigurationHelper.getObjects(value, configuration);
				}
				catch(IllegalArgumentException e) {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
					resultList = new ArrayList();
				}
				if(resultList.size() > 0) {
					if(resultList.size() > 1) _debug.finest("Anzahl der erstellten Objekte: " + resultList.size());
					for(Iterator iterator = resultList.iterator(); iterator.hasNext();) {
						SystemObject systemObject = (SystemObject)iterator.next();
						if(systemObjects.contains(systemObject)) {
							tempObjects.add(systemObject);
						}
					}
				}
				else {
					_debug.warning("Zum Wert " + value + " kein passendes Objekt gefunden!");
				}
			}
		}
		else if(_criteria.equals("Erweitert")) {
			try {
				// Klassenname mit Pfad angeben z.B. sys.funclib.preselection.test.Filtertest
				String str = _values[0];
				Class c = Class.forName(str);
				ExtendedFilter extendedFilter = (ExtendedFilter)c.newInstance();
				extendedFilter.setValues(_values);
				extendedFilter.setConnection(_connection);
				tempObjects = extendedFilter.applyFilter(systemObjects);
			}
			catch(ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch(IllegalAccessException e) {
				e.printStackTrace();
			}
			catch(InstantiationException e) {
				e.printStackTrace();
			}
		}
		else {
			_debug.error("Dieses Kriterium '" + _criteria + "' wird nicht unterstützt. Verwenden Sie dafür einen Erweiterten Filter.");
			tempObjects = systemObjects;
		}
		return tempObjects;
	}
}
