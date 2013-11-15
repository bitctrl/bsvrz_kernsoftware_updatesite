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

import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hilfsklasse zur Verwaltung von Anmeldungen auf �nderungen der Elemente von dynamischen Typen und dynamischen Mengen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5982 $
 */
public class DafMutableCollectionSupport {
	/** Logger f�r Debug-Ausgaben. */
	private static final Debug _debug = Debug.getLogger();

	/** Zugeh�rige dynamische Menge oder dynamischer Typ. */
	private MutableCollection _mutableCollection;

	/** Map mit der Zuordnung von Simulationsvarianten zu einem Hilfsobjekt an dem die Liste der zugeh�rigen angemeldeten Listenern etc. gespeichert ist. */
	private HashMap<Short, ListenersAndElements> _simVariant2ListenersAndElements = new HashMap<Short, ListenersAndElements>(4);

	/**
	 * Erzeugt ein neues Verwaltungsobjekt.
	 * @param mutableCollection dynamische Menge oder dynamischer Typ dessen Anmeldungen durch dieses Objekt verwaltet werden.
	 */
	public DafMutableCollectionSupport(final MutableCollection mutableCollection) {
		_mutableCollection = mutableCollection;
	}

	/**
	 * Meldet einen Listener auf �nderungen der Elemente einer dynamischen Menge oder eines dynamischen Typs unter Ber�cksichtigung der Simulationsvariante an.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @param changeListener Listener, der bei �nderungen der Elemente informiert werden soll.
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
	 * Informiert die angemeldeten Listener �ber hinzugef�gte oder entfernte Elemente der zugeh�rigen dynamischen Zusammenstellung.
	 * @param simulationVariant Simulationsvariante auf die sich die �nderung bezieht.
	 * @param addedElements Hinzugef�gte Elemente.
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
	 * Stellt sicher, dass eine und h�chstens eine Anmeldung bei der Konfiguration auf �nderungen der Elemente der zugeh�rigen dynamischen Zusammenstellung
	 * unter der angegebenen Simulationsvariante vorliegt.
 	 * @param listenersAndElements Hilfsobjekt aus der Map <code>_simVariant2ListenersAndElements</code> f�r die entsprechende Simulationsvariante.
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
						"Kommunikationsproblem bei Anmeldung auf �nderungen der dynamischen Zusammenstellung " + _mutableCollection.getPidOrNameOrId();
				_debug.error(message, e);
				((DafDataModel)_mutableCollection.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			listenersAndElements.setSubscribed(true);
		}
	}

	/**
	 * Stellt sicher, dass keine Anmeldung bei der Konfiguration auf �nderungen der Elemente der zugeh�rigen dynamischen Zusammenstellung unter der angegebenen
	 * Simulationsvariante vorliegt.
 	 * @param listenersAndElements Hilfsobjekt aus der Map <code>_simVariant2ListenersAndElements</code> f�r die entsprechende Simulationsvariante.
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
						"Kommunikationsproblem bei Abmeldung auf �nderungen der dynamischen Zusammenstellung " + _mutableCollection.getPidOrNameOrId();
				_debug.error(message, e);
				((DafDataModel)_mutableCollection.getDataModel()).getConnection().disconnect(true, message + " " + e.getMessage());
			}
			listenersAndElements.setSubscribed(false);
		}
	}

	/**
	 * Bestimmt das Hilfsobjekt mit den angemeldeten Listenern etc. aus der Map <code>_simVariant2ListenersAndElements</code> f�r die entsprechende
	 * Simulationsvariante.
 	 * @param simulationVariant Simulationsvariante des Hilfsobjekts mit den gew�nschten Listenern
	 * @param create Wenn <code>true</code>, dann wird eine neues Hilfsobjekt erzeugt und in die Map eingetragen, wenn noch kein Hilfsobjekt mit den
	 * angemeldeten Listenern f�r diese Simulationsvariante vorhanden war.
	 * @return Hilfsobjekt mit den gew�nschten Listenern etc. oder <code>null</code>, falls kein Hilfsobjekt vorhanden war und kein neues erzeugt werden sollte.
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
	 * Bestimmt die aktuellen Elemente der zugeh�rigen dynamischen Menge oder des zugeh�rigen dynamischen Typs unter Ber�cksichtigung der Simulationsvariante.
	 * @param simulationVariant Simulationsvariante unter der die dynamische Zusammenstellung betrachtet werden soll.
	 * @return Aktuelle Elemente der dynamischen Menge oder des dynamischen Typs unter Ber�cksichtigung der Simulationsvariante.
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
	 * Hilfsobjekt mit den angemeldeten Listenern etc. aus der Map <code>_simVariant2ListenersAndElements</code> f�r die entsprechende Simulationsvariante.
	 */
	private static class ListenersAndElements {

		/** Angemeldete Listener */
		private final CopyOnWriteArrayList<MutableCollectionChangeListener> _listeners = new CopyOnWriteArrayList<MutableCollectionChangeListener>();

		/** Aktuelle Elemente */
		private final ArrayList<SystemObject> _elements = new ArrayList<SystemObject>();

		/** Flag, das <code>true</code> ist, wenn bereits eine entsprechende Anmeldung bei der Konfiguration get�tigt wurde.*/
		private boolean _subscribed = false;

		/** @return Liefert die angemeldeten Listener zur�ck. */
		public List<MutableCollectionChangeListener> getListeners() {
			return _listeners;
		}

		/** @return Liefert die aktuellen Elemente zur�ck.*/
		public List<SystemObject> getElements() {
			return _elements;
		}

		/** @return <code>true</code>, wenn bereits eine entsprechende Anmeldung bei der Konfiguration get�tigt wurde.*/
		public boolean isSubscribed() {
			return _subscribed;
		}

		/**
		 * Setzt, die Kennung, ob bereits eine entsprechende Anmeldung bei der Konfiguration get�tigt wurde.
		 * @param subscribed <code>true</code>, wenn bereits eine entsprechende Anmeldung bei der Konfiguration get�tigt wurde.
		 * */
		public void setSubscribed(final boolean subscribed) {
			_subscribed = subscribed;
		}
	}
}
