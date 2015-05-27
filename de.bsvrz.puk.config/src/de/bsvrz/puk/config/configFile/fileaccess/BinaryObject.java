/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.configFile.fileaccess;

import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Bin�re Darstellung eines Objekts in der Konfigurationsdatei ({@link de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile}).
 *
 * Diese Klasse bietet einfache Methoden um Objekte aus {@link java.io.DataInput}-Objekten (wie DataInputStreams oder {@link de.bsvrz.dav.daf.util.BufferedRandomAccessFile}s)
 * einzulesen und sie wieder zu schreiben.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 13166 $
 */
public abstract class BinaryObject {

	/**
	 * Gr��e des Headers f�r Konfigurationsobjekte:
	 * ID (long), PidHashCode (int), Typ-ID (long), Objekttyp (byte), Ung�ltig (short), G�ltig (short)
	 */
	public static final int CONFIG_OBJ_HEADER_SIZE = (8 + 4 + 8 + 1 + 2 + 2);

	/**
	 * Gr��e des Headers f�r dynamische Objekte:
	 * ID (long), PidHashCode (int), Typ-ID (long), Objekttyp (byte), Ung�ltig (long), G�ltig (long), Simulationsvariante (short)
	 */
	public static final int DYN_OBJ_HEADER_SIZE = (8 + 4 + 8 + 1 + 8 + 8 + 2);

	// Markierungsbyte f�r Konfigurationsobjekte
	public static final int CONFIGURATION_OBJECT_TYPE = 0;

	// Markierungsbyte f�r dynamische Objekte
	public static final int DYNAMIC_OBJECT_TYPE = 1;

	/**
	 * Liest ein Objekt ein
	 * @param input Eingabe-Stream oder {@link de.bsvrz.dav.daf.util.BufferedRandomAccessFile}, das sich an der richtigen Position befindet.
	 *              Der Stream befindet sich nach dem Einlesen garantiert an der Position nach dem Objekt (oder der L�cke),
	 *              wo also ein weiteres Objekt gelesen werden k�nnte.
	 * @return Eingelesenes Objekt oder null, wenn sich an dieser Position eine L�cke befindet.
	 * @throws IOException
	 */
	public static BinaryObject fromDataInput(DataInput input) throws IOException {
		// L�nge des Blocks einlesen
		final int sizeOfObject = input.readInt();

		// Id des Objekts einlesen
		final long objectId = input.readLong();

		final int pidHashCode = input.readInt();

		final long typeId = input.readLong();

		// 0 = Konfobjekt, 1 = dyn Objekt
		final byte objectType = input.readByte();

		if(objectType == CONFIGURATION_OBJECT_TYPE) {
			// Konfigurationsobjekt
			final short firstInvalid = input.readShort();
			final short firstValid = input.readShort();

			final int sizeOfPackedData = sizeOfObject - CONFIG_OBJ_HEADER_SIZE;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			input.readFully(packedBytes);
			return new BinaryConfigObject(
					objectId,
			        pidHashCode,
			        typeId,
			        firstInvalid,
			        firstValid,
			        packedBytes
			);
		}
		else if(objectType == DYNAMIC_OBJECT_TYPE) {
			final long firstInvalid = input.readLong();
			final long firstValid = input.readLong();
			final short simulationVariant = input.readShort();

			final int sizeOfPackedData = sizeOfObject - DYN_OBJ_HEADER_SIZE;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			input.readFully(packedBytes);
			return new BinaryDynamicObject(
					objectId,
					pidHashCode,
					typeId,
					firstInvalid,
					firstValid,
					simulationVariant,
					packedBytes
			);

		}
		else {
			// Unbekannt, das darf nicht passieren.
			throw new IOException(
					"Ein Objekt konnte weder als dynamisches Objekt noch als Konfigurationsobjekt identifiziert werden, Typ : " + objectType
			);
		}
	}

	/**
	 * Schreibt das Objekt in den Ausgabe-Stream
	 * @param output Ausgabe
	 * @return Anzahl geschriebener Bytes
	 * @throws IOException
	 */
	public abstract int write(DataOutput output) throws IOException;

	/**
	 * Gibt die Objekt-ID zur�ck
	 * @return die Objekt-ID oder 0 falls es sich um eine L�cke handelt
	 */
	public abstract long getObjectId();

	/**
	 * Gibt den Pid-Hashcode zur�ck
	 * @return den Pid-Hashcode
	 */
	public abstract int getPidHashCode();

	/**
	 * Gibt die ID des Objekttyps zu�ck
	 * @return die ID des Objekttyps
	 */
	public abstract long getTypeId();

	/**
	 * Gibt die gepackten Bytes zur�ck. Die gepackten Bytes enthalten weitere Objektinformationen wie die Name, Pid, Konfigurationsdaten usw.
	 * @return die gepackten Bytes
	 */
	public abstract byte[] getPackedBytes();

	public abstract SystemObjectInformationInterface toSystemObjectInfo(final ConfigAreaFile file, final long position) throws IOException, NoSuchVersionException;
}
