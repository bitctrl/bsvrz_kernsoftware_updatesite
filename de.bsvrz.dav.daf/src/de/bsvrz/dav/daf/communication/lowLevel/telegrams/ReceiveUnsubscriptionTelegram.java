/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kniß Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
 * @version $Revision: 5084 $
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
