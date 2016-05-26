/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.subscription;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;


/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ReceiverSubscription {

	/** Representant der Empänger */
	private ClientReceiverInterface _clientReceiver;

	/** Der Objekt dieser Anmeldung */
	private SystemObject _systemObject;

	/** Beschreibende Informationen der zu versendenden Daten */
	private DataDescription _dataDescription;

	/** Empfangsanmeldeinformationen */
	private ReceiveSubscriptionInfo _receiveSubscriptionInfo;

	/** Die mindestens Verweilzeit im Cache wenn -1 dann 60000 */
	private long _timeInCache;

	/**
	 * Objekt, dass für die Zwischenspeicherung und Auslieferung von empfangenen Datensätzen an den Receiver zuständig ist, oder <code>null</code>, falls noch
	 * nicht angemeldet.
	 */
	private CollectingReceiver _collectingReceiver = null;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param client                    Empfänger
	 * @param systemObject              Objekt der Anmeldung
	 * @param dataDescription           Informationen zur Beschreibung der Anmeldung
	 * @param externalSimulationVariant Simulationsvariante
	 * @param options                   Optionen
	 * @param role                      Rolle
	 * @param timeInCache               mindestverweilzeit im Cache
	 */
	public ReceiverSubscription(
			ClientReceiverInterface client,
			SystemObject systemObject,
			DataDescription dataDescription,
			final short externalSimulationVariant,
			ReceiveOptions options,
			ReceiverRole role,
			long timeInCache) {
		_clientReceiver = client;
		_systemObject = systemObject;
		_dataDescription = dataDescription;
		if(timeInCache < 0) {
			_timeInCache = 0;
		}
		else {
			_timeInCache = timeInCache;
		}

		BaseSubscriptionInfo baseSubscriptionInfo = new BaseSubscriptionInfo(
				_systemObject.getId(), _dataDescription.getAttributeGroup().getAttributeGroupUsage(_dataDescription.getAspect()), externalSimulationVariant
		);
		_receiveSubscriptionInfo = new ReceiveSubscriptionInfo(baseSubscriptionInfo, options, role);
	}

	/**
	 * Gibt den Empfänger zurück.
	 *
	 * @return Empfänger
	 */
	public final ClientReceiverInterface getClientReceiver() {
		return _clientReceiver;
	}

	/**
	 * Gibt das Systemobjekt zurück.
	 *
	 * @return Systemobjekt
	 */
	public final SystemObject getSystemObject() {
		return _systemObject;
	}

	/**
	 * Gibt die beschreibende Informationen der zu versendenden Daten zurück.
	 *
	 * @return beschreibende Informationen
	 */
	public final DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Gibt an, ob Interesse an nachgelieferten oder an aktuellen Datensätzen besteht.
	 *
	 * @return <code>true:</code> nachgelieferte Datensätze erwünscht, <code>false:</code> nachgelieferte Datensätze nicht erwünscht
	 */
	public final boolean getDelayedDataFlag() {
		return _receiveSubscriptionInfo.getDelayedDataFlag();
	}

	/**
	 * Gibt an, ob Interesse an nur den geänderten Datensätzen oder an allen Datensätzen besteht.
	 *
	 * @return <code>true:</code> nur geänderte Datensätze erwünscht, <code>false:</code> alle Datensätze erwünscht
	 */
	public final boolean getDeltaDataFlag() {
		return _receiveSubscriptionInfo.getDeltaDataFlag();
	}

	/**
	 * Gibt an, ob die Applikation als ein normaler Empfänger für dieses Datums angemeldet ist oder nicht.
	 *
	 * @return <code>true:</code> Applikation ist normaler Emfänger, <code>false:</code> Applikation ist kein normaler Empfänger
	 */
	public final boolean isReceiver() {
		return _receiveSubscriptionInfo.isReceiver();
	}

	/**
	 * Gibt an, ob die Applikation als Senke für dieses Datums angemeldet ist oder nicht.
	 *
	 * @return <code>true:</code>Applikation ist als Senke angemeldet, <code>false:</code>Applikation ist nicht als Senke angemeldet.
	 */
	public final boolean isDrain() {
		return _receiveSubscriptionInfo.isDrain();
	}

	/**
	 * Gibt die Mindestverweilzeit im Cache zurück.
	 *
	 * @return Mindestverweilzeit im Cache
	 */
	public final long getTimeInCache() {
		return _timeInCache;
	}

//	/**
//	 * Gibt ob Historie Daten angefragt sind
//	 */
//	public final boolean needsHistoryData() {
//		return historyTime > 0;
//	}

//	/**
//	 * Setzt die Information, dass die Historie Daten nicht mehr gefragt sind.
//	 * Dies geschieht nach der erhalt der lückenlose Historiedaten.
//	 */
//	public final void setHistoryDataNotNeeded() {
//		historyTime = 0;
//	}

//	/**
//	 * Gibt der Zeitbereich der benötigten Historie zurück
//	 */
//	public final long getHistoryTime() {
//		return historyTime;
//	}

	/**
	 * Gibt die Empfangsanmeldeinformationen zurück.
	 *
	 * @return Empfangsanmeldeinformationen
	 */
	public final ReceiveSubscriptionInfo getReceiveSubscriptionInfo() {
		return _receiveSubscriptionInfo;
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück.
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _receiveSubscriptionInfo.getBaseSubscriptionInfo();
	}

	/**
	 * Aktualisiert die Empfangsanmeldeinformationen.
	 *
	 * @param _receiveSubscriptionInfo Empfangsanmeldeinformationen
	 *
	 * @return <code>true:</code>Empfangsanmeldeinformationen aktualisiert, <code>false:</code>Empfangsanmeldeinformationen nicht aktualisiert
	 */

	public final boolean updateSubscriptionInfo(ReceiveSubscriptionInfo _receiveSubscriptionInfo) {
		return this._receiveSubscriptionInfo.updateSubscriptionInfo(_receiveSubscriptionInfo);
	}

	/**
	 * Setzt des Objekt, dass für die Zwischenspeicherung und Auslieferung von empfangenen Datensätzen an den Receiver zuständig ist.
	 *
	 * @param collectingReceiver Objekt, dass für die Zwischenspeicherung und Auslieferung von empfangenen Datensätzen an den Receiver zuständig ist.
	 */
	public void setCollectingReceiver(final CollectingReceiver collectingReceiver) {
		_collectingReceiver = collectingReceiver;
	}

	/**
	 * Bestimmt des Objekt, dass für die Zwischenspeicherung und Auslieferung von empfangenen Datensätzen an den Receiver zuständig ist.
	 *
	 * @return Objekt, dass für die Zwischenspeicherung und Auslieferung von empfangenen Datensätzen an den Receiver zuständig ist oder <code>null</code>, falls
	 *         noch nicht angemeldet.
	 */
	public CollectingReceiver getCollectingReceiver() {
		return _collectingReceiver;
	}
}
