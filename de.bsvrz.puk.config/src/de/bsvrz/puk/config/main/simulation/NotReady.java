/*
 * Copyright 2009 by Kappich Systemberatung Aachen 
 * Copyright 2006 by Kappich Systemberatung Aachen 
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

package de.bsvrz.puk.config.main.simulation;

import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Dieses Objekt stellt den Zustand der Konfiguration für Simulationen dar. Es wird der Zustand "nicht Bereit" dargestellt und alle möglichen Zustnadsübergänge
 * aus diesem Zustand implementiert. Wird ein Zustand gewechselt, wird das verwaltende Objekt auf den neuen Zustand gewechselt.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class NotReady implements SimulationStates {

	private final ConfigSimulationObject _simulationObject;

	/** DebugLogger für Debug-Ausgaben */
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
		// Aus diesem Zustand ist diese Aktion nicht möglich
	}

	public void stop() {
		// Aus diesem Zustand ist diese Aktion nicht möglich
	}

	public void pause() {
		// Aus diesem Zustand ist diese Aktion nicht möglich
	}

	public void delete() {
		try {
			_util.doDelete();
			_simulationObject.setState(_simulationObject.getDeletedState());
		}
		catch(SendSubscriptionNotConfirmed sendSubscriptionNotConfirmed) {
			// Der Datensatz, der den Zustand der Konfiguration darstellt, kann nicht verschickt werden. Die Daten wurden allerdings gelöscht
			sendSubscriptionNotConfirmed.printStackTrace();
			_debug.error("Beim Wechsel einer  Simulation in den Zustand Löschen ist ein unerwarteter Fehler aufgetreten", sendSubscriptionNotConfirmed);
		}
	}

	public String toString(){
		return "Zustand: Nicht bereit";
	}

	public void noSource() {
		// Aus diesem Zustand ist diese Aktion nicht möglich
	}

	public void removedFromSet() {
		delete();
	}
}
