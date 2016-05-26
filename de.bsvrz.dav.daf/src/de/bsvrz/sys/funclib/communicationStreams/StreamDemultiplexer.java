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
package de.bsvrz.sys.funclib.communicationStreams;

import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.ProtocolException;
import java.nio.channels.ClosedChannelException;

import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;

/**
 * Diese Klasse empfängt Nutzdatenpakete, die vom StreamMultiplexer über Streams versandt wurden. Dabei werden die Daten
 * in streams aufgeteilt. Jeder stream stellt dabei der Applikation Nutzdaten zur Verfügung, die sie mit einer Methode
 * abrufen kann. Der StreamMultiplexer wird benachrichtigt falls die Applikation Nutzdaten benötigt. Dieser sorgt dann
 * dafür, dass neue Daten auf dem dafür vorgesehenen Stream bereitgestellt werden. Das Speichern der Nutzdaten geschieht
 * in einem Puffer, unterschreitet die Anzahl der vorgehaltenen Daten einen bestimmten Wert, so fordert der
 * StreamDemultiplexer neue Nutzdaten vom StreamMultiplexer an. Dies gewährleistet einen ständigen Vorrat von Nutzdaten,
 * auf die die Applikation zugreifen kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StreamDemultiplexer {
	/**
	 * Anzahl der Streams auf denen Datensätze verschickt werden können
	 */
	private final int _numberOfStreams;

	/**
	 * Der _blockingFactor  bestimmt die größe des Empfängerpuffers. Ist der Puffer nur noch halb voll, wird der Sender
	 * benachrichtigt, dass er neue Nutzdatensätze verschicken soll.
	 */
	private final int _blockingFactor;

	/**
	 * Der _blockingFactor wird durch 2 geteilt. Dieser Wert wird für die Ermittlung des neuen Indizes, an dem ein neues
	 * Ticket verschickt wird, benötigt. Im Konstruktor wird diese Variable auf _blockingFactor/2 gesetzt, wurde als
	 * _blockingFactor der Wert 1 gewählt, dann wird diese Variable ebenfalls auf 1 gesetzt.
	 */
	private final int _ticketBlockingFactor;

	/**
	 * Objekt, das das versenden von Tickets (Bestätigungen, dass der Sender Datenpakete senden darf) zur Verfügung
	 * stellen
	 */
	private final StreamDemultiplexerDirector _director;

	/**
	 * Alle Streams bekommen einen Eintrag in diesem Array. Der Platz an dem sie gespeichert werden entspricht der Nummer
	 * des Streams.
	 */
	private final DemultiplexerStreaminformations[] _arrayOfStreams;

	/**
	 * Debug, hier wird gezählt wie viele Nutzdatenpackete empfangen wurden
	 */
	private int _numberOfPacketsReceived = 0;
	/**
	 * Debug  Wenn ein stream aborted wird, dann darf er keine Daten mehr empfangen. Diese Variable zählt wie oft dies doch
	 * geschieht (durch verzahnung von Threads). Dieses Verhalten löst keinen Fehler aus.
	 */
	private int _abortedStreamReceivedData = 0;
	/**
	 * Debug Wie oft überlastet der Sender den Empfänger (der Sender schickt mehr Nutzdatenpakete als die dataQueue des
	 * Empfängers aufnehmen darf (_blockingFactor)). Für einen fehlerfreien Betrieb muß dieser Wert bei 0 bleiben !
	 */
	private int _numberOfOverchargesReceiver;

	/**
	 * Debug Anzahl der empfangenen Pakete je stream
	 */
	private final int[] _numberOfReceivedPackets;

	/**
	 * Debug Anzahl der Takes, die auch Nutzdaten enthielten (take mit einem Datensatz, der null enthält wird nicht
	 * gezählt)
	 */
	private final int[] _numberOfTakes;

	private static Debug _debug = Debug.getLogger();

	/**
	 * @param numberOfStreams Anzahl von Streams, die Datenpakete versenden sollen
	 * @param blockingFactor  Puffer des Empfängers (dieser Wert muß größer gleich 1 sein)
	 * @param director        Schnittstelle, die eine Methode zum verschicken von Informationen an den Sender bereitstellt
	 *                        (siehe Interface Beschreibung)
	 * @throws IllegalArgumentException Der blockingFactor war kleiner als 1
	 */
	public StreamDemultiplexer(int numberOfStreams, int blockingFactor, StreamDemultiplexerDirector director) {
		_numberOfStreams = numberOfStreams;

		if (blockingFactor < 1) {
			throw new IllegalArgumentException("Der blockingFactor muß größer gleich 1 sein");
		}

		_blockingFactor = blockingFactor;

		if (_blockingFactor == 1) {
			_ticketBlockingFactor = 1;
		} else {
			_ticketBlockingFactor = _blockingFactor / 2;
		}

		_arrayOfStreams = new DemultiplexerStreaminformations[_numberOfStreams];
		for (int i = 0; i < _arrayOfStreams.length; i++) {
			// i ist der Index des Streams
			// Wann muß der Stream die erste Sendebestätigung verschicken (_ticketBlockingFactor)
			// Wie viele Pakete kommen beim ersten zustanden kommen der Verbindung vom Sender (_blockingFactor)
			DemultiplexerStreaminformations streaminformations = new DemultiplexerStreaminformations(i, _ticketBlockingFactor, _blockingFactor);
			// Infos im Array speichern
			_arrayOfStreams[i] = streaminformations;
		}

		_director = director;

		// Debug
		_numberOfReceivedPackets = new int[_numberOfStreams];
		_numberOfTakes = new int[_numberOfStreams];
	}

	/**
	 * Eine Methode zum beenden eines Streams. Die restlichen Daten, die sich im  _arrayOfStreams befinden, werden
	 * gelöscht. An den Sender der Daten wird ein "-1" geschickt, dieser wird darauf hin keine Daten mehr auf diesem Stream
	 * schicken. Der Stream wird über seinen Index identifiziert.
	 *
	 * @param indexOfStream Index des Streams, der beendet werden soll
	 */
	public void abort(int indexOfStream) {
		_debug.fine("Abort Stream: " + indexOfStream);

		// Es wird nur ein Eintrag des Arrays "gesperrt", nicht das gesamte Array
		final DemultiplexerStreaminformations stream = _arrayOfStreams[indexOfStream];

		synchronized (stream) {
			if ((stream.isEndStream() == false) && (stream.isStreamAborted() == false) && (stream.isStreamTerminated() == false) && (stream.isLostConnectionToSender() == false)) {
				// Der Stream darf keinen Nutzdaten mehr empfangen/senden.
				// Es kann passieren, dass Threads gerade Nutzdaten anfordern (take Methode: referenceDataPacket = stream.getReferenceDataPacket();), allerdings
				// sind keine Nutzdaten vorhanden (deblockingFactor ist 1, ein take wurde aufgerufen und die Empfängerapplikation sendet ein abort).
				// Die neuen Daten sind dann zwar unterwegs, werden aber abgelehnt und die Threads warten vergebens auf neue Daten.
				// In der "setStreamAbortedTrue()" Methode wird ein künstliches "null" Paket erzeugt. Dadruch werden die Threads befreit.
				// In der Take Methode wird dann geprüft ob der Stream mit "Abort" beendet wurde, wenn ja, wird eine Exception geworfen.
				stream.setStreamAbortedTrue();
				// Dem Sender wird mitgeteilt, dass der Stream nicht mehr benötigt wird. Dies geschieht in dem der maximale Index
				// bis zu dem gesendet werden darf auf "-1" gesetzt wird.
				try {
					sendNewTicketIndexToSender(indexOfStream, -1);
				} catch (IOException e) {
					e.printStackTrace();
					_debug.error("Ein Fehler beim serialisieren/deserialisieren: " + e);
				}
			}
		}
	}

	/**
	 * Diese Methode gibt die Nutzdaten eines bestimmten Streams an die Empfängerapplikation zurück.
	 * Wenn alle Nutzdatenpakete von der Senderapplikation mit take angefordert wurden, gibt diese
	 * Methode nur noch null zurück, im Fehlerfall werden entsprechende Exceptions geworfen.
	 *
	 * @param indexOfStream Eindeutiger Index des Streams, der Daten zurückgeben soll
	 * @return Die Nutzdaten werden als Byte-Array geliefert
	 * @throws InterruptedException   Ein Thread, der Nutzdaten mit take anfordert hat, wird mit Interrupt abgebrochen.
	 * @throws IllegalStateException  Ein Stream wurde mit abort durch die Empfängerapplikation beendet und anschließend
	 *                                führte die Empfängerapplikation erneut ein take auf diesen Stream aus.
	 * @throws ProtocolException      Es wurde ein Paket mit einem falschen Index bearbeitet. Das deutet darauf hin, dass
	 *                                das Paket entweder schon einmal empfangen wurde (doppelt vorhanden) oder das ein
	 *                                Paket fehlt. Der Stream wird automatisch abgebrochen. Die Applikation kann weiter
	 *                                "take" aufrufen, wird aber immer für diesen Stream, diese Exception bekommen.
	 * @throws ClosedChannelException Die physische Verbindung zum Sender wurde unterbrochen. Jeder take Aufruf wird für
	 *                                alle Streams diese Exception ausgeben, da alle Streams betroffen sind
	 */
	public byte[] take(int indexOfStream) throws InterruptedException, IllegalStateException, ProtocolException, ClosedChannelException {

		// stream anfordern
		final DemultiplexerStreaminformations stream = _arrayOfStreams[indexOfStream];

		// Alle take-Aufrufe auf einem Stream fordern den Monitor auf dieses Objekt an. Somit kann
		// nur ein take eines Threads weiter arbeiten.
		// Das verhindert, dass alle Threads <code>stream.sizeOfSmallDataPacketQueue() == 0</code>
		// bekommen und somit alle Threads ein großes Paket auspacken können.
		synchronized (stream._smallDataPacketQueue) {

			// Der Aufruf zum entpacken eines großen Pakets darf auf keinen Fall mit in den synch. Block.
			// Denn wenn kein großes Parket vorhanden ist, wird der erste Thread sich mit dem synchronisierten Stream
			// schlafen legen (beim anforden des Pakets mit take), somit ist es dann unmöglich dem Stream jemals wieder
			// große Pakete zu geben (was aber nötig wäre, damit der erste Thread befreit wird). -> Deadlock
			boolean unpackBigPacket = false;

			synchronized (stream) {

				// Dieser If-Abfagen Block ist nötig, weil in der Zwischenzeit ein Fehler aufgetreten sein kann.
				// (als Beipsiel, ein Paketdreher beim Empfang)
				if (stream.isEndStream() == true) {
					// Die Sendeapplikation hatte keine Nutzdaten mehr (Normalfall).

					// Dieser Aufruf befreit einen weiteren Thread. Dieser wird auch zu dieser Abfrage kommen und
					// einen anderen wartenden Thread befreien, usw. . Alle Threads geben also "null" zurück
					stream.createUnlockPacket();
					// throw new IllegalStateException("Fehler Stream (Index: " + indexOfStream + "): Der Sender hat keine Nutzdaten mehr für den Empfänger. Der Stream wurde bereits beidseitig beendet, aber von der Empfängerapplikation wurde erneut 'take' augerufen");
					return null;
				} else if (stream.isStreamAborted() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Der Stream wurde mit Abort von der Empfängerapplikation beendet, somit darf kein Take mehr ausgeführt werden.
					throw new IllegalStateException("Fehler Stream (Index: " + indexOfStream + "): Der Stream wurde mit 'abort'von der Empfängerapplikation abgebrochen und dann erneut mit 'take' aufgerufen");
				} else if (stream.isStreamTerminated() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Ein Nutzdatenpaket des Stream ist entweder doppelt vorhanden oder vorschwunden. Der Empfänger hat die Verbindung abgebrochen
					throw new ProtocolException("Fehler Stream (Index: " + indexOfStream + "): Ein Nutzdatenpaket wurde entweder doppelt empfangen oder ist verschwunden. Der Empfänger(StreamDemultplexer) hat den Stream abgebrochen und den Sender (StreamMultiplexer) benachrichtigt");
				} else if (stream.isLostConnectionToSender() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Die physische Verbindung zum Empfänger ist unterbrochen. Die Empfängerapplikation informiert den Empfänger darüber.
					
					throw new ClosedChannelException();
				}

				if (stream.sizeOfSmallDataPacketQueue() == 0) {
					// Es stehen "keine" Nutzdaten mehr zur Verfügung. Aber vielleicht gibt es noch
					// große Pakete, die ausgepackt werden können und Nutzdaten enthalten.
					unpackBigPacket = true;
				}

			} // synchronized (stream)

			if (unpackBigPacket == true) {
				// Es muss ein großes Paket entpackt werden um Nutzdaten zu erhalten
				unpackBigPacket(stream);
			}

			synchronized (stream) {

				// Auch hier muss wieder geprüft werden, ob der Stream noch Nutzdaten schicken darf.
				// Es kann zu Paketdrehern gekommen sein (als Beispiel).
				if (stream.isEndStream() == true) {
					// Die Sendeapplikation hatte keine Nutzdaten mehr (Normalfall).

					// Dieser Aufruf befreit einen weiteren Thread. Dieser wird auch zu dieser Abfrage kommen und
					// einen anderen wartenden Thread befreien, usw. . Alle Threads geben also "null" zurück
					stream.createUnlockPacket();
					// throw new IllegalStateException("Fehler Stream (Index: " + indexOfStream + "): Der Sender hat keine Nutzdaten mehr für den Empfänger. Der Stream wurde bereits beidseitig beendet, aber von der Empfängerapplikation wurde erneut 'take' augerufen");
					return null;
				} else if (stream.isStreamAborted() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Der Stream wurde mit Abort von der Empfängerapplikation beendet, somit darf kein Take mehr ausgeführt werden.
					throw new IllegalStateException("Fehler Stream (Index: " + indexOfStream + "): Der Stream wurde mit 'abort'von der Empfängerapplikation abgebrochen und dann erneut mit 'take' aufgerufen");
				} else if (stream.isStreamTerminated() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Ein Nutzdatenpaket des Stream ist entweder doppelt vorhanden oder vorschwunden. Der Empfänger hat die Verbindung abgebrochen
					throw new ProtocolException("Fehler Stream (Index: " + indexOfStream + "): Ein Nutzdatenpaket wurde entweder doppelt empfangen oder ist verschwunden. Der Empfänger(StreamDemultplexer) hat den Stream abgebrochen und den Sender (StreamMultiplexer) benachrichtigt");
				} else if (stream.isLostConnectionToSender() == true) {
					// wartende Threads befreien
					stream.createUnlockPacket();
					// Die physische Verbindung zum Empfänger ist unterbrochen. Die Empfängerapplikation informiert den Empfänger darüber.
					throw new ClosedChannelException();
				}

				byte[] data = stream.getData();
				if (data == null) {
					stream.setEndStreamTrue();
				}

				// Diese Zeilen dienen nur Debugzwecken
//				System.out.println("StreamDemultiplexer Thread hält an");
//				Thread.sleep(5000);

				return data;
			} // synchronized (stream) (Teil 2, das ist nicht das erste synch auf dem stream)
		} // synchronized (stream._smallDataPacketQueue)
	}

	private void unpackBigPacket(DemultiplexerStreaminformations stream) throws InterruptedException {

		// Nutzdatenpakete (das können mehrere sein) und der Paketindex(des großen Pakets) stehen nun zur Verfügung.
		// Genau an dieser Stelle greift der Thread auf eine Queue zu, die ihn schlafen legt, wenn KEIN Paket vorhanden ist.
		// Um aber auf diese Schlange ein Paket zu legen, muss man sich auf die <code>DemultiplexerStreaminformations</code>
		// aber synchronisieren. Hat aber der wartende Thread den Monitor noch, dann gibt es einen Deadlock.
		// Aus diesem Grund wird dieser Zugriff aus jeden <code>synchronized(stream)</code> rausgehalten.
		final ReferenceDataPacket referenceDataPacket = stream.getReferenceDataPacket();

		// Der neue maximale Paketindex, dieser wird hier deklariert, weil er außerhalb des synch. Blocks benötigt wird.
		// Er wird aber innerhalb des synch. Blocks gesetzt, dies stellt aber kein Problem dar (siehe Kommentar zum versenden des Tickets).
		int newMaxStreamPacketIndex = 0;

		// Wenn im synch. Block ein neuer max Index Wert berechnet wird, dann muß ein Ticket versandt werden (aber nur dann).
		// Ist diese Variable auf true, dann muß ein Ticket versandt werden.
		boolean sendNewTicket = false;

		final int indexOfStream;

		// War das null-Paket in dem großen Paket
		boolean nullPacket = false;

		synchronized (stream) {
			// Nutzdaten und der Paketindex stehen nun zur Verfügung

			// Index des Stream
			indexOfStream = stream.getIndexOfStream();

			// Falls der Paketindex mit dem Index an dem eine neue Sendebestätigung gesendet werden muß überein stimmt, dann wird ein Ticket verschickt
			if (stream.getPacketIndexToSendNextMaxTicketIndex() == referenceDataPacket.getStreamPacketIndex()) {
				// Dieses Packet muß den Sender benachrichtigen, dass er neue Pakete verschicken muß.
				// Es werden _blockingFactor viele Pakete angefordert.

				// Den neuen Index berechnen, bis zu dem der Sender Pakete schicken darf
				newMaxStreamPacketIndex = stream.getPacketIndexToSendNextMaxTicketIndex() + _blockingFactor;

				// Den neuen Index berechnen, an dem eine neue Sendeerlaubnis an den Sender verschickt werden darf. Dies entspricht
				// _blockingFactor/2 oder aber der Variablen _ticketBlockingFactor. Diese wird bei einem
				// _blockingFactor von 1 automatisch auf 1 gesetzt.
				final int newPacketIndexToSendNextMaxTicketIndex = stream.getPacketIndexToSendNextMaxTicketIndex() + _ticketBlockingFactor;

				stream.setMaxStreamPacketIndex(newMaxStreamPacketIndex);
				stream.setPacketIndexToSendNextMaxTicketIndex(newPacketIndexToSendNextMaxTicketIndex);
				sendNewTicket = true;
			}

			// Hier steht nun das große Paket zur Verfügung, alle Berechnungen ob ein Ticket verschickt werden muss
			// (wenn ja, was für eins) wurden beendet. Nun können die Nutzdatenpakete (die kleinen Pakete) erzeugt werden.
			// Dabei kann gleich geprüft werden, ob ein null-Paket dabei war, falls ja, muß das Ticket (wenn denn eins verschickt
			// werden muss) gar nicht mehr versandt werden.

			// Das ganze findet auf einem synchronisierten Stream statt, da auf die Queue für die Nutzdaten zugegriffen werden
			// muss. Ist dieser nicht synchronisiert, kann die Reihenfolge der Nutzdaten durcheinander kommen.

			// Wurden alle Pakete aus dem großen Paket ausgepackt
			boolean unpackedAllPackets = false;
			// Hier sind alle kleinen Nutzdatenpakete(die für die Empfängerapplikation bestimmt sind) verpackt
			final byte[] bigDataPacket = referenceDataPacket.getData();

			final InputStream in = new ByteArrayInputStream(bigDataPacket);
			//deserialisieren
			final Deserializer deserializer = SerializingFactory.createDeserializer(in);

			while (unpackedAllPackets == false) {
				try {
					// Größe des Nutzdatenpakets
					int sizeOfData = deserializer.readInt();

					byte[] data;
					if (sizeOfData >= 0) {
						// Es sind Nutzdaten vorhanden, das null-Paket hat einen negativen Index
						data = deserializer.readBytes(sizeOfData);

						// Das gerade ausgepackte Nutzdatenpaket kann nun im Stream gespeichert werden.
						// _debug.fine("Ein normales Nutzdatenpaket wird in die kleine Queue gespeichert");
						stream.putDataSmallDataPacketQueue(data);

					} else {
						// Die Größe des Nutzdatenpakets ist negativ, somit wurde entweder ein null-Paket empfangen oder
						// das große Paket hat keine kleinen Nutzdatenpakete mehr.

						if (sizeOfData == -1) {
							// Das bedeutet, dass die Senderapplikation keine Nutzdaten mehr für die
							// Empfängerapplikation hat. Der Stream wurde auf der Senderseite bereits als
							// "beendet" markiert.
							// Es wird nicht weiter geguckt ob die -2 auch noch versandt wurde (was der Fall ist), weil
							// die -1 bereits eindeutig ist.
							data = null;
							nullPacket = true;
							unpackedAllPackets = true;

							// Das gerade ausgepackte Nutzdatenpaket kann nun im Stream gespeichert werden.
							stream.putDataSmallDataPacketQueue(data);

							_debug.fine("Beim auspacken eines großen Pakets, Stream(" + indexOfStream + ") wurde ein null-Paket mit Index(Index des großen Pakets) " + referenceDataPacket.getStreamPacketIndex() + " gefunden");
						} else {

							// Es wurden alle bytes des byte-Arrays ausgelesen, also ist das große Paket "leer"
							unpackedAllPackets = true;
							_debug.finer("Ein großes Paket wurde ausgepackt, es enthielt nur Nutzdaten. Stream:" + indexOfStream + " Index des großen Pakets: " + referenceDataPacket.getStreamPacketIndex());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}

		} // synchronized(stream)

		// Das versenden darf außerhalb des synch. Blocks stehen, da auf der Gegenseite (Sender) geprüft wird, ob
		// der neue maximale Index größer ist als der der gerade empfangen wird.
		// Es ist also nicht schlimm, wenn der Empfänger einen "alten" Index an den Sender schickt.
		if ((sendNewTicket == true) && (nullPacket == false)) {
			// Ein neues Ticket muß an den Sender verschickt werden. Dies muß aber nur geschehen, wenn der Sender noch Nutzdaten
			// für den Empfänger hat. Dies erkennt man daran, dass die Nutzdaten ungleich null sind. Sind sie null, dann hat
			// der Sender keine Nutzdaten mehr und hat seinerseits den Stream schon beendet (falls noch Tickets für
			// den Stream unterwegs sind, wird der Sender diese ignorieren)
			try {
				sendNewTicketIndexToSender(indexOfStream, newMaxStreamPacketIndex);
			} catch (IOException e) {
				e.printStackTrace();
				_debug.error("Ein Fehler beim serialisieren/deserialisieren: " + e);
			}
		}
		// Debug
		_numberOfTakes[indexOfStream] = _numberOfTakes[indexOfStream] + 1;
		_debug.finer("Take Stream(" + indexOfStream + ") liefert: Paket mit Index " + referenceDataPacket.getStreamPacketIndex() + " Insgesamt wurden " + _numberOfTakes[indexOfStream] + " takes auf diesem Stream ausgeführt");
	}

	/**
	 * Ein streamDataPacket, das der Multiplexer verschickt hat, wird entgegen genommen. Es hat als Inhalt den Index des
	 * Streams (die ersten 4 Bytes), den Index des Pakets (die nächsten 4 Bytes), die Größe des Byte-Arrays in dem die
	 * Nutzdaten gespeichert waren und die Nutzdaten selber (der Rest).
	 * <p>
	 * Diese Methode erzeugt dann aus dem Byte-Array die benötigten Objekte und legt diese in den dafür vorgesehenen
	 * Datenstrukturen ab.
	 * <p>
	 * Der Empfang neuer Nutzdaten wird von der Applikation an den StreamDemultiplexer geleitet (dadurch hat die
	 * Applikation den Datentransfer als Aufgabe).
	 *
	 * @param streamDataPacket Ein Byte-Array in dem verschlüsselt der Index des Streams, der Index des Pakets, die Größe
	 *                         des Byte-Arrays in dem die Nutzdaten gespeichert sind und die Nutzdaten selber stehen.
	 * @throws IOException Es ist ein Fehler beim deserialisieren der Daten aufgetreten
	 */
	public void receivedDataFromSender(byte[] streamDataPacket) throws IOException {
		// Byte-Array in Objekte zurück verwandeln

		InputStream in = new ByteArrayInputStream(streamDataPacket);

		//deserialisieren, als Serializer-Version wird default benutzt. Der StreamMultiplexer benutzt auf seiner Seite
		// ebenfalls diese Version.
		Deserializer deserializer = SerializingFactory.createDeserializer(in);

		// Den Index des Streams für den die Nutzdaten sind aus dem Byte-Array herausschreiben und ein int-Objekt erzeugen
		final int indexOfStream = deserializer.readInt();

		// Index des Pakets
		final int streamPacketIndex = deserializer.readInt();

		// Die Größe des Nutzdatenarrays
		final int lengthOfData = deserializer.readInt();

		// Es wird mit Nutzdatenarray mit der Größe <code>lengthOfData</code> angelegt. Dieses Array kann durchaus die Größe 0 haben (Die Senderapplikation hat
		// derzeit keine Daten, oder es ist eine Lücke vorhanden, oder dies hat eine andere Bedeutung)
		final byte[] data = deserializer.readBytes(lengthOfData);

		// Nutzdatenpaket für die Queue des Streams erzeugen
		ReferenceDataPacket referenceDataPacket = new ReferenceDataPacket(streamPacketIndex, data);

		DemultiplexerStreaminformations stream = _arrayOfStreams[indexOfStream];

		synchronized (stream) {
			// Darf der Stream noch Nutzdatenpakete annehmen
			if ((stream.isEndStream() == false) && (stream.isStreamAborted() == false) && (stream.isStreamTerminated() == false) && (stream.isLostConnectionToSender() == false)) {

				// Ist das Packet überhaupt das Packet, das erwartet wurde ? Oder ist zwischendurch ein Packet verloren gegangen oder ein Packet wurde
				// doppelt empfangen.
				// Der Stream speichert, welches Packet (Index des Packets) als nächstes ankommen muß. Wird dieses Packet nicht empfangen, dann ist bei
				// der Packetübertragung ein Fehler aufgetreten.
				if (streamPacketIndex == stream.getNextPacketIndex()) {

					// Nutzdatenpaket in der Queue des Streams speichern
					stream.newDataPacketForStream(referenceDataPacket);

					// Da ein Packet empfangen wurde muß der Packetindex des nächsten Packets um eins höher sein als der jetzige.
					stream.setNextPacketIndex(streamPacketIndex + 1);

					// Debug, wenn der Sender den Empfänger überlastet, dann wird abgebrochen
					if (stream.sizeOfDataQueue() > _blockingFactor) {
						// Die DataQueue hat mehr Daten gespeichert als sie eigentlich dürfte.
						// Somit überlastet der Sender den Empfänger

						_numberOfOverchargesReceiver++;
						_debug.error("Überlastung des Empfängers: Stream (" + indexOfStream + ") Paketindex (" + referenceDataPacket.getStreamPacketIndex() + ") _blockingFactor (" + _blockingFactor + ") Größe Dataqueue (" + stream.sizeOfDataQueue() + ") Der Fehler ist " + _numberOfOverchargesReceiver + " vorgekommen");

						throw new IllegalStateException("Der Empfängerpuffer wurde über das erlaubte Maß belaßtet");

						// assert stream.sizeOfDataQueue() <= _blockingFactor : "Der Puffer eines Streams(" + indexOfStream + ")wird über seinen Grenzwert hinweg belastet. Paketindex (" + referenceDataPacket.getStreamPacketIndex() + ") _blockingFactor (" + _blockingFactor + ") Größe Dataqueue (" + stream.sizeOfDataQueue() + ")";
					}
				} else {
					// Ein Packet doppelt oder es fehlt eins
					stream.setStreamTerminatedTrue();
					_debug.error("Stream (" + indexOfStream + ") wurde beendet, weil Nutzdatenpakete in der falschen Reihenfolge/gar nicht ankamen. Erwartet wurde ein Paket mit dem Index: " + stream.getNextPacketIndex() + " Empfangen wurde ein mit dem Index: " + referenceDataPacket.getStreamPacketIndex());
					// Den Sender informieren, dass etwas schief gegangen ist und das er das versenden von Nutzdaten einstellen kann
					sendNewTicketIndexToSender(indexOfStream, -1);
				}
			} else {
				// Der Stream wurde schon beendet/gestoppt, also kann dieses Nutzdatenpaket verworfen werden
				_debug.info("Der Stream war bereits beendet, aber es kamen noch Daten für ihn (" + indexOfStream + ")");
				_abortedStreamReceivedData++;
			}


		} // synchronized (stream)
		_numberOfPacketsReceived++;
		_numberOfReceivedPackets[indexOfStream] = _numberOfReceivedPackets[indexOfStream] + 1;
		// System.out.println("Empfänger bekommt Paket (geht in die Queue des Streams): Stream = " + indexOfStream + " Paketnummer = " + streamPacketIndex + " Anzahl aller empfangenden Pakete (über alle Streams) = " + _numberOfPacketsReceived);
		// printByteArrayScreen(data);
		// System.out.println("");
	}

	/**
	 * Der Sender wird benachrichtigt, dass er auf einem Stream weitere Nutzdatenpakete schicken darf.
	 * <p>
	 * Dies ist ein Vorgang, der intern zwischen dem Multiplexer/Demultiplexer statt findet, darauf soll die Applikation
	 * keinen Zugriff haben.
	 *
	 * @param indexOfStream             Die eindeutige Nummer des Stream, der neue Datenpakete schicken darf
	 * @param maximumStreamTicketsIndex Bis zu diesem Wert darf der Sender auf dem Stream neue Nutzdatenpakete schicken.
	 *                                  Der Sender benutzt dafür den streamPacketIndex als Anzahl wie viele Datenpakete
	 *                                  verschickt wurden.
	 *
	 * @throws IOException Es ist ein Fehler beim serialisieren der Daten aufgetreten
	 */
	private void sendNewTicketIndexToSender(int indexOfStream, int maximumStreamTicketsIndex) throws IOException {

		// Ein streamTicketPacket muß erzeugt werden. In ihm ist der Index des Streams und die der neue maximale Paketindex
		// gespeichert.

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		//serialisieren
		Serializer serializer = SerializingFactory.createSerializer(out);

		// Die Daten werden in folgender Reihenfolge gespeichert:
		// 1) indexOfStream (int)
		// 2) maximumStreamTicketsIndex (int)

		serializer.writeInt(indexOfStream);
		serializer.writeInt(maximumStreamTicketsIndex);

		_director.sendNewTicketIndexToSender(out.toByteArray());
		_debug.finer("Ein Ticket wird an den Sender verschickt: Stream " + indexOfStream + " neuer MaxIndex: " + maximumStreamTicketsIndex);
	}

	/**
	 * Die physische Verbindung zum Sender ist zusammengebrochen und alle Streams werden beendet. Allen Streams wird die
	 * Erlaubnis zum empfangen/senden entzogen. Diese Methode wird von der übergeordneten Applikation aufgerufen, diese
	 * bemerkt den Fehler.
	 */
	public void killAllStreams() {
		_debug.fine("Die Empfängerapplikation bricht alle Streams ab, da die Leitung zur Senderapplikation nicht mehr vorhanden ist.");
		for (int i = 0; i < _arrayOfStreams.length; i++) {
			DemultiplexerStreaminformations stream = _arrayOfStreams[i];

			synchronized (stream) {

				// Den Fehler genauer spezifizieren und dem Stream die Empfangs/Sendeerlaubnis entziehen
				stream.setLostConnectionToSenderTrue();
			}
		}
	}

	// Debug Methode, das Byte-Array ausgeben

	// gibt das Byte-Array formatiert auf dem Bildschirm aus
	private void printByteArrayScreen(byte[] data) {
		String output = "Nutzdaten: ";
		if (data != null) {
			for (int nr = 0; nr < data.length; nr++) {
				output = output + (char) data[nr];
			}
			System.out.println(output);
			System.out.println("");
		} else {
			System.out.println("Das Nutzdatenpaket hatte als Inhalt NULL");
		}
	}

	// Debug Methode, es werden alle Debug Variablen ausgegeben.
	private void printDebugVariables() {
		System.out.println("*****************Debug Variablen StreamDemultiplexer (Empfänger)*************************");

		System.out.println("");
		System.out.println("Anzahl Streams, die Abgebrochen wurden, aber noch Daten empfangen haben (kein Fehler): " + _abortedStreamReceivedData);
		System.out.println("Anzahl empfangender Pakete über alle Streams: " + _numberOfPacketsReceived);
		System.out.println("Anzahl der Fälle, wo der Sender überlastet wurde (dies ist ein schwerer Fehler, die Zahl sollte 0 sein): " + _numberOfOverchargesReceiver);
	}

	/**
	 * Objekte dieser Klasse speichern die Nutzdaten(Byte-Array) und den Index der Nutzdaten. Diese beiden Informationen
	 * werden zum ReferenceDataPacket (Nutzdatenpaket).
	 */
	private static class ReferenceDataPacket {
		private final byte[] _data;
		private final int _streamPacketIndex;

		ReferenceDataPacket(int streamPacketIndex, byte[] data) {
			_streamPacketIndex = streamPacketIndex;
			_data = data;
		}

		public byte[] getData() {
			return _data;
		}

		public int getStreamPacketIndex() {
			return _streamPacketIndex;
		}
	}

	/**
	 * Diese Objekt beinhaltet alle Informationen, die für einen Stream, auf Empfängerseite, wichtig sind. Diese Objekte
	 * werden in einem Array (Index des Arrays ist dabei gleich der Nummer des Stream) gespeichert. Somit können alle
	 * Inforamtionen zu einem Stream mit einem Arrayzugriff geholt werden
	 */
	private static class DemultiplexerStreaminformations {

		private final int _indexOfStream;
		/**
		 * Wann muß das nächste Ticket zum Sender geschickt werden, damit er neue Nutzdatenpakete verschickt
		 */
		private int _packetIndexToSendNextMaxTicketIndex;

		/**
		 * Bis zu welchem Paketindex darf der Sender Pakete versenden
		 */
		private int _maxStreamPacketIndex;

		/**
		 * Welchen Paketindex muß das nächste Paket haben. Diese Variable soll verhindern, dass Pakete doppelt empfangen
		 * werden oder das Pakete "verschwinden". Der default Wert ist 1, da das erste Paket den Index 1 hat.
		 */
		private int _nextPacketIndex;

		/**
		 * In dieser Queue werden die großen Nutzdatenpakete gespeichert. Diese Pakete enthalten die kleinen Nutzdatenpakete
		 * und können somit nicht zur Empfängerapplikation weitergegeben werden, sondern müssen zuerst ausgepackt werden. Die
		 * kleinen Nutzdatenpakete werden dann an die Empfängerapplikation weitergegeben.
		 */
		private final UnboundedQueue _bigDataPacketQueue;

		/**
		 * In dieser Queue werden die Nutzdaten gespeichert. Diese Pakete werden an die Empfängerapplikation weitergegeben.
		 */
		private final UnboundedQueue _smallDataPacketQueue;

		/**
		 * Wenn der Sender keine Nutzdaten mehr für den Empfänger hat, dann wird der Stream beendet (dies sollte der
		 * Normalfall sein). Diese Variable wird auf true gesetzt, wenn dieser Fall eintritt.
		 */
		private boolean _endStream;

		/**
		 * Wenn ein Stream mit abort beendet wurde, dann geschieht dies durch den Empfänger (er will keine Nutzdaten mehr).
		 * Die Variable _streamAborted wird auf true gesetzt.
		 */
		private boolean _streamAborted;

		/**
		 * Bei der Übertragung der Daten ist ein Fehler aufgetreten, entweder wurde ein Nutzdatenpaket doppelt empfangen oder
		 * ein Paket fehlt. Dieser Fehler betrifft aber nur diesen einen Stream. Damit die Applaktion auf die fehlerhafte
		 * Datenleitung des einen Streams aufmerksam gemacht werden kann, wird diese Variable auf true gesetzt, dadurch kann
		 * eine ProtocolException ausgelöst.
		 */
		private boolean _streamTerminated;

		/**
		 * Bei der Übertragung der Daten ist ein Fehler aufgetreten. Der DaV meldet, dass ein Verbindungsproblem vorliegt.
		 * Dies betrifft alle Streams und alle Streams werden beendet. Dieser Fall erzeugt eine ClosedChannelException für
		 * alle Streams auf die ein take ausgeführt wird.
		 */
		private boolean _lostConnectionToSender;

		public DemultiplexerStreaminformations(int indexOfStream, int packetIndexToSendNextMaxTicketIndex, int maxStreamPacketIndex) {
			_indexOfStream = indexOfStream;
			_packetIndexToSendNextMaxTicketIndex = packetIndexToSendNextMaxTicketIndex;
			_maxStreamPacketIndex = maxStreamPacketIndex;
			_nextPacketIndex = 1;

			// Puffer des Streams anlegen für große Pakete anlegen
			_bigDataPacketQueue = new UnboundedQueue();

			// Queue für ausgepackte Nutzdaten
			_smallDataPacketQueue = new UnboundedQueue();

			// Beim erzeugen der Streaminformationen ist der Stream für den Datenaustausch bereit
			_endStream = false;
			_streamAborted = false;
			_streamTerminated = false;
			_lostConnectionToSender = false;
		}

		public int getPacketIndexToSendNextMaxTicketIndex() {
			return _packetIndexToSendNextMaxTicketIndex;
		}

		public void setPacketIndexToSendNextMaxTicketIndex(int packetIndexToSendNextMaxTicketIndex) {
			_packetIndexToSendNextMaxTicketIndex = packetIndexToSendNextMaxTicketIndex;
		}

		public int getMaxStreamPacketIndex() {
			return _maxStreamPacketIndex;
		}

		public void setMaxStreamPacketIndex(int maxStreamPacketIndex) {
			_maxStreamPacketIndex = maxStreamPacketIndex;
		}

		public int getNextPacketIndex() {
			return _nextPacketIndex;
		}

		/**
		 * Index des Streams
		 *
		 * @return Index des Streams
		 */
		public int getIndexOfStream() {
			return _indexOfStream;
		}

		/**
		 * Der Index des Nutzdatenpakets, das als nächstes erwartet wird, wird hier neu gesetzt.
		 */
		public void setNextPacketIndex(int nextPacketIndex) {
			_nextPacketIndex = nextPacketIndex;
		}

		/**
		 * Diese Methode fordert ein Datenpaket des Streams an. Die Datenpakete werden in einer Queue gespeichert. Aus der
		 * Schlange wird das erste Paket zurück gegeben.
		 *
		 * @return Nutzdatenpaket, es besteht aus (verkapselt) data (Nutzdaten) und dem Index des Pakets
		 * @throws InterruptedException Die Datenstruktur UnboundedQueue kann durch ein Interrupt unterbrochen werden
		 */
		public ReferenceDataPacket getReferenceDataPacket() throws InterruptedException {
			ReferenceDataPacket dataPacket = (ReferenceDataPacket) _bigDataPacketQueue.take();
			return dataPacket;
		}

		/**
		 * Ein Datenpaket in einem Stream speichern (das Datenpaket muß mit take von der Applikation abgerufen werden, sonst
		 * steht es nicht zur Verfügung)
		 *
		 * @param dataPacket Ein Datenpaket (Definition: s.o.)
		 */
		public void newDataPacketForStream(ReferenceDataPacket dataPacket) {
			_bigDataPacketQueue.put(dataPacket);
		}

		/**
		 * Bestimmt die Größe der Queue, die die Nutzdatenpakete für die Empfängerapplikation speichert. Ist die Größe "0",
		 * dann muss ein großes Paket ausgepackt werden.
		 *
		 * @return Anzahl von Nutzdatenpakete, die für die Empfängerapplikation bestimmt sind
		 */
		public int sizeOfSmallDataPacketQueue() {
			return _smallDataPacketQueue.size();
		}

		/**
		 * Ein Nutzdatenpaket speichern, diese wird dann später an die Empfängerapplikation weiter gegeben.
		 *
		 * @param data Ein Nutzdaten für die Empfängerapplikation
		 */
		public void putDataSmallDataPacketQueue(byte[] data) {
			_smallDataPacketQueue.put(data);
		}

		/**
		 * Diese Methode stellt die Nutzdaten des Streams zur Verfügung. Dieser Zugriff wird nur dann ausgeführt, wenn vorher
		 * überprüft wurde, ob überhaupt Nutzdaten vorhanden sind (Methode: <code>sizeOfSmallDataPacketQueue</code>).
		 *
		 * @return
		 * @throws InterruptedException
		 */
		public byte[] getData() throws InterruptedException {
			assert _smallDataPacketQueue.size() > 0 : "Ein kleines Paket sollte geholt werden, obwohl keines mehr da war.";
			byte[] data = (byte[]) _smallDataPacketQueue.take();
			return data;
		}

		/**
		 * Wurde der Stream aufgrund fehlender Nutzdaten vom Sender beendet. true = ja, false = nein
		 */
		public boolean isEndStream() {
			return _endStream;
		}

		/**
		 * Wurde der Stream vom der Empfängerapplikation beendet (aus welchen Gründen auch immer). true = ja, false = nein
		 */
		public boolean isStreamAborted() {
			return _streamAborted;
		}

		/**
		 * Wurde der Stream durch den Empfänger beendet, weil ein Paket doppelt vorhanden oder gar nicht vorhanden war. true =
		 * ja, false = nein
		 */
		public boolean isStreamTerminated() {
			return _streamTerminated;
		}

		/**
		 * Wurde der Stream durch die Empfängerapplikation beendet, weil die physische Verbindung zur Senderapplikation
		 * unterbrochen wurde. true = ja, false = nein
		 */
		public boolean isLostConnectionToSender() {
			return _lostConnectionToSender;
		}

		/**
		 * Der Stream bei einem take Aufruf ein null-Paket zurück geliefert. Somit hatte die Senderapplikation keine Nutzdaten
		 * mehr und hat den Stream auf ihrer Seite bereits beendet. Der StreamDemultiplexer kann seinen Stream ebenfalls
		 * beenden.
		 */
		public void setEndStreamTrue() {
			_endStream = true;
		}

		/**
		 * Der Stream wurde abgebrochen, es kann passieren, dass noch Threads in der _bigDataPacketQueue hängen und auf Daten
		 * warten. Diese müßen künstlich befreit werden, in dem "unlockPackets" erzeugt werden. Diese Datenpakete enthalten
		 * keine Daten werden aber in die _bigDataPacketQueue aufgenommen und befreien somit einen Thread.
		 */
		public void setStreamAbortedTrue() {
			_streamAborted = true;
			// Ein unlockPacket erzeugen und in die dataQueue legen
			createUnlockPacket();
		}

		/**
		 * Da Fehlerhafte Nutzdatenpakete empfangen wurden, können alle empfangenen Nutzdatenpakete, die sich in der dataQueue
		 * befinden, verworfen werden. Falls noch Threads auf Daten warten, müßen diese befreit werden (Erklärung, siehe
		 * "setStreamAbortedTrue").
		 */
		public void setStreamTerminatedTrue() {
			_streamTerminated = true;
			createUnlockPacket();
		}

		/**
		 * Da die physische Verbindung zum Sender unterbrochen wurde, und somit keine Nutzdatenpakete mehr ankommen, können
		 * die restlichen Nutdaten auch gelöscht werden. Falls noch Threads auf Daten warten, müßen diese befreit werden
		 * (Erklärung, siehe "setStreamAbortedTrue").
		 */
		public void setLostConnectionToSenderTrue() {
			_lostConnectionToSender = true;
			createUnlockPacket();
		}

		/**
		 * Es kann passieren, dass in der _bigDataPacketQueue Threads auf Daten warten (durch den take-Aufruf). Wenn nun der
		 * Stream unterbrochen/beendet wird, dann kommen keine Pakete mehr. Die Threads warten also vergebens in der
		 * _bigDataPacketQueue. Damit diese Threads nun befreit werden können, werden "Unlock Packets" erzeugt. Diese
		 * enthalten keine Nutzdaten und jedes Paket befreit genau einen Thread aus der _bigDataPacketQueue. Der Thread wird
		 * dann mit dem "UnlockPacket" weiterarbeiten. Sobald er aber an die Stelle kommt an der der Zustand des Streams
		 * getestet wird (take Methode, synchronized(stream), if-Abfragen), wird eine Exception geworfen (der Stream wurde ja
		 * unterbrochen/beendet, also darf er nichts mehr senden). Diese Pakete sind also nur ein Trick um "hängende" Threads
		 * aus der dataQueue zu befreien.
		 * <p>
		 * Jeder Thread, der eine Exception auslösen wird (mit throws Exception), wird vorher diese Methode aufrufen.
		 */
		public void createUnlockPacket() {
			String errorMessage = "Dies ist ein unlockPacket. Es dient nur dazu, um wartende Threads aus der dataQueue eines Streams zu befreien. Der Stream sollte zu diesem Zeitpunkt bereits abgebrochen sein";
			ReferenceDataPacket unlockPacket = new ReferenceDataPacket(-999, errorMessage.getBytes());
			_bigDataPacketQueue.put(unlockPacket);
		}

		/**
		 * Diese Methode dient zum Debuggen. Sie liefert die größe (Anzahl Objekte) der Queue zurück. Ist die Anzahl der
		 * eingetragenen Elemente größer als der _deblockingFactor, dann ist ein Fehler aufgetreten. (Der Sender überlastet
		 * den Empfänger)
		 *
		 * @return Anzahl der eingetragenen Objekte in der Queue
		 */
		public int sizeOfDataQueue() {
			return _bigDataPacketQueue.size();
		}
	}
}
