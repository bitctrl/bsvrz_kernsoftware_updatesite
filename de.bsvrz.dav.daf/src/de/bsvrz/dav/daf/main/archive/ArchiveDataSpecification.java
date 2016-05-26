/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.main.archive;

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Ein Objekt dieser Klasse spiegelt eine Archivanfrage (ohne Priorität) wieder.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ArchiveDataSpecification {
	private final ArchiveTimeSpecification _timeSpec;
	private final ArchiveDataKindCombination _dataKinds;
	private final ArchiveOrder _sortOrder;
	private final ArchiveRequestOption _requestOption;
	private final DataDescription _dataDescription;
	private final SystemObject _object;
	private boolean _queryWithPid = false;

	/**
	 * Erzeugt eine Definition für eine Archivanfrage, die Archivdaten für ein Systemobjekt abfragt
	 * @param timeSpec Definition des angefragten Zeitbereichs/Indexbereichs
	 * @param dataKinds Datenarten
	 * @param sortOrder Sortierung
	 * @param requestOption Anfrageart
	 * @param dataDescription Angefragte Datenart (Attributguppe/Aspekt/Simulationsvariante-Kombination)
	 * @param object Systemobjekt, von dem die Daten abgefragt werden sollen
	 */
	public ArchiveDataSpecification(ArchiveTimeSpecification timeSpec,
									ArchiveDataKindCombination dataKinds,
									ArchiveOrder sortOrder,
									ArchiveRequestOption requestOption,
									DataDescription dataDescription,
									SystemObject object) {
		_timeSpec = timeSpec;
		_requestOption = requestOption;
		_dataDescription = dataDescription;
		_dataKinds = dataKinds;
		_sortOrder = sortOrder;
		_object = object;
	}

	/**
	 * Erzeugt eine Definition für eine Archivanfrage, die Archivdaten für ein Systemobjekt und optional historische Objekte mit gleicher Pid abfragt.
	 * Dieser Konstruktor ist möglicherweise bei älteren DAF nicht vorhanden.
	 * @param timeSpec Definition des angefragten Zeitbereichs/Indexbereichs
	 * @param dataKinds Datenarten
	 * @param sortOrder Sortierung
	 * @param requestOption Anfrageart
	 * @param dataDescription Angefragte Datenart (Attributguppe/Aspekt/Simulationsvariante-Kombination)
	 * @param object Systemobjekt, von dem die Daten abgefragt werden sollen
	 * @param queryWithPid Bestimmt ob anhand der Objekt-Pid eventuell noch Daten von zusätzlichen historischen Objekten abgefragt werden sollen. Siehe {@link #setQueryWithPid()}.
	 */
	public ArchiveDataSpecification(ArchiveTimeSpecification timeSpec,
									ArchiveDataKindCombination dataKinds,
									ArchiveOrder sortOrder,
									ArchiveRequestOption requestOption,
									DataDescription dataDescription,
									SystemObject object,
									boolean queryWithPid) {
		_timeSpec = timeSpec;
		_requestOption = requestOption;
		_dataDescription = dataDescription;
		_dataKinds = dataKinds;
		_sortOrder = sortOrder;
		_object = object;
		_queryWithPid = queryWithPid;
	}

	/**
	 * Sorgt dafür, dass anhand der Objekt-Pid eventuell noch Daten von zusätzlichen historischen Objekten abgefragt werden sollen.
	 * Archivsystemseitig oder bei Systemobjekten ohne Pid hat dieses Flag keine Funktion.
	 * Diese Methode ist möglicherweise bei älteren DAF nicht vorhanden. Es kann daher aus Kompatibilitätsgründen sinnvoll sein,
	 * den klassischen Konstruktor ohne <code>queryWithPid</code>-Parameter zu verwenden und nachher diese Funktion in einem try-catch-Block aufzurufen:
	 * <pre>{@code
	 * ArchiveDataSpecification ads = new ArchiveDataSpecification(...);
	 * try {
	 *     ads.setQueryWithPid();
	 * }
	 * catch(NoSuchMethodError e) {}
	 * }
	 * </pre>
	 */
	public void setQueryWithPid() {
		_queryWithPid = true;
	}

	/**
	 * Gibt zurück, ob anhand der Objekt-Pid eventuell noch Daten von zusätzlichen historischen Objekten abgefragt werden sollen.
	 * Archivsystemseitig oder bei Systemobjekten ohne Pid hat dieses Flag keine Funktion.
	 * Diese Methode ist möglicherweise bei älteren DAF nicht vorhanden.
	 * @return true wenn anhand der Pid historische Objekte berücksichtigt werden sollen, sonst false
	 */
	public boolean getQueryWithPid() {
		return _queryWithPid;
	}

	/**
	 *
	 * @return SystemObject
	 */
	public SystemObject getObject() {
		return _object;
	}

	/**
	 * String-Repräsentation des Objektes.
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
				", _queryWithPid=" + _queryWithPid +
				"}";
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
	 * @return Welche Art von Daten sollen in die Archivantwort einbezogen werden (online, onlineDelayed, ...)
	 */
	public ArchiveDataKindCombination getDataKinds() {
		return _dataKinds;
	}

	/**
	 *
	 * @return Wie sollen die nachgelieferten Datensätze einsortiert werden (Zeit, Index)
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
	 * Angefragte Datenart (Attributguppe/Aspekt/Simulationsvariante-Kombination)
	 * @return DataDescription
	 */
	public DataDescription getDataDescription() {
		return _dataDescription;
	}

}
