/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.application;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Schnittstelle, die von einer Applikation implementiert werden muss, wenn der StandardApplicationRunner-Mechanismus verwendet werden soll.
 * in der main-Methode der Applikation sollte die Methode {@link StandardApplicationRunner#run} aufgerufen werden.
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public interface StandardApplication {

	/**
	 * Beim Aufruf dieser Methode bekommt die Applikation die Möglichkeit spezielle Aufrufargumente zu lesen.
	 * @param argumentList Aufrufargumente der Applikation
	 * @throws Exception Falls ein unerwarteter Fehler aufgetreten ist.
	 */
	void parseArguments(ArgumentList argumentList) throws Exception;

	/**
	 * Diese Methode wird nach dem Verbindungsaufbau zum Datenverteiler aufgerufen. Die Applikation bekommt damit die Möglichkeit sich zu initialisieren.
	 * @param connection Verbindung zum Datenverteiler
	 * @throws Exception Falls ein unerwarteter Fehler aufgetreten ist.
	 */
	void initialize(ClientDavInterface connection) throws Exception;

}
