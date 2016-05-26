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

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * TBD RS dokumentieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class AbstractAsyncRequest implements AsyncRequest {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private AsyncRequestCompletion _asyncRequestCompletion;

	private AsyncRequestQueue _asyncRequestQueue;

	public void setCompletion(final AsyncRequestCompletion asyncRequestCompletion) {
		_asyncRequestCompletion = asyncRequestCompletion;
	}

	public void callAsyncCompletion() {
//		System.out.println("AbstractAsyncRequest.callAsyncCompletion");
		try {
			if(_asyncRequestCompletion != null) {
				new AsyncCallCompletionRequest(this).enqueueTo(getAsyncRequestQueue());
			}
		}
		catch(Exception e) {
			e.printStackTrace(System.out);
			_debug.error("Fehler beim asynchronen Versand einer Konfigurationsantwort: ", e);
		}

	}

	public void enqueueTo(AsyncRequestQueue queue) {
		setAsyncRequestQueue(queue);
		if(queue == null) {
			startProcessing();
		}
		else {
			queue.put(this);
		}
	}

	private void setAsyncRequestQueue(final AsyncRequestQueue asyncRequestQueue) {
		_asyncRequestQueue = asyncRequestQueue;
	}

	public AsyncRequestCompletion getAsyncRequestCompletion() {
		return _asyncRequestCompletion;
	}

	private AsyncRequestQueue getAsyncRequestQueue() {
		return _asyncRequestQueue;
	}
}
