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

package de.bsvrz.sys.funclib.losb.messages;


/**
 * Diverse Fehlermeldungen f�r Protokolle und Auswertungen.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 8878 $ / $Date: 2011-03-09 15:36:58 +0100 (Mi, 09 Mrz 2011) $ / ($Author: rs $)
 */
public class ErrorMessage {

	public static final String COMMUNICATION = "Fehler bei der Kommunikation mit der Konfiguration. ";

	public static final String NO_DATA = "Keine Daten. ";

	public static final String MULIPLE_SUBSCRIPTIONS = "Es existiert bereits eine Sendeanmeldung f�r diese Datenidentifikation: ";

	public static final String SENDING_NOT_ALLOWED = "Keine positive Sendesteuerung erhalten. ";

	public static final String WRONG_OBJECT_SERIALIZED = "Unerwartetes Objekt im Datenstrom: ";

	public static final String INTERRUPTED_EXCEPTION = "Beende Thread: ";

	public static final String CAN_NOT_GET_PROTOCOL_ID = "Kann Protokoll-Id nicht lesen: ";

	public static final String CAN_NOT_GET_SCRIPT_ID = "Kann Script-Id nicht lesen: ";

	public static final String CAN_NOT_SERIALIZE = "Kann Daten nicht serialisieren: ";

	public static final String CAN_NOT_DESERIALIZE = "Kann Daten nicht deserialisieren: ";

	public static final String UNEXPECTED_DATA_TYPE = "Unerwarteter Datentyp: ";

	public static final String CAN_NOT_ACCESS_FILE = "Kann auf Datei nicht zugreifen: ";

	public static final String INVALID_PID = "Ung�ltige PID: ";

	public static final String FILE_NOT_FOUND = "Datei existiert nicht: ";

	public static final String CAN_NOT_SEND = "Fehler beim Senden: ";

	public static final String CAN_NOT_SUBSCRIBE = "Fehler bei der Sendeanmeldung: ";

	public static final String CAN_NOT_UNSUBSCRIBE = "Fehler bei der Sendeabmeldung: ";

	public static final String IS_NULL_OR_EMPTY = "Parameter ist null oder leer. ";

	public static final String INVALID_ID = "Ung�ltige Id. ";

	public static final String CAN_NOT_CREATE_SCRIPT_OBJECT = "Kann Skript nicht erstellen";

	public static final String TIMEOUT = "Timeout der Operation.";

	public static final String PARAM_IS_NULL = "Parameter ist null: ";

	public static final String INVALID_PARAM_VALUE = "Ung�ltiger Parameterwert: ";

	public static final String MULTIPLE_RECEIVER_SUBSCRIPTIONS = "Mehrfachanmeldung von ";

	public static final String INCOMPATIBLE_TYPES = "Typen stimmen nicht �berein: ";

	public static final String NO_PERIODS = "Es wurde kein Zeitbereich angegeben.";

	public static final String OLD_DATAMODEL = "Der Auftrag konnte aufgrund eines veralteten Datenmodells nicht ausgef�hrt werden. Ben�tigt wird kb.tmVewProtokolleGlobal in mindestens Version 4.";

	public static final String NULL = "Es wurde ein null-Wert verwendet!";

	public static final String MULTIPLE_OBJECTS = "Es kann pro Typ nur ein Objekt definiert werden, aber es wurden mehrere Objekte angegeben: ";

	public static final String INVALID_PERIOD = "Ung�ltiger Zeitbereich";
}
