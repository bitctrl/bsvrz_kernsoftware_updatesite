/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl;

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionInfo;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.impl.subscription.CollectingReceiver;
import de.bsvrz.dav.daf.main.impl.subscription.CollectingReceiverManager;
import de.bsvrz.dav.daf.main.impl.subscription.ReceiverSubscription;

import java.util.*;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ReceiveSubscriptionObject {

	/** Der Basisanmeldeinformationen dieses Objekts */
	private final BaseSubscriptionInfo baseSubscriptionInfo;

	/** Liste der Empfangsanmeldungen */
	private final List<ReceiverSubscription> receiverSubscriptionList = new ArrayList<ReceiverSubscription>();

	/** Die Empfangsanmeldeinformationen, die beim Datenverteiler angemeldet ist. */
	private ReceiveSubscriptionInfo receiveSubscriptionInfo;

	/** Die Verweilzeit im Cache */
	private long timeInCache;

	/** Gibt die Information ob der aktuelle Datum im Cache ist. */
	private boolean actualDataAvaillable;

	/**
	 * @param _receiverSubscription Dieses Objekt wird in die Liste der Empfangsanmeldungen hinzugefügt (an erster Stelle). Des Weiteren werden die Information
	 *                              <code>BaseSubscriptionInfo</code>, <code>getReceiveSubscriptionInfo</code>, <code>TimeInCache</code> aus dem Objekt
	 *                              gespeichert.
	 *
	 * @throws IllegalArgumentException Der übergebene Parameter war <code>null</code>
	 */
	public ReceiveSubscriptionObject(ReceiverSubscription _receiverSubscription, CollectingReceiverManager receiverManager) {
		if(_receiverSubscription == null) {
			throw new IllegalArgumentException("Anmeldeinformationenobject ist null");
		}

		baseSubscriptionInfo = _receiverSubscription.getBaseSubscriptionInfo();
		final CollectingReceiver collectingReceiver = receiverManager.addReceiverReference(_receiverSubscription.getClientReceiver());
		_receiverSubscription.setCollectingReceiver(collectingReceiver);
		synchronized(_receiverSubscription) {
			receiverSubscriptionList.add(_receiverSubscription);
		}

		receiveSubscriptionInfo = _receiverSubscription.getReceiveSubscriptionInfo().cloneObject();
		timeInCache = _receiverSubscription.getTimeInCache();
		actualDataAvaillable = false;
	}

	/**
	 * Gibt die Basisanmeldeinformationen dieses Objekts zurück. Dieser Wert wird indirekt im Konstruktor übergeben.
	 *
	 * @return Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return baseSubscriptionInfo;
	}

	/**
	 * Gibt die Liste der Empfangsanmeldungen zurück
	 *
	 * @return Liste der Empfangsanmeldungen. Sind keine Empfangsanmeldungen vorhanden, so wird eine leere Liste zurückgegeben.
	 */
	public final List<ReceiverSubscription> getReceiverSubscriptionList() {
		return receiverSubscriptionList;
	}

	/**
	 * Gibt die Empfangsanmeldeinformation zurück, die beim Datenverteiler angemeldet ist.
	 *
	 * @return Empfangsanmeldeinformationen oder <code>null</code>falls keine weiteren Anmeldungen bestehen
	 *
	 * @see #getReceiverSubscriptionList()
	 */
	public final ReceiveSubscriptionInfo getReceiveSubscriptionInfo() {
		return receiveSubscriptionInfo;
	}

	/**
	 * Verweilzeit im Cache für Datensätze dieser Anmeldung.
	 *
	 * @return s.o.
	 */
	public final long getTimeInCache() {
		return timeInCache;
	}

	/**
	 * Ist der aktuelle Datensatz dieser Anmeldung vorhanden.
	 *
	 * @return true = Der aktuelle Datensatz der Anmeldung ist vorhanden; false = sonst
	 */
	public final boolean isActualDataAvaillable() {
		return actualDataAvaillable;
	}

	/**
	 * Setzt den neuen Status ob der aktuelle Datensatz diese Anmeldung vorhanden ist oder nicht.
	 *
	 * @param availlable true = Der aktuelle Datensatz ist vorhanden; false = sonst
	 */
	public final void setActualDataAvaillable(boolean availlable) {
		actualDataAvaillable = availlable;
	}

	/**
	 * Aktuallisiert dieses Objekt. Wenn die gegebene Anmeldung nicht existiert, dann wird sie zur Anmeldeliste hinzugefügt. Wenn sie existiert, dann wird sie
	 * aktuallisiert.
	 *
	 * @param _receiverSubscription Dieses Objekt wird entweder zur Anmeldeliste hinzugefügt (falls noch keine Anmeldung besteht) oder die Informationen der
	 *                              bestehenden Anmeldung wird aktualisiert.
	 *
	 * @return true = Wenn durch die Aktuallisierung eine Änderung der Informationen, die beim Datenverteiler angemeldet wurden, durchgeführt wurde; false = sonst
	 */
	public final synchronized boolean addSubscription(ReceiverSubscription _receiverSubscription, CollectingReceiverManager receiverManager) {
		boolean changed = false;
		ReceiveSubscriptionInfo _receiveSubscriptionInfo = _receiverSubscription.getReceiveSubscriptionInfo();
		if(_receiveSubscriptionInfo == null) {
			throw new IllegalArgumentException("Übergabeparameter ist leer oder inconsistent");
		}
		// Schonmal beim Datenverteiler gemeldet?
		if(receiveSubscriptionInfo == null) {
			receiveSubscriptionInfo = _receiveSubscriptionInfo.cloneObject();
			changed = true;
		}
		else {
			// Wenn ein Unterschied zum anmeldung beim Datenverteiler -> zusammen fassen der Anmeldungen -> Datenverteiler aktuallisieren
			try {
				changed = receiveSubscriptionInfo.updateSubscriptionInfo(_receiveSubscriptionInfo);
			}
			catch(IllegalStateException e) {
				throw new IllegalStateException(e.getLocalizedMessage() + ". Empfangsanmeldung für " + _receiverSubscription.getSystemObject() + ":" + _receiverSubscription.getDataDescription(), e);
			}
		}
		// Schonmal von der gleiche Teilapplikation gemeldet?
		ReceiverSubscription subscription = getSubscription(_receiverSubscription.getClientReceiver());
		if(subscription == null) {
			final CollectingReceiver collectingReceiver = receiverManager.addReceiverReference(_receiverSubscription.getClientReceiver());
			_receiverSubscription.setCollectingReceiver(collectingReceiver);
			synchronized(receiverSubscriptionList) {
				receiverSubscriptionList.add(_receiverSubscription);
			}
		}
		else {
			subscription.updateSubscriptionInfo(_receiveSubscriptionInfo);
		}
		long time = _receiverSubscription.getTimeInCache();
		if(time > timeInCache) {
			timeInCache = time;
		}
		if(changed) {
			actualDataAvaillable = false;
		}
		return changed;
	}

	/**
	 * Gibt zugehörige Empfängeranmeldung zum übergebenen Objekt zurück.
	 *
	 * @param client Objekt, zu dem die <code>ReceiverSubscription</code> gesucht werden soll
	 *
	 * @return ReceiverSubscription oder <code>null</code>, falls es zu dem übergebenen Objekt keine <code>ReceiverSubscription</code> gibt.
	 */
	private final ReceiverSubscription getSubscription(ClientReceiverInterface client) {
		synchronized(receiverSubscriptionList) {
			for(int i = 0; i < receiverSubscriptionList.size(); ++i) {
				ReceiverSubscription subscription = receiverSubscriptionList.get(i);
				if(subscription != null) {
					// Gleiche Teilapplikation -> zusammen fassen
					if(subscription.getClientReceiver() == client) {
						return subscription;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Entfernt eine Anmeldung als Empfänger.
	 *
	 * @param receiver Objekt, über das die Empfangsanmeldung identifiziert wird, die entfernt werden soll.
	 *
	 * @return true = Wenn die Anmeldung entfernt werden konnte; false = sonst
	 *
	 * @throws IllegalArgumentException Wird geworfen, wenn der übergebene Parameter <code>null</code> ist.
	 */
	public final synchronized boolean removeSubscription(ClientReceiverInterface receiver, CollectingReceiverManager receiverManager) {
		if(receiver == null) {
			throw new IllegalArgumentException("Übergabeparameter ist leer oder inkonsistent");
		}

		boolean foundReceiver = false;

		synchronized(receiverSubscriptionList) {

			for(int i = receiverSubscriptionList.size() - 1; i > -1; --i) {
				ReceiverSubscription receiverSubscription = receiverSubscriptionList.get(i);
				if(receiverSubscription == null) {
					continue;
				}
				if(receiverSubscription.getClientReceiver() == receiver) {
					receiverSubscriptionList.remove(i);
					receiverSubscription.setCollectingReceiver(null);
					receiverManager.removeReceiverReference(receiver);
					foundReceiver = true;
					break;
				}
			}
			if(!foundReceiver) {
				return false;
			}
			boolean changed = false;
			if(receiverSubscriptionList.size() == 0) {
				receiveSubscriptionInfo = null;
				changed = true;
			}
			else {
				if(receiveSubscriptionInfo != null) {
					ReceiveSubscriptionInfo tmpReceiveSubscriptionInfo = null;
					ReceiverSubscription firstReceiverSubscription = receiverSubscriptionList.get(0);
					if(firstReceiverSubscription != null) {
						tmpReceiveSubscriptionInfo = firstReceiverSubscription.getReceiveSubscriptionInfo().cloneObject();
						for(int i = 1; i < receiverSubscriptionList.size(); ++i) {
							ReceiverSubscription tmpSubscription = receiverSubscriptionList.get(i);
							if(tmpSubscription != null) {
								tmpReceiveSubscriptionInfo.updateSubscriptionInfo(tmpSubscription.getReceiveSubscriptionInfo());
							}
						}
						ReceiveSubscriptionInfo comparatorReceiveSubscriptionInfo = tmpReceiveSubscriptionInfo.cloneObject();
						changed = comparatorReceiveSubscriptionInfo.updateSubscriptionInfo(receiveSubscriptionInfo);
						receiveSubscriptionInfo = tmpReceiveSubscriptionInfo;
					}
					else {
						receiverSubscriptionList.clear();
						receiveSubscriptionInfo = null;
						changed = true;
					}
				}
			}
			return changed;
		}
	}

	/**
	 * Prüft ob {@link #getReceiveSubscriptionInfo()} ein Objekt ungleich <code>null</code> zurück gibt.
	 * Ist ein Objekt vorhanden, so kann dies zum abmelden beim Datenverteiler benutzt werden.
	 *
	 * @return true = {@link #getReceiveSubscriptionInfo()} wird ein Objekt zurück geben; false = {@link #getReceiveSubscriptionInfo()} gibt <code>null</code> zurück
	 */
	public final boolean isValidSubscription() {
		return receiveSubscriptionInfo != null;
	}
}
