/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.puk.config.configFile.fileaccess;

/**
 * Diese Klasse stellt zwei Objekte zur Verfügung, die festzulegen ob der Zeitpunkt, zu der eine Version aktiviert
 * wurde, vom Konfigurationsverantwortlichen benutzt wird oder ob der Zeitpunkt der lokalen Aktivierung einer Versio
 * benutzt wird.
 * <p>
 * Diese Information ist zum Beispiel bei Anfragen an den Konfigurationsbereich nach Objekten, die in einem bestimmten
 * Zeitbereich gültig sein müssen, wichtig.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
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
