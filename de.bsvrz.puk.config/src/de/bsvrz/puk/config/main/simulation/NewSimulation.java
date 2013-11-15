/*
 * Copyright 2009 by Kappich Systemberatung Aachen 
 * Copyright 2006 by Kappich Systemberatung Aachen 
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

package de.bsvrz.puk.config.main.simulation;

import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Diese Klasse stellt einen Zustand dar. Der Zustand heisst "Simulation entdeckt".
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class NewSimulation implements SimulationStates {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	private final ConfigSimulationObject _simulationObject;

	private final Util _util;

	public NewSimulation(final ConfigSimulationObject simulationObject, final Util util) {
		_simulationObject = simulationObject;
		_util = util;
	}

	public void preStart() {
		try {
			_util.doPrestart();
			_simulationObject.setState(_simulationObject.getReadyState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer neuen Simulation in den Zustand Vorstart ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public void start() {
		// unerlaubter �bergang wird behandelt wie der �bergang nach STOP behandelt
		stop();
	}

	public void stop() {
		try {
			_util.doStop();
			_simulationObject.setState(_simulationObject.getNotReadyState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer neuen Simulation in den Zustand Stop ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public void pause() {
		// unerlaubter �bergang wird behandelt wie der �bergang nach STOP behandelt
		stop();
	}

	public void delete() {
		try {
			_util.doDelete();
			// Die Konfiguration f�r die Simulation in den Zustand "gel�scht" �berf�hren
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer neuen Simulation in den Zustand L�schen ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public String toString() {
		return "Zustand: NeueSimulation";
	}

	public void noSource() {
		_simulationObject.setState(_simulationObject.getNotReadyState());
	}

	public void removedFromSet() {
		try {
			_util.sendNotReady();
			// Alle Daten der Simulation l�schen
			_util.doDelete();
			// Die Konfiguration f�r die Simulation in den Zustand "gel�scht" �berf�hren
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Entfernen einer neuen Simulation ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}
}
