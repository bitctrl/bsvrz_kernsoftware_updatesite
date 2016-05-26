/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationCommunicationChangeListener;
import de.bsvrz.dav.daf.main.config.MutableSetChangeListener;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.config.request.ConfigurationRequester;
import de.bsvrz.dav.daf.main.impl.config.request.RequestException;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.MutableCollectionChangeListener;
import de.bsvrz.dav.daf.main.config.MutableCollection;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Mit dieser Klasse kann auf dynamische Mengen zugegriffen werden. Diesen Mengen können online Elemente hinzugefügt und entfernt, ohne dass eine neue
 * Konfigurationsversion erstellt werden muss.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */

public class DafMutableSet extends DafObjectSet implements MutableSet {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	/** Repräsentiert die Konfiguration. */
	private final DafDataModel _configuration;

	/** Hiermit werden Konfigurationsanfragen gestellt. */
	private ConfigurationRequester _requester = null;

//	/** Speichert alle Beobachter, die an Änderungen der dynamischen Menge interessiert sind. */
//	private final Set<MutableSetChangeListener> _observer = new HashSet<MutableSetChangeListener>();
	
	/**Verwaltet die Listener für die Komunikation mit der Konfiguration */
	DafConfigurationCommunicationListenerSupport _configComSupport;

	/** Objekt zur Verwaltung von Anmeldungen auf Änderungen der Elemente dieses Typs. */
	private DafMutableCollectionSupport _mutableCollectionSupport = new DafMutableCollectionSupport(this);

	private HashMap<MutableSetChangeListener, MutableCollectionChangeListener> _observer2listener = new HashMap<MutableSetChangeListener, MutableCollectionChangeListener>();

	/**
	 * Erzeugt ein neues Objekt dessen Eigenschaften im Anschluss mit der read-Methode eingelesen werden sollten.
	 *
	 * @param dataModel DataModel Implementierung, der das neue Objekt zugeordnet ist.
	 */
	public DafMutableSet(DafDataModel dataModel) {
		super(dataModel);
		_internType = MUTABLE_SET;	// "Dynamische Menge"
		_configuration = dataModel;
		_configComSupport = new DafConfigurationCommunicationListenerSupport(this);
	}

	/** Erzeugt ein Objekt einer dynamischen Menge. */
	public DafMutableSet(
			long id,
			String pid,
			String name,
			long typId,
			byte state,
			String error,
			DafDataModel dataModel,
			short validFromVersionNumber,
			short validToVersionNumber,
			long responsibleObjectId,
			long[] setIds,
			ArrayList setElementIds
	) {
		super(id, pid, name, typId, state, error, dataModel, validFromVersionNumber, validToVersionNumber, responsibleObjectId, setIds, setElementIds);
		_internType = MUTABLE_SET;	// "Dynamische Menge"
		_configuration = dataModel;
		_configComSupport = new DafConfigurationCommunicationListenerSupport(this);
	}

	/**
	 * Gibt die Elemente zurück, die sich aktuell in der dynamischen Menge befinden.
	 *
	 * @return die Elemente, die aktuell in der dynamischen Menge sind
	 */
	public List<SystemObject> getElements() {
		final short simulationVariant = _dataModel.getConnection().getClientDavParameters().getSimulationVariant();
		return getElements(simulationVariant);
//		return getElements(Long.MAX_VALUE, Long.MAX_VALUE, false);
	}

	/**
	 * Gibt die Elemente zurück, die sich zu einem bestimmten Zeitpunkt in der dynamischen Menge befinden.
	 *
	 * @param time der zu betrachtende Zeitpunkt
	 *
	 * @return die Elemente, die zum angegebenen Zeitpunkt in der dynamischen Menge sind
	 */
	public List getElements(long time) {
		return getElements(time, time, false);
	}

	/**
	 * Gibt die Elemente zurück, die innerhalb des angegebenen Zeitraumes gültig sind bzw. gültig waren.
	 *
	 * @param startTime Beginn des zu betrachtenden Zeitraumes
	 * @param endTime   Ende des zu betrachtenden Zeitraumes
	 *
	 * @return die Elemente, die innerhalb des angegebenen Zeitraumes in der dynamischen Menge sind bzw. waren
	 */
	public List getElementsInPeriod(long startTime, long endTime) {
		return getElements(startTime, endTime, false);
	}

