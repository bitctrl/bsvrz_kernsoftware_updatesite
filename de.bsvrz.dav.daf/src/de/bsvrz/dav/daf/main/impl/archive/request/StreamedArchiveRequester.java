/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.archive.request;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.archive.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.timeout.TimeoutTimer;

import java.util.*;

/**
 * Die Objekte dieser Klasse verwalten alle Arten von Anfragen an das Archivsystem, gleichzeitig werden auch alle Antworten vom Archivsystem, die für die
 * Applikation bestimmt sind, entgegen genommen und in entsprechende Objekte umgewandelt (der Datensatz wird von einem Objekt der Klasse {@link
 * StreamedRequestManager} empfangen, aber an den richtigen StreamedArchiveRequester weitergeleitet). Für jede anfragende Applikation wird ein solches Objekt
 * erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StreamedArchiveRequester implements ArchiveRequestManager {

	/** Stellt die Verbindung zum Archiv dar. Hier werden Steuerungs/Nutzdaten angefragt und empfangen. */
	private final ClientDavInterface _connection;

	/** Diese Variable wird benutzt um zu prüfen ob es einen Abnehmer für Archivanfragen gibt. Den Kommentar im Konstruktor beachten. */
	private byte _state = ClientSenderInterface.STOP_SENDING;

	/** Diese Objekt für <code>synchronized()</code> benötigt, es sperrt den Zugriff auf die Variable _state. */
	private final Byte _lockState = new Byte(_state);

	/** Jede Anfrage erhält einen eigene Index. Somit können Antworten des Archivs, die auch diesen Index enthalten, genau dem Empfänger zugeordnet werden. */
	private int _indexOfRequest = 0;

	/**
	 * Hier sind alle Anfragen gespeichert, als Key wird der Index der Anfrage benutzt und das Archiv von dem die Daten angefordert werden. Zu jeder Anfrage gehört
	 * ein eindeutiger Index.
	 */
	private final Map _requests;

	/**
	 * Diese Variable bestimmt die Größe des Empfangspuffers (StreamDemultiplexer). Die Größe wird in Bytes angegben. Der Wert "0" ist der default Wert. Das
	 * bedeutet, dass das Archiv die Größe des Empfangspuffers festlegt. Der Defaultwert ist in der Konfiguration gespeichert und wird dort vom Archiv angefordert.
	 * Soll ein anderer Wert benutzt werden, so kann dieser mit {@link #setReceiveBufferSize} gesetzt werden.
	 */
	private int _receiveBufferSize = 0;

	/** Für welches Archivsystem ist dieser Manager. */
	private final SystemObject _archiveSystem;

	private final Date _timeOutArchiveRequest;


	/** DebugLogger für Debug-Ausgaben */
	private static Debug _debug = Debug.getLogger();

	private ClientSender _clientSender;

	private DataDescription _dataDescriptionSender;

	/** Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt. Der Wert wird vom StreamedRequestManager gesetzt. */
	private final short _defaultSimulationVariant;

	private final SubscriptionArchiveOnlineDataManager _subscriptionArchiveOnlineDataManager;

	/** Speichert alle Listener, die benachrichtigt werden, wenn das Archivsystem nicht mehr über den DaV zu erreichen ist */
	private final List<ArchiveAvailabilityListener> _listener = new ArrayList<ArchiveAvailabilityListener>();

	private boolean _gotDataRequest;

	/**
	 * Dieser Konstruktor erzeugt ein StreamedArchiveRequester Objekt und meldet sich gleichzeitig auf der übergebenen Verbindung als Sender für Anfragen an. Die
	 * Anfragen werden an das Archiv geschickt, das ebenfalls übergeben wird. Die Anfragen enthalten einmal die Archivanfragen und als zweites Tickets, die dem
	 * Archiv erlauben weitere Datensätze zu verschicken.
	 *
	 * @param archiveConnection Eine Verbindung auf der Datensätze verschickt werden sollen
	 * @param archiveSystem     Das Archiv, für das die Datensätze bestimmt sind
	 */
	public StreamedArchiveRequester(
			ClientDavInterface archiveConnection, int timeOutArchiveRequest, SystemObject archiveSystem, short defaultSimulationVariant) {
		_connection = archiveConnection;
		_archiveSystem = archiveSystem;
		_requests = Collections.synchronizedMap(new HashMap());

		// Legt den Zeitpunkt fest, bis zu dem maximal auf eine Verbidnung zum Archivsystem gewartet wird.
		// Wird dieser Zeitpunkt überschritten, so liefert die Methode isConnectionOk immer false zurück, wenn
		// keine Verbidnung besteht, es wird nicht gewartet.
		_timeOutArchiveRequest = new Date(System.currentTimeMillis() + timeOutArchiveRequest);
		_defaultSimulationVariant = defaultSimulationVariant;

		// Konfiguration, alle Infos ueber das Objekt werden vom "Konfigurator" angefordert.
		final DataModel configuration = _connection.getDataModel();

		try {
			// Verbindung zum Archiv aufbauen

			// Auf die Attributgruppe anmelden
			final AttributeGroup attributeGroup;
			attributeGroup = configuration.getAttributeGroup("atg.archivAnfrageSchnittstelle");

			// In diesem Aspekt stehen die Anfragen/Tickets der Empfängerapplikation, diese müssen gesendet werden
			final Aspect aspectSender = configuration.getAspect("asp.anfrage");

			final short simulationVariant = 0;

			// final DataDescription dataDescriptionSender = new DataDescription(attributeGroup, aspectSender);
			_dataDescriptionSender = new DataDescription(attributeGroup, aspectSender, simulationVariant);

			// Objekt, das ein ClientSenderInterface implementiert
			_clientSender = new ClientSender();

			// Auf der connection als Sender anmelden
			_connection.subscribeSender(_clientSender, _archiveSystem, _dataDescriptionSender, SenderRole.sender());
		}
		catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
			oneSubscriptionPerSendData.printStackTrace();
		}

		// Objekt, das Anmeldungen auf Online+Archivdaten zuläßt erzeugen
		_subscriptionArchiveOnlineDataManager = new SubscriptionArchiveOnlineDataManager(this, _connection);
	}

	/**
	 * Diese Methode stellt eine Anfrage an das Archiv. Da eine Liste mit Objekten, die angefragt werden sollen, zur Verfügung gestellt wird, werden auch soviele
	 * Streams bereitgestellt, wie die Liste Einträge hat.
	 *
	 * @param priority Mit welcher Priorität soll die Anfrage beantwortet werden (hohe, mittlere, niedrige Priorität)
	 * @param specs    Eine Liste von Objekten, die alle Informationen enthalten, die zur Bearbeitung der Archivanfrage nötig sind. Für jedes Objekt der Liste wird
	 *                 ein Stream bereitgestellt, der die geforderten Informationen enthält.Wird eine leere Liste übergeben, wird das Objekt, das die Antwort auf
	 *                 diese Anfrage enthält, ein Array mit Streams zurückgegbene, das ebenfalls leer ist.
	 *
	 * @return Ein Objekt, das die Möglichkeit bietet zu prüfen ob die Anfrage erfolgreich war oder ob die Anfrage fehlgeschlagen ist. War die Anfrage erfolgreich,
	 *         so kann das Objekt die Streams zur weiteren Bearbeitung weitergeben. War die Liste <code>specs</code> leer, ist das Array, das die Streams
	 *         darstellt, ebenfalls leer.
	 *
	 * @throws IllegalStateException Das Archiv, an das die Anfrage gestellt wurde, kann nicht erreicht werden, die Anfrage wird verworfen.
	 */
	public ArchiveDataQueryResult request(ArchiveQueryPriority priority, List<ArchiveDataSpecification> specs) throws IllegalStateException {

		if(specs != null && specs.size() > 0) {
			// Am Anfang wird geprüft, ob das Archivsystem verfügbar ist, nur wenn eine Verbidnung besteht wird die
			// Anfrage überhaupt bearbeitet
			try {
				if(isConnectionOk(_timeOutArchiveRequest) == false) {
					_debug.warning(
							"Die Applikation: " + _connection.getLocalApplicationObject().getNameOrPidOrId() + " will eine Archivanfrage beim Archivsystem: "
							+ _archiveSystem.getNameOrPidOrId() + " stellen, bekommt aber keine Rückmeldung vom Archivsystem. Die Archivanfrage wird verworfen."
					);
					throw new IllegalStateException("Das Archivsystem " + _archiveSystem.getNameOrPidOrId() + " kann nicht erreicht werden");
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}

			// über diese ID ist jede Archivanfrage eindeutig zu identifizieren
			// Als Key wird ein laufender Index und das Archiv (für das die Anfrage ist) genommen
			final int indexOfRequest;
			synchronized(this) {
				indexOfRequest = _indexOfRequest;
				_indexOfRequest++;
			}

			ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

			return query(priority, specs, archiveQueryID);
		}
		else {
			if(specs == null) {
				// Es wurde <code>null</code> übergeben
				throw new IllegalArgumentException("Die Liste, die eine Archivanfrage spezifiziert, war null");
			}
			else {
				// Die Liste enthält keine Elemente, also ein Array der Größe 0 zurückgeben
				return getEmptyResult();
			}
		}
	}

	private ArchiveDataQueryResult query(final ArchiveQueryPriority priority, final List<ArchiveDataSpecification> specs, final ArchiveQueryID archiveQueryID) {
		final List<ArchiveDataSpecification> rawSpecs = new ArrayList<ArchiveDataSpecification>(specs.size());
		final ArchiveStreamCombiner combiner = new ArchiveStreamCombiner();
		for(ArchiveDataSpecification spec : specs) {
			if(spec.getQueryWithPid()) {
				Collection<ArchiveDataSpecification> split = splitQueryBetweenObjects(spec);
				rawSpecs.addAll(split);
				combiner.addQuery(split.size(), spec);
			}
			else {
				rawSpecs.add(spec);
				combiner.addQuery(1, spec);
			}
		}
		combiner.setRawResult(rawQuery(priority, rawSpecs, archiveQueryID));
		return combiner;
	}

	private Collection<ArchiveDataSpecification> splitQueryBetweenObjects(final ArchiveDataSpecification spec) {
		final List<ArchiveDataSpecification> result = new ArrayList<ArchiveDataSpecification>();
		for(SystemObject object : getAffectedObjects(spec)) {
			result.add(new ArchiveDataSpecification(spec.getTimeSpec(), spec.getDataKinds(), spec.getSortOrder(), spec.getRequestOption(), spec.getDataDescription(), object));
		}
		return result;
	}

	private SystemObject[] getAffectedObjects(final ArchiveDataSpecification spec) {
		String pid = spec.getObject().getPid();

		if(pid == null || pid.isEmpty()) {
			// Fallback, keine Pid
			return new SystemObject[]{spec.getObject()};
		}

		long start;
		long end;
		ArchiveTimeSpecification timeSpec = spec.getTimeSpec();
		if(timeSpec.getTimingType() == TimingType.DATA_TIME
				|| timeSpec.getTimingType() == TimingType.ARCHIVE_TIME){
			start = timeSpec.getIntervalStart();
			end = timeSpec.getIntervalEnd();
		}
		else {
			// Start und Ende aus angefragtem Datenindex extrahieren
			start = (timeSpec.getIntervalStart() >> 32) * 1000;
			end = (timeSpec.getIntervalEnd() >> 32) * 1000;
		}

		if(timeSpec.isStartRelative()){
			start = 0;
		}

		Collection<SystemObject> tmp = _connection.getDataModel().getObjects(pid, start, end);
		if(tmp.isEmpty()){
			// Fallback, keine passenden Objekte ermittelbar
			return new SystemObject[]{spec.getObject()};
		}
		SystemObject[] objects = tmp.toArray(new SystemObject[tmp.size()]);

		// Objekte nach Gültigkeit sortieren
		Arrays.sort(objects, new Comparator<SystemObject>() {
			@Override
			public int compare(final SystemObject o1, final SystemObject o2) {
				if(o1 instanceof ConfigurationObject && o2 instanceof ConfigurationObject) {
					int x = ((ConfigurationObject) o1).getValidSince();
					int y = ((ConfigurationObject) o2).getValidSince();
					return (x < y) ? -1 : ((x == y) ? 0 : 1);
				}
				if(o1 instanceof DynamicObject && o2 instanceof DynamicObject) {
					long x = ((DynamicObject) o1).getValidSince();
					long y = ((DynamicObject) o2).getValidSince();
					return (x < y) ? -1 : ((x == y) ? 0 : 1);
				}
				return 0;
			}
		});

		return objects;
	}

	private ArchiveDataQueryResult rawQuery(final ArchiveQueryPriority priority, final List<ArchiveDataSpecification> specs, final ArchiveQueryID archiveQueryID) {
		if(specs.size()==0){
			return getEmptyResult();
		}
		Query archiveQuery = new Query(archiveQueryID, priority, specs, _receiveBufferSize, this, _defaultSimulationVariant);
		// Die Anfrage speichern, sobald Daten für diese Anfrage kommen, kann die Archivanfrage über ihren Index
		// identifiziert werden. Der Index wird mit der Nachricht versandt.
		_requests.put(archiveQuery.getArchiveRequestID(), archiveQuery);

		archiveQuery.initiateArchiveRequest();

		return archiveQuery;
	}

	private static ArchiveDataQueryResult getEmptyResult() {
		return new ArchiveDataQueryResult() {
					public ArchiveDataStream[] getStreams() throws InterruptedException, IllegalStateException {
						return new ArchiveDataStream[0];  //To change body of implemented methods use File | Settings | File Templates.
					}

					public boolean isRequestSuccessful() throws InterruptedException {
						return true;
					}

					public String getErrorMessage() throws InterruptedException {
						return "";
					}
				};
	}

	/**
	 * Diese Methode stellt eine Anfrage an das Archiv. Da nur ein Objekt angefragt wird, steht auch nur ein Stream zur Verfügung, der Daten über das Objekt
	 * übermittelt
	 *
	 * @param priority Mit welcher Priorität soll die Anfrage beantwortet werden (hohe, mittlere, niedrige Priorität)
	 * @param spec     Ein Objekt, das alle Informationen enthält, die zur Bearbeitung der Archivanfrage nötig sind.
	 *
	 * @return Ein Objekt, das die Möglichekit bietet zu prüfen ob die Anfrage erfolgreich war oder ob die Anfrage fehlgeschlagen ist. War die Anfrage erfolgreich,
	 *         so kann das Objekt die Streams zur weiteren Bearbeitung weitergeben.
	 *
	 * @throws IllegalStateException Das Archiv, an das die Anfrage gestellt wurde, kann nicht erreicht werden, die Anfrage wird verworfen.
	 */
	public ArchiveDataQueryResult request(ArchiveQueryPriority priority, ArchiveDataSpecification spec) throws IllegalStateException {
		if(spec != null) {
			return request(priority, Collections.singletonList(spec));
		}
		else {
			throw new IllegalArgumentException("Die geforderte Spezifikation einer Archivanfrage war null");
		}
	}

	/**
	 * Die Puffergröße (in Byte) des Empfängers auf einen anderen Wert als den default Wert setzen. Der default Wert wird vom Archiv aus der Konfiguration
	 * ermittelt.
	 *
	 * @param numberOfBytes
	 */
	public void setReceiveBufferSize(int numberOfBytes) {
		_receiveBufferSize = numberOfBytes;
	}

	/**
	 * Diese Methode liefert zu einem gegebenen Index, der zu einer Archivanfrage gehört, die dazugehörige Archivanfrage. Der Index der Archivanfrage steht in der
	 * Antwort einer Archivanfrage, somit kann die Antwort einer Archivanfrage zugeordnet werden. Die Anfrage wird über ein <code>int</code> identifiziert. Dieses
	 * int wird dann intern in ein <code>Integer</code> umgewandelt.
	 *
	 * @param indexOfQuery     Index der Archivanfrage
	 * @param archiveReference Von welchem Archiv kommt die Antwort
	 *
	 * @return Die Archivanfrage
	 */
	private Query getQuery(int indexOfQuery, SystemObject archiveReference) {

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfQuery, archiveReference);
		Query query = (Query)_requests.get(hashObject);

		return query;
	}

	/**
	 * Diese Methode entfernt eine Archivauftrag aus der Hashtable.
	 *
	 * @param key key, der zu einem Archivauftrag (delete, restore, save, Archivinformationsanfrage, Archivanfrage) gehört, der entfernt werden soll
	 */
	void removeRequest(Object key) {

		_requests.remove(key);
	}

	/**
	 * Diese Methode analisiert die Daten und ruft die passenden Methoden auf, die die Daten weiter reichen oder verarbeiten um sie dann weiter zu reichen. Falls
	 * ein Paket vom falschen Typ empfangen wird, wird eine entsprechende Exception geworfen.
	 *
	 * @param data Datensatz eines Archivs
	 */
	public void dataReceiver(Data data) {
		// Von welchem Typ ist das Paket
		int dataType = data.getUnscaledValue("nachrichtenTyp").intValue();
		_debug.fine("Ein StreamedArchiveRequester hat einen Datensatz empfangen, nachrichtenTyp: " + dataType);

		switch(dataType) {
			case 1: {
				// Das Paket ist vom Typ "Anfrage". Eigentlich sollte so ein Paket niemals ankommen
				throw new IllegalArgumentException("Ein StreamedArchiveRequester hat ein Paket mit dem Typ 'Anfrage' erhalten.");
			}
			case 2: {
				// Ein Paket vom Typ "AnfrageErgebnis". Dies ist ein Antwortpaket nach dem ein "Anfragepaket" verschickt wurde
				intitiateArchiveQueryResponse(data);
				break;
			}
			case 3: {
				// Ein Paket vom Typ "StreamDaten". Dies sind Nutzdaten, die für eine bestimmte Archivanfrage gedacht sind.
				archiveDataResponse(data);
				break;
			}
			case 4: {
				// Ein Paket vom Typ "StreamSteuerung" wurde empfangen. Diese Pakete enthalten für gewöhnlich
				// Ticktes, die für den StreamMultiplexer gedacht sind. Also sind diese für
				// für Archivanfragen gar nicht zu gebrauchen, da diese Tickets verschicken.
				throw new IllegalArgumentException(
						"Ein StreamedArchiveRequester hat ein Ticket empfangen. In einer Archivanfrage"
						+ "arbeitet aber ein StreamDemultiplexer und kein StreamMultiplexer."
				);
			}
			case 6: {
				requestInfoResponse(data);
				break;
			}
			case 8: {
				deletePersistentDataResponse(data);
				break;
			}
			case 10: {
				// Eine Archivantwort auf einen Speicherauftrag
				savePersistentDataResponse(data);
				break;
			}
			case 12: {
				// Eine Archivantwort auf den Auftrag Daten aus der Sicherung in den direkten Zugriff
				// des Archivsystems zu bringen
				restoreDataResponse(data);
				break;
			}
			case 14: {

				// Die Antwort Nummer 14 wird für 2 Auftragstypen benötigt.
				// Einmal als Antwort für einen Auftrag, der den Löschzeipunkt verschieben soll
				// und einmal als Antwort für einen Löschauftrag, der sich auf verschiedene
				// Zeitbereiche bezieht.

				// Für welche Archivanfrage ist das Paket
				final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

				// Welches Archiv hat die Daten verschickt
				final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

				// Der Schlüssel zu dem Objekt
				ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);

				if(_requests.get(hashObject) instanceof IncreaseDeleteTime) {
					increaseDeleteTimeResponse(data);
					break;
				}
				else {
					deleteDataResponse(data);
					break;
				}
			}
			case 16: {
				// Eine Archivantwort auf einen Auftrag zum Abgleich der Archivverwaltungsinformationen
				archiveFileSaverAlignmentResponse(data);
				break;
			}
			case 18: {
				requestDataResponse(data);
				break;
			}
			case 20: {
				requestDataResponse(data);
				break;
			}
			case 22: {
				requestNumQueriesResponse(data);
				break;
			}
		}
	}

	/**
	 * Das Archiv Antwortet auf die gestellt Archivanfrage. Somit muss die Archivanfrage, die auf diese Antwort wartet, benachrichtigt werden, dass das Archiv
	 * geantwortet hat.
	 *
	 * @param data Datensatz
	 */
	private void intitiateArchiveQueryResponse(Data data) {

		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		// Als erstes muss die Archivanfrage "geholt" werden
		Query query = getQuery(indexOfRequest, archiveReference);
		if(query == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Archivanfrage) vom Archivsystem empfangen", data);
			return;
		}

		assert query != null : "Index der Anfrage: " + indexOfRequest + " ;Objekt, das zu der Anfrage gehört: " + archiveReference;

		// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob die Anfrage geklappt,
		// kodiert.
		byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

		// Die Antwort wird an die entsprechende Anfrage weitergeleitet
		query.initiateArchiveResponse(queryResponse);
	}

	/**
	 * Diese Klasse wird von der Implementation des StreamDemultixplexerDirector benötigt. Es verschickt das Ticket an das Archiv. Das Archiv wird das Ticket an
	 * den StreamMultiplexer weiterreichen.
	 *
	 * @param archiveRequest Mit diesem Objekt kann die Archivanfrage eindeutig identifiziert werden
	 * @param ticket         Ein byte-Array, das kodiert das Ticket für den StreamMultiplexer der Senderapplikation enthält
	 *
	 * @throws DataNotSubscribedException   Es sollen Daten ohne Anmeldung auf die Daten verschickt werden
	 * @throws SendSubscriptionNotConfirmed Es liegt keine positive Sendesteuerung vom Datenverteiler für die zu versendenden Daten vor
	 */
	void sendTicketToArchive(ArchiveQueryID archiveRequest, byte[] ticket) throws DataNotSubscribedException, SendSubscriptionNotConfirmed, DataModelException {

		// Datensatz erzeugen und verschicken
		createArchivRequestResultData(archiveRequest, 4, ticket);
	}

	/**
	 * Diese Methode leitet die Nutzdaten, die für eine bestimmte Archivanfrage gedacht sind, weiter.
	 *
	 * @param data Nutzdaten, die vom Archiv geschickt wurden
	 */
	void archiveDataResponse(Data data) {

		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();
		// Von welchem Archiv sind die Daten
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		Query query = getQuery(indexOfRequest, archiveReference);
		if(query != null) {
			// Die Nutzdaten an die Archivanfrage weiter geben
			query.archiveDataResponse(data.getUnscaledArray("daten").getByteArray());
		}
		else {
			// Der Empfänger der Daten wollte diese Daten gar nicht mehr haben (er hat die Streams abgebrochen)
			// trotzdem kam noch ein Paket für ihn, dieses kann verworfen werden
			_debug.info(
					"Es kam ein Paket für eine Applikation, die dieses Paket nicht mehr haben wollte: Von: " + archiveReference.getNameOrPidOrId()
					+ " ,Index der Anfrage: " + indexOfRequest
			);
		}
	}

	/**
	 * Diese Methode erzeugt einen Datensatz, der zum Archiv geschickt werden kann. Die Attributgruppe und der Aspekt sind fest gesetzt. Die Attribute können über
	 * die Parameter festgelegt werden.
	 * <p>
	 *
	 * @param archiveRequest Ein Object, das den Index der Anfrage und eine Referenz auf die anfragende Applikation enthält
	 * @param messageType    Von welchem Typ ist das Paket
	 * @param dataArray      Hier können zusätzliche Daten codiert werden
	 *
	 * @throws DataNotSubscribedException   Senden von Datensätzen ohne entsprechende Sendeanmeldung
	 * @throws SendSubscriptionNotConfirmed Es liegt keine positive Sendesteuerung vom Datenverteiler für die zu versendenden Daten vor
	 * @throws IllegalStateException        Die Verbindng zum Archive wurde abgebrochen, gleichzeitig wird versucht Datenpakete an das Archiv zu verschicken
	 */
	void createArchivRequestResultData(ArchiveQueryID archiveRequest, int messageType, byte[] dataArray)
			throws DataNotSubscribedException, SendSubscriptionNotConfirmed, IllegalStateException, DataModelException {

		DataModel configuration = _connection.getDataModel();

		// Attributgruppe
		AttributeGroup atg = configuration.getAttributeGroup("atg.archivAnfrageSchnittstelle");
		// Aspekt, dies ist die Anfrage(request) einer Archivanfrage
		Aspect aspectTicket = configuration.getAspect("asp.anfrage");

		DataDescription dataDescription = new DataDescription(atg, aspectTicket);

		// Dieses Ticket wird physich über den DaV verschickt und auf der "Empfänger"seite ausgepackt
		// (der Empfänger ist das Archiv)
		Data requestData = _connection.createData(dataDescription.getAttributeGroup());

		// Der Index der Anfrage
		requestData.getUnscaledValue("anfrageIndex").set(archiveRequest.getIndexOfRequest());

		// Welche Applikation verschickt diesen Datensatz

		// requestData.getReferenceValue("absender").setSystemObject(archiveRequest.getObjectReference());
		requestData.getReferenceValue("absender").setSystemObject(_connection.getLocalApplicationObject());

		// von welchem Typ ist das Paket
		requestData.getUnscaledValue("nachrichtenTyp").set(messageType);

		// byte-Array kopieren, falls es mitgegeben wurde, TBD was ist , wenn das array leer ist, null geht nicht, gibt exception !
		if(dataArray != null) {
			// Es gibt ein Array

			requestData.getUnscaledArray("daten").set(dataArray); // Array in Daten einfügen
		}

		if(!requestData.isDefined()){
			throw new DataModelException("Das verwendete Datenmodell unterstützt diese Anfrage nicht. kb.systemModellGlobal aktualisieren.");
		}

		ResultData result = new ResultData(_archiveSystem, dataDescription, System.currentTimeMillis(), requestData);

		synchronized(_lockState) {
			if(_state != 0) {
				// Es gibt keinen Empfänger für den Datensatz, somit ist das Archiv nicht mehr zu erreichen.
				// Das kann normalerweise nicht passieren, der Benutzer muss benachrichtigt werden.
				connectionLost();
			}

			_connection.sendData(result);
			_debug.fine(
					"StreamedArchiveRequester verschickt einen Datensatz, ArchivNachrichtenTyp: " + messageType + " an: " + _archiveSystem.getNameOrPidOrId()
			);
		} // synchronized (_lockState)
	}

	/**
	 * Die Verbindung zum Archiv ist verloren gegangen. Diese Methode wird derzeit benutzt um einen Fehler auszugeben, sonst hat sie keinen nutzen. Es werden
	 * Listener benutzt um das fehlen der Verbindung zum Archiv anzuzeigen. Die Methode wird an allen Stellen aufgerufen, an denen das "wegbrechen" der Verbidnung
	 * festgestellt wird.
	 */
	private void connectionLost() {
		_debug.warning("Die Verbindung zum Archivsystem ist verloren gegangen: " + _archiveSystem.getNameOrPidOrId());
	}

	/**
	 * Diese Methode prüft, ob gesendet werden darf. Ist die Sendesteuerung negativ, wird eine Zeitspanne gewartet, ist die Sendesteuerung dann noch immer negativ,
	 * wird false zurückgeliefert.
	 *
	 * @param timeOut Zeitpunkt, bis zu dem gewartet wird, um eine Verbindung aufzubauen
	 *
	 * @return true = positive Sendesteuerung vorhanden; false = auch nach Ablauf der vorgegebenen Zeitspanne ist keine positive Sendesteuerung vorhanden
	 */
	private boolean isConnectionOk(Date timeOut) throws InterruptedException {
		synchronized(_lockState) {
			final TimeoutTimer timer = new TimeoutTimer(timeOut);
			// Solange die Sendesteuerung nicht auf positiv geändert wurde oder aber der Timer ist
			// abgelaufen, wird nichts gemacht.
			while(_state != ClientSenderInterface.START_SENDING && timer.isTimeExpired() == false) {
				_debug.fine(
						"Die Applikation wartet, bis das Archiv bereit ist Anfragen/Aufträge . Applikation: " + _connection.getLocalApplicationObject()
						+ " Archiv: " + _archiveSystem
				);
				// Falls "0" zurückgegeben wird, würde der Thread für immer schlafen
				_lockState.wait(timer.getRemainingTime() + 1);
			}

			if(_state == ClientSenderInterface.START_SENDING) {
				// Es darf nun gesendet werden
				_debug.finest(
						"Die Applikation kann Archivanfragen stellen . Applikation: " + _connection.getLocalApplicationObject() + " Archiv: " + _archiveSystem
				);
				return true;
			}
			else {
				_debug.info(
						"Die Applikation kann keine Archivanfragen stellen, da die Sendesteuerung weiterhin negativ ist . Applikation: "
						+ _connection.getLocalApplicationObject() + " Archiv: " + _archiveSystem
				);
				return false;
			}
		}
	}

	/** Diese Methode meldet den StreamedArchiveRequester als Sender von Archivanfragen/Tickets ab. */
	private void unsubscribeSender() {
		_debug.info("StreamedArchiveRequester meldet sich als Sender von Archivanfragen/Tickets ab");
		_connection.unsubscribeSender(_clientSender, _archiveSystem, _dataDescriptionSender);
	}

	/**
	 * Diese Methode meldet die Archivapplikation als Sender von Archivanfragen/Tickets/Archivaufgaben an. Diese Methode wird gewöhnlich nur im Konstruktor
	 * aufgerufen, kann im Konstruktor keine Verbidnung aufgebaut werden, wird bei jedem Methodenaufruf versucht eine Verbindung aufzubauen
	 */
	private void subscribeSender() {

	}

	/**
	 * Diese innerClass implementiert das ClientSenderInterface und wird im StreamedArchiveRequester benötigt um ein ClientDavConnection Objekt zu erzeugen. Über
	 * diese connection werden dann Tickets und Archivanfragen verschickt.
	 */
	final class ClientSender implements ClientSenderInterface {

		/**
		 * Diese Methode implementiert eine Methode des Interfaces ClientSenderInterface. Der Datenverteiler benutzt diese Methode, um eventuelle Änderungen
		 * anzuzeigen, die sich auf den Datensatz beziehen, den das StreamedArchiveRquester Objekt verschicken will (Tickets/Archivanfragen).
		 *
		 * @param object          Welches Objekt
		 * @param dataDescription Welche DataDescription
		 * @param state           Wie ist der neue Status
		 */
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			synchronized(_lockState) {
				_debug.info("StreamedArchiveRequester: state ändert sich von: " + _state + " auf: " + state);
				_gotDataRequest = true;
				if((_state == ClientSenderInterface.START_SENDING) && (state != ClientSenderInterface.START_SENDING)) {
					// Es durfte vorher gesendet werden, also ist gerade die Verbindung zum Archiv verloren gegangen
					_state = state;
					// Dieser Methodenaufruf erzeugt nur eine Textmeldung, kann aber für andere Dinge benutzt werden, falls
					// dies nötig sein sollte.
					connectionLost();
					// Änderung des Verbindungsstatus, alle Listener benachrichtigen
					adviseListener();
				}
				else {
					_state = state;
					adviseListener();
					// Es werden alle Thread benachrichtigt, die darauf warten Datensätze für diese Archivanfrage zu verschicken.
					_debug.fine("Neuer status der Query: " + _state);
					_lockState.notifyAll();
				}
			}
		}

		/**
		 * Diese Methode legt fest, ob die Applikation informiert werden will, wenn es eine Änderung bei den Empfängern der Datensätze gibt, die die Applikation
		 * versendet. Die Methode {@link #dataRequest} wird nicht aufgerufen, wenn "false" als Antwort geliefert wird.
		 *
		 * @param object          Welches Objekt ist betroffen
		 * @param dataDescription Welche DataDescription
		 *
		 * @return true = bei Änderungen wird die Applikation benachrichtigt, false = keine Benachrichtigung bei Änderungen
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode liefert ein Objekt zurück, über das asynchron auf das Ergebnis der
	 * Archivinformationsanfrage zugegriffen werden kann..
	 *
	 * @param spec Spezifikation der Archivdaten zu denen Information gewünscht werden.
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 */
	public ArchiveInfoQueryResult requestInfo(ArchiveDataSpecification spec) {
		if(spec == null) throw new IllegalArgumentException("Parameter spec ist null");
		final List<ArchiveDataSpecification> specs = new ArrayList<ArchiveDataSpecification>(1);
		specs.add(spec);
		return requestInfo(specs);
	}

	public void subscribeReceiver(
			DatasetReceiverInterface receiver,
			SystemObject object,
			DataDescription dataDescription,
			ReceiveOptions options,
			HistoryTypeParameter historyType,
			long history) {
		_subscriptionArchiveOnlineDataManager.subscribe(receiver, object, dataDescription, options, historyType, history);
	}

	public void unsubscribeReceiver(DatasetReceiverInterface receiver, SystemObject object, DataDescription dataDescription) {
		_subscriptionArchiveOnlineDataManager.unsubscribe(receiver, object, dataDescription);
	}

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode liefert ein Objekt zurück, über das asynchron auf das Ergebnis der
	 * Archivinformationsanfrage zugegriffen werden kann..
	 *
	 * @param specs Liste mit Spezifikationen der Archivdaten zu denen Information gewünscht werden
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 */
	public ArchiveInfoQueryResult requestInfo(List<ArchiveDataSpecification> specs) {
		if(specs == null) throw new IllegalArgumentException("Parameter specs ist null");

		try {
			if(isConnectionOk(_timeOutArchiveRequest) == false) {
				throw new IllegalStateException("Das Archivsystem " + _archiveSystem.getNameOrPidOrId() + " ist nicht erreichbar");
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		RequestInfo requestInfo = new RequestInfo(specs, archiveQueryID, this, _defaultSimulationVariant);

		_debug.finest(
				"Eine Archivanfrage (requestInfo) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ requestInfo.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + requestInfo.getArchiveRequestID().getObjectReference()
		);
		_requests.put(requestInfo.getArchiveRequestID(), requestInfo);
		// Anfrage verschicken
		requestInfo.sendRequestInfo();
		return requestInfo;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf eine Archiveinformationsanfrage vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf einen Archivinformationsanfrage
	 */
	private void requestInfoResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		RequestInfo requestInfo = (RequestInfo)_requests.get(hashObject);
		if(requestInfo == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Archivinformationsanfrage) vom Archivsystem empfangen", data);
			return;
		}
		requestInfo.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	@Override
	public ArchiveNumQueriesResult getNumArchiveQueries() {
		try {
			if(isConnectionOk(_timeOutArchiveRequest) == false) {
				throw new IllegalStateException("Das Archivsystem " + _archiveSystem.getNameOrPidOrId() + " ist nicht erreichbar");
			}
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		RequestNumQueries requestNumQueries = new RequestNumQueries(archiveQueryID, this, _defaultSimulationVariant);

		_debug.finest(
				"Eine Archivanfrage (requestNumQueries) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
						+ requestNumQueries.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + requestNumQueries.getArchiveRequestID()
						.getObjectReference()
		);
		_requests.put(requestNumQueries.getArchiveRequestID(), requestNumQueries);
		// Anfrage verschicken
		requestNumQueries.sendRequestInfo();
		return requestNumQueries;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf eine Archiveinformationsanfrage vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf einen Archivinformationsanfrage
	 */
	private void requestNumQueriesResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		RequestNumQueries requestNumQueries = (RequestNumQueries)_requests.get(hashObject);
		if(requestNumQueries == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Archivinformationsanfrage) vom Archivsystem empfangen", data);
			return;
		}
		requestNumQueries.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	public ArchiveQueryResult savePersistentData() {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}
		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		SaveData saveTask = new SaveData(archiveQueryID, this);

		_debug.finest(
				"Eine Archivanfrage (speichern) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ saveTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + saveTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(saveTask.getArchiveRequestID(), saveTask);
		// Sicherungsuaftrag übertragen
		saveTask.save();
		return saveTask;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf einen Speicherauftrag vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf Speicherauftrag
	 */
	private void savePersistentDataResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		Object request = _requests.get(hashObject);
		if(!(request instanceof SaveData)) {
			_debug.warning("Antwort (bzgl. einer Sicherungsanfrage) vom Archivsystem empfangen:\n" + data + "\n, gefundene aber unpassende Anfrage: " + request);
			return;
		}
		SaveData saveDataResult = (SaveData) request;
		if(saveDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Sicherungsanfrage) vom Archivsystem empfangen", data);
			return;
		}
		saveDataResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	public ArchiveQueryResult requestData(Collection<ArchiveInformationResult> requiredData, Collection<SystemObject> requestedArchives) {
		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}
		final ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);
		RequestData requestData = new RequestData(archiveQueryID, this, _defaultSimulationVariant);
		_requests.put(requestData.getArchiveRequestID(), requestData);
		requestData.request(requiredData, requestedArchives);
		return requestData;
	}

	public ArchiveQueryResult requestData(long startTime, long endTime, Collection<SystemObject> requestedArchives) {
		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}
		final ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);
		RequestData requestData = new RequestData(archiveQueryID, this, _defaultSimulationVariant);
		_requests.put(requestData.getArchiveRequestID(), requestData);
		requestData.request(startTime, endTime, requestedArchives);
		return requestData;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf einen Auftrag zum initierten Nachfordern von Daten vorliegt. Dabei wird nicht unterschieden, ob es eine
	 * Antwort auf ein Nachricht vom Typ 17 oder 19 ist.
	 *
	 * @param data Antwort auf den Auftrag zum nachfordern von Daten
	 */
	private void requestDataResponse(Data data) {
		// Für welchen Archivauftrag ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);

		RequestData requestDataResult = (RequestData)_requests.get(hashObject);
		if(requestDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. Nachfordern) vom Archivsystem empfangen", data);
			return;
		}
		requestDataResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	/**
	 * Diese Methode beauftragt das Archivsystem bestimmten Daten, die sich nicht im direkten Zugriff befinden, von der Sicherung wieder in den direkten Zugriff
	 * des Archivsystems zu bringen.
	 *
	 * @param requiredData Zeitbereiche, die wieder in den direkten Zugriff des Archivsystems gebracht werden sollen
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 */
	public ArchiveQueryResult restorePersistentData(List<ArchiveInformationResult> requiredData) {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}
		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		RestoreData restoreTask = new RestoreData(archiveQueryID, requiredData, this, _defaultSimulationVariant);

		_debug.finest(
				"Eine Archivanfrage (restore) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ restoreTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + restoreTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(restoreTask.getArchiveRequestID(), restoreTask);
		// Sicherungsuaftrag übertragen
		restoreTask.restore();
		return restoreTask;
	}

	private void restoreDataResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		RestoreData restoreDataResult = (RestoreData)_requests.get(hashObject);
		if(restoreDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Wiederherstellungsanfrage) vom Archivsystem empfangen", data);
			return;
		}
		restoreDataResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}


	/**
	 * Diese Methode beauftragt das Archivsystem alle Daten, die zu einer bestimmten Simulationsvariante gehören, zu löschen.
	 *
	 * @param simulationVariant Simulationsvariante von der alle Daten aus dem Archivsystem entfernt werden sollen
	 *
	 * @return Ergebnisobjekt über das asynchron auf die gewünschten Informationen zugegriffen werden kann.
	 */
	public ArchiveQueryResult deleteDataSimulationVariant(short simulationVariant) {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		DeleteSimulationVariant deleteTask = new DeleteSimulationVariant(simulationVariant, archiveQueryID, this);

		_debug.finest(
				"Eine Archivanfrage (löschen) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ deleteTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + deleteTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(deleteTask.getArchiveRequestID(), deleteTask);
		// Sicherungsuaftrag übertragen
		deleteTask.deleteSimulationVariant();
		return deleteTask;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf einen Löschauftrag vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf Speicherauftrag
	 */
	private void deletePersistentDataResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		DeleteSimulationVariant deleteDataResult = (DeleteSimulationVariant)_requests.get(hashObject);
		if(deleteDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines Löschauftrags für eine Simulationsvariante) vom Archivsystem empfangen", data);
			return;
		}
		deleteDataResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	public ArchiveQueryResult increaseDeleteTime(List<ArchiveInformationResult> requiredData, long timePeriod) {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		IncreaseDeleteTime setNewDeleteTime = new IncreaseDeleteTime(archiveQueryID, requiredData, timePeriod, this, _defaultSimulationVariant);

		_debug.finest(
				"Eine Archivanfrage (Löschzeitpunkt verschieben) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ setNewDeleteTime.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + setNewDeleteTime.getArchiveRequestID().getObjectReference()
		);
		_requests.put(setNewDeleteTime.getArchiveRequestID(), setNewDeleteTime);
		// Sicherungsuaftrag übertragen
		setNewDeleteTime.increaseDeleteTime();
		return setNewDeleteTime;
	}

	private void increaseDeleteTimeResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		IncreaseDeleteTime setDeleteTimeResult = (IncreaseDeleteTime)_requests.get(hashObject);
		if(setDeleteTimeResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines Löschzeitverlängerungsauftrag) vom Archivsystem empfangen", data);
			return;
		}
		setDeleteTimeResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}


	public ArchiveQueryResult deleteData(List<ArchiveInformationResult> dataDisposedToDelete, boolean deleteImmediately) {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		DeleteData deleteData = new DeleteData(archiveQueryID, dataDisposedToDelete, deleteImmediately, this, _defaultSimulationVariant);

		_debug.finest(
				"Eine Archivanfrage (Löschzeitpunkt verschieben) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ deleteData.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + deleteData.getArchiveRequestID().getObjectReference()
		);
		_requests.put(deleteData.getArchiveRequestID(), deleteData);
		// Sicherungsuaftrag übertragen
		deleteData.increaseDeleteTime();
		return deleteData;
	}

	private void deleteDataResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		DeleteData deleteDataResult = (DeleteData)_requests.get(hashObject);
		if(deleteDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines Löschauftrags) vom Archivsystem empfangen", data);
			return;
		}
		deleteDataResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	public ArchiveQueryResult archiveFileSaverAlignment(int volumeIdTypB) {

		final int indexOfRequest;
		synchronized(this) {
			indexOfRequest = _indexOfRequest;
			_indexOfRequest++;
		}

		ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

		ArchiveAlignment alignmentTask = new ArchiveAlignment(volumeIdTypB, archiveQueryID, this);

		_debug.finest(
				"Eine Archivanfrage (Archivverwaltungsinformationen anpassen) wird in einer Hashtable gespeichert. Schlüssel: IndexAnfrage: "
				+ alignmentTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + alignmentTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(alignmentTask.getArchiveRequestID(), alignmentTask);
		// Sicherungsuaftrag übertragen
		alignmentTask.archiveAlignment();
		return alignmentTask;
	}

	public boolean isArchiveAvailable() {
		synchronized(_lockState) {
			if(_state == ClientSenderInterface.START_SENDING) {
				return true;
			}
			else {
				// Falls es noch keine Sendesteuerung gibt, maximal 30 Sekunden darauf warten.
				if(!_gotDataRequest){
					try {
						_lockState.wait(30 * 1000);
					}
					catch(InterruptedException ignored) {
					}
					return _state == ClientSenderInterface.START_SENDING;
				}
				return false;
			}
		}
	}

	public void addArchiveAvailabilityListener(ArchiveAvailabilityListener listener) {
		synchronized(_listener) {
			_listener.add(listener);
		}
	}

	public void removeArchiveAvailabilityListener(ArchiveAvailabilityListener listener) {
		synchronized(_listener) {
			_listener.remove(listener);
		}
	}

	/** Diese Methode benachrichtigt alle Listener, dass sich der Verbindungszustand zum Archivsystem geändert hat. */
	private void adviseListener() {
		synchronized(_listener) {
			for(int nr = 0; nr < _listener.size(); nr++) {
				final ArchiveAvailabilityListener listener = _listener.get(nr);
				listener.archiveAvailabilityChanged(this);
			}
		}
	}

	private void archiveFileSaverAlignmentResponse(Data data) {
		// Für welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		ArchiveAlignment alignmentTaskResult = (ArchiveAlignment)_requests.get(hashObject);
		if(alignmentTaskResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines Auftrags zum Abgleich der Archivverwaltungsinformationen) vom Archivsystem empfangen", data);
			return;
		}
		alignmentTaskResult.archiveResponse(data);

		// Die Antwort wurde übermittelt, es wird für diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}
}
