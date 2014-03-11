/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.puk.config.configFile.fileaccess;

/**
 * Diese Klasse stellt zwei Objekte zur Verfügung, die festzulegen ob der Zeitpunkt, zu der eine Version aktiviert
 * wurde, vom Konfigurationsverantwortlichen benutzt wird oder ob der Zeitpunkt der lokalen Aktivierung einer Versio
 * benutzt wird.
 * <p/>
 * Diese Information ist zum Beispiel bei Anfragen an den Konfigurationsbereich nach Objekten, die in einem bestimmten
 * Zeitbereich gültig sein müssen, wichtig.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $ / ($Author: rs $)
 */
public enum ConfigurationAreaTime {
	LOCAL_ACTIVATION_TIME("lokale Zeit für aktivierte Versionen"), GLOBAL_ACTIVATION_TIME("Zeit des Konfigurationsverantwortlichen für aktivierte Versionen");

	private final String _name;

	private ConfigurationAreaTime(String name) {
		_name = name;
	}

	public String toString() {
		return _name;
	}
}
