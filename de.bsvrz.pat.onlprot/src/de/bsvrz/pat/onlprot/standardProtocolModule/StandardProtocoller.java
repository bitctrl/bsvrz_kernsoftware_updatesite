/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.onlprot.
 * 
 * de.bsvrz.pat.onlprot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.onlprot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.onlprot.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.onlprot.standardProtocolModule;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Der Standardprotokollierer, welcher eine tabellarische Ausgabe (in drei verschieden ausführlichen Tiefen) und eine XML-Ausgabe erzeugen kann.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class StandardProtocoller extends ProtocolModule {

	/** Die Debug-Ausgabe */
	static private final Debug debug = Debug.getLogger();

	/** Datums-Format */
	private static final DateFormat _dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS:Z");

	/** Schreibt String-Format der Zeitstempel vor */
	private static final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

	/** Beschreibt Zeichenkette, die in jeder Zeile pro Einrückungsebene ausgegeben wird */
	private static final String indentationPrefix = "  ";

	/** Konstante für <code>state</code>: Es wurde noch kein Protokollkopf ausgegeben. */
	private static final int NO_HEADER_WRITTEN = 0;

	/** Konstante für <code>state</code>: Es wurde bereits ein Protokollkopf, aber noch kein -fuß  ausgegeben. */
	private static final int HEADER_WRITTEN = 1;

	/** Konstante für <code>state</code>: Es wurde ein Protokollfuß ausgegeben. */
	private static final int FOOTER_WRITTEN = 2;

	/** Wird in {@link UpdateSummaryProtocoller#update} als Informationstext ausgegeben */
	private final String _actionText;

	/** Anzahl der bereits empfangenen Datensätze */
	private long resultDataCount = 0;

	/**
	 * Zustand des Protokollierers: <ul> <li> <code>state == NO_HEADER_WRITTEN</code>: Ausgangszustand: noch keinen Kopf geschrieben <li> <code>state ==
	 * HEADER_WRITTEN</code>: Kopf wurde ausgegeben. Telegramme werden erwartet und Fußtext kann geschrieben werden. <li> <code>state == FOOTER_WRITTEN</code>: Fuß
	 * wurde ausgegeben. Die Protokollierung ist damit abgeschlossen. Es können keine weiteren Ausgaben folgen. </ul>
	 */
	private int state = NO_HEADER_WRITTEN;

	/** Anzahl der Aufrufe von {@link UpdateSummaryProtocoller#update} */
	private long updateCount = 0;

	/**
	 * Objekt, dessen toString Methode Infos über die aktuelle Hauptspeicherverwendung der Applikation zurückgibt. Das Objekt kann in Debug-Nachrichten als zweiter
	 * Parameter benutzt werden. Die toString-Methode wird dann nur aufgerufen, wenn das Debug-Level so hoch eingestellt ist, dass die Meldung tatsächlich
	 * ausgegeben wird.
	 */
	private static Object _memoryUsage = new Object() {
		public String toString() {
			return String.format(
					"Frei: %.3f MB, Total: %.3f MB",
					(Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0),
					(Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0)
			);
		}
	};

	/** Erzeugt ein neues Objekt der Klasse <code>StandardProtocoller</code> mit dem <code>actionText</code> <em>Aktualisierung</em> */
	public StandardProtocoller() {
		_actionText = "Aktualisierung";
	}

	/**
	 * Erzeugt ein neues Objekt der Klasse <code>StandardProtocoller</code> mit speziellem <code>actionText</code>
	 *
	 * @param actionText String mit dem gewünschten Ausgabetext bei Aktualisierungen
	 */
	public StandardProtocoller(String actionText) {
		_actionText = actionText;
	}

	/**
	 * Gibt Information über die Aufrufparameter des Protokollierungsmoduls zurück
	 *
	 * @return String mit der Beschreibung der erlaubten Aufrufparameter und deren erwartetes Format
	 */
	public String getHelp() {
		return ("-ausgabe: Hier sind folgende Werte erlaubt: " + getProtocollerSelection().getInfo());
	}

	/**
	 * Führt die Initialisierungsschritte des Standardprotokollierers aus. Insbesondere wird einer der gültigen Protokollierer ausgewählt und der Protokollkopf
	 * ausgegeben.
	 *
	 * @param argumentList       {@link ArgumentList} der noch nicht ausgewerteten Aufrufparameter der Applikation
	 * @param protocolFileWriter PrintWriter der protokollierten Datensequenzen
	 * @param args               String[] mit den Kommandozeilenargumenten
	 *
	 * @return ClientReceiverInterface-Handle auf den benutzten Protokollierer
	 */
	public ClientReceiverInterface initProtocol(ArgumentList argumentList, PrintWriter protocolFileWriter, String[] args) {
		/* Liste der Aufrufparameter der Applikation */
		/* Liste der erlaubten Protokollierer */
		ArgumentList.ValueSelection validProtocollers;

		super.setProtocolFileWriter(protocolFileWriter);
		validProtocollers = getProtocollerSelection();
		setProtocoller(
				(ClientProtocollerInterface)argumentList.fetchArgument("-ausgabe=kopf")
						.asValueCase(validProtocollers).convert()
		);
		getProtocoller().writeHeader(args);
		return getProtocoller();
	}
	
	/**
	 * Führt die Initialisierungsschritte des Standardprotokollierers aus. 
	 * Insbesondere wird einer der gültigen Protokollierer ausgewählt.
	 * In dieser Variante wird der Protokollkopf nicht ausgegeben.
	 * 
	 * @param protocollerArgumentList
	 * @param printWriter
	 * @return
	 */
	public ClientReceiverInterface initProtocolWithoutHeader(ArgumentList protocollerArgumentList, PrintWriter printWriter) {
		/* Liste der Aufrufparameter der Applikation */
		/* Liste der erlaubten Protokollierer */
		ArgumentList.ValueSelection validProtocollers;

		super.setProtocolFileWriter(printWriter);
		validProtocollers = getProtocollerSelection();
		setProtocoller(
				(ClientProtocollerInterface)protocollerArgumentList.fetchArgument("-ausgabe=kopf")
						.asValueCase(validProtocollers).convert()
		);
		return getProtocoller();
		
	}

	/**
	 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes vom benutzten Protokollierer aufgerufen wird.
	 *
	 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen
	 */
	public void update(ResultData[] results) {
		++updateCount;
		resultDataCount += results.length;
	}

	/** Führt Aufräumarbeiten nach Beendigung des Standardprotokollierers aus. Fußzeilen werden geschrieben und der Ausgabe-<i>Stream</i> geschlossen. */
	public void closeProtocol() {
		getProtocoller().writeFooter();
		getProtocolFileWriter().flush();
		getProtocolFileWriter().close();
	}

	/**
	 * Stellt Liste der bekannten Protokollierer zusammen. Kann bei der Interpretation von Aufrufargumenten mit der Methode {@link
	 * de.bsvrz.sys.funclib.commandLineArgs.ArgumentList.Argument#asValueCase} benutzt werden, um eine der verschiedenen Ausgabeoptionen auszuwählen.
	 *
	 * @return ValueSelection mit den erlaubten Protokollierern
	 */
	protected ArgumentList.ValueSelection getProtocollerSelection() {

		/* Liste der erlaubten Protokollierer */
		ArgumentList.ValueSelection validProtocollers = new ArgumentList.ValueSelection();

		validProtocollers.add("nein").alias("n").ignoreCase()
				.convertTo(new SilentProtocoller()).purpose("Keine Ausgabe.");
		validProtocollers.add("aktualisierung").alias("a").ignoreCase()
				.convertTo(new UpdateSummaryProtocoller())
				.purpose("Gibt bei jeder Aktualisierung die Anzahl der enthaltenen Datensätze aus.");
		validProtocollers.add("kopf").alias("k").ignoreCase()
				.convertTo(new HeaderProtocoller())
				.purpose("Gibt bei jeder Aktualisierung zusätzlich die Köpfe der enthaltenen Datensätze aus.");
		validProtocollers.add("daten").alias("d").ignoreCase()
				.convertTo(new DataProtocoller())
				.purpose("Gibt bei jeder Aktualisierung zusätzlich die Attributwerte der enthaltenen Datensätze aus.");
		validProtocollers.add("xml").alias("x").ignoreCase()
				.convertTo(new XmlProtocoller())
				.purpose("Gibt die Telegramme in einem XML-Format aus.");
		return validProtocollers;
	}

	/** Keine Ausgabe. */
	private class SilentProtocoller implements ClientProtocollerInterface {

		/** Einziger Konstruktor. */
		protected SilentProtocoller() {
		}

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird. Ruft lediglich
		 * {@link StandardProtocoller#update} auf.
		 *
		 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen.
		 */
		public void update(ResultData[] results) {
			StandardProtocoller.this.update(results);
		}

		/** Gibt "leeren" Fuß aus: <code>state</code> wird lediglich in den Zustand <code>FOOTER_WRITTEN</code> überführt. */
		public void writeFooter() {
			state = FOOTER_WRITTEN;
		}

		/**
		 * Gibt "leeren" Kopf aus: <code>state</code> wird lediglich in den Zustand <code>HEADER_WRITTEN</code> überführt.
		 *
		 * @param args String[] mit den Kommandozeilenparametern
		 */
		public void writeHeader(String[] args) {
			state = HEADER_WRITTEN;
		}
	}

	/** Gibt bei jeder Aktualisierung die Anzahl der enthaltenen Datensätze aus. */
	private class UpdateSummaryProtocoller extends SilentProtocoller {

		/** Einziger Konstruktor. */
		protected UpdateSummaryProtocoller() {
		}

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird und die Anzahl der
		 * erhaltenen Datensätze ausgibt.
		 *
		 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen.
		 */
		public void update(ResultData[] results) {
			if(state == HEADER_WRITTEN) {
				super.update(results);
				getProtocolFileWriter()
						.println(
								_dateFormat.format(new Date(System.currentTimeMillis())) + ": " + updateCount + ". " + _actionText + ": " + results.length
								+ " von bisher insgesamt " + resultDataCount + " Datensätzen"
						);
				//debug.info("free/total: " + (Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0) + "MB/" + (Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0) + "MB");
				debug.finer("Speicherbedarf", _memoryUsage);
			}
		}
	}

	/** Gibt bei jeder Aktualisierung zusätzlich die Köpfe der enthaltenen Datensätze aus. */
	private class HeaderProtocoller extends UpdateSummaryProtocoller {

		/** Gibt an, ob Zeilenumbruch in der Kopfzeile zugelassen ist */
		protected final boolean breakHeader;

		/** Erzeugt ein neues Objekt der Klasse <code>HeaderProtocoller</code> mit <code>breakHeader == false</code> */
		public HeaderProtocoller() {
			breakHeader = false;
		}

		/**
		 * Erzeugt ein neues Objekt der Klasse <code>HeaderProtocoller</code>, bei dem ausgwählt werden kann, ob Zeilenumbruch in Kopfzeile erlaubt ist
		 *
		 * @param	breakHeader	boolean mit der Zeilenumbruch-erlaubt-Eigenschaft
		 */
		public HeaderProtocoller(boolean breakHeader) {
			this.breakHeader = breakHeader;
		}

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird und die Köpfe der
		 * erhaltenen Datensätze ausgibt.
		 *
		 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen.
		 */
		public void update(ResultData[] results) {
			if(state == HEADER_WRITTEN) {
				super.update(results);
				for(int i = 0; i < results.length; ++i) {
					try {
						update(results[i]);
					}
					catch(Exception e) {
						debug.error(
								"Fehler beim Zugriff auf den Datensatz: " + e.getMessage()
						);
					}
				}
			}
		}

		/**
		 * Aktualisierungsmethode, für einen Datensatz
		 *
		 * @param	result	{@link ResultData} mit dem zu verarbeitenden Ergebnisdatensatz.
		 */
		protected void update(ResultData result) {

			/* Maske für den Fehlercode */
			final long MASK_ERROR_CODE = 0x0000000000000003L;

			/* Maske für die laufende Nummer des Datentyps */
			final long MASK_INDEX = 0x00000000fffffffcL;

			/* Verschiebeversatz für die laufende Nummer des Datentyps */
			final long SHIFT_INDEX = 2;

			/* Verschiebeversatz für den Zeitstempel */
			final long SHIFT_TIME_STAMP = 32;

			/* Beschreibung der Daten */
			DataDescription description = result.getDataDescription();

			/* Laufende Nummer des Datensatzes */
			long index;

			/* Erster Teil der laufenden Nummer: Zeitstempel */
			long indexTimeStamp;

			/*
						 * Zweiter Teil der laufenden Nummer: laufende Nummer des
						 * Datentyps
						 */
			long indexIndex;

			/* Dritter Teil der laufenden Nummer: Fehlerkennung */
			long indexErrorCode;

			/* System-Objekt der erthaltenen Daten */
			SystemObject object = result.getObject();

			/*Aspekt dieser Datenbeschreibung <code>description</code> */
			Aspect aspect = description.getAspect();

			/*
						 * Attributgruppe dieser Datenbeschreibung
						 * <code>description</code>
						 */
			AttributeGroup attributeGroup = description.getAttributeGroup();

			/*
						 * Simulationsvariante dieser Datenbeschreibung
						 * <code>description</code>
						 */
			int simulationVariant = description.getSimulationVariant();

			getProtocolFileWriter().print(indentationPrefix);
			getProtocolFileWriter().print(
					dateFormat
							.format(new Date(result.getDataTime()))
			);
			getProtocolFileWriter().print(" ");
			index = result.getDataIndex();
			indexTimeStamp = index >>> SHIFT_TIME_STAMP;
			indexIndex = (index & MASK_INDEX) >>> SHIFT_INDEX;
			indexErrorCode = index & MASK_ERROR_CODE;
			getProtocolFileWriter().print(indexTimeStamp);
			getProtocolFileWriter().print("#");
			getProtocolFileWriter().print(indexIndex);
			getProtocolFileWriter().print("#");
			getProtocolFileWriter().print(indexErrorCode);
			getProtocolFileWriter().print(" ");
			if(System.getProperty("de.bsvrz.pat.onlprot.object.toString", "false").equals("false")) {
				getProtocolFileWriter().print(object.getNameOrPidOrId());
			}
			else {
				getProtocolFileWriter().print(object);
			}
			getProtocolFileWriter().print(": ");

			if(breakHeader) {
				getProtocolFileWriter().println();
				getProtocolFileWriter().print(indentationPrefix);
				getProtocolFileWriter().print(indentationPrefix);
			}

			getProtocolFileWriter().print(attributeGroup.getNameOrPidOrId());
			getProtocolFileWriter().print(":");
			getProtocolFileWriter().print(aspect.getNameOrPidOrId());
			if(simulationVariant != DataDescription.NO_SIMULATION_VARIANT_SET) {
				getProtocolFileWriter().print(":");
				getProtocolFileWriter().print(simulationVariant);
			}
			getProtocolFileWriter().print(", ");
			if(!result.isSourceAvailable()) {
				getProtocolFileWriter().println("keine Quelle");
			}
			else if(!result.hasData()) {
				getProtocolFileWriter().println("keine Daten");
			}
			else {
				if(result.isDelayedData()) {
					getProtocolFileWriter().println("nachgelieferte Daten");
				}
				else {
					getProtocolFileWriter().println("Online Daten");
				}
			}
		}
	}

	/** Gibt bei jeder Aktualisierung zusätzlich die Attributwerte der enthaltenen Datensätze aus. */
	private class DataProtocoller extends HeaderProtocoller {

		/** Spalte, an der die Suffixe (Einheiten) der Attribute ausgerichtet werden */
		private static final int suffixPosition = 70;

		/** Spalte, an der die Werte der Attribute ausgerichtet werden */
		private static final int valuePosition = 60;

		/** Erzeugt ein neues Objekt der Klasse <code>DataProtocoller</code> */
		public DataProtocoller() {
			super(true);
		}

		/**
		 * Ausgabe einer Zeile.
		 *
		 * @param	data		Data: die auszugebenden Daten
		 * @param	indentLevel	int welches die Einrücktiefe angibt
		 */
		protected void print(Data data, int indentLevel) {
			StringBuffer text = new StringBuffer(80);
			for(int i = 0; i < indentLevel; ++i) {
				text.append(indentationPrefix);
			}
			text.append(data.getName()).append(": ");
			if(data.isPlain()) {
				try {
					Data.TextValue textValue = data.asTextValue();
					String value = textValue.getValueText();
					int kommaPosition = value.lastIndexOf(',');
					if(kommaPosition < 0) {
						kommaPosition = value.length();
					}
					int fillCount = valuePosition - text.length() - kommaPosition;
					while(fillCount-- > 0) {
						text.append(" ");
					}
					text.append(value).append(" ");
					String suffix = textValue.getSuffixText();
					if(!suffix.equals("")) {
						fillCount = suffixPosition - text.length();
						while(fillCount-- > 0) {
							text.append(" ");
						}
						text.append(suffix);
					}
					getProtocolFileWriter().println(text.toString());
				}
				catch(Exception e) {
					debug.error(
							text.append("<<").append(e.getMessage())
									.append(">>").toString()
					);
				}
			}
			else {
				getProtocolFileWriter().println(text.toString());
				Iterator i = data.iterator();
				++indentLevel;
				while(i.hasNext()) print((Data)i.next(), indentLevel);
			}
		}

		/**
		 * Aktualisierungsmethode, für einen Datensatz. Zusätzlich zu den Köpfen der erhaltenen Datensätze werden hier auch die Attibutwerte ausgegeben.
		 *
		 * @param	result	{@link ResultData} mit dem zu verarbeitenden Ergebnisdatensatz.
		 */
		protected void update(ResultData result) {
			super.update(result);
			if(result.hasData()) {
				Data data = result.getData();
				Iterator i = data.iterator();
				while(i.hasNext()) {
					print((Data)i.next(), breakHeader ? 3 : 2);
				}
			}
		}
	}

	/** Gibt die Telegramme in einem XML-Format aus. */
	private class XmlProtocoller implements ExtendedProtocollerInterface {

		/** Konstante für das Tag für Aufrufparameter */
		private static final String ARGUMENTS_TAG = "aufrufparameter";

		/** Konstante für das Feld-Tag */
		private static final String ARRAY_TAG = "feld";

		/** Konstante für das Aspekt-Tag */
		private static final String ASPECT_TAG = "aspekt";

		/** Konstante für das Attribut-Tag */
		private static final String ATTRIBUTE_TAG = "attribut";

		/** Konstante für das Attributgruppen-Tag */
		private static final String ATTRIBUTEGROUP_TAG = "attributgruppe";

		/** Konstante für das Block-Tag */
		private static final String BLOCK_TAG = "block";

		/** Konstante für das Körper-Tag */
		private static final String BODY_TAG = "koerper";

		/** Standardlänge eines <code>StringBuffer</code>s */
		private static final int BUFFER_LENGTH = 80;

		/** Konstante für das Nachgeliefert-Tag */
		private static final String DELAYED_TAG = "nachgeliefert";

		/** Beschreibt Dokumenttyp */
		private static final String DOCTYPE_INFO = "<!DOCTYPE protokoll SYSTEM \"protokollV3.dtd\">";

		/** Konstante für das Kopf-Tag */
		private static final String HEAD_TAG = "kopf";

		/** Konstante für den ID-Text. */
		private static final String ID_IS_QUOTES = "id=\"";

		/** Konstante für den Längen-Text. */
		private static final String LENGTH_IS_QUOTES = "länge=\"";

		/** Konstante für das Listen-Tag */
		private static final String LIST_TAG = "liste";

		/** Konstante für den Name-Text. */
		private static final String NAME_IS_QUOTES = "name=\"";

		/** Konstante für das Tag, welches als Kennung für keine Daten dient */
		private static final String NO_DATA_TAG = "keineDaten";

		/** Konstante für das Tag, welches als Kennung für keine Quelle dient */
		private static final String NO_SOURCE_TAG = "keineQuelle";

		/** Konstante für das Objekt-Tag */
		private static final String OBJECT_TAG = "objekt";

		/** Konstante für den PID-Text. */
		private static final String PID_IS_QUOTES = "pid=\"";

		/** Konstante für das Protokoll-Tag */
		private static final String PROTOCOL_TAG = "protokoll";

		/** Konstante für den Empfangszeitstempel-Text. */
		private static final String RCV_TIME_STAMP_IS_QUOTES = "empfangszeit=\"";

		/** Konstante für das Datensatz-Tag */
		private static final String RECORD_TAG = "datensatz";

		/** Konstante für das Simulationsvarianten-Tag */
		private static final String SIM_VARIANT_TAG = "simulationsvariante";

		/** Konstante für das Start-Tag */
		private static final String START_TIME_STAMP_TAG = "start";

		/** Konstante für das Zeitstempel-Tag */
		private static final String TIME_STAMP_TAG = "zeit";

		/** Konstante für den Einheiten-Text. */
		private static final String UNIT_IS_QUOTES = "einheit=\"";

		/** Konstante für den Wert-Text. */
		private static final String VALUE_IS = "wert=";

		/** Konstante für den Wert-Text. */
		private static final String VALUE_IS_QUOTES = VALUE_IS + "\"";

		/** XML-Versions-Info der erzeugten XML-Ausgabe */
		private static final String XML_VERSION_INFO = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

		/** Einrücktiefe */
		private int indentLevel = 0;

		/**
		 * Zustandskennung; wichtig bei Abbruch der Applikation: Während ein Block von Datentelegrammen geschrieben wird, darf die Applikation nicht abgebrochen
		 * werden, da die sonst resultierende XML-Datei fehlerhaft aufgebaut würde (<code>&lt;/block&gt;</code> würde fehlen).
		 */
		private boolean isInBlock = false;

		/** Erzeugt ein neues Objekt der Klasse <code>XmlProtocoller</code> */
		public XmlProtocoller() {
		}

		/**
		 * Aktualisierungsmethode, die nach Empfang eines angemeldeten Datensatzes von den Datenverteiler-Applikationsfunktionen aufgerufen wird und die erhaltenen
		 * Datensätze im XML-Format ausgibt.
		 *
		 * @param	results	{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen.
		 */
		public void update(ResultData[] results) {
			long currentTime = System.currentTimeMillis();
			if(state == HEADER_WRITTEN) {
				writeBlock(results, currentTime);
			}
		}

		/** Gibt einen Fuß, bestehend aus schließendem {@link #BODY_TAG} und {@link #PROTOCOL_TAG}, aus */
		public void writeFooter() {
			try {
				while(isInBlock) {
					Thread.sleep(100);
				}
				closeTag(BODY_TAG);
				closeTag(PROTOCOL_TAG);
				state = FOOTER_WRITTEN;
			}
			catch(Exception e) {
				debug.error("<<" + e.getMessage() + ">>");
			}
		}

		public void writeHeader(String[] args, long startTime) {
			getProtocolFileWriter().println(XML_VERSION_INFO);
			getProtocolFileWriter().println(DOCTYPE_INFO);
			getProtocolFileWriter().println();

			openTag(PROTOCOL_TAG);
			openTag(HEAD_TAG);
			openAndCloseTag(
					START_TIME_STAMP_TAG, VALUE_IS_QUOTES + dateFormat.format(new Date(startTime)) + "\""
			);
			String argumentString = "";
			for(int i = 0; i < args.length; i++) {
				argumentString = argumentString + args[i] + " ";
			}
			openAndCloseTag(ARGUMENTS_TAG, VALUE_IS_QUOTES + xmlText(argumentString) + "\"");
			closeTag(HEAD_TAG);
			getProtocolFileWriter().println();

			openTag(BODY_TAG);
			state = HEADER_WRITTEN;
		}
		/**
		 * Gibt einen Kopf aus, der Informationen über die XML-Version und den verwendeten Dokumenttyp ausgibt und den Kopf schreibt
		 *
		 * @param args String[] mit den Kommandozeilenparametern
		 */
		public void writeHeader(String[] args) {
			writeHeader(args, System.currentTimeMillis());
		}

		/**
		 * Schließendes XML-Tag ausgeben. Die übergebene Bezeichnung wird mit spitzen Klammern umgeben.
		 *
		 * @param	tag	String, der Bezeichnung des Tags enthält
		 */
		protected void closeTag(String tag) {
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			indentLevel = indentLevel - 1;
			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			getProtocolFileWriter().print(output);
			writeTag("/" + tag);
			getProtocolFileWriter().println();
		}

		/**
		 * XML-Tag schreiben und mit <i>/></i> abschließen.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 */
		protected void openAndCloseTag(String tag) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			output.append("<").append(tag).append("/>");
			getProtocolFileWriter().println(output);
		}

		/**
		 * Mit Attribut versehenes XML-Tag schreiben und mit <i>/></i> abschließen.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 * @param	value	String mit Wert des Attributs
		 */
		protected void openAndCloseTag(String tag, String value) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			output.append("<").append(tag).append(" ").append(value).append("/>");
			getProtocolFileWriter().println(output);
		}

		/**
		 * Mit Attribut und Kommentar versehenes XML-Tag schreiben und mit <i>/></i> abschließen.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 * @param	value	String mit Wert des Attributs
		 * @param	comment	String mit dem Kommentar, der hinter dem Tag ausgegeben wird
		 */
		protected void openAndCloseTag(String tag, String value, String comment) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			output.append("<").append(tag).append(" ").append(value).append("/>");
			getProtocolFileWriter().print(output);
			writeComment(comment);
		}

		/**
		 * Mit besonders benanntem Attribut und Kommentar versehenes XML-Tag schreiben und mit <i>/></i> abschließen.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 * @param	name	String mit Name des Attributs
		 * @param	value	String mit Wert des Attributs
		 * @param	comment	String mit dem Kommentar, der hinter dem Tag ausgegeben wird
		 */
		protected void openAndCloseTag(String tag, String name, String value, String comment) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			output.append("<").append(tag).append(" ").append(name).append(" ").append(value).append("/>");
			getProtocolFileWriter().print(output);
			writeComment(comment);
		}

		/**
		 * Öffnendes XML-Tag ausgeben. Die übergebene Bezeichnung wird mit spitzen Klammern umgeben.
		 *
		 * @param	tag	String, der Bezeichnung des Tags enthält
		 */
		protected void openTag(String tag) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			for(int i = 0; i < indentLevel; i++) {
				output.append(indentationPrefix);
			}
			getProtocolFileWriter().print(output);
			writeTag(tag);
			getProtocolFileWriter().println();
			indentLevel = indentLevel + 1;
		}

		/**
		 * Mit Attribut versehenes öffnendes XML-Tag ausgeben.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 * @param	value	String mit Wert des Attributs
		 */
		protected void openTag(String tag, String value) {
			openTag(tag + " " + value);
		}

		/**
		 * Mit Attribut und Länge versehenes öffnendes XML-Tag ausgeben.
		 *
		 * @param	tag		String, der Bezeichnung des Tags enthält
		 * @param	value	String mit Wert des Attributs
		 * @param	length	String mit einer Längeninformation, der hinter dem Tag ausgegeben wird
		 */
		protected void openTag(String tag, String value, String length) {
			openTag(tag + " " + value + " " + length);
		}

		/**
		 * Einen Datensatz ausgeben. Grundsätzlich wird unterschieden zwischen "einfachen" Daten (Eigenschaft <code>isPlain</code>) und im Gegensatz dazu Feldern und
		 * Listen. Zur Ausgabe der letzteren beiden ruft sich diese Methode rekursiv wieder auf.
		 *
		 * @param	data	{@link Data} mit dem Telegramm
		 */
		protected void printData(Data data) {
			if(data.isPlain()) {
				try {
					/* Kommentar aus der Konfiguration zu diesem Datum */
					String comment = data.asTextValue().getSuffixText();

					/* Name dieses Datums */
					String name = data.getName();

					/* Der konkrete Wert dieses Datums */
					String value = data.asTextValue().getValueText();

					openAndCloseTag(
							ATTRIBUTE_TAG, NAME_IS_QUOTES + xmlText(name) + "\"", VALUE_IS_QUOTES + xmlText(value) + "\"", UNIT_IS_QUOTES + xmlText(comment) + "\""
					);
				}
				catch(Exception e) {
					debug.error("<<" + e.getMessage() + ">>");
				}
			}
			else if(data.isArray()) {
				try {

					/* Iterator zum Durchlaufen des Feldes */
					Iterator i = data.iterator();

					/* Anzahl der Einträge in diesem Feld */
					int length = data.asArray().getLength();

					/* Name dieses Datums */
					String name = data.getName();

					openTag(
							ARRAY_TAG, NAME_IS_QUOTES + xmlText(name) + "\"", LENGTH_IS_QUOTES + length + "\""
					);
					while(i.hasNext()) {
						printData((Data)i.next());
					}
					closeTag(ARRAY_TAG);
				}
				catch(Exception e) {
					debug.error("<<" + e.getMessage() + ">>");
				}
			}
			else {
				try {

					/* Iterator zum Durchlaufen der Liste */
					Iterator i = data.iterator();

					/* Name dieses Datums */
					String name = data.getName();

					openTag(LIST_TAG, NAME_IS_QUOTES + xmlText(name) + "\"");
					while(i.hasNext()) {
						printData((Data)i.next());
					}
					closeTag(LIST_TAG);
				}
				catch(Exception e) {
					debug.error("<<" + e.getMessage() + ">>");
				}
			}
		}

		/**
		 * Gibt Informationen über die Art der Daten aus. Dies sind im einzelnen Informationen über
		 * <p>
		 * <ul> <li> das Objekt <li> die Attributgruppe <li> den Aspekt <li> die Simulationsvariante <li> den (<em>Sende</em>-)Zeitstempel </ul>
		 *
		 * @param	telegram	{@link ResultData} mit dem Datentelegramm
		 */
		protected void printDataKind(ResultData telegram) {
			String comment = "";
			long id = 0;
			String name = "";
			String pid = "";
			String value = "";

			openTag(RECORD_TAG);
			SystemObject object = telegram.getObject();
			name = object.getName();
			pid = object.getPid();
			id = object.getId();
			if(!pid.equals("")) {
				value = PID_IS_QUOTES + xmlText(pid) + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
				else {
					comment = ID_IS_QUOTES + id + "\"";
				}
			}
			else {
				value = ID_IS_QUOTES + id + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
			}
			openAndCloseTag(OBJECT_TAG, value, comment);

			DataDescription description = telegram.getDataDescription();
			AttributeGroup attributeGroup = description.getAttributeGroup();
			name = attributeGroup.getName();
			pid = attributeGroup.getPid();
			id = attributeGroup.getId();
			if(!pid.equals("")) {
				value = PID_IS_QUOTES + xmlText(pid) + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
				else {
					comment = ID_IS_QUOTES + id + "\"";
				}
			}
			else {
				value = ID_IS_QUOTES + id + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
			}
			openAndCloseTag(ATTRIBUTEGROUP_TAG, value, comment);

			Aspect aspect = description.getAspect();
			name = aspect.getName();
			pid = aspect.getPid();
			id = aspect.getId();
			if(!pid.equals("")) {
				value = PID_IS_QUOTES + xmlText(pid) + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
				else {
					comment = ID_IS_QUOTES + id + "\"";
				}
			}
			else {
				value = ID_IS_QUOTES + id + "\"";
				if(!name.equals("")) {
					comment = NAME_IS_QUOTES + xmlText(name) + "\"";
				}
			}
			openAndCloseTag(ASPECT_TAG, value, comment);

			int simulationVariant = description.getSimulationVariant();
			if(simulationVariant != DataDescription.NO_SIMULATION_VARIANT_SET) {
				openAndCloseTag(
						SIM_VARIANT_TAG, VALUE_IS_QUOTES + simulationVariant + "\""
				);
			}

			openAndCloseTag(
					TIME_STAMP_TAG, VALUE_IS_QUOTES + dateFormat.format(new Date(telegram.getDataTime())) + "\""
			);

			if(telegram.isDelayedData()) {
				openAndCloseTag(DELAYED_TAG, VALUE_IS_QUOTES + "ja" + "\"");
			}
			else {
				openAndCloseTag(DELAYED_TAG, VALUE_IS_QUOTES + "nein" + "\"");
			}
		}

		private String xmlText(final String text) {
			return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
		}

		/**
		 * Block, bestehend aus mehreren Telegrammen, ausgeben.
		 *
		 * @param	results		{@link ResultData}[] mit den empfangenen Ergebnisdatensätzen
		 * @param	currentTime	long mit der aktuellen Uhrzeit. Der Block wird in der Ausgabe damit versehen.
		 */
		public synchronized void writeBlock(ResultData[] results, long currentTime) {
			isInBlock = true;
			openTag(
					BLOCK_TAG, RCV_TIME_STAMP_IS_QUOTES + dateFormat.format(new Date(currentTime)) + "\""
			);
			for(int i = 0; i < results.length; i++) {
				try {
					writeTelegram(results[i]);
				}
				catch(Exception e) {
					debug.error(
							"Fehler beim Zugriff auf den Datensatz: " + e.getMessage()
					);
				}
			}
			closeTag(BLOCK_TAG);
			isInBlock = false;
		}

		/**
		 * Zeichenkette als XML-Kommentar ausgeben. Der XML-Kommentar wird durch ein Tabulatorzeichen eingeleitet.
		 *
		 * @param	comment	String mit dem Kommentar
		 */
		protected void writeComment(String comment) {
			getProtocolFileWriter().println("\t<!-- " + comment + " -->");
		}

		/**
		 * XML-Tag auf Ausgabe-Stream schreiben
		 *
		 * @param	tag	String, der zwischen spitzen Klammern auszugeben ist
		 */
		protected void writeTag(String tag) {

			/* Puffer, in dem die Ausgabe zusammengestellt wird */
			StringBuffer output = new StringBuffer(BUFFER_LENGTH);

			output.append("<").append(tag).append(">");
			getProtocolFileWriter().print(output);
		}

		/**
		 * Mit Attribut versehenes XML-Tag auf Ausgabe-Stream schreiben
		 *
		 * @param	tag		String, der zwischen spitzen Klammern auszugeben ist
		 * @param	value	String mit Wert des Attributs
		 */
		protected void writeTag(String tag, String value) {

			/* Auszugebende Zeichenkette */
			String output = tag + " " + value;

			writeTag(output);
		}

		/**
		 * Mit Attribut und Zähler versehenes XML-Tag auf Ausgabe-Stream schreiben
		 *
		 * @param	tag		String, der zwischen spitzen Klammern auszugeben ist
		 * @param	value	String mit Wert des Attributs
		 * @param	counter	String mit dem Zählerwert
		 */
		protected void writeTag(String tag, String value, String counter) {

			/* Auszugebende Zeichenkette */
			String output = tag + " " + value + " " + counter;

			writeTag(output);
		}

		/**
		 * Ausgabe eines Datentelegramms. Ein Telegramm besteht aus Informationen über die Art der Daten und über die eigentlichen Daten selbst.
		 *
		 * @param	telegram	{@link de.bsvrz.dav.daf.main.ResultData} mit dem Datentelegramm
		 */
		protected void writeTelegram(ResultData telegram) {
			printDataKind(telegram);

			if(!telegram.isSourceAvailable()) {
				openAndCloseTag(NO_SOURCE_TAG);
			}
			else if(!telegram.hasData()) {
				openAndCloseTag(NO_DATA_TAG);
			}
			else {
				try {

					/* Datenteil des Telegramms */
					Data data = telegram.getData();

					/* Iterator zum durchlaufen der Attribute des Telegramms */
					Iterator i = data.iterator();

					while(i.hasNext()) {
						printData((Data)i.next());
					}
				}
				catch(Exception e) {
					debug.error("<<" + e.getMessage() + ">>");
				}
			}
			closeTag(RECORD_TAG);
		}
	}
}
