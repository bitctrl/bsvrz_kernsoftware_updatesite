/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.sys.funclib.application;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;

/**
 * Dieses Interface erweitert die {@link StandardApplication} um die {@link #connect(de.bsvrz.dav.daf.main.ClientDavParameters)}-Methode.
 * Damit kann eine Implementierung dieses Interfaces den Verbindungsaufbau und das Anmelden beim Datenverteiler mittels
 * Benutzername und Passwort selbst umsetzen. Dies bietet die Möglichkeit, den Anmeldevorgang durch einen Login-Dialog
 * zu erweitern, wie in der abstrakten Klasse {@link AbstractGUIApplication} geschehen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see StandardApplication
 * @see AbstractGUIApplication
 */
public interface GUIApplication extends StandardApplication {
	/**
	 * Die Implementierung dieser Methode soll eine Verbindung zum Datenverteiler herstellen und nach erfolgreicher
	 * Anmeldung (Login) zurückgeben.
	 *
	 * @param parameters Parameter für die Datenverteiler-Applikationsfunktionen. Wird für den Verbindungsaufbau benötigt.
	 * @return eine Verbindung zum Datenverteiler
	 */
	ClientDavInterface connect(ClientDavParameters parameters);
}
