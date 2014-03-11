/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.Data;
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
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.Pid;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.ConfigurationControl;
import de.bsvrz.dav.daf.main.config.management.UserAdministration;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResult;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntry;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntryType;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.FixableConsistencyCheckResultEntry;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileBackupTask;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileManager;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationAreaTime;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationFileManager;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigurationObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformation;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.consistencycheck.ConsistencyCheck;
import de.bsvrz.puk.config.main.consistencycheck.KindOfConsistencyCheck;
import de.bsvrz.puk.config.main.importexport.ConfigurationExport;
import de.bsvrz.puk.config.main.importexport.ConfigurationImport;
import de.bsvrz.puk.config.main.managementfile.ConfigurationAreaManagementInfo;
import de.bsvrz.puk.config.main.managementfile.ConfigurationManagementFile;
import de.bsvrz.puk.config.main.managementfile.ManagementFile;
import de.bsvrz.puk.config.main.managementfile.VersionInfo;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Implementierung des Datenmodells auf Seiten der Konfiguration zum Zugriff auf die Konfigurationsdaten. Die {@link
 * de.bsvrz.puk.config.main.managementfile.ConfigurationManagementFile Verwaltungsdaten} und die {@link ConfigurationFileManager Konfigurationsdaten} werden
 * hier zusammengeführt und entsprechend des {@link DataModel Datenmodells} zur Verfügung gestellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11641 $
 * @see DataModel
 */
public class ConfigDataModel implements DataModel, ConfigurationControl {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Zugriff auf die Verwaltungsdaten der Konfiguration. */
	private final ConfigurationManagementFile _managementFile;

	/** Die Verwaltungsdatei der Konfiguration. */
	private final File _adminFile;

	/** Zugriff auf die Konfigurationsdaten. */
	private final ConfigurationFileManager _configurationFileManager;

	/** Speichert die Basistypen der Konfiguration. */
	private List<SystemObjectType> _baseTypes;

	/** Speichert die größte Objekt-Id, die in einem Konfigurationsbereich vorkam, der vom Verantwortlichen der Konfiguration verändert werden darf. */
	private long _nextObjectId = -1;

	/** Attribugruppenverwendung für Konfigurationsleseanfragen */
	private AttributeGroupUsage _configurationReadRequestUsage;

	/** Attribugruppenverwendung für Konfigurationsleseantworten */
	private AttributeGroupUsage _configurationReadReplyUsage;

	/** Attribugruppenverwendung für Konfigurationsschreibanfragen */
	private AttributeGroupUsage _configurationWriteRequestUsage;

	/** Attribugruppenverwendung für Konfigurationsschreibantworten */
	private AttributeGroupUsage _configurationWriteReplyUsage;

	/** Kodierung des Konfigurationsverantwortlichen der Konfiguration */
	private long _authorityCoding;

	/** Der Konfigurationsverantwortliche der Konfiguration. */
	private ConfigurationAuthority _configurationAuthority;

	/** Flag, mit dem die Konsistenzprüfung entscheiden soll, ob doppelte Pids in verschiedenen Konfigurationsbereichen erlaubt sind. */
	private boolean _allowDoublePids = false;

	private Set<ConfigMutableSet> _dirtyMutableSets = Collections.synchronizedSet(new HashSet<ConfigMutableSet>());

	private boolean _ignoreDependencyErrorsInConsistencyCheck = false;

	private File _backupBaseDirectory = null;

	private ConfigAuthentication _userManagement = null;

	private final DynamicObjectTypePublisher _dynamicObjectTypePublisher = new DynamicObjectTypePublisher();

	/**
	 * Erzeugt das Datenmodell der Konfiguration.
	 *
	 * @param adminFile Datei mit den Verwaltungsdaten der Konfiguration oder leere Datei.
	 */
	public ConfigDataModel(File adminFile) {
		this(adminFile, false);
	}

	/**
	 * Erzeugt das Datenmodell der Konfiguration.
	 *
	 * @param adminFile Datei mit den Verwaltungsdaten der Konfiguration oder leere Datei.
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *                  Flag zum ignorieren fehlender Abhängigkeiten zwischen Konfigurationsbereichen
	 */
	public ConfigDataModel(File adminFile, boolean ignoreDependencyErrorsInConsistencyCheck) {
		_ignoreDependencyErrorsInConsistencyCheck = ignoreDependencyErrorsInConsistencyCheck;

		// Falls es im Konstruktor zu einem Fehler kommt, wird die close()-Methode aufgerufen,
		// damit alle lock-Dateien, die bisher erzeugt wurden, wieder gelöscht werden.
		try {
			long startTime = System.currentTimeMillis();    // Startzeit gibt an, wann das Datenmodell eingeladen wird

			// Erstellt ein Objekt für den Zugriff auf die Verwaltungsdaten.
			_adminFile = adminFile;    // wird nur für toString benötigt
			_managementFile = new ManagementFile(adminFile);

			// Prüfung der für die Konfiguration selbst notwendigen Konfigurationsbereiche 
			checkRequiredArea("kb.metaModellGlobal", 12);
			checkRequiredArea("kb.systemModellGlobal", 19);
			checkRequiredArea("kb.fachModellGlobal", 7);

			// Erstellt ein Objekt für den Zugriff auf die Konfigurationsbereiche mit deren Systemobjekten.
			_configurationFileManager = new ConfigFileManager();

			// Liste von Pids von Konfigurationsbereichen, die in eine neue Version überführt wurden
			List<String> configurationAreaWithNewActivatedVersion = new ArrayList<String>();

			// ermittelt alle in den Verwaltungsdaten angegebenen Konfigurationsbereiche
			final List<ConfigurationAreaManagementInfo> managementInfos = _managementFile.getAllConfigurationAreaManagementInfos();
			_debug.finer("Anzahl der Konfigurationsbereiche, die in der Verwaltungsdatei stehen", managementInfos.size());

			// Speichert zu jedem Bereich die Pid als String und die Version(short), in der der Bereich benutzt werden soll.
			// Dieser Zwischenschritt muss gemacht werden (den Bereich über die Pid identifizieren), da der Bereich erst am Ende
			// des Konstruktors zur Verfügung steht.
			final Map<String, Short> areasAndVersionsMap = new LinkedHashMap<String, Short>();

			for(ConfigurationAreaManagementInfo managementInfo : managementInfos) {

				if(managementInfo.isNewVersionActivated()) {
					// falls eine neue Version aktiviert wurde - diesen Konfigurationsbereich merken
					configurationAreaWithNewActivatedVersion.add(managementInfo.getPid());
				}
				try {
					final String areaPid = managementInfo.getPid();
					final short activeVersion = managementInfo.getActiveVersion().getVersion();
					_configurationFileManager.addAreaFile(
							areaPid, managementInfo.getDirectory(), activeVersion, managementInfo.getVersions()
					);
					areasAndVersionsMap.put(areaPid, activeVersion);
				}
				catch(Exception ex) {
					final String errorMessage =
							"Der Konfigurationsbereich mit der Pid " + managementInfo.getPid() + " konnte der Konfiguration nicht hinzugefügt werden.";
					_debug.error(errorMessage, ex);
					throw new IllegalStateException(errorMessage, ex);
				}
				manualGc();
			}// for, über alle Bereiche der Datei

			// alle Konfigurationsbereiche, die eine neue Version erhalten haben, werden, wenn zulässig, reorganisiert.

			_debug.finer("Anzahl der Konfigurationsbereiche, die in eine neue Version überführt wurden", configurationAreaWithNewActivatedVersion.size());

			for(String pid : configurationAreaWithNewActivatedVersion) {
				final ConfigurationArea configurationArea = getAllConfigurationAreas().get(pid);
				ConfigurationAuthority configurationAuthority = configurationArea.getConfigurationAuthority();
				_debug.finest("KV Bereich: " + configurationAuthority.getPid() + " KV Konfiguration: " + _managementFile.getConfigurationAuthority());
				if(configurationAuthority.getPid().equals(_managementFile.getConfigurationAuthority())) {
					_debug.finest("Restrukturierung in folgendem Bereich erforderlich", configurationArea.getPidOrNameOrId());
					((ConfigAreaFile)_configurationFileManager.getAreaFile(pid)).initialVersionRestructure();
					manualGc();
				}
			}

			// Falls ein Konfigurationsbereich in eine neue Version überführt wurde, haben sich die Verwaltungsinfos geändert - deshalb müssen sie gesichert werden.
			if(configurationAreaWithNewActivatedVersion.size() > 0) {
				try {
					_managementFile.save();
				}
				catch(IOException ex) {
					final String errorMessage = "Verwaltungsdaten konnten nicht gespeichert werden";
					_debug.error(errorMessage, ex);
					throw new RuntimeException(errorMessage, ex);
				}
			}

			// nächste zu aktivierende Version festlegen
			for(ConfigurationAreaManagementInfo managementInfo : managementInfos) {

				ConfigurationAreaFile areaFile = _configurationFileManager.getAreaFile(managementInfo.getPid());
				if(areaFile != null) {

					ConfigurationArea configurationArea = getConfigurationArea(managementInfo.getPid());

					if(configurationArea == null) {
						configurationArea = getAllConfigurationAreas().get(managementInfo.getPid());
					}
					
					/* Der Bereich konnte nicht gefunden werden, dann ist configurationArea == null und es wird eine
					 * NullpointerException ausgelöst. Diese wird gefangen, dafür wird eine neue erzeugt, die eine
					 * hilfreiche Fehlermeldung enthält.
					 */
					short transferableVersion;
					short activatableVersion;
					try {
						transferableVersion = configurationArea.getTransferableVersion();
						activatableVersion = configurationArea.getActivatableVersion();
					}
					catch(NullPointerException e) {
						throw new RuntimeException(
								"Fehler beim Zugriff auf den Bereich " + managementInfo.getPid() +
								", bitte Groß-/Kleinschreibung der Pid in der Verwaltungsdatei prüfen.", e
						);
					}

					short modifiableVersion = configurationArea.getActiveVersion();
					if(transferableVersion > modifiableVersion) modifiableVersion = transferableVersion;
					if(activatableVersion > modifiableVersion) modifiableVersion = activatableVersion;
					modifiableVersion++;    // um eins erhöhen!!
					areaFile.setNextActiveVersion(modifiableVersion);
					//				_debug.info("Nächste zu aktivierende Version " + modifiableVersion + " des KB " + managementInfo.getPid());
				} // Falls die Bereichsdatei noch nicht existiert, muss auch keine Versionsnummer angegeben werden. Dies wird beim Erstellen des Bereichs gesetzt!
				manualGc();
			}

			// Überprüfung auf Eindeutigkeit der Kodierung des Konfigurationsverantwortlichen
			checkCodingOfConfigurationAuthority();

			_configurationReadRequestUsage = getAttributeGroup("atg.konfigurationsAnfrage").getAttributeGroupUsage(getAspect("asp.anfrage"));
			_configurationReadReplyUsage = getAttributeGroup("atg.konfigurationsAntwort").getAttributeGroupUsage(getAspect("asp.antwort"));
			_configurationWriteRequestUsage = getAttributeGroup("atg.konfigurationsSchreibAnfrage").getAttributeGroupUsage(getAspect("asp.anfrage"));
			_configurationWriteReplyUsage = getAttributeGroup("atg.konfigurationsSchreibAntwort").getAttributeGroupUsage(getAspect("asp.antwort"));

			// Enthält alle Bereiche und deren Versionen in den die Bereiche benutzt werden.
			final List<ConfigAreaAndVersion> areasAndVersions = new ArrayList<ConfigAreaAndVersion>();

			final Set<String> allAreasPid = areasAndVersionsMap.keySet();
			for(String oneAreaPid : allAreasPid) {
				final ConfigurationArea configArea = getConfigurationArea(oneAreaPid);

				// Zu einer Pid konnte kein Berich gefunden werden. Das kann vorkommen, wenn der Bereich zwar in der
				// Datei steht, aber gar nicht benutzt werden soll/kann.
				if(configArea != null) {
					areasAndVersions.add(
							new ConfigAreaAndVersion(
									configArea, areasAndVersionsMap.get(oneAreaPid)
							)
					);
				}
			}

			// Abhängigkeiten nur prüfen, wenn mindestens Version 9 des Metamodells vorliegt
			final ConfigurationArea metaModelArea = getConfigurationArea("kb.metaModellGlobal");
			if(metaModelArea != null && metaModelArea.getActiveVersion() >= 9) {
				// Alle Bereiche, die benutzt werden sollen, stehen mit der zu nutzenden Version zur Verfügung. Können
				// die Beziehungen zwischen den Bereichen aufgelöst werden ?
				// Wenn der Test positiv verläuft, muss kein weiterer Test auf Abhängigkeiten gemacht werden (bei Aktivierung, Übergabe, usw.),
				// da das geladene Datenmodell alle Bereiche im Zugriff hat, die benötigt werden um Abhängigkeiten aufzulösen.
				checkAreaDependency(areasAndVersions);
			}
			// Folgende Zeilen initialisieren die Liste ConfigSystemObjectType._allElements für alle dynamischen Typen.
			// Dies ist erforderlich, da dies ansonsten beim ersten Erzeugen eines Objekts gemacht wird und das neu erzeugte Elemente doppelt eingetragen wird.
			List<SystemObject> dynamicTypes = getType("typ.dynamischerTyp").getElements();
			for(SystemObject dynamicType : dynamicTypes) {
				((SystemObjectType)dynamicType).getElements();
			}

			_debug.info("Das Datenmodell wurde geladen. Dauer in Sekunden", ((System.currentTimeMillis() - startTime) / 1000));
		}
		catch(RuntimeException ex) {
			_debug.error("Ein Fehler ist beim Erzeugen des Datenmodells aufgetreten. Die Konfiguration wird beendet.", ex);
			close();
			throw new RuntimeException(ex);
		}
	}

