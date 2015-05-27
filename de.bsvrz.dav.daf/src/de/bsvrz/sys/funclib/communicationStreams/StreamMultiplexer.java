/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
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
package de.bsvrz.sys.funclib.communicationStreams;

import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Diese Klasse verschickt Nutzdatenpakete mit Streams an einen StreamDemultiplexer. Die Applikation, die ein Objekt
 * dieser Klasse erzeugt hat, stellt ihrerseits Nutzdaten f�r jeden Stream zur Verf�gung. Auf der Gegenseite kann der
 * StreamDemultiplexer Nutzdaten auf jedem Stream anfordern und verarbeiten. Der StreamMultiplexer sendet seinerseits
 * nur dann Nutzdatenpakete, wenn ihn der StreamDemultiplexer dazu auffordert. Die Nutzdaten werden auch erst dann
 * erzeugt, wenn diese verschickt werden sollen. Der StreamMultiplexer verschickt die Nutzdatenpakete nicht einzeln,
 * sondern b�ndelt diese in einem gro�en Paket. Diese gro�en Pakete werden dann vom StreamDemultiplexer entgegen
 * genommen und ausgepackt. Diese B�ndelung findet f�r jeden Stream einzeln statt, in jedem gro�en Paket befinden sich
 * also nur Nutzdaten f�r diesen einen Stream, nicht die Nutzdaten anderer Streams.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8223 $
 */
public class StreamMultiplexer {

	/**
	 * Der blockingFactor bestimmt die Gr��e des Empfangspuffers. Dieser Wert bestimmt beim Sender nur, wie viele Pakete
	 * der Sender ohne Best�tigung des Empf�ngers, beim ersten Versenden, verschicken darf (danach mu� der Empf�nger dem
	 * Sender eine Sendeerlaubnis(Ticket) schicken). Dieser Wert mu� beim Sender und beim Empf�nger gleich sein.
	 */
	private final int _blockingFactor;

	/**
	 * Diese Variable bestimmt die gesamte Gr��e des Puffers, der zum StreamMultiplexer geh�rt. Jeder Stream besitzt einen
	 * eigenen Puffer. Sobald dieser gef�llt ist, werden alle Nutzdatenpakete als ein gro�es Paket verschickt.
	 */
	private final int _bufferSizeStreamMultiplexer;

	/**
	 * Diese Variable bestimmt die Gr��e des Puffers, den jeder Stream zur Verf�gung hat. Im Konstruktor wird dieser Wert
	 * mit <code>_bufferSizeStreamMultiplexer /_numberOfStreams</code> gesetzt.
	 */
	private final int _bufferSizeStream;

	/**
	 * Eine Warteschlange, in ihr werden alle Streams gespeichert, die Nutzdatenpakete verschicken k�nnen. In der
	 * Warteschlange werden Objekte vom Type "IndexOfStreamAndMaxSendPackets" gespeichert. Diese Objekte enthalten den
	 * "indexOfStream", welcher Stream kann Nutzdatenpakete verschicken, und "maxSendPackets". Dieser Wert dr�ckt aus, wie
	 * viele Nutzdatenpakete verschickt werden d�rfen (f�r diesen Stream). Die Warteschlange erspart somit das "Suchen"
	 * eines sendebereiten Streams.
	 */
	private final UnboundedQueue _queueWithStreamsPermitedToSendData;

	/**
	 * Anzahl der Streams auf denen Nutzdatenpakete verschickt werden k�nnen
	 */
	private final int _numberOfStreams;
	/**
	 * Anzahl der Streams, die mit "Abort" abgebrochen wurden oder die ein Nutzdatenpaket verschickt haben, in dem die
	 * Nutzdaten <code>null</code> waren.
	 */
	private int _numberTerminatedStreams = 0;

	/**
	 * In diesem Array werden alle Informationen aller Streams gespeichert.
	 */
	private final MultiplexerStreaminformations[] _streams;

	/**
	 * Objekt, das das Anfordern von Daten (von der Applikation) und versenden von Datenpaketen an den Empf�nger
	 * erm�glicht
	 */
	private final StreamMultiplexerDirector _director;

	/**
	 * Mit dieser Version wird der Serializer Nutzdaten verpacken und der Deserializer Tickets auspacken. Auf der
	 * Gegenseite wird der StreamDemultiplexer die Datens�tze mit dieser Version deserialisieren und die Tickets
	 * serialisieren.
	 */
	private final int _serializerVersion;

	// Debug, wie viele Daten wurden verschickt
	// Anzahl versandter Nutzdatenpackete
	private int _numberOfPacketsSend = 0;

	// Anzahl von falschen maxTicketIndex Ticktes, die der Empf�nger verschickt hat.
	// (der max Index war kleiner, als der aktuelle max Index. Das Ticketpaket wurde "�berholt")
	private int _numberOfFalseMaxTickets = 0;

	private static Debug _debug = Debug.getLogger();

//	private int _dataCount = 0;

