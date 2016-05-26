/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.datk;


/**
 * Enthält zur Simulation gehörende Pids.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class PidSimulation {

	/** Menge der Simulationsobjekte. */
	public static final String objectSet = "Simulationen";

	public static final String typeOnlineSimulation = "typ.onlineSimulation";

	public static final String typeOfflineSimulation = "typ.offlineSimulation";

	public static final String atgOnlineControl = "atg.simulationsSteuerungOnline";

	public static final String atgOfflineControl = "atg.simulationsSteuerungOffline";

	public static final String atgArchiveData = "atg.simulationsDatenArchivierung";

	public static final String atgArchiveState = "atg.simulationsStatusArchiv";

	public static final String atgProperties = "atg.simulationsEigenschaften";

	public static final String aspState = "asp.zustand";

	public static final String aspTarget = "asp.parameterSoll";

	public static final String attArchiveReady = "ArchivBereit";

	public static final String attSimulationState = "SimulationsZustand";

	public static final String attSimVar = "SimulationsVariante";

	public static final String attSimulationRange = "SimulationsStreckenReferenz";
}
