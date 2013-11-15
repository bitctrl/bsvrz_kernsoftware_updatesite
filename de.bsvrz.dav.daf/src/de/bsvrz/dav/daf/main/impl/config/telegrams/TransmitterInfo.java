/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main.impl.config.telegrams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Diese Klasse stellt verschiedene Informationen zu einem Datenverteiler zur Verfügung. Die Informationen werden entweder im Konstruktor übergeben oder können
 * später mittels eines Byte-Stroms gesetzt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5054 $
 */
public class TransmitterInfo {

	/** Die Id des Datenverteilers */
	private long transmitterId;

	/** Die Adresse des Datenverteilers */
	private String adress;

	/** Die Subadresse des Datenverteilers */
	private int subadress;

	/**
	 * Erstellt ein Objekt legt aber die Werte TransmitterId, Adresse des Datenverteilers und die Subadresse nicht fest.
	 * <p/>
	 * Diese werden mit dem Aufruf von {@link #read(java.io.DataInputStream)} gesetzt.
	 */
	public TransmitterInfo() {
	}

	/**
	 * Legt ein Objekt an und setzt die übergebenen Parameter am Objekt.
	 *
	 * @param _transmitterId Id des Datenverteilers
	 * @param _adress        Adresse des Datenverteilers
	 * @param _subadress     Subadresse des Datenverteilers
	 */
	public TransmitterInfo(long _transmitterId, String _adress, int _subadress) {
		transmitterId = _transmitterId;
		adress = _adress;
		subadress = _subadress;
	}

	/**
	 * Gibt die Id des Datenverteilers zurück.
	 *
	 * @return Id des Datenverteilers
	 */
	public final long getTransmitterId() {
		return transmitterId;
	}

	/**
	 * Gibt die Adresse des Datenverteilers zurück.
	 *
	 * @return Adresse des Datenverteilers
	 */
	public final String getAdress() {
		return adress;
	}

	/**
	 * Gibt die Subadresse des Datenverteilers zurück
	 *
	 * @return Subadresse des Datenverteilers
	 */
	public final int getSubAdress() {
		return subadress;
	}

	/**
	 * Gibt einen String zurück, der die Id des Datenverteilers, die Adresse des Datenverteilers und die Subadresse enthält.
	 *
	 * @return s.o.
	 */
	public final String parseToString() {
		String str = "Datenverteiler Id: " + transmitterId + "\n";
		str += "Datenverteiler Adresse: " + adress + "\n";
		str += "Datenverteiler Subadresse: " + subadress + "\n";
		return str;
	}

	/**
	 * Schreibt die Informationen des Objekt in den übergebenen Stream.
	 *
	 * @param out Stream, in den die Informationen des Objekt abgelegt werden. Reihenfolge: 1) Long (TransmitterId), 2) UTF (Adresse des Datenverteilers), 3) Int
	 *            (Subadresse)
	 *
	 * @throws IOException Fehler beim schreiben der Informationen
	 */
	public final void write(DataOutputStream out) throws IOException {
		out.writeLong(transmitterId);
		out.writeUTF(adress);
		out.writeInt(subadress);
	}

	/**
	 * Ließt die Informationen des Objekts aus dem übergebenen Stream aus. Reihenfolge: 1) Long (TransmitterId), 2) UTF (Adresse des Datenverteilers), 3) Int
	 * (Subadresse)
	 *
	 * @param in Stream, aus dem die Daten in folgender Reihenfolge ausgelesen werden.
	 *
	 * @throws IOException Fehler beim lesen der Informationen
	 */
	public final void read(DataInputStream in) throws IOException {
		transmitterId = in.readLong();
		adress = in.readUTF();
		subadress = in.readInt();
	}

	public String toString() {
		return "Datenverteiler{id: " + getTransmitterId() + ", Adresse: " + getAdress() + ":" + getSubAdress() + "}";
	}
}
