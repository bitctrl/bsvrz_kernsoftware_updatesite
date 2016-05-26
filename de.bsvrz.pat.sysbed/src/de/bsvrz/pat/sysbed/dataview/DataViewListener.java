/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.dataview;

import java.util.*;

/**
 * Dieses Interface trennt das Model ({@link DataViewModel}) und das View ({@link DataViewPanel}) voneinander. 
 * Das View erhält eine Nachricht, falls das Model von der Applikation neue oder geänderte Datensätze erhält.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface DataViewListener {

	/**
	 * Fügt beliebig viele Datensätze ans Ende aller bisher gesammelten Datensätze.
	 *
	 * @param dataTableObjects neue Datensätze
	 */
	public void addDataTableObjects(final List<DataTableObject> dataTableObjects);

	/**
	 * Fügt einen Datensatz an eine bestimmte Position der bisherigen Datensätze ein.
	 *
	 * @param index           Position des neuen Datensatzes
	 * @param dataTableObject der neue Datensatz
	 */
	public void addDataTableObject(int index, final DataTableObject dataTableObject);

	/**
	 * Übergibt alle Datensätze auf einmal.
	 *
	 * @param dataTableObjects die neuen Datensätze
	 */
	public void setDataTableObjects(List<DataTableObject> dataTableObjects);

	/**
	 * Löscht einen Datensatz an der Position.
	 *
	 * @param index Position des zu löschenden Datensatzes
	 */
	public void removeDataTableObject(int index);

	/**
	 * Aktualisiert einen Datensatz an angegebener Position.
	 *
	 * @param index           Position des zu ändernden Datensatzes
	 * @param dataTableObject neuer Datensatz
	 */
	public void update(int index, final DataTableObject dataTableObject);
}
