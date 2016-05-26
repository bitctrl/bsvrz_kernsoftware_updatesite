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

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Abstrakte Klasse für Kommandos des {@link de.bsvrz.sys.funclib.losb.util.cmdinterface.CmdInterpreter}
 *
 * @author beck et al. projects GmbH
 * @author Thomas Müller
 * @version $Revision$ / $Date$ / ($Author$)
 */
public abstract class Command {

	private CmdMenu parentNode;

	private int index;

	private String help, description;

	private BufferedReader in;

	private BufferedWriter out;

	/**
	 * Kommando für {@link CmdInterpreter}
	 *
	 * @param desc die Beschreibung
	 * @param help der Hilfetext
	 */
	public Command(String desc, String help) {
		this.description = (desc.equals("")) ? "Keine Beschreibung verfuegbar." : desc;
		this.help = (help.equals("")) ? "Keine Hilfe verfuegbar." : help;
	}

	/**
	 * Reader und Writer für Ein- und Ausgabefunktionen des Kommandos setzen
	 *
	 * @param in  Eingabe-Reader
	 * @param out Ausgabe-Writer
	 */
	public void setStreams(BufferedReader in, BufferedWriter out) {
		this.in = in;
		this.out = out;
	}

	/**
	 * Eltern-Menü für Kommando lesen
	 *
	 * @return Eltern-Menü
	 */
	public CmdMenu getParent() {
		return this.parentNode;
	}

	/**
	 * Eltern-Menü für Kommando setzen
	 *
	 * @param parent das Eltern-Menü
	 */
	public void setParent(CmdMenu parent) {
		this.parentNode = parent;
		setIndex();
	}

	/** Index des Kommandos setzen (abhängig vom Eltern-Menü und Geschwister-Einträgen) */
	public void setIndex() {
		this.index = this.parentNode.getSubMenues().size() + this.parentNode.getCommands().size() - this.parentNode.getNumHiddenCommands();
	}

	/**
	 * Index des Kommandos auslesen
	 *
	 * @return Index (immer eindeutig in einem Menü)
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Hilfetext setzen
	 *
	 * @param help Hilfetext
	 */
	public void setHelp(String help) {
		this.help = help;
	}

	/**
	 * Hilfetext lesen
	 *
	 * @return der Hilfetext
	 */
	public String getHelp() {
		return this.help;
	}

	/**
	 * Beschreibung setzen (wird im Menü in eckigen Klammern angezeigt)
	 *
	 * @param desc Beschreibung
	 */
	public void setDesc(String desc) {
		this.description = desc;
	}

	/**
	 * Beschreibung lesen
	 *
	 * @return die Beschreibung
	 */
	public String getDesc() {
		return this.description;
	}

	/**
	 * Benutzereingabe in einem Kommando (Abbruch bei Überschreitung des Server-Timeout)
	 *
	 * @return userinput Benutzereingabe
	 */
	public String readln() throws Exception {
		out.newLine();
		out.write(CmdInterpreter.PROMPT);
		out.newLine();
		out.flush();
		String input = in.readLine();
		return (input == null) ? "" : input.trim();
	}

	/**
	 * Ausgabe auf Client-Konsole (z.b. via Telnet)
	 *
	 * @param out der Ausgabe-String
	 */
	public void println(String out) throws Exception {
		this.out.newLine();
		this.out.write(" [" + this.description + "] " + out);
		this.out.flush();
	}

	/**
	 * Ausgabe auf Client-Konsole (z.b. via Telnet) ohne die aktuelle Option auszugeben
	 *
	 * @param out der Ausgabe-String
	 */
	public void printlnPlain(String out) throws Exception {
		this.out.newLine();
		this.out.write(out);
		this.out.flush();
	}


	/**
	 * Methode muss von einer implementierenden Klasse gefüllt werden
	 *
	 * @throws Exception meist bei Server-Timeout (wird im {@link CmdInterpreter} abgefangen)
	 */
	public abstract void execute() throws Exception;

	/** @see java.lang.Object#toString() */
	public String toString() {
		return description;
	}
}
