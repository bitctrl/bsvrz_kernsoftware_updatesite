/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class AreaDependencyChecker implements AreaDependencyCheck {

	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	public AreaDependencyCheckResult checkAreas(
			List<ConfigAreaAndVersion> areas
	) {

		// Prüfen ob gültige Versionen übergeben wurden.
		checkRightVersions(areas);
		return check(areas);
	}

	/**
	 * Prüft, ob für jeden der übergebenen Bereich die Abhängigkeiten zu anderen Bereichen erfüllt ist und gibt das Ergebnis der Prüfung zurück.
	 *
	 * @param areas Bereiche, die in den übergenen Versionen, zum Start der Konfiguration genutzt werden sollen
	 *
	 * @return Ergebnis der Prüfung ob alle Abhängigkeiten, wie gefordert, aufgelöst werden können.
	 */
	private AreaDependencyCheckResult check(List<ConfigAreaAndVersion> areas) {
		// Speichert alle gefundenen Fehler
		final CheckResult result = new CheckResult();

		// Alle Bereiche, die zur Verfügung stehen
		final Map<String, Short> usedAreasAndVersions = getUsedAreasWithVersionMap(areas);

		for(ConfigAreaAndVersion area : areas) {

			// Bei alten Bereichen kann es sein, das die Abhängigkeiten noch gar nicht vorliegen
			final ConfigConfigurationAreaInterface configArea = (ConfigConfigurationAreaInterface)area.getConfigArea();

				if(configArea.dependenciesChecked() == false) {
					result.addUnknownArea(configArea);
					// Da es keine Abhängigkeiten zum prüfen gibt, kann der nächste Bereich betrachtet werden
					continue;
				}

			// Alle Abhängigkeiten des Bereichs anfordern und die Abhängigkeiten sortieren
			final Map<String, List<ConfigurationAreaDependency>> sortedDependencies = getAllDependencies(configArea.getDependencyFromOtherConfigurationAreas());

			// Der betrachtete Bereich soll gestartet werden.
			// Die Version in der das geschehen soll bestimmt die nötigen Abhängigkeiten.

			// Welche Abhängigkeiten sind in dieser Version aufgetreten. Diese müssen gefunden werden.
			short startVersion = usedAreasAndVersions.get(configArea.getPid());

			// Bereiche, von dem der Bereich abhängig ist
			final Set<String> areasDependentFrom = sortedDependencies.keySet();

			for(String areaDependentFrom : areasDependentFrom) {
				// In dieser Liste steht, in welcher Version der Bereich vom Bereich areaDependentFrom abhängig wurde
				final List<ConfigurationAreaDependency> dependencies = sortedDependencies.get(areaDependentFrom);

				// Das letzte Element in der Liste besitzt die Größte "OccurredAtVersion"
				for(int nr = dependencies.size() - 1; nr >= 0; nr--) {
					final ConfigurationAreaDependency areaDependency = dependencies.get(nr);
					if(areaDependency.getDependencyOccurredAtVersion() <= startVersion) {
						// Es wurde eine Abhängigkeit gefunden.
						// Der Bereich muss in der richtigen Version vorliegen, bzw. überhaupt verfügbar sein.

						// In diesem Bereich liegt der abhängige Bereich vor

						final Short versionDependentAreaIsUsed = usedAreasAndVersions.get(areaDependentFrom);
						if(versionDependentAreaIsUsed != null) {

							// Der Bereich liegt vor, aber wird er in einer Version genutzt, die größer gleich der benötigten Version ist ?
							if(versionDependentAreaIsUsed >= areaDependency.getNeededVersion()) {
								// Ja, der Bereich liegt in der Version vor, die benötigt wird um die Abhängigkeiten aufzulösen.
								// Es kann der nächste Bereich betrachtet werden.
								break;
							}
							else {
								// Der Bereich liegt in der falschen Version vor -> Fehler/Warnung gefunden

								if(areaDependency.getKind() == ConfigurationAreaDependencyKind.OPTIONAL) {
									result.addOptionalDependencyError(configArea, areaDependency);
								}
								else if(areaDependency.getKind() == ConfigurationAreaDependencyKind.REQUIRED) {
									result.addNeededDependencyError(configArea, areaDependency);
								}
								else {
									throw new IllegalArgumentException("Unbekannte Abhängigkeit: " + areaDependency.getKind());
								}
							}
						}
						else {
							// Der Bereich liegt gar nicht vor -> Fehler/Warnung gefunden
							if(areaDependency.getKind() == ConfigurationAreaDependencyKind.OPTIONAL) {
								result.addOptionalDependencyError(configArea, areaDependency);
							}
							else if(areaDependency.getKind() == ConfigurationAreaDependencyKind.REQUIRED) {
								result.addNeededDependencyError(configArea, areaDependency);
							}
							else {
								throw new IllegalArgumentException("Unbekannte Abhängigkeit: " + areaDependency.getKind());
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param areaDependencies Alle Abhängigkeiten des Bereichs.
	 *
	 * @return Als Schlüssel dient der Bereich, von dem ein anderer Bereich Abhängig ist. Als Wert wird die Abhängigkeit selbst zurückgegeben (beinhaltet den
	 *         Bereich noch einmal). Die Liste ist sortiert. Die letzte Abhängigkeit, die entdeckt wurde, ist am Ende der Liste gespeichert.
	 */
	private Map<String, List<ConfigurationAreaDependency>> getAllDependencies(Collection<ConfigurationAreaDependency> areaDependencies) {
		final Map<String, List<ConfigurationAreaDependency>> result = new HashMap<String, List<ConfigurationAreaDependency>>();

		for(ConfigurationAreaDependency areaDependency : areaDependencies) {
			List<ConfigurationAreaDependency> dependencyList = result.get(areaDependency.getDependantArea());
			if(dependencyList == null) {
				dependencyList = new ArrayList<ConfigurationAreaDependency>();
				result.put(areaDependency.getDependantArea(), dependencyList);
			}
			dependencyList.add(areaDependency);
		}

		// Alle Listen sortieren
		Collection<List<ConfigurationAreaDependency>> unsortedDependancyLists = result.values();

		for(List<ConfigurationAreaDependency> unsortedDependencies : unsortedDependancyLists) {
			// Die Liste sortieren
			Collections.sort(
					unsortedDependencies, new Comparator<ConfigurationAreaDependency>() {
				public int compare(final ConfigurationAreaDependency o1, final ConfigurationAreaDependency o2) {
					final Short shortO1 = new Short(o1.getDependencyOccurredAtVersion());
					final Short shortO2 = new Short(o2.getDependencyOccurredAtVersion());
					return shortO1.compareTo(shortO2);
				}
			}
			);
		}//for, über alle Listen, die sortiert werden müssen

		return result;
	}

	/**
	 * Erzeugt eine Map in der alle Bereiche gespeichert sind, die der Konfiguration zur Verfügung und die Version, in der der Bereich zur Verfügung steht.
	 * <p>
	 * Als Schlüssel dient die Pid des Konfigurationsbereichs, als Wert wird die Version zurückgegeben.
	 *
	 * @param areas Alle Bereiche und deren Versionen, die der Konfiguration zur Verfügung stehen.
	 *
	 * @return s.o.
	 */
	private Map<String, Short> getUsedAreasWithVersionMap(List<ConfigAreaAndVersion> areas) {
		final Map<String, Short> result = new HashMap<String, Short>();

		for(ConfigAreaAndVersion area : areas) {
			result.put(area.getConfigArea().getPid(), area.getVersion());
		}
		return result;
	}

	/**
	 * Methode, die prüft, ob alle Versionen >= 0 sind.
	 *
	 * @param areas Bereiche mit Versionsnummern, die >= 0 sein müssen
	 *
	 * @throws IllegalArgumentException Wenn eine Version < 0 ist.
	 */
	private void checkRightVersions(List<ConfigAreaAndVersion> areas) {
		for(ConfigAreaAndVersion area : areas) {
			if(area.getVersion() < 0) throw new IllegalArgumentException("Für einen Bereich wurde keine gültige Versionsnummer angegeben: " + area);
		}
	}

	private static final class CheckResult implements AreaDependencyCheckResult {

		private final Map<ConfigurationArea, List<ConfigurationAreaDependency>> _optionalDependencyErrors = new HashMap<ConfigurationArea, List<ConfigurationAreaDependency>>();

		private final Map<ConfigurationArea, List<ConfigurationAreaDependency>> _neededDependencyErrors = new HashMap<ConfigurationArea, List<ConfigurationAreaDependency>>();

		private final List<ConfigurationArea> _unknownAreas = new ArrayList<ConfigurationArea>();

		/**
		 * @param area       Bereich, für den die Abhängigkeit gilt, die nicht aufgelöst werden konnte.
		 * @param dependency Abhängigkeit
		 */
		public void addOptionalDependencyError(final ConfigurationArea area, ConfigurationAreaDependency dependency) {
			List<ConfigurationAreaDependency> list = _optionalDependencyErrors.get(area);
			if(list == null) {
				list = new ArrayList<ConfigurationAreaDependency>();
				_optionalDependencyErrors.put(area, list);
			}
			list.add(dependency);
		}

		public void addNeededDependencyError(final ConfigurationArea area, ConfigurationAreaDependency dependency) {
			List<ConfigurationAreaDependency> list = _neededDependencyErrors.get(area);

			if(list == null) {
				list = new ArrayList<ConfigurationAreaDependency>();
				_neededDependencyErrors.put(area, list);
			}

			list.add(dependency);
		}

		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getOptionalDependencyErrors() {
			return _optionalDependencyErrors;
		}

		public Map<ConfigurationArea, List<ConfigurationAreaDependency>> getNeededDependencyErrors() {
			return _neededDependencyErrors;
		}

		public List<ConfigurationArea> getAreasWithUnknownDependencies() {
			return _unknownAreas;
		}

		/**
		 * Fügt einen Bereich hinzu, dessen Abhängigkeiten zu anderen Bereichen noch nicht erfasst wurden.
		 *
		 * @param area s.o.
		 */
		public void addUnknownArea(ConfigurationArea area) {
			_unknownAreas.add(area);
		}

		public String toString() {
			return "Optionale Abhängigkeiten: " + _optionalDependencyErrors.toString() + " Notwendige Abhängigkeiten: " + _neededDependencyErrors.toString();
		}
	}

	/**
	 * Gibt das Ergebnis einer Prüfung mittel {@link de.bsvrz.sys.funclib.debug.Debug} aus.
	 * <p>
	 * Fehlen Bereiche deren Abhängigkeit optionale ist, wird eine Warnung ausgegeben.
	 * <p>
	 * Wurden die Abhängigkeiten eines Bereichs noch nicht erfasst, so wird eine Warnung ausgegeben.
	 * <p>
	 * Fehlen Bereiche deren Abhängigkeit notwenig ist, wird ein Error ausgegeben.
	 * <p>
	 * Wird auch nur ein Error ausgegeben, wird nach Ausgabe aller Warnungen und Errors eine Exception geworfen.
	 *
	 * @param dependencyCheckResult Egebnis einer Prüfung
	 *
	 * @throws IllegalStateException Es wurden notwendige Abhängigkeiten zwischen Bereichen gefunden, die nicht aufgelöst werden konnten.
	 */
	public void printAndVerifyAreaDependencyCheckResult(AreaDependencyCheck.AreaDependencyCheckResult dependencyCheckResult) {

		


		final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredOptionalErrors = dependencyCheckResult.getOptionalDependencyErrors();
		if(occuredOptionalErrors.isEmpty() == false) {
			// Es sind einige Abhängigkeiten zwischen den Bereichen nicht erfüllt. Allerdings sind diese Abhängigkeiten optional und führen deshalb zu einer
			// Warnung.
			final Set<ConfigurationArea> areas = occuredOptionalErrors.keySet();
			// Alle Bereich, deren Abhängigkeiten nicht erfüllt sind (optionale Abhängigkeit)

			final StringBuffer text = new StringBuffer();
			for(ConfigurationArea area : areas) {
				final List<ConfigurationAreaDependency> dependencies = occuredOptionalErrors.get(area);

				text.append(
						"Der Bereich " + area.getPid()
						+ " besitzt folgende optionale Abhängigkeiten zu anderen Bereichen:"
						+ "\n"
				);

				for(ConfigurationAreaDependency dependency : dependencies) {
					text.append(dependency.toString());
					text.append("\n");
				}
			}// alle optionalen Abhängigkeiten
			_debug.warning(text.toString());
		}

		final List<ConfigurationArea> areasWithUnknownDependencies = dependencyCheckResult.getAreasWithUnknownDependencies();
		if(areasWithUnknownDependencies.isEmpty() == false) {
			final StringBuffer text = new StringBuffer();

			for(ConfigurationArea areaWithUnknownDependency : areasWithUnknownDependencies) {
				text.append("Für den Bereich ").append(areaWithUnknownDependency.getPid()).append(" wurden mögliche Abhängigkeiten noch nicht erfasst. \n");
			}
			_debug.warning(text.toString());
		}

		// Alle Abhängigkeiten, die benötigt werden, aber nicht vorhanden sind.
		final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredNeededDependencyErrors = dependencyCheckResult.getNeededDependencyErrors();
		if(occuredNeededDependencyErrors.isEmpty() == false) {
			// Es fehlen Bereiche, die gebraucht werden. Bereiche ausgeben und eine Exception werfen, da in diesem Fall die Konfiguration nicht gestartet werden
			// darf.

			final Set<ConfigurationArea> areas = occuredNeededDependencyErrors.keySet();
			final StringBuffer text = new StringBuffer();

			for(ConfigurationArea area : areas) {

				text.append(
						"Der Bereich " + area.getPid() + " benötigt folgende Bereiche in den angegebenen Versionen:" + "\n"
				);

				final List<ConfigurationAreaDependency> list = occuredNeededDependencyErrors.get(area);
				for(ConfigurationAreaDependency dependency : list) {
					text.append(dependency.toString());
					text.append("\n");
				}
			}// alle benötigen Bereiche
			throw new IllegalStateException(text.toString());
		}
	}
}
