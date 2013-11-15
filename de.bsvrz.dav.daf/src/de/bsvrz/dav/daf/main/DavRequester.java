/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Klasse zur Kommunikations mit dem Datenverteiler. Wird derzeit nur für Anmeldungen von Transaktionsquellen/Senken benutzt, ist aber erweiterbar.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8953 $
 */
public abstract class DavRequester {

	protected static final Debug _debug = Debug.getLogger();

	protected static final int SUBSCRIBE_TRANSMITTER_SOURCE = 1;

	protected static final int SUBSCRIBE_TRANSMITTER_DRAIN = 2;

	protected static final int ANSWER_OK = 1000;

	protected static final int ANSWER_ERROR = 1001;

	protected final ClientDavConnection _connection;

	protected final AttributeGroup _attributeGroup;

	protected final Aspect _receiveAspect;

	protected final Aspect _sendAspect;

	private final Map<SystemObject, Sender> _senderMap = new HashMap<SystemObject, Sender>();

	/**
	 * Erzeugt einen neuen DavRequester
	 * @param connection Verbindung zum Datenverteiler
	 * @param sendAspect Sende-Aspekt
	 * @param receiveAspect Empfangs-Aspekt
	 */
	public DavRequester(
			final ClientDavConnection connection, final Aspect sendAspect, final Aspect receiveAspect) {
		_connection = connection;
		_sendAspect = sendAspect;
		_receiveAspect = receiveAspect;

		_attributeGroup = connection.getDataModel().getAttributeGroup("atg.datenverteilerSchnittstelle");
	}

	/**
	 * Initialisiert den Dav-Requester und meldet sich als Senke für Nachrichten an.
	 * @param object Applikation auf die sich angemeldet werden soll
	 */
	protected void subscribeDrain(final SystemObject object) {
		if (_attributeGroup == null || _receiveAspect == null || _sendAspect == null) {
			// Schnittstelle im Datenmodell nicht vorhanden
			return;
		}
		_connection.subscribeReceiver(
				new Receiver(), object, new DataDescription(_attributeGroup, _receiveAspect), ReceiveOptions.normal(), ReceiverRole.drain()
		);
	}

	/**
	 * Wird beim Empfang von Daten aufgerufen
	 * @param data Daten
	 */
	protected abstract void onReceive(Data data);

