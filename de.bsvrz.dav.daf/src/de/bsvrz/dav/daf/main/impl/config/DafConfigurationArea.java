/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Applikationsseitige Implementierung der Schnittstelle zum Zugriff auf die Eigenschaften eines Bereichs.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8005 $
 */
public class DafConfigurationArea extends DafConfigurationObject implements ConfigurationArea {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Aktive Version des Bereichs oder -1 falls diese Version noch nicht abgefragt wurde. * */
	private short _activeVersion = -1;

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafConfigurationArea(DafDataModel dataModel) {
		super(dataModel);
		_internType = CONFIGURATION_AREA;
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafConfigurationArea(
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
			long setIds[]
	) {
		super(
				id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds
		);
		_internType = CONFIGURATION_AREA;
	}

	public ConfigurationAuthority getConfigurationAuthority() {
		AttributeGroup atg = getDataModel().getAttributeGroup("atg.konfigurationsBereichEigenschaften");
		Data.ReferenceValue reference = getConfigurationData(atg).getReferenceValue("zust�ndiger");
		return (ConfigurationAuthority)reference.getSystemObject();
	}

	/**
	 * Diese Methode fordert den konfigurierenden Datensatz des Bereichs mit der ATG "atg.konfigurationsBereich�bernahmeInformationen" und dem Aspekt
	 * "asp.eigenschaften" an, aus diesem wird dann eine Version ausgelesen. Die auszulesende Version wird �ber den �bergebenen Parameter festgelegt.
	 *
	 * @param kindOfVersion Version, die aus dem Datensatz gelesen werden soll ("�bernehmbareVersion" oder "aktivierbareVersion")
	 *
	 * @return Version
	 *
	 * @throws IllegalStateException Die Version konnte nicht ermittelt werden
	 */
	private short getVersion(String kindOfVersion) throws IllegalStateException {
		final Data data = getConfigurationData(
				getDataModel().getAttributeGroup("atg.konfigurationsBereich�bernahmeInformationen"), getDataModel().getAspect("asp.eigenschaften")
		);

		if(data != null) {
			return data.getUnscaledValue(kindOfVersion).shortValue();
		}
		else {
			throw new IllegalStateException(
					"Die " + kindOfVersion + " Version des Konfigurationsbereichs " + getNameOrPidOrId() + " konnte nicht ermittelt werden."
			);
		}
	}

	public short getActivatableVersion() {
		return getVersion("aktivierbareVersion");
	}

	public short getTransferableVersion() {
		return getVersion("�bernehmbareVersion");
	}

	public short getModifiableVersion() {
		try {
			return _dataModel.getRequester().getModifiableVersion(this);
		}
		catch(RequestException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public short getActiveVersion() {
		if(_activeVersion == -1) {
			// Version wird beim ersten Aufruf abgefragt und f�r weitere Aufrufe gespeichert.
			_activeVersion = getDataModel().getActiveVersion(this);
		}
		return _activeVersion;
	}

	/**
	 * Gibt einen Zeitpunkt zur�ck, dieser bezieht sich auf den �bergebenen Parameter. Der Zeitpunkt wird aus einem konfigurierenden Datensatz ausgelesen
	 * (atg.konfigurationsBereich�nderungsZeiten), dieser Datensatz ist am Konfigurationsbereich gespeichert.
	 *
	 * @param kindOfTime "Letzte�nderungszeitDynamischesObjekt", "Letzte�nderungszeitKonfigurationsObjekt" oder "Letzte�nderungszeitDatensatz"
	 *
	 * @return Zeitpunkt, der zu dem �bergebenen Parameter geh�rt
	 *
	 * @throws IllegalStateException Der �bergebene Parameter ist unbekannt
	 */
	private long getTime(String kindOfTime) throws IllegalStateException {
		Data data = getConfigurationData(getDataModel().getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten"));
		if(data != null) {
			return data.getTimeValue(kindOfTime).getMillis();
		}
		else {
			return 0;
		}
	}

	public long getTimeOfLastDynamicChange() {
		return getTime("Letzte�nderungszeitDynamischesObjekt");
	}

	public long getTimeOfLastNonActiveConfigurationChange() {
		return getTime("Letzte�nderungszeitKonfigurationsObjekt");
	}

	public long getTimeOfLastActiveConfigurationChange() {
		return getTime("Letzte�nderungszeitDatensatz");
	}

	public Collection<SystemObject> getObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification) {
		final DafDataModel dataModel = (DafDataModel)getDataModel();
		Collection<ConfigurationArea> oneArea = new ArrayList<ConfigurationArea>();
		oneArea.add(this);
		return dataModel.getObjects(oneArea, systemObjectTypes, timeSpecification);
	}

	public Collection<SystemObject> getDirectObjects(Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification timeSpecification) {
		try {
			return getRequester().getDirectObjects(this, systemObjectTypes, timeSpecification);
		}
		catch(RequestException e) {
			e.printStackTrace();
			_debug.error("Fehler bei der Abfrage der Objekte des Bereichs", e);
			throw new RuntimeException("Fehler bei der �bertragung", e);
		}
	}

	public Collection<SystemObject> getCurrentObjects() {
		Collection<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>();
		configurationAreas.add(getConfigurationArea());
		return getDataModel().getObjects(configurationAreas, null, ObjectTimeSpecification.valid());
	}

	public Collection<SystemObject> getNewObjects() {
		final ConfigurationRequester requester = getRequester();
		try {
			return requester.getNewObjects(this);
		}
		catch(RequestException e) {
			e.printStackTrace();
			_debug.error("Fehler bei der Abfrage der noch nicht aktivierten Objekte des Bereichs", e);
			throw new RuntimeException("Fehler bei der �bertragung", e);
		}
	}

	/**
	 * Pr�ft, ob ein Name l�nger als 255 Zeichen ist.
	 *
	 * @param name Name oder <code>null</code>
	 *
	 * @throws ConfigurationChangeException Der Name ist l�nger als 255 Zeichen
	 */
	private void checkNameLength(final String name) throws ConfigurationChangeException {
		if(name != null) {
			if(name.length() <= 255) {
				return;
			}
			else {
				throw new ConfigurationChangeException("Der Name ist l�nger als 255 Zeichen " + name + " L�nge " + name.length());
			}
		}
	}

	/**
	 * Pr�ft, ob die Pid l�nger als 255 Zeichen ist.
	 *
	 * @param pid Pid oder <code>null</code>
	 *
	 * @throws ConfigurationChangeException Die Pid ist l�nger als 255 Zeichen
	 */
	private void checkPidLength(final String pid) throws ConfigurationChangeException {
		if(pid != null) {
			if(pid.length() <= 255) {
				return;
			}
			else {
				throw new ConfigurationChangeException("Die Pid ist l�nger als 255 Zeichen " + pid + " L�nge " + pid.length());
			}
		}
	}

	public ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List<ObjectSet> sets)
			throws ConfigurationChangeException {
		try {
			checkNameLength(name);
			checkPidLength(pid);

			return getRequester().createConfigurationObject(this, type, pid != null ? pid : "", name != null ? name : "", sets);
		}
		catch(RequestException e) {
			e.printStackTrace();
			_debug.error("Fehler beim Erzeugen eines neuen Konfigurationsobjekt", e);
			throw new RuntimeException("Fehler bei der �bertragung", e);
		}
	}

	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name) throws ConfigurationChangeException {
		try {
			checkNameLength(name);
			checkPidLength(pid);

			return getRequester().createDynamicObject(this, type, pid != null ? pid : "", name != null ? name : "");
		}
		catch(RequestException e) {
			e.printStackTrace();
			_debug.error("Fehler beim Erzeugen eines neuen dynamischen Objekt", e);
			throw new RuntimeException("Fehler bei der �bertragung", e);
		}
	}

	public DynamicObject createDynamicObject(DynamicObjectType type, String pid, String name, Collection<DataAndATGUsageInformation> dataSets)
			throws ConfigurationChangeException {
		checkNameLength(name);
		checkPidLength(pid);
		if(dataSets != null) {
			for(DataAndATGUsageInformation dataSet : dataSets) {
				final Data data = dataSet.getData();
				if(data == null) throw new ConfigurationChangeException("Objekt konnte nicht erzeugt werden, weil ein Datensatz null ist: " + data);
				if(!data.isDefined()) throw new ConfigurationChangeException("Objekt konnte nicht erzeugt werden, weil in einem Datensatz nicht alle Attribute definiert sind: " + data);
			}
		}


		final ConfigurationRequester requester = getRequester();
		try {
			final LinkedList<DataAndATGUsageInformation> list;
			if(dataSets != null) {
				list = new LinkedList<DataAndATGUsageInformation>(dataSets);
			}
			else {
				list = null;
			}

			return requester.createDynamicObject(this, type, pid != null ? pid : "", name != null ? name : "", list);
		}
		catch(RequestException e) {
			e.printStackTrace();
			_debug.error("Fehler beim Erzeugen eines neuen dynamischen Objekt mit initialen Datens�tzen", e);
			throw new RuntimeException("Fehler bei der �bertragung", e);
		}
	}

	private ConfigurationRequester getRequester() {
		DafDataModel dataModel = (DafDataModel)getDataModel();
		return dataModel.getRequester();
	}
}
