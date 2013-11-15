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

import de.bsvrz.dav.daf.main.archive.ArchiveQueryResult;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.ConfigurationException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 * Diese Klasse stellt ein Objekt zur Verf�gung, das ein Archivsystem beauftragt eine Simulationsvariante zu l�schen.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
 */
public class DeleteSimulationVariant implements ArchiveQueryResult {

	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte der L�schauftrag durchgef�hrt werden
	 */
	private boolean _deleteSuccessful;

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
	private final short _simulationVariant;

	/**
	 * Sperrt solange Methodenaufrufe, bis eine Antwort des Archivs vorliegt
	 */
	private boolean _lock = true;

	public DeleteSimulationVariant(short simulationVariant, ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester) {
		_simulationVariant = simulationVariant;
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

			return _deleteSuccessful;
		}
	}

	public String getErrorMessage() throws InterruptedException {
		if (isRequestSuccessful() == false) {
			return _errorString;
		} else {
			return "Die Archivanfrage(l�schen) (" + _archiveRequestID.getIndexOfRequest() + ") war erfolgreich";
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den L�schauftrag vorliegt.
	 *
	 * @param data Antwort des Archivs
	 */
	public void archiveResponse(Data data) {
		synchronized (this) {

			// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob der L�schauftrag geklappt hat,
			// kodiert.
			byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

			InputStream in = new ByteArrayInputStream(queryResponse);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			try {
				final byte requestSuccessfulFlag = deserializer.readByte();

				if (requestSuccessfulFlag == 0) {
					_deleteSuccessful = false;
					_errorString = deserializer.readString();
				} else {
					_deleteSuccessful = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * Der Aufruf dieser Methode st��t die L�schfunktion des Archivsystems an. Alle Datens�tze, die zu der angegebene
	 * Simulation geh�ren, werden gel�scht.
	 */
	public void deleteSimulationVariant() {

		// Im byte-Array wird die Serialisiererversion gespeichert, die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert, und die Simulationsvariante, die gel�scht werden soll.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		//serialisieren
		Serializer serializer = SerializingFactory.createSerializer(out);
		try {
			serializer.writeShort(_simulationVariant);
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
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 7, dataAndSeriVersion);
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
