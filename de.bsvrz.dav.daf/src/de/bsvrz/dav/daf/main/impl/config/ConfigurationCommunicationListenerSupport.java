/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet die Listener f�r die Kommunikation mit der Komfiguration.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 6072 $
 */
public class ConfigurationCommunicationListenerSupport {
	
	/** Speichert alle Beobachter, die an Kommunikations�nderungen zu diesem Objekts interessiert sind. */
	CopyOnWriteArrayList<ConfigurationCommunicationChangeListener> _configComListeners = new CopyOnWriteArrayList<ConfigurationCommunicationChangeListener>();


	/**
	 * @see ConfigurationCommunicationInterface#isConfigurationCommunicationActive()
	 * 
	 * @param listener Beobachter f�r Zustands�nderungen der Kommunikation.
	 */
	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		_configComListeners.add(listener);
    }
	
	/**
	 * @see ConfigurationCommunicationInterface#removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener)
	 * 
	 * @param listener Ein bisher f�r Zustands�nderungen der Kommunikation angemeldeter Beobachter.
	 */
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		_configComListeners.remove(listener);
    }
	
	/**
	 * Schnittstelle um allen angemeldeten Listenern Bescheid zu geben, dass sich der Zustand der Kommunikation mit der verwaltenden Konfiguration 
	 * des angegebenen Objekts ge�ndert hat.
	 * 
	 * @param object Objekt ({@link de.bsvrz.dav.daf.main.config.MutableSet} oder {@link de.bsvrz.dav.daf.main.config.DynamicObject}) zu dessen verwaltenden Konfiguration sich der Kommunikationszustand ge�ndert hat
	 * @param configComStatus der aktuelle Kommunikationszustand. True bedeutet die Kommunikation steht, false zeigt eine Unterbrechung der Kommunikation an
	 */
	public void configurationCommunicationChange(ConfigurationCommunicationInterface object, boolean configComStatus) {
		for(ConfigurationCommunicationChangeListener configComListener : _configComListeners) {
	        configComListener.configurationCommunicationChange(object, configComStatus);
        }
	}

}
