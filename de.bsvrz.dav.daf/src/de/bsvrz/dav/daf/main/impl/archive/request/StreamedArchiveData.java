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
package de.bsvrz.dav.daf.main.impl.archive.request;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.archive.ArchiveData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Die Objekte dieser Klassen stellen die archivierten Datensätze dar, die die anfragende
 * Applikation angefordert hat.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class StreamedArchiveData implements ArchiveData {

	private final long _dataTime;
	private final long _archiveTime;
	private final long _dataIndex;
	private final DataState _dataState;
	private final ArchiveDataKind _archiveDataKind;
	private final Data _data;
	private final SystemObject _object;
	private final DataDescription _dataDescription;

	public StreamedArchiveData(long dataTime, long archiveTime, long dataIndex, DataState dataState,
							   ArchiveDataKind archiveDataKind, Data data, SystemObject systemObject,
							   DataDescription dataDescription) {
		_dataTime = dataTime;
		_archiveTime = archiveTime;
		_dataIndex = dataIndex;
		_dataState = dataState;
		_archiveDataKind = archiveDataKind;
		_data = data;
		_object = systemObject;
		_dataDescription = dataDescription;
	}

	public long getDataTime() {
		return _dataTime;
	}

	public long getArchiveTime() {
		return _archiveTime;  
	}

	public long getDataIndex() {
		return _dataIndex;
	}

	public DataState getDataType() {
		return _dataState;
	}

	public ArchiveDataKind getDataKind() {
		return _archiveDataKind;
	}

	public Data getData() {
		return _data;
	}

	public SystemObject getObject() {
		return _object;
	}

	public DataDescription getDataDescription() {
		return _dataDescription;
	}

	@Override
	public String toString() {
		return "StreamedArchiveData{" + "_dataTime=" + _dataTime + ", _archiveTime=" + _archiveTime + ", _dataIndex=" + _dataIndex + ", _dataState="
		       + _dataState + ", _archiveDataKind=" + _archiveDataKind + ", _data=" + _data + ", _object=" + _object + ", _dataDescription=" + _dataDescription
		       + '}';
	}
}
