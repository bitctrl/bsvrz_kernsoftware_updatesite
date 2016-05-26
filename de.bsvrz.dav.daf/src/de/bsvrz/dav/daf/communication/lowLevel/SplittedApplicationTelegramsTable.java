/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;

import java.util.*;


/**
 * Diese Klasse stellt einen Mechanismus zur Verfügung, der zerstückelte Datensätze zu vollständigen Datensätzen zusammenbaut und dann zur Verfügung stellt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SplittedApplicationTelegramsTable {

	/** Die Tabelle wo je nach Datum eine Liste der zerstückelten Telegramme gehalten wird. */
	private Hashtable dataTable;

	/** Erzeugt ein Objekt dieser Klasse. */
	public SplittedApplicationTelegramsTable() {
		dataTable = new Hashtable();
	}

	/**
	 * Diese Methode sammelt alle Teiltelegramme. Wurden alle Teiltelegramme empfangen, werden alle Teiltelegramme zurückgegeben.
	 *
	 * @param telegram Teiltelegramm, das ein Telegramm vervollständigen soll oder ein komplettes Telegramm, das als ganzes übergeben wurde und somit nicht
	 *                 zusammengebaut werden muss.
	 *
	 * @return Alle Teiltelegramme, aus denen ein vollständiges Telegramm rekonstruiert werden kann (und damit ein Datenatz) oder aber <code>null</code>.
	 *         <code>null</code> bedeutet, dass noch nicht alle Teiltelegramme empfangen wurden, die nötig sind um das gesamte Telegramm zusammen zu bauen.
	 *
	 * @throws IllegalArgumentException Das übergebene Telegramm konnte keinem bisher empfangenen Teil zugeordnet werden oder war <code>null</code>.
	 */
	public final ApplicationDataTelegram[] put(ApplicationDataTelegram telegram) {
		if(telegram == null) {
			throw new IllegalArgumentException("Der Parameter ist null");
		}
		int totalTelegramCount = telegram.getTotalTelegramsCount();
		int index = telegram.getTelegramNumber();
		if(index >= totalTelegramCount) {
			throw new IllegalArgumentException("Der Telegramm-Index ist grösser als die maximale Anzahl der zerstückelten Telegramme dieses Datensatzes");
		}
		if((index == 0) && (totalTelegramCount == 1)) {
			return (new ApplicationDataTelegram[]{telegram});
		}
		BaseSubscriptionInfo key = telegram.getBaseSubscriptionInfo();
		if(key == null) {
			throw new IllegalArgumentException("Das Telegramm ist inkonsistent");
		}
		Hashtable table = (Hashtable)dataTable.get(key);
		Long subKey = new Long(telegram.getDataNumber());
		if(table == null) {
			table = new Hashtable();
			ApplicationDataTelegram list[] = new ApplicationDataTelegram[totalTelegramCount];
			list[index] = telegram;
			table.put(subKey, list);
			dataTable.put(key, table);
			return null;
		}
		else {
			ApplicationDataTelegram list[] = (ApplicationDataTelegram[])table.get(subKey);
			if(list == null) {
				list = new ApplicationDataTelegram[totalTelegramCount];
				list[index] = telegram;
				table.put(subKey, list);
				return null;
			}
			else {
				synchronized(list) {
					list[index] = telegram;
					for(int i = 0; i < list.length; ++i) {
						ApplicationDataTelegram tmpTelegram = list[i];
						if(tmpTelegram == null) {
							return null;
						}
						if(i == tmpTelegram.getTelegramNumber()) {
							continue;
						}
						else {
							throw new IllegalArgumentException("Falsche Daten in der Cache-Tabelle der zerstückelten Telegramme");
						}
					}
				}
				table.remove(subKey);
				if(table.size() == 0) {
					dataTable.remove(key);
				}
				return list;
			}
		}
	}
}
