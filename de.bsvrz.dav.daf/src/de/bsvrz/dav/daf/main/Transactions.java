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

import java.util.Collection;

/**
 * Interface um Transaktionen durchzuf�hren
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8953 $
 */
public interface Transactions {

	/**
	 * Meldet eine Transaktionsquelle an und sendet einen Transaktionsdatensatz.
	 * @param sender Callback zur Sendesteuerung
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @param subscriptions Datenidentifikationen innerhalb der Transaktion
	 * @param initialData Initialer Datensatz
	 * @param time Datenzeit des Transaktionsdatensatzes
	 * @throws OneSubscriptionPerSendData Falls es schon eine Quellen-Anmeldung zu dieser Transaktion oder einem enthaltenen Datensatz gibt.
	 */
	public void subscribeSource(TransactionSenderInterface sender, TransactionDataDescription dataDescription,
	                            Collection<InnerDataSubscription> subscriptions, Collection<ResultData> initialData, long time) throws OneSubscriptionPerSendData;

	/**
	 * Meldet eine Transaktionsquelle an und sendet einen leeren Datensatz.
	 * @param sender Callback zur Sendesteuerung
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @param subscriptions Datenidentifikationen innerhalb der Transaktion
	 * @throws OneSubscriptionPerSendData Falls es schon eine Quellen-Anmeldung zu dieser Transaktion oder einem enthaltenen Datensatz gibt.
	 */
	public void subscribeSource(TransactionSenderInterface sender, TransactionDataDescription dataDescription, Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData;

	/**
	 * Meldet einen Transaktionssender an.
	 * @param sender Callback zur Sendesteuerung
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @throws OneSubscriptionPerSendData Falls es schon eine widerspr�chliche Anmeldung zu dieser Transaktion oder einem enthaltenen Datensatz gibt.
	 */
	public void subscribeSender(TransactionSenderInterface sender, TransactionDataDescription dataDescription) throws OneSubscriptionPerSendData;

	/**
	 * Meldet einen Transaktionsempf�nger an.
	 * @param receiver Callback zum Empfang von Daten
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @throws OneSubscriptionPerSendData Falls es schon eine widerspr�chliche Anmeldung zu dieser Transaktion oder einem enthaltenen Datensatz gibt.
	 */
	public void subscribeReceiver(TransactionReceiverInterface receiver, TransactionDataDescription dataDescription) throws OneSubscriptionPerSendData;

	/**
	 * Meldet eine Transaktionssenke an.
	 * @param receiver Callback zum Empfang von Daten
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @param subscriptions Datenidentifikationen innerhalb der Transaktion
	 * @throws OneSubscriptionPerSendData Falls es schon eine widerspr�chliche Anmeldung zu dieser Transaktion oder einem enthaltenen Datensatz gibt.
	 */
	public void subscribeDrain(TransactionReceiverInterface receiver, TransactionDataDescription dataDescription, Collection<InnerDataSubscription> subscriptions) throws OneSubscriptionPerSendData;

	/**
	 * Meldet einen Transaktionempf�nger oder eine Transaktionssenke ab.
	 * @param receiver Empf�nger-Klasse
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 */
	public void unsubscribeReceiver(TransactionReceiverInterface receiver, TransactionDataDescription dataDescription);

	/**
	 * Meldet einen Transaktionsender oder eine Transaktionsquelle ab.
	 * @param sender Sender-Klasse
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 */
	public void unsubscribeSender(TransactionSenderInterface sender, TransactionDataDescription dataDescription);

	/**
	 * Sendet eine Transaktion
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @param data Liste mit Daten
	 * @param dataTime Datenzeit (des Transaktionsdatensatzes)
	 * @throws SendSubscriptionNotConfirmed Bei fehlender Sender-Anmeldung
	 */
	public void sendTransaction(final TransactionDataDescription dataDescription, final Collection<ResultData> data, final long dataTime) throws SendSubscriptionNotConfirmed ;

	/**
	 * Sendet eine Transaktion mit dem Transaktiondatensatz-Zeitstempel der aktuellen Zeit. Innere Daten k�nnen einen anderen Zeitstempel haben.
	 * @param dataDescription Datenidentifikation der Transaktion (Transaktionsobjekt, -attributgruppe, -aspekt)
	 * @param data Liste mit Daten
	 * @throws SendSubscriptionNotConfirmed Bei fehlender Sender-Anmeldung
	 */
	public void sendTransaction(final TransactionDataDescription dataDescription, final Collection<ResultData> data) throws SendSubscriptionNotConfirmed;

	/**
	 * Sendet eine Transaktion
	 * @param data Transaktion
	 * @throws SendSubscriptionNotConfirmed Bei fehlender Sender-Anmeldung
	 */
	public void sendTransaction(final TransactionResultData data) throws SendSubscriptionNotConfirmed;
}
