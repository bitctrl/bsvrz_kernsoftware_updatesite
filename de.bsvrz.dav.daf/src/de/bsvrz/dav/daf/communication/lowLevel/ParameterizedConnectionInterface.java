/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.CommunicationError;

/**
 * Eine Implementierung der Interfaces ServerConnectionInterface und ConnectionInterface, die zusätzlich dieses Interfaces implementiert, verwaltet und
 * berücksichtigt zusätzliche Einstellungen, die über die Methoden setParameters() und getParameters() gesetzt bzw. abgefragt werden können.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface ParameterizedConnectionInterface {

	/**
	 * Setzt zusätzliche Einstellungen, die von der Implementierung berücksichtigt werden sollen. Der Aufbau und die Bedeutung der Parameter wird durch die
	 * konkreten Implementierungen dieses Interfaces selbst festgelegt.
	 * @param parameters Einstellungen, die von der Implementierung berücksichtigt werden sollen.
	 */
	void setParameters(String parameters);

	/**
	 * Liefert die zusätzliche Einstellungen, die von der Implementierung berücksichtigt werden sollen. Der Aufbau und die Bedeutung der Parameter wird durch
	 * die konkreten Implementierungen dieses Interfaces selbst festgelegt.
	 * @return Einstellungen, die von der Implementierung berücksichtigt werden sollen.
	 */
	String getParameters();

}
