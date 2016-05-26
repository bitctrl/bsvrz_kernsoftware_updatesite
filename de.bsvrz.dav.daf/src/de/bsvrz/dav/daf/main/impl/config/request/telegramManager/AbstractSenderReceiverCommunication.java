/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config.request.telegramManager;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.daf.main.impl.NonQueueingReceiver;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Bietet eine Kommunikation mit einem Sender und einer Senke. Der Sender verschickt Aufträge, die Antworten auf diese Aufträge werden dann durch die Quelle
 * empfangen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class AbstractSenderReceiverCommunication implements SenderReceiverCommunication {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final ClientDavInterface _connection;

	private final SystemObject _senderObject;

	private final SystemObject _receiverObject;

	/**
	 * Objekt, das Telegramm verarbeiten kann und dieses dann aus dem Strom der Telegramme entfernen kann. Ist das Objekt <code>null</code>, werden die Telegramme
	 * normal durch diese Klasse bearbeitet.
	 */
	private DataListener _dataListener = null;

	/**
	 * Beschreibt ob der Empfänger für Antworten für Konfigurationsanfragen diese Antworten auch empfangen darf. Dürfen keine antworten empfangen werden, weil die
	 * Rechte nicht gesetzt wurde, so darf auch keine Anfrage gesendet werden (da die Antwort der Konfigurations niemals empfangen werden könnte und ewig auf die
	 * Antwort gewartet werden würde).
	 * <p>
	 * Der default DataState.DATA ist zufällig gewählt und muss nur ungleich DataState.NO_RIGHTS sein.
	 */
	private DataState _dataStateReceiver = DataState.DATA;

	/** Für Anfragen */
	private DataDescription _requestDescription;

	/** Für Antworten, kann <code>null</code> bleiben, wenn keine Antworten benötigt werden */
	private DataDescription _responseDescription = null;

	private final Object _monitor = new Object();

	private int _requestIndex = 0;

	private ConnectionState _connectionState = ConnectionState.NotYetConnected;

	private boolean _subscribeReceiver;


	/** Hier werden die Antworten von der Konfiguration abgelegt, die dann zur weiteren Verarbeitung von der Methode "waitForReply" wieder herausgenommen werden. */
	private List<Data> _replyList = new LinkedList<Data>();

	/** Wird auf true gesetzt, wenn die Verbindung zum Datenverteiler geschlossen wurde */
	private boolean _closed = false;

	private ClientSenderInterface _requester;

	private ClientReceiverInterface _receiver;

	/** Verwaltet die angemeldeten Senken */
	private static final Map<DataIdent, DrainSubscription> _drainSubscriptions = new HashMap<DataIdent, DrainSubscription>();

	/**
	 * @param connection    Verbindung zum DaV
	 * @param senderObject  Objekt, zum anmelden für Sendeaufträge
	 * @param ordererObject Objekt, zum anmelden als Senke (siehe {@link #init}). Dieses Objekt wird beim versenden auch als "Absender" eingetragen. Dadurch weiss
	 *                      die Empfangende Applikation (senderObjekt), wohin die Antwort muss(wenn eine Senke angemeldet wurde) bzw. wer die Antwort verschickt
	 *                      hat.
	 */
	protected AbstractSenderReceiverCommunication(ClientDavInterface connection, SystemObject senderObject, SystemObject ordererObject) {
		if(senderObject == null) throw new IllegalArgumentException("senderObject ist null");
		if(ordererObject == null) throw new IllegalArgumentException("receiverObject ist null");
		_connection = connection;
		_senderObject = senderObject;
		_receiverObject = ordererObject;
	}

	/**
	 * Meldet einen Sender und eine Senke auf die übergebenen Parameter an, wenn alle vier Parameter gesetzt sind. Als Simulationsvariante wird die beim starten
	 * der Applikation gesetzt Variante benutzt.
	 * <p>
	 * Sind die Parameter responseAtg und responseAspect <code>null</code>, wird keine Senke angemeldet sondern nur der Sender.
	 *
	 * @param requestAtg     ATG für Anfrage (Anmeldung als Sender)
	 * @param requestAspect  Aspekt für Anfragen (Anmeldung für Sender)
	 * @param responseAtg    ATG für Antworten auf Anfragen (Anmeldung als Senke) oder <code>null</code>, wenn kein Senke angemeldet werden soll.
	 * @param responseAspect Aspekt für Antworten auf Anfragen (Anmeldung als Senke) oder <code>null</code>, wenn kein Senke angemeldet werden soll.
	 * @param dataListener Objekt, das Telegramme verarbeiten und diese dann aus dem Strom der Telegramme entfernen kann. Ist das Objekt <code>null</code>,
	 * werden die Telegramme nicht gefiltert und normal durch diese Klasse bearbeitet.

	 *
	 * @throws OneSubscriptionPerSendData Wenn bereits eine Senke für die gleichen Daten angemeldet wurde.
	 */
	public void init(AttributeGroup requestAtg, Aspect requestAspect, AttributeGroup responseAtg, Aspect responseAspect, DataListener dataListener)
			throws OneSubscriptionPerSendData {
		init(requestAtg, requestAspect, responseAtg, responseAspect, DataDescription.NO_SIMULATION_VARIANT_SET, dataListener);
	}

	/**
	 * Meldet einen Sender und eine Senke auf die übergebenen Parameter an, wenn alle vier Parameter gesetzt sind. Die zu nutzende Simulationsvariante wird
	 * übergeben.
	 * <p>
	 * Sind die Parameter responseAtg und responseAspect <code>null</code>, wird keine Senke angemeldet sondern nur der Sender.
	 *
	 * @param requestAtg        ATG für Anfrage (Anmeldung als Sender)
	 * @param requestAspect     Aspekt für Anfragen (Anmeldung für Sender)
	 * @param responseAtg       ATG für Antworten auf Anfragen (Anmeldung als Senke) oder <code>null</code>, wenn kein Senke angemeldet werden soll.
	 * @param responseAspect    Aspekt für Antworten auf Anfragen (Anmeldung als Senke) oder <code>null</code>, wenn kein Senke angemeldet werden soll.
	 * @param simulationVariant Simulationsvariante, die zur Anmeldung benutzt werden soll
	 * @param dataListener Objekt, das Telegramme verarbeiten und diese dann aus dem Strom der Telegramme entfernen kann. Ist das Objekt <code>null</code>,
	 * werden die Telegramme nicht gefiltert und normal durch diese Klasse bearbeitet.

	 *
	 * @throws OneSubscriptionPerSendData Wenn bereits eine Senke für die gleichen Daten angemeldet wurde.
	 */
	public void init(
			AttributeGroup requestAtg,
			Aspect requestAspect,
			AttributeGroup responseAtg,
			Aspect responseAspect,
			short simulationVariant,
			DataListener dataListener
	) throws OneSubscriptionPerSendData {

		DavConnectionListener listener = new DavConnectionListener() {
			@Override
			public void connectionClosed(final ClientDavInterface connection) {
				synchronized(_monitor) {
					_connectionState = ConnectionState.DavConnectionLost;
					_monitor.notifyAll();
				}
			}
		};
		_connection.addConnectionListener(listener);
		
		// Sender von Aufträgen
		_requestDescription = new DataDescription(requestAtg, requestAspect, simulationVariant);
		_dataListener = dataListener;

		_requester = new RequestSender();
		_debug.fine("Anmeldung als Sender Objekt " + _senderObject + " Datenidentifikation " + _requestDescription);

		_connection.subscribeSender(_requester, _senderObject, _requestDescription, SenderRole.sender());

		if(responseAtg != null && responseAspect != null) {
			// Senke für Antworten
			_responseDescription = new DataDescription(responseAtg, responseAspect, simulationVariant);
			_receiver = new AnswerReceiver();
			final DrainSubscription drain;
			synchronized(_drainSubscriptions) {
				DataIdent dataIdent = new DataIdent(_connection, _receiverObject, _responseDescription);
				DrainSubscription drainSubscription = _drainSubscriptions.get(dataIdent);
				if(drainSubscription == null){
					drainSubscription = new DrainSubscription(_connection, _receiverObject, _responseDescription);
					_drainSubscriptions.put(dataIdent, drainSubscription);
				}
				drain = drainSubscription;
				drain.subscribeReceiver(_receiver);
			}
			_subscribeReceiver = true;
			_debug.finer("Anmeldung als Senke Objekt " + _receiverObject + " Datenidentifikation " + _responseDescription);
		}
		else {
			_subscribeReceiver = false;
		}
	}


	public int sendData(String messageType, byte[] data) throws SendSubscriptionNotConfirmed, IllegalStateException {
		int requestIndex;
		synchronized(this) {
			if(_requestIndex == 0) _requestIndex = 1;
			requestIndex = _requestIndex++;
		}
		sendData(messageType, data, requestIndex);
		return requestIndex;
	}

	public void sendData(String messageType, byte[] data, int queryIndex) throws SendSubscriptionNotConfirmed, IllegalStateException {
		if(_dataStateReceiver != DataState.NO_RIGHTS) {

			synchronized(_monitor) {
				// warten bis die Sendesteuerung eindeutig gesetzt ist
				while(_connectionState == ConnectionState.NotYetConnected) {
					try {
						_monitor.wait();
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if(_connectionState == ConnectionState.Connected) {

				final Data requestData = createRequestData(messageType, data, queryIndex);
				_debug.finer("Senden", messageType);
//				_debug.finest("Senden", requestData);
				_connection.sendData(
						new ResultData( _senderObject, _requestDescription, System.currentTimeMillis(), requestData)
				);
			}
			else {
				throw new SendSubscriptionNotConfirmed(
						"Keine Positive Sendesteuerung: " + _connectionState + " Datenidentifikation Anfrage: " + _requestDescription
						+ " Datenidentifikation Antwort: " + _responseDescription
				);
			}
		}
		else {
			throw new IllegalStateException(
					"Eine Konfigurationsanfrage mit Datenidentifikation " + _requestDescription
					+ " konnte nicht ausgeführt werden, weil für den Empfang der Antwort mit Datenidentifikation " + _responseDescription
					+ " keine Rechte zum empfang vorlagen. Systemobjekt Konfiguration: " + _senderObject + " Systemobjekt Applikation " + _receiverObject
					+ " Nachrichtentyp, der verschickt werden sollte " + messageType
			);
		}
	}

	private Data createRequestData(String messageType, byte[] message, int requestIndex) {
		final AttributeGroup attributeGroup = _requestDescription.getAttributeGroup();
		Data data = AttributeBaseValueDataFactory.createAdapter(attributeGroup, AttributeHelper.getAttributesValues(_requestDescription.getAttributeGroup()));

		// Der Absender des Telegramms ist das Objekt, dass sich als Empfänger auf die Daten angemeldet hat
		data.getReferenceValue("absender").setSystemObject(_receiverObject);

		data.getScaledValue("anfrageIndex").set(requestIndex);
		data.getScaledValue("nachrichtenTyp").setText(messageType);
		data.getUnscaledArray("daten").set(message);
		return data;
	}

	public void handleReply(Data data) {
		synchronized(_replyList) {
			_replyList.add(data);
			_replyList.notifyAll();
		}
	}

	public Data waitForReply(int requestIndex) throws RequestException {
		if(_subscribeReceiver) {
			Data reply = null;
			synchronized(_replyList) {
				long waitTime = CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE;
				try {
					while(reply == null) {
						if(_closed) throw new RequestException("Verbindung zum Datenverteiler wurde terminiert");
						for(Iterator iterator = _replyList.iterator(); iterator.hasNext();) {
							Data data = (Data)iterator.next();
							if(data.getScaledValue("anfrageIndex").intValue() == requestIndex) {
								reply = data;
								break;
							}
						}
						if(reply == null) {
							if(waitTime <= 0){
								throw new RuntimeException("Die Konfiguration antwortet nicht");
							}
							long startTime = System.nanoTime();
							_replyList.wait(waitTime);
							long waitedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
							if(waitedTime > 0){
								waitTime -= waitedTime;
							}
						}
					}
					_replyList.remove(reply);
				}
				catch(InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException(e); 
				}
			}
			return reply;
		}
		else {
			// Es wurde gar kein Empfänger für Nachrichten angemeldet. Also wird nie eine Antwort kommen
			throw new IllegalStateException("Es wurde keine Senke für Antworten angemeldet.");
		}
	}

	public ConnectionState getConnectionState() {
		synchronized(_monitor) {
			return _connectionState;
		}
	}

	public void close() {
		synchronized(_replyList) {
			_closed = true;
			_replyList.notifyAll();
		}
		_connection.unsubscribeSender(_requester, _senderObject, _requestDescription);
		if(_receiver != null) {
			synchronized(_drainSubscriptions) {
				DataIdent dataIdent = new DataIdent(_connection, _receiverObject, _responseDescription);
				DrainSubscription drainSubscription = _drainSubscriptions.get(dataIdent);
				if(drainSubscription != null){
					drainSubscription.unsubscribeReceiver(_receiver);
					if(drainSubscription._receivers.isEmpty()){
						_drainSubscriptions.remove(dataIdent);
					}
				}
			}
		}

		if(_dataListener != null) {
			_dataListener.close();
		}
	}


	/** Receiverklasse, die Anworten der Konfiguration verarbeitet */
	private class AnswerReceiver implements ClientReceiverInterface, NonQueueingReceiver {

		/**
		 * Nimmt Antworten der Konfiguration entgegen.
		 *
		 * @param results Empfangene Datensätze.
		 */
		public void update(ResultData results[]) {
			for(int i = 0; i < results.length; i++) {
				ResultData result = results[i];
				_debug.finer("Konfigurationsantwort erhalten");
				//_debug.finest("Konfigurationsantwort erhalten", result.getData());

				// Es dürfen keine Antworten empfangen werden, da keine Rechte gesetzt wurde.
				// Also dürfen auch keine Anfragen verschickt werden, da die Antwort der Konfiguration
				// niemals empfangen werden könnte.
				if(result.getDataState() == DataState.NO_RIGHTS) {
					// Es gibt keine Rechte, also versenden von Daten verhindern
					_dataStateReceiver = DataState.NO_RIGHTS;
				}
				else if(_dataStateReceiver == DataState.NO_RIGHTS) {
					// Die Variable steht auf "nicht senden", aber ein neuer Datensatz erlaubt das
					// versenden von Daten. Also den Zustand ändern.
					// Hatte die Variable vorher einen anderen Zustand, muss dieser nicht angepaßt werden.
					_dataStateReceiver = result.getDataState();
				}

				try {
					final Data data = result.getData();
					if(data == null || !result.hasData()) {
						_debug.fine("leerer Datensatz erhalten", data);
					}
					else {
						long replySenderId = data.getReferenceValue("absender").getId();

						if(replySenderId != _senderObject.getId()) {
							_debug.fine("Falscher Empfänger", replySenderId);
							continue;
						}

						final boolean processTelegram;
						_debug.finer("Empfangen von", replySenderId);
						if(_dataListener != null) {
							// Soll das Telegramm vielleicht woanders bearbeitet werden und nicht durch
							// den normalen Mechanismus ?
							processTelegram = _dataListener.messageReceived(data);
						}
						else {
							// Es gibt kein Objekt, das Telegramm aus dem Telegramm-Strom herausziehen möchte
							processTelegram = true;
						}

						if(processTelegram) {
							handleReply(data);
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					_debug.warning("Antwort auf eine Konfigurationsanfrage konnte nicht interpretiert werden", e);
				}
			}
		}
	}

	/** Callback-Klasse für Sendeanmeldung der Anfragen */
	private class RequestSender implements ClientSenderInterface {

		/**
		 * Sendesteuerung des Datenverteilers an die Applikation. Diese Methode muss von der Applikation implementiert werden, um den Versand von Daten zu starten
		 * bzw. anzuhalten. Der Datenverteiler signalisiert damit einer Quelle oder einem Sender dass mindestens ein Abnehmer bzw. kein Abnehmer mehr für die zuvor
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
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			_debug.finer("RequestSender Sendesteuerung" , state);
			synchronized(_monitor) {
				if(_connectionState == ConnectionState.DavConnectionLost){
					_debug.finer("Ignoriere Sendesteuerung, Datenverteilerverbindung verloren");
					return;
				}
				switch(state) {
					case START_SENDING:
						_connectionState = ConnectionState.Connected;
						break;
					case STOP_SENDING:
						if(_connectionState != ConnectionState.NotYetConnected) _connectionState = ConnectionState.Disconnected;
						break;
					default:
						_connectionState = ConnectionState.Error;
				}
				_monitor.notifyAll();
			}
		}

		/**
		 * Diese Methode muss von der Applikation implementiert werden, um zu signalisieren, ob Sendesteuerungen erwünscht sind und mit der Methode
		 * <code>dataRequest</code> verarbeitet werden.
		 *
		 * @param object          Das in der zugehörigen Sendeanmeldung angegebene System-Objekt.
		 * @param dataDescription Die in der zugehörigen Sendeanmeldung angegebenen beschreibenden Informationen der angemeldeten Daten.
		 *
		 * @return <code>true</code>, falls Sendesteuerungen gewünscht sind, sonst <code>false</code>.
		 *
		 * @see #dataRequest
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}

	/**
	 * Dieses Interface ermöglich es Daten, die durch eine Instanz der Klasse {@link AbstractSenderReceiverCommunication} empfangen wurde, zu verarbeiten, bevor
	 * diese durch den normalen Mechanismus bearbeitet werden.
	 * <p>
	 * Ein Beispiel wären dynamische Mengen. Die Konfigurations verschickt, sobald sich der Status einer dynamischen Menge ändert, eine Nachricht mit den
	 * Änderungen der Menge an alle angemeldeten Objekte, die sich für Änderungen interssieren. Für diese Nachricht gibt es kein Objekt, das aktiv {@link
	 * AbstractSenderReceiverCommunication#waitForReply} wartet.
	 * <p>
	 * Die Nachricht wird mit der update-Methode empfangen und dann zuerst an ein Objekt, das dieses Interface implementiert, weitergereicht. Dort kann dann
	 * entschieden werden, ob das Telegramm anders verarbeitet werden soll (wie es bei dynamischen Menge der Fall ist) oder ob das Telegramm normal weitergeleitet
	 * werden soll (an ein Objekt, das aktiv mit  {@link AbstractSenderReceiverCommunication#waitForReply} wartet).
	 */
	public interface DataListener {

		/**
		 * Diese Methode wird aufgerufen, sobald eine Instanz von {@link AbstractSenderReceiverCommunication} ein Telegramm in der update-Methode empfängt. Das
		 * Telegramm kann normal weiter verarbeitet werden oder aber aus dem Strom der Telegramme entfernt werden.
		 *
		 * @param data Telegramm, das empfangen wurde
		 *
		 * @return true = Das Telegramm soll normal weiterverarbeitet werden; false = Das Telegramm wurde aus dem Strom der Telegramm entfernt und soll nicht weiter
		 *         beachtet werden
		 */
		boolean messageReceived(Data data);

		/** Diese Methode wird aufgerufen, wenn die Kommunikation abgebrochen werden soll. Alle Sende/Empfangsanmeldungen, Threads, usw. sind zu beenden. */
		void close();
	}

	/**
	 * {@inheritDoc} Defaultimplementierung erzeugt eine Exception.
	 * @param notifyingMutableCollectionChangeListener
	 * @throws UnsupportedOperationException Wenn die Methode nicht überschrieben wurde.
	 */
	public void setMutableCollectionChangeListener(final MutableCollectionChangeListener notifyingMutableCollectionChangeListener) {
		throw new UnsupportedOperationException("setMutableCollectionChangeListener nicht implementiert");
	}

	/**
	 * Klasse, die mehrere Empfängsobjekte an einer Senke kapselt
	 */
	private static class DrainSubscription implements ClientReceiverInterface, NonQueueingReceiver {

		private final List<ClientReceiverInterface> _receivers = new CopyOnWriteArrayList<ClientReceiverInterface>();
		private final ClientDavInterface _connection;
		private final SystemObject _receiverObject;
		private final DataDescription _dataDescription;

		private DrainSubscription(final ClientDavInterface connection, final SystemObject receiverObject, final DataDescription dataDescription) {
			_connection = connection;
			_receiverObject = receiverObject;
			_dataDescription = dataDescription;
		}

		public void unsubscribeReceiver(final ClientReceiverInterface receiver) {
			_receivers.remove(receiver);
			if(_receivers.size() == 0){
				_connection.unsubscribeReceiver(this, _receiverObject, _dataDescription);
			}
		}

		public void subscribeReceiver(final ClientReceiverInterface receiver) {
			if(_receivers.size() == 0){
				_connection.subscribeReceiver(this, _receiverObject, _dataDescription, ReceiveOptions.normal(), ReceiverRole.drain());
			}
			_receivers.add(receiver);
		}

		@Override
		public void update(final ResultData[] results) {
			for(ClientReceiverInterface receiver : _receivers) {
				receiver.update(results);
			}
		}
	}

	/**
	 * Klasse für eine Datenidentifikation, wird als Key in {@link #_drainSubscriptions} benutzt.
	 */
	private static class DataIdent {
		private final ClientDavInterface _connection;
		private final SystemObject _receiverObject;
		private final DataDescription _responseDescription;

		public DataIdent(final ClientDavInterface connection, final SystemObject receiverObject, final DataDescription responseDescription) {
			_connection = connection;
			_receiverObject = receiverObject;
			_responseDescription = responseDescription;
		}

		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(!(o instanceof DataIdent)) return false;

			final DataIdent dataIdent = (DataIdent) o;

			if(!_connection.equals(dataIdent._connection)) return false;
			if(!_receiverObject.equals(dataIdent._receiverObject)) return false;
			if(!_responseDescription.equals(dataIdent._responseDescription)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = _connection.hashCode();
			result = 31 * result + _receiverObject.hashCode();
			result = 31 * result + _responseDescription.hashCode();
			return result;
		}
	}
}
