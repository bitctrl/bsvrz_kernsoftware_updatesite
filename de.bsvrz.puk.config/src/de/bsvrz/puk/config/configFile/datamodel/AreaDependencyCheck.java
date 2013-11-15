/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;

import java.util.*;

/**
 * Die Aufgabe der Implementation dieses Interfaces ist es, fehlende Konfigurationsbereiche, die ben�tigt werden um Abh�ngigkeiten zwischen
 * Konfigurationsberichen aufzul�sen, zu entdecken.
 * <p/>
 * Jeder Konfigurationsbereich speichert die Konfigurationsbereiche von denen er abh�ngig ist. Soll der Konfigurationsbereich nun benutzt werden (Import,
 * Export, Aktivierung, Freigabe zur �bernahme, usw.), so m�ssen diese Bereiche vorhanden sein.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public interface AreaDependencyCheck {

	/**
	 * Die Methode pr�ft ob alle Abh�ngigkeiten der einzelnen Bereiche mit den �bergebenen Bereichen aufgel�st werden k�nnen.
	 * <p/>
	 *
	 * @param areas Bereiche mit entsprechenden Vesionen(>0). Alle Abh�ngigkeiten der Bereiche m�ssen mit den �bergebenen Bereichen aufgel�st werden k�nnen.
	 *
	 * @return Objekt, dass das Ergebnis der Pr�fung enth�lt.
	 */
	public AreaDependencyCheckResult checkAreas(List<ConfigAreaAndVersion> areas);

	public interface AreaDependencyCheckResult {

		/**
		 * Gibt alle Bereiche und deren Abh�ngigkeiten zur�ck, die nicht aufgel�st werden konnten und deren Abh�ngigkeit als "optional" gekennzeichnet sind.
		 *
		 * @return Als Schl�ssel dient der Bereich, dessen Abh�ngigkeit nicht aufgel�st werden konnte. Es wird eine Liste zur�ckgegeben, mit allen Abh�ngigkeiten, die
		 *         nicht aufgel�st werden konnten und die optional sind. Wurden keine Fehler gefunden, ist die Map leer.
		 */
		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getOptionalDependencyErrors();

		/**
		 * Gibt alle Bereiche und deren Avh�ngigkeiten zur�ck, die nicht aufgel�st werden konnten und deren Abh�ngigkeit als "notwenig" gekennzeichnet sind.
		 *
		 * @return Als Schl�ssel dient der Bereich, dessen Abh�ngigkeit nicht aufgel�st werden konnte. Es wird eine Liste zur�ckgegeben, die alle Abh�ngigkeiten des
		 *         Schl�ssel-Bereichs enth�lt. Wurden keine Fehler gefunden, ist die Map leer.
		 */
		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getNeededDependencyErrors();

		/**
		 * Gibt alle Bereiche zur�ck, deren Abh�gigkeiten nicht gepr�ft werden konnte. Dies ist zum Beispiel m�glich, wenn ein Bereich vor der Einf�hrung dieses
		 * Mechanismus aktiviert wurde. Sobald dieser Bereich allerdings aktiviert/zur Aktivierung freigegeben/ zur �bernahme freigegeben wird, stehen diese
		 * Informationen zur Verf�gung.
		 *
		 * @return Alle Bereiche, deren Abh�ngigkeiten noch nicht erfasst wurden.
		 */
		public List<ConfigurationArea> getAreasWithUnknownDependencies();
	}

	/** Gibt alle Aktionen an, die die Konfiguration beim Start durchf�hren kann. Anhand dieser Aktion werden unterschiedliche Tests durchgef�hrt. */
	public enum KindOfConfigurationAction {
		START_CONFIGURATION,
		IMPORT,
		EXPORT,
		ACTIVATION,
		RELEASE_AREAS_FOR_TRANSFER,
		RELEASE_AREAS_FOR_ACTIVATION,;
	}
}
