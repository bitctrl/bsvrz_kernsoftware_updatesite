/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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
 * @version $Revision: 6316 $
 */
public class ReceiverSubscription {

	/** Representant der Emp�nger */
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
	 * Objekt, dass f�r die Zwischenspeicherung und Auslieferung von empfangenen Datens�tzen an den Receiver zust�ndig ist, oder <code>null</code>, falls noch
	 * nicht angemeldet.
	 */
	private CollectingReceiver _collectingReceiver = null;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param client                    Empf�nger
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
	 * Gibt den Empf�nger zur�ck.
	 *
	 * @return Empf�nger
	 */
	public final ClientReceiverInterface getClientReceiver() {
		return _clientReceiver;
	}

	/**
	 * Gibt das Systemobjekt zur�ck.
	 *
	 * @return Systemobjekt
	 */
	public final SystemObject getSystemObject() {
		return _systemObject;
	}

	/**
	 * Gibt die beschreibende Informationen der zu versendenden Daten zur�ck.
	 *
	 * @return beschreibende Informationen
	 */
	public final DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 * Gibt an, ob Interesse an nachgelieferten oder an aktuellen Datens�tzen besteht.
	 *
	 * @return <code>true:</code> nachgelieferte Datens�tze erw�nscht, <code>false:</code> nachgelieferte Datens�tze nicht erw�nscht
	 */
	public final boolean getDelayedDataFlag() {
		return _receiveSubscriptionInfo.getDelayedDataFlag();
	}

	/**
	 * Gibt an, ob Interesse an nur den ge�nderten Datens�tzen oder an allen Datens�tzen besteht.
	 *
	 * @return <code>true:</code> nur ge�nderte Datens�tze erw�nscht, <code>false:</code> alle Datens�tze erw�nscht
	 */
	public final boolean getDeltaDataFlag() {
		return _receiveSubscriptionInfo.getDeltaDataFlag();
	}

	/**
	 * Gibt an, ob die Applikation als ein normaler Empf�nger f�r dieses Datums angemeldet ist oder nicht.
	 *
	 * @return <code>true:</code> Applikation ist normaler Emf�nger, <code>false:</code> Applikation ist kein normaler Empf�nger
	 */
	public final boolean isReceiver() {
		return _receiveSubscriptionInfo.isReceiver();
	}

	/**
	 * Gibt an, ob die Applikation als Senke f�r dieses Datums angemeldet ist oder nicht.
	 *
	 * @return <code>true:</code>Applikation ist als Senke angemeldet, <code>false:</code>Applikation ist nicht als Senke angemeldet.
	 */
	public final boolean isDrain() {
		return _receiveSubscriptionInfo.isDrain();
	}

	/**
	 * Gibt die Mindestverweilzeit im Cache zur�ck.
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
//	 * Dies geschieht nach der erhalt der l�ckenlose Historiedaten.
//	 */
//	public final void setHistoryDataNotNeeded() {
//		historyTime = 0;
//	}

//	/**
//	 * Gibt der Zeitbereich der ben�tigten Historie zur�ck
//	 */
//	public final long getHistoryTime() {
//		return historyTime;
//	}

	/**
	 * Gibt die Empfangsanmeldeinformationen zur�ck.
	 *
	 * @return Empfangsanmeldeinformationen
	 */
	public final ReceiveSubscriptionInfo getReceiveSubscriptionInfo() {
		return _receiveSubscriptionInfo;
	}

	/**
	 * Gibt die Basisanmeldeinformationen zur�ck.
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
	 * Setzt des Objekt, dass f�r die Zwischenspeicherung und Auslieferung von empfangenen Datens�tzen an den Receiver zust�ndig ist.
	 *
	 * @param collectingReceiver Objekt, dass f�r die Zwischenspeicherung und Auslieferung von empfangenen Datens�tzen an den Receiver zust�ndig ist.
	 */
	public void setCollectingReceiver(final CollectingReceiver collectingReceiver) {
		_collectingReceiver = collectingReceiver;
	}

	/**
	 * Bestimmt des Objekt, dass f�r die Zwischenspeicherung und Auslieferung von empfangenen Datens�tzen an den Receiver zust�ndig ist.
	 *
	 * @return Objekt, dass f�r die Zwischenspeicherung und Auslieferung von empfangenen Datens�tzen an den Receiver zust�ndig ist oder <code>null</code>, falls
	 *         noch nicht angemeldet.
	 */
	public CollectingReceiver getCollectingReceiver() {
		return _collectingReceiver;
	}
}
