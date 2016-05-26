/*
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
package de.bsvrz.dav.daf.main.impl.archive.request;

import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;

/**
 * Ein Objekt dieser Klasse enthält Informationen darüber welche Zeitbereiche/Indexbereiche sich im direkten Zugriff des
 * Archivsystems befinden. Das Objekt bezieht sich sich dabei auf eine Datenidentifikation {@link
 * de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification}. Zu jedem Zeitbereich/Indexbereich wird außerdem das Speichermedium vom Typ B
 * angegeben, auf dem die Informationen persistent gespeichert sind. In den Fällen wo das Archivsystem den
 * Zeitbereich/Indexbereich nicht identifizieren kann, wird das Flag "Datenlücke" gesetzt.
 *
 * Zu dem oben beschriebenen Zeitbereich/Indexbereich wird außerdem noch ein Objekt vom Typ {@link de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification}
 * gespeichert.
 * 
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Achim Wullenkord (AW)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ArchiveInfoResult implements ArchiveInformationResult {

	/**
	 * obere Intervallgrenze
	 */
	private final long _intervalStart;
	/**
	 * untere Intervallgrenze
	 */
	private final long _intervalEnd;
	/**
	 * Beziehen sich die Intervallgerenzen auf Datenzeit oder Archivzeit oder Datenindex
	 */
	private final TimingType _timingType;
	/**
	 * true = potentielle Datenlücke, dieser Zeitbereich befindet sich weder im direkten Zugriff des Archivsystems, noch
	 * auf einem Speichermedium Tyb B false = Dieser Zeitbereich befindet sich entweder im direkten Zugriff des
	 * Archivsystems oder ist auf einem Speichermedium Typ B vorhanden.
	 */
	private final boolean _dataGap;
	/**
	 * Eindeutige Identifikation des Speichermdiums Typ B, auf dem der angegebene Zeit/Indexbereich gesichert wurde.
	 */
	private final int _labelTypB;

	/**
	 * Befindet sich der Zeitbereich im direkten Zugriff des Archivsystems
	 */
	private final boolean _directAccess;

	/**
	 * Archivanfrage
	 */
	private final ArchiveDataSpecification _archiveDataSpecification;

	/**
	 * @param intervalStart Auf welchen Intervallteil beziehen sich die Informationen
	 * @param intervalEnd   Auf welchen Intervallteil beziehen sich die Informationen
	 * @param timingType    Datenindex oder Datenzeit oder Archivzeit
	 * @param dataGap       potentielle Datenlücke
	 * @param labelTypB     Eindeutige Identifikation des Speichermediums Typ B, auf dem das angegebene Intervall
	 *                      persistent gespeichert wurde
	 */
	public ArchiveInfoResult(long intervalStart, long intervalEnd, TimingType timingType, boolean dataGap, boolean directAccess,int labelTypB, ArchiveDataSpecification archiveDataSpecification) {
		_intervalStart = intervalStart;
		_intervalEnd = intervalEnd;
		_timingType = timingType;
		_dataGap = dataGap;
		_labelTypB = labelTypB;
		_directAccess = directAccess;
		_archiveDataSpecification = archiveDataSpecification;
	}

	/**
	 * Zeit/Indexbereich auf den sich die Informationen beziehen (obere Schranke)
	 *
	 * @return obere Schranke
	 */
	public long getIntervalStart() {
		return _intervalStart;
	}

	/**
	 * Zeit/Indexbereich auf den sich die Informationen beziehen (untere Schranke)
	 *
	 * @return untere Schranke
	 */
	public long getIntervalEnd() {
		return _intervalEnd;
	}

	/**
	 * Art des Intervalls (Datenindex oder Datenzeit oder Archivzeit)
	 *
	 * @return Datenindex oder Datenzeit oder Archivzeit
	 */
	public TimingType getTimingType() {
		return _timingType;
	}

	/**
	 * Ist eine potentielle Datenlücke vorhanden
	 *
	 * @return true = potentielle Datenlücke (Daten befinden sich nicht im direkten Zugriff und sind auch auf einem
	 *         Speichermedium Typ B nicht vorhanden); false = alle Daten des Intervalls sind verfügbar
	 */
	public boolean isDataGap() {
		return _dataGap;
	}

	public boolean directAccess() {
		return _directAccess;
	}

	/**
	 * Gibt die eindeutige Identifikation des Speichermediums Tyb B zurück, auf dem das angegebene Intervall gesichert
	 * wurde.
	 *
	 * @return eindeutige Identifikation des Speichermediums Tyb B oder <code>null</code> falls die Identifikation
	 *         unbekannt ist.
	 */
	public int getVolumeIdTypB() {
		return _labelTypB;
	}

	/**
	 * Gibt die zugehörige Archivanfrage zurück.
	 * @return Archivanfrage
	 */
	public ArchiveDataSpecification getArchiveDataSpecification() {
		return _archiveDataSpecification;
	}
}
