/*
 * Copyright 2003 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.gui.LoggingFrame;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Stellt Methoden für die Debugausgabe zur Vefügung.<br> Unterstützt werden neben unterschiedlichen DebugLeveln auch hierarisch verwaltete DebugBereiche, die
 * je Klasse angelegt werden können. Für die Ausgabe stehen verschiedene Ausgabekänale mit jeweils spezifischen Formatierungen der Ausgabe zur Verfügung ({@link
 * DebugFormatterXML}, {@link DebugFormatterFileText}, {@link DebugFormatterExcel}, {@link DebugFormatterHTML}).
 * <p/>
 * Die Ausgabe (über den DebugLevel) kann zur Laufzeit sowohl für die verfügbaren Ausgabekanäle als auch die DebugBereiche geändert werden.
 * <p/>
 * Für jeden DebugBereich (jeder Klasse, in denen DebugAusgben vorgenommen werden) kann der Level ({@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link
 * #CONFIG}, {@link #FINE}, {@link #FINER}, {@link #FINEST}, {@link #ALL}, {@link #OFF}) unabhängig vom Level des jeweiligen Ausgabekanals ({@link
 * DebugFormatterXML}, {@link DebugFormatterStdErrText}, {@link DebugFormatterFileText}, {@link DebugFormatterExcel}, {@link DebugFormatterHTML}) eingestellt
 * werden. Damit nicht bei einer Applikation mit hunderten von Klassen (und damit entsprechend vielen DebugBereichen) nicht jeden DebugBereich einzeln
 * einstellen zu müssen, werden die DebugBereiche entsprechend ihrer Hierachie verwaltet. Wird der Level eines DebugBereichs geändert, so werden auch alle Level
 * für die untergeordneten Bereiche mit umgestellt. Setzt man also den Level des WurzelLoggers auf FEHLER, so werden alle Logger auf diesen Level gesetzt und
 * nur noch Fehlermeldungen protokolliert. Anschließend kann dann der Logger einer speziel zu untersuchenden Klasse auf z.B. den Level FEINER gesetzt werden, um
 * so genauere Informationen über einen speziellen Programmteil zu protokollieren.
 *
 * @author Kappich+Kniß Systemberatung Aachen (K2S)
 * @author Hans Christian Kniß (HCK)
 * @version $Revision: 6566 $ / $Date: 2009-04-20 17:21:33 +0200 (Mon, 20 Apr 2009) $ / ($Author: rs $)
 */

public class Debug {

	// ------------------------------ FIELDS ------------------------------

	/** Zeilenumbruch plattformunabhängig */
	public static final String NEWLINE = System.getProperty("line.separator");

	/** FEHLER ist höchster Level: Verwendung ausschließlich für "echte" Fehler */
	public final static Level ERROR = Level.SEVERE;

	/** Verwendung nur für Warnungen, die vom Programm zwar noch abgefangen werden können, aber unbedingt behoben werden müssen. */
	public final static Level WARNING = Level.WARNING;

	/** Verwendung für Infoausgaben (z.B. Status des Programms, verwendete Startparameter etc.). */
	public final static Level INFO = Level.INFO;

	/** Verwendung für Konfigurationsinformationen (z.B. angemeldete Objekte etc.). */
	public final static Level CONFIG = Level.CONFIG;

	/** Verwendung für programmnahe Ausgaben zur Verfolgung des Programmablaufs. */
	public final static Level FINE = Level.FINE;

	/** Wie bei {@link #FINE}, aber feinere Ausgabe. */
	public final static Level FINER = Level.FINER;

	/** Wie bei {@link #FINER}, aber mit allen Details. */
	public final static Level FINEST = Level.FINEST;

	/** Schaltet die Ausgabe aller Level ein */
	public final static Level ALL = Level.ALL;

	/** Schaltet die Ausgabe aller Level aus */
	public final static Level OFF = Level.OFF;

	/** Wurzel-DebugLogger, von dem alle weiteren Logger die Eigenschaften erben */
	private static Debug _rootLogger;

	/** (Default) Name des Wurzel-DebugLogger, wird auch als Bestandteil der Ausgabedateien verwendet */
	private static String _rootName = "root";

	/** Aktueller Formatter für Ausgaben im XML-Format */
	private static Formatter _formatterXML;