	private void manualGc() {
		// Nachdem der Bereich in den Speicher geladen wurde, wird die GC erzwungen. Dies ist nötig, da das Programm
		// sehr viele externe Ressourcen anfordert, die nicht sofort freigegeben werden. Somit "stauen" sich diese Ressourcen im
		// Speicher und führen dann zu einer Fehlermeldung (entweder OutOfMemory oder "Not Enough Swap Space").
		// jh: manueller System.gc() ist eigentlich immer eine schlechte Idee, standardmäßig deaktiviert.
		if(System.getProperty("config.manual.gc") != null) System.gc();
	}

	private void checkRequiredArea(final String configurationAreaPid, final int recommendedVersion) {
		final short version = _managementFile.getConfigurationAreaManagementInfo(configurationAreaPid).getActiveVersion().getVersion();
		if(version <= 0) {
			throw new RuntimeException(
					"Der Bereich " + configurationAreaPid + " muss aktiviert sein. Empfohlen wird Version " + recommendedVersion + " oder neuer."
			);
		}
		if(version < recommendedVersion) {
			_debug.warning("Es wird empfohlen, den Bereich " + configurationAreaPid + " in Version " + recommendedVersion + " oder neuer zu aktivieren.");
		}
	}

	/**
	 * Diese Methode prüft, ob die Kodierung des Konfigurationsverantwortlichen dieser Konfiguration eindeutig ist. Es darf also keinen aktuellen
	 * Konfigurationsverantwortlichen geben, der die gleiche Kodierung verwendet. Ansonsten wird eine Ausnahme gemeldet.
	 *
	 * @throws IllegalStateException Falls die Kodierung des Konfigurationsverantwortlichen nicht eindeutig ist.
	 */
	private void checkCodingOfConfigurationAuthority() throws IllegalStateException {
		final ConfigurationAuthority authority = getConfigurationAuthority();
		// Die Prüfung auf != null ist notwendig, da bei neu zu erstellenden Bereichen auch ein noch nicht vorhandener Verantwortlicher erzeugt werden kann.
		// Dann ist der Konfigurationsverantwortliche noch null.
		if(authority != null) {
//			System.out.println("Überprüfe KV:");
//			printAuthority(authority);
//			System.out.println("Vergleich mit anderen KV");
			short authorityCoding = authority.getCoding();
			final List<SystemObject> allCurrentAuthorities = getType("typ.konfigurationsVerantwortlicher").getObjects();
//			System.out.println("allCurrentAuthorities = " + allCurrentAuthorities);
			for(SystemObject systemObject : allCurrentAuthorities) {
				final ConfigurationAuthority otherAuthority = (ConfigurationAuthority)systemObject;
//				printAuthority(otherAuthority);
				if(authority != otherAuthority && authorityCoding == otherAuthority.getCoding()) {
//					ArrayList<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>(1);
//					configurationAreas.add(authority.getConfigurationArea());
//					Collection<SystemObject> objects = getObjects(configurationAreas, null, ObjectTimeSpecification.valid());
//					System.out.println("objects = " + objects);
//					for(SystemObject object : objects) {
//						if(object instanceof ConfigurationAuthority) {
//							ConfigurationAuthority configurationAuthority = (ConfigurationAuthority)object;
//							final Data data = configurationAuthority.getConfigurationData(getAttributeGroup("atg.konfigurationsVerantwortlicherLaufendeNummer"));
//							if(data != null) {
//								// Überprüfung, da Datensatz optional
//								final long dataValue = data.getUnscaledValue("laufendeNummer").longValue();
//								System.out.println("dataValue = " + dataValue);
//							}
//
//						}
//					}
					throw new IllegalStateException(
							"Die Kodierung " + authorityCoding + " des Konfigurationsverantwortlichen " + authority.getPid()
							+ " kollidiert mit dem Konfigurationsverantwortlichen " + otherAuthority.getPid() + "."
					);
				}
			}
		}
	}

//	private void printAuthority(final ConfigurationAuthority authority) {
//		System.out.println("authority = " + authority);
//		System.out.println("  authority.hashcode() = " + authority.hashCode());
//		System.out.println("  authority.originalHashCode() = " + ((ConfigSystemObject)authority).originalHashCode());
//		System.out.println("  authority.getId() = " + authority.getId());
//		System.out.println("  authority.getConfigurationArea() = " + authority.getConfigurationArea());
//		System.out.println("  authority.getDataModel() = " + authority.getDataModel());
//		System.out.println("  authority.getInfo() = " + authority.getInfo());
//		System.out.println("  authority.getValidSince() = " + authority.getValidSince());
//		System.out.println("  authority.getNotValidSince() = " + authority.getNotValidSince());
//	}

