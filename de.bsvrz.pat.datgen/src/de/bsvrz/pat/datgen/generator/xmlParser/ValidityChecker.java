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

import de.bsvrz.sys.funclib.debug.Debug;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Klasse zum Prüfen der Validität einer XML-Daten-Datei.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ValidityChecker extends DefaultHandler {

	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/** Initialisierungszustand */
	public static final int INIT = 0;

	/** Parser befindet sich innerhalb eines Datensatzes */
	public static final int IN_RECORD = 1;

	/** Objekt wird gelesen */
	public static final int OBJECT_READ = 2;

	/** Attributgruppe wird gelesen */
	public static final int ATTRIBUTEGROUP_READ = 2;

	/** Aspekt wird gelesen */
	public static final int ASPECT_READ = 2;

	/** Zustand des Parsers */
	public static int state = INIT;

	/** Creates a new instance of SaxHandler */
	public ValidityChecker() {
		super();
	}

	/** @throws SAXException bei Problemen */
	public void skippedEntity(String name) throws SAXException {
		super.skippedEntity(name);
		debug.warning("SAXHandler.skippedEntity: " + name);
	}

	/**
	 * Schwerer Fehler
	 *
	 * @param e Den fatalen Fehler auslösende Exception
	 *
	 * @throws SAXException bei Problemen
	 */
	public void fatalError(SAXParseException e) throws SAXException {
	}

	/**
	 * Fehler
	 *
	 * @param e Den Fehler auslösende Exception
	 *
	 * @throws SAXException bei Problemen
	 */
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
		debug.error("SAXHandler.error: " + e);
	}

	/**
	 * Warnung
	 *
	 * @param e Die Warnung auslösende Exception
	 *
	 * @throws SAXException bei Problemen
	 */
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
		debug.warning("SAXHandler.warning: " + e);
	}

	/**
	 * Callback-Methode für Start des XML-Dokuments
	 *
	 * @throws SAXException bei Problemen
	 */
	public void startDocument() throws SAXException {
	}

	/**
	 * Ende des Dokuments erreicht
	 *
	 * @throws SAXException bei Problemen
	 */
	public void endDocument() throws SAXException {
	}

	/**
	 * Start-Tag gefunden.
	 *
	 * @param uri        URI des Tags
	 * @param localName  Bezeichner
	 * @param qName      Qualifizierter Name
	 * @param attributes Liste der Attribute des Tags
	 *
	 * @throws SAXException bei Problemen
	 */
	public void startElement(
			String uri, String localName, String qName, Attributes attributes
	) throws SAXException {
		if(localName.equals("Datensatz")) {
			state = IN_RECORD;
		}
		else if(localName.equals("Objekt") && state == IN_RECORD) {
		}
	}

	/**
	 * Ende-Tag gefunden.
	 *
	 * @param uri       URI des Tags
	 * @param localName Bezeichner
	 * @param qName     Qualifizierter Name
	 *
	 * @throws SAXException bei Problemen
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(localName.equals("Datensatz")) {
			state = INIT;
		}
	}

	/**
	 * Callback-Methode für die Verarbeitung von Characters.
	 *
	 * @param ch     Feld mit den erparsten <code>char</CODE>s
	 * @param start  Startposition
	 * @param length Länge des Feldes
	 *
	 * @throws SAXException bei Problemen
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
	}
}