	/** Aktueller Formatter für Ausgaben im Text-Format auf StdErr */
	private static Formatter _formatterStdErrText;

	/** Aktueller Formatter für Ausgaben im Text-Format auf Datei */
	private static Formatter _formatterFileText;

	/** Aktueller Formatter für Ausgaben im csv-Format */
	private static Formatter _formatterExcel;

	/** Aktueller Formatter für Ausgaben im HTML-Format */
	private static Formatter _formatterHTML;

	/** Aktueller Handler für Dateiausgabe im XML-Format */
	private static Handler _handlerFileXML;

	/** Aktueller Handler für Dateiausgabe im Text-Format */
	private static Handler _handlerFileText;

	/** Aktueller Handler für Dateiausgabe im csv-Format */
	private static Handler _handlerFileExcel;

	/** Aktueller Handler für Dateiausgabe im HTML-Format */
	private static Handler _handlerFileHTML;

	/** Aktueller Handler für stderr-Ausgabe im Text-Format */
	private static Handler _handlerStderrText;

	/**
	 * Pfad für Ausgabedateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/debug/{@link #_rootName}.{@link
	 * #_debugFileNamePattern}._fileExtensionXYZ (z.B. "C:/root-%u-%g.xml") Kann über Aufrufparameter geändert werden.
	 */
	private static String _debugFilePath = ".";

	private static File _debugPath;

	/**
	 * Dateinamemuster für Ausgabedateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/{@link #_rootName}.{@link
	 * #_debugFileNamePattern}._fileExtensionXYZ (z.B. "C:/root-%u-%g.xml") Kann über Aufrufparameter geändert werden.
	 */
	private static String _debugFileNamePattern = "-%u-%g";

	/**
	 * Dateiendung für XML-Dateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/{@link #_rootName}.{@link #_debugFileNamePattern}.{@link
	 * #_fileExtensionXML} (z.B. "C:/root-%u-%g.log.xml") Kann über Aufrufparameter geändert werden.
	 */
	private static String _fileExtensionXML = ".log.xml";

	/**
	 * Dateiendung für Text-Dateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/{@link #_rootName}.{@link #_debugFileNamePattern}.{@link
	 * #_fileExtensionText} (z.B. "C:/root-%u-%g.log.txt") Kann über Aufrufparameter geändert werden.
	 */
	private static String _fileExtensionText = ".log.txt";

	/**
	 * Dateiendung für HTML-Dateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/{@link #_rootName}.{@link #_debugFileNamePattern}.{@link
	 * #_fileExtensionHTML} (z.B. "C:/root-%u-%g.log.html") Kann über Aufrufparameter geändert werden.
	 */
	private static String _fileExtensionHTML = ".log.html";

	/**
	 * Dateiendung für Excel-Dateien. Der komplette Dateiname wird gebildet aus {@link #_debugFilePath}/{@link #_rootName}.{@link #_debugFileNamePattern}.{@link
	 * #_fileExtensionExcel} (z.B. "C:/root-%u-%g.log.csv") Kann über Aufrufparameter geändert werden.
	 */
	private static String _fileExtensionExcel = ".log.csv";

	/** Ungefähre maximale Größe einer Ausgabedatei. Kann über Aufrufparameter geändert werden. */
	private static int _debugFileSize = 10000000;

	/** Anzahl der Ausgabedateien, die wie ein Ringpuffer beschrieben werden. Kann über Aufrufparameter geändert werden. */
	private static int _debugFileCount = 5;

	/**
	 * Legt fest ob an bereits existente Ausgabedateien angehängt werden darf, oder ob die alte Datei überschrieben wird. Kann über Aufrufparameter geändert
	 * werden.
	 */
	private static boolean _debugFileAppend = false;

	/** Aktueller DebugLevel für Dateiausgaben im XML-Format Kann über Aufrufparameter geändert werden. */
	private static Level _debugLevelFileXML = OFF;

	/** Aktueller DebugLevel für Dateiausgaben im Text-Format Kann über Aufrufparameter geändert werden. */
	private static Level _debugLevelFileText = OFF;

	/** Aktueller DebugLevel für Dateiausgaben im csv-Format Kann über Aufrufparameter geändert werden. */
	private static Level _debugLevelFileExcel = OFF;

	/** Aktueller DebugLevel für Dateiausgaben im HTML-Format Kann über Aufrufparameter geändert werden. */
	private static Level _debugLevelFileHTML = OFF;

