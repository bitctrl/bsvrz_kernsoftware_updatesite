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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class StringQueueSerializer implements QueueSerializer<String> {

	public int getSize(final String object) {
		return object.length() * 2 + 4;
	}

	public void serialize(final DataOutputStream outputStream, final String object) throws IOException {
		outputStream.writeInt(object.length());
		outputStream.writeChars(object);
	}

	public String deserialize(final DataInputStream inputStream) throws IOException {
		final int length = inputStream.readInt();
		final StringBuilder builder = new StringBuilder(length);
		for(int i = 0; i < length; i++) {
			builder.append(inputStream.readChar());
		}
		return builder.toString();
	}
}
