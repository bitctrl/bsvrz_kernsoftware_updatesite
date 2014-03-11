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

import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryResult;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;

/**
 * Diese Klasse kann benutzt werden um ein Archivsystem aufzufordern seine Verwaltungsinformationen mit einem
 * Datentr�ger der Sicherung abzugleichen. Dies kann n�tig werden, wenn die Verwaltungsinformationen des Archivsystems
 * nicht mehr auf dem neusten Stand sind, f�r eine genaue Beschreibung siehe {@link
 * de.bsvrz.dav.daf.main.archive.ArchiveRequestManager#archiveFileSaverAlignment}.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sa, 01 Sep 2007) $ / ($Author: rs $)
 */
public class ArchiveAlignment implements ArchiveQueryResult {

	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte der Abgleich ausgef�hrt werden
	 */
	private boolean _alignmentSuccessful;

	/**
	 * Fehler, der beim l�schen von Daten aufgetreten sein kann
	 */
	private String _errorString;

	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	private final StreamedArchiveRequester _streamedArchiveRequester;

	/**
	 * Speichert die Simulationsvariante, die gel�scht werden soll
	 */
	private final int _volumeIdTypB;

	/**
	 * Sperrt solange Methodenaufrufe, bis eine Antwort des Archivs vorliegt
	 */
	private boolean _lock = true;

	public ArchiveAlignment(int volumeIdTypB, ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester) {
		_volumeIdTypB = volumeIdTypB;
		_archiveRequestID = archiveRequestID;
		_streamedArchiveRequester = streamedArchiveRequester;
	}

	public boolean isRequestSuccessful() {

		synchronized (this) {
			while (_lock) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return _alignmentSuccessful;
		}
	}

	public String getErrorMessage() throws InterruptedException {
		if (isRequestSuccessful() == false) {
			return _errorString;
		} else {
			return "Die Archivanfrage(Abgleich mit Sicherung) (" + _archiveRequestID.getIndexOfRequest() + ") war erfolgreich";
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den Auftrag zum Abgleichen der Verwaltungsinformationen
	 * mit einem bestimmten Speichermedium der Sicherung vorliegt.
	 *
	 * @param data Antwort des Archivs
	 */
	public void archiveResponse(Data data) {
		synchronized (this) {

			// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob der Speicherauftrag geklappt hat,
			// kodiert.
			byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

			InputStream in = new ByteArrayInputStream(queryResponse);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			try {
				final byte requestSuccessfulFlag = deserializer.readByte();

				if (requestSuccessfulFlag == 0) {
					_alignmentSuccessful = false;
					_errorString = deserializer.readString();
				} else {
					_alignmentSuccessful = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * Der Aufruf dieser Methode st��t die Abgleichsmethode des Archivsystems mit einem Speichermedium der Sicherung an.
	 * Die genaue Identifikation des Speichermediums der Sicherung wurde im Konstruktor �bergeben.
	 */
	public void archiveAlignment() {

		// Im byte-Array wird die Serialisiererversion gespeichert, die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert, und die Simulationsvariante, die gel�scht werden soll.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		//serialisieren
		Serializer serializer = SerializingFactory.createSerializer(out);
		try {
			serializer.writeInt(_volumeIdTypB);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final byte[] serilizedData = out.toByteArray();

		final byte[] dataAndSeriVersion = new byte[serilizedData.length + 4];

		// Serializerversion speichern
		dataAndSeriVersion[0] = (byte) ((serializerVersion & 0xff000000) >>> 24);
		dataAndSeriVersion[1] = (byte) ((serializerVersion & 0x00ff0000) >>> 16);
		dataAndSeriVersion[2] = (byte) ((serializerVersion & 0x0000ff00) >>> 8);
		dataAndSeriVersion[3] = (byte) (serializerVersion & 0x000000ff);

		System.arraycopy(serilizedData, 0, dataAndSeriVersion, 4, serilizedData.length);

		try {
			// Daten verschicken
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 15, dataAndSeriVersion);
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public ArchiveQueryID getArchiveRequestID() {
		return _archiveRequestID;
	}

}
