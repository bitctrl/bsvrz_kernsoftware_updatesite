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

package de.bsvrz.sys.funclib.losb.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Diese statische Klasse erlaubt es, auf private Felder zuzugreifen und private Methoden aufzurufen.
 *
 * @author beck et al. projects GmbH
 * @author Phil Schrettenbrunner
 * @version $Revision: 6420 $ / $Date: 2009-03-10 23:19:01 +0100 (Di, 10 Mrz 2009) $ / ($Author: rs $)
 */
public class JavaSpy {

	/**
	 * Liefert den Wert eines (privaten) Feldes als String.
	 * <p/>
	 * <code>JavaSpy.getFieldvalue(dti, "index");</code> ist gleichbedeutend mit <code>... dti.index ... ;</code>
	 * <p/>
	 * Die erste Varianten funktioniert aber auch mit privaten Feldern.
	 *
	 * @param object    Das Objekt
	 * @param fieldName Der Name des Feldes
	 *
	 * @return Der Wert des Feldes des angegebenen Objekts als String
	 *
	 * @throws Exception Bei Fehlern
	 */
	public static String getFieldValueAsString(Object object, String fieldName) throws Exception {
		Class c = object.getClass();
		Field f = c.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(object).toString();
	}

	public static void setStaticFieldvalue(Class c, String fieldName, Object value) throws Exception {
		Field f = c.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(null, value);
	}

	public static Object getStaticFieldvalue(Class c, String fieldName) throws Exception {
		Field f = c.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(null);
	}

	/**
	 * Liefert ein (privates) Feld.
	 *
	 * @param object    Das Objekt
	 * @param fieldName Der Name des Feldes
	 *
	 * @return Der Wert des Feldes des angegebenen Objekts
	 *
	 * @throws Exception Bei Fehlern
	 */
	public static Object getFieldValue(Object object, String fieldName) throws Exception {
		Class c = object.getClass();
		Field f = c.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f.get(object);
	}

	/**
	 * Setzt den Wert eines (privaten) Feldes.
	 * <p/>
	 * <code>JavaSpy.setFieldvalue(dti, "index", new Integer(123));</code> ist gleichbedeutend mit <code>dti.index = 123;</code>
	 * <p/>
	 * Die erste Varianten funktioniert aber auch mit privaten Feldern.
	 *
	 * @param object    Das Objekt
	 * @param fieldName Der Name des Feldes
	 * @param value     Der zu setzende Wert
	 *
	 * @throws Exception Bei Fehlern
	 */
	public static void setFieldValue(Object object, String fieldName, Object value) throws Exception {
		Class c = object.getClass();
		Field f = c.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(object, value);
	}

	/**
	 * Ruft eine Methode (auch private) auf dem angegebenen Objekt mit den angegebenen Paramtern auf. <code>JavaSpy.executeMethod(dti, "getAbsolutPosition", new
	 * Class[] {int.class}, new Object[] {0} );</code> ist gleichbedeutend mit <code>dti.getAbsolutPosition(0);</code>
	 * <p/>
	 * Die erste Variante funktioniert aber auch mit privaten Methoden.
	 *
	 * @param object      Das Objekt
	 * @param methodName  Der Name der Methode, die aufgerufen werden soll, z.B. <code>toString</code>
	 * @param paramTypes  Die Signatur als Class-Array. Wenn die Signatur <code>(long, int)</code> ist, muss hier <code>new Class[] {long.class,
	 *                    int.class}</code> stehen
	 * @param paramValues Die zu übergebenden Werte als Object Array
	 *
	 * @return Der Returnwert der Methode als Object
	 *
	 * @throws Exception Bei Fehlern
	 */
	public static Object executeMethod(Object object, String methodName, Class paramTypes[], Object paramValues[]) throws Exception {
		Class c = object.getClass();

		Method privateMethod = c.getDeclaredMethod(methodName, paramTypes);
		privateMethod.setAccessible(true);
		return privateMethod.invoke(object, paramValues);
	}
}
