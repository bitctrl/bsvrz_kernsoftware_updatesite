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
import de.bsvrz.pat.sysbed.main.ApplicationInterface;

/**
 * Das Interface erm�glicht das Hinzuf�gen von Modulen zu einer {@link ApplicationInterface Applikation}. <p>Ein Modul erh�lt durch eine Vorauswahl eine
 * Datenidentifikation (bestehend aus Objekttypen, Attributgruppen, Aspekte und Objekte). Ist die Datenidentifikation f�r das Modul {@link #isPreselectionValid
 * g�ltig} , dann kann es {@link #startModule gestartet} werden. <br>Zur Beschriftung eines Buttons f�r das Modul kann die Methode {@link #getButtonText}
 * verwendet werden. Soll ein Tooltip angegeben werden, der die Anforderungen an die Datenidentifikation des Moduls ausgibt, dann ist {@link #getTooltipText} zu
 * verwenden.</p> <p>Einige der Methoden dieses Interfaces wurden bereits in einem {@link ExternalModuleAdapter Adapter} implementiert. </p>
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5001 $
 * @see #setApplication
 * @see #getModuleName
 * @see #startSettings
 * @see #change
 */
public interface ExternalModule {

	/**
	 * Mit dieser Methode wird der Name des Moduls abgefragt. Er wird u.a. daf�r ben�tigt, bei der Auflistung gespeicherter Einstellungen dem Anwender mitzuteilen,
	 * welches Modul sich hinter den Einstellungen verbirgt.
	 *
	 * @return der Name des Moduls
	 */
	public String getModuleName();

	/**
	 * Damit der Button (oder eine andere Komponente), mit der das Modul gestartet wird, einen zum Modul passenden Text erh�lt, wird dieser �bergeben.
	 *
	 * @return der Text des Buttons
	 */
	public String getButtonText();

	/**
	 * Ein Tooltip, welcher beschreibt, was f�r eine Datenidentifikation dieses Modul ben�tigt, kann hier �bergeben werden. Dieser Tooltip wird bei dem Button
	 * (oder einer anderen Komponente), die dieses Modul startet, angezeigt.
	 *
	 * @return Text des Tooltips
	 */
	public String getTooltipText();

	/**
	 * Diese Methode wird von der Applikation aufgerufen, wenn der Button (oder eine andere Komponente), die dieses Modul repr�sentiert, bet�tigt wird. Ggf. wird
	 * ein dem Modul zugeh�render Dialog aufgerufen, damit weitere Einstellungen, neben der �bergebenen Datenidentifikation vorgenommen werden k�nnen.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startModule(final SettingsData settingsData);

	/**
	 * Sind die Einstellungen des Dialogs eines Moduls bekannt, kann das Modul direkt gestartet werden. Die Methode wird durch den "Starten"-Button aufgerufen, der
	 * sich auf dem Panel befindet, wo die gespeicherten Einstellungen dargestellt werden.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void startSettings(final SettingsData settingsData);

	/**
	 * Diese Methode dient dazu bereits gespeicherte Einstellungen zu �ndern, indem der Dialog des Moduls aufgerufen wird. Wird auf dem Panel, wo die gespeicherten
	 * Einstellungen dargestellt werden eine Einstellung ausgew�hlt und der "�ndern"-Button bet�tigt, dann wird diese Methode aufgerufen.
	 *
	 * @param settingsData enth�lt die ausgew�hlte Datenidentifikation
	 */
	public void change(final SettingsData settingsData);

	/**
	 * Diese Methode pr�ft, ob die Auswahl der Datenidentifikation den Anforderungen des Moduls entspricht.
	 *
	 * @param settingsData die Einstellungsdaten mit der Datenidentifikation
	 *
	 * @return gibt zur�ck, ob die Auswahl der Datenidentifikation den Anforderungen des Moduls entspricht.
	 */
	public boolean isPreselectionValid(final SettingsData settingsData);

	/**
	 * Es wird einmal ein {@link de.bsvrz.pat.sysbed.main.ApplicationInterface Applikationsobjekt} an das Modul �bergeben, damit das Modul auf die Methoden des
	 * <code>ApplicationInterfaces</code> zugreifen kann. Diese Methode wurde bereits im {@link ExternalModuleAdapter} implementiert.
	 *
	 * @param application die Applikation
	 */
	public void setApplication(final ApplicationInterface application);
}

