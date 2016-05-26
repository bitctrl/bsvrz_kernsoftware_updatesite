/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.main.impl;

import java.util.ArrayList;

/**
 *
 * @author  fouad
 */
public class ArgumentParser {

	/** Diese Methode extrahiert von einem Argument den Wert. Das Argument muss
	 * mit dem Schlüssel anfangen.
	 *
	 *@param argument  Das zu bearbeitende Argument.
	 *@param key  Der Schlüssel des Arguments.
	 *@return Argument-Wert.
	 *@throws InvalidArgumentException Wenn die eingabe parameter <code>null</code> sind oder
	 *       wenn das Argument nicht mit dem Schlüssel anfängt.
	 */
	public static final String getParameter(String argument, String key) throws InvalidArgumentException {
		String parameter = "";
		if (argument == null) {
			throw new InvalidArgumentException("Das angegebene Argument ist null");
		}
		if (key == null) {
			throw new InvalidArgumentException("Der angegebene Schlüssel ist null");
		}
		if (argument.startsWith(key)) {
			parameter = argument.substring(key.length(), argument.length());
		}
		else {
			throw new InvalidArgumentException("Das angegebene Argument muss mit der gegebenen Schlüssel anfangen.");
		}
		return parameter;
	}

	/** Diese Methode extrahiert aus einem Argument die Werte. Das Argument muss
	 * mit dem Schlüssel anfangen und die Werte (wenn es mehrere gibt) müssen mit dem
	 * angegebenen Trennzeichen getrennt sein.
	 *
	 *@param argument Das zu bearbeitende Argument.
	 *@param key Der Schlüssel des Arguments.
	 *@param separator Trennzeichen für die Werte
	 *@throws InvalidArgumentException  Wenn ein übergebener Parameter null ist oder
	 *       wenn das Argument nicht mit dem Schlüssel anfängt.
	 */
	public static final String[] getParameters(String argument, String key, String separator) throws InvalidArgumentException {
		ArrayList list = new ArrayList();
		if (argument == null) {
			throw new InvalidArgumentException("Das angegebene Argument ist null");
		}
		if (key == null) {
			throw new InvalidArgumentException("Der angegebene Schlüssel ist null");
		}

		if (argument.startsWith(key)) {
			if (separator == null) {
				list.add(argument.substring(key.length(), argument.length()));
			}
			else {
				String tmp = argument.substring(key.length(), argument.length());
				if (tmp != null) {
					int pos = 0, pos1 = 0;
					while (pos < tmp.length()) {
						pos1 = tmp.indexOf(separator, pos);
						if (pos1 == -1) {
							list.add(tmp.substring(pos, tmp.length()));
							break;
						}
						else {
							list.add(tmp.substring(pos, pos1));
							pos = pos1 + 1;
						}
					}
				}
			}
			if (list.size() == 0) {
				return null;
			}
			String parameters[] = new String[list.size()];
			for (int i = 0; i < list.size(); ++i) {
				parameters[i] = (String)list.get(i);
			}
			return parameters;
		}
		else {
			throw new InvalidArgumentException("Das angegebene Argument muss mit dem angegebenen Schlüssel anfangen.");
		}
	}

	/** Test Methode
	 */
	public static void main(String args[]) {
		String tmp = "-datenverteiler=124.124.124.53:8080";
		try {
			String parameters[] = getParameters(tmp, "-datenverteiler=", ":");
			if (parameters != null) {
				for (int i = 0; i < parameters.length; ++i) {
					System.out.println(parameters[i]);
				}
			}
		}
		catch(InvalidArgumentException ex) {
			ex.printStackTrace();
		}

		String tmp1 = "-datenhaltung=datenhaltung.pid";
		try {
			String parameter = getParameter(tmp1, "-datenhaltung=");
			if (parameter != null) {
				System.out.println(parameter);
			}
		}
		catch(InvalidArgumentException ex) {
			ex.printStackTrace();
		}
	}
}
