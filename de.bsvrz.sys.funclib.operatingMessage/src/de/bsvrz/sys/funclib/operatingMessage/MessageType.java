/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.operatingMessage.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.operatingMessage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.operatingMessage; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.operatingMessage;


/**
 * Diese Klasse stellt die beiden Zustände "System" und "Fach" für Meldungen, die sich auf systemtechnische oder
 * fachliche Zustände beziehen, bereit.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class MessageType {
	public final static MessageType SYSTEM_DOMAIN = new MessageType("System");
	public final static MessageType APPLICATION_DOMAIN = new MessageType("Fach");

	private final String _type;

	public MessageType(String type) {
		_type = type;
	}

	public String getMessageType() {
		return _type;
	}

	public String toString() {
		return _type;
	}
}
