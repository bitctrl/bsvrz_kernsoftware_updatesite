/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.main.communication;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.ByteArrayAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.ByteAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.LongAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.StringAttribute;
import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.config.*;
import de.bsvrz.dav.daf.main.impl.config.telegrams.*;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.main.authentication.Authentication;
import de.bsvrz.puk.config.main.communication.async.AsyncIdsToObjectsRequest;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.main.simulation.ConfigSimulationObject;
import de.bsvrz.puk.config.main.simulation.SimulationHandler;
import de.bsvrz.puk.config.util.async.AsyncRequest;
import de.bsvrz.puk.config.util.async.AsyncRequestCompletion;
import de.bsvrz.puk.config.util.async.AsyncRequestQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.*;
import java.util.*;

/**
 * Diese Klasse empfängt Telegramme vom Typ "atg.konfigurationsAnfrage" und "atg.konfigurationsSchreibAnfrage" und verschickt Telegramme vom Typ
 * "atg.konfigurationsAntwort" und "atg.konfigurationsSchreibAntwort".
 * <p>
 * Die Telegramme vom Typ "atg.konfigurationsAnfrage" und "atg.konfigurationsSchreibAnfrage" werden interpretiert und an das Datenmodell weitergereicht. Die
 * Antwort des Datenmodells wird in Telegrammen vom Typ "atg.konfigurationsAntwort" und "atg.konfigurationsSchreibAntwort" an die anfragende Applikation
 * verschickt.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
class ConfigurationRequesterCommunicator {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final AsyncRequestQueue _asyncRequestQueue;

	private final ConfigDataModel _dataModel;

	private final ClientDavInterface _connection;

	private final ConfigurationAuthority _configAuthority;

	private final DataDescription _answerDataDescription;

	private final DataDescription _writeAnswerDataDescription;

	/** Wird benötigt um "isUserValid" zu bearbeiten */
	private final Authentication _authentication;

	private final Map<SystemObject, ClientInfo> _clientInfos = new TreeMap<SystemObject, ClientInfo>();

	private final ConfigurationArea _defaultConfigArea;

	private final boolean WAIT_FOR_SEND_CONTROL = true;

	private final SenderRole SENDER_ROLE = SenderRole.sender();

	/**
	 * Enthält die Objekte, die in der Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten enthalten sind.
	 * Die Antwort enthält alle gültigen Aspekte, Attribute,
	 * Attributgruppen, Attributgruppenverwendungen, Attributtypen, Konfigurationsbereiche, Konfigurationsverantwortlichem, Mengenverwendungen, Typen,
	 * Wertebereiche und Werteustände.
	 */
	private SystemObject[] _metaDataObjects;

	/**
	 * Enthält die minimalen Objekte zum Stellen von neuartigen Konfigurationsanfragen
	 */
	private SystemObject[] _metaDataObjectsMinimal;

	private ForeignObjectManager _foreignObjectManager = null;

	/**
	 * Klasse mit Informationen über Simulationen (kann initial null sein)
	 */
	private SimulationHandler _simulationHandler;
	public static final String[] META_TYPES = new String[]{
			"typ.aspekt",
			"typ.attribut",
			"typ.attributgruppe",
			"typ.attributgruppenVerwendung",
			"typ.attributTyp",
			"typ.konfigurationsBereich",
			"typ.konfigurationsVerantwortlicher",
			"typ.mengenVerwendung",
			"typ.typ",
			"typ.werteBereich",
			"typ.werteZustand",
			"menge.aspekte",
			"menge.attribute",
			"menge.attributgruppen",
			"menge.attributgruppenVerwendungen",
			"menge.mengenVerwendungen",
			"menge.objektTypen",
			"menge.werteZustaende"
	};

	public static final String[] META_TYPES_MINIMAL = new String[]{
			"typ.konfigurationsBereich",
			"typ.konfigurationsVerantwortlicher",
			"typ.aspekt",
			"typ.attribut",
			"typ.attributgruppe",
			"typ.attributgruppenVerwendung",
			"typ.attributTyp",
			"typ.mengenVerwendung",
			"typ.werteBereich",
			"typ.werteZustand",
			"menge.aspekte",
			"menge.attribute",
			"menge.attributgruppen",
			"menge.attributgruppenVerwendungen",
			"menge.mengenVerwendungen",
			"menge.objektTypen",
			"menge.werteZustaende"
	};

	public static final String[] META_OBJECTS_MINIMAL = new String[]{
			"asp.eigenschaften",
			"atgv.atg.konfigurationsAnfrage.asp.anfrage",
			"atgv.atg.konfigurationsAntwort.asp.antwort",
			"atgv.atg.konfigurationsSchreibAnfrage.asp.anfrage",
			"atgv.atg.konfigurationsSchreibAntwort.asp.antwort",
			"asp.antwort",
			"asp.anfrage",
			"atg.konfigurationsAnfrageSchnittstelleLesend",
			"atgv.atg.konfigurationsAnfrageSchnittstelleLesend.asp.anfrage",
			"atgv.atg.konfigurationsAnfrageSchnittstelleLesend.asp.antwort",
			"atg.konfigurationsAnfrageSchnittstelleSchreibend",
			"atgv.atg.konfigurationsAnfrageSchnittstelleSchreibend.asp.anfrage",
			"atgv.atg.konfigurationsAnfrageSchnittstelleSchreibend.asp.antwort",
			"atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle",
			"atgv.atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle.asp.anfrage",
			"atgv.atg.konfigurationsBenutzerverwaltungsAnfrageSchnittstelle.asp.antwort",
			"atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle",
			"atgv.atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle.asp.anfrage",
			"atgv.atg.konfigurationsBereichsverwaltungsAnfrageSchnittstelle.asp.antwort"
	};

	ConfigurationRequesterCommunicator(ConfigDataModel dataModel, Authentication authentication, ClientDavInterface connection) {
		this(null, dataModel, authentication, connection);
	}

	ConfigurationRequesterCommunicator(AsyncRequestQueue asyncRequestQueue, ConfigDataModel dataModel, Authentication authentication, ClientDavInterface connection) {
		_asyncRequestQueue = asyncRequestQueue;
		_dataModel = dataModel;
		_configAuthority = _dataModel.getConfigurationAuthority();
		_authentication = authentication;

		_connection = connection;

		AttributeGroup configAreaPropertyAtg = _dataModel.getAttributeGroup(
				"atg.konfigurationsVerantwortlicherEigenschaften"
		);
		Data configAuthorityPropertyData = _configAuthority.getConfigurationData(
				configAreaPropertyAtg
		);
		short configAuthorityCode = configAuthorityPropertyData.getScaledValue("kodierung").shortValue();
		long objectCreationIdPattern = (long)configAuthorityCode << 48;
		_debug.info("Eindeutige Kodierung des lokalen Konfigurationsverantwortlichen: " + configAuthorityCode);
		_debug.info("Maske zum Erzeugen neuer Objekte: " + objectCreationIdPattern);

		Data.TextArray defaultConfigAreaArray = configAuthorityPropertyData.getTextArray(
				"defaultBereich"
		);
		if(defaultConfigAreaArray.getLength() != 1) {
			throw new IllegalArgumentException(
					"Kein Default-Bereich für neue Objekte am Konfigurationsverantwortlichen versorgt: " + _configAuthority.getPid()
			);
		}

		final String defaultConfigAreaPid = defaultConfigAreaArray.getTextValue(0).getValueText();
		_defaultConfigArea = (ConfigurationArea)dataModel.getObject(defaultConfigAreaPid);
		if(_defaultConfigArea == null) {
			throw new IllegalArgumentException(
					"Default-Bereich '" + defaultConfigAreaPid + "' für neue Objekte am Konfigurationsverantwortlichen '" + _configAuthority.getPid()
					+ "' nicht gefunden"
			);
		}

		_metaDataObjects = getMetaDataObjects();
		_metaDataObjectsMinimal = getMetaDataObjectsMinimal();

		// Wird zum versenden von Konfigurationsantworten gebraucht
		Aspect answerAspect = _dataModel.getAspect("asp.antwort");
		_answerDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsAntwort"), answerAspect, (short)0
		);
		_writeAnswerDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsSchreibAntwort"), answerAspect, (short)0
		);

		// Als Senke für Konfigurationsanfragen anmelden
		Aspect requestAspect = _dataModel.getAspect("asp.anfrage");
		final DataDescription requestDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsAnfrage"), requestAspect, (short)0
		);

		final DataDescription writeRequestDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsSchreibAnfrage"), requestAspect, (short)0
		);

		final RequestReceiver receiver = new RequestReceiver();

		_connection.subscribeReceiver(
				receiver, _configAuthority, requestDataDescription, ReceiveOptions.normal(), ReceiverRole.drain()
		);

		_connection.subscribeReceiver(
				receiver, _configAuthority, writeRequestDataDescription, ReceiveOptions.normal(), ReceiverRole.drain()
		);
	}

	/**
	 * Ermittelt die Objekte, die in der Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten enthalten sind. Die Antwort enthält alle
	 * gültigen Aspekte, Attribute, Attributgruppen, Attributgruppenverwendungen, Attributtypen, Konfigurationsbereiche, Konfigurationsverantwortlichem,
	 * Mengenverwendungen, Typen, Wertebereiche und Wertezustände.
	 *
	 * @return Array mit allen relevanten Meta-Objekten
	 */
	private ConfigurationObject[] getMetaDataObjects() {
		_debug.finer("getMetaDataObjects");
		Set<ConfigurationObject> relevantObjects = new HashSet<ConfigurationObject>();
		for(final String type : META_TYPES) {
			SystemObjectType metaType = _dataModel.getType(type);
			final List<SystemObject> objectsOfType = metaType.getObjects();
			for(SystemObject object : objectsOfType) {
				if(object instanceof ConfigurationObject) {
					ConfigurationObject configurationObject = (ConfigurationObject) object;
					relevantObjects.add(configurationObject);
				}
			}
		}
		return relevantObjects.toArray(new ConfigurationObject[relevantObjects.size()]);
	}

	private ConfigurationObject[] getMetaDataObjectsMinimal() {
		_debug.finer("getMetaDataObjectsMinimal");
		Set<ConfigurationObject> relevantObjects = new HashSet<ConfigurationObject>();
		for(final String type : META_TYPES_MINIMAL) {
			SystemObjectType metaType = _dataModel.getType(type);
			final List<SystemObject> objectsOfType = metaType.getObjects();
			for(SystemObject object : objectsOfType) {
				if(object instanceof ConfigurationObject) {
					ConfigurationObject configurationObject = (ConfigurationObject) object;
					relevantObjects.add(configurationObject);
				}
			}
		}
		for(String pid : META_OBJECTS_MINIMAL) {
			SystemObject object = _dataModel.getObject(pid);
			addObject(relevantObjects, object);
		}
		return relevantObjects.toArray(new ConfigurationObject[relevantObjects.size()]);
	}

	static void addObject(final Set<ConfigurationObject> relevantObjects, final SystemObject object) {
		if(object instanceof ConfigurationObject) {
			ConfigurationObject configurationObject = (ConfigurationObject) object;
			relevantObjects.add(configurationObject);
			List<ObjectSet> objectSets = configurationObject.getObjectSets();
			relevantObjects.addAll(objectSets);
			for(ObjectSet objectSet : objectSets) {
				for(SystemObject systemObject : objectSet.getElements()) {
					addObject(relevantObjects, systemObject);
				}
			}
		}
	}

	/**
	 * Ermittelt die Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten.
	 *
	 * @param objects Array mit den Konfigurationsobjekten, die in der Antwort enthalten sein sollen.
	 *
	 * @param protocolVersion
	 * @return Antwortobjekt mit allen relevanten Meta-Objekten
	 */
	private MetaDataAnswer getMetaDataAnswer(Collection<SystemObject> objects, final long protocolVersion) {
		_debug.finer("determineMetaDataAnswer");
		final DafSystemObject[] metaObjectsArray = new DafSystemObject[objects.size()];
		_debug.finer("metaObjectsArray.length", metaObjectsArray.length);
		int f = 0;
		for(SystemObject object : objects) {
			metaObjectsArray[f++] = getMetaObject(object);
		}
		return new MetaDataAnswer(protocolVersion, metaObjectsArray, null);
	}

	public void setForeignObjectManager(final ForeignObjectManager foreignObjectManager) {
		_foreignObjectManager = foreignObjectManager;
	}

	public void setSimulationHandler(final SimulationHandler simulationHandler) {
		_simulationHandler = simulationHandler;
	}

	/** Nimmt Konfigurationsanfragen entgegen und leitet sie an eine Methode zum verarbeiten weiter. */
	private final class RequestReceiver implements ClientReceiverInterface {

		public void update(ResultData[] results) {
			//System.out.println("----------------------update() wurde aufgerufen------------------------");
			//printResults(results);
			for(int resultIndex = 0; resultIndex < results.length; ++resultIndex) {
				try {
					ResultData result = results[resultIndex];
					SystemObject object = result.getObject();
					DataDescription description = result.getDataDescription();
					AttributeGroup attributeGroup = description.getAttributeGroup();
					Aspect aspect = description.getAspect();
					if(result.hasData()) {
						if(object == _configAuthority) {
							if("asp.anfrage".equals(aspect.getPid())) {
								String attributeGroupPid = attributeGroup.getPid();
								Data data = result.getData();
								if("atg.konfigurationsAnfrage".equals(attributeGroupPid)) {
									processRequest(false, data);
								}
								else if("atg.konfigurationsSchreibAnfrage".equals(attributeGroupPid)) {
									processRequest(true, data);
								}
							}
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace(System.out);
					_debug.error("Fehler beim Bearbeiten einer Konfigurationsanfrage", e);
				}
			}
		}
	}

	/** Verschickt die Antworten einer Konfigurationsanfrage */
	private class ClientInfo implements ClientSenderInterface {

		private final SystemObject _client;

		private List<ResultData> _answers = null;

		private List<ResultData> _writeAnswers = null;

		private ClientInfo(SystemObject client) throws OneSubscriptionPerSendData, ConfigurationException {
			_client = client;
			if(WAIT_FOR_SEND_CONTROL) {
				_answers = new LinkedList<ResultData>();
				_writeAnswers = new LinkedList<ResultData>();
			}
			_connection.subscribeSender(this, client, _answerDataDescription, SENDER_ROLE);
			_connection.subscribeSender(this, client, _writeAnswerDataDescription, SENDER_ROLE);
		}

		private void sendData(boolean isWriteRequestAnswer, ResultData result)
				throws DataNotSubscribedException, ConfigurationException, SendSubscriptionNotConfirmed {
			synchronized(this) {
				if(isWriteRequestAnswer) {
					if(_writeAnswers == null) {
						//System.out.println("sending write request answer: " + result);
						_connection.sendData(result);
					}
					else {
						_writeAnswers.add(result);
					}
				}
				else {
					if(_answers == null) {
						//System.out.println("sending request answer: " + result);
						_connection.sendData(result);
					}
					else {
						_answers.add(result);
					}
				}
			}
		}

		/**
		 * Signalisiert einer Sendenden Quelle dass ihre Daten von einem Empfänger angemeldet wurden. Die Quelle wird damit aufgefordert Daten zu versenden.
		 *
		 * @param object          Die Anmeldeinformation der zu versendenden Daten.
		 * @param dataDescription Beschreibende Informationen zu den abzumeldenden Daten.
		 * @param state           Informationen zur angeforderten Daten : 0: bedeutet Sendung starten
		 */
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			try {
				//System.out.println("Sendesteuerung für " + dataDescription.getAttributeGroup().getNameOrPidOrId() + ": " + state);
				if(state != 0) {
					boolean terminate = false;
					if(dataDescription.getAttributeGroup() == _answerDataDescription.getAttributeGroup()) {
						if(_answers == null) {
							terminate = true;
						}
					}
					else {
						if(_writeAnswers == null) {
							terminate = true;
						}
					}
					if(terminate) {
						/** Nicht object sondern _client für die Ausgaben verwenden. Normalerweise ist beides dasselbe Objekt, aber wenn das Objekt der lokalen
						/ Konfiguration unbekannt ist, befindet sich nur in _client das {@link UnknownObject}.*/
						_debug.info(
								dataDescription.getAttributeGroup().getName() + " wird für " + _client.getType().getNameOrPidOrId() + " " + _client.getName() + " id "
								+ _client.getId() + " abgemeldet"
						);
						_connection.unsubscribeSender(this, _client, dataDescription);
						//System.out.println("unsubscribe sender done");
					}
				}
				else {
					synchronized(this) {
						if(dataDescription.getAttributeGroup() == _answerDataDescription.getAttributeGroup()) {
							if(_answers != null) {
								Iterator<ResultData> i = _answers.iterator();
								while(i.hasNext()) {
									//System.out.println("sending queued request answer");
									_connection.sendData(i.next());
								}
								_answers = null;
							}
						}
						else if(dataDescription.getAttributeGroup() == _writeAnswerDataDescription.getAttributeGroup()) {
							if(_writeAnswers != null) {
								Iterator<ResultData> i = _writeAnswers.iterator();
								while(i.hasNext()) {
									//System.out.println("sending queued write request answer");
									_connection.sendData(i.next());
								}
								_writeAnswers = null;
							}
						}
					}
				}
			}
			catch(Exception e) {
				_debug.warning("Fehler bei der Bearbeitung der Sendesteuerung: ", e);
			}
		}

		/**
		 * Liefert <code>true</code> zurück, um den Datenverteiler-Applikationsfunktionenen zu signalisieren, dass eine Sendesteuerung erwünscht ist.
		 *
		 * @param object          Wird ignoriert.
		 * @param dataDescription Wird ignoriert.
		 *
		 * @return <code>true</code>.
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}


	private void processRequest(final boolean isWriteRequest, Data data)
			throws IOException, ConfigurationChangeException, DataNotSubscribedException, OneSubscriptionPerSendData, SendSubscriptionNotConfirmed {

		StringBuilder message = new StringBuilder();
		SystemObject sender = null;
		try {
			sender = data.getReferenceValue("absenderId").getSystemObject();
			if(sender == null) {
				message.append("Das SystemObjekt des Absenders einer Konfigurationsanfrage wurde nicht gefunden:\n");
			}
		}
		catch(RuntimeException e) {
			message.append("Das SystemObjekt des Absenders einer Konfigurationsanfrage wurde nicht gefunden: (").append(e.getMessage()).append(")\n");
		}
		if(sender != null) {
			if(!sender.isValid()) {
				message.append("Als Absender einer Konfigurationsanfrage ist ein nicht mehr gültiges Objekt angegeben\n");
			}
			if(!(sender instanceof ClientApplication) && !(sender instanceof DavApplication)) {
				message.append(
						"Als Absender einer Konfigurationsanfrage ist ein Objekt angegeben, das weder eine Applikation noch einen Datenverteiler darstellt\n"
				);
			}
		}
		
		if(sender == null){
			sender = new UnknownObject(data.getReferenceValue("absenderId").getId(), _dataModel.getConfigurationAuthority().getConfigurationArea());
			final long senderId = data.getReferenceValue("absenderId").getId();
			message.append("  Id des Absenders: ").append(senderId).append("\n");		
			_debug.warning(message.toString());
			message.setLength(0);
		}
		if(message.length() != 0) {
			final long senderId = data.getReferenceValue("absenderId").getId();
			message.append("  Id des Absenders: ").append(senderId).append("\n");
			message.append("  SystemObjekt des Absenders: ").append(sender).append("\n");
			message.append(
					"  Eine mögliche Ursache dieses Problems könnte sein, dass beim Start des Datenverteilers die im Aufrufparameter -datenverteilerId= "
					+ "angegebene Objekt-Id nicht korrekt ist.\n" + "  Folgende Datenverteiler sind der Konfiguration bekannt:\n"
			);
			final SystemObjectType davType = _connection.getDataModel().getType("typ.datenverteiler");
			final List<SystemObject> davs = davType.getElements();
			Formatter formatter = new Formatter();
			formatter.format("%40s %22s %s\n", "PID", "ID", "NAME");
			for(SystemObject dav : davs) {
				formatter.format("%40s %22d %s\n", dav.getPid(), dav.getId(), dav.getName());
			}
			message.append(formatter.toString());
			_debug.error(message.toString());
			throw new IllegalArgumentException("Ungültiges SystemObjekt des Absenders einer Konfigurationsanfrage: id " + senderId);
		}

		_debug.finer("ApplikationsID: " + sender.getId());

		final String senderReference = data.getTextValue("absenderZeichen").getText();
		_debug.finer(" Bezug: " + senderReference);

		byte requestType = data.getUnscaledValue("anfrageTyp").byteValue();
		_debug.finer(" AnfrageTyp: " + requestType);

		byte[] requestData = data.getUnscaledArray("anfrage").getByteArray();
		ConfigTelegram request = ConfigTelegram.getTelegram(requestType, null);
		request.read(new DataInputStream(new ByteArrayInputStream(requestData)));
		ConfigTelegram answer = null;
		ClientInfo clientInfo = _clientInfos.get(sender);
		if(clientInfo == null) {
			clientInfo = new ClientInfo(sender);
			_clientInfos.put(sender, clientInfo);
		}

		final SystemObject finalSender = sender;
		final ClientInfo finalClientInfo = clientInfo;

		boolean sendAnswerAsynchronously = false;
		switch(requestType) {
			case ConfigTelegram.META_DATA_REQUEST_TYPE: {
				_debug.fine("META_DATA_REQUEST_TYPE");
				MetaDataRequest metaDataRequest = (MetaDataRequest) request;
				if(metaDataRequest.getProtocolVersion() == 0 || oldDataModel()){
					answer = getMetaDataAnswer(Arrays.asList(_metaDataObjects), 0);
				}
				else {
					ArrayList<SystemObject> list = new ArrayList<SystemObject>(_metaDataObjectsMinimal.length + 1);
					Collections.addAll(list, _metaDataObjectsMinimal);
					// Die Applikation braucht auf jeden Fall noch ihr eigenes Objekt und den KV
					list.add(sender);
					list.add(_configAuthority);
					answer = getMetaDataAnswer(list, ConfigDataModel.PROTOCOL_VERSION);
				}
				break;
			}
			case ConfigTelegram.OBJECT_REQUEST_TYPE: {
				SystemObjectsRequest r = (SystemObjectsRequest)request;
				_debug.finer("OBJECT_REQUEST_TYPE");
				SystemObjectRequestInfo info = r.getSystemObjectRequestInfo();
				SystemObjectAnswerInfo answerInfo = null;
				switch(info.getRequestType()) {
					case SystemObjectRequestInfo.IDS_TO_OBJECTS_TYPE: {
						_debug.fine(" IDS_TO_OBJECTS_TYPE:");
						IdsToObjectsRequest ir = (IdsToObjectsRequest)info;
						final long[] ids = ir.getIds();

						final AsyncIdsToObjectsRequest asyncIdsToObjectsRequest = new AsyncIdsToObjectsRequest(_dataModel, _foreignObjectManager, ids);
						asyncIdsToObjectsRequest.setCompletion(
								new AsyncRequestCompletion() {
									public void requestCompleted(AsyncRequest asyncRequest) {
//										System.out.println("ConfigurationRequesterCommunicator.requestCompleted");
										SystemObjectAnswerInfo asyncAnswerInfo = buildIdsToObjectsAnswerInfo(ids, asyncIdsToObjectsRequest.getObjects());
										final SystemObjectAnswer asyncAnswer = new SystemObjectAnswer(0, asyncAnswerInfo, null);
										try {
											buildAndSendReply(isWriteRequest, finalSender, senderReference, asyncAnswer, finalClientInfo);
										}
										catch(Exception e) {
											e.printStackTrace(System.out);
											_debug.error("Fehler beim asynchronen Versand einer Konfigurationsantwort: ", e);
										}
									}
								}
						);
						asyncIdsToObjectsRequest.enqueueTo(_asyncRequestQueue);
						sendAnswerAsynchronously = true;
						break;
					}
					case SystemObjectRequestInfo.PIDS_TO_OBJECTS_TYPE: {
						_debug.fine(" PIDS_TO_OBJECTS_TYPE:");
						PidsToObjectsRequest ir = (PidsToObjectsRequest)info;
						String[] pids = ir.getPids();
						DafSystemObject[] objects = new DafSystemObject[pids.length];
						for(int i = 0; i < pids.length; ++i) {
							short simulationvariant = 0;
							if(_simulationHandler != null){
								ConfigSimulationObject simulation = _simulationHandler.getSimulationByApplication(sender);
								if(simulation != null){
									simulationvariant = simulation.getSimulationVariant();
								}
							}
							SystemObject object = _dataModel.getObject(pids[i], simulationvariant);
							if(object != null) {
								_debug.finer(" pid " + pids[i] + ": " + object.getNameOrPidOrId());
							}
							else {
								_debug.warning("Objekt mit pid " + pids[i] + " nicht gefunden");
							}
							objects[i] = getMetaObject(object);
						}
						answerInfo = new PidsToObjectsAnswer(objects, null);
						break;
					}
					case SystemObjectRequestInfo.TYPE_IDS_TO_OBJECTS_TYPE: {
						_debug.finer(" TYPE_IDS_TO_OBJECTS_TYPE:");
						TypeIdsToObjectsRequest ir = (TypeIdsToObjectsRequest)info;
						long[] ids = ir.getIds();
						ObjectsList[] objects = new ObjectsList[ids.length];
						for(int i = 0; i < ids.length; ++i) {
							SystemObject typeObject = (SystemObject)_dataModel.getObject(ids[i]);
							if(typeObject instanceof SystemObjectType) {
								SystemObjectType type = (SystemObjectType)typeObject;
								_debug.finer(" type: " + type.getNameOrPidOrId());
								List<SystemObject> elementList = type.getElements();
								Iterator<SystemObject> elementIterator = elementList.iterator();
								int metaIterator = 0;
								DafSystemObject[] metaTypeElements = new DafSystemObject[elementList.size()];
								while(elementIterator.hasNext()) {
									metaTypeElements[metaIterator++] = getMetaObject(elementIterator.next());
								}
								objects[i] = new ObjectsList(ids[i], metaTypeElements, null);
							}
							else {
								if(typeObject == null) {
									_debug.finer("Typ-Objekt mit id " + ids[i] + " nicht gefunden.");
								}
								else {
									_debug.finer("Gefundenes Objekt mit id " + ids[i] + " ist kein Typ-Objekt.");
								}
							}
						}
						_debug.finer("objects.length = " + objects.length);
						answerInfo = new TypeIdsToObjectsAnswer(objects, null);
						break;
					}
				}
				if(answerInfo != null) {
					answer = new SystemObjectAnswer(0, answerInfo, null);
				}
				//Achtung Exception in parseToString
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			case ConfigTelegram.NEW_OBJECT_REQUEST_TYPE: {
				NewObjectRequest r = (NewObjectRequest)request;
				_debug.finer("NEW_OBJECT_REQUEST_TYPE:");
				SystemObjectType type = (SystemObjectType)_dataModel.getObject(r.getTypeId());
				SystemObject object;
				final DafSystemObject metaObject;
				if(type instanceof ConfigurationObjectType) {
					_debug.warning("Neue Konfigurationsobjekte können noch nicht online erzeugt werden");
					metaObject = null;
				}
				else {
					if(r.getPid() == null || _dataModel.getObject(r.getPid()) == null) {
						object = _defaultConfigArea.createDynamicObject((DynamicObjectType)type, r.getPid(), r.getName());

						metaObject = getMetaObject(object);
						_debug.finer(" neues Objekt: " + metaObject.getId() + ":" + metaObject.getPid() + ":" + metaObject.getName());
					}
					else {
						object = null;
						metaObject = null;
						_debug.warning(
								"Neues dynamisches Objekt konnte nicht erzeugt werden, da bereits ein Objekt mit " + "der Pid " + r.getPid() + " existiert."
						);
					}
				}
				answer = new NewObjectAnswer(0, metaObject, null);
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			case ConfigTelegram.TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE: {
				TransmitterConnectionInfoRequest r = (TransmitterConnectionInfoRequest)request;
				long davId = r.getTransmitterId();
				_debug.finer("TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE:");
				_debug.finer(" dav: " + davId);

				AttributeGroup connectionPropertiesAtg = _dataModel.getAttributeGroup("atg.datenverteilerTopologie");
				AttributeGroup davPropertiesAtg = _dataModel.getAttributeGroup("atg.datenverteilerEigenschaften");
				Iterator connectionIterator = _dataModel.getType("typ.datenverteilerVerbindung").getElements().iterator();
				List<TransmitterConnectionInfo> connectionInfoList = new LinkedList<TransmitterConnectionInfo>();
				while(connectionIterator.hasNext()) {
					ConfigurationObject connection = (ConfigurationObject)connectionIterator.next();
					try {
						Data connectionProperties = connection.getConfigurationData(connectionPropertiesAtg);
						if(connectionProperties == null) {
							_debug.warning("keine Topologie-Informationen für Verbindung " + connection.getNameOrPidOrId());
							continue;
						}
						//davEigenschaften der betroffenen Datenverteiler holen
						SystemObject dav1 = connectionProperties.getReferenceValue("datenverteilerA").getSystemObject();
						Data dav1Properties = dav1.getConfigurationData(davPropertiesAtg);
						if(dav1Properties == null) {
							_debug.warning("keine Eigenschaften für Datenverteiler " + dav1.getNameOrPidOrId());
							continue;
						}
						String dav1Address = dav1Properties.getTextValue("adresse").getText();
						int dav1SubAddress = dav1Properties.getScaledValue("subAdresse").intValue();
						SystemObject dav2 = connectionProperties.getReferenceValue("datenverteilerB").getSystemObject();
						Data dav2Properties = dav2.getConfigurationData(davPropertiesAtg);
						if(dav2Properties == null) {
							_debug.warning("keine Eigenschaften für Datenverteiler " + dav2.getNameOrPidOrId());
							continue;
						}
						String dav2Address = dav2Properties.getTextValue("adresse").getText();
						int dav2SubAddress = dav2Properties.getScaledValue("subAdresse").intValue();

						TransmitterInfo transmitterInfo1 = new TransmitterInfo(dav1.getId(), dav1Address, dav1SubAddress);
						TransmitterInfo transmitterInfo2 = new TransmitterInfo(dav2.getId(), dav2Address, dav2SubAddress);
						int direction = connectionProperties.getUnscaledValue("aktiverDatenverteiler").intValue();

						TransmitterInfo altTransmitterArray1[] = null;
						TransmitterInfo altTransmitterArray2[] = null;
						//	exchangeTransmitterList= new TransmitterInfo[altConnectionList.size()];
						ObjectSet altConnections = null;
						//if(dav1.getId()==davId || dav2.getId()==davId)
						altConnections = connection.getObjectSet("Ersatzverbindungen");
						if(altConnections != null) {
							List<TransmitterInfo> altTransmitterInfos1 = new LinkedList<TransmitterInfo>();
							List<TransmitterInfo> altTransmitterInfos2 = new LinkedList<TransmitterInfo>();
							List<SystemObject> altConnectionList = altConnections.getElements();
							Iterator<SystemObject> altConnectionIterator = altConnectionList.iterator();
							while(altConnectionIterator.hasNext()) {
								SystemObject altConnection = altConnectionIterator.next();
								try {
									Data altConnectionProperties = altConnection.getConfigurationData(connectionPropertiesAtg);
									if(altConnectionProperties == null) {
										_debug.warning("keine Topologie-Informationen für Ersatz-Verbindung " + altConnection.getNameOrPidOrId());
										continue;
									}
									SystemObject altDavA = altConnectionProperties.getReferenceValue("datenverteilerA").getSystemObject();
									SystemObject altDavB = altConnectionProperties.getReferenceValue("datenverteilerB").getSystemObject();
									SystemObject altDav = null;
									List<TransmitterInfo> altTransmitterInfos = null;
									if(altDavA == dav1) {
										altDav = altDavB;
										altTransmitterInfos = altTransmitterInfos1;
									}
									else if(altDavB == dav1) {
										altDav = altDavA;
										altTransmitterInfos = altTransmitterInfos1;
									}
									else if(altDavA == dav2) {
										altDav = altDavB;
										altTransmitterInfos = altTransmitterInfos2;
									}
									else if(altDavB == dav2) {
										altDav = altDavA;
										altTransmitterInfos = altTransmitterInfos2;
									}
									if(altDav != null) {
										Data altDavProperties = altDav.getConfigurationData(davPropertiesAtg);
										if(altDavProperties == null) {
											_debug.warning("keine Eigenschaften für Datenverteiler " + dav2.getNameOrPidOrId());
											continue;
										}
										TransmitterInfo altTransmitterInfo = new TransmitterInfo(
												altDav.getId(),
												altDavProperties.getTextValue("adresse").getText(),
												altDavProperties.getScaledValue("subAdresse").intValue()
										);
										altTransmitterInfos.add(altTransmitterInfo);
									}
								}
								catch(Exception e) {
									_debug.warning(
											"Fehler beim Auslesen der Topologie-Information der Ersatz-Verbindung " + connection.getNameOrPidOrId() + ": "
											+ e.getMessage()
									);
								}
							}
							altTransmitterArray1 = (TransmitterInfo[])altTransmitterInfos1.toArray(
									new TransmitterInfo[altTransmitterInfos1.size()]
							);
							altTransmitterArray2 = (TransmitterInfo[])altTransmitterInfos2.toArray(
									new TransmitterInfo[altTransmitterInfos2.size()]
							);
						}
						if(direction == 1) {
							TransmitterConnectionInfo connectionInfo1 = new TransmitterConnectionInfo(
									transmitterInfo1,
									transmitterInfo2,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)1,
									// Normaleverbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray1,
									connectionProperties.getTextValue("benutzer1").getValueText(),
									connectionProperties.getTextValue("benutzer2").getValueText()
							);
							connectionInfoList.add(connectionInfo1);
						}
						else if(direction == 2) {
							TransmitterConnectionInfo connectionInfo2 = new TransmitterConnectionInfo(
									transmitterInfo2,
									transmitterInfo1,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)1,
									// Normaleverbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray2,
									connectionProperties.getTextValue("benutzer2").getValueText(),
									connectionProperties.getTextValue("benutzer1").getValueText()
							);
							connectionInfoList.add(connectionInfo2);
						}
						else {
							TransmitterConnectionInfo connectionInfo1 = new TransmitterConnectionInfo(
									transmitterInfo1,
									transmitterInfo2,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)((direction == 0) ? 0 : 2),
									// 0 heißt Ersatzverbindung, 2 heißt doppelte verbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray1,
									connectionProperties.getTextValue("benutzer1").getValueText(),
									connectionProperties.getTextValue("benutzer2").getValueText()
							);
							connectionInfoList.add(connectionInfo1);
							TransmitterConnectionInfo connectionInfo2 = new TransmitterConnectionInfo(
									transmitterInfo2,
									transmitterInfo1,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)((direction == 0) ? 0 : 2),
									// 0 heißt Ersatzverbindung, 2 heißt doppelte verbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray2,
									connectionProperties.getTextValue("benutzer2").getValueText(),
									connectionProperties.getTextValue("benutzer1").getValueText()
							);
							connectionInfoList.add(connectionInfo2);
						}
					}
					catch(Exception e) {
						_debug.warning(
								"Fehler beim Auslesen der Topologie-Information der Verbindung " + connection.getNameOrPidOrId() + ": " + e.getMessage()
						);
					}
				}
				TransmitterConnectionInfo[] connectionInfoArray = (TransmitterConnectionInfo[])connectionInfoList.toArray(
						new TransmitterConnectionInfo[connectionInfoList.size()]
				);
				final long desiredReplyVersion = r.getDesiredReplyVersion();
				long replyVersion = 2;
				if(desiredReplyVersion < replyVersion) replyVersion = desiredReplyVersion;
				answer = new TransmitterConnectionInfoAnswer(replyVersion, davId, connectionInfoArray);
				//System.out.println(" ANSWER: " + answer.parseToString());
				break;
			}
//			case ConfigTelegram.OBJECT_SET_PID_REQUEST_TYPE: {
//				break;
//			}
//			case ConfigTelegram.SET_CHANGES_REQUEST_TYPE: {
//				break;
//			}
			case ConfigTelegram.AUTHENTIFICATION_REQUEST_TYPE: {
				// Bedingung 1: Der Benutzer muss als Objekt in der Konfiguration vorhanden sein
				// Bedingung 2: Der Benutzername muss mit dem gespeicherten Passwort übereinstimmen
				AuthentificationRequest r = (AuthentificationRequest)request;

				_debug.finer("AUTHENTIFICATION_REQUEST_TYPE: " + r.getUserName() + ":");
				Iterator<SystemObject> i = _dataModel.getType("typ.benutzer").getObjects().iterator();
				try {
					while(i.hasNext()) {
						SystemObject benutzer = i.next();
						if(r.getUserName().equals(benutzer.getName())) {
							// answer = new AuthentificationAnswer(benutzer.getId());
							_debug.finer(" gefunden, id " + benutzer.getId());
							// Wenn der Benutzer nicht identifiziert werden kann, wird eine Exception geworfen
							_authentication.isValidUser(
									r.getUserName(), r.getEncriptedPasswort(), r.getAuthentificationText(), r.getAuthentificationProcessName()
							);
							answer = new AuthentificationAnswer(benutzer.getId());
							break;
						}
					} // while über alle Benutzer

					if(answer == null) {
						answer = new AuthentificationAnswer(-1);
						_debug.warning(
								"Authentifizierung fehlgeschlagen: Zum Benutzer '" + r.getUserName() + "' wurde kein Objekt in der Konfiguration gefunden"
						);
					}
				}
				catch(Exception e) {
					// Benutzer/Passwort Kombination paßt nicht
					answer = new AuthentificationAnswer(-1);
					_debug.warning( "Authentifizierung fehlgeschlagen", e);
				}
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
//			case ConfigTelegram.NEW_IDS_REQUEST_TYPE: {
//				break;
//			}
//			case ConfigTelegram.ARCHIVE_REQUEST_TYPE: {
//				break;
//			}

			case ConfigTelegram.OBJECT_INVALIDATE_REQUEST_TYPE: {
				_debug.fine("OBJECT_INVALIDATE_REQUEST_TYPE");
				ObjectInvalidateRequest r = (ObjectInvalidateRequest)request;
				SystemObject object = (SystemObject)_dataModel.getObject(r.getObjectId());
				try {
					object.invalidate();
					long notValidSince = 0;
					if(object instanceof DynamicObject) {
						final DynamicObject dynamicObject = (DynamicObject)object;
						notValidSince = dynamicObject.getNotValidSince();
					}
					answer = new ObjectInvalidateAnswer(notValidSince, r.getObjectId(), true);
				}
				catch(ConfigurationChangeException e) {
					answer = new ObjectInvalidateAnswer(0, r.getObjectId(), false);
				}
				break;
			}
			case ConfigTelegram.OBJECT_REVALIDATE_REQUEST_TYPE: {
				_debug.fine("OBJECT_REVALIDATE_REQUEST_TYPE");
				final ObjectRevalidateRequest r = (ObjectRevalidateRequest)request;
				final ConfigurationObject object = (ConfigurationObject)_dataModel.getObject(r.getObjectId());

				try {
					object.revalidate();
					answer = new ObjectRevalidateAnswer(0, r.getObjectId(), true);
				}
				catch(ConfigurationChangeException e) {
					answer = new ObjectRevalidateAnswer(0, r.getObjectId(), false);
				}
				break;
			}
			case ConfigTelegram.OBJECT_SET_NAME_REQUEST_TYPE: {
				_debug.fine("OBJECT_SET_NAME_REQUEST_TYPE");
				ObjectSetNameRequest r = (ObjectSetNameRequest)request;
				SystemObject object = (SystemObject)_dataModel.getObject(r.getObjectId());
				try {
					object.setName(r.getObjectName());
					answer = new ObjectSetNameAnswer(0, r.getObjectId(), true);
				}
				catch(Exception e) {
					_debug.warning("Objektname konnte nicht geändert werden", e);
				}
				if(answer == null) answer = new ObjectSetNameAnswer(0, r.getObjectId(), false);

				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			default: {
				_debug.warning("Ungültige Anfrage: " + request.parseToString());
				throw new IllegalArgumentException(
						"Ungültige Konfigurationsanfrage:" + " TelegrammTyp:" + requestType + ", absender:" + finalSender + ", absenderZeichen:" + senderReference
				);
			}
		}
		if(answer != null) {
			buildAndSendReply(isWriteRequest, finalSender, senderReference, answer, finalClientInfo);
		}
		else {
			if(!sendAnswerAsynchronously) {
				_debug.warning("Zur Konfigurationsanfrage konnte keine Antwort erzeugt werden: " + request.parseToString());
			}
		}
	}

	/**
	 * Gibt true zurück, wenn die neuen Konfigurationsanfragen nicht verfügbar sind, sonst false.
	 * @return true wenn die alten Konfigurationsanfragen aufgrund eines veralteten Datenmodells benutzt werden müssen.
	 */
	private boolean oldDataModel() {
		IntegerAttributeType att;
		att = (IntegerAttributeType) _dataModel.getAttributeType("att.konfigurationsAnfrageNachrichtenTypLesend");
		for(IntegerValueState state : att.getStates()) {
			if(state.getName().equals("ObjekteAnfragenMitTyp")){
				// Neues Datenmodell ist verfügbar
				return false;
			}
		}
		return true;
	}

	private SystemObjectAnswerInfo buildIdsToObjectsAnswerInfo(final long[] ids, final SystemObject[] objects) {
		final SystemObjectAnswerInfo answerInfo;
		final DafSystemObject[] dafObjects = new DafSystemObject[objects.length];
		for(int i = 0; i < ids.length; ++i) {
			final SystemObject object = objects[i];
			if(object == null) {
				_debug.warning("Objekt nicht gefunden", ids[i]);
			}
			dafObjects[i] = getMetaObject(object);
		}
		answerInfo = new IdsToObjectsAnswer(dafObjects, null);
		return answerInfo;
	}

	private void buildAndSendReply(
			final boolean isWriteRequest, final SystemObject sender, final String senderReference, final ConfigTelegram answer, final ClientInfo clientInfo)
			throws IOException, SendSubscriptionNotConfirmed {
		//Anmelden wenn noch nicht passiert
		DataDescription resultDataDescription;
		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>(4);
		if(isWriteRequest) {
			resultDataDescription = _writeAnswerDataDescription;
		}
		else {
			resultDataDescription = _answerDataDescription;
		}
		Iterator<Attribute> attributesIterator = resultDataDescription.getAttributeGroup().getAttributes().iterator();
		while(attributesIterator.hasNext()) {
			Attribute attribute = attributesIterator.next();
			AttributeValue attributeValue = new AttributeValue(null, attribute);
			if("absenderId".equals(attribute.getName())) {
				attributeValue.setValue(new LongAttribute(_configAuthority.getId()));
			}
			else if("absenderZeichen".equals(attribute.getName())) {
				attributeValue.setValue(new StringAttribute(senderReference));
			}
			else if("antwortTyp".equals(attribute.getName())) {
				attributeValue.setValue(new ByteAttribute(answer.getType()));
			}
			else if("antwort".equals(attribute.getName())) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				DataOutputStream dataStream = new DataOutputStream(byteStream);
				answer.write(dataStream);
				dataStream.flush();
				attributeValue.setValue(new ByteArrayAttribute(byteStream.toByteArray()));
				//System.out.println("result byte array length: " + byteStream.toByteArray().length);
			}
			else {
				throw new RuntimeException("Unbekanntes Attribut in der Attributgruppe: " + attribute);
			}
			attributeValues.add(attributeValue);
		}
		ResultData result = new ResultData(
				sender, resultDataDescription, false, System.currentTimeMillis(), attributeValues
		);
		clientInfo.sendData(isWriteRequest, result);
	}

	private static long[] getIds(List<ObjectSet> systemObjects) {
		long[] ids = new long[systemObjects.size()];
		int i = 0;
		Iterator<ObjectSet> iterator = systemObjects.iterator();
		while(iterator.hasNext()) {
			ids[i++] = (iterator.next()).getId();
		}
		return ids;
	}

	private static ArrayList<Long> getIdsAsLongArrayList(List<SystemObject> systemObjects) {
		ArrayList<Long> ids = new ArrayList<Long>(systemObjects.size());
		int i = 0;
		Iterator<SystemObject> iterator = systemObjects.iterator();
		while(iterator.hasNext()) {
			SystemObject element = iterator.next();
			//System.out.println("adding " + element.getId());
			ids.add(new Long(element.getId()));
		}
		return ids;
	}

	static DafSystemObject getMetaObject(SystemObject object) throws ConfigurationException {
		if(object == null) {
			return null;
		}
		if(object instanceof ConfigurationObject) {
			byte state = object.isValid() ? (byte)1 : (byte)2;
//			byte state = (byte)1;
			if(object instanceof Aspect) {
				Aspect o = (Aspect)object;
				return new DafAspect(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof Attribute) {
				Attribute o = (Attribute)object;
				AttributeType attributeType = o.getAttributeType();
				if(attributeType == null) {
					throw new IllegalStateException("Attributtyp des Attributs " + o + " nicht definiert");
				}
				DafAttribute metaAttribute = new DafAttribute(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						(short)o.getPosition(),
						o.getMaxCount(),
						o.isCountVariable(),
						attributeType.getId(),
						o.getDefaultAttributeValue()
				);
//			if(o.getId()==504) {
//				System.out.println("Attribute 504:");
//				System.out.println("   getMaxCount():" + metaAttribute.getMaxCount() );
//				System.out.println("   isCountVariable():" + metaAttribute.isCountVariable() );
//				System.out.println("   isArray():" + metaAttribute.isArray() );
//			}
				return metaAttribute;
			}
			else if(object instanceof AttributeGroup) {
				AttributeGroup o = (AttributeGroup)object;
				return new DafAttributeGroup(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof AttributeListDefinition) {
				AttributeListDefinition o = (AttributeListDefinition)object;
				return new DafAttributeListDefinition(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof DoubleAttributeType) {
				DoubleAttributeType o = (DoubleAttributeType)object;
				return new DafDoubleAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAccuracy(),
						o.getUnit(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof IntegerAttributeType) {
				IntegerAttributeType o = (IntegerAttributeType)object;
				IntegerValueRange range = o.getRange();
				return new DafIntegerAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getByteCount(),
						range == null ? 0 : range.getId(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof ReferenceAttributeType) {
				ReferenceAttributeType o = (ReferenceAttributeType)object;
				SystemObjectType referencedType = o.getReferencedObjectType();
				return new DafReferenceAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						referencedType == null ? 0 : referencedType.getId(),
						o.getDefaultAttributeValue(),
						o.isUndefinedAllowed(),
						o.getReferenceType()
				);
			}
			else if(object instanceof StringAttributeType) {
				StringAttributeType o = (StringAttributeType)object;
				return new DafStringAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getMaxLength(),
						o.getEncodingName(),
						o.isLengthLimited(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof TimeAttributeType) {
				TimeAttributeType o = (TimeAttributeType)object;
				return new DafTimeAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAccuracy(),
						o.isRelative(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof ConfigurationAuthority) {
				ConfigurationAuthority o = (ConfigurationAuthority)object;
				return new DafConfigurationAuthority(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof ConfigurationArea) {
				ConfigurationArea o = (ConfigurationArea)object;
				return new DafConfigurationArea(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof IntegerValueRange) {
				IntegerValueRange o = (IntegerValueRange)object;
				return new DafIntegerValueRange(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getConversionFactor(),
						o.getMaximum(),
						o.getMinimum(),
						o.getUnit()
				);
			}
			else if(object instanceof IntegerValueState) {
				IntegerValueState o = (IntegerValueState)object;
				return new DafIntegerValueState(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.getValue()
				);
			}
			else if(object instanceof MutableSet) {
				MutableSet o = (MutableSet)object;
				return new DafMutableSet(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), getIdsAsLongArrayList(o.getElements())
				);
			}
			else if(object instanceof NonMutableSet) {
				NonMutableSet o = (NonMutableSet)object;
				return new DafNonMutableSet(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), getIdsAsLongArrayList(o.getElements())
				);
			}
			else if(object instanceof ObjectSetUse) {
				ObjectSetUse o = (ObjectSetUse)object;
				return new DafObjectSetUse(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getObjectSetName(),
						o.getObjectSetType().getId(),
						o.isRequired()
				);
			}
			else if(object instanceof ObjectSetType) {
				ObjectSetType o = (ObjectSetType)object;
				return new DafObjectSetType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						o.isNameOfObjectsPermanent(),
						getIds(o.getObjectSets()),
						o.getMinimumElementCount(),
						o.getMaximumElementCount(),
						o.isMutable()
				);
			}
			else if(object instanceof ConfigurationObjectType) {
				ConfigurationObjectType o = (ConfigurationObjectType)object;
				return new DafConfigurationObjectType(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.isNameOfObjectsPermanent()
				);
			}
			else if(object instanceof DynamicObjectType) {
				DynamicObjectType o = (DynamicObjectType)object;
				return new DafDynamicObjectType(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.isNameOfObjectsPermanent()
				);
			}
			else if(object instanceof SystemObjectType) {
				_debug.warning("Ungültiger Typ der weder konfigurierend noch dynamisch ist " + object);
				return null;
			}
			else if(object instanceof AttributeGroupUsage) {
				AttributeGroupUsage o = (AttributeGroupUsage)object;
				return new DafAttributeGroupUsage(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAttributeGroup(),
						o.getAspect(),
						o.isExplicitDefined(),
						o.getUsage()
				);
			}
			else if(object instanceof DavApplication) {
				ConfigurationObject o = (ConfigurationObject)object;
				return new DafDavApplication(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else {
				ConfigurationObject o = (ConfigurationObject)object;
				return new DafConfigurationObject(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}

		}
		else if(object instanceof DynamicObject) {
			final ConfigurationArea area = object.getConfigurationArea();
			if(object instanceof ClientApplication) {
				ClientApplication o = (ClientApplication)object;
				return new DafClientApplication(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), o.isValid() ? (byte)1 : (byte)0, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), area == null ? 0 : area.getId()
				);
			}
			else {
				DynamicObject o = (DynamicObject)object;
				return new DafDynamicObject(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), o.isValid() ? (byte)1 : (byte)0, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), area == null ? 0 : area.getId()
				);
			}

		}
		else {
			Thread.dumpStack();
			_debug.warning("Keine Objekt-Konvertierung möglich: " + object);
			return null;
		}
	}
}
