/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.losb.
 * 
 * de.bsvrz.sys.funclib.losb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.losb is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.sys.funclib.losb.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.sys.funclib.losb.kernsoftware;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.exceptions.FailureException;
import de.bsvrz.sys.funclib.losb.exceptions.LoggerException;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;
import de.bsvrz.sys.funclib.losb.util.Util;

/**
 * Vereinfacht das Senden von Daten über den Datenverteiler. Im Gegensatz zu {@link de.bsvrz.sys.funclib.losb.kernsoftware.SimpleSender} ist diese Klasse zum
 * Versand von Massendaten gedacht.
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 * @see SubscriptionManager
 */
public class Sender implements ClientSenderInterface {

	private static final boolean NOT_DELAYED = false;

	private static final boolean WAIT_FOR_SEND_CTRL = true;

	private static final boolean DONT_WAIT_FOR_SEND_CTRL = false;

	private static final byte NO_SENDCTRL_YET = -1;

	/** ATG & ASP */
	private DataDescription dataDescription;

	/** Empfängerobjekt */
	private SystemObject receiver;

	/** Verbindung zum Datenverteiler */
	private ClientDavInterface dav;

	/** false = Sendevorgang abbrechen, da negative Sendesteuerung. */
	private boolean canSend = false;

	private byte lastState = NO_SENDCTRL_YET;

	/** Debug-Ausgaben */
	private static final Debug debug = Debug.getLogger();

	protected Sender(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription) {
		this.dav = dav;
		this.receiver = receiver;
		this.dataDescription = dataDescription;
	}

	/**
	 * Erzeugt einen Sender.
	 *
	 * @param dav            Verbindung zum Datenverteiler.
	 * @param receiver       Empfänger.
	 * @param attributeGroup Attributgruppe.
	 * @param aspect         Aspekt.
	 * @param srole          Quelle oder 'einfahcer' Sender.
	 *
	 * @throws FailureException Fehler bei der Kommunikation mit der Konfiguration. Oder: Es existiert bereits eine Sendeanmeldung.
	 * @return Sender zum Versand von Daten
	 */
	public static Sender subscribe(ClientDavInterface dav, SystemObject receiver, String attributeGroup, String aspect, SenderRole srole)
			throws FailureException {
		try {
			DataModel model = dav.getDataModel();
			AttributeGroup atg = model.getAttributeGroup(attributeGroup);
			Aspect asp = model.getAspect(aspect);

			if(atg == null) {
				throw new FailureException(ErrorMessage.INVALID_PID + attributeGroup, LoggerException.WARNING);
			}
			else if(asp == null) throw new FailureException(ErrorMessage.INVALID_PID + aspect, LoggerException.WARNING);

			DataDescription dataDescription = new DataDescription(atg, asp);
			Sender sender = new Sender(dav, receiver, dataDescription);
			ConnectionManager.subscribeSender(dav, sender, receiver, dataDescription, srole);
			return sender;
		}
		catch(ConfigurationException e) {
			throw new FailureException(ErrorMessage.COMMUNICATION + Util.getStackTrace(e), e, LoggerException.WARNING);
		}
		catch(OneSubscriptionPerSendData e) {
			throw new FailureException(ErrorMessage.MULIPLE_SUBSCRIPTIONS, e, LoggerException.WARNING);
		}
	}

	/**
	 * Erzeugt einen einfachen Sender.
	 *
	 * @param dav            Verbindung zum Datenverteiler.
	 * @param sysObj         Empfänger.
	 * @param attributeGroup Attributgruppe-PID
	 * @param aspect         Aspekt-PID
	 *
	 * @throws FailureException Fehler bei der Kommunikation mit der Konfiguration. Oder: Es existiert bereits eine Sendeanmeldung.
	 * @return Sender zum Versand von Daten
	 */
	public static Sender subscribeSender(ClientDavInterface dav, SystemObject sysObj, String attributeGroup, String aspect) throws FailureException {
		return subscribe(dav, sysObj, attributeGroup, aspect, SenderRole.sender());
	}

	/**
	 * Erzeugt einen einfachen Sender.
	 *
	 * @param dav            Verbindung zum Datenverteiler.
	 * @param sysObj         Pid des Objekts, für das die Anmeldung gilt
	 * @param attributeGroup Attributgruppe-PID
	 * @param aspect         Aspekt-PID
	 *
	 * @throws FailureException Fehler bei der Kommunikation mit der Konfiguration. Oder: Es existiert bereits eine Sendeanmeldung.
	 * @return Sender zum Versand von Daten
	 */
	public static Sender subscribeSender(ClientDavInterface dav, String sysObj, String attributeGroup, String aspect) throws FailureException {
		try {
			return subscribe(dav, dav.getDataModel().getObject(sysObj), attributeGroup, aspect, SenderRole.sender());
		}
		catch(ConfigurationException e) {
			throw new FailureException(ErrorMessage.COMMUNICATION, e, LoggerException.ERROR);
		}
	}