	/** Aktueller DebugLevel für StdErr-Ausgaben im Text-Format Kann über Aufrufparameter geändert werden. */
	private static Level _debugLevelStdErrText = WARNING;

	/** Logger Objekt. */
	private final Logger _logger;

	// -------------------------- STATIC METHODS --------------------------

	static {
		init("DEFAULT-DEBUG", new ArgumentList(new String[]{""}));
	}

	/**
	 * Initialisiert den DebugLogger.
	 * <p/>
	 * Folgende Initialisierungen werden durchgeführt: <<ul> <li>Alle registrierten DebugLogger werden entfernt</li> <li>Es wird der Wurzel-DebugLogger
	 * initialisiert, der den Namen des Aufrufparameters erhält.</li> <li>Es wird der Dateipfad für alle dateibasierten Debugausgaben festgelegt</li> <li>Es werden
	 * folgende Ausgabekanäle und Ausgabeformate angemeldet und die Ausgabelevel auf Default eingestellt:</li> <ul> <li>Datei im XML-Format (*.xml),
	 * DebugLevel:INFO</li> <li>Datei mit Ausgaben als formatierter Text (*.txt), DebugLevel:INFO</li> <li>Datei im EXCEL-Format (*.csv), DebugLevel:OFF</li>
	 * <li>Datei im HTML-Format(*.htm), DebugLevel:OFF</li> <li>Ausgabe auf stderr als Text, DebugLevel:INFO</li> </ul> Die Ausgabekanäle/Ausgabeformate stehen
	 * anschliessend allen DebugLoggern automatisch zur Verfügung. <li>Auswertung der Aufrufparameter der Applikation</li> <li>Anmeldung der Telegramme zur
	 * Steuerung des DebugLoggers über den Datenverteiler</li> <li>Alle registrierten DebugLogger werden entfernt</li> </ul>
	 *
	 * @param argumentList Liste der bei Applikationsstart übergebenen Parameter zur Voreinstellung.
	 * @param rootName     Wurzel-Name aller verwalteter DebugLogger
	 *
	 * @since V 1.0
	 */
	public static void init(String rootName, ArgumentList argumentList) {
		// Wurzel-Logger anmelden
		_rootName = rootName;
		_rootLogger = new Debug(Logger.getLogger(_rootName));
		_rootLogger._logger.setUseParentHandlers(false);
		_rootLogger._logger.setLevel(ALL);

		int debugStdErrWithStackTraces = -1;
		try {
			// Parameter -debugHelp bzw. -? auswerten

			// Parameter auswerten
			_debugFileSize = argumentList.fetchArgument("-debugFileSize=10000000").intValue();
			_debugFileCount = argumentList.fetchArgument("-debugFileCount=5").intValue();
			_debugLevelStdErrText = string2Level(argumentList.fetchArgument("-debugLevelStdErrText=WARNING").asString());
			_debugLevelFileText = string2Level(argumentList.fetchArgument("-debugLevelFileText=OFF").asString());
			_debugLevelFileXML = string2Level(argumentList.fetchArgument("-debugLevelFileXML=OFF").asString());
			_debugLevelFileExcel = string2Level(argumentList.fetchArgument("-debugLevelFileExcel=OFF").asString());
			_debugLevelFileHTML = string2Level(argumentList.fetchArgument("-debugLevelFileHTML=OFF").asString());
			_debugPath = argumentList.fetchArgument("-debugFilePath=.").asDirectory();
			final ArgumentList.ValueSelection stackTracesSelection = new ArgumentList.ValueSelection();
			stackTracesSelection.add("auto").ignoreCase().convertTo(-1);
			stackTracesSelection.add("ja").alias("wahr").alias("an").alias("true").alias("yes").alias("on").ignoreCase().convertTo(1);
			stackTracesSelection.add("nein").alias("falsch").alias("aus").alias("false").alias("no").alias("off").ignoreCase().convertTo(1);
			debugStdErrWithStackTraces = argumentList.fetchArgument("-debugStdErrTextStacktraces=auto").asValueCase(stackTracesSelection).intValue();
			_debugFilePath = _debugPath.getAbsolutePath();

			if(argumentList.fetchArgument("-debugGui=nein").booleanValue()) {
				// Das GUI zur Verwaltung der Logger anzeigen
				new LoggingFrame().setVisible(true);
			}
			try {
				while(argumentList.hasArgument("-debugSetLoggerAndLevel")) {
					String arg = argumentList.fetchArgument("-debugSetLoggerAndLevel").asString();
					String[] args = arg.split(":");
					String loggerName;
					if(args[0].trim().equals("")) {
						loggerName = _rootName;
					}
					else {
						loggerName = _rootName + "." + args[0];
					}
					String level = args[1];

					new Debug(Logger.getLogger(loggerName));

					Logger.getLogger(loggerName).setLevel(string2Level(level));
				}
			}
			catch(Exception e) {
				System.err.println("Fehler bei der Auswertung von -debugSetLoggerAndLevel");
			}
		}
		catch(IllegalArgumentException e) {
			System.err.println(e);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("Debug Initialisierung ohne Parameter. Es wird der Standard verwendet. " + e);
		}

		// Handler und Formatter erzeugen und registrieren
		try {
			File directory = new File(_debugPath, "debug");
			String fileName = directory.getAbsolutePath() + "/" + _rootName + _debugFileNamePattern;
			//Handler und Formatter erzeugen, aber nur, wenn mindestens ein FileFormater ungleich OFF
			if(!((_debugLevelFileXML == OFF) && (_debugLevelFileText == OFF) && (_debugLevelFileHTML == OFF) && (_debugLevelFileExcel == OFF))) {
				// System.err.println("Debug-Ordner anlegen: " + directory.getAbsolutePath());
				// System.err.println("filename: " + fileName);
				directory.mkdir();
			}

			// Jeweils Handler und Formatter holen und verknüpfen, Handlerlevel setzen und beim
			// Rootlogger registrieren.
			// Registrierung erfolgt nur, wenn Level ungleich OFF ist (Ausnahme: Ausgabe auf StdErr erfolgt immer!

			if(debugStdErrWithStackTraces == -1) {
				if(_debugLevelFileText.equals(OFF)) {
					debugStdErrWithStackTraces = 1;
				}
				else {
					debugStdErrWithStackTraces = 0;
				}
			}
			_formatterStdErrText = new DebugFormatterStdErrText(debugStdErrWithStackTraces == 1);  // Formatierer für Textausgabe auf StdErr holen
			reinstallConsoleHandler();

			if(!_debugLevelFileXML.equals(OFF)) {
				_handlerFileXML = new FileHandler(fileName + _fileExtensionXML, _debugFileSize, _debugFileCount, _debugFileAppend);
				_formatterXML = new DebugFormatterXML();		        // Formatierer für XML-Formatierung holen
				_handlerFileXML.setFormatter(_formatterXML);
				_handlerFileXML.setLevel(_debugLevelFileXML);
				_rootLogger._logger.addHandler(_handlerFileXML);
			}
			if(!_debugLevelFileText.equals(OFF)) {
				_handlerFileText = new FileHandler(fileName + _fileExtensionText, _debugFileSize, _debugFileCount, _debugFileAppend);
				_formatterFileText = new DebugFormatterFileText();      // Formatierer für Textausgabe auf Datei holen
				_handlerFileText.setFormatter(_formatterFileText);
				_handlerFileText.setLevel(_debugLevelFileText);
				_rootLogger._logger.addHandler(_handlerFileText);
			}
			if(!_debugLevelFileHTML.equals(OFF)) {
				_handlerFileHTML = new FileHandler(fileName + _fileExtensionHTML, _debugFileSize, _debugFileCount, _debugFileAppend);
				_formatterHTML = new DebugFormatterHTML();		        // Formatierer für HTML-Formatierung holen
				_handlerFileHTML.setFormatter(_formatterHTML);
				_handlerFileHTML.setLevel(_debugLevelFileHTML);
				_rootLogger._logger.addHandler(_handlerFileHTML);
			}
			if(!_debugLevelFileExcel.equals(OFF)) {
				_handlerFileExcel = new FileHandler(fileName + _fileExtensionExcel, _debugFileSize, _debugFileCount, _debugFileAppend);
				_formatterExcel = new DebugFormatterExcel();	        // Formatierer für Excel-Formatierung holen
				_handlerFileExcel.setFormatter(_formatterExcel);
				_handlerFileExcel.setLevel(_debugLevelFileExcel);
				_rootLogger._logger.addHandler(_handlerFileExcel);
			}
		}
		catch(SecurityException e) {
			System.err.println("Fehler bei Konfiguration der Handler des DebugLoggers:");
		}
		catch(IOException e) {
			System.err.println("Fehler bei Konfiguration der Handler des DebugLoggers:");
		}

		if(!rootName.equals("DEFAULT-DEBUG")) {
			// Aktuellen DebugLogger-Zustand auf Debug-Kanäle ausgeben
			_rootLogger.status(debugInfo());

			// Argumentliste ausgeben
			_rootLogger.info("Aufrufargumente von " + rootName, argumentList.toString());
		}
	}

