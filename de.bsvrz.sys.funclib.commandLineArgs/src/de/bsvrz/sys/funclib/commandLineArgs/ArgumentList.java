/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.commandLineArgs.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.commandLineArgs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.commandLineArgs; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.commandLineArgs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Klasse zum Zugriff auf die Aufrufargumente einer Applikation.
 * Eine Applikation bekommt die Aufrufargumente von der Laufzeitumgebung in einem String-Array als Parameter
 * der <code>main</code>-Funktion übergeben. Dieses String-Array wird dem Konstruktor dieser Klasse übergeben.
 * Mit der Methode {@link #fetchArgument} kann auf die einzelnen Argumente zugegriffen werden.
 * Beim Zugriff auf ein Argument wird der entsprechende Eintrag im String-Array auf <code>null</code> gesetzt.
 * Nach dem Zugriff auf alle von einer Applikation
 * unterstützten Argumente kann mit der Methode {@link #ensureAllArgumentsUsed} sichergestellt werden, daß alle
 * angegebenen Argumente verwendet wurden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ArgumentList {
	/**
	 * Speichert eine Kopie der ursprünglichen Aufrufargumente der Applikation, die dem Konstruktor übergeben wurden.
	 */
	private final String[] _initialArgumentStrings;

	/**
	 * Speichert eine Referenz auf die Aufrufargumente der Applikation, die dem Konstruktor übergeben wurden.
	 * Nachdem ein Argument interpretiert wurde, wird es im übergebenen Array auf <code>null</code> gesetzt.
	 *
	 */
	private final String[] _argumentStrings;

	/**
	 * Erzeugt eine neue Argumentliste und initialisiert diese mit den übergebenen Aufrufargumenten der Applikation.
	 * Einzelne Argumente, die von der Applikation bereits interpretiert wurden, sollten vorher auf den Wert <code>null</code>
	 * gesetzt werden, damit sie nicht nochmal interpretiert werden.
	 *
	 * @param argumentStrings  String-Array, das die Aufrufargumente enthält, die der Applikation beim Aufruf der
	 *                         main-Funktion übergeben werden.
 	 */
	public ArgumentList(String[] argumentStrings) {
		_argumentStrings= argumentStrings;
		_initialArgumentStrings = new String[argumentStrings.length];
		System.arraycopy(_argumentStrings, 0, _initialArgumentStrings, 0, _argumentStrings.length);
	}

	/**
	 * Liefert ein bestimmtes Argument zurück und setzt es im String-Array, das beim Konstruktor übergeben wurde,
	 * auf <code>null</code>. Im übergebenen Parameter der Funktion wird der Name des Arguments und bei Bedarf
	 * (durch ein Gleich-Zeichen getrennt) ein Default-Wert angegeben.
	 * Die Aufrufargumente müssen dem Format "argumentName=argumentWert" entsprechen:
	 * Beim Zugriff auf ein Argument muss der Argument-Name angegeben werden. Ergebnis des Zugriffs ist ein
	 * Objekt der Klasse {@link Argument} über das der Wert des Arguments abgefragt werden kann.
	 * Wenn das gewünschte Argument in der Argumentliste gefunden wurde, wird der entsprechende Eintrag im
	 * String-Array, das dem {@link #ArgumentList Konstruktor} übergeben wurde, auf den Wert <code>null</code> gesetzt.
	 * Wenn auf ein Argument zugegriffen wird, das in der Argumentlist nicht mehr vorhanden ist, weil es bereits vorher
	 * interpretiert wurde, dann wird der entsprechende Wert aus der initialen Argumentliste erneut zurückgegeben.
	 * Wenn auf ein Argument zugegriffen wird, das in der initialen Argumentliste nicht vorhanden ist,
	 * wird als Wert der Default-Wert benutzt, wenn dieser im Parameter der Methode (durch ein Gleichzeichen vom
	 * Argumentnamen getrennt) angegeben wurde. Wenn das gewünschte Argument nicht in der Argumentliste enthalten
	 * ist und kein Default-Wert angegeben wurde, wird eine Ausnahme generiert.
	 * @param nameAndOptionalDefault  Name des gewünschten Arguments und optional durch ein Gleichzeichen getrennt
	 *                                der Default-Wert des Arguments.
	 * @return  Ein Argument-Objekt über das mit verschiedenen Methoden auf den Wert des Arguments zugegriffen werden kann.
	 * @throws IllegalArgumentException  Wenn kein Wert für das gewünschte Argument ermittelt werden konnte.
	 */
	public Argument fetchArgument(String nameAndOptionalDefault) {
		int defaultSeparatorPosition= nameAndOptionalDefault.indexOf("=");
		String name= nameAndOptionalDefault;
		if(defaultSeparatorPosition >= 0) name= nameAndOptionalDefault.substring(0,defaultSeparatorPosition);
		for(int i=0; i<_argumentStrings.length; ++i) {
			String argumentString= _argumentStrings[i];
			if(argumentString!=null && ( argumentString.equals(name) || argumentString.startsWith(name + "=") )) {
				_argumentStrings[i]= null;
				return new Argument(argumentString);
			}
		}
		for(int i=0; i<_initialArgumentStrings.length; ++i) {
			String argumentString= _initialArgumentStrings[i];
			if(argumentString!=null && ( argumentString.equals(name) || argumentString.startsWith(name + "=") )) {
				return new Argument(argumentString);
			}
		}
		if(defaultSeparatorPosition >= 0) return new Argument(nameAndOptionalDefault);
		throw new java.lang.IllegalArgumentException("fehlendes Argument: " + name);
	}

	/**
	 * Prüft, ob ein bestimmtes Argument vorhanden ist und noch nicht interpretiert wurde.
	 *
	 * @param name Name des gesuchten Arguments.
	 * @return <code>true</code> Wenn das gesuchte Argument in der Argumentliste enthalten ist und noch nicht interpretiert
	 *         wurde, sonst <code>false</code>.
	 */
	public boolean hasArgument(String name) {
		for(int i=0; i<_argumentStrings.length; ++i) {
			String argumentString= _argumentStrings[i];
			if(argumentString!=null && ( argumentString.equals(name) || argumentString.startsWith(name + "=") )) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Diese Methode stellt sicher, daß alle Argumente interpretiert wurden.
	 * @throws IllegalStateException  Wenn in der Argumentliste noch nicht ausgewertete Argumente enthalten sind.
	 */
	public void ensureAllArgumentsUsed() {
		if(hasUnusedArguments()) {
			StringBuffer message= new StringBuffer("Unbenutzte Argumente: {");
			Argument[] unusedArguments= fetchUnusedArguments();
			for(int i=0; i< unusedArguments.length; ++i) message.append(unusedArguments[i]).append(", ");
			message.append("}");
			throw new java.lang.IllegalStateException(message.toString());
		}
	}

	/**
	 * Bestimmt, ob in der Argumentliste noch Argumente exististieren, die noch nicht ausgewertet wurden.
	 * @return <code>true</code>, falls weitere Argumente existieren; <code>false</code>, falls alle Argumente
	 *         ausgewertet wurden.
	 */
	public boolean hasUnusedArguments() {
		for(int i=0; i<_argumentStrings.length; ++i) {
			if(_argumentStrings[i] != null) return true;
		}
		return false;
	}

	/**
	 * Liefert den Namen des nächsten noch nicht interpretierten Arguments zurück.
	 * @return  Name des nächsten noch nicht interpretierten Arguments.
	 * @throws IllegalStateException  Wenn bereits alle Argumente interpretiert wurden.
	 */
	public String getNextArgumentName() {
		for(int i=0; i<_argumentStrings.length; ++i) {
			String argumentString= _argumentStrings[i];
			if(argumentString!=null) return new Argument(argumentString).getName();
		}
		throw new java.lang.IllegalStateException("Zu wenig Argumente");
	}

	/**
	 * Liefert das erste noch nicht interpretierte Argument zurück und setzt es im String-Array, das beim Konstruktor übergeben wurde,
	 * auf <code>null</code>. Die Aufrufargumente müssen dem Format "argumentName=argumentWert" entsprechen:
	 * Beim Zugriff auf ein Argument muss der Argument-Name angegeben werden. Ergebnis des Zugriffs ist ein
	 * Objekt der Klasse {@link Argument} über das der Wert des Arguments abgefragt werden kann. Für Argumente die kein
	 * Gleichzeichen mit folgendem argumentWert enthalten, wird als Wert der Text "wahr" angenommen.
	 * Der entsprechende Eintrag im
	 * String-Array, das dem {@link #ArgumentList Konstruktor} übergeben wurde, wird auf den Wert <code>null</code> gesetzt.
	 * Wenn in der Argumentliste kein Argument mehr enthalten war, wird eine Ausnahme generiert.
	 * @return  Ein Argument-Objekt über das mit verschiedenen Methoden auf den Wert des Arguments zugegriffen werden kann.
	 * @throws IllegalStateException  Wenn bereits alle Argumente interpretiert wurden.
	 */
	public Argument fetchNextArgument() {
		for(int i=0; i<_argumentStrings.length; ++i) {
			String argumentString= _argumentStrings[i];
			if(argumentString!=null) {
				_argumentStrings[i]= null;
				return new Argument(argumentString);
			}
		}
		throw new java.lang.IllegalStateException("Zu wenig Argumente");
	}

	/**
	 * Bestimmt die Anzahl der in der Argumentliste noch vorhandenen und nicht ausgewerteten Argumente.
	 * @return  Anzahl noch nicht ausgewerteten Argumente in der Argumentliste.
	 */
	public int getUnusedArgumentCount() {
		int unusedCount= 0;
		for(int i=0; i<_argumentStrings.length; ++i) {
			if(_argumentStrings[i] != null) ++unusedCount;
		}
		return unusedCount;
	}

	/**
	 * Liefert ein Feld mit den noch nicht ausgewerteten Argumenten der Aufrufliste zurück und setzt die
	 * entsprechenden Einträge im String-Array, das beim Konstruktor übergeben wurde,
	 * auf <code>null</code>.
	 * @return  Feld mit Argument-Objekten der noch nicht ausgewerteten Argumente.
	 */
	public Argument[] fetchUnusedArguments() {
		int unusedCount= getUnusedArgumentCount();
		if(unusedCount==0) return null;
		Argument[] unusedArguments= new Argument[unusedCount];
		unusedCount= 0;
		for(int i=0; i<_argumentStrings.length; ++i) {
			if(_argumentStrings[i] != null) {
				unusedArguments[unusedCount++]= new Argument(_argumentStrings[i]);
				_argumentStrings[i]= null;
			}
		}
		return unusedArguments;
	}

	/**
	 * Klasse zum Zugriff auf Name und Wert eines Aufrufarguments.
	 */
	public static class Argument {
		/** Speichert den Namen eines Arguments.
		 */
		private final String _name;

		/** Speichert den Wert eines Arguments.
		 */
		private final String _value;


//		public Argument(String name, String value) {
//			_name= name;
//			_value= value;
//		}

		/**
		 * Erzeugt ein neues Argument-Objekt aus dem übergebenen Argument-Text.
		 * Der Argument-Text muss folgenden Aufbau haben: "argumentName=argumentWert".
		 * Name und Wert des Argument-Objekts werden entsprechend gesetzt.
		 * Wenn der Argument-Text kein Gleichzeichen enthält, dann wird der ganze
		 * Argument-Text als Name des Arguments interpretiert und es wird vermerkt, daß das
		 * Argument keinen Wert hat.
		 */
		Argument(String argumentString) {
			int separatorPosition= argumentString.indexOf("=");
			if(separatorPosition >= 0) {
				_name= argumentString.substring(0,separatorPosition);
				_value= argumentString.substring(separatorPosition+1);
			}
			else {
				_name= argumentString;
				_value= null; // Default-Wert, wenn kein Wert angegeben wurde
			}
		}

		/**
		 * Erzeugt ein neues Argument-Objekt aus den übergebenen Parametern
		 * @param name Name des neuen Arguments
		 * @param value Wert des neuen Arguments
		 */
		private Argument(String name, String value) {
				_name= name;
				_value= value;
		}


		/**
		 * Erzeugt ein neues Argument dessen Wert aus dem Namen dieses Arguments übernommen wird.
		 * @return Neues Argument-Objekt
		 */
		public Argument toArgumentWithNameAsValue() {
			return new Argument(getName(), getName());
		}

		/**
		 * Bestimmt den Namen des Arguments.
		 * @return  Name des Arguments.
		 */
		public String getName() {
			return _name;
		}

		/**
		 * Überprüft, ob das Argument einen Wert hat. Wenn das Argument keinen Wert
		 * hat, kann nur noch mit der Methode #{@link #booleanValue} auf den Wert
		 * zugegriffen werden, ohne daß eine Ausnahme generiert wird.
		 * @return <code>true</code>, falls das Argument einen Wert hat; sonst <code>false</code>.
		 */
		public boolean hasValue() {
			if(_value==null) return false;
			return true;
		}

		/**
		 * Bestimmt den Wert des Arguments.
		 * @return  Wert des Arguments.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public String getValue() {
			if(hasValue()) return _value;
			throw new IllegalArgumentException("Argument " + getName() + " hat keinen Wert");
		}

		/**
		 * Gibt den Wert des Arguments als <code>boolean</code> Wert zurück.
		 * Die Argumentwerte "wahr", "ja", "1" werden zum Boolschen Wert <code>true</code> konvertiert;
		 * die Argumentwerte "falsch", "nein", "0" werden zum Boolschen Wert <code>false</code> konvertiert.
		 * Die Groß-/Kleinschreibung des Argumentwerts hat beim Vergleich keine Relevanz.
		 * Wenn das Argument keinen Wert hat, dann wird als Ergebnis der Konvertierung <code>true</code>
		 * zurückgegeben.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 */
		public boolean booleanValue() throws IllegalArgumentException{
			if(!hasValue()) return true;
			String value= _value.toLowerCase();
			if(value.equals("wahr") || value.equals("ja") || value.equals("1")) return true;
			if(value.equals("falsch") || value.equals("nein") || value.equals("0")) return false;
			//englische Varianten werden auch unterstützt:
			if(value.equals("true") || value.equals("yes")) return true;
			if(value.equals("false") || value.equals("no")) return false;
			else throw new java.lang.IllegalArgumentException("Argument " + getName() + " hat keinen boolschen Wert: " + getValue());
		}

		/**
		 * Gibt den Wert des Arguments als <code>byte</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>byte</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public byte byteValue() throws NumberFormatException {
			try {
				return Byte.parseByte(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Byte-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>short</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>short</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public short shortValue() throws NumberFormatException {
			try {
				return Short.parseShort(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Short-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>int</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>int</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public int intValue() throws NumberFormatException {
			try {
				return Integer.parseInt(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Integer-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>long</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>long</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public long longValue() throws NumberFormatException {
			try {
				return Long.parseLong(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Long-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>float</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>float</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public float floatValue() throws NumberFormatException {
			try {
				return Float.parseFloat(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Float-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>double</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>double</code> konvertiert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public double doubleValue() throws NumberFormatException {
			try {
				return Double.parseDouble(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Double-Wert: " + getValue());
			}
		}

		/**
		 * Gibt den Wert des Arguments als <code>byte</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>byte</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public byte byteValueBetween(byte minimum, byte maximum) throws NumberFormatException {
			byte value;
			try {
				value= Byte.parseByte(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Byte-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + "von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}

		/**
		 * Gibt den Wert des Arguments als <code>short</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>short</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public short shortValueBetween(short minimum, short maximum) throws NumberFormatException {
			short value;
			try {
				value= Short.parseShort(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Short-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + "von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}

		/**
		 * Gibt den Wert des Arguments als <code>int</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>int</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public int intValueBetween(int minimum, int maximum) throws NumberFormatException {
			int value;
			try {
				value= Integer.parseInt(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Integer-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + " von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}

		/**
		 * Gibt den Wert des Arguments als <code>long</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>long</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public long longValueBetween(long minimum, long maximum) throws NumberFormatException {
			long value;
			try {
				value= Long.parseLong(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Long-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + "von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}

		/**
		 * Gibt den Wert des Arguments als <code>float</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>float</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public float floatValueBetween(float minimum, float maximum) throws NumberFormatException {
			float value;
			try {
				value= Float.parseFloat(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Float-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + "von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}

		/**
		 * Gibt den Wert des Arguments als <code>double</code> Wert zurück.
		 * Der Argumentwert wird in einen Zahlwert vom Typ <code>double</code> konvertiert
		 * und überprüft, ob der Wert nicht ausserhalb der angegebenen Grenzen liegt.
		 * @param minimum  Kleinster erlaubter Wert.
		 * @param maximum  Größter erlaubter Wert.
		 * @return  Der konvertierte Argumentwert.
		 * @throws IllegalArgumentException  Wenn der Wert kleiner als das Minimum oder größer als das Maximum ist.
		 * @throws NumberFormatException  Wenn der Argumentwert nicht konvertiert werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public double doubleValueBetween(double minimum, double maximum) throws NumberFormatException {
			double value;
			try {
				value= Double.parseDouble(getValue());
			}
			catch(Exception e) {
				throw new java.lang.NumberFormatException("Argument " + getName() + " hat keinen Double-Wert: " + getValue());
			}
			if(value>=minimum && value<=maximum) return value;
			throw new IllegalArgumentException(
				"Argumentwert " +getValue() + "von Argument " + getName() +
				" liegt nicht zwischen " + minimum + " und " + maximum
			);
		}


		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asFile() {
			return new File(getValue());
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer existierenden Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asExistingFile() {
			File file= asFile();
			if(!file.exists()) throw new java.lang.IllegalArgumentException("Argument " + getName() + ": Datei existiert nicht: " + getValue() + ", Pfad: " + file.getAbsolutePath());
			return file;
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer lesbaren Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert und ob ein lesender Zugriff erlaubt ist.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert oder nicht lesbar ist.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asReadableFile() {
			File file= asExistingFile();
			if(!file.canRead()) throw new java.lang.IllegalArgumentException("Argument " + getName() + ": Datei ist nicht lesbar: " + getValue());
			return file;
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer beschreibaren Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert und ob ein schreibender Zugriff erlaubt ist.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert oder nicht beschreibar ist.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asWritableFile() {
			File file= asExistingFile();
			if(!file.canWrite()) throw new java.lang.IllegalArgumentException("Argument " + getName() + ": Datei ist nicht beschreibar: " + getValue());
			return file;
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer beschreibaren Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert und ob ein schreibender Zugriff erlaubt ist.
		 * @param createIfNotExistent  Wenn die spezifizierte Datei nicht existiert und dieser Parameter
		 *                             den Wert <code>true</code> hat, wird eine neue Datei erzeugt.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert und nicht
		 *                                   angelegt werden sollte oder nicht beschreibar ist.
		 * @throws IOException  Wenn die Datei nicht angelegt werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asWritableFile(boolean createIfNotExistent) throws IOException {
			File file= asFile();
			if(createIfNotExistent && !file.exists()) file.createNewFile();
			return asWritableFile();
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer änderbaren Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert und ob ein lesender und schreibender Zugriff erlaubt ist.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert oder nicht
		 *                                   nicht lesbar oder nicht beschreibar ist.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asChangeableFile() {
			File file= asReadableFile();
			if(!file.canWrite()) throw new java.lang.IllegalArgumentException("Argument " + getName() + ": Datei ist nicht beschreibar: " + getValue());
			return file;
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation einer änderbaren Datei zurück.
		 * Der Argumentwert wird als Dateiname der Datei interpretiert. Es wird geprüft, ob die
		 * Datei existiert und ob ein lesender und schreibender Zugriff erlaubt ist.
		 * @param createIfNotExistent  Wenn die spezifizierte Datei nicht existiert und dieser Parameter
		 *                             den Wert <code>true</code> hat, wird eine neue Datei erzeugt.
		 * @return  Dateiobjekt zum Zugriff auf die durch den Argumentwert identifizierten Datei.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert und nicht
		 *                                   angelegt werden sollte oder nicht lesbar oder nicht beschreibar ist.
		 * @throws IOException  Wenn die Datei nicht angelegt werden konnte.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asChangeableFile(boolean createIfNotExistent) throws IOException {
			File file= asFile();
			if(createIfNotExistent && !file.exists()) file.createNewFile();
			return asReadableFile();
		}

		/**
		 * Gibt den Wert des Arguments als Datei-Identifikation eines Dateiverzeichnisses zurück.
		 * Der Argumentwert wird als Dateiname des Dateiverzeichnisses interpretiert. Es wird geprüft, ob die
		 * spezifizierte Datei existiert und ein Verzeichnis ist.
		 * @return  Dateiobjekt zum Zugriff auf das durch den Argumentwert identifizierte Dateiverzeichnis.
		 * @throws IllegalArgumentException  Wenn die identifizierte Datei nicht existiert oder kein Verzeichnis ist.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public File asDirectory() {
			File file= asExistingFile();
			if(file.isDirectory()) return(file);
			throw new java.lang.IllegalArgumentException("Argument " + getName() + ": Kein Dateiverzeichnis: " + getValue());
		}

		/**
		 * Bestimmt den Wert des Arguments als Zeichenkette.
		 * @return  Wert des Arguments.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public String asString() {
			return getValue();
		}

		/**
		 * Bestimmt den Wert des Arguments als nicht leere Zeichenkette.
		 * @return  Wert des Arguments.
		 * @throws IllegalArgumentException  Wenn der Argumentwert leer ist.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public String asNonEmptyString() {
			String value= getValue();
			if(value.equals("")) throw new IllegalArgumentException("Argument " + getName() + " darf nicht leer sein");
			return value;
		}

		public ValueCase asValueCase(ValueSelection validValues) {
			String value= getValue();
			ValueCase valueCase= validValues.get(value);
			if(valueCase!=null) return valueCase;
			throw new IllegalArgumentException(
				"Argument " + getName() + " hat einen ungültigen Wert: \"" + value +
				"\", erlaubt sind folgende Werte:" + validValues.getInfo()
			);
		}

		/**
		 * Bestimmt den Wert des Arguments als Enum-Konstante.
		 * @param typeClass Klasse von dem der Enum-Wert eingelesen werden soll. Unterstützt native Enum-Klassen und Enum-ähnliche Klassen,
		 *                  mit festen öffentlichen Konstanten. Groß- und Kleinschreibung wird ignoriert.
		 * @return  Wert des Arguments.
		 * @throws IllegalArgumentException  Wenn der Argumentwert leer oder ungültig ist.
		 */
		public <E> E asEnum(final Class<E> typeClass) {
			final List<String> validValues = new ArrayList<String>();
			String value = getValue();
			E[] enumConstants = typeClass.getEnumConstants();
			if(enumConstants != null){
				for(E e : enumConstants) {
					if(((Enum)e).name().equalsIgnoreCase(value)) return e;
					if(e.toString().equalsIgnoreCase(value)) return e;
					validValues.add(e.toString());
				}
			}
			else {
				try {
					Field[] fields = typeClass.getFields();
					for(Field field : fields) {
						if(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getType()
								.equals(typeClass)) {
							Object fieldValue = field.get(null);
							if(field.getName().equalsIgnoreCase(value)) return (E) fieldValue;
							if(fieldValue.toString().equalsIgnoreCase(value)) return (E) fieldValue;
							validValues.add(fieldValue.toString());
						}
					}
				}
				catch(IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			throw new IllegalArgumentException(
					"Argument " + getName() + " hat einen ungültigen Wert: \"" + value +
							"\", erlaubt sind folgende Werte: " + validValues
			);
		}

		/**
		 * Bestimmt den Wert des Arguments als Kommunikationsadresse.
		 * Der Argumentwert muss dabei einem der folgenden folgenden Formate entsprechen:
		 * <code>"Protokoll:Adresse:Subadresse"</code> , <code>"Adresse:Subadresse"</code> , <code>"Adresse"</code>.
		 * <code>Protokoll</code> spezifiziert den das zu verwendende Kommunikationsprotokoll,
		 * <code>Adresse</code> spezifiziert die Geräteadresse (z.B. IP-Adresse oder Rechnername),
		 * <code>Subadresse</code> spezifiziert die zu verwendende Subadresse (z.B. Portnummer bei TCP).
		 * Als <code>Protokoll</code>  und <code>Adresse</code> werden beliebige Texte akzeptiert,
		 * Als <code>Subadresse</code> werden beliebige Zahlen vom Typ <code>int</code> akzeptiert.
		 * Wenn <code>Protokoll</code> nicht angegeben wurde, wird das entsprechende Feld auf den leerer Text gesetzt,
		 * wenn keine Subadresse angegeben wurde, wird das entsprechende Feld auf den Wert 0 gesetzt.
		 * Weitere Prüfungen und die Behandlung der Defaults sind abhängig von der Anwendung und dem verwendeten
		 * Protokoll und müssen an anderer Stelle stattfinden.
		 * @return  Objekt der Klasse CommunicationAddress über das auf Protokoll, Adresse und Subadresse zugegriffen
		 *          werden kann.
		 */
		CommunicationAddress asCommunicationAddress() {
			String[] parts= getValue().split(":", 3);
			String protocol= "";
			String host;
			int port=0;
			int partIndex= 0;
			if(parts.length==3) protocol= parts[partIndex++];
			host= parts[partIndex++];
			if(partIndex<parts.length) port= Integer.parseInt(parts[partIndex++]);
			return new CommunicationAddress(protocol, host, port);
		}

		/**
		 * Interpretiert den Wert des Arguments als Zeitangabe.
		 * Erkannt werden absolute Zeitangabe wie in der Methode {@link #asAbsoluteTime}
		 * und relative Zeitangaben wie in der Methode @{link #asRelativeTime}.
		 * Wenn eine relative Zeitangabe angegeben wurde, wird der angegebene Wert
		 * vom aktuellen Zeitpunkt abgezogen d.h. das Ergebnis liegt bei positiven Angaben
		 * in der Vergangenheit liegt.
		 * @return Anzahl Millisekunden seit 1970.
		 */
		public long asTime() {
			try {
				return asAbsoluteTime();
			}
			catch(Exception e) {
				return -asRelativeTime() + System.currentTimeMillis();
			}
		}

		private static final DateFormat[] _parseDateFormats= new DateFormat[] {
			new SimpleDateFormat("HH:mm:ss,SSS dd.MM.yy"),
			new SimpleDateFormat("HH:mm:ss dd.MM.yy"),
			new SimpleDateFormat("HH:mm dd.MM.yy"),
			new SimpleDateFormat("dd.MM.yy HH:mm:ss,SSS"),
			new SimpleDateFormat("dd.MM.yy HH:mm:ss"),
			new SimpleDateFormat("dd.MM.yy HH:mm"),
			new SimpleDateFormat("dd.MM.yy"),
		};

		private static final DateFormat[] _parseTimeFormats= new DateFormat[] {
			new SimpleDateFormat("HH:mm:ss,SSS"),
			new SimpleDateFormat("HH:mm:ss"),
			new SimpleDateFormat("HH:mm"),
		};

		private static final long _startDay;

		static {
			Calendar startDay= new GregorianCalendar();
			//startDay.setTimeInMillis(System.currentTimeMillis());
			startDay.set(Calendar.HOUR_OF_DAY, 0);
			startDay.set(Calendar.MINUTE, 0);
			startDay.set(Calendar.SECOND, 0);
			startDay.set(Calendar.MILLISECOND, 0);
			_startDay= startDay.getTimeInMillis();
			for(int i= 0; i < _parseTimeFormats.length; ++i) {
				_parseTimeFormats[i].setTimeZone(TimeZone.getTimeZone("GMT"));
			}
		}

		/**
		 * Interpretiert den Wert des Arguments als absolute Zeitangabe.
		 * Das Argument muss aus einer Zeitangabe im Format HH:MM[:SS[,mmm]]
		 * und/oder aus einer Datumsangabe im Format TT.MM.[YY]YY bestehen.
		 * Die Reihenfolge von Datum und Zeit ist egal. Wenn nur eine Zeitangabe
		 * im Argument enthalten ist, wird als Datum der Tag benutzt, an dem
		 * das Programm gestartet wurde. Wenn nur eine Datumsangabe im Argument
		 * enthalten ist, dann wird als Zeitangabe 0:00 Uhr Lokalzeit benutzt.
		 * Bei Datumsangaben mit zweistelliger Jahreszahl wird ein Jahr gewählt,
		 * das im Bereich von 80 Jahren vor und 20 Jahren nach dem aktuellen Jahr
		 * liegt.
		 * Als spezieller Wert wird der Text "jetzt" erkannt und durch die
		 * beim Aufruf der Methode aktuelle Zeit interpretiert.
		 * @return Anzahl Millisekunden seit 1970.
		 */
		public long asAbsoluteTime() {
			DateFormat format;
			Date date;
			String value= getValue();
			if(value.toLowerCase().equals("jetzt")) return System.currentTimeMillis();
			value= value.replace('-',' ');
			for(int i= 0; i < _parseDateFormats.length; ++i) {
				format= _parseDateFormats[i];
				try {
					synchronized(format) {
						date= format.parse(value);
					}
					return date.getTime();
				}
				catch(ParseException e) {
					//continue with next Format
				}
			}
			for(int i= 0; i < _parseTimeFormats.length; ++i) {
				format= _parseTimeFormats[i];
				try {
					synchronized(format) {
						date= format.parse(getValue());
					}
					//long today= (System.currentTimeMillis() / (1000 * 60 * 60 * 24) ) * (1000 * 60 * 60 * 24);
					return date.getTime() + _startDay;
				}
				catch(ParseException e) {
					//continue with next Format
				}
			}
			throw new IllegalArgumentException("keine absolute Zeitangabe");
		}

		private static final String _relNumberPattern= "-?(?:(?:0[0-7]{1,22}+)|(?:[1-9][0-9]{0,18}+)|(?:(?:#|0x|0X)[0-9a-fA-F]{0,16}+)|(?:0))";
		private static final String _relNamePattern= "[tThHsSmM][a-zA-Z]{0,15}+";
		private static final String _relNumberNamePattern= "(?<=" +_relNumberPattern + ")\\s*(?=" + _relNamePattern + ")";
		private static final String _relNameNumberPattern= "(?<=" +_relNamePattern + ")\\s*(?=" + _relNumberPattern + ")";
		private static final String _relPattern= "(?:" + _relNumberNamePattern + ")|(?:" + _relNameNumberPattern + ")";

		/**
		 * Interpretiert den Wert des Arguments als relative Zeitangabe.
		 * Das Argument muss aus einer Liste von Zahlen und Einheiten bestehen.
		 * Als Einheiten sind "t" und "Tag[e]" für Tage, "h" und "Stunde[n]" für Stunden,
		 * "m" und "Minute[n]" für Minuten, "s" und "Sekunde[n]" für Sekunden
		 * sowie "ms" und "Millisekunden[e]" für Millisekunden erkannt.
		 * Die Einzelnen Werte werden in Millisekunden umgerechnet und aufsummiert.
		 * Als spezieller Wert wird der Text "jetzt" erkannt und als "0 Sekunden"
		 * interpretiert.
		 * @return  Relative Zeitangabe in Millisekunden.
		 */
		public long asRelativeTime() {
			String value= getValue();
			if(value.toLowerCase().equals("jetzt")) return 0;
			String[] splitted= value.trim().split(_relPattern);
			long number= 0;
			long millis= 0;
			for(int i=0; i< splitted.length; ++i) {
				String word= splitted[i];
				number= Long.decode(word).longValue();
				if(++i<splitted.length) {
					word= splitted[i].toLowerCase();
					if(word.equals("t") || word.startsWith("tag")) {
						millis+= (1000*60*60*24) * number;
					}
					else if(word.equals("h") || word.startsWith("stunde")) {
						millis+= (1000*60*60) * number;
					}
					else if(word.equals("m") || word.startsWith("minute")) {
						millis+= (1000*60) * number;
					}
					else if(word.equals("s") || word.startsWith("sekunde")) {
						millis+= 1000 * number;
					}
					else if(word.equals("ms") || word.startsWith("milli")) {
						millis+= number;
					}
					else throw new IllegalArgumentException("Ungültige relative Zeitangabe: " + splitted[i]);
				}
				else if(number!=0) throw new IllegalArgumentException("Einheit bei relativer Zeitangabe fehlt");
			}
			return millis;
		}

		/**
		 * Erzeugt eine Zeichenkette, die den Namen und den Wert des Arguments enthält.
		 * @return  Zeichenkette mit Name und Wert des Arguments.
		 * @throws IllegalStateException  Wenn das Argument keinen Wert hat.
		 */
		public String toString() {
			return "Argument " + _name + (hasValue() ? "=" + _value : "");
		}


	}

	/**
	 * Hauptfunktion zum Test einzelner Methoden
	 */
	public static void main(String[] args) {
		try {
			DateFormat outputFormat= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
			ArgumentList arguments= new ArgumentList(args);
			long fromTime= arguments.fetchArgument("-von=24:00").asTime();
			long duration= arguments.fetchArgument("-dauer=011t#011m0s").asRelativeTime();

			long now= System.currentTimeMillis();
			System.out.println("now:" + now + " ms");
			System.out.println("now:" + outputFormat.format(new Date(now)));
			System.out.println("fromTime:" + fromTime + " ms");
			System.out.println("fromTime:" + outputFormat.format(new Date(fromTime)));
			System.out.println("duration:" + duration + " ms");
			System.out.println("duration:" + outputFormat.format(new Date(duration)));
			arguments.ensureAllArgumentsUsed();
		}
		catch(Exception e) {
			System.out.println("Fehler: " + e);
			System.exit(1);
			return;
		}
	}

	/**
	 * Liefert das String-Array mit den noch nicht interpretierten Aufrufargumenten der Applikation zurück.
	 * Das zurückgegebene Objekt ist das selbe Objekt mit dem der Konstruktor aufgerufen wurde, allerdings
	 * ist zu beachten, dass die Elemente des Arrays, die bereits mit den Methoden {@link #fetchArgument} bzw.
	 * {@link #fetchNextArgument} interpretiert wurden, im Array auf <code>null</code> gesetzt wurden.
	 * @return Das String-Array mit den noch nicht interpretierten Aufrufargumenten der Applikation.
	 */
	public String[] getArgumentStrings() {
		return _argumentStrings;
	}

	/**
	 * Liefert das String-Array mit den initialen Aufrufargumenten der Applikation zurück.
	 * Das zurückgegebene Array enthält die selben Aufrufargumente die dem Konstruktor übergeben wurden.
	 * @return Das String-Array mit den initialen Aufrufargumenten der Applikation.
	 */
	public String[] getInitialArgumentStrings() {
		return _initialArgumentStrings;
	}

	public static class ValueSelection {
		List _valueCases= new LinkedList();

		public ValueSelection() {
		}

		public ValueCase add(String caseName) {
			ValueCase valueCase= new ValueCase(caseName);
			_valueCases.add(valueCase);
			return valueCase;
		}

		ValueCase get(String caseName) {
			Iterator caseIterator= _valueCases.iterator();
			while(caseIterator.hasNext()) {
				ValueCase valueCase= (ValueCase)caseIterator.next();
				if(valueCase.matches(caseName)) return valueCase;
			}
			return null;
		}

		public String toString() {
			StringBuffer result= new StringBuffer();
			result.append("ValueSelection{");
			Iterator caseIterator= _valueCases.iterator();
			while(caseIterator.hasNext()) {
				result.append(((ValueCase)caseIterator.next()).toString());
				if(caseIterator.hasNext()) result.append(", ");
			}
			result.append("}");
			return result.toString();
		}

		public String getInfo() {
			StringBuffer result= new StringBuffer();
			Iterator caseIterator= _valueCases.iterator();
			while(caseIterator.hasNext()) {
				result.append(((ValueCase)caseIterator.next()).getInfo());
			}
			return result.toString();
		}
	}

	public static class ValueCase{
		List _caseNames= new LinkedList();
		boolean _ignoreCase= false;
		Object _conversion= null;
		String _description= null;

		ValueCase(String caseName) {
			alias(caseName);
		}

		public ValueCase alias(String aliasName) {
			_caseNames.add(aliasName);
			return this;
		}

		public ValueCase ignoreCase() {
			_ignoreCase= true;
			return this;
		}

		public ValueCase checkCase() {
			_ignoreCase= false;
			return this;
		}

		public ValueCase convertTo(Object object) {
			_conversion= object;
			return this;
		}

		public ValueCase convertTo(int conversionValue) {
			_conversion= new Integer(conversionValue);
			return this;
		}

		public ValueCase convertTo(boolean conversionValue) {
			_conversion= conversionValue ? Boolean.TRUE : Boolean.FALSE;
			return this;
		}

		public ValueCase purpose(String description) {
			_description= description;
			return this;
		}

		public Object convert() {
			return _conversion;
		}

		public int intValue() {
			return ((Number)_conversion).intValue();
		}

		public boolean booleanValue() {
			return ((Boolean)_conversion).booleanValue();
		}

		public boolean matches(String name) {
			if(_ignoreCase) name= name.toLowerCase();
			Iterator nameIterator= _caseNames.iterator();
			while(nameIterator.hasNext()) {
				String caseName= (String)nameIterator.next();
				if(_ignoreCase) caseName= caseName.toLowerCase();
				if(name.equals(caseName)) return true;
			}
			return false;
		}

		public String toString() {
			StringBuffer result= new StringBuffer("ValueCase{");
			if(_description!=null) {
				result.append('"');
				result.append(_description);
				result.append('"');
				result.append(",");
			}
			result.append(_ignoreCase ? "ignoreCase" : "checkCase");
			result.append(",match{");
			Iterator nameIterator= _caseNames.iterator();
			while(nameIterator.hasNext()) {
				result.append('"');
				result.append((String)nameIterator.next());
				result.append('"');
				if(nameIterator.hasNext()) result.append(",");
			}
			result.append("}");
			if(_conversion!=null) {
				result.append(",conversion:");
				result.append(_conversion);
			}
			result.append("}");
			return result.toString();
		}

		public String getInfo() {
			StringBuffer result= new StringBuffer();
			result.append(System.getProperty("line.separator")).append("   ");
			Iterator nameIterator= _caseNames.iterator();
			while(nameIterator.hasNext()) {
				result.append('"').append((String)nameIterator.next()).append('"').append(", ");
			}
			result.append("(").append(_ignoreCase ? "ohne" : "mit").append(" Prüfung der Groß-/Kleinschreibung)");
			if(_description!=null) {
				result.append(System.getProperty("line.separator")).append("      Zweck: ").append(_description);
			}
			return result.toString();
		}
	}

	/**
	 * Liefert eine textuelle Beschreibung dieser Argumentliste mit den initialen Argumenten zurück.
	 * Das genaue Format ist nicht festgelegt und kann sich ändern.
	 * @return Beschreibung dieser Argumentliste.
	 */
	public String toString() {
		return "ArgumentList" + Arrays.asList(_initialArgumentStrings).toString();
	}
}

