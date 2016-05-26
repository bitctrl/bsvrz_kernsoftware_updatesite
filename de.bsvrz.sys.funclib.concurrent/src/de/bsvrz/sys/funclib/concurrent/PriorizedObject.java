/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.concurrent.
 * 
 * de.bsvrz.sys.funclib.concurrent is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.concurrent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.concurrent; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.concurrent;

/**
 * Schnittstelle, die von priorisierten Objekten implementiert werden muss.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see PriorityChannel
 */
public interface PriorizedObject {

	/**
	 * Liefert die Prioritätsklasse des Objektes zurück. Der Wert 1 entspricht dabei der Klasse mit der höchsten Priorität. Größere Werte kennzeichnen
	 * Prioritätsklassen mit niedrigerer Priorität.
	 *
	 * @return Prioritätsklasse als positive Zahl.
	 */
	public int getPriorityClass();
}