	/**
	 * Sendet eine Anfrage mit einer Fehlernachricht
	 * @param target Ziel-Systemobjekt
	 * @param requestId Anfrage-ID
	 * @param errorString Fehlermeldung
	 * @param senderObject Eigenes Systemobjekt
	 */
	protected void sendError(
			final SystemObject target, final long requestId, final String errorString, final SystemObject senderObject) {
		try {
			Sender sender = _senderMap.get(target);
			if(sender == null || !sender.isRunning()) {
				final Sender newSender = new Sender(target);
				_senderMap.put(target, newSender);
				sender = newSender;
			}
			sender.send(getData(requestId, errorString, senderObject));
			waitUntilSent(sender);
		}
		catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
			_debug.warning("Kann Antwort nicht senden", oneSubscriptionPerSendData);
		}
	}

	/**
	 * Sendet eine Anfrage mit einem byte-Array als Daten
	 * @param target Ziel-Systemobjekt
	 * @param requestId Anfrage-ID
	 * @param answerKind Nachrichtentyp
	 * @param data Daten
	 * @param senderObject Eigenes Systemobjekt
	 */
	protected void sendBytes(final SystemObject target, final long requestId, final long answerKind, final byte[] data, final SystemObject senderObject) {
		try {
			Sender sender = _senderMap.get(target);
			if(sender == null || !sender.isRunning()) {
				final Sender newSender = new Sender(target);
				_senderMap.put(target, newSender);
				sender = newSender;
			}
			sender.send(getData(requestId, answerKind, data, senderObject));
			waitUntilSent(sender);
		}
		catch(OneSubscriptionPerSendData oneSubscriptionPerSendData) {
			_debug.warning("Kann Nachricht nicht senden", oneSubscriptionPerSendData);
		}
	}

	private void waitUntilSent(final Sender sender) {
		final long startTime = System.currentTimeMillis();
		synchronized(sender){
			while(!sender.hasSentData()){
				try {
					sender.wait(1000);
					if(System.currentTimeMillis() - startTime > 60 * 1000) {
						_debug.warning("Kann Nachricht nicht senden: Timeout beim Warten auf Sendesteuerung; " + sender);
					}
				}
				catch(InterruptedException ignored) {
				}
			}
		}
	}


	private Data getData(final long requestId, final String errorString, final SystemObject senderObject) {
		final byte[] bytes;
		if(errorString != null) {
			bytes = errorString.getBytes();
		}
		else{
			bytes = new byte[0];
		}
		return getData(requestId, ANSWER_ERROR, bytes, senderObject);
	}

	private Data getData(final long requestId, final long answerKind, final byte[] bytes, final SystemObject senderObject) {
		final Data data = _connection.createData(_attributeGroup);
		data.getReferenceValue("Absender").setSystemObject(senderObject);
		data.getUnscaledValue("AnfrageIndex").set(requestId);
		data.getUnscaledValue("AnfrageTyp").set(answerKind);
		data.getUnscaledArray("Daten").set(bytes);
		return data;
	}

	private class Receiver implements ClientReceiverInterface {

		public void update(final ResultData[] results) {
			for(final ResultData result : results) {
				if(result.hasData()) {
					onReceive(result.getData());
				}
			}
		}
	}

	private final class Sender implements ClientSenderInterface {

		private final Queue<Data> _queue = new LinkedList<Data>();

		private volatile byte _state = (byte)-1;

		private final SystemObject _object;

		private final DataDescription _dataDescription;

		private boolean _running = true;

		public Sender(final SystemObject object) throws OneSubscriptionPerSendData {
			_object = object;
			_dataDescription = new DataDescription(_attributeGroup, _sendAspect);
			_connection.subscribeSender(this, _object, _dataDescription, SenderRole.sender());
			final Thread thread = new Thread(new QueueHandler());
			thread.setDaemon(true);
			thread.setName("DavRequesterSendQueue");
			thread.start();
		}

		/**
		 * Sendesteuerung des Datenverteilers an die Applikation. Diese Methode muss von der Applikation implementiert werden, um den Versand von Daten zu starten bzw.
		 * anzuhalten. Der Datenverteiler signalisiert damit einer Quelle oder einem Sender dass mindestens ein Abnehmer bzw. kein Abnehmer mehr für die zuvor
		 * angemeldeten Daten vorhanden ist. Die Quelle wird damit aufgefordert den Versand von Daten zu starten bzw. zu stoppen.
		 *
		 * @param object          Das in der zugehörigen Sendeanmeldung angegebene Objekt, auf das sich die Sendesteuerung bezieht.
		 * @param dataDescription Beschreibende Informationen zu den angemeldeten Daten auf die sich die Sendesteuerung bezieht.
		 * @param state           Status der Sendesteuerung. Kann einen der Werte <code>START_SENDING</code>, <code>STOP_SENDING</code>,
		 *                        <code>STOP_SENDING_NO_RIGHTS</code>, <code>STOP_SENDING_NOT_A_VALID_SUBSCRIPTION</code> enthalten.
		 *
		 * @see #START_SENDING
		 * @see #STOP_SENDING
		 * @see #STOP_SENDING_NO_RIGHTS
		 * @see #STOP_SENDING_NOT_A_VALID_SUBSCRIPTION
		 */
		public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
			synchronized(_queue){
				_state = state;
				_queue.notifyAll();
			}
		}

		/**
		 * Diese Methode muss von der Applikation implementiert werden, um zu signalisieren, ob Sendesteuerungen erwünscht sind und mit der Methode
		 * <code>dataRequest</code> verarbeitet werden. In der Implementierung dieser Methode dürfen keine synchronen Aufrufe, die auf Telegramme vom Datenverteiler
		 * warten (wie z.B. Konfigurationsanfragen) durchgeführt werden, da ansonsten ein Deadlock entsteht.
		 *
		 * @param object          Das in der zugehörigen Sendeanmeldung angegebene System-Objekt.
		 * @param dataDescription Die in der zugehörigen Sendeanmeldung angegebenen beschreibenden Informationen der angemeldeten Daten.
		 *
		 * @return <code>true</code>, falls Sendesteuerungen gewünscht sind, sonst <code>false</code>.
		 *
		 * @see #dataRequest
		 */
		public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
			return true;
		}

		public void send(final Data data) {
			synchronized(_queue) {
				_queue.add(data);
				_queue.notifyAll();
			}
		}

		public void stop() {
			synchronized(_queue) {
				_running = false;
				_queue.notifyAll();
			}
		}

		public boolean hasSentData() {
			synchronized(_queue) {
				return _queue.isEmpty();
			}
		}

		private class QueueHandler implements Runnable {
			public void run() {
				synchronized(_queue) {
					while(true) {
						while((_state != ClientSenderInterface.START_SENDING || _queue.isEmpty()) && _running) {
							try {
								_queue.wait(2000);
							}
							catch(InterruptedException ignored) {
							}
						}
						if(!_running) return;
						try {
							_connection.sendData(new ResultData(_object, _dataDescription, System.currentTimeMillis(), _queue.peek()));
							synchronized(this) {
								_queue.poll();
								notifyAll();
							}
						}
						catch(SendSubscriptionNotConfirmed ignored) {
						}
					}
				}
			}
		}

		public boolean isRunning() {
			synchronized(_queue) {
				return _running;
			}
		}

		@Override
		public String toString() {
			return "Sender{" + "_queue=" + _queue + ", _state=" + _state + ", _object=" + _object + ", _dataDescription=" + _dataDescription + "_running=" + isRunning() + '}';
		}
	}
}
