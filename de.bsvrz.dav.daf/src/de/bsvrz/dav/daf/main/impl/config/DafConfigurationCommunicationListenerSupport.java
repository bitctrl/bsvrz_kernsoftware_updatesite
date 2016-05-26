/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.main.impl.config;

import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet die Listener für die Kommunikation mit der Komfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafConfigurationCommunicationListenerSupport {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Speichert alle Beobachter, die an Kommunikationsänderungen zu diesem Objekts interessiert sind. */
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
	 * @param listener Beobachter für Zustandsänderungen der Kommunikation.
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
	 * @param listener Ein bisher für Zustandsänderungen der Kommunikation angemeldeter Beobachter.
	 */
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		synchronized(this) {
			_configComListeners.remove(listener);
			if(_configComListeners.isEmpty()) ensureUnsubscribedState();
		}
    }

	/**
	 * Schnittstelle um allen angemeldeten Listenern Bescheid zu geben, dass sich der Zustand der Kommunikation mit der verwaltenden Konfiguration
	 * des angegebenen Objekts geändert hat.
	 *
	 * @param object Objekt ({@link de.bsvrz.dav.daf.main.config.MutableSet} oder {@link de.bsvrz.dav.daf.main.config.DynamicObject}) zu dessen verwaltenden Konfiguration sich der Kommunikationszustand geändert hat
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

	/**
	 * Stellt sicher, dass die Anmeldung auf Änderungen des Kommunikationsstatus bei der lokalen Konfiguration erfolgt ist.
	 */
	private void ensureSubscribedState() {
		try {
			if(_subscriptionState == NOT_SUBSCRIBED) {
				int communicationState = ((DafDataModel)_object.getDataModel()).getRequester().subscribeConfigurationCommunicationChanges(_object);
				switch(communicationState) {
					case -2: // Objekt weder lokal noch remote gefunden
						_subscriptionState = MANAGED_IN_UNKNOWN_CONFIGURATION;
						_configurationCommunicationActive = false;
						break;
					case -1: // Menge wird lokal verwaltet
						_subscriptionState = MANAGED_IN_LOCAL_CONFIGURATION;
						_configurationCommunicationActive = true;
						break;
					case 0: // Menge wird remote verwaltet und Kommunikation zwischen lokaler und Remote-Konfiguration funktioniert nicht
						_subscriptionState = SUBSCRIBED;
						_configurationCommunicationActive = false;
						break;
					case 1: // Menge wird remote verwaltet und Kommunikation zwischen lokaler und Remote-Konfiguration funktioniert
						_subscriptionState = SUBSCRIBED;
						_configurationCommunicationActive = true;
						break;
					default:
						_subscriptionState = NOT_SUBSCRIBED;
						_configurationCommunicationActive = false;
						_debug.error("Ungültige Antwort auf Anmeldung für Kommunikationsänderungen, communicationState", communicationState);
						break;
				}
			}
		}
		catch(RequestException e) {
			final String message =
					"Kommunikationsproblem bei Anmeldung auf Änderungen des Kommunikationsstatus für " + _object;
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
					"Kommunikationsproblem bei Abmeldung auf Änderungen des Kommunikationsstatus für " + _object;
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
