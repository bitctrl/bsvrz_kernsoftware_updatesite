/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.puk.config.main.simulation;

/**
 * Dieser Zustand zeigt an, dass eine Simulation gelöscht wurde, es ist der finale Zustand.
 * Dieser Zustand kann durch keinen Methodenaufruf verlassen werden, es muss ein neues Simulationsobjekt {@link ConfigSimulationObject} angelegt werden.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public class Deleted implements SimulationStates{

	public void preStart() {
	}

	public void start() {
	}

	public void stop() {
	}

	public void pause() {
	}

	public void delete() {
	}

	public void noSource() {
	}

	public void removedFromSet() {
	}

	public String toString(){
		return "Zustand: Gelöscht";
	}
}
