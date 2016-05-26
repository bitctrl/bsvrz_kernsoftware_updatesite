/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config.management.consistenycheck;

import java.util.*;

/**
 * Dieses Interface wird von Klassen implementiert, die das Ergebnis einer Konsistenzprüfung enthalten (siehe TPuK1-138).
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ConsistencyCheckResultInterface {

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzprüfung zu lokalen Fehlern gekommen ist.
	 *
	 * @return true = Es ist zu lokalen Fehlern gekommen; false = Es ist zu keinem lokalen Fehler gekommen
	 */
	boolean localError();

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzprüfung zu Interferenzfehlern gekommen ist.
	 *
	 * @return true = Es ist zu Interferenzfehlern gekommen; false = Es ist zu keinem Interferenzfehler gekommen
	 */
	boolean interferenceErrors();

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzprüfung zu Warnungen gekommen ist.
	 *
	 * @return true = Es gab Warnungen; false = Es gab keine Warnungen
	 */
	boolean warnings();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen lokalen Fehler zurück.
	 *
	 * @return Liste mit lokalen Fehlern. Sind keine lokalen Fehler aufgetreten, ist die Liste leer.
	 */
	List<ConsistencyCheckResultEntry> getLocalErrors();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen Interferenzfehler zurück.
	 *
	 * @return Liste mit allen Interferenzfehlern. Sind keine Interferenzfehler aufgetreten ist die Liste leer.
	 */
	List<ConsistencyCheckResultEntry> getInterferenceErrors();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen Warnungen zurück.
	 *
	 * @return Warnungen, die bei der Konsistenzprüfung aufgetreten sind. Die Liste ist leer, falls es keine Warnungen gegeben hat.
	 */
	List<ConsistencyCheckResultEntry> getWarnings();
}
