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
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.exceptions.LoggerException;
import de.bsvrz.sys.funclib.losb.exceptions.SenderException;
import de.bsvrz.sys.funclib.losb.messages.ErrorMessage;
import de.bsvrz.sys.funclib.losb.util.Util;

/**
 * Klasse die das Senden immer unter Berücksichtigung der Sendesteuerung durchführt. Nach dem Senden der Daten meldet sich der Sender sofort beim Datenverteiler
 * ab. Falls keine Exception geworfen wird, und {@link #sent} dennoch false ist, dann gibt es keinen Abnehmer für die Daten. <br> <b>WICHTIG</b><br> Wenn
 * versucht wird, etwas ohne angemeldeten Empfänger Quelle zu senden, dann wird (korrekterweise) ein Fehler gemeldet. Problematisch wird es, wenn sofort danach
 * eine Quellen-/Empfängeranmeldung durchgeführt und das Senden wiederholt wird. <b>Dies führt zu einem Fehler<b>, da die Sendesteuerung das Senden immer noch
 * nicht gestattet. Daher sollte eine Pause zwischen zwei Sendeversuchen liegen. (Siehe JUnit-Tests).
 *
 * @author beck et al. projects GmbH
 * @author Martin Hilgers
 * @version $Revision$ / $Date$ / ($Author$)
 */
public class SimpleSender implements ClientSenderInterface {

	private Data data;

	private SystemObject receiver;

	private ClientDavInterface dav;

	private DataDescription dataDescription;

	private static final Debug debug = Debug.getLogger();

	/** Wird als Synchronisationspunkt verwendet. Wird von Sender mit sent.notifyAll() geweckt, nachdem die Daten gesendet wurden. */
	public Object lock;

	/**
	 * Wird nach dem Senden gesetzt. True, falls das Senden erfolgreich durchgeführt wurde. False sonst. Falls keine Exception geworfen wird, und sent dennoch
	 * false ist, dann gibt es keinen Abnehmer für die Daten.
	 */
	public boolean sent;

	/** Mit null oder einer Fehlermeldung belegt. */
	public String errorMsg;

	/** Zeigt ob die Methode {@link #dataRequest(SystemObject,DataDescription,byte)} bereits aufgerufen wurde. */
	private boolean dataRequestCalled = false;

	/**
	 * Erzeugt einen Sender und sendet die Daten als einfacher Sender. Alternativ kann auch ein neues Objekt erzeugt werden {@link
	 * SimpleSender#SimpleSender(ClientDavInterface,SystemObject,DataDescription,Data,boolean)} - das hat den gleichen Effekt.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 *
	 * @return Einfacher Sender.
	 *
	 * @throws SenderException Falls es bei der Sendeanmeldung zu einem Fehler kommt.
	 */
	public static SimpleSender send(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data) throws SenderException {
		SimpleSender sender = new SimpleSender(dav, receiver, dataDescription, data, false);
		return sender;
	}

	/**
	 * Erzeugt einen Sender und sendet die Daten als einfacher Sender. Die Methode wartet solange, bis die Daten gesendet wurden, oder es zu einem Fehler kam.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 * @param timeout         Maximale Wartezeit in ms. Bei <code>0</code> unbegrenzt.
	 *
	 * @return <code>true</code> falls das Senden erfolgreich durchgeführt wurde.
	 *
	 * @throws SenderException  Fehler beim Senden.
	 * @throws RuntimeException Kann bei Fehler geworfen werden.
	 */
	public static boolean sendWait(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data, long timeout)
			throws SenderException {
		return send(dav, receiver, dataDescription, data, false, timeout);
	}

	/**
	 * Erzeugt einen Sender und sendet die Daten als Quelle.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 *
	 * @return Quelle
	 *
	 * @throws SenderException Fehler beim Senden.
	 */
	public static SimpleSender source(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data) throws SenderException {
		SimpleSender sender = new SimpleSender(dav, receiver, dataDescription, data, true);
		return sender;
	}

