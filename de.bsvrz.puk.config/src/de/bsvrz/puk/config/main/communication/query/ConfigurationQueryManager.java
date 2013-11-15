/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.ConfigurationTaskException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.MutableSetChangeListener;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeSpecificationType;
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultEntry;
import de.bsvrz.dav.daf.main.config.management.consistenycheck.ConsistencyCheckResultInterface;
import de.bsvrz.dav.daf.main.impl.config.request.KindOfUpdateTelegramm;
import de.bsvrz.dav.daf.main.impl.config.request.KindOfVersion;
import de.bsvrz.dav.daf.main.impl.config.request.telegramManager.SenderReceiverCommunication;
import de.bsvrz.puk.config.configFile.datamodel.ConfigConfigurationArea;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDynamicObject;
import de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet;
import de.bsvrz.puk.config.configFile.datamodel.ConfigNonMutableSet;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileBackupTask;
import de.bsvrz.puk.config.main.authentication.Authentication;
import de.bsvrz.puk.config.main.authentication.ConfigAuthentication;
import de.bsvrz.puk.config.main.simulation.ConfigSimulationObject;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Diese Klasse nimmt Konfigurationsanfragen entgegen und leitet sie entsprechend an die Konfiguration weiter. Muss die Konfiguration eine Antwort auf die
 * Anfrage verschicken, wird dies ebenfalls durch dieses Objekt realisiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9229 $
 */
public class ConfigurationQueryManager {

	private static final Debug _debug = Debug.getLogger();

	private ClientDavInterface _connection;

	private final DataModel _localConfiguration;

	private final ConfigurationAuthority _localAuthority;

	private final Authentication _authentication;

	private final Map<SystemObject, ConfigurationQueryManager.QueryHandler> _querySender2queryHandlerMap = new HashMap<SystemObject, QueryHandler>();

	/**
	 * Speichert zu einem SystemObjekt, das eine Simulation darstellt, ein Java Objekt, das eine Simulation aus Sicht der Konfiguration, darstellt.
	 * <p/>
	 * Als Schl�ssel dient eine Simulation aus der dynamischen Menge der Simulationen. Der value ist das entsprechende JavaObjekt, das die Simulation aus Sicht der
	 * Konfiguration darstellt.
	 */
	private final Map<SystemObject, ConfigSimulationObject> _simulationObjects = new HashMap<SystemObject, ConfigSimulationObject>();

	/** Map zum Zugriff auf die Simulationsobjekte �ber die Simulationsvariante */
	private HashMap<Short, ConfigSimulationObject> _simulations = new HashMap<Short, ConfigSimulationObject>();

	/** Enth�lt alle Anmeldungen der Konfiguration als Senke auf lesende Konfigurationsanfragen unter Ber�cksichtigung der Simulationsvariante. */
	private final Map<Short, ReceiverSubscriptionSimulation> _simulationReadSubscriptions = Collections.synchronizedMap(new HashMap<Short, ReceiverSubscriptionSimulation>());

	/** Enth�lt alle Anmeldungen der Konfiguration als Senke auf schreibende Konfigurationsanfragen unter Ber�cksichtigung der Simulationsvariante. */
	private final Map<Short, ReceiverSubscriptionSimulation> _simulationWriteSubscriptions = Collections.synchronizedMap(new HashMap<Short, ReceiverSubscriptionSimulation>());

	/** Auftr�ge f�r die Konfiguration, schreibend */
	private DataDescription _dataDescriptionWrite;

	/** Auftr�ge f�r die Konfiguration, lesen */
	private DataDescription _dataDescriptionRead;

	/** Auftr�ge f�r die Konfiguration, Benutzerverwaltung */
	private DataDescription _dataDescriptionUser;

	/** Auftr�ge f�r die Konfiguration, Konfigurationsbereiche */
	private DataDescription _dataDescriptionArea;

	/** Listener, der �nderungen auf der Menge der Simulationen �berwacht */
	private SimulationListener _simulationListener;

	private ForeignObjectManager _foreignObjectManager;

	/**
	 * Hilfsfunktion, die zu den �bergebenen PIDs von einer Attributgruppe und eines Aspekts eine DataDescription erstellt.
	 *
	 * @param configuration Datenmodell mit dessen Hilfe die Objekte zu den PIDs ermittelt werden
	 * @param atgPid        Pid der gew�nschten Attributgruppe
	 * @param aspPid        Pid des gew�nschten Aspekts
	 *
	 * @return DataDescription-Objekt, dass mit der gew�nschten Attributgruppe und dem gew�nschten Aspekt initialisiert ist.
	 *
	 * @throws IllegalStateException mit entsprechender Fehlermeldung, falls eine der beiden PIDs nicht aufgel�st werden konnte.
	 */
	private static DataDescription getDataDescription(DataModel configuration, String atgPid, String aspPid) {
		final AttributeGroup atg = configuration.getAttributeGroup(atgPid);
		if(atg == null) {
			throw new IllegalStateException("Attributgruppe " + atgPid + " wurde nicht gefunden oder ist nicht aktiviert");
		}
		final Aspect asp = configuration.getAspect(aspPid);
		if(asp == null) {
			throw new IllegalStateException("Aspekt " + aspPid + " wurde nicht gefunden oder ist nicht aktiviert");
		}
		return new DataDescription(atg, asp);
	}

	public ForeignObjectManager getForeignObjectManager() {
		return _foreignObjectManager;
	}

	public ConfigurationQueryManager(
			ClientDavInterface connection,
			DataModel localConfiguration,
			ConfigurationAuthority localAuthority,
			Authentication authentication,
			final File foreignObjectCacheFile) {
		_connection = connection;
		_localConfiguration = localConfiguration;

		// Die alte ConfigApp setzt diesen Wert. Die neue ConfigurationApp kann den Wert aus dem Datenmodell beziehen.
		if(localAuthority != null) {
			// Aufruf alte ConfigApp
			_localAuthority = localAuthority;
		}
		else {
			// Aufruf neue ConfigurationApp
			_localAuthority = _localConfiguration.getConfigurationAuthority();
		}
		_foreignObjectManager = new ForeignObjectManager(connection, localConfiguration, _localAuthority, foreignObjectCacheFile);

		// 4 Kan�le, auf die sich die Konfiguration als Senke anmeldet
		_dataDescriptionWrite = getDataDescription(localConfiguration, "atg.konfigurationsAnfrageSchnittstelleSchreibend", "asp.anfrage");
		_dataDescriptionRead = getDataDescription(localConfiguration, "atg.konfigurationsAnfrageSchnittstelleLesend", "asp.anfrage");
		_dataDescriptionUser = getDataDescription(localConfiguration, "atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle", "asp.anfrage");
		_dataDescriptionArea = getDataDescription(localConfiguration, "atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle", "asp.anfrage");

		_authentication = authentication;

		// 1) Alle Simulationen anfordern und entsprechende Simulationsobjekte erzeugen
		final MutableSet mutableSet = _connection.getLocalConfigurationAuthority().getMutableSet("Simulationen");
		final List<SystemObject> simulations = mutableSet.getElements();

		for(SystemObject simulationSystemObject : simulations) {
			synchronized(_simulationObjects) {
				if(simulationSystemObject.isValid() && !_simulationObjects.containsKey(simulationSystemObject) &&
				   (simulationSystemObject.isOfType("typ.onlineSimulation") || simulationSystemObject.isOfType("typ.offlineSimulation"))) {
					final ConfigSimulationObject newSimulationObject;
					try {
						newSimulationObject = createNewSimulationObject(simulationSystemObject);
					}
					catch(RuntimeException e) {
						_debug.warning("Fehler beim Zugriff auf die Simulation " + simulationSystemObject + " (Objekt wird ignoriert)", e);
						continue;
					}
					catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
						// Eine Doppelanmeldung wird verhindert, da es f�r jede Simulation nur einen Sender pro Simulationsvariante
						// geben kann.
						oneSubscriptionPerSendData.printStackTrace();
						_debug.error("Fehler beim Anmelden einer Simulation", oneSubscriptionPerSendData);
						throw new IllegalStateException(oneSubscriptionPerSendData);
					}
					_simulationObjects.put(simulationSystemObject, newSimulationObject);
					_simulations.put(new Short(newSimulationObject.getSimulationVariant()), newSimulationObject);
				}
			}
		}

