/*
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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.Dataset;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.archive.ArchiveData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveDataStream;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryPriority;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestManager;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.DatasetReceiverInterface;
import de.bsvrz.dav.daf.main.archive.HistoryTypeParameter;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Diese Klasse stellt ein Objekt zur Verfügung, das Archidaten und Onlinedaten mischt. Dafür meldet es sich als
 * Empfänger auf vorgegebene Daten an und fordert dann aus dem Archivsystem ebenfalls Daten an um diese zu mischen.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SubscriptionArchiveOnlineDataManager {

	/**
	 * Speichert alle Objekte, die als Empfänger angemeldet sind
	 */
	private final Map<ReceiverKey, Receiver> _receiverList = new HashMap<ReceiverKey, Receiver>();

	/**
	 * Objekt, über das Archivanfragen gestellt werden können. Wird benötigt um Archivdaten vor den Onlinestrom zu mischen
	 */
	private final ArchiveRequestManager _archive;

	/**
	 * Wird zum an/abmelden der Datenidentifikationen benötigt
	 */
	private final ClientDavInterface _connection;

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Erzeugt ein Objekt, das es ermöglicht einen Empfänger anzumelden. Vor die Onlinedatensätze werden Archidatensätze
	 * gemischt.
	 *
	 * @param archive    Archiv, das die Archivdaten zur Verfügung stellt
	 * @param connection Verbindung zum DaV
	 */
	public SubscriptionArchiveOnlineDataManager(ArchiveRequestManager archive, ClientDavInterface connection) {
		_archive = archive;
		_connection = connection;
	}

	/**
	 * Meldet sich als Empfänger von Datensätzen an und stellt über das Objekt <code>receiver</code> die Daten
	 * (Archivdaten+Online) zur Verfügung.
	 *
	 * @param receiver
	 * @param object
	 * @param dataDescription
	 * @param options
	 * @param historyType
	 * @param history
	 * @throws de.bsvrz.dav.daf.main.config.ConfigurationException
	 */
	public void subscribe(
			DatasetReceiverInterface receiver,
						  SystemObject object,
						  DataDescription dataDescription,
						  ReceiveOptions options,
						  HistoryTypeParameter historyType,
						  long history) throws ConfigurationException {

		// Objekt erzeugen, das Onlinedaten entgegen nimmt und die Archivdaten vor die Onlinedaten mischt.
		


		final Receiver newReceiver = new Receiver(_archive, _connection, receiver, object, dataDescription, options, historyType, history);
		// damit das Receiverobjekt in die Map eingefügt werden kann
		final ReceiverKey key = new ReceiverKey(receiver, object, dataDescription);

		// Anmelden
		_connection.subscribeReceiver(newReceiver, object, dataDescription, options, ReceiverRole.receiver());

		// In einer Map ablegen, damit der Empfänger sich wieder abmelden kann
		synchronized (_receiverList) {
			_receiverList.put(key, newReceiver);
		}
	}

	public void unsubscribe(DatasetReceiverInterface receiver, SystemObject object, DataDescription dataDescription) throws ConfigurationException {
		final ReceiverKey key = new ReceiverKey(receiver, object, dataDescription);

		synchronized (_receiverList) {
			final Receiver abortReceiver = _receiverList.remove(key);
			_connection.unsubscribeReceiver(abortReceiver, object, dataDescription);
			abortReceiver.unsubscribeReceiver();
		}
	}

	/**
	 * Objekt, das Onlinedaten puffert oder durchreicht, es stellt ebenfalls den ersten Online verfügbaren Datensatz zur
	 * Verfügung. Es enthält alle Informationen, die nötig sind, um ein anderes Objekt Archivanfragen stellen zu lassen. Es
	 * stellt ein Synchronisationsobjekt zur Verfügung, auf dem sich ein anderes Objekt synchronisieren kann.
	 */
	private final class Receiver implements ClientReceiverInterface {

		/**
		 * Objekt, über das Archivanfragen gestellt werden können. Wird benötigt um Archivdaten vor den Onlinestrom zu
		 * mischen
		 */
		private final ArchiveRequestManager _archive;

		/**
		 * TBD wird benötigt, wenn es nur begrentzt viele Thread geben soll
		 * Wird zum an/abmelden der Datenidentifikationen benötigt
		 */
		private final ClientDavInterface _connection;

		/**
		 * Puffert online Daten während Archivdaten angefordert werden. Der Puffer enthält auch den ersten Datensatz, der zum
		 * rausfinden des Endzeitpunkts der Archivanfrage benötigt wird.
		 */
		private final List<ResultData[]> _onlineBuffer = new ArrayList<ResultData[]>();

		/**
		 * Empfänger, der die gemischten Archin/Onlinedaten haben möchte.
		 */
		private final DatasetReceiverInterface _receiverArchiveOnlineStream;

		/**
		 * Wird benötigt um zu entscheiden ob die Archidaten delta oder normal sein sollen
		 */
		private final ReceiveOptions _receiveOptions;

		/**
		 * bezieht sich der Wert in der Variablen _history auf einen Zeitbereich oder einen Datenindex
		 */
		private final HistoryTypeParameter _historyType;

		/**
		 * Zeitbereich oder Index, der aus dem aus Archivsystem angefordert werden soll. Diese Variable wird im laufe der
		 * Archivanfrage angepaßt. Wenn 10 Archivdaten benötigt werden, aber es werden nur 5 empfangen, müssen in der nächsten
		 * Archivanfrage nur noch 5 Archivdaten angefordert werden. Das selbe passiert mit einer Anfrage, die sich auf Zeit
		 * bezieht, wenn 4 Stunden benötigt werden, es werden aber nur 3 geliefert, wird die nächste Archivanfrage auf 1
		 * Stunde relativ gestellt. Kommentar Konstruktor beachten.
		 */
		private long _history;

		/**
		 * Objekt, auf das synchronisiert wird sobald Daten empfangen werden. Synchronisiert sich jemand anderes auf das
		 * Objekt, können solange keine Daten mehr empfangen werden (Zeit um den Puffer zu leeren).
		 */
		private final Object _dataReceived = new Object();

		/**
		 * Wurde der erste online Datensatz empfangen
		 */
		private boolean _receivedFirstOnlineDataSet = false;

		/**
		 * Können die online empfangenen Daten direkt durchgereicht werden? Das ist möglich, wenn die Archivdaten alle
		 * übertragen wurden und der Puffer, der die online Daten speichert, leer ist.
		 */
		private boolean _handOverOnline = false;

		/**
		 * Speichert den ersten online Datensatz, dieser wird gebraucht, um den Endzeitpunkt der Archivanfrage zu bestimmen
		 */
		private ResultData _firstOnlineDataSet;

		/**
		 * Wird für die Archivanfrage gebraucht
		 */
		private final SystemObject _systemObject;

		/**
		 * Wird für die Archivanfrage gebraucht
		 */
		private final DataDescription _dataDescription;

		/**
		 * Diese Variable wird true, wenn der Sender abgemeldet wurde. Die Variable verhindert dann, das weitere Daten an den
		 * Empfänger verschickt werden.
		 */
		private boolean _unsubcribeReceiver = false;

		/**
		 * Thread, der die Archivdaten anfordert und vor die Onlinedaten mischt
		 */
		private final ArchiveDataRequester _archiveThread;

		public Receiver(ArchiveRequestManager archive,
						ClientDavInterface connection,
						DatasetReceiverInterface receiverArchiveOnlineStream,
						SystemObject systemObject,
						DataDescription dataDescription,
						ReceiveOptions receiveOptions,
						HistoryTypeParameter historyType,
						long history) {
			_archive = archive;
			_connection = connection;
			_receiverArchiveOnlineStream = receiverArchiveOnlineStream;
			_systemObject = systemObject;
			_dataDescription = dataDescription;
			_receiveOptions = receiveOptions;
			_historyType = historyType;

			// Wenn XX Datensätze vor dem aktuellen Datensatz gefordert werden muss +1 gerechnet werden,
			// weil der letzte Archivdatensatz gleich dem Onlinedatensatz ist.
			// Wenn 5 Archivdatensätze gefordert werden würden, würden 5 vom Archivsystem zurückgegeben,
			// aber es würden nur 4 der update-Methode übergeben, der fünfte wäre der erste Onlinedatensatz.
			// also wird ein Datensatz "mehr" angefordert, weil der letzte Archivdatensatz und der Onlinedatensatz
			// identisch sind.
			if (_historyType == HistoryTypeParameter.INDEX) {
				_history = history + 1;
			} else {
				_history = history;
			}
			// Thread mischt Archivdaten unter den Onlinestrom und verschickt diese
			_archiveThread = new ArchiveDataRequester(this, _archive, _systemObject, _dataDescription, _receiveOptions, _historyType, _history);
			Thread thread = new Thread(_archiveThread, "HistoryThread");
			thread.start();
		}

		public void update(ResultData results[]) {
			// Für Debugausgaben
			final String format = "dd.MM.yyyy HH:mm:ss,SSS";
			final DateFormat timeFormat = new SimpleDateFormat(format);
			// Sobald der erste Datensatz empfangen wird, wird dieser Zeitstempel als
			// Endzeitpunkt für die Archivanfrage benutzt. Onlinedaten, die zwischendurch empfangen werden, werden
			// gepuffert.
			// Wurden alle Archivdaten vor den Onlinestrom gemisch, werden die Datensätze nur noch durchgereicht.
			synchronized (_dataReceived) {
				if (_handOverOnline) {
					// Es wurden alle Archivdaten verschickt, der Puffer mit den zwischengespeicherten Online Datensätzen
					// wurde auch verschickt, also können die gerade empfangenden Daten durchgreicht werden.
					sendData(results);
				} else {
					// Wurde der erste Datensatz schon empfangen ?
					if (_receivedFirstOnlineDataSet) {
						// Der erste Datensatz wurde schon empfangen, also müssen diese Daten
						// gepuffert werden
						_onlineBuffer.add(results);
					} else {
						// Der erste Datensatz wurde noch nicht empfangen, also befindet er sich unter
						// den gerade empfangenen Daten. Dieser wird gespeichert und alle Datensätze
						//, mit dem ersten, werden in den Puffer abgelegt.

						// Alles in den Puffer
						_onlineBuffer.add(results);

						if (results[0].getData() != null) {
							// Der erste Onlinedatensatz steht zur Verfügung
							_firstOnlineDataSet = results[0];
							_receivedFirstOnlineDataSet = true;

							_debug.finest("Datenzeit erster Onlinedatensatz: " + timeFormat.format(new Date(_firstOnlineDataSet.getDataTime())));
							// System.out.println("**** Datenzeit erster Onlinedatensatz: " + timeFormat.format(new Date(_firstOnlineDataSet.getDataTime())));

							// Falls jemand auf den ersten Datensatz wartet, wird er benachrichtigt
							_dataReceived.notifyAll();

						} else {
							
							_debug.info("Der erste Datensatz war null, der dafür benutzt werden sollte um den Andzeitpunkt der Archivanfrage zu bestimmen. Es wird auf den nächsten Datensatz gewartet");
							return;
						}
					}
				}
			} // synch
		}

		/**
		 * Diese Methode wird aufgerufen, wenn der Empfänger abgemeldet wird. Weitere Datensätze, die zu dem Empfänger
		 * geschickt werden sollen, werden nicht weitergereicht. Falls der Thread, der Archivdaten anfordert, noch Archivdaten
		 * verschicken will, werden diese ebenfalls nicht durchgereicht.
		 */
		public void unsubscribeReceiver() {
			// Der Zugriff ist nicht synchronisiert, weil der Empfänger abgemeldet wurde und es somit völlig egal
			// ist, wer derzeit Daten senden dürfte, es sollen keine Daten mehr an den Empfänger durchgereicht werden.
			_unsubcribeReceiver = true;
			// Falls noch Archivdaten unterwegs sind, werden diese jetzt abgebrochen
			_archiveThread.cancelThread();
		}

		private Object getDataReceived() {
			return _dataReceived;
		}

		private boolean isReceivedFirstOnlineDataSet() {
			return _receivedFirstOnlineDataSet;
		}

		private ResultData getFirstOnlineDataSet() {
			return _firstOnlineDataSet;
		}

		/**
		 * Verschickt Datensätze an den Vorgegebenen Empfänger, falls dieser die Verbindung noch nicht abgemeldet hat. Wurde
		 * die Verbindung abgemeldet, dann werden die Daten verworfen.
		 *
		 * @param datasetResult Archivdaten oder ResultData, die verschickt werden sollen
		 */
		public void sendData(Dataset[] datasetResult) {
			if (_unsubcribeReceiver == false) {
				// Es dürfen noch Daten verschickt werden, falls nicht, werden die Daten einfach verworfen
				_receiverArchiveOnlineStream.update(datasetResult);
			}
		}

		/**
		 * Diese Methode wird aufgerufen, sobald alle Archivdaten vor den Onlinestrom gemischt wurden. Der Puffer, der die
		 * Onlinedaten zwischengespeichert hat, wird dann übertragen. Die Methode sperrt die update-Methode und setzt danach
		 * eine Variable, so dass alle folgenden Onlinedaten direkt durchgereicht werden, ohne gepuffert zu werden.
		 */
		private void sendOnlineBuffer() {
			synchronized (_dataReceived) {
//				System.out.println("*** Puffer leeren *** " + _onlineBuffer.size());
				for (int nr = 0; nr < _onlineBuffer.size(); nr++) {
					// Die Datensätze in der Reihenfolge ihres eintreffens verschicken
					final ResultData bufferdOnlineData[] = _onlineBuffer.get(nr);
					sendData(bufferdOnlineData);
				}
				// Die folgenden update-Aufrufe reichen die Onlinedaten direkt durch, ohne diese
				// zwischenzuspeichern
				_handOverOnline = true;
			}
		}
	}

	/**
	 * Klasse, die Archivdaten anfordert und diese vor einen Onlinedatensatz einmischt
	 */
	private final static class ArchiveDataRequester implements Runnable {

		/**
		 * Enthält alle Daten und Methoden, die der Thread braucht um Archivdaten anzufodern und diese vor die aktuellen Daten
		 * zu mischen
		 */
		private final Receiver _receiver;

		/**
		 * Erster Onlinedatensatz, dieser Datensatz dient als Endzeitpunkt für die Archivanfrage
		 */
		private ResultData _firstOnlineDataSet;

		/**
		 * Speichert den letzten Datensatz einer Archivanfrage. Dieser wird benötigt, wenn bei einer Archivanfrage nicht alle
		 * benötigten Werte übertragen wurden. Es fehlen zum Beispiel die letzten drei Datensätze, weil diese gerade
		 * archiviert werden und das Archivsystem sie deshalb nicht mitgeschickt hat. Um an alle Daten zu kommen, würde erneut
		 * angefragt werden, ein paar Archivdatensätze dieser Anfrage könnte aber verworfen werden, weil sie bereits in der
		 * ersten Anfrage vorhanden waren. Damit diese Datensätze erkannt werden können, wird der letzte Datensatz
		 * gespeichert.
		 */
		private ArchiveData _lastReceivedDataSet = null;

		/**
		 * Bezieht sich der Wert in der Variablen _history auf einen Zeitbereich oder einen Datenindex.
		 */
		private final HistoryTypeParameter _historyType;

		/**
		 * Zeitbereich oder Index, der aus dem aus Archivsystem angefordert werden soll. Diese Variable wird im laufe der
		 * Archivanfrage angepaßt. Wenn 10 Archivdaten benötigt werden, aber es werden nur 5 empfangen, müssen in der nächsten
		 * Archivanfrage nur noch 5 Archivdaten angefordert werden. Das selbe passiert mit einer Anfrage, die sich auf Zeit
		 * bezieht, wenn 4 Stunden benötigt werden, es werden aber nur 3 geliefert, wird die nächste Archivanfrage auf 1
		 * Stunde relativ gestellt.
		 */
		private long _history;

		/**
		 * Wird benötigt um zu entscheiden ob die Archidaten delta oder normal sein sollen
		 */
		private final ReceiveOptions _receiveOptions;
		/**
		 * Wird für die Archivanfrage gebraucht
		 */
		private final SystemObject _systemObject;
		/**
		 * Wird für die Archivanfrage gebraucht
		 */
		private final DataDescription _dataDescription;
		/**
		 * Archivsystem, an das die Anfrage gestellt werden soll
		 */
		private final ArchiveRequestManager _archive;

		/**
		 * Wurden schon einmal Archivdaten verschickt. Wird nur EOA vom Archiv empfangen und es wurden schon einmal
		 * Archivdaten verschickt, dann wird auf Daten gewartet. Wird hingegen EOA empfangen und es wurden noch nie
		 * Archivdaten verschickt, hat das Archiv keine Daten für diese Datenidentifikation.
		 */
		private boolean _archiveDataSend = false;

		/**
		 * true = der Thread stellt seine Arbeit ein und meldet sich beim Archiv ab.
		 */
		private boolean _cancel = false;

		public ArchiveDataRequester(Receiver receiver,
									ArchiveRequestManager archive,
									SystemObject systemObject,
									DataDescription dataDescription,
									ReceiveOptions receiveOptions,
									HistoryTypeParameter historyType,
									long history) {
			_receiver = receiver;
			_archive = archive;
			_systemObject = systemObject;
			_dataDescription = dataDescription;
			_historyType = historyType;
			_history = history;
			_receiveOptions = receiveOptions;
		}

		public void run() {
			// warten, bis Onlinedaten zur Verfügung stehen
			Object dataReceivedLockObject = _receiver.getDataReceived();
			synchronized (dataReceivedLockObject) {
				while (_receiver.isReceivedFirstOnlineDataSet() == false) {
					// Es steht noch kein Datensatz zur Verfügung, also kann der Thread schlafen gelegt werden.
					// Wird er geweckt und der erste Datensatz steht zur Verfügung, kann er die Archivdaten anfordern
					try {
						dataReceivedLockObject.wait();
					} catch (InterruptedException e) {
					}
				}

				// Der erste Datensatz steht zur Verfügung
				_firstOnlineDataSet = _receiver.getFirstOnlineDataSet();
				// Der Thread hat nun alle Daten die er braucht um arbeiten zu können. Der Synchblock kann verlassen werden
			}

			// Der erste Onlinedatensatz wurde gefunden, also können jetzt die Archivdaten angefordert werden

			// Wenn das Archiv keine Daten mehr zur Verfügung stelle kann, die vor den Onlinestrom geschrieben werden
			// können, wird diese Variable true.
			// Es müssen nicht zwangsläufig Daten empfangen worden sein, wenn es keine Archivdaten gibt, wird diese
			// Variable ebenfalls true.
			boolean allPossibleArchiveDataReceived = false;

			while (allPossibleArchiveDataReceived == false) {
				// Es müssen zwei Fälle unterschieden werden.
				// Fall 1, "Es wurden noch keine Archivdaten angefordert": Bei XXX Datensätze vor
				// dem aktuellen Datensatz wird der Index benutzt, bei XXX ms vor dem aktuellen Datensatz wird der Zeitpunkt berechnet.
				// Fall 2, "Es wurden bereits Archivdaten angefordert": Der letzte empfangende Datensatz des Archivs ist bekannt
				// (_lastReceivedDataSet) und kann als Startindex benutzt werden
				final ArchiveTimeSpecification archiveTimeSpecification;

				if (_lastReceivedDataSet != null) {
					// Der letzte Datensatz einer Archivanfrage ist bekannt.
					// Dieser Fall tritt ein, wenn bereits eine Archivanfrage gestellt wurde, aber nicht alle
					// Datensätze aus dem Archiv übertragen wurden. Dann wird erneut angefragt, diesmal wird
					// aber eine Archivanfrage auf den Datenindex gestellt. Als Anfangswert wird der Datenindex
					// des _lastReceivedDataSet benutzt, als Endindex wird der Datenindex des _firstOnlineDataSet
					// benutzt.
					archiveTimeSpecification = new ArchiveTimeSpecification(TimingType.DATA_INDEX, false, _lastReceivedDataSet.getDataIndex(), _firstOnlineDataSet.getDataIndex());

				} else if (_historyType == HistoryTypeParameter.TIME) {
					final long archiveDataEndTime = _firstOnlineDataSet.getDataTime();
					final long archiveDataStartTime = archiveDataEndTime - _history;
					// Es sollen _history viele ms vor dem ersten Onlinedatensatz zurückgegeben werden
					archiveTimeSpecification = new ArchiveTimeSpecification(TimingType.DATA_TIME, false, archiveDataStartTime, archiveDataEndTime);
				} else {
					// Es werden mindestens _history viele Datensätze vorher angefordert. Als Endwert wird der Datenindex des ersten
					// Onlinedatensatzes benutzt.
					archiveTimeSpecification = new ArchiveTimeSpecification(TimingType.DATA_INDEX, true, _history, _firstOnlineDataSet.getDataIndex());
				}

				ArchiveDataKindCombination archiveDataKindCombination = new ArchiveDataKindCombination(ArchiveDataKind.ONLINE, ArchiveDataKind.ONLINE_DELAYED, ArchiveDataKind.REQUESTED, ArchiveDataKind.REQUESTED_DELAYED);

				final ArchiveRequestOption archiveRequestOption;
				if (_receiveOptions == ReceiveOptions.normal()) {
					archiveRequestOption = ArchiveRequestOption.NORMAL;
				} else {
					archiveRequestOption = ArchiveRequestOption.DELTA;
				}

				final ArchiveDataSpecification archiveDataSpecification =
						new ArchiveDataSpecification(archiveTimeSpecification,
								archiveDataKindCombination,
								ArchiveOrder.BY_DATA_TIME,
								archiveRequestOption,
								_dataDescription,
								_systemObject);

//				System.out.println("archiveDataSpecification = " + archiveDataSpecification);

				ArchiveDataQueryResult archiveResponse = null;
				try {
					archiveResponse = _archive.request(ArchiveQueryPriority.MEDIUM, archiveDataSpecification);
				} catch (IllegalStateException e) {
					_debug.warning("Anmeldung mit Historie gibt nur den Onlinestrom zurück: ", e);
					// Es gibt keine Verbindung zum Archiv, also wird nur der Onlinestrom zurückgegeben
					break;
				}

				try {
					if (archiveResponse.isRequestSuccessful()) {
						// Es stehen Archivdaten zur Verfügung
						ArchiveDataStream currentStream = archiveResponse.getStreams()[0];

						// Speichert alle Archivdaten, die mit Aufruf der update-Methode des Empfänger übergeben werden
						final List<ArchiveData> archiveDataList = new LinkedList<ArchiveData>();


						ArchiveData currentArchiveData = currentStream.take();

						if ((_lastReceivedDataSet == null) && (currentArchiveData.getDataType() != DataState.END_OF_ARCHIVE)) {
							// Das Objekt wurde noch nicht gesetzt, also ist dies die erste Archivantwort auf eine Anfrage.
							// Es steht kein Index zur Verfügung, also wird vom ersten Onlineaktuellen Datensatz
							// entweder die oder Datenindex abgezogen um zu prüfen ob der Archivdatensatz übertragen werden
							// kann.
							if (_historyType == HistoryTypeParameter.TIME) {
								// Ist der erste Datensatz nun ein Kandidat für Archivdaten, die übertragen werden müssen,
								// oder stellt er nur den Datensatz dar, der anzeigt in welchem Bereich der folgende
								// Datensatz gültig ist ?
								// Beispiel: Es wurden die Daten 11:59, 12:01 gespeichert, bei einer Archivanfrage auf 12:00
								// (Der Benutzer ruft um 15:00 subscribeReceiver auf, mit history = 3 Stunden)
								// würde der Datensatz 11:59 übertragen, er darf aber nicht übertragen werden, sondern nur der 12:01 Datensatz

								if (currentArchiveData.getDataTime() < (_firstOnlineDataSet.getDataTime() - _history)) {
									// Die Datenzeit des ersten Datensatzes liegt ausserhalb der gewünschten Zeit.
									// Dieser Datensatz darf nicht in die Liste aufgenommen werden, aber er kann als
									// Datensatz für _lastReceivedDataSet benutzt werden
									_lastReceivedDataSet = currentArchiveData;

									// Nun den nächsten Datensatz anfordern, dieser wird innerhalb des gewünschten
									// Zeitrahmens liegen (oder ist nicht vorhanden)
									currentArchiveData = currentStream.take();
								} else if ((currentArchiveData.getDataTime() >= (_firstOnlineDataSet.getDataTime() - _history))
										&& (currentArchiveData.getDataTime() <= _firstOnlineDataSet.getDataTime())) {
									// Die Datenzeit des ersten Datensatzes ist größer gleich dem Startbereich,
									// in dem die Daten liegen sollen. Ist aber kleiner gleich dem Ende des Startbereichs
									// für die benötigten Archivdaten.
									// Dieser Datensatz muss in die Liste der zu übertragenen Archivdatensätze
									// aufgenommen werden, danach dient er als _lastReceivedDataSet
									archiveDataList.add(currentArchiveData);
									_lastReceivedDataSet = currentArchiveData;

									// Den nächsten Datensatz anfordern
									currentArchiveData = currentStream.take();
								} else {
									// Die Datenzeit des ersten Archivdatensatzes liegt außerhalb des gewünschten
									// Bereichs (oberhalb). Dieser Fall kann nicht aufreten.
									


								}

							} else {
								// Der erste Datensatz bei einer relativen Anfrage mit XXX Datensätze davor kann immer
								// weggelassen werden, da er den Zustand vor dem gewünschten Datensatz wiederspiegelt.
								// Also kann dieser Archivdatensatz auch als _lastReceivedDataSet benutzt werden.
								_lastReceivedDataSet = currentArchiveData;

								// Den nächsten Datensatz anfordern, dieser gehört in die Liste mit benötigen
								// Archivdatensätzen, die zurückgegeben werden müssen
								currentArchiveData = currentStream.take();
							}
						}

						


						// Liste mit Archivdaten füllen, die übertragen werden sollen, der EAO Datensatz wird nicht mehr gespeichert
						while ((currentArchiveData.getDataType() != DataState.END_OF_ARCHIVE) && (_cancel == false)) {
							// Der Datenindex des Datensatzes aus der Archivantwort muss immer größer sein, als der der in _lastReceivedDataSet
							// gespeichert wurde. Somit werden automatisch doppelte Datensätze rausgefilter, die in einer zweiten Archivantwort
							// doppelt übertragen werden

							if (currentArchiveData.getDataIndex() > _lastReceivedDataSet.getDataIndex()) {
								// Der Datensatz wurde noch nicht übertragen
								archiveDataList.add(currentArchiveData);
							}
							// Den nächsten Archivdatensatz anfordern
							currentArchiveData = currentStream.take();

							







						} // while, eine Archivantwort auswerten

						// Der Thread soll abgebrochen werden
						if (_cancel == true) {
							if (currentArchiveData.getDataType() != DataState.END_OF_ARCHIVE) {
								// Der Thread soll beendet werden aber das Archiv hat noch Daten.
								// Also den Stream abbrechen. Der else-Fall bedeutet, dass der Thread beendet werden soll
								// aber eh schon der EOA Datensatz gesendet wurde, also wurde der Stream schon beendet
								currentStream.abort();
							}

							// Raus aus der while-Schleife, die Archivanfragen stellt
							break;
						}

						// Es wurden alle Archivdaten, ausser dem EOA ausgelesen. War der letzte Archivdatensatz gleich
						// der erste Onlinedatensatz, der empfangen wurde.
						// Wenn ja, dann sind alle Archivdaten vorhanden, die vor die Onlinedaten gemischt werden müssen.
						// Wenn nicht, dann müssen erneut Archivdaten angefordert werden, dies geschieht, in dem die while-Schleife
						// erneut durchlaufen wird. Es kann auch passieren, dass das Archivsystem einen alten Archivdatensatz
						// verschickt + EOA, dies geschieht, wenn das Archivsystem einfach noch keine neuen Daten besitzt, auch
						// dann wird durch erneutes durchlaufen der while-Schleife Archivdaten angefordert.

						// Ein Sonderfall tritt ein, wenn das Archivsystem gar keine Archivdaten geschickt hat, sondern nur
						// EOA. Dann wurden zu dieser Datenidentifikation keine Daten archiviert (es kann sogar sein, dass zu
						// dieser Datenidentifikation nicht einmal Daten gespeichert werden).

						if (archiveDataList.size() > 0) {
							// Es wurde mindestens ein Archivdatensatz gefunden, der verschickt werden kann

							// letzter Datensatz, der verschickt werden soll (der EOA steht nicht in dieser Liste)
							final ArchiveData lastArchiveDataSet = archiveDataList.get(archiveDataList.size() - 1);

							if (lastArchiveDataSet.getDataIndex() == _firstOnlineDataSet.getDataIndex()) {
								// Der Datenindex stimmt überein, also sind alle Archivdaten vorhanden.
								// Der letzte Archivdatensatz in der Liste wird gelöscht, da dieser
								// ebenfalls als Onlinedatensatz vorhanden ist
								archiveDataList.remove(archiveDataList.size() - 1);

								// die Variable _archiveDataSend = true wird nicht gesetzt, da die while-Schleife verlassen wird
								// und somit keine neue Archivanfrage gestellt werden muss

								allPossibleArchiveDataReceived = true;

								final ArchiveData archiveDataArray[] = (ArchiveData[]) archiveDataList.toArray(new ArchiveData[archiveDataList.size()]);
								// Archivdaten verschicken, als nächstes muss der Puffer der Onlinedaten übertragen werden
								_receiver.sendData(archiveDataArray);
							} else {
								// Es wurden brauchbare Archivdaten empfangen, aber es fehle noch welche
								_archiveDataSend = true;

								// Es können alle Daten übertragen werden, aber es fehlen noch welche
								final ArchiveData archiveDataArray[] = (ArchiveData[]) archiveDataList.toArray(new ArchiveData[archiveDataList.size()]);
								_lastReceivedDataSet = archiveDataArray[archiveDataArray.length - 1];
								_receiver.sendData(archiveDataArray);

								// Jetzt wird die while-Schleife erneut durchlaufen, aber diesmal steht ein "letzter Datensatz"
								// aus einer Archivanfrage zur Verfügung, dieser wird benutzt um die neue Archivanfrage anzupassen
							}
						} else {
							if (!_archiveDataSend) {
								// Es wurde vom Archiv nur ein EOA Datensatz verschickt, also hat das Archivsystem keine Daten.
								// Die gepufferten Onlinedaten verschicken
								allPossibleArchiveDataReceived = true;

								// Der Else-Zweig dieser IF-Abrage würde bedeuten:
								// Es wurden schon einmal Archivdaten empfangen diesmal nur bekannter Datensatz + EAO, also noch einmal fragen, es fehlen noch
								// Daten und das geschieht von alleine.
							}
						}

					} else {
						// Die Archivanfrage konnte nicht beantwortet werden, also gibt es keine Archivdaten, der Onlinestrom
						// wird übergeben
						allPossibleArchiveDataReceived = true;
					}
				} catch (InterruptedException e) {
					
					_debug.warning("Fehler bei der Archivanfrage, es wird nur der Onlinestrom durchgereicht, da es Aufgrund des Fehlers keine Archivdaten gibt: ", e);
					// Nur die Onlinedaten übertagen
					break;
				} catch (IOException e) {
					e.printStackTrace();
					_debug.warning("Unbekannter IO-Fehler, es werden nur der Onlinestrom angezeigt", e);
					break;
				}
			} // while, die die Archivdaten überträgt

			// Es wurden alle Archivdaten übertragen oder die Übertragung wurde abgebrochen

			if (_cancel == false) {
				// Es wurden alle Daten übertragen und nicht abgebrochen4, also ganz normal weiter
				// Diese Synchronisation blockiert die update-Methode, also kann der Puffer für Onlinedatensätze
				// übertragen werden. Falls ein Thread in der update-Methode blockiert, wird dieser sobald er
				// nicht mehr blockiert ist, die Datensätze direkt durchschleusen ohne sie in den Puffer zu schreiben.
				synchronized (dataReceivedLockObject) {
					// Den Onlinepuffer übertragen
					_receiver.sendOnlineBuffer();
				}
			}
		}
		// Alle Archivdaten übertragen, Onlinepuffer übertragen, der Thread kann sich beenden

		/**
		 * Wird aufgerufen, wenn der Thread gestoppt werden soll, weil die Archivdaten nicht mehr gebraucht werden
		 */
		public void cancelThread() {
			_cancel = true;
		}
	}

	/**
	 * Stellt ein Objekt für eine HashMap zur Verfügung
	 */
	private final static class ReceiverKey {
		private final DatasetReceiverInterface _receiver;
		private final SystemObject _systemObject;
		private final DataDescription _dataDescription;

		public ReceiverKey(DatasetReceiverInterface receiver, SystemObject systemObject, DataDescription dataDescription) {
			_receiver = receiver;
			_systemObject = systemObject;
			_dataDescription = dataDescription;
		}

		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof ReceiverKey)) return false;

			final ReceiverKey otherKey = (ReceiverKey) o;

			return _receiver == otherKey.getReceiver() && _systemObject == otherKey.getSystemObject() && _dataDescription == otherKey.getDataDescription();
		}

		public int hashCode() {
			int result = 13;
			result = 23 * result + _receiver.hashCode();
			result = 23 * result + _systemObject.hashCode();
			result = 23 * result + _dataDescription.hashCode();
			return result;
		}

		public DatasetReceiverInterface getReceiver() {
			return _receiver;
		}

		public SystemObject getSystemObject() {
			return _systemObject;
		}

		public DataDescription getDataDescription() {
			return _dataDescription;
		}


		public String toString() {
			return "ReceiverKey{" +
					"_receiver=" + _receiver +
					", _systemObject=" + _systemObject +
					", _dataDescription=" + _dataDescription +
					"}";
		}
	}
}
