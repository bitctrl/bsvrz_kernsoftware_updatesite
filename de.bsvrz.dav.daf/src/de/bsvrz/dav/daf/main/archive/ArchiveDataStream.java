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

import java.io.IOException;

/**
 * Schnittstelle zum Zugriff auf einen einzelnen Ergebnisdatenstrom einer Archivanfrage. Über die hier definierten
 * Methoden können die Datensätze dieses Archivdatenstroms abgefragt werden, es kann signalisiert werden, dass keine
 * weiteren Datensätze benötigt werden und es kann die mit diesem Ergebnisdatenstrom korrespondierende
 * Archivdatenspezifikation, die in der Archivanfrage angegeben wurde, abgefragt werden. Die einzelnen zu einer
 * Archivanfrage gehörenden Ergebnisdatenströme können über die Methode {@link ArchiveDataQueryResult#getStreams} abgefragt
 * werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ArchiveRequestManager#request(ArchiveQueryPriority, de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification)
 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
 * @see ArchiveDataQueryResult#getStreams
 */
public interface ArchiveDataStream {

	/**
	 * Bestimmt die Archivdatenspezifikation, die zu diesem Ergebnisdatenstrom geführt hat.
	 *
	 * @return Archivdatenspezifikation, die zu diesem Ergebnisdatenstrom geführt hat.
	 */
	ArchiveDataSpecification getDataSpecification();

	/**
	 * Entfernt einen Datensatz vom Ergebnisdatenstrom und gibt ihn zurück. Diese Methode wird von einer Applikation
	 * aufgerufen um die vom Archivsystem auf diesem Ergebnisdatenstrom zur Verfügung gestellten Datensätze abzurufen. Die
	 * einzelnen Datensätze werden mit wiederholten Aufrufen dieser Methode sukzessiv in der gleichen Reihenfolge wie sie
	 * im Archivsystem erzeugt werden zur Verfügung gestellt. Sind alle Datensätze so übergeben worden, dann muss dies
	 * durch Rückgabe von <code>null</code> signalisiert werden.
	 *
	 * @return Der nächste Archivdatensatz oder <code>null</code>, wenn alle Datensätze dieses Ergeebnisdatenstroms
	 *         abgefragt wurden.
	 * @throws IllegalStateException Falls der Ergebnisdatenstrom mit der Methode {@link #abort} abgebrochen wurde.
	 * @throws InterruptedException  Falls der aufrufende Thread unterbrochen wurde, während auf den nächsten Datensatz
	 *                               gewartet wurde.
	 * @throws IOException           Falls Probleme in der Kommunikation mit dem Archivsystem aufgetreten sind und noch
	 *                               nicht alle Datensätze übertragen wurden.
	 */
	ArchiveData take() throws InterruptedException, IOException, IllegalStateException;

	/**
	 * Bricht die Übertragung von Datensätzen für diesen Ergebnisdatenstrom ab. Diese Methode kann von einer Applikation
	 * aufgerufen werden, um zu signalisieren, dass keine weiteren Datensätze mehr von diesem Ergebnisdatenstrom benötigt
	 * werden. Anschließende Aufrufe der Methode {@link #take} werden mit einer entsprechenden Exception quittiert.
	 */
	void abort();


}
