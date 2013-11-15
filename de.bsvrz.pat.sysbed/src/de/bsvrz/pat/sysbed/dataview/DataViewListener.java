/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.dataview;

import java.util.*;

/**
 * Dieses Interface trennt das Model ({@link DataViewModel}) und das View ({@link DataViewPanel}) voneinander. 
 * Das View erh�lt eine Nachricht, falls das Model von der Applikation neue oder ge�nderte Datens�tze erh�lt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 7843 $
 */
public interface DataViewListener {

	/**
	 * F�gt beliebig viele Datens�tze ans Ende aller bisher gesammelten Datens�tze.
	 *
	 * @param dataTableObjects neue Datens�tze
	 */
	public void addDataTableObjects(final List<DataTableObject> dataTableObjects);

	/**
	 * F�gt einen Datensatz an eine bestimmte Position der bisherigen Datens�tze ein.
	 *
	 * @param index           Position des neuen Datensatzes
	 * @param dataTableObject der neue Datensatz
	 */
	public void addDataTableObject(int index, final DataTableObject dataTableObject);

	/**
	 * �bergibt alle Datens�tze auf einmal.
	 *
	 * @param dataTableObjects die neuen Datens�tze
	 */
	public void setDataTableObjects(List<DataTableObject> dataTableObjects);

	/**
	 * L�scht einen Datensatz an der Position.
	 *
	 * @param index Position des zu l�schenden Datensatzes
	 */
	public void removeDataTableObject(int index);

	/**
	 * Aktualisiert einen Datensatz an angegebener Position.
	 *
	 * @param index           Position des zu �ndernden Datensatzes
	 * @param dataTableObject neuer Datensatz
	 */
	public void update(int index, final DataTableObject dataTableObject);
}
