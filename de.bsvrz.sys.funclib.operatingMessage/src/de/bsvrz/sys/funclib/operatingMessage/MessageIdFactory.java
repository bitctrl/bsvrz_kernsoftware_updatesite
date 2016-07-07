/*
 * Copyright 2016 by Kappich Systemberatung Aachen
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
 * Interface für Lambda-Ausdrücke, um eine Betriebsmeldungs-ID aus einer Betriebsmeldung zu erzeugen
 *
 * @author Kappich Systemberatung
 */
@FunctionalInterface
public interface MessageIdFactory {

	/**
	 * Erzeugt eine Meldungs-ID für eine Meldung
	 * @param message Meldungs-Objekt für das eine Meldungs-ID gebildet werden soll. Die Meldungs-ID ist noch nicht gesetzt
	 * @return ID, die gesetzt werden soll
	 */
	String generateMessageId(OperatingMessage message);
	
}
