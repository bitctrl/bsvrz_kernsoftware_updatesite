/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.NonQueueingReceiver;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Klasse, die sich auf Antworten anderer Konfigurationen anmeldet und diese an die jeweils intern angemeldeten Objekte weiterleitet
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9184 $
 */
public class ForeignConfigReceiveCommunicator {
	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final ClientDavInterface _connection;

	private final SystemObject _receiverObject;

	private DataState _dataStateReceiver = DataState.INVALID_SUBSCRIPTION;

	/** F�r Antworten */
	private final DataDescription _responseDescription;

	private ClientReceiverInterface _receiver;

	private CopyOnWriteArraySet<ClientReceiverInterface> _receivers = new CopyOnWriteArraySet<ClientReceiverInterface>();

	protected ForeignConfigReceiveCommunicator(ClientDavInterface connection, SystemObject localConfig, DataDescription responseDescription) {
		_connection = connection;
		_receiverObject = localConfig;
		_responseDescription = responseDescription;
	}

	public String toString() {
		return "ForeignConfigReceiveCommunicator{"
		       + "_receiverObject: " + _receiverObject + ", _responseDescription: " + _responseDescription + ", "
		       + ", _dataStateReceiver: " + _dataStateReceiver
		       + "}";
	}

	public void start() {
		try {
			_receiver = new ForeignConfigReceiveCommunicator.AnswerReceiver();

			_debug.fine("Anmeldung als Senke Objekt " + _receiverObject + " Datenidentifikation " + _responseDescription);
			_connection.subscribeReceiver(_receiver, _receiverObject, _responseDescription, ReceiveOptions.normal(), ReceiverRole.drain());
		}
		catch(Exception e) {
			_debug.warning("ForeignConfigReceiveCommunicator konnte nicht gestartet werden", e);
		}
	}

	public void close() {
		_connection.unsubscribeReceiver(_receiver, _receiverObject, _responseDescription);
	}

	public void removeListener(final ClientReceiverInterface receiver) {
		_receivers.remove(receiver);
	}

	public void addListener(final ClientReceiverInterface receiver) {
		_receivers.add(receiver);
	}


	public DataState getDataStateReceiver() {
		return _dataStateReceiver;
	}

	/** Receiverklasse, die Anworten der Konfiguration verarbeitet */
	private class AnswerReceiver implements ClientReceiverInterface, NonQueueingReceiver {

		public void update(ResultData results[]) {
			for(int i = 0; i < results.length; i++) {
				ResultData result = results[i];
				final DataState dataState = result.getDataState();
				_dataStateReceiver = dataState;
			}
//			System.out.println("Antworten einer anderen Konfiguration: " + Arrays.toString(results));
			for(ClientReceiverInterface receiver : _receivers) {
				receiver.update(results);
			}
		}

	}

}
