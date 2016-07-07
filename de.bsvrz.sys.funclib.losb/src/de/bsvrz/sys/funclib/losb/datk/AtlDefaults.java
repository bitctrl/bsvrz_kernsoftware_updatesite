/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
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
import java.util.stream.Collectors;

/**
 * Attributliste {@link PidScript#atlDefaults}
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class AtlDefaults implements Serializable {

	private static final long serialVersionUID = 0xa9e2daa96d53d9adL;

	private static final String MESSAGE_OLD_DATAMODEL =
			"Die im Skript verwendeten Aliase und ungebundenen Aspekte konnten nicht übertragen werden, dazu wird kb.tmVewProtokolleGlobal in Version 4 "
			               + "benötigt";

	public static final String MESSAGE_NO_EVENT_PROTOCOL_ITEM =
			"Die verwendete Version von kb.tmVewProtokolleGlobal unterstützt das Eintragen der Protokollart 'Ereignisprotokoll' nicht und muss aktualisiert werden.";

	public static final String MESSAGE_NO_CELL_NO_CHANGE_MARKER =
			"Die verwendete Version von kb.tmVewProtokolleGlobal unterstützt das Eintragen der NoChange-Kennzeichung 'pro Zelle' nicht und muss aktualisiert werden.";

	private Map<SystemObjectType, SystemObject> _objects;

	private Map<String, SystemObjectType> _aliases;

	private Map<String, SystemObject[]> _pseudoObjects;

	private List<String> _aspects;

	private Map<String, Aspect> _aspectBindings;

	private List<Tuple<Long, Long>> _periods;

	private ProtocolType _protocolType;

	private NoChangeMarker _noChangeMarker;

	private static final Debug _debug = Debug.getLogger();

	/**
	 * Gibt die Protokollart zurück
	 * @return Protokollart
	 */
	public ProtocolType getProtocolType() {
		return _protocolType;
	}

	/**
	 * Gibt die Art der Markierung von "Keine Änderung"-Datensätzen zurück. Bei statusprotokollen können
	 * Keine-Änderung-Informationen entweder pro Zeiel übertragen werden, oder pro Datensatz.
	 * @return die Art der Markierung von "Keine Änderung"-Datensätzen
	 */
	public NoChangeMarker getNoChangeMarker() {
		return _noChangeMarker;
	}

	/**
	 * Gibt zurück ob es sich um ein Änderungsprotokoll handelt
	 * @deprecated {@link #getProtocolType()} unterstützt die Abfrage nach allen Protokollarten
	 */
	@Deprecated
	public boolean isDeltaProtocol() {
		return _protocolType == ProtocolType.DeltaProtocol;
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
			final ProtocolType protocolType,
			final NoChangeMarker noChangeMarker) {

		_objects = objects;
		_aliases = aliases;
		_pseudoObjects = pseudoObjects;
		_aspects = aspects;
		_aspectBindings = aspectBindings;
		_periods = periods;
		_protocolType = protocolType;
		_noChangeMarker = noChangeMarker;
	}

	public static AtlDefaults createRaw(
			final Map<SystemObjectType, SystemObject> objects,
			final Map<String, String[]> pseudoObjects,
			final Map<String, String> aspectBindings,
			final List<Tuple<Long, Long>> periods,
			final ProtocolType protocolType,
			final Map<String, String> aliases,
			final List<String> aspects,
			final ObjectLookup model,
			final NoChangeMarker noChangeMarker) {

		final Map<String, SystemObject[]> newPseudoObjects = new LinkedHashMap<String, SystemObject[]>(pseudoObjects.size());
		final Map<String, Aspect> newAspectBindings = new LinkedHashMap<String, Aspect>(aspectBindings.size());
		final Map<String, SystemObjectType> newAliases = new LinkedHashMap<String, SystemObjectType>(aliases.size());

		for(final Map.Entry<String, String[]> entry : pseudoObjects.entrySet()) {
			newPseudoObjects.put(entry.getKey(), stringsToSystemObjects(model, entry.getValue()));
		}

		for(final Map.Entry<String, String> entry : aspectBindings.entrySet()) {
			newAspectBindings.put(entry.getKey(), (Aspect)model.getObject(entry.getValue()));
		}

		for(final Map.Entry<String, String> entry : aliases.entrySet()) {
			newAliases.put(entry.getKey(), (SystemObjectType)model.getObject(entry.getValue()));
		}

		return new AtlDefaults(objects, newAliases, newPseudoObjects, aspects, newAspectBindings, periods, protocolType, noChangeMarker);
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
		data.getReferenceArray(PidScript.objects).set(systemObjects);
		int i = 0;
		Data.Array array = data.getArray(PidScript.pseudoObjects);
		array.setLength(_pseudoObjects.size());
		for(final Map.Entry<String, SystemObject[]> entry : _pseudoObjects.entrySet()) {
			final Data item = array.getItem(i);
			item.getTextValue("Alias").setText(entry.getKey());
			item.getReferenceArray("Objekte").set(entry.getValue());
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
		String typeText = "Zustandsprotokoll";
		switch(_protocolType){
			case StatusProtocol:
				typeText = "Zustandsprotokoll";
				break;
			case DeltaProtocol:
				typeText = "Änderungsprotokoll";
				break;
			case EventProtocol:
				typeText = "Ereignisprotokoll";
				break;
		}
		String markerText = "pro Zeile";
		switch(_noChangeMarker){
			case Row:
				markerText = "pro Zeile";
				break;
			case Cell:
				markerText = "pro Zelle";
				break;
		}
		try {
			data.getScaledValue(PidScript.protocolType).setText(typeText);
		}
		catch(Exception e){
			if(_protocolType == ProtocolType.EventProtocol) {
				throw new UnsupportedOperationException(MESSAGE_NO_EVENT_PROTOCOL_ITEM, e);
			}
		}
		try {
			data.getScaledValue(PidScript.noChangeMarker).setText(markerText);
		}
		catch(Exception e){
			if(_noChangeMarker == NoChangeMarker.Cell) {
				throw new UnsupportedOperationException(MESSAGE_NO_CELL_NO_CHANGE_MARKER, e);
			}
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
		final Map<SystemObjectType, SystemObject> objects = new LinkedHashMap<SystemObjectType, SystemObject>(objectarray.length);
		for(final SystemObject systemObject : objectarray) {
			objects.put(systemObject.getType(), systemObject);
		}

		final Data pseudoObjectsArray = data.getItem(PidScript.pseudoObjects);
		final Map<String, SystemObject[]> pseudoObjects = new LinkedHashMap<String, SystemObject[]>();
		for(Iterator iterator = pseudoObjectsArray.iterator(); iterator.hasNext();) {
			final Data d = (Data)iterator.next();
			pseudoObjects.put(
					d.getTextValue("Alias").getText(), d.getReferenceArray("Objekte").getSystemObjectArray()
			);
		}

		final Data aspectBindingsArray = data.getItem(PidScript.aspectBindings);
		final Map<String, Aspect> aspectBindings = new LinkedHashMap<String, Aspect>();
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

		final Map<String, SystemObjectType> aliases = new LinkedHashMap<String, SystemObjectType>();
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

		ProtocolType protocolType = ProtocolType.Undefined;
		String text = data.getScaledValue(PidScript.protocolType).getText();
		if("Zustandsprotokoll".equals(text)){
			protocolType = ProtocolType.StatusProtocol;
		}
		else if("Änderungsprotokoll".equals(text)){
			protocolType = ProtocolType.DeltaProtocol;
		}
		else if("Ereignisprotokoll".equals(text)){
			protocolType = ProtocolType.EventProtocol;
		}


		NoChangeMarker noChangeMarker = NoChangeMarker.Row;
		try {
			if(data.getItem(PidScript.noChangeMarker).asTextValue().getValueText().equals("pro Zelle")) {
				noChangeMarker = NoChangeMarker.Cell;
			}
		}
		catch(IllegalArgumentException ignored){
			_debug.warning(MESSAGE_NO_CELL_NO_CHANGE_MARKER);
		}

		return new AtlDefaults(
				objects, aliases, pseudoObjects, aspects, aspectBindings, periods, protocolType, noChangeMarker
		);
	}

	public void set(final AtlDefaults defaults) {
		_aspectBindings = defaults.getAspectBindings();
		_protocolType = defaults.getProtocolType();
		_objects = defaults.getObjects();
		_periods = defaults.getPeriods();
		_pseudoObjects = defaults.getPseudoObjects();
		_aliases = defaults.getAliases();
		_aspects = defaults.getAspects();
		_noChangeMarker = defaults.getNoChangeMarker();
	}

	@Override
	public String toString() {
		return "AtlDefaults{" +
				"_objects=" + _objects +
				", _aliases=" + _aliases +
				", _pseudoObjects=" + _pseudoObjects.entrySet().stream().map(e -> "{" + e.getKey() + "=" + Arrays.toString(e.getValue()) + "}").collect(Collectors.toList()) + 
				", _aspects=" + _aspects +
				", _aspectBindings=" + _aspectBindings +
				", _periods=" + _periods +
				", _protocolType=" + _protocolType +
				", _noChangeMarker=" + _noChangeMarker +
				'}';
	}
}
