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

import java.util.*;

/**
 * Menü für den {@link de.bsvrz.sys.funclib.losb.util.cmdinterface.CmdInterpreter}
 *
 * @author beck et al. projects GmbH
 * @author Thomas Müller
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class CmdMenu {

	private CmdMenu parentMenu;

	private Vector<CmdMenu> subMenues;

	private Vector<Command> commands;

	private int index;

	private String help, description;

	/** Anzahl zusätzlicher Kommandos. */
	private int numHiddenCommands = 0;

	/**
	 * Menü mit Beschreibung und Hilfetext
	 *
	 * @param desc Beschreibung
	 * @param help Hilfetext
	 */
	public CmdMenu(String desc, String help) {
		subMenues = new Vector<CmdMenu>();
		commands = new Vector<Command>();
		this.description = (desc.equals("")) ? "Keine Beschreibung verfuegbar." : desc;
		this.help = (help.equals("")) ? "Keine Hilfe verfuegbar." : help;
	}

	/**
	 * Eltern-Menü dieses Menüs setzen
	 *
	 * @param parent das Eltern-Menü
	 */
	public void setParent(CmdMenu parent) {
		this.parentMenu = parent;
		setIndex();
	}

	/**
	 * Eltern-Menü dieses Menüs lesen
	 *
	 * @return Eltern-Menü
	 */
	public CmdMenu getParent() {
		return this.parentMenu;
	}

	/** Index des Menüs setzen (abhängig vom Eltern-Menü und Geschwister-Einträgen), ist eindeutig */
	public void setIndex() {
		if(this.parentMenu == null) {
			this.index = 0;
		}
		else {
			this.index = this.parentMenu.getSubMenues().size() + this.parentMenu.getCommands().size() + 1;
		}
	}

	/**
	 * Index des Menüs auslesen
	 *
	 * @return Index (immer eindeutig auf einer Menühierarchie-Ebene)
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Unter-Menü hinzufügen
	 *
	 * @param childMenu menü
	 */
	public void addNode(CmdMenu childMenu) {
		childMenu.setParent(this);
		this.subMenues.add(childMenu);
	}

	/**
	 * Alle Untermenüs auslesen
	 *
	 * @return Vektor mit Untermenüs
	 */
	public Vector<CmdMenu> getSubMenues() {
		return this.subMenues;
	}

	/**
	 * Ein bestimmtes Untermenü auslesen
	 *
	 * @param i Index des Untermenüs
	 *
	 * @return das Untermenü
	 */
	public CmdMenu getChildNode(int i) {
		return this.subMenues.get(i);
	}

	/**
	 * Ein Kommando hinzufügen. Fügt das Kommando vor etwaigen versteckten Kommandos ein.
	 *
	 * @param cmd das Kommando
	 *
	 * @see HiddenCommand
	 */
	public void addCmd(Command cmd) {
		if(cmd instanceof HiddenCommand) {
			commands.add(cmd);
			numHiddenCommands++;
		}
		else	//"Normales" Kommando
		{
			//Es handelt sich um einen Command -> Hinter letzten Command und ersten HiddenCommand einfügen
			int i = commands.size() - 1;
			while(i >= 0 && commands.get(i) instanceof HiddenCommand) i--;
			commands.add(i + 1, cmd);	//Hinter den letzten Command der Liste einfügen
		}
		cmd.setParent(this);
	}

	/**
	 * Alle Kommandos auslesen
	 *
	 * @return die Kommandos als Vektor
	 */
	public Vector<Command> getCommands() {
		return this.commands;
	}

	/**
	 * Ein bestimmtes Kommando
	 *
	 * @param i der Index des Kommandos
	 *
	 * @return das Kommando
	 */
	public Command getLeaf(int i) {
		return this.commands.get(i);
	}

	/**
	 * Einen Hilfetext für das Menü setzen
	 *
	 * @param help der Hilfetext
	 */
	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * Hilfe für das Menü ermitteln
	 *
	 * @return der Hilfetext
	 */
	public String getHelp() {
		return this.help;
	}

	/**
	 * Eine Beschreibung setzen
	 *
	 * @param desc die Beschreibung
	 */
	public void setDesc(String desc) {
		this.description = desc;
	}

	/**
	 * Die Beschreibung auslesen
	 *
	 * @return Beschreibung
	 */
	public String getDesc() {
		return this.description;
	}

	/**
	 * Liefert die Anzahl der zusätzlichen Kommandos.
	 *
	 * @return Liefert die Anzahl der zusätzlichen Kommandos.
	 */
	public int getNumHiddenCommands() {
		return numHiddenCommands;
	}
}
