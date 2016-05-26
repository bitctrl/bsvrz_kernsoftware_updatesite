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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer.DataKindDeterminer;
import de.bsvrz.pat.datgen.generator.main.DataGenerator;
import de.bsvrz.pat.datgen.generator.main.SendInterface;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.net.URL;

/**
 * SAX-Handler für den Zugriff auf die XML-Datei.
 *
 * Datensätze können Felder und Listen (im folgenden <i>NonPlains</i> genannt)
 * enthalten. Würde man die XML-Datei "von Hand" verarbeiten, so hätte eine
 * Methode <code>datenVerarbeiten</code> wohl einen rekursiven Aufbau (vgl
 * {@link DataGenerator.DataCycler#setRandomData}:
 * <p>
 * <pre><code>
 * IF (tag == "attribut") THEN
 *		// Wert aus XML-Datei lesen
 * ELSE IF (tag == "feld") THEN
 *		FOR ALL (Feldelemente) DO
 *			datenVerarbeiten();
 *		ENDFOR
 * ELSE IF (tag == "liste") THEN
 *		FOR EACH (listenelement) DO
 *			datenVerarbeiten();
 *		ENDFOR
 * ENDIF
 * </code></pre>
 * <p>
 *Durch die Art des Einlesens der XML-Datei mit dem SAX-Parser (jedes Lesen
 * eines Start-/Ende-Tags löst Aufruf einer der hier zu findenden
 * Call-Back-Methoden aus), ist das rekursive Abarbeiten nicht möglich ==&gt;
 * Über einen Stack wird erreicht, daß die Zuordnung der eingelesenen Attribute
 * zu den richtigen Einträgen in der rekursiven Datenstruktur gelingt:
 * <p>
 * Ein Datensatz vom Typ {@link Data} wird "lazy" erzeugt, d. h. enthält er ein
 * <i>NonPlain</i>, so wird beim Erzeugen eines {@link Data} lediglich eine
 * "flacher" Datenstruktur erzeugt, in die wiederum {@link Data}s für die
 * <i>NonPlain</i>s eingesetzt werden, die wiederum {@link Data}s für
 * <i>NonPlain</i>s enthalten können usw.
 * <p>
 * Die o. g. zunächst erzeugte flache Datenstruktur ist {@link #data}. Jedes
 * weitere für <i>NonPlain</i>s erzeugte {@link Data} wird auf einen Stack
 * gelegt. Durch {@link #readTop} wird nun erreicht, daß beim Einlesen eines
 * Attributs immer das {@link Data}-Element gefüllt wird, welches sich gerade in
 * Bearbeitung befindet, d. h. der Stack bildet das "Gedächtnis" des Parsers und
 * "aus Sicht der Attribute" ist nur dieses {@link Data}-Element bekannt, wie es
 * auch in einer rekursiven Methode der Fall wäre.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SaxHandler extends DefaultHandler {

	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/** Konstante: Man befindet sich vor dem "Protokoll"-Tag */
	private static final int BEFORE_PROTOCOL = 0;

	/** Konstante: Man befindet sich im "Protokoll"-Tag. */
	private static final int IN_PROTOCOL = 1;

	/** Konstante: Man befindet sich im "Kopf"-Tag */
	private static final int IN_HEADER = 2;

	/** Konstante: Man befindet sich hinter dem "Kopf"-Tag. */
	private static final int NACH_HEADER = 3;

	/** Konstante: Man befindet sich im "Koerper"-Tag. */
	private static final int IN_BODY = 4;

	/** Konstante: Man befindet sich im "Block"-Tag. */
	private static final int IN_BLOCK = 5;

	/** Konstante: Man befindet sich im "Datensatz"-Tag. */
	private static final int IN_RECORD = 6;

	/** Konstante: Man befindet sich hinter dem "Protokoll"-Tag. */
	private static final int BEHIND_PROTOCOL = 7;

	/**
	 * Aufrufender Datengenerator
	 */
	private DataGenerator dataGenerator = null;

	/**
	 * Gibt an, ob beim Aufruf des {@link DataGenerator Datengenerators} Objekte
	 * angegeben wurden oder nicht
	 */
	private boolean hasObjects = true;

	/**
	 * Zeigt an, ob ein Datensatz als nachgesendet gekennzeichnet ist
	 */
	private boolean isDelayed = false;

	/**
	 * <code>true</code> wenn sich der XML-Parser im Verifizierungsschritt
	 * befindet.
	 */
	private boolean isInVerify = false;

	/** Das aus der XML-Datei gelesene Telegramm */
	private ResultData[] results = null;

	/** Gewünschter Sendezeitpunkt für einen Datenblock */
	private long blockTime = 0;

	/** Objekt */
	private SystemObject object = null;

	/** Datenbeschreibung (bestehend aus Attributgruppe, Aspekt und Simulationsvariante) */
	private DataDescription dd = null;

	/** Attributgruppe */
	private AttributeGroup atg = null;

	/** Aspekt */
	private Aspect asp = null;

	/** Simulationsvariante */
	private short simVariant = 0;
	/**
	 * Zeigt an, ob die Simulationsvariante explizit aus der XML-Datei gelesen
	 * wurde oder nicht
	 */
	private boolean simVariantExplicitlySet = false;

	/** Zeitstempel */
	private long timeStamp = 0;

	/** Datenelement */
	private Data data;

	/**
	 * Zeigt an, ob ein Telegramm Daten enthält
	 */
	private boolean hasNoData = false;

	/**
	 * Zeigt an, ob momentan Quelle für das entsprechende Objekt existiert
	 */
	private boolean hasNoSource = false;

	/** Zeitstempel des Blocks in der XML-Datei */
	private long historicalBlockTime = 0;
	/** Offset vom Block zur Startzeit in der XML-Datei */
	private long historicalOffset = 0;
	/** Historische Startzeit (d. h. Startzeit in der XML-Datei) */
	private long historicalStartTime = 0;
	/** Zeit des historischen Zeitstempels */
	private long historicalTimeStamp = 0;
	/** Zeitunterschied zwischen Startzeit in der XML-Datei und aktueller Startzeit der
	 * Applikation
	 */
	private long offset = 0;
	/** Startzeit der Applikation */
	private long startTime = 0;

	/** Status des SAX-Handlers */
	private int state = BEFORE_PROTOCOL;

	/** Anzahl der geparsten Datensätze */
	private int numberOfRecords = 0;
	/** Das benutzte Datenmodell der Applikation */
	private DataModel dataModel;
	/** Verbindung zum DaV */
	private static ClientDavInterface connection;
	/** Die Sende-Queue des SAX-Handlers */
	private SendInterface sendQueue;
	/** Datensatz eines der vom Anwender erwünschte Objekte, welcher in der XML-Datei
	 * gefunden wurde
	 */
	private LinkedList requestedResults = null;
	/** Stack für die Verarbeitung von geschachtelten {@link Data}-Objekten */
	private LinkedList itemStack = new LinkedList();
	/** Objekte, für die Daten aus der XML-Datei ausgelesen werden sollen */
	private List requestedObjects = null;
	/** Zeitstempel-Option: Zeitstempel der Daten aus der XML-Datei wird beim Einspielen
	 * der Daten entweder übernommen oder aber die Daten werden in die Jetztzeit
	 * verschoben.
	 */
	private int timeStampOption = 0;
	private int i=0;
	/** Creates a new instance of SaxHandler */
	public SaxHandler() {
		super();
		startTime = System.currentTimeMillis();
	}

	/** Callback-Methode für die Verarbeitung von Characters.
	 * @param ch Feld mit den erparsten <code>char</CODE>s
	 * @param start Startposition
	 * @param length Länge des Feldes
	 * @throws SAXException bei Problemen
	 */
	public void characters(char ch[], int start, int length)
	throws SAXException {
		debug.finer("characters.");
		super.characters(ch, start, length);
		debug.error("SAXHandler.characters; ch = " + ch + "; start = " + start + "; length = " + length);
		StringBuffer s = new StringBuffer(start + length + 10);
		s.append("Inhalt: ");
		for (int i = start; i <= start + length; i++) {
			s.append(ch[i]);
		}
		debug.error(s.toString());
	}

	/** Ende des Dokuments erreicht
	 * @throws SAXException bei Problemen
	 */
	public void endDocument()
	throws SAXException {
		debug.finer("endDocument.");
		super.endDocument();
		if (isInVerify) {
			isInVerify = false;
		}
	}

	/** Ende-Tag gefunden.
	 * @param uri URI des Tags
	 * @param localName Bezeichner
	 * @param qName Qualifizierter Name
	 * @throws SAXException bei Problemen
	 */
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		debug.finer("endElement " + localName);
		super.endElement(uri, localName, qName);
		switch (state) {
			case IN_HEADER:
				if (localName.equals("kopf")) {
					state = NACH_HEADER;
				}
				break;
			case IN_RECORD:
				if (localName.equals("feld")) {
					Data item = popItem();
				} else if (localName.equals("liste")) {

					/* <code>Data</code>-Liste ist abgearbeitet ==> Weg vom
					 * Stack!
					 */
					Data item = popItem();
				} else if (localName.equals("datensatz")) {
					state = IN_BLOCK;
					if ((!hasNoData) && (!hasNoSource)) {
						if (requestedObjects.contains(object)) {
							if (simVariantExplicitlySet) {
								dd = new DataDescription(atg, asp, simVariant);
							} else {
								dd = new DataDescription(atg, asp);
								simVariantExplicitlySet = false;
							}
							if (!isInVerify) {
								requestedResults.add(new ResultData(object, dd, timeStamp, data, isDelayed));
							}
						}
						numberOfRecords = numberOfRecords + 1;
					}
				} else if (localName.equals("attributgruppe")) {
					// Abgearbeitete Attributgruppe vom Stack entfernen
					popItem();
				}
				break;
			case IN_BLOCK:
				if (localName.equals("block")) {
					try {
						state = IN_BODY;
						numberOfRecords = 0;
						long wait = blockTime - System.currentTimeMillis();
						if((wait>0) && (!isInVerify)) {
							Thread.sleep(wait);
						}
						if (!hasNoData && !hasNoSource) {
							results = new ResultData[requestedResults.size()];
							Iterator it = requestedResults.iterator();

							for (int i = 0; i < results.length; i++) {
								results[i] = (ResultData) it.next();
							}
							sendQueue.push(results);
						}
					} catch (Exception e) {
						debug.error("Thread wird terminiert wegen Fehler: " + e.getMessage());
					}

				}
				break;
			case IN_BODY:
				if (localName.equals("koerper")) {
					state = IN_PROTOCOL;
					debug.fine("fertig!");
				}
				break;
			case IN_PROTOCOL:
				if (localName.equals("protokoll")) {
					state = BEHIND_PROTOCOL;
				}
				break;
			default:
				throw new SAXException("Unerlaubter Zustand bei Parsen der XML-Datei: " + localName);
		}
	}

	/** Fehler
	 * @throws SAXException bei Problemen
	 * @param e Den Fehler auslösende Exception
	 */
	public void error(SAXParseException e)
	throws SAXException {
		super.error(e);
		debug.error("SAXHandler.error: " + e);
	}

	/** Schwerer Fehler
	 * @throws SAXException bei Problemen
	 * @param e Den fatalen Fehler auslösende Exception
	 */
	public void fatalError(SAXParseException e)
	throws SAXException {
		debug.error("SAXHandler.fatalError: " + e);
		super.fatalError(e);
	}

	/** Die DaV-Verbindung der Applikation im SAX-Handler eintragen
	 * @param cdi Die DaV-Verbindung der Applikation
	 */
	public void setConnection(ClientDavInterface cdi) {
		connection = cdi;
	}

	/**
	 * Den Datengenerator im SAX-Handler eintragen
	 *
	 * @param	dg	{@link DataGenerator}, von dem aus u. a. dieses Objekt der
	 *				Klasse {@link SaxHandler} gestartet wurde.
	 */
	public void setDataGenerator(DataGenerator dg) {
		dataGenerator = dg;
	}

	/** Das Datenmodell der Applikation im SAX-Handler eintragen
	 * @param dm Das Datenmodell der Applikation
	 */
	public void setDataModel(DataModel dm) {
		dataModel = dm;
	}

	/** Festlegen, ob sich der Parser in der Verifizierung befindet
	 * @param iiv True, wenn in der Verifizierung, sonst False
	 */
	public void setIsInVerify(boolean iiv) {
		isInVerify = iiv;
	}

	/** Die angeforderten Datenobjekte der Applikation im SAX-Handler eintragen
	 * @param objects Die gewünschten Objekte
	 */
	public void setRequestedObjects(List objects) {
		requestedObjects = objects;
		hasObjects = (objects != null && objects.size() > 0);
	}

	/** Sende-Queue der Applikation im SAX-Handler eintragen (in diesen werden die aus
	 * der XML-Datei gelesenen Telegramme geschrieben, so daß sie vom Sende-Thread
	 * verschickt werden können)
	 * @param sq Die Sende-Queue
	 */
	public void setSendQueue(SendInterface sq) {
		sendQueue = sq;
	}

	public void	skippedEntity(String name)
	throws SAXException {
		super.skippedEntity(name);
		debug.warning("SAXHandler.skippedEntity: " + name);
	}

	/** Callback-Methode für Start des XML-Dokuments
	 * @throws SAXException bei Problemen
	 */
	public void startDocument()
	throws SAXException {
		super.startDocument();
		debug.finer("startDocument.");
		state = BEFORE_PROTOCOL;
	}

	/** Start-Tag gefunden.
	 * @param uri URI des Tags
	 * @param localName Bezeichner
	 * @param qName Qualifizierter Name
	 * @throws SAXException bei Problemen
	 * @param attributes Liste der Attribute des Tags
	 */
	public void startElement(String uri, String localName,
	String qName, Attributes attributes)
	throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		debug.finer("startElement: " + localName);
		int length= attributes.getLength();
		switch (state) {
			case BEFORE_PROTOCOL:
				if (localName.equals("protokoll")) {
					state = IN_PROTOCOL;
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case IN_PROTOCOL:
				if (localName.equals("kopf")) {
					state = IN_HEADER;
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case IN_HEADER:
				if (localName.equals("start")) {
					String s = "";

					try {
						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						s = attributes.getValue(uri, "wert");
						historicalStartTime = dateFormat.parse(s).getTime();
						if (timeStampOption == 1) {
							historicalOffset = startTime - historicalStartTime;
						} else {
							historicalOffset = 0;
						}
					} catch (Exception e) {
						throw new SAXException("Fehler beim Parsen des Datums des Datensatzes: " + s);
					}
				} else if (localName.equals("aufrufparameter")) {
					if (!hasObjects) {
						String s = "";
						s = attributes.getValue(uri, "wert");
						makeSubscriptions(s);
					}
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case NACH_HEADER:
				if (localName.equals("koerper")) {
					state = IN_BODY;
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case IN_BODY:
				if (localName.equals("block")) {
					state = IN_BLOCK;
					String s = "";
					try {
						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						s = attributes.getValue(uri, "empfangszeit");
						historicalBlockTime = dateFormat.parse(s).getTime();
						offset = historicalBlockTime - historicalStartTime;
						blockTime = startTime + offset;
						requestedResults = new LinkedList();
					} catch (Exception e) {
						throw new SAXException("Fehler beim Parsen des Datums des Datensatzes: " + s);
					}
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case IN_BLOCK:
				if (localName.equals("datensatz")) {
					state = IN_RECORD;
				} else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			case IN_RECORD:
				if (localName.equals("objekt")) {
					String s = attributes.getValue(uri, "pid");
					try {
						if (s == null) {
							s = attributes.getValue(uri, "id");
							object = dataModel.getObject((int) Integer.parseInt(s));
						} else {
							object = dataModel.getObject(s);
						}
					} catch (Exception e) {
						throw new SAXException("Fehler bei der Kommunikation mit der Konfiguration: " + e.getMessage());
					}
				} else if (localName.equals("attributgruppe")) {
					String s = attributes.getValue(uri, "pid");

					try {
						if (s == null) {
							s = attributes.getValue(uri, "id");
							atg = (AttributeGroup)dataModel.getObject(Long.parseLong(s));
						} else {
							atg = dataModel.getAttributeGroup(s);
						}
						data = connection.createData(atg);
						pushItem(data);
						hasNoData = false;
						hasNoSource = false;
					} catch (Exception e) {
						throw new SAXException("Fehler bei der Kommunikation mit der Konfiguration: " + e.getMessage());
					}
				} else if (localName.equals("aspekt")) {
					String s = attributes.getValue(uri, "pid");
					try {
						if (s == null) {
							s = attributes.getValue(uri, "id");
							asp = (Aspect)dataModel.getObject(Long.parseLong(s));
						} else {
							asp = dataModel.getAspect(s);
						}
					} catch (Exception e) {
						throw new SAXException("Fehler bei der Kommunikation mit der Konfiguration: " + e.getMessage());
					}
				} else if (localName.equals("simulationsvariante")) {
					simVariantExplicitlySet = true;
					String s = attributes.getValue(uri, "wert");
					simVariant = (short) Short.parseShort(s);
				} else if (localName.equals("nachgeliefert")) {
					String s = attributes.getValue(uri, "wert");
                    isDelayed = s.equals("ja");
				} else if (localName.equals("zeit")) {
					String s = "";
					try {
						DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
						s = attributes.getValue(uri, "wert");
						historicalTimeStamp = dateFormat.parse(s).getTime();
						timeStamp = historicalTimeStamp + historicalOffset;
					} catch (Exception e) {
						throw new SAXException("Fehler beim Parsen des Datums des Datensatzes: " + s);
					}
				} else if (localName.equals("attribut")) {
					String name = attributes.getValue(uri, "name");
					String value = attributes.getValue(uri, "wert");
					debug.finer("Attribut mit Namen " + name + ", Wert: " + value);
					Data item = readTop();
					item.getItem(name).asTextValue().setText(value);
				} else if (localName.equals("feld")) {
					String name = attributes.getValue(uri, "name");
					int count = (int) Integer.parseInt(attributes.getValue(uri, "länge"));
					debug.finer("Feld mit Namen " + name + ", Länge: " + count);
					Data item = readTop().getItem(name);
					item.asArray().setLength(count);
					pushItem(item);
				} else if (localName.equals("liste")) {
					String name = attributes.getValue(uri, "name");
					Data item = readTop().getItem(name);
					pushItem(item);
				} else if (localName.equals("keineQuelle")) {
					hasNoSource = true;
				} else if (localName.equals("keineDaten")) {
					hasNoData = true;
				}else {
					throw new SAXException("Unerlaubtes Tag bei Parsen der XML-Datei: " + localName);
				}
				break;
			default:
				throw new SAXException("Unerlaubter Zustand bei Parsen der XML-Datei: " + localName);
		}

	}

	/** Option für den Zeitstempel setzen: Entweder werden die Daten mit den originalen
	 * Zeitstempeln wieder eingespielt oder in die Jetztzeit verschoben
	 * @param tso Gibt an, ob die Daten mit den originalen Zeitstempeln wieder eingespielt oder in
	 * die Jetztzeit verschoben werden sollen.
	 */
	public void setTimeStampOption(int tso) {
		timeStampOption = tso;
	}

	/** Warnung
	 * @throws SAXException bei Problemen
	 * @param e Die Warnung auslösende Exception
	 */
	public void warning(SAXParseException e)
	throws SAXException {
		super.warning(e);
		debug.warning("SAXHandler.warning: " + e.getMessage());
	}

	/** Oberstes Element des {@link Data}-Stacks lesen, ohne es vom Stack zu entfernen
	 * @return {@link Data}-Element, welches zuoberst auf dem Stack liegt
	 */
	private Data readTop() {
		if (itemStack.size() == 0) {
			// erster Eintrag in der Iteratorliste
			return data;
		} else {
			return ((Data) itemStack.getLast());
		}
	}

	/**
	 * "-rolle"-, "-objekte"- und "-daten"-Einträge aus der XML-Datei auswerten
	 * und entsprechende Anmeldungen durchführen. Andere in der Datei
	 * festgehaltene Parameter werden ignoriert.
	 *
	 * @param	argumentString	String mit den Aufrufparametern, die in der
	 *							XML-Datei gespeichert sind.
	 */
	private void makeSubscriptions(String argumentString) {
		if (!isInVerify) {
			String[] arguments = argumentString.split(" ");
			ArgumentList argumentList = new ArgumentList(arguments);
			String[] dataDescriptions = null;
			ArrayList matchList = new ArrayList();;
			ArgumentList.Argument arg;
			while(argumentList.hasUnusedArguments()) {
				String argumentName = argumentList.getNextArgumentName();
				arg = argumentList.fetchNextArgument();
				if (argumentName.equals("-rolle")) {
					String argString = "-rolle=" + arg.asString();
					matchList.add(argString);
				} else if ((argumentName.equals("-objekt"))
					|| (argumentName.equals("-objekte"))) {
					String argString = "-objekt=" + arg.asString();
					matchList.add(argString);
				} else if (argumentName.equals("-daten")) {
					String argString = "-daten=" + arg.asString();
					matchList.add(argString);
				}
			}
	        int resultSize = matchList.size();
		    dataDescriptions = new String[resultSize];
			matchList.subList(0, resultSize).toArray(dataDescriptions);
			ArgumentList relevantArguments = new ArgumentList(dataDescriptions);

			DataKindDeterminer dkd
			= new DataKindDeterminer(DataKindDeterminer.isSender,
									 "Quelle",
									 DataKindDeterminer.notExamineParamData);
			List subscriptions = new LinkedList();
			subscriptions = dkd.getDataKinds(relevantArguments);
			try {
				requestedObjects
				= dkd.register(dataGenerator, subscriptions, dataModel,
							   dataGenerator.getIsAutarkic(), connection);
				dataGenerator.checkSubscription(subscriptions);
			} catch (Exception e) {
				debug.error("Registrierung der Objekte der XML-Datei fehlgeschlagen: "
							+ e.getMessage());
			}
		}
	}

	/** Oberstes Element des {@link Data}-Stacks lesen
	 * @return {@link Data}-Element, welches zuoberst auf dem Stack liegt
	 */
	private Data popItem() {
		debug.finer("popItem: " + itemStack.getLast());
		debug.finer("popItem(): " + (itemStack.size() - 1) + " Items auf dem Stack");
		return ((Data) itemStack.removeLast());
	}

	/** Neues {@link Data} auf den Stack der Daten-Elemente legen
	 * @param item Datenelement, welches auf den Stack gelegt wird
	 */
	private void pushItem(Data item) {
		debug.finer("pushItem: " + item.toString());
		itemStack.add(item);
		debug.finer("pushItem(): " + itemStack.size() + " Items auf dem Stack");
	}

	public InputSource resolveEntity(
			String publicId, String systemId) throws SAXException, IOException {
		if(systemId != null && systemId.endsWith("/protokollV3.dtd")) {
			URL url = this.getClass().getResource("protokollV3.dtd");
			assert url != null : this.getClass();
			debug.info("Es wird die im Datengenerator enthaltene protokollV3.dtd zum Parsen verwendet");
			return new InputSource(url.toExternalForm());
		}
		return null;
	}
}
