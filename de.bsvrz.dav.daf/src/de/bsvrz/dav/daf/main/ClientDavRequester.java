/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementiert die Schnittstelle Applikation-Dav (siehe {@link #DavRequester}) auf Client-Seite
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ClientDavRequester extends DavRequester {

	private final Map<Long, Result> _answerIdMap = new HashMap<Long, Result>();

	private static long _requestId = 0;

	/**
	 * Erstellt eine neue ClientDavRequester-Instanz
	 * @param connection Verbindung zum Datenverteiler
	 */
	public ClientDavRequester(final ClientDavConnection connection) {
		super(
				connection,
				connection.getDataModel().getAspect("asp.anfrage"),
				connection.getDataModel().getAspect("asp.antwort")
		);
		subscribeDrain(_connection.getLocalApplicationObject());
	}

	@Override
	protected void onReceive(final Data data) {
		final SystemObject sender = data.getReferenceValue("Absender").getSystemObject();
		final long requestId = data.getUnscaledValue("AnfrageIndex").longValue();
		final int requestKind = (int)data.getUnscaledValue("AnfrageTyp").longValue();
		final byte[] bytes = data.getUnscaledArray("Daten").getByteArray();
		synchronized(_answerIdMap) {
			_answerIdMap.put(requestId, new Result(sender, requestKind, bytes));
			_answerIdMap.notifyAll();
		}
	}

	/**
	 * Sendet eine Nachricht an den Datenverteiler, die das anmeldenden von Transaktionen bewirken soll
	 * @param isSource Quell-Anmeldung? Sonst Senke.
	 * @param dataDescription Datenidentifikation der Transaktion
	 * @param subscriptions Datenidentifikationen innerhalb der Transaktion
	 * @throws OneSubscriptionPerSendData Fehler bei der Anmeldung
	 */
	public void triggerSubscribe(final boolean isSource, final TransactionDataDescription dataDescription, final Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData {
		if (_attributeGroup == null || _receiveAspect == null || _sendAspect == null) {
			throw new IllegalStateException("Das verwendete Datenmodell unterstützt keine Transaktionen.");
		}
		final long id = generateRequestId();
		try {
			sendBytes(
					_connection.getLocalDav(),
					id,
					isSource ? SUBSCRIBE_TRANSMITTER_SOURCE : SUBSCRIBE_TRANSMITTER_DRAIN,
					serializeTransactionSubscriptions(dataDescription, subscriptions),
					_connection.getLocalApplicationObject()
			);
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		synchronized(_answerIdMap){
			while(!_answerIdMap.containsKey(id)){
				try {
					_answerIdMap.wait();
				}
				catch(InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
			final Result answer = _answerIdMap.get(id);
			if(answer.getRequestKind() != ANSWER_OK) {
				throw new OneSubscriptionPerSendData(new String(answer.getBytes()));
			}
		}
	}

	private byte[] serializeTransactionSubscriptions(
			final TransactionDataDescription transactionDataDescription, final Collection<InnerDataSubscription> subscriptions) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DataOutputStream dataOutputStream = new DataOutputStream(out);
		try {
			dataOutputStream.writeInt(subscriptions.size());
			dataOutputStream.writeLong(transactionDataDescription.getObject().getId());
			dataOutputStream.writeLong(transactionDataDescription.getAttributeGroup().getAttributeGroupUsage(transactionDataDescription.getAspect()).getId());
			dataOutputStream.writeShort(
					transactionDataDescription.getSimulationVariant() == -1
					? _connection.getClientDavParameters().getSimulationVariant()
					: transactionDataDescription.getSimulationVariant()
			);
			for(final InnerDataSubscription subscription : subscriptions) {
				dataOutputStream.writeLong(subscription.getObject().getId());
				dataOutputStream.writeLong(subscription.getAttributeGroup().getId());
				dataOutputStream.writeLong(subscription.getAspect().getId());
			}
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			try {
				dataOutputStream.close();
			}
			catch(IOException ignored) {
			}
		}
		return out.toByteArray();
	}

	private byte[] serializeSubscriptionQuery(final SystemObject object, final AttributeGroupUsage usage, final short simulationVariant) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DataOutputStream dataOutputStream = new DataOutputStream(out);
		try {
			dataOutputStream.writeLong(object.getId());
			dataOutputStream.writeLong(usage.getId());
			dataOutputStream.writeShort(
					simulationVariant == -1 ? _connection.getClientDavParameters().getSimulationVariant() : simulationVariant
			);
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			try {
				dataOutputStream.close();
			}
			catch(IOException ignored) {
			}
		}
		return out.toByteArray();
	}

	private byte[] serializeSubscriptionQuery(final ClientApplication application) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DataOutputStream dataOutputStream = new DataOutputStream(out);
		try {
			dataOutputStream.writeLong(application.getId());
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			try {
				dataOutputStream.close();
			}
			catch(IOException ignored) {
			}
		}
		return out.toByteArray();
	}

	private static synchronized long generateRequestId() {
		final long result = _requestId;
		_requestId++;
		return result;
	}

	/**
	 * Gibt Informationen über die Anmeldungen am lokalen Datenverteiler heraus
	 *
	 * @param davApplication
	 * @param object Objekt
	 * @param usage Attributgruppenverwendung
	 * @param simulationVariant Simulationsvariante
	 * @return Info-Objekt
	 */
	public ClientSubscriptionInfo getSubscriptionInfo(
			final DavApplication davApplication, final SystemObject object, final AttributeGroupUsage usage, final short simulationVariant) throws IOException {
		if(_attributeGroup == null) return null;
		final long id = generateRequestId();
		sendBytes(
				davApplication,
				id,
				SUBSCRIPTION_INFO,
				serializeSubscriptionQuery(object, usage, simulationVariant),
				_connection.getLocalApplicationObject()
		);
		synchronized(_answerIdMap){
			while(!_answerIdMap.containsKey(id)){
				try {
					_answerIdMap.wait();
				}
				catch(InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
			final Result answer = _answerIdMap.get(id);
			if(answer.getRequestKind() != SUBSCRIPTION_INFO){
				throw new IOException(new String(answer.getBytes()));
			}
			else{
				try {
					return new ClientSubscriptionInfo(_connection, usage, answer.getBytes());
				}
				catch(IOException e) {
					_debug.warning("Fehler beim Verarbeiten der Anmelde-Informationen", e);
					throw e;
				}
			}
		}
	}

	/**
	 * Gibt Informationen über die Anmeldungen am lokalen Datenverteiler heraus
	 *
	 *
	 * @param davApplication
	 * @param application Applikation, von der Anmeldungen ermittelt werden sollen
	 * @return Info-Objekt
	 */
	public ApplicationSubscriptionInfo getSubscriptionInfo(
			final DavApplication davApplication, final ClientApplication application) throws IOException {
		if(_attributeGroup == null) return null;
		final long id = generateRequestId();
		sendBytes(
				davApplication,
				id,
				APP_SUBSCRIPTION_INFO,
				serializeSubscriptionQuery(application),
				_connection.getLocalApplicationObject()
		);
		synchronized(_answerIdMap){
			while(!_answerIdMap.containsKey(id)){
				try {
					_answerIdMap.wait();
				}
				catch(InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
			final Result answer = _answerIdMap.get(id);
			if(answer.getRequestKind() != APP_SUBSCRIPTION_INFO){
				throw new IOException(new String(answer.getBytes()));
			}
			else{
				try {
					return new ApplicationSubscriptionInfo(_connection, application, answer.getBytes());
				}
				catch(IOException e) {
					_debug.warning("Fehler beim Verarbeiten der Anmelde-Informationen", e);
					throw e;
				}
			}
		}
	}

	/**
	 * Ergebnis einer Anfrage
	 */
	private class Result {

		private final SystemObject _sender;

		private final int _requestKind;

		private final byte[] _bytes;

		public Result(final SystemObject sender, final int requestKind, final byte[] bytes) {

			_sender = sender;
			_requestKind = requestKind;
			_bytes = bytes;
		}

		public SystemObject getSender() {
			return _sender;
		}

		public int getRequestKind() {
			return _requestKind;
		}

		public byte[] getBytes() {
			return _bytes;
		}

		@Override
		public String toString() {
			return "Result{" + "_sender=" + _sender + ", _requestKind=" + _requestKind + ", _bytes=" + new String(_bytes) + '}';
		}
	}
}
