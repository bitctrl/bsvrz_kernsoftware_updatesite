/*
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

import de.bsvrz.dav.daf.main.config.DynamicObjectType;


/**
 * Dieses Interface stellt f�r dynamische Objekte Informationen zur Verf�gung.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5074 $ / $Date: 2007-09-02 14:19:12 +0200 (So, 02 Sep 2007) $ / ($Author: rs $)
 */
public interface DynamicObjectInfo extends SystemObjectInformationInterface {

	/**
	 * Diese Methode gibt an, zu welchem Zeitpunkt das dynamische Objekt des Konfigurationsbereichs g�ltig wird/wurde.
	 *
	 * @return s.o.
	 */
	long getFirstValidTime();

	/**
	 * Diese Methode gibt an, zu welchem Zeitpunkt das dynamische Objekt des Konfigurationsbereichs ung�ltig wird/wurde. Der Wert "0" bedeutet, dass dieser
	 * Zeitpunkt noch unbekannt ist und das das Objekt bis zu Festlegung dieses Zeitpunkt g�ltig bleibt.
	 *
	 * @return s.o.
	 */
	long getFirstInvalidTime();

	/**
	 * Diese Methode markiert ein dynamisches Objekt als ung�ltig, als Zeitpunkt wird die aktuelle Uhrzeit benutzt. Die Methode {@link #getFirstInvalidTime} wird
	 * danach den gerade gesetzten Wert zur�ckgeben. Wird die Methode ein zweites mal aufgerufen, wird der Aufruf ignoriert da das Objekt bereits ung�ltig ist.
	 */
	void setInvalid() throws IllegalStateException;

	/**
	 * Diese Methode gibt die Simulationsvariante des dynamischen Objekts zur�ck.
	 *
	 * @return s.o.
	 */
	short getSimulationVariant();

	/**
	 * Diese Methode l�scht ein dynamisches Objekt. Das Objekt steht danach nicht mehr zur Verf�gung und kann nicht mehr rekonstruiert werden.
	 * <p/>
	 * Eventuelle Informationen in Dateien und/oder in Datenstrukturen m�ssen entfernt werden und d�rfen auch nach Neustart des Systems nicht mehr zur Verf�gung
	 * stehen.
	 *
	 * Diese Methode kann nur auf Objekte angewendet werden, die eine Simulationsvariante gr��er/gleich 1 besitzen. Ist die Simulationsvariante < 1, so wird
	 * nichts gemacht.
	 *
	 * @throws IllegalStateException 
	 */
	void remove() throws IllegalStateException;

	/**
	 * Gibt den Persistenzmodus des dynamischen Objekts zur�ck.
	 * @return PersistenceMode
	 */
	DynamicObjectType.PersistenceMode getPersPersistenceMode();
}
