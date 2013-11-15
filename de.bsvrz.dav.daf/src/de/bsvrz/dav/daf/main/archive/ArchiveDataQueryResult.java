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
 * Schnittstelle zum Zugriff auf Ergebnisse von Archivanfragen. Diese Schnittstelle wird von Applikationen benutzt, um
 * auf Ergebnisse von Archivdatenanfragen zuzugreifen, die mit den Methoden {@link ArchiveRequestManager#request}
 * gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben den Methoden des �bergeordneten Interfaces
 * {@link ArchiveQueryResult} eine Methode zur Verf�gung, mit der auf die Ergebnisdatenstr�me zugegriffen werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5064 $
 * @see ArchiveRequestManager#request(ArchiveQueryPriority,ArchiveDataSpecification)
 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
 */
public interface ArchiveDataQueryResult extends ArchiveQueryResult {
	/**
	 * Bestimmt die Ergebnisdatenstr�me der entsprechenden Archivdatenspezifikationen aus der Archivanfrage. Diese Methode
	 * wird von einer Applikation aufgerufen, um auf die Ergebnisdatenstr�me zuzugreifen. Eine Implementierung dieser
	 * Methode muss zu jeder Archivdatenspezifikation aus der Archivanfrage einen entsprechenden Ergebnisdatenstrom
	 * bereitstellen.
	 *
	 * @return Array mit mit Ergebnisdatenstr�men entsprechend den Archivdatenspezifikationen aus der Archivanfrage. Zu
	 *         jeder Archivdatenspezifikation aus der Archivanfrage wird ein korrespondierender Ergebnisdatenstrom im Array
	 *         erzeugt.
	 * @throws IllegalStateException Falls die Archivanfrage nicht erfolgreich war und keine Ergebnisdatenstr�me bestimmt
	 *                               werden k�nnen.
	 * @throws InterruptedException  Falls der aufrufende Thread unterbrochen wurde, w�hrend auf die entsprechende
	 *                               Antwortnachricht aus dem Archivsystem gewartet wurde.
	 * @see ArchiveRequestManager#request(ArchiveQueryPriority,ArchiveDataSpecification)
	 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
	 */
	ArchiveDataStream[] getStreams() throws InterruptedException, IllegalStateException;
}
