/*
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

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveDataStream;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryPriority;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.communicationStreams.StreamDemultiplexer;
import de.bsvrz.sys.funclib.communicationStreams.StreamDemultiplexerDirector;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Diese Klasse stellt alle Methoden zur Verf�gung um eine Archivanfrage zu stellen, sie wird der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 6346 $ / $Date: 2009-02-14 19:07:41 +0100 (Sat, 14 Feb 2009) $ / ($Author: rs $)
 */
class Query implements ArchiveDataQueryResult {

	StreamedArchiveRequester _streamedArchiveRequester;

	/** Dieses Objekt identifiziert die Archivanfrage eindeutig. */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Hier werden alle streams, die zu einer Archivanfrage geh�ren, gespeichert. Das Array wird mit null initialisiert, es bekommt erst dann die Streams, sobald
	 * das Archiv bereit ist Nutzdaten zu verschicken.
	 */
	private DataStream _arrayOfStreams[] = null;

	private final ArchiveQueryPriority _priority;

	/** ArchiveDataSpecification einer Anfrage in einer Liste speichern */
	private final List _spec;

	private StreamDemultiplexer _streamDemultiplexer;

	/** Wieviele Streams geh�ren zu der Archivanfrage. Jedes Objekt der Anfrage besitzt einen Stream. */
	private final int _numberOfStreams;

	/**
	 * Diese Variable speichert, wie viele Streams bisher beendet wurden, entweder durch empfang des null-Pakets oder durch abort. Ist der Wert der Variablen
	 * gleich _numberOfStreams kann die Verbindung zum Archiv beendet werden.
	 */
	private int _numberOfFinishedStreams = 0;

	/**
	 * Diese Variable bestimmt die Gr��e des Empfangspuffers (StreamDemultiplexer). Die Gr��e wird in Bytes angegben. Der Wert "0" ist der default Wert. Das
	 * bedeutet, dass das Archiv die Gr��e des Empfangspuffers festlegt. Der Defaultwert ist in der Konfiguration gespeichert und wird dort vom Archiv angefordert.
	 * Soll ein anderer Wert benutzt werden, so kann dieser mit {@link StreamedArchiveRequester#setReceiveBufferSize} gesetzt werden, f�r diese Anfrage ist der
	 * Wert allerdings konstant.
	 */
	private final int _receiveBufferSize;


	/** Konnte die Anfrage zum Archiv durchgef�hrt werden */
	private boolean _requestSuccessful;

	/** Falls es zu einem Fehler gekommen ist, zu welchem. */
	private String _errorMessage = "";

	/**
	 * Solange diese Variable true ist, werden alle Anfragen an die Query blockiert da das Archiv noch nicht geantwortet hat. Erst wenn das Archiv zum ersten mal
	 * eine Nachricht zu der Anfrage geschickt hat, darf weiter gearbeitet werden.
	 */
	private boolean _blocking = true;

	/** Bestimmt den blockingFactor des StreamDemultiplexers. Dieser wird vom Archiv gesetzt (ist in der Antwort des Archivs, auf die Archivanfrage, enthalten) */
	private int _blockingFactor;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	private final short _defaultSimulationVariant;

	/**
	 * Archivanfrage mit einem Objekt
	 *
	 * @param archiveRequestID         eindeutige Identifizierung der Anfrage, diese wird ben�tigt um Archivantworten an das Objekt weiterzuleiten
	 * @param priority                 Priorit�t der Anfrage
	 * @param spec                     Archivanfrage
	 * @param receiveBufferSize        Gr��e des Empfangspuffers (in Byte)
	 * @param streamedArchiveRequester Objekt �ber das Archivanfragen verschickt werden k�nnen
	 * @param defaultSimulationVariant Falls keine Simulationsvariante gesetzt wurde wird dieser Wert als default benutzt.
	 */
	public Query(
			ArchiveQueryID archiveRequestID,
			ArchiveQueryPriority priority,
			ArchiveDataSpecification spec,
			int receiveBufferSize,
			StreamedArchiveRequester streamedArchiveRequester,
			short defaultSimulationVariant
	) {

		_archiveRequestID = archiveRequestID;
		_streamedArchiveRequester = streamedArchiveRequester;
		_priority = priority;
		_receiveBufferSize = receiveBufferSize;
		_defaultSimulationVariant = defaultSimulationVariant;

		_spec = new LinkedList();
		_spec.add(spec);

		// Es wird nur ein Objekt angefragt, somit gibt es nur einen Stream
		_numberOfStreams = 1;
	}

