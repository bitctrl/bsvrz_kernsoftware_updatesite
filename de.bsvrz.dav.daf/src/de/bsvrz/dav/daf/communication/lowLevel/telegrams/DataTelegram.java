/*
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.communication.lowLevel.telegrams;

import de.bsvrz.dav.daf.communication.lowLevel.QueueableTelegram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * In dieser Klasse wird der Typ eines Telegramms definiert und Methoden zum Lesen und Schreiben deklariert. Es wird von zwei Verbindungsarten ausgegangen: von
 * DAV zu DAV und von DAF zu DAV.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */


public abstract class DataTelegram implements QueueableTelegram {

	/* Die DAF-DAV Typen */

	/** Telegrammtyp: Protokollversionsverhandlung */
	public static final byte PROTOCOL_VERSION_REQUEST_TYPE = 1;

	/** Telegrammtyp: Antwort auf die Protokollversionsverhandlung */
	public static final byte PROTOCOL_VERSION_ANSWER_TYPE = 2;

	/** Telegrammtyp: Authentifizierungsanfrage */
	public static final byte AUTHENTIFICATION_TEXT_REQUEST_TYPE = 3;

	/** Telegrammtyp: Antwort auf eine Anfrage eines Authentifizierungsschluessels */
	public static final byte AUTHENTIFICATION_TEXT_ANSWER_TYPE = 4;

	/** Telegrammtyp: Authentifizierungsanfrage */
	public static final byte AUTHENTIFICATION_REQUEST_TYPE = 5;

	/** Telegrammtyp: Antwort auf Authentifizierungsanfrage */
	public static final byte AUTHENTIFICATION_ANSWER_TYPE = 6;

	/** Telegrammtyp: Kommunikationsparameterverhandlung */
	public static final byte COM_PARAMETER_REQUEST_TYPE = 7;

	/** Telegrammtyp: Antwort auf Kommunikationsparameterverhandlung */
	public static final byte COM_PARAMETER_ANSWER_TYPE = 8;

	/** Telegrammtyp: Terminierungsbefehl */
	public static final byte TERMINATE_ORDER_TYPE = 9;

	/** Telegrammtyp: Schließungsnachricht */
	public static final byte CLOSING_TYPE = 10;

	/** Telegrammtyp: Sendedatenaufforderung */
	public static final byte REQUEST_SENDER_DATA_TYPE = 11;

	/** Telegrammtyp: Telegrammlaufzeitermittlungsanfrage */
	public static final byte TELEGRAM_TIME_REQUEST_TYPE = 12;

	/** Telegrammtyp: Telegrammlaufzeitermittlungsantwort */
	public static final byte TELEGRAM_TIME_ANSWER_TYPE = 13;

	/** Telegrammtyp: KeepAlive Telegramm */
	public static final byte KEEP_ALIVE_TYPE = 14;

	/** Telegrammtyp: Sendeanmeldung */
	public static final byte SEND_SUBSCRIPTION_TYPE = 15;

	/** Telegrammtyp: ReceiveSubscriptionTelegram: Empfangsanmeldung */
	public static final byte RECEIVE_SUBSCRIPTION_TYPE = 16;

	/** Telegrammtyp: Sendeabmeldung */
	public static final byte SEND_UNSUBSCRIPTION_TYPE = 17;

	/** Telegrammtyp: Empfangsabmeldung */
	public static final byte RECEIVE_UNSUBSCRIPTION_TYPE = 18;

	/** Telegrammtyp: Austausch von Datensätzen */
	public static final byte APPLICATION_DATA_TELEGRAM_TYPE = 19;

	/* Die DAV-DAV Typen */
	/** Telegrammtyp: Verhandlung der Protokollversion (Client) */
	public static final byte TRANSMITTER_PROTOCOL_VERSION_REQUEST_TYPE = 65;

	/** Telegrammtyp: Verhandlung der Protokollversion (Server). */
	public static final byte TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE = 66;