	/**
	 * Ermittelt die bisher größte vergebene Objekt-Id der Konfigurationsbereiche, die vom Verantwortlichen der Konfiguration verändert werden darf.
	 *
	 * @return eine neue noch nicht vergebene Objekt-Id
	 */
	synchronized long getNextObjectId() {
		// größte bisher vergebene Objekt-Id ermitteln, falls noch nicht ermittelt
		if(_nextObjectId == -1) {
			// größte laufende Nummer ermitteln
			final long runningNumber = getRunningNumberOfAuthority();

			// Kodierung ermitteln
			long coding = getCodingOfAuthority();

			// laufende Nummer und Kodierung zusammenführen
			coding = (coding << 48); // Kodierung des KV an die richtige Position der ID verschieben
			_nextObjectId = (runningNumber | coding);
		}

		// ID um eins erhöhen
		_nextObjectId++;

		_debug.fine("Nächste zu vergebene ID", idOutput(_nextObjectId));
//		System.out.println("Nächste zu vergebene ID = " + idOutput(_nextObjectId));

		// die letzten 40 Bit prüfen
		if((_nextObjectId & 0xFFFFFFFFFFL) == 0) {
			final String errorMessage = "Der Wertebereich für die laufende Nummer einer ID reicht nicht mehr aus: " + _nextObjectId;
			_debug.warning(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		// laufende Nummer extrahieren und am KV speichern
		try {
			setRunningNumberAtAuthority(_nextObjectId);
		}
		catch(ConfigurationChangeException ex) {
			_debug.warning("Laufende Nummer konnte nicht am Konfigurationsverantwortlichen gespeichert werden", ex.toString());
		}

		// neue Id zurückgeben
		return _nextObjectId;
	}

	private void setRunningNumberAtAuthority(final long nextObjectId) throws ConfigurationChangeException {
		final long runningNumber = (nextObjectId & 0xFFFFFFFFFFL);

		// Datensatz am KV speichern
		final ConfigurationAuthority authority = getConfigurationAuthority();
		// KV kann null sein, wenn ein neuer Bereich mit neuem KV angelegt wurde
		if(authority != null) {
			final AttributeGroup atg = getAttributeGroup("atg.konfigurationsVerantwortlicherLaufendeNummer");
			Data data = authority.getConfigurationData(atg);
			if(data == null) {
				// Data erzeugen
				data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
			}
			data.getUnscaledValue("laufendeNummer").set(runningNumber);
			authority.setConfigurationData(atg, data);
		}
	}

	/**
	 * Kodierung des Verantwortlichen der Konfiguration ermitteln.
	 *
	 * @return Kodierung des Konfigurationsverantwortlichen
	 */
	private long getCodingOfAuthority() {
		// Kodierung des Verantwortlichen der Konfiguration ermitteln
		final ConfigurationAuthority authority = getConfigurationAuthority();
		if(authority == null) {
			// Die Kodierung _authorityCoding wurde beim Erstellen eines neuen Bereichs mit neuem KV zugewiesen.
			return _authorityCoding;
		}
		else {
			final AttributeGroup atg = (AttributeGroup)getObject("atg.konfigurationsVerantwortlicherEigenschaften");
			final Data data = authority.getConfigurationData(atg);
			return data.getScaledValue("kodierung").longValue(); // direkt ein long, obwohl es ein short ist, dann braucht man nicht mehr casten
		}
	}

	/**
	 * Ermittelt die größte laufende Nummer zu einem Konfigurationsverantwortlichen.
	 *
	 * @return Größte laufende Nummer eines Konfigurationsverantwortlichen.
	 */
	private long getRunningNumberOfAuthority() {
		long runningNumber = 0;
		final String configurationAuthorityPid = getConfigurationAuthorityPid();
		// alle Konfigurationsbereiche werden durchlaufen
		for(ConfigurationArea configurationArea : getAllConfigurationAreas().values()) {
			final ConfigurationAreaFile areaFile = _configurationFileManager.getAreaFile(configurationArea.getPid());
			// falls der KV des Bereichs gleich dem KV der Konfiguration ist, dann ...
			if(configurationAuthorityPid.equals(configurationArea.getConfigurationAuthority().getPid())) {
				// größte bisher vergebene laufende Nummer ermitteln
				long greatestConsecutiveNumber = areaFile.getGreatestId();
//					_debug.finest("Konfigurationsbereich " + configurationArea.getPidOrNameOrId() + " greatestConsecutiveNumber " + idOutput(greatestConsecutiveNumber));
				if(greatestConsecutiveNumber > runningNumber) runningNumber = greatestConsecutiveNumber;
			}
		}
//		System.out.println("runningNumber (Bereiche) = " + runningNumber);

		// laufende Nummer am KV ermitteln, falls vorhanden
		final ConfigurationAuthority authority = getConfigurationAuthority();
		if(authority != null) {
			// asp.eigenschaften
			final Data data = authority.getConfigurationData(getAttributeGroup("atg.konfigurationsVerantwortlicherLaufendeNummer"));
			if(data != null) {
				// Überprüfung, da Datensatz optional
				final long dataValue = data.getUnscaledValue("laufendeNummer").longValue();
				if(dataValue > runningNumber) runningNumber = dataValue;
			}
		}
//		System.out.println("runningNumber (KV) = " + runningNumber);

		return runningNumber;
	}

	/**
	 * Gibt eine Objekt-ID formatiert aus (siehe TPuK1-9 Vergabe von Objekt-IDs): id (0#Kodierung des KV#0#Laufende Nummer)
	 *
	 * @param id anzuzeigende Objekt-ID
	 *
	 * @return String-Repräsentation der Objekt-ID
	 */
	String idOutput(long id) {
		StringBuffer result = new StringBuffer();
		result.append(id);
		result.append(" (");
		result.append((id >>> 63) & 0x1L);
		result.append("#");
		result.append((id >>> 48) & 0x7FFFL);
		result.append("#");
		result.append((id >>> 40) & 0xFFL);
		result.append("#");
		result.append((id & 0xFFFFFFFFFFL));
		result.append(")");
		return result.toString();
	}

	/**
	 * Liefert die Verwaltung für die Konfigurationsdateien.
	 *
	 * @return die Verwaltung für die Konfigurationsdateien
	 */
	public ConfigurationFileManager getConfigurationFileManager() {
		synchronized(_configurationFileManager) {
			return _configurationFileManager;
		}
	}

	/**
	 * Gibt das Objekt für Änderungen an der Verwaltungsdatei zurück.
	 *
	 * @return das Objekt zur Manipulation der Verwaltungsdatei
	 */
	public synchronized ConfigurationManagementFile getManagementFile() {
		return _managementFile;
	}

	public ConfigurationAuthority getConfigurationAuthority() {
		// Bei der Erstellung eines neuen KV in einem neuen Bereich ist der KV der Konfiguration == null. Dieser kann aber bei der Erstellung
		// direkt gesetzt werden. Was zur Folge hat, dass weitere Objekte in seiner Verantwortung erstellt werden können.
		if(_configurationAuthority == null) {
			synchronized(_managementFile) {
				_configurationAuthority = (ConfigurationAuthority)getObject(_managementFile.getConfigurationAuthority());
			}
		}
		return _configurationAuthority;
	}

	public String getConfigurationAuthorityPid() {
		return getManagementFile().getConfigurationAuthority();
	}

	/**
	 * Gibt zum angegebenen Konfigurationsbereich die Versionsinformationen zurück, wann welche Version aktiviert wurde.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @return die Versionsinformationen des Konfigurationsbereichs
	 */
	List<VersionInfo> getVersionInfoOfConfigurationArea(ConfigurationArea configurationArea) {
		synchronized(_managementFile) {
			final ConfigurationAreaManagementInfo managementInfo = _managementFile.getConfigurationAreaManagementInfo(configurationArea.getPid());
			return managementInfo.getVersions();
		}
	}

	/**
	 * Gibt zu einem Konfigurationsbereich dessen Verwaltungsinformationen zurück.
	 *
	 * @param pid Pid des Konfigurationsbereichs
	 *
	 * @return Verwaltungsinformationen des gewünschten Konfigurationsbereichs
	 */
	public ConfigurationAreaManagementInfo getConfigurationAreaManagementInfo(String pid) {
		synchronized(_managementFile) {
			return _managementFile.getConfigurationAreaManagementInfo(pid);
		}
	}

	public ConsistencyCheckResultInterface checkConsistency(Collection<ConfigAreaAndVersion> configurationAreas) {
		return checkConsistency(configurationAreas, KindOfConsistencyCheck.CONSISTENCY_CHECK);
	}

	/**
	 * Führt die Konsistenzprüfung aus und gibt das Ergebnis im Fehlerfalls auf dem Bildschirm aus.
	 *
	 * @param configurationAreas     Konfigurationsbereiche in den zu betrachtenden Versionen.
	 * @param kindOfConsistencyCheck Art der durchzuführenden Prüfung.
	 *
	 * @return Ergebnisse der Konsistenzprüfung
	 */
	private ConsistencyCheckResultInterface checkConsistency(
			Collection<ConfigAreaAndVersion> configurationAreas, KindOfConsistencyCheck kindOfConsistencyCheck) {
		// Konsistenzprüfung ausführen
		final ConsistencyCheckResultInterface consistencyCheckResult = new ConsistencyCheck(
				configurationAreas.toArray(new ConfigAreaAndVersion[configurationAreas.size()]), this
		).startConsistencyCheck(kindOfConsistencyCheck);

		// Ergebnis ausgeben
		printConsistencyCheckResult(configurationAreas, consistencyCheckResult);

		// zurückgeben
		return consistencyCheckResult;
	}

	private void printConsistencyCheckResult(
			final Collection<ConfigAreaAndVersion> configurationAreas, final ConsistencyCheckResultInterface consistencyCheckResult) {
		StringBuilder areaList = new StringBuilder();
		areaList.append(String.format("Version  Bereich\n"));
		for(ConfigAreaAndVersion configurationArea : configurationAreas) {
			areaList.append(String.format("%7d  %s\n", configurationArea.getVersion(), configurationArea.getConfigArea().getPid()));
		}

		if(consistencyCheckResult.warnings() || consistencyCheckResult.interferenceErrors() || consistencyCheckResult.localError()) {
			// Wenn es zu einem Fehler gekommen ist, wird alles ausgegeben
			final StringBuilder warningText = new StringBuilder();
			warningText.append("Probleme bei der Konsistenzprüfung. Geprüfte Bereiche:\n");
			warningText.append(areaList);
			warningText.append(consistencyCheckResult.toString());
			_debug.warning(warningText.toString());
		}
		else {
			// Kein Fehler
			_debug.config("Keine Probleme bei der Konsistenzprüfung. Geprüfte Bereiche:\n" + areaList.toString());
		}
	}

	public ConsistencyCheckResultInterface activateConfigurationAreas(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException {

		// Enthält keine Version 0 mehr, alle Versionen wurden angepaßt
		final Collection<ConfigAreaAndVersion> simplifiedAreaAndVersion = new ArrayList<ConfigAreaAndVersion>();

		// Die übergebenen Versionen können den Wert "0" enthalten. Dies wird als "nimm die größt mögliche Version" interpretiert.
		// Im Fall der aktivierung bedeutet dies die neuste Version für den Bereich, wenn der KV für den Bereich verantwortlich ist.
		// Ist der KV nicht verantwortlich, so wird die "zur Übernahme freigegebene" Version benutzt.

		for(ConfigAreaAndVersion configAreaAndVersion : configurationAreas) {
			// Version, die gesetzt wird. Dies kann entweder die übergebene Version sein oder eine andere, falls 0 übergeben wurde.
			short version = configAreaAndVersion.getVersion();
			if(configAreaAndVersion.getVersion() == 0) {
				final ConfigConfigurationArea configConfigurationArea = (ConfigConfigurationArea)configAreaAndVersion.getConfigArea();

				if(getConfigurationAuthorityPid().equals(configConfigurationArea.getConfigurationArea().getConfigurationAuthority().getPid())) {
					version = configConfigurationArea.getLastModifiedVersion();
				}
				else {
					version = configConfigurationArea.getActivatableVersion();
				}
			}
			simplifiedAreaAndVersion.add(new ConfigAreaAndVersion(configAreaAndVersion.getConfigArea(), version));
		}

		final ConsistencyCheckResultInterface consistencyCheckResult;
		try {
			consistencyCheckResult = checkConsistencyAndFixErrors(simplifiedAreaAndVersion, KindOfConsistencyCheck.LOCAL_ACTIVATION);

			if(!consistencyCheckResult.interferenceErrors() && !consistencyCheckResult.localError()) {
				// Es gibt weder lokale noch Interferenzfehler, also können die Bereiche aktiviert werden.

				// Die Konfiguration darf alle Bereiche aktivieren, für die sie verantwortlich ist.
				// Bereiche, für die sie nicht verantwortlich ist, dürfen nur in der Version aktiviert werden, die auch
				// für die Aktivierung freigegeben sind.
				// Die Version, in der der Bereich aktiviert werden soll, muss größer als die aktuelle Version
				// (es gibt keine Rücksprünge).
				// Es wird als erstes geprüft, ob die Versionen auch wirklich aktiviert werden dürfen, ist dies der Fall
				// wird die Aktivierung durchgeführt (Motto, alles oder nichts wird aktiviert)

				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedAreaAndVersion) {
					// Bereich, der in eine neue Version überführt werden soll
					final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();

					// Die zu aktivierende Version muss größer als die aktuell laufende Version sein
					// wenn die Versionsnummern gleich sind, ist dies kein Fehler, es muss nur nicht aktiviert werden
					if(configAreaAndVersion.getVersion() < configurationArea.getActiveVersion()) {
						throw new IllegalArgumentException(
								"Der Konfigurationsbereich " + configurationArea.getPid() + " sollte in Version " + configAreaAndVersion.getVersion()
								+ " aktiviert werden. Die derzeit aktuelle Version ist aber " + configurationArea.getActiveVersion() + " ."
						);
					}

					if(!getConfigurationAuthorityPid().equals(configurationArea.getConfigurationAuthority().getPid())) {
						// Die Konfiguration ist nicht der Konfigurationsverantwortliche für den Bereich
						if(configAreaAndVersion.getVersion() > configurationArea.getActivatableVersion()) {
							// Die Version darf nicht aktiviert werden, da sie nicht zur Aktivierung freigegeben wurde
							_debug.warning(
									"Der Konfigurationsbereich " + configurationArea.getPid() + " sollte in Version " + configAreaAndVersion.getVersion()
									+ " aktiviert werden. Die größte zur Aktivierung freigegebene Version war " + configurationArea.getActivatableVersion()
							);
							throw new IllegalStateException(
									"Der Konfigurationsbereich " + configurationArea.getPid() + " sollte in Version " + configAreaAndVersion.getVersion()
									+ " aktiviert werden. Die größte zur Aktivierung freigegebene Version war " + configurationArea.getActivatableVersion()
							);
						}
					}
				} // for, prüfen ob die Konfigurationsbereiche wirklich aktiviert werden dürfen

				// Die Prüfung war erfolgreich, also dürfen alle Bereiche aktiviert werden
				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedAreaAndVersion) {
					final ConfigurationArea configurationArea = getAllConfigurationAreas().get(configAreaAndVersion.getConfigArea().getPid());
					// nur wenn die neue Version größer als die bisherige ist, wird aktiviert.
					if(configAreaAndVersion.getVersion() > configAreaAndVersion.getConfigArea().getActiveVersion()) {
						// die Verwaltungsinfo erhält den Auftrag den Bereich in einer bestimmten Version zu aktivieren
						final ConfigurationAreaManagementInfo configurationAreaManagementInfo = getConfigurationAreaManagementInfo(configAreaAndVersion.getConfigArea().getPid());
						configurationAreaManagementInfo.setNextActiveVersion(configAreaAndVersion.getVersion());

						// nur wenn der Konfigurationsverantwortliche identisch mit dem des Bereichs ist, muss die modifizierbare Version geändert werden
						// Ist die derzeitige zukünftige Version gleich der Version, die aktiviert werden soll ?
						// ja) zukünftige Version raufzählen
						// nein) nichts machen

						if(getConfigurationAuthorityPid().equals(configurationArea.getConfigurationAuthority().getPid())) {
							final ConfigurationAreaFile areaFile = getConfigurationFileManager().getAreaFile(configAreaAndVersion.getConfigArea().getPid());
							short nextActiveVersion = areaFile.getNextActiveVersion();
							if(nextActiveVersion == configAreaAndVersion.getVersion()) {
								areaFile.setNextActiveVersion((short)(nextActiveVersion + 1));
							}
						}
					}

					// Bereiche, in denen kein Objekt und keine Menge verändert wurde, aber der Konfigurationsverantwortliche geändert wurde, wird dies berücksichtigt
					if(getConfigurationAuthorityPid().equals(configurationArea.getConfigurationAuthority().getPid())) {
						// Falls ein neuer Zuständiger beim Bereich eingetragen wurde, wird er hier dem Zuständigen zugewiesen.
						final AttributeGroup configurationAreaAtg = getAttributeGroup("atg.konfigurationsBereichEigenschaften");
						final Aspect configurationAreaAsp = getAspect("asp.eigenschaften");

						final Data data = configurationArea.getConfigurationData(configurationAreaAtg, configurationAreaAsp);
						final SystemObject newAuthority = data.getReferenceValue("neuerZuständiger").getSystemObject();
						if(newAuthority != null) {
							// es wurde ein neuer Konfigurationsverantwortlicher angegeben
							((ConfigConfigurationArea)configurationArea).activateNewAuthority(newAuthority);
							_debug.info(
									"Der Konfigurationsverantwortliche des Bereichs " + configurationArea.getPidOrNameOrId() + " wurde geändert in "
									+ newAuthority.getPidOrNameOrId()
							);
						}
					}
				} // for, Aktivierung der Bereiche

				// Konfigurationsdatei und die Bereichsdateien speichern. Es muss kein close Aufgerufen werden, da die Konfiguration ganz normal weiterläuft.
				save();

				// Alle Bereiche wurden aktiviert, das positive Ergebnis der Konsistenzprüfung wird zurückgegeben

				// Den Benutzer benachrichtigen, was alles in welcher Version aktiviert wurde.
				// Speichert die Bereiche und die Version in denen sie aktiviert wurde
				final StringBuffer areas = new StringBuffer();

				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedAreaAndVersion) {
					areas.append(configAreaAndVersion).append("\n");
				}

				_debug.info("Aktivierung erfolgreich, aktivierte Bereiche: " + "\n" + areas.toString());
			} // if (Konsistenzprüfung == fehlerfrei)
		}
		catch(Exception ex) {
			// fängt alle geworfenen Exceptions und gibt hierfür eine ConfigurationChangeException weiter, damit der Anwender der Methode nur eine Exception abfangen muss
			throw new ConfigurationChangeException(ex);
		}
		return consistencyCheckResult;
	}

	/**
	 * Diese Methode beauftragt die Konfiguration alle Konfigurationsbereiche einer Konsistenzprüfung zu unterziehen und behebbare Probleme zu beheben. Diese
	 * Methode kann unabhängig von der Aktivierung {@link #activateConfigurationAreas} oder Freigabe {@link #releaseConfigurationAreasForTransfer} aufgerufen
	 * werden.
	 *
	 * @param configurationAreas Definiert alle Konfigurationsbereiche, die einer Konsistenzprüfung unterzogen werden sollen. Der Bereich wird über seine Pid
	 *                           identifiziert, zusätzlich wird die Version angegeben in der der Konfigurationsbereich geprüft werden soll. Alle Bereiche der
	 *                           Konfiguration, die nicht angegeben werden, werden in die Prüfung einbezogen und zwar mit ihrer aktuellen Version und müssen somit
	 *                           nicht explizit angegeben werden.
	 *
	 * @return Ergebnis der Konsistenzprüfung
	 */
	public ConsistencyCheckResultInterface checkConsistencyAndFixErrors(final Collection<ConfigAreaAndVersion> configurationAreas) {
		return checkConsistencyAndFixErrors(configurationAreas, KindOfConsistencyCheck.CONSISTENCY_CHECK);
	}

