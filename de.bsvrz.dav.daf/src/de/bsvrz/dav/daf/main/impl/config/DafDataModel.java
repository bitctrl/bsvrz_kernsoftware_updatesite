/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DavConnectionListener;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.BackupProgressCallback;
import de.bsvrz.dav.daf.main.config.BackupResult;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.UpdateDynamicObjects;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.ConfigurationManager;
import de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester;
import de.bsvrz.dav.daf.main.impl.config.request.RemoteRequestManager;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.ConfigurationUserAdministration;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ConfigTelegram;
import de.bsvrz.dav.daf.main.impl.config.telegrams.IdsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.IdsToObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.MetaDataAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.MetaDataRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.NewObjectAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.NewObjectRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectInvalidateAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectInvalidateRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectRevalidateAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectRevalidateRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectSetNameAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectSetNameRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectsList;
import de.bsvrz.dav.daf.main.impl.config.telegrams.PidsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.PidsToObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectRequestInfo;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TypeIdsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TypeIdsToObjectsRequest;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.filelock.FileLock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Applikationsseitige Implementierung der DataModel Schnittstelle, die Zugriffe auf die Datenmodelle und Versorgungsdaten erm�glicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9276 $
 */
public class DafDataModel implements DataModel, UpdateDynamicObjects {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Verbindung zum Datenverteiler. */
	private ClientDavInterface _connection;

	/** Zeit der Konfiguration, beim Verbindungsaufbau */
	private long _configurationTime = 0;

	/** Map der zwischengespeicherten konfigurierenden oder dynamischen Systemobjekte, als Key dient die ID des Objekts */
	private HashMap<Long, DafSystemObject> _systemObjectsById;

	/** Map der zwischengespeicherten Objekte mit PID, als Key dient die PID des Objekts */
	private HashMap<String, DafSystemObject> _systemObjectsByPid;

	/** Tabelle der zwischengespeicherten konfigurierenden Datens�tze, als Key dient ein ConfigDataKey mit Systemobjekt und Attributgruppenverwendung */
	private Hashtable<ConfigDataKey, Object> _configDataValuesTable;

	/** ConfigurationRequester f�r Konfigurationsanfragen. */
	private ConfigurationRequester _remoteRequester;

	/** Objekt zur asynchronen Benachrichtigung der Listener f�r �nderungen der Elemente von dynamischen Zusammenstellungen. */
	private NotifyingMutableCollectionChangeListener _notifyingMutableCollectionChangeListener;

	private int _acceptedCachedAreas = 0;

	private int _ignoredCachedAreas = 0;

	private int _acceptedCachedSystemObjects = 0;

	private int _ignoredCachedSystemObjects = 0;

	private int _acceptedCachedConfigData = 0;

	private int _ignoredCachedConfigData = 0;

	/**
	 * @return Liefert die Anzahl von Konfigurationsbereichen, die aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getAcceptedCachedAreas() {
		return _acceptedCachedAreas;
	}

	/**
	 * @return Liefert die Anzahl von Konfigurationsbereichen, die nicht aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getIgnoredCachedAreas() {
		return _ignoredCachedAreas;
	}

	/**
	 * @return Liefert die Anzahl von Konfigurationsobjekten, die aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getAcceptedCachedSystemObjects() {
		return _acceptedCachedSystemObjects;
	}

	/**
	 * @return Liefert die Anzahl von Konfigurationsobjekten, die nicht aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getIgnoredCachedSystemObjects() {
		return _ignoredCachedSystemObjects;
	}

	/**
	 * @return Liefert die Anzahl von konfigurierenden Datens�tzen, die aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getAcceptedCachedConfigData() {
		return _acceptedCachedConfigData;
	}

	/**
	 * @return Liefert die Anzahl von konfigurierenden Datens�tzen, die nicht aus dem lokal gespeicherten Cache der Konfigurationsobjekte �bernommen wurden.
	 */
	public int getIgnoredCachedConfigData() {
		return _ignoredCachedConfigData;
	}

	/**
	 * @return Liefert die Hashtabelle mit den zwischengespeicherten konfigurierenden Datens�tzen zur�ck.
	 */
	Hashtable<ConfigDataKey, Object> getConfigDataValuesTable() {
		return _configDataValuesTable;
	}

	/**
	 * Falls ein Datensatz angefordert wurde, aber es keinen Datensatz gab, wird dieser Platzhalter eingef�gt. Dies verhindert, das der Datensatz erneut
	 * angefordert wird.
	 */
	private static final Object _noDataMarker = new Object();

	/** Konfigurationsverantwortlicher der Konfiguration */
	private DafConfigurationAuthority _configurationAuthority;

	/** Anmeldeinfo f�r Konfigurationsanfragen zum Lesen */
	private BaseSubscriptionInfo _readBaseSubscriptionInfo;

	/** Objekt zur Verwaltung der Kommunikation mit der Konfiguration */
	private ConfigurationManager _configurationManager;

	/** Liste mit den noch nicht bearbeiteten Antworten auf Konfigurationsanfragen */
	private LinkedList _pendingResponces;

	/** Die Objekt-Id des Konfigurationsverantwortlichen */
	private long _configurationAuthorityId;

	/** Aspekt "asp.eigenschaften", der als Default f�r Konfigurierende Datens�tze bei Anfragen ohne Aspekt benutzt wird. */
	private Aspect _defaultConfigurationDataAspect;

	/** Attribugruppenverwendung Konfigurationsleseanfragen */
	private AttributeGroupUsage _configurationReadRequestUsage;

	/** Attribugruppenverwendung Konfigurationsleseantworten */
	private AttributeGroupUsage _configurationReadReplyUsage;

	/** Attribugruppenverwendung Konfigurationsschreibanfragen */
	private AttributeGroupUsage _configurationWriteRequestUsage;

	/** Attribugruppenverwendung Konfigurationsschreibantworten */
	private AttributeGroupUsage _configurationWriteReplyUsage;

	/**
	 * Verschickt Auftrage zur Benutzerverwaltung mittels des ConfigurationsRequesters an die Konfiguration. Das Objekt wird so sp�t wie m�glich initialisiert,
	 * damit alle Verbindungen aufgebaut werden k�nnen.
	 */
	private UserAdministration _userAdministration = null;

	/** Wird gesetzt, wenn die Verbindung zum Datenverteiler geschlossen wurde. */
	private boolean _connectionClosed = false;

	private DavConnectionListener _davConnectionListener;

	private Map<Long, ConfigurationAreaInfo> _areaInfos;

	private DafSystemObject[] _configAreas;

	private Set<SystemObjectType> _metaObjectTypes;

	/**
	 * Erzeugt ein neues Objekt zum Zugriff auf die Konfiguration �ber eine vorgegebene Datenverteilerverbindung.
	 *
	 * @param connection Verbindung zum Datenverteiler.
	 */
	public DafDataModel(ClientDavInterface connection) {
		_connection = connection;
		_systemObjectsById = new HashMap<Long, DafSystemObject>();
		_systemObjectsByPid = new HashMap<String, DafSystemObject>();
		_configDataValuesTable = new Hashtable<ConfigDataKey, Object>();
		_pendingResponces = new LinkedList();
		_davConnectionListener = new DavConnectionListener() {
			public void connectionClosed(ClientDavInterface connection) {
				synchronized(_pendingResponces) {
					_connectionClosed = true;
					_pendingResponces.notifyAll();
				}
			}
		};
		connection.addConnectionListener(_davConnectionListener);
	}

	/**
	 * Initialisiert dieses Objekt nach erfolgreichem Verbindungsaufbau mit dem Datenverteiler
	 *
	 * @param configurationManager     Objekt zur Verwaltung der Kommunikation mit der Konfiguration
	 * @param configurationAuthorityId Die Objekt-Id des Konfigurationsverantwortlichen
	 */
	public void init(ConfigurationManager configurationManager, long configurationAuthorityId) {
		synchronized(this) {
			_configurationManager = configurationManager;
			_configurationAuthorityId = configurationAuthorityId;
			_readBaseSubscriptionInfo = new BaseSubscriptionInfo(
					_configurationAuthorityId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
			);
			_configurationTime = 0;
			_configAreas = getMetaDataFromConfiguration();
			_configurationAuthority = (DafConfigurationAuthority)getObject(_configurationAuthorityId);
			_defaultConfigurationDataAspect = getAspect("asp.eigenschaften");
			_configurationReadRequestUsage = (AttributeGroupUsage)getObject("atgv.atg.konfigurationsAnfrage.asp.anfrage");
			_configurationReadReplyUsage = (AttributeGroupUsage)getObject("atgv.atg.konfigurationsAntwort.asp.antwort");
			_configurationWriteRequestUsage = (AttributeGroupUsage)getObject("atgv.atg.konfigurationsSchreibAnfrage.asp.anfrage");
			_configurationWriteReplyUsage = (AttributeGroupUsage)getObject("atgv.atg.konfigurationsSchreibAntwort.asp.antwort");
		}
	}

	/** Hilfsklasse zum Speichern von Informationen zu Konfigurationsbereichen. */
	private class ConfigurationAreaInfo {

		private DafConfigurationArea _area;

		private short _activeVersion;

		private long _dynamicObjectChangeTime;

		private long _configurationObjectChangeTime;

		private long _configurationDataChangeTime;

		public ConfigurationAreaInfo(
				final DafConfigurationArea area,
				final short activeVersion,
				final long dynamicObjectChangeTime,
				final long configurationObjectChangeTime,
				final long configurationDataChangeTime
		) {
			_area = area;
			_activeVersion = activeVersion;
			_dynamicObjectChangeTime = dynamicObjectChangeTime;
			_configurationObjectChangeTime = configurationObjectChangeTime;
			_configurationDataChangeTime = configurationDataChangeTime;
		}
	}

