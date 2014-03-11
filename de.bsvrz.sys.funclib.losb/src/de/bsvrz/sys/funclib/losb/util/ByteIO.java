/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.util;

/**
 * Diese Klasse enthaelt die Funktionalitaet fuer das Handling von Ganzzahlen in Byte-Arrays.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class ByteIO {

	/** Trennzeichenfolge zwischen Datensaetzen. */
	public static final byte[] SEPARATOR = new byte[]{'#', '#', '\n'};

	/** Datentyp-Laenge in Byte. */
	public static final int INT4B_LEN = 4, LONG8B_LEN = 8, LONG5B_LEN = 5, LONG6B_LEN = 6;

	/**
	 * Schreibt den Seperator an die angegebene Stelle im uebergebenen Array.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 *
	 * @return Laenge des Seperators in Byte.
	 */
	public static int writeSeparator(byte[] buf, int pos) {
		System.arraycopy(SEPARATOR, 0, buf, pos, SEPARATOR.length);
		return SEPARATOR.length;
	}

	/**
	 * Schreibt den Integer val in das Byte-Array buf an die Stelle pos. Val kann eine positive oder negative Zahl sein.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 * @param val Integer-Wert
	 *
	 * @return Laenge des Integer in Byte.
	 */
	public static int writeSignedInt4Bytes(byte[] buf, int pos, int val) {
		buf[pos] = (byte)(val >>> 24);
		buf[++pos] = (byte)(val >>> 16);
		buf[++pos] = (byte)(val >>> 8);
		buf[++pos] = (byte)val;
		return INT4B_LEN;
	}

	/**
	 * Schreibt den Long val in das Byte-Array buf an die Stelle pos. Val kann eine positive oder negative Zahl sein.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 * @param val Long-Wert.
	 *
	 * @return Laenge des Long in Byte.
	 */
	public static int writeSignedLong8Byte(byte[] buf, int pos, long val) {
		int hi = (int)(val >>> 32);	// int sind schneller als long
		int lo = (int)val;
		buf[pos] = (byte)(hi >>> 24);
		buf[++pos] = (byte)(hi >>> 16);
		buf[++pos] = (byte)(hi >>> 8);
		buf[++pos] = (byte)hi;
		buf[++pos] = (byte)(lo >>> 24);
		buf[++pos] = (byte)(lo >>> 16);
		buf[++pos] = (byte)(lo >>> 8);
		buf[++pos] = (byte)lo;
		return LONG8B_LEN;
	}

	/**
	 * Schreibt die untersten 5 Byte des Long val in das Byte-Array buf an die Stelle pos. Der geschriebene Wert ist stets >= 0.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 * @param val Long-Wert, nur die untersten 5 Byte werden beruecksichtigt
	 *
	 * @return Laenge des 5-Byte-Long in Byte
	 */
	public static int writeUnsignedLong5Byte(byte[] buf, int pos, long val) {
		int hi = (int)(val >>> 32);	// int sind schneller als long
		int lo = (int)val;
		buf[pos] = (byte)hi;
		buf[++pos] = (byte)(lo >>> 24);
		buf[++pos] = (byte)(lo >>> 16);
		buf[++pos] = (byte)(lo >>> 8);
		buf[++pos] = (byte)lo;
		return LONG5B_LEN;
	}

	/**
	 * Schreibt die untersten 6 Byte des Long val in das Byte-Array buf an die Stelle pos. Der geschriebene Wert ist stets >= 0.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 * @param val Long-Wert, nur die untersten 6 Byte werden beruecksichtigt (also auch nur positive Zahlen)
	 *
	 * @return Laenge des 6-Byte-Long in Byte
	 */
	public static int writeUnsignedLong6Byte(byte[] buf, int pos, long val) {
		int hi = (int)(val >>> 32);	// int sind schneller als long
		int lo = (int)val;
		buf[pos] = (byte)(hi >>> 8);
		buf[++pos] = (byte)hi;
		buf[++pos] = (byte)(lo >>> 24);
		buf[++pos] = (byte)(lo >>> 16);
		buf[++pos] = (byte)(lo >>> 8);
		buf[++pos] = (byte)lo;
		return LONG6B_LEN;
	}

	/**
	 * Schreibt das Quell-Byte-Array b in das Ziel-Byte-Array buf an die Stelle pos.
	 *
	 * @param buf Ziel-Byte-Array
	 * @param pos Position
	 * @param b   Quell-Byte-Array
	 *
	 * @return Laenge des Quell-Byte-Array b.
	 */
	public static int writeBytes(byte[] buf, int pos, byte[] b) {
		System.arraycopy(b, 0, buf, pos, b.length);
		return b.length;
	}

	/**
	 * Liest 4 Bytes im Byte-Array buf ab Stelle pos und liefert sie als Integer. Das erste Bit wird als Vorzeichenbit interpretiert.
	 *
	 * @param buf Byte-Array.
	 * @param pos Position.
	 *
	 * @return Integer.
	 */
	public static int readSignedInt4Bytes(byte[] buf, int pos) {
		// Bytes werden in Java als Integer gespeichert und sind stets signed, also & 0xff:		
		return ((buf[pos] & 0xFF) << 24) + ((buf[++pos] & 0xFF) << 16) + ((buf[++pos] & 0xFF) << 8) + (buf[++pos] & 0xFF);
	}

	/**
	 * Liest 8 Bytes im Byte-Array buf ab Stelle pos und liefert sie als Long. Das erste Bit wird als Vorzeichenbit interpretiert.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 *
	 * @return Long
	 */
	public static long readSignedLong8Byte(byte[] buf, int pos) {
		return ((long)((buf[pos] << 24) +			 // inkl. VZ
		               ((buf[++pos] & 0xFF) << 16) + ((buf[++pos] & 0xFF) << 8) + (buf[++pos] & 0xFF)) << 32) + ((((long)buf[++pos]) & 0xFF) << 24) +
		                                                                                                      // VZ nicht interpretieren!
		                                                                                                      ((buf[++pos] & 0xFF) << 16)
		                                                                                                      + ((buf[++pos] & 0xFF) << 8) + (buf[++pos]
		                                                                                                                                      & 0xFF);
	}

	/**
	 * Liest 5 Bytes im Byte-Array buf ab Stelle pos und liefert sie als Long. Das erste Bit wird nicht als Vorzeichenbit interpretiert.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 *
	 * @return Long (0-2^40)
	 */
	public static long readUnsignedLong5Byte(byte[] buf, int pos) {
		return ((long)((buf[pos] & 0xFF)) << 32) + ((((long)buf[++pos]) & 0xFF) << 24) +	 // VZ nicht interpretieren!
		       ((buf[++pos] & 0xFF) << 16) + ((buf[++pos] & 0xFF) << 8) + (buf[++pos] & 0xFF);
	}

	/**
	 * Liest 6 Bytes im Byte-Array buf ab Stelle pos und liefert sie als Long. Das erste Bit wird nicht als Vorzeichenbit interpretiert.
	 *
	 * @param buf Byte-Array
	 * @param pos Position
	 *
	 * @return Long (0-2^48)
	 */
	public static long readUnsignedLong6Byte(byte[] buf, int pos) {
		return ((long)(((buf[pos] & 0xFF) << 8) + (buf[++pos] & 0xFF)) << 32) + ((((long)buf[++pos]) & 0xFF) << 24) +	 // VZ nicht interpretieren!
 			((buf[++pos] & 0xFF) << 16) + 
 			((buf[++pos] & 0xFF) << 8) + 
 			(buf[++pos] & 0xFF);
    }
    
}