	/**
	 * Gibt die Elemente zurück, die während des gesamten Zeitraumes gültig waren.
	 *
	 * @param startTime Beginn des zu betrachtenden Zeitraumes
	 * @param endTime   Ende des zu betrachtenden Zeitraumes
	 *
	 * @return die Elemente, die während des gesamten Zeitraums in der dynamischen Menge waren
	 */
	public List getElementsDuringPeriod(long startTime, long endTime) {
		return getElements(startTime, endTime, true);
	}

	/**
	 * Diese interne Methode führt die Anfrage nach allen Elementen dieser Menge bei der Konfiguration durch.
	 *
	 * @param startTime               Beginn des zu betrachtenden Zeitraumes
	 * @param endTime                 Ende des zu betrachtenden Zeitraumes
	 * @param validDuringEntirePeriod ob die Elemente während des gesamten Zeitraumes in der Menge waren
	 *
	 * @return die Elemente in der dynamischen Menge oder eine leere Liste
	 */
	private List getElements(long startTime, long endTime, boolean validDuringEntirePeriod) {
		SystemObject[] objects = null;
		try {
			if(_requester == null) _requester = _configuration.getRequester();
			objects = _requester.getElements(this, startTime, endTime, validDuringEntirePeriod);
		}
		catch(RequestException ex) {
			_debug.error("Fehler führt zum Beenden der Verbindung zum Datenverteiler", ex);
			_configuration.getConnection().disconnect(true, ex.getMessage());
		}
		return (objects != null ? Arrays.asList(objects) : new LinkedList());
	}

	/**
	 * Meldet einen Beobachter an, der informiert wird, falls sich an der dynamischen Menge etwas ändert.
	 *
	 * @param observer Der Beobachter
	 */
	public void addChangeListener(final MutableSetChangeListener observer) {
		final short simulationVariant = _dataModel.getConnection().getClientDavParameters().getSimulationVariant();
		final MutableCollectionChangeListener listener = new MutableCollectionChangeListener() {
			public void collectionChanged(
					MutableCollection mutableCollection, short simulationVariant, List<SystemObject> addedElements, List<SystemObject> removedElements) {
				final SystemObject[] addedElementsArray = addedElements.toArray(new SystemObject[addedElements.size()]);
				final SystemObject[] removedElementsArray = removedElements.toArray(new SystemObject[removedElements.size()]);
				observer.update(DafMutableSet.this, addedElementsArray, removedElementsArray);
			}
		};
		synchronized(_observer2listener) {
			_observer2listener.put(observer, listener);
		}
		addChangeListener(simulationVariant, listener);
//		// Observer in Liste eintragen
//		synchronized(_observer) {
//			_observer.add(observer);
//			// Wird ein Beobachter eingetragen, überprüfen, ob diese dynamische Menge schon beim Gegenstück in der Konfiguration angemeldet ist.
//			if(_observer.size() == 1) {
//				if(_requester == null) _requester = _configuration.getRequester();
//				try {
//					// es wird der aktuelle Zeitpunkt angegeben, da ab diesem Zeitpunkt die Änderungen übermittelt werden sollen
//					_requester.subscribe(this, System.currentTimeMillis());
//				}
//				catch(RequestException e) {
//					final String message = "Kommunikationsproblem bei Anmeldung auf Änderungen der dynamischen Menge " + getName();
//					_debug.error(message, e);
//					_configuration.getConnection().disconnect(true, message + " " + e.getMessage());
//				}
//			}
//		}
	}

	/**
	 * Meldet einen Beobachter wieder ab.
	 *
	 * @param observer Der Beobachter
	 */
	public void removeChangeListener(MutableSetChangeListener observer) {
		final MutableCollectionChangeListener listener;
		synchronized(_observer2listener) {
			listener = _observer2listener.remove(observer);
		}
		if(listener == null) return;
		final short simulationVariant = _dataModel.getConnection().getClientDavParameters().getSimulationVariant();
		removeChangeListener(simulationVariant, listener);
//		// Observer aus Liste löschen
//		synchronized(_observer) {
//			boolean isListenerRemoved = _observer.remove(observer);
//			// Wird der letzte Beobachter abgemeldet, muss dies beim Gegenstück dieser Menge in der Konfiguration ebenfalls abgemeldet werden.
//			if(_observer.isEmpty() && isListenerRemoved) {
//				try {
//					_requester.unsubscribe(this);
//				}
//				catch(RequestException e) {
//					final String message = "Kommunikationsproblem bei Abmeldung auf Änderungen der dynamischen Menge " + getName();
//					_debug.error(message, e);
//					_configuration.getConnection().disconnect(true, message + " " + e.getMessage());
//				}
//			}
//		}
	}

