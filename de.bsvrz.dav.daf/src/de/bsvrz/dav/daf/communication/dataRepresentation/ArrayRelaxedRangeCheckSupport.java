/*
 * Copyright 2013 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.communication.dataRepresentation;

/**
 * Dieses Interface markiert Daten-Arrays, die das Setzen von Längen außerhalb der eigentlich erlaubten Bereichen unterstützen.
 * Dies wird benötigt, damit Deserialisierer von Daten bei unversionierten Datenmodelländerungen den empfangenen, im lokalen
 * Datenmodell eigentlich ungültigen, Datensatz korrekt deserialisieren können.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ArrayRelaxedRangeCheckSupport {

	/**
	 * Setzt die Länge eines Daten-Arrays mit gelockerter Bereichsprüfung und ohne Initialisierung der Werte. Diese Methode ist unsicher,
	 * und sollte nur verwendet werden, wenn es notwendig ist und die Konsequenzen bekannt sind. Es können ungültige Datensätze entstehen.
	 * @param newLength neue Arraylänge
	 */
	void setLengthRelaxedRangeCheck(int newLength);
}