	/**
	 * Liest lokal zwischengespeicherte Konfigurationsobjekte und konfigurierende Datens�tze ein, falls in dem entsprechenden Aufrufparameter ein Verzeichnis
	 * angegeben wurde und dort eine passende Datei vorhanden ist.
	 * Nach der Anzahl der relevanten Konfigurationsbereiche werden f�r jeden Konfigurationsbereich folgende Informationen in der Datei erwartet
	 * <ul><li>die (long-)Id des Konfigurationsbereiches,</li> <li>die  (short-)Aktive Version des Konfigurationsbereiches,</li><li> der (long-) Zeitstempel der
	 * letzen �nderung von dynamischen Objekten,</li><li> der (long-) Zeitsempel der letzten �nderung von konfigurierenden Objekten,</li><li> der Zeitstempel der
	 * letzten �nderung von konfigurierenden Datens�tzen</li></ul>
	 * Diese Informationen werden mit den entsprechenden Werten in der Konfiguration verglichen. Nur wenn alle Werte �bereinstimmen, werden Objekte des
	 * jeweiligen Bereichs aus der Datei geladen.
	 * Ein Konfigurationsobjekt wird seriell wie folgt aus der Datei gelesen: <ol> <li>byte: 1 (die eins kennzeichnet ein Konfigurationsobjekt und zeigt an, dass
	 * ein solches folgt)</li> <li>byte: internType (gint an, um welchen Typ von Konfigurationsobjekt es sich handelt)</li> <li>Object: object(serielles
	 * Objekt)</li> </ol> Ein konfigurierender Datensatz wird seriell aus der Datei wie folgt gelesen: <ol> <li> byte: 2 (die zwei kennzeichnet einen
	 * konfigurierenden Datensatz und zeigt an das ein solcher folgt)</li> <li> long: Objekt-ID(Die ID des Objektes zu dem der konfiguriende Datensatz geh�rt)</li>
	 * <li> long: AtgV-ID(Die ID der Atributgruppenverwedung des konfigurienden Datensatzes)</li> <li> boolean: true-> Datensatz enth�lt Daten, false-> Datensatz
	 * enth�lt keine Daten</li> <li> Data: serialisierte Daten des Datensatzes</li> </ol> Abschlie�end wird eine byte-0 geschrieben, das anzeigt, dass das Ende der
	 * Datei erreicht ist.
	 */
	public void loadLocalConfigurationCache() {
		String configurationPath = _configurationManager.getConfigurationPath();

		try {
			if(configurationPath == null) {
				return;
			}
			Map<Long, ConfigurationAreaInfo> areaInfos = new HashMap<Long, ConfigurationAreaInfo>();
			final Data[] datas = getConfigurationData(_configAreas, getAttributeGroup("atg.konfigurationsBereich�nderungsZeiten"));
			for(int i = 0; i < _configAreas.length; i++) {
				DafSystemObject configArea = _configAreas[i];
				if(configArea instanceof DafConfigurationArea) {
					DafConfigurationArea configurationArea = (DafConfigurationArea)configArea;
					final long areaId = configurationArea.getId();
					final short activeVersion = configurationArea.getActiveVersion();
					long dynamicObjectChangeTime = -1;
					long configurationObjectChangeTime = -1;
					long configurationDataChangeTime = -1;
					if(datas[i] != null) {
						dynamicObjectChangeTime = datas[i].getTimeValue("Letzte�nderungszeitDynamischesObjekt").getMillis();
						configurationObjectChangeTime = datas[i].getTimeValue("Letzte�nderungszeitKonfigurationsObjekt").getMillis();
						configurationDataChangeTime = datas[i].getTimeValue("Letzte�nderungszeitDatensatz").getMillis();
					}
					final ConfigurationAreaInfo info;
					info = new ConfigurationAreaInfo(
							configurationArea, activeVersion, dynamicObjectChangeTime, configurationObjectChangeTime, configurationDataChangeTime
					);
					areaInfos.put(areaId, info);
				}
			}
			_areaInfos = areaInfos;

			String[] metaTypePids = {
					"typ.aspekt", "typ.attribut", "typ.attributgruppe", "typ.attributgruppenVerwendung", "typ.attributTyp", "typ.konfigurationsBereich",
					"typ.konfigurationsVerantwortlicher", "typ.mengenVerwendung", "typ.typ", "typ.werteBereich", "typ.werteZustand", "menge.aspekte",
					"menge.attribute", "menge.attributgruppen", "menge.attributgruppenVerwendungen", "menge.mengenVerwendungen", "menge.objektTypen",
					"menge.werteZustaende"
			};
			Set<SystemObjectType> metaObjectTypes;
			metaObjectTypes = new HashSet<SystemObjectType>();
			final List<SystemObjectType> typesToAdd = new LinkedList<SystemObjectType>();
			for(int i = 0; i < metaTypePids.length; i++) {
				String metaTypePid = metaTypePids[i];
				final DafSystemObjectType metaObjectType = (DafSystemObjectType)getType(metaTypePid);
				typesToAdd.add(metaObjectType);
			}
			while(!typesToAdd.isEmpty()) {
				SystemObjectType typeToAdd = typesToAdd.remove(0);
				if(metaObjectTypes.add(typeToAdd)) {
					// Wenn der Typ noch nicht im Set war, dann auch dessen Sub-Typen betrachten
					typesToAdd.addAll(typeToAdd.getSubTypes());
				}
			}
			_metaObjectTypes = metaObjectTypes;
		}
		catch(Exception e) {
			_debug.warning("Vorbereitung zum Laden der lokalen Konfigurationsdatei fehlgeschlagen ", e);
			return;
		}
		try {
			final File localConfigurationFile = getLocalConfigurationCacheFile(configurationPath);
			if(!localConfigurationFile.exists()) {
				_debug.info("Lokale Konfigurationsdatei ist nicht vorhanden", localConfigurationFile);
				return;
			}
			if(!localConfigurationFile.canRead()) {
				_debug.warning("Lesender Zugriff auf lokale Konfigurationsdatei nicht erlaubt", localConfigurationFile);
				return;
			}
			FileLock fileLock = new FileLock(localConfigurationFile);
			DataInputStream in = null;
			fileLock.lock();
			try {
				_debug.info("Lokale Konfiguration wird gelesen", localConfigurationFile);
				in = new DataInputStream(new BufferedInputStream(new FileInputStream(localConfigurationFile)));

				// Fester String zur Kennzeichnung der Datei
				if(in.readUTF().equals("LokaleKonfigurationsCacheDatei") == false) {
					throw new IllegalArgumentException("Dateikopf Fehlerhaft; Datei ist keine lokale Konfigurationsdatei");
				}

				// Versionsnummer
				if(in.readByte() != 1) {
					throw new IllegalArgumentException("Version der lokalen Konfigurationsdatei ist nicht wie erwartet 1");
				}

				Set<Long> acceptableAreas = new HashSet<Long>();

				// Anzahl der folgenden Bl�cke, die jeweils Informationen zu einem Konfigurationsbereich aufweisen
				int numberOfAreas = in.readInt();

				_acceptedCachedAreas = 0;

				_ignoredCachedAreas = 0;


				for(int i = 0; i < numberOfAreas; i++) {

					// Id des Bereichs
					long areaId = in.readLong();

					// Aktive Version des Bereichs
					short activeVersion = in.readShort();

					// Zeitstempel der letzten �nderung an dynamischen Objekten des Bereichs
					final long dynamicObjectChangeTime = in.readLong();

					// Zeitstempel der letzten �nderung an Konfigurationsobjekten des Bereichs
					final long configurationObjectChangeTime = in.readLong();

					// Zeitstempel der letzten �nderung an konfigurierenden Datens�tzen
					final long configurationDataChangeTime = in.readLong();

					final DafDataModel.ConfigurationAreaInfo info = _areaInfos.get(areaId);
					if(info._activeVersion == activeVersion && info._dynamicObjectChangeTime == dynamicObjectChangeTime
					   && info._configurationObjectChangeTime == configurationObjectChangeTime
					   && info._configurationDataChangeTime == configurationDataChangeTime) {
						_acceptedCachedAreas++;
						acceptableAreas.add(areaId);
						_debug.finer("Objekte des folgenden Bereichs werden akzeptiert", info._area.getPid());
					}
					else {
						_ignoredCachedAreas++;
						_debug.info(
								"Da sich der folgende Konfigurationsbereich ge�ndert hat, werden lokal gespeicherte Objekte dieses Bereichs verworfen",
								info._area.getPid()
						);
					}
				}

				_acceptedCachedSystemObjects = 0;
				_ignoredCachedSystemObjects = 0;

				_acceptedCachedConfigData = 0;
				_ignoredCachedConfigData = 0;

				//deserialisieren
				Deserializer deserializer = SerializingFactory.createDeserializer(in);

				while(true) {
					// Kennung, 1 heisst: es folgt ein Objekt, 2 heisst: es folgt ein konfigurierender Datensatz,  0 heisst: Ende
					byte token = in.readByte();

					if(token == 1) {

						// Typkennung des Objekts
						byte objectTypeByte = in.readByte();

						DafSystemObject object = DafSystemObject.getObject(objectTypeByte, this);
						if(object == null) {
							throw new RuntimeException("Fehlerhafter Dateiaufbau, Objekttypkennung " + objectTypeByte);
						}

						// Serialisiertes Objekt
						object.read(in);
						if(acceptableAreas.contains(object.getConfigurationAreaId())) {
							_acceptedCachedSystemObjects++;
							// Objekt in interne Tabellen eintragen
							updateInternalDataStructure(object);
						}
						else {
							_ignoredCachedSystemObjects++;
						}
					}
					else if(token == 2) {
						long objectID = deserializer.readLong();
						long atgUsageID = deserializer.readLong();

						SystemObject object = this.getObject(objectID);
						AttributeGroupUsage atgUsage = this.getAttributeGroupUsage(atgUsageID);
						if(object == null || atgUsage == null) {
							throw new IllegalStateException("unbekannte gespeicherte Datenidentifikation: Objekt-ID: " + objectID + ", objekt: " + object + ", atgUsageID: " + atgUsageID + ", atgUsage: " + atgUsage);
						}

						ConfigDataKey dataKey = new ConfigDataKey(object, atgUsage);

						boolean dataExists = deserializer.readBoolean();
						boolean accepted = acceptableAreas.contains(object.getConfigurationArea().getId());
						Data data;

						//Existiert der Datensatz ?
						if(dataExists) {

							AttributeGroup atg = atgUsage.getAttributeGroup();

							try {
								data = deserializer.readData(atg, this);
							}
							catch(Exception e) {
								//beim Lesen des Datensatzes ist ein Fehler aufgetreten,
								//ab hier werden alle konfigurierenden Datens�tze aus der Datei ignoriert
								_debug.warning("Fehler beim Lesen eines Zwischengespeicherten Datensatzes", e);
								break;
							}
						}
						else {
							data = null;
						}

						if(accepted) {
							_acceptedCachedConfigData++;

							if(data != null) {
								_configDataValuesTable.put(dataKey, data);
							}
							else {
								//es existiert kein Datensatz
								_configDataValuesTable.put(dataKey, _noDataMarker);
							}
						}
						else {
							_ignoredCachedConfigData++;
						}
					}
					else if(token == 0) {
						break;
					}
					else {
						throw new RuntimeException("Fehlerhafter Dateiaufbau, Token " + token);
					}
				}
				_debug.fine("Anzahl akzeptierter Konfigurationsbereiche", _acceptedCachedAreas);
				_debug.fine("Anzahl verworfener Konfigurationsbereiche", _ignoredCachedAreas);
				_debug.fine("Anzahl akzeptierter SystemObjekte", _acceptedCachedSystemObjects);
				_debug.fine("Anzahl verworfener SystemObjekte", _ignoredCachedSystemObjects);
				_debug.fine("Anzahl akzeptierte konfigurierende Datens�tze", _acceptedCachedConfigData);
				_debug.fine("Anzahl verworfene konfigurierende Datens�tze", _ignoredCachedConfigData);

				_debug.info("lokale Konfigurationsdatei wurde erfolgreich eingelesen");
			}
			finally {
				if(in != null) {
					in.close();
				}
				fileLock.unlock();
			}
		}
		catch(Exception e) {
			_debug.warning("Fehler beim Laden der lokalen Konfigurationsdatei aus dem Verzeichnis " + configurationPath, e);
			return;
		}
	}

