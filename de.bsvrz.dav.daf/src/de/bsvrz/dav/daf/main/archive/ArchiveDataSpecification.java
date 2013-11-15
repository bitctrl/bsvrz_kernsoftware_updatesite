/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniﬂ Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.archive;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Ein Objekt dieser Klasse spiegelt eine Archivanfrage(ohne Priorit‰t) wieder. Eine genauere Beschreibung steht in den
 * "gettern" der Klasse.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 */
public class ArchiveDataSpecification {
	private final ArchiveTimeSpecification _timeSpec;
	private final ArchiveDataKindCombination _dataKinds;
	private final ArchiveOrder _sortOrder;
	private final ArchiveRequestOption _requestOption;
	private final DataDescription _dataDescription;
	private final SystemObject _object;

	public ArchiveDataSpecification(ArchiveTimeSpecification timeSpec,
									ArchiveDataKindCombination dataKinds,
									ArchiveOrder sortOrder,
									ArchiveRequestOption requestOption,
									DataDescription dataDescription,
									SystemObject object) {
		_timeSpec = timeSpec;
		_dataKinds = dataKinds;
		_sortOrder = sortOrder;
		_requestOption = requestOption;
		_dataDescription = dataDescription;
		_object = object;
	}

	/**
	 *
	 * @return Zeit/Indexbereich auf den sich die Archivanfrage bezieht
	 */
	public ArchiveTimeSpecification getTimeSpec() {
		return _timeSpec;
	}

	/**
	 *
	 * @return Welche Art von Daten sollen in die Archivantwort einbezogen werden (online, onlineDelayd, ...)
	 */
	public ArchiveDataKindCombination getDataKinds() {
		return _dataKinds;
	}

	/**
	 *
	 * @return Wie sollen die nachgelieferten Datens‰tze einsortiert werden (Zeit, Index)
	 */
	public ArchiveOrder getSortOrder() {
		return _sortOrder;
	}

	/**
	 *
	 * @return Zustandsanfrage oder Deltaanfrage
	 */
	public ArchiveRequestOption getRequestOption() {
		return _requestOption;
	}

	/**
	 *
	 *
	 * @return DataDescription
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}

	/**
	 *
	 * @return SystemObject
	 */
	public SystemObject getObject() {
		return _object;
	}

	/**
	 * String-Repr‰sentation des Objektes.
	 *
	 * @return Beschreibung des Objektes.
	 */
	public String toString() {
		return "ArchiveDataSpecification{" +
				"_timeSpec=" + _timeSpec +
				", _dataKinds=" + _dataKinds +
				", _sortOrder=" + _sortOrder +
				", _requestOption=" + _requestOption +
				", _dataDescription=" + _dataDescription +
				", _object=" + _object +
				"}";
	}
}
