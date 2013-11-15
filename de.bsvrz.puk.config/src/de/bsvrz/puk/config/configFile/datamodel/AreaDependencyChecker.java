/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.config.management.ConfigAreaAndVersion;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 6053 $
 */
public class AreaDependencyChecker implements AreaDependencyCheck {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();


	public AreaDependencyCheckResult checkAreas(
			List<ConfigAreaAndVersion> areas
	) {

		// Pr�fen ob g�ltige Versionen �bergeben wurden.
		checkRightVersions(areas);
		return check(areas);
	}

	/**
	 * Pr�ft, ob f�r jeden der �bergebenen Bereich die Abh�ngigkeiten zu anderen Bereichen erf�llt ist und gibt das Ergebnis der Pr�fung zur�ck.
	 *
	 * @param areas Bereiche, die in den �bergenen Versionen, zum Start der Konfiguration genutzt werden sollen
	 *
	 * @return Ergebnis der Pr�fung ob alle Abh�ngigkeiten, wie gefordert, aufgel�st werden k�nnen.
	 */
	private AreaDependencyCheckResult check(List<ConfigAreaAndVersion> areas) {
		// Speichert alle gefundenen Fehler
		final CheckResult result = new CheckResult();

		// Alle Bereiche, die zur Verf�gung stehen
		final Map<String, Short> usedAreasAndVersions = getUsedAreasWithVersionMap(areas);

		for(ConfigAreaAndVersion area : areas) {

			// Bei alten Bereichen kann es sein, das die Abh�ngigkeiten noch gar nicht vorliegen
			final ConfigConfigurationAreaInterface configArea = (ConfigConfigurationAreaInterface)area.getConfigArea();

				if(configArea.dependenciesChecked() == false) {
					result.addUnknownArea(configArea);
					// Da es keine Abh�ngigkeiten zum pr�fen gibt, kann der n�chste Bereich betrachtet werden
					continue;
				}

			// Alle Abh�ngigkeiten des Bereichs anfordern und die Abh�ngigkeiten sortieren
			final Map<String, List<ConfigurationAreaDependency>> sortedDependencies = getAllDependencies(configArea.getDependencyFromOtherConfigurationAreas());

			// Der betrachtete Bereich soll gestartet werden.
			// Die Version in der das geschehen soll bestimmt die n�tigen Abh�ngigkeiten.

			// Welche Abh�ngigkeiten sind in dieser Version aufgetreten. Diese m�ssen gefunden werden.
			short startVersion = usedAreasAndVersions.get(configArea.getPid());

			// Bereiche, von dem der Bereich abh�ngig ist
			final Set<String> areasDependentFrom = sortedDependencies.keySet();

			for(String areaDependentFrom : areasDependentFrom) {
				// In dieser Liste steht, in welcher Version der Bereich vom Bereich areaDependentFrom abh�ngig wurde
				final List<ConfigurationAreaDependency> dependencies = sortedDependencies.get(areaDependentFrom);

				// Das letzte Element in der Liste besitzt die Gr��te "OccurredAtVersion"
				for(int nr = dependencies.size() - 1; nr >= 0; nr--) {
					final ConfigurationAreaDependency areaDependency = dependencies.get(nr);
					if(areaDependency.getDependencyOccurredAtVersion() <= startVersion) {
						// Es wurde eine Abh�ngigkeit gefunden.
						// Der Bereich muss in der richtigen Version vorliegen, bzw. �berhaupt verf�gbar sein.

						// In diesem Bereich liegt der abh�ngige Bereich vor

						final Short versionDependentAreaIsUsed = usedAreasAndVersions.get(areaDependentFrom);
						if(versionDependentAreaIsUsed != null) {

							// Der Bereich liegt vor, aber wird er in einer Version genutzt, die gr��er gleich der ben�tigten Version ist ?
							if(versionDependentAreaIsUsed >= areaDependency.getNeededVersion()) {
								// Ja, der Bereich liegt in der Version vor, die ben�tigt wird um die Abh�ngigkeiten aufzul�sen.
								// Es kann der n�chste Bereich betrachtet werden.
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
									throw new IllegalArgumentException("Unbekannte Abh�ngigkeit: " + areaDependency.getKind());
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
								throw new IllegalArgumentException("Unbekannte Abh�ngigkeit: " + areaDependency.getKind());
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param areaDependencies Alle Abh�ngigkeiten des Bereichs.
	 *
	 * @return Als Schl�ssel dient der Bereich, von dem ein anderer Bereich Abh�ngig ist. Als Wert wird die Abh�ngigkeit selbst zur�ckgegeben (beinhaltet den
	 *         Bereich noch einmal). Die Liste ist sortiert. Die letzte Abh�ngigkeit, die entdeckt wurde, ist am Ende der Liste gespeichert.
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
		}//for, �ber alle Listen, die sortiert werden m�ssen

		return result;
	}

	/**
	 * Erzeugt eine Map in der alle Bereiche gespeichert sind, die der Konfiguration zur Verf�gung und die Version, in der der Bereich zur Verf�gung steht.
	 * <p/>
	 * Als Schl�ssel dient die Pid des Konfigurationsbereichs, als Wert wird die Version zur�ckgegeben.
	 *
	 * @param areas Alle Bereiche und deren Versionen, die der Konfiguration zur Verf�gung stehen.
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
	 * Methode, die pr�ft, ob alle Versionen >= 0 sind.
	 *
	 * @param areas Bereiche mit Versionsnummern, die >= 0 sein m�ssen
	 *
	 * @throws IllegalArgumentException Wenn eine Version < 0 ist.
	 */
	private void checkRightVersions(List<ConfigAreaAndVersion> areas) {
		for(ConfigAreaAndVersion area : areas) {
			if(area.getVersion() < 0) throw new IllegalArgumentException("F�r einen Bereich wurde keine g�ltige Versionsnummer angegeben: " + area);
		}
	}

	private static final class CheckResult implements AreaDependencyCheckResult {

		private final Map<ConfigurationArea, List<ConfigurationAreaDependency>> _optionalDependencyErrors = new HashMap<ConfigurationArea, List<ConfigurationAreaDependency>>();

		private final Map<ConfigurationArea, List<ConfigurationAreaDependency>> _neededDependencyErrors = new HashMap<ConfigurationArea, List<ConfigurationAreaDependency>>();

		private final List<ConfigurationArea> _unknownAreas = new ArrayList<ConfigurationArea>();

		/**
		 * @param area       Bereich, f�r den die Abh�ngigkeit gilt, die nicht aufgel�st werden konnte.
		 * @param dependency Abh�ngigkeit
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
		 * F�gt einen Bereich hinzu, dessen Abh�ngigkeiten zu anderen Bereichen noch nicht erfasst wurden.
		 *
		 * @param area s.o.
		 */
		public void addUnknownArea(ConfigurationArea area) {
			_unknownAreas.add(area);
		}

		public String toString() {
			return "Optionale Abh�ngigkeiten: " + _optionalDependencyErrors.toString() + " Notwendige Abh�ngigkeiten: " + _neededDependencyErrors.toString();
		}
	}

	/**
	 * Gibt das Ergebnis einer Pr�fung mittel {@link de.bsvrz.sys.funclib.debug.Debug} aus.
	 * <p/>
	 * Fehlen Bereiche deren Abh�ngigkeit optionale ist, wird eine Warnung ausgegeben.
	 * <p/>
	 * Wurden die Abh�ngigkeiten eines Bereichs noch nicht erfasst, so wird eine Warnung ausgegeben.
	 * <p/>
	 * Fehlen Bereiche deren Abh�ngigkeit notwenig ist, wird ein Error ausgegeben.
	 * <p/>
	 * Wird auch nur ein Error ausgegeben, wird nach Ausgabe aller Warnungen und Errors eine Exception geworfen.
	 *
	 * @param dependencyCheckResult Egebnis einer Pr�fung
	 *
	 * @throws IllegalStateException Es wurden notwendige Abh�ngigkeiten zwischen Bereichen gefunden, die nicht aufgel�st werden konnten.
	 */
	public void printAndVerifyAreaDependencyCheckResult(AreaDependencyCheck.AreaDependencyCheckResult dependencyCheckResult) {

		


		final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredOptionalErrors = dependencyCheckResult.getOptionalDependencyErrors();
		if(occuredOptionalErrors.isEmpty() == false) {
			// Es sind einige Abh�ngigkeiten zwischen den Bereichen nicht erf�llt. Allerdings sind diese Abh�ngigkeiten optional und f�hren deshalb zu einer
			// Warnung.
			final Set<ConfigurationArea> areas = occuredOptionalErrors.keySet();
			// Alle Bereich, deren Abh�ngigkeiten nicht erf�llt sind (optionale Abh�ngigkeit)

			final StringBuffer text = new StringBuffer();
			for(ConfigurationArea area : areas) {
				final List<ConfigurationAreaDependency> dependencies = occuredOptionalErrors.get(area);

				text.append(
						"Der Bereich " + area.getPid()
						+ " besitzt folgende optionale Abh�ngigkeiten zu anderen Bereichen:"
						+ "\n"
				);

				for(ConfigurationAreaDependency dependency : dependencies) {
					text.append(dependency.toString());
					text.append("\n");
				}
			}// alle optionalen Abh�ngigkeiten
			_debug.warning(text.toString());
		}

		final List<ConfigurationArea> areasWithUnknownDependencies = dependencyCheckResult.getAreasWithUnknownDependencies();
		if(areasWithUnknownDependencies.isEmpty() == false) {
			final StringBuffer text = new StringBuffer();

			for(ConfigurationArea areaWithUnknownDependency : areasWithUnknownDependencies) {
				text.append("F�r den Bereich ").append(areaWithUnknownDependency.getPid()).append(" wurden m�gliche Abh�ngigkeiten noch nicht erfasst. \n");
			}
			_debug.warning(text.toString());
		}

		// Alle Abh�ngigkeiten, die ben�tigt werden, aber nicht vorhanden sind.
		final Map<ConfigurationArea, List<ConfigurationAreaDependency>> occuredNeededDependencyErrors = dependencyCheckResult.getNeededDependencyErrors();
		if(occuredNeededDependencyErrors.isEmpty() == false) {
			// Es fehlen Bereiche, die gebraucht werden. Bereiche ausgeben und eine Exception werfen, da in diesem Fall die Konfiguration nicht gestartet werden
			// darf.

			final Set<ConfigurationArea> areas = occuredNeededDependencyErrors.keySet();
			final StringBuffer text = new StringBuffer();

			for(ConfigurationArea area : areas) {

				text.append(
						"Der Bereich " + area.getPid() + " ben�tigt folgende Bereiche in den angegebenen Versionen:" + "\n"
				);

				final List<ConfigurationAreaDependency> list = occuredNeededDependencyErrors.get(area);
				for(ConfigurationAreaDependency dependency : list) {
					text.append(dependency.toString());
					text.append("\n");
				}
			}// alle ben�tigen Bereiche
			throw new IllegalStateException(text.toString());
		}
	}
}
