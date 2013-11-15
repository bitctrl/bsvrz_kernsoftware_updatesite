/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.dataSerializer.
 * 
 * de.bsvrz.sys.funclib.dataSerializer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.dataSerializer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.dataSerializer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.dataSerializer;

/**
 * Ausnahme, die beim Erzeugen von Serialisieren oder Deserialisierern erzeugt wird, um zu signalisieren, dass die
 * gewünschte Version nicht verfügbar ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5021 $
 * @see SerializingFactory#createSerializer(int, java.io.OutputStream)
 * @see SerializingFactory#createDeserializer(int, java.io.InputStream)
 */
public final class NoSuchVersionException extends Exception {
	/**
	 * Constructs a new exception with <code>null</code> as its detail message. The cause is not initialized, and may
	 * subsequently be initialized by a call to {@link #initCause}.
	 */
	public NoSuchVersionException() {
	}

	/**
	 * Constructs a new exception with the specified detail message.  The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
	 *                method.
	 */
	public NoSuchVersionException(final String message) {
		super(message);
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "NoSuchVersionException{ " + getLocalizedMessage() + "}";
	}


}
