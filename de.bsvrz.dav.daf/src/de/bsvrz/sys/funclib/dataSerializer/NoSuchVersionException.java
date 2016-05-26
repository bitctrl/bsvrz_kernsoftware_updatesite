/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
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

/**
 * Ausnahme, die beim Erzeugen von Serialisieren oder Deserialisierern erzeugt wird, um zu signalisieren, dass die
 * gewünschte Version nicht verfügbar ist.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
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