	public static void reinstallConsoleHandler() {
		final ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(_formatterStdErrText);
		consoleHandler.setLevel(_debugLevelStdErrText);
		synchronized(_rootLogger) {
			if(_handlerStderrText != null) {
				_rootLogger._logger.removeHandler(_handlerStderrText);
			}
			_rootLogger._logger.addHandler(consoleHandler);
			_handlerStderrText = consoleHandler;
		}
	}

	/**
	 * Konvertiert den Debuglevel aus den Aufrufparametern von String in Level.
	 *
	 * @param s Zu konvertierender String.
	 *
	 * @return Konvertierter Level.
	 */
	private static Level string2Level(String s) {
		if(s.trim().toUpperCase().equals("ERROR")) return ERROR;
		if(s.trim().toUpperCase().equals("WARNING")) return WARNING;
		if(s.trim().toUpperCase().equals("CONFIG")) return CONFIG;
		if(s.trim().toUpperCase().equals("INFO")) return INFO;
		if(s.trim().toUpperCase().equals("FINE")) return FINE;
		if(s.trim().toUpperCase().equals("FINER")) return FINER;
		if(s.trim().toUpperCase().equals("FINEST")) return FINEST;
		if(s.trim().toUpperCase().equals("ALL")) return ALL;
		//Wenn kein gültiger Wert übergeben wird, Kanal ausschalten.

		return OFF;
	}

