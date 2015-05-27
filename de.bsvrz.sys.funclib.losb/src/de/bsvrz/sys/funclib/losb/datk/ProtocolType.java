/*
 * Copyright 2014 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

/**
 * Definiert die Art eines Protokolls, Z.B. Zustandsprotokoll, �nderungsprotokoll, Ereignisprotokoll
 *
 * @author Kappich Systemberatung
 * @version $Revision: 12760 $
 */
public enum ProtocolType {
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein Zustandsprotokoll ist. Das
	 * Zustandsprotokoll dient der �bertragung des vollen Zustands aller Objekte pro Zeitpunkt
	 * an dem neue Daten von mindestens einem Objekt vorliegen. Zeilen deren Inhalt
	 * sich nicht ge�ndert hat, werden mit einer NoChange-Kennung ersetzt. Zellen f�r
	 * die sich keine neuen Inhalte ergeben haben, werden mit den
	 * bisherigen Werten aus dem letzten Datensatz aufgef�llt.
	 */
	StatusProtocol,
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein �nderungsprotokoll ist. Das
	 * �nderungsprotokoll dient der �bertragung des vollen Zustands aller Objekte nur dann,
	 * wenn sich der Inhalt von mindestens einem Attribut ge�ndert hat. Zellen f�r
	 * die sich keine neuen Inhalte ergeben haben, werden mit den
	 * bisherigen Werten aus dem letzten Datensatz aufgef�llt.
	 */
	DeltaProtocol,
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein Ereignisprotokoll ist. Mit dem Ereignisprotokoll
	 * soll erreicht werden, dass die Daten so weitergegeben werden, wie sie im Archiv vorliegen.
	 * Das bedeutet, dass Datens�tze nicht aufgef�llt werden wenn kein neuer Zustand anliegt,
	 * dass Datens�tze nicht durch NoChange-Kennungen ersetzt werden wenn sich das Datum nicht ge�ndert hat
	 * und dass Datens�tze nicht untersr�ckt werden wenn sich der Inhalt nicht ge�ndert hat.
	 * <p>
	 * Das Ereignisprotokoll wird erst am Version 2.8.0 unterst�tzt, �ltere PuA-Server behandeln dies wie ein Zustandsprotokoll.
	 */
	EventProtocol,
	/**
	 * Gibt an, dass der Default-Wert des Skriptes beibehalten werden soll, Wenn das Skript keine explizite Vorgabe macht, wird ein
	 * Zustandsprotokoll erstellt.
	 */
	Undefined
}
