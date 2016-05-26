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

import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hilfsklasse zur Verwaltung von Anmeldungen auf Änderungen der Elemente von dynamischen Typen und dynamischen Mengen.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DafMutableCollectionSupport {
	/** Logger für Debug-Ausgaben. */
	private static final Debug _debug = Debug.getLogger();

	/** Zugehörige dynamische Menge oder dynamischer Typ. */
	private MutableCollection _mutableCollection;

	/** Map mit der Zuordnung von Simulationsvarianten zu einem Hilfsobjekt an dem die Liste der zugehörigen angemeldeten Listenern etc. gespeichert ist. */
	private HashMap<Short, ListenersAndElements> _simVariant2ListenersAndElements = new HashMap<Short, ListenersAndElements>(4);

	/**
	 * Erzeugt ein neues Verwaltungsobjekt.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ dessen Anmeldungen durch dieses Objekt verwaltet werden.
	 */
	public DafMutableCollectionSupport(final MutableCollection mutableCollection) {
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
			final ListenersAndElements listenersAndElements = getListenersAndElements(simulationVariant, true);
			listenersAndElements.getListeners().add(changeListener);
			ensureSubscribedState(listenersAndElements, simulationVariant);
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
			final ListenersAndElements listenersAndElements = getListenersAndElements(simulationVariant, false);
			if(listenersAndElements == null) return false;
			boolean result = listenersAndElements.getListeners().remove(changeListener);
			if(listenersAndElements.getListeners().isEmpty()) {
				ensureUnsubscribedState(listenersAndElements, simulationVariant);
				_simVariant2ListenersAndElements.remove(new Short(simulationVariant));
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
		final ListenersAndElements listenersAndElements;
		synchronized(this) {
			listenersAndElements = getListenersAndElements(simulationVariant, false);
			if(listenersAndElements == null) return;
			final List<SystemObject> savedElements = listenersAndElements.getElements();
			savedElements.removeAll(removedElements);
			savedElements.addAll(addedElements);
			if(!listenersAndElements.isSubscribed()) return;
			if(listenersAndElements.getListeners().isEmpty()) {
				ensureUnsubscribedState(listenersAndElements, simulationVariant);
				_simVariant2ListenersAndElements.remove(new Short(simulationVariant));
				return;
			}
		}
		for(MutableCollectionChangeListener mutableCollectionChangeListener : listenersAndElements.getListeners()) {
			mutableCollectionChangeListener.collectionChanged(_mutableCollection, simulationVariant, addedElements, removedElements);
		}
	}

	/**
	 * Stellt sicher, dass eine und höchstens eine Anmeldung bei der Konfiguration auf Änderungen der Elemente der zugehörigen dynamischen Zusammenstellung
	 * unter der angegebenen Simulationsvariante vorliegt.
 	 * @param listenersAndElements Hilfsobjekt aus der Map <code>_simVariant2ListenersAndElements</code> für die entsprechende Simulationsvariante.
	 * @param simulationVariant Simulationsvariante, die angemeldet werden soll.
	 */
	private void ensureSubscribedState(final ListenersAndElements listenersAndElements, final short simulationVariant) {
		if(!listenersAndElements.isSubscribed()) {
			try {
				final Collection<SystemObject> elements;
				elements = ((DafDataModel)_mutableCollection.getDataModel()).getRequester().subscribeMutableCollectionChanges(
						_mutableCollection, simulationVariant
				);
				final List<SystemObject> savedElements = listenersAndElements.getElements();
				savedElements.clear();
				savedElements.addAll(elements);
			}
			catch(RequestException e) {
				final String message =
						"Kommunikationsproblem bei Anmeldung auf Änderungen der dynamischen Zusammenstellung " + _mutableCollection.getPidOrNameOrId();
				_debug.error(message, e);
				((DafDataModel)_mutableCollection.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			listenersAndElements.setSubscribed(true);
		}
	}

	/**
	 * Stellt sicher, dass keine Anmeldung bei der Konfiguration auf Änderungen der Elemente der zugehörigen dynamischen Zusammenstellung unter der angegebenen
	 * Simulationsvariante vorliegt.
 	 * @param listenersAndElements Hilfsobjekt aus der Map <code>_simVariant2ListenersAndElements</code> für die entsprechende Simulationsvariante.
	 * @param simulationVariant Simulationsvariante, die abgemeldet werden soll.
	 */
	private void ensureUnsubscribedState(final ListenersAndElements listenersAndElements, final short simulationVariant) {
		if(listenersAndElements.isSubscribed()) {
			try {
				((DafDataModel)_mutableCollection.getDataModel()).getRequester().unsubscribeMutableCollectionChanges(_mutableCollection, simulationVariant);
				final List<SystemObject> savedElements = listenersAndElements.getElements();
				savedElements.clear();
			}
			catch(RequestException e) {
				final String message =
						"Kommunikationsproblem bei Abmeldung auf Änderungen der dynamischen Zusammenstellung " + _mutableCollection.getPidOrNameOrId();
				_debug.error(message, e);
				((DafDataModel)_mutableCollection.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			listenersAndElements.setSubscribed(false);
		}
	}

	/**
	 * Bestimmt das Hilfsobjekt mit den angemeldeten Listenern etc. aus der Map <code>_simVariant2ListenersAndElements</code> für die entsprechende
	 * Simulationsvariante.
 	 * @param simulationVariant Simulationsvariante des Hilfsobjekts mit den gewünschten Listenern
	 * @param create Wenn <code>true</code>, dann wird eine neues Hilfsobjekt erzeugt und in die Map eingetragen, wenn noch kein Hilfsobjekt mit den
	 * angemeldeten Listenern für diese Simulationsvariante vorhanden war.
	 * @return Hilfsobjekt mit den gewünschten Listenern etc. oder <code>null</code>, falls kein Hilfsobjekt vorhanden war und kein neues erzeugt werden sollte.
	 */
	private ListenersAndElements getListenersAndElements(final short simulationVariant, boolean create) {
		ListenersAndElements listenersAndElements;
		final Short simulationVariantObject = new Short(simulationVariant);
		listenersAndElements = _simVariant2ListenersAndElements.get(simulationVariantObject);
		if(create && listenersAndElements == null) {
			listenersAndElements = new ListenersAndElements();
			_simVariant2ListenersAndElements.put(simulationVariantObject, listenersAndElements);
		}
		return listenersAndElements;
	}

	/**
	 * Bestimmt die aktuellen Elemente der zugehörigen dynamischen Menge oder des zugehörigen dynamischen Typs unter Berücksichtigung der Simulationsvariante.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @return Aktuelle Elemente der dynamischen Menge oder des dynamischen Typs unter Berücksichtigung der Simulationsvariante.
	 */
	public List<SystemObject> getElements(final short simulationVariant) {
		synchronized(this) {
			final ListenersAndElements listenersAndElements = getListenersAndElements(simulationVariant, true);
			ensureSubscribedState(listenersAndElements, simulationVariant);
			final List<SystemObject> savedElements = listenersAndElements.getElements();
			return new ArrayList<SystemObject>(savedElements);
		}
	}

	/**
	 * Hilfsobjekt mit den angemeldeten Listenern etc. aus der Map <code>_simVariant2ListenersAndElements</code> für die entsprechende Simulationsvariante.
	 */
	private static class ListenersAndElements {

		/** Angemeldete Listener */
		private final CopyOnWriteArrayList<MutableCollectionChangeListener> _listeners = new CopyOnWriteArrayList<MutableCollectionChangeListener>();

		/** Aktuelle Elemente */
		private final ArrayList<SystemObject> _elements = new ArrayList<SystemObject>();

		/** Flag, das <code>true</code> ist, wenn bereits eine entsprechende Anmeldung bei der Konfiguration getätigt wurde.*/
		private boolean _subscribed = false;

		/** @return Liefert die angemeldeten Listener zurück. */
		public List<MutableCollectionChangeListener> getListeners() {
			return _listeners;
		}

		/** @return Liefert die aktuellen Elemente zurück.*/
		public List<SystemObject> getElements() {
			return _elements;
		}

		/** @return <code>true</code>, wenn bereits eine entsprechende Anmeldung bei der Konfiguration getätigt wurde.*/
		public boolean isSubscribed() {
			return _subscribed;
		}

		/**
		 * Setzt, die Kennung, ob bereits eine entsprechende Anmeldung bei der Konfiguration getätigt wurde.
		 * @param subscribed <code>true</code>, wenn bereits eine entsprechende Anmeldung bei der Konfiguration getätigt wurde.
		 * */
		public void setSubscribed(final boolean subscribed) {
			_subscribed = subscribed;
		}
	}
}
