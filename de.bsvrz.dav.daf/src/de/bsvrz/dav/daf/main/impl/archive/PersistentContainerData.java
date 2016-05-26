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

import de.bsvrz.dav.daf.main.DataState;

/**
 * Datensatz eines Archivdatencontainers im Sinne des Persistenzmoduls.
 * Enthalten sind Datenzeitstempel, Archivzeitstempel, Datensatzindex, Typ des Datensatzes
 * und die Nutzdaten in serialisierter Form.
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class PersistentContainerData {
	public static final byte NO_COMPRESSION = 0;
	public static final byte ZIP_COMPRESSION = 1;

	private final DataTiming _timing;
	private final DataState _dataState;
	private final ArchiveDataCompression _compression;
	private final byte[] _dataBytes;


	/**
	 * Erzeugt einen Datensatz.
	 * @param timing  Datenzeitstempel, Archivzeitstempel und Datensatzindex des Datensatzes.
	 * @param dataState  Typ des Datensatzes (Nutzdaten, keine Daten, keine Quelle, potentielle Datenlücke etc.).
	 * @param dataBytes  Byte-Array mit den unkomprimierten, serialisierten Nutzdaten oder <code>null</code> im Falle eines
	 *                   leerer Datensatzes ohne Nutzdaten.
	 */
	public PersistentContainerData(DataTiming timing, DataState dataState, byte[] dataBytes) {
		this(timing, dataState, ArchiveDataCompression.NONE, dataBytes);
	}

	/**
	 * Erzeugt einen Datensatz.
	 * @param timing  Datenzeitstempel, Archivzeitstempel und Datensatzindex des Datensatzes.
	 * @param dataState  Typ des Datensatzes (Nutzdaten, keine Daten, keine Quelle, potentielle Datenlücke etc.).
	 * @param dataBytes  Byte-Array mit den zu serialisierten Nutzdaten oder <code>null</code> im Falle eines
	 *                   leerer Datensatzes ohne Nutzdaten.
	 */
	public PersistentContainerData(DataTiming timing, DataState dataState, ArchiveDataCompression compression, byte[] dataBytes) {
		_timing = timing;
		_dataState = dataState;
		_compression = compression;
		_dataBytes = dataBytes;
	}

	/**
	 * Bestimmt den Typ des Datensatzes (Nutzdaten, keine Daten, keine Quelle, potentielle Datenlücke etc.).
	 * @return Typ des Datensatzes.
	 */
	public DataState getDataType() {
		return _dataState;
	}

	/**
	 * Bestimmt die Nutzdaten dieses Datensatzes in serialisierter Form.
	 * @return Byte-Array mit den serialisierten Nutzdaten oder <code>null</code>, im Falle eines leeren Datensatzes.
	 */
	public byte[] getDataBytes() {
		return _dataBytes;
	}

	/**
	 * Bestimmt Datenzeitstempel, Archivzeitstempel und Datensatzindex des Datensatzes.
	 * @return Datenstruktur mit Datenzeitstempel, Archivzeitstempel und Datensatzindex des Datensatzes.
	 */
	public DataTiming getTiming() {
		return _timing;
	}

	/**
	 * Bestimmt die Kompressionsart dieses Datensatzes.
	 * @return Kompressionsart des Datensatzes.
	 */
	public ArchiveDataCompression getCompression() {
		return _compression;
	}
}
