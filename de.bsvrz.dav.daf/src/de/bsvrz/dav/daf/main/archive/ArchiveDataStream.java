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

import java.io.IOException;

/**
 * Schnittstelle zum Zugriff auf einen einzelnen Ergebnisdatenstrom einer Archivanfrage. �ber die hier definierten
 * Methoden k�nnen die Datens�tze dieses Archivdatenstroms abgefragt werden, es kann signalisiert werden, dass keine
 * weiteren Datens�tze ben�tigt werden und es kann die mit diesem Ergebnisdatenstrom korrespondierende
 * Archivdatenspezifikation, die in der Archivanfrage angegeben wurde, abgefragt werden. Die einzelnen zu einer
 * Archivanfrage geh�renden Ergebnisdatenstr�me k�nnen �ber die Methode {@link ArchiveDataQueryResult#getStreams} abgefragt
 * werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 * @see ArchiveRequestManager#request(ArchiveQueryPriority,ArchiveDataSpecification)
 * @see ArchiveRequestManager#request(ArchiveQueryPriority, java.util.List)
 * @see ArchiveDataQueryResult#getStreams
 */
public interface ArchiveDataStream {

	/**
	 * Bestimmt die Archivdatenspezifikation, die zu diesem Ergebnisdatenstrom gef�hrt hat.
	 *
	 * @return Archivdatenspezifikation, die zu diesem Ergebnisdatenstrom gef�hrt hat.
	 */
	ArchiveDataSpecification getDataSpecification();

	/**
	 * Entfernt einen Datensatz vom Ergebnisdatenstrom und gibt ihn zur�ck. Diese Methode wird von einer Applikation
	 * aufgerufen um die vom Archivsystem auf diesem Ergebnisdatenstrom zur Verf�gung gestellten Datens�tze abzurufen. Die
	 * einzelnen Datens�tze werden mit wiederholten Aufrufen dieser Methode sukzessiv in der gleichen Reihenfolge wie sie
	 * im Archivsystem erzeugt werden zur Verf�gung gestellt. Sind alle Datens�tze so �bergeben worden, dann muss dies
	 * durch R�ckgabe von <code>null</code> signalisiert werden.
	 *
	 * @return Der n�chste Archivdatensatz oder <code>null</code>, wenn alle Datens�tze dieses Ergeebnisdatenstroms
	 *         abgefragt wurden.
	 * @throws IllegalStateException Falls der Ergebnisdatenstrom mit der Methode {@link #abort} abgebrochen wurde.
	 * @throws InterruptedException  Falls der aufrufende Thread unterbrochen wurde, w�hrend auf den n�chsten Datensatz
	 *                               gewartet wurde.
	 * @throws IOException           Falls Probleme in der Kommunikation mit dem Archivsystem aufgetreten sind und noch
	 *                               nicht alle Datens�tze �bertragen wurden.
	 */
	ArchiveData take() throws InterruptedException, IOException, IllegalStateException;

	/**
	 * Bricht die �bertragung von Datens�tzen f�r diesen Ergebnisdatenstrom ab. Diese Methode kann von einer Applikation
	 * aufgerufen werden, um zu signalisieren, dass keine weiteren Datens�tze mehr von diesem Ergebnisdatenstrom ben�tigt
	 * werden. Anschlie�ende Aufrufe der Methode {@link #take} werden mit einer entsprechenden Exception quittiert.
	 */
	void abort();


}
