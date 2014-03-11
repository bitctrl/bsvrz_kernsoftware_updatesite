/*
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
package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Schnittstelle zum verallgemeinerten Zugriff auf online empfangene Ergebnisdatens�tze und auf Archivdatens�tze.
 * Die Klasses des Objekts kann mit <code>instanceof</code> bestimmt werden (ArchiveData oder ResultData).
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision: 5084 $ / $Date: 2007-09-03 10:42:50 +0200 (Mo, 03 Sep 2007) $ / ($Author: rs $)
 * @see ResultData
 * @see de.bsvrz.dav.daf.main.archive.ArchiveData
 */
public interface Dataset {
	/**
	 * Bestimmt den Datenzeitstempel des Datensatzes.
	 *
	 * @return Datenzeitstempel in Millisekunden seit 1970
	 */
	long getDataTime();

	/**
	 * Bestimmt den je Datenidentifikation eindeutigen vom Datenverteiler vergebenen Datensatzindex dieses
	 * Datensatzes.
	 *
	 * @return Datensatzindex
	 */
	long getDataIndex();

	/**
	 * Bestimmt den Datensatztyp des Datensatzes.
	 *
	 * @return Datensatztyp
	 */
	DataState getDataType();

	/**
	 * Bestimmt die Datensatzart des Datensatzes.
	 *
	 * @return Datensatzart
	 */
	ArchiveDataKind getDataKind();

	/**
	 * Bestimmt den eigentlichen Datensatzes mit den von der jeweiligen Attributgruppe definierten Attributwerten dieses
	 * Datensatzes.
	 *
	 * @return Datensatz mit Attributwerten oder <code>null</code> im Falle eines leeren Datensatzes.
	 */
	Data getData();

	/**
	 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten geh�ren.
	 *
	 * @return System-Objekt der enthaltenen Daten
	 */
	SystemObject getObject();

	/**
	 * Bestimmt die Beschreibung der im Ergebnis enthaltenen Daten.
	 *
	 * @return Beschreibung der Daten
	 */
	DataDescription getDataDescription();
}
