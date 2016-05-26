/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.hexdump.
 * 
 * de.bsvrz.sys.funclib.hexdump is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.hexdump is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.hexdump; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.hexdump;

import java.io.PrintStream;

/**
 * Klasse zum Erzeugen von Hex-Dumps.
 * Ein Hex-Dump enthält ein Darstellung der in einem Byte-Array enthaltenen
 * Bytes.
 * Beispiel:
 * <pre>
 * 00000000:  20 21 22 23  24 25 26 27  28 29 2A 2B  2C 2D 2E 2F     !"#$%&'()*+,-./
 * 00000010:  30 31 32 33  34 35 36 37  38 39 3A 3B  3C 3D 3E 3F    0123456789:;<=>?
 * 00000020:  40 41 42 43  44 45 46 47  48 49 4A 4B  4C 4D 4E 4F    @ABCDEFGHIJKLMNO
 * 00000030:  50 51 52 53  54 55 56 57  58 59 5A 5B  5C 5D 5E 5F    PQRSTUVWXYZ[\]^_
 *</pre>
 * Eine Zeile eines Hexdumps enthält die Darstellung von bis zu 16 Bytes des Byte-Arrays.
 * Links wird die Adresse des ersten Bytes der Zeile dargestellt, in der Mitte werden
 * die Bytes in Hexadezimaler Form und rechts in lesbarer Form dargestellt. Für die
 * lesbare Darstellung wird die ISO-8859-1 Kodierung angenommen und nicht darstellbare
 * Zeichen werden durch Punkte ersetzt.

 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class HexDumper {

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @return  Hex-Dump
	 */
	public static String toString(byte[] bytes) {
		return toString(0, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump.
	 * @param bytes Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 * @return  Hex-Dump
	 */
	public static String toString(byte[] bytes, int startOffset, int count) {
		return toString(0, bytes, startOffset, count);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @return  Hex-Dump
	 */
	public static String toString(int baseAddress, byte[] bytes) {
		return toString(baseAddress, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 * @return  Hex-Dump
	 */
	public static String toString(int baseAddress, byte[] bytes, int startOffset, int count) {
		StringOutput out= new StringOutput();
		dump(out, baseAddress, bytes, startOffset, count);
		return out.toString();
	}



	/** Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf einen
	 * PrintStream aus.
	 * @param out  PrintStream der zur Ausgabe benutzt werden soll.
	 * @param bytes Array mit zu konvertierenden Bytes.
	 */
	public static void dumpTo(PrintStream out, byte[] bytes) {
		dump(new StreamOutput(out), 0, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf einen
	 * PrintStream aus.
	 * @param out  PrintStream der zur Ausgabe benutzt werden soll.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 */
	public static void dumpTo(PrintStream out, byte[] bytes, int startOffset, int count) {
		dump(new StreamOutput(out), 0, bytes, startOffset, count);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf einen
	 * PrintStream aus.
	 * @param out  PrintStream der zur Ausgabe benutzt werden soll.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 */
	public static void dumpTo(PrintStream out, int baseAddress, byte[] bytes) {
		dump(new StreamOutput(out), baseAddress, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf einen
	 * PrintStream aus.
	 * @param out  PrintStream der zur Ausgabe benutzt werden soll.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 */
	public static void dumpTo(PrintStream out, int baseAddress, byte[] bytes, int startOffset, int count) {
		dump(new StreamOutput(out), baseAddress, bytes, startOffset, count);
	}

	/**
	 * Erzeugt ein neues HexDumper-Objekt, das bei der Ausgabe über {@link #dump dump(...)}
	 * den PrintStream {@link System#out} benutzt.
	 */
	public HexDumper() {
		this(System.out);
	}

	/**
	 * Erzeugt ein neues HexDumper-Objekt, das zur Ausgabe über {@link #dump dump(...)}
	 * das angegebene PrintStream-Objekt benutzt.
	 * @param out  PrintStream-Objekt, das bei der Ausgabe mit den Funktionen
	 */
	public HexDumper(PrintStream out) {
		_out= new StreamOutput(out);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf den
	 * PrintStream aus, der diesem HexDumper-Objekt im Konstruktor zugeordnet
	 * wurde.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 */
	public void dump(byte[] bytes) {
		dump(_out, 0, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf den
	 * PrintStream aus, der diesem HexDumper-Objekt im Konstruktor zugeordnet
	 * wurde.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 */
	public void dump(byte[] bytes, int startOffset, int count) {
		dump(_out, 0, bytes, startOffset, count);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf den
	 * PrintStream aus, der diesem HexDumper-Objekt im Konstruktor zugeordnet
	 * wurde.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 */
	public void dump(int baseAddress, byte[] bytes) {
		dump(_out, baseAddress, bytes, 0, -1);
	}

	/**
	 * Konvertiert ein Byte-Array in ein Hex-Dump und gibt das Ergebnis auf den
	 * PrintStream aus, der diesem HexDumper-Objekt im Konstruktor zugeordnet
	 * wurde.
	 * @param baseAddress  Offset für die Addressausgaben. Der angegebene Wert
	 *                     wird als Adresse des ersten Bytes im Byte-Array
	 *                     interpretiert.
	 * @param bytes  Array mit zu konvertierenden Bytes.
	 * @param startOffset  Index des ersten zu konvertierenden Bytes im Array.
	 * @param count  Anzahl der zu konvertierenden Bytes.
	 */
	public void dump(int baseAddress, byte[] bytes, int startOffset, int count) {
		dump(_out, baseAddress, bytes, startOffset, count);
	}


	/**
	 * Konvertiert ein Int-Array in ein Byte-Array.
	 * Dabei werden int-Werte zwischen 0 und 255 in entsprechende Werte eines
	 * Bytes konvertiert (0 bis 127 und -128 bis -1). Andere Werte führen
	 * zu einer RunTimeException
	 * @param ints  Zu konvertierendes int-Array mit Werten zwischen 0 und 255.
	 * @return  byte-Array mit entsprechenden Werten im Bereich 0 bis 127 und -128 bis -1.
	 * @throws IllegalArgumentException  Wenn im zu konvertierenden int-Array Werte
	 *         außerhalb des Bereich 0-255 vorkommen.
	 */
	public static byte[] toBytes(int[] ints) {
		int length= ints.length;
		byte[] bytes= new byte[length];
		for(int i=0; i < length; ++i) {
			int val= ints[i];
			if((val < 0) || (val >255)) throw new IllegalArgumentException("Wert am Index " + i + " liegt nicht zwischen 0 und 255 sondern ist " + val);
			bytes[i]= (byte)ints[i];
		}
		return bytes;
	}

	/**
	 * Schnittstelle, die die von der {@link HexDumper#dump} Methode verwendete Ausgabemethode definiert.
	 */
	private static interface DumpOutput {
		void println(String message);
	}

	private static class StringOutput implements DumpOutput {
		private static final String LINE_SEPARATOR= System.getProperty("line.separator");
		private static final int LINE_SEPARATOR_LENGTH= LINE_SEPARATOR.length();
		private StringBuffer _output= new StringBuffer();

		public void println(String message) {
			_output.append(message);
			_output.append(LINE_SEPARATOR);
		}

		public String toString() {
			int len= _output.length();
			if(len >= LINE_SEPARATOR_LENGTH) len-= LINE_SEPARATOR_LENGTH;
			return _output.substring(0,len);
		}
	}

	private static class StreamOutput implements DumpOutput {
		private final PrintStream _output;

		public StreamOutput(PrintStream out) {
			_output= out;
		}

		public void println(String message) {
			_output.println(message);
		}
	}

	private final DumpOutput _out;

	private final static char[] DIGITS= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private static void dump(DumpOutput out, int baseAddress, byte[] bytes, int startOffset, int count) {
		if(bytes==null) {
			out.println("<null>");
			return;
		}
		// Negative Werte von count bedeuten:  -1 bis zum letzten Byte, -2 bis zum vorletzten Byte ...
		if(count < 0) count = bytes.length + count + 1 - startOffset;
		int arrayIndex= startOffset;
		int address= baseAddress + arrayIndex;
		int startAlign= address % 16;
		int rowAddress= address;
		char[] addressChars= {'0', '1', '2', '3', '4', '5', '6', '7', ':', ' '};
		StringBuffer hex= new StringBuffer();
		StringBuffer chars= new StringBuffer();
		StringBuffer line= new StringBuffer();
		while(--startAlign>=0) {
			if(startAlign%4 == 0) hex.append(' ');
			hex.append("   ");
			chars.append(' ');
		}
		while(--count>=0) {
			int b= bytes[arrayIndex] & 0xff;
			if(address%4 == 0) hex.append(' ');
			hex.append(DIGITS[b>>4]).append(DIGITS[b&0xf]).append(' ');
			if( (b>=0x20 && b<=0x7e) || (b>=0xa0 && b<=0xff) ) chars.append((char)b);
			else chars.append('.');
			++address;
			++arrayIndex;
			if((address % 16) == 0 || count==0) {
				if((address % 16) != 0) {
					int endAlign= address % 16;
					for(int i= endAlign; i<16; ++i) {
						if(i%4 == 0) hex.append(' ');
						hex.append("   ");
					}
				}
				for(int nibble=7; nibble>=0; --nibble) {
					addressChars[nibble]= DIGITS[rowAddress & 0xf];
					rowAddress >>>= 4;
				}
				line.append(addressChars).append(hex).append("   ").append(chars);
				out.println(line.toString());
				line.setLength(0);
				hex.setLength(0);
				chars.setLength(0);
				rowAddress= address;
			}
		}
	}

}
