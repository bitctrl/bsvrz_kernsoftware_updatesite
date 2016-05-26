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
 * Dieses Interface enthält alle Zustände in der sich eine Simulation, aus Sicht der Konfiguration, befinden kann.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface SimulationStates {

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "vorstart" überführt werden soll. */
	public void preStart();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "start" überführt werden soll. */
	public void start();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "stop" überführt werden soll. */
	public void stop();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "pause" überführt werden soll. */
	public void pause();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "gelöscht" überführt werden soll. */
	public void delete();

	/** Wird aufgerufen, wenn es keine Quelle gibt, die Daten verschicken kann. */
	public void noSource();

	/** Diese Methode wird aufgerufen, wenn die Simulation aus der Menge der Simulationen entfernt wurde. */
	public void removedFromSet();
}
