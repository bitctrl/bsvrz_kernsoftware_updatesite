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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.byteArray;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.Data;

import java.util.Iterator;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniﬂ Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5055 $ / $Date: 2007-09-01 11:31:09 +0200 (Sa, 01 Sep 2007) $ / ($Author: rs $)
 */
public abstract class ByteArrayStructuredData extends ByteArrayData {
	protected ByteArrayStructuredData(byte[] bytes, int offset, AttributeInfo info) {
		super(bytes, offset, info);
	}

	public String valueToString() {
		final boolean isArray = isArray();
		StringBuffer result = new StringBuffer();
		result.append(isArray ? "[" : "{");
		try {
			for(Iterator i = iterator(); i.hasNext();) {
				try {
					ByteArrayData item = (ByteArrayData)i.next();
					result.append(isArray ? item.valueToString() : item.toString());
				}
				catch(Exception e) {
					result.append("<<Fehler:").append(e.getMessage()).append(">>");
				}
				if(i.hasNext()) result.append("; ");
			}
		}
		catch(Exception e) {
			result.append("<<").append(e.getMessage()).append(">>");
		}
		result.append(isArray ? "]" : "}");
		return result.toString();
	}

	public Data.NumberValue asUnscaledValue() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einen unskaliertem Zahlwert dargestellt werden");
	}

	public Data.TimeValue asTimeValue() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einem Zeitwert dargestellt werden");
	}

	public Data.NumberValue asScaledValue() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einem skalierten Zahlwert dargestellt werden");
	}

	public Data.ReferenceValue asReferenceValue() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einem Referenzwert dargestellt werden");
	}

	public Data.TextValue asTextValue() {
		throw new UnsupportedOperationException("Attribut " + getName() + " kann nicht in einem Textwert dargestellt werden");
	}

}
