/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.asyncReceiver.
 * 
 * de.bsvrz.sys.funclib.asyncReceiver is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.asyncReceiver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.asyncReceiver; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.asyncReceiver;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ResultData;

import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;

/**
 * Klasse, die beim Empfang von Datens�tzen zur Entkopplung von Datenverteiler-Applikationsfunktionen und
 * Verarbeitungsfunktionen eingesetzt werden kann. Empfangene Datens�tze werden in eine Queue eingetragen und asynchron
 * von einem eigenen Thread an die eigentlichen Empf�nger weitergeleitet. <br/> Wenn zum Beispiel statt
 * <pre>
 *    ClientReceiverInterface receiver= new ....;
 *    connection.subscribeReceiver(receiver, ... );
 * </pre>
 * der folgende Code bei der Anmeldung auf bestimmte Daten benutzt wird:
 * <pre>
 *    ClientReceiverInterface receiver= new ....;
 *    ClientReceiverInterface asyncReceiver= new AsyncReceiver(receiver);
 *    connection.subscribeReceiver(asyncReceiver, ... );
 * </pre>
 * dann wird jeder empfangene Datensatz im Hintergrund verarbeitet. <br/> Es ist zu beachten, dass die Klasse ein
 * einzigen Thread verwendet, der asynchron zu den anderen Aktivit�ten der Applikation und insbesondere asynchron zum
 * update-Thread der Datenverteiler-Applikationsfunktionen arbeitet. Die in einer Queue zwischengespeicherten Datens�tze
 * werden sequentiell an die update-Methode des eigentlichen Empf�ngers weiterleitet.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5058 $
 */
public class AsyncReceiver implements ClientReceiverInterface {
	private static final Debug _debug = Debug.getLogger();

	private static class QueueEntry {
		private final ClientReceiverInterface _syncReceiver;
		private final ResultData[] _results;

		public ClientReceiverInterface getSyncReceiver() {
			return _syncReceiver;
		}

		public ResultData[] getResults() {
			return _results;
		}

		public QueueEntry(ClientReceiverInterface syncReceiver, ResultData[] results) {
			_syncReceiver = syncReceiver;
			_results = results;
		}
	}

	private static final class QueueWorker implements Runnable {
		public void run() {
			while(true) {
				try {
					QueueEntry entry = (QueueEntry)_queueSingleton.take();
					if(entry == null) {
						// null signalisiert dem Thread, dass er sich beenden soll
						break;
					}
					entry.getSyncReceiver().update(entry.getResults());
				}
				catch(RuntimeException e) {
					_debug.error("Fehler bei der asynchronen Bearbeitung von empfangenen Daten: " + e);
					e.printStackTrace();
					System.exit(1);
				}
				catch(InterruptedException e) {
					_debug.warning("Unterbrechung bei der asynchronen Bearbeitung von empfangenen Daten");
					break;
				}

			}
		}
	}

	private static final UnboundedQueue _queueSingleton;

	static {
		_queueSingleton = new UnboundedQueue();
		Thread thread = new Thread(new QueueWorker(), "AsyncReceiverQueueWorker");
		thread.setDaemon(true);
		thread.start();
	}

	private final ClientReceiverInterface _syncReceiver;

	/**
	 * Erzeugt ein neues Empf�ngerobjekt mit asynchroner Weiterleitung empfangener Daten an das �bergebene
	 * Empf�ngerobjekt.
	 *
	 * @param syncReceiver Empf�ngerobjekt, an das empfangene Daten asynchron zur Verarbeitung weitergeleitet werden
	 *                     sollen.
	 */

	public AsyncReceiver(ClientReceiverInterface syncReceiver) {
		_syncReceiver = syncReceiver;
	}

	/**
	 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den
	 * Datenverteiler-Applikationsfunktionen aufgerufen wird.
	 * <p/>
	 * Empfangene Datens�tze werden in eine Queue eingetragen und asynchron an das eigentliche Empf�ngerobjekt zur
	 * Verarbeitung weitergeleitet.
	 *
	 * @param results Feld mit den empfangenen Ergebnisdatens�tzen.
	 */
	public void update(ResultData results[]) {
		_queueSingleton.put(new QueueEntry(_syncReceiver, results));
	}

}
