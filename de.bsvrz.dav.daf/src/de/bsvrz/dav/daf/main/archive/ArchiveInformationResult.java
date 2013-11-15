/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

/**
 * Eine Implementation dieses Interface enth�lt Informationen dar�ber, welche Zeitbereiche/Indexbereiche sich im
 * direkten Zugriff des Archivsystems befinden. Das Objekt bezieht sich sich dabei auf eine Datenidentifikation {@link
 * ArchiveDataSpecification}. Zu jedem Zeitbereich/Indexbereich wird au�erdem das Speichermedium vom Typ B
 * angegeben, auf dem die Informationen persistent gespeichert sind. In den F�llen wo das Archivsystem den
 * Zeitbereich/Indexbereich nicht identifizieren kann, wird das Flag "Datenl�cke" gesetzt.
 * <p/>
 * Zu dem oben beschriebenen Zeitbereich/Indexbereich wird au�erdem noch ein Objekt vom Typ {@link
 * ArchiveDataSpecification} gespeichert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public interface ArchiveInformationResult {

	/**
	 * Gibt den Zeit/Indexbereich zur�ck, auf den sich die Informationen beziehen (obere Grenze).
	 *
	 * @return obere Grenze des Intervalls
	 */
	long getIntervalStart();

	/**
	 * Gibt den Zeit/Indexbereich zur�ck, auf den sich die Informationen beziehen (untere Grenze).
	 *
	 * @return untere Grenze des Intervalls
	 */
	long getIntervalEnd();

	/**
	 * Gibt die Art des Intervalls (Datenindex, Datenzeit oder Archivzeit) zur�ck.
	 *
	 * @return Datenindex, Datenzeit oder Archivzeit
	 */
	TimingType getTimingType();

	/**
	 * Der R�ckgabewert dieser Methode bestimmt, ob eine Datenl�cke vorhanden ist.
	 *
	 * @return true = potentielle Datenl�cke (Daten befinden sich nicht im direkten Zugriff und sind auch auf einem
	 *         Speichermedium Typ B nicht vorhanden); false = alle Daten des Intervalls sind verf�gbar
	 */
	boolean isDataGap();

	/**
	 * Der R�ckgabewert dieser Methode bestimmt, ob sich der Zeitbereich im direkten Zugriff des Archivsystems befindet.
	 *
	 * @return true = Der Zeitbereich ist im direkten Zugriff des Archivsystems; false = Der Zeitbereich ist nicht im
	 *         direkten Zugriff des Archivsystems
	 */
	boolean directAccess();

	/**
	 * Diese Methode gibt die eindeutige Identifikation des Speichermediums Tyb B zur�ck, auf dem das angegebene Intervall
	 * gesichert wurde.
	 *
	 * @return eindeutige Identifikation des Speichermediums Tyb B oder <code>null</code> falls die Identifikation
	 *         unbekannt ist.
	 */
	int getVolumeIdTypB();

	/**
	 * Gibt die Archivanfrage zur�ck, die diese Informationen erzeugt hat.
	 *
	 * @return Archivanfrage
	 */
	ArchiveDataSpecification getArchiveDataSpecification();
}
