/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2003 by Kappich+Kniß Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.datgen.
 * 
 * de.bsvrz.pat.datgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.datgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.datgen.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.datgen.generator.xmlParser;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.pat.datgen.generator.main.DataGenerator;
import de.bsvrz.pat.datgen.generator.main.SendInterface;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/**
 * Klasse zur Verarbeitung unserer XML-Daten-Dateien
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class XmlFormatReader {

	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/** Bei Wiedergabe einer XML-Datei wird die Originalzeit in der Datei beibehalten */
	private static final int ORIGINAL_TIME = 0;

	/** Bei Wiedergabe einer XML-Datei wird die Originalzeit in der Datei angepaßt: Das zeitliche Verhalten wird in die Jetztzeit verschoben. */
	private static final int ADJUST_TIME = 1;

	/** Basis-URI für die DTD */
	private String baseUri;

	/** Die Parser-<i>Factory</i> */
	private SAXParserFactory factory;

	/** Handler für SAX-Ereignisse */
	private DefaultHandler parserHandler;

	/** Name der Eingabedatei */
	private String inputFile;

	/** Eingabe-<i>Stream</i> für die Protokolle */
	private BufferedInputStream inputFileReader;

	/** Zeigt an, ober der Parser validierend sein soll oder nicht */
	private boolean isValidating = true;

	/** Der XML-Parser */
	private SAXParser parser;

	/** Handler für Eingangs-Validitäts-Prüfung */
	private DefaultHandler validityHandler;

	/** Der Parser, der zum Verwenden der Validität der XML-Datei verwendet wird */
	private SAXParser validityParser;

	/**
	 * Zeigt an, ob bei Wiedergabe einer XML-Datei die Originalzeit in der Datei beibehalten wird <br> <code>ORIGINAL_TIME</code>: wird beibehalten<br>
	 * <code>ADJUST_TIME</code>: wird nicht beibehalten
	 */
	private int timeStampOption = ORIGINAL_TIME;

	/** Liste der gülten Werte für den <code>-zeitstempel</code>-Parameter */
	private ArgumentList.ValueSelection validTimeStampOptions = new ArgumentList.ValueSelection();

	/**
	 * Erzeugt ein neues Objekt der Klasse <code>XmlFormatReader</code>
	 *
	 * @param argumentList {@link ArgumentList} der noch nicht ausgewerteten Aufrufparameter der Applikation
	 *
	 * @throws Exception wenn beim Anlegen des Objekts ein Problem auftritt
	 */
	public XmlFormatReader(ArgumentList argumentList) throws Exception {

		validTimeStampOptions.add("übernehmen").alias("uebernehmen").alias("u")
				.ignoreCase().convertTo(ORIGINAL_TIME)
				.purpose("Wiedergabe des Protokolls geschieht mit den ursprünglichen Zeiten");
		validTimeStampOptions.add("anpassen").alias("a").ignoreCase()
				.convertTo(ADJUST_TIME)
				.purpose("Wiedergabe des Protokolls wird in die Jetztzeit verschoben");

		ArgumentList.Argument dummy = argumentList.fetchArgument("-eingabe=");
		inputFile = dummy.getValue();
		if(dummy.hasValue() && (inputFile.length() > 0)) {
			inputFileReader = new BufferedInputStream(new FileInputStream(inputFile));
		}
		else {
			debug.finer("System.in wird Eingabe-Stream");
			inputFileReader = new BufferedInputStream(System.in);
		}
		if((argumentList.hasUnusedArguments()) && (argumentList.hasArgument("-basisUri="))) {
			baseUri = argumentList.fetchArgument("-basisUri=").asString();
		}
		timeStampOption = argumentList.fetchArgument("-zeitstempel=uebernehmen")
				.asValueCase(validTimeStampOptions)
				.intValue();
		isValidating = argumentList
				.fetchArgument("-validieren=ja").booleanValue();
	}

	/**
	 * Validitäts-Check der XML-Datei: Bevor die Daten in der XML-Datei gelesen werden, wird diese auf formale Korrektheit geprüft.
	 *
	 * @throws IOException  wenn beim Lesen der Datei o. ä. ein Fehler auftritt
	 * @throws SAXException wenn ein SAX-spezifisches Problem auftritt.
	 */
	public void checkValidity() throws IOException, SAXException {
		debug.fine("Validitäts-Check");
		if((inputFile.length() > 0) && isValidating) {
			((SaxHandler)parserHandler).setIsInVerify(true);
			debug.finer("XmlFormatReader: Vor validityParser.parse. inputFile = \"" + inputFile + "\"");
			if(baseUri != null) {
				validityParser.parse(inputFileReader, parserHandler, baseUri);
			}
			else {
				validityParser.parse(new File(inputFile), parserHandler);
			}
		}
		else {
			((SaxHandler)parserHandler).setIsInVerify(false);
		}
	}

	/**
	 * SAX-Handler mit notwendigen Informationen versorgen
	 *
	 * @param dm      Das Datenmodell, auf dem die Applikation arbeitet.
	 * @param cdi     Die Verbindung zum DaV
	 * @param sq      Sende-Queue
	 * @param objects Liste der Objekte, die gesendet werden sollen (d. h. in dieser Liste <b>nicht</B> angegebene Objekte werden in der XML-Datei überlesen)
	 * @param dg      Der aufrufende Datengenerator
	 */
	public void initSaxHandlers(
			DataGenerator dg, DataModel dm, ClientDavInterface cdi, SendInterface sq, List objects
	) {
		try {
			factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(isValidating);
			parser = factory.newSAXParser();
			validityParser = factory.newSAXParser();
			parserHandler = new SaxHandler();
			((SaxHandler)parserHandler).setDataGenerator(dg);
			((SaxHandler)parserHandler).setDataModel(dm);
			((SaxHandler)parserHandler).setConnection(cdi);
			((SaxHandler)parserHandler).setSendQueue(sq);
			((SaxHandler)parserHandler).setRequestedObjects(objects);
			((SaxHandler)parserHandler).setTimeStampOption(timeStampOption);
			validityHandler = new ValidityChecker();
		}
		catch(Exception e) {
			debug.error("Fehler: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * XML-Datei parsen.
	 *
	 * @throws IOException  wenn beim Lesen der Datei o. ä. ein Fehler auftritt
	 * @throws SAXException wenn ein SAX-spezifisches Problem auftritt.
	 */
	public void parse() throws IOException, SAXException {
		debug.fine("Parser");
		if(baseUri != null) {
			parser.parse(inputFileReader, parserHandler, baseUri);
		}
		else {
			if(inputFile.length() > 0) {
				parser.parse(new File(inputFile), parserHandler);
				inputFileReader.close();
			}
			else {
				parser.parse(inputFileReader, parserHandler);
			}
		}
	}

	public void setIsInVerify(boolean iiv) {
		((SaxHandler)parserHandler).setIsInVerify(iiv);
	}
}
