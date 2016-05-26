/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray.ByteArrayData;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Verwaltet alle CollectingReceiver Objekte für noch angemeldete Receiver der Applikation.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CollectingReceiverManager {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Map, die zu jedem angemeldeten Receiver der Applikation den zugehörigen CollectingReceiver enthält */
	private final HashMap<ClientReceiverInterface, CollectingReceiver> _receivers = new HashMap<ClientReceiverInterface, CollectingReceiver>();

	/** Queue mit den CollectingReceiver-Objekten, die auszuliefernde Datensätze gespeichert haben */
	private UnboundedQueue<CollectingReceiver> _receiversForDelivery = new UnboundedQueue<CollectingReceiver>();

	/** Maximale Gesamtkapazität für auszuliefernde Datensätze */
	private final int _capacity;

	/** Gesamtgröße der momentan zu Auslieferung an die Applikation zwischengespeicherten Datensätze. */
	private int _size;


	public CollectingReceiverManager(final int capacity) {
		_capacity = capacity;
	}

	/**
	 * Nimmt eine neue Referenz zu einem Receiver in die Verwaltung auf. Wenn es zum angegebenen Receiver bereits einen zugehörigen CollectingReceiver gab, dann
	 * wird dessen Referenzzähler um eins erhöht. Wenn es zum angegebenen Receiver noch keinen CollectingReceiver gab, dann wird ein entsprechender
	 * CollectingReceiver erzeugt und dessen Referenzzähler auf eins gesetzt.
	 *
	 * @param receiver Receiver der Applikation zur Verarbeitung von empfangenen Datensätzen.
	 *
	 * @return Dem angegebenen Receiver zugeordneter CollectingReceiver.
	 */
	public CollectingReceiver addReceiverReference(ClientReceiverInterface receiver) {
		synchronized(_receivers) {
			CollectingReceiver collectingReceiver = _receivers.get(receiver);
			if(collectingReceiver == null) {
				collectingReceiver = new CollectingReceiver(receiver);
				_receivers.put(receiver, collectingReceiver);
			}
			collectingReceiver.incrementReferenceCount();
			return collectingReceiver;
		}
	}

	/**
	 * Freigabe einer Referenz auf einen Receiver. Beim zugeordneten CollectingReceiver wird der Referenzzähler um eins erniedrigt. Wenn der Referenzzähler den
	 * Wert 0 erreicht, dann wird der entsprechende Eintrag für den Receiver entfernt und der zugeordnete CollectingReceiver freigegeben.
	 *
	 * @param receiver
	 *
	 * @return
	 */
	public CollectingReceiver removeReceiverReference(ClientReceiverInterface receiver) {
		synchronized(_receivers) {
			CollectingReceiver collectingReceiver = _receivers.get(receiver);
			if(collectingReceiver == null) {
				throw new IllegalArgumentException("Der übergebene Receiver kann nicht entfernt werden, da kein Eintrag gefunden wurde");
			}
			if(collectingReceiver.decrementReferenceCount()) {
				_receivers.remove(receiver);
			}
			;
			return collectingReceiver;
		}
	}

	/**
	 * Gibt einen an die Applikation auszuliefernden Datensatz an den angegebenen CollectingReceiver weiter und trägt den CollectingReceiver in die
	 * Auslieferungsliste ein, falls dies noch nicht geschehen ist. Wenn die maximale Größe des Auslieferungspuffers erreicht wurde, dann blockiert diese Methode,
	 * bis die Größe des Auslieferungspuffers wieder unter die Maximalgröße gesunken ist.
	 *
	 * @param collectingReceiver CollectingReceiver bei dem der Datensatz zur Auslieferung zwischengespeichert werden soll.
	 * @param result             Auszuliefernder Datensatz.
	 *
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während er blockiert war.
	 */
	public void storeForDelivery(final CollectingReceiver collectingReceiver, final ResultData result) throws InterruptedException {
		int size = 46;
		final Data data = result.getData();
		if(data instanceof ByteArrayData) {
			size += ((ByteArrayData)data).getBytes().length;
		}
//		_debug.fine("################# storeForDelivery size", size);
		synchronized(this) {
			while(_size >= _capacity) {
				_debug.info("Puffer für auszuliefernde Datensätze ist voll");
				this.wait();
			}
			_size += size;
			if(collectingReceiver.storeForDelivery(result, size)) {
				// collectingReceiver hatte noch keine gepufferten Datensätze und ist folglich noch nicht in der Auslieferungs-Queue
				_receiversForDelivery.put(collectingReceiver);
			}
		}
	}

	/**
	 * Gibt einen an die Applikation auszuliefernden Datensatz an den angegebenen CollectingReceiver weiter und trägt den CollectingReceiver in die
	 * Auslieferungsliste ein, falls dies noch nicht geschehen ist. Diese Methode blockiert nicht, wenn die maximale Größe des Auslieferungspuffers erreicht
	 * wurde.
	 *
	 * @param collectingReceiver CollectingReceiver bei dem der Datensatz zur Auslieferung zwischengespeichert werden soll.
	 * @param result             Auszuliefernder Datensatz.
	 *
	 * @throws InterruptedException Wenn der Thread unterbrochen wurde, während er blockiert war.
	 */
	public void storeForDeliveryWithoutBlocking(final CollectingReceiver collectingReceiver, final ResultData result) {
//		_debug.fine("################# storeForDeliveryNonBlocking");
		synchronized(this) {
			if(collectingReceiver.storeForDelivery(result, 0)) {
				// collectingReceiver hatte noch keine gepufferten Datensätze und ist folglich noch nicht in der Auslieferungs-Queue
				_receiversForDelivery.put(collectingReceiver);
			}
		}
	}

	/**
	 * Startet die Auslieferung von Datenensätzen des nächsten in der Auslieferungsliste eingetragenen CollectingReceivers.
	 *
	 * @throws InterruptedException
	 */
	public void deliverOnce() throws InterruptedException {
		final CollectingReceiver collectingReceiver = _receiversForDelivery.take();
		final int deliveredSize = collectingReceiver.deliver();
		synchronized(this) {
			_size -= deliveredSize;
			this.notifyAll();
		}
	}
}
