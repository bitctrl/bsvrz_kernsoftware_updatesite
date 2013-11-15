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
 * Dieses Objekt stellt den Zustand der Konfiguration f�r Simulationen dar. Es wird der Zustand "nicht Bereit" dargestellt und alle m�glichen Zustnads�berg�nge
 * aus diesem Zustand implementiert. Wird ein Zustand gewechselt, wird das verwaltende Objekt auf den neuen Zustand gewechselt.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class NotReady implements SimulationStates {

	private final ConfigSimulationObject _simulationObject;

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	private final Util _util;

	public NotReady(final ConfigSimulationObject simulationObject, final Util util) {
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
			_debug.error("Beim Wechsel einer Simulation in den Zustand Vorstart ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public void start() {
		// Aus diesem Zustand ist diese Aktion nicht m�glich
	}

	public void stop() {
		// Aus diesem Zustand ist diese Aktion nicht m�glich
	}

	public void pause() {
		// Aus diesem Zustand ist diese Aktion nicht m�glich
	}

	public void delete() {
		try {
			_util.doDelete();
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			// Der Datensatz, der den Zustand der Konfiguration darstellt, kann nicht verschickt werden. Die Daten wurden allerdings gel�scht
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer  Simulation in den Zustand L�schen ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public String toString(){
		return "Zustand: Nicht bereit";
	}

	public void noSource() {
		// Aus diesem Zustand ist diese Aktion nicht m�glich
	}

	public void removedFromSet() {
		delete();
	}
}
