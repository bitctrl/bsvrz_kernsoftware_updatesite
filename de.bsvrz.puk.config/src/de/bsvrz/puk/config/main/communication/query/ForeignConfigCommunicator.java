/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main.communication.query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.NonQueueingReceiver;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ForeignConfigCommunicator {
	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private ForeignConfigReceiveCommunicator _foreignConfigReceiveCommunicator;

	public interface CommunicationHandler {
		void communicationStateChanged(boolean connected);
		void answerReceived(Data data);
	}

	private final static class SenderState {

		public final static SenderState NOT_YET_CONNECTED = new SenderState("Noch nicht verbunden");

		public final static SenderState CONNECTED = new SenderState("Verbunden");

		public final static SenderState DISCONNECTED = new SenderState("Nicht mehr verbunden");

		public final static SenderState ERROR = new SenderState("Fehler");

		public String toString() {
			return _name;
		}

		private String _name;

		private SenderState(String name) {
			_name = name;
		}
	}

	private final CommunicationHandler _communicationHandler;

	private final ClientDavInterface _connection;

	private final SystemObject _senderObject;

	private DataState _dataStateReceiver = DataState.INVALID_SUBSCRIPTION;

	/** Für Anfragen */
	private final DataDescription _requestDescription;

	private SenderState _senderState = SenderState.NOT_YET_CONNECTED;

	private ClientSenderInterface _sender;

	private ClientReceiverInterface _receiver;

	protected ForeignConfigCommunicator(
			CommunicationHandler communicationHandler, ClientDavInterface connection, SystemObject foreignConfig, DataDescription requestDescription, final ForeignConfigReceiveCommunicator foreignConfigReceiveCommunicator) {
		_foreignConfigReceiveCommunicator = foreignConfigReceiveCommunicator;
		_communicationHandler = communicationHandler;
		_connection = connection;
		_senderObject = foreignConfig;
		_requestDescription = requestDescription;
	}

	public String toString() {
		return "ForeignConfigCommunicator{"
		       + "_senderObject: " + _senderObject + ", _requestDescription: " + _requestDescription + ", "
		       + "_senderState: " + _senderState + ", _dataStateReceiver: " + _dataStateReceiver
		       + "}";
	}

	public void start() {
		try {
			_sender = new RequestSender();

			_receiver = new AnswerReceiver();
			synchronized(ForeignConfigCommunicator.this) {
				_foreignConfigReceiveCommunicator.addListener(_receiver);
				_dataStateReceiver = _foreignConfigReceiveCommunicator.getDataStateReceiver();
			}
			_debug.fine("Anmeldung als Sender Objekt " + _senderObject + " Datenidentifikation " + _requestDescription);
			_connection.subscribeSender(_sender, _senderObject, _requestDescription, SenderRole.sender());

		}
		catch(Exception e) {
			_debug.warning("ForeignConfigCommunicator konnte nicht gestartet werden", e);
		}
	}


	public void sendData(final Data requestData) throws IllegalStateException {
//		System.out.println("Sende Anfrage an andere Konfiguration" + requestData);
		synchronized(ForeignConfigCommunicator.this) {
			if(_dataStateReceiver == DataState.NO_RIGHTS) {
				throw new IllegalStateException("Keine Rechte zum Empfang von Antworten: " + this);
			}
			if(_senderState != SenderState.CONNECTED) {
				throw new IllegalStateException("Keine positive Sendesteuerung: " + this);
			}
		}
		_debug.finer("Senden", requestData);
		try {
			_connection.sendData( new ResultData( _senderObject, _requestDescription, System.currentTimeMillis(), requestData));
		}
		catch(SendSubscriptionNotConfirmed e) {
			throw new IllegalStateException("Fehler beim Senden: " + this, e);
		}
	}


	public boolean isConnected() {
		synchronized(ForeignConfigCommunicator.this) {
			return _senderState == SenderState.CONNECTED && _dataStateReceiver != DataState.NO_RIGHTS && _dataStateReceiver != DataState.INVALID_SUBSCRIPTION;
		}
	}

	public void close() {
		_connection.unsubscribeSender(_sender, _senderObject, _requestDescription);
		_foreignConfigReceiveCommunicator.removeListener(_receiver);
	}


	/** Receiverklasse, die Anworten der Konfiguration verarbeitet */
	private class AnswerReceiver implements ClientReceiverInterface, NonQueueingReceiver {

		public void update(ResultData results[]) {
			for(int i = 0; i < results.length; i++) {
				ResultData result = results[i];
				_debug.finer("Konfigurationsantwort erhalten", result.getData());
				final DataState dataState = result.getDataState();
				setDataStateReceiver(dataState);
				try {
					final Data data = result.getData();
					if(data == null || !result.hasData()) {
						_debug.fine("leerer Datensatz erhalten", data);
					}
					else {
						if(data.getReferenceValue("absender").getSystemObject().equals(_senderObject)) {
							_debug.finer("Empfangen", data);
							_communicationHandler.answerReceived(data);
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					_debug.warning("Antwort konnte nicht interpretiert werden", e);
				}
			}
		}

		private void setDataStateReceiver(final DataState dataState) {
			final boolean wasConnected;
			final boolean isConnected;
			synchronized(ForeignConfigCommunicator.this) {
				wasConnected = isConnected();
				_dataStateReceiver = dataState;
				isConnected = isConnected();
				if(wasConnected ^ isConnected) _communicationHandler.communicationStateChanged(isConnected);
			}
		}
	}

	/** Callback-Klasse für Sendeanmeldung der Anfragen */
	private class RequestSender implements ClientSenderInterface {

		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			_debug.finer("RequestSender Sendesteuerung " + state);
			final boolean wasConnected;
			final boolean isConnected;
			synchronized(ForeignConfigCommunicator.this) {
				wasConnected = isConnected();
				switch(state) {
					case START_SENDING:
						_senderState = SenderState.CONNECTED;
						break;
					case STOP_SENDING:
						if(_senderState != SenderState.NOT_YET_CONNECTED) _senderState = SenderState.DISCONNECTED;
						break;
					
					default:
						_senderState = SenderState.ERROR;
				}
				isConnected = isConnected();
				if(wasConnected ^ isConnected) _communicationHandler.communicationStateChanged(isConnected);
			}
		}

		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}
}
