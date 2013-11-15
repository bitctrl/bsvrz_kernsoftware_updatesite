/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.puk.config.configFile.fileaccess.DynamicObjectInfo;
import de.bsvrz.puk.config.configFile.fileaccess.SystemObjectInformationInterface;

import java.util.*;

/**
 * Implementierung des Interfaces {@link DynamicObject} auf Seiten der Konfiguration.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 10094 $
 */
public class ConfigDynamicObject extends ConfigSystemObject implements DynamicObject {

	/** Objekt für den synchronen Zugriff auf den {@link #_listeners Listener} */
	private final Object _lockObject = new Object();

	/** Sammelt alle angemeldeten Listener-Objekte. */
	private Set<InvalidationListener> _listeners;
	
	/**
	 * Konstruktor eines dynamischen Objekts
	 *
	 * @param configurationArea Konfigurationsbereich des dynamischen Objekts
	 * @param systemObjectInfo  das korrespondierende Objekt aus den Konfigurationsdateien
	 */
	public ConfigDynamicObject(ConfigurationArea configurationArea, SystemObjectInformationInterface systemObjectInfo) {
		super(configurationArea, systemObjectInfo);
	}

	public long getValidSince() {
		return ((DynamicObjectInfo)_systemObjectInfo).getFirstValidTime();
	}

	public long getNotValidSince() {
		return ((DynamicObjectInfo)_systemObjectInfo).getFirstInvalidTime();
	}

	public boolean isValid() {
		long currentTime = System.currentTimeMillis();
		if(getValidSince() > currentTime) {
			// Objekt wird erst zu einem späteren Zeitpunkt gültig
			return false;
		}
		final long notValidSince = getNotValidSince();
		if(notValidSince != 0 && notValidSince <= currentTime) {
			// Objekt wurde bereits ungültig
			return false;
		}
		// Objekt ist gültig und wurde noch nicht auf ungültig gesetzt oder wird erst zu einem späteren Zeitpunkt ungültig
		return true;
	}

	public void addListenerForInvalidation(InvalidationListener listener) {
		synchronized(_lockObject) {
			if(_listeners == null) {
				_listeners = new HashSet<InvalidationListener>();
			}
			_listeners.add(listener);
		}
	}

	public void removeListenerForInvalidation(InvalidationListener listener) {
		synchronized(_lockObject) {
			if(_listeners == null) return;
			_listeners.remove(listener);
		}
	}

	/**
	 * Alle angemeldeten Listener werden benachrichtigt, sobald dieses dynamische Objekt {@link de.bsvrz.dav.daf.main.config.SystemObject#invalidate
	 * ungültig} gemacht wird.
	 */
	void informListeners() {
		synchronized(_lockObject) {
			if(_listeners == null) return;
			for(InvalidationListener listener : _listeners) {
				listener.invalidObject(this);
			}
		}
	}

	/**
	 * Diese Methode gibt die Simulationsvariante des dynamischen Objekts zurück.
	 *
	 * @return die Simulationsvariante dieses Objekts
	 */
	public short getSimulationVariant() {
		return ((DynamicObjectInfo)_systemObjectInfo).getSimulationVariant();
	}

	public void invalidate() throws ConfigurationChangeException {
		super.invalidate(); // prüfen, ob gelöscht werden darf

		long oldNotValidSince = getNotValidSince();
		((DynamicObjectInfo)_systemObjectInfo).setInvalid();
		long newNotValidSince = getNotValidSince();
		if(oldNotValidSince != newNotValidSince) {
			((ConfigConfigurationArea)getConfigurationArea()).setTimeOfLastChanges(ConfigConfigurationArea.KindOfLastChange.DynamicObject);
		}
		informListeners(); // alle InvalidationListener des Objekts werden benachrichtigt
		// Alle Listener des Typs informieren, dass ein Objekt ungültig geworden ist
		((ConfigDynamicObjectType)getType()).informInvalidationListener(this);
	}

	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {

    }
	
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {

    }

	public boolean isConfigurationCommunicationActive() {
		 return true;
    }
}