	/**
	 * Erzeugt einen Sender und sendet die Daten als einfache Quelle. Die Methode wartet solange, bis die Daten gesendet wurden, oder es zu einem Fehler kam.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 * @param timeout         Maximale Wartezeit in ms. Bei <code>0</code> unbegrenzt.
	 *
	 * @return False, falls die Daten wegen negativer Sendesteuerung nicht gesendet wurden.
	 *
	 * @throws SenderException  Falls es beim Senden zu einem Fehler kommt.
	 * @throws RuntimeException Kann bei Fehler geworfen werden.
	 */
	public static boolean sourceWait(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data, long timeout)
			throws SenderException {
		return send(dav, receiver, dataDescription, data, true, timeout);
	}

	/** Initialisiert das Lock-Objekt. */
	private SimpleSender() {
		lock = new Object();
	}

	/**
	 * Erzeugt den Sender und sendet die Daten.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 * @param source          True wenn die Daten als Quelle versendet werden sollen. Ist source == false meldet sich {@link SimpleSender} als einfacher Sender
	 *                        an.
	 *
	 * @throws SenderException Falls es bei der Sendeanmeldung zu einem Fehler kommt.
	 */
	private SimpleSender(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data, boolean source) throws SenderException {
		this();
		init(dav, receiver, dataDescription, data, source);
	}

	/**
	 * Objektinitialisierung.
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 * @param source          True wenn die Daten als Quelle versendet werden sollen. Ist source == false meldet sich {@link SimpleSender} als einfacher Sender
	 *                        an.
	 *
	 * @throws SenderException Falls es bei der Sendeanmeldung zu einem Fehler kommt.
	 */
	private void init(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data, boolean source) throws SenderException {
		this.dav = dav;
		this.data = data;
		this.receiver = receiver;
		this.dataDescription = dataDescription;

		sent = false;
		errorMsg = null;

		SenderRole role = (source) ? SenderRole.source() : SenderRole.sender();
		try {
			ConnectionManager.subscribeSender(dav, this, receiver, dataDescription, role);
		}
		catch(OneSubscriptionPerSendData e)	//Es existiert bereits eine Anmeldung. Stört nicht weiter.
		{
			String msg = receiver.getNameOrPidOrId() + ":" + dataDescription;
			debug.warning(ErrorMessage.MULIPLE_SUBSCRIPTIONS + msg);
		}
		catch(ConfigurationException e) {
			debug.warning(ErrorMessage.COMMUNICATION, e);
		}
		catch(RuntimeException re) {
			throw new SenderException(ErrorMessage.CAN_NOT_SUBSCRIBE + Util.getStackTrace(re), LoggerException.WARNING);
		}
	}

	/**
	 * Erzeugt einen Sender und sendet die Daten. Die Methode wartet solange, bis die Daten gesendet wurden, oder es zu einem Fehler kam. Kann
	 * <code>RuntimException</code> werfen!
	 *
	 * @param dav             Verbindung zum Datenverteiler
	 * @param receiver        Empfänger der Daten
	 * @param dataDescription DataDescription der zu versendenden Daten
	 * @param data            Daten, die versendet werden sollen
	 * @param source          True wenn als Quelle gesendet werden soll. False, wenn als einfacher Sender gesendet werden soll.
	 * @param timeout         Maximale Wartezeit in ms. Bei <code>0</code> unbegrenzt.
	 *
	 * @return <code>true</code> falls das Senden erfolgreich durchgeführt wurde.
	 *
	 * @throws SenderException  Falls es während des Sendens zu einem Fehler kam.
	 * @throws RuntimeException Kann bei Fehler geworfen werden.
	 */
	private static boolean send(ClientDavInterface dav, SystemObject receiver, DataDescription dataDescription, Data data, boolean source, long timeout)
			throws SenderException {
		SimpleSender sender = new SimpleSender();
		try {
			sender.init(dav, receiver, dataDescription, data, source);
			synchronized(sender.lock) {
				while(!sender.dataRequestCalled) {
					sender.lock.wait(timeout);
				}

				// Falls negative Sendesteuerung auf positive Sendesteuerung warten
				// (bei Dav-Dav-Kopplung wichtig, da dort während noch nach Empfängern gesucht wurd u.U. eine kurze negative Sendesteuerugn eintrifft)
				if(!sender.sent) sender.lock.wait(5000);

				//warten, bis gesendet oder timeout
			}
			return sender.sent;
		}
		catch(InterruptedException e) {
			sender.errorMsg = Util.getStackTrace(e);
			return false;
		}
		catch(SenderException e) {
			sender.errorMsg = e.getMessage();
			return false;
		}
		finally {
			// Sender abmelden
			ConnectionManager.unsubscribeSender(dav, sender, receiver, dataDescription);
			//ggf. Fehlermeldung ausgeben (auch nicht-kritische)
			if(sender.errorMsg != null) {
				debug.fine(sender.errorMsg);
				//überprüfen, ob das Senden funktioniert hat
				if(!sender.sent) throw new SenderException(sender.errorMsg, LoggerException.WARNING);
			}
		}
	}

