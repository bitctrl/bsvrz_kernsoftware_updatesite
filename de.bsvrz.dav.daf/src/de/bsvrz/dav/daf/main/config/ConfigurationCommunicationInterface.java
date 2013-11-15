/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstelle zum Zugriff auf den Zustand der Kommunikation zwischen zwei Konfigurationen. Eine Applikation kann sich
 * auf Änderungen des Kommunikationszustandes {@link #addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener)} anmelden und
 * auch wieder {@link #removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener)} abmelden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5953 $
 *
 */
public interface ConfigurationCommunicationInterface {
	
	/**
	 * Meldet einen Beobachter für die Zustandsänderungen der Kommunikation mit der verwaltenden Konfigurationen dieses Objekts an. Bei Änderungen wird die Methode
	 * {@link ConfigurationCommunicationChangeListener#configurationCommunicationChange(ConfigurationCommunicationInterface, boolean)} des angegebenen Beobachters aufgerufen.
	 *
	 * @param listener Beobachter für Zustandsänderungen der Kommunikation.
	 */
	public void addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener);

	/**
	 * Meldet einen Beobachter für die Zustandsänderungen dieser Kommunikation wieder ab.
	 *
	 * @param listener Ein bisher für Zustandsänderungen der Kommunikation angemeldeter Beobachter.
	 */
	public void removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener listener);
	
	/**
	 * Gibt an, ob die Kommunikation mit der verwaltenden Konfiguration dieses Objekts aktiv ist.
	 * 
	 * @return true bedeutet, dass die Kommunikation aktiv ist und false zeigt an, dass die Kommunikation unterbrochen ist
	 */
	public boolean isConfigurationCommunicationActive();
}
