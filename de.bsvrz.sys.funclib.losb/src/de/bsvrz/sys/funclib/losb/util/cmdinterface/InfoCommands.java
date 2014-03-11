/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.util.cmdinterface;

import de.bsvrz.sys.funclib.debug.Debug;

import java.lang.management.ManagementFactory;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * Klasse die Kommandos und Men�s f�r Statusinformationen zur verwendeten Software bereitstellt.
 *
 * @author beck et al. projects GmbH
 * @author Phil Schrettenbrunner
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class InfoCommands {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * Kommando zur Anzeige von Informationen �ber die Speicherauslastung.
	 *
	 * @return Kommando.
	 */
	public static Command memoryInfo() {
		return new Command("Speicherinformation", "") {
			public void execute() throws Exception {

				printlnPlain(String.format("Belegter Heapspeicher:   %,15d Bytes", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
				printlnPlain(String.format("Freier Heapspeicher:     %,15d Bytes", Runtime.getRuntime().freeMemory()));
				printlnPlain(String.format("Maximaler Heapspeicher:  %,15d Bytes", Runtime.getRuntime().maxMemory()));
			}
		};
	}

	/**
	 * Kommando zur Anzeige von Laufzeitinformationen.
	 *
	 * @return Kommando.
	 */
	public static Command runtimeInfo() {
		return new Command("Laufzeitinformation", "") {
			// TODO pr�fen, ob MXBean vorhanden und notfalls Exception abfangen!
			public void execute() throws Exception {
				printlnPlain(String.format("Classpath:          %s", System.getProperty("java.class.path")));
				printlnPlain(String.format("VM Argumente:       %s", ManagementFactory.getRuntimeMXBean().getInputArguments()));
				printlnPlain(String.format("Startzeit:          %s", dateFormat.format(ManagementFactory.getRuntimeMXBean().getStartTime())));
				printlnPlain(String.format("Betriebszeit:       %s", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime())));
			}

			private String formatUptime(long l) {
				long SECOND = 1000;
				long MINUTE = 60 * SECOND;
				long HOUR = 60 * MINUTE;
				long DAY = 24 * HOUR;

				long remaining = l;
				long days = remaining / DAY;
				remaining %= 1 * DAY;
				long hours = remaining / HOUR;
				remaining %= 1 * HOUR;
				long minutes = remaining / MINUTE;
				remaining %= 1 * MINUTE;
				long seconds = remaining / SECOND;

				return String.format(
						"%d Tag%s %02d Stunde%s %02d Minute%s %02d Sekunde%s",
						days,
						(days == 1) ? "" : "e",
						hours,
						(hours == 1) ? "" : "n",
						minutes,
						(minutes == 1) ? "" : "n",
						seconds,
						(seconds == 1) ? "" : "n"
				);
			}
		};
	}

	/**
	 * Kommando zur Anzeige von Systeminformationen.
	 *
	 * @return Kommando.
	 */
	public static Command systemInfo() {
		return new Command("Systeminformation und Versionen", "") {
			// TODO pr�fen, ob MXBean vorhanden und notfalls Exception abfangen!
			public void execute() throws Exception {
				printlnPlain(String.format("Java Hersteller:          %s", System.getProperty("java.vendor")));
				printlnPlain(String.format("Java Version:             %s", System.getProperty("java.version")));
				printlnPlain(String.format("Rechnerart:               %s", ManagementFactory.getOperatingSystemMXBean().getArch()));
				printlnPlain(String.format("Prozessoren:              %s", ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors()));
				printlnPlain(
						String.format(
								"Betriebssystem:           %s (%s)",
								ManagementFactory.getOperatingSystemMXBean().getName(),
								ManagementFactory.getOperatingSystemMXBean().getVersion()
						)
				);
				printlnPlain(String.format("Archivsystem Hersteller:  %s", getManifestInfo("Implementation-Vendor")));
				printlnPlain(String.format("Archivsystem Version:     %s", getManifestInfo("Implementation-Version")));
				printlnPlain(String.format("Archivsystem Datum:       %s", getManifestInfo("Source-Date")));
			}

			private String getManifestInfo(String key) {
				URL u = getClass().getResource("/de/bsvrz/ars/ars/mgmt/ArchiveManager.class");
				if(u != null) {
					try {
						JarURLConnection juc = (JarURLConnection)u.openConnection();
						Manifest mf = juc.getManifest();
						Attributes attr = mf.getMainAttributes();
						String s = attr.getValue(key);
						if(s != null) return s;
					}
					catch(Exception e) {
					} // Fehler -> return unten beendet Methode
				}
				return "(nicht verf�gbar)";
			}
		};
	}

	/**
	 * Liefert ein Menu zum �ndern des StdErr Debug-Levels.
	 *
	 * @param desc Beschreibung des Men�s.
	 * @param help Hilfetext zum Men�.
	 *
	 * @return Men�.
	 */
	public static CmdMenu getDebugLevelMenu(String desc, String help) {
		return new DebugLevelMenu(desc, help);
	}

	/**
	 * Liefert ein Menu zum �ndern des StdErr Debug Levels. Die Beschreibung des Men�s ist 'Debug-Level fuer StdErr aendern'.
	 *
	 * @return Men�.
	 */
	public static CmdMenu getDebugLevelMenu() {
		return new DebugLevelMenu("Debug-Level fuer StdErr aendern", "");
	}
}

/**
 * Subem� zum �ndern des Log-Levels zur Laufzeit.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 */
class DebugLevelMenu extends CmdMenu {

	/** @see CmdMenu#CmdMenu(String,String) */
	public DebugLevelMenu(String desc, String help) {
		super(desc, help);
		addCommands(Debug.INFO);
		addCommands(Debug.WARNING);
		addCommands(Debug.ERROR);
		addCommands(Debug.ALL);
		addCommands(Debug.CONFIG);
		addCommands(Debug.FINE);
		addCommands(Debug.FINER);
		addCommands(Debug.FINEST);
		addCommands(Debug.OFF);
	}

	/**
	 * F�gt ein Kommando hinzu.
	 *
	 * @param level Debug Level.
	 */
	private void addCommands(final Level level) {
		this.addCmd(
				new Command("Debug Level '" + level + "'", "") {
					public void execute() throws Exception {
						Debug.setHandlerLevel("StdErr", level);
						println("Neues Debug-Level gesetzt: " + level);
					}
				}
		);
	}
}
