/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.main.config.management.consistenycheck;

import java.util.*;

/**
 * Dieses Interface wird von Klassen implementiert, die das Ergebnis einer Konsistenzpr�fung enthalten (siehe TPuK1-138).
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5060 $
 */
public interface ConsistencyCheckResultInterface {

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzpr�fung zu lokalen Fehlern gekommen ist.
	 *
	 * @return true = Es ist zu lokalen Fehlern gekommen; false = Es ist zu keinem lokalen Fehler gekommen
	 */
	boolean localError();

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzpr�fung zu Interferenzfehlern gekommen ist.
	 *
	 * @return true = Es ist zu Interferenzfehlern gekommen; false = Es ist zu keinem Interferenzfehler gekommen
	 */
	boolean interferenceErrors();

	/**
	 * Die Implementierung dieser Methode gibt an, ob es bei der Konsistenzpr�fung zu Warnungen gekommen ist.
	 *
	 * @return true = Es gab Warnungen; false = Es gab keine Warnungen
	 */
	boolean warnings();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen lokalen Fehler zur�ck.
	 *
	 * @return Liste mit lokalen Fehlern. Sind keine lokalen Fehler aufgetreten, ist die Liste leer.
	 */
	List<ConsistencyCheckResultEntry> getLocalErrors();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen Interferenzfehler zur�ck.
	 *
	 * @return Liste mit allen Interferenzfehlern. Sind keine Interferenzfehler aufgetreten ist die Liste leer.
	 */
	List<ConsistencyCheckResultEntry> getInterferenceErrors();

	/**
	 * Die Implementierung dieser Methode gibt alle aufgetretenen Warnungen zur�ck.
	 *
	 * @return Warnungen, die bei der Konsistenzpr�fung aufgetreten sind. Die Liste ist leer, falls es keine Warnungen gegeben hat.
	 */
	List<ConsistencyCheckResultEntry> getWarnings();
}
