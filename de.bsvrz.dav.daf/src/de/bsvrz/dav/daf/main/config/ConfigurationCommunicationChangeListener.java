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
 * Schnittstelle für Änderungen des Kommunikationszustandes zwischen Konfigurationen, auf die sich eine Applikation 
 * {@link ConfigurationCommunicationInterface#addConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener)}
 * anmelden und 
 * {@link ConfigurationCommunicationInterface#removeConfigurationCommunicationChangeListener(ConfigurationCommunicationChangeListener)} 
 * abmelden kann.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 6072 $
 *
 */
public interface ConfigurationCommunicationChangeListener {
	/**
	 * Methode, die nach Änderung des Kommunikationszustandes aufgerufen wird. Die Methode ist seitens der Applikation zu implementieren.
	 * 
	 * @param object	Objekt ({@link MutableSet} oder {@link DynamicObject}) zu dessen verwaltenden Konfiguration sich der Kommunikationszustand geändert hat
	 * @param configComStatus der aktuelle Kommunikationszustand. True bedeutet die Kommunikation steht, false zeigt eine Unterbrechung der Kommunikation an
	 *
	 */
	public void configurationCommunicationChange(ConfigurationCommunicationInterface object, boolean configComStatus);
}
