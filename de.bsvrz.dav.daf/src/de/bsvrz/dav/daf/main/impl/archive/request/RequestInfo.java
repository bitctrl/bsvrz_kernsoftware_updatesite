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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveInfoQueryResult;
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveQueryID;
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
 * Diese Klasse stellt ein Objekt zur Verfügung, über das eine Archivinformationsanfrage gestartet werden kann.
 * Diese Klasse wird von der Klasse {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class RequestInfo implements ArchiveInfoQueryResult {

	private final ArchiveQueryID _client;

	private final List<ArchiveDataSpecification> _specs;

	/**
	 * DebugLogger für Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	private String _errorMessage = "";

	/**
	 * Liste, die die Antwort des Archivs speichert
	 */
	private List<ArchiveInformationResult> _archiveInformationResults = new LinkedList<ArchiveInformationResult>();

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

	public RequestInfo(List<ArchiveDataSpecification> specs, ArchiveQueryID archiveRequestID, StreamedArchiveRequester streamedArchiveRequester, short defaultSimulationVariant) {
		_specs = specs;
		_client = archiveRequestID;
		_streamedArchiveRequester = streamedArchiveRequester;
		_defaultSimulationVariant = defaultSimulationVariant;
	}

	/**
	 * Diese Methode gibt eine Liste zurück, die alle Zeit/Indexbereiche einer Archivinformationsanfrage beinhaltet. Der
	 * Aufruf ist blockierend, bis ein Ergebnis vorliegt. War die Anfrage erfolgreich, wird die Liste zurückgegeben. War
	 * die Anfrage nicht erfolgreich wird eine RuntimeException ausgelöst ! Ein Aufruf dieser Methode sollte also nur
	 * erfolgen, nach dem sichergestellt wurde das <code>isRequestSuccessful</code> den Wert <code>true</code>
	 * zurückliefert.
	 *
	 * @return
	 */
	public List<ArchiveInformationResult> getArchiveInfoQueryResult() {
		try {
			if (isRequestSuccessful() == true) {
				return _archiveInformationResults;
			} else {
				throw new RuntimeException("Die Archiinformationsanfrage konnte nicht beantwortet werden, da ein Fehler aufgetreten ist: " + getErrorMessage());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
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

		// Die ArchivInfoAnfrage in ein byte-Array umwandeln

		// Alle Parameter müssen in einen Datensatz verpackt werden.
		// Die Priorität und die "spec" werden dabei in einem byte-Array des Datensatzes übertragen.
		// Die Daten werden mit einem Serializer verpackt und entsprechend auf der Empfangsseite mit einem
		// Deserializer entpackt. Damit die Daten mit der richtigen Version des Deserializers ausgepackt werden,
		// wird diese mitgeschickt. Die ersten 4 Bytes des byte-Arrays sind dabei die Versionsnummer
		// des Serializers und die entsprechende Version muss der Deserializer benutzen.
		// Die ersten 4 Bytes werden "von Hand" ein/ausgepackt, ohne den Serializer/Deserializer zu benutzen.
		// (somit ist die Versionsnummer immer unabhängig verfügbar, egal welche Version des Serializer/Deserializer
		// die Apllikation zur Verfügung hat)

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer serializer = SerializingFactory.createSerializer(out);

		try {
			// Anzahl Einträge der Liste speichern, beim auspacken ist dann bekannt, wie viele Einträge
			// entpackt werden müssen.
			serializer.writeInt(_specs.size());

			// Jede Anfrage, die in der spec-Liste steht, in das byte-Array speichern
			for (int i = 0; i < _specs.size(); i++) {

				ArchiveDataSpecification archiveDataSpecification = (ArchiveDataSpecification) _specs.get(i);

				{
					// ArchiveTimeSpecification anfordern und einzeln speichern
					ArchiveTimeSpecification archiveTimeSpecification = archiveDataSpecification.getTimeSpec();

					// TimingType als Byte speichern, dieser String kann beim entkodieren
					// benutzt werden, um das alte Objekt wieder zu erzeugen.
					// 1 = DATA_TIME
					// 2 = ARCHIVE_TIME
					// 3 = DATA_INDEX

					TimingType timingType = archiveTimeSpecification.getTimingType();

					if (timingType == TimingType.DATA_TIME) {
						serializer.writeByte(1);
					} else if (timingType == TimingType.ARCHIVE_TIME) {
						serializer.writeByte(2);
					} else if (timingType == TimingType.DATA_INDEX) {
						serializer.writeByte(3);
					}

					// boolean startRelative speichern. 0 = false, 1 = true, wird als Byte gespeichert
					if (archiveTimeSpecification.isStartRelative() == true) {
						serializer.writeByte(1);
					} else {
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
				}

				// SystemObject speichern
				SystemObject systemObject = archiveDataSpecification.getObject();

				{
					// SystemObject speichern
					serializer.writeObjectReference(systemObject);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// byte-Array erzeugen
		final byte[] data = out.toByteArray();

		// Die Versionsnummer des Serializer speichern
		final int serializerVersion = serializer.getVersion();

		// daten + 4 bytes für die Serializerversion
		byte[] dataAndSeriVersion = new byte[data.length + 4];

		// Serializerversion speichern
		// Das höherwärtigste Byte steht in Zelle 0
		dataAndSeriVersion[0] = (byte) ((serializerVersion & 0xff000000) >>> 24);
		dataAndSeriVersion[1] = (byte) ((serializerVersion & 0x00ff0000) >>> 16);
		dataAndSeriVersion[2] = (byte) ((serializerVersion & 0x0000ff00) >>> 8);
		dataAndSeriVersion[3] = (byte) (serializerVersion & 0x000000ff);

		System.arraycopy(data, 0, dataAndSeriVersion, 4, data.length);

		try {
			_streamedArchiveRequester.createArchivRequestResultData(_client, 5, dataAndSeriVersion);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (DataNotSubscribedException e) {
			e.printStackTrace();
		} catch (SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			throw new IllegalStateException("Versand einer Anfrage ist wegen nicht vorliegender positiver Sendesteuerung nicht möglich", sendSubscriptionNotConfirmed);
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
					// Die Infoanfrage konnte bearbeitet werden

					// Das Antwortobjekt erzeugen

					// Der erste Wert ist ein int, er bestimmt wieviele Objekte serialisiert wurde
					final int numberOfData = deserializer.readInt();
					for (int nr = 0; nr < numberOfData; nr++) {
						//Folgende Werte werden eingelesen
						// 1) intervalStart (long)
						// 2) intervalEnd (long)
						// 3) timingType (byte)
						// 4) isDataGap (byte)
						// 5) directAccess (byte)
						// 6) volumeId (int)
						// 7) Zeiger auf ArchiveDataSpecification Eintrag (int)

						final long intervalStart = deserializer.readLong();
						final long intervalEnd = deserializer.readLong();
						final byte byteTimingType = deserializer.readByte();
						final TimingType timingType;


						if (byteTimingType == 1) {
							timingType = TimingType.DATA_TIME;
						} else if (byteTimingType == 2) {
							timingType = TimingType.ARCHIVE_TIME;
						} else {
							timingType = TimingType.DATA_INDEX;
						}

						final byte byteDataGap = deserializer.readByte();
						final boolean dataGap;
						if (byteDataGap == 0) {
							dataGap = false;
						} else {
							dataGap = true;
						}

						final byte byteDirectAccess = deserializer.readByte();
						final boolean directAccess;
						if (byteDirectAccess == 0) {
							directAccess = false;
						} else {
							directAccess = true;
						}

						final int volumeId = deserializer.readInt();

						// Die ArchiveDataSpec werden nicht übertragen, sondern nur ein Index auf welche ArchiveDataSpec
						// sich diese antwort bezieht, somit kann aus der Liste der ArchiveDataSpecs der richtige
						// ausgewählt werden.
						final int pointerArchiveDataSpec = deserializer.readInt();

						// Es wurden alle Infos eingelesen um ein ArchiveInformationResult Objekt zu erzeugen
						ArchiveInformationResult archiveInformationResult = new ArchiveInfoResult(intervalStart, intervalEnd, timingType, dataGap, directAccess, volumeId, _specs.get(pointerArchiveDataSpec));

						// Ergebnisliste
						_archiveInformationResults.add(archiveInformationResult);


					} // for

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
