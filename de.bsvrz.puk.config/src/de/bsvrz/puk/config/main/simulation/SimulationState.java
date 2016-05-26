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
 * Stellt alle Zustände dar, die eine Simulation annehmen kann.
 *
 * @author Achim Wullenkord (AW), Kappich Systemberatung
 * @version $Revision:5077 $ / $Date:2007-09-02 14:48:31 +0200 (So, 02 Sep 2007) $ / ($Author:rs $)
 */
public enum SimulationState {
	NEW("neu",0),PRESTART("Vorstart",1), START("Start",2), STOP("Stop",3), DELETED("gelöscht",4), PAUSE("Pause",5);

	public static final SimulationState getInstance(final String state)
	{
		if(NEW.getState().equals(state))
		{
			return NEW;
		}else if(PRESTART.getState().equals(state))
		{
			return PRESTART;
		}else if(START.getState().equals(state))
		{
			return START;
		}else if(STOP.getState().equals(state))
		{
			return STOP;
		}else if(DELETED.getState().equals(state))
		{
			return DELETED;
		}else if(PAUSE.getState().equals(state))
		{
			return PAUSE;
		}else
		{
			throw new IllegalArgumentException("Der String " + state + " kann in keinen Zustand umgewandelt werden.");
		}
	}

	public static final SimulationState getInstance(final int code)
	{
		switch(code){
			case 0 : return NEW;
			case 1 : return PRESTART;
			case 2 : return START;
			case 3 : return STOP;
			case 4 : return DELETED;
			case 5 : return PAUSE;
			default : throw new IllegalArgumentException("Die Zahl " + code + " kann in keinen Zustand umgewandelt werden.");
		}
	}

	private final String _state;
	private final int _code;
	private SimulationState(String state, int code) {
		_state = state;
		_code = code;
	}

	public String getState()
	{
		return _state;
	}

	public int getCode()
	{
		return _code;
	}

	public String toString()
	{
		return _state + "(" + _code+ ")";
	}
}
