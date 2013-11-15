/*
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main.impl;

import java.util.ArrayList;

/**
 *
 * @author  fouad
 */
public class ArgumentParser {

	/** Diese Methode extrahiert von einem Argument den Wert. Das Argument muss
	 * mit dem Schl�ssel anfangen.
	 *
	 *@param argument  Das zu bearbeitende Argument.
	 *@param key  Der Schl�ssel des Arguments.
	 *@return Argument-Wert.
	 *@throws InvalidArgumentException Wenn die eingabe parameter <code>null</code> sind oder
	 *       wenn das Argument nicht mit dem Schl�ssel anf�ngt.
	 */
	public static final String getParameter(String argument, String key) throws InvalidArgumentException {
		String parameter = "";
		if (argument == null) {
			throw new InvalidArgumentException("Das angegebene Argument ist null");
		}
		if (key == null) {
			throw new InvalidArgumentException("Der angegebene Schl�ssel ist null");
		}
		if (argument.startsWith(key)) {
			parameter = argument.substring(key.length(), argument.length());
		}
		else {
			throw new InvalidArgumentException("Das angegebene Argument muss mit der gegebenen Schl�ssel anfangen.");
		}
		return parameter;
	}

	/** Diese Methode extrahiert aus einem Argument die Werte. Das Argument muss
	 * mit dem Schl�ssel anfangen und die Werte (wenn es mehrere gibt) m�ssen mit dem
	 * angegebenen Trennzeichen getrennt sein.
	 *
	 *@param argument Das zu bearbeitende Argument.
	 *@param key Der Schl�ssel des Arguments.
	 *@param separator Trennzeichen f�r die Werte
	 *@throws InvalidArgumentException  Wenn ein �bergebener Parameter null ist oder
	 *       wenn das Argument nicht mit dem Schl�ssel anf�ngt.
	 */
	public static final String[] getParameters(String argument, String key, String separator) throws InvalidArgumentException {
		ArrayList list = new ArrayList();
		if (argument == null) {
			throw new InvalidArgumentException("Das angegebene Argument ist null");
		}
		if (key == null) {
			throw new InvalidArgumentException("Der angegebene Schl�ssel ist null");
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
			throw new InvalidArgumentException("Das angegebene Argument muss mit dem angegebenen Schl�ssel anfangen.");
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
