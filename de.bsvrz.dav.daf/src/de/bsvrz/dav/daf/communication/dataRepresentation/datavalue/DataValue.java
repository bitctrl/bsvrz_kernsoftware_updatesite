/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation.datavalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt eine Basisklasse f�r das Package Datavalue dar. Es werden Konstanten festgelegt und gemeinsamme Methoden deklariert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5049 $
 */
public abstract class DataValue {

	/** Konstante repr�sentiert den DatenTyp byte */
	public static final byte BYTE_TYPE = 1;

	/** Konstante repr�sentiert den DatenTyp short */
	public static final byte SHORT_TYPE = 2;

	/** Konstante repr�sentiert den DatenTyp int */
	public static final byte INTEGER_TYPE = 3;

	/** Konstante repr�sentiert den DatenTyp long */
	public static final byte LONG_TYPE = 4;

	/** Konstante repr�sentiert den DatenTyp float */
	public static final byte FLOAT_TYPE = 5;

	/** Konstante repr�sentiert den DatenTyp double */
	public static final byte DOUBLE_TYPE = 6;

	/** Konstante repr�sentiert den DatenTyp String */
	public static final byte STRING_TYPE = 7;

	/** Konstante repr�sentiert den DatenTyp bit Array */
	public static final byte BIT_ARRAY_TYPE = 10;

	/** Konstante repr�sentiert den DatenTyp byte Array */

	public static final byte BYTE_ARRAY_TYPE = 11;

	/** Konstante repr�sentiert den DatenTyp short Array */
	public static final byte SHORT_ARRAY_TYPE = 12;

	/** Konstante repr�sentiert den DatenTyp int Array */
	public static final byte INTEGER_ARRAY_TYPE = 13;

	/** Konstante repr�sentiert den DatenTyp long Array */
	public static final byte LONG_ARRAY_TYPE = 14;

	/** Konstante repr�sentiert den DatenTyp float Array */
	public static final byte FLOAT_ARRAY_TYPE = 15;

	/** Konstante repr�sentiert den DatenTyp double Array */
	public static final byte DOUBLE_ARRAY_TYPE = 16;

	/** Konstante repr�sentiert den DatenTyp String Array */
	public static final byte STRING_ARRAY_TYPE = 17;

	/** Konstante repr�sentiert den DatenTyp Liste */
	public static final byte ATTRIBUTE_LIST_TYPE = 100;

	/** Konstante repr�sentiert den DatenTyp Arrayliste */
	public static final byte ATTRIBUTE_LIST_ARRAY_TYPE = 110;

	/** Konstante repr�sentiert den Array-Offset */
	public static final byte ARRAY_OFFSET = 10;

	/** Der Typ dieses Datensatzes */
	protected byte _type;

	/**
	 * Gibt den Typ dieses Datensatzes zur�ck
	 *
	 * @return Typ dieses Datensatzes
	 */
	public final byte getType() {
		return _type;
	}

	/**
	 * Gibt ein leeres Objekt vom gegebenen Typ zur�ck
	 *
	 * @param _type _type des Objektes
	 *
	 * @return leeres Objekt vom Typ des �bergabeparameters
	 */
	public static DataValue getObject(byte _type) {
		switch(_type) {
			case(BYTE_TYPE): {
				return new ByteAttribute();
			}
			case(SHORT_TYPE): {
				return new ShortAttribute();
			}
			case(INTEGER_TYPE): {
				return new IntegerAttribute();
			}
			case(LONG_TYPE): {
				return new LongAttribute();
			}
			case(FLOAT_TYPE): {
				return new FloatAttribute();
			}
			case(DOUBLE_TYPE): {
				return new DoubleAttribute();
			}
			case(STRING_TYPE): {
				return new StringAttribute();
			}
			case(ATTRIBUTE_LIST_TYPE): {
				return new AttributeListAttribute();
			}
			case(BYTE_ARRAY_TYPE): {
				return new ByteArrayAttribute();
			}
			case(SHORT_ARRAY_TYPE): {
				return new ShortArrayAttribute();
			}
			case(INTEGER_ARRAY_TYPE): {
				return new IntegerArrayAttribute();
			}
			case(LONG_ARRAY_TYPE): {
				return new LongArrayAttribute();
			}
			case(FLOAT_ARRAY_TYPE): {
				return new FloatArrayAttribute();
			}
			case(DOUBLE_ARRAY_TYPE): {
				return new DoubleArrayAttribute();
			}
			case(STRING_ARRAY_TYPE): {
				return new StringArrayAttribute();
			}
			case(ATTRIBUTE_LIST_ARRAY_TYPE): {
				return new AttributeListArrayAttribute();
			}
		}
		return null;
	}

	/**
	 * Lesen eines Datensatzes vom gegebenen DataInputStream
	 *
	 * @param in Eingabe-Stream
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void read(DataInputStream in) throws IOException;

	/**
	 * Schreiben eines Datensatzes in den gegebenen DataOutputStream
	 *
	 * @param out Ausgabe-Stream
	 *
	 * @throws IOException, wenn beim Schreiben vom Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void write(DataOutputStream out) throws IOException;

	/**
	 * Diese Methode erzeugt eine Kopie dieses Datensatzes
	 *
	 * @return Eine Kopie dieses Datensatzes
	 */
	public abstract DataValue cloneObject();

	/**
	 * Gibt ein String zurr�ck, der diesen Datensatz beschreibt
	 *
	 * @return Der String, der diesen Datensatz beschreibt
	 */
	public abstract String parseToString();

	/**
	 * gibt den Wert zur�ck des Objektes
	 *
	 * @return der Wert des Objektes
	 */
	public abstract Object getValue();
}