	;

	/**
	 * Debugausgabe interner Art (auf Level INFO) für Ausgabe der aktuellen Einstellungen oder bei Änderung der Einstellungen. Bei Ausgabe über
	 * <source>status</source> wird der DebugLevel nur für diese Ausgabe auf INFO gesetzt
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	private void status(String msg) {
		Level _aktLevel = _logger.getLevel();		// aktuellen Level merken...
		_logger.setLevel(INFO);						// ...für die nächste Ausgabe Level auf INFO setzen...
		_logger.log(INFO, msg);						// ...Meldung als Info ausgeben...
		_logger.setLevel(_aktLevel);					// ...und Level wieder zurücksetzen.
	}

	/**
	 * TESTMETHODE: Gibt Info über angemeldete Logger aus
	 *
	 * @return Liste aller angemelder Logger als Text
	 */
	public static String debugInfo() {
		StringBuffer sb = new StringBuffer();
		String NEWLINE = System.getProperty("line.separator");
		sb.append("Aktuelle Debugeinstellungen").append(NEWLINE);
		sb.append("----------------------------------------------").append(NEWLINE);
		sb.append("Registrierte DebugLogger: ").append(NEWLINE);
		for(Enumeration e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements();) {
			String loggerName = e.nextElement().toString();
			Level level = Logger.getLogger(loggerName).getLevel();
			sb.append("  Name: ").append(loggerName).append("  Level: ").append(level).append(NEWLINE);
		}
		//		sb.append(NEWLINE);
		//		sb.append("Basiseinstellung des Wurzel-DebugLoggers").append(NEWLINE);
		//		sb.append("----------------------------------------------").append(NEWLINE);
		//		sb.append("Name            :").append(_rootLogger.getName()).append(NEWLINE);
		//		sb.append("DebugLevel      :").append(_rootLogger.getLevel()).append(NEWLINE);
		//		sb.append("Filter          :").append(_rootLogger.getFilter()).append(NEWLINE);
		//		sb.append("ParentName      :").append(_rootLogger.getParent()).append(NEWLINE);
		//		sb.append("RCS-Bundle      :").append(_rootLogger.getResourceBundle()).append(NEWLINE);
		//		sb.append("RCS-Bundle-Name :").append(_rootLogger.getResourceBundleName()).append(NEWLINE);

		return (sb.toString());
	}

