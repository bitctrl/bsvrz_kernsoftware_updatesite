/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniﬂ Systemberatung, Aachen
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

package de.bsvrz.dav.daf.main;

/**
 * Schnittstelle, die seitens der Applikation zu implementieren ist, um Aktualisierungen von Daten, die zum Empfang angemeldet sind, zu verarbeiten.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 * @see ClientDavInterface#subscribeReceiver
 */
public interface ClientReceiverInterface {

	/**
	 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird. Diese Methode muss
	 * von der Applikation zur Verarbeitung der empfangenen Datens‰tze implementiert werden.
	 *
	 * @param results Feld mit den empfangenen Ergebnisdatens‰tzen.
	 */
	public void update(ResultData results[]);
}

