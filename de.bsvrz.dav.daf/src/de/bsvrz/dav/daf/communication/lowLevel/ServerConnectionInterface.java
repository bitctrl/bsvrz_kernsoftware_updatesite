/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.CommunicationError;

/**
 * Eine Implementierung dieses Interfaces legt fest, welches Netzwerkprotokoll die Daten zwischen Softwareeinheiten oder Rechner übertragen werden. Die
 * Funktionalität wird protokollneutral durch dieses Interface für die Server-seitig notwendigen Methoden nach Außen vertreten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public interface ServerConnectionInterface {

	/**
	 * Erstellt eine Verbindung zur lokalen Adresse mit der Nummer der Subadresse. Eine maximal erlaubte Anzahl an Verbindungen kann spezifiziert werden.
	 *
	 * @param subAdressNumber die Nummer der Subadresse
	 *
	 * @throws de.bsvrz.dav.daf.main.CommunicationError Wenn die Verbindung nicht erfolgreich erzeugt wurde.
	 */
	public void connect(int subAdressNumber) throws CommunicationError;

	/** Schließt die aktuelle Verbindung. */
	public void disconnect();

	/**
	 * Wartet auf eine Verbindungsanfrage eines Clients (passive Verbindung). Wenn die Anfrage erfolgt, wird der Repräsentant einer Verbindung erzeugt und
	 * zurückgegeben.
	 *
	 * @return Repräsentant einer Verbindung oder <code>null</code>, wenn keine Verbindung erzeugt werden konnte.
	 */
	public ConnectionInterface accept();

	/**
	 * Gibt eine Instanz der Kommunikationsklasse vom Typ {@link ConnectionInterface} des gleichen Protokolls.
	 *
	 * @return eine Instanz der Kommunikationsklasse
	 */
	public ConnectionInterface getPlainConnection();

	/**
	 * Liefert den Klassennamen der Kommunikationsklasse vom Typ {@link ConnectionInterface} des gleichen Protokolls.
	 *
	 * @return Klassenname der Kommunikationsklasse
	 */
	public String getPlainConnectionName();
}
