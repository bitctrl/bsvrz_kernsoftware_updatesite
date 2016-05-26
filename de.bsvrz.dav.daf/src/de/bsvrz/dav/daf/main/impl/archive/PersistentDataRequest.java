/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.archive;

import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;

/**
 * Beschreibt eine Teilanfrage einer Archivanfrage. Eine Archivanfrage wird durch übergabe eines Arrays von Teilanfragen
 * mit der Methode {@link PersistenceModule#getArchiveDataStreams} gestartet.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public final class PersistentDataRequest {

	private ArchiveTimeSpecification _archiveTimeSpecification;
	private final PersistentDataContainer[] _relevantContainers;
	private final boolean _delayedDataReorderedByDataTime;

	/**
	 * Erzeugt eine neue Teilanfrage.

	 * @param archiveTimeSpecification       Bereich über den sich die Archivanfrage erstreckt.
	 * @param relevantContainers             Array mit den für diese Teilanfrage zu betrachtenden Containern.
	 * @param delayedDataReorderedByDataTime Spezifiziert, ob nachgelieferte Datensätze mit Hilfe des Datenzeitstempels
	 *                                       einsortiert werden sollen. Der Wert <code>false</code> legt fest, dass die
	 *                                       Datensätze in der Reihenfolge ihres Datensatzindexes geliefert werden sollen.
	 *                                       Der Wert <code>true</code> definiert, dass die Datensätze aus Containern der
	 *                                       Datensatzart {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind#ONLINE} und {@link
	 *                                       de.bsvrz.dav.daf.main.archive.ArchiveDataKind#REQUESTED} in der Reihenfolge ihres Datensatzindexes geliefert
	 *                                       werden sollen und Datensätze aus Containern der Datensatzart {@link
	 *                                       de.bsvrz.dav.daf.main.archive.ArchiveDataKind#ONLINE_DELAYED} und {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind#REQUESTED_DELAYED}
	 *                                       entsprechend ihres Datenzeitstempels einsortiert werden sollen.
	 */
	public PersistentDataRequest(
			ArchiveTimeSpecification archiveTimeSpecification,
								 PersistentDataContainer[] relevantContainers, boolean delayedDataReorderedByDataTime) {
		_archiveTimeSpecification = archiveTimeSpecification;
		_relevantContainers = relevantContainers;
		_delayedDataReorderedByDataTime = delayedDataReorderedByDataTime;
	}

	/**
	 * Bestimmt die für diese Teilanfrage zu betrachtenden Container.
	 *
	 * @return Array mit den relevanten Containern.
	 */
	public PersistentDataContainer[] getRelevantContainers() {
		return _relevantContainers;
	}

	/**
	 * Bestimmt, ob nachgelieferte Datensätze mit Hilfe des Datenzeitstempels einsortiert werden sollen.
	 *
	 * @return <code>false</code>, falls die Datensätze in der Reihenfolge ihres Datensatzindexes geliefert werden sollen;
	 *         <code>true</code> falls die Datensätze aus Containern der Datensatzart {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind#ONLINE} und
	 *         {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind#REQUESTED} in der Reihenfolge ihres Datensatzindexes geliefert werden sollen und
	 *         Datensätze aus Containern der Datensatzart {@link de.bsvrz.dav.daf.main.archive.ArchiveDataKind#ONLINE_DELAYED} und {@link
	 *         de.bsvrz.dav.daf.main.archive.ArchiveDataKind#REQUESTED_DELAYED} entsprechend ihres Datenzeitstempels einsortiert werden sollen.
	 */
	public boolean isDelayedDataReorderedByDataTime() {
		return _delayedDataReorderedByDataTime;
	}

	
	public ArchiveTimeSpecification getArchiveTimeSpecification() {
		return _archiveTimeSpecification;
	}
}
