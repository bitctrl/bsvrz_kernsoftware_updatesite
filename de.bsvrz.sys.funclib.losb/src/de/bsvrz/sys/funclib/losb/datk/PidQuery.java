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
 * Enthaelt die PIDs und Namen fuer die Archiv-Anfrage-Schnittstelle als Konstanten.
 *
 * @author beck et al. projects GmbH
 * @author Thomas Schaefer
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class PidQuery {

	/** PID der Attributgruppe ArchivAnfrageSchnittstelle. */
	public static final String ATG_QUERY_PID = "atg.archivAnfrageSchnittstelle";

	/** PID des Anfrage-Aspekts bei der Attributgruppe ArchivAnfrageSchnittstelle. */
	public static final String ASP_QUERY_PID = "asp.anfrage";

	/** PID des Antwort-Aspekts bei der Attributgruppe ArchivAnfrageSchnittstelle. */
	public static final String ASP_RESPONSE_PID = "asp.antwort";

	/** Name des Attributs mit Referenz auf den Absender eines Datensatzes. */
	public final static String ATT_SENDER_NAME = "absender";

	/** Name des Attributs mit dem Anfrage-Index zur Unterscheidung mehrerer paralleler Anfragen. */
	public final static String ATT_QUERY_IDX_NAME = "anfrageIndex";

	/** Name des Attributs mit dem Typ der Nachricht. */
	public final static String ATT_MESSAGE_TYP_NAME = "nachrichtenTyp";

	/** Name des Attributs mit den Datenbytes der Nachricht. */
	public final static String ATT_DATA_NAME = "daten";
}
