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
 * Der Datenverteiler meldet Daten für Empfänger oder Sender bei einem anderen Datenverteiler ab.
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public class TransmitterDataUnsubscription extends DataTelegram {

	/** Die Basisinformationen eine Anmeldung */
	private BaseSubscriptionInfo baseSubscriptionInfo;

	/** Die Information ob die Anmeldung als Sender- oder Empfängeranmeldung ist 0: Senderanmeldung 1: Empfängeranmeldung */
	private byte subscriptionState;

	/** Die Liste der zu berücksichtigenden Datenverteiler */
	private long transmitterList[];

	public TransmitterDataUnsubscription() {
		type = TRANSMITTER_DATA_UNSUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 *
	 * @param _baseSubscriptionInfo Basisinformationen
	 * @param _subscriptionState    Anmeldung als Sender oder Empfänger
	 * @param _transmitterList      Liste der zu berücksichtigenden Datenverteiler
	 */
	public TransmitterDataUnsubscription(BaseSubscriptionInfo _baseSubscriptionInfo, byte _subscriptionState, long _transmitterList[]) {
		type = TRANSMITTER_DATA_UNSUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		baseSubscriptionInfo = _baseSubscriptionInfo;
		subscriptionState = _subscriptionState;
		transmitterList = _transmitterList;
		length = 17;
		if(transmitterList != null) {
			length += (transmitterList.length * 8);
		}
	}

	/**
	 * Gibt die Basisanmeldeinformationen zurück.
	 *
	 * @return die Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return baseSubscriptionInfo;
	}

	/**
	 * Gibt der Status der Anmeldung zurück 0: Senderanmeldung 1: Empfängeranmeldung
	 *
	 * @return der Status der Anmeldung
	 */
	public final byte getSubscriptionState() {
		return subscriptionState;
	}

	/**
	 * Gibt die Liste der zu berücksichtigenden Datenverteiler zurück
	 *
	 * @return die Liste der zu berücksichtigenden Datenverteiler
	 */
	public final long[] getTransmitters() {
		return transmitterList;
	}

	public final String parseToString() {
		String str = "Datenverteiler Datenabmeldung Systemtelegramm: ";
		str += baseSubscriptionInfo.toString();
		if(subscriptionState == TransmitterSubscriptionsConstants.SENDER_SUBSCRIPTION) {
			str += ", Anmeldung als Sender";
		}
		else {
			str += ", Anmeldung als Empfänger";
		}
		if(transmitterList != null) {
			str += ", Potentielle Datenverteiler [ ";
			for(int i = 0; i < transmitterList.length; ++i) {
				str += " " + transmitterList[i] + " ";
			}
			str += " ]";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		baseSubscriptionInfo.write(out);
		out.writeByte(subscriptionState);
		if(transmitterList == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(transmitterList.length);
			for(int i = 0; i < transmitterList.length; ++i) {
				out.writeLong(transmitterList[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		baseSubscriptionInfo = new BaseSubscriptionInfo();
		baseSubscriptionInfo.read(in);
		subscriptionState = in.readByte();
		length = 17;
		int size = in.readShort();
		if(size != 0) {
			transmitterList = new long[size];
			for(int i = 0; i < size; ++i) {
				transmitterList[i] = in.readLong();
			}
			length += (transmitterList.length * 8);
		}
		if(length != _length) {
			throw new IOException("Falsche Telegrammlänge");
		}
	}
}