		// 2) Auf �nderungen dieser Menge anmelden, damit bei neuen Simulationen ein neues Objekt angelegt werden kann. Wird eine
		// Simulation aus der Menge entfernt, so muss auch das Objekt benachrichtigt und entfernt werden
		_simulationListener = new SimulationListener();
		mutableSet.addChangeListener(_simulationListener);
	}

	public void start() {
		try {
			// Alle Anmeldungen finden auf die Simulationsvariante statt, die durch das System vorgegeben werden.
			// Dies sind die Standardkan�le, auf denen die Konfiguration Anfragen entgegen nimmt.
			// F�r Simulationen werden extra Kan�le, mittels einer entsprechenden Methode, angemeldet.

			// Senke f�r lesende Konfigurationsanfragen
			_connection.subscribeReceiver(new QueryReceiver(), _localAuthority, _dataDescriptionRead, ReceiveOptions.normal(), ReceiverRole.drain());
			// Senke f�r schreibende Konfigurationsanfragen
			_connection.subscribeReceiver(new QueryReceiver(), _localAuthority, _dataDescriptionWrite, ReceiveOptions.normal(), ReceiverRole.drain());
			// Senke f�r Benutzerverwaltungsanfragen
			_connection.subscribeReceiver(new QueryReceiver(), _localAuthority, _dataDescriptionUser, ReceiveOptions.normal(), ReceiverRole.drain());
			// Senke f�r Konfigurationsbereichsoperationen
			_connection.subscribeReceiver(new QueryReceiver(), _localAuthority, _dataDescriptionArea, ReceiveOptions.normal(), ReceiverRole.drain());
			_foreignObjectManager.start();
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void detachHandler(final SystemObject querySender) {
//		System.out.println("QueryHandler abgemeldet f�r " + querySender);
		synchronized(_querySender2queryHandlerMap) {
			_querySender2queryHandlerMap.remove(querySender);
		}
	}

	/**
	 * Diese Methode meldet f�r eine Simulationsvariante einen Empf�nger f�r lesende Konfigurationsanfragen an, falls dies nicht bereits geschehen ist.
	 *
	 * @param simulationVariant Simulationsvariante, mit der sich Konfiguration auf Anfragen anmeldet
	 */
	public void subscribeReadRequestForSimulation(final short simulationVariant, ConfigSimulationObject simulationObject) {
		if(!_simulationReadSubscriptions.containsKey(simulationVariant)) {
			// Senke f�r lesende Konfigurationsanfragen, es wird auf eine bestimmte Simulationsvariante angemeldet
			final DataDescription simulationDataDescriptionRead = new DataDescription(
					_dataDescriptionRead.getAttributeGroup(), _dataDescriptionRead.getAspect(), simulationVariant
			);
			final QueryReceiver receiver = new QueryReceiver(simulationObject);
			_connection.subscribeReceiver(receiver, _localAuthority, simulationDataDescriptionRead, ReceiveOptions.normal(), ReceiverRole.drain());

			final ReceiverSubscriptionSimulation receiverSubscriptionSimulation = new ReceiverSubscriptionSimulation(simulationDataDescriptionRead, receiver);
			// Verhindert Doppelanmeldungen und erm�glicht sp�ter die Abmeldung
			_simulationReadSubscriptions.put(simulationVariant, receiverSubscriptionSimulation);
		}
	}

	/**
	 * Diese Methode meldet f�r eine Simulationsvariante einen Empf�nger f�r Konfigurationsschreibanfragenanfragen an, falls dies nicht bereits geschehen ist.
	 *
	 * @param simulationVariant Simulationsvariante, mit der sich Konfiguration auf Anfragen anmeldet
	 */
	public void subscribeWriteRequestForSimulation(final short simulationVariant, ConfigSimulationObject simulationObject) {
		if(!_simulationWriteSubscriptions.containsKey(simulationVariant)) {
			// Senke f�r schreibende Konfigurationsanfragen, es wird auf eine bestimmte Simulationsvariante angemeldet
			final DataDescription simulationDataDescriptionWrite = new DataDescription(
					_dataDescriptionWrite.getAttributeGroup(), _dataDescriptionWrite.getAspect(), simulationVariant
			);
			final QueryReceiver receiver = new QueryReceiver(simulationObject);
			_connection.subscribeReceiver(receiver, _localAuthority, simulationDataDescriptionWrite, ReceiveOptions.normal(), ReceiverRole.drain());

			final ReceiverSubscriptionSimulation receiverSubscriptionSimulation = new ReceiverSubscriptionSimulation(simulationDataDescriptionWrite, receiver);
			_simulationWriteSubscriptions.put(simulationVariant, receiverSubscriptionSimulation);
		}
	}

	/**
	 * Meldet f�r eine Simulationsvariante die Anmeldung als Empf�nger f�r Konfigurationsschreibanfragen ab. Es ist dann unm�glich f�r diese Simulation weiter
	 * Anfragen zu stellen, die schreibend auf die Konfiguration zugreifen wollen. Die Methode kann mehrfach aufgerufen werden, de Verbindung wird nur dann
	 * abgebaut, wenn sie auch existiert.
	 *
	 * @param simulationVariant Simulationsvariante, f�r die es nicht mehr m�glich sein soll Schreibanfragen zu stellen.
	 */
	public void unsubscribeWriteRequestForSimulation(short simulationVariant) {
		final ReceiverSubscriptionSimulation receiverSubscriptionSimulation = _simulationWriteSubscriptions.remove(simulationVariant);
		if(receiverSubscriptionSimulation != null) {
			_connection.unsubscribeReceiver(
					receiverSubscriptionSimulation.getClientReceiverInterface(), _localAuthority, receiverSubscriptionSimulation.getDataDescription()
			);
		}
	}

	/**
	 * Meldet f�r eine Simulationsvariante die Anmeldung als Empf�nger f�r Konfigurationsschreibanfragen ab. Es ist dann unm�glich f�r diese Simulation weiter
	 * Anfragen zu stellen, die schreibend auf die Konfiguration zugreifen wollen. Die Methode kann mehrfach aufgerufen werden, de Verbindung wird nur dann
	 * abgebaut, wenn sie auch existiert.
	 *
	 * @param simulationVariant Simulationsvariante, f�r die es nicht mehr m�glich sein soll Schreibanfragen zu stellen.
	 */
	public void unsubscribeReadRequestForSimulation(short simulationVariant) {
		final ReceiverSubscriptionSimulation receiverSubscriptionSimulation = _simulationReadSubscriptions.remove(simulationVariant);
		if(receiverSubscriptionSimulation != null) {
			_connection.unsubscribeReceiver(
					receiverSubscriptionSimulation.getClientReceiverInterface(), _localAuthority, receiverSubscriptionSimulation.getDataDescription()
			);
		}
	}

	private final static class ReceiverSubscriptionSimulation {

		private final DataDescription _dataDescription;

		private final ClientReceiverInterface _clientReceiverInterface;

		public ReceiverSubscriptionSimulation(final DataDescription dataDescription, final ClientReceiverInterface clientReceiverInterface) {
			_dataDescription = dataDescription;
			_clientReceiverInterface = clientReceiverInterface;
		}

		public DataDescription getDataDescription() {
			return _dataDescription;
		}

		public ClientReceiverInterface getClientReceiverInterface() {
			return _clientReceiverInterface;
		}
	}

	/**
	 * Erzeugt ein Simulationsobjekt {@link de.bsvrz.puk.config.main.simulation.ConfigSimulationObject} aus einem SystemObjekt.
	 * <p/>
	 * Dabei wird der Typ(online/offline), die Simulationsvariante und die speziell zu behandelnden Mengen der Simulation ausgelesen.
	 *
	 * @param systemObject Objekt, das vom Typ typ.simulation sein muss (oder diese Eigenschaften erbt).
	 */
	private ConfigSimulationObject createNewSimulationObject(SystemObject systemObject) throws OneSubscriptionPerSendData {
		// Das Objekt wird hier angelegt da das ConfigurationQueryManagerObjekt ben�tigt wird und die Methode aus "innerClasses" aufgerufen wird.
		return new ConfigSimulationObject(_connection, systemObject, this);
	}

	/**
	 * Listener, der aufgerufen wird, sobald sich die dynamische Menge mit Simulationen �ndert. Wird eine Simulation entfernt, wird das jeweilige Objekt
	 * benachrichtigt und dann aus der Map entfernt. Wird eine Simulation hinzugef�gt, so wird ein entsprechendes Objekt der Map hinzugef�gt.
	 */
	private final class SimulationListener implements MutableSetChangeListener {

		/** Attributgruppe atg.simulationsEigenschaften */
		private final AttributeGroup _atgSimulationProperties;

		public SimulationListener() {
			final DataModel dataModel = _connection.getDataModel();
			_atgSimulationProperties = dataModel.getAttributeGroup("atg.simulationsEigenschaften");
		}

		public void update(MutableSet set, SystemObject[] addedObjects, SystemObject[] removedObjects) {

			if(addedObjects != null) {
				for(SystemObject systemObject : addedObjects) {
					synchronized(_simulationObjects) {
						if(systemObject.isValid() && !_simulationObjects.containsKey(systemObject) &&
						   (systemObject.isOfType("typ.onlineSimulation") || systemObject.isOfType("typ.offlineSimulation"))) {
							// Das Objekt ist nicht vorhanden, das ist der Normalfall
							final ConfigSimulationObject configSimulationObject;
							try {
								configSimulationObject = createNewSimulationObject(systemObject);
							}
							catch(RuntimeException e) {
								_debug.warning("Fehler beim Zugriff auf die Simulation " + systemObject + " (Objekt wird ignoriert)", e);
								continue;
							}
							catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
								// Eine Doppelanmeldung wird verhindert, da es f�r jede Simulation nur einen Sender pro Simulationsvariante
								// geben kann.
								oneSubscriptionPerSendData.printStackTrace();
								throw new IllegalStateException(oneSubscriptionPerSendData);
							}
							_simulationObjects.put(systemObject, configSimulationObject);
							_simulations.put(new Short(configSimulationObject.getSimulationVariant()), configSimulationObject);
						}
					}
				}// neue Objekte
			}

			if(removedObjects != null) {
				for(SystemObject systemObject : removedObjects) {
					final ConfigSimulationObject configSimulationObject;
					synchronized(_simulationObjects) {
						configSimulationObject = _simulationObjects.remove(systemObject);
						_simulations.remove(configSimulationObject.getSimulationVariant());
					}
					if(configSimulationObject != null) {
						// Das Objekt benachrichtigen, dass es aus der Menge entfernt wurde
						configSimulationObject.simulationRemovedFromSet();
					}
				}
			}
		}
	}

	private class QueryReceiver implements ClientReceiverInterface {

		private final ConfigSimulationObject _configSimulationObject;

		/** Dieses Objekt empf�ngt Konfigurationanfragen f�r die Simulationsvariante 0. */
		public QueryReceiver() {
			_configSimulationObject = null;
		}

		/**
		 * Dieses Objekt empf�ngt Konfigurationsanfragen f�r eine bestimmte Simulation. Das Objekt, das die Simulation aus Sicht der Konfiguration darstellt wird
		 * �bergeben.
		 *
		 * @param configSimulationObject Objekt, das aus Sicht der Konfiguration eine Simulation darstellt.
		 */
		public QueryReceiver(ConfigSimulationObject configSimulationObject) {
			_configSimulationObject = configSimulationObject;
		}


		public void update(ResultData results[]) {
			for(int i = 0; i < results.length; i++) {
				ResultData result = results[i];
				try {
					Data data = result.getData();
					if(data == null || !result.hasData()) {
						_debug.fine("leerer Datensatz erhalten", data);
					}
					else {
						SystemObject querySender = data.getReferenceValue("absender").getSystemObject();
						_debug.finer("Konfigurationsanfrage erhalten von", querySender);
						QueryHandler handler;
						synchronized(_querySender2queryHandlerMap) {
							handler = (QueryHandler)_querySender2queryHandlerMap.get(querySender);
							if(handler == null) {
								handler = new QueryHandler(querySender, result.getDataDescription().getSimulationVariant(), _configSimulationObject);
//								System.out.println("neuen QueryHandler angelegt f�r " + querySender);
								_querySender2queryHandlerMap.put(querySender, handler);
							}
						}
//						System.out.println("QueryHandler f�r " + querySender + " bearbeitet Anfrage: " + result);
						handler.handleQuery(result);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					_debug.warning("Anfrage konnte nicht interpretiert werden", e);
				}
			}
		}
	}


	private class QueryHandler implements Runnable {

		private final SystemObject _querySender;

		/** <code>true</code>, wenn der Kommunikationspartner eine normale Applikation ist; <code>false</code>, wenn der Kommunikationspartner eine andere Konfiguration ist */
		private boolean _isRequestFromApplication;

		Thread _worker;

		UnboundedQueue<ResultData> _queries = new UnboundedQueue<ResultData>();

		Object _lock = new Object();

		/** Lockt die Zugriffe auf den Publisher. */
		private Object _lockPublisher = new Object();

		/** Objekt, welches bei dynamischen Mengen genutzt wird, um die �nderungen an diesen Mengen zu publizieren. */
		private MutableSetChangePublisher _publisher;

		/** Merkt sich alle dynamischen Mengen, bei denen ein Beobachter (MutableSetChangePublisher) angemeldet wurde. */
		private Set<MutableSet> _publisherSets;

		/** Enth�lt alle dynamischen Objekte, zu denen es einen <code>_invalidationListenerPublisher</code> gibt. */
		private Set<DynamicObject> _monitoredDynamicObject = Collections.synchronizedSet(new HashSet<DynamicObject>());

		/** Objekt, das den Versand von Konfigurationsanfragen(die Antwort darauf) �bernimmt. */
		private final SenderReceiverCommunication _senderReplyReadTasks;

		/** Objekt, das den Versand von Konfigurationsanfragen(die Antwort darauf) �bernimmt. */
		private final SenderReceiverCommunication _senderReplyWriteTasks;

		/** Objekt, das den Versand von Konfigurationsanfragen(die Antwort darauf) �bernimmt. */
		private final SenderReceiverCommunication _senderReplyAreaTasks;

		/** Objekt, das den Versand von Konfigurationsanfragen(die Antwort darauf) �bernimmt. */
		private final SenderReceiverCommunication _senderReplyUserAdministrationTask;

		/** Datenidentifikation f�r lesende Konfigurationsanfragen. Die Simulationsvariante wird beachtet. */
		private final DataDescription _dataDescriptionReadLocal;

		/** Datenidentifikation f�r schreibende Konfigurationsanfragen. Die Simulationsvariante wird beachtet. */
		private final DataDescription _dataDescriptionWriteLocal;

		/**
		 * Datenidentifikation f�r Konfigurationsanfragen, die Konfigurationsbereiche steuern. Die Identifikation wird auch bei Simulationen erzeugt, um eine
		 * Null-Pointer Exception zu verhindern. Allerdings wird weder ein Sender noch ein Empf�nger f�r diese Anfragen angemeldet.
		 */
		private final DataDescription _dataDescriptionAreaLocal;

		/**
		 * Datenidentifikation f�r Konfigurationsanfragen, die Benutzer beeinflussen. Die Identifikation wird auch bei Simulationen erzeugt, um eine Null-Pointer
		 * Exception zu verhindern. Allerdings wird weder ein Sender noch ein Empf�nger f�r diese Anfragen angemeldet.
		 */
		private final DataDescription _dataDescriptionUserLocal;

		/** Wird f�r Anmdeldung als Sender/Empf�nger ben�tigt */
		private final short _simulationVariant;

		/** Objekt, das aus Sicht der Konfiguration eine Simulation darstellt. Ist die <code>_simulationVariant</code> > 0, so muss dieses Objekt vorhanden sein. */
		private ConfigSimulationObject _simulationObject = null;

		/** Enth�lt f�r jeden dynamischen Typ einen Listener. Die Listener werden gespeichert, um sie sp�ter wieder abmelden zu k�nnen. */
		private final Map<DynamicObjectType, InvalidationListener> _invalidationListenerForAllTyps = new HashMap<DynamicObjectType, InvalidationListener>();

		/** Enth�lt f�r jeden dynamischen Typ einen Listener. Die Listener werden gespeichert, um sie sp�ter wieder abmelden zu k�nnen. */
		private final Map<DynamicObjectType, DynamicObjectType.NameChangeListener> _nameChangedListener = new HashMap<DynamicObjectType, DynamicObjectType.NameChangeListener>();

		/** Enth�lt f�r jeden dynamischen Typ einen Listener. Die Listener werden gespeichert, um sie sp�ter wieder abmelden zu k�nnen. */
		private final Map<DynamicObjectType, DynamicObjectType.DynamicObjectCreatedListener> _objectCreatedListener = new HashMap<DynamicObjectType, DynamicObjectType.DynamicObjectCreatedListener>();

		/** Map, die die angemeldeten Listener f�r �nderungen der Elemente von dynamischen Zusammenstellungen speichert, f�r Key und Value wird das selbe
		 * Listener-Objekt verwendet. Allerdings ist die interne Simulationsvariante im Listenerobjekt nicht signifikant f�r die HashMap.
 		 */
		private Map<PublishingMutableCollectionChangeListener, PublishingMutableCollectionChangeListener> _mutableCollectionChangeHandlers = new HashMap<PublishingMutableCollectionChangeListener, PublishingMutableCollectionChangeListener>();

		private HashMap<PublishingCommunicationStateListener, PublishingCommunicationStateListener> _communicationChangedHandlers = new HashMap<PublishingCommunicationStateListener, PublishingCommunicationStateListener>();

		/**
		 * Bearbeitet Konfigurationsanfragen und reicht diese an das Datenmodell weiter und verschickt anschlie�end die Antwort.
		 *
		 * @param querySender       Applikation, die die Anfragen gestellt hat
		 * @param simulationVariant Simulationsvariante, mit der die Antworten angemeldet werden. Ist die Simulationsvariante > 0, so muss auch das Objekt
		 *                          <code>simulationObject</code> �bergeben werden.
		 * @param simulationObject  Objekt, �ber das zus�tzliche Informationen �ber eine Simulation abgefragt werden k�nnen. Dieses Objekt kann auch <code>null</code>
		 *                          sein, falls der Parameter <code>simulationVariant</code> kleiner/gleich 0 ist.
		 */
		public QueryHandler(SystemObject querySender, short simulationVariant, ConfigSimulationObject simulationObject) {
			_querySender = querySender;
			_isRequestFromApplication = _querySender instanceof ClientApplication;
			_debug.fine("QueryHandler f�r " + _querySender + ", isRequestFromApplication: " + _isRequestFromApplication);
			_simulationVariant = simulationVariant;
			_worker = new Thread(this);
			_worker.start();
			if(simulationVariant > 0 && simulationObject == null) throw new IllegalStateException("F�r eine Simulation wurde kein Simulationsobjekt angegeben");
			_simulationObject = simulationObject;

			// Die Anfragen m�ssen identifiziert werden (�ber ihre Datenidentifikation), aus diesem Grund wird an dieser Stelle
			// die erwarteten/unterst�tzten Identifikationen erzeugt.
			if(simulationVariant > 0) {
				// Es handelt sich um eine Simulation, also m�ssen neue Datenidentifikationen erzeut werden
				_dataDescriptionReadLocal = new DataDescription(_dataDescriptionRead.getAttributeGroup(), _dataDescriptionRead.getAspect(), simulationVariant);
				_dataDescriptionWriteLocal = new DataDescription(
						_dataDescriptionWrite.getAttributeGroup(), _dataDescriptionWrite.getAspect(), simulationVariant
				);
				_dataDescriptionAreaLocal = new DataDescription(_dataDescriptionArea.getAttributeGroup(), _dataDescriptionArea.getAspect(), simulationVariant);
				_dataDescriptionUserLocal = new DataDescription(_dataDescriptionUser.getAttributeGroup(), _dataDescriptionUser.getAspect(), simulationVariant);
			}
			else {
				// Es handelt sich um keine Simulation, es k�nnen die normalen Datenidentifikationen benutzt werden
				_dataDescriptionReadLocal = _dataDescriptionRead;
				_dataDescriptionWriteLocal = _dataDescriptionWrite;
				_dataDescriptionAreaLocal = _dataDescriptionArea;
				_dataDescriptionUserLocal = _dataDescriptionUser;
			}

			try {
				// 4 Kan�le, die Antworten der Konfiguration zur�ckschicken

				// Auf welches Objekt soll sich angemeldet werden (_querySender)
				// Wer verschickt diese Nachricht (_localAuthority)
				_senderReplyWriteTasks = new ConfigurationAnswerWriteTasks(_connection, _querySender, _localAuthority, simulationVariant);

				// Auf welches Objekt soll sich angemeldet werden (_querySender)
				// Wer verschickt diese Nachricht (_localAuthority)
				_senderReplyReadTasks = new ConfigurationAnswerReadTasks(_connection, _querySender, _localAuthority, simulationVariant);

				// Simulationen d�rfen weder Benutzer �ndern noch Konfigurationsbereiche steuern. Aus diesem Grund werden keine Sender f�r
				// diesen Fall angemeldet.
				if(simulationVariant > 0) {
					// Auf die nicht initialisierten Objekte wird bei Simulationen nicht Zugegriffen, da die Simulation
					// �ber die Datenidentifikation identifiziert werden kann.
					_senderReplyUserAdministrationTask = null;
					_senderReplyAreaTasks = null;
				}
				else {
					// Auf welches Objekt soll sich angemeldet werden (_querySender)
					// Wer verschickt diese Nachricht (_localAuthority)
					_senderReplyUserAdministrationTask = new ConfigurationAnswerUserAdministrationTasks(_connection, _querySender, _localAuthority);

					// Auf welches Objekt soll sich angemeldet werden (_querySender)
					// Wer verschickt diese Nachricht (_localAuthority)
					_senderReplyAreaTasks = new ConfigurationAnswerAreaTasks(_connection, _querySender, _localAuthority);
				}

				// Die Konfiguration muss alle �nderungen an Objekten propagieren. Daf�r werden alle Listener bei allen Typen angemeldet.

				// Alle dynamische Typen anfordern
				final List<SystemObject> allDynamicTypes = _localConfiguration.getType("typ.dynamischerTyp").getElements();

				for(SystemObject objectType : allDynamicTypes) {
					final DynamicObjectType dynamicObjectType = (DynamicObjectType)objectType;

					// Listener f�r "Objekte werden ung�ltig" anmelden
					// �nderungen werden �ber die "atg.konfigurationsAnfrageSchnittstelleLesend" propagiert
					final InvalidationListenerForTyps invalidationListenerForTyps = new InvalidationListenerForTyps(_senderReplyReadTasks, dynamicObjectType, _isRequestFromApplication);
					dynamicObjectType.addInvalidationListener(invalidationListenerForTyps);
					// Damit die Listener wieder entfernt werden k�nnen
					_invalidationListenerForAllTyps.put(dynamicObjectType, invalidationListenerForTyps);

					// Listener f�r "Der Name �ndert sich" anmelden
					final NameChangedListenerForTyps nameChangedListenerForTyps = new NameChangedListenerForTyps(_senderReplyReadTasks, dynamicObjectType,
					                                                                                             _isRequestFromApplication
					);
					dynamicObjectType.addNameChangeListener(nameChangedListenerForTyps);
					_nameChangedListener.put(dynamicObjectType, nameChangedListenerForTyps);

					// Listener f�r "Neues Objekt" anmelden
					final ObjectCreatedListenerForTyps objectCreatedListenerForTyps = new ObjectCreatedListenerForTyps(
							_senderReplyReadTasks, dynamicObjectType
					);
					dynamicObjectType.addObjectCreationListener(objectCreatedListenerForTyps);
					_objectCreatedListener.put(dynamicObjectType, objectCreatedListenerForTyps);
				}
			}
			catch(ConfigurationException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
				oneSubscriptionPerSendData.printStackTrace();
				throw new RuntimeException(oneSubscriptionPerSendData);
			}
		}

		/** Meldet alle Listener ab, die auf �nderungen bei dynamischen Typen angemeldet waren. */
		private void detachListener() {

			synchronized(_communicationChangedHandlers) {
				for(PublishingCommunicationStateListener listener : _communicationChangedHandlers.keySet()) {
					listener.getForeignConfigRequester().removeCommunicationStateListener(listener);
				}
			}

			synchronized(_mutableCollectionChangeHandlers) {
				for(PublishingMutableCollectionChangeListener listener : _mutableCollectionChangeHandlers.keySet()) {
					final ForeignMutableCollectionProxy collectionProxy = listener.getForeignMutableCollectionProxy();
					if(collectionProxy == null) {
						listener.getMutableCollection().removeChangeListener(listener.getInternalSimVariant(), listener);
					}
					else {
						collectionProxy.removeChangeListener(listener);
					}
				}
			}

			final List<SystemObject> allDynamicTypes = _localConfiguration.getType("typ.dynamischerTyp").getElements();

			for(SystemObject objectType : allDynamicTypes) {
				final DynamicObjectType dynamicObjectType = (DynamicObjectType)objectType;

				final InvalidationListener invalidationListenerForTyps = _invalidationListenerForAllTyps.get(dynamicObjectType);
				assert invalidationListenerForTyps != null : dynamicObjectType;
				dynamicObjectType.removeInvalidationListener(invalidationListenerForTyps);
				// Damit die Listener wieder entfernt werden k�nnen

				// Listener f�r "Der Name �ndert sich" abmelden
				final DynamicObjectType.NameChangeListener nameChangedListenerForTyps = _nameChangedListener.get(dynamicObjectType);
				assert nameChangedListenerForTyps != null : dynamicObjectType;
				dynamicObjectType.removeNameChangeListener(nameChangedListenerForTyps);

				// Listener f�r "Neues Objekt" abmelden
				final DynamicObjectType.DynamicObjectCreatedListener objectCreatedListenerForTyps = _objectCreatedListener.get(dynamicObjectType);
				assert objectCreatedListenerForTyps != null : dynamicObjectType;
				dynamicObjectType.removeObjectCreationListener(objectCreatedListenerForTyps);
			}
		}

		private void detachSelf() {
			try {
				// meldet den Beobachter bei den dynamischen Mengen wieder ab, die zuvor beobachtet wurden
				synchronized(_lockPublisher) {
					if(_publisherSets != null) {
						for(MutableSet mutableSet : _publisherSets) {
							mutableSet.removeChangeListener(_publisher);
						}
					}
				}
			}
			catch(ConfigurationException e) {
				e.printStackTrace();
				_debug.warning("Fehler beim Abmelden", e);
			}

			_senderReplyWriteTasks.close();
			_senderReplyReadTasks.close();
			// Bei Simulationen werden diese Kan�le nicht angemeldet
			if(_simulationVariant <= 0) {
				_senderReplyUserAdministrationTask.close();
				_senderReplyAreaTasks.close();
			}
			detachListener();
			detachHandler(_querySender);
		}

		private void handleQuery(ResultData resultData) {
			_queries.put(resultData);
		}

		public void run() {
			Thread.currentThread().setName("QueryHandler(Konfiguration) fuer " + _querySender);
			while(true) {
				try {
					ResultData resultData = null;
//					do {
					while(resultData == null) {
						// Es wird alle 60 Sekunden gepr�ft, ob die Verbindung noch vorhanden ist
						resultData = _queries.poll(60000);
						synchronized(_lock) {
							// Es werden nur noch die neuen Anfrageschnittstellen unterst�tzt -> Die vier Sendesteuerungen m�ssen vorhanden sein.
							// Nur wenn beide F�lle negativ sind, kann der Thread, der sich um die Anfragen k�mmert beendet werden
							// (Es gab einmal eine positive Sendesteuerung und jetzt nicht mehr).
							if(((_senderReplyAreaTasks != null
							     && _senderReplyAreaTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.Connected
							     && _senderReplyAreaTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.NotYetConnected) && (
									_senderReplyReadTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.Connected
									&& _senderReplyReadTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.NotYetConnected) && (
									_senderReplyUserAdministrationTask != null
									&& _senderReplyUserAdministrationTask.getConnectionState() != SenderReceiverCommunication.ConnectionState.Connected
									&& _senderReplyUserAdministrationTask.getConnectionState() != SenderReceiverCommunication.ConnectionState.NotYetConnected)
							                                                                                                                      && (
									_senderReplyWriteTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.Connected
									&& _senderReplyWriteTasks.getConnectionState() != SenderReceiverCommunication.ConnectionState.NotYetConnected))) {

								// Alles abmelden
								detachSelf();

								_debug.fine(
										"QueryHandlerWorker ist fertig und beendet sich. Anfragende Applikation, die keine Antworten mehr erh�lt: "
										+ _querySender
								);
								return;
							}
						}
					}
//					while(resultData == null);

					final Data query = resultData.getData();

					


					int queryIndex = query.getScaledValue("anfrageIndex").intValue();
					String queryType = query.getScaledValue("nachrichtenTyp").getValueText();
					final byte[] queryMessage = query.getUnscaledArray("daten").getByteArray();
					_debug.finer("queryIndex = " + queryIndex);
					_debug.finer("queryType = " + queryType);
					//_debug.fine("queryMessage = " + queryMessage);
					Deserializer deserializer = SerializingFactory.createDeserializer(2, new ByteArrayInputStream(queryMessage));

					_debug.fine("Konfiguration empf�ngt Auftrag: " + resultData.getDataDescription() + " AnfrageTyp: " + queryType);

					// Enth�lt die serialisierte Nachricht(byte-Array), die verschickt werden muss
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					if(resultData.getDataDescription().equals(_dataDescriptionReadLocal)) {
						// Lesende Anfrage
						// Wenn ein Beobachter angemeldet wird, muss keine Antwort verschickt werden

						// Speichert welche Antwort verschickt wird (Spezialfall Beobachter, dort wird keine Nachricht verschickt).
						// Die Variable kann mit "" initialisiert werden, da immer (auch Exception) ein String gesetzt wird.
						String messageType = "";

						boolean sendData = true;
						try {
							if(queryType.equals("ObjektAnfrageMitId")) {
								long id = deserializer.readLong();
								SystemObject object = _localConfiguration.getObject(id);
								writeSystemObject(serializer, object, id, "");
								messageType = "ObjektAntwort";
							}
							else if(queryType.equals("ObjektAnfrageMitPid")) {
								String pid = deserializer.readString();
								SystemObject object = _localConfiguration.getObject(pid);
								writeSystemObject(serializer, object, 0, pid);
								messageType = "ObjektAntwort";
							}
							else if(queryType.equals("DynamischeMengeAlleElementeAnfrage")) {
								final MutableSet set = (MutableSet)deserializer.readObjectReference(_localConfiguration);

								// Zu beachten:  Das Simulationobjekt ist bei Simulationsvariante <= 0 <code>null</code>, bei Simulationen ist das Objekt vorhanden

								// Bei der Anfrage m�ssen 3 F�lle unterschieden werden:
								// 1) Keine Simulation -> Anfrage ganz normal stellen
								// 2) Simulation, aber der Typ der Menge befindet sich nicht in den speziell zu behandelnden Mengen -> Anfrage normal stellen
								// 3) Simulation und der Typ der Menge muss speziell behandelt werden -> Anfrage an die Menge, aber unter Ber�cksichtigung der Simulationsvariante

								// true = Die Elemente einer Menge m�ssen unter Ber�cksichtigung der Simulationsvariante angefordert werden; false = Die Elemente
								// k�nnen ganz normal angefordert werden

								final boolean requestWithSimulationVariant;
								if(_simulationVariant <= 0) {
									// 1)
									requestWithSimulationVariant = false;
								}
								else if(_simulationObject.isSpecialTreatedSetType((ObjectSetType)set.getType())) {
									// Es handelt sich um eine Simulation und der Typ der Menge ist an der Simulationsstrecke als "speziell zu behandelnde Menge"
									// aufgelistet.
									// 3)
									requestWithSimulationVariant = true;
								}
								else {
									// 2)
									requestWithSimulationVariant = false;
								}

								long startTime = deserializer.readLong();
								long endTime = deserializer.readLong();
								boolean validDuringEntirePeriod = deserializer.readBoolean();
								// Fallunterscheidung, welches getElements aufgerufen werden muss
								List resultList = new LinkedList();
								if(startTime == Long.MAX_VALUE && endTime == Long.MAX_VALUE) {
									// Muss bei den Elementen die Simulationsvariante ber�cksichtigt werden
									if(requestWithSimulationVariant) {
										resultList = ((ConfigMutableSet)set).getElementsWithSimulationVariant(_simulationVariant);
									}
									else {
										resultList = set.getElements();
									}
								}
								else if(startTime == endTime) {
									// Muss bei den Elementen die Simulationsvariante ber�cksichtigt werden
									if(requestWithSimulationVariant) {
										resultList = ((ConfigMutableSet)set).getElementsWithSimulationVariant(startTime, _simulationVariant);
									}
									else {
										resultList = set.getElements(startTime);
									}
								}
								else if(validDuringEntirePeriod) {
									// Muss bei den Elementen die Simulationsvariante ber�cksichtigt werden
									if(requestWithSimulationVariant) {
										resultList = ((ConfigMutableSet)set).getElementsDuringPeriod(startTime, endTime, _simulationVariant);
									}
									else {
										resultList = set.getElementsDuringPeriod(startTime, endTime);
									}
								}
								else if(!validDuringEntirePeriod) {
									// Muss bei den Elementen die Simulationsvariante ber�cksichtigt werden
									if(requestWithSimulationVariant) {
										resultList = ((ConfigMutableSet)set).getElementsInPeriod(startTime, endTime, _simulationVariant);
									}
									else {
										resultList = set.getElementsInPeriod(startTime, endTime);
									}
								}

								serializer.writeInt(resultList.size());	// speichert die L�nge der Antwort
								for(Iterator iterator = resultList.iterator(); iterator.hasNext();) {
									SystemObject systemObject = (SystemObject)iterator.next();
									serializer.writeObjectReference(systemObject);
								}
								messageType = "DynamischeMengeAlleElementeAntwort";
							}
							else if(queryType.equals("DynamischeMengeBeobachterAnmelden")) {
								MutableSet set = (MutableSet)deserializer.readObjectReference(_localConfiguration);
								long time = deserializer.readLong();	// Zeit auslesen
								if(_publisher == null) _publisher = new MutableSetChangePublisher();

								if(_simulationVariant <= 0) {
									// der Zeitstempel wird momentan nicht ben�tigt, deshalb wird er nicht weitergereicht
									set.addChangeListener(_publisher /*, time*/);
								}
								else {
									// der Zeitstempel wird momentan nicht ben�tigt, deshalb wird er nicht weitergereicht
									((ConfigMutableSet)set).addChangeListener(_publisher, _simulationVariant /*, time*/);
								}
								synchronized(_lockPublisher) {
									if(_publisherSets == null) _publisherSets = new HashSet<MutableSet>();
									_publisherSets.add(set);
								}
								sendData = false;
							}
							else if(queryType.equals("DynamischeMengeBeobachterAbmelden")) {
								MutableSet set = (MutableSet)deserializer.readObjectReference(_localConfiguration);

								set.removeChangeListener(_publisher);
								synchronized(_lockPublisher) {
									if(_publisherSets != null) {
										_publisherSets.remove(set);
									}
								}
								sendData = false;
							}
							else if(queryType.equals("DynamischeKollektionAnmeldung")) {
								final SystemObject systemObject = deserializer.readObjectReference(_localConfiguration);
								final MutableCollection mutableCollection = ((MutableCollection)systemObject);
								short externalSimVariant = deserializer.readShort();
								short internalSimVariant = getInternalSimVariant(externalSimVariant, mutableCollection);
								PublishingMutableCollectionChangeListener handler;

								ForeignMutableCollectionProxy foreignCollectionProxy = _foreignObjectManager.getForeignMutableCollectionProxy(internalSimVariant, mutableCollection);
								handler = new PublishingMutableCollectionChangeListener(_querySender, mutableCollection, externalSimVariant, internalSimVariant, foreignCollectionProxy, queryIndex);
								synchronized(_mutableCollectionChangeHandlers) {
									if(_mutableCollectionChangeHandlers.put(handler, handler) == null) {
										if(foreignCollectionProxy == null) {
											mutableCollection.addChangeListener(internalSimVariant, handler);
										}
										else {
											foreignCollectionProxy.addChangeListener(handler);
											sendData = false;
										}
									}
								}
								if(sendData) {
									final Collection<SystemObject> elements = mutableCollection.getElements(internalSimVariant);
									messageType = "DynamischeKollektionElemente";
									serializer.writeObjectReference(systemObject);
									serializer.writeShort(externalSimVariant);
									serializer.writeInt(elements.size());
									for(SystemObject element : elements) {
										serializer.writeObjectReference(element);
									}
								}
							}
							else if(queryType.equals("DynamischeKollektionAbmeldung")) {
								final SystemObject systemObject = deserializer.readObjectReference(_localConfiguration);
								final MutableCollection mutableCollection = ((MutableCollection)systemObject);
								short externalSimVariant = deserializer.readShort();
								PublishingMutableCollectionChangeListener handler;
								synchronized(_mutableCollectionChangeHandlers) {
									handler = _mutableCollectionChangeHandlers.remove(new PublishingMutableCollectionChangeListener(_querySender, mutableCollection, externalSimVariant, (short)0, null, 0));
									if(handler != null) {
										final ForeignMutableCollectionProxy collectionProxy = handler.getForeignMutableCollectionProxy();
										if(collectionProxy==null) {
											mutableCollection.removeChangeListener(handler.getInternalSimVariant(), handler);
										}
										else {
											collectionProxy.removeChangeListener(handler);
										}
									}
								}
								sendData = false;
							}
							else if(queryType.equals("KommunikationszustandAnmeldung")) {
								long systemObjectId = deserializer.readLong();
								SystemObject object = _localConfiguration.getObject(systemObjectId);
								int communicationState = -2;
								ForeignConfigRequester foreignConfigRequester = null;
								if(object instanceof MutableSet) {
									MutableSet mutableSet = (MutableSet)object;
									final String managementPid = _foreignObjectManager.getElementsManagementPid(mutableSet);
									if(managementPid.equals(_localAuthority.getPid())) {
										communicationState = -1;
									}
									else {
										foreignConfigRequester = _foreignObjectManager.getForeignConfigRequester(mutableSet);
									}
								}
								else if(object==null) {
									object= _foreignObjectManager.getRemoteObject(systemObjectId);
									if(object instanceof ForeignDynamicObject) {
										ForeignDynamicObject foreignDynamicObject = (ForeignDynamicObject)object;
										foreignConfigRequester = foreignDynamicObject.getForeignConfigRequester();
									}
								}
								else {
									communicationState = -1;
								}
								if(foreignConfigRequester != null) {
									final PublishingCommunicationStateListener listener = new PublishingCommunicationStateListener(_querySender, object, foreignConfigRequester);
									synchronized(_communicationChangedHandlers) {
										if(_communicationChangedHandlers.put(listener, listener) == null) {
											foreignConfigRequester.addCommunicationStateListener(listener);
										}
										communicationState = foreignConfigRequester.isCommunicatorConnected() ? 1 : 0;
									}
								}
								messageType = "KommunikationszustandR�ckmeldung";
								serializer.writeObjectReference(object);
								serializer.writeByte(communicationState);
							}
							else if(queryType.equals("KommunikationszustandAbmeldung")) {
								long systemObjectId = deserializer.readLong();
								SystemObject object = _localConfiguration.getObject(systemObjectId);
								if(object==null) {
									object= _foreignObjectManager.getRemoteObject(systemObjectId);
								}
								if(object != null) {
									PublishingCommunicationStateListener listener = new PublishingCommunicationStateListener(_querySender, object, null);
									synchronized(_communicationChangedHandlers) {
										listener = (PublishingCommunicationStateListener)_communicationChangedHandlers.remove(listener);
										if(listener != null) listener.getForeignConfigRequester().removeCommunicationStateListener(listener);
									}
								}
								sendData = false;
							}
							else if(queryType.equals("DatensatzAnfrage")) {
								long attributeGroupUsageId = deserializer.readLong();
								AttributeGroupUsage attributeGroupUsage = (AttributeGroupUsage)_localConfiguration.getObject(attributeGroupUsageId);
								_debug.finer("attributeGroupUsage.getPidOrId()", attributeGroupUsage.getPidOrId());
								int numberOfObjects = deserializer.readInt();
								serializer.writeInt(numberOfObjects);
								final ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
								Serializer dataSerializer = SerializingFactory.createSerializer(serializer.getVersion(), dataOutputStream);
								for(int i = 0; i < numberOfObjects; ++i) {
									long systemObjectId = deserializer.readLong();
									SystemObject object = _localConfiguration.getObject(systemObjectId);
									if(object==null) {
										object= _foreignObjectManager.getRemoteObject(systemObjectId);
										if(object==null) {
											object= _foreignObjectManager.getCachedForeignObject(systemObjectId);
										}
									}
//									final ConfigSystemObject configSystemObject = ((ConfigSystemObject)object);
//									// Version des Serialisierers, mit dem der nachfolgende Datensatz serialisiert wurde, als Byte versenden
//									// serializer.writeByte(configSystemObject.getSerializerVersion());
//									byte[] dataBytes = configSystemObject.getConfigurationDataBytes(attributeGroupUsage);
//									if(dataBytes == null || dataBytes.length == 0) {
//										serializer.writeInt(0);
//									}
//									else {
//										serializer.writeInt(dataBytes.length);
//										serializer.writeBytes(dataBytes);
//									}


									Data configData = (object == null ? null : object.getConfigurationData(attributeGroupUsage));
									if(configData == null) {
										serializer.writeInt(0);
									}
									else {
										// Aus dem Data-Objekt wird mit einem eigenen Serializer ein ByteArray erzeugt,
										// damit die L�nge bestimmt werden kann.
										// Stream wird bei jedem Schleifendurchlauf wiederverwendet und deshalb mit reset() initialisiert
										dataOutputStream.reset();
										dataSerializer.writeData(configData);
										byte[] dataBytes = dataOutputStream.toByteArray();
										serializer.writeInt(dataBytes.length);
										serializer.writeBytes(dataBytes);
									}
								}
								messageType = "DatensatzAntwort";
							}
							else if(queryType.equals("ObjekteAnfragenMitPidUndZeitbereich")) {
								// Pid, Startzeitpunkt und Endzeitpunkt aulesen
								final String pid = deserializer.readString();
								final long startTime = deserializer.readLong();
								final long endTime = deserializer.readLong();

								final Collection<SystemObject> result = _localConfiguration.getObjects(pid, startTime, endTime);

								// Antwort serialisieren

								// Aufbau Antwort:
								// Anzahl Referenzen, int
								//      Referenzen auf SystemObjekte

								serializer.writeInt(result.size());
								for(SystemObject systemObject : result) {
									serializer.writeObjectReference(systemObject);
								}
								messageType = "AntwortObjekteAnfragenMitPidUndZeitbereich";
							}
							else if(queryType.equals("AktiveVersionKonfigurationsbereich")) {
								// Das �bergebene Objekt ist ein Konfigurationsbereich
								final ConfigurationArea configurationArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);

								// Aktive Version anfragen und den short-Wert verschicken
								final short activeVersion = _localConfiguration.getActiveVersion(configurationArea);
								serializer.writeShort(activeVersion);

								messageType = "AntwortAktiveVersionKonfigurationsbereich";
							}
							else if(queryType.equals("VersionInArbeitKonfigurationsbereich")) {
								// Das �bergebene Objekt ist ein Konfigurationsbereich
								final ConfigurationArea configurationArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);

								// modifizierbare Version anfragen und den short-Wert verschicken
								final short modifiableVersion = configurationArea.getModifiableVersion();
								serializer.writeShort(modifiableVersion);

								messageType = "AntwortVersionInArbeitKonfigurationsbereich";
							}
							else if(queryType.equals("ObjekteMitBereichUndTypAnfragen")) {
								// Konfigurationsbereiche einlesen. Die Zahl -99 wird als <code>null</code> interpretiert und dient als Wildcard.
								final int numberOfConfigurationAreas = deserializer.readInt();
								// Bereiche, die gepr�ft werden sollen
								final Collection<ConfigurationArea> configurationAreas;

								if(numberOfConfigurationAreas >= 0) {
									configurationAreas = new ArrayList<ConfigurationArea>(numberOfConfigurationAreas);
									for(int nr = 0; nr < numberOfConfigurationAreas; nr++) {
										configurationAreas.add((ConfigurationArea)deserializer.readObjectReference(_localConfiguration));
									}
								}
								else {
									// Wildcard f�r Bereiche
									configurationAreas = null;
								}

								// Typen einlesen. Die Zahl -99 wird als <code>null</code> interpretiert und dient als Wildcard.
								final int numberOfTypes = deserializer.readInt();
								// Typen, die gepr�ft werden sollen
								final Collection<SystemObjectType> systemObjectTypes;

								if(numberOfTypes >= 0) {
									systemObjectTypes = new ArrayList<SystemObjectType>(numberOfTypes);

									for(int nr = 0; nr < numberOfTypes; nr++) {
										systemObjectTypes.add((SystemObjectType)deserializer.readObjectReference(_localConfiguration));
									}
								}
								else {
									// Wildcard f�r Typen
									systemObjectTypes = null;
								}

								// Zeitspezifikation einlesen
								final ObjectTimeSpecification objectTimeSpecification = deserializeObjectTimeSpecification(deserializer);

								final Collection<SystemObject> result = _localConfiguration.getObjects(
										configurationAreas, systemObjectTypes, objectTimeSpecification
								);

								// Die Antwort wird wie folgt kodiert:
								// Anzahl Referenzen, int
								//      Referenzen

								serializer.writeInt(result.size());
								for(SystemObject systemObject : result) {
									serializer.writeObjectReference(systemObject);
								}

								messageType = "AntwortObjekteMitBereichUndTypAnfragen";
							}
							else if(queryType.equals("ObjekteDirekterTyp")) {
								// Einen bestimmten Bereich nach Objekte bestimmter Typen anfragen

								// Bereich, in dem die Daten gesucht werden sollen
								final ConfigurationArea configurationArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);

								final int numberOfTypes = deserializer.readInt();
								final Collection<SystemObjectType> systemObjectTypes = new ArrayList<SystemObjectType>(numberOfTypes);

								for(int nr = 0; nr < numberOfTypes; nr++) {
									systemObjectTypes.add((SystemObjectType)deserializer.readObjectReference(_localConfiguration));
								}

								// Zeit auslesen
								final ObjectTimeSpecification objectTimeSpecification = deserializeObjectTimeSpecification(deserializer);

								// Anfrage stellen und Antwort verschicken
								final Collection<SystemObject> result = configurationArea.getDirectObjects(systemObjectTypes, objectTimeSpecification);

								serializer.writeInt(result.size());

								for(SystemObject systemObject : result) {
									serializer.writeObjectReference(systemObject);
								}
								messageType = "AntwortObjekteDirekterTyp";
							}
							else if(queryType.equals("NeueObjekteEinesBereichsAnfordern")) {

								// Es sollen alle neuen Objekte eines Bereichs angefragt werden

								final ConfigurationArea area = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);

								// Das Antworttelegramm besitzt folgenden Aufbau:
								// 1) Anzahl Elemente, int (Ist kein Objekt vorhanden, so wird 0 eingetragen)
								//      2) Objektreferenzen auf die neuen Objekte

								final Collection<SystemObject> newObjects = area.getNewObjects();

								serializer.writeInt(newObjects.size());

								for(SystemObject newObject : newObjects) {
									serializer.writeObjectReference(newObject);
								}

								messageType = "AntwortNeueObjekteEinesBereichsAnfordern";
							}
							else if(queryType.equals("ElementeEinerMengeZeit")) {
								final ObjectSet set = (ObjectSet)deserializer.readObjectReference(_localConfiguration);

								// Zeit, in der die Objekte g�ltig sein sollen
								final ObjectTimeSpecification objectTimeSpecification = deserializeObjectTimeSpecification(deserializer);

								final List<SystemObject> result;
								if(objectTimeSpecification.getType() == TimeSpecificationType.VALID_AT_TIME) {
									result = set.getElements(objectTimeSpecification.getTime());
								}
								else if(objectTimeSpecification.getType() == TimeSpecificationType.VALID_DURING_PERIOD) {
									result = set.getElementsDuringPeriod(objectTimeSpecification.getStartTime(), objectTimeSpecification.getEndTime());
								}
								else if(objectTimeSpecification.getType() == TimeSpecificationType.VALID_IN_PERIOD) {
									result = set.getElementsInPeriod(objectTimeSpecification.getStartTime(), objectTimeSpecification.getEndTime());
								}
								else {
									throw new IllegalStateException("Anfrage unbekannten Typs: " + objectTimeSpecification.getType());
								}

								messageType = "AntwortElementeEinerMengeZeit";

								serializeObjectList(serializer, result);
							}
							else if(queryType.equals("ElementeEinerMengeVersion")) {

								// Menge, von der die Elemente angefordert werden sollen
								final ConfigNonMutableSet set = (ConfigNonMutableSet)deserializer.readObjectReference(_localConfiguration);

								final KindOfVersion kindOfVersion = KindOfVersion.getInstance(deserializer.readByte());

								List<SystemObject> result;

								if(kindOfVersion == KindOfVersion.IN_ALL_VERSIONS || kindOfVersion == KindOfVersion.IN_ANY_VERSIONS) {
									// die Versionen sind in beiden F�llen gespeichert
									final short fromVersion = deserializer.readShort();
									final short toVersion = deserializer.readShort();

									if(kindOfVersion == KindOfVersion.IN_ALL_VERSIONS) {

										result = set.getElementsInAllVersions(fromVersion, toVersion);
									}
									else {
										result = set.getElementsInAnyVersions(fromVersion, toVersion);
									}
								}
								else if(kindOfVersion == KindOfVersion.IN_VERSION) {
									final short version = deserializer.readShort();
									result = set.getElementsInVersion(version);
								}
								else if(kindOfVersion == KindOfVersion.IN_NEXT_VERSION) {
									// F�r diesen Fall muss nichts weiter ausgelesen werden
									result = set.getElementsInModifiableVersion();
								}
								else {
									throw new IllegalStateException("Unbekannte Versionsanfrage: " + kindOfVersion);
								}
								serializeObjectList(serializer, result);
								messageType = "AntwortElementeEinerMengeVersion";
							}
							else {
								final String errorMessage = "Unbekannter nachrichtenTyp: " + queryType;
								serializer.writeString(errorMessage);
								messageType = "FehlerAntwort";
								_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", errorMessage);
							}
						}
						catch(Exception e) {
							// Es ist zu einem Fehler gekommen
							e.printStackTrace();
							final String errorMessage = "Fehler beim Erzeugen der Antwort: " + e;
							serializer.writeString(errorMessage);
							messageType = "FehlerAntwort";
							_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", e);
						}

						if(sendData) {
							assert !"".equals(messageType) : "Unbekannter Messagetype";
							_debug.finer(
									"Die Antwort auf die Anfrage " + queryType + " mit Index " + queryIndex + " wird verschickt. Empfangsobjekt: "
									+ _querySender.getNameOrPidOrId() + " KonfigurationsAnfrageLesend"
							);
							_senderReplyReadTasks.sendData(messageType, byteArrayStream.toByteArray(), queryIndex);
//							System.out.println("Read verschickt antwort auf " + _replyDataDescriptionRead);
//							// Antwort auf die Anfrage verschicken
//							sendReply(reply, byteArrayStream.toByteArray(), _replyDataDescriptionRead);
						}
					}
					else if(resultData.getDataDescription().equals(_dataDescriptionWriteLocal)) {

						// Bestimmt den Typ der Antwort
						String messageType = "";
						// Schreibende Anfrage
						try {
							if(queryType.equals("DynamischeMengeElemente�ndern") || queryType.equals("KonfigurierendeMengeElemente�ndern")) {

								final ObjectSet set;

								// Soll eine dynamische oder eine konfigurierende Menge ge�ndert werden. Da auf dem Interface gearbeitet wird,
								// ist nur die Objektzuweisung wichtig.
								// Es wird auch sofort der richtige Antworttyp des Telegramms festgelegt.
								if(queryType.equals("DynamischeMengeElemente�ndern")) {
									set = (MutableSet)deserializer.readObjectReference(_localConfiguration);
									// Im Fehlerfall wird der Typ erneut gesetzt
									messageType = "DynamischeMengeElementeAntwort";

									// Bei dynamischen Mengen muss die Simulation ber�cksichtigt werden. Es gibt drei F�lle:
									// 1) Es handelt sich um keine Simulation (Simvariante <= 0) -> Elemente der Menge �ndern
									// 2) Es handelt sich um eine Simulation (Simvariante > 0) und der Typ der zu �ndernden Menge wurde an der Simulationsstrecke
									// angegeben -> Elemente der Menge �ndern
									// 3) Es handelt sich um eine Simulation und der Typ der zu �ndernden Menge wurde nicht an der Simulationsstrecke angegeben
									// -> Fehler, die Menge darf nicht ge�ndert werden

									// Anmerkung: Das _simulationObject darf <code>null</code> sein, wenn die Simulationsvariante <= 0 ist.
									if(_simulationVariant <= 0) {
										// Es handelt sich um keine Simulation
										handleChangeElementsRequest(deserializer, serializer, set, true, false);
									}
									else if(_simulationObject.isSpecialTreatedSetType((ObjectSetType)set.getType())) {
										// Es ist eine Simulation und die Menge darf ge�ndert werden
										handleChangeElementsRequest(deserializer, serializer, set, true, true);
									}
									else {
										// Es ist eine Simulation, aber der Typ der Menge ist nicht an der Simulationsstrecke vermerkt. Also
										// darf die Simulation nicht �ndernd auf die dynamische Menge zugreifen.
										// Da eine Exception geworfen wird, wird der messageType entsprechend ge�ndert.
										throw new ConfigurationChangeException(
												"Die Menge " + set.getPid() + " darf durch die Simulation " + _simulationObject.getSimulationObject().getPid()
												+ " mit Simulationsvariante " + _simulationVariant + " nicht ge�ndert werden."
										);
									}
								}
								else {
									set = (NonMutableSet)deserializer.readObjectReference(_localConfiguration);
									messageType = "KonfigurierendeMengeElementeAntwort";
									// Bei konfigurierenden Mengen spielt die Simulationsvariante keine Rolle.
									// Aufruf der Methode:  considerSimulationVariant = false kann auch weggelassen werden.
									handleChangeElementsRequest(deserializer, serializer, set, false, false);
								}
							}
							else if(queryType.equals("KonfigurierendenDatensatzFestlegen")) {

								// Das �bergebene Byte-Array ist wie folgt aufgebaut:

								// Id der Attributgruppenverwendung, long
								// Id des Systemobjekts, long
								// L�nge des folgenden byte-Arrays, int
								// Datensatz als byte-Array. Ein byte-Array der L�nge 0 wird als <code>null</code> interpretiert

								// ATG-Verwendung
								final AttributeGroupUsage attributeGroupUsage = (AttributeGroupUsage)_localConfiguration.getObject(deserializer.readLong());
								// SystemObject an dem der Datensatz hinzugef�gt werden soll
								final SystemObject object = _localConfiguration.getObject(deserializer.readLong());

								// L�nge des folgenden byte-Arrays
								final int dataByteSize = deserializer.readInt();
								// Konfigurierender Datensatz, der an das Objekt geh�ngt werden soll
								final Data data;
								if(dataByteSize > 0) {
									data = deserializer.readData(attributeGroupUsage.getAttributeGroup());
								}
								else {
									data = null;
								}
								object.setConfigurationData(attributeGroupUsage, data);
								// Die Gegenseite erkennt, dass alles geklappt hat.
								// Bei "false" wird versucht den Fehlertext auszulesen
								serializer.writeBoolean(true);
								messageType = "KonfigurierendenDatensatzFestlegenAntwort";
							}
							else if(queryType.equals("ObjektAnlegen")) {
								// Es muss entweder ein dynamische Objekte oder ein Konfigurationsobjekt angelegt werden

								// true = Es soll ein Konfiguraitonsobjekt angelegt werden
								final boolean configurationObject = deserializer.readBoolean();
								// Bereich, in dem das neue Objekt angelegt werden soll
								final ConfigurationArea configurationArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);
								// Pid des Objekts
								final String pid = deserializer.readString();
								// Name des Objekts
								final String name = deserializer.readString();

								// Das neu erzeugte Objekt
								final SystemObject newObject;

								if(configurationObject) {
									// Type des Konfigurationsobjekts einlesen
									final ConfigurationObjectType type = (ConfigurationObjectType)deserializer.readObjectReference(_localConfiguration);
									// Mengen, die angef�gt werden sollen, auslesen

									// Anzahl Mengen, die angef�gt werden soll. Die Zahl "-99" stellt den Wert <code>null</code> dar
									final int numberOfSets = deserializer.readInt();

									final List<ObjectSet> sets;

									if(numberOfSets >= 0) {
										sets = new ArrayList<ObjectSet>(numberOfSets);

										for(int nr = 0; nr < numberOfSets; nr++) {
											sets.add((ObjectSet)deserializer.readObjectReference(_localConfiguration));
										}
									}
									else {
										sets = null;
									}

									newObject = configurationArea.createConfigurationObject(type, pid, name, sets);
								}
								else {

									final DynamicObjectType type = (DynamicObjectType)deserializer.readObjectReference(_localConfiguration);

									// Das neue Objekt anlegen, da bei diesem Auftrag keine Datens�tze mit �bertragen werden, wird eine leere Liste �bergeben.
									// An dieser Stelle wird festgelegt, dass die alte Methode benutzt werden soll. Diese pr�ft derzeit nicht, ob alle
									// ben�tigten Datens�tze an dem neuen Objekt vorhanden sind, auch wenn diese vorhanden sein m�ssen.
									newObject = createDynamicObject(
											(ConfigConfigurationArea)configurationArea, type, pid, name, new ArrayList<DataAndATGUsageInformation>(), true
									);
								}
								serializer.writeObjectReference(newObject);
								messageType = "AntwortObjektAnlegen";
							}
							else if(queryType.equals("DynamischesObjektMitKonfigurierendenDatensaetzenAnlegen")) {
								// Dynamisches Objekt anlegen und sofort konfigurierende Datens�tze speichern

								final ConfigurationArea configArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);
								final String pid = deserializer.readString();
								final String name = deserializer.readString();
								final DynamicObjectType type = (DynamicObjectType)deserializer.readObjectReference(_localConfiguration);

								final int numberOfDataSets = deserializer.readInt();

								final List<DataAndATGUsageInformation> dataSets = new ArrayList<DataAndATGUsageInformation>();

								for(int nr = 0; nr < numberOfDataSets; nr++) {
									final AttributeGroupUsage usage = (AttributeGroupUsage)deserializer.readObjectReference(_localConfiguration);
									final Data data = deserializer.readData(usage.getAttributeGroup());
									final DataAndATGUsageInformation dataSet = new DataAndATGUsageInformation(usage, data);
									dataSets.add(dataSet);
								}

								// Es soll die neue create-Methode benutzt werden. Also wird auch gepr�ft, ob alle ben�tigten Datens�tze
								// vorhanden sind.
								final DynamicObject newObject = createDynamicObject(
										(ConfigConfigurationArea)configArea, type, pid, name, dataSets, false
								);

								serializer.writeObjectReference(newObject);
								messageType = "AntwortObjektAnlegen";
							}
							else if(queryType.equals("ObjektKopieren")) {

								// Das �bergebene Byte-Array besitzt folgenden aufbau:
								// 1) Referenz auf das zu kopierende Objekt
								// 2) Gr��e der Map, die angelegt werden muss
								// 3) Je Eintrag in die Map die 2 Werte;
								// 4) Pid, die ersetzt werden soll (String)
								// 5) Pid, die die bestehnde Pid ersetzt (String)

								// Objekt das kopiert werden soll
								final SystemObject systemObject = deserializer.readObjectReference(_localConfiguration);

								final int numberOfEntries = deserializer.readInt();

								final Map<String, String> substitutePids = new HashMap<String, String>(numberOfEntries);

								for(int nr = 0; nr < numberOfEntries; nr++) {
									final String key = deserializer.readString();
									final String value = deserializer.readString();

									substitutePids.put(key, value);
								}

								if(systemObject instanceof ConfigurationObject) {
									final ConfigurationObject configurationObject = (ConfigurationObject)systemObject;
									final SystemObject clonedObject = configurationObject.duplicate(substitutePids);

									serializer.writeObjectReference(clonedObject);
									messageType = "AntwortObjektKopieren";
								}
								else {
									// Wenn es auch m�glich ist dynamische Objekte zu kopieren, dann muss nur noch dieser Fall betrachtet werden, weil
									// die Methode dann an dem SystemObject Interface gefordert wird.
									throw new UnsupportedOperationException(
											"Derzeit k�nnen nur Konfigurationsobjekte kopiert werden. Vom Objekt " + systemObject.getPid()
											+ " kann keine Kopie erstellt werden, da es kein Konfigurationsobjekt ist."
									);
								}
							}
							else if(queryType.equals("ObjektMengenBearbeiten")) {
								// Das Telegramm besitzt folgenden Aufbau
								// Objekt, dessen Mengen ge�ndert werden sollen
								// Menge, die ge�ndert werden soll
								// boolean, true = Menge hinzuf�gen; false = Menge entfernen

								final ConfigurationObject configObject = (ConfigurationObject)deserializer.readObjectReference(_localConfiguration);
								final ObjectSet set = (ObjectSet)deserializer.readObjectReference(_localConfiguration);
								final boolean addSet = deserializer.readBoolean();

								if(addSet) {
									configObject.addSet(set);
								}
								else {
									configObject.removeSet(set);
								}

								// Die Antwort signalisiert, dass die �nderungen vorgenommen wurden. Kommt es zu einem Fehler, so
								// wird die Exception in einem anderen Telegramm �bertragen.
								messageType = "AntwortObjektMengenBearbeiten";
							}
							else if(queryType.equals("Konfigurations�nderungVerweigert")) {
								final String errorMessage = "Konfigurations�nderung wurde wegen fehlender Rechte nicht ausgef�hrt: " + deserializer.readString();;
								serializer.writeString(errorMessage);
								messageType = "Konfigurations�nderungVerweigert";
							}
							else {
								final String errorMessage = "Unbekannter nachrichtenTyp: " + queryType;
								serializer.writeString(errorMessage);
								messageType = "FehlerAntwort";
								_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", errorMessage);
							}
						}
						catch(ConfigurationChangeException e) {
							// Die Konfiguration weigert sich den Schreibvorgang durchzuf�hren.
							// Ein Grund daf�r k�nnten mangelnde Rechte sein.
							// Es ist zu einem Fehler gekommen
//							e.printStackTrace();
							final String errorMessage = "Fehler beim Versuch Daten der Konfiguration zu �ndern: " + e;
							serializer.writeString(errorMessage);
							messageType = "Konfigurations�nderungVerweigert";
							_debug.warning("Fehler beim Versuch Daten der Konfiguration zu �ndern", e);
						}
						catch(Exception e) {
							// Es ist zu einem Fehler gekommen
							e.printStackTrace();
							final String errorMessage = "Fehler beim Erzeugen der Antwort: " + e;
							serializer.writeString(errorMessage);
							messageType = "FehlerAntwort";
							_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", e);
						}
						// Antwort auf die Anfrage verschicken
						_senderReplyWriteTasks.sendData(messageType, byteArrayStream.toByteArray(), queryIndex);
						_debug.finer(
								"Die Antwort auf die Anfrage " + queryType + " mit Index " + queryIndex + " wird verschickt. Empfangsobjekt: "
								+ _querySender.getNameOrPidOrId() + " KonfigurationsAnfrageSchreibend"
						);
					}
					else if((_dataDescriptionUserLocal.getSimulationVariant() <= 0) && (resultData.getDataDescription().equals(_dataDescriptionUserLocal))) {
						// Benutzerverwaltungsanfrage, diese d�rfen nicht von Simulationen angestossen werden

						// Speichert den Antworttyp der Nachricht
						String messageType = "";

						try {
							if("AuftragBenutzerverwaltung".equals(queryType)) {
								// Es soll (eines der unten aufgef�hrten Aktionen):
								// - ein neuer Benutzer angelegt
								// - ein Einmal-Passwort angelegt
								// - die Rechte eines bestehenden Benutzers ge�ndert
								// - das Passwort eines Benutzers ge�ndert werden
								// - ein Benutzer gel�scht werden
								// - Einmalpassw�rter gel�scht werden odr deren verbleibende Anzahl ermittelt werden
								// - gepr�ft werden, ob ein Benutzer Admin-Rechte hat.
								// Werden neue Aufgaben erg�nzt, sollte der Kommentar erweitert werden

								// Das �bergebene Byte-Array enth�lt:
								// Benutzername des Benutzers, der den Auftrag anst��t (String)
								// Benutztes Verschl�sslungsverfahren (String)
								// L�nge des Byte-Arrays, das den verschl�sselten Auftrag enth�lt (int)
								// Byte-Array, das den verschl�sselten Auftrag enth�lt (byte[])

								final String username = deserializer.readString();
								final String usedEncryptionProcessName = deserializer.readString();
								final int lengthOfData = deserializer.readInt();
								final byte[] encryptedTask = deserializer.readBytes(lengthOfData);

								serializer.writeInt(_authentication.processTask(username, encryptedTask, usedEncryptionProcessName));
								messageType = "AuftragBenutzerverwaltungAntwort";
							}
							else if("AuftragZufallstext".equals(queryType)) {
								// Beauftragt die Benutzerverwaltung einen Zufallstext zu generieren

								// Der Zufallstext wird wie folgt gespeichert:
								// L�nge des Textes (int)
								// Zufallstext (byte[])

								byte[] randomText = _authentication.getText();
								serializer.writeInt(randomText.length);
								serializer.writeBytes(randomText);
								messageType = "AuftragZufallstextAntwort";
							}
							else {
								final String errorMessage = "Unbekannter nachrichtenTyp: " + queryType;
								serializer.writeString(errorMessage);
								messageType = "FehlerAntwort";
								_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", errorMessage);
							}
						}
						catch(ConfigurationTaskException e) {
							// Die Konfiguration lehnt es ab den Auftrag zu bearbeiten
							final String errorMessage = "Die Konfiguration lehnt den Auftrag ab: " + e;
							serializer.writeString(errorMessage);
							messageType = "KonfigurationsauftragVerweigert";
						}
						catch(Exception e) {
							// Es ist zu einem Fehler gekommen
							e.printStackTrace();
							final String errorMessage = "Fehler beim Erzeugen der Antwort: " + e;
							serializer.writeString(errorMessage);
							messageType = "FehlerAntwort";
							_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", e);
						}
						// Antwort auf die Anfrage verschicken
						_senderReplyUserAdministrationTask.sendData(messageType, byteArrayStream.toByteArray(), queryIndex);
						_debug.finer(
								"Die Antwort auf die Anfrage " + queryType + " mit Index " + queryIndex + " wird verschickt. Empfangsobjekt: "
								+ _querySender.getNameOrPidOrId() + " Benutzerverwaltung"
						);
					}
					else if((_dataDescriptionAreaLocal.getSimulationVariant() <= 0) && (resultData.getDataDescription().equals(_dataDescriptionAreaLocal))) {
						// Auftrag f�r Konfigurationsbereiche, diese k�nnen nicht durch Simulationen angestossen werden
						// Speichert den Antworttyp der Nachricht
						String messageType = "";

						try {
							if("BereichePr�fen".equals(queryType)) {
								ConsistencyCheckResultInterface consistencyCheckResultInterface = ((ConfigDataModel)_localConfiguration).checkConsistency(
										createAreaAndVersion(deserializer)
								);
								// Die Antwort zusammenfassen
								transferConsistencyCheckResult(consistencyCheckResultInterface, serializer);
								messageType = "BereichePr�fenAntwort";
							}
							else if("BereicheAktivieren".equals(queryType)) {
								ConsistencyCheckResultInterface consistencyCheckResultInterface = ((ConfigDataModel)_localConfiguration).activateConfigurationAreas(
										createAreaAndVersion(deserializer)
								);
								transferConsistencyCheckResult(consistencyCheckResultInterface, serializer);
								messageType = "BereicheAktivierenAntwort";
							}
							else if("BereicheFreigabeZur�bernahme".equals(queryType)) {
								ConsistencyCheckResultInterface consistencyCheckResultInterface = ((ConfigDataModel)_localConfiguration).releaseConfigurationAreasForTransfer(
										createAreaAndVersion(deserializer)
								);
								transferConsistencyCheckResult(consistencyCheckResultInterface, serializer);
								messageType = "BereicheFreigabeZur�bernahmeAntwort";
							}
							else if("BereicheFreigabeZurAktivierung".equals(queryType)) {
								((ConfigDataModel)_localConfiguration).releaseConfigurationAreasForActivation(createAreaAndVersion(deserializer));
								// Irgendwas zur�ckgeben. Die positive Antwort wird nicht ausgewertet, der Fehlerfall (Fehlemeldung) ist von intresse.
								serializer.writeBoolean(true);
								messageType = "BereicheFreigabeZurAktivierungAntwort";
							}
							else if("AlleBereicheAnfordern".equals(queryType)) {
								// Ein Benutzer fordert alle Konfigurtionsbereiche an, auch die, die nur in den Verwaltungsdateien
								// gespeichert sind und nicht aktiv sind.

								// Der Deserialisierer enth�lt keine Informationen, die ausgelesen werden m�ssen

								// Die Bereiche anfordern. Es werden nur die Id�s gebraucht. �ber diese kann sich
								// der Empf�nger die Map selbst zusammenbauen (Objekt mit Id anfordern, dann steht das Objekt und die Pid zur Verf�gung).
								final Collection<ConfigurationArea> allConfigurationAreas = ((ConfigDataModel)_localConfiguration).getAllConfigurationAreas().values();

								// Aufbau der Daten:
								// Anzahl Id�s (int)
								// Anzahl viele Long-Werte, die jeweils einer Id eines Bereichs entsprechen

								serializer.writeInt(allConfigurationAreas.size());

								for(ConfigurationArea configurationArea : allConfigurationAreas) {
									serializer.writeLong(configurationArea.getId());
								}
								// Antwort Kennzeichnen
								messageType = "AlleBereicheAnfordernAntwort";
							}
							else if("BereichAnlegen".equals(queryType)) {
								// Neuen Bereich anlegen

								// Den Namen auslesen
								final String name = deserializer.readString();
								// Pid
								final String pid = deserializer.readString();
								// Pid des Konfigurationsverantwortlichen
								final String authorityPid = deserializer.readString();

								// Den Bereich anlegen, wird eine Exception geworfen, wird diese gefangen und
								// als Antwort verschickt. Kann der Bereich angelegt werden, wird die Id verschickt.
								final ConfigurationArea newConfigurationArea = ((ConfigDataModel)_localConfiguration).createConfigurationArea(
										name, pid, authorityPid
								);
								// Id des neuen Bereichs. Damit kann der Empf�nger das Objekt anfordern, auch wenn es noch nicht
								// aktiv ist
								serializer.writeLong(newConfigurationArea.getId());
								messageType = "BereichAnlegenAntwort";
							}
							else if("BereicheImportieren".equals(queryType) || "BereicheExportieren".equals(queryType)) {

								// In beiden F�llen muss ein Pfad und eine Anzahl Pids ausgelesen werden

								// Verzeichnis der Versorgungsdateien
								final String pathString = deserializer.readString();
								// Verzeichnis der Versorgungsdateien
								File maintenanceFile = new File(pathString);

								// Wieviele Pids
								final int numberOfPids = deserializer.readInt();
								final Collection<String> configurationAreaPids = new ArrayList<String>(numberOfPids);

								for(int nr = 0; nr < numberOfPids; nr++) {
									configurationAreaPids.add(deserializer.readString());
								}

								// Was soll gemacht werden, import oder export
								if("BereicheImportieren".equals(queryType)) {
									((ConfigDataModel)_localConfiguration).importConfigurationAreas(maintenanceFile, configurationAreaPids);
									messageType = "BereicheImportierenAntwort";
								}
								else {
									((ConfigDataModel)_localConfiguration).exportConfigurationAreas(maintenanceFile, configurationAreaPids);
									messageType = "BereicheExportierenAntwort";
								}

								// Irgendwas zur�ckgeben. Die positive Antwort wird nicht ausgewertet, der Fehlerfall (Fehlermeldung) ist von Interesse.
								serializer.writeBoolean(true);
							}
							else if("BereicheFreigabeZurAktivierungOhneKVAktivierung".equals(queryType)) {
								// Es sollen Bereiche zur Aktivierung freigegeben werden, ohne das diese vorher durch den KV aktiviert wurden

								// Bereiche und Versionen auslesen
								final Collection<ConfigAreaAndVersion> configAreaAndVersions = createAreaAndVersion(deserializer);

								final ConsistencyCheckResultInterface consistencyCheckResult = ((ConfigDataModel)_localConfiguration).releaseConfigurationAreasForActivationWithoutCAActivation(
										configAreaAndVersions
								);

								transferConsistencyCheckResult(consistencyCheckResult, serializer);
								messageType = "AntwortBereicheFreigabeZurAktivierungOhneKVAktivierung";
							}
							else if("BackupKonfigurationsdaten".equals(queryType)) {
								// Sicherungsauftrag der Konfigurationsdateien
								final ConfigFileBackupTask fileBackupTask = new ConfigFileBackupTask(
										(ConfigAuthentication)_authentication,
										(ConfigDataModel)_localConfiguration,
										deserializer.readString(),
										_senderReplyAreaTasks,
										queryIndex
								);
								serializer.writeInt(ConfigFileBackupTask.BACKUP_STATE_INITIALIZING);
								serializer.writeString(fileBackupTask.getTargetPath());
								fileBackupTask.startAsync();
								messageType = "AntwortBackupKonfigurationsdaten";
							}
							else {
								final String errorMessage = "Unbekannter nachrichtenTyp: " + queryType;
								serializer.writeString(errorMessage);
								messageType = "FehlerAntwort";
								_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", errorMessage);
							}
						}
						catch(ConfigurationChangeException e) {
							// Die Konfiguration verweigert es den Auftrag(Konfigurations�nderungen) auszuf�hren, weil gegen Randbediengungen
							// versto�en wurden, die f�r den Auftrag gelten mu�ten.
							// Davon muss die Gegenseite informatiert werden.
							final String reason = "Die Konfiguration kann die Konfigurations�nderung nicht ausf�hren: " + e;
							serializer.writeString(reason);
							messageType = "Konfigurations�nderungVerweigert";
						}
						catch(ConfigurationTaskException e) {
							// Die Konfiguration verweigert es den Auftrag auszuf�hren, weil gegen Randbediengungen
							// versto�en wurden, die f�r den Auftrag gelten mu�ten.
							// Davon muss die Gegenseite informatiert werden.
							final String reason = "Die Konfiguration kann den Auftrag nicht ausf�hren: " + e;
							serializer.writeString(reason);
							messageType = "KonfigurationsauftragVerweigert";
						}
						catch(Exception e) {
							// Es ist zu einem Fehler gekommen
							e.printStackTrace();
							final String errorMessage = "Fehler beim Erzeugen der Antwort: " + e;
							serializer.writeString(errorMessage);
							messageType = "FehlerAntwort";
							_debug.warning("Bearbeitung von einer Konfigurationsanfrage fehlgeschlagen", e);
						}
						// Antwort auf die Anfrage verschicken
						_senderReplyAreaTasks.sendData(messageType, byteArrayStream.toByteArray(), queryIndex);
						_debug.finer(
								"Die Antwort auf die Anfrage " + queryType + " mit Index " + queryIndex + " wird verschickt. Empfangsobjekt: "
								+ _querySender.getNameOrPidOrId() + " Konfigurationsbereichverwaltung"
						);
					}
					else {
						// Unbekannter Anfragetyp. Die Exception wird weiter unten gefangen und nur ausgegeben.

						if(_dataDescriptionReadLocal.getSimulationVariant() <= 0) {
							// Es handelt sich nicht um eine Simulation
							final StringBuffer errorText = new StringBuffer("Unbekannter Konfigurationsanfragetype: " + "\n");
							errorText.append(resultData.getDataDescription() + "\n");
							errorText.append("Unterst�tzte Anfragetypen: " + "\n");
							errorText.append(_dataDescriptionReadLocal + "\n");
							errorText.append(_dataDescriptionWriteLocal + "\n");
							errorText.append(_dataDescriptionUserLocal + "\n");
							errorText.append(_dataDescriptionAreaLocal + "\n");
							_debug.error(errorText.toString());

							throw new IllegalArgumentException(
									"Der Typ der Konfigurationsanfrage ist unbekannt: " + resultData.getDataDescription() + " Art der Anfrage:" + queryType
									+ " Index der Anfrage " + queryIndex + " Empfangsobjekt " + _querySender
							);
						}
						else {
							// Es handelt sich um eine Simulation, entweder ist der Anfragetyp unbekannt oder eine Simulation versucht
							// eine Aktion, die ihr nicht gestattet ist (Benutzer anlegen, Konfigurationsbereiche manipulieren)

							final StringBuffer errorText = new StringBuffer(
									"Unbekannter Konfigurationsanfragetyp oder unerlaubte Aktion einer Simulation: " + "\n"
							);
							errorText.append(resultData.getDataDescription() + "\n");
							errorText.append("Unterst�tzte Anfragetypen: " + "\n");
							errorText.append(_dataDescriptionReadLocal + "\n");
							errorText.append(_dataDescriptionWriteLocal + "\n");
							_debug.error(errorText.toString());

							throw new IllegalArgumentException(
									"Der Typ der Konfigurationsanfrage ist unbekannt oder f�r Simulationen nicht zugelassen: " + resultData.getDataDescription()
									+ " Art der Anfrage:" + queryType + " Index der Anfrage " + queryIndex + " Empfangsobjekt " + _querySender
							);
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					_debug.error("Fehler beim Versenden einer Antwort", e);
				}
			}// while(true)
		}

		/**
		 * Ermittelt die Simulationsvariante f�r interne Anmeldungen auf �nderungen der Elemente von dynamischen Zusammenstellungen.
		 * @param externalSimVariant Von au�en vorgegebene Simulationsvariante.
		 * @param mutableCollection Dynamische Menge oder dynamischer Typ f�r den die interne Simulationsvariante ermittelt werden soll.
		 * @return Simulationsvariante f�r interne Anmeldungen auf �nderungen der Elemente von dynamischen Zusammenstellungen.
		 */
		private short getInternalSimVariant(final short externalSimVariant, final MutableCollection mutableCollection) {
			short internalSimVariant = 0;
			if(externalSimVariant < 0) {
				internalSimVariant = _simulationVariant;
			}
			else if(externalSimVariant > 0) {
				final ConfigSimulationObject configSimulationObject = _simulations.get(new Short(externalSimVariant));
				if(configSimulationObject == null) {
					_debug.warning(
							"Anmeldung auf �nderung der Zusammenstellung " + mutableCollection.getPidOrNameOrId() + " unter Simulationsvariante "
							+ externalSimVariant + " durch Applikation " + _querySender + " ist nicht m�glich, weil kein entsprechendes Simulationsobjekt "
							+ "vorhanden ist."
					);
				}
				else {
					if(mutableCollection instanceof DynamicObjectType) {
						if(configSimulationObject.isSpecialTreatedDynamicObjectType((DynamicObjectType)mutableCollection)) {
							internalSimVariant = externalSimVariant;
						}
					}
					else {
						if(configSimulationObject.isSpecialTreatedSetType((ObjectSetType)mutableCollection.getType())) {
							internalSimVariant = externalSimVariant;
						}
					}
				}
			}
			return internalSimVariant;
		}

		/**
		 * Schreibt eine Liste mit Systemobjekten in eine Serializer. Der Aufbau ist 1) L�nge der Liste (int) 2)Referenzen auf die Objekte der Liste
		 *
		 * @param serializer Serialisierer, in den die Daten geschrieben werden.
		 * @param result     Liste, die geschrieben werden soll. Wird <code>null</code> �bergeben, so wird f�r die L�nge der Liste eine 0 geschrieben.
		 *
		 * @throws IOException Fehler beim schreiben der Daten
		 */
		private void serializeObjectList(final Serializer serializer, final List<SystemObject> result) throws IOException {
			final List<SystemObject> notNullResult;
			if(result == null) {
				notNullResult = new ArrayList<SystemObject>(0);
			}
			else {
				notNullResult = result;
			}

			serializer.writeInt(result.size());
			for(SystemObject systemObject : notNullResult) {
				serializer.writeObjectReference(systemObject);
			}
		}

		/**
		 * Bearbeitet eine Anfrage vom Typ "DynamischeMengeElemente�ndern" oder "KonfigurierendeMengeElemente�ndern". Dabei wird der Auftrag aus dem
		 * Parameter deserializer ausgelesen und in die Antwort in den Paramter serializer geschrieben.
		 * <p/>
		 * Bei dynamischen Mengen wird die Simulationsvariante ber�cksichtigt, falls dies gefordert wird.
		 * <p/>
		 * Bei konfigurierenden Mengen wird die Simulationsvariante niemals ber�cksichtigt.
		 *
		 * @param deserializer              Enth�lt den Auftrag
		 * @param serializer                In dieses Objekt wird die Antwort geschrieben
		 * @param set                       Menge (dynamisch oder konfigurierend)
		 * @param mutableSet                true = Es handelt sich um eine dynamische Menge
		 * @param considerSimulationVariant true = Die Simulationsvariante muss ber�cksichtigt werden (dieser Parameter ist nur f�r dynamische Mengen wichtig)
		 *
		 * @throws IOException
		 * @throws ConfigurationChangeException
		 */
		private void handleChangeElementsRequest(
				final Deserializer deserializer, final Serializer serializer, final ObjectSet set, boolean mutableSet, boolean considerSimulationVariant)
				throws IOException, ConfigurationChangeException {

			// Einlesen der Objekte in entsprechende Arrays (Add und Remove Arrays)

			int addLength = deserializer.readInt();
			final List<SystemObject> addElements = new LinkedList<SystemObject>();
			for(int i = 1; i <= addLength; i++) {
				SystemObject object = deserializer.readObjectReference(_localConfiguration);

				if(object == null) {
					throw new ConfigurationException("Element konnte nicht aus der Konfiguration gelesen werden.");
				}
				addElements.add(object);
			}

			int removeLength = deserializer.readInt();
			final List<SystemObject> removeElements = new LinkedList<SystemObject>();
			for(int i = 1; i <= removeLength; i++) {
				SystemObject object = deserializer.readObjectReference(_localConfiguration);
				if(object == null) {
					throw new ConfigurationException("Element konnte nicht aus der Konfiguration gelesen werden.");
				}
				removeElements.add(object);
			}

			// hinzuf�gen/l�schen der Elemente aus der Menge

			try {
				if(addLength > 0) {
					if(mutableSet == false || considerSimulationVariant == false) {
						set.add(addElements.toArray(new SystemObject[addElements.size()]));
					}
					else {
						// Es handelt sich um eine Simulation und die Simulationsvariante muss ber�cksichtigt werden
						((ConfigMutableSet)set).add(addElements.toArray(new SystemObject[addElements.size()]), _simulationVariant);
					}
				}
				if(removeLength > 0) {
					if(mutableSet == false || considerSimulationVariant == false) {
						set.remove(removeElements.toArray(new SystemObject[removeElements.size()]));
					}
					else {
						((ConfigMutableSet)set).remove(removeElements.toArray(new SystemObject[removeElements.size()]), _simulationVariant);
					}
				}
				serializer.writeBoolean(true);
			}
			catch(ConfigurationException ex) {
				serializer.writeBoolean(false);
				serializer.writeString(ex.getMessage());
			}
		}

		/**
		 * Lie�t aus einem Deserialisierer ein Objekt vom Typ ObjectTimeSpecification aus.
		 *
		 * @param deserializer
		 *
		 * @return ObjectTimeSpecification
		 *
		 * @throws IOException
		 */
		private ObjectTimeSpecification deserializeObjectTimeSpecification(Deserializer deserializer) throws IOException {
			// ObjectTimeSpecification einlesen
			final TimeSpecificationType timeSpecType = TimeSpecificationType.getInstance(deserializer.readShort());

			final ObjectTimeSpecification objectTimeSpecification;

			if(timeSpecType == TimeSpecificationType.VALID) {
				// Es gibt kein getTime
				// Es gibt kein getStartTime
				// Es gibt kein getEndTime
				// Es muss nichts deserialisiert werden
				objectTimeSpecification = ObjectTimeSpecification.valid();
			}
			else if(timeSpecType == TimeSpecificationType.VALID_AT_TIME) {
				// Es gibt kein getStartTime
				// Es gibt kein getEndTime
				// nur getTime serialisieren
				final long time = deserializer.readLong();
				objectTimeSpecification = ObjectTimeSpecification.valid(time);
			}
			else {
				// Es gibt kein getTime
				final long startTime = deserializer.readLong();
				final long endTime = deserializer.readLong();
				if(timeSpecType == TimeSpecificationType.VALID_DURING_PERIOD) {
					objectTimeSpecification = ObjectTimeSpecification.validDuringPeriod(startTime, endTime);
				}
				else {
					objectTimeSpecification = ObjectTimeSpecification.validInPeriod(startTime, endTime);
				}
			}
			return objectTimeSpecification;
		}

		/**
		 * Lie�t aus einem Deserializer Konfigurationsbereiche und zugeh�rige Versionen aus.
		 *
		 * @param deserializer s.o.
		 *
		 * @return s.o.
		 */
		private Collection<ConfigAreaAndVersion> createAreaAndVersion(Deserializer deserializer) throws IOException {
			// Aufbau der Daten
			// 1) Anzahl Bereiche+Versionen, int
			// a) und b) sind so oft vorhanden, wie die Zahl, die unter 1) eingelesen wurde
			// a) Konfigurationsbereich, Referenz auf das Objekt
			// b) Version des Bereichs, short

			final int numberOfData = deserializer.readInt();

			final List<ConfigAreaAndVersion> configAreaAndVersions = new ArrayList<ConfigAreaAndVersion>(numberOfData);
			for(int nr = 0; nr < numberOfData; nr++) {
				final ConfigurationArea configurationArea = (ConfigurationArea)deserializer.readObjectReference(_localConfiguration);
				final short version = deserializer.readShort();
				configAreaAndVersions.add(new ConfigAreaAndVersion(configurationArea, version));
			}
			return configAreaAndVersions;
		}

		/**
		 * Schreibt das Ergebnis einer Konsistenzpr�fung in einen Serialisierer.
		 *
		 * @param consistencyCheckResult Ergebnis der Pr�fung
		 * @param serializer             In diesen Serialisierer wird das Ergebnis der Pr�fung geschrieben
		 */
		private void transferConsistencyCheckResult(ConsistencyCheckResultInterface consistencyCheckResult, Serializer serializer) throws IOException {
			// Es wird f�r jeden Fall (lokaler Fehler, Interferenzfehler, Warnung) ein Eintrag erzeugt.
			// Gab es keinen Fehler, wird f�r diesen Fall eine "0" eingetragen, somit kann auf der Gegenseite erkannt werden,
			// das es diesen Fehler nicht gab.

			// Aufbau der Daten:
			// Anzahl lokaler Fehler(0 bedeutet, es gab keinen Fehler), int
			//		Pro lokalen Fehler gibt es folgenden Eintrag:
			//			Referenz auf den Konfigurationsbereich, Objektreferenz
			//			Fehlertext des Fehlers, String
			//			Anzahl betroffener Objekte, int (Jeder Eintrag entspricht einem Objekt und das Objekt wird �ber eine Referenz identifiziert)
			//				Id des betroffenen Objekts, Objektreferenz
			// Anzahl Interferenzfehler(0 bedeutet, es gab keinen Fehler), int
			// 		Der Aufbau ist gleich dem der lokalen Fehler
			// Anzahl Warnungen(0 bedeutet, es gab keinen Fehler), int
			//		Der Aufbau ist gleich dem der lokalen Fehler

			if(consistencyCheckResult.localError()) {
				// Es gab lokale Fehler
				transferList(consistencyCheckResult.getLocalErrors(), serializer);
			}
			else {
				// Es gab keine lokalen Fehler
				serializer.writeInt(0);
			}

			if(consistencyCheckResult.interferenceErrors()) {
				// Es gab Interferenzfehler
				transferList(consistencyCheckResult.getInterferenceErrors(), serializer);
			}
			else {
				// Es gab keine Interferenzfehler
				serializer.writeInt(0);
			}

			if(consistencyCheckResult.interferenceErrors()) {
				// Es gab Warnungen
				transferList(consistencyCheckResult.getWarnings(), serializer);
			}
			else {
				// Es gab keine Warnungen
				serializer.writeInt(0);
			}
		}

		/**
		 * Schreibt eine Liste, die lokale/Interferenzfehler oder Warnungen enth�lt in einen Serializer.
		 *
		 * @param errorOrWarning Fehler oder Warnungen. Enth�lt die Liste keine Eintr�ge, wird eine Exception geworfen
		 * @param serializer     Serializer, in den die Fehler, bzw. Warnungen geschrieben werden
		 *
		 * @throws IllegalArgumentException Die Liste enth�lt keine Eintr�ge
		 */
		private void transferList(List<ConsistencyCheckResultEntry> errorOrWarning, Serializer serializer) throws IllegalArgumentException, IOException {
			if(errorOrWarning.size() > 0) {
				// Gr��e der Liste
				serializer.writeInt(errorOrWarning.size());
				for(ConsistencyCheckResultEntry consistencyCheckResultEntry : errorOrWarning) {
					// Konfigurationsbereich
					serializer.writeObjectReference(consistencyCheckResultEntry.getConfigurationArea());
					// Fehlertext
					serializer.writeString(consistencyCheckResultEntry.getErrorText());
					// Anzahl der betroffenen Objekte
					final SystemObject[] involvedObjects = consistencyCheckResultEntry.getInvolvedObjects();
					serializer.writeInt(involvedObjects.length);
					for(SystemObject systemObject : involvedObjects) {
						serializer.writeObjectReference(systemObject);
					}
				}
			}
			else {
				throw new IllegalArgumentException();
			}
		}

		private void writeSystemObject(Serializer serializer, SystemObject systemObject, long queryId, String queryPid) throws IOException, ConfigurationException {
			if(systemObject instanceof DynamicObject) {
				DynamicObject object = (DynamicObject)systemObject;
				serializer.writeByte(1);	// dynamisches Objekt
				serializer.writeLong(systemObject.getId());
				serializer.writeLong(systemObject.getType().getId());
				final String pid = object.getPid();
				final String name = object.getName();
				byte flag = 0;
				if(object.isValid()) flag |= 1;
				if(pid != null) flag |= 2;
				if(name != null) flag |= 4;
				serializer.writeByte(flag);
				if(pid != null) serializer.writeString(pid);
				if(name != null) serializer.writeString(name);
				serializer.writeLong(object.getValidSince());
				serializer.writeLong(object.getNotValidSince());
				serializer.writeLong(object.getConfigurationArea().getId());
			}
			else if(systemObject instanceof ConfigurationObject) {
				serializer.writeByte(2);
				serializer.writeLong(systemObject.getId());
				serializer.writeLong(systemObject.getType().getId());
			}
			else {
				serializer.writeByte(0);
				serializer.writeLong(queryId);
				serializer.writeString(queryPid);
			}
		}

		/**
		 * Diese Methode erzeugt ein dynamisches Objekt. Dabei wird ber�cksichtigt, ob es sich um eine Simulation handelt, ist dies der Fall, wird gepr�ft, ob die
		 * Simulation das �berhaupt darf.
		 *
		 * @param configurationArea  Bereich an dem das Objekt angelegt werden soll
		 * @param type               Typ des neuen Objekts.
		 * @param pid                Pid des neuen Objekts
		 * @param name               Name des neuen Objekts
		 * @param data               Alle Datens�tze, die am neuen Objekt angelegt werden sollen. Es kann eine leere Liste oder <code>null</code> �bergeben werden.
		 * @param useOldCreateMethod <code>true</code>, wenn die alte "Create-Methode" benutzt werden soll. Die alte create-Methode pr�ft DERZEIT((29.6.2007) das soll
		 *                           sp�ter anders sein) nicht, ob alle Datens�tze am neuen Objekt vorhanden sind, die vorhanden sein m�ssen. <code>false</code>, es
		 *                           wird die neue create-Methode benutzt und somit gepr�ft, ob alle geforderten Datens�tze vorhanden sind. Wurde die Software
		 *                           umgestellt(es muss immer gerp�ft werden), kann dieser Parameter sehr wahrscheinlich entfernt werden.
		 *
		 * @return Neues Objekt
		 *
		 * @throws ConfigurationChangeException Die Konfiguration kann das neue Objekt nicht anlegen (mangelnde Rechte, eine Simuluation darf keine Typen dieser
		 *                                      Objekte anlegen, usw.).
		 */
		private DynamicObject createDynamicObject(
				ConfigConfigurationArea configurationArea,
				DynamicObjectType type,
				String pid,
				String name,
				List<DataAndATGUsageInformation> data,
				boolean useOldCreateMethod) throws ConfigurationChangeException {

			// Bei dynamischen Objekte muss gepr�ft werden ob es sich um eine Simulation handelt. Und eventuelle Listener
			// m�ssen benachrichtigt werden.

			// Handelt es sich um eine Simulation muss gepr�ft werden ob der Typ des neuen Objekts an der Simulationsstrecke
			// festgelegt wurde.
			// Ist das der Fall so wird das neue Objekt mit der Simulationsvariante der Simulation angelegt.
			// Im anderen Fall wird eine Exception geworfen.
			// Handelt es sich um keine Simulation, so kann das Objekt normal angelegt werden.

			final DynamicObject newObject;
			if(_simulationVariant <= 0) {
				// Es ist keine Simulation
				if(useOldCreateMethod == false) {
					newObject = ((ConfigConfigurationArea)configurationArea).createDynamicObject(type, pid, name, data);
				}
				else {
					// Dieser Weg wird nur gegangen, wenn die alte Methode benutzt werden soll (diese pr�ft nicht, ob die Datens�tze alle
					// vorhanden sind).
					newObject = ((ConfigConfigurationArea)configurationArea).createDynamicObject(type, pid, name);
				}
			}
			else {
				// Darf die Simulation ein Objekt von diesem Typ anlegen?
				if(_simulationObject.isSpecialTreatedDynamicObjectType(type)) {
					if(useOldCreateMethod == false) {
						newObject = ((ConfigConfigurationArea)configurationArea).createDynamicObject(type, pid, name, data, _simulationVariant);
					}
					else {
						// Dieser Weg wird nur gegangen, wenn die alte Methode benutzt werden soll (diese pr�ft nicht, ob die Datens�tze alle
						// vorhanden sind).
						newObject = ((ConfigConfigurationArea)configurationArea).createDynamicObject(type, pid, name, _simulationVariant);
					}
				}
				else {
					// Eine Simulation versucht ein Objekt anzulegen, obwohl dies f�r diesen Typ nicht erlaubt ist
					throw new ConfigurationChangeException(
							"Eine Simulation " + _simulationObject.getSimulationObject().getPidOrNameOrId() + " und Simulationsvariante " + _simulationVariant
							+ " versucht ein dynamisches Objekt vom Typ " + type.getPidOrNameOrId()
							+ " anzulegen, obwohl f�r die Simulation dieser Typ nicht an der Simulationsstrecke angegeben ist."
					);
				}
			}
			return newObject;
		}

		/** Intern verwendeter Listener f�r Anmeldungen auf �nderungen der Elemente von dynamischen Zusammenstellungen. */
		private class PublishingMutableCollectionChangeListener implements ExtendedMutableCollectionChangeListener {

			/** Applikationsobjekt dient zur richtigen Zuordnung der Anmeldungen. */
			private final SystemObject _querySender;

			/** Dynamische Menge oder dynamischer Typ bei dem eine Anmeldung vorgenommen wurde. */
			private final MutableCollection _mutableCollection;

			/** Von der Applikation vorgegebene Simulationsvariante */
			private final short _externalSimVariant;

			/** Intern verwendete Simulationsvariante. Dieses Feld wird bei den Methoden <code>equals</code> und <code>hashCode</code> nicht ber�cksichtigt. */
			private final short _internalSimVariant;

			private final ForeignMutableCollectionProxy _foreignMutableCollectionProxy;

			private final int _queryIndex;

			/** Erzeugt ein neues Objekt mit den angegebenen Werten */
			public PublishingMutableCollectionChangeListener(
					final SystemObject querySender, final MutableCollection mutableCollection, final short externalSimVariant, final short internalSimVariant,
					final ForeignMutableCollectionProxy foreignMutableCollectionProxy, final int queryIndex) {
				_querySender = querySender;
				_mutableCollection = mutableCollection;
				_externalSimVariant = externalSimVariant;
				_internalSimVariant = internalSimVariant;
				_foreignMutableCollectionProxy = foreignMutableCollectionProxy;
				_queryIndex = queryIndex;
			}


			public ForeignMutableCollectionProxy getForeignMutableCollectionProxy() {
				return _foreignMutableCollectionProxy;
			}

			/**
			 * Liefert die zugeh�rige dynamische Menge oder den dynamischen Typ bei dem eine Anmeldung vorgenommen wurde bzw. wird.
			 * @return Zugeh�rige dynamische Menge oder dynamischer Typ.
			 */
			public MutableCollection getMutableCollection() {
				return _mutableCollection;
			}

			/**
			 * @return Von der Applikation vorgegebene Simulationsvariante.
			 */
			public short getExternalSimVariant() {
				return _externalSimVariant;
			}

			/**
			 * @return Intern verwendete Simulationsvariante. Dieses Feld wird bei den Methoden <code>equals</code> und <code>hashCode</code> nicht ber�cksichtigt.
			 */
			public short getInternalSimVariant() {
				return _internalSimVariant;
			}

			/**
			 * {@inheritDoc} Die intern verwendete Simulationsvariante geht nicht in den Hashcode ein.
			 */
			public int hashCode() {
				return (_querySender.hashCode() << 10 ) ^ _mutableCollection.hashCode() ^ (_externalSimVariant << 16);
			}

			/**
			 * {@inheritDoc} Die intern verwendete Simulationsvariante wird beim Vergleich nicht ber�cksichtigt.
			 */
			public boolean equals(Object obj) {
				if(this == obj) return true;
				if(obj instanceof PublishingMutableCollectionChangeListener) {
					PublishingMutableCollectionChangeListener other = (PublishingMutableCollectionChangeListener)obj;
					return _externalSimVariant == other._externalSimVariant && _querySender.equals(other._querySender) && _mutableCollection.equals(other._mutableCollection);
				}
				return false;
			}

			public void collectionChanged(
					MutableCollection mutableCollection, short simVariant, List<SystemObject> addedElements, List<SystemObject> removedElements) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					serializer.writeObjectReference((SystemObject)mutableCollection);
					serializer.writeShort(_externalSimVariant);
					serializer.writeInt(addedElements.size());
					for(SystemObject addedElement : addedElements) {
						serializer.writeObjectReference(addedElement);
					}
					serializer.writeInt(removedElements.size());
					for(SystemObject removedElement : removedElements) {
						serializer.writeObjectReference(removedElement);
					}
					_senderReplyReadTasks.sendData("DynamischeKollektionAktualisierung", byteArrayStream.toByteArray(), 0);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Der Empf�nger der Daten ist nicht mehr erreichbar. Also kann die Nachricht auch verworfen werden.
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Aktualisierung von " + mutableCollection + " kann nicht versendet werden";
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}

			public void initialState(final MutableCollection mutableCollection, final short simulationVariant, List<SystemObject> elements) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					serializer.writeObjectReference(mutableCollection);
					serializer.writeShort(_externalSimVariant);
					serializer.writeInt(elements.size());
					for(SystemObject element : elements) {
						serializer.writeObjectReference(element);
					}

					_senderReplyReadTasks.sendData("DynamischeKollektionElemente", byteArrayStream.toByteArray(), _queryIndex);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Der Empf�nger der Daten ist nicht mehr erreichbar. Also kann die Nachricht auch verworfen werden.
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Initialer Zustand von " + mutableCollection + " kann nicht versendet werden";
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}
		/** Intern verwendeter Listener f�r Anmeldungen auf �nderungen der Elemente von dynamischen Zusammenstellungen. */
		private class PublishingCommunicationStateListener implements ForeignConfigRequester.ForeignCommunicationStateListener {

			/** Applikationsobjekt dient zur richtigen Zuordnung der Anmeldungen. */
			private final SystemObject _querySender;

			private final SystemObject _object;

			private final ForeignConfigRequester _foreignConfigRequester;

			/** Erzeugt ein neues Objekt mit den angegebenen Werten */
			public PublishingCommunicationStateListener (
					final SystemObject querySender, final SystemObject object, final ForeignConfigRequester foreignConfigRequester) {
				_querySender = querySender;
				_object = object;
				_foreignConfigRequester = foreignConfigRequester;
			}


			public ForeignConfigRequester getForeignConfigRequester() {
				return _foreignConfigRequester;
			}

			/**
			 * {@inheritDoc} Der ForeignConfigRequester geht nicht in den Hashcode mit ein.
			 */
			public int hashCode() {
				return (_querySender.hashCode() << 10 ) ^ _object.hashCode();
			}

			/**
			 * {@inheritDoc} Der ForeignConfigRequester wird beim Vergleich nicht ber�cksichtigt.
			 */
			public boolean equals(Object obj) {
				if(this == obj) return true;
				if(obj instanceof PublishingCommunicationStateListener) {
					PublishingCommunicationStateListener other = (PublishingCommunicationStateListener)obj;
					return _querySender.equals(other._querySender) && _object.equals(other._object);
				}
				return false;
			}

			public void communicationStateChanged(boolean communicationState) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					serializer.writeObjectReference(_object);
					serializer.writeBoolean(communicationState);
					_senderReplyReadTasks.sendData("KommunikationszustandAktualisierung", byteArrayStream.toByteArray(), 0);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Der Empf�nger der Daten ist nicht mehr erreichbar. Also kann die Nachricht auch verworfen werden.
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Aktualisierung des Kommunikationszustands von " + _object + " kann nicht versendet werden";
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}

		/**
		 * Diese Klasse wird bei Dynamischen Mengen angemeldet, wenn man �ber �nderungen der dynamischen Menge informiert werden m�chte. Wird die {@link
		 * #update}-Methode aufgerufen, dann wird eine Antwort mit den �nderungen zusammengestellt und an den Datenverteiler geschickt. Der {@link
		 * de.bsvrz.dav.daf.main.impl.config.request.RemoteRequestManager} liest und wertet den geschickten Datensatz aus.
		 */
		private class MutableSetChangePublisher implements MutableSetChangeListener {

			public void update(MutableSet set, SystemObject[] addedObjects, SystemObject[] removedObjects) {
				if(set != null) {
					try {
						String messageType;
						byte[] message = new byte[0];
						try {

							ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
							Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);
							serializer.writeObjectReference(set);	// Dynamische Menge, die ver�ndert wurde

							if(addedObjects == null) {
								serializer.writeInt(0);
							}
							else {
								serializer.writeInt(addedObjects.length);
								for(int i = 0; i < addedObjects.length; i++) {
									SystemObject addedObject = addedObjects[i];
									serializer.writeObjectReference(addedObject);
								}
							}
							if(removedObjects == null) {
								serializer.writeInt(0);
							}
							else {
								serializer.writeInt(removedObjects.length);
								for(int i = 0; i < removedObjects.length; i++) {
									SystemObject removedObject = removedObjects[i];
									serializer.writeObjectReference(removedObject);
								}
							}

							message = byteArrayStream.toByteArray();
							messageType = "DynamischeMengeAktualisierung";
						}
						catch(Exception e) {
							// Fehler
							e.printStackTrace();
							ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
							Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);
							final String errorMessage = "Fehler beim Erzeugen der Antwort: " + e;
							serializer.writeString(errorMessage);
							message = byteArrayStream.toByteArray();
							messageType = "FehlerAntwort";
							_debug.warning("Fehler beim Erzeugen einer Aktualisierungsnachricht bei �nderung einer dynamischen Menge ", e);
						}
						_senderReplyReadTasks.sendData(messageType, message, 0);
					}
					catch(Exception e) {
						e.printStackTrace();
						_debug.error("Fehler beim Versenden einer Aktualisierungsnachricht bei �nderung einer dynamischen Menge", e);
					}
				}
			} // update
		} // class
	}

	private final class ObjectCreatedListenerForTyps implements DynamicObjectType.DynamicObjectCreatedListener {

		private final SenderReceiverCommunication _sender;

		/** Der Typ, den dieser Listener unterst�tzt. */
		private final DynamicObjectType _dynamicObjectType;


		public ObjectCreatedListenerForTyps(final SenderReceiverCommunication sender, final DynamicObjectType dynamicObjectType) {
			_sender = sender;
			_dynamicObjectType = dynamicObjectType;
		}

		public void objectCreated(DynamicObject createdObject) {

			// Es wird nur dann ein Telegramm verschickt, wenn das Objekt vom richtigen Typ ist.
			// Der Supertyp versucht auch diese Nachricht zu verschicken, es reicht allerdings, wenn nur der
			// direkte Typ des Objekts diese Nachricht verschickt. Da auf der Gegenseite diese Nachricht an alle Supertypen
			// propagiert wird.
			if(createdObject.getType() == _dynamicObjectType) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					// Telegrammaufbau
					// Ein neues dynamisches Objekt wurde erzeugt. Das Telegramm besitzt folgenden Aufbau
					// 0) Was f�r ein Telegramm wird verschickt
					// 1) Id des Objekts, das erzeugt wurde (long)
					// 2) Id des Typs des Objekts (long)

					serializer.writeByte(KindOfUpdateTelegramm.CREATED.getCode());
					serializer.writeLong(createdObject.getId());
					serializer.writeLong(createdObject.getType().getId());

					_sender.sendData("Objektaktualisierung", byteArrayStream.toByteArray(), 0);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Die Daten konnten nicht verschickt werden, weil der Empf�nger nicht mehr vorhanden ist. Also interssiert ihn auch
					// die Antwort nicht mehr
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Die Konfiguration ist nicht in der Lage die Objekte der angemeldeten Apllikationen zu aktualisieren";
					_debug.fine(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}
	}

	private final class NameChangedListenerForTyps implements DynamicObjectType.NameChangeListener {

		private final SenderReceiverCommunication _sender;

		/** Der Typ, den dieser Listener unterst�tzt. */
		private final DynamicObjectType _dynamicObjectType;

		private boolean _isRequestFromApplication;

		public NameChangedListenerForTyps(
				final SenderReceiverCommunication sender, final DynamicObjectType dynamicObjectType, final boolean requestFromApplication) {
			_isRequestFromApplication = requestFromApplication;
			_sender = sender;
			_dynamicObjectType = dynamicObjectType;
		}

		public void nameChanged(DynamicObject newNamedObject) {
			// Es wird nur dann ein Telegramm verschickt, wenn das Objekt vom richtigen Typ ist.
			// Der Supertyp versucht auch diese Nachricht zu verschicken, es reicht allerdings, wenn nur der
			// direkte Typ des Objekts diese Nachricht verschickt. Da auf der Gegenseite diese Nachricht an alle Supertypen
			// propagiert wird.
			// Wenn der Kommunikationspartner eine Applikation ist, dann bekommt er alle Aktualisierungen und wenn er eine andere
			// Konfiguration ist, bekommt er nur die Aktualisierungen, die sich auf lokale Objekte beziehen
			if(newNamedObject.getType() == _dynamicObjectType && (_isRequestFromApplication || newNamedObject instanceof ConfigDynamicObject)) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					// Telegrammaufbau
					// 0) Was f�r ein Telegramm ist es (byte)
					// 1) Id des Objekt, dessen Name ge�ndert werden soll (long)
					// 2) Id des Typs von dem das Objekt ist (long)
					// 3) Der neue Name (String)

					serializer.writeByte(KindOfUpdateTelegramm.UPDATE_NAME.getCode());
					serializer.writeLong(newNamedObject.getId());
					serializer.writeLong(newNamedObject.getType().getId());
					serializer.writeString(newNamedObject.getName());

					_sender.sendData("Objektaktualisierung", byteArrayStream.toByteArray(), 0);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Die Daten konnten nicht verschickt werden, weil der Empf�nger nicht mehr vorhanden ist. Also interssiert ihn auch
					// die Antwort nicht mehr
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Die Konfiguration ist nicht in der Lage die Objekte der angemeldeten Apllikationen zu aktualisieren";
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}
	}

	private final class InvalidationListenerForTyps implements InvalidationListener {

		private final SenderReceiverCommunication _sender;

		/** Der Typ, den dieser Listener unterst�tzt. */
		private final DynamicObjectType _dynamicObjectType;

		private final boolean _isRequestFromApplication;

		/**
		 * @param sender            �ber dieses Objekt werden alle �nderungen an Objekten an alle angeschlossenen Applikationen propagiert.
		 * @param dynamicObjectType Typ, f�r den dieser Listener verantwortlich ist.
		 */
		public InvalidationListenerForTyps(final SenderReceiverCommunication sender, final DynamicObjectType dynamicObjectType, boolean isRequestFromApplication) {
			_sender = sender;
			_dynamicObjectType = dynamicObjectType;
			_isRequestFromApplication = isRequestFromApplication;
		}

		public void invalidObject(DynamicObject dynamicObject) {

			// Es wird nur dann ein Telegramm verschickt, wenn das Objekt vom richtigen Typ ist.
			// Der Supertyp versucht auch diese Nachricht zu verschicken, es reicht allerdings, wenn nur der
			// direkte Typ des Objekts diese Nachricht verschickt. Da auf der Gegenseite diese Nachricht an alle Supertypen
			// propagiert wird.
			// Wenn der Kommunikationspartner eine Applikation ist, dann bekommt er alle Aktualisierungen und wenn er eine andere
			// Konfiguration ist, bekommt er nur die Aktualisierungen, die sich auf lokale Objekte beziehen
			if(dynamicObject.getType() == _dynamicObjectType && (_isRequestFromApplication || dynamicObject instanceof ConfigDynamicObject)) {
				try {
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					final Serializer serializer = SerializingFactory.createSerializer(2, byteArrayStream);

					// Telegrammaufbau
					// 0) Was f�r ein Telegramm ist es (byte)
					// 1) Id des Objekt, dessen Version/Zeitpunkt ge�ndert werden soll (long)
					// 2) Id des Typs von dem das Objekt ist (long)
					// 3) Konfiguration oder dynamisches Objekt (byte, 0 = Konfigurationsobjekt, 1 = dynamisches Objekt)
					// Der n�chste Wert ist abh�ngig von 3), ist es ein Konfigurationsobjekt, so muss ein short gelesen werden
					// 4a) Version, ab der das Objekt ung�ltig werden wird, short
					// 4b) Zeitpunkt, ab dem das Objekt ung�ltig geworden ist, long

					serializer.writeByte(KindOfUpdateTelegramm.UPDATE_NOT_VALID_SINCE.getCode());
					serializer.writeLong(dynamicObject.getId());
					serializer.writeLong(dynamicObject.getType().getId());

					// Es ist ein dynamisches Objekt
					serializer.writeByte(1);
					// Zeitpunkt an dem es ung�ltig wird
					serializer.writeLong(dynamicObject.getNotValidSince());

					_sender.sendData("Objektaktualisierung", byteArrayStream.toByteArray(), 0);
				}
				catch(SendSubscriptionNotConfirmed e) {
					// Der Empf�nger der Daten ist nicht mehr erreichbar. Also kann die Nachricht auch verworfen werden.
				}
				catch(Exception e) {
					e.printStackTrace();
					final String errorText = "Die Konfiguration ist nicht in der Lage die Objekte der angemeldeten Apllikationen zu aktualisieren";
					_debug.error(errorText, e);
					throw new IllegalStateException(errorText, e);
				}
			}
		}
	}
}