	/**
	 * Gibt eine DebugLogger zurück, der automatisch in der Klassenhierachie unter dem WurzelLogger einsortiert wurde. Existiert der DebugLogger noch nicht, wird
	 * er erzeugt, ansonsten wird der bereits existierende DebugLogger mit dem angeforderten Namen zurückgegeben. Als Name wird automatisch gebildet aus
	 * "rootName.voll qualifizierter ClassName" z.B. "root.sys.funclib.Debug". Für jeden Logger kann der DebugLevel per DaV-Telegramm geändert werden.
	 *
	 * @return Logger Objekt, welches für die Debugausgeben und zur Steuerung des Debuglevels verwendet wird.
	 */
	public static Debug getLogger() {
		String debugClassName = "Klassenname nicht ermittelbar!";
		Throwable t = new Throwable();
		if(t.getStackTrace().length > 1) {
			debugClassName = t.getStackTrace()[1].getClassName();
		}
		String _loggerName = _rootName + "." + debugClassName;
		return new Debug(Logger.getLogger(_loggerName));
	}

	/**
	 * TESTMETHODE: Setzt DebugLevel des entsprechenden Loggers. Setzt den Level des angebenen Loggers UND aller untergeordneten Logger auf den eingestellten
	 * Level.
	 * <p/>
	 * Es wird dazu der Name des Loggers verwendet. Als untergeordnet gelten aller Logger, deren Namensanfang mit dem des angegebenen Loggers übereinstimmt.
	 *
	 * @param logger Name des mit {@link #getLogger} erzeugter DebugLogger, dessen Level geändert werden soll
	 * @param level  Neuer DebugLevel ({@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #CONFIG}, {@link #FINE}, {@link #FINER}, {@link #FINEST}, {@link
	 *               #ALL}, {@link #OFF}).
	 */
	public static void setLoggerLevel(String logger, Level level) {
		try {
			for(Enumeration e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements();) {
				String loggerName = e.nextElement().toString();
				if(loggerName.startsWith(logger)) {
					Logger.getLogger(loggerName).setLevel(level);
				}
			}
		}
		catch(SecurityException e) {
			_rootLogger.error("Fehler bei Konfiguration der Handler des DebugLoggers:" + e);
		}
	}

	/**
	 * TESTMETHODE: Setzt DebugLevel des entsprechenden Ausgabekanals. Damit werden alle nur noch Ausgaben auf diesem Kanal mit dem eingestellten Level oder
	 * darüber ausgegeben, unabhängig davon ob einzelne Logger einen feineren Level eingestellt haben. Die Leveleinstellungen der einzelnen Logger werden aber
	 * nicht verändert.
	 *
	 * @param s     Ausgabekanal, dessem Level geändert werden soll (StdErr, FileText, FileXML, FileHTML, FileExcel)
	 * @param level Neuer DebugLevel ({@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #CONFIG}, {@link #FINE}, {@link #FINER}, {@link #FINEST}, {@link
	 *              #ALL}, {@link #OFF}).
	 */

	public static void setHandlerLevel(String s, Level level) {
		if(s == "StdErr") {
			_handlerStderrText.setLevel(level);
		}
		else if(s == "FileText") {
			_handlerFileText.setLevel(level);
		}
		else if(s == "FileXML") {
			_handlerFileXML.setLevel(level);
		}
		else if(s == "FileHTML") {
			_handlerFileHTML.setLevel(level);
		}
		else if(s == "FileExcel") {
			_handlerFileExcel.setLevel(level);
		}
		else {
			_rootLogger.error(
					"Levelumschaltung des Ausgabekanals [" + s + "] auf Level " + level + " nicht möglich, da Level [" + s + "] nicht zulässig!"
			);
		}
	}

	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Konstruktor (nur intern verwendet).
	 *
	 * @param logger Ein LoggerObjekt.
	 */
	private Debug(Logger logger) {
		_logger = logger;
	}

	// -------------------------- OTHER METHODS --------------------------

