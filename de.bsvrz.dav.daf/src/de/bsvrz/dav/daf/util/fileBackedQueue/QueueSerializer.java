/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util.fileBackedQueue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Ein Interface, das Objekte kapselt, die der {@link FileBackedQueue} dabei helfen, Objekte auf die Festplatte zu serialisieren.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9109 $
 */
public interface QueueSerializer<E> {

	/**
	 * Gibt die Gr��e eines Objektes an. Es ist wichtig, dass hier exakt die L�nge zur�ckgegeben wird, die in den outputStream geschrieben werden,
	 * w�rde man serialize(outputStream, object) mit dem gleichen Objekt aufrufen.
	 *
	 * @param object Objekt
	 *
	 * @return Gr��e des serialisierten Objektes in Bytes
	 */
	int getSize(E object);

	/**
	 * Serialisiert das Objekt
	 *
	 * @param outputStream In diesen Stream soll das Objekt geschrieben werden. Es muss an die aktuelle Stream-Position geschrieben werden und es m�ssen so viele
	 *                     Bytes geschrieben werden, wie getSize(objekt) zur�ckgeben w�rde.
	 * @param object       Objekt, das geschrieben werden soll
	 *
	 * @throws IOException Falls beim Schreiben Fehler auftreten
	 */
	void serialize(DataOutputStream outputStream, E object) throws IOException;

	/**
	 * Deserialisiert ein Objekt aus dem Stream. Es muss an der aktuellen Stream-Position gelesen werden und zwar so viele Bytes, wie das deserialisierte Objekt in
	 * der getSize()-Funktion zur�ckliefern w�rde. Damit vor dem Deserialisieren bekannt ist, wie viele Bytes gelesen werden sollen, ist es bei Typen variabler
	 * L�nge evtl. n�tig beim Serialisieren zuerst die L�nge zu schreiben und beim Deserialisieren als erstes zu lesen.
	 *
	 * @param inputStream Eingabestream.
	 *
	 * @return Das deserialisierte Objekt
	 *
	 * @throws IOException Falls beim Lesen Fehler auftreten
	 */
	E deserialize(DataInputStream inputStream) throws IOException;
}
