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

/** Formatiert die Debugmeldungen für die XML-Ausgabe.
 * Folgende DTD liegt der Ausgabe zugrunde:
 * <pre><code>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?>
 * &lt;!ELEMENT DebugAusgabe (LfdNr, Zeitpunkt, DebugLevel, MeldungsText, DebugLogger, ThreadID)>
 * &lt;!ELEMENT DebugLevel (#PCDATA)>
 * &lt;!ELEMENT DebugLogger (#PCDATA)>
 * &lt;!ELEMENT LfdNr (#PCDATA)>
 * &lt;!ELEMENT MeldungsText (#PCDATA)>
 * &lt;!ELEMENT ThreadID (#PCDATA)>
 * &lt;!ELEMENT Zeitpunkt (#PCDATA)>
 * &lt;!ELEMENT debug (DebugAusgabe+)>
 * </code></pre>
 *
 * @author Hans Christian Kniß (HCK)
 * @version $Revision$ / $Date$
 */
public class DebugFormatterXML extends java.util.logging.Formatter {

	/** Formatstring für das Ausgabeformat des Zeitstempels. Ausgabe erfolgt mit Datum,
	 * Uhrzeit, Millisekunden und Zeitoffset zur Zeitangabe in UMT.
	 */
	private static final DateFormat _absoluteMillisecondsFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS:Z");
	/** Formatstring mit Angabe für die Formatierung der Zahlenausgaben für die lfdNr
	 * der Meldung und die ThreadId.
	 */
	private static final DecimalFormat _numberFormat = new DecimalFormat("000000");
	/** Systemabhängig zur Laufzeit ermittelter String für den Zeilenumbruch. Daduch
	 * wird idie Meldungsausgabe plattformunabhängig.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");

	/** Liefert einen String der Debuginformationen im XML-Format mit dem XML-Element
	 * DebugAusgabe:
	 * <pre><code>
	 * &lt;?xml version="1.0" encoding="ISO-8859-1"?>
	 * &lt;!ELEMENT DebugAusgabe (LfdNr, Zeitpunkt, DebugLevel, MeldungsText, DebugLogger, ThreadID)>
	 * &lt;!ELEMENT DebugLevel (#PCDATA)>
	 * &lt;!ELEMENT DebugLogger (#PCDATA)>
	 * &lt;!ELEMENT LfdNr (#PCDATA)>
	 * &lt;!ELEMENT MeldungsText (#PCDATA)>
	 * &lt;!ELEMENT ThreadID (#PCDATA)>
	 * &lt;!ELEMENT Zeitpunkt (#PCDATA)>
	 * &lt;!ELEMENT debug (DebugAusgabe+)>
	 * </pre></code>
	 * @param lr LogRecord mit den Informationen einer Meldung.
	 * @return Gibt ein XML-Element <CODE><DebugAusgabe></CODE> mit den im LogRecord
	 * übergebenen Informationen aus.
	 */

	public String format(LogRecord lr) {
		Date date = new Date(lr.getMillis());

		StringBuffer sb = new StringBuffer();
		sb.append("<DebugAusgabe>").append(NEWLINE);
		sb.append("<LfdNr>");
		sb.append(_numberFormat.format(lr.getSequenceNumber()));
		sb.append("</LfdNr>").append(NEWLINE);

		sb.append("<Zeitpunkt>");
		sb.append(_absoluteMillisecondsFormat.format(date));
		sb.append("</Zeitpunkt>").append(NEWLINE);

		sb.append("<DebugLevel>");
		Level l = lr.getLevel();
		if      (l == Debug.ERROR)
			sb.append("FEHLER");
		else if (l == Debug.WARNING)
			sb.append("WARNUNG");
		else if (l == Debug.INFO)
			sb.append("INFO");
		else if (l == Debug.CONFIG)
			sb.append("KONFIG");
		else if (l == Debug.FINE)
			sb.append("FEIN");
		else if (l == Debug.FINER)
			sb.append("FEINER");
		else if (l == Debug.FINEST)
			sb.append("DETAIL");
		sb.append("</DebugLevel>").append(NEWLINE);

		sb.append("<MeldungsText>");
		sb.append(lr.getMessage());
		sb.append("</MeldungsText>").append(NEWLINE);

		sb.append("<DebugLogger>");
		sb.append(lr.getLoggerName());
		sb.append("</DebugLogger>").append(NEWLINE);

		sb.append("<ThreadID>");
		sb.append(_numberFormat.format(lr.getThreadID()));
		sb.append("</ThreadID>").append(NEWLINE);

		sb.append("</DebugAusgabe>").append(NEWLINE);

		return (sb.toString());
	}


	/** Gibt am Anfang einer Datei einmalig den notwendigen XML-Kopf aus.
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 * @return String mit XML-Kopf:<br>
	 * <CODE>
	 * &lt?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
	 * &lt!DOCTYPE debug SYSTEM "debug.dtd">
	 * &ltdebug>
	 * </CODE>.
	 */
	public String getHead(Handler h) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=").append('"').append("1.0").append('"').append(" encoding=").append('"').append("ISO-8859-1").append('"').append(" standalone=").append('"').append("no").append('"').append("?>").append(NEWLINE);
		sb.append("<!DOCTYPE debug SYSTEM ").append('"').append("debug.dtd").append('"').append(">").append(NEWLINE);
		sb.append("<debug>").append(NEWLINE);
		return sb.toString();
	}

	/** Gibt am Ende der Datei die abschliessenden XML-Elemente aus.
	 * Wird der Prozess nicht normal terminiert, kann diese abschliessende Information
	 * in der Datei fehlen. Das XML-Dokument ist dann nicht "wohlgeformt".
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 * @return String mit abschliessendem <CODE></debug></CODE>.
	 */
	public String getTail(Handler h) {
		StringBuffer sb = new StringBuffer();
		sb.append("</debug>").append(NEWLINE);
		return sb.toString();
	}
}
