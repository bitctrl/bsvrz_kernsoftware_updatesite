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

import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.ArchiveQueryResult;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Diese Klasse stellt ein Objekt zur Verfügung, mit dem das Archivsystem beauftragt werden kann Daten nachzufordern.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class RequestData implements ArchiveQueryResult {
	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte der Auftrag zum nachfordern der Daten ausgeführt werden
	 */
	private boolean _requestSuccessful;

	/**
	 * Fehler, der beim nachfordern von Daten aufgetreten sein kann
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

	/**
	 * Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt.
	 */
	private final short _defaultSimulationVariant;

	public RequestData(ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester, short defaultSimulationVariant) {
		_archiveRequestID = archiveRequestID;
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
			return _requestSuccessful;
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
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den Auftrag zum Nachfordern vorliegt.
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
					_requestSuccessful = false;
					_errorString = deserializer.readString();
					_debug.info("Der Auftrag die Daten des Achivsystems " + _archiveRequestID.getObjectReference().getNameOrPidOrId() + " zu sichern ist fehlgeschlagen. Fehler: " + _errorString);
				} else {
					_requestSuccessful = true;
					_debug.fine("Der Auftrag die Daten des Achivsystems " + _archiveRequestID.getObjectReference().getNameOrPidOrId() + " zu sichern war erfolgreich.");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}


	public void request(Collection<ArchiveInformationResult> requiredData, Collection<SystemObject> requestedArchives) {

		// Im byte-Array wird die Serialisiererversion gespeichert. Die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {
			// Anzahl Einträge der Liste speichern, beim auspacken ist dann bekannt, wie viele Einträge
			// entpackt werden müssen.
			serializer.writeInt(requiredData.size());

			// Datenidentifikationen speichern
			for (Iterator<ArchiveInformationResult> iterator = requiredData.iterator(); iterator.hasNext();) {
				final ArchiveInformationResult archiveInformationResult = iterator.next();

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
				// für die Simulationsvariante gewählt werden. Der default-Wert wurde im Konstruktor dieser
				// Klasse übergeben.
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
			} // for

			// Referenzen auf Archivsysteme speichern

			writeSystemObjectReferences(requestedArchives, serializer);

		} catch (IOException e) {
			e.printStackTrace();
		}


		// byte-Array erzeugen
		final byte[] data = out.toByteArray();

		final byte[] dataAndSeriVersion = new byte[data.length + 4];

		dataAndSeriVersion[0] = ((byte) ((serializerVersion & 0xff000000) >>> 24));
		dataAndSeriVersion[1] = ((byte) ((serializerVersion & 0x00ff0000) >>> 16));
		dataAndSeriVersion[2] = ((byte) ((serializerVersion & 0x0000ff00) >>> 8));
		dataAndSeriVersion[3] = ((byte) (serializerVersion & 0x000000ff));

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);


		try {
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 17, dataAndSeriVersion);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
		}
	}

	public void request(final long startTime, final long endTime, final Collection<SystemObject> requestedArchives) {

		final int serializerVersion = SerializingFactory.getDefaultVersion();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {

			// Startzeit, long
			// Endzeit, long
			// Anzahl der anzufragenden Archivsysteme, int
			//	 Anzahl viele Einträge für Referenzen

			serializer.writeLong(startTime);
			serializer.writeLong(endTime);

			writeSystemObjectReferences(requestedArchives, serializer);

		} catch (IOException e) {
			e.printStackTrace();
		}

		final byte[] data = out.toByteArray();

		final byte[] dataAndSeriVersion = new byte[data.length + 4];

		dataAndSeriVersion[0] = ((byte) ((serializerVersion & 0xff000000) >>> 24));
		dataAndSeriVersion[1] = ((byte) ((serializerVersion & 0x00ff0000) >>> 16));
		dataAndSeriVersion[2] = ((byte) ((serializerVersion & 0x0000ff00) >>> 8));
		dataAndSeriVersion[3] = ((byte) (serializerVersion & 0x000000ff));

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);

		try {
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 19, dataAndSeriVersion);
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

	/**
	 * Speichert eine Collection, die SystemObjecs enthält, in einem Serializer. Der erste Wert stellt die Anzahl von
	 * Referenzen dar, die gespeichert werden sollen. Danach werden die Referenzen gespeichert.
	 *
	 * @param references Alle Referenenzen, die gespeichert werden sollen
	 * @param serializer Serializer, mit dem die Daten gespeichert werden
	 */
	private void writeSystemObjectReferences(final Collection<SystemObject> references, final Serializer serializer) throws IOException {
		serializer.writeInt(references.size());
		for (Iterator<SystemObject> iterator = references.iterator(); iterator.hasNext();) {
			final SystemObject systemObject = iterator.next();
			serializer.writeObjectReference(systemObject);
		}
	}
}
