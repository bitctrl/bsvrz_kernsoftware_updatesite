/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.commandLineArgs.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.commandLineArgs; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.commandLineArgs;

/**
 * Wertklasse, die eine abstrakte Kommunikationsadresse speichert.
 * Ein abstrakte Kommunikationsadresse besteht aus einem Protokollnamen (z.B. "tcp"),
 * aus einer Geräteadresse (z.B IP-Adresse oder Domainname eines Rechners) und
 * einer Subadresse (z.B. TCP Portnummer).
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CommunicationAddress {
	/** Speichert den Namen des Kommunikationsprotokolls.
	 */
	private final String _protocol;

	/** Speichert die Geräteadresse.
	 */
	private final String _address;

	/** Speichert die Subadresse.
	 */
	private final int _subAddress;

	/** Erzeugt ein neues Objekt der Klasse CommunicationAddress.
	 * @param protocol  Name des Kommunikationsprotokolls (z.B. "tcp").
	 * @param address  Geräteaddresse (z.B IP-Adresse oder Domainname eines Rechners).
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
	 * Bestimmt die Geräteadresse dieser Kommunikationsadresse.
	 * @return Geräteadresse.
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
