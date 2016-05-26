/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.archive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Datenstruktur, die Datenzeitstempel, Archivzeitstempel und Datensatzindex eines Datensatzes verwaltet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class DataTiming {
	private final long _dataTime;
	private final long _archiveTime;
	private final long _dataIndex;

	/**
	 * Legt eine neue Datenstruktur mit den übergebenen Werten von Datenzeitstempel, Archivzeitstempel und Datensatzindex
	 * an.
	 *
	 * @param dataTime    Datenzeitstempel des Datensatzes in Millisekunden seit 1970.
	 * @param archiveTime Archivzeitstempel des Datensatzes in Millisekunden seit 1970.
	 * @param dataIndex   Innerhalb des Archivdatencontainers eindeutiger Datensatzindex des Datenverteilers.
	 */
	public DataTiming(long dataTime, long archiveTime, long dataIndex) {
		_dataTime = dataTime;
		_archiveTime = archiveTime;
		_dataIndex = dataIndex;
	}

	/**
	 * Bestimmt den Datenzeitstempel dieses Datensatzes.
	 *
	 * @return Datenzeitstempel in Millisekunden seit 1970.
	 */
	public long getDataTime() {
		return _dataTime;
	}

	/**
	 * Bestimmt den Archivzeitstempel dieses Datensatzes.
	 *
	 * @return Archivzeitstempel in Millisekunden seit 1970.
	 */
	public long getArchiveTime() {
		return _archiveTime;
	}

	/**
	 * Betimmt den je Archivdatenidentifikation eindeutigen Datensatzindex des Datenverteilers.
	 *
	 * @return Datensatzindex.
	 */
	public long getDataIndex() {
		return _dataIndex;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		DateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
		return "DataTiming{" +
		        "_dataTime=" + timeFormat.format(new Date(_dataTime)) +
		        ", _archiveTime=" + timeFormat.format(new Date(_archiveTime)) +
		        ", _dataIndex=" + (_dataIndex >> 32) + "#" + ((_dataIndex >> 2) & 0x3fffffff) + "#" + (_dataIndex & 0x3) +
		        "}";
	}


}
