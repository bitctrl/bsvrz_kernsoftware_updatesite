/*
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.main.config.StringAttributeType;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class StringDefinitionInfo extends AttributeTypeDefinitionInfo {
	public StringDefinitionInfo(StringAttributeType stringAttributeType) {
		super(stringAttributeType);
	}

	public boolean isSizeFixed() {
		return false;
	}

	public int getFixedSize() {
		return 0;
	}

	public int getSize(byte[] bytes, int offset) {
		// Strings werden mit writeUTF kodiert.
		// Vorneweg ist eine 2 Byte unsigned Längenangabe in Big-Endian Format, die die Anzahl der folgenden Bytes
		// enthält.
		return 2 + ((bytes[offset] & 0xff) << 8 | bytes[offset + 1] & 0xff);
	}

	public String getSuffixText(byte[] bytes, int offset) {
		return "";
	}

	public String getValueText(final byte[] bytes, int offset) {
		final int utf8Length = ((bytes[offset] & 0xff) << 8 | bytes[offset + 1] & 0xff);
		offset += 2;
		//return new String(bytes, offset, byteCount, "UTF8");
		StringBuffer result;
		byte b;
		result = new StringBuffer();
		final int endOffset = offset + utf8Length;
		while(offset < endOffset) {
			b = bytes[offset];
			if((b & 0x80) == 0) {
				// 1 Byte für Zeichen 0x0001 .. 0x007f
				result.append((char)b);
				offset += 1;
			}
			else {
				switch(b & 0xe0) {
				case 0xc0:
					// 2 Bytes für Zeichen 0x0000 und 0x0080 .. 0x07ff
					result.append((char)(
										  ((bytes[offset + 0] & 0x1f) << 6) |
						                    ((bytes[offset + 1] & 0x3f) << 0)
					                    ));
					offset += 2;
					break;
				case 0xe0:
					// 3 Bytes für Zeichen 0x0800 .. 0xffff
					result.append((char)(
					        ((bytes[offset + 0] & 0x1f) << 12) |
					        ((bytes[offset + 1] & 0x3f) << 6) |
					        ((bytes[offset + 2] & 0x3f) << 0)
					));
					offset += 3;
					break;
				default:
					throw new RuntimeException("Ungültige utf8 kodierung: " + (int)b);
				}
			}
		}
		return result.toString();
	}
}
