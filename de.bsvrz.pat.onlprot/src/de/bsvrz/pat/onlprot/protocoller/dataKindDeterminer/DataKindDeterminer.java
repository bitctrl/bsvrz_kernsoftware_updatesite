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

package de.bsvrz.pat.onlprot.protocoller.dataKindDeterminer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.configObjectAcquisition.ConfigurationHelper;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Datenart bestimmen. Dient zum Zusammentragen von Informationen der zu
 * protokollierenden Objekte, Attributgruppen, Aspekte, Simulationsvarianten,
 * Optionen und Rollen
 *
 * @author Kappich Systemberatung
 * @version $Revision:5031 $
 */
public class DataKindDeterminer {

	/** Der Debug-Logger der Klasse */
	static private final Debug debug = Debug.getLogger();

	/**
	 * Parameter <code>-option</code> soll ausgewertet werden
	 */
	public static final boolean			examineParamData = true;

	/**
	 * Parameter <code>-option</code> soll nicht ausgewertet werden
	 */
	public static final boolean			notExamineParamData = false;

	/**
	 * Applikation ist ein Sender
	 */
	public static final boolean			isSender = true;

	/**
	 * Applikation ist eine Senke
	 */
	public static final boolean			notIsSender = false;

	/**
	 * Vorgabewert für die Rolle. Wird im Konstruktor gesetzt.
	 */
	private final String				defaultRole;

	/**
	 * Anzahl der zu sendenden Intervalle
	 */
	private int							intervalCount = 0;

	/**
	 * Kürzestes zu erzeugendes Intervall
	 */
	private long						intervalLowerBound = 60 * 1000;

	/**
	 * Längstes zu erzeugendes Intervall
	 */
	private long						intervalUpperBound = intervalLowerBound;

	/**
	 * Spezifiziert die erlaubten Rollen
	 */
	private final boolean				isSetToSender;

	/**
	 * Spezifiziert, ob der Aufrufparameter <code> -option </code>ausgewertet
	 * wird
	 */
	private final boolean				optionParamAllowed;


	/**
	 * Streuung
	 */
	private int							spread = 0;

	/**
	 * Zeit-Option
	 */
	private int							timeOption
										= INTERVAL_TIME;


	/**
	 * Liste der gülten Optionen dieser Applikation
	 */
	private ArgumentList.ValueSelection validOptions
										= new ArgumentList.ValueSelection();

	/**
	 * Liste der gültigen Rollen dieser Applikation
	 */
	private ArgumentList.ValueSelection	validRoles
										= new ArgumentList.ValueSelection();

	/**
	 * Liste der gültigen Zeit-Optionen dieser Applikation
	 */
	private ArgumentList.ValueSelection	validTimeOptions
										= new ArgumentList.ValueSelection();

	/** Konstante für <code>-zeit</code>-Argument: Intervallbeginn als Zeitstempel. */
	public static final int INTERVAL_TIME = 0;

	/** Konstante für <code>-zeit</code>-Argument: Versandzeit als Zeitstempel. */
	public static final int SEND_TIME = 1;

	/** Erzeugt ein neues Objekt der Klasse <code>DataKindDeterminer</code> mit
	 * speziellem <code>actionText</code>
	 *
	 * @param isSetToSender			boolean, welches angibt, ob es sich bei der
	 *								Applikation um einen Sender oder Empfänger
	 *								handelt
	 * @param defaultRole			String, der den Vorgabewert für die Rolle
	 *								angibt
	 * @param optionParamAllowed	boolean, welches angibt, ob der Parameter
	 *								<code>-option</code> ausgewertet wird
	 */
	public DataKindDeterminer(boolean isSetToSender, String defaultRole,
							  boolean optionParamAllowed) {
		this.isSetToSender = isSetToSender;
		this.defaultRole = defaultRole;
		this.optionParamAllowed = optionParamAllowed;
	}

