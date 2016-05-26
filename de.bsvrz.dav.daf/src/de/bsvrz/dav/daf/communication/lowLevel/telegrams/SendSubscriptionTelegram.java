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
 * Die Applikation meldet Daten als Sender oder Quelle an.
 * <p>
 * Eine Applikation muss beim Datenaustausch über den Datenverteiler die zu übertragenden Daten identifizieren. Dies geschieht durch Angabe folgender
 * Informationen:
 * <p>
 * Konfigurationsobjekt (Objekt-Id), Attributgruppe, Aspekt, Simulationsvariante
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class SendSubscriptionTelegram extends DataTelegram {

	/** Die Informationen der Sendeanmeldung */
	private SendSubscriptionInfo sendSubscriptionInfo;

	/** Erzeugt neues SendSubscriptionTelegram */
	public SendSubscriptionTelegram() {
		type = SEND_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues SendSubscriptionTelegram
	 *
	 * @param _sendSubscriptionInfo Die Informationen der Sendeanmeldung
	 */
	public SendSubscriptionTelegram(SendSubscriptionInfo _sendSubscriptionInfo) {
		type = SEND_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		sendSubscriptionInfo = _sendSubscriptionInfo;
		length = 16;
	}

	/**
	 * Gibt die Informationen diese Sendeanmeldung an.
	 *
	 * @return Die Informationen der Sendeanmeldung
	 */
	public final SendSubscriptionInfo getSendSubscriptionInfo() {
		return sendSubscriptionInfo;
	}

	public String parseToString() {
		String str = "Sendeanmeldung Systemtelegramm: \n";
		if(sendSubscriptionInfo != null) {
			str += sendSubscriptionInfo.parseToString();
		}
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		sendSubscriptionInfo.write(out);
	}

	public void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		sendSubscriptionInfo = new SendSubscriptionInfo();
		sendSubscriptionInfo.read(in);
		length = 16;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
