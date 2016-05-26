/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config;

/**
 * Schnittstelle zum Zugriff auf die Eigenschaften eines dynamischen Objektes. Dynamische Objekte haben, neben den Eigenschaften aller System-Objekte, einen
 * Zeitstempel ab dem sie gültig geworden sind und einen Zeistempel ab dem sie nicht mehr gültig sind.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface DynamicObject extends SystemObject, ConfigurationCommunicationInterface {

	/**
	 * Liefert den Zeitpunkt ab dem dieses dynamische Objekt gültig geworden ist.
	 *
	 * @return Zeit in Millisekunden seit 1970.
	 */
	public long getValidSince();

	/**
	 * Liefert den Zeitpunkt ab dem dieses dynamische Objekt nicht mehr gültig ist.
	 *
	 * @return Zeit in Millisekunden seit 1970. Wird die "0" zurückgegeben, wurde das Objekt noch nicht auf ungültig gesetzt.
	 */
	public long getNotValidSince();

	/**
	 * Methode zum Anmelden auf die Invalidierung des dynamischen Objekts. Sobald das dynamische Objekt auf invalid gesetzt wird, werden alle angemeldeten Listener
	 * informiert.
	 *
	 * @param listener Listener, der informiert wird, sobald das dynamische Objekt auf invalid gesetzt wird.
	 */
	public void addListenerForInvalidation(InvalidationListener listener);

	/**
	 * Methode zum Abmelden auf die Invalidierung des dynamischen Objekts.
	 *
	 * @param listener Listener, der nicht mehr informiert werden soll, sobald das dynamische Objekt auf invalid gesetzt wird.
	 */
	public void removeListenerForInvalidation(InvalidationListener listener);
}

