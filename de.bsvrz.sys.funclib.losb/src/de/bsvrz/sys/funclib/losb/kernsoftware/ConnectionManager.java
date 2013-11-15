/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.losb.kernsoftware;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.dataIdentificationSettings.DataIdentification;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.exceptions.FailureException;
import de.bsvrz.sys.funclib.losb.exceptions.LoggerException;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Verwaltet die An- und Abmeldungen als Sender / Empf�nger beim Dav. Verhindert, dass die gleichen Datenidentifikationen mehrfach zum Senden / Empfangen
 * angemeldet werden. Au�erdem werden Abmeldungen erst dann durchgef�hrt, wenn es keinen Sender / Empf�nger mehr gibt. Mehrfachanmeldungen eines Senders /
 * Empf�ngers f�r die gleiche Datenidentifikation sind nicht m�glich, auch wenn er sich mit unterschiedlichen Rollen anmeldet.<br> Die Anmeldungen werden nach
 * Datenverteilerverbindung getrennt verwaltet.<br> Mittels {@link #noSubscriptions(ClientDavInterface)} kann gepr�ft werden, ob es noch offene Anmeldungen
 * gibt.<br> Alle Methoden sind Threadsafe.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision: 8537 $ / $Date: 2011-01-04 16:11:56 +0100 (Tue, 04 Jan 2011) $ / ($Author: jh $)
 */
public class ConnectionManager {

	/** Debug Ausgabe */
	private static final Debug debug = Debug.getLogger();

	/** Abbildung Datenidentifikation -> Anzahl angemeldeter Empf�nger, zugeordnete Empf�ngerobjekte. (Keine Senken, nur normale Empf�nger!) */
	private Hashtable<DataIdentification, List<ClientReceiverInterface>> receivers = new Hashtable<DataIdentification, List<ClientReceiverInterface>>();

	/** Abbildung Datenidentifikation -> Anzahl angemeldeter Senken, zugeordnete Empf�ngerobjekte. */
	private Hashtable<DataIdentification, CMDrain> drains = new Hashtable<DataIdentification, CMDrain>();

	/** Abbildung Datenidentifikation -> Anzahl angemeldeter Sender, zugeordnete Senderobjekte. */
	private Hashtable<DataIdentification, CMSender> senders = new Hashtable<DataIdentification, CMSender>();

	/** Ordnet jeder Datenverteiler-Verbindung einen eigenen connectionManager zu. */
	private static Hashtable<ClientDavInterface, ConnectionManager> connectionManagers = new Hashtable<ClientDavInterface, ConnectionManager>();

	/** Gibt den Status aus. Loglevel ist info */
	public static void printStatus() {
		debug.info("Anzahl CM: " + connectionManagers.size());
		int i = 0;
		for(ConnectionManager cm : connectionManagers.values()) {
			debug.info(
					"CM_" + i + Debug.NEWLINE
					+ "  Anzahl Sender:     " + cm.senders.size() + Debug.NEWLINE
					+ "  Anzahl Senken:     " + cm.drains.size() + Debug.NEWLINE
					+ "  Anzahl Empfaenger: " + cm.receivers.size()
			);
		}
	}

	/**
	 * Liefert den der Datenverteilerverbindung zugeordneten ConnectionManager zur�ck. Falls noch kein ConnectionManager existiert, wird er angelegt.
	 *
	 * @param dav Datenverteilerverbindung.
	 *
	 * @return Zugeordneter ConnectionManager.
	 */
	private synchronized static ConnectionManager getConnectionManager(ClientDavInterface dav) {
		ConnectionManager cm = null;
		cm = connectionManagers.get(dav);
		if(cm == null) {
			cm = new ConnectionManager();
			connectionManagers.put(dav, cm);
		}
		return cm;
	}

	/**
	 * Entfernt einen ConnectionManager aus der Menge der ConnectionManager ({@link #connectionManagers}) falls er keine Anmeldungen mehr zu verwalten hat.
	 *
	 * @param dav
	 */
	private synchronized static void eventuallyRemoveConnectionManager(ClientDavInterface dav) {
		ConnectionManager cm = connectionManagers.get(dav);
		if(cm != null && cm.receivers.isEmpty() && cm.senders.isEmpty() && cm.drains.isEmpty()) connectionManagers.remove(dav);
	}

	/**
	 * Anmeldung zum Empfangen von Daten. Die Anmeldung wird nur durchgef�hrt, falls die �bergebene Datenidentifikation noch nicht angemeldet wurde.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empf�nger
	 * @param object          Objekt. Objekt-Teil der Datenidentifikation.
	 * @param dataDescription Datenbeschreibung. Attributgruppe und Aspekt der Datenidentifikation.
	 * @param options         Empfangsoptionen. Delta oder Normaldaten.
	 * @param role            Anmeldung als Empf�nger oder Senke.
	 *
	 * @throws ConfigurationException Fehler bei der Kommunikation mit der Konfiguration.
	 * @throws FailureException       Mehrfach-Anmeldung des gleichen Receivers
	 * @see ClientDavInterface#subscribeReceiver(ClientReceiverInterface,SystemObject,DataDescription,ReceiveOptions,ReceiverRole)
	 */
	public synchronized static void subscribeReceiver(
			ClientDavInterface dav,
			ClientReceiverInterface receiver,
			SystemObject object,
			DataDescription dataDescription,
			ReceiveOptions options,
			ReceiverRole role) throws ConfigurationException, FailureException {
		ConnectionManager cm = getConnectionManager(dav);
		DataIdentification di = getDID(dav, object, dataDescription);
		if(role.equals(ReceiverRole.receiver())) {
			List<ClientReceiverInterface> v = cm.receivers.get(di);

			// Anmeldung als normaler Empf�nger

			if(v != null) {
				if(!v.contains(receiver))	//nur Z�hler erh�hen, wenn es f�r diesen Empf�nger & diese DI noch keine Anmeldung gab.
				{
					dav.subscribeReceiver(receiver, object, dataDescription, options, ReceiverRole.receiver());
					v.add(receiver);
				}
				else {
					throw new FailureException("Mehrfachanmeldung des gleichen Receivers: " + receiver, LoggerException.OTHER);
				}
			}
			else {
				dav.subscribeReceiver(receiver, object, dataDescription, options, ReceiverRole.receiver());
				v = new ArrayList<ClientReceiverInterface>();
				v.add(receiver);
				cm.receivers.put(di, v);		//wird nur gesetzt, falls die Anmeldung keine Exception wirft.
			}
		}
		else {

			// Anmeldung als Senke

			CMDrain v = cm.drains.get(di);
			if(v != null) {
				if(!v.contains(receiver))	//nur Z�hler erh�hen, wenn es f�r diese Senke & diese DI noch keine Anmeldung gab.
				{
					v.add(receiver);
				}
				else {
					throw new FailureException("Mehrfachanmeldung der gleichen Senke: " + receiver, LoggerException.OTHER);
				}
			}
			else {
				v = new CMDrain(di);
				dav.subscribeReceiver(v, object, dataDescription, options, ReceiverRole.drain());
				cm.drains.put(
						di, v
				);	//WICHTIG: Erst in Hashtable ablegen!
				v.add(receiver);
			}
		}
	}

	/**
	 * Anmeldung zum Empfangen von Daten. Angemeldet wird auf den Konfigurationsverantwortlichen unter <code>ReceiveOptions.normal()</code> und
	 * <code>ReceiverRole.drain()</code>.
	 *
	 * @param dav      Verbindung zum Datenverteiler
	 * @param receiver Empf�nger
	 * @param atgPid   Pid der Attributgruppe
	 * @param aspPid   Pis des Aspektes
	 *
	 * @throws ConfigurationException
	 * @throws FailureException
	 */
	public synchronized static void subscrDrainNormal(ClientDavInterface dav, ClientReceiverInterface receiver, SystemObject so, String atgPid, String aspPid)
			throws ConfigurationException, FailureException {
		subscribeReceiver(
				dav,
				receiver,
				so,
				new DataDescription(dav.getDataModel().getAttributeGroup(atgPid), dav.getDataModel().getAspect(aspPid)),
				ReceiveOptions.normal(),
				ReceiverRole.drain()
		);
	}

	/**
	 * Anmeldung zum Empfangen von Daten. Angemeldet wird auf den Konfigurationsverantwortlichen unter <code>ReceiveOptions.normal()</code> und
	 * <code>ReceiverRole.receiver()</code>.
	 *
	 * @param dav      Verbindung zum Datenverteiler
	 * @param receiver Empf�nger
	 * @param atgPid   Pid der Attributgruppe
	 * @param aspPid   Pis des Aspektes
	 *
	 * @throws ConfigurationException
	 * @throws FailureException
	 */
	public synchronized static void subscrRecNormal(ClientDavInterface dav, ClientReceiverInterface receiver, SystemObject so, String atgPid, String aspPid)
			throws ConfigurationException, FailureException {
		subscribeReceiver(
				dav,
				receiver,
				so,
				new DataDescription(dav.getDataModel().getAttributeGroup(atgPid), dav.getDataModel().getAspect(aspPid)),
				ReceiveOptions.normal(),
				ReceiverRole.receiver()
		);
	}

	/**
	 * Meldet den Empfang von Daten ab, die aud dem Konfigurationsverantwortlichen angemeldet waren.
	 *
	 * @param dav
	 * @param receiver
	 * @param atgPid
	 * @param aspPid
	 *
	 * @throws ConfigurationException
	 * @throws FailureException
	 */
	public synchronized static void unsubscribeReceiver(ClientDavInterface dav, ClientReceiverInterface receiver, SystemObject so, String atgPid, String aspPid)
			throws ConfigurationException {
		unsubscribeReceiver(
				dav, receiver, so, new DataDescription(dav.getDataModel().getAttributeGroup(atgPid), dav.getDataModel().getAspect(aspPid))
		);
	}

	/**
	 * Meldet den Empfang von Daten ab. Die Abmeldung wird nur durchgef�hrt, wenn es keine weiteren Abnehmer f�r die Daten gibt.
	 *
	 * @param dav             Verbindung zum Datenverteiler.
	 * @param receiver        Empf�nger
	 * @param object          Objekt. Objekt-Teil der Datenidentifikation.
	 * @param dataDescription Datenbeschreibung. Attributgruppe und Aspekt der Datenidentifikation.
	 *
	 * @throws ConfigurationException Fehler bei der Kommunikation mit der Konfiguration.
	 * @see ClientDavInterface#unsubscribeReceiver(ClientReceiverInterface,SystemObject,DataDescription)
	 */
	public synchronized static void unsubscribeReceiver(
			ClientDavInterface dav, ClientReceiverInterface receiver, SystemObject object, DataDescription dataDescription) throws ConfigurationException {
		ConnectionManager cm = getConnectionManager(dav);
		DataIdentification di = getDID(dav, object, dataDescription);

		// Sowohl als Empf�nger als auch als Senke abmelden. Einer dieser Bereiche wird nichts bewirken, das schadet aber nicht.
		final List<ClientReceiverInterface> v = cm.receivers.get(di);
		if(v != null && v.contains(receiver)) {
			dav.unsubscribeReceiver(receiver, object, dataDescription);
			if(v.size() == 1) {
				cm.receivers.remove(di);	//wird nur gesetzt falls die Abmeldung keine Exception wirft.
				debug.finest("Alle Empf�nger entfernt fuer: ", di);
				eventuallyRemoveConnectionManager(dav);	// muss das letzte Statement sein, da viele Methoden als Nebeneffekt cm's anlegen
			}
			else {
				v.remove(receiver);		//wird nicht in 'if' entfernt, denn sonst w�rde es auch entfernt werden, wenn unsubscribeReceiver fehlschl�gt!
			}
		}

		final CMDrain drain = cm.drains.get(di);
		if(drain != null && drain.contains(receiver)) {
			if(drain.size() == 1) {
				dav.unsubscribeReceiver(drain, object, dataDescription);
				debug.finest("Alle Senken abgemeldet fuer: ", di);
				cm.drains.remove(di);	//wird nur gesetzt falls die Abmeldung keine Exception wirft.
				eventuallyRemoveConnectionManager(dav);	// muss das letzte Statement sein, da viele Methoden als Nebeneffekt cm's anlegen
			}
			else {
				drain.remove(receiver);	//wird nicht in 'if' entfernt, denn sonst w�rde es auch entfernt werden, wenn unsubscribeReceiver fehlschl�gt!
			}
		}
	}

	/**
	 * Anmeldung zum Senden von Daten. Die Anmeldung wird nur durchgef�hrt, falls die �bergebene Datenidentifikation noch nicht angemeldet wurde. Wenn sich ein
	 * Sender f�r die gleiche Datenidentifikation anmeldet, so wird nur die erste Anmeldung durchgef�hrt. Die weiteren Anmeldungen werden NICHT durchgef�hrt.
	 * <code>ConnectionManager</code> ruft die {@link ClientSenderInterface#dataRequest(SystemObject,DataDescription,byte)} Methode mit dem zuletzt g�ltigen Wert
	 * der Sendesteuerung auf. Dies ist n�tig, falls der Sender, der sich anmelden will, vor dem Senden auf eine positive Sendesteuereung wartet. (Falls der Sender
	 * die Sendesteuerung nicht benutzt, wird diese auch nicht aufgerufen.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param sender          Sender.
	 * @param object          Objekt. Objekt-Teil der Datenidentifikation.
	 * @param dataDescription Datenbeschreibung. Attributgruppe und Aspekt der Datenidentifikation.
	 * @param role            Anmeldung als Sender oder Quelle.
	 *
	 * @throws ConfigurationException     Fehler bei der Kommunikation mit der Konfiguration.
	 * @throws OneSubscriptionPerSendData Falls bereits eine Anmeldung f�r diese Datenidentifikation existiert. Kann auftreten, wenn Anmeldungen zum Senden nicht
	 *                                    nur durch <code>ConnectionManager</code> durchgef�hrt werden.
	 * @see ClientDavInterface#subscribeSender(ClientSenderInterface,SystemObject,DataDescription,SenderRole)
	 */
	public synchronized static void subscribeSender(
			ClientDavInterface dav, ClientSenderInterface sender, SystemObject object, DataDescription dataDescription, SenderRole role)
			throws ConfigurationException, OneSubscriptionPerSendData {
		ConnectionManager cm = getConnectionManager(dav);
		DataIdentification di = getDID(dav, object, dataDescription);
		CMSender v = cm.senders.get(di);
		if(v != null) {
			if(!v.contains(sender))	//nur Z�hler erh�hen, wenn es f�r diesen Sender & diese DI noch keine Anmeldung gab.
			{
				v.add(sender);
			}
			else {
				throw new OneSubscriptionPerSendData("Mehrfachanmeldung eines Senders: " + sender);
			}
		}
		else {
			v = new CMSender(di);
			dav.subscribeSender(v, object, dataDescription, role);
			cm.senders.put(
					di, v
			);	//WICHTIG: Erst in Hashtable ablegen! Sonst kann u.u. folgendes passieren: add ruft dataRequest auf, und dataRequest versucht den Sender abzumelden. Da dieser aber noch nicht in der Liste steht, wird er nicht abgemeldet. Erst jetzt wird add aufgerufen -> sender wird in hashtable eingetragen...
			v.add(sender);
		}
	}


	/**
	 * Meldet das Senden von Daten ab. Die Abmeldung wird nur durchgef�hrt, wenn es keine weiteren Sender f�r die Daten gibt.
	 *
	 * @param dav             Verbindung zum Datenverteiler.
	 * @param sender          Sender.
	 * @param object          Objekt. Objekt-Teil der Datenidentifikation.
	 * @param dataDescription Datenbeschreibung. Attributgruppe und Aspekt der Datenidentifikation.
	 *
	 * @throws ConfigurationException Fehler bei der Kommunikation mit der Konfiguration.
	 * @see ClientDavInterface#unsubscribeSender(ClientSenderInterface,SystemObject,DataDescription)
	 */
	public synchronized static void unsubscribeSender(
			ClientDavInterface dav, ClientSenderInterface sender, SystemObject object, DataDescription dataDescription) throws ConfigurationException {
		ConnectionManager cm = getConnectionManager(dav);
		DataIdentification di = getDID(dav, object, dataDescription);
		CMSender v = cm.senders.get(di);
		if(v != null && v.contains(sender)) {
			if(v.size() == 1) {
				dav.unsubscribeSender(v, object, dataDescription);
				debug.finest("Alle Sender abgemeldet fuer: ", di);
				cm.senders.remove(di);	//wird nur gesetzt falls die Abmeldung keine Exception wirft.
				eventuallyRemoveConnectionManager(dav);	// muss das letzte Statement sein, da viele Methoden als Nebeneffekt cm's anlegen
			}
			else {
				v.remove(sender);	//wird nicht in 'if' entfernt, denn sonst w�rde es auch entfernt werden, wenn unsubscribeReceiver fehlschl�gt!
			}
		}
	}

	/**
	 * Loescht die Tabelle, in der alle Anmeldungen vermerkt sind. Dies ist bei einem DAV-Absturz Anotwendig, wenn keine Zeit zum Abmelden mehr war.
	 *
	 * @param dav Verbindung zum Datenverteiler.
	 */
	public synchronized static void resetSubscriptionMarkers(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		cm.senders.clear();
		cm.receivers.clear();
		cm.drains.clear();
	}


	/**
	 * Liefert die Datenidentifikation zur�ck. �ndert die Simulationsvariante! Sie wird immer auf einen Wert != NO_SIMULATIONVARIANT_SET gesetzt. Ist n�tig, da
	 * Operationen wie <code>subscribeReceiver</code> den Wert der SimVar �ndern!
	 *
	 * @param dav             Dav
	 * @param object          Objekt
	 * @param dataDescription Datenbeschreibung.
	 *
	 * @return Datenidentifikation mit gesetzter Simulationsvariante.
	 */
	private static DataIdentification getDID(ClientDavInterface dav, SystemObject object, DataDescription dataDescription) {
		if(dataDescription.getSimulationVariant() == DataDescription.NO_SIMULATION_VARIANT_SET) {
			dataDescription.setSimulationVariant(dav.getClientDavParameters().getSimulationVariant());
		}

		return new DataIdentification(object, dataDescription);
	}

	/**
	 * @param dav Verbindung zum Datenverteiler.
	 *
	 * @return Anzahl der registrierten Sender / Quellen.
	 */
	public synchronized static int numberSenders(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		int result = 0;
		for(CMSender s : cm.senders.values()) {
			result += s.size();
		}
		return result;
	}

	/**
	 * @param dav Verbindung zum Datenverteiler.
	 *
	 * @return Anzahl der registrierten Empf�nger / Senken.
	 */
	public synchronized static int numberReceivers(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		int result = 0;
		for(List<ClientReceiverInterface> r : cm.receivers.values()) {
			result += r.size();
		}
		for(final CMDrain drain : cm.drains.values()) {
			result += drain.size();
		}
		return result;
	}


	/**
	 * @param dav Verbindung zum Datenverteiler.
	 *
	 * @return <code>true</code> falls es keine Anmeldungen mehr gibt. <code>false</code> sonst.
	 */
	public synchronized static boolean noSubscriptions(ClientDavInterface dav) {
		return numberSenders(dav) + numberReceivers(dav) == 0;
	}

	/**
	 * Meldet alle Sender und Empf�nger ab.
	 *
	 * @param dav Verbindung zum Datenverteiler
	 */
	public synchronized static void unsubscribeAll(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		debug.info(
				"Bereite Herunterfahren vor..." + Debug.NEWLINE + "Es gibt noch " + numberSenders(dav) + " angemeldete Sender." + Debug.NEWLINE
				+ "Es gibt noch " + numberReceivers(dav) + " angemeldete Empf�nger."
		);

		Hashtable<DataIdentification, List<ClientReceiverInterface>> localReceivers = (Hashtable<DataIdentification, List<ClientReceiverInterface>>)cm.receivers.clone();
		Hashtable<DataIdentification, CMDrain> localDrains = (Hashtable<DataIdentification, CMDrain>)cm.drains.clone();
		Hashtable<DataIdentification, CMSender> localSenders = (Hashtable<DataIdentification, CMSender>)cm.senders.clone();

		for(DataIdentification di : localSenders.keySet()) {
			for(ClientSenderInterface sender : localSenders.get(di).elements()) {
				try {
					unsubscribeSender(dav, sender, di.getObject(), di.getDataDescription());
				}
				catch(ConfigurationException e) {
					debug.warning(ErrorMessage.CAN_NOT_UNSUBSCRIBE + sender + ", " + di);
				}
			}
		}
		for(DataIdentification di : localDrains.keySet()) {
			for(ClientReceiverInterface drain : localDrains.get(di).elements()) {
				try {
					unsubscribeReceiver(dav, drain, di.getObject(), di.getDataDescription());
				}
				catch(ConfigurationException e) {
					debug.warning("Kann Senke nicht abmelden: " + drain + ", " + di);
				}
			}
		}

		for(DataIdentification di : localReceivers.keySet()) {
			for(ClientReceiverInterface receiver : localReceivers.get(di)) {
				try {
					unsubscribeReceiver(dav, receiver, di.getObject(), di.getDataDescription());
				}
				catch(ConfigurationException e) {
					debug.warning("Kann Empf�nger nicht abmelden: " + receiver + ", " + di);
				}
			}
		}
		eventuallyRemoveConnectionManager(dav);	// muss das letzte Statement sein, da viele Methoden als Nebeneffekt cm's anlegen
	}


	/**
	 * @param dav Verbindung zum Datenverteiler.
	 *
	 * @return Liefert Informationen �ber alle Empf�nger.
	 */
	public synchronized static List<String> getReceiverInformation(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		ArrayList<String> result = new ArrayList<String>();
		for(DataIdentification di : cm.receivers.keySet()) {
			String s = di.getObject() + ":" + di.getDataDescription().getAttributeGroup() + ":" + di.getDataDescription().getAspect() + ":"
			           + di.getDataDescription().getSimulationVariant() + ": Anzahl:" + cm.receivers.get(di).size();
			result.add(s);
		}
		for(DataIdentification di : cm.drains.keySet()) {
			String s = di + ": ";
			for(ClientReceiverInterface drain : cm.drains.get(di).elements()) {
				s += "\n\t" + drain.toString() + ", ";
			}
			result.add(s);
		}
		return result;
	}


	/**
	 * @param dav Verbindung zum Datenverteiler.
	 *
	 * @return Liefert Informationen �ber alle Sender.
	 */
	public synchronized static List<String> getSenderInformation(ClientDavInterface dav) {
		ConnectionManager cm = getConnectionManager(dav);
		ArrayList<String> result = new ArrayList<String>();
		for(DataIdentification di : cm.senders.keySet()) {
			String s = di + ": ";
			for(ClientSenderInterface sender : cm.senders.get(di).elements()) {
				s += "\n\t" + sender.toString() + ", ";
			}
			result.add(s);
		}
		return result;
	}
}

class CMSender implements ClientSenderInterface {

	/** Sendesteuerung wurde noch nicht aufgerufen. */
	public static final byte NOT_SET_YET = -1;

	/** Letzter Wert der Sendesteuerung */
	private byte state = NOT_SET_YET;

	/** Datenidentifikation f�r die das Objekt als Sender registriert wird. */
	private DataIdentification di;

	/** Liste mit angemeldeten Sendern. */
	private final List<ClientSenderInterface> senders;

	/** @param di Datenidentifikation f�r die das Objekt als Sender registriert wird. */
	public CMSender(DataIdentification di) {
		this.di = di;
		senders = new CopyOnWriteArrayList<ClientSenderInterface>();
	}

	/**
	 * @param sender Sender
	 *
	 * @return Ergebnis
	 *
	 * @see List#remove(java.lang.Object)
	 */
	public boolean remove(ClientSenderInterface sender) {
		synchronized(senders) {
			return senders.remove(sender);
		}
	}


	/** @return Anzahl der registrierten Sender. */
	public int size() {
		synchronized(senders) {
			return senders.size();
		}
	}

	/** @see ClientSenderInterface#dataRequest(SystemObject,DataDescription,byte) */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		this.state = state;

		List<ClientSenderInterface> clone = null;
		synchronized(senders) {
			clone = new ArrayList<ClientSenderInterface>(senders);
		}
		for(ClientSenderInterface s : clone) {
			s.dataRequest(di.getObject(), di.getDataDescription(), state);
		}
	}

	/**
	 * @param sender Sender
	 *
	 * @return Ergebnis
	 *
	 * @see List#add(java.lang.Object)
	 */
	public boolean add(ClientSenderInterface sender) {
		boolean result;
		synchronized(senders)						//ERST senders.add! Weil: in sender.dataRequest wird ConnectionManager.unsubscribe aufgerufen, und die versucht den sender aus der Liste zu l�schen!
		{
			result = senders.add(sender);
		}
		if(state != NOT_SET_YET) sender.dataRequest(di.getObject(), di.getDataDescription(), state);
		return result;
	}

	/**
	 * @param sender Sender
	 *
	 * @return Ergebnis
	 *
	 * @see List#contains(java.lang.Object)
	 */
	public boolean contains(ClientSenderInterface sender) {
		synchronized(senders) {
			return senders.contains(sender);
		}
	}


	/** @return Liste der eingetragenen Sender. */
	List<ClientSenderInterface> elements() {
		return Collections.unmodifiableList(senders);
	}

	/** @see ClientSenderInterface#isRequestSupported(SystemObject,DataDescription) */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return true;
	}

	/** @return Returns the state. */
	public byte getState() {
		return state;
	}
}

