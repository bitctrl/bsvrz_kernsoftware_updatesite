/*
 * Copyright 2007 by Kappich Systemberatung Aachen 
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
import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;

import java.util.*;

/**
 * Die Aufgabe der Implementation dieses Interfaces ist es, fehlende Konfigurationsbereiche, die benötigt werden um Abhängigkeiten zwischen
 * Konfigurationsberichen aufzulösen, zu entdecken.
 * <p>
 * Jeder Konfigurationsbereich speichert die Konfigurationsbereiche von denen er abhängig ist. Soll der Konfigurationsbereich nun benutzt werden (Import,
 * Export, Aktivierung, Freigabe zur Übernahme, usw.), so müssen diese Bereiche vorhanden sein.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface AreaDependencyCheck {

	/**
	 * Die Methode prüft ob alle Abhängigkeiten der einzelnen Bereiche mit den übergebenen Bereichen aufgelöst werden können.
	 * <p>
	 *
	 * @param areas Bereiche mit entsprechenden Vesionen(>0). Alle Abhängigkeiten der Bereiche müssen mit den übergebenen Bereichen aufgelöst werden können.
	 *
	 * @return Objekt, dass das Ergebnis der Prüfung enthält.
	 */
	public AreaDependencyCheckResult checkAreas(List<ConfigAreaAndVersion> areas);

	public interface AreaDependencyCheckResult {

		/**
		 * Gibt alle Bereiche und deren Abhängigkeiten zurück, die nicht aufgelöst werden konnten und deren Abhängigkeit als "optional" gekennzeichnet sind.
		 *
		 * @return Als Schlüssel dient der Bereich, dessen Abhängigkeit nicht aufgelöst werden konnte. Es wird eine Liste zurückgegeben, mit allen Abhängigkeiten, die
		 *         nicht aufgelöst werden konnten und die optional sind. Wurden keine Fehler gefunden, ist die Map leer.
		 */
		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getOptionalDependencyErrors();

		/**
		 * Gibt alle Bereiche und deren Avhängigkeiten zurück, die nicht aufgelöst werden konnten und deren Abhängigkeit als "notwenig" gekennzeichnet sind.
		 *
		 * @return Als Schlüssel dient der Bereich, dessen Abhängigkeit nicht aufgelöst werden konnte. Es wird eine Liste zurückgegeben, die alle Abhängigkeiten des
		 *         Schlüssel-Bereichs enthält. Wurden keine Fehler gefunden, ist die Map leer.
		 */
		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getNeededDependencyErrors();

		/**
		 * Gibt alle Bereiche zurück, deren Abhägigkeiten nicht geprüft werden konnte. Dies ist zum Beispiel möglich, wenn ein Bereich vor der Einführung dieses
		 * Mechanismus aktiviert wurde. Sobald dieser Bereich allerdings aktiviert/zur Aktivierung freigegeben/ zur Übernahme freigegeben wird, stehen diese
		 * Informationen zur Verfügung.
		 *
		 * @return Alle Bereiche, deren Abhängigkeiten noch nicht erfasst wurden.
		 */
		public List<ConfigurationArea> getAreasWithUnknownDependencies();
	}

	/** Gibt alle Aktionen an, die die Konfiguration beim Start durchführen kann. Anhand dieser Aktion werden unterschiedliche Tests durchgeführt. */
	public enum KindOfConfigurationAction {
		START_CONFIGURATION,
		IMPORT,
		EXPORT,
		ACTIVATION,
		RELEASE_AREAS_FOR_TRANSFER,
		RELEASE_AREAS_FOR_ACTIVATION,;
	}
}
