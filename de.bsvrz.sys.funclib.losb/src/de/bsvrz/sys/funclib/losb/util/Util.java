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

import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.archive.ArchiveData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveInformationResult;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hilfsklasse.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 9877 $ / $Date: 2012-01-26 15:00:50 +0100 (Thu, 26 Jan 2012) $ / ($Author: rs $)
 */
public class Util {

	private static final int FILE_BUFFER_SIZE = 16384;

	private static DecimalFormat df2 = new DecimalFormat("0.00");

	private static Pattern LINUX_DF_PATTERN = Pattern.compile("^\\S*\\s*\\S*\\s*\\S*\\s*(\\S*).*$");

	private static ArchiveDataKind[] ALL_ADK = new ArchiveDataKind[]{
			ArchiveDataKind.ONLINE, ArchiveDataKind.ONLINE_DELAYED, ArchiveDataKind.REQUESTED, ArchiveDataKind.REQUESTED_DELAYED
	};

	public static int OA = 0, ON = 1, NA = 2, NN = 3;

	/** Wird von {@link #msToDate(StringBuffer,long)} verwendet. */
	private static final GregorianCalendar gc = new GregorianCalendar();

	/** Format für Datumsausgabe festlegen. */
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

	/** Debug Ausgaben. */
	private static final Debug debug = Debug.getLogger();

	/**
	 * Berechnet das Datum aus Zeitangabe in Millisekunden. Das Datum wird folgendermaßen formatiert: DD.MM.JJJJ HH:MM:SS,sss
	 *
	 * @param sb           Stringbuffer, in den das Datum geschrieben wird.
	 * @param timeInMillis Zeitangabe in Millisekunden.
	 */
	public static void msToDate(StringBuffer sb, long timeInMillis) {
		synchronized(gc) {
			gc.clear();
			gc.setTimeInMillis(timeInMillis);
			sdf.format(gc.getTime(), sb, new FieldPosition(0));
		}
	}

	/**
	 * Berechnet das Datum aus Zeitangabe in Millisekunden. Das Datum wird folgendermaßen formatiert: DD.MM.JJJJ HH:MM:SS,sss
	 *
	 * @param timeInMillis Zeitangabe in Millisekunden.
	 *
	 * @return String mit formatiertem Datum.
	 */
	public static String msToDate(long timeInMillis) {
		StringBuffer sb = new StringBuffer();
		msToDate(sb, timeInMillis);
		return sb.toString();
	}

	/**
	 * Liefert formatiertes Datum. Ruft {@link #msToDate(long)} mit <code>System.currentMillis()</code> auf.
	 *
	 * @return Aktuelles Datum.
	 */
	public static String now() {
		return msToDate(System.currentTimeMillis());
	}

	/**
	 * Wandelt einen Sting mit Datumsangabe in Millisekunden um.
	 *
	 * @param date String mit Datumsangabe. Format: <code>dd.MM.yyyy HH:mm:ss,SSS</code>
	 *
	 * @return Datum in Millisekunden oder -1, falls es zu einem Fehler beim Parsen des Strings kam.
	 */
	public static long dateToMs(String date) {
		try {
			gc.clear();
			gc.setTime(sdf.parse(date));
			return gc.getTimeInMillis();
		}
		catch(ParseException e) {
			return -1;
		}
	}

	/**
	 * Liefert die aktuelle Systemzeit in Millisekunden, um damit einen Timer zu starten (nur zur Abkuerzung).
	 *
	 * @return System.currentTimeMillis()
	 */
	public static long startTimer() {
		return System.currentTimeMillis();
	}

	/**
	 * Liefert die Sekunden seit t.
	 *
	 * @param t Startzeitpunkt in Millisekunden
	 *
	 * @return Sekunden mit zwei Kommastellen
	 */
	public static String stopTimer(long t) {
		return df2.format((double)(System.currentTimeMillis() - t) / 1000) + "s";
	}

	public static String relTimestrMillis(long time) {
		long h = time / 3600000;
		long m = ((time / 1000) % 3600) / 60;
		long s = (((time / 1000) % 3600) % 60);
		long ss = time % 1000;
		return leadZero(h, 2) + "h:" + leadZero(m, 2) + "m:" + leadZero(s, 2) + "," + leadZero(ss, 3) + "s";
	}

	public static String relTimestr(long time) {
		long d = time / (3600 * 24);
		long h = (time % (3600 * 24)) / 3600;
		long m = (time % 3600) / 60;
		long s = (time % 3600) % 60;
		return leadZero(d, 2) + "d:" + leadZero(h, 2) + "h:" + leadZero(m, 2) + "m:" + leadZero(s, 2) + "s";
	}

	/**
	 * @param time
	 *
	 * @return YYYYMMDDHHMMSSsss (17 Bytes)
	 */
	public static String timestrmillis(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		return leadZero(now.get(Calendar.YEAR), 4) + leadZero(now.get(Calendar.MONTH) + 1, 2) + leadZero(now.get(Calendar.DAY_OF_MONTH), 2)
		       + leadZero(now.get(Calendar.HOUR_OF_DAY), 2) + leadZero(now.get(Calendar.MINUTE), 2) + leadZero(now.get(Calendar.SECOND), 2) + leadZero(
				now.get(
						Calendar.MILLISECOND
				), 3
		);
	}

	/**
	 * @param time
	 *
	 * @return YYYYMMDDHHMMSS (14 Bytes)
	 */
	public static String timestr(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		return leadZero(now.get(Calendar.YEAR), 4) + leadZero(now.get(Calendar.MONTH) + 1, 2) + leadZero(now.get(Calendar.DAY_OF_MONTH), 2)
		       + leadZero(now.get(Calendar.HOUR_OF_DAY), 2) + leadZero(now.get(Calendar.MINUTE), 2) + leadZero(now.get(Calendar.SECOND), 2);
	}

	/** @return Aktuelle Zeit & Datum als YYYYMMDDHHMMSS (14 Bytes) */
	public static String timestrNow() {
		return timestr(System.currentTimeMillis());
	}

	/**
	 * @param time
	 *
	 * @return Formatierte Datum- und Zeitangabe
	 */
	public static String timestrFormatted(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		return leadZero(now.get(Calendar.YEAR), 4) + "/" + leadZero(now.get(Calendar.MONTH) + 1, 2) + "/" + leadZero(now.get(Calendar.DAY_OF_MONTH), 2) + "-"
		       + leadZero(now.get(Calendar.HOUR_OF_DAY), 2) + ":" + leadZero(now.get(Calendar.MINUTE), 2) + ":" + leadZero(now.get(Calendar.SECOND), 2);
	}