	/**
	 * @param archiveRequestID         eindeutige Identifizierung der Anfrage, diese wird ben�tigt um Archivantworten an das Objekt weiterzuleiten
	 * @param priority                 Priorit�t der Anfrage
	 * @param spec                     Archivanfrage
	 * @param receiveBufferSize        Gr��e des Empfangspuffers (in Byte)
	 * @param streamedArchiveRequester Objekt �ber das Archivanfragen verschickt werden k�nnen
	 * @param defaultSimulationVariant Falls keine Simulationsvariante gesetzt wurde wird dieser Wert als default benutzt.
	 */
	public Query(
			ArchiveQueryID archiveRequestID,
			ArchiveQueryPriority priority,
			List spec,
			int receiveBufferSize,
			StreamedArchiveRequester streamedArchiveRequester,
			short defaultSimulationVariant
	) {

		_archiveRequestID = archiveRequestID;
		_receiveBufferSize = receiveBufferSize;

		_streamedArchiveRequester = streamedArchiveRequester;
		_priority = priority;
		_spec = spec;
		_defaultSimulationVariant = defaultSimulationVariant;

		// Es werden mehrere Objekte angefragt
		_numberOfStreams = spec.size();
	}

	/** Mit dieser Methode wird die initiale Archivanfrage verschickt. */
	public void initiateArchiveRequest() {
		// Beim Archiv Archivanfrage stellen. Das Archiv muss auf die erste Nachricht erst Antworten, solange wird
		// jede Methode dieser Klasse blockieren
		try {
			// Alle Parameter m�ssen in einen Datensatz verpackt werden.
			// Die Priorit�t und die "spec" werden dabei in einem byte-Array des Datensatzes �bertragen.
			// Die Daten werden mit einem Serializer verpackt und entsprechend auf der Empfangsseite mit einem
			// Deserializer entpackt. Damit die Daten mit der richtigen Version des Deserializers ausgepackt werden,
			// wird diese mitgeschickt. Die ersten 4 Bytes des byte-Arrays sind dabei die Versionsnummer
			// des Serializers und die entsprechende Version muss der Deserializer benutzen.
			// Die ersten 4 Bytes werden "von Hand" ein/ausgepackt, ohne den Serializer/Deserializer zu benutzen.
			// (somit ist die Versionsnummer immer unabh�ngig verf�gbar, egal welche Version des Serializer/Deserializer
			// die Applikation zur Verf�gung hat)

			// Ein byte-Array erzeugen, in dem <code>priority</code> und <code>spec</code> gespeichert sind.

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Serializer serializer = SerializingFactory.createSerializer(out);

			try {

				// Das erste int spiegelt die Priorit�t der Anfrage wieder
				serializer.writeInt(_priority.getCode());

				// Als n�chstes wird der receiveBufferSize gespeichert, dies ist ein int
				serializer.writeInt(_receiveBufferSize);

				// Anzahl Eintr�ge der Liste speichern, beim auspacken ist dann bekannt, wie viele Eintr�ge
				// entpackt werden m�ssen.
				serializer.writeInt(_spec.size());

				// Jede Anfrage, die in der spec-Liste steht, in das byte-Array speichern
				for(int i = 0; i < _spec.size(); i++) {

					ArchiveDataSpecification archiveDataSpecification = (ArchiveDataSpecification)_spec.get(i);

					{
						// ArchiveTimeSpecification anfordern und einzeln speichern
						ArchiveTimeSpecification archiveTimeSpecification = archiveDataSpecification.getTimeSpec();

						// TimingType als Byte speichern, dieser String kann beim entkodieren
						// benutzt werden, um das alte Objekt wieder zu erzeugen.
						// 1 = DATA_TIME
						// 2 = ARCHIVE_TIME
						// 3 = DATA_INDEX

						TimingType timingType = archiveTimeSpecification.getTimingType();

						if(timingType == TimingType.DATA_TIME) {
							serializer.writeByte(1);
						}
						else if(timingType == TimingType.ARCHIVE_TIME) {
							serializer.writeByte(2);
						}
						else if(timingType == TimingType.DATA_INDEX) {
							serializer.writeByte(3);
						}

						// boolean startRelative speichern. 0 = false, 1 = true, wird als Byte gespeichert
						if(archiveTimeSpecification.isStartRelative() == true) {
							serializer.writeByte(1);
						}
						else {
							serializer.writeByte(0);
						}

						// long intervalStart speichern
						serializer.writeLong(archiveTimeSpecification.getIntervalStart());

						// long intervalEnd speichern
						serializer.writeLong(archiveTimeSpecification.getIntervalEnd());
					}

					// ArchiveDataKindCombination speichern
					ArchiveDataKindCombination archiveDataKindCombination = archiveDataSpecification.getDataKinds();

					{
						// Jede boolean Variable wird als Byte gespeichert (0 = false; 1 = true)

						if(archiveDataKindCombination.isOnline() == true) {
							serializer.writeByte(1);
						}
						else {
							serializer.writeByte(0);
						}

						if(archiveDataKindCombination.isOnlineDelayed() == true) {
							serializer.writeByte(1);
						}
						else {
							serializer.writeByte(0);
						}

						if(archiveDataKindCombination.isRequested() == true) {
							serializer.writeByte(1);
						}
						else {
							serializer.writeByte(0);
						}

						if(archiveDataKindCombination.isRequestedDelayed() == true) {
							serializer.writeByte(1);
						}
						else {
							serializer.writeByte(0);
						}
					}

					// ArchiveOrder speichern
					ArchiveOrder archiveOrder = archiveDataSpecification.getSortOrder();

					{
						// Der int Wert kann benutzt werden, um das Objekt wieder herzustellen
						serializer.writeInt(archiveOrder.getCode());
					}

					// ArchiveRequestOption speichern
					ArchiveRequestOption archiveRequestOption = archiveDataSpecification.getRequestOption();

					{
						// Der int Wert kann benutzt werden, um das Objekt wieder herzustellen
						serializer.writeInt(archiveRequestOption.getCode());
					}

					// DataDescription speichern
					DataDescription dataDescription = archiveDataSpecification.getDataDescription();

					{
						if(dataDescription.getAttributeGroup() == null || dataDescription.getAspect() == null) {
							throw new IllegalArgumentException("Ein Element der Datenidentifikation ist null. " + dataDescription);
						}
						// Attributgruppe speichern
						serializer.writeObjectReference(dataDescription.getAttributeGroup());

						// Aspekt speichern
						serializer.writeObjectReference(dataDescription.getAspect());

						// short simulationVariant speichern.
						// Dabei tritt ein Sonderfall auf, wenn die Simulationsvariante auf
						// <code>NO_SIMULATION_VARIANT_SET</code> , dies entspricht einer
						// <code>-1</code>, gesetzt wurde. In diesem Fall muss der default Wert
						// f�r die Simulationsvariante gew�hlt werden. Der default-Wert wurde im Konstruktor dieser
						// Klasse �bergeben.
						if(dataDescription.getSimulationVariant() != -1) {
							serializer.writeShort(dataDescription.getSimulationVariant());
						}
						else {
							// Die Simulationsvariante wurde auf -1 gesetzt, somit muss der default-Wert benutzt werden.
							serializer.writeShort(_defaultSimulationVariant);
						}
					}

					// SystemObject speichern
					SystemObject systemObject = archiveDataSpecification.getObject();

					{
						// SystemObject speichern
						serializer.writeObjectReference(systemObject);
					}
				}
			}
			catch(Exception e) {
				throw new IllegalStateException("Fehler beim verschicken der initialen Archivanfrage: " + e);
			}

			// byte-Array erzeugen
			final byte[] data = out.toByteArray();

			// Die Versionsnummer des Serializer speichern
			final int serializerVersion = serializer.getVersion();

			// Ein neues byte-Array anlegen, dieses ist 4 Bytes gr��er als das alte, da die Version des Serializer
			// gespeichert werden muss
			final byte[] dataAndVersion = new byte[data.length + 4];

			// Das h�herw�rtigste Byte steht in Zelle 0
			dataAndVersion[0] = (byte)((serializerVersion & 0xff000000) >>> 24);
			dataAndVersion[1] = (byte)((serializerVersion & 0x00ff0000) >>> 16);
			dataAndVersion[2] = (byte)((serializerVersion & 0x0000ff00) >>> 8);
			dataAndVersion[3] = (byte)(serializerVersion & 0x000000ff);

			// Das data-Array hinter die Versionsnummer kopieren
			System.arraycopy(data, 0, dataAndVersion, 4, data.length);

			// Datensatz erzeugen und verschicken
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 1, dataAndVersion);
		}
		catch(DataNotSubscribedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		catch(ConfigurationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * Diese Methode blockiert solange, bis das Archiv auf die erste Archivanfrage geantwortet hat. In der Archivantwort wird dann gekl�rt, ob das Archiv die
	 * geforderten Nutzdaten zur Verf�gung stellen kann (falls nicht, warum nicht).
	 *
	 * @return true, falls das Archiv die Anfrage bearbeiten kann. false = das Archiv kann die Anfrage nicht bearbeiten, somit steht eine errorMessage zur
	 *         Verf�gung.
	 */
	public boolean isRequestSuccessful() throws InterruptedException {

		// Diese Frage darf erst beantwortet werden, wenn diese Archivanfrage auch vom Archiv beantwortet wurde.
		// Also legen wir diese Anfrage solange schlafen, bis diese Antwort kommt.
		// Die Antwort empf�ngt der StreamedArchiveRequester, dieser kann auch wieder Zugriff auf dieses Objekt
		// bekommen und somit alle wartenden aufwecken (also auch diesen Thread, der auf dieses Ereignis wartet).

		synchronized(this) {
			while(_blocking) {
				this.wait();
			}

			return _requestSuccessful;
		} // synchronized (this)
	}

	public String getErrorMessage() throws InterruptedException {

		// Dieser Methodenaufruf l��t die Anfrage nach den Streams solange warten, bis
		// es zu einer Antwort des Archivs gekommen ist, dass die angeforderten Nutzdaten
		// vorhanden sind oder das ein Fehler aufgetreten ist.
		if(isRequestSuccessful() == true) {
			return "Die Archivanfrage (" + _archiveRequestID.getIndexOfRequest() + ") war erfolgreich";
		}
		else {
			return _errorMessage;
		}
	}

	/**
	 * Diese Methode gibt alle Streams, die zu einer Archivanfrage geh�ren, zur�ck. Die geforderten Archivdaten liegen in den Stream bereit und k�nnen mit der
	 * entsprechenden Methode angefordert werden.
	 *
	 * @return Alle Streams, die zu einer Archivanfrage geh�ren
	 *
	 * @throws IllegalStateException Die Archivanfrage konnte nicht vom Archiv bearbeitet werden, somit durfte diese Methode nicht aufgerufen werden
	 */
	public ArchiveDataStream[] getStreams() throws IllegalStateException, InterruptedException {

		// Dieser Methodenaufruf l��t die Anfrage nach den Streams solange warten, bis
		// es zu einer Antwort des Archivs gekommen ist. Entweder sind die angeforderten Nutzdaten
		// vorhanden oder es ist ein Fehler aufgetreten ist.

		if(isRequestSuccessful() == true) {
			return _arrayOfStreams;
		}
		else {
			throw new IllegalStateException("Es kam zu einem Fehler bei der Archivanfrage(" + _archiveRequestID.getIndexOfRequest() + "): " + _errorMessage);
		}
	}

	/**
	 * Diese Methode wird vom StreamedArchivRequester aufgerufen, sobald das Archiv auf die erste Archivanfrage eines Auftrags antwortet. Diese Antwort bedeutet,
	 * dass die Anfrage bearbeitet werden kann oder das ein Fehler aufgetreten ist.
	 *
	 * @param queryResponse Die Antwort des Archivs, die Informationen im byte-Array codiert
	 */
	public void initiateArchiveResponse(byte[] queryResponse) {

		synchronized(this) {

			try {
				InputStream in = new ByteArrayInputStream(queryResponse);

				//deserialisieren
				Deserializer deserializer = SerializingFactory.createDeserializer(in);

				// Eine 0 bedeutet, dass die Archivanfrage nicht bearbeitet werden kann, bei einer 1 ist alles in Ordnung
				final byte requestSuccessfulFlag = deserializer.readByte();

				final boolean requestSuccessful;
				if(requestSuccessfulFlag == 0) {
					requestSuccessful = false;
				}
				else {
					requestSuccessful = true;
				}

				_debug.finer("erste Archivantwort: requestSuccessful = " + requestSuccessful);

				// Die errorMessage einlesen. War alles in Ordnung, ist der String leer ("")
				final String errorMessage = deserializer.readString();
				_debug.finer("erste Archivantwort: errorMessage = " + errorMessage);

				// Den errechneten blockingFactor einlesen
				final int blockingFactor = deserializer.readInt();
				_debug.finer("erste Archivantwort: blockingFactor = " + blockingFactor);


				_requestSuccessful = requestSuccessful;
				_errorMessage = errorMessage;
				_blockingFactor = blockingFactor;

				// wartende Threads "befreien" und verhindern, dass andere Threads anfangen zu warten
				_blocking = false;

				// Nur wenn die Archivanfrage erfolgreich war werden die Streams angelegt und k�nnen
				// angefordert werden.
				if(_requestSuccessful == true) {

					// StreamDemultiplexer und StreamDemultiplexerDirector anlegen
					DirectorStreamDemultiplexer directorStreamDemultiplexer = new DirectorStreamDemultiplexer(_archiveRequestID, _streamedArchiveRequester);
					_streamDemultiplexer = new StreamDemultiplexer(_numberOfStreams, _blockingFactor, directorStreamDemultiplexer);

					_arrayOfStreams = new DataStream[_numberOfStreams];

					// Der Index des Arrays identifiziert auch den Stream
					for(int indexOfStream = 0; indexOfStream < _arrayOfStreams.length; indexOfStream++) {
						ArchiveDataSpecification archiveDataSpecification = (ArchiveDataSpecification)_spec.get(indexOfStream);
						DataStream dataStream = new DataStream(indexOfStream, _streamDemultiplexer, archiveDataSpecification, this);
						_arrayOfStreams[indexOfStream] = dataStream;
					}
				}

				// Alle Threads, die auf diese Nachricht warten aufwecken. Da _blocking gesetzt wurde, kann nun
				// kein Thread mehr in den status "schlafend" gesetzt werden.
				this.notifyAll();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		} // synchronized(this)
	}

	/**
	 * Diese Methode verwaltet Nutzdaten, die f�r eine Archivanfrage empfangen wurden.
	 *
	 * @param data Nutzdaten f�r eine Archivanfrage
	 */
	public void archiveDataResponse(byte[] data) {
		try {
			_streamDemultiplexer.receivedDataFromSender(data);
		}
		catch(IOException e) {
			e.printStackTrace();
			_debug.error("Ein Fehler beim serialisieren/deserialisieren: " + e);
		}
	}

	/**
	 * Diese Methode wird von einem Objekt der Klasse Query aufgerufen sobald der Stream des Objekts ein null-Paket empf�ngt oder die Empf�ngerapplikation abort
	 * aufruft. Das null-Paket bedeutet, dass das Archiv f�r diesen Stream keine Archivdaten mehr zur Verf�gung hat. Der Aufruf von abort bedeutet, dass die
	 * Empf�ngeraplikation keine Archivdaten f�r diesen Stream mehr ben�tigt.
	 * <p/>
	 * Sobald alle Streams ein null-Paket empfangen haben oder mit abort beendet wurden, wird das Objekt aus der Hashtable entfernt. TBD name schlecht gew�hlt, da
	 * im zweifelsfall die connection abgebaut wird, hier nicht nicht nur gez�hlt
	 */
	void countFinishedStream() {
		_numberOfFinishedStreams++;

		if(_numberOfFinishedStreams == _numberOfStreams) {
			// Das Objekt aus der Hashtable entfernen, der Auftrag ist abgearbeitet und es werden keine Archivantworten
			// mehr f�r diese Archivanfrage erwartet.
			_streamedArchiveRequester.removeRequest(_archiveRequestID);
		}
	}

	/**
	 * Diese Methode benachrichtigt den StreamDemultiplexer, dass ein Fehler aufgetreten ist und das alle Streams beendet werden m�ssen. Jeder aufruf der
	 * take-Methode liefert eine entsprechende Exception.
	 */
	void killAllStreams() {
		_streamDemultiplexer.killAllStreams();
	}

	private static class DirectorStreamDemultiplexer implements StreamDemultiplexerDirector {

		/** Dieses Objekt wird ben�tigt um ihm das Ticket zu �berreichen, dies wird dann verpackt und verschickt. */
		private final StreamedArchiveRequester _streamedArchiveRequester;

		private final ArchiveQueryID _archiveQueryID;

		/**
		 * Implementiert den StreamDemultiplexerDirector, somit kann eine Archivanfrage einen StreamDemultiplexer erzeugen.
		 *
		 * @param streamedArchiveRequester Verpackt und verschickt das Ticket an das Archiv
		 * @param archiveQueryID           Welche Archivanfrage verschickt das Ticket
		 */
		public DirectorStreamDemultiplexer(ArchiveQueryID archiveQueryID, StreamedArchiveRequester streamedArchiveRequester) {
			_streamedArchiveRequester = streamedArchiveRequester;
			_archiveQueryID = archiveQueryID;
		}

		public void sendNewTicketIndexToSender(byte[] streamTicketPacket) {
			try {
				_streamedArchiveRequester.sendTicketToArchive(_archiveQueryID, streamTicketPacket);
			}
			catch(ConfigurationException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				_debug.error(
						"Eine Quittung konnte nicht verschickt werden. Somit stoppt der Datenfluss vom StreamMultiplexer zum StreamDemultiplexer."
				);
			}
			catch(DataNotSubscribedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				_debug.error(
						"Eine Quittung konnte nicht verschickt werden. Somit stoppt der Datenfluss vom StreamMultiplexer zum StreamDemultiplexer."
				);
			}
			catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
				sendSubscriptionNotConfirmed.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				_debug.error(
						"Eine Quittung konnte nicht verschickt werden. Somit stoppt der Datenfluss vom StreamMultiplexer zum StreamDemultiplexer."
				);
			}
		}
	}

	/**
	 * Die Verbindung zum Archive wurde unterbrochen, alle Streams werden abgebrochen und liefern beim Aufruf der Methode {@link DataStream#take()} eine
	 * Exception.
	 */
	public void lostArchive() {
		_streamDemultiplexer.killAllStreams();
	}

	/**
	 * Die R�ckgabe identifiziert eine Archivanfrage.
	 *
	 * @return Eindeutige Identifikation einer Archivanfrage
	 */
	public ArchiveQueryID getArchiveRequestID() {
		return _archiveRequestID;
	}
}
