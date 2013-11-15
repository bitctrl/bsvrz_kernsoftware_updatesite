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

/**
 * @author Kappich Systemberatung
 * @version $Revision: 9109 $
 */
public final class FileBackedShortQueue extends FileBackedQueue<Short> {

	/**
	 * Erstellt eine neue Queue, die durch ein Dateisystem unterstützt wird und so recht groß werden kann.
	 *
	 * @param memoryCapacity     Wie viel Speicher in Bytes maximal im Arbeitsspeicher gehalten werden sollen.
	 * @param filesystemCapacity Wie viel Speicher in Bytes maximal im Dateisystem gehalten werden sollen. Es handelt sich um einen Richtwert, der geringfügig
	 *                           überschritten werden kann.
	 */
	public FileBackedShortQueue(final int memoryCapacity, final long filesystemCapacity) {
		super(memoryCapacity, filesystemCapacity, new ShortQueueSerializer());
	}
}
