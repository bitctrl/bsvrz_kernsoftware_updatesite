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

import de.bsvrz.dav.daf.main.impl.archive.ArchiveDataCompression;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.archive.ArchiveData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataStream;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.communicationStreams.StreamDemultiplexer;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.channels.ClosedChannelException;
import java.util.zip.InflaterInputStream;

/**
 * Diese Klasse implementiert das Interface ArchiveDataStream. Die Datens�tze, die empfangen wurden, k�nnen gepackt
 * sein, diese Klasse entpackt die Datens�tze und stellt sie als Objekte zur Verf�gung. Diese Klasse wird von der Klasse
 * {@link StreamedArchiveRequester} benutzt.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 6324 $ / $Date: 2009-02-09 16:47:39 +0100 (Mon, 09 Feb 2009) $ / ($Author: rs $)
 */
class DataStream implements ArchiveDataStream {

	private final StreamDemultiplexer _streamDemultiplexer;
	/**
	 * Stream von dem die Daten geholt werden.
	 */
	private final int _indexOfStream;

	/**
	 * Dies ist die Anfrage, zu der dieser Stream geh�rt. Der Anfrage(_query) wird gemeldet, dass der Stream keine
	 * Archivdaten mehr vom Archiv empf�ngt (null-Paket) oder das der Benutzer den Stream mit abort abgebrochen hat.
	 */
	private final Query _query;

	private final ArchiveDataSpecification _archiveDataSpecification;

	/**
	 * DebugLogger f�r Debug-Ausgaben
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Objekt, dass einen Stream repr�sentiert.
	 *
	 * @param indexOfStream            Index des Streams
	 * @param streamDemultiplexer      Objekt �ber das Datens�tze angefordert werden k�nnen
	 * @param archiveDataSpecification Siehe Klassenbeschreibung
	 * @param query                    Archivanfrage, die zu diesem Objekt geh�rt
	 */
	public DataStream(int indexOfStream, StreamDemultiplexer streamDemultiplexer, ArchiveDataSpecification archiveDataSpecification, Query query) {
		_streamDemultiplexer = streamDemultiplexer;
		_indexOfStream = indexOfStream;
		_query = query;
		_archiveDataSpecification = archiveDataSpecification;
	}

	public ArchiveDataSpecification getDataSpecification() {
		return _archiveDataSpecification;
	}

