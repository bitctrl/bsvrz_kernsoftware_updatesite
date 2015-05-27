/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.bsvrz.dav.daf.main.config;

import de.bsvrz.dav.daf.main.Data;

/**
 * Jeder Attributtyp stellt einen sogenannten "undefiniert Wert" f�r seine Attribute zur Verf�gung. Soll ein Datensatz
 * verschickt werden, der ein Attribut enth�lt das auf diesen "undefiniert Wert" gesetzt ist, wird eine Exception
 * geworfen.
 * <p/>
 * Dieses Interface wird von allen Attributtypen implementiert und stellt Methoden zur Verwaltung des "undefiniert Wert"
 * zur Verf�gung.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision: 5055 $ / $Date: 2007-09-01 11:31:09 +0200 (Sat, 01 Sep 2007) $ / ($Author: rs $)
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
	 * Die Implementierung dieser Methode pr�ft ob das �bergebene Attribut definiert ist. Ein Attribut gilt als definiert,
	 * wenn der Wert des Attributs ungleich dem sogenannten "undefiniert Wert" ist. Der "undefiniert Wert" ist am
	 * Attributtyp gespeichert, der dieses Interface implementiert.
	 *
	 * @param data Attribut, das gepr�ft werden soll
	 * @return true = Das Attribut ist definiert; false = Das Attribut enth�lt als Wert den sogenannten "undefiniert Wert"
	 * @see #setToUndefined
	 */
	public boolean isDefined(Data data);
}
