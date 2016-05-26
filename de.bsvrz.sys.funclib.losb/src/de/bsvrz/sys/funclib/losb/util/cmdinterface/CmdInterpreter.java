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

import de.bsvrz.sys.funclib.debug.Debug;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Kommando-Interpreter
 *
 * @author beck et al. projects GmbH
 * @author Thomas Müller
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class CmdInterpreter extends Thread {

	private static final Debug logger = Debug.getLogger();

	/** Das Prompt für die Darstellung einer Eingabeaufforderung an der Konsole */
	public static String PROMPT = "? ";

	private boolean closeConnection = false, stopServer = false;

	private int port = -1, timeout;

	private CmdMenu currentMenu = null, rootMenu = null;

	private Socket client = null;

	private ServerSocket serverSocket = null;

	private BufferedReader in;

	private BufferedWriter out;

	/** True falls zusätzliche Kommandos angezeigt werden sollen. */
	private boolean showHidden = false;

	/** Kommando zum schliessen der Verbindung (Server horcht weiter) */
	public Command closeConnCmd = new Command("Verbindung trennen", "Beendet die Verbindung zum Server (nicht den Server selbst).") {
		public void execute() {
			closeConnection = true;
			logger.info("Kommandoverbindung zum Telent-Server wurde beendet");
		}
	};

	/** Kommando zum beenden des Serverdienstes auf localhost */
	public Command stopServerCmd = new Command("Server stoppen", "Beendet den Serverdienst.") {
		public void execute() throws Exception {
			/** Kommando zum Schliessen der Verbindung und beenden des Telnet-Servers */
			this.println("Wirklich den Server herunterfahren? [ja|nein]");
			String yes = this.readln();
			if(yes.equalsIgnoreCase("ja")) {
				this.println("Server wird heruntergefahren.");
				closeConnection = true;
				stopServer = true;
			}
		}
	};


	/**
	 * Erzeugt den Telnet-Server
	 *
	 * @param port           der Port auf dem der Server horcht
	 * @param timeOutMinutes Timeout für eine Socket-Verbindung
	 */
	public CmdInterpreter(int port, int timeOutMinutes) {
		setName(getClass().getSimpleName());    // Zur Identifikation im Debugger
		this.port = port;
		timeout = timeOutMinutes * 60 * 1000;
	}

	/**
	 * Erzeugt den Telnet-Server
	 *
	 * @param port           Port auf dem der Server horcht
	 * @param root           das Hauptmenü
	 * @param timeOutMinutes Timeout für eine Socket-Verbindung
	 */
	public CmdInterpreter(int port, int timeOutMinutes, CmdMenu root) {
		this(port, timeOutMinutes);
		rootMenu = root;
		currentMenu = root;
		currentMenu.addCmd(closeConnCmd);       // ist beim Zugang uebers Netz automatisch dabei
	}

	/**
	 * Erzeugt den Telnet-Server
	 *
	 * @param port           Port auf dem der Server horcht
	 * @param timeOutMinutes Timeout für eine Socket-Verbindung
	 * @param showHidden     <code>true</code> falls zusätzliche Kommandos angezeigt werden sollen
	 */
	public CmdInterpreter(int port, int timeOutMinutes, boolean showHidden) {
		this(port, timeOutMinutes);
		this.showHidden = showHidden;
	}


	/** @see java.lang.Runnable#run() */
	public void run() {
		if(port != -1) {
			try {
				processConnections();
			}
			catch(Exception e) {
				logger.warning("Die Kommandoverbindung auf dem Port " + port + " konnte nicht aufgebaut werden: " + e.getMessage());
			}
		}
		else {
			menuLoop();
		}
	}

	/**
	 * Warten auf Verbindung, Menü in Loop anzeigen, Verbindung schliessen
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void processConnections() throws UnknownHostException, IOException {
		serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		logger.info("Kommandoverbindung zum Archivsystem auf Port " + port + " verfuegbar (Timeout = " + timeout / 1000 + " Sekunden)");
		while(!stopServer) {
			try {
				waitForConnection();
				menuLoop();
				client.close();
			}
			catch(SocketException sEx) {
				logger.info("Verbindung abgebaut: " + sEx.getMessage());
				break;
			}
			catch(IOException ioEx) {
				logger.warning("Fehler beim Aufbau der Verbindung. (IOException): ", ioEx.getMessage());
				break;
			}
			if(client != null) client.close();
		}
		if(serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
	}

	/**
	 * Socket-Verbindung (Telnet) akzeptieren und Timeout setzen
	 *
	 * @throws IOException
	 */
	private void waitForConnection() throws IOException {
		client = serverSocket.accept();
		client.setSoTimeout(timeout);
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		closeConnection = false;
		currentMenu = rootMenu;
		logger.info("Kommandoverbindung zum Telnet-Server hergestellt.");
	}

	/** Menü anzeigen, Navigation durchführen, Kommandos ausführen */
	public void menuLoop() {
		try {
			boolean showMenu = true;
			while(!closeConnection) {
				if(showMenu) showMenu();
				String userInput = getUserInput();
				if(userInput.length() > 0) {
					if(userInput.charAt(0) == 'h') {
						println(getHelp(userInput.substring(1)));
						showMenu = false;
					}
					else {
						try {
							boolean found = false;
							int index = Integer.parseInt(userInput);
							if(index == 0) {
								if(currentMenu.getParent() != null) currentMenu = currentMenu.getParent();
								showMenu = true;
								continue;
							}
							for(Command cmd : currentMenu.getCommands()) {
								if(cmd.getIndex() == index) {
									cmd.setStreams(in, out);
									cmd.execute();
									found = true;
									break;
								}
							}
							if(!found) {
								for(CmdMenu node : currentMenu.getSubMenues()) {
									if(node.getIndex() == index) {
										currentMenu = node;
										found = true;
										break;
									}
								}
							}

							if(!found) println("Ungueltige Eingabe.");
							showMenu = found;
						}
						catch(NumberFormatException numEx) {
							if(userInput.equals("^")) {
								if(currentMenu != rootMenu) currentMenu = currentMenu.getParent();
								showMenu = true;
							}
							else {
								println("Ungueltige Eingabe.");
								showMenu = false;
							}
						}
					}
				}
			}
		}
		catch(SocketTimeoutException socTEx) {
			logger.info("Timeout nach " + (timeout / 1000) + " Sekunden. Verbindung wird getrennt.");
		}
		catch(SocketException socEx) {
			logger.warning("Kommandoverbindung zum Telnet-Server wurde getrennt: " + socEx.getMessage());
		}
		catch(Exception e) {
			logger.warning("Telnet Server Fehler: ", e.getMessage());
		}
	}


	/**
	 * Benutzereingabe vom In-Stream lesen und z.B. an {@link #menuLoop()} zurückgeben, Abbruch bei Überschreitung des Server-Timeout
	 *
	 * @return Benutzereingabe
	 *
	 * @throws Exception
	 */
	private String getUserInput() throws Exception {
		println(PROMPT);
		out.newLine();
		out.flush();
		String input = in.readLine();
		return (input == null) ? "" : input.trim();
	}

	/**
	 * Hilfe für Menüeintrag zurückgeben
	 *
	 * @param ind Index des Menüeintrags (Submenü oder Kommando)
	 *
	 * @return Hilfetext
	 */
	private String getHelp(String ind) {
		boolean found = false;
		String help = "";
		try {
			int index = Integer.parseInt(ind);
			for(Command leaf : currentMenu.getCommands()) {
				if(leaf.getIndex() == index) {
					help = "[" + leaf.getDesc() + "] " + leaf.getHelp();
					found = true;
				}
			}
			for(CmdMenu node : currentMenu.getSubMenues()) {
				if((node.getIndex() == index) && (!found)) {
					help = "[" + node.getDesc() + "] " + node.getHelp();
					found = true;
				}
			}
			if(index == 0) {
				help = "[" + currentMenu.getDesc() + "] " + currentMenu.getHelp();
				found = true;
			}
			if(!found) {
				help = "Ungueltige Eingabe.";
			}
		}
		catch(Exception e) {
			help = "Ungueltige Eingabe.";
		}
		return help;
	}

	/**
	 * Aktuelles Menü darstellen
	 *
	 * @throws Exception
	 */
	private void showMenu() throws Exception {
		println("");
		CmdMenu tmp = currentMenu;
		String title = "";
		do {
			title = tmp.getDesc() + ":" + title;
		}
		while((tmp = tmp.getParent()) != null);
		println("_____________________" + title.substring(0, title.length() - 1));
		for(CmdMenu child : currentMenu.getSubMenues()) {
			println(" + " + child.getIndex() + "  " + child.getDesc());
		}
		for(Command leaf : currentMenu.getCommands()) {
			if(!(leaf instanceof HiddenCommand)) {
				println("   " + leaf.getIndex() + "  " + leaf.getDesc());
			}
			else if(showHidden) println("hidden: " + leaf.getIndex() + "  " + leaf.getDesc());
		}
		if(currentMenu != rootMenu) println("   0  Aufwaerts");
	}

	/**
	 * Einen Text in den Out-Stream (z.B. via Telnet) ausgeben
	 *
	 * @param str der darzustellende String
	 *
	 * @throws Exception
	 */
	public void println(String str) throws Exception {
		out.newLine();
		out.write(str);
		out.flush();
	}

	/**
	 * Menü für den Interpreter setzen (falls noch nicht geschehen)
	 *
	 * @param root das Menü
	 */
	public void setMenu(CmdMenu root) {
		rootMenu = root;
		currentMenu = root;
		if(port != -1) rootMenu.addCmd(closeConnCmd);
	}

	/** Beendet den Telnet-Servers. Schliesst die Verbindung. */
	public void stopServerCmd() throws IOException {
		if(serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
		closeConnection = true;
		stopServer = true;
	}
}