	/**
	 * Debugausgabe auf Level CONFIG.
	 * <p/>
	 * Verwendung für Konfigurationsinformationen (z.B. angemeldete Objekte etc.)
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void config(String msg) {
		_logger.log(CONFIG, msg);
	}

	/**
	 * Debugausgabe auf Level CONFIG.
	 * <p/>
	 * Verwendung für Konfigurationsinformationen (z.B. angemeldete Objekte etc.)
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void config(String msg, Throwable thrown) {
		_logger.log(CONFIG, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level CONFIG.
	 * <p/>
	 * Verwendung für Konfigurationsinformationen (z.B. angemeldete Objekte etc.)
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void config(String msg, Object[] param) {
		_logger.log(CONFIG, msg, param);
	}

	/**
	 * Debugausgabe auf Level CONFIG.
	 * <p/>
	 * Verwendung für Konfigurationsinformationen (z.B. angemeldete Objekte etc.)
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void config(String msg, Object param) {
		_logger.log(CONFIG, msg, param);
	}

	/**
	 * Debugausgabe auf Level ERROR.
	 * <p/>
	 * Verwendung nur für "echte" Fehlerausgaben, die vom Programm nicht abgefangen werden oder die zu unkontrollierbaren Folgefehlern führen.
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void error(String msg, Throwable thrown) {
		_logger.log(ERROR, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level ERROR.
	 * <p/>
	 * Verwendung nur für "echte" Fehlerausgaben, die vom Programm nicht abgefangen werden oder die zu unkontrollierbaren Folgefehlern führen.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void error(String msg, Object[] param) {
		_logger.log(ERROR, msg, param);
	}

	/**
	 * Debugausgabe auf Level ERROR.
	 * <p/>
	 * Verwendung nur für "echte" Fehlerausgaben, die vom Programm nicht abgefangen werden oder die zu unkontrollierbaren Folgefehlern führen.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void error(String msg, Object param) {
		_logger.log(ERROR, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINE.
	 * <p/>
	 * Verwendung für programmnahe Ausgaben zur Verfolgung des Programmablaufs.
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void fine(String msg) {
		_logger.log(FINE, msg);
	}

	/**
	 * Debugausgabe auf Level FINE.
	 * <p/>
	 * Verwendung für programmnahe Ausgaben zur Verfolgung des Programmablaufs.
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void fine(String msg, Throwable thrown) {
		_logger.log(FINE, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level FINE.
	 * <p/>
	 * Verwendung für programmnahe Ausgaben zur Verfolgung des Programmablaufs.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void fine(String msg, Object[] param) {
		_logger.log(FINE, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINE.
	 * <p/>
	 * Verwendung für programmnahe Ausgaben zur Verfolgung des Programmablaufs.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void fine(String msg, Object param) {
		_logger.log(FINE, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINER.
	 * <p/>
	 * Wie bei {@link #fine}, aber feinere Ausgabe.
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void finer(String msg) {
		_logger.log(FINER, msg);
	}

	/**
	 * Debugausgabe auf Level FINER.
	 * <p/>
	 * Wie bei {@link #fine}, aber feinere Ausgabe.
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void finer(String msg, Throwable thrown) {
		_logger.log(FINER, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level FINER.
	 * <p/>
	 * Wie bei {@link #fine}, aber feinere Ausgabe.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void finer(String msg, Object[] param) {
		_logger.log(FINER, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINER.
	 * <p/>
	 * Wie bei {@link #fine}, aber feinere Ausgabe.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void finer(String msg, Object param) {
		_logger.log(FINER, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINEST.
	 * <p/>
	 * Wie bei {@link #finer}, aber maximale Details.
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void finest(String msg) {
		_logger.log(FINEST, msg);
	}

	/**
	 * Debugausgabe auf Level FINEST.
	 * <p/>
	 * Wie bei {@link #finer}, aber maximale Details.
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void finest(String msg, Throwable thrown) {
		_logger.log(FINEST, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level FINEST.
	 * <p/>
	 * Wie bei {@link #finer}, aber maximale Details.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void finest(String msg, Object[] param) {
		_logger.log(FINEST, msg, param);
	}

	/**
	 * Debugausgabe auf Level FINEST.
	 * <p/>
	 * Wie bei {@link #finer}, aber maximale Details.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void finest(String msg, Object param) {
		_logger.log(FINEST, msg, param);
	}

	/**
	 * Debugausgabe auf Level INFO.
	 * <p/>
	 * Verwendung für Infoausgaben (z.B. Status des Programms, verwendete Startparameter etc.)
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void info(String msg) {
		_logger.log(INFO, msg);
	}

	/**
	 * Debugausgabe auf Level INFO.
	 * <p/>
	 * Verwendung für Infoausgaben (z.B. Status des Programms, verwendete Startparameter etc.)
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void info(String msg, Throwable thrown) {
		_logger.log(INFO, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level INFO.
	 * <p/>
	 * Verwendung für Infoausgaben (z.B. Status des Programms, verwendete Startparameter etc.)
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void info(String msg, Object[] param) {
		_logger.log(INFO, msg, param);
	}

	/**
	 * Debugausgabe auf Level INFO.
	 * <p/>
	 * Verwendung für Infoausgaben (z.B. Status des Programms, verwendete Startparameter etc.)
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void info(String msg, Object param) {
		_logger.log(INFO, msg, param);
	}

	/**
	 * TESTMETHODE: Setzt DebugLevel des entsprechenden Loggers. Setzt den Level des angebenen Loggers UND aller untergeordneten Logger auf den eingestellten
	 * Level.
	 * <p/>
	 * Es wird dazu der Name des Loggers verwendet. Als untergeordnet gelten aller Logger, deren Namensanfang mit dem des angegebenen Loggers übereinstimmt.
	 *
	 * @param level Neuer DebugLevel ({@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #CONFIG}, {@link #FINE}, {@link #FINER}, {@link #FINEST}, {@link
	 *              #ALL}, {@link #OFF}).
	 */
	public void setLoggerLevel(Level level) {
		try {
			for(Enumeration e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements();) {
				String loggerName = e.nextElement().toString();
				if(loggerName.startsWith(_logger.getName())) {
					Logger.getLogger(loggerName).setLevel(level);
				}
			}
		}
		catch(SecurityException e) {
			_rootLogger.error("Fehler bei Konfiguration der Handler des DebugLoggers:" + e);
		}
	}