	/** Aufrufparameter filtern: Interpretiert werden die Parameter <code>-rolle
	 * </code>, <code>-option </code>, <code>-objekte </code> und <code>-daten
	 * </code>. Ein Satz aus diesen Parametern bildet eine Einheit, wobei
	 * einzelne Werte fehlen können, die dann durch Vorgaben ersetzt werden.
	 * @param argumentList {@link ArgumentList} der noch nicht
	 * 							ausgewerteten Aufrufparameter der Applikation
	 * @return List der angegebenen Datenbeschreibungen
	 */
	public List getDataKinds(ArgumentList argumentList) {

		/* Name eines gelesenen Aufrufparameters */
		String						argumentName = null;

		/* Datenspezifikation */
		String						dataSpec = null;

		/* Objektspezifikation */
		String						objectSpecs = null;

		/*
		 * Option (bei Empfängern): Online-Daten, Delta-Daten oder
		 * nachgelieferte Daten
		 */
		ReceiveOptions				receiveOptions = ReceiveOptions.normal();

		/* Rolle: Quelle, Sender, Empfänger oder Senke */
		String						role = null;

		/* Liste der auszuführenden Anmeldungen */
		List						subscriptions = new LinkedList();

		if (isSetToSender) {
			validRoles.add("quelle").alias("source").alias("empfänger").alias("empfaenger").alias("receiver").ignoreCase()
					.convertTo(SenderRole.source());
			validRoles.add("sender").alias("senke").alias("drain").ignoreCase()
					.convertTo(SenderRole.sender());
			validTimeOptions.add("zyklus").alias("z").ignoreCase()
					.convertTo(INTERVAL_TIME)
					.purpose("Intervallbeginn als Zeitstempel");
			validTimeOptions.add("versand").alias("v").ignoreCase()
					.convertTo(SEND_TIME)
					.purpose("Versandzeit als Zeitstempel");
		} else {
			validRoles.add("senke").alias("drain").ignoreCase()
					.convertTo(ReceiverRole.drain());
			validRoles.add("empfänger").alias("empfaenger").alias("receiver")
					.ignoreCase().convertTo(ReceiverRole.receiver());
		}

		if (optionParamAllowed) {
			validOptions.add("online").alias("o").ignoreCase()
					.convertTo(ReceiveOptions.normal())
					.purpose("Anmeldung auf alle Online-Datensätze");
			validOptions.add("delta").alias("d").ignoreCase()
					.convertTo(ReceiveOptions.delta())
					.purpose("Anmeldung nur auf geänderte Datensätze");
			validOptions.add("nachgeliefert").alias("n").alias("delayed")
					.ignoreCase().convertTo(ReceiveOptions.delayed())
					.purpose("Anmeldung auch auf nachgelieferte Datensätze");
		}

		role = defaultRole;
		receiveOptions = ReceiveOptions.normal();
		objectSpecs = "";
		dataSpec = "";
		while(argumentList.hasUnusedArguments()) {
			argumentName = argumentList.getNextArgumentName();
			if ((argumentName.equals("-zyklus")) && (isSender)) {
				String cycleString
						= argumentList.fetchArgument("-zyklus=60s")
						  .asString();
				String[] subStrings = cycleString.split("-", 2);
				for (int i = 0; i < subStrings.length; i++) {
					subStrings[i] = "-dummy[" + i + "]=" + subStrings[i];
				}
				ArgumentList dummyArgumentList
						= new ArgumentList(subStrings);
				intervalLowerBound = dummyArgumentList
									 .fetchArgument("-dummy[0]=")
									 .asRelativeTime();
				if (dummyArgumentList.hasUnusedArguments()) {
					intervalUpperBound = dummyArgumentList
										 .fetchArgument("-dummy[1]=")
										 .asRelativeTime();
				} else {
					intervalUpperBound = intervalLowerBound;
				}
			} else if ((argumentName.equals("-anzahl")) && (isSender)) {
				intervalCount = argumentList.fetchArgument("-anzahl=0").intValue();
			} else if ((argumentName.equals("-spreizung")) && (isSender))  {
				spread = argumentList.fetchArgument("-spreizung=0")
						 .intValueBetween(0,100);
			} else if ((argumentName.equals("-zeit")) && (isSender))  {
				timeOption = argumentList.fetchArgument("-zeit=zyklus")
							 .asValueCase(validTimeOptions).intValue();
			} else if(argumentName.equals("-rolle")) {
				role = (String) argumentList.fetchNextArgument()
								.asValueCase(validRoles).convert().toString();
			} else if(optionParamAllowed && (argumentName.equals("-option"))) {
				receiveOptions = (ReceiveOptions) argumentList
						.fetchNextArgument().asValueCase(validOptions)
						.convert();
			} else if(argumentName.equals("-objekt")
					|| argumentName.equals("-objekte")) {
				objectSpecs
						= argumentList.fetchNextArgument().asNonEmptyString();
			} else if(argumentName.equals("-daten")) {
				if(objectSpecs == null) {
					throw
					new	IllegalArgumentException("Objektspezifikation fehlt");
				}
				dataSpec = argumentList.fetchNextArgument().asNonEmptyString();
				if (isSetToSender) {
					subscriptions.add(new SubscriptionInfo(intervalLowerBound,
							intervalUpperBound, intervalCount, spread,
							timeOption, role, objectSpecs, dataSpec));
				} else {
					subscriptions.add(new SubscriptionInfo(role,
							receiveOptions, objectSpecs, dataSpec));
				}

				role = defaultRole;
				receiveOptions = ReceiveOptions.normal();
				objectSpecs = "";
				dataSpec = "";

			} else {
				break;
			}
		}
		return subscriptions;
	}

