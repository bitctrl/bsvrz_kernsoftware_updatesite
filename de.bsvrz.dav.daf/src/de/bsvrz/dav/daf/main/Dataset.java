/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Schnittstelle zum verallgemeinerten Zugriff auf online empfangene Ergebnisdatensätze und auf Archivdatensätze.
 * Die Klasses des Objekts kann mit <code>instanceof</code> bestimmt werden (ArchiveData oder ResultData).
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
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
	 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten gehören.
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
