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
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.List;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;

/**
 * Diese Klasse beauftragt das Archivsystem den L�schzeitpunkt von Daten, die sich im direkten Zugriff des Archivsystems
 * befinden, um einen bestimmten Zeitraum zu verl�ngern.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5064 $ / $Date: 2007-09-01 22:25:35 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
 */
public class IncreaseDeleteTime implements ArchiveQueryResult {
	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte die geforderte Zeit gesetzt werden
	 */
	private boolean _increaseTimeSuccessful;

	/**
	 * Fehler, der beim wiederherstellen von Daten aufgetreten sein kann
	 */
	private String _errorString;

	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	private final StreamedArchiveRequester _streamedArchiveRequester;

	/**
	 * Sperrt solange Methodenaufrufe, bis eine Antwort des Archivs vorliegt
	 */
	private boolean _lock = true;
	/**
	 * Zeitbereiche, die l�nger im Zugriff des Archivsystems bleiben sollen.
	 */
	private final List<ArchiveInformationResult> _requiredData;

	/**
	 * Bestimmt die Zeitspanne um die die Daten l�nger im direkten Zugriff des Archivsystems bleiben sollen.
	 */
	private final long _timePeriod;

	/**
	 * Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt.
	 */
	private final short _defaultSimulationVariant;

	/**
	 * @param archiveRequestID         eindeutige Identifikation des Objekts
	 * @param requiredData             Daten, die L�nger im direkten Zugriff des Archivs bleiben sollen
	 * @param timePeriod               Zeitspanne, die die Daten l�nger im direkten Zugriff des Archivsystems bleiben
	 *                                 sollen
	 * @param streamedArchiveRequester Objekt, �ber das Daten verschickten kann
	 */
	public IncreaseDeleteTime(ArchiveQueryID archiveRequestID, List<ArchiveInformationResult> requiredData, long timePeriod, StreamedArchiveRequester streamedArchiveRequester, short defaultSimulationVariant) {
		_archiveRequestID = archiveRequestID;
		_timePeriod = timePeriod;
		_requiredData = requiredData;
		_streamedArchiveRequester = streamedArchiveRequester;
		_defaultSimulationVariant = defaultSimulationVariant;
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
			return _increaseTimeSuccessful;
		}
	}

	public String getErrorMessage() throws InterruptedException {
		if (isRequestSuccessful() == false) {
			return _errorString;
		} else {
			return "Die Archivanfrage(wiederherstellen) (" + _archiveRequestID.getIndexOfRequest() + ") war erfolgreich";
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den Auftrag zum verschieben des L�schzeitpunkts vorliegt.
	 *
	 * @param data Antwort des Archivs
	 */
	public void archiveResponse(Data data) {
		synchronized (this) {
			// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob der Auftrag zum verschieben des L�schzeitpunkts geklappt hat,
			// kodiert.
			byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

			InputStream in = new ByteArrayInputStream(queryResponse);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			try {
				final byte requestSuccessfulFlag = deserializer.readByte();

				if (requestSuccessfulFlag == 0) {
					_increaseTimeSuccessful = false;
					_errorString = deserializer.readString();
				} else {
					_increaseTimeSuccessful = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * Der Aufruf dieser Methode beauftragt das Archivsystem den L�schzeitpunkt der angegebenen Zeitbereiche um den
	 * angegebenen Zeitbereich zu verl�nger.
	 */
	public void increaseDeleteTime() {

		// Im byte-Array wird die Serialisiererversion gespeichert. Die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {

			// Zeitspanne um die die Zeitbereiche l�nger im Archiv verbleiben sollen
			serializer.writeLong(_timePeriod);

			// Anzahl Eintr�ge der Liste speichern, beim auspacken ist dann bekannt, wie viele Eintr�ge
			// entpackt werden m�ssen.
			serializer.writeInt(_requiredData.size());

			// Jede Anfrage, die in der spec-Liste steht, in das byte-Array speichern
			for (int i = 0; i < _requiredData.size(); i++) {

				ArchiveInformationResult archiveInformationResult = _requiredData.get(i);

				// TimingType als Byte speichern, dieser String kann beim entkodieren
				// benutzt werden, um das alte Objekt wieder zu erzeugen.
				// 1 = DATA_TIME
				// 2 = ARCHIVE_TIME
				// 3 = DATA_INDEX

				final TimingType timingType = archiveInformationResult.getTimingType();

				if (timingType == TimingType.DATA_TIME) {
					serializer.writeByte(1);
				} else if (timingType == TimingType.ARCHIVE_TIME) {
					serializer.writeByte(2);
				} else if (timingType == TimingType.DATA_INDEX) {
					serializer.writeByte(3);
				}

				// Die Angaben sind immer absolut
				// boolean startRelative speichern. 0 = false, 1 = true, wird als Byte gespeichert
				serializer.writeByte(0);

				// long intervalStart speichern
				serializer.writeLong(archiveInformationResult.getIntervalStart());

				// long intervalEnd speichern
				serializer.writeLong(archiveInformationResult.getIntervalEnd());

				// ArchiveDataKindCombination speichern
				final ArchiveDataKindCombination archiveDataKindCombination = archiveInformationResult.getArchiveDataSpecification().getDataKinds();

				// Jede boolean Variable wird als Byte gespeichert (0 = false; 1 = true)
				if (archiveDataKindCombination.isOnline() == true) {
					serializer.writeByte(1);
				} else {
					serializer.writeByte(0);
				}

				if (archiveDataKindCombination.isOnlineDelayed() == true) {
					serializer.writeByte(1);
				} else {
					serializer.writeByte(0);
				}

				if (archiveDataKindCombination.isRequested() == true) {
					serializer.writeByte(1);
				} else {
					serializer.writeByte(0);
				}

				if (archiveDataKindCombination.isRequestedDelayed() == true) {
					serializer.writeByte(1);
				} else {
					serializer.writeByte(0);
				}

				// ArchiveOrder speichern
				final ArchiveOrder archiveOrder = archiveInformationResult.getArchiveDataSpecification().getSortOrder();

				// Der int Wert kann benutzt werden, um das Objekt wieder herzustellen
				serializer.writeInt(archiveOrder.getCode());

				// ArchiveRequestOption speichern
				final ArchiveRequestOption archiveRequestOption = archiveInformationResult.getArchiveDataSpecification().getRequestOption();

				// Der int Wert kann benutzt werden, um das Objekt wieder herzustellen
				serializer.writeInt(archiveRequestOption.getCode());

				// DataDescription speichern
				final DataDescription dataDescription = archiveInformationResult.getArchiveDataSpecification().getDataDescription();

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
				if (dataDescription.getSimulationVariant() != -1) {
					serializer.writeShort(dataDescription.getSimulationVariant());
				} else {
					// Die Simulationsvariante wurde auf -1 gesetzt, somit muss der default-Wert benutzt werden.
					serializer.writeShort(_defaultSimulationVariant);
				}

				// SystemObject speichern
				final SystemObject systemObject = archiveInformationResult.getArchiveDataSpecification().getObject();

				// SystemObject speichern
				serializer.writeObjectReference(systemObject);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// byte-Array erzeugen
		final byte[] data = out.toByteArray();

		// daten + 4 bytes f�r die Serializerversion
		final byte[] dataAndSeriVersion = new byte[data.length + 4];

		// Serializerversion speichern
		// Das h�herw�rtigste Byte steht in Zelle 0
		dataAndSeriVersion[0] = (byte) ((serializerVersion & 0xff000000) >>> 24);
		dataAndSeriVersion[1] = (byte) ((serializerVersion & 0x00ff0000) >>> 16);
		dataAndSeriVersion[2] = (byte) ((serializerVersion & 0x0000ff00) >>> 8);
		dataAndSeriVersion[3] = (byte) (serializerVersion & 0x000000ff);

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);


		try {
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 13, dataAndSeriVersion);
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
