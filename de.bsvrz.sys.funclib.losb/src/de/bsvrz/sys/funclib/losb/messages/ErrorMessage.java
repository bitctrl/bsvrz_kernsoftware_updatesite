/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.messages;


/**
 * Diverse Fehlermeldungen für Protokolle und Auswertungen.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ErrorMessage {

	public static final String COMMUNICATION = "Fehler bei der Kommunikation mit der Konfiguration. ";

	public static final String NO_DATA = "Keine Daten. ";

	public static final String MULIPLE_SUBSCRIPTIONS = "Es existiert bereits eine Sendeanmeldung für diese Datenidentifikation: ";

	public static final String SENDING_NOT_ALLOWED = "Keine positive Sendesteuerung erhalten. ";

	public static final String WRONG_OBJECT_SERIALIZED = "Unerwartetes Objekt im Datenstrom: ";

	public static final String INTERRUPTED_EXCEPTION = "Beende Thread: ";

	public static final String CAN_NOT_GET_PROTOCOL_ID = "Kann Protokoll-Id nicht lesen: ";

	public static final String CAN_NOT_GET_JOB_ID = "Kann Auftrags-Id nicht lesen: ";

	public static final String CAN_NOT_GET_JOB_LIST = "Kann Auftragsliste nicht abrufen: ";

	public static final String CAN_NOT_SERIALIZE = "Kann Daten nicht serialisieren: ";

	public static final String CAN_NOT_DESERIALIZE = "Kann Daten nicht deserialisieren: ";

	public static final String UNEXPECTED_DATA_TYPE = "Unerwarteter Datentyp: ";

	public static final String CAN_NOT_ACCESS_FILE = "Kann auf Datei nicht zugreifen: ";

	public static final String INVALID_PID = "Ungültige PID: ";

	public static final String FILE_NOT_FOUND = "Datei existiert nicht: ";

	public static final String CAN_NOT_SEND = "Fehler beim Senden: ";

	public static final String CAN_NOT_SUBSCRIBE = "Fehler bei der Sendeanmeldung: ";

	public static final String CAN_NOT_UNSUBSCRIBE = "Fehler bei der Sendeabmeldung: ";

	public static final String IS_NULL_OR_EMPTY = "Parameter ist null oder leer. ";

	public static final String CAN_NOT_CREATE_SCRIPT_OBJECT = "Kann Skript nicht erstellen";

	public static final String TIMEOUT = "Timeout der Operation.";

	public static final String PARAM_IS_NULL = "Parameter ist null: ";

	public static final String INVALID_PARAM_VALUE = "Ungültiger Parameterwert: ";

	public static final String INCOMPATIBLE_TYPES = "Typen stimmen nicht überein: ";

	public static final String NO_PERIODS = "Es wurde kein Zeitbereich angegeben.";

	public static final String OLD_DATAMODEL = "Der Auftrag konnte aufgrund eines veralteten Datenmodells nicht ausgeführt werden. Benötigt wird kb.tmVewProtokolleGlobal in mindestens Version 5.";

	public static final String NULL = "Es wurde ein null-Wert verwendet!";

	public static final String MULTIPLE_OBJECTS = "Es kann pro Typ nur ein Objekt definiert werden, aber es wurden mehrere Objekte angegeben: ";

	public static final String INVALID_PERIOD = "Ungültiger Zeitbereich";
}
