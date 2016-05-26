/*
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.consistencycheck.RelaxedModelChanges;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications.CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET;

/**
 * Implementierung des Interfaces {@link de.bsvrz.dav.daf.main.config.NonMutableSet} für nicht veränderbare Mengen auf Seiten der Konfiguration.
 *
 * @author Stephan Homeyer (sth), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigNonMutableSet extends ConfigObjectSet implements NonMutableSet {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** enthält alle aktuellen Elemente dieser Menge - diese Menge kann sich nur durch Versionswechsel, also Neustart der Konfiguration ändern! */
//	private List<SystemObject> _elements;

	/**
	 * Konstruktor für eine Konfigurationsmenge.
	 *
	 * @param configurationArea Konfigurationsbereich dieser Menge
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigNonMutableSet(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public List<SystemObject> getElementsInModifiableVersion() {
		return Collections.unmodifiableList(getElementsInVersion(getConfigurationArea().getModifiableVersion()));
	}

	public List<SystemObject> getElementsInVersion(short version) {
		final List<SystemObject> elements = new ArrayList<SystemObject>();
		// alle Elemente überprüfen, wann sie gültig bzw. ungültig werden
		for(NonMutableElement nonMutableElement : getNonMutableElements()) {
			if(nonMutableElement.getFromVersion() <= version && (nonMutableElement.getToVersion() > version || nonMutableElement.getToVersion() == 0)) {
				// das Element gehört mindestens seit dieser Version dazu und
				// das Element ist auch noch in der angegebenen Version gültig
				// (d.h. wird erst in einer nachfolgenden Version aus der Menge entfernt bzw. wurde noch nie entfernt und soll auch noch nicht entfernt werden.)
				final long elementId = nonMutableElement.getElementId();
				final SystemObject element = getDataModel().getObject(elementId);
				if(element != null) {
					elements.add(element);
				}
			}
		}
		// alle Elemente, die in der nächsten Version gültig sind werden zurückgegeben
		return Collections.unmodifiableList(elements);
	}

	public List<SystemObject> getElementsInAllVersions(short fromVersion, short toVersion) {
		final List<SystemObject> elements = new ArrayList<SystemObject>();
		// alle Elemente, die zur Version fromVersion bis zur Version toVersion gültig waren
		for(NonMutableElement nonMutableElement : getNonMutableElements()) {
			if(nonMutableElement.getFromVersion() <= fromVersion && (nonMutableElement.getToVersion() > toVersion || nonMutableElement.getToVersion() == 0)) {
				final long elementId = nonMutableElement.getElementId();
				final SystemObject element = getDataModel().getObject(elementId);
				if(element != null) {
					elements.add(element);
				}
			}
		}
		return Collections.unmodifiableList(elements);
	}

	public List<SystemObject> getElementsInAnyVersions(short fromVersion, short toVersion) {
		final List<SystemObject> elements = new ArrayList<SystemObject>();
		// alle Elemente, die mindestens während einer der Versionen zwischen fromVersion und toVersion gültig waren
		for(NonMutableElement nonMutableElement : getNonMutableElements()) {
			if(nonMutableElement.getFromVersion() <= toVersion && (nonMutableElement.getToVersion() > fromVersion || nonMutableElement.getToVersion() == 0)) {
				final long elementId = nonMutableElement.getElementId();
				final SystemObject element = getDataModel().getObject(elementId);
				if(element != null) {
					elements.add(element);
				}
			}
		}
		return Collections.unmodifiableList(elements);
	}

	public List<SystemObject> getElements() {
		// die Elemente dürfen nicht gecached werden, da sonst neu hinzugefügte Elemente nicht erkannt und zurückgegeben werden.
		return Collections.unmodifiableList(getElementsInVersion(getConfigurationArea().getActiveVersion()));
	}

	public List<SystemObject> getElements(long time) {
		// ermitteln, welche Version dieses Konfigurationsbereichs zum angegebenen Zeitpunkt gültig ist
		short version = getConfigurationArea().getVersionAtAssignedTime(time);
		return Collections.unmodifiableList(getElementsInVersion(version));
	}

	public List<SystemObject> getElementsInPeriod(long startTime, long endTime) {
		// Versionsnummern ermitteln
		short fromVersion = getConfigurationArea().getVersionAtAssignedTime(startTime);
		short toVersion = getConfigurationArea().getVersionAtAssignedTime(endTime);
		// Elemente ermitteln und zurückgeben
		return Collections.unmodifiableList(getElementsInAnyVersions(fromVersion, toVersion));
	}

	public List<SystemObject> getElementsDuringPeriod(long startTime, long endTime) {
		short fromVersion = getConfigurationArea().getVersionAtAssignedTime(startTime);
		short toVersion = getConfigurationArea().getVersionAtAssignedTime(endTime);
		// Elemente ermitteln und zurückgeben
		return Collections.unmodifiableList(getElementsInAllVersions(fromVersion, toVersion));
	}

	public void add(SystemObject[] objects) throws ConfigurationChangeException {
		if(checkChangePermit()) {
			// wenn die Menge bereits aktiviert oder freigegeben wurde, dann darf nichts mehr geändert werden, es sei denn
			// die Referenzierungsart ist "Gerichtete Assoziation", dann darf in der in Bearbeitung befindlichen Version Elemente hinzugefügt werden
			if(!(getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION)
					&& !RelaxedModelChanges.getInstance(getDataModel()).allowObjectSetAdd(this)) {
				// also Komposition und Aggregation
				if(getValidSince() < getConfigurationArea().getModifiableVersion()) {
					// die Menge wurde bereits aktiviert
					throw new ConfigurationChangeException(
							"Die Menge " + getNameOrPidOrId() + " darf nicht mehr verändert werden, "
							+ "da sie bereits aktiviert oder zur Übernahme / Aktivierung freigegeben wurde und die Referenzierungsart "
							+ getObjectSetType().getReferenceType() + " lautet."
					);
				}
			}

			// Die Menge hat als Referenzierungsart "Assoziation" oder wurde gerade erst erstellt.
			// ermitteln, welche Elemente sich in der in Bearbeitung befindlichen Version befinden
			final Set<SystemObject> elementsInModifiableVersion = new HashSet<SystemObject>(getElementsInModifiableVersion());

			// Typ-Überprüfung der Elemente wird nur in der Konsistenzprüfung vorgenommen, da hier die zu betrachtende Version des Mengen-Typs nicht bekannt ist.
			// ermitteln, welche Typen in der Menge zugelassen sind
//			final Set<SystemObjectType> objectTypes = new HashSet<SystemObjectType>();
//			final List<SystemObject> objectTypesInModifiableVersion = getObjectSetType().getNonMutableSet("ObjektTypen").getElementsInModifiableVersion();
//			for(SystemObject systemObject : objectTypesInModifiableVersion) {
//				final SystemObjectType objType = (SystemObjectType)systemObject;
//				objectTypes.add(objType);
//			}

			// folgende Menge enthält die Elemente, die tatsächlich hinzugefügt werden
			final Set<SystemObject> addSet = new LinkedHashSet<SystemObject>();

			// hinzuzufügende Elemente werden überprüft
			for(SystemObject systemObject : objects) {
				// Typ-Überprüfung
//				if(isTypeOfObjectAllowed(systemObject.getType(), objectTypes)) {
				// prüfen, ob das Objekt bereits in der Menge ist
				if(!elementsInModifiableVersion.contains(systemObject)) {
					addSet.add(systemObject);
				}
//				}
//				else {
//					throw new ConfigurationChangeException(
//							"Der Typ " + systemObject.getType().getNameOrPidOrId() + " des Objekts " + systemObject.getNameOrPidOrId()
//							+ " ist in dieser Konfigurationsmenge " + getNameOrPidOrId() + " nicht erlaubt."
//					);
//				}
			}

			// hinzufügen der Elemente zu der Menge
			if(!addSet.isEmpty()) {
				setConfigurationData(addSet.toArray(new SystemObject[addSet.size()]), null, getConfigurationArea().getModifiableVersion());
			}
		}
		else {
			throw new ConfigurationChangeException(
					"Es liegt keine Berechtigung zum Verändern dieser Menge '" + getNameOrPidOrId() + "' vor."
					+ " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
					+ "' zuständig."
			);
		}
	}

//	/**
//	 * Prüft, ob der angegebene Typ in der Menge der erlaubten Typen vorkommt, oder einen dieser Typen erweitert.
//	 *
//	 * @param type        zu überprüfender Typ
//	 * @param objectTypes Menge der erlaubten Typen
//	 *
//	 * @return <code>true</code>, falls der angegebene Typ in der Menge der erlaubten Typen vorkommt, oder einen dieser Typen erweitert, sonst <code>false</code>
//	 */
//	private boolean isTypeOfObjectAllowed(SystemObjectType type, Set<SystemObjectType> objectTypes) {
//		if(objectTypes.contains(type)) {
//			return true;
//		}
//		else {
//			for(SystemObjectType systemObjectType : objectTypes) {
//				if(type.inheritsFrom(systemObjectType)) {
//					// da dieser Typ von einem der anderen abgeleitet ist, kann dieser der Menge der erlaubten Typen hinzugefügt werden,
//					// dies hat den Vorteil, dass bei einem weiteren Element die contains-Abfrage direkt zuschlägt
//					objectTypes.add(type);
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	public void remove(SystemObject[] objects) throws ConfigurationChangeException {
		if(checkChangePermit()) {
			// wenn die Menge bereits aktiviert wurde, dann darf nichts mehr geändert werden, es sei denn
			// die Referenzierungsart ist "Gerichtete Assoziation", dann darf in der in Bearbeitung befindlichen Version Elemente entfernt werden
			if(!(getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION)) {
				// also Komposition und Aggregation
				if(getValidSince() < getConfigurationArea().getModifiableVersion()) {
					// die Menge wurde bereits aktiviert
					throw new ConfigurationChangeException(
							"Die Menge " + getNameOrPidOrId() + " darf nicht mehr verändert werden, "
							+ "da sie bereits aktiviert oder zur Übernahme / Aktivierung freigegeben wurde und die Referenzierungsart "
							+ getObjectSetType().getReferenceType() + " lautet."
					);
				}
			}

			// die Menge hat als Referenzierungsart "Assoziation" oder wurde gerade erst erstellt
			// ermitteln, welche Element sich in der in Bearbeitung befindlichen Version befinden
			final Set<SystemObject> elementsInModifiableVersion = new HashSet<SystemObject>(getElementsInModifiableVersion());

			// folgende Menge enthält die Elemente, die tatsächlich entfernt werden
			final Set<SystemObject> removeSet = new HashSet<SystemObject>();

			// zu entfernende Elemente werden überprüft
			for(SystemObject systemObject : objects) {
				// prüfen, ob die Elemente überhaupt in der Menge sind
				if(elementsInModifiableVersion.contains(systemObject)) {
					removeSet.add(systemObject);
				}
			}

			// entfernen der Elemente aus der Menge
			if(!removeSet.isEmpty()) {
				setConfigurationData(null, removeSet.toArray(new SystemObject[removeSet.size()]), getConfigurationArea().getModifiableVersion());
			}
		}
		else {
			throw new ConfigurationChangeException(
					"Es liegt keine Berechtigung zum Verändern dieser Menge '" + getNameOrPidOrId() + "' vor."
					+ " Der Verantwortliche der Konfiguration ist nicht für den Konfigurationsbereich '" + getConfigurationArea().getNameOrPidOrId()
					+ "' zuständig."
			);
		}
	}

	/**
	 * Der konfigurierende Datensatz mit den Elementen wird aktualisiert.
	 *
	 * @param addedElements   hinzugefügte Elemente oder <code>null</code>, falls es keine Elemente zum Hinzufügen gab
	 * @param removedElements entfernte Elemente oder <code>null</code>, falls es keine Elemente zum Entfernen gab
	 * @param changeVersion   die Version, in der die Änderung stattfindet
	 *
	 * @throws ConfigurationChangeException Falls der Datensatz nicht geschrieben werden konnte.
	 */
	private void setConfigurationData(SystemObject[] addedElements, SystemObject[] removedElements, short changeVersion) throws ConfigurationChangeException {
		// alle Elemente aus der Menge holen einschließlich der nicht aktuellen (versionierten)
		final List<NonMutableElement> nonMutableElements = getNonMutableElements();
		// Elementliste auf den neuesten Stand bringen
		if(addedElements != null) {
			// neue Elemente hinzufügen
			for(SystemObject systemObject : addedElements) {
				nonMutableElements.add(new NonMutableElement(systemObject.getId(), changeVersion, (short)0));
			}
		}
		if(removedElements != null) {
			// Elemente löschen
			for(SystemObject systemObject : removedElements) {
				// richtige Element finden
				for(NonMutableElement nonMutableElement : nonMutableElements) {
					// da Elemente mehrfach in die Menge eingefügt und entfernt werden können, muss auf toVersion == 0 abgefragt werden
					if(nonMutableElement.getElementId()==systemObject.getId() && nonMutableElement.getToVersion() == 0) {
						nonMutableElement.setToVersion(changeVersion);
					}
				}
			}
		}
		// Liste in ein Byte-Array packen und abspeichern
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
			if(getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION) {
				// lose Kopplung, hier muss versioniert abgespeichert werden
				for(NonMutableElement nonMutableElement : nonMutableElements) {
					serializer.writeLong(nonMutableElement.getElementId());
					serializer.writeShort(nonMutableElement.getFromVersion());
					serializer.writeShort(nonMutableElement.getToVersion());
				}
			}
			else {
				// starke Kopplung (nur die aktuellen Elemente werden gespeichert)
				for(NonMutableElement nonMutableElement : nonMutableElements) {
					if(nonMutableElement.getToVersion() == 0) {
						serializer.writeLong(nonMutableElement.getElementId());
					}
				}
			}
			_systemObjectInfo.setConfigurationData(CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET, out.toByteArray());
			getConfigurationArea().setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der konfigurierende Datensatz mit den Elementen der Menge " + getNameOrPidOrId() + " konnte nicht geschrieben werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	public void removeNullElements() throws ConfigurationChangeException {
		// alle Elemente aus der Menge holen einschließlich der nicht aktuellen (versionierten)
		final List<NonMutableElement> nonMutableElements = getNonMutableElements();
		for(Iterator<NonMutableElement> iterator = nonMutableElements.iterator(); iterator.hasNext();) {
			final NonMutableElement nonMutableElement = iterator.next();
			if(getDataModel().getObject(nonMutableElement.getElementId()) == null){
				// Wenn das Objekt nicht existiert, das Objekt aus der Liste entfernen
				iterator.remove();
			}
		}

		// Liste in ein Byte-Array packen und abspeichern
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final Serializer serializer = SerializingFactory.createSerializer(getSerializerVersion(), out);
			if(getObjectSetType().getReferenceType() == ReferenceType.ASSOCIATION) {
				// lose Kopplung, hier muss versioniert abgespeichert werden
				for(NonMutableElement nonMutableElement : nonMutableElements) {
					serializer.writeLong(nonMutableElement.getElementId());
					serializer.writeShort(nonMutableElement.getFromVersion());
					serializer.writeShort(nonMutableElement.getToVersion());
				}
			}
			else {
				// starke Kopplung (nur die aktuellen Elemente werden gespeichert)
				for(NonMutableElement nonMutableElement : nonMutableElements) {
					if(nonMutableElement.getToVersion() == 0) {
						serializer.writeLong(nonMutableElement.getElementId());
					}
				}
			}
			_systemObjectInfo.setConfigurationData(CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET, out.toByteArray());
			getConfigurationArea().setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.ConfigurationData);
			out.close();
		}
		catch(Exception ex) {
			final String errorMessage = "Der konfigurierende Datensatz mit den Elementen der Menge " + getNameOrPidOrId() + " konnte nicht geschrieben werden";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	/**
	 * Gibt zurück, ob es an der Menge eine Änderung in der in Bearbeitung befindlichen Version gibt.
	 *
	 * @param modifiableVersion in Bearbeitung befindliche Version dieses Bereichs
	 *
	 * @return <code>true</code>, wenn es eine Änderung gab, sonst <code>false</code>
	 */
	public boolean isSetChanged(short modifiableVersion) {
		final List<NonMutableElement> nonMutableElements = getNonMutableElements();
		for(NonMutableElement nonMutableElement : nonMutableElements) {
			final short fromVersion = nonMutableElement.getFromVersion();
			final short toVersion = nonMutableElement.getToVersion();
			// wurde ein Element hinzugefügt ?
			if(fromVersion == modifiableVersion) return true;
			// wurde ein Element entfernt ?
			if(toVersion == modifiableVersion) return true;
		}
		return false;
	}

	/**
	 * Diese Methode liest den konfigurierenden Datensatz für die Elemente dieser Menge ein und gibt sie in einer Liste zurück.
	 *
	 * @return eine Liste von Elementen mit Versionsnummern, die die Zugehörigkeitszeiträume kennzeichnen
	 */
	private synchronized List<NonMutableElement> getNonMutableElements() {
		final List<NonMutableElement> nonMutableElements = new ArrayList<NonMutableElement>();
		try {
			byte[] bytes = _systemObjectInfo.getConfigurationData(CONFIGURATION_ELEMENTS_IN_NON_MUTABLE_SET);  // feste ID für die Attributgruppenverwendung um die Elemente einer Menge zu erhalten
			final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			final Deserializer deserializer = SerializingFactory.createDeserializer(getSerializerVersion(), in);
			ReferenceType referenceType;
			if(("menge.attributgruppenVerwendungen").equals(getObjectSetType().getPid())) {
				// Sonderbehandlung für die Attributgruppenverwendungen
				// (um an die Referenzierungsart zu gelangen benötigt man die Attributgruppenverwendung und umgekehrt)
				referenceType = ReferenceType.ASSOCIATION;
			}
			else {
				referenceType = getObjectSetType().getReferenceType();
			}

			if(referenceType == ReferenceType.ASSOCIATION) {
				// lose Kopplung (versioniert)
				assert bytes.length % 12 == 0 : "Format des Byte-Arrays für die Elemente einer Menge " + getNameOrPidOrId()
				                                + " hat sich geändert. Länge muss durch 12 teilbar sein.";
				int numberOfElements = bytes.length / 12;
				for(int i = 0; i < numberOfElements; i++) {
					long id = deserializer.readLong();
					short fromVersion = deserializer.readShort();
					short toVersion = deserializer.readShort();
					nonMutableElements.add(new NonMutableElement(id, fromVersion, toVersion));
				}
			}
			else {
				// starke Kopplung (Aggregation, Komposition)
				assert bytes.length % 8 == 0 : "Format des Byte-Arrays für die Elemente einer Menge " + getNameOrPidOrId()
				                               + " hat sich geändert. Länge muss durch 8 teilbar sein.";
				int numberOfElements = bytes.length / 8;	// ein Eintrag ist 8 Byte lang (Objekt-ID)
				for(int i = 0; i < numberOfElements; i++) {
					long id = deserializer.readLong();
					nonMutableElements.add(new NonMutableElement(id, (short)0, (short)0));
				}
			}
			in.close();
		}
		catch(IllegalArgumentException ex) {
			final String errorMessage =
					"Elemente der Konfigurationsmenge '" + getNameOrPidOrId() + "' konnten nicht ermittelt werden (evtl. wurde die Menge neu angelegt)";
			_debug.finest(errorMessage, ex.getMessage());
		}
		catch(Exception ex) {
			final String errorMessage = "Elemente der Konfigurationsmenge " + getNameOrPidOrId() + " konnten nicht ermittelt werden";
			_debug.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		return nonMutableElements;
	}

	/**
	 * Repräsentiert ein Element der Menge mit einer Versionsnummer, ab der das Element zur Menge gehört und einer Versionsnummer, ab der das Element nicht mehr
	 * zur Menge gehört.
	 */
	private class NonMutableElement {

		/** ein Element dieser Menge */
		private SystemObject _element;

		/** Objekt-ID des Elements */
		private long _elementId;

		/** Version, seit der das Element zur Menge gehört */
		private short _fromVersion;

		/** Version, seit der das Element nicht mehr zur Menge gehört */
		private short _toVersion;

		/**
		 * Erzeugt einen Elementeintrag einer nicht-dynamischen Menge.
		 *
		 * @param elementId   Objekt-ID des Elements
		 * @param fromVersion Version, seit der das Element zur Menge gehört
		 * @param toVersion   Version, seit der das Element nicht mehr zur Menge gehört
		 */
		private NonMutableElement(long elementId, short fromVersion, short toVersion) {
			_elementId = elementId;
			_fromVersion = fromVersion;
			_toVersion = toVersion;
		}

		/**
		 * Gibt die Version zurück, seit der das Element zur Menge gehört.
		 *
		 * @return Version, seit der das Element zur Menge gehört
		 */
		public short getFromVersion() {
			// fromVersion <= aktuelle Version -> es wurde bereits aktiviert
			return _fromVersion;
		}

		/**
		 * Gibt die Version zurück, seit der das Element nicht mehr zur Menge gehört.
		 *
		 * @return Version, seit der das Element nicht mehr zur Menge gehört oder <code>0</code>, falls das Objekt noch aktuell ist.
		 */
		public short getToVersion() {
			// toVersion == 0 -> es wurde bisher nicht aus der Menge genommen
			return _toVersion;
		}

		/**
		 * Setzt die Version, ab der das Element nicht mehr zur Menge gehört.
		 *
		 * @param toVersion Version, ab der das Element nicht mehr zur Menge gehört
		 */
		public void setToVersion(short toVersion) {
			_toVersion = toVersion;
		}

		/**
		 * Bestimmt die Objekt-ID des Mengenelements
		 * @return Objekt-ID des Elements
		 */
		public long getElementId() {
			return _elementId;
		}
	}
}
