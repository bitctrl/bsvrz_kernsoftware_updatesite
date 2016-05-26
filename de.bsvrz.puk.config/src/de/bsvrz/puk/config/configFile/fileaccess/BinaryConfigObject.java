/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Binäres Konfigurationsobjekt
 *
 * @see de.bsvrz.puk.config.configFile.fileaccess.BinaryObject
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class BinaryConfigObject extends BinaryObject {
	private final long _objectId;
	private final int _pidHashCode;
	private final long _typeId;
	private final short _firstInvalid;
	private final short _firstValid;
	private final byte[] _packedBytes;

	public BinaryConfigObject(final long objectId, final int pidHashCode, final long typeId, final short firstInvalid, final short firstValid, final byte[] packedBytes) {
		_objectId = objectId;
		_pidHashCode = pidHashCode;
		_typeId = typeId;
		_firstInvalid = firstInvalid;
		_firstValid = firstValid;
		_packedBytes = packedBytes;
	}

	@Override
	public int write(final DataOutput output) throws IOException {
		final int configObjectSize = CONFIG_OBJ_HEADER_SIZE + _packedBytes.length;
		output.writeInt(configObjectSize);
		output.writeLong(_objectId);
		output.writeInt(_pidHashCode);
		output.writeLong(_typeId);
		output.writeByte(0);
		output.writeShort(_firstInvalid);
		output.writeShort(_firstValid);
		output.write(_packedBytes);
		return configObjectSize + 4;
	}

	@Override
	public long getObjectId() {
		return _objectId;
	}

	@Override
	public int getPidHashCode() {
		return _pidHashCode;
	}

	@Override
	public long getTypeId() {
		return _typeId;
	}

	public short getFirstInvalid() {
		return _firstInvalid;
	}

	public short getFirstValid() {
		return _firstValid;
	}

	@Override
	public byte[] getPackedBytes() {
		return _packedBytes;
	}

	@Override
	public SystemObjectInformationInterface toSystemObjectInfo(final ConfigAreaFile file, final long position) throws IOException, NoSuchVersionException {
		if(_objectId == 0) return null;
		return ConfigurationObjectInformation.fromBinaryObject(file, position, this);
	}
}
