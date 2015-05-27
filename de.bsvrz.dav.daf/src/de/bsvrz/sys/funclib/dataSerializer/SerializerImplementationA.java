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
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Implementierung eines Serialisierers zum serialisieren von Datens�tzen. Die Klasse ist nicht �ffentlich zug�nglich.
 * Ein Objekt dieser Klasse kann mit der Methode {@link SerializingFactory#createSerializer} erzeugt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13421 $
 */
final class SerializerImplementationA implements Serializer {
	private final int _version;
	private OutputStream _outputStream;

	/**
	 * Erzeugt ein neues Serialisierungsobjekt mit der gew�nschten Version.
	 *
	 * @param version      Gew�nschte Version der Deserialisierung.
	 * @param outputStream Ausgabe-Stream, der beim serialisieren zu verwenden ist.
	 * @throws RuntimeException Wenn die gew�nschte Version nicht durch diese Klasse implementiert werden kann.
	 */
	SerializerImplementationA(final int version, final OutputStream outputStream) throws RuntimeException {
		_version = version;
		_outputStream = outputStream;
		if(version < 2 || version > 3) {
			throw new RuntimeException("SerializerImplementationA implementiert nicht Version " + version);
		}
	}

	/**
	 * Bestimmt den f�r die Serialisierung zu verwendenden Ausgabe-Stream.
	 *
	 * @return F�r die Serialisierung zu verwendenden Ausgabe-Stream.
	 */
	public OutputStream getOutputStream() {
		return _outputStream;
	}

	/**
	 * Setzt den zu verwendenden Ausgabe-Stream.
	 *
	 * @param outputStream Zu verwendender Ausgabe-Stream
	 */
	public void setOutputStream(OutputStream outputStream) {
		_outputStream = outputStream;
	}

	/**
	 * Bestimmt die Version des konkreten Serialisierers.
	 *
	 * @return Version des Serialisierers.
	 */
	public int getVersion() {
		return _version;
	}

	/**
	 * Serialisiert einen Datensatz in einen Bytestrom und schreibt diesen auf den angegebenen Ausgabe-Stream.
	 * AttributListen und Arrays werden durch Serialisierung der enthalten Attribute serialisiert. Bei Arrays variabler
	 * L�nge wird die L�nge vorweg serialisiert, und zwar je nach maximaler Anzahl der Elemente in 1, 2 oder 4 Bytes als
	 * vorzeichenloser Wert.
	 *
	 * @param data Der zu serialisierende Datensatz.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeData(final Data data) throws IOException {
		if(data.isPlain()) {
			try {
				final AttributeType att = data.getAttributeType();
				if(att instanceof IntegerAttributeType) {
					final IntegerAttributeType integerAtt = (IntegerAttributeType) att;
					final Data.NumberValue unscaledValue = data.asUnscaledValue();
					switch(integerAtt.getByteCount()) {
						case 1:
							writeByte(unscaledValue.byteValue());
							break;
						case 2:
							writeShort(data.asUnscaledValue().shortValue());
							break;
						case 4:
							writeInt(data.asUnscaledValue().intValue());
							break;
						case 8:
							writeLong(data.asUnscaledValue().longValue());
							break;
						default:
							throw new RuntimeException("Ganzzahlattribut mit ung�ltiger Byte-Anzahl: " + integerAtt.getNameOrPidOrId());
					}
				}
				else if(att instanceof ReferenceAttributeType) {
					final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) att;
					if(_version >= 3 && referenceAttributeType.getReferenceType() == ReferenceType.ASSOCIATION) {
						final String pid;
						final SystemObject systemObject = data.asReferenceValue().getSystemObject();
						if(systemObject != null) {
							pid = systemObject.getPid();
							if(pid.length() == 0) {
								throw new IllegalArgumentException(
										"Serialisierung des Attributs " + data.getName() + " kann nicht durchgef�hrt werden, weil"
												+ "das referenzierte Objekt keine Pid hat und als Referenzierungsart Assoziation festgelegt ist"
								);
							}
						}
						else {
							// Bei nicht aufl�sbaren Referenzen wird die urspr�ngliche Pid (falls vorhanden) eingetragen, sonst ein Leerstring f�r undefiniert
							pid = data.asReferenceValue().getSystemObjectPid();
						}
						writeString(pid, 255);
					}
					else {
						//writeObjectReference(data.asReferenceValue().getSystemObject());
						long objectId = data.asReferenceValue().getId();
						writeLong(objectId);
					}
				}
				else if(att instanceof TimeAttributeType) {
					final TimeAttributeType timeAtt = (TimeAttributeType) att;
					if(timeAtt.getAccuracy() == TimeAttributeType.MILLISECONDS) {
						writeLong(data.asTimeValue().getMillis());
					}
					else {
						writeInt((int) data.asTimeValue().getSeconds());
					}
				}
				else if(att instanceof StringAttributeType) {
					final StringAttributeType stringAtt = (StringAttributeType) att;
					writeString(data.asTextValue().getValueText(), stringAtt.getMaxLength());
				}
				else if(att instanceof DoubleAttributeType) {
					final DoubleAttributeType doubleAtt = (DoubleAttributeType) att;
					if(doubleAtt.getAccuracy() == DoubleAttributeType.DOUBLE) {
						writeDouble(data.asUnscaledValue().doubleValue());
					}
					else {
						writeFloat(data.asUnscaledValue().floatValue());
					}
				}
				else {
					throw new RuntimeException("Serialisierung einer unbekannten Attributart nicht m�glich");
				}
			}
			catch(ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			if(data.isArray()) {
				final Data.Array array = data.asArray();
				final int arrayLength = array.getLength();
				final int maxCount = array.getMaxCount();
				if(array.isCountVariable()) {
					if(maxCount <= 0 || maxCount > 65535) {
						writeInt(arrayLength);
					}
					else if(maxCount > 255) {
						writeShort(arrayLength);
					}
					else {
						writeByte(arrayLength);
					}
				}
				else {
					if(arrayLength != maxCount) {
						throw new RuntimeException(
								"L�nge des Arrays im Attribut " + data.getName() + " ist " + arrayLength + ", " +
										"aber es sollte die L�nge " + maxCount + " haben"
						);
					}
				}
			}
			final Iterator iterator = data.iterator();
			while(iterator.hasNext()) {
				final Data subData = (Data) iterator.next();
				writeData(subData);
			}
		}
	}

	/**
	 * Serialisiert die id eines Systemobjekts in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param object Das Systemobjekt dessen id serialisiert werden soll.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeObjectReference(final SystemObject object) throws IOException {
		if(object == null) {
			writeLong(0);
		}
		else {
			writeLong(object.getId());
		}
	}

	/**
	 * Serialisiert einen <code>long</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeLong(final long value) throws IOException {
		writeInt((int) (value >>> 32));
		writeInt((int) (value & 0xffffffff));
	}

	/**
	 * Serialisiert einen <code>int</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeInt(final int value) throws IOException {
		_outputStream.write((value >>> 24) & 0xff);
		_outputStream.write((value >>> 16) & 0xff);
		_outputStream.write((value >>> 8) & 0xff);
		_outputStream.write(value & 0xff);
	}

	/**
	 * Serialisiert einen <code>short</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeShort(final int value) throws IOException {
		_outputStream.write((value >>> 8) & 0xff);
		_outputStream.write(value & 0xff);
	}

	/**
	 * Serialisiert einen <code>byte</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeByte(final int value) throws IOException {
		_outputStream.write(value);
	}

	/**
	 * Serialisiert einen <code>boolean</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler beim Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeBoolean(final boolean value) throws IOException {
		_outputStream.write((value ? 1 : 0));
	}

	/**
	 * Serialisiert einen <code>double</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream. Die Werte
	 * werden entsprechend dem IEEE 754 floating-point "double format" serialisiert.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeDouble(final double value) throws IOException {
		writeLong(Double.doubleToLongBits(value));
	}

	/**
	 * Serialisiert einen <code>float</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream. Die Werte
	 * werden entsprechend dem IEEE 754 floating-point "single format" serialisiert.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeFloat(final float value) throws IOException {
		writeInt(Float.floatToIntBits(value));
	}

	/**
	 * Serialisiert einen <code>String</code>-Wert mit einer MaximalL�nge von 65535 in einen Bytestrom und schreibt diesen
	 * auf den Ausgabe-Stream.
	 *
	 * @param value Der zu serialisierende Wert.
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeString(final String value) throws IOException {
		writeString(value, 65535);
	}

	/**
	 * Serialisiert einen <code>String</code>-Wert in einen Bytestrom und schreibt diesen auf den Ausgabe-Stream. Strings werden in ISO-8859-1 kodiert und vorweg
	 * wird die L�nge des Strings serialisiert und zwar je nach maximaler Stringl�nge in 1, 2, oder 4 Bytes als vorzeichenloser Wert.
	 *
	 * @param value     Der zu serialisierende Wert.
	 * @param maxLength Maximale L�nge des zu serialisierenden Strings oder <code>0</code> wenn keine Begrenzung vorgegeben werden kann.
	 * @throws IOException              Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 * @throws IllegalArgumentException Wenn die L�nge des Strings gr��er als die angegebene Maximall�nge ist.
	 */
	public void writeString(final String value, final int maxLength) throws IOException {
		final int length = value.length();
		if(maxLength <= 0 || maxLength > 65535) {
			writeInt(length);
		}
		else {
			if(length > maxLength) {
				throw new IllegalArgumentException(
						"L�nge " + length + " des zu serialisierenden Strings '" + value + "' ist gr��er als das zul�ssige Maximum " + maxLength
				);
			}
			if(maxLength > 255) {
				writeShort(length);
			}
			else {
				writeByte(length);
			}
		}
		final byte[] bytes = value.getBytes("ISO-8859-1");
		if(length != bytes.length) {
			throw new RuntimeException("Stringl�nge ungleich kodierter Stringl�nge: " + value);
		}
		_outputStream.write(bytes);
	}


	/**
	 * Schreibt ein Byte-Array auf den Ausgabe-Stream. Es ist zu beachten, dass die Gr��e des Arrays nicht implizit
	 * serialisiert wird und beim Deserialisieren angegeben werden muss.
	 *
	 * @param bytes Zu schreibendes Byte-Array
	 * @throws IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeBytes(byte[] bytes) throws IOException {
		_outputStream.write(bytes);
	}

	/**
	 * Schreibt ein Folge von Bytes aus einem Byte-Array auf den Ausgabe-Stream. Es ist zu beachten, dass die Anzahl der
	 * Bytes nicht implizit serialisiert wird und beim Deserialisieren passend angegeben werden muss.
	 *
	 * @param bytes  Byte-Array mit den zu schreibenden Bytes
	 * @param offset Start-Offset des ersten zu schreibenden Bytes im Array
	 * @param length Anzahl der zu schreibenden Bytes
	 * @throws java.io.IOException Wenn ein I/O Fehler bei Schreiben auf den Ausgabe-Stream auftritt.
	 */
	public void writeBytes(byte[] bytes, int offset, int length) throws IOException {
		_outputStream.write(bytes, offset, length);
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts f�r Debug-Zwecke.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "SerializerImplementationA{" +
				"_version=" + _version +
				", _outputStream=" + _outputStream +
				'}';
	}

}

