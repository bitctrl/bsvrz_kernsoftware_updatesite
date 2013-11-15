/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Eine Implementierung dieses Interfaces legt fest, welches Netzwerkprotokoll die Daten zwischen Softwareeinheiten oder Rechner �bertragen werden. Die
 * Funktionalit�t wird protokollneutral durch dieses Interface f�r die Client-seitig notwendigen Methoden nach Au�en vertreten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 * @see ServerConnectionInterface
 */
public interface ConnectionInterface {

	/**
	 * Gibt die Hauptadresse der Verbindung zur�ck.
	 *
	 * @return Die Hauptadresse der Verbindung als Zeichenkette oder <code>null</code>, wenn die Adresse nicht bekannt ist.
	 */
	public String getMainAdress();

	/**
	 * Gibt die Nummer der Subadresse der Verbindung zur�ck.
	 *
	 * @return Die Nummer der Subadresse der Verbindung oder <code>0</code>, wenn der keine Verbindung besteht oder <code>-1</code>, wenn die Adresse nicht bekannt
	 *         ist.
	 */
	public int getSubAdressNumber();

	/**
	 * Gibt den Stream des Sendekanals zur�ck, auf den die Datens�tze geschrieben werden.
	 *
	 * @return Der Stream des Sendekanals oder <code>null</code>, wenn die Verbindung nicht zur Verf�gung steht.
	 */
	public OutputStream getOutputStream();

	/**
	 * Gibt den Stream des Empfangskanals zur�ck, von dem die Datens�tze gelesen werden.
	 *
	 * @return Der Stream des Empfangskanals oder <code>null</code>, wenn die Verbindung nicht zur Verf�gung steht.
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

	/** Schlie�t die aktuelle Verbindung. */
	public void disconnect();

	/**
	 * Gibt die Information �ber den Verbindungsstatus zur�ck.
	 *
	 * @return <code>true</code>, falls eine Verbindung aufgebaut wurde, sonst <code>false</code>.
	 */
	public boolean isConnected();
}
