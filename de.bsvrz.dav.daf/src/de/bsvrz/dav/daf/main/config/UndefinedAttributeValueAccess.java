/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
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
package de.bsvrz.dav.daf.main.config;

import de.bsvrz.dav.daf.main.Data;

/**
 * Jeder Attributtyp stellt einen sogenannten "undefiniert Wert" für seine Attribute zur Verfügung. Soll ein Datensatz
 * verschickt werden, der ein Attribut enthält das auf diesen "undefiniert Wert" gesetzt ist, wird eine Exception
 * geworfen.
 * <p>
 * Dieses Interface wird von allen Attributtypen implementiert und stellt Methoden zur Verwaltung des "undefiniert Wert"
 * zur Verfügung.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface UndefinedAttributeValueAccess {
	/**
	 * Die Implementierung dieser Methode setzt ein Attribut auf den sogenannten "undefiniert Wert". Dieser Wert unterscheidet sich je
	 * nach Attributtyp. Bei einem "Referenz Attributtyp" wird der "undefiniert Wert" zum Beispiel mit "0" abgebildet. Beim
	 * Attributtyp Zeichenkette wird der Wert als String "_Undifiniert_" dargestellt.
	 *
	 * @param data Attribut, dessen Wert auf "undefiniert Wert" gesetzt werden soll
	 */
	public void setToUndefined(Data data);

	/**
	 * Die Implementierung dieser Methode prüft ob das übergebene Attribut definiert ist. Ein Attribut gilt als definiert,
	 * wenn der Wert des Attributs ungleich dem sogenannten "undefiniert Wert" ist. Der "undefiniert Wert" ist am
	 * Attributtyp gespeichert, der dieses Interface implementiert.
	 *
	 * @param data Attribut, das geprüft werden soll
	 * @return true = Das Attribut ist definiert; false = Das Attribut enthält als Wert den sogenannten "undefiniert Wert"
	 * @see #setToUndefined
	 */
	public boolean isDefined(Data data);
}
