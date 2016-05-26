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

package de.bsvrz.dav.daf.main;

/**
 * Ausnahme, die beim Senden von Daten als einfacher Sender generiert wird, wenn noch keine positive Sendesteuerung vom Datenverteiler für die zu versendenden
 * Daten vorliegt.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SendSubscriptionNotConfirmed extends Exception {

	/**
	 * Erzeugt eine neue Ausnahme dieser Klasse.
	 *
	 * @param message Fehlerbeschreibung.
	 */
	public SendSubscriptionNotConfirmed(String message) {
		super(message);
	}
}