	/**
	 * @param time
	 *
	 * @return Formatierte Datum- und Zeitangabe inkl. Millisekunden
	 */
	public static String timestrMillisFormatted(long time) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(time);
		return leadZero(now.get(Calendar.YEAR), 4) + "/" + leadZero(now.get(Calendar.MONTH) + 1, 2) + "/" + leadZero(now.get(Calendar.DAY_OF_MONTH), 2) + "-"
		       + leadZero(now.get(Calendar.HOUR_OF_DAY), 2) + ":" + leadZero(now.get(Calendar.MINUTE), 2) + ":" + leadZero(now.get(Calendar.SECOND), 2) + ","
		       + leadZero(now.get(Calendar.MILLISECOND), 3);
	}

	/** @return Formatierte Datum- und Zeitangabe */
	public static String timestrNowFormatted() {
		return timestrFormatted(System.currentTimeMillis());
	}

	/**
	 * Parst einen Timestr im Format YYYYMMDDHHMMSS in ein Calendar-Objekt.
	 *
	 * @param timestr
	 *
	 * @return Calendar-Objekt
	 */
	public static Calendar parseTimestr(String timestr) {
		int year = Integer.parseInt(timestr.substring(0, 4));
		int month = Integer.parseInt(timestr.substring(4, 6));
		int day = Integer.parseInt(timestr.substring(6, 8));
		int hour = Integer.parseInt(timestr.substring(8, 10));
		int min = Integer.parseInt(timestr.substring(10, 12));
		int sec = Integer.parseInt(timestr.substring(12));
		Calendar result = new GregorianCalendar(year, month - 1, day, hour, min, sec);
		return result;
	}

	/**
	 * Fuegt bei positiven Zahlen Punkte an den Tausender-Trennstellen ein
	 *
	 * @param n Zahl
	 *
	 * @return String mit Tausender-Punkten
	 */
	public static String kiloBlocks(long n) {
		if(n >= 0) {
			String s = n + "", erg = "";
			int len = s.length();
			for(int i = len - 1; i >= 0; i--) {
				erg = ((i != 0 && (len - i) % 3 == 0) ? "." : "") + s.charAt(i) + erg;
			}
			return erg;
		}
		else {
			return n + "";
		}
	}

	/**
	 * Fuegt ein Objekt in eine einelementige Liste ein.
	 *
	 * @param o
	 *
	 * @return Liste mit <code>o</code> als einzigem Element.
	 */
	public static ArrayList mkList(Object o) {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(o);
		return list;
	}

	/**
	 * @param list Liste von Strings
	 * @param s    String
	 *
	 * @return Wahr, wenn <code>s</code> in <code>list</code> enthalten ist, falsch sonst
	 */
	public static boolean containsStr(String list[], String s) {
		if(list == null) return false;
		for(String arch : list) {
			if(s.equals(arch)) return true;
		}
		return false;
	}

	/**
	 * @param list Liste von Objekten
	 * @param pid  String
	 *
	 * @return Wahr, wenn <code>s</code> in den Pids von <code>list</code> enthalten ist, falsch sonst
	 */
	public static boolean containsPid(List<SystemObject> list, String pid) {
		if(list == null) return false;
		for(SystemObject so : list) {
			if(pid.equals(so.getPid())) return true;
		}
		return false;
	}

	/**
	 * Liefert str gefolgt von anz-str.length() Leerzeichen, falls anz>str.length()
	 *
	 * @param str String, an den Leerzeichen angefuegt werden muessen.
	 * @param anz Anzahl Zeichen im Ergebnisstring.
	 *
	 * @return Ergebnis-String.
	 */
	public static String sr(String str, int anz) {
		int le = str.length();
		for(int i = 0; i < anz - le; i++) {
			str += " ";
		}
		return str;
	}

	/**
	 * Wandelt die uebergebene Zahl in einen String um und fuegt vorne Nullen an bis die angegebene Anzahl an Zeichen erreicht ist.
	 *
	 * @param num Zahl
	 * @param anz Anzahl Zeichen
	 *
	 * @return Zahl mit fuehrenden Nullen als String der Laenge anz.
	 */
	public static String leadZero(long num, int anz) {
		return leadZero(String.valueOf(num), anz);
	}

	public static void appendLeadZero(final StringBuilder stringBuilder, long num, int anz) {
		appendLeadZero(stringBuilder, String.valueOf(num), anz);
	}

	/**
	 * Wandelt die uebergebene Zahl in einen String um und fuegt vorne Leerzeichen an bis die angegebene Anzahl an Zeichen erreicht ist.
	 *
	 * @param num Zahl
	 * @param anz Anzahl Zeichen
	 *
	 * @return Zahl mit fuehrenden Nullen als String der Laenge anz.
	 */
	public static String leadBlank(long num, int anz) {
		return leadBlank(num + "", anz);
	}

	/**
	 * Fuegt vorne Leerzeichen an bis die angegebene Anzahl an Zeichen erreicht ist.
	 *
	 * @param num String
	 * @param anz    Anzahl Zeichen
	 *
	 * @return Zahl mit fuehrenden Nullen als String der Laenge anz.
	 */
	public static String leadBlank(String num, int anz) {
		String erg = num;
		int le = erg.length();
		for(int i = 0; i < anz - le; i++) {
			erg = " " + erg;
		}
		return erg;
	}

	/**
	 * Nimmt die als String uebergebene Zahl und fuegt vorne Nullen an bis die angegebene Anzahl an Zeichen erreicht ist.
	 *
	 * @param num Zahl
	 * @param anz Anzahl Zeichen
	 *
	 * @return Zahl mit fuehrenden Nullen als String der Laenge anz.
	 */
	public static String leadZero(String num, int anz) {
		String erg = num;
		int le = erg.length();
		for(int i = 0; i < anz - le; i++) {
			erg = "0" + erg;
		}
		return erg;
	}































	public static void appendLeadZero(final StringBuilder stringBuilder, String num, int anz) {
		final int numLength = num.length();
		final int fillLength = anz - numLength;
		for(int i = 0 ; i < fillLength; ++i) stringBuilder.append('0');
		stringBuilder.append(num);
	}

	/**
	 * Entfernt das letzte Zeichen im uebergebenen String und liefert diesen zurueck.
	 *
	 * @param s String
	 *
	 * @return Ergebnis-String
	 */
	public static String removeLastChar(String s) {
		return s.length() > 0 ? s.substring(0, s.length() - 1) : s;
	}

	/**
	 * Liefert die Elemente einer numerischen ID als Liste von 3 Zeichen langen Strings. Beispiel: ID "12345678" ergibt Liste ("123", "456", "78").
	 *
	 * @param id Numerische ID.
	 *
	 * @return String[]
	 */
	public static String[] getIdElements(long id) {
		char[] ids = Long.toString(id).toCharArray();
		String[] erg = new String[ids.length % 3 == 0 ? (ids.length / 3) : (ids.length / 3) + 1];
		for(int i = erg.length - 1; i >= 0; i--) {
			erg[i] = "";
		}
		int cnt = 0;
		for(char ch : ids) {
			erg[cnt] += ch;
			if(erg[cnt].length() == 3) cnt++;
		}
		return erg;
	}

	/**
	 * Zerlegt die numerische ID in Gruppen von jeweils 3 Zeichen in Dezimaldarstellung und ergänzt diese so zum übergebenen StringBuilder, dass vor jeder
	 * Gruppe die übergebene Separtor/Prefixsequenz hinzugefügt wird. Beispiel: mit "/obj" im Parameter separatorAndPrefix führt die ID 12345678 dazu, dass die
	 * Zeichenkette "/obj123/obj456/obj78" zum Stringbuilder ergänzt wird.
	 *
	 * @param pathName StringBuilder-Objekt, an das die ermittelte Zeichenkette angehangen werden soll.
	 * @param separatorAndPrefix Sequenz von Separator und Prefixzeichen, die vor jeder Zeichengruppe eingefügt werden soll.
	 * @param id Numerische ID.
	 */
	public static void appendIdElements(StringBuilder pathName, String separatorAndPrefix, long id) {
		final String idString = Long.toString(id);
		final int end = idString.length();
		int start = 0;
		while(start < end) {
			int newStart = start + 3;
			if(newStart > end) newStart = end;
			pathName.append(separatorAndPrefix);
			pathName.append(idString, start, newStart);
			start = newStart;
		}
	}

	/**
	 * Wandelt den angegebenen boolschen Wert in eine textuelle Repraesentation um.
	 *
	 * @param x Boolscher Wert
	 *
	 * @return Entweder '0' oder '1'
	 */
	public static String b01(boolean x) {
		return x ? "1" : "0";
	}

	/**
	 * Wandelt den angegebenen boolschen Wert in eine textuelle Repraesentation um.
	 *
	 * @param x Boolscher Wert
	 *
	 * @return Entweder 'F' oder 'T'
	 */
	public static String bTF(boolean x) {
		return x ? "T" : "F";
	}

	/**
	 * Gibt das angegebene {@link ResultData} als String zurueck.
	 *
	 * @param rd ResultData
	 *
	 * @return String-Darstellung
	 */
	public static String printRD(ResultData rd) {
		return "data_index:" + rd.getDataIndex() + ", data_time:" + rd.getDataTime() + ", delayed:" + b01(rd.isDelayedData()) + ", (nutzdaten:"
		       + b01(rd.hasData()) + ", keine_quelle:" + b01(rd.isNoSourceAvailable()) + "/quelle:" + b01(rd.isSourceAvailable()) + ", keine_daten:"
		       + b01(rd.isNoDataAvailable()) + ", keine_rechte:" + b01(rd.isNoRightsAvailable()) + "), no_valid_subsrc:" + b01(rd.isNoValidSubscription());
	}

	/**
	 * Liefert den Index der angegebenen Datensatzart (0=OA, 1=ON, 2=NA, 3=NN).
	 *
	 * @param adk Datensatzart
	 *
	 * @return Index, -1 im Fehlerfall.
	 */
	public static int getDataKindIndex(ArchiveDataKind adk) {
		if(ArchiveDataKind.ONLINE.equals(adk)) {
			return OA;
		}
		else if(ArchiveDataKind.ONLINE_DELAYED.equals(adk)) {
			return ON;
		}
		else if(ArchiveDataKind.REQUESTED.equals(adk)) {
			return NA;
		}
		else if(ArchiveDataKind.REQUESTED_DELAYED.equals(adk)) {
			return NN;
		}
		else {
			return -1;
		}
	}

	/**
	 * Liefert die Datensatzart zum angegebenen Index.
	 *
	 * @param index 0:ONLINE, 1:ONLINE_DELAYED, 2:REQUESTED, 3:REQUESTED_DELAYED
	 *
	 * @return Datensatzart, null im Fehlerfall.
	 */
	public static ArchiveDataKind getDataKindFromIndex(int index) {
		if(index == OA) {
			return ArchiveDataKind.ONLINE;
		}
		else if(index == ON) {
			return ArchiveDataKind.ONLINE_DELAYED;
		}
		else if(index == NA) {
			return ArchiveDataKind.REQUESTED;
		}
		else if(index == NN) {
			return ArchiveDataKind.REQUESTED_DELAYED;
		}
		else {
			return null;
		}
	}

	/**
	 * Liefert den {@link TimingType} mit dem angegebenen Integer-Typ, <code>null</code> falls nicht gefunden.
	 *
	 * @param type Interger-Darstellung
	 *
	 * @return TimingType, <code>null</code> falls nicht gefunden
	 */
	public static TimingType getTimingType(int type) {
		switch(type) {
			case 1:
				return TimingType.DATA_TIME;
			case 2:
				return TimingType.ARCHIVE_TIME;
			case 3:
				return TimingType.DATA_INDEX;
			default:
				return null;
		}
	}

	/**
	 * Liefert ein Feld mit den in der {@link ArchiveDataKindCombination} gesetzten Datensatzarten.
	 *
	 * @param adkComb
	 *
	 * @return Array aller gesetzten ArchiveDataKinds
	 */
	public static ArchiveDataKind[] getDataKinds(ArchiveDataKindCombination adkComb) {
		List<ArchiveDataKind> dataKinds = new ArrayList<ArchiveDataKind>();
		if(adkComb.isOnline()) dataKinds.add(ArchiveDataKind.ONLINE);
		if(adkComb.isOnlineDelayed()) dataKinds.add(ArchiveDataKind.ONLINE_DELAYED);
		if(adkComb.isRequested()) dataKinds.add(ArchiveDataKind.REQUESTED);
		if(adkComb.isRequestedDelayed()) dataKinds.add(ArchiveDataKind.REQUESTED_DELAYED);

		return (ArchiveDataKind[])dataKinds.toArray(new ArchiveDataKind[dataKinds.size()]);
	}

	/**
	 * Liefert ein Feld mit allen Datensatzarten.
	 *
	 * @return Array aller ArchiveDataKinds
	 */
	public static ArchiveDataKind[] getAllDataKinds() {
		return ALL_ADK;
	}

	/**
	 * Erzeugt eine {@link ArchiveDataKindCombination} aus den angegebenen Parametern, sofern mindestens einer der vier Datensatzarten <code>true</code> ist.
	 *
	 * @param oa Datensatzart Online aktuell
	 * @param on Datensatzart Online nachgeliefert
	 * @param na Datensatzart Nachgefordert aktuell
	 * @param nn Datensatzart Nachgefordert nachgeliefert
	 *
	 * @return ArchiveDataKindCombination, <code>null</code> falls alle 4 Parameter <code>false</code> sind.
	 */
	public static ArchiveDataKindCombination getADKCombination(boolean oa, boolean on, boolean na, boolean nn) {
		List<ArchiveDataKind> dataKinds = new ArrayList<ArchiveDataKind>();
		if(oa) dataKinds.add(ArchiveDataKind.ONLINE);
		if(on) dataKinds.add(ArchiveDataKind.ONLINE_DELAYED);
		if(na) dataKinds.add(ArchiveDataKind.REQUESTED);
		if(nn) dataKinds.add(ArchiveDataKind.REQUESTED_DELAYED);

		switch(dataKinds.size()) {
			case 1:
				return new ArchiveDataKindCombination(dataKinds.get(0));
			case 2:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1));
			case 3:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1), dataKinds.get(2));
			case 4:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1), dataKinds.get(2), dataKinds.get(3));
			default:
				return null; // Falls keine Datensatzart gewaehlt worden ist
		}
	}

	public static ArchiveDataKindCombination getADK_OA() {
		return new ArchiveDataKindCombination(ArchiveDataKind.ONLINE);
	}

	public static ArchiveDataKindCombination getADK_ON() {
		return new ArchiveDataKindCombination(ArchiveDataKind.ONLINE_DELAYED);
	}

	public static ArchiveDataKindCombination getADK_NA() {
		return new ArchiveDataKindCombination(ArchiveDataKind.REQUESTED);
	}

	public static ArchiveDataKindCombination getADK_NN() {
		return new ArchiveDataKindCombination(ArchiveDataKind.REQUESTED_DELAYED);
	}

	public static ArchiveDataKindCombination getADK_OA_NA() {
		return new ArchiveDataKindCombination(ArchiveDataKind.ONLINE, ArchiveDataKind.REQUESTED);
	}

	public static ArchiveDataKindCombination getADK_OA_ON() {
		return new ArchiveDataKindCombination(ArchiveDataKind.ONLINE, ArchiveDataKind.ONLINE_DELAYED);
	}

	public static ArchiveDataKindCombination getADK_ON_NN() {
		return new ArchiveDataKindCombination(ArchiveDataKind.ONLINE_DELAYED, ArchiveDataKind.REQUESTED_DELAYED);
	}

	public static ArchiveDataKindCombination getADK_NA_NN() {
		return new ArchiveDataKindCombination(ArchiveDataKind.REQUESTED, ArchiveDataKind.REQUESTED_DELAYED);
	}

	public static ArchiveDataKindCombination getAllADKC() {
		return new ArchiveDataKindCombination(
				ArchiveDataKind.ONLINE, ArchiveDataKind.ONLINE_DELAYED, ArchiveDataKind.REQUESTED, ArchiveDataKind.REQUESTED_DELAYED
		);
	}

	/**
	 * Liefert die Datenidentifikation als String zurück.
	 *
	 * @param objID Objekt-ID
	 * @param atgID Attributgruppen-ID
	 * @param aspID Aspekt-ID
	 * @param sv    Simulationsvariante
	 *
	 * @return objID=<objID> atgID=<atgID> aspID=<aspID> sv=<sv>
	 */
	public static String did2Str(long objID, long atgID, long aspID, int sv) {
		return "objID=" + objID + " atgID=" + atgID + " aspID=" + aspID + " sv=" + sv;
	}

	/**
	 * Droeselt den Datenindex in Anmeldezeit Quelle, laufende Nummer und Kennzeichenbits auf
	 *
	 * @param dIdx
	 *
	 * @return
	 */
	public static String dIdx2Str(long dIdx) {
		return (dIdx >> 32) + "#" + ((dIdx >> 2) & 0x3fffffff) + "#" + (dIdx & 0x3);
	}

	/**
	 * Gibt den Datenindex ausfuehrlich aus.
	 *
	 * @param dIdx
	 *
	 * @return
	 */
	public static String dIdx2StrExt(long dIdx) {
		return Util.timestrFormatted(Util.dIdxSrcSubscrTime(dIdx)) + "#" + Util.leadZero(Util.dIdxLfdnr(dIdx), 10) + "#" + Util.dIdxArSBit(dIdx) + "#"
		       + Util.dIdxDaVBit(dIdx);
	}

	public static String rd2Str(ResultData rd) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("dx=");
		sb.append(dIdx2Str(rd.getDataIndex()));
		sb.append(", dt=");
		sb.append(timestrMillisFormatted(rd.getDataTime()));
		sb.append(", [");
		sb.append(rd.getObject());
		sb.append('/');
		DataDescription dataDescription = rd.getDataDescription();
		if(dataDescription == null) {
			sb.append("-/-/-");
		}
		else {
			sb.append(dataDescription.getAttributeGroup());
			sb.append('/');
			sb.append(dataDescription.getAspect());
			sb.append('/');
			sb.append(dataDescription.getSimulationVariant());
		}
		sb.append("]\ndelayed=");
		sb.append(bTF(rd.isDelayedData()));
		sb.append(", Art=");
		sb.append(rd.getDataKind());
		sb.append(", keineQuelle=");
		sb.append(bTF(rd.isNoSourceAvailable()));
		sb.append(", quelleVerfuegb.=");
		sb.append(bTF(rd.isSourceAvailable()));
		sb.append(", keineDaten=");
		sb.append(bTF(rd.isNoDataAvailable()));
		sb.append(", keineRechte=");
		sb.append(bTF(rd.isNoRightsAvailable()));
		sb.append(", unguelt.Anm.=");
		sb.append(bTF(rd.isNoValidSubscription()));
		return sb.toString();
	}

	public static String ad2Str(ArchiveData ad) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(
				ad.getDataKind().equals(ArchiveDataKind.ONLINE)
				? "OA"
				: (ad.getDataKind().equals(ArchiveDataKind.ONLINE_DELAYED) ? "ON" : (ad.getDataKind().equals(ArchiveDataKind.REQUESTED) ? "NA" : "NN"))
		);
		sb.append("  dx=");
		sb.append(dIdx2StrExt(ad.getDataIndex()));
		sb.append("  dt=");
		sb.append(timestrMillisFormatted(ad.getDataTime()));
		sb.append("  [");
		sb.append(ad.getObject());
		sb.append('/');
		DataDescription dataDescription = ad.getDataDescription();
		if(dataDescription == null) {
			sb.append("-/-/-");
		}
		else {
			sb.append(dataDescription.getAttributeGroup());
			sb.append('/');
			sb.append(dataDescription.getAspect());
			sb.append('/');
			sb.append(dataDescription.getSimulationVariant());
		}
		sb.append("]");
		return sb.toString();
	}

	public static String ad2StrNoData(ArchiveData rd) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(
				rd.getDataKind().equals(ArchiveDataKind.ONLINE)
				? "OA"
				: (rd.getDataKind().equals(ArchiveDataKind.ONLINE_DELAYED) ? "ON" : (rd.getDataKind().equals(ArchiveDataKind.REQUESTED) ? "NA" : "NN"))
		);
		sb.append("  dx=");
		sb.append(dIdx2StrExt(rd.getDataIndex()));
		sb.append("  dt=");
		sb.append(timestrMillisFormatted(rd.getDataTime()));
		sb.append("  at=");
		sb.append(timestrMillisFormatted(rd.getArchiveTime()));
		sb.append("  [" + rd.getObject().getPid() + "]");
		sb.append("  " + rd.getDataType());
		return sb.toString();
	}

	public static String ads2Str(ArchiveDataSpecification ads) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("Archivanfrage:");
		sb.append("\n  Obj:       " + ads.getObject().getNameOrPidOrId());
		sb.append("\n  Atg:       " + ads.getDataDescription().getAttributeGroup().getNameOrPidOrId());
		sb.append("\n  Asp:       " + ads.getDataDescription().getAspect().getNameOrPidOrId());
		sb.append("\n  SV:        " + ads.getDataDescription().getSimulationVariant());
		String xx = (ads.getDataKinds().isOnline() ? "OA/" : "") + (ads.getDataKinds().isOnlineDelayed() ? "ON/" : "")
		            + (ads.getDataKinds().isRequested() ? "NA/" : "") + (ads.getDataKinds().isRequestedDelayed() ? "NN/" : "");
		sb.append("\n  DS-Arten:  " + Util.removeLastChar(xx));
		sb.append("\n  Bereich:   " + ads.getTimeSpec().getTimingType());
		sb.append("\n  relativ:   " + ads.getTimeSpec().isStartRelative());
		long start = ads.getTimeSpec().getIntervalStart();
		long end = ads.getTimeSpec().getIntervalEnd();
		if(!ads.getTimeSpec().isStartRelative()) {
			sb.append(
					"\n  von:       " + (ads.getTimeSpec().getTimingType().equals(TimingType.DATA_INDEX)
					                     ? (Util.dIdx2StrExt(start) + " (" + start + ")")
					                     : Util.timestrMillisFormatted(start))
			);
		}
		else {
			sb.append("\n  Anzahl:    " + start);
		}
		sb.append(
				"\n  bis:       " + (ads.getTimeSpec().getTimingType().equals(TimingType.DATA_INDEX)
				                     ? (Util.dIdx2StrExt(end) + " (" + end + ")")
				                     : Util.timestrMillisFormatted(end))
		);
		sb.append("\n  Anfrage:   " + ads.getRequestOption());
		sb.append("\n  ON/NN:     " + ads.getSortOrder());
		return sb.toString();
	}

	/**
	 * Gibt Informationen über das Ergebnis der Archivinformationsanfrage zurück.
	 *
	 * @param air Ergebnis der Archivinformationsanfrage.
	 *
	 * @return String mit Informationen über die Archivinformationsanfrage.
	 */
	public static String air2Str(ArchiveInformationResult air) {
		if(air == null) return "";

		StringBuilder sb = new StringBuilder();

		if(air.getTimingType().equals(TimingType.DATA_INDEX)) {
			sb.append("Anfragezeitraum (Index): " + Util.dIdx2StrExt(air.getIntervalStart()) + " - " + Util.dIdx2StrExt(air.getIntervalEnd()));
		}
		else if(air.getTimingType().equals(TimingType.ARCHIVE_TIME)) {
			sb.append(
					"Anfragezeitraum (Archivzeit): " + Util.msToDate(air.getIntervalStart()) + " - " + Util.msToDate(air.getIntervalEnd())
			);
		}
		else {
			sb.append(
					"Anfragezeitraum (Datenzeit): " + Util.msToDate(air.getIntervalStart()) + " - " + Util.msToDate(air.getIntervalEnd())
			);
		}
		sb.append("\n");

		sb.append(
				"Der Anfragezeitraum liegt im direktem Zugriff des Archivsystems: " + ((air.directAccess()) ? "JA" : "NEIN")
		);
		sb.append("\n");

		sb.append("Der Anfragezeitraum enthält eine Datenlücke: " + ((air.isDataGap()) ? "JA" : "NEIN"));
		sb.append("\n");

		sb.append("Medium ID der Datensätze (falls bekannt): " + air.getVolumeIdTypB());

		return sb.toString();
	}

	/**
	 * Gibt die im ArchiveInformationResult gespeicherte Datenidentifikation zurück.
	 *
	 * @param aiqr Ergebnis der Archivinformationsanfrage.
	 *
	 * @return Datenidentifikation als String.
	 */
	public static String airHdr2Str(ArchiveInformationResult aiqr) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(aiqr.getArchiveDataSpecification().getObject());
		sb.append('/');
		sb.append(aiqr.getArchiveDataSpecification().getDataDescription().getAttributeGroup());
		sb.append('/');
		sb.append(aiqr.getArchiveDataSpecification().getDataDescription().getAspect());
		sb.append('/');
		sb.append(aiqr.getArchiveDataSpecification().getDataDescription().getSimulationVariant());
		return sb.toString();
	}

	/**
	 * Liefert die Anmeldezeit der Quelle dieses Datenindex
	 *
	 * @param didx Datenindex
	 *
	 * @return Anmeldezeit in Millis seit Epoch
	 */
	public static long dIdxSrcSubscrTime(long didx) {
		return (didx >> 32) * 1000;
	}

	/**
	 * Liefert die Anmeldezeit der Quelle dieses Datensatzes
	 *
	 * @param rd Datensatz
	 *
	 * @return Anmeldezeit in Millis seit Epoch
	 */
	public static long dIdxSrcSubscrTime(ResultData rd) {
		return dIdxSrcSubscrTime(rd.getDataIndex());
	}

	/**
	 * Liefert den Datenindex ohne die Anmeldezeit der Quelle.
	 *
	 * @param didx Datenindex
	 *
	 * @return Die unteren 4 Byte des Datenindex
	 */
	public static long didxNoSubscrTime(long didx) {
		return 0xFFFFFFFFL & didx;
	}

	/**
	 * Liefert die laufende Nummer, die im Datenindex des Datensatzes enthalten ist
	 *
	 * @param didx Datenindex
	 *
	 * @return Laufende Nummer
	 */
	public static int dIdxLfdnr(long didx) {
		return (int)((didx >> 2) & 0x3fffffff); // 30 Bit
	}

	/**
	 * Liefert die laufende Nummer, die im Datenindex des Datensatzes enthalten ist
	 *
	 * @param rd Datensatz
	 *
	 * @return Laufende Nummer
	 */
	public static int dIdxLfdnr(ResultData rd) {
		return dIdxLfdnr(rd.getDataIndex());
	}

	/**
	 * Liefert die Modifier-Bits, die im Datenindex des Datensatzes enthalten sind
	 *
	 * @param didx Datenindex
	 *
	 * @return Modifier-Bits (Archivbit und Datenverteilerbit)
	 */
	public static int dIdxModBits(long didx) {
		return (int)(didx & 0x3);
	}

	/**
	 * Liefert den Datenindex unter Nichtbeachtung der beiden untersten Bits (Archivbit und Datenverteilerbit)
	 *
	 * @param didx Datenindex
	 *
	 * @return Datenindex ohne Modifier-Bits
	 */
	public static long dIdxNoModBits(long didx) {
		return didx >> 2;
	}

	/**
	 * Fuegt ArS- und DaV-Bit an, jeweils mit Wert 0.
	 *
	 * @param didxNoModBits
	 *
	 * @return Datenindex mit Modifier-Bits (Wert 0)
	 */
	public static long dIdxAppendZeroModBits(long didxNoModBits) {
		return didxNoModBits * 4;
	}

	/**
	 * Liefert das Archiv-Bit des Datenindex
	 *
	 * @param didx Datenindex
	 *
	 * @return Archiv-Bit (0 oder 1)
	 */
	public static int dIdxArSBit(long didx) {
		return (int)((didx >> 1) & 0x1);
	}

	/**
	 * Liefert das Datenverteiler-Bit des Datenindex
	 *
	 * @param didx Datenindex
	 *
	 * @return Archiv-Bit (0 oder 1)
	 */
	public static int dIdxDaVBit(long didx) {
		return (int)(didx & 0x1);
	}

	/**
	 * Setzt das Archivbit im Datenindex
	 *
	 * @param didx Datenindex
	 *
	 * @return Datenindex mit gesetztem Archivbit
	 */
	public static long dIdxSetArSBit(long didx) {
		return didx | 0x2;
	}

	/**
	 * Überprüft ob die laufende Nummer des Datenindex springt. Wenn die beiden Datenindizes gleich sind, wird <code>false</code> zurückgegeben.
	 *
	 * @param index1 Erster Datenindex
	 * @param index2 Darauf folgender Datenindex
	 *
	 * @return <code>true</code> falls der Datenindex springt, <code>false</code> sonst.
	 */
	public static boolean didxIndexSkipped(long index1, long index2) {
		if(Util.dIdxLfdnr(index1) == Util.dIdxLfdnr(index2) || Util.dIdxLfdnr(index1) == Util.dIdxLfdnr(index2) - 1) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Liefert die Datenidentifikation inkl. Datensatzart als String zurück.
	 *
	 * @param objID Objekt-ID
	 * @param atgID Attributgruppen-ID
	 * @param aspID Aspekt-ID
	 * @param sv    Simulationsvariante
	 * @param adk   Datensatzart
	 *
	 * @return objID=<objID> atgID=<atgID> aspID=<aspID> sv=<sv> <adk>
	 */
	public static String did2Str(long objID, long atgID, long aspID, int sv, ArchiveDataKind adk) {
		return "objID=" + objID + " atgID=" + atgID + " aspID=" + aspID + " sv=" + sv + " " + adk;
	}

	public static String dsKeys2Str(long didx, long atime, long dtime) {
		return "dx=" + didx + " dt=" + dtime + " at=" + atime;
	}

	/**
	 * Dekodiert die Serialisiererversion wie im Datenkatalog spezifiziert. Da die Implementierungen der zum Schreiben der Container verwendeten {@link
	 * ByteIO}-Klasse sich aendern koennen, ist der Algorithmus extra aufgefuehrt.
	 *
	 * @param data
	 *
	 * @return Serialisiererversion
	 */
	public static int getSerVersion(byte[] data) {
		return ((data[0] & 0xFF) << 24) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
	}

	/**
	 * Liefert eine Array aller Unterverzeichnisse (ohne Dateien) des angegebenen Verzeichnisses.
	 *
	 * @param dir Verzeichnis
	 *
	 * @return Array von Unterverzeichnissen
	 */
	public static File[] listDirectories(File dir) {
		File[] erg;
		erg = dir.listFiles(
				new FileFilter() {
					public boolean accept(File f) {
						return f.isDirectory();
					}
				}
		);
		return erg == null ? new File[0] : erg;
	}

	/**
	 * Liefert eine Array aller Unterverzeichnisse (ohne Dateien) des angegebenen Verzeichnisses.
	 *
	 * @param dir Verzeichnis
	 *
	 * @return Array von Unterverzeichnissen
	 */
	public static File[] listDirectories(String dir) {
		return listDirectories(new File(dir));
	}

	/**
	 * Liefert eine Array aller Dateien (ohne Unterverzeichnisse) des angegebenen Verzeichnisses.
	 *
	 * @param dir Verzeichnis
	 *
	 * @return Array von Dateien
	 */
	public static File[] listFiles(File dir) {
		return dir.listFiles(
				new FileFilter() {
					public boolean accept(File f) {
						return f.isFile();
					}
				}
		);
	}

	/**
	 * Liefert den alphabetisch kleinsten String, der in der Liste enthalten ist.
	 *
	 * @param list Liste von Strings
	 *
	 * @return Alphabetisch kleinstes Element
	 */
	public static String getFirstInAlphabet(String[] list) {
		int smallestIdx = 0;
		if(list.length == 0) {
			return null;
		}
		else {
			for(int i = 1; i < list.length; i++) {
				if(list[smallestIdx].compareTo(list[i]) > 0) smallestIdx = i;
			}
		}
		return list[smallestIdx];
	}

	/**
	 * Liefert den alphabetisch groessten String, der in der Liste enthalten ist.
	 *
	 * @param list Liste von Strings
	 *
	 * @return Alphabetisch groesstes Element
	 */
	public static String getLastInAlphabet(String[] list) {
		int largestIdx = 0;
		if(list.length == 0) {
			return null;
		}
		else {
			for(int i = 1; i < list.length; i++) {
				if(list[largestIdx].compareTo(list[i]) < 0) largestIdx = i;
			}
		}
		return list[largestIdx];
	}

	/**
	 * Loescht im angegebenen Verzeichnis die Datei mit dem angegebenen Dateinamen, sofern sie bereits existiert, und legt sie danach neu an.
	 *
	 * @param dir  Verzeichnis
	 * @param name Dateiname
	 *
	 * @return Datei
	 *
	 * @throws IOException
	 */
	public static File deleteCreateNewFile(File dir, String name) throws IOException {
		File file = new File(dir, name);
		if(file.exists()) file.delete();
		file.createNewFile();
		return file;
	}

	/**
	 * Loescht die angegebene Datei falls sie existiert und legt eine neue an.
	 *
	 * @param file Zu leoschende/neu anzulegende Datei
	 *
	 * @return Datei
	 *
	 * @throws IOException
	 */
	public static File deleteCreateNewFile(File file) throws IOException {
		if(file.exists()) file.delete();
		file.createNewFile();
		return file;
	}

	/**
	 * Loescht das angegebene Verzeichnis inkl. seines Inhalts rekursiv, sofern es bereist existiert, und legt es dann neu an.
	 *
	 * @param dir Verzeichnis
	 */
	public static boolean deleteCreateNewDir(File dir) {
		boolean result = true;
		if(dir.exists()) {
			result = deleteDir(dir);
		}
		dir.mkdir();
		return result;
	}

	/**
	 * Loescht das angegebene Verzeichnis inkl. seines Inhalts rekursiv.
	 *
	 * @param dir Verzeichnis
	 *
	 * @return Erfolgs-Kennzeichen.
	 */
	public static boolean deleteDir(File dir) {
		if(dir.isDirectory()) {
			for(File f : dir.listFiles()) {
				if(!deleteDir(f)) return false;
			}
		}
		return dir.delete();
	}

	/**
	 * Loescht die angegebene Datei falls sie existiert.
	 *
	 * @param file Zu loeschende Datei
	 *
	 * @return Wahr, falls die Datei nicht existiert oder sie existiert und erfolgreich geloescht (d.h. <code>file.delete()==true</code>) werden konnte, falsch
	 *         sonst.
	 */
	public static boolean deleteIfExists(File file) {
		if(file.exists()) {
			return file.delete();
		}
		else {
			return true;
		}
	}

	/**
	 * Vergleicht zwei Dateien auf Gleichheit.
	 *
	 * @param f0 Erste Datei
	 * @param f1 Zweite Datei
	 *
	 * @return <code>true</code> bei gleichen Dateien, <code>false</code> sonst.
	 *
	 * @throws IOException
	 */
	public static boolean cmpFiles(File f0, File f1) throws IOException {
		if(f0.length() != f1.length()) return false;
		InputStream i0 = new BufferedInputStream(new FileInputStream(f0));
		InputStream i1 = new BufferedInputStream(new FileInputStream(f1));
		byte[][] buf = new byte[2][FILE_BUFFER_SIZE];
		boolean ident = true;
		while(true) {
			int len = i0.read(buf[0]);
			if(len < 0) break;
			i1.read(buf[1]);
			for(int i = 0; i < len; i++) {
				if(buf[0][i] != buf[1][i]) {
					ident = false;
					break;
				}
			}
			if(!ident) break;
		}
		i0.close();
		i1.close();
		return ident;
	}

	/**
	 * Zaehlt die Zeilen einer Textdatei.
	 *
	 * @param f Textdatei
	 *
	 * @return Zahl der Zeilen oder -1 im Fehlerfall
	 */
	public static int countLines(File f) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			int erg = 0;
			while(br.readLine() != null) erg++;
			br.close();
			return erg;
		}
		catch(IOException e) {
			return -1;
		}
	}

	/**
	 * Vergleicht zwei Bytefelder
	 *
	 * @param data1 Bytefeld 1
	 * @param data2 Bytefeld 2 (Vergleichswert)
	 *
	 * @return Wahr, falls <code>data1</code> identisch zu <code>data2</code> ist, falsch sonst
	 */
	public static boolean cmpBytes(byte[] data1, byte[] data2) {
		return cmpBytes(data1, 0, data2);
	}

	/**
	 * Vergleicht zwei Bytefelder
	 *
	 * @param data1  Bytefeld 1
	 * @param offset Start im Bytefeld 1
	 * @param data2  Bytefeld 2 (Vergleichswert)
	 *
	 * @return Wahr, falls <code>data1</code> ab Position <code>offest</code> genau <code>data2</code> enthaelt, falsch sonst
	 */
	public static boolean cmpBytes(byte[] data1, int offset, byte[] data2) {
		if(data1 == null && data2 == null) return true;
		if(data1 == null && data2 != null || data1 != null && data2 == null) return false;
		int keyLen = data2.length;
		if(data1.length - offset < keyLen) {
			return false;
		}
		else {
			for(int i = 0; i < keyLen; i++) {
				if(data1[offset + i] != data2[i]) return false;
			}
		}
		return true;
	}

	/**
	 * Kopiert eine Datei in eine andere.
	 *
	 * @param src  Quelldatei
	 * @param dest Zieldatei
	 *
	 * @throws IOException
	 */
	public static void copyFiles(File src, File dest) throws IOException {
		FileInputStream srcStr = new FileInputStream(src);
		FileOutputStream destStr = new FileOutputStream(dest);
		copyStreams(srcStr, destStr);
		srcStr.close();
		destStr.close();
	}

	private static Comparator FILE_COMP_ALPHA = new Comparator() {
		public int compare(Object a, Object b) {
			return ((File)a).getName().compareTo(((File)b).getName());
		}
	};

	/**
	 * Sortiert ein Feld von Files alphabetisch
	 *
	 * @param files
	 */
	@SuppressWarnings({"unchecked"})
	public static void sortFilesAlpha(File[] files) {
		if(files != null) Arrays.sort(files, FILE_COMP_ALPHA);
	}

	/**
	 * Kopiert einen Stream in einen anderen. Die Position der Streams kann vorher festgelegt werden.
	 *
	 * @param src  InputStream
	 * @param dest OutputStream
	 *
	 * @throws IOException
	 */
	public static void copyStreams(InputStream src, OutputStream dest) throws IOException {
		byte[] buf = new byte[FILE_BUFFER_SIZE];
		int len;
		while((len = src.read(buf)) > 0) dest.write(buf, 0, len);
	}

	/**
	 * Liefert den verfuegbaren Speicherplatz in Bytes auf einem Laufwerk zurueck. Die Implementierung ist plattformunabhängig mit Hilfe der seit Java 1.6
	 * existierenden Möglichkeiten realisiert.
	 *
	 * @param drive Als <code>drive</code> kann ein beliebiger Filename verwendet werden, das Laufwerk wird automatisch erkannt.
	 *
	 * @return Verfuegbarer Speicherplatz in Bytes.
	 *
	 * @throws Exception
	 * @see java.io.File#getUsableSpace()
	 */
	public static long calcFreeDiskSpace(String drive) throws Exception {
		return new File(drive).getUsableSpace();
	}

	/**
	 * Liefert den verfuegbaren Speicherplatz in Bytes auf einem Laufwerk zurueck. Momentan werden Windows- und Linux-Systeme unterstuetzt. Als <code>drive</code>
	 * kann ein beliebiger Filenamen verwendet werden, das Laufwerk wird automatisch erkannt.<br>
	 * Diese Methode wird von Testfällen benutzt, um Vergleichswerte für das neue Systemunabhängige Verfahren zu ermitteln.
	 *
	 * @param drive
	 *
	 * @return Verfuegbarer Speicherplatz in Bytes.
	 *
	 * @throws Exception
	 */
	static long oldCalcFreeDiskSpace(String drive) throws Exception {

		long result = Long.MAX_VALUE;
		String os = System.getProperty("os.name").toLowerCase();
		if(os.startsWith("windows")) {
			// dir - Befehl ausführen
			Runtime r = Runtime.getRuntime();
			if(drive.contains(":")) {
				drive = drive.substring(0, drive.indexOf(":") + 1);
			}
			String command = "dir " + drive;
			Process p = r.exec("cmd.exe /c" + command);
			// Ausgabe einlesen
			StringBuffer buf = new StringBuffer();
			InputStream is = p.getInputStream();
			int c;
			while((c = is.read()) != -1) buf.append((char)c);

			result = getFreeDiscSpaceWindows(buf.toString());
		}
		else {
			String command = "/bin/df -k " + drive + "";
			debug.fine("command: " + command);
			String output = "";
			try {
				// Linux, Solaris, Mac OS X und andere UNIX-basierte Systeme
				Process proc = Runtime.getRuntime().exec(command);
				InputStream iStream = proc.getInputStream();
				BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
				String bufStr;
				StringBuffer buf = new StringBuffer();
				while((bufStr = bReader.readLine()) != null) buf.append(bufStr);
				output = buf.toString();

				// erste Zeile abschneiden (zweite Zeile besitzt immer als erstes Zeichen "/")
				// debug.info("output1: " + output);
				output = output.substring(output.indexOf("/"));
				// Beispiel-Output: /dev/cciss/c0d0p2 139164516 6651784 132512732 5% /
				// wir interessieren uns fuer die 4. Spalte
				// debug.info("output2: " + output);
				Matcher matcher = LINUX_DF_PATTERN.matcher(output);
				if(matcher.matches()) {
					result = Long.parseLong(matcher.group(1)) * 1024L;
				}
				else {
					debug.warning(
							"Freier Speicherplatz konnte nicht ermittelt werden. Befehl war: '" + command + "', Ergebnis war: '" + output + "'"
					);
				}
			}
			catch(Exception e) {
				debug.warning("Freier Speicherplatz konnte nicht ermittelt werden. Befehl war: '" + command + "', Ergebnis war: '" + output + "'", e);
			}
		}
		debug.fine("Freier Plattenplatz in Bytes", result);
		return result;
	}

	/**
	 * Liefert den verfügbaren Speicherplatz zurück.
	 *
	 * @param input String mit dem verfügbaren Speicherplatz. Der verfügbare Speicherplatz muss in folgendem Format vorliegen:<br> <code> [Space] [Zahl] [Punkt
	 *              Zahl]* [Space]</code><br> Falls mehrere solcher Ziffern und Punkt Ketten im String vorkommen, wird das letzte Vorkommen verwendet.
	 *
	 * @return Verfuegbarer Speicherplatz in Bytes.
	 *
	 * @throws Exception Fehler bei der Bestimmung des Speicherplatzes.
	 */
	public static long getFreeDiscSpaceWindows(String input) throws Exception {
		String result = null;
		try {
			// Regulären Ausdruck erzeugen
			Pattern pattern = Pattern.compile("\\s\\d+((\\.|,)?\\d+)*\\s");
			Matcher matcher = pattern.matcher(input);

			while(matcher.find()) result = matcher.group();

			// Dezimaltrenner entfernen:
			result = result.replace(".", "").trim(); // überflüssige Spaces abschneiden
			result = result.replace(",", "");
			return Long.parseLong(result);
		}
		catch(Exception e) {
			debug.warning(
					"Freier Speicherplatz konnte nicht ermittelt werden! Ermittelter Speicherplatz: " + result + ". Eingabe war " + input
			);
			throw e;
		}
	}

	/**
	 * Gibt den StackTrace der Exception als String zurueck.
	 *
	 * @param exception Exception
	 *
	 * @return StackTrace der Exception als String
	 */
	public static String getStackTrace(Throwable exception) {
		String result = null;
		if(exception != null) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			exception.printStackTrace(printWriter);
			printWriter.flush();
			result = stringWriter.toString();
		}
		return result;
	}

	public static String printHex(byte[] field, int start, int len) {
		StringBuffer sb = new StringBuffer((field.length * 3) - 1);
		int stop = field.length < len ? field.length : (start + len);
		for(int i = start; i < stop; i++) {
			int b = (field[i] >> 4) & 0x0000000F;
			if(b < 10) {
				sb.append(b);
			}
			else {
				sb.append((char)('A' + b - 10));
			}
			b = field[i] & 0x0000000F;
			if(b < 10) {
				sb.append(b);
			}
			else {
				sb.append((char)('A' + b - 10));
			}
			if(i + 1 < stop) sb.append(" ");
		}
		return sb.toString();
	}

	public static String printHex(byte[] field) {
		return printHex(field, 0, field.length);
	}

	/**
	 * Schließt ein Objekt. Kommt es zu einem Fehler, wird dieser Fehler geloggt.
	 *
	 * @param obj Objekt. Darf <code>null</code> sein.
	 */
	public static void close(Closeable obj) {
		try {
			if(obj != null) obj.close();
		}
		catch(IOException e) {
			debug.warning(ErrorMessage.CAN_NOT_ACCESS_FILE, e);
		}
	}

	/**
	 * Erzeugt ein temporäres Verzeichnis. Falls das Verzeichnis schon existiert, wird der Inhalt gelöscht.
	 *
	 * @param dirName Unterordner, der im temporären Verzeichnis angelegt werden soll.
	 *
	 * @return Pfad des angelgeten Verzeichnisses. Endet mit {@link File#separator}.
	 */
	public static String deleteCreateNewTempDir(String dirName) {
		String path = tempDir(dirName);
		Util.deleteCreateNewDir(new File(path));
		return path;
	}

	/**
	 * @param dirName Unterordner im temporären Verzeichnis
	 *
	 * @return Pfad des Unterordners im temporären Verzeichnis. Endet mit {@link File#separator}.
	 */
	public static String tempDir(String dirName) {
		String path = System.getProperty("java.io.tmpdir");
		if(!path.endsWith(File.separator)) path += File.separator;
		path += dirName;
		if(!dirName.endsWith(File.separator) && !dirName.equals("")) path += File.separator;
		return path;
	}

	/**
	 * Vergleicht zwei Objekte. Zwei Objekte sind gleich, wenn<br> - obj1 nicht <code>null</code> ist, und <code>obj1.equals(obj2)==true</code><br> - beide Objekte
	 * <code>null</code> sind.
	 *
	 * @param obj1 Objekt.
	 * @param obj2 Objekt.
	 *
	 * @return <code>true</code> wenn die Objekte gleich sind.
	 */
	public static boolean cmpObj(Object obj1, Object obj2) {
		if(obj1 != null) {
			return obj1.equals(obj2); // Objekte direkt Vergleichen
		}
		else if(obj2 != null) {
			return false; // obj1 ist null, obj2 nicht
		}
		else {
			return true; // beide Objekte sind null.
		}
	}

	/**
	 * Setzt alle Instanzvariablen eines Objektes auf <code>null</code>. Geerbte und statische Variablen werden nicht berücksichtigt. Fehler werden auf der Konsole
	 * ausgegeben und ein fail aufgerufen.
	 *
	 * @param object Objekt
	 */
	public static void nullifyFields(Object object) {
		try {
			nullifyFields(object.getClass(), object);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Setzt die Instanzvariablen des Objekts zurück. Es erfolgt keine Prüfung, ob objClass und object zusammenpassen.
	 *
	 * @param objClass Klasse des Objekts
	 * @param object   Objekt
	 *
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 */
	private static void nullifyFields(Class objClass, Object object) throws SecurityException, IllegalAccessException {
		if(objClass == null || object == null) return;
		Field[] fields = objClass.getDeclaredFields();
		for(Field field : fields) {
			if((field.getModifiers() & Modifier.STATIC) == 0 && (field.getModifiers() & Modifier.FINAL) == 0) {
				field.setAccessible(true);
				try {
					field.set(object, null);
				}
				catch(IllegalArgumentException iae) {
					// falls primitiver Datentyp übergeben wurde
				}
			}
		}
	}

	/**
	 * Entspricht {@link #nullifyFields(Object)}, ändert jedoch auch geerbte Felder.
	 *
	 * @param object Objekt, dessen Instanzvariablen auf <code>null</code> gesetzt werden.
	 *
	 * @see #nullifyFields(Object)
	 */
	public static void nullifyFieldsSuperClass(Object object) {
		if(object.getClass().getSuperclass() == null) {
			nullifyFields(object);
		}
		else {
			try {

				Class classes[] = new Class[]{object.getClass(), object.getClass().getSuperclass(),};
				for(int i = 0; i < classes.length; i++) {
					nullifyFields(classes[i], object);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
