/*
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

import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;

/**
 * Diese Klasse stellt verschiedene Methoden zur Verf�gung, mit denen Zustandns�berg�nge durchgef�hrt werden k�nnen.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
class Util {

	private final SourceSimulationStatus _senderSimulationStatus;

	private final ConfigDataModel _configDataModel;

	private final short _simulationVariant;

	private final ConfigurationQueryManager _queryManager;
	private final ConfigSimulationObject _simulationObject;

	/**
	 * Objekt, mit dem alle Aktionen ausgef�hrt werden, die f�r jeweilige Zustands�bergange n�tig sind.
	 *
	 * @param senderSimulationStatus Verschickt den Status der Konfiguration.
	 * @param configDataModel        Zugriff auf dynamische Objekte
	 * @param simulationVariant      Simulationsvariante, f�r die bestimmte Atkionen (l�schen, Abmeldung f�r Schreibanfragen an die Konfiguration, usw.) ausgef�hrt
	 *                               werden.
	 * @param queryManager           Erm�glicht es, die Konfiguration f�r eine Simulationsvariante an/abzumelden
	 */
	public Util(
			final SourceSimulationStatus senderSimulationStatus,
			final ConfigDataModel configDataModel,
			final short simulationVariant,
			final ConfigurationQueryManager queryManager,
	        ConfigSimulationObject simulationObject
	) {
		_senderSimulationStatus = senderSimulationStatus;
		_configDataModel = configDataModel;
		_simulationVariant = simulationVariant;
		_queryManager = queryManager;
		_simulationObject = simulationObject;
	}

	/**
	 *
	 */
	public void doPrestart() throws SendSubscriptionNotConfirmed {
		// Alle dynamischen Objekte einer Simulation l�schen
		deleteDynamicObjectsAndCleanUpSets();

		// Meldet die Konfiguration f�r Konfigurationsanfragen f�r diese Simulationsvariante an
		_queryManager.subscribeReadRequestForSimulation(_simulationVariant, _simulationObject);
		_queryManager.subscribeWriteRequestForSimulation(_simulationVariant, _simulationObject);

		// Alle Mengen und Objekttypen anfordern, die speziell behandelt werden m�ssen
		_simulationObject.getSpecialTypes();

		// Datensatz schreiben, dass die Konfiguration bereit f�r Simulationen ist
		_senderSimulationStatus.sendReady();
	}

	/**
	 * F�hrt alle Aktionen aus, die unter TPuK1-125 gefordert sind. Es wird ein Datensatz geschrieben, der anzeigt, dass die Konfiguration nicht mehr zur
	 * Durchf�hrung derSimulations zur Verf�gung steht. Des Weiteren werden Schreibanfragen an die Konfiguration unterbunden.
	 *
	 * @throws SendSubscriptionNotConfirmed Auch wenn diese Exception geworfen wird, findet die Abmeldung als Senke f�r "Konfigurationsanfragen schreibend" statt.
	 *                                      Auch wenn wenn die Zustans�nderung nicht mehr propagiert werden kann, k�nnen trotzdem keine dynamischen Objekte mehr
	 *                                      angelegt werden.
	 */
	public void doStop() throws SendSubscriptionNotConfirmed {

		// Datensatz verschicken "Konfiguration steht nicht mehr f�r die Simulation zur Verf�gung".
		try {
			_senderSimulationStatus.sendNotReady();
		}
		finally {
			// Sicherstellen, dass die Konfiguration f�r lesende Konfigurationsanfragen angemeldet ist, nicht aber f�r schreibende
			_queryManager.subscribeReadRequestForSimulation(_simulationVariant, _simulationObject);
			_queryManager.unsubscribeWriteRequestForSimulation(_simulationVariant);
		}
	}

	/**
	 * F�hrt alle Aktionen aus, die unter TPuK1-126 gefordert sind. Es m�ssen alle dynamischen Objekte einer Simulationvariante gel�scht werden. Es wird ein
	 * Datensatz geschrieben, dass das Simulationsobjekt gel�scht werden kann.
	 */
	public void doDelete() throws SendSubscriptionNotConfirmed {
		// Alle dynamischen Objekte l�schen
		deleteDynamicObjectsAndCleanUpSets();

		_queryManager.unsubscribeWriteRequestForSimulation(_simulationVariant);
		_queryManager.unsubscribeReadRequestForSimulation(_simulationVariant);
		
		// Datensatz verschicken (Simulationsobjekt kann gel�scht werden)
		_senderSimulationStatus.sendDelete();
	}

	/** L�scht alle dynamischen Objekte einer Simulationsvariante und l�scht alle Elemente aus den Mengen, die speziell behandelt werden sollten. */
	private void deleteDynamicObjectsAndCleanUpSets() {
		// L�scht alle dynamischen Objekte
		_configDataModel.deleteObjects(_simulationVariant);
		// R�umt bei allen Mengen auf, die gesondert behandlet werden sollten.
		_simulationObject.cleanUpSets();
	}

	/**
	 * Verschickt einen Datensatz, der besagt das die Konfiguration nicht mehr bereit ist eine bestimmte Simulation zu unterst�tzen
	 * @throws SendSubscriptionNotConfirmed
	 */
	public void sendNotReady() throws SendSubscriptionNotConfirmed {
		_senderSimulationStatus.sendNotReady();
	}
}
