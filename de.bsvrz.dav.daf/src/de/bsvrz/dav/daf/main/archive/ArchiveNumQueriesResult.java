/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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
 * Schnittstelle zum Zugriff auf Ergebnisse von Anfragen auf die maximal möglichen Anfragezahlen im Archivsystem. Diese Schnittstelle wird von Applikationen
 * benutzt, um auf Ergebnisse von Archivinformationsanfragen zuzugreifen, die mit den Methoden {@link
 * ArchiveRequestManager#getNumArchiveQueries()} gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben den
 * Methoden des übergeordneten Interfaces {@link de.bsvrz.dav.daf.main.archive.ArchiveQueryResult} eine Methode zur Verfügung, mit der auf die
 * angefragten Informationen zugegriffen werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ArchiveNumQueriesResult extends ArchiveQueryResult {
	/**
	 * Gibt die im Archivsystem fest eingestellte maximale Anzahl an gleichzeitigen Archivanfragen pro Applikation zurück.
	 * @return maximale Anzahl an gleichzeitigen Archivanfragen pro Applikation
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zurückgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} prüfen.
	 */
	int getMaximumArchiveQueriesPerApplication();

	/**
	 * Gibt die (aus Sicht des Archivsystems) aktuelle Anzahl der derzeit aktiven Archivanfragen dieser Applikation zurück.
	 * @return Aktuell verwendete Anzahl an Archivanfragen
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zurückgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} prüfen.*
	 */
	int getCurrentlyUsedQueries();

	/**
	 * Gibt die (aus Sicht des Archivsystems) aktuelle Anzahl der noch möglichen gleichzeitigen Archivanfragen dieser Applikation zurück.
	 * Theoretisch kann eine Applikation noch die zurückgegebene Anzahl an Archivanfragen stellen, ohne dass es zu Problemen kommt.
	 * Aufgrund von Timing-Problemen kann das aber nicht immer garantiert werden.
	 * @return Noch unbenutzes Kontigent an möglichen Archivanfragen für diese Applikation.
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zurückgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} prüfen.
	 */
	int getRemainingQueries();
}