	/**
	 * @param numberOfStreams             Anzahl von Streams, die Datenpakete versenden sollen
	 * @param blockingFactor              Anzahl der Pakete, die initial am Anfang versendet werden
	 * @param bufferSizeStreamMultiplexer Diese Variable bestimmt die gesamte Gr��e des Puffers, der zum StreamMultiplexer
	 *                                    geh�rt
	 * @param serializerVersion           Diese Variable legt die Versionsnummer des Deserializer/Serializer fest, der
	 *                                    benutzt wird. Sowohl der StreamMultiplexer als auch der StreamDemultiplexer
	 *                                    m�ssen die selbe Version benutzen
	 * @param director                    Schnittstelle, die eine Methode zum verschicken von Informationen an den Sender
	 *                                    bereitstellt (siehe Interface Beschreibung)
	 * @see de.bsvrz.sys.funclib.dataSerializer.Serializer
	 * @see de.bsvrz.sys.funclib.dataSerializer.Deserializer
	 */
	public StreamMultiplexer(int numberOfStreams, int blockingFactor, int bufferSizeStreamMultiplexer, int serializerVersion, StreamMultiplexerDirector director) {
		_numberOfStreams = numberOfStreams;
		_blockingFactor = blockingFactor;
		_bufferSizeStreamMultiplexer = bufferSizeStreamMultiplexer;
		_bufferSizeStream = _bufferSizeStreamMultiplexer / _numberOfStreams;
		_serializerVersion = serializerVersion;
		_director = director;

		_streams = new MultiplexerStreaminformations[_numberOfStreams];

		// In dieser Warteschlange werden alle Streams eingetragen, die Daten versenden d�rfen.
		_queueWithStreamsPermitedToSendData = new UnboundedQueue();


		for (int i = 0; i < _streams.length; i++) {
			// F�r jeden Stream ein Objekt f�r seine Informationen anlegen und im Array aller Streams speichern.
			// Auf dieses Objekt kann dann sp�ter ein synchronized angewendet werden.
			MultiplexerStreaminformations newStreaminformations = new MultiplexerStreaminformations(_blockingFactor, _director);
			_streams[i] = newStreaminformations;

			// Diese Objekt speichert, wie viele Nutzdatenpakete auf welchem Stream gesendet werden d�rfen.
			// Beim ersten Senden d�rfen aber blockingFactor viele Packete an den Empf�nger geschickt werden, ohne das dieser
			// best�tigen mu�.
			IndexOfStreamAndMaxSendPackets paketsForStream = new IndexOfStreamAndMaxSendPackets(i, _blockingFactor);
			// Den Sendeauftrag in die Warteschlange einf�gen
			_queueWithStreamsPermitedToSendData.put(paketsForStream);
		}
	}

