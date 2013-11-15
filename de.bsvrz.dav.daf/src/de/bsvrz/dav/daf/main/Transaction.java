/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

import java.util.List;

/**
 * Interface mit dem auf die Daten einer Transaktion zugegriffen werden kann
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8953 $
 */
public interface Transaction {

	/**
	 * Bestimmt die Beschreibung der im Ergebnis enthaltenen Daten.
	 *
	 * @return Beschreibung der Daten
	 */
	public TransactionDataDescription getDataDescription();

	/**
	 * Bestimmt den Datenzeitstempel des Datensatzes.
	 *
	 * @return Datenzeitstempel in Millisekunden seit 1970
	 */
	long getDataTime();

	/**
	 * Bestimmt den je Datenidentifikation eindeutigen vom Datenverteiler vergebenen Datensatzindex dieses Datensatzes.
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
	 * Bestimmt den eigentlichen Datensatzes mit den von der jeweiligen Attributgruppe definierten Attributwerten dieses Datensatzes.
	 *
	 * @return Collection mit Attributwerten oder leere Liste im Falle eines leeren Datensatzes. Der R�ckgabewert kann in Spezialf�llen eine leere Liste sein, auch
	 *         wenn hasData true zur�ckgibt. Das ist der Fall, wenn die Transaktion auch irgendeinem Grund keine inneren Datens�tze enth�lt, es sich aber um einen
	 *         normalen Datensatz handelt, der von einer Quelle /einem Sender gesendet wurde.
	 */
	List<TransactionDataset> getData();

	/**
	 * Bestimmt ob ein Transaktionsdatensatz vorliegt
	 *
	 * @return false, wenn der Transaktionsdatensatz ein leerer Datensatz ist. True wenn er vorhanden ist (auch wenn keine inneren Datens�tze vorhanden sind).
	 */
	boolean hasData();

	/**
	 * Bestimmt das System-Objekt zu dem die im Ergebnis enthaltenen Daten geh�ren.
	 *
	 * @return System-Objekt der enthaltenen Daten
	 */
	SystemObject getObject();
}
