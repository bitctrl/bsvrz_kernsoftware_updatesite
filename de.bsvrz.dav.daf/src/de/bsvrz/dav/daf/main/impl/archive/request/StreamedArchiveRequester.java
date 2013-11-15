/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.archive.request;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.archive.ArchiveAvailabilityListener;
import de.bsvrz.dav.daf.main.archive.ArchiveDataQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveDataStream;
import de.bsvrz.dav.daf.main.archive.ArchiveInfoQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryPriority;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestManager;
import de.bsvrz.dav.daf.main.archive.DatasetReceiverInterface;
import de.bsvrz.dav.daf.main.archive.HistoryTypeParameter;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.timeout.TimeoutTimer;

import java.util.*;

/**
 * Die Objekte dieser Klasse verwalten alle Arten von Anfragen an das Archivsystem, gleichzeitig werden auch alle Antworten vom Archivsystem, die f�r die
 * Applikation bestimmt sind, entgegen genommen und in entsprechende Objekte umgewandelt (der Datensatz wird von einem Objekt der Klasse {@link
 * StreamedRequestManager} empfangen, aber an den richtigen StreamedArchiveRequester weitergeleitet). F�r jede anfragende Applikation wird ein solches Objekt
 * erzeugt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8573 $
 */
public class StreamedArchiveRequester implements ArchiveRequestManager {

	/** Stellt die Verbindung zum Archiv dar. Hier werden Steuerungs/Nutzdaten angefragt und empfangen. */
	private final ClientDavInterface _connection;

	/** Diese Variable wird benutzt um zu pr�fen ob es einen Abnehmer f�r Archivanfragen gibt. Den Kommentar im Konstruktor beachten. */
	private byte _state = ClientSenderInterface.STOP_SENDING;

	/** Diese Objekt f�r <code>synchronized()</code> ben�tigt, es sperrt den Zugriff auf die Variable _state. */
	private final Byte _lockState = new Byte(_state);

	/** Jede Anfrage erh�lt einen eigene Index. Somit k�nnen Antworten des Archivs, die auch diesen Index enthalten, genau dem Empf�nger zugeordnet werden. */
	private int _indexOfRequest = 0;

	/**
	 * Hier sind alle Anfragen gespeichert, als Key wird der Index der Anfrage benutzt und das Archiv von dem die Daten angefordert werden. Zu jeder Anfrage geh�rt
	 * ein eindeutiger Index.
	 */
	private final Map _requests;

	/**
	 * Diese Variable bestimmt die Gr��e des Empfangspuffers (StreamDemultiplexer). Die Gr��e wird in Bytes angegben. Der Wert "0" ist der default Wert. Das
	 * bedeutet, dass das Archiv die Gr��e des Empfangspuffers festlegt. Der Defaultwert ist in der Konfiguration gespeichert und wird dort vom Archiv angefordert.
	 * Soll ein anderer Wert benutzt werden, so kann dieser mit {@link #setReceiveBufferSize} gesetzt werden.
	 */
	private int _receiveBufferSize = 0;

	/** F�r welches Archivsystem ist dieser Manager. */
	private final SystemObject _archiveSystem;

	private final Date _timeOutArchiveRequest;


	/** DebugLogger f�r Debug-Ausgaben */
	private static Debug _debug = Debug.getLogger();

	private ClientSender _clientSender;

	private DataDescription _dataDescriptionSender;

	/** Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt. Der Wert wird vom StreamedRequestManager gesetzt. */
	private final short _defaultSimulationVariant;

	private final SubscriptionArchiveOnlineDataManager _subscriptionArchiveOnlineDataManager;

	/** Speichert alle Listener, die benachrichtigt werden, wenn das Archivsystem nicht mehr �ber den DaV zu erreichen ist */
	private final List<ArchiveAvailabilityListener> _listener = new ArrayList<ArchiveAvailabilityListener>();

	private boolean _gotDataRequest;