class CMDrain implements ClientReceiverInterface {

	/** Datenidentifikation f�r die das Objekt als Senke registriert wird. */
	private DataIdentification di;

	/** Liste mit angemeldeten Empf�ngern. */
	private final List<ClientReceiverInterface> receivers;

	/** @param di Datenidentifikation f�r die das Objekt als Senke registriert wird. */
	public CMDrain(DataIdentification di) {
		this.di = di;
		receivers = new CopyOnWriteArrayList<ClientReceiverInterface>();
	}

	/**
	 * @param drain Senke
	 *
	 * @return Ergebnis
	 *
	 * @see List#remove(java.lang.Object)
	 */
	public boolean remove(ClientReceiverInterface drain) {
		synchronized(receivers) {
			return receivers.remove(drain);
		}
	}


	/** @return Anzahl der registrierten Senke. */
	public int size() {
		synchronized(receivers) {
			return receivers.size();
		}
	}


	/**
	 * @param drain Sender
	 *
	 * @return Ergebnis
	 *
	 * @see List#add(java.lang.Object)
	 */
	public boolean add(final ClientReceiverInterface drain) {
		final boolean result;
		synchronized(receivers)
		{
			result = receivers.add(drain);
		}
		drain.update(new ResultData[]{new ResultData(di.getObject(), di.getDataDescription(), System.currentTimeMillis(), null, false, DataState.NO_SOURCE)});
		return result;
	}

	/**
	 * @param drain Senke
	 *
	 * @return Ergebnis
	 *
	 * @see List#contains(java.lang.Object)
	 */
	public boolean contains(final ClientReceiverInterface drain) {
		synchronized(receivers) {
			return receivers.contains(drain);
		}
	}


	/** @return Liste der eingetragenen Senken. */
	List<ClientReceiverInterface> elements() {
		return Collections.unmodifiableList(receivers);
	}

	public void update(final ResultData[] results) {
		for(final ClientReceiverInterface receiver : receivers) {
			receiver.update(results);
		}
	}
}