	/**
	 * Speichert Konfigurationsobjekte und konfigurierende Datens�tze in einer lokalen Konfigurationsdatei, falls im entsprechenden Aufrufparameter ein Verzeichnis
	 * angegeben wurde. Vor den Konfigurationsobjekten und deren Datens�tzen wird folgendes geschrieben: <ul><li>die (long-)Id des Konfigurationsbereiches,</li>
	 * <li>die  (short-)Aktive Version des Konfigurationsbereiches,</li><li> der (long-) Zeitstempel der letzen �nderung von dynamischen Objekten,</li><li> der
	 * (long-) Zeitsempel der letzten �nderung von konfigurierenden Objekten,</li><li> der Zeitstempel der letzten �nderung von konfigurierenden
	 * Datens�tzen</li></ul>
	 * EinkonfigurationsObjekt wird seriell wie folgt in der Datei abgelegt: <ol> <li>byte: 1 (die eins kennzeichnet ein Konfigurationsobjekt und zeigt an, dass
	 * ein solches folgt)</li> <li>byte: internType (gint an, um welchen Typ von Konfigurationsobjekt es sich handelt)</li> <li>Object: object(serielles
	 * Objekt)</li> </ol> Ein konfigurierender Datensatz wird seriell in der Datei wie folgt abgelegt: <ol> <li> byte: 2 (die zwei kennzeichnet einen
	 * konfigurierenden Datensatz und zeigt an das ein solcher folgt)</li> <li> long: Objekt-ID(Die ID des Objektes zu dem der konfiguriende Datensatz geh�rt)</li>
	 * <li> long: AtgV-ID(Die ID der Atributgruppenverwedung des konfigurienden Datensatzes)</li> <li> boolean: true-> Datensatz enth�lt Daten, false-> Datensatz
	 * enth�lt keine Daten</li> <li> Data: serialisierte Daten des Datensatzes</li> </ol> Abschlie�end wird eine byte-0 geschrieben, die anzeigt, dass das Ende der
	 * Datei erreicht ist.
	 */
	private void saveLocalConfigurationCache() {
		String configurationPath = null;
		if(_configurationManager != null) {
			configurationPath = _configurationManager.getConfigurationPath();
		}
		if(configurationPath == null) {
			return;
		}
		if(_areaInfos == null) {
			_debug.warning(
					"Lokale Konfigurationsdatei kann nicht gespeichert werden, weil die dazu erforderlichen Konfigurationsbereichsinformationen "
					+ "nicht ermittelt werden konnten"
			);
			return;
		}
		try {
			final File localConfigurationFile = getLocalConfigurationCacheFile(configurationPath);
			if(localConfigurationFile.exists() && !localConfigurationFile.canWrite()) {
				_debug.warning("Schreibender Zugriff auf vorhandene lokale Konfigurationsdatei nicht erlaubt", localConfigurationFile);
				return;
			}
			boolean saveWasSuccessful = false;
			DataOutputStream out = null;
			FileLock fileLock = new FileLock(localConfigurationFile);
			fileLock.lock();
			try {
				_debug.info("Lokale Konfiguration wird geschrieben", localConfigurationFile);
				out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(localConfigurationFile)));
				// Fester String zur Kennzeichnung der Datei
				out.writeUTF("LokaleKonfigurationsCacheDatei");
				// Versionsnummer
				out.writeByte(1);
				// Anzahl der folgenden Bl�cke, die jeweils Informationen zu einem Konfigurationsbereich aufweisen
				out.writeInt(_areaInfos.size());
				for(Map.Entry<Long, ConfigurationAreaInfo> entry : _areaInfos.entrySet()) {
					final long areaId = entry.getKey();
					final DafDataModel.ConfigurationAreaInfo info = entry.getValue();
					// Id des Bereichs
					out.writeLong(areaId);
					// Aktive Version des Bereichs
					out.writeShort(info._activeVersion);
					// Zeitstempel der letzten �nderung an dynamischen Objekten des Bereichs
					out.writeLong(info._dynamicObjectChangeTime);
					// Zeitstempel der letzten �nderung an Konfigurationsobjekten des Bereichs
					out.writeLong(info._configurationObjectChangeTime);
					// Zeitstempel der letzten �nderung an konfigurierenden Datens�tzen
					out.writeLong(info._configurationDataChangeTime);
				}

				DafSystemObject[] objects;
				synchronized(_systemObjectsById) {
					objects = _systemObjectsById.values().toArray(new DafSystemObject[0]);
				}
				int systemObjectsWritten = 0;
				for(int i = 0; i < objects.length; i++) {

					DafSystemObject object = objects[i];
					if(object.isValid() && !_metaObjectTypes.contains(object.getType())) {

						// Kennung 1 heisst: es folgt ein Objekt
						out.writeByte(1);

						// Typkennung des Objekts
						out.writeByte(object.getInternType());

						// Serialisiertes Objekt
						object.write(out);

						systemObjectsWritten++;
					}
				}

				//Konfigurierende Datens�tze schreiben
				int configDataSetsWritten = 0;
				final Serializer serializer = SerializingFactory.createSerializer(out);
				final Set<Map.Entry<ConfigDataKey, Object>> entries = _configDataValuesTable.entrySet();
				for(Map.Entry<ConfigDataKey, Object> entry : entries) {

					// Kennung 2 heisst: es folgt ein konfigurierender Datensatz
					// Aufbau => 2-> Long:ID-Objekt-> Long:ID-AtgV-> Boolean-Data-> Data
					serializer.writeByte(2);

					final ConfigDataKey key = entry.getKey();
					serializer.writeLong(key._object.getId());
					serializer.writeLong(key._atgUsage.getId());

					if(entry.getValue() instanceof Data) {
						Data data = (Data)entry.getValue();
						serializer.writeBoolean(true);
						serializer.writeData(data);
					}
					else {
						// Kein Datensatz in der Konfiguration
						serializer.writeBoolean(false);
					}

					configDataSetsWritten++;
				}
				_debug.fine("Anzahl geschriebener Systemobjekte", systemObjectsWritten);
				_debug.fine("Anzahl geschriebener konfigurierender Datens�tze", configDataSetsWritten);

				// Kennung 0 heisst: Ende
				out.writeByte(0);

				saveWasSuccessful = true;
				_debug.info("lokale Konfigurationsdatei wurde erfolgreich geschrieben");
			}
			finally {
				if(out != null) {
					out.close();
				}
				if(saveWasSuccessful == false) {
					localConfigurationFile.delete();
				}
				fileLock.unlock();
			}
		}
		catch(Exception e) {
			_debug.warning("Fehler beim Speichern der lokalen Konfigurationsdatei in das Verzeichnis " + configurationPath, e);
		}
	}

	/**
	 * Bestimmt das File-Objekt f�r die Datei zur lokalen Speicherung von Konfigurationsdaten. Der Dateiname setzt sich aus der Pid des
	 * Konfigurationsverantwortlichen der Konfiguration, dem Namen der Applikation und der Endung <code>.configcache</code> zusammen.
	 *
	 * @param configurationPath Verzeichnis f�r die Datei zur lokalen Speicherung von Konfigurationsdaten.
	 *
	 * @return File-Objekt f�r die Datei zur lokalen Speicherung von Konfigurationsdaten.
	 */
	private File getLocalConfigurationCacheFile(final String configurationPath) {
		final File specifiedDirectory = new File(configurationPath);
		if(!specifiedDirectory.exists()) {
			throw new IllegalArgumentException(
					"Das im Aufrufparameter -lokaleSpeicherungKonfiguration=" + configurationPath + " angegebene Verzeichnis ist nicht vorhanden"
			);
		}
		if(!specifiedDirectory.isDirectory()) {
			throw new IllegalArgumentException(
					"Das im Aufrufparameter -lokaleSpeicherungKonfiguration=" + configurationPath + " angegebene Verzeichnis ist kein Verzeichnis"
			);
		}
		final String kv = _configurationAuthority.getPidOrId();
		String applicationName = _configurationManager.getApplicationName();
		return new File(specifiedDirectory, kv + "-" + applicationName + ".configcache");
	}

	/**
	 * Diese Methode sollte beim Terminieren der Datenverteilerverbindung aufgerufen werden. Sie speichert die zwischengespeicherten Objekte falls gew�nscht in
	 * einer Datei im lokalen Dateisystem.
	 */
	public final void close() {
		synchronized(_pendingResponces) {
			_connectionClosed = true;
			_pendingResponces.notifyAll();
		}
		_connection.removeConnectionListener(_davConnectionListener);

		if(_configurationManager != null) {
			saveLocalConfigurationCache();
		}
		if(_notifyingMutableCollectionChangeListener != null) {
			_notifyingMutableCollectionChangeListener.stop();
		}
	}

	/**
	 * Mit dem zur�ckgegebenen Objekt k�nnen Anfragen an eine Konfiguration gestellt werden.
	 *
	 * @return Objekt f�r Konfigurationsanfragen
	 */
	public ConfigurationRequester getRequester() {
		if(_remoteRequester == null) {
			final RemoteRequestManager remoteRequestManager;

			if(_connection instanceof ClientDavConnection) {
				// Dieser Schritt ist notwendig, da sich der Datenverteiler ebenfalls anmeldet. Das Objekt des Datenverteilers kann aber nur �ber die Id
				// identifiziert werden. Die Methode _connection.getLocalApplicationObject() kann nicht benutzt werden, da das Datenverteilerobjekt kein
				// <code>ClientApplication</code> ist. Es w�rde zu einer ClassCastException kommen.
				remoteRequestManager = RemoteRequestManager.getInstance(_connection, this, ((ClientDavConnection)_connection).getLocalApplicationObjectId());
			}
			else {
				remoteRequestManager = RemoteRequestManager.getInstance(_connection, this, _connection.getLocalApplicationObject());
			}

			_remoteRequester = remoteRequestManager.getRequester(_connection.getLocalConfigurationAuthority());
			_notifyingMutableCollectionChangeListener = new NotifyingMutableCollectionChangeListener();
			_remoteRequester.setMutableCollectionChangeListener(_notifyingMutableCollectionChangeListener);
			_notifyingMutableCollectionChangeListener.start();
		}
		return _remoteRequester;
	}

	/**
	 * Gibt die aktuelle Verbindung zum Datenverteiler zur�ck.
	 *
	 * @return Die Verbindung zum Datenverteiler
	 */
	public ClientDavInterface getConnection() {
		return _connection;
	}

	/**
	 * Aktualisiert die Tabellen mit zwischengespeicherten Objekten, wenn Objekte g�ltig bzw. ung�ltig geworden sind.
	 *
	 * @param systemObject Objekt das m�glicherweise g�ltig bzw. ung�ltig geworden ist.
	 *
	 * @return Bereits vorher zwischengespeichertes Objekt mit der angegebenen Id oder �bergebenes Objekt
	 */
	private DafSystemObject updateInternalDataStructure(DafSystemObject systemObject) {

		Long id = new Long(systemObject.getId());
		synchronized(_systemObjectsById) {
			final byte objectState = systemObject.getState();
			if(objectState == DafSystemObject.OBJECT_EXISTS || objectState == DafSystemObject.OBJECT_INVALID) {
				DafSystemObject oldObject = _systemObjectsById.put(id, systemObject);
				if(oldObject != null) {
					if(oldObject != systemObject) {
						// Ein �lteres Java-Objekt ist bereits vorhanden, also dieses verwenden
						systemObject = oldObject;
						_systemObjectsById.put(id, systemObject);
					}
				}
				else {
					if(objectState == DafSystemObject.OBJECT_EXISTS) {
						String pid = systemObject.getPid();
						if(pid != null && !pid.equals("")) {
							_systemObjectsByPid.put(pid, systemObject);
						}
					}
				}
			}
			else {
				// Objekt wurde ung�ltig
				final DafSystemObject oldObject = _systemObjectsById.get(id);
				if(oldObject != null) {
					systemObject = oldObject;
					String pid = systemObject.getPid();
					if(pid != null && !pid.equals("")) {
						_systemObjectsByPid.remove(pid);
					}
				}
				














			}
		}
		return systemObject;
	}

	/**
	 * Liefert das Objekt zur Verwaltung der Kommunikation mit der Konfiguration zur�ck.
	 *
	 * @return Objekt zur Verwaltung der Kommunikation oder <code>null</code>, falls es noch nicht gesetzt wurde.
	 *
	 * @see #init(de.bsvrz.dav.daf.main.impl.ConfigurationManager,long)
	 */
	public final ConfigurationManager getConfigurationManager() {
		return _configurationManager;
	}

	public final Aspect getAspect(String pid) {
		try {
			return (Aspect)getObject(pid);
		}
		catch(ClassCastException ex) {
			throw new IllegalArgumentException("Die angegebene Pid " + pid + " ist keine Pid eines Aspekts");
		}
	}

	public final AttributeGroup getAttributeGroup(String pid) {
		try {
			return (AttributeGroup)getObject(pid);
		}
		catch(ClassCastException ex) {
			throw new IllegalArgumentException("Die gegebene Pid " + pid + " ist keine Pid einer Attributgruppe");
		}
	}

	public final AttributeType getAttributeType(String pid) {
		try {
			return (DafAttributeType)getObject(pid);
		}
		catch(ClassCastException ex) {
			throw new IllegalArgumentException("Die gegebene Pid " + pid + " ist keine Pid eines Attributtyps");
		}
	}

	public final SystemObjectType getType(String pid) {
		try {
			return (DafSystemObjectType)getObject(pid);
		}
		catch(ClassCastException ex) {
			throw new IllegalArgumentException("Die gegebene Pid " + pid + " ist keine Pid eines Typs");
		}
	}

	public final ObjectSetType getObjectSetType(String pid) {
		try {
			return (ObjectSetType)getType(pid);
		}
		catch(ClassCastException ex) {
			throw new IllegalArgumentException("Die gegebene Pid " + pid + " ist keine Pid eines Mengen-Typs");
		}
	}

	public final SystemObjectType getTypeTypeObject() {
		return getType(Pid.Type.TYPE);
	}

	public final List getBaseTypes() {
		ArrayList list = new ArrayList();
		SystemObjectType primType = null;
		primType = getType(Pid.Type.CONFIGURATION_OBJECT);
		if(primType != null) {
			list.add(primType);
		}
		primType = getType(Pid.Type.DYNAMIC_OBJECT);
		if(primType != null) {
			list.add(primType);
		}
		return list;
	}

	public final SystemObject getObject(long id) {
		if(id == 0) {
			return null;
		}
		DafSystemObject systemObject = getObjectFromCache(id);
		if(systemObject == null) {
			systemObject = getSystemObjectFromConfiguration(id);
		}
		return systemObject;
	}

	/**
	 * Initiale Abfrage der von der Applikation ben�tigten Objekte.
	 *
	 * @return Array mit den Konfigurationsbereichen
	 */
	private final DafSystemObject[] getMetaDataFromConfiguration() {
		ConfigTelegram telegram = new MetaDataRequest(0);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);
		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.META_DATA_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							DafSystemObject objects[] = ((MetaDataAnswer)responce).getObjects();
							ArrayList<DafSystemObject> configurationAreas = new ArrayList<DafSystemObject>();
							if(objects != null) {
								for(int j = 0; j < objects.length; ++j) {
									DafSystemObject object = objects[j];
									if(object != null) {
										object = updateInternalDataStructure(object);
										if(object instanceof DafConfigurationArea) {
											configurationAreas.add((DafConfigurationArea)object);
										}
									}
								}
							}
							return configurationAreas.toArray(new DafSystemObject[configurationAreas.size()]);
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public SystemObject getObject(String pid) {
		if(pid == null) {
			throw new IllegalArgumentException("�bergabeparameter ist null");
		}
		DafSystemObject systemObject;
		synchronized(_systemObjectsById) {
			systemObject = (DafSystemObject)_systemObjectsByPid.get(pid);
		}
		if(systemObject != null) {
			return systemObject;
		}
		else {
			String[] pids = {pid};
			SystemObjectRequestInfo systemObjectRequestInfo = new PidsToObjectsRequest(pids);
			ConfigTelegram telegram = new SystemObjectsRequest(_configurationTime, systemObjectRequestInfo);
			String info = Integer.toString(telegram.hashCode());
			telegram.setInfo(info);
			_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

			// Waiting for Answer
			ConfigTelegram responce = null;
			long waitingTime = 0, startTime = System.currentTimeMillis();
			long sleepTime = 10;
			while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
				try {
					synchronized(_pendingResponces) {
						if(_connectionClosed) {
							throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
						}
						_pendingResponces.wait(sleepTime);
						if(sleepTime < 1000) {
							sleepTime *= 2;
						}

						ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
						while(_iterator.hasPrevious()) {
							responce = (ConfigTelegram)_iterator.previous();
							if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
								_iterator.remove();
								PidsToObjectsAnswer pidsToObjectsAnswer = null;
								try {
									pidsToObjectsAnswer = (PidsToObjectsAnswer)((SystemObjectAnswer)responce).getSystemObjectAnswerInfo();
								}
								catch(ClassCastException ex) {
									ex.printStackTrace();
								}
								if(pidsToObjectsAnswer != null) {
									DafSystemObject objects[] = pidsToObjectsAnswer.getObjects();
									if(objects != null) {
										systemObject = objects[0];
										if(systemObject == null) {
											return null;
										}
										else {
											if(pid.equals(systemObject.getPid())) {
												return updateInternalDataStructure(systemObject);
											}
										}
									}
								}
								return null;
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
				catch(InterruptedException ex) {
					ex.printStackTrace();
					break;
				}
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * L�dt ein Systemobjekt durch entsprechende Konfigurationsanfragen aus der Konfiguration.
	 *
	 * @param id ID des gew�nschen Objekts.
	 *
	 * @return Gew�nschtes Systemobjekt
	 */
	private final DafSystemObject getSystemObjectFromConfiguration(long id) {
		DafSystemObject systemObject = null;
		long[] ids = {id};
		SystemObjectRequestInfo systemObjectRequestInfo = new IdsToObjectsRequest(ids);
		ConfigTelegram telegram = new SystemObjectsRequest(_configurationTime, systemObjectRequestInfo);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}
					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							IdsToObjectsAnswer idsToObjectsAnswer = null;
							try {
								idsToObjectsAnswer = (IdsToObjectsAnswer)((SystemObjectAnswer)responce).getSystemObjectAnswerInfo();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							if(idsToObjectsAnswer != null) {
								DafSystemObject objects[] = idsToObjectsAnswer.getObjects();
								if(objects != null) {
									systemObject = objects[0];
									if(systemObject == null) {
										return null;
									}
									else {
										if(id == systemObject.getId()) {
											return updateInternalDataStructure(systemObject);
										}
									}
								}
							}
							return null;
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Liefert eine Liste mit allen Systemobjekten eines Typs zur�ck. Zu beachten ist, das auch Objekte eines Typs, der diesen Typ erweitert, zur�ckgegeben
	 * werden.
	 *
	 * @param type Typ der gew�nschten Systemobjekte
	 *
	 * @return Liste von {@link DafSystemObject System-Objekten}
	 */
	final List<SystemObject> getObjectsOfType(DafSystemObjectType type) {
		long[] ids = {type.getId()};
		SystemObjectRequestInfo systemObjectRequestInfo = new TypeIdsToObjectsRequest(ids);
		ConfigTelegram telegram = new SystemObjectsRequest(_configurationTime, systemObjectRequestInfo);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							TypeIdsToObjectsAnswer typIdsToObjectsAnswer = null;
							try {
								typIdsToObjectsAnswer = (TypeIdsToObjectsAnswer)((SystemObjectAnswer)responce).getSystemObjectAnswerInfo();
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							ArrayList<SystemObject> list = new ArrayList<SystemObject>();
							if(typIdsToObjectsAnswer != null) {
								ObjectsList[] objectLists = typIdsToObjectsAnswer.getObjectsOfTypes();
								if(objectLists != null) {
									if(objectLists[0] != null) {
										if(objectLists[0].getBaseObjectId() == ids[0]) {
											DafSystemObject systemObjects[] = objectLists[0].getObjects();
											if(systemObjects != null) {
												for(int j = 0; j < systemObjects.length; ++j) {
													DafSystemObject systemObject = systemObjects[j];
													if(systemObject != null) {
														list.add(updateInternalDataStructure(systemObject));
													}
												}
											}
										}
									}
								}
							}
							return list;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public final ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List sets)
			throws ConfigurationChangeException {
		if(!type.isConfigurating()) {
			throw new IllegalArgumentException("createConfigurationObject wurde mit einem Typ aufgerufen, der nicht konfigurierend ist: " + type);
		}
		long setIds[] = null;
		if(sets.size() > 0) {
			setIds = new long[sets.size()];
			for(int i = 0; i < sets.size(); ++i) {
				setIds[i] = ((DafObjectSet)sets.get(i)).getId();
			}
		}
		ConfigTelegram telegram = new NewObjectRequest(_configurationTime, -1, pid, name, type.getId(), setIds);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.NEW_OBJECT_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							DafSystemObject object = ((NewObjectAnswer)responce).getObject();
							if(object == null) {
								throw new ConfigurationChangeException("Objekt konnte nicht in der Konfiguration angelegt werden.");
							}
							else {
								return (ConfigurationObject)updateInternalDataStructure(object);
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public final DynamicObject createDynamicObject(SystemObjectType type, String pid, String name) throws ConfigurationChangeException {
		if(type.isConfigurating()) {
			throw new IllegalArgumentException("createDynamicObject wurde mit einem Typ aufgerufen, der nicht dynamisch ist: " + type);
		}
		ConfigTelegram telegram = new NewObjectRequest(_configurationTime, -1, pid, name, type.getId(), null);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.NEW_OBJECT_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							DafSystemObject object = ((NewObjectAnswer)responce).getObject();
							if(object == null) {
								throw new ConfigurationChangeException("Objekt konnte nicht in der Konfiguration angelegt werden.");
							}
							else {
								return (DynamicObject)updateInternalDataStructure(object);
							}
						}
					}
					waitingTime = System.currentTimeMillis() - startTime;
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public DafConfigurationArea getConfigurationArea(String pid) {
		return (DafConfigurationArea)getObject(pid);
	}

	public ConfigurationAuthority getConfigurationAuthority() {
		return _configurationAuthority;
	}

	public String getConfigurationAuthorityPid() {
		return getConfigurationAuthority().getPid();
	}

	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime) {
		final ConfigurationRequester requester = getRequester();

		try {
			return requester.getObjects(pid, startTime, endTime);
		}
		catch(RequestException e) {
			e.printStackTrace();
			final String msg = "Fehler bei einer Konfigurationsanfrage nach Objekten, die in einem bestimmten Zeitbereich aktiv waren, angefragte Pid: " + pid;
			_debug.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public Collection<SystemObject> getObjects(
			Collection<ConfigurationArea> configurationAreas, Collection<SystemObjectType> systemObjectTypes, ObjectTimeSpecification objectTimeSpecification
	) {
		final ConfigurationRequester requester = getRequester();

		try {
			return requester.getObjects(configurationAreas, systemObjectTypes, objectTimeSpecification);
		}
		catch(RequestException e) {
			e.printStackTrace();
			final String msg =
					"Fehler bei einer Konfigurationsanfrage nach Objekten, die in bestimmten Bereichen gespeichert sind und bestimmte Typen besitzen, Bereiche "
					+ configurationAreas + " Typen: " + systemObjectTypes + " Zeitspezifikation " + objectTimeSpecification;
			_debug.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public UserAdministration getUserAdministration() {

		// Das Objekt wird so sp�t wie m�glich angelegt, damit alle Verbindungen initialisiert sind.
		synchronized(this) {
			if(_userAdministration == null) {
				_userAdministration = new ConfigurationUserAdministration(getRequester());
			}
		}
		return _userAdministration;
	}

	public BackupResult backupConfigurationFiles(final String targetDirectory, final BackupProgressCallback callback)
			throws ConfigurationTaskException, RequestException {
		final ConfigurationRequester requester = getRequester();
		return requester.backupConfigurationFiles(targetDirectory, callback);
	}

	/**
	 * Ermittelt einen konfigurierenden Datensatz. Wenn nicht vorhanden wird es aus der Konfiguration geholt.
	 *
	 * @param object         Objekt des gew�nschten Datensatzes
	 * @param attributeGroup Attributgruppe des gew�nschten Datensatzes
	 *
	 * @return Liste mit den Attributwerten des Datensatzes.
	 *
	 * @deprecated Zum Lesen von konfigurierenden Datens�tzen sollten die Methoden {@link #getConfigurationData} und {@link
	 *             de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData} verwendet werden.
	 */
	@Deprecated
	public final List getObjectDataValues(SystemObject object, AttributeGroup attributeGroup) {
		Data data = getConfigurationData(object, attributeGroup);
		if(data != null) {
			data = data.createModifiableCopy();
			if(data instanceof AttributeBaseValueDataFactory.AttributeGroupAdapter) {
				return ((AttributeBaseValueDataFactory.AttributeGroupAdapter)data)._attributeBaseValueList;
			}
			else {
				throw new RuntimeException("Nicht unterst�tze Data-Implementierung");
			}
		}
		return null;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf eine Konfigurationsanfrage empfangen wurde.
	 *
	 * @param telegram Telegramm mit der empfangenen Antwort aus der Konfiguration.
	 */
	public final void update(final ConfigTelegram telegram) {
		if(telegram != null) {
			synchronized(_pendingResponces) {
				_pendingResponces.add(telegram);
				_pendingResponces.notifyAll();
			}
		}
	}

	/**
	 * L�scht das Objekt, indem es ung�ltig gemacht wird. Dynamische System-Objekte werden sofort ung�ltig. Bereits g�ltige konfigurierende System-Objekte werden
	 * mit Aktivierung der n�chsten Konfigurationsversion ung�ltig. F�r historische Anfragen bleiben ung�ltige Objekte nach wie vor existent. Konfigurierende
	 * System-Objekte, die noch nie g�ltig waren werden durch diese Methode gel�scht und sind nicht mehr zugreifbar.
	 *
	 * @param object Objekt, dass gel�scht bzw. ung�ltig gesetzt werden soll.
	 *
	 * @return <code>true</code>, falls das Objekt gel�scht bzw. ung�ltig gesetzt wurde; <code>false</code> wenn die Operation nicht durchgef�hrt werden konnte.
	 */
	final boolean invalidate(DafSystemObject object) {
		ObjectInvalidateRequest telegram = new ObjectInvalidateRequest(_configurationTime, object.getId());
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_INVALIDATE_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							try {
								ObjectInvalidateAnswer objectInvalidateAnswer = (ObjectInvalidateAnswer)responce;
								if(telegram.getObjectId() == objectInvalidateAnswer.getObjectId()) {
									if(objectInvalidateAnswer.isInvalidated()) {
										if(object instanceof DafDynamicObject) {
											object.setState(DafSystemObject.OBJECT_DELETED);
											DafDynamicObject dynamicObject = (DafDynamicObject)object;
											dynamicObject.setNotValidSince(objectInvalidateAnswer.getConfigTime());
											if(updateInternalDataStructure(object) != object) {
												_debug
														.error("Es wurde ein Systemobjekt auf ung�ltig gesetzt, zu dessen Id ein anderes Objekt im Cache gewesen ist");
												Thread.dumpStack();
											}
										}
										return true;
									}
									else {
										return false;
									}
								}
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							return false;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Macht ein bereits als ung�ltig markiertes Objekt wieder g�ltig. Wenn ein Konfigurationsobjekt mit der Methode {@link DafSystemObject#invalidate} f�r eine
	 * zuk�nftige Konfigurationsversion als ung�ltig markiert wurde und diese Konfigurationsversion noch nicht aktiviert wurde, dann kann das Objekt durch Aufruf
	 * dieser Methode wieder g�ltig gemacht werden.
	 *
	 * @param object Objekt, dass wieder g�ltig gemacht werden soll.
	 *
	 * @return <code>true</code>, falls das Objekt wieder g�ltig gemacht werden konnte; <code>false</code> wenn die Operation nicht durchgef�hrt werden konnte.
	 */
	final boolean revalidate(DafSystemObject object) {
		ObjectRevalidateRequest telegram = new ObjectRevalidateRequest(_configurationTime, object.getId());
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_REVALIDATE_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							try {
								ObjectRevalidateAnswer objectRevalidateAnswer = (ObjectRevalidateAnswer)responce;
								if(telegram.getObjectId() == objectRevalidateAnswer.getObjectId()) {
									return objectRevalidateAnswer.isRevalidated();
								}
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							return false;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	/**
	 * Setzt den Namen eines Systemobjekts.
	 *
	 * @param object Systemobjekt, das umbenannt werden soll.
	 * @param name   Neuer Name des Objekts. Der leere String ("") wird als "kein Name" interpretiert.
	 *
	 * @return <code>true</code>, falls der Name ge�ndert werden konnte; <code>false</code> wenn die Operation nicht durchgef�hrt werden konnte.
	 *
	 * @see DafSystemObjectType#isNameOfObjectsPermanent
	 */
	final boolean setName(DafSystemObject object, String name) {
		ObjectSetNameRequest telegram = new ObjectSetNameRequest(_configurationTime, object.getId(), name);
		String info = Integer.toString(telegram.hashCode());
		telegram.setInfo(info);
		_configurationManager.sendConfigData(_readBaseSubscriptionInfo, telegram);

		// Waiting for Answer
		ConfigTelegram responce = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_pendingResponces) {
					if(_connectionClosed) {
						throw new RuntimeException("Verbindung zum Datenverteiler wurde geschlossen");
					}
					_pendingResponces.wait(sleepTime);
					if(sleepTime < 1000) {
						sleepTime *= 2;
					}

					ListIterator _iterator = _pendingResponces.listIterator(_pendingResponces.size());
					while(_iterator.hasPrevious()) {
						responce = (ConfigTelegram)_iterator.previous();
						if((responce != null) && (responce.getType() == ConfigTelegram.OBJECT_SET_NAME_ANSWER_TYPE) && (info.equals(responce.getInfo()))) {
							_iterator.remove();
							try {
								ObjectSetNameAnswer objectSetNameAnswer = (ObjectSetNameAnswer)responce;
								if(telegram.getObjectId() == objectSetNameAnswer.getObjectId()) {
									return objectSetNameAnswer.isNameSet();
								}
							}
							catch(ClassCastException ex) {
								ex.printStackTrace();
							}
							return false;
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				break;
			}
		}
		throw new RuntimeException("Die Konfiguration antwortet nicht");
	}

	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg) {
		return getConfigurationData(objects.toArray(new SystemObject[objects.size()]), atg, _defaultConfigurationDataAspect);
	}

	public void updateName(final long objectId, final long typeId, final String newName) {
		// Typen sind immer konfigurationsobjekt.
		final DafDynamicObjectType dynamicType = (DafDynamicObjectType)getObjectFromCache(new Long(typeId));

		// Ist der Typ in diesem Datenmodell vorhanden ? Wenn nicht, dann gibt es auch keine Objekte und die Anfrage kann verworfen werden.
		if(dynamicType != null) {
			dynamicType.updateName(objectId, newName);
		}
	}

	public void updateNotValidSince(final long objectId, final long typeId, final long invalidTime) {
		final DafDynamicObjectType dynamicType = (DafDynamicObjectType)getObjectFromCache(new Long(typeId));

		// Ist der Typ in diesem Datenmodell vorhanden ? Wenn nicht, dann gibt es auch keine Objekte und die Anfrage kann verworfen werden.
		if(dynamicType != null) {
			dynamicType.updateNotValidSince(objectId, invalidTime);
		}
	}

	public void newDynamicObjectCreated(final long objectId, final long typeId) {
		final DafDynamicObjectType dynamicType = (DafDynamicObjectType)getObjectFromCache(new Long(typeId));

		// Ist der Typ in diesem Datenmodell vorhanden ? Wenn nicht, dann gibt es auch keine Objekte und die Anfrage kann verworfen werden.
		if(dynamicType != null) {
			dynamicType.updateObjectCreated(objectId);
		}
	}

	/**
	 * Gibt das SystemObjekt mit der angegebenen Objekt-Id aus dem Cache zur�ck. Befindet sich das Objekt nicht im Cache wird <code>null</code> zur�ckgegeben.
	 *
	 * @param objectId Id des Objekts
	 *
	 * @return SystemObjekt aus dem Cache oder <code>null</code>, falls es sich nicht im Cache befindet.
	 */
	DafSystemObject getObjectFromCache(final long objectId) {
		synchronized(_systemObjectsById) {
			return _systemObjectsById.get(new Long(objectId));
		}
	}

	/** Identifikation eines konfigurierenden Datensatzes, die das zugeh�rige Systemobjekt und die zugeh�rige Attributgruppenverwendung speichert. */
	static class ConfigDataKey {

		/** Hashcode dieser Identifikation */
		private final int _hashCode;

		/** Zugeh�riges Systemobjekt dieser Identifikation */
		public final SystemObject _object;

		/** Zugeh�rige Attribugruppenverwendung dieser Identifikation */
		public final AttributeGroupUsage _atgUsage;

		/**
		 * Erzeugt eine neue Identifikation
		 *
		 * @param object   Zugeh�riges Systemobjekt der neuen Identifikation
		 * @param atgUsage Zugeh�rige Attribugruppenverwendung der neuen Identifikation
		 */
		public ConfigDataKey(SystemObject object, AttributeGroupUsage atgUsage) {
			_object = object;
			_atgUsage = atgUsage;
			_hashCode = _object.hashCode() ^ _atgUsage.hashCode();
		}

		/**
		 * Gibt den Hashcode dieser Identifikation zur�ck. {@inheritDoc}
		 *
		 * @see #equals(Object)
		 * @see Hashtable
		 */
		@Override
		public int hashCode() {
			return _hashCode;
		}

		/**
		 * Vergleicht diese Identifikation mit einer anderen auf Gleichheit. {@inheritDoc}
		 *
		 * @see #hashCode()
		 * @see Hashtable
		 */
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof ConfigDataKey)) {
				return false;
			}
			final ConfigDataKey otherKey = (ConfigDataKey)other;
			// "==" Operator ist hier zul�ssig, weil es von jedem SystemObjekt h�chstens eine Instanziierung gibt.
			return _object == otherKey._object && _atgUsage == otherKey._atgUsage;
		}

		/**
		 * Ermittelt einen beschreibenden Text f�r diese Identifikation. Das genaue Format ist nicht festgelegt und kann sich �ndern.
		 *
		 * @return Beschreibender Text f�r diese Identifikation.
		 */
		@Override
		public String toString() {
			return "ConfigDataKey{" + _object + ", " + _atgUsage;
		}
	}

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppenverwendung f�r mehrere Objekte zur�ck. Die zur�ckgelieferten Datens�tze werden auch lokal
	 * zwischengespeichert und k�nnen mit der Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage
	 * abgefragt werden. Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verz�gerungszeiten eingesetzt
	 * werden.
	 *
	 * @param objects Liste der {@link de.bsvrz.dav.daf.main.config.SystemObject Systemobjekten} der gew�nschten konfigurierenden Datens�tze.
	 * @param usage   Attributgruppenverwendung der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Im Array enth�lt f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppenverwendung
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroupUsage usage) {
		return getConfigurationData(objects.toArray(new SystemObject[objects.size()]), usage);
	}

	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg, Aspect asp) {
		return getConfigurationData(objects.toArray(new SystemObject[objects.size()]), atg, asp);
	}

	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg) {
		return getConfigurationData(objects, atg, _defaultConfigurationDataAspect);
	}

	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg, Aspect aspect) {
		AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(aspect);
		if(attributeGroupUsage == null) {
			throw new IllegalArgumentException(
					"Keine Attributgruppenverwendung f�r Attributgruppe " + atg.getPidOrNameOrId() + " und Aspekt " + aspect.getPidOrNameOrId() + " gefunden"
			);
		}
		if(!attributeGroupUsage.isConfigurating()) {
			throw new IllegalArgumentException("Attributgruppenverwendung ist nicht f�r konfigurierende Datens�tze vorgesehen");
		}
		return getConfigurationData(objects, attributeGroupUsage);
	}

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppenverwendung f�r mehrere Objekte zur�ck. Die Methode sendet eine Konfiguationsanfrage an die
	 * Konfiguration um die noch nicht im Zwischenspeicher vorhandenen Datens�tze zu laden. Die zur�ckgelieferten Datens�tze werden lokal zwischengespeichert
	 * und k�nnen mit der Methode {@link de.bsvrz.dav.daf.main.config.SystemObject#getConfigurationData} ohne weitere Konfigurationsanfrage abgefragt werden.
	 * Die Methode kann somit zur Minimierung der Anzahl von Konfigurationsanfragen und den damit verbundenen Verz�gerungszeiten eingesetzt werden.
	 *
	 * @param objects Array mit den {@link de.bsvrz.dav.daf.main.config.SystemObject Systemobjekten} der gew�nschten konfigurierenden Datens�tze.
	 * @param usage   Attributgruppenverwendung der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Im Array existiert f�r jedes Element des Parameters <code>objects</code> ein
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppenverwendung
	 *         Kombination hat.
	 */
	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroupUsage usage) {
		Data[] result = new Data[objects.length];
		ArrayList<Integer> originalPositions = new ArrayList<Integer>();
		ArrayList<SystemObject> remoteObjects = new ArrayList<SystemObject>();

		for(int i = 0; i < objects.length; i++) {
			final SystemObject object = objects[i];
			final ConfigDataKey configDataKey = new ConfigDataKey(object, usage);
			Object o = _configDataValuesTable.get(configDataKey);
			if(o != null) {
				// Objekt ist bereits im lokalen Cache, also einfach auslesen.
				if(o == _noDataMarker) {
					result[i] = null;
				}
				else {
					result[i] = (Data)o;
				}
			}
			else {
				// Objekt muss per KonfigurationsAnfrage geholt werden, dazu in Liste einf�gen
				remoteObjects.add(object);
				// und Merken, wo das Objekt im Array hingeh�rt
				originalPositions.add(i);
			}
		}

		//Jetzt falls n�tig KonfigurationsAnfrage f�r alle Objekte in der Liste senden
		if(remoteObjects.size() > 0) {
			final Data[] datas = getConfigurationDataRemote(remoteObjects, usage);
			//und  in das result-Array einsortieren
			for(int i = 0; i < datas.length; i++) {
				final Integer originalPosition = originalPositions.get(i);
				result[originalPosition] = datas[i];
			}
		}
		return result;
	}

	/**
	 * Liefert die konfigurierenden Datens�tze einer Attributgruppenverwendung f�r mehrere Objekte zur�ck. Dies ist eine Hilfsfunktion zu
	 * <code>getConfigurationData</code>, die im Gegensatz zu dieser keine Daten aus dem Cache liest (wohl aber welche hineinschreibt).
	 *
	 * @param objects Array mit den {@link de.bsvrz.dav.daf.main.config.SystemObject Systemobjekten} der gew�nschten konfigurierenden Datens�tze.
	 * @param usage   Attributgruppenverwendung der gew�nschten Datens�tze.
	 *
	 * @return Array mit den gew�nschten konfigurierenden Datens�tzen. Im Array existiert f�r jedes Element des Parameters <code>objects</code> einen
	 *         korrespondierender konfigurierender Datensatz oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppenverwendung
	 *         Kombination hat.
     */
    private Data[] getConfigurationDataRemote(List<SystemObject> objects, AttributeGroupUsage usage) {
		Data[] datas = new Data[objects.size()];
		ConfigurationRequester requester = getRequester();
		AttributeGroup atg = usage.getAttributeGroup();
		try {
			byte[][] propertiesDataBytesArray = requester.getConfigurationData(objects.toArray(new SystemObject[]{}), usage);
			for(int i = 0; i < propertiesDataBytesArray.length; i++) {
				ConfigDataKey configDataKey = new ConfigDataKey(objects.get(i), usage);
				byte[] bytes = propertiesDataBytesArray[i];
				if(bytes == null) {
					datas[i] = null;
					_configDataValuesTable.put(configDataKey, _noDataMarker);
				}
				else {
					try {
						final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
						final Deserializer deserializer = SerializingFactory.createDeserializer(2, in);
						final Data data = deserializer.readData(atg).createUnmodifiableCopy();
						_configDataValuesTable.put(configDataKey, data);
						datas[i] = data;
					}
					catch(Exception ex) {
						final String errorMessage = "Der konfigurierende Datensatz f�r das Objekt " + objects.get(i) + " und der Attributgruppenverwendung " + usage
						                            + " konnte nicht deserialisiert werden";
						_debug.warning(errorMessage, ex);
						throw new RuntimeException(errorMessage, ex);
					}
				}
			}
			return datas;
		}
		catch(RequestException e) {
			String message =
					"Fehler bei der Konfigurationsanfrage nach konfigurierenden Datens�tzen f�r " + usage + " und die Objekte " + Arrays.asList(objects);
			throw new RuntimeException(message, e);
		}
	}

	public short getActiveVersion(ConfigurationArea configurationArea) {
		final ConfigurationRequester requester = getRequester();

		try {
			return requester.getActiveVersion(configurationArea);
		}
		catch(RequestException e) {
			e.printStackTrace();
			final String msg =
					"Fehler bei einer Konfigurationsanfrage nach der aktiven Version eines Konfigurationsbereichs, Bereich: " + configurationArea.getPid();
			_debug.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Liefert einen konfigurierenden Datensatz eines Objekts zur�ck. Als Aspekt des gew�nschten Datensatzes wird "<code>asp.eigenschaften</code>" angenommen.
	 *
	 * @param object SystemObject des gew�nschten Datensatzes.
	 * @param atg    Attributgruppe des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppe oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen Attributgruppe
	 *         hat.
	 */
	Data getConfigurationData(SystemObject object, AttributeGroup atg) {
		AttributeGroupUsage attributeGroupUsage = atg.getAttributeGroupUsage(_defaultConfigurationDataAspect);
		return getConfigurationData(object, attributeGroupUsage);
	}

	/**
	 * Liefert einen konfigurierenden Datensatz eines Objekts zur�ck.
	 *
	 * @param object   SystemObject des gew�nschten Datensatzes.
	 * @param atgUsage Attributgruppenverwendung des gew�nschten Datensatzes.
	 *
	 * @return Konfigurierender Datensatz der angegebenen Attributgruppenverwendung oder <code>null</code>, wenn das Objekt keinen Datensatz der angegebenen
	 *         Attributgruppenverwendung hat.
	 */
	Data getConfigurationData(SystemObject object, AttributeGroupUsage atgUsage) {
		return getConfigurationData(new SystemObject[]{object}, atgUsage)[0];
	}

	/**
	 * Gibt die Objekt-Id des Konfigurationsverantwortlichen zur�ck.
	 *
	 * @return Die Objekt-Id des Konfigurationsverantwortlichen
	 */
	public final long getConfigurationAuthorityId() {
		return _configurationAuthorityId;
	}

	/**
	 * Ordnet dem Konfigurationsobjekt eine weitere Menge zu. Die Zuordnung wird erst mit der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @param configurationObject Objekt, dem eine neue Menge zugeordnet werden soll.
	 * @param set                 Menge, die dem Konfigurationsobjekt zugeordnet werden soll.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration f�hrt den Auftrag nicht aus. Ein Grund w�re zum Beispiel, dass die Konfiguration nicht der
	 *                                      Konfigurationsverantwortliche f�r dieses Objekt ist.
	 */
	final void addSet(DafConfigurationObject configurationObject, ObjectSet set) throws ConfigurationChangeException {
		try {
			getRequester().editConfigurationSet(configurationObject, set, true);
		}
		catch(RequestException e) {
			e.printStackTrace();
			final String msg = "Fehler bei einer Konfigurationsanfrage, betroffenes Objekt und Menge: " + configurationObject.getPidOrNameOrId() + " " + set;
			_debug.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Entfernt die Zuordnung von diesem Konfigurationsobjekt zu einer Menge. Die �nderung wird erst mit der n�chsten Konfigurationsversion g�ltig.
	 *
	 * @param configurationObject Objekt, von dem eine Menge entfernt werden soll
	 * @param set                 Menge, die entfernt werden soll.
	 *
	 * @throws ConfigurationChangeException Die Konfiguration f�hrt den Auftrag nicht aus. Ein Grund w�re zum Beispiel, dass die Konfiguration nicht der
	 *                                      Konfigurationsverantwortliche f�r dieses Objekt ist.
	 */
	final void removeSet(DafConfigurationObject configurationObject, ObjectSet set) throws ConfigurationChangeException {
		try {
			getRequester().editConfigurationSet(configurationObject, set, false);
		}
		catch(RequestException e) {
			e.printStackTrace();
			final String msg = "Fehler bei einer Konfigurationsanfrage, betroffenes Objekt und Menge: " + configurationObject.getPidOrNameOrId() + " " + set;
			_debug.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Bestimmt die zu einem vorgegebenen Zeitpunkt zur Zusammenstellung geh�renden Elemente.
	 *
	 * @param type Type, der gepr�ft werden soll
	 * @param time Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den w�hrend des gesamten Zeitbereichs zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List<SystemObject> getElementsOfType(DafSystemObjectType type, long time) {
		final Collection<SystemObjectType> typs = new ArrayList<SystemObjectType>();
		typs.add(type);
		return castInterfaceToClass(getObjects(null, typs, ObjectTimeSpecification.valid(time)));
	}

	/**
	 * Bestimmt die Elemente, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs zur Zusammenstellung geh�rt haben.
	 *
	 * @param type      die Id des Typs.
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den w�hrend des gesamten Zeitbereichs zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List<SystemObject> getElementsOfTypeInPeriod(DafSystemObjectType type, long startTime, long endTime) {
		final Collection<SystemObjectType> typs = new ArrayList<SystemObjectType>();
		typs.add(type);
		return castInterfaceToClass(getObjects(null, typs, ObjectTimeSpecification.validInPeriod(startTime, endTime)));
	}

	/**
	 * Bestimmt die Elemente, die w�hrend des gesamten angegebenen Zeitbereichs zur Zusammenstellung geh�rt haben.
	 *
	 * @param type      die Id des Typs.
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den w�hrend des gesamten Zeitbereichs zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List<SystemObject> getElementsOfTypeDuringPeriod(DafSystemObjectType type, long startTime, long endTime) {
		final Collection<SystemObjectType> typs = new ArrayList<SystemObjectType>();
		typs.add(type);
		return castInterfaceToClass(getObjects(null, typs, ObjectTimeSpecification.validDuringPeriod(startTime, endTime)));
	}

	/**
	 * Diese Methode castet eine Collection, die Objekte enth�lt, die ein Interface implementieren, auf die konkrete Klasse.
	 *
	 * @param systemObjects Collection, deren Elemente gecastet werden m�ssen
	 *
	 * @return Liste mit gecasteten Objekten
	 */
	private List<SystemObject> castInterfaceToClass(Collection<SystemObject> systemObjects) {
		final List<SystemObject> result = new ArrayList<SystemObject>(systemObjects.size());

		for(SystemObject systemObject : systemObjects) {
			result.add(systemObject);
		}
		return result;
	}

	/**
	 * Bestimmt die zu einem vorgegebenen Zeitpunkt zur Mengenzusammenstellung geh�renden Elemente.
	 *
	 * @param set  Menge, aus der die Objekte angefordert werden sollen
	 * @param time Zeitpunkt in Millisekunden seit 1970
	 *
	 * @return Liste mit den zum angegebenen Zeitpunkt zur Mengenzusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElements(DafObjectSet set, long time) {

		final ObjectTimeSpecification timeSpec = ObjectTimeSpecification.valid(time);

		try {
			return castInterfaceToClass(getRequester().getSetElements(set, timeSpec));
		}
		catch(RequestException e) {
			throw new IllegalStateException(
					"Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId() + " Zeit: " + timeSpec
			);
		}
	}

	/**
	 * Bestimmt die Elemente, die an mindestens einem Zeitpunkt des angegebenen Zeitbereichs zur Mengenzusammenstellung geh�rt haben.
	 *
	 * @param set       Menge, aus der die Objekte angefordert werden sollen
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den zu mindestens einem Zeitpunkt des Zeitbereichs zur Mengenzusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsInPeriod(DafObjectSet set, long startTime, long endTime) {
		final ObjectTimeSpecification timeSpec = ObjectTimeSpecification.validInPeriod(startTime, endTime);

		try {
			return castInterfaceToClass(getRequester().getSetElements(set, timeSpec));
		}
		catch(RequestException e) {
			throw new IllegalStateException(
					"Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId() + " Zeit: " + timeSpec
			);
		}
	}

	/**
	 * Bestimmt die Elemente, die w�hrend des gesamten angegebenen Zeitbereichs zur Mengenzusammenstellung geh�rt haben.
	 *
	 * @param set       Menge, aus der die Objekte angefordert werden sollen
	 * @param startTime Erster Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 * @param endTime   Letzter Zeitpunkt des Zeitbereichs in Millisekunden seit 1970.
	 *
	 * @return Liste mit den w�hrend des gesamten Zeitbereichs zur Mengenzusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsDuringPeriod(DafObjectSet set, long startTime, long endTime) {
		final ObjectTimeSpecification timeSpec = ObjectTimeSpecification.validDuringPeriod(startTime, endTime);

		try {
			return castInterfaceToClass(getRequester().getSetElements(set, timeSpec));
		}
		catch(RequestException e) {
			throw new IllegalStateException(
					"Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId() + " Zeit: " + timeSpec
			);
		}
	}

	/**
	 * Bestimmt die nach Aktivierung der n�chsten Konfigurationsversion zur Mengenzusammenstellung geh�renden Elemente.
	 *
	 * @param set Menge, aus der die Objekte angefordert werden sollen
	 *
	 * @return Liste mit den in der n�chsten Konfigurationsversion zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsInNextVersion(DafObjectSet set) {
		try {
			return castInterfaceToClass(getRequester().getSetElementsInNextVersion(set));
		}
		catch(RequestException e) {
			throw new IllegalStateException("Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId());
		}
	}

	/**
	 * Bestimmt die in einer bestimmten Konfigurationsversion zur Mengenzusammenstellung geh�renden Elemente.
	 *
	 * @param set     Menge, in der die Objekte betrachtet werden sollen
	 * @param version Version der Konfiguration
	 *
	 * @return Liste mit den in der angegebenen Version zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsInVersion(DafObjectSet set, short version) {
		try {
			return castInterfaceToClass(getRequester().getSetElementsInVersion(set, version));
		}
		catch(RequestException e) {
			throw new IllegalStateException("Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId());
		}
	}

	/**
	 * Bestimmt die Elemente, die in allen Konfigurationsversionen eines vorgegebenen Versionsbereichs zur Mengenzusammenstellung geh�rt haben.
	 *
	 * @param set         Menge, die die Elemente enth�lt, die betrachtet werden sollen.
	 * @param fromVersion Erste Version des Bereichs von Konfigurationversionen
	 * @param toVersion   Letzte Version des Bereichs von Konfigurationversionen
	 *
	 * @return Liste mit den in allen Version des Bereichs zur Mengenzusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsInAllVersions(DafObjectSet set, short fromVersion, short toVersion) {
		try {
			return castInterfaceToClass(getRequester().getSetElementsInAllVersions(set, fromVersion, toVersion));
		}
		catch(RequestException e) {
			throw new IllegalStateException("Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId());
		}
	}

	/**
	 * Bestimmt die Elemente, die in mindestens einer Konfigurationsversion eines vorgegebenen Versionsbereichs zur Mengenzusammenstellung geh�rt haben.
	 *
	 * @param set         Menge, aus der die Objekte angefordert werden sollen
	 * @param fromVersion Erste Version des Bereichs von Konfigurationversionen
	 * @param toVersion   Letzte Version des Bereichs von Konfigurationversionen
	 *
	 * @return Liste mit den in mindestens einer Version des Bereichs zur Zusammenstellung geh�renden System-Objekten.
	 */
	final List getSetElementsInAnyVersions(DafObjectSet set, short fromVersion, short toVersion) {
		try {
			return castInterfaceToClass(getRequester().getSetElementsInAnyVersions(set, fromVersion, toVersion));
		}
		catch(RequestException e) {
			throw new IllegalStateException("Ein Telegramm konnte nicht zur Konfiguration geschickt werden, Objekttyp: " + set.getPidOrNameOrId());
		}
	}

	/**
	 * �ndert einen konfigurierenden Datensatz eines Objekts.
	 *
	 * @param systemObject Systemobjekt an dem der Datensatz ge�ndert werden soll.
	 * @param atgUsage     Attributgruppenverwendung des zu �ndernden Datensatzes
	 * @param data         Der neue Datensatz. Wird <code>null</code> angegeben, wird der Datensatz am Objekt gel�scht.
	 *
	 * @throws ConfigurationChangeException Wenn der Datensatz nicht ge�ndert werden konnte.
	 */
	void setConfigurationData(SystemObject systemObject, AttributeGroupUsage atgUsage, Data data) throws ConfigurationChangeException {

		try {
			final ConfigurationRequester requester = getRequester();

			// Den Datensatz in ein byte-Array umwandeln.
			final byte[] dataAsByteArray;
			if(data != null) {
				// Pr�fen ob das Data-Objekt einen ganzen Datensatz darstellt
				if(data.getAttributeType() != null) {
					throw new IllegalArgumentException(
							"Der zu speichernde Datensatz stellt keinen ganzen Datensatz dar sondern nur einen Teildatensatz vom Typ "
							+ data.getAttributeType().getPid()
					);
				}
				// Pr�fen ob das Data-Objekt ein Datensatz der passenden Attributgruppe ist
				if(!data.getName().equals(atgUsage.getAttributeGroup().getPid())) {
					throw new IllegalArgumentException(
							"Die Attributgruppe des zu speichernden Data-Objekts (" + data.getName() + ") entspricht nicht der angegebenen Attributgruppe: "
							+ atgUsage.getAttributeGroup().getPid() + ")"
					);
				}
				// Pr�fen, ob alle Attribute im Datensatz definiert sind
				if(!data.isDefined()) {
					// Der Datensatz kann nicht verschickt werden, weil mindestens ein Attribut den "undefiniert Wert" enth�lt
					throw new IllegalArgumentException("Der zu speichernde Datensatz enth�lt mindestens ein Attribut, dass nicht definiert ist: " + data);
				}
				final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				final Serializer serializer = SerializingFactory.createSerializer(byteArrayStream);
				serializer.writeData(data);
				dataAsByteArray = byteArrayStream.toByteArray();
			}
			else {
				// Das leere byte-Array wird auf der Gegenseite als <code>null</code> interpretiert.
				dataAsByteArray = new byte[0];
			}
			requester.setConfigurationData(atgUsage, systemObject, dataAsByteArray);
			// Nach erfolgreicher �nderung des Datensatzes in der Konfiguration wird der neue Datensatz in den lokalen Cache eingetragen
			ConfigDataKey configDataKey = new ConfigDataKey(systemObject, atgUsage);
			if(data != null) {
				final Data dataCopy = data.createUnmodifiableCopy();
				_configDataValuesTable.put(configDataKey, dataCopy);
			}
			else {
				_configDataValuesTable.put(configDataKey, _noDataMarker);
			}
		}
		catch(IOException e) {
			// Fehler bei der Anfrage
			_debug.error("Anfrage kann nicht serialisiert werden", e);
			throw new IllegalStateException("Anfrage kann nicht serialisiert werden: " + e);
		}
		catch(RequestException e) {
			// Fehler beim versand der Anfrage. Etwas stimmt mit der Verbindung zum Datenverteiler nicht.
			final String errorMessage = "Kommunikationsproblem mit dem Datenverteiler, Attributgruppenverwendung: " + atgUsage;
			_debug.error(errorMessage, e);
			throw new IllegalStateException(errorMessage + e);
		}
	}

	public AttributeGroupUsage getAttributeGroupUsage(final long usageIdentification) {
		if(usageIdentification == AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST) {
			return _configurationReadRequestUsage;
		}
		if(usageIdentification == AttributeGroupUsageIdentifications.CONFIGURATION_READ_REPLY) {
			return _configurationReadReplyUsage;
		}
		if(usageIdentification == AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST) {
			return _configurationWriteRequestUsage;
		}
		if(usageIdentification == AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REPLY) {
			return _configurationWriteReplyUsage;
		}
		final SystemObject object = getObject(usageIdentification);
		if(object instanceof AttributeGroupUsage) {
			return (AttributeGroupUsage)object;
		}
		return null;
	}

	/** Klasse zur asynchronen Benachrichtigung der Listener f�r �nderungen der Elemente von dynamischen Zusammenstellungen.*/
	private class NotifyingMutableCollectionChangeListener implements MutableCollectionChangeListener {

		/** Thread f�r die asynchrone Verarbeitung */
		private Thread _asyncNotifierThread;

		/** Queue in der Benachrichtigungsauftr�ge zwischengespeichert werden. */
		private UnboundedQueue<NotificationObject> _notificationQueue = new UnboundedQueue<NotificationObject>();

		public NotifyingMutableCollectionChangeListener() {
			_asyncNotifierThread = new Thread(new AsyncNotifier(), "NotifyingMutableCollectionChangeListener");
			_asyncNotifierThread.setDaemon(true);
		}

		public void collectionChanged(
				MutableCollection mutableCollection, short simVariant, List<SystemObject> addedElements, List<SystemObject> removedElements) {
			_notificationQueue.put(new NotificationObject(mutableCollection, simVariant, addedElements, removedElements));
		}

		/** Startet die asynchrone Verarbeitung */
		public void start() {
			_asyncNotifierThread.start();
		}

		/** Beendet die asynchrone Verarbeitung */
		public void stop() {
			_asyncNotifierThread.interrupt();
		}

		/** Enth�lt die run-Methode des Threads zur asynchronen Verarbeitung. */
		public class AsyncNotifier implements Runnable {

			public void run() {
				while(!Thread.interrupted()) {
					try {
						final NotificationObject notificationObject = _notificationQueue.take();
						collectionChanged(notificationObject._mutableCollection, notificationObject._simVariant, notificationObject._addedElements, notificationObject._removedElements);
					}
					catch(RuntimeException e) {
						_debug.warning("Fehler bei der asynchronen Benachrichtigung bzgl. �nderungen von dynamischen Mengen und dynamischen Typen", e);
					}
					catch(InterruptedException e) {
						return;
					}
				}
			}

			/** Benachrichtigt die zugeh�rige dynamische Menge bzw. den dynamischen Typ, dass sich �nderungen an den Elementen ergeben haben. */
			public void collectionChanged(
					MutableCollection mutableCollection, short simVariant, List<SystemObject> addedElements, List<SystemObject> removedElements) {
				if(mutableCollection instanceof DafMutableSet) {
					DafMutableSet dafMutableSet = (DafMutableSet)mutableCollection;
					dafMutableSet.collectionChanged(simVariant, addedElements, removedElements);
				}
				else if(mutableCollection instanceof DafDynamicObjectType) {
					DafDynamicObjectType dafDynamicObjectType = (DafDynamicObjectType)mutableCollection;
					dafDynamicObjectType.collectionChanged(simVariant, addedElements, removedElements);
				}
				else {
					_debug.warning("Aktualisierung der MutableCollection " + mutableCollection +
					               " kann nicht verarbeitet werden, weil das Objekt einen unbekannten Typ hat");
				}
			}
		}

	}

	/** Speichert die f�r eine asynchrone Benachrichtigung erforderlichen Parameter. */
	private static class NotificationObject {

		private final MutableCollection _mutableCollection;

		private final short _simVariant;

		private final List<SystemObject> _addedElements;

		private final List<SystemObject> _removedElements;

		public NotificationObject(
				final MutableCollection mutableCollection,
				final short simVariant,
				final List<SystemObject> addedElements,
				final List<SystemObject> removedElements) {

			_mutableCollection = mutableCollection;
			_simVariant = simVariant;
			_addedElements = addedElements;
			_removedElements = removedElements;
		}
	}
}