	/**
	 * Diese Methode stellt einen archivierten Datensatz zur Verf�gung. Der Datensatz kann dabei vom Archiv gepackt worden
	 * sein, diese Methode wird den Datensatz entpacken.
	 *
	 * @return archivierter Datensatz
	 * @throws ClosedChannelException Die Verbindung zum DaV wurde unterbrochen
	 * @throws InterruptedException   Der Thread, der sich um die Verwaltung des Datensatzempfangs k�mmert wurde mit
	 *                                <code>Interrupt</code> abgebrochen
	 * @throws ProtocolException      Ein Datensatz wurde entweder doppelt empfangen oder fehlt
	 */
	public ArchiveData take() throws ClosedChannelException, InterruptedException, ProtocolException {

		// Das ArchiveData Objekt ist in dem Byte-Array kodiert.
		byte[] dataByteArray = _streamDemultiplexer.take(_indexOfStream);

		if (dataByteArray != null) {
			// Es wurde ein Datensatz empfangen
			InputStream in = new ByteArrayInputStream(dataByteArray);

			//deserialisieren
			Deserializer deserializer = SerializingFactory.createDeserializer(in);

			StreamedArchiveData streamedArchiveData = null;
			try {

				// archiveDataKind
				final int archiveDataKindCode = deserializer.readInt();
				ArchiveDataKind archiveDataKind = ArchiveDataKind.getInstance(archiveDataKindCode);

				// dataTime, steht in der Archivantwort
				final long dataTime = deserializer.readLong();

				// archiveTime, steht in der Archivantwort
				final long archiveTime = deserializer.readLong();

				// dataIndex, steht in der Archivantwort
				final long dataIndex = deserializer.readLong();

				// DataState, steht in der Archivantwort
				final int codeArchiveDataType = deserializer.readInt();
				final DataState dataState = DataState.getInstance(codeArchiveDataType);

				// Serializer-Version, mit dem der Datensatz erzeugt werden kann, auslesen
				final int serializerVersion = deserializer.readInt();

				// Wurde der Datensatz gepackt
				final byte byteCompression = deserializer.readByte();
				final ArchiveDataCompression compression = ArchiveDataCompression.getInstance(byteCompression);

				// Gr��e des Datensates, der als Byte-Array verschickt wurde. Ist die Gr��e 0, so
				// war auf Archivseite das byte-Array <code>null</code>.
				final int sizeOfData = deserializer.readInt();

				// Datensatzobjekt, das erzeugt werden soll. Der intitale Wert ist null, somit muss der else-Zweig von
				// <code>if(sizeOfData > 0)</code> nicht betrachtet werden, da dort nur data=null ausgef�hrt werden w�rde.
				Data data = null;

				if (dataState == DataState.DATA) {
					if (sizeOfData > 0) {
						// Der Datensatz in einem Byte-Array (dieser kann gepackt sein).
						// Wenn der Datensatz gepackt war, steht der entpackte Datensatz ebenfalls in dieser Variablen.
						byte[] byteData = deserializer.readBytes(sizeOfData);

						

						if (compression == ArchiveDataCompression.ZIP) {
							ByteArrayInputStream inputStream = new ByteArrayInputStream(byteData);
							InflaterInputStream unzip = new InflaterInputStream(inputStream);
							// In diesem Stream werden die entpackten Daten gespeichert
							ByteArrayOutputStream unzippedData = new ByteArrayOutputStream();

							try {
								// Speichert die ungepackten Daten
								byte[] byteArray = new byte[1000];

								// Ergebnis, nach dem die Daten eingelesen wurden (-1 bedeutet, dass es keine
								// Daten mehr gibt, die gepackt sind)
								int readResult = unzip.read(byteArray);

								while (readResult != -1) {
									unzippedData.write(byteArray, 0 , readResult);
									readResult = unzip.read(byteArray);
								}
								unzip.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

							// Der Datensatz wurde entpackt und kann deserialisiert werden. Das alte byte-Array wird an dieser
							// Stelle �berschrieben, da es nicht mehr ben�tigt wird.
							byteData = unzippedData.toByteArray();
						} else if (compression == ArchiveDataCompression.NONE) {
							// Alles in Ordnung, es wurde nicht gepackt
						} else {
							// Der Datensatz wurde mit einer unbekannte Version gepackt
							throw new RuntimeException("Entpacken von Datens�tzen nicht m�glich, da die Version des Packers nicht unterst�tzt wird, geforderte Version " + compression.toString());
						}

						// Das Byte-Array wird nun in einen Datensatz umgewandelt. Daf�r muss ein neuer Deserializer
						// erzeugt werden. Dieser benutzt die �bertragene Serializer-Version.
						// Dies ist n�tig, da gerade alte Archivdaten mit einer �lteren Serializer-Version
						// verpackt wurden.

						InputStream newUnpackedByteArray = new ByteArrayInputStream(byteData);

						//deserialisieren, diesmal mit einer anderen Serializer-Version
						try {
							Deserializer deserializerNewVersion = SerializingFactory.createDeserializer(serializerVersion, newUnpackedByteArray);
							data = deserializerNewVersion.readData(_archiveDataSpecification.getDataDescription().getAttributeGroup());
						} catch (NoSuchVersionException e) {
							e.printStackTrace();
							throw new IllegalStateException("Ein Archivdatensatz kann nicht deserialisiert werden, da das Archiv eine f�r die Applikation unbekannte Version zum serialisieren benutzt hat. Serializer-Version: " + serializerVersion);
						}
					}
				}

				// Objekt erzeugen, dies ist nun ein Archivdatensatz
				streamedArchiveData = new StreamedArchiveData(dataTime, archiveTime, dataIndex, dataState, archiveDataKind, data, _archiveDataSpecification.getObject(), _archiveDataSpecification.getDataDescription());

			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			return streamedArchiveData;
		} else {
			// Das null-Paket wurde empfangen, somit hat das Archiv alle Datens�tze versandt, die zu der Archivanfrage
			// geh�rten.
			_query.countFinishedStream();
			return null;
		}
	}

	/**
	 * Diese Methode wird aufgerufen, falls keine Datens�tze mehr ben�tigt werden.
	 */
	public void abort() {
		// Da der Stream mit abort beendet wurde, wird die Query dar�ber informiert.
		_query.countFinishedStream();
		_streamDemultiplexer.abort(_indexOfStream);
	}
}
