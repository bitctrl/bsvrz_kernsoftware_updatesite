/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.sys.funclib.dataSerializer;


import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.ObjectLookup;

import java.io.IOException;
import java.io.InputStream;

/**
 * Schnittstelle zum Deserialisieren von Datensätzen. Konkrete Objekte zum Deserialisieren können mit den verschiedenen
 * Methoden der Klasse {@link SerializingFactory} erzeugt werden. Dabei kann der Eingabe-Stream für die Deserialisierung
 * vorgegeben werden.
 * <p>
 * Mit den verschiedenen <code>read</code>-Methoden können primitive Datentypen, Referenzen auf {@link SystemObject
 * System-Objekte} und ganze {@link Data Datensätze} von einem {@link InputStream} eingelesen und deserialisiert
 * werden.
 * <p>
 * Über die {@link #getVersion} Methode kann die Version eines konkreten Deserialisieres abgefragt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see Serializer
 */
public interface Deserializer {

	/**
	 * Bestimmt die Version des konkreten Deserialisierers.
	 *
	 * @return Version des Deserialisierers.
	 */
	int getVersion();

	/**
	 * Bestimmt den bei der Deserialisierung zu verwendenden Eingabe-Stream.
	 *
	 * @return Bei der Deserialisierung zu verwendender Eingabe-Stream.
	 */
	InputStream getInputStream();

	/**
	 * Setzt den zu verwendenden Eingabe-Stream.
	 * @param inputStream Zu verwendender InputStream
	 */
	void setInputStream(InputStream inputStream);

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param data Neuer Datensatz, der mit der Attributgruppe der erwarteten Daten initialisiert wurde.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 * @see de.bsvrz.dav.daf.main.ClientDavConnection#createData
	 */
	void readData(Data data) throws IOException;

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param atg Attributgruppe des einzulesenden Datensatzes.
	 * @return Eingelesener Datensatz
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	Data readData(AttributeGroup atg) throws IOException;

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param data Neuer Datensatz, der mit der Attributgruppe der erwarteten Daten initialisiert wurde.
	 * @param dataModel Datenmodell mit dessen Hilfe Objektreferenzen aufgelöst werden.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 * @see de.bsvrz.dav.daf.main.ClientDavConnection#createData
	 */
	void readData(Data data, ObjectLookup dataModel) throws IOException;

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param atg Attributgruppe des einzulesenden Datensatzes.
	 * @param dataModel Datenmodell mit dessen Hilfe Objektreferenzen aufgelöst werden.
	 * @return Eingelesener Datensatz
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	Data readData(AttributeGroup atg, ObjectLookup dataModel) throws IOException;

	/**
	 * Liest und deserialisiert einen <code>boolean</code>-Wert vom Eingabe-Strom dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public boolean readBoolean() throws IOException;

	/**
	 * Liest und deserialisiert eine Referenz auf ein Systemobjekt vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param dataModel Datenmodell mit dessen Hilfe Objektreferenzen aufgelöst werden.
	 * @return Das referenzierte Systemobjekt oder <code>null</code>, wenn das referenzierte Objekt nicht bestimmt werden
	 *         kann.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public SystemObject readObjectReference(ObjectLookup dataModel) throws IOException;

	/**
	 * Liest und deserialisiert einen <code>byte</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public byte readByte() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>byte</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readUnsignedByte() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>short</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public short readShort() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>short</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readUnsignedShort() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>int</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readInt() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>int</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public long readUnsignedInt() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>long</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public long readLong() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>float</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public float readFloat() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>double</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public double readDouble() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>String</code>-Wert mit einer maximalen Länge von 65535 vom Eingabe-Stream
	 * dieses Deserialisierers.
	 *
	 * @return Der eingelesene String.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public String readString() throws IOException;

	/**
	 * Liest und deserialisiert einen <code>String</code>-Wert mit einer vorgegebenen Maximal-Länge vom Eingabe-Stream
	 * dieses Deserialisierers. Es ist zu beachten, dass beim Deserialiseren die gleiche Maximalgröße wie beim
	 * Serialisieren angegeben wird.
	 *
	 * @param maxLength Maximale Länge des einzulesenden Strings oder <code>0</code> wenn keine Begrenzung vorgegeben
	 *                  werden kann.
	 * @return Der eingelesene String.
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public String readString(int maxLength) throws IOException;

	/**
	 * Liest ein Byte-Array mit vorgegebener Länge vom Eingabe-Stream dieses Deserialisierers. Es ist zu beachten, das als
	 * Länge exakt die Größe des entsprechenden serialisierten Arrays angegeben werden muss.
	 *
	 * @param length Länge des einzulesenden Byte-Arrays
	 * @return Das eingelesene Byte-Array
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public byte[] readBytes(int length) throws IOException;

	/**
	 * Liest eine vorgegebene Anzahl von Bytes vom Eingabe-Stream dieses Deserialisierers ein und speichert diese an
	 * einem vorgegebenen Offset in ein vorhandenes Byte-Array. Es ist zu beachten, das als
	 * Länge exakt die Größe des entsprechenden serialisierten Arrays angegeben werden muss.
	 *
	 * @param buffer Byte-Array in das die eingelesenen Bytes gespeichert werden sollen.
	 * @param offset Startposition im Byte-Array ab der die eingelesenen Bytes gespeichert werden sollen.
	 * @param length Anzahl der einzulesenden Bytes
	 * @throws IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public void readBytes(byte[] buffer, int offset, int length) throws IOException;


}


