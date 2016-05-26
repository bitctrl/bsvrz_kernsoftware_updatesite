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

import java.util.HashMap;
import java.util.Map;

/**
* Interne Klasse, die Informationen während einer Restrukturierugn speichert
*
* @author Kappich Systemberatung
* @version $Revision$
*/
class RestructureInfo implements HeaderInfo {
	/**
	 * Diese Map speichert zu Objekten, welche im Speicher gehalten werden (
	 * {@link ConfigAreaFile#_actualObjects _actualObjects} und
	 * {@link ConfigAreaFile#_newObjects _newObjects}) die neuen Dateipositionen ab.
	 *
	 * Key ist die alte Dateiposition, value die neue.
	 *
	 * Eine Map ist hier eigentlich unnötig, da eine Liste von Paaren oder so genauso ausreichen würde. Dateiposition als Key ist
	 * aber eindeutig und daher OK.
	 */
	final Map<Long, Long> _newFilePositions = new HashMap<Long, Long>();

	/**
	 * Speichert Headerende in neuer Datei (= Start NgaBlöcke)
	 */
	long _headerEnd = -1;

	/**
	 * Speichert Start des NgDyn-Blocks in neuer Datei
	 */
	long _startOldDynamicObjects = -1;

	/**
	 * Start des ID-idnex in neuer Datei
	 */
	long _startIdIndex = -1;

	/**
	 * Start des Pid-Index in neuer Datei
	 */
	long _startPidHashCodeIndex = -1;

	/**
	 * Start der Mischmenge in neuer Datei
	 */
	long _startMixedSet = -1;

	@Override
	public long getHeaderEnd() {
		return _headerEnd;
	}

	@Override
	public long getStartOldDynamicObjects() {
		return _startOldDynamicObjects;
	}

	@Override
	public long getStartIdIndex() {
		return _startIdIndex;
	}

	@Override
	public long getStartPidHashCodeIndex() {
		return _startPidHashCodeIndex;
	}

	@Override
	public long getStartMixedSet() {
		return _startMixedSet;
	}

	public void rememberFilePosition(final FilePointer oldFilePosition, final long newFilePosition) {
		if(oldFilePosition == null) return;
		_newFilePositions.put(oldFilePosition.getAbsoluteFilePosition(), newFilePosition);
	}

	public void rememberFilePosition(final long oldFilePosition, final long newFilePosition) {
		_newFilePositions.put(oldFilePosition, newFilePosition);
	}

}
