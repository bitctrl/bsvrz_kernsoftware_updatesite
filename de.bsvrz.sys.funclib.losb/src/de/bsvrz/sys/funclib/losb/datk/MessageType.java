/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.datk;

/**
 * Verschiedene Typen zur Unterscheidung von Nachrichten-Arten.
 *
 * @author beck et al. projects GmbH
 * @author Thomas Schaefer
 * @version $Revision: 11353 $ / $Date: 2013-06-25 16:12:23 +0200 (Tue, 25 Jun 2013) $ / ($Author: jh $)
 */
public class MessageType {

	/** Anfrage an das Archivsystem. */
	public static final int QUERY = 1;

	/** Initiale Ergebnismeldung des Archivsystems zu einer Archivanfrage. */
	public static final int INITIAL_QUERY_RESULT = 2;

	/** Einem Stream zugeordneten Ergebnisdatenpakete des Archivsystems. */
	public static final int STREAM_DATA = 3;

	/** Steuerungspakete, die zwecks Flusskontrolle an dass Archivsystem gesendet werden. */
	public static final int STREAM_CONTROL = 4;

	/** Informationsanfragen an das Archivsystem. */
	public static final int QUERY_INFO = 5;

	/** Antwort auf eine Informationsanfragen, die an das Archivsystem gestellt wurde. */
	public static final int QUERY_INFO_RESULT = 6;

	/** Auftrag, der Archivdaten einer Simulationsvariante loescht. */
	public static final int DELETE = 7;

	/** Ergebnis eines Auftrags, der Archivdaten loeschen sollte. */
	public static final int DELETE_RESULT = 8;

	/** Auftrag, die Archivdaten zu sichern. */
	public static final int BACKUP = 9;

	/** Ergebnis eines Auftrags, die Archivdaten zu sichern. */
	public static final int BACKUP_RESULT = 10;

	/** Auftrag, der Archivdaten wiederherstellt. */
	public static final int RESTORE = 11;

	/** Ergebnis eines Auftrags, der Archivdaten wiederherstellen sollte. */
	public static final int RESTORE_RESULT = 12;

	/** Auftrag zur Aenderung des Loeschzeitpunkts. */
	public static final int DELETION_TIME = 13;

	/** Ergebnis eines Auftrags zur Aenderung des Löschzeitpunkts. */
	public static final int DELETION_TIME_RESULT = 14;

	/** Auftrag zum Abgleich der Verwaltungsinformationen mit einem Medium von Typ "B". */
	public static final int HEADER_RESTORE = 15;

	/** Ergebnis des Verwaltungsinformationsabgleichs. */
	public static final int HEADER_RESTORE_RESULT = 16;

	/** Auftrag zum Nachfordern */
	public static final int REQUEST_DID = 17;

	/** Ergebnis des Nachforderns. */
	public static final int REQUEST_DID_RESULT = 18;

	/** Nachforderungsauftrag für Datenidentifikationen der automatischen Nachforderung */
	public static final int REQUEST_AUTOM = 19;

	/** Ergebnis Nachforderungsauftrag */
	public static final int REQUEST_AUTOM_RESULT = 20;

	/** Anfrage Anzahl Aufträge pro Applikation */
	public static final int NUM_QUERIES_INFO = 21;

	/** Ergebnis Anzahl Aufträge pro Applikation */
	public static final int NUM_QUERIES_INFO_RESULT = 22;
}