	/**
	 * Diese Methode beauftragt die Konfiguration alle Konfigurationsbereiche einer Konsistenzprüfung zu unterziehen und behebbare Probleme zu beheben.
	 *
	 * @param configurationAreas     Definiert alle Konfigurationsbereiche, die einer Konsistenzprüfung unterzogen werden sollen. Der Bereich wird über seine Pid
	 *                               identifiziert, zusätzlich wird die Version angegeben in der der Konfigurationsbereich geprüft werden soll. Alle Bereiche der
	 *                               Konfiguration, die nicht angegeben werden, werden in die Prüfung einbezogen und zwar mit ihrer aktuellen Version und müssen
	 *                               somit nicht explizit angegeben werden.
	 * @param kindOfConsistencyCheck Art der Prüfung
	 *
	 * @return Ergebnis der Konsistenzprüfung
	 */
	private ConsistencyCheckResultInterface checkConsistencyAndFixErrors(
			final Collection<ConfigAreaAndVersion> configurationAreas, final KindOfConsistencyCheck kindOfConsistencyCheck) {
		final ConsistencyCheckResultInterface result;

		// Erst eine Prüfung durchführen und nachsehen, ob behebbare Fehler vorhanden sind.
		final ConsistencyCheckResultInterface firstRun = checkConsistency(configurationAreas, kindOfConsistencyCheck);
		final List<FixableConsistencyCheckResultEntry> fixableErrors = getFixableErrors(firstRun);

		if(fixableErrors.size() > 0) {

			_debug.warning("Anzahl behebbare Probleme: " + fixableErrors.size() + "\nFehlerbehebung wird gestartet.");

			// Es sind behebbare Fehler vorhanden. Diese beheben...
			fixErrors(fixableErrors);


			// Zählen, wie viele Fehler behoben werden konnten
			int failed = 0;
			int fixed = 0;

			for(final FixableConsistencyCheckResultEntry fixableError : fixableErrors) {
				if(fixableError.isError()) {
					failed++;
				}
				else if(fixableError.isFixed()) {
					fixed++;
				}
			}

			_debug.warning(
					"Fehlerbehebung beendet.\n" +
					"Anzahl erfolgreich behobene Probleme: " + fixed + "\n" +
					"Anzahl nicht erfolgreich behobene Probleme: " + failed
			);

			// ... erneut eine Prüfung durchführen ...
			final ConsistencyCheckResultInterface secondRun = new ConsistencyCheck(
					configurationAreas.toArray(new ConfigAreaAndVersion[configurationAreas.size()]), this
			).startConsistencyCheck(kindOfConsistencyCheck);

			// ... und die (hoffentlich) behobenen Fehler dem Ergebnis mit dem Ergebnis der zweiten Prüfung vereinigen, damit der Benutzer über diese informiert wird.
			// Dabei werden immer noch vorhandene behebbare Fehler als echte lokale Fehler gewertet.
			final ConsistencyCheckResult tmp = new ConsistencyCheckResult();

			// Die Probleme hinzufügen, die bereits behoben wurden (oder wo das Beheben zumindest versucht wurde)
			for(final FixableConsistencyCheckResultEntry fixableError : fixableErrors) {
				tmp.addEntry(fixableError);
			}

			for(final ConsistencyCheckResultEntry entry : secondRun.getLocalErrors()) {
				tmp.addEntry(entry);
			}
			for(final ConsistencyCheckResultEntry entry : secondRun.getInterferenceErrors()) {
				tmp.addEntry(entry);
			}
			boolean hasFixableProblems = false;
			for(final ConsistencyCheckResultEntry entry : secondRun.getWarnings()) {
				if(entry instanceof FixableConsistencyCheckResultEntry) {
					hasFixableProblems = true;
				}
				tmp.addEntry(entry);
			}

			if(hasFixableProblems) {
				final ConfigurationAuthority configurationAuthority = getConfigurationAuthority();
				final ConfigurationArea configurationArea = configurationAuthority == null ? null : configurationAuthority.getConfigurationArea();
				tmp.addEntry(
						new ConsistencyCheckResultEntry(
								ConsistencyCheckResultEntryType.LOCAL_ERROR,
								configurationArea,
								Collections.<SystemObject>emptyList(),
								"Es sind auch nach der Fehlerkorrektur noch behebbare Fehler vorhanden. "
								+ "Eventuell trat beim Korrigieren des Fehlers ein Problem auf, oder der Vorgang muss wiederholt werden."
						)
				);
			}

			printConsistencyCheckResult(configurationAreas, tmp);

			result = tmp;
		}
		else {
			result = firstRun;
		}
		return result;
	}

	private static void fixErrors(final List<FixableConsistencyCheckResultEntry> fixableErrors) {
		for(final FixableConsistencyCheckResultEntry fixableError : fixableErrors) {
			fixableError.fixError();
		}
	}

	private static List<FixableConsistencyCheckResultEntry> getFixableErrors(final ConsistencyCheckResultInterface tempConsistencyCheckResult) {
		final List<FixableConsistencyCheckResultEntry> resultEntries = new ArrayList<FixableConsistencyCheckResultEntry>();
		for(final ConsistencyCheckResultEntry entry : tempConsistencyCheckResult.getWarnings()) {
			if(entry instanceof FixableConsistencyCheckResultEntry) {
				resultEntries.add((FixableConsistencyCheckResultEntry)entry);
			}
		}
		return resultEntries;
	}

