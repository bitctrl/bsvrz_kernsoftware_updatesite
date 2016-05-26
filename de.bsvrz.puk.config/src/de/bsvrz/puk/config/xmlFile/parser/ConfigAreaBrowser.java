/*
 * Copyright 2009 by Kappich Systemberatung Aachen 
 * Copyright 2005 by Kappich+Kniß Systemberatung Aachen (K2S)
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
package de.bsvrz.puk.config.xmlFile.parser;

import de.bsvrz.puk.config.xmlFile.properties.ConfigurationAreaProperties;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ConfigAreaBrowser {
	private static Debug _debug;

	final File _file;
	private boolean _needsUsage;

	public static void main(String[] args) {
		ArgumentList arguments = new ArgumentList(args);
		Debug.init("Browser", arguments);
		_debug = Debug.getLogger();
		try {
			ConfigAreaBrowser browser = new ConfigAreaBrowser(arguments);
			if (browser._needsUsage)
				usage();
			else {
				browser.start();
				System.exit(0);
			}
		} catch (Exception e) {
			_debug.error("Beim Ausführen der Applikation ist ein unerwarteter Fehler aufgetreten", e);
			usage();
		}
	}

	private static void usage() {
		System.out.println("Verwendung:");
		System.out.println("   java " + ConfigAreaBrowser.class.getName() + " [optionen]");
		System.out.println("");
		System.out.println("Ohne Optionen wird ein File-Dialog zur Auswahl der zu lesenden Datei angezeigt.");
		System.out.println("");
		System.out.println("Folgende Optionen werden unterstützt:");
		System.out.println("   -?");
		System.out.println("   -hilfe");
		System.out.println("      Ausgabe dieser Beschreibung der Aufrufargumente");
		System.out.println("   -datei=<datenkatalog>");
		System.out.println("      Angabe der einzulesenden Datenkatalog-Datei");
	}

	private ConfigAreaBrowser(ArgumentList arguments) {
		if (arguments.fetchArgument("-hilfe=false").booleanValue() || arguments.fetchArgument("-?=false").booleanValue()) {
			_needsUsage = true;
			_file = null;
			return;
		}
		ArgumentList.Argument fileArgument = arguments.fetchArgument("-datei=");
		if (fileArgument.asString().equals("")) {
			_file = chooseFileAwt();
		} else {
			_file = fileArgument.asReadableFile();
		}
		arguments.ensureAllArgumentsUsed();
	}

	private void start() {
		_debug.info("Inputdatei", _file);
		if (_file != null) {
			try {
				ConfigAreaParser parser = new ConfigAreaParser();
				ConfigurationAreaProperties area = parser.parse(_file);
				System.out.println("ready");
				System.out.println("Konfigurationsbereich: " + area);
			} catch (Exception e) {
				_debug.error("Fehler beim Parsen", e);
				return;
			}
		}
	}


	private File chooseFileSwing() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toLowerCase().endsWith(".xml");
			}

			public String getDescription() {
				return "XML Dateien im Datenkatalogformat.";
			}
		};
		chooser.setFileFilter(filter);
		final String recentUsedFilePath = Preferences.userNodeForPackage(getClass()).get("recentUsedFile", "");
		if (!recentUsedFilePath.equals("")) {
			chooser.setSelectedFile(new File(recentUsedFilePath));
		}
		final int chooserResult = chooser.showOpenDialog(null);
		switch (chooserResult) {
			case JFileChooser.APPROVE_OPTION:
				final File file = chooser.getSelectedFile();
				try {
					Preferences.userNodeForPackage(getClass()).put("recentUsedFile", file.getCanonicalPath());
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Fehler bei der Speicherung des Dateinamens: ", "Warnung", JOptionPane.WARNING_MESSAGE);
				}
				return file;
			case JFileChooser.ERROR_OPTION:
				JOptionPane.showMessageDialog(null, "Fehler bei der Dateiauswahl. Programm wird beendet.", "Fehler", JOptionPane.ERROR_MESSAGE);
				return null;
			case JFileChooser.CANCEL_OPTION:
				return null;
			default:
				assert false : chooserResult;
				return null;
		}
	}

	private File chooseFileAwt() {
		FileDialog chooser = new FileDialog(JOptionPane.getRootFrame(), "Datei auswählen", FileDialog.LOAD);
		chooser.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		final String recentUsedFilePath = Preferences.userNodeForPackage(getClass()).get("recentUsedFile", "");
		if (!recentUsedFilePath.equals("")) {
			_debug.info("recentUsedFilePath = " + recentUsedFilePath);
			final File file = new File(recentUsedFilePath);
			_debug.info("file.getParent() = " + file.getParent());
			chooser.setDirectory(file.getParent());
			_debug.info("file.getName() = " + file.getName());
			chooser.setFile(file.getName());
		}
		chooser.setVisible(true);
		final String fileName = chooser.getFile();
		if (fileName != null) {
			final File file = new File(chooser.getDirectory(), fileName);
			try {
				Preferences.userNodeForPackage(getClass()).put("recentUsedFile", file.getCanonicalPath());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Speicherung des Dateinamens: ", "Warnung", JOptionPane.WARNING_MESSAGE);
			}
			return file;
		} else {
			return null;
		}
	}

}
