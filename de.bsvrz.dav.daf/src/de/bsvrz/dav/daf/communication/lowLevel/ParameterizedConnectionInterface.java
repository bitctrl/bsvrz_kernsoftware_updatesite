/*
 * Copyright 2007 by Kappich Systemberatung Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

import de.bsvrz.dav.daf.main.CommunicationError;

/**
 * Eine Implementierung der Interfaces ServerConnectionInterface und ConnectionInterface, die zusätzlich dieses Interfaces implementiert, verwaltet und
 * berücksichtigt zusätzliche Einstellungen, die über die Methoden setParameters() und getParameters() gesetzt bzw. abgefragt werden können.
 * @author Kappich Systemberatung
 * @version $Revision: 7692 $
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
