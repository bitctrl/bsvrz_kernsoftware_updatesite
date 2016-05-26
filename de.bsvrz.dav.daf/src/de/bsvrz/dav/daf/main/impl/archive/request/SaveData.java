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

import de.bsvrz.dav.daf.main.archive.ArchiveQueryResult;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.Data;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

/**
 * Diese Klasse stellt ein Objekt zur Verfügung, mit dem ein Archivsystem beauftragt werden kann alle Datensätze,
 * die gespeichert werden dürfen, zu speichern.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SaveData implements ArchiveQueryResult {

	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte der Speicherauftrag durchgeführt werden
	 */
	private boolean _saveSuccessful;

	/**
	 * Fehler, der beim speichern von Daten aufgetreten sein kann
	 */
	private String _errorString;

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	private final StreamedArchiveRequester _streamedArchiveRequester;

	/**
	 * Sperrt solange Methodenaufrufe, bis eine Antwort des Archivs vorliegt
	 */
	private boolean _lock = true;

	public SaveData(ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester) {
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
			return _saveSuccessful;
		}
	}

	public String getErrorMessage() throws InterruptedException {
		if (isRequestSuccessful() == false) {
			return _errorString;
		} else {
			return "Die Archivanfrage(löschen) (" + _archiveRequestID.getIndexOfRequest() + ") war erfolgreich";
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den Speicherauftrag vorliegt.
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
					_saveSuccessful = false;
					_errorString = deserializer.readString();
					_debug.info("Der Auftrag die Daten des Achivsystems " + _archiveRequestID.getObjectReference().getNameOrPidOrId() + " zu sichern ist fehlgeschlagen. Fehler: " + _errorString );
				} else {
					_saveSuccessful = true;
					_debug.fine("Der Auftrag die Daten des Achivsystems " + _archiveRequestID.getObjectReference().getNameOrPidOrId() + " zu sichern war erfolgreich.");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * Der Aufruf dieser Methode stößt die Sicherungsfunktion des Archivsystems an. Alle Datensätze, die gesichert werden
	 * können, werden der Sicherung {@link de.bsvrz.dav.daf.main.impl.archive.filesaver.ArchiveFileSaver} übergeben.
	 */
	public void save() {

		// Im byte-Array wird die Serialisiererversion gespeichert. Die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();

		final byte[] dataAndSeriVersion = new byte[4];

		dataAndSeriVersion[0] = ((byte) ((serializerVersion & 0xff000000) >>> 24));
		dataAndSeriVersion[1] = ((byte) ((serializerVersion & 0x00ff0000) >>> 16));
		dataAndSeriVersion[2] = ((byte) ((serializerVersion & 0x0000ff00) >>> 8));
		dataAndSeriVersion[3] = ((byte) (serializerVersion & 0x000000ff));

		try {
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 9, dataAndSeriVersion);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
		}

	}

	public ArchiveQueryID getArchiveRequestID() {
		return _archiveRequestID;
	}
}
