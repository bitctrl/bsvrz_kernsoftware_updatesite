/*
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

import java.util.List;

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften von Ganzzahl-Attributtypen. Attribute von diesem
 * Attributtyp enthalten ganze Zahlen. Durch den Attributtyp werden diskrete Zustände und ein Zahlenbereich
 * jeweils mit Minimum, Maximum, Skalierung und Einheit definiert.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Stephan Homeyer (sth)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fr, 31 Aug 2007) $ / ($Author: rs $)
 */
public interface IntegerAttributeType extends AttributeType, UndefinedAttributeValueAccess {
	/**
	 * Rückgabewert von {@link #getByteCount} für ein Byte große Attribute.
	 */
	public static final int BYTE = 1;

	/**
	 * Rückgabewert von {@link #getByteCount} für zwei Byte große Attribute.
	 */
	public static final int SHORT = 2;

	/**
	 * Rückgabewert von {@link #getByteCount} für vier Byte große Attribute.
	 */
	public static final int INT = 4;

	/**
	 * Rückgabewert von {@link #getByteCount} für acht Byte große Attribute.
	 */
	public static final int LONG = 8;

	/**
	 * Bestimmt die Größe von Attributen dieses Typs in Bytes. Diese Methode liefert den gleichen Wert wie die
	 * Methode {@link #getByteCount()} als <code>short</code> zurück.
	 *
	 * @return Anzahl der für die Darstellung des Attributs benötigten Bytes
	 *
	 * @deprecated Wurde von der Methode {@link #getByteCount()} abgelöst.
	 */
	public short getValueSize();

	/**
	 * Bestimmt die Größe von Attributen dieses Typs in Bytes.
	 *
	 * @return Anzahl der für die Darstellung des Attributs benötigten Bytes
	 */
	public int getByteCount();

	/**
	 * Bestimmt den für Attribute dieses Attributtyps definierten Zahlenbereich.
	 *
	 * @return Definierter Zahlenbereich oder <code>null</code>, wenn kein Zahlenbereich definiert ist.
	 */
	public IntegerValueRange getRange();

	/**
	 * Bestimmt die mit diesem Attributtyp erlaubten diskreten Zustände eines Attributs.
	 *
	 * @return Liste mit diskreten Zuständen dieses Attributs als Objekte der Klasse {@link IntegerValueState}.
	 */
	public List<IntegerValueState> getStates();
}

