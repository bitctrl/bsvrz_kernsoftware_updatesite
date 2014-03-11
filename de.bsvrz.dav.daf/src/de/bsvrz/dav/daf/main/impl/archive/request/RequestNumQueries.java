/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.archive.ArchiveNumQueriesResult;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Diese Klasse stellt ein Objekt zur Verfügung, über das eine Anfrage nach der Anzahl möglichen Archivanfragen (pro Applikation) gestartet werden kann.
 * Diese Klasse wird von der Klasse {@link de.bsvrz.dav.daf.main.impl.archive.request.StreamedArchiveRequester} benutzt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11357 $ / $Date: 2013-06-26 13:29:33 +0200 (Mi, 26 Jun 2013) $ / ($Author: jh $)
 */
public class RequestNumQueries implements ArchiveNumQueriesResult {

	private final ArchiveQueryID _client;

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	private String _errorMessage = "";

	/**
	 * Diese Variable sperrt alle Anfrage, bis die Antwort des Archivsystems vorliegt.
	 */
	private boolean _lock = true;

	/**
	 * Diese Variable speichert, ob die Infoanfrage erfolgreich war.
	 */
	private boolean _requestSuccessful;
	private final StreamedArchiveRequester _streamedArchiveRequester;

	/**
	 * Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt.
	 */
	private final short _defaultSimulationVariant;

	private int _maxArchiveQueriesPerApplication;

	private int _currentlyUsedQueries;

	public RequestNumQueries(ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester, short defaultSimulationVariant) {
		_client = archiveRequestID;
		_streamedArchiveRequester = streamedArchiveRequester;
		_defaultSimulationVariant = defaultSimulationVariant;
	}

	@Override
	public int getMaximumArchiveQueriesPerApplication() {
		try {
			if (isRequestSuccessful()) {
				return _maxArchiveQueriesPerApplication;
			} else {
				throw new RuntimeException(_errorMessage);
			}
		}
		catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getCurrentlyUsedQueries() {
		try {
			if (isRequestSuccessful()) {
				return _currentlyUsedQueries;
			} else {
				throw new RuntimeException(_errorMessage);
			}
		}
		catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getRemainingQueries() {
		try {
			if (isRequestSuccessful()) {
				return _maxArchiveQueriesPerApplication - _currentlyUsedQueries;
			} else {
				throw new RuntimeException(_errorMessage);
			}
		}
		catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Diese Methode ist blockierend, bis die Antwort des Archivs vorliegt.
	 *
	 * @return true = Die Anfrage konnte fehlerfrei bearbeitet werden; false = Während der Bearbeitung der Anfrage kam es
	 *         zu einem Fehler, dieser kann mit <code>getErrorMessage</code> angezeigt werden
	 * @throws InterruptedException Der Thread, der den Auftrag bearbeitet, wurde abgebrochen
	 */
	public boolean isRequestSuccessful() throws InterruptedException {
		synchronized (this) {
			// Der Archivmanager ist möglicherweise veraltet und verwirft die Anfrage, daher hier nur eine begrenzte Zeit warten
			long waitUntil = System.currentTimeMillis() + 10000; // Max 10 Sekunden warten
			while (_lock) {
				long remainingMillis = waitUntil - System.currentTimeMillis();
				if(remainingMillis <= 0) {
					_requestSuccessful = false;
					_errorMessage = "Keine Antwort vom Archivsystem. Archivsystem unterstützt möglicherweise die Anfrage nicht (ist veraltet).";
				}
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return _requestSuccessful;
		}
	}

	/**
	 * Diese Methode liefert einen String mit der Fehlermeldung, die dazu geführt hat das die Informationsanfrage nicht
	 * ausgeführt werden konnte. Dieser Aufruf blockiert solange, bis ein Ergebnis des Archivsystems vorliegt.
	 *
	 * @return String mit einer Fehlermeldung
	 * @throws InterruptedException Der Thread, der den Auftrag bearbeitet, wurde abgebrochen
	 */
	public String getErrorMessage() throws InterruptedException {
		if (isRequestSuccessful() == false) {
			return _errorMessage;
		} else {
			return "Die Archiinformationsanfrage (" + _client.getIndexOfRequest() + ") war erfolgreich";
		}
	}

	/**
	 * Diese Methode verschickt eine Archivinfoanfrage an das entsprechende Archivsystem und meldet sich wieder als Sender
	 * ab.
	 */
	public void sendRequestInfo() {
		sendData();
	}

	private void sendData() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {
			serializer.writeInt(0); // Anfrage-ID, ertmal immer 0, kann aber erweitert werden
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}

		// byte-Array erzeugen
		final byte[] data = out.toByteArray();

		// Die Versionsnummer des Serializer speichern
		final int serializerVersion = serializer.getVersion();

		// daten + 4 bytes für die Serializerversion
		byte[] dataAndSeriVersion = new byte[data.length + 4];

		// Serializerversion speichern
		// Das höherwertigste Byte steht in Zelle 0
		dataAndSeriVersion[0] = (byte) ((serializerVersion & 0xff000000) >>> 24);
		dataAndSeriVersion[1] = (byte) ((serializerVersion & 0x00ff0000) >>> 16);
		dataAndSeriVersion[2] = (byte) ((serializerVersion & 0x0000ff00) >>> 8);
		dataAndSeriVersion[3] = (byte) (serializerVersion & 0x000000ff);

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);

		try {
			_streamedArchiveRequester.createArchivRequestResultData(_client, 21, dataAndSeriVersion);
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			throw new IllegalStateException("Versand einer Anfrage ist wegen nicht vorliegender positiver Sendesteuerung nicht möglich", sendSubscriptionNotConfirmed);
		} catch(DataModelException e){
			synchronized(this){
				_errorMessage = e.getMessage();
				_requestSuccessful = false;
				_lock = false;
				notifyAll();
			}
		}


		_debug.fine("ArchiveQueryManager verschickt Antwort auf einen Speicherauftrag an: Index der Anfrage: " + _client.getIndexOfRequest() + " Objekt: " + _client.getObjectReference().getName() + " archivNachrichtenTyp: 10 (Antwort auf einen Auftrag zum speichern)");
	}

	/**
	 * Diese Methode wird aufgerufen, wenn die Antwort des Archivsystems empfangen wurde. Die Antwort wird analysiert und
	 * die betreffenden Objekte zur Verfügung gestellt, wartende Threads werden benachrichtigt.
	 */
	public void archiveResponse(Data data) {
		synchronized (this) {
			// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen,
			// ob die Infoanfrage geklappt hat, gespeichert
			byte[] requestInfoResponse = data.getUnscaledArray("daten").getByteArray();

			InputStream in = new ByteArrayInputStream(requestInfoResponse);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			// Das erste byte bestimmt, ob die Anfrage geklappt hat. 0 = nein; 1 = ja
			try {
				final byte requestSuccessfulFlag = deserializer.readByte();

				if (requestSuccessfulFlag == 0) {
					_requestSuccessful = false;
				} else {
					_requestSuccessful = true;
				}

				if (_requestSuccessful == true) {
					// Die Anfrage konnte bearbeitet werden


					_maxArchiveQueriesPerApplication = deserializer.readInt();
					_currentlyUsedQueries = deserializer.readInt();


				} else {
					// Die Anfrage hat nicht funktioniert, den Fehler auslesen
					_errorMessage = deserializer.readString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * @return Eindeutige Identifikation der Archivanfrage (RequestInfo in diesem Fall)
	 */
	public ArchiveQueryID getArchiveRequestID() {
		return _client;
	}
}
