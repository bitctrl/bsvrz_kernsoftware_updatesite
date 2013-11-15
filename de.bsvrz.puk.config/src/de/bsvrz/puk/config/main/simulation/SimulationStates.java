/*
 * Copyright 2006 by Kappich Systemberatung Aachen 
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.main.simulation;

/**
 * Dieses Interface enth�lt alle Zust�nde in der sich eine Simulation, aus Sicht der Konfiguration, befinden kann.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public interface SimulationStates {

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "vorstart" �berf�hrt werden soll. */
	public void preStart();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "start" �berf�hrt werden soll. */
	public void start();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "stop" �berf�hrt werden soll. */
	public void stop();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "pause" �berf�hrt werden soll. */
	public void pause();

	/** Diese Methode wird aufgerufen, wenn die Simulation in den Zustand "gel�scht" �berf�hrt werden soll. */
	public void delete();

	/** Wird aufgerufen, wenn es keine Quelle gibt, die Daten verschicken kann. */
	public void noSource();

	/** Diese Methode wird aufgerufen, wenn die Simulation aus der Menge der Simulationen entfernt wurde. */
	public void removedFromSet();
}
