/*
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

package de.bsvrz.dav.daf.main.impl.archive.request;

import java.util.Map;
import java.util.HashMap;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.impl.NonQueueingReceiver;

/**
 * Diese Klasse verwaltet Archivantworten, die an unterschiedliche StreamedArchiveRequester gerichtet sind und sorgt daf�r, dass jeder StreamedArchivRequester
 * nur die Archivantworten bekommt, die f�r ihn sind. Somit werden die Antworten des Archivs nicht an alle StreamedArchiveRequester geschickt, sondern an diesen
 * Manager und dieser verteilt die Antworten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public class StreamedRequestManager {

	/**
	 * Hier werden alle StreamedArchiveRequester gespeichert, als Key wird das Archivsystem genommen, das die Daten versendet. Das Archivsystem tr�gt sich selber
	 * in den Datensatz ein den es verschickt. Somit kann der Datensatz, der empfangen wird, wieder einem StreamedArchiveRequester zugeordnet werden.
	 */
	private final Map _streamedArchiveRequesterMap;

	/** Connection, �ber die Archivdaten f�r die Empf�ngerapplikation �bertragen werden. */
	private final ClientDavInterface _connection;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final short _defaultSimulationVariant;


	public StreamedRequestManager(ClientDavInterface connection, short defaultSimulationVariant) {
		_connection = connection;
		_streamedArchiveRequesterMap = new HashMap();
		_defaultSimulationVariant = defaultSimulationVariant;
		try {
			subscribeReceiver();
		}
		catch(DataNotSubscribedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Diese Methode gibt ein StreamedArchiveRequester zur�ck, falls noch kein Objekt vorhanden ist, wird ein Objekt angelegt. Der StreamedArchiveRequester bekommt
	 * ein Archivsystem �bergeben und kann somit Daten anfordern
	 *
	 * @param archiveSystem Von diesem Archivsystem kann der StreamedArchiveRequester Daten anfordern
	 *
	 * @return StreamedArchiveRequester zum anfordern von Daten aus einem Archivsystem
	 */
	public StreamedArchiveRequester getArchiveRequester(SystemObject archiveSystem) {

		synchronized(_streamedArchiveRequesterMap) {
			StreamedArchiveRequester streamedArchiveRequester = (StreamedArchiveRequester)_streamedArchiveRequesterMap.get(archiveSystem);

			if(streamedArchiveRequester != null) {
				// Es gab schon einen streamedArchiveRequester f�r dieses Archivsystem
				return streamedArchiveRequester;
			}
			else {
				// Einen neuen streamedArchiveRequester anlegen, als Key dient das Archivsystem
				
				StreamedArchiveRequester newStreamedArchiveRequester = new StreamedArchiveRequester(
						_connection, 10000, archiveSystem, _defaultSimulationVariant
				);

				_streamedArchiveRequesterMap.put(archiveSystem, newStreamedArchiveRequester);
				return newStreamedArchiveRequester;
			}
		}
	}

	/**
	 * Diese Methode meldet das StreamedRequestManager Objekt als Empf�nger f�r Datens�tze an. Die Datens�tze werden vom Archive verschickt und sind f�r einen
	 * bestimmten StreamedArchiveRequester gedacht.
	 *
	 * @throws de.bsvrz.dav.daf.main.DataNotSubscribedException
	 *                                Senden von Datens�tzen ohne entsprechende Sendeanmeldungen
	 */
	private void subscribeReceiver() throws DataNotSubscribedException {

		// Konfiguration, alle Infos ueber das Objekt werden vom "Konfigurator" angefordert.
		final DataModel configuration = _connection.getDataModel();

		// Auf die Attributgruppe anmelden
		final AttributeGroup attributeGroup = configuration.getAttributeGroup("atg.archivAnfrageSchnittstelle");

		// In diesem Aspekt stehen die Antworten des Archivs, diese m�ssen empfangen werden
		final Aspect aspectReceiver = configuration.getAspect("asp.antwort");

		final short simulationVariant = 0;

		final DataDescription dataDescriptionReceiver = new DataDescription(attributeGroup, aspectReceiver, simulationVariant);

		ClientReceiver clientReceiver = new ClientReceiver();

		_connection.subscribeReceiver(
				clientReceiver, _connection.getLocalApplicationObject(), dataDescriptionReceiver, ReceiveOptions.normal(), ReceiverRole.drain()
		);
		_debug.info(
				"StreamRequestManager meldet sich als Empf�nger von Archivdaten an, Empf�nger: " + _connection.getLocalApplicationObject().getNameOrPidOrId()
		);
	}

	/**
	 * Diese Methode analysiert einen Datensatz und reicht diesen an den richtigen StreamedArchiveRequester weiter.
	 *
	 * @param data Datensatz eines Archivs
	 */
	private void receivingData(Data data) {

		// Welches Archiv hat den Datensatz verschickt
		final SystemObject archiveReference = data.getReferenceValue("absender").getSystemObject();

		// Das StreamedArchiveRequester Objekt suchen, dass diesen Datensatz haben m�chte
		StreamedArchiveRequester streamedArchiveRequester = getArchiveRequester(archiveReference);
		assert streamedArchiveRequester != null : "Es kamen Archivadaten f�r einen SAR, der noch gar nicht angemeldet wurde";
		streamedArchiveRequester.dataReceiver(data);
	}

	/**
	 * Diese Klasse implementiert ein ClientReceiverInterface. Dieses wird von der Klasse StreamedRequestManager ben�tigt um sich als Empf�nger von Datens�tzen
	 * anzumelden, die vom Archiv versendet werden und f�r die StreamedArchiveRequester Objekte gedacht sind. TBD NonQueueingReceiver kann deadlock erzeugen !!
	 * drauf achten
	 */
	private final class ClientReceiver implements ClientReceiverInterface, NonQueueingReceiver {

		/**
		 * Diese Klasse wird vom DaV aufgerufen sobald Datens�tze zur Verf�gung stehen, auf die sich dieses Objekt als Empf�nger angemeldet hat.
		 *
		 * @param results Datens�tze, die empfangen wurden
		 */
		public void update(ResultData results[]) {
			for(int i = 0; i < results.length; i++) {
				_debug.finer("StreamedRequestManager erh�lt Datensatz");

				Data data = results[i].getData();
				if(data != null) {
					receivingData(data);
				}
				else {
					_debug.finer("StreamedRequestManager erh�lt leerern Datensatz");
				}
			}
		}
	}
}
