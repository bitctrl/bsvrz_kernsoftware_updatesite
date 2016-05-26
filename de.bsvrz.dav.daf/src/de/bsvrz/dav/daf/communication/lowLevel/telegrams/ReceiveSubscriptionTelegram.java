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
 * Die Applikation meldet Daten als Empfänger oder Senke an. Eine Applikation muss beim Datenaustausch über den Datenverteiler die zu empfangenden Daten
 * identifizieren. Dies geschieht durch Angabe folgender Informationen: Konfigurationsobjekt (Objekt-Id), Attributgruppe, Aspekt, Simulationsvariante Optional
 * kann spezifiziert werden, ob die Applikation auch nachgelieferte Daten erhalten möchte und ob nur geänderte Datensätze empfangen werden sollen. Weiter kann
 * ein Auswahl von bestimmten Attributen aus der zu empfangenden Attributgruppe getroffen werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */

public class ReceiveSubscriptionTelegram extends DataTelegram {

	/** Die Informationen der Empfangsanmeldung */
	private ReceiveSubscriptionInfo receiveSubscriptionInfo;

	/** Creates new ReceiveSubscriptionTelegram */
	public ReceiveSubscriptionTelegram() {
		type = RECEIVE_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues ReceiveSubscriptionTelegram
	 *
	 * @param _receiveSubscriptionInfo Die Informationen der Empfangsanmeldung
	 */
	public ReceiveSubscriptionTelegram(ReceiveSubscriptionInfo _receiveSubscriptionInfo) {
		type = RECEIVE_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		receiveSubscriptionInfo = _receiveSubscriptionInfo;
		length = receiveSubscriptionInfo.getLength();
	}

	/** @return Informationen der Empfangsanmeldung */

	public final ReceiveSubscriptionInfo getReceiveSubscriptionInfo() {
		return receiveSubscriptionInfo;
	}

	public String parseToString() {
		String str = "Empfangsanmeldung Systemtelegramm: \n";
		if(receiveSubscriptionInfo != null) {
			str += receiveSubscriptionInfo.parseToString();
		}
		return str;
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		receiveSubscriptionInfo.write(out);
	}

	public void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		receiveSubscriptionInfo = new ReceiveSubscriptionInfo();
		receiveSubscriptionInfo.read(in);
		length = receiveSubscriptionInfo.getLength();
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
