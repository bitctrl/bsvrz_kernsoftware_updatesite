/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.util.Tuple;

import java.io.Serializable;
import java.util.*;

/**
 * Attributliste {@link PidScript#atlDefaults}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 8878 $ / $Date: 2011-03-09 15:36:58 +0100 (Wed, 09 Mar 2011) $ / ($Author: rs $)
 */
public class AtlDefaults implements Serializable {

	private static final long serialVersionUID = 0xa9e2daa96d53d9adL;

	private static final String MESSAGE_OLD_DATAMODEL =
			"Die im Skript verwendeten Aliase und ungebundenen Aspekte konnten nicht übertragen werden, dazu wird kb.tmVewProtokolleGlobal in Version 4 "
			               + "benötigt";

	private Map<SystemObjectType, SystemObject> _objects;

	private Map<String, SystemObjectType> _aliases;

	private Map<String, SystemObject[]> _pseudoObjects;

	private List<String> _aspects;

	private Map<String, Aspect> _aspectBindings;

	private List<Tuple<Long, Long>> _periods;

	private boolean _deltaProtocol;

	private static final Debug _debug = Debug.getLogger();

	public boolean isDeltaProtocol() {
		return _deltaProtocol;
	}

	/**
	 * Gibt die definierten Standardobjekte zurück
	 * @return Standardobjekte
	 */
	public Map<SystemObjectType, SystemObject> getObjects() {
		return _objects;
	}

	/**
	 * Gibt die im Script festgelegten Standardwerte der PseudoObjekte zurück
	 * @return Map mit Zuordnung Alias zu Objekten. Die Objekt-Arrays haben alle die gleiche Länge. Objekte sind null wenn unvollständige Pseudoobjekte genutzt werden.
	 */
	public Map<String, SystemObject[]> getPseudoObjects() {
		return _pseudoObjects;
	}

	/**
	 * Gibt die im Script standardmäßig festgelegten Aspekt-Bindungen zurück.
	 * @return Map mit Zuordnung Aspektbindung zu Aspekt. Der Aspekt kann null sein, wenn er nicht aufgelöst werden konnte.
	 */
	public Map<String, Aspect> getAspectBindings() {
		return _aspectBindings;
	}

	/**
	 * Gibt die im Script definierten Standardzeitbereiche zurück
	 * @return Liste mit Zeitbereichen (Millisekunden seit 1970)
	 */
	public List<Tuple<Long, Long>> getPeriods() {
		return _periods;
	}

	/**
	 * Gibt die Alias-Definitionen im Skript zurück
	 * @return Zuordnung Alias zu Objekttyp
	 */
	public Map<String, SystemObjectType> getAliases() {
		return _aliases;
	}

	/**
	 * Gibt die Aspekte im Skript zurück, die gebunden werden können bzw. müssen.
	 * @return Liste mit ungebundenen Aspektnamen.
	 */
	public List<String> getAspects() {
		return _aspects;
	}

	/**
	 * Erstellt ein leeres Objekt zur Übergabe als Parameter für {@link de.bsvrz.pua.prot.client.PuaClient#getDefaults}.
	 */
	public AtlDefaults() {
	}

	/**
	 * Konstruktor, der die Werte manuell setzt
	 */
	public AtlDefaults(
			final Map<SystemObjectType, SystemObject> objects,
			final Map<String, SystemObjectType> aliases,
			final Map<String, SystemObject[]> pseudoObjects,
			final List<String> aspects,
			final Map<String, Aspect> aspectBindings,
			final List<Tuple<Long, Long>> periods,
			final boolean isDeltaProtocol) {

		_objects = objects;
		_aliases = aliases;
		_pseudoObjects = pseudoObjects;
		_aspects = aspects;
		_aspectBindings = aspectBindings;
		_periods = periods;
		_deltaProtocol = isDeltaProtocol;
	}

