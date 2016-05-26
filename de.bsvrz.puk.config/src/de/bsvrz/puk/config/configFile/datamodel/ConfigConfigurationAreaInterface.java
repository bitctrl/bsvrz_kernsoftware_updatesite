/*
 * Copyright 2008 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.main.config.ConfigurationArea;

import java.util.*;

/**
 * Interface zum Zugriff auf interne Informationen bzgl. der Abhängigkeitsprüfung, das für automatisierte Tests benötigt wird.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConfigConfigurationAreaInterface extends ConfigurationArea {
	/**
	 * Gibt alle Abhängigkeiten des Bereichs zu anderen Konfigurationsbereichen zurück.
	 *
	 * @return Abhängigkeiten des Bereich oder eine leere Collection, falls keine Abhängigkeiten bestehen.
	 */
	public Collection<ConfigurationAreaDependency> getDependencyFromOtherConfigurationAreas();

	/**
	 * @return <code>true</code>, wenn der Bereich bereits auf Abhängigkeiten geprüft wurde; <code>false</code>, wenn der Bereich nicht auf Abhängigkeiten geprüft
	 *         wurde.
	 */
	boolean dependenciesChecked();
}
