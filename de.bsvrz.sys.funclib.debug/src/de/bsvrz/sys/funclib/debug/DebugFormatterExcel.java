/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.debug.
 * 
 * de.bsvrz.sys.funclib.debug is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.debug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.debug; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.debug;

import java.util.logging.*;
import java.util.Date;
import java.text.*;

/** Gibt die Debugmeldungen als *.csv Datei aus, so dass diese direkt in Excel als
 * Tabellenblatt geöffnet und weiterverarbeitet werden können.
 * @author Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$
 */
public class DebugFormatterExcel extends java.util.logging.Formatter {

	/** Formatstring für das Ausgabeformat des Zeitstempels. Ausgabe erfolgt mit Datum,
	 * Uhrzeit, Millisekunden und Zeitoffset zur Zeitangabe in UMT.
	 */
	private static final DateFormat _absoluteMillisecondsFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS:Z");
	/** Formatstring mit Angabe für die Formatierung der Zahlenausgaben für die lfd. Nr.
	 * der Meldung und die ThreadId.
	 */
	private static final DecimalFormat _numberFormat = new DecimalFormat("000000");
	/** Systemabhängig zur Laufzeit ermittelter String für den Zeilenumbruch. Daduch
	 * wird idie Meldungsausgabe plattformunabhängig.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");

	/** Gibt dei DebungMeldung aus
	 * @param lr LogRecord mit den Informationen einer Meldung.
	 * @return Gibt den als *.csv kompatiblen formatierten Meldungstext mit den im LogRecord
	 * übergebenen Informationen aus.
	 */

	public String format(LogRecord lr) {
		Date date = new Date(lr.getMillis());

		StringBuffer sb = new StringBuffer();
		sb.append(_numberFormat.format(lr.getSequenceNumber())).append(";");
		sb.append(_absoluteMillisecondsFormat.format(date)).append(";");
		Level l = lr.getLevel();
		if      (l == Debug.ERROR)
			sb.append("FEHLER").append(";");
		else if (l == Debug.WARNING)
			sb.append("WARNUNG").append(";");
		else if (l == Debug.INFO)
			sb.append("INFO").append(";");
		else if (l == Debug.CONFIG)
			sb.append("KONFIG").append(";");
		else if (l == Debug.FINE)
			sb.append("FEIN").append(";");
		else if (l == Debug.FINER)
			sb.append("FEINER").append(";");
		else if (l == Debug.FINEST)
			sb.append("DETAIL").append(";");

		sb.append('"').append(lr.getMessage()).append('"').append(";");
		sb.append(lr.getLoggerName()).append(";");
		sb.append(_numberFormat.format(lr.getThreadID()));
		sb.append(NEWLINE);

		return (sb.toString());
	}

	/** Gibt in der ersten Zeile der *.csv Datei die Spaltenüberschriften aus.
	 * LfdNr Zeitpunkt DebugLevel Meldungstext DebugLogger ThreadId
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 * @return String mit einleitendem Meldungstext
	 */
	public String getHead(Handler h) {
		StringBuffer sb = new StringBuffer();
		sb.append("LfdNr;").append("Zeitpunkt;").append("DebugLevel;").append("Meldungstext;").append("DebugLogger;").append("ThreadId").append(NEWLINE);
		return sb.toString();
	}
	/** Gibt am Ende als letzten Meldungstext "Ausgabedatei korrekt abgeschlossen." aus.
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 * @return String mit abschliessenden Meldungstext
	 */
	public String getTail(Handler h) {
		StringBuffer sb = new StringBuffer();
		sb.append("ENDE").append(";");
		sb.append(_absoluteMillisecondsFormat.format(new Date(System.currentTimeMillis()))).append(";");
		sb.append("STATUS").append(";");
		sb.append("Ausgabedatei korrekt abgeschlossenen.").append(";");
		sb.append("DebugLogger").append(";");
		sb.append(" ").append(NEWLINE);
		return sb.toString();
	}
}
