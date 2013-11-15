/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse ist die Basisklasse f�r System-Antworttelegramme. Hier werden die Typen der Telegramme festgelegt und Methoden zur Bearbeitung deklariert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public abstract class SystemObjectAnswerInfo {

	/** Die Typen */
	public static final byte IDS_TO_OBJECTS_TYPE = 1;

	/** Antworttelegramm f�r PIDs zu Objekten */
	public static final byte PIDS_TO_OBJECTS_TYPE = 2;

	/** Antworttelegramm f�r Objekte gleichen Typs */
	public static final byte TYPE_IDS_TO_OBJECTS_TYPE = 3;

	/** @deprecated Wird nicht mehr verwendet */
	@Deprecated
	public static final byte SET_IDS_TO_OBJECTS_TYPE = 4;

	/** Typ der Antwort */
	protected byte _answerType;

	/**
	 * Gibt den Typ zur�ck
	 *
	 * @return Typ des Telegramms
	 */
	public final byte getAnswerType() {
		return _answerType;
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts f�r Debug-Zwecke zur�ck.
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
