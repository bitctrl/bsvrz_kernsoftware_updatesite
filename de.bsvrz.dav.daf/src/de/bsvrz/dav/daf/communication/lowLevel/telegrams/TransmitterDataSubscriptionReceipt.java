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
 * Der Datenverteiler quittiert eine an ihn gerichtete Datenanmeldung.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public class TransmitterDataSubscriptionReceipt extends DataTelegram {

	/** Die Basisinformationen der Anmeldung */
	private BaseSubscriptionInfo _baseSubscriptionInfo;

	/** Die Information, ob die Anmeldung eine Sender- oder Empf�ngeranmeldung ist 0: Senderanmeldung 1: Empf�ngeranmeldung */
	private byte _subscriptionState;

	/**
	 * Die Quittung f�r die Anmeldung: 0: Keiner der angemeldeten Zentraldatenverteiler ist f�r die Daten zust�ndig. 1: Der spezifizierte Datenverteiler ist der
	 * Zust�ndige f�r die Daten. 2: Der spezifizierte Datenverteiler ist der Zust�ndige f�r die Daten, die notwendigen Rechte sind aber nicht vorhanden.
	 */
	private byte _receipt;

	/**
	 * Der Zentraldatenverteiler der f�r die angemeldeten Daten zust�ndig ist. Wenn kein zentraler Datenverteiler f�r die angemeldeten Daten existiert, dann wird
	 * -1 �bertragen.
	 */
	private long _transmitterId;

	/** Die Liste der zu ber�cksichtigenden Datenverteiler */
	private long _transmitterList[];

	public TransmitterDataSubscriptionReceipt() {
		type = TRANSMITTER_DATA_SUBSCRIPTION_RECEIPT_TYPE;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
	}

	/**
	 * @param baseSubscriptionInfo Basisinformationen
	 * @param subscriptionState    Information ob die Anmeldung ein  Sender oder Empf�nger ist
	 * @param receipt              Quittung
	 * @param transmitterId        ID des Zentraldatenverteilers
	 * @param transmitterList      Liste der zu ber�cksichtigenden Datenverteiler
	 */
	public TransmitterDataSubscriptionReceipt(
			BaseSubscriptionInfo baseSubscriptionInfo, byte subscriptionState, byte receipt, long transmitterId, long transmitterList[]
	) {
		type = TRANSMITTER_DATA_SUBSCRIPTION_RECEIPT_TYPE;
		_baseSubscriptionInfo = baseSubscriptionInfo;
		_subscriptionState = subscriptionState;
		_receipt = receipt;
		_transmitterId = transmitterId;
		_transmitterList = transmitterList;
		priority = CommunicationConstant.SYSTEM_TELEGRAM_PRIORITY;
		length = 26;
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
	 * @return der Status der Anmeldung
	 */
	public final byte getSubscriptionState() {
		return _subscriptionState;
	}

	/**
	 * Gibt den Status der Quittung zur�ck 0: Keiner der angemeldeten Zentraldatenverteiler ist f�r die Daten zust�ndig. 1: Der spezifizierte Datenverteiler ist
	 * der Zust�ndige f�r die Daten. 2: Der spezifizierte Datenverteiler ist der Zust�ndige f�r die Daten, die notwendigen Rechte sind aber nicht vorhanden.
	 *
	 * @return der Status der Quittung
	 */
	public final byte getReceipt() {
		return _receipt;
	}

	/**
	 * Gibt die Id des f�r die angemeldeten Daten zust�ndigen Zentraldatenverteilers zur�ck.
	 *
	 * @return die Id des Zentraldatenverteilers
	 */
	public final long getMainTransmitterId() {
		return _transmitterId;
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
		String str = "Datenverteiler Datenanmeldungsquittung Systemtelegramm: ";
		str += _baseSubscriptionInfo.toString();

		if(_subscriptionState == TransmitterSubscriptionsConstants.SENDER_SUBSCRIPTION) {
			str += ", Anmeldung als Sender";
		}
		else {
			str += ", Anmeldung als Empf�nger";
		}

		if(_receipt == TransmitterSubscriptionsConstants.NEGATIV_RECEIP) {
			str += ", Kein Zentraldatenverteiler f�r die angemeldeten Daten vorhanden";
		}
		else if(_receipt == TransmitterSubscriptionsConstants.POSITIV_RECEIP) {
			str += ", Zentraldatenverteiler f�r die angemeldeten Daten vorhanden";
			str += ", Zentraldatenverteiler Id: " + _transmitterId;
		}
		else {
			str += ", Zentraldatenverteiler f�r die angemeldeten Daten gefunden aber keine Zugriffsrechte vorhanden.";
			str += ", Zentraldatenverteiler Id: " + _transmitterId;
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
		out.writeByte(_receipt);
		out.writeLong(_transmitterId);
	}

	public final void read(DataInputStream in) throws IOException {
		int _length = in.readShort();
		_baseSubscriptionInfo = new BaseSubscriptionInfo();
		_baseSubscriptionInfo.read(in);
		_subscriptionState = in.readByte();
		length = 26;
		int size = in.readShort();
		if(size != 0) {
			_transmitterList = new long[size];
			for(int i = 0; i < size; ++i) {
				_transmitterList[i] = in.readLong();
			}
			length += (_transmitterList.length * 8);
		}
		_receipt = in.readByte();
		_transmitterId = in.readLong();
		if(length != _length) {
			throw new IOException("Falsche Telegramml�nge");
		}
	}
}
