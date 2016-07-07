/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.main;

import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.dav.daf.main.ClientDavInterface;

import java.util.*;

/**
 * Dieses Interface dient für die {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Module} als Schnittstelle zur Applikation. Dadurch haben die Module die Möglichkeit auf die Verbindung
 * zum Datenverteiler der Applikation, die Argumentliste (die beim Starten der Anwendung der <code>main</code>-Methode übergeben wurde) und den Auswahlbaum
 * zugreifen zu können. Die Module können die zu speichernden Einstellungen der Dialog an die Applikation zur weiteren Verarbeitung übergeben werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #getConnection
 * @see #getArgumentList
 * @see #getTreeNodes
 * @see #saveSettings
 */
public interface ApplicationInterface {

	/**
	 * Diese Methode kann dazu verwendet werden, die Verbindung zum Datenverteiler weiterzugeben, damit immer die gleiche Verbindung in der Anwendung genutzt
	 * wird.
	 *
	 * @return die Verbindung zum Datenverteiler
	 */
	public ClientDavInterface getConnection();

	/**
	 * Es wird die Argumentliste übergeben, die beim Starten der Anwendung als Parameter angegeben wurde.
	 *
	 * @return die Argumentliste, die beim Aufruf der Anwendung übergeben wurde
	 */
	public List getArgumentList();

	/**
	 * Der Auswahlbaum des PreselectionTree der Applikation kann hier angefordert werden.
	 *
	 * @return der Auswahlbaum
	 */
	public Collection getTreeNodes();

	/**
	 * Diese Methode ermöglicht der Applikation das Verwalten von Einstellungen der genutzten {@link de.bsvrz.pat.sysbed.plugins.api.ExternalModule Module}. Module, die einen Dialog zum
	 * Einstellen von Parametern nutzen, können mit dieser Methode ihre Einstellungen an die Applikation übergeben. Diese kann dann die Einstellungen dann
	 * speichern, laden und starten.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void saveSettings(final SettingsData settingsData);
}
