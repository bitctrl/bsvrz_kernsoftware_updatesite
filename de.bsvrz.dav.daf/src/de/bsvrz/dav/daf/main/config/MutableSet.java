/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
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
 * Schnittstelle zum Zugriff auf die Eigenschaften von online änderbaren Mengen. Eine Applikation kann sich
 * auf Änderungen einer dynamischen Menge {@link #addChangeListener(MutableSetChangeListener) anmelden} und
 * auch wieder {@link #removeChangeListener(MutableSetChangeListener) abmelden}.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface MutableSet extends ObjectSet, ConfigurationCommunicationInterface, MutableCollection {

	/**
	 * Meldet einen Beobachter für die Änderungen dieser Menge an. Bei Änderungen der Menge wird die Methode
	 * {@link MutableSetChangeListener#update} des angegebenen Beobachters aufgerufen.
	 *
	 * @param listener Beobachter für Änderungen der Menge.
	 */
	public void addChangeListener(MutableSetChangeListener listener);

	/**
	 * Meldet einen Beobachter für die Änderungen dieser Menge wieder ab.
	 *
	 * @param listener Ein bisher für Änderungen der Menge angemeldeter Beobachter.
	 */
	public void removeChangeListener(MutableSetChangeListener listener);
}