	/**
	 * Diese Methode verschickt die Nutzdaten �ber einen bestimmten Stream an den Empf�nger.
	 *
	 * @param indexOfStream     : Der eindeutige Index des Streams auf dem gesendet werden soll
	 * @param streamPacketIndex : Jedes Datenpacket bekommt eine laufende Nummer
	 * @param data              : Nutzdaten, die versendet werden sollen
	 */
	private void sendDataToReceiver(int indexOfStream, int streamPacketIndex, byte[] data) {
		// Die �bergebenen Variablen m��en nun in ein byte-Array verpackt werden. Dieses byte-Array wird dann mit dem
		// _director �bertragen und auf der Gegenseite (StreamDemultiplexer) wieder "ausgepackt" und in die entsprechenden Variablen
		// umgewandelt und die passenden Objekte werden erzeugt.

		// Dieses Paket wird physisch �ber eine Verbindung an einen StreamDemultiplexer verschickt.
		// Die gr��e des Pakets setzt sich aus den drei Integerzahlen zusammen (3 * 4 Bytes) und der L�nge
		// des Nutzdatenpakets.

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//serialisieren
		Serializer serializer = null;
		try {
			serializer = SerializingFactory.createSerializer(_serializerVersion, out);
		} catch (NoSuchVersionException e) {
			e.printStackTrace();
			_debug.error("Die geforderte Serializer-Version kann vom Serializer des StreamMultiplexer nicht benutzt werden, geforderte Version: " + _serializerVersion);
		}

		try {
			// Die Daten werden wie folgt in dem Byte-Array verpackt
			// 1) indexOfStream (int)
			// 2) streamPacketIndex (int)
			// 3) L�nge des Byte-Arrays (int)
			// 4) Byte-Array (byte[])
			serializer.writeInt(indexOfStream);
			serializer.writeInt(streamPacketIndex);
			serializer.writeInt(data.length);
			serializer.writeBytes(data);
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		// Das Byte-Array erzeugen und verschicken
		_director.sendData(out.toByteArray());

		_numberOfPacketsSend++;
		_debug.finer("\t\t\t\t\t\t\t\t\t\tSenderpaket: Stream = " + indexOfStream + " PaketIndex = " + streamPacketIndex);
	}

	/**
	 * Diese Methode fordert von einer Application, auf einem bestimmten Stream, neue Nutzdaten an.
	 * <p/>
	 * Die Applikation wird von dem StreamMultiplexer angesprochen, darum private.
	 *
	 * @param indexOfStream Index des Streams, auf dem neue Daten angefordert werden sollen
	 * @return Nutzdaten
	 */
	private byte[] take(int indexOfStream) {

		// Es werden soviele Nutzdaten angefordert, bis <code>_bufferSizeStream</code> erreicht wird, dann wird
		// das gro�e Paket (mit den vielen kleinen Nutzdatenpaketen als Inhalt) verschickt.

		// Hier werden alle Informationen f�r das gro�e Paket gespeichert. Dies wird sp�ter in ein byte-Array umgewandelt
		ByteArrayOutputStream bigPacket = new ByteArrayOutputStream();
		// Serializer vorbereiten
		Serializer serializer = null;
		try {
			serializer = SerializingFactory.createSerializer(_serializerVersion, bigPacket);
		} catch (NoSuchVersionException e) {
			e.printStackTrace();
			_debug.error("Die geforderte Serializer-Version kann vom Serializer des StreamMultiplexer nicht benutzt werden, geforderte Version: " + _serializerVersion);
		}

		// Wie viele Bytes wurden bereits in dem gro�en Paket gespeichert, dies wird ben�tigt um zu pr�fen ob das
		// Paket schon die passende Gr��e hat.
		int sizeOfBigPacket = 0;

		// Es werden solange Nutzdaten in das gro�e Paket gepackt, bis dies die gew�nschte Gr��e hat oder null-Paket
		// von der Senderapplikation versandt wurde (null bedeutet, dass keine weiteren Nutzdaten mehr zur Verf�gung stehen).

		boolean bigPacketReady = false;

		// Anmerkung: Es wird mindestens ein Nutzdatenpaket verschickt (auch wenn das eine Paket die Gr��e des Puffers �berschreitet),
		// da die gr��e des aktuellen Pakets erst nach dem einf�gen eines Pakets gepr�ft wird.

		while (bigPacketReady == false) {
			// Ein Nutzdatum anfordern
			byte[] oneData = _director.take(indexOfStream);
//			_dataCount++;
			if (oneData == null) {
				// Die Senderappliaktion hat keine Nutzdaten mehr zur Verf�gung, somit mu� ein "null"-Paket verpackt werden.
				// Dies wird durch einen negativen Index f�r die Nutzdatenpaketgr��e angezeigt

				try {
					int negativPaketSize = -1;
					serializer.writeInt(negativPaketSize);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Nun mu� der Stream gekennzeichnet werden, dass er nicht mehr senden braucht.
				// Das null-Paket wird in dem gro�en Paket aber noch verschickt.

				synchronized (_streams[indexOfStream]) {

					// Somit darf der Stream keine Daten mehr senden, ABER das letzte gro�e Paket darf er noch verschicken.
					// Das passiert auch in der <code>sendAllStreamData()</code> Methode.
					_streams[indexOfStream].setStreamFinished();
					_debug.finer("\t\t\t\t\t\t\t\t\t\tSender verschickt null-Paket. Stream: " + indexOfStream + " und beendet den Stream");
					_numberTerminatedStreams++;
				}

			} else {
				// Es wurden Nutzdaten von der Senderapplikation geschickt

				// Wie gro� ist das Nutzdatum
				int sizeOfData = oneData.length;
				sizeOfBigPacket = sizeOfBigPacket + sizeOfData;

				// Als erstes wird gespeichert wie gro� der Nutzdatensatz ist und dann das Array f�r die Nutzdaten.
				// Somit ist beim auspacken des gro�en Pakets immer klar, wie gro� denn nun die eigentlichen
				// Nutzdatenpakete sind (die gr��e steht ja vor den Nutzdatenpaketen).
				try {
					serializer.writeInt(sizeOfData);
					serializer.writeBytes(oneData);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if ((sizeOfBigPacket > _bufferSizeStream) || (oneData == null)) {
				// Das erzeugte Paket ist nun gr��er als der Puffer des Streams, somit kann das Paket verschickt werden.
				// Wenn keine Nutzdaten mehr zur Verf�gung stehen (take liefert null), dann ist das Paket auch bereits
				// versandt zu werden.
				bigPacketReady = true;
			}
		}

		// Das gro�e Paket ist nun fertig, damit der Empf�nger wei� wann keine Daten (int, byte[]) mehr kommen, wird eine
		// Art EOF gesetzt. dies wird durch eine -2 symbolisiert. Jedes "bigPacket"/gro�es Paket bekommt diesen Stempel
		// am Ende (auch das null-Paket).

		try {
			serializer.writeInt(-2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Das bigPaket in ein Byte-Array umwandeln
		byte[] data = bigPacket.toByteArray();

		// Das gro�e Paket "verschicken"
		return data;
	}

	/**
	 * Diese Methode verschickt Nutzdaten, die die Senderapplikation erzeugt hat, an den Empf�nger (StreamDemultiplexer).
	 * Ein Problem das sich dabei ergibt ist, welcher Stream ist gerade sendebereit ? Das durchsuchen aller Streams nach
	 * einem sendebereiten Stream kann dabei sehr ungeschickt sein, da im "worst Case" immer alle Streams betrachtet werden
	 * m��ten und das f�r jede Nachricht. Da jeder Stream mindestens eine Nachricht verschickt w�re somit eine quadratische
	 * Laufzeit erreicht.
	 * <p/>
	 * Die Grundidee ist, dass alle Tickets an einer zentralen Stelle gesammelt werden. Daf�r wurde als Datenstruktur eine
	 * Warteschlange gew�hlt. Diese synchronisiert sich selbst�ndig und schickt wartende Threads automatisch in den wait
	 * Modus, gleichzeitig werden diese Threads wieder aufgeweckt, wenn neue Daten zur Verf�gung stehen.
	 * <p/>
	 * Sobald der Empf�nger dem Sender eine Sendeerlaubnis erteilt wird der Stream mit seinem Index und der Anzahl Pakete,
	 * die er senden darf, in die Warteschlange eingetragen.
	 * <p/>
	 * Wenn nun ein Stream gesucht wird, der senden soll, dann wird von der Warteschlange das vorderste Element
	 * angefordert. Es steht somit sofort ein Stream zur Verf�gung. Gleichzeitig ist bekannt, wieviele Pakete dieser Stream
	 * verschicken darf.
	 *
	 * @throws InterruptedException Ein Thread, der auf ein Objekt in der Warteschlange gewartet hat, wurde mit Interrupt
	 *                              unterbrochen.
	 */
	public void sendAllStreamData() throws InterruptedException {

		// In welchem Modus werden die Pakete versandt (jeder Stream verschickt nr ein Paket und reiht sich dann erneut ein, oder
		// jeder Stream verschickt soviele Pakete wie er darf).
		boolean singlePacketMode;

		// Solange es Streams gibt, die Senden d�rfen
		while (_numberTerminatedStreams < _numberOfStreams) {

			// Ein Element aus der Warteschlange anfordern.
			// Anmerkung, in der Zwischenzeit kann der StreamMultiplexer unterbrochen worden sein (killAllStreams).
			// Dieser Abbruch kann notwenig gewesen sein, weil der StreamDemultiplexer physich nicht mehr zu erreichen war,
			// es w�rden also keine Tickets mehr verschickt und somit w�rde es auch keine Sendebereiten Streams mehr geben.
			// Der Thread der den folgenden Aufruf t�tigt w�rde also nie wieder die Queue verlassen k�nnen.
			// Der Aufruf <code>killAllStreams</code> legt einen Dummy-Stream in die Queue, der abgebrochen wurde.
			// Somit wird der hier wartende Thread befreit und verl��t die while-Schleife.
			IndexOfStreamAndMaxSendPackets paketsForStream = (IndexOfStreamAndMaxSendPackets) _queueWithStreamsPermitedToSendData.take();

			// Welcher Stream darf Nutzdatenpakete verschicken
			int indexOfStream = paketsForStream.getIndexOfStream();

			// Falls der Stream noch nicht mit Abort abgebrochen wurde, darf der Stream senden.
			// Somit werden alle Streams, die noch in der Warteschleife stehen aber abgebrochen wurden,
			// langsam aus der Warteschlange entfernt ohne Daten zu verschicken.

			MultiplexerStreaminformations stream = _streams[indexOfStream];
			synchronized (stream) {

				// Wurde der Stream bereits abgebrochen ?
				if (stream.isStreamTerminated() == false) {
					_debug.finer("\n\t\t\t\t\t\t\t\t\t\tSender verschickt Nutzdatenpaket(e)");

					// Wie viele Pakete sollen auf einmal verschickt werden. Ist der single Modus aktiv, dann nur eins.
					// Im anderen Fall soviele wie erlaubt.
					final int numberOfPackets;

					if (_queueWithStreamsPermitedToSendData.size() == 0) {
						// Die Schlange ist leer, somit kann der gerade gew�hlte Stream alle Nutzdatenpakete verschicken
						// die er verschicken darf.
						singlePacketMode = false;
						// Wie viele Pakete sollen verschickt werden (in diesem Modus so viele wie m�glich)
						numberOfPackets = paketsForStream._maxSendPackets;
					} else {
						// Es gibt noch andere Streams die senden m�chten. Also verschickt jeder nur ein Nutzdatenpaket
						// und reiht sich dann wieder in der Warteschlange ein.
						singlePacketMode = true;
						// Wie viele Pakete sollen verschickt werden (in diesem Modus nur eins)
						numberOfPackets = 1;
					}

					for (int nr = 0; nr < numberOfPackets; nr++) {

						// Wie viele Nutzdatenpackete hat der Stream schon verschickt. Hinweis: Das Array wird mit "0" in jedem Eintrag initialisiert.
						int currentStreamPacketIndex = stream.getCurrentStreamPacketIndex();
						// Ein Paket verschicken
						currentStreamPacketIndex++;

						// Nutzdaten von der "Applikation" anfordern, wenn die Applikation eine "null" zur�ck gibt, dann sind
						// keine Nutzdaten mehr vorhanden. Der stream kann also beendet werden.
						byte[] data = take(indexOfStream);

						// Debug
						_debug.finer("\t\t\t\t\t\t\t\t\t\tPaketdaten: Stream(" + indexOfStream + ") Paketindex(" + currentStreamPacketIndex + ") Anzahl Pakete, die zu diesem Satz geh�ren (" + numberOfPackets + ")" + "Paketnummer des Satzes (Nummer 0 ist das erste Paket)) (" + nr + ")");

						// Das mit take angeforderte gro�e Nutzdatenpaket verschicken
						sendDataToReceiver(indexOfStream, currentStreamPacketIndex, data);
						// Da ein Paket verschickt wurde, mu� der neue Index gespeichert werden
						stream.setCurrentStreamPacketIndex(currentStreamPacketIndex);

						// Wenn bei dem Aufruf der take Methode ein null-Paket verpackt wurde, dann wird der Stream dort
						// mit <code>setStreamTerminated</code> beendet. Das angelegte Paket WURDE aber noch verschickt.
						// Allerdings werden f�r den Stream keine weiteren Pakete mehr versandt.
						if (stream.isStreamTerminated()) {
							// Es gibt f�r den Stream keine Nutzdaten mehr, der Sender beendet auf seiner Seite den Stream.
							// Der Empf�nger mu� nicht benachrichtigt werden, da er ein Nutzdatenpaket mit leeren
							// Nutzdaten empf�ngt. Er wird daraufhin auf seiner Seite den Stream
							// ebenfalls beenden (Der Sender wird davon nicht benachrichtigt, da er den Stream sowieso schon beendet hat).

							// Nun brauchen die restlichen Pakete nicht mehr verschickt werden, die For-Schleife kann abgebrochen werden
							break;
						}
					}

					if ((singlePacketMode == true) && (stream.isStreamTerminated() == false)) {
						// Im Single-Modus wird nur ein Paket verschickt, wenn der Stream noch Daten verschicken darf

						// Da ein Paket verschickt wurde, darf der Stream ein Paket weniger verschicken als vorher
						paketsForStream.decrementMaxSendPackets();
						if (paketsForStream.getMaxSendPackets() > 0) {
							// Der Stream darf noch Nutzdaten verschicken, also wird er wieder in die Warteschlange eingef�gt.
							// Wurden alle Nutzdatenpakete verschickt ("virtueller else Zweig"), dann mu� nichts gemacht werden.

							_queueWithStreamsPermitedToSendData.put(paketsForStream);
						}
					}

					_debug.finest("\t\t\t\t\t\t\t\t\t\t Anzahl verschickter Pakete(alle Streams) = " + _numberOfPacketsSend);
				}
			}
		}
//		_debug.info("sendAllStreamData: _dataCount = " + _dataCount + ", Anzahl verschickter Pakete(alle Streams) = " + _numberOfPacketsSend + ", _numberTerminatedStreams = " + _numberTerminatedStreams + ", _numberOfStreams = " + _numberOfStreams);
		_debug.info("StreamMultiplexer beendet sich");
	}

	/**
	 * <p/>
	 * Diese Methode setzt den "maximumStreamTicketIndex" eines Streams herauf. Dadurch kann der Stream Datenpakete bis zu
	 * diesem neuen Index versenden. Wird der "maximumStreamTicketIndex" erreicht, stellt der Stream seine Sendet�tigkeiten
	 * ein, bis der "maximumStreamTicketIndex" wieder erh�ht wird. Verschickt der Empf�nger eine "-1", dann will er die
	 * Empfangst�tigkeiten auf diesem Stream einstellen.
	 * <p/>
	 * <p/>
	 * Die ersten 4 Byte enthalten den Index des Streams. Die letzen vier Bytes enthalten den neuen maximalen Index. Dies
	 * ist eine Steuerung des einen Multis(Sender) durch den anderen(Receiver), kein Zugriff von au�en.
	 *
	 * @param streamTicketPacket Dieses Byte-Array enth�lt verschl�sselt den Index des Streams und den maximalen Index, bis
	 *                           zu dem der StreamMultiplexer senden darf.
	 * @throws IOException Ein Fehler beim deserialisieren von Daten
	 */
	public void setMaximumStreamTicketIndexForStream(byte[] streamTicketPacket) throws IOException {

		// Das empfangene Byte-Array in die entsprechenden Objekte umwandeln

		InputStream in = new ByteArrayInputStream(streamTicketPacket);

		//deserialisieren
		Deserializer deserializer = null;
		try {
			deserializer = SerializingFactory.createDeserializer(_serializerVersion, in);
		} catch (NoSuchVersionException e) {
			e.printStackTrace();
			_debug.error("Die geforderte Serializer-Version kann vom Serializer des StreamMultiplexer nicht benutzt werden, geforderte Version: " + _serializerVersion);
		}

		// Index des Streams, dessen Ticketanzahl erh�ht werden soll
		int indexOfStream = deserializer.readInt();

		// Eine neue Obergrenze bis zu der der Stream Daten senden darf, -1 zeigt an, dass der Stream beendet werden soll
		int maximumStreamTicketIndex = deserializer.readInt();

		// Wenn ein Stream beendet werden soll, verschickt der Empf�nger eine "-1 mit dem ""maximumStreamTicketIndex"
		if (maximumStreamTicketIndex > 0) {
			// Es wird auf das Objekt MultiplexerStreaminformations synchronisiert, nicht auf das gesamte Array
			MultiplexerStreaminformations stream = _streams[indexOfStream];
			synchronized (stream) {
				// Es kann sein, das der Sender einen Stream beendet (keine Nutzdaten mehr), aber denoch gerade Tickets zu ihm unterwegs sind
				if (stream.isStreamTerminated() == false) {
					// Der Empf�nger kann "maximumStreamTicketIndex" verschicken, die zu alt sind (ihr Wert ist kleiner als der schon empfangene oder sogar 0)
					// Siehe Kommentar StreamDemultiplexer take-Methode, Stichwort: verschicken neuer Tickets.
					if (stream.getMaxStreamPacketIndex() <= maximumStreamTicketIndex) {
						// Es mu� nun ein neues Element in die Warteschlange gelegt werden und der maximale Index, bis zu dem gesendet werden darf, mu�
						// aktualisiert werden.

						// Die neue maximale Anzahl von zu verschickenden Pakten wird berechnet
						// (dies sind wirklich einzelne Pakete, keine Index Nummer bis zu der geschickt werden darf).
						int maxNumberOfPacketsSendByStream = maximumStreamTicketIndex - stream.getMaxStreamPacketIndex();
						// Objekt f�r die Warteschlange anlegen
						IndexOfStreamAndMaxSendPackets paketsForStream = new IndexOfStreamAndMaxSendPackets(indexOfStream, maxNumberOfPacketsSendByStream);
						// Objekt in die Warteschlange einf�gen.
						// Der Zugriff auf die Warteschlange mu� nicht synchronisiert werden, da dies die Datenstruktur von alleine macht.
						_queueWithStreamsPermitedToSendData.put(paketsForStream);
						// Den neuen maximalen Index speichern
						stream.setMaxStreamPacketIndex(maximumStreamTicketIndex);

						// Debug
						_debug.finer("\t\t\t\t\t\t\t\t\t\tNeuer Max Ticket Index gesetzt: Stream(" + indexOfStream + ") neue maximaler Ticketindex(" + stream.getMaxStreamPacketIndex() + ")");
					} else {
						// Debug, wie viele "falsche" Ticktes werden verschickt (der maximale Paketindex war kleiner, als der aktuelle max Index)
						_numberOfFalseMaxTickets++;
					}
				}
			}

		} else {
			// Der Empf�nger hat einen Stream beendet
			_numberTerminatedStreams++;

			// Der Stream darf keine Nutzdatenpakete mehr verschicken
			synchronized (_streams[indexOfStream]) {
				_streams[indexOfStream].setStreamTerminated(indexOfStream);
			}
			// Falls der Stream, der gerade von der Empfangsseite beendet wurde, der letzte Stream war,
			// wird der Thread in der Warteschlange <code>_queueWithStreamsPermitedToSendData</code>
			// festh�ngen. Da keine Tickets mehr ankommen werden, somit muss der Thread befreit werden.
			// Jeder Stream, der abgebrochen wird, wird sich  noch einemal in diese Warteschlange
			// eintragen. Da der Stream aber abgebrochen, wird er keine Daten mehr verschicken.
			// Der wartende Thread wird dadurch aber erneut gestartet und kann erkennen, dass
			// der Stream nicht mehr senden dard und falls alle Streams beendet wurden, wird
			// sich der Thread ebenfalls beenden.
			// Das Problem ist fast identisch mit der <code>killAllStreams</code> - Methode, auch
			// dort konnte es zu einem Deadlock kommen. Die L�sung ist somit fast identisch.

			// Als indexOfStream, wird der abgebrochene Stream genommen
			// (das ist der unterschied zu <code>killAllStreams</code>, dort wird immer der Stream mit Index 0 genommen
			// der Effekt ist identisch)
			IndexOfStreamAndMaxSendPackets dummyEntry = new IndexOfStreamAndMaxSendPackets(indexOfStream, 1);
			_queueWithStreamsPermitedToSendData.put(dummyEntry);
		}
	}

	/**
	 * Alle Streams werden beendet, da die Verbindung zum Empf�nger unterbrochen wurde.
	 * <p/>
	 * Wenn das Objekt, dem der StreamMultiplexer geh�rt, einen Fehler des DaV gemeldet bekommt (die Leitung zur
	 * Empf�ngerapplikation wurde unterbrochen, als Beispiel) wird mit dieser Methode jeder Stream abgebrochen.
	 * Gleichzeitig wird die Sendeapplikation darauf hingewiesen, dass sie alle Nutzdaten f�r die Streams verwerfen kann.
	 * Auf der Gegenseite wird dem StreamDemultiplexer ebenfalls gemeldet, dass etwas mit der Verbindung nicht stimmt (dies
	 * �bernimmt dort das Objekt, das den StreamDemultiplexer erzeugt hat). Der StreamDemultiplexer wird daraufhin
	 * ebenfalls alle Streams beenden. Der beidseitige Abbruch geschieht automatisch.
	 */
	public void killAllStreams() {
		for (int nr = 0; nr < _numberOfStreams; nr++) {
			MultiplexerStreaminformations stream = _streams[nr];
			synchronized (stream) {
				// Der Stream wird abgebrochen/beendet
				stream.setStreamTerminated(nr);
				_numberTerminatedStreams++;
			}
		}
		// Alle Streams wurden abgebrochen. Es kann sein, das kein Stream senden durfte und somit
		// der Thread in der _queueWithStreamsPermitedToSendData festh�ngt. Es muss somit ein Dummy-Eintrag
		// erzeugt werden, dieser befreit den Thread. Nach dem er befreit wurde, wird festgestellt, dass
		// alle Streams abgeborchen wurden und der Thread beendet sich wie gewollt.
		IndexOfStreamAndMaxSendPackets dummyEntry = new IndexOfStreamAndMaxSendPackets(0, 1);
		_queueWithStreamsPermitedToSendData.put(dummyEntry);

	}

	private void printDebugVariables() {
		System.out.println("");
		System.out.println("");
		System.out.println("************************* Debug Variablen Stream Multiplexer (Sender) *************************");
		System.out.println("Anzahl versandter Pakete: " + _numberOfPacketsSend);
		System.out.println("Anzahl von MaxTicketIndizis, die falsch waren: " + _numberOfFalseMaxTickets);
		System.out.println("");
		System.out.println("");

	}

	/**
	 * Diese Klasse erzeugt ein Objekt f�r die Warteschlange "_queueWithStreamsPermitedToSendData". In dem Objekt ist der
	 * <code>_indexOfStream</code> und die Anzahl der zu verschickenden Daten "_maxSendPackets" enthalten. Die Klasse
	 * ben�tigt keinen Zugriff auf die sie umgebene Klasse, darum ist sie static.
	 */
	private static class IndexOfStreamAndMaxSendPackets {

		







		/**
		 * Index des Streams, der Nutzdatenpakete verschicken kann
		 */
		private final int _indexOfStream;
		/**
		 * Die Anzahl der Pakete, die ein Stream verschicken kann
		 */
		private int _maxSendPackets;

		/**
		 * @param indexOfStream  Index des Streams, der Daten verschicken kann
		 * @param maxSendPackets Maximale Anzahl von Paketen, die verschickt werden k�nnen
		 */
		public IndexOfStreamAndMaxSendPackets(int indexOfStream, int maxSendPackets) {
			_indexOfStream = indexOfStream;
			_maxSendPackets = maxSendPackets;

			// Debug
//			System.out.println("IndexOfStreamAndMaxSendPackets: indexOfStream = " + indexOfStream + " maxSendPackets = " + maxSendPackets);
		}

		/**
		 * Der Index des Streams wird zur�ck gegeben
		 */
		public int getIndexOfStream() {
			return _indexOfStream;
		}

		/**
		 * Die maximale Anzahl von zu verschickenden Paketen wird zur�ck gegeben
		 */
		public int getMaxSendPackets() {
			return _maxSendPackets;
		}

		/**
		 * Ein Paket wurde verschickt, somit kann das Maximum um eins verringert werden. Diese Methode wird nur benutzt, wenn
		 * "singlePacketMode = true" gesetzt wurde.
		 */
		public void decrementMaxSendPackets() {
			_maxSendPackets = _maxSendPackets - 1;
		}
	}

	/**
	 * Diese Objekt beinhaltet alle Informationen, die f�r einen Stream, auf Senderseiteseite, wichtig sind. Diese Objekte
	 * werden in einem Array (Index des Arrays ist dabei gleich der Nummer des Stream) gespeichert. Somit k�nnen alle
	 * Inforamtionen zu einem Stream mit einem Arrayzugriff geholt werden.
	 *
	 * @see StreamMultiplexer#_streams
	 */
	private static class MultiplexerStreaminformations {

		/**
		 * Bis zu welchem Paketindex darf der Sender Pakete versenden
		 */
		private int _maxStreamPacketIndex;

		/**
		 * Der aktuelle Index bis zu dem Pakete verschickt wurden
		 */
		private int _currentStreamPacketIndex;

		/**
		 * Wenn ein Stream abgebrochen wurde (mit abort des Empf�ngers oder take liefert null oder <code> killAllStreams
		 * </code>), dann wird dieser boolean true. Streng genommen m��te man die F�lle trennen, aber das bringt keinen
		 * Informationsgewinn, darum findet keine Trennung statt. (Jede Abfrage ob _streamTerminated == true m��te um eine
		 * Variable, ob der Stream "normal" beendet wurde oder der Senderapplikation kill verwendet hat, erweitert werden)
		 */
		private boolean _streamTerminated;

		private final StreamMultiplexerDirector _director;

		/**
		 * Beim erzeugen des Objekts wird sofort festgelegt, wie viele Nutzdatenpakete vom Sender beim ersten Senden
		 * verschickt werden (deblockingFactor viele). Darum wird der deblockngFactor beim erzeugen mit gegeben.
		 *
		 * @param deblockingFactor Wie viele Pakete werden beim initialen Senden durch den StreamMultiplexer verschickt
		 * @param director         Ein Stream kann mit diesem Objekt anzeigen das er abgebrochen wurde. Somit m�ssen keine
		 *                         weiteren Nutzdatenpakete, die mit <code>take</code> angefordert werden, bereitgestellt
		 *                         werden.
		 */
		public MultiplexerStreaminformations(int deblockingFactor, StreamMultiplexerDirector director) {
			_maxStreamPacketIndex = deblockingFactor;
			_director = director;
			// Dies ist zwar nicht n�tig, soll aber zeigen, dass der Wert am Anfang auf "0" initialsiert wird. Dies ist f�r
			// den Ablauf des Algorithmus wichtig.
			_currentStreamPacketIndex = 0;
			_streamTerminated = false;
		}

		public int getMaxStreamPacketIndex() {
			return _maxStreamPacketIndex;
		}

		public void setMaxStreamPacketIndex(int maxStreamPacketIndex) {
			_maxStreamPacketIndex = maxStreamPacketIndex;
		}

		public int getCurrentStreamPacketIndex() {
			return _currentStreamPacketIndex;
		}

		public void setCurrentStreamPacketIndex(int currentStreamPacketIndex) {
			_currentStreamPacketIndex = currentStreamPacketIndex;
		}

		/**
		 * Wenn der Stream keine Nutzdaten mehr verschicken darf, dann wird true zur�ck gegeben.
		 *
		 * @return true, wenn der Stream keine Nutzdaten mehr senden darf. false, wenn er noch Nutzdaten verschicken darf.
		 */
		public boolean isStreamTerminated() {
			return _streamTerminated;
		}

		/**
		 * Der Stream darf keine Daten mehr senden. Alle Informationen �ber den Stream stehen allerdings weiterhin zur
		 * Verf�gung (_streamTerminated wird noch ben�tigt, der Rest kann f�r Debug benutzt werden). Die Methode
		 * "isStreamTerminated" liefert nach Aufruf der Methode den Wert "true" zur�ck.
		 * <p/>
		 * Diese Methode wird aufgerufen, sobald das Null-Paket bei einem <code>take</code> Aufruf zur�ckgegeben wird.
		 */
		public void setStreamFinished() {
			_streamTerminated = true;
//			_debug.info("setStreamFinished: ###############################");
//			Thread.dumpStack();
		}

		/**
		 * Der Stream darf keine Daten mehr senden. Alle Informationen �ber den Stream stehen allerdings weiterhin zur
		 * Verf�gung (_streamTerminated wird noch ben�tigt, der Rest kann f�r Debug benutzt werden). Die Methode
		 * "isStreamTerminated" liefert nach Aufruf der Methode den Wert "true" zur�ck.
		 * <p/>
		 * Diese Methode wird aufgerufen, sobald der Stream ein abort vom StreamDemultiplexer empf�ngt oder die Methode
		 * <code>killAllStreams</code> des StreamMultiplexer wird aufgerufen. Bei Aufruf dieser Methode wird das Objekt, das
		 * den StreamMultiplexerDirector implementiert, benachrichtigt das es keine weiteren Nutzdatenpakete f�r den Stream
		 * bereithalten muss.
		 * <p/>
		 * Wurde der Stream bereits mit  <code>setStreamFinished</code> beendet, bewirkt dieser Methodenaufruf nichts.
		 */
		public void setStreamTerminated(int indexOfStream) {
			if (_streamTerminated == false) {
//				_debug.info("setStreamTerminated ###########################");
				// Der Stream wurde noch nicht beendet
				_streamTerminated = true;
				_director.streamAborted(indexOfStream);
//				Thread.dumpStack();
			}
		}
	}
}
