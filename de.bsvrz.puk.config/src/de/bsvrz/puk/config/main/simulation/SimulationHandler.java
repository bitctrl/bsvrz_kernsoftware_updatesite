/*
 * Copyright 2014 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Interface f�r Abfragen nach Simulationen
 *
 * @author Kappich Systemberatung
 * @version $Revision: 12887 $
 */
public interface SimulationHandler {

	/**
	 * Gibt das Simulationsobjekt zur angegebenen Simulationsvariante zur�ck
	 * @param simulationVariant Siulationsvariante
	 * @return Simulationsobjekt oder null falls nicht in Simulation
	 */
	ConfigSimulationObject getSimulationByVariant(short simulationVariant);

	/**
	 * Gibt das Simulationsobjekt zum angegebenen Applikationsobjekt zur�ck
	 * @param systemObject Applikationsobjekt
	 * @return Simulationsobjekt oder null falls nicht in Simulation
	 */
	ConfigSimulationObject getSimulationByApplication(SystemObject systemObject);
}
