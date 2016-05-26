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

import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
* ObjectLookup-Klasse als Wrapper des ConfigDataModels, das Pids abhängig von einer Simulationsvariante auflöst.
*
* @author Kappich Systemberatung
* @version $Revision$
*/
public class SimulationLookup implements ObjectLookup {
	/** Datenmodell */
	private final ConfigDataModel _dataModel;

	/** Zu berücksichtigende Simulationsvariante */
	private final short _simulationVariant;

	/**
	 * Erstellt einen neuen SimulationLookup
	 * @param dataModel dataModel
	 * @param simulationVariant Simulatiosnvariante
	 */
	public SimulationLookup(final ConfigDataModel dataModel, final short simulationVariant) {
		_dataModel = dataModel;
		_simulationVariant = simulationVariant;
	}

	@Override
	public SystemObject getObject(final String pid) {
		return _dataModel.getObject(pid, _simulationVariant);
	}

	@Override
	public SystemObject getObject(final long id) {
		return _dataModel.getObject(id);
	}
}
