/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.util.cron;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Definition eines Cron-Eintrages.
 * <p>
 * Ein Cron-Eintrag besteht aus 5 Werten:
 * <p>
 * <ol> <li>Minute (0-59)</li> <li>Stunde (0-23)</li> <li>Tag des Monats (0-31)</li> <li>Monat (1-12 oder Monatsnamen)</li> <li>Tag der
 * Woche (0-7, 0 und 7 sind Sonntag, oder Namen)</li> </ol>
 * <p>
 * Soll ein Wert ignoriert werden, sind Sternchen möglich.
 * <p>
 * Listen und Bereiche können ebenfalls angegeben werden mit "," und "-". Mit / kann der Befehl alle x Intervalle ausgeführt werden.
 * <p>
 * Wochen und Monatsnamen sind in deutscher Sprache anzugeben und werden tolerant behandelt. Gültig für Sonntag ist beispielsweise "so",
 * "son", "Sonntag", usw., Groß- und Kleinschreibung ist unerheblich.
 * <p>
 * Beispiele:<br/>
 * <code><pre>
 * Min    Std  Tag  Mon   WT
 * &nbsp;&nbsp;5      0    *    *    *    Jeden Tag um 00:05:00
 * &nbsp;15  14,20    1    *    *    Am 1. jeden Monats um 14:15:00 und um 20:15:00
 * &nbsp;&nbsp;0     22    *    *  1-5    An jedem Werktag (Mo-Fr) um 22:00:00
 * &nbsp;23  * / 2    *    *    *    Alle 2 Stunden um jeweils xx:23:00, also 00:23:00, 02:23:00, ...
 * &nbsp;&nbsp;5      4    *    *  son    Jeden Sonntag um 04:05:00
 * &nbsp;&nbsp;0      1    1   12    1    Jeden 1. Dezember UND jeden Montag im Dezember jeweils um 01:00:00
 * </pre></code>
 * <p>
 * Für weitere Erklärungen und Beispiele siehe Dokumentation der /etc/crontab oder https://de.wikipedia.org/wiki/Cron
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public final class CronDefinition {

	private final BitSet _minutes = new BitSet(60); // Minuten 0-59
	private final BitSet _hours = new BitSet(24);   // Stunden 0-32
	private final BitSet _days = new BitSet(31);    // Tage im Monat 0-30 (+1 Rechnen für richtigen Tag)
	private final BitSet _months = new BitSet(12);  // Monate 0-11 (+1 Rechnen für richtigen Monat)
	private final BitSet _dayOfWeek = new BitSet(7); // Wochentage mit 0=Sonntag, 1=Montag,...
	private final ThreadLocal<Calendar> calendar = new ThreadLocal<Calendar>() {
		@Override
		protected Calendar initialValue() {
			return Calendar.getInstance();
		}
	};
	private final String[] _segments;

	/**
	 * Erstellt eien neue Cron-Definition zum periodischen Ausführen von Aufgaben. Diese Klasse dient nur zum Festlegen der Zeitpunkte.
	 * Zum Planen von Aufgaben bitte {@link de.bsvrz.dav.daf.util.cron.CronScheduler#schedule(Runnable, CronDefinition)} verwenden.
	 *
	 * @param definition Definition, siehe Dokumentation von {@link de.bsvrz.dav.daf.util.cron.CronDefinition}. Beispiel: "* * * * *"
	 *                   führt eine Aufgabe jede Minute aus.
	 * @throws IllegalArgumentException Wenn der übergebene String nicht verarbeitet werden kann.
	 */
	public CronDefinition(String definition) throws IllegalArgumentException {
		// Überflüssige Leerzeichen entfernen
		definition = definition.replaceAll("\\s*,\\s*", ",");
		definition = definition.replaceAll("\\s*/\\s*", "/");
		definition = definition.replaceAll("\\s*-\\s*", "-");

		// In 5 Teile Auftrennen
		_segments = definition.trim().split("\\s+");
		if(_segments.length != 5) {
			throw new IllegalArgumentException("Cron-String benötigt 5 Angaben");
		}

		// Einzelne Bestandteile parsen und Bitsets füllen
		IntParser intParser = new IntParser();
		try {
			intParser.parseField(_minutes, 0, 59, _segments[0]);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Ungültige Minuten-Angabe: '" + _segments[0] + "'", e);
		}
		try {
			intParser.parseField(_hours, 0, 23, _segments[1]);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Ungültige Stunden-Angabe: '" + _segments[1] + "'", e);
		}
		try {
			new DoMParser().parseField(_days, 0, 30, _segments[2]);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Ungültige Tages-Angabe: '" + _segments[2] + "'", e);
		}
		try {
			new MonthParser().parseField(_months, 0, 11, _segments[3]);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Ungültige Monats-Angabe: '" + _segments[3] + "'", e);
		}
		try {
			new DoWParser().parseField(_dayOfWeek, 0, 7, _segments[4]);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Ungültige Wochentags-Angabe: '" + _segments[4] + "'", e);
		}

		if(nextScheduledTime(0) == -1){
			// ungültige Definition
			throw new IllegalArgumentException("Ungültige Cron-Definition, Datum wird nie gültig");
		}
	}

	/**
	 * Gibt den nächsten geplanten Zeitpunkt zurück
	 * @param startTimeMillis Startzeit
	 * @return Nächster Zeitpunkt in Millisekungen analog zu System.currentTimeMillis()
	 */
	public long nextScheduledTime(long startTimeMillis) {
		Calendar cal = calendar.get();
		cal.setTimeInMillis(startTimeMillis);
		if(cal.get(Calendar.SECOND) > 0 || cal.get(Calendar.MILLISECOND) > 0) {
			// Nächste Minute
			cal.add(Calendar.MINUTE, 1);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		int startYear = cal.get(Calendar.YEAR);
		while(true) {
			int min = cal.get(Calendar.MINUTE);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day = cal.get(Calendar.DAY_OF_MONTH) - 1;  // 0-Indiziert
			int month = cal.get(Calendar.MONTH);
			int dow = cal.get(Calendar.DAY_OF_WEEK) - 1; // Woche startet hier mit Sonntag = 0
			int year = cal.get(Calendar.YEAR);
			if(year > startYear + 100){
				// Keine Ausführung in den nächsten 100 Jahren, wahrscheinlich weil ungültiges Datum wie 31. Februar angegeben wurde.
				return -1;
			}
			if(!_months.get(month)) {
				// falscher Monat, beginn nächsten Monats auswählen
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.add(Calendar.MONTH, 1);
				continue;
			}
			if(_segments[2].equals("*") && !_dayOfWeek.get(dow)) {
				// falscher Wochentag, beginn nächsten Tags auswählen
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			if(_segments[4].equals("*") && !_days.get(day)) {
				// falscher Tag des Monats, beginn nächsten Tags auswählen
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			if(!_dayOfWeek.get(dow) || !_days.get(day)) {
				// falscher Tag (ODER-Bedingung-Sonderfall), beginn nächsten Tags auswählen
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			if(!_hours.get(hour)) {
				// falsche Stunde, beginn nächster Stunde auswählen
				cal.set(Calendar.MINUTE, 0);
				cal.add(Calendar.HOUR_OF_DAY, 1);
				continue;
			}
			if(!_minutes.get(min)) {
				// falsche Minute, nächste Minute auswählen
				cal.add(Calendar.MINUTE, 1);
				continue;
			}
			// Es scheint alles zu passen
			return cal.getTimeInMillis();
		}
	}

	/**
	 * Gibt eine Liste mit 5 Strings zurück, die die Einträge für Minuten, Stunden, Monatstag, Monat und Tag der Woche in dieser Reihenfolge
	 * repräsentiert. Dies entspricht dem im Kontruktor übergebenen String, wobei überflüssige Leerzeichen entfernt wurden und der String
	 * entsprechend der Cron-Spezifikation in seine 5 Abschnitte geteilt wurde.
	 *
	 * @return Liste mit 5 Strings, die die Cron-Definition repräsentieren
	 */
	public List<String> getSegments() {
		return Collections.unmodifiableList(Arrays.asList(_segments));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\"");
		for(String segment : _segments) {
			builder.append(String.format("%8s", segment)).append(" ");
		}
		builder.setLength(builder.length()-1);
		long scheduledTime = nextScheduledTime(System.currentTimeMillis());
		builder.append("\" Nächste Ausführung: ");
		builder.append(new SimpleDateFormat("EEE, d. MMM yyyy HH:mm", Locale.GERMANY).format(scheduledTime));
		return builder.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final CronDefinition that = (CronDefinition) o;

		if(!_dayOfWeek.equals(that._dayOfWeek)) return false;
		if(!_days.equals(that._days)) return false;
		if(!_hours.equals(that._hours)) return false;
		if(!_minutes.equals(that._minutes)) return false;
		if(!_months.equals(that._months)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = _minutes.hashCode();
		result = 31 * result + _hours.hashCode();
		result = 31 * result + _days.hashCode();
		result = 31 * result + _months.hashCode();
		result = 31 * result + _dayOfWeek.hashCode();
		return result;
	}

	private static class IntParser {
		private static void setBits(final BitSet bitSet, final int min, final int max, final int interval) {
			if(interval <= 0) throw new IllegalArgumentException();
			for(int i = min; i <= max; i += interval) {
				bitSet.set(i);
			}
		}

		public void parseField(final BitSet bitSet, final int min, final int max, final String definition) {
			String[] segments = definition.split(",");
			for(String segment : segments) {
				parseSegment(bitSet, min, max, segment);
			}
		}

		public void parseSegment(final BitSet bitSet, final int min, final int max, final String segment) {
			String[] split = segment.split("/");
			if(split.length == 1) {
				parseRange(bitSet, min, max, segment, 1);
			}
			else if(split.length == 2) {
				parseRange(bitSet, min, max, split[0], Integer.parseInt(split[1]));
			}
			else {
				throw new IllegalArgumentException();
			}
		}

		public void parseRange(final BitSet bitSet, final int min, final int max, final String range, final int interval) {
			if(range.equals("*")) {
				setBits(bitSet, min, max, interval);
				return;
			}
			String[] split = range.split("-");
			if(split.length == 1) {
				int val = parseInt(split[0]);
				if(val < min) throw new IllegalArgumentException();
				if(val > max) throw new IllegalArgumentException();
				setBits(bitSet, val, val, 1);
			}
			else if(split.length == 2) {
				int myMin = parseInt(split[0]);
				int myMax = parseInt(split[1]);
				if(myMin < min) throw new IllegalArgumentException();
				if(myMax > max) throw new IllegalArgumentException();
				setBits(bitSet, myMin, myMax, 1);
			}
		}

		public int parseInt(final String s) {
			return Integer.parseInt(s);
		}
	}

	private static class MonthParser extends IntParser {

		private SimpleDateFormat _dateFormat = new SimpleDateFormat("MMM", Locale.GERMANY);

		@Override
		public int parseInt(final String s) {
			try {
				return _dateFormat.parse(s).getMonth();
			}
			catch(ParseException e) {
				try {
					return super.parseInt(s) - 1;
				}
				catch(IllegalArgumentException ignored){
					//e.addSuppressed(ignored);
					throw new IllegalArgumentException(e);
				}
			}
		}
	}

	private static class DoWParser extends IntParser {

		private SimpleDateFormat _dateFormat = new SimpleDateFormat("EEE", Locale.GERMANY);

		@Override
		public int parseInt(final String s) {
			try {
				return _dateFormat.parse(s).getDay();
			}
			catch(ParseException e) {
				try {
					return super.parseInt(s) % 7;
				}
				catch(IllegalArgumentException ignored){
					//e.addSuppressed(ignored);
					throw new IllegalArgumentException(e);
				}
			}
		}
	}

	private static class DoMParser extends IntParser {
		@Override
		public int parseInt(final String s) {
			return super.parseInt(s)-1;
		}
	}
}
