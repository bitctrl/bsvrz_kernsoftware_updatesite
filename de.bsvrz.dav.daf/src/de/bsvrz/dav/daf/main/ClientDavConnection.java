/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.communication.dataRepresentation.data.DataFactory;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionInfo;
import de.bsvrz.dav.daf.communication.protocol.ClientConnectionProperties;
import de.bsvrz.dav.daf.communication.protocol.ClientHighLevelCommunication;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestManager;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.*;
import de.bsvrz.dav.daf.main.impl.archive.request.StreamedRequestManager;
import de.bsvrz.dav.daf.main.impl.config.AttributeGroupUsageIdentifications;
import de.bsvrz.dav.daf.main.impl.config.DafDataModel;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.timeout.TimeoutTimer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Diese Klasse repräsentiert die logische Verbindung zum Datenverteiler.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */

public class ClientDavConnection implements ClientDavInterface {

	private static final Debug _debug = Debug.getLogger();

	/** Die höhere Ebene der Kommunikation */
	private volatile ClientHighLevelCommunication _highLevelCommunication;

	/** Der Cachemanager */
	private CacheManager _cacheManager;

	/** Der Konfigurationsmanager */
	private ConfigurationManager _configurationManager;

	/** Der Anmeldemanager */
	private SubscriptionManager _subscriptionManager;

	/** Die Startparameter */
	private ClientDavParameters _clientDavParameters;

	/** Monitorobjekt mit dem Aktualisierungen von impliziten Anmeldungen kommuniziert werden. */
	private Object _implicitSubscriptionDataReceived;

	/** Die Liste wo die Implizitanmeldungen eingetragen werden. */
	private Hashtable<BaseSubscriptionInfo, ImplicitSubscriptionNote> _implicitSubscriptions;

	/** Der Abmelder der Implizitanmeldungen */
	private ImplicitUnsubscriber _implicitUnsubscriber;

	/** Empfänger für Implizitanmeldungen */
	private ImplicitReceiver _implicitReceiver;

	/** Das DataModel Objekt dieser Verbindung. */
	private DataModel _dataModel;

	/** Das Objekt, das für das Schliessen nach Auftritt eines Fehlers zuständig ist. */
	private DavCloseHandler _closeHandler;

	/** Information über den Verbindungsstatus. */
	private boolean _isConnected = false;

	/** Information über den Authentifizierungsstatus. */
	private boolean _isLoggedIn = false;

	/** Simulationszeithilfsobjekt */
	private SimulationTime _simulationTime = null;

	/** verwaltet Archivantworten */
	private StreamedRequestManager _streamedRequestManager;

	/** Enthält die Beobachter, die eine Mitteilung erhalten wollen, wenn die Verbindung zum Datenverteiler terminiert wird. */
	private List<DavConnectionListener> _connectionListeners = new CopyOnWriteArrayList<DavConnectionListener>();

	/**
	 * Diese Map speichert zu einer Attributgruppe, den dazugehörigen "leeren" Datensatz, der vom Datenverteiler zur Verfügung gestellt wurde. Alle Attribute des
	 * Datensatzes enthalten entweder den "Default-Wert", falls dieser definiert wurde, oder den sogenannten "undefiniert Wert". Der Datensatz der Map ist nicht
	 * änderbar und wird nicht zurückgegeben. Falls der Datensatz gebraucht wird, wird eine "modifizierbare" Kopie angelegt und diese zurückgegeben. <p> Die Map
	 * ist nicht synchronisiert. <p> Als Schlüssel dient die Attributgruppe, als Value wird ein nicht modifizierbarer Datensatz gespeichert der benutzt wird um
	 * eine modifizierbare Kopie zu erzeugen.
	 */
	private Map<AttributeGroup, Data> _defaultData = new HashMap<AttributeGroup, Data>();

	/**
	 * Hält fest, ob sich eine Applikation explizit selbst um die Fertigmeldung kümmert oder nicht. Aus Kompatibilitätsgründen wird angenommen, dass die
	 * Applikation sich nicht um eine Fertigmeldung kümmert. Das Flag kann mit der Methode {@link #enableExplicitApplicationReadyMessage()} gesetzt werden.
	 */
	private boolean _applicationSendsDoneMessage = false;

	/** Der Sender, der die Fertigmeldung verschickt. */
	private ClientSenderInterface _readyMessageSender = null;

	private boolean _reinitializeOnConnect = false;

	private ClientDavRequester _clientDavRequester = null;

	private TransactionManager _transactionManager = null;

	private ClientDavConnection _dataModelConnection = null;
	
	private final Object _lock;

	/**
	 * Erzeugt eine neue logische Datenverteilerverbindung mit Default-Parametern.
	 *
	 * @throws MissingParameterException Wenn notwendige Informationen nicht in den Default-Parametern spezifiziert wurden.
	 */
	public ClientDavConnection() throws MissingParameterException {
		this(null, null);
	}

	/**
	 * Erzeugt eine neue logische Datenverteilerverbindung mit den angegebenen Parametern.
	 *
	 * @param parameters Parameter für die Datenverteiler-Applikationsfunktionen.
	 *
	 * @throws de.bsvrz.dav.daf.main.MissingParameterException
	 *          Wenn notwendige Informationen nicht in den übergebenen Parametern spezifiziert wurden.
	 */
	public ClientDavConnection(ClientDavParameters parameters) throws MissingParameterException {
		this(parameters, null);
	}

	/**
	 * Erzeugt eine neue logische Datenverteilerverbindung mit den angegebenen Parametern. Es wird keine Verbindung zur Konfiguration aufgebaut, sämtliche
	 * konfigurierenden Anfragen werden über das angegebene Datenmodell abgewickelt. Dieser Konstruktor ist für die Konfigurations-Applikation vorgesehen, da diese
	 * ihr Datenmodell selbst verwaltet.
	 *
	 * @param parameters Parameter für die Datenverteiler-Applikationsfunktionen.
	 * @param dataModel  Das zu verwendende Datenmodell für Konfigurationsanfragen.
	 *
	 * @throws de.bsvrz.dav.daf.main.MissingParameterException Wenn notwendige Informationen nicht in den übergebenen Parametern spezifiziert wurden.
	 */
	public ClientDavConnection(ClientDavParameters parameters, DataModel dataModel) throws MissingParameterException {
		this(parameters, dataModel, new Object());
	}

	/**
	 * Erzeugt eine neue logische Datenverteilerverbindung mit den angegebenen Parametern. Es wird keine Verbindung zur Konfiguration aufgebaut, sämtliche
	 * konfigurierenden Anfragen werden über das angegebene Datenmodell abgewickelt. Dieser Konstruktor ist für die Konfigurations-Applikation vorgesehen, da diese
	 * ihr Datenmodell selbst verwaltet.
	 *
	 * @param parameters Parameter für die Datenverteiler-Applikationsfunktionen.
	 * @param dataModel  Das zu verwendende Datenmodell für Konfigurationsanfragen.
	 * @param lock Objekt, welches zur Synchronisierung verwendet wird. Wird eine zweite Verbindung aufgebaut, synchronisieren beide Objekte auf
	 *             dasselbe Lock-Objekt um Deadlocks zu vermeiden. (Ansonsten passiert es sehr schnell, dass jede Verbindung das eigene Lock
	 *             hält und irgendeine Aktion auf der anderen Verbindung triggert, was dann zu einem Deadlock führt.)
	 * @throws MissingParameterException Wenn notwendige Informationen nicht in den übergebenen Parametern spezifiziert wurden.
	 */
	protected ClientDavConnection(ClientDavParameters parameters, DataModel dataModel, final Object lock) throws MissingParameterException {
		_lock = lock;

		if(parameters == null || !parameters.isSecondConnection()) {

			// Release-Info, bei 2. Verbindung unterdrücken

			StringBuilder info = new StringBuilder();
			info.append("Datenverteiler-Applikationsfunktionen 'de.bsvrz.dav.daf'");
			try {
				final Class<?> infoClass = Class.forName("de.bsvrz.dav.daf.PackageRuntimeInfo");
				final Object release = infoClass.getMethod("getRelease").invoke(null);
				info.append(", Release: ").append(release);
				final Object revision = infoClass.getMethod("getRevision").invoke(null);
				info.append(", Version: ").append(revision);
				final Object compileTime = infoClass.getMethod("getCompileTime").invoke(null);
				info.append(", Stand: ").append(compileTime);
			}
			catch(Exception e) {
				info.append(" <Zugriff auf Release-Informationen nicht möglich> ");
				info.append(e);
			}

			_debug.info(info.toString());

		}


		if(parameters == null) {
			_clientDavParameters = new ClientDavParameters();
		}
		else {
			if(parameters.getUseSecondConnection()) { // Kann in Zukunft in "if (true)" geändert werden sobald alle Applikationen kompatibel sind
				_clientDavParameters = parameters.clone(true);
			}
			else {
				// Kompatibilität: Für alte Applikationen keine Kopie erzeugen, sondern bisheriges Objekt weiterverwenden
				_clientDavParameters = parameters;

				// macht das Objekt derzeit nicht wirklich readonly, sorgt aber dafür, dass Warnungen beim nachträglichen Ändern entstehen
				_clientDavParameters.setReadonly(true);
			}
		}
		if(dataModel == null) {
			createDataModel();
		}
		else {
			this._dataModel = dataModel;
		}
		_closeHandler = new DavCloseHandler(new SystemTerminator(), this);
		_implicitSubscriptionDataReceived = new Object();
		_implicitSubscriptions = new Hashtable<BaseSubscriptionInfo, ImplicitSubscriptionNote>();
//		implicitUnsubscriber = new ImplicitUnsubscriber();
//		implicitUnsubscriber.start();
		_implicitReceiver = new ImplicitReceiver();

		Runtime.getRuntime().addShutdownHook(new ConnectionShutdownHook(this));
	}

