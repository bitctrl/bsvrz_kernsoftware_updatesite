/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.commandLineArgs.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.commandLineArgs; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.commandLineArgs;

/**
 * Wertklasse, die eine abstrakte Kommunikationsadresse speichert.
 * Ein abstrakte Kommunikationsadresse besteht aus einem Protokollnamen (z.B. "tcp"),
 * aus einer Ger‰teadresse (z.B IP-Adresse oder Domainname eines Rechners) und
 * einer Subadresse (z.B. TCP Portnummer).
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5005 $
 */
public class CommunicationAddress {
	/** Speichert den Namen des Kommunikationsprotokolls.
	 */
	private final String _protocol;

	/** Speichert die Ger‰teadresse.
	 */
	private final String _address;

	/** Speichert die Subadresse.
	 */
	private final int _subAddress;

	/** Erzeugt ein neues Objekt der Klasse CommunicationAddress.
	 * @param protocol  Name des Kommunikationsprotokolls (z.B. "tcp").
	 * @param address  Ger‰teaddresse (z.B IP-Adresse oder Domainname eines Rechners).
	 * @param subAdress  Subadresse (z.B. TCP Portnummer).
	 */
	public CommunicationAddress(String protocol, String address, int subAdress) {
		_protocol= protocol;
		_address= address;
		_subAddress= subAdress;
	}

	/**
	 * Bestimmt den Protokollnamen dieser Kommunikationsadresse.
	 * @return Protokollname.
	 */
	public String getProtocol() {
		return _protocol;
	}

	/**
	 * Bestimmt die Ger‰teadresse dieser Kommunikationsadresse.
	 * @return Ger‰teadresse.
	 */
	public String getAddress() {
		return _address;
	}

	/**
	 * Bestimmt die Subadresse dieser Kommunikationsadresse.
	 * @return Subadresse
	 */
	public int getSubaddress() {
		return _subAddress;
	}
}
