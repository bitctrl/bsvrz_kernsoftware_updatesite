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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Zeichenketten-Attributtypen. Attribute von
 * diesem Attributtyp enthalten Zeichenketten.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface StringAttributeType extends AttributeType, UndefinedAttributeValueAccess {
	/**
	 * Ergebnis der Methode {@link #getEncodingValue} für die ASCII Kodierung.
	 */
//	public static final byte ASCII = 1;

	/**
	 * Ergebnis der Methode {@link #getEncodingValue} für die ISO-8859-1 Kodierung.
	 */
	public static final byte ISO_8859_1 = 2;

	/**
	 * Bestimmt die maximal erlaubte Anzahl von Zeichen in Attributen dieses Typs.
	 *
	 * @return Maximale Anzahl von Zeichen oder 0 falls die Anzahl nicht begrenzt ist.
	 */
	public int getMaxLength();

	/**
	 * Bestimmt, ob die Maximale Anzahl von Zeichen in Attributen dieses Typs beschränkt ist.
	 *
	 * @return <code>true</code> falls die Anzahl Zeichen beschränkt ist, sonst <code>false</code>.
	 */
	public boolean isLengthLimited();

	/**
	 * Bestimmt die Kodierung der Zeichen in Attributen dieses Typs.
	 *
	 * @return Name der Zeichenkodierung wie im Datenkatalog beim Attributtyp "att.zeichenKodierung" festgelegt.
	 */
	public String getEncodingName();

	/**
	 * Bestimmt die Kodierung der Zeichen in Attributen dieses Typs.
	 *
	 * @return {@link #ISO_8859_1}.
	 */
	public byte getEncodingValue();
}

