/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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
 * Schnittstelle zum Zugriff auf Ergebnisse von Anfragen auf die maximal m�glichen Anfragezahlen im Archivsystem. Diese Schnittstelle wird von Applikationen
 * benutzt, um auf Ergebnisse von Archivinformationsanfragen zuzugreifen, die mit den Methoden {@link
 * ArchiveRequestManager#getNumArchiveQueries()} gestellt wurden. Eine Implementierung dieser Schnittstelle stellt neben den
 * Methoden des �bergeordneten Interfaces {@link de.bsvrz.dav.daf.main.archive.ArchiveQueryResult} eine Methode zur Verf�gung, mit der auf die
 * angefragten Informationen zugegriffen werden kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 11353 $
 */
public interface ArchiveNumQueriesResult extends ArchiveQueryResult {
	/**
	 * Gibt die im Archivsystem fest eingestellte maximale Anzahl an gleichzeitigen Archivanfragen pro Applikation zur�ck.
	 * @return maximale Anzahl an gleichzeitigen Archivanfragen pro Applikation
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zur�ckgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} pr�fen.
	 */
	int getMaximumArchiveQueriesPerApplication();

	/**
	 * Gibt die (aus Sicht des Archivsystems) aktuelle Anzahl der derzeit aktiven Archivanfragen dieser Applikation zur�ck.
	 * @return Aktuell verwendete Anzahl an Archivanfragen
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zur�ckgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} pr�fen.*
	 */
	int getCurrentlyUsedQueries();

	/**
	 * Gibt die (aus Sicht des Archivsystems) aktuelle Anzahl der noch m�glichen gleichzeitigen Archivanfragen dieser Applikation zur�ck.
	 * Theoretisch kann eine Applikation noch die zur�ckgegebene Anzahl an Archivanfragen stellen, ohne dass es zu Problemen kommt.
	 * Aufgrund von Timing-Problemen kann das aber nicht immer garantiert werden.
	 * @return Noch unbenutzes Kontigent an m�glichen Archivanfragen f�r diese Applikation.
	 * @throws RuntimeException wenn {@link #isRequestSuccessful()} falls zur�ckgegeben hat. Daher bitte vorher {@link #isRequestSuccessful()} pr�fen.
	 */
	int getRemainingQueries();
}