	/** Telegrammtyp:  Authentifizierungsbereitschaft */
	public static final byte TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE = 67;

	/** Telegrammtyp: Aufforderung zur Authentifizierung */
	public static final byte TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE = 68;

	/** Telegrammtyp: Übermittelung der Authentifizierungsdaten */
	public static final byte TRANSMITTER_AUTHENTIFICATION_REQUEST_TYPE = 69;

	/** Telegrammtyp: Informationen, die nach der erfolgreichen Authentifizierung übergeben werden */
	public static final byte TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE = 70;

	/** Telegrammtyp: Verhandlung der Verbindungsparameter (Client). */
	public static final byte TRANSMITTER_COM_PARAMETER_REQUEST_TYPE = 71;

	/** Telegrammtyp: Verhandlung der Verbindungsparameter (Server). */
	public static final byte TRANSMITTER_COM_PARAMETER_ANSWER_TYPE = 72;

	/** Telegrammtyp: Terminierung der Verbindung */
	public static final byte TRANSMITTER_TERMINATE_ORDER_TYPE = 73;

	/** Telegrammtyp: Abmeldung einer Datenverteiler-Datenverteiler-Verbindung */
	public static final byte TRANSMITTER_CLOSING_TYPE = 74;

	/** Telegrammtyp: Ermittlung der Telegrammlaufzeit */
	public static final byte TRANSMITTER_TELEGRAM_TIME_REQUEST_TYPE = 75;

	/** Telegrammtyp: Ermittlung der Telegrammlaufzeit */
	public static final byte TRANSMITTER_TELEGRAM_TIME_ANSWER_TYPE = 76;

	/** Telegrammtyp: Keep-Alive Telegramm */
	public static final byte TRANSMITTER_KEEP_ALIVE_TYPE = 77;

	/** Der Datenverteiler meldet Daten für Empfänger oder Sender an */
	public static final byte TRANSMITTER_DATA_SUBSCRIPTION_TYPE = 78;

	/** Der Datenverteiler quittiert eine an ihn gerichtete Datenanmeldung */
	public static final byte TRANSMITTER_DATA_SUBSCRIPTION_RECEIPT_TYPE = 79;

	/** Der Datenverteiler meldet Daten für Empfänger oder Sender bei einem anderen Datenverteiler ab */
	public static final byte TRANSMITTER_DATA_UNSUBSCRIPTION_TYPE = 80;

	/** Telegramm zur Übertragung der Anwendungsdaten */
	public static final byte TRANSMITTER_DATA_TELEGRAM_TYPE = 81;

	/** Telegramm zur Aktualisierung der Matrix der günstigsten Wege */
	public static final byte TRANSMITTER_BEST_WAY_UPDATE_TYPE = 82;

	/** Telegramm zur Anmeldung von Anmeldungslisten Abonnements */
	public static final byte TRANSMITTER_LISTS_SUBSCRIPTION_TYPE = 83;

	/** Telegrammtyp: Abmeldung von Anmeldungslisten Abonnements */
	public static final byte TRANSMITTER_LISTS_UNSUBSCRIPTION_TYPE = 84;

	/** Telegrammtyp: Kündigung von Anmeldungslisten Abonnements. */
	public static final byte TRANSMITTER_LISTS_DELIVERY_UNSUBSCRIPTION_TYPE = 85;

	/** Telegrammtyp: Änderungsmitteilung zu Anmeldungslisten. */
	public static final byte TRANSMITTER_LISTS_UPDATE_TYPE = 86;

	/** Telegrammtyp: Änderungsmitteilung zu Anmeldungslisten. */
	public static final byte TRANSMITTER_LISTS_UPDATE_2_TYPE = 87;

	/** Der Telegrammtyp. */
	protected byte type;

	/** Die Telegrammpriorität. */
	protected byte priority;

	/** Die Länge des Telegrams. */
	protected int length;

