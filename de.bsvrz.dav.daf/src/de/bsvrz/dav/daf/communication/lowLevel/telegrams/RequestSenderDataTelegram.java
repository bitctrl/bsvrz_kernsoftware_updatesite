/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
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

import de.bsvrz.dav.daf.main.impl.CommunicationConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sendesteuerung. Aufforderung an die Applikation, Daten zu senden. Über dieses Telegramm kann der Datenverteiler das Sendeverhalten der Applikation steuern.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class RequestSenderDataTelegram extends DataTelegram {

	/** Benachrichtigungscode 0: Sendung starten */
	public static final byte START_SENDING = 0;

	/** Benachrichtigungscode 1: Sendung anhalten */
	public static final byte STOP_SENDING = 1;

	/** Benachrichtigungscode 2: Sendung anhalten keine Berechtigung */
	public static final byte STOP_SENDING_NO_RIGHTS = 2;

	/** Benachrichtigungscode 3: Sendung anhalten unzulässige Anmeldung(2 quellen, 2 Senken, 1 Quelle 1 senke und umgekehrt) */
	public static final byte STOP_SENDING_NOT_A_VALID_SUBSCRIPTION = 3;

	/** Die Information der Daten, die gesendet werden müssen */
	private BaseSubscriptionInfo dataInfo;

	/** Benachrichtigungscode */
	private byte state;

	public RequestSenderDataTelegram() {
		type = REQUEST_SENDER_DATA_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * erzeugt neues RequestSenderDataTelegram
	 *
	 * @param _dataInfo Die Information der Daten
	 * @param _state    Status
	 */
	public RequestSenderDataTelegram(BaseSubscriptionInfo _dataInfo, byte _state) {
		type = REQUEST_SENDER_DATA_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		dataInfo = _dataInfo;
		state = _state;
		length = 15;
	}

	/**
	 * Gibt die Anmeldeinformation an.
	 *
	 * @return AnmeldeInformation
	 */
	public final BaseSubscriptionInfo getDataToSendInfo() {
		return dataInfo;
	}

	/**
	 * Gibt den Status diese Nachricht an.
	 *
	 * @return Status des Telegramms (siehe Benachrichtigungscode)
	 */
	public final byte getState() {
		return state;
	}

	public final String parseToString() {
		String str = "Systemtelegramm Sendesteuerung ";
		str += dataInfo.toString();
		if(state == 0) {
			str += ", Sendung starten.";
		}
		else if(state == 1) {
			str += ", Sendung anhalten.";
		}
		else if(state == 2) {
			str += ", Sendung anhalten. Keine Berechtigung.";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		dataInfo.write(out);
		out.writeByte(state);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		dataInfo = new BaseSubscriptionInfo();
		dataInfo.read(in);
		state = in.readByte();
		length = 15;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
