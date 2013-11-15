/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.datamodel;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;

import java.util.*;

/**
 * Interface zum Zugriff auf interne Informationen bzgl. der Abh�ngigkeitspr�fung, das f�r automatisierte Tests ben�tigt wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5831 $
 */
public interface ConfigConfigurationAreaInterface extends ConfigurationArea {
	/**
	 * Gibt alle Abh�ngigkeiten des Bereichs zu anderen Konfigurationsbereichen zur�ck.
	 *
	 * @return Abh�ngigkeiten des Bereich oder eine leere Collection, falls keine Abh�ngigkeiten bestehen.
	 */
	public Collection<ConfigurationAreaDependency> getDependencyFromOtherConfigurationAreas();

	/**
	 * @return <code>true</code>, wenn der Bereich bereits auf Abh�ngigkeiten gepr�ft wurde; <code>false</code>, wenn der Bereich nicht auf Abh�ngigkeiten gepr�ft
	 *         wurde.
	 */
	boolean dependenciesChecked();
}