	/**
	 * Erzeugt eine Quelle.
	 *
	 * @param dav            Verbindung zum Datenverteiler.
	 * @param sysObj         Objekt für das die Anmeldung gilt.
	 * @param attributeGroup Attributgruppe-PID
	 * @param aspect         Aspekt-PID
	 *
	 * @throws FailureException Fehler bei der Kommunikation mit der Konfiguration. Oder: Es existiert bereits eine Sendeanmeldung.
	 * @return Sender zum Versand von Daten
	 */
	public static Sender subscribeSource(ClientDavInterface dav, String sysObj, String attributeGroup, String aspect) throws FailureException {
		try {
			return subscribe(dav, dav.getDataModel().getObject(sysObj), attributeGroup, aspect, SenderRole.source());
		}
		catch(ConfigurationException e) {
			throw new FailureException(ErrorMessage.COMMUNICATION, e, LoggerException.ERROR);
		}
	}

	/**
	 * Meldet den Versand von Daten ab.
	 *
	 * @return <code>false</code> falls die Abmeldung nicht durchgeführt werden konnte.
	 */
	public boolean unsubscribe() {
		try {
			ConnectionManager.unsubscribeSender(dav, this, receiver, dataDescription);
			return true;
		}
		catch(ConfigurationException e) {
			debug.warning(ErrorMessage.CAN_NOT_UNSUBSCRIBE, e);
			return false;
		}
	}

	/**
	 * Sendet die spezifizierten Daten als "online aktuell" mit der aktuellen Systemzeit. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde. Dies muss
	 * nicht notwendigerweise eine positive Sendesteuerung sein.
	 *
	 * @param data Zu sendende Daten.
	 *
	 * @throws FailureException
	 * @return	<code>true</code> falls die Daten gesendet wurden, <code>false</code> falls der Versand von der Sendesteuerung gestoppt wurde.
	 */
	public boolean send(Data data) throws FailureException {
		return send(data, NOT_DELAYED);
	}

	/**
	 * Sendet die spezifizierten Daten mit der aktuellen Systemzeit. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde.  Dies muss nicht notwendigerweise
	 * eine positive Sendesteuerung sein.
	 *
	 * @param data    Zu sendende Daten.
	 * @param delayed falls wahr, werden Daten als nachgeliefert gekennzeichnet
	 *
	 * @throws FailureException
	 * @return	<code>true</code> falls die Daten gesendet wurden, <code>false</code> falls der Versand von der Sendesteuerung gestoppt wurde.
	 */
	public boolean send(Data data, boolean delayed) throws FailureException {
		return sendData(data, System.currentTimeMillis(), delayed, DONT_WAIT_FOR_SEND_CTRL);
	}

	/**
	 * Sendet die spezifizierten Daten. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde.
	 *
	 * @param data     Zu sendende Daten.
	 * @param dataTime Datenzeitstempel des zu sendenden Datensatzes
	 * @param delayed  falls wahr, werden Daten als nachgeliefert gekennzeichnet
	 *
	 * @throws FailureException
	 * @return	<code>true</code> falls die Daten gesendet wurden, <code>false</code> falls der Versand von der Sendesteuerung gestoppt wurde.
	 */
	public boolean send(Data data, long dataTime, boolean delayed) throws FailureException {
		return sendData(data, dataTime, delayed, DONT_WAIT_FOR_SEND_CTRL);
	}

	/**
	 * Sendet die spezifizierten Daten als "online aktuell" mit der aktuellen Systemzeit. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde. Falls die
	 * aktuelle Sendesteuerung negativ ist, blockiert die Methode bis zur naechsten positiven Sendesteuerung.
	 *
	 * @param data Zu sendende Daten.
	 *
	 * @throws FailureException
	 */
	public boolean sendIfPosSendCtrl(Data data) throws FailureException {
		return sendData(data, System.currentTimeMillis(), NOT_DELAYED, WAIT_FOR_SEND_CTRL);
	}

	/**
	 * Sendet die spezifizierten Daten mit der aktuellen Systemzeit. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde. Falls die aktuelle Sendesteuerung
	 * negativ ist, blockiert die Methode bis zur naechsten positiven Sendesteuerung.
	 *
	 * @param data    Zu sendende Daten.
	 * @param delayed falls wahr, werden Daten als nachgeliefert gekennzeichnet
	 *
	 * @throws FailureException
	 */
	public void sendIfPosSendCtrl(Data data, boolean delayed) throws FailureException {
		sendData(data, System.currentTimeMillis(), delayed, WAIT_FOR_SEND_CTRL);
	}

