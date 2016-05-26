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
 * Die Applikation meldet Daten, für die sie als Quelle oder Sender angemeldet war (siehe  Sendeanmeldung ), wieder ab.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class ReceiveUnsubscriptionTelegram extends DataTelegram {

	/** Die Abmeldeinformation */
	private BaseSubscriptionInfo unsubscription;

	/** Creates new ReceiveUnsubscriptionTelegram */
	public ReceiveUnsubscriptionTelegram() {
		type = RECEIVE_UNSUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Creates new ReceiveUnsubscriptionTelegram
	 *
	 * @param _unsubscription Die Abmeldung
	 */
	public ReceiveUnsubscriptionTelegram(BaseSubscriptionInfo _unsubscription) {
		type = RECEIVE_UNSUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		unsubscription = _unsubscription;
		length = 14;
	}

	/**
	 * Gibt die Abmeldeinformationen an
	 *
	 * @return Abmeldeinformation
	 */

	public final BaseSubscriptionInfo getUnSubscriptionInfo() {
		return unsubscription;
	}

	public final String parseToString() {
		String str = "Empfangsabmeldung Systemtelegramm: ";
		if(unsubscription != null) {
			str += unsubscription.toString();
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		unsubscription.write(out);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		unsubscription = new BaseSubscriptionInfo();
		unsubscription.read(in);
		length = 14;
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
