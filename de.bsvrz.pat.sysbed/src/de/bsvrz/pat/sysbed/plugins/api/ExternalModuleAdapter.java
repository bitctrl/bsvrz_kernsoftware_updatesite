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

package de.bsvrz.pat.sysbed.plugins.api;

import de.bsvrz.pat.sysbed.plugins.api.settings.SettingsData;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

import de.bsvrz.pat.sysbed.main.ApplicationInterface;

/**
 * Diese Klasse stellt für einige Methoden des Interfaces {@link ExternalModule} Implementierungen zur Verfügung, die für jedes Modul gleich sind. Dazu gehört
 * das Bekanntmachen der {@link de.bsvrz.pat.sysbed.main.ApplicationInterface Applikation} mit den zu ihr gehörenden Methoden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #setApplication
 * @see #getConnection
 * @see #getArgumentList
 * @see #saveSettings
 */
public abstract class ExternalModuleAdapter implements ExternalModule {

	/** speichert eine Instanz der Applikation */
	private ApplicationInterface _application;

	/**
	 * Setzt die Applikation für die Module. Die gleichnamige Methode des {@link ExternalModule Interfaces} wurde überschrieben.
	 *
	 * @param application die Applikation, die die Module nutzt
	 */
	public void setApplication(final ApplicationInterface application) {
		_application = application;
	}

	/**
	 * Gibt die {@link ApplicationInterface Applikation} zurück.
	 *
	 * @return die Applikation
	 */
	public ApplicationInterface getApplication() {
		return _application;
	}

	/**
	 * Diese Methode ermöglicht den Modulen, die Verbindung zum Datenverteiler der Applikation zu nutzen. Sie wird zurückgegeben. Die gleichnamige Methode der
	 * Applikation wird delegiert.
	 *
	 * @return die Verbindung zum Datenverteiler
	 */
	public ClientDavInterface getConnection() {
		return _application.getConnection();
	}

	/**
	 * Die Argumentliste, die an die <code>main</code>-Methode der Applikation übergeben wurde, wird hier zurückgegeben. Diese wird u.a. dann benötigt, wenn ein
	 * Prozeß in einer Konsole gestartet werden soll. Die gleichnamige Methode der Applikation wird delegiert.
	 *
	 * @return die Argumentliste, die an die <code>main</code>-Methode übergeben wurde
	 */
	public List getArgumentList() {
		return _application.getArgumentList();
	}

	/**
	 * Diese Methode ermöglicht den Modulen die Einstellungen ihrer Dialoge an die Applikation zur Speicherung zu übergeben. Die gleichnamige Methode der
	 * Applikation wird delegiert.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void saveSettings(final SettingsData settingsData) {
		_application.saveSettings(settingsData);
	}

	/**
	 * Allgemeingültige Einstellungen (eine Attributgruppe, Aspekt und mindestens ein Objekt müssen ausgewählt sein) werden hier geprüft. Soll noch mehr überprüft
	 * werden, so muss die Methode überschrieben werden.
	 *
	 * @param settingsData die Einstellungsdaten mit der Datenidentifikation
	 *
	 * @return <code>true</code>, die Anforderungen wurden erfüllt, sonst <code>false</code>
	 */
	public boolean isPreselectionValid(final SettingsData settingsData) {
		final AttributeGroup atg = settingsData.getAttributeGroup();
		final Aspect asp = settingsData.getAspect();
		final List<SystemObject> objects = settingsData.getObjects();
		if(atg == null || asp == null || objects == null || objects.isEmpty()) {
			return false;
		}
		return true;
	}
}
