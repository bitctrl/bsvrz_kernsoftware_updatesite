/*
 * Copyright 2014 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.datk;

/**
 * Definiert die Art eines Protokolls, Z.B. Zustandsprotokoll, Änderungsprotokoll, Ereignisprotokoll
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public enum ProtocolType {
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein Zustandsprotokoll ist. Das
	 * Zustandsprotokoll dient der Übertragung des vollen Zustands aller Objekte pro Zeitpunkt
	 * an dem neue Daten von mindestens einem Objekt vorliegen. Zeilen oder Zellen (siehe {@linkplain NoChangeMarker}) deren Inhalt
	 * sich nicht geändert hat, werden mit einer NoChange-Kennung ersetzt. Zellen für
	 * die sich keine neuen Inhalte ergeben haben, werden üblicherweise mit den
	 * bisherigen Werten aus dem letzten Datensatz aufgefüllt.
	 */
	StatusProtocol,
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein Änderungsprotokoll ist. Das
	 * Änderungsprotokoll dient der Übertragung des vollen Zustands aller Objekte nur dann,
	 * wenn sich der Inhalt von mindestens einem Attribut geändert hat. Zellen für
	 * die sich keine neuen Inhalte ergeben haben, werden mit den
	 * bisherigen Werten aus dem letzten Datensatz aufgefüllt.
	 */
	DeltaProtocol,
	/**
	 * Gibt an, dass das zu erstellende Protokoll ein Ereignisprotokoll ist. Mit dem Ereignisprotokoll
	 * soll erreicht werden, dass die Daten so weitergegeben werden, wie sie im Archiv vorliegen.
	 * Das bedeutet, dass Datensätze nicht aufgefüllt werden wenn kein neuer Zustand anliegt,
	 * dass Datensätze nicht durch NoChange-Kennungen ersetzt werden wenn sich das Datum nicht geändert hat
	 * und dass Datensätze nicht unterdrückt werden wenn sich der Inhalt nicht geändert hat.
	 * <p>
	 * Das Ereignisprotokoll wird erst ab Version 2.8.0 unterstützt, ältere PuA-Server behandeln dies wie ein Zustandsprotokoll.
	 */
	EventProtocol,
	/**
	 * Gibt an, dass der Default-Wert des Skriptes beibehalten werden soll, Wenn das Skript keine explizite Vorgabe macht, wird ein
	 * Zustandsprotokoll erstellt.
	 */
	Undefined
}
