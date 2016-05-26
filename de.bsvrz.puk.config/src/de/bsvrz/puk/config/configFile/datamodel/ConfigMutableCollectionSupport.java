/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hilfsklasse zur Verwaltung von Anmeldungen auf Änderungen der Elemente von dynamischen Typen und dynamischen Mengen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ConfigMutableCollectionSupport {
	/** Zugehörige dynamische Menge oder dynamischer Typ. */
	private MutableCollection _mutableCollection;

	/** Map mit der Zuordnung von Simulationsvarianten zu der Liste mit den zugehörigen angemeldeten Listenern. */
	private HashMap<Short, List<MutableCollectionChangeListener>> _simVariant2Listeners = new HashMap<Short, List<MutableCollectionChangeListener>>(4);

	/**
	 * Erzeugt ein neues Verwaltungsobjekt.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ dessen Anmeldungen durch dieses Objekt verwaltet werden.
	 */
	public ConfigMutableCollectionSupport(final MutableCollection mutableCollection) {
		_mutableCollection = mutableCollection;
	}

	/**
	 * Meldet einen Listener auf Änderungen der Elemente einer dynamischen Menge oder eines dynamischen Typs unter Berücksichtigung der Simulationsvariante an.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @param changeListener Listener, der bei Änderungen der Elemente informiert werden soll.
	 */
	public void addChangeListener(final short simulationVariant, final MutableCollectionChangeListener changeListener) {
		synchronized(this) {
			if(changeListener == null) throw new IllegalArgumentException("changeListener darf nicht null sein");
			final List<MutableCollectionChangeListener> listeners = getListeners(simulationVariant, true);
			listeners.add(changeListener);
		}
	}

	/**
	 * Meldet einen zuvor angemeldeten Listener wieder ab.
	 * @param simulationVariant Simulationsvariante der entsprechenden Anmeldung.
	 * @param changeListener Listener der entsprechenden Anmeldung.
	 * @return <code>true</code>, falls ein entsprechender Listener gefunden und abgemeldet wurde.
	 */
	public boolean removeChangeListener(final short simulationVariant, final MutableCollectionChangeListener changeListener) {
		synchronized(this) {
			if(changeListener == null) throw new IllegalArgumentException("changeListener darf nicht null sein");
			final List<MutableCollectionChangeListener> listeners = getListeners(simulationVariant, false);
			if(listeners == null) return false;
			boolean result = listeners.remove(changeListener);
			if(listeners.isEmpty()) {
				_simVariant2Listeners.remove(new Short(simulationVariant));
			}
			return result;
		}
	}

	/**
	 * Informiert die angemeldeten Listener über hinzugefügte oder entfernte Elemente der zugehörigen dynamischen Zusammenstellung.
	 * @param simulationVariant Simulationsvariante auf die sich die Änderung bezieht.
	 * @param addedElements Hinzugefügte Elemente.
	 * @param removedElements Entfernte Elemente.
	 */
	public void collectionChanged(final short simulationVariant, final List<SystemObject> addedElements, final List<SystemObject> removedElements) {
		final List<MutableCollectionChangeListener> listeners;
		synchronized(this) {
			listeners = getListeners(simulationVariant, false);
			if(listeners == null) return;
		}
		for(MutableCollectionChangeListener mutableCollectionChangeListener : listeners) {
			mutableCollectionChangeListener.collectionChanged(_mutableCollection, simulationVariant, addedElements, removedElements);
		}
	}

	/**
	 * Bestimmt die Liste der angemeldeten Listener einer vorgegebenen Simulationsvariante.
	 * @param simulationVariant Simulationsvariante der gewünschten Listener
	 * @param create Wenn <code>true</code>, dann wird eine leere Liste erzeugt, wenn noch kein Listener dieser Simulationsvariante vorhanden war.
	 * @return Liste mit den gewünschten Listenern oder <code>null</code>, falls kein Listener vorhanden war und keine leere Liste erzeugt werden sollte.
	 */
	private List<MutableCollectionChangeListener> getListeners(final short simulationVariant, boolean create) {
		List<MutableCollectionChangeListener> listeners;
		final Short simulationVariantObject = new Short(simulationVariant);
		listeners = _simVariant2Listeners.get(simulationVariantObject);
		if(create && listeners == null) {
			listeners = new CopyOnWriteArrayList<MutableCollectionChangeListener>();
			_simVariant2Listeners.put(simulationVariantObject, listeners);
		}
		return listeners;
	}

}