	private void createDataModel() {
		ClientDavParameters secondConnectionParameters = _clientDavParameters.getSecondConnectionParameters();
		if(secondConnectionParameters != null){
			if(_dataModelConnection == null) {
				try {
					_dataModelConnection = new ClientDavConnection(secondConnectionParameters, null, _lock);
				}
				catch(MissingParameterException e) {
					// Sollte nicht vorkommen, da die 2. Verbindung praktisch identische Parameter verwendet, wie die erste
					throw new RuntimeException(e);
				}
				_dataModelConnection.addConnectionListener(
						new DavConnectionListener() {
							@Override
							public void connectionClosed(final ClientDavInterface connection) {
								if(_isConnected) {
									disconnect(false, "");
								}
							}
						}
				);
				_dataModelConnection.setCloseHandler(
						new ApplicationCloseActionHandler() {
							@Override
							public void close(final String error) {
								_debug.fine("Zweite Verbindung wurde geschlossen", error);
								// Wenn die zweite Verbindung durch einen Fehler terminiert wird, wird zuerst der DavConnectionListener aufgerufen, der die erste
								// Verbindung (ohne Fehler) beendet, wodurch dort der Close-Handler nicht aufgerufen wird.
								// Daher muss jetzt noch der Close-Handler aufgerufen werden.
								_closeHandler.close(error);
							}
						}
				);
			}
			this._dataModel = _dataModelConnection.getDataModel();
			((DafDataModel)this._dataModel).setPublicConnection(this);
		}
		else {
			this._dataModel = new DafDataModel(this);
		}
	}

	@Override
	public void setCloseHandler(ApplicationCloseActionHandler closer) {
		DavCloseHandler davCloseHandler = new DavCloseHandler(closer, this);
		this._closeHandler = davCloseHandler;
		if(_highLevelCommunication != null) {
			_highLevelCommunication.setCloseHandler(davCloseHandler);
		}
	}

	@Override
	public final long getTime() {
		if(_simulationTime == null) {
			short simulationVariant = _clientDavParameters.getSimulationVariant();
			if(simulationVariant == 0) {
				return System.currentTimeMillis();
			}
			else {
				_simulationTime = new SimulationTime(simulationVariant, this);
			}
		}
		return _simulationTime.getTime();
	}

	@Override
	public final void sleep(long timeToSleep) {
		if(_simulationTime == null) {
			short simulationVariant = _clientDavParameters.getSimulationVariant();
			if(simulationVariant == 0) {
				try {
					Thread.sleep(timeToSleep);
				}
				catch(InterruptedException ex) {
				}
				return;
			}
			else {
				_simulationTime = new SimulationTime(simulationVariant, this);
			}
		}
		_simulationTime.sleep(timeToSleep);
	}

	@Override
	public final void sleepUntil(long absolutTime) {
		if(_simulationTime == null) {
			short simulationVariant = _clientDavParameters.getSimulationVariant();
			if(simulationVariant == 0) {
				while(System.currentTimeMillis() < absolutTime) {
					try {
						Thread.sleep(100);
					}
					catch(InterruptedException ex) {
						break;
					}
				}
				return;
			}
			else {
				_simulationTime = new SimulationTime(simulationVariant, this);
			}
		}
		_simulationTime.sleepUntil(absolutTime);
	}

	@Override
	public ArchiveRequestManager getArchive() {
		return getArchive(getLocalConfigurationAuthority());
	}

	@Override
	public ArchiveRequestManager getArchive(SystemObject archiveSystem) {
		synchronized(_lock) {
			if(_streamedRequestManager == null) {
				_streamedRequestManager = new StreamedRequestManager(this, _clientDavParameters.getSimulationVariant());
			}
		}
		return _streamedRequestManager.getArchiveRequester(archiveSystem);
	}