	/**
	 * Liest ein Telegramm vom übergegebenen DataInputStream.
	 *
	 * @param in Der DataInputStream.
	 *
	 * @throws java.io.IOException Falls der Datensatz nicht aus dem Stream gelesen werden kann.
	 */
	public abstract void read(DataInputStream in) throws IOException;

	/**
	 * Schreibt ein Telegramm in den übergegebenen DataOutputStream.
	 *
	 * @param out Der DataOutputStream.
	 *
	 * @throws java.io.IOException Falls der Datensatz nicht in den Stream geschrieben werden kann.
	 */
	public abstract void write(DataOutputStream out) throws IOException;

	/**
	 * Gibt eine String-Repräsentation dieses Datensatzes zurück.
	 *
	 * @return Eine String-Repräsentation dieses Datensatzes.
	 */
	public abstract String parseToString();

	/**
	 * Gibt die Länge des Telegramms an.
	 *
	 * @return Die Länge des Telegrams.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Liefert die Gesamtgröße des Telegramms einschließlich des führenden Typ-Bytes und der Länge des Telegramminhalts.
	 *
	 * @return Gesamtgröße des Telegrams in Bytes.
	 */
	public int getSize() {
		return 1 + 2 + length;
	}

	/**
	 * Gibt den Typ des Telegramms an.
	 *
	 * @return Typ des Telegramms.
	 */
	public final byte getType() {
		return type;
	}

	/**
	 * Gibt die Priorität des Telegramms an. Je größer der Wert, desto größer die Priorität.
	 *
	 * @return Priorität des Telegramms.
	 */
	public final byte getPriority() {
		return priority;
	}

