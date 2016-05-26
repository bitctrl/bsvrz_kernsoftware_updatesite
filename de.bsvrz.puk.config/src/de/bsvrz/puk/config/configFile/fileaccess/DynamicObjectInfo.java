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

import de.bsvrz.dav.daf.main.config.DynamicObjectType;


/**
 * Dieses Interface stellt für dynamische Objekte Informationen zur Verfügung.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface DynamicObjectInfo extends SystemObjectInformationInterface {

	/**
	 * Diese Methode gibt an, zu welchem Zeitpunkt das dynamische Objekt des Konfigurationsbereichs gültig wird/wurde.
	 *
	 * @return s.o.
	 */
	long getFirstValidTime();

	/**
	 * Diese Methode gibt an, zu welchem Zeitpunkt das dynamische Objekt des Konfigurationsbereichs ungültig wird/wurde. Der Wert "0" bedeutet, dass dieser
	 * Zeitpunkt noch unbekannt ist und das das Objekt bis zu Festlegung dieses Zeitpunkt gültig bleibt.
	 *
	 * @return s.o.
	 */
	long getFirstInvalidTime();

	/**
	 * Diese Methode markiert ein dynamisches Objekt als ungültig, als Zeitpunkt wird die aktuelle Uhrzeit benutzt. Die Methode {@link #getFirstInvalidTime} wird
	 * danach den gerade gesetzten Wert zurückgeben. Wird die Methode ein zweites mal aufgerufen, wird der Aufruf ignoriert da das Objekt bereits ungültig ist.
	 */
	void setInvalid() throws IllegalStateException;

	/**
	 * Diese Methode gibt die Simulationsvariante des dynamischen Objekts zurück.
	 *
	 * @return s.o.
	 */
	short getSimulationVariant();

	/**
	 * Diese Methode löscht ein dynamisches Objekt. Das Objekt steht danach nicht mehr zur Verfügung und kann nicht mehr rekonstruiert werden.
	 * <p>
	 * Eventuelle Informationen in Dateien und/oder in Datenstrukturen müssen entfernt werden und dürfen auch nach Neustart des Systems nicht mehr zur Verfügung
	 * stehen.
	 *
	 * @throws IllegalStateException 
	 */
	void remove() throws IllegalStateException;

	/**
	 * Gibt den Persistenzmodus des dynamischen Objekts zurück.
	 * @return PersistenceMode
	 */
	DynamicObjectType.PersistenceMode getPersPersistenceMode();
}
