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


package de.bsvrz.pat.datgen.generator.main;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.pat.datgen.generator.xmlParser.XmlFormatReader;
import de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer.DataKindDeterminer;
import de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer.SubscriptionInfo;
import de.bsvrz.pat.onlprot.protocoller.protocolModuleConnector.ProtocolModuleConnector;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.configObjectAcquisition.ConfigurationHelper;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Generator-Applikation zur zeitlich gesteuerten Erzeugung von Online-Daten beliebiger Attributgruppen zu Test- und Analysezwecken. Über Aufrufparameter können
 * Attributgruppe, Aspekt, Simulationsvariante und die Objekte, für die die Daten erzeugt werden, die Art der Anmeldung (als Quelle oder Sender) und das
 * zeitliche Verhalten vorgegeben werden. Die erzeugten Daten können bei Bedarf beim Versand protokolliert werden. Aufruf: <blockquote><code> java
 * de.bsvrz.pat.datgen.generator.main.DataGenerator [-protModul=<modulName>] [-datei=<protokollDatei>] [-autark] [<weitereParameterDesVerwendetenProtokollierungsmoduls>]
 * ([-zyklus=<zyklusVon>[-<zyklusBis>]] [-anzahl=<anzahlZyklen>] [-spreizung=<spreizungsGrad>] [-zeit=<zeitstempelWert>] [-rolle=<anmeldeRolle>]
 * objekte=<objektSpezifikationen> -daten=<datenSpezifikation>)+ </code></blockquote> oder <blockquote><code> java de.bsvrz.pat.datgen.generator.main.DataGenerator
 * [-protModul=<modulName>] -eingabe=<eingabeDatei> [-basisUri=<uri>] [-zeitstempel=<wiedergabeVerhalten>] [-validieren=ja|nein] [-datei=<protokollDatei>]
 * [-autark] [<weitereParameterDesVerwendetenProtokollie-rungsmoduls>] ([-rolle=<anmeldeRolle>] [-objekte=<objektSpezifikationen> -daten=<datenSpezifikation>])+
 * </code></blockquote>
 * <p>
 * Beispiel: <blockquote><code> java de.bsvrz.pat.datgen.generator.main.DataGenerator -ausgabe=kopf -objekte=vrz.aachen:MQ -daten=atg.verkehrswerte:asp.analyseWerte </code></blockquote>
 * Es können also eine oder mehrere Anmeldespezifikationen beim Start des Programms angegeben werden. Jede Anmeldespezifikation kann optional die Zykluszeit,
 * Anmelde-Rolle, die Art der Protokollierung und die Angabe der Objekte, für die die Anmeldung durchgeführt werden soll, enthalten. Wenn diese Argumente
 * weggelassen werden, dann übernehmen sie ihren Wert aus der vorhergegangenen Anmeldespezifikation. Über die Datenspezifikation muss angegeben werden, welche
 * Daten angemeldet werden sollen.
 * <p>
 * Die Zykluszeit kann durch eine {@link de.bsvrz.sys.funclib.commandLineArgs.ArgumentList.Argument#asRelativeTime relative Zeitangabe } spezifiziert werden.
 * <p>
 * Für den Platzhalter <code>anmeldeRolle</code> können die Werte <code>quelle</code> bzw. <code>sender</code> eingesetzt werden, um zu signalisieren, dass die
 * anzumeldenden Daten als Quelle an beliebige Empfänger bzw. als Sender an eine Senke übertragen werden sollen. Wenn keine Anmelde-Rolle angegeben wurde, wird
 * standardmäßig der Wert <code>empfänger</code> benutzt.
 * <p>
 * Mit dem Platzhalter <code>objektSpezifikationen</code>  können mehrere Objekte durch Komma getrennt spezifiziert werden. Ein Objekt wird entweder durch die
 * Objekt-Id oder vorzugsweise durch die PID des Objekts spezifiziert. Optional kann hinter der Objekt-ID oder PID mit Doppelpunkt getrennt ein Mengenname
 * angegeben werden; damit wird dann nicht das angegebene Objekt, sondern alle in der angegebenen Menge unterhalb des angegebenen Objekts enthaltenen Objekte
 * spezifiziert.
 * <p>
 * Mit dem Platzhalter <code>datenSpezifikation</code> werden durch Doppelpunkt getrennt die PID der Attributgruppe, die PID des Aspekts und die
 * Simulationsvariante für die Anmeldung und die Datenerzeugung spezifiziert. Die Simulationsvariante kann (einschließlich des vorhergehenden Doppelpunkts)
 * weggelassen werden um die Default-Simulationsvariante (i.a. 0) zu benutzen.
 * <p>
 * Nach der Anmeldung auf die spezifizierten Daten werden für jede Anmeldespezifikation unabhängig zyklisch Daten für die jeweiligen Objekte generiert. Dabei
 * wird das Senden der Datensätze über den im Platzhalter <code>spreizungsGrad</code> angegeben Wert (in Prozent) über den entsprechenden Anteil der Zykluszeit
 * homogen verteilt. Bei der Angabe <code>-zyklus=1m -spreizung=50</code> werden minütlich Datensätze für die Objekte generiert und innerhalb der ersten 30
 * Sekunden eines jeden Zyklus übertragen. Wenn kein Spreizungsgrad angegeben wurde, wird der Wert <code>0</code> benutzt. Dabei werden die Datensätze der
 * einzelnen Objekte in jedem Zyklus im Block übertragen.
 * <p>
 * Die Anzahl der Zyklen, kann über den Platzhalter <code>anzahlZyklen</code> vorgegeben werden. Wenn keine Anzahl vorgegeben ist, dann wird der Wert 0 benutzt,
 * der als unbeschränkt interpretiert wird.
 * <p>
 * Beim Versenden von Datensätzen können diese mit Hilfe der Ausgabefunktion des Onlineprotokollierers ausgegeben werden.
 * <p>
 * Beim Erzeugen von Daten berücksichtigt der Datengenerator die im Metamodell angegebenen Wertebereichsgrenzen. Ansonsten sind die erzeugten Daten im
 * wesentlichen zufällig, d.h. aus fachlicher Sicht i.a. nicht sinnvoll.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class DataGenerator implements ClientSenderInterface {

	/** Das Objekt zur Bestimmung der Datenart */
	private static DataKindDeterminer dkd;

	/*
	 * Zeigt an, ob neue, zufällige Daten erzeugt werden (<code>true</code>)
	 * oder eine XML-Datei eingelesen und wiedergegeben werden soll
	 * (<code>false</code>)
	 */
	private static boolean isGenerator = true;

	/** <code>true</code>, wenn Sendesteuerung verlangt wird. */
	private static boolean isSendControlValid = false;

	/** Liste der Objekte, für die Sendeerlaubnis vorliegt */
	private static Set registeredObjects = new HashSet();

	/** Sende-<i>Queue</i> */
	private static final SendInterface sendQueue = new QueueSurrogate(); //new Queue();

	/** Wartezeit nach dem Anmelden , damit die Sendesteuerungen eintreffen können, bevor Daten gesendet werden */
	private static long waitForDataRequests;

	/** Klasse die statt der Klasse Queue zum Versenden eingesetzt wird, aber die Datensätze ungepuffert an die Datenverteiler-Applikationsfunktionen weitergibt. */
	private static class QueueSurrogate implements SendInterface {

		/** Legt ein neues Objekt dieser Klasse an. */
		QueueSurrogate() {
		}

		/** Der Debug-Logger der Klasse */
		static private final Debug debug = Debug.getLogger();

		/**
		 * Element in die LIFO-Liste schreiben
		 *
		 * @param object {@link Object}, welches in die LIFO-Liste geschrieben wird.
		 *
		 * @throws ConfigurationException     wenn bei der Kommunikation mit der Konfiguration ein Problem auftritt.
		 * @throws de.bsvrz.dav.daf.main.DataNotSubscribedException wenn für die bei der Konfiguration angeforderten Daten keine Anmeldung vorliegt.
		 */
		public void push(Object object) throws ConfigurationException, DataNotSubscribedException {

			ResultData[] results = (ResultData[])object;
			List resultsToSendList = new LinkedList();
			ResultData[] resultsToSend;

			synchronized(registeredObjects) {
				for(int i = 0; i < results.length; i++) {
					if(hasDataRequest(results[i])) {
						resultsToSendList.add(results[i]);
					}
				}
				if(resultsToSendList.size() > 0) {
					resultsToSend = (ResultData[])resultsToSendList.toArray(new ResultData[0]);

					pmc.getProtocoller().update(resultsToSend);
					if(!isAutarkic) {
						try {
							/*
							 * XXX
							 * Work around: Ist die Simulationsvariante -1 (also nicht explizit
							 * über die Aufrufparameter gesetzt), so wird sie durch die
							 * DaV-Applikationsfunktionen automatisch auf 0 gesetzt. Dieses
							 * Verhalten ist unerwünscht und wird hier bis auf weiteres
							 * umgangen.
							 */
							short[] rescueSimulationVariants = new short[resultsToSend.length];
							for(int i = 0; i < resultsToSend.length; i++) {
								rescueSimulationVariants[i] = resultsToSend[i].getDataDescription().getSimulationVariant();
							}
							connection.sendData(resultsToSend);
							/*
							 * XXX
							 * Work around(s. o.)
							 */
							for(int i = 0; i < rescueSimulationVariants.length; i++) {
								resultsToSend[i].getDataDescription().setSimulationVariant(rescueSimulationVariants[i]);
							}
						}
						catch(SendSubscriptionNotConfirmed e) {
							debug.error(e.getMessage());
						}
					}
				}
			}
		}
	}

	/** Verbindung zum DaV */
	private static ClientDavInterface connection;

	/** Das Datenmodell */
	private static DataModel dataModel;

	/** Die Debug-Ausgabe */
	private static Debug debug = null;

	/** Zeigt an, ob Datengenerator autark arbeiten, d. h. die Ausgabe lediglich mitprotokollieren, soll, ohne generierte Daten an einen DaV zu senden */
	private static boolean isAutarkic = false;

	/** Anzahl der laufenden Datenerzeugungs-<i>Threads</i> */
	private static int numberOfThreads = 0;

	/** Andockstelle für Protokollierungsmodule */
	private static ProtocolModuleConnector pmc = null;

	/** Gibt an, ober der Datengenerator Sendesteuerung wünscht oder nicht. */
	private static boolean hasSendControl = false;


	/**
	 * Erzeugt einen neuen Datengenerator, der Daten aus einer XML-Protokolldatei einliest und als Datentelegramme versendet
	 *
	 * @param subscriptionInfos {@link List}e der anzumeldenden Daten
	 * @param xfr               XmlFormatReader zum Interprtieren der XML-Datei
	 * @param parameters        {@link ClientDavParameters} mit den Kommandozeilenparametern
	 *
	 * @throws Exception bei Problemen
	 */
	private DataGenerator(ClientDavParameters parameters, List subscriptionInfos, XmlFormatReader xfr) throws Exception {
		debug.finer("XML-Konstruktor: Vor dkd.register.");
		List objects = dkd.register(this, subscriptionInfos, dataModel, isAutarkic, connection);
		checkSubscription(objects);
		try {
			debug.finer("XML-Konstruktor: Vor xfr.initSaxHandlers.");
			xfr.initSaxHandlers(this, dataModel, connection, sendQueue, objects);
			debug.finer("XML-Konstruktor: Vor xfr.checkValidity.");
			xfr.checkValidity();
		}
		catch(Exception e) {
			debug.error("Eingabedatei enthält Fehler: " + e.getMessage());
			System.exit(1);
		}

		debug.info("Start des Parsens");
		try {
			debug.finer("XML-Konstruktor: Vor xfr.parse.");
			xfr.parse();
		}
		catch(Exception e) {
			debug.error("Fehler beim Verarbeiten der XML-Datei: " + e.getMessage());
			System.exit(1);
		}

		synchronized(sendQueue) {
			while(numberOfThreads > 0) sendQueue.wait();
		}
		debug.info("Verbindung wird terminiert");
		connection.disconnect(false, "ciao");
	}

	/**
	 * Erzeugt einen neuen Datengenerator, der Zufallsdaten erzeugt und als Datentelegramme versendet
	 *
	 * @param parameters        {@link ClientDavParameters} der Applikation
	 * @param subscriptionInfos {@link List}e der anzumeldenden Daten
	 *
	 * @throws Exception bei Problemen
	 */
	private DataGenerator(ClientDavParameters parameters, List subscriptionInfos) throws Exception {
		register(subscriptionInfos);
		Iterator infoIterator = subscriptionInfos.iterator();
		while(infoIterator.hasNext()) {
			SubscriptionInfo subscriptionInfo = (SubscriptionInfo)infoIterator.next();

			new Thread(new DataCycler(connection, subscriptionInfo), "DatenGenerator").start();
		}
		synchronized(sendQueue) {
			while(numberOfThreads > 0) {
				sendQueue.wait();
			}
		}
		debug.info("Verbindung wird terminiert");
		connection.disconnect(false, "ciao");
	}

	/**
	 * Anmeldungen durchgehen und den Wunsch nach Sendesteuerung entsprechend setzen. Sind keine Objekte angegeben, wird die Auswertung auf einen späteren
	 * Zeitpunkt verschoben. Sonst gilt die Regel, daß generell keine Sendesteuerung gewünscht wird, außer es ist mindestens ein Sender vorhanden.
	 *
	 * @param	subscriptions	Liste der Anmeldunge
	 */
	public static void checkSubscription(List subscriptions) {
		if(!isSendControlValid) {
			if((subscriptions != null) && (subscriptions.size() > 0)) {
				Iterator it = subscriptions.iterator();
				while(it.hasNext()) {
					SubscriptionInfo si;
					si = (SubscriptionInfo)it.next();
					if(si.getSenderRole().isSender()) {
						hasSendControl = true;
						break;
					}
					hasSendControl = false;
				}
				isSendControlValid = true;
			}
		}
	}

	/**
	 * Sendesteuerung des Datenverteilers an die Applikation.
	 *
	 * @param	object			Das in der zugehörigen Sendeanmeldung angegebene Objekt, auf das sich die Sendesteuerung bezieht.
	 * @param	dataDescription	Beschreibende Informationen zu den angemeldeten Daten auf die sich die Sendesteuerung bezieht.
	 * @param	state			Status der Sendesteuerung. Kann einen der Werte <code>START_SENDING</code>, <code>STOP_SENDING</code>, <code>STOP_SENDING_NO_RIGHTS</code>,
	 * <code>STOP_SENDING_NOT_A_VALID_SUBSCRIPTION</code> enthalten.
	 */
	public void dataRequest(
			SystemObject object, DataDescription dataDescription, byte state
	) {
		debug.finer("object: " + object + "dataDescription: " + dataDescription + "state: " + state);
		if(state == START_SENDING) {
			List newObject = new LinkedList();
			newObject.add(object);
			newObject.add(dataDescription);
			synchronized(registeredObjects) {
				registeredObjects.add(newObject);
				debug.finer("Hinzugefügt: " + newObject);
			}
		}
		else if((state == STOP_SENDING) || (state == STOP_SENDING_NO_RIGHTS) || (state == STOP_SENDING_NOT_A_VALID_SUBSCRIPTION)) {
			if(state == STOP_SENDING_NO_RIGHTS) debug.warning("Keine Rechte zum Senden von " + dataDescription + " für " + object);
			if(state == STOP_SENDING_NOT_A_VALID_SUBSCRIPTION) debug.warning("Fehlerhafte Anmeldung zum Senden von " + dataDescription + " für " + object);
			synchronized(registeredObjects) {
				if(registeredObjects.size() > 0) {
					List deleteObject = new LinkedList();
					deleteObject.add(object);
					deleteObject.add(dataDescription);
					if(!registeredObjects.remove(deleteObject)) {
						debug.finer("Nicht entfernt: " + deleteObject + "# reg. Objekte: " + registeredObjects.size());
					}
					else {
						debug.finer("Entfernt: " + deleteObject + "# reg. Objekte: " + registeredObjects.size());
					}
				}
			}
		}
	}

	/**
	 * Liefert Information, ob Datengenerator autark läuft oder nicht.
	 *
	 * @return boolean, welches <code>true</code> ist, falls der Datengenerator autark läuft. Sonst ist es <code>false</code>.
	 */
	public boolean getIsAutarkic() {
		return isAutarkic;
	}

	/**
	 * Methode zur Feststellung, ob angegebenes Objekt gesendet werden darf
	 *
	 * @param	data	Der zu prüfende Datensatz
	 * @return boolean	welches anzeigt, ob das angegebene Objekt gesendet werden darf
	 */
	private static boolean hasDataRequest(ResultData data) {
		if(!hasSendControl) {
			// Sendesteuerung wurde nicht "beantragt" ==> Daten werden ohne
			// Rücksicht <b>immer<i> gesendet.
			return true;
		}
		if(registeredObjects.size() > 0) {
			Iterator it = registeredObjects.iterator();
			while(it.hasNext()) {
				List registeredObject = new LinkedList();
				registeredObject = (List)it.next();
				SystemObject referenceObject = (SystemObject)registeredObject.get(0);
				DataDescription referenceDataDescription = (DataDescription)registeredObject.get(1);
				SystemObject objectToSend = data.getObject();
				DataDescription dataDescriptionToSend = data.getDataDescription();
				if((referenceObject.equals(objectToSend)) && (referenceDataDescription.getAttributeGroup().equals(dataDescriptionToSend.getAttributeGroup()))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Diese Methode muss von der Applikation implementiert werden, um zu signalisieren, ob Sendesteuerungen gewünscht sind und mit der Methode
	 * <code>dataRequest</code> verarbeitet werden. Da hier Sendesteuerung erwünscht ist, liefert diese Methode <code>true</code> zurück.
	 *
	 * @param object          Das in der zugehörigen Sendeanmeldung angegebene System-Objekt.
	 * @param dataDescription Die in der zugehörigen Sendeanmeldung angegebenen beschreibenden Informationen der angemeldeten Daten.
	 *
	 * @return	<code>true</code>, falls Sendesteuerungen gewünscht sind, sonst <code>false</code>.
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		debug.fine("object = " + object);
		debug.fine("dataDescription = " + dataDescription);
		debug.fine("hasSendControl = " + hasSendControl);
		return hasSendControl;
	}


	/**
	 * Hauptfunktion des Datengenerators
	 *
	 * @param arguments String[] mit Aufrufargumenten
	 */
	public static void main(String[] arguments) {
		ArgumentList argumentList = new ArgumentList(arguments);
		Debug.init("DataGenerator", argumentList);
		debug = Debug.getLogger();

		/* Vollständige Aufrufparameter-Zeile */
		String[] originalArguments = new String[arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			originalArguments[i] = arguments[i];
		}

		/* Der Datengenerator */
		final DataGenerator application;

		dkd = new DataKindDeterminer(
				DataKindDeterminer.isSender, "Quelle", DataKindDeterminer.notExamineParamData
		);


		final ClientDavParameters parameters;
		final List subscriptions;

		/**
		 * Der XML-Leser
		 */
		XmlFormatReader xfr = null;

		try {
			parameters = new ClientDavParameters(arguments);
			parameters.setApplicationName("DataGenerator");
			connection = new ClientDavConnection(parameters);
			connection.connect();
			connection.login();
			dataModel = connection.getDataModel();

			pmc = new ProtocolModuleConnector(argumentList, originalArguments);
			if(argumentList.hasArgument("-eingabe")) {
				isGenerator = false;
				xfr = new XmlFormatReader(argumentList);
			}
			if(argumentList.hasArgument("-autark")) {
				isAutarkic = argumentList.fetchArgument("-autark").booleanValue();
			}

			if(argumentList.hasArgument("-sendesteuerung")) {
				hasSendControl = argumentList.fetchArgument("-sendesteuerung")
						.booleanValue();
				isSendControlValid = true;
			}
			else {
				isSendControlValid = false;
			}


			subscriptions = dkd.getDataKinds(argumentList);
			argumentList.ensureAllArgumentsUsed();

			checkSubscription(subscriptions);
		}
		catch(Exception e) {
			debug.error("Fehler beim Auswerten der Argumente:");
			debug.error("  " + e.getMessage());
			debug.error(
					"Benutzung:\tjava de.bsvrz.pat.datgen.generator.main.DataGenerator [-protModul=<modulName>] [-datei=<protokollDatei>] [-autark] [<weitereParameterDesVerwendetenProtokollierungsmoduls>] ([-zyklus=<zyklusVon>[-<zyklusBis>]] [-anzahl=<anzahlZyklen>] [-spreizung=<spreizungsGrad>] [-zeit=<zeitstempelWert>] [-rolle=<anmeldeRolle>] objekte=<objektSpezifikationen> -daten=<datenSpezifikation>)+"
			);
			debug.error(
					"bzw.\t\tjava de.bsvrz.pat.datgen.generator.main.DataGenerator [-protModul=<modulName>] -eingabe=<eingabeDatei> [-zeitstempel=<wiedergabeVerhalten>] [-datei=<protokollDatei>] [-autark] [<weitereParameterDesVerwendetenProtokollierungsmoduls>] ([-rolle=<anmeldeRolle>] -objekte=<objektSpezifikationen> -daten=<datenSpezifikation>)+"
			);
			debug.error(
					"-protModul: Name des Moduls, welches ein Protokollierungsmodul implementiert. Wird kein Protokollierungsmodul angegeben, so wird das Standardmodul hinzugeladen."
			);
			debug.error("-eingabe: XML-Datei mit den einzuspielenden Daten");
			debug.error(
					"-basisUri: Basis-URI für die DTD. Muß angegeben werden, wenn die URI der DTD in der einzulesenden XML-Datei nicht als absolute URI angegeben ist."
			);
			debug.error(
					"-zeitstempel: \"uebernehmen\", wenn die Zeitstempel aus der Eingabedatei übernommen werden sollen; \"anpassen\", wenn die Startzeit des Protokolls durch die Startzeit des Starts des Datengenerators ersetzt werden soll"
			);
			debug.error("-validieren: \"ja\", falls die einzulesende XML-Datei validiert werden soll, sonst \"nein\"");
			debug.error("-datei: Name der Protokolldatei");
			debug.error("-autark: Die erzeugten Daten werden nicht an den DaV gesendet");
			debug.error("-sendesteuerung: \"ja\", falls Sendesteuerung erwünscht, sonst \"nein\".");
			if(pmc != null) {
				debug.error(pmc.getHelp());
			}
			else {
				debug.error("<weitere Parameter des Protokollierungsmoduls>");
			}
			debug.error(
					"-zyklus: Zeitangabe als Folge von Zahlen und Einheiten (t für Tage, h für Stunden, m für Minuten, s für Sekunden und ms für Millisekunden"
			);
			debug.error("-anzahl: Anzahl der Zyklen oder 0, wenn die Anzahl nicht beschränkt ist");
			debug.error("-spreizung: Zahl zwischen 0 und 100 als Anteil in Prozent der Zykluszeit, die zum Senden der Datensätze benutzt wird");
			if(dkd != null) {
				debug.error("-zeit: " + dkd.getValidTimeOptions().getInfo());
				debug.error("-rolle: " + dkd.getValidRoles().getInfo());
			}
			else {
				debug.error("-zeit: <keine Information verfügbar>");
				debug.error("-rolle: <keine Information verfügbar>");
			}
			debug.error("-objekte: Komma separierte Liste von Objekt-Ids oder PIDs mit optionalem durch Doppelpunkt getrenntem Mengennamen");
			debug.error("-daten: Durch Doppelpunkt getrennt Attributgruppen-PID, Aspekt-PID und optional Simulationsvariante");
			System.exit(1);
			return;
		}

		try {
			if(isGenerator) {
				application = new DataGenerator(parameters, subscriptions);
			}
			else {
				application = new DataGenerator(parameters, subscriptions, xfr);
			}
		}
		catch(Exception e) {
			debug.error("  " + e + " " + e.getMessage());
//			e.printStackTrace();
			System.exit(1);
			return;
		}
	}

	/**
	 * Registrierung durchführen
	 *
	 * @param subscriptionInfos {@link List} mit den Anmeldeinformationen
	 *
	 * @return List mit allen angemeldeten Objekten
	 *
	 * @throws Exception bei Problemen
	 */
	private List register(List subscriptionInfos) throws Exception {
		List allObjects = new LinkedList();
		Iterator infoIterator = subscriptionInfos.iterator();
		while(infoIterator.hasNext()) {
			SubscriptionInfo subscriptionInfo = (SubscriptionInfo)infoIterator.next();
			List<SystemObject> objects = new LinkedList<SystemObject>();
			String objectSpec = subscriptionInfo.getObjectSpec();
			objects = ConfigurationHelper.getObjects(objectSpec, dataModel);
			Iterator objectIterator = objects.iterator();
			while(objectIterator.hasNext()) {
				SystemObject checkedObject = (SystemObject)objectIterator.next();
				if(checkedObject == null) {
					debug.error("Angegebenes Objekt \"" + objectSpec + "\" wurde in der Konfiguration nicht gefunden.");
					System.exit(1);
				}
			}
			String[] dataSpecs = subscriptionInfo.getSplittedData(":", 3);
			AttributeGroup attributeGroup = dataModel.getAttributeGroup(dataSpecs[0]);
			if(attributeGroup == null) {
				debug.error("Attributgruppe  \"" + dataSpecs[0] + "\" existiert nicht.");
				System.exit(1);
			}
			Aspect aspect = dataModel.getAspect(dataSpecs[1]);
			if(aspect == null) {
				debug.error("Aspekt  \"" + dataSpecs[1] + "\" existiert nicht.");
				System.exit(1);
			}
			DataDescription dataDescription;
			String simulationVariantText = "";
			if(dataSpecs.length == 3) {
				short simulationVariant = Short.parseShort(dataSpecs[2]);
				simulationVariantText = ":" + simulationVariant;
				dataDescription = new DataDescription(attributeGroup, aspect, simulationVariant);
			}
			else {
				dataDescription = new DataDescription(attributeGroup, aspect);
			}
			;
			debug.config(
					"Anmeldung als " + subscriptionInfo.getSenderRole() + " für " + attributeGroup.getNameOrPidOrId() + ":" + aspect.getNameOrPidOrId()
			);
			debug.config(simulationVariantText);
			debug.config(" Objekte: " + objects);

			/*
			 * XXX
			 * Work around: Ist die Simulationsvariante -1 (also nicht explizit
			 * über die Aufrufparameter gesetzt), so wird sie durch die
			 * DaV-Applikationsfunktionen automatisch auf 0 gesetzt. Dieses
			 * Verhalten ist unerwünscht und wird hier bis auf weiteres
			 * umgangen.
			 */
			short rescueSimulationVariant = dataDescription.getSimulationVariant();

			if(!isAutarkic) {
				connection.subscribeSender(
						this, objects, dataDescription, subscriptionInfo.getSenderRole()
				);
			}

			waitForDataRequests = System.currentTimeMillis() + 3000;

			/*
			 * XXX
			 * Work around (s. o.)
			 */
			dataDescription.setSimulationVariant(rescueSimulationVariant);

			allObjects.addAll(objects);

			subscriptionInfo.setObjects(objects);
			subscriptionInfo.setDataDescription(dataDescription);
		}
		infoIterator = subscriptionInfos.iterator();
		debug.fine("Start der Datengenerierung-Threads");
		return allObjects;
	}

	/** Implementiert den Sende-Thread */
	private static class Sender implements Runnable {

		/** Verbindung zum DaV */
		private ClientDavInterface _connection;

		/**
		 * Erzeugt einen Sender
		 *
		 * @param connection {@link ClientDavInterface}, welches die Verbindung zum DaV hält
		 * @param pmc        {@link de.bsvrz.pat.onlprot.protocoller.protocolModuleConnector.ProtocolModuleConnector}, welcher die Verbindung zum Protokollierer hält
		 */
		Sender(ClientDavInterface connection, ProtocolModuleConnector pmc) {
			_connection = connection;
		}

		/** Startet den Sende-Thread */
		public void run() {
			try {
				while(true) {
					ResultData[] results = (ResultData[])((de.bsvrz.pat.datgen.generator.main.Queue)sendQueue).pop();
					if(results == null) {
						return;
					}
					pmc.getProtocoller().update(results);
					if(!isAutarkic) {
						_connection.sendData(results);
					}
				}
			}
			catch(Exception e) {
				debug.fine("Sender.run: " + e.getMessage());
			}
		}
	}

	/** Datenerzeugungs-Thread */
	private static class DataCycler implements Runnable {

		/** Der Debug-Logger der Klasse */
		static private final Debug debug = Debug.getLogger();

		/** Die Verbindung zum DaV */
		private ClientDavInterface _connection;

		/** Die Anmeldeinformationen */
		private SubscriptionInfo _info;

		/** Typ eines Typ-Objekts (für die Kommunikation mit der Konfiguration) */
		private SystemObjectType _typeType;

		/** Liste aller Objekte eines bestimmten Typs (wird für die zufällige Erzeugung von Datem vom Typ "Referenz" benötigt) */
		private final Map _objectsOfType = new TreeMap();

		/**
		 * Erzeugt ein Datenerzeugungs-Objekt
		 *
		 * @param connection {@link ClientDavInterface}, welches die Verbindung zum DaV hält
		 * @param info       {@link SubscriptionInfo} mit den Anmeldeinformationen der zu erzeugenden Objekte
		 *
		 * @throws Exception bei Problemen
		 */
		DataCycler(ClientDavInterface connection, SubscriptionInfo info) throws Exception {
			_connection = connection;
			_info = info;
			synchronized(sendQueue) {
				numberOfThreads++;
			}
			_typeType = _connection.getDataModel().getTypeTypeObject();
		}

		/**
		 * Zufälligen Text (der hier aus den "Lottozahlen der nächsten Woche" besteht) erzeugen
		 *
		 * @return String mit zufällig erzeugtem Text
		 */
		private String getRandomText() {
			HashSet set = new HashSet(7);
			while(set.size() < 6) {
				int num = (int)(Math.random() * 49) + 1;
				set.add(new Integer(num));
			}

			String setString = set.toString();
			setString = setString.substring(1, setString.length() - 1);
			return setString + " (Lottozahlen der nächsten Woche)";
		}


		/**
		 * Zufälliges System-Objekt vom gewünschten Typ erzeugen
		 *
		 * @param type Der gewünschte Typ
		 *
		 * @return Zufällig erzeugtes System-Objekt
		 *
		 * @throws Exception wenn Kommunikation mit der Konfiguration nicht klappt oder ein anderes Problem auftritt
		 */
		private SystemObject getRandomSystemObjectOfType(SystemObjectType type) throws Exception {
			if(type == null) type = _typeType;
			List elements = (List)_objectsOfType.get(type);
			if(elements == null) {
				elements = type.getElements();
				_objectsOfType.put(type, elements);
			}
			if(elements.size() == 0) {
				return null;
			}
			else {
				return (SystemObject)elements.get((int)(Math.random() * elements.size()));
			}
		}

		/**
		 * Zufälligen Integer-Wert erzeugen
		 *
		 * @param iType Spezifiziert den gewünschten Integer-Typ
		 *
		 * @return long mit erzeugtem Wert
		 */
		private long getRandomUnscaledValue(IntegerAttributeType iType) {
			IntegerValueRange range = null;
			try {
				range = iType.getRange();
			}
			catch(ConfigurationException e) {
			}
			long val = 0;
			boolean valInRange = false;
			if(range != null) {
				try {
					long min = range.getMinimum();
					long max = range.getMaximum();
					val = (long)((Math.random() * ((double)max - (double)min + 1)) + min);
					if(val < min) {
						val = min;
					}
					else if(val > max) {
						val = max;
					}
					else {
						valInRange = true;
					}
				}
				catch(ConfigurationException e) {
				}
			}
			boolean tryState = Math.random() < 0.05d;
			if(!valInRange || tryState) {
				try {
					List states = iType.getStates();
					if(states.size() > 0) val = ((IntegerValueState)states.get((int)(Math.random() * states.size()))).getValue();
				}
				catch(Exception e) {
				}
			}
			return val;
		}

		/**
		 * <code>Data</code>-Element mit zufällig erzeugten Daten belegen
		 *
		 * @param data Das zu füllende Datenfeld
		 *
		 * @throws Exception wenn beim Belegen des Datenfeldes ein Problem auftritt
		 */
		private void setRandomData(Data data) throws Exception {
			if(data.isPlain()) {
				AttributeType attributeType = data.getAttributeType();
				if(attributeType instanceof IntegerAttributeType) {
					data.asUnscaledValue().set(getRandomUnscaledValue((IntegerAttributeType)attributeType));
				}
				else if(attributeType instanceof DoubleAttributeType) {
					data.asUnscaledValue().set(Math.random() * 200.0d - 100.0d);
				}
				else if(attributeType instanceof TimeAttributeType) {
					data.asTimeValue().setMillis(System.currentTimeMillis());
				}
				else if(attributeType instanceof ReferenceAttributeType) {
					SystemObject o = getRandomSystemObjectOfType(((ReferenceAttributeType)attributeType).getReferencedObjectType());
					data.asReferenceValue().setSystemObject(o);
				}
				else if(attributeType instanceof StringAttributeType) {
					String randomText = getRandomText();
					StringAttributeType sat = (StringAttributeType)attributeType;

					// Wenn der Text länger ist, als die maximal erlaubte Länge
					// des Data-Objekts, wird abgeschnitten.
					if(sat.isLengthLimited()) {
						int lengthChecker = sat.getMaxLength();
						if(randomText.length() > lengthChecker) {
							randomText = randomText.substring(0, lengthChecker);
						}
					}
					data.asTextValue().setText(randomText);
				}
				else {
					debug.fine("AttributTyp " + attributeType + " wird nicht unterstützt");
				}
			}
			else {
				if(data.isList()) {
					Iterator attributeIterator = data.iterator();
					while(attributeIterator.hasNext()) {
						Data attributeData = (Data)attributeIterator.next();
						setRandomData(attributeData);
					}
				}
				else if(data.isArray()) {
					Data.Array dataArray = data.asArray();

					/*
					 * Für Felder variabler Länge sollen bei keiner
					 * Größenbeschränkung max. 10 Einträge erzeugt werden
					 */
					int length = 10;
					if(dataArray.isCountLimited()) {
						length = dataArray.getMaxCount();
					}

					if(dataArray.isCountVariable()) {
						length = 1 + (int)(Math.random() * ((double)length - 1));
					}
					dataArray.setLength(length);

					for(int i = 0; i < length; ++i) {
						Data item = dataArray.getItem(i);
						setRandomData(item);
					}
				}
				else {
					throw new IllegalStateException("Ungültige Datenstruktur im Datensatz");
				}
			}
		}

		/**
		 * Ergebnis erzeugen.
		 *
		 * @param object {@link SystemObject}, für welches ein Telegramm erzeugt wird
		 * @param data   Datensatz, der diesem Ergebnis zugeordnet werden soll.
		 *
		 * @return {@link ResultData} mit dem erzeugten Telegramm
		 *
		 * @throws Exception wenn beim Erzeugen des Telegramms ein Problem auftritt
		 */
		private ResultData createResult(SystemObject object, Data data) throws Exception {
			String[] dataSpecs = _info.getSplittedData(":", 3);
			AttributeGroup atg = dataModel.getAttributeGroup(dataSpecs[0]);
			if(atg == null) {
				debug.error("Attributgruppe \"" + dataSpecs[0] + "\" existiert nicht.");
				System.exit(1);
			}
			if(dataModel.getAspect(dataSpecs[1]) == null) {
				debug.error("Aspekt \"" + dataSpecs[1] + "\" existiert nicht.");
				System.exit(1);
			}
			DataDescription dd;
			if(dataSpecs.length == 3) {
				dd = new DataDescription(
						dataModel.getAttributeGroup(dataSpecs[0]), dataModel.getAspect(dataSpecs[1]), (short)Short.parseShort(dataSpecs[2])
				);
			}
			else {
				dd = new DataDescription(
						dataModel.getAttributeGroup(dataSpecs[0]), dataModel.getAspect(dataSpecs[1]), (short)-1
				);
			}

			return new ResultData(object, dd, 0L, data);
		}

		/**
		 * Neue Daten generieren
		 *
		 * @param resultArray   Array mit Ergebnissen für alle Objekte.
		 * @param dataArray     Array mit Datensätzen für alle Objekte.
		 * @param objectIndex   Index des Objekts für das neue Daten generiert werden sollen.
		 * @param intervalStart Der Zeitstempel des Telegramms
		 *
		 * @return Das modifizierte Ergebnis (resultArray[objectIndex]).
		 *
		 * @throws Exception wenn beim Erzeugen des Telegramms ein Problem auftritt
		 */
		private ResultData getResult(ResultData[] resultArray, Data[] dataArray, int objectIndex, long intervalStart) throws Exception {
			setRandomData(dataArray[objectIndex]);
			long timeStamp;
			if(_info.getTimeStampOption() == DataKindDeterminer.INTERVAL_TIME) {
				timeStamp = intervalStart;
			}
			else {
				timeStamp = System.currentTimeMillis();
			}
			ResultData result = resultArray[objectIndex];
			result.setDataTime(timeStamp);
			return result;
		}

		/** Startet den Datenerzeugungs-Thread */
		public void run() {
			boolean forEver = false;

			try {
				boolean firstInterval = true;	// noch keine Daten gesendet
				long intervalStart;
				long actualInterval = 0;

				// Startzeit für das nächste Intervall ermitteln
				if(_info.getIntervalLowerBound() == 0) {
					intervalStart = System.currentTimeMillis();
				}
				else {
					actualInterval = _info.getRandomInterval();
					debug.fine(
							"actualInterval: " + actualInterval + " zwischen " + _info.getIntervalLowerBound() + " und " + _info.getIntervalUpperBound()
					);
					if(_info.getIntervalLowerBound() == _info.getIntervalUpperBound()) {
						intervalStart = (System.currentTimeMillis() / actualInterval) * actualInterval;
					}
					else {
						intervalStart = System.currentTimeMillis();
					}
				}

				// Parameter f. d. Spreizung berechnen
				long objects = (_info.getSpread() == 100 ? _info.getObjects().size() : _info.getObjects().size() - 1);
				long subIntervalDuration = actualInterval * _info.getSpread() / 100 / (objects == 0 ? 1 : objects);
				if(_info.getIntervalCount() == 0) {
					forEver = true;
				}
				// Was muß erzeugt werden?
				String[] dataSpecs = _info.getSplittedData(":", 3);
				AttributeGroup atg = dataModel.getAttributeGroup(dataSpecs[0]);
				int numberOfObjects = _info.getObjects().size();
				// Datenfeld f. entsprechende Anz. Datensätze anlegen
				Data[] dataArray = new Data[numberOfObjects];
				// Feld anlegen, das versendet wird
				ResultData[] resultArray = new ResultData[numberOfObjects];
				// Struktur der Datensätze generieren
				for(int i = 0; i < numberOfObjects; ++i) {
					dataArray[i] = _connection.createData(atg);
				}
				int objectIndex = 0;
				Iterator objectsIterator = _info.getObjects().iterator();
				while(objectsIterator.hasNext()) {
					SystemObject object = (SystemObject)objectsIterator.next();
					// Struktur für einen Datensatz erzeugen
					resultArray[objectIndex] = createResult(object, dataArray[objectIndex]);
					++objectIndex;
				}
				do {
					long wait;
					intervalStart += actualInterval;
					if(intervalStart > waitForDataRequests) {
						wait = intervalStart - System.currentTimeMillis();
					}
					else {
						wait = waitForDataRequests - System.currentTimeMillis();
					}
					if(wait < -10000 && actualInterval > 0) {
						long dropCount = -wait / actualInterval + 1;
						intervalStart += dropCount * actualInterval;
						debug.warning("Intervallzyklus um mehr als 10 Sekunden überschritten. Es werden " + dropCount + " Intervalle ausgelassen.");
					}
					else {
						if(firstInterval) {
							firstInterval = false;
							debug.fine("Zykluszeit " + actualInterval + " Millisekunden, Start des ersten Zyklus in ca. " + (wait / 1000) + " Sekunden");
						}
						// Auf Beginn des nächsten Intervalls warten
						if(wait > 0) {
							Thread.sleep(wait);
						}
						objectsIterator = _info.getObjects().iterator();
						objectIndex = 0;
						long subIntervalStart = intervalStart;
						while(objectsIterator.hasNext()) {
							SystemObject object = (SystemObject)objectsIterator.next();
							if(subIntervalDuration > 0) {
								wait = subIntervalStart - System.currentTimeMillis();
								if(wait > 0) {
									Thread.sleep(wait);
								}
								debug.finer("Sendung");
								// Neue Daten erzeugen
								ResultData result = getResult(resultArray, dataArray, objectIndex, intervalStart);
								sendQueue.push(new ResultData[]{result});

								//get and send result
								subIntervalStart += subIntervalDuration;
							}
							else {
								// Neue Daten erzeugen
								getResult(resultArray, dataArray, objectIndex, intervalStart);
							}
							++objectIndex;
						}
						if(subIntervalDuration == 0) {
							sendQueue.push(resultArray);
						}

						if(!forEver) {
							_info.decIntervalCount();
						}
					}
					debug.finer("intervalCount: " + _info.getIntervalCount());

					// Intervallbeginn für die "nächste Runde" berechnen
					if(_info.getIntervalLowerBound() == 0) {
						intervalStart = System.currentTimeMillis();
					}
					else {
						actualInterval = _info.getRandomInterval();
						debug.finer(
								"actualInterval: " + actualInterval + " zwischen " + _info.getIntervalLowerBound() + " und " + _info.getIntervalUpperBound()
						);
					}
				}
				while(forEver || _info.getIntervalCount() > 0);
			}
			catch(Exception e) {
				debug.error("Thread wird terminiert wegen Fehler: " + e.getMessage());
			}
			finally {
				synchronized(sendQueue) {
					if(--numberOfThreads <= 0) {
						sendQueue.notifyAll();
					}
				}
			}
		}
	}
}
