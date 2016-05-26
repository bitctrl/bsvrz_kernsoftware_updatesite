/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse ist die Basisklasse für System-Anfragetelegramme. Hier werden die Typen der Telegramme festgelegt und Methoden zur Bearbeitung deklariert.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public abstract class SystemObjectRequestInfo {

	/** Die Typen */
	public static final byte IDS_TO_OBJECTS_TYPE = 1;

	/** Anfragetelegramm für permanente IDs */
	public static final byte PIDS_TO_OBJECTS_TYPE = 2;

	/** Anfagetelegramm für Objekte gleichen Typs */
	public static final byte TYPE_IDS_TO_OBJECTS_TYPE = 3;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_IDS_TO_OBJECTS_TYPE = 4;

	/** Typ der Anfrage */
	protected byte _requestType;

	public SystemObjectRequestInfo() {
	}

	/**
	 * Gibt den Typ zurück
	 *
	 * @return Typ des Telegramms
	 */
	public final byte getRequestType() {
		return _requestType;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts für Debug-Zwecke zurück.
	 *
	 * @return Beschreibender Text dieses Objekts.
	 */
	public abstract String parseToString();

	/**
	 * Deserialisiert dieses Objekt.
	 *
	 * @param in Stream von dem das Objekt gelesen werden soll.
	 *
	 * @throws IOException, wenn beim Lesen vom Eingabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void read(DataInputStream in) throws IOException;

	/**
	 * Serialisiert dieses Objekt.
	 *
	 * @param out Stream auf den das Objekt geschrieben werden soll.
	 *
	 * @throws IOException, wenn beim Schreiben auf den Ausgabe-Stream Fehler aufgetreten sind.
	 */
	public abstract void write(DataOutputStream out) throws IOException;
}
