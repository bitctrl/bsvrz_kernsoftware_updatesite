/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet die Listener f�r die Kommunikation mit der Komfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 6074 $
 */
public class DafConfigurationCommunicationListenerSupport {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert alle Beobachter, die an Kommunikations�nderungen zu diesem Objekts interessiert sind. */
	CopyOnWriteArrayList<ConfigurationCommunicationChangeListener> _configComListeners = new CopyOnWriteArrayList<ConfigurationCommunicationChangeListener>();

	private boolean _configurationCommunicationActive = false;

	private static final byte NOT_SUBSCRIBED = 0;
	private static final byte SUBSCRIBED = 1;
	private static final byte MANAGED_IN_LOCAL_CONFIGURATION = 3;
	private static final byte MANAGED_IN_UNKNOWN_CONFIGURATION = 4;

	private byte _subscriptionState = NOT_SUBSCRIBED;

	private final SystemObject _object;

	public DafConfigurationCommunicationListenerSupport(final SystemObject object) {
		_object = object;
	}


	/**
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationCommunicationInterface#isConfigurationCommunicationActive()
	 *
	 * @param listener Beobachter f�r Zustands�nderungen der Kommunikation.
	 */
	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		synchronized(this) {
			_configComListeners.add(listener);
			ensureSubscribedState();
			if(_subscriptionState == MANAGED_IN_LOCAL_CONFIGURATION || _subscriptionState == MANAGED_IN_UNKNOWN_CONFIGURATION) {
				_configComListeners.remove(listener);
			}
		}
    }

	/**
	 * @see de.bsvrz.dav.daf.main.config.ConfigurationCommunicationInterface#removeConfigurationCommunicationChangeListener(de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener)
	 *
	 * @param listener Ein bisher f�r Zustands�nderungen der Kommunikation angemeldeter Beobachter.
	 */
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		synchronized(this) {
			_configComListeners.remove(listener);
			if(_configComListeners.isEmpty()) ensureUnsubscribedState();
		}
    }

	/**
	 * Schnittstelle um allen angemeldeten Listenern Bescheid zu geben, dass sich der Zustand der Kommunikation mit der verwaltenden Konfiguration
	 * des angegebenen Objekts ge�ndert hat.
	 *
	 * @param object Objekt ({@link de.bsvrz.dav.daf.main.config.MutableSet} oder {@link de.bsvrz.dav.daf.main.config.DynamicObject}) zu dessen verwaltenden Konfiguration sich der Kommunikationszustand ge�ndert hat
	 * @param configComStatus der aktuelle Kommunikationszustand. True bedeutet die Kommunikation steht, false zeigt eine Unterbrechung der Kommunikation an
	 */
	public void configurationCommunicationChange(ConfigurationCommunicationInterface object, boolean configComStatus) {
		_configurationCommunicationActive = configComStatus;
		java.util.Iterator it = _configComListeners.iterator();
		if(it.hasNext()) {
			while(it.hasNext()) {
				ConfigurationCommunicationChangeListener configComListener = (ConfigurationCommunicationChangeListener)it.next();
				configComListener.configurationCommunicationChange(object, configComStatus);
			}
		}
//		else {
//			// kein Listener vorhanden
//			synchronized(this) {
//				ensureUnsubscribedState();
//			}
//		}
		
	}

	public boolean isConfigurationCommunicationActive() {
		synchronized(this) {
			ensureSubscribedState();
		}
		return _configurationCommunicationActive;
	}

	private void ensureSubscribedState() {
		try {
			if(_subscriptionState == NOT_SUBSCRIBED) {
				int communicationState = ((DafDataModel)_object.getDataModel()).getRequester().subscribeConfigurationCommunicationChanges(_object);
				switch(communicationState) {
					case -2:
						_subscriptionState = MANAGED_IN_UNKNOWN_CONFIGURATION;
						_configurationCommunicationActive = false;
						break;
					case -1:
						_subscriptionState = MANAGED_IN_LOCAL_CONFIGURATION;
						_configurationCommunicationActive = true;
						break;
					case 0:
						_subscriptionState = SUBSCRIBED;
						_configurationCommunicationActive = false;
						break;
					case 1:
						_subscriptionState = SUBSCRIBED;
						_configurationCommunicationActive = true;
						break;
					default:
						_subscriptionState = NOT_SUBSCRIBED;
						_configurationCommunicationActive = false;
						_debug.error("Ung�ltige Antwort auf Anmeldung f�r Kommunikations�nderungen, communicationState", communicationState);
						break;
				}
			}
		}
		catch(RequestException e) {
			final String message =
					"Kommunikationsproblem bei Anmeldung auf �nderungen des Kommunikationsstatus f�r " + _object;
			_debug.error(message, e);
			try {
				((DafDataModel)_object.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			catch(Exception ignoredException) {
				// wird ignoriert
			}
			throw new RuntimeException(message, e);
		}
	}

	private void ensureUnsubscribedState() {
		try {
			if(_subscriptionState == SUBSCRIBED) {
				((DafDataModel)_object.getDataModel()).getRequester().unsubscribeConfigurationCommunicationChanges(_object);
				_subscriptionState = NOT_SUBSCRIBED;
			}
		}
		catch(RequestException e) {
			final String message =
					"Kommunikationsproblem bei Abmeldung auf �nderungen des Kommunikationsstatus f�r " + _object;
			_debug.error(message, e);
			try {
				((DafDataModel)_object.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			catch(Exception ignoredException) {
				// wird ignoriert
			}
			throw new RuntimeException(message, e);
		}
	}
}
