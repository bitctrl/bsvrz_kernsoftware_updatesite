/*
 * Copyright 2008 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.config;


/**
 * Schnittstelle zum Zugriff auf die Eigenschaften von online �nderbaren Mengen. Eine Applikation kann sich
 * auf �nderungen einer dynamischen Menge {@link #addChangeListener(MutableSetChangeListener) anmelden} und
 * auch wieder {@link #removeChangeListener(MutableSetChangeListener) abmelden}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5982 $
 */
public interface MutableSet extends ObjectSet, ConfigurationCommunicationInterface, MutableCollection {

	/**
	 * Meldet einen Beobachter f�r die �nderungen dieser Menge an. Bei �nderungen der Menge wird die Methode
	 * {@link MutableSetChangeListener#update} des angegebenen Beobachters aufgerufen.
	 *
	 * @param listener Beobachter f�r �nderungen der Menge.
	 */
	public void addChangeListener(MutableSetChangeListener listener);

	/**
	 * Meldet einen Beobachter f�r die �nderungen dieser Menge wieder ab.
	 *
	 * @param listener Ein bisher f�r �nderungen der Menge angemeldeter Beobachter.
	 */
	public void removeChangeListener(MutableSetChangeListener listener);
}

