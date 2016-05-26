/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.InvalidationListener;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Klasse, die den Zugriff auf dynamische Objekte seitens der Datenverteiler-Applikationsfunktionen ermöglicht.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafDynamicObject extends DafSystemObject implements DynamicObject {

	/** Seit wann existiert das Objekt */
	private long _validSince;

	/** Seit wann existiert das Objekt nicht mehr. Der Wert 0 bedeutet, dass das Objekt noch immer gültig ist. */
	private long _notValidSince = 0;

	/** Alle Objekte, die benachrichtigt werden sollen sobald das dynamische Objekt ungültig wird. */
	private Set<InvalidationListener> _invalidationListeners = new HashSet<InvalidationListener>();
	
	/**Delegations-Klasse für das Interface {@link ConfigurationCommunicationInterface} */
	DafConfigurationCommunicationListenerSupport _configComHelper;


	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafDynamicObject(DafDataModel dataModel) {
		super(dataModel);
		_internType = DYNAMIC_OBJECT;
		_configComHelper = new DafConfigurationCommunicationListenerSupport(this);
	}

	/** Erzeugt ein neues Objekt mit den angegebenen Eigenschaften */
	public DafDynamicObject(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			long validSince,
			long notValidSince,
			long configurationAreaId
	) {
		super(
				id, pid, name, typId, state, error, dataModel, configurationAreaId
		);
		_internType = DYNAMIC_OBJECT;
		_validSince = validSince;
		_notValidSince = notValidSince;
		_configComHelper = new DafConfigurationCommunicationListenerSupport(this);
	}

	public String parseToString() {
		String str = super.parseToString();
		str += "Gültig ab: " + new Date(_validSince) + "\n";
		str += "Gültig bis: " + new Date(_notValidSince) + "\n";
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeLong(_validSince);
		out.writeLong(_notValidSince);
	}

	public void read(DataInputStream in) throws IOException {
		super.read(in);
		_validSince = in.readLong();
		_notValidSince = in.readLong();
	}

	@Override
	public void read(final Deserializer deserializer) throws IOException {
		super.read(deserializer);
		_validSince = deserializer.readLong();
		_notValidSince = deserializer.readLong();
		_configurationAreaId = deserializer.readLong(); // muss hier gelesen werden, da nach Gültigkeit serialisiert
	}

	public final long getValidSince() {
		return _validSince;
	}

	public final long getNotValidSince() {
		return _notValidSince;
	}

	public void addListenerForInvalidation(InvalidationListener listener) {
		synchronized(_invalidationListeners) {
			_invalidationListeners.add(listener);
		}
	}

	public void removeListenerForInvalidation(InvalidationListener listener) {
		synchronized(_invalidationListeners) {
			_invalidationListeners.remove(listener);
		}
	}

	/** Benachrichtigt alle Observer, dass sich der Zustand dieses Objektes auf "ungültig" geändert hat. */
	private void informInvalidationListeners() {
		final List<InvalidationListener> invalidationListenersCopy;
		synchronized(_invalidationListeners) {
			invalidationListenersCopy = new ArrayList<InvalidationListener>(_invalidationListeners);
		}
		for(InvalidationListener invalidationListener : invalidationListenersCopy) {
			invalidationListener.invalidObject(this);
		}
	}

	/**
	 * Dieser Aufruf setzt den Zeitpunkt, ab dem das Objekt nicht mehr gültig ist.
	 * <p>
	 * Dieser Wert wird nicht an die Konfiguration weitergereicht sondern nur am Objekt vermerkt. Außerdem werden die angemeldeten InvalidationListener
	 * aufgerufen.
	 *
	 * @param notValidSince Zeitpunkt an dem das Objekt ungültig wurde
	 */
	void storeNotValidSince(final long notValidSince) {
		// Verhindert, dass ein Listener sich einträgt während der Wert gesetzt wird und somit nicht informiert wird
		synchronized(_invalidationListeners) {
			_notValidSince = notValidSince;
			informInvalidationListeners();
		}
	}

	/**
	 * Dieser Aufruf setzt den Zeitpunkt, ab dem das Objekt nicht mehr gültig ist.
	 * <p>
	 * Dieser Wert wird nicht an die Konfiguration weitergereicht sondern nur am Objekt vermerkt. Die angemeldeten InvalidationListener werden durch diesen Aufruf
	 * nicht aufgerufen.
	 *
	 * @param notValidSince Zeitpunkt an dem das Objekt ungültig wurde
	 */
	public void setNotValidSince(final long notValidSince) {
		_notValidSince = notValidSince;
	}

	public final boolean isValid() {
		if(_notValidSince == 0) {
			// Der Wert 0 bedeutet, dass das Objekt noch gültig ist. Jeder Wert größer 0 bedeutet, dass das Objekt zu diesem zeitpunkt ungültig geworden ist.
			return true;
		}
		else {
			return false;
		}
	}

	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		_configComHelper.addConfigurationCommunicationChangeListener(listener);
    }
	
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
		_configComHelper.removeConfigurationCommunicationChangeListener(listener);
    }
	
	public void configurationCommunicationChange(boolean configComStatus){
		_configComHelper.configurationCommunicationChange(this, configComStatus);
	}
	
	public boolean isConfigurationCommunicationActive() {
		return _configComHelper.isConfigurationCommunicationActive();
    }
}
