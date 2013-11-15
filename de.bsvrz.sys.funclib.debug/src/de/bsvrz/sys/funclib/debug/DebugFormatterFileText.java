/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.debug.
 * 
 * de.bsvrz.sys.funclib.debug is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.debug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.debug; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.*;
import java.util.Date;
import java.util.logging.*;

/**
 * Gibt die DebugMeldungen als Text aus. Die Ausgabe erfolgt in der folgenden Form (Bespiel für Ausgaben in
 * Verschiedenen Leveln und mit mehrzeiligen Meldungstexten): <PRE><CODE> ------- 18.09.2003
 * 16:58:22,960:+0200(TID:......) ---------------------- STATUS Ausgabedatei angelegt. #000000 18.09.2003
 * 16:58:22,960:+0200(TID:000010) ---------------------- INFO LOGGER :ApplikationName MELDUNG: Aktuelle
 * Debugeinstellungen ---------------------------------------------- Registrierte DebugLogger: global ApplikationName
 * <p/>
 * Basiseinstellung des Wurzel-DebugLoggers ---------------------------------------------- Name
 * :ApplikationName DebugLevel      :ALL Filter          :null ParentName      :java.util.logging.LogManager$RootLogger@e7b241
 * RCS-Bundle      :null RCS-Bundle-Name :null #000001 18.09.2003 16:58:22,970:+0200(TID:000010) ######################
 * FEHLER LOGGER :ApplikationName.hck.debug.TestLogger MELDUNG: hck.debug.TestLogger :Testausgabe in SEVERE #000002
 * 18.09.2003 16:58:22,970:+0200(TID:000010) ====================== WARNUNG LOGGER :ApplikationName.hck.debug.TestLogger
 * MELDUNG: hck.debug.TestLogger :Testausgabe in WARNING #000003 18.09.2003 16:58:22,970:+0200(TID:000010)
 * ---------------------- INFO LOGGER :ApplikationName.hck.debug.TestLogger MELDUNG: hck.debug.TestLogger :Testausgabe
 * in INFO #000004 18.09.2003 16:58:23,070:+0200(TID:000010) ...................... KONFIG LOGGER
 * :ApplikationName.hck.debug.TestLogger MELDUNG: hck.debug.TestLogger :Testausgabe in CONFIG #000005 18.09.2003
 * 16:58:23,070:+0200(TID:000010) .  .  .  .  .  .  .  . FEIN LOGGER :ApplikationName.hck.debug.TestLogger MELDUNG:
 * hck.debug.TestLogger :Testausgabe in FINE #000006 18.09.2003 16:58:23,070:+0200(TID:000010) .  .  .  .  .  .  .  .
 * FEINER LOGGER :ApplikationName.hck.debug.TestLogger MELDUNG: hck.debug.TestLogger :Testausgabe in FINER #000007
 * 18.09.2003 16:58:23,070:+0200(TID:000010) .  .  .  .  .  .  .  . DETAIL LOGGER :ApplikationName.hck.debug.TestLogger
 * MELDUNG: hck.debug.TestLogger :Testausgabe in FINEST </CODE></PRE>
 *
 * @author Hans Christian Kniß (HCK)
 * @version $Revision: 5003 $ / $Date: 2007-08-27 21:41:46 +0200 (Mon, 27 Aug 2007) $
 */
public class DebugFormatterFileText extends java.util.logging.Formatter {

	/**
	 * Formatstring für das Ausgabeformat des Zeitstempels. Ausgabe erfolgt mit Datum, Uhrzeit, Millisekunden und
	 * Zeitoffset zur Zeitangabe in UMT.
	 */
	private static final DateFormat _absoluteMillisecondsFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS:Z");
	/**
	 * Formatstring mit Angabe für die Formatierung der Zahlenausgaben für die lfd. Nr. der Meldung und die ThreadId.
	 */
	private static final DecimalFormat _numberFormat = new DecimalFormat("000000");
	/**
	 * Systemabhängig zur Laufzeit ermittelter String für den Zeilenumbruch. Daduch wird idie Meldungsausgabe
	 * plattformunabhängig.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");
	/**
	 * Linientyp wird bei der Ausgabe vom Level FEHLER verwendet
	 */
	private static final String HASH_LINE = " ###################### ";
	/**
	 * Linientyp wird bei der Ausgabe vom Level WARNUNG verwendet
	 */
	private static final String DOUBLE_LINE = " ====================== ";
	/**
	 * Linientyp wird bei der Ausgabe vom Level INFO verwendet
	 */
	private static final String SINGLE_LINE = " ---------------------- ";
	/**
	 * Linientyp wird bei der Ausgabe vom Level KONFIGURATION verwendet
	 */
	private static final String DOT_LINE = " ...................... ";
	/**
	 * Linientyp wird bei der Ausgabe vom Level FEIN, FEINER und DETAIL verwendet
	 */
	private static final String SIMPLE_LINE = " .  .  .  .  .  .  .  . ";