	/**
	 * Debugausgabe auf Level ERROR.
	 * <p/>
	 * Verwendung nur für "echte" Fehlerausgaben, die vom Programm nicht abgefangen werden oder die zu unkontrollierbaren Folgefehlern führen.
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void error(String msg) {
		_logger.log(ERROR, msg);
	}

	/**
	 * Debugausgabe auf Level WARNING.
	 * <p/>
	 * Verwendung nur für Warnungen, die vom Programm zwar noch abgefangen werden können, aber unbedingt behoben werden müssen.
	 *
	 * @param msg Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit ausgegeben,
	 *            so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr der
	 *            Meldung,...)
	 */
	public void warning(String msg) {
		_logger.log(WARNING, msg);
	}

	/**
	 * Debugausgabe auf Level WARNING.
	 * <p/>
	 * Verwendung nur für Warnungen, die vom Programm zwar noch abgefangen werden können, aber unbedingt behoben werden müssen.
	 *
	 * @param msg    Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *               ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *               der Meldung,...)
	 * @param thrown Zusätzliche Meldungen, die sich aus der übergebenen Exeption ergibt.
	 */

	public void warning(String msg, Throwable thrown) {
		_logger.log(WARNING, msg, thrown);
	}

	/**
	 * Debugausgabe auf Level WARNING.
	 * <p/>
	 * Verwendung nur für Warnungen, die vom Programm zwar noch abgefangen werden können, aber unbedingt behoben werden müssen.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldungen, die über die {@link #toString} Methode an den  übergebenen Objekten ausgewertet werden.
	 */

	public void warning(String msg, Object[] param) {
		_logger.log(WARNING, msg, param);
	}

	/**
	 * Debugausgabe auf Level WARNING.
	 * <p/>
	 * Verwendung nur für Warnungen, die vom Programm zwar noch abgefangen werden können, aber unbedingt behoben werden müssen.
	 *
	 * @param msg   Auszugebender Debugtext. Der Text kann auch mehrzeilig formatiert sein. Neben dem Text werden noch folgende Ausgaben automatisch mit
	 *              ausgegeben, so dass diese nicht im Text enthalten sein sollten. (Datum, Zeit, Fehlerlevel (FEHLER, WARNUNG,...), Klasse des Aufrufers, lfdNr
	 *              der Meldung,...)
	 * @param param Zusätzliche Meldung, die über die {@link #toString} Methode am übergebenen Objekt ausgewertet werden.
	 */
	public void warning(String msg, Object param) {
		_logger.log(WARNING, msg, param);
	}
}

