/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Eine Implementierung dieses Interfaces legt fest, welches Netzwerkprotokoll die Daten zwischen Softwareeinheiten oder Rechner übertragen werden. Die
 * Funktionalität wird protokollneutral durch dieses Interface für die Client-seitig notwendigen Methoden nach Außen vertreten.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see ServerConnectionInterface
 */
public interface ConnectionInterface {

	/**
	 * Gibt die Hauptadresse der Verbindung zurück.
	 *
	 * @return Die Hauptadresse der Verbindung als Zeichenkette oder <code>null</code>, wenn die Adresse nicht bekannt ist.
	 */
	public String getMainAdress();

	/**
	 * Gibt die Nummer der Subadresse der Verbindung zurück.
	 *
	 * @return Die Nummer der Subadresse der Verbindung oder <code>0</code>, wenn der keine Verbindung besteht oder <code>-1</code>, wenn die Adresse nicht bekannt
	 *         ist.
	 */
	public int getSubAdressNumber();

	/**
	 * Gibt den Stream des Sendekanals zurück, auf den die Datensätze geschrieben werden.
	 *
	 * @return Der Stream des Sendekanals oder <code>null</code>, wenn die Verbindung nicht zur Verfügung steht.
	 */
	public OutputStream getOutputStream();

	/**
	 * Gibt den Stream des Empfangskanals zurück, von dem die Datensätze gelesen werden.
	 *
	 * @return Der Stream des Empfangskanals oder <code>null</code>, wenn die Verbindung nicht zur Verfügung steht.
	 */
	public InputStream getInputStream();

	/**
	 * Erzeugt eine Verbindung mit der spezifizierten Hauptadresse und der Nummer der Subadresse.
	 *
	 * @param mainAdress      die Hauptadresse der Verbindung
	 * @param subAdressNumber die Nummer der Subadresse der Verbindung
	 *
	 * @throws de.bsvrz.dav.daf.main.ConnectionException Wenn die Verbindung nicht erfolgreich erzeugt werden konnte.
	 */
	public void connect(String mainAdress, int subAdressNumber) throws ConnectionException;

	/** Schließt die aktuelle Verbindung. */
	public void disconnect();

	/**
	 * Gibt die Information über den Verbindungsstatus zurück.
	 *
	 * @return <code>true</code>, falls eine Verbindung aufgebaut wurde, sonst <code>false</code>.
	 */
	public boolean isConnected();
}