	@Override
	public final void connect() throws CommunicationError, ConnectionException {
		synchronized(_lock) {
			if(_isConnected) {
				throw new RuntimeException("Verbindung zum Datenverteiler besteht bereits.");
			}
			if(_dataModelConnection != null) {
				_dataModelConnection.connect();
				_dataModel = _dataModelConnection.getDataModel();
				((DafDataModel)this._dataModel).setPublicConnection(this);
			}
			if(_reinitializeOnConnect) {
				if(_dataModel instanceof DafDataModel && _dataModelConnection == null) {
					createDataModel();
				}
				_closeHandler.reset();
				_cacheManager = null;
				_configurationManager = null;
				_subscriptionManager = null;
				if(_implicitUnsubscriber != null) {
					try {
						_implicitUnsubscriber.interrupt();
						_implicitUnsubscriber.join();
					}
					catch(InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				_implicitSubscriptionDataReceived = new Object();
				_implicitSubscriptions = new Hashtable<BaseSubscriptionInfo, ImplicitSubscriptionNote>();
				_implicitUnsubscriber = null;
				_streamedRequestManager = null;
				_defaultData = new HashMap<AttributeGroup, Data>();
				_readyMessageSender = null;
			}
			
			_reinitializeOnConnect = true;
			if(_highLevelCommunication != null) {
				_highLevelCommunication.terminate(false, "Verbindung soll erneut aufgebaut werden");
			}
			_highLevelCommunication = new ClientHighLevelCommunication(_clientDavParameters, _closeHandler);
			_highLevelCommunication.setConnectionListener(
					new DavConnectionListener() {
						@Override
						public void connectionClosed(ClientDavInterface nullConnection) {
							notifyConnectionClosed();
						}
					}
			);
			_isConnected = true;
			_isLoggedIn = false;
			if(_implicitUnsubscriber == null) {
				_implicitUnsubscriber = new ImplicitUnsubscriber();
				_implicitUnsubscriber.start();
			}
		}
	}

	@Override
	public final void disconnect(boolean error, String message) {
		// Diese Methode nicht synchonisiert ausführen, da es sonst zu (beinahe-)Deadlocks kommt, wenn andere synchronisierte Methoden
		// wie login() gleichzeitig laufen.
		// Länger dauernde Aufräumarbeiten können in notifyConnectionClosed() durchgeführt werden
		ClientHighLevelCommunication highLevelCommunication = _highLevelCommunication;
		if(highLevelCommunication != null) {
			String terminateMessage;
			terminateMessage = error
					? "Verbindung wird applikationsseitig wegen eines Fehlers geschlossen"
					: "Verbindung wird auf Wunsch der Applikation geschlossen";
			if(message != null && message.length() > 0) terminateMessage += ": " + message;
			highLevelCommunication.terminate(error, terminateMessage);
		}
		else {
			_debug.warning("Es gibt keine Verbindung zum Datenverteiler, die abgebaut werden könnte.");
		}
	}

	@Override
	public final void login() throws InconsistentLoginException, CommunicationError {
		synchronized(_lock) {
			if(!_isConnected) {
				throw new RuntimeException("Datenverteilerverbindung muss vor der Authentifizierung zuerst mit connect() aufgebaut werden.");
			}
			if(_isLoggedIn) {
				throw new RuntimeException("Authentifizierung wurde bereits durchgeführt.");
			}
			if(_dataModelConnection != null) {
				// 2. Verbindung initialisieren
				_dataModelConnection.login();
			}
			if(_highLevelCommunication != null) {
				ClientConnectionProperties properties = _highLevelCommunication.getConnectionProperties();
				if(properties.getUserName().equals("")) {
					throw new IllegalStateException("Benutzername zur Authentifizierung sollte mit -benutzer=... angegeben werden");
				}
				if(properties.getUserPassword().equals("")) {
					throw new IllegalStateException("Passwort zur Authentifizierung sollte in der mit -authentifizierung=... spezifizierten Datei enthalten sein");
				}
				_highLevelCommunication.connect();
				long configurationId = _highLevelCommunication.getConfigurationId();
				_subscriptionManager = new SubscriptionManager(_clientDavParameters);
				String applicationName = _clientDavParameters.getApplicationNameForLocalConfigurationCache();
				if(_clientDavParameters.getIncarnationName().length() > 0)
					applicationName = applicationName + "-" + _clientDavParameters.getIncarnationName();
				if(_dataModelConnection != null) {
					_configurationManager = _dataModelConnection._configurationManager;
				}
				else {
					_configurationManager = new ConfigurationManager(
							configurationId, _clientDavParameters.getConfigurationPid(), _clientDavParameters
							.getConfigurationPath(), applicationName, _dataModel
					);
				}
				_cacheManager = new CacheManager(_subscriptionManager, _configurationManager);
				//			archiveManager = new ArchiveManager(subscriptionManager, configurationManager, cacheManager, configurationId);
				_highLevelCommunication.completeInitialisation(_configurationManager, _cacheManager, _subscriptionManager);
				_subscriptionManager.completeInitialisation(_dataModelConnection != null);
				long time = System.currentTimeMillis();
				long sleepTime = 10;
				while(!_subscriptionManager.isInitialisationComplete()) {
					if(_highLevelCommunication.isDisconnecting()){
						throw new CommunicationError("Verbindung zum Datenverteiler wurde unterbrochen");
					}
					else if(System.currentTimeMillis() - time < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
						try {
							Thread.sleep(sleepTime);
							if(sleepTime < 1000) sleepTime *= 2;
						}
						catch(InterruptedException ex) {
							return;
						}
					}
					else {
						throw new CommunicationError("Konfiguration ist nicht erreichbar.");
					}
				}
				if(_dataModelConnection != null) {
					_subscriptionManager.setConfigurationManager(_configurationManager);
				}
				else {
					_configurationManager.completeInitialisation(_subscriptionManager);
				}
				_isLoggedIn = true;

				// Auf Meta-Seite wird die Verbindung zur Konfiguration hergestellt
				if(_dataModel instanceof DafDataModel && _dataModelConnection == null) {
					((DafDataModel)_dataModel).createRequester();
					((DafDataModel)_dataModel).loadLocalConfigurationCache();
				}

				// _applicationSendsDoneMessage steht auf true, falls die Applikation explizit selbst die Fertigmeldung versenden will,
				// auf False andernfalls (default);
				sendApplicationReadyMessage(_applicationSendsDoneMessage);
			}
		}
	}

	@Override
	public final void login(String userName, String password) throws InconsistentLoginException, CommunicationError {
		synchronized(_lock) {
			if(userName == null || userName.equals(""))
				throw new IllegalArgumentException("Benutzername zur Authentifizierung muss angegeben werden");
			if(password == null || password.equals(""))
				throw new IllegalArgumentException("Passwort zur Authentifizierung muss angegeben werden");
			if(_highLevelCommunication != null) {
				ClientConnectionProperties properties = _highLevelCommunication.getConnectionProperties();
				properties.setUserName(userName);
				properties.setUserPassword(password);
			}
			if(_dataModelConnection != null && _dataModelConnection._highLevelCommunication != null) {
				ClientConnectionProperties properties = _dataModelConnection._highLevelCommunication.getConnectionProperties();
				properties.setUserName(userName);
				properties.setUserPassword(password);
			}
			login();
		}
	}

	/**
	 * Gibt die Aspektumleitung für eine Kombination von Attributgruppe und Aspekt zurück. Wenn keine entsprechende Aspektumleitung besteht, wird der übergebene
	 * Original-Aspekt zurückgegeben.
	 *
	 * @param attributeGroup die Attributgruppe
	 * @param aspect         der Aspekt
	 *
	 * @return Den zu verwendenden Aspekt, falls es eine Aspektumleitung gibt, sonst den übergebenen Aspekt.
	 */
	private Aspect aspectToSubstitute(AttributeGroup attributeGroup, Aspect aspect) {
		String oldAspectPid = aspect.getPid();
		String newAspectPid = _clientDavParameters.aspectToSubstitute(attributeGroup.getPid(), oldAspectPid);
		if(!oldAspectPid.equals(newAspectPid)) {
			if(_configurationManager != null && _dataModel != null) {
				final Aspect asp = _dataModel.getAspect(newAspectPid);
				if(asp != null) {
					return asp;
				}
			}
		}
		return aspect;
	}

	/**
	 * Überprüft, ob die angegebene Kombination von Objekt, Attributgruppe und Aspekt zulässig ist.
	 *
	 * @param objects zu prüfende Objekte
	 * @param atg     zu prüfende Attributgruppe
	 * @param aspect  zu prüfenden Aspekt
	 * @param context Kontext, von dem diese Prüfung vorgenommen wird
	 *
	 * @throws IllegalArgumentException Wenn die angegebene Kombination von Objekt, Attributgruppe und Aspekt nicht zulässig ist.
	 */
	private void checkDataIdentification(SystemObject[] objects, AttributeGroup atg, Aspect aspect, String context) {
		String message = context + ": ";
		if(objects == null || atg == null || aspect == null) {
			if(objects == null) {
				message = "Angegebenes Objekt-Array ist null, Attributgruppe: " + atg + ", Aspekt: " + aspect;
			}
			else if(atg == null) {
				message = "Angegebene Attributgruppe ist null, Aspekt: " + aspect + ", Objekte: " + Arrays.asList(objects);
			}
			else if(aspect == null) {
				message = "Angegebener Aspekt ist null, Attributgruppe: " + atg + ", Objekte: " + Arrays.asList(objects);
			}
			_debug.error(message);
			throw new IllegalArgumentException(message);
		}

		

		// Dies muss so gemacht werden, da es sonst zu einer Endlosschleife bei getAspects kommt.
		// Die ATGVerwendung kann nur mit einem Datensatz aufgelöst werden, dafür wird wieder getAspects benötigt -> Endlos
		if("atg.konfigurationsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.antwort".equals(aspect.getPid())) return;
		if("atg.konfigurationsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.anfrage".equals(aspect.getPid())) return;
		if("atg.konfigurationsAnfrageSchnittstelleLesend".equals(atg.getPid()) && "asp.antwort".equals(aspect.getPid())) return;
		if("atg.konfigurationsAnfrageSchnittstelleLesend".equals(atg.getPid()) && "asp.anfrage".equals(aspect.getPid())) return;
		if("atg.konfigurationsAnfrageSchnittstelleSchreibend".equals(atg.getPid()) && "asp.antwort".equals(aspect.getPid())) return;
		if("atg.konfigurationsAnfrageSchnittstelleSchreibend".equals(atg.getPid()) && "asp.anfrage".equals(aspect.getPid())) return;
		if("atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.antwort".equals(aspect.getPid())) return;
		if("atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.anfrage".equals(aspect.getPid())) return;
		if("atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.antwort".equals(aspect.getPid())) return;
		if("atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle".equals(atg.getPid()) && "asp.anfrage".equals(aspect.getPid())) return;

		Collection<Aspect> atgAspects = atg.getAspects();

		if(!atgAspects.contains(aspect)) {
			message +=
					"Aspekt " + aspect + " darf nicht in Kombination mit Attributgruppe " + atg + " verwendet werden" + ", Objekte: " + Arrays.asList(objects);
			_debug.error(message);
			throw new IllegalArgumentException(message);
		}
		SystemObjectType lastCheckedType = null;
		for(int i = 0; i < objects.length; i++) {
			final SystemObject object = objects[i];
			if(object == null) {
				message += "null-Objekt, Attributgruppe: " + atg + ", Aspekt: " + aspect;
				_debug.error(message);
				throw new IllegalArgumentException(message);
			}
			else {
				final SystemObjectType type = object.getType();
				if(type != lastCheckedType) {
					List<? extends AttributeGroup> typeAtgs = type.getAttributeGroups();
					if(!typeAtgs.contains(atg)) {
						message += "Attributgruppe " + atg + " darf nicht in Kombination mit Objekten vom Typ " + object.getType()
						           + " verwendet werden, Aspekt: " + aspect + ", Object: " + object;
						_debug.error(message);
						throw new IllegalArgumentException(message);
					}
					lastCheckedType = type;
				}
			}
		}
	}

	@Override
	public final ResultData[] getCachedData(
			SystemObject[] objects, DataDescription dataDescription, ReceiveOptions options, HistorySpecification history) {
		synchronized(_lock) {
			if(_cacheManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
			}
			if(objects == null) throw new IllegalArgumentException("objects darf nicht null sein");
			if(dataDescription == null) throw new IllegalArgumentException("dataDescription darf nicht null sein");
			if(options == null) throw new IllegalArgumentException("options darf nicht null sein");
			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if(attributeGroup == null) throw new IllegalArgumentException("Attributgruppe darf nicht null sein");
			if(aspect == null) throw new IllegalArgumentException("Aspekt darf nicht null sein");
			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

			checkDataIdentification(objects, attributeGroup, _aspect, "CacheAbfrage");

			// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
			// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
			short externalSimulationVariant = dataDescription.getSimulationVariant();
			if(externalSimulationVariant == (short) -1) {
				externalSimulationVariant = _clientDavParameters.getSimulationVariant();
			}

			boolean delayedDataFlag = options.withDelayed();
			final ArrayList<ResultData> list = new ArrayList<ResultData>();
			for(int i = 0; i < objects.length; ++i) {
				SystemObject object = objects[i];
				if(object != null) {
					BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
							object.getId(), attributeGroup.getAttributeGroupUsage(_aspect), externalSimulationVariant
					);
					List results = null;
					if(history.isCountSpecification()) {
						results = _cacheManager.getCachedData(baseSubscriptionInfo, delayedDataFlag, history.getCount());
					}
					else {
						long fromTime = history.getFromTime();
						long toTime = history.getToTime();
						if((toTime == -1) && (fromTime == -1)) {
							results = _cacheManager.getCachedData(baseSubscriptionInfo, delayedDataFlag, 1);
						}
						else {
							if(toTime == -1) {
								toTime = System.currentTimeMillis();
							}
							results = _cacheManager.getCachedData(baseSubscriptionInfo, delayedDataFlag, fromTime, toTime);
						}
					}
					if(results != null) {
						for(int j = 0; j < results.size(); ++j) {
							CachedObject cachedObject = (CachedObject) results.get(j);
							list.add(
									new ResultData(
											object, dataDescription, cachedObject.getDelayedDataFlag(), cachedObject.getDataNumber(), cachedObject
											.getDataTime(), cachedObject.getErrorFlag(), cachedObject.getData()
									)
							);
						}
					}
				}
			}
			ResultData results[] = new ResultData[list.size()];
			for(int i = 0; i < list.size(); ++i) {
				results[i] = list.get(i);
			}
			return results;
		}
	}

	@Override
	public final ResultData[] getData(SystemObject[] objects, DataDescription dataDescription, long unsubscriptionTime) {
		synchronized(_lock) {
			if(_cacheManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
			}
			if((objects == null) || (dataDescription == null)) {
				return null;
			}
			ResultData results[] = new ResultData[objects.length];
			for(int i = 0; i < objects.length; ++i) {
				if(objects[i] != null) {
					results[i] = getData(objects[i], dataDescription, unsubscriptionTime);
				}
			}
			return results;
		}
	}

	@Override
	public ResultData getData(SystemObject object, DataDescription dataDescription, long unsubscriptionTime) {
		synchronized(_lock) {
			if(_cacheManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
			}
			if(object == null) {
				throw new IllegalArgumentException("Objekt ist null");
			}
			if(dataDescription == null) {
				throw new IllegalArgumentException("dataDescription ist null");
			}

			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if(attributeGroup == null) {
				throw new IllegalArgumentException("Attributgruppe ist null");
			}
			if(aspect == null) {
				throw new IllegalArgumentException("Aspekt ist null");
			}
			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

			checkDataIdentification(new SystemObject[]{object}, attributeGroup, _aspect, "Datenabfrage");

			// Wenn in der Datadescription keine Simulationsvariante explizit vorgegeben wurde, dann muss für die Anmeldung beim Datenverteiler die über
			// Aufrufparameter von außen vorgebbare Simulationsvariante benutzt werden
			short externalSimulationVariant = dataDescription.getSimulationVariant();
			if(externalSimulationVariant == (short) -1) {
				externalSimulationVariant = _clientDavParameters.getSimulationVariant();
			}

			BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
					object.getId(), attributeGroup.getAttributeGroupUsage(_aspect), externalSimulationVariant
			);

			ImplicitSubscriptionNote implicitSubscriptionNote = _implicitSubscriptions.get(baseSubscriptionInfo);
			CachedObject cachedObject = _cacheManager.getLastValueOfCachedData(baseSubscriptionInfo, true);

			if(implicitSubscriptionNote == null && cachedObject == null) {
				SystemObject array[] = {object};
				DataDescription _dataDescription = dataDescription;
				if(!_aspect.equals(aspect)) {
					_dataDescription = dataDescription.getRedirectedDescription(_aspect);
				}
				implicitSubscriptionNote = new ImplicitSubscriptionNote();
				implicitSubscriptionNote.baseSubscriptionInfo = baseSubscriptionInfo;
				implicitSubscriptionNote.objects = array;
				implicitSubscriptionNote.dataDescription = _dataDescription;
				implicitSubscriptionNote.unsubscriptionTime =
						System.currentTimeMillis() + Math.max(unsubscriptionTime, CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE);
				_implicitSubscriptions.put(baseSubscriptionInfo, implicitSubscriptionNote);
				_subscriptionManager.subscribeReceiver(
						_implicitReceiver, array, _dataDescription, ReceiveOptions.normal(), ReceiverRole.receiver(), unsubscriptionTime
				);
			}

			synchronized(_implicitSubscriptionDataReceived) {
				final TimeoutTimer timer = new TimeoutTimer(CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE);
				long sleepTime = 10;
				while(cachedObject == null && !timer.isTimeExpired()) {
					try {
						if(sleepTime > 0) _implicitSubscriptionDataReceived.wait(sleepTime);
						cachedObject = _cacheManager.getLastValueOfCachedData(baseSubscriptionInfo, true);
						sleepTime = timer.getRemainingTime();
					}
					catch(InterruptedException ex) {
						throw new RuntimeException("Lesen eines Datensatzes wurde unterbrochen");
					}
				}
			}
			if(cachedObject == null) throw new RuntimeException("Timeout beim Lesen eines Datensatzes");
			if(implicitSubscriptionNote != null) {
				synchronized(implicitSubscriptionNote) {
					implicitSubscriptionNote.unsubscriptionTime = System.currentTimeMillis() + unsubscriptionTime;
				}
			}
			return new ResultData(
					object,
					dataDescription,
					cachedObject.getDelayedDataFlag(),
					cachedObject.getDataNumber(),
					cachedObject.getDataTime(),
					cachedObject.getErrorFlag(),
					cachedObject.getData()
			);
		}
	}