	public static AtlDefaults createRaw(
			final Map<SystemObjectType, SystemObject> objects,
			final Map<String, String[]> pseudoObjects,
			final Map<String, String> aspectBindings,
			final List<Tuple<Long, Long>> periods,
			final boolean deltaProtocol,
			final Map<String, String> aliases,
			final List<String> aspects,
			final ObjectLookup model) {

		final Map<String, SystemObject[]> newPseudoObjects = new HashMap<String, SystemObject[]>(pseudoObjects.size());
		final Map<String, Aspect> newAspectBindings = new HashMap<String, Aspect>(aspectBindings.size());
		final Map<String, SystemObjectType> newAliases = new HashMap<String, SystemObjectType>(aliases.size());

		for(final Map.Entry<String, String[]> entry : pseudoObjects.entrySet()) {
			newPseudoObjects.put(entry.getKey(), stringsToSystemObjects(model, entry.getValue()));
		}

		for(final Map.Entry<String, String> entry : aspectBindings.entrySet()) {
			newAspectBindings.put(entry.getKey(), (Aspect)model.getObject(entry.getValue()));
		}

		for(final Map.Entry<String, String> entry : aliases.entrySet()) {
			newAliases.put(entry.getKey(), (SystemObjectType)model.getObject(entry.getValue()));
		}

		return new AtlDefaults(objects, newAliases, newPseudoObjects, aspects, newAspectBindings, periods, deltaProtocol);
	}

	private static SystemObject[] stringsToSystemObjects(final ObjectLookup model, final String[] value) {
		final SystemObject[] systemObjects = new SystemObject[value.length];
		for(int i = 0; i < value.length; i++) {
			final String pid = value[i];
			if(!"".equals(pid)) {
				systemObjects[i] = model.getObject(pid);
			}
		}
		return systemObjects;
	}

	/**
	 * Trägt die Werte dieses Objekts in das Daten-Objekt ein.
	 *
	 * @param data Ziel der Eintragung.
	 */
	public void build(final Data data) {
		final SystemObject[] systemObjects = _objects.values().toArray(new SystemObject[_objects.size()]);
		fillArray(systemObjects, data.getReferenceArray(PidScript.objects));
		int i = 0;
		Data.Array array = data.getArray(PidScript.pseudoObjects);
		array.setLength(_pseudoObjects.size());
		for(final Map.Entry<String, SystemObject[]> entry : _pseudoObjects.entrySet()) {
			final Data item = array.getItem(i);
			item.getTextValue("Alias").setText(entry.getKey());
			fillArray(entry.getValue(), item.getReferenceArray("Objekte"));
			i++;
		}
		i = 0;
		array = data.getArray(PidScript.aspectBindings);
		array.setLength(_aspectBindings.size());
		for(final Map.Entry<String, Aspect> entry : _aspectBindings.entrySet()) {
			final Data item = array.getItem(i);
			item.getTextValue("Alias").setText(entry.getKey());
			item.getReferenceValue("Aspekt").setSystemObject(entry.getValue());
			i++;
		}
		i = 0;
		array = data.getArray(PidScript.periods);
		array.setLength(_periods.size());
		for(final Tuple<Long, Long> entry : _periods) {
			final Data item = array.getItem(i);
			item.getTimeValue("Startzeit").setMillis(entry.first);
			item.getTimeValue("Endzeit").setMillis(entry.last);
			i++;
		}

		i = 0;
		try{
			array = data.getArray(PidScript.aliases);
			array.setLength(_aliases.size());
			for(final Map.Entry<String, SystemObjectType> entry : _aliases.entrySet()) {
				final Data item = array.getItem(i);
				item.getTextValue("Name").setText(entry.getKey());
				item.getReferenceValue("Objekttyp").setSystemObject(entry.getValue());
				i++;
			}
			i = 0;
			array = data.getArray(PidScript.aspects);
			array.setLength(_aspects.size());
			for(final String entry : _aspects) {
				final Data item = array.getItem(i);
				item.asTextValue().setText(entry);
				i++;
			}
		}
		catch(NoSuchElementException ignored){
			_debug.warning(MESSAGE_OLD_DATAMODEL);
		}
		data.getScaledValue(PidScript.protocolType).setText(_deltaProtocol ? "Änderungsprotokoll" : "Zustandsprotokoll");
	}

