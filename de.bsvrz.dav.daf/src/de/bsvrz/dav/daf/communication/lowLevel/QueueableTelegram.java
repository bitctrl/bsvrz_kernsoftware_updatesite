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

/**
 * Definiert eine Schnittstelle für Telegramme mit Priorität und Größe.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see TelegramQueue
 */
public interface QueueableTelegram {

	/**
	 * Bestimmt die Priorität des Telegramms. Je größer der Wert, desto größer die Priorität.
	 *
	 * @return Priorität des Telegramms.
	 */
	byte getPriority();

	/**
	 * Bestimmt die Gesamtgröße des Telegramms einschließlich Headerinformationen.
	 *
	 * @return Gesamtgröße des Telegramms in Bytes.
	 */
	int getSize();
}
