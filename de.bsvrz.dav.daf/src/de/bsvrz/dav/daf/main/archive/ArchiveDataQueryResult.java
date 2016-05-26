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

/**
 * Schnittstelle zum Zugriff auf Ergebnisse von Archivanfragen. Diese Schnittstelle wird von Applikationen benutzt, um
 * auf Ergebnisse von Archivdatenanfragen zuzugreifen, die mit den Methoden {@link ArchiveRequestManager#request}
 * gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben den Methoden des übergeordneten Interfaces
 * {@link ArchiveQueryResult} eine Methode zur Verfügung, mit der auf die Ergebnisdatenströme zugegriffen werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ArchiveRequestManager#request(ArchiveQueryPriority,ArchiveDataSpecification)
 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
 */
public interface ArchiveDataQueryResult extends ArchiveQueryResult {
	/**
	 * Bestimmt die Ergebnisdatenströme der entsprechenden Archivdatenspezifikationen aus der Archivanfrage. Diese Methode
	 * wird von einer Applikation aufgerufen, um auf die Ergebnisdatenströme zuzugreifen. Eine Implementierung dieser
	 * Methode muss zu jeder Archivdatenspezifikation aus der Archivanfrage einen entsprechenden Ergebnisdatenstrom
	 * bereitstellen.
	 *
	 * @return Array mit mit Ergebnisdatenströmen entsprechend den Archivdatenspezifikationen aus der Archivanfrage. Zu
	 *         jeder Archivdatenspezifikation aus der Archivanfrage wird ein korrespondierender Ergebnisdatenstrom im Array
	 *         erzeugt.
	 * @throws IllegalStateException Falls die Archivanfrage nicht erfolgreich war und keine Ergebnisdatenströme bestimmt
	 *                               werden können.
	 * @throws InterruptedException  Falls der aufrufende Thread unterbrochen wurde, während auf die entsprechende
	 *                               Antwortnachricht aus dem Archivsystem gewartet wurde.
	 * @see ArchiveRequestManager#request(ArchiveQueryPriority,ArchiveDataSpecification)
	 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
	 */
	ArchiveDataStream[] getStreams() throws InterruptedException, IllegalStateException;
}
