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

import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse, die Transaktionen verwaltet.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class TransactionManager implements Transactions {

	private final ClientDavConnection _connection;

	/**
	 * Bildet pro Datenidentifikation die Empfänger ab, die die Rohdaten empfangen und dann verarbeitet an die eigentliche Applikation weitergeben
	 */
	private final Map<TransactionDataDescription, ClientReceiverInterface> _realReceivers = Collections.synchronizedMap(new HashMap<TransactionDataDescription, ClientReceiverInterface>());

	/**
	 * Bildet pro Datenidentifikation bzw. Anmeldung die erlaubten Datenidentifikationen ab.
	 */
	private final Map<TransactionDataDescription, Collection<InnerDataSubscription>> _allowedDataIdentifications = new HashMap<TransactionDataDescription,Collection<InnerDataSubscription>>();

	/**
	 * Bildet pro Datenidentifikation bzw. Anmeldung die benötigten Datenidentifikationen ab.
	 */
	private final Map<TransactionDataDescription, Collection<InnerDataSubscription>> _requiredDataIdentifications = new HashMap<TransactionDataDescription,Collection<InnerDataSubscription>>();

	/**
	 * Erstellt einen neuen TransaktionsManager zur Verwaltung von Transaktionen
	 * @param connection Verbindung zum Datenverteiler
	 */
	public TransactionManager(final ClientDavConnection connection) {
		if(connection == null) throw new IllegalArgumentException("connection ist null");
		if(connection.getDataModel().getType("typ.transaktion") == null ){
			throw new IllegalStateException("Damit Transaktionen verwendet werden können, ist ein aktuelleres Datenmodell notwendig. Es wird "
			                                + "kb.systemModellGlobal in Version 26 und kb.metaModellGlobal in Version 14 benötigt.");
		}
		_connection = connection;
	}

	public void subscribeSource(
			final TransactionSenderInterface sender,
			final TransactionDataDescription dataDescription,
			final Collection<InnerDataSubscription> subscriptions,
			final Collection<ResultData> initialData,
			final long time) throws OneSubscriptionPerSendData {

		if(sender == null) throw new IllegalArgumentException("sender ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		if(subscriptions == null) throw new IllegalArgumentException("subscriptions ist null");

		testSubscribe(dataDescription, subscriptions);

		if(subscriptions.size() < 1) throw new IllegalArgumentException("Es muss mindestens eine Datenanmeldung für eine Transaktions-Quelle geben.");
		_connection.triggerTransactionSender(sender, dataDescription, subscriptions);

		try {
			if(initialData == null) {
				_connection.sendData(new ResultData(dataDescription.getObject(), dataDescription.getDataDescription(), time, null));
			}
			else {
				sendTransaction(dataDescription, initialData, System.currentTimeMillis());
			}
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			throw new IllegalStateException("Sendeanmeldung sollte bestehen", sendSubscriptionNotConfirmed);
		}
	}

	public void sendTransaction(final TransactionDataDescription dataDescription, final Collection<ResultData> data) throws SendSubscriptionNotConfirmed {
		sendTransaction(dataDescription, data, System.currentTimeMillis());
	}

	public void sendTransaction(final TransactionDataDescription dataDescription, final Collection<ResultData> data, final long dataTime) throws SendSubscriptionNotConfirmed {
		sendTransaction(new TransactionResultData(dataDescription , data, dataTime));
	}

	public void sendTransaction(final TransactionResultData data) throws SendSubscriptionNotConfirmed {
		if(data == null) throw new IllegalArgumentException("data ist null");
		testSend(data);
		_connection.sendData(data.getResultData(_connection));
	}

	private void testSend(final TransactionResultData data) {
		final Collection<InnerDataSubscription> allowed = _allowedDataIdentifications.get(data.getDataDescription());
		final Collection<InnerDataSubscription> required = _requiredDataIdentifications.get(data.getDataDescription());

		for(final TransactionDataset dataset : data.getData()) {
			if(!allowed.contains(
					new InnerDataSubscription(
							dataset.getObject(), dataset.getDataDescription().getAttributeGroup(), dataset.getDataDescription().getAspect()
					)
			)) {
				throw new IllegalArgumentException(
						"Folgender Datensatz befindet sich nicht in der Liste der registrierten Anmeldungen: " + dataset
				);
			}
		}

		for(final InnerDataSubscription requiredSubscription : required) {
			boolean found = false;
			for(final TransactionDataset dataset : data.getData()) {
				if(dataset.getObject().equals(requiredSubscription.getObject())
				   && dataset.getDataDescription().getAttributeGroup().equals(requiredSubscription.getAttributeGroup())
				   && dataset.getDataDescription().getAspect().equals(requiredSubscription.getAspect())) {
					found = true;
					break;
				}
			}
			if(!found) {
				throw new IllegalArgumentException(
						"Folgende Datenidentifikation ist erforderlich für diese Transaktion, befindet sich aber nicht in den Daten: " + requiredSubscription
				);
			}
		}
	}

	public void unsubscribeReceiver(final TransactionReceiverInterface receiver, final TransactionDataDescription dataDescription) {
		if(receiver == null) throw new IllegalArgumentException("receiver ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");

		final ClientReceiverInterface realReceiver = getRealReceiver(dataDescription);
		if(realReceiver != null) {
			_connection.unsubscribeReceiver(realReceiver, dataDescription.getObject(), dataDescription.getDataDescription());
		}
	}

	private ClientReceiverInterface getRealReceiver(final TransactionDataDescription receiver) {
		return _realReceivers.get(receiver);
	}

	public void unsubscribeSender(final TransactionSenderInterface sender, final TransactionDataDescription dataDescription) {
		if(sender == null) throw new IllegalArgumentException("sender ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		
		_connection.unsubscribeSender(sender, dataDescription.getObject(), dataDescription.getDataDescription());
	}

	public void subscribeSource(
			final TransactionSenderInterface sender, final TransactionDataDescription dataDescription, final Collection<InnerDataSubscription> subscriptions)
			throws OneSubscriptionPerSendData {
		subscribeSource(sender, dataDescription, subscriptions, null, System.currentTimeMillis());
	}

	private void testSubscribe(final TransactionDataDescription dataDescription, final Collection<InnerDataSubscription> subscriptions) {
		final Data data = dataDescription.getAttributeGroup().getConfigurationData(
				_connection.getDataModel().getAttributeGroup(
						"atg.transaktionsEigenschaften"
				)
		);

		// Nachschauen ob alle Datenidentifikationen akzeptiert werden
		if(data.getArray("akzeptiert").getLength() > 0) {
			for(final InnerDataSubscription subscription : subscriptions) {
				boolean found = false;
				// Befindet sich die Identifikation bei den akzeptierten Identifikationen?
				for(final Data item : data.getItem("akzeptiert")) {
					if(equals(item, subscription, dataDescription.getObject())) {
						found = true;
						break;
					}
				}
				if(!found) {
					// Falls nicht, bei den Benötigen nachschauen, die sind in jedem Fall akzeptiert.
					for(final Data item : data.getItem("benötigt")) {
						if(equals(item, subscription, dataDescription.getObject())) {
							found = true;
							break;
						}
					}
				}
				if(!found) {
					// Falls weder bei den Akzeptierten noch Benötigten, Fehler werfen.
					throw new IllegalArgumentException(
							"Folgende Datenidentifikation befindet sich nicht in der Liste der erlaubten Anmeldungen: " + subscription
					);
				}
			}
		}

		final Collection<InnerDataSubscription> required = new ArrayList<InnerDataSubscription>();

		// Nachschauen, ob alle benötigten Datenidentifikationen vorhanden sind
		for(final Data item : data.getItem("benötigt")) {
			boolean found = false;
			for(final InnerDataSubscription subscription : subscriptions) {
				if(equals(item, subscription, dataDescription.getObject())) {
					found = true;
					required.add(subscription);
					break;
				}
			}
			if(!found) {
				throw new IllegalArgumentException(
						"Folgende Datenidentifikation ist erforderlich für diese Transaktion, befindet sich aber nicht in den Anmeldungen: " + item
				);
			}
		}

		_allowedDataIdentifications.put(dataDescription, subscriptions);
		_requiredDataIdentifications.put(dataDescription, required);
	}

	/**
	 * Prüft, ob die Datenidentifikation in einem Data-Objekt mit einer Datenidentifikation in einer InnerDataSubscription übereinstimmt, und ob das Transaktionsobjekt
	 * übereinstimmt, wenn NurTransaktionsObjekt im Data festgelegt ist.
	 * @param item Data-Objekt
	 * @param subscription Anmelde-Info
	 * @param transactionObject Transaktionsobjekt (zur Prüfung von NurTransaktionsObjekt)
	 * @return true wenn Übereinstimmung, sonst false
	 */
	private boolean equals(
			final Data item, final InnerDataSubscription subscription, final SystemObject transactionObject) {
		final SystemObject requiredObjectType = item.getReferenceValue("ObjektTyp").getSystemObject();
		if(requiredObjectType != null && !subscription.getObject().getType().equals(requiredObjectType)) return false;

		if("Ja".equals(item.getTextValue("NurTransaktionsObjekt").getText())) {
			if(!subscription.getObject().equals(transactionObject)) return false;
		}

		final SystemObject requiredAttributeGroup = item.getReferenceValue("Attributgruppe").getSystemObject();
		if(requiredAttributeGroup != null && !subscription.getAttributeGroup().equals(requiredAttributeGroup)) return false;

		final SystemObject requiredAspect = item.getReferenceValue("Aspekt").getSystemObject();
		if(requiredAspect != null && !subscription.getAspect().equals(requiredAspect)) return false;

		// Sonst true zurückgeben
		return true;
	}


	public void subscribeSender(final TransactionSenderInterface sender, final TransactionDataDescription dataDescription) throws OneSubscriptionPerSendData {
		if(sender == null) throw new IllegalArgumentException("sender ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		
		_connection.triggerTransactionSender(sender, dataDescription, null);
	}

	public void subscribeReceiver(final TransactionReceiverInterface receiver, final TransactionDataDescription dataDescription)
			throws OneSubscriptionPerSendData {
		if(receiver == null) throw new IllegalArgumentException("receiver ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		
		subscribeReceiver(receiver, dataDescription, null);
	}

	public void subscribeDrain(
			final TransactionReceiverInterface receiver,
			final TransactionDataDescription dataDescription,
			final Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData {
		if(receiver == null) throw new IllegalArgumentException("receiver ist null");
		if(dataDescription == null) throw new IllegalArgumentException("dataDescription ist null");
		if(subscriptions == null) throw new IllegalArgumentException("subscriptions ist null");

		testSubscribe(dataDescription, subscriptions);
		if(subscriptions.size() < 1) throw new IllegalArgumentException("Es muss mindestens eine Datenanmeldung für eine Transaktions-Senke geben.");
		subscribeReceiver(receiver, dataDescription, subscriptions);
		_allowedDataIdentifications.put(dataDescription, subscriptions);
	}

	/**
	 * Meldet eine Senke oder einen Empfänger an
	 * @param receiver Empfänger
	 * @param dataDescription Datenbeschreibung
	 * @param subscriptions Innere Anmeldungen falls Senke, sonst null
	 * @throws OneSubscriptionPerSendData Fehler
	 */
	private void subscribeReceiver(
			final TransactionReceiverInterface receiver,
			final TransactionDataDescription dataDescription,
			final Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData {
		if(_realReceivers.containsKey(dataDescription)) throw new OneSubscriptionPerSendData("Für diese Datenidentifikation ist bereits ein Empfänger angemeldet.");
		final ClientReceiverInterface realReceiver = new TransactionReceiver(receiver);
		_realReceivers.put(dataDescription, realReceiver);
		_connection.triggerTransactionReceiver(realReceiver, dataDescription, subscriptions);
	}

	private Transaction createTransaction(final ResultData result) {
		return new TransactionResultData(result);
	}

	private class TransactionReceiver implements ClientReceiverInterface{

		private final TransactionReceiverInterface _receiver;

		public TransactionReceiver(final TransactionReceiverInterface receiver) {
			_receiver = receiver;
		}

		public void update(final ResultData[] results) {
			final Transaction[] transactionResults = new Transaction[results.length];
			for(int i = 0; i < results.length; i++) {
				transactionResults[i] = createTransaction(results[i]);
			}
			_receiver.update(transactionResults);
		}

		@Override
		public String toString() {
			return "TransactionReceiver{" + "_receiver=" + _receiver + '}';
		}
	}

	@Override
	public String toString() {
		return "TransactionManager{" + "_connection=" + _connection + '}';
	}
}