	/**
	 * Dieser Konstruktor erzeugt ein StreamedArchiveRequester Objekt und meldet sich gleichzeitig auf der �bergebenen Verbindung als Sender f�r Anfragen an. Die
	 * Anfragen werden an das Archiv geschickt, das ebenfalls �bergeben wird. Die Anfragen enthalten einmal die Archivanfragen und als zweites Tickets, die dem
	 * Archiv erlauben weitere Datens�tze zu verschicken.
	 *
	 * @param archiveConnection Eine Verbindung auf der Datens�tze verschickt werden sollen
	 * @param archiveSystem     Das Archiv, f�r das die Datens�tze bestimmt sind
	 */
	public StreamedArchiveRequester(
			ClientDavInterface archiveConnection, int timeOutArchiveRequest, SystemObject archiveSystem, short defaultSimulationVariant) {
		_connection = archiveConnection;
		_archiveSystem = archiveSystem;
		_requests = Collections.synchronizedMap(new HashMap());

		// Legt den Zeitpunkt fest, bis zu dem maximal auf eine Verbidnung zum Archivsystem gewartet wird.
		// Wird dieser Zeitpunkt �berschritten, so liefert die Methode isConnectionOk immer false zur�ck, wenn
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

			// In diesem Aspekt stehen die Anfragen/Tickets der Empf�ngerapplikation, diese m�ssen gesendet werden
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

		// Objekt, das Anmeldungen auf Online+Archivdaten zul��t erzeugen
		_subscriptionArchiveOnlineDataManager = new SubscriptionArchiveOnlineDataManager(this, _connection);
	}