	public ConsistencyCheckResultInterface releaseConfigurationAreasForTransfer(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException {

		// Damit Bereiche zur Übernahme freigegeben werden dürfen, darf es zu keinem lokalen Fehler bei der Konsistenzprüfung kommen

		// Falls die Version, in der ein Bereich freigegeben werden soll 0 ist, so muss diese 0 in eine richtige Version
		// umgewandelt werden

		// Enthält nur Versionen ungleich 0
		final Collection<ConfigAreaAndVersion> simplifiedConfigurationAreasAndVersion = new ArrayList<ConfigAreaAndVersion>();

		// Alle Bereiche der Konfiguration werden in der neusten Version freigegeben (für diese Bereiche muss die Konf. auch KV sein)
		for(ConfigAreaAndVersion configAreaAndVersion : configurationAreas) {

			// Es dürfen nur Bereiche freigegeben werden, für die die Konfiguration auch verantwortlich ist
			if(!getConfigurationAuthorityPid().equals(configAreaAndVersion.getConfigArea().getConfigurationAuthority().getPid())) {
				_debug.warning(
						"Die Konfiguration ist für den Konfigurationsbereich " + configAreaAndVersion.getConfigArea().getPid()
						+ " nicht der Konfigurationsverantwortliche. Darf diesen also auch nicht freigeben."
				);
				throw new ConfigurationChangeException(
						"Die Konfiguration ist für den Konfigurationsbereich " + configAreaAndVersion.getConfigArea().getPid()
						+ " nicht der Konfigurationsverantwortliche."
				);
			}

			short version = configAreaAndVersion.getVersion();
			// 0 wird als "nimm die aktuellste Version" interpretiert
			if(configAreaAndVersion.getVersion() == 0) {
				final ConfigConfigurationArea configConfigurationArea = (ConfigConfigurationArea)configAreaAndVersion.getConfigArea();
				version = configConfigurationArea.getLastModifiedVersion();
			}
			simplifiedConfigurationAreasAndVersion.add(new ConfigAreaAndVersion(configAreaAndVersion.getConfigArea(), version));
		}

		// Die Konsistenzprüfung prüft die nicht übergebenen Bereiche in deren aktueller Version
		final ConsistencyCheckResultInterface consistencyCheckResult;
		try {
			consistencyCheckResult = checkConsistencyAndFixErrors(
					simplifiedConfigurationAreasAndVersion, KindOfConsistencyCheck.RELEASE_FOR_TRANSFER
			);

			// damit der Vorgang ganz oder gar nicht durchgeführt wird, muss erst geprüft werden, ob es den Datensatz und die Attributgruppenverwendung gibt
			// und ob der Datensatz geändert werden darf
			final AttributeGroup atg = getAttributeGroup("atg.konfigurationsBereichÜbernahmeInformationen");
			final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(getAspect("asp.eigenschaften"));
			if(atgUsage == null || (atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData
			                        && atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData)) {
				throw new ConfigurationChangeException("Die Attributgruppenverwendung existiert nicht, oder läßt keine Änderungen an den Datensätzen zu.");
			}

			if(!consistencyCheckResult.localError()) {
				// Es gab keinen lokalen Fehler, die Bereiche dürfen zur Übernahme freigegeben werden

				// Prüfen, ob die Konfiguration für alle angegebenen Bereiche auch der Konfigurationsverantwortliche ist (dies ist bereits weiter oben geschehen).
				// Prüfen, ob die neue übernahme Version größer als die bisherige ist
				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {
					final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();

					// Es darf zu keinem Rücksprung der Versionen kommen
					if(configurationArea.getTransferableVersion() > configAreaAndVersion.getVersion()) {
						// Die zu setzende Version ist kleiner
						_debug.warning(
								"Die Version zur Freigabe für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
								+ configurationArea.getTransferableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion()
								+ " gesetzt werden."
						);
						throw new ConfigurationChangeException(
								"Die Version zur Freigabe für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
								+ configurationArea.getTransferableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion()
								+ " gesetzt werden."
						);
					}

					// prüfen, ob der Datensatz existiert
					if(configurationArea.getConfigurationData(atg) == null) {
						throw new ConfigurationChangeException("Der Datensatz zur Speicherung der Versionsnummern ist nicht vorhanden.");
					}
				}

				// zur Übernahme freigeben
				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {
					final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();
					// wenn die Versionen gleich sind, ändert sich nichts, nur wenn die gespeicherte Version
					// kleiner als die neue Version ist, wird der Datensatz geändert.
					if(configurationArea.getTransferableVersion() < configAreaAndVersion.getVersion()) {
						// Datensatz anpassen
						_debug.info(
								"Konfigurationsbereich " + configurationArea.getPid() + " wird in Version " + configAreaAndVersion.getVersion()
								+ " zur Übernahme freigegeben"
						);
						((ConfigConfigurationArea)configurationArea).setTransferableVersion(configAreaAndVersion.getVersion());
					}
				}
			}

			// Konfigurationsdatei und die Bereichsdateien speichern
			save();
		}
		catch(Exception ex) {
			// alle Exceptions werden hier gefangen und als ConfigurationChangeException weitergeworfen, damit der Anwender der Methode nur eine Exception abfangen muss
			throw new ConfigurationChangeException(ex);
		}
		return consistencyCheckResult;
	}

	public void releaseConfigurationAreasForActivation(Collection<ConfigAreaAndVersion> configurationAreas) throws ConfigurationChangeException {

		try {
			// Die Bereiche sind in Ordnung und können zur Aktivierung freigegeben werden
			// damit der Vorgang ganz oder gar nicht durchgeführt wird, muss erst geprüft werden, ob es den Datensatz und die Attributgruppenverwendung gibt
			// und ob der Datensatz geändert werden darf
			final AttributeGroup atg = getAttributeGroup("atg.konfigurationsBereichÜbernahmeInformationen");
			final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(getAspect("asp.eigenschaften"));
			if(atgUsage == null || (atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData
			                        && atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData)) {
				throw new ConfigurationChangeException("Die Attributgruppenverwendung existiert nicht, oder läßt keine Änderungen an den Datensätzen zu.");
			}

			// Wenn zu einem Bereich die Version 0 gefordert wird, ist dies als "gebe mit der höchst möglichen Version frei"
			// zu interpretieren. Diese Version muss aber erst rausgefunden werden.

			// Enthält nur Versionen ungleich 0
			final Collection<ConfigAreaAndVersion> simplifiedConfigurationAreasAndVersion = new ArrayList<ConfigAreaAndVersion>(configurationAreas.size());

			for(ConfigAreaAndVersion configAreaAndVersion : configurationAreas) {
				final ConfigAreaAndVersion simplifiedVersion;

				if(configAreaAndVersion.getVersion() == 0) {
					// Version muss gesucht werden. Es wird die lokal aktivierte Version für andere freigegeben.
					simplifiedVersion = new ConfigAreaAndVersion(
							configAreaAndVersion.getConfigArea(), configAreaAndVersion.getConfigArea().getActiveVersion()
					);
				}
				else {
					// Die alten Werte können benutzt werden
					simplifiedVersion = configAreaAndVersion;
				}
				simplifiedConfigurationAreasAndVersion.add(simplifiedVersion);
			}

			for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {
				final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();

				// Die Konfigurations muss für den Bereich verantwortlich sein
				if(!getConfigurationAuthorityPid().equals(configurationArea.getConfigurationAuthority().getPid())) {
					// Die Konfigurations ist für den Bereich nicht verantwortlich
					_debug.warning(
							"Die Konfiguration ist für den Konfigurationsbereich " + configurationArea.getPid()
							+ " nicht der Konfigurationsverantwortliche. Darf diesen also auch nicht freigeben."
					);
					throw new IllegalStateException(
							"Die Konfiguration ist für den Konfigurationsbereich " + configurationArea.getPid() + " nicht der Konfigurationsverantwortliche."
					);
				}

				// Die neue Version muss bereits lokal aktiv/aktiv gewesen sein (Annahme, alles was unter der aktuellen lokalen Version liegt, ist in Ordnung)
				if(!(configAreaAndVersion.getVersion() <= configurationArea.getActiveVersion())) {
					// Die Version ist lokal noch nicht aktiviert worden
					throw new IllegalArgumentException(
							"In einem Konfigurationsbereich sollte eine Version zur Aktivierung freigegeben werden, die gewünschte Version wurde aber lokal nicht aktiviert. Betroffener Konfigurationsbereich: "
							+ configurationArea.getPid() + " aktive Version: " + configurationArea.getActiveVersion()
							+ " Version, die zur Aktivierung freigegeben werden sollte: " + configAreaAndVersion.getVersion()
					);
				}

				// Die neue Version, die zur Aktivierung freigegeben wird, muss höher sein als die letzte Version, die zur Aktivierung freigegeben wurde (keine Rücksprünge)
				if(configurationArea.getActivatableVersion() > configAreaAndVersion.getVersion()) {
					_debug.warning(
							"Die Version zur Freigabe der Aktivierung für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
							+ configurationArea.getActivatableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion() + " gesetzt werden."
					);
					throw new IndexOutOfBoundsException(
							"Die Version zur Freigabe der Aktivierung für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
							+ configurationArea.getActivatableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion() + " gesetzt werden."
					);
				}

				// prüfen, ob der Datensatz existiert
				if(configurationArea.getConfigurationData(atg) == null) {
					throw new ConfigurationChangeException("Der Datensatz zur Speicherung der Versionsnummern ist nicht vorhanden.");
				}
			} // for, alle Bereiche prüfen

			// zur Aktivierung freigeben
			for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {
				final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();
				final short newActivatableVersion = configAreaAndVersion.getVersion();
				// wenn die Versionen gleich sind, muss der Datensatz nicht geändert werden - nur wenn die neue Version
				// größer als die bisherige ist wird der Datensatz geändert.
				if(configurationArea.getActivatableVersion() < newActivatableVersion) {
					// Datensatz anpassen
					_debug.info(
							"Konfigurationsbereich " + configurationArea.getPid() + " wird in Version " + newActivatableVersion
							+ " zur Übernahme und Aktivierung freigegeben"
					);
					// Abfrage damit die übernehmbare Version nicht verkleinert wird.
					if(((ConfigConfigurationArea)configurationArea).getTransferableVersion() < newActivatableVersion) {
						// Bei Veränderung der aktivierbaren Version wird die übernehmbare Version mit vergrößert.
						((ConfigConfigurationArea)configurationArea).setTransferableVersion(newActivatableVersion);
					}
					((ConfigConfigurationArea)configurationArea).setActivatableVersion(newActivatableVersion);
				}
			}

			// Verwaltungsdatei und Bereiche speichern
			save();
		}
		catch(Exception ex) {
			// alle Exceptions werden hier gefangen und als ConfigurationChangeException weitergeworfen, damit der Anwender der Methode nur eine Exception abfangen muss.
			throw new ConfigurationChangeException(ex);
		}
	}

	public void importConfigurationAreas(File importPath, Collection<String> configurationAreaPids) throws ConfigurationChangeException {
		new ConfigurationImport(this, importPath, configurationAreaPids);
		try {
			// Konfigurationsdatei und die Bereichsdateien speichern
			save();
		}
		catch(IOException ex) {
			// die Exception wird nur in eine RuntimeException umgewandelt.
			throw new IllegalStateException(ex);
		}
	}

	public void exportConfigurationAreas(File exportPath, Collection<String> configurationAreaPids) throws ConfigurationTaskException {
		try {
			new ConfigurationExport(this, exportPath, configurationAreaPids);
		}
		catch(Exception e) {
			throw new ConfigurationTaskException(e);
		}
	}

	public ConsistencyCheckResultInterface releaseConfigurationAreasForActivationWithoutCAActivation(Collection<ConfigAreaAndVersion> configurationAreas)
			throws ConfigurationChangeException {
		// Falls die Version, in der ein Bereich freigegeben werden soll 0 ist, so muss diese 0 in eine richtige Version
		// umgewandelt werden

		// Enthält alle Bereiche, die in der höchst möglichen Version zur Aktivierung freigegeben werden sollen. Alle Bereiche, die hier nicht eingetragen sind,
		// und für die der KV verantwortlich ist, werden in der "zur Aktivierung freigegeben" Version geprüft.
		// Als Schlüssel dient der Bereich, als Value der Bereich und die zu aktivierende Version.
		final Map<ConfigurationArea, ConfigAreaAndVersion> areaWithHighestPossibleVersion = new HashMap<ConfigurationArea, ConfigAreaAndVersion>();
		for(ConfigAreaAndVersion configurationAreaAndVersion : configurationAreas) {
			areaWithHighestPossibleVersion.put(configurationAreaAndVersion.getConfigArea(), configurationAreaAndVersion);
		}

		// Enthält nur Versionen ungleich 0
		final Collection<ConfigAreaAndVersion> simplifiedConfigurationAreasAndVersion = new ArrayList<ConfigAreaAndVersion>();

		// Alle Bereich der Konfiguration
		final Collection<ConfigurationArea> allAreas = getAllConfigurationAreas().values();

		for(ConfigurationArea area : allAreas) {
			if(area.getConfigurationAuthority() == getConfigurationAuthority()
			   || area.getConfigurationAuthority().getPid().equals(getConfigurationAuthorityPid())) {
				final ConfigAreaAndVersion simplifiedConfigAreaAndVersion;
				if(areaWithHighestPossibleVersion.containsKey(area)) {
					// Der Bereich soll in der höchst möglichen Version freigegeben werden oder aber in der übergebenen Version.
					final ConfigAreaAndVersion areaAndVersion = areaWithHighestPossibleVersion.get(area);

					short version = areaAndVersion.getVersion();
					// 0 wird als "nimm die aktuellste Version" interpretiert
					if(areaAndVersion.getVersion() == 0) {
						final ConfigConfigurationArea configConfigurationArea = (ConfigConfigurationArea)areaAndVersion.getConfigArea();
						version = configConfigurationArea.getLastModifiedVersion();
					}

					simplifiedConfigAreaAndVersion = new ConfigAreaAndVersion(area, version);
				}
				else {
					// Der Bereich soll nicht für andere freigegeben werden. Da ihn aber andere benutzen, muss die Version, in der er für anderen
					// zur Aktivierung freigegeben wurde, bei der Konsistenzprüfung benutzt werden.
					simplifiedConfigAreaAndVersion = new ConfigAreaAndVersion(area, area.getActivatableVersion());
				}

				simplifiedConfigurationAreasAndVersion.add(simplifiedConfigAreaAndVersion);
			}
			else {
				// Der KV ist für den Bereich nicht verantwortlich, aber vielleicht soll ja fälschlicherweise ein Bereich freigegeben werden, der
				// gar nicht freigegeben werden darf.
				if(areaWithHighestPossibleVersion.containsKey(area)) {
					// Ja, der Bereich sollte freigegeben werden. Der KV darf das aber gar nicht.

					final String errorText = "Die Konfiguration ist für den Konfigurationsbereich " + area.getPid()
					                         + " nicht der Konfigurationsverantwortliche. Darf diesen also auch nicht freigeben.";

					_debug.warning(errorText);
					throw new IllegalStateException(errorText);
				}
			}
		}

		// Die Konsistenzprüfung prüft die nicht übergebenen Bereiche in deren aktueller Version.
		final ConsistencyCheckResultInterface consistencyCheckResult;
		try {
			consistencyCheckResult = checkConsistencyAndFixErrors(
					simplifiedConfigurationAreasAndVersion, KindOfConsistencyCheck.RELEASE_FOR_ACTIVATION_WITHOUT_LOCAL_ACTIVATION
			);

			// damit der Vorgang ganz oder gar nicht durchgeführt wird, muss erst geprüft werden, ob es den Datensatz und die Attributgruppenverwendung gibt
			// und ob der Datensatz geändert werden darf
			final AttributeGroup atg = getAttributeGroup("atg.konfigurationsBereichÜbernahmeInformationen");
			final AttributeGroupUsage atgUsage = atg.getAttributeGroupUsage(getAspect("asp.eigenschaften"));
			if(atgUsage == null || (atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableOptionalConfigurationData
			                        && atgUsage.getUsage() != AttributeGroupUsage.Usage.ChangeableRequiredConfigurationData)) {
				throw new ConfigurationChangeException("Die Attributgruppenverwendung existiert nicht, oder läßt keine Änderungen an den Datensätzen zu.");
			}

			if(!consistencyCheckResult.localError()) {
				// Es gab keine lokalen, die Bereiche dürfen durch andere Aktiviert werden, obwohl der KV den Bereich noch nicht aktiviert hat

				// Prüfen, ob die Konfiguration für alle angegebenen Bereiche auch der Konfigurationsverantwortliche ist (dies ist bereits weiter oben geschehen).
				// Prüfen, ob die neue übernahme Version größer als die bisherige ist
				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {
					final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();

					// Es darf zu keinem Rücksprung der Versionen kommen
					if(configurationArea.getTransferableVersion() > configAreaAndVersion.getVersion()) {
						// Die zu setzende Version ist kleiner
						_debug.warning(
								"Die Version zur Freigabe für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
								+ configurationArea.getTransferableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion()
								+ " gesetzt werden."
						);
						throw new IllegalArgumentException(
								"Die Version zur Freigabe für den Konfigurationsbereich " + configurationArea.getPid() + " ist "
								+ configurationArea.getTransferableVersion() + " und sollte auf Version " + configAreaAndVersion.getVersion()
								+ " gesetzt werden."
						);
					}

					// prüfen, ob der Datensatz existiert
					if(configurationArea.getConfigurationData(atg) == null) {
						throw new ConfigurationChangeException("Der Datensatz zur Speicherung der Versionsnummern ist nicht vorhanden.");
					}
				}

				// zur aktivierung durch andere freigeben
				for(ConfigAreaAndVersion configAreaAndVersion : simplifiedConfigurationAreasAndVersion) {

					// Es müssen 3 Versionen angepaßt werden:
					// 1) Die Version, die durch andere übernommen werden darf (wenn diese Version kleiner ist, als die die aktiviert werden darf)
					// 2) Die Version, die durch andere aktviert werden darf
					// 3) Version, in der neue Konfigurationsobjekte angelegt werden. Diese muss nur erhöht werden, wenn die Version, die durch andere aktiviert
					// werden darf gleich der Version ist, in der neue Konfigurationsobjekte angelegt werden.

					final ConfigurationArea configurationArea = configAreaAndVersion.getConfigArea();
					// wenn die Versionen gleich sind, ändert sich nichts, nur wenn die gespeicherte Version
					// kleiner als die neue Version ist, wird der Datensatz geändert.
					if(configurationArea.getTransferableVersion() < configAreaAndVersion.getVersion()) {
						// Datensatz anpassen
						_debug.info(
								"Konfigurationsbereich " + configurationArea.getPid() + " wird in Version " + configAreaAndVersion.getVersion()
								+ " zur Aktivierung durch andere freigegeben"
						);

						((ConfigConfigurationArea)configurationArea).setTransferableVersion(configAreaAndVersion.getVersion());
					}

					// Andere dürfen diese Version nun aktivieren
					((ConfigConfigurationArea)configurationArea).setActivatableVersion(configAreaAndVersion.getVersion());

					// Die Version, in der neue Konfigurationsobjekte entstehen, muss vielleicht angepaßt werden.
					final ConfigurationAreaFile areaFile = getConfigurationFileManager().getAreaFile(configAreaAndVersion.getConfigArea().getPid());
					short nextActiveVersion = areaFile.getNextActiveVersion();

					// Abfrage auf == und nicht <= weil in "configAreaAndVersion.getVersion()" ein Wert stehen kann der < ist.
					// Beispiel: Die letzten Änderungen wurden in Version 4 gemacht, dann wird auf Version 5 benutzt. Aber in 5
					// wurde noch nichts geändert. Dann steht in nextActiveVersion die 5 und in configAreaAndVersion.getVersion() die 4.
					// In Version 5 können aber problemslos neue Objekte eingefügt werden. Bei der Abfrage <= würde Version 5 einfach übersprungen und eine
					// Version verschenkt.
					if(nextActiveVersion == configAreaAndVersion.getVersion()) {

						// Neue Objekte würden in einer Version entstehen, die bereits für andere freigegeben wurde.
						// Es muss eine neue Version gesetzt werden, in der neue Objekte entstehen.

						areaFile.setNextActiveVersion((short)(nextActiveVersion + 1));
					}
				}
			}

			// Konfigurationsdatei und die Bereichsdateien speichern
			save();
		}
		catch(Exception ex) {
			// alle Exceptions werden hier gefangen und als ConfigurationChangeException weitergeworfen, damit der Anwender der Methode nur eine Exception abfangen muss
			throw new ConfigurationChangeException(ex);
		}

		return consistencyCheckResult;
	}

	public SystemObject getObject(String pid) {
		if(pid == null || pid.equals("") || pid.equals("null")) return null;
		final SystemObjectInformation systemObjectInformation;
		synchronized(_configurationFileManager) {
			systemObjectInformation = (SystemObjectInformation)_configurationFileManager.getActiveObject(pid);
		}
		if(systemObjectInformation != null) {
			return createSystemObject(systemObjectInformation);
		}
		else {
			_debug.fine("Zur angegebenen PID " + pid + " gibt es kein Objekt!");
			return null;
		}
	}

	public SystemObject getObject(long id) {
		if(id == 0) return null;
		final SystemObjectInformation objectInformation;
		synchronized(_configurationFileManager) {
			objectInformation = (SystemObjectInformation)_configurationFileManager.getObject(id);
		}
		if(objectInformation != null) {
			return createSystemObject(objectInformation);
		}
		else {
			_debug.fine("Zur angegebenen ID " + id + " gibt es kein Objekt!");
			return null;
		}
	}

	/**
	 * Gibt zu einer ID die Pid zurück.
	 *
	 * @param id die ID des Objekts
	 *
	 * @return Die Pid zur ID oder einen leeren String, falls das Objekt nicht existiert.
	 */
	private String getPidOfType(long id) {
		SystemObjectInformationInterface systemObjectInfo = null;
		synchronized(_configurationFileManager) {
			systemObjectInfo = _configurationFileManager.getObject(id);
		}
		if(systemObjectInfo != null) {
			return systemObjectInfo.getPid();
		}
		else {
			return "";
		}
	}

	/**
	 * Erstellt, wenn es noch nicht existiert, ein neues System-Object und gibt es zurück. Diese Methode wird verwendet, falls der Konfigurationsbereich nicht
	 * bekannt ist.
	 *
	 * @param systemObjectInfo das korrespondierende Objekt aus den Konfigurationsdateien
	 *
	 * @return das neue System-Objekt
	 */
	SystemObject createSystemObject(SystemObjectInformationInterface systemObjectInfo) {
		return createSystemObject(null, systemObjectInfo);
	}

	/**
	 * Erstellt, wenn es noch nicht existiert, ein neues System-Objekt und gibt es zurück.
	 *
	 * @param systemObjectInfo  das korrespondierende Objekt für die Dateioperationen des SystemObjekts
	 * @param configurationArea Konfigurationsbereich des System-Objekt
	 *
	 * @return das neue System-Objekt
	 */
	SystemObject createSystemObject(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		final Object object = ((SystemObjectInformation)systemObjectInfo).getReference();
		if(object != null) {
			// es gibt bereits ein SystemObject an SystemObjectInfo.
			return (SystemObject)object;
		}
		else {
			// neues SystemObject wird erstellt
			// _debug.finer("Neues Objekt - Pid: " + systemObjectInfo.getPid() + " ID: " + systemObjectInfo.getID() + " Typ-ID: " + systemObjectInfo.getTypeId());
			// brauche Pid des Typs
			String typePid = getPidOfType(systemObjectInfo.getTypeId());
			if(typePid.equals(Pid.Type.CONFIGURATION_AREA)) {
				return new ConfigConfigurationArea(this, systemObjectInfo);
			}

			// wurde kein Konfigurationsbereich mitgegeben, dann muss er ermittelt werden
			if(configurationArea == null) {
				// brauche Konfigurationsbereich des Objekts
				ConfigurationAreaFile configurationAreaFile = ((SystemObjectInformation)systemObjectInfo).getConfigurationAreaFile();
				SystemObjectInformation configurationAreaInfo = (SystemObjectInformation)configurationAreaFile.getConfigurationAreaInfo();
				configurationArea = (ConfigurationArea)configurationAreaInfo.getReference();
				if(configurationArea == null) {
					configurationArea = (ConfigurationArea)getObject(configurationAreaInfo.getPid());
					configurationAreaInfo.setReference(configurationArea);
				}
			}

			SystemObject systemObject = null;
			// _debug.finest("Typ-Pid", typePid);
			if(typePid.equals(Pid.Type.ASPECT)) {
				systemObject = new ConfigAspect(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE)) {
				systemObject = new ConfigAttribute(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE_GROUP) || typePid.equals(Pid.Type.TRANSACTION)) {
				systemObject = new ConfigAttributeGroup(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE_GROUP_USAGE)) {
				systemObject = new ConfigAttributeGroupUsage(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE_LIST_DEFINITION)) {
				systemObject = new ConfigAttributeListDefinition(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE_SET)) {
				systemObject = new ConfigAttributeSet(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.ATTRIBUTE_TYPE)) {
				systemObject = new ConfigAttributeType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.CLIENT_APPLICATION)) {
				systemObject = new ConfigClientApplication(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.CONFIGURATION_AUTHORITY)) {
				systemObject = new ConfigConfigurationAuthority(configurationArea, systemObjectInfo);
				// } else if (typePid.equals(Pid.Type.CONFIGURATION_OBJECT)) {
				//
			}
			else if(typePid.equals(Pid.Type.DAV_APPLICATION)) {
				systemObject = new ConfigDavApplication(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.DOUBLE_ATTRIBUTE_TYPE)) {
				systemObject = new ConfigDoubleAttributeType(configurationArea, systemObjectInfo);
				// } else if (typePid.equals(Pid.Type.DYNAMIC_OBJECT)) {
				//
			}
			else if(typePid.equals(Pid.Type.DYNAMIC_TYPE)) {
				systemObject = new ConfigDynamicObjectType(configurationArea, systemObjectInfo, _dynamicObjectTypePublisher);
			}
			else if(typePid.equals(Pid.Type.INTEGER_ATTRIBUTE_TYPE)) {
				systemObject = new ConfigIntegerAttributeType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.INTEGER_VALUE_RANGE)) {
				systemObject = new ConfigIntegerValueRange(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.INTEGER_VALUE_STATE)) {
				systemObject = new ConfigIntegerValueState(configurationArea, systemObjectInfo);
				// } else if (typePid.equals(Pid.Type.MUTABLE_SET)) {
				//
				// } else if (typePid.equals(Pid.Type.NON_MUTABLE_SET)) {
				//
			}
			else if(typePid.equals(Pid.Type.OBJECT_SET_TYPE)) {
				systemObject = new ConfigObjectSetType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.OBJECT_SET_USE)) {
				systemObject = new ConfigObjectSetUse(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.REFERENCE_ATTRIBUTE_TYPE)) {
				systemObject = new ConfigReferenceAttributeType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.STRING_ATTRIBUTE_TYPE)) {
				systemObject = new ConfigStringAttributeType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.TIME_ATTRIBUTE_TYPE)) {
				systemObject = new ConfigTimeAttributeType(configurationArea, systemObjectInfo);
			}
			else if(typePid.equals(Pid.Type.TYPE)) {
				systemObject = new ConfigConfigurationObjectType(configurationArea, systemObjectInfo);
			}
			else {    // Sonderbehandlung verschiedener Fälle
//				SystemObjectType type = (SystemObjectType) getObject(typePid);
				// hier brauche ich die ID, da ich sonst zukünftige Typen nicht erhalte
				SystemObjectType type = (SystemObjectType)getObject(systemObjectInfo.getTypeId());
				if(type == null) {
					throw new NullPointerException(
							"Beim Anlegen eines Systemobjekts konnte der zugehörige Typ nicht ermittelt werden. ObjektInfo: " + systemObjectInfo
					);
				}
				final SystemObjectType typeType = type.getType();
				if(typeType == null) {
					throw new NullPointerException(
							"Beim Anlegen eines Systemobjekts konnte der Typ des Objekttyps mit der Pid '" + typePid + "' nicht ermittelt werden. ObjektInfo: "
							+ systemObjectInfo
					);
				}
				String typeTypePid = typeType.getPid();    // Pid des Typs vom Typ
				if(typeTypePid == null) {
					throw new NullPointerException(
							"Beim Anlegen eines Systemobjekts konnte die Pid vom Typ des Objekttyps mit der Pid '" + typePid
							+ "' nicht ermittelt werden. ObjektInfo: " + systemObjectInfo
					);
				}
				if(typeTypePid.equals(Pid.Type.OBJECT_SET_TYPE)) {    // es handelt sich um eine Menge
					if(((ObjectSetType)type).isMutable()) {    // veränderbare Menge
						systemObject = new ConfigMutableSet(configurationArea, systemObjectInfo);
					}
					else {    // nicht veränderbare Menge
						systemObject = new ConfigNonMutableSet(configurationArea, systemObjectInfo);
					}
				}
				else if(type.isConfigurating()) {
					// der Typ zeigt an, dass das Objekt ein konfigurierendes Objekt ist
					if(type.inheritsFrom(getType(Pid.Type.CONFIGURATION_AUTHORITY))) {
						// damit auch die autarke Organisationseinheit berücksichtigt wird
						systemObject = new ConfigConfigurationAuthority(configurationArea, systemObjectInfo);
					}
					else if(type.inheritsFrom(getType(Pid.Type.DAV_APPLICATION))) {
						// für den Datenverteiler
						systemObject = new ConfigDavApplication(configurationArea, systemObjectInfo);
					}
					else {            // für alle anderen Konfigurationsobjekte
						systemObject = new ConfigConfigurationObject(configurationArea, systemObjectInfo);
					}
				}
				else {    // wenn keiner der oberen Fälle zutrifft, dann muss es ein dynamisches Objekt sein
					if(type.inheritsFrom(getType(Pid.Type.CLIENT_APPLICATION))) {
						// für die verschiedenen Applikationen
						systemObject = new ConfigClientApplication(configurationArea, systemObjectInfo);
					}
					else {    // alle anderen dynamischen Objekte
						systemObject = new ConfigDynamicObject(configurationArea, systemObjectInfo);
					}
				}
			}
			return systemObject;
		}
	}

	public ConfigurationArea getConfigurationArea(String pid) {
		return (ConfigurationArea)getObject(pid);
	}

	public Map<String, ConfigurationArea> getAllConfigurationAreas() {
		synchronized(_managementFile) {
			final Map<String, ConfigurationArea> configurationAreas = new LinkedHashMap<String, ConfigurationArea>();
			for(ConfigurationAreaManagementInfo managementInfo : _managementFile.getAllConfigurationAreaManagementInfos()) {
				ConfigurationArea configurationArea = getConfigurationArea(managementInfo.getPid());
				if(configurationArea == null) {
					// Konfigurationsbereich ist noch kein aktuelles Objekt
					SystemObjectInformationInterface[] newObjectInfos = getConfigurationFileManager().getNewObjects(managementInfo.getPid());
					if(newObjectInfos.length == 0) {
						// es gibt noch kein KonfigurationsBereichs-Objekt -> mache nichts
					}
					else if(newObjectInfos.length == 1) {
						final SystemObjectInformationInterface objectInfo = newObjectInfos[0];
						configurationAreas.put(objectInfo.getPid(), (ConfigurationArea)createSystemObject(objectInfo));
					}
					else {
						throw new IllegalStateException("Es gibt mehr als einen zukünftigen Konfigurationsbereich mit dieser Pid: " + managementInfo.getPid());
					}
				}
				else {
					configurationAreas.put(configurationArea.getPid(), configurationArea);
				}
			}
			return configurationAreas;
		}
	}

	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg) {
		return getConfigurationData(Arrays.asList(objects), atg);
	}

	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg) {
		return getConfigurationData(objects, atg, getAspect("asp.eigenschaften"));
	}

	public Data[] getConfigurationData(SystemObject[] objects, AttributeGroup atg, Aspect asp) {
		return getConfigurationData(Arrays.asList(objects), atg, asp);
	}

	public Data[] getConfigurationData(Collection<SystemObject> objects, AttributeGroup atg, Aspect asp) {
		List<Data> datas = new ArrayList<Data>();
		for(SystemObject systemObject : objects) {
			datas.add(systemObject.getConfigurationData(atg, asp));
		}
		return datas.toArray(new Data[datas.size()]);
	}

	public short getActiveVersion(ConfigurationArea configurationArea) {
		synchronized(_managementFile) {
			return _managementFile.getConfigurationAreaManagementInfo(configurationArea.getPid()).getActiveVersion().getVersion();
		}
	}

	public SystemObjectType getTypeTypeObject() {
		return (SystemObjectType)getObject(Pid.Type.TYPE);
	}

	public List<SystemObjectType> getBaseTypes() {
		if(_baseTypes == null) {
			// alle Typen besorgen
			final List<SystemObject> typeTypeObjects = getTypeTypeObject().getObjects();
			_debug.finest("Anzahl Typen", typeTypeObjects.size());
			List<SystemObjectType> baseTypes = new ArrayList<SystemObjectType>();
			for(SystemObject systemObject : typeTypeObjects) {
				SystemObjectType type = (SystemObjectType)systemObject;
				if(type.isBaseType()) {
					_debug.finest("Basistyp", type.getPidOrNameOrId());
					baseTypes.add(type);
				}
			}
			_baseTypes = Collections.unmodifiableList(baseTypes);
		}
		return _baseTypes;
	}

	public SystemObjectType getType(String pid) {
		SystemObject object = getObject(pid);
		if(object != null) {
			if(object instanceof SystemObjectType) {
				return (SystemObjectType)object;
			}
			else {
				_debug.warning("getType(" + pid + "): Objekt hat einen falschen Typ", object);
			}
		}
		return null;
	}

	public ObjectSetType getObjectSetType(String pid) {
		SystemObject object = getObject(pid);
		if(object != null) {
			if(object instanceof ObjectSetType) {
				return (ObjectSetType)object;
			}
			else {
				_debug.warning("getObjectSetType(" + pid + "): Objekt hat einen falschen Typ", object);
			}
		}
		return null;
	}

	public AttributeGroup getAttributeGroup(String pid) {
		SystemObject object = getObject(pid);
		if(object != null) {
			if(object instanceof AttributeGroup) {
				return (AttributeGroup)object;
			}
			else {
				_debug.warning("getAttributeGroup(" + pid + "): Objekt hat einen falschen Typ", object);
			}
		}
		return null;
	}

	public AttributeType getAttributeType(String pid) {
		SystemObject object = getObject(pid);
		if(object != null) {
			if(object instanceof AttributeType) {
				return (AttributeType)object;
			}
			else {
				_debug.warning("getAttributeType(" + pid + "): Objekt hat einen falschen Typ", object);
			}
		}
		return null;
	}

	public Aspect getAspect(String pid) {
		SystemObject object = getObject(pid);
		if(object != null) {
			if(object instanceof Aspect) {
				return (Aspect)object;
			}
			else {
				_debug.warning("getAspect(" + pid + "): Objekt hat einen falschen Typ", object);
			}
		}
		return null;
	}