	/**
	 * Sendet die spezifizierten Daten mit der aktuellen Systemzeit. Blockiert bis erstmalig eine Sendesteuerung empfangen wurde. Falls die aktuelle Sendesteuerung
	 * negativ ist, blockiert die Methode bis zur naechsten positiven Sendesteuerung.
	 *
	 * @param data     Zu sendende Daten.
	 * @param dataTime Datenzeitstempel des zu sendenden Datensatzes
	 * @param delayed  falls wahr, werden Daten als nachgeliefert gekennzeichnet
	 *
	 * @throws FailureException
	 */
	public void sendIfPosSendCtrl(Data data, long dataTime, boolean delayed) throws FailureException {
		sendData(data, dataTime, delayed, WAIT_FOR_SEND_CTRL);
	}

	/**
	 * Sendet die spezifizierten Daten. Blockiert, bis erstmalig eine Sendesteuerung empfangen wurde.
	 *
	 * @param data               Zu sendende Daten.
	 * @param dataTime           Datenzeitstempel des zu sendenden Datensatzes
	 * @param delayed            falls wahr, werden Daten als nachgeliefert gekennzeichnet
	 * @param waitForPosSendCtrl falls wahr, wird auf pos. Sendesteuerung gewartet (falls aktuelle SendeSt. negativ)
	 *
	 * @throws FailureException
	 * @return	<code>true</code> falls die Daten gesendet wurden, <code>false</code> falls der Versand von der Sendesteuerung gestoppt wurde.
	 */
	private boolean sendData(Data data, long dataTime, boolean delayed, boolean waitForPosSendCtrl) throws FailureException {
		try {
			synchronized(this) {
				// Vollstaendig im sync-Block, damit die Variable canSend zwischen if... und return.. nicht
				// durch eine erneute Sendesteuerung veraendert werden kann.
				while(lastState == NO_SENDCTRL_YET || (waitForPosSendCtrl && !canSend)) wait();

				// Workaround: Bei Dav-Dav-Koppung besteht das Problem, dass zuerst eine negative Sendesteuerung kommt
				// und kurze Zeit später eine positive. Hier darf nach der ersten negativen Sendesteuerung nicht aufgehört werden
				// zu warten, sonst kommt keine Kommunikation zustande!
				if(!canSend) wait(5000);
				if(canSend) dav.sendData(new ResultData(receiver, dataDescription, dataTime, data, delayed));
				return canSend;
			}
		}
		catch(Exception e) {
			FailureException fe = new FailureException("Fehler beim Senden von " + receiver + "/" + dataDescription, e, LoggerException.WARNING);
			debug.warning("Fehler beim Senden von " + receiver + "/" + dataDescription, fe);
			throw fe;
		}
	}


	/**
	 * Sendet Daten ohne auf die Sendesteuerung zu achten. Je nachdem die Daten als Quelle oder Sender angemeldet wurden, kann dies eine Exception verursachen.
	 *
	 * @param data     Zu sendende Daten.
	 * @param dataTime Datenzeitstempel des zu sendenden Datensatzes
	 * @param delayed  wahr, falls Daten als nachgeliefert gekennzeichnet werden sollen
	 *
	 * @throws FailureException
	 */
	public void sendIgnoreSendControl(Data data, long dataTime, boolean delayed) throws FailureException {
		try {
			dav.sendData(new ResultData(receiver, dataDescription, dataTime, data, delayed));
		}
		catch(Exception e) {
			FailureException fe = new FailureException("Fehler beim Senden von " + receiver + "/" + dataDescription, e, LoggerException.WARNING);
			debug.warning("Fehler beim Senden von " + receiver + "/" + dataDescription, fe);
			throw fe;
		}
	}

	/** @see ClientSenderInterface#dataRequest(SystemObject,DataDescription,byte) */
	public void dataRequest(@SuppressWarnings("unused")SystemObject object, @SuppressWarnings("unused")DataDescription dataDescription, byte state) {
		synchronized(this) {
			canSend = state == ClientSenderInterface.START_SENDING;
			lastState = state;
			notifyAll();
		}
	}

	/** @return Liefert den zuletzt eingegangenen Status der Sendesteuerung. */
	public byte getLastState() {
		return lastState;
	}

	/** @see ClientSenderInterface#isRequestSupported(SystemObject,DataDescription) */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return true;
	}

	/** @return Returns the dav. */
	public ClientDavInterface getDav() {
		return dav;
	}

	/** @return Returns the receiver. */
	public SystemObject getReceiver() {
		return receiver;
	}

	/** @return Returns dataDescription. */
	public DataDescription getDataDescription() {
		return dataDescription;
	}
}
