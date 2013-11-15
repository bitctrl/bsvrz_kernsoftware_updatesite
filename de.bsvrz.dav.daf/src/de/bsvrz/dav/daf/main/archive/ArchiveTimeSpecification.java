/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.archive;

/**
 * Ein Objekt dieser Klasse bestimmt den Zeitbereich/Indexbereich (Intervall) einer Archivanfrage. Bei der Bestimmung
 * des Intervalls k�nnen folgende 6 F�lle unterschieden werden: <br> Anfangs- und Endzeitpunkt bzgl. Datenzeit <br>
 * Anfangs- und Endzeitpunkt bzgl. Archivzeit <br> Anfangs- und Enddatenindex <br> Endzeitpunkt bzgl. Datenzeit, Anfang
 * durch Anzahl Datens�tze <br> Endzeitpunkt bzgl. Archivzeit, Anfang durch Anzahl Datens�tze <br> Enddatenindex, Anfang
 * durch Anzahl Datens�tze
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public class ArchiveTimeSpecification {
	private final TimingType _timingType;
	private final boolean _startRelative;

	/**
	 * siehe Konstruktor
	 */
	private final long _intervalStart;

	/**
	 * siehe Konstruktor
	 */
	private final long _intervalEnd;

	/**
	 * @param timingType    Typ der Intervallangabe in den folgenden Parametern. Je nach Wert beziehen sich die Angaben in
	 *                      <code>intervalStart</code> und <code>intervalEnd</code> auf den Datenzeitstempel, den
	 *                      Archivzeitstempel oder den Datensatzindex.
	 * @param intervalStart Start des Intervalls. Positive Werte dieses Parameters beziehen sich je nach Wert des
	 *                      Parameters <code>timingType</code> auf den Datenzeitstempel, den Archivzeitstempel oder den
	 *                      Datensatzindex. Wenn <code>startRelative</code> auf <code>true</code> gesetzt wurde, wird der
	 *                      Wert als Anzahl Datens�tze vor dem <code>intervalEnd</code> interpretiert, unabh�ngig vom
	 *                      gew�hlten <code>timingType</code>.
	 * @param intervalEnd   Ende des Intervalls. Dieser Wert bezieht sich je nach <code>timingType</code> auf den
	 *                      Datenzeitstempel, den Archivzeitstempel oder den Datensatzindex.
	 * @param startRelative true = Der Wert in <code>intervalStart</code> wird als Anzahl Datens�tze interpretiert, die vor
	 *                      dem <code>intervalEnd</code> liegen m�ssen; false =  <code>intervalStart, intervalEnd</code>
	 *                      sind absolute Werte
	 */
	public ArchiveTimeSpecification(TimingType timingType, boolean startRelative, long intervalStart, long intervalEnd) {
		_timingType = timingType;
		_startRelative = startRelative;
		_intervalStart = intervalStart;
		_intervalEnd = intervalEnd;
	}

	/**
	 * Auf welchen Typ (Datenzeit, Archivzeit, Datenindex) beziehen sich die Intervallangaben.
	 *
	 * @return Typ des Intervalls
	 */
	public TimingType getTimingType() {
		return _timingType;
	}

	/**
	 * Der R�ckgabewert bestimmt, ob der Intervallstart als relativer Wert interpretiert werden muss.
	 *
	 * @return true = Der Intervallstart wird als Anzahl Datens�tze interpretiert, die vor dem Intervallende liegen m�ssen;
	 *         false = Der Intervallstart wird als absoluter Wert interpretiert
	 */
	public boolean isStartRelative() {
		return _startRelative;
	}

	/**
	 * Der R�ckgabewert ist entweder ein aboluter Wert oder eine gewisse Anzahl Datens�tze, die vor dem Intervallende
	 * liegen m�ssen ({@link ArchiveTimeSpecification#isStartRelative}).
	 *
	 * @return absoluter Wert oder Anzahl Datens�tze
	 */
	public long getIntervalStart() {
		return _intervalStart;
	}

	/**
	 * Das Intervallende einer Archivanfrage.
	 *
	 * @return abolutes Intervallende
	 */
	public long getIntervalEnd() {
		return _intervalEnd;
	}

	/**
	 * Liefert eine String-Repr�sentation dieser Klasse zur�ck.
	 *
	 * @return String-Repr�sentation dieser Klasse
	 */
	public String toString() {
		return "ArchiveTimeSpecification{" +
				"_timingType=" + _timingType +
				", _startRelative=" + _startRelative +
				", _intervalStart=" + _intervalStart +
				", _intervalEnd=" + _intervalEnd +
				"}";
	}
}
