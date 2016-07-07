/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.util.cmdinterface;

/**
 * Kommando das nicht im Menü angezeigt wird. Darf nur in der obersten Menüebene hinter allen "sichtbaren" Einträgen verwendet werden.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class HiddenCommand extends Command {

	private int index = 0;

	/**
	 * Kommando für {@link CmdInterpreter}
	 *
	 * @param desc  die Beschreibung
	 * @param index Index des Kommandos. Muss eindeutig sein. Es sollte eine Zahl > 100 gewählt werden, damit es zu keinen Überschneidungen mit den übrigen
	 *              Kommandos kommt.
	 */
	public HiddenCommand(String desc, int index) {
		super(desc, "");
		this.index = index;
	}

	/** @see de.bsvrz.sys.funclib.losb.util.cmdinterface.Command#getIndex() */
	public int getIndex() {
		return index;
	}

}
