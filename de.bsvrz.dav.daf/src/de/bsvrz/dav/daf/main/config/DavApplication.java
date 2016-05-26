/*
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

import java.util.List;

/**
 * Schnittstellenklasse zum Zugriff auf die Eigenschaften eines Datenverteiler-Objekts. Jeder Datenverteiler
 * erzeugt ein solches dynamisches Objekt für sich selbst.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @author Stephan Homeyer (sth)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public interface DavApplication extends ConfigurationObject {
	/**
	 * Liefert die mit dem Datenverteiler verbundenen Applikationen zurück. Diese werden vom jeweiligen
	 * Datenverteiler in der ihm zugeordneten Menge mit dem Namen "Applikationen" verwaltet.
	 *
	 * @return Liste von {@link ClientApplication Applikations-Objekten}
	 *
	 * @deprecated Wurde ersetzt durch die Methode {@link #getClientApplicationSet()}.
	 */
	@Deprecated
	public List<ClientApplication> getClientApplications();

	/**
	 * Liefert die mit dem Datenverteiler verbundenen Applikationen in einer dynamischen Menge zurück. Diese
	 * werden vom jeweiligen Datenverteiler in der ihm zugeordneten Menge mit dem Namen "Applikationen"
	 * verwaltet. Durch Abfrage der {@link MutableSet#getElements Elemente} gelangt man an die Applikationen.
	 *
	 * @return Dynamische Menge mit Namen "Applikationen" oder <code>null</code>, wenn die Menge nicht vorhanden
	 *         ist.
	 */
	public MutableSet getClientApplicationSet();
}