	/**
	 * Gibt dei DebungMeldung aus
	 *
	 * @param lr LogRecord mit den Informationen einer Meldung.
	 *
	 * @return Gibt den als Text formatierten Meldungstext mit den im LogRecord übergebenen Informationen aus.
	 */
	public String format(LogRecord lr) {
		Date date = new Date(lr.getMillis());

		StringBuffer sb = new StringBuffer().append(NEWLINE);
		sb.append("#").append(_numberFormat.format(lr.getSequenceNumber()));			// Kopfzeile: LfdNr...
		sb.append(" ");
		sb.append(_absoluteMillisecondsFormat.format(date));							// ... Datum ...
		sb.append(" (TID:").append(_numberFormat.format(lr.getThreadID())).append(")");  // ...Thread ID ...

		if (lr.getLevel() == Debug.ERROR) {
			sb.append(HASH_LINE);														// ... Linie abhängig ...
		}																				// ... vom Level ...
		else if (lr.getLevel() == Debug.WARNING) {
			sb.append(DOUBLE_LINE);
		}
		else if (lr.getLevel() == Debug.INFO) {
			sb.append(SINGLE_LINE);
		}
		else if (lr.getLevel() == Debug.CONFIG) {
			sb.append(DOT_LINE);
		}
		else {
			sb.append(SIMPLE_LINE);
		}
		sb.append(NEWLINE);

		Level l = lr.getLevel();														// ... und Level im Klartext.
		if (l == Debug.ERROR)
			sb.append("FEHLER : ");
		else if (l == Debug.WARNING)
			sb.append("WARNUNG: ");
		else if (l == Debug.INFO)
			sb.append("INFO   : ");
		else if (l == Debug.CONFIG)
			sb.append("KONFIG : ");
		else if (l == Debug.FINE)
			sb.append("FEIN   : ");
		else if (l == Debug.FINER)
			sb.append("FEINER : ");
		else if (l == Debug.FINEST)
			sb.append("DETAIL : ");
		sb.append(lr.getLoggerName()).append(NEWLINE);				                    // 2. Zeile: Wurzel+Klassenname

		

		sb.append(lr.getMessage());

		Object[] objects = lr.getParameters();

		if ((objects != null) && (objects.length == 1)) {
			final Object object = objects[0];
			sb.append(": ").append(object == null ? "null" : object.toString());
		}

		if ((objects != null) && (objects.length > 1)) {
			sb.append(':').append(NEWLINE);
			for (int i = 0; i < objects.length; i++) {
				final Object object = objects[i];
				sb.append("[").append(i).append("] ").append(object == null ? "null" : object.toString()).append(NEWLINE);
			}
		}

		if (lr.getThrown() != null) {
			sb.append(":").append(NEWLINE);
			;
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				lr.getThrown().printStackTrace(pw);
				pw.close();
				sb.append("      ").append(sw.toString());
			}
			catch (Exception ex) {
			}
		}

		sb.append(NEWLINE);
		return sb.toString();
	}

	/**
	 * Gibt am Anfang des Protokolls bzw. der Datei den Text "Ausgabedatei angelegt." aus
	 *
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 *
	 * @return String mit einleitendem Meldungstext
	 */
	public String getHead(Handler h) {
		StringBuffer sb = new StringBuffer().append(NEWLINE);
		sb.append("-------").append(_absoluteMillisecondsFormat.format(new Date(System.currentTimeMillis())));
		sb.append("(TID:......)");
		sb.append(SINGLE_LINE);
		sb.append("STATUS").append(NEWLINE);
		sb.append("Ausgabedatei angelegt.").append(NEWLINE);
		return sb.toString();
	}

	/**
	 * Gibt am Ende des Protokolls bzw. der Datei den Text "Ausgabedatei angelegt." aus. ACHTUNG: Wird nicht bei StdErr
	 * ausgegeben oder bei abnormaler Beendigung der Debungausgabe!
	 *
	 * @param h Handler der den Formatter aufgerufen hat. Wird nicht verwendet.
	 *
	 * @return String mit abschliessendem Meldungstext
	 */

	public String getTail(Handler h) {
		StringBuffer sb = new StringBuffer().append(NEWLINE);
		sb.append("-------").append(_absoluteMillisecondsFormat.format(new Date(System.currentTimeMillis())));
		sb.append("(TID:......)");
		sb.append(SINGLE_LINE);
		sb.append("STATUS").append(NEWLINE);
		sb.append("Ausgabedatei korrekt abgeschlossenen.").append(NEWLINE);
		return sb.toString();
	}
}