	/**
	 * Diese Methode stellt eine Anfrage an das Archiv. Da eine Liste mit Objekten, die angefragt werden sollen, zur Verf�gung gestellt wird, werden auch soviele
	 * Streams bereitgestellt, wie die Liste Eintr�ge hat.
	 *
	 * @param priority Mit welcher Priorit�t soll die Anfrage beantwortet werden (hohe, mittlere, niedrige Priorit�t)
	 * @param specs    Eine Liste von Objekten, die alle Informationen enthalten, die zur Bearbeitung der Archivanfrage n�tig sind. F�r jedes Objekt der Liste wird
	 *                 ein Stream bereitgestellt, der die geforderten Informationen enth�lt.Wird eine leere Liste �bergeben, wird das Objekt, das die Antwort auf
	 *                 diese Anfrage enth�lt, ein Array mit Streams zur�ckgegbene, das ebenfalls leer ist.
	 *
	 * @return Ein Objekt, das die M�glichkeit bietet zu pr�fen ob die Anfrage erfolgreich war oder ob die Anfrage fehlgeschlagen ist. War die Anfrage erfolgreich,
	 *         so kann das Objekt die Streams zur weiteren Bearbeitung weitergeben. War die Liste <code>specs</code> leer, ist das Array, das die Streams
	 *         darstellt, ebenfalls leer.
	 *
	 * @throws IllegalStateException Das Archiv, an das die Anfrage gestellt wurde, kann nicht erreicht werden, die Anfrage wird verworfen.
	 */
	public ArchiveDataQueryResult request(ArchiveQueryPriority priority, List<ArchiveDataSpecification> specs) throws IllegalStateException {

		if(specs != null && specs.size() > 0) {
			// Am Anfang wird gepr�ft, ob das Archivsystem verf�gbar ist, nur wenn eine Verbidnung besteht wird die
			// Anfrage �berhaupt bearbeitet
			try {
				if(isConnectionOk(_timeOutArchiveRequest) == false) {
					_debug.warning(
							"Die Applikation: " + _connection.getLocalApplicationObject().getNameOrPidOrId() + " will eine Archivanfrage beim Archivsystem: "
							+ _archiveSystem.getNameOrPidOrId() + " stellen, bekommt aber keine R�ckmeldung vom Archivsystem. Die Archivanfrage wird verworfen."
					);
					throw new IllegalStateException("Das Archivsystem " + _archiveSystem.getNameOrPidOrId() + " kann nicht erreicht werden");
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}

			// �ber diese ID ist jede Archivanfrage eindeutig zu identifizieren
			// Als Key wird ein laufender Index und das Archiv (f�r das die Anfrage ist) genommen
			final int indexOfRequest;
			synchronized(this) {
				indexOfRequest = _indexOfRequest;
				_indexOfRequest++;
			}

			ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

			Query archiveQuery = new Query(archiveQueryID, priority, specs, _receiveBufferSize, this, _defaultSimulationVariant);
			// Die Anfrage speichern, sobald Daten f�r diese Anfrage kommen, kann die Archivanfrage �ber ihren Index
			// identifiziert werden. Der Index wird mit der Nachricht versandt.
			_requests.put(archiveQuery.getArchiveRequestID(), archiveQuery);

			archiveQuery.initiateArchiveRequest();

			return archiveQuery;
		}
		else {
			if(specs == null) {
				// Es wurde <code>null</code> �bergeben
				throw new IllegalArgumentException("Die Liste, die eine Archivanfrage spezifiziert, war null");
			}
			else {
				// Die Liste enth�lt keine Elemente, also ein Array der Gr��e 0 zur�ckgeben
				ArchiveDataQueryResult queryResult = new ArchiveDataQueryResult() {
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
				return queryResult;
			}
		}
	}

	/**
	 * Diese Methode stellt eine Anfrage an das Archiv. Da nur ein Objekt angefragt wird, steht auch nur ein Stream zur Verf�gung, der Daten �ber das Objekt
	 * �bermittelt
	 *
	 * @param priority Mit welcher Priorit�t soll die Anfrage beantwortet werden (hohe, mittlere, niedrige Priorit�t)
	 * @param spec     Ein Objekt, das alle Informationen enth�lt, die zur Bearbeitung der Archivanfrage n�tig sind.
	 *
	 * @return Ein Objekt, das die M�glichekit bietet zu pr�fen ob die Anfrage erfolgreich war oder ob die Anfrage fehlgeschlagen ist. War die Anfrage erfolgreich,
	 *         so kann das Objekt die Streams zur weiteren Bearbeitung weitergeben.
	 *
	 * @throws IllegalStateException Das Archiv, an das die Anfrage gestellt wurde, kann nicht erreicht werden, die Anfrage wird verworfen.
	 */
	public ArchiveDataQueryResult request(ArchiveQueryPriority priority, ArchiveDataSpecification spec) throws IllegalStateException {

		if(spec != null) {
			try {
				if(isConnectionOk(_timeOutArchiveRequest) == false) {
					_debug.warning(
							"Die Applikation: " + _connection.getLocalApplicationObject().getNameOrPidOrId() + " will eine Archivanfrage beim Archivsystem: "
							+ _archiveSystem.getNameOrPidOrId() + " stellen, bekommt aber keine R�ckmeldung vom Archivsystem. Die Archivanfrage wird verworfen."
					);
					throw new IllegalStateException("Das Archivsystem " + _archiveSystem.getNameOrPidOrId() + " kann nicht erreicht werden");
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}

			// �ber diese ID ist jede Archivanfrage eindeutig zu identifizieren
			final int indexOfRequest;
			synchronized(this) {
				indexOfRequest = _indexOfRequest;
				_indexOfRequest++;
			}
			final ArchiveQueryID archiveQueryID = new ArchiveQueryID(indexOfRequest, _archiveSystem);

			Query archiveQuery = new Query(archiveQueryID, priority, spec, _receiveBufferSize, this, _defaultSimulationVariant);
			// Die Anfrage speichern, sobald Daten f�r diese Anfrage kommen, kann die Archivanfrage �ber ihren Index
			// identifiziert werden. Der Index wird mit der Nachricht versandt.
			_requests.put(archiveQuery.getArchiveRequestID(), archiveQuery);

			archiveQuery.initiateArchiveRequest();

			return archiveQuery;
		}
		else {
			throw new IllegalArgumentException("Die geforderte Spezifikation einer Archivanfrage war null");
		}
	}

	/**
	 * Die Puffergr��e (in Byte) des Empf�ngers auf einen anderen Wert als den default Wert setzen. Der default Wert wird vom Archiv aus der Konfiguration
	 * ermittelt.
	 *
	 * @param numberOfBytes
	 */
	public void setReceiveBufferSize(int numberOfBytes) {
		_receiveBufferSize = numberOfBytes;
	}

	/**
	 * Diese Methode liefert zu einem gegebenen Index, der zu einer Archivanfrage geh�rt, die dazugeh�rige Archivanfrage. Der Index der Archivanfrage steht in der
	 * Antwort einer Archivanfrage, somit kann die Antwort einer Archivanfrage zugeordnet werden. Die Anfrage wird �ber ein <code>int</code> identifiziert. Dieses
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
	 * @param key key, der zu einem Archivauftrag (delete, restore, save, Archivinformationsanfrage, Archivanfrage) geh�rt, der entfernt werden soll
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
				// Ein Paket vom Typ "StreamDaten". Dies sind Nutzdaten, die f�r eine bestimmte Archivanfrage gedacht sind.
				archiveDataResponse(data);
				break;
			}
			case 4: {
				// Ein Paket vom Typ "StreamSteuerung" wurde empfangen. Diese Pakete enthalten f�r gew�hnlich
				// Ticktes, die f�r den StreamMultiplexer gedacht sind. Also sind diese f�r
				// f�r Archivanfragen gar nicht zu gebrauchen, da diese Tickets verschicken.
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

				// Die Antwort Nummer 14 wird f�r 2 Auftragstypen ben�tigt.
				// Einmal als Antwort f�r einen Auftrag, der den L�schzeipunkt verschieben soll
				// und einmal als Antwort f�r einen L�schauftrag, der sich auf verschiedene
				// Zeitbereiche bezieht.

				// F�r welche Archivanfrage ist das Paket
				final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

				// Welches Archiv hat die Daten verschickt
				final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

				// Der Schl�ssel zu dem Objekt
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
		}
	}

	/**
	 * Das Archiv Antwortet auf die gestellt Archivanfrage. Somit muss die Archivanfrage, die auf diese Antwort wartet, benachrichtigt werden, dass das Archiv
	 * geantwortet hat.
	 *
	 * @param data Datensatz
	 */
	private void intitiateArchiveQueryResponse(Data data) {

		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		// Als erstes muss die Archivanfrage "geholt" werden
		Query query = getQuery(indexOfRequest, archiveReference);
		if(query == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Archivanfrage) vom Archivsystem empfangen", data);
			return;
		}

		assert query != null : "Index der Anfrage: " + indexOfRequest + " ;Objekt, das zu der Anfrage geh�rt: " + archiveReference;

		// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob die Anfrage geklappt,
		// kodiert.
		byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

		// Die Antwort wird an die entsprechende Anfrage weitergeleitet
		query.initiateArchiveResponse(queryResponse);
	}

	/**
	 * Diese Klasse wird von der Implementation des StreamDemultixplexerDirector ben�tigt. Es verschickt das Ticket an das Archiv. Das Archiv wird das Ticket an
	 * den StreamMultiplexer weiterreichen.
	 *
	 * @param archiveRequest Mit diesem Objekt kann die Archivanfrage eindeutig identifiziert werden
	 * @param ticket         Ein byte-Array, das kodiert das Ticket f�r den StreamMultiplexer der Senderapplikation enth�lt
	 *
	 * @throws DataNotSubscribedException   Es sollen Daten ohne Anmeldung auf die Daten verschickt werden
	 * @throws SendSubscriptionNotConfirmed Es liegt keine positive Sendesteuerung vom Datenverteiler f�r die zu versendenden Daten vor
	 */
	void sendTicketToArchive(ArchiveQueryID archiveRequest, byte[] ticket) throws DataNotSubscribedException, SendSubscriptionNotConfirmed {

		// Datensatz erzeugen und verschicken
		createArchivRequestResultData(archiveRequest, 4, ticket);
	}

	/**
	 * Diese Methode leitet die Nutzdaten, die f�r eine bestimmte Archivanfrage gedacht sind, weiter.
	 *
	 * @param data Nutzdaten, die vom Archiv geschickt wurden
	 */
	void archiveDataResponse(Data data) {

		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();
		// Von welchem Archiv sind die Daten
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		Query query = getQuery(indexOfRequest, archiveReference);
		if(query != null) {
			// Die Nutzdaten an die Archivanfrage weiter geben
			query.archiveDataResponse(data.getUnscaledArray("daten").getByteArray());
		}
		else {
			// Der Empf�nger der Daten wollte diese Daten gar nicht mehr haben (er hat die Streams abgebrochen)
			// trotzdem kam noch ein Paket f�r ihn, dieses kann verworfen werden
			_debug.info(
					"Es kam ein Paket f�r eine Applikation, die dieses Paket nicht mehr haben wollte: Von: " + archiveReference.getNameOrPidOrId()
					+ " ,Index der Anfrage: " + indexOfRequest
			);
		}
	}

	/**
	 * Diese Methode erzeugt einen Datensatz, der zum Archiv geschickt werden kann. Die Attributgruppe und der Aspekt sind fest gesetzt. Die Attribute k�nnen �ber
	 * die Parameter festgelegt werden.
	 * <p/>
	 *
	 * @param archiveRequest Ein Object, das den Index der Anfrage und eine Referenz auf die anfragende Applikation enth�lt
	 * @param messageType    Von welchem Typ ist das Paket
	 * @param dataArray      Hier k�nnen zus�tzliche Daten codiert werden
	 *
	 * @throws DataNotSubscribedException   Senden von Datens�tzen ohne entsprechende Sendeanmeldung
	 * @throws SendSubscriptionNotConfirmed Es liegt keine positive Sendesteuerung vom Datenverteiler f�r die zu versendenden Daten vor
	 * @throws IllegalStateException        Die Verbindng zum Archive wurde abgebrochen, gleichzeitig wird versucht Datenpakete an das Archiv zu verschicken
	 */
	void createArchivRequestResultData(ArchiveQueryID archiveRequest, int messageType, byte[] dataArray)
			throws DataNotSubscribedException, SendSubscriptionNotConfirmed, IllegalStateException {

		DataModel configuration = _connection.getDataModel();

		// Attributgruppe
		AttributeGroup atg = configuration.getAttributeGroup("atg.archivAnfrageSchnittstelle");
		// Aspekt, dies ist die Anfrage(request) einer Archivanfrage
		Aspect aspectTicket = configuration.getAspect("asp.anfrage");

		DataDescription dataDescription = new DataDescription(atg, aspectTicket);

		// Dieses Ticket wird physich �ber den DaV verschickt und auf der "Empf�nger"seite ausgepackt
		// (der Empf�nger ist das Archiv)
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

			// Die L�nge des Arrays setzen
			final int lengthDataArray = dataArray.length;

			requestData.getUnscaledArray("daten").setLength(lengthDataArray);
			// Byteweise kopieren

			for(int nr = 0; nr < lengthDataArray; nr++) {
				requestData.getUnscaledArray("daten").getValue(nr).set(dataArray[nr]);
			}
		}

		ResultData result = new ResultData(_archiveSystem, dataDescription, System.currentTimeMillis(), requestData);

		synchronized(_lockState) {
			if(_state != 0) {
				// Es gibt keinen Empf�nger f�r den Datensatz, somit ist das Archiv nicht mehr zu erreichen.
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
	 * Diese Methode pr�ft, ob gesendet werden darf. Ist die Sendesteuerung negativ, wird eine Zeitspanne gewartet, ist die Sendesteuerung dann noch immer negativ,
	 * wird false zur�ckgeliefert.
	 *
	 * @param timeOut Zeitpunkt, bis zu dem gewartet wird, um eine Verbindung aufzubauen
	 *
	 * @return true = positive Sendesteuerung vorhanden; false = auch nach Ablauf der vorgegebenen Zeitspanne ist keine positive Sendesteuerung vorhanden
	 */
	private boolean isConnectionOk(Date timeOut) throws InterruptedException {
		synchronized(_lockState) {
			final TimeoutTimer timer = new TimeoutTimer(timeOut);
			// Solange die Sendesteuerung nicht auf positiv ge�ndert wurde oder aber der Timer ist
			// abgelaufen, wird nichts gemacht.
			while(_state != ClientSenderInterface.START_SENDING && timer.isTimeExpired() == false) {
				_debug.fine(
						"Die Applikation wartet, bis das Archiv bereit ist Anfragen/Auftr�ge . Applikation: " + _connection.getLocalApplicationObject()
						+ " Archiv: " + _archiveSystem
				);
				// Falls "0" zur�ckgegeben wird, w�rde der Thread f�r immer schlafen
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
	 * Diese Methode meldet die Archivapplikation als Sender von Archivanfragen/Tickets/Archivaufgaben an. Diese Methode wird gew�hnlich nur im Konstruktor
	 * aufgerufen, kann im Konstruktor keine Verbidnung aufgebaut werden, wird bei jedem Methodenaufruf versucht eine Verbindung aufzubauen
	 */
	private void subscribeSender() {

	}

	/**
	 * Diese innerClass implementiert das ClientSenderInterface und wird im StreamedArchiveRequester ben�tigt um ein ClientDavConnection Objekt zu erzeugen. �ber
	 * diese connection werden dann Tickets und Archivanfragen verschickt.
	 */
	final class ClientSender implements ClientSenderInterface {

		/**
		 * Diese Methode implementiert eine Methode des Interfaces ClientSenderInterface. Der Datenverteiler benutzt diese Methode, um eventuelle �nderungen
		 * anzuzeigen, die sich auf den Datensatz beziehen, den das StreamedArchiveRquester Objekt verschicken will (Tickets/Archivanfragen).
		 *
		 * @param object          Welches Objekt
		 * @param dataDescription Welche DataDescription
		 * @param state           Wie ist der neue Status
		 */
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			synchronized(_lockState) {
				_debug.info("StreamedArchiveRequester: state �ndert sich von: " + _state + " auf: " + state);
				_gotDataRequest = true;
				if((_state == ClientSenderInterface.START_SENDING) && (state != ClientSenderInterface.START_SENDING)) {
					// Es durfte vorher gesendet werden, also ist gerade die Verbindung zum Archiv verloren gegangen
					_state = state;
					// Dieser Methodenaufruf erzeugt nur eine Textmeldung, kann aber f�r andere Dinge benutzt werden, falls
					// dies n�tig sein sollte.
					connectionLost();
					// �nderung des Verbindungsstatus, alle Listener benachrichtigen
					adviseListener();
				}
				else {
					_state = state;
					adviseListener();
					// Es werden alle Thread benachrichtigt, die darauf warten Datens�tze f�r diese Archivanfrage zu verschicken.
					_debug.fine("Neuer status der Query: " + _state);
					_lockState.notifyAll();
				}
			}
		}

		/**
		 * Diese Methode legt fest, ob die Applikation informiert werden will, wenn es eine �nderung bei den Empf�ngern der Datens�tze gibt, die die Applikation
		 * versendet. Die Methode {@link #dataRequest} wird nicht aufgerufen, wenn "false" als Antwort geliefert wird.
		 *
		 * @param object          Welches Objekt ist betroffen
		 * @param dataDescription Welche DataDescription
		 *
		 * @return true = bei �nderungen wird die Applikation benachrichtigt, false = keine Benachrichtigung bei �nderungen
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}

	/**
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode liefert ein Objekt zur�ck, �ber das asynchron auf das Ergebnis der
	 * Archivinformationsanfrage zugegriffen werden kann..
	 *
	 * @param spec Spezifikation der Archivdaten zu denen Information gew�nscht werden.
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
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
	 * Start einer Archivinformationsanfrage an das Archivsystem. Diese Methode liefert ein Objekt zur�ck, �ber das asynchron auf das Ergebnis der
	 * Archivinformationsanfrage zugegriffen werden kann..
	 *
	 * @param specs Liste mit Spezifikationen der Archivdaten zu denen Information gew�nscht werden
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
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
				"Eine Archivanfrage (requestInfo) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
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
		// F�r welche Archivanfrage ist das Paket
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

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
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
				"Eine Archivanfrage (speichern) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ saveTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + saveTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(saveTask.getArchiveRequestID(), saveTask);
		// Sicherungsuaftrag �bertragen
		saveTask.save();
		return saveTask;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf einen Speicherauftrag vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf Speicherauftrag
	 */
	private void savePersistentDataResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		SaveData saveDataResult = (SaveData)_requests.get(hashObject);
		if(saveDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. einer Sicherungsanfrage) vom Archivsystem empfangen", data);
			return;
		}
		saveDataResult.archiveResponse(data);

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
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
		// F�r welchen Archivauftrag ist das Paket
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

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}

	/**
	 * Diese Methode beauftragt das Archivsystem bestimmten Daten, die sich nicht im direkten Zugriff befinden, von der Sicherung wieder in den direkten Zugriff
	 * des Archivsystems zu bringen.
	 *
	 * @param requiredData Zeitbereiche, die wieder in den direkten Zugriff des Archivsystems gebracht werden sollen
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
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
				"Eine Archivanfrage (restore) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ restoreTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + restoreTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(restoreTask.getArchiveRequestID(), restoreTask);
		// Sicherungsuaftrag �bertragen
		restoreTask.restore();
		return restoreTask;
	}

	private void restoreDataResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
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

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}


	/**
	 * Diese Methode beauftragt das Archivsystem alle Daten, die zu einer bestimmten Simulationsvariante geh�ren, zu l�schen.
	 *
	 * @param simulationVariant Simulationsvariante von der alle Daten aus dem Archivsystem entfernt werden sollen
	 *
	 * @return Ergebnisobjekt �ber das asynchron auf die gew�nschten Informationen zugegriffen werden kann.
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
				"Eine Archivanfrage (l�schen) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ deleteTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + deleteTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(deleteTask.getArchiveRequestID(), deleteTask);
		// Sicherungsuaftrag �bertragen
		deleteTask.deleteSimulationVariant();
		return deleteTask;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort auf einen L�schauftrag vorliegt. Die Antwort wird an den Auftraggeber weitergeleitet.
	 *
	 * @param data Archivantwort auf Speicherauftrag
	 */
	private void deletePersistentDataResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		DeleteSimulationVariant deleteDataResult = (DeleteSimulationVariant)_requests.get(hashObject);
		if(deleteDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines L�schauftrags f�r eine Simulationsvariante) vom Archivsystem empfangen", data);
			return;
		}
		deleteDataResult.archiveResponse(data);

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
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
				"Eine Archivanfrage (L�schzeitpunkt verschieben) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ setNewDeleteTime.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + setNewDeleteTime.getArchiveRequestID().getObjectReference()
		);
		_requests.put(setNewDeleteTime.getArchiveRequestID(), setNewDeleteTime);
		// Sicherungsuaftrag �bertragen
		setNewDeleteTime.increaseDeleteTime();
		return setNewDeleteTime;
	}

	private void increaseDeleteTimeResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		IncreaseDeleteTime setDeleteTimeResult = (IncreaseDeleteTime)_requests.get(hashObject);
		if(setDeleteTimeResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines L�schzeitverl�ngerungsauftrag) vom Archivsystem empfangen", data);
			return;
		}
		setDeleteTimeResult.archiveResponse(data);

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
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
				"Eine Archivanfrage (L�schzeitpunkt verschieben) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ deleteData.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + deleteData.getArchiveRequestID().getObjectReference()
		);
		_requests.put(deleteData.getArchiveRequestID(), deleteData);
		// Sicherungsuaftrag �bertragen
		deleteData.increaseDeleteTime();
		return deleteData;
	}

	private void deleteDataResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
		final int indexOfRequest = data.getUnscaledValue("anfrageIndex").intValue();

		// Welches Archiv hat die Daten verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		ArchiveQueryID hashObject = new ArchiveQueryID(indexOfRequest, archiveReference);
		DeleteData deleteDataResult = (DeleteData)_requests.get(hashObject);
		if(deleteDataResult == null) {
			_debug.warning("Unerwartete Antwort (bzgl. eines L�schauftrags) vom Archivsystem empfangen", data);
			return;
		}
		deleteDataResult.archiveResponse(data);

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
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
				"Eine Archivanfrage (Archivverwaltungsinformationen anpassen) wird in einer Hashtable gespeichert. Schl�ssel: IndexAnfrage: "
				+ alignmentTask.getArchiveRequestID().getIndexOfRequest() + " Archiv: " + alignmentTask.getArchiveRequestID().getObjectReference()
		);
		_requests.put(alignmentTask.getArchiveRequestID(), alignmentTask);
		// Sicherungsuaftrag �bertragen
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

	/** Diese Methode benachrichtigt alle Listener, dass sich der Verbindungszustand zum Archivsystem ge�ndert hat. */
	private void adviseListener() {
		synchronized(_listener) {
			for(int nr = 0; nr < _listener.size(); nr++) {
				final ArchiveAvailabilityListener listener = _listener.get(nr);
				listener.archiveAvailabilityChanged(this);
			}
		}
	}

	private void archiveFileSaverAlignmentResponse(Data data) {
		// F�r welche Archivanfrage ist das Paket
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

		// Die Antwort wurde �bermittelt, es wird f�r diesen Auftrag keine Antwort mehr kommen.
		// Den Auftrag entfernen
		removeRequest(hashObject);
	}
}