	private void fillArray(final SystemObject[] systemObjects, final Data.ReferenceArray array) {
		final int length = systemObjects.length;
		array.setLength(length);
		for(int i = 0; i < length; i++) {
			array.getReferenceValue(i).setSystemObject(systemObjects[i]);
		}
	}

	/**
	 * Erzeugt ein Java-Objekt aus den übergebenen Daten.
	 *
	 * @param data Daten.
	 *
	 * @return Java Objekt, in das die Werte aus <code>data</code> eingetragen sind.
	 */
	public static AtlDefaults getJavaObject(final Data data) {

		final SystemObject[] objectarray = data.getReferenceArray(PidScript.objects).getSystemObjectArray();
		final Map<SystemObjectType, SystemObject> objects = new HashMap<SystemObjectType, SystemObject>(objectarray.length);
		for(final SystemObject systemObject : objectarray) {
			objects.put(systemObject.getType(), systemObject);
		}

		final Data pseudoObjectsArray = data.getItem(PidScript.pseudoObjects);
		final Map<String, SystemObject[]> pseudoObjects = new HashMap<String, SystemObject[]>();
		for(Iterator iterator = pseudoObjectsArray.iterator(); iterator.hasNext();) {
			final Data d = (Data)iterator.next();
			pseudoObjects.put(
					d.getTextValue("Alias").getText(), d.getReferenceArray("Objekte").getSystemObjectArray()
			);
		}

		final Data aspectBindingsArray = data.getItem(PidScript.aspectBindings);
		final Map<String, Aspect> aspectBindings = new HashMap<String, Aspect>();
		for(Iterator iterator = aspectBindingsArray.iterator(); iterator.hasNext();) {
			final Data d = (Data)iterator.next();
			aspectBindings.put(d.getTextValue("Alias").getText(), (Aspect)d.getReferenceValue("Aspekt").getSystemObject());
		}


		final Data periodsArray = data.getItem(PidScript.periods);
		final List<Tuple<Long, Long>> periods = new ArrayList<Tuple<Long, Long>>();
		for(Iterator iterator = periodsArray.iterator(); iterator.hasNext();) {
			final Data d = (Data)iterator.next();
			periods.add(new Tuple<Long, Long>(d.getTimeValue("Startzeit").getMillis(), d.getTimeValue("Endzeit").getMillis()));
		}

		final Map<String, SystemObjectType> aliases = new HashMap<String, SystemObjectType>();
		final List<String> aspects = new ArrayList<String>();
		try{
			final Data aliasArray = data.getItem(PidScript.aliases);
			for(Iterator iterator = aliasArray.iterator(); iterator.hasNext();) {
				final Data d = (Data)iterator.next();
				aliases.put(d.getTextValue("Name").getText(), (SystemObjectType)d.getReferenceValue("Objekttyp").getSystemObject());
			}

			final Data aspectsArray = data.getItem(PidScript.aspects);
			for(Iterator iterator = aspectsArray.iterator(); iterator.hasNext();) {
				final Data d = (Data)iterator.next();
				aspects.add(d.asTextValue().getText());
			}
		}
		catch(IllegalArgumentException ignored){
			_debug.warning(MESSAGE_OLD_DATAMODEL);
		}

		return new AtlDefaults(
				objects, aliases, pseudoObjects, aspects, aspectBindings, periods, "Änderungsprotokoll".equals(data.getScaledValue(PidScript.protocolType)
						                                                                                              .getText())
		);
	}

	public void set(final AtlDefaults defaults) {
		_aspectBindings = defaults.getAspectBindings();
		_deltaProtocol = defaults.isDeltaProtocol();
		_objects = defaults.getObjects();
		_periods = defaults.getPeriods();
		_pseudoObjects = defaults.getPseudoObjects();
		_aliases = defaults.getAliases();
		_aspects = defaults.getAspects();
	}

	@Override
	public String toString() {
		return "AtlDefaults{" + "_objects=" + _objects + ", _aliases=" + _aliases + ", _pseudoObjects=" + _pseudoObjects + ", _aspects=" + _aspects
		       + ", _aspectBindings=" + _aspectBindings + ", _periods=" + _periods + ", _deltaProtocol=" + _deltaProtocol + '}';
	}
}
