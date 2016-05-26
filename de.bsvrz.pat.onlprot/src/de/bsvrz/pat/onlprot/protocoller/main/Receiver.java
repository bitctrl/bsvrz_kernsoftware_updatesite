/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.onlprot.protocoller.main;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientDavParameters;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer.DataKindDeterminer;
import de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer.SubscriptionInfo;
import de.bsvrz.pat.onlprot.protocoller.protocolModuleConnector.ProtocolModuleConnector;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.configObjectAcquisition.ConfigurationHelper;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Online-Protokollierer
 * <p>
 * Diese Klasse implementiert den Online-Protokollierer, der sich über Aufrufargumente gesteuert auf beliebige Online-Daten des Systems anmelden kann.
 * <p>
 * Aufruf: <blockquote><code> java de.bsvrz.pat.onlprot.protocoller.main.Receiver [-protModul=modulName] [-datei=protokollDatei] [&lt;weitereParameterDesVerwendetenProtokollierungsmoduls&gt;]
 * ([-rolle=anmeldeRolle] [-option=anmeldeOption] [-objekte=objektSpezifikationen] -daten=datenSpezifikation)+ </code></blockquote>
 * <p>
 * Das heißt, es können eine oder mehrere Anmeldespezifikationen beim Start des Programms angegeben werden.
 * <p>
 * Jede Anmeldespezifikation kann optional die Anmelde-Rolle, die Anmeldeoption und die Angabe der Objekte enthalten, für die die Anmeldung durchgeführt werden
 * soll. Wenn diese Argumente weggelassen werden, dann übernehmen sie die unten angegebenen Standardwerte. Über die Datenspezifikation muss angegeben werden,
 * welche Daten angemeldet werden sollen.
 * <p>
 * Im einzelnen stehen die Parameter für folgendes:
 * <p>
 * <dl> <dt><code>-protModul = modulName</code> <dd>Name des Moduls, welches ein Protokollierungsmodul implementiert. Wird kein Protokollierungsmodul angegeben,
 * so wird das Standardmodul hinzugeladen. <dt><code>-datei = protokollDatei</code> <dd> Legt die Datei fest, in der die protokollierten Datensequenzen
 * gespeichert werden können. Wird dieser Parameter nicht angegeben, so werden die protokollierten Daten auf die Standardausgabe ausgegeben. <dt><code>-rolle =
 * anmeldeRolle</code> <dd>Hier sind die Werte <code>Empfänger</code> (<code>receiver</code>) bzw. <code>Senke</code> (<code>drain</code>) möglich, um zu
 * signalisieren, dass die Applikation als Empfänger bzw. als Senke für beliebige Daten arbeiten soll. Wenn keine Anmelde-Rolle angegeben wurde, wird
 * standardmäßig der Wert Empfänger benutzt. <dt><code>-option = anmeldeOption</code> <dd><code>online</code> (Standard), <code>o</code>: Anmeldung auf alle
 * Online-Datensätze.<br> <code>delta</code>, <code>d</code>: Anmeldung nur auf geänderte Datensätze. <br> <code>nachgeliefert</code>, <code>n</code>,
 * <code>delayed</code>: Anmeldung zusätzlich auch auf nachgelieferte Datensätze. <dt><code>-objekte = objektSpezifikationen</code> <dd>Kommaseparierte Liste
 * von PIDs oder Objekt-Ids mit optionalem, durch Doppelpunkt getrennten Mengennamen. <br> Es können mehrere, durch Kommas getrennte, Objekte spezifiziert
 * werden. Ein Objekt wird entweder durch die Objekt-Id oder vorzugsweise durch die PID des Objekts spezifiziert. Optional kann hinter der Objekt-ID oder PID
 * mit Doppelpunkt getrennt ein Mengenname angegeben werden; damit wird dann nicht das angegebene Objekt, sondern alle in der angegebenen Menge unterhalb des
 * angegebenen Objekts enthaltenen Objekte spezifiziert. <dt><code>-daten = datenSpezifikation</code> <dd>Durch Doppelpunkt getrennt Attributgruppen-PID,
 * Aspekt-PID und optional Simulationsvariante.<br> Anstelle der PID der Attributgruppe kann mit einem Stern ("*") angegeben werden, dass die Anmeldung für alle
 * Attributgruppen, die bei den ausgewählten Objekten als Online-Attributgruppen konfiguriert sind, durchgeführt wird.<br> Anstelle der PID des Aspekte kann mit
 * einem Stern ("*") angegeben werden, dass die Anmeldung für alle Aspekte, die bei den ausgewählten Attributgruppen konfiguriert sind, durchgeführt wird.<br>
 * Die Simulationsvariante kann (einschließlich des vorhergehenden Doppelpunkts) weggelassen werden um die Default-Simulationsvariante zu benutzen. </dl>
 * <p>
 * Empfangene Datensätze werden mit Hilfe einer {@link de.bsvrz.pat.onlprot.standardProtocolModule.StandardProtocoller Ausgabefunktion} ausgegeben. Die Art der Ausgabe kann variiert
 * werden durch weitere Protokollierungsmodule, die von {@link de.bsvrz.pat.onlprot.standardProtocolModule.ProtocolModule} abgeleitet werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5031 $
 */