	/**
	 * Erweitert die Menge um ein weiteres Element. Wenn das angegebene Element schon in der Menge enthalten ist, dann wird die Menge nicht verändert. Wenn der Typ
	 * des angegebenen System-Objekts in der Menge nicht erlaubt ist, wird die Menge nicht verändert und eine Ausnahme generiert. Wenn bei online änderbaren Mengen
	 * die maximale Anzahl von Objekten bereits erreicht ist, wird die Menge nicht verändert und eine Ausnahme generiert.
	 *
	 * @param object Das System-Objekt, das der Menge hinzugefügt werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn eines der übergebenen Objekte nicht in die Menge aufgenommen werden konnte und noch nicht in der Menge enthalten
	 *                                      war.
	 */
	public void add(SystemObject object) throws ConfigurationChangeException {
		add(new SystemObject[]{object});
	}

	/**
	 * Entfernt ein Element der Menge. Wenn das Element nicht in der Menge enthalten ist, wird es ignoriert. Ausnahmen werden generiert, u.a. wenn bei online
	 * änderbaren Mengen die minimale Anzahl von Objekten bereits erreicht ist. Bei Ausnahmen wird die Menge nicht verändert.
	 *
	 * @param object Das System-Objekt, das aus der Menge entfernt werden soll.
	 *
	 * @throws ConfigurationChangeException Wenn die minimale Anzahl von Objekten unterschritten werden würde.
	 */
	public void remove(SystemObject object) throws ConfigurationChangeException {
		remove(new SystemObject[]{object});
	}

	/**
	 * Diese Methode informiert alle Beobachter über Veränderungen an der dynamischen Menge.
	 *
	 * @param addedObjects   Hinzugefügte Elemente
	 * @param removedObjects Entfernte Elemente
	 */
	public void update(SystemObject[] addedObjects, SystemObject[] removedObjects) {
		_debug.warning("Unerwarteter update-Aufruf für Änderungen einer dynamischen Menge");
//		// auf einer Kopie arbeiten, sonst gibt es synch-Probleme
//		final List<MutableSetChangeListener> observersCopy;
//		synchronized(_observer) {
//			observersCopy = new ArrayList<MutableSetChangeListener>(_observer);
//		}
//		for(MutableSetChangeListener mutableSetChangeListener : observersCopy) {
//			mutableSetChangeListener.update(this, addedObjects, removedObjects);
//		}
	}

	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
	    _configComSupport.addConfigurationCommunicationChangeListener(listener);
    }

	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener) {
	   _configComSupport.removeConfigurationCommunicationChangeListener(listener);
    }
	
	public void configurationCommunicationChange(boolean configComStatus){
		_configComSupport.configurationCommunicationChange(this, configComStatus);
	}
	
	public boolean isConfigurationCommunicationActive() {
		return _configComSupport.isConfigurationCommunicationActive();
    }


	public void addChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.addChangeListener(simulationVariant, changeListener);
	}

	public void removeChangeListener(short simulationVariant, MutableCollectionChangeListener changeListener) {
		_mutableCollectionSupport.removeChangeListener(simulationVariant, changeListener);
	}

	public List<SystemObject> getElements(short simulationVariant) {
		return _mutableCollectionSupport.getElements(simulationVariant);
	}

	/**
	 * Leitet die Aktualisierungsnachrichten bzgl. Änderungen von dynamischen Mengen und dynamischen Typen an das entsprechende Verwaltungsobjekt weiter.
	 * @param simVariant Simulationsvariante der Änderung
	 * @param addedElements Hinzugefügte Elemente der dynamischen Zusammenstellung
	 * @param removedElements Entfernte Elemente der dynamischen Zusammenstellung
	 */
	public void collectionChanged(final short simVariant, final List<SystemObject> addedElements, final List<SystemObject> removedElements) {
		_mutableCollectionSupport.collectionChanged(simVariant, addedElements, removedElements);
	}
}
