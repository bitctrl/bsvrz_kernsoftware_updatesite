/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.onlprot.
 * 
 * de.bsvrz.pat.onlprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.onlprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.onlprot.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.onlprot.standardProtocolModule;

import de.bsvrz.dav.daf.main.ResultData;

/**
 * Schnittstelle, die das <code>ClientProtocollerInterface</code> erweitert und benutzt werden
 * kann um Protokolle von empfangenen Telegrammen zu erzeugen und dabei dem Anwender die
 * Möglichkeit gibt, die im erstellten Protokoll zu verwendenden Zeitstempel zu beeinflussen.
 * <p>
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ExtendedProtocollerInterface extends ClientProtocollerInterface {
	/**
	 * Gibt einen Protokollkopf aus. Die evtl. im Kopf ausgegebene Startzeit kann
	 * durch einen entsprechenden Parameter vorgegben werden.
	 *
	 * @param args Kommandozeilenargumente, die evtl. im Kopf des Protokolls ausgegeben werden.
	 * @param startTime Startzeit in Millisekunden seit 1970, die evtl. im Kopf des Protokoll ausgegeben wird.
	 */
	void writeHeader(String[] args, long startTime);

	/**
	 * Block mit mehreren Telegrammen ausgeben.
	 *
	 * @param	results		Ergebnisdatensätzen, die ausgegeben werden sollen.
	 * @param	time    	Zeitangabe in Millisekunden seit 1970, die evtl. im Protokoll ausgegeben wird.
	 */
	void writeBlock(ResultData[] results, long time);
}
