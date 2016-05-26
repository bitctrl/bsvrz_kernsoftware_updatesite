/*
 * Copyright 2006 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.dataRepresentation.data.util;

import de.bsvrz.dav.daf.communication.dataRepresentation.UndefinedValueHandler;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Klasse, die Hilfsmethoden zum Zugriff und zur Konvertierung von Attributwerten zur Verfügung stellt.
 *
 * @author Roland Schmitz (rs), Kappich Systemberatung
 * @version $Revision: none $, $Date: 01.12.2006 $, $Author: rs $
 */
public class AttributeValues {

	private static Pattern _locationDistancePattern = Pattern.compile("[0-9]{1,5}\\s*-\\s*[0-9]{1,3}");

	private static final NumberFormat _parseNumberFormat = NumberFormat.getNumberInstance();

	static {
		_parseNumberFormat.setMinimumIntegerDigits(1);
		_parseNumberFormat.setMaximumIntegerDigits(999);
		_parseNumberFormat.setMinimumFractionDigits(0);
		_parseNumberFormat.setMaximumFractionDigits(999);
		_parseNumberFormat.setGroupingUsed(false);
	}

	public static void checkValue(final AttributeType attributeType, final String textValue, final ObjectLookup objectLookup) {
		// Defaultwert vorhanden, ist er OK?
		if(attributeType instanceof DoubleAttributeType) {
			DoubleAttributeType doubleAttributeType = (DoubleAttributeType)attributeType;
			textToDouble(doubleAttributeType, textValue);
		}
		else if(attributeType instanceof IntegerAttributeType) {
			IntegerAttributeType integerAttributeType = (IntegerAttributeType)attributeType;
			textToUnscaled(integerAttributeType, textValue);
		}
		else if(attributeType instanceof ReferenceAttributeType) {
			ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType)attributeType;
			textToReference(referenceAttributeType, textValue, objectLookup);
		}
		else if(attributeType instanceof StringAttributeType) {
			StringAttributeType stringAttributeType = (StringAttributeType)attributeType;
			checkText(stringAttributeType, textValue);
		}
		else if(attributeType instanceof TimeAttributeType) {
			TimeAttributeType timeAttributeType = (TimeAttributeType)attributeType;
			textToMillis(timeAttributeType, textValue);
		}
	}

	private static long textToMillis(final TimeAttributeType timeAttributeType, final String textValue) {
		if(timeAttributeType.isRelative()) {
			return relativeTimeToMillis(textValue);
		}
		else {
			return absoluteTimeToMillis(textValue);
		}
	}

	private static final String _relNumberPattern = "-?(?:(?:0[0-7]{1,22}+)|(?:[1-9][0-9]{0,18}+)|(?:(?:#|0x|0X)[0-9a-fA-F]{0,16}+)|(?:0))";

	private static final String _relNamePattern = "[tThHsSmM][a-zA-Z]{0,15}+";

	private static final String _relNumberNamePattern = "(?<=" + _relNumberPattern + ")\\s*(?=" + _relNamePattern + ")";

	private static final String _relNameNumberPattern = "(?<=" + _relNamePattern + ")\\s*(?=" + _relNumberPattern + ")";

	private static final String _relPattern = "(?:" + _relNumberNamePattern + ")|(?:" + _relNameNumberPattern + ")";

	private static long relativeTimeToMillis(final String text) {
		String[] splitted = text.trim().split(_relPattern);
		long number = 0;
		long millis = 0;
		for(int i = 0; i < splitted.length; ++i) {
			String word = splitted[i];
			number = Long.decode(word).longValue();
			if(++i < splitted.length) {
				word = splitted[i].toLowerCase();
				if(word.equals("t") || word.startsWith("tag")) {
					millis += (1000 * 60 * 60 * 24) * number;
				}
				else if(word.equals("h") || word.startsWith("stunde")) {
					millis += (1000 * 60 * 60) * number;
				}
				else if(word.equals("m") || word.startsWith("minute")) {
					millis += (1000 * 60) * number;
				}
				else if(word.equals("s") || word.startsWith("sekunde")) {
					millis += 1000 * number;
				}
				else if(word.equals("ms") || word.startsWith("milli")) {
					millis += number;
				}
				else {
					throw new IllegalArgumentException("Ungültige relative Zeitangabe: " + splitted[i]);
				}
			}
			else {
				throw new IllegalArgumentException("Fehlende Einheit bei relativer Zeitangabe: " + text);
			}
		}
		return millis;
	}

	private static final DateFormat[] _parseDateFormats = new DateFormat[]{new SimpleDateFormat("dd.MM.yy HH:mm:ss,SSS"),
	                                                                       new SimpleDateFormat("dd.MM.yy HH:mm:ss"), new SimpleDateFormat("dd.MM.yy HH:mm"),
	                                                                       new SimpleDateFormat("dd.MM.yy"),};

	private static long absoluteTimeToMillis(final String text) {
		DateFormat format;
		Date date;
		for(int i = 0; i < _parseDateFormats.length; ++i) {
			format = _parseDateFormats[i];
			try {
				synchronized(format) {
					date = format.parse(text);
				}
				return date.getTime();
			}
			catch(ParseException e) {
				//continue with next Format
			}
		}
		throw new IllegalArgumentException(
				"Ungültig Zeitangabe '" + text + "' (Unterstützte Formate: 'dd.MM.yy HH:mm:ss,SSS', 'dd.MM.yy HH:mm:ss', 'dd.MM.yy HH:mm', 'dd.MM.yy')"
		);
	}

	private static SystemObject textToReference(final ReferenceAttributeType referenceAttributeType, final String textValue, final ObjectLookup lookup) {
		int startIndex;
		boolean tryPid = true;
		boolean tryId = true;
		String text = textValue;
		String lowercaseText = text.toLowerCase();
		startIndex = lowercaseText.lastIndexOf("pid:");
		if(startIndex >= 0) {
			startIndex += 4;
			tryId = false;
		}
		else {
			startIndex = lowercaseText.lastIndexOf("id:");
			if(startIndex >= 0) {
				startIndex += 3;
				tryPid = false;
			}
			else {
				// Wenn weder "Pid:" noch "Id:" gefunden wurde, dann wird von vorne gesucht
				startIndex = 0;
			}
		}
		text = text.substring(startIndex).trim();
		if(tryId) {
			String numberText = text.split("\\D", 2)[0];
			if(numberText.length() > 0) {
				long id = Long.parseLong(numberText);
				if(id == 0) {
					if(referenceAttributeType.isUndefinedAllowed()) {
						return null;
					}
					else {
						throw new IllegalArgumentException(
								"Attributwert '" + textValue + "' wird als undefinierte Referenz interpretiert, diese sind allerdings am Attributtyp '"
								+ referenceAttributeType.getPid() + "' nicht zugelassen."
						);
					}
				}
				try {
					SystemObject object;
					object = lookup.getObject(id);
					if(object != null) return object;
				}
				catch(Exception e) {
					// Ignorieren
				}
			}
		}
		if(tryPid) {
			String pid = text.split("[\\s\\Q[]{}():\\E]", 2)[0];
			if(pid.equals("null") || pid.equals("undefiniert")) {
				if(referenceAttributeType.isUndefinedAllowed()) {
					return null;
				}
				else {
					throw new IllegalArgumentException(
							"Attributwert '" + textValue + "' wird als undefinierte Referenz interpretiert, diese sind allerdings am Attributtyp '"
							+ referenceAttributeType.getPid() + "' nicht zugelassen."
					);
				}
			}
			try {
				SystemObject object;
				object = lookup.getObject(pid);
				if(object != null) return object;
			}
			catch(Exception e) {
				// Ignorieren
			}
		}
		throw new IllegalArgumentException("Der Text '" + text + "' kann nicht als Objektreferenz interpretiert werden.");
	}

	private static void checkText(final StringAttributeType stringAttributeType, final String textValue) {
		final int maxLength = stringAttributeType.getMaxLength();
		if(maxLength != 0 && maxLength < textValue.length()) {
			throw new IllegalArgumentException("Attribut enthält mehr als die höchstens erlaubten " + maxLength + " Zeichen");
		}
	}

	private static long textToUnscaled(final IntegerAttributeType type, String text) {
		IntegerValueState bestMatch = stateTextToUnscaled(type, text);
		if(bestMatch != null) return bestMatch.getValue();
		text = text.trim();

		final long unscaledValue;

		// Ist der Text im location-distance Format?
		if(_locationDistancePattern.matcher(text).matches()) {
			unscaledValue = locationDistanceToUnscaled(text);
		}
		else {
			unscaledValue = scaledToUnscaled(text, type);
		}
		checkRange(type, unscaledValue);
		return unscaledValue;
	}

	private static long scaledToUnscaled(final String text, final IntegerAttributeType type) {
		final long unscaledValue;
		Number number;
		ParsePosition parsePosition = new ParsePosition(0);
		synchronized(_parseNumberFormat) {
			number = _parseNumberFormat.parse(text.replace('.', ','), parsePosition);
		}
		if(number == null) throw new IllegalArgumentException("Text " + text + " kann nicht in eine Zahl konvertiert werden");
		unscaledValue = scaledToUnscaled(type, number);
		return unscaledValue;
	}

	private static long locationDistanceToUnscaled(final String text) {
		String[] locationDistance = text.split("\\s*-\\s*");
		int location = Integer.parseInt(locationDistance[0]);
		int distance = Integer.parseInt(locationDistance[1]);
		if(location > 65535) {
			throw new IllegalArgumentException("Wert im Location-Distance Format aber Location zu groß: " + location);
		}
		if(distance > 255) {
			throw new IllegalArgumentException("Wert im Location-Distance Format aber Distance zu groß: " + distance);
		}
		return (location * 256) + distance;
	}

	private static IntegerValueState stateTextToUnscaled(final IntegerAttributeType type, final String text) {
		List<IntegerValueState> states = type.getStates();
		Iterator<IntegerValueState> i = states.iterator();
		int matchLength = 0;
		IntegerValueState bestMatch = null;
		while(i.hasNext()) {
			IntegerValueState state = i.next();
			if(text.equals(state.getName())) {
				bestMatch = state;
				break;
			}
			if(text.startsWith(state.getName())) {
				if(state.getName().length() > matchLength) {
					bestMatch = state;
					matchLength = state.getName().length();
				}
			}
		}
		return bestMatch;
	}

	private static long scaledToUnscaled(final IntegerAttributeType type, final Number number) {
		IntegerValueRange range = type.getRange();
		if(range == null) {
			throw new IllegalArgumentException("Kein Wertebereich definiert");
		}
		double conversionFactor = range.getConversionFactor();
		if(conversionFactor == 1.0) {
			return number.longValue();
		}
		else {
			//Folgende Fallunterscheidung ist erforderlich um Rundungsfehler zu vermeiden
			//Beispielsweise führt Math.round(0.95 / 0.1) zum falschen Ergebnis 9
			//aber Math.round(0.95 * (1 / 0.1)) führt zum richtigen Ergebnis 10
			if(conversionFactor < 1.0) {
				return Math.round(number.doubleValue() * (1 / conversionFactor));
			}
			else {
				return Math.round(number.doubleValue() / conversionFactor);
			}
		}
	}

	private static void checkRange(final IntegerAttributeType type, final long unscaledValue) {
		IntegerValueRange range = type.getRange();
		if(range == null) {
			throw new IllegalArgumentException("Kein Wertebereich definiert");
		}
		if(unscaledValue < range.getMinimum() || range.getMaximum() < unscaledValue) {
			throw new IllegalArgumentException(
					"Unskalierter Wert " + unscaledValue + " liegt nicht im erlaubten Wertebereich [" + range.getMinimum() + ", " + range.getMaximum() + "]"
			);
		}
	}

	private static double textToDouble(final DoubleAttributeType doubleAttributeType, final String textValue) {
		Number number;
		ParsePosition parsePosition = new ParsePosition(0);
		synchronized(_parseNumberFormat) {
			number = _parseNumberFormat.parse(textValue.replace('.', ','), parsePosition);
		}
		if(number == null) throw new IllegalArgumentException("Text " + textValue + " kann nicht in eine Fließkommazahl konvertiert werden");
		double doubleValue = number.doubleValue();
		if(doubleAttributeType.getAccuracy() == DoubleAttributeType.FLOAT) {
			if(doubleValue < -(Float.MAX_VALUE) || Float.MAX_VALUE < doubleValue) {
				throw new IllegalArgumentException("Fließkommazahl " + number + " liegt außerhalb der Grenzen des 32 Bit Single-Formats");
			}
		}
		return doubleValue;
	}

	public static boolean hasUndefinedValue(final AttributeType attributeType) {
		if(attributeType instanceof IntegerAttributeType) {
			IntegerAttributeType integerAttributeType = (IntegerAttributeType)attributeType;
			final Long undefinedValue = UndefinedValueHandler.getInstance().getUndefinedValueInteger(integerAttributeType);
			return undefinedValue != null;
		}
		else if(attributeType instanceof StringAttributeType) {
			return true;
		}
		else if(attributeType instanceof TimeAttributeType) {
			return true;
		}
		else if(attributeType instanceof ReferenceAttributeType) {
			return true;
		}

		return false;
	}
}
