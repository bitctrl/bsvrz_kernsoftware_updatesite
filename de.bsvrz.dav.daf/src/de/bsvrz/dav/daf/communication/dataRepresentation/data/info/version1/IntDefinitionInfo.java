/*
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.main.config.IntegerAttributeType;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public final class IntDefinitionInfo extends NumberDefinitionInfo {
	public IntDefinitionInfo(IntegerAttributeType att) {
		super(att);
	}

	public int getFixedSize() {
		return 4;
	}

	public byte unscaledByteValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attributtyp " + getAttributeType().getPid() + " kann nicht in einem byte dargestellt werden");
	}

	public short unscaledShortValue(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("Attributtyp " + getAttributeType().getPid() + " kann nicht in einem short dargestellt werden");
	}

	public int unscaledIntValue(byte[] bytes, int offset) {
		return readInt(bytes, offset);
	}

	public long unscaledLongValue(byte[] bytes, int offset) {
		return readInt(bytes, offset);
	}

	public float unscaledFloatValue(byte[] bytes, int offset) {
		return readInt(bytes, offset);
	}

	public double unscaledDoubleValue(byte[] bytes, int offset) {
		return readInt(bytes, offset);
	}

}
