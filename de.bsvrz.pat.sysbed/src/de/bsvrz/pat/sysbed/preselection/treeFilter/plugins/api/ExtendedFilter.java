/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.preselection.treeFilter.plugins.api;


import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Das Interface <code>ExtendedFilter</code> ermöglicht das Hinzufügen eines weiteren Filters.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5055 $
 */
public interface ExtendedFilter {

	/**
	 * Übergibt die Werte, nach denen gefiltert werden soll. Der erste Wert entspricht der aufzurufenden Klasse.
	 *
	 * @param values die Werte, nach denen gefiltert werden soll
	 */
	void setValues(String[] values);

	/**
	 * Übergibt die Verbindung zum Datenverteiler.
	 *
	 * @param connection die Verbindung zum Datenverteiler
	 */
	void setConnection(ClientDavInterface connection);

	/**
	 * Die übergebenen Systemobjekte werden mittels der Methode applyFilter gefiltert und wieder zurückgegeben.
	 *
	 * @param systemObjects die System-Objekte
	 *
	 * @return die gefilterten System-Objekte
	 */
	Collection<SystemObject> applyFilter(Collection<SystemObject> systemObjects);
}
