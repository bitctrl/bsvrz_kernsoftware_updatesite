/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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
 * Der Datenverteiler meldet Daten f�r Empf�nger oder Sender an. Wenn sich eine Applikation als Quelle oder Senke von Daten anmeldet, dann wird der verbundene
 * Datenverteiler zum Zentraldatenverteiler dieser Daten. Bei Anmeldungen als Sender oder Empf�nger besteht die Aufgabe des Datenverteilers darin, den
 * zugeh�rigen Zentraldatenverteiler ausfindig zu machen und �ber die Datenverteiler, die auf dem g�nstigsten Weg zum Zentraldatenverteiler liegen, eine
 * Anmeldekette aufzubauen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 7691 $
 */
public class TransmitterDataSubscription extends DataTelegram {

	/** Die Basisinformationen der Anmeldung */
	private BaseSubscriptionInfo _baseSubscriptionInfo;

	/** Die Information, ob die Anmeldung eine Sender- oder Empf�ngeranmeldung ist 0: Senderanmeldung 1: Empf�ngeranmeldung */
	private byte _subscriptionState;

	/** Die Liste der zu ber�cksichtigenden Datenverteiler */
	private long _transmitterList[];

	public TransmitterDataSubscription() {
		type = TRANSMITTER_DATA_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * Erzeugt neues TransmitterDataSubscription-Telegramm.
	 *
	 * @param baseSubscriptionInfo Basisinformationen
	 * @param subscriptionState    Anmeldung als Sender oder Empf�nger (0: Senderanmeldung 1: Empf�ngeranmeldung)
	 * @param transmitterList      Liste der zu ber�cksichtigenden Datenverteiler
	 */
	public TransmitterDataSubscription(BaseSubscriptionInfo baseSubscriptionInfo, byte subscriptionState, long transmitterList[]) {
		type = TRANSMITTER_DATA_SUBSCRIPTION_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;

		_baseSubscriptionInfo = baseSubscriptionInfo;
		_subscriptionState = subscriptionState;
		_transmitterList = transmitterList;
		length = 17;
		if(_transmitterList != null) {
			length += (_transmitterList.length * 8);
		}
	}

	/**
	 * Gibt die Basisanmeldeinformationen zur�ck.
	 *
	 * @return die Basisanmeldeinformationen
	 */
	public final BaseSubscriptionInfo getBaseSubscriptionInfo() {
		return _baseSubscriptionInfo;
	}

	/**
	 * Gibt den Status der Anmeldung zur�ck 0: Senderanmeldung 1: Empf�ngeranmeldung.
	 *
	 * @return Status der Anmeldung
	 */
	public final byte getSubscriptionState() {
		return _subscriptionState;
	}

	/**
	 * Gibt die Liste der zu ber�cksichtigenden Datenverteiler zur�ck.
	 *
	 * @return die Liste der zu ber�cksichtigenden Datenverteiler
	 */
	public final long[] getTransmitters() {
		return _transmitterList;
	}

	public final String parseToString() {
		String str = "Datenverteiler Datenanmeldung Systemtelegramm: ";
		str += _baseSubscriptionInfo.toString();
		if(_subscriptionState == TransmitterSubscriptionsConstants.SENDER_SUBSCRIPTION) {
			str += ", Anmeldung als Sender";
		}
		else {
			str += ", Anmeldung als Empf�nger";
		}
		if(_transmitterList != null) {
			str += ", Potentielle Datenverteiler [ ";
			for(int i = 0; i < _transmitterList.length; ++i) {
				str += " " + _transmitterList[i] + " ";
			}
			str += " ]";
		}
		return str;
	}

	public final void write(DataOutputStream out) throws IOException {
		out.writeShort(length);
		_baseSubscriptionInfo.write(out);
		out.writeByte(_subscriptionState);
		if(_transmitterList == null) {
			out.writeShort(0);
		}
		else {
			out.writeShort(_transmitterList.length);
			for(int i = 0; i < _transmitterList.length; ++i) {
				out.writeLong(_transmitterList[i]);
			}
		}
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_baseSubscriptionInfo = new BaseSubscriptionInfo();
		_baseSubscriptionInfo.read(in);
		_subscriptionState = in.readByte();
		
		length = 17;
		int size = in.readShort();
		if(size != 0) {
			_transmitterList = new long[size];
			for(int i = 0; i < size; ++i) {
				_transmitterList[i] = in.readLong();
			}
			length += (_transmitterList.length * 8);
		}
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}