	@Override
	public final DataModel getDataModel() {
		return _dataModel;
	}

	@Override
	public DataModel getDataModel(final SystemObject configAuthority) throws ConfigurationTaskException {
		if(configAuthority == null) throw new IllegalArgumentException("configAuthority ist null");
		return getDataModel(configAuthority.getId());
	}

	@Override
	public final DataModel getDataModel(final String configAuthority) throws ConfigurationTaskException {
		if(configAuthority == null) throw new IllegalArgumentException("configAuthority ist null");
		final SystemObject configuration = _dataModel.getObject(configAuthority);
		if(configuration == null) throw new IllegalArgumentException("configuration konnte nicht gefunden werden");
		return getDataModel(configuration);
	}

	@Override
	public DataModel getDataModel(final long configAuthorityId) throws ConfigurationTaskException {
		if(configAuthorityId == _dataModel.getConfigurationAuthority().getId()) return _dataModel;

		if(_dataModelConnection != null){
			return _dataModelConnection.getDataModel(configAuthorityId);
		}

		final SystemObject conf = _dataModel.getObject(configAuthorityId);

		final DafDataModel model = new DafDataModel(this);
		final ConfigurationManager confManager = new ConfigurationManager(
				configAuthorityId, conf == null ? "" : conf.getPid(), null, getLocalApplicationObject().getName(), model
		);

		_subscriptionManager.addConfiguration(confManager, conf == null ? '[' + String.valueOf(configAuthorityId) + ']' : conf.getNameOrPidOrId());

		// Konfigurationsleseanfragen anmelden
		final BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
				configAuthorityId, AttributeGroupUsageIdentifications.CONFIGURATION_READ_REQUEST, (short)0
		);
		final SendSubscriptionInfo sendSubscriptionInfo = new SendSubscriptionInfo(baseSubscriptionInfo, SenderRole.sender(), true);
		_highLevelCommunication.sendSendSubscription(sendSubscriptionInfo);