	/**
	 * Erstellt ein leeres Telegramm vom Typ des Übergabeparameters.
	 *
	 * @param _type Typ eines Telegramms.
	 *
	 * @return gibt eine Instanz des von <code>DataTelegram</code> abgeleiteten Typs zurück.
	 */
	public static DataTelegram getTelegram(byte _type) {
		switch(_type) {
			case PROTOCOL_VERSION_REQUEST_TYPE: {
				return new ProtocolVersionRequest();
			}
			case PROTOCOL_VERSION_ANSWER_TYPE: {
				return new ProtocolVersionAnswer();
			}
			case AUTHENTIFICATION_TEXT_REQUEST_TYPE: {
				return new AuthentificationTextRequest();
			}
			case AUTHENTIFICATION_TEXT_ANSWER_TYPE: {
				return new AuthentificationTextAnswer();
			}
			case AUTHENTIFICATION_REQUEST_TYPE: {
				return new AuthentificationRequest();
			}
			case AUTHENTIFICATION_ANSWER_TYPE: {
				return new AuthentificationAnswer();
			}
			case COM_PARAMETER_REQUEST_TYPE: {
				return new ComParametersRequest();
			}
			case COM_PARAMETER_ANSWER_TYPE: {
				return new ComParametersAnswer();
			}
			case REQUEST_SENDER_DATA_TYPE: {
				return new RequestSenderDataTelegram();
			}
			case TELEGRAM_TIME_REQUEST_TYPE: {
				return new TelegramTimeRequest();
			}
			case TELEGRAM_TIME_ANSWER_TYPE: {
				return new TelegramTimeAnswer();
			}
			case APPLICATION_DATA_TELEGRAM_TYPE: {
				return new ApplicationDataTelegram();
			}
			case RECEIVE_SUBSCRIPTION_TYPE: {
				return new ReceiveSubscriptionTelegram();
			}
			case RECEIVE_UNSUBSCRIPTION_TYPE: {
				return new ReceiveUnsubscriptionTelegram();
			}
			case SEND_SUBSCRIPTION_TYPE: {
				return new SendSubscriptionTelegram();
			}
			case SEND_UNSUBSCRIPTION_TYPE: {
				return new SendUnsubscriptionTelegram();
			}
			case TERMINATE_ORDER_TYPE: {
				return new TerminateOrderTelegram();
			}
			case KEEP_ALIVE_TYPE: {
				return new KeepAliveTelegram();
			}
			case CLOSING_TYPE: {
				return new ClosingTelegram();
			}
			case TRANSMITTER_PROTOCOL_VERSION_REQUEST_TYPE: {
				return new TransmitterProtocolVersionRequest();
			}
			case TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE: {
				return new TransmitterProtocolVersionAnswer();
			}
			case TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE: {
				return new TransmitterAuthentificationTextRequest();
			}
			case TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE: {
				return new TransmitterAuthentificationTextAnswer();
			}
			case TRANSMITTER_AUTHENTIFICATION_REQUEST_TYPE: {
				return new TransmitterAuthentificationRequest();
			}
			case TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE: {
				return new TransmitterAuthentificationAnswer();
			}
			case TRANSMITTER_COM_PARAMETER_REQUEST_TYPE: {
				return new TransmitterComParametersRequest();
			}
			case TRANSMITTER_COM_PARAMETER_ANSWER_TYPE: {
				return new TransmitterComParametersAnswer();
			}
			case TRANSMITTER_TERMINATE_ORDER_TYPE: {
				return new TransmitterTerminateOrderTelegram();
			}
			case TRANSMITTER_CLOSING_TYPE: {
				return new TransmitterClosingTelegram();
			}
			case TRANSMITTER_TELEGRAM_TIME_REQUEST_TYPE: {
				return new TransmitterTelegramTimeRequest();
			}
			case TRANSMITTER_TELEGRAM_TIME_ANSWER_TYPE: {
				return new TransmitterTelegramTimeAnswer();
			}
			case TRANSMITTER_KEEP_ALIVE_TYPE: {
				return new TransmitterKeepAliveTelegram();
			}
			case TRANSMITTER_DATA_SUBSCRIPTION_TYPE: {
				return new TransmitterDataSubscription();
			}
			case TRANSMITTER_DATA_SUBSCRIPTION_RECEIPT_TYPE: {
				return new TransmitterDataSubscriptionReceipt();
			}
			case TRANSMITTER_DATA_UNSUBSCRIPTION_TYPE: {
				return new TransmitterDataUnsubscription();
			}
			case TRANSMITTER_DATA_TELEGRAM_TYPE: {
				return new TransmitterDataTelegram();
			}
			case TRANSMITTER_BEST_WAY_UPDATE_TYPE: {
				return new TransmitterBestWayUpdate();
			}
			case TRANSMITTER_LISTS_SUBSCRIPTION_TYPE: {
				return new TransmitterListsSubscription();
			}
			case TRANSMITTER_LISTS_UNSUBSCRIPTION_TYPE: {
				return new TransmitterListsUnsubscription();
			}
			case TRANSMITTER_LISTS_DELIVERY_UNSUBSCRIPTION_TYPE: {
				return new TransmitterListsDeliveryUnsubscription();
			}
			case TRANSMITTER_LISTS_UPDATE_TYPE: {
				return new TransmitterListsUpdate(TRANSMITTER_LISTS_UPDATE_TYPE);
			}
			case TRANSMITTER_LISTS_UPDATE_2_TYPE: {
				return new TransmitterListsUpdate(TRANSMITTER_LISTS_UPDATE_2_TYPE);
			}
			default: {
				return null;
			}
		}
	}

	/**
	 * Gibt eine kurze Beschreibung des Objektes zurück.
	 *
	 * @return Beschreibung des Objektes
	 */
	public String toString() {
		return toShortDebugString();
	}

	/**
	 * Gibt eine kurze Beschreibung des Objektes zurück.
	 *
	 * @return Beschreibung des Objektes
	 */
	public String toShortDebugString() {
		return getClass().getName() + "{" + toShortDebugParamString() + "}";
	}

	/**
	 * Bestimmt eine kurze Beschreibung der Eigenschaften eines Telegramms.
	 *
	 * @return Beschreibung der Eigenschaften eines Telegramms
	 */
	public String toShortDebugParamString() {
		return "";
	}
}

