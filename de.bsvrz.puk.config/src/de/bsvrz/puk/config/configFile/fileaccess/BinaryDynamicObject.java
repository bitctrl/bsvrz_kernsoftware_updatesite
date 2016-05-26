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
 * Binäres dynamisches Objekt
 *
 * @see de.bsvrz.puk.config.configFile.fileaccess.BinaryObject
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class BinaryDynamicObject extends BinaryObject {

	private final long _objectId;
	private final int _pidHashCode;
	private final long _typeId;
	private final long _firstInvalid;
	private final long _firstValid;
	private final short _simulationVariant;
	private final byte[] _packedBytes;

	public BinaryDynamicObject(final long objectId, final int pidHashCode, final long typeId, final long firstInvalid, final long firstValid, final short simulationVariant, final byte[] packedBytes) {
		_objectId = objectId;
		_pidHashCode = pidHashCode;
		_typeId = typeId;
		_firstInvalid = firstInvalid;
		_firstValid = firstValid;
		_simulationVariant = simulationVariant;
		_packedBytes = packedBytes;
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

	public long getFirstInvalid() {
		return _firstInvalid;
	}

	public long getFirstValid() {
		return _firstValid;
	}

	public short getSimulationVariant() {
		return _simulationVariant;
	}

	@Override
	public byte[] getPackedBytes() {
		return _packedBytes;
	}

	@Override
	public int write(final DataOutput output) throws IOException {
		final int dynamicObjectSize = DYN_OBJ_HEADER_SIZE + _packedBytes.length;
		output.writeInt(dynamicObjectSize);
		output.writeLong(_objectId);
		output.writeInt(_pidHashCode);
		output.writeLong(_typeId);
		output.writeByte(1);
		output.writeLong(_firstInvalid);
		output.writeLong(_firstValid);
		output.writeShort(_simulationVariant);
		output.write(_packedBytes);
		return dynamicObjectSize + 4;
	}

	@Override
	public SystemObjectInformationInterface toSystemObjectInfo(final ConfigAreaFile file, final long position) throws IOException, NoSuchVersionException {
		if(_objectId == 0) return null;
		return DynamicObjectInformation.fromBinaryObject(file, position, this);
	}
}
