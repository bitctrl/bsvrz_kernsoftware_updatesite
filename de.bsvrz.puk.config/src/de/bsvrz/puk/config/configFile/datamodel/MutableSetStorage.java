/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interface um die Speicherung einer dynamischem Menge ({@linkplain de.bsvrz.puk.config.configFile.datamodel.ConfigMutableSet}) zu
 * realisieren. Diese werden entweder als Konfigurationsdatensatz oder als eigene Datei gespeichert, je nach Implementierung
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class MutableSetStorage {

	private SoftReference<List<MutableElement>> _elements = null;

	/**
	 * Entfernt überflüssige leere/ungültige Einträge am Ende der Liste der referenzierten Objekte
	 *
	 * @param mutableElements Liste mit allen elemente, wird modifiziert
	 */
	private static void cleanTailElements(final List<MutableElement> mutableElements) {
		while(!mutableElements.isEmpty()) {
			MutableElement lastElement = mutableElements.get(mutableElements.size() - 1);
			if(lastElement.getObject() != null) return;
			mutableElements.remove(mutableElements.size() - 1);
		}
	}

	/**
	 * Fügt Objekte zu der Menge hinzu
	 *
	 * @param addedElements     Objekte
	 * @param simulationVariant Simulationsvariante
	 * @throws ConfigurationChangeException
	 */
	public final synchronized void add(Collection<SystemObject> addedElements, short simulationVariant) throws ConfigurationChangeException {
		List<MutableElement> mutableElements = getMutableElements();
		long time = System.currentTimeMillis();

		int startIndex = 0;

		for(SystemObject systemObject : addedElements) {
			if(systemObject != null) {
				boolean added = false;

				// Erste Lücke füllen, sofern vorhanden
				for(; startIndex < mutableElements.size(); startIndex++) {
					// StartIndex wird weiterverwendet, damit nicht für jedes eingefügte Element von
					// vorne gesucht wird
					final MutableElement mutableElement = mutableElements.get(startIndex);
					if(mutableElement.getObject() == null) {
						mutableElement.setObject(systemObject);
						mutableElement.setStartTime(time);
						mutableElement.setEndTime(0);
						mutableElement.setSimulationVariant(simulationVariant);
						added = true;
						break;
					}
				}
				if(!added) {
					// Falls keine Lücke da war, Element am Ende anfügen
					mutableElements.add(new MutableElement(systemObject, time, 0, simulationVariant));
				}
			}
		}
		writeElements(mutableElements);
	}

	/**
	 * Macht Objektreferenzen in der Menge ungültig
	 *
	 * @param removedElements   Objekte, die nicht mehr in der Menge enthalten sein sollen
	 * @param simulationVariant Simulationsvariante
	 * @throws ConfigurationChangeException
	 */
	public final synchronized void invalidate(Collection<SystemObject> removedElements, short simulationVariant) throws ConfigurationChangeException {
		List<MutableElement> mutableElements = getMutableElements();
		long time = System.currentTimeMillis();
		for(MutableElement mutableElement : mutableElements) {
			// da Elemente mehrfach in die Menge eingefügt und entfernt werden können, muss auf EndTime == 0 abgefragt werden
			if(removedElements.contains(mutableElement.getObject())
					&& mutableElement.getSimulationVariant() == simulationVariant
					&& mutableElement.getEndTime() == 0) {
				mutableElement.setEndTime(time);
			}
		}
		writeElements(mutableElements);
	}

	/**
	 * Löscht Objekte eienr angegebenen Simulation permanent und vollständig aus dieser Menge (z. B. beim Beenden einer Simulation)
	 *
	 * @param simulationVariant Simulationsvariante
	 * @return Liste mit gelöschten Objekten
	 * @throws ConfigurationChangeException
	 */
	public final synchronized List<SystemObject> deleteElements(short simulationVariant) throws ConfigurationChangeException {
		final List<MutableElement> mutableElements = getMutableElements();
		final List<SystemObject> removed = new ArrayList<SystemObject>();
		long time = System.currentTimeMillis();

		// Elemente mit angegebener Simulationsvariante rausfiltern
		for(MutableElement mutableElement : mutableElements) {
			if(mutableElement.getObject() != null && mutableElement.getSimulationVariant() == simulationVariant) {
				if(mutableElement.getEndTime() == 0 || mutableElement.getEndTime() > time) {
					removed.add(mutableElement.getObject());
				}
				mutableElement.setStartTime(0);
				mutableElement.setEndTime(0);
				mutableElement.setObject(null);
				mutableElement.setSimulationVariant((short) 0);
			}
		}
		cleanTailElements(mutableElements);
		writeElements(mutableElements);
		return removed;
	}

	/**
	 * Entfernt alle historischen (ungültigen) Elemente, die vor dem angegebenen Zeitstempel auf ungültig gesetzt wurden
	 * @param deletionTime Zeitstempel analog zu System.currentTimeMillis()
	 */
	public final synchronized List<SystemObject> deleteElementsOlderThan(long deletionTime) throws ConfigurationChangeException {
		final List<MutableElement> mutableElements = getMutableElements();
		final List<SystemObject> removed = new ArrayList<SystemObject>();

		// Elemente mit angegebener Simulationsvariante rausfiltern
		for(MutableElement mutableElement : mutableElements) {
			if(mutableElement.getObject() != null
					&& mutableElement.getSimulationVariant() == (short)0
					&& mutableElement.getEndTime() != 0L
					&& mutableElement.getEndTime() < deletionTime) {
				removed.add(mutableElement.getObject());
				mutableElement.setStartTime(0);
				mutableElement.setEndTime(0);
				mutableElement.setObject(null);
				mutableElement.setSimulationVariant((short) 0);
			}
		}
		cleanTailElements(mutableElements);
		writeElements(mutableElements);
		return removed;
	}

	/**
	 * Schreibt eine Menge von Mengenelementen in den dahinterliegenden Speicher (etweder Konfigurationsdaten oder externe Datei).
	 * Die Reihenfolge bzw. der Index der Elemente entspricht dem neuesten Aufruf von readElements(). Es können aber
	 * Objekte am Ende gelöscht und oder eingefügt werden.
	 * @param mutableElements Liste mit zu schreibenen Elementen
	 * @throws ConfigurationChangeException
	 */
	protected abstract void writeElements(final List<MutableElement> mutableElements) throws ConfigurationChangeException;

	/**
	 * Gibt alle bisher gespeicherten Elemente (auch ungültige/gelöschte) zurück. Die Elemente sollen in der Reihenfolge zurückgegeben
	 * werden, wie sie in der Datei stehen.
	 * <p>
	 * Die zurückgebenene Liste wird vom Aufrufer ggf. modifiziert, darf also nicht von der implementierenden Klasse gecacht werden.
	 *
	 * @return Liste mit allen Elementen des Sets (enthält Objektreferenz, Gültigkeit, Simulationsvariante)
	 */
	protected abstract List<MutableElement> readElements();

	/**
	 * Gibt alle aktuell enthaltenen Elemente zurück.
	 *
	 * @return eine Liste von Elementen mit Zeitstempeln, die die Zugehörigkeitszeiträume repräsentieren
	 */
	public final List<MutableElement> getMutableElements() {
		if(_elements != null) {
			List<MutableElement> elements = _elements.get();
			if(elements != null) {
				return elements;
			}
		}
		List<MutableElement> elements = readElements();
		_elements = new SoftReference<List<MutableElement>>(elements);
		return elements;
	}
	
	public void purgeCache(){
		_elements = null;
	}

	/**
	 * Repräsentiert ein Element der Menge mit dem Zeitstempel, ab dem das Element zur Menge gehört und dem Zeitstempel, ab dem das Element
	 * nicht mehr zur Menge gehört.
	 */
	static class MutableElement implements MutableElementInterface {

		public static final int BYTE_SIZE = 26;

		/**
		 * ID des Systemobjektes
		 */
		private SystemObject _object;

		/**
		 * Zeitstempel, seit dem das Element zur Menge gehört
		 */
		private long _startTime;

		/**
		 * Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 */
		private long _endTime;

		/**
		 * Simulationsvariante, in welcher das Objekt zur Menge hinzugefügt wurde
		 */
		private short _simulationVariant;

		/**
		 * Erzeugt ein Objekt für die dynamische Menge.
		 *
		 * @param object            das System-Objekt
		 * @param startTime         Zeitstempel, seit dem das Element zur Menge gehört
		 * @param endTime           Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 * @param simulationVariant Ob das Objekt gespeichert wurde (false = Objekt muss beim Speichern neu geschrieben werden)
		 */
		public MutableElement(SystemObject object, long startTime, long endTime, short simulationVariant) {
			_object = object;
			_startTime = startTime;
			_endTime = endTime;
			_simulationVariant = simulationVariant;
		}

		/**
		 * Gibt das System-Objekt zurück.
		 *
		 * @return das System-Objekt
		 */
		@Override
		public SystemObject getObject() {
			return _object;
		}

		public void setObject(final SystemObject object) {
			_object = object;
		}

		/**
		 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element zur Menge gehört.
		 *
		 * @return Zeitstempel, seit dem das Element zur Menge gehört
		 */
		@Override
		public long getStartTime() {
			return _startTime;
		}

		public void setStartTime(final long startTime) {
			_startTime = startTime;
		}

		/**
		 * Gibt den Zeitstempel zurück, der angibt, seit wann das Element nicht mehr zur Menge gehört.
		 *
		 * @return Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 */
		@Override
		public long getEndTime() {
			return _endTime;
		}

		/**
		 * Setzt den Zeitstempel, der angibt, seit wann das Element nicht mehr zur Menge gehört.
		 *
		 * @param endTime Zeitstempel, seit dem das Element nicht mehr zur Menge gehört
		 */
		public void setEndTime(long endTime) {
			_endTime = endTime;
		}

		/**
		 * Gibt die Simulationsvariante dieses Elements zurück, in der das Objekt dieser dynamischen Menge hinzugefügt wurde.
		 *
		 * @return die Simulationsvariante, in welcher das Objekt der Menge hinzugefügt wurde.
		 */
		@Override
		public short getSimulationVariant() {
			return _simulationVariant;
		}

		public void setSimulationVariant(final short simulationVariant) {
			_simulationVariant = simulationVariant;
		}

		public long getId() {
			if(_object != null) return _object.getId();
			return 0;
		}
	}
}
