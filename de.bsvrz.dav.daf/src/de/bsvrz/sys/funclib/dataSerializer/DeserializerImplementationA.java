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


import de.bsvrz.dav.daf.communication.dataRepresentation.ArrayRelaxedRangeCheckSupport;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValueDataFactory;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeHelper;
import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementierung eines Deserialisierers zum deserialisieren von Datensätzen. Die Klasse ist nicht öffentlich
 * zugänglich. Ein Objekt dieser Klasse kann mit der Methode {@link SerializingFactory#createDeserializer}
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
final class DeserializerImplementationA implements Deserializer {
	/** DebugLogger für Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();
	private final int _version;
	private InputStream _inputStream;

	/**
	 * Erzeugt ein neues Deserialisierungsobjekt mit der gewünschten Version.
	 *
	 * @param version     Gewünschte Version
	 * @param inputStream Eingabe-Stream, der beim deserialisieren zu verwenden ist.
	 * @throws RuntimeException Wenn die gewünschte Version nicht durch diese Klasse implementiert werden kann.
	 */
	DeserializerImplementationA(final int version, final InputStream inputStream) throws RuntimeException {
		_version = version;
		_inputStream = inputStream;
		if (version < 2 || version > 3) throw new RuntimeException("DeserializerImplementationA implementiert nicht Version " + version);
	}

	/**
	 * Bestimmt den bei der Deserialisierung zu verwendenden Eingabe-Stream.
	 *
	 * @return Bei der Deserialisierung zu verwendender Eingabe-Stream.
	 */
	public InputStream getInputStream() {
		return _inputStream;
	}

	/**
	 * Setzt den zu verwendenden Eingabe-Stream.
	 *
	 * @param inputStream Zu verwendender Eingabe-Stream
	 */
	public void setInputStream(InputStream inputStream) {
		_inputStream = inputStream;
	}

	/**
	 * Bestimmt die Version des konkreten Deserialisierers.
	 *
	 * @return Version des Deserialisierers.
	 */
	public int getVersion() {
		return _version;
	}

	/**
	 * Deserialisiert einen Datensatz aus dem Eingabe-Stream.
	 *
	 * @param data Neuer Datensatz, der mit der AttributGruppe der erwarteten Daten initialisiert wurde.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 * @see ClientDavConnection#createData
	 */
	public void readData(final Data data) throws EOFException, IOException {
		readData(data, data.getAttributeType().getDataModel());
	}

	/**
	 * Deserialisiert einen Datensatz aus dem Eingabe-Stream.
	 *
	 * @param data Neuer Datensatz, der mit der AttributGruppe der erwarteten Daten initialisiert wurde.
	 * @param objectLookup Wird für die Auflösung von Objektreferenzen benutzt.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 * @see ClientDavConnection#createData
	 */
	public void readData(final Data data, ObjectLookup objectLookup) throws EOFException, IOException {
		if (data.isPlain()) {
				final AttributeType att = data.getAttributeType();
				if (att instanceof IntegerAttributeType) {
					final IntegerAttributeType integerAtt = (IntegerAttributeType) att;
					final Data.NumberValue unscaledValue = data.asUnscaledValue();
					switch (integerAtt.getByteCount()) {
						case 1:
							unscaledValue.set(readByte());
							break;
						case 2:
							unscaledValue.set(readShort());
							break;
						case 4:
							unscaledValue.set(readInt());
							break;
						case 8:
							unscaledValue.set(readLong());
							break;
						default:
							throw new RuntimeException("Ganzzahlattribut mit ungültiger Byte-Anzahl: " + integerAtt.getNameOrPidOrId());
					}
				} else if (att instanceof ReferenceAttributeType) {
					final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)att;
					if(_version >= 3 && referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION) {
						final String pid = readString(255);
						try {
							data.asReferenceValue().setSystemObjectPid(pid, objectLookup);
						}
						catch(RuntimeException e) {
							throw new RuntimeException("Am Referenzattribut " + data.getName() + " konnte die Pid " + pid + " nicht aufgelöst werden", e);
						}
					}
					else {
						final SystemObject systemObject = readObjectReference(objectLookup);
						data.asReferenceValue().setSystemObject(systemObject);
					}
				} else if (att instanceof TimeAttributeType) {
					final TimeAttributeType timeAtt = (TimeAttributeType) att;
					if (timeAtt.getAccuracy() == TimeAttributeType.MILLISECONDS) {
						data.asTimeValue().setMillis(readLong());
					} else {
						data.asTimeValue().setSeconds(readInt());
					}
				} else if (att instanceof StringAttributeType) {
					final StringAttributeType stringAtt = (StringAttributeType) att;
					data.asTextValue().setText(readString(stringAtt.getMaxLength()));
				} else if (att instanceof DoubleAttributeType) {
					final DoubleAttributeType doubleAtt = (DoubleAttributeType) att;
					if (doubleAtt.getAccuracy() == DoubleAttributeType.DOUBLE) {
						data.asUnscaledValue().set(readDouble());
					} else {
						data.asUnscaledValue().set(readFloat());
					}
				} else {
					throw new RuntimeException("Deserialisierung einer unbekannten Attributart nicht möglich");
				}
		} else {
			if (data.isArray()) {
				final Data.Array array = data.asArray();
				final int maxCount = array.getMaxCount();
				if (array.isCountVariable()) {
					int newLength;
					if (maxCount <= 0 || maxCount > 65535) {
						newLength = readInt();
					} else if (maxCount > 255) {
						newLength = readUnsignedShort();
					} else {
						newLength = readUnsignedByte();
					}
					if(array instanceof ArrayRelaxedRangeCheckSupport) {
						((ArrayRelaxedRangeCheckSupport) array).setLengthRelaxedRangeCheck(newLength);
					}
					else {
						array.setLength(newLength);
					}
				} else {
					array.setLength(maxCount);
				}
			}
			for(final Data subData : data) {
				readData(subData, objectLookup);
			}
		}

	}

	/**
	 * Liest und deserialisiert einen <code>byte</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public byte readByte() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		if (byte1 < 0) throw new EOFException();
		return (byte) byte1;
	}

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param atg Attributgruppe des einzulesenden Datensatzes.
	 *
	 * @return Eingelesener Datensatz
	 *
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public Data readData(final AttributeGroup atg) throws EOFException, IOException {
		return readData(atg, atg.getDataModel());
	}

	/**
	 * Liest und deserialisiert einen Datensatz aus dem Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param atg          Attributgruppe des einzulesenden Datensatzes.
	 * @param objectLookup Wird für die Auflösung von Objektreferenzen benutzt.
	 *
	 * @return Eingelesener Datensatz
	 *
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public Data readData(final AttributeGroup atg, ObjectLookup objectLookup) throws EOFException, IOException {
		final Data data = AttributeBaseValueDataFactory.createAdapter(atg, AttributeHelper.getAttributesValues(atg));
		readData(data, objectLookup);
		return data;
	}

	/**
	 * Liest und deserialisiert einen <code>boolean</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesens erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public boolean readBoolean() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		if (byte1 < 0) throw new EOFException();
		if (byte1 > 1) throw new IllegalStateException("Der eingelesene Wert ist kein boolean-Wert.");
		return (boolean) (byte1 == 0 ? false : true);
	}

	/**
	 * Liest und deserialisiert einen <code>double</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public double readDouble() throws EOFException, IOException {
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Liest und deserialisiert einen <code>float</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public float readFloat() throws EOFException, IOException {
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Liest und deserialisiert einen <code>int</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readInt() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		int byte2 = _inputStream.read();
		int byte3 = _inputStream.read();
		int byte4 = _inputStream.read();
		if (byte1 < 0 || byte2 < 0 || byte3 < 0 || byte4 < 0) throw new EOFException();
		return (byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4;
	}

	/**
	 * Liest und deserialisiert einen <code>long</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public long readLong() throws EOFException, IOException {
		return (((long) readInt()) << 32) | (((long) readInt()) & 0xffffffffL);
	}

	/**
	 * Liest und deserialisiert einen <code>short</code>-Wert vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @return Der eingelesene Wert.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public short readShort() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		int byte2 = _inputStream.read();
		if (byte1 < 0 || byte2 < 0) throw new EOFException();
		return (short) ((byte1 << 8) | byte2);
	}

	/**
	 * Liest und deserialisiert einen <code>String</code>-Wert mit einer maximalen Länge von 65535 vom Eingabe-Stream
	 * dieses Deserialisierers.
	 *
	 * @return Der eingelesene String.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public String readString() throws EOFException, IOException {
		return readString(65535);
	}

	/**
	 * Liest und deserialisiert einen <code>String</code>-Wert mit einer vorgegebenen Maximal-Länge vom Eingabe-Stream
	 * dieses Deserialisierers. Es ist zu beachten, dass beim deserialiseren die gleiche Maximalgröße wie beim
	 * serialisieren angegeben wird.
	 *
	 * @param maxLength Maximale Länge des zu serialisierenden Strings oder <code>0</code> wenn keine Begrenzung vorgegeben
	 *                  werden kann.
	 * @return Der eingelesene String.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public String readString(final int maxLength) throws EOFException, IOException {
		final int length;
		if (maxLength <= 0 || maxLength > 65535) {
			length = readInt();
		} else if (maxLength > 255) {
			length = readUnsignedShort();
		} else {
			length = readUnsignedByte();
		}
		if(length == 0) return "";
		final byte[] bytes = new byte[length];
		if (length > 0) {
			readBytes(bytes, 0, length);
		}
		return new String(bytes, "ISO-8859-1");
	}

	/**
	 * Liest und deserialisiert eine Referenz auf ein Systemobjekt vom Eingabe-Stream dieses Deserialisierers.
	 *
	 * @param objectLookup Wird für die Auflösung von Objektreferenzen benutzt.
	 * @return Das referenzierte Systemobjekt oder <code>null</code>, wenn das referenzierte Objekt nicht bestimmt werden
	 *         kann.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public SystemObject readObjectReference(final ObjectLookup objectLookup) throws EOFException, IOException {
		final long id = readLong();
		if (id != 0) {
			final SystemObject systemObject = objectLookup.getObject(id);
			if(systemObject == null) {
				_debug.warning("Das referenzierte Objekt mit der ID " + id + " konnte nicht gefunden werden");
			}
			return systemObject;
		}
		return null;
	}

	/**
	 * Liest und deserialisiert einen <code>byte</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readUnsignedByte() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		if (byte1 < 0) throw new EOFException();
		return byte1;
	}

	/**
	 * Liest und deserialisiert einen <code>int</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public long readUnsignedInt() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		int byte2 = _inputStream.read();
		int byte3 = _inputStream.read();
		int byte4 = _inputStream.read();
		if (byte1 < 0 || byte2 < 0 || byte3 < 0 || byte4 < 0) throw new EOFException();
		return ((long) ((byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4)) & 0xffffffffL;
	}

	/**
	 * Liest und deserialisiert einen <code>short</code>-Wert vom Eingabe-Stream dieses Deserialisierers und interpretiert
	 * den Wert als vorzeichenlose Zahl.
	 *
	 * @return Der eingelesene Wert als vorzeichenlose Zahl.
	 * @throws EOFException Wenn das Ende des Eingabe-Streams während des Lesen erkannt wurde.
	 * @throws IOException  Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public int readUnsignedShort() throws EOFException, IOException {
		int byte1 = _inputStream.read();
		int byte2 = _inputStream.read();
		if (byte1 < 0 || byte2 < 0) throw new EOFException();
		return (byte1 << 8) | byte2;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "DeserializerImplementationA{" +
		       "_version=" + _version +
		       ", _inputStream=" + _inputStream +
		       '}';
	}

	/**
	 * Liest ein Byte-Array mit vorgegebener Länge vom Eingabe-Stream dieses Deserialisierers. Es ist zu beachten, das als
	 * Länge exakt die Größe des entsprechenden serialisierten Arrays angegeben werden muss.
	 *
	 * @param length Länge des einzulesenden Byte-Arrays
	 * @return Das eingelesene Byte-Array
	 * @throws java.io.IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public byte[] readBytes(int length) throws IOException {
		byte[] bytes = new byte[length];
		readBytes(bytes, 0, length);
		return bytes;
	}

	/**
	 * Liest eine vorgegebene Anzahl von Bytes vom Eingabe-Stream dieses Deserialisierers ein und speichert diese an einem
	 * vorgegebenen Offset in ein vorhandenes Byte-Array. Es ist zu beachten, das als Länge exakt die Größe des
	 * entsprechenden serialisierten Arrays angegeben werden muss.
	 *
	 * @param buffer Byte-Array in das die eingelesenen Bytes gespeichert werden sollen.
	 * @param offset Startposition im Byte-Array ab der die eingelesenen Bytes gespeichert werden sollen.
	 * @param length Anzahl der einzulesenden Bytes
	 * @throws java.io.IOException Wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public void readBytes(byte[] buffer, int offset, int length) throws IOException {
		if (length > 0) {
			int read;
			while(true) {
				read = _inputStream.read(buffer, offset, length);
				if(read == -1) throw new EOFException("Ende des Streams mitten im erwarteten Byte-Array");
				length -= read;
				offset += read;
				if(length == 0) return;
			}
		}
	}
}
