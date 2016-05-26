/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.util.async;

import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AsyncRequestQueue {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final UnboundedQueue<AsyncRequest> _asyncRequestQueue;

	private Thread _asyncRequestQueueThread;

	public AsyncRequestQueue() {
		_asyncRequestQueue = new UnboundedQueue<AsyncRequest>();
		_asyncRequestQueueThread = new Thread(new AsyncRequestQueue.AsyncRequestQueueWorker(), "AsyncRequestQueueThread");
		_asyncRequestQueueThread.setDaemon(true);
	}

	public void start() {
		_asyncRequestQueueThread.start();
	}

	public void put(final AsyncRequest asyncRequest) {
		_asyncRequestQueue.put(asyncRequest);
	}

	/**
	 * TBD RS dokumentieren.
	 *
	 * @author Kappich Systemberatung
	 * @version $Revision$
	 */
	public class AsyncRequestQueueWorker implements Runnable {

		public void run() {
			try {
				while(!Thread.interrupted()) {
					AsyncRequest asyncRequest = null;
					try {
						asyncRequest = _asyncRequestQueue.take();
//					System.out.println(">>>>starting processing on asyncRequest = " + asyncRequest);
						asyncRequest.startProcessing();
//					System.out.println("<<<<<<started processing on asyncRequest = " + asyncRequest);
					}
					catch(RuntimeException e) {
						_debug.warning("Fehler beim Start einer asynchronen Anfrage: " + asyncRequest , e);
					}
				}
				_debug.warning("AsyncRequestQueueWorker beendet sich wegen eines gesetzten Interrupt-Status");

			}
			catch(InterruptedException e) {
				_debug.warning("AsyncRequestQueueWorker beendet sich wegen einer InterruptedException", e);
			}
		}
	}
}
