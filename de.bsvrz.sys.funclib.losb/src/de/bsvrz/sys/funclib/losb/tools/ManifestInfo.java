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

package de.bsvrz.sys.funclib.losb.tools;

import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Diese Klasse gibt für alle übergebenen Jar-Dateien die Informationen aus dem Manifest aus
 *
 * @author beck et al. projects GmbH
 * @author Phil Schrettenbrunner
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class ManifestInfo {

	/**
	 * Mainmethode
	 *
	 * @param args Mindestens eine Jar-Datei
	 */
	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("Bitte geben Sie mindestens eine Jar-Datei an.");
			System.exit(1);
		}
		for(String arg : args) {
			printManifestEntries(arg);
		}
	}


	/**
	 * Liest für eine Jar-Datei das Manifest und gibt es auf Stdout aus.
	 *
	 * @param s Dateiname eines Jars
	 */
	public static void printManifestEntries(String s) {
		if(s.endsWith("\n")) s = s.substring(0, s.length() - 2);
		System.out.printf("\n=== %-78s ===\n", s);
		try {
			JarFile jarfile = new JarFile(s);
			Manifest manifest = jarfile.getManifest();
			Attributes attrs = (Attributes)manifest.getMainAttributes();

			for(Iterator it = attrs.keySet().iterator(); it.hasNext();) {
				Attributes.Name attrName = (Attributes.Name)it.next();
				String attrValue = attrs.getValue(attrName);
				System.out.printf("  %-30s %-40s\n", attrName.toString() + ":", attrValue);
			}
		}
		catch(Exception e) {
			System.out.println("  FEHLER: Ueberspringe Datei '" + s + "'. Grund: " + e.getMessage());
		}
	}
}
