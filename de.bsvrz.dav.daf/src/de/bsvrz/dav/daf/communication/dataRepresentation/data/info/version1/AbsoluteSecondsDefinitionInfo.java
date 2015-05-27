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

import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5084 $ / $Date: 2007-09-03 10:42:50 +0200 (Mon, 03 Sep 2007) $ / ($Author: rs $)
 */
public final class AbsoluteSecondsDefinitionInfo extends AttributeTypeDefinitionInfo {
	private static final DateFormat _absoluteSecondsFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	public AbsoluteSecondsDefinitionInfo(TimeAttributeType att) {
		super(att);
	}

	public boolean isSizeFixed() {
		return true;
	}

	public int getFixedSize() {
		return 4;
	}

	public String getValueText(byte[] bytes, int offset) {
		try {
			Date date = new Date(getMillis(bytes, offset));
			synchronized(_absoluteSecondsFormat) {
				return _absoluteSecondsFormat.format(date);
			}
		}
		catch(Exception e) {
			return "<<" + e.getMessage() + ">>";
		}
	}

	public String getSuffixText(byte[] bytes, int offset) {
		return "Uhr";
	}


	public boolean isTimeAttribute() {
		return true;
	}

	public long getSeconds(byte[] bytes, int offset) {
		final long seconds = readUnsignedInt(bytes, offset);
		return seconds;
	}

	public long getMillis(byte[] bytes, int offset) {
		return getSeconds(bytes, offset) * 1000L;
	}
}