		// Konfigurationsschreibanfragen anmelden
		final BaseSubscriptionInfo writeBaseSubscriptionInfo = new BaseSubscriptionInfo(
				configAuthorityId, AttributeGroupUsageIdentifications.CONFIGURATION_WRITE_REQUEST, (short)0
		);
		final SendSubscriptionInfo writeSendSubscriptionInfo = new SendSubscriptionInfo(writeBaseSubscriptionInfo, SenderRole.sender(), true);
		_highLevelCommunication.sendSendSubscription(writeSendSubscriptionInfo);

		_subscriptionManager.waitForInitialization(configAuthorityId);

		try{
			confManager.completeInitialisation(_subscriptionManager);
			model.createRequester();
		}
		catch(RuntimeException e){
			throw new ConfigurationTaskException(e);
		}
		catch(CommunicationError e){
			throw new ConfigurationTaskException(e);
		}

		return model;
	}

	@Override
	public final long getDavRoundTripTime() {
		synchronized(_lock) {
			if(_highLevelCommunication == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
			}
			try {
				return _highLevelCommunication.getTelegramTime(60000);
			}
			catch(CommunicationError error) {
				error.printStackTrace();
			}
			return -1;
		}
	}

	@Override
	public ConfigurationAuthority getLocalConfigurationAuthority() {
		if((_configurationManager == null) || (_dataModel == null) || (_subscriptionManager == null)) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
		}
		long configurationId = _subscriptionManager.getHighLevelCommunication().getConfigurationId();
		return (ConfigurationAuthority) _dataModel.getObject(configurationId);
	}

	@Override
	public final ClientApplication getLocalApplicationObject() {
		if((_configurationManager == null) || (_dataModel == null) || (_subscriptionManager == null)) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
		}
		long applicationId = _subscriptionManager.getHighLevelCommunication().getApplicationId();
		if(applicationId < 0) {
			return null;
		}
		return (ClientApplication) _dataModel.getObject(applicationId);
	}

	/**
	 * Gibt die Id des Stellvertreterobjekts für diese Applikation zurück. Die Erzeugung des entsprechenden Objekts in der Konfiguration wird vom Datenverteiler
	 * nach dem Verbindungsaufbau und der erfolgreichen Authentifizierung veranlasst.
	 *
	 * @return Id des Stellvertreterobjekts für die lokale Applikation.
	 */
	public long getLocalApplicationObjectId() {
		if((_configurationManager == null) || (_dataModel == null) || (_subscriptionManager == null)) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
		}
		return _subscriptionManager.getHighLevelCommunication().getApplicationId();
	}

	/**
	 * @throws InitialisationNotCompleteException
	 *          Wenn die Authentifizierung noch nicht erfolgreich durchgeführt wurde.
	 */
	@Override
	public final DynamicObject getLocalUser() {
		if((_configurationManager == null) || (_dataModel == null) || (_subscriptionManager == null)) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
		}
		long userId = _subscriptionManager.getHighLevelCommunication().getUserId();
		if(userId <= 0) {
			throw new InitialisationNotCompleteException("Authentifizierung noch nicht erfolgreich.");
		}
		return (DynamicObject) _dataModel.getObject(userId);
	}

	@Override
	public final DavApplication getLocalDav() {
		if((_configurationManager == null) || (_dataModel == null) || (_subscriptionManager == null)) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
		}
		long transmitterId = _subscriptionManager.getHighLevelCommunication().getDataTransmitterId();
		if(transmitterId < 0) {
			return null;
		}
		return (DavApplication) _dataModel.getObject(transmitterId);
	}

	@Override
	public final void sendData(ResultData result) throws DataNotSubscribedException, SendSubscriptionNotConfirmed {
		synchronized(_lock) {
			if(_subscriptionManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert.");
			}
			if(result == null) {
				throw new IllegalArgumentException("Das mit sendData zu versendende ResultData-Objekt ist null");
			}
			DataDescription dataDescription = result.getDataDescription();
			if(dataDescription == null) {
				throw new IllegalArgumentException("Die DataDescription des mit sendData zu versendenden ResultData-Objekt ist null");
			}
			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if(attributeGroup == null) {
				throw new IllegalArgumentException("Die Attributgruppe der DataDescription des mit sendData zu versendenden ResultData-Objekt ist null");
			}
			if(aspect == null) {
				throw new IllegalArgumentException("Der Aspekt der DataDescription des mit sendData zu versendenden ResultData-Objekt ist null");
			}
			DataDescription _dataDescription = dataDescription;
			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);
			if(!_aspect.equals(aspect)) {
				_dataDescription = dataDescription.getRedirectedDescription(_aspect);
			}
			Data data = result.getData();

			if(data != null) {
				if(data.getAttributeType() != null) {
					throw new IllegalArgumentException(
							"Der zu versendende Datensatz stellt keinen ganzen Datensatz dar sondern nur einen Teildatensatz vom Typ "
									+ data.getAttributeType().getPid()
					);
				}
				if(!data.getName().equals(attributeGroup.getPid())) {
					throw new IllegalArgumentException(
							"Die Attributgruppe des zu versendenden Data-Objekts (" + data.getName()
									+ ") entspricht nicht der Attributgruppe in der DataDescription: " + attributeGroup.getPid() + ")"
					);
				}
				if(!data.isDefined()) {
					// Der Datensatz kann nicht verschickt werden, weil mindestens ein Attribut den "undefiniert Wert" enthält
					throw new IllegalArgumentException("Der übergebene Datensatz enthält mindestens ein Attribut, das nicht definiert ist: " + data);
				}
				data = data.createUnmodifiableCopy();
			}

			ResultData _result = new ResultData(
					result.getObject(), _dataDescription, result.isDelayedData(), result.getDataIndex(), result.getDataTime(), result
					.getErrorFlag(), data
			);

			_subscriptionManager.sendData(_result);
		}
	}

	@Override
	public final void sendData(ResultData[] results) throws DataNotSubscribedException, SendSubscriptionNotConfirmed {
		synchronized(_lock) {
			if(results == null) {
				return;
			}
			for(int i = 0; i < results.length; ++i) {
				sendData(results[i]);
			}
		}
	}

	@Override
	public final void subscribeReceiver(
			ClientReceiverInterface receiver, Collection<SystemObject> objects, DataDescription dataDescription, ReceiveOptions options, ReceiverRole role) {
		synchronized(_lock) {
			subscribeReceiver(receiver, objects, dataDescription, options, role, 0);
		}
	}

	@Override
	public final void subscribeReceiver(
			ClientReceiverInterface receiver,
			Collection<SystemObject> objects,
			DataDescription dataDescription,
			ReceiveOptions options,
			ReceiverRole role,
			long cacheTime) {
		synchronized(_lock) {
			SystemObject objectsArray[] = new SystemObject[objects.size()];
			objectsArray = objects.toArray(objectsArray);
			subscribeReceiver(receiver, objectsArray, dataDescription, options, role, cacheTime);
		}
	}

	@Override
	public void subscribeReceiver(
			ClientReceiverInterface receiver, SystemObject object, DataDescription dataDescription, ReceiveOptions options, ReceiverRole role) {
		subscribeReceiver(receiver, new SystemObject[]{object}, dataDescription, options, role, 0);
	}

	@Override
	public void subscribeReceiver(
			ClientReceiverInterface receiver, SystemObject object, DataDescription dataDescription, ReceiveOptions options, ReceiverRole role, long cacheTime) {
		subscribeReceiver(receiver, new SystemObject[]{object}, dataDescription, options, role, cacheTime);
	}

	@Override
	public final void subscribeReceiver(
			ClientReceiverInterface receiver, SystemObject[] objects, DataDescription dataDescription, ReceiveOptions options, ReceiverRole role) {
		synchronized(_lock) {
			subscribeReceiver(receiver, objects, dataDescription, options, role, 0);
		}
	}

	@Override
	public final void subscribeReceiver(
			ClientReceiverInterface receiver,
			SystemObject[] objects,
			DataDescription dataDescription,
			ReceiveOptions options,
			ReceiverRole role,
			long cacheTime) {
		synchronized(_lock) {
			if(_subscriptionManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert..");
			}
			if((receiver == null)) throw new IllegalArgumentException("Parameter 'receiver' ist null");
			if((objects == null)) throw new IllegalArgumentException("Array mit Systemobjekten 'objects' ist null");
			if((dataDescription == null)) throw new IllegalArgumentException("Parameter 'dataDescription ist null");
			if((options == null)) throw new IllegalArgumentException("Parameter 'options' ist null");
			if((role == null)) throw new IllegalArgumentException("Parameter 'role' ist null");
			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if((attributeGroup == null)) {
				throw new IllegalArgumentException("Attributgruppe im Parameter 'dataDescription' ist null");
			}
			else if((aspect == null)) throw new IllegalArgumentException("Aspekt im Parameter 'dataDescription' ist null");
			for(int i = 0; i < objects.length; i++) {
				SystemObject object = objects[i];
				if(object == null) throw new IllegalArgumentException("Systemobjekt mit Index " + i + " im Parameter 'objects' ist null");
			}

			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

			checkDataIdentification(objects, attributeGroup, _aspect, "Empfangsanmeldung");

			DataDescription _dataDescription = dataDescription;
			if(!_aspect.equals(aspect)) {
				_dataDescription = dataDescription.getRedirectedDescription(_aspect);
			}
			_subscriptionManager.subscribeReceiver(receiver, objects, _dataDescription, options, role, cacheTime);
		}
	}

	@Override
	public void unsubscribeReceiver(ClientReceiverInterface receiver, Collection<SystemObject> objects, DataDescription dataDescription) {
		unsubscribeReceiver(receiver, objects.toArray(new SystemObject[objects.size()]), dataDescription);
	}

	@Override
	public void unsubscribeReceiver(ClientReceiverInterface receiver, SystemObject object, DataDescription dataDescription) {
		unsubscribeReceiver(receiver, new SystemObject[]{object}, dataDescription);
	}

	@Override
	public void unsubscribeReceiver(ClientReceiverInterface receiver, SystemObject[] objects, DataDescription dataDescription) {
		if(_subscriptionManager == null) {
			throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert..");
		}
		if((receiver == null) || (objects == null) || (dataDescription == null)) {
			throw new IllegalArgumentException("Ein Argument ist null");
		}
		AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
		Aspect aspect = dataDescription.getAspect();
		if((attributeGroup == null) || (aspect == null)) {
			throw new IllegalArgumentException("Attributgruppe oder Aspekt ist null");
		}
		Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

		checkDataIdentification(objects, attributeGroup, _aspect, "Empfangsabmeldung");

		DataDescription _dataDescription = dataDescription;
		if(!_aspect.equals(aspect)) {
			_dataDescription = dataDescription.getRedirectedDescription(_aspect);
		}
		_subscriptionManager.unsubscribeReceiver(receiver, objects, _dataDescription);
	}

	@Override
	public final void subscribeSender(
			ClientSenderInterface sender, Collection<SystemObject> objects, DataDescription dataDescription, SenderRole role)
			throws OneSubscriptionPerSendData {
		synchronized(_lock) {
			
			subscribeSender(sender, objects.toArray(new SystemObject[objects.size()]), dataDescription, role);
		}
	}

	@Override
	public void subscribeSender(ClientSenderInterface sender, SystemObject object, DataDescription dataDescription, SenderRole role)
			throws OneSubscriptionPerSendData {
		subscribeSender(sender, new SystemObject[]{object}, dataDescription, role);
	}

	@Override
	public final void subscribeSender(ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription, SenderRole role)
			throws OneSubscriptionPerSendData {
		synchronized(_lock) {
			if(_subscriptionManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert..");
			}
			if((sender == null)) throw new IllegalArgumentException("Parameter 'sender' ist null");
			if((objects == null)) throw new IllegalArgumentException("Parameter 'objects' ist null");
			if((dataDescription == null)) throw new IllegalArgumentException("Parameter 'dataDescription' ist null");
			if((dataDescription.getAttributeGroup() == null))
				throw new IllegalArgumentException("Attributgruppe im Parameter 'dataDescription' ist null");
			if((dataDescription.getAspect() == null)) throw new IllegalArgumentException("Aspekt im Parameter 'dataDescription' ist null");
			if((role == null)) throw new IllegalArgumentException("Parameter 'role' ist null");
			for(int i = 0; i < objects.length; i++) {
				SystemObject object = objects[i];
				if(object == null) throw new IllegalArgumentException("Das Systemobjekt mit Index " + i + " ist null");
			}
			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if((attributeGroup == null) || (aspect == null)) {
				throw new IllegalArgumentException("Attributgruppe oder Aspekt ist null");
			}
			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

			checkDataIdentification(objects, attributeGroup, _aspect, "Sendeanmeldung");

			DataDescription _dataDescription = dataDescription;
			if(!_aspect.equals(aspect)) {
				_dataDescription = dataDescription.getRedirectedDescription(_aspect);
			}
			_subscriptionManager.subscribeSender(sender, objects, _dataDescription, role);

			if(role.isSource()) {
				// Wenn Anmeldung als Quelle, dann leeren Datensatz für jedes angemeldete Objekt generieren
				long now = System.currentTimeMillis();
				for(int i = 0; i < objects.length; i++) {
					SystemObject object = objects[i];
					ResultData initialResult = new ResultData(object, dataDescription, now, null);
					try {
						sendData(initialResult);
					}
					catch(DataNotSubscribedException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					catch(SendSubscriptionNotConfirmed e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	public void subscribeSource(ClientSenderInterface sender, ResultData initialData) throws OneSubscriptionPerSendData {
		DataDescription dataDescription = initialData.getDataDescription();

		if((sender == null)) throw new IllegalArgumentException("Parameter 'sender' ist null");
		if((dataDescription == null)) throw new IllegalArgumentException("dataDescription im Parameter 'initialData' ist null");

		AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
		Aspect aspect = dataDescription.getAspect();

		if((attributeGroup == null)) throw new IllegalArgumentException("Attributgruppe im Parameter 'initialData' ist null");
		if((aspect == null)) throw new IllegalArgumentException("Aspekt im Parameter 'initialData' ist null");
		final SystemObject systemObject = initialData.getObject();
		if(systemObject == null) throw new IllegalArgumentException("Das Systemobjekt im Parameter 'initialData' ist null");

		Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

		SystemObject[] objects = new SystemObject[]{systemObject};
		checkDataIdentification(objects, attributeGroup, _aspect, "Quellanmeldung");

		DataDescription redirectedDataDescription = dataDescription;
		if(!_aspect.equals(aspect)) {
			redirectedDataDescription = dataDescription.getRedirectedDescription(_aspect);
		}
		_subscriptionManager.subscribeSender(sender, objects, redirectedDataDescription, SenderRole.source());

		try {
			sendData(initialData);
		}
		catch(Exception e) {
			RuntimeException runtimeException;
			if(e instanceof RuntimeException) {
				runtimeException = (RuntimeException)e;
			}
			else {
				runtimeException = new RuntimeException(e);
			}
			try {
				unsubscribeSender(sender, objects, dataDescription);
			}
			catch(Exception unsubscribeException) {
				_debug.warning(
						"Initialer Datensatz bei Anmeldung als Quelle konnte nicht versandt werden und Anmeldung konnte nicht rückgängig gemacht werden",
						unsubscribeException
				);
			}
			throw runtimeException;
		}
	}

	@Override
	public final void unsubscribeSender(ClientSenderInterface sender, Collection<SystemObject> objects, DataDescription dataDescription) {
		synchronized(_lock) {
			
			unsubscribeSender(sender, objects.toArray(new SystemObject[objects.size()]), dataDescription);
		}
	}

	@Override
	public void unsubscribeSender(ClientSenderInterface sender, SystemObject object, DataDescription dataDescription) {
		unsubscribeSender(sender, new SystemObject[]{object}, dataDescription);
	}

	@Override
	public final void unsubscribeSender(ClientSenderInterface sender, SystemObject[] objects, DataDescription dataDescription) {
		synchronized(_lock) {
			if(_subscriptionManager == null) {
				throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert..");
			}
			if((sender == null) || (objects == null) || (dataDescription == null)) {
				throw new IllegalArgumentException("Ein Argument ist null");
			}
			AttributeGroup attributeGroup = dataDescription.getAttributeGroup();
			Aspect aspect = dataDescription.getAspect();
			if((attributeGroup == null) || (aspect == null)) {
				throw new IllegalArgumentException("Attributgruppe oder Aspekt ist null");
			}
			Aspect _aspect = aspectToSubstitute(attributeGroup, aspect);

			checkDataIdentification(objects, attributeGroup, _aspect, "Sendeabmeldung");

			DataDescription _dataDescription = dataDescription;
			if(!_aspect.equals(aspect)) {
				_dataDescription = dataDescription.getRedirectedDescription(_aspect);
			}
			_subscriptionManager.unsubscribeSender(sender, objects, _dataDescription);
		}
	}

	@Override
	public Data createData(AttributeGroup attributeGroup) {
		// Es soll ein neuer Datensatz erzeugt werden. In dem Datensatz muss jedes Attribut entweder auf den Default-Wert
		// gesetzt werden oder aber auf den "undefiniert Wert", wenn kein Default-Wert definiert wurde.
		// Damit diese Prozedure nicht immer wiederholt werden muss (Datensatz anfordern, Default-Werte setzen)
		// wird diese einmal gemacht und dieser Datensatz gespeichert. Wird erneut ein Datensatz angefordert, wird
		// der gespeicherte Datensatz kopiert und diese Kopie zurückgegeben.

		// Speichert den Datensatz, der nicht geändert werden darf und als Kopiervorlage dienen soll
		Data masterCopy;

		synchronized(_defaultData) {
			masterCopy = _defaultData.get(attributeGroup);
			if(masterCopy == null) {
				// Es gibt keinen Datensatz, der alle Werte enthält. Also muss dieser erzeugt werden

				// Datensatz anfordern
				final Data firstData = AttributeBaseValueDataFactory.createAdapter(attributeGroup, AttributeHelper.getAttributesValues(attributeGroup));
				// Default und undefiniert Werte setzen
				firstData.setToDefault();
				// ein nicht änderbare Kopie erzeugen und in der Map speichern
				masterCopy = firstData.createUnmodifiableCopy();
				// Datensatz speichern, dieser dient nun immer als Kopiervorlage
				_defaultData.put(attributeGroup, masterCopy);
			}
		} // synchronized

		// Die Kopiervorlage steht nun zur Verfügung. Eine modifizierbare Kopie anlegen und zurückgeben
		return masterCopy.createModifiableCopy();
	}

	@Override
	public void sendApplicationReadyMessage() {
		// die endgültige Fertigmeldung soll verschickt werden.
		sendApplicationReadyMessage(false);
	}

	/**
	 * Meldet sich als Quelle beim Datenverteiler an und verschickt eine (Noch-nicht-)Fertigmeldung.
	 *
	 * @param sendNotReadyYet gibt an, ob eine Noch-nicht-Fertigmeldung (<code>true</code>) oder eine Fertigmeldung (<code>false</code>)
	 *                        verschickt werden soll.
	 */
	private void sendApplicationReadyMessage(boolean sendNotReadyYet) {
		synchronized(_lock) {

			// Datenmodell erfragen
			final DataModel configuration = getDataModel();

			// Attributgruppe auslesen
			AttributeGroup attribute = configuration.getAttributeGroup("atg.applikationsFertigmeldung");

			// atg liegt vor, versende (noch-nicht-)Fertigmeldung
			if(attribute != null) {

				ResultData sendReadyData = composeApplicationReadyMessage(configuration, attribute, sendNotReadyYet);

				// Falls die Nachricht null ist, kann sie nicht verschickt werden
				if(sendReadyData != null) {
					try {
						if(_readyMessageSender == null) { // Die Quelle wurde noch nicht initialisiert und auch nicht angemeldet
							_readyMessageSender = new ReadyMessageSender();
							// Quelle anmelden und Datensatz verschicken
							subscribeSource(_readyMessageSender, sendReadyData);
						}
						else { // Die Quelle ist bereits angemeldet
							// Datensatz versenden
							sendData(sendReadyData);
						}
					}
					catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
						_debug.warning("Die Fertigmeldung konnte nicht verschickt werden", sendSubscriptionNotConfirmed);
					}
					catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
						_debug.warning("Die Fertigmeldung konnte nicht verschickt werden", oneSubscriptionPerSendData);
					}
				}
			}

			// Bei alter Konfiguration liegt diese atg nicht vor - Versenden der Fertigmeldung nicht möglich
			else {
				_debug.warning("Die verwendete Konfiguration unterstützt nicht die Fertigmeldung für Start/Stop.");
			}
		}
	}

	/**
	 * Erstellt die zu versendende Fertigmeldung
	 *
	 * @param configuration   Verwendetes Datenmodell
	 * @param attributeGroup  Attributgruppe für die Fertigmeldung
	 * @param sendNotReadyYet <code>true</code>, falls Noch-Nicht-Fertigmeldung versendet werden soll, <code>false</code> sonst.
	 *
	 * @return Datensatz, der verschickt werden soll.
	 */
	private ResultData composeApplicationReadyMessage(DataModel configuration, AttributeGroup attributeGroup, boolean sendNotReadyYet) {
		// Ermittele die ID des aktuellen Applikationsobjektes
		long applicationID = getLocalApplicationObjectId();

		if(_dataModel.getObject(applicationID) instanceof ClientApplication) {
			// Ermittele das Applikationsobjekt, für das die Fertigmeldung versendet werden soll
			SystemObject object = _dataModel.getObject(applicationID);

			// Zusammenstellen der zu versendenden Daten
			DataDescription dataDescription;
			long time = System.currentTimeMillis();
			Aspect aspect = configuration.getAspect("asp.standard");

			dataDescription = new DataDescription(attributeGroup, aspect, (short)0);

			Data data = createData(attributeGroup);

			// Inkarnationsname setzen
			data.getTextValue("Inkarnationsname").setText(getClientDavParameters().getIncarnationName());

			if(sendNotReadyYet) {
				// Noch-nicht-Fertigmeldung wird verschickt
				data.getTextValue("InitialisierungFertig").setText("Nein");
			}
			else {
				// Fertigmeldung wird verschickt
				data.getTextValue("InitialisierungFertig").setText("Ja");
			}

			return new ResultData(object, dataDescription, time, data);
		}
		return null;
	}

	@Override
	public void enableExplicitApplicationReadyMessage() {
		_applicationSendsDoneMessage = true;
	}

	/**
	 * Meldet einen Transaktionssender oder eine Transaktionsquelle an.
	 * @param sender Sender-Interface
	 * @param dataDescription Daten-Identifikation
	 * @param subscriptions Innere Anmeldungen (für Quelle, falls null wird ein Sender angemeldet)
	 * @throws OneSubscriptionPerSendData Fehler bei der Anmeldung (z.B. es gibt schon eine Quelle)
	 */
	void triggerTransactionSender(
			final ClientSenderInterface sender, final TransactionDataDescription dataDescription, final Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData {
		final SenderRole role;
		if(subscriptions != null) {
			if(_clientDavRequester == null) {
				_clientDavRequester = new ClientDavRequester(this);
			}
			_clientDavRequester.triggerSubscribe(true, dataDescription, subscriptions);
			role = SenderRole.source();
			final SystemObject[] objects = {dataDescription.getObject()};

			synchronized(_lock) {
				if(_subscriptionManager == null) {
					throw new InitialisationNotCompleteException("Die Datenverteiler-Applikationsfunktionen sind noch nicht initialisiert..");
				}
				final Aspect aspectToSubstitute = aspectToSubstitute(dataDescription.getAttributeGroup(), dataDescription.getAspect());
				checkDataIdentification(objects, dataDescription.getAttributeGroup(), aspectToSubstitute, "Sendeanmeldung");
				DataDescription description = dataDescription.getDataDescription();
				if(!aspectToSubstitute.equals(dataDescription.getAspect())) {
					description = dataDescription.getDataDescription().getRedirectedDescription(aspectToSubstitute);
				}
				_subscriptionManager.subscribeSender(sender, objects, description, role);
			}
		}
		else {
			role = SenderRole.sender();
			subscribeSender(sender, dataDescription.getObject(), dataDescription.getDataDescription(), role);
		}

	}

	/**
	 * Meldet einen Transaktionsempfänger oder eine Transaktionssenke an.
	 * @param receiver Empfänger-Interface
	 * @param dataDescription Daten-Identifikation
	 * @param subscriptions Innere Anmeldungen (für Senke, falls null wird ein Empfänger angemeldet)
	 * @throws OneSubscriptionPerSendData Fehler bei der Anmeldung (z.B. es gibt schon eine Senke)
	 */
	void triggerTransactionReceiver(
			final ClientReceiverInterface receiver, final TransactionDataDescription dataDescription, final Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData {
		final ReceiverRole role;
		if(subscriptions != null) {
			if(_clientDavRequester == null) {
				_clientDavRequester = new ClientDavRequester(this);
			}
			_clientDavRequester.triggerSubscribe(false, dataDescription, subscriptions);
			role = ReceiverRole.drain();
		}
		else {
			role = ReceiverRole.receiver();
		}
		subscribeReceiver(receiver, dataDescription.getObject(), dataDescription.getDataDescription(), ReceiveOptions.normal(), role);
	}

	/**
	 * Gibt den Zeitpunkt einer Sender-Anmeldung zurück
	 * @param info Anmeldung
	 * @return Sekunden seit 1970
	 */
	public int getTimeStampFromSenderSubscription(BaseSubscriptionInfo info) {
		return _subscriptionManager.getTimeStampFromSenderSubscription(info);
	}

	/**
	 * Gibt Informationen über eine Datenanmeldung zurück
	 *
	 * @param davApplication Datenverteiler, der gefragt werden soll
	 * @param object Systemobjekt
	 * @param usage Attributgruppenverwendung
	 * @param simulationVariant Simulationsvariante
	 * @return Klasse mit Informationen über die angemeldeten Applikationen auf dieses Datum
	 */
	public ClientSubscriptionInfo getSubscriptionInfo(
			final DavApplication davApplication, final SystemObject object, final AttributeGroupUsage usage, final short simulationVariant) throws IOException {
		if(_clientDavRequester == null) {
			_clientDavRequester = new ClientDavRequester(this);
		}
		return _clientDavRequester.getSubscriptionInfo(davApplication, object, usage, simulationVariant);
	}

	/**
	 * Gibt Informationen über die Datenanmeldungen einer Applikation zurück
	 *
	 * @param davApplication Datenverteiler, der gefragt werden soll
	 * @param application    Applikation von der vorhandene Anmeldungen abgefragt werden sollen
	 *
	 * @return Klasse mit Informationen über die angemeldeten Applikationen auf dieses Datum
	 */
	public ApplicationSubscriptionInfo getSubscriptionInfo(
			final DavApplication davApplication, final ClientApplication application) throws IOException {
		if(_clientDavRequester == null) {
			_clientDavRequester = new ClientDavRequester(this);
		}
		return _clientDavRequester.getSubscriptionInfo(davApplication, application);
	}

	// Eine Hilfsklasse für die implizite Anmeldung
	class ImplicitSubscriptionNote {

		long unsubscriptionTime;

		BaseSubscriptionInfo baseSubscriptionInfo;

		SystemObject objects[];

		DataDescription dataDescription;
	}

	class ImplicitUnsubscriber extends Thread {

		ImplicitUnsubscriber() {
			super("ImplicitUnsubscriber");
			//XXX setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			while(!interrupted()) {
				try {
					if((_implicitSubscriptions != null) && (_implicitSubscriptions.size() > 0)) {
						Collection<? extends ImplicitSubscriptionNote> collection = _implicitSubscriptions.values();
						if(collection != null) {
							Object subscriptions[] = collection.toArray();
							if(subscriptions != null) {
								for(int i = 0; i < subscriptions.length; ++i) {
									ImplicitSubscriptionNote implicitSubscriptionNote = (ImplicitSubscriptionNote)subscriptions[i];
									if(implicitSubscriptionNote != null) {
										synchronized(implicitSubscriptionNote) {
											if(implicitSubscriptionNote.unsubscriptionTime < System.currentTimeMillis()) {
												_subscriptionManager.unsubscribeReceiver(
														_implicitReceiver, implicitSubscriptionNote.objects, implicitSubscriptionNote.dataDescription/*, null*/
												);
												_implicitSubscriptions.remove(implicitSubscriptionNote.baseSubscriptionInfo);
											}
										}
									}
								}
							}
						}
					}
					sleep(5000);
				}
				catch(InterruptedException e) {
					return;
				}
			}
		}
	}

	private class ImplicitReceiver implements ClientReceiverInterface, NonQueueingReceiver {

		@Override
		public final void update(ResultData[] results) {
			synchronized(_implicitSubscriptionDataReceived) {
				_implicitSubscriptionDataReceived.notifyAll();
			}
		}
	}

	@Override
	public ClientDavParameters getClientDavParameters() {
		return _clientDavParameters;
	}

	@Override
	public void addConnectionListener(DavConnectionListener davConnectionListener) {
		_connectionListeners.add(davConnectionListener);
	}

	@Override
	public void removeConnectionListener(DavConnectionListener davConnectionListener) {
		if(!_connectionListeners.remove(davConnectionListener)) {
			_debug.warning("ClientDavConnection.removeConnectionListener(): Zu entfernender Listener wurde nicht gefunden");
			Thread.dumpStack();
		}
	}

	private void notifyConnectionClosed() {
		if(_dataModelConnection != null){
			// hierdurch werden laufende Operationen in der 2. Verbindung unterbrochen, die die folgenden Aktionen blockieren können.
			// Deswegen am Anfang der Methode ausführen.
			_dataModelConnection.disconnect(false, "");
		}
		List<DavConnectionListener> connectionListenersCopy = new ArrayList<DavConnectionListener>(_connectionListeners);
		for(DavConnectionListener davConnectionListener : connectionListenersCopy) {
			try {
				davConnectionListener.connectionClosed(this);
			}
			catch(Exception e) {
				_debug.warning("Fehler beim Verarbeiten der connectionClosed-Meldung", e);
			}
		}
		synchronized(_lock) {
			if(_implicitUnsubscriber != null) {
				_implicitUnsubscriber.interrupt();
				_implicitUnsubscriber = null;
			}
			_isConnected = false;
			_isLoggedIn = false;
			if(_dataModel instanceof DafDataModel && _dataModelConnection == null) {
				((DafDataModel) _dataModel).close();
			}
			if(_subscriptionManager != null) {
				_subscriptionManager.close();
			}
			if(_cacheManager != null) {
				_cacheManager.close();
			}
		}
		DataFactory.forget(getDataModel());
	}

	// Eine Hilfsklasse für das Versenden der Fertigmeldung

	private class ReadyMessageSender implements ClientSenderInterface {

		@Override
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {

		}

		@Override
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}

	@Override
	public boolean checkLoggedUserNameAndPassword(String userName, String password) {
		if(_isLoggedIn) {
			final ClientConnectionProperties clientConnectionProperties = _highLevelCommunication.getConnectionProperties();
			if(userName.equals(clientConnectionProperties.getUserName()) && password.equals(clientConnectionProperties.getUserPassword())) return true;
		}
		try {
			Thread.sleep(3000);
		}
		catch(InterruptedException e) {
			throw new UnsupportedOperationException("nicht implementiert");
		}
		return false;
	}

	@Override
	public Transactions getTransactions() {
		if(_transactionManager == null){
			_transactionManager = new TransactionManager(this);
		}
		return _transactionManager;
	}

	public boolean isConnected() {
		return _isConnected;
	}

	public boolean isLoggedIn() {
		return _isLoggedIn;
	}

	private static class ConnectionShutdownHook extends Thread {

		// Verbindung über WeakReference referenzierten (um Memory Leak zu verhindern, wenn eine Verbindung nicht mehr benutzt wird)
		private final WeakReference<ClientDavConnection> _conn;

		public ConnectionShutdownHook(ClientDavConnection conn) {
			super("Shutdown Hook");
			_conn = new WeakReference<ClientDavConnection>(conn);
		}

		@Override
		public void run() {
			ClientDavConnection connection = _conn.get();
			if(connection != null) {
				if(connection.isConnected() && connection._dataModelConnection == null) {
					DataModel dataModel = connection.getDataModel();
					if(dataModel instanceof DafDataModel) {
						DafDataModel dafDataModel = (DafDataModel) dataModel;
						dafDataModel.close();
					}
				}
			}
		}
	}
}
