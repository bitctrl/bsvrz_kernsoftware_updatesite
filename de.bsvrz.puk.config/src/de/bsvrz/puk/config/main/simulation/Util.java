/*
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

import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.puk.config.main.communication.query.ConfigurationQueryManager;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;

/**
 * Diese Klasse stellt verschiedene Methoden zur Verfügung, mit denen Zustandnsübergänge durchgeführt werden können.
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
	 * Objekt, mit dem alle Aktionen ausgeführt werden, die für jeweilige Zustandsübergange nötig sind.
	 *
	 * @param senderSimulationStatus Verschickt den Status der Konfiguration.
	 * @param configDataModel        Zugriff auf dynamische Objekte
	 * @param simulationVariant      Simulationsvariante, für die bestimmte Atkionen (löschen, Abmeldung für Schreibanfragen an die Konfiguration, usw.) ausgeführt
	 *                               werden.
	 * @param queryManager           Ermöglicht es, die Konfiguration für eine Simulationsvariante an/abzumelden
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
		// Alle dynamischen Objekte einer Simulation löschen
		deleteDynamicObjectsAndCleanUpSets();

		// Meldet die Konfiguration für Konfigurationsanfragen für diese Simulationsvariante an
		_queryManager.subscribeReadRequestForSimulation(_simulationVariant, _simulationObject);
		_queryManager.subscribeWriteRequestForSimulation(_simulationVariant, _simulationObject);

		// Alle Mengen und Objekttypen anfordern, die speziell behandelt werden müssen
		_simulationObject.getSpecialTypes();

		// Datensatz schreiben, dass die Konfiguration bereit für Simulationen ist
		_senderSimulationStatus.sendReady();
	}

	/**
	 * Führt alle Aktionen aus, die unter TPuK1-125 gefordert sind. Es wird ein Datensatz geschrieben, der anzeigt, dass die Konfiguration nicht mehr zur
	 * Durchführung derSimulations zur Verfügung steht. Des Weiteren werden Schreibanfragen an die Konfiguration unterbunden.
	 *
	 * @throws SendSubscriptionNotConfirmed Auch wenn diese Exception geworfen wird, findet die Abmeldung als Senke für "Konfigurationsanfragen schreibend" statt.
	 *                                      Auch wenn wenn die Zustansänderung nicht mehr propagiert werden kann, können trotzdem keine dynamischen Objekte mehr
	 *                                      angelegt werden.
	 */
	public void doStop() throws SendSubscriptionNotConfirmed {

		// Datensatz verschicken "Konfiguration steht nicht mehr für die Simulation zur Verfügung".
		try {
			_senderSimulationStatus.sendNotReady();
		}
		finally {
			// Sicherstellen, dass die Konfiguration für lesende Konfigurationsanfragen angemeldet ist, nicht aber für schreibende
			_queryManager.subscribeReadRequestForSimulation(_simulationVariant, _simulationObject);
			_queryManager.unsubscribeWriteRequestForSimulation(_simulationVariant);
		}
	}

	/**
	 * Führt alle Aktionen aus, die unter TPuK1-126 gefordert sind. Es müssen alle dynamischen Objekte einer Simulationvariante gelöscht werden. Es wird ein
	 * Datensatz geschrieben, dass das Simulationsobjekt gelöscht werden kann.
	 */
	public void doDelete() throws SendSubscriptionNotConfirmed {
		// Alle dynamischen Objekte löschen
		deleteDynamicObjectsAndCleanUpSets();

		_queryManager.unsubscribeWriteRequestForSimulation(_simulationVariant);
		_queryManager.unsubscribeReadRequestForSimulation(_simulationVariant);
		
		// Datensatz verschicken (Simulationsobjekt kann gelöscht werden)
		_senderSimulationStatus.sendDelete();
	}

	/** Löscht alle dynamischen Objekte einer Simulationsvariante und löscht alle Elemente aus den Mengen, die speziell behandelt werden sollten. */
	private void deleteDynamicObjectsAndCleanUpSets() {
		// Löscht alle dynamischen Objekte
		_configDataModel.deleteObjects(_simulationVariant);
		// Räumt bei allen Mengen auf, die gesondert behandlet werden sollten.
		_simulationObject.cleanUpSets();
	}

	/**
	 * Verschickt einen Datensatz, der besagt das die Konfiguration nicht mehr bereit ist eine bestimmte Simulation zu unterstützen
	 * @throws SendSubscriptionNotConfirmed
	 */
	public void sendNotReady() throws SendSubscriptionNotConfirmed {
		_senderSimulationStatus.sendNotReady();
	}
}
