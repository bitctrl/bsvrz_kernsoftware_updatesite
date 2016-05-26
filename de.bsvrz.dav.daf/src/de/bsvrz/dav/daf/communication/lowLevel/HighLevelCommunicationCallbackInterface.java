/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;

/**
 * Schnittstelle mit der Informationen der unteren Kommunikationsschicht an eine höhere Kommunikationsschicht weitergegeben werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface HighLevelCommunicationCallbackInterface {

	/**
	 * Wird von der unteren Kommunikationsschicht nach Empfang eines Telegramms aufgerufen.
	 *
	 * @param telegram Das empfangene Telegramm
	 * @throws InterruptedException Wenn der Thread während eines blockierenden Aufrufs unterbrochen wurde
	 */
	public void update(DataTelegram telegram) throws InterruptedException;

	/**
	 * Wird von der unteren Kommunikationsschicht in Fehlersituationen zum Abbruch der Kommunikationsverbindung aufgerufen.
	 *
	 * @param error   <code>true</code> signalisiert eine Fehlersituation der unteren Kommunikationsschicht.
	 * @param message Fehlermeldung, die die Fehlersituation näher beschreibt.
	 */
	public void disconnected(boolean error, final String message);

	/**
	 * Wenn ein neues Konfigurationstelegramm angekommen ist, wird es an die Verwaltung weitergeleitet.
	 *
	 * @param receivedData das Konfigurationstelegramm
	 */
	void updateConfigData(SendDataObject receivedData);
}
