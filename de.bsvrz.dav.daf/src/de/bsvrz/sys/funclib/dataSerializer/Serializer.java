/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.sys.funclib.dataSerializer;


import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Schnittstelle zum Serialisieren von Datens�tzen. Konkrete Objekte zum Serialisieren k�nnen mit den verschiedenen
 * Methoden der Klasse {@link SerializingFactory} erzeugt werden. Dabei kann der Ausgabe-Stream f�r die Serialisierung
 * vorgegeben werden.
 * <p/>
 * Mit den verschiedenen <code>write</code>-Methoden k�nnen primitive Datentypen, Referenzen auf {@link SystemObject
 * System-Objekte} und ganze {@link Data Datens�tze} auf einen OutputStream serialisiert werden.
 * <p/>
 * �ber die {@link #getVersion} Methode kann die Version eines konkreten Serialisieres abgefragt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see Deserializer
 */
public interface Serializer {

	/**
	 * Bestimmt die Version des konkreten Serialisierers.
	 *
	 * @return Version des Serialisierers.
	 */
	int getVersion();

	/**
	 * Bestimmt den f�r die Serialisierung zu verwendenden Ausgabe-Stream.
	 *
	 * @return F�r die Serialisierung zu verwendenden Ausgabe-Stream.
	 */
	OutputStream getOutputStream();

	/**
	 * Setzt den zu verwendenden Ausgabe-Stream.
	 * @param outputStream Zu verwendender Ausgabe-Stream
	 */
	void setOutputStream(OutputStream outputStream);

	/**
	 * Serialisiert einen Datensatz in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param data Der zu serialisierende Datensatz.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	void writeData(Data data) throws IOException;

	/**
	 * Serialisiert einen <code>boolean</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler beim Schreiben auf den Ausgabe-Strom auftritt.
	 */
	void writeBoolean(boolean value) throws IOException;

	/**
	 * Serialisiert die id eines Systemobjekts in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param object Das Systemobjekt dessen id serialisiert werden soll.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	void writeObjectReference(SystemObject object) throws IOException;

	/**
	 * Serialisiert einen <code>long</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeLong(long value) throws IOException;

	/**
	 * Serialisiert einen <code>int</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeInt(int value) throws IOException;

	/**
	 * Serialisiert einen <code>short</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeShort(int value) throws IOException;

	/**
	 * Serialisiert einen <code>byte</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeByte(int value) throws IOException;

	/**
	 * Serialisiert einen <code>double</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeDouble(double value) throws IOException;

	/**
	 * Serialisiert einen <code>float</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeFloat(float value) throws IOException;

	/**
	 * Serialisiert einen <code>String</code>-Wert mit einer maximalL�nge von 65535 in einen Bytestrom und schreibt diesen
	 * auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException              Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 * @throws IllegalArgumentException Wenn die L�nge des Strings gr��er als 65535 Zeichen ist.
	 */
	public void writeString(String value) throws IOException;

	/**
	 * Serialisiert einen <code>String</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream. Es ist zu
	 * beachten, dass beim Deserialiseren die gleiche Maximalgr��e wie beim serialisieren angegeben wird.
	 *
	 * @param value     Der zu serialisierende Wert.
	 * @param maxLength Maximale L�nge des zu serialisierenden Strings oder <code>0</code> wenn keine Begrenzung vorgegeben
	 *                  werden kann.
	 * @throws IOException              Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 * @throws IllegalArgumentException Wenn die L�nge des Strings gr��er als die angegebene Maximall�nge ist.
	 */
	public void writeString(String value, int maxLength) throws IOException;

	/**
	 * Schreibt ein Byte-Array auf den Ausgabe-Stream. Es ist zu beachten, dass die Gr��e des Arrays nicht implizit
	 * serialisiert wird und beim Deserialisieren angegeben werden muss.
	 *
	 * @param bytes Zu schreibendes Byte-Array
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	void writeBytes(byte[] bytes) throws IOException;

	/**
	 * Schreibt ein Folge von Bytes aus einem Byte-Array auf den Ausgabe-Stream. Es ist zu beachten, dass die Anzahl der
	 * Bytes nicht implizit serialisiert wird und beim Deserialisieren passend angegeben werden muss.
	 *
	 * @param bytes Byte-Array mit den zu schreibenden Bytes
	 * @param offset Start-Offset des ersten zu schreibenden Bytes im Array
	 * @param length Anzahl der zu schreibenden Bytes
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	void writeBytes(byte[] bytes, int offset, int length) throws IOException;
}
