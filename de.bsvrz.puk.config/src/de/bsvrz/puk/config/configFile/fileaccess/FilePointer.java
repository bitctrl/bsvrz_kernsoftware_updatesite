/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.configFile.fileaccess;

/**
* Bildet einen Dateipointer aif ein Objekt in einer Konfigurationsdatei ab
*
* @author Kappich Systemberatung
* @version $Revision$
*/
public final class FilePointer implements ObjectReference {

	private long _absoluteFilePosition;

	private FilePointer(final long absoluteFilePosition) {
		_absoluteFilePosition = absoluteFilePosition;
	}

	public static FilePointer fromAbsolutePosition(long position, ConfigAreaFile file){
		return new FilePointer(position);
	}

	public static FilePointer fromRelativePosition(long position, ConfigAreaFile file){
		FilePointer pointer;
		if(position > 0) {
			// Es handelt sich um dynamisches Objekt, das sich in der dyn. nGa Menge befindet.
			// Die relative Positionsangabe bezieht sich auf den Beginn des dyn. nGa Bereichs.
			// Die relative Position ist immer um +1 erhöht worden, damit wurde eine "doppelte 0" verhindert.
			// Die "0" gehört zu den Konfigurationsobjekten.
			pointer = new FilePointer(((file.getStartOldDynamicObjects() + file.getHeaderEnd()) + position) - 1);
		}
		else {
			// Es handelt sich um ein Konfigurationsobjekt. Die relative Position bezieht sich auf das
			// Headerende.
			pointer = new FilePointer(file.getHeaderEnd() + (position * (-1)));
		}
		return pointer;
	}

	public long getAbsoluteFilePosition() {
		return _absoluteFilePosition;
	}

	public void setAbsoluteFilePosition(final long absoluteFilePosition) {
		_absoluteFilePosition = absoluteFilePosition;
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final FilePointer that = (FilePointer) o;

		if(_absoluteFilePosition != that._absoluteFilePosition) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (_absoluteFilePosition ^ (_absoluteFilePosition >>> 32));
	}

	@Override
	public String toString() {
		return "FilePointer{" + _absoluteFilePosition +	'}';
	}
}