	/**
	 * Sendesteuerung. Callback Methode für den Datenverteiler. Wird der Versand von Daten erlaubt, so werden die Daten sofort abgesetzt. Danach meldet sich der
	 * Sender unverzüglich ab. Wird der Versand der Daten nicht erlaubt, so werden sie verworfen.
	 *
	 * @param object          Das in der zugehörigen Sendeanmeldung angegebene Objekt, auf das sich die Sendesteuerung bezieht.
	 * @param dataDescription Beschreibende Informationen zu den angemeldeten Daten auf die sich die Sendesteuerung bezieht.
	 * @param state           Status der Sendesteuerung. Kann einen der Werte START_SENDING, STOP_SENDING, STOP_SENDING_NO_RIGHTS,
	 *                        STOP_SENDING_NOT_A_VALID_SUBSCRIPTION enthalten.
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		try {
			sent = false;
			switch(state) {
				case ClientSenderInterface.START_SENDING:
					ResultData rd = new ResultData(receiver, this.dataDescription, System.currentTimeMillis(), data);
					try {
						dav.sendData(rd);
						sent = true;
					}
					catch(SendSubscriptionNotConfirmed e) {
						//sollte nie aufgerufen werden, da ja gerade überprüft wurde, ob Sender senden darf
						errorMsg = ErrorMessage.SENDING_NOT_ALLOWED + e.getMessage();
					}
					catch(RuntimeException e) {
						errorMsg = ErrorMessage.CAN_NOT_SEND + Util.getStackTrace(e);
					}
					break;
				case ClientSenderInterface.STOP_SENDING:
					sent = false;
					break;
				default:
					errorMsg = ErrorMessage.SENDING_NOT_ALLOWED + object.getNameOrPidOrId() + ":" + dataDescription + ", Sendsteuerung: " + state;
					sent = false;
			}
		}
		catch(ConfigurationException e) {
			errorMsg = ErrorMessage.COMMUNICATION + e.getMessage();
		}
		finally {
			synchronized(lock) {
				dataRequestCalled = true;
				lock.notifyAll();
			}
		}
	}

	/**
	 * Sendesteuerung ist erwünscht. Wird vom Datenverteiler aufgerufen. Liefert deshalb immer true zurück.
	 *
	 * @param object          Das in der zugehörigen Sendeanmeldung angegebene System-Objekt.
	 * @param dataDescription Die in der zugehörigen Sendeanmeldung angegebenen beschreibenden Informationen der angemeldeten Daten.
	 *
	 * @return <code>true</code>, falls Sendesteuerungen gewünscht sind, sonst <code>false</code>.
	 *
	 * @see ClientSenderInterface#isRequestSupported(SystemObject,DataDescription)
	 */
	public boolean isRequestSupported(@SuppressWarnings("unused")SystemObject object, @SuppressWarnings("unused")DataDescription dataDescription) {
		return true;
	}
}
