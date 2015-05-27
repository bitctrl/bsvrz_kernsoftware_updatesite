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
 * Enth�lt die Pids und Namen f�r das Skriptobjekt ({@link #type}) F�r Attributgruppen und Aspekte werden die Pids verwendet, f�r Attributlisten und Attribute
 * die Namen.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 12841 $ / $Date: 2014-10-02 13:50:44 +0200 (Thu, 02 Oct 2014) $ / ($Author: jh $)
 */
public class PidScript {

	//Skriptobjekt, siehe Kapitel 1.3.2

	public static final String type = "typ.puaSkript";

	public static final String atgAnswer = "atg.skriptAntwortSchnittstelle";

	public static final String atlMetaInformation = "Metainformation";

	public static final String atlExtendedInformation = "Erweiterteinformation";//"atl.skriptErweiterteinformation";

	public static final String atlDefaults = "Standardwerte";//"atl.skriptErweiterteinformation";

	public static final String atlDetailInformation = "Detailinformation";

	public static final String atlVariable = "VariableElemente";

	public static final String atlVersion = "VersionsHistorie";

	public static final String aspIs = "asp.ist";

	public static final String fileName = "Dateiname";

	public static final String sender = "Absender";

	public static final String requestId = "AnfrageId";

	public static final String operationCode = "OperationsCode";

	public static final String message = "Meldung";

	public static final String source = "Quellcode";


	public static final String atgRequest = "atg.skriptAnfrageSchnittstelle";

	public static final String atlBase = "Grundinformation";

	public static final String name = "Name";

	public static final String description = "Beschreibung";

	public static final String author = "Autor";

	public static final String status = "Status";

	public static final String date = "Datum";

	public static final String version = "Version";

	public static final String checksum = "Pruefsumme";

	public static final String objectType = "Objekttyp";

	public static final String attributeGroup = "Attributgruppe";

	public static final String aspect = "Aspekt";

	public static final String dateOfChange = "Aenderungsdatum";

	public static final String originator = "Urheber";

	public static final String objectSet = "PuaSkripte";

	public static final String mainObject = "Hauptobjekt";

	public static final String objects = "Objekte";

	public static final String pseudoObjects = "PseudoObjekte";

	public static final String aspectBindings = "Bindungen";

	public static final String periods = "Zeitbereiche";

	public static final String protocolType = "Protokollart";

	public static final String noChangeMarker = "Unver�ndertkennzeichnung";

	public static final String aliases = "Aliase";

	public static final String aspects = "Aspekte";

	public static final String defaults = "Standardwerte";
}
