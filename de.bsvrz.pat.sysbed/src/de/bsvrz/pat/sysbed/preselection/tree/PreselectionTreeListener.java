/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.preselection.tree;

import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Listener zum Anmelden bei einem Objekt der Klasse {@link PreselectionTree}.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see PreselectionTreeListener#setObjects(java.util.Collection)
 */
public interface PreselectionTreeListener {

	/**
	 * Methode zum Übergeben der Systemobjekte (z.B. an das PreselectionLists-Panel)
	 *
	 * @param systemObjects die zu übergebenden Systemobjekte
	 */
	void setObjects(Collection<SystemObject> systemObjects);
}