public class Receiver {

	/** Verbindung zum DaV */
	private ClientDavInterface connection = null;

	/** Die Debug-Ausgabe */
	private static Debug debug = null;

	/** Andockstelle für Protokollierungsmodule */
	private static ProtocolModuleConnector pmc = null;

	/**
	 * Erzeugt ein neues Objekt der Klasse <code>Receiver</code>
	 *
	 * @param parameters        {@link ClientDavParameters}
	 * @param subscriptionInfos List
	 *
	 * @throws Exception bei Problemen
	 */
	private Receiver(ClientDavParameters parameters, List subscriptionInfos) throws Exception {
		connection = new ClientDavConnection(parameters);
		connection.connect();
		connection.login();
		DataModel dataModel = connection.getDataModel();

		Iterator infoIterator = subscriptionInfos.iterator();
		while(infoIterator.hasNext()) {
			List<SystemObject> objects = new LinkedList<SystemObject>();
			SubscriptionInfo subscriptionInfo = (SubscriptionInfo)infoIterator.next();
			String objectSpec = subscriptionInfo.getObjectSpec();
			objects = ConfigurationHelper.getObjects(objectSpec, dataModel);

			String[] dataSpecs = subscriptionInfo.getSplittedData(":", 3);
			AttributeGroup attributeGroup = dataModel.getAttributeGroup(dataSpecs[0]);
			if(attributeGroup == null) {
				debug.error("Attributgruppe \"" + dataSpecs[0] + "\" existiert nicht.");
				System.exit(1);
			}
			Aspect aspect = dataModel.getAspect(dataSpecs[1]);
			if(aspect == null) {
				debug.error("Aspekt \"" + dataSpecs[1] + "\" existiert nicht.");
				System.exit(1);
			}
			DataDescription dataDescription;
			String simulationVariantText = "";
			if(dataSpecs.length == 3) {
				short simulationVariant = Short.parseShort(dataSpecs[2]);
				simulationVariantText = ":" + simulationVariant;
				dataDescription = new DataDescription(
						attributeGroup, aspect, simulationVariant
				);
			}
			else {
				dataDescription = new DataDescription(attributeGroup, aspect);
			}

			debug.config(
					subscriptionInfo.getOptions() + "-Anmeldung als " + subscriptionInfo.getReceiverRole() + " für " + attributeGroup.getNameOrPidOrId() + ":"
					+ aspect.getNameOrPidOrId() + simulationVariantText
			);
			debug.config(" Objekte: " + objects);

			/* XXX
			 * Work around: Ist die Simulationsvariante -1 (also nicht explizit
			 * über die Aufrufparameter gesetzt), so wird sie durch die
			 * DaV-Applikationsfunktionen automatisch auf 0 gesetzt. Dieses
			 * Verhalten ist unerwünscht und wird hier bis auf weiteres
			 * umgangen.
			 */
			short rescueSimulationVariant = dataDescription.getSimulationVariant();

			connection.subscribeReceiver(
					pmc.getProtocoller(), objects, dataDescription, subscriptionInfo.getOptions(), subscriptionInfo.getReceiverRole()
			);

			/* XXX
			 * Work around (s. o.)
			 */
			dataDescription.setSimulationVariant(rescueSimulationVariant);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String inputLine = "";
		while(true) {
			inputLine = in.readLine();
			if(inputLine == null) {
				// nicht mehr weiter lesen, aber nicht das Programm terminieren
				break;
			}
			else if(inputLine.startsWith("q")) {
				//Programm kontrolliert terminieren
				System.exit(0);
			}
		}
	}

	/**
	 * Hauptfunktion des Online-Protokollierers
	 *
	 * @param arguments String[] mit Aufrufargumenten
	 */
	public static void main(String[] arguments) {

		ArgumentList argumentList = new ArgumentList(arguments);
		Debug.init("OnlineProtokollierer", argumentList);
		debug = Debug.getLogger();

		/* Vollständige Aufrufparameter-Zeile */
		String[] originalArguments = new String[arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			originalArguments[i] = arguments[i];
		}

		DataKindDeterminer dkd = new DataKindDeterminer(
				DataKindDeterminer.notIsSender, "Empfänger", DataKindDeterminer.examineParamData
		);
		ClientDavParameters parameters;
		List subscriptions = new LinkedList();

		try {
			parameters = new ClientDavParameters(arguments);
			parameters.setApplicationTypePid("typ.applikation");
			parameters.setApplicationName("OnlineProtokollierer");
			pmc = new ProtocolModuleConnector(argumentList, originalArguments);
			subscriptions = dkd.getDataKinds(argumentList);

			argumentList.ensureAllArgumentsUsed();
		}
		catch(Exception e) {
			debug.error("Fehler beim Auswerten der Argumente:");
			debug.error("  " + e.getMessage());
			debug.error(
					"Benutzung: java de.bsvrz.pat.onlprot.protocoller.main.Receiver [-protModul=modulName] [-datei=protokollDatei] [<weitereParameterDesVerwendetenProtokollierungsmoduls>] ([-rolle=anmeldeRolle] [-option=anmeldeOption] [-objekte=objektSpezifikationen] -daten=datenSpezifikation)+"
			);
			debug.error(
					"-protModul: Name des Moduls, welches ein Protokollierungsmodul implementiert. Wird kein Protokollierungsmodul angegeben, so wird das Standardmodul hinzugeladen."
			);
			debug.error(
					"-datei: Legt die Datei fest, in der die protokollierten Datensequenzen gespeichert werden können. Wird dieser Parameter nicht angegeben, so werden die protokollierten Daten auf die Standardausgabe ausgegeben."
			);
			if(pmc != null) {
				debug.error(pmc.getHelp());
			}
			else {
				debug.error("<weitere Parameter des Protokollierungsmoduls>");
			}
			if(dkd != null) {
				debug.error("-rolle= " + dkd.getValidRoles().getInfo());
				debug.error("-option= " + dkd.getValidOptions().getInfo());
			}
			else {
				debug.error("-rolle = <keine Information verfügbar>");
				debug.error("-option = <keine Information verfügbar>");
			}
			debug.error("-objekte:Kommaseparierte Liste von Objekt-IDs oder PIDs mit optionalem, durch Doppelpunkt getrennten Mengennamen");
			debug.error("-daten:durch Doppelpunkt getrennt Attributgruppen-PID, Aspekt-PID und optional Simulationsvariante");
			System.exit(1);
			return;
		}
		try {
			Receiver application = new Receiver(parameters, subscriptions);
		}
		catch(Exception e) {
			e.printStackTrace(System.out);
			debug.error("Fehler:");
			debug.error("  " + e.getMessage());
			System.exit(1);
			return;
		}
	}
}