	/**
	 * Gibt die Liste der gültigen Rollen dieser Applikation zurück
	 *
	 * @return	{@link ArgumentList.ValueSelection} der gültigen Rollen
	 */
	public ArgumentList.ValueSelection getValidRoles() {
		return validRoles;
	}

	/**
	 * Gibt die Liste der gültigen Optionen dieser Applikation zurück
	 *
	 * @return	{@link ArgumentList.ValueSelection} der gültigen Optionen
	 */
	public ArgumentList.ValueSelection getValidOptions() {
		return validOptions;
	}

	/**
	 * Gibt die Liste der gültigen Rollen dieser Applikation zurück
	 *
	 * @return	{@link ArgumentList.ValueSelection} der gültigen Rollen
	 */
	public ArgumentList.ValueSelection getValidTimeOptions() {
		return validTimeOptions;
	}

	/**
	 * Registrierung durchführen
	 *
	 * @param	sender				{@link de.bsvrz.dav.daf.main.ClientSenderInterface} mit Referenz
	 *								auf die Senderapplikation
	 * @param	subscriptionInfos	{@link List} mit den Anmeldeinformationen
	 * @param	dataModel			{@link DataModel}, welches verwendet wird
	 * @param	isAutarkic			boolean; 1: Datengenerator arbeitet autark,
	 *								d. h. es ist keine Anmeldung der Objekte
	 *								notwendig; 0: Objekte müssen beim DaV
	 *								angemeldet werden.
	 * @param	connection			{@link ClientDavInterface Verbindung} zum
	 *								DaV
	 * @return						List mit allen angemeldeten Objekten
	 * @throws						Exception bei Problemen
	 */
	public List
	register(ClientSenderInterface sender, List subscriptionInfos,
			 DataModel dataModel, boolean isAutarkic,
			 ClientDavInterface connection)
	throws Exception {
		List allObjects = new LinkedList();
		Iterator infoIterator= subscriptionInfos.iterator();
		while(infoIterator.hasNext()) {
			SubscriptionInfo subscriptionInfo= (SubscriptionInfo)infoIterator.next();
			List<SystemObject> objects= new LinkedList<SystemObject>();
			String objectSpec = subscriptionInfo.getObjectSpec();
			objects = ConfigurationHelper.getObjects(objectSpec, dataModel);
			String[] dataSpecs = subscriptionInfo.getSplittedData(":",3);
			AttributeGroup attributeGroup= dataModel.getAttributeGroup(dataSpecs[0]);
			Aspect aspect= dataModel.getAspect(dataSpecs[1]);
			DataDescription dataDescription;
			String simulationVariantText= "";
			if(dataSpecs.length == 3) {
				short simulationVariant= Short.parseShort(dataSpecs[2]);
				simulationVariantText= ":" + simulationVariant;
				dataDescription= new DataDescription(attributeGroup, aspect, simulationVariant);
			}
			else dataDescription= new DataDescription(attributeGroup, aspect);
;
			debug.config("Anmeldung als " + subscriptionInfo.getSenderRole()
						 + " für " + attributeGroup.getNameOrPidOrId() + ":"
						 + aspect.getNameOrPidOrId());
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

			if (!isAutarkic) {
				connection.subscribeSender(
					sender,
					objects,
					dataDescription,
					subscriptionInfo.getSenderRole()
				);
			}

			/*
			 * XXX
			 * Work around (s. o.)
			 */
			dataDescription.setSimulationVariant(rescueSimulationVariant);

			allObjects.addAll(objects);

			subscriptionInfo.setObjects(objects);
			subscriptionInfo.setDataDescription(dataDescription);
		}
		infoIterator= subscriptionInfos.iterator();
		debug.fine("Start der Datengenerierung-Threads");
		return allObjects;
	}

}
