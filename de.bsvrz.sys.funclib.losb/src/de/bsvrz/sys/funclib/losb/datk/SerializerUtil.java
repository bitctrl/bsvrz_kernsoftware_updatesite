/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.datk;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.exceptions.FailureException;
import de.bsvrz.sys.funclib.losb.exceptions.LoggerException;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;
import de.bsvrz.sys.funclib.losb.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Hilfsklasse zum (De-)Serialisieren von Daten für Protokolle und Auswertungen.<br> <b>Wichtig:</b></br> Die Methoden {@link
 * #serializeIntoDataArray(Data.Array,Serializable)} und {@link #deserializeZIP(byte[])} sind <b>nicht</b> Threadsafe!
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SerializerUtil {

	/** Debug Ausgabe */
	private static final Debug debug = Debug.getLogger();

	/** Puffergröße für Ausgabestream und Array für komprimierte Daten. Für jeden Thread zur Protokollerstellung werden 2 dieser Puffer angelegt. */
	private static final int BUFFER_SIZE = 128 * 1024;

	/** Kommpressionsfaktor. 1 = sehr schnell vs 9 = sehr klein. */
	static final int COMPRESSION_LEVEL = 3;

	/** Objekt zum Komprimieren. */
	private Deflater def = new Deflater(COMPRESSION_LEVEL);

	/** Array das den komprimierten Datensatz (serialisiertes & gezipptes Objekt) enthält. */
	private byte compressedData[] = new byte[BUFFER_SIZE];

	/** Outputstream. Enthält das serialisiertes Objekt. */
	private ByteArrayOutputStream bosStatic = new ByteArrayOutputStream(BUFFER_SIZE);

	/** Anzahl Bytes die zusätzlich nach einem Reset vom Objektoutputstream vor jedes Objekt geschrieben werden. */
	private int headerOffset;

	/** Header, der jedem Objekt vorangeht. */
	private byte[] header;

	/** Führt die Serialisierung durch. */
	private ObjectOutputStream oos = null;

	/**
	 * Konstruktor. Führt Initialisierung durch.
	 *
	 * @throws FailureException Fehler bei der Initialisierung.
	 */
	public SerializerUtil() throws FailureException {
		try {
			oos = new ObjectOutputStream(bosStatic);
			oos.flush();
			header = bosStatic.toByteArray();
			//jetzt offset ausrechnen:
			oos.reset();
			bosStatic.reset();
			oos.writeByte(0);
			oos.flush();
			headerOffset = bosStatic.toByteArray().length - 1;
			oos.reset();
			bosStatic.reset();
		}
		catch(IOException e) {
			throw new FailureException(e, LoggerException.WARNING);
		}
	}

	/**
	 * Serialisiert und komprimiert ein Objekt in ein Data-Feld. Diese Methode ist nicht Threadsafe. TODO nicht komprimieren, falls komprimierte Daten >
	 * unkomprimierte Daten.
	 *
	 * @param dest Data-Feld, in das das gepackte und serialisierte Objekt geschrieben wird.
	 * @param obj  Objekt das serialisiert und gezippt wird.
	 *
	 * @throws FailureException Fehler beim serialisieren und packen.
	 */
	public void serializeIntoDataArray(Data.Array dest, Serializable obj) throws FailureException {
		byte buffer[] = compressedData;
		try {
			oos.reset();
			def.reset();
			bosStatic.reset();
			oos.write(header);
			oos.writeObject(obj);
			oos.flush();

			byte serializedObject[] = bosStatic.toByteArray();
			def.setInput(serializedObject, headerOffset, serializedObject.length - headerOffset);
			if(serializedObject.length > compressedData.length) buffer = new byte[serializedObject.length];

			def.finish();
			int compressedDataLength = def.deflate(buffer, 4, buffer.length - 4);
			writeInt(serializedObject.length, buffer);	//Länge an den Anfang des Arrays schreiben.

			if(!def.finished()) {
				throw new FailureException("Komprimierte Daten zu groß.", LoggerException.WARNING);
			}
			else {
				//Daten gleich in das Data-Objekt schreiben
				insertArray(dest, buffer, compressedDataLength);
			}
		}
		catch(IOException e) {
			throw new FailureException(ErrorMessage.CAN_NOT_SERIALIZE, e, LoggerException.WARNING);
		}
		finally {
			Util.close(oos);
		}
	}

	/** Objekt zum dekomprimieren. */
	private Inflater inflater = new Inflater();

	/** Bytefeld für dekomprimierte Daten. */
	private byte uncompressedData[] = new byte[10000];

	/**
	 * Deserialisiert und dekomprimiert ein Objekt aus einem Bytefeld.
	 *
	 * @param compressedData Bytefeld mit komprimiertem und serialisiertem Objekt.
	 *
	 * @throws FailureException Fehler beim dekomprimieren und deserialisieren.
	 * @return Wiederhergestelltes Objekt.
	 */
	public Object deserializeZIP(byte[] compressedData) throws FailureException {
		byte buffer[] = uncompressedData;
		inflater.reset();
		ObjectInputStream ois = null;
		try {
			int length = readInt(compressedData);
			if(length > uncompressedData.length) buffer = new byte[length];

			inflater.setInput(compressedData, 4, compressedData.length - 4);
			inflater.inflate(buffer);

			ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
			ois = new ObjectInputStream(bis);
			return ois.readObject();
		}
		catch(IOException e) {
			throw new FailureException(ErrorMessage.CAN_NOT_DESERIALIZE, e, LoggerException.WARNING);
		}
		catch(DataFormatException e) {
			throw new FailureException(e, LoggerException.WARNING);
		}
		catch(ClassNotFoundException e) {
			throw new FailureException(ErrorMessage.WRONG_OBJECT_SERIALIZED, e, LoggerException.WARNING);
		}
		catch(Exception e) {
			return null;
		}
		finally {
			Util.close(ois);
		}
	}

	/** Beendet den Serialisierer. Diese Methode muss aufgerufen werden, wenn der Serialisierer nicht mehr verwendet wird. */
	public void done() {
		if(oos != null) {
			try {
				oos.close();
			}
			catch(IOException e) {
				debug.warning("Kann Serialisierer nicht beenden.", e);
			}
			finally {
				oos = null;
			}
		}
	}

	/**
	 * Ruft {@link #done()} auf.
	 *
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		done();
		super.finalize();
	}


	/**
	 * Serialisiert ein Objekt in einen Byte-Array.
	 *
	 * @param obj Objekt
	 *
	 * @throws FailureException Objekt konnte nicht serialisiert werden
	 * @return Byte-Array mit serialisiertem Objekt
	 */
	public static byte[] serializeToByteArray(Serializable obj) throws FailureException {
		ObjectOutputStream oos = null;
		try {
			// Daten serialisieren:
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		}
		catch(IOException e) {
			throw new FailureException(ErrorMessage.CAN_NOT_SERIALIZE, e, LoggerException.WARNING);
		}
		finally {
			Util.close(oos);
		}
	}

	/**
	 * Serialisiert ein Objekt in eine Datei.
	 *
	 * @param fileName Dateiname
	 * @param obj      Objekt
	 *
	 * @throws FailureException Falls es zu einem Fehler kommt.
	 */
	public static void serialize(String fileName, Serializable obj) throws FailureException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
		}
		catch(FileNotFoundException e) {
			throw new FailureException(ErrorMessage.FILE_NOT_FOUND + fileName, e, LoggerException.WARNING);
		}
		catch(IOException e) {
			throw new FailureException(ErrorMessage.CAN_NOT_ACCESS_FILE + fileName, e, LoggerException.WARNING);
		}
		finally {
			Util.close(fos);
		}
	}

	/**
	 * Deserialisiert ein Objekt aus einer Datei.
	 *
	 * @param fileName Dateiname
	 *
	 * @return Deserialisiertes Objekt oder null im Fehlerfall.
	 */
	public static Object deserialize(String fileName) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(fileName));
			return ois.readObject();
		}
		catch(ClassNotFoundException e) {
			debug.warning(ErrorMessage.CAN_NOT_SERIALIZE, e);
			return null;
		}
		catch(IOException e) {
			debug.warning(ErrorMessage.CAN_NOT_ACCESS_FILE + fileName + ", " + e.getMessage());
			return null;
		}
		finally {
			Util.close(ois);
		}
	}


	/**
	 * Deserialisiert ein Objekt aus einem Byte-Array
	 *
	 * @param data Serialisiertes Objekt.
	 *
	 * @return Deserialisiertes Objekt oder <code>null</code> im Fehlerfall oder falls keine Daten zu deserialisieren waren.
	 */
	public static Object deserialize(byte[] data) {
		if(data.length != 0) {
			ObjectInputStream ois = null;
			Object result = null;
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ois = new ObjectInputStream(bis);
				result = ois.readObject();
			}
			catch(Exception e) {
				debug.warning(ErrorMessage.CAN_NOT_SERIALIZE, e);
				result = null;
			}
			finally {
				Util.close(ois);
			}
			return result;
		}
		else {
			return null;
		}
	}

	/**
	 * Fügt einen Byte-Array in den Datensatz ein. Kann normalerweise durch {@link Data.NumberArray#set(byte...)} ersetzt werden.
	 *
	 * @param dest   Datensatz, in den der Byte-Array kopiert wird.
	 * @param source Byte-Array mit den Quelldaten. Falls source <code>null</code> ist, wird ein leerer Datensatz (d.h. ein Feld mit Länge 0) gesendet.
	 * @param length Anzahl der zu kopierenden Bytes. Muss <code><= source.length</code> sein.
	 */
	public static void insertArray(Data.Array dest, byte source[], int length) {
		if(source != null) {
			if(length != source.length) {
				byte[] tmp = new byte[length];
				System.arraycopy(source, 0, tmp, 0, length);
				source = tmp;
			}
			dest.asUnscaledArray().set(source);
		}
		else {
			dest.setLength(0);
		}
	}

	/**
	 * Serialisiert einen Long Wert in einen Byte-Array
	 *
	 * @param longValue Zu serialisierender Long-Wert.
	 *
	 * @throws FailureException Objekt konnte nicht serialisiert werden
	 * @return Byte-Array mit serialisiertem Objekt
	 */
	public static byte[] serializeId(long longValue) throws FailureException {
		ObjectOutputStream oos = null;
		try {
			//Daten serialisieren:
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeLong(longValue);
			oos.flush();
			byte ba[] = bos.toByteArray();

			return ba;
		}
		catch(IOException e) {
			throw new FailureException(ErrorMessage.CAN_NOT_ACCESS_FILE, e, LoggerException.WARNING);
		}
		finally {
			Util.close(oos);
		}
	}

	/**
	 * Deserialisiert einen <code>long</code> Wert aus einem Byte-Array
	 *
	 * @param data Serialisierter long.
	 *
	 * @return Long Objekt oder <code>null</code> im Fehlerfall.
	 */
	public static Long deserializeId(byte[] data) {
		if(data.length == 8){
			debug.error("Ein Kommunikationspartner verwendet eine inkompatible Version der de.bsvrz.sys.funclib.losb (3.7.0 oder 3.7.1), bitte aktualisieren!");
		}
		ObjectInputStream ois = null;
		Long result = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bis);
			result = ois.readLong();
		}
		catch(Exception e) {
			debug.warning(ErrorMessage.CAN_NOT_DESERIALIZE, e);
			result = null;
		}
		finally {
			Util.close(ois);
		}
		return result;
	}

	static private void writeInt(int input, byte ba[]) {
		ba[0] = (byte)(input >>> 24);
		ba[1] = (byte)(input >>> 16);
		ba[2] = (byte)(input >>> 8);
		ba[3] = (byte)(input);
	}

	static private int readInt(byte ba[]) {
		return (ba[0] & 0xFF) << 24 | (ba[1] & 0xFF) << 16 | (ba[2] & 0xFF) << 8 | (ba[3] & 0xFF);
	}
}
