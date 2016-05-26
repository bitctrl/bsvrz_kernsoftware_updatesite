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
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Diese Klasse beauftragt das Archivsystem Daten aus der Sicherung wieder in den direkten Zugriff des Archivsystems zu
 * bringen.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class RestoreData implements ArchiveQueryResult {

	/**
	 * Dieses Objekt identifiziert die Archivanfrage eindeutig.
	 */
	private final ArchiveQueryID _archiveRequestID;

	/**
	 * Konnte der Wiederherstellungsauftrag durchgeführt werden
	 */
	private boolean _restoreSuccessful;

	/**
	 * Fehler, der beim wiederherstellen von Daten aufgetreten sein kann
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
	private final List<ArchiveInformationResult> _requiredData;

	/**
	 * Falls keine Simulationsvariante gesetzt wird, dann wird dieser default-Wert benutzt.
	 */
	private final short _defaultSimulationVariant;

	public RestoreData(ArchiveQueryID archiveRequestID, List<ArchiveInformationResult> requiredData, StreamedArchiveRequester streamedArchiveRequester, short defaultSimulationVariant) {
		_archiveRequestID = archiveRequestID;
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
			return _restoreSuccessful;
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
	 * Diese Methode wird aufgerufen, wenn eine Antwort des Archivs auf den Wiederherstellungsauftrag vorliegt.
	 *
	 * @param data Antwort des Archivs
	 */
	public void archiveResponse(Data data) {
		synchronized (this) {
			// aus den Daten das byte-Array anfordern. In dem Array sind die Informationen, ob der Wiederherstellungsauftrag geklappt hat,
			// kodiert.
			byte[] queryResponse = data.getUnscaledArray("daten").getByteArray();

			InputStream in = new ByteArrayInputStream(queryResponse);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			try {
				final byte requestSuccessfulFlag = deserializer.readByte();

				if (requestSuccessfulFlag == 0) {
					_restoreSuccessful = false;
					_errorString = deserializer.readString();
				} else {
					_restoreSuccessful = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			_lock = false;
			this.notifyAll();
		}
	}

	/**
	 * Der Aufruf dieser Methode stößt die Wiederherstellungsfunktion des Archivsystems an. Alle angegebenen Datensätze
	 * werden von der Sicherung geladen und stehen dem Archivsystem wieder im direkten Zugriff zur Verfügung.
	 */
	public void restore() {

		// Im byte-Array wird die Serialisiererversion gespeichert. Die Antwort des Archivs wird dann ebenfalls
		// mit dieser Version serialisiert.

		// Die benutzte Serialisiererversion anfordern
		final int serializerVersion = SerializingFactory.getDefaultVersion();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {
			// Anzahl Einträge der Liste speichern, beim auspacken ist dann bekannt, wie viele Einträge
			// entpackt werden müssen.
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
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// byte-Array erzeugen
		final byte[] data = out.toByteArray();

		// daten + 4 bytes für die Serializerversion
		final byte[] dataAndSeriVersion = new byte[data.length + 4];

		// Serializerversion speichern
		// Das höherwärtigste Byte steht in Zelle 0
		dataAndSeriVersion[0] = (byte) ((serializerVersion & 0xff000000) >>> 24);
		dataAndSeriVersion[1] = (byte) ((serializerVersion & 0x00ff0000) >>> 16);
		dataAndSeriVersion[2] = (byte) ((serializerVersion & 0x0000ff00) >>> 8);
		dataAndSeriVersion[3] = (byte) (serializerVersion & 0x000000ff);

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);


		try {
			_streamedArchiveRequester.createArchivRequestResultData(_archiveRequestID, 11, dataAndSeriVersion);
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
