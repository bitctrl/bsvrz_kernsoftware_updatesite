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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.sys.funclib.concurrent.UnboundedQueue;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DynamicObjectTypePublisher {

	/** Debug Ausgabe */
	private static final Debug _debug = Debug.getLogger();

	private final UnboundedQueue<ListenerNotificationInfo> _unboundedQueue = new UnboundedQueue<ListenerNotificationInfo>();

	private Thread _thread = null;

	/**
	 * Sorgt dafür, dass eine Benachrichtigung verschickt wird
	 * @param listenerNotificationInfo Benachrichtigung
	 */
	public void update(final ListenerNotificationInfo listenerNotificationInfo) {
		_unboundedQueue.put(listenerNotificationInfo);
		ensureThreadIsRunning();
	}

	private synchronized void ensureThreadIsRunning() {
		if(_thread == null || !_thread.isAlive()) {
			_thread = new Thread(new Publisher(), "ConfigDynamicObjectTypeChangeNotificationThread");
			_thread.setDaemon(true);
			_thread.start();
		}
	}

	private class Publisher implements Runnable{
		public void run() {
			ListenerNotificationInfo listenerNotificationInfo = null;
			while(!Thread.interrupted()) {
				try {
					listenerNotificationInfo = _unboundedQueue.take();
					final ListenerNotificationInfo.ListenerType listenerType = listenerNotificationInfo.getListenerType();
					final Object listener = listenerNotificationInfo.getListener();
					final DynamicObject parameter = listenerNotificationInfo.getParameter();

					switch(listenerType) {
						case INVALITDATION:
							final InvalidationListener invalidationListener = (InvalidationListener)listener;
							invalidationListener.invalidObject(parameter);
							break;
						case NAMECHANGED:
							final DynamicObjectType.NameChangeListener nameChangeListener = (DynamicObjectType.NameChangeListener)listener;
							nameChangeListener.nameChanged(parameter);
							break;
						case CREATED:
							final DynamicObjectType.DynamicObjectCreatedListener dynamicObjectCreatedListener = (DynamicObjectType.DynamicObjectCreatedListener)listener;
							dynamicObjectCreatedListener.objectCreated(parameter);
							break;
					}
				}
				catch(InterruptedException interruptedException) {
					_debug.warning("Thread wurde aufgrund einer InterruptedException beendet", interruptedException);
					break;
				}
				catch(Exception exception) {
					_debug.warning("Fehler bei der Benachrichtigung eines Listeners", exception);
				}
			}
		}
	}
}