//	/** @deprecated Kodierung von Attributgruppen wird nicht mehr unterstützt. */
//	public AttributeGroup getAttributeGroup(short attributeGroupCode) {
//		final List<SystemObject> atgs = getType(Pid.Type.ATTRIBUTE_GROUP).getObjects();
//		for(SystemObject systemObject : atgs) {
//			AttributeGroup atg = (AttributeGroup)systemObject;
//			if(atg.getCode() == attributeGroupCode) {
//				return atg;
//			}
//		}
//		_debug.warning("Attributgruppe mit Kodierung " + attributeGroupCode + " wurde nicht gefunden.");
//		return null;
//	}

//	/** @deprecated Kodierung von Aspekten wird nicht mehr unterstützt. */
//	public Aspect getAspect(short aspectCode) {
//		for(SystemObject systemObject : getType(Pid.Type.ASPECT).getObjects()) {
//			Aspect asp = (Aspect)systemObject;
//			if(asp.getCode() == aspectCode) {
//				return asp;
//			}
//		}
//		_debug.warning("Aspekt mit Kodierung " + aspectCode + " wurde nicht gefunden.");
//		return null;
//	}

	public ConfigurationObject createConfigurationObject(ConfigurationObjectType type, String pid, String name, List sets) throws ConfigurationChangeException {
		throw new ConfigurationChangeException("Methode DataModel.createConfigurationObject(..) wird nicht mehr unterstützt.");
	}

	public DynamicObject createDynamicObject(SystemObjectType type, String pid, String name) throws ConfigurationChangeException {
		throw new ConfigurationChangeException("Methode DataModel.createDynamicObject(..) wird nicht mehr unterstützt.");
	}

	/**
	 * Löscht alle dynamischen Objekte mit der angegebenen Simulationsvariante permanent.
	 *
	 * @param simulationVariant die Simulationsvariante
	 */
	public void deleteObjects(short simulationVariant) {
		final List<DynamicObjectInfo> dynamicObjectInfos = _configurationFileManager.getObjects(simulationVariant);
		for(DynamicObjectInfo dynamicObjectInfo : dynamicObjectInfos) {
//			DynamicObject dynamicObject = (DynamicObject) getObject(dynamicObjectInfo.getID());
//			((ConfigDynamicObjectType) dynamicObject.getType()).informInvalidationListener(dynamicObject);
			dynamicObjectInfo.remove();
		}
	}

	public Collection<SystemObject> getObjects(String pid, long startTime, long endTime) {
		// alle Bereiche durchgehen
		final Collection<SystemObject> objects = new ArrayList<SystemObject>();
		ConfigurationAreaFile[] areaFiles = getConfigurationFileManager().getConfigurationAreas();
		for(ConfigurationAreaFile areaFile : areaFiles) {
			// es wird die lokale Aktivierungszeit verwendet, die Verwendung der globalen Aktivierungszeit wurde bisher noch nicht gefordert
			SystemObjectInformationInterface[] objectInfos = areaFile.getObjects(pid, startTime, endTime, ConfigurationAreaTime.LOCAL_ACTIVATION_TIME);
			for(SystemObjectInformationInterface systemObjectInfo : objectInfos) {
				SystemObject systemObject = createSystemObject(systemObjectInfo);
				objects.add(systemObject);
			}
		}
		return Collections.unmodifiableCollection(objects);
	}

	public Collection<SystemObject> getObjects(
			final Collection<ConfigurationArea> configurationAreas,
			final Collection<SystemObjectType> systemObjectTypes,
			ObjectTimeSpecification objectTimeSpecification) {
		// Liste, die alle ermittelten Objekte enthält
		final Collection<SystemObject> objects = new ArrayList<SystemObject>();

		// alle Konfigurationsbereiche, die zu berücksichtigen sind, ermitteln
		final List<ConfigurationArea> areas;
		if(configurationAreas == null) {
			areas = new ArrayList<ConfigurationArea>();
			ConfigurationAreaFile[] areaFiles = getConfigurationFileManager().getConfigurationAreas();
			for(ConfigurationAreaFile areaFile : areaFiles) {
				ConfigurationArea area = (ConfigurationArea)getObject(areaFile.getConfigurationAreaInfo().getPid());
				// nicht aktive Bereiche werden ignoriert
				if(area != null) {
					areas.add(area);
				}
			}
		}
		else {
			areas = new ArrayList<ConfigurationArea>(configurationAreas);
		}

		// alle Objekt-Typen, die zu betrachten sind, ermitteln - also auch die Typen, die die angegebenen Typen erweitern
		final Set<SystemObjectType> relevantObjectTypes = new HashSet<SystemObjectType>();
		if(systemObjectTypes == null) {
			// es wurde keine Einschränkung der Typen gegeben -> alle Typen ermitteln
			// die Typen eines TypTyp-Objekts ermittelt man mit getElements()!
			relevantObjectTypes.add(getTypeTypeObject());
			final List<SystemObject> typeTypeElements = getTypeTypeObject().getElements();
			for(SystemObject typeTypeElement : typeTypeElements) {
				relevantObjectTypes.add((SystemObjectType)typeTypeElement);
			}
		}
		else {
			// die Typen eines Typ-Objekts werden über die Sub-Types ermittelt.
			for(SystemObjectType objectType : systemObjectTypes) {
				relevantObjectTypes.add(objectType);
				relevantObjectTypes.addAll(getAllSubTypes(objectType));
			}
		}

		// Ermittlung der direkten Objekte der angegebenen Typen
//		System.out.println("ConfigDataModel.getObjects");
//		System.out.println("configurationAreas = " + configurationAreas);
//		System.out.println("systemObjectTypes = " + systemObjectTypes);
		for(ConfigurationArea configurationArea : areas) {
//			System.out.println("configurationArea = " + configurationArea);
			Collection<SystemObject> directObjects = configurationArea.getDirectObjects(relevantObjectTypes, objectTimeSpecification);
//			System.out.println("directObjects = " + directObjects);
			objects.addAll(directObjects);
		}
//		System.out.println("objects = " + objects);
		return objects;
	}

	public UserAdministration getUserAdministration() {
		// Wird diese Methode gebraucht, so muss die Klasse {@link de.bsvrz.puk.config.main.authentication.ConfigAuthentication} entsprechen geändert werden
		// und benutzt werden. (Die Klasse muss das UserAdmin Interface implementieren und dann kann die Klasse hier zurückgegeben werden)
		throw new UnsupportedOperationException("Diese Funktionalität wird nicht unterstützt.");
	}

	public BackupResult backupConfigurationFiles(final String targetDirectory, final BackupProgressCallback callback) throws ConfigurationTaskException {
		return backupConfigurationFiles(targetDirectory, null, callback);
	}

	public BackupResult backupConfigurationFiles(
			final String targetDirectory,
			final ConfigurationAuthority configurationAuthority,
			final BackupProgressCallback callback) throws ConfigurationTaskException {
		try {
			final ConfigFileBackupTask fileBackupTask = new ConfigFileBackupTask(
					getUserManagement(), this, targetDirectory, configurationAuthority, callback
			);
			if(callback != null) callback.backupStarted(fileBackupTask.getTargetPath());
			return fileBackupTask.startSync();
		}
		catch(IOException e) {
			throw new ConfigurationChangeException("Der Backup-Vorgang konnte nicht gestartet werden:", e);
		}
	}

	/**
	 * Diese Methode ermittelt zu diesem Objekt-Typ rekursiv alle Typen, die diesen Typ direkt und indirekt erweitern.
	 *
	 * @param type der zu betrachtende Objekt-Typ
	 *
	 * @return Alle Typen, die diesen Typ direkt und indirekt erweitern.
	 */
	protected Collection<SystemObjectType> getAllSubTypes(SystemObjectType type) {
		Collection<SystemObjectType> allSubTypes = new ArrayList<SystemObjectType>();
		for(SystemObjectType objectType : type.getSubTypes()) {
			allSubTypes.add(objectType);
			allSubTypes.addAll(getAllSubTypes(objectType));
		}
		return allSubTypes;
	}

	/**
	 * Gibt die String-Repräsentation dieser Klasse zurück. Der Wert kann sich ändern.
	 *
	 * @return die String-Repräsentation dieser Klasse
	 */
	public String toString() {
		return "Konfiguration { Verwaltungsdatei = '" + _adminFile.getPath() + "'}";
	}

	/**
	 * Speichert die Verwaltungsdaten und die Konfigurationsbereiche ab.
	 *
	 * @throws java.io.IOException Fehler, die beim Speichern der Verwaltungsdaten oder der Konfigurationsdateien auftreten, werden hier weitergereicht.
	 */
	public void save() throws IOException {
		// Dateien mit der Elementzugehörigkeit von dynamischen Mengen speichern
		saveSetElementsFiles();
		// Verwaltungsdatei speichern
		getManagementFile().save();
		// Konfigurationsbereiche speichern
		_configurationFileManager.saveConfigurationAreaFiles();
	}

	public void close() {
		try {
			// Dateien mit der Elementzugehörigkeit von dynamischen Mengen speichern
			saveSetElementsFiles();
			final ConfigurationManagementFile configurationManagementFile = getManagementFile();
			// configurationManagementFile kann null sein, wenn der Konstruktor vor dem Setzen des Fields auf eine Exception gelaufen ist,
			// beispielsweise dann, wenn die Verwaltungsdatei nicht gelockt werden konnte.
			if(configurationManagementFile != null) {
				configurationManagementFile.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			_debug.error(
					"Fehler beim Speichern der Verwaltungsdaten-Datei " + getManagementFile()
					+ ". Das System wird weiter heruntergefahren und soviele Daten wie möglich gesichert.", e
			);
		}
		finally {
			// Alle Bereiche sichern und die lock-Dateien freigeben.
			// _configurationFileManager kann null sein, wenn der Konstruktor vor dem Setzen des _configurationFileManager auf eine Exception gelaufen ist,
			// beispielsweise dann, wenn die Verwaltungsdatei nicht gelockt werden konnte.
			if(_configurationFileManager != null) {
				_configurationFileManager.close();
			}
		}
	}

	/**
	 * Setzt den Konfigurationsverantwortlichen der Konfiguration.
	 *
	 * @param authority der Konfigurationsverantwortliche der Konfiguration
	 */
	public void setConfigurationAuthority(final ConfigurationAuthority authority) {
		_configurationAuthority = authority;
	}

	public ConfigurationArea createConfigurationArea(
			final String areaName,
			final String areaPid,
			final SystemObjectType authorityObjectType,
			final String authorityPid,
			final String authorityName,
			final long authorityCoding) throws ConfigurationChangeException {

		try {
			// Verwaltungseintrag holen bzw. neu anlegen
			ConfigurationAreaManagementInfo managementInfo = getConfigurationAreaManagementInfo(areaPid);
			// Sans, STS, KonfigAss: Neuer Eintrag in Verwaltungsdatei wird hier NICHT, sondern - falls erforderlich - später, unmittelbar vor save() angelegt
			// Prüfen, ob der Konfigurationsverantwortliche bereits existiert - damit ist die Pid-Überprüfung geschehen.
			ConfigurationAuthority authority = (ConfigurationAuthority)getObject(authorityPid);
			if(authority == null) {
				// ID zuweisen
				_authorityCoding = authorityCoding;
			}

			// Konfigurationsbereich anlegen
			// Sans, STS, KonfigAss: Falls KB noch nicht vorhanden war - benutze Verzeichnis der Verwaltungsdatei
			final ConfigurationAreaFile areaFile = getConfigurationFileManager().createAreaFile(
					areaPid, managementInfo != null ? managementInfo.getDirectory() : _adminFile.getParentFile()
			);

			areaFile.setNextActiveVersion((short)1);    // Version festlegen, ab der die Objekte gültig werden sollen
			final SystemObjectType objectType = getType("typ.konfigurationsBereich");
			final ConfigurationObjectInfo areaInfo = areaFile.createConfigurationObject(getNextObjectId(), objectType.getId(), areaPid, areaName);
			final ConfigurationArea configurationArea = (ConfigurationArea)createSystemObject(areaInfo);

			// Konfigurationsverantwortlichen ermitteln bzw. erzeugen
			if(authority == null) {
				final ConfigurationObjectInfo authorityInfo = areaFile.createConfigurationObject(
						getNextObjectId(), authorityObjectType.getId(), authorityPid, authorityName
				);
				authority = (ConfigurationAuthority)createSystemObject(authorityInfo);
				setConfigurationAuthority(authority);
			}

			// Aspekt Eigenschaften wird für verschiedene Datensätze benötigt
			final Aspect propertyAsp = getAspect("asp.eigenschaften");

			// konfigurierenden Datensatz für den Konfigurationsverantwortlichen anlegen.
			final AttributeGroup areaPropertyAtg = getAttributeGroup("atg.konfigurationsBereichEigenschaften");
			Data areaPropertyData = AttributeBaseValueDataFactory.createAdapter(areaPropertyAtg, AttributeHelper.getAttributesValues(areaPropertyAtg));
			areaPropertyData.getReferenceValue("zuständiger").setSystemObject(authority);
			areaPropertyData.getReferenceValue("neuerZuständiger").setSystemObject(null);

			final AttributeGroupUsage areaPropertyAtgUsage = areaPropertyAtg.getAttributeGroupUsage(propertyAsp);
			((ConfigSystemObject)configurationArea).createConfigurationData(areaPropertyAtgUsage, areaPropertyData);

			// konfigurierenden Datensatz für die Versionsnummern anlegen
			final AttributeGroup areaReleaseAtg = getAttributeGroup("atg.konfigurationsBereichÜbernahmeInformationen");
			Data areaReleaseData = AttributeBaseValueDataFactory.createAdapter(areaReleaseAtg, AttributeHelper.getAttributesValues(areaReleaseAtg));
			areaReleaseData.getUnscaledValue("aktivierbareVersion").set((short)0);
			areaReleaseData.getUnscaledValue("übernehmbareVersion").set((short)0);
			final AttributeGroupUsage areaReleaseAtgUsage = areaReleaseAtg.getAttributeGroupUsage(propertyAsp);
			((ConfigSystemObject)configurationArea).createConfigurationData(areaReleaseAtgUsage, areaReleaseData);

			// Sans, STS, KonfigAss: Falls KB noch nicht vorhanden war - Eintrag in Verwaltungsdatei
			if(managementInfo == null) {
				// neuen Eintrag erstellen, da noch keiner in der Versorgungsdatei vorhanden ist
				managementInfo = getManagementFile().addConfigurationAreaManagementInfo(areaPid);
			}

			// Verwaltungsdatei und Konfigurationsdatei abspeichern
			save();

			// Objekt des Konfigurationsbereichs wird zurückgegeben
			_debug.info("Ein neuer Konfigurationsbereich mit der Pid '" + configurationArea.getPid() + "' wurde angelegt.");
			return configurationArea;
		}
		catch(Exception ex) {
			final String errorMessage = "Fehler beim Anlegen eines Konfigurationsbereichs";
			_debug.error(errorMessage, ex);
			throw new ConfigurationChangeException(errorMessage, ex);
		}
	}

	public ConfigurationArea createConfigurationArea(String name, String pid, String authorityPid) throws ConfigurationChangeException {
		// prüfen, ob es ein aktives Objekt des Konfigurationsverantwortlichen gibt
		if(getObject(authorityPid) != null) {
			return createConfigurationArea(name, pid, null, authorityPid, null, 0);
		}
		else {
			throw new ConfigurationChangeException("Zum Konfigurationsverantwortlichen " + authorityPid + " gibt es kein aktives Objekt.");
		}
	}

	/**
	 * Gibt zu einem Konfigurationsbereich die aktuellen Objekte zurück.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @return die aktuellen Objekte des angegebenen Konfigurationsbereichs
	 *
	 * @see ConfigurationArea
	 */
	Collection<SystemObject> getCurrentObjects(ConfigurationArea configurationArea) {
		final Collection<SystemObject> objects = new ArrayList<SystemObject>();
		final ConfigurationAreaFile areaFile = getConfigurationFileManager().getAreaFile(configurationArea.getPid());
		if(areaFile != null) {
			final SystemObjectInformationInterface[] actualObjectInfos = areaFile.getCurrentObjects();
			for(SystemObjectInformationInterface systemObjectInfo : actualObjectInfos) {
				objects.add(createSystemObject(configurationArea, systemObjectInfo));
			}
		}
		return objects;
	}

	/**
	 * Gibt zu einem Konfigurationsbereich die noch nicht aktuellen Objekte zurück.
	 *
	 * @param configurationArea der Konfigurationsbereich
	 *
	 * @return die zukünftigen Objekte des angegebenen Konfigurationsbereichs
	 *
	 * @see ConfigurationArea
	 */
	Collection<SystemObject> getNewObjects(ConfigurationArea configurationArea) {
		final Collection<SystemObject> objects = new ArrayList<SystemObject>();
		final ConfigurationAreaFile areaFile = getConfigurationFileManager().getAreaFile(configurationArea.getPid());
		if(areaFile != null) {
			final SystemObjectInformationInterface[] newObjectInfos = areaFile.getNewObjects();
			for(SystemObjectInformationInterface systemObjectInfo : newObjectInfos) {
				objects.add(createSystemObject(configurationArea, systemObjectInfo));
			}
		}
		return objects;
	}

	/**
	 * Bestimmt die Attributgruppenverwendung mit der angegebenen Datenverteiler-Identifizierung.
	 *
	 * @param usageIdentification Identifizierung dieser Attributgruppenverwendung bei der Kommunikation über den Datenverteiler.
	 *
	 * @return Zur Identifizierung gehörende Attributgruppenverwendung.
	 */
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


	/**
	 * Prüft, ob eventuelle Abhängigkeiten zwischen den Bereichen aufgelöst werden können.
	 * <p/>
	 * Bei optionalen Abhängigkeiten, die nicht aufgelöst werden können, wird eine Warnung ausgegeben.
	 * <p/>
	 * Bei notwendigen Abhängigkeiten, die nicht aufgelöst werden können, wird ein Error Ausgegeben. Nachdem alle nicht aufgelösten notwendigen Abhängigkeiten
	 * ausgegeben wurden, wird eine Exception geworfen.
	 * <p/>
	 * Wurden bei einem Bereich noch keine Abhängigkeiten geprüft, wird eine Warnung ausgegeben.
	 *
	 * @param areasAndVersions Bereiche und deren Versionen die auf Abhängigikeit geprüft werden sollen.
	 *
	 * @throws IllegalStateException Es wurden notwendige Abhängigkeiten zwischen Bereichen gefunden, die nicht aufgelöst werden konnten.
	 */
	private void checkAreaDependency(Collection<ConfigAreaAndVersion> areasAndVersions) {
		final AreaDependencyChecker dependencyChecker = new AreaDependencyChecker();
		final List<ConfigAreaAndVersion> helper = new ArrayList<ConfigAreaAndVersion>(areasAndVersions);
		final AreaDependencyCheck.AreaDependencyCheckResult dependencyCheckResult = dependencyChecker.checkAreas(helper);

		dependencyChecker.printAndVerifyAreaDependencyCheckResult(dependencyCheckResult);
	}

	/**
	 * Setzt das Flag, mit dem die Konsistenzprüfung entscheiden soll, ob doppelte Pids in verschiedenen Konfigurationsbereichen erlaubt sind.
	 *
	 * @param allowDoublePids <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 */
	public void setAllowDoublePids(final boolean allowDoublePids) {
		_allowDoublePids = allowDoublePids;
	}

	/**
	 * Liefert das Flag, mit dem die Konsistenzprüfung entscheiden soll, ob doppelte Pids in verschiedenen Konfigurationsbereichen erlaubt sind.
	 *
	 * @return <code>true</code> falls doppelte Pids in verschiedenen Konfigurationsbereichen von der Konsistenzprüfung zugelassen werden sollen.
	 */
	public boolean getAllowDoublePids() {
		return _allowDoublePids;
	}

	/**
	 * Setzt das Flag, mit dem die Konsistenzprüfung entscheidet, ob Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden oder zum
	 * Abbruch führen sollen. werden sollen.
	 *
	 * @param ignoreDependencyErrorsInConsistencyCheck
	 *         <code>true</code> falls Fehler bei der Prüfung der Abhängigkeiten in der Konsistenzprüfung ignoriert werden sollen.
	 */
	public void setIgnoreDependencyErrorsInConsistencyCheck(final boolean ignoreDependencyErrorsInConsistencyCheck) {
		_ignoreDependencyErrorsInConsistencyCheck = ignoreDependencyErrorsInConsistencyCheck;
	}


	public boolean getIgnoreDependencyErrorsInConsistencyCheck() {
		return _ignoreDependencyErrorsInConsistencyCheck;
	}

	public void saveSetElementsFileLater(final ConfigMutableSet configMutableSet) {
		_dirtyMutableSets.add(configMutableSet);
	}

	public void saveSetElementsFiles() {
		for(ConfigMutableSet dirtyMutableSet : _dirtyMutableSets) {
			dirtyMutableSet.saveElementsData();
		}
	}

	/**
	 * Leitet die Aktualisierungsnachrichten bzgl. Änderungen von dynamischen Mengen und dynamischen Typen an das entsprechende Verwaltungsobjekt weiter.
	 *
	 * @param mutableCollectionSupport Verwaltungsobjekt für Aktualisierungsnachrichten
	 * @param simulationVariant        Simulationsvariante der Änderung
	 * @param addedElements            Hinzugefügte Elemente der dynamischen Zusammenstellung
	 * @param removedElements          Entfernte Elemente der dynamischen Zusammenstellung
	 */
	public void sendCollectionChangedNotification(
			final ConfigMutableCollectionSupport mutableCollectionSupport,
			final short simulationVariant,
			final List<SystemObject> addedElements,
			final List<SystemObject> removedElements) {
		mutableCollectionSupport.collectionChanged(simulationVariant, addedElements, removedElements);
	}

	/**
	 * Gibt das Verzeichnis für Sicherungen der Konfigurationsdateien zurück
	 *
	 * @return das Verzeichnis, in dem Konfigurationsdateien gesichert werden sollen. null wenn keines festgelegt wurde
	 */
	public File getBackupBaseDirectory() {
		return _backupBaseDirectory;
	}

	/**
	 * Setzt das Verzeichnis, in dem Konfigurationsdateien gesichert werden sollen
	 *
	 * @param backupBaseDirectory das Verzeichnis, in dem Konfigurationsdateien gesichert werden sollen
	 */
	public void setBackupBaseDirectory(final File backupBaseDirectory) {
		_backupBaseDirectory = backupBaseDirectory;
	}

	/**
	 * Setzt die Benutzerverwaltung
	 *
	 * @param userManagement Benutzerverwaltungsklasse
	 */
	public void setUserManagement(final ConfigAuthentication userManagement) {
		_userManagement = userManagement;
	}

	/**
	 * Gibt die Benutzerverwaltung zurück, falls über setUserManagement festgelegt
	 *
	 * @return eine ConfigAuthentication oder null falls keine festgelegt wurde
	 */
	public ConfigAuthentication getUserManagement() {
		return _userManagement;
	}
}

