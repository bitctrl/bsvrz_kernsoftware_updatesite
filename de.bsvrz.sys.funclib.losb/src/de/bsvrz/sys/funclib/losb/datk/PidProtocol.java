/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.datk;

/**
 * Enthält die Pids für Protokoll-bezogene Anfragen
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class PidProtocol {

	//Siehe 1.3.3, Pids / Namen für protokollbezogene Anfragen
	public static final String atgProtocolRequest = "atg.puaProtokollAnfrageSchnittstelle";

	public static final String sender = "Absender";

	public static final String protocolId = "Protokoll-Id";

	public static final String opCode = "Operationscode";

	public static final String data = "Daten";

	public static final String aspAnswer = "asp.antwort";

	public static final String aspRequest = "asp.anfrage";
}
