/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util.fileBackedQueue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ObjectQueueSerializer<E> implements QueueSerializer<E> {

	public int getSize(final E object) {
		try {
			return serializeToByteArray(object).size();
		}
		catch(IOException e) {
			throw new IllegalStateException("Größe eines Objekts nicht ermittelbar: " + object, e);
		}
	}

	public void serialize(final DataOutputStream outputStream, final E object) throws IOException {
		serializeToByteArray(object).writeTo(outputStream);
	}

	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "unchecked", "OverlyBroadCatchBlock", "CatchGenericClass"})
	public E deserialize(final DataInputStream inputStream) throws IOException {
		final ObjectInputStream stream = new ObjectInputStream(inputStream);
		try {
			return (E)stream.readObject();
			// Nicht den Stream schließen, der wird noch gebraucht!
		}
		catch(Exception e) {
			throw new IllegalStateException("Kann Objekt nicht deserialisieren: " , e);
		}
	}

	private ByteArrayOutputStream serializeToByteArray(final E object) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(object);
		return byteArrayOutputStream;
	}
}
