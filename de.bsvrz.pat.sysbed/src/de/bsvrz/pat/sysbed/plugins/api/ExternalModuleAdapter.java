/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
 * Diese Klasse stellt f�r einige Methoden des Interfaces {@link ExternalModule} Implementierungen zur Verf�gung, die f�r jedes Modul gleich sind. Dazu geh�rt
 * das Bekanntmachen der {@link de.bsvrz.pat.sysbed.main.ApplicationInterface Applikation} mit den zu ihr geh�renden Methoden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see #setApplication
 * @see #getConnection
 * @see #getArgumentList
 * @see #saveSettings
 */
public abstract class ExternalModuleAdapter implements ExternalModule {

	/** speichert eine Instanz der Applikation */
	private ApplicationInterface _application;

	/**
	 * Setzt die Applikation f�r die Module. Die gleichnamige Methode des {@link ExternalModule Interfaces} wurde �berschrieben.
	 *
	 * @param application die Applikation, die die Module nutzt
	 */
	public void setApplication(final ApplicationInterface application) {
		_application = application;
	}

	/**
	 * Gibt die {@link ApplicationInterface Applikation} zur�ck.
	 *
	 * @return die Applikation
	 */
	public ApplicationInterface getApplication() {
		return _application;
	}

	/**
	 * Diese Methode erm�glicht den Modulen, die Verbindung zum Datenverteiler der Applikation zu nutzen. Sie wird zur�ckgegeben. Die gleichnamige Methode der
	 * Applikation wird delegiert.
	 *
	 * @return die Verbindung zum Datenverteiler
	 */
	public ClientDavInterface getConnection() {
		return _application.getConnection();
	}

	/**
	 * Die Argumentliste, die an die <code>main</code>-Methode der Applikation �bergeben wurde, wird hier zur�ckgegeben. Diese wird u.a. dann ben�tigt, wenn ein
	 * Proze� in einer Konsole gestartet werden soll. Die gleichnamige Methode der Applikation wird delegiert.
	 *
	 * @return die Argumentliste, die an die <code>main</code>-Methode �bergeben wurde
	 */
	public List getArgumentList() {
		return _application.getArgumentList();
	}

	/**
	 * Diese Methode erm�glicht den Modulen die Einstellungen ihrer Dialoge an die Applikation zur Speicherung zu �bergeben. Die gleichnamige Methode der
	 * Applikation wird delegiert.
	 *
	 * @param settingsData die Einstellungsdaten
	 */
	public void saveSettings(final SettingsData settingsData) {
		_application.saveSettings(settingsData);
	}

	/**
	 * Allgemeing�ltige Einstellungen (eine Attributgruppe, Aspekt und mindestens ein Objekt m�ssen ausgew�hlt sein) werden hier gepr�ft. Soll noch mehr �berpr�ft
	 * werden, so muss die Methode �berschrieben werden.
	 *
	 * @param settingsData die Einstellungsdaten mit der Datenidentifikation
	 *
	 * @return <code>true</code>, die Anforderungen wurden erf�llt, sonst <code>false</code>
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
